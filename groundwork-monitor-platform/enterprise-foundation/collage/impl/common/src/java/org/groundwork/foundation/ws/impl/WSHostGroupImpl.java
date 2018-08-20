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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.groundwork.collage.metrics.CollageTimer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSHostGroup;
import org.groundwork.foundation.ws.model.HostGroupQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.HostGroupInfo;
import org.groundwork.foundation.ws.model.impl.HostGroupInfoQueryType;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.HostGroup;

/**
 * WebServiec Implementation for WSHost interface
 * @author rogerrut
 *
 */
public class WSHostGroupImpl extends WebServiceImpl implements WSHostGroup {
	
	/* Enable logging */
	protected static Log log = LogFactory.getLog(WSHostGroupImpl.class);
	
	protected final static String ALL = "ALL";
	
	public WSHostGroupImpl() 
	{  
	}

	public WSFoundationCollection getHostGroups(HostGroupQueryType hostGroupQueryType,
			String value, 
			String applicationType, 
			boolean deep,  
			int fromRange, 
			int toRange, 
			SortCriteria orderedBy) 
	throws RemoteException, WSFoundationException
	{
	    CollageTimer timer = startMetricsTimer();
		if (log.isInfoEnabled())
		{
			log.info("WSHostGroup Web Service -- Input: Deep[" + deep +"]");			
		}
		
		WSFoundationCollection hostGroups = null;

        // check first for null type and if so, return all Hosts
        if (hostGroupQueryType == null)
        {
        	log.error("Invalid HostGroupQueryType specified in getHostGroups");
            throw new WSFoundationException("Invalid HostGroupQueryType specified in getHostGroups", ExceptionType.WEBSERVICE);
        }
        
        try {
	        if (hostGroupQueryType.getValue().compareToIgnoreCase(HostGroupQueryType._ALL) == 0) 
	            hostGroups = getHostGroups(applicationType, fromRange, toRange, deep);
	        else if (hostGroupQueryType.getValue().compareToIgnoreCase(HostGroupQueryType._MONITORSERVERNAME) == 0) 
	            hostGroups = getHostGroupsForMonitorServer(value);
	        else if (hostGroupQueryType.getValue().compareToIgnoreCase(HostGroupQueryType._HOSTGROUPNAME) == 0) 
	            hostGroups = getHostGroupByName(value,deep);
	        else if (hostGroupQueryType.getValue().compareToIgnoreCase(HostGroupQueryType._HOSTGROUPID) == 0) 
	            hostGroups = getHostGroupByID(value, deep);
	        else
	        	throw new WSFoundationException("Invalid HostGroupQueryType specified in getHostGroups", ExceptionType.WEBSERVICE);       
        }
        catch (WSFoundationException wsfe)
        {
        	log.error("Error occurred in getHostGroups()", wsfe);
        	throw wsfe;
        }
        catch (Exception e)
        {
        	log.error("Error occurred in getHostGroups()", e);
        	throw new WSFoundationException("Error occurred in getHostGroups() - " + e, ExceptionType.WEBSERVICE);        	
        }

		stopMetricsTimer(timer);
        return hostGroups;
	}

    /**
     * String parameter version of getHostGroups()
     */
    public WSFoundationCollection getHostGroupsByString(String type, String value, String applicationType, String deep, String fromRange, String toRange, String sortOrder, String sortField) 
    throws WSFoundationException, RemoteException
    {
		CollageTimer timer = startMetricsTimer();
        // Do parameter conversion then delegate
        org.groundwork.foundation.ws.model.impl.HostGroupQueryType queryType = 
            org.groundwork.foundation.ws.model.impl.HostGroupQueryType.ALL;
        
        if (type != null) 
        {
            queryType = org.groundwork.foundation.ws.model.impl.HostGroupQueryType.fromValue(type);
        }
        
        boolean bDeep = Boolean.parseBoolean(deep);
        
        int intFromRange = 0;
        int intToRange = 0;
        
        if (fromRange != null && fromRange.length() > 0)
        {
            try {
                intFromRange = Integer.parseInt(fromRange);
            }
            catch (Exception e) {} // Suppress and just use default value
        }
        
        if (toRange != null && toRange.length() > 0)
        {
            try {
                intToRange = Integer.parseInt(toRange);
            }
            catch (Exception e) {} // Suppress and just use default value
        }
        
        SortCriteria sortCriteria = null;
        if (sortOrder != null && sortOrder.trim().length() > 0 &&
            sortField != null && sortField.trim().length() > 0)
        {
            sortCriteria = new SortCriteria(sortOrder, sortField);
        }

        WSFoundationCollection hostGroups = getHostGroups(queryType, value, applicationType, bDeep, intFromRange, intToRange, sortCriteria);
		stopMetricsTimer(timer);
        return hostGroups;
    }   
    
	public WSFoundationCollection getHostGroupInfo(String type, String value) 
	throws RemoteException, WSFoundationException 
	{
		CollageTimer timer = startMetricsTimer();
		HostGroupInfo[] infoArray = null;
	    
		try {
			// If type is null or value is null then we return all
	        if (type == null || value == null || type.equalsIgnoreCase(HostGroupInfoQueryType._ALL ) == true) {
	        	
	            FoundationQueryList list = getHostGroupService().getHostGroups(null, null, -1, -1);            
	            infoArray = convertToHostGroupInfo(list.getResults());            
	        }
	        else if (type.equalsIgnoreCase(HostGroupInfoQueryType._APPLICATIONTYPEID ) == true) 
	        {        
	        	FilterCriteria filterCriteria = FilterCriteria.eq(HostGroup.HP_APPLICATION_TYPE_ID, Integer.parseInt(value));
	        	
	        	FoundationQueryList list = getHostGroupService().getHostGroups(filterCriteria, null, -1, -1);    
	        	infoArray = convertToHostGroupInfo(list.getResults()); 
	        }
	        else if (type.equalsIgnoreCase(HostGroupInfoQueryType._HOSTGROUPID ) == true) 
	        {
	        	HostGroup hostgroup = getHostGroupService().getHostGroupById(Integer.parseInt(value));
	        	
	        	Collection colInfo = convertHostGroupToHostGroupInfo(hostgroup);
	        	
	        	if (colInfo != null)  		
	        		infoArray = (HostGroupInfo[])colInfo.toArray(new org.groundwork.foundation.ws.model.impl.HostGroupInfo[0]);
	
	        } else
	        	throw new WSFoundationException("Invalid type specified in getHostGroupInfo - " + type, ExceptionType.WEBSERVICE);       
	 
			if (log.isInfoEnabled())
			{
				log.info("Info Array being returned with element count of " + ((infoArray == null) ? "0" : infoArray.length));
			}
	        
	        // Flatten host groups information for each host and return a collection of HostGroupInfo instances
	        return new WSFoundationCollection(infoArray);
		}
		catch (Exception e)
		{
			log.error("Exception occurred in getHostGroupInfo()", e);
			throw new WSFoundationException("Exception occurred in getHostGroupInfo()" + e, ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
	}
		
    public WSFoundationCollection getHostGroupsByCriteria(Filter filter, Sort sort, int firstResult, int maxResults, boolean bDeep)  throws RemoteException, WSFoundationException
    {
		CollageTimer timer = startMetricsTimer();
    	try {
	    	FilterCriteria filterCriteria = getConverter().convert(filter);
	    	org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter().convert(sort);
	
	    	FoundationQueryList list = getHostGroupService().getHostGroups(filterCriteria, sortCriteria, firstResult, maxResults);
	    	
	    	return new WSFoundationCollection(list.getTotalCount(),
	    			getConverter().convertHostGroup((Collection<HostGroup>)list.getResults(), bDeep));    
		}
		catch (Exception e)
		{
			log.error("Exception occurred in getHostGroupsByCriteria()", e);
			throw new WSFoundationException("Exception occurred in getHostGroupsByCriteria()" + e, ExceptionType.DATABASE);
		} finally {
			stopMetricsTimer(timer);
		}
    }   
    
	private org.groundwork.foundation.ws.model.impl.HostGroupInfo[] convertToHostGroupInfo(Collection colHostGroups)
	{
		CollageTimer timer = startMetricsTimer();
		if (colHostGroups == null || colHostGroups.size() == 0)
		{
			return null;
		}
		
		List<HostGroupInfo> infoList = new ArrayList<HostGroupInfo>(10);
		
		Iterator it = colHostGroups.iterator();
		while (it.hasNext())
		{
			Collection<HostGroupInfo> colInfo = convertHostGroupToHostGroupInfo((HostGroup)it.next());
			
			if (colInfo != null)
				infoList.addAll(colInfo);
		}

		HostGroupInfo[] results = (HostGroupInfo[])infoList.toArray(new org.groundwork.foundation.ws.model.impl.HostGroupInfo[0]);
		stopMetricsTimer(timer);
		return results;
	}
	
	/**
	 * Returns an array of HostGroupInfo from a single HostGroupInstance
	 * @param hostGroup
	 * @return
	 */
	private Collection<HostGroupInfo> convertHostGroupToHostGroupInfo(HostGroup hostGroup)
	{
		CollageTimer timer = startMetricsTimer();
		if (hostGroup == null)
		{
			return null;
		}
		
		// If there are no hosts defined for the host group, we don't return anything
		Set hosts = hostGroup.getHosts();
		if (hosts == null || hosts.size() == 0)
		{
			log.info("No hosts for hostgroup - " + hostGroup.getName());
			return null;
		}		
		
		List<HostGroupInfo> infoList = new ArrayList<HostGroupInfo>(hosts.size());		
		
		Iterator it = hosts.iterator();
		while (it.hasNext())
		{
			com.groundwork.collage.model.impl.Host host = (com.groundwork.collage.model.impl.Host)it.next();
			
			if (host == null)
			{
				continue;
			}	
			
			HostGroupInfo info = new HostGroupInfo();
			
			info.setApplicationTypeID(hostGroup.getApplicationType().getApplicationTypeId());
			info.setApplicationName(hostGroup.getApplicationType().getName());
			
			info.setHostGroupID((hostGroup.getHostGroupId() == null ) ? -1 :  hostGroup.getHostGroupId().intValue());
			info.setHostGroupName(hostGroup.getName());			
			
			info.setHostID((host.getHostId() == null ) ? -1 :  host.getHostId().intValue());
			info.setHostName(host.getHostName());			
			
			infoList.add(info);		
		}

		stopMetricsTimer(timer);
		return infoList;
	}
	
    /*
     * Get all host groups 
     */
    private WSFoundationCollection getHostGroups(String appType, int startRange, int endRange, boolean deep) throws WSFoundationException
    {
		CollageTimer timer = startMetricsTimer();
        try
        {
        	FilterCriteria filterCriteria = null;
        	if (appType != null && appType.length() > 0 && appType.equalsIgnoreCase(ALL) == false)
        	{
        		filterCriteria = FilterCriteria.eq(HostGroup.HP_APPLICATION_TYPE_NAME, appType);
        	}
        	
        	FoundationQueryList list = getHostGroupService().getHostGroups(filterCriteria, null, startRange, endRange);
        	
        	return new WSFoundationCollection(list.getTotalCount(),
        			getConverter().convertHostGroup((Collection<HostGroup>)list.getResults(), deep));
        }
        catch (CollageException e)
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
			stopMetricsTimer(timer);
		}
    }
    
    /*
     * Get Host Groups for a Monitor Server
     */
    private WSFoundationCollection getHostGroupsForMonitorServer(String serverName) throws WSFoundationException
    {
        throw new WSFoundationException("Future enhancement for multiple monitor server instances - not implemented.", null);
    }
    
    /*
     * Get a HostGroup with the Name specified
     */
    private WSFoundationCollection getHostGroupByName(String hgName, boolean deep) throws WSFoundationException
    {
		CollageTimer timer = startMetricsTimer();
        try
        {        	
            HostGroup hostGroup = getHostGroupService().getHostGroupByName(hgName);
            if (hostGroup == null)
            	return new WSFoundationCollection(0, new org.groundwork.foundation.ws.model.impl.HostGroup[0]);
            
            Collection<HostGroup> hostgroups = new ArrayList<HostGroup>();
            hostgroups.add(hostGroup);
            
            return new WSFoundationCollection(1, getConverter().convertHostGroup(hostgroups, deep));
        }
        catch (CollageException e)
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
        	stopMetricsTimer(timer);
		}
    }
    
    /*
     * Get a HostGroup with the ID specified
     */
    private WSFoundationCollection getHostGroupByID(String id, boolean deep) throws WSFoundationException
    {
		CollageTimer timer = startMetricsTimer();
        try
        {        	
            HostGroup hostGroup = getHostGroupService().getHostGroupById(Integer.valueOf(id).intValue());
            if (hostGroup == null)
            	return new WSFoundationCollection(0, new org.groundwork.foundation.ws.model.impl.HostGroup[0]);
            
            Collection<HostGroup> hostgroups = new ArrayList<HostGroup>();
            hostgroups.add(hostGroup);
            
            return new WSFoundationCollection(1, getConverter().convertHostGroup(hostgroups, deep));
        }
        catch (CollageException e)
        {
            throw new WSFoundationException(e.getMessage(), ExceptionType.DATABASE);
        } finally {
        	stopMetricsTimer(timer);
		}
    }
}
