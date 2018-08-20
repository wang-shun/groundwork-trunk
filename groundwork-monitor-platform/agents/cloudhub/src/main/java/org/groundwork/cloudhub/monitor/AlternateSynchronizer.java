package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.gwos.GWOSHost;
import org.groundwork.cloudhub.gwos.GWOSHostGroup;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dtaylor on 6/10/15.
 */
public class AlternateSynchronizer {

    private static Logger log = Logger.getLogger(AlternateSynchronizer.class);

    public AlternateSynchronizer() {
    }

    public MonitoringState synchronize(
            ConnectionConfiguration configuration,
            CloudhubAgentInfo agentInfo,
            MonitoringState monitoringState,
            DataCenterSyncResult syncResult,
            MonitorAgentSynchronizerService synchronizer) {

        if (log.isDebugEnabled()) {
            log.debug("Inside SyncMonitorAgentData Method for agent " + agentInfo.getName());
        }
        ConfigurationProvider provider = synchronizer.getConnectorFactory().getConfigurationProvider(configuration.getCommon().getVirtualSystem());

        // sent to add hypervisors
        List<BaseHost> comparedHostList = new ArrayList<BaseHost>();
        // sent to add VMs
        List<BaseVM> comparedVMList = new ArrayList<BaseVM>();
        List<String> listOfHypervisorsToAdd = new ArrayList<String>();
        List<String> listOfHypervisors = new ArrayList<String>();
        List<String> vmList = new ArrayList<String>();
        Map<String, String> prefixlessHostgroupMap = null;

        BaseHost baseHost = null;
        BaseVM baseVM = null;

        MonitorAgentSynchronizerService.SynchronizedResource vemaBaseObject = null;
        MonitorAgentSynchronizerService.SynchronizedResource hgVemaBaseObject = null;

        GWOSHostGroup hypervisorHostGroup = null;
        GWOSHostGroup deleteHostGroups = null;

        // retrieve all hosts (necessary for finding hosts not owned by the current agent, but still monitored)
        GwosService gwosService = synchronizer.getGwosServiceFactory().getGwosServicePrototype(configuration, agentInfo);
        Map<String, GWOSHost> groundworkHosts = syncResult.getGwosInventory().getAllHosts();
        Map<String, String> groundworkHostKeys = new HashMap<String, String>();
        for (String groundworkHostKey : groundworkHosts.keySet()) {
            groundworkHostKeys.put(configuration.makeHostKey(groundworkHostKey), groundworkHostKey);
        }
        Map<String, BaseHost> hosts = monitoringState.hosts();
        // merge all hosts and VMs into a single resource
        Map<String, MonitorAgentSynchronizerService.SynchronizedResource> hostsAndVms = synchronizer.mergeHostsAndVMs(hosts);

        synchronizeViews(syncResult, monitoringState, provider, hostsAndVms);

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
            vemaBaseObject = hostsAndVms.get(hostOrVm);

            String groundworkHostKey = groundworkHostKeys.get(configuration.makeHostKey(hostOrVm));
            GWOSHost gwosHost = ((groundworkHostKey != null) ? groundworkHosts.get(groundworkHostKey) : null);
            if (gwosHost == null) {
                // not found in Groundwork database, so add it
                if (log.isDebugEnabled())
                    log.debug("Host: '" + hostOrVm
                            + "' TYPE >>> '" + vemaBaseObject.getType()
                            + "' Enum Type Return; '"
                            + MonitorAgentSynchronizerService.SynchronizedResourceType.HOST + "'");

                if (vemaBaseObject.getType() == MonitorAgentSynchronizerService.SynchronizedResourceType.HOST) {
                    baseHost = vemaBaseObject.getHost();
                    baseHost.setGwosHostName(baseHost.getHostName());

                    if (log.isInfoEnabled())
                        log.info("Hypervisor '" + baseHost.getHostName()
                                + "' in CloudHub but not in GWOS. Will be added");

                    comparedHostList.add(baseHost);

                    if (log.isDebugEnabled())
                        log.debug("Temp Host List size: [" + comparedHostList.size() + "]");

                } else if (vemaBaseObject.getType() == MonitorAgentSynchronizerService.SynchronizedResourceType.VM) {
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
                if (vemaBaseObject.getType() == MonitorAgentSynchronizerService.SynchronizedResourceType.HOST) {
                    baseHost = vemaBaseObject.getHost();
                    baseHost.setGwosHostName(gwosHost.getHostName());
                } else if (vemaBaseObject.getType() == MonitorAgentSynchronizerService.SynchronizedResourceType.VM) {
                    baseVM = vemaBaseObject.getVM();
                    baseVM.setGwosHostName(gwosHost.getHostName());
                } else {
                    // should NEVER get here.
                    log.error("Unknown host/vm type: '" + vemaBaseObject.getType() + "'");
                }
            }
            //hostsOrVmsList.add(hostOrVm);
        }

        // Delete orphaned GWOS Hypervisor and Vm Hosts
        deleteHostsAndVMs(gwosService, groundworkHosts, hostsAndVms, agentInfo.getAgentId(), configuration);

        // Add new GWOS Hypervisor Hosts
        if (comparedHostList.size() > 0)  // i.e. if there are new hosts to tell GWOS about
        {
            if (log.isInfoEnabled())
                log.info("Adding [" + comparedHostList.size() + "]" + " Hypervisors to the gwosService");
            comparedHostList = synchronizer.filterHypervisors(comparedHostList);
            gwosService.addHypervisors(comparedHostList, agentInfo.getName());
        }

        // Add new GWOS Vm Hosts
        if (comparedVMList.size() > 0)  // i.e. if there are new VMs to tell GWOS about
        {
            if (log.isInfoEnabled())
                log.info("Adding [" + comparedVMList.size() + "]" + " Virtual Machines to the gwosService");
            gwosService.addVirtualMachines(comparedVMList, agentInfo.getName());
        }
        // End Compare gwosHostList and vemaHostList

        // call GWOS getHotGroup Webservice.
        List<String> gwosHostGroupList = gwosService.getHostGroupNames();
        if (log.isDebugEnabled())
            log.debug("Total Number of HostGroups from GwosHostGroup Service: '" + gwosHostGroupList.size() + "'");

		/* Returns a list of Hostgroup names without the prefix */
        prefixlessHostgroupMap = synchronizer.stripHostGroupList(gwosHostGroupList);

        // Clear lists before it gets used
        listOfHypervisors.clear();
        listOfHypervisorsToAdd.clear();

        for (String hostKey : hosts.keySet()) {
            hgVemaBaseObject = hostsAndVms.get(hostKey);
            //String prefixHostGroup = prefixlessHostgroupMap.get(hostKey);
            if (!hostKey.equalsIgnoreCase(ConnectorConstants.HOSTLESS_VMS) && provider.isValidManagementServerHostGroup(hostKey)) {
                if (hgVemaBaseObject != null) {
                    listOfHypervisors.add(hgVemaBaseObject.getHost().getGwosHostName());
                }
            }

            if (hgVemaBaseObject != null) {
                if (!prefixlessHostgroupMap.containsKey(hgVemaBaseObject.getHost().getHostName())) {
                    if (log.isDebugEnabled())
                        log.debug(hgVemaBaseObject.getHost().getHostName() + " not in prefixlessHostGroups: " + prefixlessHostgroupMap.keySet().toString());
                    GWOSHostGroup hostGroup = new GWOSHostGroup(
                            gwosService.buildHostGroupName(
                                    agentInfo,
                                    ConnectorConstants.ENTITY_HYPERVISOR,
                                    hgVemaBaseObject.getHost().getHostName()),
                            agentInfo.getHyperVisorName(),
                            agentInfo.getConnectorName(),
                            agentInfo.getApplicationType()
                    );

                    listOfHypervisorsToAdd.add(hgVemaBaseObject.getHost().getHostName());

                    gwosService.addHostGroup(hostGroup);
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("Host Groups are added to GWOS Server #["
                    + listOfHypervisorsToAdd.size() + "]");

        // add management server to hostgroup
        String hostName = "";


        hostName = configuration.getConnection().getHostName();

        String hostGroupName = gwosService.buildHostGroupName(
                agentInfo,
                ConnectorConstants.ENTITY_MGMT_SERVER,
                hostName);


        GWOSHostGroup gwosHostGroup = new GWOSHostGroup(hostGroupName, agentInfo.getManagementServerName(),
                agentInfo.getConnectorName(), agentInfo.getApplicationType());

        if (!(gwosHostGroupList.contains(hostGroupName))) {
            log.debug("ADDING ManagementServer '" + hostGroupName + "' to HostGroup");
            gwosService.addHostGroup(gwosHostGroup);
        }

        log.debug("Modifying MgmtServer '" + hostGroupName
                + "' to HostGroup with HypervisorList of '"
                + listOfHypervisors.size() + "' elements");

        gwosService.modifyHostGroup(gwosHostGroup, listOfHypervisors);

        // Delete hostGroupList not present in hypervisorList
        log.debug("Check which HostGroups no longer present in GWOS system (for deletion)");

        for (Map.Entry<String, String> hostGroupEntry : prefixlessHostgroupMap.entrySet()) {
            String gwosHostGroupName = hostGroupEntry.getValue();
            String hostKey = hostGroupEntry.getKey();
            if (gwosHostGroupName.startsWith(ConnectorConstants.PREFIX_STORAGE) || gwosHostGroupName.startsWith(ConnectorConstants.PREFIX_NETWORK) || gwosHostGroupName.startsWith(ConnectorConstants.PREFIX_POOL)) {
                hostKey = gwosHostGroupName.replaceFirst(":", "-");
            }
            if (!(hostsAndVms.containsKey(hostKey))) {

                gwosService.deleteHostGroup(new GWOSHostGroup(
                        hostKey,
                        agentInfo.getHyperVisorName(),
                        agentInfo.getConnectorName(),
                        agentInfo.getApplicationType()));
            }
        }

        // ModifyHostGroup for Hypervisor List
        // Iterate through the vemaHostList and get the VM's for each Host.
        BaseHost vbHost = null;
        BaseVM vmName = null;

        for (BaseHost hypervisorHostList : hosts.values()) {

            vbHost = hosts.get(hypervisorHostList.getHostName());
            for (String vms : vbHost.getVMPool().keySet()) {
                vmName = vbHost.getVM(vms);
                if (vmName != null)
                    vmList.add(vmName.getGwosHostName());
                else
                    log.error("Alert: vmname is (null)");
            }

            // Create the HostGroups for VMs
            String checkHostName = hypervisorHostList.getHostName();
            String hgName = prefixlessHostgroupMap.get(checkHostName);
            if (hgName == null) {
                hgName = gwosService.buildHostGroupName(agentInfo,
                        ConnectorConstants.ENTITY_HYPERVISOR,
                        checkHostName);
            }
            hypervisorHostGroup = new GWOSHostGroup(
                    hgName,
                    agentInfo.getHyperVisorName(),
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

    /**
     * Synchronize the views's VM collections for special views only
     *
     * @param syncResult
     * @param monitoringState modified by this method
     * @return
     */
    protected int synchronizeViews(DataCenterSyncResult syncResult, MonitoringState monitoringState,
                                   ConfigurationProvider provider, Map<String, MonitorAgentSynchronizerService.SynchronizedResource> hostsAndVms) {
        int count = 0;
        if (syncResult.getMonitoringInventory().getOptions().isViewDatastores()) {
            String prefix = provider.getPrefix(ConfigurationProvider.PrefixType.VmStorage);
            count += processSynchronizeViews(syncResult.getMonitoringInventory().getDatastores(), monitoringState.hosts(), hostsAndVms, prefix);
        }
        if (syncResult.getMonitoringInventory().getOptions().isViewNetworks()) {
            String prefix = provider.getPrefix(ConfigurationProvider.PrefixType.VmNetwork);
            count += processSynchronizeViews(syncResult.getMonitoringInventory().getNetworks(), monitoringState.hosts(), hostsAndVms, prefix);
        }
        if (syncResult.getMonitoringInventory().getOptions().isViewResourcePools()) {
            String prefix = provider.getPrefix(ConfigurationProvider.PrefixType.ResourcePool);
            count += processSynchronizeViews(syncResult.getMonitoringInventory().getResourcePools(), monitoringState.hosts(), hostsAndVms, prefix);
        }
        return count;
    }



    protected int processSynchronizeViews(Map<String, InventoryContainerNode> syncView, Map<String, BaseHost> hosts, Map<String, MonitorAgentSynchronizerService.SynchronizedResource> hostsAndVms, String view) {
        int count = 0;
        for (Map.Entry<String, InventoryContainerNode> entry : syncView.entrySet()) {
            BaseHost host = hosts.get(view + entry.getKey());
            if (host != null) {
                InventoryContainerNode node = entry.getValue();
                if (node != null) {
                    for (VirtualMachineNode vm : node.getVms().values()) {
                        MonitorAgentSynchronizerService.SynchronizedResource sync = hostsAndVms.get(vm.getName());
                        if (sync != null && sync.getVM() != null) {
                            host.putVM(vm.getName(), sync.getVM());
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private void deleteHostsAndVMs(GwosService gwosService, Map<String, GWOSHost> gwosHosts,
                                   Map<String, MonitorAgentSynchronizerService.SynchronizedResource> hostsOrVms,
                                   String agentId, ConnectionConfiguration configuration) {
        BaseHost deleteHost = null;
        List<BaseHost> deleteHostList = new ArrayList<>();

        Set<String> hostsOrVmsKeys = new HashSet<String>();
        for (String hostOrVmKey : hostsOrVms.keySet()) {
            hostsOrVmsKeys.add(configuration.makeHostKey(hostOrVmKey));
        }
        for (GWOSHost gwosHost : gwosHosts.values()) {
            if (gwosHost.getAgentId() != null && gwosHost.getAgentId().equals(agentId)) {
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
            gwosService.deleteHypervisors(deleteHostList, agentId);
        }
    }

}
