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

import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * This class contain previous start and end time for Perf Measurement portlet
 * to avoid multiple web service call .
 * 
 * @author manish_kjain
 * 
 */
public class PerfMeasurementTimeBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2737805046422341526L;
    /**
     * Start time in string
     */
    private String previousStartTime = Constant.EMPTY_STRING;
    /**
     * End time in String
     */
    private String previousEndTime = Constant.EMPTY_STRING;

    /**
     * Sets the previousStartTime.
     * 
     * @param previousStartTime
     *            the previousStartTime to set
     */
    public void setPreviousStartTime(String previousStartTime) {
        this.previousStartTime = previousStartTime;
    }

    /**
     * Returns the previousStartTime.
     * 
     * @return the previousStartTime
     */
    public String getPreviousStartTime() {
        return previousStartTime;
    }

    /**
     * Sets the previousEndTime.
     * 
     * @param previousEndTime
     *            the previousEndTime to set
     */
    public void setPreviousEndTime(String previousEndTime) {
        this.previousEndTime = previousEndTime;
    }

    /**
     * Returns the previousEndTime.
     * 
     * @return the previousEndTime
     */
    public String getPreviousEndTime() {
        return previousEndTime;
    }

}
