/*
 *  bronx_log.c -- Functions for logging.
 *
 *  Copyright (C) 2009-2017 Groundwork Open Source
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
 *      2007-09-17 DEF;		Initial creation.
 *      2012-12-11 GH;		Added comments about future development.
 *	2017-01-29 GH;		Updated comments.
 */

#include "bronx.h"
#include "bronx_config.h"
#include "bronx_utils.h"
#include "bronx_log.h"
#include "bronx_cmd_acceptor.h"
#include "bronx_safe_fork.h"

#include <stdarg.h>

/*
 *  Globals
 */

int			_logging_level = BRONX_LOGGING_ERROR;
apr_pool_t		*_log_pool;
/* A mutex for writing to the nagios log */
apr_thread_mutex_t	*_log_mutex;
/* A mutex for logging to the event broker log. */
apr_thread_mutex_t	*_bronx_log_mutex;

// This value is effectively treated as shared state.  But we don't apply
// any critical regions around accessing it, because we only expect it to
// be accessed from a single thread.
static FILE *bronx_log_fp = NULL;

/*
 *  General initialization of logging system.
 */

apr_status_t
log_init()
{
    apr_status_t	rv;

    _log_pool		= NULL;
    _log_mutex		= NULL;
    _bronx_log_mutex 	= NULL;

    if ((rv=apr_pool_create(&_log_pool, NULL)) == APR_SUCCESS)
    {
	if (((rv=apr_thread_mutex_create(&_log_mutex,       APR_THREAD_MUTEX_UNNESTED, _log_pool)) != APR_SUCCESS) ||
	    ((rv=apr_thread_mutex_create(&_bronx_log_mutex, APR_THREAD_MUTEX_UNNESTED, _log_pool)) != APR_SUCCESS))
	{
	    apr_pool_destroy(_log_pool);
	    _log_pool = NULL;
	}
    }

    return(rv);
}

void
log_setlevel(int level)
{
    _logging_level = level;
}

int
log_getlevel()
{
    return (_logging_level);
}

void
log_uninit()
{
    if (_log_mutex)
	apr_thread_mutex_destroy(_log_mutex);
    if (_bronx_log_mutex)
	apr_thread_mutex_destroy(_bronx_log_mutex);
    if (_log_pool)
	apr_pool_destroy(_log_pool);
}

void
bronx_log(char *msg, int level)
{
    /* If the logging level is "commands", don't log "passive checks" and vice versa. */
    if ((level == BRONX_LOGGING_COMMANDS       && _logging_level == BRONX_LOGGING_PASSIVE_CHECKS) ||
	(level == BRONX_LOGGING_PASSIVE_CHECKS && _logging_level == BRONX_LOGGING_COMMANDS))
    {
	return;
    }
    if (level <= _logging_level)
    {
	apr_pool_t  *pool;
	char        *formatted_string;

	apr_pool_create(&pool, _log_pool);
	formatted_string = apr_psprintf(pool, "[BRONX] %s", msg);

	if (level <= BRONX_LOGGING_WARNING) {
	    /*
	    ** Error and warning messages only. Write a copy to the nagios log,
	    ** so these messages are prominently available.
	    */
	    apr_thread_mutex_lock(_log_mutex);
	    write_to_logs_and_console(formatted_string, nagios_level(level), TRUE);
	    apr_thread_mutex_unlock(_log_mutex);
	}

	/*
	** Write all levels to the bronx log. This includes error and warning messages, as
	** it would be too confusing to have the bronx log be missing relevant messages.
	*/
	apr_thread_mutex_lock(_bronx_log_mutex);
	bronx_write_to_log(formatted_string);
	apr_thread_mutex_unlock(_bronx_log_mutex);

	apr_pool_destroy(pool);
    }
}

void
bronx_logprintf(int level, char *format_string, ...)
{
    /* If the logging level is "commands", don't log "passive checks" and vice versa. */
    if ((level == BRONX_LOGGING_COMMANDS       && _logging_level == BRONX_LOGGING_PASSIVE_CHECKS) ||
	(level == BRONX_LOGGING_PASSIVE_CHECKS && _logging_level == BRONX_LOGGING_COMMANDS))
    {
	return;
    }

    if (level <= _logging_level)
    {
	apr_pool_t  *pool;
	va_list     ap;
	char        *formatted_string;
	char        *tagged_format_string;

	apr_pool_create(&pool, _log_pool);
	tagged_format_string = apr_pstrcat(pool, "[BRONX] ", format_string, NULL);

	va_start(ap, format_string);
	formatted_string = apr_pvsprintf(pool, tagged_format_string, ap);
	va_end(ap);

	if (level <= BRONX_LOGGING_WARNING) {
	    /*
	    ** Error and warning messages only. Write a copy to the nagios log,
	    ** so these messages are prominently available.
	    */
	    apr_thread_mutex_lock(_log_mutex);
	    write_to_logs_and_console(formatted_string, nagios_level(level), TRUE);
	    apr_thread_mutex_unlock(_log_mutex);
	}

	/*
	** Write all levels to the bronx log. This includes error and warning messages, as
	** it would be too confusing to have the bronx log be missing relevant messages.
	*/
	apr_thread_mutex_lock(_bronx_log_mutex);
	bronx_write_to_log(formatted_string);
	apr_thread_mutex_unlock(_bronx_log_mutex);

	apr_pool_destroy(pool);
    }
}

/*
 * Write to the event broker logfile.
 * The log filename can be set in the event broker config file.
 */
void
bronx_write_to_log(char *buffer)
{
    // FIX MINOR:  Change this routine to not close the log file for every single log message
    // (in conjunction with changes to external log rotation of the logfile to accommodate this).
    // The file should be opened if it is not already open, then left that way.  The companion
    // bronx_close_log() routine can be called from the main body of the Bronx code (say, as the
    // last step of shutdown, when no more messages will be output via this channel, or perhaps just
    // within the log_uninit() routine above).  The file descriptor should be line-buffered, so it
    // gets flushed with every write.  The file descriptor should be closed and re-opened before a
    // new log message is output, if it has been some configurable interval since the last message
    // was output (say, one hour), to allow for externally-driven log rotation to be recognized.
    // Of course, that doesn't help if the user completely removes the log file which is currently
    // being written into; until Bronx closes and re-opens its log file, no log messages will appear
    // anywhere.  Perhaps a stat() call before each write() would be useful, though there we are
    // increasing the overhead back near the realm of what we were trying to avoid by not closing
    // the log file after every write.

    time_t log_time = 0L;

    // We only log to the event broker log file if we have a
    // non-empty string configured for the path to that log file.
    if (buffer && _configuration->log_filename[0])
    {
	/* Strip any newlines from the end of the buffer. */
	strip(buffer);
	if (bronx_log_fp == NULL)
	{
	    bronx_log_fp = bronx_safe_fopen(_configuration->log_filename, "a+");
	}
	if (bronx_log_fp)
	{
	    time(&log_time);
	    fprintf(bronx_log_fp, "[%lu] %s\n", log_time, buffer);

	    // FIX MINOR:  See comment above about not closing the log file after every write.
	    bronx_safe_fclose(bronx_log_fp);
	    bronx_log_fp = NULL;
	}
    }
    else
    {
	// FIX MINOR:  Since we might need to generate error messages during the processing
	// of the config file that contains the log_filename, perhaps we should failover to
	// writing to the standard error stream if the log_filename value is not available.
	// Then again, that's why we set default values for the config-file options before
	// we read in the config file, so we always do have a value for log_filename.
	if (0)
	{
	    strip(buffer);
	    time(&log_time);
	    fprintf(stderr, "[%lu] %s\n", log_time, buffer);
	}
    }
}

void
bronx_close_log()
{
    if (bronx_log_fp)
    {
	bronx_safe_fclose(bronx_log_fp);
	bronx_log_fp = NULL;
    }
}

void
bronx_logprintf_old(int level, char *format_string, ...)
{
    if (level <= _logging_level)
    {
	va_list     ap;
	static char formatted_string[32767];

	va_start(ap, format_string);
	apr_thread_mutex_lock(_log_mutex);

	vsprintf(formatted_string, format_string, ap);
	write_to_logs_and_console(formatted_string, nagios_level(level), TRUE);

	apr_thread_mutex_unlock(_log_mutex);
	va_end(ap);
    }
}

int nagios_level(int bronx_level)
{
    int nl;

    /*
     * Error and debugs are the only levels that should make it here.
     * But we try to be lenient.
     */
    switch(bronx_level)
    {
	case BRONX_LOGGING_ERROR:
	    nl = NSLOG_RUNTIME_ERROR;
	    break;
	case BRONX_LOGGING_WARNING:
	    nl = NSLOG_RUNTIME_WARNING;
	    break;
	default:
	    nl = NSLOG_INFO_MESSAGE;
	    break;
    }
    return(nl);
}

/*
 * audit_log()
 * This function generates the audit trail for Nagios commands.
 * The writing to the log file is not lock-protected because
 * there is only one thread, command_acceptor, writing to it.
 * If more threads start writing to this lockfile, locking should
 * be introduced for synchronization.
 * Parameters -
 * char *cmd            : Command to be logged.
 * char *user           : The user who submitted the command
 * time_t timestamp     : Time when the command was received.
 */
void
audit_log(char *cmd, char *status, char *user, time_t timestamp)
{
    FILE *audit_fp;

    /* Check if we are configured to create the audit trail. */
    if (_configuration->audit_trail == AUDIT_TRAIL_ON && _configuration->audit_trail_filename[0])
    {
	/* If the file does not exist, create it. */
	audit_fp = bronx_safe_fopen(_configuration->audit_trail_filename, "a+");
	if(audit_fp == NULL)
	{
	    bronx_log("{audit_log} Failed to open the Audit log file", BRONX_LOGGING_WARNING);
	    return;
	}
	if ((fprintf(audit_fp,"%s:\t[%lu] [%s] %s\n", status, timestamp, user, cmd)) < 0)
	{
	    bronx_log("{audit_log} Failed to write to the Audit log file", BRONX_LOGGING_WARNING);
	}

	bronx_safe_fclose(audit_fp);
    }
}
