package org.groundwork.cloudhub.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.MonitorAgentInfo;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoConnectorStatus {

    // identification
    private String name;
    private String agentId;
    private String connectorType;
    private String applicationType;
    private String displayName;

    // monitorState
    private Boolean isSuspended;
    private ConnectionState connectionState;

    // Groundwork server
    private String groundworkServer;
    private Boolean isMergeHosts;
    private Integer checkIntervalMinutes;
    private Integer connectionRetries;
    private String monitorServer;

    // Errors
    private List<MonitorAgentInfo.ErrorInfo> errors;
    private MonitorAgentInfo.ErrorInfo lastError;
    private Integer groundworkExceptionCount;
    private Integer monitorExceptionCount;


    public DtoConnectorStatus() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public Boolean getSuspended() {
        return isSuspended;
    }

    public void setSuspended(Boolean suspended) {
        isSuspended = suspended;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public String getGroundworkServer() {
        return groundworkServer;
    }

    public void setGroundworkServer(String groundworkServer) {
        this.groundworkServer = groundworkServer;
    }

    public Boolean getMergeHosts() {
        return isMergeHosts;
    }

    public void setMergeHosts(Boolean mergeHosts) {
        isMergeHosts = mergeHosts;
    }

    public Integer getCheckIntervalMinutes() {
        return checkIntervalMinutes;
    }

    public void setCheckIntervalMinutes(Integer checkIntervalMinutes) {
        this.checkIntervalMinutes = checkIntervalMinutes;
    }

    public Integer getConnectionRetries() {
        return connectionRetries;
    }

    public void setConnectionRetries(Integer connectionRetries) {
        this.connectionRetries = connectionRetries;
    }

    public String getMonitorServer() {
        return monitorServer;
    }

    public void setMonitorServer(String monitorServer) {
        this.monitorServer = monitorServer;
    }

    public List<MonitorAgentInfo.ErrorInfo> getErrors() {
        return errors;
    }

    public void setErrors(List<MonitorAgentInfo.ErrorInfo> errors) {
        this.errors = errors;
    }

    public MonitorAgentInfo.ErrorInfo getLastError() {
        return lastError;
    }

    public void setLastError(MonitorAgentInfo.ErrorInfo lastError) {
        this.lastError = lastError;
    }

    public Integer getGroundworkExceptionCount() {
        return groundworkExceptionCount;
    }

    public void setGroundworkExceptionCount(Integer groundworkExceptionCount) {
        this.groundworkExceptionCount = groundworkExceptionCount;
    }

    public Integer getMonitorExceptionCount() {
        return monitorExceptionCount;
    }

    public void setMonitorExceptionCount(Integer monitorExceptionCount) {
        this.monitorExceptionCount = monitorExceptionCount;
    }

    // builders
    public DtoConnectorStatus name(String name) {
        this.name = name;
        return this;
    }
    public DtoConnectorStatus agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }
    public DtoConnectorStatus connectorType(String connectorType) {
        this.connectorType = connectorType;
        return this;
    }
    public DtoConnectorStatus applicationType(String applicationType) {
        this.applicationType = applicationType;
        return this;
    }
    public DtoConnectorStatus isSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
        return this;
    }
    public DtoConnectorStatus connectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        return this;
    }
    public DtoConnectorStatus groundworkServer(String groundworkServer) {
        this.groundworkServer = groundworkServer;
        return this;
    }
    public DtoConnectorStatus mergeHosts(Boolean mergeHosts) {
        isMergeHosts = mergeHosts;
        return this;
    }
    public DtoConnectorStatus checkIntervalMinutes(Integer checkIntervalMinutes) {
        this.checkIntervalMinutes = checkIntervalMinutes;
        return this;
    }
    public DtoConnectorStatus connectionRetries(Integer connectionRetries) {
        this.connectionRetries = connectionRetries;
        return this;
    }
    public DtoConnectorStatus monitorServer(String monitorServer) {
        this.monitorServer = monitorServer;
        return this;
    }
    public DtoConnectorStatus errors(List<MonitorAgentInfo.ErrorInfo> errors) {
        this.errors = errors;
        return this;
    }
    public DtoConnectorStatus lastError(MonitorAgentInfo.ErrorInfo lastError) {
        this.lastError = lastError;
        return this;
    }
    public DtoConnectorStatus groundworkExceptionCount(Integer groundworkExceptionCount) {
        this.groundworkExceptionCount = groundworkExceptionCount;
        return this;
    }
    public DtoConnectorStatus monitorExceptionCount(Integer monitorExceptionCount) {
        this.monitorExceptionCount = monitorExceptionCount;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public DtoConnectorStatus displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }
}
