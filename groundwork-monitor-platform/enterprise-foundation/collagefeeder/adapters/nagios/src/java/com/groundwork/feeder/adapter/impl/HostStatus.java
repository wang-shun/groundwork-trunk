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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.feeder.adapter.FeederBase;

/**
 * 
 * HostStatus
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: HostStatus.java 19298 2012-08-10 18:27:28Z rruttimann $
 * 
 * Implements inserts/updates into the HostStatus section (State) of Collage
 */
public class HostStatus implements FeederBase {
	
	//  Enable log for log4j
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Adapter for HostStatus entries
     */
    private static String ADAPTER_NAME="HOST_STATUS";
    
    public HostStatus() {
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
        return ADAPTER_NAME;
    }

    /* (non-Javadoc)
     * @see com.groundwork.feeder.collage.FeederBase#process(java.lang.String)
     */
    public void process(Object beanFactory, FoundationMessage message) 
    {
    	if (message == null)
    	{
    		log.debug("HostStatus: Null FoundationMessage.");
    		return;
    	}
    	
        // Extract attributes
        List<Hashtable<String, String>> listAttributes = message.getAttributes();
        
        if (listAttributes == null || listAttributes.size() == 0)
        {
            log.debug("HostStatus: Could not find attributes in xml " + message);
            return;
        }
        
        Map<String, String> attributes = listAttributes.get(0);
        
        // Get the CollageAdmin interface
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure)((CollageFactory)beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
        if (admin == null)
        {
            // Interface not available throw an error
            log.debug("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
            throw new CollageException("Error in Adapter. CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
        }
        
        // Call into the API
        //admin.updateHostStatus();
        admin.updateHostStatus(  (String)attributes.get(AdminAPISignatures.updateHostStatus[0]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[1]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[2]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[3]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[4]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[5]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[6]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[7]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[8]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[9]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[10]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[11]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[12]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[13]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[14]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[15]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[16]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[17]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[18]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[19]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[20]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[21]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[22]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[23]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[24]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[25]),
					(String)attributes.get(AdminAPISignatures.updateHostStatus[26])
		        );

     }

}
