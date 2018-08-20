package org.groundwork.foundation.jmx;

import java.util.Hashtable;

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
import org.groundwork.foundation.jms.JMSDestinationWriter;

public class IncommingMessageProcessing extends IncommingMessageListenerThread{

//	private static final String DEFAULT_JNDI_FACTORY_CLASS = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
//	private static final String DEFAULT_JNDI_HOST = "localhost";
//	private static final String DEFAULT_JNDI_PORT = "16400";
//	private static final String DEFAULT_SERVER_CONTEXT = "cf0";
//	private static final String DEFAULT_QUEUE = "groundwork";
//	private static final String JMX_QUEUE = "jmx-monitor";
	
	private ConnectionFactory	cnxF = null;
    private Queue				dest = null;
    private Connection			cnx = null;
    
	private JMSDestinationWriter jmsProducer = null;
	private boolean isSending = true;
	private boolean isReading = true;
	private boolean isJMSProducerInitialized = false;
	private boolean isJMSConsumerInitialized = false;
    private MsgProducer producer;
	private static final long MIN_RETRY_TIME = 3000; // 30 seconds
	private static final long MAX_RETRY_TIME = 36000000; // 1 Hour
	private static final long RETRY_TIMEOUT = 3000; // 30 seconds
	
		/* Enable log for log4j */
	private Log log = LogFactory.getLog(this.getClass());

	public IncommingMessageProcessing(){
		super();
		//isJMSConsumerInitialized = initializeJMSConsumer();
		//initializeJMSProducer();
	}
	public void run(){
		MessageConsumer consumer = null;
		Session session = null;
		Message msg = null;
		
		while (isReading)
		{
			// Try to connect to JMS server and initialize
			while (isJMSConsumerInitialized == false)
			{							
				isJMSConsumerInitialized = initializeJMSConsumer();
				
				if (isJMSConsumerInitialized == false)
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
				isJMSConsumerInitialized = false; // Re-initialize connection

				log.error("IM - Unable to read JMX JMS Queue.  JMS Server may be down.  Will re-try connection.", e);
			}
			finally {			
				try {
					if (consumer != null)
						consumer.close();
				}
				catch (Exception e)
				{
					log.error("IM - Error closing consumer.", e);								
				}
			}
			
			if (msg == null)
				continue;
					
			// Currently, only supporting TextMessage
			if ((msg instanceof TextMessage) == false)
			{
				log.error("IM - Currently only supporting text messages.");
				
				try {
					if (msg != null)
					{
						log.error("IM - Invalid message type - " + msg.getJMSType());
						
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
				if (log.isInfoEnabled())
				{
					log.info("IM - Incoming JMS Message about to be processed - [" 
							+ ((TextMessage)msg).getText()
							+ "]");
				}
				System.out.println("IM - Incoming JMS Message about to be processed - [" 
						+ ((TextMessage)msg).getText()
						+ "]");
				
				String xmlMessage = ((TextMessage)msg).getText();
				XMLProcessing.addHosts(xmlMessage);
				XMLProcessing.addServices(xmlMessage);
				String fMessage = XMLProcessing.filterMessage(xmlMessage);
				producer = new MsgProducer(fMessage);
				producer.start();
				producer.join();
				producer.unInitialize();
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
				
				log.error("IM - Error placing message on queue message.", e);
			}						
		}
	}
	
	public void unInitialize(){
		try{
			cnx.close();
		}catch(Exception e){
			log.warn(e);
		}
		isReading = false;
		isSending = false;
		log.warn("Shutdown listener in IncommingMessageListener");
	}
	

	public boolean initializeJMSConsumer(){
		Hashtable<String, String> htJndiProperties = new Hashtable<String, String>(3);
		htJndiProperties.put("java.naming.factory.initial", ConfigurationManager.JNDI_FACTORY_CLASS);
		htJndiProperties.put("java.naming.factory.host", ConfigurationManager.JNDI_HOST);
		htJndiProperties.put("java.naming.factory.port", ConfigurationManager.JNDI_PORT);
		
		Context ictx = null;
		
		try 
		{
			ictx = new InitialContext(htJndiProperties);
	        cnxF = (ConnectionFactory) ictx.lookup(ConfigurationManager.SERVER_CONTEXT);
	        dest = (Queue) ictx.lookup(ConfigurationManager.J_QUEUE);
	        ictx.close();
	        
	        cnx = cnxF.createConnection();
	        log.warn("Successful connect to JMX message queue. Wait for input ...");
	        System.out.println("IM - Successful connect to JMX message queue. Wait for input ...");
	        cnx.start();			
		}
		catch (Exception e)
		{
			log.error("Error initializing connection to JMS server when reading message from jmx-monitor queue.", e);
			return false;
		}
		return true;
	}
}
