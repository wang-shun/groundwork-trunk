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
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.ServiceStatus;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.groundwork.foundation.bs.statistics.StatisticsService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

@XmlRootElement(name="host")
@XmlAccessorType(XmlAccessType.FIELD)
public class Host extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.Host
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
								Host.ENTITY_TYPE_CODE,
								true);

	private static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_NAME,
								HP_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Host.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DESCRIPTION,
								HP_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Host.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_DEVICE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DEVICE_ID,
								HP_DEVICE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Device.ENTITY_TYPE_CODE,
								true);  	

	private static final PropertyType PROP_DEVICE_IDENTIFICATION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DEVICE_IDENTIFICATION,
								HP_DEVICE_IDENTIFICATION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Device.ENTITY_TYPE_CODE,
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
	
	private static final PropertyType PROP_LAST_CHECK_TIME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_LAST_CHECK_TIME,
								HP_LAST_CHECK_TIME, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								null,
								true);  
	
	private static final PropertyType PROP_MONITOR_STATUS_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MONITOR_STATUS_ID,
								HP_MONITOR_STATUS_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								MonitorStatus.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_MONITOR_STATUS_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MONITOR_STATUS_NAME,
								HP_MONITOR_STATUS_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								MonitorStatus.ENTITY_TYPE_CODE,
								true);  

	private static final PropertyType PROP_SERVICE_COUNT = 
		new com.groundwork.collage.model.impl.PropertyType(
								EP_SERVICE_COUNT,
								null, // Leave null since there is no related hibernate property 
								PropertyType.DataType.LONG, 
								null,
								true);	
    
	/** Filter-Only Properties */
	private static final PropertyType PROP_HOSTGROUP_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOSTGROUP_ID,
								HP_HOSTGROUP_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								HostGroup.ENTITY_TYPE_CODE,
								true);
	
	private static final PropertyType PROP_HOSTGROUP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOSTGROUP_NAME,
								HP_HOSTGROUP_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								HostGroup.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_MONITORSERVER_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MONITORSERVER_ID,
								HP_MONITORSERVER_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								MonitorServer.ENTITY_TYPE_CODE,
								true);
	
	private static final PropertyType PROP_MONITORSERVER_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MONITORSERVER_NAME,
								HP_MONITORSERVER_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								MonitorServer.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_SERVICE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_SERVICE_ID,
								HP_SERVICE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);
	
	private static final PropertyType PROP_SERVICE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_SERVICE_NAME,
								HP_SERVICE_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);

    private static final PropertyType PROP_AGENTID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_AGENT_ID,
                    HP_AGENT_ID, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Host.ENTITY_TYPE_CODE,
                    true);

    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
    /** identifier field */
    private Integer hostId;

    /** nullable persistent field */
    private String hostName;
    
    /** nullable persistent field */
    private String description;

    /** persistent field */
    private com.groundwork.collage.model.Device device;

    /** persistent field */
    private HostStatus hostStatus;

    /** persistent field */
    private Set serviceStatuses;

	/** persistent field */
	private Set comments;

	/** persistent field */
    private ApplicationType applicationType;

    /** persistent field */
    private String agentId;

    /* 
     * Association table HostGroupCollections 
     * Contains a list of HostGroups for this Host
     */
    private Set hostgroups;
    
    private String lastMonitorStatus;
    
    /** full constructor */
    public Host(Integer hostId, String hostName,
            com.groundwork.collage.model.Device device,
            Set serviceStatus)
    {
        this.hostId = hostId;
        this.hostName = hostName;
        this.device = device;
        this.serviceStatuses = serviceStatus;
    }

    /** default constructor */
    public Host()
    {
    }

    /** minimal constructor */
    public Host(Integer hostId, com.groundwork.collage.model.Device device,
            String hostName, Set serviceStatus)
    {
        this.hostId = hostId;
        this.device = device;
        this.hostName = hostName;
        this.serviceStatuses = serviceStatus;
    }
    
	@Override
	public String getEntityTypeCode()
	{
		return com.groundwork.collage.model.Host.ENTITY_TYPE_CODE;
	}

	@Override
	public PropertyValue getPropertyValueInstance(String name, Object value)
	{
		return new EntityPropertyValue(hostId, getEntityTypeId(), name, value);
	}    

    public Integer getHostId()
    {
        return this.hostId;
    }

    public void setHostId(Integer hostId)
    {
        this.hostId = hostId;
    }

    public String getHostName()
    {
        return this.hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }
    
    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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

    public com.groundwork.collage.model.Device getDevice()
    {
        return this.device;
    }

    public void setDevice(com.groundwork.collage.model.Device device)
    {
        this.device = device;
    }


    public com.groundwork.collage.model.HostStatus getHostStatus()
    {
        return this.hostStatus;
    }

    public void setHostStatus(com.groundwork.collage.model.HostStatus hostStatus)
    {
        this.hostStatus = hostStatus;
    }


    public void setServiceStatuses(Set serviceStatus)
    {
        this.serviceStatuses = serviceStatus;
    }
    
    public Set getServiceStatuses()
    {
        if (this.serviceStatuses == null)
            this.serviceStatuses = new HashSet();

        return this.serviceStatuses;
    }

    public ServiceStatus getServiceStatus(String description)
    {
        return this.getServiceStatusFromSet(description);
    }

   	public Set getComments() {
    	if (this.comments == null)
    		this.comments = new HashSet();

        return this.comments;
	}

	public void addComment(com.groundwork.collage.model.Comment comment) {
		if (comments == null) comments = new HashSet();
		if (comment != null) comments.add(comment);
	}

	public void removeComment(com.groundwork.collage.model.Comment comment) {
		comments.remove(comment);
	}

	public void setComments(Set comments) {
		this.comments = comments;
	}

    public String toString()
    {
        return new ToStringBuilder(this).append("hostId", getHostId())
                .toString();
    }

    /**
     * @return Returns the hostgroups.
     */
    public Set getHostGroups() 
    {
    	if (this.hostgroups == null)
    		this.hostgroups = new HashSet();
    	
        return hostgroups;
    }
    /**
     * @param hostgroups The hostgroups to set.
     */
    public void setHostGroups(Set hostgroups) {
        this.hostgroups = hostgroups;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /** 
     * two Hosts are equal if they have the same name, compared in a
     * case-insensitive way
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Host) ) return false;
        Host castOther = (Host) other;

        return new EqualsBuilder()
            .append(this.getHostName().toLowerCase(), castOther.getHostName().toLowerCase())
            .isEquals();
    }


    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getHostName().toLowerCase())
            .toHashCode();
    }

    /** 
     * used to retrieve a ServiceStatus from the ServiceStatus Set -
     * instead of this method we could implement the relationship as a Map
     * keyed by Service Description
     */
    private ServiceStatus getServiceStatusFromSet(String description)
    {
        for (Iterator i = this.getServiceStatuses().iterator(); i.hasNext() ;)
        {
            ServiceStatus service = (ServiceStatus)i.next();
            if (service == null)
            	continue;
            
            if (service.getServiceDescription().equalsIgnoreCase(description)) 
            {
                return service;
            }
        }
        
        return null;
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
    	
        if (key.equalsIgnoreCase(EP_NAME)) {
            return this.getHostName();
        }
        else if (key.equalsIgnoreCase(EP_ID)) {
            return this.getHostId();
        }
        else if (key.equalsIgnoreCase(EP_DESCRIPTION)) {
            return this.getDescription();
        }
        else if (key.equalsIgnoreCase(EP_DEVICE_IDENTIFICATION)) {
        	Device device = this.getDevice();
        	
        	if (device == null)
        		return null;
        	
            return device.getIdentification();
        }
        else if (key.equalsIgnoreCase(EP_DEVICE_ID)) {
        	Device device = this.getDevice();
        	
        	if (device == null)
        		return null;
        	
            return device.getDeviceId();
        }
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_NAME)) 
        {
        	ApplicationType appType = this.getApplicationType();
        	
        	if (appType == null)
        		return null;
        	
        	return appType.getName();
        }
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_ID)) 
        {
        	ApplicationType appType = this.getApplicationType();
        	
        	if (appType == null)
        		return null;
        	
        	return appType.getApplicationTypeId();
        }        
        else  if (key.equalsIgnoreCase(EP_LAST_CHECK_TIME)) 
        {
        	HostStatus status = this.getHostStatus();
        	
        	if (status == null)
        		return null;
        	
        	return status.getLastCheckTime();
        }
        else  if (key.equalsIgnoreCase(EP_MONITOR_STATUS_NAME)) 
        {
        	HostStatus status = this.getHostStatus();
        	
        	if (status == null)
        		return null;
        	
        	MonitorStatus monStatus = status.getHostMonitorStatus();
        	if (monStatus == null)
        		return null;
        	
        	return monStatus.getName();
        }    
        else  if (key.equalsIgnoreCase(EP_MONITOR_STATUS_ID)) 
        {
        	HostStatus status = this.getHostStatus();
        	
        	if (status == null)
        		return null;
        	        	
        	MonitorStatus monStatus = status.getHostMonitorStatus();
        	if (monStatus == null)
        		return null;
        	
        	return monStatus.getMonitorStatusId();
        }         
        else if (key.equalsIgnoreCase(EP_SERVICE_COUNT)) 
        {
			StatisticsService statisticService = getStatisticService();
		    
			StateStatistics stateStatistics = statisticService.getServiceStatisticByHostName(this.getHostName());
			if (stateStatistics == null)
				return 0L;
			
			return stateStatistics.getTotalServices();
        }                      
	    else if (key.startsWith(PREFIX_STAT_SERVICE_STATUS))
	    {
			StatisticsService statisticService = getStatisticService();
	    
			StateStatistics stateStatistics = statisticService.getServiceStatisticByHostName(this.getHostName());
			if (stateStatistics == null)
				return 0L;
			
			StatisticProperty statProperty = stateStatistics.getStatisticProperty(key.substring(PREFIX_STAT_SERVICE_STATUS.length()));
			if (statProperty == null)
				return 0L;
			
			return statProperty.getCount();
	    }
//	    else if (key.startsWith(PREFIX_STAT_SERVICE_APP_PROP_ENABLED))
//	    {
//			StatisticsService statisticService = getStatisticService();
//	    
//			Collection<NagiosStatisticProperty> appStatistics = 
//				statisticService.getApplicationStatisticsHost(this.getApplicationTypeId().intValue(), this.getHostName());
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
//				statisticService.getApplicationStatisticsHost(this.getApplicationTypeId().intValue(), this.getHostName());
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
            return super.getProperty(key);
        }
    }   
    
	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
		
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Build up properties based on non-volatile properties and statistic properties
		
		// Service Count is a statistic property
		BUILT_IN_PROPERTIES.add(PROP_SERVICE_COUNT);
		
		// For each statistic
		StatisticsService statisticService = getStatisticService();
		
		// Add statistic properties
		List<String> serviceStats = statisticService.getServiceStatusList();
		Iterator<String> it = serviceStats.iterator();
		while(it.hasNext())
		{
			BUILT_IN_PROPERTIES.add(new com.groundwork.collage.model.impl.PropertyType(
					PREFIX_STAT_SERVICE_STATUS + it.next(),
					null,										
					PropertyType.DataType.LONG,
					null,
					true));	
		}
				
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_NAME);
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);
		BUILT_IN_PROPERTIES.add(PROP_DEVICE_ID);
		BUILT_IN_PROPERTIES.add(PROP_DEVICE_IDENTIFICATION);
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);
		BUILT_IN_PROPERTIES.add(PROP_LAST_CHECK_TIME);
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_ID);
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_NAME);
		BUILT_IN_PROPERTIES.add(PROP_AGENTID);

		return BUILT_IN_PROPERTIES;
	}
	
	/** Filterable properties **/
	public List<PropertyType> getComponentProperties()
	{
		if (COMPONENT_PROPERTIES != null)
			return COMPONENT_PROPERTIES;
		
		COMPONENT_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties		
		COMPONENT_PROPERTIES.add(PROP_NAME);
		COMPONENT_PROPERTIES.add(PROP_ID);
		COMPONENT_PROPERTIES.add(PROP_DESCRIPTION);
		COMPONENT_PROPERTIES.add(PROP_DEVICE_ID);
		COMPONENT_PROPERTIES.add(PROP_DEVICE_IDENTIFICATION);
		COMPONENT_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);
		COMPONENT_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);
		COMPONENT_PROPERTIES.add(PROP_LAST_CHECK_TIME);
		COMPONENT_PROPERTIES.add(PROP_MONITOR_STATUS_ID);
		COMPONENT_PROPERTIES.add(PROP_MONITOR_STATUS_NAME);
		COMPONENT_PROPERTIES.add(PROP_HOSTGROUP_ID);
		COMPONENT_PROPERTIES.add(PROP_HOSTGROUP_NAME);
		COMPONENT_PROPERTIES.add(PROP_MONITORSERVER_ID);
		COMPONENT_PROPERTIES.add(PROP_MONITORSERVER_NAME);
		COMPONENT_PROPERTIES.add(PROP_SERVICE_ID);
		COMPONENT_PROPERTIES.add(PROP_SERVICE_NAME);
		COMPONENT_PROPERTIES.add(PROP_AGENTID);

		return COMPONENT_PROPERTIES;
	}

	public String getLastMonitorStatus() {
		return lastMonitorStatus;
	}

	public void setLastMonitorStatus(String lastMonitorStatus) {
		this.lastMonitorStatus = lastMonitorStatus;
	}	
}

