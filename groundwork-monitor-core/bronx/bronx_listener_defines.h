/* 
 *  bronx_listener_defines.h
 *
 *  Copyright (C) 2007 Groundwork Open Source
 *  Written by Daniel Emmanuel Feinsmith
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
 *      DEF Created on September 17, 2007, 1:00 PM
 */

#ifndef _BRONX_LISTENER_DEFINES_H
#define _BRONX_LISTENER_DEFINES_H

#include <stdio.h>
#include <stdlib.h>

#define DEFAULT_NSCA_PORT     5667     /* default port to use */

#undef socklen_t

#undef STDC_HEADERS
#undef HAVE_SYSLOG_H
#undef HAVE_STRDUP
#undef HAVE_STRSTR
#undef HAVE_STRTOUL 
#undef HAVE_INITGROUPS
#undef HAVE_LIMITS_H
#undef HAVE_SYS_RESOURCE_H

#undef SIZEOF_INT
#undef SIZEOF_SHORT
#undef SIZEOF_LONG

/* stupid stuff for u_int32_t */
#undef U_INT32_T_IS_USHORT
#undef U_INT32_T_IS_UINT
#undef U_INT32_T_IS_ULONG
#undef U_INT32_T_IS_UINT32_T

#ifdef U_INT32_T_IS_USHORT
typedef unsigned short u_int32_t;
#endif
#ifdef U_INT32_T_IS_ULONG
typedef unsigned long u_int32_t;
#endif
#ifdef U_INT32_T_IS_UINT
typedef unsigned int u_int32_t;
#endif
#ifdef U_INT32_T_IS_UINT32_t
typedef uint32_t u_int32_t;
#endif

/* stupid stuff for int32_t */
#undef INT32_T_IS_SHORT
#undef INT32_T_IS_INT
#undef INT32_T_IS_LONG

#ifdef INT32_T_IS_USHORT
typedef short int32_t;
#endif
#ifdef INT32_T_IS_ULONG
typedef long int32_t;
#endif
#ifdef INT32_T_IS_UINT
typedef int int32_t;
#endif

#include <regex.h>
#include <strings.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdint.h>
#include <sys/poll.h>

#include <sys/types.h>
#include <sys/wait.h>

#ifndef WEXITSTATUS
# define WEXITSTATUS(stat_val) ((unsigned)(stat_val) >> 8)
#endif
#ifndef WIFEXITED
# define WIFEXITED(stat_val) (((stat_val) & 255) == 0)
#endif

#include <errno.h>

/* needed for the time_t structures we use later... */
#undef TIME_WITH_SYS_TIME
#undef HAVE_SYS_TIME_H
#if TIME_WITH_SYS_TIME
# include <sys/time.h>
# include <time.h>
#else
# if HAVE_SYS_TIME_H
#  include <sys/time.h>
# else
#  include <time.h>
# endif
#endif

#include <sys/socket.h>
#include <tcpd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <ctype.h>
#include <mcrypt.h>
#include <db.h>
#include <pwd.h>
#include <grp.h>
#include <inttypes.h>

#undef HAVE_INTTYPES_H
#undef HAVE_STDINT_H
#ifdef HAVE_INTTYPES_H
#include <inttypes.h>
#else
#ifdef HAVE_STDINT_H
#include <stdint.h>
#endif
#endif

#endif	/* _BRONX_LISTENER_DEFINES_H */
