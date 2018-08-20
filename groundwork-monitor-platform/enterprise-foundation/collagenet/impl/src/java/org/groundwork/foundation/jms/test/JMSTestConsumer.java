/*
* Collage - The ultimate data integration framework.
*
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
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
/*
* Collage - The ultimate data integration framework.
*
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
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

package org.groundwork.foundation.jms.test;

import java.io.FileInputStream;
import java.util.Properties;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationReader;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationReaderImpl;

/**
 * @author rogerrut
 *
 * Created: Apr 9, 2007
 */
public class JMSTestConsumer extends Thread 
{
		/** Use log4j */
	    private static Log log = LogFactory.getLog(JMSTestConsumer.class);

		private String queueName;

		private String contextId;
		private String jndiInitialFactory = JMSDestinationInfo.DEFAULT_JNDI_FACTORY_CLASS;
		private String jndiHost = JMSDestinationInfo.DEFAULT_JNDI_HOST;
		private String jndiServer = JMSDestinationInfo.DEFAULT_JNDI_PORT;
	    
		public JMSTestConsumer(Properties configuration) {
			queueName = configuration.getProperty("queue", "groundwork").trim();
			contextId = configuration.getProperty("context", "cf0").trim();
		}

		public void run() 
		{			
			JMSDestinationInfo queueInfo	= new JMSDestinationInfoImpl(jndiInitialFactory, 
																			jndiHost, 
																			jndiServer, 
																			this.contextId, 
																			this.queueName, JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER, JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS);
			
			JMSDestinationReader jmsReader = null;
			try
			{
				jmsReader = new JMSDestinationReaderImpl();
				jmsReader.initialize(queueInfo, true, -1, null);					
			} 
			catch (FoundationJMSException fje)
			{
				log.error("Error initializing reader: " + fje);
			}
						
			Message msg = null;
			long start = 0;
			
			// Wait for input
			while (true)
			{
				try {
					char c = (char)System.in.read();
					
					// Exit on 
					if (c == 'c')
						break;
					
					
					switch (c)
					{
						// Read 1 Message
						case 'r':		
							start = System.currentTimeMillis();
							msg = jmsReader.readMsg();							
							jmsReader.commit();					
							log.info("Time to Read 1 Message (ms): " + (System.currentTimeMillis() - start));
							log.info("Message: " + ((TextMessage)msg).getText());
							break;	
							// Read All Message on queue
						case 'a':	
						{
							int i = 0;						
							start = System.currentTimeMillis();
							while (true)
							{
								msg = jmsReader.readMsg(500);
								if (msg != null)
								{			
									log.info("Message " + i + ": " + msg.toString());
									i++;
								}
								else {
									break;
								}
							}
							jmsReader.commit();
							
							log.info("Time to Read " + i + " Messages (ms): " + (System.currentTimeMillis() - start));
						}
							break;								
						default:
							continue;
					}		
				}
				catch (Exception e)
				{
					log.error(e);
				}
				
			}
			
			jmsReader.unInitialize();			
		}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = System.getProperty("configuration",
		"./consumer.properties");
			Properties configuration = new Properties();
			try {
				FileInputStream fis = new FileInputStream(configFile);
				configuration.load(fis);
			} catch (Exception e) {
				
				log.warn("WARNING: Could not load Consumer properties. Using defaults");
			}
			// TODO: log4j
			//log.warn("Properies loaded..");
			JMSTestConsumer jmsTestConsumer = new JMSTestConsumer(configuration);
			jmsTestConsumer.run();
			
	}
}
