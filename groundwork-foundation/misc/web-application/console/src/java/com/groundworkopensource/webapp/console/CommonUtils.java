package com.groundworkopensource.webapp.console;

import java.util.Date;

public class CommonUtils {
	
	public static boolean isEmpty(String val)
	{
		if (val!=null && !val.equalsIgnoreCase(""))
			return false;
		else
			return true;
	}
	
	public static boolean isEmpty(Date val)
	{
		if (val!=null && !val.equals(""))
			return false;
		else
			return true;
	}

}
