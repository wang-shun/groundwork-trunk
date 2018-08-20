package org.groundwork.cloudhub.connectors.cisco;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.CiscoConnection;
import org.groundwork.cloudhub.configuration.MonitorConnection;
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

@Service(CiscoConnector.NAME)
@Scope("prototype")
public class CiscoConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    public static final String NAME = "CiscoConnector";
    private static Logger log = Logger.getLogger(CiscoConnector.class);
    private ConnectionState connectionState = ConnectionState.NASCENT;
    private CiscoConnection connection = null;

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
