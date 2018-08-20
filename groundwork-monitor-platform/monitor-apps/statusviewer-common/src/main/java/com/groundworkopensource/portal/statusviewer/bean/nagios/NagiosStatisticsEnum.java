package com.groundworkopensource.portal.statusviewer.bean.nagios;

import static com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants.IMAGE_PATH_HOST_GREEN;
import static com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants.IMAGE_PATH_HOST_RED;

/**
 * Represents Nagios Statistics states for HostGroup,ServiceGroup or the entire
 * network
 * 
 * @author shivangi_walvekar
 * 
 */
public enum NagiosStatisticsEnum {

    // TODO - Make the icon paths nagios specific.

    /**
     * Enum for Disabled state of HostGroup,ServiceGroup or the entire network
     */
    DISABLED(IMAGE_PATH_HOST_RED, "Disabled"),

    /**
     * Enum for Enabled state of HostGroup,ServiceGroup or the entire network
     */
    ENABLED(IMAGE_PATH_HOST_GREEN, "Enabled"),

    /**
     * Enum for X number of disabled hosts
     */
    DISABLED_HOSTS(IMAGE_PATH_HOST_GREEN, "Disabled Hosts"),

    /**
     * Enum for X number of disabled services
     */
    DISABLED_SERVICES(IMAGE_PATH_HOST_GREEN, "Disabled Services"),

    /**
     * Enum for all services enabled
     */
    ALL_SERVICES_ENABLED(IMAGE_PATH_HOST_GREEN, "All Services Enabled"),

    /**
     * Enum for all hosts enabled
     */
    ALL_HOSTS_ENABLED(IMAGE_PATH_HOST_GREEN, "All Hosts Enabled");

    /**
     * String property for the icon path to be displayed for a particular
     * status.
     */
    private String iconPath;

    /**
     * String property for the status.
     */
    private String status;

    /**
     * Constructor
     * 
     * @param iconPath
     * @param status
     */
    NagiosStatisticsEnum(String iconPath, String status) {
        this.iconPath = iconPath;
        this.status = status;
    }

    /**
     * @return iconPath
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * @param iconPath
     */
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
