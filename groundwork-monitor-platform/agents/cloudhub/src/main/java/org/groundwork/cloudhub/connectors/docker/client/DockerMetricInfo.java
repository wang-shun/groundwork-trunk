package org.groundwork.cloudhub.connectors.docker.client;

public class DockerMetricInfo {

    public final String meter;
    public final String resource;
    public final String timestamp;
    public final long metric;
    public final String unit;
    public final String query;

    public DockerMetricInfo(String meter, String resource, String timestamp, long metric, String unit, String query) {
        this.meter = meter;
        this.resource = resource;
        this.timestamp = timestamp;
        this.metric = metric;
        this.unit = unit;
        this.query = query;
    }

    public String metricToString() {
        return Long.toString(this.metric);
    }

    public String toString() {
        return String.format("%s: %s - %s %s (%s)", resource, timestamp, metric, unit, query);
    }
}
