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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * Service Information Portlet - provides Status and Check information for
 * particular Service. This will be used in "Service View" subpage.
 * 
 * @author swapnil_gujrathi
 */
public class ServiceInformationPortlet extends BasePortlet {
    /**
     * Constant String for portlet title
     */
    private static final String SERVICE_INFORMATION_PORTLET_TITLE = "Service Information";

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
                SERVICE_INFORMATION_PORTLET_TITLE, true));

        super.setViewPath(Constant.SERVICE_INFORMATION_PATH);
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

        Object hostPrefObj = request
                .getParameter(Constant.SERVICE_HOST_PREF_REQ_ATT);

        Object servicePrefObj = request
                .getParameter(Constant.SERVICE_PREF_REQ_ATT);
        Object custLink1PrefObj = request
                .getParameter(Constant.SERVICE_CUSTLINK1_PREF_REQ_ATT);
        Object custLink2PrefObj = request
                .getParameter(Constant.SERVICE_CUSTLINK2_PREF_REQ_ATT);
        Object custLink3PrefObj = request
                .getParameter(Constant.SERVICE_CUSTLINK3_PREF_REQ_ATT);
        Object custLink4PrefObj = request
                .getParameter(Constant.SERVICE_CUSTLINK4_PREF_REQ_ATT);
        Object custLink5PrefObj = request
                .getParameter(Constant.SERVICE_CUSTLINK5_PREF_REQ_ATT);

        PortletPreferences pref = request.getPreferences();
        if (hostPrefObj != null) {
            String hostPrefValue = (String) hostPrefObj;
            pref.setValue(Constant.DEFAULT_HOST_PREF, hostPrefValue);
        }

        if (servicePrefObj != null) {
            String servicePrefValue = (String) servicePrefObj;
            pref.setValue(Constant.DEFAULT_SERVICE_PREF, servicePrefValue);
        }
        if (custLink1PrefObj != null) {
            String custLink1PrefValue = (String) custLink1PrefObj;
            pref.setValue(Constant.DEFAULT_SERVICE_CUST_LINK1_PREF,
                    custLink1PrefValue);
        }

        if (custLink2PrefObj != null) {
            String custLink2PrefValue = (String) custLink2PrefObj;
            pref.setValue(Constant.DEFAULT_SERVICE_CUST_LINK2_PREF,
                    custLink2PrefValue);
        }

        if (custLink3PrefObj != null) {
            String custLink3PrefValue = (String) custLink3PrefObj;
            pref.setValue(Constant.DEFAULT_SERVICE_CUST_LINK3_PREF,
                    custLink3PrefValue);
        }

        if (custLink4PrefObj != null) {
            String custLink4PrefValue = (String) custLink4PrefObj;
            pref.setValue(Constant.DEFAULT_SERVICE_CUST_LINK4_PREF,
                    custLink4PrefValue);
        }

        if (custLink5PrefObj != null) {
            String custLink5PrefValue = (String) custLink5PrefObj;
            pref.setValue(Constant.DEFAULT_SERVICE_CUST_LINK5_PREF,
                    custLink5PrefValue);
        }

        // Custom Portlet Title
        Object customPortletTitleObj = request
                .getParameter(PreferenceConstants.CUSTOM_PORTLET_TITLE);
        if (customPortletTitleObj != null) {
            pref.setValue(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                    (String) customPortletTitleObj);
        }

        pref.store();
        response.setPortletMode(PortletMode.VIEW);

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
        response.setTitle("Edit ServiceStatus Preferences");

        PortletPreferences pref = request.getPreferences();
        String hostPrefValue = pref.getValue(Constant.DEFAULT_HOST_PREF,
                Constant.DEFAULT_HOST_NAME);
        String servicePrefValue = pref.getValue(Constant.DEFAULT_SERVICE_PREF,
                Constant.DEFAULT_SERVICE_NAME);

        String custLink1PrefValue = pref
                .getValue(Constant.DEFAULT_SERVICE_CUST_LINK1_PREF,
                        Constant.EMPTY_STRING);
        String custLink2PrefValue = pref
                .getValue(Constant.DEFAULT_SERVICE_CUST_LINK2_PREF,
                        Constant.EMPTY_STRING);
        String custLink3PrefValue = pref
                .getValue(Constant.DEFAULT_SERVICE_CUST_LINK3_PREF,
                        Constant.EMPTY_STRING);
        String custLink4PrefValue = pref
                .getValue(Constant.DEFAULT_SERVICE_CUST_LINK4_PREF,
                        Constant.EMPTY_STRING);
        String custLink5PrefValue = pref
                .getValue(Constant.DEFAULT_SERVICE_CUST_LINK5_PREF,
                        Constant.EMPTY_STRING);

        request.setAttribute(Constant.SERVICE_HOST_PREF_REQ_ATT, hostPrefValue);
        request.setAttribute(Constant.SERVICE_PREF_REQ_ATT, servicePrefValue);
        request.setAttribute(Constant.SERVICE_CUSTLINK1_PREF_REQ_ATT,
                custLink1PrefValue);
        request.setAttribute(Constant.SERVICE_CUSTLINK2_PREF_REQ_ATT,
                custLink2PrefValue);
        request.setAttribute(Constant.SERVICE_CUSTLINK3_PREF_REQ_ATT,
                custLink3PrefValue);
        request.setAttribute(Constant.SERVICE_CUSTLINK4_PREF_REQ_ATT,
                custLink4PrefValue);
        request.setAttribute(Constant.SERVICE_CUSTLINK5_PREF_REQ_ATT,
                custLink5PrefValue);

        // Custom Portlet Title
        String customPortletTitlePrefValue = pref
                .getValue(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                        Constant.EMPTY_STRING);
        request.setAttribute(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                customPortletTitlePrefValue);

        PortletContext ctxt = getPortletContext();
        PortletRequestDispatcher disp = ctxt
                .getRequestDispatcher(Constant.SERVICEINFO_EDIT_PATH);
        response.setContentType("text/html");
        disp.include(request, response);
    }
}
