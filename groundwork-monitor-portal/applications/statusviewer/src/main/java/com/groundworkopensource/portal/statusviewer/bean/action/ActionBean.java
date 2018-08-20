/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
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
package com.groundworkopensource.portal.statusviewer.bean.action;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.actions.ActionCommandsConstants;
import com.groundworkopensource.portal.statusviewer.common.actions.HostActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.HostGroupActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ParentMenuActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ParentMenuActionsForGroup;
import com.groundworkopensource.portal.statusviewer.common.actions.ServiceActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ServiceGroupActionEnum;
import com.groundworkopensource.portal.statusviewer.handler.ActionHandlerEE;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.handler.SubpageIntegrator;
import com.icesoft.faces.component.menubar.MenuItem;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.jfree.util.Log;

import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is a managed bean for Actions Portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public class ActionBean extends ServerPush implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 8382618986220786161L;

	/**
	 * LOGGER
	 */
	private static final Logger LOGGER = Logger.getLogger(ActionBean.class
			.getName());

	/**
	 * Boolean flag indicating whether the node status [monitor-status] is in a
	 * state of acknowledgment.
	 */
	private boolean acknowledgeState = true;

	/**
	 * Boolean to indicate if error has occurred.
	 */
	private boolean error = false;

	private boolean rendered = true;

    private static BooleanProperty[] DEFAULT_BOOLEAN_PROPERTIES = new BooleanProperty[] {
            new BooleanProperty(Constant.IS_ACKNOWLEDGED, Boolean.FALSE)
    };

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
	 * This list contains parent level menus
	 * Acknowledge,Downtime,Notifications,Settings,EventHandlers,CheckResults
	 * depending on the context (Host/Host Group/Service/Service Group).
	 */

	private List<MenuItem> parentMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * String constant for 'Found null host'
	 */
	private static final String NULL_HOST = "Found null host";

	/**
	 * String constant for 'Found null service'
	 */
	private static final String NULL_SERVICE = "Found null service";

	/**
	 * action portlet links and value pair.
	 */
	private static final Map<String, String> CONNECTION_URLS_MAP = new LinkedHashMap<String, String>();

	/**
	 * Boolean property isAcknowledged
	 */
	private boolean isAcknowledged;

	/**
	 * foundationWSFacade Object to call web services.
	 */
	private final IWSFacade foundationWSFacade = new WebServiceFactory()
			.getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

	/**
	 * @return isAcknowledged
	 */
	public boolean isAcknowledged() {
		return isAcknowledged;
	}

	/**
	 * @param isAcknowledged
	 */
	public void setAcknowledged(boolean isAcknowledged) {
		this.isAcknowledged = isAcknowledged;
	}

	/**
	 * Boolean property isChecksEnabled
	 */
	private boolean isChecksEnabled;

	/**
	 * @return isChecksEnabled
	 */
	public boolean isChecksEnabled() {
		return isChecksEnabled;
	}

	/**
	 * @param isChecksEnabled
	 */
	public void setChecksEnabled(boolean isChecksEnabled) {
		this.isChecksEnabled = isChecksEnabled;
	}

	/**
	 * Boolean property isAcceptPassiveChecks
	 */
	private boolean isAcceptPassiveChecks;

	/**
	 * @return isAcceptPassiveChecks
	 */
	public boolean isAcceptPassiveChecks() {
		return isAcceptPassiveChecks;
	}

	/**
	 * @param isAcceptPassiveChecks
	 */
	public void setAcceptPassiveChecks(boolean isAcceptPassiveChecks) {
		this.isAcceptPassiveChecks = isAcceptPassiveChecks;
	}

	/**
	 * Boolean property isNotificationsEnabled
	 */
	private boolean isNotificationsEnabled;

	/**
	 * @return isNotificationsEnabled
	 */
	public boolean isNotificationsEnabled() {
		return isNotificationsEnabled;
	}

	/**
	 * @param isNotificationsEnabled
	 */
	public void setNotificationsEnabled(boolean isNotificationsEnabled) {
		this.isNotificationsEnabled = isNotificationsEnabled;
	}

	/**
	 * Boolean property isEventHandlersEnabled
	 */
	private boolean isEventHandlersEnabled;

	/**
	 * @return isEventHandlersEnabled
	 */
	public boolean isEventHandlersEnabled() {
		return isEventHandlersEnabled;
	}

	/**
	 * @param isEventHandlersEnabled
	 */
	public void setEventHandlersEnabled(boolean isEventHandlersEnabled) {
		this.isEventHandlersEnabled = isEventHandlersEnabled;
	}

	/**
	 * Boolean property isFlapDetectionEnabled
	 */
	private boolean isFlapDetectionEnabled;

	/**
	 * @return isFlapDetectionEnabled
	 */
	public boolean isFlapDetectionEnabled() {
		return isFlapDetectionEnabled;
	}

	/**
	 * @param isFlapDetectionEnabled
	 */
	public void setFlapDetectionEnabled(boolean isFlapDetectionEnabled) {
		this.isFlapDetectionEnabled = isFlapDetectionEnabled;
	}

	// /**
	// * Boolean property isObsessedOverHost
	// */
	// private boolean isObsessedOverHost;
	//
	// /**
	// * @return isObsessedOverHost
	// */
	// public boolean isObsessedOverHost() {
	// return isObsessedOverHost;
	// }
	//
	// /**
	// * @param isObsessedOverHost
	// */
	// public void setObsessedOverHost(boolean isObsessedOverHost) {
	// this.isObsessedOverHost = isObsessedOverHost;
	// }

	/**
	 * @return ackChildMenuList - List of menuItems for the 'acknowledge' parent
	 *         menu.
	 */
	public List<MenuItem> getAckChildMenuList() {
		return ackChildMenuList;
	}

	/**
	 * @param ackChildMenuList
	 */
	public void setAckChildMenuList(List<MenuItem> ackChildMenuList) {
		this.ackChildMenuList = ackChildMenuList;
	}

	/**
	 * @param downtimeChildMenuList
	 */
	public void setDowntimeChildMenuList(List<MenuItem> downtimeChildMenuList) {
		this.downtimeChildMenuList = downtimeChildMenuList;
	}

	/**
	 * @return downtimeChildMenuList - List of menuItems for the 'downtime'
	 *         parent menu.
	 */
	public List<MenuItem> getDowntimeChildMenuList() {
		return downtimeChildMenuList;
	}

	/**
	 * @return notificationsChildMenuList - List of menuItems for the
	 *         'notifications' parent menu.
	 */
	public List<MenuItem> getNotificationsChildMenuList() {
		return notificationsChildMenuList;
	}

	/**
	 * @param notificationsChildMenuList
	 */
	public void setNotificationsChildMenuList(
			List<MenuItem> notificationsChildMenuList) {
		this.notificationsChildMenuList = notificationsChildMenuList;
	}

	/**
	 * @return settingsChildMenuList - List of menuItems for the 'settings'
	 *         parent menu.
	 */
	public List<MenuItem> getSettingsChildMenuList() {
		return settingsChildMenuList;
	}

	/**
	 * @param settingsChildMenuList
	 */
	public void setSettingsChildMenuList(List<MenuItem> settingsChildMenuList) {
		this.settingsChildMenuList = settingsChildMenuList;
	}

	/**
	 * @return eventHandlersChildMenuList - List of menuItems for the 'event
	 *         Handlers' parent menu.
	 */
	public List<MenuItem> getEventHandlersChildMenuList() {
		return eventHandlersChildMenuList;
	}

	/**
	 * @param eventHandlersChildMenuList
	 */
	public void setEventHandlersChildMenuList(
			List<MenuItem> eventHandlersChildMenuList) {
		this.eventHandlersChildMenuList = eventHandlersChildMenuList;
	}

	/**
	 * @return chkResultsChildMenuList - List of menuItems for the 'Check
	 *         Results' parent menu.
	 */
	public List<MenuItem> getChkResultsChildMenuList() {
		return chkResultsChildMenuList;
	}

	/**
	 * @param chkResultsChildMenuList
	 */
	public void setChkResultsChildMenuList(
			List<MenuItem> chkResultsChildMenuList) {
		this.chkResultsChildMenuList = chkResultsChildMenuList;
	}

	/**
	 * This list contains child menu items for the parent menu item ,Acknowledge
	 * 
	 */
	private List<MenuItem> ackChildMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * This list contains child menu items for the parent menu item, Downtime
	 * 
	 */
	private List<MenuItem> downtimeChildMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * This list contains child menu items for the parent menu item,
	 * Notifications
	 * 
	 */
	private List<MenuItem> notificationsChildMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * This list contains child menu items for the parent menu item ,Settings
	 * 
	 */
	private List<MenuItem> settingsChildMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * This list contains child menu items for the parent menu item,
	 * eventHandlers
	 * 
	 */
	private List<MenuItem> eventHandlersChildMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * This list contains child menu items for the parent menu item, Check
	 * Results
	 * 
	 */
	private List<MenuItem> chkResultsChildMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * This list contains child menu items for the parent menu item, Connections
	 * 
	 */
	private List<MenuItem> connectionChildMenuList = Collections
			.synchronizedList(new ArrayList<MenuItem>());

	/**
	 * String constant for "Found Null Node Type."
	 */
	private static final String FOUND_NULL_NODE_TYPE = "Found Null Node Type.";

	/**
	 * String constant for "Found Null Property Binding for host "
	 */
	private static final String FOUND_NULL_HOST_PROPERTY_BINDING = "Found Null Property Binding for host ";

	/**
	 * String constant for "Found Null Boolean Properties for host "
	 */
	public static final String FOUND_NULL_HOST_BOOLEAN_PROPERTIES = "Found Null Boolean Properties for host ";

	/**
	 * String constant for "Found Null Boolean Property "
	 */
	public static final String FOUND_NULL_BOOLEAN_PROERTY = "Found Null Boolean Property";

	/**
	 * String constant for "Found Null Property Binding for serivce "
	 */
	private static final String FOUND_NULL_SERVICE_PROPERTY_BINDING = "Found Null Property Binding for host ";

	/**
	 * String constant for "Found Null Boolean Property for service "
	 */
	public static final String FOUND_NULL_SERVICE_BOOLEAN_PROPERTIES = "Found Null Boolean Properties for service ";

	/**
	 * selectedNodeName
	 */
	private String selectedNodeName = Constant.EMPTY_STRING;

	/**
	 * selectedNodeType
	 */
	private NodeType selectedNodeType;

	/**
	 * selectedNodeId
	 */
	private int selectedNodeId = 0;

	/**
	 * String property for command description
	 */
	private String commandDescription;

	/**
	 * @return commandDescription
	 */
	public String getCommandDescription() {
		return commandDescription;
	}

	/**
	 * @param commandDescription
	 */
	public void setCommandDescription(String commandDescription) {
		this.commandDescription = commandDescription;
	}

	/**
	 * @return parentMenuList - list of MenuItem objects for parent menu
	 */
	public List<MenuItem> getParentMenuList() {
		return parentMenuList;
	}

	/**
	 * @param parentMenuList
	 */
	public void setParentMenuList(List<MenuItem> parentMenuList) {
		this.parentMenuList = parentMenuList;
	}

	/**
	 * hidden Field
	 */
	private String hiddenField = Constant.HIDDEN;

	/**
	 * ReferenceTreeMetaModel instance
	 */
	private ReferenceTreeMetaModel referenceTreeModel;

	/**
	 * subpageIntegrator
	 */
	private SubpageIntegrator subpageIntegrator;

	/** The action handler. */
	private ActionHandlerEE actionHandlerEE = null;

	/**
	 * Gets the hidden field.
	 * 
	 * @return the hidden field
	 */
	public String getHiddenField() {
		if (subpageIntegrator.isInStatusViewer() && !isIntervalRender()) {
			// fetch the latest nav params
			subpageIntegrator.setNavigationParameters();
			// check for node type and node Id
			int nodeID = subpageIntegrator.getNodeID();
			NodeType nodeType = subpageIntegrator.getNodeType();
			if (nodeID != selectedNodeId || !nodeType.equals(selectedNodeType)) {
				// take action handler instance
				if (null == actionHandlerEE) {
					actionHandlerEE = (ActionHandlerEE) FacesUtils
							.getManagedBean(Constant.ACTION_HANDLER_MANAGED_BEAN);
				}

				// update node type vals
				selectedNodeType = nodeType;
				selectedNodeName = subpageIntegrator.getNodeName();
				selectedNodeId = nodeID;

				// update action handler values
				actionHandlerEE.update(selectedNodeType, selectedNodeName,
						selectedNodeId);

				// subpage - update node type vals
				setIntervalRender(true);
			}
		}

		if (isIntervalRender()) {
			cleanup();
			// Populates parent and child menus.
			initialize();

		}
		setIntervalRender(false);
		return hiddenField;
	}

	/**
	 * Sets the hidden field.
	 * 
	 * @param hiddenField
	 */
	public void setHiddenField(String hiddenField) {
		this.hiddenField = hiddenField;
	}

	/**
	 * public constructor for ActionBean
	 * 
	 * @throws GWPortalGenericException
	 */
	public ActionBean() throws GWPortalGenericException {
		referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
				.getManagedBean(Constant.REFERENCE_TREE);
		// cleanup();
		// handle subpage integration here
		subpageIntegrator = new SubpageIntegrator();
		handleSubpageIntegration();
		// Populates parent and child menus.
		// initialize();
	}

	/**
	 * Handles the subpage integration: Reads parameters from request in case of
	 * Status Viewer. If portlet is in dashboard, reads preferences.
	 */
	private void handleSubpageIntegration() {
		boolean isPrefSet = subpageIntegrator.doSubpageIntegration(null);
		if (!isPrefSet) {
			/*
			 * as this portlet is not applicable for "Network View", show the
			 * error message to user. If it was in the "Network View", then we
			 * would have to assign Node Type as NETWORK with NodeId as 0.
			 */
			String message = "Portlet placed at wrong place.";
			setError(true);
			setErrorMessage(message);
			LOGGER.error(message);
			return;
		}
		// get the required data from SubpageIntegrator
		this.selectedNodeType = subpageIntegrator.getNodeType();
		this.selectedNodeId = subpageIntegrator.getNodeID();
		this.selectedNodeName = subpageIntegrator.getNodeName();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(new StringBuilder("[Action Bean] # Node Type [")
					.append(selectedNodeType).append("] # Node ID [")
					.append(selectedNodeId).append("]"));
		}
	}

	/**
	 * This method populates the parent and child menus as per the context.
	 * (Host,HostGroup,Service,Service Group)
	 */
	public void initialize() {
		try {
			// Clear the error fields
			setError(false);
			setErrorMessage(Constant.EMPTY_STRING);
			cleanup();
			// Populate parent level menus.
			populateParentMenuItems(selectedNodeType);
			// Populate child menus for each of the parent menus.
			populateChildMenus(selectedNodeType);
		} catch (GWPortalGenericException ex) {
			handleError(
					"com_groundwork_portal_statusviewer_actionsPortlet_menu_error",
					ex);
		}
	}

	/**
	 * This method populates parentMenuList with the menu items based on the
	 * context. Context can be Host/Host Group/Service/Service Group.
	 * 
	 * @param nodeType
	 *            - Host/Host Group/Service/Service Group.
	 * @throws GWPortalGenericException
	 */
	private void populateParentMenuItems(NodeType nodeType)
			throws GWPortalGenericException {
		final String methodName = "populateParentMenuItems() : ";
		if (nodeType != null) {
			/*
			 * Check for the selected node type,accordingly populate the
			 * parentMenuList. Menus to be displayed are context-sensitive.
			 */
			switch (nodeType) {
			case HOST:
				populateParentMenuForHost();
				break;
			case HOST_GROUP:
				populateParentMenuForGroups();
				break;
			case SERVICE:
				populateParentMenuForService();
				break;
			case SERVICE_GROUP:
				populateParentMenuForGroups();
				break;
			default:
				break;
			}
		} else {
			handleError(
					"com_groundwork_portal_statusviewer_actionsPortlet_menu_error",
					Constant.METHOD + methodName + FOUND_NULL_NODE_TYPE);
			throw new GWPortalGenericException();
		}
	}

	/**
	 * This method populates parentMenuList with the menu items based on the
	 * context. Context can be Host/Host Group/Service/Service Group.
	 */
	private void populateChildMenus(NodeType nodeType)
			throws GWPortalGenericException {
		final String methodName = " populateChildMenus() : ";
		try {
			/*
			 * Check for parent menu option. e.g. Acknowledge.For that parent
			 * menu option,identify all the children menu items. Check if the
			 * option is to be changed with state.If yes,retrieve the current
			 * state,accordingly set the option to be displayed in the child
			 * list.
			 */
			if (nodeType != null) {
				/*
				 * Check for the selected node type,accordingly populate the
				 * childMenuList. Menus to be displayed are context-sensitive.
				 */
				switch (nodeType) {
				case HOST:
					populateHostChildMenus();
					break;
				case HOST_GROUP:
					populateHostGroupChildMenus();
					break;
				case SERVICE:
					populateServiceChildMenus();
					break;
				case SERVICE_GROUP:
					populateServiceGroupChildMenus();
					break;
				default:
					break;
				}
			} else {
				handleError(
						"com_groundwork_portal_statusviewer_actionsPortlet_menu_error",
						Constant.METHOD + methodName + FOUND_NULL_NODE_TYPE);
				return;
			}
		} catch (GWPortalGenericException ex) {
			handleError(
					"com_groundwork_portal_statusviewer_actionsPortlet_menu_error",
					ex);
			throw ex;
		}
	}

	/**
	 * This method populates childMenuList with the menu items for the host
	 * context. Implements the logic for 'change with state'. e.g. If
	 * notifications are already enabled for a particular host,then 'disable
	 * notifications' menu should be displayed.
	 * 
	 * @throws GWPortalGenericException
	 */
	private void populateHostChildMenus() {
		for (MenuItem parentMenu : parentMenuList) {
			// Get the parent menu enum.
			ParentMenuActionEnum parentActionEnum = ParentMenuActionEnum
					.getParentMenuActionEnum(parentMenu.getValue().toString());
			if (parentMenu.getChildren() != null) {
				parentMenu.getChildren().clear();
				switch (parentActionEnum) {
				case ACKNOWLEDGE:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(getAckChildMenuForHost());
					break;
				case DOWNTIME:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getDownTimeChildMenuForHost());
					break;
				case NOTIFICATIONS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getNotificationsChildMenuForHost());
					break;
				case SETTINGS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getSettingsChildMenuForHost());
					break;
				case EVENT_HANDLERS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getEventHandlersChildMenuForHost());
					break;
				case CHECK_RESULTS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getChkResultsChildMenuForHost());
					break;
				case CONNECTIONS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getConnectionChildMenuForHost());
					break;

				default:
					break;
				}
			}
		}
	}

	/**
	 * This method populates childMenuList with the menu items for the service
	 * context. Implements the logic for 'change with state'. e.g. If
	 * notifications are already enabled for a particular service,then 'disable
	 * notifications' menu should be displayed.
	 * 
	 * @throws GWPortalGenericException
	 */
	private void populateServiceChildMenus() {
		for (MenuItem parentMenu : parentMenuList) {
			// Get the parent menu enum.
			ParentMenuActionEnum parentActionEnum = ParentMenuActionEnum
					.getParentMenuActionEnum(parentMenu.getValue().toString());
			if (parentMenu.getChildren() != null) {
				parentMenu.getChildren().clear();
				switch (parentActionEnum) {
				case ACKNOWLEDGE:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren()
							.addAll(getAckChildMenuForService());
					break;
				case DOWNTIME:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getDownTimeChildMenuForService());
					break;
				case NOTIFICATIONS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getNotificationsChildMenuForService());
					break;
				case SETTINGS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getSettingsChildMenuForService());
					break;
				case EVENT_HANDLERS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getEventHandlersChildMenuForService());
					break;
				case CHECK_RESULTS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getChkResultsChildMenuForService());
					break;
				case CONNECTIONS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getConnectionChildMenuForService());
					break;
				default:
					break;
				}
			}
		}
	}


    /**
	 * This method retrieves the host object containing all the dynamic
	 * properties using web service call HostWSFacade.getHostsByName(hostname)
	 */
	private void getHostDynamicProperties() throws GWPortalGenericException {
		// LOGGER
		// .error(
		// "!!!!!!!!ACTION PORTLETTTTTTTTTTTTTTT !!!!!!!!!! Calling getHostDynamicProperties........"
		// );
		Host host = foundationWSFacade.getHostsById(String
				.valueOf(selectedNodeId));
		if (host == null) {
			LOGGER.debug(NULL_HOST);
			throw new GWPortalGenericException(
					ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_menu_error"));
		}

		// Retrieve monitor status of host
		MonitorStatus monitorStatus = host.getMonitorStatus();
		if (monitorStatus == null) {
			LOGGER.debug("getHostDynamicProperties() : Monitor Status for host "
					+ host.getName() + " is null");
			throw new GWPortalGenericException(
					ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_menu_error"));
		}

		setAcknowledgeState(true);
		/*
		 * Set acknowledge status based on monitor status. If UP/PENDING do not
		 * show acknowledge option.
		 */
		if (monitorStatus.getName().equalsIgnoreCase(Constant.UP)
				|| monitorStatus.getName().equalsIgnoreCase(Constant.PENDING)) {
			setAcknowledgeState(false);
		}

		PropertyTypeBinding propertyBindings = host.getPropertyTypeBinding();
		if (propertyBindings == null) {
			LOGGER.debug(FOUND_NULL_HOST_PROPERTY_BINDING + host.getName());
			throw new GWPortalGenericException();
		}
		BooleanProperty[] booleanProperties = propertyBindings.getBooleanProperty();
		if (booleanProperties == null) {
            booleanProperties = DEFAULT_BOOLEAN_PROPERTIES;
		}
        for (BooleanProperty booleanProperty : booleanProperties) {
            if (booleanProperty == null) {
                LOGGER.debug(FOUND_NULL_BOOLEAN_PROERTY);
                // throw new GWPortalGenericException();
            } else {
                // Check if booleanProperty is 'isAcknowledged'
                if (Constant.IS_ACKNOWLEDGED.equals(booleanProperty
                        .getName())) {
                    setAcknowledged(booleanProperty.isValue());
                } else
                // Check if booleanProperty is
                // 'isNotificationsEnabled'
                if (NagiosStatisticsConstants.IS_NOTIFICATIONS_ENABLED_PROPERTY
                        .equals(booleanProperty.getName())) {
                    setNotificationsEnabled(booleanProperty.isValue());

                } else // Check if booleanProperty is
                // 'isChecksEnabled'
                if (NagiosStatisticsConstants.IS_ACTIVECHECKS_ENABLED_PROPERTY
                        .equals(booleanProperty.getName())) {
                    setChecksEnabled(booleanProperty.isValue());
                } else // Check if booleanProperty is
                // 'isAcceptPassiveChecks'
                if (NagiosStatisticsConstants.IS_PASSIVECHECKS_ENABLED_PROPERTY
                        .equals(booleanProperty.getName())) {
                    setAcceptPassiveChecks(booleanProperty.isValue());
                } else // Check if booleanProperty is
                // 'isFlapDetectionEnabled'
                if (NagiosStatisticsConstants.IS_FLAP_DETECTION_ENABLED_PROPERTY
                        .equals(booleanProperty.getName())) {
                    setFlapDetectionEnabled(booleanProperty.isValue());
                } else // Check if booleanProperty is
                // 'isEventHandlersEnabled'
                if (NagiosStatisticsConstants.IS_EVENT_HANDLERS_ENABLED_PROPERTY
                        .equals(booleanProperty.getName())) {
                    setEventHandlersEnabled(booleanProperty.isValue());
                }
            }
        }
		// // For 'isObsessOverHost' integerProperty.
		// // Get the IntegerProperty object for 'isObsessOverHost'.
		// IntegerProperty integerProperty = propertyBindings
		// .getIntegerProperty(NagiosStatisticsConstants.
		// IS_OBSESSED_OVER_HOST_PROPERTY);
		// if (integerProperty != null) {
		// if (integerProperty.getValue() == 0) {
		// setObsessedOverHost(false);
		// } else {
		// setObsessedOverHost(true);
		// }
		// }
	}

	/**
	 * This method retrieves the service object containing all the dynamic
	 * properties using web service call HostWSFacade.getHostsByName(hostname)
	 */
	private void getServicesDynamicProperties() throws GWPortalGenericException {
		ServiceStatus serviceStatus = foundationWSFacade
				.getServicesById(selectedNodeId);
		if (serviceStatus == null) {
			LOGGER.debug(NULL_SERVICE);
			throw new GWPortalGenericException(
					ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_menu_error"));
		}

		// Retrieve monitor status of service
		// MonitorStatus monitorStatus = serviceStatus.getMonitorStatus();
		// if (monitorStatus == null) {
		// LOGGER
		// .error("getServiceDynamicProperties() : Monitor Status for service "
		// + serviceStatus.getDescription() + " is null");
		// throw new GWPortalGenericException(
		// ResourceUtils
		// .getLocalizedMessage(
		// "com_groundwork_portal_statusviewer_actionsPortlet_menu_error"));
		// }

		setAcknowledgeState(true);

		NetworkMetaEntity serviceNetworkMetaEntity = referenceTreeModel
				.getServiceById(selectedNodeId);
		String serviceCurrentStatus = NetworkObjectStatusEnum.NO_STATUS
				.getStatus();
		if (null != serviceNetworkMetaEntity) {
			// make use of service status from RefrenceTreeMEtaModel over here
			serviceCurrentStatus = serviceNetworkMetaEntity.getStatus()
					.getStatus();
		}
		/*
		 * Set acknowledge status based on monitor status. If OK/PENDING do not
		 * show acknowledge option.
		 */
		if (serviceCurrentStatus.equalsIgnoreCase(Constant.OK)
				|| serviceCurrentStatus.equalsIgnoreCase(Constant.PENDING)) {
			setAcknowledgeState(false);
		}

		PropertyTypeBinding propertyBindings = serviceStatus
				.getPropertyTypeBinding();
		if (propertyBindings == null) {
			LOGGER.debug(FOUND_NULL_SERVICE_PROPERTY_BINDING
					+ serviceStatus.getDescription());
			throw new GWPortalGenericException(
					ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_menu_error"));
		}
		BooleanProperty[] booleanProperties = propertyBindings
				.getBooleanProperty();
		if (booleanProperties == null) {
			this.rendered = false;
			LOGGER.info(FOUND_NULL_SERVICE_BOOLEAN_PROPERTIES
					+ serviceStatus.getDescription());
			throw new GWPortalGenericException(
					ResourceUtils
							.getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_menu_error"));
		}
		for (BooleanProperty booleanProperty : booleanProperties) {
			if (booleanProperty == null) {
				LOGGER.debug(FOUND_NULL_BOOLEAN_PROERTY);
			} else {
				// Check if booleanProperty is 'isProblemAcknowledged'
				if (Constant.IS_PROBLEM_ACKNOWLEDGED.equals(booleanProperty
						.getName())) {
					setAcknowledged(booleanProperty.isValue());
				} else
				// Check if booleanProperty is
				// 'isNotificationsEnabled'
				if (NagiosStatisticsConstants.IS_NOTIFICATIONS_ENABLED_PROPERTY
						.equals(booleanProperty.getName())) {
					setNotificationsEnabled(booleanProperty.isValue());

				} else // Check if booleanProperty is
				// 'isChecksEnabled'
				if (NagiosStatisticsConstants.IS_ACTIVECHECKS_ENABLED_PROPERTY
						.equals(booleanProperty.getName())) {
					setChecksEnabled(booleanProperty.isValue());
				} else // Check if booleanProperty is
				// 'isAcceptPassiveChecks'
				if (NagiosStatisticsConstants.IS_ACCEPT_PASSIVECHECKS_PROPERTY
						.equals(booleanProperty.getName())) {
					setAcceptPassiveChecks(booleanProperty.isValue());
				} else // Check if booleanProperty is
				// 'isFlapDetectionEnabled'
				if (NagiosStatisticsConstants.IS_FLAP_DETECTION_ENABLED_PROPERTY
						.equals(booleanProperty.getName())) {
					setFlapDetectionEnabled(booleanProperty.isValue());
				} else // Check if booleanProperty is
				// 'isEventHandlersEnabled'
				if (NagiosStatisticsConstants.IS_EVENT_HANDLERS_ENABLED_PROPERTY
						.equals(booleanProperty.getName())) {
					setEventHandlersEnabled(booleanProperty.isValue());
				}
			}
		}
	}

	/**
	 * This method populates the child menu list for parent menu 'Acknowledge'
	 * 
	 * @return ackChildMenuList
	 */
	private List<MenuItem> getAckChildMenuForHost() {
		for (HostActionEnum.Acknowledge ackEnum : HostActionEnum.Acknowledge
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));

			// Check if the enum is 'Remove Acknowledgment of this
			// host problem'
			if (ackEnum.getActionCommand().equals(
					ActionCommandsConstants.REMOVE_HOST_ACKNOWLEDGEMENT)) {
				if (isAcknowledged()) {
					menuItem.setId(ackEnum.name());
					menuItem.setValue(ackEnum.getActionCommand());
				} else {
					continue;
				}
			} else // Check if the enum is 'Acknowledge This Host
			// Problem'
			if (ackEnum.getActionCommand().equals(
					ActionCommandsConstants.ACK_HOST_PROB)) {
				if (!isAcknowledged()) {
					menuItem.setId(ackEnum.name());
					menuItem.setValue(ackEnum.getActionCommand());
				} else {
					continue;
				}
			} else { // For all other menus
				menuItem.setId(ackEnum.name());
				menuItem.setValue(ackEnum.getActionCommand());
			}
			// Check for change with state.
			ackChildMenuList.add(menuItem);
		}
		return ackChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Acknowledge'
	 * for the service context.
	 * 
	 * @return ackChildMenuList
	 */
	private List<MenuItem> getAckChildMenuForService() {
		for (ServiceActionEnum.Acknowledge ackEnum : ServiceActionEnum.Acknowledge
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			// Check for enum 'Acknowledge This service Problem'
			if (ActionCommandsConstants.ACKNOWLEDGE_SVC_PROBLEM.equals(ackEnum
					.getActionCommand())) {
				if (!isAcknowledged()) {
					menuItem.setId(ackEnum.name());
					menuItem.setValue(ackEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			/*
			 * Check for enum 'Remove Problem Acknowledgment for this service'
			 */
			if (ActionCommandsConstants.REMOVE_SVC_ACKNOWLEDGEMENT
					.equals(ackEnum.getActionCommand())) {
				if (isAcknowledged()) {
					menuItem.setId(ackEnum.name());
					menuItem.setValue(ackEnum.getActionCommand());
				} else {
					continue;
				}
			} else {
				menuItem.setId(ackEnum.name());
				menuItem.setValue(ackEnum.getActionCommand());
			}
			ackChildMenuList.add(menuItem);
		}
		return ackChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Check Result'
	 * for the host context
	 * 
	 * @return chkResultsChildMenuList
	 */
	private List<MenuItem> getChkResultsChildMenuForHost() {
		// Populate the chkResultsChildMenuList
		for (HostActionEnum.CheckResults chkResultsEnum : HostActionEnum.CheckResults
				.values()) {
			MenuItem menuItem = new MenuItem();
			menuItem.setId(chkResultsEnum.name());
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));

			menuItem.setValue(chkResultsEnum.getActionCommand());
			chkResultsChildMenuList.add(menuItem);
		}
		return chkResultsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'connections'
	 * for the host context
	 * 
	 * @return chkResultsChildMenuList
	 */
	private List<MenuItem> getConnectionChildMenuForHost() {
		readConnectionURLSFromProperties();
		Set<Entry<String, String>> entrySet = CONNECTION_URLS_MAP.entrySet();
		Iterator<Entry<String, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String displayString = entry.getKey();
			String connectionUrlProp = entry.getValue();
			String connectionUrl = replaceDollarParams(connectionUrlProp,
					selectedNodeName);
			MenuItem menuItem = new MenuItem();
			menuItem.setId(displayString);
			menuItem.setLink(connectionUrl);
			menuItem.setValue(displayString);
			connectionChildMenuList.add(menuItem);

		}

		return connectionChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'connections'
	 * for the host context
	 * 
	 * @return chkResultsChildMenuList
	 */
	private List<MenuItem> getConnectionChildMenuForService() {
		readConnectionURLSFromProperties();
		Set<Entry<String, String>> entrySet = CONNECTION_URLS_MAP.entrySet();
		Iterator<Entry<String, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String displayString = entry.getKey();
			String connectionUrlProp = entry.getValue();
			String connectionUrl = replaceDollarParams(connectionUrlProp,
					selectedNodeName);
			MenuItem menuItem = new MenuItem();
			menuItem.setId(displayString);
			menuItem.setLink(connectionUrl);
			menuItem.setValue(displayString);
			connectionChildMenuList.add(menuItem);

		}

		return connectionChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Check Result'
	 * for the service context
	 * 
	 * @return chkResultsChildMenuList
	 */
	private List<MenuItem> getChkResultsChildMenuForService() {
		for (ServiceActionEnum.CheckResults chkResultsEnum : ServiceActionEnum.CheckResults
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(chkResultsEnum.name());
			menuItem.setValue(chkResultsEnum.getActionCommand());
			chkResultsChildMenuList.add(menuItem);
		}
		return chkResultsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Downtime' for
	 * the host context
	 * 
	 * @return downtimeChildMenuList
	 */
	private List<MenuItem> getDownTimeChildMenuForHost() {
		// Populate the downtimeChildMenuList
		for (HostActionEnum.Downtime downtimeEnum : HostActionEnum.Downtime
				.values()) {
			MenuItem menuItem = new MenuItem();
			menuItem.setId(downtimeEnum.name());
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));

			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));

			menuItem.setValue(downtimeEnum.getActionCommand());
			downtimeChildMenuList.add(menuItem);
		}
		return downtimeChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Downtime' for
	 * the service context
	 * 
	 * @return downtimeChildMenuList
	 */
	private List<MenuItem> getDownTimeChildMenuForService() {
		// Populate the downtimeChildMenuList
		for (ServiceActionEnum.Downtime downtimeEnum : ServiceActionEnum.Downtime
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(downtimeEnum.name());
			menuItem.setValue(downtimeEnum.getActionCommand());
			downtimeChildMenuList.add(menuItem);
		}
		return downtimeChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Downtime' for
	 * the service group context
	 * 
	 * @return downtimeChildMenuList
	 */
	private List<MenuItem> getDownTimeChildMenuForServiceGroup() {
		// Populate the downtimeChildMenuList
		for (ServiceGroupActionEnum.Downtime downtimeEnum : ServiceGroupActionEnum.Downtime
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(downtimeEnum.name());
			menuItem.setValue(downtimeEnum.getActionCommand());
			downtimeChildMenuList.add(menuItem);
		}
		return downtimeChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Downtime' for
	 * the host group context
	 * 
	 * @return downtimeChildMenuList
	 */
	private List<MenuItem> getDownTimeChildMenuForHostGroup() {
		// Populate the downtimeChildMenuList
		for (HostGroupActionEnum.Downtime downtimeEnum : HostGroupActionEnum.Downtime
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(downtimeEnum.name());
			menuItem.setValue(downtimeEnum.getActionCommand());
			downtimeChildMenuList.add(menuItem);
		}
		return downtimeChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Notifications'
	 * for the host context
	 * 
	 * @return notificationsChildMenuList
	 */
	private List<MenuItem> getNotificationsChildMenuForHost() {
		// Populate the notificationsChildMenuList
		for (HostActionEnum.Notifications notificationsEnum : HostActionEnum.Notifications
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			if (ActionCommandsConstants.DISABLE_HOST_NOTIFICATIONS
					.equals(notificationsEnum.getActionCommand())) {
				if (isNotificationsEnabled()) {
					menuItem.setId(notificationsEnum.name());
					menuItem.setValue(notificationsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Delay next Notifications'
			// for host
			if (ActionCommandsConstants.DELAY_HOST_NOTIFICATION
					.equals(notificationsEnum.getActionCommand())) {

				if (isNotificationsEnabled()) {
					menuItem.setId(notificationsEnum.name());
					menuItem.setValue(notificationsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Enable Notifications' for host
			if (ActionCommandsConstants.ENABLE_HOST_NOTIFICATIONS
					.equals(notificationsEnum.getActionCommand())) {
				if (!isNotificationsEnabled()) {
					menuItem.setId(notificationsEnum.name());
					menuItem.setValue(notificationsEnum.getActionCommand());
				} else {
					continue;
				}
			} else {
				// For all other menus
				menuItem.setId(notificationsEnum.name());
				menuItem.setValue(notificationsEnum.getActionCommand());
			}
			notificationsChildMenuList.add(menuItem);
		}
		return notificationsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Notifications'
	 * for the service context
	 * 
	 * @return notificationsChildMenuList
	 */
	private List<MenuItem> getNotificationsChildMenuForService() {
		// Populate the notificationsChildMenuList
		for (ServiceActionEnum.Notifications notificationsEnum : ServiceActionEnum.Notifications
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			// Check for enum 'Enable Notifications' for this service
			if (ActionCommandsConstants.ENABLE_SVC_NOTIFICATIONS
					.equals(notificationsEnum.getActionCommand())) {
				if (!isNotificationsEnabled()) {
					menuItem.setId(notificationsEnum.name());
					menuItem.setValue(notificationsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check for enum 'Disable Notifications' for this service
			if (ActionCommandsConstants.DISABLE_SVC_NOTIFICATIONS
					.equals(notificationsEnum.getActionCommand())) {
				if (isNotificationsEnabled()) {
					menuItem.setId(notificationsEnum.name());
					menuItem.setValue(notificationsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			/*
			 * Check if the enum is 'Delay next Notifications' for service
			 */
			if (ActionCommandsConstants.DELAY_HOST_NOTIFICATION
					.equals(notificationsEnum.getActionCommand())) {

				if (isNotificationsEnabled()) {
					menuItem.setId(notificationsEnum.name());
					menuItem.setValue(notificationsEnum.getActionCommand());
				} else {
					continue;
				}
			} else {
				menuItem.setId(notificationsEnum.name());
				menuItem.setValue(notificationsEnum.getActionCommand());
			}
			notificationsChildMenuList.add(menuItem);
		}
		return notificationsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Notifications'
	 * for the service group context
	 * 
	 * @return notificationsChildMenuList
	 */
	private List<MenuItem> getNotificationsChildMenuForServiceGroup() {
		// Populate the notificationsChildMenuList
		for (ServiceGroupActionEnum.Notifications notificationsEnum : ServiceGroupActionEnum.Notifications
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(notificationsEnum.name());
			menuItem.setValue(notificationsEnum.getActionCommand());
			notificationsChildMenuList.add(menuItem);
		}
		return notificationsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Notifications'
	 * for the host group context
	 * 
	 * @return notificationsChildMenuList
	 */
	private List<MenuItem> getNotificationsChildMenuForHostGroup() {
		// Populate the notificationsChildMenuList
		for (HostGroupActionEnum.Notifications notificationsEnum : HostGroupActionEnum.Notifications
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(notificationsEnum.name());
			menuItem.setValue(notificationsEnum.getActionCommand());
			notificationsChildMenuList.add(menuItem);
		}
		return notificationsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Event
	 * Handlers' for the host context
	 * 
	 * @return eventHandlersChildMenuList
	 */
	private List<MenuItem> getEventHandlersChildMenuForHost() {
		for (HostActionEnum.EventHandlers eventHanldersEnum : HostActionEnum.EventHandlers
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));

			// Check if the enum is 'Enable Event Handler'
			if (ActionCommandsConstants.ENABLE_HOST_EVENT_HANDLER
					.equals(eventHanldersEnum.getActionCommand())) {
				if (!isEventHandlersEnabled()) {
					menuItem.setId(eventHanldersEnum.name());
					menuItem.setValue(eventHanldersEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Disable Event Handler'
			if (ActionCommandsConstants.DISABLE_HOST_EVENT_HANDLER
					.equals(eventHanldersEnum.getActionCommand())) {
				if (isEventHandlersEnabled()) {
					menuItem.setId(eventHanldersEnum.name());
					menuItem.setValue(eventHanldersEnum.getActionCommand());
				} else {
					continue;
				}
			} else {
				// For all other menus
				menuItem.setId(eventHanldersEnum.name());
				menuItem.setValue(eventHanldersEnum.getActionCommand());
			}
			eventHandlersChildMenuList.add(menuItem);
		}
		return eventHandlersChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Event
	 * Handlers' for the service context
	 * 
	 * @return eventHandlersChildMenuList
	 */
	private List<MenuItem> getEventHandlersChildMenuForService() {
		// Populate the eventHandlersChildMenuList
		for (ServiceActionEnum.EventHandlers eventHanldersEnum : ServiceActionEnum.EventHandlers
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			// Check if the enum is 'Enable Event Handler' for service
			if (ActionCommandsConstants.ENABLE_SVC_EVENT_HANDLER
					.equals(eventHanldersEnum.getActionCommand())) {
				if (!isEventHandlersEnabled()) {
					menuItem.setId(eventHanldersEnum.name());
					menuItem.setValue(eventHanldersEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Disable Event Handler' for
			// service
			if (ActionCommandsConstants.DISABLE_SVC_EVENT_HANDLER
					.equals(eventHanldersEnum.getActionCommand())) {
				if (isEventHandlersEnabled()) {
					menuItem.setId(eventHanldersEnum.name());
					menuItem.setValue(eventHanldersEnum.getActionCommand());
				} else {
					continue;
				}
			}
			menuItem.setId(eventHanldersEnum.name());
			menuItem.setValue(eventHanldersEnum.getActionCommand());

			eventHandlersChildMenuList.add(menuItem);
		}
		return eventHandlersChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Settings' for
	 * the host context.
	 * 
	 * @return settingsChildMenuList
	 */
	private List<MenuItem> getSettingsChildMenuForHost() {
		// Populate the settingsChildMenuList
		for (HostActionEnum.Settings settingsEnum : HostActionEnum.Settings
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));

			// Check if the enum is 'Disable Checks On This Host'
			if (ActionCommandsConstants.DISABLE_HOST_CHECK.equals(settingsEnum
					.getActionCommand())) {
				if (isChecksEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Enable Checks On This Host'
			if (ActionCommandsConstants.ENABLE_HOST_CHECK.equals(settingsEnum
					.getActionCommand())) {
				if (!isChecksEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Enable Passive Checks'
			if (ActionCommandsConstants.ENABLE_PASSIVE_HOST_CHECKS
					.equals(settingsEnum.getActionCommand())) {
				if (!isAcceptPassiveChecks()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Disable Passive Checks'
			if (ActionCommandsConstants.DISABLE_PASSIVE_HOST_CHECKS
					.equals(settingsEnum.getActionCommand())) {
				if (isAcceptPassiveChecks()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Enable Flap Detection'
			if (ActionCommandsConstants.ENABLE_HOST_FLAP_DETECTION
					.equals(settingsEnum.getActionCommand())) {
				if (!isFlapDetectionEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else
			// Check if the enum is 'Disable Flap Detection'
			if (ActionCommandsConstants.DISABLE_HOST_FLAP_DETECTION
					.equals(settingsEnum.getActionCommand())) {
				if (isFlapDetectionEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else {
				// for all other menus
				menuItem.setId(settingsEnum.name());
				menuItem.setValue(settingsEnum.getActionCommand());
			}

			// NOTE: keeping code for future reference
			// else
			// // Check if the enum is 'Start Obsessing Over This Host'
			// if (ActionCommandsConstants.START_OBSESSING_OVER_HOST
			// .equals(settingsEnum.getActionCommand())) {
			// if (!isObsessedOverHost()) {
			// menuItem.setId(settingsEnum.name());
			// menuItem.setValue(settingsEnum.getActionCommand());
			// } else {
			// continue;
			// }
			// } else
			// // Check if the enum is 'Stop Obsessing Over This Host'
			// if (ActionCommandsConstants.STOP_OBSESSING_OVER_HOST
			// .equals(settingsEnum.getActionCommand())) {
			// if (isObsessedOverHost()) {
			// menuItem.setId(settingsEnum.name());
			// menuItem.setValue(settingsEnum.getActionCommand());
			// } else {
			// continue;
			// }
			// }

			settingsChildMenuList.add(menuItem);
		}
		return settingsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Settings' for
	 * the service context.
	 * 
	 * @return settingsChildMenuList
	 */
	private List<MenuItem> getSettingsChildMenuForService() {
		// Populate the settingsChildMenuList
		for (ServiceActionEnum.Settings settingsEnum : ServiceActionEnum.Settings
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			// Check for enum 'Disable Checks On This Service'
			if (ActionCommandsConstants.DISABLE_SVC_CHECK.equals(settingsEnum
					.getActionCommand())) {
				if (isChecksEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else // Check for enum 'Enable Checks On This Service'
			if (ActionCommandsConstants.ENABLE_SVC_CHECK.equals(settingsEnum
					.getActionCommand())) {
				if (!isChecksEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else /*
					 * Check for enum 'Disable Passive Checks for this Service'
					 */
			if (ActionCommandsConstants.DISABLE_PASSIVE_SVC_CHECKS
					.equals(settingsEnum.getActionCommand())) {
				if (isAcceptPassiveChecks()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else /*
					 * Check for enum 'Enable Passive Checks for this Service'
					 */
			if (ActionCommandsConstants.ENABLE_PASSIVE_SVC_CHECKS
					.equals(settingsEnum.getActionCommand())) {
				if (!isAcceptPassiveChecks()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else /*
					 * Check for enum 'Enable Passive Checks for this Service'
					 */
			if (ActionCommandsConstants.ENABLE_SVC_FLAP_DETECTION
					.equals(settingsEnum.getActionCommand())) {
				if (!isFlapDetectionEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else /*
					 * Check for enum 'Disable Passive Checks for this Service'
					 */
			if (ActionCommandsConstants.DISABLE_SVC_FLAP_DETECTION
					.equals(settingsEnum.getActionCommand())) {
				if (isFlapDetectionEnabled()) {
					menuItem.setId(settingsEnum.name());
					menuItem.setValue(settingsEnum.getActionCommand());
				} else {
					continue;
				}
			} else {
				menuItem.setId(settingsEnum.name());
				menuItem.setValue(settingsEnum.getActionCommand());
			}
			settingsChildMenuList.add(menuItem);
		}
		return settingsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Settings' for
	 * the service group context.
	 * 
	 * @return settingsChildMenuList
	 */
	private List<MenuItem> getSettingsChildMenuForServiceGroup() {
		// Populate the settingsChildMenuList
		for (ServiceGroupActionEnum.Settings settingsEnum : ServiceGroupActionEnum.Settings
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(settingsEnum.name());
			menuItem.setValue(settingsEnum.getActionCommand());
			settingsChildMenuList.add(menuItem);
		}
		return settingsChildMenuList;
	}

	/**
	 * This method populates the child menu list for parent menu 'Settings' for
	 * the host group context.
	 * 
	 * @return settingsChildMenuList
	 */
	private List<MenuItem> getSettingsChildMenuForHostGroup() {
		// Populate the settingsChildMenuList
		for (HostGroupActionEnum.Settings settingsEnum : HostGroupActionEnum.Settings
				.values()) {
			MenuItem menuItem = new MenuItem();
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_CHILD_ACTION_MENUS));
			menuItem.setId(settingsEnum.name());
			menuItem.setValue(settingsEnum.getActionCommand());
			settingsChildMenuList.add(menuItem);
		}
		return settingsChildMenuList;
	}

	/**
	 * This method populates childMenuList with the menu items for the service
	 * groups context. Implements the logic for 'change with state'. e.g. If
	 * notifications are already enabled for a particular service group,then
	 * 'disable notifications' menu should be displayed.
	 * 
	 * @throws GWPortalGenericException
	 */
	private void populateServiceGroupChildMenus()
			throws GWPortalGenericException {
		for (MenuItem parentMenu : parentMenuList) {
			// Get the parent menu enum.
			ParentMenuActionEnum parentActionEnum = ParentMenuActionEnum
					.getParentMenuActionEnum(parentMenu.getValue().toString());
			if (parentMenu.getChildren() != null) {
				parentMenu.getChildren().clear();
				switch (parentActionEnum) {
				case DOWNTIME:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getDownTimeChildMenuForServiceGroup());
					break;
				case NOTIFICATIONS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getNotificationsChildMenuForServiceGroup());
					break;
				case SETTINGS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getSettingsChildMenuForServiceGroup());
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * This method populates childMenuList with the menu items for the host
	 * groups context. Implements the logic for 'change with state'. e.g. If
	 * notifications are already enabled for a particular host group,then
	 * 'disable notifications' menu should be displayed.
	 * 
	 * @throws GWPortalGenericException
	 */
	private void populateHostGroupChildMenus() throws GWPortalGenericException {
		for (MenuItem parentMenu : parentMenuList) {
			// Get the parent menu enum.
			ParentMenuActionEnum parentActionEnum = ParentMenuActionEnum
					.getParentMenuActionEnum(parentMenu.getValue().toString());
			if (parentMenu.getChildren() != null) {
				parentMenu.getChildren().clear();
				switch (parentActionEnum) {
				case DOWNTIME:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getDownTimeChildMenuForHostGroup());
					break;
				case NOTIFICATIONS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getNotificationsChildMenuForHostGroup());
					break;
				case SETTINGS:
					// Add the sub/child menu-list to the parent menu list
					parentMenu.getChildren().addAll(
							getSettingsChildMenuForHostGroup());
					break;
				default:
					break;
				}
			}
		}
	}

	// /**
	// * Creates an action listener method for the menu
	// *
	// * @param actionListenerString
	// * @return
	// */
	// @SuppressWarnings("deprecation")
	// private MethodBinding createActionListenerMethodBinding(
	// String actionListenerString) {
	// Class[] args = { ActionEvent.class };
	// MethodBinding methodBinding = null;
	//
	// methodBinding = FacesContext.getCurrentInstance().getApplication()
	// .createMethodBinding(actionListenerString, args);
	// return methodBinding;
	// }

	/**
	 * Populates the parentMenuList with the menu items for Host context.
	 */
	private void populateParentMenuForHost() {

		try {
			// Fetch all the dynamic properties for this host.
			getHostDynamicProperties();
		} catch (GWPortalGenericException ex) {
			if (this.rendered)
			handleError(
					"com_groundwork_portal_statusviewer_actionsPortlet_menu_error",
					ex);
			else
			return;
		}
		createParentMenuList();
	}

	/**
	 * Populates the parentMenuList with the menu items for service context.
	 */
	private void populateParentMenuForService() {

		try {
			// Fetch all the dynamic properties for this service.
			getServicesDynamicProperties();
		} catch (GWPortalGenericException ex) {
			if (this.rendered)
			handleError(
					"com_groundwork_portal_statusviewer_actionsPortlet_menu_error",
					ex);
			else
			return;
		}
		createParentMenuList();
	}

	/**
	 * Populates items in parent menu list.
	 */
	private void createParentMenuList() {
		for (ParentMenuActionEnum menuEnum : ParentMenuActionEnum.values()) {

			// If the host or service is in a state of acknowledgment
			if ((menuEnum == ParentMenuActionEnum.ACKNOWLEDGE)
					&& (!acknowledgeState)) {
				continue;
			}
			// Else add to parent menu list
			MenuItem menuItem = new MenuItem();
			// Apply style class
			menuItem.setStyleClass(Constant.ACTION_MENU_ITEMS_STYLE_CLASS);
			menuItem.setValue(menuEnum.getMenuString());
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_PARENT_ACTION_MENUS));
			parentMenuList.add(menuItem);
		}
	}

	/**
	 * This method is called when any menu from the parentMenuList gets clicked
	 * 
	 * @param event
	 */
	public void parentMenuListener(ActionEvent event) {
		try {
			cleanChildMenus();
			populateChildMenus(selectedNodeType);
		} catch (GWPortalGenericException ex) {
			LOGGER.info(ex.getMessage());
			setError(true);
			setErrorMessage(ex.getMessage());
		}
	}

	/**
	 * Populates the parentMenuList with the menu items for Host Group or
	 * Service Group context.
	 */
	private void populateParentMenuForGroups() {
		if (this.selectedNodeType == NodeType.HOST_GROUP) {
			try {
				HostGroup hostGroup = foundationWSFacade
						.getHostGroupsById(this.selectedNodeId);
				if (hostGroup.getApplicationName().equalsIgnoreCase(
						Constant.VEMA)) {
					this.error = true;
					return;
				}
			} catch (Exception exc) {
				Log.error(exc.getMessage());
			}
		}
		for (ParentMenuActionsForGroup menuEnum : ParentMenuActionsForGroup
				.values()) {
			MenuItem menuItem = new MenuItem();
			// Apply style class
			menuItem.setStyleClass(Constant.ACTION_MENU_ITEMS_STYLE_CLASS);
			menuItem.setValue(menuEnum.getMenuString());
			// menuItem
			// .setActionListener(createActionListenerMethodBinding(Constant.
			// ACTION_LISTENER_FOR_PARENT_ACTION_MENUS));
			// menuItem.setIcon(MENU_ICON_PATH);
			parentMenuList.add(menuItem);
		}
	}

	/**
	 * This method cleans up the parent menu list.
	 */
	public void cleanParentMenus() {
		this.parentMenuList.clear();
	}

	/**
	 * This method cleans up the child menu lists.
	 */
	public void cleanChildMenus() {
		this.ackChildMenuList.clear();
		this.downtimeChildMenuList.clear();
		this.eventHandlersChildMenuList.clear();
		this.settingsChildMenuList.clear();
		this.notificationsChildMenuList.clear();
		this.chkResultsChildMenuList.clear();
		this.connectionChildMenuList.clear();
	}

	/**
	 * This method cleans up the child menu as well as parent menu lists.
	 */
	public void cleanup() {
		cleanParentMenus();
		cleanChildMenus();
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
	 * Call back method for JMS
	 */
	@Override
	public void refresh(String xmlTopic) {
		// List<JMSUpdate> jmsUpdates = new ArrayList<JMSUpdate>();
		// try {
		// if (xmlTopic != null) {
		// // Get the jms updates.
		// jmsUpdates = JMSUtils.getJMSUpdatesListFromXML(xmlTopic,
		// selectedNodeType);
		// }
		// if (jmsUpdates != null) {
		// for (JMSUpdate update : jmsUpdates) {
		// if (update != null) {
		// /*
		// * If the selectedNodeID matches with the enitiyID from
		// * jmsUpdates list,then only reload the data.
		// */
		// if (update.getId() == selectedNodeId) {
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER
		// .debug("Processing JMS Push in ActionBean for selectedNodeId = "
		// + selectedNodeId);
		// }
		// // Clean up the already created menus.
		// cleanup();
		// // Populate the parent and child menus.
		// initialize();
		// SessionRenderer
		// .render(Constant.ACTIONS_GROUP_RENDER_NAME);
		// }
		// }
		// }
		// }
		// } catch (Exception exc) {
		// LOGGER.error(exc.getMessage());
		// }
	}

	/**
	 * Sets the acknowledgeState.
	 * 
	 * @param acknowledgeState
	 *            the acknowledgeState to set
	 */
	public void setAcknowledgeState(boolean acknowledgeState) {
		this.acknowledgeState = acknowledgeState;
	}

	/**
	 * Returns the acknowledgeState.
	 * 
	 * @return the acknowledgeState
	 */
	public boolean isAcknowledgeState() {
		return acknowledgeState;
	}

	/**
	 * Reads custom URLS from status-viewer properties.
	 */
	private static void readConnectionURLSFromProperties() {
		CONNECTION_URLS_MAP.put("SSH", PropertyUtils.getProperty(
				ApplicationType.STATUS_VIEWER,
				"portal.statusviewer.action.url.ssh"));
		CONNECTION_URLS_MAP.put("HTTP", PropertyUtils.getProperty(
				ApplicationType.STATUS_VIEWER,
				"portal.statusviewer.action.url.http"));
		CONNECTION_URLS_MAP.put("HTTPS", PropertyUtils.getProperty(
				ApplicationType.STATUS_VIEWER,
				"portal.statusviewer.action.url.https"));
		CONNECTION_URLS_MAP.put("VNC", PropertyUtils.getProperty(
				ApplicationType.STATUS_VIEWER,
				"portal.statusviewer.action.url.vnc"));
		CONNECTION_URLS_MAP.put("RDP", PropertyUtils.getProperty(
				ApplicationType.STATUS_VIEWER,
				"portal.statusviewer.action.url.rdp"));
	}

	/**
	 * replace dollar parameter for Connection URL
	 * 
	 * @param connectionUrl
	 * @param entityName
	 * @return connectionUrl
	 */
	private String replaceDollarParams(String connectionUrl, String entityName) {
		if (connectionUrl != null
				&& !connectionUrl.trim().equals(Constant.EMPTY_STRING)) {
			if (connectionUrl.contains("$HOST")) {
				connectionUrl = connectionUrl.replace("$HOST", entityName);
			}
		}

		return connectionUrl;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}
}
