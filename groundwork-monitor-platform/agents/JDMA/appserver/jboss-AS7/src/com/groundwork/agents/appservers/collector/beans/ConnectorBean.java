package com.groundwork.agents.appservers.collector.beans;

public class ConnectorBean {

    private String hostName="localhost";
    private String port = "4447"; // 9999 for secure via management server
    private String username;
    private String password;
    private String nagiosHostname =null;
    private String nagiosPort ="5667";
    private String nagiosEncryption= "1";
    private String nagiosPassword = null;
    private String passiveCheckInterval = "600";
    private String objectNameFilter = "*:*";
    private String instanceId=null;

    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getNagiosHostname() {
        return nagiosHostname;
    }
    public void setNagiosHostname(String nagiosHostname) {
        this.nagiosHostname = nagiosHostname;
    }
    public String getNagiosPort() {
        return nagiosPort;
    }
    public void setNagiosPort(String nagiosPort) {
        this.nagiosPort = nagiosPort;
    }
    public String getNagiosEncryption() {
        return nagiosEncryption;
    }
    public void setNagiosEncryption(String nagiosEncryption) {
        this.nagiosEncryption = nagiosEncryption;
    }
    public String getNagiosPassword() {
        return nagiosPassword;
    }
    public void setNagiosPassword(String nagiosPassword) {
        this.nagiosPassword = nagiosPassword;
    }
    public String getPassiveCheckInterval() {
        return passiveCheckInterval;
    }
    public void setPassiveCheckInterval(String passiveCheckInterval) {
        this.passiveCheckInterval = passiveCheckInterval;
    }
    public String getObjectNameFilter() {
        return objectNameFilter;
    }
    public void setObjectNameFilter(String objectNameFilter) {
        this.objectNameFilter = objectNameFilter;
    }

    public String getInstanceId() {
        return instanceId;
    }
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

}
