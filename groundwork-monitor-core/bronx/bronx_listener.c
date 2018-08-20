/*
 *  bronx_listener.c -- Listening for external messages.
 *
 *  Copyright (c) 2007-2017 Groundwork Open Source
 *  Originally written by Daniel Emmanuel Feinsmith.
 *  Now extensively revised.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor
 *  Boston, MA  02110-1301, USA.
 *
 *	2007-09-17 DEF;		Initial creation.
 *	2008, 2009 HK, GH;	Fixes too numerous to mention here.
 *	2010-07-20 GH;		Added support for the version 101 packet,
 *				based on earlier work by HK.
 *	2012-05-14 GH;		Added support for an anonymous pipe to
 *				break out of poll() before timeout.
 *	2012-06-22 GH;		Cleaned up strerror() code that was not
 *				production-ready (it was neither portable
 *				nor thread-safe).  Improved error handling.
 *	2012-12-05 GH;		Fixed the handling of an unexpected closure of
 *				the listening socket, which was inadvertently
 *				broken while adding the anonymous-pipe support.
 *				Made memory allocation more efficient.
 *				Fixed some probable small memory leaks.
 *				Fixed some network byte-ordering conversions.
 *				Improved debug messages.
 *	2012-12-07 GH;		Limit the total number of client connections, so
 *				the process does not run out of file descriptors.
 *	2012-12-11 GH;		Implement forcibly closing idle connections.
 *	2015-02-26 GH;		Extend log messages to better identify connections.
 *	2017-01-29 GH;		Updated to support Nagios 4.2.4.
 */

// STUFF TO THINK ABOUT:
// * Previously, we got a client disconnect message in the log file,
//   because handle_connection_read() was called at the end even when
//   there was no message waiting.  Now, maybe not; this remains to be
//   tested.  Does that matter to any of the functionality in the code,
//   or to track which sockets are being closed?

#include <errno.h>

#include "bronx.h"
#include "bronx_admin.h"
#include "bronx_config.h"
#include "bronx_listener_common.h"
#include "bronx_listener_defines.h"
#include "bronx_listener_netutils.h"
#include "bronx_listener_utils.h"
#include "bronx_listener.h"
#include "bronx_nagios.h"
#include "bronx_log.h"
#include "bronx_safe_fork.h"
#include "bronx_thread.h"
#include "bronx_utils.h"

#include <time.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <stdint.h>

/*
 *  Globals
 */

struct handler_entry    *rhand = NULL;
struct handler_entry    *whand = NULL;
struct pollfd           *pfds  = NULL;
struct file_descriptor	*fdesc = NULL;
int                     maxrhand  = 0;
int                     maxwhand  = 0;
int                     maxpfds   = 0;
int                     fdescsize = 0;
int                     nrhand    = 0;
int                     nwhand    = 0;
int                     npfds     = 0;
int                     stopfd    = -1;

/*
 *  Externs
 */

// Declaration needed here because of unavoidable forward references.
static int handle_connection_read(int sock, void *data, int cleanup_only);

#define DEBUG_DETAIL_NONE	0
#define DEBUG_DETAIL_FATAL	1
#define DEBUG_DETAIL_ERROR	2
#define DEBUG_DETAIL_WARNING	3
#define DEBUG_DETAIL_NOTICE	4
#define DEBUG_DETAIL_STATS	5
#define DEBUG_DETAIL_INFO	6
#define DEBUG_DETAIL_DEBUG	7

#ifdef SUPPRESS_DEBUG_DETAIL
#define DEBUG_DETAIL_LEVEL DEBUG_DETAIL_NONE
#endif

#ifndef DEBUG_DETAIL_LEVEL
#define DEBUG_DETAIL_LEVEL DEBUG_DETAIL_NOTICE
#endif

// How many empty structures to allocate at once, each time we need
// to extend the number of elements in rhand[], whand[], or pfds[].
// We want this number to be larger than 1 both to avoid leaving
// behind large memory areas in the free list that never get filled in,
// and to avoid the memory-copying overhead of realloc() on every single
// extension of such an array.
#define	ALLOCATION_BLOCK_SIZE	10

// FIX MINOR:  this ought to be set in bronx.cfg, not hardcoded here
int debug_detail_level = DEBUG_DETAIL_LEVEL;

// Obviously we can't have an unlimited number of file descriptors,
// but we'll pretend we did until this value is properly initialized.
rlim_t max_file_descriptors_in_process = RLIM_INFINITY;

// bronx_logprintf() doesn't support the %zu conversion specification (yet),
// because the underlying apr_vformatter() routine (called by apr_pvsprintf())
// in the Apache Portability Runtime (APR) library doesn't support the z modifier.
//
// In the meantime, we specify a symbol here to allow an equivalent format
// to be used, that is effectively portable between 32-bit and 64-bit platforms
// by making its value conditionally defined.  The value of the symbol has been
// adjusted to what actually works in each environment, based not only on the
// platform but also the system header file definitions and the degree to which
// the compiler detects format mismatches with the associated variables.  In a
// 64-bit (_LP64) environment, "%lu" is the closest match to "%zu", and in a
// 32-bit environment, "%u" works.  "%lu" ought to work in a 32-bit environment
// as well, considering that longs in that environment are 32 bits, but we find
// the GCC compiler is picky and tries to protect us against writing code that
// it believes won't be portable to compiling in a 64-bit environment (without
// actually understanding that the symbol definition would work there).
//
// What we would use if it were supported:
// #define SIZE_T_FORMAT "%zu"
//
// What we need to use instead:
#if defined(_LP64)
#define SIZE_T_FORMAT "%lu"
#else
#define SIZE_T_FORMAT "%u"
#endif

// There is no standard means to format a time_t value, partly because it has no
// standard definition; it may be an integral type or a floating-point type.  In
// practice on modern UNIX-like platforms, it is an integral type, but it may be
// 32-bit or 64-bit.  Our only real hope here is to cast the value to a large
// integral type, then use a corresponding format specifier.
//
// C99 defined the "j" modifier for just this type of usage, but we do also have
// to separately cast the value we're going to format to (intmax_t) to ensure that
// it is integral and not a floating-point value.  intmax_t is from <stdint.h>.
//
// Unfortunately, as with the case of the z modifier above, the j modifier is not
// supported by the APR routines.  Nor is the ll modifier ("%lld").
//
// What we would use if it were supported:
// #define TIME_T_FORMAT "%jd"
//
// The only thing we can apparently use instead is the following, although it's not
// clear that this would suffice if time_t were a 64-bit quantity.  We define a
// compatible casting type to use so we can guarantee that these two specifications
// are maintained centrally in tandem.
#define TIME_T_FORMAT		"%ld"
#define	TIME_T_CAST_TYPE	long


static void
close_all_sockets()
{
    int i;
    /* Buffer to store peer IP address */
    char address[MAX_IPADDR_LENGTH];

    for (i = 0; i < nrhand; i++)
    {
	// Note:  We never have to worry about double-free() errors because rhand[i] is
	// always removed from the rhand[] array before a handler is called, and that handler
	// is responsible for either cleaning up the object itself or queueing up another
	// handler and thereby saving the pointer for another round.  So there will always
	// be just one copy of the .data pointer sitting around somewhere, either in rhand[]
	// or in the handler, and that's the one that will be used to delete the object.
	if (rhand[i].data != NULL)
	{
	    // Note that we really only use the .data for one purpose, so we know exactly
	    // what function to call here.  In a more general application, we would need to
	    // store an indication of the correct cleanup function in the rhand[] element.
	    encrypt_cleanup(_configuration->listener_encryption_method, rhand[i].data);
	    rhand[i].data = NULL;
	}
    }
    nrhand = 0;

    for (i = 0; i < nwhand; i++)
    {
	if (whand[i].data != NULL)
	{
	    // There is nothing to do here, as we currently never use the .data member for write handles.
	}
    }
    nwhand = 0;

    // pfds[0] is for the stopfd, which we never close here in the listener, so we skip that index here.
    // pfds[1] should be the primary listener socket, which we will close here just like any socket
    // opened in response to a client connection on the primary listener socket.
    // pfds[2...] are client-connection sockets.
    int port;
    char port_string[30];
    for (i = 1; i < npfds; i++)
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_full_peer_address(pfds[i].fd, address, sizeof(address), &port);
	    if (port < 0)
	    {
		snprintf(port_string, sizeof(port_string), "(unknown port)");
	    }
	    else
	    {
		snprintf(port_string, sizeof(port_string), "port %d", port);
	    }

	    // Note that the "from client" part really doesn't apply to the listener socket,
	    // which should essentially never have a peer address, but we want to pretend it
	    // does in case the listener socket was inadvertently closed at some previous time
	    // and that slot is now occupied by some other socket.
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{close_all_sockets} Forcibly closing connection from client %s %s (file descriptor %d); some data loss may occur.",
		address, port_string, pfds[i].fd);
	}
	bronx_safe_close(pfds[i].fd);
    }
    // Account for keeping stopfd in the array.
    npfds = npfds ? 1 : 0;
}

static void
listener_thread_exit(char *cause_string, int return_code)
{
    // This call might be redundant in normal operation, but it's here for safety, to close down
    // all outstanding resources if we ever abort out of the middle of the thread.
    close_all_sockets();

    // FIX MINOR:  There is perhaps a resource leak here, in that the stopfd was intentionally
    // not just closed, and it won't be closed unless threads_stop() is somehow called.  This
    // will be an issue if we ever extend Bronx to support a Nagios reload, not just a restart.
    // If you think that there is no resource leak, then you should document here exactly what
    // path is followed in the calling code to close the stopfd.

    // Force the count to zero (discard the stopfd, which will be closed by calling code).
    // This is necessary so this count is properly initialized for another round, upon reload.
    npfds = 0;

    /*** CLEAR SENSITIVE INFO FROM MEMORY ***/

    /* overwrite _configuration->password */
    clear_buffer(_configuration->listener_password, sizeof(_configuration->listener_password));

    /* disguise decryption method */
    _configuration->listener_encryption_method=-1;

    /* print our final log message */
    bronx_logprintf(BRONX_LOGGING_INFO, "{listener_thread_exit} Terminating Listener Thread, Cause: %s", cause_string);

    /* and leave */
    apr_thread_exit(_thread_listener, return_code);
}

/* register a file descriptor to be polled for an event set */
static int
register_poll(short events, int fd)
{
    int i;

    // If it's already in the list, just flag the events and return.
    // FIX MINOR:  This linear lookup is horribly inefficient (has O(n^^2) scalability)
    // if/when there is ever a substantial number of file descriptors to search.  We
    // should perhaps replace this lookup by using a parallel hash structure.
    for (i = 0; i < npfds; i++)
    {
	if (pfds[i].fd == fd)
	{
	    pfds[i].events |= events;
	    return BRONX_OK;
	}
    }

    // else add it to the list
    if (maxpfds == 0)
    {
	maxpfds += ALLOCATION_BLOCK_SIZE;
	pfds = malloc(sizeof(struct pollfd) * maxpfds);
    }
    else if (npfds + 1 > maxpfds)
    {
	maxpfds += ALLOCATION_BLOCK_SIZE;
	pfds = realloc(pfds, sizeof(struct pollfd) * maxpfds);
    }
    if (pfds == NULL) {
	bronx_log("{register_poll} Failed to allocate memory for socket pool", BRONX_LOGGING_ERROR);
	return BRONX_ABORT;
    }

    pfds[npfds].fd      = fd;
    pfds[npfds].events  = events;
    pfds[npfds].revents = 0;
    npfds++;

    // For the fdesc[] array, we use simple and fast direct indexing using the (always non-negative)
    // file descriptor, rather than some lengthy search process.  We don't care whether we waste a bit
    // of storage at the beginning of the array or in the middle of the array for file descriptors
    // that don't belong to client connections, nor do we care about trying to periodically compact
    // the array.  Fast access and O(1) scalability are the most important criteria.  If we saw we
    // had performance problems with rhand[] and whand[] lookups at large scale, we might change the
    // historic implementation of those arrays to use a similar strategy.
    if (fdescsize == 0)
    {
	fdescsize = fd + ALLOCATION_BLOCK_SIZE;
	fdesc = malloc(sizeof(struct file_descriptor) * fdescsize);
    }
    else if (fd >= fdescsize)
    {
	fdescsize = fd + ALLOCATION_BLOCK_SIZE;
	fdesc = realloc(fdesc, sizeof(struct file_descriptor) * fdescsize);
    }
    if (fdesc == NULL) {
	bronx_log("{register_poll} Failed to allocate memory for file descriptor pool", BRONX_LOGGING_ERROR);
	return BRONX_ABORT;
    }

    fdesc[fd].last_active_time = time(0);

    return BRONX_OK;
}

/* register a read handler */
static int
register_read_handler(int fd, int (*fp)(int, void *, int), void *data)
{
    int i;

    /* register our interest in this descriptor */
    int outcome = register_poll(POLLIN, fd);
    if (outcome != BRONX_OK)
    {
	return outcome;
    }

    /* if it's already in the list, just update the handler */
    // FIX MINOR:  This linear lookup is horribly inefficient (has O(n^^2) scalability)
    // if/when there is ever a substantial number of file descriptors to search.  We
    // should perhaps replace this lookup by using a parallel hash structure.
    for (i = 0; i < nrhand; i++)
    {
	if (rhand[i].fd == fd)
	{
	    rhand[i].handler = fp;
	    rhand[i].data    = data;
	    return BRONX_OK;
	}
    }

    /* else add it to the list */
    if (maxrhand == 0)
    {
	maxrhand += ALLOCATION_BLOCK_SIZE;
	rhand = malloc(sizeof(struct handler_entry) * maxrhand);
    }
    else if (nrhand+1 > maxrhand)
    {
	maxrhand += ALLOCATION_BLOCK_SIZE;
	rhand = realloc(rhand, sizeof(struct handler_entry) * maxrhand);
    }
    if (rhand == NULL) {
	bronx_log("{register_read_handler} Failed to allocate memory for read handler pool", BRONX_LOGGING_ERROR);
	return BRONX_ABORT;
    }

    rhand[nrhand].fd      = fd;
    rhand[nrhand].handler = fp;
    rhand[nrhand].data    = data;
    nrhand++;

    return BRONX_OK;
}

/* register a write handler */
static int
register_write_handler(int fd, int (*fp)(int, void *, int), void *data)
{
    int i;

    /* register our interest in this descriptor */
    int outcome = register_poll(POLLOUT, fd);
    if (outcome != BRONX_OK)
    {
	return outcome;
    }

    /* if it's already in the list, just update the handler */
    // FIX MINOR:  This linear lookup is horribly inefficient (has O(n^^2) scalability)
    // if/when there is ever a substantial number of file descriptors to search.  We
    // should perhaps replace this lookup by using a parallel hash structure.
    for (i = 0; i < nwhand; i++)
    {
	if (whand[i].fd == fd)
	{
	    whand[i].handler = fp;
	    whand[i].data    = data;
	    return BRONX_OK;
	}
    }

    /* else add it to the list */
    if (maxwhand == 0)
    {
	maxwhand += ALLOCATION_BLOCK_SIZE;
	whand = malloc(sizeof(struct handler_entry) * maxwhand);
    }
    else if (nwhand+1 > maxwhand)
    {
	maxwhand += ALLOCATION_BLOCK_SIZE;
	whand = realloc(whand, sizeof(struct handler_entry) * maxwhand);
    }
    if (rhand == NULL) {
	bronx_log("{register_write_handler} Failed to allocate memory for write handler pool", BRONX_LOGGING_ERROR);
	return BRONX_ABORT;
    }

    whand[nwhand].fd      = fd;
    whand[nwhand].handler = fp;
    whand[nwhand].data    = data;
    nwhand++;

    return BRONX_OK;
}

static void
unregister_read_handler(int i)
{
    if (i < 0 || i >= nrhand)
    {
	listener_thread_exit("unregister_read_handler: attempt to delete impossible read handler", STATE_CRITICAL);
    }
    nrhand--;
    rhand[i] = rhand[nrhand];
}

static void
unregister_write_handler(int i)
{
    if (i < 0 || i >= nwhand)
    {
	listener_thread_exit("unregister_write_handler: attempt to delete impossible write handler", STATE_CRITICAL);
    }
    nwhand--;
    whand[i] = whand[nwhand];
}

/* find read handler */
static int
find_rhand(int fd)
{
    int i;

    // FIX MINOR:  This linear lookup is horribly inefficient (has O(n^^2) scalability)
    // if/when there is ever a substantial number of file descriptors to search.  We
    // should perhaps replace this lookup by using a parallel hash structure.
    for (i = 0; i < nrhand; i++)
    {
	if (rhand[i].fd == fd)
	    return i;
    }

    /* we couldn't find the read handler */
    listener_thread_exit("find_rhand: Read handler stack is corrupt", STATE_CRITICAL);

    return(-1);
}

/* find write handler */
static int
find_whand(int fd)
{
    int i;

    // FIX MINOR:  This linear lookup is horribly inefficient (has O(n^^2) scalability)
    // if/when there is ever a substantial number of file descriptors to search.  We
    // should perhaps replace this lookup by using a parallel hash structure.
    for (i = 0; i < nwhand; i++)
    {
	if (whand[i].fd == fd)
	    return i;
    }

    /* we couldn't find the write handler */
    listener_thread_exit("find_whand: Write handler stack is corrupt", STATE_CRITICAL);

    return(-1);
}

/*
 * handle pending events
 */

static int
handle_events(void)
{
    int (*handler)(int, void *, int);
    void *data;
    int i, hand, result;
    int status = BRONX_OK;

    // We should never be waiting here only for the stopfd.
    if (npfds <= 1)
    {
	bronx_logprintf (BRONX_LOGGING_ERROR, "{handle_events} error: polling for %s",
	    (npfds <= 0) ? "no descriptors at all" : "nothing beyond the stop descriptor");
	return (npfds <= 0 ? BRONX_ABORT : BRONX_ERROR);
    }

    if (! bronx_is_terminating())
    {
	// With a non-zero timeout, this is a blocking call.
	result = poll(pfds, npfds, BRONX_LISTENER_POLL_TIMEOUT);

	// Capture the time just once when the poll() finished.  We'll use this many times below.
	time_t now = time(0);

	int check_for_dead_connections = 0;

	// Understand fully what happened to break us out of the poll.
	if (result < 0)
	{
	    // An error occurred.
	    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
	    bronx_logprintf (BRONX_LOGGING_ERROR, "{handle_events} error: poll() returned errno=%d (%s)",
		errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	}
	else if (result == 0)
	{
	    // The poll() call timed out.
	    //
	    // The count of "read handles" here does not include the stopfd pipe, which is separately prepped for
	    // reading.  Therefore, so as not to cause confusion because the numbers don't add up, we list the stopfd
	    // pipe as a fixed count.  And then if the numbers don't add up, we have a different sort of problem.
	    bronx_logprintf (BRONX_LOGGING_DEBUG,
		"{handle_events} poll(%d descriptors: 1 pipe, %d read handle%s, %d write handle%s) timed out.",
		npfds, nrhand, nrhand == 1 ? "" : "s", nwhand, nwhand == 1 ? "" : "s");

	    // If the user has bumped up the logging to an extreme level, we'll get lots of detail that
	    // might help us to understand how client sockets could be waiting, still connected, for a
	    // long time and not sending in any data.
	    if (log_getlevel() >= BRONX_LOGGING_DEVELOP)
	    {
		for (i = 0; i < npfds; i++)
		{
		    bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} pfds[%d].fd = %d", i, pfds[i].fd);
		    // FIX LATER:  Perhaps we should dump out fdesc[pfds[i].fd].last_active_time here as well.
		}
		for (i = 0; i < nrhand; i++)
		{
		    bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} rhand[%d].fd = %d", i, rhand[i].fd);
		}
		for (i = 0; i < nwhand; i++)
		{
		    bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} whand[%d].fd = %d", i, whand[i].fd);
		}
	    }

	    // FIX MINOR:  The only types of connections we expect are (1) transient port-probe
	    // connections from check_tcp, (2) GDMA server-test connections via Net::Telnet that
	    // are immediately closed, simply to tell whether the server is available [GDMA-407:
	    // we ought to revise the GDMA spooler logic to avoid pre-testing Bronx connections,
	    // and to simply recover appropriately if an actual send_nsca call fails, to avoid a
	    // concentration of such noise on the server side], and (3) data-carrying connections
	    // from send_nsca.  So any client connection which has remained connected through the
	    // entire polling timeout interval should have been eager to transmit or receive data.
	    //
	    // If we see this case in production again, and there are lots of persistent inactive
	    // client socket connections, we want to understand how that situation could possibly
	    // come about.  We'll investigate by looking at in-depth customer logs.
	    //
	    // Partly to deal with this, we now drop some client connections from the server side,
	    // based on how long each client has remained inactive.  (We're trying to prevent an
	    // apparent resource-leak buildup of inactive file descriptors in this process.)

	    // This is an excellent time to run through all the file descriptors and clean out any idle client connections.
	    // In fact, since the poll() just timed out, we expect rather a lot of the connections might be good candidates,
	    // depending on the relative lengths of the hardcoded poll timeout and the configured idle-connection timeout.
	    //
	    // When checking for idle connections, we have to skip the stopfd at pfds[0] and the listener socket at pfds[1].
	    for (i = 2; i < npfds; i++)
	    {
		if (now > fdesc[pfds[i].fd].last_active_time + _configuration->idle_connection_timeout)
		{
		    bronx_logprintf (BRONX_LOGGING_DEBUG,
			"{handle_events} file descriptor %d was inactive for " TIME_T_FORMAT " seconds, is being closed",
			pfds[i].fd, (TIME_T_CAST_TYPE) (now - fdesc[pfds[i].fd].last_active_time));

		    // Aside from marking the socket as should-be-closed-from-the-server-side, we need to be
		    // careful in how any associated data is cleaned up, to avoid further resource leaks.
		    // The code below does that for other types of problems, and can be taken as a guideline.
		    if (pfds[i].events & POLLIN)
		    {
			hand = find_rhand(pfds[i].fd);
			if (hand >= 0)
			{
			    if (0)
			    {
				// This message is useful in showing whether the .data field is 0 or some other value.
				// %p is not handled by the routines underlying bronx_logprintf(),
				// so we have to do that conversion ourselves.
				char ptrbuf[30];
				snprintf(ptrbuf, sizeof(ptrbuf), "%p", rhand[hand].data);
				bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} rhand[%d].data = %s", hand, ptrbuf);
			    }

			    handler = rhand[hand].handler;
			    data    = rhand[hand].data;
			    rhand[hand].handler = NULL;
			    rhand[hand].data    = NULL;
			    unregister_read_handler(hand);
			    // The handler knows how to interpret the .data element and do the right kind of cleanup.
			    // Call the handler just to do .data cleanup, if any.
			    if (handler (pfds[i].fd, data, 1) != BRONX_OK)
			    {
				// Unexpected condition, but nothing special to do here.
			    }
			}
		    }
		    else if (pfds[i].events & POLLOUT)
		    {
			hand = find_whand(pfds[i].fd);
			if (hand >= 0)
			{
			    if (0)
			    {
				// This message is useful in showing whether the .data field is 0 or some other value.
				// %p is not handled by the routines underlying bronx_logprintf(),
				// so we have to do that conversion ourselves.
				char ptrbuf[30];
				snprintf(ptrbuf, sizeof(ptrbuf), "%p", whand[hand].data);
				bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} whand[%d].data = %s", hand, ptrbuf);
			    }

			    handler = whand[hand].handler;
			    data    = whand[hand].data;
			    whand[hand].handler = NULL;
			    whand[hand].data    = NULL;
			    unregister_write_handler(hand);
			    // The handler knows how to interpret the .data element and do the right kind of cleanup.
			    // Call the handler just to do .data cleanup, if any.
			    if (handler (pfds[i].fd, data, 1) != BRONX_OK)
			    {
				// Unexpected condition, but nothing special to do here.
			    }
			}
		    }
		    // invalidate pfds[i] so we will close pfds[i].fd below
		    pfds[i].events = 0;
		}
	    }
	    check_for_dead_connections = 1;
	}

	// Only run through this loop if something actually happened.
	if (result > 0)
	{
	    // We test pfds[0] to see if we got any input on the stopfd, only for logging purposes.
	    // Regardless, we don't even bother to read it; we just process any other file descriptors
	    // that are now ready, then exit the routine and allow testing of the bronx_is_terminating()
	    // flag outside of this routine to control large-scale looping.  It will be up to the caller
	    // to re-initialize the pipe and the entire process so the wakeup byte we don't read here
	    // doesn't cause us to continually invoke this logging.
	    if (pfds[0].revents) {
		// A principle reason for logging this information is to identify if this file descriptor
		// ever gets unexpectedly closed for any reason.  We only expect to ever see POLLIN here,
		// when there is a wakeup byte waiting to be read.  If we ever see POLLERR or POLLHUP, we
		// have an actual problem to solve.
		bronx_logprintf (
		    (pfds[0].revents & (POLLERR | POLLHUP)) ? BRONX_LOGGING_ERROR : BRONX_LOGGING_INFO,
		    "{handle_events} listener pipe file descriptor %d state is%s%s%s",
		    pfds[0].fd,
		    pfds[0].revents & POLLIN   ? " POLLIN"   : "",
		    pfds[0].revents & POLLERR  ? " POLLERR"  : "",
		    pfds[0].revents & POLLHUP  ? " POLLHUP"  : ""
		);
		// FIX MINOR:  Perhaps we should check for (POLLERR | POLLHUP) and exit the listener
		// thread if we find such an egregious error has occurred, since currently the caller
		// won't do sufficient cleanup that would correct such a situation.
	    }

	    // We skip the stopfd (which we just looked at above), and process any other pending i/o.
	    // At the end of the loop, we'll check the stopfd just so we can issue a log message if
	    // input is pending on it, but we'll basically depend on the bronx_is_terminating() flag
	    // having been set to avoid an infinite loop here, rather than trying to read the stopfd
	    // to empty the input (its purpose was just to break us out of the poll() well before the
	    // timeout expired).
	    for (i = 1; i < npfds; i++)
	    {
		if (pfds[i].revents & POLLNVAL) {
		    // Badness has happened -- we should never get here.
		    bronx_logprintf (BRONX_LOGGING_ERROR,
			"{handle_events} internal error: file descriptor %d found in POLLNVAL state", pfds[i].fd);
		    pfds[i].events = 0;
		    // This situation is so potentially bad that we should do whatever we can to reset ourselves.
		    // FIX MINOR:  Should we issue BRONX_ABORT here instead?
		    status = BRONX_ERROR;
		}
		if ((pfds[i].events & POLLIN) && (pfds[i].revents & (POLLIN | POLLERR | POLLHUP)))
		{
		    fdesc[pfds[i].fd].last_active_time = now;
#ifdef INCLUDE_EXTRA_DEBUG_DETAIL
		    bronx_logprintf (BRONX_LOGGING_DEBUG, "{handle_events} input file descriptor %d state is%s%s%s",
			pfds[i].fd,
			pfds[i].revents & POLLIN   ? " POLLIN"   : "",
			pfds[i].revents & POLLERR  ? " POLLERR"  : "",
			pfds[i].revents & POLLHUP  ? " POLLHUP"  : "");
#endif
		    // Unlike the "write" branch below, we don't simultaneously test for POLLHUP here as well, because
		    // its presence does not necessarily indicate that there is no incoming data to be read on the socket.
		    if (pfds[i].revents & POLLERR)
		    {
			// The exact meaning of POLLERR is not clear from the documentation of poll(),
			// but we will treat it like an unrecoverable problem and discard any data that
			// might be available if POLLIN is set.  In contrast, POLLHUP and POLLIN are
			// not mutually exclusive, so if we get POLLHUP here, we might still be able
			// to read data from the socket.  Note also that in practice we have seen the
			// socket sometimes be in state "POLLIN POLLERR POLLHUP", and that is likely
			// to be from a periodic TCP port check which is not intending to send any
			// useful data.
			bronx_logprintf (BRONX_LOGGING_WARNING,
			    "{handle_events} file descriptor %d at pfds[%d] found in state%s%s%s",
			    pfds[i].fd, i,
			    pfds[i].revents & POLLIN   ? " POLLIN"   : "",
			    pfds[i].revents & POLLERR  ? " POLLERR"  : "",
			    pfds[i].revents & POLLHUP  ? " POLLHUP"  : "");
			// Clean up anything related to the handling of this file descriptor.
			hand = find_rhand(pfds[i].fd);
			if (hand >= 0)
			{
			    if (0)
			    {
				// This message is useful in showing whether the .data field is 0 or some other value.
				// %p is not handled by the routines underlying bronx_logprintf(),
				// so we have to do that conversion ourselves.
				char ptrbuf[30];
				snprintf(ptrbuf, sizeof(ptrbuf), "%p", rhand[hand].data);
				bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} rhand[%d].data = %s", hand, ptrbuf);
			    }

			    handler = rhand[hand].handler;
			    data    = rhand[hand].data;
			    rhand[hand].handler = NULL;
			    rhand[hand].data    = NULL;
			    unregister_read_handler(hand);
			    // The handler knows how to interpret the .data element and do the right kind of cleanup.
			    // Call the handler just to do .data cleanup, if any.
			    if (handler (pfds[i].fd, data, 1) != BRONX_OK)
			    {
				// Unexpected condition, but nothing special to do here.
			    }
			}
			// invalidate pfds[i] so we will close pfds[i].fd below
			pfds[i].events = 0;
		    }
		    // this test is not redundant because it distinguishes between POLLHUP-only and POLLHUP-with-POLLIN
		    else if (pfds[i].revents & POLLIN)
		    {
			pfds[i].events &= ~POLLIN;
			hand = find_rhand(pfds[i].fd);
			if (hand >= 0)
			{
			    handler = rhand[hand].handler;
			    data    = rhand[hand].data;
			    rhand[hand].handler = NULL;
			    rhand[hand].data    = NULL;
			    unregister_read_handler(hand);
			    if (handler (pfds[i].fd, data, 0) != BRONX_OK)
			    {
				// invalidate pfds[i] so we will close pfds[i].fd below
				pfds[i].events = 0;

				// Note:  We don't clean up the .data element here as well, or below
				// when we call bronx_safe_close() on the file descriptor, by calling
				// encrypt_cleanup() or somesuch.  That's because our convention is
				// that if the handler saw a problem that was serious enough to not
				// return BRONX_OK, then the handler itself knows how to interpret the
				// .data element and it is responsible for such cleanup.  Distributing
				// the cleanup that way is not necessarily a great design, because it
				// makes for more-complex maintenance work, but it's what the current
				// code is doing, in case you start wondering about that aspect of
				// destroying a particular file descriptor.
			    }
			}
		    }
		}
		if ((pfds[i].events & POLLOUT) && (pfds[i].revents & (POLLOUT | POLLERR | POLLHUP)))
		{
		    fdesc[pfds[i].fd].last_active_time = now;
#ifdef INCLUDE_EXTRA_DEBUG_DETAIL
		    bronx_logprintf (BRONX_LOGGING_DEBUG, "{handle_events} output file descriptor %d state is%s%s%s",
			pfds[i].fd,
			pfds[i].revents & POLLOUT  ? " POLLOUT"  : "",
			pfds[i].revents & POLLERR  ? " POLLERR"  : "",
			pfds[i].revents & POLLHUP  ? " POLLHUP"  : "");
#endif
		    if (pfds[i].revents & (POLLERR | POLLHUP))
		    {
			// The exact meaning of POLLERR is not clear from the documentation of poll(),
			// but in this context we will treat it like an unrecoverable problem and abort
			// this socket connection.  Also, POLLHUP and POLLOUT are mutually exclusive,
			// so if we get POLLHUP here, there is no sense in trying to carry out any write
			// operations on the socket.
			bronx_logprintf (BRONX_LOGGING_WARNING,
			    "{handle_events} file descriptor %d at pfds[%d] found in state%s%s%s",
			    pfds[i].fd, i,
			    pfds[i].revents & POLLOUT  ? " POLLOUT"  : "",
			    pfds[i].revents & POLLERR  ? " POLLERR"  : "",
			    pfds[i].revents & POLLHUP  ? " POLLHUP"  : "");
			// Clean up anything related to the handling of this file descriptor.
			hand = find_whand(pfds[i].fd);
			if (hand >= 0)
			{
			    if (0)
			    {
				// This message is useful in showing whether the .data field is 0 or some other value.
				// %p is not handled by the routines underlying bronx_logprintf(),
				// so we have to do that conversion ourselves.
				char ptrbuf[30];
				snprintf(ptrbuf, sizeof(ptrbuf), "%p", whand[hand].data);
				bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} whand[%d].data = %s", hand, ptrbuf);
			    }

			    handler = whand[hand].handler;
			    data    = whand[hand].data;
			    whand[hand].handler = NULL;
			    whand[hand].data    = NULL;
			    unregister_write_handler(hand);
			    // The handler knows how to interpret the .data element and do the right kind of cleanup.
			    // Call the handler just to do .data cleanup, if any.
			    if (handler (pfds[i].fd, data, 1) != BRONX_OK)
			    {
				// Unexpected condition, but nothing special to do here.
			    }
			}
			// invalidate pfds[i] so we will close pfds[i].fd below
			pfds[i].events = 0;
		    }
		    // this test should be redundant at this point, but it won't hurt
		    else if (pfds[i].revents & POLLOUT)
		    {
			pfds[i].events &= ~POLLOUT;
			hand = find_whand(pfds[i].fd);
			if (hand >= 0)
			{
			    handler = whand[hand].handler;
			    data    = whand[hand].data;
			    whand[hand].handler = NULL;
			    whand[hand].data    = NULL;
			    unregister_write_handler(hand);
			    if (handler (pfds[i].fd, data, 0) != BRONX_OK)
			    {
				// invalidate pfds[i] so we will close pfds[i].fd below
				pfds[i].events = 0;

				// Note:  We don't clean up the .data element here as well, or below
				// when we call bronx_safe_close() on the file descriptor, by calling
				// encrypt_cleanup() or somesuch.  That's because our convention is
				// that if the handler saw a problem that was serious enough to not
				// return BRONX_OK, then the handler itself knows how to interpret the
				// .data element and it is responsible for such cleanup.  Distributing
				// the cleanup that way is not necessarily a great design, because it
				// makes for more-complex maintenance work, but it's what the current
				// code is doing, in case you start wondering about that aspect of
				// destroying a particular file descriptor.
			    }
			}
		    }
		}

		// When checking for idle connections, we have to skip the stopfd at pfds[0] and the listener socket at pfds[1].
		// We don't bother to log-as-inactive any file descriptors that would have been so logged if we had not already
		// marked them for closure in the code above.
		if (i > 1 && now > fdesc[pfds[i].fd].last_active_time + _configuration->idle_connection_timeout && pfds[i].events != 0)
		{
		    bronx_logprintf (BRONX_LOGGING_DEBUG,
			"{handle_events} file descriptor %d was inactive for " TIME_T_FORMAT " seconds, is being closed",
			pfds[i].fd, (TIME_T_CAST_TYPE) (now - fdesc[pfds[i].fd].last_active_time));

		    // Aside from marking the socket as should-be-closed-from-the-server-side, we need to be
		    // careful in how any associated data is cleaned up, to avoid further resource leaks.
		    // The code below does that for other types of problems, and can be taken as a guideline.
		    if (pfds[i].events & POLLIN)
		    {
			hand = find_rhand(pfds[i].fd);
			if (hand >= 0)
			{
			    if (0)
			    {
				// This message is useful in showing whether the .data field is 0 or some other value.
				// %p is not handled by the routines underlying bronx_logprintf(),
				// so we have to do that conversion ourselves.
				char ptrbuf[30];
				snprintf(ptrbuf, sizeof(ptrbuf), "%p", rhand[hand].data);
				bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} rhand[%d].data = %s", hand, ptrbuf);
			    }

			    handler = rhand[hand].handler;
			    data    = rhand[hand].data;
			    rhand[hand].handler = NULL;
			    rhand[hand].data    = NULL;
			    unregister_read_handler(hand);
			    // The handler knows how to interpret the .data element and do the right kind of cleanup.
			    // Call the handler just to do .data cleanup, if any.
			    if (handler (pfds[i].fd, data, 1) != BRONX_OK)
			    {
				// Unexpected condition, but nothing special to do here.
			    }
			}
		    }
		    else if (pfds[i].events & POLLOUT)
		    {
			hand = find_whand(pfds[i].fd);
			if (hand >= 0)
			{
			    if (0)
			    {
				// This message is useful in showing whether the .data field is 0 or some other value.
				// %p is not handled by the routines underlying bronx_logprintf(),
				// so we have to do that conversion ourselves.
				char ptrbuf[30];
				snprintf(ptrbuf, sizeof(ptrbuf), "%p", whand[hand].data);
				bronx_logprintf (BRONX_LOGGING_DEVELOP, "{handle_events} whand[%d].data = %s", hand, ptrbuf);
			    }

			    handler = whand[hand].handler;
			    data    = whand[hand].data;
			    whand[hand].handler = NULL;
			    whand[hand].data    = NULL;
			    unregister_write_handler(hand);
			    // The handler knows how to interpret the .data element and do the right kind of cleanup.
			    // Call the handler just to do .data cleanup, if any.
			    if (handler (pfds[i].fd, data, 1) != BRONX_OK)
			    {
				// Unexpected condition, but nothing special to do here.
			    }
			}
		    }
		    // invalidate pfds[i] so we will close pfds[i].fd below
		    pfds[i].events = 0;
		}
	    }
	    check_for_dead_connections = 1;
	}

	if (check_for_dead_connections)
	{
	    // Collapse out now-useless file descriptors.  Fill in dead positions in the list,
	    // taking elements from the end of the list and shrinking the list accordingly.
	    // pfds[0] is for the stopfd, which we never close here in the listener, so we skip that index here.
	    int printed_listener_close_message = 0;
	    for (i = 1; i < npfds; i++)
	    {
		while (i < npfds && pfds[i].events == 0)
		{
		    // pfds[1] should be the primary listener socket, inasmuch as it is the first
		    // file descriptor registered with the poller after the stopfd is so registered.
		    // We don't know what would cause this file descriptor to be seen as in some
		    // state where it ought to be closed, though we have seen evidence at customer
		    // sites that such a situation can arise.
		    if (i == 1)
		    {
			// pfds[1] is presumed to be the listening socket, which in general we expect to
			// stay open forever.  (We could check explicitly against the listener socket file
			// descriptor if we stored it somewhere public, but currently we don't do so, so we
			// just depend on the pfds[] index comparison that got us here.)  When it goes out,
			// we need to flag that fact so the caller can invoke an appropriate restart action.
			// Let's also emit a log message to provide some critical insight as to why Bronx
			// stopped accepting new connections (or at least, that's what would happen if we
			// get this wrong, since the caller is equipped to recover if we get this right).
			//
			// Of course, after this entry is closed, some other entry will be copied to this same
			// position, so this position will no longer be occupied by the listener socket if
			// another socket is opened for that purpose, unless all non-stopfd sockets are closed
			// before the new listener socket is opened and registered for polling.  That's why
			// our caller, the manage_connections() routine, closes all sockets when it sees this
			// type of error, in order to reset the processing to a clean state before another
			// listener socket is opened.  In any case, we use a flag to suppress extra meaningless
			// copies of the log message from appearing.
			if (! printed_listener_close_message)
			{
			    // FIX LATER:  We'd like to know why the listening socket might get closed, so in
			    // some future version we should try to capture any information that might give us
			    // a clue.
			    //
			    // We have no intention of closing the listening socket while Bronx is in normal
			    // operation, unless I'm forgetting something, so ordinarily we would classify
			    // this situation as an error instead of a warning.  However, we know that the
			    // calling code is supposed to recover from this unexpected failure and continue
			    // operating, so we downgrade the severity of this condition to a warning.
			    bronx_logprintf (BRONX_LOGGING_WARNING,
				"{handle_events} unexpected action: pfds[1] file descriptor %d (presumed listening socket) is being closed",
				pfds[i].fd);
			    printed_listener_close_message = 1;
			}
			status = BRONX_ERROR;
		    }
		    if (! (pfds[i].revents & POLLNVAL))
		    {
			bronx_safe_close(pfds[i].fd);
		    }
		    // Copy the last element in the list to this dead cell, to shrink the list and keep the
		    // poll()ing efficient.  If this is itself the last one in the list, this just copies it
		    // onto itself, but that won't matter, as we are effectively deleting it anyway.
		    //
		    // FIX MINOR:  Note that this method of shrinking the list has a potentially undesireable
		    // side effect.  The file descriptor at the end of the queue, which is likely one of the
		    // youngest, gets to jump the queue and subsequently be treated as one of the oldest
		    // (in terms of which ready file descriptors get serviced first, it will get attention
		    // early because of its now-earlier position in the queue and the fact that we process
		    // ready file descriptors in order by their pfds[] index).  This means that many file
		    // descriptors which are older than the one we are copying here will only be serviced
		    // after this copied file descriptor, which might mean that their respective clients time
		    // out before completing their work.  That's not a good thing.  We ought to find some
		    // other way to manage the list of open file descriptors, or at least to walk the list
		    // of ready file descriptors, one that processes them roughly in connection-time order.
		    //
		    // The thing that ought to save us is that in practice, the number of file descriptors
		    // that are concurrently open ought to be quite small, so the effect above should be
		    // quite limited in scope.
		    npfds--;
		    pfds[i] = pfds[npfds];
		}
	    }
	}
    }

    return (status);
}

/*
 * Writes service/host check results to the
 * Nagios service check results queue
 */

static int
post_check_result(char *host_name, char *svc_description, int return_code, char *plugin_output, time_t check_time)
{
    // January 24, 2017:
    //
    // Why we originally split off host-check results and sent them via a different channel is something
    // of a mystery.  That code structure was put into place in the old OS-archive checkin revision
    // 10611 (2008-02-05 16:21:14), with a simple commit comment of "incorrect handling of passive host
    // checks".  Previous to that, both host checks and service checks were sent via the same call
    // to submit_check_result_to_nagios() that we use for service checks in the Nagios 3.5.1 version
    // of Bronx (and now again for both host checks and service checks in the Nagios 4.2.4 version
    // of Bronx).  Most likely, the problem being "corrected" (incorrectly) was that here we check
    // svc_description for an empty string, while there we checked it for a NULL pointer (that has now
    // been fixed) when in fact it was always a non-NULL pointer even for a host check.  Alternatively,
    // that commit was made while Bronx was still operating in the Nagios 2.X context; maybe that has
    // something to do with it.  Or perhaps the problem was that submit_check_result_to_nagios() was
    // written to be too specific to service checks, and needed a little generalization (beyond fixing
    // the determination of whether it was a host check or a service check) to handle host checks.
    // Finally, perhaps there was observed to be some issue of order of processing, where you want host
    // checks to be processed before all the related service checks, or vice versa, and sending via
    // different channels helps that cause.  However, it seems to me that sending via parallel channels
    // probably invites uncontrolled race conditions.
    //
    // I have fixed and generalized submit_check_result_to_nagios() to be able to handle host checks.
    // With that change in place, we are now reverting back to a single channel for sending all
    // host/service-check data.
    //
#ifdef NAGIOS_4_2_4_OR_LATER
#ifndef SUPPRESS_DEBUG_DETAIL
    bronx_logprintf(BRONX_LOGGING_PASSIVE_CHECKS,
	"{post_check_result} Writing %s check result into nagios {check_result_list}.",
	svc_description[0] ? "service" : "host");
#endif
    return(submit_check_result_to_nagios(host_name, svc_description, return_code, plugin_output, check_time));
#else
    if (!strcmp(svc_description, ""))
    {
	char cmd[4096];

	bronx_log("{post_check_result} Writing host check result into nagios {external_command_buffer}.",
	    BRONX_LOGGING_PASSIVE_CHECKS);
	sprintf(cmd, "[%lu] PROCESS_HOST_CHECK_RESULT;%s;%d;%s\n",
	    (unsigned long)check_time, host_name, return_code, plugin_output);
	return(submit_command_to_nagios(cmd));
    }
    else
    {
#ifndef SUPPRESS_DEBUG_DETAIL
	bronx_log("{post_check_result} Writing service check result into nagios {check_result_list}.",
	    BRONX_LOGGING_PASSIVE_CHECKS);
#endif
	return(submit_check_result_to_nagios(host_name, svc_description, return_code, plugin_output, check_time));
    }
#endif
}

/*
 * get_packet_v3_remainder()
 * Gets the rest of a version 3 data packet from the socket.
 * crypt_instance *CI - Pointer to the current crypt instance.
 * int sock - Socket on which to receive the packet.
 * arbitrary_data_packet *full_data_packet - buffer containing the partial packet already read
 * size_t packet_remainder_offset - where to begin stuffing the rest of the packet
 * time_t *time_to_send - The packet time stamp is stored here.
 * int *submit_to_nagios - An output parameter that will indicate
 *                         whether the result is valid and can be submitted
 *                         to nagios.
 */
static int
get_packet_v3_remainder(struct crypt_instance *CI, int sock, arbitrary_data_packet *full_data_packet,
    size_t packet_remainder_offset, time_t *time_to_send, int *submit_to_nagios)
{
    bronx_log("{get_packet_v3_remainder} entered", BRONX_LOGGING_DEBUG);
    u_int32_t packet_crc32;
    u_int32_t calculated_crc32;
    time_t packet_time;
    time_t current_time;
    /* Buffer to store peer IP address */
    char address[MAX_IPADDR_LENGTH];
    unsigned long packet_age = 0L;
    size_t bytes_to_recv;
    int rc;
    /* We will consume the remainder of the incoming packet. */
    int peek = 0;

    /* Assume that that result is valid */
    *submit_to_nagios = 1;

    if (sizeof(full_data_packet->v3_data) > packet_remainder_offset)
    {
	/* Read the rest of the packet from the client. */
	bytes_to_recv = sizeof(full_data_packet->v3_data) - packet_remainder_offset;
	// FIX LATER:  recvall() is a bad construction because it internally waits up to the supplied timeout
	// interval for the full packet to arrive, thereby potentially starving other sockets of the chance to have
	// their data processed.  We ought to replace it with an architecture that will accept all the currently
	// available data up to some limit and buffer it, and only process a packet once we know we have enough
	// incoming data ready to go.  But that will take a significant high-level reorganization of the code, so
	// for the moment we're going to live with this limitation.  Given the short (720-byte or so) standard NSCA
	// v3 packet, we generally don't expect long waits here, but with network i/o there's never any guarantee.
	rc = recvall(sock, ((char *) &(full_data_packet->v3_data)) + packet_remainder_offset,
	    &bytes_to_recv, NSCA_DEFAULT_SOCKET_TIMEOUT, peek);
	if (rc <= 0)
	{
	    // recv() error or client disconnect
	    if (log_getlevel() >= BRONX_LOGGING_DEBUG)
	    {
		int port;
		get_full_peer_address(sock, address, sizeof(address), &port);
		bronx_logprintf(BRONX_LOGGING_DEBUG, "{handle_connection_read} Client %s port %d (file descriptor %d) has disconnected.",
		    address, port, sock);
	    }
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return BRONX_ERROR;
	}

	if (bytes_to_recv != sizeof(full_data_packet->v3_data) - packet_remainder_offset)
	{
	    // we couldn't read the correct amount of data, so bail out
	    if (log_getlevel() >= BRONX_LOGGING_WARNING)
	    {
		get_peer_address(sock, address, sizeof(address));
		bronx_logprintf(BRONX_LOGGING_WARNING,
		    "{handle_connection_read} Data sent from client %s was too short "
		    "("SIZE_T_FORMAT" of "SIZE_T_FORMAT" expected remaining bytes); aborting connection.",
		    address, bytes_to_recv, sizeof(full_data_packet->v3_data) - packet_remainder_offset);
	    }
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return BRONX_ERROR;
	}

	/* decrypt the remainder of the packet (just the part we just read) */
	decrypt_buffer(((char *) full_data_packet) + packet_remainder_offset, (int) bytes_to_recv,
	    _configuration->listener_password, _configuration->listener_encryption_method, CI, 0);
    }

    /* check the crc 32 value */
    packet_crc32=ntohl(full_data_packet->v3_data.crc32_value);
    full_data_packet->v3_data.crc32_value=0L;
    calculated_crc32=calculate_crc32((char *) &(full_data_packet->v3_data), sizeof(full_data_packet->v3_data));
    if (packet_crc32!=calculated_crc32)
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{handle_connection_read} Dropping packet from client %s with invalid V3 CRC32 (got %8X, calculated %8X) - "
		"possibly due to client using wrong _configuration->password or crypto algorithm?",
		address, packet_crc32, calculated_crc32);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    /* Ensure that the host name is NUL-terminated, so we can use it as an ordinary string. */
    full_data_packet->v3_data.host_name[sizeof(full_data_packet->v3_data.host_name) - 1]='\0';

    /* check the timestamp in the packet */
    // If the packet time is in the future, the packet "age" will be negative.
    // So we have make related calculations carefully when using unsigned variables.
    packet_time=(time_t)ntohl(full_data_packet->v3_data.timestamp);
    time(&current_time);
    if (packet_time > current_time + _configuration->listener_max_packet_imminence)
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{handle_connection_read} Dropping packet from client %s for host '%s' with timestamp too far in the future.",
		address, full_data_packet->v3_data.host_name);
	}
	// We will drop this one packet, but keep the socket open and allow the client to send more packets.
	*submit_to_nagios = 0;
	int outcome = register_read_handler(sock, handle_connection_read, (void *) CI);
	if (outcome != BRONX_OK)
	{
	    bronx_log("{handle_connection_read} Failed to register read handler", BRONX_LOGGING_ERROR);
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return outcome;
	}
	return BRONX_OK;
    }
    else if (current_time >= packet_time)
    {
	packet_age = (unsigned long)(current_time - packet_time);
	if (_configuration->listener_max_packet_age > 0 && (packet_age > _configuration->listener_max_packet_age))
	{
	    if (log_getlevel() >= BRONX_LOGGING_WARNING)
	    {
		get_peer_address(sock, address, sizeof(address));
		bronx_logprintf(BRONX_LOGGING_WARNING,
		    "{handle_connection_read} Dropping packet from client %s for host '%s' with stale timestamp - "
		    "packet was %lu seconds old.",
		    address, full_data_packet->v3_data.host_name, packet_age);
	    }
	    // We will drop this one packet, but keep the socket open and allow the client to send more packets,
	    // some of which may be younger and still valid.
	    *submit_to_nagios = 0;
	    int outcome = register_read_handler(sock, handle_connection_read, (void *) CI);
	    if (outcome != BRONX_OK)
	    {
		bronx_log("{handle_connection_read} Failed to register read handler", BRONX_LOGGING_ERROR);
		encrypt_cleanup(_configuration->listener_encryption_method, CI);
		return outcome;
	    }
	    return BRONX_OK;
	}
    }
    else
    {
	// We have a future timestamp, relative to time known to the nagios server,
	// but within our Bronx-configured imminence threshold.
	//
	// In this case, just below we will use the current_time instead, even if
	// _configuration->use_client_timestamp is non-zero, to prevent potential
	// problems with a future timestamp inside Nagios.  That might happen, for
	// instance, if Nagios somewhere tries to calculate the packet age as we do
	// above, and computes a negative number which it might interpret as a huge
	// positive number because the computation is done using unsigned variables.
    }
    /* Now that we are sure that we want this packet, which timestamp do we use? */
    if (_configuration->use_client_timestamp > 0 && packet_time <= current_time)
    {
	*time_to_send = packet_time;
    }
    else
    {
	*time_to_send = current_time;
    }

    // GET THE SERVICE CHECK INFORMATION

    /* plugin return code */
    full_data_packet->v3_data.return_code = ntohs(full_data_packet->v3_data.return_code);

    /* service description */
    full_data_packet->v3_data.svc_description[sizeof(full_data_packet->v3_data.svc_description) - 1]='\0';

    /* plugin output */
    full_data_packet->v3_data.plugin_output[sizeof(full_data_packet->v3_data.plugin_output) - 1]='\0';
    normalize_plugin_output(full_data_packet->v3_data.plugin_output, "B1");

    /* Everything went ok. */
    return BRONX_OK;
}

/*
 * get_packet_v101_remainder()
 * Gets the rest of a version 101 data packet from the socket.
 * crypt_instance *CI - Pointer to the current crypt instance.
 * int sock - Socket on which to receive the packet.
 * arbitrary_data_packet *full_data_packet - buffer containing the partial packet already read
 * size_t packet_remainder_offset - where to begin stuffing the rest of the packet
 * time_t *time_to_send - The packet time stamp is stored here.
 * int *submit_to_nagios - An output parameter that will indicate
 *                         whether the result is valid and can be submitted
 *                         to nagios.
 */
static int
get_packet_v101_remainder(struct crypt_instance *CI, int sock, arbitrary_data_packet *full_data_packet,
    size_t packet_remainder_offset, time_t *time_to_send, int *submit_to_nagios)
{
    bronx_log("{get_packet_v101_remainder} entered", BRONX_LOGGING_DEBUG);

    u_int32_t packet_crc32;
    u_int32_t calculated_crc32;
    /* Buffer to store peer IP address */
    char address[MAX_IPADDR_LENGTH];
    time_t packet_time;
    time_t current_time;
    unsigned long packet_age = 0L;
    size_t bytes_to_recv;
    size_t full_packet_size;
    uint16_t real_host_name_size;
    uint16_t real_svc_description_size;
    uint16_t real_plugin_output_size;
    uint16_t real_alignment_padding_size;
    int rc = 0;
    /* We will consume the remainder of the incoming packet. */
    int peek = 0;

    /* Assume that that result is valid */
    *submit_to_nagios = 1;

    if (sizeof(full_data_packet->v101_data.variable_packet.fixed_data) > packet_remainder_offset)
    {
	/*
	** Read the rest of the fixed-length part of the packet from the client.
	** (In fact, this has probably already been done before we got here.)
	*/
	bytes_to_recv = sizeof(full_data_packet->v101_data.variable_packet.fixed_data) - packet_remainder_offset;
	// FIX LATER:  recvall() is a bad construction because it internally waits up to the supplied timeout
	// interval for the full packet to arrive, thereby potentially starving other sockets of the chance to have
	// their data processed.  We ought to replace it with an architecture that will accept all the currently
	// available data up to some limit and buffer it, and only process a packet once we know we have enough
	// incoming data ready to go.  But that will take a significant high-level reorganization of the code, so
	// for the moment we're going to live with this limitation.
	rc = recvall(sock, ((char *) &(full_data_packet->v101_data)) + packet_remainder_offset,
	    &bytes_to_recv, NSCA_DEFAULT_SOCKET_TIMEOUT, peek);
	if (rc <= 0)
	{
	    // recv() error or client disconnect
	    if (log_getlevel() >= BRONX_LOGGING_DEBUG)
	    {
		int port;
		get_full_peer_address(sock, address, sizeof(address), &port);
		bronx_logprintf(BRONX_LOGGING_DEBUG, "{handle_connection_read} Client %s port %d (file descriptor %d) has disconnected.",
		    address, port, sock);
	    }
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return BRONX_ERROR;
	}

	if (bytes_to_recv != sizeof(full_data_packet->v101_data.variable_packet.fixed_data) - packet_remainder_offset)
	{
	    // we couldn't read the correct amount of data, so bail out
	    if (log_getlevel() >= BRONX_LOGGING_WARNING)
	    {
		get_peer_address(sock, address, sizeof(address));
		bronx_logprintf(BRONX_LOGGING_WARNING,
		    "{handle_connection_read} Data sent from client %s was too short "
		    "("SIZE_T_FORMAT" of "SIZE_T_FORMAT" expected additional bytes); aborting connection.",
		    address, bytes_to_recv,
		    sizeof(full_data_packet->v101_data.variable_packet.fixed_data) - packet_remainder_offset);
	    }
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return BRONX_ERROR;
	}

	/* decrypt the part of the packet we just read, so we can access its fields */
	decrypt_buffer(((char *) full_data_packet) + packet_remainder_offset, (int) bytes_to_recv,
	    _configuration->listener_password, _configuration->listener_encryption_method, CI, 0);

	// Note the fact that we've now pulled in additional packet bytes.
	packet_remainder_offset += bytes_to_recv;
    }

    // Now we have at least all the fixed-length data.  So we do what we must to pull in all the
    // variable-length data (or just the rest of it, if we came in with some of it already read).

    real_host_name_size         = ntohs(full_data_packet->v101_data.wide_host_name_size);
    real_svc_description_size   = ntohs(full_data_packet->v101_data.wide_svc_description_size);
    real_plugin_output_size     = ntohs(full_data_packet->v101_data.wide_plugin_output_size);
    real_alignment_padding_size = ntohs(full_data_packet->v101_data.wide_alignment_padding_size);

    // First check that all the variable-field sizes are reasonable.  This will ensure that we don't
    // overflow our buffer when we read the remainder of the packet.  In theory, we shouldn't even
    // trust these values enough to check their content without first verifying some kind of checksum
    // (which we haven't included in this packet format to cover just the fixed-size fields, which are
    // all we've necessarily read up to this point).  But we will verify the full-packet checksum later
    // on, so we have at least a high likelihood of being correct in our validation of the packet.
    if  (
	real_host_name_size         > MAX_HOSTNAME_LENGTH               ||
	real_svc_description_size   > MAX_DESCRIPTION_LENGTH            ||
	real_plugin_output_size     > MAX_NSCA_PLUGINOUTPUT_LENGTH_V101 ||
	real_alignment_padding_size > MAX_ALIGNMENT_PADDING_LENGTH_V101 ||
	// Verify the specific size of the alignment padding, to make sure it's doing its job.
	// (The compiler ought to be smart enough to convert "%4" into the much faster "&3",
	// and in testing gcc does appear to recognize that optimization.)
	(real_host_name_size + real_svc_description_size + real_plugin_output_size + real_alignment_padding_size) % 4
	)
    {
	// the size fields had unreasonable values, so bail out
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{handle_connection_read} Data sent from client %s had unreasonable field sizes "
		"(%hu, %hu, %hu, %hu); aborting connection.",
		address,
		real_host_name_size, real_svc_description_size, real_plugin_output_size, real_alignment_padding_size);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    full_packet_size = sizeof(full_data_packet->v101_data.variable_packet.fixed_data) +
	real_host_name_size + real_svc_description_size + real_plugin_output_size + real_alignment_padding_size;
    if (full_packet_size > packet_remainder_offset)
    {
	/* Read the rest of the variable-length part of the packet from the client. */
	bytes_to_recv = full_packet_size - packet_remainder_offset;
	// FIX LATER:  recvall() is a bad construction because it internally waits up to the supplied timeout
	// interval for the full packet to arrive, thereby potentially starving other sockets of the chance to have
	// their data processed.  We ought to replace it with an architecture that will accept all the currently
	// available data up to some limit and buffer it, and only process a packet once we know we have enough
	// incoming data ready to go.  But that will take a significant high-level reorganization of the code, so
	// for the moment we're going to live with this limitation.
	rc = recvall(sock, ((char *) &(full_data_packet->v101_data)) + packet_remainder_offset,
	    &bytes_to_recv, NSCA_DEFAULT_SOCKET_TIMEOUT, peek);
	if (rc <= 0)
	{
	    // recv() error or client disconnect
	    if (log_getlevel() >= BRONX_LOGGING_DEBUG)
	    {
		int port;
		get_full_peer_address(sock, address, sizeof(address), &port);
		bronx_logprintf(BRONX_LOGGING_DEBUG, "{handle_connection_read} Client %s port %d (file descriptor %d) has disconnected.",
		    address, port, sock);
	    }
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return BRONX_ERROR;
	}

	if (bytes_to_recv != full_packet_size - packet_remainder_offset)
	{
	    // we couldn't read the correct amount of data, so bail out
	    if (log_getlevel() >= BRONX_LOGGING_WARNING)
	    {
		get_peer_address(sock, address, sizeof(address));
		bronx_logprintf(BRONX_LOGGING_WARNING,
		    "{handle_connection_read} Data sent from client %s was too short "
		    "("SIZE_T_FORMAT" of "SIZE_T_FORMAT" expected remaining bytes); aborting connection.",
		    address, bytes_to_recv, full_packet_size - packet_remainder_offset);
	    }
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return BRONX_ERROR;
	}

	/* decrypt the remainder of the packet (just the part we just read) */
	decrypt_buffer(((char *) full_data_packet) + packet_remainder_offset, (int) bytes_to_recv,
	    _configuration->listener_password, _configuration->listener_encryption_method, CI, 0);
    }

    /* check the crc 32 value */
    packet_crc32=ntohl(full_data_packet->v101_data.wide_crc32_value);
    full_data_packet->v101_data.wide_crc32_value=0L;
    calculated_crc32=calculate_crc32((char *) &(full_data_packet->v101_data), full_packet_size);
    if (packet_crc32!=calculated_crc32)
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{handle_connection_read} Dropping packet from client %s with invalid V101 CRC32 (got %8X, calculated %8X) - "
		"possibly due to client using wrong _configuration->password or crypto algorithm?",
		address, packet_crc32, calculated_crc32);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    full_data_packet->v101_data.wide_host_name         = ((char *) &full_data_packet->v101_data) +
	sizeof(full_data_packet->v101_data.variable_packet.fixed_data);
    full_data_packet->v101_data.wide_svc_description   = full_data_packet->v101_data.wide_host_name +
	real_host_name_size;
    full_data_packet->v101_data.wide_plugin_output     = full_data_packet->v101_data.wide_svc_description +
	real_svc_description_size;
    full_data_packet->v101_data.wide_alignment_padding = full_data_packet->v101_data.wide_plugin_output +
	real_plugin_output_size;

    /* Ensure that the host name is NUL-terminated, so we can use it as an ordinary string. */
    full_data_packet->v101_data.wide_host_name[real_host_name_size - 1]='\0';

    /* check the timestamp in the packet */
    // If the packet time is in the future, the packet "age" will be negative.
    // So we have make related calculations carefully when using unsigned variables.
    packet_time=(time_t)ntohl(full_data_packet->v101_data.wide_timestamp);
    time(&current_time);
    if (packet_time > current_time + _configuration->listener_max_packet_imminence)
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{handle_connection_read} Dropping packet from client %s for host '%s' with timestamp too far in the future.",
		address, full_data_packet->v101_data.wide_host_name);
	}
	// We will drop this one packet, but keep the socket open and allow the client to send more packets.
	*submit_to_nagios = 0;
	int outcome = register_read_handler(sock, handle_connection_read, (void *) CI);
	if (outcome != BRONX_OK)
	{
	    bronx_log("{handle_connection_read} Failed to register read handler", BRONX_LOGGING_ERROR);
	    encrypt_cleanup(_configuration->listener_encryption_method, CI);
	    return outcome;
	}
	return BRONX_OK;
    }
    else if (current_time >= packet_time)
    {
	packet_age = (unsigned long)(current_time - packet_time);
	if (_configuration->listener_max_packet_age > 0 && (packet_age > _configuration->listener_max_packet_age))
	{
	    if (log_getlevel() >= BRONX_LOGGING_WARNING)
	    {
		get_peer_address(sock, address, sizeof(address));
		bronx_logprintf(BRONX_LOGGING_WARNING,
		    "{handle_connection_read} Dropping packet from client %s for host '%s' with stale timestamp - "
		    "packet was %lu seconds old.",
		    address, full_data_packet->v101_data.wide_host_name, packet_age);
	    }
	    // We will drop this one packet, but keep the socket open and allow the client to send more packets,
	    // some of which may be younger and still valid.
	    *submit_to_nagios = 0;
	    int outcome = register_read_handler(sock, handle_connection_read, (void *) CI);
	    if (outcome != BRONX_OK)
	    {
		bronx_log("{handle_connection_read} Failed to register read handler", BRONX_LOGGING_ERROR);
		encrypt_cleanup(_configuration->listener_encryption_method, CI);
		return outcome;
	    }
	    return BRONX_OK;
	}
    }
    else
    {
	// We have a future timestamp, relative to time known to the nagios server,
	// but within our Bronx-configured imminence threshold.
	//
	// In this case, just below we will use the current_time instead, even if
	// _configuration->use_client_timestamp is non-zero, to prevent potential
	// problems with a future timestamp inside Nagios.  That might happen, for
	// instance, if Nagios somewhere tries to calculate the packet age as we do
	// above, and computes a negative number which it might interpret as a huge
	// positive number because the computation is done using unsigned variables.
    }
    /* Now that we are sure that we want this packet, which timestamp do we use? */
    if (_configuration->use_client_timestamp > 0 && packet_time <= current_time)
    {
	*time_to_send = packet_time;
    }
    else
    {
	*time_to_send = current_time;
    }

    // GET THE SERVICE CHECK INFORMATION

    /* plugin return code */
    full_data_packet->v101_data.wide_return_code = ntohs(full_data_packet->v101_data.wide_return_code);

    /* service description */
    full_data_packet->v101_data.wide_svc_description[real_svc_description_size - 1]='\0';

    /* plugin output */
    full_data_packet->v101_data.wide_plugin_output[real_plugin_output_size - 1]='\0';
    normalize_plugin_output(full_data_packet->v101_data.wide_plugin_output, "B1");

    /* Everything went ok. */
    return BRONX_OK;
}

/*
 * handle reading from a client connection
 */
static int
handle_connection_read(int sock, void *data, int cleanup_only)
{
    bronx_log("{handle_connection_read} entered", BRONX_LOGGING_DEBUG);

    struct crypt_instance *CI;
    int rc;
    char address[MAX_IPADDR_LENGTH];
    arbitrary_data_packet full_data_packet;
    int16_t real_packet_version;
    char *host_name;
    char *svc_description;
    int16_t return_code = (int16_t)0;
    char *plugin_output;
    time_t time_to_send = (time_t)0;
    size_t bytes_to_recv;
    int submit_to_nagios = 0;

    // "peek" is the flag to recv(), called from inside recvall().
    // We want to be able to receive version 3 as well as version 101 of
    // data packet.  So, we need to know the version of the incoming packet
    // in order to decide how to process the rest of the packet, and in
    // particular to find out how many bytes need to be received to completely
    // get the packet -- a version 101 packet may be either smaller or larger
    // than a version 3 packet.  An earlier version of this code tried to
    // peek (read without consuming) just the packet version, but there's
    // really not much point in that.  We may as well read as much as we can
    // reasonably presume ought to be present, and then deal with that data.
    // That also simplifies our handling of decryption, since it would be
    // difficult to force the encryption state back to what it was before
    // we decrypt the first part of the packet, if we later wanted to decrypt
    // the entire packet.
    int peek = 0;

    CI = data;

    if (cleanup_only)
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    int port;
	    get_full_peer_address(sock, address, sizeof(address), &port);
	    bronx_logprintf(BRONX_LOGGING_WARNING, "{handle_connection_read} Client %s port %d (file descriptor %d) will be forcibly disconnected.",
		address, port, sock);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_OK;
    }

    // Read the maximum amount of data that must be present regardless of
    // which packet type is being sent, and put it into a buffer that can
    // hold any packet.
    bytes_to_recv = MIN_COMMON_PACKET_SIZE;
    rc = recvall(sock, (char*) &full_data_packet, &bytes_to_recv, NSCA_DEFAULT_SOCKET_TIMEOUT, peek);

    if (rc <= 0)
    {
	// recv() error or client disconnect
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    int port;
	    get_full_peer_address(sock, address, sizeof(address), &port);
	    bronx_logprintf(BRONX_LOGGING_WARNING, "{handle_connection_read} Client %s port %d (file descriptor %d) has disconnected.",
		address, port, sock);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    if (bytes_to_recv != MIN_COMMON_PACKET_SIZE)
    {
	// Did not recv() enough bytes.
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING,
			    "{handle_connection_read} Data sent from client %s was too short "
			    "("SIZE_T_FORMAT" of "SIZE_T_FORMAT" expected bytes); aborting connection.",
			    address, bytes_to_recv, MIN_COMMON_PACKET_SIZE);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    /* decrypt the presumably-partial packet */
    decrypt_buffer((char *) &full_data_packet, (int) MIN_COMMON_PACKET_SIZE,
		   _configuration->listener_password,
		   _configuration->listener_encryption_method, CI, 1);

    real_packet_version = ntohs(full_data_packet.arbitrary_packet_version);
    /* Make sure this is the right type of packet */
    if ((real_packet_version != NSCA_PACKET_VERSION_101) &&
	(real_packet_version != NSCA_PACKET_VERSION_3))
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    int port;
	    get_full_peer_address(sock, address, sizeof(address), &port);
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{handle_connection_read} Received invalid packet type/version %d from client %s port %d (file descriptor %d) - "
		"possibly due to client using wrong _configuration->password or crypto algorithm?",
		real_packet_version, address, port, sock);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    /*
     * Receive the rest of the appropriate version of the packet.
     */
    if (real_packet_version == NSCA_PACKET_VERSION_101)
    {
	rc = get_packet_v101_remainder(CI, sock, &full_data_packet, bytes_to_recv, &time_to_send, &submit_to_nagios);
	// If we didn't get a full packet, these values may be invalid, but in that case we won't use them.
	host_name       = full_data_packet.v101_data.wide_host_name;
	svc_description = full_data_packet.v101_data.wide_svc_description;
	return_code     = full_data_packet.v101_data.wide_return_code;
	plugin_output   = full_data_packet.v101_data.wide_plugin_output;
    }
    else
    {
	rc = get_packet_v3_remainder(CI, sock, &full_data_packet, bytes_to_recv, &time_to_send, &submit_to_nagios);
	// If we didn't get a full packet, these values may be invalid, but in that case we won't use them.
	host_name       = full_data_packet.v3_data.host_name;
	svc_description = full_data_packet.v3_data.svc_description;
	return_code     = full_data_packet.v3_data.return_code;
	plugin_output   = full_data_packet.v3_data.plugin_output;
    }
    if ((rc != BRONX_OK) || (submit_to_nagios != 1))
    {
	// Something went wrong while receiving the packet, or the received result is not valid.
	// Simply return, as we already logged the error.  The call to get_packet_v101_remainder()
	// or get_packet_v3_remainder() also already handled the cleanup of CI, so we intentionally
	// have no call to encrypt_cleanup() here.
	return rc;
    }

    // FIX MINOR:  Allowing arbitrary administrative commands on the listener port from
    // any self-certified client is a gaping security hole (although except for commands
    // to pause and restart Bronx itself, this facility ends up being controlled via the
    // listener_nagios_cmd_execution option in bronx.cfg, which is defaulted to deny
    // execution of Nagios commands via this channel).  We need to drop support for that
    // here.  (Also note that the current implementation of arbitrary-command execution
    // in admin_execute_nagios_command() in bronx_admin.c is not at all safe with respect
    // to race conditions against the Nagios main thread.)

    /* We got all the necessary fields from the packet. Now, process them. */
    if (!strcmp(host_name, "0.0.0.0") && !strcmp(svc_description, "bronx"))
    {
	if (log_getlevel() >= BRONX_LOGGING_COMMANDS)
	{
	    int port;
	    get_full_peer_address(sock, address, sizeof(address), &port);
	    bronx_logprintf(BRONX_LOGGING_COMMANDS,
		 "{handle_connection_read} Received BRONX EXTERNAL COMMAND from client %s port %d (file descriptor %d) -> '%s'",
		 address, port, sock, plugin_output);
	}
	admin_execute_command(_configuration, plugin_output);
    }
    else
    {
	// These messages occur so often that printing the extra detail might
	// slow things down enough to affect timing.  So we give ourselves
	// options to limit the amount of time spent processing these messages,
	// and the amount of output provided for them.  That could be useful
	// in diagnosing issues that we suspect might be due to race conditions.
	// We also check the logging level here (in addition to doing so within
	// the logging function) in some cases, to avoid time spent on setting up
	// data to pass to the logging calls if that data would just be dropped
	// on the floor.
	if (log_getlevel() >= BRONX_LOGGING_PASSIVE_CHECKS)
	{
	    if (!strcmp(svc_description, ""))
	    {
		if (debug_detail_level >= DEBUG_DETAIL_DEBUG)
		{
		    int port;
		    get_full_peer_address(sock, address, sizeof(address), &port);
		    bronx_logprintf(BRONX_LOGGING_PASSIVE_CHECKS,
			"{handle_connection_read} Received HOST CHECK RESULT from client %s port %d (file descriptor %d) -> "
			"Host Name='%s', Return Code='%d', Output='%s'",
			address, port, sock, host_name, return_code, plugin_output);
		}
		else if (debug_detail_level >= DEBUG_DETAIL_NOTICE)
		{
		    int port;
		    get_full_peer_address(sock, address, sizeof(address), &port);
		    bronx_logprintf(BRONX_LOGGING_PASSIVE_CHECKS,
			"{handle_connection_read} Received HOST CHECK RESULT from client %s port %d (file descriptor %d) -> "
			"Host Name='%s', Return Code='%d'",
			address, port, sock, host_name, return_code);
		}
		else if (debug_detail_level > DEBUG_DETAIL_NONE)
		{
		    bronx_logprintf(BRONX_LOGGING_PASSIVE_CHECKS,
			"{handle_connection_read} Received HOST CHECK RESULT");
		}
	    }
	    else
	    {
		if (debug_detail_level >= DEBUG_DETAIL_DEBUG)
		{
		    int port;
		    get_full_peer_address(sock, address, sizeof(address), &port);
		    bronx_logprintf(BRONX_LOGGING_PASSIVE_CHECKS,
			"{handle_connection_read} Received SERVICE CHECK RESULT from client %s port %d (file descriptor %d) -> "
			"Host Name='%s', Service Description='%s', Return Code='%d', Output='%s'",
			address, port, sock, host_name, svc_description, return_code, plugin_output);
		}
		else if (debug_detail_level >= DEBUG_DETAIL_NOTICE)
		{
		    int port;
		    get_full_peer_address(sock, address, sizeof(address), &port);
		    bronx_logprintf(BRONX_LOGGING_PASSIVE_CHECKS,
			"{handle_connection_read} Received SERVICE CHECK RESULT from client %s port %d (file descriptor %d) -> "
			"Host Name='%s', Service Description='%s', Return Code='%d'",
			address, port, sock, host_name, svc_description, return_code);
		}
		else if (debug_detail_level > DEBUG_DETAIL_NONE)
		{
		    bronx_logprintf(BRONX_LOGGING_PASSIVE_CHECKS,
			"{handle_connection_read} Received SERVICE CHECK RESULT");
		}
	    }
	}

	if (bronx_is_paused())
	    bronx_log("{handle_connection_read} BRONX PAUSED: Discarding Message.", BRONX_LOGGING_WARNING);
	else
	{
	    // Write directly into nagios.
	    post_check_result(host_name, svc_description, return_code, plugin_output, time_to_send);
	}
    }

    int outcome = register_read_handler(sock, handle_connection_read, (void *) CI);
    if (outcome != BRONX_OK)
    {
	bronx_log("{handle_connection_read} Failed to register read handler", BRONX_LOGGING_ERROR);
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return outcome;
    }

    return BRONX_OK;
}

/*
 * handle a client connection
 */

static int
handle_connection(int sock, void *unused, int cleanup_only)
{
    bronx_log("{handle_connection} entered", BRONX_LOGGING_DEBUG);

    init_packet send_packet;
    size_t bytes_to_send;
    int rc;
    int flags;
    time_t packet_send_time;
    struct crypt_instance *CI;
    /* Buffer to store peer IP address */
    char address[MAX_IPADDR_LENGTH];

    // The socket must be non-blocking, for our polling elsewhere to work as intended.
    if ((flags = fcntl (sock, F_GETFL, 0)) < 0 ||
	fcntl (sock, F_SETFL, flags | O_NONBLOCK) < 0)
    {
	char errno_string_buf[ERRNO_STRING_BUF_SIZE];
	bronx_logprintf (BRONX_LOGGING_ERROR,
	    "{handle_connection} cannot set file descriptor %d non-blocking, errno=%d (%s)",
	    sock, errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	// We know this failure is pretty much an indicator of severe tire damage.
	// But exiting the process is not a great way to deal with the problem;
	// this will bring down the entire Nagios process, not just this one socket.
	//
	// FIX LATER:  Perhaps change this to clean up and return BRONX_ERROR.
	//
	// FIX MINOR:  If we're going to die here, we ought to at least unconditionally
	// force out a Nagios log message even if Bronx logging itself is disabled.
	// That way, the administrator will have some clue as to what happened.
	exit (EXIT_FAILURE);
    }

    /* initialize encryption/decryption routines (server generates the IV to use and send to the client) */
    if (encrypt_init(_configuration->listener_password, _configuration->listener_encryption_method, NULL, &CI) != BRONX_OK)
    {
	get_peer_address(sock, address, sizeof(address));
	bronx_logprintf(BRONX_LOGGING_ERROR,
	    "{handle_connection} Dropping connection to client %s due to previous error.", address);
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    /* create initial packet to send to client (contains random IV and timestamp) */
    memcpy(&send_packet.iv[0], CI->transmitted_iv, TRANSMITTED_IV_SIZE);
    time(&packet_send_time);
    send_packet.timestamp=(u_int32_t)htonl(packet_send_time);

    /* send client the initial packet */
    bytes_to_send = sizeof(send_packet);
    rc = sendall(sock, (char *) &send_packet, &bytes_to_send);
    // check if there was an error sending the packet
    if (rc == -1)
    {
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING, "{handle_connection} Could not send init packet to client %s", address);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }
    else if (bytes_to_send < sizeof(send_packet))
    {
	/* for some reason we didn't send all the bytes we were supposed to */
	if (log_getlevel() >= BRONX_LOGGING_WARNING)
	{
	    get_peer_address(sock, address, sizeof(address));
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{handle_connection} Only able to send "SIZE_T_FORMAT" of "SIZE_T_FORMAT" bytes of init packet to client %s",
		bytes_to_send, sizeof(send_packet), address);
	}
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return BRONX_ERROR;
    }

    int outcome = register_read_handler(sock, handle_connection_read, (void *) CI);
    if (outcome != BRONX_OK)
    {
	bronx_log("{handle_connection} Failed to register read handler", BRONX_LOGGING_ERROR);
	encrypt_cleanup(_configuration->listener_encryption_method, CI);
	return outcome;
    }

    return BRONX_OK;
}

// Note that there are various dependencies here on IPv4; IPv6 is not yet supported by this code.

static int
accept_connection(int sock, void *unused, int cleanup_only)
{
    bronx_log("{accept_connection} entered", BRONX_LOGGING_DEBUG);

    int outcome = BRONX_OK;
    int new_sd = -1;
    struct sockaddr addr;
    struct sockaddr_in *nptr;
    socklen_t addrlen;
    char ipaddr[INET_ADDRSTRLEN];
    const char *address;
    int i;
    char *allowed_host_range;
    char *wildcard_pos;
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
    static time_t last_max_file_descriptor_message_time = 0;
    time_t now;

    // We don't want to accept so many connections that either we run out (and cannot log any
    // further error messages, for instance), or we interfere with the operation of Nagios itself,
    // or we are just going wild with new connections and potentially not servicing the existing
    // connections properly.
    if (npfds >= max_file_descriptors_in_process - _configuration->reserved_file_descriptor_count ||
	npfds >= _configuration->max_client_connections)
    {
	// This log message could potentially appear quite a lot (on every client connection, during
	// periods of intense client activity), so we throttle it.  That's why we say "may be" instead
	// of "will be" in the message; the overload condition might be cured quickly or slowly without
	// explicit notice, because this message won't necessarily show up every time this happens.
	time(&now);
	if (now - last_max_file_descriptor_message_time > 60)
	{
	    bronx_log("{accept_connection} Reached maximum listener file descriptors; new client connections may be delayed.",
		BRONX_LOGGING_WARNING);
	    last_max_file_descriptor_message_time = now;
	}

	// Reschedule the bronx_safe_accept() call (below) for later on, by which time hopefully
	// some file descriptors will have been closed elsewhere, allowing us to process the new
	// client connection.
	outcome = register_read_handler(sock, accept_connection, NULL);
	if (outcome != BRONX_OK)
	{
	    bronx_log("{accept_connection} Failed to register read handler", BRONX_LOGGING_ERROR);
	    return outcome;
	}
	return BRONX_OK;
    }

    /* wait for a connection request */
    while (! bronx_is_terminating())
    {
	// With regard to the subtleties of socket handling here, see Stevens,
	// Unix Network Programming, 3/e, sections 5.11 and 16.6.

	// FIX LATER:  bronx_safe_accept() currently behaves (or at least used to behave)
	// as a blocking call, waiting until some connection comes in.  But we already
	// have a blocking call in the poll() within handle_events(), so we don't need
	// another.  Figure out the current status of the code in this regard, and apply
	// any corrections necessary to ensure that if the call is indeed still blocking,
	// it will break out of that wait immediately if we get a termination signal.

	// We want bronx_safe_accept() to behave as a non-blocking call, so we can service
	// other sockets before a new connection comes in if none is immediately available.
	addrlen = sizeof(addr);
	if ((new_sd = bronx_safe_accept(sock, &addr, &addrlen)) >= 0)
	{
	    break;
	}
	else if
	    (
	    bronx_is_terminating()
	    || errno == EWOULDBLOCK
	    || errno == ECONNABORTED
	    || errno == EPROTO
	    || errno == EINTR
	    || errno == ECHILD
	    )
	{
	    outcome = register_read_handler(sock, accept_connection, NULL);
	    if (outcome != BRONX_OK)
	    {
		bronx_log("{accept_connection} Failed to register read handler", BRONX_LOGGING_ERROR);
		return outcome;
	    }
	    return BRONX_OK;
	}
	else
	{
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{accept_connection} Network server accept failure, errno=%d (%s)",
		errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	    // Note:  we're in a situation of severe tire damage here.  Returning an error here will cause the
	    // calling code to close the entire "sock" descriptor.  That will in turn cause the calling code to
	    // kill any number of good connections that are still queued up and might have useful data available,
	    // causing a failure on the listening socket to cascade into a rather worse condition.  We don't like
	    // that, but it seems better than putting ourselves into some kind of limp-along mode trying to read
	    // all the other descriptors until they are exhausted, but not knowing when that might occur, and only
	    // then resetting the listening socket.  Perhaps a future version will be more sophisticated, leaving
	    // all the other sockets still open and only closing and re-opening the listening socket.
	    return BRONX_ERROR;
	}
    }
    outcome = register_read_handler(sock, accept_connection, NULL);
    if (outcome != BRONX_OK)
    {
	bronx_log("{accept_connection} Failed to register read handler", BRONX_LOGGING_ERROR);
	bronx_safe_close(new_sd);
	return outcome;
    }

    nptr = (struct sockaddr_in *) &addr;
    address = inet_ntop(AF_INET, &nptr->sin_addr, ipaddr, sizeof(ipaddr));
    if (address == NULL)
    {
	address = "unknown address";
    }
    if (_configuration->listener_num_allowed_hosts != 0)
    {
	for (i = 0; i < _configuration->listener_num_allowed_hosts; i++)
	{
	    allowed_host_range = _configuration->listener_allowed_hosts[i];
	    wildcard_pos = strchr(allowed_host_range, '*');

	    if (wildcard_pos == NULL)
	    {
		if (!strcmp(address, allowed_host_range))
		    break;
	    }
	    else
	    {
		if (!strncmp(address, allowed_host_range, (wildcard_pos - allowed_host_range)))
		    break;
	    }
	}
	if (i == _configuration->listener_num_allowed_hosts)
	{
	    bronx_logprintf(BRONX_LOGGING_INFO,
		"{accept_connection} CONNECTION FROM %s NOT ON ALLOWED HOST LIST, NOT ACCEPTING CONNECTION", address);
	    bronx_safe_close(new_sd);
	    return BRONX_OK;
	}
    }

    // Handle the new connection, by queueing it for the initial i/o (which must always
    // be a write from the server, as part of negotiating the encryption protocol).
    outcome = register_write_handler(new_sd, handle_connection, NULL);
    if (outcome != BRONX_OK) {
	bronx_log("{accept_connection} Failed to register write handler", BRONX_LOGGING_ERROR);
	bronx_safe_close(new_sd);
	return outcome;
    }

    bronx_logprintf(BRONX_LOGGING_DEBUG, "{accept_connection} Accepting connection on file descriptor %d from client %s port %d",
	new_sd, address, ntohs(nptr->sin_port));

    return BRONX_OK;
}

/*
 * wait for incoming connection requests
 */

static int
manage_connections()
{
    struct sockaddr_in myname;
    int sock=0, flag, rv;
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];

    // create a socket for listening

    sock = bronx_safe_socket(AF_INET, SOCK_STREAM, 0);

    // exit if we couldn't create the socket

    if (sock < 0)
	listener_thread_exit("(manage_connections) Network server socket failure", STATE_CRITICAL);

    bronx_logprintf(BRONX_LOGGING_INFO, "{manage_connections} Established local socket (file descriptor %d) for listening.", sock);

    // set the reuse address flag so we don't get errors when restarting

    flag = 1;
    if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (char *) &flag, sizeof(flag)) < 0)
    {
	bronx_safe_close(sock);
	listener_thread_exit("(manage_connections) Could not set reuse address option on socket!", STATE_CRITICAL);
    }

    myname.sin_family = AF_INET;
    myname.sin_port   = htons(_configuration->listener_port);
    bzero(&myname.sin_zero, sizeof(myname.sin_zero));

    // what address should we bind to?

    if (!strlen(_configuration->listener_address))
    {
	bronx_log("{manage_connections} Server address is <automatic>, using INADDR_ANY.", BRONX_LOGGING_INFO);
	myname.sin_addr.s_addr = htonl(INADDR_ANY);
    }
    else
    {
	bronx_logprintf(BRONX_LOGGING_INFO,
	    "{manage_connections} Server address specified as '%s'", _configuration->listener_address);
	if (!my_inet_aton(_configuration->listener_address, &myname.sin_addr))
	{
	    bronx_safe_close(sock);
	    listener_thread_exit("(manage_connections) Server listener_address is not a valid IP address", STATE_CRITICAL);
	}
    }

    // bind the address to the Internet socket

    rv = bind(sock, (struct sockaddr *) &myname, sizeof(myname));
    if (rv < 0)
    {
	bronx_logprintf(BRONX_LOGGING_ERROR, "{manage_connections} Network server bind failure, errno=%d (%s)",
	    errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	bronx_safe_close(sock);
	listener_thread_exit("(manage_connections) Network server bind failure", STATE_CRITICAL);
    }
    bronx_logprintf(BRONX_LOGGING_DEBUG, "{manage_connections} Listener socket is bound to address %s port %d.",
	inet_ntoa(myname.sin_addr), ntohs(myname.sin_port));

    // open the socket for listening

    if (listen(sock, SOMAXCONN) < 0)
    {
	bronx_safe_close(sock);
	listener_thread_exit("(manage_connections) Network server listen failure", STATE_CRITICAL);
    }

    bronx_logprintf(BRONX_LOGGING_INFO, "{manage_connections} Socket descriptor %d is open for listening on port %d.",
	sock, ntohs(myname.sin_port));

    // listen for connection requests

    int outcome = register_read_handler(sock, accept_connection, NULL);
    if (outcome != BRONX_OK)
    {
	bronx_log("{manage_connections} Failed to register read handler", BRONX_LOGGING_ERROR);
	bronx_safe_close(sock);
	listener_thread_exit("(manage_connections) Failed to register read handler", STATE_CRITICAL);
    }
    bronx_logprintf(BRONX_LOGGING_DEBUG, "{manage_connections} Registered read handler and polling for primary listener file descriptor %d.", sock);

    // Enter our event handling loop.

    bronx_log("{manage_connections} Entering listener main event wait loop.", BRONX_LOGGING_DEBUG);

    // If the listening socket gets closed along the way for any reason while handling events, and
    // we had no way of finding out that the listening socket got closed, we would continue looping
    // here until the last client connection closes.  Of course, if that client connection were
    // from some dead machine, it might never close, since we don't have any sort of low-level
    // keepalive heartbeat in place to detect the remote failure.  And in that case we might never
    // allow any more connections from any other machine, until Bronx is terminated and restarted.
    // Even with such a heartbeat, it might take a couple of hours for the keepalive to finally
    // recognize that the remote end is no longer accessible, and in that time a lot of monitoring
    // data could be lost and a lot of alarms could go off.  To solve this, we have handle_events()
    // flag in its return value when the socket for pfds[1] (presumed to be the listening socket,
    // after pfds[0] is established as the stopfd) is closed.  We use that error status here to
    // exit the loop and shut down all the other sockets so we can exit from manage_connections()
    // and then re-initialize ourself by looping around inside thread_listener() and calling
    // manage_connections() again.  If we shut down some active client connections while doing this,
    // that is considered an acceptable loss in preference to a possible wholesale failure.  The
    // packets they have already sent may have been already received and acknowledged by the kernel,
    // so if our application intentionally fails to read them from the kernel, there will be a
    // slight loss of data.

    // The bigger question is why the listening socket might sometimes get closed by the system,
    // without our manifest intention to do so.  If we can, in a future release we should try to
    // collect data on why that happens, when it happens.

    int event_status = BRONX_OK;
    while (! bronx_is_terminating())
    {
	// Check for new events, and handle them.
	event_status = handle_events();
	if (event_status != BRONX_OK)
	{
	    bronx_log("{manage_connections} Forcing listener event loop restart.", BRONX_LOGGING_WARNING);
	    break;
	}
    }
    // Close all sockets, including both the primary listener socket we opened just above, and
    // all sockets subsequently opened from client connections to the primary listener socket.
    // This will leave the stopfd still open; that should be eventually closed separately by
    // the calling code.
    close_all_sockets();

    bronx_log("{manage_connections} Exiting connection handler.", BRONX_LOGGING_INFO);
    return event_status;
}

/*
 *  thread_listener
 *
 *  This is the thread main for the listener thread.
 */

void* APR_THREAD_FUNC thread_listener(apr_thread_t *thread, void *data)
{
    struct rlimit RLIMIT_NOFILE_limits;
    int outcome = BRONX_OK;

    bronx_log("{thread_listener} Listener Thread Started.", BRONX_LOGGING_INFO);

    if (getrlimit(RLIMIT_NOFILE, &RLIMIT_NOFILE_limits))
    {
	// An error occurred.
	char errno_string_buf[ERRNO_STRING_BUF_SIZE];
	bronx_logprintf (BRONX_LOGGING_ERROR, "{thread_listener} error: getrlimit() returned errno=%d (%s)",
	    errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	outcome = BRONX_ABORT;
    }
    else
    {
	max_file_descriptors_in_process = RLIMIT_NOFILE_limits.rlim_cur;
    }

    if (outcome == BRONX_OK)
    {
	stopfd = *(int *) data;
	outcome = register_poll(POLLIN, stopfd);  // stopfd will always be pfds[0].
	if (outcome == BRONX_OK)
	{
	    bronx_logprintf(BRONX_LOGGING_DEBUG, "{thread_listener} Registered listener stop-pipe file descriptor %d for polling.", stopfd);

	    generate_crc32_table();

	    while (! bronx_is_terminating())
	    {
		/* wait for connections */
		outcome = manage_connections();
		if (outcome == BRONX_ABORT)
		{
		    bronx_log("{thread_listener} Bronx internal error detected.", BRONX_LOGGING_ERROR);
		    break;
		}
	    }
	}
    }

    bronx_log("{thread_listener} Bronx Terminating, Exiting Listener Thread.", BRONX_LOGGING_INFO);
    if (outcome == BRONX_ABORT)
	listener_thread_exit("Exiting Listener Abnormally.", STATE_CRITICAL);
    else
	listener_thread_exit("Exiting Listener Normally.", STATE_OK);
    return(NULL);
}
