package org.groundwork.rs.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@XmlRootElement(name = "host")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHost extends DtoPropertiesBase {

    // Base Fields
    @XmlAttribute
    protected Integer id;

    @XmlAttribute
    protected String hostName;

    @XmlAttribute
    protected String description;

    @XmlAttribute
    protected String monitorStatus;

    @XmlAttribute
    protected String appType;

    @XmlAttribute
    protected String appTypeDisplayName;

    @XmlAttribute
    protected String deviceIdentification;

    @XmlAttribute
    private String deviceDisplayName;

    @XmlAttribute
    protected Date lastCheckTime;

    @XmlAttribute
    protected Date nextCheckTime;

    @XmlAttribute
    protected String bubbleUpStatus;

    @XmlAttribute
    protected String serviceAvailability;

    @XmlAttribute
    protected Boolean acknowledged = false;

    @XmlAttribute
    protected String agentId;

    // Deep Attributes
    @XmlElement(name="device")
    private DtoDevice device;

    @XmlElement(name="hostStatus")
    private DtoHostStatus hostStatus;

    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private List<DtoService> services;

    @XmlAttribute
    private Integer serviceCount = 0;

    @XmlElementWrapper(name="hostGroups")
    @XmlElement(name="hostGroup")
    private List<DtoHostGroup> hostGroups;

    @XmlElement(name="applicationType")
    private DtoApplicationType applicationType;

    @XmlElementWrapper(name="statistics")
    @XmlElement(name="statistic")
    private List<DtoStatistic> statistics;

    @XmlElementWrapper(name="comments")
    @XmlElement(name="comment")
    private List<DtoComment> comments;

    // Update Attributes
    @XmlAttribute
    private String monitorServer;
    @XmlAttribute
    private String stateType;
    @XmlAttribute
    private String checkType;

    @XmlAttribute
    private String alias;
    @XmlAttribute
    private Date lastStateChange;
    @XmlAttribute
    private String lastPlugInOutput;

    public DtoHost() {
        super();
    }

    public DtoHost(String hostName) {
        super();
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
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

    public void setMonitorStatus(String lastMonitorStatus) {
        this.monitorStatus = lastMonitorStatus;
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

    public String toString() {
        return String.format("Host: %s - %s", hostName, description);
    }

    public String getDeviceIdentification() {
        return deviceIdentification;
    }

    public void setDeviceIdentification(String deviceIdentification) {
        this.deviceIdentification = deviceIdentification;
    }

    public String getDeviceDisplayName() {
        return deviceDisplayName;
    }

    public void setDeviceDisplayName(String deviceDisplayName) {
        this.deviceDisplayName = deviceDisplayName;
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

    public String getBubbleUpStatus() {
        return bubbleUpStatus;
    }

    public void setBubbleUpStatus(String bubbleUpStatus) {
        this.bubbleUpStatus = bubbleUpStatus;
    }

    public String getServiceAvailability() {
        return serviceAvailability;
    }

    public void setServiceAvailability(String serviceAvailability) {
        this.serviceAvailability = serviceAvailability;
    }

    public Boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Deep accessors
    public DtoDevice getDevice() {
        return device;
    }

    public void setDevice(DtoDevice device) {
        this.device = device;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }

    public List<DtoService> getServices() {
        return services;
    }

    public void setServiceStatuses(List<DtoService> services) {
        this.services = services;
    }

    public DtoHostStatus getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(DtoHostStatus hostStatus) {
        this.hostStatus = hostStatus;
    }

    public List<DtoHostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<DtoHostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public DtoApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(DtoApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public List<DtoStatistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<DtoStatistic> statistics) {
        this.statistics = statistics;
    }

    public List<DtoComment> getComments() {
        return comments;
    }

    public void setComments(List<DtoComment> comments) {
        this.comments = comments;
    }

    // Update accessors
    public String getMonitorServer() {
        return monitorServer;
    }

    public void setMonitorServer(String monitorServer) {
        this.monitorServer = monitorServer;
    }

    public void addHostGroup(DtoHostGroup group) {
        if (getHostGroups() == null) {
            hostGroups = new ArrayList<DtoHostGroup>();
        }
        hostGroups.add(group);
    }

    public void addService(DtoService service) {
        if (getServices() == null) {
            services = new ArrayList<DtoService>();
        }
        services.add(service);
    }

    public void addStatistic(DtoStatistic statistic) {
        if (getStatistics() == null) {
            statistics = new ArrayList<DtoStatistic>();
        }
        statistics.add(statistic);
    }

    public void addComment(DtoComment comment) {
        if (getComments() == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    public String getLastPlugInOutput() {
        return lastPlugInOutput;
    }

    public void setLastPlugInOutput(String lastPlugInOutput) {
        this.lastPlugInOutput = lastPlugInOutput;
    }
}
