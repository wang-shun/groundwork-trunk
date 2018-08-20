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

package org.groundwork.cloudhub.connectors.icinga2;

import org.codehaus.jackson.JsonNode;
import org.groundwork.cloudhub.connectors.MonitorConnector;
import org.groundwork.cloudhub.gwos.GwosServiceStatus;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.MonitorInventory;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Icinga2MonitorInventoryBrowser
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2MonitorInventory extends MonitorInventory {

    private Map<String,Set<String>> syntheticServiceMappings = new ConcurrentHashMap<String,Set<String>>();

    /**
     * Construct Icinga2 monitor inventory.
     *
     * @param monitorServer connection monitor server
     * @param appType info application type
     * @param agentId info agent id
     */
    public Icinga2MonitorInventory(String monitorServer, String appType, String agentId) {
        super(monitorServer, appType, agentId);
    }

    /**
     * Add Icinga2 JSON host to inventory. Hosts must be added first
     * to inventory so that host groups and services can be linked to
     * the hosts. Services are added for host performance data.
     *
     * @param jsonHost Icinga2 JSON host
     * @param hostValidator host name validator
     */
    public void addHost(JsonNode jsonHost, MonitorConnector.ValidateHost hostValidator) {
        // validate JSON host
        if ((jsonHost == null) || !jsonHost.has("attrs")) {
            throw new IllegalArgumentException("Invalid Icinga2 host JSON: "+jsonHost);
        }
        JsonNode jsonAttrs = jsonHost.get("attrs");
        if (!jsonAttrs.isObject()) {
            throw new IllegalArgumentException("Invalid Icinga2 host JSON: "+jsonHost);
        }
        String hostName = attrsStringMember(jsonAttrs, "name");
        if (hostName == null) {
            throw new IllegalArgumentException("Invalid Icinga2 host JSON: "+jsonHost);
        }
        if ((hostValidator != null) && !hostValidator.validateHost(hostName)) {
            return; // filter invalid host from inventory
        }

        // add host to inventory: force downtime, acknowledged, and comments to
        // be set since synchronizing setting from Icinga2 host
        DtoHost dtoHost = new DtoHost();
        dtoHost.setHostName(hostName);
        dtoHost.setMonitorServer(monitorServer);
        dtoHost.setAppType(appType);
        dtoHost.setAgentId(agentId);
        dtoHost.setDescription(attrsStringMember(jsonAttrs, "display_name"));
        String device = attrsStringMember(jsonAttrs, "address");
        if (device == null) {
            device = attrsStringMember(jsonAttrs, "address6");
        }
        dtoHost.setDeviceIdentification(device);
        dtoHost.setDeviceDisplayName(hostName);
        Boolean downtime = attrsBooleanMember(jsonAttrs, "last_in_downtime");
        dtoHost.putProperty(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME, (((downtime != null) && downtime) ? "1" : "0"));
        Boolean reachable = attrsBooleanMember(jsonAttrs, "last_reachable");
        Integer state = attrsIntegerMember(jsonAttrs, "state");
        dtoHost.setMonitorStatus(hostMonitorStatus(state, reachable, downtime));
        dtoHost.setLastStateChange(attrsTimestampMember(jsonAttrs, "last_state_change"));
        if (dtoHost.getLastStateChange() != null) {
            dtoHost.putProperty(LAST_STATE_CHANGE_PROPERTY_NAME, dtoHost.getLastStateChange());
        }
        Integer stateType = attrsIntegerMember(jsonAttrs, "last_state_type");
        if (stateType != null) {
            switch (stateType) {
                case 0: dtoHost.setStateType("SOFT"); break;
                case 1: dtoHost.setStateType("HARD"); break;
            }
        }
        Integer acknowledged = attrsIntegerMember(jsonAttrs, "acknowledgement");
        if (acknowledged != null) {
            switch (acknowledged) {
                case 0: dtoHost.setAcknowledged(false); break;
                case 1:
                case 2: dtoHost.setAcknowledged(true); break;
            }
            dtoHost.putProperty(ACKNOWLEDGED_PROPERTY_NAME, dtoHost.isAcknowledged());
        } else {
            dtoHost.putProperty(ACKNOWLEDGED_PROPERTY_NAME, "");
        }
        if (jsonAttrs.has("last_check_result")) {
            JsonNode jsonCheckResult = jsonAttrs.get("last_check_result");
            if (jsonCheckResult.isObject()) {
                dtoHost.setLastCheckTime(attrsTimestampMember(jsonCheckResult, "execution_start"));
                dtoHost.setNextCheckTime(attrsTimestampMember(jsonCheckResult, "schedule_start"));
                Boolean active = attrsBooleanMember(jsonCheckResult, "active");
                if ((active != null) && active) {
                    dtoHost.setCheckType("ACTIVE");
                } else {
                    dtoHost.setCheckType("PASSIVE");
                }
                dtoHost.setLastPlugInOutput(attrsStringMember(jsonCheckResult, "output"));
                if (dtoHost.getLastPlugInOutput() != null) {
                    dtoHost.putProperty(LAST_PLUGIN_OUTPUT_PROPERTY_NAME, dtoHost.getLastPlugInOutput());
                }
            }
        }
        dtoHost.putProperty(COMMENTS_PROPERTY_NAME, "");
        hosts.put(dtoHost.getHostName(), dtoHost);

        // add synthetic services to host and inventory for host performance data
        Integer lastHardState = attrsIntegerMember(jsonAttrs, "last_hard_state");
        String hostLastHardState = hostMonitorStatus(lastHardState, null, null);
        List<String> hostPerformanceDataLabels = attrsPerformanceDataLabels(jsonAttrs, "last_check_result");
        if (!hostPerformanceDataLabels.isEmpty()) {
            Set<String> hostSyntheticServices = new HashSet<String>();
            for (String hostPerformanceDataLabel : hostPerformanceDataLabels) {
                String serviceInventoryKey = addService(hostName, hostPerformanceDataLabel,
                        dtoHost.getProperty(SCHEDULED_DOWNTIME_DEPTH_PROPERTY_NAME),
                        hostToServiceMonitorStatus(dtoHost.getMonitorStatus()), dtoHost.getLastStateChange(),
                        dtoHost.getLastCheckTime(), dtoHost.getNextCheckTime(), dtoHost.getStateType(),
                        hostToServiceMonitorStatus(hostLastHardState), dtoHost.getCheckType(),
                        dtoHost.getLastPlugInOutput(), dtoHost.getProperty(COMMENTS_PROPERTY_NAME),
                        dtoHost.getProperty(ACKNOWLEDGED_PROPERTY_NAME), dtoHost);
                hostSyntheticServices.add(serviceInventoryKey);
            }
            syntheticServiceMappings.put(hostName, hostSyntheticServices);
        }
    }

    /**
     * Add Icinga2 JSON host group to inventory. Assumes all hosts have
     * been previously loaded into inventory.
     *
     * @param jsonHostGroup Icinga2 JSON host group
     */
    public void addHostGroup(JsonNode jsonHostGroup) {
        // validate JSON host group
        if ((jsonHostGroup == null) || !jsonHostGroup.has("attrs") || !jsonHostGroup.has("meta")) {
            throw new IllegalArgumentException("Invalid Icinga2 hostgroup JSON: "+jsonHostGroup);
        }
        JsonNode jsonAttrs = jsonHostGroup.get("attrs");
        JsonNode jsonMeta = jsonHostGroup.get("meta");
        if (!jsonAttrs.isObject() || !jsonMeta.isObject()) {
            throw new IllegalArgumentException("Invalid Icinga2 hostgroup JSON: "+jsonHostGroup);
        }
        String name = attrsStringMember(jsonAttrs, "name");
        if (name == null) {
            throw new IllegalArgumentException("Invalid Icinga2 hostgroup JSON: "+jsonHostGroup);
        }

        // add host group to inventory
        DtoHostGroup dtoHostGroup = new DtoHostGroup();
        dtoHostGroup.setName(name);
        dtoHostGroup.setAppType(appType);
        dtoHostGroup.setAgentId(agentId);
        dtoHostGroup.setDescription(attrsStringMember(jsonAttrs, "display_name"));
        if (jsonMeta.has("used_by")) {
            JsonNode jsonUsedBy = jsonMeta.get("used_by");
            if (jsonUsedBy.isArray() && (jsonUsedBy.size() > 0)) {
                for (Iterator<JsonNode> elementIter = jsonUsedBy.getElements(); elementIter.hasNext();) {
                    JsonNode jsonUsedByElement = elementIter.next();
                    String elementName = attrsStringMember(jsonUsedByElement, "name");
                    String elementType = attrsStringMember(jsonUsedByElement, "type");
                    if ((elementName != null) && "Host".equals(elementType)) {
                        // add host to host group, (invalid host may have been filtered from inventory)
                        DtoHost dtoHost = hosts.get(elementName);
                        if (dtoHost != null) {
                            // add simple host group to host
                            dtoHostGroup.addHost(copyDtoHost(dtoHost));
                            // add simple host group to host
                            dtoHost.addHostGroup(copyDtoHostGroup(dtoHostGroup));
                        }
                    }
                }
            }
        }
        hostGroups.put(dtoHostGroup.getName(), dtoHostGroup);
    }

    /**
     * Add Icinga2 JSON service to inventory. Assumes all hosts have
     * been previously loaded into inventory.
     *
     * @param jsonService Icinga2 JSON service
     */
    public void addService(JsonNode jsonService) {
        // validate JSON service
        if ((jsonService == null) || !jsonService.has("attrs")) {
            throw new IllegalArgumentException("Invalid Icinga2 service JSON: "+jsonService);
        }
        JsonNode jsonAttrs = jsonService.get("attrs");
        if (!jsonAttrs.isObject()) {
            throw new IllegalArgumentException("Invalid Icinga2 service JSON: "+jsonService);
        }
        String hostName = attrsStringMember(jsonAttrs, "host_name");
        String serviceDescription = attrsStringMember(jsonAttrs, "name");
        if ((hostName == null) || (serviceDescription == null)) {
            throw new IllegalArgumentException("Invalid Icinga2 service JSON: "+jsonService);
        }
        DtoHost dtoHost = hosts.get(hostName);
        if (dtoHost == null) {
            return; // invalid host may have been filtered from inventory
        }

        // add service to inventory: force downtime, acknowledged, and comments
        // to be set since synchronizing settings from Icinga2 host
        Boolean downtime = attrsBooleanMember(jsonAttrs, "last_in_downtime");
        String serviceScheduledDowntimeDepth = (((downtime != null) && downtime) ? "1" : "0");
        Boolean reachable = attrsBooleanMember(jsonAttrs, "last_reachable");
        Integer state = attrsIntegerMember(jsonAttrs, "state");
        String serviceMonitorStatus = serviceMonitorStatus(state, reachable, downtime);
        Date serviceLastStateChange = attrsTimestampMember(jsonAttrs, "last_state_change");
        String serviceStateType = null;
        Integer stateType = attrsIntegerMember(jsonAttrs, "last_state_type");
        if (stateType != null) {
            switch (stateType) {
                case 0: serviceStateType = "SOFT"; break;
                case 1: serviceStateType = "HARD"; break;
            }
        }
        Integer hardState = attrsIntegerMember(jsonAttrs, "last_hard_state");
        String serviceLastHardState = ((hardState != null) ? serviceMonitorStatus(hardState, null, null) : null);
        String serviceAcknowledged = "";
        Integer acknowledged = attrsIntegerMember(jsonAttrs, "acknowledgement");
        if (acknowledged != null) {
            switch (acknowledged) {
                case 0: serviceAcknowledged = "false"; break;
                case 1:
                case 2: serviceAcknowledged = "true"; break;
            }
        }
        Date serviceLastCheckTime = null;
        Date serviceNextCheckTime = null;
        String serviceCheckType = null;
        String serviceLastPlugInOutput = null;
        if (jsonAttrs.has("last_check_result")) {
            JsonNode jsonCheckResult = jsonAttrs.get("last_check_result");
            if (jsonCheckResult.isObject()) {
                Boolean active = attrsBooleanMember(jsonCheckResult, "active");
                if ((active != null) && active) {
                    serviceCheckType = "ACTIVE";
                } else {
                    serviceCheckType = "PASSIVE";
                }
                serviceLastCheckTime = attrsTimestampMember(jsonCheckResult, "execution_start");
                serviceNextCheckTime = attrsTimestampMember(jsonCheckResult, "schedule_start");
                serviceLastPlugInOutput = attrsStringMember(jsonCheckResult, "output");
            }
        }
        String serviceComments = "";
        List<String> servicePerformanceDataLabels = attrsPerformanceDataLabels(jsonAttrs, "last_check_result");
        if (servicePerformanceDataLabels.size() > 1) {
            // add synthetic services to inventory for service performance data
            Set<String> serviceSyntheticServices = new HashSet<String>();
            for (String servicePerformanceDataLabel : servicePerformanceDataLabels) {
                String serviceInventoryKey = addService(hostName, serviceDescription + "!" + servicePerformanceDataLabel,
                        serviceScheduledDowntimeDepth, serviceMonitorStatus, serviceLastStateChange, serviceLastCheckTime,
                        serviceNextCheckTime, serviceStateType, serviceLastHardState, serviceCheckType,
                        serviceLastPlugInOutput, serviceComments, serviceAcknowledged, dtoHost);
                serviceSyntheticServices.add(serviceInventoryKey);
            }
            syntheticServiceMappings.put(hostName + "!" + serviceDescription, serviceSyntheticServices);
        } else {
            // add service to inventory
            addService(hostName, serviceDescription, serviceScheduledDowntimeDepth, serviceMonitorStatus,
                    serviceLastStateChange, serviceLastCheckTime, serviceNextCheckTime, serviceStateType,
                    serviceLastHardState, serviceCheckType, serviceLastPlugInOutput, serviceComments, serviceAcknowledged,
                    dtoHost);
        }
    }

    /**
     * Add Icinga2 JSON service group to inventory. Assumes all
     * services have been previously added to inventory.
     *
     * @param jsonServiceGroup Icinga2 JSON service group
     */
    public void addServiceGroup(JsonNode jsonServiceGroup) {
        // validate JSON service group
        if ((jsonServiceGroup == null) || !jsonServiceGroup.has("attrs") || !jsonServiceGroup.has("meta")) {
            throw new IllegalArgumentException("Invalid Icinga2 servicegroup JSON: "+jsonServiceGroup);
        }
        JsonNode jsonAttrs = jsonServiceGroup.get("attrs");
        JsonNode jsonMeta = jsonServiceGroup.get("meta");
        if (!jsonAttrs.isObject() || !jsonMeta.isObject()) {
            throw new IllegalArgumentException("Invalid Icinga2 servicegroup JSON: "+jsonServiceGroup);
        }
        String name = attrsStringMember(jsonAttrs, "name");
        if (name == null) {
            throw new IllegalArgumentException("Invalid Icinga2 servicegroup JSON: "+jsonServiceGroup);
        }

        // add service group to inventory
        DtoServiceGroup dtoServiceGroup = new DtoServiceGroup();
        dtoServiceGroup.setName(name);
        dtoServiceGroup.setAppType(appType);
        dtoServiceGroup.setAgentId(agentId);
        dtoServiceGroup.setDescription(attrsStringMember(jsonAttrs, "display_name"));
        if (jsonMeta.has("used_by")) {
            JsonNode jsonUsedBy = jsonMeta.get("used_by");
            if (jsonUsedBy.isArray() && (jsonUsedBy.size() > 0)) {
                for (Iterator<JsonNode> elementIter = jsonUsedBy.getElements(); elementIter.hasNext();) {
                    JsonNode jsonUsedByElement = elementIter.next();
                    String elementName = attrsStringMember(jsonUsedByElement, "name");
                    String elementType = attrsStringMember(jsonUsedByElement, "type");
                    if ((elementName != null) && "Service".equals(elementType)) {
                        // add services to service group, (invalid service hosts may have been filtered from inventory)
                        Set<String> syntheticServices = syntheticServiceMappings.get(elementName);
                        if (syntheticServices != null) {
                            for (String syntheticService : syntheticServices) {
                                DtoService dtoService = services.get(syntheticService);
                                if (dtoService != null) {
                                    // add simple service to service group
                                    dtoServiceGroup.addService(copyDtoService(dtoService));
                                }
                            }
                        } else {
                            DtoService dtoService = services.get(elementName);
                            if (dtoService != null) {
                                // add simple service to service group
                                dtoServiceGroup.addService(copyDtoService(dtoService));
                            }
                        }
                    }
                }
            }
        }
        serviceGroups.put(dtoServiceGroup.getName(), dtoServiceGroup);
    }

    /**
     * Add Icinga2 JSON comment to inventory. Assumes all hosts and
     * services have been previously added to inventory.
     *
     * @param jsonComment Icinga2 JSON comment
     */
    public void addComment(JsonNode jsonComment) {
        // validate JSON comment
        if ((jsonComment == null) || !jsonComment.has("attrs")) {
            throw new IllegalArgumentException("Invalid Icinga2 comment JSON: "+jsonComment);
        }
        JsonNode jsonAttrs = jsonComment.get("attrs");
        if (!jsonAttrs.isObject()) {
            throw new IllegalArgumentException("Invalid Icinga2 comment JSON: "+jsonComment);
        }
        String host = attrsStringMember(jsonAttrs, "host_name");
        String service = attrsStringMember(jsonAttrs, "service_name");
        Date reportDate = attrsTimestampMember(jsonAttrs, "entry_time");
        String textMessage = attrsStringMember(jsonAttrs, "text");
        Long commentId = attrsLongTimestampMember(jsonAttrs, "entry_time");
        String commentAuthor = attrsStringMember(jsonAttrs, "author");
        if ((host == null) || (commentId == null)) {
            throw new IllegalArgumentException("Invalid Icinga2 comment JSON: "+jsonComment);
        }

        // add comment to host and service inventory
        String commentHost = ((service == null) ? host : null);
        List<String> commentServices = new ArrayList<String>();
        String syntheticServicesKey = ((service != null) ? host + "!" + service : host);
        Set<String> syntheticServices = syntheticServiceMappings.get(syntheticServicesKey);
        if (syntheticServices != null) {
            commentServices.addAll(syntheticServices);
        } else if (service != null) {
            commentServices.add(syntheticServicesKey);
        }
        if (commentHost != null) {
            DtoHost dtoHost = hosts.get(commentHost);
            if (dtoHost != null) {
                String hostComments = editCommentsProperty(dtoHost.getProperty(COMMENTS_PROPERTY_NAME), commentId,
                        reportDate, commentAuthor, textMessage, true);
                dtoHost.getProperties().put(COMMENTS_PROPERTY_NAME, hostComments);
            }
        }
        for (String commentService : commentServices) {
            DtoService dtoService = services.get(commentService);
            if (dtoService != null) {
                String hostComments = editCommentsProperty(dtoService.getProperty(COMMENTS_PROPERTY_NAME), commentId,
                        reportDate, commentAuthor, textMessage, true);
                dtoService.getProperties().put(COMMENTS_PROPERTY_NAME, hostComments);
            }
        }
    }

    /**
     * Build inventory hosts, services, events, notifications, and performance
     * data from Icinga2 JSON event.
     *
     * @param jsonEvent Icinga2 JSON event
     * @param enablePerformanceData enable performance data
     * @return collection of inventory hosts, services, events, and performance data or null
     */
    public Collection<Object> buildEventInventory(JsonNode jsonEvent, boolean enablePerformanceData) {
        // validate JSON event
        if ((jsonEvent == null) || !jsonEvent.has("type")) {
            throw new IllegalArgumentException("Invalid Icinga2 event JSON");
        }
        String type = attrsStringMember(jsonEvent, "type");
        String host = attrsStringMember(jsonEvent, "host");
        String service = attrsStringMember(jsonEvent, "service");
        Date reportDate = attrsTimestampMember(jsonEvent, "timestamp");
        if (reportDate == null) {
            throw new IllegalArgumentException("Invalid Icinga2 event JSON");
        }

        // return inventory events and performance data
        String [] statusAndState = null;
        String monitorStatus = null;
        String serviceMonitorStatus = null;
        String stateType = null;
        boolean stateChanged = false;
        String textMessage = null;
        String errorType = null;
        Date nextCheckTime = null;
        Long commentId = null;
        String commentAuthor = null;
        Boolean addComment = null;
        Integer downtime = null;
        Boolean acknowledged = null;
        JsonNode jsonCheckResult = null;
        if (type.equals("CheckResult") && jsonEvent.has("check_result")) {
            jsonCheckResult = jsonEvent.get("check_result");
            if (jsonCheckResult.isObject()) {
                statusAndState = eventCheckResultMonitorStatus(jsonCheckResult, (service == null));
                textMessage = attrsStringMember(jsonCheckResult, "output");
                reportDate = attrsTimestampMember(jsonCheckResult, "execution_start");
                nextCheckTime = attrsTimestampMember(jsonCheckResult, "schedule_start");
            } else {
                jsonCheckResult = null;
            }
        } else if (type.equals("Notification") && jsonEvent.has("check_result")) {
            textMessage = attrsStringMember(jsonEvent, "text");
            errorType = attrsStringMember(jsonEvent, "notification_type");
            jsonCheckResult = jsonEvent.get("check_result");
            if (jsonCheckResult.isObject()) {
                statusAndState = eventCheckResultMonitorStatus(jsonCheckResult, (service == null));
                if (textMessage == null) {
                    textMessage = attrsStringMember(jsonCheckResult, "output");
                }
                reportDate = attrsTimestampMember(jsonCheckResult, "execution_start");
                nextCheckTime = attrsTimestampMember(jsonCheckResult, "schedule_start");
            } else {
                jsonCheckResult = null;
            }
        } else if (type.equals("StateChange") && jsonEvent.has("check_result")) {
            stateChanged = true;
            jsonCheckResult = jsonEvent.get("check_result");
            if (jsonCheckResult.isObject()) {
                statusAndState = eventCheckResultMonitorStatus(jsonCheckResult, (service == null));
                textMessage = attrsStringMember(jsonCheckResult, "output");
                reportDate = attrsTimestampMember(jsonCheckResult, "execution_start");
                nextCheckTime = attrsTimestampMember(jsonCheckResult, "schedule_start");
            } else {
                jsonCheckResult = null;
            }
        } else if (type.equals("CommentAdded") && jsonEvent.has("comment")) {
            JsonNode jsonComment = jsonEvent.get("comment");
            if (jsonComment.isObject()) {
                host = attrsStringMember(jsonComment, "host_name");
                service = attrsStringMember(jsonComment, "service_name");
                reportDate = attrsTimestampMember(jsonComment, "entry_time");
                textMessage = attrsStringMember(jsonComment, "text");
                commentId = attrsLongTimestampMember(jsonComment, "entry_time");
                commentAuthor = attrsStringMember(jsonComment, "author");
            }
            addComment = true;
        } else if (type.equals("CommentRemoved") && jsonEvent.has("comment")) {
            JsonNode jsonComment = jsonEvent.get("comment");
            if (jsonComment.isObject()) {
                host = attrsStringMember(jsonComment, "host_name");
                service = attrsStringMember(jsonComment, "service_name");
                textMessage = attrsStringMember(jsonComment, "text");
                commentId = attrsLongTimestampMember(jsonComment, "entry_time");
                commentAuthor = attrsStringMember(jsonComment, "author");
            }
            addComment = false;
        } else if (type.equals("DowntimeAdded") && jsonEvent.has("downtime")) {
            JsonNode jsonDowntime = jsonEvent.get("downtime");
            if (jsonDowntime.isObject()) {
                host = attrsStringMember(jsonDowntime, "host_name");
                service = attrsStringMember(jsonDowntime, "service_name");
                reportDate = attrsTimestampMember(jsonDowntime, "entry_time");
                textMessage = attrsStringMember(jsonDowntime, "comment");
            }
            downtime = 1;
        } else if (type.equals("DowntimeRemoved") && jsonEvent.has("downtime")) {
            JsonNode jsonDowntime = jsonEvent.get("downtime");
            if (jsonDowntime.isObject()) {
                host = attrsStringMember(jsonDowntime, "host_name");
                service = attrsStringMember(jsonDowntime, "service_name");
                textMessage = attrsStringMember(jsonDowntime, "comment");
            }
            downtime = -1;
        } else if (type.equals("AcknowledgementSet")) {
            textMessage = attrsStringMember(jsonEvent, "comment");
            acknowledged = true;
        } else if (type.equals("AcknowledgementCleared")) {
            acknowledged = false;
        } else {
            throw new IllegalArgumentException("Unsupported Icinga2 event type");
        }
        if (host == null) {
            throw new IllegalArgumentException("Invalid Icinga2 event JSON");
        }
        if (!hosts.containsKey(host)) {
            return null;
        }
        if (statusAndState != null) {
            monitorStatus = statusAndState[0];
            serviceMonitorStatus = ((service == null) ? hostToServiceMonitorStatus(monitorStatus) : monitorStatus);
            stateType = statusAndState[1];
        }

        // determine host inventory update/event
        boolean eventHost = (service == null);

        // determine service inventory update/event/performance data
        List<String> eventServices = new ArrayList<String>();
        String syntheticServicesKey = ((service != null) ? host + "!" + service : host);
        Set<String> syntheticServices = syntheticServiceMappings.get(syntheticServicesKey);
        Map<String,String[]> eventServicesPerformanceData = new HashMap<String,String[]>();
        if ((jsonCheckResult != null) && jsonCheckResult.has("performance_data")) {
            // service events and performance data
            JsonNode jsonPerformanceData = jsonCheckResult.get("performance_data");
            if (jsonPerformanceData.isArray()) {
                for (Iterator<JsonNode> elementIter = jsonPerformanceData.getElements(); elementIter.hasNext();) {
                    JsonNode jsonPerformanceDataElement = elementIter.next();
                    String [] performanceData;
                    if (jsonPerformanceDataElement.isTextual()) {
                        String performanceDataText = jsonPerformanceDataElement.getTextValue();
                        try {
                            performanceData = parsePerformanceData(performanceDataText, 4);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Cannot parse Icinga2 performance data: " + performanceDataText);
                        }
                    } else if (jsonPerformanceDataElement.isObject()) {
                        performanceData = new String[]{
                                attrsStringMember(jsonPerformanceDataElement, "label"),
                                attrsStringMember(jsonPerformanceDataElement, "value"),
                                attrsStringMember(jsonPerformanceDataElement, "warn"),
                                attrsStringMember(jsonPerformanceDataElement, "crit")};
                    } else {
                        throw new IllegalArgumentException("Unsupported Icinga2 performance data");
                    }
                    String performanceDataLabel = performanceData[0];
                    if (syntheticServices != null) {
                        String performanceDataSyntheticServiceKey = syntheticServicesKey + "!" + performanceDataLabel;
                        if (syntheticServices.contains(performanceDataSyntheticServiceKey)) {
                            // synthetic service update/event/performance data
                            eventServices.add(performanceDataSyntheticServiceKey);
                            if (enablePerformanceData) {
                                eventServicesPerformanceData.put(performanceDataSyntheticServiceKey, performanceData);
                            }
                        }
                    } else {
                        // service update/event/performance data
                        eventServices.add(syntheticServicesKey);
                        if (enablePerformanceData) {
                            eventServicesPerformanceData.put(syntheticServicesKey, performanceData);
                        }
                        break;
                    }
                }
            }
        } else if (syntheticServices != null) {
            // synthetic service updates/events
            for (String syntheticServiceKey : syntheticServices) {
                eventServices.add(syntheticServiceKey);
            }
        } else if (service != null) {
            // service update/event
            eventServices.add(syntheticServicesKey);
        }

        // return updated inventory hosts and/or services for check events and
        // downtime, (check events return a monitor status), return events for
        // inventory hosts and/or services state change, downtime, notification
        // events, or non check events, (notification events return an error type
        // and check events return a monitor status), and return performance data
        // for inventory services
        return buildEventInventory(monitorStatus, serviceMonitorStatus, stateChanged, stateType, reportDate,
                nextCheckTime, textMessage, errorType, host, eventHost, eventServices, eventServicesPerformanceData,
                commentId, commentAuthor, addComment, downtime, acknowledged);
    }

    public Map<String, Set<String>> getSyntheticServiceMappings() {
        return syntheticServiceMappings;
    }

    /**
     * Get event monitor status and state type from JSON event check result.
     *
     * @param jsonCheckResult JSON event check result
     * @param isHostCheckResult
     * @return event monitor status and state type
     */
    private static String [] eventCheckResultMonitorStatus(JsonNode jsonCheckResult, boolean isHostCheckResult) {
        if (jsonCheckResult.has("vars_after")) {
            JsonNode jsonVarsAfter = jsonCheckResult.get("vars_after");
            if (jsonVarsAfter.isObject()) {
                String [] statusAndState = new String[2];
                Boolean reachable = attrsBooleanMember(jsonVarsAfter, "reachable");
                Integer state = attrsIntegerMember(jsonVarsAfter, "state");
                if (isHostCheckResult) {
                    // host status
                    statusAndState[0] = hostMonitorStatus(state, reachable, null);
                } else {
                    // service state
                    statusAndState[0] = serviceMonitorStatus(state, reachable, null);
                }
                Integer stateType = attrsIntegerMember(jsonVarsAfter, "state_type");
                if (stateType != null) {
                    switch (stateType) {
                        case 0: statusAndState[1] = "SOFT"; break;
                        case 1: statusAndState[1] = "HARD"; break;
                    }
                }
                return statusAndState;
            }
        }
        return null;
    }

    /**
     * Extract JSON string member.
     *
     * @param jsonAttrs JSON object
     * @param member member name
     * @return member value or null
     */
    private static String attrsStringMember(JsonNode jsonAttrs, String member) {
        if (!jsonAttrs.has(member)) {
            return null;
        }
        JsonNode jsonStringMember = jsonAttrs.get(member);
        if (!jsonStringMember.isValueNode()) {
            return null;
        }
        String stringMember = jsonStringMember.asText();
        if (stringMember != null) {
            stringMember = stringMember.trim();
        }
        return (((stringMember != null) && (stringMember.length() > 0)) ? stringMember : null);
    }

    /**
     * Extract JSON integer member.
     *
     * @param jsonAttrs JSON object
     * @param member member name
     * @return member value or null
     */
    private static Integer attrsIntegerMember(JsonNode jsonAttrs, String member) {
        if (!jsonAttrs.has(member)) {
            return null;
        }
        JsonNode jsonIntegerMember = jsonAttrs.get(member);
        if (!jsonIntegerMember.isNumber()) {
            return null;
        }
        return jsonIntegerMember.asInt();
    }

    /**
     * Extract JSON boolean member.
     *
     * @param jsonAttrs JSON object
     * @param member member name
     * @return member value or null
     */
    private static Boolean attrsBooleanMember(JsonNode jsonAttrs, String member) {
        if (!jsonAttrs.has(member)) {
            return null;
        }
        JsonNode jsonBooleanMember = jsonAttrs.get(member);
        if (!jsonBooleanMember.isBoolean()) {
            return null;
        }
        return jsonBooleanMember.getBooleanValue();
    }

    /**
     * Constant used to convert BigDecimal seconds since epoch to millis.
     */
    private static BigDecimal MILLIS_PER_SEC = new BigDecimal("1000.0");

    /**
     * Extract Icinga2 JSON decimal date member.
     *
     * @param jsonAttrs JSON object
     * @param member member name
     * @return member value or null
     */
    private static Date attrsTimestampMember(JsonNode jsonAttrs, String member) {
        Long time = attrsLongTimestampMember(jsonAttrs, member);
        return ((time != null) ? new Date(time) : null);
    }

    /**
     * Extract Icinga2 JSON decimal date member.
     *
     * @param jsonAttrs JSON object
     * @param member member name
     * @return member value or null
     */
    private static Long attrsLongTimestampMember(JsonNode jsonAttrs, String member) {
        if (!jsonAttrs.has(member)) {
            return null;
        }
        JsonNode jsonTimestampMember = jsonAttrs.get(member);
        if (!jsonTimestampMember.isBigDecimal()) {
            return null;
        }
        return jsonTimestampMember.getDecimalValue().multiply(MILLIS_PER_SEC).longValue();
    }

    /**
     * Extract Icinga2 JSON performance data labels from check result.
     *
     * @param jsonAttrs JSON object
     * @param checkResultMember check result member name
     * @return list of performance data labels
     */
    private static List<String> attrsPerformanceDataLabels(JsonNode jsonAttrs, String checkResultMember) {
        List<String> performanceDataLabels = new ArrayList<String>();
        if (jsonAttrs.has(checkResultMember)) {
            JsonNode jsonCheckResult = jsonAttrs.get(checkResultMember);
            if (jsonCheckResult.isObject() && jsonCheckResult.has("performance_data")) {
                JsonNode jsonPerformanceData = jsonCheckResult.get("performance_data");
                if (jsonPerformanceData.isArray()) {
                    for (Iterator<JsonNode> elementIter = jsonPerformanceData.getElements(); elementIter.hasNext(); ) {
                        JsonNode jsonPerformanceDataElement = elementIter.next();
                        String performanceDataLabel = null;
                        if (jsonPerformanceDataElement.isTextual()) {
                            String performanceDataText = jsonPerformanceDataElement.getTextValue();
                            String [] performanceData;
                            try {
                                performanceData = parsePerformanceData(performanceDataText, 1);
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Cannot parse Icinga2 performance data: " + performanceDataText);
                            }
                            performanceDataLabel = performanceData[0];
                        } else if (jsonPerformanceDataElement.isObject()) {
                            performanceDataLabel = attrsStringMember(jsonPerformanceDataElement, "label");
                        } else {
                            throw new IllegalArgumentException("Unsupported Icinga2 performance data");
                        }
                        if (performanceDataLabel != null) {
                            performanceDataLabels.add(performanceDataLabel);
                        }
                    }
                }
            }
        }
        return performanceDataLabels;
    }

    /**
     * Return amalgam host monitor status.
     *
     * @param state host state code or null
     * @param reachable host reachable or null
     * @param downtime host in downtime or null
     * @return host monitor status
     */
    private static String hostMonitorStatus(Integer state, Boolean reachable, Boolean downtime) {
        if ((reachable != null) && !reachable) {
            return GwosStatus.UNREACHABLE.status;
        }
        if (state != null) {
            switch (state) {
                case 0: return GwosStatus.UP.status;
                case 1: {
                    if (((downtime != null) && downtime)) {
                        return GwosStatus.SCHEDULED_DOWN.status;
                    }
                    return GwosStatus.UNSCHEDULED_DOWN.status;
                }
            }
        }
        return GwosStatus.PENDING.status;
    }

    /**
     * Return amalgam service monitor status.
     *
     * @param state service state code or null
     * @param reachable service reachable or null
     * @param downtime service in downtime or null
     * @return service monitor status
     */
    private static String serviceMonitorStatus(Integer state, Boolean reachable, Boolean downtime) {
        if ((reachable != null) && !reachable) {
            return GwosServiceStatus.UNKNOWN.status;
        }
        if (state != null) {
            switch (state) {
                case 0: return GwosServiceStatus.OK.status;
                case 1: return GwosServiceStatus.WARNING.status;
                case 2: {
                    if (((downtime != null) && downtime)) {
                        return GwosServiceStatus.SCHEDULED_CRITICAL.status;
                    }
                    return GwosServiceStatus.UNSCHEDULED_CRITICAL.status;
                }
                case 3: return GwosServiceStatus.UNKNOWN.status;
            }
        }
        return GwosServiceStatus.PENDING.status;
    }

    /**
     * Parse performance data string.
     *
     * 'label'=value[UOM];[warn];[crit];[min];[max]
     *
     * Notes:
     *
     * - label can contain any characters
     * - the single quotes for the label are optional. Required if spaces, = or ' are in the label
     * - label length is arbitrary, but ideally the first 19 characters are unique (due to a limitation in RRD). Be
     *   aware of a limitation in the amount of data that NRPE returns to Nagios
     * - to specify a quote character, use two single quotes
     * - warn, crit, min, and/or max, respectively, may be null (for example, if the threshold is not defined or min
     *   and max do not apply). Trailing unfilled semicolons can be dropped
     * - min and max are not required if UOM=%
     * - value, min and max in class [-0-9.]. Must all be the same UOM
     * - warn and crit are in the range format (see Section 2.5). Must be the same UOM
     * - UOM (unit of measurement) is one of:
     *   no unit specified - assume a number (int or float) of things (eg, users, processes, load averages)
     *   s - seconds (also us, ms)
     *   % - percentage
     *   B - bytes (also KB, MB, TB, GB?)
     *   c - a continous counter (such as bytes transmitted on an interface)
     *
     * @param performanceData performance data string
     * @param performanceDataLimit performance data fields limit
     * @return performance data fields array
     */
    private static String [] parsePerformanceData(String performanceData, int performanceDataLimit) {
        List<String> parsedPerformanceData = new ArrayList<String>();
        char parseChar = '\0';
        char lastParseChar = '\0';
        boolean parsingQuotedLabel = false;
        boolean parsingLabel = false;
        boolean parsingValueWithUOM = false;
        boolean parsingValueWithUOMSkip = false;
        boolean parsingValue = false;
        StringBuilder token = new StringBuilder();
        for (int parseIndex = 0, limit = performanceData.length(); ((parseIndex < limit) && (parsedPerformanceData.size() < performanceDataLimit)); parseIndex++) {
            lastParseChar = parseChar;
            parseChar = performanceData.charAt(parseIndex);
            // parse by state
            if (parsingQuotedLabel) {
                if ((parseChar != '\'') || (lastParseChar == '\'')) {
                    if ((parseChar == '=') && (lastParseChar == '\'')) {
                        if (token.length() == 0) {
                            throw new RuntimeException("Missing label");
                        }
                        parsedPerformanceData.add(token.toString());
                        parsingQuotedLabel = false;
                        parsingValueWithUOM = true;
                        parseChar = '\0';
                        token.setLength(0);
                    } else if ((parseChar == '\'') && (lastParseChar == '\'')) {
                        token.append(parseChar);
                        parseChar = '\0';
                    } else if (lastParseChar == '\'') {
                        throw new RuntimeException("Unexpected quote in label");
                    } else {
                        token.append(parseChar);
                    }
                }
                continue;
            }
            if (parsingLabel) {
                if (parseChar == '=') {
                    if (token.length() == 0) {
                        throw new RuntimeException("Missing label");
                    }
                    parsedPerformanceData.add(token.toString());
                    parsingLabel = false;
                    parsingValueWithUOM = true;
                    token.setLength(0);
                } else {
                    token.append(parseChar);
                }
                continue;
            }
            if (parsingValueWithUOM) {
                if (parseChar == ';') {
                    if (token.length() == 0) {
                        throw new RuntimeException("Missing value");
                    }
                    parsedPerformanceData.add(token.toString());
                    parsingValueWithUOM = false;
                    parsingValueWithUOMSkip = false;
                    parsingValue = true;
                    token.setLength(0);
                } else if (!parsingValueWithUOMSkip && (Character.isDigit(parseChar) || (parseChar == '.') || (parseChar == '-'))) {
                    token.append(parseChar);
                } else {
                    parsingValueWithUOMSkip = true;
                }
                continue;
            }
            if (parsingValue) {
                if (parseChar == ';') {
                    if (token.length() > 0) {
                        parsedPerformanceData.add(token.toString());
                        token.setLength(0);
                    } else {
                        parsedPerformanceData.add(null);
                    }
                } else if (Character.isDigit(parseChar) || (parseChar == '.') || (parseChar == '-')) {
                    token.append(parseChar);
                }
                continue;
            }
            // start parse
            if (parseChar == '\'') {
                parsingQuotedLabel = true;
                parseChar = '\0';
            } else {
                parsingLabel = true;
                token.append(parseChar);
            }
        }
        if (token.length() > 0) {
            parsedPerformanceData.add(token.toString());
        }
        if ((parsedPerformanceData.size() < 2) && (performanceDataLimit > 1)) {
            throw new RuntimeException("Missing label or value");
        }
        return parsedPerformanceData.toArray(new String[parsedPerformanceData.size()]);
    }
}
