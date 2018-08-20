/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceUtils {
	
	public static String getLocalizedMessage(String key)
	{
		ResourceBundle resourceBundle =
		      ResourceBundle.getBundle(LocaleBean.RESOURCE_NAME, new Locale (ConsoleHelper.getLocaleBean().getLocale()));
		String value = resourceBundle.getString(key);
		return value;
	}

}
