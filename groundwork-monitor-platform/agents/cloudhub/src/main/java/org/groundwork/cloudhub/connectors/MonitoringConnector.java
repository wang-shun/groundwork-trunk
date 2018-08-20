/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.connectors;

import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.rs.dto.profiles.Metric;

import java.util.List;

public interface MonitoringConnector extends BaseMonitorConnector {

    /**
     * Run a cycle of collecting metrics for  a connector, creating a snapshot of monitored
     * state for all hosts, VMs, services, and metrics for this connector
     *
     * @param priorState
     * @param hostQueries
     * @param vmQueries
     * @return a snapshot of MonitoringState of all monitored objects in this data center
     * @throws ConnectorException
     */
    MonitoringState collectMetrics(MonitoringState priorState, List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException;

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

    /**
     * Query for information about a particular connector-specific piece of information (tag)
     *
     * @param tag can be any connector-specific tag
     * @return
     */
    String queryConnectorInfo(String tag);

    /**
     * Retrieve a list of unique custom metrics from this connector
     *
     * @return
     * @throws ConnectorException
     */
    List<Metric> retrieveCustomMetrics() throws ConnectorException;

    /**
     * List metric names for a given service type and configuration
     *
     * @param serviceType
     * @param configuration
     * @return
     */
    List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration);
}
