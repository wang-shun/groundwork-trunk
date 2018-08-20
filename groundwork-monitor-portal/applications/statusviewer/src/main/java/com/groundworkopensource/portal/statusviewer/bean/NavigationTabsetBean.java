package com.groundworkopensource.portal.statusviewer.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.NavigationHelper;
import com.groundworkopensource.portal.model.UserNavigation;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.IPCHandlerConstants;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * The Class NavigationTabsetBean.
 * 
 * @author swapnil_gujrathi
 */
public class NavigationTabsetBean extends ServerPush implements Serializable {

    /**
     * application type
     */
    private static final String APP_TYPE_STATUSVIEWER = "statusviewer";

    /**
     * part of LABEL_FOR_SERVICE
     */
    private static final String LABEL_FOR_SERVICE = " on ";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5183274597179553679L;

    /** The tab index. */
    private int tabIndex = -1;

    /** The tabs. */
    private List<Tab> tabs = new ArrayList<Tab>();

    /** StateController. */
    private StateController stateController;

    /** logger. */
    private static final Logger LOGGER = Logger
            .getLogger(NavigationTabsetBean.class.getName());

    /**
     * MAX_LABEL_LENGTH after which truncated label will be shown on screen
     */
    private static final int MAX_LABEL_LENGTH = 15;

    /**
     * Id of logged in user.
     */
    private String userId;

    /**
     * NavigationHelper
     */
    private NavigationHelper navigationHelper;

    /**
     * Hidden Field.
     */
    private String hiddenField;

    /**
     * ReferenceTreeMetaModel instance
     */
    private ReferenceTreeMetaModel referenceTreeModel;

    /**
     * newHiddenField
     */
    private String newHiddenField;

    /**
     * this field is to avoid repetitive calling of javascript function call on
     * page.
     */
    private boolean isTabChanged;

    /**
     * This field indicates if the tab is newly opened or not (existing one).
     */
    private boolean isNewTab;

    /**
     * UserRoleBean
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Instantiates a new navigation tabset bean.
     */
    public NavigationTabsetBean() {
        super(true,30000);
        /*
         * important: initialize UserRoleBean over here and determine the user
         * role from Portlet Request. Reason to do this here - afterwards
         * PortletRequest becomes dirty.
         */
        FacesUtils.getManagedBean(Constant.USER_ROLE_BEAN);

        // get the userExtendedRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();
        stateController = new StateController();
        navigationHelper = new NavigationHelper();
        referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                .getManagedBean(Constant.REFERENCE_TREE);
       
        // take id of logged in user
        userId = FacesUtils.getLoggedInUser();
      
        this.fetchTabHistory(tabIndex);

        tabIndex = 0;
    }

    /**
     * fetch navigation history depending on extended role list
     */
    private void fetchTabHistory(int tabIndex) {
        if (userExtendedRoleBean.getExtRoleHostGroupList().isEmpty()
                && userExtendedRoleBean.getExtRoleServiceGroupList().isEmpty()) {
            // pre-defined 'Entire Network' node tab into the panelTabSet
            Tab newTab1 = new Tab("Entire Network", NodeType.NETWORK, "0",
                    ++tabIndex, Constant.EMPTY_STRING, "Entire Network");
            tabs.add(newTab1);

            // fetch navigation history and add to tabs
            fetchNavigationHistory(userId, tabIndex);

        } else {

            // fetch navigation history and add to tabs
            fetchExtRoleNavigationHistory(userId, tabIndex);

        }
    }

    /**
     * add a new tab to the panelTabSet.
     * 
     * @param nodeName
     *            the tab label node name
     * @param nodeType
     *            the node type
     * @param nodeId
     *            the node id
     * @param parentInfo
     *            the parent information
     */
    public void addTab(String nodeName, NodeType nodeType, String nodeId,
            String parentInfo) {
        if (NodeType.NETWORK.equals(nodeType)) {
            // set the tab index to 0
            tabIndex = 0;
            return;
        }

        /*
         * here decide whether to add new tab or if present show the existing
         * one to user and set the tabIndex accordingly
         */
        Tab searchTab = searchTab(nodeType, nodeId);
        if (null == searchTab) {

            // set new tab as true
            isNewTab = true;

            tabIndex = getTabsSize();

            // for node type 'service', we need to fetch associated host
            // information
            String tabTooltip = nodeName;
            if (NodeType.SERVICE.equals(nodeType)) {
                NetworkMetaEntity serviceById = referenceTreeModel
                        .getServiceById(Integer.valueOf(nodeId));
                if (null != serviceById) {
                    NetworkMetaEntity hostById = referenceTreeModel
                            .getHostById(serviceById.getParentId());
                    if (null != hostById) {
                        tabTooltip = new StringBuilder(nodeName).append(
                                LABEL_FOR_SERVICE).append(hostById.getName())
                                .toString();
                    }
                }
            }

            // set the new tab as per the node Id, name and type
            Tab newTab = new Tab(nodeName, nodeType, nodeId, tabIndex,
                    parentInfo, tabTooltip);

            // add tab into list of tabs
            tabs.add(newTab);

            // store selected node (navigation history) into database as per the
            // user id
            try {
                navigationHelper.addHistoryRecord(userId, Integer
                        .parseInt(nodeId), nodeName, nodeType.getTypeName(),
                        parentInfo, tabTooltip, APP_TYPE_STATUSVIEWER);
            } catch (NumberFormatException e) {
                // ignore
                LOGGER
                        .debug("NumberFormatException : nodeId seems to be incorrect ["
                                + nodeId + "]");
            } catch (IOException e) {
                // ignore
                LOGGER
                        .warn("Failed to add node navigation information into database for user with Id ["
                                + userId
                                + "]. Node name for the tab ["
                                + nodeName + "]");
            }
        } else {
            // set the tab index
            tabIndex = searchTab.index;
            searchTab.setParentInfo(parentInfo);
        }

    }

    /**
     * Searches tab for particular node (based on node type and node id).
     * 
     * @param nodeType
     *            the node type
     * @param nodeId
     *            the node id
     * 
     * @return searched tab on successful search. If search fails, returns null.
     */
    private Tab searchTab(NodeType nodeType, String nodeId) {
        for (Tab tab : tabs) {
            if (nodeType.equals(tab.nodeType) && tab.nodeId.equals(nodeId)) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Gets the tabs.
     * 
     * @return the tabs
     */
    public List<Tab> getTabs() {
        return tabs;
    }

    /**
     * Gets the tab index.
     * 
     * @return the tab index
     */
    public int getTabIndex() {
        return tabIndex;
    }

    /**
     * Gets the tabs size.
     * 
     * @return the tabs size
     */
    public int getTabsSize() {
        return tabs.size();
    }

    /**
     * Sets the tab index.
     * 
     * @param tabIndex
     *            the new tab index
     */
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    /**
     * Inner class that represents a tab object with a label, content, and an
     * index.
     */
    public class Tab {

        /** The label. */
        private String label;

        /** The index. */
        private int index;

        /** nodeType. */
        private NodeType nodeType;

        /** nodeId. */
        private String nodeId;

        /** node name. */
        private String nodeName;

        /**
         * Parent Information
         */
        private String parentInfo = "";

        /** The Tooltip. */
        private String toolTip;

        /**
         * Instantiates a new tab.
         * 
         * @param nodeName
         *            node name
         * @param nodeId
         *            the node id
         * @param nodeType
         *            the node type
         * @param index
         *            the index
         * @param parentInfo
         *            the parent information
         */
        Tab(String nodeName, NodeType nodeType, String nodeId, int index,
                String parentInfo, String tabTooltip) {
            this.setNodeName(nodeName);
            this.setIndex(index);
            this.setNodeType(nodeType);
            this.setNodeId(nodeId);
            this.setParentInfo(parentInfo);

            // set the tooltip
            this.setToolTip(tabTooltip);

            // set the label
            if (nodeName.length() > MAX_LABEL_LENGTH) {
                this.label = nodeName.substring(0, MAX_LABEL_LENGTH)
                        + Constant.ELLIPSES;
            } else {
                this.label = nodeName;
            }

        }

        /**
         * Gets the label.
         * 
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Sets the label.
         * 
         * @param label
         *            the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * Gets the index.
         * 
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the index.
         * 
         * @param index
         *            the new index
         */
        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * Close tab. It closes current (clicked) tab and shows previous
         * (index-1) tab to the user.
         * 
         * @param event
         *            the event
         */
        public void closeTab(ActionEvent event) {
            int selectedIndex = this.index;
            Tab currentlySelectedTab = tabs.get(tabIndex);

            // remove closed node from navigation history database
            try {
                navigationHelper.deleteHistoryRecord(userId, Integer
                        .parseInt(this.nodeId), this.nodeType.getTypeName(),
                        APP_TYPE_STATUSVIEWER);

                // remove session attribute created for Filters (host and
                // service)
                stateController.removeFilterSessionAttributes(this.nodeType,
                        this.nodeName, this.nodeId);

                // remove from tabs list
                tabs.remove(this);
            } catch (NumberFormatException e) {
                // ignore
                LOGGER
                        .debug("NumberFormatException : nodeId seems to be incorrect ["
                                + nodeId + "]");
            } catch (IOException e) {
                // ignore
                LOGGER
                        .warn("Failed to remove/delete node navigation information from database for user with Id ["
                                + userId
                                + "]. Node name for the tab ["
                                + this.nodeName + "]");
            }

            // check if user has deleted the selected tab
            if (tabIndex == selectedIndex) {
                // try and find a valid index
                Tab tab = tabs.get(selectedIndex - 1);
                tabIndex = tab.index;
                // re-arrange tab indexes
                rearrangeIndexes(tab.index);
                // navigate to the previous tab
                String viewParam = NodeType.getNodeViewByNodeType(tab.nodeType)
                        + Constant.NODE_VIEW_DELIMITER + tab.nodeName + Constant.NODE_VIEW_DELIMITER
                        + tab.nodeId + Constant.NODE_VIEW_DELIMITER + tab.parentInfo;
                stateController.addSessionAttribute("viewParam", viewParam,
                        true);

            } else {
                // re-arrange tab indexes
                rearrangeIndexes(selectedIndex - 1);
                if (selectedIndex < tabIndex) {
                    Tab tab = searchTab(currentlySelectedTab.nodeType,
                            currentlySelectedTab.nodeId);
                    if (null != tab) {
                        tabIndex = tab.index;
                    }
                }
            }

            setTabChanged(true);

        }

        /**
         * Rearrange tab indexes.
         * 
         * @param fromIndex
         *            the from index
         */
        private void rearrangeIndexes(int fromIndex) {
            for (int i = fromIndex; i < tabs.size(); i++) {
                tabs.get(i).setIndex(i);
            }
        }

        /**
         * Navigation tab selected.
         * 
         * @param event
         *            the event
         */
        public void navigationTabSelected(ActionEvent event) {
            tabIndex = this.index;
            navigationTabSelected();
        }

        /**
         * Navigation tab selected.
         */
        private void navigationTabSelected() {
            String selectedNodeView = FacesUtils
                    .getRequestParameter("nodeView");
            String nodeNameParam = FacesUtils.getRequestParameter("nodeNameValue");
            String nodeIdParam = FacesUtils.getRequestParameter("nodeId");

            String viewParam = selectedNodeView + Constant.NODE_VIEW_DELIMITER
                    + nodeNameParam + Constant.NODE_VIEW_DELIMITER + nodeIdParam;
            stateController.addSessionAttribute("viewParam", viewParam, true);
            if (nodeNameParam.equalsIgnoreCase(Constant.ENTIRE_NETWORK)) {
                // if tab is entire n/w, don't do anything
                return;
            }
            // Expand the tree here
            String path;
            // Check for null or null string.Strange "null" value here
            if (parentInfo != null && !parentInfo.equals("null")) {
                path = parentInfo + "," + nodeNameParam;
            } else {
                path = nodeNameParam;
            } // end if

            stateController.addSessionAttribute(
                    IPCHandlerConstants.SV_PATH_ATTRIBUTE, path, true);
            stateController.addSessionAttribute(
                    IPCHandlerConstants.SV_NODE_TYPE_ATTRIBUTE,
                    selectedNodeView, true);
            stateController.addSessionAttribute(
                    IPCHandlerConstants.SV_TAB_PRESSED_ATTRIBUTE, "true", true);
        }

        /**
         * Sets the nodeType.
         * 
         * @param nodeType
         *            the nodeType to set
         */
        public void setNodeType(NodeType nodeType) {
            this.nodeType = nodeType;
        }

        /**
         * Returns the nodeType.
         * 
         * @return the nodeType
         */
        public NodeType getNodeType() {
            return nodeType;
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
         * Returns the nodeId.
         * 
         * @return the nodeId
         */
        public String getNodeId() {
            return nodeId;
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
            return parentInfo;
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
         * Returns the nodeName.
         * 
         * @return the nodeName
         */
        public String getNodeName() {
            return nodeName;
        }

        /**
         * Sets the toolTip.
         * 
         * @param toolTip
         *            the toolTip to set
         */
        public void setToolTip(String toolTip) {
            this.toolTip = toolTip;
        }

        /**
         * Returns the toolTip.
         * 
         * @return the toolTip
         */
        public String getToolTip() {
            return toolTip;
        }
    }

    /**
     * Perform navigation.
     * 
     * @param navNodeType
     *            the nav node type
     * @param navNodeName
     *            the nav node name
     * @param navNodeId
     *            the nav node id
     * @param parentInfo
     *            parent information
     */
    public void performNavigation(String navNodeId, String navNodeName,
            NodeType navNodeType, String parentInfo) {
        setTabChanged(true);
        // add new tab into navigation
        this.addTab(navNodeName, navNodeType, navNodeId, parentInfo);
    }

    /**
     * fetchNavigationHistory
     * 
     * @param userId
     * @param tabIndex
     */
    public void fetchNavigationHistory(String userId, int tabIndex) {
        try {
        	List<UserNavigation> historyRecords = navigationHelper.getHistoryRecords(
                    userId, APP_TYPE_STATUSVIEWER);
            for (UserNavigation historyRecord : historyRecords) {
                Tab newTab = new Tab(historyRecord.getNodeName(),
                        NodeType.getNodeTypeByTypeName(historyRecord.getNodeType()), String.valueOf(historyRecord.getNodeId()), ++tabIndex,
                        historyRecord.getParentInfo(), historyRecord.getToolTip());
                tabs.add(newTab);
            }
        } catch (IOException e) {
            LOGGER
                    .error("IOException while fetching Navigation History for user with Id ["
                            + userId + "]. Actual Exception : " + e);
        }
    }

    /**
     * Adding Node against Extended Role Permissions.
     * 
     * @param userId
     * @param tabIndex
     */
    public void fetchExtRoleNavigationHistory(String userId, int tabIndex) {
        try {
        	List<UserNavigation> historyRecords = navigationHelper.getHistoryRecords(
                    userId, APP_TYPE_STATUSVIEWER);
            List<String> extRoleHostGroupList = userExtendedRoleBean
                    .getExtRoleHostGroupList();
            List<String> extRoleServiceGroupList = userExtendedRoleBean
                    .getExtRoleServiceGroupList();
            String defaultHostGroup = userExtendedRoleBean
                    .getDefaultHostGroup();
            String defaultServiceGroup = userExtendedRoleBean
                    .getDefaultServiceGroup();
            if (tabs.isEmpty()) {
                tabIndex = addDefaultExtRoleTab(tabIndex, extRoleHostGroupList,
                        extRoleServiceGroupList, defaultHostGroup,
                        defaultServiceGroup);

            }
            for (UserNavigation historyRecord : historyRecords) {
                
                Tab newTab = null;
                String nodeName = historyRecord.getNodeName();
                NodeType nodeType = NodeType.getNodeTypeByTypeName(historyRecord.getNodeType());
                String nodeID = String.valueOf(historyRecord.getNodeId());
                String parentInfo = historyRecord.getParentInfo();

                switch (nodeType) {
                    case HOST_GROUP:
                        if (extRoleHostGroupList.isEmpty()) {
                            newTab = new Tab(nodeName, nodeType, nodeID,
                                    ++tabIndex, parentInfo, historyRecord.getToolTip());
                            tabs.add(newTab);
                        } else {
                            // check if current node is default node
                            if (tabs.get(0).getNodeName().equalsIgnoreCase(
                                    nodeName)
                                    && tabs.get(0).nodeType == NodeType.HOST_GROUP) {
                                deleteNodeFromHistoryRecord(userId, nodeName,
                                        nodeType, nodeID);

                            } else if (extRoleHostGroupList.contains(nodeName)) {
                                newTab = new Tab(nodeName, nodeType, nodeID,
                                        ++tabIndex, parentInfo, historyRecord.getToolTip());
                                tabs.add(newTab);
                            } else {
                                deleteNodeFromHistoryRecord(userId, nodeName,
                                        nodeType, nodeID);
                            }
                        }

                        break;
                    case SERVICE_GROUP:
                        if (extRoleServiceGroupList.isEmpty()) {
                            newTab = new Tab(nodeName, nodeType, nodeID,
                                    ++tabIndex, parentInfo, historyRecord.getToolTip());
                            tabs.add(newTab);
                        } else {
                            // check if current node is default node
                            if (tabs.get(0).getNodeName().equalsIgnoreCase(
                                    nodeName)
                                    && tabs.get(0).nodeType == NodeType.SERVICE_GROUP) {

                                deleteNodeFromHistoryRecord(userId, nodeName,
                                        nodeType, nodeID);

                            } else if (extRoleServiceGroupList
                                    .contains(nodeName)) {
                                newTab = new Tab(nodeName, nodeType, nodeID,
                                        ++tabIndex, parentInfo, historyRecord.getToolTip());
                                tabs.add(newTab);
                            } else {
                                deleteNodeFromHistoryRecord(userId, nodeName,
                                        nodeType, nodeID);
                            }
                        }

                        break;
                    case HOST:
                        if (extRoleHostGroupList.isEmpty()
                                || referenceTreeModel
                                        .checkNodeForExtendedRolePermissions(
                                                Integer.parseInt(nodeID),
                                                nodeType, nodeName,
                                                extRoleHostGroupList,
                                                extRoleServiceGroupList)) {
                            newTab = new Tab(nodeName, nodeType, nodeID,
                                    ++tabIndex, parentInfo, historyRecord.getToolTip());
                            tabs.add(newTab);

                        } else {
                            deleteNodeFromHistoryRecord(userId, nodeName,
                                    nodeType, nodeID);
                        }
                        break;
                    case SERVICE:
                        if (referenceTreeModel
                                .checkNodeForExtendedRolePermissions(Integer
                                        .parseInt(nodeID), nodeType, nodeName,
                                        extRoleHostGroupList,
                                        extRoleServiceGroupList)) {
                            newTab = new Tab(nodeName, nodeType, nodeID,
                                    ++tabIndex, parentInfo, historyRecord.getToolTip());
                            tabs.add(newTab);
                        } else {
                            deleteNodeFromHistoryRecord(userId, nodeName,
                                    nodeType, nodeID);
                        }
                        break;
                    default:
                        break;
                }

            }

        } catch (IOException e) {
            LOGGER
                    .error("IOException while fetching Navigation History for user with Id ["
                            + userId + "]. Actual Exception : " + e);
        }
    }

    /**
     * Add default host group or service group tab
     * 
     * @param tabIndex
     * @param extRoleHostGroupList
     * @param extRoleServiceGroupList
     * @param defaultHostGroup
     * @param defaultServiceGroup
     * @return tabIndex
     */
    private int addDefaultExtRoleTab(int tabIndex,
            List<String> extRoleHostGroupList,
            List<String> extRoleServiceGroupList, String defaultHostGroup,
            String defaultServiceGroup) {
        // adding default host group or service group tab
        if (referenceTreeModel == null) {
            referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                    .getManagedBean(Constant.REFERENCE_TREE);
        }
        NetworkMetaEntity defaultEntityByUserRole = referenceTreeModel
                .getDefaultEntityByUserRole(defaultHostGroup,
                        defaultServiceGroup, extRoleHostGroupList,
                        extRoleServiceGroupList);
        Tab newTab = new Tab(defaultEntityByUserRole.getName(),
                defaultEntityByUserRole.getType(), defaultEntityByUserRole
                        .getObjectId().toString(), ++tabIndex,
                defaultEntityByUserRole.getParentListString(),
                defaultEntityByUserRole.getName());
        tabs.add(newTab);

        return tabIndex;
    }

    /**
     * delete node from user navigation
     * 
     * @param userId
     * @param nodeName
     * @param nodeType
     * @param nodeID
     */
    private void deleteNodeFromHistoryRecord(String userId, String nodeName,
            NodeType nodeType, String nodeID) {

        try {
            navigationHelper.deleteHistoryRecord(userId, Integer
                    .parseInt(nodeID), nodeType.getTypeName(),
                    APP_TYPE_STATUSVIEWER);
        } catch (NumberFormatException e) {
            // ignore
            LOGGER
                    .debug("NumberFormatException : nodeId seems to be incorrect ["
                            + nodeID + "]");
        } catch (IOException e) {
            // ignore
            LOGGER
                    .warn("Failed to remove/delete node navigation information from database for user with Id ["
                            + userId
                            + "]. Node name for the tab ["
                            + nodeName
                            + "]");
        }
    }

    /**
     * Returns the userId.
     * 
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the userId.
     * 
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Closes All Tabs and navigates to Entire Network
     * 
     * @param event
     */
    public void closeAllTabs(ActionEvent event) {

        try {
            // delete all tab history from the database
            navigationHelper.deleteAllHistoryRecords(userId,
                    APP_TYPE_STATUSVIEWER);
            tabs.clear();
            Tab newTab = null;
            List<String> extRoleHostGroupList = userExtendedRoleBean
                    .getExtRoleHostGroupList();
            List<String> extRoleServiceGroupList = userExtendedRoleBean
                    .getExtRoleServiceGroupList();
            tabIndex = -1;
            if (extRoleHostGroupList.isEmpty()
                    && extRoleServiceGroupList.isEmpty()) {
                // remove all tabs from tabs list except 'Entire Network'
                newTab = new Tab("Entire Network", NodeType.NETWORK, "0",
                        ++tabIndex, Constant.EMPTY_STRING, "Entire Network");

            } else {
                NetworkMetaEntity defaultEntityByUserRole = referenceTreeModel
                        .getDefaultEntityByUserRole(userExtendedRoleBean
                                .getDefaultHostGroup(), userExtendedRoleBean
                                .getDefaultServiceGroup(),
                                extRoleHostGroupList, extRoleServiceGroupList);
                newTab = new Tab(defaultEntityByUserRole.getName(),
                        defaultEntityByUserRole.getType(),
                        defaultEntityByUserRole.getObjectId().toString(),
                        ++tabIndex, null, defaultEntityByUserRole.getToolTip());
            }
            tabs.add(newTab);
            setTabChanged(true);

            // navigate to entire network node tab
            String viewParam = NodeType.getNodeViewByNodeType(newTab.nodeType)
                    + Constant.NODE_VIEW_DELIMITER + newTab.nodeName + Constant.NODE_VIEW_DELIMITER
                    + newTab.nodeId + Constant.NODE_VIEW_DELIMITER + newTab.parentInfo;
            stateController.addSessionAttribute("viewParam", viewParam, true);

            // call JS method on page to resize tab bar accordingly
            JavascriptContext.addJavascriptCall(FacesContext
                    .getCurrentInstance(), "setTabWidth();");

        } catch (IOException e) {
            // ignore
            LOGGER
                    .warn("Failed to remove/delete all node navigation information from database for user with Id ["
                            + userId + "].");
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlMessage) {
        // Method not implemented yet: ServerPush.refresh(...) is not
        // implemented
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
        if (isIntervalRender()) {
            try {
                Tab prevSelectedTab = tabs.get(tabIndex);

                // remove all tabs from tabs list except 'Entire Network'
                // Tab entireNetworkTab = tabs.get(0);
                tabs.clear();
                // tabs.add(entireNetworkTab);
                // fetch navigation history from database. Recreate tabs list.
                List<UserNavigation> historyRecords = navigationHelper
                        .getHistoryRecords(userId, APP_TYPE_STATUSVIEWER);
                int index = -1;
                this.fetchTabHistory(index);

                // logic for updating tabs on rename / delete of tabs
                Iterator<Tab> tabsIterator = tabs.iterator();
                // skip entire network tab
                tabsIterator.next();
                /*
                 * using iterator so as to avoid concurrent modification as we
                 * need to remove deleted tabs from the list.
                 */
                while (tabsIterator.hasNext()) {
                    Tab nextTab = tabsIterator.next();
                    Integer nodeId = Integer.valueOf(nextTab.nodeId);
                    NetworkMetaEntity networkMetaEntity = null;
                    switch (nextTab.nodeType) {
                        case HOST:
                            networkMetaEntity = referenceTreeModel
                                    .getHostById(nodeId);
                            break;
                        case HOST_GROUP:
                            networkMetaEntity = referenceTreeModel
                                    .getHostGroupById(nodeId);
                            break;
                        case SERVICE:
                            networkMetaEntity = referenceTreeModel
                                    .getServiceById(nodeId);
                            break;
                        case SERVICE_GROUP:
                            networkMetaEntity = referenceTreeModel
                                    .getServiceGroupById(nodeId);
                            break;
                        default:
                            break;
                    }
                    /*
                     * networkMetaEntity will be null if not found in RTMM. So
                     * consider that it has been renamed or deleted. Now take
                     * necessary actions like removing from database as well as
                     * from tabs.
                     */
                    if (null == networkMetaEntity) {
                        // this node has been deleted from the system
                        navigationHelper.deleteHistoryRecord(userId, Integer
                                .parseInt(nextTab.nodeId), nextTab.nodeType
                                .getTypeName());
                        tabsIterator.remove();

                        // re-arrange tab indexes
                        for (int i = nextTab.index; i < tabs.size(); i++) {
                            tabs.get(i).setIndex(i);
                        }
                    }
                }

                /*
                 * As we have re-arranged tabs, firstly need to highlight the
                 * currently selected tab by user. Also that tab may have been
                 * deleted or closed by same user from different instance. So
                 * switch to appropriate available node.
                 */
                Tab currentlySelectedTab = tabs.get((tabs.size() - 1));
                if (tabIndex <= (tabs.size() - 1)) {
                    currentlySelectedTab = tabs.get(tabIndex);
                }
                tabIndex = currentlySelectedTab.index;

                if (!prevSelectedTab.nodeType
                        .equals(currentlySelectedTab.nodeType)
                        || !prevSelectedTab.nodeId
                                .equals(currentlySelectedTab.nodeId)) {
                    // switch to 'currentlySelectedTab' node
                    String viewParam = NodeType
                            .getNodeViewByNodeType(currentlySelectedTab.nodeType)
                            + Constant.NODE_VIEW_DELIMITER
                            + currentlySelectedTab.nodeName
                            + Constant.NODE_VIEW_DELIMITER
                            + currentlySelectedTab.nodeId
                            + Constant.NODE_VIEW_DELIMITER + currentlySelectedTab.parentInfo;
                    stateController.addSessionAttribute("viewParam", viewParam,
                            true);
                }

            } catch (IOException e) {
                LOGGER
                        .error("IOException while fetching Navigation History for user with Id ["
                                + userId + "]. Actual Exception : " + e);
            }
            setIntervalRender(false);
        }

        return hiddenField;
    }

    /**
     * Sets the newHiddenField.
     * 
     * @param newHiddenField
     *            the newHiddenField to set
     */
    public void setNewHiddenField(String newHiddenField) {
        this.newHiddenField = newHiddenField;
    }

    /**
     * Returns the newHiddenField.
     * 
     * @return the newHiddenField
     */
    public String getNewHiddenField() {
        if (isTabChanged()) {
            JavascriptContext.addJavascriptCall(FacesContext
                    .getCurrentInstance(), "selectTab(" + tabIndex + ","
                    + getJumpSteps() + "," + isNewTab + ");");
            setTabChanged(false);
            isNewTab = false;
        }
        return newHiddenField;
    }

    /**
     * Determines the jump steps for navigation tab scrolling as per the type of
     * node.
     * 
     * @return Jump Steps
     */
    private int getJumpSteps() {
        Tab tab = tabs.get(tabIndex);
        switch (tab.nodeType) {
            case NETWORK:
                // if ONLY entire network is there, width is 955 due to absence
                // of close all button.
            case HOST:
            case SERVICE_GROUP:
                return Constant.EIGHT;

            case HOST_GROUP:
            case SERVICE:
                return Constant.NINE;

            default:
                return Constant.EIGHT;
        }
    }

    /**
     * Sets the isTabChanged.
     * 
     * @param isTabChanged
     *            the isTabChanged to set
     */
    public void setTabChanged(boolean isTabChanged) {
        this.isTabChanged = isTabChanged;
    }

    /**
     * Returns the isTabChanged.
     * 
     * @return the isTabChanged
     */
    public boolean isTabChanged() {
        return isTabChanged;
    }

    /**
     * Sets the userExtendedRoleBean.
     * 
     * @param userExtendedRoleBean
     *            the userExtendedRoleBean to set
     */
    public void setUserExtendedRoleBean(
            UserExtendedRoleBean userExtendedRoleBean) {
        this.userExtendedRoleBean = userExtendedRoleBean;
    }

    /**
     * Returns the userExtendedRoleBean.
     * 
     * @return the userExtendedRoleBean
     */
    public UserExtendedRoleBean getUserExtendedRoleBean() {
        return userExtendedRoleBean;
    }

}