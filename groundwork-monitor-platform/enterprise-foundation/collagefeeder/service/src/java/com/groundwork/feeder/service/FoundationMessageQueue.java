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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.feeder.adapter.AdapterManager;
import com.groundwork.feeder.adapter.impl.FoundationMessage;

/**
 * @author glee
 *
 */
public class FoundationMessageQueue extends FoundationListenerThread
{
	private static final String DOT = ".";
	private static final String COMMA = ",";
	private static final String MSGKEY_PREFIX = "fmd.msgkeys";
	private static final String SERIAL_MSGKEY_PREFIX = "fmd.msgserialkeys";
	
	private static final String PROP_CORE_POOL_SIZE = "fmd.executor.core.pool.size";
	private static final String PROP_MAX_POOL_SIZE = "fmd.executor.max.pool.size";
	private static final String PROP_QUEUE_SIZE = "fmd.executor.queue.size";
	private static final String PROP_KEEP_ALIVE = "fmd.executor.keep.alive";
	
	/**
	 * Constants for message processing
	 */
	private static final String SYNC_START_MSG	= "<SYNC action='start'/>";
	private static final String LOG_MESSAGE_TAG = "<LogMessage ";
	
		
	// List of messages received from dispatcher and have not gone through process
	// of determining whether they can be processed
	// Message Pipeline:
	// IncomingMessages -> MessageQueue -> Execution
	private ConcurrentLinkedQueue<FoundationMessage> _incomingMessages = 
		new ConcurrentLinkedQueue<FoundationMessage>();
	
	/* Queue for messages that need to be executed in serial -- commit messages are a good example*/
	private ConcurrentLinkedQueue<FoundationMessage> _serialIncomingMessages = 
		new ConcurrentLinkedQueue<FoundationMessage>();
	
	// Ordered list of JMS Messages that have been received and went through process
	// of determining whether it can be executed
	private ConcurrentLinkedQueue<FoundationMessage> _messageQueue = 
		new ConcurrentLinkedQueue<FoundationMessage>();			
	
	// Hashtable of message attributes which are used as keys to coordinate processing
	// and is initialized at startup
	private Hashtable<String, List<String>> _htMessageKeys = 
		new Hashtable<String, List<String>>(10);
	
	// Hashtable of message attributes which are used to identify messages which need to
	// processed serially.
	private Hashtable<String, List<String>> _htSerialMessageKeys = 
		new Hashtable<String, List<String>>(2);
	
	private AtomicBoolean bSerialProcessing = new AtomicBoolean(false);
	private FoundationRequestExecutor _requestExecutor = null;	
	
	/* Executting incoming request in serial -- Important for messages coming during a commit */
	private FoundationRequestExecutor _serialRequestExecutor = null;
	
	private boolean _isProcessing = true;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/*************************************************************************/
	/* Constructors
	/*************************************************************************/	
	
	public FoundationMessageQueue (Properties configuration, AdapterManager adapterMgr)
	{
		initialize(configuration, adapterMgr);
	}
	
	/*************************************************************************/
	/* Public Methods
	/*************************************************************************/	
	
	/**
	 * Messages will be put in two different queues:
	 * 1) LogMessage will go into the serial queue where only on thread is processing it
	 * 2) Status messages will go into queue where a multiple threads will process messages
	 * 
	 * If the sync messages arrives (<SYNC action='start'/>) the queue with status messages will be flushed
	 */
	public void processMessage(FoundationMessage foundationMessage)
	{
		if (foundationMessage == null)
			throw new IllegalArgumentException("Invalid null FoundationMessage parameter.");
				
		if (foundationMessage.getText() == null || foundationMessage.getText().length() == 0)
			throw new IllegalArgumentException("Invalid FoundationMessage parameter - Invalid text.");
		
		
		/* Dispatching the messages in different queues 
		 * 
		 * Read the first 1k max in order to decide what queue the message should go
		 */
		String messageToProcess = null;
		try {
			messageToProcess = foundationMessage.getText().substring(0, 1024);
		}
		catch(IndexOutOfBoundsException oobe)
		{
			// Message is smaller than 1k read the entire message
			messageToProcess = foundationMessage.getText();
		}
				
		/* Check for Sync message */
		if (messageToProcess.indexOf(FoundationMessageQueue.SYNC_START_MSG) != -1) {
			log.warn("Sync message received. Flush Status Messages queue that has "+ _incomingMessages.size() + " un-processed messages");
			_incomingMessages.clear();
			processingComplete(foundationMessage, false);
			return; // skip this message
		}
		
		/* check if message contains a LogMessage which requires single threaded execution of the message */
		if (messageToProcess.indexOf(FoundationMessageQueue.LOG_MESSAGE_TAG) != -1) {
			this._serialIncomingMessages.add(foundationMessage);
			
			if (log.isInfoEnabled()) {
				log.info("Log message. Will go into serial queue. Message head : " + messageToProcess + "...");
			}
		}
		else
		{
			// All other messages
			this._incomingMessages.add(foundationMessage);
			if (log.isInfoEnabled()) {
				log.info("Status message. Will go into multithreaded queue. Message head : " + messageToProcess + "...");
			}
		}		
	}
	
	@Override
	public void unInitialize()
	{
		_isProcessing = false;
	}

	public void run ()
	{
		FoundationMessage foundationMessage = null;
		FoundationMessage serialfoundationMessage = null;
		boolean bSerialMessagesAvailable = false;
		
		// Continue to process or if processing was stopped then first process all messages
		while (_isProcessing == true || _incomingMessages.isEmpty() == false)
		{
			/* Serial message processing has higher priority and will be
			 * executed before any other messages. As long messages are in the
			 * queue they will be executed first
			 */
			if ( this._serialIncomingMessages.isEmpty() == false )
			{
				bSerialMessagesAvailable = true;
				
				serialfoundationMessage = this._serialIncomingMessages.poll();
				if (serialfoundationMessage != null  )
				{	
					try {
						this._serialRequestExecutor.executeMessage(serialfoundationMessage);
						
						if (log.isDebugEnabled())
							log.debug("Executing [serial] Msg: " 
								+ ", Msg Type: " + serialfoundationMessage.getName()
								+ ", Msg ID: " + serialfoundationMessage.getID());
					} catch(Exception e)
					{
						try {
						log.error("FoundationMessageQueue:run() exception: Error" +e);	
						// Make sure message is removed from the queue.  
		    			processingComplete(serialfoundationMessage, false);
						}
						catch(Exception ee)
						{
							log.error("Serial Message Queue execution. Processing Complete failed. Continue. Error " + ee);
						}
					}
				}
			}
			else
			{
				bSerialMessagesAvailable = false;
			}
			
			/* Parallel processing of status messages */
			// Get next message
    		foundationMessage = _incomingMessages.poll();
    		/* Sleep if no messages are available in both queues */
    		if (foundationMessage == null && bSerialMessagesAvailable == false)
    		{
    			if (log.isDebugEnabled())
    				log.debug("No messages to process available sleep");
    			
				try {			
					Thread.sleep(100);
				}
				catch (Exception e)
				{
					log.error(e);
				}
				
				continue;
			}            
    		if (foundationMessage != null)
    		{
	    		try {
					// Check to see if the message can be processed.  If it cannot b/c of a message currently being
					// processed then the canProcess () call will set up the internal message queues appropriately.
	    			/* 
	    			 * Update: Dec 2, 2009
	    			 * The sync process makes sure that all objects are in place and messages are updates only. For this
	    			 * reason dependency check can be turned off
	    			*/
					if ( /*canProcess(foundationMessage) ==*/ true)
					{	
						if (this._isProcessing == true)
						{
							if (log.isInfoEnabled() )
								log.info("Post message to executor...");
							_requestExecutor.executeMessage(foundationMessage);
						}
						else
						{
							if (log.isInfoEnabled() )
								log.info("Remove message from internal queue (shutdown mode)");
							synchronized (_messageQueue)
							{							
								_messageQueue.remove(foundationMessage);
							}
						}
						
						if (log.isDebugEnabled())
							log.debug("Executing [parallel] Msg: " 
								+ ", Msg Type: " + foundationMessage.getName()
								+ ", Msg ID: " + foundationMessage.getID());
						
						if (log.isDebugEnabled())
							log.debug("Message being executed, ID: [" 
									+ ", Msg Type: " + foundationMessage.getName()
									+ ", Msg ID: " + foundationMessage.getID()
									+ "]");	        					
					}
	    			else if (log.isDebugEnabled())
	    			{
	    				log.debug("Message queued: [" 
	    						+ ", Msg Type: " + foundationMessage.getName()
								+ ", Msg ID: " + foundationMessage.getID()
								+ "]");
	    			} 
					
					/* Give time to execute message in the threadpool */
					try {			
						Thread.sleep(20);
					}
					catch (Exception e)
					{
						log.error(e);
					}
	    		}
					
	    		catch (Exception e)
	    		{
	    			// Make sure message is removed from the queue.  
	    			processingComplete(foundationMessage, false);    			
	    			log.error(e);
	    		}
    		}
		}
		
		// Clean up thread pool
		if (_requestExecutor != null)
		{
			if (log.isInfoEnabled() )
								log.info("Call un-initialize executor...");
			_requestExecutor.unInitialize();
		}
		
		if (this._serialRequestExecutor != null)
		{
			this._serialRequestExecutor.unInitialize();
		}
	}
	
	/*************************************************************************/
	/* Protected Methods
	/*************************************************************************/
//private static int msgProcessedCount = 0;
	/**
	 * Called when message has completed processing.
	 */
	protected void processingComplete (FoundationMessage foundationMessage, boolean bSucceeded)
	{
		if (log.isDebugEnabled())
			log.debug("Message processing complete for msg [" + foundationMessage.getText() + "]");	
		/* 
		 * Update: Dec 2, 2009
		 * The sync process makes sure that all objects are in place and messages are updates only. For this
		 * reason dependency check can be turned off
		*/
		if (false){
			// Remove from messages queue		
			synchronized (_messageQueue)
			{							
				_messageQueue.remove(foundationMessage);
			}		
		}
		try {
			// Acknowledge message to remove from destination
			foundationMessage.acknowledge();
		
			if (log.isDebugEnabled())
				log.debug("Msg Finished: " 
					+ "Msg Type: " + foundationMessage.getName()
					+ ", ID: " + foundationMessage.getID()
					+ ", Elapsed Time (ms): " + (System.currentTimeMillis() - foundationMessage.getTimestamp())
					+ ", Succeeded: " + bSucceeded);
								
			if (log.isDebugEnabled())
				log.debug("Time elapsed since provider sent message and processing finished (ms): "
						+ (System.currentTimeMillis() - foundationMessage.getTimestamp()));		
		}
		catch (Exception e)
		{
			log.error("Processing complete: Acknowledge failed. Continue");
		}
		
		/* 
		 * Update: Dec 2, 2009
		 * The sync process makes sure that all objects are in place and messages are updates only. For this
		 * reason dependency check can be turned off
		*/
		if (false) {
			// Go through each dependent message an see if it can be processed			
			synchronized (foundationMessage)
			{			
				if (foundationMessage.hasDependentMessages())
				{
					Queue<FoundationMessage> dependentQueue = foundationMessage.getDependentMessages();
					if (dependentQueue != null)
					{				
						FoundationMessage dependentMsg = null;
						while (dependentQueue.isEmpty() == false)
						{
							dependentMsg = dependentQueue.poll();
							
							// Decrement completed message from pending queue
							// If there are no more pending messages then we execute the message
							if (dependentMsg.decrementPendingCount() == 0)
							{					
								try {
									if (this._isProcessing == true) {
									if (log.isInfoEnabled() )
										log.info("Post dependent message to executor...");
									_requestExecutor.executeMessage(dependentMsg);
									}
									else
									{
										if (log.isInfoEnabled() )
											log.info("Remove dependent message from internal queue");
										synchronized (_messageQueue)
										{							
											_messageQueue.remove(dependentMsg);
										}	
									}
								}
								catch (Exception e)
								{
					    			// Make sure message is removed from the queue and all dependent messages
									// are processed.
									log.error("Post dependent message  executor exception. Error: " +e);
									processingComplete(dependentMsg, false);
									
								}
												
								if (log.isDebugEnabled())
									log.debug("Executing Msg (Dependent): " 
										+ ", Msg Type: " + dependentMsg.getName()
										+ ", Msg ID: " + dependentMsg.getID());
																
							}					
						}
					}
				}
			} // End synchronized (foundationMessage)
		} // Dependency checking
					
		if (log.isDebugEnabled())
			log.debug("Message acknowledged, ID: [" + foundationMessage.getID() + "]");		
	}
	
	/**
	 * Return the total number of messages in the incoming queue and the message queue. 
	 * @return
	 */
	public int getMessageQueueCount ()
	{
		return _messageQueue.size() + _incomingMessages.size();
	}		

	/*************************************************************************/
	/* Private Methods
	/*************************************************************************/
	
	/**
	 * Check to see if there is a message being processed which the specified message
	 * must wait for before being processed.  If there is such a message then
	 * the message will be placed in queue to be processed once the message it is waiting
	 * for is finished.
	 * 
	 * @param processedMsg
	 * @return
	 */
	private boolean canProcess(FoundationMessage message)
	{	
		if (message == null)
			throw new IllegalArgumentException("Invalid null FoundationMessage parameter.");
		
		String msgName = message.getName();
		if (log.isInfoEnabled())
			log.info("canProcess Message Name [" + msgName + "]");
		
		List<String> serialKeyList = _htSerialMessageKeys.get(msgName);
				
		// Lookup Message Key Attributes and clone
		// the call to hasPropertyValueMatch() will remove keys as they are found.
		// Once all key matches exist in message currently being processed then
		// we can stop looking for matches b/c once the processed messages are complete
		// there will be no collisions.
		List<String> keyList = _htMessageKeys.get(msgName);
		List<String> clonedList = null;
		if ((keyList == null || keyList.isEmpty()) && (serialKeyList == null || serialKeyList.isEmpty()))
		{
			// No keys defined so we can process immediately.
			// Also, no need to add it to _messageQueue b/c it has not keys to coordinate
			return true; 
		}
		else 
		{
			clonedList = new ArrayList<String>(keyList.size());
			clonedList.addAll(keyList);
		}
		
		// Go through each key and find messages that are currently be processed
		boolean bCanProcess = true;					
		
		// Go through each message that has been received
		synchronized (_messageQueue)
		{					
			Iterator<FoundationMessage> itMsgs = _messageQueue.iterator();
			FoundationMessage msgInQueue = null;
			int matchValue = -1;
			while (itMsgs.hasNext())
			{
				msgInQueue = itMsgs.next();
				
				synchronized (msgInQueue) 
				{
					// Go through message attribute values to find match (even bulk) messages
					// Once a match for a key is found it is removed from the clonedList
					// Once all keys have been found we can stop looking for message dependencies
					// unless we have a message that needs to be serially processed (i.e. NAGIOS_LOG with consolidation)
					if (clonedList != null && clonedList.size() > 0)
					{
						matchValue = msgInQueue.hasPropertyValueMatch(clonedList, message, true);
						if (matchValue >= 0)
						{
							// Add message to increment pending count and dependent message queue 
							// for the message currently being processed and the message
							// that must wait for the message to complete, respectively
							msgInQueue.addDependentMessage(message);
							message.incrementPendingCount();
							
							bCanProcess = false;	
							
							// If all keys match then we don't have to worry about other messages b/c 
							// all entities will be created with the completion of the pending message
							// and therefore synchronization is not required.
							// NOTE:  With the above last commit wins.  This is a problem with LogMessage
							// consolidation b/c the MsgCount may be stale with two or more LogMessages
							// being processed concurrently.  This issue will taken care of by the serial key
							// list processing.
							if (clonedList.size() == 0 && (serialKeyList == null || serialKeyList.size() == 0))
							{					
								break;
							}
							else 
							{
								continue; // Continue looking for serial processed messages
							}
						}							
					} 
					
					// Once we have coordinated with other messages - we need to coordinate serially processed
					// messages.  In effect a chain is created.  We look for the first message of the 
					// same type with the same serial key values and add the incoming message as a dependent.
					if (msgName.equalsIgnoreCase(msgInQueue.getName()) &&
							(msgInQueue.hasDependentMessages() == false) &&
							((msgInQueue.hasPropertyValueMatch(serialKeyList, message, false)) == 1))
					{
						msgInQueue.addDependentMessage(message);
						message.incrementPendingCount();
						
						bCanProcess = false;
						break;
					}
				} // End synchronized (msgInQueue)
								
			}
			
			// Add to messages queue after so it won't be included.
			_messageQueue.add(message);			
		} // End synchronized (_messageQueue)
				
		return bCanProcess;
	}	
	
	private void initialize (Properties configuration, AdapterManager adapterMgr)
	{
		if (adapterMgr == null)
			throw new IllegalArgumentException("Unable to Initialize.  Invalid null AdapterManager parameter.");

		// Request Executor Configuration
		int corePoolSize = 10;
		int maxPoolSize = 100;
		int queue_size = 1000;
		int keepAlive = 60;
		
		if (configuration != null)
		{
			// Build up message key list and serial message key list
			String propName = null;
			String value = null;
			String key = null;
			StringTokenizer tokenizer = null;					
			List<String> messageKeyList = null;
			int pos = -1;
			
			Enumeration enumPropNames = configuration.propertyNames();
			while (enumPropNames.hasMoreElements())
			{
				propName = (String)enumPropNames.nextElement();
				
				if (propName.startsWith(MSGKEY_PREFIX) ||
				    propName.startsWith(SERIAL_MSGKEY_PREFIX))
				{
					value = configuration.getProperty(propName, null);
					if (value == null)
						continue;
					
					pos = propName.lastIndexOf(DOT);
					String messageType = propName.substring(pos + 1);
					
					tokenizer = new StringTokenizer(value, COMMA);
					if (tokenizer == null)
						continue;
					
					// Add each key
					messageKeyList = new ArrayList<String>(tokenizer.countTokens());					
					while (tokenizer.hasMoreTokens())
					{
						key = tokenizer.nextToken();
		
						messageKeyList.add(key);						
					}

					if (propName.startsWith(MSGKEY_PREFIX))
					{
						_htMessageKeys.put(messageType.toUpperCase(), messageKeyList);
						
						if (log.isDebugEnabled())
							log.debug("FMD Message Keys - Type: " + messageType 
									+ ", Keys: " + messageKeyList);						
					}
					else {
						_htSerialMessageKeys.put(messageType, messageKeyList);
						
						if (log.isDebugEnabled())
							log.debug("FMD Serial Message Keys - Type: " + messageType 
									+ ", Keys: " + messageKeyList);
					}
				}
			}
			
			// Get Request Executor configuration
            String propValue = configuration.getProperty(PROP_CORE_POOL_SIZE, "10").trim();            
            corePoolSize = Integer.parseInt(propValue);
            
            propValue = configuration.getProperty(PROP_MAX_POOL_SIZE, "100").trim();            
            maxPoolSize = Integer.parseInt(propValue);
            
            propValue = configuration.getProperty(PROP_QUEUE_SIZE, "1000").trim();            
            queue_size = Integer.parseInt(propValue);
            
            propValue = configuration.getProperty(PROP_KEEP_ALIVE, "60").trim();            
            keepAlive = Integer.parseInt(propValue);            	
		}
		
        // Create Thread Pool        
		_requestExecutor = new FoundationRequestExecutor("FoundationDispatcherProcessThread",
        													this,
															adapterMgr, 
															corePoolSize,
															maxPoolSize,
															keepAlive,
															queue_size);     
		
		// Create a serial Thread pool
		this._serialRequestExecutor = new FoundationRequestExecutor("FoundationDispatcherSerialQueue",
																		this,
																		adapterMgr,
																		1 /*core pool size*/,
																		1 /*max pool size*/,
																		keepAlive,
																		queue_size);
	}	
}
