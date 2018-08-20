package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;

import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;

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

/**
 * Bean for Host Group Health portlet.
 * 
 * @author swapnil_gujrathi
 */
public class ServiceGroupHealthBean implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4969088648174294788L;

    /**
     * service Group Name.
     */
    private String serviceGroupName;

    /**
     * service Group Name.
     */
    private String serviceGroupNameLabel;

    /**
     * Service Up time.
     */
    private String serviceUptime;
    /**
     * Host Group Status.
     */
    private NetworkObjectStatusEnum status;
    /**
     * Host Group State Time - time from which Host Group is in this particular
     * state.
     */
    private String serviceGroupStateTime;

    /**
     * Description for the Service Group.
     */
    private String serviceGroupDescription;

    /**
     * Returns the serviceGroupName.
     * 
     * @return the serviceGroupName
     */
    public String getServiceGroupName() {
        return serviceGroupName;
    }

    /**
     * Sets the serviceGroupName.
     * 
     * @param serviceGroupName
     *            the serviceGroupName to set
     */
    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    /**
     * Returns the serviceUptime.
     * 
     * @return the serviceUptime
     */
    public String getServiceUptime() {
        return serviceUptime;
    }

    /**
     * Sets the serviceUptime.
     * 
     * @param serviceUptime
     *            the serviceUptime to set
     */
    public void setServiceUptime(String serviceUptime) {
        this.serviceUptime = serviceUptime;
    }

    /**
     * Returns the serviceGroupStateTime.
     * 
     * @return the serviceGroupStateTime
     */
    public String getServiceGroupStateTime() {
        return serviceGroupStateTime;
    }

    /**
     * Sets the serviceGroupStateTime.
     * 
     * @param serviceGroupStateTime
     *            the serviceGroupStateTime to set
     */
    public void setServiceGroupStateTime(String serviceGroupStateTime) {
        this.serviceGroupStateTime = serviceGroupStateTime;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            the status to set
     */
    public void setStatus(NetworkObjectStatusEnum status) {
        this.status = status;
    }

    /**
     * Returns the status.
     * 
     * @return the status
     */
    public NetworkObjectStatusEnum getStatus() {
        return status;
    }

    /**
     * Sets the serviceGroupNameLabel.
     * 
     * @param serviceGroupNameLabel
     *            the serviceGroupNameLabel to set
     */
    public void setServiceGroupNameLabel(String serviceGroupNameLabel) {
        this.serviceGroupNameLabel = serviceGroupNameLabel;
    }

    /**
     * Returns the serviceGroupNameLabel.
     * 
     * @return the serviceGroupNameLabel
     */
    public String getServiceGroupNameLabel() {
        return serviceGroupNameLabel;
    }

    /**
     * Sets the serviceGroupDescription.
     * 
     * @param serviceGroupDescription
     *            the serviceGroupDescription to set
     */
    public void setServiceGroupDescription(String serviceGroupDescription) {
        this.serviceGroupDescription = serviceGroupDescription;
    }

    /**
     * Returns the serviceGroupDescription.
     * 
     * @return the serviceGroupDescription
     */
    public String getServiceGroupDescription() {
        return serviceGroupDescription;
    }

}
