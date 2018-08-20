package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "applicationType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoApplicationType extends DtoPropertiesBase {

    public static final String DEFAULT_APP_TYPE = "NAGIOS";

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String  name;

    @XmlAttribute
    private String  displayName;

    @XmlAttribute
    private String  description;

    @XmlAttribute
    private String 	stateTransitionCriteria;

    @XmlElementWrapper(name="entityProperties")
    @XmlElement(name="entityProperty")
    private List<DtoEntityProperty> entityProperties;

    @XmlElementWrapper(name="entityTypes")
    @XmlElement(name="entityType")
    private List<DtoEntityType> entityTypes;

    public DtoApplicationType() {}

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStateTransitionCriteria() {
        return stateTransitionCriteria;
    }

    public void setStateTransitionCriteria(String stateTransitionCriteria) {
        this.stateTransitionCriteria = stateTransitionCriteria;
    }

    public List<DtoEntityProperty> getEntityProperties() {
        return entityProperties;
    }

    public void setEntityProperties(List<DtoEntityProperty> entityProperties) {
        this.entityProperties = entityProperties;
    }

    public List<DtoEntityType> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<DtoEntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public void addEntityProperty(DtoEntityProperty entityProperty) {
        if (getEntityProperties() == null) {
            entityProperties = new ArrayList<DtoEntityProperty>();
        }
        entityProperties.add(entityProperty);
    }

    public void addEntityType(DtoEntityType entityType) {
        if (getEntityTypes() == null) {
            entityTypes = new ArrayList<DtoEntityType>();
        }
        entityTypes.add(entityType);
    }

}
