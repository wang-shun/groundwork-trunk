package org.groundwork.rs.biz;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.HibernateProgrammaticTxnSupport;
import com.groundwork.collage.biz.BizServices;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.rs.common.BizFormParameters;
import org.groundwork.rs.common.GWRestConstants;
import org.groundwork.rs.conversion.BizHostServiceInDowntimeConverter;
import org.groundwork.rs.conversion.HostConverter;
import org.groundwork.rs.conversion.ServiceConverter;
import org.groundwork.rs.dto.*;
import org.groundwork.rs.influxdb.InfluxDBClient;
import org.groundwork.rs.resources.AbstractResource;
import org.groundwork.rs.resources.HostResource;
import org.groundwork.rs.resources.ResourceMessages;
import org.groundwork.rs.resources.ServiceResource;
import org.hibernate.FlushMode;

import javax.servlet.ServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Business (Biz) Service Layer Resources further abstract the low level REST API calls.
 * To start with it should be part of the Java API and in the future we could add REST API for it.
 * The selenium-connector is on a hold until we have an easier way for Java applications to communicate with the GroundWork backend.
 */
@Path("/biz")
public class BizResource extends AbstractResource {
    public static final String RESOURCE_PREFIX = "/biz/";

    private static final String WILDCARD = "*";

    /**
     * Crushed host services status mapping.
     */
    private static final Map<String,String> CRUSHED_HOST_SERVICES_STATUS_MAP = new HashMap<String,String>();
    static {
        CRUSHED_HOST_SERVICES_STATUS_MAP.put(MonitorStatusBubbleUp.SCHEDULED_DOWN, MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
        CRUSHED_HOST_SERVICES_STATUS_MAP.put(MonitorStatusBubbleUp.UNSCHEDULED_DOWN, MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL);
        CRUSHED_HOST_SERVICES_STATUS_MAP.put(MonitorStatusBubbleUp.DOWN, MonitorStatusBubbleUp.CRITICAL);
        CRUSHED_HOST_SERVICES_STATUS_MAP.put(MonitorStatusBubbleUp.SUSPENDED, MonitorStatusBubbleUp.UNKNOWN);
    }

    private final boolean INFLUX_ENABLED;
    private final Set<String> LOGPERF_APPS;

    public BizResource() {
        CollageFactory service = CollageFactory.getInstance();
        Properties configuration = service.getFoundationProperties();
        String backend = configuration.getProperty("perfdata.backend.default", "rrd");
        INFLUX_ENABLED = backend.equals("influxdb");
        String logPerfAppNames = configuration.getProperty("perfdata.logperf.appnames", "");
        LOGPERF_APPS = new HashSet<>(Arrays.asList(StringUtils.stripAll(StringUtils.split(logPerfAppNames, ","))));
    }

    @POST
    @Path("/host")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    /**
     *  This call would check if the Host exists. If it doesn't exist the host entity would be created and an initial event (PENDING) will be created.
     *  If the hostGroup is not null the hostGroup will be created if it doesn't exist. Host will be assigned to the HostGroup.
     *  If the host exists status and message will be updated.
     *  Method would also retrieve the current host status and if a state change occurred an Event and a Notification will be generated.
     */
    public DtoHost createOrUpdateHost(
                                     @FormParam(BizFormParameters.PARAM_HOST) @DefaultValue("") String host,
                                     @FormParam(BizFormParameters.PARAM_STATUS) @DefaultValue("") String status,
                                     @FormParam(BizFormParameters.PARAM_MESSAGE) @DefaultValue("") String message,
                                     @FormParam(BizFormParameters.PARAM_HOST_GROUP) @DefaultValue("") String hostGroup,
                                     @FormParam(BizFormParameters.PARAM_HOST_CATEGORY) @DefaultValue("") String hostCategory,
                                     @FormParam(BizFormParameters.PARAM_DEVICE) @DefaultValue("") String device,
                                     @FormParam(BizFormParameters.PARAM_APP_TYPE) @DefaultValue("") String appType,
                                     @FormParam(BizFormParameters.PARAM_AGENT_ID) @DefaultValue("") String agentId,
                                     @FormParam(BizFormParameters.PARAM_CHECK_INTERVAL_MINUTES) @DefaultValue("5") Integer checkIntervalMinutes,
                                     @FormParam(BizFormParameters.PARAM_ALLOW_INSERTS) @DefaultValue("TRUE") Boolean allowInserts,
                                     @FormParam(BizFormParameters.PARAM_MERGE_HOSTS) @DefaultValue("TRUE") Boolean mergeHosts,
                                     @FormParam(BizFormParameters.PARAM_SET_STATUS_ON_CREATE) @DefaultValue("FALSE") Boolean setStatusOnCreate,
                                     @Context ServletRequest request
    ) {
        CollageTimer timer = startMetricsTimer();
        host = host.trim();
        status = status.trim();
        message = message.trim();
        device = device.trim();
        hostGroup = hostGroup.trim();
        hostCategory = hostCategory.trim();
        appType = appType.trim();
        agentId = agentId.trim();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /biz/host with host = %s", host));
        }
        BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);
        try {
            Host updatedHost = biz.createOrUpdateHost(
                    host, status, message, hostGroup, hostCategory, device, appType, agentId,
                    checkIntervalMinutes, allowInserts, mergeHosts, setStatusOnCreate, null);
            if (updatedHost == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Host not merged or inserted: " + host).build());
            }
            return HostConverter.convert(updatedHost, DtoDepthType.Deep);
        }
        catch (BusinessServiceException e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for hosts.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Path("/hosts")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults upsertBizHosts(final DtoBizHostList dtoHosts) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /biz/hosts with %d hosts", (dtoHosts == null) ? 0 : dtoHosts.size()));
        }
        if (dtoHosts == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Host list was not provided").build());
        }
        if (dtoHosts.size() == 0) {
            return new DtoOperationResults("Host", DtoOperationResults.UPDATE);
        }

        // Build the dto list for influx here, using the dynamic "isGraphed" property before it is stripped off
        List<DtoPerfData> dtoPerfDataList = new ArrayList<>();
        if (INFLUX_ENABLED) {
            for (DtoBizHost dtoHost : dtoHosts.getHosts()) {
                addServicesToPerformanceData(dtoHost.getServices(), dtoPerfDataList);
            }
        }

        // try to upsert hosts in one transaction: disable Hibernate session flush
        // and retry individually if single transaction fails
        DtoOperationResults results = (DtoOperationResults) HibernateProgrammaticTxnSupport.executeInTxn(
                new HibernateProgrammaticTxnSupport.RunInTxnAdapter() {
                    @Override
                    public Object run() throws Exception {
                        // upsert hosts and services transactionally; caching for hosts and services assume consistent
                        // naming will be used for hosts within the transaction.
                        DtoOperationResults results = new DtoOperationResults("Host", DtoOperationResults.UPDATE);
                        Map<String,Host> hosts = new HashMap<String,Host>();
                        Map<String,HostGroup> hostGroups = new HashMap<String,HostGroup>();
                        Map<String,Category> hostCategories = new HashMap<String,Category>();
                        Map<String,Device> devices = new HashMap<String,Device>();
                        Map<String,ServiceStatus> services = new HashMap<String,ServiceStatus>();
                        Map<String,Category> serviceGroups = new HashMap<String,Category>();
                        Map<String,Category> serviceCategories = new HashMap<String,Category>();
                        upsertBizHosts(dtoHosts, hosts, hostGroups, hostCategories, devices, services, serviceGroups,
                                serviceCategories, true, results);
                        return results;
                    }

                    @Override
                    public boolean failed(Object result) {
                        return (((DtoOperationResults) result).getFailed() > 0);
                    }

                    @Override
                    public HibernateProgrammaticTxnSupport.RunInTxnRetry retryNotification(Object result, Exception exception) {
                        DtoOperationResults operationResults = (DtoOperationResults)result;
                        if ((operationResults != null) && (operationResults.getCount() == 1) && (operationResults.getSuccessful() == 0) && (dtoHosts.size() == 1)) {
                            return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETURN;
                        }
                        log.debug("Retrying upsert hosts and services: " + exception, exception);
                        return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETRY;
                    }

                    @Override
                    public Object retry() throws Exception {
                        // retry upsert hosts and services individually
                        DtoOperationResults results = new DtoOperationResults("Host", DtoOperationResults.UPDATE);
                        upsertBizHosts(dtoHosts, null, null, null, null, null, null, null, false, results);
                        return results;
                    }
                }, FlushMode.COMMIT);

        // Write out all influx perf data in a single shot to maximize transfer efficiency. This should be done after
        // the standard processing to ensure hosts/hostgroups/etc are all available.
        if (INFLUX_ENABLED && (dtoPerfDataList.size() > 0)) InfluxDBClient.write(dtoPerfDataList);

        stopMetricsTimer(timer);
        return results;
    }

    /**
     * Upsert hosts transaction. Host group, host category, device, service group, and
     * service category caches must be specified if run as a single transaction with Hibernate
     * session flushing disabled. This is required since new host groups, host categories,
     * devices, services, service groups, and service categories will not be available by query
     * within the transaction. Caching for hosts and services assume consistent naming will be
     * used for hosts.
     *
     * @param dtoHosts hosts to upsert
     * @param hosts hosts transaction cache or null
     * @param hostGroups host groups transaction cache or null
     * @param hostCategories host categories transaction cache or null
     * @param devices devices transaction cache or null
     * @param services services transaction cache or null
     * @param serviceGroups service groups transaction cache or null
     * @param serviceCategories service categories transaction cache or null
     * @param abortOnFailure abort transaction on failure
     * @param results operation results
     */
    private void upsertBizHosts(DtoBizHostList dtoHosts, Map<String,Host> hosts, Map<String,HostGroup> hostGroups,
                                Map<String,Category> hostCategories, Map<String,Device> devices,
                                Map<String,ServiceStatus> services, Map<String,Category> serviceGroups,
                                Map<String,Category> serviceCategories,
                                boolean abortOnFailure, DtoOperationResults results) {
        BizServices biz = (BizServices) CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);

        // This is a very basic attempt to process data differently based on where it comes from.  Long-term this should
        // most likely come from a proper configuration framework.
        String appName = this.request.getHeader(GWRestConstants.PARAM_GWOS_APP_NAME);
        boolean processLogPerf = LOGPERF_APPS.contains(appName);

        for (DtoBizHost host : dtoHosts.getHosts()) {
            try {
                scrubHost(host);
                if (host.getHost() == null) {
                    results.fail("(Unknown)", "No Host Identification provided");
                    if (abortOnFailure) {
                        return;
                    } else {
                        continue;
                    }
                }
                // create or update host
                boolean [] hostCreated = new boolean[]{false};
                Host updatedHost = biz.createOrUpdateHost(host.getHost(), host.getStatus(), host.getMessage(),
                        host.getHostGroup(), host.getHostCategory(), host.getDevice(), host.getAppType(), host.getAgentId(),
                        host.getCheckIntervalMinutes(), host.isAllowInserts(), host.isMergeHosts(),
                        host.isSetStatusOnCreate(), hosts, hostGroups, hostCategories, devices,
                        host.getProperties(), hostCreated);
                if (updatedHost != null) {
                    results.success(host.getHost(), buildResourceLocator(uriInfo, HostResource.RESOURCE_PREFIX,
                            updatedHost.getHostName()),
                            (hostCreated[0] ? DtoOperationResults.INSERT : DtoOperationResults.UPDATE));
                    // handle crushed metrics services special case: inflate
                    // missing services to ensure service updates are made
                    if (!updatedHost.getServiceStatuses().isEmpty() && host.getServices().isEmpty() &&
                            CRUSHED_HOST_SERVICES_STATUS_MAP.containsKey(host.getStatus())) {
                        String inflatedServiceStatus = CRUSHED_HOST_SERVICES_STATUS_MAP.get(host.getStatus());
                        for (ServiceStatus serviceStatus : (Set<ServiceStatus>)updatedHost.getServiceStatuses()) {
                            // inflate services with crushed services status
                            DtoBizService inflatedService = new DtoBizService();
                            inflatedService.setHost(host.getHost());
                            inflatedService.setService(serviceStatus.getServiceDescription());
                            inflatedService.setStatus(inflatedServiceStatus);
                            // pickup host defaults
                            inflatedService.setMessage("");
                            inflatedService.setHostGroup("");
                            inflatedService.setDevice("");
                            inflatedService.setAppType("");
                            inflatedService.setAgentId("");
                            inflatedService.setCheckIntervalMinutes(host.getCheckIntervalMinutes());
                            inflatedService.setAllowInserts(host.isAllowInserts());
                            inflatedService.setMergeHosts(host.isMergeHosts());
                            host.add(inflatedService);
                        }
                    }
                    // create or update host services
                    for (DtoBizService service : host.getServices()) {
                        String entity = host.getHost() + ":" + service.getService();
                        try {
                            if (service.getService() == null) {
                                results.fail("(Unknown)", "Service description is a required field to update a service status");
                                if (abortOnFailure) {
                                    return;
                                } else {
                                    continue;
                                }
                            }
                            String message = ((service.getMessage().length() > 0) ? service.getMessage() : host.getMessage());
                            String hostGroup = ((service.getHostGroup().length() > 0) ? service.getHostGroup() : host.getHostGroup());
                            String device = ((service.getDevice().length() > 0) ? service.getDevice() : host.getDevice());
                            String appType = ((service.getAppType().length() > 0) ? service.getAppType() : host.getAppType());
                            String agentId = ((service.getAgentId().length() > 0) ? service.getAgentId() : host.getAgentId());
                            boolean mergeHosts = (service.isMergeHosts() && host.isMergeHosts());
                            boolean [] serviceCreated = new boolean[]{false};
                            ServiceStatus updatedService = biz.createOrUpdateHostService(updatedHost, host.getHost(),
                                    service.getService(), service.getStatus(), message, service.getServiceGroup(),
                                    service.getServiceCategory(), hostGroup, device, appType, agentId,
                                    service.getCheckIntervalMinutes(), mergeHosts, service.isSetStatusOnCreate(),
                                    service.getServiceValue(), service.getWarningLevel(), service.getCriticalLevel(),
                                    service.getMetricType(), services, serviceGroups, serviceCategories,
                                    service.getProperties(), serviceCreated, processLogPerf);
                            if (updatedService != null) {
                                results.success(entity, buildResourceLocatorWithQueryParam(uriInfo, ServiceResource.RESOURCE_PREFIX,
                                        updatedService.getServiceDescription(), "hostName", updatedHost.getHostName()),
                                        (serviceCreated[0] ? DtoOperationResults.INSERT : DtoOperationResults.UPDATE));
                            } else {
                                results.warn(entity, "Service host not merged, (host exists)");
                            }
                        } catch (BusinessServiceException e) {
                            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
                            results.fail(entity, e.getMessage());
                            if (abortOnFailure) {
                                return;
                            }
                        }
                    }
                } else {
                    results.warn(host.getHost(), "Host not merged or inserted");
                }
            } catch (BusinessServiceException e) {
                log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
                results.fail(host.getHost(), e.getMessage());
                if (abortOnFailure) {
                    return;
                }
            }
        }
    }

    @POST
    @Path("/service")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    /**
     *  This call would check if the Host exists. If it doesn't exist the host entity would be created and an initial event (PENDING) will be created.
     *  If the hostGroup is not null the hostGroup will be created if it doesn't exist. Host will be assigned to the HostGroup.
     *  If the host exists status and message will be updated.
     *  Method would also retrieve the current host status and if a state change occurred an Event and a Notification will be generated.
     */
    public DtoService createOrUpdateService (
            @FormParam(BizFormParameters.PARAM_HOST) @DefaultValue("") String host,
            @FormParam(BizFormParameters.PARAM_SERVICE) @DefaultValue("") String service,
            @FormParam(BizFormParameters.PARAM_STATUS) @DefaultValue("") String status,
            @FormParam(BizFormParameters.PARAM_MESSAGE) @DefaultValue("") String message,
            @FormParam(BizFormParameters.PARAM_SERVICE_GROUP) @DefaultValue("") String serviceGroup,
            @FormParam(BizFormParameters.PARAM_SERVICE_CATEGORY) @DefaultValue("") String serviceCategory,
            @FormParam(BizFormParameters.PARAM_HOST_GROUP) @DefaultValue("") String hostGroup,
            @FormParam(BizFormParameters.PARAM_HOST_CATEGORY) @DefaultValue("") String hostCategory,
            @FormParam(BizFormParameters.PARAM_DEVICE) @DefaultValue("") String device,
            @FormParam(BizFormParameters.PARAM_APP_TYPE) @DefaultValue("") String appType,
            @FormParam(BizFormParameters.PARAM_AGENT_ID) @DefaultValue("") String agentId,
            @FormParam(BizFormParameters.PARAM_CHECK_INTERVAL_MINUTES) @DefaultValue("5") Integer checkIntervalMinutes,
            @FormParam(BizFormParameters.PARAM_ALLOW_INSERTS) @DefaultValue("TRUE") Boolean allowInserts,
            @FormParam(BizFormParameters.PARAM_MERGE_HOSTS) @DefaultValue("TRUE") Boolean mergeHosts,
            @FormParam(BizFormParameters.PARAM_SET_STATUS_ON_CREATE) @DefaultValue("FALSE") Boolean setStatusOnCreate,
            @FormParam(BizFormParameters.PARAM_SERVICE_VALUE) @DefaultValue("") String serviceValue,
            @FormParam(BizFormParameters.PARAM_WARNING_LEVEL) @DefaultValue("-1") Long warningLevel,
            @FormParam(BizFormParameters.PARAM_CRITICAL_LEVEL) @DefaultValue("-1") Long criticalLevel,
            @FormParam(BizFormParameters.PARAM_METRIC_TYPE) @DefaultValue("") String metricType,
            @Context ServletRequest request
    ) {
        CollageTimer timer = startMetricsTimer();
        host = host.trim();
        service = service.trim();
        status = status.trim();
        message = message.trim();
        device = device.trim();
        serviceGroup = serviceGroup.trim();
        serviceCategory = serviceCategory.trim();
        hostGroup = hostGroup.trim();
        hostCategory = hostCategory.trim();
        appType = appType.trim();
        agentId = agentId.trim();
        metricType = metricType.trim();

        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /biz/host with host = %s", host));
        }
        BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);

        // This is a very basic attempt to process data differently based on where it comes from.  Long-term this should
        // most likely come from a proper configuration framework.
        String appName = this.request.getHeader(GWRestConstants.PARAM_GWOS_APP_NAME);
        boolean processLogPerf = LOGPERF_APPS.contains(appName);

        try {
            ServiceStatus updatedService = biz.createOrUpdateService(
                    host, service, status, message, serviceGroup, serviceCategory,
                    hostGroup, hostCategory, device, appType, agentId,
                    checkIntervalMinutes, allowInserts, mergeHosts, setStatusOnCreate,
                    serviceValue, warningLevel, criticalLevel,
                    metricType, null, null, processLogPerf);
            if (updatedService == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Service host not merged or inserted: " + host + ":" + service).build());
            }
            // The "isGraphed" dynamic property is not available here, unlike with /services and /hosts, so assume that
            // the values provided should be recorded
            if (INFLUX_ENABLED && StringUtils.isNotBlank(serviceValue)) {
                DtoPerfData dtoPerfData = new DtoPerfData();
                dtoPerfData.setAppType(appType);
                dtoPerfData.setServerName(host);
                dtoPerfData.setServerTime(System.currentTimeMillis() / 1000L);
                dtoPerfData.setServiceName(service);
                dtoPerfData.setLabel(service);
                dtoPerfData.setValue(serviceValue);
                if (warningLevel != -1) {
                    dtoPerfData.setWarning(Long.toString(warningLevel));
                }
                if (criticalLevel != -1) {
                    dtoPerfData.setCritical(Long.toString(criticalLevel));
                }
                InfluxDBClient.write(Collections.singletonList(dtoPerfData));
            }
            return ServiceConverter.convert(updatedService, DtoDepthType.Shallow);
        }
        catch (BusinessServiceException e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for services.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Path("/services")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults upsertBizServices(final DtoBizServiceList dtoServices) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /biz/services with %d services",
                    (dtoServices == null) ? 0 : dtoServices.size()));
        }
        if (dtoServices == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Host list was not provided").build());
        }
        if (dtoServices.size() == 0) {
            return new DtoOperationResults("ServiceStatus", DtoOperationResults.UPDATE);
        }

        List<DtoPerfData> dtoPerfDataList = new ArrayList<>();
        // Build the dto list for influx here, using the dynamic "isGraphed" property before it is stripped off
        if (INFLUX_ENABLED) {
            this.addServicesToPerformanceData(dtoServices.getServices(), dtoPerfDataList);
        }

        // try to upsert services in one transaction: disable Hibernate session flush
        // and retry individually if single transaction fails
        DtoOperationResults results = (DtoOperationResults) HibernateProgrammaticTxnSupport.executeInTxn(
                new HibernateProgrammaticTxnSupport.RunInTxnAdapter() {
                    @Override
                    public Object run() throws Exception {
                        // upsert services transactionally; caching for hosts and services assume consistent
                        // naming will be used for hosts within the transaction.
                        DtoOperationResults results = new DtoOperationResults("ServiceStatus", DtoOperationResults.UPDATE);
                        Map<String,HostGroup> hostGroups = new HashMap<String,HostGroup>();
                        Map<String,Category> hostCategories = new HashMap<String,Category>();
                        Map<String,Host> hosts = new HashMap<String,Host>();
                        Map<String,Device> devices = new HashMap<String,Device>();
                        Map<String,ServiceStatus> services = new HashMap<String,ServiceStatus>();
                        Map<String,Category> serviceGroups = new HashMap<String,Category>();
                        Map<String,Category> serviceCategories = new HashMap<String,Category>();
                        upsertBizServices(dtoServices, hostGroups, hostCategories, hosts, devices, services,
                                serviceGroups, serviceCategories, true, results);
                        return results;
                    }

                    @Override
                    public boolean failed(Object result) {
                        return (((DtoOperationResults) result).getFailed() > 0);
                    }

                    @Override
                    public HibernateProgrammaticTxnSupport.RunInTxnRetry retryNotification(Object result, Exception exception) {
                        if ((result != null) && (((DtoOperationResults)result).getCount() == 1) && (dtoServices.size() == 1)) {
                            return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETURN;
                        }
                        log.debug("Retrying upsert services: " + exception, exception);
                        return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETRY;
                    }

                    @Override
                    public Object retry() throws Exception {
                        // retry upsert services individually
                        DtoOperationResults results = new DtoOperationResults("ServiceStatus", DtoOperationResults.UPDATE);
                        upsertBizServices(dtoServices, null, null, null, null, null, null, null, false, results);
                        return results;
                    }
                }, FlushMode.COMMIT);

        // Write out all influx perf data in a single shot to maximize transfer efficiency. This should be done after
        // the standard processing to ensure hosts/hostgroups/etc are all available.
        if (INFLUX_ENABLED && (dtoPerfDataList.size() > 0)) InfluxDBClient.write(dtoPerfDataList);

        stopMetricsTimer(timer);
        return results;
    }

    /**
     * Upsert services transaction. Host group, host category, host, device, service group and
     * service category caches must be specified if run as a single transaction with Hibernate
     * session flushing disabled. This is required since new host groups, host categories, hosts,
     * devices, services, service groups, and service categories will not be available by query
     * within the transaction. Caching for hosts and services assume consistent naming will be
     * used for hosts.
     *
     * @param dtoServices services to upsert
     * @param hostGroups host groups transaction cache or null
     * @param hostCategories host categories transaction cache or null
     * @param hosts hosts transaction cache or null
     * @param devices devices transaction cache or null
     * @param services services transaction cache or null
     * @param serviceGroups service groups transaction cache or null
     * @param serviceCategories service categories transaction cache or null
     * @param abortOnFailure abort transaction on failure
     * @param results operation results
     */
    private void upsertBizServices(DtoBizServiceList dtoServices, Map<String,HostGroup> hostGroups,
                                   Map<String,Category> hostCategories, Map<String,Host> hosts,
                                   Map<String,Device> devices, Map<String,ServiceStatus> services,
                                   Map<String,Category> serviceGroups, Map<String,Category> serviceCategories,
                                   boolean abortOnFailure, DtoOperationResults results) {
        BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);
        Set<String> updatedHosts = new HashSet<String>();

        // This is a very basic attempt to process data differently based on where it comes from.  Long-term this should
        // most likely come from a proper configuration framework.
        String appName = this.request.getHeader(GWRestConstants.PARAM_GWOS_APP_NAME);
        boolean processLogPerf = LOGPERF_APPS.contains(appName);

        for (DtoBizService service : dtoServices.getServices()) {
            scrubService(service);
            String entity = service.getHost() + ":" + service.getService();
            try {
                if (service.getHost() == null || service.getService() == null) {
                    results.fail("(Unknown)", "Host name and Service description are required fields to update a service status");
                    if (abortOnFailure) {
                        return;
                    } else {
                        continue;
                    }
                }
                boolean [] hostCreated = new boolean[]{false};
                boolean [] serviceCreated = new boolean[]{false};
                ServiceStatus updatedService = biz.createOrUpdateService(
                        service.getHost(), service.getService(), service.getStatus(),
                        service.getMessage(), service.getServiceGroup(), service.getServiceCategory(),
                        service.getHostGroup(), service.getHostCategory(), service.getDevice(),
                        service.getAppType(), service.getAgentId(),
                        service.getCheckIntervalMinutes(), service.isAllowInserts(), service.isMergeHosts(),
                        service.isSetStatusOnCreate(),
                        service.getServiceValue(), service.getWarningLevel(), service.getCriticalLevel(),
                        service.getMetricType(),
                        hostGroups, hostCategories, hosts, devices, services, serviceGroups, serviceCategories,
                        service.getProperties(), hostCreated, serviceCreated, processLogPerf);
                if (updatedService != null) {
                    if (updatedHosts.add(service.getHost())) {
                        results.success(service.getHost(), buildResourceLocator(uriInfo, HostResource.RESOURCE_PREFIX,
                                updatedService.getHost().getHostName()),
                                (hostCreated[0] ? DtoOperationResults.INSERT : DtoOperationResults.UPDATE));
                    }
                    results.success(entity, buildResourceLocatorWithQueryParam(uriInfo, ServiceResource.RESOURCE_PREFIX,
                            updatedService.getServiceDescription(), "hostName", updatedService.getHost().getHostName()),
                            (serviceCreated[0] ? DtoOperationResults.INSERT : DtoOperationResults.UPDATE));
                } else {
                    results.warn(entity, "Service host not merged or inserted");
                }
            }
            catch (BusinessServiceException e) {
                log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
                results.fail(entity, e.getMessage());
                if (abortOnFailure) {
                    return;
                }
            }
        }
    }

    private void scrubHost(DtoBizHost host) {
        host.setHost(host.getHost() == null ? "" : host.getHost().trim());
        host.setStatus(host.getStatus() == null ? "" : host.getStatus().trim());
        host.setMessage(host.getMessage() == null ? "" : host.getMessage().trim());
        host.setDevice(host.getDevice() == null ? "" : host.getDevice().trim());
        host.setHostGroup(host.getHostGroup() == null ? "" : host.getHostGroup().trim());
        host.setHostCategory(host.getHostCategory() == null ? "" : host.getHostCategory().trim());
        host.setAppType(host.getAppType() == null ? "" : host.getAppType().trim());
        host.setAgentId(host.getAgentId() == null ? "" : host.getAgentId().trim());
        for (DtoBizService service : host.getServices()) {
            scrubService(service);
        }
    }

    private void scrubService(DtoBizService service) {
        service.setHost(service.getHost() == null ? "" : service.getHost().trim());
        service.setService(service.getService() == null ? "" : service.getService().trim());
        service.setStatus(service.getStatus() == null ? "" : service.getStatus().trim());
        service.setMessage(service.getMessage() == null ? "" : service.getMessage().trim());
        service.setServiceGroup(service.getServiceGroup() == null ? "" : service.getServiceGroup().trim());
        service.setServiceCategory(service.getServiceCategory() == null ? "" : service.getServiceCategory().trim());
        service.setDevice(service.getDevice() == null ? "" : service.getDevice().trim());
        service.setHostGroup(service.getHostGroup() == null ? "" : service.getHostGroup().trim());
        service.setHostCategory(service.getHostCategory() == null ? "" : service.getHostCategory().trim());
        service.setAppType(service.getAppType() == null ? "" : service.getAppType().trim());
        service.setAgentId(service.getAgentId() == null ? "" : service.getAgentId().trim());
    }


    @POST
    @Path("/setindowntime")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoBizHostServiceInDowntimeList setInDowntime(DtoBizHostsAndServices dtoHostsAndServices) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /POST on /biz/setindowntime with %d host names, %d service descriptions, %d host groups, and %d service groups; set hosts %b; set services %b",
                        (((dtoHostsAndServices == null) || (dtoHostsAndServices.getHostNames() != null)) ? 0 : dtoHostsAndServices.getHostNames().size()),
                        (((dtoHostsAndServices == null) || (dtoHostsAndServices.getServiceDescriptions() != null)) ? 0 : dtoHostsAndServices.getServiceDescriptions().size()),
                        (((dtoHostsAndServices == null) || (dtoHostsAndServices.getHostGroupNames() != null)) ? 0 : dtoHostsAndServices.getHostGroupNames().size()),
                        (((dtoHostsAndServices == null) || (dtoHostsAndServices.getServiceGroupCategoryNames() != null)) ? 0 : dtoHostsAndServices.getServiceGroupCategoryNames().size()),
                        ((dtoHostsAndServices == null) ? false : dtoHostsAndServices.isSetHosts()),
                        ((dtoHostsAndServices == null) ? false : dtoHostsAndServices.isSetServices())));
            }
            if (dtoHostsAndServices == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Hosts and services not provided").build());
            }
            BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);
            List<String> hostNames = dtoHostsAndServices.getHostNames();
            List<String> serviceDescriptions = dtoHostsAndServices.getServiceDescriptions();
            List<String> hostGroupNames = dtoHostsAndServices.getHostGroupNames();
            List<String> serviceGroupCategoryNames = dtoHostsAndServices.getServiceGroupCategoryNames();
            boolean setHosts = dtoHostsAndServices.isSetHosts();
            boolean setServices = dtoHostsAndServices.isSetServices();
            // use parameter conventions to determine whether hosts and/or services are set in downtime
            if (!setHosts && !setServices) {
                // validate conventional usage
                if (isParamWildcard(hostGroupNames) || isParamWildcard(serviceGroupCategoryNames)) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Convention: host and service groups cannot be wildcard").build());
                }
                if (isParamEmpty(hostNames) && isParamEmpty(serviceDescriptions)) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Convention: either host names or service descriptions are required").build());
                }
                if (isParamEmptyOrWildcard(hostNames) && isParamEmptyOrWildcard(serviceDescriptions) &&
                        isParamEmpty(hostGroupNames) && isParamEmpty(serviceGroupCategoryNames)) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Convention: empty or wildcard host names and service descriptions require host or service groups").build());
                }
                if (!isParamEmptyOrWildcard(hostNames) &&
                        (!isParamEmpty(hostGroupNames) || !isParamEmpty(serviceGroupCategoryNames))) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Convention: host or service groups cannot be specified with host names").build());
                }
                // set host and service downtime properties by convention
                if (isParamWildcard(hostNames)) {
                    if (isParamWildcard(serviceDescriptions)) {
                        setHosts = true;
                        setServices = true;
                    } else if (isParamEmpty(serviceDescriptions)) {
                        setHosts = true;
                    }
                } else if (isParamEmpty(hostNames)) {
                    if (isParamWildcard(serviceDescriptions)) {
                        setServices = true;
                    }
                } else {
                    if (isParamWildcard(serviceDescriptions)) {
                        setServices = true;
                    } else if (isParamEmpty(serviceDescriptions)) {
                        setHosts = true;
                    } else {
                        setServices = true;
                    }
                }
                if (!setHosts && !setServices) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Convention: not setting host or service downtime").build());
                }
            }
            List<BizServices.HostServiceInDowntime> hostServiceInDowntimes = biz.setHostsAndServicesInDowntime(hostNames, serviceDescriptions, hostGroupNames, serviceGroupCategoryNames, setHosts, setServices);
            return convertHostServiceInDowntimes(hostServiceInDowntimes);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for biz downtime services.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Check empty or wildcard parameter.
     *
     * @param param parameter
     * @return parameter disposition
     */
    private static boolean isParamEmptyOrWildcard(List<String> param) {
        return ((param == null) || param.isEmpty() || ((param.size() == 1) && WILDCARD.equals(param.get(0))));
    }

    /**
     * Check wildcard parameter.
     *
     * @param param parameter
     * @return parameter disposition
     */
    private static boolean isParamWildcard(List<String> param) {
        return ((param != null) && (param.size() == 1) && WILDCARD.equals(param.get(0)));
    }

    /**
     * Check empty parameter.
     *
     * @param param parameter
     * @return parameter disposition
     */
    private static boolean isParamEmpty(List<String> param) {
        return ((param == null) || param.isEmpty());
    }

    @POST
    @Path("/clearindowntime")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoBizHostServiceInDowntimeList clearInDowntime(DtoBizHostServiceInDowntimeList dtoHostsAndServicesInDowntime) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /POST on /biz/clearindowntime with %d host and services", ((dtoHostsAndServicesInDowntime == null) ? 0 : dtoHostsAndServicesInDowntime.size())));
            }
            if (dtoHostsAndServicesInDowntime == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Hosts and services not provided").build());
            }
            List<BizServices.HostServiceInDowntime> hostServiceInDowntimes = convertDtoHostServiceInDowntimeList(dtoHostsAndServicesInDowntime);
            BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);
            hostServiceInDowntimes = biz.clearHostsAndServicesInDowntime(hostServiceInDowntimes);
            return convertHostServiceInDowntimes(hostServiceInDowntimes);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for biz downtime services.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Path("/getindowntime")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoBizHostServiceInDowntimeList getInDowntime(DtoBizHostServiceInDowntimeList dtoHostsAndServicesInDowntime) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /POST on /biz/clearindowntime with %d host and services", ((dtoHostsAndServicesInDowntime == null) ? 0 : dtoHostsAndServicesInDowntime.size())));
            }
            if (dtoHostsAndServicesInDowntime == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Hosts and services not provided").build());
            }
            List<BizServices.HostServiceInDowntime> hostServiceInDowntimes = convertDtoHostServiceInDowntimeList(dtoHostsAndServicesInDowntime);
            BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);
            hostServiceInDowntimes = biz.getHostsAndServicesInDowntime(hostServiceInDowntimes);
            return convertHostServiceInDowntimes(hostServiceInDowntimes);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for biz downtime services.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    private static List<BizServices.HostServiceInDowntime> convertDtoHostServiceInDowntimeList(DtoBizHostServiceInDowntimeList dtoHostServiceInDowntimeList) {
        List<BizServices.HostServiceInDowntime> hostServiceInDowntimes = new ArrayList<BizServices.HostServiceInDowntime>();
        for (DtoBizHostServiceInDowntime dtoHostServiceInDowntime : dtoHostServiceInDowntimeList.getBizHostServiceInDowntimes()) {
            hostServiceInDowntimes.add(BizHostServiceInDowntimeConverter.convert(dtoHostServiceInDowntime));
        }
        return hostServiceInDowntimes;
    }

    private static DtoBizHostServiceInDowntimeList convertHostServiceInDowntimes(List<BizServices.HostServiceInDowntime> hostServiceInDowntimes) {
        DtoBizHostServiceInDowntimeList dtoHostServiceInDowntimeList = new DtoBizHostServiceInDowntimeList();
        for (BizServices.HostServiceInDowntime hostServiceInDowntime : hostServiceInDowntimes) {
            dtoHostServiceInDowntimeList.add(BizHostServiceInDowntimeConverter.convert(hostServiceInDowntime));
        }
        return dtoHostServiceInDowntimeList;
    }

    @GET
    @Path("/getauthorizedservices")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoBizAuthorizedServices getAuthorizedServices() {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /biz/getauthorizedservices"));
            }
            BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);
            BizServices.AuthorizedServices authorizedServices = biz.getAuthorizedServices();
            if (authorizedServices == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("No authorization").build());
            }
            return new DtoBizAuthorizedServices(authorizedServices.hostNames, authorizedServices.serviceHostNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for biz authorization services.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Path("/getauthorizedservices")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoBizAuthorizedServices getAuthorizedServices(DtoBizAuthorization dtoAuthorization) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                int hostGroups = (((dtoAuthorization != null) && (dtoAuthorization.getHostGroupNames() != null)) ? dtoAuthorization.getHostGroupNames().size() : 0);
                int serviceGroups = (((dtoAuthorization != null) && (dtoAuthorization.getServiceGroupNames() != null)) ? dtoAuthorization.getServiceGroupNames().size() : 0);
                log.debug(String.format("processing /POST on /biz/getauthorizedservices with %d host groups and %d service groups", hostGroups, serviceGroups));
            }
            if (dtoAuthorization == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Authorization not specified").build());
            }
            BizServices biz = (BizServices)CollageFactory.getInstance().getAPIObject(BizServices.SERVICE);
            BizServices.AuthorizedServices authorizedServices = biz.getAuthorizedServices(dtoAuthorization.getHostGroupNames(), dtoAuthorization.getServiceGroupNames());
            if (authorizedServices == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("No authorization").build());
            }
            return new DtoBizAuthorizedServices(authorizedServices.hostNames, authorizedServices.serviceHostNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for biz authorization services.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    private boolean isFalse(String value) {
        return ((value != null) && !BooleanUtils.toBoolean(value));
    }

    /**
     * Build out the performance data list from a Biz Service List of services
     *
     * @param dtoServices
     * @param dtoPerfDataList
     */
    private void addServicesToPerformanceData(List<DtoBizService> dtoServices, List<DtoPerfData> dtoPerfDataList) {
        for (DtoBizService service : dtoServices) {
            // Skip processing if the service value is blank
            if (StringUtils.isBlank(service.getServiceValue()) || isFalse(service.getProperty(ServiceStatus.TP_IS_GRAPHED))) continue;
            DtoPerfData dtoPerfData = new DtoPerfData();
            dtoPerfData.setAppType(service.getAppType());
            dtoPerfData.setServerName(service.getHost());
            dtoPerfData.setServerTime(System.currentTimeMillis() / 1000L);
            dtoPerfData.setServiceName(service.getService());
            dtoPerfData.setLabel(service.getService());
            dtoPerfData.setValue(service.getServiceValue());
            if (service.getWarningLevel() != -1) {
                dtoPerfData.setWarning(Long.toString(service.getWarningLevel()));
            }
            if (service.getCriticalLevel() != -1) {
                dtoPerfData.setCritical(Long.toString(service.getCriticalLevel()));
            }
            dtoPerfDataList.add(dtoPerfData);
        }
    }
    
}
