package org.groundwork.cloudhub.monitor;

import com.groundwork.collage.model.AuditLog;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.MonitorAgentResult;
import org.groundwork.agents.monitor.MonitorTimer;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.SupportsExtendedViews;
import org.groundwork.cloudhub.connectors.CollectionMode;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.RateExceededException;
import org.groundwork.cloudhub.gwos.GWOSHost;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.gwos.GwosServiceFactory;
import org.groundwork.cloudhub.gwos.messages.RateExceededStatusMessages;
import org.groundwork.cloudhub.gwos.messages.RetriesExhaustedStatusMessages;
import org.groundwork.cloudhub.gwos.messages.SuspendedStatusMessages;
import org.groundwork.cloudhub.gwos.messages.UnreachableStatusMessages;
import org.groundwork.cloudhub.gwos.messages.UpdateStatusMessages;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.profile.ProfileConversion;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.cloudhub.statistics.MonitoringStatistics;
import org.groundwork.cloudhub.statistics.MonitoringStatisticsService;
import org.groundwork.rs.client.CollageRestException;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.profiles.HubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service(CloudhubMonitorAgentClient.NAME)
@Scope("prototype")
public class CloudhubMonitorAgentClient extends AbstractCloudhubMonitorAgentClient implements CloudhubMonitorAgent {

    public final static String NAME = "CloudhubMonitorAgentClient";

    private static Logger log = Logger.getLogger(CloudhubMonitorAgentClient.class);

    @Resource(name = ConfigurationService.NAME)
    private ConfigurationService configurationService;
    @Resource(name = ProfileService.NAME)
    private ProfileService profileService;
    @Resource(name = MonitorAgentSynchronizer.NAME)
    private MonitorAgentSynchronizer synchronizer;
    @Resource(name = GwosServiceFactory.NAME)
    private GwosServiceFactory gwosServiceFactory;
    @Resource(name="ConnectorMonitor")
    private ConnectorMonitor connectorMonitor;

    // optimize creation of so many gwos Services
    private GwosService gwosServiceInstance;

    @Resource(name = ConnectorFactory.NAME)
    private ConnectorFactory connectorFactory;
    @Resource(name = MonitorAgentCollector.NAME)
    private MonitorAgentCollector collector;
    @Resource(name = MonitoringStatisticsService.NAME)
    private MonitoringStatisticsService statisticsService;
    @Resource
    private ServiceSynchronizer serviceSynchronizer;

    private MonitoringConnector monitoringConnector;
    private ManagementConnector managementConnector;
    private ConfigurationProvider configurationProvider;
    MonitorTimer syncTimer = null;
    MonitorTimer monitorTimer = null;
    MonitorTimer comaTimer = null;

    private List<BaseQuery> hypervisorMetrics = new ArrayList<BaseQuery>();
    private List<BaseQuery> vmMetrics = new ArrayList<BaseQuery>();
    private List<BaseQuery> customMetrics = new ArrayList<BaseQuery>();

    private volatile boolean bFirstTimeSync = true;
    private volatile boolean bForceMonitorAfterSync = false;
    private int monitorExceptionCount = 0;
    private int gwosExceptionCount = 0;

    public enum ThreadInterruptBehavior {
        Break,
        Continue,
        Normal
    }

    public CloudhubMonitorAgentClient(ConnectionConfiguration configuration, CloudhubAgentInfo agentInfo) {
        super(configuration, agentInfo);
        if (configuration != null)
            resetTimersFromConfig(configuration);
        else
            resetTimersFromDefaults(configuration);
    }

    public ConnectionState getConnectionState() {
        if (managementConnector != null) {
            return managementConnector.getConnectionState();
        }
        return ConnectionState.DISCONNECTED;
    }

    public Integer getGroundworkExceptionCount() {
        return this.gwosExceptionCount;
    }

    public Integer getMonitorExceptionCount() {
        return this.monitorExceptionCount;
    }

    public void unsuspend() {
        super.unsuspend();
        monitorExceptionCount = 0;
        gwosExceptionCount = 0;
        // @since 7.1.0: force timers to reset to get immediate start of monitoring
        monitorTimer.resetAndTrigger();
        syncTimer.resetAndTrigger();
    }

    public MonitorAgentResult call() throws Exception {
        long start = System.currentTimeMillis();
        log.info("Cloudhub Monitoring thread started for configuration: " + agentInfo.getName());
        monitorState.setRunning(true);
        agentInfo.clearErrors();
        MonitoringState virtualMonitoredHosts = null;
        monitorExceptionCount = 0;
        gwosExceptionCount = 0;
        try {
            while (monitorState.isRunning() && !monitorState.isForceShutdown()) {
                try {
                    try {
                        Thread.sleep(agentInfo.getMsAgentSleep());
                    } catch (InterruptedException ie) {
                        monitorState.setRunning(false);
                        String message = String.format("Agent %s interrupted. Error: %s ", agentInfo.getName(), ie.toString());
                        log.error(message);
                        agentInfo.addError(message);
                        collector.remove(agentInfo.getName());
                        return new MonitorAgentResult(this, false, System.currentTimeMillis() - start, false);
                    }

                    ThreadInterruptBehavior behavior = checkForInterrupts(virtualMonitoredHosts);
                    if (log.isDebugEnabled()) {
                        log.debug("Heartbeat of CloudHub Agent: \n"
                                + "\tagent name                           = '" + agentInfo.getName() + "'\n"
                                + "\tthread-behavior                      = '" + behavior + "'\n"
                                + "\tSync                             = '" + syncTimer.secondsToGo() + "'\n"
                                + "\tComa                             = '" + comaTimer.secondsToGo() + "'\n"
                                + "\tMonitor                          = '" + monitorTimer.secondsToGo() + "'\n"
                                // + "\n\tvGwosConfig Object inside RUN Method = '" + configuration.getGwos().getGwosServer() + "'\n"
                                + "\tGet Connection State                 = '" + monitoringConnector.getConnectionState() + "'\n" + ""
                        );
                    }

                    if (behavior == ThreadInterruptBehavior.Break)
                        break;
                    if (behavior == ThreadInterruptBehavior.Continue)
                        continue;

                    boolean configurationUpdated = monitorState.isConfigurationUpdated();
                    if (configurationUpdated) {
                        log.info(String.format("Refreshing configuration for agent %s", agentInfo.getName()));
                        configuration = configurationService.readConfiguration(agentInfo.getConfigurationPath());
                        if (this.gwosServiceInstance != null) {
                            gwosServiceInstance.setConnection(configuration);
                            gwosServiceInstance.setAgentInfo(agentInfo);
                        }
                        CollectionMode mode = createCollectionModes(configuration);
                        monitoringConnector.setCollectionMode(mode);
                        managementConnector.setCollectionMode(mode);
                        monitoringConnector.disconnect();
                        managementConnector.closeConnection();
                        resetTimersFromConfig(configuration);
                        // reset retries with new configuration
                        agentInfo.setConnectionRetries(configuration.getCommon().getConnectionRetries());
                        monitorExceptionCount = 0;
                        gwosExceptionCount = 0;
                        monitorState.setConfigurationUpdated(false);
                        getGwosService().authenticate(configuration); // reset connection info
                    }

                    if (monitorTimer.isReady() && monitoringConnector.getConnectionState() != ConnectionState.CONNECTED) {
                        try {
                            connectAndReset();
                        } catch (Exception e) {
                            handleCollectorException(e, virtualMonitoredHosts);
                            virtualMonitoredHosts = null;
                            continue;
                        }
                    }

                    behavior = checkForInterrupts(virtualMonitoredHosts);
                    if (behavior == ThreadInterruptBehavior.Break)
                        break;
                    if (behavior == ThreadInterruptBehavior.Continue)
                        continue;

                    // read profile and reset metrics using new connectors
                    // if configuration updated
                    if (configurationUpdated) {
                        readMetrics();
                        behavior = checkForInterrupts(virtualMonitoredHosts);
                        if (behavior == ThreadInterruptBehavior.Break)
                            break;
                        if (behavior == ThreadInterruptBehavior.Continue)
                            continue;
                    }

                    if (monitorTimer.isReadyAndReset()) {
                        if (monitoringConnector.getConnectionState() != ConnectionState.CONNECTED) {
                            connectAndReset();
                        }
                        long startTime = System.currentTimeMillis();
                        if (log.isInfoEnabled())
                            log.info("Cloudhub Start the Monitor Process for agent " + agentInfo.getName());

                        virtualMonitoredHosts = collect(virtualMonitoredHosts);
                        virtualMonitoredHosts = filter(virtualMonitoredHosts);

                        DataCenterSyncResult syncResult = synchronizeInventory();
                        virtualMonitoredHosts = synchronize(virtualMonitoredHosts, syncResult);

                        updateMonitor(virtualMonitoredHosts, syncResult);

                        // added 7.2.0 for detecting and syncing deleted metrics
                        if (serviceSynchronizer.isEnabled(configurationProvider)) {
                            serviceSynchronizer.sync(getGwosService(), virtualMonitoredHosts);
                        }

                        long timeToRunMonitor = (System.currentTimeMillis() - startTime);
                        if (log.isInfoEnabled()) {
                            log.info("Time to execute monitor operation ["
                                    + timeToRunMonitor
                                    + "] ms for agent " + agentInfo.getName());
                        }
                        monitorTimer.reset();
                        comaTimer.reset();
                        gwosExceptionCount =  monitorExceptionCount = 0; // Reset counts on success
                    }

                    behavior = checkForInterrupts(virtualMonitoredHosts);
                    if (behavior == ThreadInterruptBehavior.Break)
                        break;
                    if (behavior == ThreadInterruptBehavior.Continue)
                        continue;
                    
                }
                catch (Throwable e) {
                    // handle GWOS exceptions specially. Note this reuses the agentInfo retry threshold for connectors
                    if (e instanceof  CollageRestException || e instanceof CloudHubException) {
                        gwosExceptionCount++;
                        String message = "Agent " + agentInfo.getName() + ", GWOS error: (counts:"
                                + monitorExceptionCount + "," + gwosExceptionCount + ") :" + e.getMessage();
                        log.error(message, e);
                        agentInfo.addError(message);
                        if (agentInfo.getConnectionRetries() > -1 && gwosExceptionCount >= agentInfo.getConnectionRetries()) {
                            log.error("GWOS Exception count exceeded. Suspending Monitor Agent " + agentInfo.getName());
                            monitorState.setSuspended(true);
                            gwosExceptionCount = 0;
                        }
                    }
                    else { // Collector Exception, special processing
                        handleCollectorException(e, virtualMonitoredHosts);
                        virtualMonitoredHosts = null;
                    }
                }
            }
        }
        catch (Throwable t) {
            log.error("System Error in Cloudhub Thread: " + agentInfo.getName());
            log.error(t);
        }
        log.info("Cloudhub exiting agent " + agentInfo.getName());
        collector.remove(agentInfo.getName());
        return new MonitorAgentResult(this, true, System.currentTimeMillis() - start, false);
    }

    protected void connectAndReset() {
        monitoringConnector.connect(configuration.getConnection());
        managementConnector.openConnection(configuration.getConnection());
        CollectionMode mode = createCollectionModes(configuration);
        monitoringConnector.setCollectionMode(mode);
        managementConnector.setCollectionMode(mode);
    }

    /**
     * Handle ConnectorExceptions and derived exceptions. Handles special cases of exceeding the connector API usage limits,
     * as well as handling the connector's exception count threshold. Once the threshold is exceeded, the connector is
     * suspended. The main part of this handler is calling into GWOS to update the status of all hosts and services
     *
     * @see GwosService#updateAllHypervisorsStatus(CloudhubAgentInfo, String, String, UpdateStatusMessages)
     * @see org.groundwork.cloudhub.exceptions.ConnectorException
     * @since 7.2.0 consolidating all handling of collection exceptions
     * @param e the exception to be handled
     */
    private void handleCollectorException(Throwable e, MonitoringState monitoringState) {

        monitorExceptionCount++;
        boolean isThresholdExceeded = (agentInfo.getConnectionRetries() > -1 && monitorExceptionCount >= agentInfo.getConnectionRetries());
        boolean isRateExceeded = (e instanceof RateExceededException);

        // logging
        StringBuffer message = new StringBuffer();
        message.append("Agent ").append(agentInfo.getName()).append(", Connection error: ").append(e.getMessage());
        message.append(", (counts: ").append(monitorExceptionCount).append(",").append(gwosExceptionCount).append(")");
        if (isThresholdExceeded) message.append("[Error Threshold Count Exceeded. Suspending]");
        log.error(message.toString(), e);
        agentInfo.addError(message.toString());

        // build status and last plugin output messages
        UpdateStatusMessages messages;
        if (isThresholdExceeded) {
            messages = new RetriesExhaustedStatusMessages();
        }
        else if (isRateExceeded) {
            messages = new RateExceededStatusMessages();
        }
        else {
            messages = new UnreachableStatusMessages();
        }

        // update all hosts and services
        try {
            getGwosService().updateAllHypervisorsStatus(agentInfo, MonitorStatusBubbleUp.UNREACHABLE,
                    MonitorStatusBubbleUp.UNKNOWN, messages);
            ConnectorMonitorState state = (monitoringState == null) ? new ConnectorMonitorState() : monitoringState.getConnectorMonitorState();
            connectorMonitor.updateGroundworkConnector(getGwosService(), monitorExceptionCount, state);
        }
        catch (Exception updateException) {
            log.error("Exception handling Management server down and updating status for all Hypervisors for Agent " + agentInfo.getName(), updateException);
        }

        // suspend only if threshold exceeded
        if (isThresholdExceeded) {
            monitorExceptionCount = 0;
            monitorState.setSuspended(true);
            log.error("Suspending Monitor Agent " + agentInfo.getName());
        }
        // ensure that we restart the wait sequence
        monitorTimer.reset();
    }

    private ThreadInterruptBehavior checkForInterrupts(MonitoringState virtualMonitoredHosts) {
        if (monitorState.isForceDelete()) {
            log.info("Cloudhub Commencing with deletion of all agent monitor information ... "
                    + agentInfo.getName() + ", " + agentInfo.getAgentId());
            monitorState.setRunning(false);
            monitorState.setSuspended(true);
            GwosService gwosService = getGwosService();

            try {
                int count = configurationService.countByHostName(configuration.getGwos().getGwosServer());
                DtoOperationResults results = gwosService.deleteByAgent(configuration, count);
                log.info(String.format("Deletion of agent data complete for agent %s : success: %d failure %d ",
                        agentInfo.getName(), results.getSuccessful(), results.getFailed()));
                // delete any unified services that exist under different hosts
                gwosService.deleteServices(configurationProvider.createDeleteServiceList(extractMetricNames(hypervisorMetrics)),
                        agentInfo.getApplicationType(),
                        ProfileConversion.convertVirtualSystemToMetricType(configuration.getCommon().getVirtualSystem(), true),
                        configuration.getCommon().getAgentId());
                gwosService.deleteServices(configurationProvider.createDeleteServiceList(extractMetricNames(vmMetrics)),
                        agentInfo.getApplicationType(),
                        ProfileConversion.convertVirtualSystemToMetricType(configuration.getCommon().getVirtualSystem(), false),
                        configuration.getCommon().getAgentId());
                gwosService.deleteServices(configurationProvider.createDeleteServiceList(extractMetricNames(customMetrics)),
                        agentInfo.getApplicationType(),
                        ProfileConversion.convertVirtualSystemToMetricType(configuration.getCommon().getVirtualSystem(), false),
                        configuration.getCommon().getAgentId());
            }
            catch (Exception e) {
                String message = "Agent: " + agentInfo.getName() + " - Failure occurred deleting agent monitoring data";
                log.error(message, e);
                gwosExceptionCount++;
                agentInfo.addError(message);
            }
            collector.remove(agentInfo.getName());
            return ThreadInterruptBehavior.Break;
        }

        if (monitorState.isForceRename()) {
            log.info("Cloudhub Commencing with rename of all agent monitor information ... "
                            + agentInfo.getName() + ", " + agentInfo.getAgentId());
            try {
                DtoOperationResults results = getGwosService().renamePrefixByAgent(monitorState.getRenameAgentId(),
                        monitorState.getRenameOldPrefix(), monitorState.getRenameNewPrefix());
                log.info(String.format("Rename of agent data complete for agent %s : success: %d failure %d ",
                        agentInfo.getName(), results.getSuccessful(), results.getFailed()));
            }
            catch (Exception e) {
                String message = "Agent: " + agentInfo.getName() + " - Failure occurred renaming agent monitoring data";
                log.error(message, e);
                gwosExceptionCount++;
                agentInfo.addError(message);
            }
            monitorState.completeRename();
            return ThreadInterruptBehavior.Continue;
        }

        if (monitorState.isForceDeleteView()) {
            return deleteViewData(virtualMonitoredHosts);
        }

        if (monitorState.isForceDeleteServices()) {
            return deleteServicesData(virtualMonitoredHosts);
        }

        if (monitorState.getForceDeleteConnectorHost()) {
            return deleteConnectorHost();
        }

        if (monitorState.isForceShutdown()) {
            log.info("Cloudhub Commencing with forced shutdown of agent monitor for " + agentInfo.getName());
            monitorState.setRunning(false);
            return ThreadInterruptBehavior.Break;
        }

        if (monitorState.isForceSuspend()) {
            if (log.isDebugEnabled()) {
                log.debug("Monitoring FORCE suspended for agent monitor: " + agentInfo.getName());
            }
            // @since 7.1.1 - force close of connection on stop - needs testing on all connectors
            if (this.monitoringConnector.getConnectionState() == ConnectionState.CONNECTED) {
                try {
                    disconnect();
                }
                catch (Exception disconnectException) {
                    log.error("Exception occurred while disconnectiong virtualization connection from Force Suspend", disconnectException);
                }
            }
            // @since 7.1.0 - Update status when suspending - CLOUDHUB-236
            try {
                getGwosService().updateAllHypervisorsStatus(agentInfo, MonitorStatusBubbleUp.UNREACHABLE, MonitorStatusBubbleUp.UNKNOWN, new SuspendedStatusMessages());
                ConnectorMonitorState state = (virtualMonitoredHosts == null) ? new ConnectorMonitorState() : virtualMonitoredHosts.getConnectorMonitorState();
                connectorMonitor.updateGroundworkConnector(getGwosService(), ConnectorMonitorState.FORCE_MONITOR_SHUTDOWN, state);
            }
            catch (Exception e) {
                log.error("Exception occurred updating Hypervisor status", e);
            }
            monitorState.setForceSuspend(false);
            return ThreadInterruptBehavior.Continue;
        }

        if (monitorState.isSuspended()) {
            if (log.isDebugEnabled()) {
                log.debug("Monitoring suspended for agent monitor: " + agentInfo.getName());
            }
            return ThreadInterruptBehavior.Continue;
        }

        return ThreadInterruptBehavior.Normal;
    }

    public void monitor() {
        MonitoringState virtualMonitoredHosts = null;
        virtualMonitoredHosts = monitoringConnector.collectMetrics(virtualMonitoredHosts, hypervisorMetrics, vmMetrics, customMetrics);
        updateMonitor(virtualMonitoredHosts, null);
    }

    /**
     * Collect Metrics from Virtualization Server
     *
     * @param monitoringState
     * @return
     */
    public MonitoringState collect(MonitoringState monitoringState) {
        if (hypervisorMetrics.isEmpty() || vmMetrics.isEmpty()) {
            readMetrics();
        }
        return monitoringConnector.collectMetrics(monitoringState, hypervisorMetrics, vmMetrics, customMetrics);
    }

    /**
     * Filter the metric collection, removing unwanted host or VMs
     *
     * @param monitoringState the monitoring state collection minus filtered hosts and vms
     * @return
     */
    public MonitoringState filter(MonitoringState monitoringState) {

        GwosService gwosService = getGwosService();
        if (gwosService.isFeatureEnabled(GwosService.GroundworkFeature.BlackListFilter)) {
            MonitoringState newState = new MonitoringState();
            for (Map.Entry<String, BaseHost> entry : monitoringState.hosts().entrySet()) {
                if (!gwosService.isHostNameBlackListed(entry.getKey())) {
                    BaseHost hypervisor = entry.getValue();
                    ConcurrentHashMap<String, BaseVM> newVms = new ConcurrentHashMap<>();
                    for (Map.Entry<String, BaseVM> vmEntry : hypervisor.getVMPool().entrySet()) {
                        if (!gwosService.isHostNameBlackListed(vmEntry.getKey())) {
                            newVms.put(vmEntry.getKey(), vmEntry.getValue());
                        } else {
                            if (log.isInfoEnabled()) {
                                log.info("VM black listed: " + vmEntry.getKey());
                            }
                        }
                    }
                    hypervisor.clearVM();
                    for (Map.Entry<String, BaseVM> vm : newVms.entrySet()) {
                        hypervisor.putVM(vm.getKey(), vm.getValue());
                    }
                    newState.hosts().put(entry.getKey(), hypervisor);
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("Host black listed: " + entry.getKey());
                    }
                }
            }
            return newState;
        }
        return monitoringState;
    }

    /**
     * Perform monitoring updating on GWOS backend
     *
     * @param monitoringState
     * @param syncResult
     */
    public void updateMonitor(MonitoringState monitoringState, DataCenterSyncResult syncResult) {
        long startTime = System.currentTimeMillis();
        Map<String, BaseHost> hosts = monitoringState.hosts();
        GwosService gwosService = getGwosService();
        String hostGroupName = null;
        
        String hostName = configuration.getConnection().getServer();
        String mgmtServer = gwosService.buildHostGroupName(
                agentInfo,
                ConnectorConstants.ENTITY_MGMT_SERVER,
                hostName);

        if (log.isDebugEnabled())
            log.debug("Monitor Process Started for " + agentInfo.getName());

        List<BaseHost> listOfHypervisors = new ArrayList<BaseHost>();
        List<BaseVM> listOfVM = new ArrayList<BaseVM>();
        Set<String> uniqueVMs = new HashSet<>();

        if (log.isDebugEnabled()) {
            log.debug("Host List Size: "
                    + (hosts == null ? "null.size() = 0" : hosts.size()));
        }

        if (hosts == null)
            return; // there's nothing to do - hostsList is null.

        Map<String, String> prefixlessHostgroupMap = MonitorAgentSynchronizerService.stripHostGroupList(gwosService.getHostGroupNames());
        for (BaseHost host : hosts.values()) {
            log.debug("Hypervisor to update: '" + host.getHostName() + "'");

            host.setHostGroup(mgmtServer);
            listOfHypervisors.add(host);

            Map<String, GWOSHost> groundworkHosts =
                    ((syncResult != null) ? syncResult.getGwosInventory().getAllHosts() : Collections.EMPTY_MAP);
            Map<String, String> groundworkHostKeys = new HashMap<String, String>();
            for (String groundworkHostKey : groundworkHosts.keySet()) {
                groundworkHostKeys.put(configuration.makeHostKey(groundworkHostKey), groundworkHostKey);
            }

            String groundworkHostKey = groundworkHostKeys.get(configuration.makeHostKey(host.getHostName()));
            GWOSHost gwosHost = ((groundworkHostKey != null) ? groundworkHosts.get(groundworkHostKey) : null);
            if (gwosHost != null && gwosHost.getAgentId() != null) {
                host.setOwnedByAgent(gwosHost.getAgentId().equals(agentInfo.getAgentId()));
                host.setGwosHostName(gwosHost.getHostName());
            }
            hostGroupName = prefixlessHostgroupMap.get(host.getHostName());
            if (hostGroupName == null) {
                hostGroupName = gwosService.buildHostGroupName(
                        agentInfo,
                        ConnectorConstants.ENTITY_HYPERVISOR, host.getHostName());
            }

            for (BaseVM vm : host.getVMPool().values()) {
                vm.setHostGroup(hostGroupName);
                String groundworkVMKey = groundworkHostKeys.get(configuration.makeHostKey(vm.getVMName()));
                GWOSHost gwosVM = ((groundworkVMKey != null) ? groundworkHosts.get(groundworkVMKey) : null);
                if (gwosVM != null && gwosVM.getAgentId() != null) {
                    vm.setOwnedByAgent(gwosVM.getAgentId().equals(agentInfo.getAgentId()));
                    vm.setGwosHostName(gwosVM.getHostName());
                }
                if (!uniqueVMs.contains(vm.getVMName())) {
                    uniqueVMs.add(vm.getVMName());
                    listOfVM.add(vm);
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("Call Modify Hypervisors");
        listOfHypervisors = synchronizer.filterHypervisors(listOfHypervisors);
        Map<String,String> hypervisorRunStates = new HashMap<String,String>();
        gwosService.modifyHypervisors(listOfHypervisors, agentInfo.getName(), hypervisorRunStates, false);

        if (log.isDebugEnabled()) log.debug("Call Modify VirtualMachines");
        gwosService.modifyVirtualMachines(listOfVM, agentInfo.getName(), hypervisorRunStates);
        long timeToRunMonitor = System.currentTimeMillis() - startTime;
        if (log.isDebugEnabled()) {
            log.debug("Total number of Hypervisors that will be updated: '" + listOfHypervisors.size() + "'");
            log.debug("Total number of VM's        that will be updated: '" + listOfVM.size() + "'");
            log.debug("Monitor Process Ended for " + agentInfo.getName());
        }

        if (monitoringState.events().size() > 0) {
            if (log.isInfoEnabled()) log.info("Sending monitoring state events (" + monitoringState.events().size() + ")...");
            gwosService.sendMonitoringFaults(monitoringState.events(), agentInfo.getApplicationType());
            if (log.isInfoEnabled()) log.info("...end sending monitoring state events");
        }

        // update CloudHub Connector state, reset successful state
        connectorMonitor.updateGroundworkConnector(getGwosService(), 0, monitoringState.getConnectorMonitorState());

        if (statisticsService.isEnabled()) {
            MonitoringStatistics stats = getMonitorStatistics(agentInfo.getName());
            stats.getExecutionTimes().setMonitorUpdate(timeToRunMonitor);
        }

    }

    /**
     * Synchronize with Groundwork backend.
     * Finds all inventory: hypervisors and VMs and compares to hypervisors and vms coming "back" from GroundWork.
     * Creates the update instructions to cause hostgroup changes in Groundwork.
     *
     * @return the new set of monitored state for the current run cycle
     */
    public MonitoringState synchronize(MonitoringState monitoringState, DataCenterSyncResult syncResult) {
        if (log.isInfoEnabled())
            log.info("Gathering Hosts+VMs for agent " + getAgentInfo().getName());

        if (hypervisorMetrics.isEmpty() || vmMetrics.isEmpty()) {
            readMetrics();
        }

        if (monitoringState == null) {
            monitoringState = new MonitoringState();
        }

        if (log.isDebugEnabled())
            log.debug("Number of hosts discovered by CloudHub: '" + monitoringState.hosts().size() + "'");

        long startTime = System.currentTimeMillis();
        synchronizer.synchronize(configuration, agentInfo, monitoringState, syncResult);
        long timeToExecuteMonitorSync = (System.currentTimeMillis() - startTime);
        if (log.isInfoEnabled()) {
            log.info("Time to execute sync operation ["
                    + timeToExecuteMonitorSync
                    + "] ms  (hosts & VMs: " + monitoringState.hosts().size() + ") for agent " + agentInfo.getName());
        }
        if (statisticsService.isEnabled()) {
            MonitoringStatistics stats = getMonitorStatistics(agentInfo.getName());
            stats.getExecutionTimes().setMonitorSync(timeToExecuteMonitorSync);
        }

        /*
         * Make sure that monitor is started after the 2nd sync
         * to update any PENDING state
         */
        if (this.bForceMonitorAfterSync) {
            if (log.isDebugEnabled())
                log.debug("Triggering monitoring in 2nd sync to update PENDING metrics");
            monitorTimer.resetAndTrigger();
            this.bForceMonitorAfterSync = false;
        }

        /*
         * First sync completed. Set flag to force monitor after
         * the next sync operation
         */
        if (this.bFirstTimeSync) {
            this.bFirstTimeSync = false;
            this.bForceMonitorAfterSync = true;
        }
        /* Reset timer to current time */
        syncTimer.reset();
        comaTimer.reset();
        return monitoringState;
    }

    private void resetMetrics(ProfileMetrics profileMetrics) {
        hypervisorMetrics.clear();
        if (profileMetrics == null)
            return;
        for (Metric hypervisorMetric : profileMetrics.getPrimary()) {
            hypervisorMetrics.add(new BaseQuery(hypervisorMetric));
        }
        vmMetrics.clear();
        for (Metric vmMetricXML : profileMetrics.getSecondary()) {
            vmMetrics.add(new BaseQuery(vmMetricXML));
        }
        customMetrics.clear();
        for (Metric customMetricXML : profileMetrics.getCustom()) {
            customMetrics.add(new BaseQuery(customMetricXML));
        }

    }

    private void resetTimersFromConfig(ConnectionConfiguration configuration) {
        this.monitorTimer = new MonitorTimer("vemaMonitor", configuration.getCommon().getCheckIntervalMinutes(), 0);
        this.syncTimer = new MonitorTimer("vemaSync", configuration.getCommon().getSyncIntervalMinutes(), 0);
        this.comaTimer = new MonitorTimer("vemaComa", configuration.getCommon().getComaIntervalMinutes(), 0);
        if (log.isInfoEnabled()) {
            log.info("Monitoring interval set to '" + configuration.getCommon().getCheckIntervalMinutes() + "' Minutes");
            log.info("Syncing interval set to '" + configuration.getCommon().getSyncIntervalMinutes() + "' Minutes");
            log.info("COMA-detection interval set to '" + configuration.getCommon().getSyncIntervalMinutes() + "' Minutes");
        }
    }

    private void resetTimersFromDefaults(ConnectionConfiguration configuration) {
        monitorTimer = new MonitorTimer("vemaMonitor", MonitorTimer.DEFAULT_MONITOR_INTERVAL_MINUTES, 0);
        syncTimer = new MonitorTimer("vemaSync", MonitorTimer.DEFAULT_SYNC_INTERVAL_MINUTES, 0);
        comaTimer = new MonitorTimer("vemaSync", MonitorTimer.DEFAULT_COMA_INTERVAL_MINUTES, 0);
        if (log.isInfoEnabled()) {
            log.info("Monitoring interval set to '" + configuration.getCommon().getCheckIntervalMinutes() + "' Minutes (Defaults)");
            log.info("Syncing interval set to '" + configuration.getCommon().getSyncIntervalMinutes() + "' Minutes (Defaults)");
            log.info("COMA-detection interval set to '" + configuration.getCommon().getSyncIntervalMinutes() + "' Minutes (Defaults)");
        }
    }

    @PostConstruct
    public void initialize() {
        configurationProvider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
        monitoringConnector = connectorFactory.getMonitoringConnector(configuration);
        managementConnector = connectorFactory.getManagementConnector((configuration));
        CollectionMode mode = createCollectionModes(configuration);
        monitoringConnector.setCollectionMode(mode);
        managementConnector.setCollectionMode(mode);
        getGwosService();
    }

    protected synchronized GwosService getGwosService() {
        if (gwosServiceInstance == null) {
            gwosServiceInstance = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
        }
        return gwosServiceInstance;
    }

    @Override
    public void connect() {

        if (monitoringConnector == null || managementConnector == null)
            initialize();
        try {
            monitoringConnector.connect(configuration.getConnection());
            managementConnector.openConnection(configuration.getConnection());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        monitoringConnector.disconnect();
        managementConnector.closeConnection();
    }

    /**
     * Synchronize all data center inventory
     *
     * @return the inventory for the data center
     */
    public DataCenterSyncResult synchronizeInventory() {
        long startTime = new Date().getTime();
        GwosService gwosService = getGwosService();
        // migrate application types from older versions
        gwosService.migrateApplicationTypes();
        DataCenterInventory vemaInventory = managementConnector.gatherInventory();
        InventoryOptions options = new InventoryOptions(configuration.getCommon().isHypervisorView(),
                                                        configuration.getCommon().isStorageView(),
                                                        configuration.getCommon().isNetworkView(),
                                                        configuration.getCommon().isResourcePoolView(),
                                                        configuration.getCommon().isEnableGroupTag(),
                                                        configuration.getCommon().getGroupTag());
        DataCenterInventory gwosInventory = gwosService.gatherInventory(options);
        DataCenterSyncResult results =
                synchronizer.synchronizeInventory(vemaInventory, gwosInventory, configuration, agentInfo, gwosService);
        results.setMonitoringInventory(vemaInventory);
        results.setGwosInventory(gwosInventory);
        if (log.isDebugEnabled()) {
            results.debug(agentInfo.getName());
        }
        long timeToSync = (System.currentTimeMillis() - startTime);
        if (log.isInfoEnabled()) {
            log.info("Synchronize DataCenter execution time: " + timeToSync + " ms");
        }
        if (statisticsService.isEnabled()) {
            MonitoringStatistics stats = getMonitorStatistics(agentInfo.getName());
            stats.getExecutionTimes().setInventorySync(timeToSync);
        }
        return results;
    }

    protected ProfileMetrics readMetrics() {
        VirtualSystem virtualSystem = configuration.getCommon().getVirtualSystem();
        ProfileMetrics profileMetrics = profileService.readMetrics(virtualSystem, configuration.getCommon().getAgentId());
        if (profileMetrics == null) {
            // somehow metrics are not available locally, try to recover using connector,
            // remote profile, or defaults.
            HubProfile profile = managementConnector.readProfile();
            if (profile == null) {
                profile = profileService.readRemoteProfile(virtualSystem, configuration.getGwos());
            }
            if (profile == null) {
                profile = profileService.createProfile(configuration.getCommon().getVirtualSystem(), configuration.getCommon().getAgentId());
            }
            profile.setAgent(configuration.getCommon().getAgentId());
            profileService.saveProfile(profile);
            // reread metrics from default profile
            profileMetrics = profileService.readMetrics(virtualSystem, configuration.getCommon().getAgentId());
            if (profileMetrics == null) {
                throw new RuntimeException("Unable to read or recover metrics for "+virtualSystem+"/"+configuration.getCommon().getAgentId());
            }
        }
        resetMetrics(profileMetrics);
        return profileMetrics;
    }

    private MonitoringStatistics getMonitorStatistics(String agentName) {
        MonitoringStatistics stats = statisticsService.lookup(agentName);
        if (stats == null) {
            stats = statisticsService.create(agentName);
        }
        return stats;
    }

    private List<String> extractMetricNames(List<BaseQuery> metrics) {
        List<String> names = new LinkedList<>();
        if (metrics == null) {
            return names;
        }
        for (BaseQuery query : metrics) {
            names.add(query.getServiceName());
        }
        return names;
    }

    protected static final int MAX_AUDIT_MESSAGE = 4095;

    protected String trimAuditMessage(String message) {
        String trim = null;
        if (message.length() > MAX_AUDIT_MESSAGE) {
            trim = message.substring(0, MAX_AUDIT_MESSAGE);
        }
        else {
            trim = new String(message);
        }
        return trim;
    }

    protected ThreadInterruptBehavior deleteViewData(MonitoringState virtualMonitoredHosts) {
        log.info("Cloudhub Commencing with deletion of view data for view " + monitorState.getViewChangeState().getViews() +
                " agent: " + agentInfo.getName() + ", " + agentInfo.getAgentId());
        int successCount = 0;
        int failureCount = 0;
        try {
            if (monitorState.getViewChangeState() != null) {
                GwosService gwosService = getGwosService();
                if (monitorState.getViewChangeState().getViews().size() == 0 && monitorState.getViewChangeState().getGroupViews().size() > 0) {
                    for (int ix = 0; ix < monitorState.getViewChangeState().getGroupViews().size(); ix++) {
                        String view = monitorState.getViewChangeState().getGroupViews().get(ix);
                        DtoOperationResults hostResults = gwosService.deleteView(null, view, configuration.getCommon().getAgentId());
                        successCount += hostResults.getSuccessful();
                        failureCount += hostResults.getFailed();
                    }
                }
                else {
                    for (int ix = 0; ix < monitorState.getViewChangeState().getViews().size(); ix++) {
                        String view = monitorState.getViewChangeState().getViews().get(ix);
                        String groupView = null;
                        if (monitorState.getViewChangeState().getGroupViews().size() > 0) {
                            groupView = monitorState.getViewChangeState().getGroupViews().get(ix);
                        }
                        DtoOperationResults hostResults = gwosService.deleteView(view, groupView, configuration.getCommon().getAgentId());
                        successCount += hostResults.getSuccessful();
                        failureCount += hostResults.getFailed();
                    }
                }
                List<DeleteServiceInfo> deletions = monitorState.getViewChangeState().getGroupedServices().getPrimary();
                int serviceSuccess = 0, serviceFailure = 0;
                if (deletions != null && deletions.size() > 0) {
                    DtoOperationResults serviceResults = gwosService.deleteServices(deletions,
                            agentInfo.getApplicationType(),
                            ProfileConversion.convertVirtualSystemToMetricType(configuration.getCommon().getVirtualSystem(), true),
                            configuration.getCommon().getAgentId());
                    serviceSuccess = serviceResults.getSuccessful();
                    serviceFailure = serviceResults.getFailed();
                }

                // 7.1.1: delete by source type (metric type in servicestatus table)
                if (monitorState.getViewChangeState().getMetricsViews() != null) {
                    DtoOperationResults serviceResults = gwosService.deleteServicesBySourceType(
                            agentInfo.getApplicationType(),
                            monitorState.getViewChangeState().getMetricsViews(),
                            configuration.getCommon().getAgentId());
                    serviceSuccess += serviceResults.getSuccessful();
                    serviceFailure += serviceResults.getFailed();
                }

                List<String> deleteList = new LinkedList<>();
                // Delete from memory
                List<String> views;
                if (monitorState.getViewChangeState().getViews() != null &&
                        monitorState.getViewChangeState().getViews().size() > 0) {
                    views = monitorState.getViewChangeState().getViews();
                }
                else if (monitorState.getViewChangeState().getGroupViews() != null &&
                        monitorState.getViewChangeState().getGroupViews().size() > 0) {
                    views = monitorState.getViewChangeState().getGroupViews();
                }
                else {
                    views = new ArrayList<>();
                }
                if (virtualMonitoredHosts != null) {
                    for (Map.Entry<String,BaseHost> hostEntry : virtualMonitoredHosts.hosts().entrySet()) {
                        String key = hostEntry.getKey();
                        for (String view :views) {
                            if (key.startsWith(view)) {
                                deleteList.add(key);
                            }
                            if (agentInfo.getVirtualSystem().equals(VirtualSystem.CLOUDERA)) {
                                List<String> deleteVMList = new LinkedList<>();
                                for (Map.Entry<String,BaseVM> vm : hostEntry.getValue().getVMPool().entrySet()) {
                                    String key2 = vm.getKey();
                                    String name = vm.getValue().getVMName();
                                    for (String view2 : monitorState.getViewChangeState().getViews()) {
                                        if (key2.equals(view2)) {
                                            if (key2.equals("host")) {
                                               deleteVMList.add(name);
                                            }
                                            else {
                                                deleteVMList.add(key2);
                                            }
                                        }
                                    }
                                }
                                for (String vmName : deleteVMList) {
                                    hostEntry.getValue().getVMPool().remove(vmName);
                                }
                            }
                        }
                    }
                    for (String key : deleteList) {
                        virtualMonitoredHosts.hosts().remove(key);
                    }
                }
                String message = trimAuditMessage("Deleting Views[" + monitorState.getViewChangeState().getViews() +
                        "], total hosts deleted : " + successCount);
                gwosService.auditLogHost(configuration.getCommon().getVirtualSystem(),
                        configuration.getConnection().getHostName(),
                              AuditLog.Action.DELETE.name(), message, monitorState.getViewChangeState().getUserName());
                log.info(String.format("Force delete of view complete for agent %s : success: %d failure %d ",
                        agentInfo.getName(), successCount + serviceSuccess, failureCount + serviceFailure));
            }
        }
        catch (Exception e) {
            String message = "Agent: " + agentInfo.getName() + " - Failure occurred forcing deletion of view";
            log.error(message, e);
            gwosExceptionCount++;
            agentInfo.addError(message);
        }
        monitorState.completeDeleteView();
        return ThreadInterruptBehavior.Continue;
    }

    protected ThreadInterruptBehavior deleteServicesData(MonitoringState virtualMonitoredHosts) {
        log.info("Cloudhub Commencing with deletion of services data for " +
                " agent: " + agentInfo.getName() + ", " + agentInfo.getAgentId());
        try {
            if (monitorState.getServicesChangeState() != null) {

                GwosService gwosService = getGwosService();
                int serviceSuccess = 0, serviceFailure = 0;

                // delete by grouped views or metric types(primary,secondary)
                // this logic is necessary for deleting Cloudera services, where it is common to have
                // the same named service for different serviceTypes, for example ZOOKEEPER and HDFS can both have
                // a metric named 'fd_open'. So we only want to delete services from GWOS DB for given service type
                // Logically, this condition is applicable to other connectors, although some connectors prohibit
                // this condition by the naming conventions where each metric is unique system wide
                if (monitorState.getServicesChangeState().getGroupedServices().getGroups().size() > 0) {
                    for (String serviceType : monitorState.getServicesChangeState().getGroupedServices().getGroups().keySet()) {
                        List<DeleteServiceInfo> deletions = monitorState.getServicesChangeState().getGroupedServices().getGroups().get(serviceType);
                        if (deletions != null && deletions.size() > 0) {
                            DtoOperationResults results = gwosService.deleteServices(deletions,
                                    agentInfo.getApplicationType(),
                                    serviceType,
                                    configuration.getCommon().getAgentId());
                            serviceSuccess += results.getSuccessful();
                            serviceFailure += results.getFailed();
                            String message = trimAuditMessage("Deleting grouped service/metrics: " + deletions.toString());
                            gwosService.auditLogHost(configuration.getCommon().getVirtualSystem(),
                                    configuration.getConnection().getHostName(),
                                    AuditLog.Action.DELETE.name(), message, monitorState.getServicesChangeState().getUserName());
                            if (log.isInfoEnabled()) {
                                log.info("Deleted grouped: " + deletions.toString());
                            }
                        }
                    }
                }
                else {
                    List<DeleteServiceInfo> deletions = monitorState.getServicesChangeState().getGroupedServices().getPrimary();
                    if (deletions != null && deletions.size() > 0) {
                        DtoOperationResults results = gwosService.deleteServices(deletions,
                                agentInfo.getApplicationType(),
                                ProfileConversion.convertVirtualSystemToMetricType(configuration.getCommon().getVirtualSystem(), true),
                                configuration.getCommon().getAgentId());
                        serviceSuccess += results.getSuccessful();
                        serviceFailure += results.getFailed();
                        String message = trimAuditMessage("Deleting primary service/metrics: " + deletions.toString());
                        gwosService.auditLogHost(configuration.getCommon().getVirtualSystem(),
                                configuration.getConnection().getHostName(),
                                AuditLog.Action.DELETE.name(), message, monitorState.getServicesChangeState().getUserName());
                    }
                    deletions = monitorState.getServicesChangeState().getGroupedServices().getSecondary();
                    if (deletions != null && deletions.size() > 0) {
                        DtoOperationResults results = gwosService.deleteServices(deletions,
                                agentInfo.getApplicationType(),
                                ProfileConversion.convertVirtualSystemToMetricType(configuration.getCommon().getVirtualSystem(), false),
                                configuration.getCommon().getAgentId());
                        serviceSuccess += results.getSuccessful();
                        serviceFailure += results.getFailed();
                        String message = trimAuditMessage("Deleting secondary service/metrics: " + deletions.toString());
                        gwosService.auditLogHost(configuration.getCommon().getVirtualSystem(),
                                configuration.getConnection().getHostName(),
                                AuditLog.Action.DELETE.name(), message, monitorState.getServicesChangeState().getUserName());
                    }
                    deletions = monitorState.getServicesChangeState().getGroupedServices().getCustom();
                    if (deletions != null && deletions.size() > 0) {
                        DtoOperationResults results = gwosService.deleteServices(deletions,
                                agentInfo.getApplicationType(),
                                ProfileConversion.convertVirtualSystemToMetricType(configuration.getCommon().getVirtualSystem(), false),
                                configuration.getCommon().getAgentId());
                        serviceSuccess += results.getSuccessful();
                        serviceFailure += results.getFailed();
                        String message = trimAuditMessage("Deleting custom service/metrics: " + deletions.toString());
                        gwosService.auditLogHost(configuration.getCommon().getVirtualSystem(),
                                configuration.getConnection().getHostName(),
                                AuditLog.Action.DELETE.name(), message, monitorState.getServicesChangeState().getUserName());
                    }
                }
                if (virtualMonitoredHosts != null) {
                    deleteServicesFromMonitoringState(virtualMonitoredHosts);
                }
                log.info(String.format("Force delete of services complete for agent %s : success: %d failure %d ",
                        agentInfo.getName(), serviceSuccess, serviceFailure));
            }
        }
        catch (Exception e) {
            String message = "Agent: " + agentInfo.getName() + " - Failure occurred forcing deletion of services";
            log.error(message, e);
            gwosExceptionCount++;
            agentInfo.addError(message);
        }
        readMetrics();
        monitorState.completeDeleteServices();
        return ThreadInterruptBehavior.Continue;
    }

    protected ThreadInterruptBehavior deleteConnectorHost() {
        try {
            if (monitorState.getViewChangeState() == null) {
                return ThreadInterruptBehavior.Continue;
            }
            String hostName = monitorState.getViewChangeState().getConnectorHost();
            if (hostName == null) {
                return ThreadInterruptBehavior.Continue;
            }
            log.info("Cloudhub commenciing with deletion of connector hosts for host: " + hostName + ", " + agentInfo.getAgentId());
            int count = configurationService.countByHostName(hostName);
            DtoOperationResults results = getGwosService().deleteByConnectorHost(hostName, count);
            log.info(String.format("Force delete of connector host complete for agent %s : success: %d failure %d ",
                    agentInfo.getName(), results.getSuccessful(), results.getFailed()));
        }
        catch(Exception e) {
                String message = "Agent: " + agentInfo.getName() + " - Failure occurred forcing deletion of connector host";
                log.error(message, e);
                gwosExceptionCount++;
                agentInfo.addError(message);
        }
        monitorState.completeDeleteConnectorHost();
        return ThreadInterruptBehavior.Continue;
    }

    protected CollectionMode createCollectionModes(ConnectionConfiguration configuration) {
        CollectionMode mode = new CollectionMode(
                configuration.getCommon().isHypervisorView(), // hosts
                true, // VMs
                configuration.getCommon().isStorageView(),
                configuration.getCommon().isNetworkView(),
                configuration.getCommon().isResourcePoolView(),
                false,  // clusters
                false,  // data centers
                configuration.getCommon().isEnableGroupTag(),
                configuration.getCommon().getGroupTag(),
                configuration.getCommon().isCustomView(),
                configuration.getCommon().isPrefixServiceNames()
        );
        if (configuration instanceof SupportsExtendedViews) {
            mode.setViews(((SupportsExtendedViews)configuration).getViews());
        }
        return mode;
    }

}
