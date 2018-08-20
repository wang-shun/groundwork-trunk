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

package com.groundwork.collage;

import java.util.Map;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyType;

/**
 * Adds or updates metadadata in the Collage database; this is designed to be
 * accessed from the Collage feeders (collector/normalizers) to create or
 * update metadata information on the metrics collected/monitored by the system
 * for different Entities (Device, Host, Service, Status, Application) in the
 * context of different ApplicationTypes (NAGIOS, SYSLOG, custom JMX, etc...)
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com" > Roger Ruttimann </a>
 * @author <a href="mailto:pparavicini@itgroundwork.com">Philippe Paravicini</a>
 *
 * @version $Id: CollageAdminMetadata.java 6397 2007-04-02 21:27:40Z glee $
 *  
 */
public interface CollageAdminMetadata 
{
	/** returns a list of the ApplicationTypes in the system */
	String[] getApplicationTypeNames();

	/** returns a list of the Entity Types that can be extended in the system */
	String[] getExtensibleEntityNames();

	/** returns a list of all the PropertyTypes already defined in the system */
	String[] getPropertyTypeNames();

	/** 
	 * returns a list of the codes for primitive that can be assigned to
	 * a PropertyType (currently one of STRING, BOOLEAN, DATE, INTEGER, LONG or
	 * DOUBLE)
	 */
	String[] getSupportedPrimitives();

	/** 
	 * returns a map of {@link ApplicationType} objects indexed by Application
	 * Type names, from the ApplicationType it is possible to retrieve the
	 * different {@link EntityType} objects defined for the application, and
	 * their corresponding list of {@link PropertyType PropertyTypes}; the
	 * metadata is retrieved from cache.
	 */
	Map getAllMetadata();

	/** 
	 * returns all the metadata for a specific {@link ApplicationType}; from
	 * the ApplicationType it is possible to retrieve the different {@link
	 * EntityType} objects defined for the application, and their corresponding
	 * list of {@link PropertyType PropertyTypes}; equivalent to calling 
	 */
	ApplicationType getApplicationType(String applicationTypeName);

	/** adds a new application type to the system */
	void createApplicationType(String name, String description);


	/**
	 * Adds a PropertyType to the system; if the PropertyType already
	 * exists, an exception is thrown; for a more permissive useage see 
	 * {@link #createOrUpdatePropertyType}.
	 * <p>
	 * Since PropertyTypes may be shared accross ApplicationTypes and
	 * EntityTypes,  modifying a PropertyType may have unintended
	 * repercussions.  This method should be used when the intent is strictly
	 * to add a new PropertyType, and avoid unintended side-effects.
	 * </p>
	 *
	 * @param name the name of the application type, most likely
	 * one of the values returned by {@link #getApplicationTypeNames} - if none
	 * is found an exception is thrown since {@link #createApplicationType} should
	 * be called first to create the ApplicationType
	 *
	 * @param description a short description of the property, for example to
	 * display on a UI
	 *
	 * @param primitiveType the name/code of the runtime type of the property
	 * to add; this <b>must</b> be one of the primitive types included in the
	 * list returned by {@link #getSupportedPrimitives}
	 *
	 */
	void createPropertyType(String name, String description, String primitiveType);


	/**
	 * Adds or modifies a Property in the system; note that since PropertyTypes
	 * may be shared by multiple ApplicationTypes and/or EntityTypes, changing
	 * a PropertyType will change it for all ApplicationType/EntityType
	 * combinations that use that PropertyType.
	 *
	 * @param name the name of the application type, most likely
	 * one of the values returned by {@link #getApplicationTypeNames} - if none
	 * is found an exception is thrown since {@link #createApplicationType} should
	 * be called first to create the ApplicationType
	 *
	 * @param description a short description of the property, for example to
	 * display on a UI
	 *
	 * @param primitiveType the name/code of the runtime type of the property
	 * to add; this <b>must</b> be one of the primitive types included in the
	 * list returned by {@link #getSupportedPrimitives}
	 *
	 */
	void createOrUpdatePropertyType(String name, String description, String primitiveType);

	
	/**
	 * Assigns an existing PropertyType to an EntityType in the context of an
	 * ApplicationType
	 *
	 * @param applicationTypeName the name of the application type, most likely
	 * one of the values returned by {@link #getApplicationTypeNames} - if none
	 * is found an exception is thrown since {@link #createApplicationType} should
	 * be called first to create the ApplicationType
	 *
	 * @param entityTypeName the name of the entity type to be extended - this
	 * <b>must</b> be one of the entities returned by {@link #getExtensibleEntityNames}
	 *
	 * @param propertyTypeName the name of the property type to add - if the
	 * property type does not exist an exception is thrown, and the method
	 * {@link #createPropertyType} should be used instead, with the appropriate
	 * primitive type parameter
	 *
	 */
	void assignPropertyType(
			String applicationTypeName, String entityTypeName, String propertyTypeName);


	/** 
	 * Returns true if a PropertyType with the given name has been assigned to
	 * the EntityType provided, in the context of the ApplicationType indicated.
	 */
	boolean isPropertyTypeAssigned(
			String applicationTypeName, String entityTypeName, String propertyTypeName);

	/** 
	 * returns an array of string tuples, each of which contains an
	 * ApplicationType name and EntityType name denoting the entities to which
	 * the given PropertyType has been assigned, in the context of an
	 * ApplicationType; this method can be used to survey the useage of a
	 * PropertyType before it is deleted
	 *
	 * @return an array of string array tuples, where the first element in the
	 * tuple is the name of the ApplicationType and the second name in the tuple
	 * is the name of the EntityType
	 */
	String[][] getPropertyTypeAssignments(String propertyTypeName);


	/** 
	 * Unassigns a PropertyType from an Entity, within the context of an
	 * ApplicationType; this method does not remove the PropertyType from the
	 * system altogether
	 */
	void unassignPropertyType(String applicationTypeName, String entityTypeName, String propertyTypeName);


	/**
	 * Removes a PropertyType definition from the system altogether; if
	 * <code>safeDelete</code> is <code>true</code> and the PropertyType is currently
	 * in use by an EntityType, the operation fails; if <code>safeDelete</code>
	 * is <code>false</code> the PropertyType is unassigned from all the
	 * EntityTypes that may be using it, and expunged from the system
	 * unconditionally
	 */
	void deletePropertyType(String propertyType, boolean safeDelete);

	/**
	 * This method is used to automatically create and/or assign PropertyTypes
	 * based on a map of property values, and assign them to the ApplicationType
	 * and EntityType with the names provided; this method is meant to be used
	 * when the environment variable {@link CollageAccessor#isAutoCreateUnknownProperties} 
	 * is set to <code>true</code>
	 *
	 * @param applicationTypeName the name of the ApplicationType to which we
	 * should assign the new PropertyTypes
	 *
	 * @param entityTypeName the name of the EntityType to which we should assign
	 * the new PropertyTypes
	 *
	 * @param props a map of property values, indexed by String, that is similar
	 * to the map passed via the methods 
	 * {@link CollageAdminInfrastructure#updateHostStatus}, 
	 * {@link CollageAdminInfrastructure#updateServiceStatus} or 
	 * {@link CollageAdminInfrastructure#updateLogMessage} 
	 * The key of the map will be used as the name of the PropertyType, and its
	 * primitive type will be based on the runtime type of the values of the map;
	 * note that if the map is a map of String/Strings,  all the PropertyTypes
	 * created will have a String as the primitive type
	 */
	public void createOrAssignUnknownProperties(String applicationTypeName, String entityTypeName, Map props);
}
