package com.groundwork.collage.biz;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminInfrastructureUtils;
import com.groundwork.collage.CollageCheckType;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.CollageSeverity;
import com.groundwork.collage.CollageState;
import com.groundwork.collage.CollageStatus;
import com.groundwork.collage.biz.notifications.NomaActions;
import com.groundwork.collage.biz.notifications.NomaHostNotification;
import com.groundwork.collage.biz.notifications.NomaServiceNotification;
import com.groundwork.collage.biz.performance.PerformanceNotification;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.DateTime;
import com.groundwork.collage.util.Nagios;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FoundationDAO;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The current version of the Foundation REST API provides CRUD access to all Foundation entities.
 * For an application developer that wants to communicate status to the foundation backend several calls are necessary
 * (initialization, state change verification, update) to make it work.
 * To simplify the integration task additional business services are necessary to abstract the low level calls.
 * The first step is to come up with specifications for calls that would accelerate the completion of connectors and bridges
 *
 * @since 7.1.0
 */
public class BizServicesImpl extends HibernateDaoSupport implements BizServices {

    protected static Log log = LogFactory.getLog(BizServicesImpl.class);

    public static final String DEFAULT_APP_TYPE = "NAGIOS";
    public static final String DEFAULT_MONITOR_SERVER = "localhost";
    public static final String DOWNTIME_APP_TYPE = "DOWNTIME";

    public static final String WILDCARD_HOST_NAME = "*";
    public static final String WILDCARD_SERVICE_DESCRIPTION = "*";

    public static final String START_DOWNTIME_MONITOR_STATUS = "START DOWNTIME";
    public static final String IN_DOWNTIME_MONITOR_STATUS = "IN DOWNTIME";
    public static final String END_DOWNTIME_MONITOR_STATUS = "END DOWNTIME";

    private final FoundationDAO foundationDAO;
    private final CollageAdminInfrastructure adminService;
    private final HostIdentityService hostIdentityService;
    private final HostService hostService;
    private final DeviceService deviceService;
    private final StatusService statusService;
    private final HostGroupService hostGroupService;
    private final LogMessageService logMessageService;
    private final CategoryService categoryService;
    private final MetadataService metadataService;
    private final PerformanceNotification performance;

    private final boolean sendJMSNotifications;
    private final boolean noConfiguredPerfWriters;

    private DateFormat dateFormat = new SimpleDateFormat(DateTime.DATE_FORMAT);

    private CollageMetrics collageMetrics;

    private CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = CollageFactory.getInstance().getCollageMetrics();
        }
        return collageMetrics;
    }

    public CollageTimer startMetricsTimer(String methodName) {
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer("BizServicesImpl", methodName));
    }

    public void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) collageMetrics.stopTimer(timer);
    }

    public BizServicesImpl(FoundationDAO foundationDAO,
                           CollageAdminInfrastructure adminService,
                           HostIdentityService hostIdentityService,
                           HostService hostService,
                           DeviceService deviceService,
                           StatusService statusService,
                           HostGroupService hostGroupService,
                           LogMessageService logMessageService,
                           CategoryService categoryService,
                           MetadataService metadataService) {
        this.foundationDAO = foundationDAO;
        this.adminService = adminService;
        this.hostIdentityService = hostIdentityService;
        this.hostService = hostService;
        this.deviceService = deviceService;
        this.statusService = statusService;
        this.hostGroupService = hostGroupService;
        this.logMessageService = logMessageService;
        this.categoryService = categoryService;
        this.metadataService = metadataService;
        this.performance = new PerformanceNotification();
        String bizTest = System.getProperties().getProperty("bizTest");
        this.sendJMSNotifications = (bizTest == null || bizTest.equalsIgnoreCase("false"));
        Properties properties = CollageFactory.getInstance().getFoundationProperties();
        this.noConfiguredPerfWriters = StringUtils.isBlank(properties.getProperty("perfdata.vema.writers", ""));
    }

    @Override
    public Host createOrUpdateHost(String host,
                                   String status,
                                   String message,
                                   String hostGroup,
                                   String hostCategory,
                                   String device,
                                   String appType,
                                   String agentId,
                                   Integer checkIntervalMinutes,
                                   Boolean allowInserts,
                                   Boolean mergeHosts,
                                   Boolean setStatusOnCreate,
                                   boolean [] created)
            throws BusinessServiceException {
        return createOrUpdateHost(host, status, message, hostGroup, hostCategory, device, appType, agentId,
                checkIntervalMinutes, allowInserts, mergeHosts, setStatusOnCreate, null, null, null, null, null,
                created);
    }

    @Override
    public Host createOrUpdateHost(String host,
                                   String status,
                                   String message,
                                   String hostGroup,
                                   String hostCategory,
                                   String device,
                                   String appType,
                                   String agentId,
                                   Integer checkIntervalMinutes,
                                   Boolean allowInserts,
                                   Boolean mergeHosts,
                                   Boolean setStatusOnCreate,
                                   Map<String,Host> hosts,
                                   Map<String,HostGroup> hostGroups,
                                   Map<String,Category> hostCategories,
                                   Map<String,Device> devices,
                                   Map<String,String> dynamicProperties,
                                   boolean [] created)
            throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer("createOrUpdateHost");

        if (isEmptyParameter(host))
            throw new BusinessServiceException("Invalid parameter, empty host");
        if (isEmptyParameter(message))
            throw new BusinessServiceException("Invalid parameter, empty message");

        if (checkIntervalMinutes == null)
            checkIntervalMinutes = 5;
        Date now = new Date();
        Date nextCheckTime = new Date(now.getTime() + (checkIntervalMinutes * 60 * 1000));

        if (allowInserts == null)
            allowInserts = true;
        if (mergeHosts == null)
            mergeHosts = true;
        if (setStatusOnCreate == null)
            setStatusOnCreate = false;

        boolean [] isNew = new boolean[]{false};
        boolean [] isOwner = new boolean[]{false};

        try {
            // #1: check if the host exists
            Host updatedHost = addOrUpdateHost(host, status, message, message, device, appType, agentId,
                    allowInserts, mergeHosts, now, nextCheckTime, isNew, isOwner, hosts, devices, dynamicProperties);
            if (updatedHost == null) {
                return null;
            }

            // #2 hostGroup
            /*
                GWMON-13138 - optimize extra host group processing when host group has not changed
                by blinding storing the HostGroup even if it has not changed membership, this call
                was kicking an AOP interception and Statistics recalculation
                The logic below minimizes the Statistics recalcs by only sending a statistics AOP when
                a new host group is provided
             */
            boolean shouldUpdateHostGroup = true;
            if (hostGroup != null && updatedHost != null && updatedHost.getHostGroups() != null) {
                for (HostGroup hg : (Set<HostGroup>)updatedHost.getHostGroups()) {
                    if (hg.getName().equals(hostGroup)) {
                        shouldUpdateHostGroup = false;
                        break;
                    }
                }
            }
            if (shouldUpdateHostGroup) {
                appType = addOrUpdateHostGroup(hostGroup, updatedHost.getHostName(), appType, updatedHost, hostGroups);
            }

            // #3 hostCategory
            appType = addOrUpdateHostCategory(hostCategory, appType, updatedHost, hostCategories);

            // #4 host status notification
            if (isNew[0] || isOwner[0]) {
                sendHostStatus(updatedHost, message, device, hostGroup, appType, now, isNew[0]);
            }

            // #5 set status on create
            if (isNew[0] && setStatusOnCreate && (status != null) &&
                    ((updatedHost.getHostStatus() == null) ||
                            (updatedHost.getHostStatus().getHostMonitorStatus() == null) ||
                            !status.equals(updatedHost.getHostStatus().getHostMonitorStatus().getName()))) {
                // update host status
                Date update = new Date(now.getTime()+1000L);
                addOrUpdateHost(host, status, message, message, device, appType, agentId, false, mergeHosts, update,
                        nextCheckTime, null, null, hosts, null, null);
                // host status notification
                sendHostStatus(updatedHost, message, device, hostGroup, appType, update, false);
            }

            // return created and host
            if (created != null) {
                created[0] = isNew[0];
            }
            return updatedHost;
        }
        catch (BusinessServiceException bse) {
            throw bse;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessServiceException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @Override
    public ServiceStatus createOrUpdateService(String host,
                                               String service,
                                               String status,
                                               String message,
                                               String serviceGroup,
                                               String serviceCategory,
                                               String hostGroup,
                                               String hostCategory,
                                               String device,
                                               String appType,
                                               String agentId,
                                               Integer checkIntervalMinutes,
                                               Boolean allowInserts,
                                               Boolean mergeHosts,
                                               Boolean setStatusOnCreate,
                                               String serviceValue,
                                               long warningLevel,
                                               long severeLevel,
                                               String metricType,
                                               boolean [] hostCreated,
                                               boolean [] serviceCreated,
                                               boolean processLogPerf)
            throws BusinessServiceException {
        return createOrUpdateService(host, service, status, message, serviceGroup, serviceCategory, hostGroup,
                hostCategory, device, appType, agentId, checkIntervalMinutes, allowInserts, mergeHosts, setStatusOnCreate,
                serviceValue, warningLevel, severeLevel, metricType, null, null, null, null, null, null, null, null,
                hostCreated, serviceCreated, processLogPerf);
    }

    @Override
    public ServiceStatus createOrUpdateService(String host,
                                               String service,
                                               String status,
                                               String message,
                                               String serviceGroup,
                                               String serviceCategory,
                                               String hostGroup,
                                               String hostCategory,
                                               String device,
                                               String appType,
                                               String agentId,
                                               Integer checkIntervalMinutes,
                                               Boolean allowInserts,
                                               Boolean mergeHosts,
                                               Boolean setStatusOnCreate,
                                               String serviceValue,
                                               long warningLevel,
                                               long severeLevel,
                                               String metricType,
                                               Map<String,HostGroup> hostGroups,
                                               Map<String,Category> hostCategories,
                                               Map<String,Host> hosts,
                                               Map<String,Device> devices,
                                               Map<String,ServiceStatus> services,
                                               Map<String,Category> serviceGroups,
                                               Map<String,Category> serviceCategories,
                                               Map<String,String> dynamicProperties,
                                               boolean [] hostCreated,
                                               boolean [] serviceCreated,
                                               boolean processLogPerf)
            throws BusinessServiceException {

        if (isEmptyParameter(host))
            throw new BusinessServiceException("Invalid parameter, empty host");
        if (isEmptyParameter(service))
            throw new BusinessServiceException("Invalid parameter, empty service");
        if (isEmptyParameter(message))
            throw new BusinessServiceException("Invalid parameter, empty message");

        CollageTimer timer = startMetricsTimer("createOrUpdateService");
        if (checkIntervalMinutes == null)
            checkIntervalMinutes = 5;
        Date now = new Date();
        Date nextCheckTime = new Date(now.getTime() + (checkIntervalMinutes * 60 * 1000));

        if (allowInserts == null)
            allowInserts = true;
        if (mergeHosts == null)
            mergeHosts = true;
        if (setStatusOnCreate == null)
            setStatusOnCreate = false;

        boolean [] isHostNew = new boolean[]{false};
        boolean [] isServiceNew = new boolean[]{false};
        boolean [] isServiceOwner = new boolean[]{false};

        try {
            boolean isMonitored = getBooleanAndRemove(dynamicProperties, ServiceStatus.TP_IS_MONITORED, true);
            boolean isGraphed = getBooleanAndRemove(dynamicProperties, ServiceStatus.TP_IS_GRAPHED, true);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Graphing status - Host:Service = %s:%s, monitored[%b] graphed[%b]",
                        host, service, isMonitored, isGraphed));
            }

            // #1: check if the host exists
            String existingHostMessage = "Last service check " + now;
            Host updatedHost = addOrUpdateHost(host, CollageStatus.UP.status, message, existingHostMessage, device, appType,
                    agentId, allowInserts, mergeHosts, now, nextCheckTime, isHostNew, null, hosts, devices, null);
            if (updatedHost == null) {
                return null;
            }

            // #2 hostGroup
            /*
                GWMON-13138 - optimize extra host group processing when host group has not changed
                by blinding storing the HostGroup even if it has not changed membership, this call
                was kicking an AOP interception and Statistics recalculation
                The logic below minimizes the Statistics recalcs by only sending a statistics AOP when
                a new host group is provided
             */
            boolean shouldUpdateHostGroup = true;
            if (hostGroup != null && updatedHost != null && updatedHost.getHostGroups() != null) {
                for (HostGroup hg : (Set<HostGroup>)updatedHost.getHostGroups()) {
                    if (hg.getName().equals(hostGroup)) {
                        shouldUpdateHostGroup = false;
                        break;
                    }
                }
            }
            if (shouldUpdateHostGroup) {
                appType = addOrUpdateHostGroup(hostGroup, updatedHost.getHostName(), appType, updatedHost, hostGroups);
            }

            // #3 hostCategory
            appType = addOrUpdateHostCategory(hostCategory, appType, updatedHost, hostCategories);

            // #4 set status on create
            if (isHostNew[0] && setStatusOnCreate && (status != null) &&
                    ((updatedHost.getHostStatus() == null) ||
                            (updatedHost.getHostStatus().getHostMonitorStatus() == null) ||
                            !status.equals(updatedHost.getHostStatus().getHostMonitorStatus().getName()))) {
                // update host status
                Date update = new Date(now.getTime()+1000L);
                addOrUpdateHost(host, CollageStatus.UP.status, message, existingHostMessage, device, appType, agentId,
                        false, mergeHosts, update, nextCheckTime, null, null, hosts, null, null);
            }
            // #5 create service
            ServiceStatus updatedService = addOrUpdateService(host, null, service, status, message, device, appType,
                    agentId, mergeHosts, serviceValue, now, nextCheckTime, isServiceNew, isServiceOwner, metricType,
                    hosts, devices, services, dynamicProperties);
            if (updatedService == null) {
                throw new RuntimeException("Host merge check already performed: should not fail for service");
            }

            // #6 serviceGroup
            appType = addOrUpdateServiceGroup(serviceGroup, appType, updatedService, serviceGroups);

            // #7 serviceCategory
            appType = addOrUpdateServiceCategory(serviceCategory, appType, updatedService, serviceCategories);

            // #8 service status notification
            if (isServiceNew[0] || isServiceOwner[0]) {
                sendServiceStatus(updatedService, message, device, hostGroup, appType, now, isServiceNew[0]);
            }

            // #9 set status on create
            if (isServiceNew[0] && setStatusOnCreate && (status != null) &&
                    ((updatedService.getMonitorStatus() == null) ||
                            !status.equals(updatedService.getMonitorStatus().getName()))) {
                // update service status
                Date update = new Date(now.getTime()+1000L);
                addOrUpdateService(host, null, service, status, message, device, appType, agentId, mergeHosts, null,
                        update, nextCheckTime, null, null, null, null, null, services, null);
                // service status notification
                sendServiceStatus(updatedService, message, device, hostGroup, appType, update, false);
                // update now for performance
                now = update;
            }

            // #10 Performance
            if (isGraphed) {
                recordServicePerformance(updatedService, appType, serviceValue, warningLevel, severeLevel, now, processLogPerf);
            }

            // return created and service
            if (hostCreated != null) {
                hostCreated[0] = isHostNew[0];
            }
            if (serviceCreated != null) {
                serviceCreated[0] = isServiceNew[0];
            }
            return updatedService;
        }
        catch (BusinessServiceException bse) {
            throw bse;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessServiceException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @Override
    public ServiceStatus createOrUpdateHostService(Host host,
                                                   String hostName,
                                                   String service,
                                                   String status,
                                                   String message,
                                                   String serviceGroup,
                                                   String serviceCategory,
                                                   String hostGroup,
                                                   String device,
                                                   String appType,
                                                   String agentId,
                                                   Integer checkIntervalMinutes,
                                                   Boolean mergeHosts,
                                                   Boolean setStatusOnCreate,
                                                   String serviceValue,
                                                   long warningLevel,
                                                   long severeLevel,
                                                   String metricType,
                                                   Map<String,ServiceStatus> services,
                                                   Map<String,Category> serviceGroups,
                                                   Map<String,Category> serviceCategories,
                                                   Map<String,String> dynamicProperties,
                                                   boolean [] created,
                                                   boolean processLogPerf)
            throws BusinessServiceException {

        if (host == null)
            throw new BusinessServiceException("Invalid parameter, null host");
        if (isEmptyParameter(service))
            throw new BusinessServiceException("Invalid parameter, empty service");
        if (isEmptyParameter(message))
            throw new BusinessServiceException("Invalid parameter, empty message");

        CollageTimer timer = startMetricsTimer("createOrUpdateHostService");
        if (checkIntervalMinutes == null)
            checkIntervalMinutes = 5;
        Date now = new Date();
        Date nextCheckTime = new Date(now.getTime() + (checkIntervalMinutes * 60 * 1000));

        if (mergeHosts == null)
            mergeHosts = true;
        if (setStatusOnCreate == null)
            setStatusOnCreate = false;

        boolean [] isNew = new boolean[]{false};
        boolean [] isOwner = new boolean[]{false};

        try {
            boolean isMonitored = getBooleanAndRemove(dynamicProperties, ServiceStatus.TP_IS_MONITORED, true);
            boolean isGraphed = getBooleanAndRemove(dynamicProperties, ServiceStatus.TP_IS_GRAPHED, true);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Graphing status - Host:Service = %s:%s, monitored[%b] graphed[%b]",
                        host.getHostName(), service, isMonitored, isGraphed));
            }
            // #1 create service, (assume host exists).
            ServiceStatus updatedService = addOrUpdateService(hostName, host, service, status, message, device,
                    appType, agentId, mergeHosts, serviceValue, now, nextCheckTime, isNew, isOwner, metricType, null,
                    null, services, dynamicProperties);
            if (updatedService == null) {
                return null;
            }

            // #2 serviceGroup
            addOrUpdateServiceGroup(serviceGroup, appType, updatedService, serviceGroups);

            // #3 serviceCategory
            appType = addOrUpdateServiceCategory(serviceCategory, appType, updatedService, serviceCategories);

            // #3 service status notification
            if (isNew[0] || isOwner[0]) {
                sendServiceStatus(updatedService, message, device, hostGroup, appType, now, isNew[0]);
            }

            // #4 set status on create
            if (isNew[0] && setStatusOnCreate && (status != null) &&
                    ((updatedService.getMonitorStatus() == null) ||
                            !status.equals(updatedService.getMonitorStatus()))) {
                // update service status
                Date update = new Date(now.getTime()+1000L);
                addOrUpdateService(hostName, host, service, status, message, device, appType, agentId, mergeHosts, null,
                        update, nextCheckTime, null, null, null, null, null, services, null);
                // service status notification
                sendServiceStatus(updatedService, message, device, hostGroup, appType, update, false);
                // update now for performance
                now = update;
            }

            // #5 Performance
            if (isGraphed) {
                recordServicePerformance(updatedService, appType, serviceValue, warningLevel, severeLevel, now, processLogPerf);
            }

            // return created and service
            if (created != null) {
                created[0] = isNew[0];
            }
            return updatedService;
        }
        catch (BusinessServiceException bse) {
            throw bse;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessServiceException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Add or update host using admin utility.
     *
     * @param host host name
     * @param status new/updated host monitor status
     * @param newHostMessage message for new host
     * @param existingHostMessage message for existing host update
     * @param deviceIdentification new host device and display name or null
     * @param appType new/updated application type or null
     * @param agentId new/updated agent id or null
     * @param allowInserts allow new host
     * @param mergeHosts merge hosts with matching but different names
     * @param now transaction timestamp
     * @param nextCheckTime next status check time
     * @param isNew return new host added
     * @param isOwner return service owner
     * @param hosts host cache for batch operation
     * @param devices device cache for batch operation
     * @param dynamicProperties dynamic Collage properties
     * @return updated host
     * @throws BusinessServiceException
     */
    private Host addOrUpdateHost(String host,
                                 String status,
                                 String newHostMessage,
                                 String existingHostMessage,
                                 String deviceIdentification,
                                 String appType,
                                 String agentId,
                                 Boolean allowInserts,
                                 Boolean mergeHosts,
                                 Date now,
                                 Date nextCheckTime,
                                 boolean [] isNew,
                                 boolean [] isOwner,
                                 Map<String,Host> hosts,
                                 Map<String,Device> devices,
                                 Map<String,String> dynamicProperties
    ) throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer("addOrUpdateHost");
        Map<String, String> properties = new HashMap<String, String>();
        Host existingHost = null;
        if (hosts != null) {
            existingHost = hosts.get(host.toLowerCase());
        }
        if (existingHost == null) {
            existingHost = hostIdentityService.getHostByIdOrHostName(host);
        }
        Device device = null;
        if (existingHost == null) {
            if (allowInserts == false) {
                log.error("Host " + host + " does not exist and inserts are not allowed.");
                return null;
            }
            if (isNew != null) {
                isNew[0] = true;
            }
            status = CollageStatus.PENDING.status;
            if (isEmptyParameter(appType)) {
                appType = DEFAULT_APP_TYPE;
            }
            if (isEmptyParameter(deviceIdentification)) {
                deviceIdentification = host;
            }
            if (devices != null) {
                device = devices.get(deviceIdentification);
            }
            properties.put(CollageAdminInfrastructure.PROP_DESCRIPTION, host);
            properties.put(CollageAdminInfrastructure.PROP_DEVICE_IDENTIFICATION, deviceIdentification);
            properties.put(CollageAdminInfrastructure.PROP_DISPLAY_NAME, host);
            properties.put(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT, newHostMessage);
            if (!isEmptyParameter(agentId)) {
                properties.put(CollageAdminInfrastructure.PROP_AGENT_ID, agentId);
            }
        } else {
            device = existingHost.getDevice();
            if (isEmptyParameter(status)) {
                throw new BusinessServiceException("Invalid parameter, empty status");
            }
            if (status.equals(CollageStatus.SCHEDULED_DOWN.status)) {
                properties.put(CollageAdminInfrastructure.PROP_IS_ACKNOWLEDGED, Boolean.TRUE.toString());
            }
            // last state change set here txn timestamp, (not used in admin unless state changed)
            properties.put(CollageAdminInfrastructure.PROP_LAST_STATE_CHANGE, formatDate(now));

            properties.put(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT, existingHostMessage);
            // CLOUDHUB-209: never update agent id on updates, first agent in owns the host
        }
        properties.put(CollageAdminInfrastructure.PROP_HOST_NAME, host);
        properties.put(CollageAdminInfrastructure.PROP_MONITOR_STATUS, status);
        properties.put(CollageAdminInfrastructure.PROP_STATE_TYPE, CollageState.HARD.name());
        properties.put(CollageAdminInfrastructure.PROP_SERVICE_LAST_CHECK_TIME, Long.toString(now.getTime()));
        properties.put(CollageAdminInfrastructure.PROP_SERVICE_NEXT_CHECK_TIME, Long.toString(nextCheckTime.getTime()));
        if (!isEmptyParameter(appType)) {
            properties.put(CollageAdminInfrastructure.PROP_APPLICATION_TYPE_NAME, appType);
        }

        // dynamic properties
        if (dynamicProperties != null) {
            for (String key : dynamicProperties.keySet()) {
                properties.put(key, dynamicProperties.get(key));
            }
        }

        // update host
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        Host updatedHost = CollageAdminInfrastructureUtils.updateHost(existingHost, device, properties, mergeHosts, admin);

        // update host and device caches
        if (updatedHost != null) {
            if (devices != null) {
                device = updatedHost.getDevice();
                devices.put(device.getIdentification(), device);
            }
            if (hosts != null) {
                hosts.put(updatedHost.getHostName().toLowerCase(), updatedHost);
            }
        }

        // return host owner status
        if ((isOwner != null) && (updatedHost != null)) {
            isOwner[0] = (isEmptyParameter(appType) || updatedHost.getApplicationType() == null ||
                    appType.equals(updatedHost.getApplicationType().getName()));
        }

        // return updated host
        stopMetricsTimer(timer);
        return updatedHost;
    }

    /**
     * Add or update host group using admin utility.
     *
     * @param hostGroupName host group name or null
     * @param hostName host name to add to group
     * @param appType new/updated host group application type or null
     * @param host host to add to group or null
     * @param hostGroups host group cache for batch operation
     * @return host group application type
     * @throws BusinessServiceException
     */
    private String addOrUpdateHostGroup(String hostGroupName,
                                        String hostName,
                                        String appType,
                                        Host host,
                                        Map<String,HostGroup> hostGroups) throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer("addOrUpdateHostGroup");
        if (!isEmptyParameter(hostGroupName)) {
            if (isEmptyParameter(appType)) {
                HostGroup oldHostGroup = null;
                if (hostGroups != null) {
                    oldHostGroup = hostGroups.get(hostGroupName);
                }
                if (oldHostGroup == null) {
                    oldHostGroup = hostGroupService.getHostGroupByName(hostGroupName);
                }
                if (oldHostGroup != null) {
                    appType = oldHostGroup.getApplicationType().getName();
                }
            }
            List<String> hostNames = Arrays.asList(new String[]{hostName});
            HostGroup hostGroup = ((hostGroups != null) ? hostGroups.get(hostGroupName) : null);
            List<Host> hosts = ((host != null) ? Arrays.asList(new Host[]{host}) : null);
            HostGroup updatedHostGroup = adminService.updateHostGroup(appType, hostGroupName, hostNames, hostGroup, hosts);
            if (hostGroups != null) {
                hostGroups.put(updatedHostGroup.getName(), updatedHostGroup);
            }
        }
        stopMetricsTimer(timer);
        return appType;
    }

    /**
     * Add or update host category using admin utility.
     *
     * @param hostCategoryName host category name or null
     * @param appType host category application type or null
     * @param updatedHost updated host to add to category or null
     * @param hostCategories host category cache for batch operation
     * @return host category application type
     * @throws BusinessServiceException
     */
    private String addOrUpdateHostCategory(String hostCategoryName,
                                           String appType,
                                           Host updatedHost,
                                           Map<String,Category> hostCategories) throws BusinessServiceException {

        if (isEmptyParameter(hostCategoryName)) {
            return appType;
        }
        // add or update host category
        return addOrUpdateCategory(hostCategoryName, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY, appType,
                updatedHost.getHostId(), CategoryService.ENTITY_TYPE_CODE_HOST, hostCategories);
    }

    /**
     * Send host status event and notification on monitor status change.
     *
     * @param updatedHost updated host instance
     * @param message event/notification message
     * @param deviceIdentification event/notification device or null
     * @param hostGroup notification host group or null
     * @param appType event application type
     * @param now transaction timestamp
     * @param isNew new host flag
     * @throws BusinessServiceException
     */
    private void sendHostStatus(Host updatedHost,
                                String message,
                                String deviceIdentification,
                                String hostGroup,
                                String appType,
                                Date now,
                                boolean isNew) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("sendHostStatus");
        String hostName = updatedHost.getHostName();
        Device device = updatedHost.getDevice();
        deviceIdentification = ((deviceIdentification == null) ? device.getIdentification() : deviceIdentification);
        String status = (((updatedHost.getHostStatus() != null) && (updatedHost.getHostStatus().getHostMonitorStatus() != null)) ?
                updatedHost.getHostStatus().getHostMonitorStatus().getName() : null);
        if (!isNew) {
            String oldStatus = updatedHost.getLastMonitorStatus();
            if (isEmptyParameter(oldStatus) || !StringUtils.equals(status, oldStatus)) {
                appType = updatedHost.getApplicationType().getName();
                sendEvent(hostName, null, status, message, deviceIdentification, appType, device, updatedHost, null, now);
                if (!isEmptyParameter(oldStatus) &&
                        (!StringUtils.equals(oldStatus, CollageStatus.PENDING.status) || !StringUtils.equals(status, CollageStatus.UP.status))) {
                    sendHostNotification(new NomaHostNotification(hostName, deviceIdentification, status, hostGroup,
                            message, formatDate(now)));
                }
            }
        } else {
            sendEvent(hostName, null, status, message, deviceIdentification, appType, device, updatedHost, null, now);
        }
        stopMetricsTimer(timer);
    }

    /**
     * Add or update host service using admin utility.
     *
     * @param host host name
     * @param updatingHost updating host or null
     * @param service service description
     * @param status new/updated service monitor status
     * @param message message for new/updated service
     * @param deviceIdentification new/updated device or null
     * @param appType new service application type or null
     * @param agentId new/updated agentId or null
     * @param mergeHosts merge hosts with matching but different names
     * @param serviceValue service value or null
     * @param now transaction timestamp
     * @param nextCheckTime next status check time
     * @param isNew return new service added
     * @param isOwner return service owner
     * @param metricType the metric type of this service
     * @param hosts host cache for batch operation
     * @param devices device cache for batch operation
     * @param services service cache for batch operation
     * @param dynamicProperties dynamic Collage properties
     * @return updated service
     * @throws BusinessServiceException
     */
    private ServiceStatus addOrUpdateService(String host,
                                             Host updatingHost,
                                             String service,
                                             String status,
                                             String message,
                                             String deviceIdentification,
                                             String appType,
                                             String agentId,
                                             Boolean mergeHosts,
                                             String serviceValue,
                                             Date now,
                                             Date nextCheckTime,
                                             boolean [] isNew,
                                             boolean [] isOwner,
                                             String metricType,
                                             Map<String,Host> hosts,
                                             Map<String,Device> devices,
                                             Map<String,ServiceStatus> services,
                                             Map<String,String> dynamicProperties) throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer("addOrUpdateService");
        String entity = host + ":" + service;
        ServiceStatus existingService = null;
        if (services != null) {
            existingService = services.get(entity);
        }
        if (existingService == null) {
            existingService = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(service, host);
        }
        Map<String, String> serviceProperties = new HashMap<String, String>();
        if (existingService == null) {
            if (isNew != null) {
                isNew[0] = true;
            }
            status = CollageStatus.PENDING.status;
            if (isEmptyParameter(appType)) {
                appType = DEFAULT_APP_TYPE;
                serviceProperties.put(CollageAdminInfrastructure.PROP_APPLICATION_TYPE_NAME, appType);
            }
            if (isEmptyParameter(deviceIdentification)) {
                deviceIdentification = host;
            }
            serviceProperties.put(CollageAdminInfrastructure.PROP_SERVICE_DESC, service);
            serviceProperties.put(CollageAdminInfrastructure.PROP_MONITOR_STATUS, status);
            // additional properties
            serviceProperties.put(CollageAdminInfrastructure.PROP_STATE_TYPE, CollageState.HARD.name());
            serviceProperties.put(CollageAdminInfrastructure.PROP_CHECK_TYPE, CollageCheckType.ACTIVE.name());
            serviceProperties.put(CollageAdminInfrastructure.PROP_LAST_HARD_STATE, status);
            serviceProperties.put(CollageAdminInfrastructure.PROP_STATE_TYPE, CollageState.HARD.name());
        } else {
            serviceProperties.put(CollageAdminInfrastructure.PROP_SERVICE_DESC, service);

            if (isEmptyParameter(status)) {
                throw new BusinessServiceException("Invalid parameter, empty status");
            }
            serviceProperties.put(CollageAdminInfrastructure.PROP_MONITOR_STATUS, status);
            serviceProperties.put(CollageAdminInfrastructure.PROP_LAST_HARD_STATE, status);
            serviceProperties.put(CollageAdminInfrastructure.PROP_STATE_TYPE, CollageState.HARD.name());
            // last state change set here txn timestamp, (not used in admin unless state changed)
            serviceProperties.put(CollageAdminInfrastructure.PROP_SERVICE_LAST_STATE_CHANGE, formatDate(now));
            if (updatingHost == null) {
                updatingHost = existingService.getHost();
            }
        }
        if (!isEmptyParameter(serviceValue)) {
            serviceProperties.put(CollageAdminInfrastructure.PROP_PERFORMANCE_DATA, serviceValue);
        }
        serviceProperties.put(CollageAdminInfrastructure.PROP_SERVICE_LAST_CHECK_TIME, formatDate(now));
        serviceProperties.put(CollageAdminInfrastructure.PROP_SERVICE_NEXT_CHECK_TIME, formatDate(nextCheckTime));
        serviceProperties.put(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT, message);
        if (!isEmptyParameter(metricType)) {
            serviceProperties.put(CollageAdminInfrastructure.PROP_SERVICE_METRIC_TYPE, metricType);
        }

        // dynamic properties
        if (dynamicProperties != null) {
            for (String key : dynamicProperties.keySet()) {
                serviceProperties.put(key, dynamicProperties.get(key));
            }
        }

        // get host and device from updating host or caches
        if ((updatingHost == null) && (hosts != null)) {
            updatingHost = hosts.get(host.toLowerCase());
        }
        Device device = null;
        if (updatingHost != null) {
            device = updatingHost.getDevice();
        } else if (devices != null) {
            device = devices.get(deviceIdentification);
        }
        if (isEmptyParameter(deviceIdentification) && (device != null)) {
            deviceIdentification = device.getIdentification();
        }

        // update service
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        ServiceStatus updatedService = CollageAdminInfrastructureUtils.updateService(existingService,
                DEFAULT_MONITOR_SERVER, appType, host, deviceIdentification, agentId, updatingHost, device,
                serviceProperties, mergeHosts, admin);

        // update service, host, and device caches
        if (updatedService != null) {
            if (devices != null) {
                device = updatedService.getHost().getDevice();
                devices.put(device.getIdentification(), device);
            }
            if (hosts != null) {
                updatingHost = updatedService.getHost();
                hosts.put(updatingHost.getHostName().toLowerCase(), updatingHost);
            }
            if (services != null) {
                services.put(entity, updatedService);
            }
        }

        // return service owner status
        if ((isOwner != null) && (updatedService != null)) {
            isOwner[0] = (isEmptyParameter(appType) || updatedService.getApplicationType() == null ||
                    appType.equals(updatedService.getApplicationType().getName()));
        }

        // return updated service
        stopMetricsTimer(timer);
        return updatedService;
    }

    /**
     * Add or update service group using admin utility.
     *
     * @param serviceGroupName service group name or null
     * @param appType service group application type or null
     * @param updatedService updated service to add to group or null
     * @param serviceGroups service group cache for batch operation
     * @return service group application type
     * @throws BusinessServiceException
     */
    private String addOrUpdateServiceGroup(String serviceGroupName,
                                           String appType,
                                           ServiceStatus updatedService,
                                           Map<String,Category> serviceGroups) throws BusinessServiceException {

        if (isEmptyParameter(serviceGroupName)) {
            return appType;
        }
        // add or update service group category
        return addOrUpdateCategory(serviceGroupName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, appType,
                updatedService.getServiceStatusId(), CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS, serviceGroups);
    }

    /**
     * Add or update service category using admin utility.
     *
     * @param serviceCategoryName host category name or null
     * @param appType service category application type or null
     * @param updatedService updated service to add to category or null
     * @param serviceCategories service category cache for batch operation
     * @return service category application type
     * @throws BusinessServiceException
     */
    private String addOrUpdateServiceCategory(String serviceCategoryName,
                                              String appType,
                                              ServiceStatus updatedService,
                                              Map<String,Category> serviceCategories) throws BusinessServiceException {

        if (isEmptyParameter(serviceCategoryName)) {
            return appType;
        }
        // add or update service category
        return addOrUpdateCategory(serviceCategoryName, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY, appType,
                updatedService.getServiceStatusId(), CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS, serviceCategories);
    }

    /**
     * Send host service events and notification on monitor status change.
     *
     * @param updatedService updated service
     * @param message event/notification message
     * @param deviceIdentification event/notification device or null
     * @param hostGroup notification host group or null
     * @param appType event application type
     * @param now transaction timestamp
     * @param isNew new service flag
     * @throws BusinessServiceException
     */
    private void sendServiceStatus(ServiceStatus updatedService,
                                   String message,
                                   String deviceIdentification,
                                   String hostGroup,
                                   String appType,
                                   Date now,
                                   boolean isNew) throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer("sendServiceStatus");
        Host host = updatedService.getHost();
        Device device = host.getDevice();
        String hostName = host.getHostName();
        String service = updatedService.getServiceDescription();
        deviceIdentification = ((deviceIdentification == null) ? device.getIdentification() : deviceIdentification);
        String status = ((updatedService.getMonitorStatus() != null) ? updatedService.getMonitorStatus().getName() : null);
        if (!isNew) {
            String oldStatus = updatedService.getLastMonitorStatus();
            if (isEmptyParameter(oldStatus) || !StringUtils.equals(status, oldStatus)) {
                // always send event on status change
                appType = updatedService.getApplicationType().getName();
                sendEvent(hostName, service, status, message, deviceIdentification, appType, device, host,
                        updatedService, now);
                // send service notifications only if not transitioning from PENDING to OK or
                // from UNKNOWN to OK when host is transitioning from SUSPENDED
                boolean pendingToOk = StringUtils.equals(oldStatus, CollageStatus.PENDING.status) &&
                        StringUtils.equals(status, CollageStatus.OK.status);
                String hostStatus = (((host.getHostStatus() != null) && (host.getHostStatus().getHostMonitorStatus() != null)) ?
                        host.getHostStatus().getHostMonitorStatus().getName() : null);
                String oldHostStatus = host.getLastMonitorStatus();
                boolean unsuspendUnknownToOk = (!isEmptyParameter(hostStatus) && !isEmptyParameter(oldHostStatus) &&
                        !StringUtils.equals(hostStatus, CollageStatus.SUSPENDED.status) &&
                        StringUtils.equals(oldHostStatus, CollageStatus.SUSPENDED.status) &&
                        StringUtils.equals(oldStatus, CollageStatus.UNKNOWN.status) &&
                        StringUtils.equals(status, CollageStatus.OK.status));
                if (!pendingToOk && !unsuspendUnknownToOk) {
                    sendServiceNotification(new NomaServiceNotification(hostName, service, deviceIdentification, status,
                            hostGroup, message, formatDate(now)));
                }
            }
        } else {
            sendEvent(hostName, service, status, message, deviceIdentification, appType, device, host, updatedService,
                    now);
        }
        stopMetricsTimer(timer);
    }

    /**
     * Record service performance data and notifications.
     *
     * @param updatedService updated service
     * @param appType new service application type
     * @param serviceValue service value
     * @param warningLevel warning performance level
     * @param severeLevel severe performance level
     * @param now transaction timestamp
     * @throws BusinessServiceException
     */
    private void recordServicePerformance(ServiceStatus updatedService,
                                          String appType,
                                          String serviceValue,
                                          long warningLevel,
                                          long severeLevel,
                                          Date now,
                                          boolean processLogPerf) throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer("recordServicePerformance");
        if (isEmptyParameter(serviceValue)) {
            return;
        }
        String host = updatedService.getHost().getHostName();
        String service = updatedService.getServiceDescription();
        String label = StringUtils.defaultString(updatedService.getMetricType());
        if (sendJMSNotifications && !noConfiguredPerfWriters) {
            performance.writeMessage(appType, host, service, now.getTime() / 1000, serviceValue, warningLevel, severeLevel);
            performance.commit();
        }
        if (processLogPerf) {
            adminService.insertPerformanceData(host, service, label, Double.parseDouble(serviceValue), dateFormat.format(now));
        }
        stopMetricsTimer(timer);
    }

    /**
     * Add or update entity category using admin utility.
     *
     * @param categoryName category name or null
     * @param categoryEntityTypeCode category entity type code
     * @param appType category application type or null
     * @param entityId entity id of entity to associate with category
     * @param entityTypeCode entity type code of entity to associate with category
     * @param categories category cache for batch operation
     * @return category application type
     * @throws BusinessServiceException
     */
    private String addOrUpdateCategory(String categoryName,
                                       String categoryEntityTypeCode,
                                       String appType,
                                       Integer entityId,
                                       String entityTypeCode,
                                       Map<String,Category> categories) throws BusinessServiceException {

        CollageTimer timer = startMetricsTimer("addOrUpdateCategory");
        if (isEmptyParameter(categoryName)) {
            return appType;
        }
        // get category
        boolean isNewCategory = false;
        Category category = null;
        if (categories != null) {
            category = categories.get(categoryName);
        }
        if (category == null) {
            category = categoryService.getCategoryByName(categoryName, categoryEntityTypeCode);
        }
        if (category != null) {
            // default application type
            if (isEmptyParameter(appType) && (category.getApplicationType() != null)) {
                appType = category.getApplicationType().getName();
            }
        } else {
            // create category
            ApplicationType applicationType = null;
            if (!isEmptyParameter(appType)) {
                applicationType = metadataService.getApplicationTypeByName(appType);
                if (applicationType == null) {
                    throw new BusinessServiceException(String.format("Application type %s not an application type.",
                            appType));
                }
            }
            EntityType categoryEntityType = metadataService.getEntityTypeByName(categoryEntityTypeCode);
            if (categoryEntityType == null) {
                throw new BusinessServiceException(String.format("Entity type %s not found.", categoryEntityTypeCode));
            }
            category = categoryService.createCategory(categoryName, null, categoryEntityType, applicationType, null);
            isNewCategory = true;
        }
        // check if entity is already in category
        boolean entityInCategory = false;
        if (!isNewCategory) {
            for (CategoryEntity categoryEntity : category.getCategoryEntities()) {
                if ((categoryEntity.getObjectID() != null) &&
                        categoryEntity.getObjectID().equals(entityId) &&
                        (categoryEntity.getEntityType() != null) &&
                        categoryEntity.getEntityType().getName().equals(entityTypeCode)) {
                    entityInCategory = true;
                    break;
                }
            }
        }
        // add entity to category if necessary
        if (!entityInCategory) {
            EntityType entityType = metadataService.getEntityTypeByName(entityTypeCode);
            if (entityType == null) {
                throw new BusinessServiceException(String.format("Entity type %s not found.", entityTypeCode));
            }
            CategoryEntity categoryEntity = categoryService.createCategoryEntity();
            categoryEntity.setCategory(category);
            categoryEntity.setEntityType(entityType);
            categoryEntity.setObjectID(entityId);
            category.getCategoryEntities().add(categoryEntity);
            // update category
            adminService.saveCategory(category);
        }
        // cache updated category
        if (categories != null) {
            categories.put(category.getName(), category);
        }
        stopMetricsTimer(timer);
        return appType;
    }

    @Override
    public List<HostServiceInDowntime> setHostsAndServicesInDowntime(Collection<String> hostNames,
                                                                     Collection<String> serviceDescriptions,
                                                                     Collection<String> hostGroupNames,
                                                                     Collection<String> serviceGroupCategoryNames,
                                                                     boolean setHosts, boolean setServices) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("setHostsAndServicesInDowntime");
        // gather hosts and services in downtime
        long start = System.currentTimeMillis();

        List<HostServiceInDowntime> hostsAndServices = new ArrayList<HostServiceInDowntime>();
        if (!setHosts && !setServices) {
            return hostsAndServices;
        }
        // aggregate specified hosts
        Map<Host,String[]> hosts = new HashMap<Host,String[]>();
        if ((hostNames == null) || hostNames.isEmpty() || ((hostNames.size() == 1) && WILDCARD_HOST_NAME.equals(hostNames.iterator().next()))) {
            // hosts from host groups
            if ((hostGroupNames != null) && !hostGroupNames.isEmpty()) {
                for (String hostGroupName : hostGroupNames) {
                    HostGroup hostGroup = hostGroupService.getHostGroupByName(hostGroupName);
                    if (hostGroup != null) {
                        Collection<Host> hostGroupHosts = hostGroup.getHosts();
                        // save host group hosts
                        for (Host host : hostGroupHosts) {
                            hosts.put(host, new String[]{HostGroup.ENTITY_TYPE_CODE, hostGroupName});
                        }
                        // specified host group hosts in downtime
                        if (setHosts) {
                            for (Host host : hostGroupHosts) {
                                hostsAndServices.add(new HostServiceInDowntime(host.getHostName(), HostGroup.ENTITY_TYPE_CODE, hostGroupName));
                            }
                        }
                    }
                }
            }
            // hosts from service groups
            if (setHosts && (serviceGroupCategoryNames != null) && !serviceGroupCategoryNames.isEmpty()) {
                for (String serviceGroupCategoryName : serviceGroupCategoryNames) {
                    Category category = categoryService.getCategoryByName(serviceGroupCategoryName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                    if (category != null) {
                        // hosts in service group services
                        Collection<Host> serviceGroupHosts = new HashSet<Host>();
                        List<ServiceStatus> serviceStatuses = statusService.getServicesByCategoryId(category.getCategoryId());
                        for (ServiceStatus serviceStatus : serviceStatuses) {
                            serviceGroupHosts.add(serviceStatus.getHost());
                        }
                        // specified service group hosts in downtime
                        for (Host host : serviceGroupHosts) {
                            hostsAndServices.add(new HostServiceInDowntime(host.getHostName(), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, serviceGroupCategoryName));
                        }
                    }
                }
            }
        } else {
            // specified hosts
            Collection<Host> hostsCollection = hostIdentityService.getHostsByIdOrHostNames(new ArrayList<String>(hostNames));
            // save hosts
            for (Host host : hostsCollection) {
                hosts.put(host, new String[]{Host.ENTITY_TYPE_CODE, host.getHostName()});
            }
            // specified hosts in downtime
            if (setHosts) {
                for (Host host : hostsCollection) {
                    String hostName = host.getHostName();
                    hostsAndServices.add(new HostServiceInDowntime(hostName, Host.ENTITY_TYPE_CODE, hostName));
                }
            }
        }
        if (setServices) {
            // aggregate specified services
            if ((serviceDescriptions == null) || serviceDescriptions.isEmpty() || ((serviceDescriptions.size() == 1) && WILDCARD_SERVICE_DESCRIPTION.equals(serviceDescriptions.iterator().next()))) {
                // all services for hosts
                for (Map.Entry<Host,String[]> hostEntry : hosts.entrySet()) {
                    // specified services in downtime
                    for (ServiceStatus serviceStatus : (Collection<ServiceStatus>)hostEntry.getKey().getServiceStatuses()) {
                        String hostName = serviceStatus.getHost().getHostName();
                        String serviceDescription = serviceStatus.getServiceDescription();
                        String entityType = hostEntry.getValue()[0];
                        String entityName = hostEntry.getValue()[1];
                        hostsAndServices.add(new HostServiceInDowntime(hostName, serviceDescription, entityType, entityName));
                    }
                }
                // services from service groups
                if ((serviceGroupCategoryNames != null) && !serviceGroupCategoryNames.isEmpty()) {
                    for (String serviceGroupCategoryName : serviceGroupCategoryNames) {
                        Category category = categoryService.getCategoryByName(serviceGroupCategoryName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                        if (category != null) {
                            List<ServiceStatus> serviceStatuses = statusService.getServicesByCategoryId(category.getCategoryId());
                            // specified services in downtime
                            for (ServiceStatus serviceStatus : serviceStatuses) {
                                String hostName = serviceStatus.getHost().getHostName();
                                String serviceDescription = serviceStatus.getServiceDescription();
                                hostsAndServices.add(new HostServiceInDowntime(hostName, serviceDescription, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, serviceGroupCategoryName));
                            }
                        }
                    }
                }
            } else {
                // specified services for hosts
                for (Host host : hosts.keySet()) {
                    for (String serviceDescription : serviceDescriptions) {
                        ServiceStatus serviceStatus = statusService.getServiceByDescription(serviceDescription, host.getHostName());
                        if (serviceStatus != null) {
                            // specified services in downtime
                            String hostName = serviceStatus.getHost().getHostName();
                            String entityName = hostName+":"+serviceDescription;
                            hostsAndServices.add(new HostServiceInDowntime(hostName, serviceDescription, ServiceStatus.ENTITY_TYPE_CODE, entityName));
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("ENDING setHostsAndServicesInDowntime " + (System.currentTimeMillis() - start));
        }
        // update in downtime, send messages, and return in downtime
        updateInDowntimeAndSendLogMessageEvents(hostsAndServices, true);
        stopMetricsTimer(timer);
        return hostsAndServices;
    }

    @Override
    public List<HostServiceInDowntime> clearHostsAndServicesInDowntime(List<HostServiceInDowntime> hostsAndServices) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("clearHostsAndServicesInDowntime");
        // reverse and update in downtime, send messages, and return in downtime
        List<HostServiceInDowntime> reverseHostsAndServices = new ArrayList<>(hostsAndServices);
        Collections.reverse(reverseHostsAndServices);
        updateInDowntimeAndSendLogMessageEvents(reverseHostsAndServices, false);
        stopMetricsTimer(timer);
        return reverseHostsAndServices;
    }

    /**
     * Update in downtime hosts and services list incrementing or decrementing Nagios
     * scheduled downtime levels and sending downtime LogMessage events. Event messages
     * are composed using the hosts and services list element entity type and name. Returns
     * Nagios scheduled downtime level in the hosts and services downtime list elements.
     *
     * @param hostsAndServices hosts and services in downtime list
     * @param inDowntime increment or decrement scheduled downtime level
     */
    private void updateInDowntimeAndSendLogMessageEvents(List<HostServiceInDowntime> hostsAndServices, boolean inDowntime) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("updateInDowntimeAndSendLogMessageEvents");
        // set Nagios scheduled downtime levels on in downtime hosts and services

        long start1 = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("starting update downtime..");
        }
        Set<Host> updateHosts = new HashSet<Host>();
        Set<ServiceStatus> updateServiceStatuses = new HashSet<ServiceStatus>();

        CollageFactory service = CollageFactory.getInstance();
        Properties properties = service.getFoundationProperties();
        String downtimeThrottleConfig = properties.getProperty(CONFIG_DOWNTIME_THROTTLE, String.valueOf(DEFAULT_DOWNTIME_THROTTLE));
        long downtimeThrottle = Long.parseLong(downtimeThrottleConfig);
        String enableDowntimeThreadConfig = properties.getProperty(CONFIG_DOWNTIME_BACKGROUND_ENABLE, String.valueOf(DEFAULT_DOWNTIME_BACKGROUND_ENABLE));
        boolean enableDowntimeThread = Boolean.parseBoolean(enableDowntimeThreadConfig);

        // set hosts/services in downtime
        for (HostServiceInDowntime hostOrService : hostsAndServices) {
            hostOrService.scheduledDowntimeDepth = null;
            if (hostOrService.serviceDescription != null) {
                ServiceStatus serviceStatus = statusService.getServiceByDescription(hostOrService.serviceDescription, hostOrService.hostName);
                if (serviceStatus != null) {
                    Integer scheduledDowntimeDepth = (Integer)serviceStatus.getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH);
                    scheduledDowntimeDepth = new Integer(Math.max(((scheduledDowntimeDepth != null) ? scheduledDowntimeDepth : 0)+(inDowntime ? 1 : -1), 0));
                    serviceStatus.setProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH, scheduledDowntimeDepth);
                    hostOrService.scheduledDowntimeDepth = scheduledDowntimeDepth;
                    updateServiceStatuses.add(serviceStatus);
                }
            } else {
                Host host = hostService.getHostByHostName(hostOrService.hostName);
                if (host != null) {
                    HostStatus hostStatus = host.getHostStatus();
                    if (hostStatus == null) {
                        hostStatus = hostService.createHostStatus(host.getApplicationType().getName(), host);
                        host.setHostStatus(hostStatus);
                    }
                    Integer scheduledDowntimeDepth = (Integer) hostStatus.getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH);
                    scheduledDowntimeDepth = new Integer(Math.max(((scheduledDowntimeDepth != null) ? scheduledDowntimeDepth : 0) + (inDowntime ? 1 : -1), 0));
                    hostStatus.setProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH, scheduledDowntimeDepth);
                    hostOrService.scheduledDowntimeDepth = scheduledDowntimeDepth;
                    updateHosts.add(host);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("MIDDLE update downtime " + (System.currentTimeMillis() - start1));
            log.debug("Downtime Updated hosts: " + updateHosts.size());
            log.debug("Downtime Updated services: " + updateServiceStatuses.size());
        }
        // update hosts and services
        if (!updateHosts.isEmpty()) {
            long start2a = System.currentTimeMillis();
            hostService.saveHost(updateHosts);
            if (log.isDebugEnabled()) {
                log.debug("ENDING update hosts " + (System.currentTimeMillis() - start2a));
            }
        }
        if (!updateServiceStatuses.isEmpty()) {
            long start2b = System.currentTimeMillis();
            statusService.saveService(updateServiceStatuses);
            if (log.isDebugEnabled()) {
                log.debug("ENDING update services " + (System.currentTimeMillis() - start2b));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("ENDING update downtime " + (System.currentTimeMillis() - start1));
        }
        LogMessageUpdateThread updateThread = new LogMessageUpdateThread(hostsAndServices, inDowntime, downtimeThrottle, enableDowntimeThread);
        if (enableDowntimeThread) {
            updateThread.start();
        }
        else {
            updateThread.run();
        }
        stopMetricsTimer(timer);
    }

    public static final String CONFIG_DOWNTIME_BACKGROUND_ENABLE = "collagerest.downtime.background.enable";
    public static final String CONFIG_DOWNTIME_THROTTLE = "collagerest.downtime.throttleWaitMs";
    public static final long DEFAULT_DOWNTIME_THROTTLE = 0;
    public static final boolean DEFAULT_DOWNTIME_BACKGROUND_ENABLE = false;

    public class LogMessageUpdateThread extends Thread {

        private List<HostServiceInDowntime> hostsAndServices;
        private boolean inDowntime;
        private long downtimeThrottle;
        private boolean enableDowntimeThread;

        public LogMessageUpdateThread(List<HostServiceInDowntime> hostAndServices, boolean inDowntime,
                                      long downtimeThrottle, boolean enableDowntimeThread) {
            this.hostsAndServices = hostAndServices;
            this.inDowntime = inDowntime;
            this.downtimeThrottle = downtimeThrottle;
            this.enableDowntimeThread = enableDowntimeThread;
        }

        @Override
        public void run() {
            CollageTimer timer = startMetricsTimer("run");
            // send LogMessage events
            long start = System.currentTimeMillis();
            try {
                BizTransaction session = null;
                if (enableDowntimeThread) {
                    session = new BizTransaction();
                    session.startTransaction();
                }
                if (log.isDebugEnabled()) {
                    log.debug("starting Downtime LOG MESSAGE updates..." + hostsAndServices.size());
                }
                for (HostServiceInDowntime hostOrService : hostsAndServices) {
                    if (hostOrService.scheduledDowntimeDepth != null) {
                        // get host and service LogMessage properties
                        Properties logMessageProperties = new Properties();
                        Host host = hostService.getHostByHostName(hostOrService.hostName);
                        if ((host == null) || (host.getHostStatus() == null)) {
                            continue;
                        }
                        logMessageProperties.put(LogMessage.EP_HOST_NAME, host.getHostName());
                        logMessageProperties.put(LogMessage.EP_HOST_STATUS_ID, host.getHostStatus());
                        if (hostOrService.serviceDescription != null) {
                            ServiceStatus serviceStatus = statusService.getServiceByDescription(hostOrService.serviceDescription, host.getHostName());
                            if (serviceStatus == null) {
                                continue;
                            }
                            logMessageProperties.put(LogMessage.EP_SERVICE_STATUS_DESCRIPTION, serviceStatus.getServiceDescription());
                            logMessageProperties.put(LogMessage.EP_SERVICE_STATUS_ID, serviceStatus);
                        }
                        // compose and send downtime LogMessage event
                        String device = host.getDevice().getIdentification();
                        String severity = CollageSeverity.LOW.name();
                        String message = (inDowntime ? "Start downtime" : "End downtime");
                        if (hostOrService.serviceDescription == null) {
                            message += " for Host " + hostOrService.hostName;
                        } else {
                            message += " for Service " + hostOrService.hostName + ":" + hostOrService.serviceDescription;
                        }
                        if ((hostOrService.entityType != null) && (hostOrService.entityName != null)) {
                            message += " from ";
                            if (Host.ENTITY_TYPE_CODE.equals(hostOrService.entityType)) {
                                message += "Host " + hostOrService.entityName;
                            } else if (ServiceStatus.ENTITY_TYPE_CODE.equals(hostOrService.entityType)) {
                                message += "Service " + hostOrService.entityName;
                            } else if (HostGroup.ENTITY_TYPE_CODE.equals(hostOrService.entityType)) {
                                message += "Host Group " + hostOrService.entityName;
                            } else if (CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP.equals(hostOrService.entityType)) {
                                message += "Service Group " + hostOrService.entityName;
                            } else {
                                message += "Unknown";
                            }
                        }
                        String nowAsString = formatDate(new Date());
                        logMessageProperties.put(LogMessage.EP_REPORT_DATE, nowAsString);
                        logMessageProperties.put(LogMessage.EP_LAST_INSERT_DATE, nowAsString);
                        String monitorStatus = IN_DOWNTIME_MONITOR_STATUS;
                        if (inDowntime && (hostOrService.scheduledDowntimeDepth == 1)) {
                            monitorStatus = START_DOWNTIME_MONITOR_STATUS;
                        } else if (!inDowntime && (hostOrService.scheduledDowntimeDepth == 0)) {
                            monitorStatus = END_DOWNTIME_MONITOR_STATUS;
                        }
                        logMessageProperties.put(LogMessage.EP_MONITOR_STATUS_NAME, monitorStatus);
                        adminService.updateLogMessage(DEFAULT_MONITOR_SERVER, DOWNTIME_APP_TYPE, device, severity, message, logMessageProperties);
                        if (downtimeThrottle > 0) {
                            Thread.sleep(downtimeThrottle);
                        }
                    }
                }
                if (session != null) {
                    session.releaseSession();
                }
            }
            catch (Exception e) {
                log.error("Failed to update LOG MESSAGE: " + e.getMessage(), e);
            }
            if (log.isDebugEnabled()) {
                log.debug("ENDING Downtime update LOG MESSAGES " + (System.currentTimeMillis() - start));
            }
            stopMetricsTimer(timer);
        }
    }

    @Override
    public List<HostServiceInDowntime> getHostsAndServicesInDowntime(List<HostServiceInDowntime> hostsAndServices) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("getHostsAndServicesInDowntime");
        // get Nagios scheduled downtime levels on in downtime hosts and services
        for (HostServiceInDowntime hostOrService : hostsAndServices) {
            hostOrService.scheduledDowntimeDepth = null;
            if (hostOrService.serviceDescription != null) {
                ServiceStatus serviceStatus = statusService.getServiceByDescription(hostOrService.serviceDescription, hostOrService.hostName);
                if (serviceStatus != null) {
                    Integer scheduledDowntimeDepth = (Integer)serviceStatus.getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH);
                    hostOrService.scheduledDowntimeDepth = ((scheduledDowntimeDepth != null) ? scheduledDowntimeDepth : new Integer(0));
                }
            } else {
                Host host = hostService.getHostByHostName(hostOrService.hostName);
                if (host != null) {
                    HostStatus hostStatus = host.getHostStatus();
                    Integer scheduledDowntimeDepth = ((hostStatus != null) ? (Integer)hostStatus.getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH) : null);
                    hostOrService.scheduledDowntimeDepth = ((scheduledDowntimeDepth != null) ? scheduledDowntimeDepth : new Integer(0));
                }
            }
        }
        stopMetricsTimer(timer);
        return hostsAndServices;
    }

    @Override
    public AuthorizedServices getAuthorizedServices() {
        CollageTimer timer = startMetricsTimer("getAuthorizedServices");
        // get all sorted host/service description pairs using optimized SQL query
        List<Object[]> hostNameServiceDescriptions = foundationDAO.sqlQuery(
                "select h.hostname, s.servicedescription from " +
                        "host h " +
                        "left outer join servicestatus s on (h.hostid = s.hostid) " +
                        "group by h.hostname, s.servicedescription " +
                        "order by h.hostname, s.servicedescription;");
        // extract and return authorized hosts and their services from query results
        List<String> authorizedHostsList = new ArrayList<String>();
        Map<String,Collection<String>> authorizedServicesLists = new LinkedHashMap<String,Collection<String>>();
        addHostNameServiceDescriptions(hostNameServiceDescriptions, true, authorizedHostsList, authorizedServicesLists, ArrayList.class);
        AuthorizedServices authorizedServices = makeAuthorizedServices(authorizedHostsList, authorizedServicesLists);
        stopMetricsTimer(timer);
        return authorizedServices;
    }

    @Override
    public AuthorizedServices getAuthorizedServices(List<String> authorizedHostGroups, List<String> authorizedServiceGroups) {
        CollageTimer timer = startMetricsTimer("getAuthorizedServices");
        // full authorization check
        if (((authorizedHostGroups == null) || authorizedHostGroups.isEmpty()) &&
                ((authorizedServiceGroups == null) || authorizedServiceGroups.isEmpty())) {
            return null;
        }
        // aggregate authorized hosts and services
        Set<String> authorizedHostsSet = new TreeSet<String>();
        Map<String,Collection<String>> authorizedServicesSets = new TreeMap<String,Collection<String>>();
        // authorized host group hosts and services
        if ((authorizedHostGroups != null) && !authorizedHostGroups.isEmpty()) {
            // get sorted host group host/service description pairs using optimized SQL query
            Map<String,Object> sqlQueryParameters = new HashMap<String,Object>();
            sqlQueryParameters.put("hgnames", authorizedHostGroups);
            List<Object[]> hostNameServiceDescriptions = foundationDAO.sqlQuery(
                    "select h.hostname, s.servicedescription from " +
                            "hostgroup hg " +
                            "join hostgroupcollection hgc on (hg.hostgroupid = hgc.hostgroupid) " +
                            "join host h on (hgc.hostid = h.hostid) " +
                            "left outer join servicestatus s on (h.hostid = s.hostid) " +
                            "where hg.name in (:hgnames) " +
                            "group by h.hostname, s.servicedescription " +
                            "order by h.hostname, s.servicedescription;",
                    sqlQueryParameters);
            // extract authorized hosts and their services from query results
            addHostNameServiceDescriptions(hostNameServiceDescriptions, true, authorizedHostsSet, authorizedServicesSets, TreeSet.class);
        }
        // authorized service group services
        if ((authorizedServiceGroups != null) && !authorizedServiceGroups.isEmpty()) {
            // get sorted service group host/service description pairs using optimized SQL query
            Map<String,Object> sqlQueryParameters = new HashMap<String,Object>();
            sqlQueryParameters.put("sentityid", metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS).getEntityTypeId());
            sqlQueryParameters.put("sgnames", authorizedServiceGroups);
            sqlQueryParameters.put("sgentityid", metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP).getEntityTypeId());
            List<Object[]> hostNameServiceDescriptions = foundationDAO.sqlQuery(
                    "select h.hostname, s.servicedescription from " +
                            "category c " +
                            "join categoryentity ce on (c.categoryid = ce.categoryid) " +
                            "join servicestatus s on (ce.entitytypeid = :sentityid and ce.objectid = s.servicestatusid) " +
                            "join host h on (s.hostid = h.hostid) " +
                            "where c.name in (:sgnames) and c.entitytypeid = :sgentityid " +
                            "group by h.hostname, s.servicedescription " +
                            "order by h.hostname, s.servicedescription;",
                    sqlQueryParameters);
            // extract authorized services from query results
            addHostNameServiceDescriptions(hostNameServiceDescriptions, false, authorizedHostsSet, authorizedServicesSets, TreeSet.class);
        }
        // return authorized services
        AuthorizedServices authorizedServices = makeAuthorizedServices(authorizedHostsSet, authorizedServicesSets);
        stopMetricsTimer(timer);
        return authorizedServices;
    }

    /**
     * Add host name/service description query results to hosts list and
     * service hosts lists.
     *
     * @param hostNameServiceDescriptions query results
     * @param addHosts whether to add host to hosts list, (implies all services)
     * @param hostsList returned hosts list
     * @param servicesLists returned service hosts lists
     * @param serviceHostsClass collection class used to hold service hosts lists
     */
    private void addHostNameServiceDescriptions(List<Object[]> hostNameServiceDescriptions, boolean addHosts, Collection<String> hostsList,
                                                Map<String,Collection<String>> servicesLists, Class<?> serviceHostsClass) {
        CollageTimer timer = startMetricsTimer("addHostNameServiceDescriptions");
        String lastHostName = null;
        for (Object [] hostNameServiceDescription : hostNameServiceDescriptions) {
            String hostName = (String)hostNameServiceDescription[0];
            String serviceDescription = (String)hostNameServiceDescription[1];
            // add hosts
            if (addHosts && ((lastHostName == null) || !hostName.equals(lastHostName))) {
                hostsList.add(hostName);
                lastHostName = hostName;
            }
            // add services hosts
            if (serviceDescription != null) {
                Collection<String> serviceHosts = servicesLists.get(serviceDescription);
                if (serviceHosts == null) {
                    try {
                        serviceHosts = (Collection<String>)serviceHostsClass.newInstance();
                    } catch (Exception e) {
                    }
                    servicesLists.put(serviceDescription, serviceHosts);
                }
                serviceHosts.add(hostName);
            }
        }
        stopMetricsTimer(timer);
    }

    /**
     * Construct AuthorizedServices from hosts list and service hosts lists.
     *
     * @param hostsList hosts list
     * @param servicesLists service hosts lists
     * @return AuthorizedServices instance
     */
    private AuthorizedServices makeAuthorizedServices(Collection<String> hostsList, Map<String,Collection<String>> servicesLists) {
        List<String> authorizedHostsList = new ArrayList<String>(hostsList);
        Map<String,List<String>> authorizedServicesLists = new LinkedHashMap<String,List<String>>();
        for (Map.Entry<String,Collection<String>> authorizedService : servicesLists.entrySet()) {
            authorizedServicesLists.put(authorizedService.getKey(), new ArrayList<String>(authorizedService.getValue()));
        }
        return new AuthorizedServices(authorizedHostsList, authorizedServicesLists);
    }

    /**
     * Create an event for given message
     *
     * @param hostName host name
     * @param serviceDescription service description or null
     * @param status status
     * @param message message
     * @param appType application type
     * @param deviceIdentification device identification
     * @param device device instance or null
     * @param host host instance or null
     * @param serviceStatus service status instance or null
     * @param now effective timestamp for event
     * @throws BusinessServiceException
     */
    protected void sendEvent(String hostName, String serviceDescription, String status, String message,
                             String deviceIdentification, String appType, Device device, Host host,
                             ServiceStatus serviceStatus, Date now)
            throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("sendEvent");
        try {
            String nowAsString = formatDate(now);
            String severity = CollageSeverity.LOW.name();
            Properties properties = Nagios.createLogMessageProps(
                    hostName,
                    status,
                    nowAsString,
                    nowAsString,
                    null, // component
                    null, // error type
                    serviceDescription,
                    null, // logger name
                    appType, // application type
                    null,
                    message);
            adminService.updateLogMessage(DEFAULT_MONITOR_SERVER, appType, deviceIdentification, severity,
                    message, device, host, serviceStatus, properties);
        }
        catch (CollageException e) {
            log.error(e);
            throw new BusinessServiceException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    protected void sendHostNotification(NomaHostNotification noma) throws BusinessServiceException {
        if (sendJMSNotifications) {
            NomaActions.cleanupHostData(noma);
            NomaActions.performAction(NomaActions.buildHostNomaNotification(noma), NomaActions.NOMA_HOST_ACTION);
        }
    }

    protected void sendServiceNotification(NomaServiceNotification noma) throws BusinessServiceException {
        if (sendJMSNotifications) {
            NomaActions.cleanupServiceData(noma);
            NomaActions.performAction(NomaActions.buildServiceNomaNotification(noma), NomaActions.NOMA_SERVICE_ACTION);
        }
    }

    protected CollageAdminInfrastructure getAdminInfrastructureService() {
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure) CollageFactory.getInstance()
                .getAPIObject("com.groundwork.collage.CollageAdmin");
        return admin;
    }

    protected boolean isEmptyParameter(String parameter) {
        return (parameter == null || parameter.trim().isEmpty());
    }

    public final static String lookupLastMonitorStatus(Host host) {
        HostStatus status = host.getHostStatus();
        if (status == null)
            return null;
        MonitorStatus monStatus = status.getHostMonitorStatus();
        if (monStatus == null)
            return null;
        return monStatus.getName();
    }

    protected String formatDate(Date date) {
        if (date != null) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return formatter.format(date);
        }
        return null;
    }

    private boolean getBooleanAndRemove(Map<String,String> properties, String key, boolean defaultValue) {
        if (properties != null) {
            String value = properties.get(key);
            if (value != null) {
                properties.remove(key);
                return Boolean.parseBoolean(value);
            }
        }
        return defaultValue;
    }

}

