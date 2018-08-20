package org.groundwork.cloudhub.statistics;

public interface MonitoringStatisticsService {

    final String NAME = "MonitoringStatisticsService";

    /**
     * Creates a new in memory statistics record zeroed out for a given agent.
     * Note that if another set of statistics exist for this agent, it will be overwritten
     *
     * @param agentName
     * @return the new statistics record
     */
    MonitoringStatistics create(String agentName);

    /**
     * Retrieve statistics for a given agent from in process state
     *
     * @param agentName
     * @return
     */
    MonitoringStatistics lookup(String agentName);

    /**
     * Enable the statistics service, disabled by default
     *
     * @param flag true to enable
     */
    void setEnabled(boolean flag);

    /**
     * Check if this service is enabled
     *
     * @return
     */
    boolean isEnabled();

    /**
     * Saves all statistics records to default area on the file system in comma-separated format
     *
     * @return
     */
    String save();

    /**
     * Rename statistics record from old name to new name
     *
     * @param oldName
     * @param newName
     * @return the converted statistics record
     */
    MonitoringStatistics rename(String oldName, String newName);

    /**
     * Read a Monitoring Statistics CSV file from file system
     *
     * @param fileName
     * @param index index (column) of statistics to read
     * @return
     */
    MonitoringStatistics readCSV(String fileName, int index);
}
