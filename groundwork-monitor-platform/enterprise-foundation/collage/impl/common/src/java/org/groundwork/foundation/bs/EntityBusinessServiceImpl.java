/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.foundation.bs;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * EntityBusinessServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public abstract class EntityBusinessServiceImpl extends AbstractEntityBusinessServiceImpl
{
	private CollageMetrics collageMetrics = null;

	private CollageMetrics getCollageMetrics() {
		if (collageMetrics == null) {
			collageMetrics = CollageFactory.getInstance().getCollageMetrics();
		}
		return collageMetrics;
	}

	public CollageTimer startMetricsTimer() {
		StackTraceElement element = Thread.currentThread().getStackTrace()[2];
		String className = element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
		CollageMetrics collageMetrics = getCollageMetrics();
		return (collageMetrics == null ? null : collageMetrics.startTimer(className, element.getMethodName()));
	}

	public void stopMetricsTimer(CollageTimer timer) {
		CollageMetrics collageMetrics = getCollageMetrics();
		if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
	}

	/*************************************************************************/
	/* Constructors */
	/*************************************************************************/		

	/**
	 * Constructor
	 *
     * @param foundationDAO
     * @param interfaceName
	 * @param componentName
	 */
	protected EntityBusinessServiceImpl(FoundationDAO foundationDAO, String interfaceName, String componentName)
    {
        super(foundationDAO, interfaceName, componentName);
	}

	/*************************************************************************/
	/* Protected Methods */
	/*************************************************************************/

	protected void delete(int objectId) throws BusinessServiceException
	{
		try 
		{
			_foundationDAO.delete(_componentName, objectId);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}			
	}

	protected void delete(int[] objectIds) throws BusinessServiceException 
	{		
		_foundationDAO.delete(_componentName, convertToIntegerCollection(objectIds));		
	}
	
	protected void delete(String[] objectIds) throws BusinessServiceException 
	{		
		try {
			_foundationDAO.delete(_componentName, convertToIntegerCollection(objectIds));
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}			
	}
	
	protected Object queryById (int id)
	throws BusinessServiceException
	{
		try {
			return _foundationDAO.queryById(_componentName, id);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}

	protected List queryById (List<Integer> ids, SortCriteria sortCriteria)
	throws BusinessServiceException {
		try {
			return _foundationDAO.queryById(_componentName,
					ids,
					sortCriteria);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}

	protected List queryById (int[] ids, SortCriteria sortCriteria)
	throws BusinessServiceException
	{
		try {
			return _foundationDAO.queryById(_componentName, 
										convertToIntegerCollection(ids), 
										sortCriteria);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected List queryById (String[] ids, SortCriteria sortCriteria)
	throws BusinessServiceException
	{
		try {
			return _foundationDAO.queryById(_componentName, 
										convertToIntegerCollection(ids), 
										sortCriteria);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	/*************************************************************************/
	/* Private Methods */
	/*************************************************************************/

	private Collection<Integer> convertToIntegerCollection (int[] vals)
	{
		Collection<Integer> col = new ArrayList<Integer>();
		
		// Return empty collection
		if (vals == null)
			return col;
		
		int length = vals.length;

		for (int i = 0; i < length; i++)
		{
			col.add(new Integer(vals[i]));
		}
		
		return col;
	}
	
	private Collection<Integer> convertToIntegerCollection (String[] vals) 
	throws NumberFormatException
	{
		Collection<Integer> col = new ArrayList<Integer>();
		
		// Return empty collection
		if (vals == null)
			return col;
		
		int length = vals.length;
		int id;
		for (int i = 0; i < length; i++)
		{
			// Convert String to Integer
			id = Integer.parseInt(vals[i]);
			
			col.add(new Integer(id));
		}
		
		return col;
	}
}
