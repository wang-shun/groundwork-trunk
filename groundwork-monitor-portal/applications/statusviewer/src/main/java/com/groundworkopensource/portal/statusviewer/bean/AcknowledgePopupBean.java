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
package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;

import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.ValidationUtils;
import com.groundworkopensource.portal.statusviewer.common.actions.ActionCommandsConstants;
import com.groundworkopensource.portal.statusviewer.common.actions.CommandDescriptionConstants;
import com.groundworkopensource.portal.statusviewer.common.actions.NagiosCommandsConstants;
import com.groundworkopensource.portal.statusviewer.common.eventbroker.ClientSocket;
import com.groundworkopensource.portal.statusviewer.handler.ActionCommandHandler;
import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * Backing bean for acknowledge pop up window.
 * 
 * @author mridu_narang
 */

public class AcknowledgePopupBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8965850161422636101L;

    /**
     * Logger
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Boolean variable indicating visibility - open/close for pop-up
     */
    private boolean visible = false;

    /**
     * from which portlet this popup window is called?
     */
    private String fromPortlet;

    /**
     * Boolean variable indicating if the event broker server is listening for
     * nagios commands or not.
     */
    private boolean nagiosDown = false;

    /**
     * 
     * @return nagiosDown
     */
    public boolean isNagiosDown() {
        return nagiosDown;
    }

    /**
     * 
     * @param nagiosDown
     */
    public void setNagiosDown(boolean nagiosDown) {
        this.nagiosDown = nagiosDown;
    }

    /**
     * Title for pop-up
     */
    private String title = "";

    /**
     * Command Description
     */
    private String commandDescription = "";

    /**
     * Host Name
     */
    private String hostName = "";

    /**
     * Boolean field indicating if acknowledgment is for a host or a service
     */
    private boolean hostAck;

    /**
     * Boolean field indicating if portlet in StatusViewer or in Dashboard.
     */
    private boolean inStatusViewer;

    /**
     * Boolean field indicating request too acknowledge for all services
     */
    private boolean acknowledgeServices;

    /**
     * Boolean field indicating request too acknowledge for all services
     */
    private boolean acknowledgeServicesCheckboxDisabled;

    /**
     * Boolean field indicating if notification is to be sent
     */
    private boolean notify;

    /**
     * Boolean field indicating if comment is to be made persistent (By Default true)
     */
    private boolean persistentComment = true;

    /**
     * Service Description
     */
    private String serviceDescription = "";

    /**
     * Service Description
     */
    private String author = "";

    /**
     * Service Description
     */
    private String comment = "";

    /**
     * Service Description
     */
    private String userName = "";

    /**
     * Client socket to communicate with event broker
     */
    private ClientSocket socket = new ClientSocket();

    // CONSTANTS
    /**
     * Constant for 'bytesize_cmd'
     */
    private static final String BYTESIZE_CMD_PARAM = "<bytesize_cmd>";

    /**
     * Parameter for User name
     */
    private static final String USER_NAME = "<user_name>";

    /**
     * Parameter for Host Name associated with comment
     */
    private static final String HOST_NAME = "<host_name>";

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
     * Maximum allowable length of Check Output,Performance Data.
     */
    private static final int MAX_ALLOWABLE_LENGTH = 200;

    /**
     * In case of composite command 'Acknowledge all services of this host',this
     * list will contain list of services for that host.
     */
    private List<ServiceStatus> services = new ArrayList<ServiceStatus>();

    /**
     * This flag is used to decide if the check-box for 'Acknowledge all
     * services' is to be rendered or not.
     */
    private boolean ackAllServicesCheckboxInvisible = false;

    /**
     * popup style
     */
    private String popupStyle = Constant.ACK_POPUP_STATUSVIEWER_STYLE;

    /**
     * pop-up style for nagios error
     */
    private String nagiosErrPopupStyle = Constant.ACK_NAGIOS_ERR_POPUP_STATUSVIEWER_STYLE;

    /**
     * Boolean field indicating request too acknowledge all services
     * (specifically used in Seurat View to just acknowledge all services and
     * not the host)
     */
    private boolean acknowledgeAllServices;

    // for status viewer:
    // "z-index:999; width: 900px; height: 200px; background: #FFFFFF;";
    // for dashboard:
    // "z-index:999; top: 45%; left: 100px; width: 900px; height: 200px; position:fixed; background: #FFFFFF;"

    /**
     * return nagiosErrPopupStyle
     * 
     * @return nagiosErrPopupStyle
     */
    public String getNagiosErrPopupStyle() {
        return nagiosErrPopupStyle;
    }

    /**
     * 
     * @param nagiosErrPopupStyle
     */
    public void setNagiosErrPopupStyle(String nagiosErrPopupStyle) {
        this.nagiosErrPopupStyle = nagiosErrPopupStyle;
    }

    /**
     * @return services
     */
    public List<ServiceStatus> getServices() {
        return services;
    }

    /**
     * @param services
     */
    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }

    /**
     * Method called when user clicks on the 'Submit' button on the modal pop-up
     * for acknowledge action command. It checks the command requested,checks if
     * it is a composite command,parses the command,replaces formal parameters
     * in the command with the actual values and sends it to event broker.
     * 
     * @param event
     */
    public void submitCommand(ActionEvent event) {
        // validate
        if (!validateComments(event)) {
            return;
        }

        // submit command
        String parsedCommand = Constant.EMPTY_STRING;

        StringBuffer nagiosCommand = new StringBuffer();
        try {
            if (isHostAck()) {
                // Acknowledge this host.
                nagiosCommand
                        .append(processCommand(NagiosCommandsConstants.ACK_HOST_PROB));

                // Acknowledge services of this host too
                if (isAcknowledgeServices()) {
                    this.logger.debug("submitCommand(): Processing "
                            + NagiosCommandsConstants.ACK_SERVICES_TOO);
                    String commandForAllServices = parseAckAllServicesCmd();
                    nagiosCommand.append(commandForAllServices);
                }

            } else if (isAcknowledgeAllServices()) {
                // Acknowledge all services - specifically gets called for
                // Seurat View portlet - "Acknowledge all Services" button
                nagiosCommand = new StringBuffer(parseAckAllServicesCmd());

            } else {
                // Acknowledge this service. (single service)
                this.logger.debug("submitCommand(): Processing "
                        + NagiosCommandsConstants.ACKNOWLEDGE_SVC_PROBLEM);
                nagiosCommand
                        .append(processCommand(NagiosCommandsConstants.ACKNOWLEDGE_SVC_PROBLEM));
            }
        } catch (GWPortalGenericException e) {
            this.logger.error(e);
            return;
        }
        parsedCommand = nagiosCommand.toString();
        sendCommandToNagios(parsedCommand);

        // close the pop-up
        closePopup();
    }

    /**
     * This method gets the list of services for the host.
     */
    private void getServicesForHost() {
        // cleat the old service list
        services.clear();
        // Get the list of services for the host.
        FoundationWSFacade foundationWSfacade = new FoundationWSFacade();
        try {
            ServiceStatus[] serviceArray = foundationWSfacade
                    .getServicesByHostName(this.hostName);
            if ((serviceArray != null) && (serviceArray.length > 0)) {
                // Create the services list from services array.
                for (ServiceStatus serviceStatus : serviceArray) {
                    if (serviceStatus != null) {
                        services.add(serviceStatus);
                    }
                }
            }
        } catch (GWPortalGenericException e) {
            logger.error("showAcknowledgementPopup(): Error occured while fetching the service for host :"
                    + hostName);
            return;
        }
    }

    /**
     * Method to process command
     */
    private String processCommand(String commandTemplate) {
        String command = Constant.EMPTY_STRING;
        if (commandTemplate == null
                || commandTemplate.trim().equals(Constant.EMPTY_STRING)) {
            this.logger
                    .debug("processCommand(): Improper nagios command parameter passed for processing with actual parameters");
            return command;
        }
        StringBuffer buffer = new StringBuffer(commandTemplate);
        /*
         * Command parameters are separated by semicolons
         */
        StringTokenizer parameterTokenizer = new StringTokenizer(
                commandTemplate, Constant.SEMICOLON);

        while (parameterTokenizer.hasMoreElements()) {

            int isCheckValue = 0;

            String token = parameterTokenizer.nextToken();

            /*
             * For '\n' token replace with space and trim - last parameter in
             * template workaround
             */
            if (token.indexOf(Constant.NEWLINE_CHAR) > -1) {
                token = token.replace(Constant.NEWLINE_CHAR,
                        Constant.EMPTY_CHAR).trim();
            }
            if (token == null || token.trim().equals(Constant.EMPTY_STRING)) {
                this.logger
                        .debug("processCommand(): Empty token in nagios command template");
            } else {
                // Find the index of the token in strBuf
                int startIndex = buffer.indexOf(token);
                int endIndex = startIndex + token.length();

                // Parameter parsing and replacement
                if (token.equals(USER_NAME) && this.userName != null) {
                    buffer.replace(startIndex, endIndex, this.userName);
                } else if (token.equals(HOST_NAME) && this.hostName != null) {
                    buffer.replace(startIndex, endIndex, this.hostName);
                } else if (token.equals(IS_PERSISTENT_COMMENT_PARAM)) {
                    if (isPersistentComment()) {
                        isCheckValue = 1;
                    }
                    buffer.replace(startIndex, endIndex,
                            String.valueOf(isCheckValue));
                } else if (token.equals(COMMENT_AUTHOR_PARAM)
                        && this.author != null) {
                    buffer.replace(startIndex, endIndex, this.author);
                } else if (token.equals(COMMENT_DATA_PARAM)
                        && this.comment != null) {
                    buffer.replace(startIndex, endIndex, this.comment);
                } else if (token.equals(SERVICE_DESC_PARAM)
                        && this.serviceDescription != null) {
                    buffer.replace(startIndex, endIndex,
                            this.serviceDescription);
                } else if (token.equals(IS_SEND_NOTIFICATION_PARAM)) {
                    if (isNotify()) {
                        isCheckValue = 1;
                    }
                    buffer.replace(startIndex, endIndex,
                            String.valueOf(isCheckValue));
                }

            }
        }

        // Check for bytesize_cmd parameter.
        int startIndex = buffer.indexOf(BYTESIZE_CMD_PARAM);
        if (startIndex <= -1) {
            this.logger
                    .debug("processCommand(): Invalid command template. Byte-size token absent.");
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
                byteSize = new ActionCommandHandler()
                        .calculateCommandSize(buffer);
                buffer.replace(startIndex, BYTESIZE_CMD_PARAM.length(),
                        byteSize);
                command = buffer.toString();
            } catch (GWPortalGenericException e) {
                this.logger
                        .debug("processCommand(): Error calculating command size");
                return command;
            }
        }
        logger.debug("Processcommand = " + command);
        return command;
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
            /*
             * If the event broker server/nagios is down,display info message
             * pop-up.
             */
            if (socket.isNagiosDown()) {
                setNagiosDown(true);
            }
            // set if in dashboard or in status viewer
            boolean inDashbord = PortletUtils.isInDashbord();
            this.setInStatusViewer(!inDashbord);
            if (inDashbord) {
                this.setNagiosErrPopupStyle(Constant.ACK_NAGIOS_ERR_POPUP_DASHBOARD_STYLE);
            }
            this.logger
                    .error("processCommand(): Could not process command to event broker. Error running command from socket. Command : "
                            + buffer);
            return;
        }
    }

    /**
     * This command processes the the nagios command for 'Acknowledge all
     * services' action command. It parses and constructs the composite part of
     * this nagios command.
     * 
     * @return nagiosCommand1
     * @throws GWPortalGenericException
     */
    private String parseAckAllServicesCmd() throws GWPortalGenericException {
        StringBuffer parsedCommand = new StringBuffer(Constant.EMPTY_STRING);

        // // Parse ACKNOWLEDGE_HOST_PROBLEM command for host.
        StringBuffer nagiosCommand = null;
        // parsedCommand.append(processCommand(nagiosCommand.toString()));

        // Get this services for the host
        getServicesForHost();
        // Construct commands for all the services of this host.
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
                    nagiosCommand = new StringBuffer();
                    setServiceDescription(serviceStatus.getDescription());
                    nagiosCommand
                            .append(NagiosCommandsConstants.ACKNOWLEDGE_SVC_PROBLEM);
                    int index = nagiosCommand.indexOf(Constant.SERVICE_DESC);
                    if (index != -1) {
                        nagiosCommand.replace(index, index
                                + Constant.SERVICE_DESC.length(),
                                serviceStatus.getDescription());
                    }
                    // Replace all the formal parameters with the actual values.
                    String cmd = processCommand(nagiosCommand.toString());
                    parsedCommand.append(cmd);
                } else {
                    logger.debug("Found null service inside parseAckHostProbCmd() method for host : "
                            + hostName);
                    throw new GWPortalGenericException();
                }
            }
        } else {
            logger.debug(Constant.NO_SERVICES_FOUND_FOR_THIS_HOST
                    + Constant.COLON + hostName);
            throw new GWPortalGenericException();
        }
        logger.debug("parseAckAllServicesCmd() is returning = "
                + parsedCommand.toString());
        return parsedCommand.toString();
    }

    /**
     * Method to retrieve acknowledgment pop up window state
     * 
     * @return boolean
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Method to set acknowledgment pop up window to visible state passed
     * 
     * @param visible
     */
    public void setVisible(boolean visible) {
        if (isHostAck()) {
            setTitle(ActionCommandsConstants.ACK_HOST_PROB);
            setCommandDescription(CommandDescriptionConstants.ACK_HOST_PROB);
        } else {
            setTitle(ActionCommandsConstants.ACKNOWLEDGE_SVC_PROBLEM);
            setCommandDescription(CommandDescriptionConstants.ACK_SVC_PROB);
        }
        // Reset Fields
        resetFields();
        this.visible = visible;
    }

    /**
     * This is overloaded form of setVisible method, without setting default
     * title. It sets the "visible" field to true.
     * 
     * Method to set acknowledgment pop up window to visible state passed
     * 
     */
    public void setVisible() {

        if (isHostAck()) {
            setCommandDescription(CommandDescriptionConstants.ACK_HOST_PROB);
        } else {
            setCommandDescription(CommandDescriptionConstants.ACK_SVC_PROB);
        }
        // Reset Fields
        resetFields();
        this.visible = true;
    }

    /**
     * Method to close acknowledgment pop up window
     */
    public void closePopup() {
        resetFields();
        this.visible = false;

        // This call is used to initialize right click functionality on hosts.
        if (Constant.SEURAT.equals(fromPortlet)) {
            JavascriptContext.addJavascriptCall(
                    FacesContext.getCurrentInstance(), "initSeurat();");
        }
    }

    /**
     * Method to close pop up window for info message to be displayed when
     * nagios is down.
     */
    public void closeNagiosDownPopup() {
        setNagiosDown(false);
    }

    /**
     * Reset Fields
     */
    public void resetFields() {
        setComment(Constant.EMPTY_STRING);
        setNotify(false);
        setPersistentComment(true);
        setAcknowledgeServices(false);
    }

    /**
     * This method validates the comments field. 1) Should be non-empty. 2)
     * Length must be <= 500.
     * 
     * @param event
     * 
     * @return false if validation fails
     */
    public boolean validateComments(ActionEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();

        UIComponent base = event.getComponent();
        if (null == context || null == base) {
            return false;
        }
        UIComponent parentForm = base.getParent();
        if (null == parentForm) {
            return false;
        }

        UIComponent commentsComponent = parentForm
                .findComponent("ackPAckPanelPopupTxtComment");
        String value = getComment();

        if (value != null) {
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(value)) {
                ((UIInput) commentsComponent).setValid(false);
                showMessage(
                        ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                        ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                        context, commentsComponent);
                return false;
            }
            // check for length limit.
            if (checkIfLengthExceedsLimit(value)) {
                ((UIInput) commentsComponent).setValid(false);
                showMessage(
                        ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_500"),
                        ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_500"),
                        context, commentsComponent);
                return false;
            }

        } // (value != null)
        return true;
    }

    /**
     * 
     * Method to set detail & summary message, severity fields for Faces
     * Message.
     * 
     * @param detailMessage
     * @param summaryMessage
     */
    private void showMessage(String detailMessage, String summaryMessage,
            FacesContext facesContext, UIComponent component) {
        // Custom faces message
        FacesMessage message = new FacesMessage();
        // Set message details
        message.setDetail(detailMessage);
        message.setSummary(summaryMessage);
        // Set severity
        message.setSeverity(FacesMessage.SEVERITY_ERROR);
        facesContext.addMessage(component.getClientId(facesContext), message);
    }

    /**
     * This method validates the length of the input for non-empty value.
     * 
     * @param value
     * @return true - if the length exceeds maximum allowed number of
     *         characters. false ,if the length is within allowed limits.
     */
    public boolean checkIfLengthExceedsLimit(Object value) {
        if (value != null) {
            String inputString = (String) value;
            // if the inputString is non-empty then check for max length.
            if (!Constant.EMPTY_STRING.equals(inputString.trim())) {
                if (inputString.length() > MAX_ALLOWABLE_LENGTH) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the title.
     * 
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the title.
     * 
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the commandDescription.
     * 
     * @param commandDescription
     *            the commandDescription to set
     */
    public void setCommandDescription(String commandDescription) {
        this.commandDescription = commandDescription;
    }

    /**
     * Returns the commandDescription.
     * 
     * @return the commandDescription
     */
    public String getCommandDescription() {
        return this.commandDescription;
    }

    /**
     * Sets the hostName.
     * 
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Returns the hostName.
     * 
     * @return the hostName
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Sets the hostAck.
     * 
     * @param hostAck
     *            the hostAck to set
     */
    public void setHostAck(boolean hostAck) {
        this.hostAck = hostAck;
    }

    /**
     * Returns the hostAck.
     * 
     * @return the hostAck
     */
    public boolean isHostAck() {
        return this.hostAck;
    }

    /**
     * Sets the serviceDescription.
     * 
     * @param serviceDescription
     *            the serviceDescription to set
     */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    /**
     * Returns the serviceDescription.
     * 
     * @return the serviceDescription
     */
    public String getServiceDescription() {
        return this.serviceDescription;
    }

    /**
     * Sets the persistentComment.
     * 
     * @param persistentComment
     *            the persistentComment to set
     */
    public void setPersistentComment(boolean persistentComment) {
        this.persistentComment = persistentComment;
    }

    /**
     * Returns the persistentComment.
     * 
     * @return the persistentComment
     */
    public boolean isPersistentComment() {
        return this.persistentComment;
    }

    /**
     * Sets the author.
     * 
     * @param author
     *            the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the author.
     * 
     * @return the author
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Sets the comment.
     * 
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the comment.
     * 
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Sets the notify.
     * 
     * @param notify
     *            the notify to set
     */
    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    /**
     * Returns the notify.
     * 
     * @return the notify
     */
    public boolean isNotify() {
        return this.notify;
    }

    /**
     * @return userName
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Sets the acknowledgeServices.
     * 
     * @param acknowledgeServices
     *            the acknowledgeServices to set
     */
    public void setAcknowledgeServices(boolean acknowledgeServices) {
        this.acknowledgeServices = acknowledgeServices;
    }

    /**
     * Returns the acknowledgeServices.
     * 
     * @return the acknowledgeServices
     */
    public boolean isAcknowledgeServices() {
        return this.acknowledgeServices;
    }

    /**
     * Sets the acknowledgeServicesCheckboxDisabled.
     * 
     * @param acknowledgeServicesCheckboxDisabled
     *            the acknowledgeServicesCheckboxDisabled to set
     */
    public void setAcknowledgeServicesCheckboxDisabled(
            boolean acknowledgeServicesCheckboxDisabled) {
        this.acknowledgeServicesCheckboxDisabled = acknowledgeServicesCheckboxDisabled;
    }

    /**
     * Returns the acknowledgeServicesCheckboxDisabled.
     * 
     * @return the acknowledgeServicesCheckboxDisabled
     */
    public boolean isAcknowledgeServicesCheckboxDisabled() {
        return acknowledgeServicesCheckboxDisabled;
    }

    /**
     * sets the check box bound to "acknowledge services too?" checked and
     * disabled.
     */
    public void setAcknowledgeServicesCheckbox() {
        setAcknowledgeServicesCheckboxDisabled(true);
        acknowledgeServices = true;
    }

    /**
     * Sets the ackAllServicesCheckboxInvisible.
     * 
     * @param ackAllServicesCheckboxInvisible
     *            the ackAllServicesCheckboxInvisible to set
     */
    public void setAckAllServicesCheckboxInvisible(
            boolean ackAllServicesCheckboxInvisible) {
        this.ackAllServicesCheckboxInvisible = ackAllServicesCheckboxInvisible;
    }

    /**
     * Returns the ackAllServicesCheckboxInvisible.
     * 
     * @return the ackAllServicesCheckboxInvisible
     */
    public boolean isAckAllServicesCheckboxInvisible() {
        return ackAllServicesCheckboxInvisible;
    }

    /**
     * Sets the inStatusViewer.
     * 
     * @param inStatusViewer
     *            the inStatusViewer to set
     */
    public void setInStatusViewer(boolean inStatusViewer) {
        this.inStatusViewer = inStatusViewer;
    }

    /**
     * Returns the inStatusViewer.
     * 
     * @return the inStatusViewer
     */
    public boolean isInStatusViewer() {
        return inStatusViewer;
    }

    /**
     * Sets the popupStyle.
     * 
     * @param popupStyle
     *            the popupStyle to set
     */
    public void setPopupStyle(String popupStyle) {
        this.popupStyle = popupStyle;
    }

    /**
     * Returns the popupStyle.
     * 
     * @return the popupStyle
     */
    public String getPopupStyle() {
        return popupStyle;
    }

    /**
     * @param ae
     */
    public void resetFields(ActionEvent ae) {
        UIComponent base = ae.getComponent();
        UIComponent parentForm = base.getParent();
        resetField(parentForm, "ackPAckPanelPopupTxtComment",
                Constant.EMPTY_STRING);
        resetField(parentForm, "ackPAckPanelPopupChkBoxPersistentComment",
                "true");
        resetField(parentForm, "ackPAckPanelPopupChkSendNotification", "false");
        resetField(parentForm, "ackPAckPanelPopupAckAllServicesToo", "false");
    }

    /**
     * reset ack pop field depending on component ID
     * 
     * @param component
     * @param id
     */
    private void resetField(UIComponent component, String componentId,
            String value) {
        UIInput clearInput = (UIInput) component.findComponent(componentId);
        if (clearInput != null) {
            clearInput.setSubmittedValue(value);
        }

    }

    /**
     * Sets the acknowledgeAllServices.
     * 
     * @param acknowledgeAllServices
     *            the acknowledgeAllServices to set
     */
    public void setAcknowledgeAllServices(boolean acknowledgeAllServices) {
        this.acknowledgeAllServices = acknowledgeAllServices;
    }

    /**
     * Returns the acknowledgeAllServices.
     * 
     * @return the acknowledgeAllServices
     */
    public boolean isAcknowledgeAllServices() {
        return acknowledgeAllServices;
    }

    /**
     * Sets the fromPortlet.
     * 
     * @param fromPortlet
     *            the fromPortlet to set
     */
    public void setFromPortlet(String fromPortlet) {
        this.fromPortlet = fromPortlet;
    }

    /**
     * Returns the fromPortlet.
     * 
     * @return the fromPortlet
     */
    public String getFromPortlet() {
        return fromPortlet;
    }
}