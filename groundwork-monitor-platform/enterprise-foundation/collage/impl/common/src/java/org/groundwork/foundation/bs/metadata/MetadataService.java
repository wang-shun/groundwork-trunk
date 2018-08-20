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
package org.groundwork.foundation.bs.metadata;

import com.groundwork.collage.model.ApplicationEntityProperty;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Component;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.OperationStatus;
import com.groundwork.collage.model.PluginPlatform;
import com.groundwork.collage.model.Priority;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.Severity;
import com.groundwork.collage.model.StateType;
import com.groundwork.collage.model.TypeRule;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

public interface MetadataService extends BusinessService 
{
	/** Component Properties **/
	public static final String PROP_NAME = "name";
	
	/**
	 * Creates a new instance of ApplicationType which has NOT been persisted.
	 * Use saveApplicationType to persist the new instance.
	 * @return
	 */
	ApplicationType createApplicationType() throws BusinessServiceException;
	
	/**
	 * Creates a new instance of ApplicationType which has NOT been persisted.
	 * 
	 * @param name
	 * @param description
	 * @return
	 * @throws BusinessServiceException
	 */
	ApplicationType createApplicationType(String name, String description) throws BusinessServiceException;
	
	/**
	 * Persists the provided application type.
	 * 
	 * @param applicationType
	 */
	void saveApplicationType(ApplicationType applicationType) throws BusinessServiceException;
	
	/**
	 * Creates and persists the new application type or update the description of an existing application
	 * type.
	 * 
	 * @param name
	 * @param description
	 * @return Returns new instance of ApplicationType
	 * @throws BusinessServiceException
	 */
	ApplicationType saveApplicationType(String name, String description) throws BusinessServiceException;
	
	/**
	 * Removes the application type from persistence.
	 * 
	 * @param applicationType
	 * @throws BusinessServiceException
	 */
	void deleteApplicationType(ApplicationType applicationType) throws BusinessServiceException;
	
	/**
	 * Removes the application type whic matches the id specified.
	 * 
	 * @param applicationTypeId
	 * @throws BusinessServiceException
	 */
	void deleteApplicationTypeById(int applicationTypeId) throws BusinessServiceException;
	
	/**
	 * Removes the application type which matches the name specified.
	 * 
	 * @param name
     * @return deleted status
	 * @throws BusinessServiceException
	 */
	boolean deleteApplicationTypeByName(String name) throws BusinessServiceException;
	
	/**
	 * loads the ApplicationType with the name provided, from which all
	 * the EntityTypes and PropertyTypes defined for this application will be
	 * available
	 * 
	 * @param name
	 */
	ApplicationType getApplicationTypeByName(String name) throws BusinessServiceException;

	/**
	 * loads the ApplicationType with the id provided, from which all
	 * the EntityTypes and PropertyTypes defined for this application will be
	 * available
	 * 
	 * @param id
	 */
	ApplicationType getApplicationTypeById(int id) throws BusinessServiceException;

	/** retrieves all the ApplicationTypes defined in the system */
	FoundationQueryList getApplicationTypes(FilterCriteria filterCriteria,
    												SortCriteria sortCriteria,
    												int firstResult,
    												int maxResults) throws BusinessServiceException;

	/**
	 * Retrieves application entity types for specified criteria
	 * 
	 * @param entityType
	 * @param appType
	 * @param bComponentProperties - Boolean indicating whether to return "filterable / hibernate" properties 
	 * or properties returned in a query
	 * @return
	 * @throws BusinessServiceException
	 */
	List<ApplicationEntityProperty> getApplicationEntityProperties (String entityType,
													String appType,
													boolean bComponentProperties) throws BusinessServiceException;
	
	/** retrieves all the EntityTypes defined in the system */
	FoundationQueryList getEntityTypes(FilterCriteria filterCriteria,
											SortCriteria sortCriteria,
											int firstResult,
											int maxResults) throws BusinessServiceException;
	
	/**
	 * Returns entity information in the form of a property value map.
	 * 
	 * @param entityType
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	FoundationQueryList performEntityQuery (String entityType,
											FilterCriteria filterCriteria, 
											SortCriteria sortCriteria, 
											int firstResult, 
											int maxResults) throws BusinessServiceException;
	
	int performEntityCountQuery (String entityType,
	  							  FilterCriteria filterCriteria) throws BusinessServiceException;	

	/** 
	 * retrieves all the PropertyTypes defined in the system - does not support 
	 * transaction propagation
	 */
	FoundationQueryList getPropertyTypes(FilterCriteria filterCriteria,
												SortCriteria sortCriteria,
												int firstResult,
												int maxResults) throws BusinessServiceException;

	/**
	 * Retrieve the EntityType with the given name
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	EntityType getEntityTypeByName(String name) throws BusinessServiceException;
	
	/**
	 * Retrieve the EntityType by Id
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	EntityType getEntityTypeById(int id) throws BusinessServiceException;

	/**
	 * Retrieve the PropertyType with the given name
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	PropertyType getPropertyTypeByName(String name) throws BusinessServiceException;
	
	/**
	 * Retrieve the PropertyType with the given id
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	PropertyType getPropertyTypeById(int id) throws BusinessServiceException;
	
	/**
	 * Creates a new instance of PropertyType if it does not exist and Persists it.
	 * If the PropertyType does exist it is updated.
	 * 
	 * @param name
	 * @param description
	 * @param primitiveType
	 * @return
	 * @throws BusinessServiceException
	 */
	void savePropertyType(String name, String description, String primitiveType) throws BusinessServiceException;
	
	/**
	 * Deletes the specified PropertyType if it exists.
	 * 
	 * @param name
     * @return deleted status
	 */
	boolean deletePropertyTypeByName(String name);
	
	/** 
	 * Retrieves all MonitorStatus values defined in the system; MonitorStatus is an
	 * enumerated class that represents the state of a Monitor (OK, UP, DOWN,
	 * UNREACHABLE, etc...)
	 */
	Collection<MonitorStatus> getMonitorStatusValues() throws BusinessServiceException;
	
	/**
	 * Returns the specified MonitorStatus
	 * 
	 * @param monitorStatusName
	 * @return
	 * @throws BusinessServiceException
	 */
	MonitorStatus getMonitorStatusByName (String monitorStatusName) throws BusinessServiceException;

	/**
	 * Returns the specified MonitorStatus
	 * 
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	MonitorStatus getMonitorStatusById (int id) throws BusinessServiceException;
	
	/** 
	 * Retrieves all StateTypes defined in the system; StateType is an
	 * enumerated class that represents the state of a check (currently either
	 * 'SOFT' or 'HARD')
	 */
	Collection<StateType> getStateTypeValues() throws BusinessServiceException;
	
	/**
	 * Retrieves state type by name.
	 * 
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	StateType getStateTypeByName(String name) throws BusinessServiceException;

	/**
	 * Retrieves state type by id.
	 * 
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	StateType getStateTypeById(int id) throws BusinessServiceException;
	
	/** 
	 * Retrieves all CheckTypes defined in the system; CheckType is an
	 * enumerated class that represents the type of a check performed (currently
	 * either 'ACTIVE' or 'PASSIVE')
	 */
	Collection<CheckType> getCheckTypeValues() throws BusinessServiceException;
	
	/** 
	 * Retrieves check type by name
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	CheckType getCheckTypeByName(String name) throws BusinessServiceException;

	/** 
	 * Retrieves check type by Id
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	CheckType getCheckTypeById(int id) throws BusinessServiceException;
	
	/** 
	 * Retrieves all Severity values defined in the system; Severity is an
	 * enumerated class that represents the severity of an event ('FATAL',
	 * 'WARNING', 'LOW', 'STATISTIC', etc...)
	 */
	Collection<Severity> getSeverityValues() throws BusinessServiceException;
	
	/**
	 * Retrieve severity by name.
	 * 
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	Severity getSeverityByName(String name) throws BusinessServiceException;

	/**
	 * Retrieve severity by Id.
	 * 
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	Severity getSeverityById(int id) throws BusinessServiceException;
	
	/** 
	 * Retrieves all Components defined in the system; Component is an enumerated
	 * class that represents a type of monitoring agent ('SNMP', 'MQ',
	 * 'JMSLISTENER', etc...)
	 */
	Collection<Component> getComponentValues() throws BusinessServiceException;
	
	/**
	 * Retrieve component by name
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	Component getComponentByName(String name) throws BusinessServiceException;
	
	/**
	 * Retrieve component by id
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	Component getComponentById(int id) throws BusinessServiceException;

	/** 
	 * Retrieves all TypeRules defined in the system; TypeRule is an
	 * enumerated class that  represents a rule to apply to a message based on
	 * its source (currently one of 'NETWORK', 'HARDWARE', 'SERVICE',
	 * 'APPLICATION', 'FILTERED', 'UNDEFINED')
	 */
	Collection<TypeRule> getTypeRuleValues() throws BusinessServiceException;
	
	/**
	 * Retrieve type rule by name
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	TypeRule getTypeRuleByName(String name) throws BusinessServiceException;

	/**
	 * Retrieve type rule by id
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	TypeRule getTypeRuleById(int id) throws BusinessServiceException;
	
	/** 
	 * Retrieves all Priority values defined in the system; Priority is an
	 * enumerated class that  represents a priority on a scale between one and
	 * ten, with 1 being the lowest and 10 being the highest.
	 */
	Collection<Priority> getPriorityValues() throws BusinessServiceException;
	
	/** 
	 * Retrieve priority by name
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	Priority getPriorityByName(String name) throws BusinessServiceException;

	/** 
	 * Retrieve priority by id
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	Priority getPriorityById(int id) throws BusinessServiceException;
	
	/** 
	 * Retrieves all OperationStatus values defined in the system;
	 * OperationStatus is an enumerated class that represents the organizational
	 * disposition of an event, currently one of 'OPEN', 'CLOSED', 'NOTIFIED' or
	 * 'ACK'
	 */
	Collection<OperationStatus> getOperationStatusValues() throws BusinessServiceException;
	
	/**
	 * Retrieve operation status by name.
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	OperationStatus getOperationStatusByName(String name) throws BusinessServiceException;
	
	/**
	 * Retrieve operation status by id.
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	OperationStatus getOperationStatusById(int id) throws BusinessServiceException;
	
	/**
     * Indicates whether a given PropertyType name corresponds to a 'built-in'
     * property of an entity; that is, it is accessible through a regular
     * getter/setter.
     * 
	 * @param entityType
	 * @param propertyTypeName
	 * @return
	 */
	boolean isBuiltInProperty(String entityType, String propertyTypeName) throws BusinessServiceException;
	

	/**
	 * Retrieve Platform by name.
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	PluginPlatform getPlatformById(int id) throws BusinessServiceException;

    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hql
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of host objects matching the query
     */
    public FoundationQueryList queryMetadata(String hql, String hqlCount, int firstResult, int maxResults);

}
