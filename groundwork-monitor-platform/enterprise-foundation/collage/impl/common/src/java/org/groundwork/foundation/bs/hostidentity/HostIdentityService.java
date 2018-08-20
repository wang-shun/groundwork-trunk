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

package org.groundwork.foundation.bs.hostidentity;

import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostIdentity;
import com.groundwork.collage.model.ServiceStatus;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * HostIdentityService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface HostIdentityService extends BusinessService {

    /**
     * General query by criteria API for HostIdentity instances.
     *
     * @param filterCriteria filter criteria
     * @param sortCriteria optional sort criteria or null
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return HostIdentity query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getHostIdentities(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * General query by HQL API for HostIdentity instances.
     *
     * @param hqlQuery HQL query string
     * @param hqlCountQuery HQL count query string
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return HostIdentity query results
     * @throws BusinessServiceException
     */
    FoundationQueryList queryHostIdentities(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Get HostIdentity instance primary host names.
     *
     * @return collection of primary host names
     * @throws BusinessServiceException
     */
    Collection<String> getHostNames() throws BusinessServiceException;

    /**
     * Get all HostIdentity instance host names.
     *
     * @return collection of host names
     * @throws BusinessServiceException
     */
    Collection<String> getAllHostNames() throws BusinessServiceException;

    /**
     * Get HostIdentity instance by host name. Host name will match any
     * host names for the HostIdentity instance.
     *
     * @param hostName host name to match
     * @return matched HostIdentity or null
     * @throws BusinessServiceException
     */
    HostIdentity getHostIdentityByHostName(String hostName) throws BusinessServiceException;

    /**
     * Get HostIdentity instance by primary id.
     *
     * @param id UUID primary id
     * @return matched HostIdentity or null
     * @throws BusinessServiceException
     */
    HostIdentity getHostIdentityById(UUID id) throws BusinessServiceException;

    /**
     * Get HostIdentity instance by host name or UUID string. Host name
     * will match any host names for the HostIdentity instance.
     *
     * @param idOrHostName host name or string UUID id to match
     * @return matched HostIdentity or null
     * @throws BusinessServiceException
     */
    HostIdentity getHostIdentityByIdOrHostName(String idOrHostName) throws BusinessServiceException;

    /**
     * Get HostIdentity instances that match a list of host name or UUID
     * strings. Host names will match any host names for the HostIdentity
     * instances.
     *
     * @param idOrHostNames collection of host names or string UUID ids to match
     * @return collection of matched HostIdentities
     * @throws BusinessServiceException
     */
    Collection<HostIdentity> getHostIdentitiesByIdOrHostNames(Collection<String> idOrHostNames) throws BusinessServiceException;

    /**
     * Lookup HostIdentity instance by host name or UUID string. Host name
     * will match any host names for the HostIdentity instance using the SQL
     * like syntax. UUID ids will only match the full UUID.
     *
     * @param idOrHostNamesLookup host name or string UUID id pattern to match
     * @return collection of matched HostIdentities
     * @throws BusinessServiceException
     */
    Collection<HostIdentity> getHostIdentitiesByIdOrHostNamesLookup(String idOrHostNamesLookup) throws BusinessServiceException;

    /**
     * Get Host instance by HostIdentity host name, UUID string, or Host host
     * name. Host name will match any HostIdentity host names or the Host host
     * name associated with the Host instance.
     *
     * @param idOrHostName host name or string UUID id to match
     * @return matched Host or null
     * @throws BusinessServiceException
     */
    Host getHostByIdOrHostName(String idOrHostName) throws BusinessServiceException;

    /**
     * Get Host instances that match a list of HostIdentity host names, UUID
     * strings, or Host host names. Host names will match any host names or the
     * Host host name associated with the Host instances.
     *
     * @param idOrHostNames host names or string UUID ids to match
     * @return collection of matched Hosts
     * @throws BusinessServiceException
     */
    Collection<Host> getHostsByIdOrHostNames(Collection<String> idOrHostNames) throws BusinessServiceException;

    /**
     * Lookup Host instance by HostIdentity host name, UUID string, or Host host
     * name. Host name will match any HostIdentity host names or the Host host
     * name associated with the Host instance using the SQL like syntax. UUID
     * id will only match the full UUID.
     *
     * @param idOrHostNamesLookup host name or string UUID id pattern to match
     * @return collection of matched Hosts
     * @throws BusinessServiceException
     */
    Collection<Host> getHostsByIdOrHostNamesLookup(String idOrHostNamesLookup) throws BusinessServiceException;

    /**
     * Get ServiceStatus instance by service description and HostIdentity host
     * name, UUID string, or Host host name. Host name will match any HostIdentity
     * host names or the Host host name associated with the Service Host instance.
     *
     * @param serviceDescription service description
     * @param idOrHostName host name or string UUID id to match
     * @return matched ServiceStatus or null
     * @throws BusinessServiceException
     */
    ServiceStatus getServiceByDescriptionAndHostIdOrHostName(String serviceDescription, String idOrHostName) throws BusinessServiceException;

    /**
     * Get Host and HostIdentity instances that match a specified regular expression
     * pattern by host name or UUID string. All Hosts and HostIdentities are returned
     * whether matched directly or indirectly via the HostIdentity to Host relationship.
     * The pattern matching is intended to follow that supported by {@link com.groundwork.collage.model.HostBlacklist},
     * most notably, it is case insensitive.
     *
     * @param regex regular expression pattern to match or null to match all
     * @param hosts returned list of matched Hosts or null
     * @param hostIdentities returned list of matched HostIdentities or null
     */
    void getHostsAndHostIdentitiesByIdOrHostNamesRegex(String regex, Collection<Host> hosts, Collection<HostIdentity> hostIdentities);

    /**
     * Create HostIdentity instance from Host instance.
     *
     * @param host Host instance
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(Host host) throws BusinessServiceException;

    /**
     * Create HostIdentity instance from Host instance and additional
     * host names.
     *
     * @param host Host instance
     * @param hostNames collection of additional host names
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(Host host, Collection<String> hostNames) throws BusinessServiceException;

    /**
     * Create HostIdentity instance from Host instance with specified UUID
     * primary id.
     *
     * @param id UUID primary id
     * @param host Host instance
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(UUID id, Host host) throws BusinessServiceException;

    /**
     * Create HostIdentity instance from Host instance and additional host
     * names with specified UUID primary id.
     *
     * @param id UUID primary id
     * @param host Host instance
     * @param hostNames collection of additional host names
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(UUID id, Host host, Collection<String> hostNames) throws BusinessServiceException;

    /**
     * Create HostIdentity instance with host name.
     *
     * @param hostName host name
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(String hostName) throws BusinessServiceException;

    /**
     * Create HostIdentity instance with host name and additional
     * host names.
     *
     * @param hostName host name
     * @param hostNames collection of additional host names
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(String hostName, Collection<String> hostNames) throws BusinessServiceException;

    /**
     * Create HostIdentity instance with host name with specified UUID
     * primary id.
     *
     * @param id UUID primary id
     * @param hostName host name
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(UUID id, String hostName) throws BusinessServiceException;

    /**
     * Create HostIdentity instance with host name and additional host
     * names with specified UUID primary id.
     *
     * @param id UUID primary id
     * @param hostName host name
     * @param hostNames collection of additional host names
     * @return created HostIdentity instance
     * @throws BusinessServiceException
     */
    HostIdentity createHostIdentity(UUID id, String hostName, Collection<String> hostNames) throws BusinessServiceException;

    /**
     * Rename HostIdentity primary and Host host name, retaining original
     * host name as an additional host name. HostIdentity and Host instances
     * are persistently updated after rename.
     *
     * @param idOrHostName id or host name of HostIdentity
     * @param newHostName new host name
     * @return success flag
     * @throws BusinessServiceException
     */
    boolean renameHostIdentity(String idOrHostName, String newHostName) throws BusinessServiceException;

    /**
     * Add additional host name to HostIdentity. HostIdentity instance
     * is persistently updated after add.
     *
     * @param idOrHostName id or host name of HostIdentity
     * @param addHostName host name to add
     * @return success flag
     * @throws BusinessServiceException
     */
    boolean addHostNameToHostIdentity(String idOrHostName, String addHostName) throws BusinessServiceException;

    /**
     * Remove additional host name from HostIdentity. The HostIdentity primary
     * host name cannot be removed. HostIdentity instance is persistently updated
     * after remove.
     *
     * @param idOrHostName id or host name of HostIdentity
     * @param removeHostName host name to remove
     * @return success flag
     * @throws BusinessServiceException
     */
    boolean removeHostNameFromHostIdentity(String idOrHostName, String removeHostName) throws BusinessServiceException;

    /**
     * Remove additional host name from HostIdentity. The HostIdentity primary
     * host name cannot be removed. HostIdentity instance is persistently updated
     * after remove.
     *
     * @param removeHostName host name of HostIdentity and to remove
     * @return success flag
     * @throws BusinessServiceException
     */
    boolean removeHostNameFromHostIdentity(String removeHostName) throws BusinessServiceException;

    /**
     * Remove all additional host names from HostIdentity. The HostIdentity primary
     * host name cannot be removed. HostIdentity instance is persistently updated
     * after remove.
     *
     * @param idOrHostName id or host name of HostIdentity
     * @return success flag
     * @throws BusinessServiceException
     */
    boolean removeAllHostNamesFromHostIdentity(String idOrHostName) throws BusinessServiceException;

    /**
     * Save HostIdentity instance.
     *
     * @param hostIdentity HostIdentity instance
     * @throws BusinessServiceException
     */
    void saveHostIdentity(HostIdentity hostIdentity) throws BusinessServiceException;

    /**
     * Save HostIdentity instances.
     *
     * @param hostIdentities HostIdentity instances
     * @throws BusinessServiceException
     */
    void saveHostIdentities(List<HostIdentity> hostIdentities) throws BusinessServiceException;

    /**
     * Delete HostIdentity instance by id.
     *
     * @param id UUID primary id to delete
     * @throws BusinessServiceException
     */
    void deleteHostIdentityById(UUID id) throws BusinessServiceException;

    /**
     * Delete HostIdentity instance by id or host name.
     *
     * @param idOrHostName host name or string UUID id to delete
     * @return deleted status
     * @throws BusinessServiceException
     */
    boolean deleteHostIdentityByIdOrHostName(String idOrHostName) throws BusinessServiceException;

    /**
     * Delete HostIdentity instance.
     *
     * @param hostIdentity HostIdentity instance
     * @throws BusinessServiceException
     */
    void deleteHostIdentity(HostIdentity hostIdentity) throws BusinessServiceException;

    /**
     * Delete HostIdentity instances.
     *
     * @param hostIdentities HostIdentity instances
     * @throws BusinessServiceException
     */
    void deleteHostIdentities(List<HostIdentity> hostIdentities) throws BusinessServiceException;

    /**
     * Invalidate all host state and caches.
     *
     * @param hostNames
     */
    void invalidateHosts(Collection<String> hostNames);
}
