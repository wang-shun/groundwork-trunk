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
 * This class provide the date to pop up window.
 * 
 * @author manish_kjain
 * 
 */
public class ModelPopUpDataBean {

    /**
     * host or service or service group or service name.
     */
    private String name;

    /**
     * host or service or service group or service name.
     */
    private String parentName;

    /**
     * host or service is acknowledged.
     */
    private String acknowledged;
    /**
     * host or service or service group or service last check date time.
     */
    private String datetime;

    /**
     * property for path of icon
     */
    private String iconPath;
    /**
     * sub page url String
     */
    private String subPageURL;
    /**
     * parent sub page url String
     */
    private String parentPageURL;
    /**
     * totalCount of ModelPopUpData
     */
    private int totalCount;

    /**
     * @return iconPath
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * @param iconPath
     */
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * data table sorting column name.
     */
    private String sortColumnName;

    /**
     */
    public ModelPopUpDataBean() {

    }

    /**
     * 
     */
    public void init() {

    }

    /**
     * data table ascending.
     */
    private boolean ascending = false;

    /**
     * Returns the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the acknowledged.
     * 
     * @return the acknowledged
     */
    public String getAcknowledged() {
        return acknowledged;
    }

    /**
     * Sets the acknowledged.
     * 
     * @param acknowledged
     *            the acknowledged to set
     */
    public void setAcknowledged(String acknowledged) {
        this.acknowledged = acknowledged;
    }

    /**
     * Returns the datetime.
     * 
     * @return the datetime
     */
    public String getDatetime() {
        return datetime;
    }

    /**
     * Sets the datetime.
     * 
     * @param datetime
     *            the datetime to set
     */
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    /**
     * Sets the sortColumnName.
     * 
     * @param sortColumnName
     *            the sortColumnName to set
     */
    public void setSortColumnName(String sortColumnName) {
        this.sortColumnName = sortColumnName;
    }

    /**
     * Returns the sortColumnName.
     * 
     * @return the sortColumnName
     */
    public String getSortColumnName() {
        return sortColumnName;
    }

    /**
     * Sets the ascending.
     * 
     * @param ascending
     *            the ascending to set
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Returns the ascending.
     * 
     * @return the ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the parentName.
     * 
     * @param parentName
     *            the parentName to set
     */
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    /**
     * Returns the parentName.
     * 
     * @return the parentName
     */
    public String getParentName() {
        return parentName;
    }

    /**
     * Sets the subPageURL.
     * 
     * @param subPageURL
     *            the subPageURL to set
     */
    public void setSubPageURL(String subPageURL) {
        this.subPageURL = subPageURL;
    }

    /**
     * Returns the subPageURL.
     * 
     * @return the subPageURL
     */
    public String getSubPageURL() {
        return subPageURL;
    }

    /**
     * Sets the parentPageURL.
     * 
     * @param parentPageURL
     *            the parentPageURL to set
     */
    public void setParentPageURL(String parentPageURL) {
        this.parentPageURL = parentPageURL;
    }

    /**
     * Returns the parentPageURL.
     * 
     * @return the parentPageURL
     */
    public String getParentPageURL() {
        return parentPageURL;
    }

    /**
     * Sets the totalCount.
     * 
     * @param totalCount
     *            the totalCount to set
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Returns the totalCount.
     * 
     * @return the totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }

}
