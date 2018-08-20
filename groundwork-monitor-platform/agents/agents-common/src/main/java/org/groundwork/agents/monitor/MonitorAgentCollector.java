package org.groundwork.agents.monitor;

import java.util.List;

public interface MonitorAgentCollector {

    final String NAME = "MonitorAgentCollector";

    /**
     * List the state and agent information for all monitored agents
     *
     * @return
     */
    List<? extends MonitorAgent> list();

    /**
     * Suspend an agent given the unique agent name
     *
     * @param agentName
     */
    void suspend(String agentName);

    /**
     * Un-suspend an agent given the unique agent name
     *
     * @param agentName
     */
    void unsuspend(String agentName);

    /**
     * Lookup a monitor agent by name
     *
     * @param agentIdentifier
     * @return
     */
    <T extends MonitorAgent> T lookup(String agentIdentifier);

    /**
     * Inform the agent that its configuration has been updated
     *
     */
    void setConfigurationUpdated(String agentIdentifier);

    /**
     * Specifically instruct monitor agent collector to start monitoring a configuration connection
     *
     * @param connectionConfiguration
     */
    <T extends MonitorAgent> T startMonitoringConnection(MonitorConnectionConfig connectionConfiguration);

    /**
     * Create Agent Info from configuration
     *
     * @param connection
     * @return
     */
    <T extends MonitorAgentInfo> T createMonitorAgentInfo(MonitorConnectionConfig connection);

    /**
     * Create an Monitoring Agent from the configuration and Monitor Agent info
     *
     * @param configuration
     * @return
     */
    <T extends MonitorAgent> T createMonitorAgent(MonitorConnectionConfig configuration);

    /**
     * Remove an agent from the monitored collection
     *
     * @param agentIdentifier
     * @return true if successfully removed, false if not removed or not found
     */
    boolean remove(String agentIdentifier);

    /**
     * Informs all agent threads that its configuration has been updated for a given virtual system type. One or
     * more monitoring agents may receive this message.
     *
     * @param virtualSystem The type of virtual system such as VmWare or Redhat
     * @return the count of monitor agents updated
     */
    int setConfigurationUpdated(VirtualSystem virtualSystem);
}