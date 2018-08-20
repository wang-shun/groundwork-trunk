/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ActionReturn;
import org.groundwork.foundation.ws.model.impl.Filter;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.icesoft.faces.component.menubar.MenuItem;
import com.icesoft.faces.component.ext.HtmlCommandLink;

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

	/**
	 * DEFAULT_TOPIC_NAME - to be published for Event portlet
	 */
	private static final String DEFAULT_TOPIC_NAME = "foundation_events";

	/**
	 * FAKE_UPDATE_MESSAGE. these is published on event topic, whenever user
	 * takes action on some event, so as to notify all other instances of event
	 * consoles/portlets about this action.
	 */
	private static final String FAKE_UPDATE_MESSAGE = "<EVENT>EntityType=LOG_MESSAGE;Action=UPDATE;EntityId=-1</EVENT>";

	/**
	 * Instance of ManagedBean - entitySubscriber.
	 */
	private JMSTopicConnection jmsConnection = (JMSTopicConnection) FacesUtils
			.getManagedBean("jmsTopicConnection");

	private Map<Integer, String> actionsMap = null;
	private static final String MENU_ID_PREFIX = "menu_";

	private Action[] systemActions = null;

	public ActionBean() {
		menuModel = new ArrayList<MenuItem>();
		actionsMenu = createMenuItem(
				ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_ACTIONS),
				ConsoleConstants.MENU_ID,
				"#{tabset.tabs[tabset.tabIndex].actionBean.dummyListener}",
				null, ConsoleConstants.MENU_ICON_OFF);
		menuModel.add(actionsMenu);
		ConsoleManager mgr = ConsoleHelper.getConsoleManager();
		systemActions = mgr
				.getActionsByApplicationType(ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM);

	}

	private void createMenuItems(Map<Integer, String> actionsMap) {

		Set<Integer> keySet = actionsMap.keySet();
		Iterator<Integer> iter = keySet.iterator();
		while (iter.hasNext()) {
			Integer actionId = (Integer) iter.next();
			String actionName = actionsMap.get(actionId);
			logger.debug(actionId.intValue() + "---" + actionName);
			actionsMenu.getChildren().add(
					createMenuItem(
							actionName,
							MENU_ID_PREFIX
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
			actionFired = actionFired.substring(menuIndex + 5,
					actionFired.length());
			logger.debug("Action fired=" + actionFired);
			int actionId = Integer.parseInt(actionFired);

			String actionValue = (String) ((MenuItem) e.getSource()).getValue();
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

				if (!singleAppType
						.equalsIgnoreCase(ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM)) {
					if ((singleAppType
							.equalsIgnoreCase(ConsoleConstants.APP_TYPE_NAGIOS)
							&& !actionValue
									.equalsIgnoreCase("Accept Log Message")
							&& !actionValue
									.equalsIgnoreCase("Open Log Message")
							&& !actionValue
									.equalsIgnoreCase("Close Log Message") && !actionValue
								.equalsIgnoreCase("Notify Log Message"))
							|| !singleAppType
									.equalsIgnoreCase(ConsoleConstants.APP_TYPE_NAGIOS)) {
						PopupBean popup = ConsoleHelper.getPopupBean();
						popup.setShowModalPanel(false);
						popup.setShowDraggablePanel(false);
						popup.setShowModalInputPanel(true);
						popup.setTitle("Input required");
						popup.setButtonValue("Submit");
						popup.setMessage(ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_USER_COMMENT));
						popup.setMessageIds(messageIds);
						popup.setActionId(actionId);
						popup.setAppType(singleAppType);
						popup.setInputText(null);
						return;
					} else {
						this.performAction(messageIds, actionId, singleAppType,
								"");
					}

				} else {
					this.performAction(messageIds, actionId, singleAppType, "");
				} // end if

			} // end if

		} catch (Exception exc) {
			logger.error(exc.getMessage());
		} // end try/catch

	}

	/**
	 * Updates the operationstatus
	 * @param e
	 */
	public void updateOperationStatus(ActionEvent e) {
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
			String actionValue = (String) ((HtmlCommandLink) e.getSource())
					.getValue();
			int actionId = getKeyByValue(actionsMap, actionValue);
			this.performAction(messageIds, actionId, singleAppType, "");
		}
	}

	/**
	 * Helper to get the key from the value in a hashmap
	 * @param map
	 * @param value
	 * @return
	 */
	private int getKeyByValue(Map<Integer, String> map, String value) {
		for (Entry<Integer, String> entry : actionsMap.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return -1;
	}

	/**
	 * Helper to execute from the popup comment
	 */
	public ActionReturn performAction(int[] messageIds, int actionId,
			String appType, String userComment) {
		ConsoleManager mgr = ConsoleHelper.getConsoleManager();
		ActionReturn actionReturn = mgr.performAction(messageIds, actionId,
				appType, userComment);
		logger.debug("LogMessageIds:" + messageIds + "---UserComment:"
				+ userComment);
		logger.debug("ActionCode:" + actionReturn.getReturnCode()
				+ "---ActionValue:" + actionReturn.getReturnValue());
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(tabset.getTabIndex());
		MessageSelectBean msgSelectBean = tab.getMsgSelector();
		// if return code is from script SUCCESS or http request
		// 200,
		// then consider success.
		if (ConsoleConstants.ACTION_RETURN_SUCCESS.equals(actionReturn
				.getReturnCode())
				|| ConsoleConstants.ACTION_RETURN__HTTP_OK
						.equalsIgnoreCase(actionReturn.getReturnCode())) {
			msgSelectBean.reset();
			this.reset();
			tab.getFreezeBean().freeze(false);
			// Remove the updated rows from the view
			mgr.initializePage(false);
			MenuItem menu = (MenuItem) menuModel.get(0);
			menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
		}
		return actionReturn;
		/*
		 * else { PopupBean popup = ConsoleHelper.getPopupBean();
		 * popup.setShowModalPanel(true); popup.setShowDraggablePanel(false);
		 * popup.setShowModalInputPanel(false); popup.setInputText(null);
		 * popup.setTitle("Error"); popup.setMessage("Error occurred: " +
		 * actionReturn.getReturnCode() + " : " +
		 * actionReturn.getReturnValue()); } // end if
		 */
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
				actions = systemActions;
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
			menuItem.setActionListener(createActionListenerMethodBinding(actionListener));
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
	 * Gets the system actions
	 * 
	 * @return
	 */
	public Action[] getSystemActions() {
		return systemActions;
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
		if (this.actionsMenu != null && this.actionsMenu.getChildren() != null) {
			actionsMenu.getChildren().clear();
			MenuItem menu = (MenuItem) menuModel.get(0);
			menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
		}
	}

	public void dummyListener(ActionEvent e) {

	}

	/**
	 * 
	 * This method publishes updates for Event portlet.
	 * 
	 * @param eventPushString
	 * @throws GWPortalGenericException
	 * 
	 * 
	 */

	private void publishEventListUpdates(String eventPushString)
			throws GWPortalGenericException {
		if (null == eventPushString || eventPushString.equals("")) {
			return;
		}
		Session session = null;
		try {
			session = this.jmsConnection.getConnection().createSession(true,
					Session.SESSION_TRANSACTED);
			// finds the topic and build a publisher:
			Topic topic = (Topic) this.jmsConnection.getJndi().lookup(
					DEFAULT_TOPIC_NAME);
			MessageProducer publisher = session.createProducer(topic);
			TextMessage message = session.createTextMessage();
			message.setText(eventPushString);
			publisher.send(message);
		} catch (Exception exc) {
			logger.error(exc.getMessage());
			throw new GWPortalGenericException(exc.getMessage());
		} finally {
			if (session != null) {
				try {
					session.commit();
					session.close();
					session = null;
				} catch (Exception exc) {
					logger.error(exc.getMessage());
				}
			}
		}
	}

}