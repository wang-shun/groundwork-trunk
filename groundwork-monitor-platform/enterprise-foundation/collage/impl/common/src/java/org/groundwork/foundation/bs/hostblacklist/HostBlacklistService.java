/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.foundation.bs.hostblacklist;

import com.groundwork.collage.model.HostBlacklist;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

/**
 * HostBlacklistService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface HostBlacklistService extends BusinessService {

    /**
     * General query by criteria API for HostBlacklist instances.
     *
     * @param filterCriteria filter criteria
     * @param sortCriteria optional sort criteria or null
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return HostBlacklist query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getHostBlacklists(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * General query by HQL API for HostBlacklist instances.
     *
     * @param hqlQuery HQL query string
     * @param hqlCountQuery HQL count query string
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return HostBlacklist query results
     * @throws BusinessServiceException
     */
    FoundationQueryList queryHostBlacklists(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Get HostBlacklist instance host names.
     *
     * @return collection of host names
     * @throws BusinessServiceException
     */
    Collection<String> getHostNames() throws BusinessServiceException;

    /**
     * Match host name against blacklist host names taken as case-insensitive
     * match patterns. Pattern matches are made against a locally cached set
     * of host names. Periodic refreshes are made to keep the cache current.
     *
     * @param hostName host name to match
     * @return match result
     */
    boolean matchHostNameAgainstHostNames(String hostName);

    /**
     * Get HostBlacklist instance by host name.
     *
     * @param hostName host name to match
     * @return matched HostBlacklist or null
     * @throws BusinessServiceException
     */
    HostBlacklist getHostBlacklistByHostName(String hostName) throws BusinessServiceException;

    /**
     * Get HostBlacklist instance by primary id.
     *
     * @param id primary id
     * @return matched HostBlacklist or null
     * @throws BusinessServiceException
     */
    HostBlacklist getHostBlacklistById(int id) throws BusinessServiceException;

    /**
     * Get HostBlacklist instances that match a list of host names.
     *
     * @param hostNames host names
     * @return collection of matched HostBlacklists
     * @throws BusinessServiceException
     */
    Collection<HostBlacklist> getHostBlacklistsByHostNames(Collection<String> hostNames) throws BusinessServiceException;

    /**
     * Create HostBlacklist instance with host name.
     *
     * @param hostName host name
     * @return created HostBlacklist instance
     * @throws BusinessServiceException
     */
    HostBlacklist createHostBlacklist(String hostName) throws BusinessServiceException;

    /**
     * Save HostBlacklist instance.
     *
     * @param hostBlacklist HostBlacklist instance
     * @throws BusinessServiceException
     */
    void saveHostBlacklist(HostBlacklist hostBlacklist) throws BusinessServiceException;

    /**
     * Save HostBlacklist instances.
     *
     * @param hostBlacklists HostBlacklist instances
     * @throws BusinessServiceException
     */
    void saveHostBlacklists(List<HostBlacklist> hostBlacklists) throws BusinessServiceException;

    /**
     * Delete HostBlacklist instance by id.
     *
     * @param id primary id to delete
     * @throws BusinessServiceException
     */
    void deleteHostBlacklistById(int id) throws BusinessServiceException;

    /**
     * Delete HostBlacklist instance by host name.
     *
     * @param hostName host name to delete
     * @return deleted status
     * @throws BusinessServiceException
     */
    boolean deleteHostBlacklistByHostName(String hostName) throws BusinessServiceException;

    /**
     * Delete HostBlacklist instance.
     *
     * @param hostBlacklist HostBlacklist instance
     * @throws BusinessServiceException
     */
    void deleteHostBlacklist(HostBlacklist hostBlacklist) throws BusinessServiceException;

    /**
     * Delete HostBlacklist instances.
     *
     * @param hostBlacklists HostBlacklist instances
     * @throws BusinessServiceException
     */
    void deleteHostBlacklists(List<HostBlacklist> hostBlacklists) throws BusinessServiceException;
}
