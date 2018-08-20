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
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoHostNotification;
import org.groundwork.rs.dto.DtoHostNotificationList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;

import javax.servlet.ServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;

@Path("/hosts")
public class HostBridgeResource extends AbstractBridgeResource {

    protected static Log log = LogFactory.getLog(HostBridgeResource.class);

    @POST
    public String postHosts(@FormParam(PARAM_AGENT_ID) @DefaultValue("") String agentID ,
                            @FormParam(PARAM_TIMESTAMP) @DefaultValue("") String timeStamp,
                            @FormParam(PARAM_HOST) @DefaultValue("") String host,
                            @FormParam(PARAM_STATUS) @DefaultValue("") String status,
                            @FormParam(PARAM_MESSAGE) @DefaultValue("") String message,
                            @FormParam(PARAM_PERFORMANCE) @DefaultValue("") String performance,
                            @FormParam(PARAM_IP) @DefaultValue("") String ip,
                            @FormParam(PARAM_HOST_GROUP) @DefaultValue("") String hostGroups,
                            @Context ServletRequest request
    ) {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();

        if (!configuration.isAuditMode()) {
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
            log.debug(String.format("processing /POST on /hosts with host = %s and Hostgroups " , host, hostGroups));
        }
        DtoHost dtoHost = new DtoHost();
        boolean addedDefaultHostGroup = false;
        try {
            HostClient hostClient = GroundworkService.getHostClient();
            DtoHost existingHost = null;
            if (!configuration.isAuditMode()) {
                // Fill in defaults
                if (host.isEmpty()) {
                    if (!configuration.isProcessUnknownHosts()) {
                        return DROPPING_REQUEST_NO_HOST_PROVIDED;
                    }
                    host = configuration.getUnknownHost();
                }
                if (hostGroups.isEmpty()) {
                    hostGroups = configuration.getDefaultHostGroup();
                    addedDefaultHostGroup = true;
                }
                if (ip.isEmpty()) {
                    ip = host;
                }
                if (agentID.isEmpty()) {
                    agentID = getRemoteAddress(request, host);
                }
                if (message.isEmpty()) {
                    String errorMessage = "Host Request error: Parameter 'Message' is a required parameter";
                    log.error(errorMessage);
                    BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.WARNING, SeverityStatus.WARNING, OperationalStatus.OPEN);
                    return "Failed" + host + ": " + errorMessage;
                }
                if (!configuration.isValidAgent(agentID)) {
                    String errorMessage = "Invalid agent, not in white list: " + agentID;
                    log.error(errorMessage);
                    BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.WARNING, SeverityStatus.WARNING, OperationalStatus.OPEN);
                    return "Failed " + host + ": " + errorMessage;
                }
                if (configuration.isStatusSuffix()) {
                    message = message + SW_STATUS_POSTFIX + status;
                }
                existingHost = hostClient.lookup(host, DtoDepthType.Deep); // need deep to get host groups
            }
            DtoHostList updates = new DtoHostList();
            dtoHost.setAppType(SolarWindsConfiguration.instance().getAppType());
            dtoHost.setAgentId(agentID);
            dtoHost.setLastCheckTime((configuration.isUseSolarWindsTimestamps() ? parseDate(timeStamp) : new Date()));
            dtoHost.setHostName(host);
            dtoHost.setDescription(host);
            dtoHost.setMonitorStatus(configuration.translateStatus(status));
            dtoHost.setDeviceIdentification(ip);
            dtoHost.setDeviceDisplayName(ip);
            if (!performance.isEmpty()) {
                dtoHost.putProperty(MonitorProperty.PerformanceData.value(), performance);
            }
            dtoHost.putProperty(MonitorProperty.LastPluginOutput.value(), message); // TODO: formatPluginOutput(dtoHost, message));
            boolean stateChange = isStateChange(dtoHost, existingHost);
            if (stateChange) {
                dtoHost.putProperty("LastStateChange", dtoHost.getLastCheckTime());
            }
            // TODO: dtoHost.setNextCheckTime(nextCheckTime);
            if (dtoHost.getMonitorStatus().equals(MonitorStatus.SCHEDULED_DOWN.value())) {
                dtoHost.putProperty("isAcknowledged", Boolean.TRUE);
            }
            updates.add(dtoHost);

            if (configuration.isAuditMode()) {
                AuditLog.logHost(dtoHost);
                return "OK Host " + host + " logged and not stored (debug mode).";
            }
            DtoOperationResults results = hostClient.post(updates);
            if (results.getFailed() > 0) {
                String errorMessage =
                        (results.getResults().size() > 0) ? results.getResults().get(0).getMessage() : "(none)";
                String completeMessage = "failed to save host " + host + ": " + errorMessage;
                log.error(completeMessage);
                BridgeStatusService.logBridgeStatus(completeMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
                return (results.getResults().size() > 0) ? errorMessage : "Failed ";
            }

            // Update Host Groups
            if (!hostGroups.isEmpty()) {
                String defaultHostGroup = configuration.getDefaultHostGroup();
                if (addedDefaultHostGroup == false && configuration.isAddToDefaultHostGroup() && !defaultHostGroup.isEmpty()) {
                    hostGroups = hostGroups + "," + defaultHostGroup;
                }
                DtoHostGroupList hostGroupList = parseHostGroups(hostGroups, dtoHost);
                dtoHost.setHostGroups(hostGroupList.getHostGroups());
                if (hostGroupList.size() > 0) {
                    HostGroupClient hostGroupClient = GroundworkService.getHostGroupClient();
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

            if (log.isDebugEnabled()) {
                log.debug(String.format("completed /POST on /hosts with host = %s", host));
            }
//            if (!performance.isEmpty()) {
//                sendPerformanceData(host, null, performance);
//            }
            if (stateChange) {
                if (existingHost == null) {
                    sendNewHostEvent(dtoHost, message, agentID);
                }
                else {
                    sendModifyHostEvent(dtoHost, message, existingHost.getMonitorStatus(), agentID, configuration.isNotificationsEnabled());
                }
            }
            if (existingHost == null) {
                BridgeStatusService.logSolarWindsStatus("Host " + host + " added", agentID, MonitorStatus.WARNING, SeverityStatus.WARNING, OperationalStatus.OPEN);
            }
            BridgeStatusService.logSolarWindsStatus("Host " + host + " processed", agentID, MonitorStatus.OK, SeverityStatus.OK, OperationalStatus.OPEN);
            return "OK Host " + host + " stored.";

        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            BridgeStatusService.logBridgeStatus(e.getMessage(), agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            log.error(UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for host [%s].", host)).build());
        }
    }

    public boolean isStateChange(DtoHost host, DtoHost old) {
        if (old == null || host == null)
            return true;
        if (old.getMonitorStatus() == null || host.getMonitorStatus() == null)
            return true;
        if (host.getMonitorStatus().equals(old.getMonitorStatus()))
            return false;
        return true;

    }

    public void sendNewHostEvent(DtoHost host, String message, String agentID) {

        DtoEventList eventUpdates = new DtoEventList();
        DtoEvent event = new DtoEvent(host.getHostName(), OperationalStatus.OPEN.name(), MonitorStatus.PENDING.value(), SeverityStatus.LOW.name(), message);
        event.setAppType(host.getAppType());
        Date lastUpdate = host.getLastCheckTime();
        event.setReportDate(lastUpdate);
        event.setLastInsertDate(lastUpdate);
        event.setDevice(host.getDeviceIdentification());
        eventUpdates.add(event);
        DtoOperationResults results = GroundworkService.getEventClient().post(eventUpdates);
        for (DtoOperationResult result : results.getResults()) {
            if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                String errorMessage = "failed to send new host event for host " + host + ": " + result.getMessage();
                log.error(errorMessage);
                BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            }
        }
        if (results.getFailed() == 0 && log.isDebugEnabled()) {
            log.debug("sent event for new host " + host + ", message: " + message);
        }
    }

    public void sendModifyHostEvent(DtoHost host, String message, String previousState, String agentID, boolean isSendNotification) {
        DtoEventList eventUpdates = new DtoEventList();
        DtoEvent event = new DtoEvent(host.getHostName(), OperationalStatus.OPEN.name(),
                (previousState == null) ?  MonitorStatus.PENDING.value() : host.getMonitorStatus(),
                SeverityStatus.LOW.name(), message);
        event.setAppType(host.getAppType());
        event.setReportDate(host.getLastCheckTime());
        event.setDevice(host.getDeviceIdentification());
        eventUpdates.add(event);
        DtoOperationResults results = GroundworkService.getEventClient().post(eventUpdates);
        for (DtoOperationResult result : results.getResults()) {
            if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                String errorMessage = "failed to send mod host event for host " + host + ": " + result.getMessage();
                log.error(errorMessage);
                BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            }
        }
	    // Send Notification if State has changed and previous message was not PENDING
        if (isSendNotification && previousState.equalsIgnoreCase(MonitorStatus.PENDING.value()) == false) {
            sendHostNotification(host.getHostName(), host.getDeviceIdentification(),
                    host.getMonitorStatus(), message, makeHostGroupsList(host.getHostGroups()), agentID);
        }
        if (results.getFailed() == 0 && log.isDebugEnabled()) {
            log.debug("sent event for modified host " + host + ", message: " + message);
        }
    }

    protected void sendHostNotification(String hostName, String ipAddress,
                                                         String runState, String runExtra, String hostGroups, String agentID) {
        DtoHostNotificationList notifyUpdates = new DtoHostNotificationList();
        DtoHostNotification notification = new DtoHostNotification();
        notification.setHostState(runState);
        notification.setHostName(hostName);
        if (!hostGroups.isEmpty())
            notification.setHostGroupNames(hostGroups);
        notification.setNotificationType((runState.equalsIgnoreCase(MonitorStatus.UP.value()))
                ? NOTIFICATIONTYPE_RECOVERY
                : NOTIFICATIONTYPE_PROBLEM);
        notification.setHostAddress((ipAddress == null) ? hostName : ipAddress);
        notification.setHostOutput(runExtra);
        notification.setCheckDateTime(nowTime());
        notification.setNotificationComment("Solar Winds Host Notification");
        notifyUpdates.add(notification);
        DtoOperationResults results = GroundworkService.getNotificationClient().notifyHosts(notifyUpdates);
        for (DtoOperationResult result : results.getResults()) {
            if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                String errorMessage = "failed to send host notification for host " + hostName + ": " + result.getMessage();
                log.error(errorMessage);
                BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Host Notification sent to NoMa for host " + hostName);
        }
    }

}