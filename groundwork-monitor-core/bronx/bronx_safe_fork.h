/* 
//  bronx_safe_fork.h -- enforce safe fork() actions
//
//  Copyright (C) 2009 Groundwork Open Source
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
//  Change Log:
//      GH Created on April 21, 2009
*/

#ifndef _BRONX_SAFE_FORK_H
#define _BRONX_SAFE_FORK_H

/*
// This symbol provides an emergency means to disable the safety protections
// in this code.  If that is absolutely necessary, just define this as 0, and
// recompile all the code.
*/
#define	USE_SAFE_FORK_PROTECTION	1

#if USE_SAFE_FORK_PROTECTION

#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>

#define	BRONX_SUCCESS	0
#define	BRONX_FAILURE	1

/*
// Routine to initialize the safety mechanisms.  Call this just once in
// the program, before calling any of the bronx_safe_*() routines below.
// Returns BRONX_SUCCESS or BRONX_FAILURE (or may die).
*/
int bronx_init_safe_operations (void);

/*
// Routines to reliably create and destroy file descriptors, in conjunction
// with maintaining a list of the open file descriptor so they can be used by
// the atfork handlers.  Use these as direct one-for-one replacements for the
// standard socket(), accept(), and close() functions.
//
// IMPORTANT NOTE:  It is critical that bronx_safe_accept() never block while
// holding a mutex lock.  To ensure this, the passed listening socket "s" MUST
// have the O_NONBLOCK flag set (see socket(7)).  And to eliminate the overhead
// of enforcing that setting inside every call to bronx_safe_accept(), we make
// setting the flag a precondition for calling bronx_safe_accept().  To simplify
// the user code and make it unnecessary for the application developer to worry
// about the precondition, that setting will be handled automatically by the
// bronx_safe_socket() call.  However, in spite of the normal semantics of that
// flag, the bronx_safe_accept() routine as a whole will block waiting for an
// incoming connection, to simplify the construction of the calling code.
*/
int bronx_safe_socket (int domain, int type, int protocol);
int bronx_safe_accept (int s, struct sockaddr *addr, socklen_t *addrlen);
int bronx_safe_close (int fildes);

/*
// Routines to reliably create and destroy FILE handles, in conjunction with
// maintaining a list of the associated open file descriptors so they can be
// used by the atfork handlers.  Use these as direct one-for-one replacements
// for the standard fopen() and fclose() functions.  Handling FILE handles in
// a child process is trickier than handling the underlying file descriptors,
// because the child process is not allowed to flush any pending output.  That
// responsibility is left to the parent process.
*/
FILE *bronx_safe_fopen (const char *filename, const char *mode);
int bronx_safe_fclose (FILE *stream);

#else

/*
// These definitions would restore the operation of the calling code
// to what it was before the safety mechanisms were in place.
*/
#define bronx_init_safe_operations()
#define bronx_safe_socket	socket
#define bronx_safe_accept	accept
#define bronx_safe_close	close
#define bronx_safe_fopen	fopen
#define bronx_safe_fclose	fclose

#endif

#endif	/* _BRONX_SAFE_FORK_H */
