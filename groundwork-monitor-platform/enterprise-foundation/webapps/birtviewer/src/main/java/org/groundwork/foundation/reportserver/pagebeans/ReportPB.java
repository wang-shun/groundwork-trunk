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

 import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.reportserver.ExtensionFileFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
/**
 * Report Page Bean class responsible for reading the file system for 
 * available reports.
 * 
 * @author glee
 *
 */
public class ReportPB extends PageBean
{
	private static final String JSON_EMPTY_ARRAY = "[]";
	private static final String DOT = ".";

	private Log log = LogFactory.getLog(this.getClass()); 
	
	public ReportPB (ServletContext context)
	{
		super(context);		
	}
	
	/**
	 * Returns a string representing a JSON array of tree nodes to be used by the DOJO tree widget.
	 * @param directory
	 * @param bDirectoryOnly
	 * @return
	 * @throws JSONException
	 */
	public String getDirectoryChildren (String directory, boolean bDirectoryOnly) throws JSONException
	{
		String absDirectory = null;
		
		if (directory == null || directory.length() == 0)
		{
			absDirectory = this.getReportDirectory();
		}
		else {
			// directory passed in is relative to the root directory;
			absDirectory = this.getReportDirectory() + directory;
		}

		if (log.isInfoEnabled())
			log.info("getDirectoryChildren for directory, " + absDirectory);

		File root = new File(absDirectory);
		ExtensionFileFilter fileFilter = new ExtensionFileFilter(this.getReportFileFilter());
		File[] files = root.listFiles(fileFilter);
		
		if (files != null && files.length > 0)
		{
			// Sort Files directories first and then files in alphabetical order
			Arrays.sort(files, new FileComparator());
			
			JSONArray jsonArray = new JSONArray();
			
			for (int i = 0; i < files.length; i++)
			{
				File file = files[i];
				String name = file.getName();
								
				JSONObject jsonObj = new JSONObject();								
				
				// Widget Id is the relative path from the root, so remove root directory from the name
				// The widget id is used to pass to the report servlet to render the report which is a 
				// relative path from the reports directory
				String widgetId = (directory == null) ? name : directory + name;
				if (file.isDirectory() == true)
				{ 
					widgetId += File.separator;
				}
				else if (bDirectoryOnly == true) // Ignore files
				{
					continue;
				}
				
				// Remove File Extension from name to be displayed in the tree
				int pos = name.lastIndexOf(DOT);
				String title = name;
				if (pos > 0)
				{
					title = name.substring(0, pos);
				}
				
				// Note:  We have to set widget id to the file / directory name
				// b/c title is not coming back from the tree widgets json
				jsonObj.put("title", title);
				jsonObj.put("objectId", name);
				jsonObj.put("widgetId", widgetId);
				jsonObj.put("isFolder", file.isDirectory());	
				
				jsonArray.put(jsonObj);
			}

			if (log.isInfoEnabled())
				log.info("getDirectoryChildren JSON Returned - [" + jsonArray + "]");

			return jsonArray.toString();
		}
		
		return JSON_EMPTY_ARRAY;
	}	
	
	/**
	 * Returns a sorted list of directories/ reports in the specified directory.  If the directory is null or empty
	 * a list of directories / reports in the report root directory will be returned.
	 *
	 * @param directory
	 * @param bIncludeDirectories If false only files are returned and no directories.
	 * @return
	 */
	public List getDirectoryFiles (String directory, boolean bIncludeDirectories)
	{
		String absDirectory = null;
		
		if (directory == null || directory.length() == 0)
		{
			absDirectory = this.getReportDirectory();
		}
		else {
			// directory passed in is relative to the root directory;
			absDirectory = this.getReportDirectory() + directory;
		}
		
		if (log.isInfoEnabled())
			log.info("getDirectoryFiles for directory, " + absDirectory);
				
		ArrayList fileList = new ArrayList(10);		
		File root = new File(absDirectory);
		ExtensionFileFilter fileFilter = new ExtensionFileFilter(this.getReportFileFilter());
		File[] files = root.listFiles(fileFilter);
		
		if (files != null && files.length > 0)
		{
			// Sort Files directories first and then files in alphabetical order
			Arrays.sort(files, new FileComparator());
			
			for (int i = 0; i < files.length; i++)
			{
				File file = files[i];
				String name = file.getName();
				
				// Ignore directories if specified
				if (bIncludeDirectories == false && file.isDirectory())
				{
					continue;
				}
				
				fileList.add(file);
			}					
		}
		
		return fileList;
	}	
}