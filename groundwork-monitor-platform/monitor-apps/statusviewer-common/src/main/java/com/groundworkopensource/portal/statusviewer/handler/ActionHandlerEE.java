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
package com.groundworkopensource.portal.statusviewer.handler;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.action.ActionBean;
import com.groundworkopensource.portal.statusviewer.bean.action.CommandParamsBean;
import com.groundworkopensource.portal.statusviewer.bean.action.VisibilityBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.ValidationUtils;
import com.groundworkopensource.portal.statusviewer.common.actions.HostActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.HostGroupActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.NagiosStatusCodeEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ParentMenuActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.PopupComponentsEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ScheduledDowntimeCommandEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ServiceActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ServiceGroupActionEnum;
import com.groundworkopensource.portal.common.eventbroker.ClientSocket;
import com.icesoft.faces.component.menubar.MenuItem;
import com.icesoft.faces.component.menubar.MenuItems;
import com.icesoft.faces.context.effects.JavascriptContext;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.rs.client.AuditLogClient;
import org.groundwork.rs.dto.DtoAuditLog;
import org.groundwork.rs.dto.DtoAuditLogList;
import org.groundwork.rs.dto.DtoOperationResults;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.portlet.PortletSession;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * This class handles all the click events for the action portlet menus.
 * 
 * @author shivangi_walvekar
 * 
 */
public class ActionHandlerEE implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8961488544806223349L;

    /**
     * Maximum allowable length of comment.
     */
    private static final int COMMENT_MAX_ALLOWABLE_LENGTH = 500;

    /**
     * LOGGER
     */
    private static final Logger LOGGER = Logger.getLogger(ActionHandlerEE.class
            .getName());

    /**
     * boolean variable to set the visibility of the pop-up
     */
    private boolean popupVisible;

    /**
     * 
     * @return popupVisible
     */
    public boolean isPopupVisible() {
        return popupVisible;
    }

    /**
     * 
     * @param popupVisible
     */
    public void setPopupVisible(boolean popupVisible) {
        this.popupVisible = popupVisible;
    }

    /**
     * Boolean variable indicating if the event broker server is listening for
     * nagios commands or not.
     */
    private boolean nagiosDown = false;

    /**
     * 
     * @return nagiosDown
     */
    public boolean isNagiosDown() {
        return nagiosDown;
    }

    /**
     * 
     * @param nagiosDown
     */
    public void setNagiosDown(boolean nagiosDown) {
        this.nagiosDown = nagiosDown;
    }

    /**
     * Opens a pop-up
     */
    public void openPopup() {
        popupVisible = true;
    }

    /**
     * Boolean to indicate if error has occurred.
     */
    private boolean error = false;

    /**
     * @return error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Error message to be shown on UI,in case of errors/exceptions
     */
    private String errorMessage;

    /**
     * Value of the child menu clicked
     */
    private String childMenuValue;

    /**
     * Id of the child menu clicked
     */
    private String childMenuId;

    /**
     * Value of the parent menu clicked
     */
    private String parentMenuClicked;

    /**
     * ActionCommandHandler reference
     */
    private ActionCommandHandler actionCmdHandler;

    /**
     * Name of the enum to be used on UI to decide the visibility of each of the
     * UI components.
     */
    private String visiblityEnum;

    /**
     * String constant for 'Found Null visibility bean'
     */
    public static final String NULL_VISIBILITY_BEAN = "Found Null visibility bean";

    /**
     * String constant for 'Found null menu.'
     */
    private static final String NULL_MENU = "Found null menu.";

    /**
     * String constant for 'Found Null commandParamsBean.'
     */
    public static final String NULL_COMMAND_PARAMS_BEAN = "Found Null commandParamsBean.";

    // /**
    // * String constant for 'Found Null event source.'
    // */
    // private static final String NULL_EVENT_SOURCE =
    // "Found Null event source";

    /**
     * This flag is used to identify if the action command is fired from Actions
     * portlet or other portlets (like information portlets)
     */
    private boolean fromActionPortlet;

    /**
     * This node type will be used when the action command is fired from
     * portlets other than Actions portlet.
     */
    private NodeType nodeTypeFromOthers;

    /**
     * @return nodeTypeFromOthers
     */
    public NodeType getNodeTypeFromOthers() {
        return nodeTypeFromOthers;
    }

    /**
     * @param nodeTypeFromOthers
     */
    public void setNodeTypeFromOthers(NodeType nodeTypeFromOthers) {
        this.nodeTypeFromOthers = nodeTypeFromOthers;
    }

    /**
     * @return fromActionPortlet
     */
    public boolean isFromActionPortlet() {
        return fromActionPortlet;
    }

    /**
     * @param fromActionPortlet
     */
    public void setFromActionPortlet(boolean fromActionPortlet) {
        this.fromActionPortlet = fromActionPortlet;
    }

    /**
     * @return visiblityEnum
     */
    public String getVisiblityEnum() {
        return visiblityEnum;
    }

    /**
     * @param visiblityEnum
     */
    public void setVisiblityEnum(String visiblityEnum) {
        this.visiblityEnum = visiblityEnum;
    }

    /**
     * @return actionCmdHandler
     */
    public ActionCommandHandler getActionCmdHandler() {
        return actionCmdHandler;
    }

    /**
     * @param actionCmdHandler
     */
    public void setActionCmdHandler(ActionCommandHandler actionCmdHandler) {
        this.actionCmdHandler = actionCmdHandler;
    }

    /**
     * @return parentMenuClicked
     */
    public String getParentMenuClicked() {
        return parentMenuClicked;
    }

    /**
     * @param parentMenuClicked
     */
    public void setParentMenuClicked(String parentMenuClicked) {
        this.parentMenuClicked = parentMenuClicked;
    }

    /**
     * selectedNodeId
     */
    private int selectedNodeId = 0;
    /**
     * selectedNodeType
     */
    private NodeType selectedNodeType;
    /**
     * selectedNodeName
     */
    private String selectedNodeName = Constant.EMPTY_STRING;

    /**
     * foundationWSFacade Object to call web services.
     */
    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    // /**
    // * String constant for 'Found empty action command found'
    // */
    // private static final String EMPTY_MENU = "Found empty menu.";

    /**
     * This list contains child host options status to be displayed in case of
     * 'Schedule Downtime for host' action command.
     */
    private static List<SelectItem> childHostOptions = new ArrayList<SelectItem>();

    /**
     * @return childHostOptions
     */
    public List<SelectItem> getChildHostOptions() {
        return childHostOptions;
    }

    /**
     * This list contains monitor status to be displayed in case of 'Submit
     * Passive Check Result' action command. For host context following monitor
     * status are applicable - DOWN,UP,UNREACHABLE .For Service context
     * following status are applicable - OK,WARNING,UNKNOWN,CRITICAL.
     */
    private List<SelectItem> monitorStatusList = new ArrayList<SelectItem>();

    /**
     * @return monitorStatusList
     */
    public List<SelectItem> getMonitorStatusList() {
        return monitorStatusList;
    }

    /**
     * @param monitorStatusList
     */
    public void setMonitorStatusList(List<SelectItem> monitorStatusList) {
        this.monitorStatusList = monitorStatusList;
    }

    /**
     * @return childMenuValue
     */
    public String getChildMenuValue() {
        return childMenuValue;
    }

    /**
     * @param childMenuValue
     */
    public void setChildMenuValue(String childMenuValue) {
        this.childMenuValue = childMenuValue;
    }

    /**
     * @return childMenuId
     */
    public String getChildMenuId() {
        return childMenuId;
    }

    /**
     * @param childMenuId
     */
    public void setChildMenuId(String childMenuId) {
        this.childMenuId = childMenuId;
    }

    /**
     * Static initializer that populates the 'Child hosts' drop down used for
     * 'Schedule Downtime' action command for host context)
     */
    static {
        for (ScheduledDowntimeCommandEnum enumObj : ScheduledDowntimeCommandEnum
                .values()) {
            SelectItem selectItem = new SelectItem(enumObj.name(), enumObj
                    .getActionCommand());
            childHostOptions.add(selectItem);
        }

    }

    /**
     * Constructor
     */
    public ActionHandlerEE() {
        // Creating instance of ActionCommandHandler
        actionCmdHandler = new ActionCommandHandler();

        if (PortletUtils.isInStatusViewer()) {

            // subpage integration
            if (!handleSubpageIntegration()) {
                return;
            }

            if (selectedNodeType == null) {
                setError(true);
                setErrorMessage(new GWPortalGenericException().getMessage());
                LOGGER
                        .error("Actions Portlet: got null node type. Can not proceed.");
                return;
            }

            // Populates the monitor status for 'Submit passive check result'
            // action
            // command.
            populateMonitorStatus();
        }
    }

    /**
     * Handles the subpage integration: Reads parameters from request in case of
     * Status Viewer. If portlet is in dashboard, reads preferences.
     * 
     * @return
     */
    private boolean handleSubpageIntegration() {
        SubpageIntegrator subpageIntegrator = new SubpageIntegrator();
        boolean isPrefSet = subpageIntegrator.doSubpageIntegration(null);
        if (!isPrefSet) {
            // this portlet is neither part of Network View nor Dashboard. So
            // return false.
            return false;
        }
        // get the required data from SubpageIntegrator
        this.selectedNodeType = subpageIntegrator.getNodeType();
        this.selectedNodeId = subpageIntegrator.getNodeID();
        this.selectedNodeName = subpageIntegrator.getNodeName();

        // nullify subpage integrator object
        subpageIntegrator = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder("[Action Portlet] # Node Type [")
                    .append(selectedNodeType).append("] # Node ID [").append(
                            selectedNodeId).append("]"));
        }
        return true;
    }

    /**
     * This method populates the Monitor status drop down as per the host or
     * service context (check result drop down used for 'Submit passive check
     * results' action command)
     */
    private void populateMonitorStatus() {
        switch (selectedNodeType) {
            case HOST:
                monitorStatusList.clear();
                for (NagiosStatusCodeEnum.Host nagiosCodeEnum : NagiosStatusCodeEnum.Host
                        .values()) {
                    SelectItem selectItem = new SelectItem();
                    selectItem.setLabel(nagiosCodeEnum.getStatusName());
                    selectItem.setValue(nagiosCodeEnum.getNagiosCode());
                    monitorStatusList.add(selectItem);
                    selectItem = null;
                }
                break;
            case SERVICE:
                monitorStatusList.clear();
                for (NagiosStatusCodeEnum.Service nagiosCodeEnum : NagiosStatusCodeEnum.Service
                        .values()) {
                    SelectItem selectItem = new SelectItem();
                    selectItem.setLabel(nagiosCodeEnum.getStatusName());
                    selectItem.setValue(nagiosCodeEnum.getNagiosCode());
                    monitorStatusList.add(selectItem);
                    selectItem = null;
                }
                break;
            default:
                break;
        }
    }

    // /**
    // * This method populates the 'Child hosts' drop down used for 'Schedule
    // * Downtime' action command for host context)
    // */
    // private void populateChildHostOptions() {
    // for (ScheduledDowntimeCommandEnum enumObj : ScheduledDowntimeCommandEnum
    // .values()) {
    // SelectItem selectItem = new SelectItem(enumObj.name(), enumObj
    // .getActionCommand());
    // childHostOptions.add(selectItem);
    // }
    // }

    /**
     * This method is called only from Actions Portlet. This method shows popup
     * for the action clicked.
     * 
     * @param event
     */
    public void showPopup(ActionEvent event) {
        // recreateView to reset previous pop up backing bean values.
        // this.recreateView();
        final String methodName = " showPopup() : ";
        if ((event == null) || (event.getSource() == null)
                || (event.getSource().getClass() == null)
                || (event.getSource().getClass().getName() == null)) {
            // LOGGER.error(NULL_EVENT_SOURCE);
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + "ActionEvent not found.");
            return;
        }
        // Get the child menu option clicked.
        MenuItem eventSource = (MenuItem) event.getSource();
        // Get the value of the menu clicked
        setChildMenuValue(eventSource.getValue().toString());
        // This is added for debugging http://jira/browse/GWMON-7518 JIRA
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug("method : " + methodName + " Child Menu Value : "
        // + getChildMenuValue());
        // }
        // Get the id of the menu clicked
        String childId = eventSource.getId();
        setChildMenuId(childId);
        // Get the parent menu corresponding to the child menu clicked.
        if (eventSource.getParent() == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD
                            + methodName
                            + " eventSource.getParent() found to be null. for component with id :"
                            + childId);
            return;
        }
        String parentId = eventSource.getParent().getId();

        if (parentId != null) {
            if ((eventSource.getParent().findComponent(parentId)) != null) {
                /*
                 * Check if the component is of type MenuItems. In this case do
                 * nothing.This is the case when the parent menu is clicked.
                 */
                if ((eventSource.getParent().findComponent(parentId)) instanceof MenuItems) {
                    return;
                }
                /*
                 * Check if if the component is of type MenuItem. This is the
                 * case when child menus are clicked.
                 */
                if ((eventSource.getParent().findComponent(parentId)) instanceof MenuItem) {
                    // Cast to MenuItem.
                    MenuItem parentMenuItem = (MenuItem) eventSource
                            .getParent().findComponent(parentId);
                    setParentMenuClicked(parentMenuItem.getValue().toString());
                    if (parentMenuItem.getValue().toString().equalsIgnoreCase(
                            "Connections")) {
                        String redirectLink = eventSource.getLink();
                        JavascriptContext.addJavascriptCall(FacesContext
                                .getCurrentInstance(), "window.open('"
                                + redirectLink + "','_blank');");

                        return;
                    }
                    // Added for debugging http://jira/browse/GWMON-7518 JIRA
                    // if (LOGGER.isDebugEnabled()) {
                    // LOGGER.debug("method :  " + methodName
                    // + " Parent Menu Clicked : "
                    // + getParentMenuClicked());
                    // }
                    // define popup
                    try {
                        definePopup(selectedNodeType, selectedNodeId,
                                selectedNodeName);
                    } catch (GWPortalGenericException e) {
                        handleError(
                                "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                                e);
                        return;

                    }
                    /*
                     * The action command is fired from Actions portlet,hence
                     * set the flag to true.
                     */
                    setFromActionPortlet(true);
                } // if MenuItem
            }
        } // (parentId != null)
    }

    /**
     * This method is called whenever user clicks on an action command from
     * various portlets.
     * 
     * @param event
     * @param nodeType
     * @param nodeId
     * @param nodeName
     */
    public void showPopup(ActionEvent event, NodeType nodeType, int nodeId,
            String nodeName) {
        // recreateView to reset previous pop up backing bean values.
        // this.recreateView();
        final String methodName = " showPopup() : ";
        if ((event == null) || (event.getSource() == null)
                || (event.getSource().getClass() == null)
                || (event.getSource().getClass().getName() == null)) {
            // LOGGER.error(NULL_EVENT_SOURCE);
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + "ActionEvent not found.");
            return;
        }

        PortletSession session = FacesUtils.getPortletSession(false);
        if (session == null) {
            setError(true);
            setErrorMessage(ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_popup_error"));
            return;
        }
        String childId = (String) session
                .getAttribute(Constant.SESSION_ATTR_CHILD_ID);
        String childValue = (String) session
                .getAttribute(Constant.SESSION_ATTR_CHILD_VALUE);
        String parentMenu = (String) session
                .getAttribute(Constant.SESSION_ATTR_PARENT_MENU);
        if ((childId == null) || (childValue == null) || (parentMenu == null)) {
            setError(true);
            setErrorMessage(ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_popup_error"));
            return;
        }
        // set the value of the menu clicked.
        setChildMenuValue(childValue);
        // set the id of the menu clicked
        setChildMenuId(childId);
        // set the parent menu corresponding to the child menu clicked.
        setParentMenuClicked(parentMenu);

        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug("child menu ID = " + getChildMenuId());
        // LOGGER.debug("child menu value = " + getChildMenuValue());
        // LOGGER.debug("parent menu value = " + getParentMenuClicked());
        // }

        try {
            definePopup(nodeType, nodeId, nodeName);
        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    e);
            return;
        }
        /*
         * The action command is fired from portlets in dashboard,hence set the
         * flag to false.
         */
        setFromActionPortlet(false);
        /*
         * Set the node type. We will use this node type inside submitCommand()
         * method.
         */
        setNodeTypeFromOthers(nodeType);
    }

    /**
     * When user clicks on a child menu (action command),appropriate modal
     * pop-up is displayed.
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     */
    private void definePopup(NodeType nodeType, int nodeId, String nodeName)
            throws WSDataUnavailableException, GWPortalException {
        final String methodName = " definePopup() : ";
        // PopupBean popupBean = (PopupBean) FacesUtils
        // .getManagedBean(Constant.POP_UP_MANAGED_BEAN);
        // if (popupBean == null) {
        // handleError(
        // "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
        // Constant.METHOD + methodName + "Null Popup bean found");
        // return;
        // }
        // // Open the modal pop-up for action command.
        // popupBean.openPopup();
        openPopup();
        CommandParamsBean commandParamsBean = (CommandParamsBean) FacesUtils
                .getManagedBean(Constant.COMMAND_PARAMS_MANAGED_BEAN);
        if (commandParamsBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + NULL_COMMAND_PARAMS_BEAN);
            return;
        }
        // Reset the commandParamsBean.
        commandParamsBean.reset();
        // Put the logged in user name as author name.
        commandParamsBean.setAuthorName(FacesUtils.getLoggedInUser());

        // Set the current time as default value to Start Time parameter.
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constant.DATE_FORMAT_24_HR_CLK);
        Calendar calendar = Calendar.getInstance();
        // Set the currentTime field of commandParamsBean
        commandParamsBean.setCurrentTime(dateFormat.format(calendar.getTime()));
        commandParamsBean.setStartTime(dateFormat.format(calendar.getTime()));
        // Set the (current time + 2 hours) as default value to End Time
        // parameter.
        calendar.add(Calendar.HOUR, Constant.TWO);
        commandParamsBean.setEndTime(dateFormat.format(calendar.getTime()));

        // set the node type value: Name of the node to be displayed
        commandParamsBean.setNodeTypeValue(nodeName);

        // set command parameters as pet the NodeType
        switch (nodeType) {
            case HOST:
                commandParamsBean.setNodeType(Constant.HOST_NAME
                        + Constant.SPACE + Constant.COLON);
                commandParamsBean.setHostName(nodeName);
                break;

            case HOST_GROUP:
                commandParamsBean.setNodeType(Constant.HOST_GROUP_NAME);
                commandParamsBean.setHostGroupName(nodeName);
                break;

            case SERVICE:
                commandParamsBean.setNodeType(Constant.SERVICE_NAME
                        + Constant.SPACE + Constant.COLON);
                try {
                    ServiceStatus service = foundationWSFacade
                            .getServicesById(nodeId);
                    commandParamsBean.setHostName(service.getHost().getName());
                    commandParamsBean.setServiceDesc(nodeName);
                } catch (GWPortalGenericException e) {
                    handleError(
                            "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                            Constant.METHOD + methodName + "No service found .");
                    return;
                }
                break;

            case SERVICE_GROUP:
                commandParamsBean.setNodeType(Constant.SERVICE_GROUP);
                commandParamsBean.setServiceGroupName(nodeName);
                break;

            default:
                break;
        }

        // define visibility for popup
        defineVisiblity();
    }

    /**
     * This method decides which components are to be rendered on the
     * intermediate screens for action portlet menus. A single universal
     * intermediate screen(modal pop-up) is designed instead of designing
     * individual screens for each of the commands. Decision for a particular
     * component to be rendered or not is based on the action command selected.
     */
    private void defineVisiblity() {
        final String methodName = "defineVisiblity() : ";
        VisibilityBean visibilityBean = (VisibilityBean) FacesUtils
                .getManagedBean(Constant.VISIBILITY_BEAN);
        if (visibilityBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + NULL_VISIBILITY_BEAN);
            return;
        }
        /*
         * Check the child menu clicked,identify corresponding popupEnum,copy
         * the data of popupEnum to visibilityBean.
         */
        if (getChildMenuId() == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + NULL_MENU);
            return;
        }
        for (PopupComponentsEnum popupEnum : PopupComponentsEnum.values()) {
            if (getChildMenuId().equals(popupEnum.name())) {
                visibilityBean.copyEnumData(popupEnum);
            }
        }
    }

    /**
     * This method is called when user clicks on the 'Submit' button on the
     * modal pop-up for action commands. It checks the command requested,checks
     * if it is a composite command,parses the command,replaces formal
     * parameters in the command with the actual values (user inputs on
     * intermediate screen) and sends it to event broker.
     * 
     * @param event
     */
    public void submitCommand(ActionEvent event) {
        final String methodName = "submitCommand() : ";

        CommandParamsBean commandParamsBean = (CommandParamsBean) FacesUtils
                .getManagedBean(Constant.COMMAND_PARAMS_MANAGED_BEAN);
        if (commandParamsBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_socketError",
                    Constant.METHOD + methodName + NULL_COMMAND_PARAMS_BEAN);
            return;
        }

        if (!validateActionFields(event, methodName, commandParamsBean)) {
            return;
        }

        // Close the pop-up
        this.popupVisible = false;

        String nagiosCommand = "";
        String compositeCommand = "";
        String parsedCommand = "";
        NodeType localNodeType = null;
        if (!fromActionPortlet) {
            localNodeType = getNodeTypeFromOthers();
        } else {
            localNodeType = selectedNodeType;
        }
        try {
            switch (localNodeType) {
                case HOST:
                    // Get nagios command,parse it,replace formal parameter
                    // with actual values.
                    nagiosCommand = getNagiosCommandForHost();
                    break;
                case HOST_GROUP:
                    nagiosCommand = getNagiosCommandForHostGroup();
                    break;
                case SERVICE:
                    nagiosCommand = getNagiosCommandForService();
                    break;
                case SERVICE_GROUP:
                    nagiosCommand = getNagiosCommandForServiceGroup();
                    break;
                default:
                    break;
            }
            // Identify composite commands
            compositeCommand = actionCmdHandler
                    .constructCompositeCommands(getChildMenuId());
        } catch (GWPortalGenericException ex) {
            handleError(ex.getMessage(), ex);
            return;
        }
        if (compositeCommand == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_socketError",
                    Constant.METHOD + methodName + NULL_MENU);
            return;
        }
        parsedCommand = compositeCommand;

        // Indicates that the nagios command is not a composite command.
        if (compositeCommand.equals(Constant.EMPTY_STRING)) {
            if ((nagiosCommand == null)
                    || (Constant.EMPTY_STRING.equals(nagiosCommand))) {
                handleError(
                        "com_groundwork_portal_statusviewer_actionsPortlet_socketError",
                        "Could not get the nagios command for nodeType : "
                                + localNodeType);
                return;
            }
            try {
                parsedCommand = actionCmdHandler.parseCommand(nagiosCommand);
            } catch (GWPortalGenericException ex) {
                handleError(ex.getMessage(), ex);
                return;
            }
        }
        if (parsedCommand != null
                && !parsedCommand.equals(Constant.EMPTY_STRING)) {
            // Creating an instance of ClientSocket
            ClientSocket clientSocket = new ClientSocket();
            // Encrypts the command and send to event broker.
            try {
                clientSocket.run(parsedCommand);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Command sent to Event Broker : "
                            + parsedCommand);
                }
            } catch (GWPortalGenericException ex) {
                // If the event broker server/nagios is down,display info
                // message pop-up.
                if (clientSocket.isNagiosDown()) {
                    setNagiosDown(true);
                }
                LOGGER
                        .error("Could not process action command due to internal error.");
                // handleError(ex.getMessage(), ex);
                return;
            }

            // Capture action in audit log record
            try {
                // construct audit log record
                String subsystem = "SV";
                String action = "ACTION";
                String userName = FacesUtils.getLoggedInUser();
                String description = null;
                String [] nagiosCommandElements = parsedCommand.split(Constant.SEMICOLON);
                if (nagiosCommandElements.length >= 3) {
                    userName = nagiosCommandElements[1];
                    description = nagiosCommandElements[2];
                }
                String hostName = null;
                String serviceDescription = null;
                String hostGroupName = null;
                String serviceGroupName = null;
                switch (localNodeType) {
                    case HOST:
                        hostName = commandParamsBean.getHostName();
                        break;
                    case SERVICE:
                        hostName = commandParamsBean.getHostName();
                        serviceDescription = commandParamsBean.getServiceDesc();
                        break;
                    case HOST_GROUP:
                        hostGroupName = commandParamsBean.getHostGroupName();
                        break;
                    case SERVICE_GROUP:
                        serviceGroupName = commandParamsBean.getServiceGroupName();
                        break;
                }
                DtoAuditLog auditLog = new DtoAuditLog(subsystem, action, description, userName, hostName, serviceDescription);
                auditLog.setHostGroupName(hostGroupName);
                auditLog.setServiceGroupName(serviceGroupName);
                // capture audit log record
                String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
                AuditLogClient auditLogClient = new AuditLogClient(deploymentUrl);
                DtoOperationResults results = auditLogClient.post(new DtoAuditLogList(Arrays.asList(auditLog)));
                if ((results == null) || (results.getSuccessful() == 0)) {
                    LOGGER.error("Audit log record not captured for action.");
                }
            } catch (Exception e) {
                LOGGER.error("Audit log record not captured for action: "+e);
            }
        }

        /*
         * Scope of CommandParamsBean is request.But in case of modal pop-up,the
         * bean maintains its state,so explicitely resetting the members in
         * CommandParamsBean.
         */
        commandParamsBean.reset();
    }

    /**
     * validateActionFields
     * 
     * @param methodName
     * @param context
     * @param parentForm
     * @param commandParamsBean
     * @return false if validation fails
     */
    private boolean validateActionFields(ActionEvent event, String methodName,
            CommandParamsBean commandParamsBean) {
        UIComponent base = event.getComponent();
        FacesContext context = FacesUtils.getFacesContext();
        if (null == context || null == base) {
            return false;
        }
        UIComponent parentForm = base.getParent();
        if (null == parentForm) {
            return false;
        }

        VisibilityBean visibilityBean = (VisibilityBean) FacesUtils
                .getManagedBean(Constant.VISIBILITY_BEAN);
        if (visibilityBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + NULL_VISIBILITY_BEAN);
            return false;
        }
        boolean validationFailed = false;

        // comment validation
        if (visibilityBean.isComment()) {
            UIComponent commentsComponent = parentForm
                    .findComponent("actionsPortlet_txtComment");
            if (null != commentsComponent) {
                String comment = commandParamsBean.getComment();
                if (comment == null || comment.trim().length() == 0) {
                    ((UIInput) commentsComponent).setValid(false);
                    ValidationUtils.showMessage("Comment is mandatory field.",
                            "Length of comment cannot be zero.", context,
                            commentsComponent);
                    validationFailed = true;
                }

                // Validate comment length - MAX LENGTH = 500 chars
                if (!validationFailed && comment != null
                        && comment.length() > COMMENT_MAX_ALLOWABLE_LENGTH) {
                    ((UIInput) commentsComponent).setValid(false);
                    ValidationUtils
                            .showMessage(
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_lenghtExceeds_500"),
                                    ResourceUtils
                                            .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_lenghtExceeds_500"),
                                    context, commentsComponent);
                    validationFailed = true;
                }
            }
        }

        // validate start time
        if (visibilityBean.isStartTime()) {
            UIComponent startTimeComponent = parentForm
                    .findComponent("actionsPortlet_txtStartTime");
            if (null != startTimeComponent
                    && !commandParamsBean.validateStartTime(context,
                            startTimeComponent, commandParamsBean
                                    .getStartTime())) {
                validationFailed = true;
            }
        }

        // validate end time
        if (visibilityBean.isEndTime()) {
            UIComponent endTimeComponent = parentForm
                    .findComponent("actionsPortlet_txtEndTime");
            if (null != endTimeComponent
                    && !commandParamsBean.validateEndTime(context,
                            endTimeComponent, commandParamsBean.getEndTime())) {
                validationFailed = true;
            }
        }

        // Validating duration in Hours and Minutes (if the type is flexible)
        if (visibilityBean.isDuration()
                && commandParamsBean.isDurationRequired()) {
            // Validating duration in Hours
            UIComponent durationInHoursComponent = parentForm
                    .findComponent("actionsPortlet_txtHours");
            if (null != durationInHoursComponent
                    && !commandParamsBean.validateHours(context,
                            durationInHoursComponent, commandParamsBean
                                    .getDurationHours())) {
                validationFailed = true;
            }
            // Validating duration in Minutes
            UIComponent durationInMinutesComponent = parentForm
                    .findComponent("actionsPortlet_txtMinutes");
            if (null != durationInMinutesComponent
                    && !commandParamsBean.validateMinutes(context,
                            durationInMinutesComponent, commandParamsBean
                                    .getDurationMinutes())) {
                validationFailed = true;
            }
        }

        // validate CheckTimeValue
        if (visibilityBean.isCheckTime()) {
            UIComponent checkTimeComponent = parentForm
                    .findComponent("actionsPortlet_txtCheckTimeValue");
            if (null != checkTimeComponent
                    && !commandParamsBean.validateCheckTime(context,
                            checkTimeComponent, commandParamsBean
                                    .getStartTime())) {
                validationFailed = true;
            }
        }

        // validate NotificationDelay
        if (visibilityBean.isNotificationDelay()) {
            UIComponent notificationDelayComponent = parentForm
                    .findComponent("actionsPortlet_txtNotificationDelay");
            if (null != notificationDelayComponent
                    && !commandParamsBean.validateNotificationDelay(context,
                            notificationDelayComponent, commandParamsBean
                                    .getNotificationDelay())) {
                validationFailed = true;
            }
        }

        // validate CheckOutputValue
        if (visibilityBean.isCheckOutput()) {
            UIComponent checkOutputValueComponent = parentForm
                    .findComponent("actionsPortlet_txtCheckOutputValue");
            if (null != checkOutputValueComponent
                    && !commandParamsBean.validateCheckOutput(context,
                            checkOutputValueComponent, commandParamsBean
                                    .getCheckOutput())) {
                validationFailed = true;
            }
        }

        // validate PerformanceDataValue
        if (visibilityBean.isPerformanceData()) {
            UIComponent performanceDataValueComponent = parentForm
                    .findComponent("actionsPortlet_txtPerformanceDataValue");
            if (null != performanceDataValueComponent
                    && !commandParamsBean.validatePerformanceData(context,
                            performanceDataValueComponent, commandParamsBean
                                    .getPerformanceData())) {
                validationFailed = true;
            }
        }

        if (validationFailed) {
            return false;
        }
        return true;
    }

    /**
     * This method retrieves the nagios command template for the host context
     * and for the selected menu. It checks the parent menu selected and then
     * iterates over the corresponding enum for child menus to retrieve the
     * nagios command template.
     * 
     * @return nagiosCmd - Nagios command corresponding to the menu/action
     *         command selected.
     * @throws GWPortalGenericException
     */
    public String getNagiosCommandForHost() throws GWPortalGenericException {
        final String methodName = "getNagiosCommandForHost() : ";
        String parentMenu = getParentMenuClicked();
        String childMenu = getChildMenuValue();
        String nagiosCmd = "";
        // Check if the parent menu for the selected child menu is "Acknowledge"
        if (parentMenu.equals(ParentMenuActionEnum.ACKNOWLEDGE.getMenuString())) {
            for (HostActionEnum.Acknowledge ackEnum : HostActionEnum.Acknowledge
                    .values()) {
                if (ackEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = ackEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.DOWNTIME
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Downtime"
             */
            for (HostActionEnum.Downtime downtimeEnum : HostActionEnum.Downtime
                    .values()) {
                if (downtimeEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = downtimeEnum.getNagiosCommand();
                    /*
                     * Handle this special case - 'Schedule Host Downtime' .
                     * This command has drop down in which user can select the
                     * options for child hosts. Depending on the option selected
                     * for child menu,identify which nagios command to send.
                     */
                    if (getChildMenuId() == HostActionEnum.Downtime.SCHEDULE_HOST_DOWNTIME
                            .name()) {
                        // Get the commandParams managed bean from facesContext.
                        CommandParamsBean commandParamsBean = (CommandParamsBean) FacesUtils
                                .getManagedBean(Constant.COMMAND_PARAMS_MANAGED_BEAN);
                        if (commandParamsBean == null) {
                            LOGGER.error(Constant.METHOD + methodName
                                    + NULL_COMMAND_PARAMS_BEAN);
                            throw new GWPortalGenericException(
                                    "com_groundwork_portal_statusviewer_actionsPortlet_socketError");
                        }
                        // Check which option is selected for Child Hosts.
                        String selectedOption = commandParamsBean
                                .getChildHosts();
                        if (selectedOption == null) {
                            LOGGER
                                    .error(Constant.METHOD
                                            + methodName
                                            + "Child host option selected for 'Schedule Host Downtime' command found to be null.");
                            throw new GWPortalGenericException(
                                    "com_groundwork_portal_statusviewer_actionsPortlet_socketError");
                        }
                        // Do nothing with child hosts
                        if (selectedOption
                                .equals(ScheduledDowntimeCommandEnum.DO_NOTHING_WITH_CHILD_HOSTS
                                        .name())) {
                            nagiosCmd = ScheduledDowntimeCommandEnum.DO_NOTHING_WITH_CHILD_HOSTS
                                    .getNagiosCommand();
                        } else if (selectedOption
                                .equals(ScheduledDowntimeCommandEnum.SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME
                                        .name())) {
                            // Schedule non-triggered downtime for all child
                            // hosts
                            nagiosCmd = ScheduledDowntimeCommandEnum.SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME
                                    .getNagiosCommand();
                        } else if (selectedOption
                                .equals(ScheduledDowntimeCommandEnum.SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME
                                        .name())) {
                            // Schedule triggered downtime for all child hosts
                            nagiosCmd = ScheduledDowntimeCommandEnum.SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME
                                    .getNagiosCommand();
                        }
                    }
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.NOTIFICATIONS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Notifications"
             */
            for (HostActionEnum.Notifications notificationsEnum : HostActionEnum.Notifications
                    .values()) {
                if (notificationsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = notificationsEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.EVENT_HANDLERS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Event Handlers"
             */
            for (HostActionEnum.EventHandlers evtHandlersEnum : HostActionEnum.EventHandlers
                    .values()) {
                if (evtHandlersEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = evtHandlersEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.SETTINGS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Settings"
             */
            for (HostActionEnum.Settings settingsEnum : HostActionEnum.Settings
                    .values()) {
                if (settingsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = settingsEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.CHECK_RESULTS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Check Results"
             */
            for (HostActionEnum.CheckResults chkResultsEnum : HostActionEnum.CheckResults
                    .values()) {
                if (chkResultsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = chkResultsEnum.getNagiosCommand();
                    break;
                }
            }
        }
        return nagiosCmd;
    }

    /**
     * This method retrieves the nagios command template for the service context
     * and for the selected menu. It checks the parent menu selected and then
     * iterates over the corresponding enum for child menus to retrieve the
     * nagios command template.
     * 
     * @return nagiosCmd - Nagios command corresponding to the menu/action
     *         command selected.
     */
    public String getNagiosCommandForService() {
        String parentMenu = getParentMenuClicked();
        String childMenu = getChildMenuValue();
        String nagiosCmd = "";
        // Check if the parent menu for the selected child menu is "Acknowledge"
        if (parentMenu.equals(ParentMenuActionEnum.ACKNOWLEDGE.getMenuString())) {
            for (ServiceActionEnum.Acknowledge ackEnum : ServiceActionEnum.Acknowledge
                    .values()) {
                if (ackEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = ackEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.DOWNTIME
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Downtime"
             */
            for (ServiceActionEnum.Downtime downtimeEnum : ServiceActionEnum.Downtime
                    .values()) {
                if (downtimeEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = downtimeEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.NOTIFICATIONS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Notifications"
             */
            for (ServiceActionEnum.Notifications notificationsEnum : ServiceActionEnum.Notifications
                    .values()) {
                if (notificationsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = notificationsEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.EVENT_HANDLERS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Event Handlers"
             */
            for (ServiceActionEnum.EventHandlers evtHandlersEnum : ServiceActionEnum.EventHandlers
                    .values()) {
                if (evtHandlersEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = evtHandlersEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.SETTINGS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Settings"
             */
            for (ServiceActionEnum.Settings settingsEnum : ServiceActionEnum.Settings
                    .values()) {
                if (settingsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = settingsEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.CHECK_RESULTS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Check Results"
             */
            for (ServiceActionEnum.CheckResults chkResultsEnum : ServiceActionEnum.CheckResults
                    .values()) {
                if (chkResultsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = chkResultsEnum.getNagiosCommand();
                    break;
                }
            }
        }
        return nagiosCmd;
    }

    /**
     * This method retrieves the nagios command template for the host group
     * context and for the selected menu. It checks the parent menu selected and
     * then iterates over the corresponding enum for child menus to retrieve the
     * nagios command template.
     * 
     * @return nagiosCmd - Nagios command corresponding to the menu/action
     *         command selected.
     */
    public String getNagiosCommandForHostGroup() {
        String parentMenu = getParentMenuClicked();
        String childMenu = getChildMenuValue();
        String nagiosCmd = "";
        // Check if the parent menu for the selected child menu is "Downtime"
        if (parentMenu.equals(ParentMenuActionEnum.DOWNTIME.getMenuString())) {
            for (HostGroupActionEnum.Downtime downtimeEnum : HostGroupActionEnum.Downtime
                    .values()) {
                if (downtimeEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = downtimeEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.NOTIFICATIONS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Notifications"
             */
            for (HostGroupActionEnum.Notifications notificationsEnum : HostGroupActionEnum.Notifications
                    .values()) {
                if (notificationsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = notificationsEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.SETTINGS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Settings"
             */
            for (HostGroupActionEnum.Settings settingsEnum : HostGroupActionEnum.Settings
                    .values()) {
                if (settingsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = settingsEnum.getNagiosCommand();
                    break;
                }
            }
        }
        return nagiosCmd;
    }

    /**
     * This method retrieves the nagios command template for the service group
     * context and for the selected menu. It checks the parent menu selected and
     * then iterates over the corresponding enum for child menus to retrieve the
     * nagios command template.
     * 
     * @return nagiosCmd - Nagios command corresponding to the menu/action
     *         command selected.
     */
    public String getNagiosCommandForServiceGroup() {
        String parentMenu = getParentMenuClicked();
        String childMenu = getChildMenuValue();
        String nagiosCmd = "";
        // Check if the parent menu for the selected child menu is "Downtime"
        if (parentMenu.equals(ParentMenuActionEnum.DOWNTIME.getMenuString())) {
            for (ServiceGroupActionEnum.Downtime downtimeEnum : ServiceGroupActionEnum.Downtime
                    .values()) {
                if (downtimeEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = downtimeEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.NOTIFICATIONS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Notifications"
             */
            for (ServiceGroupActionEnum.Notifications notificationsEnum : ServiceGroupActionEnum.Notifications
                    .values()) {
                if (notificationsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = notificationsEnum.getNagiosCommand();
                    break;
                }
            }
        } else if (parentMenu.equals(ParentMenuActionEnum.SETTINGS
                .getMenuString())) {
            /*
             * Check if the parent menu for the selected child menu is
             * "Settings"
             */
            for (ServiceGroupActionEnum.Settings settingsEnum : ServiceGroupActionEnum.Settings
                    .values()) {
                if (settingsEnum.getActionCommand().equals(childMenu)) {
                    nagiosCmd = settingsEnum.getNagiosCommand();
                    break;
                }
            }
        }
        return nagiosCmd;
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {
        final String methodName = "reloadPage() : ";
        ActionBean actionBean = (ActionBean) FacesUtils
                .getManagedBean(Constant.ACTION_MANAGED_BEAN);
        if (actionBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_reload_error",
                    Constant.METHOD + methodName + "Found null action bean");
            return;
        }
        this.popupVisible = false;

        // Clear the old data
        actionBean.cleanup();
        // re-initialize the handler so as to reload UI
        actionBean.initialize();
        // Clear the error fields
        setError(false);
        setErrorMessage(Constant.EMPTY_STRING);
        // Clear the error fields for actionBean
        actionBean.setError(false);
        actionBean.setErrorMessage(Constant.EMPTY_STRING);
        LOGGER.info("Reloading the action portlet....");
    }

    /**
     * This method sets the error flag to true,set the error message to be
     * displayed to the user and logs the error.
     * 
     * @param resourceKey
     *            - key for the localized message to be displayed on the UI.
     * @param logMessage
     *            - message to be logged.
     * 
     */
    public void handleError(String resourceKey, String logMessage) {
        setError(true);
        setErrorMessage(ResourceUtils.getLocalizedMessage(resourceKey));
        LOGGER.error(logMessage);
    }

    /**
     * This method sets the error flag to true,set the error message to be
     * displayed to the user and logs the error.Ideally each catch block should
     * call this method.
     * 
     * @param resourceKey
     *            - key for the localized message to be displayed on th UI.
     * @param exception
     *            - Exception to be logged.
     * 
     */
    public void handleError(String resourceKey, Exception exception) {
        setError(true);
        setErrorMessage(ResourceUtils.getLocalizedMessage(resourceKey));
        LOGGER.error(exception);
    }

    /**
     * Method to close pop up window for info message to be displayed when
     * nagios is down.
     */
    public void closeNagiosDownPopup() {
        setNagiosDown(false);
    }

    /**
     * This is an action method which gets called when user clicks on the close
     * button on the pop-up window of actions portlet pop-up.It resets all the
     * fields on the pop-up.
     * 
     * @param ae
     */
    public void closePopup(ActionEvent ae) {
        String methodName = "";
        this.popupVisible = false;
        CommandParamsBean commandParamsBean = (CommandParamsBean) FacesUtils
                .getManagedBean(Constant.COMMAND_PARAMS_MANAGED_BEAN);
        if (commandParamsBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + NULL_COMMAND_PARAMS_BEAN);
            return;
        }
        // Reset the commandParamsBean.
        commandParamsBean.reset();

        // recreates the view tree.
        // recreateView();
    }

    /**
     * @param ae
     */
    public void resetFields(ActionEvent ae) {
        UIComponent base = ae.getComponent();
        UIComponent parentForm = base.getParent();
        resetField(parentForm, "actionsPortlet_txtComment",
                Constant.EMPTY_STRING);

    }

    /**
     * reset ack pop field depending on component ID
     * 
     * @param component
     * @param id
     */
    private void resetField(UIComponent component, String componentId,
            String value) {
        UIInput clearInput = (UIInput) component.findComponent(componentId);
        if (clearInput != null) {
            clearInput.setSubmittedValue(value);
        }

    }

    /**
     * 
     */
    public void validate() {
        String methodName = "validate()";
        CommandParamsBean commandParamsBean = (CommandParamsBean) FacesUtils
                .getManagedBean(Constant.COMMAND_PARAMS_MANAGED_BEAN);
        if (commandParamsBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_actionsPortlet_popup_error",
                    Constant.METHOD + methodName + NULL_COMMAND_PARAMS_BEAN);
            return;
        }

    }

    // /**
    // * This method causes the current View tree to be discarded and a fresh
    // one
    // * created. The new components of course then have no submitted values,
    // and
    // * hence fetch their values via backing beans. Why is this required -
    // * "immediate=true" is set for 'Close' button in actionsPopup.jspx in
    // order
    // * to skip validation. But after user closes the pop-up, and clicks on
    // * action menus,the pop-up does not open up.This is because the action
    // * method on 'Close' button cause JSF to directly go to render phase of
    // the
    // * same view. This causes the components to display the cached value
    // rather
    // * than fetching the data from backing bean. Hence they throw validation
    // * errors which were thrown earlier before closing the pop-up.Resetting
    // the
    // * values in the backing bean does not help. Hence recreating the view.
    // */
    // public void recreateView() {
    // FacesContext facesContext = FacesUtils.getFacesContext();
    // if (facesContext != null) {
    // Application application = facesContext.getApplication();
    // if (application != null) {
    // ViewHandler viewHandler = application.getViewHandler();
    // if (viewHandler != null) {
    // UIViewRoot viewRoot = viewHandler.createView(facesContext,
    // facesContext.getViewRoot().getViewId());
    // facesContext.setViewRoot(viewRoot);
    // facesContext.renderResponse();
    // }
    //
    // }
    // }
    // }

    /**
     * @param nodeType
     * @param nodeName
     * @param nodeId
     */
    public void update(NodeType nodeType, String nodeName, int nodeId) {
        selectedNodeType = nodeType;
        selectedNodeName = nodeName;
        selectedNodeId = nodeId;

        // Populates the monitor status for 'Submit passive check result' action
        // command.
        populateMonitorStatus();
    }

}
