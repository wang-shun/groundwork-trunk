package com.groundworkopensource.portal.common.ws.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCategory;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.ICategoryWSFacade;

/**
 * This class provides methods to interact with "Category" foundation web
 * service.
 * 
 * @author nitin_jadhav
 * 
 */

public class CategoryWSFacade implements ICategoryWSFacade {

    /**
     * String SERVICE_GROUP
     */
    private static final String SERVICE_GROUP = "SERVICE_GROUP";

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * Returns Binding object for "category" web service
     * 
     * @return WSCategory Binding
     * 
     * 
     *         FIXME throw GWPortalException if null binding
     * @throws GWPortalException
     */
    private WSCategory getCategoryBinding() throws GWPortalException {
        // get the category binding object
        try {
            WSCategory categoryBinding = WebServiceLocator.getInstance()
                    .serviceGroupLocator().getwscategory();
            if (null != categoryBinding) {
                return categoryBinding;
            }
        } catch (ServiceException sEx) {
            LOGGER
                    .fatal("ServiceException while getting binding object for \"Category\" web service."
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + sEx);
        }
        throw new GWPortalException();
    }

    /**
     * returns all available service groups (category).
     * 
     * @return Category
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */

    public Category[] getAllServiceGroups() throws WSDataUnavailableException,
            GWPortalException {
        Category[] serviceGroupArray = new Category[] {};
        WSCategory categoryBinding = getCategoryBinding();
        if (null == categoryBinding) {
            LOGGER
                    .info("getAllServiceGroups() : NULL categoryBinding, returning empty result");
            return serviceGroupArray;
        }
        try {
            // LOGGER.debug("Getting all service groups.");
            // get list of Service Groups
            WSFoundationCollection serviceGroups = categoryBinding
                    .getRootCategories(SERVICE_GROUP, -1, -1, null, true, false);
            /* Check for null pointer occurrence */
            if (serviceGroups == null) {
                LOGGER
                        .info("No service groups found when calling getAllServiceGroups().");
                throw new WSDataUnavailableException();
            }
            serviceGroupArray = serviceGroups.getCategory();
            /* Check for null pointer occurrence */
            // if (serviceGroupArray == null) {
            // LOGGER
            // .warn("getAllServiceGroups(): got service group length 0");
            // } else {
            // LOGGER.debug("getAllServiceGroups(): got service group length "
            // + serviceGroupArray.length);
            // }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting servicegroups data"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"service group\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return serviceGroupArray;
    }

    /**
     * Returns CategoryEntity[] when passed a ServiceGroup name. (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICategoryWSFacade#getCategoryEntities(java.lang.String)
     */
    public CategoryEntity[] getCategoryEntities(String serviceGroupName)
            throws WSDataUnavailableException, GWPortalException {
        CategoryEntity[] categoryEntityArray = new CategoryEntity[] {};
        WSCategory categoryBinding = getCategoryBinding();
        if (null == categoryBinding) {
            LOGGER
                    .info("getCategoryEntities() : NULL categoryBinding, returning empty result");
            return categoryEntityArray;
        }
        try {
            // LOGGER.debug("get list of Category Entities.");
            // get list of Category Entities
            WSFoundationCollection collection = categoryBinding
                    .getCategoryEntities(serviceGroupName, SERVICE_GROUP, -1,
                            -1, null, true, false);
            if (collection == null) {
                LOGGER
                        .info("Found null wsFoundationCollection when calling getCategoryEntities() for serviceGroup : "
                                + serviceGroupName);
                throw new WSDataUnavailableException();
            }
            categoryEntityArray = collection.getCategoryEntity();
            // if (categoryEntityArray == null) {
            // LOGGER
            // .error(
            // "Found null categoryEntityArray inside getCategoryEntities()");
            // } else {
            // LOGGER.debug("getAllServiceGroups(): got service group = "
            // + categoryEntityArray.length);
            // }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting CategoryEntity data"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"service Group / CategoryEntity\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return categoryEntityArray;
    }

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
    public Category[] getCategory(Filter filter, int start, int end,
            SortCriteria sortCriteria, boolean retrieveChildren,
            boolean namePropertyOnly) throws WSDataUnavailableException,
            GWPortalException {
        Category[] categoryArr = new Category[] {};
        WSCategory categoryBinding = getCategoryBinding();
        if (null == categoryBinding) {
            LOGGER
                    .info("getCategory() : NULL categoryBinding, returning empty result");
            return categoryArr;
        }
        try {
            // LOGGER.debug("getCategory using filter= " + filter != null ?
            // filter
            // .toString() : "" + " sortcriterian = " + sortCriteria);
            WSFoundationCollection foundationCollection = categoryBinding
                    .getCategories(filter, start, end, sortCriteria,
                            retrieveChildren, namePropertyOnly);
            if (foundationCollection == null) {
                LOGGER
                        .info("Found null foundationCollection when calling getCategory()");
                throw new WSDataUnavailableException();
            }
            categoryArr = foundationCollection.getCategory();
            // if (null == categoryArr) {
            // LOGGER
            // .debug("Found null categoryArr inside getCategory() method");
            // } else {
            // LOGGER.debug("Found total number of categories = "
            // + categoryArr.length);
            // }

        } catch (WSFoundationException fEx) {
            LOGGER.error("WSFoundationException while getting Category data"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"Category\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return categoryArr;
    }

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
    public WSFoundationCollection getCategoryCollectionbyCriteria(
            Filter filter, int start, int end, SortCriteria sortCriteria,
            boolean retrieveChildren, boolean namePropertyOnly)
            throws WSDataUnavailableException, GWPortalException {

        WSCategory categoryBinding = getCategoryBinding();
        WSFoundationCollection foundationCollection = null;
        if (null == categoryBinding) {
            LOGGER
                    .info("getCategory() : NULL categoryBinding, returning empty result");
            throw new WSDataUnavailableException();
        }
        try {
            // LOGGER.debug("getCategory using filter= " + filter != null ?
            // filter
            // .toString() : "" + " sortcriterian = " + sortCriteria);
            foundationCollection = categoryBinding.getCategories(filter, start,
                    end, sortCriteria, retrieveChildren, namePropertyOnly);
            if (foundationCollection == null) {
                LOGGER
                        .info("Found null foundationCollection when calling getCategory()");
                throw new WSDataUnavailableException();
            }

        } catch (WSFoundationException fEx) {
            LOGGER.error("WSFoundationException while getting Category data"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"Category\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return foundationCollection;
    }

    /**
     * Returns Category object by its Id, throws exception if services not
     * available<br>
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICategoryWSFacade#getCategoryByID(int)
     */
    public Category getCategoryByID(int categoryId)
            throws WSDataUnavailableException, GWPortalException {
        WSCategory categoryBinding = getCategoryBinding();
        Category[] category = null;
        try {
            // LOGGER.debug("getCategoryByID for id=" + categoryId);
            WSFoundationCollection collection = categoryBinding
                    .getCategoryById(categoryId);

            if (null == collection) {
                LOGGER
                        .info("Found null collection when calling getCategoryByID() for categroryID = "
                                + categoryId);
                throw new WSDataUnavailableException();
            }
            category = collection.getCategory();
            if (null == category || null == category[0]) {
                LOGGER
                        .info("Found null category when calling getCategoryByID() for categroryID ="
                                + categoryId);
                throw new WSDataUnavailableException();
            }
            // LOGGER.debug("returning category for id " + categoryId);
        } catch (WSFoundationException fEx) {
            LOGGER.error("WSFoundationException while getting Category data"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"Category\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return category[0];
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICategoryWSFacade#getCategoryByName(java.lang.String)
     */
    public Category getCategoryByName(String categoryName)
            throws WSDataUnavailableException, GWPortalException {
        WSCategory categoryBinding = getCategoryBinding();
        if (categoryBinding == null) {
            LOGGER.info(" Category binding null , throwing exception WSD");
            throw new GWPortalException();
        }
        try {
            WSFoundationCollection collection = categoryBinding
                    .getCategoryByName(categoryName, SERVICE_GROUP);

            if (null != collection) {
                Category[] category = collection.getCategory();
                if (category != null) {
                    LOGGER.debug("got category in getCategoryByName()");
                    return category[0];
                }
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error("WSFoundationException while getting Category data"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"Category\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }

        // exception occurred or data not found.
        throw new WSDataUnavailableException(
                "Error occured: Web services data is not available.");
    }
}
