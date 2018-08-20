/*
 *  bronx_nagios.h -- Declarations for functions interfacing with Nagios.
 *
 *  Copyright (c) 2009-2017 Groundwork Open Source
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
 *	2009-04-21 GH;	Created.
 *	2017-01-29 GH;	Updated to support Nagios 4.2.4.
 */

#ifndef _BRONX_NAGIOS_H
#define	_BRONX_NAGIOS_H

extern int submit_check_result_to_nagios(char *host_name, char *svc_description, int return_code, char *plugin_output, time_t check_time);

#ifndef NAGIOS_4_2_4_OR_LATER
extern int submit_command_to_nagios(char *cmd);
#endif

#endif	/* _BRONX_NAGIOS_H */
