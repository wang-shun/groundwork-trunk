/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@itgroundwork.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.profiling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.exceptions.ConfigFileParseException;
import org.groundwork.foundation.profiling.exceptions.InvalidConfigFileException;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;

/**
 * @author rruttimann@groundworkopensource.com
 * 
 * Entry point for Profiling Framework for Foundation
 *
 */
public class ProfileFoundation 
{
	/** Enable log4j */
	protected static Log log = LogFactory.getLog(ProfileFoundation.class);
	
	private WorkloadMgr _workloadMgr = null;

	public ProfileFoundation (String[] args)
	throws InvalidConfigFileException, ConfigFileParseException, ProfilerException
	{
		String configFile = "foundation-profiler.xml";
		String sessionName = null;
		for (int i = 0; i < args.length;)
		{
			String arg = args[i];
			
			if (i++ > args.length)
				break;
			
			// Get switch value which may contain spaces therefore will be parsed into multiple args[]
			StringBuilder sb = new StringBuilder(32);			
			while (i < args.length)
			{
				String val = args[i];
				
				if (val.startsWith("-") && isValidSwitch(val))
				{
					break;
				}
								
				sb.append(val);
				
				// Note: we are replacing all whitespace with a single space
				sb.append(" ");
				                  
				 i++;
			}
						
			if ("-config".equalsIgnoreCase(arg) == true)
			{
				configFile = sb.toString().trim();
			}
			else if ("-session".equalsIgnoreCase(arg) == true)
			{
				sessionName = sb.toString().trim();				
			}
			else if ("/?".equals(arg) == true)
			{				
				System.out.println("Optional Arguments:\n\t-session <session name >\n\t-config <profiler configuration file>");
				System.out.println("\n\nNOTE:  Spaces are allowed in argument values.  Example:  -session Session A B C");
			}
		}
		
		WorkloadMgr.startWorkloads(configFile, sessionName);
	}
	
	public static void parseConfiguration ()
	{	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{		
		try {
			new ProfileFoundation(args);
		}
		catch (Exception e)
		{
			log.error(e + " - " + ((e.getCause() == null) ? "" : e.getCause().toString()));
			e.printStackTrace();
		}
	}
	
	private static boolean isValidSwitch (String val)
	{
		if (val == null || val.length() < 2)
		{
			return false;
		}
		
		if ("-config".equalsIgnoreCase(val) == true)
		{
			return true;
		}
		else if ("-session".equalsIgnoreCase(val) == true)
		{
			return true;
		}
		else if ("/?".equals(val) == true)
		{		
			return true;
		}
		
		return false;
	}
}
