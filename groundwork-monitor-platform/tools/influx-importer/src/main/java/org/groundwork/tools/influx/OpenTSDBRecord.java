package org.groundwork.tools.influx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenTSDBRecord {

    private String metric;
    private List<String> aggregateTags = new ArrayList<>();
    private Map<String,Double> dps = new HashMap<>();
    private Map<String,String> tags = new HashMap<>();

    public OpenTSDBRecord() {
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public List<String> getAggregateTags() {
        return aggregateTags;
    }

    public void setAggregateTags(List<String> aggregateTags) {
        this.aggregateTags = aggregateTags;
    }

    public Map<String, Double> getDps() {
        return dps;
    }

    public void setDps(Map<String, Double> dps) {
        this.dps = dps;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
