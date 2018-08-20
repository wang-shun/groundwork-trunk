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
 * Service Group Health portlet.
 * 
 * @author rashmi_tambe
 */
public class ServiceGroupHealthPortlet extends BasePortlet {
    /**
     * SERVICE_GROUP_HEALTH_IFACE - view file for Host Group HEalth Portlet.
     */
    private static final String SERVICE_GROUP_HEALTH_IFACE = "/jsp/serviceGroupHealth.iface";
    /**
     * SERVICE_GROUP_HEALTH_PORTLET_TITLE.
     */
    private static final String SERVICE_GROUP_HEALTH_PORTLET_TITLE = "Service Group Health";

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                SERVICE_GROUP_HEALTH_PORTLET_TITLE, false, false));

        PortletUtils.setServiceGroupDashboardPortletTitle(request, response,
                SERVICE_GROUP_HEALTH_PORTLET_TITLE);

        super.setViewPath(SERVICE_GROUP_HEALTH_IFACE);
        super.doView(request, response);
    }

    /**
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        // call processAction() of BasePortlet.
        super.processAction(request, response, DashboardEditPrefConstants
                .getRequestPreferenceParamsMap(NodeType.SERVICE_GROUP));
    }

    /**
     * This method is Responsible for editing preferences of service group
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
        response.setTitle("Edit ServiceGroup Preferences");

        List<EditPrefsBean> editPreferences = DashboardEditPrefConstants
                .getEditPreferences(NodeType.SERVICE_GROUP);
        DashboardEditPrefConstants.updateDefaultServiceGroupEditPref(
                editPreferences, Constant.DEFAULT_SERVICEGROUP_PREF);
        // call doEditPref() of BasePortlet.
        super.doEditPref(request, response, editPreferences,
                Constant.SERVICEGROUPSTAT_EDIT_PATH);
    }
}
