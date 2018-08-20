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

/**
 * Bean representing performance statistics table data
 * 
 * @author mridu_narang
 * 
 */
public class PerformanceStatisticsBean {

    /**
     * 
     * @param nameOfCheck
     * @param minValue
     * @param avgValue
     * @param maxValue
     */
    public PerformanceStatisticsBean(String nameOfCheck, String minValue,
            String avgValue, String maxValue) {
        super();
        this.nameOfCheck = nameOfCheck;
        this.minValue = minValue;
        this.avgValue = avgValue;
        this.maxValue = maxValue;
    }

    /**
     * Name of check for which statistics are available
     */
    private final String nameOfCheck;

    /**
     * Minimum value for particular check
     */
    private final String minValue;

    /**
     * Average value for particular check
     */
    private final String avgValue;

    /**
     * Maximum value for particular check
     */
    private final String maxValue;

    /**
     * Returns the nameOfCheck.
     * 
     * @return the nameOfCheck
     */
    public String getNameOfCheck() {
        return this.nameOfCheck;
    }

    /**
     * Returns the minValue.
     * 
     * @return the minValue
     */
    public String getMinValue() {
        return this.minValue;
    }

    /**
     * Returns the avgValue.
     * 
     * @return the avgValue
     */
    public String getAvgValue() {
        return this.avgValue;
    }

    /**
     * Returns the maxValue.
     * 
     * @return the maxValue
     */
    public String getMaxValue() {
        return this.maxValue;
    }

}
