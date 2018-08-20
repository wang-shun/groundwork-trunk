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
package com.groundworkopensource.portal.statusviewer.bean.nagios;

import static com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants.IS_ACTIVECHECKS_ENABLED_PROPERTY;
import static com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants.IS_EVENT_HANDLERS_ENABLED_PROPERTY;
import static com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants.IS_FLAP_DETECTION_ENABLED_PROPERTY;
import static com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants.IS_NOTIFICATIONS_ENABLED_PROPERTY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.ModelPopUpDataBean;
import com.groundworkopensource.portal.statusviewer.bean.NagiosStatisticsModelPopUpListBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.handler.NagiosStatisticsHandler;
import com.groundworkopensource.portal.statusviewer.handler.SubpageIntegrator;
import com.icesoft.faces.component.datapaginator.DataPaginator;

/**
 * This is a backing bean for Nagios monitoring statistics portlet.
 * 
 * @author shivangi_walvekar
 */
public class NagiosStatisticsBean extends ServerPush implements Serializable {

    /** serialVersionUID. */
    private static final long serialVersionUID = 1571575144387818657L;

    /** Logger. */
    private static final Logger LOGGER = Logger
            .getLogger(NagiosStatisticsBean.class.getName());

    /** selectedNodeType. */
    private NodeType selectedNodeType;

    /** selectedNodeName. */
    private String selectedNodeName = Constant.EMPTY_STRING;

    /** SelectedNodeId. */
    private int selectedNodeId = 0;

    /** Flag to identify if in Status Viewer or in Dashboard. */
    private boolean inStatusViewer;

    /** Boolean to indicate if error has occurred. */
    private boolean error = false;

    /** boolean flag to indicate if the current context is service group or not. */
    private boolean serviceGroupContext;

    /**
     * Instance of NagiosStatisticsHandler
     */
    private NagiosStatisticsHandler nagiosStatisticsHandler;
    /**
     * NagiosStatisticsModelPopUpListBean
     */
    private NagiosStatisticsModelPopUpListBean nagiosStatisticsModelPopUpList;

    /**
     * rows of model pop data table
     */
    private int popupRowSize;

    /**
     * Checks if is service group context.
     * 
     * @return serviceGroupContext
     */
    public boolean isServiceGroupContext() {
        return serviceGroupContext;
    }

    /**
     * Sets the service group context.
     * 
     * @param serviceGroupContext
     *            the service group context
     */
    public void setServiceGroupContext(boolean serviceGroupContext) {
        this.serviceGroupContext = serviceGroupContext;
    }

    /**
     * Checks if is error.
     * 
     * @return error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets the error.
     * 
     * @param error
     *            the error
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /** Error message to be shown on UI,in case of errors/exceptions. */
    private String errorMessage;

    /**
     * Gets the error message.
     * 
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     * 
     * @param errorMessage
     *            the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** This property holds the nagios statistics data for Active Checks. */
    private NagiosStatisticsProperty activeChecksStatistics = new NagiosStatisticsProperty();

    /** This property holds the nagios statistics data for Passive Checks. */
    private NagiosStatisticsProperty passiveChecksStatistics = new NagiosStatisticsProperty();

    /** This property holds the nagios statistics data for Notifications. */
    private NagiosStatisticsProperty notificationsStatistics = new NagiosStatisticsProperty();

    /** This property holds the nagios statistics data for Flap Detection. */
    private NagiosStatisticsProperty flapDetectionStatistics = new NagiosStatisticsProperty();

    /** This property holds the nagios statistics data for Event Handlers. */
    private NagiosStatisticsProperty eventHandlersStatistics = new NagiosStatisticsProperty();

    /**
     * This list holds the data to be displayed on the modal pop-up for the
     * disabled hosts/services.
     */
    private List<ModelPopUpDataBean> disabledEntityList = Collections
            .synchronizedList(new ArrayList<ModelPopUpDataBean>());

    /** This property hold the total count of disabled hosts/services. */
    private int countofDisabledEntities;

    /** Constant for string 'Unable to fetch nagios statistics. */
    public static final String UNABLE_TO_FETCH_NAGIOS_STATS = "Unable to fetch nagios statistics.";

    /** Constant for string 'Length of nagiosStatisticPropertyArray'. */
    public static final String LEN_NAGIOS_STATS = "Length of nagiosStatisticPropertyArray ";

    /** preferences Keys Map to be used for reading preferences. */
    private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
    static {
        /*
         * NOTE: Nagios Statistics Portlets are Network View portlets. So
         * default preferences must NOT have been set/specified in portlet.xml
         * in order to work properly in status-viewer => Entire Network view.
         * (Refer Roger's mail)
         */
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_GROUP_PREF,
                NodeType.SERVICE_GROUP);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_GROUP_PREF,
                NodeType.HOST_GROUP);
    }

    /**
     * Gets the countof disabled entities.
     * 
     * @return countofDisabledEntities
     */
    public int getCountofDisabledEntities() {
        if (getDisabledEntityList() != null) {
            countofDisabledEntities = getDisabledEntityList().size();
        }
        return countofDisabledEntities;
    }

    /**
     * Sets the countof disabled entities.
     * 
     * @param countofDisabledEntities
     *            the countof disabled entities
     */
    public void setCountofDisabledEntities(int countofDisabledEntities) {
        this.countofDisabledEntities = countofDisabledEntities;
    }

    /**
     * String property for the title of the pop-up panel for disabled
     * Hosts/Services.
     */
    private String panelPopupTitle;

    /**
     * String property for column header in the pop-up panel for disabled
     * Hosts/Services.
     */
    private String headerTitleName;

    /** String property for the column to sort. */
    private String sortColumn;

    /** boolean property indicating the ascending sort order. */
    private boolean ascending;

    /**
     * Gets the sort column.
     * 
     * @return sortColumn
     */
    public String getSortColumn() {
        return sortColumn;
    }

    /**
     * Sets the sort column.
     * 
     * @param sortColumn
     *            the sort column
     */
    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    /**
     * Checks if is ascending.
     * 
     * @return ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the ascending.
     * 
     * @param ascending
     *            the ascending
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Gets the header title name.
     * 
     * @return headerTitle
     */
    public String getHeaderTitleName() {
        return headerTitleName;
    }

    /**
     * Sets the header title name.
     * 
     * @param headerTitleName
     *            the header title name
     */
    public void setHeaderTitleName(String headerTitleName) {
        this.headerTitleName = headerTitleName;
    }

    /** Instance of WebServiceFactory. */
    private final WebServiceFactory webServiceFactory = new WebServiceFactory();

    /** Instance of IWSFacade. */
    private final IWSFacade foundationWSFacade = webServiceFactory
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * Gets the disabled entity list.
     * 
     * @return disabledEntityList
     */
    public List<ModelPopUpDataBean> getDisabledEntityList() {
        return disabledEntityList;
    }

    /**
     * Sets the disabled entity list.
     * 
     * @param disabledEntityList
     *            the disabled entity list
     */
    public void setDisabledEntityList(
            List<ModelPopUpDataBean> disabledEntityList) {
        this.disabledEntityList = disabledEntityList;
    }

    /**
     * Gets the panel popup title.
     * 
     * @return panelPopupTitle
     */
    public String getPanelPopupTitle() {
        return panelPopupTitle;
    }

    /**
     * Sets the panel popup title.
     * 
     * @param panelPopupTitle
     *            the panel popup title
     */
    public void setPanelPopupTitle(String panelPopupTitle) {
        this.panelPopupTitle = panelPopupTitle;
    }

    /**
     * Gets the active checks statistics.
     * 
     * @return activeChecksStatistics
     */
    public NagiosStatisticsProperty getActiveChecksStatistics() {
        return activeChecksStatistics;
    }

    /**
     * Sets the active checks statistics.
     * 
     * @param activeChecksStatistics
     *            the active checks statistics
     */
    public void setActiveChecksStatistics(
            NagiosStatisticsProperty activeChecksStatistics) {
        this.activeChecksStatistics = activeChecksStatistics;
    }

    /**
     * Gets the passive checks statistics.
     * 
     * @return passiveChecksStatistics
     */
    public NagiosStatisticsProperty getPassiveChecksStatistics() {
        return passiveChecksStatistics;
    }

    /**
     * Sets the passive checks statistics.
     * 
     * @param passiveChecksStatistics
     *            the passive checks statistics
     */
    public void setPassiveChecksStatistics(
            NagiosStatisticsProperty passiveChecksStatistics) {
        this.passiveChecksStatistics = passiveChecksStatistics;
    }

    /**
     * Gets the notifications statistics.
     * 
     * @return notificationsStatistics
     */
    public NagiosStatisticsProperty getNotificationsStatistics() {
        return notificationsStatistics;
    }

    /**
     * Sets the notifications statistics.
     * 
     * @param notificationsStatistics
     *            the notifications statistics
     */
    public void setNotificationsStatistics(
            NagiosStatisticsProperty notificationsStatistics) {
        this.notificationsStatistics = notificationsStatistics;
    }

    /**
     * Gets the flap detection statistics.
     * 
     * @return flapDetectionStatistics
     */
    public NagiosStatisticsProperty getFlapDetectionStatistics() {
        return flapDetectionStatistics;
    }

    /**
     * Sets the flap detection statistics.
     * 
     * @param flapDetectionStatistics
     *            the flap detection statistics
     */
    public void setFlapDetectionStatistics(
            NagiosStatisticsProperty flapDetectionStatistics) {
        this.flapDetectionStatistics = flapDetectionStatistics;
    }

    /**
     * Gets the event handlers statistics.
     * 
     * @return eventHandlersStatistics
     */
    public NagiosStatisticsProperty getEventHandlersStatistics() {
        return eventHandlersStatistics;
    }

    /**
     * Sets the event handlers statistics.
     * 
     * @param eventHandlersStatistics
     *            the event handlers statistics
     */
    public void setEventHandlersStatistics(
            NagiosStatisticsProperty eventHandlersStatistics) {
        this.eventHandlersStatistics = eventHandlersStatistics;
    }

    /** String constant for 'PassiveChecks'. */
    public static final String PASSIVE_CHECKS = "PassiveChecks";

    /** The hidden field. */
    private String hiddenField = Constant.HIDDEN;

    /**
     * SubpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

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
                if (null == nagiosStatisticsHandler) {
                    nagiosStatisticsHandler = (NagiosStatisticsHandler) FacesUtils
                            .getManagedBean(NagiosStatisticsConstants.NAGIOS_STATISTICS_HANDLER);
                }

                // update node type vals
                selectedNodeType = nodeType;
                selectedNodeName = subpageIntegrator.getNodeName();
                selectedNodeId = nodeID;

                // update node parameters in nagios statistics handler
                nagiosStatisticsHandler.setSelectedNodeType(selectedNodeType);
                nagiosStatisticsHandler.setSelectedNodeName(selectedNodeName);

                // subpage - update node type vals
                setIntervalRender(true);
            }
        }

        if (isIntervalRender()) {
            init();
        }
        setIntervalRender(false);
        return hiddenField;
    }

    /**
     * Sets the hidden field.
     * 
     * @param hiddenField
     *            the hidden field
     */
    public void setHiddenField(String hiddenField) {
        this.hiddenField = hiddenField;
    }

    /** boolean variable to set the visibility of the pop-up. */
    private boolean popupVisible;

    /**
     * Checks if is popup visible.
     * 
     * @return true, if is popup visible
     */
    public boolean isPopupVisible() {
        return popupVisible;
    }

    /**
     * Sets the popup visible.
     * 
     * @param popupVisible
     *            the new popup visible
     */
    public void setPopupVisible(boolean popupVisible) {
        this.popupVisible = popupVisible;
    }

    /**
     * Open popup.
     */
    public void openPopup() {
        popupVisible = true;
    }

    /**
     * Close popup.
     */
    public void closePopup() {

        popupVisible = false;
    }

    /**
     * action to be perform on close model pop up window
     * 
     * @param e
     */
    public void closeWindow(ActionEvent e) {
        DataPaginator dataPaginator = (DataPaginator) e.getComponent()
                .findComponent("disabledListPaginator");
        if (null != dataPaginator) {
            dataPaginator.gotoFirstPage();
        }

    }

    /**
     * Constructor for NagiosStatisticsBean.
     */
    public NagiosStatisticsBean() {
        // // Set the default sort order as Ascending.
        // setAscending(true);
        subpageIntegrator = new SubpageIntegrator();
        // handle subpage integration
        handleSubpageIntegration();

        try {
            setPopupRowSize(Integer.parseInt(PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    "nagios.statistics.popup.rows")));

        } catch (NumberFormatException numberFormatException) {
            LOGGER
                    .error("NumberFormatException while getting host popup page size from status-viewer properties files Hence default page size is set");
            setPopupRowSize(Constant.FIVE);
        } catch (Exception e) {
            LOGGER
                    .error("Exception while getting host popup page size from status-viewer properties files Hence default page size is set");
            setPopupRowSize(Constant.FIVE);
        }
        // // Populate Nagios Statistics
        // populateNagiosStatistics();
        //
        // // Sets the tool-tips to be displayed on various panel grids.
        // setToolTips();

        // init();
    }

    /**
     * This method sets the tool-tips for all the panels grids.
     */
    private void setToolTips() {
        if (activeChecksStatistics.isMonitoringOptionDisabled()) {
            activeChecksStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_activeChekcksDisabled"));
        } else {
            activeChecksStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_activeChekcksEnabled"));
        }
        if (passiveChecksStatistics.isMonitoringOptionDisabled()) {

            passiveChecksStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_passiveChekcksDisabled"));
        } else {
            passiveChecksStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_passiveChekcksEnabled"));
        }
        if (notificationsStatistics.isMonitoringOptionDisabled()) {
            notificationsStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_NotificationDisabled"));
        } else {
            notificationsStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_NotificationEnabled"));
        }
        if (eventHandlersStatistics.isMonitoringOptionDisabled()) {
            eventHandlersStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_eventHandlersDisabled"));
        } else {
            eventHandlersStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_eventHandlersEnabled"));
        }
        if (flapDetectionStatistics.isMonitoringOptionDisabled()) {
            flapDetectionStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_flapDetectionDisabled"));
        } else {
            flapDetectionStatistics
                    .setTooltip(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_tooltip_flapDetectionEnabled"));
        }
    }

    /**
     * Reads parameters from request or from preferences as per application
     * type.
     */
    private void handleSubpageIntegration() {
        final String methodName = " handleSubpageIntegration() : ";
        boolean isPrefSet = subpageIntegrator
                .doSubpageIntegration(PREFERENCE_KEYS_MAP);

        if (!isPrefSet) {
            /*
             * Statistics Portlets are applicable for "Network View". So we
             * should not show error here - instead assign Node Type as NETWORK
             * with NodeId as 0.
             */
            selectedNodeType = NodeType.NETWORK;
            selectedNodeId = 0;
            selectedNodeName = Constant.EMPTY_STRING;
        } else {
            // get the required data from SubpageIntegrator
            selectedNodeType = subpageIntegrator.getNodeType();
            selectedNodeId = subpageIntegrator.getNodeID();
            selectedNodeName = subpageIntegrator.getNodeName();
            inStatusViewer = subpageIntegrator.isInStatusViewer();
        }

        // check if selected node name is null then set default node type and
        // name.
        if (selectedNodeName == null) {
            LOGGER
                    .debug("selectedNodeName is null.Setting default(Entire network) node type and name");
            selectedNodeType = NodeType.NETWORK;
            selectedNodeId = 0;
            selectedNodeName = Constant.EMPTY_STRING;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder(
                    "[Nagios Statistics Portlet] # Node Type [").append(
                    selectedNodeType).append("] # Node Name [").append(
                    selectedNodeName).append("] # Node ID [").append(
                    selectedNodeId).append("]"));
        }

        // if selected node type is still null, then return from here.
        if (null == selectedNodeType) {
            LOGGER.debug(Constant.METHOD + methodName
                    + "Found null selectedNodeType");
            return;
        }

        // Set the flag indicating if the current context is service group or
        // not.
        if (selectedNodeType == NodeType.SERVICE_GROUP) {
            setServiceGroupContext(true);
        } else {
            setServiceGroupContext(false);
        }
    }

    /**
     * This method fetches the Nagios Statistics for the entire network/hosts
     * group/service group. After fetching the statistics, this method populates
     * nagios statistics bean.
     */
    private void populateNagiosStatistics() {
        final String methodName = " populateNagiosStatistics() : ";
        NagiosStatisticProperty[] nagiosStatisticPropertyArray = null;
        try {
            switch (selectedNodeType) {
                case NETWORK:
                    nagiosStatisticPropertyArray = foundationWSFacade
                            .getNagiosStatisticsForNetwork();
                    break;

                case HOST_GROUP:
                    // Get the nagios statistics for Host Group
                    nagiosStatisticPropertyArray = foundationWSFacade
                            .getNagiosStatisticsForHostGroup(selectedNodeName);
                    // set the selected node Id here (seems weird but required
                    // for JMS Push in Dashboard)
                    if (!inStatusViewer) {
                        HostGroup hostGroupsByName = foundationWSFacade
                                .getHostGroupsByName(selectedNodeName);
                        selectedNodeId = hostGroupsByName.getHostGroupID();
                    }

                    break;

                case SERVICE_GROUP:
                    // Get the nagios statistics for Service Group
                    nagiosStatisticPropertyArray = foundationWSFacade
                            .getNagiosStatisticsForServiceGroup(selectedNodeName);
                    // set the selected node Id here (seems weird but required
                    // for JMS Push in Dashboard)
                    if (!inStatusViewer) {
                        Category categoryByName = foundationWSFacade
                                .getCategoryByName(selectedNodeName);
                        selectedNodeId = categoryByName.getCategoryId();
                    }

                    break;

                default:
                    LOGGER
                            .info("Nagios Statistics Portlet is not applicable for Node Type ["
                                    + selectedNodeType + "]");
                    break;
            }
        } catch (GWPortalGenericException ex) {
            handleError(ex.getMessage(), ex);
            return;
        }

        // check if returned nagios statistics are null or empty
        if (nagiosStatisticPropertyArray == null
                || nagiosStatisticPropertyArray.length == 0) {
            handleError(
                    "com_groundwork_portal_statusviewer_error_ErrorOccured",
                    Constant.METHOD + methodName
                            + "nagiosStatisticPropertyArray is null for "
                            + selectedNodeName);
            return;
        }

        /*
         * Using these nagios statistics, now populate the nagios statistics
         * bean
         */
        populateNagiosStatisticsBean(nagiosStatisticPropertyArray);
    }

    /**
     * Populates the NagiosStatisticProperties for active checks,passive
     * checks,notifications,flap detection,event handlers.
     * 
     * @param nagiosStatisticPropertyArray
     *            the nagios statistic property array
     */
    private void populateNagiosStatisticsBean(
            NagiosStatisticProperty[] nagiosStatisticPropertyArray) {
        // Creating an Array for nagios properties.
        for (NagiosStatisticProperty nagiosStatisticsProperty : nagiosStatisticPropertyArray) {
            if (nagiosStatisticsProperty != null) {
                /*
                 * Create instance of
                 * com.groundworkopensource.portal.statusviewer.bean
                 * .NagiosStatistics
                 * .NagiosStatisticsProperty.NagiosStatisticsProperty
                 */
                NagiosStatisticsProperty nagiosStatisticsDisplayProperty = new NagiosStatisticsProperty();

                // Populate the instance
                nagiosStatisticsDisplayProperty
                        .setPropertyName(nagiosStatisticsProperty
                                .getPropertyName());
                String hostStatistics = String.valueOf(nagiosStatisticsProperty
                        .getHostStatisticDisabled());
                nagiosStatisticsDisplayProperty
                        .setHostStatisticDisabled(hostStatistics);
                if (Constant.STRING_ZERO.equalsIgnoreCase(hostStatistics)) {
                    nagiosStatisticsDisplayProperty.setLinkVisibleHosts(false);
                }
                nagiosStatisticsDisplayProperty.setHostStatisticEnabled(String
                        .valueOf(nagiosStatisticsProperty
                                .getHostStatisticEnabled()));
                String serviceStatistics = String
                        .valueOf(nagiosStatisticsProperty
                                .getServiceStatisticDisabled());
                nagiosStatisticsDisplayProperty
                        .setServiceStatisticDisabled(serviceStatistics);
                if (Constant.STRING_ZERO.equalsIgnoreCase(serviceStatistics)) {
                    nagiosStatisticsDisplayProperty
                            .setLinkVisibleServices(false);
                }
                nagiosStatisticsDisplayProperty
                        .setServiceStatisticEnabled(String
                                .valueOf(nagiosStatisticsProperty
                                        .getServiceStatisticEnabled()));
                /*
                 * For service group context 'Disabled Hosts'/'Enabled Hosts'
                 * links should not be visible. Hence set count of
                 * disabled/enabled hosts to 0 so that 'Disabled/Enabled Host'
                 * link will not be rendered.
                 */
                if (selectedNodeType.equals(NodeType.SERVICE_GROUP)) {
                    nagiosStatisticsDisplayProperty
                            .setHostStatisticDisabled(Constant.STRING_ZERO);
                    nagiosStatisticsDisplayProperty
                            .setHostStatisticEnabled(Constant.STRING_ZERO);
                }

                // Check for isChecksEnabled property
                if (nagiosStatisticsDisplayProperty.getPropertyName().equals(
                        IS_ACTIVECHECKS_ENABLED_PROPERTY)) {
                    setActiveChecksStatistics(nagiosStatisticsDisplayProperty);
                } else if (nagiosStatisticsDisplayProperty.getPropertyName()
                        .equals(NagiosStatisticsBean.PASSIVE_CHECKS)) {
                    // Check for PassiveChecks property
                    setPassiveChecksStatistics(nagiosStatisticsDisplayProperty);
                } else if (nagiosStatisticsDisplayProperty.getPropertyName()
                        .equals(IS_NOTIFICATIONS_ENABLED_PROPERTY)) {
                    // Check for isNotificationsEnabled property
                    setNotificationsStatistics(nagiosStatisticsDisplayProperty);
                } else if (nagiosStatisticsDisplayProperty.getPropertyName()
                        .equals(IS_EVENT_HANDLERS_ENABLED_PROPERTY)) {
                    // Check for isEventHandlersEnabled property
                    setEventHandlersStatistics(nagiosStatisticsDisplayProperty);
                } else if (nagiosStatisticsDisplayProperty.getPropertyName()
                        .equals(IS_FLAP_DETECTION_ENABLED_PROPERTY)) {
                    // Check for isFlapDetectionEnabled property
                    setFlapDetectionStatistics(nagiosStatisticsDisplayProperty);
                }
            }
        }
    }

    /**
     * Sorts the disabledEntityList on host/service name. This
     * disabledEntityList list is displayed binded to the data-table which gets
     * displayed when user clicks on Disabled Services/Disabled Hosts link.
     * 
     * @param event
     *            the event
     */
    public void sort(ActionEvent event) {
        Comparator<ModelPopUpDataBean> comparator = new Comparator<ModelPopUpDataBean>() {
            public int compare(ModelPopUpDataBean popupBean1,
                    ModelPopUpDataBean popupBean2) {
                int result = 0;
                if (popupBean1 != null && popupBean2 != null) {

                    String name1 = popupBean1.getName();
                    String name2 = popupBean2.getName();

                    // For sort order ascending -
                    if (isAscending()) {
                        result = name1.compareTo(name2);
                    } else {
                        // Descending
                        result = name2.compareTo(name1);
                    }
                }
                return result;
            }
        };
        Collections.sort(disabledEntityList, comparator);
    }

    /**
     * This method sets the error flag to true,set the error message to be
     * displayed to the user and logs the error.
     * 
     * @param errorMessage
     * 
     * @param logMessage
     *            - message to be logged.
     */
    public void handleError(String errorMessage, String logMessage) {
        setError(true);
        setErrorMessage(errorMessage);
        LOGGER.error(logMessage);
    }

    /**
     * This method sets the error flag to true,set the error message to be
     * displayed to the user and logs the error.Ideally each catch block should
     * call this method.
     * 
     * @param errorMessage
     *            -
     * @param exception
     *            - Exception to be logged.
     */
    public void handleError(String errorMessage, Exception exception) {
        setError(true);
        setErrorMessage(errorMessage);
        LOGGER.error(exception);
    }

    /**
     * Call back method for JMS.
     * 
     * @param xmlTopic
     *            the xml topic
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlTopic) {
        // if (xmlTopic == null) {
        // LOGGER.debug("refresh(): Received null XML Message.");
        // return;
        // }
        // // Check if the selectedNodeType is NETWROK
        // if (selectedNodeType == NodeType.NETWORK) {
        // LOGGER
        // .debug(
        // "Processing JMS push in nagios statistics bean for nodeType : NETWORK"
        // );
        // // Set the default sort order as Ascending.
        // setAscending(true);
        // // Populate nagios statistics.
        // populateNagiosStatistics();
        // // Sets the tool-tips to be displayed on various panel grids.
        // setToolTips();
        // SessionRenderer.render(groupRenderName);
        // return;
        // }
        //
        // /*
        // * Get the JMS updates for xmlMessage & particular nodeType.
        // */
        // List<JMSUpdate> jmsUpdates = JMSUtils.getJMSUpdatesListFromXML(
        // xmlTopic, selectedNodeType);
        // if (jmsUpdates == null) {
        // LOGGER
        // .debug(
        // "refresh(): Received null JMS Updates using JMSUtils.getJMSUpdatesListFromXML() utility method"
        // );
        // return;
        // }
        //
        // // iterate through the received updates
        // for (JMSUpdate update : jmsUpdates) {
        // if (update != null) {
        // /*
        // * If the selectedNodeID matches with the enitiyID from
        // * jmsUpdates list,then only reload the data.
        // */
        // if (update.getId() == selectedNodeId) {
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER
        // .debug(
        // "Processing JMS push in nagios statistics bean for selectedNodeId : "
        // + selectedNodeId);
        // }
        // // re-fetch the data
        // init();
        // SessionRenderer.render(groupRenderName);
        // /*
        // * Important: break from here - do not iterate on further
        // * updates from JMS as requirement has already been
        // * satisfied with one.
        // */
        // break;
        // } // end of if (update.getId() == selectedNodeId)
        // } // end of if (update != null)
        // } // end of for (JMSUpdate update : jmsUpdates)

    }

    /**
     * This method fetches the data required to be displayed on nagios
     * statistics portlet.
     */
    private void init() {
        setError(false);

        // Set the default sort order as Ascending.
        setAscending(true);
        // Populate nagios statistics.
        populateNagiosStatistics();
        // Sets the tool-tips to be displayed on various panel
        // grids.
        setToolTips();
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * This method re-renders the portlet contents
     * 
     * @param event
     *            the event
     */
    public void reloadPage(ActionEvent event) {
        init();
    }

    /**
     * Sets the nagiosStatisticsModelPopUpList.
     * 
     * @param nagiosStatisticsModelPopUpList
     *            the nagiosStatisticsModelPopUpList to set
     */
    public void setNagiosStatisticsModelPopUpList(
            NagiosStatisticsModelPopUpListBean nagiosStatisticsModelPopUpList) {
        this.nagiosStatisticsModelPopUpList = nagiosStatisticsModelPopUpList;
    }

    /**
     * Returns the nagiosStatisticsModelPopUpList.
     * 
     * @return the nagiosStatisticsModelPopUpList
     */
    public NagiosStatisticsModelPopUpListBean getNagiosStatisticsModelPopUpList() {
        return nagiosStatisticsModelPopUpList;
    }

    /**
     * Sets the popupRowSize.
     * 
     * @param popupRowSize
     *            the popupRowSize to set
     */
    public void setPopupRowSize(int popupRowSize) {
        this.popupRowSize = popupRowSize;
    }

    /**
     * Returns the popupRowSize.
     * 
     * @return the popupRowSize
     */
    public int getPopupRowSize() {
        return popupRowSize;
    }

}
