/*
 *  bronx_config.h
 *
 *  Copyright (C) 2008-2012 Groundwork Open Source
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
 *	2007-10-16 DEF;		Initial creation.
 *	2012-12-07 GH;		Added more configuration parameters;
 *				dropped obsolete/unused parameters.
 *	2012-12-10 GH;		Implement forcibly closing idle connections.
 */

#ifndef _BRONX_CONFIG_H
#define	_BRONX_CONFIG_H

#ifdef	__cplusplus
extern "C" {
#endif

#define BRONX_CONFIG_PATH_MAX			256
#define BRONX_CONFIG_FN_MAX			32
#define BRONX_CONFIG_TYPE_MAX			8
#define BRONX_CONFIG_ADDR_MAX			32
#define BRONX_CONFIG_NUM_ROUTES			64
#define BRONX_CONFIG_NUM_ALLOWED_HOST_RANGES	128
#define BRONX_MAX_INPUT_BUFFER			1024
#define	BRONX_NAGIOS_CMD_DENY			0
#define	BRONX_NAGIOS_CMD_ALLOW			1
#define	BRONX_NAGIOS_CMD_PASSWD_ALLOW		2
#define BRONX_MAX_KEY_LEN			256

/*
 *  Queue Insertion Method
 */

#define BRONX_QIM_IMMEDIATE         1

/*
 *  Our configuration structure.
 */

typedef struct
{
    char    config_filename[BRONX_CONFIG_PATH_MAX];       // Full pathname of our bronx configuration file.

    /*
     * Configuration file options
     */

    // Global options:

    int     listener;
    char    database_dir[BRONX_CONFIG_PATH_MAX];
    int     startup_pause_timer;		// # of seconds to pause after startup before starting processing.
    int     max_client_connections;
    int     reserved_file_descriptor_count;	// # of file descriptors to reserve for non-listener usage within Nagios and Bronx.
    int     idle_connection_timeout;

#ifdef INCLUDE_OBSOLETE_OPTIONS
    int     queue_size;                 // In # of Entries.
    int     soft_state_changes;		// bool, are soft state changes processed?
    int     spillover_on_queue_full;    // bool, should we spillover if the queue is full?
    int     seconds_between_reconnect_attempts;
    int     consolidation;              // bool, send consolidation message?
    unsigned long   tcp_socket_timeout;	// tcp socket timeout.
    int     num_aggregators;            // # of aggregators. Helpful to know, if 0, can save processing time.
#endif

    // Listener configuration parameters

    int             listener_port;
    char            listener_address[BRONX_CONFIG_ADDR_MAX];
    char            listener_password[BRONX_MAX_INPUT_BUFFER];
    int             listener_encryption_method;
    unsigned long   listener_max_packet_age;		// limit on past-timestamp delta
    unsigned long   listener_max_packet_imminence;	// limit on future-timestamp delta
    char            listener_allowed_hosts[BRONX_CONFIG_NUM_ALLOWED_HOST_RANGES][BRONX_CONFIG_ADDR_MAX];
    int             listener_num_allowed_hosts;
    int             listener_nagios_cmd_execution;

#ifdef INCLUDE_OBSOLETE_OPTIONS
    int max_plugin_output_len;
#endif
    char log_filename[BRONX_CONFIG_PATH_MAX];
    int use_client_timestamp; // bool. Should we use client timestamp for result packet received?
    
    /* Command acceptor paramters */
    int             cmd_acceptor_port;
    /* Maximum simultaneous connections allowed */
    int             cmd_acceptor_max_conn;
    /* Only DES and BLOWFISH are supported. */
    int             cmd_acceptor_encryption_method;
    /* Encryption/Decyption key */
    char	    cmd_acceptor_key[BRONX_MAX_KEY_LEN];
    /* Optional list of hosts (IP addresses) to be serviced. */
    char            cmd_acceptor_allowed_hosts[BRONX_CONFIG_NUM_ALLOWED_HOST_RANGES][BRONX_CONFIG_ADDR_MAX];
    int             cmd_acceptor_num_allowed_hosts;  
    /* Nagios commands audit trail */
    int             audit_trail;
    char            audit_trail_filename[BRONX_CONFIG_PATH_MAX];

    char            nagios_cmdpipe_filename[BRONX_CONFIG_PATH_MAX];
} configuration_criteria;

extern configuration_criteria  *_configuration;

/*
 *  Externally defined functions.
 */

extern void config_dump(configuration_criteria *);
extern void config_set_defaults(configuration_criteria *config);
extern int parse_args(configuration_criteria *config, char *args);
extern int read_config_file(configuration_criteria *config);

#ifdef	__cplusplus
}
#endif

#endif	/* _BRONX_CONFIG_H */
