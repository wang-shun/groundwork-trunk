/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.metrics;

/**
 * Created by dtaylor on 5/12/15.
 */
public class MonitoringEvent {

    private String hostName;
    private String service;
    private String message;
    private String status;

    public MonitoringEvent(String hostName, String service, String message, String status) {
        this(hostName, service, message);
        this.status = status;
    }

    public MonitoringEvent(String hostName, String service, String message) {
        this.hostName = hostName;
        this.service = service;
        this.message = message;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
