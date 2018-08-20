package org.groundwork.cloudhub.connectors.docker;


import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class DockerVM extends BaseVM implements MetricProvider {


    private DockerVM() {}

    public DockerVM(String vmName) {
        super(vmName);
    }

}