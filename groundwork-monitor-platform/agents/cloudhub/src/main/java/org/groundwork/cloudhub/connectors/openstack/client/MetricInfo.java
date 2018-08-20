package org.groundwork.cloudhub.connectors.openstack.client;

public class MetricInfo {

    public final String meter;
    public final String resource;
    public final String timestamp;
    public final String metric;
    public final String unit;
    public final String query;

    public MetricInfo(String query, String meter, String resource, String timestamp, String metric, String unit) {
        this.query = query;
        this.meter = meter;
        this.resource = resource;
        this.timestamp = timestamp;
        this.metric = metric;
        this.unit = unit;
    }

    public String toString() {
        return String.format("%s[%s]: [%s %s] - %s %s", query, meter, metric, unit, resource, timestamp);
    }

    public String toString(String name) {
        return String.format("%s[%s]: [%s %s] - %s %s", query, meter, metric, unit, name, timestamp);
    }

}
