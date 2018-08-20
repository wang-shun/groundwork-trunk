/*
 * Copyright (C) 2017 GroundWork Open Source, Inc. (GroundWork) All rights
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
package com.groundwork.dashboard.portlets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.groundwork.downtime.DowntimeMaintenanceWindow;
import org.joda.time.Period;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NocBoardService extends DashboardService {

    //status text from last check
    protected String statusText;
    //check box value for if the active check for Nagios is disabled
    protected boolean activeCheck;
    // Date/Time that the host went into its latest state
    protected Date timeStarted;
    // Time(duration) in latest state in seconds
    protected long timeInState;
    // formatted duration
    protected String duration;
    //Active/Pending/Expired/None
    protected String maintenanceStatus;
    //maintenance status message
    protected String maintenanceMessage;
    //value to show in graphic bar
    protected float maintenancePercent;
    //Ack value
    protected boolean ackBool;
    //Value of who ack-ed
    protected String acknowledger;
    // acknowledgement comment
    protected String acknowledgeComment;
    //Availability percent
    protected float availability;
    //list of comments on this host
    protected List<NocBoardComment> commentsList = new ArrayList<>();
    //list of notifications on this host
    protected List<String> notificationsList = new ArrayList<>();
    // list of downtimes
    protected List<DowntimeMaintenanceWindow> maintenanceWindows = new ArrayList<>();
    private String appType;

    public NocBoardService() {
        super("", "", new Date(), "");
        this.statusText = "";
        this.activeCheck = false;
        this.timeStarted = new Date();
        this.timeInState = 0;
        this.maintenanceStatus = "";
        this.maintenanceMessage = "";
        this.maintenancePercent = 0;
        this.ackBool = false;
        this.acknowledger = "";
        this.acknowledgeComment = "";
        this.availability = 0;
        this.appType = "";
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public boolean isActiveCheck() {
        return activeCheck;
    }

    public void setActiveCheck(boolean activeCheck) {
        this.activeCheck = activeCheck;
    }

    public Date getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(Date timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeInState() {
        return timeInState;
    }

    public void setTimeInState(long ms) {
        this.timeInState = ms;
        Period period = new Period(ms);
        this.duration = String.format("%02dh:%02dm:%02ds", period.getHours(), period.getMinutes(), period.getSeconds());
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMaintenanceStatus() {
        return maintenanceStatus;
    }

    public void setMaintenanceStatus(String maintenanceStatus) {
        this.maintenanceStatus = maintenanceStatus;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public float getMaintenancePercent() {
        return maintenancePercent;
    }

    public void setMaintenancePercent(float maintenancePercent) {
        this.maintenancePercent = maintenancePercent;
    }

    public boolean getAckBool() {
        return ackBool;
    }

    public void setAckBool(boolean ackBool) {
        this.ackBool = ackBool;
    }

    public String getAcknowledger() {
        return acknowledger;
    }

    public void setAcknowledger(String acknowledger) {
        this.acknowledger = acknowledger;
    }

    public String getAcknowledgeComment() {
        return acknowledgeComment;
    }

    public void setAcknowledgeComment(String acknowledgeComment) {
        this.acknowledgeComment = acknowledgeComment;
    }

    public float getAvailability() {
        return availability;
    }

    public void setAvailability(float availability) {
        this.availability = availability;
    }

    public List<NocBoardComment> getCommentsList() {
        return commentsList;
    }

    public void setCommentsList(List<NocBoardComment> commentsList) {
        this.commentsList = commentsList;
    }

    public List<String> getNotificationsList() {
        return notificationsList;
    }

    public void setNotificationsList(List<String> notificationsList) {
        this.notificationsList = notificationsList;
    }

    // returns the number of comments for this host
    public int getNumComments() {
        return commentsList.size();
    }

    // returns the number of comments for this host
    public int getNumNotifications() {
        return notificationsList.size();
    }

    public List<DowntimeMaintenanceWindow> getMaintenanceWindows() {
        return maintenanceWindows;
    }

    public void setMaintenanceWindows(List<DowntimeMaintenanceWindow> maintenanceWindows) {
        this.maintenanceWindows = maintenanceWindows;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }
}
