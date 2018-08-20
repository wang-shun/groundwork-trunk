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

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public interface PropertyExtensible
{
	/** 
	 * Returns a string identifying the type of entity implementing the
	 * interface, currently one of:
	 *
	 * <ul>
	 * 	<li>com.groundwork.collage.model.HostStatus.ENTITY_TYPE_CODE</li>
	 * 	<li>com.groundwork.collage.model.ServiceStatus.ENTITY_TYPE_CODE</li>
	 * 	<li>com.groundwork.collage.model.LogMessage.ENTITY_TYPE_CODE</li>
	 * </ul>
	 */
	String getEntityTypeCode();
	
	/**
	 * Returns specific instance of property value type.
	 * 
	 * @param name
	 * @param value
	 * @return
	 * 
	 * Currently one of:
	 * <ul>
	 * 	<li>com.groundwork.collage.model.impl.EntityPropertyValue</li>
	 * 	<li>com.groundwork.collage.model.impl.HostStatusPropertyValue</li>
	 * 	<li>com.groundwork.collage.model.impl.ServiceStatusPropertyValue</li>
	 * 	<li>com.groundwork.collage.model.impl.LogMessagePropertyValue</li>
	 * </ul>
	 */
	public PropertyValue getPropertyValueInstance (String name, Object value);

	/** 
	 * Returns the type of application being monitored (NAGIOS, SYSLOG, JMX_SAMPLE,...) along
	 * with all the metadata for an instance of this class in the context of
	 * that application
	 */
	ApplicationType getApplicationType();

	/** @see #getApplicationType() */
	void setApplicationType(ApplicationType applicationType);

	/** 
	 * returns the value of the property with the given name - the runtime
	 * class of the Property is one of:
	 * <ul>
	 * 	<li>java.lang.String</li>
	 * 	<li>java.util.Date</li>
	 * 	<li>java.lang.Boolean</li>
	 * 	<li>java.lang.Integer</li>
	 * 	<li>java.lang.Long</li>
	 * 	<li>java.lang.Double</li>
	 * </ul>
	 * and can be determined by calling {@link #getPropertyType} with the same
	 * property name
	 *
	 * @throws IllegalStateException 
	 * 	if the metadata for this instance has not been set, as such metadata is
	 * 	necessary to make sense of the different properties stored internally
	 *
	 * @throws IllegalArgumentException 
	 * 	if a property with the name provided has not been defined for this type
	 * 	of entity in the metadata
	 */
	Object getProperty(String propertyName);


	/** shortcut for {@link #getProperty} */
	Object get(String propertyName);


	/** 
	 * updates or sets the value for the given property
	 *
	 * @throws IllegalStateException 
	 *  if the metadata for this instance has not been set, as such metadata is
	 *  necessary to make sense of the different properties stored internally
	 *
	 * @throws IllegalArgumentException 
	 *  if a property with the name provided has not been defined for this type
	 *  of entity in the metadata, or if the runtime class of the object passed
	 *  does not match the type defined in the metadata - the method {@link
	 *  #getPropertyType} can be queried to determine whether the property
	 *  exists, and what its type should be
	 */
	void setProperty(String propertyName, Object value);


	/** shortcut for {@link #setProperty} */
	void set(String propertyName, Object value);


	/**
	 * returns a map of 'primitive' objects (as listed in {@link #getProperty})
	 * indexed by PropertyType name of the PropertyTypes for which a value has
	 * been explicitly set (even though this value may be null) - 
	 * in other words, if a PropertyType has been defined for this
	 * EntityType/ApplicationType but a value for it has not been set
	 * explicitly, it will not be included in the map returned by this method.
	 *
	 * @throws IllegalStateException 
	 *  if the metadata for this instance has not been set, as such metadata is
	 *  necessary to make sense of the different properties stored internally
	 */
	Map<String, Object> getProperties(boolean bDynamicOnly);
	
	/** 
	 * This method expects key/value mappings of String/PrimitiveObject; read
	 * below for more.
	 * 
	 * <ul>
	 * 	<li>
	 * 		the values are java primitive wrapper classes such as String, Date,
	 * 		Boolean, Integer, Long and Double, as defined in the corresponding
	 * 		{@link PropertyType} object
	 * 	</li><li>
	 * 		<p>
	 * 		the keys of the map are Strings describing some property/attribute/metric
	 * 		of the {@link PropertyExtensible} entity that is being extended 
	 * 		</p><p>
	 * 		these strings are defined (pre-configured) in the <code>Metadata</code> singleton 
	 * 		loaded from the current implementation of the 
	 * 		{@link com.groundwork.collage.CollageAccessor} factory
	 * 		</p><p>
	 * 		in the current implementation, the metadata is stored in a database
	 * 		and is accessible via the {@link ApplicationType} and its
	 * 		associated {@link EntityType} and {@link PropertyType} objects, 
	 * 		but this may change in the future, as the metadata could easily be
	 * 		stored elsewhere, such as an xml file, LDAP directory, windows
	 * 		application, remote web service, etc... 
	 * 		</p>
	 * 	</li>
	 * </ul>
	 *
	 */
	void setProperties(Map properties);

	/** 
	 * This method expects key-value mappings of String-String where the value
	 * is parseable by calling {@link #getPropertyType(String)} and {@link
	 * PropertyType#parse(String)}; that is, the values are all expressed as
	 * String but can be parsed into the proper Date, Boolean, String, Integer,
	 * Long or Double objects by retrieving the corresponding PropertyType using
	 * the key of the mapping.
	 */
	void setProperties(Properties properties);


	/** 
	 * returns metadata for the given property name, or null if a property with
	 * that name has not been defined.
	 *
	 * @throws IllegalStateException 
	 *  if the metadata for this instance has not been set, as such metadata is
	 *  necessary to make sense of the different properties stored internally
	 */
	PropertyType getPropertyType(String propertyName);


	/** 
	 * returns true if a PropertyType with the given name has been defined for
	 * this entity in this applicationType
	 *
	 * @throws IllegalStateException 
	 *  if the metadata for this instance has not been set, as such metadata is
	 *  necessary to make sense of the different properties stored internally
	 */
	boolean hasPropertyType(String propertyName);


	/** 
	 * returns sorted map of the PropertyTypes that have been defined for this
	 * ServiceStatus in the context of its ApplicationType, indexed by
	 * PropertyType name
	 *
	 * @throws IllegalStateException 
	 *  if the metadata for this instance has not been set, as such metadata is
	 *  necessary to make sense of the different properties stored internally
	 */
	Map getPropertyTypes();
	
	/**
	 * Returns list of built-in properties for the entity.
	 * @return
	 */
	List<PropertyType> getBuiltInProperties();
	
	/** Returns list of component properties for the entity.  Component properties
	 * are hibernate properties that can be used to filter / sort entity results in a 
	 * criteria query.
	 * 
	 * @return
	 */
	List<PropertyType> getComponentProperties();

}
