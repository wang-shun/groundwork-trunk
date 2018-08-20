package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="services")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoServiceList {

    @XmlElement(name="service")
    @JsonProperty("services")
    private List<DtoService> services = new ArrayList<DtoService>();

    public DtoServiceList() {}
    public DtoServiceList(List<DtoService> services) {this.services = services;}

    public List<DtoService> getServices() {
        return services;
    }

    public void add(DtoService service) {
        services.add(service);
    }

    public int size() {
        return services.size();
    }

}
