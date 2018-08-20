/* 
//  bronx_safe_fork.c -- Enforce safe fork() actions.
//
//  Copyright (c) 2009-2017 Groundwork Open Source
// 
//  This program is free software; you can redistribute it and/or
//  modify it under the terms of the GNU General Public License
//  as published by the Free Software Foundation; either version 2
//  of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin Street, Fifth Floor
//  Boston, MA  02110-1301, USA.
//
//	2009-04-21 GH;	Created.
//	2012-06-21 GH;	Improved comments.
//	2012-12-05 GH;	Improved comments.
//	2017-01-29 GH;	Updated to support Nagios 4.2.4.
*/

/*
// This file contains routines needed to properly close out resources
// which are inherited by child processes.  The complexity here is due
// to the fact that the rest of the Bronx code is constructed so as to
// dynamically create and destroy file descriptors as it operates, in
// a manner which is asynchronous with respect to fork()ing operations
// that originate in the main Nagios thread.
//
// We are implementing what is necessary to avoid problems.  Our biggest
// concern is the extra overhead of synchronization between threads that
// this introduces.  Said overhead will be constantly invoked as sockets
// are opened and closed, and as fork() operations occur.  However, due
// to the presence of race conditions in the opening and closing of file
// descriptors and the recording of the list of such file descriptors in
// a place that can be easily accessed by child processes, there appears
// to be no way to avoid said overhead.
//
// Note that some (or perhaps all) of the potential problems this code
// is trying to protect against could have been handled much more simply
// by using the FD_CLOEXEC flag on the file descriptor, via fcntl().  We
// leave it as an exercise for the reader to determine the extent to
// which that might be true.
*/

/*--------------------------------------------------------------*/

#include "bronx.h"
#include "bronx_log.h"
#include "bronx_safe_fork.h"
#include "bronx_utils.h"

#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <errno.h>
#include <sys/select.h>
#include <fcntl.h>

#if USE_SAFE_FORK_PROTECTION

/*--------------------------------------------------------------*/

#define	FREE_LIST_BLOCK_SIZE	10

/*
// The list of open file descriptors, and a corresponding list of free elements.
*/
struct bronx_open_fd {
    struct bronx_open_fd *next;
    struct bronx_open_fd *prev;
    int fd;
};

struct bronx_open_fd *bronx_fds_head = NULL;
struct bronx_open_fd *bronx_fds_tail = NULL;

struct bronx_open_fd *bronx_free_fds_head = NULL;
struct bronx_open_fd *bronx_free_fds_tail = NULL;

/*
// Synchronization structures needed to protect the list of open file descriptors.
//
// Access to the lists must always be protected by the associated mutex.
// The condition variable is used to wake up any process which is waiting 
// for the queue to become non-empty.
*/
pthread_mutex_t bronx_fds_queue_mutex = PTHREAD_MUTEX_INITIALIZER;

/*--------------------------------------------------------------*/

/*
// Note:  bronx_allocate_fd_block() must be called under the
// externally-imposed protection of the bronx_fds_queue_mutex mutex.
*/

int bronx_allocate_fd_block ()
    {
    if (! bronx_free_fds_tail)
	{
	int i;

	bronx_free_fds_head = (struct bronx_open_fd *) malloc (sizeof (struct bronx_open_fd) * FREE_LIST_BLOCK_SIZE);
	if (! bronx_free_fds_head)
	    {
	    return (BRONX_FAILURE);
	    }
	bronx_free_fds_tail = bronx_free_fds_head;
	for (i = 0; i < FREE_LIST_BLOCK_SIZE; ++i)
	    {
	    bronx_free_fds_tail->next = bronx_free_fds_tail + 1;
	    bronx_free_fds_tail->prev = bronx_free_fds_tail - 1;
	    ++bronx_free_fds_tail;
	    }
	--bronx_free_fds_tail;

	bronx_free_fds_head->prev = NULL;
	bronx_free_fds_tail->next = NULL;
	}

    return (BRONX_SUCCESS);
    }

/*--------------------------------------------------------------*/

/*
// Routines to maintain the list of open file descriptors.
//
// Since these must be called in conjunction with other operations,
// they do not themselves call the synchronization routines.  The
// caller must take of that, wrapping both the other action and the
// call to this routine together to make the entire operation atomic.
//
// Note that the current implementation takes no trouble either to
// prevent remembering the same descriptor twice, or to look for
// duplicate copies of a given descriptor to forget at one time.
// Thus we are dependent on the calling code to maintain discipline
// so the list of remembered file descriptors stays sychronized with
// the actual descriptors in use by the process.
*/

int bronx_remember_fd (int fd)
    {
    if (bronx_allocate_fd_block () != BRONX_SUCCESS)
	{
	return (BRONX_FAILURE);
	}
    bronx_free_fds_tail->fd = fd;
    bronx_free_fds_tail->next = bronx_fds_head;
    if (bronx_fds_head)
	{
	bronx_fds_head->prev = bronx_free_fds_tail;
	}
    else
	{
	bronx_fds_tail = bronx_free_fds_tail;
	}
    bronx_fds_head = bronx_free_fds_tail;
    bronx_free_fds_tail = bronx_free_fds_tail->prev;
    if (bronx_free_fds_tail)
	{
	bronx_free_fds_tail->next = NULL;
	}
    else
	{
	bronx_free_fds_head = NULL;
	}
    bronx_fds_head->prev = NULL;

    return (BRONX_SUCCESS);
    }

/*--------------------------------------------------------------*/

int bronx_forget_fd (int fd)
    {
    struct bronx_open_fd *bronx_fds_ptr;

    for (bronx_fds_ptr = bronx_fds_head; bronx_fds_ptr; bronx_fds_ptr = bronx_fds_ptr->next)
	{
	if (bronx_fds_ptr->fd == fd)
	    {
	    if (bronx_fds_ptr == bronx_fds_head)
		bronx_fds_head = bronx_fds_ptr->next;
	    if (bronx_fds_ptr == bronx_fds_tail)
		bronx_fds_tail = bronx_fds_ptr->prev;
	    if (bronx_fds_ptr->next)
		bronx_fds_ptr->next->prev = bronx_fds_ptr->prev;
	    if (bronx_fds_ptr->prev)
		bronx_fds_ptr->prev->next = bronx_fds_ptr->next;
	    if (bronx_free_fds_tail)
		{
		bronx_free_fds_tail->next = bronx_fds_ptr;
		}
	    else
		{
		bronx_free_fds_head = bronx_fds_ptr;
		}
	    bronx_fds_ptr->next = NULL;
	    bronx_fds_ptr->prev = bronx_free_fds_tail;
	    bronx_free_fds_tail = bronx_fds_ptr;
	    return (BRONX_SUCCESS);
	    }
	}

    return (BRONX_FAILURE);
    }

/*--------------------------------------------------------------*/

/*
// Routines to invoke around fork() operations.
*/

void bronx_atfork_prepare (void)
    {
    pthread_mutex_lock (&bronx_fds_queue_mutex);
    }

/*--------------------------------------------------------------*/

void bronx_atfork_parent (void)
    {
    pthread_mutex_unlock (&bronx_fds_queue_mutex);
    }

/*--------------------------------------------------------------*/

void bronx_atfork_child (void)
    {
    /*
    // Remove each recorded fd and close it.
    */

    while (bronx_fds_head)
	{
	close (bronx_fds_head->fd);
	if (bronx_free_fds_tail)
	    {
	    bronx_free_fds_tail->next = bronx_fds_head;
	    bronx_fds_head->prev = bronx_free_fds_tail;
	    }
	else
	    {
	    bronx_free_fds_head = bronx_fds_head;
	    }
	bronx_free_fds_tail = bronx_fds_head;
	bronx_fds_head = bronx_fds_head->next;
	bronx_free_fds_tail->next = NULL;
	if (bronx_fds_head)
	    {
	    bronx_fds_head->prev = NULL;
	    }
	else
	    {
	    bronx_fds_tail = NULL;
	    }
	}

    pthread_mutex_unlock (&bronx_fds_queue_mutex);
    }

/*--------------------------------------------------------------*/

void call_atfork_init_once ()
    {
    int this_errno;

    this_errno = pthread_atfork (bronx_atfork_prepare, bronx_atfork_parent, bronx_atfork_child);
    if (this_errno)
	{
	char errno_string_buf[ERRNO_STRING_BUF_SIZE];
	bronx_logprintf (BRONX_LOGGING_ERROR,
	    "{bronx_init_safe_operations} pthread_atfork() failure, errno=%d (%s)",
	    this_errno, bronx_strerror_r (this_errno, errno_string_buf, sizeof (errno_string_buf)));
	exit (EXIT_FAILURE);
	}
    }

/*--------------------------------------------------------------*/

/*
// Routine to initialize the safety mechanisms.
*/

int bronx_init_safe_operations (void)
    {
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
    static pthread_once_t atfork_init_just_once = PTHREAD_ONCE_INIT;
    int this_errno;

    pthread_mutex_lock (&bronx_fds_queue_mutex);
    if (bronx_allocate_fd_block () != BRONX_SUCCESS)
	{
	bronx_logprintf (BRONX_LOGGING_ERROR,
	    "{bronx_init_safe_operations} cannot allocate space for file descriptors, errno=%d (%s)",
	    errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	exit (EXIT_FAILURE);
	}
    pthread_mutex_unlock (&bronx_fds_queue_mutex);

    this_errno = pthread_once (&atfork_init_just_once, call_atfork_init_once);
    if (this_errno)
	{
	bronx_logprintf (BRONX_LOGGING_ERROR,
	    "{bronx_init_safe_operations} cannot initialize safe fork() operations, errno=%d (%s)",
	    this_errno, bronx_strerror_r (this_errno, errno_string_buf, sizeof (errno_string_buf)));
	exit (EXIT_FAILURE);
	}

    return (BRONX_SUCCESS);
    }

/*--------------------------------------------------------------*/

/*
// Routines to reliably create and destroy file descriptors,
// in conjunction with maintaining a list of the open file
// descriptor so they can be used by the atfork handlers.
*/

int bronx_safe_socket (int domain, int type, int protocol)
    {
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
    int fd;

    pthread_mutex_lock (&bronx_fds_queue_mutex);
    fd = socket (domain, type, protocol);
    if (fd >= 0)
	{
	if (bronx_remember_fd (fd) != BRONX_SUCCESS)
	    {
	    bronx_logprintf (BRONX_LOGGING_ERROR,
		"{bronx_safe_socket} cannot save file descriptor, errno=%d (%s)",
		errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	    exit (EXIT_FAILURE);
	    }
	}
    pthread_mutex_unlock (&bronx_fds_queue_mutex);

    if (fd >= 0)
	{
	int flags;

	if ((flags = fcntl (fd, F_GETFL, 0)) < 0 ||
	    fcntl (fd, F_SETFL, flags | O_NONBLOCK) < 0)
	    {
	    bronx_logprintf (BRONX_LOGGING_ERROR,
		"{bronx_safe_socket} cannot set file descriptor %d non-blocking, errno=%d (%s)",
		fd, errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	    exit (EXIT_FAILURE);
	    }
	}

    return (fd);
    }

/*--------------------------------------------------------------*/

// Note:  It would be bad if the accept() buried in this function were to block while holding
// the mutex lock.  That would prevent all other threads from accessing this critical region
// until an outside connection succeeded and we exited the critical region.  And in particular,
// that would prevent the main Nagios thread from fork()ing for the duration of the wait.  To
// ensure that accept() never blocks, the passed listening socket "s" MUST have the O_NONBLOCK
// flag set (see socket(7)).  To avoid excessive system calls, we insist that said flag be set
// as a precondition for calling bronx_safe_accept().  And so the application developer need
// not be concerned about it, that will be done automatically by the preceding bronx_safe_socket()
// call.  But otherwise, to simplify the calling application code, bronx_safe_accept() itself
// will handle the blocking in a safe manner, meaning that in spite of the non-blocking nature
// of the listening socket, bronx_safe_accept() will behave as if it were operating on a
// blocking socket.
//
// (NOTE:  That used to be true.  But now, bronx_safe_accept() behaves as if it were operating
// on a non-blocking socket.  And as far as we know, that's perfectly acceptable to all the
// callers of this routine [and even required; see comments in accept_connection() within the
// bronx_listener.c code].)
//
// Part of this simplification is choosing the proper method for efficiently performing the
// wait, and not requiring the application developer to both figure that out and replicate that
// code in all the places where it is required.

int bronx_safe_accept (int s, struct sockaddr *addr, socklen_t *addrlen)
    {
    struct timeval select_timeout;
    int status;
    int nfds;
    fd_set readfds;
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
    int fd;

    // Our basic strategy here is to split a blocking accept() call into a blocking wait, which
    // can and must occur outside of the critical region, and a non-blocking accept(), which must
    // occur within the critical region so we can atomically save the returned file descriptor
    // in our list of file descriptors in use.  We have three choices for how to do the wait:
    //
    //     select()
    //     pselect()
    //     poll()
    //
    // I would generally prefer pselect() for its clean integration of the descriptor events and
    // signal handling.  However, pselect() was only added to Linux in kernel 2.6.16.  Prior to
    // this, pselect() was emulated in glibc, and this emulation was subject to exactly the same
    // race condition that pselect() was designed to avoid!!  So for portability to older Linux
    // releases, we must avoid pselect().
    //
    // For simplicity, we'll just use select() rather than the somewhat more cumbersome setup
    // needed for poll().

    // Without a defined timeout, this select() call would block indefinitely if there were no
    // new connection available, which would mean that we would be holding up all i/o activity
    // from this thread on other already-connected sockets while we wait for a new connection
    // to show up on this listening socket.  In practice, we try to get around that in a wider
    // context by ensuring that we only call bronx_safe_accept() in a context in which we
    // already know the socket is ready for reading, assuming that we will get the same result
    // from the select() call here (instant response rather than blocking now).  That might work
    // in general, but given that this is network programming, the situation is actually more
    // complex.  See Stevens, UNIX Network Programming, 3/e, Section 16.6 for details.  We might
    // have previously found a new connection to be ready, but between then and now the client
    // might have dropped the connection, which could mean that we might still wait here for a
    // new connection to become available.  So we use a reasonably short timeout and return
    // EWOULDBLOCK if the timeout expires.  Of course, that makes the "s" socket act like a
    // non-blocking socket, so we need to change our description of bronx_safe_accept() above
    // and ensure that all calling code treats it like that.
    //
    // FIX MINOR:  If bronx_safe_accept() is going to act like a non-blocking accept(), and
    // that behavior is acceptable to (and even desired by) all calling code, and we have
    // elsewhere guaranteed that the socket is always set to non-blocking mode, is there
    // still any reason for us to be calling select() here?  Can't we just skip that part?

    FD_ZERO (&readfds);
    FD_SET (s, &readfds);
    nfds = s + 1;
    select_timeout.tv_sec  = 0;
    select_timeout.tv_usec = 0;
    status = select (nfds, &readfds, NULL, NULL, &select_timeout);
    if (status == 0)
        {
	errno = EWOULDBLOCK;
	return (-1);
	}
    if (status < 0)
	{
	return (status);
	}

    pthread_mutex_lock (&bronx_fds_queue_mutex);
    // Given our standard mechanism for opening "s" via bronx_safe_socket(),
    // this call to accept() is known to be non-blocking.
    fd = accept (s, addr, addrlen);
    if (fd >= 0)
	{
	if (bronx_remember_fd (fd) != BRONX_SUCCESS)
	    {
	    bronx_logprintf (BRONX_LOGGING_ERROR,
		"{bronx_safe_accept} cannot save file descriptor, errno=%d (%s)",
		errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	    exit (EXIT_FAILURE);
	    }
	}
    pthread_mutex_unlock (&bronx_fds_queue_mutex);

    return (fd);
    }

/*--------------------------------------------------------------*/

// Note the following, from the Linux close() man pages:
//
//     Not checking the return value of close is a common but nevertheless serious programming
//     error. It is quite possible that errors on a previous write(2) operation are first
//     reported at the final close. Not checking the return value when closing the file may
//     lead to silent loss of data. This can especially be observed with NFS and disk quotas.
//
//     A successful close does not guarantee that the data has been successfully saved to disk,
//     as the kernel defers writes. It is not common for a filesystem to flush the buffers
//     when the stream is closed. If you need to be sure that the data is physically stored
//     use fsync(2). (It will depend on the disk hardware at this point.)
//
//     If close() is interrupted by a signal that is to be caught, it shall return -1 with
//     errno set to [EINTR] and the state of fildes is unspecified. If an I/O error occurred
//     while reading from or writing to the file system during close(), it may return -1 with
//     errno set to [EIO]; if this error is returned, the state of fildes is unspecified.
//
// The problem for us in these statements is that we need to know whether or not we really
// ought to forget the file descriptor.  But apparently there is no good way to know, just
// from the return code.  And we cannot just retry the close() operation, because if the
// file descriptor was actually just closed and in the meantime some other thread managed
// to re-open that file descriptor, we'll be closing the newly opened descriptor rather
// than an old descriptor that never got closed in the first place.  This must be considered
// a serious defect in the specification of close(), because it means that we cannot reliably
// figure out whether our child at-fork handler needs to close this file descriptor.  The
// only good workarounds are at the application level.  One must arrange that any thread
// that calls close() cannot be interrupted, and that all i/o operations are completely
// flushed before close() is called, so close() cannot fail due to i/o failure.  However,
// a close() might be the point at which file metadata updates are forced, so perhaps there
// is no guarantee even then.  See the man pages for fsync() and fdatasync(), the Linux
// versions of which state:
//
//     fsync copies all in-core parts of a file to disk, and waits until the device reports
//     that all parts are on stable storage. It also updates metadata stat information. It
//     does not necessarily ensure that the entry in the directory containing the file has
//     also reached disk. For that an explicit fsync on the file descriptor of the directory
//     is also needed.
//
// Possibly this stuff doesn't apply to a network socket, partly because it won't have any
// attached persistent metadata, but the man pages are not sufficiently clear about that for
// us to tell unambiguously.  For the moment, we won't implement logic to handle such concerns,
// but we will at least log situations that might require such attention, so we'll have some
// clue as to whether this code should evolve in that direction.

int bronx_safe_close (int fd)
    {
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
    int status;
    int forget_status = BRONX_SUCCESS;

#ifdef INCLUDE_EXTRA_DEBUG_DETAIL
    bronx_logprintf (BRONX_LOGGING_DEBUG, "{bronx_safe_close} closing file descriptor %d", fd);
#endif

    pthread_mutex_lock (&bronx_fds_queue_mutex);
    status = close (fd);
    // For future development:  possibly, we ought not to forget this fd if we got EINTR,
    // as long as the calling code will handle EINTR and call bronx_safe_close() again on
    // this same descriptor.
    if (status == 0 || errno != EBADF)
	{
	forget_status = bronx_forget_fd (fd);
	}
    pthread_mutex_unlock (&bronx_fds_queue_mutex);

    if (forget_status != BRONX_SUCCESS)
	{
	bronx_logprintf (BRONX_LOGGING_WARNING,
	    "{bronx_safe_close} could not find descriptor %d while trying to forget it", fd);
	}

    if (status)
	{
	bronx_logprintf (BRONX_LOGGING_WARNING,
	    "{bronx_safe_close} close(%d) failed, errno=%d (%s)",
	    fd, errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	}

    return (status);
    }

/*--------------------------------------------------------------*/

/*
// Routines to reliably create and destroy FILE handles, in conjunction with
// maintaining a list of the associated open file descriptors so they can be
// used by the atfork handlers.  Use these as direct one-for-one replacements
// for the standard fopen() and fclose() functions.  Handling FILE handles in
// a child process is trickier than handling the underlying file descriptors,
// because the child process is not allowed to flush any pending buffered
// output.  That responsibility is left to the parent process.  Here we just
// remember to close the low-level file descriptor in the child process.
*/

FILE *bronx_safe_fopen (const char *filename, const char *mode)
    {
    FILE *stream;
    int fd;
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];

    pthread_mutex_lock (&bronx_fds_queue_mutex);
    stream = fopen (filename, mode);
    if (stream)
	{
	fd = fileno (stream);
	if (bronx_remember_fd (fd) != BRONX_SUCCESS)
	    {
	    bronx_logprintf (BRONX_LOGGING_ERROR,
		"{bronx_safe_fopen} cannot save file descriptor, errno=%d (%s)",
		errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	    exit (EXIT_FAILURE);
	    }
	}
    pthread_mutex_unlock (&bronx_fds_queue_mutex);

    return (stream);
    }

/*--------------------------------------------------------------*/

int bronx_safe_fclose (FILE *stream)
    {
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
    int fd;
    int status;
    int forget_status = BRONX_SUCCESS;

    fd = fileno (stream);

    pthread_mutex_lock (&bronx_fds_queue_mutex);
    status = fclose (stream);
    // For future development:  possibly, we ought not to forget this fd if we got EINTR,
    // as long as the calling code will handle EINTR and call bronx_safe_fclose() again on
    // this same stream.
    if (status == 0 || errno != EBADF)
	{
	forget_status = bronx_forget_fd (fd);
	}
    pthread_mutex_unlock (&bronx_fds_queue_mutex);

    if (forget_status != BRONX_SUCCESS)
	{
	bronx_logprintf (BRONX_LOGGING_WARNING,
	    "{bronx_safe_fclose} could not find descriptor %d while trying to forget it", fd);
	}

    if (status)
	{
	bronx_logprintf (BRONX_LOGGING_WARNING,
	    "{bronx_safe_fclose} fclose() failed, errno=%d (%s)",
	    errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	}

    return (status);
    }

/*--------------------------------------------------------------*/

#endif
