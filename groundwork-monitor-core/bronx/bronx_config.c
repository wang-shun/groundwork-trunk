/*
 *  bronx_config.c -- Configuration functions.
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
 *	2007-09-17 DEF;		Initial creation.
 *	2012-12-07 GH;		Added more configuration parameters;
 *				dropped obsolete/unused parameters.
 *	2012-12-11 GH;		Implement forcibly closing idle connections.
 */

#include "bronx.h"
#include "bronx_config.h"
#include "bronx_listener.h"
#include "bronx_listener_common.h"
#include "bronx_listener_defines.h"
#include "bronx_log.h"
#include "bronx_utils.h"
#include "bronx_cmd_acceptor.h"
#include "bronx_safe_fork.h"

/*
 *  Globals
 */

configuration_criteria  *_configuration;

/*
 *  parse_args
 *
 *  Parse argument set.
 */

int
parse_args(configuration_criteria *config, char *args)
{
    char    token[128], *state, *tok, *a;

    config->config_filename[0] = '\0';

    if (args == NULL)
	return(TRUE);

    for(a=args; *a && *a == ' '; a++)
	;

    if (*a == '\0')
	return(TRUE);

    if ((tok = apr_strtok(a, " ", &state)))
    {
	do
	{
	    strcpy(token, tok);
	    apr_collapse_spaces(token, token);

	    if (token[0] == '-')
	    {
		switch (token[1])
		{
		    case 'c':
			strcpy(config->config_filename, &token[2]);
			break;
		    case 'p':
			// Set startup pause timer.
			// Note that setting this to a non-zero value may cause data loss
			// (results submitted during the pause time will be silently dropped),
			// which is probably not something you want.
			config->startup_pause_timer = atoi(&token[2]);
			break;
#ifdef INCLUDE_OBSOLETE_OPTIONS
		    case 'C':
			if (!strncmp(&token[2], "true", 4) || !strncmp(&token[2], "on", 2))
			    config->consolidation = 1;
			else if (!strncmp(&token[2], "false", 4)|| !strncmp(&token[2], "off", 3))
			    config->consolidation = 0;
			break;
#endif
		    default:
			bronx_logprintf(BRONX_LOGGING_WARNING, "{parse_args} Unknown argument '%c'", token[1]);
			break;
		}
	    }
	    else
		strcpy(config->config_filename, token);

	} while ((tok = apr_strtok(NULL, " ", &state)));
    }

    return(TRUE);
}

void
config_dump(configuration_criteria *config)
{

}

int
count_chars(char *str, char find)
{
    int count;

    for(count=0; *str; str++)
        if (*str == find)
            ++count;

    return(count);
}

void
config_set_defaults(configuration_criteria *config)
{
    // Initialize Default Set.

    /*
     * Generic Defaults
     */
    strncpy( config->config_filename, "/usr/local/groundwork/config/bronx.cfg", sizeof( config->config_filename ) - 1 );
    config->config_filename [ sizeof( config->config_filename ) - 1 ] = '\0';

    config->listener = 1;

    strncpy( config->database_dir, "/usr/local/groundwork/var/spool", sizeof( config->database_dir ) - 1 );
    config->database_dir [ sizeof( config->database_dir ) - 1 ] = '\0';

    config->startup_pause_timer = 0;

    // The max_client_connections setting works in conjunction with the reserved_file_descriptor_count setting
    // to limit the total number of client connections.  max_client_connections provides an absolute limit;
    // reserved_file_descriptor_count provides a limit which is relative to the total number of file descriptors
    // available to the process.  The client-connection limit will be determined by the lower of the two values
    // represented by these settings.  For instance, in a typical Linux system, the max number of file descriptors
    // available to a process defaults to 1024.  In that context, if reserved_file_descriptor_count is 150, no
    // more than 874 (=1024-150) client connections can be made.  In that same context, if max_client_connections
    // is 500, the limit is further reduced to 500 concurrent open client connections.
    //
    // These two parameters act as failsafe limits.  In practice, in a well-run system, all the clients will close
    // their sockets immediately after use, and there will be no buildup of open client-connection sockets on the
    // server.  In such a system, you might see perhaps half a dozen open client connections at any one time, if
    // that.  The only reason we're setting the limits so high is to allow failure of a large number of clients to
    // close their connections, under severe operating conditions.  In that case, the server must time out idle
    // connections, during which time those open connections will be useless, so we would want to allow space for
    // other clients to continue connecting and transmitting data.
    //
    // The max_client_connections and reserved_file_descriptor_count limits are by themselves somewhat dangerous;
    // reaching the effective limit will stop Bronx from accepting any new connections.  If the clients don't
    // cooperate and close their ends of the connections, the server side will eventually fill up with idle
    // connections and data transfer will grind to a halt.  To prevent that, the server must have a way to
    // recognize a saturation condition and deal with it.
    //
    // In previous incarnations of Bronx, this would happen only when the Nagios process completely ran out of file
    // descriptors (leaving no extras for Nagios itself, which could obviously interfere with its operation).  The
    // Bronx listener socket would fail to accept() a new incoming connection because there was no file descriptor
    // available to assign to it, and that error would cascade so the listener socket itself would be closed, that
    // condition would be recognized as extraordinary, all the other client-connection sockets would be closed as
    // well (regardless of whether or not they were considered to be idle), and Bronx would internally restart by
    // re-opening the listening socket.
    //
    // With the client-connection limit now in play, we will never get to the point of an out-of-file-descriptors
    // condition, so the scenario just described will not play out and Bronx will never recover if the clients
    // don't close their ends of the open connections.  So we need another way, from the server side, to force
    // idle connections to be closed, so Bronx can continue to accept new connections and while staying under the
    // connection limit.  That is now done via the idle_connection_timeout setting, specified in seconds.  If a
    // client connection has no read or write activity seen by the server in this amount of time, the server will
    // now feel justified in closing its end of the connection.  Note that per TCP standards, this will mean that
    // the socket on the server side will remain in TIME_WAIT state, since it will be the server that performs
    // the active close in this case.  The duration of TIME_WAIT is between 1 and 4 minutes, depending on the
    // platform (see Stevens, UNIX Network Programming, Vol. 1, 3/e, pp. 43-44).
    //
    // In principle, the idle_connection_timeout might not be evaluated immediately upon its expiration; it might
    // take up to BRONX_LISTENER_POLL_TIMEOUT additional time for that to happen.  That's important to know for
    // test purposes.  But in practice, sockets to which the idle_connection_timeout applies will be processed as
    // soon as practical after the timeout expires, so there is no loss of generality.  This will typically be
    // triggered by some other client calling in to exchange data, and in the worst case it will be triggered by
    // the BRONX_LISTENER_POLL_TIMEOUT timeout we apply to all poll() calls.

    // We set the default value for max_client_connections to a fairly high value, one which ought to be much
    // greater than ever needed at any site.  The actual value needed can be calculated by taking the rate at which
    // clients fail to close connections (failed connections per second) and multiplying that by the chosen value
    // for idle_connection_timeout.  For example, if you had a system which was accepting 100 connections per
    // second, with 3.5% of those connections not being sensibly closed at the client end, and an idle timeout of
    // 30 seconds, the max client connections configured here should be a little above 105 (= 100 * .035 * 30),
    // perhaps 120 or so.
    config->max_client_connections = 500;

    // We set the default value for reserved_file_descriptor_count to a reasonably high value to ensure that
    // Nagios itself has enough file descriptors available for its own use, as well as to reflect any extra file
    // descriptors that ought to be reserved for use within Bronx outside of incoming client connections.  Since
    // we set the Nagios max_concurrent_checks directive to 100 by default in a fresh Monarch configuration,
    // Nagios may well need a lot of file descriptors to run active checks.  The standard default value for
    // the number of open files in a process under Linux is 1024, so this still leaves plenty for use by the
    // listener.  And this number can be locally adjusted if need be in the bronx.cfg file.
    //
    // If this value is set too low, Nagios might not have enough file descriptors to run active checks in parallel.
    // See these references for details:
    // General info:
    //     http://support.nagios.com/wiki/index.php/Nagios_XI:FAQs#Check_Services_Being_Orphaned
    // Nagios developer's list discussion (several articles, unfortunately not all linked together in one thread:
    //     http://thread.gmane.org/gmane.network.nagios.devel/8576
    //     http://thread.gmane.org/gmane.network.nagios.devel/8579
    //     http://thread.gmane.org/gmane.network.nagios.devel/8583
    config->reserved_file_descriptor_count = 150;

    // We set the default value for idle_connection_timeout to something that seems reasonable for a site that
    // might want to send results from a client every half-minute.  Which is to say, the timeout here should be
    // no greater than the processing interval of the clients, since if the client runs another cycle and perhaps
    // starts a new client connection, there's no good reason to keep an old idle connection from this same client
    // still hanging around.
    config->idle_connection_timeout = 30;

#ifdef INCLUDE_OBSOLETE_OPTIONS
    config->queue_size                         = 20480;
    config->soft_state_changes                 = 0;
    config->spillover_on_queue_full            = 0;
    config->seconds_between_reconnect_attempts = 1;
    config->consolidation                      = 1;
    config->tcp_socket_timeout                 = 1000000;
    config->num_aggregators                    = 0;
    config->max_plugin_output_len              = 1024;
#endif

    // The event broker logfile. All event broker log messages go to this file,
    // if we have a non-empty string configured for its filename.
    strncpy(config->log_filename, "/usr/local/groundwork/nagios/var/event_broker.log",
	sizeof(config->log_filename)-1);
    config->log_filename[sizeof(config->log_filename)-1] = '\0';

    /*
     * Listener Defaults.
     */
    config->listener_port                 = DEFAULT_NSCA_PORT;
    config->listener_password[0]          = '\0';
    config->listener_encryption_method    = ENCRYPT_XOR;
    config->listener_max_packet_age       = 30;
    config->listener_max_packet_imminence = 1;
    config->listener_address[0]           = '\0';
    config->listener_nagios_cmd_execution = BRONX_NAGIOS_CMD_DENY;
    config->listener_num_allowed_hosts    = 0;
    config->use_client_timestamp          = 1;

    /* Command acceptor defaults */
    config->cmd_acceptor_num_allowed_hosts = 0;
    config->cmd_acceptor_port              = DEFAULT_CMD_ACCEPTOR_PORT;
    /* Maximum number of simultaneous connections to handle */
    config->cmd_acceptor_max_conn          = DEFAULT_CMD_ACCEPTOR_MAX_CONN;
    config->cmd_acceptor_encryption_method = ENCRYPT_DES;
    /* Audit trail is off by default */
    config->audit_trail                    = AUDIT_TRAIL_OFF;
    /* The default encryption/decryption key is "12345678" */
    strncpy(config->cmd_acceptor_key, DEFAULT_DES_KEY, sizeof(config->cmd_acceptor_key)-1);
    config->cmd_acceptor_key[sizeof(config->cmd_acceptor_key)-1] = '\0';
    /* The nagios command pipe where the administrative commands are submitted. */
    strncpy(config->nagios_cmdpipe_filename, DEFAULT_NAGIOS_CMD_PIPE, sizeof(config->nagios_cmdpipe_filename) - 1);
    config->nagios_cmdpipe_filename[sizeof(config->nagios_cmdpipe_filename) - 1] = '\0'; 
    // Nagios commands audit trail filename.  All Nagios commands are recorded
    // in this file, if we have a non-empty string configured for its filename.
    strncpy(config->audit_trail_filename, DEFAULT_AUDIT_LOG_FILE, sizeof(config->audit_trail_filename) - 1);
    config->audit_trail_filename[sizeof(config->audit_trail_filename) - 1] = '\0'; 
}

int
read_config_file(configuration_criteria *config)
{
    FILE	*fp;
    char	input_buffer[MAX_INPUT_BUFFER],
		line_buffer[MAX_INPUT_BUFFER];
    char	*varname, *varvalue;
    int		line, i;


    /* open the config file for reading */
    if ((fp=bronx_safe_fopen(config->config_filename,"r")) == NULL)
    {
	bronx_logprintf(BRONX_LOGGING_ERROR, "{config} Could not open config file '%s' for reading",config->config_filename);
	return BRONX_ERROR;
    }

    // Initialize Defaults.

    config_set_defaults(config);

    // Now Read configuration file.

    for(line=0;fgets(input_buffer,MAX_INPUT_BUFFER-1,fp);)
    {
	line++;

	//
	//  Skip Whitespaces
	//

	for(i=0; input_buffer[i] != '\0' && i < MAX_INPUT_BUFFER; i++)
	    if (input_buffer[i] != ' ' && input_buffer[i] != '\t' && input_buffer[i] != '\r' && input_buffer[i] != '\n')
		break;

	strcpy(line_buffer, &input_buffer[i]);

	//
	//  Skip Comments and blank lines.
	//

	if(line_buffer[0]=='#' || line_buffer[0] == '\0')
	    continue;

	//
	//  Here if we have a real line.
	//

	/* get the variable name */
	varname=strtok(line_buffer,"=");
	if(varname==NULL)
	{
	    bronx_logprintf(BRONX_LOGGING_WARNING, "{config} No variable name specified in config file '%s', line %d.",
		config->config_filename,line);
	    return BRONX_ERROR;
	}

	/* get the variable value */
	varvalue=strtok(NULL,"\n");
	if (varvalue==NULL)
	{
	    bronx_logprintf(BRONX_LOGGING_WARNING, "{config} No variable value specified in config file '%s', line %d.",
		config->config_filename,line);
	    return BRONX_ERROR;
	}

	if(!strcmp(varname,"listener"))
	{
	    apr_collapse_spaces(varvalue, varvalue);
	    if (!strncmp(varvalue, "true", 4) || !strncmp(varvalue, "on", 2))
		config->listener = 1;
	    else if (!strncmp(varvalue, "false", 4)|| !strncmp(varvalue, "off", 3))
		config->listener = 0;
	    else
	    {
		bronx_logprintf(BRONX_LOGGING_WARNING,"{config} Incorrect boolean specification in file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	}
#ifdef INCLUDE_OBSOLETE_OPTIONS
	else if (!strcmp(varname, "queue_size"))
	{
	    config->queue_size = atoi(varvalue);
	}
	else if(!strcmp(varname, "soft_state_changes"))
	{
	    if (!strncmp(varvalue, "true", 4) || !strncmp(varvalue, "on", 2))
		config->soft_state_changes = 1;
	    else if (!strncmp(varvalue, "false", 4)|| !strncmp(varvalue, "off", 3))
		config->soft_state_changes = 0;
	    else
	    {
		bronx_logprintf(BRONX_LOGGING_WARNING,"{config} Incorrect boolean specification in file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname, "spillover_on_queue_full"))
	{
	    if (!strncmp(varvalue, "true", 4) || !strncmp(varvalue, "on", 2))
		config->spillover_on_queue_full = 1;
	    else if (!strncmp(varvalue, "false", 4)|| !strncmp(varvalue, "off", 3))
		config->spillover_on_queue_full = 0;
	    else
	    {
		bronx_logprintf(BRONX_LOGGING_WARNING,"{config} Incorrect boolean specification in file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	}
	else if (!strcmp(varname, "seconds_between_reconnect_attempts"))
	{
	    config->seconds_between_reconnect_attempts = atoi(varvalue);
	}
	else if (!strcmp(varname, "consolidation"))
	{
	    if (!strncmp(varvalue, "true", 4) || !strncmp(varvalue, "on", 2))
		config->consolidation = 1;
	    else if (!strncmp(varvalue, "false", 4)|| !strncmp(varvalue, "off", 3))
		config->consolidation = 0;
	    else
	    {
		bronx_logprintf(BRONX_LOGGING_WARNING,"{config} Incorrect boolean specification in file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname, "tcp_socket_timeout"))
	{
	    config->tcp_socket_timeout=atoi(varvalue);
	}
#endif
	else if(!strcmp(varname, "logging"))
	{
	    apr_collapse_spaces(varvalue, varvalue);
	    /* The error and warning log messages go to the nagios.log file, if they are enabled here. */
	    /* All event broker messages enabled here go to the event broker log file, if we have one. */
	    if (!strncmp(varvalue, "error", strlen("error")))
		log_setlevel(BRONX_LOGGING_ERROR);
	    else if (!strncmp(varvalue, "warning", strlen("warning")))
		log_setlevel(BRONX_LOGGING_WARNING);
	    /* The info and debug logs go to event broker log file */
	    else if (!strncmp(varvalue, "commands", strlen("commands")))
		log_setlevel(BRONX_LOGGING_COMMANDS);
	    else if (!strncmp(varvalue, "passive_checks", strlen("passive_checks")))
		log_setlevel(BRONX_LOGGING_PASSIVE_CHECKS);
	    else if (!strncmp(varvalue, "info", strlen("info")))
		log_setlevel(BRONX_LOGGING_INFO);
	    else if (!strncmp(varvalue, "debug", strlen("debug")))
		log_setlevel(BRONX_LOGGING_DEBUG);
	    else if (!strncmp(varvalue, "develop", strlen("develop")))
		log_setlevel(BRONX_LOGGING_DEVELOP);
	    else
	    {
		bronx_logprintf(BRONX_LOGGING_ERROR,"{config} Invalid logging specification in file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname, "database_dir"))
	{
	    strncpy(config->database_dir,varvalue,sizeof(config->database_dir) - 1);
	    config->database_dir[sizeof(config->database_dir)-1]='\0';
	}
	else if (!strcmp(varname, "startup_pause_timer"))
	{
	    config->startup_pause_timer = atoi(varvalue);
	}
	else if(!strcmp(varname, "listener_port"))
	{
	    config->listener_port=atoi(varvalue);
	    if((config->listener_port<1024 && (geteuid()!=0)) || config->listener_port<0)
	    {
		bronx_logprintf(BRONX_LOGGING_ERROR,"{config} Invalid port number specified in config file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname,"listener_address"))
	{
	    apr_collapse_spaces(varvalue, varvalue);
	    if (strncmp(varvalue, "<automatic>", 11))
	    {
		strncpy(config->listener_address,varvalue,sizeof(config->listener_address) - 1);
		config->listener_address[sizeof(config->listener_address)-1]='\0';
	    }
	}
	else if(!strcmp(varname, "listener_allowed_hosts"))
	{
	    char    *token;
	    int     i;

	    apr_collapse_spaces(varvalue, varvalue);
	    for(i=0, token = strtok(varvalue, ","); token != NULL && i < BRONX_CONFIG_NUM_ALLOWED_HOST_RANGES; i++)
	    {
		strncpy(config->listener_allowed_hosts[i], token, sizeof(config->listener_allowed_hosts[i])-1);
		config->listener_allowed_hosts[i][sizeof(config->listener_allowed_hosts[i])-1]='\0';
		++config->listener_num_allowed_hosts;
		token = strtok( NULL, ",");
	    }
	}
	else if (!strcmp(varname, "listener_nagios_cmd_execution"))
	{
	    apr_collapse_spaces(varvalue, varvalue);
	    if (!strcmp(varvalue, "deny"))
		    config->listener_nagios_cmd_execution = BRONX_NAGIOS_CMD_DENY;
	    else if (!strcmp(varvalue, "allow"))
		    config->listener_nagios_cmd_execution = BRONX_NAGIOS_CMD_ALLOW;
	    else if (!strcmp(varvalue, "password"))
		    config->listener_nagios_cmd_execution = BRONX_NAGIOS_CMD_PASSWD_ALLOW;
	    else
	    {
		bronx_logprintf(BRONX_LOGGING_ERROR,
		    "{config} listener_nagios_command_execution must be 'deny'/'allow' or 'password' in config file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname,"listener_password"))
	{
	    if(strlen(varvalue)>sizeof(config->listener_password)-1)
	    {
		bronx_logprintf(BRONX_LOGGING_ERROR,"{config} Password is too long in config file '%s', line %d.",
		    config->config_filename,line);
		return BRONX_ERROR;
	    }
	    if (strncmp(varvalue, "<none>", 6))
	    {
		strncpy(config->listener_password,varvalue,sizeof(config->listener_password)-1);
		config->listener_password[sizeof(config->listener_password)-1]='\0';
	    }
	}
	else if(!strcmp(varname, "max_client_connections"))
	{
	    config->max_client_connections=atoi(varvalue);
	    // We ensure that the limit cannot be set so low as to limit Bronx itself from ordinary operation.
	    if (config->max_client_connections < 20)
	    {
		bronx_logprintf(BRONX_LOGGING_WARNING,"{config} max_client_connections must be at least 20 (config file '%s', line %d).",
		    config->config_filename,line);
		config->max_client_connections = 20;
	    }
	}
	else if(!strcmp(varname, "reserved_file_descriptor_count"))
	{
	    config->reserved_file_descriptor_count=atoi(varvalue);
	    // We ensure that the limit cannot be set so low as to run Nagios (or Bronx itself) completely into the ground.
	    if (config->reserved_file_descriptor_count < 20)
	    {
		bronx_logprintf(BRONX_LOGGING_WARNING,"{config} reserved_file_descriptor_count must be at least 20 (config file '%s', line %d).",
		    config->config_filename,line);
		config->reserved_file_descriptor_count = 20;
	    }
	}
	else if(!strcmp(varname, "idle_connection_timeout"))
	{
	    config->idle_connection_timeout=atoi(varvalue);
	    // We ensure that the timeout cannot be set so low as to prevent Bronx itself from operating normally with fast clients.
	    if (config->idle_connection_timeout < 5)
	    {
		bronx_logprintf(BRONX_LOGGING_WARNING,"{config} idle_connection_timeout must be at least 5 (config file '%s', line %d).",
		    config->config_filename,line);
		config->idle_connection_timeout = 5;
	    }
	}
	else if(strstr(varname,"listener_encryption_method"))
	{
	    config->listener_encryption_method=atoi(varvalue);

	    switch(config->listener_encryption_method)
	    {
		case ENCRYPT_NONE:
		case ENCRYPT_XOR:
		    break;
		case ENCRYPT_DES:
		case ENCRYPT_3DES:
		case ENCRYPT_CAST128:
		case ENCRYPT_CAST256:
		case ENCRYPT_XTEA:
		case ENCRYPT_3WAY:
		case ENCRYPT_BLOWFISH:
		case ENCRYPT_TWOFISH:
		case ENCRYPT_LOKI97:
		case ENCRYPT_RC2:
		case ENCRYPT_ARCFOUR:
		case ENCRYPT_RIJNDAEL128:
		case ENCRYPT_RIJNDAEL192:
		case ENCRYPT_RIJNDAEL256:
		case ENCRYPT_WAKE:
		case ENCRYPT_SERPENT:
		case ENCRYPT_ENIGMA:
		case ENCRYPT_GOST:
		case ENCRYPT_SAFER64:
		case ENCRYPT_SAFER128:
		case ENCRYPT_SAFERPLUS:
		    break;
		default:
		    bronx_logprintf(BRONX_LOGGING_ERROR,"{config} Invalid decryption method (%d) in config file '%s', line %d.",
			config->listener_encryption_method,config->config_filename,line);
		    if(config->listener_encryption_method>=2)
			bronx_log("{config} Daemon was not compiled with mcrypt library, so decryption is unavailable.",
			    BRONX_LOGGING_ERROR);
		    return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname,"listener_max_packet_age"))
	{
	    config->listener_max_packet_age=strtoul(varvalue,NULL,10);
	    if(config->listener_max_packet_age>900)
	    {
		bronx_log("{config} Max packet age cannot be greater than 15 minutes (900 seconds).", BRONX_LOGGING_ERROR);
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname,"listener_max_packet_imminence"))
	{
	    config->listener_max_packet_imminence=strtoul(varvalue,NULL,10);
	    if(config->listener_max_packet_imminence>900)
	    {
		bronx_log("{config} Max packet imminence cannot be greater than 15 minutes (900 seconds).", BRONX_LOGGING_ERROR);
		return BRONX_ERROR;
	    }
	}
#ifdef INCLUDE_OBSOLETE_OPTIONS
	else if(!strcmp(varname, "max_plugin_output_len"))
	{
	    config->max_plugin_output_len = atoi(varvalue);
	}
#endif
	else if(!strcmp(varname, "log_filename"))
	{
	    // Note in particular here that an empty pathname is valid, and such a setup will
	    // suppress all event broker messages from ever appearing in a separate logfile for
	    // the event broker.  However, whatever error and warning messages are generated and
	    // permitted by the configured "logging" level will still appear in the nagios.log file.
	    apr_collapse_spaces(varvalue, varvalue);
	    strncpy(config->log_filename,varvalue,sizeof(config->log_filename)-1);
	    config->log_filename[sizeof(config->log_filename)-1]='\0';
	}
	else if(!strcmp(varname, "use_client_timestamp"))
	{
	    config->use_client_timestamp = atoi(varvalue);
	}
	else if(!strcmp(varname, "cmd_acceptor_port"))
	{
	    config->cmd_acceptor_port = atoi(varvalue);
	    if(config->cmd_acceptor_port < 1024 || config->cmd_acceptor_port > 65535)
	    {
		bronx_log("{config} The port number should be between 1024 and 65535. Using default port.", BRONX_LOGGING_ERROR);
		/* Reset the default port */ 
		config->cmd_acceptor_port = DEFAULT_CMD_ACCEPTOR_PORT;
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname, "cmd_acceptor_max_conn"))
	{
	    /* Maximum number of simultaneous connections to be serviced */ 
	    config->cmd_acceptor_max_conn = atoi(varvalue);
	    if(config->cmd_acceptor_max_conn <= 0)
	    {
		bronx_log("{config} Invalid value for cmd_acceptor_max_conn. Using default value.", BRONX_LOGGING_ERROR);
		config->cmd_acceptor_max_conn = DEFAULT_CMD_ACCEPTOR_MAX_CONN;
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname, "cmd_acceptor_encryption_method"))
	{
	    apr_collapse_spaces(varvalue, varvalue);
	    if(!strcmp(varvalue, "DES"))
	    {
	      /* DES is the default encryption method */
	    }
	    else if (!strcmp(varvalue, "none"))
	    {
		config->cmd_acceptor_encryption_method = ENCRYPT_NONE;
	    }
	    else
	    {
		bronx_log("{config} The encryption method is not supported", BRONX_LOGGING_ERROR);
		return BRONX_ERROR;
	    }
	}
	else if(!strcmp(varname, "cmd_acceptor_key"))
	{
	    if(strlen(varvalue) > sizeof(config->cmd_acceptor_key) - 1)
	    {
		bronx_logprintf(BRONX_LOGGING_ERROR,"{config} The command acceptor key is too long '%s', line %d.",
			       config->config_filename, line);
		return BRONX_ERROR;
	    }

	    strncpy(config->cmd_acceptor_key, varvalue, sizeof(config->cmd_acceptor_key) - 1);
	    config->cmd_acceptor_key[sizeof(config->cmd_acceptor_key) - 1] = '\0';
	}
	else if(!strcmp(varname, "cmd_acceptor_allowed_hosts"))
	{
	    char    *token;
	    int     i;

	    /* Read all the names and patterns into an array. The patterns will be dealt with later. */ 
	    apr_collapse_spaces(varvalue, varvalue);
	    for(i = 0, token = strtok(varvalue, ","); token != NULL; i++)
	    {
		strncpy(config->cmd_acceptor_allowed_hosts[i], token, (sizeof(config->cmd_acceptor_allowed_hosts[i]) - 1));
		config->cmd_acceptor_allowed_hosts[i][sizeof(config->cmd_acceptor_allowed_hosts[i]) - 1] = '\0';
		++config->cmd_acceptor_num_allowed_hosts;
		token = strtok(NULL, ",");
	    }
	}
	else if(!strcmp(varname, "audit"))
	{
	    /*
	     * Command acceptor audit trail.
	     * The default setting is "off".
	     */
	    apr_collapse_spaces(varvalue, varvalue);
	    if (!strcmp(varvalue, "on"))
	    {   
		config->audit_trail = AUDIT_TRAIL_ON;
	    }
	}
	else if(!strcmp(varname, "nagios_cmdpipe_filename"))
	{
	    /* The absolute path and filename of nagios command pipe */ 
	    apr_collapse_spaces(varvalue, varvalue);
	    strncpy(config->nagios_cmdpipe_filename, varvalue, (sizeof(config->nagios_cmdpipe_filename) - 1));
	    config->nagios_cmdpipe_filename[sizeof(config->nagios_cmdpipe_filename) - 1] = '\0';
	}
	else if(!strcmp(varname, "audit_trail_filename"))
	{
	    /* The absolute path and filename of the audit log */ 
	    apr_collapse_spaces(varvalue, varvalue);
	    strncpy(config->audit_trail_filename, varvalue, (sizeof(config->audit_trail_filename) - 1));
	    config->audit_trail_filename[sizeof(config->audit_trail_filename) - 1] = '\0';
	}
	else
	{
	    bronx_logprintf(BRONX_LOGGING_ERROR,"{config} Unknown option specified in config file '%s', line %d.",
		config->config_filename,line);
	    return BRONX_ERROR;
	}
    }

    /* close the config file */
    bronx_safe_fclose(fp);
    return BRONX_OK;
}
