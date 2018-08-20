/* 
 *  bronx_listener.h -- File route handling.
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
 *      2007-09-17 DEF;		Initial creation.
 *      2012-12-04 GH;		Extended the struct handler_entry handler.
 *	2012-12-10 GH;		Implement forcibly closing idle connections.
 */

#ifndef _BRONX_LISTENER_H
#define _BRONX_LISTENER_H

#include <time.h>

#define BRONX_LISTENER_POLL_TIMEOUT     30000

extern void* APR_THREAD_FUNC thread_listener(apr_thread_t *thread, void *data);

// The handler cleanup_only parameter is to be implemented by the handler as:
// false => go about the normal business of handling i/o
// true  => only perform cleanup activities on the fd, payload, and associated data
//
// The regime for a handler's return code is as follows.
// * If the file descriptor is to remain open for more i/o, return BRONX_OK.
// * If the file descriptor is to be closed, return BRONX_ERROR.
// If it sees a return code other than BRONX_OK, the calling code must then close
// the file descriptor and clean up whatever other data structures are involved at
// its own level.  The handler is responsible for cleaning up whatever companion
// data structures are involved at that level, before returning the non-BRONX_OK
// return value.
struct handler_entry
{
    int (*handler)(int fd, void *payload, int cleanup_only);
    void *data;
    int fd;
};

// This structure only has a trivial payload, for now.  We could have just used
// that data type as the whole object.  But we define it as a structure to allow
// this object to be easily extended in future versions.  We could, for instance,
// add a "first_active_time" field to tell when the file descriptor was opened,
// and use that to impose an activity timeout [sic] to close descriptors that
// just won't stop talking, if we ever had such a client problem.
struct file_descriptor
{
    time_t last_active_time;
};

#endif	/* _BRONX_LISTENER_H */
