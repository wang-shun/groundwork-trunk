package org.groundwork.rs.tasks;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminInfrastructureUtils;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.HibernateProgrammaticTxnSupport;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.ServiceStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.async.RestTransaction;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;
import org.hibernate.FlushMode;

import java.util.HashMap;
import java.util.Map;

public class ServiceCreateTask extends AbstractRestTask implements RestRequestTask {

    private CollageMetrics collageMetrics;

    private CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = CollageFactory.getInstance().getCollageMetrics();
        }
        return collageMetrics;
    }

    public CollageTimer startMetricsTimer(String methodName) {
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer("ServiceCreateTask", methodName));
    }

    public void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
    }

    protected static Log log = LogFactory.getLog(ServiceCreateTask.class);

    private final DtoServiceList dtoServices;
    private final boolean mergeHosts;

    public ServiceCreateTask(String name, DtoServiceList dtoServices, boolean mergeHosts, String uriTemplate) {
        super(name, uriTemplate);
        this.dtoServices = dtoServices;
        this.mergeHosts = mergeHosts;
    }

    @Override
    public RestRequestResult call() throws Exception {
        RestTransaction session = new RestTransaction();
        session.startTransaction();
        DtoOperationResults results = createServices();
        session.releaseSession();
        return new RestRequestResult(results, this, true, 0, false);
    }

    public DtoOperationResults createServices() {
        // return if no services specified to create
        if (dtoServices.size() == 0) {
            return new DtoOperationResults("ServiceStatus", DtoOperationResults.UPDATE);
        }

        // try to create services in one transaction: disable Hibernate session flush
        // and retry individually if single transaction fails
        DtoOperationResults results = (DtoOperationResults) HibernateProgrammaticTxnSupport.executeInTxn(
                new HibernateProgrammaticTxnSupport.RunInTxnAdapter() {
                    @Override
                    public Object run() throws Exception {
                        // create services transactionally
                        DtoOperationResults results = new DtoOperationResults("ServiceStatus", DtoOperationResults.UPDATE);
                        Map<String,ServiceStatus> services = new HashMap<String,ServiceStatus>();
                        Map<String,Host> hosts = new HashMap<String,Host>();
                        Map<String,Device> devices = new HashMap<String,Device>();
                        createServices(services, hosts, devices, true, results);
                        return results;
                    }

                    @Override
                    public boolean failed(Object result) {
                        DtoOperationResults results1 = (DtoOperationResults)result;
                        if (results1.getFailed() > 0) {
                            DtoOperationResult res = ((DtoOperationResults) result).getResults().get(0);
                            log.error("Failed to execute create services: " + res.getMessage() + ", " + res.getStatus() + ", count: " + results1.getCount() + ", failed: " + results1.getFailed());
                        }
                        return (((DtoOperationResults) result).getFailed() > 0);
                    }

                    @Override
                    public HibernateProgrammaticTxnSupport.RunInTxnRetry retryNotification(Object result, Exception exception) {
                        log.debug("Retrying create services: " + exception, exception);
                        DtoOperationResults operationResults = (DtoOperationResults)result;
                        if ((operationResults != null) && (operationResults.getCount() == 1) && (operationResults.getSuccessful() == 0) && (dtoServices.size() == 1)) {
                            return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETURN;
                        }
                        log.debug("Retrying create services(2)");
                        return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETRY;
                    }

                    @Override
                    public Object retry() throws Exception {
                        // retry create services individually
                        log.debug("Retrying create services... ");
                        DtoOperationResults results = new DtoOperationResults("ServiceStatus", DtoOperationResults.UPDATE);
                        createServices(null, null, null, false, results);
                        return results;
                    }
                }, FlushMode.COMMIT);
        return results;
    }

    /**
     * Create services transaction. Service, host, and device caches must be specified if run
     * as a single transaction with Hibernate session flushing disabled. This is required since
     * new services, hosts, and devices will not be available by query within the transaction.
     *
     * @param services services transaction cache or null
     * @param hosts hosts transaction cache or null
     * @param devices devices transaction cache or null
     * @param abortOnFailure abort transaction on failure
     * @param results operation results
     */
    private void createServices(Map<String,ServiceStatus> services, Map<String,Host> hosts, Map<String,Device> devices, boolean abortOnFailure, DtoOperationResults results) {
        CollageTimer timer = startMetricsTimer("createServices");
        long start = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Starting create services " + this.dtoServices.size());
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoService dtoService : dtoServices.getServices()) {
            CollageTimer innerTimer = startMetricsTimer("createService");
            // validate service
            String hostName = dtoService.getHostName();
            String serviceDescription = dtoService.getDescription();
            if (hostName == null || serviceDescription == null) {
                String msgHost = (hostName == null) ?"hostName not set": hostName;
                String msgService = (serviceDescription == null) ? "serviceName not set" : serviceDescription;
                log.error("service or host name is not valid: " + msgHost + ", " + msgService);
                results.fail("(Unknown)", "Host name and Service description are required fields to update a service status");
                if (abortOnFailure) {
                    return;
                } else {
                    continue;
                }
            }
            String entity = hostName + ":" + serviceDescription;
            try {
                // lookup service, host, and device in caches
                ServiceStatus updatingService = ((services != null) ? services.get(entity) : null);
                Host updatingHost = ((hosts != null) ? hosts.get(hostName.toLowerCase()) : null);
                String deviceIdentification = dtoService.getDeviceIdentification();
                Device device = (((devices != null) && (deviceIdentification != null)) ? devices.get(deviceIdentification) : null);
                // add or update service status and cache service, host, and device
                Map<String, String> properties = createServicePropertiesFromDto(dtoService);
                ServiceStatus updatedService = CollageAdminInfrastructureUtils.updateService(updatingService,
                        dtoService.getMonitorServer(), dtoService.getAppType(), hostName, deviceIdentification,
                        dtoService.getAgentId(), updatingHost, device, properties, mergeHosts, admin);
                if (updatedService != null) {
                    if (devices != null) {
                        if (device == null) device = updatedService.getHost().getDevice();
                        devices.put(deviceIdentification, device);
                    }
                    if (hosts != null) {
                        if (updatingHost == null) updatingHost = updatedService.getHost();
                        hosts.put(hostName.toLowerCase(), updatingHost);
                    }
                    if (services != null) {
                        services.put(entity, updatedService);
                    }
                    // save success result
                    results.success(entity, buildResourceLocatorWithQueryParam(dtoService.getDescription(),
                            "hostName", dtoService.getHostName()));
                } else {
                    // not updated/merged warning
                    results.warn(entity, "service host not merged");
                }
            }
            catch (Exception e) {
                log.error("Failed to create Service Status: " + e.getMessage(), e);
                // save failed result
                results.fail(entity, e.toString());
                if (abortOnFailure) {
                    return;
                }
            } finally {
                stopMetricsTimer(innerTimer);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Completing create services in " + (System.currentTimeMillis() - start) + " ms");
        }
        stopMetricsTimer(timer);
    }

    private Map<String, String> createServicePropertiesFromDto(DtoService dtoService) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CollageAdminInfrastructure.PROP_SERVICE_DESC, dtoService.getDescription());
        if (dtoService.getAppType() != null) {
            properties.put(CollageAdminInfrastructure.PROP_APPLICATION_TYPE_NAME, dtoService.getAppType());
        }
        if (dtoService.getMonitorStatus() != null) {
            properties.put(CollageAdminInfrastructure.PROP_MONITOR_STATUS, dtoService.getMonitorStatus());
        }
        // additional properties
        if (dtoService.getStateType() != null)
            properties.put(CollageAdminInfrastructure.PROP_STATE_TYPE, dtoService.getStateType());
        if (dtoService.getCheckType() != null)
            properties.put(CollageAdminInfrastructure.PROP_CHECK_TYPE, dtoService.getCheckType());
        if (dtoService.getLastHardState() != null)
            properties.put(CollageAdminInfrastructure.PROP_LAST_HARD_STATE, dtoService.getLastHardState());
        if (dtoService.getDomain() != null)
            properties.put(CollageAdminInfrastructure.PROP_SERVICE_DOMAIN, dtoService.getDomain());
        if (dtoService.getMetricType() != null)
            properties.put(CollageAdminInfrastructure.PROP_SERVICE_METRIC_TYPE, dtoService.getMetricType());
        if (dtoService.getLastCheckTime() != null)
            properties.put(CollageAdminInfrastructure.PROP_SERVICE_LAST_CHECK_TIME, formatDate(dtoService.getLastCheckTime()));
        if (dtoService.getNextCheckTime() != null)
            properties.put(CollageAdminInfrastructure.PROP_SERVICE_NEXT_CHECK_TIME, formatDate(dtoService.getNextCheckTime()));
        if (dtoService.getLastStateChange() != null)
            properties.put(CollageAdminInfrastructure.PROP_SERVICE_LAST_STATE_CHANGE, formatDate(dtoService.getLastStateChange()));
        properties.putAll(dtoService.getProperties());
        return properties;
    }
}
