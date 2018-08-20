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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import com.groundwork.collage.util.AutocompleteNames;
import com.groundwork.collage.util.AutocompleteNamesIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.cache.BusinessCacheService;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * LogMessage Service Implementation Class
 *
 */
public class StatusServiceImpl extends EntityBusinessServiceImpl implements StatusService, AutocompleteNames
{
	private MetadataService _metadataService = null;

	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(StatusServiceImpl.class);
	
	/** Default Sort Criteria */
	private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(PROP_SERVICEDESCRIPTION);

    private Autocomplete statusAutocompleteService = null;

	private BusinessCacheService cacheService = null;

	protected StatusServiceImpl(FoundationDAO foundationDAO, MetadataService ms) 
	{
		super(foundationDAO, ServiceStatus.INTERFACE_NAME, ServiceStatus.COMPONENT_NAME);

		_metadataService = ms;
	}

	private ConcurrentHashMap<String, Set<String>> idKeys = new ConcurrentHashMap<>();
	private final LoadingCache<String, Integer> ids = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.removalListener(new RemovalListener<String, Integer>() {
				@Override
				public void onRemoval(RemovalNotification<String, Integer> removalNotification) {
					if (removalNotification.getCause() == RemovalCause.EXPIRED) {
						idKeys.remove(removalNotification.getKey());
					}
				}
			})
			.build(new CacheLoader<String, Integer>() {
				public Integer load(@SuppressWarnings("NullableProblems") String hostAndDescription) {
					String[] splitHostAndDescription = StringUtils.split(hostAndDescription, ":");
					String hostName = splitHostAndDescription[0];
					String serviceDescription = splitHostAndDescription[1];
					List results = _foundationDAO.sqlQuery("select s.servicestatusid from servicestatus s join host h on s.hostid = h.hostid where s.servicedescription = '"
							+ serviceDescription.replaceAll("'", "''") + "' and lower(h.hostname) = '" + hostName.replaceAll("'", "''") + "'");
					if (results == null || results.size() == 0) {
						return null;
					}
					getIdKeyServiceNames(hostName, true).add(hostAndDescription);
					return (Integer) results.get(0);
				}
			});

	private Collection<String> getIdKeyServiceNames(String hostName, boolean create) {
		Collection<String> serviceNames = idKeys.get(hostName);
		while (create && serviceNames == null) {
			idKeys.putIfAbsent(hostName, Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));
			serviceNames = idKeys.get(hostName);
		}
		return serviceNames;
	}

	private Integer getServiceId(String hostname, String serviceDescription) throws BusinessServiceException {
		try {
			return ids.get(buildServiceName(hostname, serviceDescription));
		} catch (ExecutionException | CacheLoader.InvalidCacheLoadException e) {
			return null;
		}
	}

	private String buildServiceName(String hostname, String serviceDescription) {
		return hostname.toLowerCase() + ":" + serviceDescription;
	}


	public FoundationQueryList getServices(FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;
		
		FoundationQueryList results = this.query(filter, sortCriteria, firstResult, maxResults);
		// cache service host device mapping
		cacheService.saveServiceHostDevice(results.getResults(), false);
		stopMetricsTimer(timer);
		return results;
	}

	public FoundationQueryList getServicesByHostGroupId(int hgId, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hgId < 1)
			throw new IllegalArgumentException("A valid HostGroup Id must be provided.");
		
		FilterCriteria idFilter = FilterCriteria.eq(PROP_HOSTGROUPID, hgId);
		if (filter == null)
			filter = idFilter;
		else
			filter.and(idFilter);

		FoundationQueryList results = this.getServices(filter, sortCriteria, firstResult, maxResults);

		// cache service host device mapping
		cacheService.saveServiceHostDevice(results.getResults(), false);

		stopMetricsTimer(timer);
		return results;
	}
	
	/**
     * Get all services belonging to a particular category.
     *
     * @param categoryId category id
	 * @return service status list
	 * @throws BusinessServiceException
	 */
	public List<ServiceStatus> getServicesByCategoryId(int categoryId) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
        // validate query
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category id invalid.");
        }
        // query for service status entities in category
        List<ServiceStatus> results = _foundationDAO.query("select ss from ServiceStatus ss, CategoryEntity ce where " +
                "ce." + Category.HP_ENTITIES_CATEGORY_ID.substring(Category.HP_ENTITIES.length()+1) + " = ? and " +
                "ce." + Category.HP_ENTITIES_ID.substring(Category.HP_ENTITIES.length()+1) + " = ss." + ServiceStatus.HP_ID + " and " +
                "ce." + Category.HP_ENTITIES_TYPE_NAME.substring(Category.HP_ENTITIES.length()+1) + " = '" + CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS + "' " +
                "order by ss." + ServiceStatus.HP_SERVICE_DESCRIPTION + " asc", categoryId);
		// cache service host device mapping
		cacheService.saveServiceHostDevice(results, false);
		stopMetricsTimer(timer);
        return results;
	}

	public FoundationQueryList getServicesByHostGroupName(String hgName, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hgName == null || hgName.length()==0)
			throw new IllegalArgumentException("A valid hostgroup name must be provided.");
		
		FilterCriteria nameFilter = FilterCriteria.eq(PROP_HOSTGROUPNAME, hgName);
		if (filter == null)
			filter = nameFilter;
		else
			filter.and(nameFilter);
		
		FoundationQueryList results = this.getServices(filter, sortCriteria, firstResult, maxResults);

		// cache service host device mapping
		cacheService.saveServiceHostDevice(results.getResults(), false);

		stopMetricsTimer(timer);
		return results;
	}

	public FoundationQueryList getServicesByHostId(int hostId, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hostId < 1)
			throw new IllegalArgumentException("A valid Host Id must be provided.");
		
		FilterCriteria idFilter = FilterCriteria.eq(PROP_HOSTID, hostId);
		if (filter == null)
			filter = idFilter;
		else
			filter.and(idFilter);
		
		FoundationQueryList results = this.getServices(filter, sortCriteria, firstResult, maxResults);

		// cache service host device mapping
		cacheService.saveServiceHostDevice(results.getResults(), false);

		stopMetricsTimer(timer);
		return results;
	}

	public FoundationQueryList getServicesByHostName(String hostName, FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hostName == null || hostName.length()==0)
			throw new IllegalArgumentException("A valid hostname must be provided.");
		
		FilterCriteria nameFilter = FilterCriteria.eq(PROP_HOSTNAME, hostName);
		if (filter == null)
			filter = nameFilter;
		else
			filter.and(nameFilter);
		
		FoundationQueryList results = this.getServices(filter, sortCriteria, firstResult, maxResults);

		// cache service host device mapping
		cacheService.saveServiceHostDevice(results.getResults(), false);

		stopMetricsTimer(timer);
		return results;
	}

	public ServiceStatus getServiceByDescription(String serviceDescription, String hostName) throws BusinessServiceException {
		if (hostName == null || hostName.length()==0)
			throw new IllegalArgumentException("A valid hostname must be provided");
		
		if (serviceDescription == null || serviceDescription.length()==0)
			throw new IllegalArgumentException("A valid service description must be provided");

		Integer id = getServiceId(hostName, serviceDescription);
		if (id == null) {
			return null;
		}
		ServiceStatus service = getServiceById(id);
		if (service != null) {
			if (!service.getHost().getHostName().equalsIgnoreCase(hostName)) {
				cacheService.invalidate(Collections.singletonList(hostName), false);
				return null;
			}
			if (!service.getServiceDescription().equals(serviceDescription)) {
				cacheService.invalidate(Collections.singletonList(hostName), true);
				return null;
			}
		}
		return service;
	}

	public ServiceStatus getServiceById(int ssId) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (ssId < 1)
			throw new IllegalArgumentException("A valid ServiceStatus Id must be provided.");
		ServiceStatus results = null;
		try {
			results = (ServiceStatus) this.queryById(ssId);
			// cache service host device mapping
			cacheService.saveServiceHostDevice(results, false);
		}
		catch (Exception e) {
			log.warn("Failed to retreive Service by ID: " + ssId);
		}
		stopMetricsTimer(timer);
		return results;
	}

	public void deleteService(int serviceId) throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		ServiceStatus serviceStatus = getServiceById(serviceId);
		if (serviceStatus == null) {
			if (log.isWarnEnabled()) log.warn("Unable to delete service status - Not Found - id=" + serviceId);
		} else {
			this.delete(serviceId);
			// refresh ids and autocomplete since name deleted
			refreshAllCachesOnTransactionCommit(Collections.singletonList(serviceStatus));
		}
        stopMetricsTimer(timer);
	}

	public void deleteService(String hostName, String serviceDescription) throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		if (hostName == null || hostName.length()==0)
			throw new IllegalArgumentException("A valid hostname must be provided");
		
		if (serviceDescription == null || serviceDescription.length()==0)
			throw new IllegalArgumentException("A valid service descripiotn must be provided");
		
		// Look up service
		ServiceStatus serviceStatus = getServiceByDescription(serviceDescription, hostName);
		if (serviceStatus == null) {
			if (log.isWarnEnabled())
				log.warn("Unable to delete service status - Not Found - " 
					+ serviceDescription + ", Host: " + hostName);
		}
		else {
			this.delete(serviceStatus);
            // refresh ids and autocomplete since name deleted
            refreshAllCachesOnTransactionCommit(Collections.singletonList(serviceStatus));
		}
		stopMetricsTimer(timer);
	}
	
	public void deleteService(ServiceStatus service) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (service == null)
			throw new IllegalArgumentException("A valid ServiceStatus must be provided.");
		
		this.delete(service);
        // refresh ids and autocomplete since name deleted
        refreshAllCachesOnTransactionCommit(Collections.singletonList(service));
		stopMetricsTimer(timer);
	}

	public void deleteService(Collection<ServiceStatus> services) throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		if (services == null || services.size()==0)
			throw new IllegalArgumentException("A list of ServiceStatuses must be provided.");

		this.delete(services);
        // refresh ids and autocomplete since name deleted
        refreshAllCachesOnTransactionCommit(services);
        stopMetricsTimer(timer);
	}

	public ServiceStatus createService(String serviceDescr, String appTypeName, Host host) throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		if (serviceDescr == null || serviceDescr.length()==0)
			throw new IllegalArgumentException("A valid service description must be provided.");
		
		if (appTypeName == null || appTypeName.length()==0)
			throw new IllegalArgumentException("A valid Application Type name must be provided.");
		
		if (host == null)
			throw new IllegalArgumentException("A host must be provided.");
		
		// Get Application Type
		ApplicationType appType = _metadataService.getApplicationTypeByName(appTypeName);
		if (appType == null)
			throw new IllegalArgumentException("Unknown application type - " + appTypeName);
		
		ServiceStatus serviceStatus = createService();
		
		serviceStatus.setServiceDescription(serviceDescr);
		serviceStatus.setApplicationType(appType);
		serviceStatus.setHost(host);

		stopMetricsTimer(timer);
		return serviceStatus;
	}

	public ServiceStatus createService() throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		ServiceStatus results = (ServiceStatus)this.create();
		stopMetricsTimer(timer);
		return results;
	}

	public void saveService(ServiceStatus service) throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		if (service == null)
			throw new IllegalArgumentException("A service must be provided.");
		
		this.save(service);
		// add to id cache
		String serviceName = buildServiceName(service.getHost().getHostName(), service.getServiceDescription());
		ids.put(serviceName, service.getServiceStatusId());
		getIdKeyServiceNames(service.getHost().getHostName().toLowerCase(), true).add(serviceName);
		FoundationQueryList fql = getServicesByHostId(service.getHost().getHostId(), null, null, -1, -1);
		// cache service host device mapping
		cacheService.saveServiceHostDevice(service, true);
        // refresh ids and autocomplete in event names changed
        refreshAllCachesOnTransactionCommit(null);
        stopMetricsTimer(timer);
	}

	public void saveService(Collection<ServiceStatus> services) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (services == null || services.size()==0)
			throw new IllegalArgumentException("The list of ServiceStatuses may not be null or empty");

		this.save(services);
		// add to id cache
		for (ServiceStatus service : services) {
			String serviceName = buildServiceName(service.getHost().getHostName(), service.getServiceDescription());
			ids.put(serviceName, service.getServiceStatusId());
			getIdKeyServiceNames(service.getHost().getHostName().toLowerCase(), true).add(serviceName);
		}
		// cache service host device mapping
		cacheService.saveServiceHostDevice(services, true);
        // refresh ids and autocomplete in event names changed
        refreshAllCachesOnTransactionCommit(null);
		stopMetricsTimer(timer);
	}

    public FoundationQueryList queryServiceStatus(String hql, String hqlCount, int firstResult, int maxResults) {
		CollageTimer timer = startMetricsTimer();
        FoundationQueryList list = _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
		// cache service host device mapping
		cacheService.saveServiceHostDevice(list.getResults(), false);
		stopMetricsTimer(timer);
        return list;
    }

    @Override
    public Iterator<AutocompleteName> openNamesIterator(String namesEntityType) {
        return new AutocompleteNamesIterator(getSessionFactory(), "select distinct servicedescription from servicestatus");
    }

    @Override
    public void closeNamesIterator(Iterator<AutocompleteName> iterator) {
        ((AutocompleteNamesIterator)iterator).close();
    }

	@Override
	public void invalidateHosts(Collection<String> hostNames) {
		for (String hostName : hostNames) {
			hostName = hostName.toLowerCase();
			Collection<String> idKeyServiceNames = getIdKeyServiceNames(hostName, false);
			if (idKeyServiceNames != null) {
				ids.invalidateAll(idKeyServiceNames);
				idKeys.remove(hostName);
			}
		}
	}

    /**
     * Set autocomplete service, (normally Spring injected).
     *
     * @param statusAutocompleteService autocomplete service
     */
    public void setStatusAutocompleteService(Autocomplete statusAutocompleteService) {
        this.statusAutocompleteService = statusAutocompleteService;
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
	private class ServiceTransactionSynchronization extends TransactionSynchronizationAdapter {
		private Collection<String> updateHostNames;

		private ServiceTransactionSynchronization(Collection<String> updateHostNames) {
			this.updateHostNames = updateHostNames;
		}

        @Override
        public void afterCommit() {
            if (statusAutocompleteService != null) {
                statusAutocompleteService.refresh();
            }
			if (updateHostNames != null && cacheService != null) {
				cacheService.invalidate(updateHostNames, true);
			}
        }

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ServiceTransactionSynchronization)) {
				return false;
			}
			ServiceTransactionSynchronization other = (ServiceTransactionSynchronization)obj;
			return (updateHostNames == null && other.updateHostNames == null) ||
					(updateHostNames != null && other.updateHostNames != null &&
							updateHostNames.size() == other.updateHostNames.size() &&
							updateHostNames.containsAll(other.updateHostNames));
		}
    };

    /**
     * Register transaction synchronization to invoke ids and autocomplete
     * refresh on commit of current transaction.
	 *
	 * @param deleteServices
     */
    private void refreshAllCachesOnTransactionCommit(Collection<ServiceStatus> deleteServices) {
		Collection<String> updateHostNames = null;
		if (deleteServices != null) {
			updateHostNames = new HashSet<>();
			for (ServiceStatus deleteService : deleteServices) {
				updateHostNames.add(deleteService.getHost().getHostName());
			}
		}
        try {
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronization synchronization = new ServiceTransactionSynchronization(updateHostNames);
            if (!synchronizations.contains(synchronization)) {
                TransactionSynchronizationManager.registerSynchronization(synchronization);
            }
        } catch (IllegalStateException ise) {
        }
    }
}
	
