/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;

import org.groundwork.foundation.ws.api.WSDevice;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.DeviceQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;

public class DeviceSoapBindingImpl implements WSDevice
{
    public WSFoundationCollection getDevice(DeviceQueryType type, java.lang.String value, int fromRange, int toRange, SortCriteria orderedBy) 
    throws WSFoundationException, RemoteException 
    {
    	
        // get the WSDevice api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSDevice device = (WSDevice) factory.getAPIObject("WSDevice");
        
        // check the Device object, if getting it failed, bail out now.
        if (device == null) {
            throw new WSFoundationException("Unable to create WSDevice instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return device.getDevice(type, value, fromRange, toRange, orderedBy);
        }
    }

    /* (non-Javadoc)
     * @see org.groundwork.foundation.ws.api.WSDevice#getDevice(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getDeviceByString(String type, String value, String fromRange, String toRange, String sortOrder, String sortField)
    throws WSFoundationException, RemoteException 
    {
        // get the WSDevice api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSDevice device = (WSDevice) factory.getAPIObject("WSDevice");
        
        // check the Device object, if getting it failed, bail out now.
        if (device == null) {
            throw new WSFoundationException("Unable to create WSDevice instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return device.getDeviceByString(type, value, fromRange, toRange, sortOrder, sortField);
        }
    }

    public WSFoundationCollection getDevicesByCriteria(Filter filter, Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSDevice device = (WSDevice) factory.getAPIObject("WSDevice");
		
		// check the event object, if getting it failed, bail out now.
		if (device == null) {
		    throw new WSFoundationException("Unable to create WSDevice instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return device.getDevicesByCriteria(filter, sort, firstResult, maxResults);
		}
    }        
}
