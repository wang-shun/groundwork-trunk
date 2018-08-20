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

import com.groundwork.collage.test.AbstractSpringAssembledTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestPropertyValue extends AbstractSpringAssembledTest {
    Log log = LogFactory.getLog(this.getClass());

    final static String JMX_DATE1 = "JmxDate1";
    final static String JMX_BOOLEAN1 = "JmxBoolean1";
    final static String JMX_STRING1 = "JmxString1";
    final static String JMX_INTEGER1 = "JmxInteger1";
    final static String JMX_LONG1 = "JmxLong1";
    final static String JMX_DOUBLE1 = "JmxDouble1";

    public TestPropertyValue(String x) {
        super(x);
    }

    public void DISABLE_testGetValue() throws Exception {
        PropertyValue propValue;

        String SOME_STRING = "sometring";
        propValue = new PropertyValue(JMX_STRING1, SOME_STRING);
        assertEquals("getValue string", SOME_STRING, propValue.getValue());

        String SOME_DATE = "2005-12-31 12:34:23";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date someDate = df.parse(SOME_DATE);
        propValue = new PropertyValue(JMX_DATE1, someDate);
        assertEquals("getValue date", someDate, propValue.getValue());

        propValue = new PropertyValue(JMX_BOOLEAN1, new Boolean(true));
        assertEquals("getValue boolean true", Boolean.TRUE, propValue.getValue());

        propValue = new PropertyValue(JMX_BOOLEAN1, new Boolean(false));
        assertEquals("getValue boolean false", Boolean.FALSE, propValue.getValue());

        propValue = new PropertyValue(JMX_INTEGER1, new Integer(2));
        assertEquals("getValue integer", new Integer(2), propValue.getValue());

        propValue = new PropertyValue(JMX_LONG1, new Long(999999999));
        assertEquals("getValue long", new Long(999999999), propValue.getValue());

        propValue = new PropertyValue(JMX_DOUBLE1, new Double(2.0));
        assertEquals("getValue double", new Double(2.0), propValue.getValue());
    }

} // end class TestPropertyValue

