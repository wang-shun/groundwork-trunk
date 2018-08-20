package org.groundwork.rs.tasks;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminInfrastructureUtils;
import com.groundwork.collage.HibernateProgrammaticTxnSupport;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.async.RestTransaction;
import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.hibernate.FlushMode;

import java.util.HashMap;
import java.util.Map;

public class HostCreateTask extends AbstractRestTask implements RestRequestTask {
    protected static Log log = LogFactory.getLog(HostCreateTask.class);

    private final DtoHostList dtoHosts;
    private final boolean mergeHosts;

    public HostCreateTask(String name, DtoHostList dtoHosts, boolean mergeHosts, String uriTemplate) {
        super(name, uriTemplate);
        this.dtoHosts = dtoHosts;
        this.mergeHosts = mergeHosts;
    }

    @Override
    public RestRequestResult call() throws Exception {
        RestTransaction session = new RestTransaction();
        session.startTransaction();
        DtoOperationResults results = upsertHosts();
        session.releaseSession();
        return new RestRequestResult(results, this, true, 0, false);
    }

    public DtoOperationResults upsertHosts() {
        // return if no hosts specified to upsert
        if (dtoHosts.size() == 0) {
            return new DtoOperationResults("Host", DtoOperationResults.UPDATE);
        }

        // try to upsert hosts in one transaction: disable Hibernate session flush
        // and retry individually if single transaction fails
        DtoOperationResults results = (DtoOperationResults) HibernateProgrammaticTxnSupport.executeInTxn(
                new HibernateProgrammaticTxnSupport.RunInTxnAdapter() {
                    @Override
                    public Object run() throws Exception {
                        // upsert hosts transactionally
                        DtoOperationResults results = new DtoOperationResults("Host", DtoOperationResults.UPDATE);
                        Map<String,Host> hosts = new HashMap<String,Host>();
                        Map<String,Device> devices = new HashMap<String,Device>();
                        upsertHosts(hosts, devices, true, results);
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
                        log.debug("Retrying upsert hosts: " + exception, exception);
                        return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETRY;
                    }

                    @Override
                    public Object retry() throws Exception {
                        // retry upsert hosts individually
                        DtoOperationResults results = new DtoOperationResults("Host", DtoOperationResults.UPDATE);
                        upsertHosts(null, null, false, results);
                        return results;
                    }
                }, FlushMode.COMMIT);
        return results;
    }

    /**
     * Upsert host transaction. Host and device cache must be specified if run as a single
     * transaction with Hibernate session flushing disabled. This is required since new hosts
     * and devices will not be available by query within the transaction.
     *
     * @param hosts hosts transaction cache or null
     * @param devices devices transaction cache or null
     * @param abortOnFailure abort transaction on failure
     * @param results operation results
     */
    private void upsertHosts(Map<String,Host> hosts, Map<String,Device> devices, boolean abortOnFailure, DtoOperationResults results) {

        long start = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Starting create hosts " + this.dtoHosts.size());
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoHost dtoHost : dtoHosts.getHosts()) {
            // validate host
            String hostName = dtoHost.getHostName();
            if (hostName == null) {
                results.fail("unknown host", "failed to find hostname property");
                if (abortOnFailure) {
                    return;
                } else {
                    continue;
                }
            }
            try {
                // lookup host and device in cache
                Host updatingHost = ((hosts != null) ? hosts.get(hostName.toLowerCase()) : null);
                String deviceIdentification = dtoHost.getDeviceIdentification();
                Device device = (((devices != null) && (deviceIdentification != null)) ? devices.get(deviceIdentification) : null);
                // add or update host and cache host and device
                Map<String, String> properties = createHostPropertiesFromDto(dtoHost);
                Host updatedHost = CollageAdminInfrastructureUtils.updateHost(updatingHost, device, properties, mergeHosts, admin);
                if (updatedHost != null) {
                    if (devices != null) {
                        device = updatedHost.getDevice();
                        devices.put(deviceIdentification, device);
                    }
                    if (hosts != null) {
                        hosts.put(hostName.toLowerCase(), updatedHost);
                    }
                    // save success result
                    results.success(dtoHost.getHostName(), buildResourceLocator(dtoHost.getHostName()));
                } else {
                    // not updated/merged warning
                    results.warn(dtoHost.getHostName(), "host not merged");
                }
            } catch (Exception e) {
                log.error("Failed to upsert host: " + e, e);
                // save failed result
                results.fail(dtoHost.getHostName(), e.getMessage());
                if (abortOnFailure) {
                    return;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Completing create hosts in " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    private Map<String, String> createHostPropertiesFromDto(DtoHost dtoHost) {
        if (dtoHost.getAppType() == null) {
            dtoHost.setAppType(DtoApplicationType.DEFAULT_APP_TYPE);
        }
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CollageAdminInfrastructure.PROP_HOST_NAME, dtoHost.getHostName());
        if (dtoHost.getDescription() != null)
            properties.put(CollageAdminInfrastructure.PROP_DESCRIPTION, dtoHost.getDescription());
        if (dtoHost.getDeviceIdentification() != null)
            properties.put(CollageAdminInfrastructure.PROP_DEVICE_IDENTIFICATION, dtoHost.getDeviceIdentification());
        if (dtoHost.getDeviceDisplayName() != null)
            properties.put(CollageAdminInfrastructure.PROP_DISPLAY_NAME, dtoHost.getDeviceDisplayName());
        if (dtoHost.getMonitorServer() != null)
            properties.put(CollageAdminInfrastructure.PROP_MONITOR_SERVER, dtoHost.getMonitorServer());
        if (dtoHost.getAppType() != null)
            properties.put(CollageAdminInfrastructure.PROP_APPLICATION_TYPE_NAME, dtoHost.getAppType());
        if (dtoHost.getMonitorStatus() != null)
            properties.put(CollageAdminInfrastructure.PROP_MONITOR_STATUS, dtoHost.getMonitorStatus());
        if (dtoHost.getAgentId() != null)
            properties.put(CollageAdminInfrastructure.PROP_AGENT_ID, dtoHost.getAgentId());
        if (dtoHost.getLastCheckTime() != null)
            properties.put(CollageAdminInfrastructure.PROP_SERVICE_LAST_CHECK_TIME, Long.toString(dtoHost.getLastCheckTime().getTime()));
        if (dtoHost.getNextCheckTime() != null)
            properties.put(CollageAdminInfrastructure.PROP_SERVICE_NEXT_CHECK_TIME, Long.toString(dtoHost.getNextCheckTime().getTime()));
        if (dtoHost.getStateType() != null)
            properties.put(CollageAdminInfrastructure.PROP_STATE_TYPE, dtoHost.getStateType());
        if (dtoHost.getCheckType() != null)
            properties.put(CollageAdminInfrastructure.PROP_CHECK_TYPE, dtoHost.getCheckType());
        properties.putAll(dtoHost.getProperties());
        return properties;
    }
}
