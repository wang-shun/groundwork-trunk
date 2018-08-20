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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.feeder.adapter.AdapterManager;
import com.groundwork.feeder.adapter.impl.FoundationMessage;

public class FoundationRequestExecutor implements RejectedExecutionHandler 
{
	// Default Queue size if provided queue size is less than or equal to zero
	private static final int DEFAULT_QUEUE_SIZE = 1000;	
	
	// Name of thread used for logging
	private String _name = null;
	
	private AdapterManager _adapterMgr = null;
		
	private FoundationMessageQueue _ownerQueue = null;
	
	// Thread Pool
	private ThreadPoolExecutor _executor = null;	
	
	/* Enable log for log4j */
    private Log log = LogFactory.getLog(this.getClass());
		    
	protected FoundationRequestExecutor (String name,
										FoundationMessageQueue ownerQueue,
										AdapterManager adapterMgr,
										int corePoolSize,
										int maxPoolSize,
										int keepAlive, // In Seconds
										int queueSize)
	{
		if (adapterMgr == null)
			throw new IllegalArgumentException("Invalid null AdapterManager parameter.");

		_name = name;
		_adapterMgr = adapterMgr;
		_ownerQueue = ownerQueue;
		
		if (queueSize <= 0)
			queueSize = DEFAULT_QUEUE_SIZE;
		
		// Create thread pool executor
		_executor = new ThreadPoolExecutor(corePoolSize, 
				maxPoolSize, 
				keepAlive, 
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(queueSize, true),
				this);		
	}
	
	protected FoundationRequestExecutor (String name,
			AdapterManager adapterMgr,
			int corePoolSize,
			int maxPoolSize,
			int keepAlive, // In Seconds
			int queueSize) 
	{
		this(name, null, adapterMgr, corePoolSize, maxPoolSize, keepAlive, queueSize);
	}
	
	protected void executeMessage (FoundationMessage message)
	{
		String msgText = (message == null) ? null : message.getText();		
		if (msgText == null || msgText.length() == 0)
		{
			throw new CollageException(
					"FoundationRequestExecutor - Unable to execute message invalid FoundationMessage - [" +
					message + "]");
		}
		
       	if (log.isDebugEnabled() == true) 
        {
    		log.debug("Received XML request ["+ message +"]" );
       		log.debug("Number of Active Executor Threads = " + _executor.getActiveCount());
       		log.debug("Number of Executor Tasks = " + _executor.getTaskCount());                            	
        }

       	try
        {
        	_executor.execute(new ProcessMessage(_ownerQueue, _adapterMgr, message));
        	
        } 
        catch (Exception e)
        {
            // Report an error an continue
        	log.error("Collage Adapter. Exception while processing request ["+ message +"] Error: " + e);
        	
        	throw new CollageException(
					"FoundationRequestExecutor - Exception occurred executing message - [" +
					message + "]", e);
        }
		
       	if (log.isDebugEnabled() == true) 
        {
        	log.debug("Foundation Message added to FoundationRequestThread - " + _name);
        }			
	}	
    
	
	/**
	 * getActiveCount
	 * 
	 * @return int returns number of threads actively executing tasks
	 */
	public int getActiveCount() {
		return _executor.getActiveCount();
	}
	 
    /**
     * Called if executor cannot execute the task because all threads are processing active requests
     * and the queue is full
     */
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) 
	{
		log.warn("Message Executor [" + _name + "] Cannot execute task b/c all threads are active and the queue is full.");

		try {

			// Note:  This exception call stalls all execution - In the run(), we make sure there is room in the queue before
			// we execute so this handler should never be called.
			Thread.sleep(20);
			
			// Try to execute the message again
    		// Note:  We could get into an endless loop if the task cannot be executed.
    		executor.execute(r);	    		
		}
		catch (Exception e)
		{
            // Report an error an continue
        	log.error("Collage Adapter. Exception while re-executing request Error: " + e);    
        	
        	throw new RejectedExecutionException(e);
		}
	}        
	
    /** Shutdown system  **/
    public void unInitialize()
    {
    	if (log.isInfoEnabled())
    		log.info("Shutdown FoundationRequestThread - [" + _name + "]");
    	
    	try {
    		_executor.shutdown();
    		
    		// Note:  We are waiting no more than 5 minutes for the tasks to complete
    		_executor.awaitTermination(300, TimeUnit.SECONDS);    		
    	}
    	catch (Exception e)
    	{
    		log.error("Error occurred shutting down FoundationRequestThread Executor.", e);
    	}
   
    }
    
    /**
     * Runnable class used to process messages on separate thread.  
     *
     */
    private class ProcessMessage implements Runnable
    {
    	FoundationMessageQueue _fmq = null;
		AdapterManager _adapterMgr = null;
		FoundationMessage _foundationMessage = null;
    	
    	public ProcessMessage (FoundationMessageQueue fmq, AdapterManager adapterMgr, FoundationMessage foundationMsg)
    	{
    		if (adapterMgr == null)
    		{
    			throw new IllegalArgumentException("Invalid null AdapterManager Parameter.");
    		}
    		
    		if (foundationMsg == null || foundationMsg.getText() == null || foundationMsg.getText().length() == 0)
    		{
    			throw new IllegalArgumentException("Invalid null / empty FoundationMessage parameter.");
    		}
    		
    		_fmq = fmq;
    		_adapterMgr = adapterMgr;
    		_foundationMessage = foundationMsg;
    	}

    	public void run() 
    	{
            boolean bSuccess = true;
            String msgName = null;
            
            try 
            {
            	msgName = _foundationMessage.getName();
            	
            	// TODO:  We may want make a clone or package in another class before passing
            	// to the adapters.  This would avoid synchronization issues and shield /protect the
            	// FoundationMessage from manipulation in the adapter.
            	_adapterMgr.process(msgName, _foundationMessage);
            }
            catch (Exception e)
            {
            	log.error("Foundation Request Executor - Exception while processing task [" 
            				+ msgName + "] Error: " + e);
            	bSuccess = false;
            }
            
            // Notify Foundation Queue that we are done processing the message
            if (_fmq != null)
            {
            	_fmq.processingComplete(_foundationMessage, bSuccess);
            }
		}
    }    
}
