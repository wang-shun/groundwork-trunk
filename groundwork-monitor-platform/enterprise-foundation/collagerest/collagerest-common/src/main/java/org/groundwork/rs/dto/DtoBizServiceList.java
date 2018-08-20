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
public class DtoBizServiceList {

    @XmlElement(name="service")
    @JsonProperty("services")
    private List<DtoBizService> services = new ArrayList<DtoBizService>();

    public DtoBizServiceList() {}
    public DtoBizServiceList(List<DtoBizService> services) {this.services = services;}

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
