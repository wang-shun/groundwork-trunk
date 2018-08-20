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

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HitListResult {

    private Boolean success = true;
    private String message;
    private HitListCounts counts = new HitListCounts();

    private List<DashboardHost> hostsDownUnacknowledged = new ArrayList<>();
    private List<DashboardHost> hostsDownAcknowledged = new ArrayList<>();
    private List<DashboardHost> hostsUnreachable = new ArrayList<>();
    private List<DashboardHost> hostsScheduledDown = new ArrayList<>();

    private List<DashboardService> servicesCriticalUnacknowledged = new ArrayList<>();
    private List<DashboardService> servicesWarningUnacknowledged = new ArrayList<>();
    private List<DashboardService> servicesCriticalAcknowledged = new ArrayList<>();
    private List<DashboardService> servicesWarningAcknowledged = new ArrayList<>();

    private List<DashboardService> servicesCriticalDown = new ArrayList<>();
    private List<DashboardService> servicesWarningDown = new ArrayList<>();

    private HitListPrefs prefs;

    public HitListResult() {}

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HitListCounts getCounts() {
        return counts;
    }

    public void setCounts(HitListCounts counts) {
        this.counts = counts;
    }

    public List<DashboardHost> getHostsDownUnacknowledged() {
        return hostsDownUnacknowledged;
    }

    public void setHostsDownUnacknowledged(List<DashboardHost> hostsDownUnacknowledged) {
        this.hostsDownUnacknowledged = hostsDownUnacknowledged;
    }

    public List<DashboardHost> getHostsDownAcknowledged() {
        return hostsDownAcknowledged;
    }

    public void setHostsDownAcknowledged(List<DashboardHost> hostsDownAcknowledged) {
        this.hostsDownAcknowledged = hostsDownAcknowledged;
    }

    public List<DashboardHost> getHostsUnreachable() {
        return hostsUnreachable;
    }

    public void setHostsUnreachable(List<DashboardHost> hostsUnreachable) {
        this.hostsUnreachable = hostsUnreachable;
    }

    public List<DashboardHost> getHostsScheduledDown() {
        return hostsScheduledDown;
    }

    public void setHostsScheduledDown(List<DashboardHost> hostsScheduledDown) {
        this.hostsScheduledDown = hostsScheduledDown;
    }

    public void addHostDownUnacknowledged(DashboardHost host) {
        hostsDownUnacknowledged.add(host);
    }

    public void addHostDownAcknowledged(DashboardHost host) {
        hostsDownAcknowledged.add(host);
    }

    public void addHostUnreachable(DashboardHost host) {
        hostsUnreachable.add(host);
    }

    public void addHostScheduledDown(DashboardHost host) {
        hostsScheduledDown.add(host);
    }


    public List<DashboardService> getServicesCriticalUnacknowledged() {
        return servicesCriticalUnacknowledged;
    }

    public void setServicesCriticalUnacknowledged(List<DashboardService> servicesCriticalUnacknowledged) {
        this.servicesCriticalUnacknowledged = servicesCriticalUnacknowledged;
    }

    public List<DashboardService> getServicesWarningUnacknowledged() {
        return servicesWarningUnacknowledged;
    }

    public void setServicesWarningUnacknowledged(List<DashboardService> servicesWarningUnacknowledged) {
        this.servicesWarningUnacknowledged = servicesWarningUnacknowledged;
    }

    public List<DashboardService> getServicesCriticalAcknowledged() {
        return servicesCriticalAcknowledged;
    }

    public void setServicesCriticalAcknowledged(List<DashboardService> servicesCriticalAcknowledged) {
        this.servicesCriticalAcknowledged = servicesCriticalAcknowledged;
    }

    public List<DashboardService> getServicesWarningAcknowledged() {
        return servicesWarningAcknowledged;
    }

    public void setServicesWarningAcknowledged(List<DashboardService> servicesWarningAcknowledged) {
        this.servicesWarningAcknowledged = servicesWarningAcknowledged;
    }

    public List<DashboardService> getServicesCriticalDown() {
        return servicesCriticalDown;
    }

    public void setServicesCriticalDown(List<DashboardService> servicesCriticalDown) {
        this.servicesCriticalDown = servicesCriticalDown;
    }

    public List<DashboardService> getServicesWarningDown() {
        return servicesWarningDown;
    }

    public void setServicesWarningDown(List<DashboardService> servicesWarningDown) {
        this.servicesWarningDown = servicesWarningDown;
    }

    public void addServiceCriticalUnacknowledged(DashboardService service) {
        servicesCriticalUnacknowledged.add(service);
    }

    public void addServiceWarningUnacknowledged(DashboardService service) {
        servicesWarningUnacknowledged.add(service);
    }

    public void addServiceCriticalAcknowledged(DashboardService service) {
        servicesCriticalAcknowledged.add(service);
    }

    public void addServiceWarningAcknowledged(DashboardService service) {
        servicesWarningAcknowledged.add(service);
    }

    public void addServiceCriticalDown(DashboardService service) {
        servicesCriticalDown.add(service);
    }

    public void addServiceWarningDown(DashboardService service) {
        servicesWarningDown.add(service);
    }

    public void updateCounts() {
        counts.setHostsDownAcknowledged(this.hostsDownAcknowledged.size());
        counts.setHostsDownUnacknowledged(this.hostsDownUnacknowledged.size());
        counts.setHostsUnreachable(this.hostsUnreachable.size());
        counts.setHostsScheduledDown(this.hostsScheduledDown.size());
        counts.setServicesCriticalUnacknowledged(this.servicesCriticalUnacknowledged.size());
        counts.setServicesWarningUnacknowledged(this.servicesWarningUnacknowledged.size());
        counts.setServicesCriticalAcknowledged(this.servicesCriticalAcknowledged.size());
        counts.setServicesWarningAcknowledged(this.servicesWarningAcknowledged.size());
        counts.setServicesCriticalDown(this.servicesCriticalDown.size());
        counts.setServicesWarningDown(this.servicesWarningDown.size());

    }

    public HitListPrefs getPrefs() {
        return prefs;
    }

    public void setPrefs(HitListPrefs prefs) {
        this.prefs = prefs;
    }
}
