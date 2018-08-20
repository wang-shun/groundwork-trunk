/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.HubProfile;
import org.groundwork.rs.dto.profiles.NetHubProfile;

public interface ManagementConnector {

    /**
     * Gather inventory for the Management Server's Data Center for all managed objects
     *
     * @return a DataCenterInventory of all managed objects in this data center
     */
    DataCenterInventory gatherInventory()
            throws ConnectorException;

    /**
     * Retrieves a monitoring profile from management connector.
     *
     * @return profile or null if not supported
     * @throws CloudHubException
     */
    HubProfile readProfile() throws CloudHubException;

    /**
     * Retrieves a cloud monitoring profile from management connector.
     *
     * @return cloud profile or null if not supported
     * @throws CloudHubException
     */
    CloudHubProfile readCloudProfile() throws CloudHubException;

    /**
     * Retrieves a network monitoring profile from management connector.
     *
     * @return network profile or null if not supported
     * @throws CloudHubException
     */
    NetHubProfile readNetworkProfile() throws CloudHubException;

    /**
     * Retrieves a container monitoring profile from management connector.
     *
     * @return container profile or null if not supported
     * @throws CloudHubException
     */
    ContainerProfile readContainerProfile() throws CloudHubException;

    /**
     * Connect to a virtual management server using configured credentials in the configuration
     *
     * @param connection
     * @throws ConnectorException
     */
    void openConnection(MonitorConnection connection) throws ConnectorException;

    /**
     * Close the connection to the management server
     *
     */
    void closeConnection() throws ConnectorException;

    /**
     * Check the connection state of this connector to the virtual monitoring server
     *
     * @return the current state of the monitoring server
     */
    ConnectionState getConnectionState();

    /**
     * Gets the collection mode for this connector
     *
     * @return current collection mode settings
     */
    CollectionMode getCollectionMode();

    /**
     * Set the collection mode for the this connector
     *
     * @param mode the new collection mode
     */
    void setCollectionMode(CollectionMode mode);

}
