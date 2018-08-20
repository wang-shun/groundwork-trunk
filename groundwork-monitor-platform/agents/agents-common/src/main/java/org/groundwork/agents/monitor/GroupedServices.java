package org.groundwork.agents.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Services grouped by Metric Type
 * If groups is empty, delete by primaray/secondary/custom lists
 */
    public class GroupedServices {

    private Map<String,List<DeleteServiceInfo>> groups = new HashMap<>();
    private List<DeleteServiceInfo> primary = new ArrayList<>();
    private List<DeleteServiceInfo> secondary = new ArrayList<>();
    private List<DeleteServiceInfo> custom = new ArrayList<>();

    public GroupedServices(List<GroupServiceInfo> removedList) {
        for (GroupServiceInfo metric : removedList) {
            List<DeleteServiceInfo> services = groups.get(metric.getServiceType());
            if (services == null) {
                services = new ArrayList<>();
                groups.put(metric.getServiceType(), services);
            }
            DeleteServiceInfo dsi = new DeleteServiceInfo(metric.getName(), metric.getServiceType());
            services.add(dsi);
            if (metric.equals(GroupServiceInfo.MetricCategory.primary)) {
                primary.add(dsi);
            }
            else {
                secondary.add(dsi);
            }
        }
    }

    public GroupedServices(List<String> primary, List<String> secondary, List<String> custom) {
        for (String name : primary) {
            this.primary.add(new DeleteServiceInfo(name));
        }
        for (String name : secondary) {
            this.secondary.add(new DeleteServiceInfo(name));
        }
        for (String name : custom) {
            this.custom.add(new DeleteServiceInfo(name));
        }
    }

    public GroupedServices(List<DeleteServiceInfo> primary, List<DeleteServiceInfo> secondary, List<DeleteServiceInfo> custom, boolean unused) {
        this.primary.addAll(primary);
        this.secondary.addAll(secondary);
        this.custom.addAll(custom);
    }

    public Map<String, List<DeleteServiceInfo>> getGroups() {
        return groups;
    }

    public List<DeleteServiceInfo> getPrimary() {
        return primary;
    }

    public List<DeleteServiceInfo> getSecondary() {
        return secondary;
    }

    public List<DeleteServiceInfo> getCustom() {
        return custom;
    }
}
