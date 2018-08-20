package org.groundwork.cloudhub.connectors.nedi;

import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class NediHost extends BaseHost implements MetricProvider {

    public NediHost(String hostName) {
        super(hostName);
    }

}
