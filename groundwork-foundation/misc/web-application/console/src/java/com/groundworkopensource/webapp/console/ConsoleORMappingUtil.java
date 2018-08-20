package com.groundworkopensource.webapp.console;

import java.util.HashMap;

public class ConsoleORMappingUtil {
	
	private static HashMap<String,String> map=null;
	
	public static String findORMapping(String key)
	{
		if (map==null)
		{
			map = new HashMap<String,String>();
			map.put("reportDate", "reportDate");
			map.put("msgCount", "msgCount");
			map.put("device", "device.identification");
			map.put("monitorStatus", "monitorStatus.name");
			map.put("severity", "severity.name");
			map.put("applicationType", "applicationType.name");
			map.put("textMessage", "textMessage");
			map.put("lastInsertDate", "lastInsertDate");
			map.put("firstInsertDate", "firstInsertDate");
		}
		return map.get(key);
	}
}
