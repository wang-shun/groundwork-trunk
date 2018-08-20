package org.groundwork.connectors.solarwinds.status;

public enum MonitorStatus {

    UNKNOWN,
    UP,
    OK,
    DOWN,
    WARNING,
    CRITICAL,
    PENDING,
    UNREACHABLE,
    MAINTENANCE,
    SCHEDULED_DOWN("SCHEDULED DOWN"),
    UNSCHEDULED_DOWN("UNSCHEDULED DOWN"),
    SCHEDULED_CRITICAL("SCHEDULED CRITICAL"),
    SUSPENDED;

    private String value;

    MonitorStatus() {
        this.value = name();
    }

    MonitorStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}


/*
        "ACKNOWLEDGEMENT (WARNING)"
        "ACKNOWLEDGEMENT (CRITICAL)"
        "ACKNOWLEDGEMENT (DOWN)"
        "ACKNOWLEDGEMENT (UP)"
        "ACKNOWLEDGEMENT (OK)"
        "ACKNOWLEDGEMENT (UNREACHABLE)"
        "ACKNOWLEDGEMENT (UNKNOWN)"
        "ACKNOWLEDGEMENT (PENDING)"
        "ACKNOWLEDGEMENT (MAINTENANCE)"
        "SUSPENDED"

  */
