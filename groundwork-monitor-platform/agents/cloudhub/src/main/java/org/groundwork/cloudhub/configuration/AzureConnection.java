package org.groundwork.cloudhub.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "azure")
@XmlType(propOrder = { "server", "username", "password", "sslEnabled", "credentialsFile", "subscription", "timeoutMs", "enableResourceGroups" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private String credentialsFile;
    private String subscription = "groundwork-dev";
    private Long timeoutMs = 5000L;
    private Boolean enableResourceGroups = false;

    public AzureConnection() {
        setSslEnabled(false);
        setEnableResourceGroups(false);
        setServer("");
        setUsername("");
        setPassword("");
    }

    public String getCredentialsFile() {
        return credentialsFile;
    }

    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public Boolean getEnableResourceGroups() {
        return enableResourceGroups;
    }

    public void setEnableResourceGroups(Boolean enableResourceGroups) {
        this.enableResourceGroups = enableResourceGroups;
    }
}
