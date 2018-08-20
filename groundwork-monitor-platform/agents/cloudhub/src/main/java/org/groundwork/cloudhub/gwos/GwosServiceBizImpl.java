package org.groundwork.cloudhub.gwos;

import com.groundwork.collage.model.ServiceStatus;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.gwos.messages.UpdateStatusMessages;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.cloudhub.profile.ProfileConversion;
import org.groundwork.cloudhub.statistics.MonitoringStatistics;
import org.groundwork.cloudhub.statistics.MonitoringStatisticsService;
import org.groundwork.rs.client.BizClient;
import org.groundwork.rs.dto.DtoBizHost;
import org.groundwork.rs.dto.DtoBizHostList;
import org.groundwork.rs.dto.DtoBizService;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service(GwosService.NAMEBIZ)
@Scope("prototype")
public class GwosServiceBizImpl extends GwosServiceRest71Impl implements GwosService {

    private static Logger log = Logger.getLogger(GwosServiceBizImpl.class);

    private String NOTIFICATIONTYPE_PROBLEM = "PROBLEM";
    private String OPERATIONAL_STATUS_OPEN = "OPEN";
    protected static final int RETRIES = 2;

    protected static final String ADD_HOST_MESSAGE = "Initial setup";
    protected static final String ADD_SERVICE_MESSAGE = "Initial setup for ";

    private BizClient bizClient = null;

    @Resource(name = MonitoringStatisticsService.NAME)
    private MonitoringStatisticsService statisticsService;

    public GwosServiceBizImpl() {
        super();
    }

    public GwosServiceBizImpl(ConnectionConfiguration configuration, CloudhubAgentInfo agentInfo) {
        super(configuration, agentInfo);
        String connectionString = buildRsConnectionString(connection.getGwos());
        bizClient = new BizClient(connectionString);
    }

    @Override
    public boolean addHypervisors(List<BaseHost> hypervisors, String agentName) {
        boolean writeStatus = false;
        long startTime = System.currentTimeMillis();
        int serviceCount = 0;
        try {
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), true);
            String applicationType = agentInfo.getApplicationType();
            DtoBizHostList hostUpdates = new DtoBizHostList();
            for (BaseHost host : hypervisors) {
                // @since 7.1.1 skip over AWS AZ Hosts
                if (host.isTransient()) {
                    continue;
                }
                writeStatus = true;
                DtoBizHost newHost = new DtoBizHost();
                newHost.setMergeHosts(connection.getGwos().isMergeHosts());
                newHost.setHost(host.getHostName());
                newHost.setStatus(GwosStatus.PENDING.status);
                newHost.setDevice(getDeviceIdentificationFromHost(host));
                newHost.setAppType(agentInfo.getApplicationType());
                newHost.setAgentId(connection.getCommon().getAgentId());
                newHost.setMessage(ADD_HOST_MESSAGE);
                newHost.setAllowInserts(true);
                newHost.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                for (String key : host.getProperties().keySet()) {
                    Object property = host.getProperty(key);
                    newHost.putProperty(key, property);
                }
                hostUpdates.add(newHost);

                if (log.isInfoEnabled())
                    log.info("### " + agentName + ": add hyp: metric count " + host.getMetricPool().size() + " for host " + host.getHostName());

                for (String metricName : host.getMetricPool().keySet()) {
                    BaseMetric baseMetric = host.getMetric(metricName);
                    if (baseMetric.isConfigFlag()) {
                        continue;
                    }
                    // @since 7.1.1 editable custom names
                    String serviceName = baseMetric.getServiceName();
                    DtoBizService service = new DtoBizService();
                    service.setMergeHosts(connection.getGwos().isMergeHosts());
                    service.setHost(newHost.getHost());
                    service.setService(serviceName);
                    service.setStatus(GwosStatus.PENDING.status);
                    service.setDevice(getDeviceIdentificationFromHost(host));
                    service.setAppType(applicationType);
                    if (baseMetric.getMetricType() != null) {
                        service.setMetricType(baseMetric.getMetricType());
                    }
                    else {
                        service.setMetricType(metricType.name());
                    }
                    service.setAgentId(connection.getCommon().getAgentId());
                    service.setMessage(ADD_SERVICE_MESSAGE + serviceName);
                    service.setAllowInserts(true);
                    service.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                    for (String key : baseMetric.getProperties().keySet()) {
                        Object property = baseMetric.getProperty(key);
                        service.putProperty(key, property);
                    }
                    newHost.add(service);
                    serviceCount++;

                    if (log.isDebugEnabled())
                        log.debug("AH:-------\n"
                                + "ServiceName:  '" + serviceName + "'\n"
                        );

                }
            }
            if (hostUpdates.size() == 0) {
                return writeStatus;
            }
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": add hyp: storing " + hostUpdates.size() + " hosts with "
                        + serviceCount + " services");
            }
            bizClient.postHosts(hostUpdates);
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": done add hypervisors");
            }
            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setAddHypervisors(System.currentTimeMillis() - startTime);
                statistics.getAddsHypervisors().setHosts(hostUpdates.size());
                statistics.getAddsHypervisors().setServices(serviceCount);
                statistics.getAddsHypervisors().setEvents(-1);
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
        int serviceCount = 0;
        try {
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), true);
            ConcurrentHashMap<String, BaseMetric> serviceList = null;
            String applicationType = agentInfo.getApplicationType();
                DtoBizHostList hostUpdates = new DtoBizHostList();
            for (BaseHost host : hypervisors) {
                // @since 7.1.1 skip over AWS AZ Hosts
                if (host.isTransient()) {
                    continue;
                }
                hypervisorRunStates.put(host.getHostName(), host.getRunState());
                DtoBizHost dtoHost = new DtoBizHost();
                dtoHost.setMergeHosts(connection.getGwos().isMergeHosts());
                dtoHost.setHost(host.getHostName());
                dtoHost.setStatus((host.getRunState() == null) ? GwosStatus.PENDING.status : host.getRunState());
                dtoHost.setDevice(getDeviceIdentificationFromHost(host));
                dtoHost.setAppType(agentInfo.getApplicationType());
                if (!isGroundworkConnector) {
                    dtoHost.setAgentId(connection.getCommon().getAgentId());
                }
                dtoHost.setAllowInserts(isGroundworkConnector);
                dtoHost.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                dtoHost.setMessage(StringUtils.isEmpty(host.getRunExtra()) ? host.getRunState() : host.getRunExtra());
                for (String key : host.getProperties().keySet()) {
                    Object property = host.getProperty(key);
                    dtoHost.putProperty(key, property);
                }
                hostUpdates.add(dtoHost);

                Date lastUpdate = parseDate(host.getLastUpdate());

                serviceList = host.getMetricPool();
                if (log.isTraceEnabled()) {

                    log.trace("host in list of hypervisors ---:" + host.getHostName());
                    log.trace("Hypervisor [" + host.getHostName() + "] Status ["
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
                    long serviceWarning = vbm.getThresholdWarning(); //(vbm.isGraphed()) ? vbm.getThresholdWarning() : -1;
                    long serviceCritical = vbm.getThresholdCritical(); //(vbm.isGraphed()) ? vbm.getThresholdCritical() : -1;
                    boolean stateChanged = vbm.isStateChange();

                    if (log.isDebugEnabled())
                        log.debug("MH: -------\n"
                                        + "Host:  '" + host.getHostName() + "'\n"
                                        + "VBM CURR:  '" + vbm.getCurrState() + "'\n"
                                        + "VBM LAST:  '" + vbm.getLastState() + "'\n"
                                        + "VBM LASTV:  '" + vbm.getLastValue() + "'\n"
                                        + "State Change:  '" + stateChanged + "'\n"
                                        + "ServiceName:  '" + serviceName + "'\n"
                                        + "ServiceState: '" + serviceState + "'\n"
                                        + "ServiceValue: '" + serviceValue + "'\n"
                                        + "ServiceExtra: '" + serviceExtra + "'\n"
                                        + "ServiceWarn:  '" + serviceWarning + "'\n"
                                        + "ServiceCrit:  '" + serviceCritical + "'\n"
                        );

                    DtoBizService service = new DtoBizService();
                    service.setMergeHosts(connection.getGwos().isMergeHosts());
                    service.setHost(dtoHost.getHost());
                    service.setService(serviceName);
                    service.setStatus(serviceState);
                    service.setDevice(getDeviceIdentificationFromHost(host));
                    service.setAppType(applicationType);
                    if (vbm.getMetricType() != null) {
                        service.setMetricType(vbm.getMetricType());
                    }
                    else {
                        service.setMetricType(metricType.name());
                    }
                    service.setAgentId(connection.getCommon().getAgentId());
                    service.setAllowInserts(false);
                    service.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                    service.setServiceValue(serviceValue);
                    service.setWarningLevel(serviceWarning);
                    service.setCriticalLevel(serviceCritical);
                    String lastPluginOutput =
                            serviceExtra
                                    + ", Status= " + serviceState
                                    + ", " + "(value=" + serviceValue + ") "
                                    + "[W/C=" + serviceWarning + "/" + serviceCritical + "] "
                                    + lastUpdate;
                    if (vbm.getExplanation() != null) {
                        lastPluginOutput += (" - " + vbm.getExplanation());
                    }
                    service.setMessage(lastPluginOutput);
                    for (String key : vbm.getProperties().keySet()) {
                        Object property = vbm.getProperty(key);
                        service.putProperty(key, property);
                    }
                    // add dynamic properties for isGraphed and isMonitored
                    service.putProperty(ServiceStatus.TP_IS_MONITORED, vbm.isMonitored());
                    service.putProperty(ServiceStatus.TP_IS_GRAPHED, vbm.isGraphed());
                    dtoHost.add(service);
                    serviceCount++;
                } // end metric loop
            }
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": mod hyp: storing " + hostUpdates.size() + " hosts with "
                        + serviceCount + " services");
            }
            if (hostUpdates.size() > 0) {
                DtoOperationResults results = bizClient.postHosts(hostUpdates);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "hosts (hypervisor)");
                }
            }
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": done mod hypervisors");
            }

            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setModifyHypervisors(System.currentTimeMillis() - startTime);
                statistics.getModsHypervisors().setHosts(hostUpdates.size());
                statistics.getModsHypervisors().setServices(serviceCount);
                statistics.getModsHypervisors().setEvents(-1);
                statistics.getModsHypervisors().setHostNotifications(-1);
                statistics.getModsHypervisors().setServiceNotifications(-1);
                statistics.getModsHypervisors().setPerformance(-1);
            }

        } catch (Exception e) {
            log.error("Exception in modifyHypervisors: " + e.getMessage(), e);
            writeStatus = false;
        }

        return writeStatus;
    }

    @Override
    public boolean addVirtualMachines(List<BaseVM> listOfVM, String agentName) {
        boolean writeStatus = true;
        int serviceCount = 0;
        long startTime = System.currentTimeMillis();
        try {
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), false);
            String applicationType = agentInfo.getApplicationType();
            DtoBizHostList hostUpdates = new DtoBizHostList();
            for (BaseVM vm : listOfVM) {

                DtoBizHost newVM = new DtoBizHost();
                newVM.setMergeHosts(connection.getGwos().isMergeHosts());
                newVM.setHost(vm.getVMName());
                newVM.setStatus(GwosStatus.PENDING.status);
                newVM.setDevice(getDeviceIdentificationFromVm(vm));
                newVM.setAppType(agentInfo.getApplicationType());
                newVM.setAgentId(connection.getCommon().getAgentId());
                newVM.setMessage(ADD_HOST_MESSAGE);
                newVM.setAllowInserts(true);
                newVM.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                for (String key : vm.getProperties().keySet()) {
                    Object property = vm.getProperty(key);
                    newVM.putProperty(key, property);
                }
                hostUpdates.add(newVM);

                for (String metricName : vm.getMetricPool().keySet()) {
                    BaseMetric baseMetric = vm.getMetric(metricName);
                    if (baseMetric.isConfigFlag()) {
                        continue;
                    }
                    // @since 7.1.1 editable custom names
                    String serviceName = baseMetric.getServiceName();
                    DtoBizService service = new DtoBizService();
                    service.setMergeHosts(connection.getGwos().isMergeHosts());
                    service.setHost(newVM.getHost());
                    service.setService(serviceName);
                    service.setStatus(GwosStatus.PENDING.status);
                    service.setDevice(getDeviceIdentificationFromVm(vm));
                    service.setAppType(applicationType);
                    service.setAgentId(connection.getCommon().getAgentId());
                    if (baseMetric.getMetricType() != null) {
                        service.setMetricType(baseMetric.getMetricType());
                    }
                    else {
                        service.setMetricType(metricType.name());
                    }
                    service.setMessage(ADD_SERVICE_MESSAGE + serviceName);
                    service.setAllowInserts(true);
                    service.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                    for (String key : baseMetric.getProperties().keySet()) {
                        Object property =baseMetric.getProperty(key);
                        service.putProperty(key, property);
                    }
                    newVM.add(service);
                    serviceCount++;

                    if (log.isDebugEnabled())
                        log.debug("AVM:-------\n"
                                + "ServiceName:  '" + serviceName + "'\n"
                        );

                }
            }
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": add vm: storing " + hostUpdates.size() + " hosts with "
                        + serviceCount + " services");
            }
            bizClient.postHosts(hostUpdates);
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": done add vm");
            }

            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setAddVMs(System.currentTimeMillis() - startTime);
                statistics.getAddsVMs().setHosts(hostUpdates.size());
                statistics.getAddsVMs().setServices(serviceCount);
                statistics.getAddsVMs().setEvents(-1);
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
        int serviceCount = 0;
        long startTime = System.currentTimeMillis();
        try {
            MetricType metricType = ProfileConversion.convertVirtualSystemToMetricType(agentInfo.getVirtualSystem(), false);
            ConcurrentHashMap<String, BaseMetric> serviceList = null;
            String applicationType = agentInfo.getApplicationType();
            DtoBizHostList hostUpdates = new DtoBizHostList();
            for (BaseVM vm : listOfVM) {

                DtoBizHost dtoHost = new DtoBizHost();
                dtoHost.setMergeHosts(connection.getGwos().isMergeHosts());
                dtoHost.setHost(vm.getVMName());
                dtoHost.setStatus((vm.getRunState() == null) ? GwosStatus.PENDING.status : vm.getRunState());
                dtoHost.setDevice(getDeviceIdentificationFromVm(vm));
                dtoHost.setAppType(agentInfo.getApplicationType());
                dtoHost.setAgentId(connection.getCommon().getAgentId());
                dtoHost.setAllowInserts(false);
                dtoHost.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                dtoHost.setMessage(StringUtils.isEmpty(vm.getRunExtra()) ? vm.getRunState() : vm.getRunExtra());
                String hypervisorRunState = hypervisorRunStates.get(vm.getHypervisor());
                if (GwosStatus.UNREACHABLE.status.equals(vm.getRunState()) &&
                        (GwosStatus.SCHEDULED_DOWN.status.equals(hypervisorRunState) ||
                                GwosStatus.UNSCHEDULED_DOWN.status.equals(hypervisorRunState) ||
                                GwosStatus.UNREACHABLE.status.equals(hypervisorRunState))) {
                    dtoHost.setMessage(dtoHost.getMessage()+", (Hypervisor down or unreachable)");
                }
                for (String key : vm.getProperties().keySet()) {
                    Object property = vm.getProperty(key);
                    dtoHost.putProperty(key, property);
                }
                hostUpdates.add(dtoHost);

                Date lastUpdate = parseDate(vm.getLastUpdate());

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
                    long serviceWarning = vbm.getThresholdWarning(); //(vbm.isGraphed()) ? vbm.getThresholdWarning() : -1;
                    long serviceCritical = vbm.getThresholdCritical(); //(vbm.isGraphed()) ?  : -1;

                    if (log.isDebugEnabled())
                        log.debug("MVN:-------\n"
                                        + "ServiceName:  '" + serviceName + "'\n"
                                        + "ServiceState: '" + serviceState + "'\n"
                                        + "ServiceValue: '" + serviceValue + "'\n"
                                        + "ServiceExtra: '" + serviceExtra + "'\n"
                                        + "ServiceWarn:  '" + serviceWarning + "'\n"
                                        + "ServiceCrit:  '" + serviceCritical + "'\n"
                        );

                    DtoBizService service = new DtoBizService();
                    service.setMergeHosts(connection.getGwos().isMergeHosts());
                    service.setHost(dtoHost.getHost());
                    service.setService(serviceName);
                    service.setStatus(serviceState);
                    service.setDevice(getDeviceIdentificationFromVm(vm));
                    service.setAppType(applicationType);
                    service.setAgentId(connection.getCommon().getAgentId());
                    if (vbm.getMetricType() != null) {
                        service.setMetricType(vbm.getMetricType());
                    }
                    else {
                        service.setMetricType(metricType.name());
                    }
                    service.setAllowInserts(false);
                    service.setCheckIntervalMinutes(connection.getCommon().getCheckIntervalMinutes());
                    service.setServiceValue(serviceValue);
                    service.setWarningLevel(serviceWarning);
                    service.setCriticalLevel(serviceCritical);
                    String lastPluginOutput =
                            serviceExtra
                                    + ", Status= " + serviceState
                                    + ", " + "(value=" + serviceValue + ") "
                                    + "[W/C=" + serviceWarning + "/" + serviceCritical + "] "
                                    + lastUpdate;
                    if (vbm.getExplanation() != null) {
                        lastPluginOutput += (" - " + vbm.getExplanation());
                    }
                    service.setMessage(lastPluginOutput);
                    for (String key : vbm.getProperties().keySet()) {
                        Object property = vbm.getProperty(key);
                        service.putProperty(key, property);
                    }
                    // add dynamic properties for isGraphed and isMonitored
                    service.putProperty("isGraphed", vbm.isGraphed());
                    service.putProperty("isMonitored", vbm.isMonitored());
                    dtoHost.add(service);
                    serviceCount++;
                } // end metric loop
            }
            //will update the next check time for the hosts
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": mod vm: storing " + hostUpdates.size() + " hosts with "
                        + serviceCount + " services");
            }
            if (hostUpdates.size() > 0) {
                DtoOperationResults results = bizClient.postHosts(hostUpdates);
                if (results.getFailed() > 0) {
                    logRestErrors(results, "hosts (vm)");
                }
            }
            if (log.isInfoEnabled()) {
                log.info("### " + agentName + ": done mod vm");
            }

            if (statisticsService.isEnabled()) {
                MonitoringStatistics statistics = statisticsService.lookup(agentInfo.getName());
                statistics.getExecutionTimes().setModifyVMs(System.currentTimeMillis() - startTime);
                statistics.getModsVMs().setHosts(hostUpdates.size());
                statistics.getModsVMs().setServices(serviceCount);
                statistics.getModsVMs().setEvents(-1);
                statistics.getModsVMs().setHostNotifications(-1);
                statistics.getModsVMs().setServiceNotifications(-1);
                statistics.getModsVMs().setPerformance(-1);
            }

        } catch (Exception e) {
            log.error("Exception in modifyVirtualMachines: " + e.getMessage(), e);
            writeStatus = false;
        }
        return writeStatus;
    }

    @Override
    public void updateAllHypervisorsStatus(CloudhubAgentInfo agentInfo,
                                           String hostMonitorState,
                                           String serviceMonitorState,
                                           final UpdateStatusMessages messages) {
        String prefix = connectorFactory.mapToManagementServerPrefix(agentInfo.getVirtualSystem());
        if (prefix == "")
            return;
        String agentId = connection.getCommon().getAgentId();
        final Map<String, DtoHost> hypervisors = findHypervisors(agentId, prefix);
        updateHostAndServiceStatus(agentInfo, hostMonitorState, serviceMonitorState,
                new SendStatusChangeNotification() {
                    @Override
                    public boolean sendHostStatusChangeNotification(DtoHost host) {
                        // only send notifications for hypervisors
                        return hypervisors.containsKey(host.getHostName());
                    }
                },
                new LookupUpdateStatusMessage() {
                    @Override
                    public String getHostMessage(DtoHost host) {
                        // return host message for hypervisors or vms
                        if (hypervisors.containsKey(host.getHostName())) {
                            return messages.getHostHypervisorMessage();
                        } else {
                            return messages.getHostVmMessage();
                        }
                    }

                    @Override
                    public String getServiceMessage(DtoService service) {
                        // return service message for hypervisors or vms
                        if (hypervisors.containsKey(service.getHostName())) {
                            return messages.getServiceHypervisorMessage();
                        } else {
                            return messages.getServiceVmMessage();
                        }
                    }

                    @Override
                    public String getNotificationComment() {
                        // return notification comment for hypervisors
                        return messages.getComment();
                    }
                }, messages);
    }
}
