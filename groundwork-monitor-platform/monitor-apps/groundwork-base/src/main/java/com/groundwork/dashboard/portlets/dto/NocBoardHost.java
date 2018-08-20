package com.groundwork.dashboard.portlets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NocBoardHost extends DashboardHost {

    private List<NocBoardService> services = new ArrayList<>();

    public NocBoardHost(String hostName, String status, Date lastCheckTime) {
        super(hostName, status, lastCheckTime);
    }

    public List<NocBoardService> getServices() {
        return services;
    }

    public void setServices(List<NocBoardService> services) {
        this.services = services;
    }

    public void addService(NocBoardService service) {
        services.add(service);
    }

}
