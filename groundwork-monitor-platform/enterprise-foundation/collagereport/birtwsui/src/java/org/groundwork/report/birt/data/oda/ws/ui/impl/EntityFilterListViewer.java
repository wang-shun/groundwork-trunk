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
package org.groundwork.report.birt.data.oda.ws.ui.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.report.birt.data.oda.ws.impl.EntityFilter;
import org.groundwork.report.birt.data.oda.ws.impl.EntityFilterList;

/**
 * Class that plays the role of the domain model in the TableViewerExample
 * In real life, this class would access a persistent store of some kind.
 * 
 */

public class EntityFilterListViewer extends EntityFilterList
{
	private Set<IFilterListViewer> changeListeners = new HashSet<IFilterListViewer>();

	/**
	 * Default Constructor
	 */
	public EntityFilterListViewer() 
	{
		super();
	}
	
	/**
	 * Constructor
	 */
	public EntityFilterListViewer(EntityTypeProperty[] props, String filterList) throws OdaException
	{
		super(props, filterList);				
	}
	
	public void clear ()
	{
		super.clear();
		
		Iterator<IFilterListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IFilterListViewer) iterator.next()).clearFilters();		
	}
	
	/**
	 * Add a new filter to the collection of filters
	 */
	public EntityFilter addFilter(EntityTypeProperty property, FilterOperator operator, String value, FilterOperator logicalOperator) 
	{
		EntityFilter filter = super.addFilter(property, operator, value, logicalOperator);
		
		Iterator<IFilterListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IFilterListViewer) iterator.next()).addFilter(filter);
		
		return filter;
	}

	/**
	 * @param filter
	 */
	public void removeFilter(EntityFilter filter) 
	{
		super.removeFilter(filter);
	
		Iterator<IFilterListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IFilterListViewer) iterator.next()).removeFilter(filter);
	}

	/**
	 * @param filter
	 */
	public void filterChanged(EntityFilter filter) 
	{
		Iterator<IFilterListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IFilterListViewer) iterator.next()).updateFilter(filter);
	}

	/**
	 * @param viewer
	 */
	public void removeChangeListener(IFilterListViewer viewer) 
	{
		changeListeners.remove(viewer);
	}

	/**
	 * @param viewer
	 */
	public void addChangeListener(IFilterListViewer viewer) 
	{
		if (changeListeners.contains(viewer) == false)
			changeListeners.add(viewer);
	}
}
