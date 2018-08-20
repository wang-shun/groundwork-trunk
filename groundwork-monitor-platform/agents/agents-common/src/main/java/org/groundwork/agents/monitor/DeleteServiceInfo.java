package org.groundwork.agents.monitor;

/**
 * Created by dtaylor on 7/5/16.
 */
public class DeleteServiceInfo {

    public enum OperationType {
        none,
        prefixWildcard
    }

    private String name;
    private OperationType operationType;
    private String prefix;
    private String serviceType;

    public DeleteServiceInfo(String serviceName) {
        this.name = serviceName;
        this.operationType = operationType.none;
    }

    public DeleteServiceInfo(String serviceName, String serviceType) {
        this.name = serviceName;
        this.serviceType = serviceType;
        this.operationType = operationType.none;
    }

    public DeleteServiceInfo(String service, OperationType operationType, String prefix) {
        this.name = service;
        this.operationType = operationType;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String service) {
        this.name = service;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getServiceType() {
        return serviceType;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (serviceType != null) {
            buffer.append(serviceType);
            buffer.append("-");
        }
        buffer.append(name);
        return buffer.toString();
    }
}
