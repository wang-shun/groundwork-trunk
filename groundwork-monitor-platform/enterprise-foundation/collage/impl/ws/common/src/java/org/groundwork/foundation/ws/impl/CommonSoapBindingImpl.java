/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.LogMessage;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.QueryObjectWrapper;
import com.groundwork.collage.DataAccessObject.DAOConvertType;
import com.groundwork.collage.impl.AbstractDAO;

public class CommonSoapBindingImpl implements
        org.groundwork.foundation.ws.api.WSCommon {
    static final String DEFAULT_MSG = "Login attempt for user: ";
    static final String DEFAULT_ERROR = "Login failed; Reason: ";

    // Execute/Cancel Query messages

    static final String ERROR_QUERY_OBJECT_NOT_EXIST = "Query Object does not exist.";
    static final String ERROR_QUERY_NOT_RUNNING = "Query object is not running. Remove from list anyway.";
    static final String INFO_QUERY_CANCELED = "Running query has been canceled.";

    static final String ERROR_EXECUTING_QUERY = "System exception while executing the Query. Exception: ";

    /* Enable logging */
    protected static Log log = LogFactory.getLog(CommonSoapBindingImpl.class);

    public java.lang.String login(java.lang.String userName,
            java.lang.String password, java.lang.String realUserName)
            throws java.rmi.RemoteException {
        return DEFAULT_MSG + userName + DEFAULT_ERROR
                + "\nNot implemented yet!";
    }

    public void logout() throws java.rmi.RemoteException {
    }

    public WSFoundationCollection getAttributeData(AttributeQueryType type)
            throws WSFoundationException, RemoteException {
        // get the WSDevice api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.getAttributeData(type);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.groundwork.foundation.ws.api.WSCommon#getAttributeData(java.lang.
     * String)
     */
    public WSFoundationCollection getAttributeDataByString(String type)
            throws WSFoundationException, RemoteException {
        // get the WSDevice api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.getAttributeDataByString(type);
        }
    }

    /*
     * Utility methods to execute prepared queries or to cancel queries
     */
    public WSFoundationCollection executeQuery(int sessionID)
            throws WSFoundationException {
        // Get the Object from the factory
        CollageFactory factory = CollageFactory.getInstance();
        QueryObjectWrapper query = (QueryObjectWrapper) factory
                .getQuerySessionObjectByID(sessionID);

        // Error checking
        if (query == null)
            throw new WSFoundationException(ERROR_QUERY_OBJECT_NOT_EXIST,
                    ExceptionType.SYSTEM);

        if ((query.getStatus() == QueryObjectWrapper.QUERY_STATUS.running)
                || (query.getStatus() == QueryObjectWrapper.QUERY_STATUS.canceled)) {
            // query running or canceled -- no action
            return null;
        }

        // get ready to execute the query
        try {
            Collection resultSet = query.executeQuery();

            // Convert collection
            AbstractDAO dao = (AbstractDAO) factory.getAPIObject(query
                    .getDAOSpringID());

            // TBD convert to the correct array
            LogMessage[] results = (org.groundwork.foundation.ws.model.impl.LogMessage[]) dao
                    .convert(DAOConvertType.WebService, resultSet);
            if (results == null)
                return new org.groundwork.foundation.ws.model.impl.WSFoundationCollection(
                        0,
                        new org.groundwork.foundation.ws.model.impl.LogMessage[0]);

            return new org.groundwork.foundation.ws.model.impl.WSFoundationCollection(
                    results.length, results);

        } catch (Exception e) {
            // Remove from map
            factory.removeQuerySessionObject(sessionID);

            log.error(ERROR_EXECUTING_QUERY + e);
            throw new WSFoundationException(ERROR_EXECUTING_QUERY + e,
                    ExceptionType.SYSTEM);
        }
    }

    public String cancelQuery(int sessionID) {
        String returnStatus;
        // Method not supported
        return ERROR_QUERY_NOT_RUNNING;
        
    }

    public WSFoundationCollection getEntityTypeProperties(String entityType,
            String appType, boolean componentProperties)
            throws WSFoundationException, RemoteException {
        // get the WSDevice api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.getEntityTypeProperties(entityType, appType,
                    componentProperties);
        }
    }

    public WSFoundationCollection getEntityTypes()
            throws WSFoundationException, RemoteException {
        // get the WSDevice api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.getEntityTypes();
        }
    }

    public WSFoundationCollection performEntityQuery(String entityType,
            Filter filter, Sort sort, int firstResult, int maxResults)
            throws WSFoundationException, RemoteException {
        // get the WSDevice api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.performEntityQuery(entityType, filter, sort,
                    firstResult, maxResults);
        }
    }

    public int performEntityCountQuery(String entityType, Filter filter)
            throws WSFoundationException, RemoteException {
        // get the WSCommon api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.performEntityCountQuery(entityType, filter);
        }
    }

    /**
     * Returns all actions related to the specified application type
     * 
     * @param appType
     * @param includeSystem
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getActionsByApplicationType(String appType,
            boolean includeSystem) throws WSFoundationException,
            RemoteException {
        // get the WSCommon api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.getActionsByApplicationType(appType, includeSystem);
        }
    }

    /**
     * Returns all actions for the specified criteria and pagination parameters.
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getActionsByCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        // get the WSCommon api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.getActionsByCriteria(filter, sort, firstResult,
                    maxResults);
        }
    }

    /**
     * Performs specified actions and returns the status for each in the
     * WSFoundationCollection
     * 
     * @param actionPerforms
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection performActions(ActionPerform[] actionPerforms)
            throws RemoteException, WSFoundationException {
        // get the WSCommon api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.performActions(actionPerforms);
        }
    }

    /**
     * Performs search on Hostgroups, hosts, ServiceGroups and services
     * 
     * @param text
     * @param maxresults
     * @param extRoleServiceGroupList
     * @param extRoleHostGroupList
     * @return WSFoundationCollection
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection searchEntity(String text, int maxresults,
            String extRoleServiceGroupList, String extRoleHostGroupList)
            throws RemoteException, WSFoundationException {
        // get the WSCommon api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSCommon common = (WSCommon) factory.getAPIObject("WSCommon");

        // check the Common object, if getting it failed, bail out now.
        if (common == null) {
            throw new WSFoundationException(
                    "Unable to create WSCommon instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return common.searchEntity(text, maxresults,
                    extRoleServiceGroupList, extRoleHostGroupList);
        }
    }
}
