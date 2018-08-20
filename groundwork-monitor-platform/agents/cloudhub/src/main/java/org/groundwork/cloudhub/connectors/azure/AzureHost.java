package org.groundwork.cloudhub.connectors.azure;

import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class AzureHost extends BaseHost implements MetricProvider {

    private String serviceType;

    public AzureHost(String hostName, String serviceType) {
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
