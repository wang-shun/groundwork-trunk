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

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.feeder.adapter.FeederBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * 
 * ApplicationLog
 * Class to log collage (framework) erros to the log table.
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: CollageLog.java 15155 2009-04-30 22:09:42Z rruttimann $
 */
public class CollageLog implements FeederBase {

    /**
     * Adapter for framework log
     */
    private static String ADAPTER_NAME="COLLAGE_LOG";
    
//  Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    public CollageLog() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    // Adapter initialization
    public void initialize()
	{
		// Nothing to do 		
	}
	
	public void uninitialize()
	{
//		 Nothing to do 	
	}

    /* (non-Javadoc)
     * @see com.groundwork.feeder.collage.FeederBase#getName()
     */
    public String getName() {
        return ADAPTER_NAME ;
    }

    /* (non-Javadoc)
     * @see com.groundwork.feeder.collage.FeederBase#process(java.lang.String)
     */
    public void process(Object beanFactory, FoundationMessage message)
    {
    	if (message == null)
    	{
    		System.out.println("CollageLog: Null FoundationMessage.");
    		return;
    	}
    	
        // Extract attributes
        List<Hashtable<String, String>> listAttributes = message.getAttributes();
        
        if (listAttributes == null || listAttributes.size() == 0)
        {
            System.out.println("CollageLog: Could not find attributes in xml " + message);
            return;
        }
        
        Map<String, String> attributes = listAttributes.get(0);        
        
        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            log.error("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            return;        
        }
        
        Properties props = new Properties();
        
        SimpleDateFormat reportDate = new SimpleDateFormat();
        reportDate.applyPattern("yyyy-MM-dd HH:mm:ss");
                
        // Log message fields stored as properties
		props.setProperty( LogMessage.EP_HOST_NAME, "localhost"); //          "CollageListener");
		props.setProperty( LogMessage.EP_REPORT_DATE,         reportDate.format(new Date(System.currentTimeMillis())));
	
		if (attributes.containsKey(LogMessage.EP_MONITOR_STATUS_NAME) == true)
		{
			props.setProperty(LogMessage.EP_MONITOR_STATUS_NAME, (String)attributes.get(LogMessage.EP_MONITOR_STATUS_NAME));
		}
		else {
			props.setProperty(LogMessage.EP_MONITOR_STATUS_NAME, "DOWN");
		}
		
		// For text Message.Adding text message.JIRA 5634
		// Default to NA
		String txtMsg = (String)attributes.get(LogMessage.EP_TEXT_MESSAGE);
		if (txtMsg == null )
			props.setProperty(LogMessage.EP_TEXT_MESSAGE, "NA");
		else
			props.setProperty( LogMessage.EP_TEXT_MESSAGE, txtMsg);
		
		
		if (attributes.containsKey(LogMessage.KEY_CONSOLIDATION) == true)
		{
			props.setProperty(LogMessage.KEY_CONSOLIDATION, (String)attributes.get(LogMessage.KEY_CONSOLIDATION));
		}
		
		String severity = (attributes.containsKey(LogMessage.EP_SEVERITY_NAME) 
								? (String)attributes.get(LogMessage.EP_SEVERITY_NAME) 
								: "FATAL");

		try
		{
			// Log a error into the LogMessage table
			admin.updateLogMessage("localhost",
								   "SYSTEM",
								   "127.0.0.1", 
								   severity, 
								   (String)attributes.get(LogMessage.EP_TEXT_MESSAGE), props);
		}
		catch(CollageException ce)
		{
			log.error("Error while calling into Collage Admin API: Error " + ce);
		}
        
    }

}
