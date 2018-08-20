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

import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.MonitorInventory;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;

/**
 * MonitorConnector
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface MonitorConnector extends BaseMonitorConnector {

    /**
     * Connect to a monitor service using configured credentials in the configuration.
     *
     * @param monitorConnection connection configuration
     * @param monitorConnectorListener connector listener
     * @throws org.groundwork.cloudhub.exceptions.ConnectorException
     */
    void connect(MonitorConnection monitorConnection, MonitorConnectorListener monitorConnectorListener) throws ConnectorException;

    /**
     * Suspend connector notifications.
     */
    void suspend() throws ConnectorException;

    /**
     * Unsuspend connector notifications.
     */
    void unsuspend() throws ConnectorException;

    /**
     * Inventory host name validation interface.
     */
    interface ValidateHost {
        /**
         * Validate inventory host name.
         *
         * @param hostName host name to validate
         * @return valid
         */
        boolean validateHost(String hostName);
    }

    /**
     * Gather monitor inventory from monitor service. Includes all monitored
     * hosts, host groups, services, and service groups.
     *
     * @param agentInfo agent info
     * @param hostValidator host name validator
     * @return monitor inventory snapshot
     * @throws ConnectorException
     */
    MonitorInventory gatherMonitorInventory(CloudhubAgentInfo agentInfo,
                                            ValidateHost hostValidator) throws ConnectorException;

    /**
     * Release thread resources allocated using this API.
     */
    void releaseThreadResources();
}
