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

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.statistics.StatisticsService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class ApplicationType extends PropertyExtensibleAbstract implements com.groundwork.collage.model.ApplicationType
{	
	private static final String SEMI_COLON = ";";
	
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
								ApplicationType.ENTITY_TYPE_CODE,
								true);

	private static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(
								EP_NAME, 
								HP_NAME, 
								PropertyType.DataType.STRING, 
								ApplicationType.ENTITY_TYPE_CODE,
								true);

	private static final PropertyType PROP_DISPLAY_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(
								EP_DISPLAY_NAME, 
								HP_DISPLAY_NAME, 
								PropertyType.DataType.STRING, 
								ApplicationType.ENTITY_TYPE_CODE,
								true);

	private static final PropertyType PROP_SERVICE_COUNT = 
		new com.groundwork.collage.model.impl.PropertyType(
								EP_SERVICE_COUNT,
								null, // Leave null since there is no related hibernate property 
								PropertyType.DataType.LONG, 
								null,
								true);
	
	private static final PropertyType PROP_HOST_COUNT = 
		new com.groundwork.collage.model.impl.PropertyType(
								EP_HOST_COUNT,
								null, 
								PropertyType.DataType.LONG, 
								null,
								true);
	
	private static final PropertyType PROP_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(
								EP_DESCRIPTION,
								HP_DESCRIPTION, 
								PropertyType.DataType.STRING, 
								ApplicationType.ENTITY_TYPE_CODE,
								true);
	
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
	Log log = LogFactory.getLog(this.getClass());

	private Integer applicationTypeId;
	private String  name;
	private String  displayName;
	private String  description;
	private String 	stateTransitionCriteria; // Not exposed through entity query
	private List<String> stateTransitionFieldList;

	private Set appEntityProps;
	private SortedMap entityTypes;
	
	public Integer getApplicationTypeId() 
	{
		if (this.applicationTypeId == null)
			this.applicationTypeId = new Integer(-1);

		return this.applicationTypeId;
	}
	
	public void setApplicationTypeId(Integer applicationTypeId) {
		this.applicationTypeId = applicationTypeId;
	}
	
	public Integer getID ()
	{
		return getApplicationTypeId();
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getStateTransitionCriteria() 
	{
		return this.stateTransitionCriteria;
	}
	
	public List<String> getStateTransitionCriteriaList ()
	{
		if (this.stateTransitionFieldList != null)
			return this.stateTransitionFieldList;
			
		this.stateTransitionFieldList = new Vector<String>();
		
		if (this.stateTransitionCriteria == null || this.stateTransitionCriteria.length() == 0)
			return this.stateTransitionFieldList;
		
		StringTokenizer tokenizer = new StringTokenizer(this.stateTransitionCriteria, SEMI_COLON);
		while (tokenizer.hasMoreTokens())
		{
			this.stateTransitionFieldList.add(tokenizer.nextToken());
		}
		
		return this.stateTransitionFieldList;
	}
	
	public void setStateTransitionCriteria(String stateTransitionCriteria)
	{
		// Make sure we rebuild the state transition list
		this.stateTransitionFieldList = null;
		
		this.stateTransitionCriteria = stateTransitionCriteria;
	}
	
	public SortedMap getEntityTypes() 
	{
		if (this.entityTypes == null) {
			this.createEntityTypeMap();
		}

		return this.entityTypes;
	}

	public EntityType getEntityType(String name) {
		return (EntityType)this.getEntityTypes().get(name);
	}


	public PropertyType getPropertyType(String entityName, String propertyName) 
	{
		if (this.getEntityType(entityName) != null)
			return this.getEntityType(entityName).getPropertyType(propertyName);
		else
			return null;
	}


	public void assignPropertyType(EntityType entityType, PropertyType propertyType, int sortOrder)
	{
	    EntityPropertyBean aep = new EntityPropertyBean(entityType, propertyType);
        aep.setSortOrder(sortOrder);
	
			// if (!this.getApplicationEntityProperties().contains(aep))
	    	this.getApplicationEntityProperties().add(aep);
	
	    this.entityTypes = null; // reset map of entities
	}

	public boolean unassignPropertyType(String entityTypeName, String propertyTypeName)
	{
		EntityType     entityType = this.getEntityType(entityTypeName);
		PropertyType propertyType = this.getPropertyType(entityTypeName, propertyTypeName);

		boolean deleted = false;

		if (entityType != null && propertyType != null) 
		{
			EntityPropertyBean aep = 
				new EntityPropertyBean(entityType, propertyType);

			deleted = this.getApplicationEntityProperties().remove(aep);

			this.entityTypes = null; // reset map of entities

		}

		if (deleted)
			return true;
		else {
			log.warn("unable to remove PropertyType '" + propertyTypeName 
					+ "' from EntityType '" + entityTypeName 
					+ "' in the context of ApplicationType '" + this.getName());
			return false;
		}
	}

	/** 
	 * used by hibernate to retrieve all EntityPropertyBean associations 
	 * (ternary relationship) encapsulated in this ApplicationType - 
	 * not meant to be exposed via public interfaces
	 */
	public Set getApplicationEntityProperties() {
		if (this.appEntityProps == null)
			this.appEntityProps = new TreeSet();

		return this.appEntityProps;
	}
	
	/**
	 * used by hibernate to store all EntityPropertyBean associations 
	 * encapsulated in this ApplicationType - 
	 * not meant to be exposed via public interfaces
	 */
	private void setApplicationEntityProperties(Set appEntityProps) {
		this.appEntityProps = appEntityProps;
		this.entityTypes = null; // force a reset of the cached entityTypes map
	}	
	
	@Override
	public Object getProperty(String key) throws IllegalArgumentException
	{
	    if (key == null || key.length() == 0)
	    	throw new IllegalArgumentException("Invalid null / empty property key parameter.");
	    
	    // TODO:  May want to keep a function map and use reflection to call the appropriate method
	    // instead of an if / else statement.	    
	    if (EP_ID.equals(key))
	    {
	    	return getApplicationTypeId();
	    }
	    else if (EP_NAME.equals(key))
	    {
	    	return getName();
	    }
        else if (EP_DISPLAY_NAME.equals(key))
        {
            return getDisplayName();
        }
        else if (key.equalsIgnoreCase(EP_SERVICE_COUNT))
        {
			StatisticsService statisticService = getStatisticService();
		    
			StateStatistics stateStatistics = statisticService.getServiceStatisticTotals(); // TODO:  Filter by app id
			if (stateStatistics == null)
				return 0L;
			
			return stateStatistics.getTotalServices();
        }      
        else if (key.equalsIgnoreCase(EP_HOST_COUNT))
        {
			StatisticsService statisticService = getStatisticService();
		    
			StateStatistics stateStatistics = statisticService.getHostStatisticTotals(); // TODO:  Filter by app id
			if (stateStatistics == null)
				return 0L;
			
			return stateStatistics.getTotalHosts();
        }      	    
	    else if (key.startsWith(PREFIX_STAT_SERVICE_STATUS))
	    {
			StatisticsService statisticService = getStatisticService();
	    
			StateStatistics stateStatistics = statisticService.getServiceStatisticTotals(); // TODO:  Filter by app id
			
			if (stateStatistics == null)
				return 0L;
			
			StatisticProperty statProperty = stateStatistics.getStatisticProperty(key.substring(PREFIX_STAT_SERVICE_STATUS.length()));
			if (statProperty == null)
				return 0L;
			
			return statProperty.getCount();
	    }
	    else if (key.startsWith(PREFIX_STAT_HOST_STATUS))
	    {
			StatisticsService statisticService = getStatisticService();
	    
			StateStatistics stateStatistics = statisticService.getHostStatisticTotals(); // TODO:  Filter by app id
			
			if (stateStatistics == null)
				return 0L;
			
			StatisticProperty statProperty = stateStatistics.getStatisticProperty(key.substring(PREFIX_STAT_HOST_STATUS.length()));
			if (statProperty == null)
				return 0L;
			
			return statProperty.getCount();
	    }	    
//	    else if (key.startsWith(PREFIX_STAT_SERVICE_APP_PROP_ENABLED))
//	    {
//			StatisticsService statisticService = getStatisticService();
//	    
//			Collection<NagiosStatisticProperty> appStatistics = 
//				statisticService.getApplicationStatisticsTotals(this.getApplicationTypeId().intValue());
//			
//			Iterator<NagiosStatisticProperty> it = appStatistics.iterator();
//			NagiosStatisticProperty appStatisticProperty = null;
//			while (it.hasNext())
//			{
//				appStatisticProperty = it.next();
//				
//				if (appStatisticProperty.getPropertyName().equals(key.substring(PREFIX_STAT_SERVICE_APP_PROP_ENABLED.length())))
//					return appStatisticProperty.getServiceStatisticEnabled();
//			}
//			
//			return 0;	
//	    }        
//	    else if (key.startsWith(PREFIX_STAT_SERVICE_APP_PROP_DISABLED))
//	    {
//			StatisticsService statisticService = getStatisticService();
//		    
//			Collection<NagiosStatisticProperty> appStatistics = 
//				statisticService.getApplicationStatisticsTotals(this.getApplicationTypeId().intValue());
//			
//			Iterator<NagiosStatisticProperty> it = appStatistics.iterator();
//			NagiosStatisticProperty appStatisticProperty = null;
//			while (it.hasNext())
//			{
//				appStatisticProperty = it.next();
//				
//				if (appStatisticProperty.getPropertyName().equals(key.substring(PREFIX_STAT_SERVICE_APP_PROP_DISABLED.length())))
//					return appStatisticProperty.getServiceStatisticDisabled();
//			}
//			
//			return 0; 
//	    }
	    else {
	    	return super.getDynamicProperty(key);
	    }	    
	}

	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
		
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Build up properties based on non-volatile properties and statistic properties		
		BUILT_IN_PROPERTIES.add(PROP_SERVICE_COUNT);
		BUILT_IN_PROPERTIES.add(PROP_HOST_COUNT);
		
		// For each statistic
		StatisticsService statisticService = getStatisticService();
		
		// Add service statistic properties 
		String status = null;
		List<String> serviceStats = statisticService.getServiceStatusList();
		Iterator<String> it = serviceStats.iterator();
		while(it.hasNext())
		{
			status = it.next();
			BUILT_IN_PROPERTIES.add(new com.groundwork.collage.model.impl.PropertyType(
										PREFIX_STAT_SERVICE_STATUS + status,
										null,										
										PropertyType.DataType.LONG,
										null,
										true));	
		}
		
		// Add host statistic properties
		List<String> hostStats = statisticService.getHostStatusList();
		it = hostStats.iterator();
		while(it.hasNext())
		{
			status = it.next();
			BUILT_IN_PROPERTIES.add(new com.groundwork.collage.model.impl.PropertyType(
					PREFIX_STAT_HOST_STATUS + status,
					null,										
					PropertyType.DataType.LONG,
					null,
					true));	
		}		
				
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_NAME);
        BUILT_IN_PROPERTIES.add(PROP_DISPLAY_NAME);
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);

		return BUILT_IN_PROPERTIES;
	}

	public List<PropertyType> getComponentProperties()
	{
		if (COMPONENT_PROPERTIES != null)
			return COMPONENT_PROPERTIES;
		
		COMPONENT_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties		
		COMPONENT_PROPERTIES.add(PROP_ID);
		COMPONENT_PROPERTIES.add(PROP_NAME);
        COMPONENT_PROPERTIES.add(PROP_DISPLAY_NAME);
		COMPONENT_PROPERTIES.add(PROP_DESCRIPTION);

		return COMPONENT_PROPERTIES;
	}
	
	@Override
	public String getEntityTypeCode()
	{
		return ENTITY_TYPE_CODE;
	}

	@Override
	public PropertyValue getPropertyValueInstance(String name, Object value)
	{
		return new EntityPropertyValue(applicationTypeId, getEntityTypeId(), name, value);
	}	

	/** appends applTypeId and name */
	public String toString() 
	{
		return new ToStringBuilder(this)
			.append("id", getApplicationTypeId())
			.append("name", getName())
			.append("types", getEntityTypes())
			.toString();
	}

	/** 
	 * two instances are equal if their names are equals - 
	 * this is a draconian concept of equality which stresses that the name
	 * of an ApplicationType should be unique across the application
	 */
	public boolean equals(Object other) 
	{
		if ( (this == other ) ) return true;
		if ( !(other instanceof ApplicationType) ) return false;
		ApplicationType castOther = (ApplicationType) other;
		return new EqualsBuilder()
			.append(this.getName(), castOther.getName())
			.isEquals();
	}

	public int hashCode() 
	{
		return new HashCodeBuilder()
			.append(getName())
			.toHashCode();
	}

	/** 
	 * given a set of EntityPropertyBean, returns a SortedMap of EntityType
	 * objects, each of which is populated with the PropertyType objects that
	 * extend the EntityType - this is a utility implementation method that
	 * converts the ternary table in the database to a more business-use based
	 * representation of the relationship 
	 */
	protected void createEntityTypeMap() 
	{
		entityTypes = new TreeMap();

		EntityPropertyBean aep;
		EntityType   entity;

		for (Iterator i = this.getApplicationEntityProperties().iterator(); i.hasNext();)
		{
			aep      = (EntityPropertyBean)i.next();
			entity   = aep.getEntityType();
			if (! entityTypes.containsKey(entity.getName()))
			{
				// clone the EntityType, as the one that we retrieve from the
				// EntityPropertyBean is used as a Prototype and is shared by
				// several ApplicationTypes (and hence if we add PropertyTypes to it
				// here, they would also be in the other ApplicationTypes)
				try { 
					entity   = (EntityType)((com.groundwork.collage.model.impl.EntityType)aep.getEntityType()).clone();
				}
				catch (CloneNotSupportedException e) {
					String msg = "Unable to clone EntityType:" + entity;
					log.error(msg);
					throw new CollageException(msg,e);
				}
				entityTypes.put(entity.getName(), entity);
			}
			else
				entity = (EntityType)entityTypes.get(entity.getName());

			entity.mapPropertyType(aep.getPropertyType());

			if (! entityTypes.containsKey(entity.getName()))
				entityTypes.put(entity.getName(), entity);
		}
	}

}
