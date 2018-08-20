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
