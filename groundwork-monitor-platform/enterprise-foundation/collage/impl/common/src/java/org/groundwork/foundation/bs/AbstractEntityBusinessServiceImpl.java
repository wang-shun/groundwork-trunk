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

import com.groundwork.collage.CollageAccessor;
import com.groundwork.collage.CollageFactory;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.ProjectionCriteria;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

/**
 * AbstractEntityBusinessServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public abstract class AbstractEntityBusinessServiceImpl extends BusinessServiceImpl
{
	/* Business Service Interface Name */
	protected String _interfaceName = null;
	
	/* Hibernate Component Name */
	protected String _componentName = null;
	
	/* FoundationDAO */
	protected FoundationDAO _foundationDAO = null;
	
	/* Collage Factory */
	private CollageAccessor _collage =  CollageFactory.getInstance();
	
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
	protected AbstractEntityBusinessServiceImpl(FoundationDAO foundationDAO, String interfaceName, String componentName)
	{
		if (interfaceName == null || interfaceName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty interface name parameter.");
		
		if (componentName == null || componentName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty component name parameter.");
		
		_interfaceName = interfaceName;
		_componentName = componentName;
		
		/* Load FoundationDAO */
		_foundationDAO =  foundationDAO;//(FoundationDAO)_collage.getAPIObject(CollageFactory.FOUNDATION_DAO);
	}

	/*************************************************************************/
	/* Protected Methods */
	/*************************************************************************/

	protected Object create() throws BusinessServiceException
	{		
		if (_collage == null)
			_collage = CollageFactory.getInstance();
		
		try {
			return _collage.getAPIObject(_interfaceName);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected Object create(String interfaceName) throws BusinessServiceException 
	{
		if (_collage == null)
			_collage = CollageFactory.getInstance();
		
		try 
		{
			return _collage.getAPIObject(interfaceName);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}

	protected void delete(Collection persistentObjects) throws BusinessServiceException
	{
		try {				
			_foundationDAO.delete(persistentObjects);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected void delete(Object persistentObject) throws BusinessServiceException 
	{
		if (persistentObject == null)
			throw new IllegalArgumentException("Invalid null persistent object parameter.");
		
		try {
			_foundationDAO.delete(persistentObject);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
		
	}
	
	protected void save(Object persistentObject) throws BusinessServiceException
	{
		if (persistentObject == null)
			throw new IllegalArgumentException("Invalid null persistent object parameter.");
		
		try {
			_foundationDAO.save(persistentObject);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected void save(Collection persistentObjects) throws BusinessServiceException
	{
		if (persistentObjects == null)
			throw new IllegalArgumentException("Invalid null persistent object collection parameter.");
		
		try {
			_foundationDAO.save(persistentObjects);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected FoundationQueryList query(FilterCriteria filterCriteria,
									 SortCriteria sortCriteria, 
									 int firstResult, 
									 int maxResults)
	throws BusinessServiceException
	{
		try
		{
			return _foundationDAO.query(_componentName, 
									filterCriteria, 
									sortCriteria, 
									null, 
									firstResult, 
									maxResults);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected FoundationQueryList query(FilterCriteria filterCriteria,
			 SortCriteria sortCriteria, 
			 ProjectionCriteria projectionCriteria,
			 int firstResult, 
			 int maxResults)
	throws BusinessServiceException
	{
		try {
			return _foundationDAO.query(_componentName, 
									filterCriteria, 
									sortCriteria, 
									projectionCriteria, 
									firstResult, 
									maxResults);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected List query(FilterCriteria filterCriteria, 
			 			 SortCriteria sortCriteria)
	throws BusinessServiceException
	{
		try {
			return _foundationDAO.query(_componentName, 
									filterCriteria, 
									sortCriteria);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		}
	}
	
	protected int queryCount(FilterCriteria filterCriteria) throws BusinessServiceException
	{
		try {
			return _foundationDAO.queryCount(_componentName, filterCriteria);
		}
		catch (Exception e)
		{
			throw new BusinessServiceException(e);
		} 
	}
	
	protected void flush ()
	{
		_foundationDAO.flush();
	}
	
	protected void evict (Object persistentObject)
	{
		_foundationDAO.evict(persistentObject);
	}
}
