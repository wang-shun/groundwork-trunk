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

package org.groundwork.foundation.reportserver;
 
import java.io.FileFilter;
import java.io.File;
import java.util.HashSet;

/**
 * Simple file filter class which takes a comma-separated list of file extensions
 * to INCLUDE.
 * 
 *
 */
public class ExtensionFileFilter implements FileFilter
{
	private static final String COMMA = ",";
	private static final String DOT = ".";
	
	private HashSet extensions = null;
	
	public ExtensionFileFilter (String filter)
	{
		if (filter != null && filter.trim().length() > 0)
		{
			String[] exts = filter.split(COMMA);
			int numExts = exts.length;
			
			this.extensions = new HashSet(numExts);
			
			for (int i = 0; i < numExts; i++)
			{
				this.extensions.add(exts[i]);
			}
		}	
	}	
	
	public boolean accept (File file)
	{
		// No extensions defined then all files are accepted
		if (extensions == null)
		{
			return true;
		}
		
		if (file == null)
		{
			return false;
		}
		
		// All Directories are excepted
		if (file.isDirectory() == true)
		{
			return true;
		}
		
		String fileName = file.getName();
		
		int pos = fileName.lastIndexOf(DOT);
		
		// No extension and we are not allowing hidden files (.*)
		if (pos < 1 || pos == (fileName.length() - 1))
		{
			return false;
		}
		
		String ext = fileName.substring(pos + 1);
		
		// Search for ext in our list		
		return this.extensions.contains(ext);
	}
}