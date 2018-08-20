package com.groundworkopensource.portal.statusviewer.bean.tree;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.statusviewer.bean.UIEventsServerPush;
import com.groundworkopensource.portal.statusviewer.bean.UIHistoryBean;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.IPCHandlerConstants;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.icesoft.faces.component.tree.Tree;
import com.icesoft.faces.context.effects.JavascriptContext;
import org.apache.log4j.Logger;

import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.portlet.PortletRequest;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Handles Tree Portlet specific functionality. This Bean provides Host, Service
 * Trees and search Functionality
 * 
 * @author nitin_jadhav
 * 
 */

public class NetworkTreeBean extends UIEventsServerPush {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5104374719494631612L;

	/**
	 * Object representation of Host tree
	 */
	private NetworkObjectTree hostTree;

	/**
	 * Object representation of ServiceTree
	 */
	private NetworkObjectTree serviceTree;

	/**
	 * Object representation of customTree
	 */
	private NetworkObjectTree customHGTree;

	/**
	 * Object representation of customTree
	 */
	private NetworkObjectTree customSGTree;

	/**
	 * reference model for building tree
	 */
	private ReferenceTreeMetaModel referenceTreeModel;

	/**
	 * This boolean field decides whether or not to show "retry" button in case
	 * of empty host tree found.
	 */
	private boolean hostTreeOk;

	/**
	 * This boolean field decides whether or not to show "retry" button in case
	 * of empty host tree found.
	 */
	private boolean serviceTreeOk;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(NetworkTreeBean.class
			.getName());

	/**
	 * subpage path
	 */
	private String subPagePath = null;

	/**
	 * tab index
	 */
	private int index;

	/**
	 * Hidden field for tree.
	 */
	private String treeHiddenField;

	/**
	 * indicates whether the rendering is 1st time.
	 */
	private boolean firstRender;
	/**
	 * Is hosts Tree Restricted
	 */
	private boolean hostTreeRestricted;

	/**
	 * Is services Tree Restricted
	 */
	private boolean serviceTreeRestricted;

	/**
	 * Tree portlet width
	 */
	private int treePortletWidth = UIHistoryBean.getTreePortletWidth(FacesUtils
			.getLoggedInUser());

	/**
	 * constructor
	 */
	public NetworkTreeBean() {
		// createTreeModels();
		// by default set the index to 0
		setIndex(0);
		firstRender = true;
		// get the userExtendedRoleBean managed instance
		UserExtendedRoleBean userExtendedRoleBean = PortletUtils
				.getUserExtendedRoleBean();
		if (userExtendedRoleBean != null) {
			if (userExtendedRoleBean.getExtRoleHostGroupList().contains(
					UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
				hostTreeRestricted = true;
			}
			if (userExtendedRoleBean.getExtRoleServiceGroupList().contains(
					UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
				serviceTreeRestricted = true;
			}
		}

		// Check If
		// 1) The default Host group is null
		// 2) Host Group List String is not null and R#STR!CT#D
		// 3) DefaultServiceGroup is not null
		// 4) ExtRoleService Gruop List is not R#STR!CT#D
		// then set the index to 1. (Service tab in Tree View)

		if (userExtendedRoleBean.getDefaultHostGroup() == null
				&& userExtendedRoleBean.getHostGroupListString() != null
				&& userExtendedRoleBean.getHostGroupListString().equals(
						UserExtendedRoleBean.RESTRICTED_KEYWORD)
				&& userExtendedRoleBean.getDefaultServiceGroup() != null
				&& !userExtendedRoleBean.getExtRoleServiceGroupList().contains(
						UserExtendedRoleBean.RESTRICTED_KEYWORD)) {

			// setting the index to 1 for service list tab.
			setIndex(1);

		}// close if

	}

	/**
	 * Build Host/Service Tree model (JSF models) by taking data from
	 * ReferenceTreeMetaModel
	 */
	private void createTreeModels() {

		createHostGroupTreeModel();
		createServiceGroupTreeModel();

	}

	/**
	 * Restores the tree state for the sub page. Complete tree is not restored.
	 * Only the node clicked is expanded.
	 * 
	 * @param path
	 */
	private void restoreTreeStateForSubpage(String path, String targetView) {

		if (path != null && !Constant.EMPTY_STRING.equalsIgnoreCase(path)) {
			StringTokenizer stkn = new StringTokenizer(path, Constant.COMMA);
			int drillLevel = stkn.countTokens();
			String groupName = stkn.nextToken();

			// currently, nodeClickHistory is EMPTY. add this current group in
			// it, so that in case of JMS push, selected group will be restored.

			if (targetView != null) {
				switch (drillLevel) {
				case Constant.ONE:
					if (targetView.equalsIgnoreCase(Constant.HOSTGROUPVIEW)) {
						expandGroupLevelForSubpage(groupName, hostTree);
						setIndex(0);
					} else if (targetView
							.equalsIgnoreCase(Constant.SERVICEGROUPVIEW)) {
						expandGroupLevelForSubpage(groupName, serviceTree);
						setIndex(1);
					} // end if
					break;

				case Constant.TWO:
					String hostName = stkn.nextToken();
					if (targetView.equalsIgnoreCase(Constant.HOSTVIEW)) {
						expandGroupLevelForSubpage(groupName, hostTree);
						expandHostLevelForSubPage(hostName, hostTree);
						setIndex(0);
					} else if (targetView
							.equalsIgnoreCase(Constant.SERVICEVIEW)) {
						// we need to identify if this service is is host
						// tree or service tree, by it group
						expandGroupLevelForSubpage(groupName, serviceTree);
						setIndex(1);
					}
					break;
				case Constant.THREE:
					// its service under host tree
					String hostNm = stkn.nextToken();
					expandGroupLevelForSubpage(groupName, hostTree);
					expandHostLevelForSubPage(hostNm, hostTree);
					setIndex(0);
					break;
				default:
					break;

				}
			} // end if

		} // end if
	}

	/**
	 * Expands the hostGroup or service group level for the subpage
	 * 
	 * @param group
	 * @param tree
	 */
	@SuppressWarnings("unchecked")
	private void expandGroupLevelForSubpage(String group, NetworkObjectTree tree) {

		if (tree != null && group != null) {
			DefaultMutableTreeNode node = tree.getRootTreeNode();
			Enumeration<DefaultMutableTreeNode> enumRoot = node.children();
			while (enumRoot.hasMoreElements()) {
				DefaultMutableTreeNode groupNode = enumRoot.nextElement();
				NetworkObject netGroup = (NetworkObject) groupNode
						.getUserObject();
				String groupNodeText = netGroup.getText();
				// LOGGER.debug(">>>" + groupNodeText);
				if (groupNodeText.equalsIgnoreCase(group)) {
					netGroup.setExpanded(true);
					netGroup.refresh(referenceTreeModel);
					// LOGGER.debug("Expanding " + groupNodeText);
				} // end if
			} // end while
		}
	}

	/**
	 * Expands the host level for the subpage
	 * 
	 * @param entity
	 * @param tree
	 */
	@SuppressWarnings("unchecked")
	private void expandHostLevelForSubPage(String entity, NetworkObjectTree tree) {
		if (tree != null && tree.getTreeType() != null
				&& tree.getTreeType().equals(NodeType.HOST_GROUP)) {
			DefaultMutableTreeNode nodeForEntityLevel = tree.getRootTreeNode();
			Enumeration<DefaultMutableTreeNode> enumRootForHostLevel = nodeForEntityLevel
					.children();
			while (enumRootForHostLevel.hasMoreElements()) {
				DefaultMutableTreeNode hostGroupNode = enumRootForHostLevel
						.nextElement();
				Enumeration<DefaultMutableTreeNode> enumHostGroup = hostGroupNode
						.children();
				while (enumHostGroup.hasMoreElements()) {
					DefaultMutableTreeNode hostNode = enumHostGroup
							.nextElement();
					Object userObject = hostNode.getUserObject();
					if (userObject != null
							&& (userObject instanceof HostObject)) {
						HostObject netHost = (HostObject) userObject;
						String hostNodeText = netHost.getText();
						// LOGGER.debug(">>> " + hostNodeText);

						if (hostNodeText.equalsIgnoreCase(entity)) {
							// LOGGER.debug("Expanding " + hostNodeText);
							netHost.setExpanded(true);
							netHost.refresh(referenceTreeModel);
						} // end while
					} // end if

				} // end while
			} // end while
		}
	}

	/**
	 * Creates the hostgroup model
	 */
	private void createHostGroupTreeModel() {
		try {
			// build Host tree
            referenceTreeModel.rebuildModel();
			hostTree = new NetworkObjectTree(NodeType.HOST_GROUP, referenceTreeModel);
			// disable the "retry" button, since the tree is build successfully,
			// with at least 1 node.
			hostTreeOk = true;
			DefaultMutableTreeNode rootTreeNode = hostTree.getRootTreeNode();
			LOGGER.debug("Now cleaning up empty custom groups.");
			cleanupEmptyCustomgroups(rootTreeNode);
			LOGGER.debug("Host tree is build and OK.");
		} catch (GWPortalException e) {
			// error!! enable "retry" button on UI
			LOGGER.error("Empty Host tree found. Enabling retry button on UI in createTreeModels():" + e.getMessage(), e);
			hostTreeOk = false;
		}
		catch (Exception e) {
			LOGGER.error("Error processing HostGroup tree:" + e.getMessage(), e);
			hostTreeOk = false;
		}
	}

	/**
	 * Cleanup empty customgroups
	 */
	private void cleanupEmptyCustomgroups(DefaultMutableTreeNode treeNode) {
		if (treeNode != null) {
            // iterate over immutable children since cleanup mutates children
            List<DefaultMutableTreeNode> immutableChildren = new ArrayList<DefaultMutableTreeNode>();
            Enumeration<DefaultMutableTreeNode> childrenEnumeration = treeNode.children();
            while (childrenEnumeration.hasMoreElements()) {
                immutableChildren.add(childrenEnumeration.nextElement());
            }
			for (DefaultMutableTreeNode child : immutableChildren) {
				if (child != null) {
					if (child.getUserObject() instanceof NetworkCustomGroup) {
						NetworkCustomGroup custGroup = (NetworkCustomGroup) child
								.getUserObject();
                        // depth first cleaning of tree required
                        LOGGER.debug("Processing ===>" + custGroup.getText());
                        if (child.getChildCount() > 0) {
                            LOGGER.debug("Another level===>");
                            cleanupEmptyCustomgroups(child);
                        }
                        if (child.getChildCount() <= 0) {
                            removeFromParent(child);
                            continue;
                        }
                        // subtree clean, lookup and initialize state, (this is the right place
                        // to bubble up the custom group as MSP is already applied here)
						NetworkMetaEntity custGroupEntity = referenceTreeModel
								.getCustomGroupById(custGroup.getObjectId());
						NetworkObjectStatusEnum statusEnum = referenceTreeModel
								.determineBubbleUpStatusForCustomGroup(custGroupEntity);
						custGroup.setStatus(NetworkObjectStatusEnum
								.getStatusEnumFromMonitorStatus(
										statusEnum.getStatus(),
										NodeType.CUSTOM_GROUP));
					} // end if
				} // end if
			} // end for
		} // end if
	}

	/**
	 * Removes all the branches in the tree path to the root
	 */
	private void removeFromParent(DefaultMutableTreeNode child) {
		if (child != null
				&& child.getUserObject() instanceof NetworkCustomGroup
				&& child.getChildCount() == 0) {
			NetworkCustomGroup custGroup = (NetworkCustomGroup) child
					.getUserObject();
			LOGGER.debug("Removing node ===>" + custGroup.getText()
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

	/**
	 * Creates the service group model
	 **/
	private void createServiceGroupTreeModel() {
		try {
			// build Host tree
			serviceTree = new NetworkObjectTree(NodeType.SERVICE_GROUP,
					referenceTreeModel);
			DefaultMutableTreeNode rootTreeNode = serviceTree.getRootTreeNode();
			LOGGER.debug("Now cleaning up empty custom groups.");
			cleanupEmptyCustomgroups(rootTreeNode);
			// disable the "retry" button, since the tree is build successfully,
			// with at least 1 node.
			serviceTreeOk = true;

		} catch (GWPortalException e) {
			// error!! enable "retry" button on UI
			LOGGER.error("Empty Service tree found. Enabling \"retry\" button on UI in createTreeModels(): " + e.getMessage(), e);
			serviceTreeOk = false;
		}
		catch (Exception e) {
			LOGGER.error("Error processing ServiceGroup tree:" + e.getMessage(), e);
			hostTreeOk = false;
		}
	}

	/**
	 * get model
	 * 
	 * @return DefaultTreeModel
	 */
	public DefaultTreeModel getHostModel() {
		return hostTree.getModel();
	}

	/**
	 * @return DefaultTreeModel
	 */
	public DefaultTreeModel getServiceModel() {
		return serviceTree.getModel();
	}

	/**
	 * @return DefaultTreeModel
	 */
	public DefaultTreeModel getCustomHGModel() {
		return customHGTree.getModel();
	}

	/**
	 * @return DefaultTreeModel
	 */
	public DefaultTreeModel getCustomSGModel() {
		return customSGTree.getModel();
	}

	/**
	 * Method called when a node is clicked
	 * 
	 * @param event
	 */
	public void myNodeClicked(ActionEvent event) {

		Tree tree = (Tree) event.getSource();
		DefaultMutableTreeNode node = tree.getNavigatedNode();
		NetworkObject userObject = (NetworkObject) node.getUserObject();

		if (userObject.isExpanded()) {
			userObject.refresh();
		}
	}

	/**
	 * *CURRENTLY USED*
	 * 
	 * This method is called on click of "retry" button of Host Tree UI.
	 * 
	 * @param event
	 */
	public void rebuildTrees(ActionEvent event) {
		LOGGER.info("Rebuilding trees in rebuildTrees()");
		referenceTreeModel.rebuildModel();
		createTreeModels();
	}

	/**
	 * Sets the hostTreeOk.
	 * 
	 * @param hostTreeOk
	 *            the hostTreeOk to set
	 */
	public void setHostTreeOk(boolean hostTreeOk) {
		this.hostTreeOk = hostTreeOk;
	}

	/**
	 * Returns the hostTreeOk.
	 * 
	 * @return the hostTreeOk
	 */
	public boolean isHostTreeOk() {
		return hostTreeOk;
	}

	/**
	 * Sets the serviceTreeOk.
	 * 
	 * @param serviceTreeOk
	 *            the serviceTreeOk to set
	 */
	public void setServiceTreeOk(boolean serviceTreeOk) {
		this.serviceTreeOk = serviceTreeOk;
	}

	/**
	 * Returns the serviceTreeOk.
	 * 
	 * @return the serviceTreeOk
	 */
	public boolean isServiceTreeOk() {
		return serviceTreeOk;
	}

	/**
	 * Call back method for JMS.
	 * 
	 * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
	 */
	@Override
	public void refresh(String xml) {

	}

	/**
	 * @return ReferenceTreeMetaModel
	 */
	public ReferenceTreeMetaModel getReferenceTreeModel() {
		return referenceTreeModel;
	}

	/**
	 * @param referenceTreeModel
	 */
	public void setReferenceTreeModel(ReferenceTreeMetaModel referenceTreeModel) {
		this.referenceTreeModel = referenceTreeModel;
	}

	/**
	 * Sets the index.
	 * 
	 * @param index
	 *            the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Returns the index.
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the treeHiddenField.
	 * 
	 * @param treeHiddenField
	 *            the treeHiddenField to set
	 */
	public void setTreeHiddenField(String treeHiddenField) {
		this.treeHiddenField = treeHiddenField;
	}

	/**
	 * Returns the treeHiddenField.
	 * 
	 * @return the treeHiddenField
	 */
	public String getTreeHiddenField() {
		if (isIntervalRender()) {

			NetworkObjectTree preHostTree = hostTree;
			NetworkObjectTree preServiceTree = serviceTree;
			// NetworkObjectTree preHGCustomTree = customHGTree;
			// NetworkObjectTree preSGCustomTree = customSGTree;
			createTreeModels();
			if (firstRender) {
				// This code will run only on 1st render of tree.
				// If Sub page then restore the state. Read the tree path from
				// the portlet request and restore the tree

				FacesContext facesContext = FacesContext.getCurrentInstance();
				if (facesContext != null) {
					ExternalContext externalContext = facesContext
							.getExternalContext();
					PortletRequest portletRequest = (PortletRequest) externalContext
							.getRequest();
					Object pathObj = portletRequest
							.getAttribute(IPCHandlerConstants.SV_PATH_ATTRIBUTE);
					String targetView = Constant.HOSTGROUPVIEW;

					String nodeView = (String) portletRequest
							.getAttribute(IPCHandlerConstants.SV_NODE_TYPE_ATTRIBUTE);
					if (null != nodeView) {
						targetView = nodeView;
					}

					if (pathObj != null) {
						subPagePath = (String) pathObj;
						restoreTreeStateForSubpage(subPagePath, targetView);
					} // end if
				}
				firstRender = false;
				setIntervalRender(false);
				return Constant.EMPTY_STRING;

			}
			setIntervalRender(false);

			// restore Host tree
			if (preHostTree != null) {
				restoreTree(preHostTree.getRootTreeNode(),
						hostTree.getRootTreeNode());
			} // end if
			if (preServiceTree != null) {
				restoreTree(preServiceTree.getRootTreeNode(),
						serviceTree.getRootTreeNode());
			} // end if
			/*
			 * if (preHGCustomTree != null) {
			 * restoreTree(preHGCustomTree.getRootTreeNode(), customHGTree
			 * .getRootTreeNode()); } // end if if (preSGCustomTree != null) {
			 * restoreTree(preSGCustomTree.getRootTreeNode(), customSGTree
			 * .getRootTreeNode()); } // end if
			 */} else {
			// if Subpage then restore the state.
			// Read the tree path from the session and restore the
			// tree
			StateController stateController = new StateController();
			Object objTabPressed = stateController
					.getSessionAttribute(IPCHandlerConstants.SV_TAB_PRESSED_ATTRIBUTE);
			if (objTabPressed != null
					&& ((String) objTabPressed).equals("true")) {
				Object pathObj = stateController
						.getSessionAttribute(IPCHandlerConstants.SV_PATH_ATTRIBUTE);
				String targetView = Constant.HOSTGROUPVIEW;

				String nodeView = (String) stateController
						.getSessionAttribute(IPCHandlerConstants.SV_NODE_TYPE_ATTRIBUTE);
				if (null != nodeView) {
					targetView = nodeView;
				}
				// For the status viewer page path is always null
				if (pathObj != null) {
					subPagePath = (String) pathObj;
					restoreTreeStateForSubpage(subPagePath, targetView);
				} // end if
				stateController.addSessionAttribute(
						IPCHandlerConstants.SV_TAB_PRESSED_ATTRIBUTE, "false",
						true);
			} // end if
		} // end if

		return treeHiddenField;
	}

	/**
	 * @param source
	 * @param target
	 */
	private void restoreTree(DefaultMutableTreeNode source,
			DefaultMutableTreeNode target) {
		Enumeration<DefaultMutableTreeNode> sourceChildren = source.children();
		while (sourceChildren.hasMoreElements()) {
			DefaultMutableTreeNode child = sourceChildren.nextElement();
			if (((NetworkObject) child.getUserObject()).isExpanded()) {
				Enumeration<DefaultMutableTreeNode> targetChildren = target
						.children();
				while (targetChildren.hasMoreElements()) {
					DefaultMutableTreeNode targetChild = targetChildren
							.nextElement();
					NetworkObject targetChildUserObject = (NetworkObject) targetChild
							.getUserObject();
					if (((NetworkObject) child.getUserObject()).getText()
							.equals(targetChildUserObject.getText())) {
						targetChildUserObject.setExpanded(true);
						targetChildUserObject.refresh();
						// call recursively, but ONLY for host group, service
						// group and custom groups since only
						// they can have multilevel expandable objects.
						if (targetChildUserObject.getNodeType() == NodeType.HOST_GROUP
								|| targetChildUserObject.getNodeType() == NodeType.SERVICE_GROUP
								|| targetChildUserObject.getNodeType() == NodeType.CUSTOM_GROUP) {
							restoreTree(child, targetChild);
						} // end if
					} // node matches
				} // while
			}
		}
	}

	/**
	 * Method called when user resizes tree portlet
	 * 
	 * @param event
	 */
	public void setTreeWidth(ActionEvent event) {
		UIInput component = ((UIInput) FacesContext.getCurrentInstance()
				.getViewRoot().findComponent("hiddenForm:TreeIpHiddn"));
		Integer value = Integer.parseInt((String) component.getValue());
		UIHistoryBean.addTreePortletWidth(FacesUtils.getLoggedInUser(), value);
		treePortletWidth = value;
		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
				"initTreeResize();");
	}

	public int getTreeWidth() {
		return treePortletWidth;
	}

	/**
	 * // * (non-Javadoc) // * // * @see
	 * com.icesoft.faces.component.paneltabset.
	 * TabChangeListener#processTabChange
	 * (com.icesoft.faces.component.paneltabset.TabChangeEvent) //
	 */
	// public void processTabChange(TabChangeEvent tabChangeEvent)
	// throws AbortProcessingException {
	// setIndex(tabChangeEvent.getNewTabIndex());
	// }
	/**
	 * Sets the hostTreeRestricted.
	 * 
	 * @param hostTreeRestricted
	 *            the hostTreeRestricted to set
	 */
	public void setHostTreeRestricted(boolean hostTreeRestricted) {
		this.hostTreeRestricted = hostTreeRestricted;
	}

	/**
	 * Returns the hostTreeRestricted.
	 * 
	 * @return the hostTreeRestricted
	 */
	public boolean isHostTreeRestricted() {
		return hostTreeRestricted;
	}

	/**
	 * Sets the serviceTreeRestricted.
	 * 
	 * @param serviceTreeRestricted
	 *            the serviceTreeRestricted to set
	 */
	public void setServiceTreeRestricted(boolean serviceTreeRestricted) {
		this.serviceTreeRestricted = serviceTreeRestricted;
	}

	/**
	 * Returns the serviceTreeRestricted.
	 * 
	 * @return the serviceTreeRestricted
	 */
	public boolean isServiceTreeRestricted() {
		return serviceTreeRestricted;
	}
}
