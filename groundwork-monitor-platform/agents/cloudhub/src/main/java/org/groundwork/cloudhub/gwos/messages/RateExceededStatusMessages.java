package org.groundwork.cloudhub.gwos.messages;

import com.groundwork.collage.CollageSeverity;

/**
 * Created by dtaylor on 6/3/15.
 */
public class RateExceededStatusMessages implements UpdateStatusMessages {

    protected final static String GENERIC_RATE_EXCEEDED_MESSAGE = "CloudHub Connector metric sampling rate exceeded";
    protected final static String RATE_EXCEEDED_HYPERVISOR_HOST_MESSAGE = GENERIC_RATE_EXCEEDED_MESSAGE;
    protected final static String RATE_EXCEEDED_VM_HOST_MESSAGE = GENERIC_RATE_EXCEEDED_MESSAGE;
    protected final static String RATE_EXCEEDED_MONITOR_HOST_MESSAGE = GENERIC_RATE_EXCEEDED_MESSAGE;
    protected final static String RATE_EXCEEDED_HYPERVISOR_SERVICE_MESSAGE = GENERIC_RATE_EXCEEDED_MESSAGE;
    protected final static String RATE_EXCEEDED_VM_SERVICE_MESSAGE = GENERIC_RATE_EXCEEDED_MESSAGE;
    protected final static String RATE_EXCEEDED_MONITOR_SERVICE_MESSAGE = GENERIC_RATE_EXCEEDED_MESSAGE;
    protected final static String RATE_EXCEEDED_COMMENT = GENERIC_RATE_EXCEEDED_MESSAGE;
    protected final static String RATE_EXCEEDED_MONITOR_COMMENT = "Cloudhub monitor rate exceeded";
    protected final static String NOTIFICATION_TYPE_PROBLEM = "PROBLEM";
    protected final static  String OPERATIONAL_STATUS_OPEN = "OPEN";

    @Override
    public String getHostHypervisorMessage() {
        return RATE_EXCEEDED_HYPERVISOR_HOST_MESSAGE;
    }

    @Override
    public String getHostVmMessage() {
        return RATE_EXCEEDED_VM_HOST_MESSAGE;
    }

    @Override
    public String getHostMonitorMessage() {
        return RATE_EXCEEDED_MONITOR_HOST_MESSAGE;
    }

    @Override
    public String getServiceHypervisorMessage() {
        return RATE_EXCEEDED_HYPERVISOR_SERVICE_MESSAGE;
    }

    @Override
    public String getServiceVmMessage() {
        return RATE_EXCEEDED_VM_SERVICE_MESSAGE;
    }

    @Override
    public String getServiceMonitorMessage() {
        return RATE_EXCEEDED_MONITOR_SERVICE_MESSAGE;
    }

    @Override
    public String getComment() {
        return RATE_EXCEEDED_COMMENT;
    }

    @Override
    public String getMonitorComment() {
        return RATE_EXCEEDED_MONITOR_COMMENT;
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
