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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Base class for all Criteria (e.g. FilterCriteria, SortCriteria, ProjectCriteria
 * 
 */
public abstract class Criteria implements Serializable
{
	// Properties that need to be aliased when Criteria is created / used.
	protected Collection<CriteriaAlias> _criteriaAliases = new HashSet<CriteriaAlias>();
	
	/**
	 * Returns collection of criteria aliases
	 * 
	 * @return
	 */
	protected Collection<CriteriaAlias> getCriteriaAliases ()
	{
		return _criteriaAliases;
	}		
	
	/**
	 * Add alias to set 
	 * 
	 * @param alias
	 */
	protected void addCriteriaAlias (CriteriaAlias alias)
	{
		if (alias == null)
			throw new IllegalArgumentException("Invalid null / empty criteria alias parameter.");
		
		_criteriaAliases.add(alias);
	}
	
	/**
	 * Returns criterion alias (Restriction) for property name.
	 * Example:  Property Name = a.b.c => b.c
	 * @param propertyName
	 * @return
	 */
	protected static String getCriterionAlias (String propertyName)
	{
		return CriteriaAlias.getCriterionAlias(propertyName);
	}
	
	/**
	 * Updates the property name value map with proper Criterion aliases.
	 * 
	 * @param propertyNameValues
	 * @return
	 */
	protected static Map<String, Object> getCriterionAliases(Map<String, Object> propertyNameValues)
	{
		return getCriterionAliases(propertyNameValues);
	}
}
