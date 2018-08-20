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
 * This class represents host group object in network Tree.
 * 
 * All the nodes like Hosts, Services etc. extend directly or indirectly, this
 * node. this class extends "IceUserObject", which is basic lightweight entity
 * that represents icefaces network node.
 * 
 * @author nitin_jadhav
 */

public class NetworkHostGroup extends NetworkObject {

    /**
     * Reference to reference model
     */
    private ReferenceTreeMetaModel referenceTreeModel;

    /**
     * wrapper
     */
    private DefaultMutableTreeNode wrapper;

    /**
     * Constructor
     * 
     * @param wrapper
     * @param hostGroup
     */
    public NetworkHostGroup(DefaultMutableTreeNode wrapper,
            NetworkMetaEntity hostGroup) {
        super(wrapper);
        this.wrapper = wrapper;

        setText(hostGroup.getName());
        setDisplayText(hostGroup.getPrefixedName());
        setObjectId(hostGroup.getObjectId());
        setNodeType(hostGroup.getType());
        setExpanded(false);
        setStatus(hostGroup.getStatus());
        setToolTip(hostGroup.getToolTip());
        // no parent needed
        setUrl(NodeURLBuilder.buildNodeURL(hostGroup.getType(), hostGroup
                .getObjectId(), hostGroup.getName(), null));
    }

    /**
     * Called when a node is expanded. fetches children (hosts) for node from
     * reference model.
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.tree.NetworkObject#refresh()
     */
    @Override
    public void refresh() {
        if (getObjectId().intValue() != 0 && wrapper.getChildCount() == 0) {
            // LOGGER.debug("fetching nodes for child, objId: " + getObjectId()
            // + " wrapper.getChildCount() = " + wrapper.getChildCount());

            if (referenceTreeModel == null
                    && FacesContext.getCurrentInstance() != null) {
                referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                        .getManagedBean(Constant.REFERENCE_TREE);
            }
            Iterator<Integer> hostsIterator = referenceTreeModel
                    .getHostsUnderHostGroup(getObjectId());
            // synchronized (wrapper) {
            // wrapper.removeAllChildren();
            while (hostsIterator != null && hostsIterator.hasNext()) {
                NetworkMetaEntity host = referenceTreeModel
                        .getHostById(hostsIterator.next());

                if (host != null) {
                    DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
                    HostObject hostObject = new HostObject(branchNode, host,
                            getText());

                    // LOGGER.debug("added host: " + hostObject.getFullName());
                    branchNode.setUserObject(hostObject);
                    addAsChild(branchNode);
                }
            }
            // }

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
}
