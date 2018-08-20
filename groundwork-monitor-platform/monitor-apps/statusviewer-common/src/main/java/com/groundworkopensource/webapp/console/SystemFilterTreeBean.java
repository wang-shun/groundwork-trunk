/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import com.groundworkopensource.portal.common.FacesUtils;

import org.apache.log4j.Logger;

import com.icesoft.faces.async.render.SessionRenderer;

/**
 * The Class SystemFilterTreeBean.
 */
public class SystemFilterTreeBean extends ServerPush implements NavigationTree {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2655204440333444380L;

	// tree default model, used as a value for the tree component
	/** The model. */
	private DefaultTreeModel model;

	// object reference used to delete and copy the node
	/** The selected node object. */
	private DynamicNodeUserObject selectedNodeObject = null;

	/** The logger. */
	public static Logger logger = Logger.getLogger(SystemFilterTreeBean.class
			.getName());

	/** JMS Publisher COnstant for - HOSTGROUP. */
	private static final String HOSTGROUP = "HOSTGROUP";

	/** JMS Publisher Constant for - HOST. */
	private static final String HOST = "HOST";

	/** JMS Publisher COnstant for - SERVICEGROUP. */
	private static final String SERVICEGROUP = "SERVICEGROUP";

	/** JMS Publisher COnstant for - SERVICE. */
	private static final String SERVICESTATUS = "SERVICESTATUS";

	/**
	 * Instantiates a new system filter tree bean.
	 */
	public SystemFilterTreeBean() {
		// create root node with its children expanded
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
		DynamicNodeUserObject rootObject = new DynamicNodeUserObject(
				rootTreeNode, this);
		rootObject.setText(ConsoleConstants.FILTER_EVENTS);
		rootObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		rootObject.setExpanded(true);
		rootObject
				.setBranchContractedIcon(ConsoleConstants.BRANCH_CONTRACTED_ICON);
		rootObject.setBranchExpandedIcon(ConsoleConstants.BRANCH_EXPANDED_ICON);
		rootTreeNode.setUserObject(rootObject);
		long start = System.currentTimeMillis();
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_APPLICATIONS);
		long end = System.currentTimeMillis();

		logger.debug("Time to build applications tree " + (end - start) + " ms");
		start = System.currentTimeMillis();
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS);
		end = System.currentTimeMillis();

		logger.debug("Time to build hostgroups tree " + (end - start) + " ms");
		start = System.currentTimeMillis();
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS);
		end = System.currentTimeMillis();
		logger.debug("Time to build servicegroups tree " + (end - start)
				+ " ms");
		start = System.currentTimeMillis();
		addNode(rootTreeNode, ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS);
		end = System.currentTimeMillis();
		logger.debug("Time to build operation status tree " + (end - start)
				+ " ms");

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
	 * Adds the node.
	 * 
	 * @param rootTreeNode
	 *            the root tree node
	 * @param nodeName
	 *            the node name
	 */
	private void addNode(DefaultMutableTreeNode rootTreeNode, String nodeName) {
		DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(
				rootTreeNode);
		DynamicNodeUserObject branchObject = new DynamicNodeUserObject(
				branchNode, this);
		branchObject.setText(nodeName);
		// branchObject.setLeaf(true);
		branchObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		branchObject
				.setBranchContractedIcon(ConsoleConstants.BRANCH_CONTRACTED_ICON);
		branchObject
				.setBranchExpandedIcon(ConsoleConstants.BRANCH_EXPANDED_ICON);
		if (nodeName
				.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_APPLICATIONS))
			branchObject.addApplications(null);
		if (nodeName
				.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
			// check if this call is from external, if so then expand the hostgroup branch. Descendent node expansion is taken care in
			// DynamicNodeUserObject
			String viewparam = (String)FacesUtils.getPortletSession(false).getAttribute(ConsoleConstants.GWOS_CONSOLE_VIEWPARAM);
			if (viewparam != null) {
				branchObject.setExpanded(true);
			}
			branchObject.addHostGroup(null);
		}
		if (nodeName
				.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
			// check if this call is from external, if so then expand the hostgroup branch. Descendent node expansion is taken care in
			// DynamicNodeUserObject
			String viewparam = (String)FacesUtils.getPortletSession(false).getAttribute(ConsoleConstants.GWOS_CONSOLE_VIEWPARAM);
			if (viewparam != null) {
				branchObject.setExpanded(true);
			}
			branchObject.addServiceGroup(null);
		}
		if (nodeName
				.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS))
			branchObject.addOperationStatus(null);
		branchNode.setUserObject(branchObject);
		rootTreeNode.add(branchNode);

	}

	/**
	 * Sets the tree node.
	 * 
	 * @param selectedNodeObject
	 *            the new tree node
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

	// public void onMessage(Message msg) {
	// try {
	// if (msg != null) {
	// TextMessage txtMsg = (TextMessage) msg;
	// String xml = txtMsg.getText();
	// if (xml != null) {
	// if (xml.indexOf(HOSTGROUP) > 1) {
	// logger.info("Refreshing HostGroup....");
	// this.refreshHostGroup();
	// SessionRenderer.render(groupRenderName);
	// } // end if
	// else if (xml.indexOf(SERVICEGROUP) > 1) {
	// logger.info("Refreshing HostGroup....");
	// this.refreshServiceGroup();
	// SessionRenderer.render(groupRenderName);
	// } // end if
	// } // end if
	// }
	//
	// } catch (Exception exc) {
	// logger.error(exc.getMessage());
	//
	// }
	// }

	/**
	 * Callback method.
	 * 
	 * @param xmlTopic
	 *            the xml topic
	 */
	@Override
	public void refresh(String xmlTopic) {

		try {
			if (xmlTopic != null) {
				// logger.debug("xmlTopic:-" + xmlTopic);
				if (xmlTopic.indexOf(HOSTGROUP) > 1
						&& xmlTopic.indexOf(SERVICEGROUP) > 1) {
					logger.debug("Refreshing HostGroup and service group....");
					this.refreshHostGroup();
					this.refreshServiceGroup();
					/*
					 * RenderManager.getInstance().getOnDemandRenderer(
					 * groupRenderName).requestRender();
					 */
					SessionRenderer.render(groupRenderName);
				} else if (xmlTopic.indexOf(HOSTGROUP) > 1) {
					logger.debug("Refreshing HostGroup....");
					this.refreshHostGroup();

					// RenderManager.getInstance().getOnDemandRenderer(
					// groupRenderName).requestRender();

					SessionRenderer.render(groupRenderName);
				} else if (xmlTopic.indexOf(SERVICEGROUP) > 1) {
					logger.debug("Refreshing ServiceGroup....");
					this.refreshServiceGroup();
					/*
					 * RenderManager.getInstance().getOnDemandRenderer(
					 * groupRenderName).requestRender();
					 */
					SessionRenderer.render(groupRenderName);
				}
			} // end if

		} catch (Exception exc) {
			logger.debug("Exception in SystemFilterTreeBean : "
					+ exc.getMessage());

		}
	}

	/**
	 * Refreshes the hostgroup.
	 */
	private void refreshHostGroup() {
		DefaultMutableTreeNode rootTreeNode = (DefaultMutableTreeNode) model
				.getRoot();
		// index 1 is hostGroup. Need to check for node name later.
		((DynamicNodeUserObject) ((DefaultMutableTreeNode) rootTreeNode
				.getChildAt(1)).getUserObject()).addHostGroup(null);

	}

	/**
	 * Refreshes the ServiceGroup.
	 */
	private void refreshServiceGroup() {
		DefaultMutableTreeNode rootTreeNode = (DefaultMutableTreeNode) model
				.getRoot();
		// index 1 is hostGroup. Need to check for node name later.
		((DynamicNodeUserObject) ((DefaultMutableTreeNode) rootTreeNode
				.getChildAt(2)).getUserObject()).addServiceGroup(null);

	}

}
