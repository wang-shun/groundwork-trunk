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

import java.util.List;
import java.util.Map;

/**
 * Represents a class in the system that may be extended with arbitrary
 * {@link PropertyType} objects in the context of an {@link ApplicationType};
 * this class is used to keep track of metadata, see for more below.
 * <p>
 * For example, depending on whether we track a host through Nagios, or an
 * application through JMX, we may assign different attributes to the Host and
 * ServiceStatus objects in those different two contexts; this EntityType
 * represents the entity/class that we are extending, that is, the Host and
 * ServiceStatus.
 * </p>
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public interface EntityType extends Cloneable
{
    /** the name that identifies this entity in the system */
    static final String ENTITY_TYPE_CODE = "ENTITY_TYPE";

    /** Spring bean interface id */
    static final String INTERFACE_NAME = "com.groundwork.collage.model.EntityType";

    /** Hibernate component name that this entity service using */
    public static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.EntityType";
	
	Integer getEntityTypeId();

	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);
/*
	ApplicationType getApplicationType();
	void setApplicationType(ApplicationType applicationType);
*/
	/** returns a Map of PropertyType objects, indexed by PropertyType name */
	Map getPropertyTypes();

	PropertyType getPropertyType(String name);

	/** 
   * adds a PropertyType to the map and indexes it by name, provided that the
   * PropertyType is not null; PropertyTypes cannot be added directly to an
   * EntityType, they must be added to the containing ApplicationType; this
   * method is used to keep the map of PropertyTypes in synch.
	 */
	void mapPropertyType(PropertyType propertyType);
	
	List<PropertyType> getBuiltInProperties ();
	
	List<PropertyType> getComponentProperties ();
	
	void setLogicalEntity (Boolean isLogicalEntity);
	Boolean getLogicalEntity ();
	
	void setApplicationTypeSupported(Boolean isApplicationTypeSupported);
	Boolean getApplicationTypeSupported();

}
