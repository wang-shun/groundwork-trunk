/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.statusviewer.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * @author swapnil_gujrathi
 * 
 */
public class SubpageIntegrator {

    /**
     * Node ID.
     */
    private int nodeID;

    /**
     * Type of network Object.
     */
    private NodeType nodeType;

    /**
     * Node name.
     */
    private String nodeName;

    /**
     * stateController instance variable
     */
    private StateController stateController = null;

    /**
     * This flag will be true if we definitely know that this portlet is in
     * Status Viewer. It will be true when we receive request parameters from
     * Interceptor (for Host, Service, Host Group and Service Group sub-pages).
     */
    private boolean inStatusViewer;

    // /**
    // * logger
    // */
    // private static final Logger LOGGER = Logger
    // .getLogger(SubpageIntegrator.class.getName());

    /**
     * UserExtendedRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Default Constructor
     */
    public SubpageIntegrator() {
        // Default Constructor
    }

    /**
     * Handles the subpage integration: Reads parameters from request in case of
     * Status Viewer. If portlet is in dashboard, reads preferences.
     * 
     * @param preferenceKeysMap
     * 
     * @return true if all goes well and got all needed parameters. Based on
     *         this parameter, each portlet will decide: if Portlet is not
     *         applicable for "Network View", show the error message to user -
     *         as Preferences not set. If it was in the "Network View", then we
     *         would have to assign Node Type as NETWORK with NodeId as 0.
     */
    public boolean doSubpageIntegration(Map<String, NodeType> preferenceKeysMap) {
        /*
         * First check if request parameters are specified by creating new
         * instance of StateController. If portlet is placed under
         * "Status Viewer" and in sub-pages apart from "Network View" then we
         * will (and have to) get request parameters from Interceptor.
         */
        inStatusViewer = true;
        stateController = new StateController();

        // check for dashboard - Read preferences
        if (null != preferenceKeysMap) {
            /*
             * If dashboard preferences are also not set, then each portlet
             * knows if that portlet is entitled for "Network View". If yes,
             * then show data as per the Network view.
             * 
             * Else show error message
             * "Preference values required for this portlet are unavailable. Please set required preferences."
             */
            if (!PortletUtils.isInStatusViewer()) {
                inStatusViewer = false;

                try {
                    // get all the preferences
                    PortletPreferences allPreferences = FacesUtils
                            .getAllPreferences();

                    // get preferences values
                    return getPreferenceValue(allPreferences, preferenceKeysMap);

                } catch (PreferencesException e) {
                    return false;
                }
            }
        }

        // check for navigation parameters (new portlet => Navigation Portlet
        // for replacing breadcrumbs)
        // boolean navigationParameters =
        setNavigationParameters();

        // inStatusViewer = navigationParameters;
        return inStatusViewer;
    }

    /**
     * @return true if navigationParams are set in the session
     */
    public boolean setNavigationParameters() {
        String navParams = (String) FacesUtils.getPortletSession(false)
                .getAttribute("navigationParams", PortletSession.PORTLET_SCOPE);
        // LOGGER.error("############ Navigation parameters : " + navParams);

        if (navParams == null) {
            return false;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(navParams,
                Constant.NODE_VIEW_DELIMITER);
        nodeType = NodeType.getNodeTypeByView(stringTokenizer.nextToken());
        nodeName = stringTokenizer.nextToken();
        nodeID = Integer.parseInt(stringTokenizer.nextToken());
        stateController.update(nodeType, nodeName, nodeID);

        return true;

    }

    /**
     * Retrieves the preference value from preferences based on key passed. If
     * found, assigns appropriate values to "selectedNodeName",
     * "selectedNodeType" and returns true. Else returns false.
     * 
     * @param allPreferences
     * @param preferenceKeysMap
     * @return true if preference value found.
     */
    private boolean getPreferenceValue(PortletPreferences allPreferences,
            Map<String, NodeType> preferenceKeysMap) {
        if (null != allPreferences) {
            Set<Entry<String, NodeType>> prefsEntrySet = preferenceKeysMap
                    .entrySet();
            for (Entry<String, NodeType> prefEntry : prefsEntrySet) {
                String prefEntryKey = prefEntry.getKey();
                String preferenceValue = allPreferences.getValue(prefEntryKey,
                        Constant.EMPTY_STRING);
                if (preferenceValue != null
                        && !preferenceValue.trim()
                                .equals(Constant.EMPTY_STRING)) {
                    // set node name and node type
                    nodeName = preferenceValue;
                    nodeType = prefEntry.getValue();
                    if (prefEntryKey
                            .equalsIgnoreCase(Constant.PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE)) {
                        // initialize UserExtendedRoleBean
                        userExtendedRoleBean = PortletUtils
                                .getUserExtendedRoleBean();
                        // get the extended role host group list
                        List<String> extRoleHostGroupList = userExtendedRoleBean
                                .getExtRoleHostGroupList();
                        if (extRoleHostGroupList.isEmpty()) {
                            /*
                             * if list is empty, it means user has unrestricted
                             * access.
                             */
                            // set to Entire NEtwork
                            nodeType = NodeType.NETWORK;
                            nodeName = Constant.EMPTY_STRING;
                            return true;
                        } else if (!extRoleHostGroupList.isEmpty()
                                && !extRoleHostGroupList
                                        .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
                                && !extRoleHostGroupList.contains(nodeName)) {
                            nodeName = userExtendedRoleBean
                                    .getDefaultHostGroup();
                            return true;
                        }
                    }
                    if (prefEntryKey
                            .equalsIgnoreCase(Constant.PORTLET_XML_DEFAULT_SERVICEGROUP_PREFERENCE)) {
                        // initialize UserExtendedRoleBean
                        userExtendedRoleBean = PortletUtils
                                .getUserExtendedRoleBean();
                        // get the extended role service group list
                        List<String> extRoleServiceGroupList = userExtendedRoleBean
                                .getExtRoleServiceGroupList();
                        if (extRoleServiceGroupList.isEmpty()) {
                            /*
                             * if list is empty, it means user has unrestricted
                             * access.
                             */
                            // set to Entire NEtwork
                            nodeType = NodeType.NETWORK;
                            nodeName = Constant.EMPTY_STRING;
                            return true;
                        } else if (!extRoleServiceGroupList.isEmpty()
                                && !extRoleServiceGroupList
                                        .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
                                && !extRoleServiceGroupList.contains(nodeName)) {
                            nodeName = userExtendedRoleBean
                                    .getDefaultServiceGroup();
                            return true;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the nodeID.
     * 
     * @return the nodeID
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * Sets the nodeID.
     * 
     * @param nodeID
     *            the nodeID to set
     */
    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * Returns the nodeType.
     * 
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Sets the nodeType.
     * 
     * @param nodeType
     *            the nodeType to set
     */
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Returns the nodeName.
     * 
     * @return the nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the nodeName.
     * 
     * @param nodeName
     *            the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Returns the stateController.
     * 
     * @return the stateController
     */
    public StateController getStateController() {
        return stateController;
    }

    /**
     * Sets the stateController.
     * 
     * @param stateController
     *            the stateController to set
     */
    public void setStateController(StateController stateController) {
        this.stateController = stateController;
    }

    /**
     * Sets the inStatusViewer.
     * 
     * @param inStatusViewer
     *            the inStatusViewer to set
     */
    public void setInStatusViewer(boolean inStatusViewer) {
        this.inStatusViewer = inStatusViewer;
    }

    /**
     * Returns the inStatusViewer.
     * 
     * @return the inStatusViewer
     */
    public boolean isInStatusViewer() {
        return inStatusViewer;
    }

}
