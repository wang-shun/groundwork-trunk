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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.HostIdentity;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import com.groundwork.collage.util.AutocompleteNames;
import com.groundwork.collage.util.AutocompleteNamesIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.UUIDEntityBusinessServiceImpl;
import org.groundwork.foundation.bs.cache.BusinessCacheService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * HostIdentityServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostIdentityServiceImpl extends UUIDEntityBusinessServiceImpl implements HostIdentityService, AutocompleteNames {

    private static Log log = LogFactory.getLog(HostIdentityServiceImpl.class);

    /** Default sort criteria */
    private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(HostIdentity.HP_HOST_NAME);

    /** Host Service used for fallback and to update hosts on rename */
    private HostService hostService;

    /** Status Service used for fallback */
    private StatusService statusService;

    /** Host Identity Autocomplete Service */
    private Autocomplete hostIdentityAutocompleteService;

    /** Cache Service */
    private BusinessCacheService cacheService = null;

    /**
     * HostIdentityServiceImpl FoundationDAO constructor.
     *
     * @param foundationDAO service Foundation DAO
     * @param hostService Host Service instance
     */
    public HostIdentityServiceImpl(FoundationDAO foundationDAO, HostService hostService, StatusService statusService) {
        super(foundationDAO, HostIdentity.INTERFACE_NAME, HostIdentity.COMPONENT_NAME);
        this.hostService = hostService;
        this.statusService = statusService;
    }

    private ConcurrentHashMap<String, Set<String>> idKeys = new ConcurrentHashMap<>();
    private final LoadingCache<String, UUID> ids = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<String, UUID>() {
                @Override
                public void onRemoval(RemovalNotification<String, UUID> removalNotification) {
                    if (removalNotification.getCause() == RemovalCause.EXPIRED) {
                        idKeys.remove(removalNotification.getKey());
                    }
                }
            })
            .build(new CacheLoader<String, UUID>() {
                public UUID load(@SuppressWarnings("NullableProblems") String hostName) {
                    List results = _foundationDAO.query("from HostIdentity hi inner join hi."
                            + HostIdentity.HP_HOST_NAMES + " hn where lower(hn.id) = ?", hostName);
                    if (CollectionUtils.isEmpty(results)) {
                        return null;
                    }
                    HostIdentity hostIdentity = (HostIdentity) results.get(0);
                    getIdKeyHostNames(hostIdentity.getHostName().toLowerCase(), true).add(hostName);
                    return hostIdentity.getHostIdentityId();
                }
            });

    private Collection<String> getIdKeyHostNames(String hostName, boolean create) {
        Collection<String> hostNames = idKeys.get(hostName);
        while (create && hostNames == null) {
            idKeys.putIfAbsent(hostName, Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));
            hostNames = idKeys.get(hostName);
        }
        return hostNames;
    }

    private UUID getHostUUIDByHostName(String hostname) throws BusinessServiceException {
        try {
            return ids.get(hostname.toLowerCase());
        } catch (ExecutionException | CacheLoader.InvalidCacheLoadException e) {
            return null;
        }
    }

    @Override
    public FoundationQueryList getHostIdentities(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostIdentities");
        sortCriteria = ((sortCriteria != null) ? sortCriteria : DEFAULT_SORT_CRITERIA);
        FoundationQueryList results = query(filterCriteria, sortCriteria, firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    @Override
    public Collection<String> getHostNames() throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostNames");
        Collection<String> results = _foundationDAO.query("select hi." + HostIdentity.HP_HOST_NAME + " from HostIdentity hi");
        stopMetricsTimer(timer);
        return results;
    }

    @Override
    public Collection<String> getAllHostNames() throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getAllHostNames");
        Collection<String> results = _foundationDAO.query("select elements(hi." + HostIdentity.HP_HOST_NAMES + ") from HostIdentity hi");
        stopMetricsTimer(timer);
        return results;
    }

    @Override
    public FoundationQueryList queryHostIdentities(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "queryHostIdentities");
        String testHqlQuery = hqlQuery.trim().toLowerCase();
        if (!(testHqlQuery.startsWith("from ") || testHqlQuery.startsWith("select "))) {
            throw new BusinessServiceException("Only HostIdentityService HQL SELECT/FROM query supported");
        }
        String testHqlCountQuery = hqlCountQuery.trim().toLowerCase();
        if (!testHqlCountQuery.startsWith("select ") || !testHqlCountQuery.contains(" count(*) ")) {
            throw new BusinessServiceException("Only HostIdentityService HQL SELECT count(*) query supported");
        }
        FoundationQueryList results = _foundationDAO.queryWithPaging(hqlQuery, hqlCountQuery, firstResult, maxResults);
        stopMetricsTimer(timer);
        return results;
    }

    @Override
    public HostIdentity getHostIdentityByHostName(String hostName) throws BusinessServiceException {
        if (StringUtils.isBlank(hostName)) {
            return null;
        }
        UUID id = getHostUUIDByHostName(hostName);
        if (id == null) {
            return null;
        }
        HostIdentity hostIdentity = getHostIdentityById(id);
        if (hostIdentity != null && !containsIgnoreCase(hostIdentity.getHostNames(), hostName)) {
            ids.invalidate(hostName.toLowerCase());
            return null;
        }
        return hostIdentity;
    }

    @Override
    public HostIdentity getHostIdentityById(UUID id) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostIdentityById");
        HostIdentity hostIdentity = (id == null ? null : (HostIdentity) queryById(id));
        stopMetricsTimer(timer);
        return hostIdentity;
    }

    @Override
    public HostIdentity getHostIdentityByIdOrHostName(String idOrHostName) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostIdentityByIdOrHostName");
        if (StringUtils.isBlank(idOrHostName)) return null;
        HostIdentity hostIdentity = null;
        UUID id = parseUUID(idOrHostName);
        if (id != null) {
            hostIdentity = getHostIdentityById(id);
        }
        if (hostIdentity == null) {
            hostIdentity = getHostIdentityByHostName(idOrHostName);
        }
        stopMetricsTimer(timer);
        return hostIdentity;
    }

    @Override
    public Collection<HostIdentity> getHostIdentitiesByIdOrHostNames(Collection<String> idOrHostNames) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostIdentitiesByIdOrHostNames");
        if ((idOrHostNames == null) || idOrHostNames.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        // query by host names and ids
        StringBuilder query = new StringBuilder("select distinct hi from HostIdentity hi inner join hi."+HostIdentity.HP_HOST_NAMES+" hn where");
        List queryParams = new ArrayList<Object>();
        for (String idOrHostName : idOrHostNames) {
            if ((idOrHostName == null) || (idOrHostName.length() == 0)) {
                continue;
            }
            if (!queryParams.isEmpty()) {
                query.append(" or");
            }
            query.append(" lower(hn.id) = ?");
            queryParams.add(idOrHostName.toLowerCase());
            UUID id = parseUUID(idOrHostName);
            if (id != null) {
                query.append(" or hi."+HostIdentity.HP_ID+" = ?");
                queryParams.add(id);
            }
        }
        if (queryParams.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List results = _foundationDAO.query(query.toString(), queryParams.toArray());
        stopMetricsTimer(timer);
        return (((results != null) && !results.isEmpty()) ? (Collection<HostIdentity>)results : Collections.EMPTY_LIST);
    }

    @Override
    public Collection<HostIdentity> getHostIdentitiesByIdOrHostNamesLookup(String idOrHostNamesLookup) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostIdentitiesByIdOrHostNamesLookup");
        if ((idOrHostNamesLookup == null) || (idOrHostNamesLookup.length() == 0)) {
            return Collections.EMPTY_LIST;
        }
        // query by host names like match and exact id match
        StringBuilder query = new StringBuilder("select distinct hi from HostIdentity hi inner join hi."+HostIdentity.HP_HOST_NAMES+" hn where ");
        List queryParams = new ArrayList<Object>();
        query.append(" lower(hn.id) like ?");
        queryParams.add(idOrHostNamesLookup.toLowerCase());
        UUID id = parseUUID(idOrHostNamesLookup);
        if (id != null) {
            query.append(" or hi."+HostIdentity.HP_ID+" = ?");
            queryParams.add(id);
        }
        List results = _foundationDAO.query(query.toString(), queryParams.toArray());
        stopMetricsTimer(timer);
        return (((results != null) && !results.isEmpty()) ? (Collection<HostIdentity>)results : Collections.EMPTY_LIST);
    }

    @Override
    public Host getHostByIdOrHostName(String idOrHostName) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostByIdOrHostName");
        // get Host via HostIdentity lookup
        HostIdentity hostIdentity = getHostIdentityByIdOrHostName(idOrHostName);
        if ((hostIdentity != null) && (hostIdentity.getHost() != null)) {
            stopMetricsTimer(timer);
            return hostIdentity.getHost();
        }
        // fallback to get Host by host name
        Host results = hostService.getHostByHostName(idOrHostName);
        stopMetricsTimer(timer);
        return results;
    }

    @Override
    public Collection<Host> getHostsByIdOrHostNames(Collection<String> idOrHostNames) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostsByIdOrHostNames");
        // get Hosts via HostIdentities lookup
        Collection<HostIdentity> hostIdentities = getHostIdentitiesByIdOrHostNames(idOrHostNames);
        Collection<Host> hosts = new ArrayList<Host>(hostIdentities.size());
        for (HostIdentity hostIdentity : hostIdentities) {
            if (hostIdentity.getHost() != null) {
                hosts.add(hostIdentity.getHost());
            }
        }
        if (hosts.size() == idOrHostNames.size()) {
            stopMetricsTimer(timer);
            return hosts;
        }
        // fallback to get Hosts by host names
        Set<String> hostNames = new HashSet<String>(idOrHostNames);
        for (HostIdentity hostIdentity : hostIdentities) {
            if (hostIdentity.getHost() != null) {
                removeAllIgnoreCase(hostNames, hostIdentity.getHostNames());
                removeIgnoreCase(hostNames, hostIdentity.getHostIdentityId().toString());
            }
        }
        if (!hostNames.isEmpty()) {
            hosts.addAll(hostService.getHosts(new ArrayList<String>(hostNames)));
        }
        stopMetricsTimer(timer);
        return hosts;
    }

    @Override
    public Collection<Host> getHostsByIdOrHostNamesLookup(String idOrHostNamesLookup) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostsByIdOrHostNamesLookup");
        Set<Host> hosts = new HashSet<Host>();
        // get Hosts via HostIdentities lookup
        Collection<HostIdentity> hostIdentities = getHostIdentitiesByIdOrHostNamesLookup(idOrHostNamesLookup);
        for (HostIdentity hostIdentity : hostIdentities) {
            if (hostIdentity.getHost() != null) {
                hosts.add(hostIdentity.getHost());
            }
        }
        // fallback to get Hosts by host names
        hosts.addAll(hostService.hostLookup(idOrHostNamesLookup));
        stopMetricsTimer(timer);
        return hosts;
    }

    @Override
    public ServiceStatus getServiceByDescriptionAndHostIdOrHostName(String serviceDescription, String idOrHostName) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getServiceByDescriptionAndHostIdOrHostName");
        Host host = getHostByIdOrHostName(idOrHostName);
        if (host == null) return null;
        ServiceStatus results = statusService.getServiceByDescription(serviceDescription, host.getHostName());
        stopMetricsTimer(timer);
        return results;
    }

    @Override
    public void getHostsAndHostIdentitiesByIdOrHostNamesRegex(String regex, Collection<Host> hosts, Collection<HostIdentity> hostIdentities) {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "getHostsAndHostIdentitiesByIdOrHostNamesRegex");
        if ((hosts == null) && (hostIdentities == null)) {
            return;
        }
        if (hosts != null) {
            hosts.clear();
        }
        if (hostIdentities != null) {
            hostIdentities.clear();
        }
        // iterate over all HostIdentities
        Pattern pattern = (((regex != null) && (regex.length() > 0)) ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE) : null);
        Set<String> hostHostNamesMatched = new HashSet<String>();
        for (HostIdentity hostIdentity : (List<HostIdentity>)getHostIdentities(null, null, -1, -1).getResults()) {
            // match HostIdentities id or host names
            boolean matched = false;
            if ((pattern == null) || pattern.matcher(hostIdentity.getHostIdentityId().toString()).matches()) {
                matched = true;
            } else {
                for (String hostName : hostIdentity.getHostNames()) {
                    if ((pattern == null) || pattern.matcher(hostName).matches()) {
                        matched = true;
                        break;
                    }
                }
            }
            if (matched) {
                // return matched HostIdentities if requested
                if (hostIdentities != null) {
                    hostIdentities.add(hostIdentity);
                }
                // return matched HostIdentity Hosts if requested
                if (hosts != null) {
                    Host host = hostIdentity.getHost();
                    if (host != null) {
                        hostHostNamesMatched.add(host.getHostName());
                        hosts.add(host);
                    }
                }
            }
        }
        // return matching Hosts w/o HostIdentity if requested
        if (hosts != null) {
            // iterate over all Hosts
            for (Host host : (List<Host>)hostService.getHosts(null, null, -1, -1).getResults()) {
                // match Host host name
                String hostName = host.getHostName();
                if ((pattern == null) || pattern.matcher(hostName).matches()) {
                    // ensure matching Host not already returned
                    if (!hostHostNamesMatched.contains(hostName)) {
                        hosts.add(host);
                    }
                }
            }
        }
        stopMetricsTimer(timer);
    }

    @Override
    public HostIdentity createHostIdentity(Host host) throws BusinessServiceException {
        return createHostIdentity(host, null);
    }

    @Override
    public HostIdentity createHostIdentity(Host host, Collection<String> hostNames) throws BusinessServiceException {
        return createHostIdentity(null, host, hostNames);
    }

    @Override
    public HostIdentity createHostIdentity(UUID id, Host host) throws BusinessServiceException {
        return createHostIdentity(id, host, null);
    }

    @Override
    public HostIdentity createHostIdentity(UUID id, Host host, Collection<String> hostNames) throws BusinessServiceException {
        // create and construct HostIdentity instance
        HostIdentity hostIdentity = createHostIdentity(id, host.getHostName(), hostNames);
        hostIdentity.setHost(host);
        return hostIdentity;
    }

    @Override
    public HostIdentity createHostIdentity(String hostName) throws BusinessServiceException {
        return createHostIdentity(hostName, null);
    }

    @Override
    public HostIdentity createHostIdentity(String hostName, Collection<String> hostNames) throws BusinessServiceException {
        return createHostIdentity(null, hostName, hostNames);
    }

    @Override
    public HostIdentity createHostIdentity(UUID id, String hostName) throws BusinessServiceException {
        return createHostIdentity(id, hostName, null);
    }

    @Override
    public HostIdentity createHostIdentity(UUID id, String hostName, Collection<String> hostNames) throws BusinessServiceException {
        // create and construct HostIdentity instance
        com.groundwork.collage.model.impl.HostIdentity hostIdentity = (com.groundwork.collage.model.impl.HostIdentity)create();
        hostIdentity.setHostIdentityId(id);
        hostIdentity.setHostName(hostName);
        addIgnoreCase(hostIdentity.getHostNames(), hostName);
        if (hostNames != null) {
            addAllIgnoreCase(hostIdentity.getHostNames(), hostNames);
        }
        return hostIdentity;
    }

    @Override
    public boolean renameHostIdentity(String idOrHostName, String newHostName) throws BusinessServiceException {
        // lookup HostIdentity
        HostIdentity hostIdentity = getHostIdentityByIdOrHostName(idOrHostName);
        if (hostIdentity == null) {
            return false;
        }
        // rename HostIdentity and Host host name, (do not remove current name from host names).
        if (hostIdentity.getHostName().equalsIgnoreCase(newHostName)) {
            return false;
        }
        hostIdentity.setHostName(newHostName);
        addIgnoreCase(hostIdentity.getHostNames(), newHostName);
        boolean updateHost = false;
        if ((hostIdentity.getHost() != null) && !newHostName.equals(hostIdentity.getHost().getHostName())) {
            hostIdentity.getHost().setHostName(newHostName);
            updateHost = true;
        }
        // update HostIdentity and Host
        saveHostIdentity(hostIdentity);
        if (updateHost) {
            hostService.saveHost(hostIdentity.getHost());
        }
        return true;
    }

    @Override
    public boolean addHostNameToHostIdentity(String idOrHostName, String addHostName) throws BusinessServiceException {
        // lookup HostIdentity
        HostIdentity hostIdentity = getHostIdentityByIdOrHostName(idOrHostName);
        if (hostIdentity == null) {
            return false;
        }
        // add host name to host names
        if (!addIgnoreCase(hostIdentity.getHostNames(), addHostName)) {
            return false;
        }
        // update HostIdentity
        saveHostIdentity(hostIdentity);
        return true;
    }

    @Override
    public boolean removeHostNameFromHostIdentity(String idOrHostName, String removeHostName) throws BusinessServiceException {
        // lookup HostIdentity
        HostIdentity hostIdentity = getHostIdentityByIdOrHostName(idOrHostName);
        if (hostIdentity == null) {
            return false;
        }
        // remove host name from host names unless it is the HostIdentity host name
        if (hostIdentity.getHostName().equalsIgnoreCase(removeHostName) || !removeIgnoreCase(hostIdentity.getHostNames(), removeHostName)) {
            return false;
        }
        // update HostIdentity
        saveHostIdentity(hostIdentity);
        return true;
    }

    @Override
    public boolean removeHostNameFromHostIdentity(String removeHostName) throws BusinessServiceException {
        return removeHostNameFromHostIdentity(removeHostName, removeHostName);
    }

    @Override
    public boolean removeAllHostNamesFromHostIdentity(String idOrHostName) throws BusinessServiceException {
        // lookup HostIdentity
        HostIdentity hostIdentity = getHostIdentityByIdOrHostName(idOrHostName);
        if (hostIdentity == null) {
            return false;
        }
        // remove all host name from host names unless it is the HostIdentity host name
        boolean removed = false;
        for (String removeHostName : new ArrayList<String>(hostIdentity.getHostNames())) {
            if (!hostIdentity.getHostName().equalsIgnoreCase(removeHostName)) {
                removed = removeIgnoreCase(hostIdentity.getHostNames(), removeHostName) || removed;
            }
        }
        if (!removed) {
            return false;
        }
        // update HostIdentity
        saveHostIdentity(hostIdentity);
        return true;
    }

    @Override
    public void saveHostIdentity(HostIdentity hostIdentity) throws BusinessServiceException {
        // TODO: enable PostgreSQL UUID generate extension once available, (see GWCollageDB.sql). Setting id should not be done here.
        if (hostIdentity.getHostIdentityId() == null) {
            ((com.groundwork.collage.model.impl.HostIdentity)hostIdentity).setHostIdentityId(UUID.randomUUID());
            log.warn("HostIdentity UUID generated in application tier: "+hostIdentity.getHostIdentityId());
        }
        // delete host identity zombie hosts/services
        deleteHostIdentityZombiedHosts(hostIdentity);
        deleteHostIdentityZombiedServices(hostIdentity, false);
        // save host identity
        save(hostIdentity);
        // add to ids cache
        String identityHostName = hostIdentity.getHostName().toLowerCase();
        invalidateIdCaches(Collections.singletonList(identityHostName));
        Collection<String> idKeyHostNames = getIdKeyHostNames(identityHostName, true);
        for (String hostName : hostIdentity.getHostNames()) {
            hostName = hostName.toLowerCase();
            ids.put(hostName, hostIdentity.getHostIdentityId());
            idKeyHostNames.add(hostName);
        }
        // refresh ids and autocomplete in event name changed
        refreshAllCachesOnTransactionCommit(null);
    }

    @Override
    public void saveHostIdentities(List<HostIdentity> hostIdentities) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "saveHostIdentities");
        // TODO: enable PostgreSQL UUID generate extension once available, (see GWCollageDB.sql). Setting id should not be done here.
        for (HostIdentity hostIdentity : hostIdentities) {
            if (hostIdentity.getHostIdentityId() == null) {
                ((com.groundwork.collage.model.impl.HostIdentity)hostIdentity).setHostIdentityId(UUID.randomUUID());
                log.warn("HostIdentity UUID generated in application tier: "+hostIdentity.getHostIdentityId());
            }
        }
        // delete host identity zombie hosts/services
        for (HostIdentity hostIdentity : hostIdentities) {
            deleteHostIdentityZombiedHosts(hostIdentity);
            deleteHostIdentityZombiedServices(hostIdentity, false);
        }
        // save host identity
        save(hostIdentities);
        // add to id cache
        for (HostIdentity hostIdentity : hostIdentities) {
            String identityHostName = hostIdentity.getHostName().toLowerCase();
            invalidateIdCaches(Collections.singletonList(identityHostName));
            Collection<String> idKeyHostNames = getIdKeyHostNames(identityHostName, true);
            for (String hostName : hostIdentity.getHostNames()) {
                hostName = hostName.toLowerCase();
                ids.put(hostName, hostIdentity.getHostIdentityId());
                idKeyHostNames.add(hostName);
            }
        }
        // refresh ids and autocomplete in event names changed
        refreshAllCachesOnTransactionCommit(null);
        stopMetricsTimer(timer);
    }

    /**
     * Delete hosts potentially zombied by new host names.
     *
     * @param hostIdentity created/updated host identity
     */
    private void deleteHostIdentityZombiedHosts(HostIdentity hostIdentity) {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "saveHostIdentities");
        for (String hostName : hostIdentity.getHostNames()) {
            if (!hostIdentity.getHostName().equalsIgnoreCase(hostName)) {
                // lookup zombie host
                Host zombieHost = hostService.getHostByHostName(hostName);
                if (zombieHost != null) {
                    if (hostIdentity.getHost() != null) {
                        // add host identity host to zombie host host groups
                        for (Iterator<HostGroup> groupIter = ((Set<HostGroup>)zombieHost.getHostGroups()).iterator(); groupIter.hasNext();) {
                            HostGroup hostGroup = groupIter.next();
                            if (!hostIdentity.getHost().getHostGroups().contains(hostGroup)) {
                                hostIdentity.getHost().getHostGroups().add(hostGroup);
                            }
                        }
                        // merge zombie host services with host identity host services,
                        // saving host name as service application host name; delete
                        // duplicate zombie host services
                        for (Iterator<ServiceStatus> serviceIter = ((Set<ServiceStatus>)zombieHost.getServiceStatuses()).iterator(); serviceIter.hasNext();) {
                            ServiceStatus service = serviceIter.next();
                            if (hostIdentity.getHost().getServiceStatus(service.getServiceDescription()) == null) {
                                // merge unique zombie host services
                                hostIdentity.getHost().getServiceStatuses().add(service);
                                service.setHost(hostIdentity.getHost());
                                if (service.getApplicationHostName() == null) {
                                    service.setApplicationHostName(hostName);
                                }
                                statusService.saveService(service);
                            } else {
                                // delete duplicate zombie host services
                                statusService.deleteService(service);
                            }
                        }
                        // save merged host identity host
                        hostService.saveHost(hostIdentity.getHost());
                    }
                    // delete zombie host
                    hostService.deleteHost(zombieHost);
                    if (log.isInfoEnabled()) {
                        log.info("Host "+hostName+" zombied by host identity "+hostIdentity.getHostName()+" deleted");
                    }
                }
            }
        }
        stopMetricsTimer(timer);
    }

    /**
     * Delete services potentially zombied by deleted host names.
     *
     * @param hostIdentity updated host identity or host identity to delete
     * @param deleteAll delete all services potentially zombied
     */
    private void deleteHostIdentityZombiedServices(HostIdentity hostIdentity, boolean deleteAll) {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "deleteHostIdentityZombiedServices");
        if (hostIdentity.getHost() == null) {
            return;
        }
        // lookup zombie services by deleted application host names
        Set<ServiceStatus> zombieApplicationServices = new HashSet<ServiceStatus>();
        for (ServiceStatus service : (Set<ServiceStatus>)hostIdentity.getHost().getServiceStatuses()) {
            String applicationHostName = service.getApplicationHostName();
            if (applicationHostName != null) {
                if (deleteAll || !containsIgnoreCase(hostIdentity.getHostNames(), applicationHostName)) {
                    zombieApplicationServices.add(service);
                }
            }
        }
        if (zombieApplicationServices.isEmpty()) {
            return;
        }
        // remove zombie application services and capture application
        // service application types
        Set<String> deletedServicesApplicationTypes = new HashSet<String>();
        for (ServiceStatus zombieService : zombieApplicationServices) {
            if (zombieService.getApplicationType() != null) {
                deletedServicesApplicationTypes.add(zombieService.getApplicationType().getName());
            }
            hostIdentity.getHost().getServiceStatuses().remove(zombieService);
            statusService.deleteService(zombieService);
            if (log.isInfoEnabled()) {
                log.info("Service "+zombieService.getApplicationHostName()+":"+zombieService.getServiceDescription()+" zombied by host identity "+hostIdentity.getHostName()+" deleted");
            }
        }
        hostService.saveHost(hostIdentity.getHost());
        stopMetricsTimer(timer);
    }

    @Override
    public void deleteHostIdentityById(UUID id) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "deleteHostIdentityZombiedServices");
        HostIdentity hostIdentity = getHostIdentityById(id);
        if (hostIdentity != null) {
            deleteHostIdentity(hostIdentity);
        }
        stopMetricsTimer(timer);
    }

    @Override
    public boolean deleteHostIdentityByIdOrHostName(String idOrHostName) throws BusinessServiceException {
        HostIdentity hostIdentity = getHostIdentityByIdOrHostName(idOrHostName);
        if (hostIdentity != null) {
            deleteHostIdentity(hostIdentity);
            return true;
        }
        return false;
    }

    @Override
    public void deleteHostIdentity(HostIdentity hostIdentity) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "deleteHostIdentityZombiedServices");
        // delete host identity zombie services
        deleteHostIdentityZombiedServices(hostIdentity, true);
        // delete host identity
        delete(hostIdentity);
        // refresh ids and autocomplete since name deleted
        refreshAllCachesOnTransactionCommit(Collections.singletonList(hostIdentity));
        stopMetricsTimer(timer);
    }

    @Override
    public void deleteHostIdentities(List<HostIdentity> hostIdentities) throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer("HostIdentityServiceImpl", "deleteHostIdentityZombiedServices");
        // delete host identity zombie services
        for (HostIdentity hostIdentity : hostIdentities) {
            deleteHostIdentityZombiedServices(hostIdentity, true);
        }
        // delete host identities
        delete(hostIdentities);
        // refresh ids and autocomplete since names deleted
        refreshAllCachesOnTransactionCommit(hostIdentities);
        stopMetricsTimer(timer);
    }

    @Override
    public Iterator<AutocompleteName> openNamesIterator(String namesEntityType) {
        return new AutocompleteNamesIterator(getSessionFactory(),
                "select hn.hostname, hi.hostname from hostname hn join hostidentity hi on hn.hostidentityid = hi.hostidentityid " +
                        "union " +
                        "select hostname, hostname from host");
    }

    @Override
    public void closeNamesIterator(Iterator<AutocompleteName> iterator) {
        ((AutocompleteNamesIterator)iterator).close();
    }

    @Override
    public void invalidateHosts(Collection<String> hostNames) {
        // evict from L2 cache in case stale since cascade from host not
        // managed by unidirectional many-to-one hibernate mapping
        for (String hostName : hostNames) {
            hostName = hostName.toLowerCase();
            UUID id = ids.getIfPresent(hostName);
            if (id != null) {
                getSession().getSessionFactory().evict(com.groundwork.collage.model.impl.HostIdentity.class, id);
            }
        }
        // invalidate id caches
        invalidateIdCaches(hostNames);
    }

    private void invalidateIdCaches(Collection<String> hostNames) {
        for (String hostName : hostNames) {
            hostName = hostName.toLowerCase();
            Collection<String> idKeyHostNames = getIdKeyHostNames(hostName, false);
            if (idKeyHostNames != null) {
                ids.invalidateAll(idKeyHostNames);
                idKeys.remove(hostName);
            }
        }
    }

    /**
     * Set autocomplete service, (normally Spring injected).
     *
     * @param hostIdentityAutocompleteService autocomplete service
     */
    public void setHostIdentityAutocompleteService(Autocomplete hostIdentityAutocompleteService) {
        this.hostIdentityAutocompleteService = hostIdentityAutocompleteService;
    }

    /**
     * Set cache service, (normally Spring injected).
     *
     * @param cacheService cache service
     */
    public void setCacheService(BusinessCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Transaction synchronization to refresh ids and autocomplete on commit.
     */
    private class HostIdentityTransactionSynchronization extends TransactionSynchronizationAdapter {
        private Collection<String> deleteHostNames;

        private HostIdentityTransactionSynchronization(Collection<String> deleteHostNames) {
            this.deleteHostNames = deleteHostNames;
        }

        @Override
        public void afterCommit() {
            if (hostIdentityAutocompleteService != null) {
                hostIdentityAutocompleteService.refresh();
            }
            if (deleteHostNames != null && cacheService != null) {
                cacheService.invalidate(deleteHostNames, false);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HostIdentityTransactionSynchronization)) {
                return false;
            }
            HostIdentityTransactionSynchronization other = (HostIdentityTransactionSynchronization)obj;
            return (deleteHostNames == null && other.deleteHostNames == null) ||
                    (deleteHostNames != null && other.deleteHostNames != null &&
                            deleteHostNames.size() == other.deleteHostNames.size() &&
                            deleteHostNames.containsAll(other.deleteHostNames));
        }
    };

    /**
     * Register transaction synchronization to invoke ids and autocomplete
     * refresh on commit of current transaction.
     *
     * @param deleteHostIdentities
     */
    private void refreshAllCachesOnTransactionCommit(Collection<HostIdentity> deleteHostIdentities) {
        Collection<String> deleteHostNames = null;
        if (deleteHostIdentities != null) {
            deleteHostNames = new HashSet<>();
            for (HostIdentity deleteHostIdentity : deleteHostIdentities) {
                deleteHostNames.add(deleteHostIdentity.getHostName());
            }
        }
        try {
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            TransactionSynchronization synchronization = new HostIdentityTransactionSynchronization(deleteHostNames);
            if (!synchronizations.contains(synchronization)) {
                TransactionSynchronizationManager.registerSynchronization(synchronization);
            }
        } catch (IllegalStateException ise) {
        }
    }

    /**
     * Parse UUID from string. Accepts these forms:
     *
     * a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
     * A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11
     * {a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11}
     * a0eebc999c0b4ef8bb6d6bb9bd380a11
     * a0ee-bc99-9c0b-4ef8-bb6d-6bb9-bd38-0a11
     * {a0eebc99-9c0b4ef8-bb6d6bb9-bd380a11}
     *
     * @param string UUID string format
     * @return parsed UUID
     */
    private static UUID parseUUID(String string) {
        if ((string.length() < 32) || (string.length() > 39)) {
            return null;
        }
        if (string.startsWith("{") && string.endsWith("}")) {
            string = string.substring(1, string.length()-1);
        }
        if (string.length() == 32) {
            string = string.substring(0, 8)+'-'+string.substring(8, 12)+'-'+string.substring(12, 16)+'-'+string.substring(16, 20)+'-'+string.substring(20, 32);
        } else if ((string.length() == 39) && (string.charAt(4) == '-')) {
            string = string.substring(0, 4)+string.substring(5, 29)+string.substring(20, 34)+string.substring(35, 39);
        } else if ((string.length() == 35) && (string.charAt(17) == '-')) {
            string = string.substring(0, 13)+'-'+string.substring(13, 22)+'-'+string.substring(22, 26)+string.substring(27, 35);
        }
        if ((string.length() != 36) || (string.charAt(8) != '-')) {
            return null;
        }
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    /**
     * Case insensitive contains utility.
     *
     * @param set set to test against
     * @param string sring to test
     * @return contains flag
     */
    private static boolean containsIgnoreCase(Set<String> set, String string) {
        for (String setString : set) {
            if (setString.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Case insensitive set add utility.
     *
     * @param set set to add to
     * @param string string to add
     * @return added flag
     */
    private static boolean addIgnoreCase(Set<String> set, String string) {
        return (!containsIgnoreCase(set, string) ? set.add(string) : false);
    }

    /**
     * Case insensitive set remove utility.
     *
     * @param set set to add to
     * @param string string to add
     * @return added flag
     */
    private static boolean removeIgnoreCase(Set<String> set, String string) {
        for (Iterator<String> stringIter = set.iterator(); stringIter.hasNext();) {
            if (stringIter.next().equalsIgnoreCase(string)) {
                stringIter.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Case insensitive set add all utility.
     *
     * @param set set to add to
     * @param strings strings to add
     */
    private static void addAllIgnoreCase(Set<String> set, Collection<String> strings) {
        for (String string : strings) {
            addIgnoreCase(set, string);
        }
    }

    /**
     * Case insensitive set remove all utility.
     *
     * @param set set to remove from
     * @param strings strings to remove
     */
    private static void removeAllIgnoreCase(Set<String> set, Collection<String> strings) {
        for (String string : strings) {
            removeIgnoreCase(set, string);
        }

    }
}
