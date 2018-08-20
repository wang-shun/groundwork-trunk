/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
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
package com.groundworkopensource.portal.statusviewer.bean.action;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import com.groundworkopensource.portal.common.*;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.statusviewer.common.ValidationUtils;

/**
 * This is a backing bean for intermediate screen of actions portlet. When user
 * select a menu option,a modal pop-up appears which asks for input parameters
 * for the selected action command. It also holds the validation functions for
 * the input values.
 * 
 * @author shivangi_walvekar
 * 
 */
public class CommandParamsBean implements Serializable {

    // /**
    // * LOGGER
    // */
    // private static final Logger LOGGER = Logger
    // .getLogger(CommandParamsBean.class.getName());
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1740389904071777382L;

    /**
     * Node Type
     */
    private String nodeType;

    /**
     * Value of the node type
     */
    private String nodeTypeValue;
    /**
     * Host group name property
     */
    private String hostGroupName;
    /**
     * Host name property
     */
    private String hostName;
    /**
     * Service group name property
     */
    private String serviceGroupName;
    /**
     * Service description property
     */
    private String serviceDesc;
    /**
     * Author name property
     */
    private String authorName;

    /**
     * Comment input by the user.
     */
    private String comment;

    /**
     * Start time value input by user
     */
    private String startTime;

    /**
     * End time value input by user.
     */
    private String endTime;

    /**
     * Duration in hours
     */
    private String durationHours;
    /**
     * Duration in minutes
     */
    private String durationMinutes;
    /**
     * boolean field to indicate if the particular option is enabled for hosts
     * too
     */
    private boolean enabledForHostsToo;

    /**
     * boolean field to indicate if the particular option is disabled for hosts
     * too
     */
    private boolean disabledForHostsToo;

    /**
     * Delay for the notification (in minutes from now)
     */
    private String notificationDelay;
    /**
     * Type of the schedule - Flexible or Fixed
     */
    private String scheduleType;
    /**
     * boolean field to indicate if the downtime is to be scheduled for hosts
     * too
     */
    private boolean scheduleDowntimeForHostsToo;

    /**
     * Entity which has triggered the particular option
     */
    private String triggeredBy;

    /**
     * Perform a particular with child hosts or not.
     */
    private String childHosts;

    /**
     * boolean field to indicate whether the services for this host need to be
     * acknowledged as well
     */
    private boolean ackThisHostsServicesToo;

    /**
     * boolean field to indicate whether the notification should be sent or not
     */
    private boolean sendNotification;

    /**
     * boolean field to indicate whether the persistent comment is required or
     * not
     */
   
    //Persistent comment checkbox 
    // Making default as true
    private boolean persistentComment = true; 

    /**
     * Time at which a particular check is to be performed. Time Format:
     * 'mm/dd/yyyy hh:mm:ss'
     */
    private String checkTime;

    /**
     * boolean field to identify if it a forced-check or not.
     */
    private boolean forceCheck;

    /**
     * Value of the check result
     */
    private String checkResult;

    /**
     * Value of the check output
     */
    private String checkOutput;
    /**
     * Value of the performance data
     */
    private String performanceData;

    /**
     * Maximum allowable length of Check Output,Performance Data.
     */
    private static final int MAX_ALLOWABLE_LENGTH = 200;

    /**
     * Client Id for the txt_startTime component
     */
    private static final String TXT_START_TIME_CLIENT_ID = "frmActions:actionsPortlet_txtStartTime";

    /**
     * Client Id for the txt_endTime component
     */
    private static final String TXT_END_TIME_CLIENT_ID = "frmActions:actionsPortlet_txtEndTime";

    /**
     * downtime.fixed property: downtime.fixed=yes/no
     */
    private static final String DOWNTIME_FIXED_PROPERTY = "downtime.fixed";

    /**
     * int field to check if the schedule type is fixed or not. 0 - flexible -
     * not fixed 1 - fixed
     */
    private int fixed;

    /**
     * Current time value - This field will be assigned the time of at which the
     * pop-up is being created.
     */
    private String currentTime;

    /**
     * This flag is set to true when the type fixed = 0 (i.e.flexible)
     */
    private boolean durationRequired = false;

    /**
     * return fixed value from status-viewer.properties default = 0 not set
     */
    private int getFixedFromStatusViewerProperties() {
        String propVal;
        int retVal = 0;

        propVal = PropertyUtils.getProperty(ApplicationType.STATUS_VIEWER, DOWNTIME_FIXED_PROPERTY);
        if (propVal == null || propVal.equalsIgnoreCase("no"))
            retVal = 0;
        else
            retVal = 1;

        return retVal;
    }

    /**
     * @return true if the type is flexible,false otherwise.
     */
    public boolean isDurationRequired() {
        if (fixed == 0) {
            // this means 'flexible'
            durationRequired = true;
        } else {
            // this means 'fixed'
            durationRequired = false;
        }
        return durationRequired;
    }

    /**
     * @param durationRequired
     */
    public void setDurationRequired(boolean durationRequired) {
        this.durationRequired = durationRequired;
    }

    /**
     * 
     * @return currentTime
     */
    public String getCurrentTime() {
        return currentTime;
    }

    /**
     * 
     * @param currentTime
     */
    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * @return nodeType
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * @param nodeType
     */
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * @return nodeTypeValue
     */
    public String getNodeTypeValue() {
        return nodeTypeValue;
    }

    /**
     * @param nodeTypeValue
     */
    public void setNodeTypeValue(String nodeTypeValue) {
        this.nodeTypeValue = nodeTypeValue;
    }

    /**
     * @return fixed
     */
    public int getFixed() {
        return fixed;
    }

    /**
     * @param fixed
     */
    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    /**
     * @return hostGroupName
     */
    public String getHostGroupName() {
        return hostGroupName;
    }

    /**
     * @param hostGroupName
     */
    public void setHostGroupName(String hostGroupName) {
        this.hostGroupName = hostGroupName;
    }

    /**
     * @return hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return serviceGroupName
     */
    public String getServiceGroupName() {
        return serviceGroupName;
    }

    /**
     * @param serviceGroupName
     */
    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    /**
     * @return serviceDesc
     */
    public String getServiceDesc() {
        return serviceDesc;
    }

    /**
     * @param serviceDesc
     */
    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    /**
     * @return authorName
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * @param authorName
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * @return comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return endTime
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * @return durationHours
     */
    public String getDurationHours() {
        return durationHours;
    }

    /**
     * @param durationHours
     */
    public void setDurationHours(String durationHours) {
        this.durationHours = durationHours;
    }

    /**
     * @return durationMinutes
     */
    public String getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * @param durationMinutes
     */
    public void setDurationMinutes(String durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /**
     * @return enabledForHostsToo
     */
    public boolean isEnabledForHostsToo() {
        return enabledForHostsToo;
    }

    /**
     * @param enabledForHostsToo
     */
    public void setEnabledForHostsToo(boolean enabledForHostsToo) {
        this.enabledForHostsToo = enabledForHostsToo;
    }

    /**
     * @return disabledForHostsToo
     */
    public boolean isDisabledForHostsToo() {
        return disabledForHostsToo;
    }

    /**
     * @param disabledForHostsToo
     */
    public void setDisabledForHostsToo(boolean disabledForHostsToo) {
        this.disabledForHostsToo = disabledForHostsToo;
    }

    /**
     * @return notificationDelay
     */
    public String getNotificationDelay() {
        return notificationDelay;
    }

    /**
     * @param notificationDelay
     */
    public void setNotificationDelay(String notificationDelay) {
        this.notificationDelay = notificationDelay;
    }

    /**
     * @return scheduleType
     */
    public String getScheduleType() {
        return scheduleType;
    }

    /**
     * @param scheduleType
     */
    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    /**
     * @return scheduleDowntimeForHostsToo
     */
    public boolean isScheduleDowntimeForHostsToo() {
        return scheduleDowntimeForHostsToo;
    }

    /**
     * @param scheduleDowntimeForHostsToo
     */
    public void setScheduleDowntimeForHostsToo(
            boolean scheduleDowntimeForHostsToo) {
        this.scheduleDowntimeForHostsToo = scheduleDowntimeForHostsToo;
    }

    /**
     * @return triggeredBy
     */
    public String getTriggeredBy() {
        return triggeredBy;
    }

    /**
     * @param triggeredBy
     */
    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    /**
     * @return childHosts
     */
    public String getChildHosts() {
        return childHosts;
    }

    /**
     * @param childHosts
     */
    public void setChildHosts(String childHosts) {
        this.childHosts = childHosts;
    }

    /**
     * @return ackThisHostsServicesToo
     */
    public boolean isAckThisHostsServicesToo() {
        return ackThisHostsServicesToo;
    }

    /**
     * @param ackThisHostsServicesToo
     */
    public void setAckThisHostsServicesToo(boolean ackThisHostsServicesToo) {
        this.ackThisHostsServicesToo = ackThisHostsServicesToo;
    }

    /**
     * @return sendNotification
     */
    public boolean isSendNotification() {
        return sendNotification;
    }

    /**
     * @param sendNotification
     */
    public void setSendNotification(boolean sendNotification) {
        this.sendNotification = sendNotification;
    }

    /**
     * @return persistentComment
     */
    public boolean isPersistentComment() {
        return persistentComment;
    }

    /**
     * @param persistentComment
     */
    public void setPersistentComment(boolean persistentComment) {
        this.persistentComment = persistentComment;
    }

    /**
     * @return checkTime
     */
    public String getCheckTime() {
        return checkTime;
    }

    /**
     * @param checkTime
     */
    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    /**
     * @return forceCheck
     */
    public boolean isForceCheck() {
        return forceCheck;
    }

    /**
     * @param forceCheck
     */
    public void setForceCheck(boolean forceCheck) {
        this.forceCheck = forceCheck;
    }

    /**
     * @return checkResult
     */
    public String getCheckResult() {
        return checkResult;
    }

    /**
     * @param checkResult
     */
    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    /**
     * @return checkOutput
     */
    public String getCheckOutput() {
        return checkOutput;
    }

    /**
     * @param checkOutput
     */
    public void setCheckOutput(String checkOutput) {
        this.checkOutput = checkOutput;
    }

    /**
     * @return performanceData
     */
    public String getPerformanceData() {
        return performanceData;
    }

    /**
     * @param performanceData
     */
    public void setPerformanceData(String performanceData) {
        this.performanceData = performanceData;
    }

    /**
     * This method resets all the members in CommandParamsBean
     * 
     */
    public void reset() {
        nodeType = Constant.EMPTY_STRING;
        nodeTypeValue = Constant.EMPTY_STRING;
        hostGroupName = Constant.EMPTY_STRING;
        hostName = Constant.EMPTY_STRING;
        serviceGroupName = Constant.EMPTY_STRING;
        serviceDesc = Constant.EMPTY_STRING;
        authorName = Constant.EMPTY_STRING;
        comment = Constant.EMPTY_STRING;
        startTime = Constant.EMPTY_STRING;
        endTime = Constant.EMPTY_STRING;
        durationHours = Constant.EMPTY_STRING;
        durationMinutes = Constant.EMPTY_STRING;
        enabledForHostsToo = false;
        disabledForHostsToo = false;
        notificationDelay = Constant.EMPTY_STRING;
        scheduleType = Constant.EMPTY_STRING;
        scheduleDowntimeForHostsToo = false;
        triggeredBy = Constant.EMPTY_STRING;
        childHosts = Constant.EMPTY_STRING;
        ackThisHostsServicesToo = false;
        sendNotification = false;
        persistentComment = true; 
        checkTime = Constant.EMPTY_STRING;
        checkResult = Constant.EMPTY_STRING;
        checkOutput = Constant.EMPTY_STRING;
        performanceData = Constant.EMPTY_STRING;
        fixed = getFixedFromStatusViewerProperties();
        forceCheck = false;
    }

    /**
     * This method validates the checkOutput field. 1) Should be non-empty. 2)
     * Length must be <= 200.
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validateCheckOutput(FacesContext context,
            UIComponent component, Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                context, component);
                return false;
            }
            // check for length limit.
            if (checkIfLengthExceedsLimit(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_200"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_200"),
                                context, component);
                return false;
            }
            // check for valid text input.
            if (!ValidationUtils.isValidText(inputString,
                    Constant.CHK_OUTPUT_PATTERN)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_chk_output_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_chk_output_value"),
                                context, component);
                return false;
            }

        } // (value != null)
        return true;
    }

    /**
     * This method validates the comments field. 1) Should be non-empty. 2)
     * Length must be <= 200.
     * 
     * @param context
     * @param component
     * @param value
     */
    public void validateComments(FacesContext context, UIComponent component,
            Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                context, component);
                return;
            }
            // check for length limit.
            if (checkIfLengthExceedsLimit(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_200"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_200"),
                                context, component);
                return;
            }

        } // (value != null)
    }

    /**
     * This method validates the 'Performance Data' field. 1) Could be empty. 2)
     * Length must be <= 200.
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validatePerformanceData(FacesContext context,
            UIComponent component, Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (!ValidationUtils.checkForBlankValue(inputString)) {
                // check for length limit.
                if (checkIfLengthExceedsLimit(inputString)) {
                    ((UIInput) component).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_200"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_lenghtExceeds_200"),
                                    context, component);
                    return false;
                }
            }
        } // (value != null)
        return true;
    }

    /**
     * This method validates the 'Minutes' field. 1) Could be empty. 2) Must be
     * numeric. 3) must be in the range 1-60
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validateMinutes(FacesContext context, UIComponent component,
            Object value) {
        // If the type is flexible then only minutes should be mandatory field.
        if (fixed == 0) {
            if (value != null) {
                String inputString = (String) value;
                // Check for blank input
                if (ValidationUtils.checkForBlankValue(inputString)) {
                    ((UIInput) component).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                    context, component);
                    return false;
                }
                // check for numeric value.
                if (!ValidationUtils.isNumeric(inputString)) {
                    ((UIInput) component).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_numeric_value"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_numeric_value"),
                                    context, component);
                    return false;
                }
            } // (value != null)
        }
        return true;
    }

    /**
     * This method validates the 'Hours' field. 1) Could be empty. 2) Must be
     * numeric.
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validateHours(FacesContext context, UIComponent component,
            Object value) {
        // If the type is flexible then only minutes should be mandatory field.
        if (fixed == 0) {
            if (value != null) {
                String inputString = (String) value;
                // Check for blank input
                if (ValidationUtils.checkForBlankValue(inputString)) {
                    ((UIInput) component).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                    context, component);
                    return false;
                }
                // check for numeric value.
                if (!ValidationUtils.isNumeric(inputString)) {
                    ((UIInput) component).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_numeric_value"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_numeric_value"),
                                    context, component);
                    return false;
                }
            } // (value != null)
        }
        return true;
    }

    /**
     * This method validates the Check Time field. 1) Should be non-empty. 2)
     * Format:mm/dd/yyyy hh:mm:ss 3) Must be > current time
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validateCheckTime(FacesContext context,
            UIComponent component, Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                context, component);
                return false;
            }
            // check for date format.
            if (!ValidationUtils.isValidDateFormat(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_dateFormat"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_dateFormat"),
                                context, component);
                return false;
            }
            /*
             * Check for the valid values of the
             * date,month,year,hours,minutes,seconds.
             */
            if (!ValidationUtils.validateDateTimeFields(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_datetime"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_datetime"),
                                context, component);
                return false;
            }
            // Check if the input date < current time
            if (ValidationUtils.isPastDate(inputString, getCurrentTime())) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_past_dateTime"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_past_dateTime"),
                                context, component);
                return false;
            }

        } // (value != null)
        return true;
    }

    /**
     * This method validates the startTime field. 1) Should be non-empty. 2)
     * Format:mm/dd/yyyy hh:mm:ss 3) Must be > current time 4) must be < endTime
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validateStartTime(FacesContext context,
            UIComponent component, Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                context, component);
                return false;
            }
            // check for date format.
            if (!ValidationUtils.isValidDateFormat(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_dateFormat"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_dateFormat"),
                                context, component);
                return false;
            }
            /*
             * Check for the valid values of the
             * date,month,year,hours,minutes,seconds.
             */
            if (!ValidationUtils.validateDateTimeFields(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_datetime"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_datetime"),
                                context, component);
                return false;
            }
            // Check if the input date < current time
            if (ValidationUtils.isPastDate(inputString, getCurrentTime())) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_past_dateTime"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_past_dateTime"),
                                context, component);
                return false;
            }
            // Check if the input date < endTime
            if (!ValidationUtils.isPastDate(inputString, getEndTime())) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_startDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_startDate"),
                                context, component);
                return false;
            }
        } // (value != null)

        return true;
    }

    /**
     * This method validates the endTime field. 1) Should be non-empty. 2)
     * Format:mm/dd/yyyy hh:mm:ss 3) Must be > current time 4) must be >
     * startTime
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validateEndTime(FacesContext context, UIComponent component,
            Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                context, component);
                return false;
            }
            // check for date format.
            if (!ValidationUtils.isValidDateFormat(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_dateFormat"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_dateFormat"),
                                context, component);
                return false;
            }
            /*
             * Check for the valid values of the
             * date,month,year,hours,minutes,seconds.
             */
            if (!ValidationUtils.validateDateTimeFields(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_datetime"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_datetime"),
                                context, component);
                return false;
            }
            // Check if the input date < current time
            if (ValidationUtils.isPastDate(inputString, getCurrentTime())) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_past_dateTime"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_past_dateTime"),
                                context, component);
                return false;
            }
            // Check if the input date < startTime
            if (ValidationUtils.isPastDate(inputString, getStartTime())) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_endDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_endDate"),
                                context, component);
                return false;
            }
        } // (value != null)
        return true;
    }

    /**
     * This method validates the Notification Delay field. 1) Should be
     * non-empty. 2) Must be numeric.
     * 
     * @param context
     * @param component
     * @param value
     * @return false if validation fails
     */
    public boolean validateNotificationDelay(FacesContext context,
            UIComponent component, Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_non_empty_value"),
                                context, component);
                return false;
            }
            // check for numeric value.
            if (!ValidationUtils.isNumeric(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_numeric_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_numeric_value"),
                                context, component);
                return false;
            }
        } // (value != null)
        return true;
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
     * This is an ValueChangeListener. Gets invoked whenever start time value is
     * changed. It retrieves the new value for the start time and compares it
     * with end time.
     * 
     * @param event
     */
    public void startTimeChanged(ValueChangeEvent event) {
        if (event != null) {
            setStartTime((String) event.getNewValue());
        }
        FacesContext context = FacesUtils.getFacesContext();
        if (context != null) {
            UIComponent endTimeComponent = context.getViewRoot().findComponent(
                    TXT_END_TIME_CLIENT_ID);
            if (endTimeComponent != null) {
                if (ValidationUtils.isPastDate(getEndTime(), getStartTime())) {
                    ((UIInput) endTimeComponent).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_endDate"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_endDate"),
                                    context, endTimeComponent);
                } else {
                    ((UIInput) endTimeComponent).setValid(true);
                    ValidationUtils.clearFacesMessages(context,
                            endTimeComponent.getClientId(context));
                }
            }
        }
    }

    /**
     * This is an ValueChangeListener. Gets invoked whenever end time value is
     * changed. It retrieves the new value for the end time and compares it with
     * start time.
     * 
     * @param event
     */
    public void endTimeChanged(ValueChangeEvent event) {
        if (event != null) {
            setEndTime((String) event.getNewValue());
        }
        FacesContext context = FacesUtils.getFacesContext();
        if (context != null) {
            UIComponent startTimeComponent = context.getViewRoot()
                    .findComponent(TXT_START_TIME_CLIENT_ID);
            if (startTimeComponent != null) {
                // Check if the input date < endTime
                if (!ValidationUtils.isPastDate(getStartTime(), getEndTime())) {
                    ((UIInput) startTimeComponent).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_startDate"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invallid_startDate"),
                                    context, startTimeComponent);
                } else {
                    ((UIInput) startTimeComponent).setValid(true);
                    ValidationUtils.clearFacesMessages(context,
                            startTimeComponent.getClientId(context));
                }
            }
        }
    }

}
