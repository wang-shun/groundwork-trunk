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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author glee
 *
 */
public class CriteriaAlias implements Serializable
{
	private static final String DOT = ".";
	
	// A dot-seperated property path
	private String _associationPath = null;
	
	// The alias to assign to the joined association (for later reference).
	private String _alias = null;
	
	/**
	 * Private Constructor
	 * 
	 * @param associationPath
	 * @param alias
	 */
	private CriteriaAlias (String associationPath, String alias)
	{
		if (associationPath == null || associationPath.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty associationPath parameter.");
	
		if (alias == null || alias.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty alias parameter.");
		
		_associationPath = associationPath;
		_alias = alias;
	}
	
	protected String getAssociationPath ()
	{
		return _associationPath;
	}
	
	protected String getAlias ()
	{
		return _alias;
	}	
	
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		
		if ((obj instanceof CriteriaAlias) == false)
			return false;
		
		CriteriaAlias alias = (CriteriaAlias)obj;
		
		if (this._associationPath.equals(alias.getAssociationPath()))
			return true;
		
		return false;
	}

	public int hashCode()
	{
		return _associationPath.hashCode();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		
		sb.append("AssociationPath=");
		sb.append(_associationPath);		
		sb.append(", Alias=");
		sb.append(_alias);

		return sb.toString();
	}
	
	/**
	 * Utility method to create list of aliases from a property name.
	 * 
	 * @param propertyName
	 * @return
	 */
	protected static Collection<CriteriaAlias> createAliases(String propertyName)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty propertyName parameter.");
		
		Collection<CriteriaAlias> col = new HashSet<CriteriaAlias>(1);
		
		// Nothing to do if there is no DOT b/c it is a property directly related to the entity
		int pos = propertyName.indexOf(DOT);
		if (pos < 0)
			return col;
		
		String alias = null;
		String associationPath = null;		
		
		// Add aliases for each reference (e.g. a.b.c -> aliases for a and a.b will be added			
		while (pos > 0)
		{
			associationPath = propertyName.substring(0, pos);
						
			if (alias != null)
				alias += DOT;
			
			int lastPos = associationPath.lastIndexOf(DOT);
			if (lastPos > 0)
				alias = associationPath.substring(lastPos + 1);
			else
				alias = associationPath;
			
			col.add(new CriteriaAlias(associationPath, alias));
					
			if (propertyName.length() <= pos)
				break;
			
			pos = propertyName.indexOf(DOT, pos + 1);
		}	
		
		return col;
	}
	
	/***
	 * Utility method to create a collection of aliases for a set of property names.
	 * 
	 * @param propertyNames
	 * @return
	 */
	protected static Collection<CriteriaAlias> createAliases(Set<String> propertyNames)
	{
		if (propertyNames == null)
			return new HashSet<CriteriaAlias>(0);
		
		Collection<CriteriaAlias> col = new HashSet<CriteriaAlias>(propertyNames.size());
		
		Iterator<String> it = propertyNames.iterator();
		while (it.hasNext())
		{
			col.addAll(createAliases(it.next()));
		}
		
		return col;
	}	
	
	/**
	 * Returns criterion alias (Restriction) for property name.
	 * Example:  Property Name = a.b.c => b.c
	 * @param propertyName
	 * @return
	 */
	protected static String getCriterionAlias (String propertyName)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy property name parameter.");
		
		// Just return the property name if there is not sub-entity.  This means the propertyName
		// is an actual property value
		int pos = propertyName.lastIndexOf(DOT);
		if (pos < 0)
			return propertyName;
		
		String tmp = propertyName.substring(0, pos);
		
		int nextPos = tmp.lastIndexOf(DOT);
		if (nextPos < 0)
		{
			return propertyName; // example: a.b
		}
		else
		{
			return propertyName.substring(nextPos + 1);
		}
	}
	
	/**
	 * Updates the property name value map with proper Criterion aliases.
	 * 
	 * @param propertyNameValues
	 * @return
	 */
	protected static Map<String, Object> getCriterionAliases(Map<String, Object> propertyNameValues)
	{
		if (propertyNameValues == null)
			throw new IllegalArgumentException("Invalid null / empty property name value map.");
		
		Map<String, Object> newMap = new HashMap<String, Object>(propertyNameValues.size());
		
		Set<String> keySet = propertyNameValues.keySet();
		Iterator<String> it = keySet.iterator();
		String newKey = null;
		String oldKey = null;
		while (it.hasNext())
		{
			oldKey = it.next();

			// Update key to new alias
			newKey = getCriterionAlias(oldKey);			
			newMap.put(newKey, propertyNameValues.get(oldKey));
		}
		
		return newMap;
	}	
}
