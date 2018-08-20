package com.groundwork.collage;

/**
 * Common statuses, but not all. See monitorstatus table for all possible values
 */
public enum CollageStatus {
    UP("UP"),
    UNSCHEDULED_DOWN("UNSCHEDULED DOWN"),
    WARNING("WARNING"),
    UNREACHABLE("UNREACHABLE"),
    SCHEDULED_DOWN("SCHEDULED DOWN"),
    PENDING("PENDING"),
    DOWN("DOWN"),
    OK("OK"),
    SUSPENDED("SUSPENDED"),
    UNKNOWN("UNKNOWN");

    final public String status;

    CollageStatus(String status) {
        this.status = status;
    }
}
