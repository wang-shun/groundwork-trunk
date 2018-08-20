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
package org.groundwork.foundation.dao;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/***
 * FoundationQueryList encapsulates a result list from a query along with the total number of potential results.  
 * 
 * @author glee
 *
 */
public class FoundationQueryList implements Serializable
{
	// Total number of potential results
	private int _totalCount = 0;
	
	// Actual result list
	private List _objectList = null;
	
	/*************************************************************************/
	/* Constructors */
	/*************************************************************************/

	public FoundationQueryList (List objectList, int totalCount)
	{
		if (objectList == null)
		{
			return;
		}

		if (totalCount < 0)
		{
			_totalCount = objectList.size();
		}
		else {
			_totalCount = totalCount;
		}
		
		_objectList = objectList;
	}

	/*************************************************************************/
	/* Public Methods */
	/*************************************************************************/	

	public List getResults ()
	{
		return _objectList;
	}
	
	public int getTotalCount ()
	{
		return _totalCount;
	}
	
	public int size()
	{
		return (_objectList == null) ? 0 : _objectList.size();
	}
	
	public Iterator iterator ()
	{
		return (_objectList == null) ? null : _objectList.iterator();
	}
	
	public Object get(int index)
	{
		return (_objectList == null) ? null : _objectList.get(index);
	}
}
