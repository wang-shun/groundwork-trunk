package com.groundwork.agents.vema.base;

import java.util.Date;

/**
 * VemaBaseTimer - a utility class to keep real-time counters
 * for independently timing different VEMA operations.  Meant
 * to be open-ended, simple, convenient to use. 
 * 
 * One need only create VemaBaseTimer objects for each timer
 * that is needed, then repeatedly call method isReadyAndReset()
 * to read triggers (and reset them so that additional periods
 * elapse).
 * 
 * @author rlynch
 *
 */
public class VemaBaseTimer
{
	private String          name;
	private long            period;              // in milliseconds
	private Date            lastDate = null;
	private Date            currDate = null;
	
	public VemaBaseTimer(String name, int minutes, int seconds) 
	{
		this.period = ( 60 * minutes + seconds ) * 1000;
		this.name   = name;
		lastDate    = new Date();
	}
	
	/**
	 * TRUE if...
	 * 1) never called before (i.e. 'fast trigger' at the outset)
	 * 2) if more than {period} milliseconds have elapsed since last reset()
	 * 
	 * FALSE if...
	 * 1) less time than {period} milliseconds has elapsed since last reset()
	 * 
	 * @return
	 */
	public boolean isReady()
	{
		boolean atstart = ( currDate == null );
		long    interval;
		
		currDate = null;
		currDate = new Date();
		interval = currDate.getTime() - lastDate.getTime();
		
		return ( atstart || interval >= period );
	}
	
	public void sleepUntilReady()
	{
		long milliSeconds = 1000 * secondsToGo();
		try { Thread.sleep( milliSeconds ); } catch ( Exception e ){ }
	}
	
	public void sleepUntilReadyAndReset()
	{
		sleepUntilReady();
		reset();
	}
	
	/**
	 * RESETs the time counters.  Does NOT force an immediate trigger.
	 */
	public void reset()
	{
		lastDate = currDate = new Date();
	}
	
	/**
	 * RESETs the time counters AND FORCES an immediate trigger.  Can be useful.
	 */
	public void resetAndTrigger()
	{
		reset();
		currDate = null;      // causes next call to trigger.
	}
	
	/**
	 * Just as it says: checks to see if timer is ready to trigger.  If so, 
	 * returns TRUE.  But it also RESETs the counter so that the next call 
	 * won't return TRUE until another interval has elapsed.
	 * 
	 * THIS is the most useful call, by far.
	 * @return
	 */
	public boolean isReadyAndReset()
	{
		boolean weReady = isReady();
		
		if( weReady )
			reset();
		
		return weReady;
	}
	
	/**
	 * returns SECONDS left before trigger-condition is met.
	 * ZERO means less-than-a-second left.
	 * @return
	 */
	public int secondsToGo()
	{
		long    interval;
		int     seconds;
		
		currDate = new Date();
		interval = currDate.getTime() - lastDate.getTime();
		seconds  = (int)((period - interval) / 1000);
		
		if(      isReady()   ) return 0;
		else if( seconds > 0 ) return seconds;
		else                   return 0;
	}
	
	/**
	 * returns MINUTES left before trigger condition is met.
	 * ZERO means less than a minute left.
	 * 
	 * @return
	 */
	public int minutesToGo()
	{
		return secondsToGo() / 60;
	}
	
	public int minutesPeriod()
	{
		return (secondsPeriod() / 60);
	}
	
	public int secondsPeriod()
	{
		return ((int)(this.period / 1000 ));
	}
	
	/**
	 * urp the name back.
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}
}