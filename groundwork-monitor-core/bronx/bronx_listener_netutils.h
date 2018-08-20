/* 
 *  bronx_listener_netutils.h
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
 *      2012-12-05 GH;		Added get_full_peer_address().
 */

#ifndef _BRONX_LISTENER_NETUTILS_H
#define _BRONX_LISTENER_NETUTILS_H

extern int my_tcp_connect(char *,int,int *);
extern int my_connect(char *,int,int *,char *);
extern int my_inet_aton(register const char *,struct in_addr *);
extern ssize_t sendall(int,char *,size_t *);
extern ssize_t recvall(int,char *,size_t *,int,int);
extern void get_full_peer_address(int,char *,int,int *);
extern void get_peer_address(int,char *,int);
#endif	/* _BRONX_LISTENER_NETUTILS_H */
