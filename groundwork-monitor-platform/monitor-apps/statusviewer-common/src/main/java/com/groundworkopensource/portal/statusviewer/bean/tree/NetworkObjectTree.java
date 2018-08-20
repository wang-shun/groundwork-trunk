package com.groundworkopensource.portal.statusviewer.bean.tree;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import org.apache.log4j.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Network Host and Service Tree, that represents tree model and provides
 * DefaultTreemodel for displaying on GUI.
 * 
 * @author nitin_jadhav
 * 
 */
public class NetworkObjectTree {

	/**
	 * Name of the tree
	 */
	private NodeType treeType;

	/**
	 * tree default model, used as a value for the tree component
	 */
	private final DefaultTreeModel model;

	/**
	 * root node of tree
	 */
	private DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();

	/**
	 * object reference used to delete and copy the node
	 */
	private NetworkObject selectedNodeObject = null;

	/**
	 * reference model for building tree
	 */
	private ReferenceTreeMetaModel referenceTreeModel;

	/**
	 * used to pass for creating root node
	 */
	private static final int ARBITORY_LARGE_NUMBER = 100;
	/**
	 * UserExtendedRoleBean
	 */
	private UserExtendedRoleBean userExtendedRoleBean;

	private List<Integer> rootHGCustomGroups = new ArrayList<Integer>();

	private List<Integer> rootSGCustomGroups = new ArrayList<Integer>();

	private List<String> assignedHostGroups = new ArrayList<String>();
	private List<String> assignedServiceGroups = new ArrayList<String>();
	
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(NetworkObjectTree.class.getName());

	/**
	 * constructor
	 * 
	 * @param nodeType
	 * @param referenceTreeModel
	 * @throws GWPortalException
	 */
	public NetworkObjectTree(NodeType nodeType,
			ReferenceTreeMetaModel referenceTreeModel) throws GWPortalException {
		this.treeType = nodeType;

		if (referenceTreeModel == null) {
			this.referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
					.getManagedBean(Constant.REFERENCE_TREE);
		} else {
			this.referenceTreeModel = referenceTreeModel;
		}

		// get the userExtendedRoleBean managed instance
		userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();
		// create dummy NetworkHostGroup for root node set other props if
		// required, status for entire network is not applicable.

		String rootNodeText = ResourceUtils
				.getLocalizedMessage("com_groundwork_portal_statusviewer_networktree_entireNetworkText");
		NetworkMetaEntity rootEntity = new NetworkMetaEntity(0, null, rootNodeText, null,
				NetworkObjectStatusEnum.ENTIRE_NETWORK_STATUS,
				NodeType.NETWORK, null, null, null);
		List<NetworkMetaEntity> rootCustomGroups = referenceTreeModel.getRootCustomGroups();
		for (NetworkMetaEntity rootCustomGroup : rootCustomGroups) {
            // save host and service group custom roots
            NodeType rootConcreteType = referenceTreeModel.checkConcreteEntityType(rootCustomGroup);
            if ((treeType == NodeType.HOST_GROUP) && (rootConcreteType == NodeType.HOST_GROUP)) {
                rootHGCustomGroups.add(rootCustomGroup.getObjectId());
            } else if ((treeType == NodeType.SERVICE_GROUP) && (rootConcreteType == NodeType.SERVICE_GROUP)) {
                rootSGCustomGroups.add(rootCustomGroup.getObjectId());
            }
            // create placeholder custom group
            NetworkCustomGroup rootObject = new NetworkCustomGroup(rootTreeNode, rootEntity);
            rootObject.setRoot(true);
            rootObject.setExpanded(true);
            // There is no URL for the custom groups
            // rootObject.setUrl(NodeURLBuilder.getBaseURL());
            rootTreeNode.setUserObject(rootObject);
		}

		if (rootCustomGroups == null || rootCustomGroups.size() == 0
				|| rootHGCustomGroups.size() == 0
				|| rootSGCustomGroups.size() == 0) {
			if (treeType == NodeType.HOST_GROUP) {
				// create root node with its children expanded. For all trees,
				// the
				// root
				// object will be NetworkHostGroup.
				NetworkHostGroup rootObject = new NetworkHostGroup(
						rootTreeNode, rootEntity);
				rootObject.setRoot(true);
				rootObject.setExpanded(true);
				// set the url as "base URL" for this "Entire Network" root
				// node.
				rootObject.setUrl(NodeURLBuilder.getBaseURL());
				rootTreeNode.setUserObject(rootObject);
				// model is accessed by by the ice:tree component
			}

			if (treeType == NodeType.SERVICE_GROUP) {
				// create root node with its children expanded. For all trees,
				// the
				// root
				// object will be NetworkServiceGroup.
				NetworkServiceGroup rootObject = new NetworkServiceGroup(
						rootTreeNode, rootEntity);
				rootObject.setRoot(true);
				rootObject.setExpanded(true);
				// set the url as "base URL" for this "Entire Network" root
				// node.
				rootObject.setUrl(NodeURLBuilder.getBaseURL());
				rootTreeNode.setUserObject(rootObject);
			}
		}

		model = new DefaultTreeModel(rootTreeNode);
		// add children groups to root
		addChildGroupsToRoot();

	}

	/**
	 * We will add child nodes, depending on tree type under the root node. In
	 * host tree, there will be host groups. Under service tree, there will be
	 * service group nodes.
	 * 
	 * If No node found, it throws GWPortalException.
	 * 
	 * @throws GWPortalException
	 */
	private void addChildGroupsToRoot() throws GWPortalException {
		Iterator<NetworkMetaEntity> childGroupIterator = null;

		if (treeType == NodeType.HOST_GROUP) {

			synchronized (rootHGCustomGroups) {
				for (int i = 0; i < rootHGCustomGroups.size(); i++) {
					NetworkMetaEntity custGroup = referenceTreeModel.getCustomGroupById(rootHGCustomGroups.get(i));
					if (custGroup != null) {
						DefaultMutableTreeNode node = this
								.addCGClonedUserObject(null, custGroup);
						addChildGroupToRoot(node, custGroup, i);
						// If custom group doesn't have any children then dont even render
						if (node.getChildCount() <= 0) {
							node.removeFromParent();
						}
 
					} // end if
				}

			}

			if (userExtendedRoleBean != null) {
				childGroupIterator = referenceTreeModel
						.getExtRoleHostGroups(userExtendedRoleBean
								.getExtRoleHostGroupList());
			} else {
				childGroupIterator = referenceTreeModel.getAllHostGroups();
			}
			
			// synchronized block
			synchronized (childGroupIterator) {
				// add nodes one-by-one
				int i = 0;
				while (childGroupIterator.hasNext()) {
					NetworkMetaEntity child = childGroupIterator.next();
					if (!assignedHostGroups.contains(child.getName()))
						addChildGroupToRoot(null, child, i);
					i++;
				}
			}

		} else if (treeType == NodeType.SERVICE_GROUP) {

			synchronized (rootSGCustomGroups) {
				for (int i = 0; i < rootSGCustomGroups.size(); i++) {
					NetworkMetaEntity custGroup = referenceTreeModel.getCustomGroupById(rootSGCustomGroups.get(i));
					if (custGroup != null) {
						DefaultMutableTreeNode node = this
								.addCGClonedUserObject(null, custGroup);
						addChildGroupToRoot(node, custGroup, i);
						// If custom group doesn't have any children then dont even render
						if (node.getChildCount() <= 0) {
							node.removeFromParent();
						}
					} // end if
				}

			}

			if (userExtendedRoleBean != null) {
				childGroupIterator = referenceTreeModel
						.getExtRoleServiceGroups(userExtendedRoleBean
								.getExtRoleServiceGroupList());
			} else {
				childGroupIterator = referenceTreeModel.getAllServiceGroups();
			}
			
			// synchronized block
			synchronized (childGroupIterator) {
				// add nodes one-by-one
				int i = 0;
				while (childGroupIterator.hasNext()) {
					NetworkMetaEntity child = childGroupIterator.next();
					if (!assignedServiceGroups.contains(child.getName()))
						addChildGroupToRoot(null, child, i);
					i++;
				}
			}
		}

	}

	/**
	 * Adds child group node to root can be accessed fromm out side, dont make
	 * this method private
	 * 
	 * @param group
	 * @param index
	 */
	public void addChildGroupToRoot(DefaultMutableTreeNode node,
			NetworkMetaEntity group, int index) {

		NetworkObject branchObject = null;

		List<Integer> children = group.getChildNodeList();
		int count = 0;
		for (Integer groupId : children) {
			NetworkMetaEntity childHGGroup = referenceTreeModel
					.getHostGroupById(groupId);
			NetworkMetaEntity childSGGroup = referenceTreeModel
					.getServiceGroupById(groupId);
			NetworkMetaEntity childCusGroup = referenceTreeModel
					.getCustomGroupById(groupId);
			DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
			if (group.getType() == NodeType.HOST_GROUP) {
				// if childHGGroup is null then it is not rendering the custom
				// group so dont add it to the assigned group. Infact we are
				// iterating thru the assignedgroups
				if (group.isCustom()
						&& childHGGroup != null) { //Fix for GWMON-10798
						if (referenceTreeModel.getExtendedRoleHostGroupList()
								.isEmpty() || referenceTreeModel
								.getExtendedRoleHostGroupList().contains(
										childHGGroup.getName())) {
							LOGGER.debug("HG========>" + childHGGroup.getName());
							assignedHostGroups.add(childHGGroup.getName());
							branchObject = new NetworkHostGroup(clonedWrapper,
									childHGGroup);
							branchObject.setLeaf(false);
							clonedWrapper.setUserObject(branchObject);
							if (node != null)
								node.add(clonedWrapper);
							else
								rootTreeNode.add(clonedWrapper);
							
						}
				} else {
					// if there is no custom group, attach host group to root
					// and break.
					if (referenceTreeModel.getExtendedRoleHostGroupList()
							.isEmpty() || referenceTreeModel
							.getExtendedRoleHostGroupList().contains(
									group.getName())) {
						LOGGER.debug("H========>" + group.getName());
						branchObject = new NetworkHostGroup(clonedWrapper, group);
						branchObject.setLeaf(false);
						clonedWrapper.setUserObject(branchObject);
						if (node != null)
							node.add(clonedWrapper);
						else
							rootTreeNode.add(clonedWrapper);
						break;
					}
				}

			} else if (group.getType() == NodeType.SERVICE_GROUP) {
				// if childSGGroup is null then it is not rendering the custom
				// group so dont add it to the assigned group. Infact we are
				// iterating thru the assignedgroups
				if (group.isCustom()
						&& childSGGroup != null) { //Fix for GWMON-10798
						if (referenceTreeModel
								.getExtendedRoleServiceGroupList().isEmpty() || referenceTreeModel
								.getExtendedRoleServiceGroupList().contains(
										childSGGroup.getName())) {
							LOGGER.debug("SG========>" + childSGGroup.getName());
							assignedServiceGroups.add(childSGGroup.getName());
							branchObject = new NetworkServiceGroup(clonedWrapper,
									childSGGroup);
		
							branchObject.setLeaf(false);
							clonedWrapper.setUserObject(branchObject);
							if (node != null)
								node.add(clonedWrapper);
							else
								rootTreeNode.add(clonedWrapper);
						}
				} else {
					// if there is no custom group, attach service group to root
					// and break.
					if (referenceTreeModel
							.getExtendedRoleServiceGroupList().isEmpty() || referenceTreeModel
							.getExtendedRoleServiceGroupList().contains(
									group.getName())) {
						LOGGER.debug("S========>" + group.getName());
						branchObject = new NetworkServiceGroup(clonedWrapper, group);
						branchObject.setLeaf(false);
						clonedWrapper.setUserObject(branchObject);
						if (node != null)
							node.add(clonedWrapper);
						else
							rootTreeNode.add(clonedWrapper);
						break;
					}
				}

			} else {
				LOGGER.debug("CG========>" + group.getName());
				// if childCusGroup is null then is is not rendering the custom
				// group or its children so dont add it to the assigned group.
				if (childCusGroup != null) {
					DefaultMutableTreeNode childNode = this.addCGClonedUserObject(
							node, childCusGroup);
					addChildGroupToRoot(childNode, childCusGroup, count);
				}
			} // end if

			// count++;
		}

	}

	/**
	 * add custom group cloned user object
	 * 
	 * @param parent
     * @param group
	 */
	private DefaultMutableTreeNode addCGClonedUserObject(
			DefaultMutableTreeNode parent, NetworkMetaEntity group) {
		LOGGER.debug("====>" + group.getName());
		DefaultMutableTreeNode clonedWrapper = new DefaultMutableTreeNode();
		NetworkCustomGroup clonedUserObject = new NetworkCustomGroup(
				clonedWrapper, group);

		clonedUserObject.setText(group.getName());
        clonedUserObject.setDisplayText(group.getPrefixedName());
		clonedUserObject.setLeaf(false);
		clonedWrapper.setUserObject(clonedUserObject);
		// finally add the node to the parent.
		if (parent != null) {
			parent.add(clonedWrapper);
		} else
			rootTreeNode.add(clonedWrapper);
		return clonedWrapper;
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
	 * Gets the tree node.
	 * 
	 * @return the tree node
	 */
	public NetworkObject getSelectedNodeObject() {
		return selectedNodeObject;
	}

	/**
	 * Sets the tree node.
	 * 
	 * @param selectedNodeObject
	 *            the new tree node
	 */
	public void setSelectedNodeObject(NetworkObject selectedNodeObject) {
		this.selectedNodeObject = selectedNodeObject;
	}

	/**
	 * @return NodeType
	 */
	public NodeType getTreeType() {
		return treeType;
	}

	/**
	 * @param treeType
	 */
	public void setTreeType(NodeType treeType) {
		this.treeType = treeType;
	}

	/**
	 * Returns Root Tree Node.
	 * 
	 * @return DefaultMutableTreeNode
	 */
	public DefaultMutableTreeNode getRootTreeNode() {
		return rootTreeNode;
	}

	/**
	 * Updates desired host group in tree
	 * 
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	public void updateGroup(int id) {
		NetworkMetaEntity group = null;
		if (treeType == NodeType.HOST_GROUP) {
			group = referenceTreeModel.getHostGroupById(Integer.valueOf(id));
		} else if (treeType == NodeType.SERVICE_GROUP) {
			group = referenceTreeModel.getServiceGroupById(Integer.valueOf(id));
		} // end if
		if (group != null) {
			Enumeration<DefaultMutableTreeNode> children = rootTreeNode
					.children();
			boolean objectFound = false;
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode element = children.nextElement();
				NetworkObject object = (NetworkObject) element.getUserObject();

				if (object.getObjectId() == id) {
					objectFound = true;
					// this is the group, we need to update!!
					int index = rootTreeNode.getIndex(element);

					synchronized (rootTreeNode) {
						if (object.getText().equalsIgnoreCase(group.getName())) {
							element.removeFromParent();
							addChildGroupToRoot(null, group, index);
						} else {
							element.removeFromParent();
							addChildGroupToRoot(group);
						}
					}
					break;
				} // end if
			}
			// If object not found in the tree, it is a new object to be added
			// to the end.
			if (!objectFound) {
				addChildGroupToRoot(group);
			} // end if
		} else {
			removeGroup(id);
		}
	}

	/**
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	public void removeGroup(int id) {

		// entity not found, delete
		Enumeration<DefaultMutableTreeNode> children = rootTreeNode.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode element = children.nextElement();
			NetworkObject object = (NetworkObject) element.getUserObject();

			if (object.getObjectId() == id) {
				synchronized (rootTreeNode) {
					element.removeFromParent();
				}
				break;
			} // end if
		}

	}

	/**
	 * add group child node to UNSPECIFIED position in tree below root
	 * 
	 * @param group
	 */
	@SuppressWarnings("unchecked")
	private void addChildGroupToRoot(NetworkMetaEntity group) {
		Enumeration<DefaultMutableTreeNode> children = rootTreeNode.children();
		int i = 0;
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode node = children.nextElement();
			NetworkObject userObject = (NetworkObject) node.getUserObject();
			if (group.getName().compareToIgnoreCase(userObject.getText()) <= 0) {
				addChildGroupToRoot(null, group, i);
				return;
			}
			i++;
		}
		addChildGroupToRoot(null, group, i);
	}
}
