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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MetadataServiceImpl extends EntityBusinessServiceImpl implements
		MetadataService {
	/** Default Sort Criteria */
	private static final SortCriteria SORT_CRITERIA_NAME = SortCriteria
			.asc(PROP_NAME);

	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(MetadataServiceImpl.class);

	/* Cached Metadata */
	private static Map<String, ApplicationType> APP_TYPES_BY_NAME = null;
	private static Map<Integer, ApplicationType> APP_TYPES_BY_ID = null;;

	private static Map<String, EntityType> ENTITY_TYPES_BY_NAME = null;
	private static Map<Integer, EntityType> ENTITY_TYPES_BY_ID = null;

	private static Map<String, PropertyType> PROPERTY_TYPES_BY_NAME = null;
	private static Map<Integer, PropertyType> PROPERTY_TYPES_BY_ID = null;

	private static Map<String, MonitorStatus> MONITOR_STATUS_BY_NAME = null;
	private static Map<Integer, MonitorStatus> MONITOR_STATUS_BY_ID = null;

	private static Map<String, StateType> STATE_TYPES_BY_NAME = null;
	private static Map<Integer, StateType> STATE_TYPES_BY_ID = null;

	private static Map<String, CheckType> CHECK_TYPES_BY_NAME = null;
	private static Map<Integer, CheckType> CHECK_TYPES_BY_ID = null;

	private static Map<String, Severity> SEVERITY_BY_NAME = null;
	private static Map<Integer, Severity> SEVERITY_BY_ID = null;

	private static Map<String, Component> COMPONENTS_BY_NAME = null;
	private static Map<Integer, Component> COMPONENTS_BY_ID = null;

	private static Map<String, TypeRule> TYPE_RULES_BY_NAME = null;
	private static Map<Integer, TypeRule> TYPE_RULES_BY_ID = null;

	private static Map<String, Priority> PRIORITIES_BY_NAME = null;
	private static Map<Integer, Priority> PRIORITIES_BY_ID = null;

	private static Map<String, OperationStatus> OPERATION_STATUS_BY_NAME = null;
	private static Map<Integer, OperationStatus> OPERATION_STATUS_BY_ID = null;

	private static Map<String, PluginPlatform> PLUGIN_PLATFORM_BY_NAME = null;
	private static Map<Integer, PluginPlatform> PLUGIN_PLATFORM_BY_ID = null;

	public MetadataServiceImpl(FoundationDAO foundationDAO) {
		super(foundationDAO, ApplicationType.INTERFACE_NAME,
				ApplicationType.COMPONENT_NAME);
	}

	public ApplicationType createApplicationType()
			throws BusinessServiceException {
		return (ApplicationType) create();
	}

	public ApplicationType createApplicationType(String name, String description)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty application type name parameter.");

		ApplicationType appType = (ApplicationType) create();
		appType.setName(name);
		appType.setDescription(description);

		return appType;
	}

	public void deleteApplicationType(ApplicationType applicationType)
			throws BusinessServiceException {
		if (applicationType == null)
			throw new IllegalArgumentException(
					"Invalid null ApplicationType parameter.");

		// Remove by id so cache will be updated.
		deleteApplicationTypeById(applicationType.getApplicationTypeId()
				.intValue());
	}

	public void deleteApplicationTypeById(int applicationTypeId)
			throws BusinessServiceException {
        // attempt delete
		delete(applicationTypeId);
        // flush transaction before deleting from cache
        _foundationDAO.flush();

		// Update Cache
		if (APP_TYPES_BY_NAME != null) {
			ApplicationType appType = APP_TYPES_BY_ID.remove(applicationTypeId);
			if (appType != null)
				APP_TYPES_BY_NAME.remove(appType.getName());
		}
	}

	public boolean deleteApplicationTypeByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty application type name parameter.");

		// Get application type
		ApplicationType appType = getApplicationTypeByName(name);
		if (appType == null) {
			if (log.isWarnEnabled() == true)
				log.warn("Unable to delete application type - Not Found - "
						+ name);
			return false;
		}

		// Remove by id so cache will be updated.
		deleteApplicationTypeById(appType.getApplicationTypeId().intValue());
        return true;
	}

	public synchronized ApplicationType getApplicationTypeById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (APP_TYPES_BY_ID == null)
			loadEntityCache(ApplicationType.COMPONENT_NAME);

		// Retrieve from cache
		return APP_TYPES_BY_ID.get(id);
	}

	public synchronized ApplicationType     getApplicationTypeByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty application type name parameter.");

		// Load Cache, if necessary
		if (APP_TYPES_BY_NAME == null) {
			loadEntityCache(ApplicationType.COMPONENT_NAME);
		}

		// Retrieve from cache
		return APP_TYPES_BY_NAME.get(name);
	}

	public FoundationQueryList getApplicationTypes(
			FilterCriteria filterCriteria, SortCriteria sortCriteria,
			int firstResult, int maxResults) throws BusinessServiceException {
		return query(filterCriteria, sortCriteria, firstResult, maxResults);
	}

	public List<ApplicationEntityProperty> getApplicationEntityProperties(
			String entityType, String appType, boolean bComponentProperties)
			throws BusinessServiceException {
		if (entityType == null || entityType.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty entity type parameter.");

		// Make sure its a valid entity
		EntityType type = getEntityTypeByName(entityType);
		if (type == null)
			throw new BusinessServiceException("Unknown entity type ["
					+ entityType + "]");

		List<ApplicationEntityProperty> propertyList = new ArrayList<ApplicationEntityProperty>(
				15);
		FoundationQueryList results = null;

		// Query specific application type entity properties. If the application
		// type provided is 'SYSTEM' then only the
		// built-in properties are returned.
		// NOTE: If bComponentProperties = true then we only return the
		// filterable hibernate properties
		if (appType != null
				&& appType.length() > 0) {
			FilterCriteria filter = FilterCriteria.eq(
					ApplicationEntityProperty.HP_ENTITY_TYPE_NAME, entityType);
			FilterCriteria filterAppType = FilterCriteria
					.eq(ApplicationEntityProperty.HP_APPLICATION_TYPE_NAME,
							appType);
			filter.and(filterAppType);

			try {
				results = _foundationDAO
						.query(
								ApplicationEntityProperty.COMPONENT_NAME,
								filter,
								SortCriteria
										.asc(ApplicationEntityProperty.HP_PROPERTY_TYPE_NAME),
								null, -1, -1);

				if (results != null)
					propertyList
							.addAll((List<ApplicationEntityProperty>) results
									.getResults());

			} catch (Exception e) {
				throw new BusinessServiceException(e);
			}
		}

		// Add Built-In Properties - TODO: Possibly cache these properties
		try {

			List<PropertyType> builtInProperties = null;

			if (bComponentProperties == false) {
				builtInProperties = type.getBuiltInProperties();

				if (builtInProperties != null) {
					Iterator<PropertyType> it = builtInProperties.iterator();
					while (it.hasNext()) {
						propertyList
								.add(new com.groundwork.collage.model.impl.ApplicationEntityProperty(
										getApplicationTypeByName(ApplicationType.SYSTEM_APPLICATION_TYPE_NAME),
										type, it.next()));
					}
				}
			} // end if
		} catch (Exception e) {
			throw new BusinessServiceException(e);
		}

		// Sort Property List
		Collections.sort(propertyList);

		return propertyList;
	}

	/**
	 * Returns entity information in the form of EntityPropertyValue instances.
	 * 
	 * @param entityType
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	public FoundationQueryList performEntityQuery(String entityType,
			FilterCriteria filterCriteria, SortCriteria sortCriteria,
			int firstResult, int maxResults) throws BusinessServiceException {
		if (entityType == null || entityType.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty entity type parameter.");

		// Get Statistics Service
		StatisticsService statisticService = (StatisticsService) create(StatisticsService.INTERFACE_NAME);

		// Lookup Entity Type
		EntityType type = getEntityTypeByName(entityType);
		if (type == null)
			throw new BusinessServiceException("Unknown entity type ["
					+ entityType + "]");

		try {
			// Physical / Hibernate entity
			if (type.getLogicalEntity() == false) {
				return _foundationDAO.query(
						type.getDescription(), // Description is component name
						filterCriteria, sortCriteria, null, firstResult,
						maxResults);
			} else // Logical / Statistic entity
			{
				Map<String, Object> parameters = null;

				if (filterCriteria != null)
					parameters = filterCriteria.getPropertyValuePairs();

				return statisticService.getStatistics(type, parameters);
			}
		} catch (Exception e) {
			throw new BusinessServiceException(e);
		}
	}

	public int performEntityCountQuery(String entityType,
			FilterCriteria filterCriteria) throws BusinessServiceException {
		if (entityType == null || entityType.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty entity type parameter.");

		// Lookup Entity Type
		EntityType type = getEntityTypeByName(entityType);
		if (type == null)
			throw new BusinessServiceException("Unknown entity type ["
					+ entityType + "]");

		try {
			return _foundationDAO.queryCount(type.getDescription(), // Description
					// is
					// component
					// name
					filterCriteria);
		} catch (Exception e) {
			throw new BusinessServiceException(e);
		}
	}

	public synchronized Collection<CheckType> getCheckTypeValues()
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (CHECK_TYPES_BY_NAME == null)
			loadEntityCache(CheckType.COMPONENT_NAME);

		// Retrieve from cache
		return CHECK_TYPES_BY_NAME.values();
	}

	public synchronized Collection<Component> getComponentValues()
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (COMPONENTS_BY_NAME == null)
			loadEntityCache(Component.COMPONENT_NAME);

		// Retrieve from cache
		return COMPONENTS_BY_NAME.values();
	}

	public synchronized EntityType getEntityTypeById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (ENTITY_TYPES_BY_ID == null)
			loadEntityCache(EntityType.COMPONENT_NAME);

		// Retrieve from cache
		return ENTITY_TYPES_BY_ID.get(id);
	}

	public synchronized EntityType getEntityTypeByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty entity type name parameter.");

		// Load Cache, if necessary
		if (ENTITY_TYPES_BY_NAME == null)
			loadEntityCache(EntityType.COMPONENT_NAME);

		// Retrieve from cache
		return ENTITY_TYPES_BY_NAME.get(name);
	}

	public synchronized FoundationQueryList getEntityTypes(
			FilterCriteria filterCriteria, SortCriteria sortCriteria,
			int firstResult, int maxResults) throws BusinessServiceException {
		if (sortCriteria == null)
			sortCriteria = SORT_CRITERIA_NAME;

		try {
			return _foundationDAO
					.query(EntityType.COMPONENT_NAME, filterCriteria,
							sortCriteria, null, firstResult, maxResults);
		} catch (Exception e) {
			throw new BusinessServiceException(e);
		}
	}

	public synchronized Collection<MonitorStatus> getMonitorStatusValues()
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (MONITOR_STATUS_BY_NAME == null)
			loadEntityCache(MonitorStatus.COMPONENT_NAME);

		// Retrieve from cache
		return MONITOR_STATUS_BY_NAME.values();
	}

	public synchronized MonitorStatus getMonitorStatusByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty monitor status name parameter.");

		// Load Cache, if necessary
		if (MONITOR_STATUS_BY_NAME == null)
			loadEntityCache(MonitorStatus.COMPONENT_NAME);

		// Retrieve from cache
		return MONITOR_STATUS_BY_NAME.get(name);
	}

	/**
	 * Retrieve Platform by name.
	 * 
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	public synchronized PluginPlatform getPlatformById(int id)
			throws BusinessServiceException {
		if (id <= 0)
			throw new IllegalArgumentException("Invalid plaform id parameter.");

		// Load Cache, if necessary
		if (PLUGIN_PLATFORM_BY_ID == null)
			loadEntityCache(PluginPlatform.COMPONENT_NAME);

		// Retrieve from cache
		return PLUGIN_PLATFORM_BY_ID.get(id);
	}

	/**
	 * Returns the specified MonitorStatus
	 * 
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	public synchronized MonitorStatus getMonitorStatusById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (MONITOR_STATUS_BY_ID == null)
			loadEntityCache(MonitorStatus.COMPONENT_NAME);

		// Retrieve from cache
		return MONITOR_STATUS_BY_ID.get(id);
	}

	public synchronized CheckType getCheckTypeById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (CHECK_TYPES_BY_ID == null)
			loadEntityCache(CheckType.COMPONENT_NAME);

		// Retrieve from cache
		return CHECK_TYPES_BY_ID.get(id);
	}

	public synchronized Component getComponentById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (COMPONENTS_BY_ID == null)
			loadEntityCache(Component.COMPONENT_NAME);

		// Retrieve from cache
		return COMPONENTS_BY_ID.get(id);
	}

	public synchronized OperationStatus getOperationStatusById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (OPERATION_STATUS_BY_ID == null)
			loadEntityCache(OperationStatus.COMPONENT_NAME);

		// Retrieve from cache
		return OPERATION_STATUS_BY_ID.get(id);
	}

	public synchronized Priority getPriorityById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (PRIORITIES_BY_ID == null)
			loadEntityCache(Priority.COMPONENT_NAME);

		// Retrieve from cache
		return PRIORITIES_BY_ID.get(id);
	}

	public synchronized Severity getSeverityById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (SEVERITY_BY_ID == null)
			loadEntityCache(Severity.COMPONENT_NAME);

		// Retrieve from cache
		return SEVERITY_BY_ID.get(id);
	}

	public synchronized StateType getStateTypeById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (STATE_TYPES_BY_ID == null)
			loadEntityCache(StateType.COMPONENT_NAME);

		// Retrieve from cache
		return STATE_TYPES_BY_ID.get(id);
	}

	public synchronized TypeRule getTypeRuleById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (TYPE_RULES_BY_ID == null)
			loadEntityCache(TypeRule.COMPONENT_NAME);

		// Retrieve from cache
		return TYPE_RULES_BY_ID.get(id);
	}

	public synchronized Collection<OperationStatus> getOperationStatusValues()
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (OPERATION_STATUS_BY_NAME == null)
			loadEntityCache(OperationStatus.COMPONENT_NAME);

		// Retrieve from cache
		return OPERATION_STATUS_BY_NAME.values();
	}

	public synchronized Collection<Priority> getPriorityValues()
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (PRIORITIES_BY_NAME == null)
			loadEntityCache(Priority.COMPONENT_NAME);

		// Retrieve from cache
		return PRIORITIES_BY_NAME.values();
	}

	public synchronized PropertyType getPropertyTypeById(int id)
			throws BusinessServiceException {
		// Load Cache, if necessary
		if (PROPERTY_TYPES_BY_ID == null)
			loadEntityCache(PropertyType.COMPONENT_NAME);

		// Retrieve from cache
		return PROPERTY_TYPES_BY_ID.get(id);
	}

	public synchronized PropertyType getPropertyTypeByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty property type name parameter.");

		// Load Cache, if necessary
		if (PROPERTY_TYPES_BY_NAME == null)
			loadEntityCache(PropertyType.COMPONENT_NAME);

		// Retrieve from cache
		return PROPERTY_TYPES_BY_NAME.get(name);
	}

	public FoundationQueryList getPropertyTypes(FilterCriteria filterCriteria,
			SortCriteria sortCriteria, int firstResult, int maxResults)
			throws BusinessServiceException {
		if (sortCriteria == null)
			sortCriteria = SORT_CRITERIA_NAME;

		try {
			return _foundationDAO.query(PropertyType.COMPONENT_NAME,
					filterCriteria, SORT_CRITERIA_NAME, null, firstResult,
					maxResults);
		} catch (Exception e) {
			throw new BusinessServiceException(e);
		}
	}

	/**
	 * Creates a new instance of PropertyType if it does not exist and Persists
	 * it. If the PropertyType does exist it is updated.
	 * 
	 * @param name
	 * @param description
	 * @param primitiveType
	 * @return
	 * @throws BusinessServiceException
	 */
	public void savePropertyType(String name, String description,
			String primitiveType) throws BusinessServiceException {
		if (log.isDebugEnabled())
			log.debug("attempting to create or update  PropertyType '" + name
					+ "'");

		// Check to see if it exists
		PropertyType propType = getPropertyTypeByName(name);

		// a new property that does not already exist
		if (propType == null) {
			propType = (PropertyType) create(PropertyType.INTERFACE_NAME);
			propType.setName(name);
		}

		propType.setDescription(description);
		propType.setPrimitiveType(primitiveType);

		save(propType);

		// Update cache
		if (PROPERTY_TYPES_BY_NAME != null) {
			PROPERTY_TYPES_BY_NAME.put(propType.getName(), propType);
			PROPERTY_TYPES_BY_ID.put(propType.getPropertyTypeId(), propType);
		}

		if (log.isInfoEnabled())
			log.info("successfully created or updated  PropertyType - " + name);
	}

	public boolean deletePropertyTypeByName(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / emtpy property type name parameter.");

		// Retrieve property type
		PropertyType propertyType = getPropertyTypeByName(name);
		if (propertyType != null) {
            // attempt delete
			delete(propertyType);
            // flush transaction before deleting from cache
            _foundationDAO.flush();

			// Update cache
			if (PROPERTY_TYPES_BY_NAME != null) {
				PROPERTY_TYPES_BY_NAME.remove(propertyType.getName());
				PROPERTY_TYPES_BY_ID.remove(propertyType.getPropertyTypeId());
			}
            return true;
		}
        return false;
	}

	public synchronized Collection<Severity> getSeverityValues()
			throws BusinessServiceException {
		if (SEVERITY_BY_NAME == null)
			loadEntityCache(Severity.COMPONENT_NAME);

		return SEVERITY_BY_NAME.values();
	}

	public synchronized Collection<StateType> getStateTypeValues()
			throws BusinessServiceException {
		if (STATE_TYPES_BY_NAME == null)
			loadEntityCache(StateType.COMPONENT_NAME);

		return STATE_TYPES_BY_NAME.values();
	}

	public synchronized Collection<TypeRule> getTypeRuleValues()
			throws BusinessServiceException {
		if (TYPE_RULES_BY_NAME == null)
			loadEntityCache(TypeRule.COMPONENT_NAME);

		return TYPE_RULES_BY_NAME.values();
	}

	public synchronized CheckType getCheckTypeByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty check type name parameter.");

		// Load Cache, if necessary
		if (CHECK_TYPES_BY_NAME == null)
			loadEntityCache(CheckType.COMPONENT_NAME);

		// Retrieve from cache
		return CHECK_TYPES_BY_NAME.get(name);
	}

	public synchronized Component getComponentByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty component name parameter.");

		// Load Cache, if necessary
		if (COMPONENTS_BY_NAME == null)
			loadEntityCache(Component.COMPONENT_NAME);

		// Retrieve from cache
		return COMPONENTS_BY_NAME.get(name);
	}

	public synchronized OperationStatus getOperationStatusByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty operation status name parameter.");

		// Load Cache, if necessary
		if (OPERATION_STATUS_BY_NAME == null)
			loadEntityCache(OperationStatus.COMPONENT_NAME);

		// Retrieve from cache
		return OPERATION_STATUS_BY_NAME.get(name);
	}

	public synchronized Priority getPriorityByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty severity name parameter.");

		// Load Cache, if necessary
		if (PRIORITIES_BY_NAME == null)
			loadEntityCache(Priority.COMPONENT_NAME);

		// Retrieve from cache
		return PRIORITIES_BY_NAME.get(name);
	}

	public synchronized Severity getSeverityByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty severity name parameter.");

		// Load Cache, if necessary
		if (SEVERITY_BY_NAME == null)
			loadEntityCache(Severity.COMPONENT_NAME);

		// Retrieve from cache
		return SEVERITY_BY_NAME.get(name);
	}

	public synchronized StateType getStateTypeByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty state type name parameter.");

		// Load Cache, if necessary
		if (STATE_TYPES_BY_NAME == null)
			loadEntityCache(StateType.COMPONENT_NAME);

		// Retrieve from cache
		return STATE_TYPES_BY_NAME.get(name);
	}

	public synchronized TypeRule getTypeRuleByName(String name)
			throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty type rule name parameter.");

		// Load Cache, if necessary
		if (TYPE_RULES_BY_NAME == null)
			loadEntityCache(TypeRule.COMPONENT_NAME);

		// Retrieve from cache
		return TYPE_RULES_BY_NAME.get(name);
	}

	/*
	 * indicates whether a given PropertyType name corresponds to a 'built-in'
	 * property of an entity; that is, it is accessible through a regular
	 * getter/setter
	 */
	public boolean isBuiltInProperty(String entityType, String propertyTypeName)
			throws BusinessServiceException {
		List<ApplicationEntityProperty> list = getApplicationEntityProperties(
				entityType, ApplicationType.SYSTEM_APPLICATION_TYPE_NAME, true);

		return (list != null && list.size() > 0);
	}

	public void saveApplicationType(ApplicationType applicationType)
			throws BusinessServiceException {
		if (applicationType == null)
			throw new IllegalArgumentException(
					"Invalid null ApplicationType parameter.");

		save(applicationType);

		// Update Cache
		if (APP_TYPES_BY_NAME != null) {
			APP_TYPES_BY_NAME.put(applicationType.getName(), applicationType);
			APP_TYPES_BY_ID.put(applicationType.getApplicationTypeId(),
					applicationType);
		}
	}

	public synchronized ApplicationType saveApplicationType(String name,
			String description) throws BusinessServiceException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty application name parameter.");

		// Check to see if application type exists
		if (APP_TYPES_BY_NAME == null)
			loadEntityCache(ApplicationType.COMPONENT_NAME);

		ApplicationType appType = APP_TYPES_BY_NAME.get(name);

		// Create new Application type and persist
		boolean bNew = false;
		if (appType == null) {
			appType = createApplicationType(name, description);
			bNew = true;
		} else
			appType.setDescription(description);

		// Persist changes
		saveApplicationType(appType);

		// update cache
		if (bNew == true && APP_TYPES_BY_NAME != null) {
			APP_TYPES_BY_NAME.put(appType.getName(), appType);
			APP_TYPES_BY_ID.put(appType.getApplicationTypeId(), appType);
		}

		return appType;
	}

	private void loadEntityCache(String entityTypeName) {
		if (entityTypeName == null || entityTypeName.length() == 0)
			throw new IllegalArgumentException(
					"Unable to load entity cache - Invalid null / mepty entity type name parameter.");

		List list = _foundationDAO.query(entityTypeName, null,
				SORT_CRITERIA_NAME);
		int size = (list == null) ? 0 : list.size();

		if (ApplicationType.COMPONENT_NAME.equals(entityTypeName)) {
			APP_TYPES_BY_NAME = new HashMap<String, ApplicationType>(size);
			APP_TYPES_BY_ID = new HashMap<Integer, ApplicationType>(size);

			ApplicationType appType = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				appType = (ApplicationType) it.next();
				APP_TYPES_BY_NAME.put(appType.getName(), appType);
				APP_TYPES_BY_ID.put(appType.getID(), appType);
			}
		} else if (CheckType.COMPONENT_NAME.equals(entityTypeName)) {
			CHECK_TYPES_BY_NAME = new HashMap<String, CheckType>(size);
			CHECK_TYPES_BY_ID = new HashMap<Integer, CheckType>(size);

			CheckType checkType = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				checkType = (CheckType) it.next();
				CHECK_TYPES_BY_NAME.put(checkType.getName(), checkType);
				CHECK_TYPES_BY_ID.put(checkType.getID(), checkType);
			}
		} else if (Component.COMPONENT_NAME.equals(entityTypeName)) {
			COMPONENTS_BY_NAME = new HashMap<String, Component>(size);
			COMPONENTS_BY_ID = new HashMap<Integer, Component>(size);

			Component component = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				component = (Component) it.next();
				COMPONENTS_BY_NAME.put(component.getName(), component);
				COMPONENTS_BY_ID.put(component.getID(), component);
			}
		} else if (EntityType.COMPONENT_NAME.equals(entityTypeName)) {
			ENTITY_TYPES_BY_NAME = new HashMap<String, EntityType>(size);
			ENTITY_TYPES_BY_ID = new HashMap<Integer, EntityType>(size);

			EntityType entityType = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				entityType = (EntityType) it.next();
				ENTITY_TYPES_BY_NAME.put(entityType.getName(), entityType);
				ENTITY_TYPES_BY_ID
						.put(entityType.getEntityTypeId(), entityType);
			}
		} else if (MonitorStatus.COMPONENT_NAME.equals(entityTypeName)) {
			MONITOR_STATUS_BY_NAME = new HashMap<String, MonitorStatus>(size);
			MONITOR_STATUS_BY_ID = new HashMap<Integer, MonitorStatus>(size);

			MonitorStatus monitorStatus = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				monitorStatus = (MonitorStatus) it.next();
				MONITOR_STATUS_BY_NAME.put(monitorStatus.getName(),
						monitorStatus);
				MONITOR_STATUS_BY_ID.put(monitorStatus.getID(), monitorStatus);
			}
		} else if (OperationStatus.COMPONENT_NAME.equals(entityTypeName)) {
			OPERATION_STATUS_BY_NAME = new HashMap<String, OperationStatus>(
					size);
			OPERATION_STATUS_BY_ID = new HashMap<Integer, OperationStatus>(size);

			OperationStatus opStatus = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				opStatus = (OperationStatus) it.next();
				OPERATION_STATUS_BY_NAME.put(opStatus.getName(), opStatus);
				OPERATION_STATUS_BY_ID.put(opStatus.getID(), opStatus);
			}
		} else if (Priority.COMPONENT_NAME.equals(entityTypeName)) {
			PRIORITIES_BY_NAME = new HashMap<String, Priority>(size);
			PRIORITIES_BY_ID = new HashMap<Integer, Priority>(size);

			Priority priority = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				priority = (Priority) it.next();
				PRIORITIES_BY_NAME.put(priority.getName(), priority);
				PRIORITIES_BY_ID.put(priority.getID(), priority);
			}
		} else if (PropertyType.COMPONENT_NAME.equals(entityTypeName)) {
			PROPERTY_TYPES_BY_NAME = new HashMap<String, PropertyType>(size);
			PROPERTY_TYPES_BY_ID = new HashMap<Integer, PropertyType>(size);

			PropertyType propertyType = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				propertyType = (PropertyType) it.next();
				PROPERTY_TYPES_BY_NAME
						.put(propertyType.getName(), propertyType);
				PROPERTY_TYPES_BY_ID.put(propertyType.getPropertyTypeId(),
						propertyType);
			}
		} else if (Severity.COMPONENT_NAME.equals(entityTypeName)) {
			SEVERITY_BY_NAME = new HashMap<String, Severity>(size);
			SEVERITY_BY_ID = new HashMap<Integer, Severity>(size);

			Severity severity = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				severity = (Severity) it.next();
				SEVERITY_BY_NAME.put(severity.getName(), severity);
				SEVERITY_BY_ID.put(severity.getID(), severity);
			}
		} else if (StateType.COMPONENT_NAME.equals(entityTypeName)) {
			STATE_TYPES_BY_NAME = new HashMap<String, StateType>(size);
			STATE_TYPES_BY_ID = new HashMap<Integer, StateType>(size);

			StateType stateType = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				stateType = (StateType) it.next();
				STATE_TYPES_BY_NAME.put(stateType.getName(), stateType);
				STATE_TYPES_BY_ID.put(stateType.getID(), stateType);
			}
		} else if (TypeRule.COMPONENT_NAME.equals(entityTypeName)) {
			TYPE_RULES_BY_NAME = new HashMap<String, TypeRule>(size);
			TYPE_RULES_BY_ID = new HashMap<Integer, TypeRule>(size);

			TypeRule typeRule = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				typeRule = (TypeRule) it.next();
				TYPE_RULES_BY_NAME.put(typeRule.getName(), typeRule);
				TYPE_RULES_BY_ID.put(typeRule.getID(), typeRule);
			}
		} else if (PluginPlatform.COMPONENT_NAME.equals(entityTypeName)) {
			PLUGIN_PLATFORM_BY_NAME = new HashMap<String, PluginPlatform>(size);
			PLUGIN_PLATFORM_BY_ID = new HashMap<Integer, PluginPlatform>(size);

			PluginPlatform plugin = null;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				plugin = (PluginPlatform) it.next();
				PLUGIN_PLATFORM_BY_NAME.put(plugin.getName(), plugin);
				PLUGIN_PLATFORM_BY_ID.put(plugin.getID(), plugin);
			}
		}
	}

    public FoundationQueryList queryMetadata(String hql, String hqlCount, int firstResult, int maxResults) {
        FoundationQueryList list= _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
        return list;
    }

}
