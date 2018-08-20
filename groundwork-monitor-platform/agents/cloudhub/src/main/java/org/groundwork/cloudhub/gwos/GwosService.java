package org.groundwork.cloudhub.gwos;

import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.agents.monitor.DeleteServicePrimaryInfo;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.gwos.messages.UpdateStatusMessages;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.HostServiceInventory;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.MonitorInventory;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringEvent;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoOperationResults;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GwosService {

    final String NAME6 = "GwosService6";
    final String NAME70 = "GwosService70";
    final String NAME71 = "GwosService71";
    final String NAMEBIZ = "GwosServiceBiz";

    enum GroundworkFeature {
        BlackListFilter
    }

    /**
     * Call GWOS to retrieve the list of hosts from GWOS
     *
     * @return a list of host names
     */
    List<String> getHostNames();

    /**
     * Retrieve minimal information for all hosts
     *
     * @return return a list of minimal information for a GWOS host
     * @since 7.1.0 introduced to support two agents sharing one host
     */
    Map<String, GWOSHost> getAllHosts();

    /**
     * Call GWOS to retreive the list of hostGroups from GWOS
     *
     * @return a list of host group names
     */
    List<String> getHostGroupNames();

    /**
     * Return the Hostgroup name that will be used in GroundWork to display the
     * Hostgroup depending on the entity (Management Server, Hypervisor) and
     * connector (VMware or RHev)
     *
     * @param agentInfo     state information about the current agent configuration
     * @param entityScope   . Valid values: ConnectorConstants.ENTITY_MGMT_SERVER,
     *                      ConnectorConstants.HYPERVISOR_VMWARE
     * @param hostGroupName Base name used in the Virtual environment
     * @return hostgroup name with the prefix that should be used to create the
     * Hostgroup in GroundWork Monitor
     */
    String buildHostGroupName(CloudhubAgentInfo agentInfo, String entityScope, String hostGroupName);

    /**
     * Add Hypervisor hosts to GroundWork database
     *
     * @param hypervisors
     * @param agentName
     * @return
     */
    boolean addHypervisors(List<BaseHost> hypervisors, String agentName);

    /**
     * Update existing hypervisors into Groundwork database
     *
     * @param hypervisors
     * @param agentName
     * @param hypervisorRunStates
     * @param isGroundworkConnector
     * @return
     */
    boolean modifyHypervisors(List<BaseHost> hypervisors, String agentName, Map<String, String> hypervisorRunStates, boolean isGroundworkConnector);

    /**
     * Delete list of hypervisors from Groundwork database
     *
     * @param hypervisors
     * @param agentId
     * @return
     */
    boolean deleteHypervisors(List<BaseHost> hypervisors, String agentId);

    /**
     * Add Virtual machines to Groundwork to Groundwork database
     *
     * @param listOfVM
     * @param agentName
     * @return
     */
    boolean addVirtualMachines(List<BaseVM> listOfVM, String agentName);

    /**
     * Update existing list of virtual machines in Groundwork database
     *
     * @param listOfVM
     * @param agentName
     * @param hypervisorRunStates
     * @return
     */
    boolean modifyVirtualMachines(List<BaseVM> listOfVM, String agentName, Map<String, String> hypervisorRunStates);

    /**
     * Delete list of virtual machines from Groundwork database
     *
     * @param listOfVM
     * @param agentName
     * @return
     */
    boolean deleteVirtualMachines(List<BaseVM> listOfVM, String agentName);

    /**
     * Add a single Host Group to the server
     *
     * @param hostGroup
     * @return
     */
    boolean addHostGroup(GWOSHostGroup hostGroup);

    /**
     * Update a host group on the server with a list of hosts
     *
     * @param hostGroup
     * @param hostList
     * @return
     */
    boolean modifyHostGroup(GWOSHostGroup hostGroup, List<String> hostList);

    /**
     * Delete a host group from the server
     *
     * @param hostGroup
     * @return
     */
    boolean deleteHostGroup(GWOSHostGroup hostGroup);

    /**
     * Send a single event message
     *
     * @param host
     * @param device
     * @param service
     * @param monitorStatus
     * @param severity
     * @param message
     * @param type
     */
    void sendEventMessage(String host, String device, String service,
                          String monitorStatus, String severity, String message, String type);


    /**
     * Test the GWOS connection
     *
     * @param configuration The configuration connection to test
     * @return true if connection is valid, otherwise throws an exception with description of error
     * @throws CloudHubException when failed to connect. Exception includes description of error
     */
    boolean testConnection(ConnectionConfiguration configuration) throws CloudHubException;

    /**
     * Authenticate against GW server using ConnectionConfiguration
     *
     * @param configuration
     * @return true if can authenticate otherwise false
     */
    public boolean authenticate(ConnectionConfiguration configuration);

    /**
     * Deletes all hosts, host groups, host/host group associations, and services for a given agent id
     *
     * @param configuration the configuration containing the agent id definition
     * @param configCount the number of configurations using this host name, to know if we can delete the connector/host or not
     * @return the results of the operation. Some operations and fail and others may succeed
     * @throws CloudHubException when something really goes wrong like network or database connection failures
     */
    DtoOperationResults deleteByAgent(ConnectionConfiguration configuration, int configCount) throws CloudHubException;

    /**
     * Delete hosts that are associated with a connector but only if host is owned by cloudhub and connector count is 0
     *
     * @param hostName
     * @param hostCount
     * @return
     */
    DtoOperationResults deleteByConnectorHost(String hostName, int hostCount);
    /**
     * Gather inventory of virtual resources
     *
     * @param options optional retrieval of inventory
     * @return a snapshot of the data center inventory
     */
    DataCenterInventory gatherInventory(InventoryOptions options);

    /**
     * Gather monitor inventory of virtual resources. If connector inventory is
     * provided, additional inventory not owned by agent is included and mapping
     * to connector monitor host identities is performed.
     *
     * @param connectorInventory connector monitor inventory or null
     * @return GWOS monitor inventory snapshot
     */
    MonitorInventory gatherMonitorInventory(MonitorInventory connectorInventory);

    /**
     * Update the status for all Hypervisors to the the provided status
     */
    void updateAllHypervisorsStatus(CloudhubAgentInfo agentInfo,
                                    String hostMonitorStatus,
                                    String serviceMonitorStatus,
                                    UpdateStatusMessages messages);

    /**
     * Update the status for all monitor inventory to the the provided status.
     */
    void updateMonitorInventoryStatus(CloudhubAgentInfo agentInfo,
                                      String hostMonitorStatus,
                                      String serviceMonitorStatus,
                                      UpdateStatusMessages messages);

    /**
     * Migrate (from older versions) application types dynamically
     */
    public boolean migrateApplicationTypes();

    /**
     * Create new application type.
     *
     * @param appTypeName name
     * @param description description
     * @param criteria criteria
     * @return created application type
     */
    public DtoApplicationType createApplicationType(String appTypeName, String description, String criteria);

    /**
     * Delete all metrics for a given service list
     *
     * @param services   a list of services (metrics) to be mass deleted
     * @param appType    the GWOS application type
     * @param metricType the type of metric, to distinguish between VM and Hypervisor for instance
     * @param agentId    the agent to restrict deletions by
     * @return results
     */
    DtoOperationResults deleteServices(List<DeleteServiceInfo> services, String appType, MetricType metricType, String agentId) throws CloudHubException;

    /**
     * Delete all metrics for a given service list by metric(service)Type
     * 
     * @param services
     * @param appType
     * @param serviceType
     * @param agentId
     * @return
     * @throws CloudHubException
     */
    DtoOperationResults deleteServices(List<DeleteServiceInfo> services, String appType, String serviceType, String agentId) throws CloudHubException;

    /**
     * Additional way to delete metrics by view, which since 7.1.1 is stored in metricType column
     *
     * @since 7.1.1
     * @param appType
     * @param sourceType
     * @param agentId
     * @return
     * @throws CloudHubException
     */
    DtoOperationResults deleteServicesBySourceType(String appType, List<String> sourceType, String agentId) throws CloudHubException;

    /**
     * Rename the hostname prefix for all host records for the given agentId. Note this method will try to update
     * the hostname and description fields
     *
     * @param agentId   the agent id to restrict this update to
     * @param oldPrefix the old prefix to be removed
     * @param newPrefix the new prefix to be prepended
     * @return the operation results with success and failure counts
     * @throws CloudHubException
     */
    DtoOperationResults renamePrefixByAgent(String agentId, String oldPrefix, String newPrefix) throws CloudHubException;

    /**
     * Log Audit Log information back to GWOS Server about a host
     *
     * @param virtualSystem the type of virtual system we are logging
     * @param hostName    the name of the host that is being manipulated
     * @param action      the action that is being executed on this entity
     * @param description a detailed description of the action
     * @param username    the name of the current user performing the action
     */
    void auditLogHost(VirtualSystem virtualSystem, String hostName, String action, String description, String username);

    /**
     * Log Audit Log information back to GWOS Server about a host
     *
     * @param virtualSystem the type of virtual system we are loggings
     * @param hostName    the name of the host that is being manipulated
     * @param action      the action that is being executed on this entity
     * @param description a detailed description of the action
     * @param username    the name of the current user performing the action
     * @param service     the name of the service that is being manipulated
     */
    void auditLogService(VirtualSystem virtualSystem, String hostName, String action, String description, String username, String service);

    /**
     * Lookup hostname, see if it is black listed
     *
     * @param name the name of the host
     * @return true if the hostname is black listed
     */
    boolean isHostNameBlackListed(String name);

    /**
     * Determine if a Groundwork Feature is available
     * See GwosService.GroundworkFeature enumeration for valid feature types
     *
     * @param feature the kind of feature to query if enabled
     * @return true if enabled otherwise false
     */
    boolean isFeatureEnabled(GwosService.GroundworkFeature feature);

    /**
     * Send any monitoring faults or exceptions that occurred during this monitoring cycle
     *
     * @param events a list of events to be sent
     * @param applicationType the application type for this event
     */
    void sendMonitoringFaults(List<MonitoringEvent> events, String applicationType);

    /**
     * Delete all hosts for the given view and agent id combination. Note that the view is prefix.
     * As of 7.1.0, supported views are:
     *  STOR-
     *  NET-
     * Hosts will automatically be removed from groups based on groupView parameter
     * Hostgroups will also be deleted
     *
     * @param view
     * @parm groupView
     * @param agentId
     * @return results
     */
    DtoOperationResults deleteView(String view, String groupView, String agentId);

    /**
     * Add all monitor host, host group, service, and service group inventory.
     *
     * @param inventory monitory inventory to add
     */
    void addMonitorInventory(MonitorInventory inventory);

    /**
     * Update all monitor host, host group, service, and service group inventory.
     *
     * @param inventory monitory inventory to update
     */
    void updateMonitorInventory(MonitorInventory inventory);

    /**
     * Delete all monitor host, host group, service, and service group inventory.
     *
     * @param inventory monitory inventory to delete
     */
    void deleteMonitorInventory(MonitorInventory inventory);

    /**
     * Update event inventory hosts, services, events, notifications and performance data.
     *
     * @param eventInventory
     */
    void modifyEventInventory(Collection<Object> eventInventory);

    /**
     * Gather Service inventory for the given connector (agent)
     *
     * @return a sync level collection of all hosts and services filtered by agentId
     */
    HostServiceInventory gatherHostServiceInventory();


    /**
     * Delete services by id. Used in ServiceSynchronizer for deleting stale services for only particular hosts
     *
     * @param servicesToDelete
     * @return
     */
    DtoOperationResults deleteServices(List<DeleteServicePrimaryInfo> servicesToDelete);

    /**
     * Lookup a hostgroup by name
     *
     * @param hostGroupName
     * @return
     */
    DtoHostGroup lookupHostGroup(String hostGroupName);

    /**
     * Lookup a host by name
     *
     * @param hostName
     * @return
     */
    DtoHost lookupHost(String hostName);

    /**
     * Get the configuration
     *
     * @return
     */
    ConnectionConfiguration getConnection();
    void setConnection(ConnectionConfiguration connection);

    /**
     * Get the agent info
     *
     * @return
     */
    CloudhubAgentInfo getAgentInfo();
    void setAgentInfo(CloudhubAgentInfo agentInfo);


}

