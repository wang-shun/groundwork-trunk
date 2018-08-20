/*
 *  Copyright (C) 2009-2012 Groundwork Open Source
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
 * File:   bronx_thread.h
 */

#ifndef _BRONX_THREAD_H
#define	_BRONX_THREAD_H

/* Two thread handlers to our threads */
extern apr_thread_t *_thread_listener;
extern apr_thread_t *_thread_cmd_acceptor;

extern apr_status_t threads_init();
extern void threads_uninit();
extern int threads_stop(configuration_criteria *config);
extern int assert_threads_running(configuration_criteria *config);

#endif	/* _BRONX_THREAD_H */
