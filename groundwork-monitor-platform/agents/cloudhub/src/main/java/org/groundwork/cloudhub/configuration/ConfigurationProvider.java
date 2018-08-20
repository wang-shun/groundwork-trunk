package org.groundwork.cloudhub.configuration;

import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.monitor.MonitorAgentSynchronizerService;

import java.util.List;

public interface ConfigurationProvider {

    enum PrefixType {
        ManagementServer,
        Hypervisor,
        Network,
        Cluster,
        Storage,
        DataCenter,
        ResourcePool,
        VmNetwork,
        VmStorage,
        VmResourcePool
    };

    enum SupportsFeature {
        Profiles
    };

    /**
     * Create an instance of a specific ConnectionConfiguration
     * This class should be properly annotated with JAXB mappings
     *
     * @return a new configuration specific to this provider
     */
    ConnectionConfiguration createConfiguration();

    /**
     * Return the class object for the implementing configuration class
     *
     * @return the implementing class for this configuration provider
     */
    Class getImplementingClass();

    /**
     * Encrypt the virtualization server password for a given configuration
     *
     * @param configuration
     * @return the encrypted password
     * @throws CloudHubException
     */
    String encryptPassword(ConnectionConfiguration configuration) throws CloudHubException;

    /**
     * Decrypt the virtualization server password for a given configuration
     *
     * @param configuration
     * @return the decrypted password
     * @throws CloudHubException
     */
    String decryptPassword(ConnectionConfiguration configuration) throws CloudHubException;

    /**
     * Display name for Hypervisor
     */
    String getHypervisorDisplayName();

    /**
     * Display name for Management Server
     */
    String getManagementServerDisplayName();

    /**
     * Connector name, used in configuration file names
     */
    String getConnectorName();


    /**
     * GWOS Application Type
     */
    String getApplicationType();

    /**
     * Prefixes
     */
    String getPrefix(PrefixType prefixType);
    InventoryType prefixToInventoryType(String name);

    /**
     * Should we include this hostGroup name from management server group
     *
     * @param hostGroupName
     * @return
     */
    boolean isValidManagementServerHostGroup(String hostGroupName);

    /**
     * Determine if Host is also to be treated as Host Group in Synchronizer
     *
     * @param resource
     * @return true if host should be also treated as Host Group
     */
    boolean isHostAlsoHostGroup(MonitorAgentSynchronizerService.SynchronizedResource resource);

    /**
     * Determine if host group is a logical view. Logical views only contain hosts that are already
     * calculated and do not need to be involved in synchronize algorithm. Logical views should
     * be managed by the synchronizeInventory phase
     *
     * @param hostGroupName
     */
    boolean isLogicalView(String hostGroupName);

    /**
     * Migrate configuration after read.
     *
     * @param configuration read configuration
     */
    void migrateConfiguration(ConnectionConfiguration configuration);

    /**
     * Return CloudhubMonitorAgent implementation prototype bean name.
     *
     * @return prototype bean name
     */
    String getCloudhubMonitorAgentBeanName();

    /**
     * Create a list of delete service list info to instruct GW Services how to delete
     *
     * @param services
     * @return
     */
    List<DeleteServiceInfo> createDeleteServiceList(List<String> services);

    /**
     * Ensure that hypervisor name is a hypervisor view not a management view
     *
     * @param name
     * @return if name is a valid hypervisor view name, return it unaltered. If invalid, correct to a valid hypervisor view name
     */
    String ensureHypervisorView(String name);

    /**
     * Is the stale service synchronizer enabled for this connector.
     * Note the global config synchronizer.services.enabled in cloudhub.properties can turn it off for all connectors
     * @return true if connector is enabled to synchronize stale services
     */
    boolean isSynchronizeServicesEnabled();

    /**
     * Does provider support a feature
     * 
     * @param feature
     * @return true if feature supported
     */
    boolean supports(SupportsFeature feature);

    /**
     * Some connectors require provider logic to determine if its a primary metric by service type
     *
     * @param serviceType
     * @return
     */
    boolean isPrimaryMetric(String serviceType);

    boolean isSimpleHostGroupName(String hgName);

    String getHostGroupDescription(CloudhubAgentInfo agentInfo, String hostName);
}
