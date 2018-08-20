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
 * Back end bean for holding Rrg graph byes.
 * 
 * @author manish_kjain
 * 
 */
public class RrdGraphBean {

    /**
     * byte array contain rrd byte array
     */
    private byte[] rrdGraphBytes;
    /**
     * title of panel collapsible
     */
    private String collapsibleTitle;
    /**
     * expanded
     */
    private boolean expanded;

    /**
     * Sets the rrdGraphBytes.
     * 
     * @param rrdGraphBytes
     *            the rrdGraphBytes to set
     */
    public void setRrdGraphBytes(byte[] rrdGraphBytes) {
        this.rrdGraphBytes = rrdGraphBytes;
    }

    /**
     * Returns the rrdGraphBytes.
     * 
     * @return the rrdGraphBytes
     */
    public byte[] getRrdGraphBytes() {
        return rrdGraphBytes;
    }

    /**
     * Sets the collapsibleTitle.
     * 
     * @param collapsibleTitle
     *            the collapsibleTitle to set
     */
    public void setCollapsibleTitle(String collapsibleTitle) {
        this.collapsibleTitle = collapsibleTitle;
    }

    /**
     * Returns the collapsibleTitle.
     * 
     * @return the collapsibleTitle
     */
    public String getCollapsibleTitle() {
        return collapsibleTitle;
    }

    /**
     * Sets the expanded.
     * 
     * @param expanded
     *            the expanded to set
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * Returns the expanded.
     * 
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

}
