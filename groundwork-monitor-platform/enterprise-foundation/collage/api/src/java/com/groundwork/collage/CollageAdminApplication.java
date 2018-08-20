/*
 * Copyright 2005 GroundWork Open Source Solutions, Inc. (“GroundWork”)
 * All rights reserved. Use is subject to GroundWork commercial license
 */

package com.groundwork.collage;

import com.groundwork.collage.exception.CollageException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * Adds or updates data in the Collage database. This is designed to be accessed
 * from the Collage feeders (collector/normalizers) to create or update state
 * information.
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com" > Roger Ruttimann </a>
 * @version $Id: CollageAdminApplication.java,v 1.1.2.1 2005/07/13 20:30:23
 *                rogerrut Exp $
 *  
 */
public interface CollageAdminApplication {

    /**
     * Updates the Collage ServiceStatus table; If there is no entry for the
     * ServiceStatus, a new one will be created; If the Device or Host don't
     * exist, they will be added as well. The ServiceDescription
     * will be extracted from the map key that maps to a map off attributes.
     * This method is functionally equivalent to the method
     * CollageAdminInfrastructure.updateServiceStatuts
     * 
     * @param MonitorServerName
     *                   Name of the MonitorServer
     * @param ApplicationName
     *                   Name of the Application
     * @param deviceIdent
     *                   Identification of the Device which is the Host IP, Hostname or
     *                   MAC address
     * @param runtimeAttributes
     *                   Map of application attributes as Name/Value pairs
     */
    void updateRuntimeAttribute(String MonitorServerName,
                                String ApplicationType,
                                String ApplicationName, 
                                String deviceIdent, 
                                Map runtimeAttributes)
            throws CollageException;

    /**
     * 
     * @param monitorServerName
     * @param applicationType
     * @param applicationName
     * @param deviceIdent
     * @param runtimeAttributes
     * @throws CollageException
     */
    void updateRuntimeAttribute(String monitorServerName, String applicationType,
            String applicationName, String deviceIdent, Properties runtimeAttributes) throws CollageException;
    
    
    /**
     * Performs 'bulk' update/insert of Runtime Attributes for an application
		 *
     * @param monitorServerName
     * @param applicationType
     * @param applicationName
     * @param deviceIdent
     * @param runtimeAttributes
     * @throws CollageException
     */
    void updateRuntimeAttribute(String monitorServerName, String applicationType,
            String applicationName, String deviceIdent, Collection runtimeAttributes) throws CollageException;
    
    
    /**
     * 
     * @param monitorServerName
     *                   Name of the MonitorServer
     * @param applicationName
     *                   Name of the Application
     * @param deviceIdent
     *                   Identification of the Device which is the Host IP, Hostname or
     *                   MAC address
     * @param startupAttributes
     *                   Map of application attributes as Name/Value pairs
     *  
     */
    void updateStartupAttribute(String monitorServerName, String applicationType,
            String applicationName, String deviceIdent, Map startupAttributes)
            throws CollageException;
    
    /**
     * same as above {@link #updateStartupAttribute(String,String,String,String,Map)}, but
     * takes a java.lang.Properties (map of strings) rather than a map of
     * primitive objects
     */
    void updateStartupAttribute(String monitorServerName, String applicationType,
            String applicationName, String deviceIdent, Properties startupAttributes );

    /**
     * Update the collage LogMessage table. If the device, host or ServiceStatus
     * don't exist, they will be added.
     * 
     * @param ApplicationType
     *                   Type of application such as NAGIOS, JMX, COLLAGE, SYSLOG
     * @param MonitorServerName
     *                   Name of the MonitorServer
     * @param MonitoredEntity
     *                   Name of the NagiosHost or the JMX Application
     * @param deviceIdent
     *                   Identification of the Device which is the Host IP, Hostname or
     *                   MAC address
     * @param Severity
     *                   Severity of the entry 
     *                   see the enumerated class {@link com.groundwork.collage.model.Severity}
     * @param MonitorStatus
     *                   MonitorStatus ID 
     *                   see the enumerated class {@link com.groundwork.collage.model.MonitorStatus}
     * @param TextMessage
     *                   Text of the entry
     * @param ReportDate
     *                   The date of the entry
     * @param LastInsertDate
     *                   Last time the log was updated
     * @param ServiceDescription
     *                   Name or description of the Service or Attribute the log
     *                   message is linked to
     * @throws CollageException
     */

    /**
     * This method records a LogMessage for a Device; see the parameter docs of 
     * {@link #updateRuntimeAttribute(String,String,String,String,Map)} for more
     */
    void updateLogMessage(String monitorServerName, String applicationType,
            String serviceDescr, String hostName, String deviceIdent, Map properties);

    /**
     * same as above {@link #updateLogMessage(String,String,String,String,String,Map)}, but
     * takes a java.lang.Properties (map of strings) rather than a map of
     * primitive objects
     */
    void updateLogMessage(String monitorServerName, String applicationType,
            String serviceDescr, String hostName, String deviceIdent, Properties properties);
    
    
    /**
     * Adds Applications to a ApplicationGroup.
     * 
     * @param ApplicationGroupName
     *                   Name of the ApplicationGroup
     * @param applicationList
     *                   List of names of applications to be added.
     * @throws CollageException
     */
    public void addApplicationsToApplicationGroup(String applicationType, String ApplicationGroupName,
    		List<String> applicationList) throws CollageException;

    /**
     * Removes Applications from a ApplicationGroup
     * 
     * @param ApplicationGroupName
     *                   Name of the ApplicationGroup
     * @param applicationList
     *                   List of names of applications to remove
     * @throws CollageException
     */
    public void removeApplicationsFromApplicationGroup(String ApplicationGroupName,
    		List<String> applicationList) throws CollageException;

    /**
     * deletes runtime Attribute record with the attributeDescr provided, and
     * de-associates from that attribute all LogMessages that were associated
     * with that Attribute
     * 
     * @param Application
     * @param attributeDescr
     * @throws CollageException
     */
    public void removeRuntimeAttribute(String Application, String attributeDescr)
            throws CollageException;

    /**
     * deletes the Application with the name provided, and the related startup
     * attributes, and runtime attributes - unlinks (but does not delete) all
     * LogMessages that were previously attached to this Application
     * 
     * @param ApplicationName
     * @throws CollageException
     */
    public void removeApplication(String ApplicationName)
            throws CollageException;
    
    /**
     * API to manage Categories which are nothing more than nested groups.
     * The methods are called from the adapter so that third party applications
     * are able to do inserts and updates.
     */
    
    /**
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
    int addCategoryEntity(String categoryName, String entityType, String entityEntityType, String entityObjectID)
            throws CollageException;
    
    /**
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    void removeCategory(String categoryName, String entityType) throws CollageException;
    
    /**
     * @param categoryID
     * @throws CollageException
     */
    void removeCategory(Integer categoryID) throws CollageException;
    
    /**
     * @param categoryID
     * @param name
     * @param description
     * @throws CollageException
     */
    void updateCategory(Integer categoryID, String name, String description) throws CollageException;
    
    /**
     * @param categoryName
     * @param entityType
     * @param entityEntityType
     * @param entityObjectID
     * @throws CollageException
     */
    void removeCategoryEntity(String categoryName, String entityType, String entityEntityType, String entityObjectID)
            throws CollageException;
    
    /**
     * @param parentCategoryName
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    void addCategoryToParent(String parentCategoryName, String categoryName, String entityType) throws CollageException;
    
    /**
     * @param parentCategoryName
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    void removeCategoryFromParent(String parentCategoryName, String categoryName, String entityType) throws CollageException;
    
    /**
     * Manage Consolidation Criterias.
     * Consolidation criterias will be applied to any inserts to LogMessage that identify the consolidation criteria to be applied
     */
    
    /**
     * 
     * @param Name
     * @param ConsolidationCriteria
     * @throws CollageException
     */
    void addConsolidationCriteria(String Name, String ConsolidationCriteria) throws CollageException;
    
    /**
     * 
     * @param Name
     * @throws CollageException
     */
    void removeConsolidationCriteria(String Name) throws CollageException;
    
    /**
     * 
     * @param ConsolidationCriteriaID
     * @throws CollageException
     */
    void removeConsolidationCriteria(Integer ConsolidationCriteriaID) throws CollageException;
    
    /**
     * 
     * @param consolidationCriteriaID
     * @param Name
     * @param ConsolidationCriteria
     * @throws CollageException
     */
    void updateConsolidationCriteria(Integer consolidationCriteriaID, String Name, String ConsolidationCriteria) throws CollageException;
    
    /**
     * 
     * @param RuntimeAttributeID
     * @param properties
     * @throws CollageException
     */
    void updateRuntimeAttributeByID(Integer RuntimeAttributeID, Map properties) throws CollageException;
    
    /**
     * 
     * @param StartupAttributeID
     * @param properties
     * @throws CollageException
     */
    void updateStartupAttributeID(Integer StartupAttributeID, Map properties) throws CollageException;
    
    /**
     * 
     * @param LogMessageID
     * @param properties
     * @throws CollageException
     */
    void updateLogMessageByID(Integer LogMessageID, Map properties) throws CollageException;
    
    /**
     * acknowledgeEvent Updates an existing event entry by setting the acknowledged by
    * @param applicationType
    * @param typeRule
    * @param host
    * @param serviceDescription
    * @param acknowledgedBy
    * @param acknowledgeComment
    * @throws CollageException
    */
   public void acknowledgeEvent(String applicationType, String typeRule, String host, String serviceDescription, String acknowledgedBy, String acknowledgeComment) throws CollageException;


   /**
    * Insert Performance Data into the system. If the Service or Host or service doesn't exist the value will be lost.
    * @param hostName
    * @param serviceDescription
    * @param performanceDataLabel
    * @param performanceValue
    * @param checkDate
    * @throws CollageException
    */
   public void insertPerformanceData(final String hostName, final String serviceDescription, final String performanceDataLabel,double performanceValue, String checkDate) throws CollageException;
 
   
}
