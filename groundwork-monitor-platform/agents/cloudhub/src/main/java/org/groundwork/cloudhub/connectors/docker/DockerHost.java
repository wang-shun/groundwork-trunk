package org.groundwork.cloudhub.connectors.docker;

import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class DockerHost extends BaseHost implements MetricProvider {

    private DockerHost() {
    }

    public DockerHost(String hostName) {
        super(hostName);
    }

}