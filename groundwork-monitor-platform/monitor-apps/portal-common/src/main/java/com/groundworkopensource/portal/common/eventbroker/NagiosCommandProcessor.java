package com.groundworkopensource.portal.common.eventbroker;

import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.StringTokenizer;

public class NagiosCommandProcessor {

    protected static Log log = LogFactory.getLog(NagiosCommandProcessor.class);

    /**
     * Client socket to communicate with event broker
     */
    private ClientSocket socket = new ClientSocket();


    public static final String ADD_HOST_COMMENT = "<bytesize_cmd>;<user_name>;ADD_HOST_COMMENT;<host_name>;<persistent>;<author>;<comment>\n";
    public static final String ADD_SVC_COMMENT = "<bytesize_cmd>;<user_name>;ADD_SVC_COMMENT;<host_name>;<service_description>;<persistent>;<author>;<comment>\n";
    public static final String DEL_HOST_COMMENT = "<bytesize_cmd>;<user_name>;DEL_HOST_COMMENT;<comment_id>\n";
    public static final String DEL_SVC_COMMENT = "<bytesize_cmd>;<user_name>;DEL_SVC_COMMENT;<comment_id>\n";

    /**
     * Command template for ACKNOWLEDGE_SVC_PROBLEM nagios command
     */
    public static final String ACKNOWLEDGE_SVC_PROBLEM = "<bytesize_cmd>;<user_name>;ACKNOWLEDGE_SVC_PROBLEM;<host_name>;<svc_description>;1;<is_send_notification>;<is_persistent_comment>;<comment_author>;<comment_data>\n";


    // Constants in template to replace
    /**
     * Parameter for Byte-size of command
     */
    private static final String BYTE_SIZE = "<bytesize_cmd>";

    /**
     * Parameter for User name
     */
    private static final String USER_NAME = "<user_name>";

    /**
     * Parameter for Host Name associated with comment
     */
    private static final String HOST_NAME = "<host_name>";

    /**
     * Parameter for Persistent status of comment
     */
    private static final String PERSISTENT = "<persistent>";

    /**
     * Parameter for author
     */
    private static final String AUTHOR = "<author>";

    /**
     * Parameter for comment
     */
    private static final String COMMENT = "<comment>";

    /**
     * Parameter for service description
     */
    private static final String SERVICE_DESCRIPTION = "<service_description>";

    /**
     * Parameter for comment ID
     */
    private static final String COMMENT_ID = "<comment_id>";

    /**
     * String constant for default value for bytesize_cmd parameter.
     */
    private static final String DEFAULT_BYTESIZE = "00000";

    /**
     * Constant for 'bytesize_cmd'
     */
    private static final String BYTESIZE_CMD_PARAM = "<bytesize_cmd>";

    /**
     * String constant for 'bytesize_cmd token not found.'
     */
    private static final String BYTESIZE_TOKEN_ABSENT = "<bytesize_cmd> token not found.";

    /**
     * Constant for 'svc_description'
     */
    private static final String SERVICE_DESC_PARAM = "<svc_description>";

    /**
     * Constant for 'is_send_notification'
     */
    private static final String IS_SEND_NOTIFICATION_PARAM = "<is_send_notification>";

    /**
     * Constant for 'is_persistent_comment'
     */
    private static final String IS_PERSISTENT_COMMENT_PARAM = "<is_persistent_comment>";

    /**
     * Constant for 'comment_author'
     */
    private static final String COMMENT_AUTHOR_PARAM = "<comment_author>";

    /**
     * Constant for 'comment_data'
     */
    private static final String COMMENT_DATA_PARAM = "<comment_data>";
    /**
     * ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY
     */
    private static final String ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY = "com_groundwork_portal_statusviewer_actionsPortlet_socketError";

    public void addHostComment(String inputComment, String hostName, String userName) {
        processComment(ADD_HOST_COMMENT, inputComment, hostName, userName, "", true, "");
    }

    public void addServiceComment(String inputComment, String hostName, String userName, String serviceDescription) {
        processComment(ADD_SVC_COMMENT, inputComment, hostName, userName, serviceDescription, true, "");
    }

    public void deleteHostComment(String userName, String selectedCommentId) {
        processComment(DEL_HOST_COMMENT, "", "", userName, "", true, selectedCommentId);
    }

    public void deleteServiceComment(String userName, String selectedCommentId) {
        processComment(DEL_SVC_COMMENT, "", "", userName, "", true, selectedCommentId);
    }

    private void processComment(String commandTemplate, String inputComment, String hostName, String userName, String serviceDescription, boolean persistent, String selectedCommentId) {

        StringBuffer buffer = new StringBuffer(commandTemplate);
    /*
     * Command parameters are separated by semicolons
     */
        StringTokenizer parameterTokenizer = new StringTokenizer(
                commandTemplate, ";");

        while (parameterTokenizer.hasMoreElements()) {

            int isPersistentValue = 0;

            String token = parameterTokenizer.nextToken();

        /*
         * For '\n' token replace with space and trim - last parameter in
         * template workaround
         */
            if (token.indexOf('\n') > -1) {
                token = token.replace('\n',
                        ' ').trim();
            }
            if (token == null || token.trim().equals("")) {
                log.info("processCommand(): Empty token in nagios command template");
            } else {
                // Find the index of the token in strBuf
                int startIndex = buffer.indexOf(token);
                int endIndex = startIndex + token.length();

                // Parameter parsing and replacement
                if (token.equals(USER_NAME)) {
                    buffer.replace(startIndex, endIndex, userName);
                } else if (token.equals(HOST_NAME)) {
                    buffer.replace(startIndex, endIndex, hostName);
                } else if (token.equals(PERSISTENT)) {
                    if (persistent) {
                        isPersistentValue = 1;
                    }
                    buffer.replace(startIndex, endIndex, String
                            .valueOf(isPersistentValue));
                } else if (token.equals(AUTHOR)) {
                    buffer.replace(startIndex, endIndex, userName);
                } else if (token.equals(COMMENT)) {
                    /**
                     * Nagios doesn't process multi-line comments. Comments
                     * portlet should not allow newlines send to nagios command
                     * processor. Hence remove any newline characters in the
                     * inputComment.
                     */
                    if ((inputComment != null)
                            && (!"".equals(inputComment))) {
                        inputComment = inputComment.replace(
                                '\n', ' ');
                    }
                    buffer.replace(startIndex, endIndex, inputComment);
                } else if (token.equals(SERVICE_DESCRIPTION)) {
                    buffer.replace(startIndex, endIndex, serviceDescription);
                } else if (token.equals(COMMENT_ID)) {
                    buffer
                            .replace(startIndex, endIndex,
                                    selectedCommentId);
                }

            }
        }

        // Check for bytesize_cmd parameter.
        int startIndex = buffer.indexOf(BYTE_SIZE);
        if (startIndex <= -1) {
            log.info("processCommand(): Invalid command template. Byte-size token absent.");
        } else {
        /*
         * Replace bytesize_cmd parameter. When all the parameters have been
         * replaced with actuals,then only size of command can be computed.
         * ByteSize should be a 5-length string with 0z padded to the
         * left,in case length is < 5. Do not consider the semicolon after
         * bytsize_cmd while computing the byte size.
         */
            String byteSize;
            try {
                byteSize = calculateCommandSize(buffer);
                buffer.replace(startIndex, BYTE_SIZE.length(), byteSize);

            } catch (GWPortalGenericException e) {
                log.info("processCommand(): Error calculating command size");
                return;
            }
        }

        sendCommandToNagios(buffer.toString());

    }

    public void processAck(String comment, String hostName, String userName, String serviceDescription, boolean persistent) {

        String commandTemplate = ACKNOWLEDGE_SVC_PROBLEM;
        String command = "";
        StringBuffer buffer = new StringBuffer(commandTemplate);
        /*
         * Command parameters are separated by semicolons
         */
        StringTokenizer parameterTokenizer = new StringTokenizer(
                commandTemplate, ";");

        while (parameterTokenizer.hasMoreElements()) {

            int isCheckValue = 0;

            String token = parameterTokenizer.nextToken();

            /*
             * For '\n' token replace with space and trim - last parameter in
             * template workaround
             */
            if (token.indexOf('\n') > -1) {
                token = token.replace('\n',
                        ' ').trim();
            }
            if (token == null || token.trim().equals("")) {
                log.debug("processCommand(): Empty token in nagios command template");
            } else {
                // Find the index of the token in strBuf
                int startIndex = buffer.indexOf(token);
                int endIndex = startIndex + token.length();

                // Parameter parsing and replacement
                if (token.equals(USER_NAME) && userName != null) {
                    buffer.replace(startIndex, endIndex, userName);
                } else if (token.equals(HOST_NAME) && hostName != null) {
                    buffer.replace(startIndex, endIndex, hostName);
                } else if (token.equals(IS_PERSISTENT_COMMENT_PARAM)) {
                    if (persistent) {
                        isCheckValue = 1;
                    }
                    buffer.replace(startIndex, endIndex,
                            String.valueOf(isCheckValue));
                } else if (token.equals(COMMENT_AUTHOR_PARAM)
                        && userName != null) {
                    buffer.replace(startIndex, endIndex, userName);
                } else if (token.equals(COMMENT_DATA_PARAM)
                        && comment != null) {
                    buffer.replace(startIndex, endIndex, comment);
                } else if (token.equals(SERVICE_DESC_PARAM)
                        && serviceDescription != null) {
                    buffer.replace(startIndex, endIndex,
                            serviceDescription);
                } else if (token.equals(IS_SEND_NOTIFICATION_PARAM)) {

                    isCheckValue = 0;

                    buffer.replace(startIndex, endIndex,
                            String.valueOf(isCheckValue));
                }

            }
        }

        // Check for bytesize_cmd parameter.
        int startIndex = buffer.indexOf(BYTESIZE_CMD_PARAM);
        if (startIndex <= -1) {
            log.debug("processCommand(): Invalid command template. Byte-size token absent.");
        } else {
            /*
             * Replace bytesize_cmd parameter. When all the parameters have been
             * replaced with actuals,then only size of command can be computed.
             * ByteSize should be a 5-length string with 0z padded to the
             * left,in case length is < 5. Do not consider the semicolon after
             * bytsize_cmd while computing the byte size.
             */
            String byteSize;
            try {
                byteSize = calculateCommandSize(buffer);
                buffer.replace(startIndex, BYTESIZE_CMD_PARAM.length(),
                        byteSize);
                command = buffer.toString();
            } catch (GWPortalGenericException e) {
                log.debug("processCommand(): Error calculating command size");
                return;
            }
            sendCommandToNagios(command);
        }
    }

    /**
     * This method send the buffer to nagios.
     *
     * @param buffer
     */
    private void sendCommandToNagios(String buffer) {
        // Send Command
        try {
            this.socket.run(buffer);
        } catch (GWPortalGenericException e) {
            log.error(e);
        }
        /*
         // Capture action in audit log record
        try {
            // construct audit log record
            String subsystem = "SV";
            String action = "ACTION";
            String userName = getUserName();
            String description = null;
            String [] nagiosCommandElements = buffer.toString().split(Constant.SEMICOLON);
            if (nagiosCommandElements.length >= 3) {
                userName = nagiosCommandElements[1];
                description = nagiosCommandElements[2];
            }
            String hostName = getHostName();
            String serviceDescription = getServiceDescription();
            DtoAuditLog auditLog = new DtoAuditLog(subsystem, action, description, userName, hostName, serviceDescription);
            // capture audit log record
            String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
            AuditLogClient auditLogClient = new AuditLogClient(deploymentUrl);
            DtoOperationResults results = auditLogClient.post(new DtoAuditLogList(Arrays.asList(auditLog)));
            if ((results == null) || (results.getSuccessful() == 0)) {
                logger.error("Audit log record not captured for acknowledge.");
            }
        } catch (Exception e) {
            logger.error("Audit log record not captured for acknowledge: "+e);
        }



         */
    }


    /**
     * This method computes the nagios command's size.
     *
     * @param nagiosCommand
     * @return byteSize - size of the nagios command as a String
     * @throws GWPortalGenericException
     */
    protected String calculateCommandSize(StringBuffer nagiosCommand)
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
            throw new GWPortalGenericException(BYTESIZE_TOKEN_ABSENT);
        }
        /*
         * Get the subString by excluding BYTESIZE_CMD_PARAM. 1 is added so that
         * ; is skipped.
         */
        String str = nagiosCommand.substring(BYTESIZE_CMD_PARAM.length() + 1,
                nagiosCommand.length());
        if (str == null || str.trim().equals("")) {
            ;
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage(ACTIONS_PORTLET_SOCKET_ERROR_BUNDLE_PROPERTY));
        }
        int byteSizeLength = byteSize.length();
        // Calculating the no. of digits in the length of str.
        int noOfDigits = (String.valueOf(str.length())).length();
        byteSize = byteSize.replace(byteSizeLength - noOfDigits,
                byteSizeLength, String.valueOf(str.length()));

        if (log.isDebugEnabled()) {
            log.debug("Byte size for nagios command ( "
                    + nagiosCommand.toString() + " ) = " + byteSize.toString());
        }
        return byteSize.toString();
    }


}
