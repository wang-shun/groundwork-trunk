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

/**
 * This class holds the state transition data for host/services.
 * 
 * @author shivangi_walvekar
 * 
 */
public class StateTransitionBean {
    /**
     * Host name property
     */
    private String hostName;

    /**
     * Service name corresponds to service description property
     */
    private String serviceName;

    /**
     * Count of total number of services for a host.
     */
    private int servicesCount;

    /**
     * Monitor status
     */
    private String toState;

    /**
     * Duration the host/service was in a particular monitor state.
     */
    private Long timeInState;

    /**
     * Host name or service name
     */
    private String entityName;

    /**
     * Path of the icon to be displayed for the monitor status for service.
     */
    private String iconPath;

    /**
     * Monitor status ID
     */
    private int toStateID;

    /**
     * @return toStateID
     */
    public int getToStateID() {
        return toStateID;
    }

    /**
     * @param toStateID
     */
    public void setToStateID(int toStateID) {
        this.toStateID = toStateID;
    }

    /**
     * 
     * @return iconPath
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * @param iconPath
     */
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * @return entityName
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return timeInState
     */
    public Long getTimeInState() {
        return timeInState;
    }

    /**
     * @param timeInState
     */
    public void setTimeInState(Long timeInState) {
        this.timeInState = timeInState;
    }

    /**
     * @return toState
     */
    public String getToState() {
        return toState;
    }

    /**
     * @param toState
     */
    public void setToState(String toState) {
        this.toState = toState;
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
     * @return serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return servicesCount
     */
    public int getServicesCount() {
        return servicesCount;
    }

    /**
     * @param servicesCount
     */
    public void setServicesCount(int servicesCount) {
        this.servicesCount = servicesCount;
    }

}
