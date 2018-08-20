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

import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

/**
 * ProjectCriteria wraps hibernate Projections and maintains a list of projections for use in criteria query
 *
 */
public class ProjectionCriteria extends Criteria implements Serializable
{

	private ProjectionList _projectionList = Projections.projectionList();
	
	/*************************************************************************/
	/* Constructors */
	/*************************************************************************/
	
	private ProjectionCriteria (String propertyName, Projection projection)
	{
		_projectionList.add(projection);
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName));		
	}
	
	private ProjectionCriteria (Projection projection)
	{
		_projectionList.add(projection);
	}
	
	/*************************************************************************/
	/* Public Methods */
	/*************************************************************************/
			
	public void addProjection (ProjectionCriteria projectionCriteria)
	{
		if (projectionCriteria == null)
			throw new IllegalArgumentException("Invalid null ProjectionCritieria parameter.");
	
		// Add each projection
		int length = _projectionList.getLength();
		for (int i = 0; i < length; i++)
		{
			_projectionList.add(_projectionList.getProjection(i));
		}
		
		_criteriaAliases.addAll(projectionCriteria.getCriteriaAliases());
	}
	
	/**
	 * The query row count, ie. <tt>count(*)</tt>
	 */
	public static ProjectionCriteria rowCount()
	{
		return new ProjectionCriteria(Projections.rowCount());
	}	
	
	/**
	 * A property value count
	 */
	public static ProjectionCriteria count(String propertyName) 
	{
		return new ProjectionCriteria(propertyName, 
									  Projections.count(getCriterionAlias(propertyName)));		
	}
	
	/**
	 * A distinct property value count
	 */
	public static ProjectionCriteria countDistinct(String propertyName) 
	{
		return new ProjectionCriteria(propertyName, 
				Projections.countDistinct(getCriterionAlias(propertyName)));
	}
	
	/**
	 * A property maximum value
	 */
	public static ProjectionCriteria max(String propertyName)
	{
		return new ProjectionCriteria(propertyName, 
					Projections.max(getCriterionAlias(propertyName)));
	}
	
	/**
	 * A property minimum value
	 */
	public static ProjectionCriteria min(String propertyName)
	{
		return new ProjectionCriteria(propertyName, 
				Projections.min(getCriterionAlias(propertyName)));
	}
	
	/**
	 * A property average value
	 */
	public static ProjectionCriteria avg(String propertyName)
	{
		return new ProjectionCriteria(propertyName, 
				Projections.avg(getCriterionAlias(propertyName)));
	}
	
	/**
	 * A property value sum
	 */
	public static ProjectionCriteria sum(String propertyName)
	{
		return new ProjectionCriteria(propertyName, 
				Projections.sum(getCriterionAlias(propertyName)));
	}
	
	/**
	 * A grouping property value
	 */
	public static ProjectionCriteria groupProperty(String propertyName)
	{
		return new ProjectionCriteria(propertyName, 
				Projections.groupProperty(getCriterionAlias(propertyName)));
	}
	
	/**
	 * A projected property value
	 */
	public static ProjectionCriteria property(String propertyName)
	{
		return new ProjectionCriteria(propertyName, 
						Projections.property(getCriterionAlias(propertyName)));
	}
	
	/**
	 * A projected identifier value
	 */
	public static ProjectionCriteria id() 
	{
		return new ProjectionCriteria(Projections.id());
	}
		
	/*************************************************************************/
	/* Protected Methods */
	/*************************************************************************/
	
	protected ProjectionList getProjectionList ()
	{
		return _projectionList;
	}	
}