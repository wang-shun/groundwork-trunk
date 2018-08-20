package com.groundworkopensource.portal.statusviewer.bean.tree;

import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.icesoft.faces.component.tree.IceUserObject;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class represents basic object in network Tree.
 * 
 * All the nodes like Hosts, Services etc. extend directly or indirectly, this
 * node. this class extends "IceUserObject", which is basic lightweight entity
 * that represents icefaces network node.
 * 
 * @author nitin_jadhav
 */

public abstract class NetworkObject extends IceUserObject {

    /**
     * Display text of node object
     */
    private String displayText;

    /**
     * Id of node object
     */
    private Integer objectId;

    /**
     * Type of network node
     */
    private NodeType nodeType;

    /**
     * Status of network node
     */
    private NetworkObjectStatusEnum status;

    /**
     * tool tip to show when mouse hours over Aggregated service status for host
     */
    private String toolTip;

    /**
     * is this a root node?
     */
    private boolean root = false;

    /**
     * URL of this node.
     */
    private String url;

    /**
     * parent Information
     */
    private String parentInfo;

    // /** logger. */
    // private static final Logger LOGGER = Logger.getLogger(NetworkObject.class
    // .getName());

    /**
     * @return NetworkObjectStatusEnum
     */
    public NetworkObjectStatusEnum getStatus() {
        return status;
    }

    /**
     * Set status of node based on provided NetworkObjectStatusEnum
     * 
     * @param status
     */
    public void setStatus(NetworkObjectStatusEnum status) {
        this.status = status;
        setIcons();
    }

    /**
     * Constructor
     * 
     * @param wrapper
     */
    public NetworkObject(DefaultMutableTreeNode wrapper) {
        super(wrapper);
    }

    /**
     * refreshes this node and/or children of this node. listen extending
     * classes : implementation is must!
     */
    public abstract void refresh();

    /**
     * refreshes this node and/or children of this node.Required for Server PUSH
     * classes : implementation is must!
     * 
     * @param referenceTreeModel
     */
    public abstract void refresh(ReferenceTreeMetaModel referenceTreeModel);

    /**
     * Sets icon for node, based on its "status"
     */
    private void setIcons() {
        /*
         * logger .info("Status Host/Service/Group status:  " +
         * status.getStatus());
         */
        // Clean this stuff // Root cause for multiple managed bean
        // instantiation..
        String iconPath = status.getIconPath();
        setLeafIcon(iconPath);
        setBranchContractedIcon(iconPath);
        setBranchExpandedIcon(iconPath);
    }

    /**
     * add the argument node as child to current node
     * 
     * @param branchNode
     */
    public void addAsChild(DefaultMutableTreeNode branchNode) {
        getWrapper().add(branchNode);
    }

    /**
     * @param nodeType
     *            the nodeType to set
     */
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Sets if this is root node or not.
     * 
     * @param root
     *            the root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }

    /**
     * Returns true if this is root node.
     * 
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * Sets the url.
     * 
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the url.
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets toolTip
     * 
     * @param toolTip
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    /**
     * Returns toolTip
     * 
     * @return toolTip
     */
    public String getToolTip() {
        return toolTip;
    }

    /**
     * Sets the objectId.
     * 
     * @param objectId
     *            the objectId to set
     */
    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns the objectId.
     * 
     * @return the objectId
     */
    public Integer getObjectId() {
        return objectId;
    }


    /**
     * Return the display text.
     *
     * @return display text
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     * Set the display text.
     *
     * @param displayText display text
     */
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     * Sets the parentInfo.
     * 
     * @param parentInfo
     *            the parentInfo to set
     */
    public void setParentInfo(String parentInfo) {
        this.parentInfo = parentInfo;
    }

    /**
     * Returns the parentInfo.
     * 
     * @return the parentInfo
     */
    public String getParentInfo() {
        // javax.swing.tree.TreeNode[] treeNodes = this.getWrapper().getPath();
        // for (javax.swing.tree.TreeNode treeNode : treeNodes) {
        // parentInfo = parentInfo + " " + treeNode.toString();
        // }
        // LOGGER.error("PARENT INFOOOOOOOOOOOOOOOOO : " + parentInfo);
        return parentInfo;
    }

}
