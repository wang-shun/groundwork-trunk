/*$Id: $
* Collage - The ultimate data integration framework.
*
* Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/
package org.groundwork.foundation.jmx;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the main class for JMX service. 
 * JMX Service perform get configuration from /usr/local/groundwork/config/foundation_jmx.xml file. And perform queries data from MBEAN servers.
 * JMX Service also publish message to the Foundation JMS server.
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */
public class JMXService{
	
	private String message1 = "";
	private String message2 = "";
	private String message3 = "";
	private MsgProducer producer;
	public static final String PREFIX_ADAPTER = "<Adapter Session=\"1\" AdapterType=\"SystemAdmin\">";
	public static final String SURFIX_ADAPTER = "</Adapter>";
	public static final String PREFIX_COMMAND_ADD = "<Command Action=\"ADD\" ApplicationType=\"JMX\">";
	public static final String PREFIX_COMMAND_MODIFY = "<Command Action=\"MODIFY\" ApplicationType=\"JMX\">";
	public static final String SURFIX_COMMAND = "</Command>";
	private IncommingMessage im;
	private QueryMBEAN qm;
	private IncommingMessageListenerThread imlt[];
	private Log log = LogFactory.getLog(this.getClass());
	/**
	 * Constructor for JMXService
	 * Initialize for MessagePublisher from here. 
	 */
	public JMXService() throws JMXConfigurationException{

	}
	
	/**
	 * startJMXService function used to start JMX service.
	 * This function load configuration and queries MBEAN from configuration file.
	 * This function keep connection to MBEAN servers and perform queries attributes from MBEAN server using multithreading 
	 * for each interval time (1,5 and 10 minutes).
	 * Each time get the results, this function also publish message to the JMS server.
	 */
	public void startJMXService() throws JMXConfigurationException {
		try{
			im = new IncommingMessage();
			im.start();
			

		}catch(Exception e){
			log.error(e);
			throw new JMXConfigurationException("Error while startJMXService", e);
			
		}
		
	}
	
	/**
	 * stop the JMX service
	 */
	public void stopJMXService(){
		//close all JMX connection
		if(im != null){
			im.unInitialize();
			im = null;
		}

	}
	
	
}
