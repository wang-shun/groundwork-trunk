/*
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.cloudhub.connectors.icinga2;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.Icinga2Connection;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.connectors.MonitorConnector;
import org.groundwork.cloudhub.connectors.MonitorConnectorListener;
import org.groundwork.cloudhub.connectors.icinga2.client.BaseIcinga2Client;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2AuthClient;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2EventsClient;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2EventsClientListener;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2InventoryClient;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.MonitorInventory;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Icinga2Connector
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Service(Icinga2Connector.NAME)
@Scope("prototype")
public class Icinga2Connector implements MonitorConnector {

    public final static String NAME = "Icinga2Connector";

    private static Logger log = Logger.getLogger(Icinga2Connector.class);

    private final static String CLOUDHUB_EVENTS_QUEUE = "CloudHub";

    private ConnectionState connectionState = ConnectionState.NASCENT;
    private Icinga2Connection connection;
    private MonitorConnectorListener listener;
    private Icinga2EventsClient eventsClient;
    private Icinga2MonitorInventory monitorInventory;

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        // save connection and preserve monitor client listener
        connect(monitorConnection, listener);
    }

    @Override
    public void connect(MonitorConnection monitorConnection, MonitorConnectorListener monitorConnectorListener) throws ConnectorException {
        // save connection and monitor client listener
        connection = (Icinga2Connection)monitorConnection;
        listener = monitorConnectorListener;
        // connect if not already connected
        if ((connectionState != ConnectionState.CONNECTED) && (connectionState != ConnectionState.SEMICONNECTED)) {
            try {
                // assert connection
                assertConnection((Icinga2Connection) monitorConnection);
                // connected
                connectionState = ConnectionState.SEMICONNECTED;
            } catch (ConnectorException ce) {
                connection = null;
                throw ce;
            } catch (Exception e) {
                connection = null;
                throw new ConnectorException("Cannot connect to or authenticate against Icinga2 service: " + e, e);
            }
        }
    }

    @Override
    public void suspend() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            // stop events client
            eventsClient.stop();
            eventsClient = null;
            connectionState = ConnectionState.SEMICONNECTED;
        }
    }

    @Override
    public void unsuspend() throws ConnectorException {
        if (connectionState == ConnectionState.SEMICONNECTED) {
            try {
                eventsClient = new Icinga2EventsClient(connection, CLOUDHUB_EVENTS_QUEUE, new Icinga2EventsClientListener() {
                    @Override
                    public void eventReceived(JsonNode jsonEvent) {
                        if ((listener != null) && (monitorInventory != null)) {
                            Collection<Object> dtoEventInventory = monitorInventory.buildEventInventory(jsonEvent,
                                    connection.isMetricsGraphed());
                            if ((dtoEventInventory != null) && !dtoEventInventory.isEmpty()) {
                                listener.eventReceived(dtoEventInventory);
                            }
                        }
                    }

                    @Override
                    public void closed() {
                    }
                });
                eventsClient.start();
                // connected
                connectionState = ConnectionState.CONNECTED;
            } catch (Exception e) {
                eventsClient = null;
                throw new ConnectorException("Cannot connect to Icinga2 service: "+e, e);
            }
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        // suspend to disconnect
        suspend();
        // disconnect
        if (connectionState == ConnectionState.SEMICONNECTED) {
            // disconnected
            connectionState = ConnectionState.DISCONNECTED;
            connection = null;
            listener = null;
        }
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void testConnection(MonitorConnection monitorConnection) throws ConnectorException {
        try {
            // assert connection
            assertConnection((Icinga2Connection) monitorConnection);
        } finally {
            // cleanup thread client resources
            BaseIcinga2Client.shutdown();
        }
    }

    @Override
    public MonitorInventory gatherMonitorInventory(CloudhubAgentInfo agentInfo,
                                                   ValidateHost hostValidator) throws ConnectorException {
        // validate connected
        if ((connectionState != ConnectionState.SEMICONNECTED) && (connectionState != ConnectionState.CONNECTED)) {
            throw new ConnectorException("Not connected");
        }
        // gather monitor inventory
        Icinga2MonitorInventoryBrowser monitorInventoryBrowser = new Icinga2MonitorInventoryBrowser(connection);
        return monitorInventory = (Icinga2MonitorInventory)monitorInventoryBrowser.gatherMonitorInventory(agentInfo,
                hostValidator);
    }

    @Override
    public void releaseThreadResources() {
        // cleanup thread client resources
        BaseIcinga2Client.shutdown();
    }

    /**
     * Assert Icinga2 monitor connection authenticating and testing API version.
     *
     * @param monitorConnection connection to test
     * @throws ConnectorException
     */
    private void assertConnection(Icinga2Connection monitorConnection) throws ConnectorException {
        // test API authentication
        Icinga2AuthClient authClient = new Icinga2AuthClient(monitorConnection);
        if (!authClient.testAPIAuthentication()) {
            throw new ConnectorException("Cannot connect to or authenticate against Icinga2 service");
        }
        Icinga2InventoryClient inventoryClient = new Icinga2InventoryClient(monitorConnection);
        String [] version = new String[1];
        if (!inventoryClient.checkStatus(version)) {
            throw new ConnectorException("Cannot connect to service, (API version: " + version[0] + ")");
        }
    }
}
