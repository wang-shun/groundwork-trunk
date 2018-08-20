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
public class DtoServiceGroupMemberUpdate {

    @XmlAttribute
    private String name;

    // Deep Attributes
    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private List<DtoServiceKey> services;

    public DtoServiceGroupMemberUpdate() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

}
