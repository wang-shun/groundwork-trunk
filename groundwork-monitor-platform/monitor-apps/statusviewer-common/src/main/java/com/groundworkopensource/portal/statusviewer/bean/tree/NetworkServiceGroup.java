package com.groundworkopensource.portal.statusviewer.bean.tree;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Iterator;

/**
 * This class represents service group object in network Tree.
 * 
 * All the nodes like Hosts, Services etc. extend directly or indirectly, this
 * node. this class extends "IceUserObject", which is basic lightweight entity
 * that represents icefaces network node.
 * 
 * @author nitin_jadhav
 */

public class NetworkServiceGroup extends NetworkObject {

    /**
     * reference to reference model
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
     * @param serviceGroup
     */
    public NetworkServiceGroup(DefaultMutableTreeNode wrapper,
            NetworkMetaEntity serviceGroup) {
        super(wrapper);
        this.wrapper = wrapper;
        setText(serviceGroup.getName());
        setDisplayText(serviceGroup.getPrefixedName());
        setObjectId(serviceGroup.getObjectId());
        setNodeType(serviceGroup.getType());
        setExpanded(false);
        setStatus(serviceGroup.getStatus());
        setToolTip(serviceGroup.getToolTip());
        setUrl(NodeURLBuilder.buildNodeURL(serviceGroup.getType(), serviceGroup
                .getObjectId(), serviceGroup.getName(), null));
        setNodeNameParam(serviceGroup.getName());
    }

    /**
     * Called when a node is expanded. fetches children (Services) for node from
     * tree reference model.
     * 
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
     */
    @Override
    public void refresh() {
        if (getObjectId().intValue() != 0 && wrapper.getChildCount() == 0) {
            if (referenceTreeModel == null
                    && FacesContext.getCurrentInstance() != null) {
                referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                        .getManagedBean(Constant.REFERENCE_TREE);
            }

            if (null == referenceTreeModel) {
                return;
            }
            Iterator<Integer> servicesIterator = referenceTreeModel
                    .getServicesUnderServiceGroup(getObjectId());
            if (servicesIterator != null) {
                // synchronized (wrapper) {
                // wrapper.removeAllChildren();
                while (servicesIterator.hasNext()) {
                    Integer serviceId = servicesIterator.next();
                    NetworkMetaEntity service = referenceTreeModel
                            .getServiceById(serviceId);
                    if (service != null) {
                        DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
                        ServiceObject serviceObject = new ServiceObject(
                                branchNode, service, getText(), true);
                        branchNode.setUserObject(serviceObject);
                        addAsChild(branchNode);
                    }
                }
                // }
            }
        }
    }

    /**
     * Called when a node is expanded. fetches children (Services) for node from
     * tree reference model.
     * 
     * (non-Javadoc)
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
