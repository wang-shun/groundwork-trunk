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

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.handler.EventHandler;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.icesoft.faces.async.render.SessionRenderer;

/**
 * Class denoting the backing bean for the Event portlet
 * 
 * @author manish_kjain
 * 
 */
public class EventListBean extends EventServerPush implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7891255971240207149L;
    /**
     * event data table instance variable
     */
    private EventDataTableBean dataTableBean;

    /**
     * eventMenuActionBean instance variable
     */
    private EventMenuActionBean eventMenuActionBean;
    /**
     * 
     */
    private EventMessageSelectBean eventMessageSelectBean;

    /**
     * Event handler instance variable
     */
    private EventHandler eventHandler = null;

    /**
     * eventFilterBean
     */
    private EventFilterBean eventFilterBean = null;

    /**
     * ReferenceTreeMetaModel instance
     * <p>
     * !!!!!!!!!!! IMP !!!!!!!!!! : Please do not remove below declaration of
     * referenceTreeModel.
     */
    @SuppressWarnings("unused")
    private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            .getManagedBean(Constant.REFERENCE_TREE);

    /**
     * logger
     */
    private static final Logger LOGGER = Logger.getLogger(EventListBean.class
            .getName());

    /**
     * Returns the eventMessageSelectBean.
     * 
     * @return the eventMessageSelectBean
     */
    public EventMessageSelectBean getEventMessageSelectBean() {
        return eventMessageSelectBean;
    }

    /**
     * Sets the eventMessageSelectBean.
     * 
     * @param eventMessageSelectBean
     *            the eventMessageSelectBean to set
     */
    public void setEventMessageSelectBean(
            EventMessageSelectBean eventMessageSelectBean) {
        this.eventMessageSelectBean = eventMessageSelectBean;
    }

    /**
     * constructor
     */
    public EventListBean() {

        dataTableBean = new EventDataTableBean();
    }

    /**
     * Returns the dataTableBean.
     * 
     * @return the dataTableBean
     */
    public EventDataTableBean getDataTableBean() {

        return dataTableBean;
    }

    /**
     * Sets the dataTableBean.
     * 
     * @param dataTableBean
     *            the dataTableBean to set
     */
    public void setDataTableBean(EventDataTableBean dataTableBean) {
        this.dataTableBean = dataTableBean;
    }

    /**
     * Returns the eventMenuActionBean.
     * 
     * @return the eventMenuActionBean
     */
    public EventMenuActionBean getEventMenuActionBean() {
        return eventMenuActionBean;
    }

    /**
     * Sets the eventMenuActionBean.
     * 
     * @param eventMenuActionBean
     *            the eventMenuActionBean to set
     */
    public void setEventMenuActionBean(EventMenuActionBean eventMenuActionBean) {
        this.eventMenuActionBean = eventMenuActionBean;
    }

    /**
     * Separate method to get Current Data table bean object.
     * 
     * @return EventDataTableBean
     */
    public EventDataTableBean getCurrentDataTableBean() {

        if (eventHandler == null) {
            if (dataTableBean.getEventHandler() == null) {
                if (FacesContext.getCurrentInstance() == null) {
                    eventHandler = new EventHandler(this.eventFilterBean);
                } else {
                    eventHandler = new EventHandler(null);
                }
                dataTableBean.setEventHandler(eventHandler);
            } else {
                eventHandler = dataTableBean.getEventHandler();
            }
        }
        StateController stateController = eventHandler.getStateController();
        // stateController.update(eventHandler.getSelectedNodeType(),
        // eventHandler
        // .getSelectedNodeName(), eventHandler.getSelectedNodeId());

        // new StateController();
        // get current selected host filter
        String currentHostFilter = stateController.getCurrentHostFilter();
        // get current selected service filter
        String currentServiceFilter = stateController.getCurrentServiceFilter();
        EventFilterBean eventFilterBeanLocal = (EventFilterBean) FacesUtils
                .getManagedBean(Constant.EVENT_FILTER_BEAN);
        // check if previous selected filter and current selected are same then
        // does not render data table
        if (eventFilterBeanLocal != null) {
            if (!eventFilterBeanLocal.getPreviousSelectedHostFilterName()
                    .equalsIgnoreCase(currentHostFilter)
                    || !eventFilterBeanLocal
                            .getPreviousSelectedServiceFilterName()
                            .equalsIgnoreCase(currentServiceFilter)) {

                // eventHandler.createAndPopulateDataTable();
                // populate data table depending on current selected filter
                eventHandler.refreshDataTable(null);
            }
        }
        // clean up the data before returning
        currentServiceFilter = null;
        currentHostFilter = null;
        stateController = null;
        eventFilterBeanLocal = null;

        return dataTableBean;
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {
        // re-initialize the bean so as to reload UI
        if (dataTableBean != null) {
            dataTableBean.setMessage(false);
            dataTableBean.setError(false);

        }
        if (eventHandler == null) {
            eventHandler = new EventHandler(this.eventFilterBean);
        }
        if (FacesContext.getCurrentInstance() != null) {
            eventHandler.refreshDataTable(null);
        } else {
            eventHandler.refreshDataTable(this);
        }
    }

    /**
     * Call back method for JMS
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlTopic) {
        try {

            if (xmlTopic != null) {
                // populatedatatable
                this.reloadPage(null);
                SessionRenderer.render(groupRenderName);
            } // end if

        } catch (Exception exc) {
            LOGGER.debug("Exception in EventListBean : "+exc.getMessage());
        }
    }

    /**
     * Stops the topic connection
     */
    public void stopTopicConnection() {

        try {
            setListenToTopic(null);
            LOGGER.debug("Topic connection stopped...");

        } catch (Exception exc) {
            LOGGER.error("Exception in stopTopicConnection() method " + exc);
        }
        // end try/catch block

    }

    /**
     * Starts the topic connection.
     */
    public void startTopicConnection() {

        try {
            setListenToTopic("event.topic.name");
            LOGGER.debug("Topic connection started...");
        } catch (Exception exc) {
            LOGGER.error("Exception while Starting JMS push " + exc);
        } // end

    }

    /**
     * @return eventFilterBean
     */
    public EventFilterBean getEventFilterBean() {
        return eventFilterBean;
    }

    /**
     * @param eventFilterBean
     */
    public void setEventFilterBean(EventFilterBean eventFilterBean) {
        this.eventFilterBean = eventFilterBean;
    }

}
