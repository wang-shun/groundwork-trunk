package com.groundwork.agents.vema.base;

import java.util.concurrent.atomic.AtomicBoolean;

/** 
 * A series of "global" singleton methods that set and get 
 * safe booleans, to simplify the code elsewhere (which 
 * prior, suffered from too many object passings...)
 * 
 * @author rlynch
 *
 */
public abstract class VemaBaseState
{
	private static AtomicBoolean runningMonitorAgentCollector = new AtomicBoolean(false);
	private static AtomicBoolean suspendMonitorAgentCollector = new AtomicBoolean(false);
	private static AtomicBoolean isGWOSConfigurationUpdated   = new AtomicBoolean(true);
	
	public static boolean isRunningMonitorAgentCollector()
	{
		return runningMonitorAgentCollector.get();
	}
	
	public static boolean setRunningMonitorAgentCollector( boolean value )
	{
		runningMonitorAgentCollector.set( value );
		return value;
	}
	
	public static boolean isSuspendMonitorAgentCollector()
	{
		return suspendMonitorAgentCollector.get();
	}
	
	public static boolean setSuspendMonitorAgentCollector( boolean value )
	{
		suspendMonitorAgentCollector.set( value );
		return value;
	}

	public static boolean isGWOSConfigurationUpdated()
	{
		return isGWOSConfigurationUpdated.get();
	}
	
	public static boolean setGWOSConfigurationUpdated( boolean value )
	{
		isGWOSConfigurationUpdated.set( value );
		return value;
	}
}
