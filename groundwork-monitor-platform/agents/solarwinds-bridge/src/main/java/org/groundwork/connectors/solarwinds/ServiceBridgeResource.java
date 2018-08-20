package org.groundwork.connectors.solarwinds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.connectors.solarwinds.audit.AuditLog;
import org.groundwork.connectors.solarwinds.gwos.GroundworkService;
import org.groundwork.connectors.solarwinds.monitor.BridgeStatusService;
import org.groundwork.connectors.solarwinds.status.MonitorProperty;
import org.groundwork.connectors.solarwinds.status.MonitorStatus;
import org.groundwork.connectors.solarwinds.status.OperationalStatus;
import org.groundwork.connectors.solarwinds.status.SeverityStatus;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;
import org.groundwork.rs.dto.DtoServiceNotification;
import org.groundwork.rs.dto.DtoServiceNotificationList;

import javax.servlet.ServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;

@Path("/services")
public class ServiceBridgeResource extends AbstractBridgeResource {

    protected static Log log = LogFactory.getLog(ServiceBridgeResource.class);

    @POST
    public String postServices(@FormParam(PARAM_SERVICE) @DefaultValue("") String service,
                               @FormParam(PARAM_AGENT_ID) @DefaultValue("") String agentID,
                               @FormParam(PARAM_TIMESTAMP) @DefaultValue("") String timeStamp,
                               @FormParam(PARAM_HOST) @DefaultValue("") String host,
                               @FormParam(PARAM_STATUS) @DefaultValue("") String status,
                               @FormParam(PARAM_MESSAGE) @DefaultValue("") String message,
                               @FormParam(PARAM_PERFORMANCE) @DefaultValue("") String performance,
                               @FormParam(PARAM_HOST_GROUP) @DefaultValue("") String hostGroups,
                               @FormParam(PARAM_IP) @DefaultValue("") String ip,
                               @Context ServletRequest request
    ) {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();

        if (!configuration.isAuditMode()) {
            service = service.trim();
            service = service.replace('/', '-'); // temporary fix for possible encoding bug in tomcat
            agentID = agentID.trim();
            timeStamp = timeStamp.trim();
            host = host.trim();
            status = status.trim();
            message = message.trim();
            performance = performance.trim();
            ip = ip.trim();
            hostGroups = hostGroups.trim();
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /services with service = %s, host = %s, Hostgroup = %s", service, host, hostGroups));
        }
        DtoService dtoService = new DtoService();
        try {
            ServiceClient serviceClient = GroundworkService.getServiceClient();
            DtoService existingService = null;
           if (!configuration.isAuditMode()) {
                // Fill in defaults
                if (service.isEmpty()) {
                    if (!configuration.isProcessUnknownServices()) {
                        return DROPPING_REQUEST_NO_SERVICE_PROVIDED;
                    }
                    service = configuration.getUnknownService();
                }
                if (host.isEmpty()) {
                    if (!configuration.isProcessUnknownHosts()) {
                        return DROPPING_REQUEST_NO_HOST_PROVIDED;
                    }
                    host = configuration.getUnknownHost();
                }
                if (ip.isEmpty()) {
                    ip = host;
                }
                if (agentID.isEmpty()) {
                    agentID = getRemoteAddress(request, host);
                }
                // Validations
                if (message.isEmpty()) {
                    String errorMessage = "Service Request error: Parameter 'Message' is a required parameter";
                    log.error(errorMessage);
                    BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.WARNING, SeverityStatus.WARNING, OperationalStatus.OPEN);
                    return "Failed " + host + ":" + service + ": " + errorMessage;
                }
                if (!configuration.isValidAgent(agentID)) {
                    String errorMessage = "Invalid agent, not in white list: " + agentID;
                    log.error(errorMessage);
                    BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.WARNING, SeverityStatus.WARNING, OperationalStatus.OPEN);
                    return "Failed " + host + ":" + service + ": " + errorMessage;
                }

                existingService = serviceClient.lookup(service, host);
                createHostOnService(host, status, agentID, ip, timeStamp, hostGroups);
            }

            DtoServiceList updates = new DtoServiceList();
            dtoService.setAppType(SolarWindsConfiguration.instance().getAppType());
            dtoService.setAgentId(agentID);
            dtoService.setLastCheckTime((configuration.isUseSolarWindsTimestamps() ? parseDate(timeStamp) : new Date()));
            dtoService.setHostName(host);
            dtoService.setDescription(service);
            String translatedStatus = configuration.translateStatus(status);
            dtoService.setMonitorStatus(translatedStatus);
            dtoService.setLastHardState(translatedStatus);
            dtoService.setDeviceIdentification(ip);
            if (!performance.isEmpty()) {
                dtoService.putProperty(MonitorProperty.PerformanceData.value(), performance);
            }
            if (configuration.isStatusSuffix()) {
                message = message + SW_STATUS_POSTFIX + status;
            }
            dtoService.putProperty(MonitorProperty.LastPluginOutput.value(), message);
            boolean stateChange = isStateChange(dtoService, existingService);
            if (stateChange) {
                dtoService.setLastStateChange(dtoService.getLastCheckTime());
            }
            updates.add(dtoService);
            if (configuration.isAuditMode()) {
                AuditLog.logService(dtoService);
                return "OK Service " + host + ":" + service + " logged and not stored (debug mode).";
            }

            DtoOperationResults results = serviceClient.post(updates);
            if (results.getFailed() > 0) {
                String errorMessage =
                        (results.getResults().size() > 0) ? results.getResults().get(0).getMessage() : "(none)";
                String completeMessage = "failed to save service " + host + ":" + service + ": " + errorMessage;
                log.error(completeMessage);
                BridgeStatusService.logBridgeStatus(completeMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
                return (results.getResults().size() > 0) ?
                        results.getResults().get(0).getMessage() : "Failed ";
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("completed /POST on /services with service = %s, host =", service, host));
            }
            if (!performance.isEmpty()) {
                sendPerformanceData(host, service, performance, agentID);
            }
            if (stateChange) {
                if (existingService == null) {
                    sendNewServiceEvent(dtoService, message, agentID);
                } else {
                    sendModifyServiceEvent(dtoService, message, existingService.getMonitorStatus(), agentID, configuration.isNotificationsEnabled());
                }
            }
            if (existingService == null) {
                BridgeStatusService.logSolarWindsStatus("Service " + host + ":" + service + " added", agentID, MonitorStatus.WARNING, SeverityStatus.WARNING, OperationalStatus.OPEN);
            }
            BridgeStatusService.logSolarWindsStatus("Service " + host + ":" + service + " processed", agentID, MonitorStatus.OK, SeverityStatus.OK, OperationalStatus.OPEN);
            return "OK Service " + service + " stored.";

        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            BridgeStatusService.logBridgeStatus(e.getMessage(), agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            log.error(UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for service [%s].", service)).build());
        }
    }

    public static boolean isStateChange(DtoService service, DtoService old) {
        if (old == null || service == null)
            return true;
        if (old.getMonitorStatus() == null || service.getMonitorStatus() == null)
            return true;
        if (service.getMonitorStatus().equals(old.getMonitorStatus()))
            return false;
        return true;
    }

    public void sendNewServiceEvent(DtoService service, String message, String agentID) {

        DtoEventList eventUpdates = new DtoEventList();
        DtoEvent event = new DtoEvent(service.getHostName(), OperationalStatus.OPEN.name(), service.getMonitorStatus(), SeverityStatus.LOW.name(), message);
        event.setAppType(service.getAppType());
        Date lastUpdate = service.getLastCheckTime();
        event.setReportDate(lastUpdate);
        event.setLastInsertDate(lastUpdate);
        event.setDevice(service.getDeviceIdentification());
        event.setService(service.getDescription());
        eventUpdates.add(event);
        DtoOperationResults results = GroundworkService.getEventClient().post(eventUpdates);
        for (DtoOperationResult result : results.getResults()) {
            if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                String errorMessage = String.format("failed to send new service event for %s:%s - error: %s",
                        service.getHostName(), service.getDescription(), result.getMessage());
                log.error(errorMessage);
                BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            }
        }
        if (results.getFailed() == 0 && log.isDebugEnabled()) {
            log.debug(String.format("sent event for new service %s:%s",
                    service.getHostName(), service.getDescription()));
        }

    }

    public void sendModifyServiceEvent(DtoService service, String message, String previousState, String agentID, boolean isSendNotifications) {
        DtoEventList eventUpdates = new DtoEventList();
        DtoEvent event = new DtoEvent(service.getHostName(), OperationalStatus.OPEN.name(),
                service.getMonitorStatus(),
                SeverityStatus.LOW.name(), message);
        event.setAppType(service.getAppType());
        Date lastUpdate = service.getLastCheckTime();
        event.setReportDate(lastUpdate);
        event.setLastInsertDate(lastUpdate);
        event.setDevice(service.getDeviceIdentification());
        event.setService(service.getDescription());
        eventUpdates.add(event);
        DtoOperationResults results = GroundworkService.getEventClient().post(eventUpdates);
        for (DtoOperationResult result : results.getResults()) {
            if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                String errorMessage = String.format("failed to send update service event for %s:%s - error: %s",
                        service.getHostName(), service.getDescription(), result.getMessage());
                log.error(errorMessage);
                BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            }
        }
        // Send Notification if State has changed and previous message was not PENDING
        if (isSendNotifications && previousState.equalsIgnoreCase(MonitorStatus.PENDING.value()) == false) {
            sendServiceNotification(service.getHostName(), service.getDescription(), service.getDeviceIdentification(),
                    service.getMonitorStatus(), message, agentID);
        }
        if (results.getFailed() == 0 && log.isDebugEnabled()) {
            log.debug(String.format("sent event for new service %s:%s",
                    service.getHostName(), service.getDescription()));
        }
    }

    protected void sendServiceNotification(String hostName, String serviceDescription, String ipAddress,
                                                             String runState, String runExtra, String agentID) {
        DtoServiceNotificationList notifyUpdates = new DtoServiceNotificationList();
        DtoServiceNotification notification = new DtoServiceNotification();
        notification.setServiceDescription(serviceDescription);
        notification.setHostName(hostName);
        notification.setServiceState(runState);
        notification.setHostAddress((ipAddress == null) ? hostName : ipAddress);
        notification.setNotificationType((runState.equalsIgnoreCase("OK"))
                ? NOTIFICATIONTYPE_RECOVERY
                : NOTIFICATIONTYPE_PROBLEM);
        notification.setServiceOutput(runExtra);
        notification.setCheckDateTime(nowTime());
        notification.setNotificationComment("Solar Winds Service Notification");
        notifyUpdates.add(notification);
        DtoOperationResults results = GroundworkService.getNotificationClient().notifyServices(notifyUpdates);
        for (DtoOperationResult result : results.getResults()) {
            if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                String errorMessage = String.format("failed to send service notification for %s:%s - error: %s",
                        hostName, serviceDescription, result.getMessage());
                log.error(errorMessage);
                BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Service Notification sent to NoMa for host:service " + hostName + ":" + serviceDescription);
        }

    }

    public void createHostOnService(String host, String status, String agentID, String ip, String timeStamp, String hostGroups) {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        HostClient hostClient = GroundworkService.getHostClient();
        DtoHostList updates = new DtoHostList();
        DtoHost dtoHost = new DtoHost();
        dtoHost.setAppType(SolarWindsConfiguration.instance().getAppType());
        dtoHost.setAgentId(agentID);
        dtoHost.setLastCheckTime((configuration.isUseSolarWindsTimestamps() ? parseDate(timeStamp) : new Date()));
        dtoHost.setHostName(host);
        dtoHost.setDescription(host);
        dtoHost.setMonitorStatus(MonitorStatus.UP.value());
        dtoHost.setDeviceIdentification(ip);
        dtoHost.setDeviceDisplayName(ip);
        dtoHost.putProperty(MonitorProperty.LastPluginOutput.value(), "Received Service Update");
        updates.add(dtoHost);
        DtoOperationResults results = hostClient.post(updates);
        if (results.getFailed() > 0) {
            String errorMessage =
                    (results.getResults().size() > 0) ? results.getResults().get(0).getMessage() : "(none)";
            String completeMessage = "failed to save host " + host + ": " + errorMessage;
            log.error(completeMessage);
            BridgeStatusService.logBridgeStatus(completeMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
        }

        String defaultHostGroup = configuration.getDefaultHostGroup();
        if (configuration.isAddToDefaultHostGroup()) {
            if (hostGroups.isEmpty())
                hostGroups = defaultHostGroup;
            else
                hostGroups = hostGroups + "," + defaultHostGroup;
        }
        HostGroupClient hostGroupClient = GroundworkService.getHostGroupClient();
        DtoHostGroupList hostGroupList = parseHostGroups(hostGroups, dtoHost);
        dtoHost.setHostGroups(hostGroupList.getHostGroups());
        if (hostGroupList.size() > 0) {
            DtoOperationResults hgResult = hostGroupClient.post(hostGroupList);
            if (hgResult.getFailed() > 0) {
                String errorMessage =
                        (hgResult.getResults().size() > 0) ? hgResult.getResults().get(0).getMessage() : "(none)";
                String completeMessage = "failed to add host groups for host " + host + ", groups " + hostGroups + ": " + errorMessage;
                log.error(completeMessage);
                BridgeStatusService.logBridgeStatus(completeMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("posted to host group client for host " + host + ", groups " + hostGroups);
                }
            }
        }
    }



}