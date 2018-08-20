package org.groundwork.cloudhub.gwos.messages;

import com.groundwork.collage.CollageSeverity;

/**
 * Created by dtaylor on 6/3/15.
 */
public class SuspendedStatusMessages implements UpdateStatusMessages {

    protected final static String SUSPENDED_HYPERVISOR_HOST_MESSAGE = "CloudHub connector for this host has been stopped by the administrator";
    protected final static String SUSPENDED_VM_HOST_MESSAGE = "CloudHub connector for this host has been stopped by the administrator";
    protected final static String SUSPENDED_MONITOR_HOST_MESSAGE = "Monitor host has been stopped by administrator";
    protected final static String SUSPENDED_HYPERVISOR_SERVICE_MESSAGE = "CloudHub connector for this service has been stopped by administrator";
    protected final static String SUSPENDED_VM_SERVICE_MESSAGE = "CloudHub connector for this service has been stopped by administrator";
    protected final static String SUSPENDED_MONITOR_SERVICE_MESSAGE = "Monitor service has been suspended by administrator";
    protected final static String SUSPENDED_COMMENT = "Cloudhub Host has been stopped by administrator";
    protected final static String SUSPENDED_MONITOR_COMMENT = "Cloudhub monitor host has been stopped by administrator";
    protected final static String NOTIFICATION_TYPE_PROBLEM = "PROBLEM";
    protected final static  String OPERATIONAL_STATUS_OPEN = "OPEN";

    @Override
    public String getHostHypervisorMessage() {
        return SUSPENDED_HYPERVISOR_HOST_MESSAGE;
    }

    @Override
    public String getHostVmMessage() {
        return SUSPENDED_VM_HOST_MESSAGE;
    }

    @Override
    public String getHostMonitorMessage() {
        return SUSPENDED_MONITOR_HOST_MESSAGE;
    }

    @Override
    public String getServiceHypervisorMessage() {
        return SUSPENDED_HYPERVISOR_SERVICE_MESSAGE;
    }

    @Override
    public String getServiceVmMessage() {
        return SUSPENDED_VM_SERVICE_MESSAGE;
    }

    @Override
    public String getServiceMonitorMessage() {
        return SUSPENDED_MONITOR_SERVICE_MESSAGE;
    }

    @Override
    public String getComment() {
        return SUSPENDED_COMMENT;
    }

    @Override
    public String getMonitorComment() {
        return SUSPENDED_MONITOR_COMMENT;
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
        return CollageSeverity.LOW.name();
    }
}
