package org.groundwork.cloudhub.connectors.base;

import org.groundwork.cloudhub.configuration.MonitorConnection;

import java.util.List;
import java.util.Set;

public interface DiscoveryConnector {

    List<String> listHosts(MonitorConnection connection);
    List<String> listClusters(MonitorConnection connection);
    Set<String> listServices(MonitorConnection connection);
}
