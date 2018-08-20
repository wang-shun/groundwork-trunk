/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.Properties;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortalContext;
import javax.portlet.PortletContext;
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
				PortletContext PContext = (PortletContext) exContext.getContext();
				props = (Properties) PContext.getAttribute(ConsoleConstants.CONSOLE_PROPS);
				
			} // end if
		} // end if
		
	}

}
