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
public class DtoServiceGroupUpdate {
    // Base Attributes

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String appType;

    @XmlAttribute
    private String agentId;

    // Deep Attributes
    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private List<DtoServiceKey> services;

    public DtoServiceGroupUpdate() {
        super();
    }

    public DtoServiceGroupUpdate(String serviceGroupName) {
        super();
        this.name = serviceGroupName;
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

    public List<DtoServiceKey> getServices() {
        return services;
    }

    public void setServices(List<DtoServiceKey> services) {
        this.services = services;
    }

    public void addService(DtoServiceKey service) {
        if (getServices() == null) {
            services = new ArrayList<DtoServiceKey>();
        }
        services.add(service);
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

}
