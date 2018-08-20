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
package org.groundwork.foundation.bs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author glee
 *
 */
public class ServiceNotify  implements Serializable
{
	static final long serialVersionUID = 1;
	
	// String Constants
	private static final String EQUALS = "=";
	private static final String SEMI_COLON = ";";
	
	// Action which generated service notification
	private ServiceNotifyAction _action = ServiceNotifyAction.CREATE;
	private ServiceNotifyEntityType _entityType = null;
	
	// Map of service properties
	private Map<String, Object> _notifyAttributes =  new HashMap<String, Object>(0);
	
	public ServiceNotify (ServiceNotifyEntityType entityType, ServiceNotifyAction action, Map<String, Object> attributes)
	{
		if (entityType == null)
			throw new IllegalArgumentException("Invalid null ServiceNotifyEntityType parameter");

		if (action == null)
			throw new IllegalArgumentException("Invalid null ServiceNotifyAction parameter");

		_entityType = entityType;			
		_action = action;
		
		// Make a copy of the map
		if (attributes != null)
			_notifyAttributes.putAll(attributes);
	}
	
	public ServiceNotifyEntityType getEntityType ()
	{
		return _entityType;
	}
	
	public ServiceNotifyAction getAction ()
	{
		return _action;
	}
	
	public Object getAttribute (String name)
	{
		return _notifyAttributes.get(name);
	}	
	
	/** 
	 * We override toString in order to serialize ServiceNotify to JMS Queue.  
	 * The object is string form is represented as follows:
	 * 
	 * EntityType=<entity type>Action=<action>;Attribute1=<value>;Attribute2=<value>;...
	 */
	public String toString ()
	{
		StringBuilder sb = new StringBuilder(64);
		
		sb.append("EntityType=");
		sb.append(_entityType.getValue());
		sb.append(SEMI_COLON);
		
		sb.append("Action=");
		sb.append(_action.getValue());
		sb.append(SEMI_COLON);
		
		if (_notifyAttributes != null && _notifyAttributes.size() > 0)
		{
			String key = null;
			Iterator<String> itKeys = _notifyAttributes.keySet().iterator();
			while (itKeys.hasNext())
			{
				key = itKeys.next();
				
				Object value = _notifyAttributes.get(key);
				if (value != null)
				{
					sb.append(key);
					sb.append(EQUALS);
					sb.append(value.toString());
					sb.append(SEMI_COLON);
				}
			}
		}
		
		return sb.toString();	
	}
}
