package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "monitorStatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorStatus {

    @XmlAttribute
    private Integer monitorStatusId;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    public DtoMonitorStatus() {}

    public DtoMonitorStatus(Integer monitorStatusId, String name, String description) {
        this.monitorStatusId = monitorStatusId;
        this.name = name;
        this.description = description;
    }

    public Integer getMonitorStatusId() {
        return monitorStatusId;
    }

    public void setMonitorStatusId(Integer monitorStatusId) {
        this.monitorStatusId = monitorStatusId;
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
}
