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
 * Bean for displaying Host List
 * 
 * @author nitin_jadhav
 */

public class HostBean {

    /**
     * URL
     */
    private String url = "";

    /**
     * Host Id
     */
    private int hostId;

    /**
     * host name
     */
    private String name;

    /**
     * host status
     */
    private NetworkObjectStatusEnum status;

    /**
     * duration for which host is in current date
     */
    private String duration;

    /**
     * status Information Details
     */
    private String statusInfoDetails;

    /**
     * is host acknowledged?
     */
    private boolean acknowledged;

    /**
     * Field indicating if the service has a status eligible to be acknowledged
     * - e.g. Service in OK state is not required to be acknowledged.
     */
    private boolean acknowledgeStatus = true;

    /**
     * serviceStatus Tool Tip including header to show which type of services
     * are most Important (bubbled up), when mouse hours over Aggregated service
     * status for host
     */
    private String serviceStatusToolTip;

    /**
     * Total number of hosts in selected host group
     */
    private int totalCount;

    /**
     * aggregated service status of host. this is the status determined by
     * priority of services.
     */
    private NetworkObjectStatusEnum serviceStatus;

    /**
     * Tooltip for status info
     */
    private String statusInfoTooltip;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Constructor
     * 
     * @param hostId
     * @param name
     * @param status
     * @param duration
     * @param statusInfoDetails
     * @param acknowledged
     * @param statusInfotooltip
     */
    public HostBean(int hostId, String name, NetworkObjectStatusEnum status,
            String duration, String statusInfoDetails, boolean acknowledged,
            String statusInfotooltip) {
        this.hostId = hostId;
        this.name = name;
        this.status = status;
        this.duration = duration;
        this.statusInfoDetails = statusInfoDetails;
        this.acknowledged = acknowledged;
        this.statusInfoTooltip = statusInfotooltip;
    }

    /**
     * Default constructor
     */
    public HostBean() {
        // Auto-generated constructor stub
    }

    /**
     * returned test according to "acknowledged" value
     * 
     * @return the acknowledged
     */
    public String getHostAcknowledged() {
        if (acknowledged) {
            return Constant.YES;
        }
        return Constant.NO;
    }

    /**
     * @return the status
     */
    public NetworkObjectStatusEnum getStatus() {
        return status;
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
     * If length of statusInfoDetails > MAX_CHARS_IN_DETAILS_COLUMN, return full
     * statusInfoDetails.
     * 
     * otherwise return lastUpdate as tool tip
     * 
     * @return the statusInfoDetails
     */
    public String getStatusInfoDetailsToolTip() {
        return statusInfoTooltip;
    }

    /**
     * returns tool tip when mouse hovers over aggregated service status for
     * host in host list in the form of list of services, having most important
     * state
     * 
     * @return tool tip string
     */
    public String getServiceStatusToolTip() {
        return serviceStatusToolTip;

    }

    /**
     * @return the hostId
     */
    public int getHostId() {
        return hostId;
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
     * Sets the serviceStatus.
     * 
     * @param serviceStatus
     *            the serviceStatus to set
     */
    public void setServiceStatus(NetworkObjectStatusEnum serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    /**
     * Returns the serviceStatus.
     * 
     * @return the serviceStatus
     */
    public NetworkObjectStatusEnum getServiceStatus() {
        return serviceStatus;
    }

    /**
     * Sets the serviceStatusToolTip.
     * 
     * @param serviceStatusToolTip
     *            the serviceStatusToolTip to set
     */
    public void setServiceStatusToolTip(String serviceStatusToolTip) {
        this.serviceStatusToolTip = serviceStatusToolTip;
    }

}
