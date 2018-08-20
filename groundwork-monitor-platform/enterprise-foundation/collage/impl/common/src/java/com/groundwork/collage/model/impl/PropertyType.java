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

import com.groundwork.collage.util.DateTime;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/*
 * Represents metadata that characterizes the type of a primitive object
 * 'soft-coded' into various entities, for example such as the one returned by
 * ServiceStatus.getProperty(name)
 *
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public class PropertyType implements com.groundwork.collage.model.PropertyType
{
	private Integer propertyTypeId;
	private String  name;
	private String  description;
	private boolean isVisible;
	private boolean isDate;
	private boolean isBoolean;
	private boolean isString;
	private boolean isInteger;
	private boolean isLong;
	private boolean isDouble;
	
	private String 	relatedEntityType = null;
	private boolean isBuiltIn = false;

	/** empty constructor */
	public PropertyType() {}

	/** 
	 * constructor that specifies the name and type of the PropertyType; 
	 * mostly used for unit testing
	 *
	 * @throws IllegalArgumentException 
	 *	if the name is null or the PropertyType provided is invalid
	 */
	public PropertyType(String name, DataType dataType) 
	{
		if (name == null)
			throw new IllegalArgumentException("Attempting to create a PropertyType with a null name");

		this.setName(name);
		this.setPrimitiveType(dataType);
	}
	
	public PropertyType(String name, String description, DataType type, String relatedEntityType, boolean isBuiltIn) 
	{
		if (name == null)
			throw new IllegalArgumentException("Attempting to create a PropertyType with a null name");

		this.setName(name);
		this.setPrimitiveType(type);
		this.setDescription(description);
		this.setRelatedEntityType(relatedEntityType);
		this.isBuiltIn(isBuiltIn);
	}
	
	public Integer getPropertyTypeId() 
	{
		if (this.propertyTypeId == null)
			this.propertyTypeId = new Integer(-1);

		return this.propertyTypeId;
	}

	public void setPropertyTypeId(Integer propertyTypeId) {
		this.propertyTypeId = propertyTypeId;
	}


	public String getPrimitiveType() 
	{
		if (this.isDate())
			return DATE;
		else if (this.isBoolean())
			return BOOLEAN;
		else if (this.isString())
			return STRING;
		else if (this.isInteger())
			return INTEGER;
		else if (this.isLong())
			return LONG;
		else if (this.isDouble())
			return DOUBLE;
		else	// this should not happen
			throw new IllegalStateException("PropertyType primitive type has not been defined!");
	}

	public void setPrimitiveType(DataType type) 
	{
		this.setDate(false);
		this.setBoolean(false);
		this.setString(false);
		this.setInteger(false);
		this.setLong(false);
		this.setDouble(false);

		switch (type)
		{
			case DATE:
				this.setDate(true);
				break;
			case BOOLEAN:
				this.setBoolean(true);
				break;
			case STRING:
				this.setString(true);
				break;
			case INTEGER:
				this.setInteger(true);
				break;
			case LONG:
				this.setLong(true);
				break;		
			case DOUBLE:
				this.setDouble(true);
				break;		
			default:
				throw new IllegalArgumentException("Invalid type of PropertyType: '" + type + "'");
				
		}
	}

	public void setPrimitiveType(String type) 
	{
		this.setDate(false);
		this.setBoolean(false);
		this.setString(false);
		this.setInteger(false);
		this.setLong(false);
		this.setDouble(false);

		if (DATE.equals(type))
			this.setDate(true);

		else if (BOOLEAN.equals(type))
			this.setBoolean(true);

		else if (STRING.equals(type))
			this.setString(true);

		else if (INTEGER.equals(type))
			this.setInteger(true);

		else if (LONG.equals(type))
			this.setLong(true);

		else if (DOUBLE.equals(type))
			this.setDouble(true);

		else	// this should not happen
			throw new IllegalArgumentException("Invalid type of PropertyType: '" + type + "'");
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isVisible() {
		return this.isVisible;
	}
	
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}


	public boolean isDate() {
		return this.isDate;
	}

	public void setDate(boolean isDate) {
		this.isDate = isDate;
	}


	public boolean isBoolean() {
		return this.isBoolean;
	}

	public void setBoolean(boolean isBoolean) {
		this.isBoolean = isBoolean;
	}


	public boolean isString() {
		return this.isString;
	}

	public void setString(boolean isString) {
		this.isString = isString;
	}


	public boolean isInteger() {
		return this.isInteger;
	}

	public void setInteger(boolean isInteger) {
		this.isInteger = isInteger;
	}


	public boolean isLong() {
		return this.isLong;
	}

	public void setLong(boolean isLong) {
		this.isLong = isLong;
	}


	public boolean isDouble() {
		return this.isDouble;
	}

	public void setDouble(boolean isDouble) {
		this.isDouble = isDouble;
	}

	public String getRelatedEntityType()
	{
		return relatedEntityType;
	}

	public void setRelatedEntityType(String entityTypeName)
	{
		relatedEntityType = entityTypeName;
	}

	public boolean isBuiltIn()
	{
		return isBuiltIn;
	}

	public void isBuiltIn(boolean isBuiltIn)
	{
		this.isBuiltIn = isBuiltIn;
	}
	/* 
	 * returns true if the object passed is of the type specified by this
	 * PropertyType or of type String; for example, it would return true if
	 * 'isDouble' returns true and the runtime class of the object passed is
	 * java.lang.Double or java.lang.String; String is always valid because
	 * we assume that all properties may be represented as String, in which
	 * case they must be parsed before being assigned; nulls are always valid,
	 * since we may want to assign a null to a value
	 */
	public boolean isValid(Object o)
	{
		if (o == null) return true;

		// if the value is a String, see if we can make sense of it
		if (o instanceof String) {
			try { 
				this.parse((String)o);
				return true;
			}
			catch (Exception e) {
				return false;
			}
		}

		if (isDate())
			return o instanceof Date;

		else if (isString())
			return o instanceof String;

		else if (isBoolean())
			return o instanceof Boolean;

		else if (isInteger())
			return o instanceof Integer;

		else if (isLong())
			return o instanceof Long;

		else if (isDouble())
			return o instanceof Double;

		else	// this should not happen
			throw new IllegalStateException("PropertyType type has not been defined!");
	}


	/* 
	 * The Collage api generally expects parameters to be passed as strings; this
	 * method facilitates the conversion of those strings to the appropriately
	 * casted 'primitive' object; given a string representation of another
	 * primitive value, this method attempts to parse that string and returns the
	 * corresponding primitive Object; for example, if isBoolean is true,
	 * parse("1") will return Boolean.TRUE
	 */
	public Object parse(String s)
	{
		if (s == null) return null;
		try
		{ 
			if (isString())
				return s;
			else if (isDate()) {
				return DateTime.parse(s);
			}
			else if (isBoolean()) {
                return convertStringToBoolean(s);
            }
			else if (isInteger())
				return Integer.valueOf(s);
			else if (isLong())
				return Long.valueOf(s);
			else if (isDouble())
				return Double.valueOf(s);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Unable to parse string value: '" + s + "': " + e);
		}

		// this should not happen
		throw new IllegalStateException("PropertyType primitive type has not been defined!");
	}

    public boolean convertStringToBoolean(String s) {
        if (s == null)
            return Boolean.FALSE;
        if (s.equals("1") || s.equalsIgnoreCase("t"))
            return true;
        return Boolean.valueOf(s);
    }


	public String toString() 
	{
		String type = "undefined!";
		if (isDate())
			type = "isDate";
		else if (isString())
			type = "isString";
		else if (isBoolean())
			type = "isBoolean";
		else if (isInteger())
			type = "isInteger";
		else if (isLong())
			type = "isLong";
		else if (isDouble())
			type = "isDouble";

		return new ToStringBuilder(this)
			.append("id", getPropertyTypeId())
			.append("name", getName())
			.append(type)
			.toString();
	}

	/**
	 * Two PropertyTypes are equal if they share the same name and have the same
	 * primitive type
	 */
	public boolean equals(Object other) 
	{
		if ( (this == other ) ) return true;
		if ( !(other instanceof PropertyType) ) return false;
		PropertyType castOther = (PropertyType) other;
		return new EqualsBuilder()
			.append(this.getName(), castOther.getName())
			.append(this.getPrimitiveType(),castOther.getPrimitiveType())
			.isEquals();
	}

	public int hashCode() 
	{
		return new HashCodeBuilder()
			.append(getName())
			.append(getPrimitiveType())
			.toHashCode();
	}

}
