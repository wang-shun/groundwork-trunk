/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoCategoryUpdate
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "categoryUpdate")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCategoryUpdate {

    // General attributes

    @XmlAttribute
    private String entityTypeName;

    @XmlElement(name="entityType")
    private DtoEntityType entityType;

    @XmlAttribute
    private String categoryName;

    @XmlElement(name="category")
    private DtoCategory category;

    // Delete attributes

    @XmlAttribute
    private String delete;

    @XmlAttribute
    private Boolean childrenOnly;

    // Create attributes

    @XmlAttribute
    private String create;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String agentId;

    @XmlAttribute
    private String appType;

    @XmlElement(name="applicationType")
    private DtoApplicationType applicationType;

    @XmlAttribute
    private String parentName;

    @XmlElement(name="parent")
    private DtoCategory parent;

    // Clone attributes

    @XmlAttribute
    private String clone;

    @XmlAttribute
    private String cloneName;

    // Modify attributes

    @XmlAttribute
    private String modify;

    @XmlElementWrapper(name="otherCategoryNames")
    @XmlElement(name="categoryName")
    private List<String> otherCategoryNames;

    @XmlElementWrapper(name="otherCategories")
    @XmlElement(name="category")
    private List<DtoCategory> otherCategories;

    public String getEntityTypeName() {
        if ((entityTypeName == null) && (entityType != null)) {
            entityTypeName = entityType.getName();
        }
        if ((entityTypeName == null) && (category != null)) {
            entityTypeName = category.getEntityTypeName();
            if ((entityTypeName == null) && (category.getEntityType() != null)) {
                entityTypeName = category.getEntityType().getName();
            }
        }
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    public DtoEntityType getEntityType() {
        if ((entityType == null) && (category != null)) {
            entityType = category.getEntityType();
        }
        return entityType;
    }

    public void setEntityType(DtoEntityType entityType) {
        this.entityTypeName = ((entityType != null) ? entityType.getName() : null);
        this.entityType = entityType;
    }

    public String getCategoryName() {
        if ((categoryName == null) && (category != null)) {
            categoryName = category.getName();
        }
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public DtoCategory getCategory() {
        return category;
    }

    public void setCategory(DtoCategory category) {
        this.categoryName = ((category != null) ? category.getName() : null);
        this.category = category;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public Boolean getChildrenOnly() {
        return childrenOnly;
    }

    public void setChildrenOnly(Boolean childrenOnly) {
        this.childrenOnly = childrenOnly;
    }

    public String getCreate() {
        return create;
    }

    public void setCreate(String create) {
        this.create = create;
    }

    public String getDescription() {
        if ((description == null) && (category != null)) {
            description = category.getDescription();
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgentId() {
        if ((agentId == null) && (category != null)) {
            agentId = category.getAgentId();
        }
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAppType() {
        if ((appType == null) && (applicationType != null)) {
            appType = applicationType.getName();
        }
        if ((appType == null) && (category != null)) {
            appType = category.getAppType();
            if ((appType == null) && (category.getApplicationType() != null)) {
                appType = category.getApplicationType().getName();
            }
        }
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public DtoApplicationType getApplicationType() {
        if ((applicationType == null) && (category != null)) {
            applicationType = category.getApplicationType();
        }
        return applicationType;
    }

    public void setApplicationType(DtoApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getParentName() {
        if ((parentName == null) && (parent != null)) {
            parentName = parent.getName();
        }
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public DtoCategory getParent() {
        return parent;
    }

    public void setParent(DtoCategory parent) {
        this.parentName = ((parent != null) ? parent.getName() : null);
        this.parent = parent;
    }

    public String getClone() {
        return clone;
    }

    public void setClone(String clone) {
        this.clone = clone;
    }

    public String getCloneName() {
        return cloneName;
    }

    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }

    public String getModify() {
        return modify;
    }

    public void setModify(String modify) {
        this.modify = modify;
    }

    public List<String> getOtherCategoryNames() {
        if ((otherCategoryNames == null) && (otherCategories != null)) {
            otherCategoryNames = new ArrayList<String>();
            for (DtoCategory otherCategory : otherCategories) {
                if (otherCategory.getName() != null) {
                    otherCategoryNames.add(otherCategory.getName());
                } else {
                    otherCategoryNames = null;
                    break;
                }
            }
        }
        return otherCategoryNames;
    }

    public void setOtherCategoryNames(List<String> otherCategoryNames) {
        this.otherCategoryNames = otherCategoryNames;
    }

    public void addOtherCategoryName(String otherCategoryName) {
        if (otherCategoryNames == null) {
            otherCategoryNames = new ArrayList<String>();
        }
        otherCategoryNames.add(otherCategoryName);
    }

    public List<DtoCategory> getOtherCategories() {
        return otherCategories;
    }

    public void setOtherCategories(List<DtoCategory> otherCategories) {
        this.otherCategories = otherCategories;
    }

    public void addOtherCategory(DtoCategory otherCategory) {
        addOtherCategoryName(otherCategory.getName());
        if (otherCategories == null) {
            otherCategories = new ArrayList<DtoCategory>();
        }
        otherCategories.add(otherCategory);
    }
}
