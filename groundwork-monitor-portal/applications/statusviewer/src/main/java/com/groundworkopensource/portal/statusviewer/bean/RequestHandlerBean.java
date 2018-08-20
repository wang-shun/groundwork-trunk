/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.bean;

import java.util.List;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.bean.NavigationTabsetBean.Tab;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.handler.StateController;

/**
 * This bean is responsible to handle the request from outside the status viewer
 * 
 * @author manish_kjain
 * 
 */
public class RequestHandlerBean {

    // /**
    // * logger
    // */
    // private static final Logger LOGGER = Logger
    // .getLogger(RequestHandlerBean.class.getName());

    /**
     * StateController
     * 
     */
    private StateController stateController;
    /**
     * hiddenField is used to give reference on UI
     */
    private String hiddenField = "";
    /**
     * NavigationTabsetBean instance
     */
    private NavigationTabsetBean navigationTabsetBean = (NavigationTabsetBean) FacesUtils
            .getManagedBean("navigationTabsetBean");
    /**
     * NavigationRenderBean instance
     */
    private NavigationRenderBean navigationRenderBean = (NavigationRenderBean) FacesUtils
            .getManagedBean("navigationRenderBean");

    /**
     * userExtendedRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * ReferenceTreeMetaModel
     */
    private ReferenceTreeMetaModel referenceTreeModel = null;

    /**
     * 
     */
    public RequestHandlerBean() {
        stateController = new StateController();
        int nodeID;
        String nodeName;
        String selectedNodeView;
        // get node type
        NodeType nodeType = stateController.getSelectedNodeType();

        /*
         * check if its null. It will be null if request parameters have not
         * been specified.
         */
        if (null != nodeType) {
            /*
             * Status Viewer host / host group / service / service group subpage
             */

            nodeID = stateController.getSelectedNodeID();
            nodeType = stateController.getSelectedNodeType();
            nodeName = stateController.getSelectedNodeName();
            selectedNodeView = NodeType.getNodeViewByNodeType(nodeType);

            // check node parameters against the permitted extended user
            // role attributes

            // get the userExtendedRoleBean managed instance
            userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

            // return "" if Dashboard Links are Disabled as per the User Role.
            if (null != userExtendedRoleBean) {
                List<String> extRoleHostGroupList = userExtendedRoleBean
                        .getExtRoleHostGroupList();
                List<String> extRoleServiceGroupList = userExtendedRoleBean
                        .getExtRoleServiceGroupList();
                if (!extRoleHostGroupList.isEmpty()
                        || !extRoleServiceGroupList.isEmpty()) {
                    referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                            .getManagedBean(Constant.REFERENCE_TREE);
                    NetworkMetaEntity nodeForExtendedRolePermissions = referenceTreeModel
                            .getEntityByExtendedRolePermissions(nodeID,
                                    nodeType, nodeName, extRoleHostGroupList,
                                    extRoleServiceGroupList,
                                    userExtendedRoleBean.getDefaultHostGroup(),
                                    userExtendedRoleBean
                                            .getDefaultServiceGroup());

                    nodeID = nodeForExtendedRolePermissions.getObjectId();
                    nodeType = nodeForExtendedRolePermissions.getType();
                    nodeName = nodeForExtendedRolePermissions.getName();
                    selectedNodeView = NodeType.getNodeViewByNodeType(nodeType);
                }
            }

        } else {
            // get selected node view
            List<Tab> tabs = navigationTabsetBean.getTabs();
            Tab tab = tabs.get(0);
            selectedNodeView = NodeType
                    .getNodeViewByNodeType(tab.getNodeType());
            nodeName = tab.getNodeName();
            nodeID = Integer.parseInt(tab.getNodeId());
        }

        if (navigationRenderBean != null) {
            navigationRenderBean.setSelectedNodeView(selectedNodeView);
        }
        String viewParam = selectedNodeView + Constant.NODE_VIEW_DELIMITER + nodeName + Constant.NODE_VIEW_DELIMITER + nodeID;
        stateController.addSessionAttribute("viewParam", viewParam, true);

    }

    /**
     * Sets the hiddenField.
     * 
     * @param hiddenField
     *            the hiddenField to set
     */
    public void setHiddenField(String hiddenField) {
        this.hiddenField = hiddenField;
    }

    /**
     * Returns the hiddenField.
     * 
     * @return the hiddenField
     */
    public String getHiddenField() {
        return hiddenField;
    }
}
