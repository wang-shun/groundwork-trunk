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

import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;

/**
 * Bean for displaying Service List
 * 
 * @author mridu_narang
 */

public class ServiceBean {

    /**
     * URL
     */
    private String url = "";

    /**
     * parentURL
     */
    private String parentURL = "";

    /**
     * Service name
     */
    private String serviceName;

    /**
     * Service status
     */
    private NetworkObjectStatusEnum serviceStatus;

    /**
     * Host name that the service belongs to
     */
    private String hostName;

    /**
     * Duration for which service is in current state
     */
    private String duration;

    /**
     * Status Information Details
     */
    private String statusInfoDetails;

    /**
     * Field indicating if the service has a status eligible to be acknowledged
     * - e.g. Service in OK state is not required to be acknowledged.
     */
    private boolean acknowledgeStatus = true;

    /**
     * Boolean field to indicate if service is acknowledged
     */
    private boolean isAcknowledged;

    /**
     * MAX_CHARS_IN_DETAILS_COLUMN
     */
    private int maxStatusInfoTooltipChars;

    /**
     * Field indicating total number of pages in UI for pagination.
     * 
     */
    private int totalCount;

    /**
     * status info details statusInfoTooltip
     */
    private String statusInfoTooltip;

    /**
     * Service application type
     */
    private String applicationType;

    // CONSTRUCTOR
    /**
     * Default constructor
     * 
     * @param serviceName
     * @param hostName
     * @param status
     * @param duration
     * @param statusInfoDetails
     * @param acknowledged
     * @param statusInfoTooltip
     * @param applicationType
     */
    public ServiceBean(String serviceName, String hostName,
            NetworkObjectStatusEnum status, String duration,
            String statusInfoDetails, boolean acknowledged,
            String statusInfoTooltip, String applicationType) {

        setServiceName(serviceName);
        setHostName(hostName);
        setServiceStatus(status);
        setDuration(duration);
        setStatusInfoDetails(statusInfoDetails);
        setAcknowledged(acknowledged);
        setStatusInfoTooltip(statusInfoTooltip);
        setApplicationType(applicationType);
    }

    /**
     * Empty constructor
     */
    public ServiceBean() {
        /* empty */
    }

    // GETTERS AND SETTERS
    /**
     * Returns the name.
     * 
     * @return the name
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * Sets the name.
     * 
     * @param serviceName
     *            the name to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
     * Sets the hostName.
     * 
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Returns the duration.
     * 
     * @return the duration
     */
    public String getDuration() {
        return this.duration;
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
     * Sets the statusInfoDetails.
     * 
     * @param statusInfoDetails
     *            the statusInfoDetails to set
     */
    public void setStatusInfoDetails(String statusInfoDetails) {
        this.statusInfoDetails = statusInfoDetails;
    }

    /**
     * Returns the acknowledged.
     * 
     * @return the acknowledged
     */
    public boolean isAcknowledged() {
        return this.isAcknowledged;
    }

    /**
     * Sets the acknowledged.
     * 
     * @param isAcknowledged
     *            the acknowledged to set
     */
    public void setAcknowledged(boolean isAcknowledged) {
        this.isAcknowledged = isAcknowledged;
    }

    /**
     * Returns the serviceStatus.
     * 
     * @return the serviceStatus
     */
    public NetworkObjectStatusEnum getServiceStatus() {
        return this.serviceStatus;
    }

    /**
     * Sets the serviceStatus.
     * 
     * @param serviceStatus
     *            the serviceStatus to set
     */
    public void setServiceStatus(NetworkObjectStatusEnum serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    /**
     * Returns text according to the "acknowledged" value as YES or NO for the
     * service
     * 
     * @return the acknowledged
     */
    public String getServiceAcknowledged() {
        if (this.isAcknowledged) {
            return Constant.YES;
        }
        return Constant.NO;
    }

    /**
     * If length of statusInfoDetails > MAX_CHARS_IN_DETAILS_COLUMN, return full
     * statusInfoDetails, otherwise return lastUpdate as tool tip
     * 
     * @return the statusInfoDetails
     */
    public String getStatusInfoDetailsToolTip() {
        return statusInfoTooltip;
    }

    /**
     * If length of statusInfoDetails > MAX_CHARS_IN_DETAILS_COLUMN, return only
     * first MAX_CHARS_IN_DETAILS_COLUMN characters
     * 
     * else return statusInfoDetails
     * 
     * @return the statusInfoDetails
     */
    public String getStatusInfoDetails() {
        return statusInfoDetails;
    }

    /**
     * Sets the maxStatusInfoTooltipChars.
     * 
     * @param maxStatusInfoTooltipChars
     *            the maxStatusInfoTooltipChars to set
     */
    public void setMaxStatusInfoTooltipChars(int maxStatusInfoTooltipChars) {
        this.maxStatusInfoTooltipChars = maxStatusInfoTooltipChars;
    }

    /**
     * Returns the maxStatusInfoTooltipChars.
     * 
     * @return the maxStatusInfoTooltipChars
     */
    public int getMaxStatusInfoTooltipChars() {
        return this.maxStatusInfoTooltipChars;
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
     * Returns the totalCount.
     * 
     * @return the totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the url.
     * 
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the url.
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the parentURL.
     * 
     * @param parentURL
     *            the parentURL to set
     */
    public void setParentURL(String parentURL) {
        this.parentURL = parentURL;
    }

    /**
     * Returns the parentURL.
     * 
     * @return the parentURL
     */
    public String getParentURL() {
        return parentURL;
    }

    /**
     * Sets the acknowledgeStatus.
     * 
     * @param acknowledgeStatus
     *            the acknowledgeStatus to set
     */
    public void setAcknowledgeStatus(boolean acknowledgeStatus) {
        this.acknowledgeStatus = acknowledgeStatus;
    }

    /**
     * Returns the acknowledgeStatus.
     * 
     * @return the acknowledgeStatus
     */
    public boolean isAcknowledgeStatus() {
        return acknowledgeStatus;
    }

    /**
     * Sets the statusInfoTooltip.
     * 
     * @param statusInfoTooltip
     *            the statusInfoTooltip to set
     */
    public void setStatusInfoTooltip(String statusInfoTooltip) {
        this.statusInfoTooltip = statusInfoTooltip;
    }

    /**
     * Returns the statusInfoTooltip.
     * 
     * @return the statusInfoTooltip
     */
    public String getStatusInfoTooltip() {
        return statusInfoTooltip;
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
}
