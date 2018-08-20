/*
 *  bronx_thread.c -- Thread functionality.
 *
 *  Copyright (C) 2007-2012 Groundwork Open Source
 *  Originally written by Daniel Emmanuel Feinsmith
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
 *  Change Log:
 *	2007-09-17 DEF;	File created.
 *	2012-05-14 GH;	Support anonymous pipes to aid in fast thread shutdown.
 *	2012-06-22 GH;	Normalized indentation whitespace.
 */

#include "bronx.h"
#include "bronx_config.h"
#include "bronx_cmd_acceptor.h"
#include "bronx_listener.h"
#include "bronx_log.h"
#include "bronx_thread.h"

/*
 *  Globals.
 */

int             _threads_running               = FALSE;  // Global variable related to thread functioning
static int      _thread_listener_running       = FALSE;
static int      _thread_cmd_acceptor_running   = FALSE;
static int      _thread_listener_pipefd[2]     = { -1, -1 };
static int      _thread_cmd_acceptor_pipefd[2] = { -1, -1 };
apr_thread_t    *_thread_listener;
apr_thread_t    *_thread_cmd_acceptor;
apr_pool_t      *_thread_pool;

/*
 *  Initialize thread system.
 */

int threads_running()
{
    return(_threads_running);
}

apr_status_t threads_init()
{
    _threads_running = FALSE;
    _thread_listener_running = FALSE;
    _thread_cmd_acceptor_running = FALSE;
    return(apr_pool_create(&_thread_pool, NULL));
}

void threads_uninit()
{
    apr_pool_destroy(_thread_pool);
}

/*
 *  Start all relevant threads.
 */

int threads_start(configuration_criteria *config)
{
    bronx_log("{threads_start} Starting Threads.", BRONX_LOGGING_DEBUG);

    if (!_threads_running)
    {
	apr_status_t result;

	/*
	 *  If the user has specified a listener,
	 *  then create the listener thread.
	 *
	 *  TODO: In the future, we need the capacity to create several
	 *          listeners, potentially of different kinds. Right now,
	 *          the listener is a nsca-type listener. We will certainly
	 *          need other kinds of listeners as time goes on, whether
	 *          they be tap-in, perhaps nscafe, and certainly
	 *          bronx. Not to mention a listener to handle things
	 *          like administrative commands, etc...
	 */

	if (config->listener && !_thread_listener_running)
	{
	    if (pipe(_thread_listener_pipefd))
	    {
		bronx_log("{threads_start} FAILED to create pipe for main listener thread.", BRONX_LOGGING_ERROR);
		return(FALSE);
	    }
	    else
	    {
		result = apr_thread_create(&_thread_listener, NULL, thread_listener, &_thread_listener_pipefd[0], _thread_pool);
		if(result == APR_SUCCESS)
		{
		    _thread_listener_running = TRUE;
		    bronx_log("{threads_start} Created main listener thread.", BRONX_LOGGING_INFO);
		}
		else
		{
		    bronx_log("{threads_start} FAILED to create main listener thread.", BRONX_LOGGING_ERROR);
		    return(FALSE);
		}
	    }
	}

	if (!_thread_cmd_acceptor_running)
	{
	    /*
	     * Create the command acceptor thread which receives the Nagios commands.
	     * The thread writes the commands to the Nagios command pipe. 
	     * FIX MINOR:  That seems rather ridiculous, to have the command in hand
	     * and then stuff it into an external IPC mechanism.  Can't we instead
	     * stuff it directly into the appropriate Nagios internal data structure?
	     */ 
	    if (pipe(_thread_cmd_acceptor_pipefd))
	    {
		bronx_log("{threads_start} FAILED to create pipe for command acceptor thread.", BRONX_LOGGING_ERROR);
		return(FALSE);
	    }
	    else
	    {
		result = apr_thread_create(&_thread_cmd_acceptor, NULL, thread_cmd_acceptor, &_thread_cmd_acceptor_pipefd[0], _thread_pool);
		if(result == APR_SUCCESS)
		{
		    _thread_cmd_acceptor_running = TRUE;
		    bronx_log("{threads_start} Created command acceptor thread.", BRONX_LOGGING_INFO);
		}
		else
		{
		    bronx_log("{threads_start} FAILED to create command acceptor thread.", BRONX_LOGGING_ERROR);
		    return(FALSE);
		}
	    }
	}

	_threads_running = (!config->listener || _thread_listener_running) && _thread_cmd_acceptor_running;
	bronx_logprintf(BRONX_LOGGING_DEBUG, "{threads_start} Threads Initialization %s.", _threads_running ? "Completed" : "Failed");
    }
    return(_threads_running);
}

/*
 *  Stop all relevant threads.  We do so by first telling each running thread
 *  that it should stop processing and exit, then waiting for the target thread
 *  to join up with the current thread.  (The target thread might even have
 *  already noticed that we have previously set a global termination flag and
 *  exited, but we cannot count on that.)  Telling a target thread to stop
 *  processing is done via IPC, to immediately break out of any poll() call in
 *  which a thread might be currently blocked.  We just write one null byte to
 *  the thread's open pipe, so the poll() will see that i/o on the pipe is ready
 *  and break out of the blocked i/o.  The thread code can then see that thread
 *  termination was requested, and it should (must) exit the thread.
 */

int threads_stop(configuration_criteria *config)
{
    if (_threads_running)
    {
	apr_status_t    retval;

	/* Wait for the listener thread to terminate */
	if (_thread_listener_running)
	{
	    // If this pipe write fails, we continue on anyway, because it is
	    // only here to cause the thread's internal poll() call to abort
	    // earlier than it would anyway with the timeout it already uses.
	    if (write(_thread_listener_pipefd[1], "", 1) != 1)
		bronx_log("{threads_signal} FAILED to signal listener thread.", BRONX_LOGGING_ERROR);

	    // This is a blocking call.
	    apr_thread_join(&retval, _thread_listener);
	    close(_thread_listener_pipefd[0]);
	    close(_thread_listener_pipefd[1]);
	    _thread_listener_running = FALSE;
	}

	/* Wait for the command acceptor thread to terminate */
	if (_thread_cmd_acceptor_running)
	{
	    // If this pipe write fails, we continue on anyway, because it is
	    // only here to cause the thread's internal poll() call to abort
	    // earlier than it would anyway with the timeout it already uses.
	    if (write(_thread_cmd_acceptor_pipefd[1], "", 1) != 1)
		bronx_log("{threads_signal} FAILED to signal command acceptor thread.", BRONX_LOGGING_ERROR);

	    // This is a blocking call.
	    apr_thread_join(&retval, _thread_cmd_acceptor);
	    close(_thread_cmd_acceptor_pipefd[0]);
	    close(_thread_cmd_acceptor_pipefd[1]);
	    _thread_cmd_acceptor_running = FALSE;
	}

	_threads_running = (!config->listener || _thread_listener_running) && _thread_cmd_acceptor_running;
    }
    return(1);
}

/*
 *  Make sure the thread system is running.
 */

int assert_threads_running(configuration_criteria *config)
{
    if (!_threads_running)
	threads_start(config);
    return (_threads_running);
}
