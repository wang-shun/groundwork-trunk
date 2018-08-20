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

package com.groundworkopensource.portal.statusviewer.common;

/**
 * This Object is used to convey JMS updates from one object to another
 * 
 * @author nitin_jadhav
 * 
 */
public class JMSUpdate {

    /**
     * Action that is to be taken on current object 
     */
    private String action;
    /**
     * Id of current object
     */
    private int id;
    /**
     * Node type of current Object
     */
    private NodeType nodeType;

    /**
     * @param action
     * @param id
     * @param nodeType
     */
    public JMSUpdate(String action, int id, NodeType nodeType) {
        this.action = action;
        this.id = id;
        this.nodeType = nodeType;
    }

    /**
     * Returns the action.
     * 
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action.
     * 
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns the id.
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the nodeType.
     * 
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Sets the nodeType.
     * 
     * @param nodeType
     *            the nodeType to set
     */
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return action + "_" + id + "_" + nodeType;
    }

}
