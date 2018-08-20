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
package org.groundwork.foundation.bs.device;

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.MonitorServer;
import com.groundwork.collage.util.Autocomplete;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.cache.BusinessCacheService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Device Service Implementation Class
 *
 */
public class DeviceServiceImpl extends EntityBusinessServiceImpl implements DeviceService
{
	/** Default Sort Criteria */
	private static final SortCriteria DEFAULT_SORT_CRITERIA = 
		SortCriteria.asc(Device.HP_IDENTIFICATION);
	
	private MonitorServerService _monitorService = null;
    private Autocomplete hostAutocompleteService = null;
    private Autocomplete hostIdentityAutocompleteService = null;
    private Autocomplete statusAutocompleteService = null;
	private BusinessCacheService cacheService = null;

	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(DeviceServiceImpl.class);
	
	public DeviceServiceImpl (FoundationDAO foundationDAO, MonitorServerService ms)
	{		
		super(foundationDAO, Device.INTERFACE_NAME, Device.COMPONENT_NAME);
		_monitorService = ms;
	}
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#addDevicesToMonitorServer(java.lang.String, java.lang.String)
	 */
	public int addDevicesToMonitorServer(String monitorServerName, List<String> deviceIdentifications)
			throws BusinessServiceException
	{
		if (monitorServerName == null || monitorServerName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty monitor server name parameter.");
		
		// Nothing to do
		if (deviceIdentifications == null || deviceIdentifications.size() == 0)
		{
			if (log.isWarnEnabled() == true)
				log.warn("attempting to add null list of devices to MonitorServer - " + monitorServerName);
		
			return 0;
		}
		
		// Check for existing monitor server
		MonitorServer monitorServer = _monitorService.getMonitorServerByName(monitorServerName);
		
		// Create monitor server
		if (monitorServer == null)
		{
			monitorServer = _monitorService.createMonitorServer(monitorServerName);
		}
 
		// Get Devices
		List<Device> deviceList = getDevices(deviceIdentifications, null);
		
		int added = 0;
		
		// Add monitor server to devices b/c it is faster then loading all devices for a monitor server
		// the inverse relationship is much faster.
		Iterator<Device> it = deviceList.iterator();
		Device device = null;
		while (it.hasNext())
		{
			device = it.next();
			
			if (device.addMonitorServer(monitorServer))
			{
				added++;
				
				// Save each device
				save(device);
			}
		}

		return added;
	}
	
	public int removeDevicesFromMonitorServer(String monitorServerName, List<String> deviceIdentifications)
	throws BusinessServiceException
	{
		if (monitorServerName == null || monitorServerName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty monitor server name parameter.");
		
		// Nothing to do
		if (deviceIdentifications == null || deviceIdentifications.size() == 0)
		{
			if (log.isWarnEnabled() == true)
				log.warn("attempting to add null list of devices to MonitorServer - " + monitorServerName);
		
			return 0;
		}
		
		// Check for existing monitor server
		MonitorServer monitorServer = _monitorService.getMonitorServerByName(monitorServerName);		
		if (monitorServer == null)
		{
			throw new BusinessServiceException(
					"Unable to remove devices from Monitor Server.  Monitor Server Not Found - " 
					+ monitorServerName);
		}
 
		// Get Devices
		List<Device> deviceList = getDevices(deviceIdentifications, null);
		
		int removed = 0;
		
		// Remove monitor server from devices b/c it is faster then loading all devices for a monitor server
		// the inverse relationship is much faster.
		Iterator<Device> it = deviceList.iterator();
		Device device = null;
		while (it.hasNext())
		{
			device = it.next();
			
			if (device.removeMonitorServer(monitorServer))
			{
				removed++;			
			
				// Save each device
				save(device);
			}
		}

		return removed;
	}
	
	public Device createDevice() throws BusinessServiceException
	{
		return (Device)this.create();
	}

	public Device createDevice(String identification, String displayName) throws BusinessServiceException
	{
		Device device = (Device)this.create();
		device.setIdentification(identification);
		
		/* If the DisplayName is not defined use the identification as display name. The display Name is used
		 * in the console to group/sort entries and therefore it shouldn't be null
		 */
		if (displayName == null || displayName.length() < 1)
			device.setDisplayName(identification);
		else
			device.setDisplayName(displayName);
		
		return device;
	}

	public void deleteDevice(Device device) throws BusinessServiceException
	{
		// delete device
		delete(device);

        // refresh ids and autocomplete since names deleted
        refreshAllCachesOnTransactionCommit(Collections.singletonList(device.getDeviceId()));
	}

	public void deleteDeviceById(int id) throws BusinessServiceException
	{
		this.delete(id);

        // refresh ids and autocomplete since names deleted
        refreshAllCachesOnTransactionCommit(Collections.singletonList(id));
	}

	public void deleteDeviceByIdentification(String identification) throws BusinessServiceException
	{
		if (identification == null || identification.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty device identification parameter.");

		Device device = getDeviceByIdentification(identification);
		
		if (device == null)
		{
			if (log.isWarnEnabled() == true)
				log.warn("Unable to delete device - Not Found - " + identification);			
			return;
		}
		
		delete(device);

        // refresh ids and autocomplete since names deleted
        refreshAllCachesOnTransactionCommit(Collections.singletonList(device.getDeviceId()));
	}

	public void deleteDevices(int[] deviceIdList) throws BusinessServiceException
	{
		delete(deviceIdList);

        // refresh ids and autocomplete since names deleted
		Collection<Integer> deletedDeviceIds = new ArrayList<>();
		for (int deviceId : deviceIdList) {
			deletedDeviceIds.add(deviceId);
		}
        refreshAllCachesOnTransactionCommit(deletedDeviceIds);
	}

	public void deleteDevices(Collection<Device> devices) throws BusinessServiceException
	{
		delete(devices);

        // refresh ids and autocomplete since names deleted
		Collection<Integer> deletedDeviceIds = new ArrayList<>();
		for (Device device : devices) {
			deletedDeviceIds.add(device.getDeviceId());
		}
        refreshAllCachesOnTransactionCommit(deletedDeviceIds);
	}

	public void deleteDevices(String[] deviceIdentifications) throws BusinessServiceException
	{
		// Query devices by identification
		List<Device> deviceList = getDevices(deviceIdentifications, null);
		
		// delete devices
		delete(deviceList);

        // refresh ids and autocomplete since names deleted
		Collection<Integer> deletedDeviceIds = new ArrayList<>();
		for (Device device : deviceList) {
			deletedDeviceIds.add(device.getDeviceId());
		}
        refreshAllCachesOnTransactionCommit(deletedDeviceIds);
	}

	public int detachChildDevices(String identification, List<String> childIdents) throws BusinessServiceException
	{
		if (identification == null || identification.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty device identification parameter.");
		
		// Nothing to do if no child devices are provided.
		if (childIdents == null || childIdents.size() == 0)
			return 0;
		
		Device parentDevice = this.getDeviceByIdentification(identification);

		if (parentDevice == null) {
			log.warn("Attempting to retrieve parent devices from non-existing device - " + identification);
			return -1;
		}

		// Query child devices - Note:  If the one or more child devices are not found they are just
		// not returned in the list
		List<Device> childList = getDevices(childIdents, null);
		
		// Remove Children
		int numRemoved = 0;
		Device childDevice = null;
		Set children = parentDevice.getChildren();
		Iterator<Device> it = childList.iterator();		
		while (it.hasNext())
		{
			childDevice = it.next();
			
			if (children.contains(childDevice))
			{
				parentDevice.removeChild(childDevice);
				numRemoved++;
				
				if (log.isInfoEnabled()) 
					log.info("removed child Device '" + childDevice.getIdentification()
							 + "' for Device '" + identification + "'");				
			}
		}
		
		// Persist changes
		save(parentDevice);

		if (log.isInfoEnabled()) 
			log.info("removed " + numRemoved 
						+ " children Devices from Device '" 
						+ identification + "'");
		
		return numRemoved;
	}
	
	/**
	 * Attaches devices to parent device.  Parent must exist.  If a child does not exist it is ignored.
	 * 
	 * @param deviceIdent
	 * @param childIdents
	 * @return
	 * @throws BusinessServiceException
	 */
	public int attachChildDevices(String deviceIdent, List<String> childIdents) throws BusinessServiceException
	{
		if (deviceIdent == null || deviceIdent.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty parent device identification parameter.");
		
		// Nothing to do if no child devices are provided.
		if (childIdents == null || childIdents.size() == 0)
			return 0;
				
		Device parentDevice = this.getDeviceByIdentification(deviceIdent);

		if (parentDevice == null) {
			log.warn("Attempting to retrieve parent devices from non-existing device - " + deviceIdent);
			return -1;
		}
		
		// Query child devices - Note:  If the one or more child devices are not found they are just
		// not returned in the list
		List<Device> childList = getDevices(childIdents, null);		

		// Attach Children	
		int numAttached = 0;
		Device childDevice = null;
		Set children = parentDevice.getChildren();
		Iterator<Device> it = childList.iterator();		
		while (it.hasNext())
		{
			childDevice = it.next();
			
			if (children.contains(childDevice) == false)
			{
				parentDevice.addChild(childDevice);
				numAttached++;
				
				if (log.isInfoEnabled()) 
					log.info("attached child Device '" + childDevice.getIdentification()
							 + "' for Device '" + deviceIdent + "'");				
			}
		}
				
		// Persist changes
		save(parentDevice);

		if (log.isInfoEnabled()) 
			log.info("attached " + numAttached 
						+ " children Devices to Device '" 
						+ numAttached + "'");
		
		return numAttached;		
	}

	public int detachParentDevices(String childIdent, List<String> parentIdents) throws BusinessServiceException
	{
		if (childIdent == null || childIdent.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty child device identification parameter.");
		
		// Nothing to do if no parent devices are provided.
		if (parentIdents == null || parentIdents.size() == 0)
			return 0;
		
		// because parents cannot be saved from the child (getParents is the
		// 'inverse' side of the relationship), we must retrieve each parent, 
		// remove the child, and save the parent - 
		Device child = this.getDeviceByIdentification(childIdent);
		if (child == null) {
			log.warn("Attempting to retrieve parent devices from non-existing device - " + childIdent);
			return -1;
		}

		// Get parents - If a parent identification does not exist it is just not returned in the list
		List<Device> parentList = getDevices(parentIdents, null);
		if (parentList == null || parentList.size() == 0)
			return 0;
				
		// iterator over the parent devices and add child to it.
		Device parent = null;
		int numRemoved = 0;
		Iterator<Device> it = parentList.iterator();
		
		while (it.hasNext())
		{
			parent = it.next();		
			
			if (parent.getChildren().contains(child) == true)
			{
				parent.removeChild(child);
				numRemoved++;
			}
		}
		
		// Persist changes
		save(parentList);
		
		if (log.isInfoEnabled()) 
			log.info("attached " + numRemoved 
					+ " parent Devices for Device '" 
					+ childIdent + "'");
		
		return numRemoved;		
	}

	/**
	 * Attaches child to specified parent devices.  Child must exist.  If a parent does not exist it is
	 * ignored. 
	 * 
	 * @param childIdent
	 * @param parentIdents
	 * @return
	 * @throws BusinessServiceException
	 */
	public int attachParentDevices(String childIdent, List<String> parentIdents) throws BusinessServiceException
	{
		if (childIdent == null || childIdent.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty child device identification parameter.");
		
		// Nothing to do if no parent devices are provided.
		if (parentIdents == null || parentIdents.size() == 0)
			return 0;
		
		// because parents cannot be saved from the child (getParents is the
		// 'inverse' side of the relationship), we must retrieve each parent, 
		// remove the child, and save the parent - 
		Device child = this.getDeviceByIdentification(childIdent);
		if (child == null) {
			log.warn("Attempting to attach parent devices for non-existing device - " + childIdent);
			return -1;
		}

		// Get parents - If a parent identification does not exist it is just not returned in the list
		List<Device> parentList = getDevices(parentIdents, null);
		if (parentList == null || parentList.size() == 0)
			return 0;
		
		
		// iterator over the parent devices and add child to it.
		Device parent = null;
		int numAttached = 0;
		Iterator<Device> it = parentList.iterator();
		
		while (it.hasNext())
		{
			parent = it.next();		
			
			if (parent.getChildren().contains(child) == false)
			{
				parent.addChild(child);
				numAttached++;
			}
		}
		
		// Persist changes
		save(parentList);
		
		if (log.isInfoEnabled()) 
			log.info("attached " + numAttached 
					+ " parent Devices for Device '" 
					+ childIdent + "'");
		
		return numAttached;		
	}
	
	public Device getDeviceByHostId(int hostId) throws BusinessServiceException
	{
		FilterCriteria filterCriteria = FilterCriteria.eq(Device.HP_HOST_ID, hostId);
		FoundationQueryList results = query(filterCriteria, null, -1, -1);
		
		if (results == null || results.size() == 0)
			return null;
		
		return (Device)results.get(0);
	}

	public Device getDeviceByHostName(String hostName) throws BusinessServiceException
	{
		if (hostName == null || hostName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty host name parameter.");
		
		FilterCriteria filterCriteria = FilterCriteria.eq(Device.HP_HOST_NAME, hostName);
		FoundationQueryList results = query(filterCriteria, null, -1, -1);
		
		if (results == null || results.size() == 0)
			return null;
		
		return (Device)results.get(0);
	}

	public Device getDeviceById(int deviceId) throws BusinessServiceException
	{
		return (Device)this.queryById(deviceId);
	}

	public Device getDeviceByIdentification(String identification) throws BusinessServiceException
	{
		if (identification == null || identification.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty identification parameter.");
		
		// Retrieve device by identification
		FilterCriteria filterCriteria = FilterCriteria.eq(Device.HP_IDENTIFICATION, identification);
		FoundationQueryList results = query(filterCriteria, null, -1, -1);
		
		if (results == null || results.size() == 0)
			return null;

		return (Device)results.get(0);
	}

	public FoundationQueryList getDevices(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException
	{
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;
		
		return query(filterCriteria, sortCriteria, firstResult, maxResults);
	}

	public List<Device> getDevices(int[] deviceIdList, SortCriteria sortCriteria) throws BusinessServiceException
	{
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;

		return (List<Device>)queryById(deviceIdList, sortCriteria);
	}

	public List<Device> getDevices(String[] deviceIdentifications, SortCriteria sortCriteria) throws BusinessServiceException
	{
		if (deviceIdentifications == null || deviceIdentifications.length == 0)
			throw new IllegalArgumentException("Invalid null / empty device identification array parameter.");		
		
		// Build list
		int numNames = deviceIdentifications.length;
		List<String> deviceList = new ArrayList<String>(numNames);
		
		for (int i = 0; i < numNames; i++)
		{
			deviceList.add(deviceIdentifications[i]);			
		}
		
		return getDevices(deviceList, sortCriteria);
	}
	
	public  List<Device> getDevices(List<String> deviceIdentifications, SortCriteria sortCriteria) 
	throws BusinessServiceException
	{
		if (deviceIdentifications == null || deviceIdentifications.size() == 0)
			throw new IllegalArgumentException("Invalid null / empty device identification list parameter.");
		
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;
		
		// Build filter criteria
		int numNames = deviceIdentifications.size();
		String identification;
		FilterCriteria filterCriteria = null;
		
		for (int i = 0; i < numNames; i++)
		{
			identification = deviceIdentifications.get(i);
			
			if (filterCriteria == null)
				filterCriteria = FilterCriteria.eq(Device.HP_IDENTIFICATION, identification);
			else
				filterCriteria.or(FilterCriteria.eq(Device.HP_IDENTIFICATION, identification));
		}
		
		return (List<Device>)query(filterCriteria, sortCriteria);		
	}

	public FoundationQueryList getDevicesByMonitorServerId(int monitorServerId, FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException
	{
		FilterCriteria monitorCriteria = FilterCriteria.eq(Device.HP_MONITOR_SERVER_ID, monitorServerId);
		
		if (filterCriteria != null)
			filterCriteria.and(monitorCriteria);
		else
			filterCriteria = monitorCriteria;
		
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;

		return query(filterCriteria, sortCriteria, firstResult, maxResults);
	}

	public void saveDevice(Device device) throws BusinessServiceException
	{
		if (device == null)
			throw new IllegalArgumentException("Invalid null Device parameter.");
		
		save(device);		
	}

	public Device saveDevice(String identification, String displayName, String monitorServerName) throws BusinessServiceException
	{		
		if (identification == null || identification.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty device identification parameter.");
		
		if (monitorServerName == null || monitorServerName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty monitor server parameter.");
		
		// Create new device
		Device device = createDevice(identification, displayName);
		
		// Monitor Server must already exist
		MonitorServer monitorServer = _monitorService.getMonitorServerByName(monitorServerName);
		
		if (monitorServer == null)
			throw new BusinessServiceException(
					"Unable to relate device to Monitor Server.  Monitor does not exist - " 
					+ monitorServerName);
					
		// Relate monitor server to device
		device.getMonitorServers().add(monitorServer);

		// Persist Device
		save(device);
		
		return device;
	}

	public void saveDevices(Collection<Device> devices) throws BusinessServiceException
	{
		if (devices == null)
			throw new IllegalArgumentException("Invalid null Device collection parameter.");
		
		save(devices);			
	}

    public FoundationQueryList queryDevices(String hql, String hqlCount, int firstResult, int maxResults) {
        FoundationQueryList list= _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
        return list;
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
     * Transaction synchronization to refresh host autocomplete on commit.
     */
	private class DeviceTransactionSynchronization extends TransactionSynchronizationAdapter {
		private Collection<Integer> deleteDeviceIds;

		private DeviceTransactionSynchronization(Collection<Integer> deleteDeviceIds) {
			this.deleteDeviceIds = deleteDeviceIds;
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
			if (deleteDeviceIds != null && cacheService != null) {
				cacheService.invalidate(deleteDeviceIds);
			}
        }

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DeviceTransactionSynchronization)) {
				return false;
			}
			DeviceTransactionSynchronization other = (DeviceTransactionSynchronization)obj;
			return (deleteDeviceIds == null && other.deleteDeviceIds == null) ||
					(deleteDeviceIds != null && other.deleteDeviceIds != null &&
							deleteDeviceIds.size() == other.deleteDeviceIds.size() &&
							deleteDeviceIds.containsAll(other.deleteDeviceIds));
		}
    };

    /**
     * Register transaction synchronization to invoke ids and autocomplete
     * refresh on commit of current transaction.
	 *
	 * @param deleteDeviceIds
     */
    private void refreshAllCachesOnTransactionCommit(Collection<Integer> deleteDeviceIds) {
        try {
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronization synchronization = new DeviceTransactionSynchronization(deleteDeviceIds);
			if (!synchronizations.contains(synchronization)) {
				TransactionSynchronizationManager.registerSynchronization(synchronization);
			}
        } catch (IllegalStateException ise) {
        }
    }
}
