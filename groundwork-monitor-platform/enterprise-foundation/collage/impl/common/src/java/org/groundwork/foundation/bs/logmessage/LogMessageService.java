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

package org.groundwork.foundation.bs.logmessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.foundation.ws.model.impl.IntegerProperty;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.impl.StateTransition;

import com.groundwork.collage.model.LogMessage;


public interface LogMessageService extends BusinessService {

	public final static String LOG_MESSAGE_EVENT_XML_TAG_NAME = "LOG_MESSAGE_EVENT";

	/**
	 * get Log messages for a filter criteria. Apply pagination if defined
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how the results should be sorted.
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public FoundationQueryList getLogMessages(FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
	throws BusinessServiceException;
	
	/**
	 * get Log messages for a given date range. Apply pagination if defined
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how the results should be sorted.
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public FoundationQueryList getLogMessages(String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
	throws BusinessServiceException;
	
	/**
	 * Get log messages reported for a specific Application Type
	 * 
	 * @param appType
	 * @param startDate
	 * @param endDate
	 * @param filter
	 * @param sortCriteria
	 * @param firstResults
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	public FoundationQueryList getLogMessagesByApplicationTypeName(String appTypeName, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
	throws BusinessServiceException;
			
	/**
	 * Get log messages reported for a specific Application Type
	 * 
	 * @param appType
	 * @param startDate
	 * @param endDate
	 * @param filter
	 * @param sortCriteria
	 * @param firstResults
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	public FoundationQueryList getLogMessagesByApplicationTypeId(int appTypeId, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
	throws BusinessServiceException;
			
	 /**
	 * Get log messages reported for a specific server (Device)
	 * 
	 * @param deviceIdentification
	 *  the name, ip, etc. identifying the Server/Device for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
	 * @param firstResult -1 for no pagination or any integer value for the first record to return
	 * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
	 * @return a list of LogMessages
	 * @throws BusinessServiceException
	 */
	FoundationQueryList getLogMessagesByDeviceIdentification(String deviceIdentification, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
	throws BusinessServiceException;

	 /**
	 * Get log messages reported for a list of servers (Devices)
	 * 
	 * @param deviceIdentifications
	 *  the names, ips, etc. identifying the Servers/Devices for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
	 * @param firstResult -1 for no pagination or any integer value for the first record to return
	 * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
	 * @return a list of LogMessages
	 * @throws BusinessServiceException
	 */
	FoundationQueryList getLogMessagesByDeviceIdentifications(String[] deviceIdentifications, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
	throws BusinessServiceException;

    /**
     * Get log messages reported for a specific server (Device)
     * 
     * @param deviceID
     *  the id of the Server/Device for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByDeviceId(int deviceId, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;    
    
    /**
     * Get log messages reported for a list of servers (Devices)
     * 
     * @param deviceIds
     *  the ids of the Servers/Devices for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByDeviceIds(int[] deviceIds, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;    
    
    /**
     * Get log messages reported for a specific Host on a Server/Device
     * 
     * @param hostName 
     *     the name of the Host for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostName(String hostName, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;

    /**
     * Get log messages reported for a list of Hosts 
     * 
     * @param hostNames 
     *     the names of the Hosts for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostNames(String[] hostNames, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;

    /**
     * Get log messages reported for a specific Host on a Server/Device by HostID
     * 
     * @param hostId 
     *     the ID of the Host for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostId(int hostId, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;    
    
    /**
     * Get log messages reported for a list of Hosts on a Server/Device by HostID
     * 
     * @param hostIds 
     *     the IDs of the Hosts for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostIds(int[] hostIds, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;    
    
    /**
     * Get log messages reported for a specific Service
     * 
     * @param hostName the host on which the service resides 
     * @param serviceDescr 
     *     the description of the Service for which we want the log messages
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByService(String hostName, String serviceDescr, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;
    
    /**
     * Get log messages for a service specified by the servicestatus id.
     * @param serviceStatusId the id of the service
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByServiceStatusId(int serviceStatusId, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResult)
    throws BusinessServiceException;

    /**
     * Get log messages reported for a specific HostGroup
     * 
     * @param name the name of the hostgroup  
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostGroupName(String hostGroupName, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) 
    throws BusinessServiceException;
        
    /**
     * Get log messages reported for a list of HostGroups
     * 
     * @param names the names of the hostgroups  
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostGroupNames(String[] hostGroupNames, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) 
    throws BusinessServiceException;
        
    /**
     * Get log messages reported for a specific HostGroup
     * 
     * @param id the id of the hostgroup  
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostGroupId(int id, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;
        
    /**
     * Get log messages reported for a list of HostGroups
     * 
     * @param ids the ids of the hostgroups  
	 * @param startDate start of date range for which to gather messages for the last occurrence entry
	 * @param endDate date for which to stop gathering messages for the last occurrence entry
     * @param filter A FilterCriteria object that refines which LogMessages should be returned.
     * @param sortCriteria A SortCriteria object that determines how results should be sorted.
     * @param firstResult -1 for no pagination or any integer value for the first record to return
     * @param maxResults Defines maximum size of page (objects) to return starting at the record defined by firstResult.
     * @return a list of LogMessages
     * @throws BusinessServiceException
     */
    public FoundationQueryList getLogMessagesByHostGroupIds(int[] ids, String startDate, String endDate, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults)
    throws BusinessServiceException;
        
	/**
	 * 
	 * @param LogMessageID
	 * @return
	 * @throws BusinessServiceException
	 */
	public LogMessage getLogMessageById(int LogMessageId) throws BusinessServiceException;
	
	/**
	 * Creates new un-persisted LogMessage
	 * @return
	 * @throws BusinessServiceException
	 */
	public LogMessage createLogMessage () throws BusinessServiceException;
	
	/**
	 * Persists the specified log message
	 * @param logMsg
	 * @throws BusinessServiceException
	 */
	public void saveLogMessage(LogMessage logMsg) throws BusinessServiceException;
	
	/**
	 * getLogMessageForHostServiceOPStatus 
	 * @param hostName hostName for which the logMessage has a link to. Can't be null. Will throw a CollageException if null
	 * @param serviceDescription serviceDescription for which the LogMessage has a link to. Ignored for lookup if null
	 * @param operationStatus OPerationStatus for LogMessage. Can't be null.
	 * @return collection of LogMessages that match the criteria.
	 */
	// this is being removed because the FilterCriteria will allow us to accomplish this in getLogMessageForHost(...)
	//public Collection getLogMessageForHostServiceOPStatus(final String hostName, final String serviceDescr, final String operationStatus) throws CollageException;
			
	/************ Admin methods ************/
    /** 
     * sets the ServiceStatusID to NULL in LogMessage table for all records
     * matching the service description; this method is used to preserve
     * referential integrity when a ServiceStatus is deleted; use with care,
     * after this query is issued it will no longer be possible to
     * re-associate the log messages with the ServiceStatus.
     *
     * @param serviceDescr the name of the host to which the service belongs
     * @param serviceDescr the description of the service to be unlinked
     * @return the number of log message affected
     */
    int unlinkLogMessagesFromService(int serviceStatusId);

    /** 
     * sets the HostStatusID to NULL in LogMessage table for all records
     * matching the host name; this method is used to preserve
     * referential integrity when a Host is deleted; use with care,
     * after this query is issued it will no longer be possible to
     * re-associate the log messages with the Host.
     *
     * @param hostName the description of the host to be unlinked
     * @return the number of log message affected
     */
    int unlinkLogMessagesFromHost(String hostName);

	/** 
	 * removes all LogMessages for a specific Server (Device) 
	 *
	 * @param deviceIdentification 
	 *   the IP or MAC address, as the case may be, 
	 *   that identifies this device in the system
	 *
	 * @return the number of LogMessages deleted
	 */
	int deleteLogMessagesForDevice(String deviceIdentification);
	
	/************ Consolidation Criteria related methods - should these be moved to the ConsolidationService? ************/
	/**
	 * Get LogMessage Object that matches the criteria
	 * @param String criteria Comma separated list of matching database fields
	 * 
	 * Returns a LogMessagethat matches the criteria or null
	 * if the criteria don't match.
	 */
	public LogMessage getLogMessageForConsolidationCriteria(int consolidationHash);
	
	/**
	 * Changes the stateIsState changed for the passed-in hash
	 * @param consolidationHash
	 */
	public void setIsStateChanged(int consolidationHash);

	/************ Special Method for PE's, don't remove or change - see note ************/
	/**
	 * Prepares a query object and adds it to the internal list for execution and returns the ID needed to execute the object
	 * NOTE: This is a placeholder.  It is used by Daniel Feinsmith for storing queries for future use.
	 * @param query
	 * @param appType
	 * @param startRange
	 * @param endRange
	 * @param orderedBy
	 * @param firstResult
	 * @param maxResults
	 * @return Integer Property Object that defines the Query Object id
	 */		
	public IntegerProperty createPreparedQuery(String query, 
											   String appType, 
											   String startRange, 
											   String endRange, 
											   org.groundwork.foundation.ws.model.impl.SortCriteria orderedBy, 
											   int firstResult, 
											   int maxResults);
	
	/**
	 * Performs a bulk update of log messages specified setting the operation status for each.
	 * Returns the number of log messages updated.
	 * @param logMessageIds
	 * @param opStatus
	 * @return
	 * @throws BusinessServiceException
	 */
    public int updateLogMessageOperationStatus (Collection<Integer> logMessageIds, String opStatus, String updatedBy, String comments) throws BusinessServiceException;
    
    
    /**
	 * Update log message operation status for the specified log messages.
	 * 
	 * @param logMessageIds
	 * @param opStatus
	 * @throws CollageException
	 */
	public int updateLogMessageOperationStatus(
			Collection<Integer> logMessageIds, String opStatus,
			HashMap<String, Object> prop) throws BusinessServiceException;

    /**
     * Returns a List of StateTransition instances for the specified host for the date range provided.
	 * Uses optimized LogMessageWindowService if date range in window.
	 *
     * @param hostName
     * @param startDate
     * @param endDate
     * @return list of state transitions
     * @throws BusinessServiceException
     */
    public List<StateTransition> getHostStateTransitions(String hostName, String startDate, String endDate)
			throws BusinessServiceException;

	/**
	 * Returns a List of StateTransition instances for the specified host for the date range provided.
	 * Optionally use optimized LogMessageWindowService if date range in window.
	 *
	 * @param hostName
	 * @param startDate
	 * @param endDate
	 * @param useWindow
	 * @return list of state transitions
	 * @throws BusinessServiceException
	 */
	public List<StateTransition> getHostStateTransitions(String hostName, String startDate, String endDate,
														 boolean useWindow)
			throws BusinessServiceException;

	/**
     * Returns a List of StateTransition instances for the specified service for the date range provided.
     * If no service name is provided then all service state transitions for the host will be returned.
     * The list will be ordered by service and then each service transition will be in ascending transition date order
     * For example, Service1 Transition1, Service1 Transition2, Service2 Transition1, Service2, Transition2, etc.
	 * Uses optimized LogMessageWindowService if date range in window.
	 *
     * @param hostName
     * @param serviceName
     * @param startDate
     * @param endDate
	 * @return list of state transitions
     * @throws BusinessServiceException
     */
    public List<StateTransition> getServiceStateTransitions(String hostName, String serviceName, String startDate,
															String endDate)
			throws BusinessServiceException;

	/**
	 * Returns a List of StateTransition instances for the specified service for the date range provided.
	 * If no service name is provided then all service state transitions for the host will be returned.
	 * The list will be ordered by service and then each service transition will be in ascending transition date order
	 * For example, Service1 Transition1, Service1 Transition2, Service2 Transition1, Service2, Transition2, etc.
	 * Optionally use optimized LogMessageWindowService if date range in window.
	 *
	 * @param hostName
	 * @param serviceName
	 * @param startDate
	 * @param endDate
	 * @param useWindow
	 * @return list of state transitions
	 * @throws BusinessServiceException
	 */
	public List<StateTransition> getServiceStateTransitions(String hostName, String serviceName, String startDate,
															String endDate, boolean useWindow)
			throws BusinessServiceException;

	/**
	 * Computes a state transition hash for a given log message
	 * @param logMsg input message
	 * @return state transition hash
	 */
	public Integer buildStateTransitionHash(LogMessage logMsg);

    /**
     * Gets the log messages when criteria is passed as input. Supports more than one dynamic property in the criteria.
     * @param filter
     * @return
     * @throws BusinessServiceException
     */
    public List<LogMessage> getLogMessagesByCriteria(FilterCriteria filter)   
            throws BusinessServiceException;

    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hql
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of host objects matching the query
     */
    public FoundationQueryList queryEvents(String hql, String hqlCount, int firstResult, int maxResults);

    /**
     * Remove a log message by id
     *
     * @param id the id to be deleted
     */
    public void removeLogMessage(int id);

}