package org.groundwork.cloudhub.gwos;

public class GWOSHost {

    protected String hostName;

    protected String appType;

    protected String agentId;

    public GWOSHost(String hostName, String appType, String agentId) {
        this.hostName = hostName;
        this.appType = appType;
        this.agentId = agentId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
