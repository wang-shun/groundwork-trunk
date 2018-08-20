package com.groundwork.dashboard.portlets;


import com.groundwork.dashboard.NocConfiguration;
import com.groundwork.dashboard.configuration.CheckedState;
import com.groundwork.dashboard.configuration.DashboardConfiguration;
import com.groundwork.dashboard.portlets.dto.NocBoardComment;
import com.groundwork.dashboard.portlets.dto.NocBoardResult;
import com.groundwork.dashboard.portlets.dto.NocBoardService;
import com.groundwork.downtime.DowntimeContext;
import com.groundwork.downtime.DowntimeException;
import com.groundwork.downtime.DowntimeMaintenanceWindow;
import com.groundwork.downtime.DowntimeService;
import com.groundwork.downtime.DowntimeServiceFactory;
import com.groundwork.downtime.http.TransitionWindowCalculator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.StateTransition;
import org.groundwork.rs.client.EventClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.client.ServiceGroupClient;
import org.groundwork.rs.dto.*;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class NocBoardEngine {

    protected static Log log = LogFactory.getLog(NocBoardEngine.class);

    public static final String PROBLEM_ACKNOWLEDGED_PROPERTY_NAME = "isProblemAcknowledged";
    private static final String ACKNOWLEDGEDBY = "AcknowledgedBy";
    private static final String ACKNOWLEDGE_COMMENT = "AcknowledgeComment";
    private static final DateFormat DATE_FORMAT_US = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");

    public static NocBoardResult calculateBoard(String hostGroupName, String serviceGroupName,
                                                DashboardConfiguration dashboardConfiguration,
                                                DowntimeContext context) {
        NocBoardResult result = new NocBoardResult();
        long firstTimer = System.currentTimeMillis();
        long msExecHosts = 0, msExecAvailability = 0, msExecTransitionsQuery = 0;
        int randomCount = 1;
        Boolean metricsEnabled = NocConfiguration.getBooleanProperty(NocConfiguration.NOC_METRICS_ENABLE);
        Boolean availabilityEnabled = NocConfiguration.getBooleanProperty(NocConfiguration.NOC_AVAILABILITY_ENABLE);
        Boolean unscheduledDowntimeEnabled = false; // NocConfiguration.getBooleanProperty(NocConfiguration.NOC_DOWNTIME_UNSCHEDULED_ENABLE);
        try {
            Map<String,DtoHost> hostMap = new HashMap<>();
            Collection<DtoService> services = mergeHostAndServiceGroups(hostGroupName, serviceGroupName, hostMap);
            if (services.size() == 0) {
                return result;
            }
            if (metricsEnabled) {
                logMetrics("NOC: start ----------------------------------------------------");
                logMetrics("NOC: merge groups: " + (System.currentTimeMillis() - firstTimer));
            }
            result.setServiceGroup(serviceGroupName);
            result.setHostGroup(hostGroupName);
            result.setPrefs(dashboardConfiguration);
            result.setAutoExpand(dashboardConfiguration.getAutoExpand());

            // calculate scheduled downTimes
            DowntimeService downtimeService = DowntimeServiceFactory.getServiceInstance();
            Boolean enableDowntime = NocConfiguration.getBooleanProperty(NocConfiguration.NOC_DOWNTIME_ENABLE);
            Map<String, List<DowntimeMaintenanceWindow>> scheduledDownTimes = null;
            if (enableDowntime) {
                long downtimeTimer = System.currentTimeMillis();
                DateTime now = new DateTime();
                //long halfWindow = (dashboardConfiguration.getDowntimeHours() * 3600000) / 2;
                int halfWindow = (dashboardConfiguration.getDowntimeHours() * 60) / 2;
                DateTime start = now.minusMinutes(halfWindow);
                DateTime end = now.plusMinutes(halfWindow);
                try {
                    if (context != null) {
                        scheduledDownTimes = downtimeService.range(context, start.toDate(), end.toDate());
                    }
                }
                catch (DowntimeException e) {
                    try {
                        context = downtimeService.relogin(context);
                        scheduledDownTimes = downtimeService.range(context, start.toDate(), end.toDate());
                    }
                    catch (Exception ee) {
                        String msg = "Downtime service unavailable: " + ee.getMessage();
                        log.error(msg, ee);
                        result.setMessage(msg);
                        scheduledDownTimes = new HashMap<>();
                    }
                }
                if (metricsEnabled) {
                    logMetrics("NOC: scheduled down time: " + (System.currentTimeMillis() - downtimeTimer));
                }
            }

            Map<DtoServiceKey, List<DtoStateTransition>> transitionMap = null;
            DateTime dateTime = new DateTime().minusHours(dashboardConfiguration.getAvailabilityHours());
            Date startDate = dateTime.toDate();
            if (availabilityEnabled) {
                long availabilityTimer = System.currentTimeMillis();
                Date endDate = new Date();
                transitionMap = retrieveServiceAvailabilities(services, startDate, endDate);
                if (metricsEnabled) {
                    msExecAvailability += (System.currentTimeMillis() - availabilityTimer);
                }
            }

            List<Float> slaAvailabilityList = new ArrayList<>();
            Set<String> hostsCounted = new HashSet<>();
            for (DtoService dtoService : services) {
                result.incrementServiceCounts(dtoService.getMonitorStatus());

                if (!hostsCounted.contains(dtoService.getHostName())) {
                    hostsCounted.add(dtoService.getHostName());
                    long hostsTimer = System.currentTimeMillis();
                    DtoHost dtoHost = hostMap.get(dtoService.getHostName());
                    if (dtoHost == null) {
                        dtoHost = lookupHost(dtoService.getHostName());
                    }
                    result.incrementHostCounts(dtoHost.getMonitorStatus());
                    msExecHosts += (System.currentTimeMillis() - hostsTimer);
                }

                NocBoardService service = new NocBoardService();
                service.setHostName(dtoService.getHostName());
                service.setName(dtoService.getDescription());
                service.setStatus(dtoService.getMonitorStatus());
                service.setAppType(dtoService.getAppType());

                //calculate service availability
                StateTransition[] transitions = null;
                Date endDate = new Date();
                if (availabilityEnabled) {
                    List<DtoStateTransition> dto = transitionMap.get(new DtoServiceKey(dtoService.getDescription(), dtoService.getHostName()));
                    if (dto == null) {
                        log.error("key not found: " + dtoService.getDescription() + ": " + dtoService.getHostName());
                    }
                    else {
                        transitions = convertTransitions(dto);
                        //service.setStateTransitions(transitions);
                        service.setAvailability(calculateAvailability(transitions, startDate, endDate, service.getStatus()));
                        slaAvailabilityList.add(service.getAvailability());
                    }
                }

                service.setAckBool(dtoService.getPropertyBoolean("isProblemAcknowledged"));

                if (!ackFilteredIn(service.getAckBool(), dashboardConfiguration.getAckFilters())) {
                    //service does not meet the ack filter condition, so go on to the next service without adding this one to the result
                    continue;
                }

                if (!statusFilteredIn(service.getStatus(), dashboardConfiguration.getStates())) {
                    //service does not meet the state filter condition, so go on to the next service without adding this one to the result
                    continue;
                }
                boolean inScheduledDowntime = false;
                if(dtoService.getProperty("ScheduledDowntimeDepth") != null) {
                    if (!dtoService.getProperty("ScheduledDowntimeDepth").matches("0")){
                        inScheduledDowntime = true;
                    }
                }

                if(!downtimeFilteredIn(inScheduledDowntime, dashboardConfiguration.getDownTimeFilters())){
                    //service does not meet the downtime filters
                    continue;
                }
                if(inScheduledDowntime) {
                    service.setMaintenanceStatus("Active");
                }
                else {
                    service.setMaintenanceStatus("In Downtime");
                }

                service.setStatusText(dtoService.getProperty("LastPluginOutput"));

                service.setLastCheckTime(dtoService.getLastCheckTime());

                List<DtoComment> comments = dtoService.getComments();
                List<NocBoardComment> nocBoardComments = new ArrayList<>();
                if (comments != null) {
                    for (DtoComment comment : comments) {
                        NocBoardComment nocBoardComment = new NocBoardComment(comment.getId().toString(), comment.getCreatedOn().toString(), comment.getAuthor(), comment.getNotes());
                        nocBoardComments.add(nocBoardComment);
                    }
                }
                service.setCommentsList(nocBoardComments);

                //if (service.getAvailability() > (float) dashboardConfiguration.getPercentageSLA()) {
                    //service does not meet the availability filter condition, so go on to the next service without adding this one to the result
                //    continue;
                //}

                // Duration (Time in last state) column
                if (transitions != null && transitions.length > 0) {
                    int last = transitions.length - 1;
                    if(transitions[last] != null) {
                        service.setTimeStarted(transitions[last].getToTransitionDate());

                        if(transitions[last].getToTransitionDate() != null) {
                            service.setTimeInState(endDate.getTime() - transitions[last].getToTransitionDate().getTime());

                        }
                    }
                }

                if (service.getAckBool()) {
                    if (dtoService.getProperty(ACKNOWLEDGEDBY) != null) {
                        service.setAcknowledger(dtoService.getProperty(ACKNOWLEDGEDBY));
                    }
                    if (dtoService.getProperty(ACKNOWLEDGE_COMMENT) != null) {
                        service.setAcknowledgeComment(dtoService.getProperty(ACKNOWLEDGE_COMMENT));
                    }
                }

                // gather downtime from precalculated window
                if (enableDowntime) {
                    List<DowntimeMaintenanceWindow> downTimesPerService = downtimeService.lookup(dtoService.getHostName(), dtoService.getDescription(), scheduledDownTimes);
                    service.setMaintenanceWindows(downTimesPerService);
                    DowntimeMaintenanceWindow active = TransitionWindowCalculator.calculateTotalPercentageAndCurrentMessage(downTimesPerService);
                    service.setMaintenancePercent(active.getPercentage());
                    service.setMaintenanceMessage(active.getMessage());
                    //service.setMaintenanceStatus(TransitionWindowCalculator.calculateCurrentStatus(downTimesPerService).name());
                }

                if (unscheduledDowntimeEnabled) {
                    calculateUnscheduledDowntimes(transitions, dashboardConfiguration, service);
                }

                result.addService(service);

            }
            float groupSlaAvailability = getGroupSLA(slaAvailabilityList);
            result.setSlaPercent(groupSlaAvailability);
            result.setSlaMet(isSlaAvailabilityMet(groupSlaAvailability, (float) dashboardConfiguration.getPercentageSLA()));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setMessage(e.getMessage());
            result.setSuccess(false);
        }
        if (metricsEnabled) {
            logMetrics("NOC: host calc: " + msExecHosts);
            logMetrics("NOC: avail calc: " + msExecAvailability);
            logMetrics("NOC: full: " + (System.currentTimeMillis() - firstTimer));
            logMetrics("NOC: end ----------------------------------------------------");
        }
        return result;
    }

    protected static Map<DtoServiceKey, List<DtoStateTransition>> retrieveServiceAvailabilities(Collection<DtoService> services, Date startDate, Date endDate) {
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        EventClient ec = new EventClient(foundationRestService);
        List<DtoServiceKey> keys = new LinkedList<>();
        for (DtoService service : services) {
            keys.add(new DtoServiceKey(service.getDescription(), service.getHostName()));
        }
        return ec.getStateTransitions(keys, DATE_FORMAT_US.format(startDate), DATE_FORMAT_US.format(endDate));
    }


    private static float getGroupSLA(List<Float> slaAvailabilityList){
        if (slaAvailabilityList.size() < 1) {
            //prevents divide by 0
            return -1.0f;
        }
        float total = 0.0f;

        for (float percent : slaAvailabilityList) {
            total += percent;
        }
        return total / slaAvailabilityList.size();


    }
    private static boolean isSlaAvailabilityMet(float groupAvailability, float threshold) {
        boolean met = true;

        if (groupAvailability < threshold) {
            met = false;
        }
        return met;

    }

    //returns true if the status meets the filter condition, false otherwise
    private static boolean downtimeFilteredIn(boolean inScheduledDowntime, List<CheckedState> downtimeStates) {
        for (CheckedState downtimeFilter : downtimeStates) {
            if (downtimeFilter.getName().equals(DashboardConfiguration.IN_DOWNTIME)) {
                if (downtimeFilter.getChecked() && inScheduledDowntime) {
                    return true;
                }
            }
            else if(downtimeFilter.getName().equals(DashboardConfiguration.NOT_IN_DOWNTIME)) {
                if (downtimeFilter.getChecked() && !inScheduledDowntime) {
                    return true;
                }
            }
        }
        return false;
    }

    //returns true if the status meets the filter condition, false otherwise
    private static boolean statusFilteredIn(String status, List<CheckedState> states) {
        for (CheckedState stateFilter : states) {
            if (status.equalsIgnoreCase(stateFilter.getName())) {
                if (stateFilter.getChecked()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    //returns true if the ack meets the filter condition, false otherwise
    protected static boolean ackFilteredIn(Boolean ack, List<CheckedState> ackFilters) {
        for (CheckedState ackFilter : ackFilters) {
            if ((ackFilter.getName().equalsIgnoreCase(DashboardConfiguration.ACKED) && ackFilter.getChecked().equals(ack))
                    || (ackFilter.getName().equalsIgnoreCase(DashboardConfiguration.NOT_ACKED) && ackFilter.getChecked().equals(!ack))) {
                return true;
            }
        }
        return false;
    }

    protected static DtoHost lookupHost(String hostName) {
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        HostClient client = new HostClient(foundationRestService);
        return client.lookup(hostName, DtoDepthType.Shallow);
    }

    protected static StateTransition[] getStateTransactions(String hostName, String serviceDescription, Date startDate, Date endDate) {
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        EventClient ec = new EventClient(foundationRestService);
        List<DtoStateTransition> stateTransitions = ec.getStateTransitions(hostName, serviceDescription, DATE_FORMAT_US.format(startDate), DATE_FORMAT_US.format(endDate));
        return convertTransitions(stateTransitions);
    }

    private static StateTransition[] convertTransitions(List<DtoStateTransition> stateTransitions) {
        StateTransition[] transitions = new StateTransition[stateTransitions.size()];
        int index = 0;
        for (DtoStateTransition dto : stateTransitions) {
            transitions[index] = (new StateTransition(
                    dto.getHostName(),
                    dto.getServiceName(),
                    (dto.getFromStatus() == null) ? null : new MonitorStatus(dto.getFromStatus().getMonitorStatusId(), dto.getFromStatus().getName(), dto.getFromStatus().getDescription()),
                    dto.getFromTransitionDate(),
                    (dto.getToStatus() == null) ? null : new MonitorStatus(dto.getToStatus().getMonitorStatusId(), dto.getToStatus().getName(), dto.getToStatus().getDescription()),
                    dto.getToTransitionDate(),
                    null,
                    dto.getDurationInState()));
            index = index + 1;
        }
        return transitions;
    }

    private static long getDurationInMinutes(Date startDate, Date endDate) {
        return ((endDate.getTime() - startDate.getTime()) / 60000);

    }

    private static List<StateTransition> getSortedTransitions(StateTransition[] stateTransitions) {
        List<StateTransition> transitionList = new ArrayList<>();
        if (stateTransitions != null) {
            for (StateTransition transition : stateTransitions) {
                transitionList.add(transition);
            }
            if (transitionList.isEmpty()) {
                return transitionList;
            }
            //sort the list of transitions on from start date
            Collections.sort(transitionList, new transitionComparator());
        }
        return transitionList;
    }

    private static List<StateTransition> getTransitionsInWindow(List<StateTransition> transitionList, Date startDate) {
        //get first only items that are after the start window
        List<StateTransition> windowTransitions = new ArrayList<>();
        for (int i = 0; i < transitionList.size(); i++) {
            StateTransition firstTransition = transitionList.get(i);
            if (firstTransition == null) {
                break;
            }

            if (firstTransition.getToTransitionDate() == null || firstTransition.getToTransitionDate().before(startDate)) {
                continue;
            } else {
                windowTransitions = transitionList.subList(i, transitionList.size());
                break;
            }

        }
        return windowTransitions;
    }

    protected static float calculateAvailability(StateTransition[] stateTransitions, Date startDate, Date endDate, String currentStatus) {
        Long windowLength = endDate.getTime() - startDate.getTime();
        float availabilty = 100.00f;
        List<StateTransition> transitionList = getSortedTransitions(stateTransitions);

        List<StateTransition> windowTransitions = getTransitionsInWindow(transitionList, startDate);

        //get first
        if (windowTransitions.size() < 1) {
            if (isUP(currentStatus)) {
                availabilty = 100.00f;
            } else {
                availabilty = 0.00f;
            }
            return availabilty;
        }
        StateTransition firstTransition = windowTransitions.get(0);
        if (firstTransition == null) {
            return -4f;
        }
        MonitorStatus firstStatus = firstTransition.getFromStatus();
        Date fromDate = firstTransition.getFromTransitionDate();
        Long timeDown = 0L;
        Long statusLength = 0L;
        if (fromDate == null ||fromDate.before(startDate)) {
            //from date of first transition is prior to the start of the availability window
            //therefore, to calculate how long in the window it was fromStatus, subtract the to transition time from the start of the window
            statusLength = firstTransition.getToTransitionDate().getTime() - startDate.getTime();
            if (!isUP(firstTransition.getToStatus().getName())) {
                timeDown += statusLength;
            }
        } else {
            //from date of first transition is after the start of the availability window
            statusLength = firstTransition.getDurationInState();
            if (!isUP(firstStatus.getName())) {
                timeDown += statusLength;
            }
        }


        //get last
        StateTransition lastTransition = windowTransitions.get(windowTransitions.size() - 1);
        if (lastTransition == null) {
            return -5f;
        }
        MonitorStatus lastStatus = lastTransition.getToStatus();

        statusLength = endDate.getTime() - lastTransition.getToTransitionDate().getTime();
        if (statusLength < 1) {
            return -6f;
            //enddate should be now, a last transition with a future date is broken here
        }
        if (!isUP(lastStatus.getName())) {
            timeDown += statusLength;
        }

        //in between first and last
        for (int i = 1; i < windowTransitions.size(); i++) {
            StateTransition transition = windowTransitions.get(i);
            MonitorStatus curStatus = transition.getFromStatus();
            statusLength = transition.getDurationInState();
            if (!isUP(curStatus.getName())) {
                timeDown += statusLength;
            }
        }
        availabilty = 100.00f * (windowLength.floatValue() - timeDown.floatValue()) / windowLength.floatValue();

        return availabilty;
    }

    //Check if current status is OK or end downtime
    private static boolean isUP(String currentStatus) {
        if(currentStatus == null){
            return false;
        }
        return currentStatus.toUpperCase().contains("OK") || currentStatus.toUpperCase().contains("END DOWNTIME");
    }

    protected static class transitionComparator implements Comparator<StateTransition> {
        public int compare(StateTransition st1, StateTransition st2) {
            return st1.getFromTransitionDate().compareTo(st2.getFromTransitionDate());
        }

    }

    protected static Collection<DtoService> mergeHostAndServiceGroups(String hostGroupName, String serviceGroupName, Map<String,DtoHost> hostMap) {
        Map<String,DtoService> mergedServices = new HashMap<>();
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        if (!isEmpty(serviceGroupName)) {
            ServiceGroupClient serviceGroupClient = new ServiceGroupClient(foundationRestService);
            DtoServiceGroup serviceGroup = serviceGroupClient.lookup(serviceGroupName, DtoDepthType.Deep);
            if (serviceGroup != null && serviceGroup.getServices().size() > 0) {
                for (DtoService dtoService : serviceGroup.getServices()) {
                    mergedServices.put(makeKey(dtoService.getHostName(), dtoService.getDescription()), dtoService);
                }
            }
        }
        if (!isEmpty(hostGroupName)) {
            HostGroupClient hostGroupClient = new HostGroupClient(foundationRestService);
            DtoHostGroup hostGroup = hostGroupClient.lookup(hostGroupName, DtoDepthType.Deep);
            if (hostGroup != null && hostGroup.getHosts().size() > 0) {
                for (DtoHost dtoHost : hostGroup.getHosts()) {
                    hostMap.put(dtoHost.getHostName(), dtoHost);
                    for (DtoService dtoService : dtoHost.getServices()) {
                        mergedServices.put(makeKey(dtoHost.getHostName(), dtoService.getDescription()), dtoService);
                    }
                }
            }
        }
        return mergedServices.values();
    }

    protected static Collection<DtoService> mergeHostAndServiceGroupsById(String hostGroupName, String serviceGroupName) {
        Map<String,DtoService> mergedServices = new HashMap<>();
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        ServiceClient sc = new ServiceClient(foundationRestService);
        if (!isEmpty(serviceGroupName)) {
            ServiceGroupClient serviceGroupClient = new ServiceGroupClient(foundationRestService);
            DtoServiceGroup serviceGroup = serviceGroupClient.lookup(serviceGroupName);
            if (serviceGroup != null && serviceGroup.getServices().size() > 0) {
                String query = buildServiceQuery(serviceGroup.getServices());
                List<DtoService> services = sc.query(query, DtoDepthType.Deep);
                for (DtoService dtoService : services) {
                    mergedServices.put(makeKey(dtoService.getHostName(), dtoService.getDescription()), dtoService);
                }
            }
        }
        if (!isEmpty(hostGroupName)) {
            HostGroupClient hostGroupClient = new HostGroupClient(foundationRestService);
            DtoHostGroup hostGroup = hostGroupClient.lookup(hostGroupName, DtoDepthType.Sync);
            if (hostGroup != null && hostGroup.getHosts().size() > 0) {
                for (DtoHost dtoHost : hostGroup.getHosts()) {
                    String query = buildHostQuery(hostGroup.getHosts());
                    List<DtoService> services = sc.query(query, DtoDepthType.Deep);
                    for (DtoService dtoService : services) {
                        mergedServices.put(makeKey(dtoHost.getHostName(), dtoService.getDescription()), dtoService);
                    }
                }
            }
        }
        return mergedServices.values();
    }

    public static boolean isEmpty(String s) {
        if (s == null) return true;
        if (s.trim().equals("")) return true;
        return false;
    }

    protected static String makeKey(String hostName, String serviceName) {
        return hostName + "::" + serviceName;
    }

    protected static String buildServiceQuery(List<DtoService> services) {
        StringBuffer query = new StringBuffer("id in (");
        int count = 0;
        for (DtoService host : services) {
            query.append(host.getId());
            if (count < (services.size()-1)) {
                query.append(",");
            }
            count++;
        }
        query.append(")");
        return query.toString();
    }

    protected static String buildHostQuery(List<DtoHost> hosts) {
        StringBuffer query = new StringBuffer("hostId in (");
        int count = 0;
        for (DtoHost host : hosts) {
            query.append(host.getId());
            if (count < (hosts.size()-1)) {
                query.append(",");
            }
            count++;
        }
        query.append(")");
        return query.toString();
    }

    public static List<String> listHostGroups() {
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        HostGroupClient hostGroupClient = new HostGroupClient(foundationRestService);
        List<String> result = new LinkedList<>();
        List<DtoHostGroup> hostGroups = hostGroupClient.list(DtoDepthType.Simple);
        if (hostGroups != null && hostGroups.size() > 0) {
            for (DtoHostGroup group : hostGroups) {
                result.add(group.getName());
            }
        }
        return result;
    }

    public static List<String> listServiceGroups() {
        String foundationRestService = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(foundationRestService);
        List<String> result = new LinkedList<>();
        List<DtoServiceGroup> serviceGroups = serviceGroupClient.list();
        if (serviceGroups != null && serviceGroups.size() > 0) {
            for (DtoServiceGroup group : serviceGroups) {
                result.add(group.getName());
            }
        }
        return result;
    }

    protected static void logMetrics(String message) {
        System.out.println(message);
    }


    /**
     * TODO: Under Construction, not completed
     *
     * @param transitions
     * @param dashboardConfiguration
     * @param service
     * @return
     */
    protected static StateTransition[] calculateUnscheduledDowntimes(StateTransition[] transitions, DashboardConfiguration dashboardConfiguration, NocBoardService service) {

//        long halfWindow = (dashboardConfiguration.getDowntimeHours() * 3600000) / 2;
//        DateTime dateTime = new DateTime().minus(halfWindow);
//        Date startDate = dateTime.toDate();
//        dateTime = new DateTime().plus(halfWindow);
//        Date endDate = dateTime.toDate();
//        Map<String, DowntimeMaintenanceWindow> maintenanceWindows = downtimeService.range(null, startDate, endDate);
//        return maintenanceWindows;

        //convert downtime window into halves in units of milliseconds
        long halfWindow = (dashboardConfiguration.getDowntimeHours() * 3600000) / 2;
        DateTime dateTime = new DateTime().minus(halfWindow);
        Date startDate = dateTime.toDate();
        dateTime = new DateTime().plus(halfWindow);
        Date endDate = dateTime.toDate();
        Date now = new Date();
        //StateTransition[] transitions = getStateTransactions(service.getHostName(), service.getName(), startDate, endDate);

        String maintenanceStatus = "No";
        String maintenanceMessage = "None Scheduled";
        List<StateTransition> transitionList = getSortedTransitions(transitions);

        List<StateTransition> windowTransitions = getTransitionsInWindow(transitionList, startDate);

        //get first
        if (windowTransitions.size() > 0) {
            for (int i = 0; i < windowTransitions.size(); i++) {
                StateTransition transition = windowTransitions.get(i);
                if (transition.getToTransitionDate().before(now)) {
                    //end of transition is before now
                    if (transition.getToStatus().getName().toUpperCase().contains("END DOWNTIME")) {
                        maintenanceStatus = "No";
                        maintenanceMessage = "Expired " + getDurationInMinutes(transition.getToTransitionDate(), now) + " minutes ago";
                    } else if (transition.getToStatus().getName().toUpperCase().contains("START DOWNTIME")) {
                        maintenanceStatus = "Yes";
                        maintenanceMessage = "Started " + getDurationInMinutes(transition.getToTransitionDate(), now) + " minutes ago";
                    }
                }
            }
        }
        service.setMaintenanceStatus(maintenanceStatus);
        service.setMaintenanceMessage(maintenanceMessage);
        //service.setStateTransitions(transitions);
        return transitions;
    }

    protected static void mockDowntimeWindow(NocBoardService service, int randomCount) {
        List<DowntimeMaintenanceWindow> downTimesPerService = new LinkedList<>();
        DateTime start = DateTime.now().minusHours(12);
        DateTime end = DateTime.now().plusHours(12);
        if (randomCount % 2 == 0) {
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Expired, 20.0f, "test one", start.toDate(), end.toDate()));
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.None, 15.0f, "", start.toDate(), end.toDate()));
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Active, 25.0f, "test two", start.toDate(), end.toDate()));
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.None, 40.0f, "", start.toDate(), end.toDate()));
            service.setMaintenancePercent(45.0f);
        } else {
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.None, 35.0f, "", start.toDate(), end.toDate()));
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Active, 20.0f, "test one", start.toDate(), end.toDate()));
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.None, 10.0f, "", start.toDate(), end.toDate()));
            downTimesPerService.add(new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Pending, 35.0f, "test two", start.toDate(), end.toDate()));
            service.setMaintenancePercent(40.0f);
        }
        service.setMaintenanceWindows(downTimesPerService);
        DowntimeMaintenanceWindow active = TransitionWindowCalculator.calculateTotalPercentageAndCurrentMessage(downTimesPerService);
        service.setMaintenancePercent(active.getPercentage());
        service.setMaintenanceMessage(active.getMessage());
        //service.setMaintenanceStatus(TransitionWindowCalculator.calculateCurrentStatus(downTimesPerService).name());
    }

}
