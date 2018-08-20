/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.NavigationHelper;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.icesoft.faces.component.ext.HtmlCommandLink;
import com.icesoft.faces.component.tree.IceUserObject;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.model.impl.AttributeData;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class DynamicNodeUserObject extends IceUserObject {

	private static Logger logger = Logger.getLogger(DynamicNodeUserObject.class
			.getName());

	private NavigationTree treeBean;

	// displayPanel to show when a node is clicked
	private String displayPanel;

	private String styleClass = "";

	private List<String> assignedHostGroups = new ArrayList<String>();
	private List<String> assignedServiceGroups = new ArrayList<String>();

	private ReferenceTreeMetaModel rtmm = null;

	/**
	 * NavigationHelper
	 */
	private NavigationHelper navigationHelper;
	/**
	 * ExtendedUIRoleBean
	 */
	ExtendedUIRoleBean extendedUIRoleBean;

	private ArrayList<String> expandedCustomGroupList = null;

	private ArrayList<String> expandedCustomGroupForServiceGroupList = null;

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
		navigationHelper = new NavigationHelper();
		/**
		 * Faces context will be null on JMS or Non JSF thread. Perform a null
		 * check. Make increase the visibility of the statisbean to class level
		 * for the JMS thread.
		 */
		if (FacesContext.getCurrentInstance() != null) {
			extendedUIRoleBean = ConsoleHelper.getExtendedUIRoleBean();
			rtmm = ConsoleHelper.getRTMM();
		}

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
		try {
			DefaultMutableTreeNode preRefreshHostGroupState = getWrapper();
			expandedCustomGroupList = new ArrayList<String>();
			this.populateExpandedCustomGroups(preRefreshHostGroupState,
					ConsoleConstants.DB_HOSTGROUP);
			getWrapper().removeAllChildren();
			if (extendedUIRoleBean != null
					&& !extendedUIRoleBean.getHostGroupList().contains(
							ExtendedUIRoleBean.RESTRICTED_KEYWORD)) {
				Iterator<NetworkMetaEntity> hostGroups = null;
				if (rtmm != null) {
					hostGroups = rtmm.getAllHostGroups();
				}

                List<NetworkMetaEntity> rootCustomGroups = rtmm.getRootCustomGroups();
                for (NetworkMetaEntity rootCustomGroup : rootCustomGroups) {
					NodeType rootConcreteType = rtmm.checkConcreteEntityType(rootCustomGroup);
                    if (rootConcreteType == NodeType.HOST_GROUP) {
                        logger.debug("Root group name=" + rootCustomGroup.getName() + ", type=" + rootConcreteType);
						DefaultMutableTreeNode node = addCGClonedUserObject(null, rootCustomGroup);
						createCustomGroupBranch(rootCustomGroup, node);
					}
				}

				// getWrapper().removeAllChildren();
				if (hostGroups != null) {
					while (hostGroups.hasNext()) {
						NetworkMetaEntity hostGroup = hostGroups.next();
						if (!assignedHostGroups.contains(hostGroup.getName())) {
							if (extendedUIRoleBean.getHostGroupList().isEmpty()) {
								this.addHGClonedUserObject(hostGroup.getName(),
										hostGroup.getStatus().getStatus());
							} else {
								if (extendedUIRoleBean.getHostGroupList()
										.contains(hostGroup.getName())) {
									this.addHGClonedUserObject(hostGroup
											.getName(), hostGroup.getStatus()
											.getStatus());
								}
							}
						}
					}

				}// end if
				DefaultMutableTreeNode hostgroupNode = getWrapper();
				determineBubbleUpStatus(hostgroupNode,
						ConsoleConstants.DB_HOSTGROUP);
				cleanupEmptyCustomgroups(hostgroupNode);
			}// end if
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * Cleanup empty customgroups
	 */
	private void cleanupEmptyCustomgroups(DefaultMutableTreeNode treeNode) {
		if (treeNode != null) {
			logger.debug("Processing node ===>" + treeNode.getUserObject());
            // iterate over immutable children since cleanup mutates children
            List<DefaultMutableTreeNode> immutableChildren = new ArrayList<DefaultMutableTreeNode>();
            Enumeration<DefaultMutableTreeNode> childrenEnumeration = treeNode.children();
            while (childrenEnumeration.hasMoreElements()) {
                immutableChildren.add(childrenEnumeration.nextElement());
            }
            for (DefaultMutableTreeNode child : immutableChildren) {
				if (child != null) {
					IceUserObject childUserObject = ((IceUserObject) child
							.getUserObject());
					if (!child.isLeaf()) {
                        // depth first cleaning of tree required
						if (child.getChildCount() > 0) {
							logger.debug("Another level===>"
									+ child.getUserObject());
							cleanupEmptyCustomgroups(child);
						}
                        if (child.getChildCount() <= 0) {
							logger.debug("About to removing node ===>"
									+ childUserObject.getText());
							removeFromParent(child);
						} // end if
					} else {
						if (childUserObject.getBranchContractedIcon() != null
								|| childUserObject.getBranchExpandedIcon() != null) { // if
																						// this
																						// condition
																						// satisfies
																						// then
																						// it
																						// is
																						// a
																						// custom
																						// group
							removeFromParent(child);
						}
					}
				} // end if
			} // end for
		} // end if
	}

	/**
	 * Determines the bubbleupstatus by traversing the node
	 * 
	 * @param node
	 * @return
	 */
	private String determineBubbleUpStatus(DefaultMutableTreeNode node,
			String entityType) {
		if (node != null) {
			Enumeration children = node.children();
			ArrayList<String> tempBranchList = new ArrayList();
			String aggregatedBubbleUpStatus = null;
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) children
						.nextElement();
				IceUserObject userObj = (IceUserObject) child.getUserObject();
				if (child.isLeaf()) {
					logger.debug("host/service group==>" + userObj.getText()
							+ "==>" + userObj.getLeafIcon());

					if (entityType != null
							&& entityType
									.equalsIgnoreCase(ConsoleConstants.DB_HOSTGROUP)) {
						tempBranchList.add(userObj.getLeafIcon().replaceFirst(
								"host-group", "customgroup"));
						// check if this call is from external, if so then
						// simulate node clicked
						if (null != FacesContext.getCurrentInstance()) {
							Object viewparamObj = FacesUtils.getPortletSession(
									false).getAttribute(
									ConsoleConstants.GWOS_CONSOLE_VIEWPARAM);
							if (viewparamObj != null) {
								String viewparam = (String) viewparamObj;
								StringTokenizer stkn = new StringTokenizer(
										viewparam,
										ConsoleConstants.GWOS_CONSOLE_SESSION_PARAM_DELIM);
								String filterType = stkn.nextToken();
								String filterValue = stkn.nextToken();
								if (filterType != null
										&& filterValue != null
										&& filterType
												.equalsIgnoreCase(ConsoleConstants.DB_HOSTGROUP)
										&& userObj.getText().equalsIgnoreCase(
												filterValue)) {
									Object[] userObjArray = child
											.getUserObjectPath();
									for (Object expUserObj : userObjArray) {
										logger.debug(expUserObj);
										if (expUserObj instanceof DynamicNodeUserObject)
											((DynamicNodeUserObject) expUserObj)
													.setExpanded(true);
									}
									this.simulateNodeClicked(
											child,
											ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS);
									FacesUtils
											.getPortletSession(false)
											.removeAttribute(
													ConsoleConstants.GWOS_CONSOLE_VIEWPARAM);
								}
							} // end if
						}
					} else if (entityType != null
							&& entityType
									.equalsIgnoreCase(ConsoleConstants.DB_SERVICEGROUP)) {
						tempBranchList.add(userObj.getLeafIcon().replaceFirst(
								"service-group", "customgroup"));
						// check if this call is from external, if so then
						// simulate node clicked
						if (null != FacesContext.getCurrentInstance()) {
							Object viewparamObj = FacesUtils.getPortletSession(
									false).getAttribute(
									ConsoleConstants.GWOS_CONSOLE_VIEWPARAM);
							if (viewparamObj != null) {
								String viewparam = (String) viewparamObj;
								StringTokenizer stkn = new StringTokenizer(
										viewparam,
										ConsoleConstants.GWOS_CONSOLE_SESSION_PARAM_DELIM);
								String filterType = stkn.nextToken();
								String filterValue = stkn.nextToken();
								if (filterType != null
										&& filterValue != null
										&& filterType
												.equalsIgnoreCase(ConsoleConstants.DB_SERVICEGROUP)
										&& userObj.getText().equalsIgnoreCase(
												filterValue)) {
									Object[] userObjArray = child
											.getUserObjectPath();
									for (Object expUserObj : userObjArray) {
										logger.debug(expUserObj);
										if (expUserObj instanceof DynamicNodeUserObject)
											((DynamicNodeUserObject) expUserObj)
													.setExpanded(true);
									}
									this.simulateNodeClicked(
											child,
											ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS);
									FacesUtils
											.getPortletSession(false)
											.removeAttribute(
													ConsoleConstants.GWOS_CONSOLE_VIEWPARAM);
								} // end if
							} // end if
						}
					}
				} else {
					String branchIcon = this.determineBubbleUpStatus(child,
							entityType);
					String customGroupIcon = branchIcon.replaceFirst(
							"host-group", "customgroup").replaceFirst(
							"service-group", "customgroup");
					userObj.setLeafIcon(customGroupIcon);
					userObj.setBranchContractedIcon(customGroupIcon);
					userObj.setBranchExpandedIcon(customGroupIcon);
					child.setUserObject(userObj);
					logger.debug(userObj.getText() + "==>"
							+ userObj.getLeafIcon());
					tempBranchList.add(userObj.getLeafIcon());
				} // end if
			} // end while
			if (tempBranchList.size() > 0) {
				return determineBubbleUpIconsForCustomGroupIcons(tempBranchList);
			} // end if
		} // end if
		return null;
	}

    /**
     * Custom Group icon dictionary ordered most to least critical.
     */
    public static final List<String> CUSTOM_GROUP_ICON_DICTIONARY = Arrays.asList(new String[]{
            ConsoleConstants.CUSTOM_GROUP_CRITICAL_UNSCHEDULED,
            ConsoleConstants.CUSTOM_GROUP_WARNING,
            ConsoleConstants.CUSTOM_GROUP_UNREACHABLE,
            ConsoleConstants.CUSTOM_GROUP_CRITICAL_SCHEDULED,
            ConsoleConstants.CUSTOM_GROUP_PENDING,
            ConsoleConstants.CUSTOM_GROUP_OK
    });

    /**
     * String icon identity extractor for bubble up computation.
     */
    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<String> BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<String>() {
                @Override
                public String extractMonitorStatus(String obj) {
                    return obj;
                }
            };

	/**
	 * Determines the bubble up icons for custom groups.
	 * 
	 * @param customGroupIcons
	 * @return bubble up icon
	 */
	private String determineBubbleUpIconsForCustomGroupIcons(
			ArrayList<String> customGroupIcons) {
        String bubbleUpIcon = MonitorStatusBubbleUp.computeMonitorStatusBubbleUp(null, customGroupIcons,
                BUBBLE_UP_EXTRACTOR, CUSTOM_GROUP_ICON_DICTIONARY, null);
        return ((bubbleUpIcon != null) ? bubbleUpIcon : ConsoleConstants.CUSTOM_GROUP_NO_STATUS);
	}

	/**
	 * Helper to loop recursive and create customgroup branch
	 * 
	 * @param group
	 * @param node
	 */
	private void createCustomGroupBranch(NetworkMetaEntity group, DefaultMutableTreeNode node) {
		for (Integer childGroupId : group.getChildNodeList()) {
			if (group.getType() == NodeType.CUSTOM_GROUP) {
				NetworkMetaEntity nextLevel = rtmm.getCustomGroupById(childGroupId);
				DefaultMutableTreeNode childNode = addCGClonedUserObject(node, nextLevel);
				createCustomGroupBranch(nextLevel, childNode);
			} else {
				DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
				DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(clonedWrapper, treeBean);
				if (group.getType() == NodeType.HOST_GROUP) {
					NetworkMetaEntity hostGroup = rtmm.getHostGroupById(childGroupId);
					String elementName = hostGroup.getName();
					if (elementName != null) {
						assignedHostGroups.add(elementName);
						clonedUserObject.setText(elementName);
						clonedUserObject
								.setStyleClass(ConsoleConstants.STYLE_NONE);
						clonedUserObject.setLeaf(true);
						clonedUserObject.setLeafIcon(ConsoleHelper
								.determineBubbleUpStatusForHostGroup(hostGroup
										.getStatus().getStatus()));
						clonedWrapper.setUserObject(clonedUserObject);
						if (extendedUIRoleBean.getHostGroupList().isEmpty()) {
							node.add(clonedWrapper);
						} else {
							if (extendedUIRoleBean.getHostGroupList().contains(
									elementName)) {
								node.add(clonedWrapper);
							} // end if
						}
					} // end if
				} else if (group.getType() == NodeType.SERVICE_GROUP) {
					NetworkMetaEntity serviceGroup = rtmm.getServiceGroupById(childGroupId);
					String elementName = serviceGroup.getName();
					if (elementName != null) {
						assignedServiceGroups.add(elementName);
						clonedUserObject.setText(elementName);
						clonedUserObject
								.setStyleClass(ConsoleConstants.STYLE_NONE);
						clonedUserObject.setLeaf(true);
						clonedUserObject
								.setLeafIcon(ConsoleHelper
										.determineBubbleUpStatusForServiceGroup(serviceGroup
												.getStatus().getStatus()));
						clonedWrapper.setUserObject(clonedUserObject);
						if (extendedUIRoleBean.getServiceGroupList().isEmpty()) {
							node.add(clonedWrapper);
						} else {
							if (extendedUIRoleBean.getServiceGroupList()
									.contains(elementName)) {
								node.add(clonedWrapper);
							} // end if
						}
					} // end if
				}
			} // end if
		}

	}

	/**
	 * add custom group cloned user object
	 * 
	 * @param customGroup
	 */
	private DefaultMutableTreeNode addCGClonedUserObject(
			DefaultMutableTreeNode parent, NetworkMetaEntity customGroup) {
		DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
		DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(
				clonedWrapper, treeBean);
		String groupName = customGroup.getName();
		clonedUserObject.setText(groupName);
		clonedUserObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		clonedUserObject.setLeaf(false);
		clonedUserObject
				.setBranchContractedIcon(ConsoleConstants.BRANCH_CONTRACTED_ICON);
		clonedUserObject
				.setBranchExpandedIcon(ConsoleConstants.BRANCH_EXPANDED_ICON);
		clonedUserObject.setLeafIcon(ConsoleConstants.LEAF_ICON);
		if ((expandedCustomGroupList != null && expandedCustomGroupList
				.contains(groupName))
				|| (expandedCustomGroupForServiceGroupList != null && expandedCustomGroupForServiceGroupList
						.contains(groupName)))
			clonedUserObject.setExpanded(true);
		clonedWrapper.setUserObject(clonedUserObject);
		// finally add the node to the parent.
		if (parent != null) {
			parent.add(clonedWrapper);
		} else
			getWrapper().add(clonedWrapper);
		return clonedWrapper;
	}

	/**
	 * Populates the expanded custom groups
	 * 
	 * @param node
	 * @return
	 */
	private void populateExpandedCustomGroups(DefaultMutableTreeNode node,
			String type) {
		Enumeration children = node.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children
					.nextElement();
			if (!child.isLeaf()) {
				IceUserObject userObj = (IceUserObject) child.getUserObject();
				if (userObj.isExpanded()) {
					if (type.equalsIgnoreCase(ConsoleConstants.DB_HOSTGROUP))
						expandedCustomGroupList.add(userObj.getText());
					else
						expandedCustomGroupForServiceGroupList.add(userObj
								.getText());
					populateExpandedCustomGroups(child, type);
				} // end if
			} // end if
		} // end while
	}

	/**
	 * add host group cloned user object
	 * 
	 * @param name
	 *            and bubbleup status
	 */
	private void addHGClonedUserObject(String name, String bubbleUpStatus) {
		DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
		DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(
				clonedWrapper, treeBean);

		clonedUserObject.setText(name);
		clonedUserObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		clonedUserObject.setLeaf(true);
		clonedUserObject.setLeafIcon(ConsoleHelper
				.determineBubbleUpStatusForHostGroup(bubbleUpStatus));
		clonedWrapper.setUserObject(clonedUserObject);
		getWrapper().add(clonedWrapper);
	}

	/**
	 * Copies this node and adds a it as a child node.
	 * 
	 * @param event
	 *            that fired this method
	 */
	public void addServiceGroup(ActionEvent event) {
		DefaultMutableTreeNode preRefreshServiceGroupState = getWrapper();
		expandedCustomGroupForServiceGroupList = new ArrayList<String>();
		this.populateExpandedCustomGroups(preRefreshServiceGroupState,
				ConsoleConstants.DB_SERVICEGROUP);
		getWrapper().removeAllChildren();
		try {
			if (extendedUIRoleBean != null
					&& !extendedUIRoleBean.getServiceGroupList().contains(
							ExtendedUIRoleBean.RESTRICTED_KEYWORD)) {
				Iterator<NetworkMetaEntity> serviceGroups = null;
				if (rtmm != null) {
					serviceGroups = rtmm.getAllServiceGroups();
				}

                List<NetworkMetaEntity> rootCustomGroups = rtmm.getRootCustomGroups();
                for (NetworkMetaEntity rootCustomGroup : rootCustomGroups) {
                    NodeType rootConcreteType = rtmm.checkConcreteEntityType(rootCustomGroup);
                    if (rootConcreteType == NodeType.SERVICE_GROUP) {
                        logger.debug("Root group name=" + rootCustomGroup.getName() + ", type=" + rootConcreteType);
                        DefaultMutableTreeNode node = addCGClonedUserObject(null, rootCustomGroup);
                        createCustomGroupBranch(rootCustomGroup, node);
                    }
                }

				if (serviceGroups != null) {
					while (serviceGroups.hasNext()) {
						NetworkMetaEntity serviceGroup = (NetworkMetaEntity) serviceGroups
								.next();

						if (!assignedServiceGroups.contains(serviceGroup
								.getName())) {
							if (extendedUIRoleBean.getServiceGroupList()
									.isEmpty()) {
								this.addSGClonedUserObject(serviceGroup
										.getName(), serviceGroup.getStatus()
										.getStatus());
							} else {
								if (extendedUIRoleBean.getServiceGroupList()
										.contains(serviceGroup.getName())) {
									this.addSGClonedUserObject(serviceGroup
											.getName(), serviceGroup
											.getStatus().getStatus());
								}// end if
							}// end else
						} // edn if
					} // end while
				}
				DefaultMutableTreeNode servicegroupNode = getWrapper();
				determineBubbleUpStatus(servicegroupNode,
						ConsoleConstants.DB_SERVICEGROUP);
				cleanupEmptyCustomgroups(servicegroupNode);
			}// end if
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * add service group cloned user object
	 * 
	 * @param name
     * @param bubbleUpStatus
	 */
	private void addSGClonedUserObject(String name, String bubbleUpStatus) {
		DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
		DynamicNodeUserObject clonedUserObject = new DynamicNodeUserObject(
				clonedWrapper, treeBean);
		clonedUserObject.setText(name);
		clonedUserObject.setStyleClass(ConsoleConstants.STYLE_NONE);
		clonedUserObject.setLeaf(true);
		clonedUserObject.setLeafIcon(ConsoleHelper
				.determineBubbleUpStatusForServiceGroup(bubbleUpStatus));
		clonedWrapper.setUserObject(clonedUserObject);
		getWrapper().add(clonedWrapper);
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
	 * Helper to find the filtertype
	 * 
	 * @param currentNode
	 * @return
	 */
	private String getFilterType(DefaultMutableTreeNode currentNode) {
		TreeNode[] nodeArray = currentNode.getPath();
		for (TreeNode node : nodeArray) {
			if (node.toString().equalsIgnoreCase(
					ConsoleConstants.SYSTEM_FILTER_APPLICATIONS))
				return ConsoleConstants.SYSTEM_FILTER_APPLICATIONS;
			if (node.toString().equalsIgnoreCase(
					ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS))
				return ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS;
			if (node.toString().equalsIgnoreCase(
					ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS))
				return ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS;
			if (node.toString().equalsIgnoreCase(
					ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS))
				return ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS;
		}
		return ConsoleConstants.FILTER_EVENTS;
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

		ConsoleManager mgr = ConsoleHelper.getConsoleManager();
		// DefaultMutableTreeNode parent = userObject.getWrapper().getParent();
		DefaultMutableTreeNode currentNode = userObject.getWrapper();

		String filterType = this.getFilterType(currentNode);

        HtmlCommandLink treeLink = (HtmlCommandLink)event.getSource();
        String linkValue = treeLink.getValue().toString();
		if (currentNode.isLeaf() && !linkValue.equalsIgnoreCase(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
			this.resetStyle(currentNode.getRoot().children());
			int selectedIndex = tabset.getTabIndex();
			logger.debug("selectedIndex=" + selectedIndex);
			if (selectedIndex <= 0)
				selectedIndex = 0;
			Tab tab = null;
			if (tabset.getTabs().size() == selectedIndex)
				tab = tabset.getTabs().get(selectedIndex - 1);
			else
				tab = tabset.getTabs().get(selectedIndex);

            tab.getActionBean().setRenderAckButton(true);
			if (filterType != null
					&& filterType
							.equals(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
				mgr.populateEventsByHostGroup(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(currentNode.getRoot()
						.getChildAt(1).toString());
				tab.setLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS)
						+ "=" + userObject.getText());
				tab.setHiddenLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS)
						+ "=" + userObject.getText());

			} else if (filterType != null
					&& filterType
							.equals(ConsoleConstants.SYSTEM_FILTER_APPLICATIONS)) {
				mgr.populateEventsByApplicationType(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(currentNode.getRoot()
						.getChildAt(0).toString());
				tab.setLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_APPTYPE)
						+ "=" + userObject.getText());
				tab.setHiddenLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_APPTYPE)
						+ "=" + userObject.getText());
			} else if (filterType != null
					&& filterType
							.equals(ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS)) {
				mgr.populateEventsByOperationStatus(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(currentNode.getRoot()
						.getChildAt(3).toString());
				tab.setLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_OPSTATUS)
						+ "=" + userObject.getText());
				tab.setHiddenLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_OPSTATUS)
						+ "=" + userObject.getText());
			} else if (filterType != null
					&& filterType
							.equals(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
				mgr.populateEventsByServiceGroup(userObject.getText(), null);
				// add style here
				ConsoleHelper.hightlightNode(currentNode.getRoot()
						.getChildAt(2).toString());
				tab.setLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_SERVICE_GRPS)
						+ "=" + userObject.getText());
				tab.setHiddenLabel(ResourceUtils
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
					this.resetStyle(currentNode.getRoot().children());
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
							tab.setLabel(ResourceUtils
									.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_EVENT_BY)
									+ "=" + userObject.getText());
							tab.setHiddenLabel(ResourceUtils
									.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_EVENT_BY)
									+ "=" + userObject.getText());
							break;
						} // end if
					} // end if
				} // end for

			}

			tab.setFilterType(filterType);
			tab.setRendered(true);
			String tabid;
			int tabidIndex = 0;
			// update navigation history
			try {
				tabid = tab.getTabId();
				tabidIndex = Integer.parseInt(tabid
						.substring(Tab.SEARCH_PANELID_PREFIX.length()));

				navigationHelper
						.updateHistoryRecord(tabset.getUserId(), tabidIndex,
								tab.getHiddenLabel(), tab.getFilterType(),
								ConsoleConstants.APP_TYPE_CONSOLE, null,
								tab.getLabel());
			} catch (NumberFormatException e) {
				// ignore
				logger.debug("NumberFormatException : tabId seems to be incorrect ["
						+ tabidIndex + "]");
			} catch (IOException e) {
				// ignore
				logger.warn("Failed to update tab navigation information from database for user with Id ["
						+ tabset.getUserId()
						+ "].  for the tab ["
						+ tab.getLabel() + "]");
			} catch (Exception exception) {
				logger.warn("Failed to update tab navigation information from database for user with Id ["
						+ tabset.getUserId()
						+ "].  for the tab ["
						+ tab.getLabel() + "]");
			}

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

	} // end if

    /**
     * Expands/collapses the navigation tree and populates the datatable
     * @param currentNode
     * @param filterType
     */
	public void simulateNodeClicked(DefaultMutableTreeNode currentNode,
			String filterType) {
		DynamicNodeUserObject userObject = (DynamicNodeUserObject) currentNode
				.getUserObject();
		treeBean.setSelectedNodeObject(userObject);

		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		ConsoleManager mgr = ConsoleHelper.getConsoleManager();

		if (currentNode.isLeaf()) {
			this.resetStyle(currentNode.getRoot().children());
			int selectedIndex = tabset.getTabIndex();
			logger.debug("selectedIndex=" + selectedIndex);
			if (selectedIndex <= 0)
				selectedIndex = 0;
			Tab tab = null;
			if (tabset.getTabs().size() == selectedIndex)
				tab = tabset.getTabs().get(selectedIndex - 1);
			else
				tab = tabset.getTabs().get(selectedIndex);

			if (filterType != null
					&& filterType
							.equals(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
				mgr.populateEventsByHostGroup(userObject.getText(), null);

				tab.setLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS)
						+ "=" + userObject.getText());
				tab.setHiddenLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS)
						+ "=" + userObject.getText());

			} else if (filterType != null
					&& filterType
							.equals(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
				mgr.populateEventsByServiceGroup(userObject.getText(), null);

				tab.setLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_SERVICE_GRPS)
						+ "=" + userObject.getText());
				tab.setHiddenLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_LABEL_SERVICE_GRPS)
						+ "=" + userObject.getText());
			}

			tab.setFilterType(filterType);
			tab.setRendered(true);
			String tabid;
			int tabidIndex = 0;
			// update navigation history
			try {
				tabid = tab.getTabId();
				tabidIndex = Integer.parseInt(tabid
						.substring(Tab.SEARCH_PANELID_PREFIX.length()));

				navigationHelper
						.updateHistoryRecord(tabset.getUserId(), tabidIndex,
								tab.getHiddenLabel(), tab.getFilterType(),
								ConsoleConstants.APP_TYPE_CONSOLE, null,
								tab.getLabel());
			} catch (NumberFormatException e) {
				// ignore
				logger.debug("NumberFormatException : tabId seems to be incorrect ["
						+ tabidIndex + "]");
			} catch (IOException e) {
				// ignore
				logger.warn("Failed to update tab navigation information from database for user with Id ["
						+ tabset.getUserId()
						+ "].  for the tab ["
						+ tab.getLabel() + "]");
			} catch (Exception exception) {
				logger.warn("Failed to update tab navigation information from database for user with Id ["
						+ tabset.getUserId()
						+ "].  for the tab ["
						+ tab.getLabel() + "]");
			}

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
			userObject
					.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
		} // end if

	} // end if

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
			else {
				this.resetStyle(node.children());
			}
		} // end while
	} // end resetStyle

	/**
	 * Removes all the branches in the tree path to the root
	 */
	private void removeFromParent(DefaultMutableTreeNode child) {
		if (child != null && child.getChildCount() == 0) {
			logger.debug("Removing node ===>"
					+ ((IceUserObject) child.getUserObject()).getText()
					+ " with path ===>" + child.getPath() + " and level ==>"
					+ child.getLevel());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child
					.getParent();
			child.removeFromParent();
			// Now traverse up in the path and delete parent if child had no
			// children
			removeFromParent(parent);
		}
	}

}
