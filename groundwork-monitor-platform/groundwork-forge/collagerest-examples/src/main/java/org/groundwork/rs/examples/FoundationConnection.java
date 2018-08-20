package org.groundwork.rs.examples;

public class FoundationConnection {

    private final String deploymentUrl;
    private final boolean enableAsserts;

    public FoundationConnection(String deploymentUrl, boolean enableAsserts) {
        this.deploymentUrl = deploymentUrl;
        this.enableAsserts = enableAsserts;
    }

    public String getDeploymentUrl() {
        return deploymentUrl;
    }

    public boolean isEnableAsserts() {
        return enableAsserts;
    }
}
