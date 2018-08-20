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

/*Created on: Mar 8, 2006 */

package com.groundwork.feeder.adapter.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.feeder.adapter.FeederBase;

/**
 * @author rogerrut
 *
 */
public class GenericServiceStatus implements FeederBase {

	/**
     * Adapter for GenericLog entries
     */
    private static String ADAPTER_NAME="GENERICSERVICESTATUS";   
    
    // Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    // Monitoring fields
    private final String APPLICATION_TYPE = "ApplicationType";
    private final String MONITOR_SERVER = "MonitorServerName";   
    private final String Device = "Device";
    private final String HOST = "Host";
    private final String SERVICE_DESCRIPTION = "ServiceDescription";
    private final String MONITOR_STATUS = "MonitorStatus";
    
    private final String STATE_TYPE = "StateType";
    private final String CHECK_TYPE = "CheckType";
    private final String LAST_HARD_STATE = "LastHardState";
    
    private final String METRIC_TYPE = "MetricType";
    private final String DOMAIN = "Domain";
    private final String NEXT_CHECK_TIME = "NextCheckTime";
    private final String LAST_CHECK_TIME = "LastCheckTime";
    private final String LAST_STATE_CHANGE = "LastStateChange";
    
    
	public String getName() {
		return ADAPTER_NAME ;
	}
	
	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void uninitialize() {
		// TODO Auto-generated method stub

	}

	public void process(Object beanFactory, FoundationMessage message)
	{
    	if (message == null)
    	{
    		log.debug("GENERICSERVICESTATUS: Null FoundationMessage.");
    		return;
    	}
    	
        // Extract attributes
        List<Hashtable<String, String>> listAttributes = message.getAttributes();
        
        if (listAttributes == null || listAttributes.size() == 0)
        {
            log.debug("GENERICSERVICESTATUS: Could not find attributes in xml " + message);
            return;
        }
        
        Map<String, String> attributes = listAttributes.get(0);
        
        // Call into collage API updateNagiosLog        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            log.debug("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            throw new CollageException("Error in Adapter. CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
        }
        
        /**
         * Generic Service Status message adapter. The required arguments are: MonitorServer, ApplicationType, MonitorStatus, Host, Device, ServiceDescription
         * Optional arguments: All other property Types such as MetricType,Domain, LastCheckTime,NextCheckTime and LastStateChange and custom defined.
         * 
         */
        String monitorServerName = (String)attributes.get(this.MONITOR_SERVER);
        String applicationType = (String) attributes.get(this.APPLICATION_TYPE);
        String deviceIdentification = (String)attributes.get(this.Device);
        String hostName = (String)attributes.get(this.HOST);
        
        String serviceDescription = (String)attributes.get(this.SERVICE_DESCRIPTION);
        String monitorStatus = (String)attributes.get(this.MONITOR_STATUS);
        
        /*required fields */
        if (applicationType == null || monitorServerName == null || deviceIdentification ==null || hostName == null || serviceDescription == null || monitorStatus == null)
        {
            throw new CollageException("Error in GenericServiceStatus Adapter. One or more required properties are missing. Make sure to specify: ApplicationType, MonitorServerName, Device, Host, ServiceDescription and MonitorStatus in your request. Current request [" + message + "]");
        }
        
        Properties props = new Properties();
        
        // Add the required values to the properties
        props.setProperty( this.MONITOR_STATUS,    monitorStatus  );
        props.setProperty( this.SERVICE_DESCRIPTION,   serviceDescription  );
        
        try
        {
            /* 
             * Optional fields with defaults
             * StateType, CheckType and LastHardState
             */
        	String property = (String)attributes.get(this.CHECK_TYPE);
	        if (property != null)
	        	props.setProperty( this.CHECK_TYPE,    property  );
	        else
	        	props.setProperty( this.CHECK_TYPE,    "ACTIVE"  );
			
	        property = (String)attributes.get(this.STATE_TYPE);
	        if (property != null)
	        	props.setProperty( this.STATE_TYPE,    property     );
	        else
	        {
	        	props.setProperty( this.STATE_TYPE, "SOFT");
	        }
	        
	        property = (String)attributes.get(this.LAST_HARD_STATE);	
			// Default to open
			if (property == null )
				props.setProperty(this.LAST_HARD_STATE, "OK");
			else
				props.setProperty( this.LAST_HARD_STATE, property);
	        
			/*
	         * Optional fields no default
	         *
			 * All the other attributes will be added to the properties list
			 */
			
			Iterator<String> itAttributes =attributes.keySet().iterator();
			String key = null;
			String attribute = null;
			StringBuilder logMsg = new StringBuilder();
			
			while (itAttributes.hasNext())
			{
				key = (String)itAttributes.next();
				if (key != null && key.length() > 0)
				{
					attribute = (String)attributes.get(key);
					if (attribute != null && attribute.length() > 0)
					{
						props.setProperty( key, attribute);
						if (log.isInfoEnabled())
						{
							logMsg.append("GenericServiceStatusAdapter. Add property [").append(key).append("] with value [").append(attribute).append("] to request.");
							log.info(logMsg);
						}
					}
				}
			}
			
	        /*
	         * Optional fields no default
	         * MetricType,Domain, LastCheckTime,NextCheckTime and LastStateChange
	         */
			
	        /*
	        property = (String)attributes.get(this.METRIC_TYPE);
	        if (property != null)
	        	props.setProperty( this.METRIC_TYPE, property);
			
	        property = (String)attributes.get(this.DOMAIN);
	        if (property != null)
	        	props.setProperty( this.DOMAIN,   property );
			
			property = (String)attributes.get(this.LAST_CHECK_TIME);
	        if (property != null)
	        	props.setProperty( this.LAST_CHECK_TIME,  property          );
	        
	        property = (String)attributes.get(this.NEXT_CHECK_TIME);
	        if (property != null)
	        	props.setProperty( this.NEXT_CHECK_TIME,     property      );
	        
	        property = (String)attributes.get(this.LAST_STATE_CHANGE);
	        if (property != null)
	        	props.setProperty( this.LAST_STATE_CHANGE, property          );
	        */
				        
        }
        catch (Exception e)
        {
        	String error = "Exception while processing the properties. Error: " + e;
        	log.error(error);
        	
        	throw new CollageException(error);
        }
        
		try
		{
			admin.updateServiceStatus(monitorServerName, applicationType, hostName, deviceIdentification,props);
		}
		catch(CollageException ce)
		{
			String error = "Error while calling into Collage Admin API: Error " + ce;
			log.error(error);
			throw new CollageException(error);
		}
	}
}
