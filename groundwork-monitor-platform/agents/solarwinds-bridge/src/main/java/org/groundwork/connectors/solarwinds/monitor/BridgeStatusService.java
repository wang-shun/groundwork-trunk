package org.groundwork.connectors.solarwinds.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.connectors.solarwinds.HostBridgeResource;
import org.groundwork.connectors.solarwinds.ServiceBridgeResource;
import org.groundwork.connectors.solarwinds.SolarWindsConfiguration;
import org.groundwork.connectors.solarwinds.gwos.GroundworkService;
import org.groundwork.connectors.solarwinds.status.MonitorProperty;
import org.groundwork.connectors.solarwinds.status.MonitorStatus;
import org.groundwork.connectors.solarwinds.status.OperationalStatus;
import org.groundwork.connectors.solarwinds.status.SeverityStatus;
import org.groundwork.rs.client.ApplicationTypeClient;
import org.groundwork.rs.client.DeviceClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoApplicationTypeList;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoDeviceList;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BridgeStatusService implements ServletContextListener {

    public static final String INITIAL_SETUP_WAITING_FOR_UPDATES = "Initial setup, waiting for updates";
    protected static Log log = LogFactory.getLog(HostBridgeResource.class);

    private static final ScheduledExecutorService heartbeat = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> heartbeatHandle = null;

    public static void logBridgeStatus(String message, String agentID, MonitorStatus status, SeverityStatus severity, OperationalStatus operational) {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        String hostName = configuration.getBridgeDevice();
        String serviceName = configuration.getBridgeService();

        ServiceClient serviceClient = GroundworkService.getServiceClient();
        DtoService existingService = serviceClient.lookup(serviceName, hostName);

        DtoServiceList updates = new DtoServiceList();
        DtoService dtoService = new DtoService();
        dtoService.setAppType(SolarWindsConfiguration.instance().getAppType());
        //dtoService.setAgentId(agentID);
        dtoService.setLastCheckTime(new Date());
        dtoService.setHostName(hostName);
        dtoService.setDescription(serviceName);
        dtoService.setMonitorStatus(status.value());
        dtoService.setLastHardState(status.value());
        //dtoService.setDeviceIdentification(ip);
        dtoService.putProperty(MonitorProperty.LastPluginOutput.value(), message);
        boolean stateChange = ServiceBridgeResource.isStateChange(dtoService, existingService);
        if (stateChange) {
            dtoService.setLastStateChange(dtoService.getLastCheckTime());
        }
        updates.add(dtoService);
        DtoOperationResults results = serviceClient.post(updates);
        if (results.getFailed() > 0) {
            log.error("failed to save service " + hostName + ":" + serviceName + ": " + results.getResults().get(0).getMessage());
            status = MonitorStatus.CRITICAL;
        }
        if (stateChange) {
            DtoEventList eventUpdates = new DtoEventList();
            DtoEvent event = new DtoEvent(hostName, operational.name(), status.value(), severity.name(), message);
            event.setAppType(SolarWindsConfiguration.instance().getAppType());
            Date lastUpdate = new Date();
            event.setReportDate(lastUpdate);
            event.setLastInsertDate(lastUpdate);
            event.setDevice(configuration.getBridgeDevice());
            event.setService(serviceName);
            eventUpdates.add(event);
            DtoOperationResults results2 = GroundworkService.getEventClient().post(eventUpdates);
            for (DtoOperationResult result : results2.getResults()) {
                if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                    log.error(String.format("failed to send bridge status event for %s:%s - error: %s",
                            hostName, serviceName, result.getMessage()));
                }
            }
        }
        if (results.getFailed() == 0 && log.isDebugEnabled()) {
            log.debug(String.format("sent bridge status event for %s:%s - message: %s",
                    hostName, serviceName, message));
        }

    }

    public static void logSolarWindsStatus(String message, final String agentID, MonitorStatus status, SeverityStatus severity, OperationalStatus operational) {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        String hostName = configuration.getBridgeDevice(); //configuration.getHostPrefix() + "_" + agentID;
        String serviceName = configuration.getSolarWindsService() + "-" + agentID;

        ServiceClient serviceClient = GroundworkService.getServiceClient();
        DtoService existingService = serviceClient.lookup(serviceName, hostName);

        DtoServiceList updates = new DtoServiceList();
        DtoService dtoService = new DtoService();
        dtoService.setAppType(SolarWindsConfiguration.instance().getAppType());
        //dtoService.setAgentId(agentID);
        dtoService.setLastCheckTime(new Date());
        dtoService.setHostName(hostName);
        dtoService.setDescription(serviceName);
        dtoService.setMonitorStatus(status.value());
        dtoService.setLastHardState(status.value());
        //dtoService.setDeviceIdentification(ip);
        dtoService.putProperty(MonitorProperty.LastPluginOutput.value(), message);
        boolean stateChange = ServiceBridgeResource.isStateChange(dtoService, existingService);
        if (stateChange) {
            dtoService.setLastStateChange(dtoService.getLastCheckTime());
        }
        updates.add(dtoService);
        DtoOperationResults results = serviceClient.post(updates);
        if (results.getFailed() > 0) {
            log.error("failed to save service " + hostName + ":" + serviceName + ": " + results.getResults().get(0).getMessage());
            status = MonitorStatus.CRITICAL;
        }
        if (stateChange) {

            DtoEventList eventUpdates = new DtoEventList();
            DtoEvent event = new DtoEvent(hostName, operational.name(), status.value(), severity.name(), message);
            event.setAppType(SolarWindsConfiguration.instance().getAppType());
            Date lastUpdate = new Date();
            event.setReportDate(lastUpdate);
            event.setLastInsertDate(lastUpdate);
            event.setDevice(configuration.getBridgeDevice());
            event.setService(serviceName);
            eventUpdates.add(event);
            DtoOperationResults results2 = GroundworkService.getEventClient().post(eventUpdates);
            for (DtoOperationResult result : results2.getResults()) {
                if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                    log.error(String.format("failed to send solarWinds status event for %s:%s - error: %s",
                            hostName, serviceName, result.getMessage()));
                }
            }
        }
        if (results.getFailed() == 0 && log.isDebugEnabled()) {
            log.debug(String.format("sent solarWinds status event for %s:%s - message: %s",
                    hostName, serviceName, message));
        }

    }

    public static void startHeartbeat(int heartbeatInSeconds, final String agentID) {
        final Runnable heartbeatTask = new Runnable() {
            public void run() {
                if (log.isDebugEnabled())
                    log.debug("**** heartbeat running ...");
                logBridgeStatus("Heartbeat...", agentID, MonitorStatus.OK, SeverityStatus.OK, OperationalStatus.OPEN);
                if (log.isDebugEnabled())
                    log.debug("**** heartbeat done");
            }
        };
        heartbeatHandle = heartbeat.scheduleAtFixedRate(heartbeatTask, heartbeatInSeconds, heartbeatInSeconds, TimeUnit.SECONDS);
    }


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        int heartbeatInSeconds = configuration.getBridgeHeartbeat();
        if (log.isInfoEnabled())
            log.info("Starting Solarwinds Heartbeat service with heart beat in seconds of " + heartbeatInSeconds);
        for (int ix = 0; ix < configuration.getPingAPIRetries(); ix++) {
            if (ping())
                break;
            try {
                Thread.sleep(configuration.getPingAPISleep() * 1000);
            }
            catch (Exception e) {
                break;
            }
        }
        if (!initializeBridge()) {
            throw new RuntimeException("Failed to initialize bridge");
        }
        startHeartbeat(heartbeatInSeconds, SolarWindsConfiguration.instance().getUnknownHost());
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (heartbeatHandle != null) {
            heartbeatHandle.cancel(true);
        }
    }

    /**
     * Initialize the bridge with default values
     *
     * @return true if success, false on failure
     */
    public boolean initializeBridge() {
        if (log.isInfoEnabled()) {
            log.info("start: initializing solar winds bridge");
        }
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        ApplicationTypeClient applicationTypeClient = GroundworkService.getApplicationTypeClient();
        DeviceClient deviceClient = GroundworkService.getDeviceClient();
        HostGroupClient hostGroupClient = GroundworkService.getHostGroupClient();
        HostClient hostClient = GroundworkService.getHostClient();
        ServiceClient serviceClient = GroundworkService.getServiceClient();

        // create Application Type if it doesn't exist
        if (null == applicationTypeClient.lookup(SolarWindsConfiguration.instance().getAppType())) {
            if (log.isDebugEnabled()) {
                log.debug("start: setting up application type");
            }
            DtoApplicationTypeList updates = new DtoApplicationTypeList();
            DtoApplicationType applicationType = new DtoApplicationType();
            applicationType.setName(configuration.getAppType());
            applicationType.setDescription(configuration.getAppTypeDescription());
            applicationType.setStateTransitionCriteria(configuration.getAppTypeCriteria());
// DST: TODO: comment out until we can get the referential integrity problems fixed
//            int max = (configuration.getAppTypeEntities().size() > configuration.getAppTypeProperties().size()) ?
//                    configuration.getAppTypeProperties().size() : configuration.getAppTypeEntities().size();
//            for (int index = 0; index < max; index++) {
//                applicationType.addEntityProperty(
//                        new DtoEntityProperty(configuration.getAppTypeProperties().get(index),
//                                              configuration.getAppTypeEntities().get(index)));
//            }
            updates.add(applicationType);
            DtoOperationResults results = applicationTypeClient.post(updates);
            if (results.getFailed() > 0 && results.getResults().size() > 0) {
                log.error(String.format("Failed to create app type %s for criteria %s, error: %s", applicationType.getName(),
                        applicationType.getStateTransitionCriteria(), results.getResults().get(0).getMessage()));
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("end: setting up application type");
            }
        }

        // create Bridge device if it doesn't already exist
        if (null == deviceClient.lookup(configuration.getBridgeDevice())) {
            if (log.isDebugEnabled()) {
                log.debug("start: setting up default device");
            }
            DtoDeviceList updates = new DtoDeviceList();
            DtoDevice device = new DtoDevice();
            device.setDescription(configuration.getBridgeDevice());
            device.setDisplayName(configuration.getBridgeDevice());
            device.setIdentification(configuration.getBridgeDevice());
            updates.add(device);
            DtoOperationResults results = deviceClient.post(updates);
            if (results.getFailed() > 0 && results.getResults().size() > 0) {
                log.error(String.format("Failed to create device %s , error: %s", device.getIdentification(),
                            results.getResults().get(0).getMessage()));
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("end: setting up default device");
            }
        }

        // create unknown host if it doesn't already exist
        boolean addedUnknownHost = false;
        boolean addedBridgeHost = false;
        DtoHost unknownHost = hostClient.lookup(configuration.getUnknownHost());
        if (null == unknownHost) {
            if (log.isDebugEnabled()) {
                log.debug("start: setting up default host");
            }
            DtoHostList updates = new DtoHostList();
            DtoHost host = new DtoHost();
            host.setAppType(SolarWindsConfiguration.instance().getAppType());
            //host.setAgentId(agentID);
            host.setLastCheckTime(new Date());
            host.setHostName(configuration.getUnknownHost());
            host.setDescription(configuration.getUnknownHost());
            host.setMonitorStatus(MonitorStatus.PENDING.value());
            host.setDeviceIdentification(configuration.getBridgeDevice());
            host.putProperty(MonitorProperty.LastPluginOutput.value(), INITIAL_SETUP_WAITING_FOR_UPDATES);
            updates.add(host);
            DtoOperationResults results = hostClient.post(updates);
            if (results.getFailed() > 0 && results.getResults().size() > 0) {
                log.error(String.format("Failed to create unknown host %s, error: %s", host.getHostName(),
                        results.getResults().get(0).getMessage()));
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("end: setting up default host");
            }
            if (results.getSuccessful() > 0) {
                unknownHost = hostClient.lookup(configuration.getUnknownHost());
                addedUnknownHost = true;
            }
        }

        //Check if Host Device_Bridge (localhost from config file) exists. If not create it and add it to the HostGroup.
        DtoHost bridgeHost = hostClient.lookup(configuration.getBridgeDevice());
        if (null == bridgeHost) {
            if (log.isDebugEnabled()) {
                log.debug("start: setting up bridge host");
            }
            DtoHostList updates = new DtoHostList();
            DtoHost host = new DtoHost();
            host.setAppType(SolarWindsConfiguration.instance().getAppType());
            //host.setAgentId(agentID);
            host.setLastCheckTime(new Date());
            host.setHostName(configuration.getBridgeDevice());
            host.setDescription(configuration.getBridgeDevice());
            host.setMonitorStatus(MonitorStatus.PENDING.value());
            host.setDeviceIdentification(configuration.getBridgeDevice());

            host.putProperty(MonitorProperty.LastPluginOutput.value(), INITIAL_SETUP_WAITING_FOR_UPDATES);
            updates.add(host);
            DtoOperationResults results = hostClient.post(updates);
            if (results.getFailed() > 0 && results.getResults().size() > 0) {
                log.error(String.format("Failed to create bridge host %s , error: %s", host.getHostName(),
                        results.getResults().get(0).getMessage()));
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("end: setting up bridge host");
            }
            if (results.getSuccessful() > 0) {
                bridgeHost = hostClient.lookup(configuration.getBridgeDevice());
                addedBridgeHost = true;
            }
        }

        // create Default_HostGroup if doesn't already exist
        DtoHostGroup defaultHostGroup = hostGroupClient.lookup(configuration.getDefaultHostGroup());
        if (null == defaultHostGroup || addedBridgeHost || addedUnknownHost) {
            if (log.isDebugEnabled()) {
                log.debug("start: setting up default host group");
            }
            DtoHostGroupList updates = new DtoHostGroupList();
            DtoHostGroup hostGroup = new DtoHostGroup();
            hostGroup.setName(configuration.getDefaultHostGroup());
            hostGroup.setDescription(configuration.getDefaultHostGroup());
            hostGroup.setAppType(configuration.getAppType());
            if (unknownHost != null)
                hostGroup.addHost(unknownHost);
            if (bridgeHost != null)
                hostGroup.addHost(bridgeHost);
            //hostGroup.setAgentId();
            //hostGroup.setAlias();
            updates.add(hostGroup);
            DtoOperationResults results = hostGroupClient.post(updates);
            if (results.getFailed() > 0 && results.getResults().size() > 0) {
                log.error(String.format("Failed to create host group %s , error: %s", hostGroup.getName(),
                        results.getResults().get(0).getMessage()));
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("end: setting up default host group");
            }
        }

        // create unknown service if it doesn't already exist
        DtoService unknownService = serviceClient.lookup(configuration.getUnknownService(), configuration.getUnknownHost());
        if (null == unknownService) {
            if (log.isDebugEnabled()) {
                log.debug("start: setting up unknown service");
            }
            DtoServiceList updates = new DtoServiceList();
            DtoService dtoService = new DtoService();
            dtoService.setAppType(SolarWindsConfiguration.instance().getAppType());
            //dtoService.setAgentId(agentID);
            dtoService.setLastCheckTime(new Date());
            dtoService.setHostName(unknownHost.getHostName());
            dtoService.setDescription(configuration.getUnknownService());
            dtoService.setMonitorStatus(MonitorStatus.PENDING.value());
            dtoService.setLastHardState(MonitorStatus.PENDING.value());
            dtoService.setDeviceIdentification(configuration.getBridgeDevice());
            dtoService.putProperty(MonitorProperty.LastPluginOutput.value(), INITIAL_SETUP_WAITING_FOR_UPDATES);
            updates.add(dtoService);
            DtoOperationResults results = serviceClient.post(updates);
            if (results.getFailed() > 0 && results.getResults().size() > 0) {
                log.error(String.format("Failed to create service %s:%s , error: %s", dtoService.getHostName(),
                        dtoService.getDescription(), results.getResults().get(0).getMessage()));
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("end: setting up unknown service");
            }
        }

        // create Bridge service if it doesn't already exist
        DtoService bridgeService = serviceClient.lookup(configuration.getBridgeService(), configuration.getBridgeDevice());
        if (null == bridgeService) {
            if (log.isDebugEnabled()) {
                log.debug("start: setting up bridge service");
            }
            DtoServiceList updates = new DtoServiceList();
            DtoService dtoService = new DtoService();
            dtoService.setAppType(SolarWindsConfiguration.instance().getAppType());
            //dtoService.setAgentId(agentID);
            dtoService.setLastCheckTime(new Date());
            dtoService.setHostName(configuration.getBridgeDevice());
            dtoService.setDescription(configuration.getBridgeService());
            dtoService.setMonitorStatus(MonitorStatus.PENDING.value());
            dtoService.setLastHardState(MonitorStatus.PENDING.value());
            dtoService.setDeviceIdentification(configuration.getBridgeDevice());
            dtoService.putProperty(MonitorProperty.LastPluginOutput.value(), INITIAL_SETUP_WAITING_FOR_UPDATES);
            updates.add(dtoService);
            DtoOperationResults results = serviceClient.post(updates);
            if (results.getFailed() > 0 && results.getResults().size() > 0) {
                log.error(String.format("Failed to create service %s:%s , error: %s", dtoService.getHostName(),
                        dtoService.getDescription(), results.getResults().get(0).getMessage()));
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("end: setting up bridge service");
            }
        }

        logBridgeStatus("Bridge started " + HostBridgeResource.nowTime(), configuration.getBridgeDevice(),
                            MonitorStatus.OK, SeverityStatus.OK, OperationalStatus.OPEN);

        if (log.isInfoEnabled()) {
            log.info("complete: initializing solar winds bridge");
        }
        return true;
    }

    public boolean ping() {
        HostClient hostClient = GroundworkService.getHostClient();
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        try {
            DtoHost host = hostClient.lookup(configuration.getBridgeDevice());
        }
        catch (Exception e) {
            log.error("Solar Winds Bridge failed to ping server...");
            return false;
        }
        return true;
    }
}
