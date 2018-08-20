/* 
 *  bronx_cmd_acceptor.h -- Command acceptor headers.
 *
 *  Copyright (C) 2009-2012 Groundwork Open Source
 *  Originally written by Hrisheekesh Kale 
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
 *      2009-02-18 hkale; Created.
 *      2012-05-14 GH; Added CA_ABORT.
 */

#ifndef _BRONX_CMD_ACCEPTOR_H
#define _BRONX_CMD_ACCEPTOR_H

extern void* APR_THREAD_FUNC thread_cmd_acceptor(apr_thread_t *thread, void *data);

/* Command acceptor parameter defaults */
#define DEFAULT_CMD_ACCEPTOR_PORT       5677
#define DEFAULT_CMD_ACCEPTOR_MAX_CONN   5
#define AUDIT_TRAIL_OFF                 0
#define AUDIT_TRAIL_ON                  1
#define DEFAULT_DES_KEY                 "12345678"

/* Command acceptor defines */
#define DEFAULT_NAGIOS_CMD_PIPE             "/usr/local/groundwork/nagios/var/spool/nagios.cmd"
/* The time in miliseconds for which all the open socket fds are allowed to be inactive */
#define BRONX_CMD_ACCEPTOR_POLL_TIMEOUT     30000
/* Read timeout in seconds for one client socket */ 
#define CMD_ACCEPTOR_DEFAULT_SOCKET_TIMEOUT 10
/* We allow 5 chars len for command size. Max allowed command size is 99999 bytes */ 
#define BYTESIZE_CMD_LEN                    5
#define MAX_TIMESTAMP_LEN                   64
/* Length of the IP address quad */
#define IP_ADDR_LEN                         24
/* command acceptor return values */
#define CA_OK      0
#define CA_ERROR  -1
#define CA_ABORT  -2

/* Nagios command status for audit trail */
#define NAGIOS_CMD_STATUS_COMPLETE      "Submitted to Nagios"
#define NAGIOS_CMD_STATUS_INPROGRESS    "Submitting to Nagios"

/* Socket read/write handler structure */
struct ca_handler_entry
{
    /* Handler function on the read/write event on the socket */
    void (*handler)(int, void *);
    /* Handler specific data. Not used in some handlers. */
    void *data;
    /* The socket fd on which to track events. */
    int fd;
};

#endif	/* _BRONX_CMD_ACCEPTOR_H */
