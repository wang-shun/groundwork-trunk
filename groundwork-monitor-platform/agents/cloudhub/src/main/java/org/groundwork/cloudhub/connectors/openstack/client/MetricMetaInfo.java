package org.groundwork.cloudhub.connectors.openstack.client;

public class MetricMetaInfo {

    public final String meter;
    public final String resource;
    public final String source;
    public final String meterId;
    public final String type;
    public final String unit;

    public MetricMetaInfo(String meter, String resource, String source, String meterId, String type, String unit) {
        this.meter = meter;
        this.resource = resource;
        this.source = source;
        this.meterId = meterId;
        this.type = type;
        this.unit = unit;
    }

    public String toString() {
        return String.format("%s: %s %s", meter, type, unit);
    }
}
