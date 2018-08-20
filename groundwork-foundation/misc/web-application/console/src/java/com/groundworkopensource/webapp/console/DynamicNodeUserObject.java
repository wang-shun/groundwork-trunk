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

import java.util.Enumeration;
import java.util.Vector;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCategory;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSHostGroup;
import org.groundwork.foundation.ws.model.impl.AttributeData;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.HostGroupQueryType;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.icesoft.faces.component.tree.IceUserObject;

public class DynamicNodeUserObject extends IceUserObject {

	private static Logger logger = Logger.getLogger(DynamicNodeUserObject.class
			.getName());

	private NavigationTree treeBean;

	// displayPanel to show when a node is clicked
	private String displayPanel;

	private String styleClass = "";

	/**
	 * Default contsructor for a PanelSelectUserObject object. A reference is
	 * made to a backing bean with the name "panelStack", if possible.
	 * 
	 * @param wrapper
	 */
	public DynamicNodeUserObject(DefaultMutableTreeNode wrapper,
			NavigationTree tree) {
		super(wrapper);

		treeBean = tree;

	}

	/**
	 * Gets the name of a panel in the panel stack which is associated with this
	 * object.
	 * 
	 * @return name of a panel in the panel stack
	 */
	public String getDisplayPanel() {
		return displayPanel;
	}

	/**
	 * Sets the name of a panelStack panel, assumed to be valid.
	 * 
	 * @param displayPanel
	 *            panelStack panel name.
	 */
	public void setDisplayPanel(String displayPanel) {
		this.displayPanel = displayPanel;
	}

	/**
	 * Copies this node and adds a it as a child node.
	 * 
	 * @param event
	 *            that fired this method
	 */
	public void addHostGroup(ActionEvent event) {
		// System.out.println("Inside addNode");

		try {
			WSHostGroup wsHostGroup = ServiceLocator.hostGroupLocator()
					.getwshostgroup();
			HostGroupQueryType queryType = HostGroupQueryType.ALL;
			WSFoundationCollection col = wsHostGroup.getHostGroups(queryType,
					null, null, false, -1, -1, null);

			HostGroup[] hostGroups = col.getHostGroup();
			getWrapper().removeAllChildren();
			for (int i = 0; i < hostGroups.length; i++) {
				HostGroup hostGroup = (HostGroup) hostGroups[i];
				// System.out.println(hostGroup.getName());
				DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
				DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(
						clonedWrapper, treeBean);
				clonedUserObject.setText(hostGroup.getName());
				clonedUserObject.setStyleClass(ConsoleConstants.STYLE_NONE);
				clonedUserObject.setLeaf(true);
				clonedUserObject.setLeafIcon(ConsoleConstants.LEAF_ICON);
				clonedWrapper.setUserObject(clonedUserObject);
				getWrapper().add(clonedWrapper);
			}
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * Copies this node and adds a it as a child node.
	 * 
	 * @param event
	 *            that fired this method
	 */
	public void addServiceGroup(ActionEvent event) {

		try {
			WSCategory wscategory = ServiceLocator.categoryLocator()
					.getwscategory();
			WSFoundationCollection col = wscategory.getRootCategories(
					ConsoleConstants.ENTITY_NAME_SERVICEGROUP, -1, -1, null,
					true, false);
			Category[] serviceGroups = col.getCategory();
			getWrapper().removeAllChildren();
			if (serviceGroups != null) {
				for (int i = 0; i < serviceGroups.length; i++) {
					Category serviceGroup = (Category) serviceGroups[i];
					DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
					DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(
							clonedWrapper, treeBean);
					clonedUserObject.setText(serviceGroup.getName());
					clonedUserObject.setStyleClass(ConsoleConstants.STYLE_NONE);
					clonedUserObject.setLeaf(true);
					clonedUserObject.setLeafIcon(ConsoleConstants.LEAF_ICON);
					clonedWrapper.setUserObject(clonedUserObject);
					getWrapper().add(clonedWrapper);
				} // end for
			} // end if
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * Copies this node and adds a it as a child node.
	 * 
	 * @param event
	 *            that fired this method
	 */
	public void addApplications(ActionEvent event) {
		try {

			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
			AttributeQueryType queryType = AttributeQueryType.APPLICATION_TYPES;
			WSFoundationCollection col = wsCommon.getAttributeData(queryType);

			AttributeData[] applicationTypes = col.getAttributeData();

			for (int i = 0; i < applicationTypes.length; i++) {
				AttributeData application = (AttributeData) applicationTypes[i];
				DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
				DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(
						clonedWrapper, treeBean);
				clonedUserObject.setText(application.getName());
				clonedUserObject.setStyleClass(ConsoleConstants.STYLE_NONE);
				clonedUserObject.setLeaf(true);
				clonedUserObject.setLeafIcon(ConsoleConstants.LEAF_ICON);
				clonedWrapper.setUserObject(clonedUserObject);
				getWrapper().add(clonedWrapper);
			} // end for
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * Copies this node and adds a it as a child node.
	 * 
	 * @param event
	 *            that fired this method
	 */
	public void addOperationStatus(ActionEvent event) {
		// System.out.println("Inside addNode");

		try {

			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
			AttributeQueryType queryType = AttributeQueryType.OPERATION_STATUSES;
			WSFoundationCollection col = wsCommon.getAttributeData(queryType);
			AttributeData[] operationStatus = col.getAttributeData();

			for (int i = 0; i < operationStatus.length; i++) {
				AttributeData status = (AttributeData) operationStatus[i];
				DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
				DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(
						clonedWrapper, treeBean);
				clonedUserObject.setLeaf(true);
				clonedUserObject.setLeafIcon(ConsoleConstants.LEAF_ICON);
				clonedUserObject.setText(status.getName());
				clonedUserObject.setStyleClass(ConsoleConstants.STYLE_NONE);
				clonedWrapper.setUserObject(clonedUserObject);
				getWrapper().add(clonedWrapper);
			} // end for
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * Expands/collapses the navigation tree and populates the datatable
	 * 
	 * 
	 * @param event
	 *            that fired this method
	 */
	public void nodeClicked(ActionEvent event) {
		treeBean.setSelectedNodeObject(this);
		DynamicNodeUserObject userObject = (DynamicNodeUserObject) treeBean
				.getSelectedNodeObject();
		// logger.info(nodeSelected);
		TabsetBean tabset = ConsoleHelper.getTabSetBean();

		/*
		 * if (panelStack != null) { panelStack
		 * .setSelectedPanel(ConsoleConstants.PANELSTACK_DATATABLE_PANEL);
		 */
		ConsoleManager mgr = ConsoleHelper.getConsoleManager();
		TreeNode parent = userObject.getWrapper().getParent();
		if (parent != null) {
			TreeNode grandParent = parent.getParent();
			if (grandParent != null) {
				this.resetStyle(grandParent.children());
			} // end if
			int selectedIndex = tabset.getTabIndex();
			logger.debug("selectedIndex=" + selectedIndex);
			if (selectedIndex <= 0)
				selectedIndex = 0;
			Tab tab = null;
			if (tabset.getTabs().size() == selectedIndex)
				tab = tabset.getTabs().get(selectedIndex - 1);
			else
				tab = tabset.getTabs().get(selectedIndex);
			if (parent.toString().equals(
					ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
				mgr.populateEventsByHostGroup(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(parent.toString());
				tab
						.setLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS)
								+ "=" + userObject.getText());
				tab
						.setHiddenLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS)
								+ "=" + userObject.getText());

			} else if (parent.toString().equals(
					ConsoleConstants.SYSTEM_FILTER_APPLICATIONS)) {
				mgr.populateEventsByApplicationType(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(parent.toString());
				tab
						.setLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_APPTYPE)
								+ "=" + userObject.getText());
				tab
						.setHiddenLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_APPTYPE)
								+ "=" + userObject.getText());
			} else if (parent.toString().equals(
					ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS)) {
				mgr.populateEventsByOperationStatus(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(parent.toString());
				tab
						.setLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_OPSTATUS)
								+ "=" + userObject.getText());
				tab
						.setHiddenLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_OPSTATUS)
								+ "=" + userObject.getText());
			} else if (parent.toString().equals(
					ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
				mgr.populateEventsByServiceGroup(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(parent.toString());
				tab
						.setLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_SERVICE_GRPS)
								+ "=" + userObject.getText());
				tab
						.setHiddenLabel(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_SERVICE_GRPS)
								+ "=" + userObject.getText());
			} else {
				if (userObject.getText().equals(
						ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
					this.addHostGroup(null);
				} else if (userObject.getText().equals(
						ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
					this.addServiceGroup(null);
				} else {
					this.resetStyle(parent.children());
					PublicFiltersConfigBean configBean = ConsoleHelper
							.getPublicFilters();
					Vector<FilterConfigBean> filters = configBean
							.getFilterConfigs();
					for (int i = 0; i < filters.size(); i++) {

						FilterConfigBean filter = filters.get(i);
						if (userObject.getText().equalsIgnoreCase(
								filter.getLabel())) {
							mgr.populateEventsByCombinedFilters(filter, null);
							ConsoleHelper.hightlightNode(userObject.getText());
							tab
									.setLabel(ResourceUtils
											.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_EVENT_BY)
											+ "=" + userObject.getText());
							tab
									.setHiddenLabel(ResourceUtils
											.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_EVENT_BY)
											+ "=" + userObject.getText());
							break;
						} // end if
					} // end if
				} // end for

			}

			tab.setFilterType(parent.toString());
			tab.setRendered(true);
			tab.resetSearchCriteria();

			// reset the select button
			MessageSelectBean msgSelectBean = tab.getMsgSelector();
			msgSelectBean.reset();
			tab.getActionBean().reset();

			// reset freezeBean
			FreezeTableBean freezeBean = tab.getFreezeBean();
			if (freezeBean
					.getFreezeButtonText()
					.equalsIgnoreCase(
							ResourceUtils
									.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_RESUME_EVENTS)))
				freezeBean.freeze(false);
			// reset the menu
			tab.getActionBean().reset();

			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			if (!tabset.isAtleastOneNewTabDown()
					&& !tabset.isAtleastOneNewTabUp()) {
				Tab newTab = new Tab(
						ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW));

				tabset.addTab(newTab);

			} // end if
		} // end if
		// } // end if
	} // end if

	// } // end if

	public String getStyleClass() {

		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	/**
	 * Resets the style for the tree
	 * 
	 * @param enums
	 */
	private void resetStyle(Enumeration<DefaultMutableTreeNode> enums) {
		while (enums.hasMoreElements()) {
			DefaultMutableTreeNode node = enums.nextElement();
			Enumeration<DefaultMutableTreeNode> children = node.children();
			if (!children.hasMoreElements()) {
				DynamicNodeUserObject userObject = (DynamicNodeUserObject) node
						.getUserObject();
				userObject.setStyleClass(ConsoleConstants.STYLE_NONE);
			} // end if
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode childNode = children.nextElement();
				DynamicNodeUserObject userObject = (DynamicNodeUserObject) childNode
						.getUserObject();
				userObject.setStyleClass(ConsoleConstants.STYLE_NONE);
			} // end while

		} // end while
	} // end resetStyle

}
