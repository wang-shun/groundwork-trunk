package com.groundwork.dashboard.configuration;

public class DashboardConfigurationException extends RuntimeException {
    private String additional;

    public DashboardConfigurationException() {
        super();
    }

    public DashboardConfigurationException(String msg) {
        super(msg);
    }

    public DashboardConfigurationException(Throwable nested) {
        super(nested);
    }

    public DashboardConfigurationException(String msg, Throwable nested) {
        super(msg, nested);
    }

    public DashboardConfigurationException(String msg, String additional, Throwable nested) {
        super(msg, nested);
        this.additional = additional;
    }

    public DashboardConfigurationException(String msg, String additional) {
        super(msg);
        this.additional = additional;
    }

    public String getAdditional() {
        return additional;
    }

}
