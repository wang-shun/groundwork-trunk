/*
 *  Copyright (C) 2009 Groundwork Open Source
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
 * File:   bronx_admin.h
 */

#ifndef _BRONX_PROCESSORS_H
#define	_BRONX_PROCESSORS_H

extern apr_status_t processors_init();
extern void processors_uninit();

// CALLBACKS
// extern int processAcknowledgement(int cmd, void *data);
// extern int processCommand(int cmd, void *data);
// extern int processContactNotification(int cmd, void *data);
// extern int processDowntime(int cmd, void *data);
// extern int processExternalCommand(int cmd, void *data);
// extern int processHostCheck(int, void *);
   extern int processProcessData(int cmd, void *data);
// extern int processProgramStatus(int cmd, void *data);
// extern int processServiceCheck(int, void *);
// extern int processStatus(int, void *);

#endif	/* _BRONX_PROCESSORS_H */
