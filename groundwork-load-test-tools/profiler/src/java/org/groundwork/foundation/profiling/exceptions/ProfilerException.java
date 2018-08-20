package org.groundwork.foundation.profiling.exceptions;

public class ProfilerException extends Exception 
{

	public ProfilerException() {
		super();
	}

	public ProfilerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProfilerException(String message) {
		super(message);
	}

	public ProfilerException(Throwable cause) {
		super(cause);
	}
}
