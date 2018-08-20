/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.statusviewer.handler;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.bean.action.CommandParamsBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.actions.CompositeCommandsEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.HostActionEnum;

/**
 * This class parses the nagios command,replaces the formal parameters with the
 * actual parameters,identifies the composite commands.
 * 
 * @author shivangi_walvekar
 * 
 */
public class ActionCommandHandler {

    /**
     * ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY
     */
    private static final String ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY = "com_groundwork_portal_statusviewer_actionsPortlet_socketError";

    /**
     * LOGGER
     */
    private static final Logger LOGGER = Logger
            .getLogger(ActionCommandHandler.class.getName());

    /**
     * Constant for 'host_name'
     */
    private static final String HOST_NAME_PARAM = "<host_name>";

    /**
     * Constant for 'hostgroup_name'
     */
    private static final String HOSTGROUP_NAME_PARAM = "<hostgroup_name>";

    /**
     * Constant for 'svc_description'
     */
    private static final String SERVICE_DESC_PARAM = "<svc_description>";

    /**
     * Constant for 'servicegroup_name'
     */
    private static final String SERVICEGROUP_NAME_PARAM = "<servicegroup_name>";

    /**
     * Constant for 'bytesize_cmd'
     */
    private static final String BYTESIZE_CMD_PARAM = "<bytesize_cmd>";

    /**
     * Constant for 'user_name'
     */
    private static final String USER_NAME_PARAM = "<user_name>";

    /**
     * Constant for 'start_time'
     */
    private static final String START_TIME_PARAM = "<start_time>";

    /**
     * Constant for 'end_time'
     */
    private static final String END_TIME_PARAM = "<end_time>";

    /**
     * Constant for 'isfixed'
     */
    private static final String IS_FIXED_PARAM = "<isfixed>";

    /**
     * Constant for 'duration'
     */
    private static final String DURATION_PARAM = "<duration>";

    /**
     * Constant for 'comment_author'
     */
    private static final String COMMENT_AUTHOR_PARAM = "<comment_author>";

    /**
     * Constant for 'comment_data'
     */
    private static final String COMMENT_DATA_PARAM = "<comment_data>";

    /**
     * Constant for 'triggered_by'
     */
    private static final String TRIGGERED_BY_PARAM = "<triggered_by>";

    /**
     * Constant for 'is_send_notification'
     */
    private static final String IS_SEND_NOTIFICATION_PARAM = "<is_send_notification>";

    /**
     * Constant for 'is_persistent_comment'
     */
    private static final String IS_PERSISTENT_COMMENT_PARAM = "<is_persistent_comment>";

    /**
     * Constant for 'notification_time'
     */
    private static final String NOTIFICATION_TIME_PARAM = "<notification_time>";

    /**
     * Constant for 'scheduled_time'
     */
    private static final String SCHEDULED_TIME_PARAM = "<scheduled_time>";

    /**
     * Constant for 'plugin_state'
     */
    private static final String PLUGIN_STATE_PARAM = "<plugin_state>";

    /**
     * Constant for 'plugin_output'
     */
    private static final String PLUGIN_OUTPUT_PARAM = "<plugin_output>";

    /**
     * Constant for 'perf_data'
     */
    private static final String PERF_DATA_PARAM = "<perf_data>";

    /**
     * Constant for 'plugin_output|Perf_data'
     */
    private static final String PLUGIN_OUTPUT_PIPE_PERF_DATA = "<plugin_output>|<Perf_data>";

    // /**
    // * String constant for 'Found empty token.'
    // */
    // private static final String EMPTY_TOKEN = "Found empty token.";

    /**
     * String constant for 'bytesize_cmd token not found.'
     */
    private static final String BYTESIZE_TOKEN_ABSENT = "<bytesize_cmd> token not found.";

    /**
     * String constant for default value for bytesize_cmd parameter.
     */
    private static final String DEFAULT_BYTESIZE = "00000";

    // /**
    // * String constant for default value for bytesize_cmd parameter.
    // */
    // private static final String INVALID_BYTESIZE = "Invalid bytesize.";

    /**
     * This method parses the nagios command,replaces formal parameters with the
     * actual values read from the intermediate screen or from portlet session.
     * 
     * @param nagiosCommand
     * @return command
     * @throws GWPortalGenericException
     */
    public String parseCommand(String nagiosCommand)
            throws GWPortalGenericException {

        // final String methodName = "parseCommand() : ";
        /*
         * e.g . 'bytesize_cmd;user_name
         * ;ENABLE_HOSTGROUP_HOST_NOTIFICATIONS;hostgroup_name\n' is the command
         * template. This method should return
         * '56;admin;ENABLE_HOSTGROUP_HOST_NOTIFICATIONS;Linux Servers\n'
         */
        String command = "";
        CommandParamsBean commandParamsBean = (CommandParamsBean) FacesUtils
                .getManagedBean(Constant.COMMAND_PARAMS_MANAGED_BEAN);
        if (commandParamsBean == null) {
            // LOGGER.error(Constant.METHOD + methodName
            // + ActionHandler.NULL_COMMAND_PARAMS_BEAN);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        // Parse the nagios command template.
        if (nagiosCommand == null
                || nagiosCommand.trim().equals(Constant.EMPTY_STRING)) {
            // LOGGER.error(Constant.METHOD + methodName
            // + "Found null nagios command");
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        /*
         * Check the number of semicolons (;) in the command and loop through to
         * check for the formal parameters. The parameters in the command are
         * separated by semicolon.
         */
        StringTokenizer strTokenizer = new StringTokenizer(nagiosCommand,
                Constant.SEMICOLON);
        // Creating a StringBuffer instance,initializing it to
        // nagiosCommand
        StringBuffer strBuf = new StringBuffer(nagiosCommand);
        // Tokenize the nagiosCommand string
        while (strTokenizer.hasMoreElements()) {
            int iValue = 0;
            String token = strTokenizer.nextToken();
            /*
             * If the token contains '\n',replace it with space and trim the
             * output.
             */
            if (token.indexOf(Constant.NEWLINE_CHAR) > -1) {
                token = token.replace(Constant.NEWLINE_CHAR,
                        Constant.EMPTY_CHAR).trim();
            }
            if (token == null || token.trim().equals(Constant.EMPTY_STRING)) {
                // LOGGER.error(Constant.METHOD + methodName + EMPTY_TOKEN);
                throw new GWPortalGenericException(
                        ResourceUtils
                                .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
            }
            try {
                // Find the index of the token in strBuf
                int startIndex = strBuf.indexOf(token);
                /*
                 * endIndex is used when replacing tokens with actual values in
                 * strBuf.
                 */
                int endIndex = startIndex + token.length();
                // Check for user_name parameter.
                if (token.equals(USER_NAME_PARAM)) {
                    strBuf.replace(startIndex, endIndex, FacesUtils
                            .getLoggedInUser());
                } else
                // Check for host_name parameter.
                if (token.equals(HOST_NAME_PARAM)) {
                    String hostName = commandParamsBean.getHostName();
                    if (hostName != null) {
                        if (hostName.trim().equals(Constant.EMPTY_STRING)) {
                            return Constant.EMPTY_STRING;
                        }
                        strBuf.replace(startIndex, endIndex, hostName);
                    }
                } else
                // Check for hostgroup_name parameter.
                if (token.equals(HOSTGROUP_NAME_PARAM)) {
                    String hostGroupName = commandParamsBean.getHostGroupName();
                    if (hostGroupName != null) {
                        if (hostGroupName.trim().equals(Constant.EMPTY_STRING)) {
                            return Constant.EMPTY_STRING;
                        }
                        strBuf.replace(startIndex, endIndex, hostGroupName);
                    }
                } else
                // Check for svc_description parameter.
                if (token.equals(SERVICE_DESC_PARAM)) {
                    String serviceDesc = commandParamsBean.getServiceDesc();
                    if (serviceDesc != null) {
                        if (serviceDesc.trim().equals(Constant.EMPTY_STRING)) {
                            return Constant.EMPTY_STRING;
                        }
                        strBuf.replace(startIndex, endIndex, serviceDesc);
                    }
                } else
                // Check for servicegroup_name parameter.
                if (token.equals(SERVICEGROUP_NAME_PARAM)) {
                    String serviceGroupName = commandParamsBean
                            .getServiceGroupName();
                    if (serviceGroupName != null) {
                        if (serviceGroupName.trim().equals(
                                Constant.EMPTY_STRING)) {
                            return Constant.EMPTY_STRING;
                        }
                        strBuf.replace(startIndex, endIndex, serviceGroupName);
                    }
                } else
                // Check for start_time parameter.
                if (token.equals(START_TIME_PARAM)) {
                    if (commandParamsBean.getStartTime() != null) {
                        long unixTime = DateUtils.getUnixTime(commandParamsBean
                                .getStartTime());
                        strBuf.replace(startIndex, endIndex, String
                                .valueOf(unixTime));
                    }
                } else
                // Check for end_time parameter.
                if (token.equals(END_TIME_PARAM)) {
                    if (commandParamsBean.getEndTime() != null) {
                        long unixTime = DateUtils.getUnixTime(commandParamsBean
                                .getEndTime());
                        strBuf.replace(startIndex, endIndex, String
                                .valueOf(unixTime));
                    }
                } else
                // Check for duration parameter.
                if (token.equals(DURATION_PARAM)) {
                    // Duration is applicable only for the Flexible
                    // type and not for the fixed type.
                    if (commandParamsBean.getFixed() == 0) {
                        // Calculate total seconds for the duration
                        // entered by user.
                        if (commandParamsBean.getDurationHours() == null
                                || commandParamsBean.getDurationHours().trim()
                                        .equals(Constant.EMPTY_STRING)) {
                            // LOGGER.error(Constant.INVALID_HOURS);
                            throw new GWPortalGenericException(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_zero_hours"));
                        }
                        if (commandParamsBean.getDurationMinutes() == null
                                || commandParamsBean.getDurationMinutes()
                                        .trim().equals(Constant.EMPTY_STRING)) {
                            // LOGGER.error(Constant.INVALID_MINUTES);
                            throw new GWPortalGenericException(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_minutes"));
                        }
                        long seconds = DateUtils.getSeconds(commandParamsBean
                                .getDurationHours(), commandParamsBean
                                .getDurationMinutes());
                        strBuf.replace(startIndex, endIndex, String
                                .valueOf(seconds));
                    } else {
                        // Set duration as 0.
                        strBuf.replace(startIndex, endIndex,
                                Constant.STRING_ZERO);
                    }
                } else
                // Check for comment_author parameter.
                if (token.equals(COMMENT_AUTHOR_PARAM)) {
                    if (commandParamsBean.getAuthorName() != null) {
                        strBuf.replace(startIndex, endIndex, commandParamsBean
                                .getAuthorName());
                    }
                } else
                // Check for comment_data parameter.
                if (token.equals(COMMENT_DATA_PARAM)) {
                    if (commandParamsBean.getComment() != null) {
                        strBuf.replace(startIndex, endIndex, commandParamsBean
                                .getComment());
                    }
                } else
                // Check for notification_time parameter.
                if (token.equals(NOTIFICATION_TIME_PARAM)) {
                    if (commandParamsBean.getNotificationDelay() != null) {
                        long unixTime = DateUtils.getUnixTime(Integer
                                .parseInt(commandParamsBean
                                        .getNotificationDelay()));
                        strBuf.replace(startIndex, endIndex, String
                                .valueOf(unixTime));
                    }
                } else
                // Check for perf_data parameter.
                if (token.equals(PERF_DATA_PARAM)) {
                    if (commandParamsBean.getPerformanceData() != null) {
                        strBuf.replace(startIndex, endIndex, commandParamsBean
                                .getPerformanceData());
                    }
                } else
                // Check for plugin_output parameter.
                if (token.equals(PLUGIN_OUTPUT_PARAM)) {
                    if (commandParamsBean.getCheckOutput() != null) {
                        strBuf.replace(startIndex, endIndex, commandParamsBean
                                .getCheckOutput());
                    }
                } else
                // Check for plugin_state parameter.
                if (token.equals(PLUGIN_STATE_PARAM)) {
                    if (commandParamsBean.getCheckResult() != null) {
                        strBuf.replace(startIndex, endIndex, commandParamsBean
                                .getCheckResult());
                    }
                } else
                // Check for plugin_state parameter.
                if (token.equals(PLUGIN_OUTPUT_PIPE_PERF_DATA)) {
                    if ((commandParamsBean.getCheckResult() != null)
                            && (commandParamsBean.getPerformanceData() != null)) {
                        strBuf.replace(startIndex, endIndex, commandParamsBean
                                .getCheckOutput()
                                + Constant.PIPE
                                + commandParamsBean.getPerformanceData());
                    }
                } else
                // Check for scheduled_time parameter.
                if (token.equals(SCHEDULED_TIME_PARAM)) {
                    if (commandParamsBean.getStartTime() != null) {
                        long unixTime = DateUtils.getUnixTime(commandParamsBean
                                .getStartTime());
                        strBuf.replace(startIndex, endIndex, String
                                .valueOf(unixTime));
                    }
                } else
                // Check for triggered_by parameter.
                if (token.equals(TRIGGERED_BY_PARAM)) {
                    if (commandParamsBean.getTriggeredBy() != null) {
                        strBuf.replace(startIndex, endIndex, commandParamsBean
                                .getTriggeredBy());
                    }
                } else
                // Check for isfixed parameter.
                if (token.equals(IS_FIXED_PARAM)) {
                    strBuf.replace(startIndex, endIndex, String
                            .valueOf(commandParamsBean.getFixed()));

                } else
                // Check for is_persistent_comment parameter
                if (token.equals(IS_PERSISTENT_COMMENT_PARAM)) {
                    if (commandParamsBean.isPersistentComment()) {
                        iValue = 1;
                    }
                    strBuf
                            .replace(startIndex, endIndex, String
                                    .valueOf(iValue));

                } else
                // Check for is_send_notification parameter
                if (token.equals(IS_SEND_NOTIFICATION_PARAM)) {
                    if (commandParamsBean.isSendNotification()) {
                        iValue = 1;
                    }
                    strBuf
                            .replace(startIndex, endIndex, String
                                    .valueOf(iValue));
                }
            } catch (GWPortalGenericException gwEx) {
                // LOGGER.error(gwEx.getMessage());
                throw new GWPortalGenericException(
                        ResourceUtils
                                .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
            }
        }
        // Check for bytesize_cmd parameter.
        int startIndex = strBuf.indexOf(BYTESIZE_CMD_PARAM);
        if (startIndex <= -1) {
            // LOGGER.error(BYTESIZE_TOKEN_ABSENT);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        /*
         * Replace bytesize_cmd parameter. When all the parameters have been
         * replaced with actuals,then only size of command can be computed.
         * ByteSize should be a 5-length string with 0z padded to the left,in
         * case length is < 5. Do not consider the semicolon after bytsize_cmd
         * while computing the byte size.
         */
        String byteSize = calculateCommandSize(strBuf);
        strBuf.replace(startIndex, BYTESIZE_CMD_PARAM.length(), byteSize);
        command = strBuf.toString();
        return command;
    }

    /**
     * This methods constructs the composite commands
     * 
     * @param commandId
     * @return returnCommand
     * @throws GWPortalGenericException
     */
    public String constructCompositeCommands(String commandId)
            throws GWPortalGenericException {
        String returnCommand = "";
        CommandParamsBean commandParamsBean = (CommandParamsBean) FacesUtils
                .getManagedBean(Constant.COMMAND_PARAMS_MANAGED_BEAN);
        String nagiosCommand0 = "";
        StringBuffer nagiosCommand1 = new StringBuffer();
        StringBuffer parsedCommand = new StringBuffer();
        if (commandParamsBean != null) {
            boolean isComposite = false;
            for (CompositeCommandsEnum compositeCmdEnum : CompositeCommandsEnum
                    .values()) {
                try {
                    if (compositeCmdEnum.name().equals(commandId)) {
                        /*
                         * Check for 'Force Check' is checked. This applies to
                         * following commands - 1) SCHEDULE_HOST_CHECK 2)
                         * SCHEDULE_FORCED_HOST_CHECK 3)
                         * SCHEDULE_HOST_SVC_CHECKS 4)
                         * SCHEDULE_FORCED_HOST_SVC_CHECKS 5) SCHEDULE_SVC_CHECK
                         * 6) SCHEDULE_FORCED_SVC_CHECK
                         */
                        if (commandParamsBean.isForceCheck()) {
                            /*
                             * 'Force Check' is checked,send a single command
                             * for forced check.
                             */
                            nagiosCommand1 = new StringBuffer(
                                    parseCommand(compositeCmdEnum
                                            .getNagiosCommands()[1]));
                            parsedCommand = nagiosCommand1;
                            return parsedCommand.toString();
                        }
                        nagiosCommand0 = compositeCmdEnum.getNagiosCommands()[0];
                        /*
                         * Parse the parent nagiosCommand,append it to
                         * parsedCommand StringBuffer.
                         */
                        parsedCommand.append(parseCommand(nagiosCommand0));
                        // check-box for 'Enabled for hosts too' is checked
                        if ((commandParamsBean.isEnabledForHostsToo())
                                || (commandParamsBean.isDisabledForHostsToo())
                                || (commandParamsBean
                                        .isAckThisHostsServicesToo()
                                        || (commandParamsBean.isForceCheck()) || (commandParamsBean
                                        .isScheduleDowntimeForHostsToo()))) {
                            isComposite = true;
                        }

                        /*
                         * This composite command requires that the nagios
                         * command should be sent to each of the services for
                         * current host node.
                         */
                        if (isComposite) {
                            // Special case - ACK_HOST_PROB
                            if (compositeCmdEnum.isApplyToEachService()
                                    && commandId
                                            .equals(HostActionEnum.Acknowledge.ACK_HOST_PROB
                                                    .name())) {
                                nagiosCommand1 = parseAckHostProbCmd(commandParamsBean
                                        .getHostName());
                                /*
                                 * Parse the parent nagiosCommand,append it to
                                 * parsedCommand StringBuffer.
                                 */
                                parsedCommand.append(nagiosCommand1);
                            } else {
                                nagiosCommand1.append(compositeCmdEnum
                                        .getNagiosCommands()[1]);
                                parsedCommand
                                        .append(parseCommand(nagiosCommand1
                                                .toString()));
                            }
                            isComposite = false;
                        }
                    } else {
                        // Indicates single command.
                        returnCommand = Constant.EMPTY_STRING;
                    }
                } catch (GWPortalGenericException ex) {
                    // LOGGER.error(ex.getMessage());
                    throw new GWPortalGenericException(
                            ResourceUtils
                                    .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
                }
                returnCommand = parsedCommand.toString();
            }
        } else {
            // LOGGER.error(ActionHandler.NULL_COMMAND_PARAMS_BEAN
            // + " inside constructCompositeCommands() method.");
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER
                    .debug(" Returning compositeCommand from constructCompositeCommands() method = "
                            + returnCommand);
        }
        return returnCommand;
    }

    /**
     * This command processes the ACK_HOST_PROB action command. It parses and
     * constructs the composite part of this nagios command.
     * 
     * @param hostName
     * @return nagiosCommand1
     * @throws GWPortalGenericException
     */
    public StringBuffer parseAckHostProbCmd(String hostName)
            throws GWPortalGenericException {
        StringBuffer nagiosCommand1 = null;
        StringBuffer parsedCommand = new StringBuffer(Constant.EMPTY_STRING);
        // Fetch all the services for current host node.
        FoundationWSFacade foundationWSfacade = new FoundationWSFacade();
        ServiceStatus[] services = foundationWSfacade
                .getServicesByHostName(hostName);
        if (services != null) {
            for (ServiceStatus serviceStatus : services) {
                if (serviceStatus != null) {
                    /*
                     * Check if the service is in OK or Pending state or already
                     * acknowledged. If this is the case then skip that service.
                     */
                    boolean isAcknowledgable = MonitorStatusUtilities
                            .isServiceAcknowledgeable(serviceStatus);
                    if (!isAcknowledgable) {
                        continue;
                    }
                    nagiosCommand1 = new StringBuffer();
                    nagiosCommand1.append(CompositeCommandsEnum.ACK_HOST_PROB
                            .getNagiosCommands()[1]);
                    int index = nagiosCommand1.indexOf(Constant.SERVICE_DESC);
                    if (index != -1) {
                        nagiosCommand1.replace(index, index
                                + Constant.SERVICE_DESC.length(), serviceStatus
                                .getDescription());
                    }
                    // Replace all the formal parameters with the actual values.
                    String cmd = parseCommand(nagiosCommand1.toString());
                    parsedCommand.append(cmd);
                } else {
                    LOGGER
                            .info("Found null service inside parseAckHostProbCmd() method for host : "
                                    + hostName);
                    throw new GWPortalGenericException(
                            ResourceUtils
                                    .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
                }
            }
        } else {
            LOGGER.info(Constant.NO_SERVICES_FOUND_FOR_THIS_HOST
                    + Constant.COLON + hostName);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        return parsedCommand;
    }

    /**
     * This method computes the nagios command's size.
     * 
     * @param nagiosCommand
     * 
     * @return byteSize - size of the nagios command as a String
     * @throws GWPortalGenericException
     */
    public String calculateCommandSize(StringBuffer nagiosCommand)
            throws GWPortalGenericException {
        StringBuffer byteSize = new StringBuffer(DEFAULT_BYTESIZE);
        // Check if nagiosCommand is null
        if (nagiosCommand == null) {
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        int index = nagiosCommand.indexOf(BYTESIZE_CMD_PARAM);
        if (index <= -1) {
            // LOGGER.error(BYTESIZE_TOKEN_ABSENT);
            throw new GWPortalGenericException(BYTESIZE_TOKEN_ABSENT);
        }
        /*
         * Get the subString by excluding BYTESIZE_CMD_PARAM. 1 is added so that
         * ; is skipped.
         */
        String str = nagiosCommand.substring(BYTESIZE_CMD_PARAM.length() + 1,
                nagiosCommand.length());
        if (str == null || str.trim().equals(Constant.EMPTY_STRING)) {
            // LOGGER.error(INVALID_BYTESIZE);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        int byteSizeLength = byteSize.length();
        // Calculating the no. of digits in the length of str.
        int noOfDigits = (String.valueOf(str.length())).length();
        byteSize = byteSize.replace(byteSizeLength - noOfDigits,
                byteSizeLength, String.valueOf(str.length()));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Byte size for nagios command ( "
                    + nagiosCommand.toString() + " ) = " + byteSize.toString());
        }
        return byteSize.toString();
    }

}
