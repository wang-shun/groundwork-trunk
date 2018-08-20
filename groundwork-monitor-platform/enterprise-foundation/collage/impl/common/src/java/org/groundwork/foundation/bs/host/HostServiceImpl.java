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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import com.groundwork.collage.util.AutocompleteNames;
import com.groundwork.collage.util.AutocompleteNamesIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.cache.BusinessCacheService;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author rogerrut
 *
 * Created: Jan 11, 2007
 */
public class HostServiceImpl extends EntityBusinessServiceImpl implements HostService, AutocompleteNames
{
	/** Default Sort Criteria */
	private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(Host.HP_NAME);
	
	/** Default Application Type */
	private static final String DEFAULT_APPLICATION_TYPE = "NAGIOS";
	
	/** Default Monitor Status */
	private static final String DEFAULT_MONITOR_STATUS = "PENDING";
	
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(HostServiceImpl.class);
	
	/** Business Services used within HostService */
	private MetadataService metadataService = null;
	private LogMessageService logMessageService = null;
	private DeviceService deviceService = null;
    private Autocomplete hostAutocompleteService = null;
    private Autocomplete hostIdentityAutocompleteService = null;
    private Autocomplete statusAutocompleteService = null;
    private BusinessCacheService cacheService = null;

	/**
	 * Base class constructor takes Interface (Spring ID) and ComponentName (Hibernate POJO)
	 */
	public HostServiceImpl(FoundationDAO foundationDAO,
			MetadataService mds, 
			LogMessageService lms, 
			DeviceService ds)
	{
		super(foundationDAO, Host.INTERFACE_NAME, Host.COMPONENT_NAME);	
		
		metadataService		= mds;
		logMessageService	= lms;
		deviceService 		= ds;
	}

	private final LoadingCache<String, Integer> ids = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(new CacheLoader<String, Integer>() {
				public Integer load(@SuppressWarnings("NullableProblems") String hostName) {
					List results = _foundationDAO.sqlQuery("select " + Host.HP_ID + " from " + Host.ENTITY_TYPE_CODE
							+ " where lower(" + Host.HP_NAME + ") = '" + hostName.replaceAll("'", "''") + "'");
					return (results == null || results.size() == 0) ? null : (Integer) results.get(0);
				}
			});

	private Integer getHostIdByHostName(String hostname) throws BusinessServiceException {
		try {
			return ids.get(hostname.toLowerCase());
		} catch (ExecutionException | CacheLoader.InvalidCacheLoadException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#createHost(java.lang.String, com.groundwork.collage.model.Device)
	 */
	public Host createHost(String name, Device device)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		Host host = (Host)this.create();
		host.setHostName(name);
		host.setDevice(device);
		stopMetricsTimer(timer);
		return host;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#createHost()
	 */
	public Host createHost() throws BusinessServiceException {
		/*delegate*/
		return (Host)this.create();

	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#createHostStatus(java.lang.String, com.groundwork.collage.model.Host)
	 */
	public HostStatus createHostStatus(String applicationType, Host host)
	throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		/* Check Application Type*/
		ApplicationType appType = null;
		
		if (this.metadataService == null)
			throw new BusinessServiceException("Failed to create instance of Business Service for Metadata needed to validate Application Types.");
		
		if (applicationType == null || applicationType.length() == 0)
			appType = this.metadataService.getApplicationTypeByName(DEFAULT_APPLICATION_TYPE);
		else
			appType = this.metadataService.getApplicationTypeByName(applicationType);

		if (appType == null)
			throw new IllegalArgumentException("attempting to create HostStatus with undefined ApplicationType, " + applicationType);

		if (host == null)
			throw new IllegalArgumentException("attempting to create HostStatus with null host!");

		HostStatus hostStatus = (HostStatus)this.create(HostStatus.INTERFACE_NAME);
		hostStatus.setApplicationType(appType);
		
		// Default status to pending
		hostStatus.setHostMonitorStatus(this.metadataService.getMonitorStatusByName(DEFAULT_MONITOR_STATUS));
		host.setHostStatus(hostStatus);		
		hostStatus.setHost(host);

		if (log.isInfoEnabled()) log.info("created new HostStatus for Host, " + host.getHostName());

		stopMetricsTimer(timer);
		return hostStatus;
	}

	public void saveHostStatus (HostStatus hostStatus) throws BusinessServiceException
	{
		CollageTimer timer = startMetricsTimer();
		if (hostStatus == null)
			throw new IllegalArgumentException("Invalid null HostStatus parameter.");
		
		save(hostStatus);
		stopMetricsTimer(timer);
	}
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#deleteHost(com.groundwork.collage.model.Host)
	 */
	public void deleteHost(Host host) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
        deleteHostAndOrphanedDevice(host);
        // refresh ids and autocomplete since name deleted
        refreshAllCachesOnTransactionCommit(Collections.singletonList(host));
		stopMetricsTimer(timer);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#deleteHost(java.util.Collection)
	 */
	public void deleteHost(Collection<Host> hostList)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
        for (Host host : hostList) {
            deleteHostAndOrphanedDevice(host);
        }
        // refresh ids and autocomplete since names deleted
        refreshAllCachesOnTransactionCommit(hostList);
		stopMetricsTimer(timer);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#deleteHostById(int)
	 */
	public void deleteHostById(int hostId) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
        Host host = getHostByHostId(hostId);
        if (host == null) {
            if (log.isWarnEnabled()) log.warn("Unable to delete Host - Host Not Found - " + hostId);
            return;
        }
        deleteHostAndOrphanedDevice(host);
        // refresh ids and autocomplete since name deleted
        refreshAllCachesOnTransactionCommit(Collections.singletonList(host));
		stopMetricsTimer(timer);
    }

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#deleteHostByName(java.lang.String)
	 */
	public void deleteHostByName(String hostName) throws BusinessServiceException {
		if (StringUtils.isBlank(hostName)) throw new IllegalArgumentException("Invalid null / empty Host Name parameter.");
		Integer id = getHostIdByHostName(hostName);
		if (id != null) deleteHostById(id);
	}

    private boolean deleteHostAndOrphanedDevice(Host host) {
		CollageTimer timer = startMetricsTimer();
        Integer id = host.getDevice().getDeviceId();
        Set hosts = host.getDevice().getHosts();
        hosts.remove(host);
        delete(host);
        if (hosts.size() == 0) {
            log.debug("Deleting orphaned device " + id);
            deviceService.deleteDeviceById(id);
			stopMetricsTimer(timer);
            return true;
        }
		stopMetricsTimer(timer);
        return false;
    }

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#deleteHostStatus(java.lang.String)
	 */
	public void deleteHostStatus(String hostName)throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hostName == null || hostName.length() == 0)
		{
			throw new IllegalArgumentException("Invalid null / empty host name parameter.");
		}
		
		try {
			// When deleting a HostStatus, we want to preserve the log messages that
			// were issued for that Host - the log messages are only deleted when
			// the Device on which the Host resides is removed from the system
			//
			// To such end, we set the HostStatusID to Null in the LogMessages
			// table so that the deletion of the HostStatus does not yield a
			// referential integrity violation
			logMessageService.unlinkLogMessagesFromHost(hostName);

			Host host = this.getHostByHostName(hostName);
			if (host != null)
			{
				host.setHostStatus(null);
				this.save(host);
				
				if (log.isInfoEnabled()) 
					log.info("Deleted HostStatus for Host, " + hostName);				
			}
		} 
		catch (Exception e) 
		{				
			throw new BusinessServiceException("Unable to delete HostStatus for Host, " + hostName, e);
		}
		stopMetricsTimer(timer);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostByHostId(int)
	 */
	public Host getHostByHostId(int hostId) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		Object result = null;
		try {
			result = queryById(hostId);
			// cache host device mapping
			cacheService.saveHostDevice((Host)result, false);
		}
		catch (Exception e) {
			log.warn("Failed to retreive Host by ID: " + hostId);
		}
		stopMetricsTimer(timer);
		return (Host) result;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostByHostName(java.lang.String, org.groundwork.foundation.dao.FilterCriteria)
	 */
	public Host getHostByHostName(String hostName) throws BusinessServiceException {
		if (StringUtils.isBlank(hostName)) throw new IllegalArgumentException("Invalid null / empty host name parameter.");
		Integer id = getHostIdByHostName(hostName);
		if (id == null) {
			return null;
		}
		Host host = getHostByHostId(id);
		if (host != null && !host.getHostName().equalsIgnoreCase(hostName)) {
			cacheService.invalidate(Collections.singletonList(hostName), false);
			return null;
		}
		return host;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHosts(org.groundwork.foundation.dao.FilterCriteria, org.groundwork.foundation.dao.SortCriteria, int, int)
	 */
	public FoundationQueryList getHosts(FilterCriteria filterCriteria,
			SortCriteria sortCriteria, int firstResult, int maxResults)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();

		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;

		FoundationQueryList list = query(filterCriteria, sortCriteria, firstResult, maxResults);

		// cache host device mapping
		cacheService.saveHostDevice(list.getResults(), false);

		stopMetricsTimer(timer);
		return list;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHosts(java.lang.String)
	 */
	public Collection<Host> getHosts(List<String> hostList) throws BusinessServiceException {

		CollageTimer timer = startMetricsTimer();
	   	if (hostList == null || hostList.size() == 0) {
    		throw new IllegalArgumentException("Method getHosts() -- Invalid null / empty host list parameter.");
    	}

        Collection<Host> results = new ArrayList<>();
        for (String hostName: hostList) {
        	Host host = getHostByHostName(hostName);
			if (host != null) {
				results.add(host);
			}
		}

		// cache host device mapping
		cacheService.saveHostDevice(results, false);

        stopMetricsTimer(timer);
        return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostsByDeviceId(int)
	 */
	public Collection<Host> getHostsByDeviceId(int devID)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();

		FilterCriteria filterCriteria = FilterCriteria.eq(Host.HP_DEVICE_ID, devID);
		
		Collection<Host> results = (Collection<Host>)query(filterCriteria,null);

		// cache host device mapping
		cacheService.saveHostDevice(results, false);

		stopMetricsTimer(timer);
		return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostsByDeviceIdentification(java.lang.String)
	 */
	public Collection<Host> getHostsByDeviceIdentification(String devDescrip)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		FilterCriteria filterCriteria = FilterCriteria.eq(Host.HP_DEVICE_IDENTIFICATION, devDescrip);
		
		Collection<Host> results = (Collection<Host>)query(filterCriteria,null);

		// cache host device mapping
		cacheService.saveHostDevice(results, false);

		stopMetricsTimer(timer);
		return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostsByHostGroupId(int, org.groundwork.foundation.dao.FilterCriteria, org.groundwork.foundation.dao.SortCriteria, int, int)
	 */
	public FoundationQueryList getHostsByHostGroupId(int hgId,
			FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
			int maxResults) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();

		FilterCriteria tmpFilter = FilterCriteria.eq(Host.HP_HOSTGROUP_ID, hgId);
		
		if (filter != null)
			tmpFilter.and(filter);
		
		FoundationQueryList results = this.query(tmpFilter, sortCriteria, firstResult, maxResults);

		// cache host device mapping
		cacheService.saveHostDevice(results.getResults(), false);

		stopMetricsTimer(timer);
		return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostsByHostGroupName(java.lang.String, org.groundwork.foundation.dao.FilterCriteria, org.groundwork.foundation.dao.SortCriteria, int, int)
	 */
	public FoundationQueryList getHostsByHostGroupName(String hgName,
			FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
			int maxResults) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();

		FilterCriteria tmpFilter = FilterCriteria.eq(Host.HP_HOSTGROUP_NAME, hgName);
		
		if (filter != null)
			tmpFilter.and(filter);
		
		FoundationQueryList results = this.query(tmpFilter, sortCriteria, firstResult, maxResults);

		// cache host device mapping
		cacheService.saveHostDevice(results.getResults(), false);

		stopMetricsTimer(timer);
		return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostsByMonitorServer(java.lang.String, int, int)
	 */
	public FoundationQueryList getHostsByMonitorServer(String monitorServer,
			int firstResult, int maxResults) throws BusinessServiceException {

		CollageTimer timer = startMetricsTimer();
		FilterCriteria tmpFilter = FilterCriteria.eq(Host.HP_MONITORSERVER_NAME, monitorServer);
				
		FoundationQueryList results = this.query(tmpFilter, null,  firstResult, maxResults);

		// cache host device mapping
		cacheService.saveHostDevice(results.getResults(), false);

		stopMetricsTimer(timer);
		return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getHostsByServiceName(java.lang.String)
	 */
	public Collection<Host> getHostsByServiceName(String serviceName)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();

		FilterCriteria filter = FilterCriteria.eq(Host.HP_SERVICE_NAME, serviceName);
		Collection<Host> results = (Collection<Host>)this.query(filter, null);

		// cache host device mapping
		cacheService.saveHostDevice(results, false);

		stopMetricsTimer(timer);
		return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getStatusByHostId(int)
	 */
	public HostStatus getStatusByHostId(int hostId)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();

		Host host = this.getHostByHostId(hostId);
		
		if (host == null) {
			String msg = "No HostStatus available. Host not found for HostID " + hostId;
			throw new BusinessServiceException(msg);
		}
		stopMetricsTimer(timer);

		return host.getHostStatus();
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#getStatusByHostName(java.lang.String)
	 */
	public HostStatus getStatusByHostName(String hostName) throws BusinessServiceException {
		Integer id = getHostIdByHostName(hostName);
		return (id == null ? null : getStatusByHostId(id));
	}
	
	
	public Collection<String> getHostList() throws BusinessServiceException
	{
		CollageTimer timer = startMetricsTimer();
		List<String> list = _foundationDAO.sqlQuery("select HostName from Host");
		stopMetricsTimer(timer);
		return list;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#hostLookup(java.lang.String)
	 */
	public Collection<Host> hostLookup(String hostName)
			throws BusinessServiceException {
        CollageTimer timer = startMetricsTimer();
		FilterCriteria filter = FilterCriteria.ilike(Host.HP_NAME, hostName);
		
		Collection<Host> results = (Collection<Host>)this.query(filter, null);

		// cache host device mapping
		cacheService.saveHostDevice(results, false);

		stopMetricsTimer(timer);
		return results;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#saveHost(com.groundwork.collage.model.Host)
	 */
	public void saveHost(Host host) throws BusinessServiceException 
	{
		CollageTimer timer = startMetricsTimer();
		// Delegate
		save(host);
		// add to id cache
		ids.put(host.getHostName().toLowerCase(), host.getHostId());
		// cache host device mapping
		cacheService.saveHostDevice(host, true);
		// refresh ids and autocomplete in event names changed
        refreshAllCachesOnTransactionCommit(null);
        stopMetricsTimer(timer);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.host.HostService#saveHost(java.util.Collection)
	 */
	public void saveHost(Collection<Host> hostList)
			throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		// Delegate
		save(hostList);
		// add to id cache
		for (Host host : hostList) {
			ids.put(host.getHostName().toLowerCase(), host.getHostId());
		}
		// cache host device mapping
		cacheService.saveHostDevice(hostList, true);
        // refresh ids and autocomplete in event names changed
        refreshAllCachesOnTransactionCommit(null);
		stopMetricsTimer(timer);
	}

    public FoundationQueryList queryHosts(String hql, String hqlCount, int firstResult, int maxResults) {
		CollageTimer timer = startMetricsTimer();
        FoundationQueryList list = _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
		// cache host device mapping
		cacheService.saveHostDevice(list.getResults(), false);
		stopMetricsTimer(timer);
        return list;
    }

    @Override
    public Iterator<AutocompleteName> openNamesIterator(String namesEntityType) {
        return new AutocompleteNamesIterator(getSessionFactory(), "select hostname from host");
    }

    @Override
    public void closeNamesIterator(Iterator<AutocompleteName> iterator) {
        ((AutocompleteNamesIterator)iterator).close();
    }

	@Override
	public void invalidateHosts(Collection<String> hostNames) {
		for (String hostName : hostNames) {
			ids.invalidate(hostName.toLowerCase());
		}
	}

	/**
     * Set autocomplete service, (normally Spring injected).
     *
     * @param hostAutocompleteService autocomplete service
     */
    public void setHostAutocompleteService(Autocomplete hostAutocompleteService) {
        this.hostAutocompleteService = hostAutocompleteService;
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
     * Transaction synchronization to refresh autocomplete on commit.
     */
    private class HostTransactionSynchronization extends TransactionSynchronizationAdapter {
		private Collection<String> deleteHostNames;

		private HostTransactionSynchronization(Collection<String> deleteHostNames) {
			this.deleteHostNames = deleteHostNames;
		}

		@Override
        public void afterCommit() {
			if (hostAutocompleteService != null) {
                hostAutocompleteService.refresh();
            }
            if (hostIdentityAutocompleteService != null) {
                hostIdentityAutocompleteService.refresh();
            }
            if (statusAutocompleteService != null) {
                statusAutocompleteService.refresh();
            }
			if (deleteHostNames != null && cacheService != null) {
				cacheService.invalidate(deleteHostNames, false);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof HostTransactionSynchronization)) {
				return false;
			}
			HostTransactionSynchronization other = (HostTransactionSynchronization)obj;
			return (deleteHostNames == null && other.deleteHostNames == null) ||
					(deleteHostNames != null && other.deleteHostNames != null &&
							deleteHostNames.size() == other.deleteHostNames.size() &&
							deleteHostNames.containsAll(other.deleteHostNames));
		}
	};

    /**
     * Register transaction synchronization to invoke id and autocomplete
     * refresh on commit of current transaction.
	 *
	 * @param deleteHosts
     */
    private void refreshAllCachesOnTransactionCommit(Collection<Host> deleteHosts) {
		Collection<String> deleteHostNames = null;
		if (deleteHosts != null) {
			deleteHostNames = new HashSet<>();
			for (Host deleteHost : deleteHosts) {
				deleteHostNames.add(deleteHost.getHostName());
			}
		}
        try {
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronization synchronization = new HostTransactionSynchronization(deleteHostNames);
			if (!synchronizations.contains(synchronization)) {
				TransactionSynchronizationManager.registerSynchronization(synchronization);
			}
        } catch (IllegalStateException ise) {
        }
    }
}
