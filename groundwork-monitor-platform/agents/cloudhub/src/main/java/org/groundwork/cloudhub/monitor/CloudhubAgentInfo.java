package org.groundwork.cloudhub.monitor;

import org.groundwork.agents.monitor.MonitorAgentInfo;
import org.groundwork.agents.monitor.VirtualSystem;

public class CloudhubAgentInfo extends MonitorAgentInfo {

    private String hyperVisorName;
    private String configurationPath;
    private String cloudhubMonitorAgentBeanName;

    public CloudhubAgentInfo(String hyperVisorName, String cloudhubMonitorAgentBeanName, String connectorName,
                             String managementServerName, String applicationType, VirtualSystem virtualSystem,
                             int connectionRetries, String agentId) {
        super(connectorName, managementServerName, applicationType, virtualSystem, connectionRetries, agentId);
        this.hyperVisorName = hyperVisorName;
        this.cloudhubMonitorAgentBeanName = cloudhubMonitorAgentBeanName;
    }

    public String getHyperVisorName() {
        return hyperVisorName;
    }

    public void setHyperVisorName(String hyperVisorName) {
        this.hyperVisorName = hyperVisorName;
    }

    public String getCloudhubMonitorAgentBeanName() {
        return cloudhubMonitorAgentBeanName;
    }

    public void setCloudhubMonitorAgentBeanName(String cloudhubMonitorAgentBeanName) {
        this.cloudhubMonitorAgentBeanName = cloudhubMonitorAgentBeanName;
    }

    public String getConfigurationPath() {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public String toString() {
        return String.format("agent: %s : hypervisor: %s, connector: %s, mgmtServer: %s, appType: %s, agentId: %s",
                name, hyperVisorName, connectorName, managementServerName, applicationType, agentId);
    }
}
