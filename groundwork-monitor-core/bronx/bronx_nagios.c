/*
 *  bronx_nagios.c -- Functions for interfacing with Nagios.
 *
 *  Copyright (c) 2007-2017 Groundwork Open Source
 *  Originally written by Daniel Emmanuel Feinsmith
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
 *	2007-09-17 DEF; Created.
 *	2013-03-19 GH;  Ported to Nagios >= 3.4.4.
 *	2017-01-31 GH;	Updated to support Nagios 4.2.4.
 */

#include "bronx.h"
#include "bronx_log.h"
#include "bronx_nagios.h"
#include "bronx_utils.h"

/*
 *  Nagios Externals.  Too bad they're not already declared in some Nagios header file,
 *  which is really where all these declarations belong.
 */

#ifndef NAGIOS_4_2_4_OR_LATER
extern circular_buffer external_command_buffer;
extern int             external_command_buffer_slots;
extern circular_buffer service_result_buffer;
extern int             check_result_buffer_slots;
#if NAGIOS_3_4_4_OR_LATER
extern check_result    *check_result_list;
#endif
#endif

#ifdef NAGIOS_4_2_4_OR_LATER
const char *bronx_source_name(const void *);

struct check_engine bronx_engine = {
    "Bronx NSCA port",
    bronx_source_name,
    NULL
};
#endif

/*
 *  Functionality.
 */

#ifdef NAGIOS_4_2_4_OR_LATER

// This routine is passed the "source" pointer from a check_result
// and must return a non-free()-able string useful for printing what
// we need to determine exactly where the check was received from.
//
const char *bronx_source_name(const void *source)
{
    return source ? (const char *) source : "external sensor";
}

#endif

int submit_check_result_to_nagios(char *host_name, char *svc_description, int return_code, char *plugin_output, time_t check_time)
{
    check_result *chk_result;
    chk_result = (check_result *)malloc(sizeof(check_result));
    /* Set the default values in the check result structure. */
    init_check_result(chk_result);

    /*
     * Set up the check result structure with information that we were passed.
     * Nagios normally reads the check results from a diskfile specified in
     * output_file member.  But since we can directly access nagios result
     * list, we bypass the diskfile creation.  We set output_file to NULL and
     * the fd to -1, hoping that Nagios will have a NULL check.
     *
     * Note that this bypassing of diskfile storage means much better performance,
     * at the cost of not retaining pending check results across a Nagios restart.
     * Possibly in some future version, we can make this a configurable option, to
     * support customers who never want to risk losing a single bit of monitoring
     * data.
     */
    chk_result->output_file = NULL;
#ifndef NAGIOS_4_2_4_OR_LATER
    // At least in Nagios 3.5.1, this assignment is actually redundant, having been set already
    // by init_check_result().  But we make it anyway, for possible backward compatibility.
    // In Nagios 4.2.4, this field doesn't exist any more.
    chk_result->output_file_fd = -1;
#endif
    chk_result->host_name = strdup(host_name);
    if (svc_description[0])
    {
	chk_result->service_description = strdup(svc_description);
	chk_result->object_check_type = SERVICE_CHECK;
	chk_result->check_type = SERVICE_CHECK_PASSIVE;
    }
    else
    {
	chk_result->object_check_type = HOST_CHECK;   // This is the default, but let's set it anyway.
	chk_result->check_type = HOST_CHECK_PASSIVE;
    }
    normalize_plugin_output(plugin_output, "B2");
    chk_result->output = strdup(plugin_output);

    chk_result->return_code = return_code;
    chk_result->exited_ok = TRUE;  // Maybe not really true, but we have no way to know that.

    chk_result->start_time.tv_sec = check_time;
    chk_result->start_time.tv_usec = 0;
    chk_result->finish_time = chk_result->start_time;

#ifdef NAGIOS_4_2_4_OR_LATER
    // See getrusage() for the members of a "struct rusage".
    //
    // We have no direct knowledge of the resource usage by this check, so all we can
    // really do is zero out the structure that purports to contain such data.  (It's
    // not actually clear whether Nagios makes use of such data even if it is provided).
    //
    memset(&chk_result->rusage, 0, sizeof(chk_result->rusage));

    // This field defaults to NULL in init_check_result().  Let's provide something more interesting.
    //
    chk_result->engine = &bronx_engine;

    // This field defaults to NULL in init_check_result().
    //
    // It's okay to live with that.  Perhaps at some point in the future, we might want to
    // provide more detail, perhaps assigning it either host_name or strdup(host_name) or
    // somesuch, depending on the regime for cleaning up the memory used by this field.
    // However, though this data is collected by Nagios, I have yet to see that it is ever
    // used.  So there's no point in worrying about this for now.
    //
    // chk_result->source = NULL;

    // We don't bother to initialize these pointers to NULL because their initial values
    // will be set when we add the check_result to the list in Nagios.  The value of the
    // ->next pointer will be set regardless of what routine we call to add the result to
    // Nagios; the ->prev pointer will only be available, and its value will be set, if
    // we add the result to a double-linked list in Nagios.
    //
    // chk_result->next = NULL;
    // chk_result->prev = NULL;
#endif

    /*
    ** Note:  A future version might populate a non-default value for chk_result->latency
    ** so any calculations that want to use such a value would actually be accurate.
    ** (Where we would obtain the proper value to use, I don't know yet.)  In the meantime,
    ** we are just using the 0.0 value set by the preceding call to init_check_result().
    */

    if (svc_description[0])
    {
	bronx_logprintf(BRONX_LOGGING_DEBUG, "{submit_check_result_to_nagios} hostname=%s description=%s check_type=%d",
	    chk_result->host_name, chk_result->service_description, chk_result->check_type);
    }
    else
    {
	bronx_logprintf(BRONX_LOGGING_DEBUG, "{submit_check_result_to_nagios} hostname=%s check_type=%d",
	    chk_result->host_name, chk_result->check_type);
    }

    /*
    ** Call the Nagios function to insert the result into the result linklist.
    **
    ** Access to the check_result_list must be adjudicated between the Bronx thread and
    ** the Nagios main thread by means of a mutex within add_check_result_to_list() or
    ** add_check_result_to_double_list() and all other routines that manipulate the list,
    ** so we don't have race conditions.  This requires a patch to Nagios.
    */
#if defined(NAGIOS_4_2_4_OR_LATER)
    ADD_ONE_CHECK_RESULT(&check_result_list, chk_result);
#elif defined(NAGIOS_3_4_4_OR_LATER)
    add_check_result_to_list(&check_result_list, chk_result);
#else
    add_check_result_to_list(chk_result);
#endif

    return(APR_SUCCESS);
}

// We need some mechanism here to transfer a command to Nagios.  In Nagios 3.5.1 and
// earlier, we could use the Nagios internal external_command_buffer data structure
// for that purpose.  It was already set up to accept such commands, with even the
// external_command_buffer.buffer_lock mutex already in place to adjudicate access from
// multiple threads.  But Nagios 4.2.4 no longer has the external_command_buffer data
// structure or any code to support it.  Hence we cannot use that directly to transfer
// a command to Nagios.  We could perhaps invent some means to use the iocache stuff
// that Nagios now uses, to stuff our command into the data that the command_worker
// process produces, and allow the command_input_handler() routine in base/commands.c
// to pick it up from there.  Or we could stuff the command into a file, and have
// process_external_commands_from_file() (also in base/commands.c) do the work of
// picking up the command from there.  Or we could call the write_cmd_to_nagios_pipe()
// routine in bronx_cmd_acceptor.c to pump the data back into Nagios through the
// Nagios command pipe (though that seems like an ugly solution, given that the data
// is already in the process memory).  Ultimately, though, we solve the problem in a
// different way:  we modify the post_check_result() routine in bronx_listener.c so it
// doesn't need to call this routine at all.  It only called submit_command_to_nagios()
// because for some unknown reason it was decided that submit_check_result_to_nagios()
// routine above could not be used for host checks, and that some other mechanism was
// needed.  But once submit_check_result_to_nagios() has been fixed, that does not
// appear to be the case.  So we just completely suppress submit_command_to_nagios()
// in Bronx builds for Nagios 4.2.4 and later, with no loss of generality.
//
#ifndef NAGIOS_4_2_4_OR_LATER

int submit_command_to_nagios(char *cmd)
{
    int result = BRONX_OK;

    /* obtain a lock for writing to the buffer */
    pthread_mutex_lock(&external_command_buffer.buffer_lock);

    if (external_command_buffer.items < external_command_buffer_slots)
    {
	/* save the line in the buffer */
	((char **)external_command_buffer.buffer)[external_command_buffer.head] = strdup(cmd);

	/* increment the head counter and items */
	external_command_buffer.head = (external_command_buffer.head + 1) % external_command_buffer_slots;
	external_command_buffer.items++;
	if (external_command_buffer.high < external_command_buffer.items)
	    external_command_buffer.high = external_command_buffer.items;
    }
    else /* buffer was full */
    {
	bronx_logprintf(BRONX_LOGGING_ERROR,
	    "{submit_command_to_nagios} external_command_buffer is full; discarding command: %s", cmd);
	result = BRONX_ERROR;
    }

    /* release lock on buffer */
    pthread_mutex_unlock(&external_command_buffer.buffer_lock);

    return(result);
}

#endif
