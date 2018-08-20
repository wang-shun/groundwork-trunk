package org.groundwork.rs.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "hostGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostGroup {

    // Base Attributes
    @XmlAttribute
    protected Integer id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String alias;

    @XmlAttribute
    private String appType;

    @XmlAttribute
    private String appTypeDisplayName;

    @XmlAttribute
    private String agentId;

    // Deep Attributes
    @XmlElementWrapper(name="hosts")
    @XmlElement(name="host")
    private List<DtoHost> hosts;

    @XmlElement(name="applicationType")
    private DtoApplicationType applicationType;

    @XmlElementWrapper(name="statistics")
    @XmlElement(name="statistic")
    private List<DtoStatistic> statistics;

    @XmlAttribute
    private String bubbleUpStatus;

    public DtoHostGroup() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public List<DtoHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<DtoHost> hosts) {
        this.hosts = hosts;
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

    public void addHost(DtoHost host) {
        if (getHosts() == null) {
            hosts = new ArrayList<>();
        }
        hosts.add(host);
    }

    public void addStatistic(DtoStatistic statistic) {
        if (getStatistics() == null) {
            statistics = new ArrayList<>();
        }
        statistics.add(statistic);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getBubbleUpStatus() {
        return bubbleUpStatus;
    }

    public void setBubbleUpStatus(String bubbleUpStatus) {
        this.bubbleUpStatus = bubbleUpStatus;
    }

}
