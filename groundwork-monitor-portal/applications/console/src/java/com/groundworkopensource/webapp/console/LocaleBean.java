/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;


public class LocaleBean {
	
	private String baseName;
	public static final String RESOURCE_NAME="messages";
	private String locale= "en";
	
	
	public LocaleBean()
	{
		locale = PropertyUtils.getProperty("locale");
		if (locale == null)
			locale = "en";
		baseName = RESOURCE_NAME + "_" + locale;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	

	

	

}
