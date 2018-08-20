package com.groundworkopensource.portal.statusviewer.portlet;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.common.EditPrefsBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DashboardEditPrefConstants;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

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

/**
 * Host Group HEalth portlet.
 * 
 * @author swapnil_gujrathi
 */
public class HostGroupHealthPortlet extends BasePortlet {

    /**
     * HOST_GROUP_HEALTH_PORTLET_TITLE
     */
    private static final String HOST_GROUP_HEALTH_PORTLET_TITLE = "Host Group Health";

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.setViewPath(Constant.HOST_GROUP_HEALTH_PATH);
        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                HOST_GROUP_HEALTH_PORTLET_TITLE, false, false));

        PortletUtils.setHostGroupDashboardPortletTitle(request, response,
                HOST_GROUP_HEALTH_PORTLET_TITLE);

        super.doView(request, response);
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
        // call processAction() of BasePortlet.
        super.processAction(request, response, DashboardEditPrefConstants
                .getRequestPreferenceParamsMap(NodeType.HOST_GROUP));
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
        response.setTitle("Edit HostGroup Preferences");
        // call doEditPref() of BasePortlet.
        List<EditPrefsBean> editPreferences = DashboardEditPrefConstants
                .getEditPreferences(NodeType.HOST_GROUP);
        DashboardEditPrefConstants.updateDefaultHostGroupEditPref(
                editPreferences, Constant.DEFAULT_HOSTGROUP_PREF);
        super.doEditPref(request, response, editPreferences,
                Constant.HOSTGROUPSTAT_EDIT_PATH);
    }

}
