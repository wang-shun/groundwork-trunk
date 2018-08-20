package org.groundwork.cloudhub.connectors.nsx;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.NSXConnection;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(NSXConnector.NAME)
@Scope("prototype")
public class NSXConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    private static Logger log = Logger.getLogger(NSXConnector.class);

    public static final String NAME = "NSXConnector";
    private ConnectionState connectionState = ConnectionState.NASCENT;
    private NSXConnection connection = null;

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        throw new ConnectorException("Not implemented");
    }

    @Override
    public void disconnect() throws ConnectorException {
        connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState monitoringState,
                                          List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException {
        return monitoringState;
    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        throw new ConnectorException("Not implemented");
    }

    @Override
    public void openConnection(MonitorConnection monitorConnection) throws ConnectorException {
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }
    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

}
