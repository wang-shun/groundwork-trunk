/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.reportserver.pagebeans;

import java.text.Collator;
import java.util.Comparator;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Common base class for all page beans.
 *
 */
public class PageBean 
{
	private static final String SYSTEM_PROP_CONFIG_FILE = "org.groundwork.reportserver.config";
	private static final String CONTEXT_PARAM_CONFIG_FILE = "reportServerConfig";
	
	// Property Keys
	private static final String PROP_REPORT_DIRECTORY = "org.groundwork.report.directory";
	private static final String PROP_BIRT_VIEWER_URL = "org.groundwork.report.birt.url";
	private static final String PROP_REPORT_FILE_FILTER = "org.groundwork.report.filter";
	private static final String PROP_FIREFOX_1_5_SUPPORT = "org.groundwork.report.firefox_1_5.support";
	
	// Default Property Values
	private static final String DEFAULT_BIRT_VIEWER_URL = "http://localhost:8080/birtviewer/frameset?__report";
	private static final String DEFAULT_FILE_FILTER = "";
	
	// For now, we only have one static set of properties for the page bean
	// so we don't have to continue to read in a properties file
	private static Properties CONFIGURATION_PROPS = null;	
	
	private ServletContext context = null;
	
	private Log log = LogFactory.getLog(this.getClass()); 
	
	public PageBean (ServletContext context)
	{
		if (context == null)
		{
			throw new IllegalArgumentException("Invalid null ServletContext parameter.");
		}
		
		this.context = context;
		
		// Load static properties
		if (CONFIGURATION_PROPS == null) 
		{
			CONFIGURATION_PROPS = new Properties();
			
			String configFile = null;
			try {

				// The system property overrides the context parameter.
				configFile = System.getProperty(SYSTEM_PROP_CONFIG_FILE, null);
				
				if (configFile == null)
				{
					configFile = context.getInitParameter(CONTEXT_PARAM_CONFIG_FILE);
				}
								
				// Use default file location
				if (configFile == null)
				{
					throw new Exception("Property file location not defined as system property (-D" 
							+ SYSTEM_PROP_CONFIG_FILE 
							+ " or <context-param> " +
							CONTEXT_PARAM_CONFIG_FILE
							+ ".  Using defaults");
				}
				else {
					FileInputStream stream = new FileInputStream(configFile);					
					CONFIGURATION_PROPS.load(stream);
				}
			}
			catch (Exception e)
			{				
				log.error("Unable to load property file. Using defaults - " + configFile, e);
			}				
		}
		
	}	
	
	public String getReportDirectory ()
	{
		return CONFIGURATION_PROPS.getProperty(PROP_REPORT_DIRECTORY, this.context.getRealPath("/") + File.separator);
	}
	
	public String getBIRTViewerURL ()
	{
		return CONFIGURATION_PROPS.getProperty(PROP_BIRT_VIEWER_URL, DEFAULT_BIRT_VIEWER_URL);
	}
	
	public String getReportFileFilter ()
	{
		return CONFIGURATION_PROPS.getProperty(PROP_REPORT_FILE_FILTER, DEFAULT_FILE_FILTER);
	}
	
	public boolean isFirefox15Supported ()
	{
		String propVal = CONFIGURATION_PROPS.getProperty(PROP_FIREFOX_1_5_SUPPORT, null);
		
		return Boolean.valueOf(propVal);
	}
	
	protected static class FileComparator implements Comparator<File>
	{
		private Collator c = Collator.getInstance();
	
		public int compare(File file1, File file2)
		{
		  if(file1 == file2) return 0;
	
		  if(file1.isDirectory() && file2.isFile())
		    return -1;
		  
		  if(file1.isFile() && file2.isDirectory())
		    return 1;
	
		  return c.compare(file1.getName(), file2.getName());
		}
	}	
}
