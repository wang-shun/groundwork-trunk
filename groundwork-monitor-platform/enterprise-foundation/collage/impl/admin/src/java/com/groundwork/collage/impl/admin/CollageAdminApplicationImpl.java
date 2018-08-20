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

package com.groundwork.collage.impl.admin;

import com.groundwork.collage.CollageAdminApplication;
import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.exception.CollageException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of CollageAdminApplication interface. The class delegates all
 * calls to an instance of CollageAdminImpl
 * 
 * @author rruttimann
 * 
 */
public class CollageAdminApplicationImpl implements CollageAdminApplication 
{
    private static String NAGIOS = "NAGIOS";

    // Create an instance of the CollageAdmin
	private CollageAdminInfrastructure admin = null;
	
	/** Use log4j */
    protected Log log = LogFactory.getLog(this.getClass());
    
	// Constructor
	public CollageAdminApplicationImpl(CollageAdminInfrastructure admin)
	{
		this.admin = admin;
	}
	
	/*Insert of native type properties */
	public void updateRuntimeAttribute(String MonitorServerName, String ApplicationType,
			String ApplicationName, String Identification, Map runtimeAttributes)
			throws CollageException {
		
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to ServiceStatus
		this.admin.updateServiceStatus(MonitorServerName, ApplicationType, ApplicationName, Identification, (String)null, runtimeAttributes);
	}

	/* Insert by string properties */
	public void updateRuntimeAttribute(String monitorServerName, String applicationType,
            String applicationName, String device_id, Properties runTimeAttributes) throws CollageException
            {
		
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to ServiceStatus
		this.admin.updateServiceStatus(monitorServerName, applicationType,
	            applicationName, device_id, (String)null, runTimeAttributes);

	}

	/* Bulk insert */
	public void updateRuntimeAttribute(String monitorServerName, String applicationType,
            String applicationName, String device_id, Collection runtimeAttributes) throws CollageException
    {
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to ServiceStatus
		this.admin.updateServiceStatus(monitorServerName, applicationType,
	            applicationName, device_id, runtimeAttributes);
    }

	public void updateStartupAttribute(String monitorServerName, String applicationType,
            String applicationName, String device_id, Map startupAttributes)
			throws CollageException {
		
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to HostStatus
		this.admin.updateHostStatus(monitorServerName, applicationType,
	            applicationName, device_id, startupAttributes);

	}
	
	/* Properties insert */
	public void updateStartupAttribute(String monitorServerName, String applicationType,
            String applicationName, String device_id, Properties startupAttributes)
	{
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to HostStatus
		this.admin.updateHostStatus(monitorServerName, applicationType,
	            applicationName, device_id, startupAttributes);
	}

	public void updateLogMessage(String monitorServerName, String applicationType,
            String serviceDescr, String hostName, String device_id, Map properties)
	{
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to LogMessage
		this.admin.updateLogMessage(monitorServerName, applicationType,
				serviceDescr, hostName, device_id, properties);
	}

    /**
     * same as above {@link #updateLogMessage(String,String,String,String,String,Map)}, but
     * takes a java.lang.Properties (map of strings) rather than a map of
     * primitive objects
     */
    public void updateLogMessage(String monitorServerName, String applicationType,
            String serviceDescr, String hostName, String device_id, Properties properties)
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to LogMessage
		this.admin.updateLogMessage(monitorServerName, applicationType,
				serviceDescr, hostName, device_id, properties);    	
    }

	public void addApplicationsToApplicationGroup(String applicationType, String ApplicationGroupName,
			List<String> applicationList) throws CollageException {
		
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to addHostsToHostGroup
		this.admin.addHostsToHostGroup(applicationType, ApplicationGroupName, applicationList);

	}

	public void removeApplicationsFromApplicationGroup(String ApplicationGroupName,
			List<String> applicationList) throws CollageException {
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeHostsFromHostGroup
		this.admin.removeHostsFromHostGroup(ApplicationGroupName, applicationList);

	}

	public void removeRuntimeAttribute(String Application, String attributeDescr)
			throws CollageException {
		
		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		this.admin.removeService(Application, attributeDescr);

	}

	public void removeApplication(String ApplicationName)
			throws CollageException {

		if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeHost
		this.admin.removeHost(ApplicationName);

	}
	
	/*
     * API to manage Categories which are nothing more than nested groups.
     * The methods are called from the adapter so that third party applications
     * are able to do inserts and updates.
     */
    
    /*
     * 
     * @param categoryName
     * @param entityType
     * @param entityEntityType
     * @param entityObjectID
     * @return CategoryID of the created Category
     * @throws CollageException
     * 
     * Note if ObjectID is not null a CategoryEntity for that Object type will be created. If ObjectID is null
     * only a CategoryEntry will be created. If the category already exists only the CategoryEntity will be added.
     */
    public int addCategoryEntity(String categoryName, String entityType, String entityEntityType, String entityObjectID)
            throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to addCategoryEntity
    	return admin.addCategoryEntity(categoryName, entityType, entityEntityType, entityObjectID).getCategoryId().intValue();
    }
    
    /*
     * 
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    public void removeCategory(String categoryName, String entityType) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeCategory
    	admin.removeCategory(categoryName, entityType);
    }
    
    /**
     * 
     * @param categoryID
     * @throws CollageException
     */
    public void removeCategory(Integer categoryID) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeCategory
    	admin.removeCategory(categoryID);
    }
    
    /**
     * 
     * @param categoryID
     * @param name
     * @param description
     * @throws CollageException
     */
    public void updateCategory(Integer categoryID, String name, String description) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to updateCategory
    	admin.updateCategory(categoryID, name, description);
    }
    
    
    /*
     * 
     * @param categoryName
     * @param entityType
     * @param entityEntityType
     * @param entityObjectID
     * @throws CollageException
     */
    public void removeCategoryEntity(String categoryName, String entityType, String entityEntityType,
                                     String entityObjectID) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeCategoryEntity
    	admin.removeCategoryEntity(categoryName, entityType, entityEntityType, entityObjectID);
    	
    }
    
    /*
     * @param parentCategoryName
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    public void addCategoryToParent(String parentCategoryName, String categoryName, String entityType)
            throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to addCategoryToParent
    	admin.addCategoryToParent(parentCategoryName, categoryName, entityType);
    	
    }
    
    /*
     * @param parentCategoryName
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    public void removeCategoryFromParent(String parentCategoryName, String categoryName, String entityType)
            throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeCategoryFromParent
    	admin.removeCategoryFromParent(parentCategoryName, categoryName, entityType);
    	
    }
    
    /*
     * Manage Consolidation Criterias.
     * Consolidation criterias will be applied to any inserts to LogMessage that identify the consolidation criteria to be applied
     */
    
    /*
     * 
     * @param Name
     * @param ConsolidationCriteria
     * @throws CollageException
     */
    public void addConsolidationCriteria(String Name, String ConsolidationCriteria) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to addConsolidationCriteria
    	admin.addOrUpdateConsolidationCriteria(Name, ConsolidationCriteria);    	
    }
    
    /*
     * 
     * @param Name
     * @throws CollageException
     */
    public void removeConsolidationCriteria(String Name) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeConsolidationCriteria
    	admin.removeConsolidationCriteria(Name);    	
    }
    
    /**
     * 
     * @param ConsolidationCriteriaID
     * @throws CollageException
     */
    public void removeConsolidationCriteria(Integer ConsolidationCriteriaID) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to removeCOnsolidationCriteria
    	admin.removeConsolidationCriteria(ConsolidationCriteriaID);
    	
    }
    
    /**
     * 
     * @param consolidationCriteriaID
     * @param Name
     * @param ConsolidationCriteria
     * @throws CollageException
     */
    public void updateConsolidationCriteria(Integer consolidationCriteriaID, String Name, String ConsolidationCriteria) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
            log.error(msg);
            throw new CollageException(msg);
		}
		 
		// Delegate to updateConsolidationCriteria
    	admin.updateConsolidationCriteria(consolidationCriteriaID, Name, ConsolidationCriteria );	
    }
    
    /**
     * 
     * @param RuntimeAttributeID
     * @param properties
     * @throws CollageException
     */
    public void updateRuntimeAttributeByID(Integer RuntimeAttributeID, Map properties) throws CollageException
    {
	    if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
	        log.error(msg);
	        throw new CollageException(msg);
		}
		 
		// Delegate to updateConsolidationCriteria
        // todo: NAGIOS application type placeholder requires actual value
		admin.updateServiceStatusByID(RuntimeAttributeID, NAGIOS, properties);
    }
    
    /**
     * 
     * @param StartupAttributeID
     * @param properties
     * @throws CollageException
     */
    public void updateStartupAttributeID(Integer StartupAttributeID, Map properties) throws CollageException
    {
	    if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
	        log.error(msg);
	        throw new CollageException(msg);
		}
		 
		// Delegate to updateConsolidationCriteria
        // todo: NAGIOS application type placeholder requires actual value
		admin.updateHostStatusByID(StartupAttributeID, NAGIOS, properties);
    }
    
    /**
     * 
     * @param LogMessageID
     * @param properties
     * @throws CollageException
     */
    public void updateLogMessageByID(Integer LogMessageID, Map properties) throws CollageException
    {
	    if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
	        log.error(msg);
	        throw new CollageException(msg);
		}
		 
		// Delegate to updateConsolidationCriteria
		admin.updateLogMessageByID( LogMessageID,properties );
	}
    
    
    /* (non-Javadoc)
     * @see com.groundwork.collage.CollageAdminApplication#acknowledgeEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void acknowledgeEvent(String applicationType, String typeRule, String host, String serviceDescription, String acknowledgedBy, String acknowledgeComment) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
	        log.error(msg);
	        throw new CollageException(msg);
		}
		 
		// Delegate to acknowledgeEvent
		admin.acknowledgeEvent(applicationType, typeRule, host, serviceDescription, acknowledgedBy, acknowledgeComment); 	
    }
    
    /*
     * (non-Javadoc)
     * @see com.groundwork.collage.CollageAdminApplication#insertPerformanceData(java.lang.String, java.lang.String, java.lang.String, double, java.lang.String)
     */
    public void insertPerformanceData(final String hostName, final String serviceDescription, final String performanceDataLabel,double performanceValue, String checkDate) throws CollageException
    {
    	if (this.admin == null)
		{
			String msg = "CollageAdminApplication - failed to create an instance of CollageAdminImpl"; 
	        log.error(msg);
	        throw new CollageException(msg);
		}
		 
		// Delegate to acknowledgeEvent
		admin.insertPerformanceData(hostName, serviceDescription, performanceDataLabel, performanceValue, checkDate);
    }
}
