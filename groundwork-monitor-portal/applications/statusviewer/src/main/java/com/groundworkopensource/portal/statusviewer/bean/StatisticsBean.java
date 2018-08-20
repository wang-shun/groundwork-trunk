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
public class StatisticsBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5190378196461979085L;
    /**
     * filtered Statistics.
     */
    private int filtered;
    /**
     * total Statistics
     */
    private long total;
    /**
     * status of host or host group or service or service group.
     */
    private String status;
    /**
     * status image src path.
     */
    private String imgsrc;

    /**
     * Returns the img src.
     * 
     * @return the imgsrc
     */
    public String getImgsrc() {
        return imgsrc;
    }

    /**
     * Sets the imgsrc.
     * 
     * @param imgsrc
     *            the imgsrc to set
     */
    public void setImgsrc(String imgsrc) {
        this.imgsrc = imgsrc;
    }

    /**
     * Returns the status.
     * 
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the filtered.
     * 
     * @return the filtered
     */
    public int getFiltered() {
        return filtered;
    }

    /**
     * Sets the filtered.
     * 
     * @param filtered
     *            the filtered to set
     */
    public void setFiltered(int filtered) {
        this.filtered = filtered;
    }

    /**
     * Returns the total.
     * 
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * Sets the total.
     * 
     * @param total
     *            the total to set
     */
    public void setTotal(long total) {
        this.total = total;
    }

}
