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
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * This portlet displays the list of hosts under current host group
 * 
 * @author mridu_narang
 * 
 */

public class ServiceListPortlet extends BasePortlet {

    /**
     * EDIT_SERVICE_LIST_PREFERENCES_TITLE
     */
    private static final String EDIT_SERVICE_LIST_PREFERENCES_TITLE = "Edit Service List Preferences";

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
                Constant.DEFAULT_HOST_GROUP_NAME, Constant.NODE_NAME_PREF,
                false, true));

        // Node Name Preference
        editPrefs.add(new EditPrefsBean(Constant.NODE_NAME_PREF,
                Constant.DEFAULT_HOST_NAME, Constant.NODE_NAME_PREF, true,
                false));

        // Node Type Preference
        editPrefs.add(new EditPrefsBean(Constant.NODE_TYPE_PREF, NodeType.HOST
                .getTypeName(), Constant.NODE_TYPE_PREF, true, false));

        // "Show Non OK Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_OK_PREF,
                Constant.TRUE, Constant.SERVICE_FILTER_OK_PREF, true,
                false));

        // "Show Non WARNING Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_WARNING_PREF,
                Constant.FALSE_CONSTANT, Constant.SERVICE_FILTER_WARNING_PREF, true,
                false));

        // "Show Non CRITICAL Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_CRITICAL_PREF,
                Constant.FALSE_CONSTANT, Constant.SERVICE_FILTER_CRITICAL_PREF, true,
                false));

        // "Show Non CRITICAL scheduled Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_CRITICAL_SCHEDULED_PREF,
                Constant.FALSE_CONSTANT, Constant.SERVICE_FILTER_CRITICAL_SCHEDULED_PREF, true,
                false));

        // "Show Non CRITICAL unscheduled Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_CRITICAL_UNSCHEDULED_PREF,
                Constant.FALSE_CONSTANT, Constant.SERVICE_FILTER_CRITICAL_UNSCHEDULED_PREF, true,
                false));

        // "Show UNKNOWN Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_UNKNOWN_PREF,
                Constant.FALSE_CONSTANT, Constant.SERVICE_FILTER_UNKNOWN_PREF, true,
                false));

        // "Show PENDING Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_PENDING_PREF,
                Constant.FALSE_CONSTANT, Constant.SERVICE_FILTER_PENDING_PREF, true,
                false));

        // "Show acknowledged Services" filter Preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICE_FILTER_ACKNOWLEDGED_PREF,
                Constant.FALSE_CONSTANT, Constant.SERVICE_FILTER_ACKNOWLEDGED_PREF, true,
                false));

        // "Services per page" preference
        editPrefs.add(new EditPrefsBean(Constant.SERVICES_PER_PAGE_PREF, String
                .valueOf(Constant.SIX), Constant.SERVICES_PER_PAGE_PREF, true,
                false));

        // Custom Portlet Title preference
        editPrefs.add(new EditPrefsBean(
                PreferenceConstants.CUSTOM_PORTLET_TITLE,
                Constant.EMPTY_STRING,
                PreferenceConstants.CUSTOM_PORTLET_TITLE, true, false));
    }

    /**
     * (non-Javadoc).
     * 
     * @see javax.portlet.GenericPortlet#doView(RenderRequest
     *      request,RenderResponse response)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.setViewPath(Constant.SERVICELIST_VIEW_PATH);
        // Set the portlet title.
        String serviceListPortletTitle = "Services";

        if (!PortletUtils.isInStatusViewer()) {
            try {
                // get preferences
                PortletPreferences pref = FacesUtils.getAllPreferences(request,
                        true);
                /*
                 * check for serviceFilterSelected preference and set portlet
                 * title accordingly
                 */
                String serviceFilterSelectedPref = pref.getValue(
                        Constant.SERVICE_FILTER_OK_PREF,
                        Constant.EMPTY_STRING);
                if (null == serviceFilterSelectedPref
                        || !serviceFilterSelectedPref
                                .equals(Constant.FALSE_CONSTANT)) {
                    serviceListPortletTitle = "Troubled Services";
                }
                response.setTitle(PortletUtils.getPortletTitle(request,
                        serviceListPortletTitle, false, false));

                // check for custom portlet title preference
                String customPortletTitle = pref.getValue(
                        PreferenceConstants.CUSTOM_PORTLET_TITLE,
                        Constant.EMPTY_STRING);
                if (null == customPortletTitle
                        || Constant.EMPTY_STRING.equals(customPortletTitle)) {

                    String nodeTypePref = pref.getValue(
                            Constant.NODE_TYPE_PREF, Constant.EMPTY_STRING);
                    String nodeNamePref = pref.getValue(
                            Constant.NODE_NAME_PREF, Constant.EMPTY_STRING);
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

                        if ((extRoleHostGroupList.isEmpty()
                                && extRoleServiceGroupList.isEmpty()) || nodeTypePref
                                .equals(NodeType.NETWORK.getTypeName())) {
                            response.setTitle(serviceListPortletTitle
                                    + Constant.SPACE_COLON_SPACE
                                    + Constant.ENTIRE_NETWORK);
                        } else if (!extRoleHostGroupList.isEmpty()
                                && !extRoleHostGroupList
                                        .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                            response.setTitle(serviceListPortletTitle
                                    + Constant.SPACE_COLON_SPACE
                                    + userExtendedRoleBean
                                            .getDefaultHostGroup());
                        } else if (!extRoleServiceGroupList.isEmpty()
                                && !extRoleServiceGroupList
                                        .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                            response.setTitle(serviceListPortletTitle
                                    + Constant.SPACE_COLON_SPACE
                                    + userExtendedRoleBean
                                            .getDefaultServiceGroup());
                        }
                    }
                }
            } catch (PreferencesException e) {
                // ignore
            }
        } else {
            // set the portlet title for Status Viewer
            response.setTitle(PortletUtils.getPortletTitle(request,
                    serviceListPortletTitle, false, false));
        }

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

            // by default 'service filter' will be false
            String serviceFilterOkValue = Constant.FALSE_CONSTANT;
            Object serviceFilterOkPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_OK_PREF);
            if (null != serviceFilterOkPrefObj) {
                serviceFilterOkValue = (String) serviceFilterOkPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_OK_PREF,
                    serviceFilterOkValue);

            // by default 'service filter' will be false
            String serviceFilterWarningValue = Constant.FALSE_CONSTANT;
            Object serviceFilterWarningPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_WARNING_PREF);
            if (null != serviceFilterWarningPrefObj) {
                serviceFilterWarningValue = (String) serviceFilterWarningPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_WARNING_PREF,
                    serviceFilterWarningValue);

            // by default 'service filter' will be false
            String serviceFilterCriticalValue = Constant.FALSE_CONSTANT;
            Object serviceFilterCriticalPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_CRITICAL_PREF);
            if (null != serviceFilterCriticalPrefObj) {
                serviceFilterCriticalValue = (String) serviceFilterCriticalPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_CRITICAL_PREF,
                    serviceFilterCriticalValue);

            // by default 'service filter' will be false
            String serviceFilterCriticalScheduledValue = Constant.FALSE_CONSTANT;
            Object serviceFilterCriticalScheduledPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_CRITICAL_SCHEDULED_PREF);
            if (null != serviceFilterCriticalScheduledPrefObj) {
                serviceFilterCriticalScheduledValue = (String) serviceFilterCriticalScheduledPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_CRITICAL_SCHEDULED_PREF,
                    serviceFilterCriticalScheduledValue);

            // by default 'service filter' will be false
            String serviceFilterCriticalUnscheduledValue = Constant.FALSE_CONSTANT;
            Object serviceFilterCriticalUnscheduledPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_CRITICAL_UNSCHEDULED_PREF);
            if (null != serviceFilterCriticalUnscheduledPrefObj) {
                serviceFilterCriticalUnscheduledValue = (String) serviceFilterCriticalUnscheduledPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_CRITICAL_UNSCHEDULED_PREF,
                    serviceFilterCriticalUnscheduledValue);

            // by default 'service filter' will be false
            String serviceFilterUnknownValue = Constant.FALSE_CONSTANT;
            Object serviceFilterUnknownPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_UNKNOWN_PREF);
            if (null != serviceFilterUnknownPrefObj) {
                serviceFilterUnknownValue = (String) serviceFilterUnknownPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_UNKNOWN_PREF,
                    serviceFilterUnknownValue);

            // by default 'service filter' will be false
            String serviceFilterPendingValue = Constant.FALSE_CONSTANT;
            Object serviceFilterPendingPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_PENDING_PREF);
            if (null != serviceFilterPendingPrefObj) {
                serviceFilterPendingValue = (String) serviceFilterPendingPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_PENDING_PREF,
                    serviceFilterPendingValue);

            // by default 'service filter' will be false
            String serviceFilterAcknowledgedValue = Constant.FALSE_CONSTANT;
            Object serviceFilterAcknowledgedPrefObj = request
                    .getParameter(Constant.SERVICE_FILTER_ACKNOWLEDGED_PREF);
            if (null != serviceFilterAcknowledgedPrefObj) {
                serviceFilterAcknowledgedValue = (String) serviceFilterAcknowledgedPrefObj;
            }
            pref.setValue(Constant.SERVICE_FILTER_ACKNOWLEDGED_PREF,
                    serviceFilterAcknowledgedValue);
        }

        // services per page preference
        Object servicesPerPageObj = request
                .getParameter(Constant.SERVICES_PER_PAGE_PREF);
        if (servicesPerPageObj != null) {
            String servicesPerPagePrefValue = (String) servicesPerPageObj;
            pref.setValue(Constant.SERVICES_PER_PAGE_PREF,
                    servicesPerPagePrefValue);
        }

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
        response.setTitle(EDIT_SERVICE_LIST_PREFERENCES_TITLE);

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
        super.doEditPref(request, response, editPrefs,
                Constant.SERVICELIST_EDIT_PATH);
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
