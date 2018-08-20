package org.groundwork.agents.monitor;

/**
 * Created by dtaylor on 7/5/16.
 */
public class GroupServiceInfo {

    public enum MetricCategory {
        primary,
        secondary,
        custom
    };

    private String name;
    private String serviceType;
    private MetricCategory category;

    public GroupServiceInfo(String serviceName) {
        this.name = serviceName;
    }

    public GroupServiceInfo(String serviceName, String serviceType, MetricCategory category) {
        this.name = serviceName;
        this.serviceType = serviceType;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getServiceType() {
        return serviceType;
    }

    public MetricCategory getCategory() {
        return category;
    }
}
