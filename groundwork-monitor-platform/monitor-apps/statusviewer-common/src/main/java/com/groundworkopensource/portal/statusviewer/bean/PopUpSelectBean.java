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

import javax.faces.event.ValueChangeEvent;

/**
 * This class provide the current select in model pop .
 * 
 * @author manish_kjain
 * 
 */
public class PopUpSelectBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -285622337222121693L;
    /**
     * current select value
     */
    private String selectValue;

    /**
     * current select value for host
     */
    private String hostSelectValue;
    /**
     * current select value for host Group
     */
    private String hgSelectValue;
    /**
     * current select value for host Group
     */
    private String sgSelectValue;
    /**
     * current select value for host Group
     */
    private String serviceSelectValue;

    /**
     * Returns the selectValue.
     * 
     * @return the selectValue
     */
    public String getSelectValue() {
        return selectValue;
    }

    /**
     * Sets the selectValue.
     * 
     * @param selectValue
     *            the selectValue to set
     */
    public void setSelectValue(String selectValue) {
        this.selectValue = selectValue;
    }

    /**
     * @param event
     */
    public void processMenuSelection(ValueChangeEvent event) {

        String newVal = (String) event.getNewValue();
        setSelectValue(newVal);
    }

    /**
     * @param event
     */
    public void hostProcessMenuSelection(ValueChangeEvent event) {

        String newVal = (String) event.getNewValue();
        setHostSelectValue(newVal);
    }

    /**
     * Sets the hostSelectValue.
     * 
     * @param hostSelectValue
     *            the hostSelectValue to set
     */
    public void setHostSelectValue(String hostSelectValue) {
        this.hostSelectValue = hostSelectValue;
    }

    /**
     * Returns the hostSelectValue.
     * 
     * @return the hostSelectValue
     */
    public String getHostSelectValue() {
        return hostSelectValue;
    }

    /**
     * Sets the hgSelectValue.
     * 
     * @param hgSelectValue
     *            the hgSelectValue to set
     */
    public void setHgSelectValue(String hgSelectValue) {
        this.hgSelectValue = hgSelectValue;
    }

    /**
     * Returns the hgSelectValue.
     * 
     * @return the hgSelectValue
     */
    public String getHgSelectValue() {
        return hgSelectValue;
    }

    /**
     * @param event
     */
    public void hgProcessMenuSelection(ValueChangeEvent event) {

        String newVal = (String) event.getNewValue();
        setHgSelectValue(newVal);
    }

    /**
     * Sets the sgSelectValue.
     * 
     * @param sgSelectValue
     *            the sgSelectValue to set
     */
    public void setSgSelectValue(String sgSelectValue) {
        this.sgSelectValue = sgSelectValue;
    }

    /**
     * Returns the sgSelectValue.
     * 
     * @return the sgSelectValue
     */
    public String getSgSelectValue() {
        return sgSelectValue;
    }

    /**
     * Sets the serviceSelectValue.
     * 
     * @param serviceSelectValue
     *            the serviceSelectValue to set
     */
    public void setServiceSelectValue(String serviceSelectValue) {
        this.serviceSelectValue = serviceSelectValue;
    }

    /**
     * Returns the serviceSelectValue.
     * 
     * @return the serviceSelectValue
     */
    public String getServiceSelectValue() {
        return serviceSelectValue;
    }

    /**
     * @param event
     */
    public void sgProcessMenuSelection(ValueChangeEvent event) {

        String newVal = (String) event.getNewValue();
        setSgSelectValue(newVal);
    }

    /**
     * @param event
     */
    public void servicesProcessMenuSelection(ValueChangeEvent event) {

        String newVal = (String) event.getNewValue();
        setServiceSelectValue(newVal);
    }

}
