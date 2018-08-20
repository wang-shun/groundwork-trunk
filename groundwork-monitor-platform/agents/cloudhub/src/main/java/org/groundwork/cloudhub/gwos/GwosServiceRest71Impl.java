package org.groundwork.cloudhub.gwos;

import org.apache.log4j.Logger;
import org.groundwork.agents.GWOSSubSystem;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.inventory.MonitorInventory;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.rs.client.AuditLogClient;
import org.groundwork.rs.client.HostBlacklistClient;
import org.groundwork.rs.client.ServiceGroupClient;
import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoAuditLog;
import org.groundwork.rs.dto.DtoAuditLogList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.dto.DtoServiceGroupUpdate;
import org.groundwork.rs.dto.DtoServiceGroupUpdateList;
import org.groundwork.rs.dto.DtoServiceKey;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(GwosService.NAME71)
@Scope("prototype")
public class GwosServiceRest71Impl extends GwosServiceRest70Impl implements GwosService {

    private static Logger log = Logger.getLogger(GwosServiceRest71Impl.class);

    protected AuditLogClient auditClient = null;
    protected HostBlacklistClient blacklistClient = null;
    protected ServiceGroupClient serviceGroupClient = null;

    public GwosServiceRest71Impl() {
        super();
    }

    public GwosServiceRest71Impl(ConnectionConfiguration configuration, CloudhubAgentInfo agentInfo) {
        super(configuration, agentInfo);
        String connectionString = buildRsConnectionString(connection.getGwos());
        auditClient = new AuditLogClient(connectionString);
        blacklistClient = new HostBlacklistClient(connectionString);
        serviceGroupClient = new ServiceGroupClient(connectionString);
    }

    @Override
    public Map<String, GWOSHost> getAllHosts() {
        Map<String, GWOSHost> hosts = new ConcurrentHashMap<>();
        try {
            for (DtoHost host : hostClient.list(DtoDepthType.Simple)) {
                hosts.put(host.getHostName(), new GWOSHost(host.getHostName(), host.getAppType(), host.getAgentId()));
            }
        } catch (Exception e) {
            String msg = "Failed to retrieve all hosts from GWOS Service";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return hosts;
    }

    @Override
    public DtoOperationResults renamePrefixByAgent(String agentId, String oldPrefix, String newPrefix)
            throws CloudHubException {

        DtoOperationResults results = new DtoOperationResults();

        if (oldPrefix == null)
            oldPrefix = "";
        if (newPrefix == null)
            newPrefix = "";

        // default cases
        if (StringUtils.isEmpty(agentId))
            return results;
        if (oldPrefix.equals(newPrefix))
            return results;
        if (StringUtils.isEmpty(newPrefix))
            return results; // can't rename to blank prefix

        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.DOCKER);
        String dockerManagementServerPrefix = provider.getPrefix(ConfigurationProvider.PrefixType.ManagementServer);
        try {
            String lookupByAgent = String.format("agentId = '%s'", agentId);
            if (StringUtils.isEmpty(oldPrefix)) {
                // this should never happen, but if a set of docker hosts gets stored without prefix
                // then we have to be careful only to prefix the VMs, not Hypervisors
                // Hypervisors are in the DOCK-M Management Server host group
                for (DtoHost host : hostClient.query(lookupByAgent, DtoDepthType.Deep)) {
                    List<DtoHostGroup> groups = host.getHostGroups();
                    if (groups != null) {
                        boolean found = false;
                        for (DtoHostGroup group : groups) {
                            if (group.getName().startsWith(dockerManagementServerPrefix)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            String newHostName = newPrefix + host.getHostName();
                            renameHost(host, newHostName, results);
                        }
                    }
                }
            }
            else {
                for (DtoHost host : hostClient.query(lookupByAgent, DtoDepthType.Shallow)) {
                    if (host.getHostName().startsWith(oldPrefix)) {
                        String newHostName = host.getHostName().substring(oldPrefix.length());
                        newHostName = newPrefix + newHostName;
                        renameHost(host, newHostName, results);
                    }
                }
            }


        } catch (Exception e) {
            String msg = "Failed to modify Prefix by Agent";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return results;
    }

    /**
     * Audit log for hosts
     *
     * @since 7.1.0
     *
     * @param virtualSystem
     * @param hostName
     * @param action
     * @param description
     * @param username
     */
    @Override
    public void auditLogHost(VirtualSystem virtualSystem, String hostName, String action, String description, String username) {
        DtoAuditLogList audits = new DtoAuditLogList();
        audits.add(new DtoAuditLog(GWOSSubSystem.convertVirtualSystemToSubsystem(virtualSystem).name(), action,
                 description, username, (hostName == null) ? "(unknown)" : hostName));
        try {
            auditClient.post(audits);
        }
        catch (Exception e) {
            log.warn("Failed to log to audit for hostName: " + hostName );
        }
    }

    /**
     * Audit log for services
     *
     * @since 7.1.0
     *
     * @param virtualSystem
     * @param hostName
     * @param action
     * @param description
     * @param username
     * @param service
     */
    @Override
    public void auditLogService(VirtualSystem virtualSystem, String hostName, String action, String description, String username, String service) {
        DtoAuditLogList audits = new DtoAuditLogList();
        audits.add(new DtoAuditLog(GWOSSubSystem.convertVirtualSystemToSubsystem(virtualSystem).name(), action, description, username,
                (hostName == null) ? "(unknown)" : hostName, service));
        try {
            auditClient.post(audits);
        }
        catch (Exception e) {
            log.warn("Failed to log to audit for hostName: " + hostName + " and service " + service);
        }
    }

    protected boolean renameHost(DtoHost host, String newHostName, DtoOperationResults results) {
        String description = null;
        String deviceIdentification = null;
        if (host.getDescription() == null || host.getDescription().equals(host.getHostName())) {
            description = newHostName;
        }
        if (host.getDeviceIdentification() == null || host.getDeviceIdentification().equals(host.getHostName())) {
            deviceIdentification = newHostName;
        }
        try {
            DtoHost renamed = hostClient.rename(host.getHostName(), newHostName, description, deviceIdentification);
            if (renamed == null) {
                results.fail(host.getHostName(), "Host " + host.getHostName() + " not found. Cannot rename");
                return false;
            }
            results.success(newHostName, "Successfully renamed old host " + host.getHostName() + " to " + newHostName);
            return true;
        }
        catch (Exception e) {
            String message = "Could not rename old Host " + host.getHostName() + " to " + newHostName + " - error: " + e.getMessage();
            log.error(e);
            results.fail(newHostName, message);
            return false;
        }
    }

    public boolean isHostNameBlackListed(String name) {
        return blacklistClient.matchHostNameAgainstHostNames(name);
    }

    public boolean isFeatureEnabled(GwosService.GroundworkFeature feature) {
        switch (feature) {
            case BlackListFilter: {
                return (blacklistClient.list(0, 1).size() > 0);
            }
        }
        return false;
    }

    @Override
    public MonitorInventory gatherMonitorInventory(MonitorInventory connectorInventory) {
        MonitorInventory inventory = new MonitorInventory(null, agentInfo.getApplicationType(), agentInfo.getAgentId());
        // get all hosts that match connector agent id
        String lookupByAgent = String.format("agentId = '%s'", agentInfo.getAgentId());
        for (DtoHost dtoHost : hostClient.query(lookupByAgent, DtoDepthType.Deep)) {
            inventory.getHosts().put(dtoHost.getHostName(), dtoHost);
        }
        // get all remaining connector inventory hosts and save alias lookup
        // for those that do not match by host name; track host identity alias
        // and case mappings
        Map<String,String> hostIdentityMap = new HashMap<String,String>();
        for (String hostName : connectorInventory.getHosts().keySet()) {
            if (!inventory.getHosts().containsKey(hostName)) {
                DtoHost dtoHost = hostClient.lookup(hostName, DtoDepthType.Deep);
                if (dtoHost != null) {
                    if (!inventory.getHosts().containsKey(dtoHost.getHostName())) {
                        inventory.getHosts().put(dtoHost.getHostName(), dtoHost);
                    }
                    if (!hostName.equals(dtoHost.getHostName())) {
                        hostIdentityMap.put(dtoHost.getHostName(), hostName);
                    }
                }
            }
        }
        // get all services that match connector agent id and their hosts
        for (DtoService dtoService : serviceClient.query(lookupByAgent)) {
            inventory.getServices().put(dtoService.getHostName() + "!" + dtoService.getDescription(), dtoService);
            if (!inventory.getHosts().containsKey(dtoService.getHostName())) {
                DtoHost dtoHost = hostClient.lookup(dtoService.getHostName(), DtoDepthType.Deep);
                if (dtoHost != null) {
                    inventory.getHosts().put(dtoHost.getHostName(), dtoHost);
                }
            }
        }
        // extract services from deep inventory hosts filtered by connector inventory
        for (DtoHost dtoHost : inventory.getHosts().values()) {
            if (dtoHost.getServices() != null) {
                for (DtoService dtoService : dtoHost.getServices()) {
                    String serviceKey = dtoService.getHostName() + "!" + dtoService.getDescription();
                    if (!inventory.getServices().containsKey(serviceKey)) {
                        if (connectorInventory != null) {
                            String connectorHostName = hostIdentityMap.get(dtoService.getHostName());
                            String connectorServiceKey = ((connectorHostName != null) ?
                                    connectorHostName + "!" + dtoService.getDescription() : serviceKey);
                            if (connectorInventory.getServices().containsKey(connectorServiceKey)) {
                                inventory.getServices().put(serviceKey, dtoService);
                            }
                        } else {
                            inventory.getServices().put(serviceKey, dtoService);
                        }
                    }
                }
            }
        }
        // get all host groups that match connector agent id
        for (DtoHostGroup dtoHostGroup : hostGroupClient.query(lookupByAgent, DtoDepthType.Shallow)) {
            inventory.getHostGroups().put(dtoHostGroup.getName(), dtoHostGroup);
        }
        // get all remaining connector inventory host groups
        for (String name : connectorInventory.getHostGroups().keySet()) {
            if (!inventory.getHostGroups().containsKey(name)) {
                DtoHostGroup dtoHostGroup = hostGroupClient.lookup(name, DtoDepthType.Shallow);
                if (dtoHostGroup != null) {
                    inventory.getHostGroups().put(dtoHostGroup.getName(), dtoHostGroup);
                }
            }
        }
        // get all service groups that match connector agent id
        for (DtoServiceGroup dtoServiceGroup : serviceGroupClient.query(lookupByAgent)) {
            inventory.getServiceGroups().put(dtoServiceGroup.getName(), dtoServiceGroup);
        }
        // get all remaining connector inventory service groups
        for (String name : connectorInventory.getServiceGroups().keySet()) {
            if (!inventory.getServiceGroups().containsKey(name)) {
                DtoServiceGroup dtoServiceGroup = serviceGroupClient.lookup(name);
                if (dtoServiceGroup != null) {
                    inventory.getServiceGroups().put(dtoServiceGroup.getName(), dtoServiceGroup);
                }
            }
        }
        // normalize inventory based on host identity mappings; this approach
        // depends on GWOS server universally accepting inventory updates and
        // deletes using host identity alias and case mappings, (cloudhub uses
        // connector monitor host identities internally mapped here)
        if (!hostIdentityMap.isEmpty()) {
            // map hosts
            for (Map.Entry<String,String> hostIdentity : hostIdentityMap.entrySet()) {
                String gwosHostName = hostIdentity.getKey();
                String monitorHostName = hostIdentity.getValue();
                DtoHost dtoHost = inventory.getHosts().remove(gwosHostName);
                dtoHost.setHostName(monitorHostName);
                if (dtoHost.getServices() != null) {
                    for (DtoService dtoService : dtoHost.getServices()) {
                        dtoService.setHostName(monitorHostName);
                    }
                }
                inventory.getHosts().put(monitorHostName, dtoHost);
            }
            // map host groups hosts
            for (DtoHostGroup dtoHostGroup : inventory.getHostGroups().values()) {
                if (dtoHostGroup.getHosts() != null) {
                    for (DtoHost dtoHost : dtoHostGroup.getHosts()) {
                        String gwosHostName = dtoHost.getHostName();
                        String monitorHostName = hostIdentityMap.get(gwosHostName);
                        if (monitorHostName != null) {
                            dtoHost.setHostName(monitorHostName);
                        }
                    }
                }
            }
            // map services hosts
            for (DtoService dtoService : new ArrayList<DtoService>(inventory.getServices().values())) {
                String gwosHostName = dtoService.getHostName();
                String monitorHostName = hostIdentityMap.get(gwosHostName);
                if (monitorHostName != null) {
                    dtoService.setHostName(monitorHostName);
                    inventory.getServices().remove(gwosHostName + "!" + dtoService.getDescription());
                    inventory.getServices().put(monitorHostName + "!" + dtoService.getDescription(), dtoService);
                }
            }
            // map service groups service hosts
            for (DtoServiceGroup dtoServiceGroup : inventory.getServiceGroups().values()) {
                if (dtoServiceGroup.getServices() != null) {
                    for (DtoService dtoService : dtoServiceGroup.getServices()) {
                        String gwosHostName = dtoService.getHostName();
                        String monitorHostName = hostIdentityMap.get(gwosHostName);
                        if (monitorHostName != null) {
                            dtoService.setHostName(monitorHostName);
                        }
                    }
                }
            }
        }
        return inventory;
    }

    @Override
    public DtoApplicationType createApplicationType(String appTypeName, String description, String criteria) {
        DtoApplicationType appType = super.createApplicationType(appTypeName, description, criteria);
        appType.setDisplayName(appType.getName().toUpperCase());
        return appType;
    }

    /**
     * Add all monitor service group inventory.
     *
     * @param inventory service group inventory to add
     */
    protected void addServiceGroupMonitorInventory(Collection<DtoServiceGroup> inventory) {
        // add service groups inventory
        List<DtoServiceGroupUpdate> dtoServiceGroupUpdates = convertInventoryServiceGroupsToUpdates(inventory);
        serviceGroupClient.post(new DtoServiceGroupUpdateList(dtoServiceGroupUpdates));
    }

    /**
     * Update all monitor service group inventory.
     *
     * @param inventory service group inventory to update
     */
    protected void updateServiceGroupMonitorInventory(Collection<DtoServiceGroup> inventory) {
        // update, (delete and add), service groups inventory; delete and add
        // should be safe since service groups are not arranged in a hierarchy
        serviceGroupClient.delete(new DtoServiceGroupUpdateList(simpleInventoryServiceGroupUpdates(inventory)));
        List<DtoServiceGroupUpdate> dtoServiceGroupUpdates = convertInventoryServiceGroupsToUpdates(inventory);
        serviceGroupClient.post(new DtoServiceGroupUpdateList(dtoServiceGroupUpdates));
    }

    /**
     * Delete all monitor service group inventory.
     *
     * @param inventory service group inventory to delete
     */
    protected void deleteServiceGroupMonitorInventory(Collection<DtoServiceGroup> inventory) {
        // delete all service groups as categories
        serviceGroupClient.delete(new DtoServiceGroupUpdateList(simpleInventoryServiceGroupUpdates(inventory)));
    }

    /**
     * Convert inventory service groups to service group updates.
     *
     * @param dtoServiceGroups inventory service groups
     * @return inventory categories
     */
    private List<DtoServiceGroupUpdate> convertInventoryServiceGroupsToUpdates(Collection<DtoServiceGroup> dtoServiceGroups) {
        List<DtoServiceGroupUpdate> dtoServiceGroupUpdates = new ArrayList<DtoServiceGroupUpdate>(dtoServiceGroups.size());
        for (DtoServiceGroup dtoServiceGroup : dtoServiceGroups) {
            DtoServiceGroupUpdate dtoServiceGroupUpdate = new DtoServiceGroupUpdate();
            dtoServiceGroupUpdate.setName(dtoServiceGroup.getName());
            dtoServiceGroupUpdate.setDescription(dtoServiceGroup.getDescription());
            dtoServiceGroupUpdate.setAppType(dtoServiceGroup.getAppType());
            dtoServiceGroupUpdate.setAgentId(dtoServiceGroup.getAgentId());
            if ((dtoServiceGroup.getServices() != null) && !dtoServiceGroup.getServices().isEmpty()) {
                for (DtoService dtoService : dtoServiceGroup.getServices()) {
                    dtoServiceGroupUpdate.addService(new DtoServiceKey(dtoService.getDescription(), dtoService.getHostName()));
                }
            }
            dtoServiceGroupUpdates.add(dtoServiceGroupUpdate);
        }
        return dtoServiceGroupUpdates;
    }

    /**
     * Convert inventory service groups to simple service group update identities.
     *
     * @param dtoServiceGroups inventory service groups
     * @return simple inventory service group update identities
     */
    private List<DtoServiceGroupUpdate> simpleInventoryServiceGroupUpdates(Collection<DtoServiceGroup> dtoServiceGroups) {
        List<DtoServiceGroupUpdate> simpleDtoServiceGroupUpdates = new ArrayList<DtoServiceGroupUpdate>(dtoServiceGroups.size());
        for (DtoServiceGroup dtoServiceGroup : dtoServiceGroups) {
            DtoServiceGroupUpdate simpleDtoServiceGroupUpdate = new DtoServiceGroupUpdate();
            simpleDtoServiceGroupUpdate.setName(dtoServiceGroup.getName());
            simpleDtoServiceGroupUpdates.add(simpleDtoServiceGroupUpdate);
        }
        return simpleDtoServiceGroupUpdates;
    }
}
