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

package com.groundworkopensource.portal.statusviewer.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.common.EditPrefsBean;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * This portlet display the service statistics and pie chart.
 * 
 * @author manish_kjain
 * 
 */
public class ServiceStatisticsPortlet extends BasePortlet {

    /**
     * SERVICE_STATUS_PORTLET_TITLE
     */
    private static final String SERVICE_STATUS_PORTLET_TITLE = "Service Status";

    /**
     * List of edit preferences. -> to be used in doEdit()
     */
    private static List<EditPrefsBean> editPrefs = new ArrayList<EditPrefsBean>();

    // static block for initializing editPrefs list
    static {
        // Host Preference - for generating auto complete list
        editPrefs.add(new EditPrefsBean(Constant.DEFAULT_HOST_PREF,
                Constant.DEFAULT_HOST_NAME, Constant.NODE_NAME_PREF, false,
                true));

        // Service Group Preference - for generating auto complete list
        editPrefs.add(new EditPrefsBean(Constant.DEFAULT_SERVICEGROUP_PREF,
                Constant.DEFAULT_SERVICE_GROUP_NAME, Constant.NODE_NAME_PREF,
                false, true));

        // Host Group Preference - for generating auto complete list
        editPrefs.add(new EditPrefsBean(Constant.DEFAULT_HOSTGROUP_PREF,
                Constant.HOSTGROUP_NAME_LINUX, Constant.NODE_NAME_PREF, false,
                true));

        // Node Name Preference
        editPrefs.add(new EditPrefsBean(Constant.NODE_NAME_PREF,
                Constant.DEFAULT_HOST_NAME, Constant.NODE_NAME_PREF, true,
                false));

        // Node Type Preference
        editPrefs.add(new EditPrefsBean(Constant.NODE_TYPE_PREF,
                NodeType.NETWORK.getTypeName(), Constant.NODE_TYPE_PREF, true,
                false));

        // Custom Portlet Title preference
        editPrefs.add(new EditPrefsBean(
                PreferenceConstants.CUSTOM_PORTLET_TITLE,
                Constant.EMPTY_STRING,
                PreferenceConstants.CUSTOM_PORTLET_TITLE, true, false));
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        // get preferences
        PortletPreferences pref = request.getPreferences();
        // get NodeName and NodeType parameters and set them in preferences.
        Object nodeTypePrefObj = request.getParameter(Constant.NODE_TYPE_PREF);
        Object nodeNamePrefObj = request.getParameter(Constant.NODE_NAME_PREF);

        // if (nodeTypePrefObj != null && nodeNamePrefObj != null) {
        String nodeTypeValue = (String) nodeTypePrefObj;
        String nodeNameValue = (String) nodeNamePrefObj;

        pref.setValue(Constant.NODE_TYPE_PREF, nodeTypeValue);
        if (null == nodeNameValue) {
            pref.setValue(Constant.NODE_NAME_PREF, Constant.EMPTY_STRING);
        } else {
            pref.setValue(Constant.NODE_NAME_PREF, nodeNameValue);
        }

        // }

        // Custom Portlet Title
        Object customPortletTitleObj = request
                .getParameter(PreferenceConstants.CUSTOM_PORTLET_TITLE);
        if (customPortletTitleObj != null) {
            pref.setValue(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                    (String) customPortletTitleObj);
        }

        // store the preferences
        pref.store();

        // set the portlet mode
        response.setPortletMode(PortletMode.VIEW);

    }

    /**
     * This method is Responsible to view service statistics portlet
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.setViewPath(Constant.SERVICE_VIEW_PATH);

        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                SERVICE_STATUS_PORTLET_TITLE));

        if (!PortletUtils.isInStatusViewer()) {
            // get preferences
            PortletPreferences pref = request.getPreferences();
            String customPortletTitle = pref.getValue(
                    PreferenceConstants.CUSTOM_PORTLET_TITLE,
                    Constant.EMPTY_STRING);
            String nodeTypePref = pref.getValue(Constant.NODE_TYPE_PREF,
                    Constant.EMPTY_STRING);
            if ((null == customPortletTitle
                    || Constant.EMPTY_STRING.equals(customPortletTitle)) && !nodeTypePref.equals(NodeType.NETWORK.getTypeName())) {
                
                String nodeNamePref = pref.getValue(Constant.NODE_NAME_PREF,
                        Constant.EMPTY_STRING);
                if (null != nodeTypePref
                        && ((nodeTypePref.equals(Constant.EMPTY_STRING) || nodeTypePref
                                .equals(NodeType.NETWORK.getTypeName())) || (nodeNamePref != null && nodeNamePref
                                .equals(Constant.EMPTY_STRING)))) {
                    // initialize UserExtendedRoleBean
                    UserExtendedRoleBean userExtendedRoleBean = new UserExtendedRoleBean(
                            PortletUtils.getExtendedRoleAttributes());

                    // get the extended role host group list
                    List<String> extRoleHostGroupList = userExtendedRoleBean
                            .getExtRoleHostGroupList();
                    // get the extended role service group list
                    List<String> extRoleServiceGroupList = userExtendedRoleBean
                            .getExtRoleServiceGroupList();
                    if (!extRoleHostGroupList.isEmpty()
                            && !extRoleHostGroupList
                                    .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                        response.setTitle(SERVICE_STATUS_PORTLET_TITLE
                                + Constant.SPACE_COLON_SPACE
                                + userExtendedRoleBean.getDefaultHostGroup());
                    } else if (!extRoleServiceGroupList.isEmpty()
                            && !extRoleServiceGroupList
                                    .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                        response
                                .setTitle(SERVICE_STATUS_PORTLET_TITLE
                                        + Constant.SPACE_COLON_SPACE
                                        + userExtendedRoleBean
                                                .getDefaultServiceGroup());
                    }
                }
            }
        }

        super.doView(request, response);
    }

    /**
     * This method is Responsible for editing preferences of service portlet
     * 
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit Service Summary Preferences");

        // get preferences
        PortletPreferences pref = request.getPreferences();

        String nodeTypePref = pref.getValue(Constant.NODE_TYPE_PREF,
                Constant.EMPTY_STRING);
        if (null != nodeTypePref
                && (nodeTypePref.equals(Constant.EMPTY_STRING) || !nodeTypePref
                        .equals(NodeType.NETWORK.getTypeName()))) {
            String nodeNamePrefValue = pref.getValue(Constant.NODE_NAME_PREF,
                    Constant.EMPTY_STRING);
            // initialize UserExtendedRoleBean
            UserExtendedRoleBean userExtendedRoleBean = PortletUtils
                    .getUserExtendedRoleBean();
            List<String> extRoleHostGroupList = userExtendedRoleBean
                    .getExtRoleHostGroupList();
            List<String> extRoleServiceGroupList = userExtendedRoleBean
                    .getExtRoleServiceGroupList();
            if (extRoleHostGroupList.isEmpty()
                    && extRoleServiceGroupList.isEmpty()) {
                updateEditPrefValue(Constant.NODE_TYPE_PREF, NodeType.NETWORK
                        .getTypeName());
                updateEditPrefValue(Constant.NODE_NAME_PREF,
                        Constant.EMPTY_STRING);

            } else if (!extRoleHostGroupList.isEmpty()
                    && !extRoleHostGroupList
                            .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
                    && !extRoleHostGroupList.contains(nodeNamePrefValue)) {
                updateEditPrefValue(Constant.NODE_TYPE_PREF,
                        NodeType.HOST_GROUP.getTypeName());
                updateEditPrefValue(Constant.NODE_NAME_PREF,
                        userExtendedRoleBean.getDefaultHostGroup());

            } else if (!extRoleServiceGroupList.isEmpty()
                    && !extRoleServiceGroupList
                            .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
                    && !extRoleServiceGroupList.contains(nodeNamePrefValue)) {
                updateEditPrefValue(Constant.NODE_TYPE_PREF,
                        NodeType.SERVICE_GROUP.getTypeName());
                updateEditPrefValue(Constant.NODE_NAME_PREF,
                        userExtendedRoleBean.getDefaultServiceGroup());
            }
        }

        // call doEditPref() of BasePortlet.
        super.doEditPref(request, response, editPrefs,
                Constant.SERVICEGROUPSTATISTICS_EDIT_PATH);
    }

    /**
     * Updates edit preferences bean
     * 
     * @param prefKey
     * @param prefValue
     */
    private void updateEditPrefValue(String prefKey, String prefValue) {
        for (EditPrefsBean editPrefsBean : editPrefs) {
            if (prefKey.equals(editPrefsBean.getPreferenceKey())) {
                editPrefsBean.setDefaultPreferenceValue(prefValue);
            }
        }
    }
}
