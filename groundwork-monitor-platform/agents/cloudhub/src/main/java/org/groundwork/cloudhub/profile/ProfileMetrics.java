package org.groundwork.cloudhub.profile;

import org.groundwork.rs.dto.profiles.Metric;

import java.util.ArrayList;
import java.util.List;

public class ProfileMetrics {

    private List<Metric> primary;
    private List<Metric> secondary;
    private List<Metric> custom;

    public ProfileMetrics(List<Metric> primary, List<Metric> secondary) {
        this.primary = primary;
        this.secondary = secondary;
        this.custom = new ArrayList<>();
    }

    public ProfileMetrics(List<Metric> primary, List<Metric> secondary, List<Metric> custom) {
        this.primary = primary;
        this.secondary = secondary;
        this.custom = custom;
    }

    public List<Metric> getPrimary() {
        return primary;
    }

    public List<Metric> getSecondary() {
        return secondary;
    }

    public List<Metric> getCustom() {
        return custom;
    }
}
