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

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

public class PublicFilterTreeBean implements NavigationTree {
	// tree default model, used as a value for the tree component
	private DefaultTreeModel model;

	// object reference used to delete and copy the node
	private DynamicNodeUserObject selectedNodeObject = null;

	public static Logger logger = Logger.getLogger(PublicFilterTreeBean.class
			.getName());

	public PublicFilterTreeBean() {
		 // create root node with its children expanded
        DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
        DynamicNodeUserObject rootObject = new DynamicNodeUserObject(
				rootTreeNode, this);
        rootObject.setText(ConsoleConstants.FILTER_EVENTS);
        rootObject.setExpanded(true);
        // rootObject.setLeafIcon(ConsoleConstants.BRANCH_CONTRACTED_ICON);
        rootObject.setBranchContractedIcon(ConsoleConstants.BRANCH_CONTRACTED_ICON);
		rootObject.setBranchExpandedIcon(ConsoleConstants.BRANCH_EXPANDED_ICON);
        rootObject.setStyleClass(ConsoleConstants.STYLE_NONE);
        rootTreeNode.setUserObject(rootObject);
        PublicFiltersConfigBean configBean = ConsoleHelper.getPublicFilters();
        if (configBean != null)
        {
        	Vector<FilterConfigBean> filters =  configBean.getFilterConfigs();
            
            for (int i=0;i<filters.size();i++)
            {
            	FilterConfigBean filter = filters.get(i);
            	// addNode(rootTreeNode,ConsoleConstants.PUBLIC_FILTER_CRITICAL_EVENTS);
            	addNode(rootTreeNode,filter.getLabel());
            }
        } // end if
        
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

	private void addNode(DefaultMutableTreeNode rootTreeNode, String nodeName) {
		DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(
				rootTreeNode);
		DynamicNodeUserObject branchObject = new DynamicNodeUserObject(
				branchNode, this);
		branchObject.setText(nodeName);
		branchObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		branchObject.setLeaf(true);
		branchObject.setLeafIcon(ConsoleConstants.LEAF_ICON);
		branchNode.setUserObject(branchObject);
		rootTreeNode.add(branchNode);
	}

	public DynamicNodeUserObject getSelectedNodeObject() {
		return selectedNodeObject;
	}

	public void setSelectedNodeObject(DynamicNodeUserObject selectedNodeObject) {
		this.selectedNodeObject = selectedNodeObject;
	}
}
