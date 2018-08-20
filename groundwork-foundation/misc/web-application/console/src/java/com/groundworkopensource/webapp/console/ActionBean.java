package com.groundworkopensource.webapp.console;

/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ActionReturn;
import org.groundwork.foundation.ws.model.impl.Filter;

import com.icesoft.faces.component.menubar.MenuItem;

/**
 * <p>
 * The ActionBean class determines which menu item fired the ActionEvent and
 * stores the modified id information in a String. ActionBean also controls the
 * orientation of the Menu Bar.
 * </p>
 */
public class ActionBean {

	public static Logger logger = Logger.getLogger(ActionBean.class.getName());

	private List<MenuItem> menuModel;

	private MenuItem actionsMenu = null;

	private String singleAppType = null;

	private Map<Integer, String> actionsMap = null;
	private static final String MENU_ID_PREFIX = "menu_";

	public ActionBean() {
		menuModel = new ArrayList<MenuItem>();
		actionsMenu = createMenuItem(
				ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_ACTIONS),
				ConsoleConstants.MENU_ID,
				"#{tabset.tabs[tabset.tabIndex].actionBean.dummyListener}",
				null, ConsoleConstants.MENU_ICON_OFF);
		menuModel.add(actionsMenu);

	}

	private void createMenuItems(Map<Integer, String> actionsMap) {

		Set<Integer> keySet = actionsMap.keySet();
		Iterator<Integer> iter = keySet.iterator();
		while (iter.hasNext()) {
			Integer actionId = (Integer) iter.next();
			String actionName = actionsMap.get(actionId);
			logger.debug(actionId.intValue() + "---" + actionName);
			actionsMenu.getChildren().add(
					createMenuItem(actionName, MENU_ID_PREFIX
							+ String.valueOf(actionId.intValue()),
							ConsoleConstants.MENU_ITEM_ACTION_LISTENER_BIND,
							null, null));
		} // end if
		menuModel.clear();
		menuModel.add(actionsMenu);

	}

	/**
	 * Identify the ID of the element that fired the event and return it in a
	 * form suitable for display.
	 * 
	 * @param e
	 *            the event that fired the listener
	 */
	public void menuItemListener(ActionEvent e) {
		try {
			String actionFired = (String) ((UIComponent) e.getSource())
					.getClientId(FacesContext.getCurrentInstance());

			// chop off the meaningless numbers, etc. from the id tag
			int menuIndex = actionFired.indexOf(MENU_ID_PREFIX);
			actionFired = actionFired.substring(menuIndex + 5, actionFired
					.length());
			logger.debug("Action fired=" + actionFired);
			TabsetBean tabset = ConsoleHelper.getTabSetBean();
			Tab tab = tabset.getTabs().get(tabset.getTabIndex());
			MessageSelectBean msgSelectBean = tab.getMsgSelector();
			EventBean[] selectedEvents = msgSelectBean.getAllRows();
			if (selectedEvents != null && selectedEvents.length > 0) {
				int[] messageIds = new int[selectedEvents.length];
				for (int i = 0; i < selectedEvents.length; i++) {
					EventBean event = (EventBean) selectedEvents[i];
					messageIds[i] = event.getLogMessageID();
					logger.debug("MessageId=" + messageIds[i]);
				}
				ConsoleManager mgr = ConsoleHelper.getConsoleManager();
				ActionReturn actionReturn = mgr.performAction(messageIds,
						Integer.parseInt(actionFired), singleAppType);
				logger.debug("ActionCode:" + actionReturn.getReturnCode()
						+ "---ActionValue:" + actionReturn.getReturnValue());
				// if return code is from script SUCCESS or http request 200,
				// then consider success.
				if (ConsoleConstants.ACTION_RETURN_SUCCESS.equals(actionReturn
						.getReturnCode())
						|| ConsoleConstants.ACTION_RETURN__HTTP_OK
								.equalsIgnoreCase(actionReturn.getReturnCode())) {
					logger.debug("ActionCode:" + actionReturn.getReturnCode());
					msgSelectBean.reset();
					this.reset();
					tab.getFreezeBean().freeze(false);
					// Remove the updated rows from the view

					Filter filter = ConsoleHelper.getFilterBean().getFilter();
					if (filter != null && filter.getPropertyName() != null
							&& filter.getValue() != null) {
						String propertyName = filter.getPropertyName();
						String propertyValue = filter.getValue().toString();
						if (!CommonUtils.isEmpty(propertyName)
								&& !CommonUtils.isEmpty(propertyValue)) {
							logger.debug("Property name=" + propertyName);
							if (propertyName
									.equalsIgnoreCase(ConsoleConstants.PROP_NAME_OPERATION_STATUS)) {
								logger.debug("Refreshing-" + propertyValue);
								mgr.populateEventsByOperationStatus(
										propertyValue, null);
							} else if (propertyName
									.equalsIgnoreCase(ConsoleConstants.PROP_NAME_APP_TYPE)) {
								mgr.populateEventsByApplicationType(
										propertyValue, null);
							} else if (propertyName
									.equalsIgnoreCase(ConsoleConstants.PROP_NAME_DEVICE)) {
								mgr.populateEventsByHostGroup(propertyValue,
										null);
							} else if (propertyName
									.equalsIgnoreCase(ConsoleConstants.PROP_NAME_MONITOR_STATUS)) {
								mgr.populateEventsBySeverity(propertyValue,
										null);
							}
						} else {
							mgr.populateEventsByOperationStatus(
									ConsoleConstants.OPERATION_STATUS_OPEN,
									null);
						} // end if
					}
					else if (filter != null
							&& filter.getLeftFilter() != null
							&& filter.getLeftFilter().getLeftFilter() != null
							&& filter.getLeftFilter().getLeftFilter()
									.getLeftFilter().getIntegerProperty() != null
							&& filter.getLeftFilter().getLeftFilter()
									.getIntegerProperty().getName() != null
							&& filter
									.getLeftFilter()
									.getLeftFilter()
									.getIntegerProperty()
									.getName()
									.equalsIgnoreCase(
											ConsoleConstants.FILTER_SERVICE_STATUS_ID)) {
						StringTokenizer stkn = new StringTokenizer(tab
								.getHiddenLabel(), "=");
						String labelPrefix = stkn.nextToken();
						String labelSuffix = stkn.nextToken();
						mgr.populateEventsByServiceGroup(labelSuffix, null);
						logger.info("After perform action from service groups");
					} else {
						mgr.populateEventsByOperationStatus(
								ConsoleConstants.OPERATION_STATUS_OPEN, null);
						logger
								.info("After perform action from default All Open Events");
					}

				} // end if
				MenuItem menu = (MenuItem) menuModel.get(0);
				menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
			} // end if
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		} // end try/catch

	}

	/**
	 * Identify the ID of the element that fired the event and return it in a
	 * form suitable for display.
	 * 
	 * @param e
	 *            the event that fired the listener
	 */
	public void menuListener() {
		logger.debug("MenuListener method");
		MenuItem menu = (MenuItem) menuModel.get(0);

		menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
		actionsMenu.getChildren().clear();
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(tabset.getTabIndex());
		MessageSelectBean msgSelectBean = tab.getMsgSelector();
		EventBean[] selectedEvents = msgSelectBean.getAllRows();
		if (selectedEvents != null && selectedEvents.length > 0) {
			logger.debug("There are few messages selected");

			menu.setIcon(ConsoleConstants.MENU_ICON_ON);
			ConsoleManager mgr = ConsoleHelper.getConsoleManager();
			Action[] actions = null;
			if (!this.isSingleAppType(selectedEvents)) {
				actions = mgr
						.getActionsByApplicationType(ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM);
				logger.debug("Mixed appType events selected");
			} else {
				actions = mgr.getActionsByApplicationType(singleAppType);
				logger.debug("Single appType events selected");
			}// end if
			// String[] menuItems = new String[actions.length];
			actionsMap = new HashMap<Integer, String>();
			for (int i = 0; i < actions.length; i++) {
				actionsMap.put(new Integer(actions[i].getActionID()),
						actions[i].getName());

			}
			this.createMenuItems(actionsMap);

		} // end if

	}

	/**
	 * Gets the menu model.
	 * 
	 * @return
	 */
	public List<MenuItem> getMenuModel() {
		return menuModel;
	}

	/**
	 * Sets the menu model
	 * 
	 * @param menuModel
	 */
	public void setMenuModel(List<MenuItem> menuModel) {
		this.menuModel = menuModel;
	}

	/**
	 * Creates a menu item.
	 * 
	 * @param label
	 * @param id
	 * @param actionListener
	 * @param action
	 * @param icon
	 * @return
	 */
	private MenuItem createMenuItem(String label, String id,
			String actionListener, String action, String icon) {
		MenuItem menuItem = new MenuItem();
		menuItem.setValue(label);
		if (id != null)
			menuItem.setId(id);
		if (actionListener != null) {
			menuItem
					.setActionListener(createActionListenerMethodBinding(actionListener));
		}
		if (action != null) {
			menuItem.setActionExpression(createActionMethodExpression(action));
		}
		if (icon != null)
			menuItem.setIcon(icon);
		// menuItem.setStyleClass("action-menu");
		return menuItem;
	}

	/**
	 * Creates an action listener method for the menu
	 * 
	 * @param actionListenerString
	 * @return
	 */
	private MethodBinding createActionListenerMethodBinding(
			String actionListenerString) {
		Class args[] = { ActionEvent.class };
		MethodBinding methodBinding = null;

		methodBinding = FacesContext.getCurrentInstance().getApplication()
				.createMethodBinding(actionListenerString, args);
		return methodBinding;
	}

	/**
	 * Creates an action method for menu
	 * 
	 * @param action
	 * @return
	 */
	private MethodExpression createActionMethodExpression(String action) {
		Class args[] = {};
		ExpressionFactory ef = FacesContext.getCurrentInstance()
				.getApplication().getExpressionFactory();
		MethodExpression methodExp = null;

		methodExp = ef.createMethodExpression(FacesContext.getCurrentInstance()
				.getELContext(), action, Void.TYPE, args);

		return methodExp;
	}

	/**
	 * Gets the actions menu
	 * 
	 * @return
	 */
	public MenuItem getActionsMenu() {
		return actionsMenu;
	}

	/**
	 * Sets the actions Menu
	 * 
	 * @param actionsMenu
	 */
	public void setActionsMenu(MenuItem actionsMenu) {
		this.actionsMenu = actionsMenu;
	}

	/**
	 * Checks if the selected events are of singel appType or mixed
	 * 
	 * @param selectedEvents
	 * @return
	 */
	private boolean isSingleAppType(EventBean[] selectedEvents) {
		boolean result = true;
		Map<String, String> appTypeMap = new HashMap<String, String>();
		for (int i = 0; i < selectedEvents.length; i++) {
			EventBean event = (EventBean) selectedEvents[i];
			singleAppType = event.getApplicationType();
			appTypeMap.put(singleAppType, singleAppType);
		}
		if (appTypeMap.size() > 1) {
			result = false;
		}
		return result;
	}

	/**
	 * Resets the menu
	 */
	public void reset() {
		if (this.actionsMenu != null && actionsMenu.getChildren() != null) {
			actionsMenu.getChildren().clear();
			MenuItem menu = (MenuItem) menuModel.get(0);
			menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
		}
	}

	public void dummyListener(ActionEvent e) {

	}

}