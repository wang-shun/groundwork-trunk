package com.groundwork.downtime.http;

import com.groundwork.downtime.DowntimeMaintenanceWindow;
import com.groundwork.downtime.DtoDowntime;
import org.joda.time.Period;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TransitionWindowCalculator {

    public static final String NONE_SCHEDULED = "None scheduled";

    public static DowntimeMaintenanceWindow calculateTransitionWindow(Map<String, List<DowntimeMaintenanceWindow>> transitions,
                                                                      DtoDowntime downtime,
                                                                      Date startRange,
                                                                      Date endRange) {
        if (isEmpty(downtime.getHost()) || isEmpty(downtime.getService())) {
            return null;
        }
        Date dtStart = downtime.getStart();
        Date dtEnd = (downtime.getEnd() == null) ? endRange : downtime.getEnd();
        Date dtComplete = dtEnd;
        if (dtStart.after(endRange)) {
            return null;
        }
        if (dtEnd.before(startRange)) {
            return null;
        }
        if (dtStart.before(startRange)) {
            dtStart = startRange;
        }
        if (dtEnd.after(endRange)) {
            dtEnd = endRange;
        }
        float durationInState = dtEnd.getTime() - dtStart.getTime();
        float durationWindow = endRange.getTime() - startRange.getTime();
        long now = new Date().getTime();
        if (durationWindow > 0) {
            float percentage = ((durationInState / durationWindow) * 100.0f);
            DowntimeMaintenanceWindow.MaintenanceStatus status = DowntimeMaintenanceWindow.MaintenanceStatus.None;
            StringBuffer message = new StringBuffer();
            if (now > dtEnd.getTime()) { // expired
                status = DowntimeMaintenanceWindow.MaintenanceStatus.Expired;
                message.append("Expired ");
                Period period = new Period(now - dtComplete.getTime());
                message.append(String.format("%02dh:%02dm", period.getHours(), period.getMinutes()));
                message.append(" ago");
            } else if (now > dtStart.getTime() && now < dtEnd.getTime()) { // in downtime
                status = DowntimeMaintenanceWindow.MaintenanceStatus.Active;
                message.append("Complete in ");
                Period period = new Period(dtComplete.getTime() - now);
                message.append(String.format("%02dh:%02dm", period.getHours(), period.getMinutes()));
            } else if (dtStart.getTime() > now) { // Pending
                status = DowntimeMaintenanceWindow.MaintenanceStatus.Pending;
                message.append("Pending in ");
                Period period = new Period(dtStart.getTime() - now);
                message.append(String.format("%02dh:%02dm", period.getHours(), period.getMinutes()));
            }
            DowntimeMaintenanceWindow window = new DowntimeMaintenanceWindow(status, percentage, message.toString(), dtStart, dtEnd);
            List<DowntimeMaintenanceWindow> windows = transitions.get(makeKey(downtime.getHost(), downtime.getService()));
            if (windows == null) {
                windows = new LinkedList<>();
                transitions.put(makeKey(downtime.getHost(), downtime.getService()), windows);
            }
            windows.add(window);
            return window;
        }
        return null;
    }

    public static boolean isEmpty(String s) {
        if (s == null) return true;
        if (s.trim().equals("")) return true;
        return false;
    }

    public static String makeKey(String hostName, String serviceName) {
        return hostName + "::" + serviceName;
    }

    /**
     * Adds gaps to the window list to make it easier for the UI to plot the transitions
     *
     * @param transitions
     * @param startRange
     * @param endRange
     * @return
     */
    public static List<DowntimeMaintenanceWindow> addGapsToWindowList(List<DowntimeMaintenanceWindow> transitions,
                                                                      Date startRange,
                                                                      Date endRange) {
        List<DowntimeMaintenanceWindow> result = new LinkedList<>();
        float durationWindow = endRange.getTime() - startRange.getTime();
        Date current = startRange;
        for (DowntimeMaintenanceWindow window : transitions) {
            if (window.getStartDate().after(current)) {
                float durationInState = window.getStartDate().getTime() - current.getTime();
                float percentage = ((durationInState / durationWindow) * 100.0f);
                DowntimeMaintenanceWindow gap = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.None, percentage, NONE_SCHEDULED, current, window.getStartDate());
                result.add(gap);
            }
            current = window.getEndDate();
            result.add(window);
        }
        if (current.before(endRange)) {
            float durationInState = endRange.getTime() - current.getTime();
            float percentage = ((durationInState / durationWindow) * 100.0f);
            DowntimeMaintenanceWindow gap = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.None, percentage, NONE_SCHEDULED, current, endRange);
            result.add(gap);
        }
        return result;
    }

    public static DowntimeMaintenanceWindow calculateTotalPercentageAndCurrentMessage(List<DowntimeMaintenanceWindow> windows) {
        float total = 0.0f;
        Date now = new Date();
        DowntimeMaintenanceWindow result  = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.None, 0f, NONE_SCHEDULED);
        boolean found = false;
        for (DowntimeMaintenanceWindow window : windows) {
            if (window.getStatus().equals(DowntimeMaintenanceWindow.MaintenanceStatus.None)) {
                continue;
            }
            if (window.getStartDate() == null || window.getEndDate() == null) {
                continue;
            }
            total += window.getPercentage();
            if (now.after(window.getStartDate()) && now.before(window.getEndDate())) {
               result.setMessage(window.getMessage());
               result.setStatus(window.getStatus());
               found = true;
            }
            if (!found) {
                result.setMessage(window.getMessage());
                result.setStatus(window.getStatus());
            }
        }
        result.setPercentage(total);
        return result;
    }

    public static DowntimeMaintenanceWindow.MaintenanceStatus calculateCurrentStatus(List<DowntimeMaintenanceWindow> windows) {
        Date now = new Date();
        for (DowntimeMaintenanceWindow window : windows) {
            if (now.after(window.getStartDate()) && now.before(window.getEndDate())) {
                return window.getStatus();
            }
        }
        return DowntimeMaintenanceWindow.MaintenanceStatus.None;
    }

}