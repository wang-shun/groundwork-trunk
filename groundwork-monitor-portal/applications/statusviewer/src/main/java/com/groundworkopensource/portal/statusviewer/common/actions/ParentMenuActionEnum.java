package com.groundworkopensource.portal.statusviewer.common.actions;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum defines all the parent menu items to be displayed on actions
 * portlet UI for Host and services context.
 * (Acknowledge,Downtime,Notifications,Settings,EventHandlers,CheckResults)
 * 
 * @author shivangi_walvekar
 * 
 */
public enum ParentMenuActionEnum {
    /**
     * Menu item Acknowledge
     */
    ACKNOWLEDGE("Acknowledge"),
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
    SETTINGS("Settings"),
    /**
     * Menu item Event Handlers
     */
    EVENT_HANDLERS("Event Handlers"),
    /**
     * Menu item Check Results
     */
    CHECK_RESULTS("Check Results"),
    /**
     * Menu item connection
     */
    CONNECTIONS("Connections");

    // /**
    // * Logger
    // */
    // private static Logger logger = Logger
    // .getLogger(ParentMenuActionEnum.class.getName());

    /**
     * 
     * @param menuString
     */
    private ParentMenuActionEnum(String menuString) {
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

    /**
     * Map for retrieving parent menu status.
     */
    private static Map<String, ParentMenuActionEnum> parentMenuMap;
    // statically initialize entity-status map.
    static {
        parentMenuMap = new HashMap<String, ParentMenuActionEnum>() {
            /**
             * Serial Id
             */
            private static final long serialVersionUID = 1L;
            // Put All states in entity status map
            {
                ParentMenuActionEnum[] menuValues = ParentMenuActionEnum
                        .values();
                for (ParentMenuActionEnum menuValue : menuValues) {
                    put(menuValue.getMenuString(), menuValue);
                }
            }
        };
    } // end of static block

    /**
     * Returns ParentMenuActionEnum from menu string passed.
     * 
     * @param menuString
     * @return parentMenu
     * 
     * 
     */
    public static ParentMenuActionEnum getParentMenuActionEnum(String menuString) {
        ParentMenuActionEnum parentMenu = parentMenuMap.get(menuString);
        if (parentMenu == null) {
            // logger.warn("Unknown node status encountered:" + menuString);
            // Returning Acknowledge menu as default. This case will never occur
            // .
            return ParentMenuActionEnum.ACKNOWLEDGE;
        }
        return parentMenu;

    }
}
