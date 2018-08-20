/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.groundwork.collage.model;

/**
 * Represents a specific application entity property.
 * 
 * @author glee
 *
 */
public interface ApplicationEntityProperty extends Comparable<ApplicationEntityProperty>
{
	static final String INTERFACE_NAME = "com.groundwork.collage.model.ApplicationEntityProperty";
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.ApplicationEntityProperty";
	
    /** Hibernate property constants */
    public static final String HP_SORT_ORDER = "sortOrder";
    public static final String HP_APPLICATION_TYPE_ID = "applicationType.applicationTypeId";
    public static final String HP_APPLICATION_TYPE_NAME = "applicationType.name";
    public static final String HP_ENTITY_TYPE_ID = "entityType.entityTypeId";
    public static final String HP_ENTITY_TYPE_NAME = "entityType.name";
    public static final String HP_PROPERTY_TYPE_ID = "propertyType.propertyTypeId";
    public static final String HP_PROPERTY_TYPE_NAME = "propertyType.name";

 	/** Returns application type for this property */
	ApplicationType getApplicationType();

 	/** Returns application type for this property */
	EntityType getEntityType();
	
 	/** Returns application type for this property */
	PropertyType getPropertyType();
	
	/** Returns sort order for this property */
	Integer getSortOrder();
}
