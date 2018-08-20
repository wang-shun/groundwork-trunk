package org.groundwork.rs.conversion;

import com.groundwork.collage.biz.model.RTMMHost;
import com.groundwork.collage.biz.model.RTMMService;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.MonitorStatusBubbleUp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class HostStatusSupport {

    private static final String DATETIME_FORMAT_US = "MM/dd/yyyy hh:mm:ss a";

    /**
     * ServiceStatus monitor status extractor for bubble up computation.
     */
    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<ServiceStatus> BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<ServiceStatus>() {
                @Override
                public String extractMonitorStatus(ServiceStatus obj) {
                    return obj.getMonitorStatus().getName();
                }
            };

    /**
     * Calculate bubble up status for host.
     *
     * @param host host
     * @param monitorStatus monitor status
     * @return bubble up status
     */
    public static final String calculateBubbleUpStatus(Host host, String monitorStatus) {
        if (host != null) {
            String bubbleUpStatus = MonitorStatusBubbleUp.computeHostMonitorStatusBubbleUp(monitorStatus,
                    (Set<ServiceStatus>)host.getServiceStatuses(), BUBBLE_UP_EXTRACTOR);
            return bubbleUpStatus;
        }
        return null;
    }

    /**
     * Calculate service availability for host.
     *
     * @param host host
     * @return service availability
     */
    public static final double calculateServiceAvailability(Host host) {
        int count = 0;
        Set<ServiceStatus> serviceStatuses = host.getServiceStatuses();
        if (serviceStatuses != null) {
            for (ServiceStatus serviceStatus : serviceStatuses) {
                if (serviceStatus.getMonitorStatus() != null) {
                    String status = serviceStatus.getMonitorStatus().getName();
                    if (MonitorStatusBubbleUp.OK.equalsIgnoreCase(status)) {
                        count++;
                    }
                }
            }
        }
        if (count > 0) {
            return ((double)count / serviceStatuses.size()) * 100.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Build last plugin output string for service.
     *
     * @param serviceStatus service status
     * @return last plugin output string
     */
    public static final String buildLastPluginOutputStringForService(ServiceStatus serviceStatus) {
        // note: LastStateChange has been taken from service properties in
        // previous versions of this function; now it is taken from its
        // canonical source in the service status.
        return buildLastPluginOutputStringForService((Long)serviceStatus.getProperty("CurrentAttempt"),
                (Long)serviceStatus.getProperty("MaxAttempts"), (Integer)serviceStatus.getProperty("ScheduledDowntimeDepth"),
                serviceStatus.getLastStateChange(), (String)serviceStatus.getProperty("LastPluginOutput"),
                (String)serviceStatus.getProperty("PerformanceData"));
    }

    /**
     * Build last plugin output string for RTMM service.
     *
     * @param service RTMM service
     * @return last plugin output string
     */
    public static final String buildLastPluginOutputStringForService(RTMMService service) {
        return buildLastPluginOutputStringForService(service.getCurrentAttempt(), service.getMaxAttempts(),
                service.getScheduledDowntimeDepth(), service.getLastStateChange(), service.getLastPluginOutput(),
                service.getPerformanceData());
    }

    /**
     * Build last plugin output string for service.
     *
     * @return last plugin output string
     */
    private static final String buildLastPluginOutputStringForService(Long currentAttempt, Long maxAttempts,
                                                                      Integer scheduledDowntimeDepth, Date lastStateChange,
                                                                      String lastPluginOutput, String performanceData) {
        StringBuffer output = new StringBuffer();
        String delimiter = "^^^";
        DateFormat date = new SimpleDateFormat(DATETIME_FORMAT_US);
        if (currentAttempt != null) {
            output.append(currentAttempt.longValue());
        } else {
            output.append("-1");
        }
        output.append(delimiter);
        if (maxAttempts != null) {
            output.append(maxAttempts.longValue());
        } else {
            output.append("-1");
        }
        output.append(delimiter);
        if (scheduledDowntimeDepth != null) {
            output.append(scheduledDowntimeDepth.intValue());
        } else {
            output.append("-1");
        }
        output.append(delimiter);
        if (lastStateChange != null) {
            output.append(date.format(lastStateChange));
        } else {
            output.append("01/01/1970 12:00:00 AM");
        }
        output.append(delimiter);
        if (lastPluginOutput != null) {
            output.append(lastPluginOutput);
        } else {
            output.append("NA");
        }
        output.append(delimiter);
        if (performanceData != null) {
            output.append(performanceData);
        } else {
            output.append("NA");
        }
        return output.toString();
    }

    /**
     * Build last plugin output string for host.
     *
     * @param hostStatus host status
     * @return last plugin output string
     */
    public static final String buildLastPluginOutputStringForHost(HostStatus hostStatus) {
        return buildLastPluginOutputStringForHost((Long)hostStatus.getProperty("CurrentAttempt"),
                (Long)hostStatus.getProperty("MaxAttempts"), (Integer)hostStatus.getProperty("ScheduledDowntimeDepth"),
                (Date)hostStatus.getProperty("LastStateChange"), (String)hostStatus.getProperty("LastPluginOutput"),
                hostStatus.getNextCheckTime());
    }

    /**
     * RTMM service monitor status extractor for bubble up computation.
     */
    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<RTMMService> RTMM_BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<RTMMService>() {
                @Override
                public String extractMonitorStatus(RTMMService obj) {
                    return obj.getMonitorStatus();
                }
            };

    /**
     * Calculate bubble up status for RTMM host.
     *
     * @param host RTMM host
     * @param monitorStatus monitor status
     * @return bubble up status
     */
    public static final String calculateBubbleUpStatus(RTMMHost host, String monitorStatus) {
        if (host != null) {
            String bubbleUpStatus = MonitorStatusBubbleUp.computeHostMonitorStatusBubbleUp(monitorStatus,
                    host.getServices(), RTMM_BUBBLE_UP_EXTRACTOR);
            return bubbleUpStatus;
        }
        return null;
    }

    /**
     * Calculate service availability for RTMM host.
     *
     * @param host RTMM host
     * @return service availability
     */
    public static final double calculateServiceAvailability(RTMMHost host) {
        int count = 0;
        for (RTMMService service : host.getServices()) {
            if (MonitorStatusBubbleUp.OK.equalsIgnoreCase(service.getMonitorStatus())) {
                count++;
            }
        }
        return ((count > 0) ? ((double)count / host.getServices().size()) * 100.0 : 0.0);
    }

    /**
     * Build last plugin output string for RTMM host.
     *
     * @param host RTMM host
     * @return last plugin output string
     */
    public static final String buildLastPluginOutputStringForHost(RTMMHost host) {
        return buildLastPluginOutputStringForHost(host.getCurrentAttempt(), host.getMaxAttempts(),
                host.getScheduledDowntimeDepth(), host.getLastStateChange(), host.getLastPluginOutput(),
                host.getNextCheckTime());
    }

    /**
     * Build last plugin output string.
     *
     * @param currentAttempt
     * @param maxAttempts,
     * @param scheduledDowntimeDepth
     * @param lastStateChange
     * @param lastPluginOutput
     * @param nextCheckTime
     * @return last plugin output string
     */
    private static final String buildLastPluginOutputStringForHost(Long currentAttempt, Long maxAttempts,
                                                                   Integer scheduledDowntimeDepth, Date lastStateChange,
                                                                   String lastPluginOutput, Date nextCheckTime) {
        StringBuffer output = new StringBuffer();
        String delimiter = "^^^";
        DateFormat date = new SimpleDateFormat(DATETIME_FORMAT_US);
        if (currentAttempt != null) {
            output.append(currentAttempt.longValue());
        } else {
            output.append("-1");
        }
        output.append(delimiter);
        if (maxAttempts != null) {
            output.append(maxAttempts.longValue());
        } else {
            output.append("-1");
        }
        output.append(delimiter);
        if (scheduledDowntimeDepth != null) {
            output.append(scheduledDowntimeDepth.intValue());
        } else {
            output.append("-1");
        }
        output.append(delimiter);
        if (lastStateChange != null) {
            output.append(date.format(lastStateChange));
        } else {
            output.append("01/01/1970 12:00:00 AM");
        }
        output.append(delimiter);
        if (lastPluginOutput != null) {
            output.append(lastPluginOutput);
        } else {
            output.append("NA");
        }
        output.append(delimiter);
        if (nextCheckTime != null) {
            output.append(date.format(nextCheckTime));
        } else {
            output.append("NA");
        }
        return output.toString();
    }
}
