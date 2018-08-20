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
import java.util.SortedMap;

/**
 * Represents a type of application that has been configured to be monitored
 * through the collage system; as of these writing there are three such
 * application types: "NAGIOS", "SYSLOG", and "JMX_SAMPLE"; through an
 * ApplicationType, it is possible to access all the 'soft-properties' that are
 * defined for the different EntityTypes in the application (SERVICE_STATUS,
 * HOST_STATUS, etc...) for which PropertyTypes have been defined.
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public interface ApplicationType extends AttributeData, PropertyExtensible
{
	static final String ENTITY_TYPE_CODE = "APPLICATION_TYPE";
	
	static final String INTERFACE_NAME = "com.groundwork.collage.model.ApplicationType";
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.ApplicationType";
	
	/** 
	 * There are certain entities for which the Metadata does not vary according
	 * to the type of application monitored - such metadata is grouped under a
	 * fixed application type "SYSTEM"
	 */
	static final String SYSTEM_APPLICATION_TYPE_NAME = "SYSTEM";

	/** 
	 * There are certain entities for which the Metadata does not vary according
	 * to the type of application monitored - such metadata is grouped under a
	 * fixed "SYSTEM" application type with the ID provided below
	 */
	static final Integer SYSTEM_APPLICATION_TYPE_ID = new Integer(1);
	
	/** Hibernate Property Constants */
	static final String HP_ID = "applicationTypeId";
	static final String HP_NAME = "name";
	static final String HP_DISPLAY_NAME = "displayName";
	static final String HP_DESCRIPTION = "description";		
	
	/** Entity Property Name Constants */
	static final String EP_ID = "ApplicationTypeId";
	static final String EP_NAME = "ApplicationType";
	static final String EP_DISPLAY_NAME = "DisplayName";
	static final String EP_SERVICE_COUNT = "ServiceCount";
	static final String EP_HOST_COUNT = "HostCount";
	static final String EP_DESCRIPTION = "Description";
	
    /** a unique numerical representation of this ApplicationType */
	Integer getApplicationTypeId();

	/** returns a map of EntityTypes, indexed by EntityType name */
	SortedMap getEntityTypes();

	/** given an EntityType.name, return the corresponding {@link EntityType} */
	EntityType getEntityType(String name);

	/** shortcut for getEntity(name).getProperty(name) */
	PropertyType getPropertyType(String entityName, String propertyName);

	/** 
	 * assigns a PropertyType to the specified EntityType, in the context of this
     * ApplicationType (ternary relationship)
	 */
	void assignPropertyType(EntityType entityType, PropertyType propertyType, int sortOrder);

	/** 
	 * unassigns the specified property from the specified entity, in the
	 * context of this ApplicationType
	 *
	 * @return true if a PropertyType and EntityType with those names were
	 * found, and the PropertyType was subsequently unassigned from that
	 * EntityType successfully
	 */
	boolean unassignPropertyType(String entityTypeName, String propertyTypeName);

    /** returns display name for application type or null if the type should not be displayed */
    public String getDisplayName();

    /** sets display name */
    public void setDisplayName(String displayName);

	/*
	 * Returns delimited list of state transition criteria for the application type
	 */
	public String getStateTransitionCriteria();

    public void setStateTransitionCriteria(String stateTransitionCriteria);

	/**
	 * Returns list of state transition criteria for the application type
	 * @return
	 */
	public List<String> getStateTransitionCriteriaList ();	
}
