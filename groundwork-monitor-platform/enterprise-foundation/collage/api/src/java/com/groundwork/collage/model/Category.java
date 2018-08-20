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

package com.groundwork.collage.model;

import java.util.Set;


public interface Category extends AttributeData
{	
    /** the name that identifies this entity in the system */
    static final String ENTITY_TYPE_CODE = "CATEGORY";
        
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.Category";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.Category";
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */
	static final String HP_ID = "categoryId";
	static final String HP_NAME = "name";
	static final String HP_DESCRIPTION = "description";
    static final String HP_ENTITY_TYPE = "entityType";
    static final String HP_ENTITY_TYPE_ID = HP_ENTITY_TYPE + ".entityTypeId";
    static final String HP_ENTITY_TYPE_NAME = HP_ENTITY_TYPE + ".name";
    static final String HP_APPLICATION_TYPE_ID = "applicationType.applicationTypeId";
    static final String HP_APPLICATION_TYPE_NAME = "applicationType.name";
    static final String HP_AGENT_ID = "agentId";
    static final String HP_ROOT = "root";

    /** Filter-Only Properties */
    static final String HP_PARENTS = "parents";
    static final String HP_PARENTS_ID = HP_PARENTS + ".categoryId";
    static final String HP_PARENTS_NAME = HP_PARENTS + ".name";
    static final String HP_CHILDREN = "children";
    static final String HP_CHILDREN_ID = HP_CHILDREN + ".categoryId";
    static final String HP_CHILDREN_NAME = HP_CHILDREN + ".name";
    static final String HP_ANCESTORS = "ancestors";
    static final String HP_ANCESTORS_ID = HP_ANCESTORS + ".categoryId";
    static final String HP_ANCESTORS_NAME = HP_ANCESTORS + ".name";
    static final String HP_ENTITIES = "categoryEntities";
    static final String HP_ENTITIES_ID = HP_ENTITIES + ".objectID";
    static final String HP_ENTITIES_TYPE = HP_ENTITIES + ".entityType";
    static final String HP_ENTITIES_TYPE_ID = HP_ENTITIES_TYPE + ".entityTypeId";
    static final String HP_ENTITIES_TYPE_NAME = HP_ENTITIES_TYPE + ".name";
    static final String HP_ENTITIES_CATEGORY = HP_ENTITIES + ".category";
    static final String HP_ENTITIES_CATEGORY_ID = HP_ENTITIES_CATEGORY + ".categoryId";

    /** Entity Property Constants */
	static final String EP_ID = "CategoryId";
	static final String EP_NAME = "Category";
	static final String EP_DESCRIPTION = "Description";
    static final String EP_APPLICATION_TYPE_ID = "ApplicationTypeId";
    static final String EP_APPLICATION_TYPE_NAME = "ApplicationType";
    static final String EP_ENTITY_TYPE_ID = "EntityTypeId";
    static final String EP_ENTITY_TYPE_NAME = "EntityType";
    static final String EP_AGENT_ID = "AgentId";
    static final String EP_ROOT = "Root";
    static final String EP_PARENTS_ID = "ParentsId";
    static final String EP_PARENTS_NAME = "ParentsName";
    static final String EP_CHILDREN_ID = "ChildrenId";
    static final String EP_CHILDREN_NAME = "ChildrenName";
    static final String EP_ANCESTORS_ID = "AncestorsId";
    static final String EP_ANCESTORS_NAME = "AncestorsName";
    static final String EP_ENTITIES_ID = "EntitiesId";
    static final String EP_ENTITIES_TYPE_ID = "EntitiesTypeId";
    static final String EP_ENTITIES_TYPE_NAME = "EntitiesTypeName";

	Integer getCategoryId();

    String getName();
    void setName(String name);

    String getDescription();
    void setDescription(String description);

	Set<CategoryEntity> getCategoryEntities();
	void setCategoryEntities(Set<CategoryEntity> CategoryEntities);

	Set<Category> getParents();
	void setParents(Set<Category> parents);

    Set<Category> getChildren();
    void setChildren(Set<Category> children);

    Set<Category> getAncestors();
    void setAncestors(Set<Category> ancestors);

    void setEntityType(EntityType entityType);
	
	EntityType getEntityType();

    Integer getApplicationTypeId();
    ApplicationType getApplicationType();
    void setApplicationType(ApplicationType applicationType);

    String getAgentId();
    void setAgentId(String agentId);

    Boolean isRoot();
    void setRoot(Boolean root);
}