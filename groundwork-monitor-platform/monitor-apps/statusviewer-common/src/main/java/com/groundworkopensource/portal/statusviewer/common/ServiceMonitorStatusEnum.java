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
 * This Enumerator will enumerate all the states of service.
 * 
 * Important Note: DO NOT change the sequence in which the items are placed. The
 * severity based sorting is done on the basis of there place here.
 * 
 * @author manish_kjain
 * 
 */
public enum ServiceMonitorStatusEnum {

    /**
     * UNSCHEDULED CRITICAL
     */
    UNSCHEDULED_CRITICAL("UNSCHEDULED CRITICAL"),
    /**
     * CRITICAL
     */
    CRITICAL("CRITICAL"),
    /**
     * WARNING
     */
    WARNING("WARNING"),
    /**
     * PENDING
     */
    PENDING("PENDING"),
    /**
     * SCHEDULED CRITICAL
     */
    SCHEDULED_CRITICAL("SCHEDULED CRITICAL"),
    /**
     * UNKNOWN
     */
    UNKNOWN("UNKNOWN"),
    /**
     * OK
     */
    OK("OK"),

    /**
     * a non-standard status, for internal logic - NO_STATUS
     * 
     * Status not available or applicable
     */
    NO_STATUS("NO STATUS");

    /**
     * related monitor status for this state
     */
    private String monitorStatus;

    /**
     * @param monitorStatus
     */
    private ServiceMonitorStatusEnum(String monitorStatus) {
        this.setMonitorStatus(monitorStatus);
    }

    /**
     * Sets the monitorStatus.
     * 
     * @param monitorStatus
     *            the monitorStatus to set
     */
    public void setMonitorStatus(String monitorStatus) {
        this.monitorStatus = monitorStatus;
    }

    /**
     * Returns the monitorStatus.
     * 
     * @return the monitorStatus
     */
    public String getMonitorStatus() {
        return monitorStatus;
    }

}
