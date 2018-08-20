package com.groundwork.dashboard.portlets.dto;

import com.groundwork.dashboard.configuration.DashboardConfiguration;

import java.util.LinkedList;
import java.util.List;

public class DashboardConfigLookup extends DashboardConfiguration {

    private List<String> serviceGroups = new LinkedList<>();
    private List<String> hostGroups = new LinkedList<>();
    private Integer maxAvailabilityWindow = 49;

    public DashboardConfigLookup(DashboardConfiguration c) {
        setName(c.getName());
        setTitle(c.getTitle());
        setServiceGroup(c.getServiceGroup());
        setHostGroup(c.getHostGroup());
        setAutoExpand(c.getAutoExpand());
        setDowntimeHours(c.getDowntimeHours());
        setPercentageSLA(c.getPercentageSLA());
        setRows(c.getRows());
        setRefreshSeconds(c.getRefreshSeconds());
        setAckFilters(c.getAckFilters());
        setDownTimeFilters(c.getDownTimeFilters());
        setStates(c.getStates());
        setColumns(c.getColumns());
        setAvailabilityHours(c.getAvailabilityHours());
    }
    public List<String> getServiceGroups() {
        return serviceGroups;
    }

    public void setServiceGroups(List<String> serviceGroups) {
        this.serviceGroups = serviceGroups;
    }

    public void addServiceGroup(String serviceGroup) {
        serviceGroups.add(serviceGroup);
    }

    public List<String> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<String> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public void addHostGroup(String hostGroup) {
        hostGroups.add(hostGroup);
    }

    public Integer getMaxAvailabilityWindow() {
        return maxAvailabilityWindow;
    }

    public void setMaxAvailabilityWindow(Integer maxAvailabilityWindow) {
        this.maxAvailabilityWindow = maxAvailabilityWindow;
    }
}
