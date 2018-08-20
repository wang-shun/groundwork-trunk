package com.groundworkopensource.portal.common.ws;

import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * Interface defining methods for "Category (Service group)" web service
 * 
 * @author nitin_jadhav
 * 
 */
public interface ICategoryWSFacade {

    /**
     * Returns the list of host-groups by calling foundation web service API.
     * 
     * @return the list of all host-groups
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Category[] getAllServiceGroups() throws WSDataUnavailableException,
            GWPortalException;

    /**
     * returns the category array depending on filter applied.
     * 
     * @param filter
     * @param start
     * @param end
     * @param sortCriteria
     * @param retrieveChildren
     * @param namePropertyOnly
     * @return Category
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Category[] getCategory(Filter filter, int start, int end,
            SortCriteria sortCriteria, boolean retrieveChildren,
            boolean namePropertyOnly) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns CategoryEntity[] when passed a ServiceGroup name.
     * 
     * @param serviceGroupName
     * @return CategoryEntity[]
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.impl.CategoryWSFacade
     */
    CategoryEntity[] getCategoryEntities(String serviceGroupName)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns Category object by its Id
     * 
     * @param categoryId
     * @return Category object by its Id
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Category getCategoryByID(int categoryId) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns Category object by its NAme
     * 
     * @param categoryName
     * @return Category object by its Name
     * @throws WSDataUnavailableException
     */
    Category getCategoryByName(String categoryName)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns WSFoundationCollection object by CategoryCriteria
     * 
     * @param filter
     * @param start
     * @param end
     * @param sortCriteria
     * @param retrieveChildren
     * @param namePropertyOnly
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection getCategoryCollectionbyCriteria(
            Filter filter, int start, int end, SortCriteria sortCriteria,
            boolean retrieveChildren, boolean namePropertyOnly)
            throws WSDataUnavailableException, GWPortalException;

}
