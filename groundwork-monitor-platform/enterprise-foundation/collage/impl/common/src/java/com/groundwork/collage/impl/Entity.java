/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@itgroundwork.com

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
package com.groundwork.collage.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.groundwork.collage.CollageEntity;

public class Entity implements CollageEntity
{
	private String type = null;
	private Hashtable<String, String> properties = null;
	private List<CollageEntity> subEntities = null;
	
	public Entity(String type)
	{
		if (type == null || type.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty entity type string.");
		
		this.type = type;
		this.properties = new Hashtable<String, String>();
		this.subEntities = new ArrayList<CollageEntity>(10);
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return this.type;
	}	
	
	public boolean hasProperties()
	{
		if (this.properties.isEmpty())
			return false;
		return true;
	}
	
	public Hashtable<String, String> getProperties()
	{
		return this.properties;
	}
	
	public void setProperties(Hashtable<String, String> properties)
	{
		this.properties.clear();
		this.properties.putAll(properties);
	}
	
	public void addProperty(String key, String value)
	{
		this.properties.put(key, value);
	}
	
	public List<CollageEntity> getSubEntities ()
	{
		return this.subEntities;
	}
	
	public void addSubEntity(CollageEntity entity)
	{
		if (entity != null)
		{
			this.subEntities.add(entity);
		}
	}
	
	public String getProperty(String key)
	{
		if (this.properties == null)
			return null;
		
		return this.properties.get(key);
	}
	
	public String removeProperty (String key)
	{
		if (this.properties == null)
			return null;
		
		return this.properties.remove(key);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(128);
		
		sb.append("Type: " + this.type);
		sb.append(", Properties: ");
		sb.append(this.properties);
		
		sb.append(", SubEntities: ");
		sb.append(this.subEntities);		
		sb.append("\n");
		
		return sb.toString();
	}
}
