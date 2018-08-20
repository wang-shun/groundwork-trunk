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
package org.groundwork.report.birt.data.oda.ws.impl;

import java.util.Properties;

import javax.xml.rpc.ServiceException;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.impl.WSCommonServiceLocator;
import org.groundwork.foundation.ws.model.impl.AttributeData;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.EntityType;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

/**
 * Implementation class of IConnection for an ODA runtime driver.
 */
public class Connection implements IConnection
{	
	private static final String WS_COMMON_PORT = "wscommon";
	private static final String END_POINT_URI = "/foundation-webapp/services/" + WS_COMMON_PORT;
	
	private static final String DEFAULT_SERVER_URL = "http://localhost:8080";
	private static final String DEFAULT_END_POINT = DEFAULT_SERVER_URL + END_POINT_URI;
	private static final String FWD_SLASH = "/";
	private static final String REGEX_URL = "https?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?";
	
	// URL of foundation web service - DEFAULT:  http://localhost:8080/foundation-webapp/services/
    private String endPoint = DEFAULT_END_POINT;
        
    private boolean isOpen = false;
    
    // Common Web Service
    private WSCommon service = null;

    // Connection Properties
    public static final String PROP_SERVER_URL = "GW_SERVER_URL";
    
	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#open(java.util.Properties)
	 */
	public void open( Properties connProperties ) throws OdaException
	{
        if ( connProperties == null )
            throw new OdaException("Connection Properties Missing.");

        String serverURL = connProperties.getProperty( PROP_SERVER_URL );
        
        if (serverURL == null || serverURL.length() == 0)
        {
            Driver.LogWarning("No Foundation server url defined - End Point Defaulting To: " + DEFAULT_END_POINT);
            this.endPoint = DEFAULT_END_POINT;
        }
        else 
        {
        	// Validate/fix server url        	
        	if (serverURL.matches(REGEX_URL) == false)
        	{
        		Driver.LogWarning("Invalid Foundation server url defined - " + serverURL);
                throw new OdaException("Invalid Foundation server url defined - " + serverURL);
        	}
        	
        	// Remove forward slash if necessary
    		int lastIndex = serverURL.length() - 1;
        	if (serverURL.lastIndexOf(FWD_SLASH) == lastIndex)
        	{
        		serverURL = serverURL.substring(0, lastIndex);
        	}
        	
        	this.endPoint = serverURL + END_POINT_URI;
        }
                
        // See if web service exists
        try {
            this.service = loadService(this.endPoint);      
        }
        catch (OdaException odaEx)
        {
            Driver.LogError("Exception occurred loading web services", odaEx);                       
            throw odaEx;
        }
        catch (Exception ex) 
        {
            Driver.LogError("Exception occurred loading web services metadata", ex);
            throw new OdaException("Error occurred loading web services metadata.  Check Log for information.");
        }
        
        this.isOpen = true;    
 	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#setAppContext(java.lang.Object)
	 */
	public void setAppContext( Object context ) throws OdaException
	{
	    // do nothing; assumes no support for pass-through context
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#close()
	 */
	public void close() throws OdaException
	{
        this.endPoint = null;
        this.isOpen = false;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#isOpen()
	 */
	public boolean isOpen() throws OdaException
	{
		return isOpen;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMetaData(java.lang.String)
	 */
	public IDataSetMetaData getMetaData( String dataSetType ) throws OdaException
	{
	    // assumes that this driver supports only one type of data set,
        // ignores the specified dataSetType
		return new DataSetMetaData( this );
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#newQuery(java.lang.String)
	 */
	public IQuery newQuery( String dataSetType ) throws OdaException
	{
        if (this.isOpen == false )
            throw new OdaException("Connection is not opened.");

        return new Query(this);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMaxQueries()
	 */
	public int getMaxQueries() throws OdaException
	{
		return 0;	// no limit
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#commit()
	 */
	public void commit() throws OdaException
	{
	    // do nothing; assumes no transaction support needed
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#rollback()
	 */
	public void rollback() throws OdaException
	{
        // do nothing; assumes no transaction support needed
	}
 
    public WSCommon getService ()
    {
        return this.service;
    }
    
    public String[] getApplicationTypes () throws OdaException
    {
        try 
        {
	    	WSCommon wsCommon = getService();
	    	
	    	WSFoundationCollection wsfndCollection = wsCommon.getAttributeData(AttributeQueryType.APPLICATION_TYPES);
	    	if (wsfndCollection == null)
	    	{
	    		Driver.LogWarning("No application types returned from getApplicationTypes().");
	    		return null;
	    	}
	    	
	    	AttributeData[] appTypes = wsfndCollection.getAttributeData();
	    	
	    	if (appTypes == null)
	    		return null;
	    	
	    	String[] typeNames = new String[appTypes.length];
	    	for (int i = 0; i < appTypes.length; i++)
	    	{
	    		typeNames[i] = appTypes[i].getName();
	    	}
	    	
	    	return typeNames;
        }
        catch (Exception e)
        {
        	Driver.LogError("Error retrieving application types.", e);  
        	throw new OdaException("Unable to retrieve application types -" + e.toString());
        }        
    }
    
    public String[] getEntityTypes () throws OdaException
    {
        try 
        {
	    	WSCommon wsCommon = getService();
	    	
	    	WSFoundationCollection wsfndCollection = wsCommon.getEntityTypes();
	    	if (wsfndCollection == null)
	    	{
	    		Driver.LogWarning("No entity types returned from getEntityTypes().");
	    		return null;
	    	}
	    	
	    	EntityType[] entityTypes = wsfndCollection.getEntityType();
	    	
	    	if (entityTypes == null)
	    		return null;
	    	
	    	String[] typeNames = new String[entityTypes.length];
	    	for (int i = 0; i < entityTypes.length; i++)
	    	{
	    		typeNames[i] = entityTypes[i].getName();
	    	}
	    	
	    	return typeNames;
        }
        catch (Exception e)
        {
        	Driver.LogError("Error retrieving entity types.", e);  
        	throw new OdaException("Unable to retrieve entity types -" + e.toString());
        }        
    }
    
    public EntityTypeProperty[] getEntityProperties (String appType, String entityType) throws OdaException
    {
    	if (entityType == null || entityType.length() == 0)
    		throw new IllegalArgumentException("Null / empty entity type name parameter.");
    	
        try 
        {
	    	WSCommon wsCommon = getService();
	    	
	    	WSFoundationCollection wsfndCollection = wsCommon.getEntityTypeProperties(entityType, null, true);
	    	if (wsfndCollection == null)
	    	{
	    		Driver.LogWarning("No entity type properties returned from getEntityProperties().");
	    		return null;
	    	}
	    	
	    	return wsfndCollection.getEntityTypeProperty();	    	
        }
        catch (Exception e)
        {
        	Driver.LogError("Error retrieving entity type properties.", e);  
        	throw new OdaException("Unable to retrieve entity type properties -" + e.toString());
        }        
    }    
    
    private WSCommon loadService (String endPoint)
    throws IllegalArgumentException, ServiceException, OdaException
    {
        if (endPoint == null || endPoint.length() == 0)
        {
            endPoint = DEFAULT_END_POINT;
        }       
        
        try 
        {
        	WSCommonServiceLocator locator = new WSCommonServiceLocator();

            locator.setEndpointAddress(WS_COMMON_PORT, endPoint);
            
            return locator.getcommon();
                    
        }
        catch (Exception e)
        {
            throw new OdaException(
                    "Error occurred instantiating the common service locator -" 
                        + " End Point: "
                        + endPoint 
                        + e.toString());
        }            
    }       
}
