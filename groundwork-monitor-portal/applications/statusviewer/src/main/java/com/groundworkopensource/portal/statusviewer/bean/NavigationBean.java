package com.groundworkopensource.portal.statusviewer.bean;

import javax.faces.event.ActionEvent;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.StateController;

/**
 * The Class NavigationBean.
 * 
 * @author manish_kjain
 */
public class NavigationBean {

    /** StateController. */
    private StateController stateController;

    // /** logger. */
    // private static final Logger LOGGER =
    // Logger.getLogger(NavigationBean.class
    // .getName());

    /**
     * Instantiates a new navigation bean.
     */
    public NavigationBean() {
        stateController = new StateController();
    }

    /**
     * Navigation bean action listener.
     * 
     * @param event
     *            the event
     */
    public void navigationBeanActionListener(ActionEvent event) {
        String selectedNodeView = FacesUtils.getRequestParameter("nodeView");
        String nodeName = FacesUtils.getRequestParameter("nodeNameValue");
        String nodeId = FacesUtils.getRequestParameter("nodeId");
        String parentInfo = FacesUtils.getRequestParameter("parentInfo");
        String url = FacesUtils.getRequestParameter("url"); // for customgroups return null string
        
        NodeType selectedNodeType = NodeType
                .getNodeTypeByView(selectedNodeView);

        // get the userExtendedRoleBean managed instance
        UserExtendedRoleBean userExtendedRoleBean = PortletUtils
                .getUserExtendedRoleBean();

        if ((NodeType.NETWORK == selectedNodeType
                && !userExtendedRoleBean.getExtRoleHostGroupList().isEmpty()
                && !userExtendedRoleBean.getExtRoleServiceGroupList().isEmpty()) || NodeType.CUSTOM_GROUP == selectedNodeType || url == null || url.equalsIgnoreCase("null") || url.equalsIgnoreCase("")) {
            return;
        }

        String viewParam = new StringBuilder(selectedNodeView).append(
                Constant.NODE_VIEW_DELIMITER).append(nodeName).append(Constant.NODE_VIEW_DELIMITER).append(
                nodeId).append(Constant.NODE_VIEW_DELIMITER).append(parentInfo).toString();

        stateController.addSessionAttribute("viewParam", viewParam, true);
    }

}
