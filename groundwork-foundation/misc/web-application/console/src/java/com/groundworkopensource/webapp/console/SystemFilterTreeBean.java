/*
* 
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/
package com.groundworkopensource.webapp.console;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

public class SystemFilterTreeBean implements NavigationTree{
	// tree default model, used as a value for the tree component
	private DefaultTreeModel model;
	
	// object reference used to delete and copy the node
	private DynamicNodeUserObject selectedNodeObject = null;
	
	public static Logger logger = Logger.getLogger(SystemFilterTreeBean.class.getName());

	public SystemFilterTreeBean() {
		// create root node with its children expanded
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
		DynamicNodeUserObject rootObject = new DynamicNodeUserObject(
				rootTreeNode, this);
		rootObject.setText(ConsoleConstants.FILTER_EVENTS);
		rootObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		rootObject.setExpanded(true);
		rootObject.setBranchContractedIcon(ConsoleConstants.BRANCH_CONTRACTED_ICON);
		rootObject.setBranchExpandedIcon(ConsoleConstants.BRANCH_EXPANDED_ICON);
		rootTreeNode.setUserObject(rootObject);
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_APPLICATIONS);
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS);
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS);
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS);

		// model is accessed by by the ice:tree component
		model = new DefaultTreeModel(rootTreeNode);
	}

	/**
	 * Gets the tree's default model.
	 * 
	 * @return tree model.
	 */
	public DefaultTreeModel getModel() {
		return model;
	}

	/**
	 * 
	 * @param rootTreeNode
	 * @param nodeName
	 */
	private void addNode(DefaultMutableTreeNode rootTreeNode, String nodeName) {
		DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(
				rootTreeNode);
		DynamicNodeUserObject branchObject = new DynamicNodeUserObject(
				branchNode, this);
		branchObject.setText(nodeName);
		//branchObject.setLeaf(true);
		branchObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		branchObject.setBranchContractedIcon(ConsoleConstants.BRANCH_CONTRACTED_ICON);
		branchObject.setBranchExpandedIcon(ConsoleConstants.BRANCH_EXPANDED_ICON);
		if (nodeName.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_APPLICATIONS))
			branchObject.addApplications(null);
		if (nodeName.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS))
			branchObject.addHostGroup(null);
		if (nodeName.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS))
			branchObject.addServiceGroup(null);
		if (nodeName.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS))
			branchObject.addOperationStatus(null);
		branchNode.setUserObject(branchObject);
		rootTreeNode.add(branchNode);
		
	}

	/**
	 * Sets the tree node.
	 *
	 * @param selectedNodeObject the new tree node
	 */
	public void setSelectedNodeObject(DynamicNodeUserObject selectedNodeObject) {
		this.selectedNodeObject = selectedNodeObject;
	}

	/**
	 * Gets the tree node.
	 *
	 * @return the tree node
	 */
	public DynamicNodeUserObject getSelectedNodeObject() {
		return selectedNodeObject;
	}

}
