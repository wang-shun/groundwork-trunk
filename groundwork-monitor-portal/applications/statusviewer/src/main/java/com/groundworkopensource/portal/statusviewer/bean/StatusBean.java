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
 * This class is responsible for holding status and applied style class on column  
 * @author manish_kjain
 * 
 */
public class StatusBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 788347409298937531L;
    /**
     * name of style class
     */
    private String styleClass;
    /**
     * value of status i.e."warning",critical
     */
    private String value;

    /**
     * Returns the styleClass.
     * 
     * @return the styleClass
     */
    public String getStyleClass() {
        return styleClass;
    }

    /**
     * Sets the styleClass.
     * 
     * @param styleClass
     *            the styleClass to set
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
