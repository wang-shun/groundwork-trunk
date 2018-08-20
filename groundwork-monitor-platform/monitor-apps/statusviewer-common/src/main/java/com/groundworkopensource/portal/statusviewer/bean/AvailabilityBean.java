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

import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;

/**
 * This is simple bean which contains the data to be displayed on UI sfor
 * serviceStatus/host availability portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public class AvailabilityBean {
    /**
     * Path of the icon to be displayed for the monitor status for
     * serviceStatus.
     */
    private String iconPath;

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
     * SimpleHost object
     */
    private SimpleHost simpleHost;

    /**
     * Host object
     */
    private Host host;

    /**
     * @return host
     */
    public Host getHost() {
        return host;
    }

    /**
     * @return simpleHost
     */
    public SimpleHost getSimpleHost() {
        return simpleHost;
    }

    /**
     * @param simpleHost
     */
    public void setSimpleHost(SimpleHost simpleHost) {
        this.simpleHost = simpleHost;
    }

    /**
     * @param host
     */
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * Service object
     */
    private ServiceStatus serviceStatus;

    /**
     * SimpleServiceStatus object
     */
    private SimpleServiceStatus simpleServiceStatus;

    /**
     * @return simpleServiceStatus
     */
    public SimpleServiceStatus getSimpleServiceStatus() {
        return simpleServiceStatus;
    }

    /**
     * @param simpleServiceStatus
     */
    public void setSimpleServiceStatus(SimpleServiceStatus simpleServiceStatus) {
        this.simpleServiceStatus = simpleServiceStatus;
    }

    /**
     * @return serviceStatus
     */
    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    /**
     * @param serviceStatus
     */
    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    /**
     * Flag indicating if the entity is a serviceStatus or host.
     */
    private boolean isService;

    /**
     * @return isService
     */
    public boolean isService() {
        return isService;
    }

    /**
     * @param isService
     */
    public void setService(boolean isService) {
        this.isService = isService;
    }

}
