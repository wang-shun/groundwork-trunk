package org.groundwork.connectors.solarwinds.audit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.connectors.solarwinds.status.MonitorProperty;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoService;

import java.util.List;

public class AuditLog {

    protected static Log audit = LogFactory.getLog(AuditLog.class);

    public static final String UNKNOWN = "(unknown)";

    public static void logHost(DtoHost host) {
        StringBuffer hostGroups = new StringBuffer("");
        List<DtoHostGroup> groups = host.getHostGroups();
        if (groups != null && groups.size() > 0) {
            int count = 0;
            for (DtoHostGroup group : host.getHostGroups()) {
                if (count > 0)
                    hostGroups.append(",");
                hostGroups.append(group.getName().trim());
                count++;
            }
        }

        audit.info(String.format(
                "host: %s, status: %s, ip: %s, agent: %s, groups: %s - %s\n\tMessage: %s\n\tPerf: %s",
                host.getHostName(),
                host.getMonitorStatus(),
                host.getDeviceIdentification(),
                host.getAgentId(),
                hostGroups.toString(),
                host.getLastCheckTime().toString(),
                host.getProperty(MonitorProperty.LastPluginOutput.value()),
                host.getProperty(MonitorProperty.PerformanceData.value())
        ));
    }

    public static void logService(DtoService service) {
        audit.info(String.format(
                "service: %s, host: %s, status: %s, ip: %s, agent: %s - %s\n\tMessage: %s\n\tPerf: %s",
                service.getDescription(),
                service.getHostName(),
                service.getMonitorStatus(),
                service.getDeviceIdentification(),
                service.getAgentId(),
                service.getLastCheckTime().toString(),
                service.getProperty(MonitorProperty.LastPluginOutput.value()),
                service.getProperty(MonitorProperty.PerformanceData.value())
        ));
    }

    public static void logHostFailure(DtoHost host, String message) {
        audit.info(String.format("Failed to update host %s: %s\n\t", host.getHostName(), message));
    }

    public static void logServiceFailure(DtoService service, String message) {
        audit.info(String.format("Failed to update service %s-%s: %s\n\t",
                service.getHostName(), service.getDescription(), message));
    }

    public static void logServiceFailure(String host, String service, String message) {
        if (host == null) host = UNKNOWN;
        if (service == null) service = UNKNOWN;
        audit.info(String.format("Failed to update service %s-%s: %s\n\t",
                host, service, message));
    }

    public static void logHostFailure(String host, String message) {
        if (host == null) host = UNKNOWN;
        audit.info(String.format("Failed to update host %s: %s\n\t",
                host, message));
    }

    public static void logPerformanceFailure(String host, String service, String message) {
        audit.info(String.format("Failed to update performance %s-%s: %s\n\t",
                host, service, message));
    }

    public static void logEventFailure(String host, String service, String message) {
        if (service == null) {
            audit.info(String.format("Failed to send service event %s-%s: %s\n\t",
                    host, service, message));
        } else {
            audit.info(String.format("Failed to send host event %s: %s\n\t",
                    host, message));
        }
    }

    public static void logNotificationFailure(String host, String service, String message) {
        if (service == null) {
            audit.info(String.format("Failed to send service notification %s-%s: %s\n\t",
                    host, service, message));
        } else {
            audit.info(String.format("Failed to send host notification %s: %s\n\t",
                    host, message));
        }
    }

    public static void logMessage(String message) {
        audit.info(message);
    }
}

