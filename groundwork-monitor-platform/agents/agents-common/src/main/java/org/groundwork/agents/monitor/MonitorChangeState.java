package org.groundwork.agents.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtaylor on 6/8/15.
 */
public class MonitorChangeState {

    private GroupedServices groupedServices;

    private String userName;
    private List<String> views;
    private List<String> groupViews;
    // 7.1.1 alternative method for deleting metrics, by source(Metric)Type
    // necessary for metrics with variable names (new EBS requirement to add volume name to metric)
    // also can leverage this to delete custom metrics
    private List<String> metricsViews;
    private String prefix;
    private String connectorHost;

    public MonitorChangeState(GroupedServices groups, String userName) {
        this.userName = userName;
        groupedServices = groups;
    }

    public MonitorChangeState(List<String> primaryMetrics, List<String> secondaryMetrics, List<String> customMetrics, String userName) {
        this.userName = userName;
        groupedServices = new GroupedServices(primaryMetrics, secondaryMetrics, customMetrics);
    }

    public MonitorChangeState(String userName, List<String> views, List<String> groupViews, List<String> primaryMetrics) {
        this.userName = userName;
        this.views = views;
        this.groupViews = groupViews;
        groupedServices = new GroupedServices(primaryMetrics, new ArrayList<String>(), new ArrayList<String>());
    }

    public MonitorChangeState(String userName, List<String> views, List<String> groupViews, List<String> primaryMetrics, List<String> metricsViews) {
        this.userName = userName;
        this.views = views;
        this.groupViews = groupViews;
        groupedServices = new GroupedServices(primaryMetrics, new ArrayList<String>(), new ArrayList<String>());
        this.metricsViews = metricsViews;
    }

    public MonitorChangeState(List<DeleteServiceInfo> primaryMetrics, List<DeleteServiceInfo> secondaryMetrics, List<DeleteServiceInfo> customMetrics, String userName, boolean unused) {
        this.userName = userName;
        groupedServices = new GroupedServices(primaryMetrics, secondaryMetrics, customMetrics, unused);
    }

    public MonitorChangeState(String connectorHost) {
        this.connectorHost = connectorHost;
    }

    public GroupedServices getGroupedServices() {
        return groupedServices;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getViews() {
        return views;
    }

    public void setViews(List<String> views) {
        this.views = views;
    }

    public List<String> getGroupViews() {
        return groupViews;
    }

    public void setGroupViews(List<String> groupViews) {
        this.groupViews = groupViews;
    }

    public List<String> getMetricsViews() {
        return metricsViews;
    }

    public void setMetricsViews(List<String> metricsViews) {
        this.metricsViews = metricsViews;
    }
    
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setGroupedServices(GroupedServices groupedServices) {
        this.groupedServices = groupedServices;
    }

    public String getConnectorHost() {
        return connectorHost;
    }

    public void setConnectorHosts(String connectorHost) {
        this.connectorHost = connectorHost;
    }
}
