package org.groundwork.cloudhub.connectors.cloudera;

import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class ClouderaHost extends BaseHost implements MetricProvider {

    private String serviceType;

    public ClouderaHost(String hostName, String serviceType) {
        super(hostName);
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
