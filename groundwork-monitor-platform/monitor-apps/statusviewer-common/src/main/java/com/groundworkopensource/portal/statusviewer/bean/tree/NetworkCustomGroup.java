package com.groundworkopensource.portal.statusviewer.bean.tree;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class represents host group object in network Tree.
 * 
 * All the nodes like Hosts, Services etc. extend directly or indirectly, this
 * node. this class extends "IceUserObject", which is basic lightweight entity
 * that represents icefaces network node.
 * 
 * @author nitin_jadhav
 */

public class NetworkCustomGroup extends NetworkObject {

	/**
	 * Reference to reference model
	 */
	private ReferenceTreeMetaModel referenceTreeModel;

	/**
	 * wrapper
	 */
	private DefaultMutableTreeNode wrapper;

	/**
	 * node name parameter
	 */
	private String nodeNameParam;

	/**
	 * Constructor
	 * 
	 * @param wrapper
	 * @param customGroup
	 */
	public NetworkCustomGroup(DefaultMutableTreeNode wrapper,
			NetworkMetaEntity customGroup) {
		super(wrapper);
		this.wrapper = wrapper;

		setText(customGroup.getName());
        setDisplayText(customGroup.getPrefixedName());
		setObjectId(customGroup.getObjectId());
		setNodeType(customGroup.getType());
		setExpanded(false);
		setStatus(customGroup.getStatus());
		setToolTip(customGroup.getToolTip());
		setNodeNameParam(customGroup.getName());
		// no parent needed
		/*
		 * setUrl(NodeURLBuilder.buildNodeURL(customGroup.getType(),
		 * customGroup.getObjectId(), customGroup.getName(), null));
		 */
	}

	/**
	 * Called when a node is expanded. fetches children (hosts) for node from
	 * reference model.
	 * 
	 * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
	 */
	@Override
	public void refresh() {
		// TODO
		if (getObjectId().intValue() != 0 && wrapper.getChildCount() == 0) {

			if (referenceTreeModel == null
					&& FacesContext.getCurrentInstance() != null) {
				referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
						.getManagedBean(Constant.REFERENCE_TREE);
			}
			NetworkMetaEntity customGroup = referenceTreeModel
					.getCustomGroupById(getObjectId());

			java.util.List<Integer> children = customGroup.getChildNodeList();
			for (int i = 0; i < children.size(); i++) {
				DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
				if (customGroup.getType() == NodeType.HOST_GROUP) {
					NetworkMetaEntity hostGroup = referenceTreeModel
							.getHostGroupById(children.get(i));
					if (referenceTreeModel.getExtendedRoleHostGroupList()
							.isEmpty()
							|| referenceTreeModel
									.getExtendedRoleHostGroupList().contains(
											hostGroup.getName())) {
						NetworkHostGroup hostGroupObject = new NetworkHostGroup(
								branchNode, hostGroup);
						branchNode.setUserObject(hostGroupObject);
						addAsChild(branchNode);
					}
				}
				if (customGroup.getType() == NodeType.SERVICE_GROUP) {
					NetworkMetaEntity serviceGroup = referenceTreeModel
							.getServiceGroupById(children.get(i));
					if (referenceTreeModel.getExtendedRoleServiceGroupList()
							.isEmpty()
							|| referenceTreeModel
									.getExtendedRoleServiceGroupList()
									.contains(serviceGroup.getName())) {
						NetworkServiceGroup serviceGroupObject = new NetworkServiceGroup(
								branchNode, serviceGroup);
						branchNode.setUserObject(serviceGroupObject);
						addAsChild(branchNode);
					}
				}

			}
			// If custom group doesn't have any children then dont even render
			if (getWrapper().getChildCount() <= 0) {
				getWrapper().removeFromParent();
			}
		}
	}

	/**
	 * Called when a node is expanded. fetches children (hosts) for node from
	 * reference model.
	 * 
	 * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
	 */
	@Override
	public void refresh(ReferenceTreeMetaModel referenceTreeModel) {
		this.referenceTreeModel = referenceTreeModel;
		refresh();
	}

	/**
	 * Sets the nodeNameParam.
	 * 
	 * @param nodeNameParam
	 *            the nodeNameParam to set
	 */
	public void setNodeNameParam(String nodeNameParam) {
		this.nodeNameParam = nodeNameParam;
	}

	/**
	 * Returns the nodeNameParam.
	 * 
	 * @return the nodeNameParam
	 */
	public String getNodeNameParam() {
		return nodeNameParam;
	}
}
