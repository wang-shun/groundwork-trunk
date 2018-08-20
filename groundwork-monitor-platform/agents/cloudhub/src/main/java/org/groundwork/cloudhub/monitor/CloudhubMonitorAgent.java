package org.groundwork.cloudhub.monitor;

import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.MonitorAgent;
import org.groundwork.agents.monitor.MonitorChangeState;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;

public interface CloudhubMonitorAgent extends MonitorAgent {

    /**
     * Access configuration for this agent
     *
     * @return configuration
     */
    public ConnectionConfiguration getConfiguration();

    /**
     * Force the agent to suspend, but queue it up to make sure all operations complete first
     */
    public void submitRequestToSuspend();

    /**
     * Forces a deletion of all backend monitoring services in the services list parameter.
     * This method will only submit it request asynchronously. It will return immediately.
     *
     * @param changeState contains lists of metric names that are undergoing change
     */
    public void submitRequestToDeleteServices(MonitorChangeState changeState);

    /**
     * Forces a deletion of all backend hosts and services that match the prefix/view viewName parameter.
     * This method will only submit it request asynchronously. It will return immediately.
     *
     * @param changeState contains lists of metric names that are undergoing change
     */
    public void submitRequestToDeleteView(MonitorChangeState changeState);

    /**
     * Forces a deletion of all backend hosts in provided
     * This method will only submit it request asynchronously. It will return immediately.
     *
     * @param changeState contains lists of host names that are undergoing change
     */
    public void submitRequestToDeleteConnectorHost(MonitorChangeState changeState);

    /**
     * Get the current state of the connector
     * @return
     */
    public ConnectionState getConnectionState();

    /**
     * Get the current number of groundwork server exception failures
     *
     * @return a snapshot of the error count
     */
    public Integer getGroundworkExceptionCount();

    /**
     * Get the current number of Monitoring connector exception failures
     *
     * @return a snapshot of the error count
     */
    public Integer getMonitorExceptionCount();
}

