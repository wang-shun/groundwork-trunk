package com.groundwork.dashboard.portlets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.groundwork.dashboard.configuration.DashboardConfiguration;
import java.util.Map;
import java.util.HashMap;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NocBoardResult {

    // error handling
    private Boolean success = true;
    private String message;
    private String serviceGroup;
    private String hostGroup;

    private String username;

    // top statistics
    private Map<String, Integer> hostStatusCounts = new HashMap<>();

    private Map<String, Integer> serviceStatusCounts = new HashMap<>();

    private Boolean slaMet = false;

    private Boolean autoExpand = false;

    private Map<String, NocBoardHost> hosts = new HashMap();

    private DashboardConfiguration prefs;

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


    public Map<String, Integer> getHostStatusCounts() {
        return hostStatusCounts;
    }

    public void setHostStatusCounts(Map<String, Integer> hostStatusCounts) {
        this.hostStatusCounts = hostStatusCounts;
    }

    public Map<String, Integer> getServiceStatusCounts() {
        return serviceStatusCounts;
    }

    public void setServiceStatusCounts(Map<String, Integer> serviceStatusCounts) {
        this.serviceStatusCounts = serviceStatusCounts;
    }

    public Boolean getSlaMet() {
        return slaMet;
    }

    public void setSlaMet(Boolean slaMet) {
        this.slaMet = slaMet;
    }

    private float slaPercent = 0.0f;

    public Boolean getAutoExpand() {
        return autoExpand;
    }

    public void setAutoExpand(Boolean autoExpand) {
        this.autoExpand = autoExpand;
    }

    public float getSlaPercent() {return slaPercent;}

    public void setSlaPercent(float slaValue) {this.slaPercent = slaValue;}

    public Map<String, NocBoardHost> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, NocBoardHost> hosts) {
        this.hosts = hosts;
    }

    public void addHost(NocBoardHost host) {
        this.hosts.put(host.getName(), host);
    }

    public void removeHost(NocBoardHost host) {
        this.hosts.remove(host);
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public String getHostGroup() {
        return hostGroup;
    }

    public void setHostGroup(String hostGroup) {
        this.hostGroup = hostGroup;
    }

    public DashboardConfiguration getPrefs() {
        return prefs;
    }

    public void setPrefs(DashboardConfiguration prefs) {
        this.prefs = prefs;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addService(NocBoardService service) {
        NocBoardHost host = hosts.get(service.getHostName());
        if (host != null) {
            host.addService(service);
            hosts.put(service.getHostName(), host);
        } else {
            NocBoardHost newHost = new NocBoardHost(service.getHostName(), service.getStatus(), service.getLastCheckTime());
            newHost.addService(service);
            this.hosts.put(service.getHostName(), newHost);
        }
    }

    public void incrementServiceCounts(String status) {
        setServiceStatusCounts(incrementCount(this.serviceStatusCounts, status));
    }
    public void incrementHostCounts(String status) {
        setHostStatusCounts(incrementCount(this.hostStatusCounts, status));
    }
    private static Map<String, Integer> incrementCount(Map<String, Integer> counts, String status) {
        Integer count = counts.get(status);
        if (count == null) {
            count = 0;
        }
        count++;
        counts.put(status, count);
        return counts;
    }
}
