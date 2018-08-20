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

import java.util.Collection;
import java.util.List;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import com.groundwork.collage.model.Device;

/**
 * @author glee
 *
 */
public interface DeviceService extends BusinessService 
{
	/**
	 * loads the Device with the identification provided, either an IP or MAC
	 * address; this method loads the immediate Children and Parents of this
	 * Device, but none of its attached Hosts or Services
	 * 
	 * @param identification
	 */
	public Device getDeviceByIdentification(String identification) throws BusinessServiceException;

	/**
     * finds the Device with the id provided.
     *  
     * @param deviceId
     * @return
     * @throws BusinessServiceException
	 */
    public Device getDeviceById(int deviceId) throws BusinessServiceException;
    
	/***
	 * Retrieves the device for the specified host
	 * 
	 * @param hostId
	 * @return
	 * @throws BusinessServiceException
	 */
    public Device getDeviceByHostId(int hostId) throws BusinessServiceException;
    
    /**
     * Retrieves the device for the specified host
     * 
     * @param hostName
     * @return
     * @throws BusinessServiceException
     */
    public Device getDeviceByHostName(String hostName) throws BusinessServiceException;
    
    /**
     * Retrieve devices which match the specified criteria.
     * 
     * @param filterCriteria
     * @param sortCriteria
     * @param firstResult
     * @param maxResults
     * @return
     * @throws BusinessServiceException
     */
    public FoundationQueryList getDevices(FilterCriteria filterCriteria, 
    									SortCriteria sortCriteria, 
    									int firstResult, 
    									int maxResults) throws BusinessServiceException;
    
    /**
     * Retrieve devices which match the specified identification values.
     * 
     * @param deviceIdList
     * @param sortCriteria
     * @return
     * @throws BusinessServiceException
     */
	public List<Device> getDevices(String[] deviceIdentifications, SortCriteria sortCriteria) throws BusinessServiceException;
	
	   /**
     * Retrieve devices which match the specified identification values.
     * 
     * @param deviceIdList
     * @param sortCriteria
     * @return
     * @throws BusinessServiceException
     */
	public List<Device> getDevices(List<String> deviceIdentifications, SortCriteria sortCriteria) throws BusinessServiceException;
	
	/**
	 * Returns the devices related to the specified monitor server.
	 * 
	 * @param monitorServerId
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	public FoundationQueryList getDevicesByMonitorServerId(int monitorServerId,
												  FilterCriteria filterCriteria,
												  SortCriteria sortCriteria,
												  int firstResult, 
												  int maxResults)  throws BusinessServiceException;
	
	/**
	 * Retrieve devices which match the specified ids.
	 * 
	 * @param deviceIdList
	 * @param sortCriteria
	 * @return
	 * @throws BusinessServiceException
	 */
	public List<Device> getDevices(int[] deviceIdList, SortCriteria sortCriteria) throws BusinessServiceException;	
    
    /**
     * Create new device instance
     * 
     * @return
     */
    public Device createDevice () throws BusinessServiceException;
   
    /**
     * Create new device instance with the specified information.  The instance is NOT persisted.
     * 
     * @param identification
     * @param displayName
     * @return
     * @throws BusinessServiceException
     */
    public Device createDevice (String identification, String displayName) throws BusinessServiceException;
        
    /**
     * Persists the specified device.
     * 
     * @param device
     */
    public void saveDevice(Device device) throws BusinessServiceException;
    
    /**
     * Creates device and relates it to the specified monitor server and persists the device.
     *   
     * Note:  the monitor server MUST already exist.
     * 
     * @param identification
     * @param displayName
     * @param monitorServer
     * @return
     * @throws BusinessServiceException
     */
    public Device saveDevice (String identification, String displayName, String monitorServer) throws BusinessServiceException;
    
    /**
     * Persists the specified devices.
     * 
     * @param devices
     */
	public void saveDevices(Collection<Device> devices) throws BusinessServiceException;
	
	/**
	 * Removes a Device from the system and all related entities.
	 * 
	 * @param id
	 * @throws BusinessServiceException
	 */
	public void deleteDeviceById(int id) throws BusinessServiceException;	
	
	/**
	 * Removes a Device with the specified identification from the system and all related entities.
	 * 
	 * @param identification
	 * @throws BusinessServiceException
	 */
	public void deleteDeviceByIdentification(String identification) throws BusinessServiceException;	
	
	/**
	 * Removes a Device from the system and all related entities.
	 * 
	 * @param device
	 * @throws BusinessServiceException
	 */
	public void deleteDevice(Device device) throws BusinessServiceException;		
	
	/**
	 * Removes specified devices and all related entities.
	 * 
	 * @param devices
	 * @throws BusinessServiceException
	 */
	public void deleteDevices(Collection<Device> devices) throws BusinessServiceException;
	
	/**
	 * Removes all devices identified as well as all their related entities.
	 * 
	 * @param deviceIdList String array of device identification values
	 * @throws BusinessServiceException
	 */
	public void deleteDevices(String[] deviceIdentifications) throws BusinessServiceException;
	
	/**
	 * Removes all devices identified as well as all their related entities.
	 * 
	 * @param deviceIdList int array of device ids
	 * @throws BusinessServiceException
	 */
	public void deleteDevices(int[] deviceIdList) throws BusinessServiceException;
	
	/**
	 * Removes all specified child devices for the specified device
	 *  
	 * @param deviceIdent - the identifier (IP/MAC/Name) of the device affected
	 * @param childIdents - an array of identifiers of the children to be removed
	 * @return -the number of children actually removed
	 */
	public int detachChildDevices(String deviceIdent, List<String> childIdents) throws BusinessServiceException;
	
	/**
	 * Attaches devices to parent device.  Parent must exist.  If a child does not exist it is ignored.
	 * 
	 * @param deviceIdent
	 * @param childIdents
	 * @return
	 * @throws BusinessServiceException
	 */
	public int attachChildDevices(String deviceIdent, List<String> childIdents) throws BusinessServiceException;

	/**
	 * Removes specified device from all specified parent device child devices.
	 * 
	 * @param childIdent - the identifier (IP/MAC/Name) of the device affected
	 * @param parentIdents - an array of identifiers of the parents to remove child from.
	 * @return - the number of parents actually updated
	 */
	public int detachParentDevices(String childIdent, List<String> parentIdents) throws BusinessServiceException;	
	
	/**
	 * Attaches child to specified parent devices.  Child must exist.  If a parent does not exist it is ignored.
	 * 
	 * @param deviceIdent
	 * @param childIdents
	 * @return
	 * @throws BusinessServiceException
	 */
	public int attachParentDevices(String childIdent, List<String> parentIdents) throws BusinessServiceException;
	
	/**
	 * Adds a list of devices to the MonitorServer specified, and persists the
	 * each device to the database; Note:  The monitor server will be created
	 * if it does not exist.
	 *
	 * @param monitorServerName
	 * @param deviceIdentifications - String array of device identifications
	 * @return the number of devices added - note that this may be less than the
	 *	number of devices supplied if any of the devices was already monitored by
	 *	the MonitorServer
	 */
	public int addDevicesToMonitorServer(String monitorServerName, List<String> deviceIdentifications) 
	 throws BusinessServiceException;
	
	/**
	 * Removes a list of devices from the MonitorServer specified, and persists the
	 * each device to the database.
	 * 
	 * @param monitorServerName
	 * @param deviceIdentifications
	 * @return
	 * @throws BusinessServiceException
	 */
	public int removeDevicesFromMonitorServer(String monitorServerName, List<String> deviceIdentifications)
	throws BusinessServiceException;

    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hql
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of host objects matching the query
     */
    public FoundationQueryList queryDevices(String hql, String hqlCount, int firstResult, int maxResults);

}
