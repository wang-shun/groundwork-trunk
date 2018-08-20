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
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DashboardEditPrefConstants;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * This portlet display the host statistics and pie chart.
 * 
 * @author manish_kjain
 * 
 */
public class HostStatisticsPortlet extends BasePortlet {

    /**
     * HOST_STATUS_PORTLET_TITLE.
     */
    private static final String HOST_STATUS_PORTLET_TITLE = "Host Status";

    /**
     * List of edit preferences. -> to be used in doEdit()
     */
    private static List<EditPrefsBean> editPrefs = new ArrayList<EditPrefsBean>();

    // static block for initializing editPrefs list
    static {

        // Host Group Preference - for generating auto complete list
        editPrefs.add(new EditPrefsBean(Constant.DEFAULT_HOSTGROUP_PREF,
                Constant.HOSTGROUP_NAME_LINUX, Constant.HOSTGROUP_PREF_REQ_ATT,
                true, false));
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
        Object hostgroupNamePrefObj = request
                .getParameter(Constant.HOSTGROUP_PREF_REQ_ATT);

        String nodeTypeValue = (String) nodeTypePrefObj;
        String hostgroupNameValue = (String) hostgroupNamePrefObj;

        pref.setValue(Constant.NODE_TYPE_PREF, nodeTypeValue);
        if (null == hostgroupNameValue) {
            pref.setValue(Constant.DEFAULT_HOSTGROUP_PREF,
                    Constant.EMPTY_STRING);
        } else {
            pref.setValue(Constant.DEFAULT_HOSTGROUP_PREF, hostgroupNameValue);
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
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.setViewPath(Constant.HOST_VIEW_PATH);

        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                HOST_STATUS_PORTLET_TITLE));
        /*PortletUtils.setHostGroupDashboardPortletTitle(request, response,
                HOST_STATUS_PORTLET_TITLE);*/

        super.doView(request, response);
    }

    /**
     * This method is Responsible for editing preferences of host statistics
     * portlet
     * 
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit Host Statistics Preferences");

        DashboardEditPrefConstants.updateDefaultHostGroupEditPref(editPrefs,
                Constant.DEFAULT_HOSTGROUP_PREF);

        // call doEditPref() of BasePortlet.
        super.doEditPref(request, response, editPrefs,
                Constant.HOSTGROUPNETWORK_EDIT_PATH);
    }

}
