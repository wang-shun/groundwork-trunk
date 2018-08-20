package com.groundworkopensource.portal.statusviewer.common.actions;

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
    DOWNTIME("Downtime"),
    /**
     * Menu item Notifications
     */
    NOTIFICATIONS("Notifications"),
    /**
     * Menu item Settings
     */
    SETTINGS("Settings");

    /**
     * 
     * @param menuString
     */
    private ParentMenuActionsForGroup(String menuString) {
        this.menuString = menuString;
    }

    /**
     * String to be displayed as menu item on UI.
     */
    private String menuString;

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
}
