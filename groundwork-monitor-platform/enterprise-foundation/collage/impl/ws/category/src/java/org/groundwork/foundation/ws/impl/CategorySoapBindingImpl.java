/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;

import org.groundwork.foundation.ws.api.WSCategory;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;

public class CategorySoapBindingImpl implements WSCategory
{
	private static final String BEAN_WSCategory = "WSCategory";
	
    /* (non-Javadoc)
     * @see org.groundwork.foundation.ws.api.WSCategory#getCategorys(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getRootCategories(
    		String entityTypeName, 
    		int startRange,
    		int endRange,
    		SortCriteria orderBy,
    		boolean retrieveChildren,
    		boolean namePropertyOnly)
    throws WSFoundationException, RemoteException 
    {
        // get the WSCategory api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCategory Category = (WSCategory) factory.getAPIObject(BEAN_WSCategory);
        
        // check the Category object, if getting it failed, bail out now.
        if (Category == null) {
            throw new WSFoundationException("Unable to create WSCategory instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return Category.getRootCategories(entityTypeName, startRange, endRange, orderBy, retrieveChildren, namePropertyOnly);
        }        
    }

    public WSFoundationCollection getCategoryEntities(
    		String categoryName, 
    		String entityTypeName, 
    		int startRange, 
    		int endRange, 
    		SortCriteria orderBy,
    		boolean retrieveChildren,
    		boolean namePropertyOnly) 
    throws java.rmi.RemoteException, WSFoundationException 
    {
        // get the WSCategory api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCategory Category = (WSCategory) factory.getAPIObject(BEAN_WSCategory);
        
        // check the Category object, if getting it failed, bail out now.
        if (Category == null) {
            throw new WSFoundationException("Unable to create WSCategory instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return Category.getCategoryEntities(categoryName, entityTypeName, startRange, endRange, orderBy, retrieveChildren, namePropertyOnly);
        }
    }
    
    public WSFoundationCollection getCategoryByName(java.lang.String categoryName, String entityTypeName) throws java.rmi.RemoteException, WSFoundationException {
        // get the WSCategory api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCategory Category = (WSCategory) factory.getAPIObject(BEAN_WSCategory);
        
        // check the Category object, if getting it failed, bail out now.
        if (Category == null) {
            throw new WSFoundationException("Unable to create WSCategory instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return Category.getCategoryByName(categoryName, entityTypeName);
        }
    }
    
    public WSFoundationCollection getCategories(Filter filter, int startRange, int endRange, SortCriteria orderBy,
    		boolean retrieveChildren,
    		boolean namePropertyOnly) throws WSFoundationException, RemoteException
    {
        // get the WSCategory api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCategory Category = (WSCategory) factory.getAPIObject(BEAN_WSCategory);
        
        // check the Category object, if getting it failed, bail out now.
        if (Category == null) {
            throw new WSFoundationException("Unable to create WSCategory instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return Category.getCategories(filter, startRange, endRange, orderBy, retrieveChildren, namePropertyOnly);
        }
    }
    
    public WSFoundationCollection getCategoriesByEntityType(String entityTypeName, int startRange, 
    		int endRange, 
    		SortCriteria orderBy,boolean retrieveChildren,	boolean namePropertyOnly) throws WSFoundationException, RemoteException
    {
        // get the WSCategory api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCategory Category = (WSCategory) factory.getAPIObject(BEAN_WSCategory);
        
        // check the Category object, if getting it failed, bail out now.
        if (Category == null) {
            throw new WSFoundationException("Unable to create WSCategory instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return Category.getCategoriesByEntityType(entityTypeName, startRange,endRange, orderBy, retrieveChildren,namePropertyOnly);
        }
    }
    
    public WSFoundationCollection getCategoryById(int categoryId)  throws RemoteException, WSFoundationException
    {
		// get the WSHostGroup api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSCategory Category = (WSCategory) factory.getAPIObject(BEAN_WSCategory);
		
		// check the Category object, if getting it failed, bail out now.
		if (Category == null) {
		    throw new WSFoundationException("Unable to create WSCategory instance", ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
		   return Category.getCategoryById(categoryId);
		}
    }    
    
    
}
