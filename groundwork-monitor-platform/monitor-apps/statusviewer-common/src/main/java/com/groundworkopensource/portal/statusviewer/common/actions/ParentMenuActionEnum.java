package com.groundworkopensource.portal.statusviewer.common.actions;

import com.groundworkopensource.portal.statusviewer.common.Constant;

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
    ACKNOWLEDGE("Acknowledge", Constant.APP_TYPE_NAGIOS),
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
    SETTINGS("Settings", Constant.APP_TYPE_NAGIOS),
    /**
     * Menu item Event Handlers
     */
    EVENT_HANDLERS("Event Handlers", Constant.APP_TYPE_NAGIOS),
    /**
     * Menu item Check Results
     */
    CHECK_RESULTS("Check Results", Constant.APP_TYPE_NAGIOS),
    /**
     * Menu item connection
     */
    CONNECTIONS("Connections", null);

    // /**
    // * Logger
    // */
    // private static Logger logger = Logger
    // .getLogger(ParentMenuActionEnum.class.getName());

    /**
     * 
     * @param menuString
     * @param applicationType
     */
    private ParentMenuActionEnum(String menuString, String applicationType) {
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
