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

import com.groundwork.collage.CollageAccessor;

/**
 * Used to define the methods of a singleton that is used to access all
 * metadata for the application, including ApplicationType objects and their
 * associated EntityType and PropertyType objects, as well as all the values of
 * the enumerated classes
 * 
 * @author  <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Revision: 6397 $ - $Date: 2007-04-02 14:27:40 -0700 (Mon, 02 Apr 2007) $
 *
 * @see ApplicationType
 */
public interface Metadata
{
	/**
	 * Loads and caches the ApplicationType with the name provided, along with all
	 * the EntityType and PropertyType metadata defined for this application; 
	 *
	 * @param name the name of the ApplicationType for which metadata is sought
	 * @param refreshCache if true, the metadata is retrieved from
	 * persistence and the cache refreshed. If false, the metadata is
	 * retrieved from the cache
	 */
	ApplicationType getApplicationType(String name, boolean refreshCache);

	/**
	 * Loads from cache or caches the ApplicationType with the name provided,
	 * along with all the EntityType and PropertyType metadata defined for this
	 * application; this is equivalent to calling 
	 * {@link #getApplicationType(String, boolean) getApplicationType(name,refreshCache)}
	 * with <code>refreshCache</code> equal to <code>false</code>
	 */
	ApplicationType getApplicationType(String name);

	/**
	 * Loads and caches the ApplicationType with the id provided, along with all
	 * the EntityType and PropertyType metadata defined for this application; 
	 *
	 * @param id the numerical id of the ApplicationType for which metadata is sought
	 * @param refreshCache if true, the metadata is retrieved from
	 * persistence and the cache refreshed. If false, the metadata is
	 * retrieved from the cache
	 */
	ApplicationType getApplicationType(Integer id, boolean refreshCache);

	/**
	 * Loads from cache or caches the ApplicationType with the id provided,
	 * along with all the EntityType and PropertyType metadata defined for this
	 * application; this is equivalent to calling 
	 * {@link #getApplicationType(Integer, boolean) getApplicationType(id,refreshCache)}
	 * with <code>refreshCache</code> equal to <code>false</code>
	 */
	ApplicationType getApplicationType(Integer id);

	/** refreshes all applicationType metadata */
	void refreshApplicationTypeCache();


	/** retrieve EntityType by name */
	EntityType getEntityType(String name);

	/** retrieve EntityType by numerical id */
	EntityType getEntityType(Integer id);


	/** retrieve PropertyType by name */
	PropertyType getPropertyType(String name);

	/** retrieve PropertyType by numerical id */
	PropertyType getPropertyType(Integer id);

	/** 
	 * indicates whether a given PropertyType name corresponds to a 'built-in'
	 * property of an entity; that is, it is accessible through a regular
	 * getter/setter, as well as a through the 
	 * {@link PropertyExtensible#getProperty(String)} method
	 */
	boolean isBuiltInProperty(String entityType, String propertyTypeName);

	/** 
	 * causes the cache of PropertyTypes defined in the system to be reset, and
	 * forces a reload on the next time that the cache is accessed 
	 */
	void clearPropertyTypeCache();

	/** 
	 * causes the cache to reload the PropertyTypes defined in the system; if
	 * isolateFromTransaction is true, isolates the reloading from any
	 * transaction in course; if false, causes any buffered statements to be
	 * executed and retrieves the values of the cache as they stand in the
	 * transaction; this functionality is necessary to managed side-effects when
	 * reloading the metadata while the application is running, such as when the
	 * {@link CollageAccessor#isAutoCreateUnknownProperties} is set to
	 * <code>true</code>
	 */
	void reloadPropertyTypeCache(boolean isolateFromTransaction);

	/** 
	 * same as 
	 * {@link #reloadPropertyTypeCache(boolean) reloadPropertyTypeCache(isolateFromTransaction)} 
	 * with <code>isolateFromTransaction</code> equal to <code>true</code>
	 */
	void reloadPropertyTypeCache();


	/** retrieve MonitorStatus by name */
	MonitorStatus getMonitorStatus(String name);

	/** retrieve MonitorStatus by numerical id */
	MonitorStatus getMonitorStatus(Integer id);


	/** retrieve StateType by name */
	StateType getStateType(String name);

	/** retrieve StateType by numerical id */
	StateType getStateType(Integer id);


	/** retrieve CheckType by name */
	CheckType getCheckType(String name);

	/** retrieve CheckType by numerical id */
	CheckType getCheckType(Integer id);


	/** retrieve Severity by name */
	Severity getSeverity(String name);

	/** retrieve Severity by numerical id */
	Severity getSeverity(Integer id);


	/** retrieve Component by name */
	Component getComponent(String name);

	/** retrieve Component by numerical id */
	Component getComponent(Integer id);


	/** retrieve TypeRule by name */
	TypeRule getTypeRule(String name);

	/** retrieve TypeRule by numerical id */
	TypeRule getTypeRule(Integer id);


	/** retrieve Priority by name */
	Priority getPriority(String name);

	/** retrieve Priority by numerical id */
	Priority getPriority(Integer id);


	/** retrieve OperationStatus by name */
	OperationStatus getOperationStatus(String name);

	/** retrieve OperationStatus by numerical id */
	OperationStatus getOperationStatus(Integer id);
}
