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
package com.groundwork.collage.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Contains static convenience methods for formatting and parsing date and
 * time objects or strings, according to the needs of the application
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public class DateTime
{
	static Log log = LogFactory.getLog(DateTime.class);

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";	
	public static final String ZERO = "0";

    private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);

    /**
	 * convenience method to return a properly formatted string into a
	 * date object; expects a string with the format <code>yyyy-MM-dd HH:mm:ss</code>, for
	 * example: <code>2005-12-31 17:23:42</code> ; if the string has a value of "0", this
	 * method returns <code>null</code>; if a <code>ParseException</code> occurs
	 * because the String passed is not properly formatted, the method logs an
	 * error and returns <code>null</code>
	 */
	public static Date parse(String s)
	{
        Date date = null;
        if (s == null || s.equals(""))
            return null;
        if (s.contains("T")) {
            date = parseISODate(s);
        }
        else {
            if (s.contains("-")) {
                try {
                    SimpleDateFormat javaFormatter = new SimpleDateFormat(DATE_FORMAT);
                    date = (ZERO.equals(s)) ? null : javaFormatter.parse(s);
                }
                catch (Exception e) {
                    date = null;
                }
            }
        }
        if (date == null) {
            try {
                long time = Long.parseLong(s);
                return new Date(time);
            }
            catch (Exception e) {
                log.error("Unable to parse date string '" + s + "' : " + e);
                return null;
            }
        }
        return date;
	}

    /**
     * Format date into string. For example: <code>2005-12-31 17:23:42</code>.
     * If the specified date is null, <code>0</code> will be returned.
     *
     * @param date date or null
     * @return formatted date
     */
    public static String format(Date date) {
        if (date == null) {
            return ZERO;
        }
        synchronized (SIMPLE_DATE_FORMAT) {
            return SIMPLE_DATE_FORMAT.format(date);
        }
    }

    public static Date parseISODate(String s) {
        try {
            DateTimeFormatter localParser = ISODateTimeFormat.dateTimeParser();
            Date date = localParser.parseDateTime(s).toDate();
            //debug(date);
            return date;
        }
        catch(Exception e2) {
            return null;
        }
    }

    private static void debug(Date date) {
        final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        DateFormat dateFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getDefault());
        System.out.println("--- converted date: " + dateFormat.format(date));
    }

}


