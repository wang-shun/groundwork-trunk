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

package org.groundwork.foundation.jms.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationReader;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author rogerrut
 *
 * Created: Apr 6, 2007
 */
public class JMSDestinationReaderImpl implements JMSDestinationReader {
	
	/** Enable log4j for JMSServer class */
	private Log log = LogFactory.getLog(this.getClass());
	
	private AtomicBoolean initialized = new AtomicBoolean(false);
	
    private ConnectionFactory	cnxF = null;
    private Destination			dest = null;
    private Connection			cnx = null;
    private Session				session = null;
    
    private MessageConsumer messageConsumer = null;  
    private JMSDestinationInfo destInfo = null;

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSDestinationReader#commit()
	 */
	public void commit() throws FoundationJMSException {
		if (this.initialized.get() == false)
			throw new FoundationJMSException("Call initialize() before calling any other method");
		
		try {
			this.session.commit();
		}
		catch(JMSException je)
		{
			throw new FoundationJMSException("Error commit reads from destination[" 
					+ this.destInfo.getDestinationName() + "]. Error: " + je);
		}
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSDestinationReader#initialize(org.groundwork.foundation.jms.JMSDestinationInfo)
	 */
	public void initialize(JMSDestinationInfo destInfo, 
						   boolean bTransacted, 
						   int acknowledgeMode, 
						   MessageListener listener)
	throws FoundationJMSException 
	{
		if (destInfo == null)
			throw new FoundationJMSException("JMSDestinationInfo can't be null. DestinationReader can't be initialized");
			
		this.destInfo = destInfo;
		
		// Connect to destination	
		Context ictx = null;
		try {
			Hashtable<String, String> htJndiProperties = null;	
			if (destInfo.getContextFactory() != null && destInfo.getHost() != null && destInfo.getPort() != null)
			{
				htJndiProperties = new Hashtable<String, String>(3);
				htJndiProperties.put("java.naming.factory.initial", destInfo.getContextFactory());
				htJndiProperties.put("java.naming.provider.url", "remote://" + destInfo.getHost()
						+ ":" + destInfo.getPort());
			}
			
			ictx = new InitialContext(htJndiProperties);
            
            cnxF = (ConnectionFactory) ictx.lookup(destInfo.getServerContext());
            dest = (Destination) ictx.lookup(destInfo.getDestinationName());            

            cnx = cnxF.createConnection(destInfo.getAdminUser(), destInfo.getAdminCredentials());
            session = cnx.createSession(bTransacted, acknowledgeMode);
                                   
            messageConsumer = session.createConsumer(dest);                      
            
            if (listener != null)
            	messageConsumer.setMessageListener(listener);
            
            cnx.start();
	    } 
		catch (NamingException ne) 
	    {
            throw new FoundationJMSException("Couldn't create Context " + destInfo.getServerContext()
                            + " and Destination " + destInfo.getDestinationName(), ne);
	    }
	    catch (JMSException jmse) 
	    {
            throw new FoundationJMSException("Couldn't create Context " + destInfo.getServerContext()
                    + " and Destination " + destInfo.getDestinationName(), jmse);            
	    }
	    finally {
	    	if (ictx != null)
	    	{
	    		try {
	    			ictx.close();
	    		}
	    		catch (Exception e)
	    		{
	    			log.error(e);
	    		}	    		
	    	}
	    }
		    
	    /* Success */
	    this.initialized.set(true);
	}	
	
	public void unInitialize() throws FoundationJMSException
	{
		this.initialized.set(false);
		try {
			if (this.messageConsumer != null)
				this.messageConsumer.close();
			
			if (this.session != null)
				this.session.close();
			
			if (this.cnx != null)
				this.cnx.close();
		} catch(JMSException je)
		{
			throw new FoundationJMSException("Failed to close DestinationReader. Error: " + je);
		}
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSDestinationReader#readMsg()
	 */
	public Message readMsg() throws FoundationJMSException {
		if (this.initialized.get() == false)
			throw new FoundationJMSException("Call initialize() before calling any other method");
		
		try {
			return this.messageConsumer.receive();
		}
		catch(JMSException je)
		{
			throw new FoundationJMSException("Error reading message destination [" 
					+ this.destInfo.getDestinationName() +". Error: " + je);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSDestinationReader#readMsg()
	 */
	public Message readMsgNoWait() throws FoundationJMSException {
		if (this.initialized.get() == false)
			throw new FoundationJMSException("Call initialize() before calling any other method");
		
		try {
			return this.messageConsumer.receiveNoWait();
		}
		catch(JMSException je)
		{
			throw new FoundationJMSException("Error reading message destination [" 
					+ this.destInfo.getDestinationName() +". Error: " + je);
		}
	}
	
	public Message readMsg(int timeout) throws FoundationJMSException {
		if (this.initialized.get() == false)
			throw new FoundationJMSException("Call initialize() before calling any other method");
		
		try {
			return this.messageConsumer.receive(timeout);
		}
		catch(JMSException je)
		{
			throw new FoundationJMSException("Error reading message destination ["
					+ this.destInfo.getDestinationName(), je);
		}
	}	
	
	public String readTextMsg() throws FoundationJMSException
	{
		if (this.initialized.get() == false)
			throw new FoundationJMSException("Call initialize() before calling any other method");
		
		String str= null;
		try
		{
			Message msg = this.readMsg();
			if (msg instanceof TextMessage)
            {
				str = ((TextMessage) msg).getText();
            }
		}
		catch(FoundationJMSException fje)
		{
			throw new FoundationJMSException("Error reading Text message from destination.", fje);
		}
		catch (JMSException je)
		{
			throw new FoundationJMSException("Error reading Text message from destination.", je);
		}
		
		return str;
	}
}
