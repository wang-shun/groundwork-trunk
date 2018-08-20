package org.groundwork.cloudhub.gwos;

/**
 * Common statuses, but not all. See monitorstatus table for all
 */
public enum GwosServiceStatus {
    OK("OK"),
    UNSCHEDULED_CRITICAL("UNSCHEDULED CRITICAL"),
    WARNING("WARNING"),
    UNKNOWN("UNKNOWN"),
    SCHEDULED_CRITICAL("SCHEDULED CRITICAL"),
    PENDING("PENDING"),
    DOWN("DOWN");

    final public String status;

    GwosServiceStatus(String status) {
        this.status = status;
    }
}
