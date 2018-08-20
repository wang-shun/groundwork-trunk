package com.groundwork.agents.vema.utils;

public final class aPad
{
	private static int padmax = 0;

	public aPad()
	{
		; // nothing to do.
	}
	/*
	 * aPad( lookfor, message ))
	 * 
	 * AUTO-pads <message> prior to <lookfor> with spaces, based on a 
	 * series of strings with <lookfor> in various columns.  Essentially
	 * this makes output quite readable when a character such as "]" is
	 * in every string from a long list of strings, but where the stuff
	 * before the "]" can also be variable length.
	 */
	public String Pad( String character, String message )
	{
		int charAt = 0;
		
		if(character == null)
			return message;
		
		if((charAt = message.indexOf(character)) > 0 )
		{
			if( charAt >= padmax )
			{
				padmax = charAt;
				return message;
			}
			else
			{
				int padamount = padmax - charAt;
				StringBuffer buildup = new StringBuffer(250);
				buildup.append(message.substring(0, charAt));
				buildup.append(String.format("%" + padamount + "s", ""));
				buildup.append(message.substring(charAt));
				return buildup.toString();
			}
		}
		return message;
	}

}
