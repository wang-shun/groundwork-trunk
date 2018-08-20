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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.statistics.StatisticsService;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyExtensible;
import com.groundwork.collage.model.PropertyType;

/**
 * Abstract class that implements the PropertyExtensible interface, by
 * delegating to a {@link PropertyManager} object; all the heavy lifting is
 * done in the PropertyManager; this class is used as a convenience to provide
 * ease of implementation of new PropertyExtensible classes; if deriving from
 * this class is not desireable (perhaps because the PropertyExtensible class
 * to be implemented must derive from another class hierarchy), the contents
 * of class can easily be cut-and-pasted to provide a PropertyExtensible
 * implementation that also uses a PropertyManager delegate.
 *
 * @see PropertyManager
 * @see PropertyExtensible
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public abstract class PropertyExtensibleAbstract implements PropertyExtensible
{		
	/** Statistic Property Prefixes **/
	protected static String PREFIX_STAT_SERVICE_STATUS = "STAT_SERVICE_STATUS_";
	protected static String PREFIX_STAT_HOST_STATUS = "STAT_HOST_STATUS_";
	protected static String PREFIX_STAT_SERVICE_APP_PROP_ENABLED = "STAT_SERVICE_APP_PROP_ENABLED_";
	protected static String PREFIX_STAT_SERVICE_APP_PROP_DISABLED = "STAT_SERVICE_APP_PROP_DISABLED_";
	protected static String PREFIX_STAT_HOST_APP_PROP_ENABLED = "STAT_HOST_APP_PROP_ENABLED_";
	protected static String PREFIX_STAT_HOST_APP_PROP_DISABLED = "STAT_HOST_APP_PROP_DISABLED_";
	
	protected Set propertyValues;

	protected Log log = LogFactory.getLog(this.getClass());

	protected ApplicationType applicationType;
	
	//private PropertyManager propertyManager = new PropertyManager(this);
	protected static MetadataService metadataService;
	
	protected static StatisticsService statisticService;

    private static Map<String,Integer> entityTypeIdCache = new ConcurrentHashMap<String,Integer>();

	protected static MetadataService getMetadata()
	{
		if (metadataService == null) {
			CollageFactory collage = CollageFactory.getInstance();
			metadataService = (MetadataService)collage.getMetadataService();
		}
		
		return metadataService;
	}

	protected static StatisticsService getStatisticService() 
	{
		if (statisticService == null) {
			CollageFactory collage = CollageFactory.getInstance();
			statisticService = (StatisticsService)collage.getStatisticsService();
		}
		
		return statisticService;
	}
	
	public abstract String getEntityTypeCode();

    /**
     * Lookup entity type id for code.
     *
     * @return entity type id or null
     */
    public Integer getEntityTypeId() {
        // get entity type code
        String code = getEntityTypeCode();
        // check cache, (may contain null)
        if (entityTypeIdCache.containsKey(code)) {
            return entityTypeIdCache.get(code);
        }
        // query, cache, and return metadata driven ids
        EntityType entityType = getMetadata().getEntityTypeByName(code);
        Integer id = ((entityType != null) ? entityType.getEntityTypeId() : null);
        entityTypeIdCache.put(code, id);
        return id;
    }

	public abstract PropertyValue getPropertyValueInstance (String name, Object value);
	
	public void setProperty(String key, Object value) throws IllegalArgumentException
	{
		setDynamicProperty(key, value);
	}
	
	public Object getProperty(String key) throws IllegalArgumentException
	{
		return getDynamicProperty(key);
	}	

    public Integer getApplicationTypeId()
    {
    	if (this.applicationType == null)
    		return null;
    	
    	return this.applicationType.getApplicationTypeId();
    }
    
	public ApplicationType getApplicationType() 
	{	
		return this.applicationType;
	}

	public void setApplicationType(ApplicationType applicationType) 
	{	
		this.applicationType = applicationType;
	}

	/*
	 * all methods below are delegated to a method by the same name in the
	 * PropertyManager object 
	 */
	public Object getDynamicProperty(String propertyName) 
	{	
		if (propertyValues == null)
		{
			return null;
		}				
				
		PropertyValue propVal = null;
		Iterator it = propertyValues.iterator();
		while (it.hasNext())
		{
			propVal = (PropertyValue)it.next();			
			
			if (propVal.getName().equalsIgnoreCase(propertyName))
			{
				return propVal.getValue();
			}			
		}
		
		return null;
	}

	public void setDynamicProperty(String propertyName, Object value) 
	{			
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");

        // lookup matching property value
        PropertyValue propVal = null;
        if (propertyValues != null) {
            for (Iterator it = propertyValues.iterator(); it.hasNext();) {
                PropertyValue findPropVal = (PropertyValue) it.next();
                if (propertyName.equalsIgnoreCase(findPropVal.getName())) {
                    propVal = findPropVal;
                }
            }
        }

        if (value != null) {
            // add/set property value
            if (propertyValues == null) {
                propertyValues = new HashSet();
            }
            if (propVal == null) {
                propVal = getPropertyValueInstance(propertyName, value);
                propertyValues.add(propVal);
            }
            propVal.setValue(value);
        } else {
            // remove/delete property value
            if (propVal != null) {
                propertyValues.remove(propVal);
            }
        }
	}

	public Object get(String propertyName) 
	{			
		return this.getProperty(propertyName);
	}

	public void set(String propertyName, Object value) 
	{	
		this.setProperty(propertyName, value);
	}

	// Override in individual entity classes
	public abstract List<PropertyType> getBuiltInProperties();
	
	public Map<String, Object> getProperties(boolean dynamicOnly) 
	{	
		Map<String, Object> properties = new HashMap<String, Object>();

		if (dynamicOnly == false)
		{
			List<PropertyType> builtInProperties = getBuiltInProperties();			
			if (builtInProperties != null && builtInProperties.size() > 0)
			{
				PropertyType propType = null;
				
				// Include built-in property values
				Iterator<PropertyType> it = builtInProperties.iterator();
				while (it.hasNext())
				{
					propType = it.next();
					properties.put(propType.getName(), getProperty(propType.getName()));
				}
			}
		}

		// Add dynamic properties
		if (propertyValues != null)
		{				
			PropertyValue propVal = null;
			Iterator it = propertyValues.iterator();
			while (it.hasNext())
			{
				propVal = (PropertyValue)it.next();
				
				properties.put(propVal.getName(), propVal.getValue());						
			}
		}
		
		return properties;
	}

	public void setProperties(Map properties)
	{
		if (properties != null)
		{
			String key   = null;
			Object value = null;
			for (Iterator i = properties.keySet().iterator(); i.hasNext();)
			{								
				key   = (String)i.next();
				value = properties.get(key);

                // empty strings are assumed to be null since Properties are
                // typically used to set properties and cannot have null values;
                // note that null values delete dynamic property values on set
                if ((value instanceof String) && (((String)value).length() == 0)) {
                    value = null;
                }
								
				// this is necessary because it is likely that setProperty is 
				// overriden on the objectExtended to accomodate declared getter/setters
				this.setProperty(key, value);
			} // end for
		}
	}

	public void setProperties(Properties properties) 
	{
		this.setProperties(properties);
	}

	public PropertyType getPropertyType(String propertyName) 
	{
		return (PropertyType)this.getPropertyTypes().get(propertyName);		
	}

	public Map getPropertyTypes() 
	{
		String entityCode = getEntityTypeCode();
		
		ApplicationType appType = getApplicationType();
		if (appType == null) 
		{
			return new TreeMap();  // Empty Tree Map
		}
		
		EntityType entityType = appType.getEntityType(entityCode);		
		if (entityType == null)
		{
			return new TreeMap(); // Empty Tree Map
		}
		
		return entityType.getPropertyTypes();
	}
	
	public boolean hasPropertyType(String propertyName) 
	{
		return this.getPropertyType(propertyName) != null;		
	}

	public Set getPropertyValues() 
	{
		return propertyValues;
	}

	public void setPropertyValues(Set propertyValues) 
	{
		this.propertyValues = (Set)propertyValues;	
	}
}
