/*
 * Copyright (C) 2010 GroundWork Open Source, Inc. (GroundWork) All rights
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

@JsonIgnoreProperties(ignoreUnknown = true)
public class HitListCounts {

    private Integer hostsDownUnacknowledged = 0;
    private Integer hostsDownAcknowledged = 0;
    private Integer hostsUnreachable = 0;
    private Integer hostsScheduledDown = 0;

    private Integer servicesCriticalUnacknowledged = 0;
    private Integer servicesWarningUnacknowledged = 0;
    private Integer servicesCriticalAcknowledged = 0;
    private Integer servicesWarningAcknowledged = 0;

    private Integer servicesCriticalDown = 0;
    private Integer servicesWarningDown = 0;

    public Integer getHostsDownUnacknowledged() {
        return hostsDownUnacknowledged;
    }

    public void setHostsDownUnacknowledged(Integer hostsDownUnacknowledged) {
        this.hostsDownUnacknowledged = hostsDownUnacknowledged;
    }

    public Integer getHostsDownAcknowledged() {
        return hostsDownAcknowledged;
    }

    public void setHostsDownAcknowledged(Integer hostsDownAcknowledged) {
        this.hostsDownAcknowledged = hostsDownAcknowledged;
    }

    public Integer getHostsUnreachable() {
        return hostsUnreachable;
    }

    public void setHostsUnreachable(Integer hostsUnreachable) {
        this.hostsUnreachable = hostsUnreachable;
    }

    public Integer getHostsScheduledDown() {
        return hostsScheduledDown;
    }

    public void setHostsScheduledDown(Integer hostsScheduledDown) {
        this.hostsScheduledDown = hostsScheduledDown;
    }

    public Integer getServicesCriticalUnacknowledged() {
        return servicesCriticalUnacknowledged;
    }

    public void setServicesCriticalUnacknowledged(Integer servicesCriticalUnacknowledged) {
        this.servicesCriticalUnacknowledged = servicesCriticalUnacknowledged;
    }

    public Integer getServicesWarningUnacknowledged() {
        return servicesWarningUnacknowledged;
    }

    public void setServicesWarningUnacknowledged(Integer servicesWarningUnacknowledged) {
        this.servicesWarningUnacknowledged = servicesWarningUnacknowledged;
    }

    public Integer getServicesCriticalAcknowledged() {
        return servicesCriticalAcknowledged;
    }

    public void setServicesCriticalAcknowledged(Integer servicesCriticalAcknowledged) {
        this.servicesCriticalAcknowledged = servicesCriticalAcknowledged;
    }

    public Integer getServicesWarningAcknowledged() {
        return servicesWarningAcknowledged;
    }

    public void setServicesWarningAcknowledged(Integer servicesWarningAcknowledged) {
        this.servicesWarningAcknowledged = servicesWarningAcknowledged;
    }

    public Integer getServicesCriticalDown() {
        return servicesCriticalDown;
    }

    public void setServicesCriticalDown(Integer servicesCriticalDown) {
        this.servicesCriticalDown = servicesCriticalDown;
    }

    public Integer getServicesWarningDown() {
        return servicesWarningDown;
    }

    public void setServicesWarningDown(Integer servicesWarningDown) {
        this.servicesWarningDown = servicesWarningDown;
    }
}
