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

package com.groundwork.feeder.adapter.impl;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.Category;
import com.groundwork.feeder.adapter.FeederBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author rogerrut
 * 
 * Adapter for admin functionality such as managing:
 * 	Categories (Groups in Groups
 * 	Consolidation criterias
 * 	.. more ..
 * 
 * Calls into this adatper require a valid session ID which needs to be obtained from the system
 * prior to calling into this admin adapter.
 * 
 * Format of the XML messages send to the feeder:
 * 
 * <ADMIN SessionID='{Valid session ID}' Action='{create|modify|remove}' Type="{OperationStatus|Host|HostGroup|Device|LogMessage|ServiceStatus|HostStatus|Category|CategoryEntity|CategoryHierarchy|ConsolidationCriteria}' 
 * 	List of attributes name="value" />
 * 
 * Notes:
 * --> The action modify requires the PrimaryKey in the list of Attributes (e.g CategoryID="123") otherwise the operation will fail
 * --> Adding entries to CategoryHierarchy requires ParentCategoryName and CategoryName as attributes.
 * --> Adding entries to CategoryEntity requires ObjectID, EntityType and CategoryName as attributes.
 * 
 *
 */

public class CollageAdmin implements FeederBase {
	
	/**
     * Adapter for framework log
     */
    private static String ADAPTER_NAME="ADMIN";
    
//  Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    /* Attributes for this request type */
    private static final String ADMIN_TYPE = "Type";   
    private static final String ADMIN_ACTION = "Action";
    private static final String ADMIN_SESSION_ID = "SessionID";
    
    private static final String ADMIN_ACTION_CREATE = "create";
    private static final String ADMIN_ACTION_MODIFY = "modify";   
    private static final String ADMIN_ACTION_REMOVE = "remove";
    private static final String ADMIN_TYPE_CATEGORY = "Category";   
    private static final String ADMIN_TYPE_CATEGORY_ENTITY = "CategoryEntity"; 
    private static final String ADMIN_TYPE_CATEGORY_HIERARCHY = "CategoryHierarchy"; 
    private static final String ADMIN_TYPE_CONSOLIDATION = "Consolidation";
    
    private static final String ADMIN_TYPE_HOST = "Host"; 
    private static final String ADMIN_TYPE_HOSTGROUP = "HostGroup"; 
    private static final String ADMIN_TYPE_DEVICE = "Device";

    // State and event tables
    private static final String ADMIN_TYPE_LOG_MESSAGE = "LogMessage"; 
    private static final String ADMIN_TYPE_OPERATION_STATUS = "OperationStatus";
    private static final String ADMIN_TYPE_SERVICE_STATUS = "ServiceStatus"; 
    private static final String ADMIN_TYPE_HOST_STATUS = "HostStatus"; 
    
    // Primary key fields
    private static final String LOG_MESSAGE_ID = "LogMessageID"; 
    private static final String SERVICE_STATUS_ID = "ServiceSatusID"; 
    private static final String HOST_STATUS_ID = "HostStatusID"; 
    
    
    // Table fields
    private static final String CATEGORY_NAME = "Name";
    private static final String CATEGORY_DESCRIPTION = "Description";
    private static final String CATEGORY_ID = "CategoryID";
    
    private static final String CONSOLIDATION_NAME = "Name";
    private static final String CONSOLIDATION_CRITERIA = "Criteria";
    private static final String CONSOLIDATION_ID = "ConsolidationCriteriaID";
    
    private static final String CATEGORY_PARENT = "ParentCategoryName";
    private static final String CATEGORY_CHILD = "CategoryName";
    
    private static final String CATEGORY_ENTITY_OBJECTID = "ObjectID";
    private static final String CATEGORY_ENTITY_TYPE = "EntityType";
    private static final String CATEGORY_ENTITY_CATEGORY = "CategoryName";
    
    private static final String HOST_NAME = "Host";
    private static final String HOSTGROUP_NAME = "Name";
    private static final String DEVICE_NAME = "Identification";
    private static final String SERVICE_DESCRIPTION = "ServiceDescription";
    
    private static final String OPERATION_STATUS = "OperationStatus";

    private static String NAGIOS = "NAGIOS";

    public String getName() {
		 return ADAPTER_NAME ;
	}

	public void process(Object beanFactory, FoundationMessage message) 
	{
		if (log.isDebugEnabled())
			log.debug("adapter.admin method process(). XmlStream [" + message + "]");
		 
    	if (message == null)
    	{
    		log.debug("Admin Adapter: Null FoundationMessage.");
    		return;
    	}
    	
        // Extract attributes
        List<Hashtable<String, String>> listAttributes = message.getAttributes();
        
        if (listAttributes == null || listAttributes.size() == 0)
        {
            log.debug("Admin Adapter: Could not find attributes in xml " + message);
            return;
        }
        
        Map<String, String> attributes = listAttributes.get(0);
        
        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            log.error("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            return;       
        }
        
        boolean isModify = false;
        boolean isCreate = false;
        boolean isRemove = false;
        
        String type = (String)attributes.get(ADMIN_TYPE);
        String action = (String)attributes.get(ADMIN_ACTION);
        
        if (action == null || type == null)
        {
           	log.error("Attribute action or type missing or mis-typed. Make sure you define Action and Type attributes (case sensitive) and the values.");
           	return;
        }
        
        if (log.isDebugEnabled())log.debug("Admin action [" + action +"] and Type [" + type + "]");
        
        if (action.equalsIgnoreCase(ADMIN_ACTION_MODIFY) == true)
        	isModify = true;
        else if (action.equalsIgnoreCase(ADMIN_ACTION_REMOVE) == true)
        	isRemove = true;
        else if (action.equalsIgnoreCase(ADMIN_ACTION_CREATE) == true)
        	isCreate = true;
        else
        {
        	log.error("Unrecognized action " + action + " Submitted to Admin API. Accepted values are " + ADMIN_ACTION_MODIFY + ", " + ADMIN_ACTION_CREATE + " and " + ADMIN_ACTION_REMOVE);
        	throw new CollageException("Unrecognized action " + action + " Submitted to Admin API. Accepted values are " + ADMIN_ACTION_MODIFY + ", " + ADMIN_ACTION_CREATE +" and " + ADMIN_ACTION_REMOVE);
        }
        
        try
        {
        	/*
        	 * Manage updates for State and events
        	 */
        	if (	(type.equalsIgnoreCase(ADMIN_TYPE_HOST_STATUS) == true)
        		|| 	(type.equalsIgnoreCase(ADMIN_TYPE_SERVICE_STATUS) == true)
        		||	(type.equalsIgnoreCase(ADMIN_TYPE_LOG_MESSAGE) == true)
        		||	(type.equalsIgnoreCase(ADMIN_TYPE_OPERATION_STATUS) == true) ) 
        	{
        		if (isCreate == true)
        		{
        			log.error("AdminAPI. ServiceStatus, HostStatus and LogMessage entries only can be created through the Nagios feeders.");
        		}
        		else if (isRemove == true)
        		{
        			if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICE_STATUS) == true)
        			{
        				// Requires Host and ServiceDescription attributes
        				String host = (String)attributes.get(ADMIN_TYPE_HOST);
        				String serviceDescription = (String)attributes.get(SERVICE_DESCRIPTION);
        				
        				if (host != null  && serviceDescription != null)
        				{
        					Integer serviceStatusID = admin.removeService(host, serviceDescription);
        					// Now remove the service from service group as well.
        					if (serviceStatusID > 0)
        					admin.removeCategoryEntity("SERVICE_STATUS", serviceStatusID);
        				}
        				else
        				{
        					log.warn("AdminAPI: Remove ServiceStatus requires Host and ServiceDescription attributes.");
        				}
        			}
        			else
        			{
            			log.warn("AdminAPI. HostStatus and LogMessage remove not implemented yet.");
        			}
        		}
        		else
        		{    		
	        		// Create a properties map and the call the updates by ID functions
	        		Properties props = new Properties();
	        		
	        		Iterator itKeys = attributes.keySet().iterator();
	        		while (itKeys.hasNext())
	        		{
	        			String key = (String)itKeys.next();
	        			if ( key != null && key.length() > 0)
	        			{
	        				// Exclude Type, Action, SessionID
	        				if (	(key.equalsIgnoreCase(ADMIN_SESSION_ID) == false)
	        					&& 	(key.equalsIgnoreCase(ADMIN_ACTION) == false)
	        					&& 	(key.equalsIgnoreCase(ADMIN_TYPE) == false)
	        					&& 	(key.equalsIgnoreCase(HOST_STATUS_ID) == false)
	        					&& 	(key.equalsIgnoreCase(LOG_MESSAGE_ID) == false)
	        					&& 	(key.equalsIgnoreCase(SERVICE_STATUS_ID) == false))
	        				{
	        					// Add properties
	        					log.info("Update Object By ID. Added property [" +(String)attributes.get(key) + "]");
	        					props.setProperty(key, (String)attributes.get(key));
	        				}
	        			}
	        		}
	        		
	        		if ( props != null && props.size() > 0)
	        		{
	        			String objID;
	        			
	        			if (type.equalsIgnoreCase(ADMIN_TYPE_HOST_STATUS) == true)
	        			{
	        				objID  = (String)attributes.get(HOST_STATUS_ID);
		        			if ( objID != null && objID.length() > 0 )
		        				admin.updateHostStatusByID(new Integer(objID), NAGIOS, (Map)props);
	        				
	        			}
	        			else if (type.equalsIgnoreCase(ADMIN_TYPE_LOG_MESSAGE) == true)
	        			{
	        				objID  = (String)attributes.get(LOG_MESSAGE_ID);
		        			if ( objID != null && objID.length() > 0 )
		        			{
		        				if (log.isInfoEnabled())
		        					log.info("Attempt to update Object By ID. For ID[" + objID + "]");
		        				admin.updateLogMessageByID(new Integer(objID), (Map)props);
		        			}
	        				
	        			}
	        			else if (type.equalsIgnoreCase(ADMIN_TYPE_SERVICE_STATUS) == true)
	        			{
	        				objID  = (String)attributes.get(SERVICE_STATUS_ID);
		        			if ( objID != null && objID.length() > 0 )
		        				admin.updateServiceStatusByID(new Integer(objID), NAGIOS, (Map)props);
	        			}
	        			else if (type.equalsIgnoreCase(ADMIN_TYPE_OPERATION_STATUS) == true)
	        			{
	        				objID  = (String)attributes.get(LOG_MESSAGE_ID);
		        			if ( objID != null && objID.length() > 0 )
		        			{
		        				if (log.isInfoEnabled())
		        					log.info("Attempt to update log message operation status. For ID[" + objID + "]");
		        				
		        				admin.updateLogMessageOperationStatus(objID, (String)props.get(OPERATION_STATUS));
		        			}	        				
	        			}
	        		}
	        		else
	        		{
	        			log.error("Modify Objects failed. No fields to update defined");
	        		}
        		}
        	}
        	
        	/*
        	 * Manage Categories
        	 */
        	else if ( type.equalsIgnoreCase(ADMIN_TYPE_CATEGORY) == true)
	        {
                // TODO: support category entities other than ServiceGroup
	        	if (isRemove == true)
	        	{
	        		// ID or name
                    Category removed = null;
	        		String attribute = (String)attributes.get(CATEGORY_NAME);
	        		if (attribute != null && attribute.length() > 0)
	        			removed = admin.removeCategory((String)attributes.get(CATEGORY_NAME), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
	        		else
	        		{
	        			attribute = (String)attributes.get(CATEGORY_ID);
	        			if ( attribute != null && attribute.length() > 0 )
	        				removed = admin.removeCategory(new Integer(attribute));
	        			else
	        			{
	        				log.error("AdminAPI. Remove Category requires either " + CATEGORY_NAME + " or " + CATEGORY_ID + " defined.");
	        			}
	        		}
                    if (removed != null) {
                        admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, removed.getID());
                    }
	        	}
	        	else if (isCreate == true)
	        	{
	        		admin.addCategoryEntity((String)attributes.get(CATEGORY_NAME), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, null, null);
	        	}
	        	else if ( isModify == true)
	        	{
	        		//	Modify requires the PK
	        		String pk = (String)attributes.get(CATEGORY_ID);
	        		if (pk == null)
	        		{
	        			log.error("Admin feeder. Modify operation requires the identity field " + CATEGORY_ID + "");
	        			return;
	        		}
	        		
	        		admin.updateCategory(new Integer(pk), (String)attributes.get(CATEGORY_NAME), (String)attributes.get(CATEGORY_DESCRIPTION));
	        	}
	        		
	        	
	        }
 	        /*
 	         * Manage CategoryEntities
 	         */
	        else if (type.equalsIgnoreCase(ADMIN_TYPE_CATEGORY_ENTITY) == true)
	        {
                // TODO: support category entities other than ServiceGroup
	        	if (isCreate == true)
	        		admin.addCategoryEntity((String)attributes.get(CATEGORY_ENTITY_CATEGORY), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, (String)attributes.get(CATEGORY_ENTITY_TYPE), (String)attributes.get(CATEGORY_ENTITY_OBJECTID));
	        	else if (isRemove == true)
	        		admin.removeCategoryEntity((String) attributes.get(CATEGORY_ENTITY_CATEGORY), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, (String) attributes.get(CATEGORY_ENTITY_TYPE), (String) attributes.get(CATEGORY_ENTITY_OBJECTID));
	        	
	        	// TODO: Modify
	        }
 	        /*
 	         * Manage Category Hierarchy
 	         */
	        else if (type.equalsIgnoreCase(ADMIN_TYPE_CATEGORY_HIERARCHY) == true)
	        {
                // TODO: support category entities other than ServiceGroup
	        	if (isCreate == true)
	        		admin.addCategoryToParent((String)attributes.get(CATEGORY_PARENT), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, (String)attributes.get(CATEGORY_CHILD));
	        	else if (isRemove)
	        		admin.removeCategoryFromParent((String)attributes.get(CATEGORY_PARENT), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, (String)attributes.get(CATEGORY_CHILD));
	        	
	        	// TODO: modify
	        }
 	        /*
 	         * Manage Consolidation Criterias
 	         */
	        else if (type.equalsIgnoreCase(ADMIN_TYPE_CONSOLIDATION) == true)
	        {
	        	if (isCreate == true)
	        		admin.addOrUpdateConsolidationCriteria((String)attributes.get(CONSOLIDATION_NAME), (String)attributes.get(CONSOLIDATION_CRITERIA));
	        	else if ( isRemove == true)
	        	{
	        		//ID or name
	        		String attribute = (String)attributes.get(CONSOLIDATION_NAME);
	        		if (attribute != null && attribute.length() > 0)
	        			admin.removeConsolidationCriteria((String)attributes.get(CONSOLIDATION_NAME));
	        		else
	        		{
	        			attribute = (String)attributes.get(CONSOLIDATION_ID);
	        			if ( attribute != null && attribute.length() > 0 )
	        				admin.removeConsolidationCriteria(new Integer(attribute));
	        			else
	        			{
	        				log.error("AdminAPI. Remove ConsolidationCriteria requires either " + CONSOLIDATION_NAME + " or " + CONSOLIDATION_ID + " defined.");
	        			}
	        		}   		
	        	}
	        	else if (isModify == true)
	        	{
	        		// Modify requires the PK
	        		String pk = (String)attributes.get(CONSOLIDATION_ID);
	        		if (pk == null)
	        		{
	        			log.error("Admin feeder. Modify operation requires the identity field " + CONSOLIDATION_ID + "");
	        			return;
	        		}
	        		
	        		admin.updateConsolidationCriteria(new Integer(pk), (String)attributes.get(CONSOLIDATION_NAME), (String)attributes.get(CONSOLIDATION_CRITERIA));
	        	} 	
	        }
        	
        	/**
        	 * Manage Host, HostGroup or device operations
        	 */
	        else if (type.equalsIgnoreCase(ADMIN_TYPE_HOST) == true)
	        {
	        	if (isRemove)
	        	{
	        		String attribute = (String)attributes.get(HOST_NAME);
	        		if (attribute != null && attribute.length() > 0)
	        		{
	        			log.debug("Calling remove host for  host [" + attribute + "]");
	        			try
	        			{
	        				admin.removeHost(attribute);
	        			}
	        			catch(Exception e)
	        			{
	        				log.error("Error while removing Host [" + attribute + "] Error: " + e);
	        			}
	        		}
	        		else
	        		{
	        			log.error("Cannot remove Host. Name attribute missing or 0 length.");
	        		}
	        		
	        	}
	        	else
	        	{
	        		log.warn("Host entries are created through ServiceStatus or HostStatus Feeders.");
	        	}
	        	
	        }
	        else if (type.equalsIgnoreCase(ADMIN_TYPE_HOSTGROUP) == true)
	        {
	        	String attribute = (String)attributes.get(HOSTGROUP_NAME);
        		if (attribute != null && attribute.length() > 0)
        		{
		        	if (isRemove)
		        	{
		        		try
		        		{
		        			Integer id = admin.removeHostGroup(attribute);
                            if (id != null) {
                                admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_HOSTGROUP, id);
                            }
		        		}
		        		catch(Exception e)
	        			{
	        				log.error("Error while removing HostGroup [" + attribute + "] Error: " + e);
	        			}
		        		
		        	}
		        	else
		        	{
		        		log.warn("Modifying Hostgroup not supported delete/create new entry.");
		        	}
        		}
        		else
        		{
        			log.error("Cannot remove HostGroup. Name attribute missing or 0 length.");
        		}
	        }
	        else if (type.equalsIgnoreCase(ADMIN_TYPE_DEVICE) == true)
	        {
	        	if (isRemove)
	        	{
	        		String attribute = (String)attributes.get(DEVICE_NAME);
	        		if (attribute != null && attribute.length() > 0)
	        		{
	        			try
	        			{
	        				admin.removeDevice(attribute);
	        			}
	        			catch(Exception e)
	        			{
	        				log.error("Error while removing Device [" + attribute + "] Error: " +e);
	        			}
	        		}
	        		else
	        		{
	        			log.error("Cannot remove Host. Name attribute missing or 0 length.");
	        		}
	        		
	        	}
	        	else
	        	{
	        		log.warn("Device entries are created through ServiceStatus or HostStatus Feeders.");
	        	}	
	        }
        }
        catch (CollageException ce)
        {
        	log.error("Collage Exception in Admin operations Error: " + ce);
        	throw ce;
        }
        catch(Exception e)
        {
        	log.error("Collage Exception in Admin operations Error: " + e);
        	throw new CollageException("Admin Feeder exception. Error: " + e);
        }
        
	}

	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void uninitialize() {
		// TODO Auto-generated method stub

	}
}
