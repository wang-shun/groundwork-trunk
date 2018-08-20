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
package com.groundwork.collage.test;

import java.util.Date;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.model.PropertyType.DataType;
import com.groundwork.collage.model.impl.PropertyType;

public class TestPropertyType extends TestCase
{
	Log log = LogFactory.getLog(this.getClass());
	
	final PropertyType DATE    = new PropertyType("DATE",    DataType.DATE);
	final PropertyType BOOLEAN = new PropertyType("BOOLEAN", DataType.BOOLEAN);
	final PropertyType STRING  = new PropertyType("STRING",  DataType.STRING);
	final PropertyType INTEGER = new PropertyType("INTEGER", DataType.INTEGER);
	final PropertyType LONG    = new PropertyType("LONG",    DataType.LONG);
	final PropertyType DOUBLE  = new PropertyType("DOUBLE",  DataType.DOUBLE);

	PropertyType dateType;
	PropertyType boolType;
	PropertyType stringType;
	PropertyType intType;
	PropertyType longType;
	PropertyType doubleType;

	/** executed prior to each test */
	protected void setUp() 
	{ 
	}

	/** executed after each test */
	protected void tearDown() { }

	public void testConstructor()
	{
		assertTrue("DATE isDate", DATE.isDate());
		assertFalse("DATE is not boolean", DATE.isBoolean());

		assertTrue("BOOLEAN isBoolean", BOOLEAN.isBoolean());
		assertFalse("BOOLEAN is not string ", BOOLEAN.isString());

		assertTrue("STRING isString", STRING.isString());
		assertFalse("STRING is not integer ", STRING.isInteger());

		assertTrue("INTEGER isInteger", INTEGER.isInteger());
		assertFalse("INTEGER is not long ", INTEGER.isLong());

		assertTrue("DOUBLE isDouble", DOUBLE.isDouble());
		assertFalse("DOUBLE is not date ", DOUBLE.isDate());
	}


	public void testIsValid()
	{
		assertTrue("Date object is valid", DATE.isValid(new Date()));
		assertTrue("null Date is valid", DATE.isValid(null));
		assertTrue("Date string is valid", DATE.isValid("2005-12-31 12:23:32"));
		assertFalse("Non-Date object is invalid", DATE.isValid(new Double("2.2")));

		assertTrue("Boolean object is valid", BOOLEAN.isValid(Boolean.TRUE));
		assertTrue("null Boolean is valid", BOOLEAN.isValid(null));
		assertTrue("Boolean string is valid", BOOLEAN.isValid("true"));
		assertFalse("Non-Boolean object is invalid", BOOLEAN.isValid(new Date()));

		assertTrue("String object is valid", STRING.isValid("some string"));
		assertTrue("null String is valid", STRING.isValid(null));
		assertFalse("Non-String object is invalid", STRING.isValid(Boolean.FALSE));

		assertTrue("Integer object is valid", INTEGER.isValid(new Integer(2)));
		assertTrue("null Integer is valid", INTEGER.isValid(null));
		assertTrue("Integer string is valid", INTEGER.isValid("2"));
		assertFalse("Non-Integer object is invalid", INTEGER.isValid(new Long(2)));

		assertTrue("Long object is valid", LONG.isValid(new Long(2)));
		assertTrue("null Long is valid", LONG.isValid(null));
		assertTrue("Long string is valid", LONG.isValid("2"));
		assertFalse("Non-Long object is invalid", LONG.isValid(new Integer(2)));

		assertTrue("Double object is valid", DOUBLE.isValid(new Double("2.2")));
		assertTrue("null DOUBLE is valid", DOUBLE.isValid(null));
		assertTrue("Double string is valid", DOUBLE.isValid("2.2"));
		assertFalse("Non-Double object is invalid", DOUBLE.isValid(new Date()));
	}


	public void testParse() throws Exception
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		Date date = df.parse("20051231 12:24:30");

		assertEquals("parse Date", date, DATE.parse("2005-12-31 12:24:30"));

		assertEquals("parse String", "somestring", STRING.parse("somestring"));

		assertEquals("parse Boolean true",  Boolean.TRUE, BOOLEAN.parse("1"));
		assertEquals("parse Boolean false", Boolean.FALSE, BOOLEAN.parse("0"));

		Integer intTwo = new Integer(2);
		assertEquals("parse Integer", intTwo, INTEGER.parse("2"));

		Long lngTwo = new Long(2);
		assertEquals("parse Long", lngTwo, LONG.parse("2"));

		Double dblTwo = new Double(2.0);
		assertEquals("parse Double", dblTwo, DOUBLE.parse("2.0"));

		assertEquals("parse null", null, DOUBLE.parse(null));
	}

	public void testDateEquals() 
	{
		dateType = new PropertyType();
		dateType.setName("DATE");
		dateType.setDate(true);

		assertTrue("date equals DATE", dateType.equals(DATE));

		PropertyType dateType2 = new PropertyType("DATE2", DataType.DATE);
		assertFalse("date equals dateType2", dateType.equals(dateType2));

		boolType   = new PropertyType("DATE", DataType.BOOLEAN);
		stringType = new PropertyType("DATE", DataType.STRING);
		intType    = new PropertyType("DATE", DataType.INTEGER);
		longType   = new PropertyType("DATE", DataType.LONG);
		doubleType = new PropertyType("DATE", DataType.DOUBLE);

		assertFalse("date equals some bool",   dateType.equals(boolType));
		assertFalse("date equals some string", dateType.equals(stringType));
		assertFalse("date equals some int",    dateType.equals(intType));
		assertFalse("date equals some long",   dateType.equals(longType));
		assertFalse("date equals some double", dateType.equals(doubleType));
	}

	public void testBooleanEquals() 
	{
		boolType = new PropertyType();
		boolType.setName("BOOLEAN");
		boolType.setBoolean(true);

		assertTrue("bool equals BOOLEAN", boolType.equals(BOOLEAN));

		PropertyType boolType2 = new PropertyType("BOOLEAN2", DataType.BOOLEAN);
		assertFalse("bool equals bool2", boolType.equals(boolType2));

		dateType   = new PropertyType("BOOLEAN", DataType.DATE);
		stringType = new PropertyType("BOOLEAN", DataType.STRING);
		intType    = new PropertyType("BOOLEAN", DataType.INTEGER);
		longType   = new PropertyType("BOOLEAN", DataType.LONG);
		doubleType = new PropertyType("BOOLEAN", DataType.DOUBLE);

		assertFalse("bool equals some date",   boolType.equals(dateType));
		assertFalse("bool equals some string", boolType.equals(stringType));
		assertFalse("bool equals some int",    boolType.equals(intType));
		assertFalse("bool equals some long",   boolType.equals(longType));
		assertFalse("bool equals some double", boolType.equals(doubleType));
	}

	public void testStringEquals() 
	{
		stringType = new PropertyType();
		stringType.setName("STRING");
		stringType.setString(true);

		assertTrue("string equals STRING", stringType.equals(STRING));

		PropertyType stringType2 = new PropertyType("STRING2", DataType.STRING);
		assertFalse("string equals string2", stringType.equals(stringType2));

		dateType   = new PropertyType("STRING", DataType.DATE);
		boolType   = new PropertyType("STRING", DataType.BOOLEAN);
		intType    = new PropertyType("STRING", DataType.INTEGER);
		longType   = new PropertyType("STRING", DataType.LONG);
		doubleType = new PropertyType("STRING", DataType.DOUBLE);

		assertFalse("string equals some date",   stringType.equals(dateType));
		assertFalse("string equals some bool",   stringType.equals(boolType));
		assertFalse("string equals some int",    stringType.equals(intType));
		assertFalse("string equals some long",   stringType.equals(longType));
		assertFalse("string equals some double", stringType.equals(doubleType));
	}

	public void testIntegerEquals() 
	{
		intType = new PropertyType();
		intType.setName("INTEGER");
		intType.setInteger(true);

		assertTrue("int equals INTEGER", intType.equals(INTEGER));

		PropertyType intType2 = new PropertyType("INTEGER2", DataType.INTEGER);
		assertFalse("int equals int2", intType.equals(intType2));

		dateType   = new PropertyType("INTEGER", DataType.DATE);
		boolType   = new PropertyType("INTEGER", DataType.BOOLEAN);
		stringType = new PropertyType("INTEGER", DataType.STRING);
		longType   = new PropertyType("INTEGER", DataType.LONG);
		doubleType = new PropertyType("INTEGER", DataType.DOUBLE);

		assertFalse("int equals some date",   intType.equals(dateType));
		assertFalse("int equals some bool",   intType.equals(boolType));
		assertFalse("int equals some string", intType.equals(stringType));
		assertFalse("int equals some long",   intType.equals(longType));
		assertFalse("int equals some double", intType.equals(doubleType));
	}

	public void testLongEquals() 
	{
		longType = new PropertyType();
		longType.setName("LONG");
		longType.setLong(true);

		assertTrue("long equals LONG", longType.equals(LONG));

		PropertyType longType2 = new PropertyType("LONG2", DataType.LONG);
		assertFalse("long equals long2", longType.equals(longType2));

		dateType   = new PropertyType("LONG", DataType.DATE);
		boolType   = new PropertyType("LONG", DataType.BOOLEAN);
		stringType = new PropertyType("LONG", DataType.STRING);
		intType    = new PropertyType("LONG", DataType.INTEGER);
		doubleType = new PropertyType("LONG", DataType.DOUBLE);

		assertFalse("long equals some date",   longType.equals(dateType));
		assertFalse("long equals some bool",   longType.equals(boolType));
		assertFalse("long equals some string", longType.equals(stringType));
		assertFalse("long equals some int",    longType.equals(intType));
		assertFalse("long equals some double", longType.equals(doubleType));
	}

	public void testDoubleEquals() 
	{
		doubleType = new PropertyType();
		doubleType.setName("DOUBLE");
		doubleType.setDouble(true);

		assertTrue("double equals DOUBLE", doubleType.equals(DOUBLE));

		PropertyType doubleType2 = new PropertyType("DOUBLE2", DataType.DOUBLE);
		assertFalse("double equals double2", doubleType.equals(doubleType2));

		dateType   = new PropertyType("DOUBLE", DataType.DATE);
		boolType   = new PropertyType("DOUBLE", DataType.BOOLEAN);
		stringType = new PropertyType("DOUBLE", DataType.STRING);
		intType    = new PropertyType("DOUBLE", DataType.INTEGER);
		longType   = new PropertyType("DOUBLE", DataType.LONG);

		assertFalse("double equals some date",   doubleType.equals(dateType));
		assertFalse("double equals some bool",   doubleType.equals(boolType));
		assertFalse("double equals some string", doubleType.equals(stringType));
		assertFalse("double equals some int",    doubleType.equals(intType));
		assertFalse("double equals some long",   doubleType.equals(longType));
	}


} // end class TestPropertyType

