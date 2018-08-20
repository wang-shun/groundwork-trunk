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

/**
 * This Enumerator will enumerate all the states of Host in seurat view, and map
 * states to its icon. It includes the Recently recovered and troubled services
 * states.
 * 
 * Important Note: DO NOT change the sequence in which the items are placed. The
 * severity based sorting is done on the basis of there place here.
 * 
 * @author nitin_jadhav
 */
public enum SeuratStatusEnum {
    /**
     * Unscheduled down host
     */
    SEURAT_HOST_DOWN_UNSCHEDULED("UNSCHEDULED DOWN",
            "/images/seurat_legend_red.gif",
            "/images/seurat_legend_red_blink.gif"),

    /**
     * Host unreachable
     */
    SEURAT_HOST_UNREACHABLE("UNREACHABLE", "/images/seurat_legend_gray.gif",
            "/images/seurat_legend_gray_blink.gif"),

    /**
     * a non-standard status, for internal logic - Host with 76% - 100% troubled
     * services
     */
    SEURAT_HOST_TROUBLED_100P("SEURAT HOST TROUBLED 100P",
            "/images/seurat_legend_yellow_4.gif", 76, 100,
            "/images/seurat_legend_yellow4_blink.gif"),
    /**
     * a non-standard status, for internal logic - Host with 51% - 75% troubled
     * services
     */
    SEURAT_HOST_TROUBLED_75P("SEURAT HOST TROUBLED 75P",
            "/images/seurat_legend_yellow_3.gif", 51, 75,
            "/images/seurat_legend_yellow3_blink.gif"),
    /**
     * a non-standard status, for internal logic - Host with 26% - 50% troubled
     * services
     */
    SEURAT_HOST_TROUBLED_50P("SEURAT HOST TROUBLED 50P",
            "/images/seurat_legend_yellow_2.gif", 26, 50,
            "/images/seurat_legend_yellow2_blink.gif"),
    /**
     * a non-standard status, for internal logic - Host with up to 25% troubled
     * services
     */
    SEURAT_HOST_TROUBLED_25P("SEURAT HOST TROUBLED 25P",
            "/images/seurat_legend_yellow_1.gif", 1, 25,
            "/images/seurat_legend_yellow1_blink.gif"),

    /**
     * Scheduled down host
     */
    SEURAT_HOST_DOWN_SCHEDULED("SCHEDULED DOWN",
            "/images/seurat_legend_orange.gif",
            "/images/seurat_legend_orange_blink.gif"),

    /**
     * Host pending
     */
    SEURAT_HOST_PENDING("PENDING", "/images/seurat_legend_blue.gif",
            "/images/seurat_legend_blue.gif"),

    /**
     * a non-standard status, for internal logic - recently recovered host,
     * which is currently up and changed state in last n minutes
     */
    SEURAT_HOST_RECENTLY_RECOVERED("RECENTLY RECOVERED",
            "/images/seurat_legend_green.gif",
            "/images/seurat_legend_green_blink.gif"),

    /**
     * Host up
     */
    SEURAT_HOST_UP("UP", "/images/seurat_legend_white.gif",
            "/images/seurat_legend_white.gif"),
            
    /**
     * Host up
     */
    SEURAT_HOST_SUSPENDED("SUSPENDED", "/images/seurat_legend_white.gif",
            "/images/seurat_legend_white.gif"),


    /**
     * a non-standard status, for internal logic - NO_STATUS
     * 
     * Status not available or applicable
     */
    NO_STATUS(null, null, null);

    /**
     * Path of icon that represents this state
     */
    private String iconPath;

    /**
     * related monitor status for this state
     */
    private String monitorStatus;

    /**
     * Path of icon that represents this state
     */
    private String blinkIconPath;

    /**
     * Only applicable to hosts which are up. Minimum % of services the host
     * should have to qualify for this status
     */
    private int serviceLowerBound;
    /**
     * Only applicable to hosts which are up. Maximum % of services the host
     * should have to qualify for this status
     */
    private int serviceUpperBound;

    /**
     * Constructor
     * 
     * @param monitorStatus
     * @param iconPath
     * @param blinkIconPath
     */
    private SeuratStatusEnum(String monitorStatus, String iconPath,
            String blinkIconPath) {
        this.monitorStatus = monitorStatus;
        this.iconPath = iconPath;
        this.blinkIconPath = blinkIconPath;
    }

    /**
     * Constructor
     * 
     * @param serviceLowerBound
     * @param serviceUpperBound
     * @param iconPath
     * @param blinkIconPath
     */
    private SeuratStatusEnum(String monitorStatus, String iconPath,
            int serviceLowerBound, int serviceUpperBound, String blinkIconPath) {
        this.monitorStatus = monitorStatus;
        this.iconPath = iconPath;
        this.serviceLowerBound = serviceLowerBound;
        this.serviceUpperBound = serviceUpperBound;
        this.blinkIconPath = blinkIconPath;
    }

    /**
     * Returns the serviceLowerBound.
     * 
     * @return the serviceLowerBound
     */
    public int getServiceLowerBound() {
        return serviceLowerBound;
    }

    /**
     * Sets the serviceLowerBound.
     * 
     * @param serviceLowerBound
     *            the serviceLowerBound to set
     */
    public void setServiceLowerBound(int serviceLowerBound) {
        this.serviceLowerBound = serviceLowerBound;
    }

    /**
     * Returns the serviceUpperBound.
     * 
     * @return the serviceUpperBound
     */
    public int getServiceUpperBound() {
        return serviceUpperBound;
    }

    /**
     * Sets the serviceUpperBound.
     * 
     * @param serviceUpperBound
     *            the serviceUpperBound to set
     */
    public void setServiceUpperBound(int serviceUpperBound) {
        this.serviceUpperBound = serviceUpperBound;
    }

    /**
     * Returns the monitorStatus.
     * 
     * @return the monitorStatus
     */
    public String getMonitorStatus() {
        return monitorStatus;
    }

    /**
     * Returns the iconPath.
     * 
     * @return the iconPath
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * Returns the blinkIconPath.
     * 
     * @return the blinkIconPath
     */
    public String getBlinkIconPath() {
        return blinkIconPath;
    }

}
