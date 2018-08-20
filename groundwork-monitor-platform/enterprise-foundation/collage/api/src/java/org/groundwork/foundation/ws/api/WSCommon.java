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

package org.groundwork.foundation.ws.api;

import java.rmi.RemoteException;

import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public interface WSCommon extends java.rmi.Remote {
    /**
     * 
     * @param userName
     * @param password
     * @param realUserName
     * @return
     */
    public String login(String userName, String password, String realUserName)
            throws WSFoundationException, RemoteException;

    public void logout() throws WSFoundationException, RemoteException;

    /**
     * Returns collection of attribute data for the specified attribute type.
     * For example, this method returns all application types, monitor statuses,
     * check types, etc.
     * 
     * @param type
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getAttributeData(AttributeQueryType type)
            throws WSFoundationException, RemoteException;

    /**
     * String parameter version of getAttributeData();
     * 
     * @param type
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getAttributeDataByString(String type)
            throws WSFoundationException, RemoteException;

    /**
     * Cancels a running or prepared query identified by the session ID obtained
     * by calling into any prepare methods.
     * 
     * @param sessionID
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public String cancelQuery(int sessionID) throws WSFoundationException,
            RemoteException;

    /**
     * Starts execution of the query identified by the session ID
     * 
     * @param sessionID
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection executeQuery(int sessionID)
            throws WSFoundationException, RemoteException;

    /**
     * Returns collection of registered entity types
     * 
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEntityTypes()
            throws WSFoundationException, RemoteException;

    /**
     * Returns collection of EntityTypeProperty instances representing the
     * properties for the specified entity.
     * 
     * @param entityType
     * @param appType
     * @param bComponentProperties
     *            - Boolean indicating whether to return
     *            "filterable / hibernate" properties or properties that are
     *            returned in a performEntityQuery()
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEntityTypeProperties(String entityType,
            String appType, boolean bComponentProperties)
            throws WSFoundationException, RemoteException;

    /**
     * Returns collection of PropertyTypeBinding instances which represent the
     * entity information being queried.
     * 
     * @param entityType
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection performEntityQuery(String entityType,
            Filter filter, Sort sort, int firstResult, int maxResults)
            throws WSFoundationException, RemoteException;

    /**
     * Returns scalar value representing the number of entities which match the
     * specified filter.
     * 
     * @param entityType
     * @param filter
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public int performEntityCountQuery(String entityType, Filter filter)
            throws WSFoundationException, RemoteException;

    /**
     * Returns all actions related to the specified application type
     * 
     * @param appType
     * @param includeSystem
     *            boolean indicating whether to include system action or not
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getActionsByApplicationType(String appType,
            boolean includeSystem) throws WSFoundationException,
            RemoteException;

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
            WSFoundationException;

    /**
     * Performs specified actions and returns the status for each in the
     * WSFoundationCollection.
     * 
     * @param actionPerforms
     *            WSFoundationCollection containing array of ActionPerform
     *            instances to perform
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection performActions(ActionPerform[] actionPerforms)
            throws RemoteException, WSFoundationException;

    /**
     * Searchs HostGroups, Hosts, ServiceGroups and Services filter by comma
     * separated service group and host group list String and returns the
     * results.
     * 
     * If comma separated service group and host group list String is empty then
     * return All HostGroups, Hosts, ServiceGroups and Services as per search
     * text.
     * 
     * If comma separated service group contains exclude keyword then search
     * only depends on host group list and vice versa.
     * 
     * Null extended role service group or host group list string is invalid.
     * 
     * @param text
     *            search text
     * @param maxresults
     * @param extRoleServiceGroupList
     * @param extRoleHostGroupList
     * @return WSFoundationCollection
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection searchEntity(String text, int maxresults,
            String extRoleServiceGroupList, String extRoleHostGroupList)
            throws RemoteException, WSFoundationException;

}
