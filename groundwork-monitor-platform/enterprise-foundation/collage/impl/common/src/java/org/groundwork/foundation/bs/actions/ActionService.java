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
package org.groundwork.foundation.bs.actions;

import java.util.List;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;

/**
 * @author glee
 *
 */
/**
 * @author glee
 *
 */
public interface ActionService extends BusinessService 
{
	///////////////////////////////////////////////////////////////////////////
	// Query Methods
	///////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Retrieve Action instances which match specified criteria.
	 * 
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	public FoundationQueryList getActionsByCriteria(FilterCriteria filterCriteria, 
											SortCriteria sortCriteria, 
											int firstResult, 
											int maxResults) throws BusinessServiceException;
	
	/**
	 * Retrieve Action instances for a specific application type.
	 * 
	 * @param appTypeName
	 * @param includeSystem
	 * @return
	 * @throws BusinessServiceException
	 */
	public FoundationQueryList getActionByApplicationType(String appTypeName, boolean includeSystem) throws BusinessServiceException;
		
	/**
	 * Retrieve ActionType instances for specified criteria.
	 * 
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws BusinessServiceException
	 */
	public FoundationQueryList getActionTypes(FilterCriteria filterCriteria, 
											SortCriteria sortCriteria, 
											int firstResult, 
											int maxResults) throws BusinessServiceException;

	///////////////////////////////////////////////////////////////////////////
	// Action Methods
	///////////////////////////////////////////////////////////////////////////

	
	/**
	 * Performs the set of actions identified by the list of ActionPerform instances
	 * @param actionIds
	 * @return
	 * @throws BusinessServiceException
	 */
	public List<ActionReturn> performActions (List<ActionPerform> actionPerforms) throws BusinessServiceException;
}
