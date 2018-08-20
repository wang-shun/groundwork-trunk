package com.groundworkopensource.portal.statusviewer.bean.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

/**
 * This class represents service object in network Tree.
 * 
 * All the nodes like Hosts, Services etc. extend directly or indirectly, this
 * node. this class extends "IceUserObject", which is basic lightweight entity
 * that represents icefaces network node.
 * 
 * @author nitin_jadhav
 */

public class ServiceObject extends NetworkObject {

    /**
     * node name parameter
     */
    private String nodeNameParam;

    /**
     * constructor
     * 
     * @param wrapper
     * @param service
     * @param parent
     * @param maxLength
     * @param appendHostName
     */
    public ServiceObject(DefaultMutableTreeNode wrapper,
            NetworkMetaEntity service, String parent, boolean appendHostName) {
        super(wrapper);
        setLeaf(true);

        if (appendHostName) {
            setText(service.getExtendedName());
        } else {
            setText(service.getName());
        }
        setNodeNameParam(service.getName());
        setObjectId(service.getObjectId());
        setNodeType(service.getType());
        setStatus(service.getStatus());
        setToolTip(service.getToolTip());
        setParentInfo(parent);
        setUrl(NodeURLBuilder.buildNodeURL(service.getType(), service
                .getObjectId(), service.getName(), parent));
    }

    /**
     * Now how should we expand service? Currently, we can't. But in future, we
     * may expand service to something more detailed.
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
     */
    @Override
    public void refresh() {
        // currently, refreshing not supported.
    }

    /**
     * Now how should we expand service? Currently, we can't. But in future, we
     * may expand service to something more detailed.
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
     */
    @Override
    public void refresh(ReferenceTreeMetaModel referenceTree) {
        // currently, refreshing not supported.
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
