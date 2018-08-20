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
import org.groundwork.foundation.ws.api.WSHostGroup;
import org.groundwork.foundation.ws.model.HostGroupQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;

public class HostgroupSoapBindingImpl implements WSHostGroup
{
    /* (non-Javadoc)
     * @see org.groundwork.foundation.ws.api.WSHostGroup#getHostGroups(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getHostGroupsByString(String type, String value, String applicationType, String deep, String fromRange, String toRange, String sortOrder, String sortField) throws RemoteException, WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSHostGroup hostgroup = (WSHostGroup) factory.getAPIObject("WSHostGroup");
        
        // check the event object, if getting it failed, bail out now.
        if (hostgroup == null) {
            throw new WSFoundationException("Unable to create WSHostGroup instance", ExceptionType.SYSTEM );
        }
        // all is well, call our implementation.
        else {
           return hostgroup.getHostGroupsByString(type, value, applicationType, deep, fromRange, toRange, sortOrder, sortField);
        }
    }
    
    public WSFoundationCollection getHostGroups(HostGroupQueryType hostGroupQueryType,
            String value, String applicationType, boolean deep,  int fromRange, int toRange, SortCriteria orderedBy) throws RemoteException, WSFoundationException 
    {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSHostGroup hostgroup = (WSHostGroup) factory.getAPIObject("WSHostGroup");
        
        // check the event object, if getting it failed, bail out now.
        if (hostgroup == null) {
            throw new WSFoundationException("Unable to create WSHostGroup instance", ExceptionType.SYSTEM );
        }
        // all is well, call our implementation.
        else {
           return hostgroup.getHostGroups(hostGroupQueryType, value, applicationType, deep, fromRange, toRange, orderedBy);
        }
    }
    
    public WSFoundationCollection getHostGroupInfo(String type, String value) throws RemoteException, WSFoundationException 
    {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSHostGroup hostgroup = (WSHostGroup) factory.getAPIObject("WSHostGroup");
        
        // check the event object, if getting it failed, bail out now.
        if (hostgroup == null) {
            throw new WSFoundationException("Unable to create WSHostGroup instance", ExceptionType.SYSTEM );
        }
        // all is well, call our implementation.
        else {
           return hostgroup.getHostGroupInfo(type, value);
        }
    }    

    public WSFoundationCollection getHostGroupsByCriteria(Filter filter, Sort sort, int firstResult, int maxResults, boolean bDeep)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSHostGroup service = (WSHostGroup) factory.getAPIObject("WSHostGroup");
		
		// check the event object, if getting it failed, bail out now.
		if (service == null) {
		    throw new WSFoundationException("Unable to create WSHostGroup instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return service.getHostGroupsByCriteria(filter, sort, firstResult, maxResults, bDeep);
		}
    }    
}
