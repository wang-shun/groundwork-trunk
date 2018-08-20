package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)

public class DtoBizService extends DtoPropertiesBase {

    @XmlAttribute
    protected String host;

    @XmlAttribute
    protected String service;

    @XmlAttribute
    protected String status;

    @XmlAttribute
    protected String message;

    @XmlAttribute
    protected String serviceGroup;

    @XmlAttribute
    protected String serviceCategory;

    @XmlAttribute
    protected String hostGroup;

    @XmlAttribute
    protected String hostCategory;

    @XmlAttribute
    protected String device;

    @XmlAttribute
    protected String appType;

    @XmlAttribute
    protected String agentId;

    @XmlAttribute
    protected String serviceValue;

    @XmlAttribute
    protected String metricType;

    @XmlAttribute
    protected long warningLevel = -1;

    @XmlAttribute
    protected long criticalLevel = -1;

    @XmlAttribute
    protected int checkIntervalMinutes = 5;

    @XmlAttribute
    protected boolean allowInserts = true;

    @XmlAttribute
    protected boolean mergeHosts = true;

    @XmlAttribute
    protected Boolean setStatusOnCreate;

    public DtoBizService() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHostGroup() {
        return hostGroup;
    }

    public void setHostGroup(String hostGroup) {
        this.hostGroup = hostGroup;
    }

    public String getHostCategory() {
        return hostCategory;
    }

    public void setHostCategory(String hostCategory) {
        this.hostCategory = hostCategory;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
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

    public String getServiceValue() {
        return serviceValue;
    }

    public void setServiceValue(String serviceValue) {
        this.serviceValue = serviceValue;
    }

    public long getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(long warningLevel) {
        this.warningLevel = warningLevel;
    }

    public long getCriticalLevel() {
        return criticalLevel;
    }

    public void setCriticalLevel(long criticalLevel) {
        this.criticalLevel = criticalLevel;
    }

    public int getCheckIntervalMinutes() {
        return checkIntervalMinutes;
    }

    public void setCheckIntervalMinutes(int checkIntervalMinutes) {
        this.checkIntervalMinutes = checkIntervalMinutes;
    }

    public boolean isAllowInserts() {
        return allowInserts;
    }

    public void setAllowInserts(boolean allowInserts) {
        this.allowInserts = allowInserts;
    }

    public boolean isMergeHosts() {
        return mergeHosts;
    }

    public void setMergeHosts(boolean mergeHosts) {
        this.mergeHosts = mergeHosts;
    }

    public boolean isSetStatusOnCreate() {
        return ((setStatusOnCreate != null) && setStatusOnCreate);
    }

    public void setSetStatusOnCreate(boolean setStatusOnCreate) {
        this.setStatusOnCreate = (setStatusOnCreate ? true : null);
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }
}
