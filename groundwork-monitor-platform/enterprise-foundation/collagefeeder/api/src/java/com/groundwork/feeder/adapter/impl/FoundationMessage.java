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
package com.groundwork.feeder.adapter.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.groundwork.collage.CollageCommand;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.util.AdapterUtil;

/**
 * Wraps JMS Message or a regular xml request string.
 *
 */
public class FoundationMessage
{   
	private static final String ATTRIBUTE_ADAPTER = "AdapterType";
	
	// Hold on to doc builder factory instance - Since its not thread safe 
	// we synchronize access
	private static DocumentBuilderFactory DOC_BUILDER_FACTORY =
		DocumentBuilderFactory.newInstance();
	
	// Document (DOM) representation of this message
	private Document _document = null;
	
    // JMS Text Message and Session used to retrieve the message
	private TextMessage _msg = null;
	private Session _session = null;
	
	// Incoming xml request if not JMS
	private String _xmlRequest = null;
	
	// Message Name (e.g. SYSTEM_CONFIG, COLLAGE_LOG, etc)
	// This actually refers to the adapter type
	private String _msgName = null;
	
	private List<CollageCommand> _commands;
	
	private List<Hashtable<String, String>> _attributes = null;

	// Hashtable of distinct attributes used to determine attribute matches
	private Hashtable<String, Vector<String>> _distinctAttributes = null;
	
	/**
	 * Atomic integer of number of pending messages
	 */
	private AtomicInteger _pendingMessageCount = new AtomicInteger(0);
	
	/**
	 * Ordered list of messages that this message has waiting for its completion.
	 */
	private ConcurrentLinkedQueue<FoundationMessage> _dependentMessages = 
		new ConcurrentLinkedQueue<FoundationMessage>();
	
	/**
	 * Time instance was created
	 */
	private long _timeCreated = System.currentTimeMillis();
	
	// Log
	private static Log log = LogFactory.getLog(FoundationMessage.class);	

	/*************************************************************************/
	/* Constructors
	/*************************************************************************/	
	
	/**
	 * TextMessage Constructor - Currently only supporting text messages
	 * @param msg
	 */
	public FoundationMessage (Session session, TextMessage msg)
	{
		if (session == null)
			throw new IllegalArgumentException("Invalid null Session parameter.");

		if (msg == null)
			throw new IllegalArgumentException("Invalid null TextMessage parameter.");
		
		_session = session;
		_msg = msg;
		
		try {
			// validate 
			validateMessage(msg.getText());
		} 
		catch (JMSException e)
		{
			log.error("Error occurred validating message - " + msg, e);
			
			throw new CollageException("An error occurred while processing message.",e);
		}
	}
	
	public FoundationMessage (String xmlRequest)
	{
		if (xmlRequest == null || xmlRequest.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty xmlRequest parameter.");
				
		_xmlRequest = xmlRequest;
		
		validateMessage(_xmlRequest);
	}
	
	/*************************************************************************/
	/* Public Methods
	/*************************************************************************/	
	
	// TODO: rename this to getAdapterName or getAdapterType?
	public String getName()
	{
		if (_msgName != null)
			return _msgName;
		
		Element rootElement = getDocument().getDocumentElement();
		_msgName = rootElement.getAttribute(ATTRIBUTE_ADAPTER);
		
		// XML stream may not conform to xsd and may be older format in
		// that case the adapter type is the root element tag
		if (_msgName == null || _msgName.length() == 0)
			_msgName = rootElement.getTagName();		
    	
		// Convert to upper-case
		_msgName = _msgName.toUpperCase();
		
    	return _msgName;    	
	}
	
	public String getText ()
	{
		if (_msg != null)
		{
			try {
				return _msg.getText();
			}
			catch (JMSException je)
			{
				throw new CollageException("Error occurred getting message text.", je);			
			}
		}
		else {
			return _xmlRequest;
		}
	}
	
	public String getID ()
	{
		if (_msg != null)
		{
			try {
				return _msg.getJMSMessageID();
			}
			catch (JMSException je)
			{
				throw new CollageException("Error occurred getting message id.", je);			
			}
		}
		
		// Return xml request string as id.  Hashcode return hash of ID
		return _xmlRequest;
	}
	
	public Session getSession()
	{
		return _session;
	}
	
	public synchronized List<CollageCommand> getCommands()
	{
		if (_commands == null)
		{
			_commands = AdapterUtil.getCommands(getDocument());
		}
		
		return _commands;
	}
	
	public synchronized List<Hashtable<String, String>> getAttributes ()
	{
		// Parse as bulk
		if (_attributes == null)
		{
			// To avoid parsing twice we extract the attribute list from the commands
			List<CollageCommand> commands = getCommands();
			
			// If there are commands then we extract the attributes from them instead of going through the DOM
			// TODO:  Once all adapters have been converted this logic can be cleaned-up
			if (commands != null && commands.size() > 0)					
			{
				_attributes = new ArrayList<Hashtable<String, String>>(10);
				
				CollageCommand cmd = null;
				Iterator<CollageCommand> itCmds = commands.iterator();	
				while (itCmds.hasNext())
				{
					cmd = itCmds.next();
					if (cmd != null)
						_attributes.addAll(cmd.getAttributes());
				}
			}
			else
			{
				_attributes = AdapterUtil.getAttributes(getDocument().getDocumentElement());
			}				
		}
		
		return _attributes;
	}
	
	/**
	 * Returns boolean indicating whether this message has a property value of at least
	 * one of the key provided.
	 * @param keyList
	 * @return
	 */
	public boolean hasProperty (List<String> keyList)
	{
		if (keyList == null || keyList.size() == 0)
			return false;
		
		Map<String, Vector<String>> msgAttributes = getDistinctAttributes();
		if (msgAttributes == null || msgAttributes.size() == 0)
			return false;
		
		Iterator<String> it = keyList.iterator();
		while (it.hasNext())
		{
			if (msgAttributes.containsKey(it.next()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks to see if message has property value which matches the one provided.
	 * TODO:  Rework for performance - Maybe keep a list of property value objects and then
	 * do a contains() instead of a hashtable of values for each attribute.  Also, we may want to
	 * rework the XML parsing in order to facilitate the property value lookups.
	 * @param propName
	 * @param value
	 * @param bRemove
	 * @return
	 */
	public int hasPropertyValueMatch(List<String> keyList, FoundationMessage msg, boolean bRemove)
	{
		if (msg == null)
			throw new IllegalArgumentException("Invalid null FoundationMessage parameter.");
		
		if (keyList == null || keyList.size() == 0)
		{
			return -1;
		}
		
		Map<String, Vector<String>> msgAttributes1 = getDistinctAttributes();
		Map<String, Vector<String>> msgAttributes2 = msg.getDistinctAttributes();			
		
		if (msgAttributes1 == null || msgAttributes1.size() == 0 ||
			msgAttributes2 == null || msgAttributes2.size() == 0)
			return -1;

		// Go through all attribute values to find match
		String value = null;
		String key = null;
		Vector<String> values1 = null;
		Vector<String> values2 = null;
		int numKeyMatch = 0;
		boolean matchFound = false;

		for (int i = 0; i < keyList.size(); i++)
		{
			key = keyList.get(i);
			
			values1 = msgAttributes1.get(key);
			if (values1 == null)
			{
				// If key does not exist we consider a key match for determining if all keys match
				numKeyMatch++;
				continue;
			}
			
			values2 = msgAttributes2.get(key);
			if (values2 == null)
			{
				// If key does not exist we consider a key match for determining if all keys match
				numKeyMatch++;
				continue;
			}
			
			Iterator<String> itString = values1.iterator();
			while (itString.hasNext())
			{
				value = itString.next();
				
				if (values2.contains(value))
				{		
					matchFound = true;
					numKeyMatch++;
					
					// Remove key from key list since it has been found
					if (bRemove == true)
					{
						keyList.remove(i);					
						i--;
					}
					
					break;
				}
			}
		}
		
		int retValue = -1; // No Match		
		if (matchFound == true)
		{			
			if (numKeyMatch == keyList.size())
			{
				retValue = 1; // All Key values Match
			}
			else
			{
				retValue = 0; // At least one key value matches, but not all
			}
			
			if (log.isDebugEnabled())
				log.debug("Message Attribute Match Found - Type: " + msg.getName() 
						+ ", Msg ID: " + msg.getID()
						+ ", KeyMatchValue: " + retValue);
				
//				System.out.println("Message Queued - Type: " + msg.getName() 
//						+ ", Msg ID: " + msg.getID()
//						+ ", Waiting For ID: " + this.getID()
//						+ ", KeyMatchValue: " + retValue);			
		}

		return retValue;
	}
	
	public void acknowledge ()
	{
		if (_msg != null)
		{
			try {
				_msg.acknowledge();
			}
			catch (JMSException je)
			{
				throw new CollageException("Error occurred acknowledging message.", je);			
			}	
			finally {
				if (_session != null)
				{
					try {
						_session.close();
					}
					catch (Exception e)
					{
						log.error(e);
					}
				}
			}
		}
		
		// Else do nothing if this FoundationMessage is wrapping a straight xml request string
	}
	
	/**
	 * Returns time the JMS Timestamp or if not a JMS message then the time "this" instance was created.
	 * 
	 * @return
	 */
	public long getTimestamp ()
	{
		if (_msg != null)
		{
			try {
				return _msg.getJMSTimestamp();
			}
			catch (JMSException je)
			{
				throw new CollageException("Error occurred getting JMS timestamp.", je);			
			}	
		} else
			return _timeCreated;
	}
	
	public String toString()
	{		
		return getText();
	}
	
	@Override
	public int hashCode()
	{
		String id = getID();
		
		return (id == null) ? 0 : id.hashCode();
	}		
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		
		if ((obj instanceof FoundationMessage) == false)
			return false;
		
		return (obj.hashCode() == this.hashCode());
	}	
		
	/**
	 * Decrement pending message count
	 * @return
	 */
	public int decrementPendingCount ()
	{
		return _pendingMessageCount.decrementAndGet();
	}	
	
	/**
	 * Increment pending message count
	 * @return
	 */
	public int incrementPendingCount ()
	{
		return _pendingMessageCount.incrementAndGet();
	}
	
	public void addDependentMessage (FoundationMessage msg)
	{
		if (msg == null)
		{
			throw new IllegalArgumentException("Invalid null FoundationMessage parameter.");
		}
		
		synchronized (_dependentMessages)
		{
			if (_dependentMessages.contains(msg) == false)
				_dependentMessages.add(msg);
		}
	}
	
	public boolean hasDependentMessages()
	{
		if (_dependentMessages == null)
			return false;
		
		synchronized (_dependentMessages)
		{
			return (_dependentMessages.size() > 0);
		}
	}
	
	public int getDependentMessageCount ()
	{
		return _dependentMessages.size();
	}
	
	public Queue<FoundationMessage> getDependentMessages ()
	{
		return _dependentMessages;
	}
	
	public void removeDependentMessage (FoundationMessage msg)
	{
		if (msg == null)
		{
			throw new IllegalArgumentException("Invalid null FoundationMessage parameter.");
		}
		
		synchronized (_dependentMessages)
		{
			_dependentMessages.remove(msg);
		}
	}
	
	/*************************************************************************/
	/* Private Methods
	/*************************************************************************/	
	
	private void validateMessage(String msg)
	{
		// get matching schema from loaded schemas - if they're not loaded, they will get loaded here.  
		Schema schema = AdapterUtil.getSchema(getName());
		
		// validate msg against this schema
		// TODO: parse the Document here so that it's not done twice.
		if (schema != null) {
			try {
				Validator validator = schema.newValidator();
	            validator.validate(new DOMSource(getDocument()));
			}
			catch (Exception e)
			{
				throw new CollageException("Error occurred validating schema", e);
			}
		}
		else {
			// do simple validation when no schema exists.
			getName(); // Makes sure message contains							
		}
	}
	
	private synchronized Document getDocument()
	{
		// get the document if it hasn't been created yet
		if (_document == null)
		{
			ByteArrayInputStream is = null;
			try
			{
				byte[] ba = getText().getBytes();

				is = new ByteArrayInputStream(ba);
				
				DocumentBuilder parser = null;
				
				// Note:  Access is synchronized through method synchronization
				parser = DOC_BUILDER_FACTORY.newDocumentBuilder();	
				
				_document = parser.parse(is);				
			}
			catch (Exception e)
			{
				log.error("Error parsing message - Message:" + getText() + " Error:" +e);
				
				throw new CollageException("Error parsing xml message.", e);			
			}
			finally {
				if (is != null)
				{
					try { is.close(); } catch (Exception e) {}
				}
			}
		}
		
		return _document;
	}
	
	private synchronized Map<String, Vector<String>> getDistinctAttributes ()
	{
		if (_distinctAttributes == null)
		{
			List<Hashtable<String, String>> msgAttributes = getAttributes();
		
			if (msgAttributes == null || msgAttributes.size() == 0)
			{
				_distinctAttributes = new Hashtable<String, Vector<String>>(0);
			}
			else {
				// Create hashtable 
				_distinctAttributes = new Hashtable<String, Vector<String>>(20);
				
				Hashtable<String, String> htAttrs = null;
				Iterator<Hashtable<String, String>> it = msgAttributes.iterator();
				Iterator<String> itKey = null;
				String key = null;
				String value = null;
				Vector<String> values = null;
				while (it.hasNext())
				{
					htAttrs = it.next();
					
					if (htAttrs == null || htAttrs.size() == 0)
						continue;
					
					itKey = htAttrs.keySet().iterator();
					
					while (itKey.hasNext())
					{
						key = itKey.next();
						
						if (_distinctAttributes.containsKey(key))
						{
							values = _distinctAttributes.get(key);						
						}
						else
						{
							values = new Vector<String>(5);
							_distinctAttributes.put(key, values);												
						}
						
						value = htAttrs.get(key);
						
						if (values.contains(value) == false)
							values.add(value);
					}													
				}					
			}
		}
		
		return _distinctAttributes;
	}	
}
