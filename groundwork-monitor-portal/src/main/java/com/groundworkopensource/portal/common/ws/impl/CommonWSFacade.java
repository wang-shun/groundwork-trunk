package com.groundworkopensource.portal.common.ws.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.ICommonWSFacade;

/**
 * Facade defining methods for "Common" web service
 * 
 * @author nitin_jadhav
 * 
 */
public class CommonWSFacade implements ICommonWSFacade {

    /**
     * Logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * Returns Binding object for "common" web service
     * 
     * @return WSCommon Binding
     * @throws GWPortalException
     */
    private WSCommon getCommonBinding() throws GWPortalException {
        // get the host binding object
        try {
            WSCommon commonBinding = WebServiceLocator.getInstance()
                    .commonServiceLocator().getcommon();
            if (null != commonBinding) {
                return commonBinding;
            }
        } catch (ServiceException sEx) {
            LOGGER
                    .fatal("ServiceException while getting binding object for \"common\" web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + sEx);
        }
        throw new GWPortalException();
    }

    /**
     * Returns the list of searched results: containing Host, Host groups,
     * Services etc by calling foundation web service API.
     * 
     * @param searchQuery
     * @param resultsQuantity
     * @param extRoleServiceGroupList
     * @param extRoleHostGroupList
     * @return WSFoundationCollection of searched results.
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection searchEntity(String searchQuery,
            int resultsQuantity, String extRoleServiceGroupList,
            String extRoleHostGroupList) throws WSDataUnavailableException,
            GWPortalException {
        WSFoundationCollection resultCollection = null;
        WSCommon commonBinding = getCommonBinding();

        try {
            LOGGER.debug("Calling searchEntity for search query ["
                    + searchQuery + "]");
            resultCollection = commonBinding.searchEntity(searchQuery,
                    resultsQuantity, extRoleServiceGroupList,
                    extRoleHostGroupList);
            if (resultCollection == null) {
                LOGGER
                        .info("Found null resultCollection when calling searchEntity() for searchString : "
                                + searchQuery);
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while retrieving search results."
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"search (common)\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return resultCollection;

    }

    /**
     * This method perform the action According to action perform array. for
     * Example:- close the log message.
     * 
     * @param actionPerforms
     * @return WSFoundationCollection
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public WSFoundationCollection performActions(ActionPerform[] actionPerforms)
            throws GWPortalException, WSDataUnavailableException {
        WSFoundationCollection wsfoundationCollection = null;
        try {
            // LOGGER.debug("calling performActions() with action params");
            WSCommon commonBinding = getCommonBinding();
            wsfoundationCollection = commonBinding
                    .performActions(actionPerforms);
            if (wsfoundationCollection == null) {
                LOGGER
                        .info("wsfoundationCollection is null while calling performActions().");
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error("WSFoundationException while calling performActions."
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"performActions (common)\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

        return wsfoundationCollection;

    }

    /**
     * Gets actions by application type
     * 
     * @param appType
     * @param child
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection getActionsByApplicationType(String appType,
            boolean child) throws WSDataUnavailableException, GWPortalException {
        WSFoundationCollection wsfoundationCollection = null;
        try {
            LOGGER.debug("Calling getActionsByApplicationType for apptype="
                    + appType);
            WSCommon commonBinding = getCommonBinding();
            wsfoundationCollection = commonBinding.getActionsByApplicationType(
                    appType, child);
            if (wsfoundationCollection == null) {
                LOGGER
                        .info("WSFoundationException is null while calling getActionsByApplicationType.");
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while calling getActionsByApplicationType."
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"getActionsByApplicationType (common)\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

        return wsfoundationCollection;

    }

    /**
     * Gets actions by application type
     * 
     * @param entityType
     * 
     * @param appType
     * @param child
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection getEntityTypeProperties(String entityType,
            String appType, boolean child) throws WSDataUnavailableException,
            GWPortalException {
        WSFoundationCollection wsfoundationCollection = null;
        try {
            LOGGER.debug("Calling getEntityTypeProperties for apptype="
                    + appType);
            WSCommon commonBinding = getCommonBinding();
            if (commonBinding == null) {
                LOGGER
                        .info("commonBinding is null while calling getEntityTypeProperties.");
                throw new GWPortalException();
            }
            wsfoundationCollection = commonBinding.getEntityTypeProperties(
                    entityType, appType, child);
            if (wsfoundationCollection == null) {
                LOGGER
                        .info("WSFoundationException is null while calling getEntityTypeProperties.");
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while calling getEntityTypeProperties."
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while contacting \"getEntityTypeProperties (common)\" foundation web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

        return wsfoundationCollection;

    }
}
