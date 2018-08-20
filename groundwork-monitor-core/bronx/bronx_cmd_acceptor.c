/*
 *  bronx_cmd_acceptor.c -- Listening for Nagios commands.
 *
 *  The thread listens for Nagios commands on a TCP port,
 *  conditions them and writes them to Nagios command pipe.
 *  In the command received the first two fields are for
 *  command acceptor processing. The remaining part of the 
 *  command is assumed to be in the format understood by Nagios.
 *  The command acceptor inserts the timestamp in the beginning 
 *  of the command, before submitting it to Nagios.  
 * 
 *  Copyright (c) 2009-2017 Groundwork Open Source
 *  Originally written by Hrisheekesh Kale 
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
 *	2009-02-18 HK;	Created.
 *	2012-05-14 GH;	Added some encryption debug code, and support
 *			for an anonymous pipe to break out of poll()
 *			before timeout.
 *	2012-06-22 GH;	Improved error handling.
 *			Normalized indentation whitespace.
 *	2012-12-06 GH;	Fixed a htons()/ntohs() confusion.
 *	2017-01-31 GH;	Updated to support Nagios 4.2.4; cleaned up some logic.
 */

#include "bronx.h"
#include "bronx_config.h"
#include "bronx_log.h"
#include "bronx_cmd_acceptor.h"
#include "bronx_cmd_acceptor_utils.h"
#include "bronx_safe_fork.h"
#include "bronx_thread.h"
#include "bronx_utils.h"

#include <time.h>
#include <fcntl.h>

/*  Globals */
/* Read handlers, write handlers and poll fds counters */
int ca_maxrhand = 0;
int ca_maxwhand = 0;
int ca_maxpfds  = 0;
int ca_nrhand   = 0;
int ca_nwhand   = 0;
int ca_npfds    = 0;
int ca_stopfd   = -1;
struct ca_handler_entry *ca_rhand = NULL; /* Read handler pool */
struct ca_handler_entry *ca_whand = NULL; /* Write handler pool */
struct pollfd           *ca_pfds  = NULL; /* Socket fds pool */

/*
 * write_cmd_to_nagios_pipe()
 * Write the command to Nagios command pipe.
 * Parameters -
 * char *cmd     : Nagios command
 * Return -
 * CA_OK on success, CA_ERROR on failure.
 */
int
write_cmd_to_nagios_pipe(char *cmd)
{
   
    int rv = 0;
    int buf_len = 0;
    FILE *pipe_fp = NULL;
    int status = CA_OK;

    /*
     * Check if we can access the Nagios command pipe file.  Return an error if we can't.
     * More specifically, bail if the command pipe does not exist.  Calling fopen() in
     * append mode will create the file (as an ordinary file, not as the pipe that it needs
     * to be), if it does not exist.  Bronx should not do that.  But there is clearly a
     * TOCTOU (time-of-check/time-of-use) race condition here, since we might check, find
     * it's writeable, and then it might disappear before we can fopen() it, cauusing it to
     * be created.  So this check will help detect user error in setting the command pipe
     * name in the config file, but it does not completely guarantee correct operation.
     */
    if (access(_configuration->nagios_cmdpipe_filename, W_OK) != 0)
    {
	bronx_log("{send_cmd_to_nagios_pipe} Failed to access Nagios command pipe", BRONX_LOGGING_WARNING);
	status = CA_ERROR;
    }
    /* Open the Nagios command pipe for writing */
    else if ( (pipe_fp = bronx_safe_fopen(_configuration->nagios_cmdpipe_filename, "a")) == NULL )
    {
	bronx_log("{send_cmd_to_nagios_pipe} Failed to open the Nagios command pipe", BRONX_LOGGING_WARNING);
	status = CA_ERROR;
    }
    else
    {
	/*
	 * Write the command to the pipe.  Note that Nagios itself ignores SIGPIPE,
	 * so if this fails for some odd reason, at least we won't die here.
	 *
	 * Also note that we're not taking any precautions here to ensure that the
	 * size of our write to is no larger than PIPE_BUF, the max size of an atomic
	 * write to a pipe.  The longstanding typical value of PIPE_BUF is 4096, so
	 * we're not too likely to run into that limit, but we need to record here
	 * the fact that we're not invoking any protections against exceeding it.
	 *
	 * For that matter, we're presuming that fprintf() is doing just one system-level
	 * write() call to the pipe, to keep it atomic (all the data is guaranteed to be
	 * contiguous in the pipe).  If its own buffer is smaller than PIPE_BUF, it could
	 * break up the data into multiple write() calls, which might get interleaved with
	 * data from other sources, and we would end up with confusion and breakage.  If
	 * that happens, both this command here and the interleaved command(s) would be
	 * broken, leading to unexpected and unreliable overall system behavior.
	 *
	 * FIX MAJOR:  Since we have only a single string to write without any internal
	 * formatting at this point, the only reason not to use a plain open()/write()/close()
	 * sequence here using pipe_fd instead of pipe_fp is whatever safety guarantees we get
	 * with bronx_safe_fopen() and bronx_safe_fclose() calls.  Perhaps there is some way
	 * to get the FD_CLOEXEC flag set directly in an open() call so we get the same effect
	 * in a simpler manner, without any race condition between the open() and the fcntl()
	 * we would otherwise need to call.  (We would want to call open() with the O_APPEND
	 * and O_CLOEXEC flags but not the O_CREAT flag.  The O_CLOEXEC flag is not specified
	 * in POSIX.1-2001, but is specified in POSIX.1-2008.  It is supported in Linux since
	 * kernel 2.6.23.  We would want to compare that kernel against the kernel levels of
	 * all of our supported platforms before we depend on it.)
	 *
	 * FIX LATER:  Also note that an atomic write might hang indefinitely until there is
	 * room in the pipe for the complete buffer we're trying to write.  That will hold up
	 * this thread if it happens.  We might want to impose some sort of alarm timeout to
	 * break out of such a hung write() call, and just abort the submission of this
	 * command under such circumstances.  Since this is operating in multi-threaded code,
	 * we would need to take very careful precautions to ensure that no other thread is
	 * disrupted by any signal emanating from such a timeout expiration.
	 */
	buf_len = strlen(cmd);
	rv = fprintf(pipe_fp, "%s", cmd); 

	/* We partially or completely failed to write the command */
	if (rv != buf_len)
	{
	    bronx_log("{send_cmd_to_nagios_pipe} Failed to write to Nagios command pipe", BRONX_LOGGING_WARNING);
	    status = CA_ERROR;
	}

	/* Close the stream */
	bronx_safe_fclose(pipe_fp);
    }

    return status;
}

/*
 * my_inet_ntoa()
 * This is a threadsafe version of library function inet_ntoa().
 * It does exactly what inet_ntoa() does, except that it accepts
 * a buffer to populate, instead of returning address of a static.
 * Parameters -
 * struct in_addr in             : The address to be converted
 * char *buf                     : Output buffer
 * int buflen                    : Output buffer len
 */
void
my_inet_ntoa(struct in_addr in, char *buf, int buflen)
{

	register char *p;

	p = (char *)&in;

#define	UC(b)	(((int)b)&0xff)
	(void)snprintf(buf, buflen,
	    "%u.%u.%u.%u", UC(p[0]), UC(p[1]), UC(p[2]), UC(p[3]));

}

/*
 *  cmd_acceptor_thread_exit()
 *  This is the exit function for command acceptor thread.
 *  Parameters -
 *  char *cause_string    : cause of exit
 *  int return_code       : Exit code.
 */
static void
cmd_acceptor_thread_exit(char *cause_string, int return_code)
{
    // Force the count to zero (discard the ca_stopfd, which will be closed by calling code).
    // This is necessary so this count is properly initialized for another round, upon reload. 
    ca_npfds = 0;

    /* CLEAR SENSITIVE INFO FROM MEMORY */

    /* Destroy _configuration->cmd_acceptor_key */
    cmd_acceptor_clear_buffer(_configuration->cmd_acceptor_key, sizeof(_configuration->cmd_acceptor_key));

    /* Disguise decryption method */
    _configuration->cmd_acceptor_encryption_method = -1;

    /* free the poll fd, read hand and write hand arrays. */
    if (ca_pfds) {
	free(ca_pfds);
	ca_pfds = NULL;
    }   
    if (ca_rhand) {
	free(ca_rhand);
	ca_rhand = NULL;
    }   
    if (ca_whand) {
	free(ca_whand);
	ca_whand = NULL;
    }   

    /* Print our final log message */
    bronx_logprintf(BRONX_LOGGING_INFO, "{cmd_acceptor_thread_exit} Terminating Command Acceptor Thread, Cause: %s", cause_string);

    /* and leave */
    apr_thread_exit(_thread_cmd_acceptor, return_code);
}

/*
 *  cmd_acceptor_register_poll()
 *  Register a file descriptor to be polled for an event set.
 *  Add the socket fd to the pool.
 *  Parameters -
 *  short events   : The events to subscribe to
 *  int fd         : Socket fd to be registered.
 *  Return -
 *  CA_OK (0) on success, CA_ERROR (-1) on failure.
 */

static int
cmd_acceptor_register_poll(short events, int fd)
{
    int i;

    /* If it's already in the list, just flag the events and return. */
    for (i = 0; i < ca_npfds; i++) {
	if(ca_pfds[i].fd == fd)
	{
	    ca_pfds[i].events |= events;
	    return CA_OK;
	}
    }

    /* else add it to the list */
    if (ca_maxpfds == 0)
    {
	/* The list is empty. */
	ca_maxpfds++;
	ca_pfds = malloc(sizeof(struct pollfd));
    }
    else if (ca_npfds + 1 > ca_maxpfds)
    {
	/* Add to the existing list */
	ca_maxpfds++;
	ca_pfds = realloc(ca_pfds, sizeof(struct pollfd) * ca_maxpfds);
    }

    if (ca_pfds == NULL) {
	bronx_log("{cmd_acceptor_register_poll} Failed to allocate memory for socket pool", BRONX_LOGGING_ERROR);
	return CA_ABORT;
    }

    /* Record the fd and the events */
    ca_pfds[ca_npfds].fd      = fd;
    ca_pfds[ca_npfds].events  = events;
    ca_pfds[ca_npfds].revents = 0;
    ca_npfds++;
    return CA_OK;
}

/*
 * cmd_acceptor_register_read_handler() 
 * Register a function for handling read operation on a socket fd.
 * Parameters -
 * int fd                   : Socket fd
 * void (*fp)(int, void *)  : handler function
 * void *data               : handler function specific data. 
 *                            In most cases, the crypt instance.
 * return -
 * CA_OK (0) on success, CA_ERROR (-1) on failure. 
 */

static int 
cmd_acceptor_register_read_handler(int fd, void (*fp)(int, void *), void *data)
{
    int rv = CA_OK;
    int i;

    /* Register our interest in this descriptor */
    rv = cmd_acceptor_register_poll(POLLIN, fd);
    if (rv != CA_OK) {
	return rv;
    }

    /* if it's already in the list, just update the handler */
    for(i = 0; i < ca_nrhand; i++) {
	if(ca_rhand[i].fd == fd)
	{
	    ca_rhand[i].handler = fp;
	    ca_rhand[i].data = data;
	    return CA_OK;
	}
    }
    /* else add it to the list */
    if(ca_maxrhand == 0)
    {
	/* The list is empty, create one */
	ca_maxrhand++;
	ca_rhand = malloc(sizeof(struct ca_handler_entry));
    }
    else {
	/* Add to the list */
	if(ca_nrhand + 1 > ca_maxrhand)
	{
	    ca_maxrhand++;
	    ca_rhand = realloc(ca_rhand, sizeof(struct ca_handler_entry) * ca_maxrhand);
	}
    }
    if (ca_rhand == NULL) {
	bronx_log("{cmd_acceptor_register_read_handler} Failed to allocate memory for read handler pool", BRONX_LOGGING_ERROR);
	return CA_ABORT;
    }
    /* record the handler */
    ca_rhand[ca_nrhand].fd = fd;
    ca_rhand[ca_nrhand].handler = fp;
    ca_rhand[ca_nrhand].data = data;
    ca_nrhand++;
    return CA_OK;
}

/*
 * cmd_acceptor_register_write_handler()
 * register a function for handling write operation on a socket fd.
 * Parameters -
 * int fd                   : Socket fd
 * void (*fp)(int, void *)  : handler function
 * void *data               : handler function specific data. 
 *                            In most cases, the crypt instance.
 * return -
 * CA_OK (0) on success, CA_ERROR (-1) on failure. 
 */
static int
cmd_acceptor_register_write_handler(int fd, void (*fp)(int, void *), void *data)
{
    int i;
    int rv = CA_OK;

    /* Register our interest in this descriptor */
    rv = cmd_acceptor_register_poll(POLLOUT, fd);
    if (rv != CA_OK)
    {
	return rv;
    }

    /* if it's already in the list, just update the handler */
    for(i = 0; i < ca_nwhand; i++)
    {
	if(ca_whand[i].fd == fd)
	{
	    ca_whand[i].handler = fp;
	    ca_whand[i].data = data;
	    return CA_OK;
	}
    }

    /* else add it to the list */
    if(ca_maxwhand == 0)
    {
	/* The list is empty, create one */
	ca_maxwhand++;
	ca_whand = malloc(sizeof(struct ca_handler_entry));
    }
    else if(ca_nwhand + 1 > ca_maxwhand)
    {
	ca_maxwhand++;
	ca_whand = realloc(ca_whand, sizeof(struct ca_handler_entry) * ca_maxwhand);
    }
    if (ca_whand == NULL)
    {
	bronx_log("{cmd_acceptor_register_write_handler} Failed to allocate memory for write handler pool", BRONX_LOGGING_ERROR);
	return CA_ABORT;
    }
    /* Add the handler to the list. */
    ca_whand[ca_nwhand].fd = fd;
    ca_whand[ca_nwhand].handler = fp;
    ca_whand[ca_nwhand].data = data;
    ca_nwhand++;
    return CA_OK;
}

/* 
 * cmd_acceptor_find_rhand()
 * find the read handler for the fd passed. 
 * Parameters -
 * int fd         : socket fd to be seached.
 * return -
 * Index for the fd in the read handles pool, if successful.
 * CA_ERROR, on failure.
 */
static int
cmd_acceptor_find_rhand(int fd)
{
    int i;

    for(i = 0; i < ca_nrhand; i++) {
	if(ca_rhand[i].fd == fd) {
	    return i;
	}
    }
    /* We couldn't find the read handler */
    bronx_log("{cmd_acceptor_find_rhand} Read handler stack corrupt", BRONX_LOGGING_ERROR);
    return CA_ERROR;
}

/* 
 * cmd_acceptor_find_whand()
 * find the write handler for the fd passed. 
 * Parameters -
 * int fd         : socket fd to be seached.
 * return -
 * Index for the fd in the write handles pool, if successful.
 * CA_ERROR, on failure.
 */
static int
cmd_acceptor_find_whand(int fd)
{
    int i;

    for(i = 0; i < ca_nwhand; i++) {
	if(ca_whand[i].fd == fd) {
	     return i;
	}
    }
    /* We couldn't find the write handler */
    bronx_log("{cmd_acceptor_find_rhand} Write handler stack corrupt", BRONX_LOGGING_ERROR);
    return CA_ERROR;
}

/*
 * cmd_acceptor_handle_events()
 * Poll on the pool of socket fds.
 * When there is an event on a socket, find the handler for that socket.
 * Then call the handler to process the request.
 * return -
 * CA_OK on success; CA_ERROR on failure. 
 */
static int
cmd_acceptor_handle_events(void)
{
    int i;
    int hand;
    int result;
    void (*handler)(int, void *);
    void *data;
    int rv = CA_OK; 

    // We should never be waiting here only for the ca_stopfd.
    if (ca_npfds <= 1)
    {
	bronx_logprintf (BRONX_LOGGING_ERROR, "{cmd_acceptor_handle_events} error: polling for %s",
	    (ca_npfds <= 0) ? "no descriptors at all" : "nothing beyond the stop descriptor");
	return (ca_npfds <= 0 ? CA_ABORT : CA_ERROR);
    }

    if (! bronx_is_terminating())
    {
	// With a non-zero timeout, this is a blocking call.
	result = poll(ca_pfds, ca_npfds, BRONX_CMD_ACCEPTOR_POLL_TIMEOUT);

	// Understand fully what happened to break us out of the poll.
	if (result < 0)
	{
	    // An error occurred.
	    char errno_string_buf[ERRNO_STRING_BUF_SIZE];
	    bronx_logprintf (BRONX_LOGGING_ERROR, "{cmd_acceptor_handle_events} error: poll() returned errno=%d (%s)",
		errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	}
	else if (result == 0)
	{
	    // The call timed out.
	    bronx_log("{cmd_acceptor_handle_events} poll() timed out.", BRONX_LOGGING_DEBUG);
	}

	// Only run through this loop if something actually happened.
	if (result > 0)
	{
	    // We skip the ca_stopfd, and process any other pending i/o.
	    // At the end of the loop, we'll check the ca_stopfd just so
	    // we can issue a log message if input is pending on it,
	    // but we'll basically depend on the bronx_is_terminating()
	    // flag having been set to avoid an infinite loop here,
	    // rather than trying to read the ca_stopfd to empty the input
	    // (its purpose was just to break us out of the poll() well
	    // before the timeout expired).
	    for (i = 1; i < ca_npfds; i++)
	    {
		if((ca_pfds[i].events & POLLIN) && (ca_pfds[i].revents & (POLLIN|POLLERR|POLLHUP|POLLNVAL)))
		{
		    /* Read event */
		    ca_pfds[i].events &= ~POLLIN;
		    /* Find the read handler for the fd */
		    hand = cmd_acceptor_find_rhand(ca_pfds[i].fd);
		    if (hand == CA_ERROR) {
			/* No handler found. Close the fd and bail. */
			bronx_safe_close(ca_pfds[i].fd);
			rv = CA_ERROR;
			goto end;
		    }
		    handler = ca_rhand[hand].handler;
		    if (handler == NULL) {
			bronx_log("{cmd_acceptor_handle_events} NULL handler found", BRONX_LOGGING_ERROR);
			/* Close the fd before exiting. */
			bronx_safe_close(ca_pfds[i].fd);
			rv = CA_ERROR;
			goto end;
		    } 
		    data = ca_rhand[hand].data;
		    /* Reset the handler. The current handler will set the next handler for this fd. */
		    ca_rhand[hand].handler = NULL;
		    ca_rhand[hand].data = NULL;
		    handler(ca_pfds[i].fd, data);
		}
		if((ca_pfds[i].events & POLLOUT) && (ca_pfds[i].revents & (POLLOUT|POLLERR|POLLHUP|POLLNVAL)))
		{
		    /* Write event */
		    ca_pfds[i].events &= ~POLLOUT;
		    hand = cmd_acceptor_find_whand(ca_pfds[i].fd);
		    if (hand == CA_ERROR) {
			/* No handler found. Close the fd and bail. */
			bronx_safe_close(ca_pfds[i].fd);
			rv = CA_ERROR;
			goto end;
		    }
		    handler = ca_whand[hand].handler;
		    if (handler == NULL) {
			bronx_log("{cmd_acceptor_handle_events} NULL handler found", BRONX_LOGGING_ERROR);
			/* Close the fd before exiting. */
			bronx_safe_close(ca_pfds[i].fd);
			rv = CA_ERROR;
			goto end;
		    } 
		    data = ca_whand[hand].data;
		    /* Reset the handler. The current handler will set the next handler for this fd. */
		    ca_whand[hand].handler = NULL;
		    ca_whand[hand].data = NULL;
		    handler(ca_pfds[i].fd, data);
		}
	    }

	    /* Make sure that all the events are initialized */
	    /*
	     * FIX MINOR:  Note that this construction doesn't handle
	     * the case when the end-of-list element you're copying into
	     * a location earlier in the list itself has events==0.
	     */
	    for (i = 1; i < ca_npfds; i++)
		if (ca_pfds[i].events == 0)
		{
		    ca_npfds--;
		    ca_pfds[i].fd = ca_pfds[ca_npfds].fd;
		    ca_pfds[i].events = ca_pfds[ca_npfds].events;
		}

	    // We test ca_pfds[0] to see if we got any input on the ca_stopfd, only for logging purposes.
	    // Regardless, we don't even bother to read it; we just exit the routine and allow the
	    // bronx_is_terminating() flag to control large-scale looping.
	    if (ca_pfds[0].revents) {
#ifdef INCLUDE_EXTRA_DEBUG_DETAIL
		bronx_logprintf (BRONX_LOGGING_DEBUG, "{cmd_acceptor_handle_events} command acceptor pipe file descriptor %d state is%s%s%s%s",
		    ca_pfds[0].fd,
		    ca_pfds[0].revents & POLLIN   ? " POLLIN"   : "",
		    ca_pfds[0].revents & POLLERR  ? " POLLERR"  : "",
		    ca_pfds[0].revents & POLLHUP  ? " POLLHUP"  : "");
#endif
	    }
	}
    }
end:    
    return rv;
}

/*
 * cmd_acceptor_handle_connection_read()
 * The connection read handler. First Read the size in bytes of the command.
 * Then read bytesize_cmd bytes of the commamd. Check if there is a newline 
 * at the end of the command. Eat up the user information from the command,
 * insert the timestamp and submit to Nagios command pipe. 
 * Parameters -
 * int sock        - Client socket.
 * void *data      - Crypt instance pointer.
 */

static void
cmd_acceptor_handle_connection_read(int sock, void *data)
{
    int bytes_to_recv;
    int bytes_to_recv_orig;
    int rc;
    int nagios_cmd_len;
    time_t recv_time;
    char *nagios_cmd = NULL;
    char *raw_cmd = NULL;
    char *recv_buffer = NULL;
    char *username = NULL;
    char *ptr = NULL;
    struct ca_crypt_instance *CI;

    bronx_log("{cmd_acceptor_handle_connection_read}", BRONX_LOGGING_DEBUG);
    CI = (struct ca_crypt_instance *)data;

    /* Read the command size (with the delimiting semicolon) from the client */
    bytes_to_recv_orig = bytes_to_recv = BYTESIZE_CMD_LEN + 1;

    /* Allocate additional byte for '\0' */
    recv_buffer = (char *)malloc((bytes_to_recv + 1) * sizeof(char));

    if (recv_buffer == NULL) {
	bronx_log("{cmd_acceptor_handle_connection_read} Failed to allocate memory for receive buffer.", BRONX_LOGGING_WARNING);
	/* Cleanup the crypt_instance structure */
	cmd_acceptor_encrypt_cleanup(CI);
	bronx_safe_close(sock);
	goto end;
     }
    /* Try to read bytes_to_recv bytes from the socket */
    rc = cmd_acceptor_recvall(sock, recv_buffer, &bytes_to_recv, CMD_ACCEPTOR_DEFAULT_SOCKET_TIMEOUT);

     /* recv() error or client disconnect. */
    if(rc == CA_ERROR)
    {
	bronx_log("{cmd_acceptor_handle_connection_read} Client Disconnected.", BRONX_LOGGING_DEBUG);
	cmd_acceptor_encrypt_cleanup(CI);
	bronx_safe_close(sock);
	goto end;
    }

    /* Register the read handler again*/
    rc = cmd_acceptor_register_read_handler(sock, cmd_acceptor_handle_connection_read, (void *)CI);
    if(rc != CA_OK)
    {
	bronx_log("{cmd_acceptor_handle_connection_read} Failed to register read handler", BRONX_LOGGING_WARNING);
	cmd_acceptor_encrypt_cleanup(CI);
	bronx_safe_close(sock);
	goto end;
    }

    /* For development debugging of this section of code. */
    if (0) {
	char hex_char[] = {
	    '0', '1', '2', '3', '4', '5', '6', '7',
	    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	int recv_len = bytes_to_recv;
	char *hex_buf = malloc (recv_len * 2 + 1);
	char *hex = hex_buf;
	int i;
	for (i = 0; i < recv_len; ++i) {
	    unsigned char ch = recv_buffer[i];
	    *hex++ = hex_char[(ch >> 4) & 0x0f];
	    *hex++ = hex_char[(ch) & 0x0f];
	}
	*hex = '\0';
	bronx_logprintf(BRONX_LOGGING_DEBUG, "{cmd_acceptor_handle_connection_read} %d ciphertext bytes: %s", recv_len, hex_buf);
	free (hex_buf);
    }

    /* Decrypt the data received */
    rc = cmd_acceptor_decrypt_buffer(recv_buffer, bytes_to_recv, _configuration->cmd_acceptor_encryption_method, CI);
    if(rc == CA_ERROR)
    {
	bronx_log("{cmd_acceptor_handle_connection_read} Failed to decrypt", BRONX_LOGGING_WARNING);
	bronx_safe_close(sock);
	goto end;
    }
    recv_buffer[bytes_to_recv] = '\0';

    /* We couldn't read the correct amount of data, so bail out */
    if(bytes_to_recv != bytes_to_recv_orig)
    {
	/* Don't cleanup the crypt instance here. This handler will be called again to handle the socket close. */  
	bronx_logprintf(BRONX_LOGGING_WARNING, "{cmd_acceptor_handle_connection_read} Data sent from client was too short (%d < %d), aborting...",
		       bytes_to_recv, bytes_to_recv_orig);
	bronx_safe_close(sock);
	goto end;
    }

    /* We received the command size in bytes i.e. the number of bytes to be read for the rest of the command */ 
    bytes_to_recv_orig = bytes_to_recv = atoi(recv_buffer);
    if(bytes_to_recv <= 0) {
	/* Don't clean up the crypt instance here. This handler will be called again to handle the socket close. */  
	bronx_log("{cmd_acceptor_handle_connection_read} Incorrect command size received.", BRONX_LOGGING_WARNING);

	/* For development debugging of this section of code. */
	if (0) {
	    char hex_char[] = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	    };
	    int recv_len = strlen(recv_buffer);
	    char *hex_buf = malloc (recv_len * 2 + 1);
	    char *hex = hex_buf;
	    int i;
	    for (i = 0; i < recv_len; ++i) {
		unsigned char ch = recv_buffer[i];
		*hex++ = hex_char[(ch >> 4) & 0x0f];
		*hex++ = hex_char[(ch) & 0x0f];
	    }
	    *hex = '\0';
	    bronx_logprintf(BRONX_LOGGING_DEBUG, "{cmd_acceptor_handle_connection_read} %d plaintext bytes: %s", recv_len, hex_buf);
	    free (hex_buf);
	}

	bronx_safe_close(sock);
	goto end;
    }

    /* Resize the buffer to receive the rest of the command. */
    recv_buffer = (char *)realloc(recv_buffer, (bytes_to_recv + 1) * sizeof(char));
    if (recv_buffer == NULL) {
	/* Don't cleanup the crypt instance here. This handler will be called again to handle the socket close. */  
	bronx_log("{cmd_acceptor_handle_connection_read} Failed to allocate memory to receive buffer.", BRONX_LOGGING_WARNING);
	bronx_safe_close(sock);
	goto end;
    }
    rc = cmd_acceptor_recvall(sock, recv_buffer, &bytes_to_recv, CMD_ACCEPTOR_DEFAULT_SOCKET_TIMEOUT);

    if(bytes_to_recv != bytes_to_recv_orig)
    {
	/* Don't cleanup the crypt instance here. This handler will be called again to handle the socket close. */  
	bronx_logprintf(BRONX_LOGGING_WARNING, "{cmd_acceptor_handle_connection_read} Data sent from client was too short (%d < %d), aborting...", 
			bytes_to_recv, bytes_to_recv_orig);
	bronx_safe_close(sock);
	goto end;
    }

    /* Record the receipt of the command. */
    recv_time = time(NULL);

    /* Decrypt the data. */
    rc = cmd_acceptor_decrypt_buffer(recv_buffer, bytes_to_recv, _configuration->cmd_acceptor_encryption_method, CI); 
    if(rc == CA_ERROR)
    {
	bronx_log("{cmd_acceptor_handle_connection_read} Failed to decrypt", BRONX_LOGGING_WARNING);
	bronx_safe_close(sock);
	goto end;
    }
    recv_buffer[bytes_to_recv] = '\0';

    /* Check if we received the entire command */
    if (recv_buffer[bytes_to_recv - 1] != '\n') {
	bronx_log("{cmd_acceptor_handle_connection_read} Command parsing error: no newline at the end of received data", BRONX_LOGGING_WARNING);
	bronx_safe_close(sock);
	goto end;
    }
    /* The first field in the command is the user name, eat it up from the buffer. Record it for audit trail. */
    username = recv_buffer;
    ptr = strchr(recv_buffer, ';');  
    if (ptr == NULL) {
	bronx_log("{cmd_acceptor_handle_connection_read} Command parsing error: no semicolon in the received data", BRONX_LOGGING_WARNING);
	bronx_safe_close(sock);
	goto end;
    }
    /* The rest of the command is our raw command */
    raw_cmd = ptr + 1;

    /* Terminate the username */
    *ptr = '\0';

    /* 
     * We need to insert the time stamp at the beginning of the command, followed by a space.
     * Allocate enough space. Keep an additional byte for the null character.
     */ 
    nagios_cmd_len = strlen(raw_cmd) + MAX_TIMESTAMP_LEN + strlen(" ") + 1; 
    nagios_cmd = (char *)malloc(nagios_cmd_len * sizeof(char));
    if (nagios_cmd == NULL) {
	bronx_log("{cmd_acceptor_handle_connection_read} Failed to allocate memory for command buffer", BRONX_LOGGING_WARNING);
	bronx_safe_close(sock);
	goto end;
    }
    /* Construct the final Nagios command */
    snprintf(nagios_cmd, nagios_cmd_len, "[%lu] %s", recv_time, raw_cmd);
    bronx_logprintf(BRONX_LOGGING_COMMANDS, "{cmd_acceptor_handle_connection_read} Submitting command to Nagios: %s", nagios_cmd);

    /*
     * Smash the newline at the end of the raw command string.
     * We use it for audit trail. We don't want unnecessary newlines there.
     */
    if(raw_cmd[strlen(raw_cmd) - 1] == '\n') {
	raw_cmd[strlen(raw_cmd) - 1] = '\0';
    }

    if (bronx_is_paused()) {
	bronx_log("{cmd_acceptor_handle_connection_read} BRONX PAUSED: Discarding command", BRONX_LOGGING_WARNING);
    }
    else {
	/* Record in the audit log that we are submitting the command */
	audit_log(raw_cmd, NAGIOS_CMD_STATUS_INPROGRESS, username, recv_time);

	rc = write_cmd_to_nagios_pipe(nagios_cmd);
	if (rc == CA_ERROR) {
	    bronx_log("{cmd_acceptor_handle_connection_read} Failed to submit the command to Nagios command pipe", BRONX_LOGGING_WARNING);
	    bronx_safe_close(sock);
	    goto end;
	}
	bronx_logprintf(BRONX_LOGGING_COMMANDS, "{cmd_acceptor_handle_connection_read} Submitted to Nagios: %s", raw_cmd);

	/* Record in the audit log that we submitted the command */
	audit_log(raw_cmd, NAGIOS_CMD_STATUS_COMPLETE, username, recv_time);
    }

end:
    if (nagios_cmd) {
	free(nagios_cmd);
    }
    if (recv_buffer) {
	free(recv_buffer);
    }
    return;
}

/*
 * cmd_acceptor_handle_connection()
 * Initialize the encryption structure. Send the IV to the client.
 * Then register the read handler for the socket.
 * Parameters -
 * int sock       - Client socket
 * void *data     - Not used. 
 */

static void
cmd_acceptor_handle_connection(int sock, void *data)
{
    bronx_log("{cmd_acceptor_handle_connection}", BRONX_LOGGING_DEBUG);

    int bytes_to_send;
    int rc;
    struct ca_crypt_instance *CI;

    /* Initialize encryption/decryption structure. */
    if(cmd_acceptor_encrypt_init(_configuration->cmd_acceptor_key, _configuration->cmd_acceptor_encryption_method, &CI) != CA_OK)
    {
	bronx_safe_close(sock);
	return;
    }

    /*
     * If we are using an encrypted channel, send the IV to the client.
     * If we are talking plaintext, dont send an IV. Directly go about 
     * reading the commands. 
     */
    if (_configuration->cmd_acceptor_encryption_method != ENCRYPT_NONE)
    {
	bytes_to_send = CI->iv_size;
	rc = cmd_acceptor_sendall(sock, CI->IV, &bytes_to_send);
	if(rc == CA_ERROR)
	{
	    /* There was an error sending the packet */
	    bronx_log("{cmd_acceptor_handle_connection} Could not send the IV to client", BRONX_LOGGING_WARNING);
	    cmd_acceptor_encrypt_cleanup(CI);
	    bronx_safe_close(sock);
	    return;
	}
	else
	{
	    if(bytes_to_send < CI->iv_size)
	    {
		/* For some reason we didn't send all the bytes we were supposed to */
		bronx_logprintf(BRONX_LOGGING_WARNING, "{cmd_acceptor_handle_connection} Only able to send %d of %d bytes of init packet to client"
				, bytes_to_send, CI->iv_size);
		cmd_acceptor_encrypt_cleanup(CI);
		bronx_safe_close(sock);
		return;
	    }
	}
    }
    /* Register the read handler */
    rc = cmd_acceptor_register_read_handler(sock, cmd_acceptor_handle_connection_read, (void *)CI);
    if(rc != CA_OK)
    {
	bronx_log("{cmd_acceptor_handle_connection} Failed to register read handler", BRONX_LOGGING_WARNING);
	cmd_acceptor_encrypt_cleanup(CI);
	bronx_safe_close(sock);
	return;
    }
    return;
}

/*
 * cmd_acceptor_accept_connection()
 * The accept connection handler.
 * Accept the connection and register write handler for it.
 * Parameters -
 * int sock        - The socket fd.
 * void *unused    - Not used. 
 */
static void
cmd_acceptor_accept_connection(int sock, void *unused)
{
    int new_sd = -1;
    int rc;
    int i;
    struct sockaddr addr;
    struct sockaddr_in *nptr;
    socklen_t addrlen;
    char *ah;
    char *wc;
    char *ip_addr = NULL;
    int flags;
    char errno_string_buf[ERRNO_STRING_BUF_SIZE];

    bronx_log("{cmd_acceptor_accept_connection}", BRONX_LOGGING_DEBUG);
    /* Reregister the accept handler, cmd_acceptor_handle_events() would have set it to NULL */ 
    rc = cmd_acceptor_register_read_handler(sock, cmd_acceptor_accept_connection, NULL);
    if (rc != CA_OK)
    {
	/* Close the socket and leave. */
	bronx_safe_close(sock);
	cmd_acceptor_thread_exit("(cmd_acceptor_manage_connections) Failed to register accept handler", STATE_CRITICAL);
	return;
    }

    /* Wait for a connection request */
    while(! bronx_is_terminating())
    {
	if((new_sd = bronx_safe_accept(sock,0,0)) >= 0)
	    break;
	/* Continue if we got ECONNABORTED - one client failed; wait for others. */
	else if (errno != ECONNABORTED)
	    break;
    }
    if (bronx_is_terminating())
    {
	/* We have been signaled to terminate. */
	goto end;
    }
    if(new_sd < 0)
    {
	/* Accept() failed. Bail. */
	bronx_logprintf(BRONX_LOGGING_ERROR,
	    "{cmd_acceptor_accept_connection} Network server accept failure, errno=%d (%s)",
	    errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	bronx_safe_close(sock);
	goto end;
    }

    /* Find out who just connected... */
    addrlen = sizeof(addr);
    rc = getpeername(new_sd, &addr, &addrlen);

    if(rc < 0)
    {
	/* Terminate the current client connection. */
	bronx_logprintf(BRONX_LOGGING_WARNING,
	    "{cmd_acceptor_accept_connection} Error: Network server getpeername() failure, errno=%d (%s)",
	    errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	bronx_safe_close(new_sd);
	goto end;
    }

    /*
     * Check if we like our peer. If there is a list of allowed hosts in the config,
     * check if the peer is there in the list. Else allow the host.
     */
    ip_addr = (char *)malloc(IP_ADDR_LEN * sizeof(char));
    if(ip_addr == NULL)
    {
	bronx_log("{cmd_acceptor_accept_connection} Failed to allocate buffer", BRONX_LOGGING_WARNING);
	bronx_safe_close(new_sd);
	goto end;
    }
    nptr = (struct sockaddr_in *)&addr;
    
    /* Call our version of inet_ntoa to get the IP quad */
    my_inet_ntoa(nptr->sin_addr, ip_addr, IP_ADDR_LEN);
    ip_addr[IP_ADDR_LEN - 1] = '\0';
 
    if (_configuration->cmd_acceptor_num_allowed_hosts != 0)
    {
	for(i = 0; i < _configuration->cmd_acceptor_num_allowed_hosts; i++)
	{
	    /* Run through the list of allowed hosts */
	    ah = _configuration->cmd_acceptor_allowed_hosts[i];
	    wc = strchr(ah, '*');

	    if (wc == NULL)
	    {
		/* There is no wild card in the list. Just compare the hostnames. */
		if (!strcmp(ip_addr, ah))
		    break;
	    }
	    else {
		/* The list has a wildcard. Compare the part before the wild card. */
		if (!strncmp(ip_addr, ah, (wc-ah)))
		    break;
	    }
	}
	/* We reached the end; we don't like the peer. */
	if (i == _configuration->cmd_acceptor_num_allowed_hosts)
	{
	    bronx_logprintf(BRONX_LOGGING_WARNING,
		"{cmd_acceptor_accept_connection} CONNECTION FROM %s NOT ON ALLOWED HOST LIST, NOT ACCEPTING CONNECTION",
		ip_addr);
	    bronx_safe_close(new_sd);
	    goto end;
	}
    }

    bronx_logprintf(BRONX_LOGGING_DEBUG, "{cmd_acceptor_accept_connection} Connection accepted from %s", ip_addr);
    /*
     * Make the socket non-blocking. 
     * Don't let one client block us.
     */
    if ((flags = fcntl (new_sd, F_GETFL, 0)) < 0 ||
	fcntl (new_sd, F_SETFL, flags | O_NONBLOCK) < 0)
	{
	char errno_string_buf[ERRNO_STRING_BUF_SIZE];

	bronx_logprintf (BRONX_LOGGING_ERROR,
	    "{cmd_acceptor_accept_connection} cannot set file descriptor %d non-blocking, errno=%d (%s)",
	    new_sd, errno, bronx_strerror_r (errno, errno_string_buf, sizeof (errno_string_buf)));
	exit (EXIT_FAILURE);
	}

    /*
     * Let the write handler carry on from here.
     * We will be the first one to write on the socket.
     * We will send the IV that the client will use for encryption.   
     */
    rc = cmd_acceptor_register_write_handler(new_sd, cmd_acceptor_handle_connection, NULL);
    if (rc != CA_OK) {
	bronx_log("{cmd_acceptor_accept_connection} Failed to register write handler", BRONX_LOGGING_ERROR);
	bronx_safe_close(new_sd);
	goto end;
    }

end:
    if(ip_addr != NULL) {
	free(ip_addr);
    }
    return;
}

/*
 * cmd_acceptor_manage_connections()
 * Create the socket and bind to it.
 * Then start listening for incoming connections.
 * Call cmd_acceptor_handle events(), to start polling for events on the sockets.
 */
static int
cmd_acceptor_manage_connections()
{
    int sock = 0;
    int reuse_sock;
    int rv;
    struct sockaddr_in myname;

    bronx_log("{cmd_acceptor_manage_connections}", BRONX_LOGGING_DEBUG);

    /* Create a socket TCP for listening */
    sock = bronx_safe_socket(AF_INET, SOCK_STREAM, 0);

    /* Exit if we couldn't create the socket */
    if(sock < 0)
    {
	cmd_acceptor_thread_exit("(cmd_acceptor_manage_connections) Network server socket failure", STATE_CRITICAL);
    }
    bronx_logprintf(BRONX_LOGGING_INFO, "{cmd_acceptor_manage_connections} Established local socket (%d) for listening.", sock);

    /* Set the reuse address flag so we don't get errors when restarting. */
    reuse_sock = 1;
    if(setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &reuse_sock, sizeof(reuse_sock)) < 0)
    {
	bronx_safe_close(sock);
	cmd_acceptor_thread_exit("(cmd_acceptor_manage_connections) Could not set reuse address option on socket!", STATE_CRITICAL);
    }

    myname.sin_family = AF_INET;
    myname.sin_port = htons(_configuration->cmd_acceptor_port);
    bzero(&myname.sin_zero, sizeof(myname.sin_zero));
    myname.sin_addr.s_addr = INADDR_ANY;

    /* Bind the address to the Internet socket */
    rv = bind(sock, (struct sockaddr *)&myname, sizeof(myname));
    if(rv < 0)
    {
	/* Close the socket and bail */
	bronx_safe_close(sock); 
	bronx_logprintf(BRONX_LOGGING_ERROR, "{cmd_acceptor_manage_connections} Network server bind failure, errno=%d", errno);
	cmd_acceptor_thread_exit("(cmd_acceptor_manage_connections) Network server bind failure", STATE_CRITICAL);
    }
    bronx_log("{cmd_acceptor_manage_connections} Socket Bound.", BRONX_LOGGING_DEBUG);

     /* Start listening on the socket. */ 
    if(listen(sock, _configuration->cmd_acceptor_max_conn) < 0)
    {
	/* Close the socket and bail */
	bronx_safe_close(sock); 
	cmd_acceptor_thread_exit("(cmd_acceptor_manage_connections) Network server listen failure", STATE_CRITICAL);
    }
    bronx_logprintf(BRONX_LOGGING_INFO, "{cmd_acceptor_manage_connections} Socket open for listening on port %d.", ntohs(myname.sin_port));

    /* Register the read handler. The handler will accept the connection to begin with. */
    rv = cmd_acceptor_register_read_handler(sock, cmd_acceptor_accept_connection, NULL);
    if (rv != CA_OK)
    {
	bronx_log("{cmd_acceptor_manage_connections} Failed to register read handler", BRONX_LOGGING_ERROR);
	bronx_safe_close(sock);
	cmd_acceptor_thread_exit("(cmd_acceptor_manage_connections) Failed to register handler.", STATE_CRITICAL);
    }
    bronx_log("{cmd_acceptor_manage_connections} Registered read handler.", BRONX_LOGGING_DEBUG);

    /*  Enter our event handling loop. */

    bronx_log("{cmd_acceptor_manage_connections} Entering command acceptor main event wait loop.", BRONX_LOGGING_DEBUG);

    /* Unless Bronx is terminating or we ran into an error, this loop goes on forever ... */ 
    int event_status = CA_OK;
    while (! bronx_is_terminating())
    {
	/* Check for new events, and handle them. */
	event_status = cmd_acceptor_handle_events();
	if (event_status != CA_OK)
	{
	    bronx_log("{cmd_acceptor_manage_connections} Forcing command acceptor event loop restart.", BRONX_LOGGING_WARNING);
	    break;
	}
    }

    /* Close the socket whether or not there is an error. */
    bronx_safe_close(sock);

    if (event_status != CA_OK)
    {
	/* The specific details of the error or abort are logged by the related functions. */
	cmd_acceptor_thread_exit("(cmd_acceptor_manage_connections) Internal error", STATE_CRITICAL);
    }

    bronx_log("{cmd_acceptor_manage_connections} Exiting connection handler.", BRONX_LOGGING_INFO);
    return event_status;
}

/*
 *  thread_cmd_acceptor()
 *  This is the entry function for command acceptor thread.
 *  Parameters -
 *  apr_thread_t *thread : APR thread id.
 *  void *data           : Pointer to integer containing a file descriptor for use in stopping poll().
 */
void* APR_THREAD_FUNC thread_cmd_acceptor(apr_thread_t *thread, void *data)
{
    bronx_log("{thread_cmd_acceptor} Command Acceptor Thread Started. ", BRONX_LOGGING_INFO);
    ca_stopfd = *(int *) data;
    int outcome = cmd_acceptor_register_poll(POLLIN, ca_stopfd);  // ca_stopfd will always be ca_pfds[0].
    if (outcome == CA_OK)
    {
	/* Until we are signaled to terminate... */
	while (! bronx_is_terminating())
	{
	    /* Wait for connections */
	    outcome = cmd_acceptor_manage_connections();
	    if (outcome == CA_ABORT)
	    {
		bronx_log("{thread_cmd_acceptor} Bronx internal error detected.", BRONX_LOGGING_ERROR);
		break;
	    }
	}
    }

    bronx_log("{thread_cmd_acceptor} Bronx Terminating, Exiting Command Acceptor Thread.", BRONX_LOGGING_INFO);
    if (outcome == CA_ABORT) {
	cmd_acceptor_thread_exit("Exiting Command Acceptor Abnormally.", STATE_CRITICAL);
    }
    else {
	cmd_acceptor_thread_exit("Exiting Command Acceptor Normally.", STATE_OK);
    }
    return(NULL);
}
