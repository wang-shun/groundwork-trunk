package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "host")
@XmlAccessorType(XmlAccessType.FIELD)

public class DtoBizHost extends DtoPropertiesBase {

    @XmlAttribute
    protected String host;

    @XmlAttribute
    protected String status;

    @XmlAttribute
    protected String message;

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
    protected int checkIntervalMinutes = 5;

    @XmlAttribute
    protected boolean allowInserts = true;

    @XmlAttribute
    protected boolean mergeHosts = true;

    @XmlAttribute
    protected Boolean setStatusOnCreate;

    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private List<DtoBizService> services = new ArrayList<DtoBizService>();

    public DtoBizHost() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public List<DtoBizService> getServices() {
        return services;
    }

    public void add(DtoBizService service) {
        services.add(service);
    }

    public int size() {
        return services.size();
    }
}
