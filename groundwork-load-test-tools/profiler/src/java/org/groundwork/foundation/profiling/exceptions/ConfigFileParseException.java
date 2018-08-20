package org.groundwork.foundation.profiling.exceptions;

public class ConfigFileParseException extends ProfilerException 
{
	private static final String MSG_FORMAT = "Unable to parse configuration file [%1$s]";
	private static final String MSG_FORMAT_WITH_MSG = 
		"Unable to parse configuration file [%1$s] - %2$s";

	public ConfigFileParseException(String fileName) 
	{
		super(String.format(MSG_FORMAT, fileName));
	}

	public ConfigFileParseException(String fileName, String msg) 
	{
		super(String.format(MSG_FORMAT_WITH_MSG, fileName, msg));
	}
	
	public ConfigFileParseException(String fileName, Throwable cause) {
		super(String.format(MSG_FORMAT, fileName), cause);
	}

	public ConfigFileParseException(String fileName, String msg, Throwable cause) {
		super(String.format(MSG_FORMAT_WITH_MSG, fileName, msg), cause);
	}

}
