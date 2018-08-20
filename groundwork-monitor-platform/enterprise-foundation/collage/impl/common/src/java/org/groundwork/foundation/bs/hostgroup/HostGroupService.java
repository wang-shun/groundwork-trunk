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
package org.groundwork.foundation.bs.hostgroup;

import com.groundwork.collage.model.HostGroup;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

/**
 * Interface for accessing HostGroup related information stored in Foundation.
 * This Business service retrieves data from or related to HostGroup table.
 * 
 * TODO: This will be replaced with Category implementation since HostGroup is just
 * a special case of the Category.

 * @author rruttimann@groundworkopensource.com
 *
 * Created: Jan 8, 2007
 */
public interface HostGroupService extends BusinessService 
{
	/** Query services */

	/**
	 * Generic method to query hostgroup information by Filter criterias
	 * @param filter
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return FoundationQueryList
	 * @throws BusinessServiceException
	 * Note: Application Type is a Filter criteria
	 */
	
	public FoundationQueryList getHostGroups(FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Get all host groups belonging to a particular category.
     *
     * @param categoryId category id
     * @return host group list
     * @throws BusinessServiceException
     */
    List<HostGroup> getHostGroupsByCategoryId(int categoryId) throws BusinessServiceException;

    /**
	 * Gets all the hostgroup corresponding the name given, performs a shallow
	 * retrieval that only fetches the HostGroup record itself;
	 * Returns null if the HostGroup is not found.
	 * @param hgName
	 * @param filter
	 * @return HostGroup object
	 * @throws BusinessServiceException
	 */
	public HostGroup getHostGroupByName(String hgName) throws BusinessServiceException;

	/**
     * Gets HostGroup with the ID provided.  Always does "shallow" retrieval.
     * @param hgID
     * @param filter
     * @return HostGroup object
     * @throws BusinessServiceException
     */
    public HostGroup getHostGroupById(int hgID)
    throws BusinessServiceException;
    
    	
	/** Statistic Services */
	
	/**
	 * Returns the number of services in the specified host group.
	 * @param hostGroupName
	 * @return
	 * @throws BusinessServiceException
	 */
	public int getHostGroupServiceCount (String hostGroupName) throws BusinessServiceException;
	
	/**
	 * Returns the number of host in the specifiec host group
	 * @param hostGroupName
	 * @return
	 * @throws BusinessServiceException
	 */
	public long getHostGroupHostCount (String hostGroupName) throws BusinessServiceException;

	
	/** Admin services */
	
	/**
	 * Deletes the HostGroup with the name provided, but does not delete any of
	 * the Hosts in the HostGroup
	 *
	 * @param hostGroupName the name of the HostGroup to be deleted
	 */
	public void deleteHostGroupByName(String hostGroupName) throws BusinessServiceException;
	
	public void deleteHostGroupById(int hostGroupId) throws BusinessServiceException;
	
	/**
	 * Standard delete methods for HostGroup objects
	 * @param hostGroup
	 * @throws BusinessServiceException
	 */
	public void deleteHostGroup(HostGroup hostGroup) throws BusinessServiceException;
	public void deleteHostGroup(Collection<HostGroup> hostGroups) throws BusinessServiceException;
	
	/**
	 * Standard create method for HostGroup object which is not persisted yet.
	 * @return
	 * @throws BusinessServiceException
	 */
	public HostGroup createHostGroup() throws BusinessServiceException;
	public HostGroup createHostGroup(String hostgroupName) throws BusinessServiceException;
	
	/**
	 * Standard delete methods for HostGroup objects
	 * @param hostGroup
	 * @throws BusinessServiceException
	 */
	public void saveHostGroup(HostGroup hostGroup) throws BusinessServiceException;
	public void saveHostGroup(Collection<HostGroup> hostGroups) throws BusinessServiceException;

    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hql
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of host objects matching the query
     */
    public FoundationQueryList queryHostGroups(String hql, String hqlCount, int firstResult, int maxResults);

}
