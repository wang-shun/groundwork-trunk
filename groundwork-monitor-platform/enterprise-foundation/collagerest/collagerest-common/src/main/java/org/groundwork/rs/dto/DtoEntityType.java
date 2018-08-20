package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entityType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoEntityType {

    // Shallow Attributes
    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    @JsonProperty("logicalEntity")
    private Boolean isLogicalEntity;

    @XmlAttribute
    private Boolean applicationTypeSupported;

    // TODO: this seems to be deprecated in main model
//    @XmlElement(name="applicationType")
//    private DtoApplicationType applicationType;

    // TODO: unravel this
    //private Map propertyTypes;

    public DtoEntityType() {
    }

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

    public Boolean isLogicalEntity() {
        return isLogicalEntity;
    }

    public void setLogicalEntity(Boolean logicalEntity) {
        isLogicalEntity = logicalEntity;
    }

    public Boolean isApplicationTypeSupported() {
        return applicationTypeSupported;
    }

    public void setApplicationTypeSupported(Boolean applicationTypeSupported) {
        this.applicationTypeSupported = applicationTypeSupported;
    }

}
