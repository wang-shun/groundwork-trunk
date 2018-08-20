package com.groundwork.dashboard.configuration;

import java.util.List;

public interface DashboardConfigurationService {
    
    /**
     * List all connection configurations regardless of virtual system type
     *
     * @return
     * @throws DashboardConfigurationException
     */
    List<DashboardConfiguration> list() throws DashboardConfigurationException;

    /**
     * Given a name of a configuration file, read it and marshall to a connection configuration
     *
     * @param name the absolute path to the configuration file
     * @return
     * @throws DashboardConfigurationException
     */
    DashboardConfiguration read(String name) throws DashboardConfigurationException;

    /**
     * Check for existence of configuration path
     *
     * @param name the name of the configuration
     * @return
     * @throws DashboardConfigurationException
     */
    boolean exists(String name) throws DashboardConfigurationException;

    /**
     * Save a configuration
     *
     * @param configuration
     * @return
     * @throws DashboardConfigurationException
     */
    DashboardConfiguration save(DashboardConfiguration configuration) throws DashboardConfigurationException;

    /**
     * Removes a dashboard
     *
     * @param name
     * @return
     * @throws DashboardConfigurationException
     */
    boolean remove(String name) throws DashboardConfigurationException;

    /**
     * Make a deep copy of a configuration
     *
     * @param original
     * @return
     * @throws DashboardConfigurationException
     */
    DashboardConfiguration copy(DashboardConfiguration original) throws DashboardConfigurationException;

}
