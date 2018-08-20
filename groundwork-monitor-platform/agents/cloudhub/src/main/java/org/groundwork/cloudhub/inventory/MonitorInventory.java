/*
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.cloudhub.inventory;

import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.gwos.GwosServiceStatus;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostNotification;
import org.groundwork.rs.dto.DtoPerfData;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.dto.DtoServiceNotification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MonitorInventory
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class MonitorInventory {

    public static final String SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME = "ScheduledDowntimeDepth";
    public static final String LAST_STATE_CHANGE_PROPERTY_NAME = "LastStateChange";
    public static final String LAST_PLUGIN_OUTPUT_PROPERTY_NAME = "LastPluginOutput";
    public static final String LAST_CHECK_TIME_PROPERTY_NAME = "LastCheckTime";
    public static final String NEXT_CHECK_TIME_PROPERTY_NAME = "NextCheckTime";
    public static final String PERFORMANCE_DATA_PROPERTY_NAME = "PerformanceData";
    public static final String ACKNOWLEDGED_PROPERTY_NAME = "isAcknowledged";
    public static final String PROBLEM_ACKNOWLEDGED_PROPERTY_NAME = "isProblemAcknowledged";
    public static final String COMMENTS_PROPERTY_NAME = "Comments";

    protected static final String COMMENTS_SEPARATOR = "#!#";
    protected static final String COMMENTS_FIELD_SEPARATOR = ";::;";

    protected static final SimpleDateFormat GWOS_DATE_FORMAT = new SimpleDateFormat(ConnectorConstants.gwosDateFormat);

    protected String monitorServer;
    protected String appType;
    protected String agentId;

    protected Map<String,DtoHost> hosts = new ConcurrentHashMap<String,DtoHost>();
    protected Map<String,DtoHostGroup> hostGroups = new ConcurrentHashMap<String,DtoHostGroup>();
    protected Map<String,DtoService> services = new ConcurrentHashMap<String,DtoService>();
    protected Map<String,DtoServiceGroup> serviceGroups = new ConcurrentHashMap<String,DtoServiceGroup>();

    /**
     * Construct monitor inventory.
     *
     * @param monitorServer connection monitor server
     * @param appType info application type
     * @param agentId info agent id
     */
    public MonitorInventory(String monitorServer, String appType, String agentId) {
        this.monitorServer = monitorServer;
        this.appType = appType;
        this.agentId = agentId;
    }

    /**
     * Construct empty monitor inventory copy.
     *
     * @param inventory copy monitor inventory.
     */
    public MonitorInventory(MonitorInventory inventory) {
        this(inventory.getMonitorServer(), inventory.getAppType(), inventory.getAgentId());
    }

    /**
     * Inventory is empty.
     *
     * @return empty
     */
    public boolean isEmpty() {
        return (hosts.isEmpty() && hostGroups.isEmpty() && services.isEmpty() && serviceGroups.isEmpty());
    }

    /**
     * Add inventory service to host and inventory.
     *
     * @param hostName service host name
     * @param serviceDescription service description
     * @param scheduledDowntimeDepth service scheduled downtime depth
     * @param monitorStatus service monitor status
     * @param lastStateChange service last state change
     * @param lastCheckTime service last check time
     * @param nextCheckTime service next check time
     * @param stateType service state type
     * @param lastHardState service last hard state
     * @param checkType service check type
     * @param lastPlugInOutput service last plugin output
     * @param comments service comments
     * @param acknowledged service acknowledged
     * @param dtoHost service inventory host
     * @return service inventory name
     */
    public String addService(String hostName, String serviceDescription, String scheduledDowntimeDepth,
                             String monitorStatus, Date lastStateChange, Date lastCheckTime, Date nextCheckTime,
                             String stateType, String lastHardState, String checkType, String lastPlugInOutput,
                             String comments, String acknowledged, DtoHost dtoHost) {
        DtoService dtoService = new DtoService();
        dtoService.setHostName(hostName);
        dtoService.setDescription(serviceDescription);
        dtoService.setMonitorServer(monitorServer);
        dtoService.setAppType(appType);
        dtoService.setAgentId(agentId);
        dtoService.putProperty(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME, scheduledDowntimeDepth);
        dtoService.setMonitorStatus(monitorStatus);
        dtoService.setLastStateChange(lastStateChange);
        dtoService.setLastCheckTime(lastCheckTime);
        dtoService.setNextCheckTime(nextCheckTime);
        dtoService.setStateType(stateType);
        dtoService.setLastHardState(lastHardState);
        dtoService.setCheckType(checkType);
        dtoService.setLastPlugInOutput(lastPlugInOutput);
        if (dtoService.getLastPlugInOutput() != null) {
            dtoService.putProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME, dtoService.getLastPlugInOutput());
        }
        dtoService.putProperty(COMMENTS_PROPERTY_NAME, comments);
        dtoService.putProperty(PROBLEM_ACKNOWLEDGED_PROPERTY_NAME, acknowledged);
        String serviceInventoryKey = dtoService.getHostName() + "!" + dtoService.getDescription();
        services.put(serviceInventoryKey, dtoService);
        dtoHost.addService(dtoService);
        return serviceInventoryKey;
    }

    /**
     * Build state changed event for inventory host.
     *
     * @param dtoHost inventory host
     * @return inventory event
     */
    public DtoEvent buildDtoEventInventory(DtoHost dtoHost) {
        DtoEvent dtoEvent = new DtoEvent();
        dtoEvent.setAppType(dtoHost.getAppType());
        dtoEvent.setApplicationName(dtoHost.getAppType());
        dtoEvent.setMonitorServer(dtoHost.getMonitorServer());
        dtoEvent.setSeverity("LOW");
        dtoEvent.setOperationStatus("OPEN");
        dtoEvent.setHost(dtoHost.getHostName());
        dtoEvent.setDevice(dtoHost.getDeviceIdentification());
        dtoEvent.setReportDate(dtoHost.getLastCheckTime());
        dtoEvent.setLastInsertDate(dtoHost.getLastCheckTime());
        dtoEvent.setMonitorStatus(dtoHost.getMonitorStatus());
        dtoEvent.setStateChanged(true);
        dtoEvent.setTextMessage(dtoHost.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME));
        return dtoEvent;
    }

    /**
     * Build state changed event for inventory service. Uses inventory
     * host to complete event.
     *
     * @param dtoService inventory service
     * @return inventory event
     */
    public DtoEvent buildDtoEventInventory(DtoService dtoService) {
        return buildDtoEventInventory(dtoService, null);
    }

    /**
     * Build state changed event for inventory service and host.
     *
     * @param dtoService inventory service
     * @param dtoHost inventory host
     * @return inventory event
     */
    public DtoEvent buildDtoEventInventory(DtoService dtoService, DtoHost dtoHost) {
        // lookup service host if not specified
        if (dtoHost == null) {
            dtoHost = getHosts().get(dtoService.getHostName());
        }
        // build and return event
        DtoEvent dtoEvent = new DtoEvent();
        dtoEvent.setAppType(dtoService.getAppType());
        dtoEvent.setApplicationName(dtoService.getAppType());
        dtoEvent.setMonitorServer(dtoService.getMonitorServer());
        dtoEvent.setSeverity("LOW");
        dtoEvent.setOperationStatus("OPEN");
        dtoEvent.setHost(dtoService.getHostName());
        if (dtoHost != null) {
            dtoEvent.setDevice(dtoHost.getDeviceIdentification());
        }
        dtoEvent.setService(dtoService.getDescription());
        dtoEvent.setReportDate(dtoService.getLastCheckTime());
        dtoEvent.setLastInsertDate(dtoService.getLastCheckTime());
        dtoEvent.setMonitorStatus(dtoService.getMonitorStatus());
        dtoEvent.setStateChanged(true);
        dtoEvent.setTextMessage(dtoService.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME));
        return dtoEvent;
    }

    /**
     * Build state changed host notification for inventory host.
     *
     * @param dtoHost inventory host
     * @return inventory host notification
     */
    public DtoHostNotification buildDtoNotificationInventory(DtoHost dtoHost) {
        // build and return notification
        DtoHostNotification dtoHostNotification = new DtoHostNotification();
        dtoHostNotification.setHostState(dtoHost.getMonitorStatus());
        dtoHostNotification.setHostName(dtoHost.getHostName());
        dtoHostNotification.setNotificationType(GwosStatus.UP.status.equals(dtoHost.getMonitorStatus()) ? "RECOVERY" : "PROBLEM");
        dtoHostNotification.setHostOutput(dtoHost.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME));
        dtoHostNotification.setCheckDateTime(formatGwosDate(dtoHost.getLastCheckTime()));
        dtoHostNotification.setHostGroupNames(concatenateHostHostGroups(dtoHost));
        dtoHostNotification.setHostAddress(dtoHost.getDeviceIdentification());
        dtoHostNotification.setNotificationComment("Cloudhub monitor host notification");
        return dtoHostNotification;
    }

    /**
     * Build state changed service notification for inventory service.
     * Uses inventory host to complete service notification.
     *
     * @param dtoService inventory service
     * @return inventory service notification
     */
    public DtoServiceNotification buildDtoNotificationInventory(DtoService dtoService) {
        return buildDtoNotificationInventory(dtoService, null);
    }

    /**
     * Build state changed service notification for inventory service
     * and host.
     *
     * @param dtoService inventory service
     * @param dtoHost inventory host
     * @return inventory service notification
     */
    public DtoServiceNotification buildDtoNotificationInventory(DtoService dtoService, DtoHost dtoHost) {
        // lookup service host if not specified
        if (dtoHost == null) {
            dtoHost = getHosts().get(dtoService.getHostName());
        }
        // build and return notification
        DtoServiceNotification dtoServiceNotification = new DtoServiceNotification();
        dtoServiceNotification.setServiceDescription(dtoService.getDescription());
        dtoServiceNotification.setHostName(dtoService.getHostName());
        dtoServiceNotification.setServiceState(dtoService.getMonitorStatus());
        dtoServiceNotification.setNotificationType(GwosServiceStatus.OK.status.equals(dtoService.getMonitorStatus()) ? "RECOVERY" : "PROBLEM");
        dtoServiceNotification.setServiceOutput(dtoService.getProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME));
        dtoServiceNotification.setCheckDateTime(formatGwosDate(dtoService.getLastCheckTime()));
        if (dtoHost != null) {
            dtoServiceNotification.setHostGroupNames(concatenateHostHostGroups(dtoHost));
            dtoServiceNotification.setHostAddress(dtoHost.getDeviceIdentification());
        }
        dtoServiceNotification.setNotificationComment("Cloudhub monitor service notification");
        return dtoServiceNotification;
    }

    /**
     * Build inventory hosts, services, events, notifications, and performance
     * data from event.
     *
     * @param monitorStatus event host monitor status
     * @param serviceMonitorStatus event services monitor status
     * @param stateChanged event forces state change events and notification
     * @param stateType status type
     * @param reportDate report time for event
     * @param nextCheckTime next scheduled check time
     * @param textMessage message/last plugin output
     * @param errorType notification error type
     * @param host event host name
     * @param eventHost send event for host flag
     * @param eventServices keys for services to which to send events
     * @param eventServicesPerformanceData service key/performance data array
     * @param commentId comment id or null
     * @param commentAuthor comment author or null
     * @param addComment add comment, remove comment, or null
     * @param downtime scheduled downtime level delta or null
     * @param acknowledged acknowledged setting or null
     * @return collection of inventory hosts, services, events, and performance data or null
     */
    public Collection<Object> buildEventInventory(String monitorStatus, String serviceMonitorStatus, boolean stateChanged,
                                                  String stateType, Date reportDate, Date nextCheckTime, String textMessage,
                                                  String errorType, String host, boolean eventHost, List<String> eventServices,
                                                  Map<String,String[]> eventServicesPerformanceData, Long commentId,
                                                  String commentAuthor, Boolean addComment, Integer downtime,
                                                  Boolean acknowledged) {
        // build inventory
        List<Object> dtoEventInventory = new ArrayList<Object>();

        // check downtime, monitor status, and state changed
        boolean anyStateChanged = stateChanged;
        boolean hostStateChanged = stateChanged;
        String lastMonitorStatus = null;
        String updateMonitorStatus = monitorStatus;
        DtoHost dtoHost = hosts.get(host);
        if (dtoHost != null) {
            if (eventHost) {
                if (downtime != null) {
                    // set host downtime
                    int hostDowntime = Math.max(dtoHost.getPropertyInteger(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME) + downtime, 0);
                    dtoHost.getProperties().put(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME, Integer.toString(hostDowntime));
                    if (!dtoEventInventory.contains(dtoHost)) {
                        dtoEventInventory.add(dtoHost);
                    }
                }
                if (commentId != null) {
                    // add/remove host comment
                    String hostComments = editCommentsProperty(dtoHost.getProperty(COMMENTS_PROPERTY_NAME), commentId,
                            reportDate, commentAuthor, textMessage, addComment);
                    dtoHost.getProperties().put(COMMENTS_PROPERTY_NAME, hostComments);
                    if (!dtoEventInventory.contains(dtoHost)) {
                        dtoEventInventory.add(dtoHost);
                    }
                }
                if (acknowledged != null) {
                    // set/clear host acknowledged
                    dtoHost.setAcknowledged(acknowledged);
                    dtoHost.putProperty(ACKNOWLEDGED_PROPERTY_NAME, acknowledged);
                    if (!dtoEventInventory.contains(dtoHost)) {
                        dtoEventInventory.add(dtoHost);
                    }
                }
                if (monitorStatus != null) {
                    // host status change
                    lastMonitorStatus = dtoHost.getMonitorStatus();
                    // map host monitor status if in downtime, (downtime available only
                    // in host inventory updated during periodic synchronize)
                    if (updateMonitorStatus.equals(GwosStatus.UNSCHEDULED_DOWN.status) &&
                            (dtoHost.getPropertyInteger(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME) > 0)) {
                        updateMonitorStatus = GwosStatus.SCHEDULED_DOWN.status;
                    }
                    // force synthetic state change if monitor status changed
                    if (!hostStateChanged && !updateMonitorStatus.equals(dtoHost.getMonitorStatus())) {
                        hostStateChanged = true;
                        anyStateChanged = true;
                    }
                }
            }
        }
        Map<String,Boolean> servicesStateChanged = new HashMap<String,Boolean>();
        Map<String,String> servicesLastMonitorStatus = new HashMap<String,String>();
        Map<String,String> updateServiceMonitorStatus = new HashMap<String,String>();
        for (String service : eventServices) {
            servicesStateChanged.put(service, stateChanged);
            updateServiceMonitorStatus.put(service, serviceMonitorStatus);
            DtoService dtoService = services.get(service);
            if (dtoService != null) {
                if (downtime != null) {
                    // set service downtime
                    int serviceDowntime = Math.max(dtoService.getPropertyInteger(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME)+downtime, 0);
                    dtoService.getProperties().put(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME, Integer.toString(serviceDowntime));
                    if (!dtoEventInventory.contains(dtoService)) {
                        dtoEventInventory.add(dtoService);
                    }
                }
                if (commentId != null) {
                    // add/remove service comment
                    String serviceComments = editCommentsProperty(dtoService.getProperty(COMMENTS_PROPERTY_NAME),
                            commentId, reportDate, commentAuthor, textMessage, addComment);
                    dtoService.getProperties().put(COMMENTS_PROPERTY_NAME, serviceComments);
                    if (!dtoEventInventory.contains(dtoService)) {
                        dtoEventInventory.add(dtoService);
                    }
                }
                if (acknowledged != null) {
                    // set/clear host acknowledged
                    dtoService.putProperty(PROBLEM_ACKNOWLEDGED_PROPERTY_NAME, acknowledged);
                    if (!dtoEventInventory.contains(dtoService)) {
                        dtoEventInventory.add(dtoService);
                    }
                }
                if (serviceMonitorStatus != null) {
                    // service status change
                    servicesLastMonitorStatus.put(service, dtoService.getMonitorStatus());
                    // map service monitor status if in downtime, (downtime available only
                    // in service inventory updated during periodic synchronize)
                    if (updateServiceMonitorStatus.get(service).equals(GwosServiceStatus.UNSCHEDULED_CRITICAL.status) &&
                            (dtoService.getPropertyInteger(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME) > 0)) {
                        updateServiceMonitorStatus.put(service, GwosServiceStatus.SCHEDULED_CRITICAL.status);
                    }
                    // force synthetic state change if monitor status changed
                    if (!servicesStateChanged.get(service) &&
                            !updateServiceMonitorStatus.get(service).equals(dtoService.getMonitorStatus())) {
                        servicesStateChanged.put(service, true);
                        anyStateChanged = true;
                    }
                }
            }
        }

        // return updated inventory hosts and/or services for check events,
        // (check events return a monitor status)
        if ((monitorStatus != null) && (serviceMonitorStatus != null)) {
            if (eventHost && (dtoHost != null)) {
                // update host
                dtoHost.setMonitorStatus(updateMonitorStatus);
                dtoHost.setStateType(stateType);
                dtoHost.setLastCheckTime(reportDate);
                dtoHost.setNextCheckTime(nextCheckTime);
                if (hostStateChanged) {
                    dtoHost.setLastStateChange(reportDate);
                    dtoHost.putProperty(LAST_STATE_CHANGE_PROPERTY_NAME, reportDate);
                }
                dtoHost.setLastPlugInOutput(textMessage);
                if (dtoHost.getLastPlugInOutput() != null) {
                    dtoHost.putProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME, dtoHost.getLastPlugInOutput());
                }
                if (!dtoEventInventory.contains(dtoHost)) {
                    dtoEventInventory.add(dtoHost);
                }
            }
            for (String service : eventServices) {
                DtoService dtoService = services.get(service);
                if (dtoService != null) {
                    // update service
                    dtoService.setMonitorStatus(updateServiceMonitorStatus.get(service));
                    dtoService.setStateType(stateType);
                    if ("HARD".equals(stateType)) {
                        dtoService.setLastHardState(updateServiceMonitorStatus.get(service));
                    }
                    dtoService.setLastCheckTime(reportDate);
                    dtoService.setNextCheckTime(nextCheckTime);
                    if (servicesStateChanged.get(service)) {
                        dtoService.setLastStateChange(reportDate);
                    }
                    dtoService.setLastPlugInOutput(textMessage);
                    if (dtoService.getLastPlugInOutput() != null) {
                        dtoService.putProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME, dtoService.getLastPlugInOutput());
                    }
                    String [] performanceData = eventServicesPerformanceData.get(dtoService.getDescription());
                    if ((performanceData != null) && (performanceData.length > 1)) {
                        dtoService.putProperty(PERFORMANCE_DATA_PROPERTY_NAME, performanceData[1]);
                    }
                    if (!dtoEventInventory.contains(dtoService)) {
                        dtoEventInventory.add(dtoService);
                    }
                }
            }
        }

        // return events for inventory hosts and/or services state change,
        // notification events, or non check events, (notification events
        // return an error type and check events return a monitor status)
        boolean notificationEvent = (errorType != null);
        boolean noncheckEvent = ((downtime != null) ||
                ((monitorStatus == null) && (serviceMonitorStatus == null) && (commentId == null) && (acknowledged == null)));
        if (anyStateChanged || notificationEvent || noncheckEvent) {
            if (dtoHost != null) {
                // return host event and notification
                if (eventHost) {
                    if (hostStateChanged || notificationEvent || noncheckEvent) {
                        DtoEvent dtoEvent = buildDtoEventInventory(dtoHost);
                        dtoEvent.setReportDate(reportDate);
                        dtoEvent.setLastInsertDate(reportDate);
                        dtoEvent.setStateChanged(hostStateChanged);
                        dtoEvent.setErrorType(errorType);
                        dtoEvent.setTextMessage(textMessage);
                        dtoEventInventory.add(dtoEvent);
                    }
                    if (hostStateChanged && (monitorStatus != null) && (lastMonitorStatus != null)) {
                        // notify status changed only if not transitioning from PENDING to UP
                        boolean pendingToUp = (lastMonitorStatus.equals(GwosStatus.PENDING.status) &&
                                dtoHost.getMonitorStatus().equals(GwosStatus.UP.status));
                        if (!pendingToUp) {
                            dtoEventInventory.add(buildDtoNotificationInventory(dtoHost));
                        }
                    }
                }
                // return service events and notifications
                for (String service : eventServices) {
                    // lookup service
                    DtoService dtoService = services.get(service);
                    if (dtoService != null) {
                        Boolean serviceStateChanged = servicesStateChanged.get(service);
                        serviceStateChanged = ((serviceStateChanged != null) ? serviceStateChanged : false);
                        String serviceLastMonitorStatus = servicesLastMonitorStatus.get(service);
                        if (serviceStateChanged || notificationEvent || noncheckEvent) {
                            DtoEvent dtoEvent = buildDtoEventInventory(dtoService, dtoHost);
                            dtoEvent.setReportDate(reportDate);
                            dtoEvent.setLastInsertDate(reportDate);
                            dtoEvent.setStateChanged(serviceStateChanged);
                            dtoEvent.setErrorType(errorType);
                            dtoEvent.setTextMessage(textMessage);
                            dtoEventInventory.add(dtoEvent);
                        }
                        if (serviceStateChanged && (serviceMonitorStatus != null) && (serviceLastMonitorStatus != null)) {
                            // notify status changed only if not transitioning from PENDING to OK; when
                            // connector hosts are unsuspended an inventory sync is performed - testing for
                            // unsuspended UNKNOWN to OK transitions is not done for status change events
                            boolean pendingToOk = (serviceLastMonitorStatus.equals(GwosServiceStatus.PENDING.status) &&
                                    dtoService.getMonitorStatus().equals(GwosServiceStatus.OK.status));
                            if (!pendingToOk) {
                                dtoEventInventory.add(buildDtoNotificationInventory(dtoService, dtoHost));
                            }
                        }
                    }
                }
            }
        }

        // return performance data for inventory services
        if (dtoHost != null) {
            for (Map.Entry<String,String[]> performanceDataEntry : eventServicesPerformanceData.entrySet()) {
                String performanceService = performanceDataEntry.getKey();
                if (services.containsKey(performanceService)) {
                    String[] performanceData = performanceDataEntry.getValue();
                    DtoPerfData dtoPerfData = new DtoPerfData();
                    dtoPerfData.setAppType(appType);
                    dtoPerfData.setServerName(host);
                    dtoPerfData.setServiceName(performanceService.substring(performanceService.indexOf('!')+1));
                    dtoPerfData.setServerTime(reportDate.getTime());
                    dtoPerfData.setLabel(performanceData[0]);
                    dtoPerfData.setValue(performanceData[1]);
                    if (performanceData.length > 2) {
                        dtoPerfData.setWarning(performanceData[2]);
                    }
                    if (performanceData.length > 3) {
                        dtoPerfData.setCritical(performanceData[3]);
                    }
                    dtoEventInventory.add(dtoPerfData);
                }
            }
        }
        return dtoEventInventory;
    }

    /**
     * Edit host/service comments property. Note that the comment separator prefixes
     * every comment, even the first comment in the comments property.
     *
     * @param comments comments property or null
     * @param id comment id to add or remove
     * @param date comment date to add or null
     * @param author comment author to add or null
     * @param text comment text to add or null
     * @param add add or remove comment
     * @return edited comments property
     */
    protected static String editCommentsProperty(String comments, long id, Date date, String author, String text, boolean add) {
        comments = ((comments != null) ? comments : "");
        int [] commentIndex = new int[1];
        int [] nextCommentIndex = new int[1];
        boolean matchedCommentId = indexOfComment(comments, id, commentIndex, nextCommentIndex);
        if (add && !matchedCommentId) {
            // insert new comment into comments
            String comment = COMMENTS_SEPARATOR +
                    id + COMMENTS_FIELD_SEPARATOR +
                    ((date != null) ? formatGwosDate(date) : "") + COMMENTS_FIELD_SEPARATOR +
                    ((author != null) ? author : "") + COMMENTS_FIELD_SEPARATOR +
                    "'" + ((text != null) ? text : "") + "'";
            if (commentIndex[0] != -1) {
                comments = comments.substring(0, commentIndex[0]) + comment + comments.substring(commentIndex[0]);
            } else {
                comments += comment;
            }
        } else if (!add && matchedCommentId) {
            // remove existing comment from comments
            if (nextCommentIndex[0] != -1) {
                comments = comments.substring(0, commentIndex[0]) + comments.substring(nextCommentIndex[0]);
            } else {
                comments = comments.substring(0, commentIndex[0]);
            }
        }
        return comments;
    }

    /**
     * Return index of comment in comments property value assuming the
     * comments are in ascending id order. If id is found, return true and
     * index/next index of matching comment. Otherwise, return false and
     * the index/next index of comment to insert before.
     *
     * @param comments comments property
     * @param id comment id to index
     * @param index returned comment index
     * @param nextIndex returned next comment index
     * @return comment id matched
     */
    private static boolean indexOfComment(String comments, long id, int [] index, int [] nextIndex) {
        index[0] = comments.indexOf(COMMENTS_SEPARATOR);
        nextIndex[0] = -1;
        while (index[0] != -1) {
            int commentIdIndex = index[0]+COMMENTS_SEPARATOR.length();
            int endCommentIdIndex = comments.indexOf(COMMENTS_FIELD_SEPARATOR, commentIdIndex);
            nextIndex[0] = comments.indexOf(COMMENTS_SEPARATOR, endCommentIdIndex);
            long commentId = Long.parseLong(comments.substring(commentIdIndex, endCommentIdIndex));
            if (commentId == id) {
                return true;
            } else if (commentId > id) {
                return false;
            }
            index[0] = nextIndex[0];
        }
        return false;
    }

    /**
     * Format date for GWOS API use.
     *
     * @param date date to format or null
     * @return GWOS formatted date or null
     */
    protected static String formatGwosDate(Date date) {
        if (date == null) {
            return null;
        }
        synchronized (GWOS_DATE_FORMAT) {
            return GWOS_DATE_FORMAT.format(date);
        }
    }

    /**
     * Concatentate inventory host host group names into a comma separated
     * string list..
     *
     * @param dtoHost inventory host
     * @return host group list string
     */
    protected static String concatenateHostHostGroups(DtoHost dtoHost) {
        if ((dtoHost.getHostGroups() == null) || dtoHost.getHostGroups().isEmpty()) {
            return null;
        }
        StringBuilder notificationHostGroupsBuilder = new StringBuilder();
        for (DtoHostGroup dtoHostGroup : dtoHost.getHostGroups()) {
            if (notificationHostGroupsBuilder.length() > 0) {
                notificationHostGroupsBuilder.append(',');
            }
            notificationHostGroupsBuilder.append(dtoHostGroup.getName());
        }
        return notificationHostGroupsBuilder.toString();
    }

    /**
     * Make simple copy of host.
     *
     * @param dtoHost host to copy
     * @return simple host copy
     */
    protected static DtoHost copyDtoHost(DtoHost dtoHost) {
        DtoHost copyDtoHost = new DtoHost();
        copyDtoHost.setHostName(dtoHost.getHostName());
        copyDtoHost.setAppType(dtoHost.getAppType());
        copyDtoHost.setAgentId(dtoHost.getAgentId());
        return copyDtoHost;
    }

    /**
     * Make simple copy of host group.
     *
     * @param dtoHostGroup host group to copy
     * @return simple host group copy
     */
    protected static DtoHostGroup copyDtoHostGroup(DtoHostGroup dtoHostGroup) {
        DtoHostGroup copyDtoHostGroup = new DtoHostGroup();
        copyDtoHostGroup.setName(dtoHostGroup.getName());
        copyDtoHostGroup.setAppType(dtoHostGroup.getAppType());
        copyDtoHostGroup.setAgentId(dtoHostGroup.getAgentId());
        return copyDtoHostGroup;
    }

    /**
     * Make simple copy of service.
     *
     * @param dtoService service to copy
     * @return simple service copy
     */
    protected static DtoService copyDtoService(DtoService dtoService) {
        DtoService copyDtoService = new DtoService();
        copyDtoService.setHostName(dtoService.getHostName());
        copyDtoService.setDescription(dtoService.getDescription());
        copyDtoService.setAppType(dtoService.getAppType());
        copyDtoService.setAgentId(dtoService.getAgentId());
        return copyDtoService;
    }

    /**
     * Convert host to service monitor status for synthetic services.
     *
     * @param hostMonitorStatus host monitor status
     * @return service monitor status
     */
    protected static String hostToServiceMonitorStatus(String hostMonitorStatus) {
        if (hostMonitorStatus == null) {
            return null;
        }
        if (hostMonitorStatus.equals(GwosStatus.UP.status)) {
            return GwosServiceStatus.OK.status;
        }
        if (hostMonitorStatus.equals(GwosStatus.SCHEDULED_DOWN.status)) {
            return GwosServiceStatus.SCHEDULED_CRITICAL.status;
        }
        if (hostMonitorStatus.equals(GwosStatus.UNSCHEDULED_DOWN.status)) {
            return GwosServiceStatus.UNSCHEDULED_CRITICAL.status;
        }
        if (hostMonitorStatus.equals(GwosStatus.WARNING.status)) {
            return GwosServiceStatus.WARNING.status;
        }
        if (hostMonitorStatus.equals(GwosStatus.PENDING.status)) {
            return GwosServiceStatus.PENDING.status;
        }
        return GwosServiceStatus.UNKNOWN.status;
    }

    public String getMonitorServer() {
        return monitorServer;
    }

    public String getAppType() {
        return appType;
    }

    public String getAgentId() {
        return agentId;
    }

    public Map<String,DtoHost> getHosts() {
        return hosts;
    }

    public Map<String,DtoHostGroup> getHostGroups() {
        return hostGroups;
    }

    public Map<String,DtoService> getServices() {
        return services;
    }

    public Map<String,DtoServiceGroup> getServiceGroups() {
        return serviceGroups;
    }
}
