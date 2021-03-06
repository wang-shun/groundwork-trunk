/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.common;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;


import javax.ws.rs.core.MediaType;


import com.groundworkopensource.portal.common.ws.impl.BaseFacade;
import com.groundworkopensource.portal.model.*;
import org.apache.log4j.Logger;

import org.groundwork.rs.client.CollageRestException;
import org.groundwork.rs.client.UserNavigationTabClient;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

/**
 * Navigation Helper is database access class, that is used to retrieve and
 * store Navigation History objects to and from jboss portal database.
 *
 * @author nitin_jadhav
 * @version GWMON - 6.1.1
 */
public class NavigationHelper extends BaseFacade {

    /**
     * /** Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(NavigationHelper.class);


    public NavigationHelper() {
    }

    ;

    @SuppressWarnings("unchecked")
    public List<UserNavigation> getHistoryRecords(String userId, String app_type)
            throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        NavigationList naviList = client.getHistoryRecords(userId,app_type);
        Collection<UserNavigation> col = naviList.getList();
        List list = null;
        if (col == null)
            list = new ArrayList<UserNavigation>();
        else
            list = new ArrayList(col);
        return list;
    }

    /**
     * This method is used to add single record to Navigation History database.<br>
     * Note: record Id is not provided. Its auto generated by database.
     *
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param parentInfo
     * @param toolTip
     * @param app_type
     * @throws IOException
     */
    public void addHistoryRecord(String userId, int nodeId, String nodeName,
                                 String nodeType, String parentInfo, String toolTip, String app_type)
            throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.addHistoryRecord(userId, nodeId, nodeName, nodeType, parentInfo, toolTip, app_type);
    }

    /**
     * This method is used to add single record to Navigation History database.<br>
     * Note: record Id is not provided. Its auto generated by database.
     *
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param parentInfo
     * @param toolTip
     * @param app_type
     * @param nodeLabel
     * @throws IOException
     */
    public void addHistoryRecord(String userId, int nodeId, String nodeName,
                                 String nodeType, String parentInfo, String toolTip,
                                 String app_type, String nodeLabel) throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.addHistoryRecord(userId, nodeId, nodeName, nodeType, parentInfo, toolTip, app_type, nodeLabel);
    }

    /**
     * This method is used to delete a single record of a user identified by
     * "userId" from Navigation History database.
     *
     * @param userId
     * @param nodeId
     * @param nodeType
     * @param app_type
     * @throws IOException
     */
    public void deleteHistoryRecord(String userId, int nodeId, String nodeType,
                                    String app_type) throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.deleteHistoryRecord(userId, nodeId, nodeType, app_type);
    }

    /**
     * This method is used to delete all records of a user identified by
     * "userId" from Navigation History database.
     *
     * @param userId
     * @param app_type
     * @throws IOException
     */
    public void deleteAllHistoryRecords(String userId, String app_type)
            throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.deleteAllHistoryRecords(userId, app_type);
    }

    /**
     * Get Max node id
     *
     * @param userId
     * @param app_type
     * @throws IOException
     */
    public int getMaxNodeID(String userId, String app_type) throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.TEXT_PLAIN_TYPE);
        int nodeId = client.getMaxNodeID(userId, app_type);
        return nodeId;
    }

    /**
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param app_type
     * @return boolean
     * @throws IOException
     */
    public boolean updateHistoryRecord(String userId, int nodeId,
                                       String nodeName, String nodeType, String app_type)
            throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.updateHistoryRecord(userId, nodeId, nodeName, nodeType, app_type);
        return true;
    }

    /**
     * Update Node_Name and Node_type to Navigation History database.
     *
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param app_type
     * @param tabHistory
     * @param nodeLabel
     * @return boolean
     * @throws IOException
     */
    public boolean updateHistoryRecord(String userId, int nodeId,
                                       String nodeName, String nodeType, String app_type,
                                       String tabHistory, String nodeLabel) throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.updateHistoryRecord(userId, nodeId, nodeName, nodeType, app_type, tabHistory, nodeLabel);
        return true;
    }

    /**
     * Update tab history column
     *
     * @param userId
     * @param nodeId
     * @param app_type
     * @param tabHistory
     * @return boolean
     * @throws IOException
     */
    public boolean updateTabHistoryRecord(String userId, int nodeId,
                                          String app_type, String tabHistory) throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.updateTabHistoryRecord(userId, nodeId, app_type, tabHistory);
        return true;
    }

    /**
     * Update Node label column
     *
     * @param userId
     * @param nodeId
     * @param app_type
     * @param nodeLabel
     * @return boolean
     * @throws IOException
     */
    public boolean updateNodeLabelRecord(String userId, int nodeId,
                                         String app_type, String nodeLabel) throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.updateNodeLabelRecord(userId, nodeId, app_type, nodeLabel);
        return true;
    }

    /**
     * This method is used to delete a single record of a user identified by
     * "userId" from Navigation History database.
     *
     * @param userId
     * @param nodeId
     * @param app_type
     * @throws IOException
     */
    public void deleteHistoryRecord(String userId, int nodeId, String app_type)
            throws IOException {
        UserNavigationTabClient client = new UserNavigationTabClient(PORTAL_REST_ENDPOINT, MediaType.APPLICATION_XML_TYPE);
        client.deleteHistoryRecord(userId, nodeId, app_type);
    }
}
