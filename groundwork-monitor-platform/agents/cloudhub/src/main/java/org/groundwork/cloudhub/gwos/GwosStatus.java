package org.groundwork.cloudhub.gwos;

/**
 * Common statuses, but not all. See monitorstatus table for all
 */
public enum GwosStatus {
    UP("UP"),
    UNSCHEDULED_DOWN("UNSCHEDULED DOWN"),
    WARNING("WARNING"),
    UNREACHABLE("UNREACHABLE"),
    SCHEDULED_DOWN("SCHEDULED DOWN"),
    PENDING("PENDING"),
    DOWN("DOWN"),
    SUSPENDED("SUSPENDED");

    final public String status;

    GwosStatus(String status) {
        this.status = status;
    }
}
