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
package org.groundwork.report.birt.data.oda.ws.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.FilterOperator;

/**
 * Class that plays the role of the domain model in the TableViewerExample
 * In real life, this class would access a persistent store of some kind.
 * 
 */

public class EntityFilterList 
{
	private List<EntityFilter> filters = new ArrayList<EntityFilter>(5);

	// Available Operators
	protected static final String[] OPERATORS_ARRAY = 
		{ FilterOperator._EQ,
		  FilterOperator._GT,
		  FilterOperator._LT,
		  FilterOperator._GE,
		  FilterOperator._LE,
		  FilterOperator._LIKE 
		};	
	
	// Available Logical Operators
	protected static final String[] LOGICAL_OPERATORS_ARRAY = { FilterOperator._AND, FilterOperator._OR };

	/**
	 * Default Constructor
	 */
	public EntityFilterList() 
	{
		super();
	}
	
	/**
	 * Constructor
	 */
	public EntityFilterList(EntityTypeProperty[] props, String filterList) throws OdaException
	{
		super();
				
		this.initData(props, filterList);
	}	
	
	/*
	 * Initialize the table data.
	 */
	public void initData(EntityTypeProperty[] props, String filterList) throws OdaException
	{
		if (props == null || props.length == 0)
			return;
		
		if (filterList == null || filterList.length() == 0)
			return;
		
		// Clear list
		filters.clear();
		
		StringTokenizer tokenizer = new StringTokenizer(filterList, ";");
		while (tokenizer.hasMoreElements())
		{
			this.filters.add(new EntityFilter(props, tokenizer.nextToken()));			
		}		
	}

	/**
	 * Return the array of operators   
	 */
	public String[] getOperators() {
		return OPERATORS_ARRAY;
	}
	
	/**
	 * Return the array of logical operators   
	 */
	public String[] getLogicalOperators() {
		return LOGICAL_OPERATORS_ARRAY;
	}
	
	/**
	 * Return the collection of filters
	 */
	public List<EntityFilter> getFilters() {
		return filters;
	}
	
	public void clear ()
	{
		this.filters.clear();
	}
	
	/**
	 * Add a new filter to the collection of filters
	 */
	public EntityFilter addFilter(EntityTypeProperty property, FilterOperator operator, String value, FilterOperator logicalOperator) 
	{
		EntityFilter filter = new EntityFilter(property, operator, value, logicalOperator);
		
		filters.add(filter);
		
		return filter;
	}

	/**
	 * @param filter
	 */
	public void removeFilter(EntityFilter filter) 
	{
		filters.remove(filter);
	}
	
	public int size ()
	{
		if (filters == null)
			return 0;
		
		return filters.size();		
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		
		if (filters == null || filters.size() == 0)
			return sb.toString();
		
		EntityFilter filter = null;
		Iterator<EntityFilter> it = filters.iterator();
		while (it.hasNext())
		{
			filter = it.next();
						
			sb.append(filter.toString());
			sb.append(";");
		}
		return sb.toString();
	}
}
