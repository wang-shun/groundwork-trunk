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
package org.groundwork.foundation.bs.status;

import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.ServiceStatus;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

/**
 * Interface for accessing Status related information stored in Foundation.
 * This Business service retrieves data from or related to Service Status table.
 * 
 * The naming is confusing since what gets stored in ServiceStatus is configuration and Status for a check.
 * Naming needs to be reviewed and we even change the classes to have a Check (configuration) and a CheckStatus (result).
 * 
 * @author rruttimann@groundworkopensource.com
 * 
 * Created: Jan 8, 2007
 *
 */

public interface StatusService extends BusinessService 
{
	// Status Service Properties - Used to build FilterCriteria
	public static final String PROP_APP_TYPE_NAME = "applicationType.name";
	public static final String PROP_SERVICEDESCRIPTION = "serviceDescription";
	public static final String PROP_HOSTGROUPID = "host.hostGroups.hostGroupId";
	public static final String PROP_HOSTGROUPNAME = "host.hostGroups.name";
	public static final String PROP_HOSTID = "host.hostId";
	public static final String PROP_HOSTNAME = "host.hostName";
	public static final String PROP_SERVICESTATUSID = "serviceStatusId";
	
	/* Query services */
	/**
	 * getServices
	 * Generic query that is driven by the FilterCriteria object
	 * @param filter FilterCriteria that defines the query
	 * @param sortCriteria Sorting of result set.
	 * @param firstResult -1 if no paging otherwise starting record for paging
	 * @param maxResults number of records returned. Ignored if firstResult is -1
	 * @return FoundationQueryList of ServiceStatuses objects
	 * @throws BusinessServiceException
	 * Note: Changes from 1.5 DAO implementation: replaces gerSetvices, getServicesByName and getServiceByID
	 */
	FoundationQueryList getServices (FilterCriteria filter, SortCriteria sortCriteria,  int firstResult,  int maxResults ) throws BusinessServiceException;
	
	/**
	 * Get Services by Host name or ID
	 * getServicesForHost 
	 * getServicesForHostID
	 * @param hostName/HostId
	 * @param filter
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return FoundationQueryList
	 * @throws BusinessServiceException
	 * Note: Changes from 1.5 DAO implementation: replaces getService, getServicesForHostName, getServicesForHostID
	 */
	FoundationQueryList getServicesByHostName(String hostName, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;
	FoundationQueryList getServicesByHostId(int HostId, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;
	
    
    /**
     * Get all services belonging to a particular hostgroup
     * @param hgName/hgId
     * @param filter
     * @param sortCriteria
     * @param firstResult
     * @param maxResults
     * @return FoundationQueryList
     * @throws BusinessServiceException
     * Note: The filterCriteria allows to specify additional criteria such as ApplicationType, MonitorStatus, etc
     */
	FoundationQueryList getServicesByHostGroupName(String hgName, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;
    
    /**
     * Get all services belonging to a particular hostgroup
     * @param hgId
	 * @param filter
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	FoundationQueryList getServicesByHostGroupId( int hgId, FilterCriteria filter, SortCriteria sortCriteria,  int firstResult,  int maxResults) throws BusinessServiceException;

    /**
     * Get all services belonging to a particular category.
     *
     * @param categoryId category id
     * @return service status list
     * @throws BusinessServiceException
     */
	List<ServiceStatus> getServicesByCategoryId(int categoryId) throws BusinessServiceException;
	
	/**
	 * Get a service status with the Id specified.
	 * @param ssId
	 * @return
	 * @throws BusinessServiceException
	 */
	ServiceStatus getServiceById(int ssId) throws BusinessServiceException;
	
	/**
	 * get a service with the given description for the host specified.
	 * @param serviceDescription
	 * @param hostName
	 * @return
	 * @throws BusinessServiceException
	 */
	ServiceStatus getServiceByDescription(String serviceDescription, String hostName) throws BusinessServiceException;
    		
	/** Admin create/update/delete services */
	
    /**
     * Creates a new ServiceStatus with the service Name provided, and assigns
     * it to the Host passed; returns an un-persisted object with no guarantees
     * that it satisfies uniqueness constraints
     *
     * @param serviceDescr
     *   the service description to give to the ServiceStatus
     *
     * @param applicationType
     *   the type of application monitoring (NAGIOS, SYSLOG,
     *   JMX_SAMPLE)
     *
     * @param host
     *   the host on which this service resides
     */
    ServiceStatus createService(String serviceDescr, String applicationType, Host host) throws BusinessServiceException;
    
    /**
     * Standard service creation method
     * @return ServiceStatus object not persisted.
     * @throws BusinessServiceException
     */
    ServiceStatus createService() throws BusinessServiceException;
    
    /**
     * deletes ServiceStatus records with the ServiceDescription provided, and
     * de-associates from that service all LogMessages that were associated with
     * that Service
     * @return the number of records deleted
     */
    void deleteService(String hostName, String serviceDescription) throws BusinessServiceException;
    
    /**
     * deletes ServiceStatus records with the Service Id provided, and
     * de-associates from that service all LogMessages that were associated with
     * that Service
     * @return the number of records deleted
     */
    void deleteService(int serviceId) throws BusinessServiceException;
    
    /**
     * Standard deletion methods for Service Statuses
     * @param status
     * @throws BusinessServiceException
     */
    void deleteService(ServiceStatus service)throws BusinessServiceException;
    void deleteService(Collection<ServiceStatus> services)throws BusinessServiceException;
   
    /**
     * Standard persistent methods for Status messages
     * @param status
     * @throws BusinessServiceException
     */
    void saveService(ServiceStatus service) throws BusinessServiceException;
    void saveService(Collection<ServiceStatus> services) throws BusinessServiceException;

    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hql
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of service status objects matching the query
     */
    public FoundationQueryList queryServiceStatus(String hql,  String hqlCount, int firstResult, int maxResults);

	/**
	 * Invalidate all host state and caches.
	 *
	 * @param hostNames
	 */
	void invalidateHosts(Collection<String> hostNames);
}
