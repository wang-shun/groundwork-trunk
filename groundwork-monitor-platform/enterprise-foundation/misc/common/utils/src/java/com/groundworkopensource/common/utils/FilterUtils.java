/*
 * Common -Utilities framework.
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
package com.groundworkopensource.common.utils;

import java.io.FileInputStream;

import com.wutka.jox.JOXBeanInputStream;

public class FilterUtils {

	/**
	 * Converts the xml to java object
	 * 
	 * @param fileName
	 *            - Fully qualified file path.
	 * @param clazzName
	 *            - Fully qualified Class name.Ex,
	 *            com.groundworkopensource.common.utils.Filters
	 * @return
	 * @throws FilterException
	 */
	public static Object loadFilter(String fileName, String clazzName)
			throws FilterException {
		if (fileName == null)
			throw new FilterException("Invalid file name");
		if (clazzName == null)
			throw new FilterException("Invalid class name");
		Object filterBean = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			JOXBeanInputStream joxIn = new JOXBeanInputStream(in);
			filterBean = (Object) joxIn.readObject(Class.forName(clazzName));
		} catch (ClassNotFoundException exc) {
			throw new FilterException(exc.getMessage() + " not found");
		} catch (Exception exc) {
			throw new FilterException(exc.getMessage());
		} // end try/catch
		return filterBean;
	}

}
