package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCategory {

    // Shallow Attributes

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String entityTypeName;

    @XmlAttribute
    private String agentId;

    @XmlAttribute
    private String appType;

    @XmlElementWrapper(name="parentNames")
    @XmlElement(name="parentName")
    private List<String> parentNames;

    @XmlElementWrapper(name="childNames")
    @XmlElement(name="childName")
    private List<String> childNames;

    @XmlAttribute
    private Boolean root;

    // Deep Attributes

    @XmlElement(name="applicationType")
    private DtoApplicationType applicationType;

    @XmlElement
    private DtoEntityType entityType;

    @XmlElementWrapper(name="parents")
    @XmlElement(name="category")
    private List<DtoCategory> parents;

    @XmlElementWrapper(name="children")
    @XmlElement(name="category")
    private List<DtoCategory> children;

    @XmlElementWrapper(name="entities")
    @XmlElement(name="entity")
    private List<DtoCategoryEntity> entities;


    public DtoCategory() {
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

    public DtoEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(DtoEntityType entityType) {
        this.entityType = entityType;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public List<String> getParentNames() {
        return parentNames;
    }

    public void setParentNames(List<String> parentNames) {
        this.parentNames = parentNames;
    }

    public List<String> getChildNames() {
        return childNames;
    }

    public void setChildNames(List<String> childNames) {
        this.childNames = childNames;
    }

    public Boolean isRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    public DtoApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(DtoApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public List<DtoCategory> getParents() {
        return parents;
    }

    public void setParents(List<DtoCategory> parents) {
        this.parents = parents;
    }

    public List<DtoCategory> getChildren() {
        return children;
    }

    public void setChildren(List<DtoCategory> children) {
        this.children = children;
    }

    public List<DtoCategoryEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<DtoCategoryEntity> entities) {
        this.entities = entities;
    }

    public void addParentName(String parentName) {
        if (parentNames == null) {
            parentNames = new ArrayList<String>();
        }
        parentNames.add(parentName);
    }

    public void addParent(DtoCategory parent) {
        addParentName(parent.getName());
        if (parents == null) {
            parents = new ArrayList<DtoCategory>();
        }
        parents.add(parent);
    }

    public void addChildName(String childName) {
        if (childNames == null) {
            childNames = new ArrayList<String>();
        }
        childNames.add(childName);
    }

    public void addChild(DtoCategory child) {
        addChildName(child.getName());
        if (children == null) {
            children = new ArrayList<DtoCategory>();
        }
        children.add(child);
    }

    public void addEntity(DtoCategoryEntity entity) {
        if (entities == null) {
            entities = new ArrayList<DtoCategoryEntity>();
        }
        entities.add(entity);
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

}
