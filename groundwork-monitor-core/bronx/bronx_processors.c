/*
 *  bronx_processors.c -- Manages different kinds of Nagios data.
 *
 *  Copyright (C) 2007-2017 Groundwork Open Source
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
 *	2007-09-17 DEF;	Created.
 *	2012-05-14 GH;	Initial improvements to start/stop behavior.
 *	2012-06-22 GH;	Improve comments.
 *			Normalized indentation whitespace.
 *	2017-01-31 GH;	Updated to support Nagios 4.2.4.
 */

#include "bronx.h"
#include "bronx_neb.h"
#include "bronx_log.h"
#include "bronx_config.h"
#include "bronx_processors.h"
#include "bronx_safe_fork.h"
#include "bronx_thread.h"
#include "bronx_utils.h"

/*
 *  Globals
 */

apr_pool_t	*_processors_pool;
apr_hash_t	*_processors_host_last_state_table;
apr_hash_t	*_processors_hostservice_last_state_table;

/*
 * Functionality.
 */

apr_status_t processors_init()
{
    apr_status_t	rv;
    if ((rv = apr_pool_create(&_processors_pool, NULL)) == APR_SUCCESS)
    {
	_processors_host_last_state_table = apr_hash_make(_processors_pool);
	_processors_hostservice_last_state_table = apr_hash_make(_processors_pool);
    }
    else
    bronx_log("{processors_init} Failed to create memory pool for processors.", BRONX_LOGGING_ERROR);

    return(rv);
}

void processors_uninit()
{
    apr_pool_destroy(_processors_pool);
}

/*
 *  NEB Processors.
 *
 *  This is the only Bronx callback we register with NEB.  It receives notification
 *  of significant events within Nagios that we might desire to affect operation of
 *  this Event Broker.
 */
int processProcessData(int cmd, void *data)
{
    nebstruct_process_data	*process_data;

    process_data = (nebstruct_process_data *)data;

    if (process_data->type == NEBTYPE_PROCESS_EVENTLOOPSTART)
    {
	bronx_logprintf(BRONX_LOGGING_INFO, "{processProcessData} Received Event Loop Start Indicator, Initializing Threads and Routes");

	bronx_init_safe_operations();

	// This is where we start the Bronx threads for handling incoming
	// socket connections and processing data from them.  In that sense,
	// having the processing of NEBTYPE_PROCESS_EVENTLOOPEND below take the
	// inverse action of stopping those threads makes the right symmetry.
	//
	// A failure here will be reported to the caller and perhaps be logged
	// there, but in the Nagios code it won't actually stop Nagios from
	// proceeding with normal operation despite the problems this one event
	// broker is having.
	//
	if (! assert_threads_running(_configuration)) {
	    return(NEB_ERROR);
	}
    }
    else if (process_data->type == NEBTYPE_PROCESS_EVENTLOOPEND)
    {
	bronx_logprintf(BRONX_LOGGING_INFO, "{processProcessData} Received Event Loop End Indicator, Terminating Threads and Routes");

#ifdef NAGIOS_4_2_4_OR_LATER
	// Here we want to shut the thing down gracefully, using certain parts of
	// the code that is currently executed in nebmodule_deinit() as a kind of
	// "official" final backstop to bring down the Bronx threads:
	//
	//     set_bronx_terminating (TRUE);
	//     threads_stop(_configuration);
	//
	// The deinit will happen through a separate call to the nebmodule_deinit()
	// routine a bit later, and we don't want to interfere now with its operation
	// then (there might possibly be some other code path bypassing this code
	// here and still requiring those calls there, so we cannot delete them
	// there and only execute them here).  However, we do want to tell Bronx to
	// stop operating, in a safe but firm manner that won't cause any problems
	// with a hard stop later on.  We want it to stop as quickly as possible,
	// synchronously, so when we call save_queued_check_results() below, we will
	// be guaranteed that there wouldn't be any incoming data stragglers posted
	// back to Nagios after that call.
	//
	// A key consideration here is that we CANNOT stop a Bronx thread cold while
	// it is in the middle of a critical region (e.g., while it holds a mutex
	// for storing results into the check result list shared between Bronx and
	// Nagios).  If that were to happen, save_queued_check_results() would never
	// be able to lock that mutex later on for its own use, and we would have a
	// deadlock.  Fortunately, our mechanism for stopping threads is cooperative,
	// so we know that the Bronx threads won't have been killed in the middle of
	// a critical region when we get back here.
	//
	// Bear in mind the complete lifecycle of the nagios process:  it can be
	// restarted without being totally shut down.  So both initialization and
	// termination routines must cope with proper resource allocation and
	// deallocation in such a context.
	//
	// FIX LATER:  We haven't traced the execution of a full nagios-process
	// restart, to ensure that all the actions relative to Bronx starting up are
	// idempotent and would execute properly in that context.  That has not been
	// a problem in practice to date, simply because we never restart Nagios.
	// Instead, in the GroundWork context, we always just stop and start it,
	// completely.
	//
	// Fortuunately for us, the following two calls turn out to be idempotent, so
	// there's no danger in having them take effect now and possibly interfering
	// with the same calls later on.  We don't also do this for earlier Nagios
	// releases primarily for historical reasons, because we didn't do it when we
	// first built Bronx for those releases.
	//
	set_bronx_terminating (TRUE);
	threads_stop(_configuration);

	// At least for Nagios 4.2.4 and later, we call a routine that will save
	// into an external file the full content of the check_result_list (or
	// check_result_list_head and check_result_list_tail) data structure shared
	// between Nagios and Bronx, so the data which has already come in through
	// Bronx does not get lost and will be processed once Nagios starts up again.
	// This is best done after the Bronx threads are shut down, so they don't
	// continue to pump check results into that data structure after we empty
	// it.  An external check-result file can contain more than one result, which
	// makes saving of the current in-memory results a bit easier to manage.
	//
	// We ought to ensure that none of the code that saves check results into a
	// file calls into any section of code that Nagios believes should no longer
	// be executing at this point.  That is to say, when we get here, we're no
	// longer within the bounds of the event_execution_loop() call within main(),
	// and we should not assume any context that might only be valid within those
	// bounds.
	//
	// Alternatively, we could have forced the queue to be drained by reaping and
	// fully processing all the queued data until the queue is fully empty.  But
	// that approach is perhaps more likely to invoke context which is no longer
	// in bounds outside of the event_execution_loop().
	//
	// Note that in Nagios (4.2.4, at least), a NEBTYPE_PROCESS_EVENTLOOPEND
	// event sent to the event broker will be followed immediately by either
	// a NEBTYPE_PROCESS_SHUTDOWN event or a NEBTYPE_PROCESS_RESTART event if
	// those situations are coming true.  However, it's probably best not to
	// try to distinguish here between those future states.  Certainly if we're
	// doing a full shutdown, all the in-memory data should be saved.  But even
	// if we're only doing a restart, possibly the queue might end up getting
	// re-initialized before we're fully up and running again, so we can't
	// necessarily depend on all the in-memory data persisting across a restart.
	//
	// For that matter, now that Nagios 4.2.4 has abdicated any role in maintaining
	// that check-result data structure, managing it is all up to us.  And we might
	// want to initialize it dynamically on program startup or restart, not just
	// statically.
	//
	// Finally, if we want this to work, it needs to be given time to work.  That
	// is, our Nagios startup/shutdown script cannot get nervous too quickly if
	// the nagios process is taking awhile to shut down.  It must wait a reasonable
	// amount of time while sending SIGTERM signals before it finally gives up and
	// sends a SIGKILL signal.
	//
	save_queued_check_results();
#endif
    }
    // We may also see process_data->type == NEBTYPE_PROCESS_SHUTDOWN and
    // process_data->type == NEBTYPE_PROCESS_RESTART callbacks, and perhaps
    // others.  At the moment, we don't have any special actions we wish to
    // take for such occurrences.

    return(NEB_OK);
}
