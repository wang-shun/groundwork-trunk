package org.groundwork.foundation.profiling.exceptions;

public class InvalidConfigFileException extends ProfilerException 
{
	private static final String MSG_FORMAT = "Unable to read configuration file [%1$s]";

	public InvalidConfigFileException(String fileName) 
	{
		super(String.format(MSG_FORMAT, fileName));
	}

	public InvalidConfigFileException(String fileName, Throwable cause) {
		super(String.format(MSG_FORMAT, fileName), cause);
	}
}
