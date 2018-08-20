/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
package com.groundworkopensource.webapp.console;

import java.util.Properties;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

public class PropertyUtils {

	private static Properties props = null;

	public static String getProperty(String propertyName) {
		String value = null;
		if (props== null)
		loadProps();
		value = props.getProperty(propertyName);
		return value;
	}

	private static void loadProps() {
		ExternalContext exContext = FacesContext.getCurrentInstance()
				.getExternalContext();
		if (exContext != null) {
			Object context = exContext.getContext();
			if (context != null) {
				ServletContext servContext = (ServletContext) exContext
						.getContext();
				props = (Properties) servContext
						.getAttribute(ConsoleConstants.CONSOLE_PROPS);
			} // end if
		} // end if
		
	}

}
