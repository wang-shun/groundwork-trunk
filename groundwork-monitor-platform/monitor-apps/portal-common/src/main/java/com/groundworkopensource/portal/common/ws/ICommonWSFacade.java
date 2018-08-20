package com.groundworkopensource.portal.common.ws;

import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * Interface defining methods for "Common" web service
 * 
 * @author nitin_jadhav
 * 
 */
public interface ICommonWSFacade {

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
    WSFoundationCollection searchEntity(String searchQuery,
            int resultsQuantity, String extRoleServiceGroupList,
            String extRoleHostGroupList) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * This method perform the action According to action perform array. for
     * Example:- close log message.
     * 
     * @param actionPerforms
     * @return WSFoundationCollection
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    WSFoundationCollection performActions(ActionPerform[] actionPerforms)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Gets actions by application type
     * 
     * @param appType
     * @param child
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    WSFoundationCollection getActionsByApplicationType(String appType,
            boolean child) throws WSDataUnavailableException, GWPortalException;

    /**
     * @param entityType
     * @param appType
     * @param child
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    WSFoundationCollection getEntityTypeProperties(String entityType,
            String appType, boolean child) throws WSDataUnavailableException,
            GWPortalException;

}
