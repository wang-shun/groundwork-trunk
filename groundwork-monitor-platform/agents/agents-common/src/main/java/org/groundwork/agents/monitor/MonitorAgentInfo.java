package org.groundwork.agents.monitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;

public class MonitorAgentInfo {

    public final static int DEFAULT_MONITOR_SLEEP_MS = 5000;
    public final static int DEFAULT_ERROR_THRESHOLD = 20;
    protected String connectorName;
    protected String managementServerName;
    protected String applicationType;
    protected String name;
    protected VirtualSystem virtualSystem;
    protected Deque<ErrorInfo> errors = new ArrayDeque<ErrorInfo>(DEFAULT_ERROR_THRESHOLD);
    protected int connectionRetries = 10;
    protected String agentId;

    protected long msAgentSleep = DEFAULT_MONITOR_SLEEP_MS;

    public MonitorAgentInfo(String connectorName, String managementServerName,
                            String applicationType, VirtualSystem virtualSystem, int connectionRetries, String agentId) {
        this.connectorName = connectorName;
        this.managementServerName = managementServerName;
        this.applicationType = applicationType;
        this.virtualSystem = virtualSystem;
        this.connectionRetries = connectionRetries;
        this.agentId = agentId;
    }

    public VirtualSystem getVirtualSystem() {
        return virtualSystem;
    }

    public void setVirtualSystem(VirtualSystem virtualSystem) {
        this.virtualSystem = virtualSystem;
    }


    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getManagementServerName() {
        return managementServerName;
    }

    public void setManagementServerName(String managementServerName) {
        this.managementServerName = managementServerName;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMsAgentSleep() {
        return msAgentSleep;
    }

    public void setMsAgentSleep(long msAgentSleep) {
        this.msAgentSleep = msAgentSleep;
    }

    public int getConnectionRetries() {
        return connectionRetries;
    }

    public void setConnectionRetries(int connectionRetries) {
        this.connectionRetries = connectionRetries;
    }

    public String toString() {
        return String.format("agent: %s : connector: %s, mgmtServer: %s, appType: %s",
                name, connectorName, managementServerName, applicationType);
    }

    public ErrorInfo getLastError() {
        return errors.peek();
    }

    public List<ErrorInfo> getAllErrors() {
        List<ErrorInfo> list = new ArrayList<ErrorInfo>(errors.size());
        list.addAll(errors);
        return list;
    }

    public void addError(String error) {
        if (errors.size() >= DEFAULT_ERROR_THRESHOLD)
            errors.removeLast();
        errors.push(new ErrorInfo(error));
    }

    public void clearErrors() {
        errors.clear();
    }
    public int getErrorCount() {
        return errors.size();
    }

    public class ErrorInfo {
        private final String message;
        private final Date timestamp;

        public ErrorInfo(String message) {
            this.message = message;
            this.timestamp = new Date();
        }

        public String getMessage() {
            return message;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
