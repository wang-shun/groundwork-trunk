/************************************************************************
 *
 * NSCA Common Config Header File
 * Copyright (c) 2000-2006 Ethan Galstad (nagios@nagios.org)
 * Last Modified: 01-21-2006
 *
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 ************************************************************************/

#define HAVE_LIBMCRYPT
#include <stdio.h>
#include <stdlib.h>


#define DEFAULT_SERVER_PORT     5667     /* default port to use */


/* #undef ENABLE_COMMAND_ARGUMENTS */

#define STDC_HEADERS 1
#define HAVE_STRDUP 1
#define HAVE_STRSTR 1
#define HAVE_STRTOUL 1 
#define HAVE_INITGROUPS 1

#define SIZEOF_INT 4
#define SIZEOF_SHORT 2
#define SIZEOF_LONG 4

/* stupid stuff for u_int32_t */
/* #undef U_INT32_T_IS_USHORT */
/* #undef U_INT32_T_IS_UINT */
/* #undef U_INT32_T_IS_ULONG */

typedef short int16_t;
typedef unsigned long u_int32_t;

#ifdef HAVE_LIBMCRYPT
#include "mcrypt.h"
#endif

#include <winsock2.h>
#include <string.h>
#include <time.h>
#include "../win32/wincompat.h"
