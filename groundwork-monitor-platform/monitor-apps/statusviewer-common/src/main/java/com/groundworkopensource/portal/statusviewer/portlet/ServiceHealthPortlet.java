package com.groundworkopensource.portal.statusviewer.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.DashboardEditPrefConstants;

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
 * Service Health portlet.
 * 
 * @author rashmi_tambe
 */
public class ServiceHealthPortlet extends BasePortlet {
    /**
     * SERVICE_HEALTH_IFACE - view file for Service HEalth Portlet.
     */
    private static final String SERVICE_HEALTH_IFACE = "/jsp/serviceHealth.iface";

    /**
     * SERVICE_HEALTH_PORTLET_TITLE.
     */
    private static final String SERVICE_HEALTH_PORTLET_TITLE = "Service Health";

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                SERVICE_HEALTH_PORTLET_TITLE, true));

        super.setViewPath(SERVICE_HEALTH_IFACE);
        super.doView(request, response);
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
        // call processAction() of BasePortlet.
        super.processAction(request, response, DashboardEditPrefConstants
                .getRequestPreferenceParamsMap(NodeType.SERVICE));
    }

    /**
     * This method is Responsible for editing preferences of service portlet
     * 
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit ServiceStatus Preferences");

        // call doEditPref() of BasePortlet.
        super.doEditPref(request, response, DashboardEditPrefConstants
                .getEditPreferences(NodeType.SERVICE),
                Constant.SERVICESTAT_EDIT_PATH);
    }

}
