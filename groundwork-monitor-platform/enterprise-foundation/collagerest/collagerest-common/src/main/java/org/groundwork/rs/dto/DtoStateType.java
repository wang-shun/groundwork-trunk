package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stateType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoStateType {

    private Integer stateTypeId;

    private String name;

    private String description;

    public DtoStateType() {}

    public Integer getStateTypeId() {
        return stateTypeId;
    }

    public void setStateTypeId(Integer stateTypeId) {
        this.stateTypeId = stateTypeId;
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
