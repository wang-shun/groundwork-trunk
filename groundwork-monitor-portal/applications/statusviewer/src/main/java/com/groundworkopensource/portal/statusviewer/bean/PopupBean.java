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
 * 
 * 
 * This Class used to allow the dynamic opening and closing of panelPopups.
 * 
 * @author manish_kjain
 */

public class PopupBean implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6923010188397178438L;
    /**
     * boolean variable to used close and open model popup window
     */
    private boolean visible = false;

    /**
     * @return boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * 
     */
    public void closePopup() {
        visible = false;

    }

    /**
     * 
     */
    public void openPopup() {
        visible = true;

    }
}