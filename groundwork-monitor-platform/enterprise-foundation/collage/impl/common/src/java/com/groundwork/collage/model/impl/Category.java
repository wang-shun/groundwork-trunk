/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.collage.model.impl;

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Category extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.Category {

	private static final long serialVersionUID = 1;
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
	private static final PropertyType PROP_ID = 
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ID,
                    HP_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    Category.ENTITY_TYPE_CODE,
                    true);
	
	private static final PropertyType PROP_NAME = 
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_NAME,
                    HP_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Category.ENTITY_TYPE_CODE,
                    true);

	private static final PropertyType PROP_DESCRIPTION = 
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_DESCRIPTION,
                    HP_DESCRIPTION, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ENTITY_TYPE_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ENTITY_TYPE_ID,
                    HP_ENTITY_TYPE_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    com.groundwork.collage.model.EntityType.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ENTITY_TYPE_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ENTITY_TYPE_NAME,
                    HP_ENTITY_TYPE_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    com.groundwork.collage.model.EntityType.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_APPLICATION_TYPE_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_APPLICATION_TYPE_ID,
                    HP_APPLICATION_TYPE_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    com.groundwork.collage.model.ApplicationType.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_APPLICATION_TYPE_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_APPLICATION_TYPE_NAME,
                    HP_APPLICATION_TYPE_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    com.groundwork.collage.model.ApplicationType.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_AGENTID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_AGENT_ID,
                    HP_AGENT_ID, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    HostGroup.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ROOT =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ROOT,
                    HP_ROOT, // Description is hibernate property name
                    PropertyType.DataType.BOOLEAN,
                    HostGroup.ENTITY_TYPE_CODE,
                    true);

    /** Filter-Only Properties */
    private static final PropertyType PROP_PARENTS_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_PARENTS_ID,
                    HP_PARENTS_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_PARENTS_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_PARENTS_NAME,
                    HP_PARENTS_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_CHILDREN_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_CHILDREN_ID,
                    HP_CHILDREN_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_CHILDREN_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_CHILDREN_NAME,
                    HP_CHILDREN_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ANCESTORS_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ANCESTORS_ID,
                    HP_ANCESTORS_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ANCESTORS_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ANCESTORS_NAME,
                    HP_ANCESTORS_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ENTITIES_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ENTITIES_ID,
                    HP_ENTITIES_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ENTITIES_TYPE_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ENTITIES_TYPE_ID,
                    HP_ENTITIES_TYPE_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    Category.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ENTITIES_TYPE_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ENTITIES_TYPE_NAME,
                    HP_ENTITIES_TYPE_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Category.ENTITY_TYPE_CODE,
                    true);

    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;

    /* Hibernate component properties */
    private static List<PropertyType> COMPONENT_PROPERTIES = null;

    /** identifier field */
    private Integer categoryId;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String description;
    
    /* bidirectional many to many parents, (children), association */
    private Set<com.groundwork.collage.model.Category> parents;
        
    /* bidirectional many to many children, (parents), association */
    private Set<com.groundwork.collage.model.Category> children;

    /* unidirectional many to many ancestors association */
    private Set<com.groundwork.collage.model.Category> ancestors;

    /* unidirectional one to many category entities association */
    private Set<com.groundwork.collage.model.CategoryEntity> categoryEntities;
    
    private EntityType entityType;

    /** persistent field */
    private ApplicationType applicationType;

    /** persistent field */
    private String agentId;

    /** persistent field */
    private Boolean root;

    /** default constructor */
    public Category() {
        this.root = Boolean.TRUE;
    }

    /** full constructor */
    public Category(Integer categoryId, String name, String description,EntityType entityType,Set categoryEntities) {
        super();
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.entityType = entityType;
        this.categoryEntities = categoryEntities;
     }

    @Override
	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Integer getID ()
	{
		return getCategoryId();
	}	

    @Override
	public String getDescription() {
		return description;
	}

    @Override
	public void setDescription(String description) {
		this.description = description;
	}

    @Override
	public String getName() {
		return name;
	}

    @Override
	public void setName(String name) {
		this.name = name;
	}

    @Override
	public Set<com.groundwork.collage.model.Category> getParents() {
		return parents;
	}

    @Override
	public void setParents(Set<com.groundwork.collage.model.Category> parents) {
		this.parents = parents;
	}

    @Override
    public Set<com.groundwork.collage.model.Category> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<com.groundwork.collage.model.Category> children) {
        this.children = children;
    }

    @Override
    public Set<com.groundwork.collage.model.Category> getAncestors() {
        return ancestors;
    }

    @Override
    public void setAncestors(Set<com.groundwork.collage.model.Category> ancestors) {
        this.ancestors = ancestors;
    }

    public Set<com.groundwork.collage.model.CategoryEntity> getCategoryEntities() {
        if (categoryEntities == null) {
            categoryEntities = new HashSet<>();
        }
		return categoryEntities;
	}

	public void setCategoryEntities(Set<com.groundwork.collage.model.CategoryEntity> CategoryEntities) {
		this.categoryEntities = CategoryEntities;
	}
	
    /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to get the value of one
     * of the named property getters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key == null || key.length() == 0)
    	{
    		throw new IllegalArgumentException("Invalid null / empty property key.");
    	}
    	
        if (key.equalsIgnoreCase(EP_ID)) {
            return this.getID();
        }
        else if (key.equalsIgnoreCase(EP_NAME)) {
            return this.getName();
        }
        else if (key.equalsIgnoreCase(EP_DESCRIPTION)) {
            return this.getDescription();
        }
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_ID)) {
            ApplicationType applicationType = getApplicationType();
            return ((applicationType != null) ? applicationType.getApplicationTypeId() : null);
        }
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_NAME)) {
            ApplicationType applicationType = getApplicationType();
            return ((applicationType != null) ? applicationType.getName() : null);
        }
        else if (key.equalsIgnoreCase(EP_ENTITY_TYPE_ID)) {
            EntityType entityType = getEntityType();
            return ((entityType != null) ? entityType.getEntityTypeId() : null);
        }
        else if (key.equalsIgnoreCase(EP_ENTITY_TYPE_NAME)) {
            EntityType entityType = getEntityType();
            return ((entityType != null) ? entityType.getName() : null);
        }
        else if (key.equalsIgnoreCase(EP_AGENT_ID)) {
            return getAgentId();
        }
        else if (key.equalsIgnoreCase(EP_ROOT)) {
            return isRoot();
        }
        else {
            return super.getProperty(key);
        }
    }   
    
	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getBuiltInProperties()
	 */
	@Override
	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(9);
		
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_NAME);
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);
        BUILT_IN_PROPERTIES.add(PROP_ENTITY_TYPE_ID);
        BUILT_IN_PROPERTIES.add(PROP_ENTITY_TYPE_NAME);
        BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);
        BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);
        BUILT_IN_PROPERTIES.add(PROP_AGENTID);
        BUILT_IN_PROPERTIES.add(PROP_ROOT);

		return BUILT_IN_PROPERTIES;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getEntityTypeCode()
	 */
	@Override
	public String getEntityTypeCode()
	{
		return ENTITY_TYPE_CODE;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getPropertyValueInstance(java.lang.String, java.lang.Object)
	 */
	@Override
	public PropertyValue getPropertyValueInstance(String name, Object value)
	{
		return new EntityPropertyValue(categoryId, getEntityTypeId(), name, value);
	}

    @Override
    public Integer getApplicationTypeId()
    {
        if (this.applicationType == null)
            return null;

        return this.applicationType.getApplicationTypeId();
    }

    @Override
    public ApplicationType getApplicationType()
    {
        return  this.applicationType;
    }

    @Override
    public void setApplicationType(ApplicationType applicationType)
    {
        this.applicationType = applicationType;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public Boolean isRoot() {
        return root;
    }

    @Override
    public void setRoot(Boolean root) {
        this.root = root;
    }

    /* (non-Javadoc)
	 * @see com.groundwork.collage.model.PropertyExtensible#getComponentProperties()
	 */
	public List<PropertyType> getComponentProperties()
	{
        if (COMPONENT_PROPERTIES != null)
            return COMPONENT_PROPERTIES;

        COMPONENT_PROPERTIES = new ArrayList<PropertyType>(18);

        // Add non-volatile properties
        COMPONENT_PROPERTIES.add(PROP_ID);
        COMPONENT_PROPERTIES.add(PROP_NAME);
        COMPONENT_PROPERTIES.add(PROP_DESCRIPTION);
        COMPONENT_PROPERTIES.add(PROP_ENTITY_TYPE_ID);
        COMPONENT_PROPERTIES.add(PROP_ENTITY_TYPE_NAME);
        COMPONENT_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);
        COMPONENT_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);
        COMPONENT_PROPERTIES.add(PROP_AGENTID);
        COMPONENT_PROPERTIES.add(PROP_ROOT);
        COMPONENT_PROPERTIES.add(PROP_PARENTS_ID);
        COMPONENT_PROPERTIES.add(PROP_PARENTS_NAME);
        COMPONENT_PROPERTIES.add(PROP_CHILDREN_ID);
        COMPONENT_PROPERTIES.add(PROP_CHILDREN_NAME);
        COMPONENT_PROPERTIES.add(PROP_ANCESTORS_ID);
        COMPONENT_PROPERTIES.add(PROP_ANCESTORS_NAME);
        COMPONENT_PROPERTIES.add(PROP_ENTITIES_ID);
        COMPONENT_PROPERTIES.add(PROP_ENTITIES_TYPE_ID);
        COMPONENT_PROPERTIES.add(PROP_ENTITIES_TYPE_NAME);

        return COMPONENT_PROPERTIES;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}	
}
