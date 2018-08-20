package org.groundwork.rs.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "statistic")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStateStatistic {

    @XmlAttribute
    private Long totalHosts;
    @XmlAttribute
    private Long totalServices;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private Double availability;
    @XmlAttribute
    private String bubbleUpStatus;

    @XmlElementWrapper(name="properties")
    @XmlElement(name="property")
    private List<DtoStatistic> properties;

    public DtoStateStatistic() {}

    public Long getTotalHosts() {
        return totalHosts;
    }

    public void setTotalHosts(Long totalHosts) {
        this.totalHosts = totalHosts;
    }

    public Long getTotalServices() {
        return totalServices;
    }

    public void setTotalServices(Long totalServices) {
        this.totalServices = totalServices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAvailability() {
        return availability;
    }

    public void setAvailability(Double availability) {
        this.availability = availability;
    }

    public String getBubbleUpStatus() {
        return bubbleUpStatus;
    }

    public void setBubbleUpStatus(String bubbleUpStatus) {
        this.bubbleUpStatus = bubbleUpStatus;
    }

    public List<DtoStatistic> getProperties() {
        return properties;
    }

    public void setProperties(List<DtoStatistic> properties) {
        this.properties = properties;
    }

    public void addProperty(DtoStatistic statistic) {
        if (getProperties() == null) {
            properties = new ArrayList<DtoStatistic>();
        }
        properties.add(statistic);
    }

}
