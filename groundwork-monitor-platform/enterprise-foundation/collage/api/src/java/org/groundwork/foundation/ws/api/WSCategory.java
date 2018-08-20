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

/*Created on: Oct 3, 2008 */
package org.groundwork.foundation.ws.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public interface WSCategory extends Remote {
	
	/**
	 * Gets all the root categories for the supplied range and entityTypeName
	 * @param entityTypeName - Entity Name
	 * @param startRange
	 * @param endRange
	 * @param orderBy
	 * @param retrieveChildren - Boolean to indicate deep level retrieval or coarse level retrieval required.
	 * @param namePropertyOnly - Indicates where name only property needs to be retrieved.If set to true all other properties will be null besides name.
	 * @return
	 */
	public WSFoundationCollection getRootCategories(String entityTypeName,int startRange, int endRange,SortCriteria orderBy, boolean retrieveChildren, boolean namePropertyOnly) throws WSFoundationException, RemoteException;

	
	/**
	 * Gets category entities for the supplied category, range and entityTypeName
	 * @param categoryName - Name of the category
	 * @param entityTypeName - Entity Name
	 * @param startRange
	 * @param endRange
	 * @param orderBy
	 * @param retrieveChildren - Boolean to indicate deep level retrieval or coarse level retrieval required.
	 * @param namePropertyOnly - Indicates where name only property needs to be retrieved.If set to true all other properties will be null besides name.
	 * @return
	 */
	public WSFoundationCollection getCategoryEntities(String categoryName, String entityTypeName,int startRange, int endRange,SortCriteria orderBy, boolean retrieveChildren, boolean namePropertyOnly)throws WSFoundationException, RemoteException;

	
	/**
	 * Gets category for the supplied name and entityTypeName
	 * @param categoryName - Name of the category
	 * @param entityTypeName - Entity Name
	 * @return
	 */
	public WSFoundationCollection getCategoryByName(String categoryName, String entityTypeName)throws WSFoundationException, RemoteException;

	
	/**
	 * Gets categories for the supplied filter, range and entityTypeName
	 * @param filter - Filter criteria
	 * @param startRange
	 * @param endRange
	 * @param orderBy
	 * @param retrieveChildren - Boolean to indicate deep level retrieval or coarse level retrieval required.
	 * @param namePropertyOnly - Indicates where name only property needs to be retrieved.If set to true all other properties will be null besides name.
	 * @return
	 */
	public WSFoundationCollection getCategories(Filter filter,int startRange, int endRange,SortCriteria orderBy, boolean retrieveChildren, boolean namePropertyOnly)throws WSFoundationException, RemoteException;
	
	/**
	 * Gets categories for the supplied range and entityTypeName
	 * @param entityTypeName - Entity Name
	 * @param startRange
	 * @param endRange
	 * @param orderBy
	 * @param retrieveChildren - Boolean to indicate deep level retrieval or coarse level retrieval required.
	 * @param namePropertyOnly - Indicates where name only property needs to be retrieved.If set to true all other properties will be null besides name.
	 * @return
	 */
	public WSFoundationCollection getCategoriesByEntityType(String entityTypeName,int startRange, int endRange,SortCriteria orderBy, boolean retrieveChildren, boolean namePropertyOnly)throws WSFoundationException, RemoteException;
	
	
	/**
	 * Gets category for the supplied id.
	 * @param catergoryId
	 * @return
	 */
	public WSFoundationCollection getCategoryById(int catergoryId)throws WSFoundationException, RemoteException;
}
