package org.groundwork.cloudhub.gwos;

import com.groundwork.collage.CollageSeverity;
import com.groundwork.collage.CollageState;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.log4j.Logger;
import org.groundwork.agents.configuration.GWOSVersion;
import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.agents.monitor.DeleteServicePrimaryInfo;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.amazon.AmazonConfigurationProvider;
import org.groundwork.cloudhub.connectors.opendaylight.OpenDaylightConfigurationProvider;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.gwos.messages.UpdateStatusMessages;
import org.groundwork.cloudhub.inventory.*;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringEvent;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.monitor.ConnectorMonitor;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.cloudhub.profile.ProfileConversion;
import org.groundwork.cloudhub.statistics.MonitoringStatistics;
import org.groundwork.cloudhub.statistics.MonitoringStatisticsService;
import org.groundwork.rs.client.*;
import org.groundwork.rs.dto.*;
import org.jboss.resteasy.util.Base64;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service(GwosService.NAME70)
@Scope("prototype")
public class GwosServiceRest70Impl extends BaseGwosService implements GwosService {

    private static Logger log = Logger.getLogger(GwosServiceRest70Impl.class);

    protected final static String INITIAL_STATE = GwosStatus.PENDING.status;
    protected final static String NOTIFICATIONTYPE_PROBLEM = "PROBLEM";
    protected final static String NOTIFICATIONTYPE_RECOVERY = "RECOVERY";
    protected final static String OPERATIONAL_STATUS_OPEN = "OPEN";
    protected static final int RETRIES = 2;

    protected HostClient hostClient = null;
    protected EventClient eventClient = null;
    protected HostGroupClient hostGroupClient = null;
    protected ServiceClient serviceClient = null;
    protected NotificationClient notificationClient = null;
    protected PerfDataClient performanceClient = null;
    protected ApplicationTypeClient applicationTypeClient = null;
    protected AgentClient agentClient = null;
    protected CategoryClient categoryClient = null;

    @Resource(name = MonitoringStatisticsService.NAME)
    private MonitoringStatisticsService statisticsService;

    public GwosServiceRest70Impl() {
    }

    public GwosServiceRest70Impl(ConnectionConfiguration configuration, CloudhubAgentInfo agentInfo) {
        super(configuration, agentInfo);
        String connectionString = buildRsConnectionString(connection.getGwos());
        hostClient = new HostClient(connectionString);
        hostGroupClient = new HostGroupClient(connectionString);
        eventClient = new EventClient(connectionString);
        serviceClient = new ServiceClient(connectionString);
        notificationClient = new NotificationClient(connectionString);
        performanceClient = new PerfDataClient(connectionString);
        applicationTypeClient = new ApplicationTypeClient(connectionString);
        agentClient = new AgentClient(connectionString);
        categoryClient = new CategoryClient(connectionString);
    }

    @Override
    public List<String> getHostNames() {
        List<String> names = new ArrayList<String>();
        try {
            String lookupByAgent = String.format("agentId = '%s'", connection.getCommon().getAgentId());
            for (DtoHost host : hostClient.query(lookupByAgent)) {
                names.add(host.getHostName());
            }
        } catch (Exception e) {
            String msg = "Failed to retrieve host names from GWOS Service";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return names;
    }

    @Override
    public Map<String, GWOSHost> getAllHosts() {
        Map<String, GWOSHost> hosts = new ConcurrentHashMap<>();
        try {
            for (DtoHost host : hostClient.list(DtoDepthType.Shallow)) {
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
    public List<String> getHostGroupNames() {
        List<String> names = new ArrayList<String>();
        ;
        try {
            String lookupByAgent = String.format("agentId = '%s'", connection.getCommon().getAgentId());
            for (DtoHostGroup hostGroup : hostGroupClient.query(lookupByAgent)) {
                names.add(hostGroup.getName());
            }
        } catch (Exception e) {
            String msg = "Failed to retrieve host group names from GWOS Service";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return names;
    }


    @Override
    public boolean addHypervisors(List<BaseHost> hypervisors, String agentName) {
        boolean writeStatus = true;
        long startTime = System.currentTimeMillis();
        try {
            String applicationType = agentInfo.getApplicationType();
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), true);
            DtoHostList hostUpdates = new DtoHostList();
            DtoEventList eventUpdates = new DtoEventList();
            DtoServiceList serviceUpdates = new DtoServiceList();
            for (BaseHost host : hypervisors) {
                // @since 7.1.1 skip over AWS AZ Hosts
                if (host.isTransient()) {
                    continue;
                }
                DtoHost newHost = new DtoHost();
                newHost.setAgentId(connection.getCommon().getAgentId());
                newHost.setHostName(host.getHostName());
                newHost.setDescription(host.getHostName());
                newHost.setDeviceDisplayName(host.getHostName());
                newHost.setAgentId(connection.getCommon().getAgentId());
                newHost.setAppType(agentInfo.getApplicationType());
                for (String key : host.getProperties().keySet()) {
                    Object property = host.getProperty(key);
                    newHost.putProperty(key, property);
                }
                String deviceIdentification = getDeviceIdentificationFromHost(host);
                newHost.setDeviceIdentification(deviceIdentification);
                newHost.setMonitorStatus(GwosStatus.PENDING.status);
                newHost.setStateType(CollageState.HARD.name());
                Date lastUpdate = parseDate(host.getLastUpdate());
                if (host.isOwnedByAgent()) {
                    hostUpdates.add(newHost);
                    DtoEvent event = new DtoEvent(host.getHostName(), OPERATIONAL_STATUS_OPEN, GwosStatus.PENDING.status, CollageSeverity.LOW.name(), "Initial setup");
                    event.setAppType(applicationType);
                    event.setReportDate(lastUpdate);
                    event.setLastInsertDate(lastUpdate);
                    event.setDevice(deviceIdentification);
                    eventUpdates.add(event);
                    if (log.isInfoEnabled())
                        log.info("### " + agentName + ": add hyp: metric count " + host.getMetricPool().size() + " for host " + host.getHostName());
                }
                else {
                    if (log.isInfoEnabled())
                        log.info("### " + agentName + ": skip hyp: metric count " + host.getMetricPool().size() + " for host " + host.getHostName());
                }
                for (String metricName : host.getMetricPool().keySet()) {
                    BaseMetric baseMetric = host.getMetric(metricName);
                    if (baseMetric.isConfigFlag()) {
                        continue;
                    }
                    // @since 7.1.1 editable custom names
                    String serviceName = baseMetric.getServiceName();
                    DtoService serviceStatus = new DtoService();
                    serviceStatus.setAgentId(connection.getCommon().getAgentId());
                    serviceStatus.setDescription(serviceName);
                    serviceStatus.setHostName(newHost.getHostName());
                    serviceStatus.setLastStateChange(lastUpdate);
                    serviceStatus.setMonitorStatus(GwosStatus.PENDING.status);
                    serviceStatus.setLastHardState(GwosStatus.PENDING.status);
                    serviceStatus.setStateType(CollageState.HARD.name());
                    serviceStatus.setAppType(applicationType);
                    if (baseMetric.getMetricType() != null) {
                        serviceStatus.setMetricType(baseMetric.getMetricType());
                    }
                    else {
                        serviceStatus.setMetricType(metricType.name());
                    }
                    for (String key : baseMetric.getProperties().keySet()) {
                        Object property = baseMetric.getProperty(key);
                        serviceStatus.putProperty(key, property);
                    }
                    serviceUpdates.add(serviceStatus);
                    DtoEvent serviceEvent = new DtoEvent(host.getHostName(), OPERATIONAL_STATUS_OPEN, GwosStatus.PENDING.status, CollageSeverity.LOW.name(), "Initial setup for " + serviceName);
                    serviceEvent.setAppType(applicationType);
                    serviceEvent.setReportDate(lastUpdate);
                    serviceEvent.setLastInsertDate(lastUpdate);
                    serviceEvent.setDevice(deviceIdentification);
                    serviceEvent.setService(serviceName);
                    eventUpdates.add(serviceEvent);
                }
            }
            if (log.isInfoEnabled()) log.info("### " + agentName + ": add hyp: storing hosts " + hostUpdates.size());
            hostClient.post(hostUpdates, connection.getGwos().isMergeHosts());
            if (log.isInfoEnabled()) log.info("### " + agentName + ": add hyp: storing services " + serviceUpdates.size());
            serviceClient.post(serviceUpdates, connection.getGwos().isMergeHosts());
            if (log.isInfoEnabled()) log.info("### " + agentName + ": add hyp: storing events " + eventUpdates.size());
            eventClient.post(eventUpdates);
            if (log.isInfoEnabled()) log.info("### " + agentName + ": done add hypervisors");
            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setAddHypervisors(System.currentTimeMillis() - startTime);
                statistics.getAddsHypervisors().setHosts(hostUpdates.size());
                statistics.getAddsHypervisors().setServices(serviceUpdates.size());
                statistics.getAddsHypervisors().setEvents(eventUpdates.size());
            }
        } catch (Exception e) {
            log.error("Exception in addHypervisors: " + agentName + ": " + e.getMessage(), e);
            writeStatus = false;
        }
        return writeStatus;
    }

    @Override
    public boolean modifyHypervisors(List<BaseHost> hypervisors, String agentName, Map<String, String> hypervisorRunStates, boolean isGroundworkConnector) {
        long startTime = System.currentTimeMillis();
        boolean writeStatus = true;
        try {
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), true);
            // NextCheck time setting. Monitoring interval is in minutes and needs
            // to be converted to milli seconds */
            Date nextCheckTime = new Date(System.currentTimeMillis() + (connection.getCommon().getCheckIntervalMinutes() * 60 * 1000));
            ConcurrentHashMap<String, BaseMetric> serviceList = null;
            String applicationType = agentInfo.getApplicationType();
            DtoHostList hostUpdates = new DtoHostList();
            DtoEventList eventUpdates = new DtoEventList();
            DtoServiceList serviceUpdates = new DtoServiceList();
            DtoHostNotificationList hostNotifications = new DtoHostNotificationList();
            DtoServiceNotificationList serviceNotifications = new DtoServiceNotificationList();
            DtoPerfDataList performance = new DtoPerfDataList();
            for (BaseHost host : hypervisors) {
                // @since 7.1.1 skip over AWS AZ Hosts
                if (host.isTransient()) {
                    continue;
                }
                hypervisorRunStates.put(host.getHostName(), host.getRunState());
                DtoHost dtoHost = hostClient.lookup((host.getGwosHostName() != null) ? host.getGwosHostName() : host.getHostName());
                if (dtoHost == null) {
                    log.warn(agentName + ": host exists in VEMA but not GW - " + host.getHostName() + " skipping ");
                    continue;
                }
                if (!isGroundworkConnector) {
                    dtoHost.setAgentId(connection.getCommon().getAgentId());
                }
                dtoHost.setHostName((host.getGwosHostName() != null) ? host.getGwosHostName() : host.getHostName());
                dtoHost.setDescription(host.getHostName());
                dtoHost.setDeviceDisplayName(host.getHostName());
                // CLOUDHUB-209: never update agent id or app type on updates, first agent in owns the host
                String deviceIdentification = getDeviceIdentificationFromHost(host);
                dtoHost.setDeviceIdentification(deviceIdentification);
                dtoHost.setMonitorStatus((host.getRunState() == null) ? GwosStatus.PENDING.status : host.getRunState());
                dtoHost.setStateType(CollageState.HARD.name());
                Date lastUpdate = parseDate(host.getLastUpdate());
                dtoHost.setLastCheckTime(lastUpdate);
                if (host.getRunState() != null && host.getRunState().equals(BaseMetric.sScheduledDown)) {
                    dtoHost.putProperty("isAcknowledged", Boolean.TRUE);
                }
                dtoHost.putProperty("LastPluginOutput", formatHostPluginOutput(host));
                if (host.isStateChange() && lastUpdate != null) {
                    dtoHost.putProperty("LastStateChange", lastUpdate);
                }
                dtoHost.setNextCheckTime(nextCheckTime);

                if (host.isOwnedByAgent()) {
                    hostUpdates.add(dtoHost);
                }
                for (String key : host.getProperties().keySet()) {
                    Object property = host.getProperty(key);
                    dtoHost.putProperty(key, property);
                }

                serviceList = host.getMetricPool();
                if (log.isDebugEnabled()) {

                    log.debug("host in list of hypervisors ---:" + host.getHostName());
                    log.debug("Hypervisor [" + host.getHostName() + "] Status ["
                            + host.getRunState() + "] has number of metrics: "
                            + serviceList.size());

                }

                for (String metricName : serviceList.keySet()) {

                    BaseMetric vbm = serviceList.get(metricName);
                    if (vbm.isConfigFlag()) {
                        continue;
                    }
                    // @since 7.1.1 editable custom names
                    String serviceName = vbm.getServiceName();
                    String serviceValue = vbm.getCurrValue();
                    String serviceState = vbm.getCurrState();
                    if (GwosStatus.SCHEDULED_DOWN.status.equals(host.getRunState())) {
                        serviceState = GwosServiceStatus.SCHEDULED_CRITICAL.status;
                        vbm.setCurrState(serviceState);
                    } else if (GwosStatus.UNSCHEDULED_DOWN.status.equals(host.getRunState())) {
                        serviceState = GwosServiceStatus.UNSCHEDULED_CRITICAL.status;
                        vbm.setCurrState(serviceState);
                    } else if (GwosStatus.UNREACHABLE.status.equals(host.getRunState())) {
                        serviceState = GwosServiceStatus.UNKNOWN.status;
                        vbm.setCurrState(serviceState);
                    }
                    String serviceExtra = vbm.getCurrStateExtra();
                    long serviceWarn = vbm.getThresholdWarning();
                    long serviceCrit = vbm.getThresholdCritical();
                    boolean stateChanged = vbm.isStateChange();

                    if (log.isDebugEnabled())
                        log.debug("-------\n"
                                        + "Host:  '" + host.getHostName() + "'\n"
                                        + "VBM CURR:  '" + vbm.getCurrState() + "'\n"
                                        + "VBM LAST:  '" + vbm.getLastState() + "'\n"
                                        + "VBM LASTV:  '" + vbm.getLastValue() + "'\n"
                                        + "State Change:  '" + stateChanged + "'\n"
                                        + "ServiceName:  '" + serviceName + "'\n"
                                        + "ServiceState: '" + serviceState + "'\n"
                                        + "ServiceValue: '" + serviceValue + "'\n"
                                        + "ServiceExtra: '" + serviceExtra + "'\n"
                                        + "ServiceWarn:  '" + serviceWarn + "'\n"
                                        + "ServiceCrit:  '" + serviceCrit + "'\n"
                        );

                    DtoService serviceStatus = new DtoService();
                    serviceStatus.setAgentId(connection.getCommon().getAgentId());
                    serviceStatus.setDescription(serviceName);
                    serviceStatus.setHostName(dtoHost.getHostName());
                    if (stateChanged) {
                        serviceStatus.setLastStateChange(lastUpdate);
                    }
                    serviceStatus.setMonitorStatus(serviceState);
                    serviceStatus.setLastHardState(serviceState);
                    serviceStatus.setStateType(CollageState.HARD.name());
                    serviceStatus.setAppType(applicationType);
                    if (vbm.getMetricType() != null) {
                        serviceStatus.setMetricType(vbm.getMetricType());
                    }
                    else {
                        serviceStatus.setMetricType(metricType.name());
                    }
                    String lastPluginOutput =
                            serviceExtra
                                    + ", Status= " + serviceState
                                    + ", " + "(value=" + serviceValue + ") "
                                    + "[W/C=" + serviceWarn + "/" + serviceCrit + "] "
                                    + host.getLastUpdate();
                    serviceStatus.putProperty("LastPluginOutput", lastPluginOutput);
                    serviceStatus.setLastPlugInOutput(lastPluginOutput);
                    serviceStatus.setLastCheckTime(lastUpdate);
                    serviceStatus.setNextCheckTime(nextCheckTime);
                    serviceStatus.putProperty("PerformanceData", serviceValue);
                    for (String key : vbm.getProperties().keySet()) {
                        Object property = vbm.getProperty(key);
                        serviceStatus.putProperty(key, property);
                    }
                    serviceUpdates.add(serviceStatus);

                    if (stateChanged) {
                        if (log.isDebugEnabled())
                            log.debug("State for Host " + host.getHostName() + " and service " + serviceName + " has changed");
                        DtoEvent event = new DtoEvent(host.getHostName(), OPERATIONAL_STATUS_OPEN,
                                (serviceState == null ? GwosServiceStatus.PENDING.status : serviceState),
                                CollageSeverity.LOW.name(), lastPluginOutput);
                        event.setAppType(applicationType);
                        event.setReportDate(lastUpdate);
                        event.setLastInsertDate(lastUpdate);
                        event.setDevice(deviceIdentification);
                        event.setService(serviceName);
                        eventUpdates.add(event);
                    }

                    // ----------------------------------------------------------------------------
                    // Send Notification if State has changed and previous message was not PENDING
                    // or current state is not OK.
                    // ----------------------------------------------------------------------------
                    if (stateChanged &&
                            (!vbm.getLastState().equalsIgnoreCase(INITIAL_STATE) || !GwosServiceStatus.OK.status.equals(serviceState))) {
                        if (log.isDebugEnabled()) {
                            log.debug("Service Notification sent to NoMa for host [" + host.getHostName() + "] Service " + serviceName);
                        }
                        serviceNotifications.add(createServiceNotification(host.getHostName(), serviceName, host.getIpAddress(),
                                serviceState, lastPluginOutput, host.getHostGroup()));
                    }
                    if (serviceList.get(serviceName).isGraphed()) {
                        performance.add(createRestPerformanceData(serviceName, host.getHostName(),
                                serviceValue, serviceWarn, serviceCrit, applicationType));
                    }
                } // end metric loop
                // Generate Events if there is a host change...
                if (host.isOwnedByAgent() && host.isStateChange()) {

                    if (log.isDebugEnabled()) {
                        log.debug("State for Host " + host.getHostName() + " has changed. Create Event");
                        log.debug("getRunExtra():     " + (host.getRunExtra() == null
                                ? "(null)" : host.getRunExtra().toString()));
                        log.debug("getRunState():     " + (host.getRunState() == null
                                ? "(null)" : host.getRunState().toString()));
                        log.debug("getPrevRunState(): " + (host.getPrevRunState() == null
                                ? "(null)" : host.getPrevRunState().toString()));
                        log.debug("getHostGroup():    " + (host.getHostGroup() == null
                                ? "(null)" : host.getHostGroup().toString()));
                        log.debug("getIpAddress():    " + (host.getIpAddress() == null
                                ? "(null)" : host.getIpAddress().toString()));
                    }
                    // Host notifications
                    DtoEvent event = new DtoEvent(host.getHostName(), OPERATIONAL_STATUS_OPEN,
                            (host.getRunState() == null ? GwosStatus.PENDING.status : host.getRunState()),
                            CollageSeverity.LOW.name(), host.getRunExtra());
                    event.setAppType(applicationType);
                    event.setReportDate(lastUpdate);
                    event.setDevice(deviceIdentification);
                    eventUpdates.add(event);

                    /* Send Notification if State has changed and previous message was not PENDING
                     * or current run state is not UP */
                    if (!host.getPrevRunState().equalsIgnoreCase(INITIAL_STATE) || !GwosStatus.UP.status.equals(host.getRunState())) {
                        hostNotifications.add(createHostNotification(host.getHostName(), host.getIpAddress(),
                                host.getRunState(), host.getRunExtra(), host.getHostGroup()));
                        if (log.isDebugEnabled()) {
                            log.debug("Host Notification sent to NoMa for host " + host.getHostName());
                        }
                    }
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod hyp: storing hosts " + hostUpdates.size());
            if (hostUpdates.size() > 0 ) {
                DtoOperationResults results = hostClient.post(hostUpdates, connection.getGwos().isMergeHosts());
                if (results.getFailed() > 0) {
                    logRestErrors(results, "hosts (hypervisor)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod hyp: storing services " + serviceUpdates.size());
            if (serviceUpdates.size() > 0) {
                DtoOperationResults results = serviceClient.post(serviceUpdates, connection.getGwos().isMergeHosts());
                if (results.getFailed() > 0) {
                    logRestErrors(results, "services (hypervisor)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod hyp: storing events " + eventUpdates.size());
            if (eventUpdates.size() > 0) {
                DtoOperationResults results = eventClient.post(eventUpdates);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "events (hypervisor)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod hyp: sending host notifications " + hostNotifications.size());
            if (hostNotifications.size() > 0) {
                DtoOperationResults results = notificationClient.notifyHosts(hostNotifications);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "host notifications (hypervisor)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod hyp: sending service notifications " + serviceNotifications.size());
            if (serviceNotifications.size() > 0) {
                DtoOperationResults results = notificationClient.notifyServices(serviceNotifications);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "service notifications (hypervisor)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod hyp: sending performance " + performance.size());
            if (performance.size() > 0) {
                DtoOperationResults results = performanceClient.post(performance);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "performance (hypervisor)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": done mod hypervisors");

            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setModifyHypervisors(System.currentTimeMillis() - startTime);
                statistics.getModsHypervisors().setHosts(hostUpdates.size());
                statistics.getModsHypervisors().setServices(serviceUpdates.size());
                statistics.getModsHypervisors().setEvents(eventUpdates.size());
                statistics.getModsHypervisors().setHostNotifications(hostNotifications.size());
                statistics.getModsHypervisors().setServiceNotifications(serviceNotifications.size());
                statistics.getModsHypervisors().setPerformance(performance.size());
            }

        } catch (Exception e) {
            log.error("Exception in modifyHypervisors: " + e.getMessage(), e);
            writeStatus = false;
        }

        return writeStatus;
    }

    @Override
    public boolean deleteHypervisors(List<BaseHost> hypervisors, String agentId) {

        List<String> hostNamesToDelete = new ArrayList<String>();
        try {
            for (BaseHost host : hypervisors) {
                hostNamesToDelete.add(host.getHostName());
            }
            hostClient.delete(hostNamesToDelete);
        } catch (Exception ex) {
            log.error("Failed to delete hypervisors for " + agentId, ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean addVirtualMachines(List<BaseVM> listOfVM, String agentName) {
        boolean writeStatus = true;
        long startTime = System.currentTimeMillis();
        try {
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), false);
            String applicationType = agentInfo.getApplicationType();
            DtoHostList hostUpdates = new DtoHostList();
            DtoEventList eventUpdates = new DtoEventList();
            DtoServiceList serviceUpdates = new DtoServiceList();
            for (BaseVM vm : listOfVM) {
                //creates the host
                DtoHost newVm = new DtoHost();
                newVm.setHostName(vm.getVMName());
                newVm.setAgentId(connection.getCommon().getAgentId());
                newVm.setDescription(vm.getVMName());
                newVm.setDeviceDisplayName(vm.getVMName());
                newVm.setAppType(agentInfo.getApplicationType());
                String deviceIdentification = getDeviceIdentificationFromVm(vm);
                newVm.setDeviceIdentification(deviceIdentification);
                newVm.setMonitorStatus(GwosStatus.PENDING.status);
                newVm.setStateType(CollageState.HARD.name());
                Date lastUpdate = parseDate(vm.getLastUpdate());
                for (String key : vm.getProperties().keySet()) {
                    Object property = vm.getProperty(key);
                    newVm.putProperty(key, property);
                }
                if (vm.isOwnedByAgent()) {
                    hostUpdates.add(newVm);
                    DtoEvent event = new DtoEvent(vm.getVMName(), OPERATIONAL_STATUS_OPEN, GwosStatus.PENDING.status, CollageSeverity.LOW.name(), "Initial setup");
                    event.setAppType(applicationType);
                    event.setReportDate(lastUpdate);
                    event.setLastInsertDate(lastUpdate);
                    event.setDevice(deviceIdentification);
                    eventUpdates.add(event);
                }

                for (String metricName : vm.getMetricPool().keySet()) {
                    BaseMetric baseMetric = vm.getMetric(metricName);
                    if (baseMetric.isConfigFlag()) {
                        continue;
                    }
                    // @since 7.1.1 editable custom names
                    String serviceName = baseMetric.getServiceName();
                    DtoService serviceStatus = new DtoService();
                    serviceStatus.setAgentId(connection.getCommon().getAgentId());
                    serviceStatus.setDescription(serviceName);
                    serviceStatus.setHostName(newVm.getHostName());
                    serviceStatus.setLastStateChange(lastUpdate);
                    serviceStatus.setMonitorStatus(GwosStatus.PENDING.status);
                    serviceStatus.setLastHardState(GwosStatus.PENDING.status);
                    serviceStatus.setStateType(CollageState.HARD.name());
                    serviceStatus.setAppType(applicationType);
                    if (baseMetric.getMetricType() != null) {
                        serviceStatus.setMetricType(baseMetric.getMetricType());
                    }
                    else {
                        serviceStatus.setMetricType(metricType.name());
                    }
                    serviceUpdates.add(serviceStatus);
                    for (String key : baseMetric.getProperties().keySet()) {
                        Object property = baseMetric.getProperty(key);
                        serviceStatus.putProperty(key, property);
                    }
                    DtoEvent serviceEvent = new DtoEvent(vm.getVMName(), OPERATIONAL_STATUS_OPEN, GwosStatus.PENDING.status, CollageSeverity.LOW.name(), "Initial setup for " + serviceName);
                    serviceEvent.setAppType(applicationType);
                    serviceEvent.setReportDate(lastUpdate);
                    serviceEvent.setLastInsertDate(lastUpdate);
                    serviceEvent.setDevice(deviceIdentification);
                    serviceEvent.setService(serviceName);
                    eventUpdates.add(serviceEvent);
                }
            }
            if (log.isInfoEnabled()) log.info("### " + agentName + ": add vm: storing hosts " + hostUpdates.size());
            hostClient.post(hostUpdates, connection.getGwos().isMergeHosts());
            if (log.isInfoEnabled()) log.info("### " + agentName + ": add vm: storing services " + serviceUpdates.size());
            serviceClient.post(serviceUpdates, connection.getGwos().isMergeHosts());
            if (log.isInfoEnabled()) log.info("### " + agentName + ": add vm: storing events " + eventUpdates.size());
            eventClient.post(eventUpdates);
            if (log.isInfoEnabled()) log.info("### " + agentName + ": done add vm");

            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setAddVMs(System.currentTimeMillis() - startTime);
                statistics.getAddsVMs().setHosts(hostUpdates.size());
                statistics.getAddsVMs().setServices(serviceUpdates.size());
                statistics.getAddsVMs().setEvents(eventUpdates.size());
            }
        } catch (Exception e) {
            log.error("Exception in addVirtualMachines: " + e.getMessage(), e);
            writeStatus = false;
        }
        return writeStatus;
    }

    @Override
    public boolean modifyVirtualMachines(List<BaseVM> listOfVM, String agentName, Map<String, String> hypervisorRunStates) {
        boolean writeStatus = true;
        long startTime = System.currentTimeMillis();
        try {
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), false);
            // NextCheck time setting. Monitoring interval is in minutes and needs
            // to be converted to milli seconds */
            Date nextCheckTime = new Date(System.currentTimeMillis() + (connection.getCommon().getCheckIntervalMinutes() * 60 * 1000));
            ConcurrentHashMap<String, BaseMetric> serviceList = null;
            String applicationType = agentInfo.getApplicationType();
            if (applicationType == null) {
                if (log.isDebugEnabled())
                    log.debug("Another place where appType == null");
                return false;
            }
            DtoHostList hostUpdates = new DtoHostList();
            DtoServiceList serviceUpdates = new DtoServiceList();
            DtoEventList eventUpdates = new DtoEventList();
            DtoHostNotificationList hostNotifications = new DtoHostNotificationList();
            DtoServiceNotificationList serviceNotifications = new DtoServiceNotificationList();
            DtoPerfDataList performance = new DtoPerfDataList();
            for (BaseVM vm : listOfVM) {
                DtoHost dtoHost = null;
                boolean abort = false;
                // Retry Loop, to handle observed load issues on token server (JOSSO)
                for (int retry = 0; retry < RETRIES; retry++) {
                    try {
                        dtoHost = hostClient.lookup((vm.getGwosHostName() != null) ? vm.getGwosHostName() : vm.getVMName());
                        if (dtoHost == null) {
                            log.warn("Host exists in VEMA but not GW - " + vm.getVMName() + " skipping ");
                        }
                        break;
                    }
                    catch (Exception e) {
                        log.error("Failed to lookup " + vm.getVMName() + ", on modifyVirtualMachines for agent " + agentName, e);
                        // have seen errors where JOSSO gets overloaded, retry ...
                        Thread.sleep(2000);
                        abort = true;
                    }
                }
                if (dtoHost == null) {
                    if (abort) {
                        log.error("=== Aborted lookup " + vm.getVMName() + ", on modifyVirtualMachines for agent " + agentName);
                    }
                    continue;
                }
                dtoHost.setAgentId(connection.getCommon().getAgentId());
                dtoHost.setHostName((vm.getGwosHostName() != null) ? vm.getGwosHostName() : vm.getVMName());
                dtoHost.setDescription(vm.getVMName());
                dtoHost.setDeviceDisplayName(vm.getVMName());
                // CLOUDHUB-209: never update agent id or app type on updates, first agent in owns the host
                String deviceIdentification = getDeviceIdentificationFromVm(vm);
                dtoHost.setDeviceIdentification(deviceIdentification);
                dtoHost.setMonitorStatus((vm.getRunState() == null) ? GwosStatus.PENDING.status : vm.getRunState());
                dtoHost.setStateType(CollageState.HARD.name());
                Date lastUpdate = parseDate(vm.getLastUpdate());
                dtoHost.setLastCheckTime(lastUpdate);
                dtoHost.putProperty("LastPluginOutput", formatVMPluginOutput(vm));
                String hypervisorRunState = hypervisorRunStates.get(vm.getHypervisor());
                if (GwosStatus.UNREACHABLE.status.equals(vm.getRunState()) &&
                        (GwosStatus.SCHEDULED_DOWN.status.equals(hypervisorRunState) ||
                                GwosStatus.UNSCHEDULED_DOWN.status.equals(hypervisorRunState) ||
                                GwosStatus.UNREACHABLE.status.equals(hypervisorRunState))) {
                    dtoHost.putProperty("LastPluginOutput", dtoHost.getProperty("LastPluginOutput")+", (Hypervisor down or unreachable)");
                }
                if (vm.isStateChange() && lastUpdate != null) {
                    dtoHost.putProperty("LastStateChange", lastUpdate);
                }
                dtoHost.setNextCheckTime(nextCheckTime);
                if (vm.getRunState() != null && vm.getRunState().equals(BaseMetric.sScheduledDown)) {
                    dtoHost.putProperty("isAcknowledged", Boolean.TRUE);
                }
                for (String key : vm.getProperties().keySet()) {
                    Object property = vm.getProperty(key);
                    dtoHost.putProperty(key, property);
                }

                if (vm.isOwnedByAgent()) {
                    hostUpdates.add(dtoHost);
                }

                serviceList = vm.getMetricPool();
                if (log.isDebugEnabled()) {
                    log.debug("VM [" + vm.getVMName() + "] Status ["
                            + vm.getRunState() + "] has number of metrics: "
                            + serviceList.size());
                }

                for (String metricName : serviceList.keySet()) {

                    BaseMetric vbm = serviceList.get(metricName);
                    if (vbm.isConfigFlag()) {
                        continue;
                    }
                    // @since 7.1.1 editable custom names
                    String serviceName = vbm.getServiceName();
                    String serviceValue = vbm.getCurrValue();
                    String serviceState = vbm.getCurrState();
                    if (GwosStatus.SCHEDULED_DOWN.status.equals(vm.getRunState())) {
                        serviceState = GwosServiceStatus.SCHEDULED_CRITICAL.status;
                        vbm.setCurrState(serviceState);
                    } else if (GwosStatus.UNSCHEDULED_DOWN.status.equals(vm.getRunState())) {
                        serviceState = GwosServiceStatus.UNSCHEDULED_CRITICAL.status;
                        vbm.setCurrState(serviceState);
                    } else if (GwosStatus.UNREACHABLE.status.equals(vm.getRunState())) {
                        serviceState = GwosServiceStatus.UNKNOWN.status;
                        vbm.setCurrState(serviceState);
                    }
                    String serviceExtra = vbm.getCurrStateExtra();
                    long serviceWarn = vbm.getThresholdWarning();
                    long serviceCrit = vbm.getThresholdCritical();
                    boolean stateChanged = vbm.isStateChange();

                    if (log.isTraceEnabled())
                        log.trace("-------\n"
                                        + "ServiceName:  '" + serviceName + "'\n"
                                        + "ServiceState: '" + serviceState + "'\n"
                                        + "ServiceValue: '" + serviceValue + "'\n"
                                        + "ServiceExtra: '" + serviceExtra + "'\n"
                                        + "ServiceWarn:  '" + serviceWarn + "'\n"
                                        + "ServiceCrit:  '" + serviceCrit + "'\n"
                        );

                    DtoService serviceStatus = new DtoService();
                    serviceStatus.setAgentId(connection.getCommon().getAgentId());
                    serviceStatus.setDescription(serviceName);
                    serviceStatus.setHostName(dtoHost.getHostName());
                    if (stateChanged) {
                        serviceStatus.setLastStateChange(lastUpdate);
                    }
                    serviceStatus.setMonitorStatus(serviceState);
                    serviceStatus.setLastHardState(serviceState);
                    serviceStatus.setStateType(CollageState.HARD.name());
                    serviceStatus.setAppType(applicationType);
                    if (vbm.getMetricType() != null) {
                        serviceStatus.setMetricType(vbm.getMetricType());
                    }
                    else {
                        serviceStatus.setMetricType(metricType.name());
                    }
                    String lastPluginOutput =
                            serviceExtra
                                    + ", Status= " + serviceState
                                    + ", " + "(value=" + serviceValue + ") "
                                    + "[W/C=" + serviceWarn + "/" + serviceCrit + "] "
                                    + vm.getLastUpdate();
                    serviceStatus.putProperty("LastPluginOutput", lastPluginOutput);
                    serviceStatus.setLastPlugInOutput(lastPluginOutput);
                    serviceStatus.setLastCheckTime(lastUpdate);
                    serviceStatus.setNextCheckTime(nextCheckTime);
                    serviceStatus.putProperty("PerformanceData", serviceValue);
                    for (String key : vbm.getProperties().keySet()) {
                        Object property = vbm.getProperty(key);
                        serviceStatus.putProperty(key, property);
                    }
                    serviceUpdates.add(serviceStatus);

                    if (stateChanged) {
                        if (log.isDebugEnabled())
                            log.debug("State for Host " + vm.getVMName() + " and service " + serviceName + " has changed");

                        if (log.isDebugEnabled())
                            log.debug("State change. Create event for VM "
                                            + vm.getVMName()
                                            + " And service "
                                            + serviceName
                            );
                        DtoEvent event = new DtoEvent(vm.getVMName(), OPERATIONAL_STATUS_OPEN,
                                (serviceState == null) ? GwosServiceStatus.PENDING.status : serviceState,
                                CollageSeverity.LOW.name(), lastPluginOutput);
                        event.setAppType(applicationType);
                        event.setReportDate(lastUpdate);
                        event.setLastInsertDate(lastUpdate);
                        event.setDevice(deviceIdentification);
                        event.setService(serviceName);
                        eventUpdates.add(event);
                    }

                    // ----------------------------------------------------------------------------
                    // Send Notification if State has changed and previous message was not PENDING
                    // or current state is not OK.
                    // ----------------------------------------------------------------------------
                    if (stateChanged &&
                            (!vbm.getLastState().equalsIgnoreCase(INITIAL_STATE) || !GwosServiceStatus.OK.status.equals(serviceState))) {
                        // ----------------------------------------------------------------------------
                        // HostIP address might be null. In this case use the server name
                        // ----------------------------------------------------------------------------
                        if (log.isDebugEnabled()) {
                            log.debug("Service Notification sent to NoMa for host [" + vm.getVMName() + "] Service " + serviceName);
                        }
                        serviceNotifications.add(createServiceNotification(vm.getVMName(), serviceName, vm.getIpAddress(),
                                serviceState, lastPluginOutput, vm.getHostGroup()));
                    }
                    if (vm.getMetric(serviceName).isGraphed()) {
                        performance.add(createRestPerformanceData(serviceName, vm.getVMName(),
                                serviceValue, serviceWarn, serviceCrit, applicationType));
                    }
                } // end metric loop

                // Generate Events if there is a host change...
                if (vm.isOwnedByAgent() && vm.isStateChange()) {

                    if (log.isDebugEnabled()) {
                        log.debug("State for Host " + vm.getVMName() + " has changed. Create Event");
                        log.debug("getRunExtra():     " + (vm.getRunExtra() == null
                                ? "(null)" : vm.getRunExtra().toString()));
                        log.debug("getRunState():     " + (vm.getRunState() == null
                                ? "(null)" : vm.getRunState().toString()));
                        log.debug("getPrevRunState(): " + (vm.getPrevRunState() == null
                                ? "(null)" : vm.getPrevRunState().toString()));
                        log.debug("getHostGroup():    " + (vm.getHostGroup() == null
                                ? "(null)" : vm.getHostGroup().toString()));
                        log.debug("getIpAddress():    " + (vm.getIpAddress() == null
                                ? "(null)" : vm.getIpAddress().toString()));
                    }
                    DtoEvent event = new DtoEvent(vm.getVMName(), OPERATIONAL_STATUS_OPEN, (vm.getRunState() == null ? GwosStatus.PENDING.status : vm.getRunState()), CollageSeverity.LOW.name(), vm.getRunExtra());
                    event.setReportDate(lastUpdate);
                    event.setDevice(deviceIdentification);
                    event.setAppType(applicationType);
                    eventUpdates.add(event);

                    /* Send Notification if State has changed and previous message was not PENDING
                     * or current run state is not UP */
                    if (!vm.getPrevRunState().equalsIgnoreCase(INITIAL_STATE) || !GwosStatus.UP.status.equals(vm.getRunState())) {
                        hostNotifications.add(createHostNotification(vm.getVMName(), vm.getIpAddress(),
                                vm.getRunState(), vm.getRunExtra(), vm.getHostGroup()));
                        if (log.isDebugEnabled()) {
                            log.debug("Host Notification sent to NoMa for host " + vm.getVMName());
                        }
                    }
                }
            }
            //will update the next check time for the hosts
            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod vm: storing hosts " + hostUpdates.size());
            if (hostUpdates.size() > 0) {
                DtoOperationResults results = hostClient.post(hostUpdates, connection.getGwos().isMergeHosts());
                if (results.getFailed() > 0) {
                    logRestErrors(results, "hosts (vm)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod vm: storing services " + serviceUpdates.size());
            if (serviceUpdates.size() > 0) {
                DtoOperationResults results = serviceClient.post(serviceUpdates, connection.getGwos().isMergeHosts());
                if (results.getFailed() > 0) {
                    logRestErrors(results, "services (vm)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod vm: storing events " + eventUpdates.size());
            if (eventUpdates.size() > 0) {
                DtoOperationResults results = eventClient.post(eventUpdates);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "events (vm)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod vm: sending host notifications " + hostNotifications.size());

            if (hostNotifications.size() > 0) {
                DtoOperationResults results = notificationClient.notifyHosts(hostNotifications);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "host notifications (vm)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod vm: sending service notifications " + serviceNotifications.size());
            if (serviceNotifications.size() > 0) {
                DtoOperationResults results = notificationClient.notifyServices(serviceNotifications);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "service notifications (vm)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": mod vm: sending performance " + performance.size());
            if (performance.size() > 0) {
                DtoOperationResults results = performanceClient.post(performance);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "performance (vm)");
                }
            }

            if (log.isInfoEnabled()) log.info("### " + agentName + ": done mod vm");

            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setModifyVMs(System.currentTimeMillis() - startTime);
                statistics.getModsVMs().setHosts(hostUpdates.size());
                statistics.getModsVMs().setServices(serviceUpdates.size());
                statistics.getModsVMs().setEvents(eventUpdates.size());
                statistics.getModsVMs().setHostNotifications(hostNotifications.size());
                statistics.getModsVMs().setServiceNotifications(serviceNotifications.size());
                statistics.getModsVMs().setPerformance(performance.size());
            }

        } catch (Exception e) {
            log.error("Exception in modifyVirtualMachines: " + e.getMessage(), e);
            writeStatus = false;
        }
        return writeStatus;
    }

    @Override
    public boolean deleteVirtualMachines(List<BaseVM> listOfVM, String agentName) {
        List<String> vmsNamesToDelete = new ArrayList<String>();

        try {
            for (BaseVM vm : listOfVM) {
                vmsNamesToDelete.add(vm.getVMName());
            }
            hostClient.delete(vmsNamesToDelete);
        } catch (Exception ex) {
            log.error("Failed to delete virtual machines for " + agentName, ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean addHostGroup(GWOSHostGroup hostGroup) {

        boolean result = true;

        try {
            DtoHostGroup rHostGroup = new DtoHostGroup();
            rHostGroup.setAlias(hostGroup.getAlias());
            rHostGroup.setAgentId(connection.getCommon().getAgentId());
            rHostGroup.setAppType(hostGroup.getApplicationType());
            rHostGroup.setDescription(hostGroup.getDescription());
            rHostGroup.setName(hostGroup.getHostGroup());
            DtoHostGroupList hostGroupUpdateList = new DtoHostGroupList();
            hostGroupUpdateList.add(rHostGroup);
            hostGroupClient.post(hostGroupUpdateList);
        } catch (Exception e) {
            String msg = "Failed to add host group.";
            log.error(msg, e);
            result = false;
        }

        return result;
    }

    @Override
    public boolean modifyHostGroup(GWOSHostGroup hostGroup, List<String> hostList) {
        boolean result = true;

        try {
            // compute modified host group hosts
            List<String> updateHostList = null;
            boolean clearHostList = false;
            DtoHostGroup rHostGroup = hostGroupClient.lookup(hostGroup.getHostGroup());
            if ((rHostGroup != null) && (rHostGroup.getHosts() != null) && !rHostGroup.getHosts().isEmpty()) {
                // update using specified hosts and any other hosts not
                // owned by this CloudHub agent; other hosts will be removed
                // after host group clear operation
                updateHostList = new ArrayList<String>(hostList);
                for (DtoHost host : rHostGroup.getHosts()) {
                    if (!updateHostList.contains(host.getHostName()) && !connection.getCommon().getAgentId().equals(host.getAgentId())) {
                        // update with host
                        updateHostList.add(host.getHostName());
                    } else {
                        //clear and update without host
                        if (hostGroup.getAgentId() == null) {
                            clearHostList = true;
                        }
                    }
                }
            } else {
                // update using all specified hosts
                updateHostList = hostList;
            }
            // clear host group if update requires it
            if (clearHostList) {
                rHostGroup = new DtoHostGroup();
                rHostGroup.setName(hostGroup.getHostGroup());
                DtoHostGroupList hostGroupClearList = new DtoHostGroupList();
                hostGroupClearList.add(rHostGroup);
                hostGroupClient.clear(hostGroupClearList);
            }
            // update host group including host group hosts
            rHostGroup = new DtoHostGroup();
            rHostGroup.setAlias(hostGroup.getAlias());
            rHostGroup.setAgentId((hostGroup.getAgentId() == null) ? connection.getCommon().getAgentId() : hostGroup.getAgentId());
            rHostGroup.setAppType(hostGroup.getApplicationType());
            rHostGroup.setDescription(hostGroup.getDescription());
            rHostGroup.setName(hostGroup.getHostGroup());
            if (updateHostList != null) {
                for (String hostName : updateHostList) {
                    DtoHost host = new DtoHost();
                    host.setHostName(hostName);
                    rHostGroup.addHost(host);
                }
            }
            DtoHostGroupList hostGroupUpdateList = new DtoHostGroupList();
            hostGroupUpdateList.add(rHostGroup);
            hostGroupClient.post(hostGroupUpdateList);
        } catch (Exception e) {
            String msg = "Failed to modify host group.";
            log.error(msg, e);
            result = false;
        }

        return result;
    }

    @Override
    public boolean deleteHostGroup(GWOSHostGroup hostGroup) {
        boolean result = true;

        try {
            DtoHostGroup rHostGroup = new DtoHostGroup();
            rHostGroup.setName(hostGroup.getHostGroup());
            DtoHostGroupList hostGroupUpdateList = new DtoHostGroupList();
            hostGroupUpdateList.add(rHostGroup);
            hostGroupClient.delete(hostGroupUpdateList);
        } catch (Exception e) {
            log.error("Failed to delete host group.", e);
            result = false;
        }
        return result;
    }

    @Override
    public void sendEventMessage(String host, String device, String service, String monitorStatus, String severity, String message, String type) {
        DtoEventList eventUpdates = new DtoEventList();
        DtoEvent event = new DtoEvent(host, OPERATIONAL_STATUS_OPEN, (monitorStatus == null ? GwosStatus.PENDING.status : monitorStatus), severity, message);
        if (service != null)
            event.setService(service);
        event.setDevice(device);
        event.setAppType(type);
        event.setReportDate(new Date());
        eventUpdates.add(event);
        eventClient.post(eventUpdates);
    }

    @Override
    public boolean testConnection(ConnectionConfiguration configuration) throws CloudHubException {
        try {
            String connectionString = buildRsConnectionString(configuration.getGwos());
            AuthClient authClient = new AuthClient(connectionString);
            String password = configuration.getGwos().getWsPassword();
            switch (GWOSVersion.determineVersion(configuration.getGwos().getGwosVersion())) {
                case version_70:
                    password = Base64.encodeBytes(password.getBytes());
                    break;
            }
            AuthClient.Response response = authClient.login(configuration.getGwos().getWsUsername(), password,
                    configuration.getCommon().getVirtualSystem().toString());
            return response.success();
        } catch (Exception e) {
            throw new CloudHubException(e.getMessage(), e);
        }
    }

    @Override
    public boolean authenticate(ConnectionConfiguration configuration) {
        try {
            String connectionString = buildRsConnectionString(configuration.getGwos());
            AuthClient authClient = new AuthClient(connectionString);
            String password = configuration.getGwos().getWsPassword();
            switch (GWOSVersion.determineVersion(configuration.getGwos().getGwosVersion())) {
                case version_70:
                    password = Base64.encodeBytes(password.getBytes());
                    break;
            }
            AuthClient.Response response = authClient.login(configuration.getGwos().getWsUsername(), password,
                    configuration.getCommon().getVirtualSystem().toString());
            return response.success();
        }
        catch (Exception e) {
            log.error("Auth failure", e);
            return false;
        }
    }

    @Override
    public DtoOperationResults deleteByAgent(ConnectionConfiguration configuration, int hostCount) throws CloudHubException {
        try {
            String connectionString = buildRsConnectionString(configuration.getGwos());
            AgentClient client = new AgentClient(connectionString);
            DtoOperationResults results = client.delete(configuration.getCommon().getAgentId());
            String hostName = configuration.getGwos().getGwosServer();
            deleteByConnectorHost(hostName, hostCount);
            return results;
        } catch (RestClientException e) {
            throw new CloudHubException(e);
        }
    }

    @Override
    public DtoOperationResults deleteByConnectorHost(String hostName, int hostCount) {
        try {
            int count = 0;
            // host count == 0 not 1, since this configuration has already been deleted
            if (hostCount == 0 && hostName != null) {
                DtoHost host = hostClient.lookup(hostName, DtoDepthType.Simple);
                if (host != null && isCloudHubApplicationType(host.getAppType())) {
                    return hostClient.delete(hostName);
                }
            }
            return new DtoOperationResults();
        }
        catch (RestClientException e) {
            throw new CloudHubException(e);
        }
    }

    private boolean isCloudHubApplicationType(String applicationType) {
        for (String activeAppType : VirtualSystem.activeCloudHubApplicationTypes) {
            if (activeAppType.equals(applicationType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) {
        DataCenterInventory inventory = new DataCenterInventory(options);
        try {
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(connection.getCommon().getVirtualSystem());
            // Build VM list
            String lookupByAgent = String.format("agentId = '%s'", connection.getCommon().getAgentId());
            List<DtoHost> hosts = hostClient.query(lookupByAgent);
            for (DtoHost host : hosts) {
                String strippedHostName = stripVMPrefix(host.getHostName());
                VirtualMachineNode vmNode = new VirtualMachineNode(strippedHostName, host.getHostName());
                inventory.getVirtualMachines().put(strippedHostName, vmNode);
                inventory.getSystemNameMap().put(host.getHostName(), strippedHostName);
            }
            // Build HostGroup collections
            List<DtoHostGroup> groups = hostGroupClient.query(lookupByAgent);
            for (DtoHostGroup group : groups) {
                String strippedName = stripHostGroupPrefix(group.getName());
                InventoryType type = provider.prefixToInventoryType(group.getName());
                Map<String, InventoryContainerNode> nodes = null;
                switch (type) {
                    case Hypervisor:
                    case Host:
                        nodes = inventory.getHypervisors();
                        break;
                    case Network:
                        nodes = inventory.getNetworks();
                        break;
                    case Datastore:
                        nodes = inventory.getDatastores();
                        break;
                    case ResourcePool:
                        nodes = inventory.getResourcePools();
                        break;
                    default:
                        nodes = inventory.getTaggedGroups();
                        break;
                }
                if (nodes != null) {
                    InventoryContainerNode node = new InventoryContainerNode(strippedName, group.getName());
                    nodes.put(strippedName, node);
                    List<DtoHost> groupHosts = group.getHosts();
                    if (groupHosts != null) {
                        for (DtoHost host : groupHosts) {
                            String strippedHostName = stripVMPrefix(host.getHostName());
                            VirtualMachineNode vmNode = new VirtualMachineNode(strippedHostName, host.getHostName());
                            node.putVM(strippedHostName, vmNode);
                            inventory.getSystemNameMap().put(host.getHostName(), strippedHostName);
                        }
                    }
                }
            }
            inventory.setAllHosts(getAllHosts());
        } catch (Exception e) {
            String msg = "Failed to retrieve inventory from GWOS Service";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return inventory;
    }

    @Override
    public MonitorInventory gatherMonitorInventory(MonitorInventory connectorInventory) {
        MonitorInventory inventory = new MonitorInventory(null, agentInfo.getApplicationType(), agentInfo.getAgentId());
        // get all hosts that match connector agent id
        String lookupByAgent = String.format("agentId = '%s'", agentInfo.getAgentId());
        for (DtoHost dtoHost : hostClient.query(lookupByAgent, DtoDepthType.Deep)) {
            inventory.getHosts().put(dtoHost.getHostName(), dtoHost);
        }
        // get all remaining connector inventory hosts
        if (connectorInventory != null) {
            for (String hostName : connectorInventory.getHosts().keySet()) {
                if (!inventory.getHosts().containsKey(hostName)) {
                    DtoHost dtoHost = hostClient.lookup(hostName, DtoDepthType.Deep);
                    if (dtoHost != null) {
                        inventory.getHosts().put(dtoHost.getHostName(), dtoHost);
                    }
                }
            }
        }
        // get all services that match connector agent id and their hosts
        Map<Integer,DtoService> dtoServices = new HashMap<Integer, DtoService>();
        for (DtoService dtoService : serviceClient.query(lookupByAgent)) {
            inventory.getServices().put(dtoService.getHostName() + "!" + dtoService.getDescription(), dtoService);
            dtoServices.put(dtoService.getId(), dtoService);
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
                            if (connectorInventory.getServices().containsKey(serviceKey)) {
                                inventory.getServices().put(serviceKey, dtoService);
                                dtoServices.put(dtoService.getId(), dtoService);
                            }
                        } else {
                            inventory.getServices().put(serviceKey, dtoService);
                            dtoServices.put(dtoService.getId(), dtoService);
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
        if (connectorInventory != null) {
            for (String name : connectorInventory.getHostGroups().keySet()) {
                if (!inventory.getHostGroups().containsKey(name)) {
                    DtoHostGroup dtoHostGroup = hostGroupClient.lookup(name, DtoDepthType.Shallow);
                    if (dtoHostGroup != null) {
                        inventory.getHostGroups().put(dtoHostGroup.getName(), dtoHostGroup);
                    }
                }
            }
        }
        // get all service groups from categories, (assume all categories are owned by connection)
        for (DtoCategory dtoCategory : categoryClient.list()) {
            if ("SERVICE_GROUP".equals(dtoCategory.getEntityTypeName())) {
                DtoServiceGroup dtoServiceGroup = new DtoServiceGroup();
                dtoServiceGroup.setId(dtoCategory.getId());
                dtoServiceGroup.setName(dtoCategory.getName());
                dtoServiceGroup.setDescription(dtoCategory.getDescription());
                dtoServiceGroup.setAppType(agentInfo.getApplicationType());
                dtoServiceGroup.setAgentId(agentInfo.getAgentId());
                for (DtoCategoryEntity dtoCategoryEntity : dtoCategory.getEntities()) {
                    if ("SERVICE_STATUS".equals(dtoCategoryEntity.getEntityTypeName())) {
                        DtoService dtoService = dtoServices.get(dtoCategoryEntity.getObjectID());
                        if (dtoService != null) {
                            dtoServiceGroup.addService(dtoService);
                        }
                    }
                }
                inventory.getServiceGroups().put(dtoServiceGroup.getName(), dtoServiceGroup);
            }
        }
        return inventory;
    }

    @Override
    public void updateAllHypervisorsStatus(CloudhubAgentInfo agentInfo,
                                           String hostMonitorState,
                                           String serviceMonitorState,
                                           final UpdateStatusMessages messages) {
        // CloudHub GWOS 7.0.2/7.1.0 service implementation unable to update state and
        // send events/notification that will be reverted correctly when hosts later are
        // unsuspended or become reachable. Do not allow suspend or unreachable states,
        // events, and notifications to be set/sent now to avoid this problem. Note that
        // this functionality is properly supported with CloudHub GWOS Biz service
        // implementation.
        log.info("Unable to update hypervisors/vms/services state: "+hostMonitorState+"/"+serviceMonitorState);
    }

    @Override
    public void updateMonitorInventoryStatus(CloudhubAgentInfo agentInfo,
                                             String hostMonitorState,
                                             String serviceMonitorState,
                                             final UpdateStatusMessages messages) {
        updateHostAndServiceStatus(agentInfo, hostMonitorState, serviceMonitorState,
                new SendStatusChangeNotification() {
                    @Override
                    public boolean sendHostStatusChangeNotification(DtoHost host) {
                        // send notifications for all monitored hosts
                        return true;
                    }
                },
                new LookupUpdateStatusMessage() {
                    @Override
                    public String getHostMessage(DtoHost host) {
                        // return host message for monitor
                        return messages.getHostMonitorMessage();
                    }

                    @Override
                    public String getServiceMessage(DtoService service) {
                        // return service message for monitor
                        return messages.getServiceMonitorMessage();
                    }

                    @Override
                    public String getNotificationComment() {
                        // return notification comment for monitor
                        return messages.getMonitorComment();
                    }
                }, messages);
    }

    protected interface SendStatusChangeNotification {
        /**
         * Send host status change notification.
         *
         * @param host host
         * @return send notification
         */
        boolean sendHostStatusChangeNotification(DtoHost host);
    }

    protected interface LookupUpdateStatusMessage {
        /**
         * Get host update status message.
         *
         * @param host host
         * @return host update status message
         */
        String getHostMessage(DtoHost host);

        /**
         * Get service update status message.
         *
         * @param service service
         * @return service update status message
         */
        String getServiceMessage(DtoService service);

        /**
         * Get notification comment.
         *
         * @return notification comment
         */
        String getNotificationComment();
    }

    protected void updateHostAndServiceStatus(CloudhubAgentInfo agentInfo, String hostMonitorState,
                                              String serviceMonitorState,
                                              SendStatusChangeNotification sendStatusChangeNotification,
                                              LookupUpdateStatusMessage lookupUpdateStatusMessage,
                                              UpdateStatusMessages messages) {
        Date now = new Date();
        Date nextCheckTime = new Date(System.currentTimeMillis() + (connection.getCommon().getCheckIntervalMinutes() * 60 * 1000));
        try {
            if (log.isInfoEnabled()) {
                log.info("+++ updating status for all hosts and services to " + hostMonitorState);
            }
            String agentId = connection.getCommon().getAgentId();
            // event and noma update list across all types
            DtoEventList eventUpdates = new DtoEventList();
            DtoHostNotificationList notificationUpdates = new DtoHostNotificationList();
            // find all Hosts for this agent
            String lookupByAgent = String.format("agentId = '%s'", agentId);
            List<DtoHost> hosts = hostClient.query(lookupByAgent);
            // process hosts first ....
            DtoHostList hostUpdates = new DtoHostList();
            for (DtoHost host : hosts) {
                String message = lookupUpdateStatusMessage.getHostMessage(host);
                if (sendStatusChangeNotification.sendHostStatusChangeNotification(host)) {
                    notificationUpdates.add(createStatusChangeNotification(host, hostMonitorState, message,
                            messages.getNotificationType(), lookupUpdateStatusMessage.getNotificationComment()));
                }

                // only update status, plugin and check time properties
                host.setNextCheckTime(nextCheckTime);
                host.setLastCheckTime(now);
                host.setMonitorStatus(hostMonitorState);
                host.putProperty(MonitorInventory.LAST_PLUGIN_OUTPUT_PROPERTY_NAME, message);
                if (!host.getMonitorStatus().equals(hostMonitorState)) {
                    host.setLastStateChange(now);
                }
                

                hostUpdates.add(host);
                eventUpdates.add(createSeverityHostEvent(host, hostMonitorState, agentInfo.getApplicationType(), message,
                        now, messages));
            }
            if (hostUpdates.size() > 0) {
                if (log.isInfoEnabled()) {
                    log.info("+++ updating hosts for agent " + agentId + ", count: " + hostUpdates.size());
                }
                hostClient.post(hostUpdates, connection.getGwos().isMergeHosts());
            }

            // next process services ....
            DtoServiceList serviceUpdates = new DtoServiceList();
            List<DtoService> services = serviceClient.query(lookupByAgent);
            for (DtoService service : services) {
                if (service.getMetricType() != null && service.getMetricType().equals(ConnectorMonitor.CONNECTOR_METRIC_TYPE)) {
                    continue;
                }
                service.setMonitorStatus(serviceMonitorState);
                service.setLastHardState(serviceMonitorState);
                String message = lookupUpdateStatusMessage.getServiceMessage(service);
                service.setLastPlugInOutput(message);
                service.putProperty(MonitorInventory.LAST_PLUGIN_OUTPUT_PROPERTY_NAME, message);

                service.setLastCheckTime(now);
                service.setNextCheckTime(nextCheckTime);
                if (!service.getMonitorStatus().equals(serviceMonitorState)) {
                    service.setLastStateChange(now);
                }
                service.putProperty(MonitorInventory.PERFORMANCE_DATA_PROPERTY_NAME, "");

                serviceUpdates.add(service);
                eventUpdates.add(createSeverityServiceEvent(service, serviceMonitorState, agentInfo.getApplicationType(),
                        message, now, messages));
            }
            if (serviceUpdates.size() > 0) {
                if (log.isInfoEnabled()) {
                    log.info("+++ updating services for agent " + agentId + ", count: " + serviceUpdates.size());
                }
                serviceClient.post(serviceUpdates, connection.getGwos().isMergeHosts());
            }
            if (eventUpdates.size() > 0) {
                if (log.isInfoEnabled()) {
                    log.info("+++ sending events for agent " + agentId + ", count: " + eventUpdates.size());
                }
                eventClient.post(eventUpdates);
            }
            if (notificationUpdates.size() > 0) {
                if (log.isInfoEnabled()) {
                    log.info("+++ sending notifications for agent " + agentId + ", count: " + notificationUpdates.size());
                }
                notificationClient.notifyHosts(notificationUpdates);
            }

        } catch (Exception e) {
            String msg = "Failed to Update status for all hosts and services";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
    }

    @Override
    public boolean migrateApplicationTypes() {
        String appTypeName = null;
        switch(connection.getCommon().getVirtualSystem()) {
            case DOCKER:
            case OPENDAYLIGHT:
            case AMAZON:
            case NETAPP:
            case CLOUDERA:
            case ICINGA2:
            case AZURE:
            case NEDI:
                appTypeName = agentInfo.getApplicationType();
                break;
        }
        if (appTypeName != null) {
            DtoApplicationType appType = applicationTypeClient.lookup(appTypeName);
            if (appType == null) {
                String description = null;
                String criteria = null;
                switch(connection.getCommon().getVirtualSystem()) {
                    case DOCKER:
                        description = "Cloud Hub for Dockers Containers";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                    case OPENDAYLIGHT:
                        description = "Net Hub for Open Daylight SDN";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                    case AMAZON:
                        description = "Cloud Hub for Amazon Web Services";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                    case NETAPP:
                        description = "Cloud Hub for Net App";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                    case CLOUDERA:
                        description = "Cloud Hub for Cloudera";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                    case ICINGA2:
                        description = "Cloud Hub for Icinga 2";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                    case AZURE:
                        description = "Cloud Hub for Azure";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                    case NEDI:
                        description = "Cloud Hub for NeDi";
                        criteria = "Device;Host;ServiceDescription";
                        break;
                }
                appType = createApplicationType(appTypeName, description, criteria);
                // add entities?
                DtoApplicationTypeList updates = new DtoApplicationTypeList();
                updates.add(appType);
                // TODO: ApplicationTypes are cached on the server and in monitor apps; eviction strategy required
                applicationTypeClient.post(updates);
                return true;
            }
        }
        return false;
    }

    @Override
    public DtoApplicationType createApplicationType(String appTypeName, String description, String criteria) {
        DtoApplicationType appType = new DtoApplicationType();
        appType.setName(appTypeName);
        appType.setDescription(description);
        appType.setStateTransitionCriteria(criteria);
        return appType;
    }

    @Override
    public DtoOperationResults deleteServicesBySourceType(String appType, List<String> sourceType, String agentId)
            throws CloudHubException {
        if (sourceType.size() == 0)
            return new DtoOperationResults();
        StringBuffer query = new StringBuffer();
        query.append("agentid = '" + agentId + "' and metrictype in (");
        int count = 0;
        for (String source : sourceType) {
            if (count > 0) {
                query.append(",");
            }
            query.append("'");
            query.append(source);
            query.append("'");
            count++;
        }
        if (count > 0) {
            query.append(")");
        }
        List<DtoService> services = serviceClient.query(query.toString());
        if (services.size() > 0) {
            DtoServiceList deletes = new DtoServiceList();
            for (DtoService service : services) {
                deletes.add(service);
            }
            return serviceClient.delete(deletes);
        }
        return new DtoOperationResults();

    }


    @Override
    public DtoOperationResults deleteServices(List<DeleteServiceInfo> services, String appType, MetricType metricType, String agentId)
            throws CloudHubException {
        //return deleteServices(services, appType, (String)null, agentId);
        return deleteServices(services, appType, metricType.name(), agentId);
    }

    @Override
    public DtoOperationResults deleteServices(List<DeleteServiceInfo> services, String appType, String metricType, String agentId)
            throws CloudHubException {
        if (services.size() == 0)
            return new DtoOperationResults();
        List<String> likeServices = new ArrayList<>();
        DtoOperationResults results1 = new DtoOperationResults();
        DtoOperationResults results2 = new DtoOperationResults();
        boolean useLike = (appType.equals(OpenDaylightConfigurationProvider.APPLICATIONTYPE_OPENDAYLIGHT));
        StringBuffer query = new StringBuffer();
        query.append("agentid = '" + agentId + "' and ");
        if (metricType != null && !appType.equals(AmazonConfigurationProvider.APPLICATIONTYPE_AMAZON)) {  // AMAZON has mixed metricTypes
            query.append("metrictype = '" + metricType + "' and ");
        }
        int count = 0;
        if (useLike) {
            query.append("(");
            for (DeleteServiceInfo service : services) {
                if (count > 0)
                    query.append(" or ");
                query.append("description like '%-");
                query.append(service.getName());
                query.append("'");
                count++;
            }
            query.append(")");
        }
        else {
            query.append("description in (");
            for (DeleteServiceInfo service : services) {
                if (service.getOperationType() == DeleteServiceInfo.OperationType.prefixWildcard) {
                    String suffix = service.getName().substring(service.getPrefix().length());
                    likeServices.add(service.getPrefix() + "%" + suffix);
                }
                else {
                    if (count > 0)
                        query.append(",");
                    query.append("'");
                    query.append(service.getName());
                    query.append("'");
                    count++;
                }
            }
            query.append(")");
        }

        if (log.isDebugEnabled()) {
            log.debug(query);
        }

        if (count > 0) {
            List<DtoService> queriedServices = serviceClient.query(query.toString());
            if (queriedServices.size() > 0) {
                DtoServiceList deletes = new DtoServiceList();
                for (DtoService service : queriedServices) {
                    deletes.add(service);
                }
                results1 = serviceClient.delete(deletes);
            }
        }
        if (likeServices.size() > 0) {
            StringBuffer likeQuery = new StringBuffer();
            int likeCount = 0;
            likeQuery.append("agentid = '" + agentId + "' and ");
            likeQuery.append("(");
            for (String likeService : likeServices) {
                if (likeCount > 0)
                    likeQuery.append(" or ");
                likeQuery.append("description like '" + likeService + "'");
                likeCount++;
            }
            likeQuery.append(")");
            List<DtoService> queriedServices = serviceClient.query(likeQuery.toString());
            if (queriedServices.size() > 0) {
                DtoServiceList deletes = new DtoServiceList();
                for (DtoService service : queriedServices) {
                    deletes.add(service);
                }
                results2 = serviceClient.delete(deletes);
            }
        }
        DtoOperationResults results = new DtoOperationResults();
        results.setCount(results1.getCount() + results2.getCount());
        results.setSuccessful(results1.getSuccessful() + results2.getSuccessful());
        results.setFailed(results1.getFailed() + results2.getFailed());
        return results;
    }

    @Override
    public DtoOperationResults deleteView(String view, String groupView, String agentId) {
        DtoOperationResults results = new DtoOperationResults();
        if (view != null) {
            StringBuffer query = new StringBuffer();
            query.append("agentid = '" + agentId + "' and ");
            query.append("hostName like '" + view + "%'");
            List<DtoHost> hosts = hostClient.query(query.toString());
            if (hosts.size() > 0) {
                DtoHostList deletes = new DtoHostList();
                for (DtoHost host : hosts) {
                    deletes.add(host);
                }
                results = hostClient.delete(deletes);
            }
        }
        if (groupView != null) {
            //String groupView = view.replaceFirst("-", ":");
            StringBuffer query = new StringBuffer();
            query.append("agentid = '" + agentId + "' and ");
            query.append("name like '" + groupView + "%'");
            List<DtoHostGroup> hostGroups = hostGroupClient.query(query.toString());
            if (hostGroups.size() > 0) {
                DtoHostGroupList deletes = new DtoHostGroupList();
                for (DtoHostGroup hostGroup : hostGroups) {
                    deletes.add(hostGroup);
                }
                DtoOperationResults results2 = hostGroupClient.delete(deletes);
                results.setCount(results2.getCount() + results.getCount());
                results.setSuccessful(results2.getSuccessful() + results.getSuccessful());
                results.setFailed(results2.getFailed() + results.getFailed());
            }
        }
        return results;
    }


    /**
     * Rename is not supported in 70, only starting in 71
     * This operation defaults to old behavior, deleting by agent id
     *
     * @param agentId the agent id to restrict this update to
     * @param oldPrefix the old prefix to be removed
     * @param newPrefix the new prefix to be prepended
     * @return
     * @throws CloudHubException
     */
    @Override
    public DtoOperationResults renamePrefixByAgent(String agentId, String oldPrefix, String newPrefix)
            throws CloudHubException {

        try {
            return agentClient.delete(agentId);
        } catch (RestClientException e) {
            throw new CloudHubException(e);
        }
    }

    @Override
    public void sendMonitoringFaults(List<MonitoringEvent> events, String applicationType) {

        if (events == null || events.size() < 1)
            return;

        DtoEventList eventUpdates = new DtoEventList();
        Date now = new Date();
        for (MonitoringEvent event : events) {
            String monitorStatus = (event.getStatus() == null) ? MonitorStatusBubbleUp.UNKNOWN : event.getStatus();
            DtoEvent dto = new DtoEvent(event.getHostName(), OPERATIONAL_STATUS_OPEN, monitorStatus, CollageSeverity.LOW.name(), event.getMessage());
            dto.setService(event.getService());
            dto.setAppType(applicationType);
            dto.setReportDate(now);
            dto.setLastInsertDate(now);
            dto.setDevice(event.getHostName());
            dto.setConsolidationName("SYSTEM");
            eventUpdates.add(dto);
        }
        eventClient.post(eventUpdates);
    }

    @Override
    public void addMonitorInventory(MonitorInventory inventory) {
        // return if inventory empty
        if (inventory.isEmpty()) {
            return;
        }
        // add hosts inventory
        if (!inventory.getHosts().isEmpty()) {
            hostClient.post(new DtoHostList(new ArrayList<DtoHost>(inventory.getHosts().values())),
                    connection.getGwos().isMergeHosts());
        }
        // add host groups inventory
        if (!inventory.getHostGroups().isEmpty()) {
            hostGroupClient.post(new DtoHostGroupList(new ArrayList<DtoHostGroup>(inventory.getHostGroups().values())));
        }
        // add services inventory
        if (!inventory.getServices().isEmpty()) {
            serviceClient.post(new DtoServiceList(new ArrayList<DtoService>(inventory.getServices().values())),
                    connection.getGwos().isMergeHosts());
        }
        // add service groups inventory
        if (!inventory.getServiceGroups().isEmpty()) {
            addServiceGroupMonitorInventory(inventory.getServiceGroups().values());
        }
    }

    /**
     * Add all monitor service group inventory.
     *
     * @param inventory service group inventory to add
     */
    protected void addServiceGroupMonitorInventory(Collection<DtoServiceGroup> inventory) {
        // add service groups as categories, (all categories will be owned by connection)
        List<DtoCategory> dtoCategories = convertInventoryServiceGroupsToCategories(inventory);
        categoryClient.post(new DtoCategoryList(dtoCategories));
    }

    @Override
    public void updateMonitorInventory(MonitorInventory inventory) {
        // return if inventory empty
        if (inventory.isEmpty()) {
            return;
        }
        // update hosts inventory
        if (!inventory.getHosts().isEmpty()) {
            hostClient.post(new DtoHostList(new ArrayList<DtoHost>(inventory.getHosts().values())),
                    connection.getGwos().isMergeHosts());
        }
        // update host groups inventory
        if (!inventory.getHostGroups().isEmpty()) {
            hostGroupClient.clear(new DtoHostGroupList(simpleInventoryHostGroups(inventory.getHostGroups().values())));
            hostGroupClient.post(new DtoHostGroupList(new ArrayList<DtoHostGroup>(inventory.getHostGroups().values())));
        }
        // update services inventory
        if (!inventory.getServices().isEmpty()) {
            serviceClient.post(new DtoServiceList(new ArrayList<DtoService>(inventory.getServices().values())),
                    connection.getGwos().isMergeHosts());
        }
        // update service groups inventory
        if (!inventory.getServiceGroups().isEmpty()) {
            updateServiceGroupMonitorInventory(inventory.getServiceGroups().values());
        }
    }

    /**
     * Update all monitor service group inventory.
     *
     * @param inventory service group inventory to update
     */
    protected void updateServiceGroupMonitorInventory(Collection<DtoServiceGroup> inventory) {
        // update, (delete and add), service groups as categories; delete and add
        // should be safe since service groups are not arranged in a hierarchy
        categoryClient.delete(new DtoCategoryList(simpleInventoryServiceGroupsToCategories(inventory)));
        List<DtoCategory> dtoCategories = convertInventoryServiceGroupsToCategories(inventory);
        categoryClient.post(new DtoCategoryList(dtoCategories));
    }

    @Override
    public void deleteMonitorInventory(MonitorInventory inventory) {
        // return if inventory empty
        if (inventory.isEmpty()) {
            return;
        }
        // delete hosts inventory
        if (!inventory.getHosts().isEmpty()) {
            // filter owned hosts inventory
            List<DtoHost> filteredDtoHosts = new ArrayList<DtoHost>();
            for (DtoHost dtoHost : inventory.getHosts().values()) {
                if (agentInfo.getApplicationType().equals(dtoHost.getAppType()) &&
                        agentInfo.getAgentId().equals(dtoHost.getAgentId())) {
                    filteredDtoHosts.add(dtoHost);
                }
            }
            // delete owned hosts inventory
            if (!filteredDtoHosts.isEmpty()) {
                hostClient.delete(new DtoHostList(simpleInventoryHosts(filteredDtoHosts)));
            }
        }
        // delete host groups inventory
        if (!inventory.getHostGroups().isEmpty()) {
            // filter owned host groups inventory
            List<DtoHostGroup> filteredDtoHostGroups = new ArrayList<DtoHostGroup>();
            for (DtoHostGroup dtoHostGroup : inventory.getHostGroups().values()) {
                if (agentInfo.getApplicationType().equals(dtoHostGroup.getAppType()) &&
                        agentInfo.getAgentId().equals(dtoHostGroup.getAgentId())) {
                    filteredDtoHostGroups.add(dtoHostGroup);
                }
            }
            // delete owned host groups inventory
            if (!filteredDtoHostGroups.isEmpty()) {
                hostGroupClient.delete(new DtoHostGroupList(simpleInventoryHostGroups(filteredDtoHostGroups)));
            }
        }
        // delete services inventory
        if (!inventory.getServices().isEmpty()) {
            // filter owned services inventory
            List<DtoService> filteredDtoServices = new ArrayList<DtoService>();
            for (DtoService dtoService : inventory.getServices().values()) {
                if (agentInfo.getApplicationType().equals(dtoService.getAppType()) &&
                        agentInfo.getAgentId().equals(dtoService.getAgentId())) {
                    filteredDtoServices.add(dtoService);
                }
            }
            // delete owned services inventory
            if (!filteredDtoServices.isEmpty()) {
                serviceClient.delete(new DtoServiceList(simpleInventoryServices(filteredDtoServices)));
            }
        }
        // delete service groups inventory
        if (!inventory.getServiceGroups().isEmpty()) {
            // filter owned service groups inventory
            List<DtoServiceGroup> filteredDtoServiceGroups = new ArrayList<DtoServiceGroup>();
            for (DtoServiceGroup dtoServiceGroup : inventory.getServiceGroups().values()) {
                if (agentInfo.getApplicationType().equals(dtoServiceGroup.getAppType()) &&
                        agentInfo.getAgentId().equals(dtoServiceGroup.getAgentId())) {
                    filteredDtoServiceGroups.add(dtoServiceGroup);
                }
            }
            // delete owned host groups inventory
            if (!filteredDtoServiceGroups.isEmpty()) {
                deleteServiceGroupMonitorInventory(filteredDtoServiceGroups);
            }
        }
    }

    /**
     * Delete all monitor service group inventory.
     *
     * @param inventory service group inventory to delete
     */
    protected void deleteServiceGroupMonitorInventory(Collection<DtoServiceGroup> inventory) {
        // delete service groups as categories
        categoryClient.delete(new DtoCategoryList(simpleInventoryServiceGroupsToCategories(inventory)));
    }

    /**
     * Convert inventory service groups to categories.
     *
     * @param dtoServiceGroups inventory service groups
     * @return inventory categories
     */
    private List<DtoCategory> convertInventoryServiceGroupsToCategories(Collection<DtoServiceGroup> dtoServiceGroups) {
        List<DtoCategory> dtoCategories = new ArrayList<DtoCategory>(dtoServiceGroups.size());
        for (DtoServiceGroup dtoServiceGroup : dtoServiceGroups) {
            DtoCategory dtoCategory = new DtoCategory();
            dtoCategory.setId(dtoServiceGroup.getId());
            dtoCategory.setName(dtoServiceGroup.getName());
            dtoCategory.setDescription(dtoServiceGroup.getDescription());
            if ((dtoServiceGroup.getServices() != null) && dtoServiceGroup.getServices().isEmpty()) {
                for (DtoService dtoService : dtoServiceGroup.getServices()) {
                    DtoCategoryEntity dtoCategoryEntity = new DtoCategoryEntity();
                    dtoCategoryEntity.setEntityTypeName("SERVICE_STATUS");
                    dtoCategoryEntity.setObjectID(dtoService.getId());
                    dtoCategory.addEntity(dtoCategoryEntity);
                }
            }
            dtoCategories.add(dtoCategory);
        }
        return dtoCategories;
    }

    /**
     * Strip inventory hosts to simple host identities.
     *
     * @param dtoHosts inventory hosts
     * @return simple inventory host identities
     */
    private List<DtoHost> simpleInventoryHosts(Collection<DtoHost> dtoHosts) {
        List<DtoHost> simpleDtoHosts = new ArrayList<DtoHost>(dtoHosts.size());
        for (DtoHost dtoHost : dtoHosts) {
            DtoHost simpleDtoHost = new DtoHost();
            simpleDtoHost.setId(dtoHost.getId());
            simpleDtoHost.setHostName(dtoHost.getHostName());
            simpleDtoHosts.add(simpleDtoHost);
        }
        return simpleDtoHosts;
    }

    /**
     * Strip inventory host groups to simple host group identities.
     *
     * @param dtoHostGroups inventory host groups
     * @return simple inventory host group identities
     */
    private List<DtoHostGroup> simpleInventoryHostGroups(Collection<DtoHostGroup> dtoHostGroups) {
        List<DtoHostGroup> simpleDtoHostGroups = new ArrayList<DtoHostGroup>(dtoHostGroups.size());
        for (DtoHostGroup dtoHostGroup : dtoHostGroups) {
            DtoHostGroup simpleDtoHostGroup = new DtoHostGroup();
            simpleDtoHostGroup.setId(dtoHostGroup.getId());
            simpleDtoHostGroup.setName(dtoHostGroup.getName());
            simpleDtoHostGroups.add(simpleDtoHostGroup);
        }
        return simpleDtoHostGroups;
    }

    /**
     * Strip inventory services to simple service identities.
     *
     * @param dtoServices inventory services
     * @return simple inventory service identities
     */
    private List<DtoService> simpleInventoryServices(Collection<DtoService> dtoServices) {
        List<DtoService> simpleDtoServices = new ArrayList<DtoService>(dtoServices.size());
        for (DtoService dtoService : dtoServices) {
            DtoService simpleDtoService = new DtoService();
            simpleDtoService.setId(dtoService.getId());
            simpleDtoService.setHostName(dtoService.getHostName());
            simpleDtoService.setDescription(dtoService.getDescription());
            simpleDtoServices.add(simpleDtoService);
        }
        return simpleDtoServices;
    }

    /**
     * Convert inventory service groups to simple category identities.
     *
     * @param dtoServiceGroups inventory service groups
     * @return simple inventory category identities
     */
    private List<DtoCategory> simpleInventoryServiceGroupsToCategories(Collection<DtoServiceGroup> dtoServiceGroups) {
        List<DtoCategory> simpleDtoCategories = new ArrayList<DtoCategory>(dtoServiceGroups.size());
        for (DtoServiceGroup dtoServiceGroup : dtoServiceGroups) {
            DtoCategory simpleDtoCategory = new DtoCategory();
            simpleDtoCategory.setId(dtoServiceGroup.getId());
            simpleDtoCategory.setName(dtoServiceGroup.getName());
            simpleDtoCategory.setEntityTypeName("SERVICE_GROUP");
            simpleDtoCategories.add(simpleDtoCategory);
        }
        return simpleDtoCategories;
    }

    @Override
    public void modifyEventInventory(Collection<Object> eventInventory) {
        // batch update hosts, services, events, performance data
        DtoHostList dtoHostList = new DtoHostList();
        DtoServiceList dtoServiceList = new DtoServiceList();
        DtoEventList dtoEventList = new DtoEventList();
        DtoHostNotificationList dtoHostNotificationList = new DtoHostNotificationList();
        DtoServiceNotificationList dtoServiceNotificationList = new DtoServiceNotificationList();
        DtoPerfDataList dtoPerfDataList = new DtoPerfDataList();
        for (Object dtoObject : eventInventory) {
            if (dtoObject instanceof DtoHost) {
                dtoHostList.add((DtoHost) dtoObject);
            } else if (dtoObject instanceof DtoService) {
                dtoServiceList.add((DtoService)dtoObject);
            } else if (dtoObject instanceof DtoEvent) {
                dtoEventList.add((DtoEvent)dtoObject);
            } else if (dtoObject instanceof DtoHostNotification) {
                dtoHostNotificationList.add((DtoHostNotification)dtoObject);
            } else if (dtoObject instanceof DtoServiceNotification) {
                dtoServiceNotificationList.add((DtoServiceNotification)dtoObject);
            } else if (dtoObject instanceof DtoPerfData) {
                dtoPerfDataList.add((DtoPerfData)dtoObject);
            } else {
                throw new IllegalArgumentException("Unrecognized event inventory type: "+((dtoObject != null) ? dtoObject.getClass().getName() : null));
            }
        }
        if (dtoHostList.size() > 0) {
            hostClient.post(dtoHostList, connection.getGwos().isMergeHosts());
        }
        if (dtoServiceList.size() > 0) {
            serviceClient.post(dtoServiceList, connection.getGwos().isMergeHosts());
        }
        if (dtoEventList.size() > 0) {
            eventClient.post(dtoEventList);
        }
        if (dtoHostNotificationList.size() > 0) {
            notificationClient.notifyHosts(dtoHostNotificationList);
        }
        if (dtoServiceNotificationList.size() > 0) {
            notificationClient.notifyServices(dtoServiceNotificationList);
        }
        if (dtoPerfDataList.size() > 0) {
            performanceClient.post(dtoPerfDataList);
        }
    }

    protected DtoHostNotification createStatusChangeNotification(DtoHost host, String monitorStatus, String message,
                                                                 String notificationType, String comment) {
        DtoHostNotification notification = new DtoHostNotification();
        notification.setHostState(monitorStatus);
        notification.setHostName(host.getHostName());
        notification.setHostGroupNames("");
        notification.setNotificationType(notificationType);
        notification.setHostAddress(host.getDeviceIdentification());
        notification.setHostOutput(message);
        notification.setNotificationComment(comment);
        notification.setCheckDateTime(nowTime());
        return notification;
    }

    protected DtoEvent createSeverityHostEvent(DtoHost host, String monitorStatus,
                                               String applicationType, String message, Date lastUpdate,
                                               UpdateStatusMessages messages) {
        DtoEvent event = new DtoEvent(host.getHostName(), messages.getOperationalStatus(),
                                monitorStatus, messages.getSeverity(), message);
        event.setAppType(applicationType);
        event.setReportDate(lastUpdate);
        event.setLastInsertDate(lastUpdate);
        String device = (host.getDeviceIdentification() == null) ? host.getHostName() : host.getDeviceIdentification();
        event.setDevice(device);
        return event;
    }

    protected DtoEvent createSeverityServiceEvent(DtoService service, String monitorStatus,
                                                  String applicationType, String message, Date lastUpdate,
                                                  UpdateStatusMessages messages) {
        DtoEvent event = new DtoEvent(service.getHostName(), messages.getOperationalStatus(),
                                monitorStatus, messages.getSeverity(), message);
        event.setService(service.getDescription());
        event.setAppType(applicationType);
        event.setReportDate(lastUpdate);
        event.setLastInsertDate(lastUpdate);
        String device = (service.getDeviceIdentification() == null) ? service.getHostName() : service.getDeviceIdentification();
        event.setDevice(device);
        return event;
    }


    protected Map<String, DtoHost> findHypervisors(String agentId, String prefix) {
        Map<String, DtoHost> result = new HashMap<String, DtoHost>();
        try {
            String lookupByAgent = String.format("agentId = '%s'", agentId);
            for (DtoHostGroup hostGroup : hostGroupClient.query(lookupByAgent)) {
                // Prefix matches the management server, which holds all hypervisors
                if (hostGroup.getName().startsWith(prefix)) {
                    List<DtoHost> hosts = hostGroup.getHosts();
                    if (hosts != null) {
                        DtoHostList updates = new DtoHostList();
                        for (DtoHost host : hosts) {
                            result.put(host.getHostName(), host);
                        }
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Failed to Update status for all hypervisors";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return result;
    }


    protected DtoHostNotification createHostNotification(String hostName, String ipAddress,
                                                         String runState, String runExtra, String hostGroup) {
        DtoHostNotification notification = new DtoHostNotification();
        notification.setHostState(runState);
        notification.setHostName(hostName);
        notification.setHostGroupNames(hostGroup);
        notification.setNotificationType((runState.equalsIgnoreCase("UP"))
                ? NOTIFICATIONTYPE_RECOVERY
                : NOTIFICATIONTYPE_PROBLEM);
        notification.setHostAddress((ipAddress == null) ? hostName : ipAddress);
        if (runExtra == null)
            notification.setHostOutput("(none)");
        else
            notification.setHostOutput(runExtra);
        notification.setCheckDateTime(nowTime());
        notification.setNotificationComment("Cloud Hub Host Notification");
        return notification;
    }

    protected DtoServiceNotification createServiceNotification(String hostName, String serviceDescription, String ipAddress,
                                                               String runState, String runExtra, String hostGroup)  {
        DtoServiceNotification notification = new DtoServiceNotification();
        notification.setServiceDescription(serviceDescription);
        notification.setHostName(hostName);
        notification.setHostGroupNames(hostGroup);
        notification.setServiceState(runState);
        notification.setHostAddress((ipAddress == null) ? hostName : ipAddress);
        notification.setNotificationType((runState.equalsIgnoreCase(GwosServiceStatus.OK.status))
                ? NOTIFICATIONTYPE_RECOVERY
                : NOTIFICATIONTYPE_PROBLEM);
        if (runExtra == null)
            notification.setServiceOutput("(none)");
        else
            notification.setServiceOutput(runExtra);
        notification.setCheckDateTime(nowTime());
        notification.setNotificationComment("Cloud Hub Service Notification");
        return notification;
    }

    protected DtoPerfData createRestPerformanceData(String serviceName, String hostName,
                                                    String serviceValue, long serviceWarn, long serviceCrit, String appType) {
        DtoPerfData perf = new DtoPerfData();
        perf.setServerTime(System.currentTimeMillis() / 1000);
        perf.setServerName(hostName);
        perf.setServiceName(serviceName);
        // There is a 19 char limitation for the DS in the RRD graph
        String label = null;
        if (serviceName.length() > 19)
            label = serviceName.substring(serviceName.length() - 19, serviceName.length());
        else
            label = serviceName;
        perf.setLabel(label);
        perf.setValue(serviceValue);
        perf.setWarning(Long.toString(serviceWarn));
        perf.setCritical(Long.toString(serviceCrit));
        perf.setAppType(appType);
        return perf;
    }

    protected void logRestErrors(DtoOperationResults results, String api) {
        for (DtoOperationResult result : results.getResults()) {
            if (result.getStatus().equals(DtoOperationResult.FAILURE)) {
                String message = (results.getFailed() > 1) ? (", with " + results.getFailed() + " more errors") : "";
                log.error("Failed Rest API (" + api + ") - " + result.getMessage() + message);
                break;
            }
        }
    }

    private MonitoringStatistics getMonitorStatistics(String agentName) {
        MonitoringStatistics stats = statisticsService.lookup(agentName);
        if (stats == null) {
            stats = statisticsService.create(agentName);
        }
        return stats;
    }

    @Override
    public HostServiceInventory gatherHostServiceInventory() {
        HostServiceInventory state = new HostServiceInventory();
        try {
            String lookupByAgent = String.format("agentId = '%s'", connection.getCommon().getAgentId());
            // Depth sync will return IDs in 7.2.0 and higher. To get IDs versions prior, need  DtoDepthType.Deep)
            // NOTE: we are not using host or service ids in the ServiceSynchronizer, thus IDs are not required at this time
            // NOTE: Only versions 7.2.0 and higher returns agentID at Depth == Sync. Thus service sync feature against 7.1.1 or lower
            List<DtoHost> hosts = hostClient.query(lookupByAgent, DtoDepthType.Sync);
            for (DtoHost host : hosts) {
                String strippedHostName = stripVMPrefix(host.getHostName());
                ServiceContainerNode node = new ServiceContainerNode(strippedHostName, host.getHostName());
                state.addHost(node);

                if (host.getServices() != null) {
                    for (DtoService service : host.getServices()) {
                        String agentId = service.getAgentId();
                        if (agentId != null && agentId.equals(connection.getCommon().getAgentId())) {
                            node.addService(service.getDescription(), null, service.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Failed to retrieve inventory from GWOS Service";
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return state;
    }


    public DtoOperationResults deleteServices(List<DeleteServicePrimaryInfo> servicesToDelete) {
        List<DtoService> deletes = new ArrayList<>();
        for (DeleteServicePrimaryInfo info : servicesToDelete) {
            DtoService service = new DtoService();
            service.setHostName(info.getHostName());
            service.setDescription(info.getServiceName());
            deletes.add(service);
        }
        return serviceClient.delete(new DtoServiceList(deletes));
    }

    @Override
    public DtoHostGroup lookupHostGroup(String hostGroupName) {
        return hostGroupClient.lookup(hostGroupName, DtoDepthType.Simple);
    }

    @Override
    public DtoHost lookupHost(String hostName) {
        return hostClient.lookup(hostName, DtoDepthType.Simple);
    }

}
