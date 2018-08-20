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
package org.groundwork.foundation.bs.host;

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostStatus;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

/**
 * Interface for accessing Host related information stored in Foundation.
 * This Business service retrieves data from or related to Host table.
 * 
 * @author rruttimann@groundworkopensource.com
 *
 * Created: Jan 8, 2007
 */
public interface HostService extends BusinessService 
{
	/** Query services */
	
	/**
	 * generic method to query for hosts by using criterias
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 * Replaces  getHostsByCriteria
	 */
	FoundationQueryList getHosts(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;
	
	/**
	 * Finds and returns the specified host.  If the host cannot
	 * be found, null is returned.
	 * @param hostName/hostID
	 * @return Host
	 * @throws BusinessServiceException
	 */
	public Host getHostByHostName(String hostName) throws BusinessServiceException;

    public Host getHostByHostId(int hostID) throws BusinessServiceException;
    
    /**
     * Finds all hosts specified in comma delimited list of host names
     * @param hostList
     * @return
     * @throws BusinessServiceException
     */
    public Collection<Host> getHosts(List<String> hostList)
    throws BusinessServiceException;
    
    /**
     * Finds all hosts with the specified device id.
     * @param devID
     * @return
     * @throws BusinessServiceException
     */
    public Collection<Host> getHostsByDeviceId(int devID)
    throws BusinessServiceException;
    
    /**
     * Finds all hosts with the specified device 
     * @param devDescrip
     * @return
     * @throws BusinessServiceException
     */
    public Collection<Host> getHostsByDeviceIdentification(String devDescrip)
    throws BusinessServiceException;
    
    /**
     * gets all hosts that belong to the specified monitor server.
     * @param monitorServer
     * @param firstResult
     * @param maxResults
     * @return FoundationQueryList
     * @throws BusinessServiceException
     */
    public FoundationQueryList getHostsByMonitorServer(String monitorServer,int firstResult, int maxResults)
    throws BusinessServiceException;
    
    /**
     * Gets all hosts belonging to the hostgroup specified by hostgroup id.
     * @param hgId
     * @param filter
     * @param sortCriteria
     * @param firstResult
     * @param maxResults
     * @return
     * @throws BusinessServiceException
     */
    public FoundationQueryList getHostsByHostGroupId(int hgId, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;
    
    /**
     * Gets all hosts belonging to the hostgroup specified by name
     * @param hgName
     * @param filter
     * @param sortCriteria
     * @param firstResult
     * @param maxResults
     * @return
     * @throws BusinessServiceException
     */
    public FoundationQueryList getHostsByHostGroupName(String hgName, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;
    
	/**
	 * Returns all hosts that contain the named Service Name
	 * @param serviceName
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<Host> getHostsByServiceName(String serviceName) throws BusinessServiceException;
	
	/**
	 * Gets all host names.
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<String> getHostList() throws BusinessServiceException;


	/**
	 * getStatusByHostName retrieves Status for given Host Name
	 * @param hostName
	 * @return
	 * @throws BusinessServiceException
	 */
	public HostStatus getStatusByHostName(String hostName) throws BusinessServiceException;


	/**
	 * getStatusByHostId - Get HostStatus by ID. Used for updating existing objects
	 * @param hostId
	 * @return
	 * @throws BusinessServiceException
	 */
	public HostStatus getStatusByHostId(int hostId) throws BusinessServiceException;
	
	/** Lookup hosts by full/partial name
	 * @param hostName
	 * @return
	 * @throws BusinessServiceException
	 */
	public Collection<Host> hostLookup(String hostName) throws BusinessServiceException;
		
	/** Admin create/update/delete services */
	
	/**
	 * Creates (but does not save) the Host with the given name, on the device
	 * indicated, without a HostStatus
	 *
	 * @param name the name given to this host
	 * @param device the physical Device on which this host resides
	 */
	public Host createHost(String name, Device device) throws BusinessServiceException;
	
	/**
	 * Standard methods for creating hosts
	 * Note: To persist host call save on the single object or collection
	 * @throws BusinessServiceException
	 */
	public Host createHost() throws BusinessServiceException;
		
	/** 
	 * deletes the Host for the host name provided, and the related HostStatus,
	 * and ServiceStatus - unlinks (but does not delete) all LogMessages that
	 * were previously attached to this Host
	 */
	public void deleteHostByName(String hostName) throws BusinessServiceException;
	public void deleteHostById(int hostId) throws BusinessServiceException;
	
	public void deleteHost(Host host) throws BusinessServiceException;
	public void deleteHost(Collection<Host> hostList) throws BusinessServiceException;

	/**
	 * Creates (but does not save) a HostStatus for the Host provided
	 *
	 * @param host the host for which we want a status
     * @param applicationType the type of application monitoring (NAGIOS, SYSLOG, JMX_SAMPLE)
	 */
	public HostStatus createHostStatus(String applicationType, Host host) throws BusinessServiceException;
	
	/**
	 * Save HostStatus
	 * @param hostStatus
	 * @throws BusinessServiceException
	 */
	public void saveHostStatus (HostStatus hostStatus) throws BusinessServiceException;
	
	/** deletes the HostStatus for the host name provided */
	public void deleteHostStatus(String hostName) throws BusinessServiceException;
	
	/**
	 * Standard methods to persist host objects
	 * @param host
	 * @throws BusinessServiceException
	 */
	public void saveHost(Host host) throws BusinessServiceException;
	public void saveHost(Collection<Host> hostList) throws BusinessServiceException;

    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hql
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of host objects matching the query
     */
    public FoundationQueryList queryHosts(String hql,  String hqlCount, int firstResult, int maxResults);

	/**
	 * Invalidate all host state and caches.
	 *
	 * @param hostNames
	 */
	void invalidateHosts(Collection<String> hostNames);
}
