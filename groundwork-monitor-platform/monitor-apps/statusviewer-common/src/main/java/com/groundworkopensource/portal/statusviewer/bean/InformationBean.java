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

/**
 * Information bean for supporting Host Information portlet and Service
 * Information portlet.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class InformationBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4843371225279203059L;

    /**
     * Status Value. Example => OK - 172.28.112.56: rta 6.605ms, lost 0%
     */
    private String statusValue;
    /**
     * ScheduledDowntimeDepth - If value of this property is '1' then display
     * 'Yes', for '0' display 'No'.
     */
    private String scheduleDowntime;
    /**
     * Last Notification Time.
     */
    private String lastNotificationTime;
    /**
     * Notification Count - Current Notification Number.
     */
    private String currentNotificationNumber;
    /**
     * Next Check Time.
     */
    private String nextCheckTime;
    /**
     * Check Type - Active / Passive / both
     */
    private String checkType;
    /**
     * Current Check Attempts. (Say 1 attempt out of 3).
     */
    private String currentCheckAttempts;
    /**
     * Max Check Attempts.
     */
    private String maxCheckAttempts;
    /**
     * State Type - Soft / Hard.
     */
    private String stateType;
    /**
     * Latency - the time between when an active service check is scheduled to
     * be executed and the time it actually executes. This term is called
     * latency and is an indicator of the load on the server.
     */
    private String latency;
    /**
     * Latency Threshold Warning.
     */
    private String latencyThresholdWarning;

    /**
     * Duration between 2 checks.
     */
    private String duration;
    /**
     * % State Change (in host state).
     */
    private String percentageStateChange;

    /**
     * ROUND_BULLET_IMAGE_PATH
     */
    // private String roundBulletIconPath = "/images/round-bullet.gif";
    /**
     * Show Latency Warning
     */
    private boolean showLatencyWarning = false;

    /**
     * Is Active Checks enabled?
     */
    private boolean activeChecksEnabled;

    /**
     * Is Notifications enabled?
     */
    private boolean notificationsEnabled;

    /**
     * URL for Nagios (Host / Service)
     */
    private String nagiosLink;

    /**
     * host or service : isAcknowledged.
     */
    private String acknowledged;

    /**
     * Host Name
     */
    private String hostName;

    /**
     * Service Name
     */
    private String serviceName;

    /**
     * Custom Link 1 URL Value - Host in that URL
     */
    private String customLink1URLValue;

    /**
     * Custom Link 1 URL
     */
    private String customLink1URL;

    /**
     * Custom Link 2 URL Value - Host in that URL
     */
    private String customLink2URLValue;

    /**
     * Custom Link 2 URL
     */
    private String customLink2URL;

    /**
     * Custom Link 3 URL Value - Host in that URL
     */
    private String customLink3URLValue;

    /**
     * Custom Link 3 URL
     */
    private String customLink3URL;

    /**
     * Custom Link 4 URL Value - Host in that URL
     */
    private String customLink4URLValue;

    /**
     * Custom Link 4 URL
     */
    private String customLink4URL;

    /**
     * Custom Link 5 URL Value - Host in that URL
     */
    private String customLink5URLValue;

    /**
     * Custom Link 5 URL
     */
    private String customLink5URL;

    /**
     * To check if logged in User has Admin or Operator Role
     */
    private boolean userInAdminOrOperatorRole;

    /**
     * Last Check Time.
     */
    private String lastCheckTime;
    
    private String applicationType = null;

    public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	/**
     * Returns the statusValue.
     * 
     * @return the statusValue
     */
    public String getStatusValue() {
        return statusValue;
    }

    /**
     * Sets the statusValue.
     * 
     * @param statusValue
     *            the statusValue to set
     */
    public void setStatusValue(String statusValue) {
        this.statusValue = statusValue;
    }

    /**
     * Returns the scheduleDowntime.
     * 
     * @return the scheduleDowntime
     */
    public String getScheduleDowntime() {
        return scheduleDowntime;
    }

    /**
     * Sets the scheduleDowntime.
     * 
     * @param scheduleDowntime
     *            the scheduleDowntime to set
     */
    public void setScheduleDowntime(String scheduleDowntime) {
        this.scheduleDowntime = scheduleDowntime;
    }

    /**
     * Returns the lastNotificationTime.
     * 
     * @return the lastNotificationTime
     */
    public String getLastNotificationTime() {
        return lastNotificationTime;
    }

    /**
     * Sets the lastNotificationTime.
     * 
     * @param lastNotificationTime
     *            the lastNotificationTime to set
     */
    public void setLastNotificationTime(String lastNotificationTime) {
        this.lastNotificationTime = lastNotificationTime;
    }

    /**
     * Returns the nextCheckTime.
     * 
     * @return the nextCheckTime
     */
    public String getNextCheckTime() {
        return nextCheckTime;
    }

    /**
     * Sets the nextCheckTime.
     * 
     * @param nextCheckTime
     *            the nextCheckTime to set
     */
    public void setNextCheckTime(String nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    /**
     * Returns the checkType.
     * 
     * @return the checkType
     */
    public String getCheckType() {
        return checkType;
    }

    /**
     * Sets the checkType.
     * 
     * @param checkType
     *            the checkType to set
     */
    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    /**
     * Returns the currentCheckAttempts.
     * 
     * @return the currentCheckAttempts
     */
    public String getCurrentCheckAttempts() {
        return currentCheckAttempts;
    }

    /**
     * Sets the currentCheckAttempts.
     * 
     * @param currentCheckAttempts
     *            the currentCheckAttempts to set
     */
    public void setCurrentCheckAttempts(String currentCheckAttempts) {
        this.currentCheckAttempts = currentCheckAttempts;
    }

    /**
     * Returns the maxCheckAttempts.
     * 
     * @return the maxCheckAttempts
     */
    public String getMaxCheckAttempts() {
        return maxCheckAttempts;
    }

    /**
     * Sets the maxCheckAttempts.
     * 
     * @param maxCheckAttempts
     *            the maxCheckAttempts to set
     */
    public void setMaxCheckAttempts(String maxCheckAttempts) {
        this.maxCheckAttempts = maxCheckAttempts;
    }

    /**
     * Returns the latency.
     * 
     * @return the latency
     */
    public String getLatency() {
        return latency;
    }

    /**
     * Sets the latency.
     * 
     * @param latency
     *            the latency to set
     */
    public void setLatency(String latency) {
        this.latency = latency;
    }

    /**
     * Returns the duration.
     * 
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the duration.
     * 
     * @param duration
     *            the duration to set
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Returns the percentageStateChange.
     * 
     * @return the percentageStateChange
     */
    public String getPercentageStateChange() {
        return percentageStateChange;
    }

    /**
     * Sets the percentageStateChange.
     * 
     * @param percentageStateChange
     *            the percentageStateChange to set
     */
    public void setPercentageStateChange(String percentageStateChange) {
        this.percentageStateChange = percentageStateChange;
    }

    /**
     * Sets the roundBulletIconPath.
     * 
     * @param roundBulletIconPath
     *            the roundBulletIconPath to set
     */
    // public void setRoundBulletIconPath(String roundBulletIconPath) {
    // this.roundBulletIconPath = roundBulletIconPath;
    // }
    /**
     * Returns the roundBulletIconPath.
     * 
     * @return the roundBulletIconPath
     */
    // public String getRoundBulletIconPath() {
    // return roundBulletIconPath;
    // }
    /**
     * Sets the currentNotificationNumber.
     * 
     * @param currentNotificationNumber
     *            the currentNotificationNumber to set
     */
    public void setCurrentNotificationNumber(String currentNotificationNumber) {
        this.currentNotificationNumber = currentNotificationNumber;
    }

    /**
     * Returns the currentNotificationNumber.
     * 
     * @return the currentNotificationNumber
     */
    public String getCurrentNotificationNumber() {
        return currentNotificationNumber;
    }

    /**
     * Sets the latencyThresholdWarning.
     * 
     * @param latencyThresholdWarning
     *            the latencyThresholdWarning to set
     */
    public void setLatencyThresholdWarning(String latencyThresholdWarning) {
        this.latencyThresholdWarning = latencyThresholdWarning;
    }

    /**
     * Returns the latencyThresholdWarning.
     * 
     * @return the latencyThresholdWarning
     */
    public String getLatencyThresholdWarning() {
        return latencyThresholdWarning;
    }

    /**
     * Sets the stateType.
     * 
     * @param stateType
     *            the stateType to set
     */
    public void setStateType(String stateType) {
        this.stateType = stateType;
    }

    /**
     * Returns the stateType.
     * 
     * @return the stateType
     */
    public String getStateType() {
        return stateType;
    }

    /**
     * Sets the showLatencyWarning.
     * 
     * @param showLatencyWarning
     *            the showLatencyWarning to set
     */
    public void setShowLatencyWarning(boolean showLatencyWarning) {
        this.showLatencyWarning = showLatencyWarning;
    }

    /**
     * Returns the showLatencyWarning.
     * 
     * @return the showLatencyWarning
     */
    public boolean isShowLatencyWarning() {
        return showLatencyWarning;
    }

    /**
     * Sets the activeChecksEnabled.
     * 
     * @param activeChecksEnabled
     *            the activeChecksEnabled to set
     */
    public void setActiveChecksEnabled(boolean activeChecksEnabled) {
        this.activeChecksEnabled = activeChecksEnabled;
    }

    /**
     * Returns the activeChecksEnabled.
     * 
     * @return the activeChecksEnabled
     */
    public boolean isActiveChecksEnabled() {
        return activeChecksEnabled;
    }

    /**
     * Sets the notificationsEnabled.
     * 
     * @param notificationsEnabled
     *            the notificationsEnabled to set
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Returns the notificationsEnabled.
     * 
     * @return the notificationsEnabled
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets the nagiosLink.
     * 
     * @param nagiosLink
     *            the nagiosLink to set
     */
    public void setNagiosLink(String nagiosLink) {
        this.nagiosLink = nagiosLink;
    }

    /**
     * Returns the nagiosLink.
     * 
     * @return the nagiosLink
     */
    public String getNagiosLink() {
        return nagiosLink;
    }

    /**
     * Returns the acknowledged.
     * 
     * @return the acknowledged
     */
    public String getAcknowledged() {
        return acknowledged;
    }

    /**
     * Sets the acknowledged.
     * 
     * @param acknowledged
     *            the acknowledged to set
     */
    public void setAcknowledged(String acknowledged) {
        this.acknowledged = acknowledged;
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
        return hostName;
    }

    /**
     * Sets the serviceName.
     * 
     * @param serviceName
     *            the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Returns the serviceName.
     * 
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the customLink1URL.
     * 
     * @param customLink1URL
     *            the customLink1URL to set
     */
    public void setCustomLink1URL(String customLink1URL) {
        this.customLink1URL = customLink1URL;
    }

    /**
     * Returns the customLink1URL.
     * 
     * @return the customLink1URL
     */
    public String getCustomLink1URL() {
        return customLink1URL;
    }

    /**
     * Returns the customLink1URLValue.
     * 
     * @return the customLink1URLValue
     */
    public String getCustomLink1URLValue() {
        return customLink1URLValue;
    }

    /**
     * Sets the customLink1URLValue.
     * 
     * @param customLink1URLValue
     *            the customLink1URLValue to set
     */
    public void setCustomLink1URLValue(String customLink1URLValue) {
        this.customLink1URLValue = customLink1URLValue;
    }

    /**
     * Returns the customLink2URLValue.
     * 
     * @return the customLink2URLValue
     */
    public String getCustomLink2URLValue() {
        return customLink2URLValue;
    }

    /**
     * Sets the customLink2URLValue.
     * 
     * @param customLink2URLValue
     *            the customLink2URLValue to set
     */
    public void setCustomLink2URLValue(String customLink2URLValue) {
        this.customLink2URLValue = customLink2URLValue;
    }

    /**
     * Returns the customLink2URL.
     * 
     * @return the customLink2URL
     */
    public String getCustomLink2URL() {
        return customLink2URL;
    }

    /**
     * Sets the customLink2URL.
     * 
     * @param customLink2URL
     *            the customLink2URL to set
     */
    public void setCustomLink2URL(String customLink2URL) {
        this.customLink2URL = customLink2URL;
    }

    /**
     * Returns the customLink3URLValue.
     * 
     * @return the customLink3URLValue
     */
    public String getCustomLink3URLValue() {
        return customLink3URLValue;
    }

    /**
     * Sets the customLink3URLValue.
     * 
     * @param customLink3URLValue
     *            the customLink3RLValue to set
     */
    public void setCustomLink3URLValue(String customLink3URLValue) {
        this.customLink3URLValue = customLink3URLValue;
    }

    /**
     * Returns the customLink3URL.
     * 
     * @return the customLink3URL
     */
    public String getCustomLink3URL() {
        return customLink3URL;
    }

    /**
     * Sets the customLink3URL.
     * 
     * @param customLink3URL
     *            the customLink3URL to set
     */
    public void setCustomLink3URL(String customLink3URL) {
        this.customLink3URL = customLink3URL;
    }

    /**
     * Sets the customLink4URLValue.
     * 
     * @param customLink4URLValue
     *            the customLink4URLValue to set
     */
    public void setCustomLink4URLValue(String customLink4URLValue) {
        this.customLink4URLValue = customLink4URLValue;
    }

    /**
     * Returns the customLink4URLValue.
     * 
     * @return the customLink4URLValue
     */
    public String getCustomLink4URLValue() {
        return customLink4URLValue;
    }

    /**
     * Sets the customLink4URL.
     * 
     * @param customLink4URL
     *            the customLink4URL to set
     */
    public void setCustomLink4URL(String customLink4URL) {
        this.customLink4URL = customLink4URL;
    }

    /**
     * Returns the customLink4URL.
     * 
     * @return the customLink4URL
     */
    public String getCustomLink4URL() {
        return customLink4URL;
    }

    /**
     * Sets the customLink5URLValue.
     * 
     * @param customLink5URLValue
     *            the customLink5URLValue to set
     */
    public void setCustomLink5URLValue(String customLink5URLValue) {
        this.customLink5URLValue = customLink5URLValue;
    }

    /**
     * Returns the customLink5URLValue.
     * 
     * @return the customLink5URLValue
     */
    public String getCustomLink5URLValue() {
        return customLink5URLValue;
    }

    /**
     * Sets the customLink5URL.
     * 
     * @param customLink5URL
     *            the customLink5URL to set
     */
    public void setCustomLink5URL(String customLink5URL) {
        this.customLink5URL = customLink5URL;
    }

    /**
     * Returns the customLink5URL.
     * 
     * @return the customLink5URL
     */
    public String getCustomLink5URL() {
        return customLink5URL;
    }

    /**
     * Sets the userInAdminOrOperatorRole.
     * 
     * @param userInAdminOrOperatorRole
     *            the userInAdminOrOperatorRole to set
     */
    public void setUserInAdminOrOperatorRole(boolean userInAdminOrOperatorRole) {
        this.userInAdminOrOperatorRole = userInAdminOrOperatorRole;
    }

    /**
     * Returns the userInAdminOrOperatorRole.
     * 
     * @return the userInAdminOrOperatorRole
     */
    public boolean isUserInAdminOrOperatorRole() {
        return userInAdminOrOperatorRole;
    }

    /**
     * Sets the lastCheckTime.
     * 
     * @param lastCheckTime
     *            the lastCheckTime to set
     */
    public void setLastCheckTime(String lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    /**
     * Returns the lastCheckTime.
     * 
     * @return the lastCheckTime
     */
    public String getLastCheckTime() {
        return lastCheckTime;
    }
}
