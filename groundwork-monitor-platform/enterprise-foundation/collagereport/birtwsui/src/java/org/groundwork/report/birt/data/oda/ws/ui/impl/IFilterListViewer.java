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

import org.groundwork.report.birt.data.oda.ws.impl.EntityFilter;

public interface IFilterListViewer {
	
	/**
	 * Update the view to reflect the fact that a filter was added 
	 * to the filter list
	 * 
	 * @param filter
	 */
	public void addFilter(EntityFilter filter);
	
	/**
	 * Update the view to reflect the fact that a filter was removed 
	 * from the filter list
	 * 
	 * @param filter
	 */
	public void removeFilter(EntityFilter filter);
	
	/**
	 * Update the view to reflect the fact that one of the filters
	 * was modified 
	 * 
	 * @param filter
	 */
	public void updateFilter(EntityFilter filter);
	
	public void clearFilters();
}
