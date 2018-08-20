package org.groundwork.foundation.jmx;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;

import com.groundwork.collage.CollageFactory;


/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */

public class MsgProducer extends IncommingMessageListenerThread{

	private static final String DEFAULT_JNDI_FACTORY_CLASS = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
	private static final String DEFAULT_JNDI_HOST = "localhost";
	private static final String DEFAULT_JNDI_PORT = "16400";
	private static final String DEFAULT_SERVER_CONTEXT = "cf0";
	private static final String DEFAULT_QUEUE = "groundwork";
	
	private JMSDestinationInfo queueInfo = null;
	private JMSDestinationWriter jmsWriter = null;
	private CollageFactory service = null;
	private Log log = LogFactory.getLog(this.getClass());
	
	private boolean isSending = true;
	private boolean isInitialized = false;

	private static final long MIN_RETRY_TIME = 30000; // 30 seconds
	private static final long MAX_RETRY_TIME = 36000000; // 1 Hour
	
    private static final String FOUNDATION_PROPERTIES = "/usr/local/groundwork/config/foundation.properties";
    private static final String DELIMITER = ",";
    static Context ictx = null;
    private String message;
    
    public MsgProducer(String message){
    	super();
    	this.message = message;
    	this.setup();
    }
    
    public void setup(){
		try{
			queueInfo = new JMSDestinationInfoImpl(DEFAULT_JNDI_FACTORY_CLASS, DEFAULT_JNDI_HOST, DEFAULT_JNDI_PORT, DEFAULT_SERVER_CONTEXT, DEFAULT_QUEUE);
			this.jmsWriter = new JMSDestinationWriterImpl();
			initialize();
		}catch(Exception e){
			log.warn("Could not initialize MessagePublisher ", e);
		}
    }
    
    public void initialize(){
    	long lastAttempt = 0;
		int numAttempts = 0;
		while((isSending == true) && (numAttempts < 6)){
			if (isInitialized == false) {
				long retryTime = MIN_RETRY_TIME * numAttempts;
				if (retryTime > MAX_RETRY_TIME)
					retryTime = MAX_RETRY_TIME;

				if ((System.currentTimeMillis() - lastAttempt) >= retryTime) {
					try {
						jmsWriter.initialize(queueInfo);
						isInitialized = true;

						System.out.println("Foundation JMS MessagePublisher Initialized - Waiting for input ...");

						if (log.isInfoEnabled())
							log.info("Successfully initialized JMSWriter in MessagePublisher.");

						// Reset number of tries since we have successfully
						// initialized.
						numAttempts = 0;
						break;
						//Thread.sleep(20);
					} catch (Exception e) {
						isInitialized = false;
						lastAttempt = System.currentTimeMillis();
						numAttempts++;

						log.error("Failed to initialize JMSWriter in MessagePublisher - Number of attempts: "
										+ numAttempts, e);
					}
				}
			}
		}
    }
    
    public void unInitialize(){
    	this.isSending = false;
		this.isInitialized = false;

		// Un-initialize JMS Writer
		if (jmsWriter != null)
			jmsWriter.unInitialize();
    }
    
    public void run(){
    	try{
    		if(isInitialized == false){
    			initialize();
    		}
			jmsWriter.writeDestination(message);
			jmsWriter.commit();
			System.out.println(message + " sent.");
			if (log.isInfoEnabled())
				log.info("Successfully sent message to JMS Server in MessagePublisher. " + message);
    		
    	}catch(Exception e){
    		log.error("Could not send messge to the JMS server queue. ", e);
    	}
    }
}
