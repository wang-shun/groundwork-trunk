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
import org.groundwork.foundation.ws.api.WSService;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.ServiceQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;

public class ServiceSoapBindingImpl implements WSService 
{
	
	/* (non-Javadoc)
     * @see org.groundwork.foundation.ws.api.WSService#getServices(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getServicesByString(String type, String value, String applicationType, String fromRange, String toRange, String sortOrder, String sortField) throws RemoteException, WSFoundationException {
        // get the WSService api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSService service = (WSService) factory.getAPIObject("WSService");
        
        // check the event object, if getting it failed, bail out now.
        if (service == null) {
            throw new WSFoundationException("Unable to create WSService instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return service.getServicesByString(type, value, applicationType, fromRange, toRange, sortOrder, sortField);
        }
    }

       /*
    public WSFoundationCollection getServices(ServiceQueryType serviceQueryType,
            String value, String applicationType, int fromRange, int toRange, SortCriteria orderedBy) throws RemoteException, WSFoundationException 
            */
    public WSFoundationCollection getServices(ServiceQueryType serviceQueryType, String value, String appType, int startRange, int endRange, SortCriteria orderedBy) throws java.rmi.RemoteException, WSFoundationException 
    {
        // get the WSService api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSService service = (WSService) factory.getAPIObject("WSService");
        
        // check the event object, if getting it failed, bail out now.
        if (service == null) {
            throw new WSFoundationException("Unable to create WSService instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return service.getServices(serviceQueryType, value, appType, startRange, endRange, orderedBy);
        }
    }
    
    public WSFoundationCollection getServicesByCriteria(Filter filter, Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSService service = (WSService) factory.getAPIObject("WSService");
		
		// check the event object, if getting it failed, bail out now.
		if (service == null) {
		    throw new WSFoundationException("Unable to create WSService instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return service.getServicesByCriteria(filter, sort, firstResult, maxResults);
		}
    }  
    
    public WSFoundationCollection getTroubledServices(Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSService service = (WSService) factory.getAPIObject("WSService");
		
		// check the event object, if getting it failed, bail out now.
		if (service == null) {
		    throw new WSFoundationException("Unable to create WSService instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return service.getTroubledServices(sort, firstResult, maxResults);
		}
    }  
    
    public WSFoundationCollection getServiceListByHostName(String hostName)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSService service = (WSService) factory.getAPIObject("WSService");
		
		// check the event object, if getting it failed, bail out now.
		if (service == null) {
		    throw new WSFoundationException("Unable to create WSService instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return service.getServiceListByHostName(hostName);
		}
    }    
    
    /**
	 * Gets Lightweight service information.Does not return dynamic properties
	 * 
	 * @param hostName
	 * @return WSFoundationCollection(String[])
	 * @throws RemoteException
	 * @throws WSFoundationException
	 */
	public WSFoundationCollection getSimpleServiceListByHostName(String hostName)
			throws RemoteException, WSFoundationException {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSService service = (WSService) factory.getAPIObject("WSService");
		
		// check the event object, if getting it failed, bail out now.
		if (service == null) {
		    throw new WSFoundationException("Unable to create WSService instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return service.getSimpleServiceListByHostName(hostName);
		}
	}
	
	 /**
	 * Gets Lightweight service information.Does not return dynamic properties
	 * 
	 * @param hostName
	 * @return WSFoundationCollection(String[])
	 * @throws RemoteException
	 * @throws WSFoundationException
	 */
	public WSFoundationCollection getSimpleServiceListByCriteria(Filter filter,Sort sort, int firstResult, int maxResults)
			throws RemoteException, WSFoundationException {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSService service = (WSService) factory.getAPIObject("WSService");
		
		// check the event object, if getting it failed, bail out now.
		if (service == null) {
		    throw new WSFoundationException("Unable to create WSService instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return service.getSimpleServiceListByCriteria(filter,sort,firstResult,maxResults);
		}
	}
}
