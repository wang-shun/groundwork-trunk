/*
 *  bronx_admin.c -- Message marshalling functionality.
 *
 *  Copyright (c) 2007-2017 Groundwork Open Source
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
 *	2007-09-17 DEF;	Created.
 *	2017-01-29 GH;	Updated to support Nagios 4.2.4, and to advise on the
 *			unadvisability of using this facility in its present form.
 */

#include "bronx.h"
#include "bronx_admin.h"
#include "bronx_config.h"
#include "bronx_log.h"
#include "bronx_utils.h"

/*
 *  Functionality.
 */

void admin_execute_nagios_command(char *buffer)
{
        char command_id[MAX_INPUT_BUFFER];
        char args[MAX_INPUT_BUFFER];
        time_t entry_time;
        int command_type=CMD_NONE;
        char *temp_ptr;

	strip(buffer);

	bronx_logprintf(BRONX_LOGGING_DEBUG,"{admin_execute_nagios_command} Executing nagios command: '%s'", buffer);

                /* get the command entry time */
                temp_ptr=my_strtok(buffer,"[");
                if(temp_ptr==NULL)
			return;
                temp_ptr=my_strtok(NULL,"]");
                if(temp_ptr==NULL)
			return;
                entry_time=(time_t)strtoul(temp_ptr,NULL,10);

                /* get the command identifier */
                temp_ptr=my_strtok(NULL,";");
                if(temp_ptr==NULL)
			return;
                strncpy(command_id,temp_ptr+1,sizeof(command_id)-1);
                command_id[sizeof(command_id)-1]='\x0';

                /* get the command arguments */
                temp_ptr=my_strtok(NULL,"\n");
                if(temp_ptr==NULL)
                        strcpy(args,"");
                else{
                        strncpy(args,temp_ptr,sizeof(args)-1);
                        args[sizeof(args)-1]='\x0';
                }

                /**************************/
                /**** PROCESS COMMANDS ****/
                /**************************/

                if(!strcmp(command_id,"ENTER_STANDBY_MODE") || !strcmp(command_id,"DISABLE_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_NOTIFICATIONS;
                else if(!strcmp(command_id,"ENTER_ACTIVE_MODE") || !strcmp(command_id,"ENABLE_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_NOTIFICATIONS;

                else if(!strcmp(command_id,"SHUTDOWN_PROGRAM"))
                        command_type=CMD_SHUTDOWN_PROCESS;
                else if(!strcmp(command_id,"RESTART_PROGRAM"))
                        command_type=CMD_RESTART_PROCESS;

                else if(!strcmp(command_id,"SAVE_STATE_INFORMATION"))
                        command_type=CMD_SAVE_STATE_INFORMATION;
                else if(!strcmp(command_id,"READ_STATE_INFORMATION"))
                        command_type=CMD_READ_STATE_INFORMATION;

                else if(!strcmp(command_id,"ENABLE_EVENT_HANDLERS"))
                        command_type=CMD_ENABLE_EVENT_HANDLERS;
                else if(!strcmp(command_id,"DISABLE_EVENT_HANDLERS"))
                        command_type=CMD_DISABLE_EVENT_HANDLERS;

#ifndef NAGIOS_4_2_4_OR_LATER
                else if(!strcmp(command_id,"FLUSH_PENDING_COMMANDS"))
                        command_type=CMD_FLUSH_PENDING_COMMANDS;

                else if(!strcmp(command_id,"ENABLE_FAILURE_PREDICTION"))
                        command_type=CMD_ENABLE_FAILURE_PREDICTION;
                else if(!strcmp(command_id,"DISABLE_FAILURE_PREDICTION"))
                        command_type=CMD_DISABLE_FAILURE_PREDICTION;
#endif

                else if(!strcmp(command_id,"ENABLE_PERFORMANCE_DATA"))
                        command_type=CMD_ENABLE_PERFORMANCE_DATA;
                else if(!strcmp(command_id,"DISABLE_PERFORMANCE_DATA"))
                        command_type=CMD_DISABLE_PERFORMANCE_DATA;

                else if(!strcmp(command_id,"START_EXECUTING_HOST_CHECKS"))
                        command_type=CMD_START_EXECUTING_HOST_CHECKS;
                else if(!strcmp(command_id,"STOP_EXECUTING_HOST_CHECKS"))
                        command_type=CMD_STOP_EXECUTING_HOST_CHECKS;

                else if(!strcmp(command_id,"START_EXECUTING_SVC_CHECKS"))
                        command_type=CMD_START_EXECUTING_SVC_CHECKS;
                else if(!strcmp(command_id,"STOP_EXECUTING_SVC_CHECKS"))
                        command_type=CMD_STOP_EXECUTING_SVC_CHECKS;
               else if(!strcmp(command_id,"START_ACCEPTING_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS;
                else if(!strcmp(command_id,"STOP_ACCEPTING_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS;

                else if(!strcmp(command_id,"START_ACCEPTING_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS;
                else if(!strcmp(command_id,"STOP_ACCEPTING_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS;

                else if(!strcmp(command_id,"START_OBSESSING_OVER_HOST_CHECKS"))
                        command_type=CMD_START_OBSESSING_OVER_HOST_CHECKS;
                else if(!strcmp(command_id,"STOP_OBSESSING_OVER_HOST_CHECKS"))
                        command_type=CMD_STOP_OBSESSING_OVER_HOST_CHECKS;

                else if(!strcmp(command_id,"START_OBSESSING_OVER_SVC_CHECKS"))
                        command_type=CMD_START_OBSESSING_OVER_SVC_CHECKS;
                else if(!strcmp(command_id,"STOP_OBSESSING_OVER_SVC_CHECKS"))
                        command_type=CMD_STOP_OBSESSING_OVER_SVC_CHECKS;

                else if(!strcmp(command_id,"ENABLE_FLAP_DETECTION"))
                        command_type=CMD_ENABLE_FLAP_DETECTION;
                else if(!strcmp(command_id,"DISABLE_FLAP_DETECTION"))
                        command_type=CMD_DISABLE_FLAP_DETECTION;

                else if(!strcmp(command_id,"CHANGE_GLOBAL_HOST_EVENT_HANDLER"))
                        command_type=CMD_CHANGE_GLOBAL_HOST_EVENT_HANDLER;
                else if(!strcmp(command_id,"CHANGE_GLOBAL_SVC_EVENT_HANDLER"))
                        command_type=CMD_CHANGE_GLOBAL_SVC_EVENT_HANDLER;

                else if(!strcmp(command_id,"ENABLE_SERVICE_FRESHNESS_CHECKS"))
                        command_type=CMD_ENABLE_SERVICE_FRESHNESS_CHECKS;
                else if(!strcmp(command_id,"DISABLE_SERVICE_FRESHNESS_CHECKS"))
                        command_type=CMD_DISABLE_SERVICE_FRESHNESS_CHECKS;

                else if(!strcmp(command_id,"ENABLE_HOST_FRESHNESS_CHECKS"))
                        command_type=CMD_ENABLE_HOST_FRESHNESS_CHECKS;
                else if(!strcmp(command_id,"DISABLE_HOST_FRESHNESS_CHECKS"))
                        command_type=CMD_DISABLE_HOST_FRESHNESS_CHECKS;


                /*******************************/
                /**** HOST-RELATED COMMANDS ****/
                /*******************************/
                else if(!strcmp(command_id,"ADD_HOST_COMMENT"))
                        command_type=CMD_ADD_HOST_COMMENT;
                else if(!strcmp(command_id,"DEL_HOST_COMMENT"))
                        command_type=CMD_DEL_HOST_COMMENT;
                else if(!strcmp(command_id,"DEL_ALL_HOST_COMMENTS"))
                        command_type=CMD_DEL_ALL_HOST_COMMENTS;

                else if(!strcmp(command_id,"DELAY_HOST_NOTIFICATION"))
                        command_type=CMD_DELAY_HOST_NOTIFICATION;

                else if(!strcmp(command_id,"ENABLE_HOST_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_HOST_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_HOST_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_HOST_NOTIFICATIONS;

                else if(!strcmp(command_id,"ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST"))
                        command_type=CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST;
                else if(!strcmp(command_id,"DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST"))
                        command_type=CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST;

                else if(!strcmp(command_id,"ENABLE_HOST_AND_CHILD_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_HOST_AND_CHILD_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_HOST_AND_CHILD_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_HOST_AND_CHILD_NOTIFICATIONS;

                else if(!strcmp(command_id,"ENABLE_HOST_SVC_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_HOST_SVC_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_HOST_SVC_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_HOST_SVC_NOTIFICATIONS;

                else if(!strcmp(command_id,"ENABLE_HOST_SVC_CHECKS"))
                        command_type=CMD_ENABLE_HOST_SVC_CHECKS;
                else if(!strcmp(command_id,"DISABLE_HOST_SVC_CHECKS"))
                        command_type=CMD_DISABLE_HOST_SVC_CHECKS;

                else if(!strcmp(command_id,"ENABLE_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_ENABLE_PASSIVE_HOST_CHECKS;
                else if(!strcmp(command_id,"DISABLE_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_DISABLE_PASSIVE_HOST_CHECKS;

                else if(!strcmp(command_id,"SCHEDULE_HOST_SVC_CHECKS"))
                        command_type=CMD_SCHEDULE_HOST_SVC_CHECKS;
                else if(!strcmp(command_id,"SCHEDULE_FORCED_HOST_SVC_CHECKS"))
                        command_type=CMD_SCHEDULE_FORCED_HOST_SVC_CHECKS;

                else if(!strcmp(command_id,"ACKNOWLEDGE_HOST_PROBLEM"))
                        command_type=CMD_ACKNOWLEDGE_HOST_PROBLEM;
                else if(!strcmp(command_id,"REMOVE_HOST_ACKNOWLEDGEMENT"))
                        command_type=CMD_REMOVE_HOST_ACKNOWLEDGEMENT;
                else if(!strcmp(command_id,"ENABLE_HOST_EVENT_HANDLER"))
                        command_type=CMD_ENABLE_HOST_EVENT_HANDLER;
                else if(!strcmp(command_id,"DISABLE_HOST_EVENT_HANDLER"))
                        command_type=CMD_DISABLE_HOST_EVENT_HANDLER;

                else if(!strcmp(command_id,"ENABLE_HOST_CHECK"))
                        command_type=CMD_ENABLE_HOST_CHECK;
                else if(!strcmp(command_id,"DISABLE_HOST_CHECK"))
                        command_type=CMD_DISABLE_HOST_CHECK;

                else if(!strcmp(command_id,"SCHEDULE_HOST_CHECK"))
                        command_type=CMD_SCHEDULE_HOST_CHECK;
                else if(!strcmp(command_id,"SCHEDULE_FORCED_HOST_CHECK"))
                        command_type=CMD_SCHEDULE_FORCED_HOST_CHECK;

                else if(!strcmp(command_id,"SCHEDULE_HOST_DOWNTIME"))
                        command_type=CMD_SCHEDULE_HOST_DOWNTIME;
                else if(!strcmp(command_id,"SCHEDULE_HOST_SVC_DOWNTIME"))
                        command_type=CMD_SCHEDULE_HOST_SVC_DOWNTIME;
                else if(!strcmp(command_id,"DEL_HOST_DOWNTIME"))
                        command_type=CMD_DEL_HOST_DOWNTIME;

                else if(!strcmp(command_id,"ENABLE_HOST_FLAP_DETECTION"))
                        command_type=CMD_ENABLE_HOST_FLAP_DETECTION;
                else if(!strcmp(command_id,"DISABLE_HOST_FLAP_DETECTION"))
                        command_type=CMD_DISABLE_HOST_FLAP_DETECTION;

                else if(!strcmp(command_id,"START_OBSESSING_OVER_HOST"))
                        command_type=CMD_START_OBSESSING_OVER_HOST;
                else if(!strcmp(command_id,"STOP_OBSESSING_OVER_HOST"))
                        command_type=CMD_STOP_OBSESSING_OVER_HOST;

                else if(!strcmp(command_id,"CHANGE_HOST_EVENT_HANDLER"))
                        command_type=CMD_CHANGE_HOST_EVENT_HANDLER;
                else if(!strcmp(command_id,"CHANGE_HOST_CHECK_COMMAND"))
                        command_type=CMD_CHANGE_HOST_CHECK_COMMAND;

                else if(!strcmp(command_id,"CHANGE_NORMAL_HOST_CHECK_INTERVAL"))
                        command_type=CMD_CHANGE_NORMAL_HOST_CHECK_INTERVAL;

                else if(!strcmp(command_id,"CHANGE_MAX_HOST_CHECK_ATTEMPTS"))
                        command_type=CMD_CHANGE_MAX_HOST_CHECK_ATTEMPTS;

                else if(!strcmp(command_id,"SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME"))
                        command_type=CMD_SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME;

                else if(!strcmp(command_id,"SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME"))
                        command_type=CMD_SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME;
                else if(!strcmp(command_id,"SET_HOST_NOTIFICATION_NUMBER"))
                        command_type=CMD_SET_HOST_NOTIFICATION_NUMBER;


                /************************************/
                /**** HOSTGROUP-RELATED COMMANDS ****/
                /************************************/

                else if(!strcmp(command_id,"ENABLE_HOSTGROUP_HOST_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_HOSTGROUP_HOST_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS;

                else if(!strcmp(command_id,"ENABLE_HOSTGROUP_SVC_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_HOSTGROUP_SVC_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS;

                else if(!strcmp(command_id,"ENABLE_HOSTGROUP_HOST_CHECKS"))
                        command_type=CMD_ENABLE_HOSTGROUP_HOST_CHECKS;
                else if(!strcmp(command_id,"DISABLE_HOSTGROUP_HOST_CHECKS"))
                        command_type=CMD_DISABLE_HOSTGROUP_HOST_CHECKS;

                else if(!strcmp(command_id,"ENABLE_HOSTGROUP_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_ENABLE_HOSTGROUP_PASSIVE_HOST_CHECKS;
                else if(!strcmp(command_id,"DISABLE_HOSTGROUP_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_DISABLE_HOSTGROUP_PASSIVE_HOST_CHECKS;

                else if(!strcmp(command_id,"ENABLE_HOSTGROUP_SVC_CHECKS"))
                        command_type=CMD_ENABLE_HOSTGROUP_SVC_CHECKS;
                else if(!strcmp(command_id,"DISABLE_HOSTGROUP_SVC_CHECKS"))
                        command_type=CMD_DISABLE_HOSTGROUP_SVC_CHECKS;

                else if(!strcmp(command_id,"ENABLE_HOSTGROUP_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_ENABLE_HOSTGROUP_PASSIVE_SVC_CHECKS;
                else if(!strcmp(command_id,"DISABLE_HOSTGROUP_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_DISABLE_HOSTGROUP_PASSIVE_SVC_CHECKS;

                else if(!strcmp(command_id,"SCHEDULE_HOSTGROUP_HOST_DOWNTIME"))
                        command_type=CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME;
                else if(!strcmp(command_id,"SCHEDULE_HOSTGROUP_SVC_DOWNTIME"))
                        command_type=CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME;


                /**********************************/
                /**** SERVICE-RELATED COMMANDS ****/
                /**********************************/
                else if(!strcmp(command_id,"ADD_SVC_COMMENT"))
                        command_type=CMD_ADD_SVC_COMMENT;
                else if(!strcmp(command_id,"DEL_SVC_COMMENT"))
                        command_type=CMD_DEL_SVC_COMMENT;
                else if(!strcmp(command_id,"DEL_ALL_SVC_COMMENTS"))
                        command_type=CMD_DEL_ALL_SVC_COMMENTS;

                else if(!strcmp(command_id,"SCHEDULE_SVC_CHECK"))
                        command_type=CMD_SCHEDULE_SVC_CHECK;
                else if(!strcmp(command_id,"SCHEDULE_FORCED_SVC_CHECK"))
                        command_type=CMD_SCHEDULE_FORCED_SVC_CHECK;

                else if(!strcmp(command_id,"ENABLE_SVC_CHECK"))
                        command_type=CMD_ENABLE_SVC_CHECK;
                else if(!strcmp(command_id,"DISABLE_SVC_CHECK"))
                        command_type=CMD_DISABLE_SVC_CHECK;

                else if(!strcmp(command_id,"ENABLE_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_ENABLE_PASSIVE_SVC_CHECKS;
                else if(!strcmp(command_id,"DISABLE_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_DISABLE_PASSIVE_SVC_CHECKS;

                else if(!strcmp(command_id,"DELAY_SVC_NOTIFICATION"))
                        command_type=CMD_DELAY_SVC_NOTIFICATION;
                else if(!strcmp(command_id,"ENABLE_SVC_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_SVC_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_SVC_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_SVC_NOTIFICATIONS;

                else if(!strcmp(command_id,"PROCESS_SERVICE_CHECK_RESULT"))
                        command_type=CMD_PROCESS_SERVICE_CHECK_RESULT;
                else if(!strcmp(command_id,"PROCESS_HOST_CHECK_RESULT"))
                        command_type=CMD_PROCESS_HOST_CHECK_RESULT;

                else if(!strcmp(command_id,"ENABLE_SVC_EVENT_HANDLER"))
                        command_type=CMD_ENABLE_SVC_EVENT_HANDLER;
                else if(!strcmp(command_id,"DISABLE_SVC_EVENT_HANDLER"))
                        command_type=CMD_DISABLE_SVC_EVENT_HANDLER;

                else if(!strcmp(command_id,"ENABLE_SVC_FLAP_DETECTION"))
                        command_type=CMD_ENABLE_SVC_FLAP_DETECTION;
                else if(!strcmp(command_id,"DISABLE_SVC_FLAP_DETECTION"))
                        command_type=CMD_DISABLE_SVC_FLAP_DETECTION;

                else if(!strcmp(command_id,"SCHEDULE_SVC_DOWNTIME"))
                        command_type=CMD_SCHEDULE_SVC_DOWNTIME;
                else if(!strcmp(command_id,"DEL_SVC_DOWNTIME"))
                        command_type=CMD_DEL_SVC_DOWNTIME;
                else if(!strcmp(command_id,"ACKNOWLEDGE_SVC_PROBLEM"))
                        command_type=CMD_ACKNOWLEDGE_SVC_PROBLEM;
                else if(!strcmp(command_id,"REMOVE_SVC_ACKNOWLEDGEMENT"))
                        command_type=CMD_REMOVE_SVC_ACKNOWLEDGEMENT;

                else if(!strcmp(command_id,"START_OBSESSING_OVER_SVC"))
                        command_type=CMD_START_OBSESSING_OVER_SVC;
                else if(!strcmp(command_id,"STOP_OBSESSING_OVER_SVC"))
                        command_type=CMD_STOP_OBSESSING_OVER_SVC;

                else if(!strcmp(command_id,"CHANGE_SVC_EVENT_HANDLER"))
                        command_type=CMD_CHANGE_SVC_EVENT_HANDLER;
                else if(!strcmp(command_id,"CHANGE_SVC_CHECK_COMMAND"))
                        command_type=CMD_CHANGE_SVC_CHECK_COMMAND;

                else if(!strcmp(command_id,"CHANGE_NORMAL_SVC_CHECK_INTERVAL"))
                        command_type=CMD_CHANGE_NORMAL_SVC_CHECK_INTERVAL;
                else if(!strcmp(command_id,"CHANGE_RETRY_SVC_CHECK_INTERVAL"))
                        command_type=CMD_CHANGE_RETRY_SVC_CHECK_INTERVAL;

                else if(!strcmp(command_id,"CHANGE_MAX_SVC_CHECK_ATTEMPTS"))
                        command_type=CMD_CHANGE_MAX_SVC_CHECK_ATTEMPTS;

                else if(!strcmp(command_id,"SET_SVC_NOTIFICATION_NUMBER"))
                        command_type=CMD_SET_SVC_NOTIFICATION_NUMBER;


                /***************************************/
                /**** SERVICEGROUP-RELATED COMMANDS ****/
                /***************************************/

                else if(!strcmp(command_id,"ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS;

                else if(!strcmp(command_id,"ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS"))
                        command_type=CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS;
                else if(!strcmp(command_id,"DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS"))
                        command_type=CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS;

                else if(!strcmp(command_id,"ENABLE_SERVICEGROUP_HOST_CHECKS"))
                        command_type=CMD_ENABLE_SERVICEGROUP_HOST_CHECKS;
                else if(!strcmp(command_id,"DISABLE_SERVICEGROUP_HOST_CHECKS"))
                        command_type=CMD_DISABLE_SERVICEGROUP_HOST_CHECKS;

                else if(!strcmp(command_id,"ENABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_ENABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS;
                else if(!strcmp(command_id,"DISABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS"))
                        command_type=CMD_DISABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS;

                else if(!strcmp(command_id,"ENABLE_SERVICEGROUP_SVC_CHECKS"))
                        command_type=CMD_ENABLE_SERVICEGROUP_SVC_CHECKS;
                else if(!strcmp(command_id,"DISABLE_SERVICEGROUP_SVC_CHECKS"))
                        command_type=CMD_DISABLE_SERVICEGROUP_SVC_CHECKS;

                else if(!strcmp(command_id,"ENABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_ENABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS;
                else if(!strcmp(command_id,"DISABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS"))
                        command_type=CMD_DISABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS;

                else if(!strcmp(command_id,"SCHEDULE_SERVICEGROUP_HOST_DOWNTIME"))
                        command_type=CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME;
                else if(!strcmp(command_id,"SCHEDULE_SERVICEGROUP_SVC_DOWNTIME"))
                        command_type=CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME;

	bronx_logprintf(BRONX_LOGGING_DEBUG,"{admin_execute_nagios_command} Executing nagios command, type=%d: '%s'", command_type, buffer);
	// FIX MAJOR:  This is a very bad way to run an arbitrary command from a Bronx thread.
	// Note that we are calling into the Nagios internals without any sort of adjudication
	// with respect to race conditions in accessing arbitrary Nagios internal state.  To
	// make this feasible, we would need to somehow block the main Nagios thread at some
	// sequence point (https://en.wikipedia.org/wiki/Sequence_point) which is known to be
	// async-safe with respect to any state that our command might modify, run our command
	// here, and then resume the main Nagios thread.  That's a tall order.  Better would be
	// to use some other mechanism to submit the command to the main Nagios thread and have
	// it run the command, in some way that might do so at high priority.  Until and unless
	// we change the code to do so, this facility is unsafe and should not be used.
	//
	// Compare what we're trying to do here with what what we do with commands sent to the
	// Bronx command acceptor port.  After a bunch of processing, we write those commands
	// to the Nagios command pipe, and thereby let them be processed in due course by the
	// main Nagios thread.  That's completely safe, though the commands are executed at
	// normal priority instead of some possibly desired high priority.
	//
	process_external_command2(command_type,entry_time,args);
	return;
}

apr_status_t admin_execute_command(configuration_criteria *config, char *cmd)
{
    if (!strcmp(cmd, "PAUSE_PROCESSING"))
    {
        bronx_log("{admin_execute_command} PAUSING PROCESSING", BRONX_LOGGING_DEBUG);
        set_bronx_manually_paused(TRUE);
    } else if (!strcmp(cmd, "COMMENCE_PROCESSING"))
    {
        bronx_log("{admin_execute_command} COMMENCING PROCESSING", BRONX_LOGGING_DEBUG);
        set_bronx_manually_paused(FALSE);
    } else if (!strncmp(cmd, "NAGIOS_COMMAND", 14))
	{
		int allow = 0;

		bronx_log("{admin_execute_command} NAGIOS_COMMAND", BRONX_LOGGING_DEBUG);
		if (config->listener_nagios_cmd_execution != BRONX_NAGIOS_CMD_DENY)
		{
			if (config->listener_nagios_cmd_execution == BRONX_NAGIOS_CMD_PASSWD_ALLOW)
			{
				if (config->listener_password[0] != '\0')
					allow = 1;
				else
					bronx_log("{admin_execute_command} NAGIOS_COMMAND processing set to password-only mode, and no password has been provided.", BRONX_LOGGING_WARNING);
			}
			else
				allow = 1;
		}
		if (allow)
		{
			char *tok;
			int good=1;

			tok = strstr(cmd, " ");
			if (tok && strlen(tok) > 1)
			{
				while (*tok == ' ')
					++tok;

				if (*tok != '\0')
					admin_execute_nagios_command(tok);
				else
				{
					bronx_log("{admin_execute_command} Failure in tokenizing 1.", BRONX_LOGGING_WARNING);
					good = 0;
				}
			}
			else
			{
				bronx_log("{admin_execute_command} Failure in tokenizing 1.", BRONX_LOGGING_WARNING);
				good = 0;
			}

			if (!good)
				bronx_logprintf(BRONX_LOGGING_WARNING, "{admin_execute_command} NAGIOS_COMMAND requires a command string to execute");
		}
		else
			bronx_log("{admin_execute_command} Command Execution Not Allowed.", BRONX_LOGGING_WARNING);
	}

	return(APR_SUCCESS);
}
