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

package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;

/**
 * BaseMonitorConnector
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface BaseMonitorConnector {

    /**
     * Connect to a monitor service using configured credentials in the configuration
     *
     * @param connection connection configuration
     * @throws org.groundwork.cloudhub.exceptions.ConnectorException
     */
    void connect(MonitorConnection connection) throws ConnectorException;

    /**
     * Disconnects the connector from the monitor service, closing all resources and connections
     *
     * @throws ConnectorException
     */
    void disconnect() throws ConnectorException;

    /**
     * Check the connection state of this connector to the monitor service
     *
     * @return the current state of the monitoring server
     */
    ConnectionState getConnectionState();

    /**
     * Test connecting to a monitor service using configured credentials in the configuration
     *
     * @param connection connection configuration
     * @throws ConnectorException
     */
    void testConnection(MonitorConnection connection) throws ConnectorException;
}
