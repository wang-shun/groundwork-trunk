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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.groundwork.collage.CollageCommand;
import com.groundwork.collage.CollageEntity;
import com.groundwork.collage.util.AdapterUtil;

public class Command implements CollageCommand
{
	private String action = null;
	private String applicationType = null;
	private List<CollageEntity> entities = new Vector<CollageEntity>();
	private List<Hashtable<String, String>> attributes = null;
	
	public Command ()
	{		
	}
	
	public Command(String action, String applicationType)
	{		
		this.action = action;
		this.applicationType = applicationType;
	}
	
	public void setAction(String action)
	{
		this.action = action;
	}
	
	public String getAction()
	{
		return this.action;
	}
	
	public void setApplicationType(String applicationType)
	{
		this.applicationType = applicationType;
	}
	
	public String getApplicationType ()
	{
		return this.applicationType;
	}
	
	public List<CollageEntity> getEntities()
	{
		return this.entities;
	}
	
	public void setEntities(List<CollageEntity> entities)
	{
		this.entities.clear();
		this.entities.addAll(entities);
	}
	
	public void addEntity(CollageEntity entity)
	{
		this.entities.add(entity);
	}
	
	public List<Hashtable<String, String>> getAttributes ()
	{
		if (attributes != null)
			return attributes;
		
		// Build up attributes
		attributes = new ArrayList<Hashtable<String, String>>(10);
		
		Hashtable<String, String> cmdAttributes = new Hashtable<String, String>(2);
				
		// Add application type
		if (applicationType != null && applicationType.length() > 0)
			cmdAttributes.put(AdapterUtil.ATTRIBUTE_APP_TYPE, applicationType);
		
		// Add command attributes as first entry in list
		attributes.add(cmdAttributes);
		
		if (entities == null || entities.size() == 0)
			return attributes;
		
		// Add all entity properties any duplicate values will be replaced.
		CollageEntity entity = null;
		Iterator<CollageEntity> itEntities = entities.iterator();
		while (itEntities.hasNext())
		{
			entity = itEntities.next();
			if (entity != null)
				attributes.add(entity.getProperties());
		}
		
		return attributes;		
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(128);
		
		sb.append("Action: ");
		sb.append(this.action);
		
		sb.append(", Application Type: ");
		sb.append(this.applicationType);
		
		sb.append("\nEntities:\n");
		sb.append(this.entities);
		sb.append("\n");
		
		return sb.toString();
	}
}
