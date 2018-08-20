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
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

/**
 * SortCritieria is a collection of Sort allowing the user to define multiple properties to order the result set by
 *
 */
public final class SortCriteria extends Criteria implements Serializable 
{
	// List of sort orders - Order in list indicates order of sort.
	private List<Order> _sortList = new ArrayList<Order>(1);	
	
	private ProjectionList _projList = Projections.projectionList();
	
	/*************************************************************************/
	/* Constructors */
	/*************************************************************************/
	
	private SortCriteria (String propertyName, boolean bAscending)
	{
		addSort(getCriterionAlias(propertyName), bAscending);
		
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName));
	}	

	/*************************************************************************/
	/* Public Methods */
	/*************************************************************************/	
	
	/**
	 * Convenience method to construct ascending SortCriteria
	 * 
	 * @param propertyName
	 * @return
	 */
	public static SortCriteria asc (String propertyName)
	{
		return new SortCriteria(propertyName, true);
	}

	/**
	 * Convenience method to construct descending SortCriteria
	 * 
	 * @param propertyName
	 * @return
	 */
	public static SortCriteria desc (String propertyName)
	{
		return new SortCriteria(propertyName, false);
	}
	
	/**
	 * Adds sort to end of the sort list
	 * @param sort
	 */
	public void addSort (String propertyName, boolean bAscending)
	{
		if (propertyName == null || propertyName.length() == 0)
		{
			throw new IllegalArgumentException("SortCriteria.addSort(String propertyName, boolean bAscending) - Invalid null propertyName parameter");
		}
		
		_sortList.add((bAscending == true) ? Order.asc(getCriterionAlias(propertyName)) 
										   : Order.desc(getCriterionAlias(propertyName)));
		
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName));
		
		_projList.add(Projections.property(propertyName));
	}

	/**
	 * Adds sort to thesort list at the index specified.
	 * @param sort
	 * @param index
	 */
	public void addSort (String propertyName, boolean bAscending, int index)
	{
		if (propertyName == null || propertyName.length() == 0)
		{
			throw new IllegalArgumentException("SortCriteria.addSort(String propertyName, boolean bAscending, int index) - Invalid null propertyName parameter");
		}
		
		_sortList.add(index, 
				(bAscending == true) ?  Order.asc(getCriterionAlias(propertyName)) 
									 : Order.desc(getCriterionAlias(propertyName)));
		
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName));
	}				
	
	/*************************************************************************/
	/* Protected Methods */
	/*************************************************************************/
	
	protected List<Order> getSortList ()
	{
		return _sortList;
	}	
	
	protected ProjectionList getProjectionList ()
	{
		return _projList;
	}	
}
