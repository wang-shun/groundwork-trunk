package com.groundworkopensource.portal.statusviewer.bean.tree;

import java.util.Iterator;

import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

/**
 * This class represents host object in network Tree.
 * 
 * All the nodes like Hosts, Services etc. extend directly or indirectly, this
 * node. this class extends "IceUserObject", which is basic lightweight entity
 * that represents icefaces network node.
 * 
 * @author nitin_jadhav
 */
public class HostObject extends NetworkObject {

    /**
     * Reference to reference model
     */
    private ReferenceTreeMetaModel referenceTreeModel;

    /**
     * wrapper
     */
    private DefaultMutableTreeNode wrapper;

    /**
     * parentArray
     */
    private String parents = Constant.EMPTY_STRING;

    /**
     * Constructor
     * 
     * @param branchNode
     * @param host
     * @param parent
     * @param maxLength
     */
    public HostObject(DefaultMutableTreeNode branchNode,
            NetworkMetaEntity host, String parent) {
        super(branchNode);
        this.wrapper = branchNode;
        setText(host.getName());
        setObjectId(host.getObjectId());
        setStatus(host.getStatus());
        setNodeType(host.getType());
        setExpanded(false);
        setToolTip(host.getToolTip());
        setParentInfo(parent);

        this.parents = parent;
        setUrl(NodeURLBuilder.buildNodeURL(host.getType(), host.getObjectId(),
                host.getName(), parent));
    }

    /**
     * Called when a node is expanded. fetches children (services) for node from
     * tree reference model.
     * 
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
     */
    @Override
    public void refresh() {
        if (wrapper.getChildCount() == 0) {
            // remove all existing children

            if (referenceTreeModel == null
                    && FacesContext.getCurrentInstance() != null) {
                referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                        .getManagedBean(Constant.REFERENCE_TREE);
            }
            Iterator<Integer> serviceEntities = referenceTreeModel
                    .getServicesUnderHost(getObjectId());
            if (serviceEntities != null) {
                while (serviceEntities.hasNext()) {
                    NetworkMetaEntity service = referenceTreeModel
                            .getServiceById(serviceEntities.next());
                    if (service != null) {
                        DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();

                        ServiceObject serviceObject = new ServiceObject(
                                branchNode, service, new StringBuilder(parents)
                                        .append(Constant.COMMA).append(
                                                getText()).toString(), false);
                        branchNode.setUserObject(serviceObject);
                        addAsChild(branchNode);
                    }
                }
            }

            // synchronized (wrapper) {
            // wrapper.removeAllChildren();
            // if (this.parentArray != null) {
            // this.parentArray.add(getText());
            // }
            // for (NetworkMetaEntity service : servicesUnderHost) {
            // DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
            //
            // ServiceObject serviceObject = new ServiceObject(branchNode,
            // service, parentArray,
            // Constant.NODE_LEVEL3_HOST_TEXT_MAXLENGTH);
            // branchNode.setUserObject(serviceObject);
            // addAsChild(branchNode);
            // serviceObject = null;
            // }
            // }
        }
    }

    /**
     * Called when a node is expanded. fetches children (services) for node from
     * tree reference model.
     * 
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
     */
    @Override
    public void refresh(ReferenceTreeMetaModel referenceTreeModel) {
        // remove all existing children
        this.referenceTreeModel = referenceTreeModel;
        refresh();
    }
}
