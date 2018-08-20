package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "propertyType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoPropertyType {

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private DtoPropertyDataType	dataType;

    public DtoPropertyType() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public DtoPropertyDataType getDataType() {
        return dataType;
    }

    public void setDataType(DtoPropertyDataType dataType) {
        this.dataType = dataType;
    }

    public String toString() {
        return String.format("PropertyType: %s - %s - %s", name, dataType, description);
    }

}
