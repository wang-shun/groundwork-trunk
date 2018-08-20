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

import org.codehaus.jackson.JsonNode;
import org.groundwork.cloudhub.configuration.Icinga2Connection;
import org.groundwork.cloudhub.connectors.MonitorConnector;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2InventoryClient;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.MonitorInventory;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;

/**
 * Icinga2MonitorInventoryBrowser
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2MonitorInventoryBrowser {

    private Icinga2Connection connection;
    private Icinga2InventoryClient inventoryClient;

    /**
     * Construct monitor inventory browser.
     *
     * @param connection connection configuration
     */
    public Icinga2MonitorInventoryBrowser(Icinga2Connection connection) {
        this.connection = connection;
        this.inventoryClient = new Icinga2InventoryClient(connection);
    }

    /**
     * Gather monitor inventory.
     *
     * @param agentInfo agent info
     * @param hostValidator host name validator
     * @return monitor inventory
     * @throws ConnectorException
     */
    public MonitorInventory gatherMonitorInventory(CloudhubAgentInfo agentInfo,
                                                   MonitorConnector.ValidateHost hostValidator) throws ConnectorException {
        // create inventory
        String monitorServer = connection.getServer();
        String appType = agentInfo.getApplicationType();
        String agentId = agentInfo.getAgentId();
        Icinga2MonitorInventory monitorInventory = new Icinga2MonitorInventory(monitorServer, appType, agentId);
        try {
            // add inventory hosts
            for (JsonNode jsonHost : inventoryClient.getHosts()) {
                monitorInventory.addHost(jsonHost, hostValidator);
            }
            // add inventory host groups
            for (JsonNode jsonHostGroup : inventoryClient.getHostGroups()) {
                monitorInventory.addHostGroup(jsonHostGroup);
            }
            // add inventory services
            for (JsonNode jsonService : inventoryClient.getServices()) {
                monitorInventory.addService(jsonService);
            }
            // add inventory service groups
            for (JsonNode jsonServiceGroup : inventoryClient.getServiceGroups()) {
                monitorInventory.addServiceGroup(jsonServiceGroup);
            }
            // add inventory comments
            for (JsonNode jsonComment : inventoryClient.getComments()) {
                monitorInventory.addComment(jsonComment);
            }
        } catch (Exception e) {
            throw new ConnectorException("Icinga2 inventory browser gathering exception: "+e, e);
        }
        return monitorInventory;
    }
}
