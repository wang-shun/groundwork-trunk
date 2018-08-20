package org.groundwork.cloudhub.configuration;

import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.agents.monitor.VirtualSystem;

import java.util.List;

public interface ConfigurationService {

    final String NAME = "ConfigurationService";

    /**
     * Create a new configuration file, assign default values and generated ids, GUIDs, do not save
     *
     * @param virtualSystem the required virtual system type
     * @return newly created connection configuration
     * @throws CloudHubException
     */
    <T extends ConnectionConfiguration> T createConfiguration(VirtualSystem virtualSystem) throws CloudHubException;

    /**
     * Saves a connection configuration to the file system
     *
     * @param configuration
     * @throws CloudHubException
     */
    ConnectionConfiguration saveConfiguration(ConnectionConfiguration configuration) throws CloudHubException;

    /**
     * Delete a connection configuration from the file system
     *
     * @param configuration
     * @throws CloudHubException
     */
    void deleteConfiguration(ConnectionConfiguration configuration) throws CloudHubException;

    /**
     * List all connection configurations regardless of virtual system type
     *
     * @return
     * @throws CloudHubException
     */
    List<ConnectionConfiguration> listAllConfigurations() throws CloudHubException;

    /**
     * List all connection configurations by virtual system type
     *
     * @param virtualSystem
     * @return
     * @throws CloudHubException
     */
    List<? extends ConnectionConfiguration> listConfigurations(VirtualSystem virtualSystem) throws CloudHubException;

    /**
     * Given a fully qualified path to a configuration file, read it and marshall to a connection configuration
     *
     * @param path the absolute path to the configuration file
     * @return
     * @throws CloudHubException
     */
    ConnectionConfiguration readConfiguration(String path) throws CloudHubException;

    /**
     * Check for existence of configuration path
     *
     * @param path
     * @return
     * @throws CloudHubException
     */
    boolean doesConfigurationExist(String path) throws CloudHubException;

    /**
     * Refresh a configuration if it has changed
     *
     * @param configuration
     * @return
     * @throws CloudHubException
     */
    ConnectionConfiguration refreshConfiguration(ConnectionConfiguration configuration) throws CloudHubException;

    /**
     * Converts a 6.7 legacy configuration to latest configuration format, saves in new format, and then removes
     * the legacy file from the file system
     *
     * @return the newly converted connection configuration in the latest configuration format
     * @throws CloudHubException
     */
    <T extends ConnectionConfiguration> T convertLegacyConfiguration(VirtualSystem virtualSystem) throws CloudHubException;

    /**
     * Determine if a file name is a valid configuration file name
     *
     * @param fileName
     * @return true if it is a valid configuration file name
     */
    boolean matchConfigurationFileName(String fileName);

    /**
     * Generate a new agent id
     *
     * @return new UUID representing agent id
     */
    String generateAgentId();

    /**
     * Extracts a virtual system from a config file name
     * Returns null if cannot match
     *
     * @param fullPath a configuration file name in pattern of cloudhub-${virtualSystem}-${number}.xml
     * @return
     */
    VirtualSystem extractVirtualSystemFromFilename(String fullPath);

    /**
     * Count number of configurations with the given hostname in gwos.getGwosServer field
     *
     * @param hostName
     * @return count of matching hostnames
     */
    int countByHostName(String hostName);
}
