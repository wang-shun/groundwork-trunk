package org.groundwork.cloudhub.connectors.azure.health;

public class HealthInfo {
    public HealthInfo(String gwosStatus) {
        this.gwosStatus = gwosStatus;
    }

    public HealthInfo(String gwosStatus, String runStateExtra) {
        this.gwosStatus = gwosStatus;
        this.runStateExtra = runStateExtra;
    }

    String gwosStatus;
    String runStateExtra;

    public String getGwosStatus() {
        return gwosStatus;
    }

    public String getRunStateExtra() {
        return runStateExtra;
    }
}
