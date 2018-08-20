package com.groundworkopensource.portal.statusviewer.bean;

import java.util.StringTokenizer;

import javax.portlet.PortletSession;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.groundworkopensource.portal.statusviewer.handler.StatisticsHandler;

/**
 * The Class NavigationRenderBean.
 * 
 * @author swapnil_gujrathi
 */
public class NavigationRenderBean {

    /** The selected node type. */
    private NodeType selectedNodeType = NodeType.NETWORK;

    /** The selected node view. */
    private String selectedNodeView = "NetworkView";

    /** The node name. */
    private String nodeName = "Entire Network";

    /** The node id. */
    private String nodeId = "0";

    /** The state controller. */
    private StateController stateController;

    /** The statistics handler. */
    private StatisticsHandler statisticsHandler = null;

    /** The filter bean. */
    private FilterBean filterBean = null;

    /** The hidden field. */
    private String hiddenField = "";

    /** The navigation tabset bean. */
    private NavigationTabsetBean navigationTabsetBean;

    /** The prev view param. */
    private String prevViewParam;

    /**
     * Variable which decides if user in Admin or Operator role.
     */
    private boolean userInAdminOrOperatorRole;
    /**
     * UI form style class
     */
    private String megaViewStyleClass = Constant.EMPTY_STRING;

    /**
     * Instantiates a new navigation render bean.
     */
    public NavigationRenderBean() {
        stateController = new StateController();
        String viewParam = new StringBuilder(selectedNodeView).append(
                Constant.NODE_VIEW_DELIMITER).append(nodeName).append(Constant.NODE_VIEW_DELIMITER).append(
                nodeId).toString();
        stateController.addSessionAttribute("viewParam", viewParam, true);
        UserRoleBean userRoleBean = (UserRoleBean) FacesUtils
                .getManagedBean("userRoleBean");
        userInAdminOrOperatorRole = userRoleBean.isUserInAdminOrOperatorRole();
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

        String viewParam = (String) stateController
                .getSessionAttribute("viewParam");
        if (viewParam == null) {
            selectedNodeView = "NetworkView";
            nodeName = "Entire Network";
            nodeId = "0";
            viewParam = new StringBuilder(selectedNodeView).append(
                    Constant.NODE_VIEW_DELIMITER).append(nodeName).append(Constant.NODE_VIEW_DELIMITER)
                    .append(nodeId).toString();
        }

        if (prevViewParam == null || !viewParam.equals(prevViewParam)) {
            prevViewParam = viewParam;

            FacesUtils.getPortletSession(false)
                    .setAttribute("navigationParams", viewParam,
                            PortletSession.PORTLET_SCOPE);
            // parse navigation parameters and extract node id, name, type and
            // parent information
            StringTokenizer stringTokenizer = new StringTokenizer(viewParam,
                    Constant.NODE_VIEW_DELIMITER);
            NodeType nodeType = NodeType.getNodeTypeByView(stringTokenizer
                    .nextToken());

            nodeName = stringTokenizer.nextToken();
            nodeId = stringTokenizer.nextToken();
            String parentInfo = Constant.EMPTY_STRING;
            if (stringTokenizer.hasMoreTokens()) {
                parentInfo = stringTokenizer.nextToken();
            }

            selectedNodeView = NodeType.getNodeViewByNodeType(nodeType);

            // update statistics handler
            statisticsHandler = (StatisticsHandler) FacesUtils
                    .getManagedBean(Constant.STATISTICS_HANDLER);
            statisticsHandler.update(nodeType, nodeName, nodeId);

            // update filter bean
            filterBean = (FilterBean) FacesUtils.getManagedBean("filterBean");
            filterBean.update(nodeType, nodeName, Integer.parseInt(nodeId));
            // perform the actual navigation
            navigationTabsetBean = (NavigationTabsetBean) FacesUtils
                    .getManagedBean("navigationTabsetBean");
            navigationTabsetBean.performNavigation(nodeId, nodeName, nodeType,
                    parentInfo);
            if (!userInAdminOrOperatorRole) {
                switch (nodeType) {
                    case HOST:
                        setMegaViewStyleClass("UniqueMarginhv");
                        break;
                    case HOST_GROUP:
                        setMegaViewStyleClass("UniqueMarginhg");
                        break;
                    case SERVICE:
                        setMegaViewStyleClass("UniqueMarginsv");
                        break;
                    case SERVICE_GROUP:
                        setMegaViewStyleClass("UniqueMarginsg");
                        break;
                    default:
                        setMegaViewStyleClass(Constant.EMPTY_STRING);
                        break;
                }
            }

        }
        return hiddenField;
    }

    /**
     * Returns the nodeName.
     * 
     * @return the nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the nodeName.
     * 
     * @param nodeName
     *            the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Returns the nodeId.
     * 
     * @return the nodeId
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Sets the nodeId.
     * 
     * @param nodeId
     *            the nodeId to set
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Returns the selectedNodeType.
     * 
     * @return the selectedNodeType
     */
    public NodeType getSelectedNodeType() {
        return selectedNodeType;
    }

    /**
     * Returns the selectedNodeView.
     * 
     * @return the selectedNodeView
     */
    public String getSelectedNodeView() {

        return selectedNodeView;
    }

    /**
     * Sets the selectedNodeView.
     * 
     * @param selectedNodeView
     *            the selectedNodeView to set
     */
    public void setSelectedNodeView(String selectedNodeView) {
        this.selectedNodeView = selectedNodeView;
    }

    /**
     * Sets the selectedNodeType.
     * 
     * @param selectedNodeType
     *            the selectedNodeType to set
     */
    public void setSelectedNodeType(NodeType selectedNodeType) {
        this.selectedNodeType = selectedNodeType;
    }

    /**
     * Sets the userInAdminOrOperatorRole.
     * 
     * @param userInAdminOrOperatorRole
     *            the userInAdminOrOperatorRole to set
     */
    public void setUserInAdminOrOperatorRole(boolean userInAdminOrOperatorRole) {
        this.userInAdminOrOperatorRole = userInAdminOrOperatorRole;
    }

    /**
     * Returns the userInAdminOrOperatorRole.
     * 
     * @return the userInAdminOrOperatorRole
     */
    public boolean isUserInAdminOrOperatorRole() {
        return userInAdminOrOperatorRole;
    }

    /**
     * Sets the megaViewStyleClass.
     * @param megaViewStyleClass the megaViewStyleClass to set
     */
    public void setMegaViewStyleClass(String megaViewStyleClass) {
        this.megaViewStyleClass = megaViewStyleClass;
    }

    /**
     * Returns the megaViewStyleClass.
     * @return the megaViewStyleClass
     */
    public String getMegaViewStyleClass() {
        return megaViewStyleClass;
    }

}
