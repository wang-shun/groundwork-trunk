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
import java.util.Map;

/**
 * This class provide data to event portlet.
 * 
 * @author manish_kjain
 * 
 */
public class EventBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3466866107757254520L;

    /**
     * device of event
     */
    private String device;
    /**
     * severity of event
     */
    private SeverityBean severity;
    /**
     * status bean of event
     */
    private StatusBean statusBean;
    /**
     * Application Type of event
     */
    private String applicationType;
    /**
     * dateTime Type of event
     */
    private String dateTime;
    /**
     * textMessage Type of event
     */
    private String textMessage;
    /**
     * is row selected.
     */
    private boolean selected = false;
    /**
     * total count of event.
     */
    private int totalCount;
    /**
     * log message ID
     */
    private int logMessageID;
    /**
     * message count.
     */
    private int msgCount;
    /**
     * report date of log message
     */
    private String reportDate;
    /**
     * last Insert Date of log message
     */
    private String lastInsertDate;
    /**
     * first Insert Date of log message
     */
    private String firstInsertDate;
    /**
     * dynamicProperty map
     */
    private Map<String, Object> dynamicProperty;

    /**
     * Returns the selected.
     * 
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets the selected.
     * 
     * @param selected
     *            the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns the totalCount.
     * 
     * @return the totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the totalCount.
     * 
     * @param totalCount
     *            the totalCount to set
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Returns the device.
     * 
     * @return the device
     */
    public String getDevice() {
        return device;
    }

    /**
     * Sets the device.
     * 
     * @param device
     *            the device to set
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * Returns the severity.
     * 
     * @return the severity
     */
    public SeverityBean getSeverity() {
        return severity;
    }

    /**
     * Sets the severity.
     * 
     * @param severity
     *            the severity to set
     */
    public void setSeverity(SeverityBean severity) {
        this.severity = severity;
    }

    /**
     * Returns the statusBean.
     * 
     * @return the statusBean
     */
    public StatusBean getStatusBean() {
        return statusBean;
    }

    /**
     * Sets the statusBean.
     * 
     * @param statusBean
     *            the statusBean to set
     */
    public void setStatusBean(StatusBean statusBean) {
        this.statusBean = statusBean;
    }

    /**
     * Returns the applicationType.
     * 
     * @return the applicationType
     */
    public String getApplicationType() {
        return applicationType;
    }

    /**
     * Sets the applicationType.
     * 
     * @param applicationType
     *            the applicationType to set
     */
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    /**
     * Returns the dateTime.
     * 
     * @return the dateTime
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Sets the dateTime.
     * 
     * @param dateTime
     *            the dateTime to set
     */
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns the textMessage.
     * 
     * @return the textMessage
     */
    public String getTextMessage() {
        return textMessage;
    }

    /**
     * Sets the textMessage.
     * 
     * @param textMessage
     *            the textMessage to set
     */
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    /**
     * Returns the logMessageID.
     * 
     * @return the logMessageID
     */
    public int getLogMessageID() {
        return logMessageID;
    }

    /**
     * Sets the logMessageID.
     * 
     * @param logMessageID
     *            the logMessageID to set
     */
    public void setLogMessageID(int logMessageID) {
        this.logMessageID = logMessageID;
    }

    /**
     * Returns the msgCount.
     * 
     * @return the msgCount
     */
    public int getMsgCount() {
        return msgCount;
    }

    /**
     * Sets the msgCount.
     * 
     * @param msgCount
     *            the msgCount to set
     */
    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    /**
     * Returns the reportDate.
     * 
     * @return the reportDate
     */
    public String getReportDate() {
        return reportDate;
    }

    /**
     * Sets the reportDate.
     * 
     * @param reportDate
     *            the reportDate to set
     */
    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    /**
     * Returns the lastInsertDate.
     * 
     * @return the lastInsertDate
     */
    public String getLastInsertDate() {
        return lastInsertDate;
    }

    /**
     * Sets the lastInsertDate.
     * 
     * @param lastInsertDate
     *            the lastInsertDate to set
     */
    public void setLastInsertDate(String lastInsertDate) {
        this.lastInsertDate = lastInsertDate;
    }

    /**
     * Returns the firstInsertDate.
     * 
     * @return the firstInsertDate
     */
    public String getFirstInsertDate() {
        return firstInsertDate;
    }

    /**
     * Sets the firstInsertDate.
     * 
     * @param firstInsertDate
     *            the firstInsertDate to set
     */
    public void setFirstInsertDate(String firstInsertDate) {
        this.firstInsertDate = firstInsertDate;
    }

    /**
     * Sets the dynamicProperty.
     * 
     * @param dynamicProperty
     *            the dynamicProperty to set
     */
    public void setDynamicProperty(Map<String, Object> dynamicProperty) {
        this.dynamicProperty = dynamicProperty;
    }

    /**
     * Returns the dynamicProperty.
     * 
     * @return the dynamicProperty
     */
    public Map<String, Object> getDynamicProperty() {
        return dynamicProperty;
    }

}
