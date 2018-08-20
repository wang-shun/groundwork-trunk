package org.groundwork.agents.monitor;

import java.util.concurrent.Callable;

public interface MonitorAgent extends Callable<MonitorAgentResult> {

    final String NAME = "MonitorAgent";

    /**
     * Retrieve general display information about this monitor agent
     *
     * @return
     */
    MonitorAgentInfo getAgentInfo();

    /**
     * Suspend this monitor agent thread. Suspended monitors will stop all processing.
     * Suspension will not be immediate, as the thread may be sleeping or executing.
     */
    void suspend();

    /**
     * UnSuspend this monitor agent thread. Tells to agent to start gathering information again.
     * UnSuspension will not be immediate, as the thread may be sleeping or executing.
     */
    void unsuspend();

    /**
     * Tell this monitor agent that its configuration has been updated and it needs to be refreshed
     */
    void setConfigurationUpdated();

    /**
     * Shutdown this monitor agent. The thread will terminate.
     */
    void shutdown();

    /**
     * Is this monitor agent thread currently running? Will return true if suspended. Will return false if shutdown.
     *
     * @return true if the agent is running
     */
    boolean isRunning();

    /**
     * Is this monitor agent thread currently suspended?
     *
     * @return true if the agent is suspended
     */
    boolean isSuspended();

    /**
     * Tell the monitor to connect to the backend virtualization system. This is normally done automatically
     * in the thread loop.
     */
    public void connect();

    /**
     * Tell the monitor to disconnect from the backend virtualization system.
     */
    public void disconnect();

    /**
     * One pass on gathering monitor metrics
     */
    public void monitor();

    /**
     * Forces a deletion of all backend monitoring data. This method should be called with care.
     * Normally it is only called when a configuration is deleted.
     * This method will only submit it request asynchronously. It will return immediately.
     *
     */
    public void submitRequestToDeleteMonitoringData();

    /**
     * Forces a rename operation of all backend monitoring host names and descriptions.
     * This method will only submit its request asynchronously. It will return immediately.
     *
     */
    public void submitRequestToRenameHosts(String agentId, String oldPrefix, String newPrefix);

}

