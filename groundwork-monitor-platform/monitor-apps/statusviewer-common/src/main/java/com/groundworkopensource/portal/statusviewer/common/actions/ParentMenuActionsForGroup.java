package com.groundworkopensource.portal.statusviewer.common.actions;

import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * This enum defines all the parent menu items to be displayed on actions
 * portlet UI for Host Group and Service Group context.
 * (Downtime,Notifications,Settings)
 * 
 * @author shivangi_walvekar
 * 
 */
public enum ParentMenuActionsForGroup {
    /**
     * Menu item Downtime
     */
    DOWNTIME("Downtime", Constant.APP_TYPE_NAGIOS),
    /**
     * Menu item Notifications
     */
    NOTIFICATIONS("Notifications", Constant.APP_TYPE_NAGIOS),
    /**
     * Menu item Settings
     */
    SETTINGS("Settings", Constant.APP_TYPE_NAGIOS);

    /**
     * 
     * @param menuString
     * @param applicationType
     */
    private ParentMenuActionsForGroup(String menuString, String applicationType) {
        this.menuString = menuString;
        this.applicationType = applicationType;
    }

    /**
     * String to be displayed as menu item on UI.
     */
    private String menuString;

    /**
     * Application type menu item is restricted to.
     */
    private String applicationType;

    /**
     * @return menuString - String to be displayed as menu item.
     */
    public String getMenuString() {
        return menuString;
    }

    /**
     * @param menuString
     */
    public void setMenuString(String menuString) {
        this.menuString = menuString;
    }

    /**
     * Get menu item application type.
     *
     * @return application type or null
     */
    public String getApplicationType() {
        return applicationType;
    }

    /**
     * Set menu item application type.
     *
     * @param applicationType application type or null
     */
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }
}
