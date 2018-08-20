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

package com.groundwork.collage.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.groundwork.collage.CollageCommand;
import com.groundwork.collage.CollageEntity;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.impl.Command;
import com.groundwork.collage.impl.Entity;


/**
 * 
 * AdapterUtil
 * Utility class for common tasks related to adapters
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: AdapterUtil.java 11043 2008-02-29 16:12:43Z cpora $
 */
public class AdapterUtil 
{
	private static final String DOT = ".";
	private static final String COMMA = ",";
	private static final String PROP_ADAPTER_SCHEMA_PREFIX = "fmd.adapter.schema";

	private static SchemaFactory SCHEMA_FACTORY =
		SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	
    // the hash will contain the Message type as a key and the corresponding schema as value.
    private static Hashtable<String, Schema> SCHEMAS = loadSchemas();
    
    ///////////////////////////////////////////////////////////////////////////
    // XML Constants
    ///////////////////////////////////////////////////////////////////////////
    
    // Elements
    private static final String ELEMENT_COMMAND = "Command";
    
    // Attributes
    private static final String ATTRIBUTE_ACTION = "Action";
    public static final String ATTRIBUTE_APP_TYPE = "ApplicationType";
    
	/* Enable log for log4j */
    private static Log log = LogFactory.getLog(AdapterUtil.class);
    /**
     * Private Constructor 
     */
    private AdapterUtil() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	private static Hashtable<String, Schema> loadSchemas()
	{
		Hashtable<String, Schema> schemas = new Hashtable<String, Schema>();
		
		// get schema names from properties files - only one now, but there will eventually be multiple
		CollageFactory collageFactory = CollageFactory.getInstance();
		collageFactory.initializeSystem();
        Properties configuration = collageFactory.getFoundationProperties();
		String propName = null;
		String value = null;
		int pos = -1;
        Enumeration schemaEnum = configuration.propertyNames();
		while (schemaEnum.hasMoreElements())
		{
			propName = (String)schemaEnum.nextElement();
			
			if (propName.startsWith(PROP_ADAPTER_SCHEMA_PREFIX) )
			{
				value = configuration.getProperty(propName, null);
				if (value == null)
					continue;
				
				pos = propName.lastIndexOf(DOT);
				String adapterType = propName.substring(pos + 1);
				File schemaFile = new File(value);
				if (schemaFile.exists())
				{
					try {
						Schema schema = SCHEMA_FACTORY.newSchema(schemaFile);
						schemas.put(adapterType, schema);
					}
					catch (SAXException saxException)
					{
						log.error("SAXException!");
						log.error(saxException.getMessage());
						saxException.printStackTrace();
						//throw new CollageException("Error occurred loading schema files", saxException);
					}
				}
			}
		}
		return schemas;
	}
	
	public static Schema getSchema(String adapterType)
	{
		if (SCHEMAS.containsKey(adapterType))
		{
			return (Schema)SCHEMAS.get(adapterType);
		}
		else
		{
			return null;
		}
	}
	
	public static List<CollageCommand> getCommands(Document document)
	{
		Element commandElement = null;
		Command command = null;
		Node element = null;		
		List<CollageCommand> commandList = new Vector<CollageCommand>();
		NodeList elements = document.getElementsByTagName(ELEMENT_COMMAND);
				
		if (elements == null || elements.getLength() == 0)
			return commandList;
		
		for (int i = 0; i < elements.getLength(); i++)
		{			
			element = elements.item(i);
			
			if (element.getNodeType() == Node.ELEMENT_NODE)
			{			
				command = new Command();
				
				commandElement = (Element)element;
				
				NamedNodeMap attribs = commandElement.getAttributes();
				Node attribute = attribs.getNamedItem(ATTRIBUTE_ACTION);
				if (attribute != null)
				{
					command.setAction(attribute.getNodeValue());
				}
				
				attribute = attribs.getNamedItem(ATTRIBUTE_APP_TYPE);
				if (attribute != null)
				{
					command.setApplicationType(attribute.getNodeValue());
				}
						
				// get the entities for this command
				List<CollageEntity> entities = getEntitiesForCommand(commandElement);
				
				// Make sure there are entities for the command otherwise we ignore it
				if (entities != null && entities.size() > 0)
				{
					command.setEntities(entities);
					commandList.add(command);
				}
			}
		}
		
		return commandList;
	}
	
	private static Hashtable<String, String> getElementAttributes(Node element)
	{
		if (element == null)
			return null;
		
		Hashtable<String, String> htAttributes = null;
		
		// entity attributes
		if (element.hasAttributes())
		{						
			// get attributes
			NamedNodeMap attribs = element.getAttributes();
			int numAttributes = attribs.getLength();						
			Node attribute = null;				
			
			htAttributes = new Hashtable<String, String>(numAttributes);
			
			String name = null;
			String value = null;
			
			for (int j = 0; j < numAttributes; j++)
			{
				attribute = attribs.item(j);
				
				name = attribute.getNodeName();
				value = attribute.getNodeValue();
				
				// Only add attribute if it has a value
				if (value != null && value.length() > 0)
					htAttributes.put(name, value);
			}
		}		
		
		return htAttributes;
	}
	
	private static CollageEntity createEntityFromNode (Node entityElement)
	{
		if (entityElement == null)
			return null;
		
		if (entityElement.getNodeType() != Node.ELEMENT_NODE)
		{
			return null;
		}
		
		Hashtable<String, String> attributes = getElementAttributes(entityElement);
		
		// No attributes for the entity is an incomplete entity and therefore ignored.
		if (attributes == null || attributes.size() == 0)
			return null;
		
		CollageEntity entity = new Entity(entityElement.getNodeName());		
		entity.setProperties(attributes);
		
		// Create Sub-Entities
		NodeList childElements = entityElement.getChildNodes();
		for (int i = 0; i < childElements.getLength(); i++)
		{
			Node element = childElements.item(i);
			
			CollageEntity subEntity = createEntityFromNode(element);
			if (subEntity != null)
				entity.addSubEntity(subEntity);
		}
		
		return entity;
	}
	
	private static List<CollageEntity> getEntitiesForCommand(Element commandElement)
	{
		if (commandElement == null)
			throw new IllegalArgumentException("Invalid null / empty command element parameter.");
		
		List<CollageEntity> entities = new Vector<CollageEntity>();
		NodeList entityElements = commandElement.getChildNodes();
		
		if (entityElements == null)
			return entities;
		
		for (int i = 0; i < entityElements.getLength(); i++)
		{
			Node element = entityElements.item(i);
			
			if (element.getNodeType() != Node.ELEMENT_NODE)
				continue;			
			
			CollageEntity entity = createEntityFromNode(element);
			if (entity != null)
			{			
				entities.add(entity);
			}
		}
		
		return entities;
	}	
	
	/**
	 * @param element
	 * @return
	 */
	public static List<Hashtable<String, String>> getAttributes(Element element)
	{
		Hashtable<String, String> attributes = new Hashtable<String, String>(10);
        List<Hashtable<String, String>> resultSet = new Vector<Hashtable<String, String>>(10);
        		
		// if there are attributes, process them first.  The adapters expect common attributes
        // to be listed first.
		if (element.hasAttributes())
		{
			NamedNodeMap attrs = element.getAttributes();
			Node attribute = null;
			String name = null;
			String value = null;
			int len = attrs.getLength();
			for ( int attCount=0; attCount < len;attCount++)
			{
			    attribute = attrs.item(attCount);
			    name = attribute.getNodeName();
			    value = attribute.getNodeValue();
			    if (name == null || value == null || name.length() == 0)
			    {
			    	continue;
			    }
			    
		        attributes.put(name, value);	
			}
		}
		
		// Add attributes beginning of list
		if (attributes != null && attributes.size() > 0)
			resultSet.add(attributes);
		
		// process children, if any
		if (element.hasChildNodes())
		{
			List<Hashtable<String, String>> childAttributes = null;
			
			NodeList children = element.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) 
			{
				Node child = children.item(i);
				
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					Element childElement = (Element)child;
					childAttributes = getAttributes(childElement);
					
					if (childAttributes != null && childAttributes.size() > 0)
					{
						resultSet.addAll(childAttributes);
					}
				}
			}
		}		
		        
		return resultSet;		
	}
	
	/**
	 * Takes a comma-delimited string and converts it to a List of Strings
	 * @param commaDelimitedString
	 * @return
	 */
	public static List<String> commaDelimitedStringToList (String commaDelimitedString)
	{
		List<String> results = new ArrayList<String>(0);
		
		if (commaDelimitedString == null || commaDelimitedString.length() == 0)
			return results;
		
		StringTokenizer tokenizer = new StringTokenizer(commaDelimitedString, COMMA);
		while (tokenizer.hasMoreElements())
		{
			results.add(tokenizer.nextToken());
		}
		
		return results;
	}
}
