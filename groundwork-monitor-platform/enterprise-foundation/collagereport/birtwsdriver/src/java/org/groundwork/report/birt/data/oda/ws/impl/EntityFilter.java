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
package org.groundwork.report.birt.data.oda.ws.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.PropertyDataType;

public class EntityFilter {

	private EntityTypeProperty property = null;
	private FilterOperator operator	= FilterOperator.EQ;
	private String value 			= "";
	private FilterOperator logicalOperator 	= FilterOperator.AND;

	public EntityFilter(EntityTypeProperty property, 
						FilterOperator operator, 
						String value, 
						FilterOperator logicalOperator) 
	{	
		if (property == null)
			throw new IllegalArgumentException("Invalid null EntityTypeProperty parameter.");
		
		if (operator == null)
			throw new IllegalArgumentException("Invalid null FilterOperator parameter.");
		
		this.property = property;
		this.operator = operator;
		this.value = value;
		this.logicalOperator = logicalOperator;
	}
	
	/**
	 * Create a filter with an initial property
	 * Format:
	 * <property>,<operator>,<value>,<logical operator>
	 * 
	 * @param string
	 */
	public EntityFilter(EntityTypeProperty[] props, String filter) throws OdaException
	{
		if (props == null || props.length == 0)
			return;
		
		if (filter == null || filter.length() == 0)
			return;
		
		StringTokenizer tokenizer = new StringTokenizer(filter, ",");
		if ((tokenizer.countTokens() < 3) || (tokenizer.countTokens() > 4))
			throw new OdaException("Invalid filter defintion - [" + filter + "]");
				
		// Find Property Name
		String propName = tokenizer.nextToken();
    	EntityTypeProperty prop = null;
    	for (int i = 0; i < props.length; i++)
    	{
    		prop = props[i];
    		
    		if (propName.equalsIgnoreCase(props[i].getName()))
    		{
    			this.property = prop;
    			break;
    		}
    	}
    	
		this.operator = FilterOperator.fromString(tokenizer.nextToken());
		this.value = tokenizer.nextToken();
		
		if (tokenizer.hasMoreElements())
			this.logicalOperator = FilterOperator.fromString(tokenizer.nextToken());
	}

	public EntityTypeProperty getProperty() 
	{
		return property;
	}

	public void setProperty(EntityTypeProperty property) 
	{
		if (property == null)
			throw new IllegalArgumentException("Invalid null EntityTypeProperty.");
		
		this.property = property;
	}

	public FilterOperator getOperator() 
	{
		return operator;
	}
	
	public void setOperator(FilterOperator operator) 
	{
		this.operator = operator;
	}	

	public void setOperator(String operator) 
	{
		if (operator == null || operator.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty operator");
		
		this.operator = FilterOperator.fromString(operator);
	}

	public Object getValue() 
	{
		if (this.property == null)
			return null;
		
			// Convert String to proper data type
		if (this.value == null || this.value.length() == 0)
			return null;
		
		if (WSCommand.PARAMETER_INDICATOR.equals(this.value))
			return WSCommand.PARAMETER_INDICATOR;
				
		PropertyDataType dataType = this.property.getDataType();
		if (dataType.equals(PropertyDataType.BOOLEAN))
		{
			return Boolean.parseBoolean(this.value);
		}
		else if (dataType.equals(PropertyDataType.DATE))
		{
			SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd-yyyy");
			
			try 
			{
				return sdfDate.parse(this.value);
			}
			catch (Exception e) // Initialize value to current date time	
			{
				Date dtNow = new Date();
				this.value = sdfDate.format(dtNow);

				return dtNow;
			}
		}
		else if (dataType.equals(PropertyDataType.DOUBLE))
		{
			return Double.parseDouble(this.value);
		}
		else if (dataType.equals(PropertyDataType.INTEGER))
		{
			return Integer.parseInt(this.value);
		}
		else if (dataType.equals(PropertyDataType.LONG))
		{
			return Long.parseLong(this.value);
		}
		
		// String value
		return this.value;
	}

	public void setValue(String value) 
	{
		this.value = value;
	}
	
	public FilterOperator getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(FilterOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}	
	
	public void setLogicalOperator(String logicalOperator) 
	{
		if (logicalOperator == null || logicalOperator.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty logical operator");
		
		this.logicalOperator = FilterOperator.fromString(logicalOperator);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		
		sb.append(this.property.getName());
		sb.append(",");
		sb.append(this.operator);
		sb.append(",");
		sb.append(this.value);
		
		if (logicalOperator != null)
		{
			sb.append(",");
			sb.append(this.logicalOperator);
		}
		
		return sb.toString();
	}	
}
