package org.groundwork.cloudhub.connectors.docker.client;

import org.groundwork.cloudhub.connectors.base.MetricFaultInfo;

import java.util.List;
import java.util.Set;

public interface IMetricsClient {
    List<DockerMetricInfo> retrieveDockerEngineMetrics(String engine, Set<String> queries);

    List<DockerMetricInfo> retrieveContainerMetrics(String containerName, String containerId, Set<String> queries);

    List<MetricFaultInfo> getMetricFaults();

    void clearMetricFaults();
}
