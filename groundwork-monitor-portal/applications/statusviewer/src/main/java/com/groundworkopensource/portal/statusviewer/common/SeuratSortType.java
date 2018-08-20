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

import com.groundworkopensource.portal.common.ResourceUtils;

/**
 * @author nitin_jadhav
 * 
 *         All available options for sorting
 */
public enum SeuratSortType {
    /**
     * Sort in alphabetic sequence
     */
    ALPHA(
            ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_dashboard_sortOptions_HostName")),
    /**
     * Sort according to severity sequence
     */
    SEVERITY(
            ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_dashboard_sortOptions_status")),
    /**
     * Sort according to last state change time, in which host with the most
     * recent service problems is shifted to the top left.
     */
    STATE_CHANGE(
            ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_dashboard_sortOptions_lastStateChange"));

    /**
     * This is used as text for displaying sorting options on UI
     */
    private String optionName;

    /**
     * Constructor
     * 
     * @param optionName
     */
    private SeuratSortType(String optionName) {
        this.optionName = optionName;
    }

    /**
     * Returns the optionName.
     * 
     * @return the optionName
     */
    public String getOptionName() {
        return optionName;
    }
}