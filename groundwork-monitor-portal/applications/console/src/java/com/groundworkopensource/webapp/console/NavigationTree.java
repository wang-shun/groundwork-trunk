/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

/**
 * The Interface NavigationTree.
 */
public interface NavigationTree {
	
	/**
     * Sets the selected node object.
     * 
     * @param selectedNodeObject
     *            the new selected node object
     */
	void setSelectedNodeObject(DynamicNodeUserObject selectedNodeObject);
	
	/**
     * Gets the selected node object.
     * 
     * @return the selected node object
     */
	DynamicNodeUserObject getSelectedNodeObject();

}
