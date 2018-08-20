package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.connectors.azure.AzureConfigurationProvider;
import org.groundwork.cloudhub.gwos.GWOSHost;
import org.groundwork.cloudhub.gwos.GWOSHostGroup;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.gwos.GwosServiceFactory;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service(MonitorAgentSynchronizer.NAME)
public class MonitorAgentSynchronizerService implements MonitorAgentSynchronizer {

    private static Logger log = Logger.getLogger(MonitorAgentSynchronizerService.class);

    @Resource(name = GwosServiceFactory.NAME)
    private GwosServiceFactory gwosServiceFactory;

    @Resource(name = ConnectorFactory.NAME)
    private ConnectorFactory connectorFactory;

    public enum SynchronizedResourceType
    {
        HOST,
        SynchronizedResourceType, VM
    };

    public class SynchronizedResource {

        private String name;
        private SynchronizedResourceType type;
        private BaseHost hosto;
        private BaseVM vmo;

        public SynchronizedResource(String theName, SynchronizedResourceType theType, BaseHost vbh, BaseVM vbvm) {
            this.name = theName;
            this.type = theType;
            this.hosto = vbh;
            this.vmo = vbvm;
        }

        public SynchronizedResourceType getType() {
            return this.type;
        }

        public BaseHost getHost() {
            return this.hosto;
        }

        public BaseVM getVM() {
            return this.vmo;
        }

        public String getName() {
            return this.name;
        }
    }

    @Override
    public MonitoringState synchronize(
            ConnectionConfiguration configuration,
            CloudhubAgentInfo agentInfo,
            MonitoringState monitoringState,
            DataCenterSyncResult syncResult) {

        if (isAlternativeVirtualSystem(configuration.getCommon().getVirtualSystem())) {
            AlternateSynchronizer s2 = new AlternateSynchronizer();
            return s2.synchronize(configuration, agentInfo, monitoringState, syncResult, this);
        }

        if (log.isDebugEnabled()) {
            log.debug("Inside SyncMonitorAgentData Method for agent " + agentInfo.getName());
        }
        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
        // sent to add hypervisors
        List<BaseHost> comparedHostList = new ArrayList<BaseHost>();
        // sent to add VMs
        List<BaseVM> comparedVMList = new ArrayList<BaseVM>();
        List<String> listOfHypervisorsToAdd = new ArrayList<String>();
        List<String> listOfHypervisors = new ArrayList<String>();
        List<String> vmList = new ArrayList<String>();
        Set<String> hostsOrVmsList = new HashSet<String>();
        Set<String> hostsOrVmsList2 = new HashSet<String>();
        Map<String, String> prefixlessHostgroupMap = null;

        BaseHost baseHost = null;
        BaseVM baseVM = null;

        SynchronizedResource vemaBaseObject = null;

        GWOSHostGroup hypervisorHostGroup = null;
        GWOSHostGroup hostGroups = null;
        GWOSHostGroup deleteHostGroups = null;

        // retrieve all hosts (necessary for finding hosts not owned by the current agent, but still monitored)
        GwosService gwosService = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
        Map<String, GWOSHost> groundworkHosts = syncResult.getGwosInventory().getAllHosts();
        Map<String, String> groundworkHostKeys = new HashMap<String, String>();
        for (String groundworkHostKey : groundworkHosts.keySet()) {
            groundworkHostKeys.put(configuration.makeHostKey(groundworkHostKey), groundworkHostKey);
        }
        Map<String, BaseHost> hosts = monitoringState.hosts();
        // merge all hosts and VMs into a single resource
        Map<String, SynchronizedResource> hostsAndVms = mergeHostsAndVMs(hosts);

        // Compare gwosHostList and vemaHostList
        if (log.isDebugEnabled()) {
            log.debug("Compare ["
                    + groundworkHosts.size()
                    + "] Hosts from gwos Host Webservice  and ["
                    + hostsAndVms.size()
                    + "] HostsandVms from CloudHub (VSystem="
                    + configuration.getCommon().getVirtualSystem()
                    + ")");
        }

        // Synchronization loop: walk through all Hosts and VMs
        for (String hostOrVm : hostsAndVms.keySet()) {
            boolean isTransient = false;
            vemaBaseObject = hostsAndVms.get(hostOrVm);

            String groundworkHostKey = groundworkHostKeys.get(configuration.makeHostKey(hostOrVm));
            GWOSHost gwosHost = ((groundworkHostKey != null) ? groundworkHosts.get(groundworkHostKey) : null);
            if (gwosHost == null) {
                // not found in Groundwork database, so add it
                if (log.isDebugEnabled())
                    log.debug("Host: '" + hostOrVm
                            + "' TYPE >>> '" + vemaBaseObject.getType()
                            + "' Enum Type Return; '"
                            + SynchronizedResourceType.HOST + "'");

                if (vemaBaseObject.getType() == SynchronizedResourceType.HOST) {
                    baseHost = vemaBaseObject.getHost();
                    // @since 7.1.1 skip over AWS AZ Hosts
                    if (baseHost.isTransient()) {
                        isTransient = true;
                    }
                    baseHost.setGwosHostName(baseHost.getHostName());

                    if (!baseHost.isTransient() && log.isInfoEnabled())
                        log.info("Hypervisor '" + baseHost.getHostName()
                            + "' in CloudHub but not in GWOS. Will be added");

                    comparedHostList.add(baseHost);

                    if (log.isDebugEnabled())
                        log.debug("Temp Host List size: [" + comparedHostList.size() + "]");

                } else if (vemaBaseObject.getType() == SynchronizedResourceType.VM) {
                    baseVM = vemaBaseObject.getVM();
                    baseVM.setGwosHostName(baseVM.getVMName());

                    if (log.isInfoEnabled())
                        log.info("Virtual Machine '" + baseVM.getVMName()
                            + "' in CloudHub but not in GWOS. Will be added");

                    comparedVMList.add(baseVM);

                    if (log.isDebugEnabled())
                        log.debug("Filtered VM NAME: '" + baseVM.getVMName()
                                + "', TempList VM size: [" + comparedVMList.size()
                                + "]");
                } else {
                    // should NEVER get here.
                    log.error("Unknown host/vm type: '" + vemaBaseObject.getType() + "'");
                }
            } else {
                // save GWOS host name
                if (vemaBaseObject.getType() == SynchronizedResourceType.HOST) {
                    baseHost = vemaBaseObject.getHost();
                    // @since 7.1.1 skip over AWS AZ Hosts
                    if (baseHost.isTransient()) {
                        isTransient = true;
                    }
                    baseHost.setGwosHostName(gwosHost.getHostName());
                } else if (vemaBaseObject.getType() == SynchronizedResourceType.VM) {
                    baseVM = vemaBaseObject.getVM();
                    baseVM.setGwosHostName(gwosHost.getHostName());
                } else {
                    // should NEVER get here.
                    log.error("Unknown host/vm type: '" + vemaBaseObject.getType() + "'");
                }
            }
            /* Create a stringlist of all Hypervisers and Virtual Machines in VM */
            hostsOrVmsList.add(hostOrVm);
            if (!isTransient) {
                hostsOrVmsList2.add(hostOrVm);
            }
        }

        // Delete orphaned GWOS Hypervisor and Vm Hosts
        deleteHostsAndVMs(gwosService, groundworkHosts, hostsOrVmsList2, agentInfo.getAgentId(), configuration);

        // Add new GWOS Hypervisor Hosts
        if (comparedHostList.size() > 0)  // i.e. if there are new hosts to tell GWOS about
        {
            comparedHostList = filterHypervisors(comparedHostList);
            boolean writeStatus = gwosService.addHypervisors(comparedHostList, agentInfo.getName());
            if (writeStatus && log.isInfoEnabled()) {
                log.info("Adding [" + comparedHostList.size() + "]" + " Hypervisors to the gwosService");
            }
        }

        // Add new GWOS Vm Hosts
        if (comparedVMList.size() > 0)  // i.e. if there are new VMs to tell GWOS about
        {
            if (log.isInfoEnabled())
                log.info("Adding [" + comparedVMList.size() + "]" + " Virtual Machines to the gwosService");
            gwosService.addVirtualMachines(comparedVMList, agentInfo.getName());
        }

        // call GWOS getHotGroup Webservice.
        List<String> gwosHostGroupList = gwosService.getHostGroupNames();
        if (log.isDebugEnabled()) {
            log.debug("Total Number of HostGroups from GwosHostGroup Service: '" + gwosHostGroupList.size() + "'");
        }

		/* Returns a list of Hostgroup names without the prefix */
        prefixlessHostgroupMap = stripHostGroupList(gwosHostGroupList);

        // Clear lists before it gets used
        listOfHypervisors.clear();
        listOfHypervisorsToAdd.clear();

        for (String hostKey : hosts.keySet()) {
            SynchronizedResource hgVemaBaseObject = hostsAndVms.get(hostKey);
            String prefixHostGroup = prefixlessHostgroupMap.get(hostKey);
            boolean isValidHostGroup = provider.isHostAlsoHostGroup(hgVemaBaseObject);
            if (!hostKey.equalsIgnoreCase(ConnectorConstants.HOSTLESS_VMS) && provider.isValidManagementServerHostGroup(prefixHostGroup)) {
                if (hgVemaBaseObject != null) {
                    listOfHypervisors.add(hgVemaBaseObject.getHost().getGwosHostName());
                }
            }
            if (ConnectorFactory.isSpecialHost(hostKey) || isValidHostGroup == false || provider.isSimpleHostGroupName(hostKey))
                continue;

            if (hgVemaBaseObject != null && isValidHostGroup) {
                if (!prefixlessHostgroupMap.containsKey(hgVemaBaseObject.getHost().getHostName())) {
                    if (log.isDebugEnabled()) {
                        log.debug(hgVemaBaseObject.getHost().getHostName() + " not in prefixlessHostGroups: " + prefixlessHostgroupMap.keySet().toString());
                    }
                    hostGroups = new GWOSHostGroup(
                            gwosService.buildHostGroupName(
                                    agentInfo,
                                    ConnectorConstants.ENTITY_HYPERVISOR,
                                    hgVemaBaseObject.getHost().getHostName()),
                            provider.getHostGroupDescription(agentInfo, hgVemaBaseObject.getHost().getHostName()),
                            agentInfo.getConnectorName(),
                            agentInfo.getApplicationType()
                    );
                    listOfHypervisorsToAdd.add(hgVemaBaseObject.getHost().getHostName());
                    gwosService.addHostGroup(hostGroups);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Host Groups are added to GWOS Server #["
                    + listOfHypervisorsToAdd.size() + "]");
        }

        // add management server to hostgroup
        if (!provider.getConnectorName().equals(AzureConfigurationProvider.CONNECTOR_NAME)) {
            String hostName = "";
            hostName = configuration.getConnection().getHostName();
            String hostGroupName = gwosService.buildHostGroupName(
                    agentInfo,
                    ConnectorConstants.ENTITY_MGMT_SERVER,
                    hostName);

            GWOSHostGroup gwosHostGroup = new GWOSHostGroup(hostGroupName, agentInfo.getManagementServerName(),
                    agentInfo.getConnectorName(), agentInfo.getApplicationType());

            if (!(gwosHostGroupList.contains(hostGroupName))) {
                if (log.isDebugEnabled()) {
                    log.debug("ADDING ManagementServer '" + hostGroupName + "' to HostGroup");
                }
                gwosService.addHostGroup(gwosHostGroup);
            }

            if (log.isDebugEnabled()) {
                log.debug("Modifying MgmtServer '" + hostGroupName
                        + "' to HostGroup with HypervisorList of '"
                        + listOfHypervisors.size() + "' elements");
            }

            gwosService.modifyHostGroup(gwosHostGroup, listOfHypervisors);
        }

        // Delete hostGroupList not present in hypervisorList
        if (log.isDebugEnabled()) {
            log.debug("Check which HostGroups no longer present in GWOS system (for deletion)");
        }

        for (Map.Entry<String, String> hostGroupEntry : prefixlessHostgroupMap.entrySet()) {
            String gwosHostGroupName = hostGroupEntry.getKey();
            String fullHostGroupName = hostGroupEntry.getValue();
            if (provider.isLogicalView(fullHostGroupName)) {
                if (log.isDebugEnabled()) {
                    log.debug("skipping logical view: " + gwosHostGroupName + ", " + fullHostGroupName);
                }
                continue;
            }
            if (!(hostsOrVmsList.contains(gwosHostGroupName))) {
                /* Need to determine if it's a management or hypervisor */
                if (gwosHostGroupList.contains(
                        gwosService.buildHostGroupName(agentInfo,
                                ConnectorConstants.ENTITY_HYPERVISOR, gwosHostGroupName))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Delete this HostGroup: '" + gwosHostGroupName + "'");
                    }

                    deleteHostGroups = new GWOSHostGroup(
                            gwosService.buildHostGroupName(agentInfo,
                                    ConnectorConstants.ENTITY_HYPERVISOR,
                                    gwosHostGroupName),
                            provider.getHostGroupDescription(agentInfo, gwosHostGroupName),
                            agentInfo.getConnectorName(),
                            agentInfo.getApplicationType());

                    gwosService.deleteHostGroup(deleteHostGroups);
                } else if (!gwosHostGroupList.contains(
                        gwosService.buildHostGroupName(agentInfo,
                                ConnectorConstants.ENTITY_MGMT_SERVER,
                                gwosHostGroupName))) {
                    /* It's not the management server so it should be removed */
                    if (log.isDebugEnabled()) {
                        log.debug("Delete special HostGroup: '" + gwosHostGroupName + "'");
                    }

                    deleteHostGroups = new GWOSHostGroup(
                            hostGroupEntry.getValue(),
                            agentInfo.getHyperVisorName(),
                            agentInfo.getConnectorName(),
                            agentInfo.getApplicationType());

                    gwosService.deleteHostGroup(deleteHostGroups);
                }
            }
        }

        // ModifyHostGroup for Hypervisor List
        BaseHost vbHost = null;
        //BaseVM vmName = null;
        for (BaseHost hypervisorHost : hosts.values()) {

            // with Cloudera specifically (thus far), we do not want to store Hosts as Host Groups
            // only clusters and mgmt server
            SynchronizedResource sr = hostsAndVms.get(hypervisorHost.getHostName());
            if (sr != null && !provider.isHostAlsoHostGroup(sr)) {
                continue;
            }

            vbHost = hosts.get(hypervisorHost.getHostName());
            for (String vms : vbHost.getVMPool().keySet()) {
                BaseVM vm = vbHost.getVM(vms);
                if (vm != null) {
                    if (vm.getGwosHostName() == null) {
                        vm.setGwosHostName(vm.getName());
                    }
                    vmList.add(vm.getGwosHostName());
                }
                else {
                    log.error("Alert: vmname is (null)");
                }
            }

            // Create the HostGroups for VMs
            String checkHostName = hypervisorHost.getHostName();
            if (ConnectorFactory.isSpecialHost(checkHostName)) {
                checkHostName = checkHostName.replaceFirst("-", ":");
            }
            String hgName = prefixlessHostgroupMap.get(checkHostName);
            if (hgName == null) {
                hgName = gwosService.buildHostGroupName(agentInfo,
                        ConnectorConstants.ENTITY_HYPERVISOR,
                        checkHostName);
            }
            hgName = provider.ensureHypervisorView(hgName);
            hypervisorHostGroup = new GWOSHostGroup(
                    hgName,
                    provider.getHostGroupDescription(agentInfo, hgName),
                    agentInfo.getConnectorName(),
                    agentInfo.getApplicationType());

            // -----------------------------------------------------------------------
            // This code Proves that all VMs end up in the right Hostgroups.
            // Now... as to why the from-GWOS hostgroups don't get trimmed of the
            // vms that have been moved (or deactivated)... that's another question
            // -----------------------------------------------------------------------
            if (log.isDebugEnabled()) {
                StringBuffer sb = new StringBuffer(1000);
                sb.append("\nHypervisorHostGroup: '" + hypervisorHostGroup.getHostGroupName() + "'\n");
                for (String vmHandle : vmList) {
                    sb.append("modifylist: '" + vmHandle + "'\n");
                }
                log.trace(sb.toString());
            }

            // Modify the HostGroup with VM's
            gwosService.modifyHostGroup(hypervisorHostGroup, vmList);

            vmList.clear();
        }
        return new MonitoringState(hosts);
    }

    protected void deleteHostsAndVMs(GwosService gwosService, Map<String, GWOSHost> gwosHosts, Set<String> hostsOrVmsList,
                                     String agentName, ConnectionConfiguration configuration) {
        BaseHost deleteHost = null;
        List<BaseHost> deleteHostList = new ArrayList<>();

        Set<String> hostsOrVmsKeys = new HashSet<String>();
        for (String hostOrVm : hostsOrVmsList) {
            hostsOrVmsKeys.add(configuration.makeHostKey(hostOrVm));
        }
        for (GWOSHost gwosHost : gwosHosts.values()) {
            if (gwosHost.getAgentId() != null && gwosHost.getAgentId().equals(agentName)) {
                if (!(hostsOrVmsKeys.contains(configuration.makeHostKey(gwosHost.getHostName())))) {
                    deleteHost = new BaseHost(gwosHost.getHostName());
                    deleteHostList.add(deleteHost);
                }
            }
        }
        if (deleteHostList.size() > 0) {
            if (log.isDebugEnabled())
                log.debug("Delete  [" + deleteHostList.size() + "]"
                        + " Hypervisors and VM's from the GroundWork system");
            gwosService.deleteHypervisors(deleteHostList, agentName);
        }
    }

    /**
     * This method seperates the prefix from the hostGroupNames in the
     * gwosHostGroupList
     *
     * @param gwosHostGroupList
     * @return
     */
    static Map<String, String> stripHostGroupList(List<String> gwosHostGroupList) {
        Map<String, String> strippedNames = new HashMap<String, String>();
        if (log.isDebugEnabled())
            log.debug("prefixless input list: " + gwosHostGroupList.toString());
        for (String gwosHGName : gwosHostGroupList) {
            StringTokenizer st = new StringTokenizer(gwosHGName, ":");

            // JUST the first splitting of {tag}:{hostname}
            String hgType = st.hasMoreTokens() ? st.nextToken() : null;
            String hgName = st.hasMoreTokens() ? st.nextToken() : null;

            if (hgName != null)
                strippedNames.put(hgName, gwosHGName);
            else
                log.debug("hgName detokenizer returned nulls (pat=':') in gwosHGName='" + gwosHGName + "'");
        }
        return strippedNames;
    }

    @Override
    public DataCenterSyncResult synchronizeInventory(DataCenterInventory vemaInventory,
                                                     DataCenterInventory gwosInventory,
                                                     ConnectionConfiguration configuration,
                                                     CloudhubAgentInfo agentInfo,
                                                     GwosService gwosService)
    {
        long start = new Date().getTime();
        DataCenterSyncResult results = new DataCenterSyncResult();
        if (log.isDebugEnabled())
            log.debug("Starting Sync Inventory for agent " + agentInfo.getName());

        // Process Hypervisors...
        InventoryResults hypervisorResults = processHypervisorInventory(vemaInventory.getHypervisors(), gwosInventory.getHypervisors(),
                configuration.getCommon().getVirtualSystem(), InventoryType.Hypervisor, agentInfo);
        results.setHypervisorsAdded(hypervisorResults.added);
        results.setHypervisorsModified(hypervisorResults.modified);
        results.setHypervisorsDeleted(hypervisorResults.deleted);

        // Process VMs...
        InventoryResults vmrResults = processVirtualMachineInventory(vemaInventory.getVirtualMachines(),
                gwosInventory.getVirtualMachines(),
                configuration.getCommon().getVirtualSystem(), InventoryType.VirtualMachine, agentInfo);
        results.setVmsAdded(vmrResults.added);
        results.setVmsModified(vmrResults.modified);
        results.setVmsDeleted(vmrResults.deleted);

        // Process Datastores...
        InventoryResults datastoreResults = processInventory(vemaInventory.getDatastores(), gwosInventory.getDatastores(),
                configuration.getCommon().getVirtualSystem(), InventoryType.Datastore, agentInfo, gwosService);
        results.setDatastoresAdded(datastoreResults.added);
        results.setDatastoresModified(datastoreResults.modified);
        results.setDatastoresDeleted(datastoreResults.deleted);

        // Process Networks...
        InventoryResults networkResults = processInventory(vemaInventory.getNetworks(), gwosInventory.getNetworks(),
                configuration.getCommon().getVirtualSystem(), InventoryType.Network, agentInfo, gwosService);
        results.setNetworksAdded(networkResults.added);
        results.setNetworksModified(networkResults.modified);
        results.setNetworksDeleted(networkResults.deleted);

        // Process ResourcePools...
        InventoryResults poolResults = processInventory(vemaInventory.getResourcePools(), gwosInventory.getResourcePools(),
                configuration.getCommon().getVirtualSystem(), InventoryType.ResourcePool, agentInfo, gwosService);
        results.setResourcePoolsAdded(poolResults.added);
        results.setResourcePoolsModified(poolResults.modified);
        results.setResourcePoolsDeleted(poolResults.deleted);

        // CLOUDHUB-296 synchronize tagged groups
        InventoryResults taggedGroupResults = processInventory(vemaInventory.getTaggedGroups(), gwosInventory.getTaggedGroups(),
                configuration.getCommon().getVirtualSystem(), InventoryType.TaggedGroup, agentInfo, gwosService);
        results.setTaggedGroupsAdded(taggedGroupResults.added);
        results.setTaggedGroupsModified(taggedGroupResults.modified);
        results.setTaggedGroupsDeleted(taggedGroupResults.deleted);

        if (log.isDebugEnabled()) {
            long end = new Date().getTime();
            log.debug("Completed Sync Inventory for agent " + agentInfo.getName() + " in " + (end - start) + " ms");
        }
        return results;
    }

    private class InventoryResults {
        int added = 0;
        int deleted = 0;
        int modified = 0;
    }

    private InventoryResults processInventory(Map<String, InventoryContainerNode> vemaInventory,
                                  Map<String, InventoryContainerNode> gwosInventory,
                                  VirtualSystem virtualSystem,
                                  InventoryType inventoryType,
                                  CloudhubAgentInfo agentInfo,
                                  GwosService gwosService)
    {
        InventoryResults results = new InventoryResults();
        List<String> vema = getListFromInventory(vemaInventory);
        List<String> gwos = getListFromInventory(gwosInventory);
        List<String> deleted = new ArrayList<String>(gwos);
        deleted.removeAll(vema);
        List<String> same = new ArrayList<String>(gwos);
        same.retainAll(vema);
        List<String> added = new ArrayList<String>(vema);
        added.removeAll(gwos);

        for (String groupName : added) {
            if (!isAlternativeVirtualSystem(virtualSystem)) {
                String prefixedGroupName = connectorFactory.makePrefix(virtualSystem, inventoryType) + groupName;
                addHostGroup(prefixedGroupName, inventoryType,
                        vemaInventory.get(groupName), agentInfo, gwosService);
            }
            results.added++;
        }
        for (String groupName : same) {
            InventoryContainerNode vemaNode = vemaInventory.get(groupName);
            InventoryContainerNode gwosNode = gwosInventory.get(groupName);
            if (hasChanged(vemaNode, gwosNode)) {
                if (!isAlternativeVirtualSystem(virtualSystem)) {
                    String prefixedGroupName = connectorFactory.makePrefix(virtualSystem, inventoryType) + groupName;
                    modifyHostGroup(prefixedGroupName, inventoryType,
                            vemaInventory.get(groupName), agentInfo, gwosService);
                }
                results.modified++;
            }
        }
        for (String groupName : deleted) {
            if (!isAlternativeVirtualSystem(virtualSystem)) {
                String prefixedGroupName = connectorFactory.makePrefix(virtualSystem, inventoryType) + groupName;
                deleteHostGroup(prefixedGroupName, vemaInventory.get(groupName), agentInfo, gwosService);
            }
            results.deleted++;
        }
        return results;
    }

    private boolean hasChanged(InventoryContainerNode vemaNode, InventoryContainerNode gwosNode) {
        List<String> vema = getListFromVMInventory(vemaNode.getVms());
        List<String> gwos = getListFromVMInventory(gwosNode.getVms());
        List<String> deleted = new ArrayList<String>(gwos);
        deleted.removeAll(vema);
        List<String> added = new ArrayList<String>(vema);
        added.removeAll(gwos);
        return (added.size() > 0 || deleted.size() > 0);
    }

    private String addHostGroup(String prefixedGroupName,
                                  InventoryType inventoryType,
                                  InventoryContainerNode node,
                                  CloudhubAgentInfo agentInfo,
                                  GwosService gwosService) {
        return storeHostGroup(prefixedGroupName, inventoryType, node, agentInfo, gwosService, true);
    }

    private String modifyHostGroup(String prefixedGroupName,
                                InventoryType inventoryType,
                                InventoryContainerNode node,
                                CloudhubAgentInfo agentInfo,
                                GwosService gwosService) {
        return storeHostGroup(prefixedGroupName, inventoryType, node, agentInfo, gwosService, false);
    }

    private String storeHostGroup(String prefixedGroupName,
                                  InventoryType inventoryType,
                                  InventoryContainerNode node,
                                  CloudhubAgentInfo agentInfo,
                                  GwosService gwosService,
                                  boolean isNew)
    {
        String description = (isNew) ? agentInfo.getHyperVisorName() + " - " + inventoryType.name() : null;
        String alias  = (isNew) ? agentInfo.getConnectorName() + "-" + inventoryType.name() : null;
                GWOSHostGroup hostGroup = new GWOSHostGroup(
                prefixedGroupName,
                description,
                alias,
                agentInfo.getApplicationType());
        List<String> vms = new ArrayList<String>(node.getVms().size());
        for (VirtualMachineNode vm : node.getVms().values()) {
            vms.add(vm.getName());
        }
        gwosService.modifyHostGroup(hostGroup, vms); // call modify even though adding
        return prefixedGroupName;
    }

    private String deleteHostGroup(String prefixedGroupName,
                                   InventoryContainerNode node,
                                   CloudhubAgentInfo agentInfo,
                                   GwosService gwosService)
    {
        GWOSHostGroup hostGroup = new GWOSHostGroup(
                prefixedGroupName,
                agentInfo.getHyperVisorName(),
                agentInfo.getConnectorName(),
                agentInfo.getApplicationType());
        gwosService.deleteHostGroup(hostGroup);
        return prefixedGroupName;
    }


    /**
     * Reduce group list by type and strip off prefix
     *
     * @param allGroups the list of all GWOS groups
     * @param virtualSystem the virtual system type such as VMWARE
     * @param inventoryType the inventory type such as Network, Hypervisor
     * @return a reduced list of groups with prefix stripped off
     */
    private List<String> reduceGroups(List<String> allGroups, VirtualSystem virtualSystem, InventoryType inventoryType) {
        List<String> reducedList = new ArrayList<String>();
        for (String group : allGroups) {
            String prefix = connectorFactory.makePrefix(virtualSystem, inventoryType);
            if (group.startsWith(prefix)) {
                reducedList.add(group.substring(prefix.length()));
            }
        }
        return reducedList;
    }


    private List<String> getListFromInventory(Map<String, InventoryContainerNode> inventory) {
        List<String> list = new ArrayList<String>(inventory.size());
        for (Map.Entry<String, InventoryContainerNode> entry : inventory.entrySet()) {
            if (!entry.getValue().isTransient()) {
                list.add(entry.getKey());
            }
        }

        return list;
    }

    private List<String> getListFromVMInventory(Map<String, VirtualMachineNode> inventory) {
        List<String> list = new ArrayList<String>(inventory.size());
        for (String name : inventory.keySet()) {
            list.add(name);
        }
        return list;
    }

    private static final String FILTERED_HYPERVISORS[] = {
            ConnectorConstants.HOSTLESS_VMS
    };

    public List<BaseHost> filterHypervisors(List<BaseHost> hypervisors) {
        List<BaseHost> filtered = new ArrayList<BaseHost>();
        for (BaseHost hypervisor : hypervisors) {
            for (String filter : FILTERED_HYPERVISORS) {
                if (!filter.equals(hypervisor.getHostName())) {
                    filtered.add(hypervisor);
                    break;
                }
            }
        }
        return filtered;
    }

    public List<BaseVM> filterVirtualMachines(List<BaseVM> vms, DataCenterInventory gwosInventory) {
        List<BaseVM> filteredVms = new ArrayList<BaseVM>();
        for (BaseVM vm : vms) {
            if (!gwosInventory.getVirtualMachines().containsKey(vm.getVMName())) {
                if (log.isInfoEnabled())
                    log.info("+++ NOT filtering: " + vm.getVMName() + ", " + vm.getSystemName());
                filteredVms.add(vm);
            }
            else {
                if (log.isInfoEnabled())
                    log.info("+++ filtering: " + vm.getVMName() + ", " + vm.getSystemName());
            }
        }
        return filteredVms;
    }

    /**
     * Temporarily non-destructive synchronizer until full refactoring of inventory phase completed
     *
     * @param monitoredInventory
     * @param gwosInventory
     * @param virtualSystem
     * @param inventoryType
     * @param agentInfo
     * @return
     */
    private InventoryResults processHypervisorInventory(Map<String, InventoryContainerNode> monitoredInventory,
                                              Map<String, InventoryContainerNode> gwosInventory,
                                              VirtualSystem virtualSystem,
                                              InventoryType inventoryType,
                                              CloudhubAgentInfo agentInfo)
    {
        InventoryResults results = new InventoryResults();
        List<String> vema = getListFromInventory(monitoredInventory);
        List<String> gwos = getListFromInventory(gwosInventory);
        List<String> deleted = new ArrayList<String>(gwos);
        deleted.removeAll(vema);
        List<String> same = new ArrayList<String>(gwos);
        same.retainAll(vema);
        List<String> added = new ArrayList<String>(vema);
        added.removeAll(gwos);

        for (String groupName : added) {
            String prefixedGroupName = connectorFactory.makePrefix(virtualSystem, inventoryType) + groupName;
            if (!isAlternativeVirtualSystem(virtualSystem))
//            addHostGroup(prefixedGroupName, inventoryType,
//                    inventory.get(groupName), agentInfo, gwosService);
            results.added++;
        }
        for (String groupName : same) {
            InventoryContainerNode vemaNode = monitoredInventory.get(groupName);
            InventoryContainerNode gwosNode = gwosInventory.get(groupName);
            if (hasChanged(vemaNode, gwosNode)) {
                String prefixedGroupName = connectorFactory.makePrefix(virtualSystem, inventoryType) + groupName;
//                modifyHostGroup(prefixedGroupName, inventoryType,
//                        inventory.get(groupName), agentInfo, gwosService);
                results.modified++;
            }
        }
        for (String groupName : deleted) {
            String prefixedGroupName = connectorFactory.makePrefix(virtualSystem, inventoryType) + groupName;
//            deleteHostGroup(prefixedGroupName, inventory.get(groupName), agentInfo, gwosService);
            results.deleted++;
        }
        return results;
    }

    /**
     * Temporarily non-destructive synchronizer until full refactoring of inventory phase completed
     *
     * @param monitoredInventory
     * @param gwosInventory
     * @param virtualSystem
     * @param inventoryType
     * @param agentInfo
     * @return
     */
    private InventoryResults processVirtualMachineInventory(Map<String, VirtualMachineNode> monitoredInventory,
                                                        Map<String, VirtualMachineNode> gwosInventory,
                                                        VirtualSystem virtualSystem,
                                                        InventoryType inventoryType,
                                                        CloudhubAgentInfo agentInfo)
    {
        InventoryResults results = new InventoryResults();
        List<String> monitored = getListFromVMInventory(monitoredInventory);
        List<String> gwos = getListFromVMInventory(gwosInventory);
        List<String> deleted = new ArrayList<String>(gwos);
        deleted.removeAll(monitored);
        List<String> same = new ArrayList<String>(gwos);
        same.retainAll(monitored);
        List<String> added = new ArrayList<String>(monitored);
        added.removeAll(gwos);

        for (String groupName : added) {
//            String prefixedGroupName = makePrefix(virtualSystem, inventoryType) + groupName;
//            addHostGroup(prefixedGroupName, inventoryType,
//                    inventory.get(groupName), agentInfo, gwosService);
            results.added++;
        }
        // TODO: better algorithm to determine changed
        results.modified = 0; //same.size();
        for (String groupName : deleted) {
            String prefixedGroupName = connectorFactory.makePrefix(virtualSystem, inventoryType) + groupName;
//            deleteHostGroup(prefixedGroupName, inventory.get(groupName), agentInfo, gwosService);
            results.deleted++;
        }
        return results;
    }

    /**
     * Given a map of hosts, where each host contains a map of owned VMs, merge the hosts and VMs into
     * a single map of SynchronizedResources, containing both hosts and VMs
     *
     * @param hostTree the map of hosts, each containing a map of VMs
     * @return a map of combined hosts and VMs used as input to synchronizer algorithm
     */
    protected Map<String, SynchronizedResource> mergeHostsAndVMs(Map<String, BaseHost> hostTree) {
        Map<String, SynchronizedResource> result = new ConcurrentHashMap<>();

        if (hostTree == null)
            return result;

        for (String hostkey : hostTree.keySet()) {
            BaseHost hosto = hostTree.get(hostkey);
            String hostname = hosto.getHostName();

            result.put(hostname,
                    new SynchronizedResource(
                            hostname,
                            SynchronizedResourceType.HOST,
                            hosto,
                            null));

            for (String vmkey : hosto.getVMPool().keySet()) {
                BaseVM vmo = hosto.getVM(vmkey);
                String vmname = vmo.getVMName();
                result.put(vmname,
                        new SynchronizedResource(
                                vmname,
                                SynchronizedResourceType.VM,
                                null,
                                vmo));
            }
        }
        return result;
    }

    private boolean isAlternativeVirtualSystem(VirtualSystem virtualSystem) {
        return (virtualSystem == VirtualSystem.VMWARE || virtualSystem == VirtualSystem.REDHAT);
    }

    public ConnectorFactory getConnectorFactory() {
        return this.connectorFactory;
    }

    public GwosServiceFactory getGwosServiceFactory() {
        return this.gwosServiceFactory;
    }

}
