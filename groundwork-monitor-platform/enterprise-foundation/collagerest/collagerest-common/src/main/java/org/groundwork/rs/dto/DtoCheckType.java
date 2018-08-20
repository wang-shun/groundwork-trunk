package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "checkType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCheckType {

    @XmlAttribute
    private Integer checkTypeId;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    public DtoCheckType() {}

    public Integer getCheckTypeId() {
        return checkTypeId;
    }

    public void setCheckTypeId(Integer checkTypeId) {
        this.checkTypeId = checkTypeId;
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
