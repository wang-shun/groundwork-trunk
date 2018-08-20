package com.groundworkopensource.portal.statusviewer.bean;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

import com.groundworkopensource.webapp.console.*;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ActionReturn;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.listener.JMSTopicConnection;
import com.groundworkopensource.portal.statusviewer.handler.EventActionHandler;
import com.groundworkopensource.portal.statusviewer.handler.EventHandler;
import com.groundworkopensource.webapp.console.EventQueryManager;
import com.icesoft.faces.component.menubar.MenuItem;

/**
 * 
 * The ActionBean class determines which menu item fired the ActionEvent and
 * stores the modified id information in a String. ActionBean also controls the
 * orientation of the Menu Bar.
 */
@SuppressWarnings("deprecation")
public class EventMenuActionBean implements Serializable {


    /**
     * Instance of ManagedBean - entitySubscriber.
     */
    private JMSTopicConnection jmsConnection = (JMSTopicConnection) FacesUtils
            .getManagedBean("jmsTopicConnection");

    /**
     * FAKE_UPDATE_MESSAGE. these is published on event topic, whenever user
     * takes action on some event, so as to notify all other instances of event
     * consoles/portlets about this action.
     */
    private static final String FAKE_UPDATE_MESSAGE = "<EVENT>EntityType=LOG_MESSAGE;Action=UPDATE;EntityId=-1</EVENT>";

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3863855245136942782L;

    /**
     * logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(EventMenuActionBean.class.getName());

    /**
     * List of menu item
     */
    private List<MenuItem> menuModel;
    /**
     * menu item instance variable
     */
    private MenuItem actionsMenu = null;
    /**
     * Application type
     */
    private String singleAppType = null;
    /**
     * action map hold action id and action name for menu bar
     */
    private final Map<Integer, String> actionsMap = new HashMap<Integer, String>();
    /**
     * Prefix string
     */
    private static final String MENU_ID_PREFIX = "menu_";

    /**
     * Constructor
     * 
     * Initialize the menu and dummy menu listener
     */
    public EventMenuActionBean() {
        menuModel = new ArrayList<MenuItem>();
        actionsMenu = createMenuItem(ResourceUtils
                .getLocalizedMessage(Constant.ACTIONS_BUTTON_LABEL),
                Constant.MENU_BAR_ID,
                Constant.EVENT_MENU_ACTION_BEAN_DUMMY_LISTENER, null, null);
        menuModel.add(actionsMenu);

    }

    /**
     * this method Construct menu item list.
     * 
     * @param actionsMap
     */
    private void createMenuItems(Map<Integer, String> actionsMap) {
        Set<Entry<Integer, String>> entrySet = actionsMap.entrySet();
        Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Entry<Integer, String> entry = iterator.next();
            Integer actionId = entry.getKey();
            String actionName = actionsMap.get(actionId);
            LOGGER.debug(actionId.intValue() + "---" + actionName);
            actionsMenu.getChildren().add(
                    createMenuItem(actionName, MENU_ID_PREFIX
                            + String.valueOf(actionId.intValue()),
                            Constant.EVENT_MENU_ACTION_BEAN_MENU_ITEM_LISTENER,
                            null, null));

        } // end if
        menuModel.clear();
        menuModel.add(actionsMenu);

    }

    /**
     * Identify the ID of the element that fired the event and return it in a
     * form suitable for display.
     * 
     * @param actionEvent
     *            the event that fired the listener
     */
    public void menuItemListener(ActionEvent actionEvent) {

        try {
            String actionFired = ((UIComponent) actionEvent.getSource())
                    .getClientId(FacesContext.getCurrentInstance());

            // chop off the meaningless numbers, etc. from the id tag
            int menuIndex = actionFired.indexOf(MENU_ID_PREFIX);
            actionFired = actionFired.substring(menuIndex + Constant.FIVE,
                    actionFired.length());
            int actionId = Integer.parseInt(actionFired);
            String actionValue = (String) ((MenuItem) actionEvent.getSource()).getValue();
            actionPerformOnRow(actionId,actionValue);
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage());
        } // end try/catch

    }

    /**
     * Helper to show popup
     * @param messageIds
     * @param actionId
     */
    private void showPopup(int[] messageIds, int actionId) {
        com.groundworkopensource.webapp.console.PopupBean popup = ConsoleHelper.getPopupBean();
        popup.setShowModalPanel(false);
        popup.setShowDraggablePanel(false);
        popup.setShowModalInputPanel(true);
        popup.setTitle("Input required");
        popup.setButtonValue("Submit");
        popup.setMessage(com.groundworkopensource.webapp.console.ResourceUtils
                .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_USER_COMMENT));
        popup.setMessageIds(messageIds);
        popup.setActionId(actionId);
        popup.setAppType(singleAppType);
        popup.setInputText(null);
        popup.setConsoleRequester(false);
        return;
    }

    /**
     * @param actionFired
     */
    private void actionPerformOnRow(int actionFired, String actionValue) {
        EventMessageSelectBean msgSelectBean = (EventMessageSelectBean) FacesUtils
                .getManagedBean(Constant.EVENT_MESSAGE_SELECT_BEAN);
        EventBean[] selectedEvents = msgSelectBean.getAllRows();
        if (selectedEvents != null && selectedEvents.length > 0) {
            int[] messageIds = new int[selectedEvents.length];
            for (int i = 0; i < selectedEvents.length; i++) {
                EventBean event = selectedEvents[i];
                messageIds[i] = event.getLogMessageID();

            }
            this.showPopup(messageIds,actionFired);
            return;


            /*EventActionHandler eventActionHandler = (EventActionHandler) FacesUtils
                    .getManagedBean(Constant.EVENT_ACTION_HANDLER_BEAN);
            ActionReturn actionReturn = eventActionHandler.performAction(
                    messageIds, actionFired, singleAppType);

            // if return code is from script SUCCESS or http request 200,
            // then consider success.
            if (actionReturn != null) {
                if (Constant.ACTION_RETURN_SUCCESS.equals(actionReturn
                        .getReturnCode())
                        || Constant.ACTION_RETURN_HTTP_OK
                                .equalsIgnoreCase(actionReturn.getReturnCode())) {

                    msgSelectBean.reset();
                    this.reset();
                    EventFreezeBean eventFreezeBean = (EventFreezeBean) FacesUtils
                            .getManagedBean(Constant.EVENT_FREEZE_BEAN);
                    if (eventFreezeBean != null) {
                        eventFreezeBean.freeze(false);
                    }
                    EventHandler eventHandler = new EventHandler(null);
                    eventHandler.populateEvents();

                    // publish these events on topic
                    try {
                        // Publish updates for Event console / portlet
                        //LOGGER.debug("PUBLISHING: new UPDATE message on topic"
                        // );
                        publishEventListUpdates(FAKE_UPDATE_MESSAGE);
                    } catch (Exception e) {
                        LOGGER
                                .error("Error occured while publishing fake update message.");
                    }

                    // ---

                }
            } // end if
            MenuItem menu = menuModel.get(0);
            menu.setIcon(Constant.EMPTY_STRING);*/
        } // end if
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
        if (null == eventPushString
                || eventPushString.equals(Constant.EMPTY_STRING)) {
            return;
        }
        Session session = null;
        try {
            session = this.jmsConnection.getConnection().createSession(true,
                    Session.SESSION_TRANSACTED);
            // finds the topic and build a publisher:
            Topic topic = (Topic) this.jmsConnection.getJndi().lookup(
                    ConsoleConstants.TOPIC_FOUNDATION_EVENTS);
            MessageProducer publisher = session.createProducer(topic);
            TextMessage message = session.createTextMessage();
            message.setText(eventPushString);
            publisher.send(message);
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage());
            throw new GWPortalGenericException(exc.getMessage());
        } finally {
            if (session != null) {
                try {
                    session.commit();
                    session.close();
                    session = null;
                } catch (Exception exc) {
                    LOGGER.error(exc.getMessage());
                }
            }
        }
    }

    /**
     * Listener for pop up menu
     * 
     * @param actionEvent
     */
    public void menuPopUpListener(ActionEvent actionEvent) {
        // getting request parameter to identify which operation to be perform
        FacesContext context = FacesContext.getCurrentInstance();
        String param = context.getExternalContext().getRequestParameterMap()
                .get(Constant.MENU_POP_UP_PARAM);
        Integer actionId = null;
        Set<Integer> keySet = actionsMap.keySet();
        Iterator<Integer> iter = keySet.iterator();
        while (iter.hasNext()) {
            actionId = iter.next();
            String actionName = actionsMap.get(actionId);
            if (actionName.contains(param)) {
                break;
            }

        }
        if (actionId != null) {
            String actionValue = (String) ((MenuItem) actionEvent.getSource()).getValue();
            actionPerformOnRow(actionId.intValue(),actionValue);
        } else {
            LOGGER.error("Action id is null");
        }
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        EventDataTableBean dataTableBean = eventListBean.getDataTableBean();
        dataTableBean.setPopUpmenuClicked(false);

    }

    /**
     * Identify the ID of the element that fired the event and return it in a
     * form suitable for display.
     * 
     * the event that fired the listener
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public void menuListener() throws WSDataUnavailableException,
            GWPortalException {

        MenuItem menu = menuModel.get(0);

        menu.setIcon(Constant.EMPTY_STRING);
        actionsMenu.getChildren().clear();
        EventMessageSelectBean eventMessageSelectBean = (EventMessageSelectBean) FacesUtils
                .getManagedBean(Constant.EVENT_MESSAGE_SELECT_BEAN);
        /*
         * EventMessageSelectBean msgSelectBean = eventListBean
         * .getEventMessageSelectBean();
         */
        EventBean[] selectedEvents = eventMessageSelectBean.getAllRows();
        if (selectedEvents != null && selectedEvents.length > 0) {
            menu.setIcon(Constant.EMPTY_STRING);
            EventQueryManager eventQueryManager = new EventQueryManager();
            Action[] actions = null;
            if (!this.isSingleAppType(selectedEvents)) {
                actions = eventQueryManager
                        .getActionsByApplicationType(Constant.SYSTEM);
                LOGGER.debug("Mixed appType events selected");
            } else {
                actions = eventQueryManager
                        .getActionsByApplicationType(singleAppType);
                LOGGER.debug("Single appType events selected");
            } // end if
            actionsMap.clear();
            for (int i = 0; i < actions.length; i++) {
                actionsMap.put(Integer.valueOf(actions[i].getActionID()),
                        actions[i].getName());

            }
            this.createMenuItems(actionsMap);

        } // end if

    }

    /**
     * Gets the menu model.
     * 
     * @return list
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
        if (id != null) {
            menuItem.setId(id);
        }
        if (actionListener != null) {
            menuItem
                    .setActionListener(createActionListenerMethodBinding(actionListener));
        }
        if (action != null) {
            menuItem.setActionExpression(createActionMethodExpression(action));
        }
        if (icon != null) {
            menuItem.setIcon(icon);
        }
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
        Class[] args = { ActionEvent.class };
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
        Class[] args = {};
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
     * @return MenuItem
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
     * @return boolean
     */
    public boolean isSingleAppType(EventBean[] selectedEvents) {
        boolean result = true;
        Map<String, String> appTypeMap = new HashMap<String, String>();
        for (int i = 0; i < selectedEvents.length; i++) {
            EventBean event = selectedEvents[i];
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
            MenuItem menu = menuModel.get(0);
            menu.setIcon(Constant.EMPTY_STRING);

        }
        // disable pop up menu
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        if (eventListBean != null) {
            EventDataTableBean dataTableBean = eventListBean.getDataTableBean();
            dataTableBean.setEnablePopUpMenu(false);
            dataTableBean.constructComponent();

        }
    }

    /**
     * @param e
     */
    public void dummyListener(ActionEvent e) {

        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        EventDataTableBean dataTableBean = eventListBean.getDataTableBean();
        dataTableBean.setRowSelected(true);

    }

    /**
     * Listener to toggle the select all button if rows selected and then
     * paginated. if any row is selected then reset the selected row.
     * 
     * @param e
     */
    public void paginatorClicked(ActionEvent e) {

        EventMessageSelectBean msgSelectBean = (EventMessageSelectBean) FacesUtils
                .getManagedBean(Constant.EVENT_MESSAGE_SELECT_BEAN);

        reset();
        if (msgSelectBean.getAllRows() != null
                && msgSelectBean.getAllRows().length > 0) {
            msgSelectBean.reset();
        } // end if
    }

    /**
     * Returns the singleAppType.
     * 
     * @return the singleAppType
     */
    public String getSingleAppType() {
        return singleAppType;
    }

    /**
     * Sets the singleAppType.
     * 
     * @param singleAppType
     *            the singleAppType to set
     */
    public void setSingleAppType(String singleAppType) {
        this.singleAppType = singleAppType;
    }

}