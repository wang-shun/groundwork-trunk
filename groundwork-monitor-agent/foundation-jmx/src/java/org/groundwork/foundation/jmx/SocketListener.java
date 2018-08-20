package org.groundwork.foundation.jmx;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * SocketListenerThread
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @version $Id: SocketListenerThread.java,v 1.1 2005/04/16 00:26:32 rogerrut
 *                Exp $
 */
public class SocketListener implements Runnable
{
	// String constants
	private static final String CLOSE = "close";
	
    /*
     * Thread member variables  
     */
    private Socket serviceSocket = null;

    private DataInputStream requestInputStream = null;    

    private IncommingMessage.listenerMainThread parent = null;
    
    boolean acceptCalls = true;
    
    private long threadId = 0;
    
    private MsgProducer producer;
    
	/* Enable log for log4j */
    private Log log = LogFactory.getLog(this.getClass());
        
    public SocketListener(Socket newClient, IncommingMessage.listenerMainThread parent) 
    {
        try {
            this.serviceSocket = newClient;
            this.parent = parent;

            this.requestInputStream = 
            	new DataInputStream(serviceSocket.getInputStream());
        } catch (IOException e) {
        	log.error("Exception while connecting to socket.Error: " +e);
        }
    }
    
    public void run() 
    {    	
        threadId = Thread.currentThread().getId();
     
        // Timeout gate keeper just a reminder when the last request was
        // received
        long lastRequest = System.currentTimeMillis();
        
        if (log.isInfoEnabled())
        	log.info("New Listener started ID(" + threadId
                + "). Waiting for input...\n");
                
        // Parser for incoming message streams
        XMLFeedParser xmlParser = new XMLFeedParser();
               
        int numRead = 0;
        byte[] ba = new byte[ConfigurationManager.BLOCK_READ_SIZE];
        
        // Listens until client closes connection
        while (serviceSocket.isClosed() == false && acceptCalls == true) 
        {            
            // Read from the socket. Parse out XML messages < />
            try {
                // Read until we get a message
                numRead = this.requestInputStream.read(ba);                
                while (numRead > 0)
                {                                	                
                	xmlParser.processData(new String(ba, 0, numRead));
                	
                	// Message exists so we process it
                	if (xmlParser.isMessageAvailable() == true)
                		break;
                	else
                		numRead = this.requestInputStream.read(ba);
                }
                
                if (xmlParser.isMessageAvailable() == true )
                {
                    // Read all messages out of the object
                    while (xmlParser.isMessageAvailable() == true)
                    {
	                    String xmlRequest = xmlParser.getMessage();
	                    System.out.println("JMX Configuration Message from socket: " + xmlRequest);
	                    XMLProcessing.addHosts(xmlRequest);
						XMLProcessing.addServices(xmlRequest);
						String fMessage = XMLProcessing.filterMessage(xmlRequest);
						try{
							producer = new MsgProducer(fMessage);
							producer.start();
							producer.join();
							producer.unInitialize();
						}catch(Exception e){
							System.out.println("Error while sending message to groundwork queue. " + e.getMessage());
						}
	                    if (log.isDebugEnabled())
	                    	log.debug("Listener XML MSG received: " + xmlRequest);
	                    
	                    // Check for socket disconnect
	                    if (xmlRequest.indexOf(ConfigurationManager.MAINTENANCE_QUEUE) != -1
	                            && xmlRequest.indexOf(CLOSE) != -1) 
	                    {
                            // Cleanup this listener
                            uninitialize();                            
                            return;
	                    } else {
	                        synchronized (this) {

                                // write to the vectors that process the message                            	
                            	//IncommingMessage.listRequests.add(xmlRequest);
                                
                                /* 
                                 * If the request queue is overloaded stop processing requests and
                                 * give full time to process feeder data.
                                 */
                                if ( (IncommingMessage.listRequests.size() > ConfigurationManager.MAX_REQUEST_SIZE) )
                                {
                                    if (log.isInfoEnabled())
                                    	log.info("Request stack full(" + IncommingMessage.listRequests.size() + ") -- sleep for " + ConfigurationManager.THROTTLE_REQUEST_WAIT + " milliseconds");
                                    
                                    try {
                                        Thread.sleep(ConfigurationManager.THROTTLE_REQUEST_WAIT);
                                    } catch (InterruptedException ie) {
                                        log.error("Exception; Listener is throttleing. ID(" + threadId
                                                + "): Exception :" + ie);
                                    }                                    
                                }
	                        }
	                    }
	                    
	                    // Force process of next message
                    	xmlParser.processData(null);
                    }
                    
                	// Reset lastRequest timestamp
                	lastRequest = System.currentTimeMillis();
                }

                // Check if we hit a timeout
                if ((lastRequest + ConfigurationManager.THREAD_TIMEOUT_IDLE) < System.currentTimeMillis()) 
                {
                	if (log.isInfoEnabled())
                		log.info("Listener Thread ID ["+ threadId +"] timed-out. Stop listening on that thread.");                    
                    break; // Just exit
                }
                
                // Sleep for a while
                try {
                	Thread.sleep(10);
                } catch (InterruptedException ie) {
                    log.error("Exception. Listener throttleing. ID(" + threadId
                            + "): Exception :" + ie);
                }
            } catch (IOException ioe) {
                log.error("Listener ID(" + threadId + "): Exception :"
                        + ioe);
            }
        }
        
        // Check reason for stop listening
        if (log.isInfoEnabled())
        {
        	log.info("Listening on socket stopped. SocketClosed["+ serviceSocket.isClosed()+"] acceptCalls ["+ acceptCalls +"]");
        }
        
        // Cleanup
        uninitialize();
        
        if (log.isInfoEnabled())
        	log.info("Terminating Listener Thread ID ["+ threadId +"]");
    }
    
    public void shutdown ()
    {
    	this.acceptCalls = false;
    }
    
    public long getThreadId ()
    {
    	return threadId;
    }
    
    public void uninitialize()
    {
        try {
            // Close stream before sockets
        	if (requestInputStream != null)
        	{
        		requestInputStream.close();
        		requestInputStream = null;
        	}
        	
        } catch (IOException e) {
            log.error("Exception while closing socket input stream. Listener Cleanup ID(" + threadId
                    + "): Exception :" + e);
        }
        
        try {
            // Close socket
        	if (serviceSocket != null)
        	{
        		serviceSocket.close();
        		serviceSocket = null;
        	}
        	
        } catch (IOException e) {
            log.error("Exception while closing the socket. Listener Cleanup ID(" + threadId
                    + "): Exception :" + e);
        }
        
        if (this.parent != null)
        	this.parent.removeListener(this);
    }
}