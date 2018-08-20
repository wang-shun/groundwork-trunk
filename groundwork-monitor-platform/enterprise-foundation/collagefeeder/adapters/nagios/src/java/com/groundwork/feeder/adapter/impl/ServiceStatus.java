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

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.feeder.adapter.FeederBase;

/**
 * 
 * ServiceStatus
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: ServiceStatus.java 19298 2012-08-10 18:27:28Z rruttimann $
 * 
 * Implements inserts/updates into the ServiceStatus section (State) of Collage
 */
public class ServiceStatus implements FeederBase {
	
	//  Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Adapter for NagiosLog entries
     */
    private static String ADAPTER_NAME="SERVICE_STATUS";
    
    public ServiceStatus() {
        super();
        // TODO Auto-generated constructor stub
    }

//  Adapter initialization
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
    		log.debug("ServiceStatus: Null FoundationMessage.");
    		return;
    	}
    	
        // Extract attributes
        List<Hashtable<String, String>> attributeList = message.getAttributes();        
        if (attributeList == null || attributeList.size() == 0)
        {
            log.debug("ServiceStatus: Could not find attributes in xml " + message);
            return;
        }
        
        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            log.debug("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            throw new CollageException("Error in Adapter. CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
        }
        
        // Common attributes
        Hashtable attributes = (Hashtable)attributeList.get(0);
        
        // Check if it is a bulk insert or a single insert
        if ( attributeList.size() == 1)
        {          	
            // Single insert
                 
	        // Call into the API
	        admin.updateServiceStatus((String)attributes.get(AdminAPISignatures.updateServiceStatus[0]),
	        			(String)attributes.get(AdminAPISignatures.updateServiceStatus[1]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[2]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[3]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[4]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[5]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[6]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[7]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[8]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[9]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[10]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[11]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[12]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[13]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[14]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[15]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[16]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[17]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[18]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[19]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[20]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[21]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[22]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[23]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[24]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[25]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[26]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[27]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[28]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[29]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[30]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[31]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[32]),
			(String)attributes.get(AdminAPISignatures.updateServiceStatus[33]));
        }
        else
        {
            // Bulk insert
            Vector serviceStatuses = new Vector();
            
            int argLen = attributeList.size();
            for (int ii =1; ii < argLen; ii++)
            {
                 Hashtable serviceAttributes = (Hashtable)attributeList.get(ii);
                 
                serviceStatuses.add(admin.createNagiosServiceStatusProps((String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[0]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[1]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[2]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[3]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[4]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[5]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[6]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[7]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[8]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[9]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[10]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[11]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[12]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[13]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[14]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[15]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[16]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[17]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[18]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[19]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[20]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[21]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[22]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[23]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[24]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[25]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[26]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[27]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[28]),
                        																	(String)serviceAttributes.get(AdminAPISignatures.createServiceStatus[29])
                        ));
            }
            
            
            // Call into the API
            admin.updateServiceStatus((String)attributes.get(AdminAPISignatures.updateServiceStatusBulk[0]),
            		/*ApplicationType*/"NAGIOS",
        			(String)attributes.get(AdminAPISignatures.updateServiceStatusBulk[1]),
        			(String)attributes.get(AdminAPISignatures.updateServiceStatusBulk[2]),
        			serviceStatuses);
            
        }
        
     }
}
