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
 * Seurat portlet.
 * 
 * @author nitin_jadhav
 */
public class SeuratPortlet extends BasePortlet {

    /**
     * ENTIRE_NETWORK_PARAM
     */
    private static final String ENTIRE_NETWORK_PARAM = "entireNetwork";

    /**
     * HOSTHEALTH_TITLE
     */
    public static final String SEURAT_TITLE = "Host Group Snapshot";

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
        // super.processAction(request, response, DashboardEditPrefConstants
        // .getRequestPreferenceParamsMap(NodeType.HOST_GROUP));

        // get preferences
        PortletPreferences pref = request.getPreferences();

        // get selected state of "entire network" radio button, host group name
        // and custom portlet title parameters and set them in preferences.
        Object entireNetworkRadioPref = request
                .getParameter(Constant.SEURAT_ENTIRENETWORK_PREF);
        Object hostGroupNameObj = request
                .getParameter(Constant.HOSTGROUP_PREF_REQ_ATT);
        Object titlePrefObj = request
                .getParameter(PreferenceConstants.CUSTOM_PORTLET_TITLE);

        // entireNetworkRadioPref is "entireNetwork", that means currently
        // "Entire Network" radio option is selected. in this case put
        // "$ENTIRE_NETWORK" in preferences ($ is used to avoid possibility of
        // similar host group with name "ENTIRE_NETWORK"). Otherwise put the
        // name of host group in preferences.
        if (entireNetworkRadioPref != null) {
            String entireNetworkRadio = (String) entireNetworkRadioPref;

            if (entireNetworkRadio.equals(ENTIRE_NETWORK_PARAM)) {
                pref
                        .setValue(Constant.SEURAT_ENTIRENETWORK_PREF,
                                Constant.TRUE);
                pref.setValue(Constant.DEFAULT_HOSTGROUP_PREF,
                        Constant.EMPTY_STRING);
            } else {
                if (hostGroupNameObj != null) {
                    String hostGroupName = ((String) hostGroupNameObj).trim();
                    pref.setValue(Constant.DEFAULT_HOSTGROUP_PREF,
                            hostGroupName);
                    pref.setValue(Constant.SEURAT_ENTIRENETWORK_PREF,
                            Constant.FALSE_CONSTANT);
                }
            }
        }

        if (titlePrefObj == null) {
            // null or empty string
            pref.setValue(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                    Constant.EMPTY_STRING);
        } else {
            // non-null, non-empty string
            String title = ((String) titlePrefObj).trim();
            pref.setValue(PreferenceConstants.CUSTOM_PORTLET_TITLE, title);
        }

        // store the preferences
        pref.store();

        // set the portlet mode
        response.setPortletMode(PortletMode.VIEW);
    }

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
        response.setTitle(PortletUtils.getPortletTitle(request, SEURAT_TITLE));
        super.setViewPath(Constant.SEURAT_IFACE);
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
        response.setTitle("Edit Seurat Preferences");
        // call doEditPref() of BasePortlet.

        List<EditPrefsBean> editPreferences = DashboardEditPrefConstants
                .getEditPreferences(NodeType.HOST_GROUP);

        DashboardEditPrefConstants.updateDefaultHostGroupEditPref(
                editPreferences, Constant.DEFAULT_HOSTGROUP_PREF);

        editPreferences.add(new EditPrefsBean(
                Constant.SEURAT_ENTIRENETWORK_PREF, Constant.TRUE,
                Constant.SEURAT_ENTIRENETWORK_PREF, true, false));

        super.doEditPref(request, response, editPreferences,
                Constant.SEURAT_EDIT_PATH);

    }
}
