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

package com.groundworkopensource.portal.statusviewer.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.groundworkopensource.portal.common.EditPrefsBean;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;

/**
 * @author swapnil_gujrathi
 * 
 */
public class DashboardEditPrefConstants {
    /**
     * Map containing requestParam and prefKey for HostGroup. -> to be used in
     * processAction()
     */
    private static Map<String, String> reqPrefParamMapHostGroup = new HashMap<String, String>();

    /**
     * List of edit preferences for HostGroup. -> to be used in doEdit()
     */
    private static List<EditPrefsBean> editPrefsHostGroup = new ArrayList<EditPrefsBean>();

    /**
     * Map containing requestParam and prefKey for Host. -> to be used in
     * processAction()
     */
    private static Map<String, String> reqPrefParamMapHost = new HashMap<String, String>();

    /**
     * List of edit preferences for Host. -> to be used in doEdit()
     */
    private static List<EditPrefsBean> editPrefsHost = new ArrayList<EditPrefsBean>();

    /**
     * List of edit preferences for Service. -> to be used in doEdit()
     */
    private static List<EditPrefsBean> editPrefsService = new ArrayList<EditPrefsBean>();

    /**
     * Map containing requestParam and prefKey for Service. -> to be used in
     * processAction()
     */
    private static Map<String, String> reqPrefParamMapService = new HashMap<String, String>();

    /**
     * Map containing requestParam and prefKey for ServiceGroup. -> to be used
     * in processAction()
     */
    private static Map<String, String> reqPrefParamMapServiceGroup = new HashMap<String, String>();

    /**
     * List of edit preferences for ServiceGroup. -> to be used in doEdit()
     */
    private static List<EditPrefsBean> editPrefsServiceGroup = new ArrayList<EditPrefsBean>();

    /**
     * customPortletTitleEditPrefsBean.
     */
    private static EditPrefsBean customPortletTitleEditPrefsBean = new EditPrefsBean(
            PreferenceConstants.CUSTOM_PORTLET_TITLE, Constant.EMPTY_STRING,
            PreferenceConstants.CUSTOM_PORTLET_TITLE, true, false);

    // static block for initializing reqPrefParamMap map, editPrefs list
    static {
        // ***************************************************
        // ---------------- HOST GROUP -----------------------
        // ***************************************************
        // initialize reqPrefParamMap
        reqPrefParamMapHostGroup.put(Constant.HOSTGROUP_PREF_REQ_ATT,
                Constant.DEFAULT_HOSTGROUP_PREF);
        reqPrefParamMapHostGroup.put(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                PreferenceConstants.CUSTOM_PORTLET_TITLE);

        // initialize edit preferences list
        // Host Group Name preference
        editPrefsHostGroup.add(new EditPrefsBean(
                Constant.DEFAULT_HOSTGROUP_PREF, Constant.HOSTGROUP_NAME_LINUX,
                Constant.HOSTGROUP_PREF_REQ_ATT, true, true));
        // Custom Portlet Title preference
        editPrefsHostGroup.add(customPortletTitleEditPrefsBean);

        // ***************************************************
        // ------------------ HOST ---------------------------
        // ***************************************************
        // initialize reqPrefParamMap
        reqPrefParamMapHost.put(Constant.HOST_PREF_REQ_ATT,
                Constant.DEFAULT_HOST_PREF);
        reqPrefParamMapHost.put(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                PreferenceConstants.CUSTOM_PORTLET_TITLE);

        // initialize edit preferences list
        // Host Name preference
        editPrefsHost.add(new EditPrefsBean(Constant.DEFAULT_HOST_PREF,
                Constant.DEFAULT_HOST_NAME, Constant.HOST_PREF_REQ_ATT, true,
                true));
        // Custom Portlet Title preference
        editPrefsHost.add(customPortletTitleEditPrefsBean);

        // ***************************************************
        // ------------------ SERVICE ------------------------
        // ***************************************************
        // initialize reqPrefParamMap
        reqPrefParamMapService.put(Constant.SERVICE_HOST_PREF_REQ_ATT,
                Constant.DEFAULT_HOST_PREF);
        reqPrefParamMapService.put(Constant.SERVICE_PREF_REQ_ATT,
                Constant.DEFAULT_SERVICE_PREF);
        reqPrefParamMapService.put(PreferenceConstants.CUSTOM_PORTLET_TITLE,
                PreferenceConstants.CUSTOM_PORTLET_TITLE);

        // initialize edit preferences list
        // Host Name preference
        editPrefsService.add(new EditPrefsBean(Constant.DEFAULT_HOST_PREF,
                Constant.DEFAULT_HOST_NAME, Constant.SERVICE_HOST_PREF_REQ_ATT,
                true, true));
        // Service Name preference
        editPrefsService.add(new EditPrefsBean(Constant.DEFAULT_SERVICE_PREF,
                Constant.DEFAULT_SERVICE_NAME, Constant.SERVICE_PREF_REQ_ATT,
                true, true));
        // Custom Portlet Title preference
        editPrefsService.add(customPortletTitleEditPrefsBean);

        // ***************************************************
        // ---------------- SERVICE GROUP -----------------------
        // ***************************************************
        // initialize reqPrefParamMap
        reqPrefParamMapServiceGroup.put(Constant.SERVICEGROUP_PREF_REQ_ATT,
                Constant.DEFAULT_SERVICEGROUP_PREF);
        reqPrefParamMapServiceGroup.put(
                PreferenceConstants.CUSTOM_PORTLET_TITLE,
                PreferenceConstants.CUSTOM_PORTLET_TITLE);

        // initialize edit preferences list
        // Host Group Name preference
        editPrefsServiceGroup.add(new EditPrefsBean(
                Constant.DEFAULT_SERVICEGROUP_PREF,
                Constant.DEFAULT_SERVICE_GROUP_NAME,
                Constant.SERVICEGROUP_PREF_REQ_ATT, true, true));
        // Custom Portlet Title preference
        editPrefsServiceGroup.add(customPortletTitleEditPrefsBean);
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected DashboardEditPrefConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the reqPrefParamMapHostGroup.
     * 
     * @param nodeType
     * 
     * @return the reqPrefParamMapHostGroup
     */
    public static Map<String, String> getRequestPreferenceParamsMap(
            NodeType nodeType) {
        switch (nodeType) {
            case HOST_GROUP:
                return reqPrefParamMapHostGroup;

            case HOST:
                return reqPrefParamMapHost;

            case SERVICE:
                return reqPrefParamMapService;

            case SERVICE_GROUP:
                return reqPrefParamMapServiceGroup;

            default:
                return null;
        }

    }

    /**
     * Returns the editPrefsHostGroup.
     * 
     * @param nodeType
     * 
     * @return the editPrefsHostGroup
     */
    public static List<EditPrefsBean> getEditPreferences(NodeType nodeType) {
        switch (nodeType) {
            case HOST_GROUP:
                return editPrefsHostGroup;

            case HOST:
                return editPrefsHost;

            case SERVICE:
                return editPrefsService;

            case SERVICE_GROUP:
                return editPrefsServiceGroup;

            default:
                return null;
        }
    }

    /**
     * @param editPreferences
     * @param editPrefKey
     */
    public static void updateDefaultHostGroupEditPref(
            List<EditPrefsBean> editPreferences, String editPrefKey) {
        for (EditPrefsBean editPrefsBean : editPreferences) {
            if (editPrefsBean.getPreferenceKey().equalsIgnoreCase(editPrefKey)) {
                String defaultPreferenceValue = editPrefsBean
                        .getDefaultPreferenceValue();
                // initialize UserExtendedRoleBean
                UserExtendedRoleBean userExtendedRoleBean = PortletUtils
                        .getUserExtendedRoleBean();
                // get the extended role host group list
                List<String> extRoleHostGroupList = userExtendedRoleBean
                        .getExtRoleHostGroupList();
                if (extRoleHostGroupList.isEmpty()) {
                    /*
                     * if list is empty, it means user has unrestricted access.
                     * Then set "Linux Servers" as a default Host Group.
                     */
                    editPrefsBean
                            .setDefaultPreferenceValue(Constant.DEFAULT_HOST_GROUP_NAME);
                    break;
                } else if (!extRoleHostGroupList.isEmpty()
                        && !extRoleHostGroupList
                                .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
                        && !extRoleHostGroupList
                                .contains(defaultPreferenceValue)) {
                    editPrefsBean
                            .setDefaultPreferenceValue(userExtendedRoleBean
                                    .getDefaultHostGroup());
                    break;
                }
            }
        }
    }

    /**
     * @param editPreferences
     * @param editPrefKey
     */
    public static void updateDefaultServiceGroupEditPref(
            List<EditPrefsBean> editPreferences, String editPrefKey) {
        for (EditPrefsBean editPrefsBean : editPreferences) {
            if (editPrefsBean.getPreferenceKey().equalsIgnoreCase(editPrefKey)) {
                String defaultPreferenceValue = editPrefsBean
                        .getDefaultPreferenceValue();
                // initialize UserExtendedRoleBean
                UserExtendedRoleBean userExtendedRoleBean = PortletUtils
                        .getUserExtendedRoleBean();
                // get the extended role service group list
                List<String> extRoleServiceGroupList = userExtendedRoleBean
                        .getExtRoleServiceGroupList();
                if (!extRoleServiceGroupList.isEmpty()
                        && !extRoleServiceGroupList
                                .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
                        && !extRoleServiceGroupList
                                .contains(defaultPreferenceValue)) {
                    editPrefsBean
                            .setDefaultPreferenceValue(userExtendedRoleBean
                                    .getDefaultServiceGroup());
                    break;
                }
            }
        }
    }
}
