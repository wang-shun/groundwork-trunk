/*
 *  bronx_utils.c -- Various utilities.
 *
 *  Copyright (c) 2007-2017 Groundwork Open Source
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
 *	20??-??-?? ???;	Other improvements along the way.
 *	2012-06-22 GH;	Added ident string.
 *			Normalized indentation whitespace.
 *	2013-03-19 GH;	Added Nagios version to ident string.
 *	2015-02-26 GH;	Extended ident string to identify the NEB API version.
 *	2017-01-31 GH;	Updated to support Nagios 4.2.4.
 */

#include "bronx.h"
#include "bronx_config.h"
#include "bronx_utils.h"
#include "bronx_neb.h"

#ifdef NAGIOS_4_2_4_OR_LATER
// Nagios 4.2.4 config.h.in no longer includes support for HAVE_PTHREAD_H and <pthread.h>.
// In fact, configuration in this Nagios release doesn't deal with the HAVE_PTHREAD_H
// symbol at all!  So we must include <pthread.h> here ourselves, to gain access to all
// the required symbols.  We are patching Nagios and building it with a special Makefile
// to ensure that it will operate correctly once again as a multi-threaded program, in
// conjunction with the threads that Bronx creates.
#include <pthread.h>
#endif

int	_bronx_terminating = FALSE;         // Global variable to determine if the connection thread needs to shut down.
int	_bronx_paused = 1;                  // Global variable to determine if processing if paused. Always start paused.
int	_bronx_manually_paused = 0;         // Global variable to determine if processing paused manually or internally.
time_t	_bronx_start_time;                  // When bronx was first initialized.

/*
// Those globals are shared between threads, so they need to be protected against concurrent access.
*/
pthread_mutex_t bronx_terminating_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t bronx_pause_state_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t bronx_start_time_mutex  = PTHREAD_MUTEX_INITIALIZER;

/*
// Identifying string so we can distinguish different from-scratch compilations of the libbronx.so
// library.  This is not a full version identification relating to the Subversion revision, and
// it actually only tracks when this one particular .c file was compiled, not the entire library
// (that is why __FILE__ is included in the string, to limit any resulting confusion).  But at least
// it may provide some important clues that would otherwise be hard to come by.  With this string
// compiled in (as long as the compiler doesn't optimize it out, since we don't have any references
// to it anywhere), you can run "ident /usr/local/groundwork/common/lib/libbronx.so" as a shell
// command line to print this string.
//
// Also important now is the Nagios version we compile against, and with which this copy of the
// Bronx library can be used, since there are some incompatibilities between Nagios releases.
// So we include that information here as well.  We also include the NEB API version for general
// information, since this is a Nagios Event Broker.
*/
char libbronx_ident[] = "$BronxCompileTime: " __TIME__ " on " __DATE__ " (" __FILE__ ") for Nagios " PROGRAM_VERSION
    ", with NEB API version " expand_and_stringify(CURRENT_NEB_API_VERSION) " $";

/*
 *  Utility Functions.
 */

void set_bronx_terminating (int termination_state)
    {
    pthread_mutex_lock (&bronx_terminating_mutex);
    _bronx_terminating = termination_state;
    pthread_mutex_unlock (&bronx_terminating_mutex);
    }

int bronx_is_terminating ()
    {
    int termination_state;

    pthread_mutex_lock (&bronx_terminating_mutex);
    termination_state = _bronx_terminating;
    pthread_mutex_unlock (&bronx_terminating_mutex);

    return termination_state;
    }

void set_bronx_paused (int paused_state)
{
    pthread_mutex_lock (&bronx_pause_state_mutex);
    _bronx_paused = paused_state;
    pthread_mutex_unlock (&bronx_pause_state_mutex);
}

void set_bronx_manually_paused (int paused_state)
{
    pthread_mutex_lock (&bronx_pause_state_mutex);
    _bronx_paused          = paused_state;
    _bronx_manually_paused = paused_state;
    pthread_mutex_unlock (&bronx_pause_state_mutex);
}

int bronx_is_paused()
{
    int paused_state;

    pthread_mutex_lock (&bronx_pause_state_mutex);
    /* if (bronx is in startup pause) */
    if (_bronx_paused && !_bronx_manually_paused)
    {
	time_t time_now;

	time (&time_now);

	// This comparison used to be (for Nagios 3.5.1 and earlier versions of Bronx) ">"
	// instead of ">=". That would effectively enforce up to a minimum 1-second pause,
	// possibly causing some amount of very early incoming data to be dropped.  For
	// the Nagios 4.2.4 compilation of Bronx, we switch this comparison to ">=" so we
	// completely avoid dropping any of the incoming data at startup.  See GWMON-10412
	// for more information.
	if (time_now >= bronx_start_time() + _configuration->startup_pause_timer)
	    _bronx_paused = 0;
    }

    paused_state = _bronx_paused;
    pthread_mutex_unlock (&bronx_pause_state_mutex);

    return (paused_state);
}

void set_bronx_start_time ()
    {
    pthread_mutex_lock (&bronx_start_time_mutex);
    time(&_bronx_start_time);
    pthread_mutex_unlock (&bronx_start_time_mutex);
    }

time_t bronx_start_time ()
    {
    time_t start_time;

    pthread_mutex_lock (&bronx_start_time_mutex);
    start_time = _bronx_start_time;
    pthread_mutex_unlock (&bronx_start_time_mutex);

    return start_time;
    }

char *datetostring(apr_pool_t* pool, time_t time)
{
    char *message;

    if (time)
    {
	char buffer[500];
	int result;
	apr_pool_t *mp;

	apr_pool_create(&mp, pool);
	result = strftime(buffer, 256, "%Y-%m-%d %T", localtime(&time));
	message = apr_psprintf(mp, "%s", result ? buffer : "0");
    }
    else
	message = "0";

    return(message);
}

/*
 *  Do whatever processing to the plugin output
 *  is necessary.
 */

void normalize_plugin_output(char *plugin_output, char *source)
{
    if (!strncmp(plugin_output, "[TEST] ", 7))
    {
	/*
	 *  Okay, we've got a packet sent from a test harness.
	 *
	 *  We need to add timing information to this packet.
	 */

	struct  timeval tp;
	char    *old_plugin_output;
	int     old_plugin_output_len = 0;

	old_plugin_output_len = strlen(plugin_output) + 1;
	old_plugin_output = (char*)malloc(old_plugin_output_len * sizeof(char));
	snprintf(old_plugin_output, old_plugin_output_len, "%s", plugin_output);
	gettimeofday(&tp, NULL);
	// FIX MINOR:  There is a HUGE presumption here that the plugin_output buffer is
	// large enough to handle this rewrite and possible extension of its content,
	// without overwriting end of the buffer.  That presumption ought to be formally
	// checked (for packets sent from the test harness).
	sprintf(plugin_output, "[TEST] %s=%lu.%lu %s", source, (unsigned long)tp.tv_sec,(unsigned long)(tp.tv_usec / 1000), &old_plugin_output[7]);
	if (old_plugin_output) {
	    free(old_plugin_output);
	    old_plugin_output = NULL;
	}
    }
}

// Do a string replace based off of a memory pool.  Can only do one string replace at a time.
char *pstrreplace(apr_pool_t* mp, const char* str, const char* needle1, const char* needle2 )
{
    char            *var, *tmp_pos, *needle_pos;
    unsigned long   count, len,
		    needle1_len = strlen(needle1),
		    needle2_len = strlen(needle2);

    // Will attempt to replace any instances of needle1 with needle2
    // This if/else will determine the maximum size of our string (len is int bound)

    if( needle1_len < needle2_len )
    {
	count   = 0;
	tmp_pos = (char*)str;

	while( (needle_pos = (char*) strstr( tmp_pos, needle1 )) )
	{
	    tmp_pos = needle_pos;
	    tmp_pos += needle1_len;
	    count++;
	}
	len = strlen(str) + (strlen(needle2) - needle1_len) * count;
	var = (char*) apr_pcalloc(mp, sizeof(char) * (len + 1) );
    }
    else
    {
	len = strlen(str);
	var = (char*) apr_pcalloc(mp, sizeof(char) * (len+1) );
    }
    tmp_pos = (char*) str;

    while( (needle_pos = (char*)strstr( tmp_pos, needle1 )) )
    {
	len = (needle_pos - tmp_pos);

	strncat( var, tmp_pos, len );
	strcat( var, needle2);

	tmp_pos = needle_pos + needle1_len;
    }

    strcat( var, tmp_pos );
    return var;
}

char *strcleanup(apr_pool_t *tempmp, char *str)
{
    char *tempString;

    tempString = pstrreplace(tempmp, str, "<br />", " ");
    tempString = pstrreplace(tempmp, tempString, "<BR />", " ");
    tempString = pstrreplace(tempmp, tempString, "\"", "&quot;");
    tempString = pstrreplace(tempmp, tempString, "'", "&quot;");
    tempString = pstrreplace(tempmp, tempString, "&", "&amp;");
    tempString = pstrreplace(tempmp, tempString, "<", "&lt;");
    tempString = pstrreplace(tempmp, tempString, ">", "&gt;");

    return(tempString);
}

/*--------------------------------------------------------------*/

char *bronx_strerror_r (int errnum, char *strerrbuf, size_t buflen)
    {
    char *char_ptr;
    int saved_errno = errno;

    // Linux <string.h> says:
    //
    // There are 2 flavors of `strerror_r', GNU which returns the string
    // and may or may not use the supplied temporary buffer and POSIX one
    // which fills the string into the buffer.  To use the POSIX version,
    // -D_XOPEN_SOURCE=600 or -D_POSIX_C_SOURCE=200112L without 
    // -D_GNU_SOURCE is needed, otherwise the GNU version is preferred.

#if (! defined (_GNU_SOURCE)) && (((_XOPEN_SOURCE - 0) >= 600) || ((_POSIX_C_SOURCE - 0) >= 200112L))
    int strerror_r_errno = strerror_r (errnum, strerrbuf, buflen);
    if (strerror_r_errno)
	{
	if (strerror_r_errno != EINVAL)
	    {
	    /*
	    // Yes, aborting is nasty.  But better to find out and fix the problem
	    // than to hobble along in confusion.  The most likely issue here is that
	    // the size of the buffer is too small to contain the error message, so
	    // the buffer size will need to be extended and the program recompiled.
	    */
	    bronx_logprintf (BRONX_LOGGING_ERROR,
		"{bronx_strerror_r} strerror_r(%d) failed with errno=%d; aborting!\n",
		errnum, strerror_r_errno);
	    exit (EXIT_FAILURE);
	    }
	char_ptr = "unknown error";
	}
    else
	{
	char_ptr = strerrbuf;
	}
#else
    char_ptr = strerror_r (errnum, strerrbuf, buflen);
    if (char_ptr == NULL)
	{
	char_ptr = "unknown error";
	}
#endif

    errno = saved_errno;

    return (char_ptr);
    }
