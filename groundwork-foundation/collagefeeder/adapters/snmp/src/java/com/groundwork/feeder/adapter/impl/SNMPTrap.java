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
 * SNMPTrap adapter
 * 
 * * For the SNMP and Syslog, the following rules will be applied:
 *
 * 1. Use the IP address to try to do DNS lookup the host name. If I find the host name, I will set that to the Host and Device field.
 * 2. If there is no host name, I will look at Monarch database to try to find the host name. I will set that to the Host and Device field.
 * 3. If Monarch does not have the host name, I will set the IP address to the Host and Device field.
 *

 */
public class SNMPTrap implements FeederBase {
	
	/**
     * Adapter for HostAvailability entries
     */
    private static String ADAPTER_NAME="SNMPTRAP";
    
    /** Utils library*/
    private AdapterUtil utils = new AdapterUtil();
    
    // Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    // Monitoring fields
    private final String SNMP_MONITOR_SERVER = "MonitorServerName";   
    private final String SNMP_HOST = "Host";
    private final String SNMP_SEVERITY = "Severity"; 
    private final String SNMP_MONITOR_STATUS = "MonitorStatus";
    private final String SNMP_REPORT_DATE = "ReportDate";
    private final String SNMP_LAST_INSERT_DATE = "LastInsertDate";
    private final String SNMP_Device = "Device"; 
    private final String SNMP_IPADDRESS = "ipaddress";
    private final String SNMP_EVENT_OID_NUMERIC = "Event_OID_numeric"; 
    private final String SNMP_EVENT_OID_SYMBOLIC = "Event_OID_symbolic"; 
    private final String SNMP_EVENT_NAME = "Event_Name";
    private final String SNMP_CATEGORY = "Category";
    private final String SNMP_VARIABLE_BINDINGS = "Variable_Bindings"; 
    private final String SNMP_TEXT_MESSGE = "TextMessage";
    private final String SNMP_OPERATION_STATUS = "OperationStatus";
    private final String SNMP_FIRST_INSERT_DATE = "FirstInsertDate";
  
 	
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
            log.error("SNMPTRAP : Could not find attributes in xml " + xmlStream);
            throw new CollageException("Error in SNMPTRAP Adapter. SNMPTRAP : Could not find attributes in xml " + xmlStream);
        }
        
        // Call into collage API updateNagiosLog        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            System.out.println("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            throw new CollageException("Error in Adapter. CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
        }
        
        // Create properties map that includes all the properties
        
        Properties props = new Properties();
        
        try
        {
        	String value;
	        // Log message fields stored as properties
			props.setProperty( LogMessage.KEY_HOST_NAME,           (String)attributes.get(this.SNMP_HOST));
			
			value = (String)attributes.get(this.SNMP_MONITOR_STATUS);
			if (value != null && value.length() > 0)
			{
				value = value.toUpperCase();
			}
			else
			{
				// Monitor status can't be null or empty
	            throw new CollageException("SNMP Adapter reports following error: MonitoStatus needs to be defined. Message rejected.");
			}
			
			props.setProperty( LogMessage.KEY_MONITOR_STATUS,      (String)attributes.get(this.SNMP_MONITOR_STATUS));
			props.setProperty( LogMessage.KEY_REPORT_DATE,         (String)attributes.get(this.SNMP_REPORT_DATE));
			props.setProperty( LogMessage.KEY_LAST_INSERT_DATE,    (String)attributes.get(this.SNMP_LAST_INSERT_DATE));
			
			value = (String)attributes.get(this.SNMP_FIRST_INSERT_DATE);
			if (value != null && value.length() > 0)
				props.setProperty( LogMessage.KEY_FIRST_INSERT_DATE,   value);
			
			String opStatus = (String)attributes.get(this.SNMP_OPERATION_STATUS);
			
			// Default to open
			if (opStatus == null )
				props.setProperty(LogMessage.KEY_OPERATION_STATUS, "OPEN");
			else
				props.setProperty( LogMessage.KEY_OPERATION_STATUS, opStatus);
			
			// SNMP type properties
			props.setProperty( this.SNMP_IPADDRESS,                (String)attributes.get(this.SNMP_IPADDRESS));
			props.setProperty( this.SNMP_EVENT_OID_NUMERIC,        (String)attributes.get(this.SNMP_EVENT_OID_NUMERIC));
			props.setProperty( this.SNMP_EVENT_OID_SYMBOLIC,		(String)attributes.get(this.SNMP_EVENT_OID_SYMBOLIC));
			props.setProperty( this.SNMP_EVENT_NAME,                (String)attributes.get(this.SNMP_EVENT_NAME));
			props.setProperty( this.SNMP_CATEGORY,                  (String)attributes.get(this.SNMP_CATEGORY));
			props.setProperty( this.SNMP_VARIABLE_BINDINGS,         (String)attributes.get(this.SNMP_VARIABLE_BINDINGS));
			
			// process consolidation
			String consolidationName = (String)attributes.get(LogMessage.CONSOLIDATION);
			if ( consolidationName != null && consolidationName.length() > 0)
				props.setProperty( LogMessage.CONSOLIDATION, (String)attributes.get(LogMessage.CONSOLIDATION));

			// Call into the Admin API
			admin.updateLogMessage((String)attributes.get(this.SNMP_MONITOR_SERVER),"SNMPTRAP",(String)attributes.get(this.SNMP_Device), ((String)attributes.get(this.SNMP_SEVERITY)).toUpperCase(), (String)attributes.get(this.SNMP_TEXT_MESSGE), props);
		}
		catch(CollageException ce)
		{
			log.error("Error while calling into Collage Admin API: Error " + ce);
		}
		catch (Exception e)
        {
			log.error("Error while extracting the properties for a request. Error " + e);
        }
	}

	

}
