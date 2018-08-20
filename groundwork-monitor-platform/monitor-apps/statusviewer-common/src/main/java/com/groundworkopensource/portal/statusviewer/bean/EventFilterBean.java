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

import org.groundwork.foundation.ws.model.impl.Filter;

import com.groundworkopensource.portal.common.CommonConstants;

/**
 * @author manish_kjain
 * 
 */
public class EventFilterBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2222082518843464892L;

    /**
     * Filter instance variable to hold current filter
     */
    private Filter filter;

    /**
     * previous Selected HostFilter Name
     */
    private String previousSelectedHostFilterName;
    /**
     * previous Selected Service Filter Name
     */
    private String previousSelectedServiceFilterName;

    /**
     * Constructor
     */
    public EventFilterBean() {

        this.previousSelectedHostFilterName = CommonConstants.DEFAULT_FILTER;
        this.previousSelectedServiceFilterName = CommonConstants.DEFAULT_FILTER;
    }

    /**
     * Sets the filter.
     * 
     * @param filter
     *            the filter to set
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Returns the filter.
     * 
     * @return the filter
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets the previousSelectedHostFilterName.
     * 
     * @param previousSelectedHostFilterName
     *            the previousSelectedHostFilterName to set
     */
    public void setPreviousSelectedHostFilterName(
            String previousSelectedHostFilterName) {
        this.previousSelectedHostFilterName = previousSelectedHostFilterName;
    }

    /**
     * Returns the previousSelectedHostFilterName.
     * 
     * @return the previousSelectedHostFilterName
     */
    public String getPreviousSelectedHostFilterName() {
        return previousSelectedHostFilterName;
    }

    /**
     * Sets the previousSelectedServiceFilterName.
     * 
     * @param previousSelectedServiceFilterName
     *            the previousSelectedServiceFilterName to set
     */
    public void setPreviousSelectedServiceFilterName(
            String previousSelectedServiceFilterName) {
        this.previousSelectedServiceFilterName = previousSelectedServiceFilterName;
    }

    /**
     * Returns the previousSelectedServiceFilterName.
     * 
     * @return the previousSelectedServiceFilterName
     */
    public String getPreviousSelectedServiceFilterName() {
        return previousSelectedServiceFilterName;
    }

}
