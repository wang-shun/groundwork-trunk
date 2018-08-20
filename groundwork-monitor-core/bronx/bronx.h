/*
 *  bronx.h -- Access to common symbols and declarations.
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
 *	2007-07-11 DEF;	Created.
 *	2012-05-14 GH;	Added BRONX_ABORT code.
 *	2012-06-22 GH;	Normalized indentation whitespace.
 *	2017-01-29 GH;	Updated to support Nagios 4.2.4.
 */

#ifndef _BRONX_H
#define	_BRONX_H

/* include some NAGIOS stuff */
/*
// A critical piece of this is that the Nagios 3.X "common.h" file defines
// the _REENTRANT symbol, which MUST be defined before including system
// header files.  Which means that if Bronx depends on this one place to set
// that symbol rather than setting it on the compiler command line, we MUST
// insist that "bronx.h" be #include'd before any other header file in every
// one of our .c files, AND we need to make sure that even here in bronx.h
// that we #include "common.h" before #include'ing any system header files,
// and we must make sure that all the Nagios code itself does the same thing.
//
// Nagios 4.X releases change some of that by no longer defining _REENTRANT
// in any of its header files.  We effectively impose _REENTRANT again by
// specifying -pthread on the gcc command line when we compile Nagios.  And
// we get _REENTRANT established for the Bronx build through settings in its
// own Makefile.
//
// Nagios 3.X "common.h" also defines _THREAD_SAFE, which is a symbol equivalent
// to _REENTRANT which is supposedly used under AIX and OSF1 and perhaps other
// platforms to similarly invoke multi-threaded support.
//
// In Nagios 3.X releases, "config.h" pulled in certain other header files
// that we ended up needing both in other Nagios header files and in our
// own code.  In Nagios 4.X releases, this is apparently no longer necessary,
// so we drop this inclusion when compiling for Nagios 4.X because that file
// is not installed by default when we "make install-headers" in the Nagios
// distribution.  That leaves just a few places where we did still need
// such an inclusion in our own code, but we have now made those inclusions
// of certain system headers explicit in the affected code.
*/
#define NSCORE
#include "common.h"
#ifndef NAGIOS_4_2_4_OR_LATER
#include "config.h"
#endif
#include "nagios.h"
#include "objects.h"

/* Generic includes */
#include <sys/types.h>          // Include our types
#include <sys/stat.h>           // Include support for our FIFO Special Files
#include <sys/socket.h>         // Include support for our sockets
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

// Set DEBUG to 1 if you want a profusion of output in your [nagios.log] and console,
// otherwise, set it to 0, and it will complain only when there is something wrong.
// (Huh?  I don't see any support for that claim.  Where is this symbol used??)
#define DEBUG 1

// Define off64_t for non-64-bit platforms, for use with apr 1.0 or higher
#ifndef off64_t
#define off64_t long
#endif

/* Include required APR libraries */
#include <apr_errno.h>
#include <apr_general.h>
#include <apr_queue.h>
#include <apr_strings.h>
#include <apr_strings.h>
#include <apr_hash.h>
#include <apr_thread_proc.h>
#include <apr_getopt.h>
#include <apr_network_io.h>
#include <apr_thread_cond.h>

#ifdef	__cplusplus
extern "C" {
#endif

// VARIOUS

#define BRONX_ABORT	-2
#define BRONX_ERROR	-1
#define BRONX_OK	0

#define BRONX_STALE_TIMEOUT         (unsigned long)300

// OUR  TYPES
#define MSG_TYPE_HOST_STATUS        1
#define MSG_TYPE_SERVICE_STATUS     2
#define MSG_TYPE_SYSTEM_CONFIG      3
#define MSG_TYPE_NAGIOS_LOG         4
#define MSG_TYPE_ACKNOWLEDGEMENT    5
#define MSG_TYPE_NOTIFICATION       6

// OUR ERRORS
#define BRONX_EINVALIDTYPE 1

// What are the columns in our message table
#define BRONXDB_ID_COL              0
#define BRONXDB_MESSAGE_COL         1
#define BRONXDB_TIMESTAMP_COL       2

typedef struct last_object_state_struc
{
    int last_state;
    int	last_state_type;
} last_object_state;

/* Our message structure declaration */
struct message_struc
{
    apr_pool_t  *pool;

    int         type;           // Type of Message.
    apr_hash_t  *properties;    // Hash Table.
};

typedef struct message_struc message;

/* A structure to hold a result id.  populated when executing a sql select statement and iterating through the results. */
struct resultid_struc
{
    char        *id;
    apr_pool_t  *pool;
};
typedef struct resultid_struc resultid;

// Pool and Queue Data.
// extern  apr_thread_mutex_t  *_queue_mutex;
// extern  apr_queue_t         *_queue;

// Routing Data.
// extern apr_thread_cond_t	*_route_thread_cond_signal;
// extern int			_msg_sequence;

#ifdef	__cplusplus
}
#endif

#endif	/* _BRONX_H */
