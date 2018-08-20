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
package com.groundwork.feeder.service;

import java.util.Hashtable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.JMSDestinationInfo;

import com.groundwork.feeder.adapter.AdapterManager;
import com.groundwork.feeder.adapter.impl.FoundationMessage;


public class FoundationDispatcher extends FoundationListenerThread
{
	private static final int RETRY_TIMEOUT = 30000;
	
	private static final String PROP_MESSAGE_QUEUE_MAX = "fmd.message.queue.max";
	private static final String DEFAULT_MESSAGE_QUEUE_MAX = "500";
	
    private ConnectionFactory	cnxF = null;
    private Queue				dest = null;
    private Connection			cnx = null;
    private JMSDestinationInfo	_queueInfo = null;
	private FoundationMessageQueue _foundationMessageQueue = null;
		
	// Log
	private Log log = LogFactory.getLog(this.getClass());					
	
	private boolean _isReading = true;
	private boolean _isInitialized = false;
	private int _messageQueueMax = 500;
	
	/*************************************************************************/
	/* Constructors
	/*************************************************************************/
	public FoundationDispatcher (JMSDestinationInfo queueInfo, 
								 Properties configuration, 
								 AdapterManager adapterMgr)	
	{
		if (queueInfo == null)
			throw new IllegalArgumentException("Invalid null / empty JMS queue info parameter.");
		
		if (adapterMgr == null)
			throw new IllegalArgumentException("Invalid AdapterManager parameter.");		
		
		_queueInfo = queueInfo;
		
		// Set Configuration
		String val = configuration.getProperty(PROP_MESSAGE_QUEUE_MAX, DEFAULT_MESSAGE_QUEUE_MAX);
		_messageQueueMax = Integer.parseInt(val);

		// Create Foundation Message Queue
		_foundationMessageQueue = new FoundationMessageQueue(configuration, adapterMgr);	
		_foundationMessageQueue.start();
	}
	
	/*************************************************************************/
	/* Public Methods
	/*************************************************************************/	
	
	public void unInitialize()
	{
		if (_foundationMessageQueue != null)
		{
			_isReading = false;		
			_foundationMessageQueue.unInitialize();
		}
	}
	
	public void run ()
	{
		MessageConsumer consumer = null;
		Session session = null;
		Message msg = null;
		
		while (_isReading)
		{
			// Try to connect to JMS server and initialize
			while (_isInitialized == false)
			{							
				_isInitialized = initializeJMS(_queueInfo);
				
				if (_isInitialized == false)
				{
					// Try again in RETRY_TIMEOUT seconds
					try {
						Thread.sleep(RETRY_TIMEOUT);
					}
					catch (Exception ex)
					{
						log.error(ex);
					}						
				}
			}
			
			// Throttle message queue in order to increase processing performance.
			// There is no need to continually to add messages
			if (_foundationMessageQueue.getMessageQueueCount() > _messageQueueMax)
			{
				try {
					Thread.sleep(1000);
				}
				catch (Exception e)
				{
					log.error(e);
				}
				
				continue;
			}
			
			try {
				// Create a session for each message b/c each message is atomic
				// Message will be acknowledged after processing has been completed.					
				session = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
				consumer = session.createConsumer(dest);								
				msg = consumer.receive();
			}
			catch (Exception e)
			{
				// Reset 
				msg = null;
				_isInitialized = false; // Re-initialize connection

				log.error("FoundationDispatcher - Unable to read JMS Queue.  JMS Server may be down.  Will re-try connection.", e);
			}
			finally {			
				try {
					if (consumer != null)
						consumer.close();
				}
				catch (Exception e)
				{
					log.error("FoundationDispatcher - Error closing consumer.", e);								
				}
			}
			
			if (msg == null)
				continue;
					
			// Currently, only supporting TextMessage
			if ((msg instanceof TextMessage) == false)
			{
				log.error("FoundationDispatcher - Currently only supporting text messages.");
				
				try {
					if (msg != null)
					{
						log.error("FoundationDispatcher - Invalid message type - " + msg.getJMSType());
						
						msg.acknowledge(); // Make sure message is off the queue							
					}
					
					if (session != null)
						session.close();
				}
				catch (Exception e)
				{
					log.error(e);
				}
				
				continue;
			}		
			
			// Add message to incoming queue
			try 
			{			
				if (log.isDebugEnabled())
				{
					log.debug("FoundationDispatcher - Incoming JMS Message about to be processed - [" 
							+ ((TextMessage)msg).getText()
							+ "]");
				}
				if (_isReading == true)
					_foundationMessageQueue.processMessage(new FoundationMessage(session, (TextMessage)msg));
			}
			catch (Exception e)
			{
				try {
					if (msg != null)
					{
						msg.acknowledge(); // Make sure message is off the queue							
					}
					
					if (session != null)
						session.close();
				}
				catch (Exception ex)
				{
					log.error(ex);
				}
				
				log.error("FoundationDispatcher - Error placing message on queue message.", e);
			}						
		}
	}
	
	/*************************************************************************/
	/* Protected Methods
	/*************************************************************************/
	
	/*************************************************************************/
	/* Private Methods
	/*************************************************************************/
	
	private boolean initializeJMS(JMSDestinationInfo queueInfo) 
	{
		Hashtable<String, String> htJndiProperties = new Hashtable<String, String>(3);
		htJndiProperties.put("java.naming.factory.initial", queueInfo.getContextFactory());
		htJndiProperties.put("java.naming.provider.url", "remote://" + queueInfo.getHost()
				+ ":" + queueInfo.getPort());
		
		Context ictx = null;
		
		try 
		{
			ictx = new InitialContext(htJndiProperties);
	        cnxF = (ConnectionFactory) ictx.lookup(queueInfo.getServerContext());
	        dest = (Queue) ictx.lookup(queueInfo.getDestinationName());
	        ictx.close();

	        cnx = cnxF.createConnection(queueInfo.getAdminUser(),queueInfo.getAdminCredentials());
	        cnx.start();			
		}
		catch (Exception e)
		{
			log.error("Error initializing connection to JMS server.", e);
			return false;
		}
		finally 
		{
			if (ictx != null)
			{
				try { ictx.close(); } catch (Exception e) {}
			}			
		}
		
		if (log.isDebugEnabled())
			log.debug("Foundation Dispatcher - JMS connection successfully initialized.");
       
        return true;
	}
}

