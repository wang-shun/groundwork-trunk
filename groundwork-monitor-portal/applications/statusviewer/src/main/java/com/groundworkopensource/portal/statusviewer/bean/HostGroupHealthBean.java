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

import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;

/**
 * Bean for Host Group Health portlet.
 * 
 * @author swapnil_gujrathi
 */
public class HostGroupHealthBean implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4793787186069655051L;
    /**
     * Host Group Name.
     */
    private String hostGroupName;
    /**
     * Host Group Name.
     */
    private String hostGroupNameLabel;
    /**
     * Host Group Alias.
     */
    private String hostGroupAlias;
    /**
     * Host Group Alias.
     */
    private String hostGroupAliasLabel;
    /**
     * Host Availability.
     */
    private String hostAvailability;
    /**
     * Service Availability.
     */
    private String serviceAvailability;
    /**
     * Host group Status.
     */
    private NetworkObjectStatusEnum status;

    /**
     * Description for the Host Group.
     */
    private String hostGroupDescription;

    /**
     * Returns the hostGroupName.
     * 
     * @return the hostGroupName
     */
    public String getHostGroupName() {
        return hostGroupName;
    }

    /**
     * Sets the hostGroupName.
     * 
     * @param hostGroupName
     *            the hostGroupName to set
     */
    public void setHostGroupName(String hostGroupName) {
        this.hostGroupName = hostGroupName;
    }

    /**
     * Returns the hostGroupAlias.
     * 
     * @return the hostGroupAlias
     */
    public String getHostGroupAlias() {
        return hostGroupAlias;
    }

    /**
     * Sets the hostGroupAlias.
     * 
     * @param hostGroupAlias
     *            the hostGroupAlias to set
     */
    public void setHostGroupAlias(String hostGroupAlias) {
        this.hostGroupAlias = hostGroupAlias;
    }

    /**
     * Sets the hostAvailability.
     * 
     * @param hostAvailability
     *            the hostAvailability to set
     */
    public void setHostAvailability(String hostAvailability) {
        this.hostAvailability = hostAvailability;
    }

    /**
     * Returns the hostAvailability.
     * 
     * @return the hostAvailability
     */
    public String getHostAvailability() {
        return hostAvailability;
    }

    /**
     * Sets the serviceAvailability.
     * 
     * @param serviceAvailability
     *            the serviceAvailability to set
     */
    public void setServiceAvailability(String serviceAvailability) {
        this.serviceAvailability = serviceAvailability;
    }

    /**
     * Returns the serviceAvailability.
     * 
     * @return the serviceAvailability
     */
    public String getServiceAvailability() {
        return serviceAvailability;
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
     * Returns the status of current Host Group.
     * 
     * @return the status
     */
    public NetworkObjectStatusEnum getStatus() {
        return status;
    }

    /**
     * Sets the hostGroupNameLabel.
     * 
     * @param hostGroupNameLabel
     *            the hostGroupNameLabel to set
     */
    public void setHostGroupNameLabel(String hostGroupNameLabel) {
        this.hostGroupNameLabel = hostGroupNameLabel;
    }

    /**
     * Returns the hostGroupNameLabel.
     * 
     * @return the hostGroupNameLabel
     */
    public String getHostGroupNameLabel() {
        return hostGroupNameLabel;
    }

    /**
     * Sets the hostGroupAliasLabel.
     * 
     * @param hostGroupAliasLabel
     *            the hostGroupAliasLabel to set
     */
    public void setHostGroupAliasLabel(String hostGroupAliasLabel) {
        this.hostGroupAliasLabel = hostGroupAliasLabel;
    }

    /**
     * Returns the hostGroupAliasLabel.
     * 
     * @return the hostGroupAliasLabel
     */
    public String getHostGroupAliasLabel() {
        return hostGroupAliasLabel;
    }

    /**
     * Sets the hostGroupDescription.
     * 
     * @param hostGroupDescription
     *            the hostGroupDescription to set
     */
    public void setHostGroupDescription(String hostGroupDescription) {
        this.hostGroupDescription = hostGroupDescription;
    }

    /**
     * Returns the hostGroupDescription.
     * 
     * @return the hostGroupDescription
     */
    public String getHostGroupDescription() {
        return hostGroupDescription;
    }

}
