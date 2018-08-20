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


package com.groundwork.feeder.adapter.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.groundwork.collage.model.LogMessage;
import com.groundwork.feeder.adapter.FeederBase;

public class GenericLog implements FeederBase {
	
	/**
     * Adapter for GenericLog entries
     */
    private static String ADAPTER_NAME="GENERICLOG";
    
    // Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    // Monitoring fields
    private final String APPLICATION_TYPE = "ApplicationType";
    private final String MONITOR_SERVER = "MonitorServerName";   
    private final String Device = "Device";
    private final String SEVERITY = "Severity";
    private final String TEXT_MESSAGE = "TextMessage";

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
    		log.info("GenericLog: Null FoundationMessage.");
    		return;
    	}
    	
        // Extract attributes
        List<Hashtable<String, String>> listAttributes = message.getAttributes();
        
        if (listAttributes == null || listAttributes.size() == 0)
        {
            log.info("GenericLog: Could not find attributes in xml " + message);
            return;
        }
        
        Map<String, String> attributes = listAttributes.get(0);
        
        // Call into collage API updateNagiosLog        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            log.info("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            throw new CollageException("Error in Adapter. CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
        }
        
        /**
         * Generic log message adapter. The required arguments are: MonitorServer, ApplicationType, Severity, MonitorStatus, Device, TextMessage
         * Optional arguments: Host, ReportDate, LastInsertDate, OperationStatus, ApplicationSeverity, Component, Priority, ServiceDescription, TypeRule, PRIORITY
         * 
         */
        String monitorServerName = (String)attributes.get(this.MONITOR_SERVER);
        String applicationType = (String) attributes.get(this.APPLICATION_TYPE);
        String deviceIdentification = (String)attributes.get(this.Device);
        String messageSeverity = (String)attributes.get(this.SEVERITY);
        String textMsg = (String)attributes.get(this.TEXT_MESSAGE);
        
        
        /*required fields */
        if (applicationType == null || monitorServerName == null || deviceIdentification ==null || messageSeverity == null || textMsg == null )
        {
            throw new CollageException("Error in GenericLog Adapter. One or more required properties are missing. Make sure to specify: ApplicationType, MonitorServerName, Device and Severity in your request. Current request [" + message + "]");
        }
        
        // Remove required properties from attribute list
        attributes.remove(this.MONITOR_SERVER);
        attributes.remove(this.APPLICATION_TYPE);
        attributes.remove(this.Device);
        attributes.remove(this.SEVERITY);
        attributes.remove(this.TEXT_MESSAGE);
        
        Properties props = new Properties();
        
        try
        {
            /* 
             * Optional fields with defaults
             */
        	String property = (String)attributes.get(LogMessage.EP_MONITOR_STATUS_NAME);
	        if (property != null)
	        {
	        	props.setProperty( LogMessage.EP_MONITOR_STATUS_NAME,    property  );
	        	attributes.remove(LogMessage.EP_MONITOR_STATUS_NAME);
	        }
	        else
	        	props.setProperty( LogMessage.EP_MONITOR_STATUS_NAME,    "UNKNOWN"  );
			
	        property = (String)attributes.get(LogMessage.EP_REPORT_DATE);
	        if (property != null)
	        {
	        	props.setProperty( LogMessage.EP_REPORT_DATE,    property     );
	        	attributes.remove(LogMessage.EP_REPORT_DATE);
	        }
	        else
	        {
	        	// Use current
	        	SimpleDateFormat reportDate = new SimpleDateFormat();
	            reportDate.applyPattern("yyyy-MM-dd HH:mm:ss");
	            
	            props.setProperty( LogMessage.EP_REPORT_DATE, reportDate.format(new Date(System.currentTimeMillis())));
	        }
	        
	        property = (String)attributes.get(LogMessage.EP_OPERATION_STATUS_NAME);	
			// Default to open
			if (property == null )
			{
				props.setProperty(LogMessage.EP_OPERATION_STATUS_NAME, "OPEN");	
			}
			else
			{
				props.setProperty( LogMessage.EP_OPERATION_STATUS_NAME, property);
				attributes.remove(LogMessage.EP_OPERATION_STATUS_NAME);
			}
	        
			// For text Message.Adding text message.JIRA 5634
			property = (String)attributes.get(LogMessage.EP_TEXT_MESSAGE);
	        if (property != null)
	        {
	        	props.setProperty( LogMessage.EP_TEXT_MESSAGE,    property  );
	        	attributes.remove(LogMessage.EP_TEXT_MESSAGE);
	        }
	        /*else
	        	props.setProperty( LogMessage.EP_TEXT_MESSAGE,    "NA"  );*/

	        //			 process consolidation
			String consolidationName = (String)attributes.get(LogMessage.KEY_CONSOLIDATION);
			if ( consolidationName != null && consolidationName.length() > 0)
			{
				props.setProperty( LogMessage.KEY_CONSOLIDATION, (String)attributes.get(LogMessage.KEY_CONSOLIDATION));
				attributes.remove(LogMessage.KEY_CONSOLIDATION);
			}
			
			
			
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
				key = itAttributes.next();
				if (key != null && key.length() > 0)
				{
					attribute = (String)attributes.get(key);
					if (attribute != null && attribute.length() > 0)
					{
						props.setProperty( key, attribute);
						if (log.isInfoEnabled())
						{
							logMsg.append("GenericLogAdapter. Add property [").append(key).append("] with value [").append(attribute).append("] to request.");
							log.info(logMsg);
						}
					}
				}
			}
			
	        /*
	        property = (String)attributes.get(LogMessage.KEY_HOST_NAME);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_HOST_NAME,           (String)attributes.get(LogMessage.KEY_HOST_NAME));
			
	        property = (String)attributes.get(LogMessage.KEY_LAST_INSERT_DATE);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_LAST_INSERT_DATE,   property );
			
			property = (String)attributes.get(LogMessage.KEY_APPLICATION_SEVERITY);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_APPLICATION_SEVERITY,  property          );
	        
	        property = (String)attributes.get(LogMessage.KEY_COMPONENT);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_COMPONENT,     property      );
	        
	        property = (String)attributes.get(LogMessage.KEY_PRIORITY);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_PRIORITY, property          );
	        
	        property = (String)attributes.get(LogMessage.KEY_SERVICE_DESCRIPTION);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_SERVICE_DESCRIPTION,        property   );
	        
	        property = (String)attributes.get(LogMessage.KEY_TYPE_RULE);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_TYPE_RULE,      property     );
	        
	        property = (String)attributes.get(LogMessage.KEY_PRIORITY);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_PRIORITY,         property  );
	        
	        property = (String)attributes.get(LogMessage.KEY_FIRST_INSERT_DATE);
	        if (property != null)
	        	props.setProperty( LogMessage.KEY_FIRST_INSERT_DATE,   property );
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
			admin.updateLogMessage(monitorServerName,applicationType,deviceIdentification, messageSeverity.toUpperCase(), textMsg, props);
		}
		catch(CollageException ce)
		{
			String error = "Error while calling into Collage Admin API: Error " + ce;
			log.error(error);
			throw new CollageException(error);
		}
	}
}
