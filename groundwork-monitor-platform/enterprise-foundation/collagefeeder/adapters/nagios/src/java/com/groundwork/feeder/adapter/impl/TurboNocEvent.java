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

public class TurboNocEvent implements FeederBase {
	
	private static String ADAPTER_NAME="TURBO_NOC_EVENT";
	
	/**
     * Pre-defined Fields / Attributes
     */
    private static String TYPE_RULE="TypeRule";
    private static final String ACKNOWLEDGEDBY = "AcknowledgedBy";
    private static final String ACKNOWLEDGE_COMMENT = "AcknowledgeComment";
    
    /* TypeRule field */
    private static final String ACKNOWLEDGE = "ACKNOWLEDGE";
    private static final String UNACKNOWLEDGE = "UNACKNOWLEDGE";
    
    /* Other fields */
    private static final String HOST_NAME = "Host";
    private static final String SERVICE_DESCRIPTION = "ServiceDescription";
    private static final String APPLICATION_TYPE = "ApplicationType";
    
    private final String MONITOR_STATUS = "MonitorStatus";
    private final String TEXT_MESSAGE = "TextMessage";
    private final String REPORT_DATE = "ReportDate";
    private final String LAST_INSERT_DATE = "LastInsertDate";
       
//  Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    public String getName() {
        return ADAPTER_NAME ;
    }

	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void process(Object beanFactory, FoundationMessage message)
	{
    	if (message == null)
    	{
    		log.debug("TURBO_NOC_EVENT: Null FoundationMessage.");
    		return;
    	}
    	
        // Extract attributes
        List<Hashtable<String, String>> listAttributes = message.getAttributes();
        
        if (listAttributes == null || listAttributes.size() == 0)
        {
            log.debug("TURBO_NOC_EVENT: Could not find attributes in xml " + message);
            return;
        }
        
        Map<String, String> attributes = listAttributes.get(0);
        
        // Call into collage API updateNagiosLog        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
        	log.error("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
        	return;
        }
        
        try
        {
        	/*
        	 * Acknowledge / un-acknowledge of event messages
        	 * 
        	 * If the TypeRule field defines ACKNOWLEDGE or UNACKNOWLEDGE existing LogMessages
        	 * will be updated with the Acknowledgement information.
        	 * 
        	 * Required fields are: Host, ApplicationType, AcknowledgedBy
        	 * Optiobal fields: ServiceDescription, AcknowledgeComment
        	 * 
        	 * If ServiceDescription is not defined all Events for a Host, ApplicationType and OperationStatus "OPEN" will be updated with the AcknowledgeBy and AcknowledgeComment
        	 * If ServiceDescription is defined all Events for a Host, ServiceDescription, ApplicationType and OperationStatus "OPEN" will be updated with the AcknowledgeBy and AcknowledgeComment
        	 * 
        	 */
        	String typeRule = (String)attributes.get(this.TYPE_RULE);
        	if	(		  typeRule != null 
        			&& ( (typeRule.compareTo(this.ACKNOWLEDGE) == 0) || (typeRule.compareTo(this.UNACKNOWLEDGE) == 0) )
        		)
        	{
        		// update existing log message
        		String host = (String)attributes.get(this.HOST_NAME);
        		String appType = (String)attributes.get(this.APPLICATION_TYPE);
        		String acknowledgedBy = (String)attributes.get(this.ACKNOWLEDGEDBY);
       		
        		/* HostName is required in order to acknowledge messages */
        		if ( (typeRule.compareTo(this.ACKNOWLEDGE) == 0) && (host == null || appType == null || acknowledgedBy == null))
        		{
        			log.error("HostName, ApplicationType and AcknowledgedBy attributes are required to Acknowledge events." );
        			return;
        		}
        		
        		if ( (typeRule.compareTo(this.UNACKNOWLEDGE) == 0) && (host == null || appType == null ))
        		{
        			log.error("HostName and ApplicationType attributes are required to Un-Acknowledge events." );
        			return;
        		}
        		
        		
        		/* Optional attributes */ 
        		String serviceDescription = (String)attributes.get(this.SERVICE_DESCRIPTION);
        		String acknowledgeComment = (String)attributes.get(this.ACKNOWLEDGE_COMMENT);
        		
        		admin.acknowledgeEvent(appType, typeRule, host, serviceDescription, acknowledgedBy, acknowledgeComment);
        	}
        	else
        	{
        		// Check if we got a valid MonitorStatus and Severity otherwise set it to "UNKNOWN"
	        	String monitorStatus = (String)attributes.get(AdminAPISignatures.updateLogMessage[5]);
	        	if (monitorStatus == null)
	        		monitorStatus = "UNKNOWN";
	        	
	        	String severity = (String)attributes.get(AdminAPISignatures.updateLogMessage[4]);
	        	if (severity == null)
	        		severity = "UNKNOWN";
	        	
	        	/*
	        	 * ReportDate needs to be provided to the API. If the attribute is missing set it to the current
	        	 * system time
	        	 */
	        	String reportDate = (String)attributes.get(LogMessage.EP_REPORT_DATE);
		        if (reportDate == null || (reportDate != null && reportDate.length() == 0) )
		        {
		        	// Log warning since report date should be provided by the Feeder
		        	log.warn("Nagios Log Adapter. ReportDate was not provided with event. Set it to system time.");
		        	
				    // Use current System time
		        	SimpleDateFormat rd = new SimpleDateFormat();
		            rd.applyPattern("yyyy-MM-dd HH:mm:ss");
		            
		            reportDate = rd.format(new Date(System.currentTimeMillis()));
		        }
	        	
		         // Call into the API
	        	admin.updateLogMessage( (String)attributes.get(LogMessage.KEY_CONSOLIDATION),
										"NAGIOS", // LogType NAGIOS, COLLAGE or SYSLOG
										(String)attributes.get(AdminAPISignatures.updateLogMessage[1]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[2]),	// HostName
										(String)attributes.get(AdminAPISignatures.updateLogMessage[3]),	// Identification
										severity,
										monitorStatus,
										(String)attributes.get(AdminAPISignatures.updateLogMessage[6]),
										reportDate, //(String)attributes.get(AdminAPISignatures.updateLogMessage[7]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[8]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[9]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[10]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[11]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[12]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[13]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[14]),
										(String)attributes.get(AdminAPISignatures.updateLogMessage[15]));
        	}
        	
        	// Call into Service Status Update or Host Status Update
        	String errorType = (String)attributes.get("ErrorType");
        	if (errorType == null){
        		log.error("TurboNocEvent -- Error Type Service Alert or Host Alert not defined");
        		return;
        	}
        	/* Build proprties */
        	Properties props = new Properties();
            
            // Add the required values to the properties
            props.setProperty( this.MONITOR_STATUS, (String)attributes.get("MonitorStatus"));
            props.setProperty( "LastCheckTime",  (String)attributes.get(this.LAST_INSERT_DATE));
            props.setProperty( "LastStateChange",  (String)attributes.get("ReportDate"));
            props.setProperty( "LastPluginOutput",   (String)attributes.get(this.TEXT_MESSAGE)  );
	        
        	if (errorType.compareToIgnoreCase("HOST ALERT") == 0)
        	{
        		admin.updateHostStatus(
        				(String)attributes.get(AdminAPISignatures.updateLogMessage[1])/*monitorServerName*/,
						"NAGIOS", 
						(String)attributes.get(AdminAPISignatures.updateLogMessage[2])/*hostName*/,
						(String)attributes.get(AdminAPISignatures.updateLogMessage[3])/*deviceIdentification*/,
						props);
        	}
        	else
        	{
        		/* Service Alert */
        		props.setProperty( this.SERVICE_DESCRIPTION,   (String)attributes.get("ServiceDescription")  );
        		
        		admin.updateServiceStatus(
        				(String)attributes.get(AdminAPISignatures.updateLogMessage[1])/*monitorServerName*/,
        				"NAGIOS", 
        				(String)attributes.get(AdminAPISignatures.updateLogMessage[2])/*hostName*/,
        				(String)attributes.get(AdminAPISignatures.updateLogMessage[3])/*deviceIdentification*/,
        				props);
        	}
        }
        catch(CollageException ce)
        {
        	log.error("Error while calling into Collage Admin API: Error " + ce);
        }
       
    }


	public void uninitialize() {
		// TODO Auto-generated method stub

	}

}
