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
 * This portlet display the service group statistics and pie chart.
 * 
 * @author manish_kjain
 * 
 */
public class EventPortlet extends BasePortlet {

    /**
     * List of edit preferences. -> to be used in doEdit()
     */
    private static List<EditPrefsBean> editPrefs = new ArrayList<EditPrefsBean>();

    // static block for initializing editPrefs list
    static {
        // Node Name Preference
        editPrefs.add(new EditPrefsBean(Constant.NODE_NAME_PREF,
                Constant.DEFAULT_HOST_NAME, Constant.NODE_NAME_PREF, true,
                false));

        // Node Type Preference
        editPrefs.add(new EditPrefsBean(Constant.NODE_TYPE_PREF, NodeType.HOST
                .getTypeName(), Constant.NODE_TYPE_PREF, true, false));

        // "Events per page" preference
        editPrefs.add(new EditPrefsBean(Constant.EVENT_PER_PAGE_PREF_REQ_ATT,
                String.valueOf(Constant.FIVE),
                Constant.EVENT_PER_PAGE_PREF_REQ_ATT, true, false));

        // Custom Portlet Title preference
        editPrefs.add(new EditPrefsBean(
                PreferenceConstants.CUSTOM_PORTLET_TITLE,
                Constant.EMPTY_STRING,
                PreferenceConstants.CUSTOM_PORTLET_TITLE, true, false));

        // Service Events - Host Name Preference
        editPrefs.add(new EditPrefsBean(Constant.DEFAULT_HOST_PREF,
                Constant.DEFAULT_HOST_NAME, Constant.SERVICE_HOST_PREF_REQ_ATT,
                true, false));

        // Service Events - Service Name Preference
        editPrefs.add(new EditPrefsBean(Constant.DEFAULT_SERVICE_PREF,
                Constant.DEFAULT_SERVICE_NAME, Constant.SERVICE_PREF_REQ_ATT,
                true, false));

    }

    /**
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
    	 // get preferences
        PortletPreferences pref = request.getPreferences();
        String nodeTypePref = pref.getValue(Constant.NODE_TYPE_PREF,
                Constant.EMPTY_STRING);
    	
        if (!PortletUtils.isInStatusViewer()) {
        	if (nodeTypePref.equals(NodeType.NETWORK.getTypeName())) {
	        	response.setTitle(Constant.EVENT
	                    + Constant.SPACE_COLON_SPACE
	                    + Constant.ENTIRE_NETWORK);
	        	super.setViewPath(Constant.JSP_EVENT_IFACE);
	            super.doView(request, response);
	        	return;
        	}
        	
           
            // Set the portlet title.
            if (nodeTypePref.equals(NodeType.SERVICE.getTypeName())) {
                // Node Type SERVICE
                response.setTitle(PortletUtils.getPortletTitle(request,
                        Constant.EVENT, true));

            } else {
                response.setTitle(PortletUtils.getPortletTitle(request,
                        Constant.EVENT));
            }

            String customPortletTitle = pref.getValue(
                    PreferenceConstants.CUSTOM_PORTLET_TITLE,
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

                    if (extRoleHostGroupList.isEmpty()
                            && extRoleServiceGroupList.isEmpty()) {
                        response.setTitle(Constant.EVENT
                                + Constant.SPACE_COLON_SPACE
                                + Constant.ENTIRE_NETWORK);
                    } else if (!extRoleHostGroupList.isEmpty()
                            && !extRoleHostGroupList
                                    .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                        response.setTitle(Constant.EVENT
                                + Constant.SPACE_COLON_SPACE
                                + userExtendedRoleBean.getDefaultHostGroup());
                    } else if (!extRoleServiceGroupList.isEmpty()
                            && !extRoleServiceGroupList
                                    .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                        response
                                .setTitle(Constant.EVENT
                                        + Constant.SPACE_COLON_SPACE
                                        + userExtendedRoleBean
                                                .getDefaultServiceGroup());
                    }
                }
            }
        } // end of if !PortletUtils.isInStatusViewer()
        super.setViewPath(Constant.JSP_EVENT_IFACE);
        super.doView(request, response);

    }

    /**
     * This method is Responsible for editing preferences of host group portlet
     * 
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit Event Preferences");
        // get preferences from request
        PortletPreferences pref = request.getPreferences();
        String nodeTypePrefValue = pref.getValue(Constant.NODE_TYPE_PREF,
                Constant.EMPTY_STRING);
        if (nodeTypePrefValue != null
                && (nodeTypePrefValue.equals(Constant.EMPTY_STRING) || !nodeTypePrefValue
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
        super
                .doEditPref(request, response, editPrefs,
                        Constant.EVENT_EDIT_PATH);

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

    /**
     * (non-Javadoc).
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

        if (nodeTypePrefObj != null) {
            String nodeTypeValue = (String) nodeTypePrefObj;
            String nodeNameValue = Constant.EMPTY_STRING;
            if (nodeNamePrefObj != null) {
                nodeNameValue = (String) nodeNamePrefObj;
            }

            pref.setValue(Constant.NODE_TYPE_PREF, nodeTypeValue);
            pref.setValue(Constant.NODE_NAME_PREF, nodeNameValue);
        }

        Object eventsPerPageObj = request
                .getParameter(Constant.EVENT_PER_PAGE_PREF_REQ_ATT);
        // Events per page
        if (eventsPerPageObj != null) {
            String eventsPerPagePrefValue = (String) eventsPerPageObj;
            pref.setValue(Constant.EVENT_PER_PAGE_PREF_REQ_ATT,
                    eventsPerPagePrefValue);
        }

        // Custom Portlet Title
        Object customPortletTitleObj = request
                .getParameter(PreferenceConstants.CUSTOM_PORTLET_TITLE);
        if (customPortletTitleObj != null) {
            pref.setValue(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                    (String) customPortletTitleObj);
        }

        // Event Service - Host Name Value
        Object eventServiceHostName = request
                .getParameter(Constant.SERVICE_HOST_PREF_REQ_ATT);
        if (eventServiceHostName != null) {
            pref.setValue(Constant.DEFAULT_HOST_PREF,
                    (String) eventServiceHostName);
        }

        // Event Service - Service Name Value
        Object eventServiceServiceName = request
                .getParameter(Constant.SERVICE_PREF_REQ_ATT);
        if (eventServiceServiceName != null) {
            pref.setValue(Constant.DEFAULT_SERVICE_PREF,
                    (String) eventServiceServiceName);
        }

        pref.store();
        response.setPortletMode(PortletMode.VIEW);
    }
}
