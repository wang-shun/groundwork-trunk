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

package org.groundwork.foundation.engine;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSServer;
import org.groundwork.foundation.jms.JMSServerInfo;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSServerImpl;
import org.groundwork.foundation.jms.impl.JMSServerInfoImpl;

/**
 * @author Roger Ruttimann <rruttimann@groundworkopensource.com>
 *
 * Created: June 19, 2007
 */

public class PersistentService
{
	private static final String DELIMITER = ",";
	
	/** Use log4j */
    private Log log = LogFactory.getLog(this.getClass());
    
    // JNDI Configuration
    private String jndiInitialFactory = null;
    private String jndiFactoryHost = null;
    private String jndiFactoryPort = null;
        
    // Server Configuration
	private String serverName;	
	private String contextId;
	
	protected String command;
    protected String execCommand;
    
    private String persistencePath;
	
    private int serverId;
	
    private String	adminUser;
    private String	adminPassword;
    private int		adminPort;
    
    // Destinations
    private List<String> queueNames = new ArrayList<String>(1);
    private List<String> topicNames = new ArrayList<String>(1);

    /* Persistence instance */
    JMSServer jmsServer = null;

    /* Default constructor */
    public PersistentService()
    {
    	
    }
    
    /** Start JMS Server
     * Use the current configuration
     */
    public void startPersistenceService(Properties configuration) throws FoundationJMSException
    {
    	if (configuration == null)
    		configuration = new Properties();
    	
    	// load default configuration
    	this.loadConfiguration(configuration);
    	
    	// Create structures for Server and Queue information
		JMSServerInfo serverInfo = new JMSServerInfoImpl(this.jndiInitialFactory,
														this.jndiFactoryHost,
														this.jndiFactoryPort,
														this.serverName, 
														this.contextId, 
														this.serverId, 
														this.persistencePath, 
														this.adminUser, 
														this.adminPassword, 
														this.adminPort, null);
		
		// Create a server
		jmsServer = new JMSServerImpl();
		jmsServer.initialize(serverInfo);
		
		log.warn("HornetQ JMS started");
		
		/* Add queues to server */
		if (this.queueNames != null && queueNames.size() > 0)
		{
			JMSDestinationInfo queueInfo = null;
			String queueName = null;			
			Iterator<String> it = this.queueNames.iterator();
			while (it.hasNext())
			{
				queueName = it.next().trim();
				
				queueInfo = new JMSDestinationInfoImpl(this.jndiInitialFactory,
						  this.jndiFactoryHost, 
						  this.jndiFactoryPort, 
						  this.contextId, 
						  queueName,  this.adminUser,this.adminPassword);
				jmsServer.addQueue(queueInfo);
				
				log.warn("Added Message queue ["+ queueName + "]");
			}
		}

		/* Add topics */
		if (this.topicNames != null && topicNames.size() > 0)
		{
			JMSDestinationInfo topicInfo = null;
			String topicName = null;			
			Iterator<String> it = this.topicNames.iterator();
			while (it.hasNext())
			{
				topicName = it.next().trim();
				
				topicInfo = new JMSDestinationInfoImpl(this.jndiInitialFactory,
						  this.jndiFactoryHost, 
						  this.jndiFactoryPort, 
						  this.contextId, 
						  topicName, this.adminUser,this.adminPassword);
				jmsServer.addTopic(topicInfo);	
				
				log.warn("Added Topic ["+ topicName + "]");
			}
		}
    }
    
    /** Start PersistenceService using the configuration in the properties file provided by the argument
     * 
     * @param pathToConfig
     * @throws FoundationJMSException
     */
    public void startPersistenceService(String pathToConfig) throws FoundationJMSException
    {
    	this.startPersistenceService(this.readConfiguration(pathToConfig));
    }    
    
    /*setters/getters for JMS Server Configuration*/
    public void setPersistenceServiceConfiguration(Properties configuration) throws FoundationJMSException
    {
    	this.loadConfiguration(configuration);
    }
        
    public Properties getPersistenceServiceConfiguration() throws FoundationJMSException
    {
    	return null;
    }
    
    /*Stop JMS Server */
    public void stopPersistenceService() throws FoundationJMSException
    {
    	this.jmsServer.unInitialize();
    }
    
    /** Management Methods
     * addQueueToPersistentService -- Allows to add a new queue to the service
     * @param queueName
     * @throws FoundationJMSException
     */
    
    public void addQueueToPersistentService(String queueName) throws FoundationJMSException
    {
		JMSDestinationInfo queueInfo = new JMSDestinationInfoImpl(this.jndiInitialFactory,
																  this.jndiFactoryHost, 
																  this.jndiFactoryPort, 
																  this.contextId, 
																  queueName,  this.adminUser,this.adminPassword);
		this.jmsServer.addQueue(queueInfo);
    }
    
    public void removeQueueFromPersistentService(String queueName) throws FoundationJMSException
    {
		JMSDestinationInfo queueInfo = new JMSDestinationInfoImpl(this.jndiInitialFactory,
				  this.jndiFactoryHost, 
				  this.jndiFactoryPort, 
				  this.contextId, 
				  queueName,  this.adminUser,this.adminPassword);
    	this.jmsServer.removeQueue(queueInfo);
    }
        
    /* Utility functions */
    private void loadConfiguration(Properties configuration) throws FoundationJMSException
    {
		/* Admin user info */
		this.adminUser = configuration.getProperty("jms.admin.user", "root").trim();
		this.adminPassword = configuration.getProperty("jms.admin.password", "root").trim();
		
		/* Admin Port Nb */
		String portNb = configuration.getProperty("jms.admin.port", "16011").trim();
		Integer intPortNb = new Integer(portNb);
		this.adminPort = intPortNb.intValue(); 
		
		this.serverName = configuration.getProperty("jms.server.name", "localhost").trim();		
		this.contextId = configuration.getProperty("jms.server.context.id", "cf0").trim();

		String serverID = configuration.getProperty("serverid", "0").trim();
		this.persistencePath = configuration.getProperty("jms.server.persistence.path", "./s0").trim();
		
		Integer value = new Integer(serverID);
		this.serverId = value.intValue();
		
		// JNDI Configuration
		this.jndiInitialFactory = configuration.getProperty("jndi.factory.initial", "org.jnp.interfaces.NamingContextFactory").trim();
		this.jndiFactoryHost = configuration.getProperty("jndi.factory.host", "localhost").trim();
		this.jndiFactoryPort = configuration.getProperty("jndi.factory.port", "1099").trim();

	    
		// Destinations
		String queues = configuration.getProperty("jms.server.queues", "").trim();
		if (queues != null && queues.length() > 0)
		{
			StringTokenizer tokenizer = new StringTokenizer(queues, DELIMITER);
			while (tokenizer.hasMoreTokens())
			{
				queueNames.add(tokenizer.nextToken());
			}
		}
		
		String topics = configuration.getProperty("jms.server.topics", "").trim();
		if (topics != null && topics.length() > 0)
		{
			StringTokenizer tokenizer = new StringTokenizer(topics, DELIMITER);
			while (tokenizer.hasMoreTokens())
			{
				topicNames.add(tokenizer.nextToken());
			}
		}		
    }
    
    private Properties readConfiguration(String pathToConfig) throws FoundationJMSException
    {
    	Properties configuration = new Properties();
    
    	if (pathToConfig == null || pathToConfig.length() == 0)
    	{
    		log.warn("No JMS configuration properties defined. Using defaults");
    		return configuration;
    	}
    	
		try {
			FileInputStream fis = new FileInputStream(pathToConfig);
			configuration.load(fis);
		} catch (Exception e) {
			log.warn("Could not load JMS configuration properties - [" + pathToConfig + "]. Using defaults");
		}
		
		return configuration;
    }
}
