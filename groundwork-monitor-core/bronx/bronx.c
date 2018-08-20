/*
 *  bronx.c -- Functions for connecting/disconnecting from Nagios.
 *
 *  Copyright (C) 2008-2017 Groundwork Open Source
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
 *	2012-05-14 GH;	Trivial comment fixes.
 *	2012-06-22 GH;	Normalized indentation whitespace.
 *	2013-03-19 GH;	Extended a comment.
 *	2017-01-31 GH;	Updated to support Nagios 4.2.4, and to linearize the logic.
 */

#include "bronx.h"
#include "bronx_neb.h"
#include "bronx_config.h"
#include "bronx_log.h"
#include "bronx_processors.h"
#include "bronx_thread.h"
#include "bronx_utils.h"

/*
 *  specify event broker API version (required)
 */

NEB_API_VERSION(CURRENT_NEB_API_VERSION);

/*
 *  Globals
 */

void *_bronx_module_handle=NULL;	// Our module handle

/*
 * nebmodule_init
 *
 * This is the entry point to this shared object.  It is called first by Nagios
 * to connect up this shared object to nagios, and to functionally bind our
 * logic to Nagios.
 *
 * Returns zero (interpreted by the caller as OK) upon success, non-zero if failure.
 */

int
nebmodule_init(int flags, char *args, nebmodule *handle)
{
    int rv = BRONX_OK;
    int config_file_exists = 0;
    set_bronx_terminating (FALSE);
    set_bronx_start_time();
    write_to_logs_and_console("{init} BRONX Module (built at "__TIME__" on "__DATE__") Initializing.",
	nagios_level(BRONX_LOGGING_INFO), TRUE);

    /*
     *  Save the neb module handle we've been passed.
     */

    _bronx_module_handle = handle;

    /*
     * Initialize Apache portable runtime.
     */

    apr_initialize();
    write_to_logs_and_console("{init} Apache Portable Runtime Initialized.", nagios_level(BRONX_LOGGING_INFO), TRUE);

    /*
     * Initialize global memory pool.
     */

    write_to_logs_and_console("{init} Memory Pool Created.", nagios_level(BRONX_LOGGING_INFO), TRUE);

    /*
     * Initialize log system.
     */

    if (log_init() != APR_SUCCESS)
    {
	write_to_logs_and_console("{init} Unable to initialize log system, detaching.", nagios_level(BRONX_LOGGING_WARNING), TRUE);
	return(1);
    }
    /*
     * Don't call bronx_log() before the log filename is set.  (config_set_defaults()/read_config_file())
     */
    write_to_logs_and_console("{init} Log system initialized.", nagios_level(BRONX_LOGGING_INFO), TRUE);

    /*
     * Allocate space for all of our internal configuration
     * criteria, and fill the configuration spec from
     * the command line and configuration file.
     */

    _configuration = malloc(sizeof(configuration_criteria));

    write_to_logs_and_console("{init} Building Command Line Options ...", nagios_level(BRONX_LOGGING_DEBUG), TRUE);
    if (parse_args(_configuration, args) != TRUE)
    {
	write_to_logs_and_console("{init} Error parsing args on command line.  Check nagios.cfg.", nagios_level(BRONX_LOGGING_ERROR), TRUE);
	return(1);
    }

    config_set_defaults(_configuration);

    if ((_configuration->config_filename[0] != '\0') &&
	(access(_configuration->config_filename, F_OK) == 0))
    {
	config_file_exists = 1;
    }

    if (config_file_exists)
    {
	bronx_log("{init} Reading Config File", BRONX_LOGGING_DEBUG);
	rv = read_config_file(_configuration);
    }
    else
    {
	bronx_log("{init} Config File Not Found.  Using Default Values.", BRONX_LOGGING_WARNING);
	rv = BRONX_OK;
    }
    if (rv == BRONX_ERROR)
    {
	bronx_log("{init} Can't load Bronx configuration file supplied.", BRONX_LOGGING_ERROR);
	return(1);
    }

    /*
     *  Dump configuration.
     */
    config_dump(_configuration);

    /*
     *  Create main processing queue.
     */
    bronx_log("{init} Initializing Internal Processing System.", BRONX_LOGGING_INFO);
    if (processors_init() != APR_SUCCESS)
    {
	bronx_log("{init} Error Initializing Internal Processing System.", BRONX_LOGGING_ERROR);
	return(1);
    }
    bronx_log("{init} Processing System Initialized.", BRONX_LOGGING_DEBUG);

    // Initialize Thread System.
    if (threads_init() != APR_SUCCESS)
    {
	bronx_log("{init} Error Initializing Threads System.", BRONX_LOGGING_ERROR);
	return(1);
    }

    // Configure Event Broker call-backs.
    neb_register_callback(NEBCALLBACK_PROCESS_DATA, _bronx_module_handle, 0, processProcessData);

    // And we're done initializing.
    bronx_log("{init} BRONX Module Initialization Completed.", BRONX_LOGGING_INFO);
    return(0);
}

/*
 * This is our unloading function which gets called by the NEB
 * when Nagios is shutting down.
 *
 * Returns zero (interpreted by the caller as OK) upon success, non-zero if failure.
 */

// In Nagios 4.2.4, flags/reason will be either:
//
// During startup, when not all NEB modules could be loaded:
// NEBMODULE_FORCE_UNLOAD, NEBMODULE_NEB_SHUTDOWN
//
// During shutdown or restart, called at the last moment from the Nagios cleanup() routine:
// NEBMODULE_FORCE_UNLOAD, (sigshutdown == TRUE) ? NEBMODULE_NEB_SHUTDOWN : NEBMODULE_NEB_RESTART
//
// In neither case are these arguments terribly interesting to us here.

int
nebmodule_deinit(int flags, int reason)
{
    // Write to logs stating we are unloading.
    bronx_log("{deinit} Bronx Detaching from NAGIOS.", BRONX_LOGGING_INFO);

    // These two calls may have been made earlier, in processProcessData(), to force the
    // Bronx threads to have stopped before saving already-queued data.  And that seems to
    // be the right place to do so, in symmetry with where the threads are started from.
    // But these calls are idempotent, so we make them now regardless as a kind of backstop
    // to ensure the threads are down before we destroy the resources they depend on.
    set_bronx_terminating (TRUE);
    threads_stop(_configuration);

    threads_uninit();
    bronx_log("{deinit} Threads Shut Down.", BRONX_LOGGING_INFO);

    /*
     * Uninitialize the log system.  Do not use bronx_log() from this point on.
     * write_to_logs_and_console(), which was available as an external function
     * through Nagios 3.2.3, writes the message directly to the nagios log.
     * (In later Nagios releases, you can get to write_to_logs_and_console()
     * indirectly, by calling logit() instead, but the extra layer of string
     * formatting it imposes is wasteful.  In our builds, we patch Nagios to
     * once again make write_to_logs_and_console() externally visible, as
     * there seems to be no good reason it should be a static function.)
     * So be reasonable and keep the logging to a minimum.
     */
    log_uninit();

    // Release the processors resources.
    processors_uninit();

    /*
     *  Do not use APR resources (e.g., bronx_log()) from this point on.
     *  Doing so will cause a crash.
     */
    apr_terminate();
    write_to_logs_and_console("{deinit} Apache Portable Runtime terminated.", nagios_level(BRONX_LOGGING_INFO), TRUE);

    // Remove all our callbacks from the NEB interface.
    neb_deregister_callback(NEBCALLBACK_PROCESS_DATA, processProcessData);

    free(_configuration);

    write_to_logs_and_console("{deinit} Bronx Detached from NAGIOS Successfully.", nagios_level(BRONX_LOGGING_INFO), TRUE);
    return(0);
}
