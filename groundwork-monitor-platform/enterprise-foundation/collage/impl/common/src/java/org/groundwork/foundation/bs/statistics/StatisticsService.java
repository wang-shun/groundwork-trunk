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
package org.groundwork.foundation.bs.statistics;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FoundationQueryList;

import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.impl.NagiosStatisticProperty;
import com.groundwork.collage.model.impl.StateStatistics;
import com.groundwork.collage.model.impl.StatisticProperty;

/**
 * StatisticsService Business Object Interface
 */
public interface StatisticsService extends BusinessService
{	
	public static final String INTERFACE_NAME = "org.groundwork.foundation.bs.statistics.StatisticsService";
	
	// Notify Attribute Constants
	public static final String NOTIFY_ATTR_ENTITY_ID = "ID";
	public static final String NOTIFY_ATTR_ENTITY_NAME = "Name";
	public static final String NOTIFY_ATTR_HOST_LIST = "HostNameList";
	public static final String NOTIFY_ATTR_IS_DELETED = "IsDeleted";
		
	// Log Message (Event) Statistic Types
	public static final String STAT_TYPE_MONITOR_STATUS = "MonitorStatus";
	public static final String STAT_TYPE_SEVERITY_STATUS = "Severity";
	public static final String STAT_TYPE_PRIORITY_STATUS = "Priority";
	public static final String STAT_TYPE_OPERATION_STATUS = "OperationStatus";	
	public static final String STAT_TYPE_MONITOR_STATUS_WITH_OPEN = "MonitorStatusWithOpen";
	
	// Host Statistic Types
    public static final java.lang.String STAT_TYPE_TOTALS = "Totals";
    public static final java.lang.String STAT_TYPE_COUNT = "Count";
	
	/**
	 * Un-Initialize statistics gathering -- shutdown threads
	 * 
	 */
	public void stopStatisticsCalculation();

	/**
	 * Start statistic calculations.
	 */
	public void startStatisticsCalculation();

	/**
	 * Start statistic calculations.
	 * 
	 * @param propListOfHostStatuses
	 * @param propListOfServiceStatuses
	 * @param propListOfNagiosProperties
	 */
	public void startStatisticsCalculation(String propListOfHostStatuses, 
										   String propListOfServiceStatuses,
										   String propListOfNagiosProperties);
	
	/*************************************************************************/
	/** Host Statistic Methods **/
	/*************************************************************************/
	
	/** All Statistics for each host */
	public Collection<StateStatistics> getAllHostStatistics() throws BusinessServiceException;
			
	
	/** All Statistics for each host */
	public StateStatistics getAllHostStatisticsByNames(String[] hostNames) throws BusinessServiceException;
	
	
	/**
	 * Returns host statistics for all hosts in the specified host group.  
	 * If the host group parameter is null or empty then all hostgroup statistics are returned.
	 * 
	 * @param hostGroupName 
	 * @return
	 */
	public StateStatistics getHostStatisticsByHostGroupName(String hostGroupName) throws BusinessServiceException;	
		
	/**
	 * Returns host statistics for all hosts in the specified host group.  
	 * If the host group parameter is < 1 then all hostgroup statistics are returned.
	 * 
	 * @param hostGroupId
	 * @return
	 */
	public StateStatistics getHostStatisticsByHostGroupId(int hostGroupId) throws BusinessServiceException;
	
	/**
	 * Returns a collection of host statistics for each host group defined.  If one or more of the 
	 * hostgroup(s) is not found its statistics are not provided and NO exception is thrown. 
	 * 
	 * @param hostGroupNames
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StateStatistics> getHostStatisticsByHostGroupNames(Collection<String> hostGroupNames) throws BusinessServiceException;

	/**
	 * Returns a collection of host statistics for each host group defined.  If one or more of the 
	 * hostgroup(s) is not found its statistics are not provided and NO exception is thrown. 
	 * 
	 * @param hostGroupIds
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StateStatistics> getHostStatisticsByHostGroupIds(Collection<Integer> hostGroupIds) throws BusinessServiceException;

	
	/*************************************************************************/
	/** Service Statistic Methods **/
	/*************************************************************************/
	
	/** All Statistics for each host */
	public Collection<StateStatistics> getAllServiceStatistics() throws BusinessServiceException;

	/**
	 * Returns service statistics for all services in the specified host group.  
	 * If the host group parameter is null or empty then all hostgroup service statistics are returned.
	 * 
	 * @param hostGroupName
	 * @return
	 */
	public StateStatistics getServiceStatisticsByHostGroupName(String hostGroupName) throws BusinessServiceException;
		
	/**
	 * Returns service statistics for all services in the specified host group.  
	 * If the host group parameter is < 1 then all hostgroup service statistics are returned.
	 * 
	 * @param hostGroupId
	 * @return
	 */
	public StateStatistics getServiceStatisticsByHostGroupId(int hostGroupId) throws BusinessServiceException;
		
	/**
	 * Returns service statistics for the specified host  
	 *
	 * @param host
	 * @return
	 * @throws BusinessServiceException
	 */
	public StateStatistics getServiceStatisticByHostName(String hostName) throws BusinessServiceException;
	
	/**
	 * Returns service statistics for the specified host  
	 * 
	 * @param hostId
	 * @return
	 * @throws BusinessServiceException
	 */
	public StateStatistics getServiceStatisticByHostId(int hostId) throws BusinessServiceException;
	
	/**
	 * Returns a collection of service statistics for each host group defined.  If one or more of the 
	 * hostgroup(s) is not found its statistics are not provided and NO exception is thrown. 
	 * 
	 * @param hostGroupNames
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StateStatistics> getServiceStatisticsByHostGroupNames(Collection<String> hostGroupNames) throws BusinessServiceException;

	/** All Statistics for each host */
	public StateStatistics getServiceStatisticsByServiceIDs(int[] serviceIds)
			throws BusinessServiceException;
	/**
	 * Returns a collection of service statistics for each host group defined.  If one or more of the 
	 * hostgroup(s) is not found its statistics are not provided and NO exception is thrown. 
	 * 
	 * @param hostGroupIds
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StateStatistics> getServiceStatisticsByHostGroupIds(Collection<Integer> hostGroupIds) throws BusinessServiceException;

	
	/*************************************************************************/
	/** Application Statistic Methods **/
	/*************************************************************************/
	
	/**
	 * Returns state statistics for the specified application (e.g. NAGIOS) and host group.
	 * If host group is null or empty then all statistics are returned for the specified application. 
	 * TODO:  Remove Nagios Specific Collection
	 * @param appName
	 * @param hostGroupName
	 * @return
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatistics(String appName, String hostGroupName) throws BusinessServiceException;	
	
	/**
	 * Returns state statistics for the specified application (e.g. NAGIOS) and host group.
	 * If host group is < 1 then all statistics are returned for the specified application. 
	 * 
	 * @param appName
	 * @param hostGroupId
	 * @return
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatistics(String appName, int hostGroupId) throws BusinessServiceException;
	
	
	/**
	 * Returns state statistics for the specified application (e.g. NAGIOS) and host group.
	 * If host group is null or empty then all statistics are returned for the specified application. 
	 * TODO:  Remove Nagios Specific Collection
	 * @param appId
	 * @param hostGroupName
	 * @return
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatistics(int appId, String hostGroupName) throws BusinessServiceException;
	
	/**
	 * Returns state statistics for the specified application (e.g. NAGIOS) and host group.
	 * If host group is < 1 then all statistics are returned for the specified application. 
	 * TODO:  Remove Nagios Specific Collection
	 * @param appId
	 * @param hostGroupId
	 * @return
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatistics(int appId, int hostGroupId) throws BusinessServiceException;	

	/*************************************************************************/
	/** Log Message Statistic Methods **/
	/*************************************************************************/
	
	/**
	 * @param appName
	 * @param hostName
	 * @param startDate
	 * @param endDate
	 * @param statistcType
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StatisticProperty> getEventStatisticsByHostName(String appName, String hostName, String startDate, String endDate, String statisticType) throws BusinessServiceException;
				
	/**
	 * @param appName
	 * @param hostGroupname
	 * @param startDate
	 * @param endDate
	 * @param statistcType
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StatisticProperty> getEventStatisticsByHostGroupName (String appName, String hostGroupname, String startDate, String endDate, String statisticType) throws BusinessServiceException;
		
	/*************************************************************************/
	/** Total / Overall Statistic Methods **/
	/*************************************************************************/
		
	/**
	 * Returns statistics totals for all hosts
	 * @return
	 * @throws BusinessServiceException
	 */
	public StateStatistics getHostStatisticTotals() throws BusinessServiceException;
	
	/**
	 * Returns statistics totals for all services
	 * 
	 * @return
	 * @throws BusinessServiceException
	 */
	public StateStatistics getServiceStatisticTotals() throws BusinessServiceException;	
	
	/**
	 * Returns statistics totals for the specified application's hosts and services
	 * TODO:  Remove Nagios Specific Collection
	 * @param appName
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatisticTotals(String appName) throws BusinessServiceException;	
	
	/**
	 * Returns statistics totals for the specified application's hosts and services
	 * TODO:  Remove Nagios Specific Collection
	 * @param appId
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatisticTotals(int appId) throws BusinessServiceException;	
	
	
	/**
	 * Get Application Statistics for a Host identified by the HostID
	 * @param appId
	 * @param hostId
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatisticsHost(int appId, int hostId) throws BusinessServiceException;	
	
	/**
	 * Get Application Statistics for a Host identified by the Host Name
	 * @param appId
	 * @param hostName
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatisticsHost(int appId, String hostName) throws BusinessServiceException;	
	
	/**
	 * Get Application Statistics for a Host identified by the array  of Host Name
	 * @param appId
	 * @param hostName
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatisticsHostList(int appId, String[] hostNames) throws BusinessServiceException;	
	
	/**
	 * Get Application statistsics for all Hosts and Services
	 * @param appId
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<NagiosStatisticProperty> getApplicationStatisticsTotals(int appId)throws BusinessServiceException;
	
	/**
	 * Method returns StatisticProperties with the Name of the property (e.g OK, WARNING,..) and the counts of HostGroups that have any of these counts greater than 0
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StatisticProperty> getHostGroupStateCountService() throws BusinessServiceException;
	
	/**
	 * Method returns StatisticProperties with the Name of the property (e.g UP, PENDING,..) and the counts of HostGroups that have any of these counts greater than 0
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StatisticProperty> getHostGroupStateCountHost() throws BusinessServiceException;
	
	/**
	 * Returns list of host statuses for which statistics are being calculated
	 * @return
	 */
	public List<String> getHostStatusList ();
	
	/**
	 * Returns list of service statuses for which statistics are being calculated
	 * @return
	 */
	public List<String> getServiceStatusList ();
	
	/**
	 * Returns list of app properties for which statistics are being calculated
	 * @return
	 */
	public List<String> getApplicationPropertyList (int appId);
	
	/**
	 * Returns statistics for the specified entity type which match the FilterCriteria specified
	 * @param entityType
	 * @param parameters
	 * @return
	 */
	public FoundationQueryList getStatistics (EntityType entityType, Map<String, Object> parameters);
	
	/**
	 * Returns the percentage of UP state for hosts in a given hostgroup
	 * @param hgName
	 * @return
	 * @throws BusinessServiceException
	 */
	public double getHostAvailabilityForHostgroup(String hgName)  throws BusinessServiceException;
	
	/**
	 * Returns the percentage of OK state for services in a given hostgroup
	 * @param hgName
	 * @return
	 * @throws BusinessServiceException
	 */
	public double getServiceAvailabilityForHostGroup(String hgName)  throws BusinessServiceException;
	
	
	/**
	 * Returns the percentage of OK state for services in a given servicegroup
	 * @param hgName
	 * @return
	 * @throws BusinessServiceException
	 */
	public double getServiceAvailabilityForServiceGroup(String sgName)  throws BusinessServiceException;
	
	public Collection<NagiosStatisticProperty> getNagiosStatisticsForServiceGroup(
			int appId, String serviceGroupName)
			throws BusinessServiceException ;
	
	
	public StateStatistics getServiceStatisticsByServiceGroupName(
			String serviceGroupName) throws BusinessServiceException ;
	
	
	/**
	 * Gets service statistics for all service groups
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<StateStatistics> getServiceStatisticsForAllServiceGroups()
	throws BusinessServiceException ;
	
	

}
