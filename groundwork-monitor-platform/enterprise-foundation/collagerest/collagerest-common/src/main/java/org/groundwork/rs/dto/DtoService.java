package org.groundwork.rs.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoService extends DtoPropertiesBase {

    @XmlAttribute
    private Integer id;
    @XmlAttribute
    private String appType;
    @XmlAttribute
    private String appTypeDisplayName;
    @XmlAttribute
    private String description;
    @XmlAttribute
    private String monitorStatus;
    @XmlAttribute
    private Date lastCheckTime;
    @XmlAttribute
    private Date nextCheckTime;
    @XmlAttribute
    private Date lastStateChange;
    @XmlAttribute
    private String hostName;
    @XmlAttribute
    private String metricType;
    @XmlAttribute
    private String domain;
    @XmlAttribute
    private String stateType;
    @XmlAttribute
    private String checkType;
    @XmlAttribute
    private String lastHardState;
    @XmlAttribute
    private String agentId;

    // Update only attributes
    @XmlAttribute
    private String monitorServer = "localhost";

    @XmlAttribute
    private String deviceIdentification;

    @XmlAttribute
    private String lastPlugInOutput;

    @XmlElementWrapper(name="comments")
    @XmlElement(name="comment")
    private List<DtoComment> comments;

    public DtoService() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppTypeDisplayName() {
        return appTypeDisplayName;
    }

    public void setAppTypeDisplayName(String appTypeDisplayName) {
        this.appTypeDisplayName = appTypeDisplayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMonitorStatus() {
        return monitorStatus;
    }

    public void setMonitorStatus(String monitorStatus) {
        this.monitorStatus = monitorStatus;
    }

    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public Date getNextCheckTime() {
        return nextCheckTime;
    }

    public void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStateType() {
        return stateType;
    }

    public void setStateType(String stateType) {
        this.stateType = stateType;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getLastHardState() {
        return lastHardState;
    }

    public void setLastHardState(String lastHardState) {
        this.lastHardState = lastHardState;
    }

    // Update accessors
    public String getMonitorServer() {
        return monitorServer;
    }

    public void setMonitorServer(String monitorServer) {
        this.monitorServer = monitorServer;
    }

    public String getDeviceIdentification() {
        return deviceIdentification;
    }

    public void setDeviceIdentification(String deviceIdentification) {
        this.deviceIdentification = deviceIdentification;
    }

    public String getLastPlugInOutput() {
        return lastPlugInOutput;
    }

    public void setLastPlugInOutput(String lastPlugInOutput) {
        this.lastPlugInOutput = lastPlugInOutput;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public List<DtoComment> getComments() {
        return comments;
    }

    public void setComments(List<DtoComment> comments) {
        this.comments = comments;
    }

    public void addComment(DtoComment comment) {
        if (getComments() == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
    }

}
