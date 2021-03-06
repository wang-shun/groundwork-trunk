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
 * UIEventsServerPush - a class that should be extended by Tree View
 * (NetworkTreeBean). ReferenceTreeMEtaModel will be responsible for publishing
 * message into this topic.
 * 
 * @author swapnil_gujrathi
 * 
 */
public abstract class UIEventsServerPush extends ServerPush {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public UIEventsServerPush() {
        super("ui.events.topic.name");
    }

}
