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

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.util.DateTime;

public class TestDateTime extends TestCase
{
	Log log = LogFactory.getLog(this.getClass());

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	/** executed prior to each test */
	protected void setUp() { }

	/** executed after each test */
	protected void tearDown() { }

	public void testParse() throws Exception
	{
		String dateString;
		Date date;

		dateString = "2005-12-31 23:59:59";
		date = DateTime.parse(dateString);
		assertEquals("non-zero dateString to date", dateString, formatter.format(date));

		dateString = "";
		date = DateTime.parse(dateString);
		assertNull("bad dateString to date", date);
	}

} // end class TestDateTime

