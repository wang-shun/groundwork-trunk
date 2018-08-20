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

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;

import java.util.Date;

/**
 * Used internally by entities for which 'soft-coded' entities have been
 * defined to store the value of the primitive object; this class is used in
 * hibernate within a 'Component' mapping, which makes it possible to use in a
 * variety of contexts, and store the values in different tables
 * 
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public class PropertyValue implements com.groundwork.collage.model.PropertyValue
{
	static Log log = LogFactory.getLog(PropertyValue.class);
	
	protected String name;
	protected Integer propertyTypeId;	
	protected String  valueString;
	protected Date    valueDate;
	protected Boolean valueBoolean;
	protected Integer valueInteger;
	protected Long    valueLong;
	protected Double  valueDouble;
	protected Date    createdOn = new Date();
	protected Date    editedOn = new Date();

	protected PropertyType propertyType;
	
	private static MetadataService metadataService = null;

    private MetadataService getMetadataService() {
        if (metadataService == null) {
            CollageFactory collage = CollageFactory.getInstance();
            metadataService = collage.getMetadataService();
        }
        return metadataService;
    }
	/** 
	 * empty constructor - use with care as attempting to use many of the
	 * methods in this object may yield an IllegalStateException, mostly here for
	 * Hibernate's benefit 
	 */
	public PropertyValue() {}

	/** 
	 * construct a valid and useable instance of this PropertyValue; this should be the
	 * preferred way of constructing an instance of this object - note that the
	 * PropertyType must exist in the Metadata object retrieved from the
	 * Collagefactory
	 *
	 * @throws IllegalStateException if the PropertyType is null
	 * @throws IllegalArgumentException 
	 *	 if the value passed does not match the value specified in the
	 *	 PropertyType
	 */
	public PropertyValue(String name, Object value)
	{
        this.name = name;
		this.setPropertyType(getMetadataService().getPropertyTypeByName(name));
		this.setValue(value);
	}
	
	/**
	 * Convience method to return value depending on property type.
	 * @return
	 */
	public Object getValue()
	{
		if (this.getPropertyType() == null)
			throw new CollageException("Attempting to retrieve value from PropertyValue with undefined PropertyType!: " + this.toString());

		if 		(this.getPropertyType().isString())  return this.valueString;
		else if (this.getPropertyType().isDate())    return this.valueDate; 
		else if (this.getPropertyType().isBoolean()) return this.valueBoolean;
		else if (this.getPropertyType().isInteger()) return this.valueInteger;
		else if (this.getPropertyType().isLong())    return this.valueLong;
		else if (this.getPropertyType().isDouble())  return this.valueDouble; 

		// something is wrong if we get this far
		throw new IllegalStateException("Unable to determine type for PropertyValue: " + this.toString());
	}


	/**
	 * This property accepts values that are of the proper primitive wrapper
	 * (Date, Boolean, String, Integer, Long, Double), or are Strings and can
	 * be properly parsed by {@link #getPropertyType this.getPropertyType}
	 */
	public void setValue(Object o)
	{
		PropertyType type = this.getPropertyType();
		if (type == null)
			throw new IllegalStateException("Attempting to set value of PropertyValue with undefined PropertyType: " + this.getName());

		if (!type.isValid(o))
			throw new IllegalArgumentException(
					"Attempting to set value of incorrect type '" + o.getClass() + "' to PropertyValue " + this.toString());

		try
		{ 
			if      (type.isString())  this.valueString  = (String)o;
			else if (type.isDate())    this.valueDate    = (Date)   ( (o == null || o instanceof Date)    ? o : type.parse((String)o) );
			else if (type.isBoolean()) this.valueBoolean = (Boolean)( (o == null || o instanceof Boolean) ? o : type.parse((String)o) );
			else if (type.isInteger()) this.valueInteger = (Integer)( (o == null || o instanceof Integer) ? o : type.parse((String)o) );
			else if (type.isLong())    this.valueLong    = (Long)   ( (o == null || o instanceof Long)    ? o : type.parse((String)o) );
			else if (type.isDouble())  this.valueDouble  = (Double) ( (o == null || o instanceof Double)  ? o : type.parse((String)o) );
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(
					"Unable to set PropertyValue '" + o + "' for " + type + " - " + e);
		}
	}

    public Integer getPropertyTypeId ()
    {
    	return propertyTypeId;
    }
    
    public void setPropertyTypeId (Integer propertyTypeId)
    {
    	this.propertyTypeId = propertyTypeId;
    }
    
    public String getValueString ()
    {
    	return this.valueString;
    }
    
    public void setValueString (String val)
    {
    	this.valueString = val;
    }
    
    public Date getValueDate ()
    {
    	return this.valueDate;
    }
    
    public void setValueDate (Date val)
    {
    	this.valueDate = val;
    }
    
    public Boolean getValueBoolean ()
    {
    	return this.valueBoolean;
    }
    
    public void setValueBoolean (Boolean val)
    {
    	this.valueBoolean = val;
    }

    public Integer getValueInteger ()
    {
    	return this.valueInteger;
    }
    
    public void setValueInteger (Integer val)
    {
    	this.valueInteger = val;
    }
        
    public Long getValueLong ()
    {
    	return this.valueLong;
    }
    
    public void setValueLong (Long val)
    {
    	this.valueLong = val;
    }
    
    public Double getValueDouble ()
    {
    	return this.valueDouble;
    }
    
    public void setValueDouble (Double val)
    {
    	this.valueDouble = val;
    }           
    
    public Date getCreatedOn ()
    {
    	return this.createdOn;
    }
    
    public void setCreatedOn (Date createdOn)
    {
    	this.createdOn = createdOn;
    }     
    
    public Date getLastEditedOn ()
    {
    	return this.editedOn;
    }
    
    public void setLastEditedOn (Date editedOn)
    {
    	this.editedOn = editedOn;
    }        

	/** 
	 * retrieves the PropertyType from cached metadata using the PropertyTypeId
	 * stored in the database
	 */
	public PropertyType getPropertyType()
	{
		if (this.propertyType != null)
			return this.propertyType;
		
		if (this.propertyTypeId == null)
			return null;
		
		return getMetadataService().getPropertyTypeById(this.propertyTypeId.intValue());
	}

	/** 
	 * stores the propertyType id of the propertyType passed
	 */
	public void setPropertyType(PropertyType propertyType)
	{
		this.propertyType = propertyType;
		
		if (propertyType != null)
		{
			this.propertyTypeId = propertyType.getPropertyTypeId();
			this.name = propertyType.getName();
		}
	}   
	
    public String getName ()
    {
    	return this.name;
    }
    
    public void setName (String name)
    {
    	this.name = name;
    }
    
	public String toString()
	{
        return new ToStringBuilder(this)
			.append("propertyTypeId", this.getPropertyTypeId())
			.append("propertyType",   this.getPropertyType())
			.append("name",   		  name)
			.append("valueString",    valueString)
			.append("valueDate",      valueDate)
			.append("valueBoolean",   valueBoolean)
			.append("valueInteger",   valueInteger)
			.append("valueLong",      valueLong)
			.append("valueDouble",    valueDouble)
			.append("createdOn",      createdOn)
			.append("editedOn",       editedOn)
			.toString();
	}
}
