/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundwork.agents.appservers.utils;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class is the utility for collectors
 * 
 * @author Arul Shanmugam
 * 
 */
public class StatUtils {

	private static Logger log = Logger.getLogger(StatUtils.class);

	/**
	 * Parses the stat value from the string.
	 * 
	 * @param statStr
	 * @param name
	 * @return
	 */
	public static int getStatValue(String statStr, String name) {
		int value = -1;
		if (name != null && !name.equalsIgnoreCase("")) {
			int start = statStr.indexOf(name + "=");
			int end = statStr.indexOf(",", start);
			if (end == -1)
				end = statStr.length();
			value = Integer.parseInt(statStr.substring(start + name.length()
					+ 1, end));
		} // end if
		return value;
	}

	/*
	 * public static Properties getProperties(String properties) {
	 * StatUtils.getClass().getClassLoader().getResourceAsStream(properties).
	 * return ResourceBundle.getBundle(properties); }
	 */

	public static Properties readProperties(String xmlProperties) {

		Properties properties = new Properties();
		try {
			InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlProperties);
			if (fis != null)
			properties.loadFromXML(fis);
		} catch (Exception exc) {
			log.error(exc.getMessage());
		} // end try/catch
		return properties;
	}

	public static String stripSpecialChars(String unStrippedString) {
		String pattern = "[^0-9 ^A-Z ^a-z . _]";
		String strippedString = unStrippedString.replaceAll(pattern, "");
		return strippedString.replaceAll(" ", "_");
	}

}
