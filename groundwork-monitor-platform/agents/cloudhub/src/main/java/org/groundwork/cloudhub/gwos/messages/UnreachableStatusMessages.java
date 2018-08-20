package org.groundwork.cloudhub.gwos.messages;

import com.groundwork.collage.CollageSeverity;

/**
 * Created by dtaylor on 6/3/15.
 */
public class UnreachableStatusMessages implements UpdateStatusMessages {

    protected final static String UNREACHABLE_HYPERVISOR_HOST_MESSAGE = "Host cannot be reached to determine the status";
    protected final static String UNREACHABLE_VM_HOST_MESSAGE = "Host cannot be reached to determine the status";
    protected final static String UNREACHABLE_MONITOR_HOST_MESSAGE = "Host status cannot be determined because monitor is not reachable";
    protected final static String UNREACHABLE_HYPERVISOR_SERVICE_MESSAGE = "Service status cannot be determined because Host is not reachable";
    protected final static String UNREACHABLE_VM_SERVICE_MESSAGE = "Service status cannot be determined because Host is not reachable";
    protected final static String UNREACHABLE_MONITOR_SERVICE_MESSAGE = "Service status cannot be determined because monitor is not reachable";
    protected final static String UNREACHABLE_COMMENT = "Cloudhub Connector Unreachable";
    protected final static String UNREACHABLE_MONITOR_COMMENT = "Cloudhub monitor unreachable";
    protected final static String NOTIFICATION_TYPE_PROBLEM = "PROBLEM";
    protected final static  String OPERATIONAL_STATUS_OPEN = "OPEN";

    @Override
    public String getHostHypervisorMessage() {
        return UNREACHABLE_HYPERVISOR_HOST_MESSAGE;
    }

    @Override
    public String getHostVmMessage() {
        return UNREACHABLE_VM_HOST_MESSAGE;
    }

    @Override
    public String getHostMonitorMessage() {
        return UNREACHABLE_MONITOR_HOST_MESSAGE;
    }

    @Override
    public String getServiceHypervisorMessage() {
        return UNREACHABLE_HYPERVISOR_SERVICE_MESSAGE;
    }

    @Override
    public String getServiceVmMessage() {
        return UNREACHABLE_VM_SERVICE_MESSAGE;
    }

    @Override
    public String getServiceMonitorMessage() {
        return UNREACHABLE_MONITOR_SERVICE_MESSAGE;
    }

    @Override
    public String getComment() {
        return UNREACHABLE_COMMENT;
    }

    @Override
    public String getMonitorComment() {
        return UNREACHABLE_MONITOR_COMMENT;
    }

    @Override
    public String getNotificationType() {
        return NOTIFICATION_TYPE_PROBLEM;
    }

    @Override
    public String getOperationalStatus() {
        return OPERATIONAL_STATUS_OPEN;
    }

    @Override
    public String getSeverity() {
        return CollageSeverity.HIGH.name();
    }

}
