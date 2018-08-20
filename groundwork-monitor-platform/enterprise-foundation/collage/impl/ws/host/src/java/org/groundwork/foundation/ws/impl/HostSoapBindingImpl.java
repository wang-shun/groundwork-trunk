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

import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSHost;
import org.groundwork.foundation.ws.model.HostQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;

public class HostSoapBindingImpl implements WSHost
{
    /* (non-Javadoc)
     * @see org.groundwork.foundation.ws.api.WSHost#getHosts(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getHostsByString(String type, String value, String applicationType, String fromRange, String toRange, String sortOrder, String sortField) throws RemoteException, WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSHost host = (WSHost) factory.getAPIObject("WSHost");
        
        // check the event object, if getting it failed, bail out now.
        if (host == null) {
            throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return host.getHostsByString(type, value, applicationType, fromRange, toRange, sortOrder, sortField);
        }
    }

    public WSFoundationCollection getHosts(HostQueryType hostQueryType, String value, String applicationType, int fromRange, int toRange, SortCriteria orderedBy) throws RemoteException, WSFoundationException 
    {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSHost host = (WSHost) factory.getAPIObject("WSHost");
        
        // check the event object, if getting it failed, bail out now.
        if (host == null) {
            throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return host.getHosts(hostQueryType, value, applicationType, fromRange, toRange,  orderedBy);
        }
    }
    
    public WSFoundationCollection hostLookup(String hostName) throws RemoteException, WSFoundationException
    {
    	 // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSHost host = (WSHost) factory.getAPIObject("WSHost");
        
        // check the event object, if getting it failed, bail out now.
        if (host == null) {
            throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return host.hostLookup(hostName);
        }
    }
    
    public WSFoundationCollection getHostsByCriteria(Filter filter, Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSHost host = (WSHost) factory.getAPIObject("WSHost");
		
		// check the event object, if getting it failed, bail out now.
		if (host == null) {
		    throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return host.getHostsByCriteria(filter, sort, firstResult, maxResults);
		}
    }
    
    public WSFoundationCollection getHostList()  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSHost host = (WSHost) factory.getAPIObject("WSHost");
		
		// check the event object, if getting it failed, bail out now.
		if (host == null) {
		    throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return host.getHostList();
		}
    }
    
    
    public WSFoundationCollection getSimpleHosts()  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSHost host = (WSHost) factory.getAPIObject("WSHost");
		
		// check the event object, if getting it failed, bail out now.
		if (host == null) {
		    throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return host.getSimpleHosts();
		}
    }
    
    public WSFoundationCollection getSimpleHostsByHostGroupName(String hostGroupName,boolean deep)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSHost host = (WSHost) factory.getAPIObject("WSHost");
		
		// check the event object, if getting it failed, bail out now.
		if (host == null) {
		    throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return host.getSimpleHostsByHostGroupName(hostGroupName, deep);
		}
    }
    
    public WSFoundationCollection getSimpleHostByCriteria(Filter filter,Sort sort, int firstResult, int maxResults, boolean deep)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSHost host = (WSHost) factory.getAPIObject("WSHost");
		
		// check the event object, if getting it failed, bail out now.
		if (host == null) {
		    throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return host.getSimpleHostByCriteria(filter, sort, firstResult, maxResults, deep);
		}
    }
    /**
	 * Gets Lightweight Host and service information.Does not return dynamic
	 * properties
	 * 
	 * @return WSFoundationCollection(String[])
	 * @throws RemoteException
	 * @throws WSFoundationException
	 */
	public WSFoundationCollection getSimpleHost(String hostName, boolean deep)
			throws RemoteException, WSFoundationException {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSHost host = (WSHost) factory.getAPIObject("WSHost");
		
		// check the event object, if getting it failed, bail out now.
		if (host == null) {
		    throw new WSFoundationException("Unable to create WSHost instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return host.getSimpleHost(hostName, deep);
		}
	}

}
