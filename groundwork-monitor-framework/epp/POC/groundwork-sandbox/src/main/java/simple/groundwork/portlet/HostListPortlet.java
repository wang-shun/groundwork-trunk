package simple.groundwork.portlet;
///*
// * 
// * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
// * reserved. This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License version 2 as
// * published by the Free Software Foundation.
// * 
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// * details.
// * 
// * You should have received a copy of the GNU General Public License along with
// * this program; if not, write to the Free Software Foundation, Inc., 51
// * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
// */
//package com.groundworkopensource.portal.statusviewer.portlet;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.portlet.ActionRequest;
//import javax.portlet.ActionResponse;
//import javax.portlet.PortletException;
//import javax.portlet.PortletMode;
//import javax.portlet.PortletPreferences;
//import javax.portlet.RenderRequest;
//import javax.portlet.RenderResponse;
//
///**
// * This portlet displays the list of hosts under current host group
// * 
// * @author nitin_jadhav
// * 
// */
//
//public class HostListPortlet extends BasePortlet {
//
//    /**
//     * List of edit preferences. -> to be used in doEdit()
//     */
//    private static List<EditPrefsBean> editPrefs = new ArrayList<EditPrefsBean>();
//
//    // static block for initializing editPrefs list
//    static {
//
//        // Host Group Preference - for generating auto complete list
//        editPrefs.add(new EditPrefsBean(Constant.DEFAULT_HOSTGROUP_PREF,
//                Constant.HOSTGROUP_NAME_LINUX, Constant.HOSTGROUP_PREF_REQ_ATT,
//                true, false));
//        // Node Type Preference
//        editPrefs.add(new EditPrefsBean(Constant.NODE_TYPE_PREF,
//                NodeType.NETWORK.getTypeName(), Constant.NODE_TYPE_PREF, true,
//                false));
//        // "Show Hosts that are in state UP" filter Preference
//        editPrefs.add(new EditPrefsBean(Constant.HOST_FILTER_UP_PREF,
//                Constant.TRUE, Constant.HOST_FILTER_UP_PREF,
//                true, false));
//        // "Show Hosts that are in state DOWN unscheduled" filter Preference
//        editPrefs.add(new EditPrefsBean(Constant.HOST_FILTER_DOWN_UNSCHEDULED_PREF,
//                Constant.FALSE_CONSTANT, Constant.HOST_FILTER_DOWN_UNSCHEDULED_PREF,
//                true, false));
//        // "Show Hosts that are in state DOWN scheduled" filter Preference
//        editPrefs.add(new EditPrefsBean(Constant.HOST_FILTER_DOWN_SCHEDULED_PREF,
//                Constant.FALSE_CONSTANT, Constant.HOST_FILTER_DOWN_SCHEDULED_PREF,
//                true, false));
//        // "Show Hosts that are in state UNREACHABLE" filter Preference
//        editPrefs.add(new EditPrefsBean(Constant.HOST_FILTER_UNREACHABLE_PREF,
//        	     Constant.FALSE_CONSTANT, Constant.HOST_FILTER_UNREACHABLE_PREF,
//        	     true, false));
//        // "Show Hosts that are in state PENDING" filter Preference
//        editPrefs.add(new EditPrefsBean(Constant.HOST_FILTER_PENDING_PREF,
//                 Constant.FALSE_CONSTANT, Constant.HOST_FILTER_PENDING_PREF,
//                 true, false));
//        // "Show Hosts that are in state acknowledged" filter Preference
//        editPrefs.add(new EditPrefsBean(Constant.HOST_FILTER_ACKNOWLEDGED_PREF,
//                 Constant.FALSE_CONSTANT, Constant.HOST_FILTER_ACKNOWLEDGED_PREF,
//                 true, false));
//        // "Hosts per page" preference
//        editPrefs.add(new EditPrefsBean(Constant.HOSTS_PER_PAGE_PREF, String
//                .valueOf(Constant.SIX), Constant.HOSTS_PER_PAGE_PREF, true,
//                false));
//        // Custom Portlet Title preference
//        editPrefs.add(new EditPrefsBean(
//                PreferenceConstants.CUSTOM_PORTLET_TITLE,
//                Constant.EMPTY_STRING,
//                PreferenceConstants.CUSTOM_PORTLET_TITLE, true, false));
//
//    }
//
//    /**
//     * (non-Javadoc).
//     * 
//     * @see javax.portlet.GenericPortlet#doView(RenderRequest
//     *      request,RenderResponse response)
//     */
//    @Override
//    protected void doView(RenderRequest request, RenderResponse response)
//            throws PortletException, IOException {
//        super.setViewPath(Constant.HOSTLIST_VIEW_PATH);
//        String hostListPortletTitle = "Hosts";
//
//        if (!PortletUtils.isInStatusViewer()) {
//            try {
//                PortletPreferences allPreferences = FacesUtils
//                        .getAllPreferences(request, false);
//                if (null != allPreferences) {
//                    /*
//                     * check for hostFilterSelected preference and set portlet
//                     * title accordingly
//                     */
//                    String hostFilterUPPref = allPreferences.getValue(
//                            Constant.HOST_FILTER_UP_PREF,
//                            Constant.EMPTY_STRING);
//                    if (null == hostFilterUPPref
//                            || !hostFilterUPPref
//                                    .equals(Constant.FALSE_CONSTANT)) {
//                        hostListPortletTitle = "Troubled Hosts";
//                    }
//                    // set the title
//                    response.setTitle(PortletUtils.getPortletTitle(request,
//                            hostListPortletTitle, false, true));
//                    String hgPref = allPreferences.getValue(
//                    		Constant.NODE_TYPE_PREF,
//                            Constant.EMPTY_STRING);
//                    // if default HG preference is null or empty
//                    String defaultHGPref = allPreferences.getValue(
//                            PreferenceConstants.DEFAULT_HOST_GROUP_PREF,
//                            Constant.EMPTY_STRING);
//                    if ((null == defaultHGPref
//                            || Constant.EMPTY_STRING.equals(defaultHGPref)) && !hgPref.equals("Network")) {
//                        // check if custom portlet title is set
//                        String customPortletTitle = allPreferences.getValue(
//                                PreferenceConstants.CUSTOM_PORTLET_TITLE,
//                                Constant.EMPTY_STRING);
//                        if (null == customPortletTitle
//                                || Constant.EMPTY_STRING
//                                        .equals(customPortletTitle)) {
//                            // now check for defaultHostGroupPreference set in
//                            // portlet.xml
//                            String hgPreferenceFromPortletXML = allPreferences
//                                    .getValue(
//                                            Constant.PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE,
//                                            Constant.EMPTY_STRING);
//                            if (null != hgPreferenceFromPortletXML
//                                    && !Constant.EMPTY_STRING
//                                            .equals(hgPreferenceFromPortletXML)) {
//                                // initialize UserExtendedRoleBean
//                                UserExtendedRoleBean userExtendedRoleBean = new UserExtendedRoleBean(
//                                        PortletUtils
//                                                .getExtendedRoleAttributes());
//
//                                // get the extended role host group list
//                                List<String> extRoleHostGroupList = userExtendedRoleBean
//                                        .getExtRoleHostGroupList();
//                                if (!extRoleHostGroupList.isEmpty()
//                                        && !extRoleHostGroupList
//                                                .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
//                                        && !extRoleHostGroupList
//                                                .contains(hgPreferenceFromPortletXML)) {
//                                    response.setTitle(hostListPortletTitle
//                                            + Constant.SPACE_COLON_SPACE
//                                            + userExtendedRoleBean
//                                                    .getDefaultHostGroup());
//                                }
//                            }
//                        }
//                    }
//                }
//
//            } catch (PreferencesException e) {
//                // ignore
//            }
//        } else {
//            // Set the portlet title for Status Viewer.
//            response.setTitle(PortletUtils.getPortletTitle(request,
//                    hostListPortletTitle, false, true));
//        }
//
//        super.doView(request, response);
//    }
//
//    /**
//     * (non-Javadoc)
//     * 
//     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
//     *      javax.portlet.ActionResponse)
//     */
//    @Override
//    public void processAction(ActionRequest request, ActionResponse response)
//            throws PortletException, IOException {
//        // get preferences
//        PortletPreferences pref = request.getPreferences();
//        // get NodeName and NodeType parameters and set them in preferences.
//        Object nodeTypePrefObj = request.getParameter(Constant.NODE_TYPE_PREF);
//        Object hostgroupNamePrefObj = request
//                .getParameter(Constant.HOSTGROUP_PREF_REQ_ATT);
//
//        String nodeTypeValue = (String) nodeTypePrefObj;
//        String hostgroupNameValue = (String) hostgroupNamePrefObj;
//
//        pref.setValue(Constant.NODE_TYPE_PREF, nodeTypeValue);
//        if (null == hostgroupNameValue) {
//            pref.setValue(Constant.DEFAULT_HOSTGROUP_PREF,
//                    Constant.EMPTY_STRING);
//        } else {
//            pref.setValue(Constant.DEFAULT_HOSTGROUP_PREF, hostgroupNameValue);
//        }
//        
//        // by default 'host filter UP' will be false
//        String hostFilterUpValue = Constant.FALSE_CONSTANT;
//        Object hostFilterUpPrefObj = request
//                .getParameter(Constant.HOST_FILTER_UP_PREF);
//        if (null != hostFilterUpPrefObj) {
//        	hostFilterUpValue = (String) hostFilterUpPrefObj;
//        }
//        pref.setValue(Constant.HOST_FILTER_UP_PREF, hostFilterUpValue);
//        
//        // by default 'host filter DOWN unscheduled' will be false
//        String hostFilterDownUnscheduledValue = Constant.FALSE_CONSTANT;
//        Object hostFilterDownUnscheduledPrefObj = request
//                .getParameter(Constant.HOST_FILTER_DOWN_UNSCHEDULED_PREF);
//        if (null != hostFilterDownUnscheduledPrefObj) {
//        	hostFilterDownUnscheduledValue = (String) hostFilterDownUnscheduledPrefObj;
//        }
//        pref.setValue(Constant.HOST_FILTER_DOWN_UNSCHEDULED_PREF, hostFilterDownUnscheduledValue);
//        
//        // by default 'host filter DOWN scheduled' will be false
//        String hostFilterDownScheduledValue = Constant.FALSE_CONSTANT;
//        Object hostFilterDownScheduledPrefObj = request
//                .getParameter(Constant.HOST_FILTER_DOWN_SCHEDULED_PREF);
//        if (null != hostFilterDownScheduledPrefObj) {
//        	hostFilterDownScheduledValue = (String) hostFilterDownScheduledPrefObj;
//        }
//        pref.setValue(Constant.HOST_FILTER_DOWN_SCHEDULED_PREF, hostFilterDownScheduledValue);
//        
//        // by default 'host filter UNREACHABLE' will be false
//        String hostFilterUnreachableValue = Constant.FALSE_CONSTANT;
//        Object hostFilterUnreachablePrefObj = request
//                .getParameter(Constant.HOST_FILTER_UNREACHABLE_PREF);
//        if (null != hostFilterUnreachablePrefObj) {
//        	hostFilterUnreachableValue = (String) hostFilterUnreachablePrefObj;
//        }
//        pref.setValue(Constant.HOST_FILTER_UNREACHABLE_PREF, hostFilterUnreachableValue);
//        
//        // by default 'host filter PENDING' will be false
//        String hostFilterPendingValue = Constant.FALSE_CONSTANT;
//        Object hostFilterPendingPrefObj = request
//                .getParameter(Constant.HOST_FILTER_PENDING_PREF);
//        if (null != hostFilterPendingPrefObj) {
//        	hostFilterPendingValue = (String) hostFilterPendingPrefObj;
//        }
//        pref.setValue(Constant.HOST_FILTER_PENDING_PREF, hostFilterPendingValue);
//        
//        // by default 'host filter acknowledge' will be false
//        String hostFilterAcknowledgedValue = Constant.FALSE_CONSTANT;
//        Object hostFilterAcknowledgedPrefObj = request
//                .getParameter(Constant.HOST_FILTER_ACKNOWLEDGED_PREF);
//        if (null != hostFilterAcknowledgedPrefObj) {
//        	hostFilterAcknowledgedValue = (String) hostFilterAcknowledgedPrefObj;
//        }
//        pref.setValue(Constant.HOST_FILTER_ACKNOWLEDGED_PREF, hostFilterAcknowledgedValue);
//        
//        // hosts per page preference
//        Object hostsPerPageObj = request
//                .getParameter(Constant.HOSTS_PER_PAGE_PREF);
//        if (hostsPerPageObj != null) {
//            String hostsPerPagePrefValue = (String) hostsPerPageObj;
//            pref.setValue(Constant.HOSTS_PER_PAGE_PREF, hostsPerPagePrefValue);
//        }
//
//        // Custom Portlet Title
//        Object customPortletTitleObj = request
//                .getParameter(PreferenceConstants.CUSTOM_PORTLET_TITLE);
//        if (customPortletTitleObj != null) {
//            pref.setValue(PreferenceConstants.CUSTOM_PORTLET_TITLE,
//                    (String) customPortletTitleObj);
//        }
//
//        // store the preferences
//        pref.store();
//
//        // set the portlet mode
//        response.setPortletMode(PortletMode.VIEW);
//    }
//
//    /**
//     * This method is Responsible for editing preferences of host statistics
//     * portlet
//     * 
//     * @param request
//     * @param response
//     * @throws PortletException
//     * @throws IOException
//     */
//    @Override
//    protected void doEdit(RenderRequest request, RenderResponse response)
//            throws PortletException, IOException {
//        response.setTitle("Edit Host List Preferences");
//
//        DashboardEditPrefConstants.updateDefaultHostGroupEditPref(editPrefs,
//                Constant.DEFAULT_HOSTGROUP_PREF);
//
//        // call doEditPref() of BasePortlet.
//        super.doEditPref(request, response, editPrefs, "/jsp/hostListPref.jsp");
//    }
//
//}
