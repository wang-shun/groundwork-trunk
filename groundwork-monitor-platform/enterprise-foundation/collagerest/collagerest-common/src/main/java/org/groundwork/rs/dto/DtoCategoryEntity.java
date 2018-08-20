package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "categoryEntity")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCategoryEntity {

    // Shallow Attributes
    @XmlAttribute
    private Integer id; // alias categoryEntityID

    @XmlAttribute
    private Integer objectID;

    @XmlAttribute
    private Integer entityTypeId;

    @XmlElement(name="entityType")
    private DtoEntityType entityType;

    @XmlAttribute
    private String entityTypeName;

    public DtoCategoryEntity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getObjectID() {
        return objectID;
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    public DtoEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(DtoEntityType entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Integer entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }
}
