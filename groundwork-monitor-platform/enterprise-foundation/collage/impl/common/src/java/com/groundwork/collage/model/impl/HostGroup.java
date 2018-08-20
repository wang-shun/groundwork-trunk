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

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.groundwork.foundation.bs.statistics.StatisticsService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class HostGroup extends PropertyExtensibleAbstract implements Serializable,
        com.groundwork.collage.model.HostGroup
{
    private static final long serialVersionUID = 1;

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
								HostGroup.ENTITY_TYPE_CODE,
								true);

	private static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_NAME,
								HP_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								HostGroup.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DESCRIPTION,
								HP_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								HostGroup.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_ALIAS = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_ALIAS,
								HP_ALIAS, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								HostGroup.ENTITY_TYPE_CODE,
								true);

    private static final PropertyType PROP_AGENTID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_AGENT_ID,
                    HP_AGENT_ID, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    HostGroup.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_APPLICATION_TYPE_ID =
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_APPLICATION_TYPE_ID,
								HP_APPLICATION_TYPE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								ApplicationType.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_APPLICATION_TYPE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_APPLICATION_TYPE_NAME,
								HP_APPLICATION_TYPE_NAME, // Description is hibernate property name
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
								null, // Leave null since there is no related hibernate property 
								PropertyType.DataType.LONG, 
								null,
								true);
		
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
    /** identifier field */
    private Integer hostGroupId;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String description;
    
    /** nullable persistent field */
    private String alias;

    private String agentId;

    /* Association table HostGroupCollections
         * Contains a list of Hosts for this HostGroup
         */
    private Set hosts;
    
    /** persistent field **/
    private ApplicationType applicationType;    
        
    /** full constructor */
    public HostGroup(Integer hostGroupId, String name, String description, String alias)
    {
        this.hostGroupId = hostGroupId;
        this.name = name;
        this.description = description;
        this.alias = alias;
     }

    /** default constructor */
    public HostGroup()
    {
    }

    /** minimal constructor */
    public HostGroup(Integer hostGroupId)
    {
        this.hostGroupId = hostGroupId;
    }
    
	@Override
	public String getEntityTypeCode()
	{
		return com.groundwork.collage.model.HostGroup.ENTITY_TYPE_CODE;
	}

	@Override
	public PropertyValue getPropertyValueInstance(String name, Object value)
	{
		return new EntityPropertyValue(hostGroupId, getEntityTypeId(), name, value);
	}      

    public Integer getHostGroupId()
    {
        return this.hostGroupId;
    }

    public void setHostGroupId(Integer hostGroupId)
    {
        this.hostGroupId = hostGroupId;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getAlias()
    {
    		return this.alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Integer getApplicationTypeId()
    {
    	if (this.applicationType == null)
    		return null;
    	
    	return this.applicationType.getApplicationTypeId();
    }
    
    public ApplicationType getApplicationType()
    {
    	return  this.applicationType;
    }
    
    public void setApplicationType(ApplicationType applicationType)
    {
    	this.applicationType = applicationType;
    }

    public void addHost(Host host)
    {
        // Create a new set
        if (this.hosts == null)
            this.hosts = new HashSet();
        
        // Add the host to the HostGroupCollection
        if ( this.hosts != null && host != null)
            this.hosts.add(host);
    }
    
    public void removeHost(Host host)
    {
        this.hosts.remove(host);
    }

    /**
     * @return hosts in HostGroup.
     */
    public Set getHosts()
    {
    	if (this.hosts == null)
    		this.hosts = new HashSet();
    	
        return this.hosts;        
    }

    /**
     * @param hosts The hosts to set.
     */
    public void setHosts(Set hosts) {
        this.hosts = hosts;
    }
    
     /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to get the value of one
     * of the named property getters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key == null || key.length() == 0)
    	{
    		throw new IllegalArgumentException("Invalid null / empty property key.");
    	}
    	
        if (key.equals(EP_ID)) {
            return this.getHostGroupId();
        }
        else if (key.equals(EP_NAME)) {
            return this.getName();
        }
        else if (key.equals(EP_DESCRIPTION)) {
            return this.getDescription();
        }
        else if (key.equals(EP_APPLICATION_TYPE_NAME)) 
        {
        	ApplicationType appType = this.getApplicationType();
        	
        	if (appType == null)
        		return null;
        	
        	return appType.getName();
        }  
        else if (key.equals(EP_APPLICATION_TYPE_ID)) 
        {
        	return this.getApplicationTypeId();
        }  	   
        else if (key.equalsIgnoreCase(EP_SERVICE_COUNT)) 
        {
			StatisticsService statisticService = getStatisticService();
		    
			StateStatistics stateStatistics = statisticService.getServiceStatisticsByHostGroupName(getName());
			if (stateStatistics == null)
				return 0L;
			
			return stateStatistics.getTotalServices();
        }      
        else if (key.equalsIgnoreCase(EP_HOST_COUNT)) 
        {
			StatisticsService statisticService = getStatisticService();
		    
			StateStatistics stateStatistics = statisticService.getHostStatisticsByHostGroupName(getName());
			if (stateStatistics == null)
				return 0L;
			
			return stateStatistics.getTotalHosts();
        }      	    
	    else if (key.startsWith(PREFIX_STAT_SERVICE_STATUS))
	    {
			StatisticsService statisticService = getStatisticService();
		    
			StateStatistics stateStatistics = statisticService.getServiceStatisticsByHostGroupName(getName());
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
		    
			StateStatistics stateStatistics = statisticService.getHostStatisticsByHostGroupName(getName());
			if (stateStatistics == null)
				return 0L;
			
			StatisticProperty statProperty = stateStatistics.getStatisticProperty(key.substring(PREFIX_STAT_HOST_STATUS.length()));
			if (statProperty == null)
				return 0L;
			
			return statProperty.getCount();
	    }	
        else {
            return super.getProperty(key);
        }
    }   
    
	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(20);
		
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
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);
		BUILT_IN_PROPERTIES.add(PROP_ALIAS);
		BUILT_IN_PROPERTIES.add(PROP_AGENTID);

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
		COMPONENT_PROPERTIES.add(PROP_DESCRIPTION);
		COMPONENT_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);
		COMPONENT_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);
		COMPONENT_PROPERTIES.add(PROP_ALIAS);
		COMPONENT_PROPERTIES.add(PROP_AGENTID);

		return COMPONENT_PROPERTIES;
	}		
	
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("hostGroupId", getHostGroupId()).toString();
    }

    /** 
     * two HostGroup objects are equal if they have the same name, 
     * compared in a case-insensitive way
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof HostGroup) ) return false;
        HostGroup castOther = (HostGroup) other;

        return new EqualsBuilder()
            .append(this.getName().toLowerCase(), castOther.getName().toLowerCase())
            .isEquals();
    }

    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getName().toLowerCase())
            .toHashCode();
    }
}
