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
package com.groundwork.collage.model.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.PropertyExtensible;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.PropertyValue;

public class StatisticEntity implements PropertyExtensible
{
	public Object get(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	public ApplicationType getApplicationType() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PropertyType> getBuiltInProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PropertyType> getComponentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityTypeCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> getProperties(boolean dynamicOnly) 
	{	
		Map<String, Object> properties = new HashMap<String, Object>();
		
		// Statistic Entities do not support dynamic properties
		if (dynamicOnly == true)
			return null;

		List<PropertyType> builtInProperties = getBuiltInProperties();			
		if (builtInProperties != null && builtInProperties.size() > 0)
		{
			PropertyType propType = null;
			
			// Include built-in property values
			Iterator<PropertyType> it = builtInProperties.iterator();
			while (it.hasNext())
			{
				propType = it.next();
				properties.put(propType.getName(), getProperty(propType.getName()));
			}
		}

		return properties;
	}

	public Object getProperty(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyType getPropertyType(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getPropertyTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyValue getPropertyValueInstance(String name, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasPropertyType(String propertyName) {
		// TODO Auto-generated method stub
		return false;
	}

	public void set(String propertyName, Object value)
	{
		throw new java.lang.UnsupportedOperationException("Illegal operation - Unable to set property on StatisticEntity");		
	}

	public void setApplicationType(ApplicationType applicationType) 
	{
		throw new UnsupportedOperationException("Illegal operation - Unable to setApplicationType property on StatisticEntity");		
	}

	public void setProperties(Map properties) 
	{
		throw new UnsupportedOperationException("Illegal operation - Unable to setProperties on StatisticEntity");		
	}

	public void setProperties(Properties properties) 
	{
		throw new UnsupportedOperationException("Illegal operation - Unable to setProperties on StatisticEntity");		
	}

	public void setProperty(String propertyName, Object value) 
	{
		throw new UnsupportedOperationException("Illegal operation - Unable to setProperty on StatisticEntity");		
	}	
}
