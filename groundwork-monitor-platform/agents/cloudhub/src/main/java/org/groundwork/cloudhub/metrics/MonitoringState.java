/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.metrics;

import org.groundwork.cloudhub.monitor.ConnectorMonitorState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class MonitoringState {

    protected Map<String, BaseHost> hostMap ;
    protected List<MonitoringEvent> events;
    protected MetricCollectionState state = new BaseCollectionState();
    protected ConnectorMonitorState connectorMonitorState = new ConnectorMonitorState();

    public MonitoringState() {
        hostMap = new ConcurrentHashMap<>();
        events = new ArrayList<>();
    }

    public MonitoringState(Map<String, BaseHost> hosts) {
        this.hostMap = hosts;
    }

    public Map<String, BaseHost> hosts() {
        return hostMap;
    }

    public List<MonitoringEvent> events() { return events; }

    public MetricCollectionState getState() {
        return state;
    }

    public Map<String, String> getExceptions() {
        return state.getExceptions();
    }

    public void addException(String key, String message) {
        state.addException(key, message);
    }

    public void resetState() {
        state.clear();
        connectorMonitorState.clear();
    }

    public ConnectorMonitorState getConnectorMonitorState() {
        return connectorMonitorState;
    }

    public void setConnectorMonitorState(ConnectorMonitorState connectorMonitorState) {
        this.connectorMonitorState = connectorMonitorState;
    }
}
