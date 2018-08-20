/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2005  GroundWork Open Source Solutions info@itgroundwork.com

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

import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.feeder.adapter.FeederBase;

/**
 * Syslog adapter
 * @author rogerrut
 * 
 * For the SNMP and Syslog, the following rules will be applied:
 *
 * 1. Use the IP address to try to do DNS lookup the host name. If I find the host name, I will set that to the Host and Device field.
 * 2. If there is no host name, I will look at Monarch database to try to find the host name. I will set that to the Host and Device field.
 * 3. If Monarch does not have the host name, I will set the IP address to the Host and Device field.
 *
 */
public class Syslog implements FeederBase {
	
	/**
     * Adapter for Syslog messages
     */
    private static String ADAPTER_NAME="SYSLOG";
    
    /** Utils library*/
    private AdapterUtil utils = new AdapterUtil();
    
    // Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    // Monitoring fields
    private final String MONITOR_SERVER = "MonitorServerName";   
    private final String HOST = "Host";
    private final String SEVERITY = "Severity"; 
    private final String MONITOR_STATUS = "MonitorStatus";
    private final String REPORT_DATE = "ReportDate";
    private final String LAST_INSERT_DATE = "LastInsertDate";
    private final String Device = "Device";
    private final String IPADDRESS = "ipaddress";
    private final String TEXT_MESSGE = "TextMessage";
    private final String ERROR_TYPE = "ErrorType";
    private final String SUBCOMPONENT = "SubComponent";
    private final String FIRST_INSERT_DATE = "FirstInsertDate";
    	
	public String getName() {
		return ADAPTER_NAME ;
	}
	
	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void uninitialize() {
		// TODO Auto-generated method stub

	}

	public void process(Object beanFactory, String xmlStream) {
		// Extract attributes
        Hashtable attributes = this.utils.getAttributes(xmlStream);
        if (attributes == null)
        {
            log.error("SYSLOG Adapter : Could not find attributes in xml " + xmlStream);
            throw new CollageException("Error in SYSLOG Adapter. Could not find attributes in xml " + xmlStream);
        }
        
        // Call into collage API updateNagiosLog        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            System.out.println("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            throw new CollageException("Error in Adapter. CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
        }
        
        /**
         * SYSLog adapter. The required arguments are: MonitorServerName, Host, Severity, MonitorStatus, ReportDate, LastInsertDate, Device, ErrorType, SubComponent, TextMessage 
         */
        
        String monitorServerName = (String)attributes.get(this.MONITOR_SERVER);
        String deviceIdentification = (String)attributes.get(this.Device);
        String messageSeverity = (String)attributes.get(this.SEVERITY);
        String textMsg = (String)attributes.get(this.TEXT_MESSGE);
        
        if (monitorServerName == null || deviceIdentification ==null || messageSeverity == null || textMsg == null )
        {
            throw new CollageException("Error in Syslog Adapter. One or more required properties are missing. Make sure to specify: MonitorServerName, Device and Severity in your request. Current request [" + xmlStream + "]");
        }
        
        Properties props = new Properties();
        
        /* Check required properties */
        String property = (String)attributes.get(this.HOST);
        if (property == null)
        	throw new CollageException("Error in Syslog Adapter. Property Host is missing. Make sure to specify a valid value for the property.");
        else
        	props.setProperty( LogMessage.KEY_HOST_NAME,  property);
        
        property = (String)attributes.get(this.MONITOR_STATUS);
        if (property == null)
        	throw new CollageException("Error in Syslog Adapter. Property MonitorStatus is missing. Make sure to specify a valid value for the property.");
        else
        	props.setProperty( LogMessage.KEY_MONITOR_STATUS, property);
        
        property = (String)attributes.get(this.REPORT_DATE);
        if (property == null)
        	throw new CollageException("Error in Syslog Adapter. Property ReportDate is missing. Make sure to specify a valid value for the property.");
        else
        	props.setProperty( LogMessage.KEY_REPORT_DATE, property);
        
        property = (String)attributes.get(this.LAST_INSERT_DATE);
        if (property == null)
        	throw new CollageException("Error in Syslog Adapter. Property LastInsertDate is missing. Make sure to specify a valid value for the property.");
        else
        	props.setProperty( LogMessage.KEY_LAST_INSERT_DATE, property);
        
        /*Optional*/
        property = (String)attributes.get(this.FIRST_INSERT_DATE);
        if (property != null)
        	props.setProperty( LogMessage.KEY_FIRST_INSERT_DATE, property);
 
        /* SysLog Properties */
        property = (String)attributes.get(this.ERROR_TYPE);
        if (property == null)
        	throw new CollageException("Error in Syslog Adapter. Property ErrorType is missing. Make sure to specify a valid value for the property.");
        else
        	props.setProperty( "ErrorType", property);
        
        property = (String)attributes.get(this.SUBCOMPONENT);
        if (property == null)
        	throw new CollageException("Error in Syslog Adapter. Property SubComponent is missing. Make sure to specify a valid value for the property.");
        else
        	props.setProperty( "SubComponent", property);
        
        property = (String)attributes.get(this.IPADDRESS);
        if (property == null)
        	throw new CollageException("Error in Syslog Adapter. Property ipaddress is missing. Make sure to specify a valid value for the property.");
        else
        	props.setProperty( "ipaddress", property);
        
 		// process consolidation
		String consolidationName = (String)attributes.get(LogMessage.CONSOLIDATION);
		if ( consolidationName != null && consolidationName.length() > 0)
			props.setProperty( LogMessage.CONSOLIDATION, (String)attributes.get(LogMessage.CONSOLIDATION));
        
		try
		{
			admin.updateLogMessage(monitorServerName,"SYSLOG",deviceIdentification, messageSeverity.toUpperCase(), textMsg, props);
		}
		catch(CollageException ce)
		{
			log.error("Error while calling into Collage Admin API: Error " + ce);
		}
	}
}
