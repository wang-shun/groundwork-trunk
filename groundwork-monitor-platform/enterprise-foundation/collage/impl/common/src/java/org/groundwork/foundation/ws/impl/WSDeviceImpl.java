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

/*Created on: Mar 20, 2006 */

package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;
import java.util.Collection;

import com.groundwork.collage.metrics.CollageTimer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.api.WSDevice;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.DeviceQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.impl.CollageConvert;
import com.groundwork.collage.model.Device;
/**
 * WebServiec Implementation for WSDevice interface
 * @author rogerrut
 *
 */
public class WSDeviceImpl extends WebServiceImpl implements WSDevice {
	
	/* Enable logging */
	protected static Log log = LogFactory.getLog(WSDeviceImpl.class);	

	public WSDeviceImpl() 
	{ 
	}
	
	public WSFoundationCollection getDevice(DeviceQueryType deviceType,
											String value, 
											int fromRange, 
											int toRange, 
											SortCriteria orderedBy)
	throws WSFoundationException 
	{
	    CollageTimer timer = startMetricsTimer();
		WSFoundationCollection devices = null;
        
        // check first for empty event type
        if (deviceType == null) 
        {
        	log.error("DeviceQueryType cannot be null");
            throw new WSFoundationException("DeviceQueryType cannot be null", ExceptionType.WEBSERVICE);            
        }
        
        try {
	        if (org.groundwork.foundation.ws.model.impl.DeviceQueryType.ALL.equals(deviceType))
	            devices = getDevices(fromRange, toRange, orderedBy);
	        else if (org.groundwork.foundation.ws.model.impl.DeviceQueryType.DEVICEID.equals(deviceType))
	            devices = getDeviceByID(value);
	        else if (org.groundwork.foundation.ws.model.impl.DeviceQueryType.DEVICEIDENTIFICATION.equals(deviceType))
	            devices = getDeviceByIdentification(value);
	        else if (org.groundwork.foundation.ws.model.impl.DeviceQueryType.CHILDREN.equals(deviceType)) 
	            devices = getDeviceChildren(value, fromRange, toRange, orderedBy);
	        else if (org.groundwork.foundation.ws.model.impl.DeviceQueryType.HOSTID.equals(deviceType))
	            devices = getDeviceForHostID(value);
	        else if (org.groundwork.foundation.ws.model.impl.DeviceQueryType.HOSTNAME.equals(deviceType))
	            devices = getDeviceForHostName(value);
	        else if (org.groundwork.foundation.ws.model.impl.DeviceQueryType.PARENTS.equals(deviceType))
	            devices = getDeviceParents(value);
	        else
	            throw new WSFoundationException("Invalid DeviceQueryType specified in getDevice", ExceptionType.WEBSERVICE);
	            
	        return devices;
        }
        catch (WSFoundationException wsfe)
        {
        	log.error("Error occurred in getDevice()", wsfe);
        	throw wsfe;
        }
        catch (Exception e)
        {
        	log.error("Error occurred in getDevice()", e);
        	throw new WSFoundationException("Error occurred in getDevice()", ExceptionType.WEBSERVICE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
    
	/**
     * String parameter version of getDevice() in order to support clients such as the BIRT ODA.
     * 
     * @param type
     * @param value
     * @param startRange
     * @param endRange
     * @param sortOrder
     * @param sortField
	 */
    public WSFoundationCollection getDeviceByString(
            String type, 
            String value,
            String startRange, 
            String endRange, 
            String sortOrder, 
            String sortField) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        // Do conversion then delegate
        DeviceQueryType queryType = org.groundwork.foundation.ws.model.impl.DeviceQueryType.ALL;
        
        if (type != null) 
        {
            queryType = org.groundwork.foundation.ws.model.impl.DeviceQueryType.fromValue(type);
        }
        
        int intStartRange = 0;
        int intEndRange = 0;
        
        if (startRange != null && startRange.length() > 0)
        {
            try {
                intStartRange = Integer.parseInt(startRange);
            }
            catch (Exception e) {} // Suppress and just use default value
        }
        
        if (endRange != null && endRange.length() > 0)
        {
            try {
                intEndRange = Integer.parseInt(endRange);
            }
            catch (Exception e) {} // Suppress and just use default value
        }
        
        SortCriteria sortCriteria = null;
        if (sortOrder != null && sortOrder.trim().length() > 0 &&
            sortField != null && sortField.trim().length() > 0)
        {
            sortCriteria = new SortCriteria(sortOrder, sortField);
        }
        
        WSFoundationCollection device = getDevice(queryType, value, intStartRange, intEndRange, sortCriteria);
        stopMetricsTimer(timer);
        return device;
    }    
        
    public WSFoundationCollection getDevicesByCriteria(Filter filter, Sort sort, int firstResult, int maxResults)
    throws RemoteException, WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
    	try {
	    	CollageConvert converter = getConverter();
	    	
	    	FilterCriteria filterCriteria = converter.convert(filter);
	    	org.groundwork.foundation.dao.SortCriteria sortCriteria = converter.convert(sort);
	
	    	FoundationQueryList list = getDeviceService().getDevices(filterCriteria, sortCriteria, firstResult, maxResults);
	    	
	    	return new WSFoundationCollection(list.getTotalCount(),
	    			converter.convertDevice((Collection<Device>)list.getResults()));    	
    	}
    	catch (Exception e)
    	{
        	log.error("Error occurred in getDevicesByCriteria()", e);
        	throw new WSFoundationException("Error occurred in getDevicesByCriteria() - " + e,
        			ExceptionType.WEBSERVICE);    		
    	} finally {
    	    stopMetricsTimer(timer);
        }
    }   
        
    /*
     * Gets the device specified by DeviceID
     */
    private WSFoundationCollection getDeviceByID(String value) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        try
        {
            Device device = getDeviceService().getDeviceById(Integer.valueOf(value));
            if (device == null)
            	throw new CollageException("Unable to retrieve device by id.  Device not found - " + value);
 
            return new WSFoundationCollection(1,
            		new org.groundwork.foundation.ws.model.impl.Device[] {getConverter().convert(device)});            
        }
        catch (Exception e) 
        {
            throw new WSFoundationException("Error occurred in getDeviceByID() - " + e, ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
   
    /*
     * Gets the device specified by Identification
     */
    private WSFoundationCollection getDeviceByIdentification(String value) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        try
        {
            Device device = getDeviceService().getDeviceByIdentification(value);
            if (device == null)
            	throw new CollageException("Unable to retrieve device by identification.  Device not found - " + value);
            
            return new WSFoundationCollection(1,
            		new org.groundwork.foundation.ws.model.impl.Device[] {getConverter().convert(device)});            
        }
        catch (CollageException e) 
        {
            throw new WSFoundationException("Error occurred in getDeviceByIdentification() - " + e, ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
   
    /*
     * Gets the children of the specified device.
     */
    private WSFoundationCollection getDeviceChildren(String value, int fromRange, int toRange, SortCriteria orderedBy) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        try
        {
            Device device = getDeviceService().getDeviceByIdentification(value);
            if (device == null)
            	throw new CollageException("Unable to retrieve device children.  Device not found - " + value);
            
            Collection<Device> children = (Collection<Device>)device.getChildren();
            	            
            return new WSFoundationCollection(children.size(), getConverter().convertDevice(children));        
        }
        catch (CollageException e) 
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
   
    /*
     * Gets all devices
     */
    private WSFoundationCollection getDevices(int fromRange, int maxResults, SortCriteria orderedBy) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        try
        {
        	// NOTE:  SortCriteria is not used - This method will be removed and replaced
        	// with getDevicesByCriteria()
            FoundationQueryList list = getDeviceService().getDevices(null, null, fromRange, maxResults);

            return new WSFoundationCollection(list.getTotalCount(), 
            		getConverter().convertDevice((Collection<Device>)list.getResults()));        
        }
        catch (CollageException e) 
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
   
    /*
     * Gets the device for the HostID specified
     */
    private WSFoundationCollection getDeviceForHostID(String value) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        try
        {
            Device device = getDeviceService().getDeviceByHostId(Integer.valueOf(value));
            
            if (device == null)
            	throw new CollageException("Unable to retrieve for host - " + value);
            
            return new WSFoundationCollection(1, 
            		new org.groundwork.foundation.ws.model.impl.Device[] {getConverter().convert(device)});      
        }
        catch (CollageException e) 
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
   
    /*
     * Gets the device for the HostName specified.
     */
    private WSFoundationCollection getDeviceForHostName(String value) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        try
        {
            Device device = getDeviceService().getDeviceByHostName(value);
            
            if (device == null)
            	throw new CollageException("Unable to retrieve for host - " + value);
            
            return new WSFoundationCollection(1, 
            		new org.groundwork.foundation.ws.model.impl.Device[] {getConverter().convert(device)});      
        }
        catch (CollageException e) 
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
   
    /*
     * Gets the parents of the specified device.
     */
    private WSFoundationCollection getDeviceParents(String value) throws WSFoundationException
    {
        CollageTimer timer = startMetricsTimer();
        try
        {
            Device device = getDeviceService().getDeviceByIdentification(value);
            if (device == null)
            	throw new CollageException("Unable to retrieve device parents.  Device not found - " + value);
            
            Collection<Device> parents = (Collection<Device>)device.getParents();
            	            
            return new WSFoundationCollection(parents.size(), getConverter().convertDevice(parents));        
        }
        catch (CollageException e) 
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }
}
