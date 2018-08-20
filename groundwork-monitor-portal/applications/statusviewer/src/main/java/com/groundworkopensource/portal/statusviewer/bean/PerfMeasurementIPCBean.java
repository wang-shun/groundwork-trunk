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

package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;

/**
 * @author manish_kjain
 * 
 */
public class PerfMeasurementIPCBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1434061124860922942L;
    /**
     * PerfMeasurement graph start date
     */
    private String startDate;
    /**
     * PerfMeasurement graph end date
     */
    private String endDate;
    /**
     * chart width
     */
    private int width;

    /**
     * Sets the startDate.
     * 
     * @param startDate
     *            the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the startDate.
     * 
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the endDate.
     * 
     * @param endDate
     *            the endDate to set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the endDate.
     * 
     * @return the endDate
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the width.
     * 
     * @param width
     *            the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the width.
     * 
     * @return the width
     */
    public int getWidth() {
        return width;
    }

}
