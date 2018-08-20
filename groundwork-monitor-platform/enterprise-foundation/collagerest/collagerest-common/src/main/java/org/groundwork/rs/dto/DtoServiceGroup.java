package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "serviceGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceGroup {
    // Base Attributes
    @XmlAttribute
    protected Integer id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String appType;

    @XmlAttribute
    private String appTypeDisplayName;

    @XmlAttribute
    private String agentId;

    // Deep Attributes
    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private List<DtoService> services;

    @XmlAttribute
    private String bubbleUpStatus;

    public DtoServiceGroup() {
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

    public List<DtoService> getServices() {
        return services;
    }

    public void setServices(List<DtoService> services) {
        this.services = services;
    }

    public void addService(DtoService service) {
        if (getServices() == null) {
            services = new ArrayList<>();
        }
        services.add(service);
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
