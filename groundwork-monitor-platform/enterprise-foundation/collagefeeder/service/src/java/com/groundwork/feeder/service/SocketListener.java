/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2008  GroundWork Open Source Solutions info@groundworkopensource.com

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

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * SocketListenerThread
 * 
 * @author <a href="mailto:rruttimann@groundworkopensource.com"> Roger Ruttimann </a>
 * @version $Id: SocketListenerThread.java,v 1.1 2005/04/16 00:26:32 rogerrut
 *                Exp $
 */
public class SocketListener implements Runnable
{
	// String constants
	private static final String CLOSE = "close";
	private volatile Thread blinker;
    /*
     * Thread member variables  
     */
    private Socket serviceSocket = null;

    private DataInputStream requestInputStream = null;    

    private ProcessFeederData.listenerMainThread parent = null;
    
    boolean acceptCalls = true;
    
    private long threadId = 0;
    
	/* Enable log for log4j */
    private Log log = LogFactory.getLog(this.getClass());
        
    public SocketListener(Socket newClient, ProcessFeederData.listenerMainThread parent) 
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
                + ") bound to PORT["+this.serviceSocket.getPort()+"]. Waiting for input...\n");
                
        // Parser for incoming message streams
        XMLFeedParser xmlParser = new XMLFeedParser();
               
        int numRead = 0;
        byte[] ba = new byte[ProcessFeederData.BLOCK_READ_SIZE];
        
        // Listens until client closes connection
        // cpora -- added the check for parent.serviceIsListening.
        // When the parent decides to die, all the children(the socketListeners), 
        // have to flush out the inputRequestStream through the xmlParser, before dying.
        // The recovered xml messages are written to the JMS queue. 
        while (serviceSocket != null && serviceSocket.isClosed() == false && acceptCalls == true && parent.serviceIsListening) 
        {            
        	
            // Read from the socket. Parse out XML messages < />
            try {   
                // Read until we get a message
            	 
                numRead = this.requestInputStream.read(ba);  
                
                /* -1 indicates that the end of the stream is reached */
                if (numRead == -1)
                {
                	if (log.isInfoEnabled())
                		log.info("End of socket stream reached. Stop reading on PORT["+this.serviceSocket.getPort()+"]");
                	break; /*Break out of the while loop */
                }
                
                if (log.isInfoEnabled())
                	log.info("SocketListener.run() Read size ["+ numRead + "] PORT["+this.serviceSocket.getPort()+"]");
                String strDataRead = null;
                while (numRead > 0)
                {  
                	// Reset lastRequest timestamp since we got data
                	lastRequest = System.currentTimeMillis();
                	strDataRead = new String(ba, 0, numRead);
                	if (log.isInfoEnabled())
                     	log.info("SocketListener.run() Read size ["+ strDataRead.length()+"] PORT ["+this.serviceSocket.getPort()+"] Content ["+ strDataRead + "]");
                	xmlParser.processData(strDataRead);
                	 
                	// Message exists so we process it
                	if (xmlParser.isMessageAvailable() == true)
                		break;
                	else
                	{
                		numRead = this.requestInputStream.read(ba);
                		if (log.isInfoEnabled())
                         	log.info("SocketListener.run() Read size ["+ numRead + "] PORT["+this.serviceSocket.getPort()+"]");
                	}
                }
                
                if (xmlParser.isMessageAvailable() == true )
                {
                    // Read all messages out of the object
                    while (xmlParser.isMessageAvailable() == true)
                    {
	                    String xmlRequest = xmlParser.getMessage();
	                    
	                    if (log.isInfoEnabled())
	                    	log.info("Listener XML MSG received: " + xmlRequest);
	                    
	                    // Check for socket disconnect
	                    if (xmlRequest.indexOf(ProcessFeederData.MAINTENANCE_QUEUE) != -1
	                            && xmlRequest.indexOf(CLOSE) != -1) 
	                    {
                            // Cleanup this listener
                            //uninitialize();     
                            //return;
	                    	/* Stop gracefully */
	                    	this.acceptCalls = false;
	                    } else {
	                        synchronized (this) {

                                // write to the vectors that process the message                            	
                            	ProcessFeederData.listRequests.add(xmlRequest);
                                
                                /* 
                                 * If the request queue is overloaded stop processing requests and
                                 * give full time to process feeder data.
                                 */
                                if ( (ProcessFeederData.listRequests.size() > ProcessFeederData.MAX_REQUEST_SIZE) )
                                {
                                    if (log.isInfoEnabled())
                                    	log.info("Request stack full(" + ProcessFeederData.listRequests.size() + ") -- sleep for " + ProcessFeederData.THROTTLE_REQUEST_WAIT + " milliseconds");
                                    
                                    try {
                                        Thread.sleep(ProcessFeederData.THROTTLE_REQUEST_WAIT);
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
                if ((lastRequest + ProcessFeederData.THREAD_TIMEOUT_IDLE) < System.currentTimeMillis()) 
                {
                	if (log.isInfoEnabled())
                		log.info("Listener Thread ID ["+ threadId +"] timed-out. Stop listening on that thread.");                    
                    break; // Just exit
                }
                
                // Sleep for a while
                try {
                	/* Allow other threads to process before do next read attempt
                	 * Pause thread for 300ms
                	 */
                	Thread.sleep(300);
                } catch (InterruptedException ie) {
                    log.error("Exception. Listener throttleing. ID(" + threadId
                            + "): Exception :" + ie);
                }
            } catch (IOException ioe) {
                log.error("Listener ID(" + threadId + "): Exception :"
                        + ioe);
                // JIRA GWMON-4613 -- When the socket was reset we ended up in an infinite loop trying to reuse the same socket which was broken.
                // Terminate listening on this socket. Something went wrong. Let the connection pool create a new clean socket connection
                // Stop accepting calls and let the thread cleanup itself.
                acceptCalls = false;
                // Signal a bad error to the top level
                parent.setFlagErrorInSocket();
                log.error("Closing listener thread ID(" + threadId +") Send a signal a low level exception to System. If more than one thread run into the same exception the server socket will be restarted to repair." );
            }
        }
        
        // Check reason for stop listening
        if (log.isInfoEnabled())
        {
        	log.info("Listening on socket stopped. SocketClosed["+ serviceSocket.isClosed()+"] acceptCalls ["+ acceptCalls +"] PORT["+this.serviceSocket.getPort()+"]");
        }
        
        // Read what is available from input buffer and make sure everything is processed. Use the existing XML Parser class since it could
        // contain partial messages
        this.flushBuffer(xmlParser);
        
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
    	if (log.isInfoEnabled())
    		log.info("Entering unInitilaize()");
    	Thread thisThread = Thread.currentThread();
    	// cpora -- if the system is undergoing a forced shutdown
    	// then flush all remaining messages and write them to the JMS persistence store.
    	//flushBuffer();
 
        try {
            // Close socket
        	//if (serviceSocket != null) -- cpora -- setting the serviceSocket to null will cause errors
        	// whenever serviceSocket.close() is called, which would be equivalent to null.close()
        	if (serviceSocket!= null && !serviceSocket.isClosed())
        	{
        		log.info("Close socket attached to Thread ID(" + threadId+ " PORT["+this.serviceSocket.getPort()+"]");
        		serviceSocket.close();
        	}
        	
        } catch (IOException e) {
            log.error("Exception while closing the socket. Listener Cleanup ID(" + threadId
                    + "): Exception :" + e);
        }
 
        if (this.parent != null)
        {
			//ProcessFeederData.socketListeners.remove(this);
			parent.removeListener(this);
        }
    }
    
    

    
    public void flushBuffer(XMLFeedParser xmlParser)
    {
    	log.info("Socket Listener shutdown. Read socket buffer. PORT["+this.serviceSocket.getPort()+"]");
    	
    	// cpora -- flush the requestInputStream (the input buffer used by the socket to
    	// write incoming data).
         try {
             // Close stream before sockets
         	if (requestInputStream != null)
         	{
                 long lastRequest = System.currentTimeMillis();
                 // Parser for incoming message streams
                 //XMLFeedParser xmlParser = new XMLFeedParser();
         		/*
         		 * We flush out the buffer
         		 */
         		int numRead = 0;
                 byte[] ba = new byte[ProcessFeederData.BLOCK_READ_SIZE];
                 numRead = this.requestInputStream.read(ba);  
                 if (log.isInfoEnabled())
                 	log.info("SocketListener.flushBuffer() Read size ["+ numRead + "] PORT["+this.serviceSocket.getPort()+"]");
                     String strDataRead = null;
                 while (numRead > 0)                   
                 {                     	
                 	// Reset lastRequest timestamp since we got data                   	
                 	lastRequest = System.currentTimeMillis();  
                 	
                 	strDataRead = new String(ba, 0, numRead);
                	if (log.isInfoEnabled())
                         log.info("SocketListener.flushBuffer() Read size ["+ strDataRead.length()+"] PORT ["+this.serviceSocket.getPort()+"] Content ["+ strDataRead + "]");

                 	xmlParser.processData(strDataRead);
                     	                    	
                 	// Message exists so we process it                    	
                 	if (xmlParser.isMessageAvailable() == true) {                   		
                 		this.processMessage(xmlParser);
                 		numRead = this.requestInputStream.read(ba);
                 		if (log.isInfoEnabled())
                         	log.info("SocketListener.flushBuffer() Read size ["+ numRead + "] PORT["+this.serviceSocket.getPort()+"]");
                 	}
                 	else
                 	{
                 		numRead = this.requestInputStream.read(ba);
                 		if (log.isInfoEnabled())
                         	log.info("SocketListener.flushBuffer() Read size ["+ numRead + "] PORT["+this.serviceSocket.getPort()+"]");
                 	}
                 }
                 log.info("Thread shutdown read all data from socket");
                 
                 // Last Processing
                 xmlParser.processData(null);
                 this.processMessage(xmlParser);
           	}
         	
         } catch (IOException e) {
             log.error("Exception while closing socket input stream. Listener Cleanup ID(" + threadId
                     + "): Exception :" + e);
         }
    }
    
    private boolean processMessage(XMLFeedParser xmlParser)
    {
    	while (xmlParser.isMessageAvailable() == true)
     	{   	                    
     		String xmlRequest = xmlParser.getMessage();   	                    
     		    	                    
     		if (log.isInfoEnabled())    	                    	
     			log.info("Flush Buffer -- Listener XML MSG received: " + xmlRequest);    	                        	                    
           
     			synchronized (this) {    
     				// write to the vectors that process the message                            	                                	
     				ProcessFeederData.listRequests.add(xmlRequest);                                                                        
     				/*                                      
                     * If the request queue is overloaded stop processing requests and
                     * give full time to process feeder data.
                          
                     */                                    
     				if ( (ProcessFeederData.listRequests.size() > ProcessFeederData.MAX_REQUEST_SIZE) )                                 
     				{                                       
     					if (log.isInfoEnabled())
     						log.info("Request stack full(" + ProcessFeederData.listRequests.size() + 
     								") -- sleep for " + ProcessFeederData.THROTTLE_REQUEST_WAIT + " milliseconds");                                                                                
     					try {                                            
     						Thread.sleep(ProcessFeederData.THROTTLE_REQUEST_WAIT);                                        
     					} catch (InterruptedException ie) {                                            
     						log.error("Exception; Listener is throttleing. ID(" + threadId
                                         + "): Exception :" + ie);                                     
     					}                                                                    
     				}   	                    
     			}   	                
     		//}
             // Force process of next message
             xmlParser.processData(null);
         }
    	return true;
    }
    
    
}