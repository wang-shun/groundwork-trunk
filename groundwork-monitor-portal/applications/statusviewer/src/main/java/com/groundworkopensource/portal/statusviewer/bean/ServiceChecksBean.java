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
 * Bean representing service checks table data
 * 
 * @author mridu_narang
 * 
 */
public class ServiceChecksBean {

    /**
     * Name of check for which statistics are available
     */
    private final String nameOfCheck;

    /**
     * Returns the nameOfCheck.
     * 
     * @return the nameOfCheck
     */
    public String getNameOfCheck() {
        return this.nameOfCheck;
    }

    /**
     * Returns the fiveMinValue.
     * 
     * @return the fiveMinValue
     */
    public String getFiveMinValue() {
        return this.fiveMinValue;
    }

    /**
     * Returns the fifteenMinValue.
     * 
     * @return the fifteenMinValue
     */
    public String getFifteenMinValue() {
        return this.fifteenMinValue;
    }

    /**
     * Returns the sixtyMinValue.
     * 
     * @return the sixtyMinValue
     */
    public String getSixtyMinValue() {
        return this.sixtyMinValue;
    }

    /**
     * Minimum value for particular check
     */
    private final String fiveMinValue;

    /**
     * Average value for particular check
     */
    private final String fifteenMinValue;

    /**
     * Maximum value for particular check
     */
    private final String sixtyMinValue;

    /**
     * @param nameOfCheck
     * @param fiveMinValue
     * @param fifteenMinValue
     * @param sixtyMinValue
     */
    public ServiceChecksBean(String nameOfCheck, String fiveMinValue,
            String fifteenMinValue, String sixtyMinValue) {
        super();
        this.nameOfCheck = nameOfCheck;
        this.fiveMinValue = fiveMinValue;
        this.fifteenMinValue = fifteenMinValue;
        this.sixtyMinValue = sixtyMinValue;
    }
}
