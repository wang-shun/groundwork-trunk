/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.groundwork.collage.model;

/**
 * Represents metadata that characterizes the type of a primitive object
 * 'soft-coded' into various entities, for example such as the one returned by
 * ServiceStatus.getProperty(name)
 *
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @author <a href="mailto:pparavicini@itgroundwork.com">Philippe Paravicini</a>
 */
public interface PropertyType
{
	static final String INTERFACE_NAME = "com.groundwork.collage.model.PropertyType";
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.PropertyType";
	
	public enum DataType {
		DATE,
		BOOLEAN,
		STRING,
		INTEGER,
		LONG,
		DOUBLE		
	}
	
	/** used to refer to a PropertyType with isDate = true */
	public static final String DATE    = "DATE";

	/** used to refer to a PropertyType with isBoolean = true */
	public static final String BOOLEAN = "BOOLEAN";

	/** used to refer to a PropertyType with isString = true */
	public static final String STRING  = "STRING";

	/** used to refer to a PropertyType with isInteger = true */
	public static final String INTEGER = "INTEGER";

	/** used to refer to a PropertyType with isLong = true */
	public static final String LONG    = "LONG";

	/** used to refer to a PropertyType with isDouble = true */
	public static final String DOUBLE  = "DOUBLE";

	/** enumerates the valid primitive types that may characterize a PropertyType */
	public static final String[] SUPPORTED_PRIMITIVES = {DATE, BOOLEAN, STRING, INTEGER, LONG, DOUBLE};

	Integer getPropertyTypeId();

	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);

	/** 
	 * returns the string in SUPPORTED_PRIMITIVES corresponding to the type of
	 * primitive value that will be stored in this Property 
	 * (one of DATE, BOOLEAN, STRING, INTEGER, LONG or DOUBLE)
	 */
	String getPrimitiveType();

	/** 
	 * alternate way to specify the type of primitive value that will be stored
	 * in this Property (the other way is using the isDate, isBoolean, etc
	 * flags); the argument must be one of the strings enumerated in
	 * SUPPORTED_PRIMITIVES
	 */
	void setPrimitiveType(DataType type);
	void setPrimitiveType(String type);

	boolean isDate();
	void setDate(boolean isDate);

	boolean isBoolean();
	void setBoolean(boolean isBoolean);

	boolean isString();
	void setString(boolean isString);

	boolean isInteger();
	void setInteger(boolean isInteger);

	boolean isLong();
	void setLong(boolean isLong);

	boolean isDouble();
	void setDouble(boolean isDouble);

	/** 
	 * returns true if the object passed is of the type specified by this
	 * PropertyType or of type String; for example, it would return true if
	 * 'isDouble' returns true and the runtime class of the object passed is
	 * java.lang.Double or java.lang.String; String is always valid because
	 * we assume that all properties may be represented as String, in which
	 * case they must be parsed before being assigned
	 */
	boolean isValid(Object o);

	/** 
	 * The Collage api generally expects parameters to be passed as strings; this
	 * method facilitates the conversion of those strings to the appropriately
	 * casted 'primitive' object; given a string representation of another
	 * primitive value, this method attempts to parse that string and returns the
	 * corresponding primitive Object; for example, if isBoolean is true,
	 * parse("1") will return Boolean.TRUE
	 */
	Object parse(String value);
	
	/**
	 * Returns the name of the entity type which this property identifies.
	 */
	String getRelatedEntityType ();
}
