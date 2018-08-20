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

/*Created on: May 30, 2006 */

package com.groundwork.collage.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper class for keeping track of what object has changed
 * 
 * @author rruttimann@groundworkopensource.com
 *
 */

public class StatisticGroupState {
	private AtomicBoolean		isDirty = new AtomicBoolean(true);
	private ConcurrentHashMap	objectLookup = new ConcurrentHashMap();
	
	/**
	 * @return Returns the isDirty.
	 */
	public boolean getIsDirty() {
		return isDirty.get();
	}

	/**
	 * @param isDirty The isDirty to set.
	 */
	public void setIsDirty(boolean isDirty) {
		this.isDirty.set(isDirty);
	}
	
	/**
	 * @param elementName
	 */
	public void addElementToLookup(String elementName)
	{
		if (elementName != null && this.objectLookup.containsKey(elementName) == false)
		{
			this.objectLookup.put(elementName,elementName);
			this.isDirty.set(true);
		}
	}
	
	public void removeElementFromLookup(String elementName)
	{
		if (elementName != null && this.objectLookup.containsKey(elementName) == true)
		{
			this.objectLookup.remove(elementName);
			this.isDirty.set(true);
		} 
	}
	
	public void updateElement(String elementName)
	{
		if (elementName != null && this.objectLookup.containsKey(elementName) == true)
		{
			// Element exists in list. mark collection as dirty
			this.isDirty.set(true);
		}	
	}
}
