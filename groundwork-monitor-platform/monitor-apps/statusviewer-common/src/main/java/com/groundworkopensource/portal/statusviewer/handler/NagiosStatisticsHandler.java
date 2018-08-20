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

import static com.groundworkopensource.portal.statusviewer.common.Constant.HOST_NAME;
import static com.groundworkopensource.portal.statusviewer.common.Constant.SERVICE_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.common.ws.impl.HostWSFacade;
import com.groundworkopensource.portal.common.ws.impl.ServiceWSFacade;
import com.groundworkopensource.portal.statusviewer.bean.ModelPopUpDataBean;
import com.groundworkopensource.portal.statusviewer.bean.NagiosStatisticsModelPopUpListBean;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.bean.nagios.NagiosStatisticsBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * This handler handles all the action events,data retrieval functions required
 * for Nagios Monitoring Statistics Portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public class NagiosStatisticsHandler implements Serializable {

    /**
     * HOST_NAME_SORT_COLUMN
     */
    private static final String HOST_NAME_SORT_COLUMN = "hostName";

    /**
     * SERVICE_DESCRIPTION_SORT_COLUMN
     */
    private static final String SERVICE_DESCRIPTION_SORT_COLUMN = "serviceDescription";

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3471894971314628774L;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(NagiosStatisticsHandler.class.getName());

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
     * String constant for 'NagiosStatisticsBean is null.'
     */
    private static final String NULL_NAGIOS_STATS_BEAN = "NagiosStatisticsBean is null.";

    /**
     * selectedNodeType
     */
    private NodeType selectedNodeType;

    /**
     * selectedNodeName
     */
    private String selectedNodeName = Constant.EMPTY_STRING;

    /**
     * FoundationWSFacade reference
     */
    private final FoundationWSFacade foundationWsFacade;

    /**
     * Is current pop up dialogue list for services? It is used to display links
     * in front of service names.
     */
    private boolean currentPopupForServices;

    /**
     * UserRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * date time pattern
     */
    private String DATETIME_PATTERN;

    /**
     * public constructor.
     */
    public NagiosStatisticsHandler() {
        // Instantiate foundationWsFacade
        foundationWsFacade = new FoundationWSFacade();
        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        // handle subpage integration
        handleSubpageIntegration();
        try {
            DATETIME_PATTERN = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.STATUS_VIEWER_DATETIME_PATTERN);
        } catch (Exception e) {
            // Ignore exception
            DATETIME_PATTERN = Constant.DEFAULT_DATETIME_PATTERN;
        }
    }

    /**
     * preferences Keys Map to be used for reading preferences.
     */
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
     * Constant for N/A
     */
    private static final String NOT_AVAILABLE = "N/A";

    /**
     * Reads parameters from request or from preferences as per application
     * type.
     */
    private void handleSubpageIntegration() {
        SubpageIntegrator subpageIntegrator = new SubpageIntegrator();
        boolean isPrefSet = subpageIntegrator
                .doSubpageIntegration(PREFERENCE_KEYS_MAP);

        if (!isPrefSet) {
            /*
             * Statistics Portlets are applicable for "Network View". So we
             * should not show error here - instead assign Node Type as NETWORK
             * with NodeId as 0.
             */
            selectedNodeType = NodeType.NETWORK;
            selectedNodeName = Constant.EMPTY_STRING;
            return;
        }
        // get the required data from SubpageIntegrator
        selectedNodeType = subpageIntegrator.getNodeType();
        selectedNodeName = subpageIntegrator.getNodeName();

        // check if selected node name is null then set default node type and
        // name.
        if (selectedNodeName == null) {
            LOGGER
                    .debug("selectedNodeName is null.Setting default(Entire network) node type and name");
            selectedNodeType = NodeType.NETWORK;
            selectedNodeName = Constant.EMPTY_STRING;
        }

        // nullify subpage integrator object
        subpageIntegrator = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder(
                    "[Nagios Statistics Portlet] # Node Type [").append(
                    selectedNodeType).append("] # Node Name [").append(
                    selectedNodeName).append("]"));
        }
    }

    /**
     * This method handles the click event for 'Disabled Hosts/Services' link.
     * Fetches the list of disabled hosts/services using appropriate web service
     * call as per the property name.(Active Checks,Passive
     * Checks,Notifications,Event Handlers,Flap Detection).
     * 
     * @param actionEvent
     * @throws GWPortalException
     */
    public void linkClicked(ActionEvent actionEvent) throws GWPortalException {
        // Get the ID of the component who generated the ActionEvent.
        String linkClicked = actionEvent.getComponent().getId();

        // Get nagiosStatistics managed bean
        NagiosStatisticsBean nagiosStatisticsBean = (NagiosStatisticsBean) FacesUtils
                .getManagedBean(NagiosStatisticsConstants.NAGIOS_STATISTICS_MANAGED_BEAN);
        NagiosStatisticsModelPopUpListBean nagiosStatisticsModelPopUpListBean = null;
        try {
            if (linkClicked.indexOf(Constant.SERVICES) != -1) {
                currentPopupForServices = true;
                Filter filter = setServicesFilterAndPopupTitle(linkClicked);
                // modelPopupDataList = getDisabledServices(filter);
                nagiosStatisticsModelPopUpListBean = new NagiosStatisticsModelPopUpListBean(
                        nagiosStatisticsBean.getPopupRowSize(), filter,
                        SERVICE_DESCRIPTION_SORT_COLUMN, Constant.SERVICES);
            } else if (linkClicked.indexOf(Constant.HOSTS) != -1) {
                currentPopupForServices = false;
                Filter filter = setHostFilterAndPopupTitle(linkClicked);
                // modelPopupDataList = getDisabledHosts(filter);
                nagiosStatisticsModelPopUpListBean = new NagiosStatisticsModelPopUpListBean(
                        nagiosStatisticsBean.getPopupRowSize(), filter,
                        HOST_NAME_SORT_COLUMN, Constant.HOSTS);
            }
        } catch (GWPortalGenericException ex) {
            handleError(ex.getMessage(), ex);
        }
        // Set the nagiosStatisticsModelPopUpListBean in nagiosStatistics
        // managed bean
        nagiosStatisticsBean
                .setNagiosStatisticsModelPopUpList(nagiosStatisticsModelPopUpListBean);
        // Set the sort order to Ascending - Default behavior
        nagiosStatisticsBean.setAscending(true);
    }

    /**
     * This method creates the left and right filter for the host properties.
     * Also sets the title of the pop-up panel which gets displayed when user
     * clicks on 'Disabled Hosts' link.
     * 
     * @param componentId
     * @return filter
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public Filter setHostFilterAndPopupTitle(String componentId)
            throws GWPortalException, WSDataUnavailableException {
        final String methodName = "setHostFilterAndPopupTitle() : ";
        // Get nagiosStatistics managed beans
        NagiosStatisticsBean nagiosStatisticsBean = (NagiosStatisticsBean) FacesUtils
                .getManagedBean(NagiosStatisticsConstants.NAGIOS_STATISTICS_MANAGED_BEAN);
        Filter filterForDisabledHosts = null;
        Filter finalFilter = null;
        if (nagiosStatisticsBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError",
                    Constant.METHOD + methodName + NULL_NAGIOS_STATS_BEAN);
            return finalFilter;
        }
        /*
         * Set the column header for the grid displayed in pop-up panel for
         * Disabled Hosts.
         */
        nagiosStatisticsBean.setHeaderTitleName(HOST_NAME);
        /*
         * Create filter criteria. Since we have to query for 2 criteria,we need
         * two filters and an 'AND' operator between them.
         */

        // Create the left filter
        Filter leftFilter = new Filter();
        // Check if the link is clicked for Active Checks-'Disabled Hosts'
        if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_ACTIVE_CHECKS_HOSTS))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_ACTIVE_CHECKS_HOSTS_STACKED))) {
            /*
             * Set the title for modal panel pop-up to 'Active Checks:Disabled
             * Hosts'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_activeChecksDisabledHosts"));
            /*
             * This filter fetches all the hosts for whom the host-status table
             * has a property 'isChecksEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.HOST_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_ACTIVECHECKS_ENABLED_PROPERTY));
        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_PASSIVE_CHECKS_HOSTS))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_PASSIVE_CHECKS_HOSTS_STACKED))) {

            /*
             * Check if the link is clicked for Passive Checks-'Disabled Hosts'
             */

            /*
             * Set the title for modal panel pop-up to 'Passive Checks:Disabled
             * Hosts'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_passiveChecksDisabledHosts"));
            /*
             * This filter fetches all the hosts for whom the host-status table
             * has a property 'isPassiveChecksEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.HOST_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_PASSIVECHECKS_ENABLED_PROPERTY));
        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_NOTIFICATIONS_HOSTS))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_NOTIFICATIONS_HOSTS_STACKED))) {
            // Check if the link is clicked for Notifications-'Disabled
            // Hosts'

            /*
             * Set the title for modal panel pop-up to 'Notification:Disabled
             * Hosts'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_notificationsDisabledHosts"));

            /*
             * This filter fetches all the hosts for whom the host-status table
             * has a property 'isNotificationsEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.HOST_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_NOTIFICATIONS_ENABLED_PROPERTY));
        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_FLAP_DETECTION_HOSTS))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_FLAP_DETECTION_HOSTS_STACKED))) {
            // Check if the link is clicked for Flap Detection -'Disabled
            // Hosts'

            /*
             * Set the title for modal panel pop-up to 'Flap Detection:Disabled
             * Hosts'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_flapDetectionDisabledHosts"));

            /*
             * This filter fetches all the hosts for whom the host-status table
             * has a property 'isFlapDetectionEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.HOST_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_FLAP_DETECTION_ENABLED_PROPERTY));

        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_EVENT_HANDLERS_HOSTS))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_EVENT_HANDLERS_HOSTS_STACKED))) {
            // Check if the link is clicked for Event Handlers -'Disabled
            // Hosts'

            /*
             * Set the title for modal panel pop-up to 'Event Handlers:Disabled
             * Hosts'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_eventHandlersDisabledHosts"));

            /*
             * This filter fetches all the hosts for whom the host-status table
             * has a property 'isEventHandlersEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.HOST_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_EVENT_HANDLERS_ENABLED_PROPERTY));
        }
        leftFilter.setOperator(FilterOperator.EQ);

        // Create the right filter
        Filter rightFilter = new Filter();
        rightFilter
                .setBooleanProperty(new BooleanProperty(
                        FilterConstants.HOST_STATUS_PROPERTYVALUES_VALUEBOOLEAN,
                        false));
        rightFilter.setOperator(FilterOperator.EQ);

        // left filter AND right filter
        filterForDisabledHosts = Filter.AND(leftFilter, rightFilter);

        // create the filter for HOST GROUP
        Filter contextFilter = null;
        switch (selectedNodeType) {
            case HOST_GROUP:
                contextFilter = getHostFilterForHostGroup();
                break;
            case SERVICE_GROUP:
                break;
            default:
                break;
        }

        // contextFilter will be null in case of nodeType = NETWORK
        if (contextFilter != null) {
            // filterForDisabledHosts AND contextFilter
            finalFilter = Filter.AND(filterForDisabledHosts, contextFilter);
        } else {
            finalFilter = filterForDisabledHosts;
        }
        return finalFilter;
    }

    /**
     * This method constructs the filter to get the list of hosts in a
     * particular host group.
     * 
     * @return finalFilter
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    private Filter getHostFilterForHostGroup() throws GWPortalException,
            WSDataUnavailableException {
        Filter finalFilter = null;
        final String methodName = " getHostFilterForHostGroup() : ";

        if (selectedNodeName == null
                || selectedNodeName.equals(Constant.EMPTY_STRING)) {
            handleError(
                    "com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError",
                    Constant.METHOD + methodName + "hostGroup with Name ["
                            + selectedNodeName + "]is null");
            return finalFilter;
        }
        // Create the filter to get the list of hosts in host group.
        finalFilter = new Filter(FilterConstants.HOST_GROUP_NAME,
                FilterOperator.EQ, selectedNodeName);
        return finalFilter;
    }

    /**
     * This method creates the left and right filter for the services
     * properties. Also sets the title of the pop-up panel which gets displayed
     * when user clicks on 'Disabled Services' link.
     * 
     * @param componentId
     * @return filter
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public Filter setServicesFilterAndPopupTitle(String componentId)
            throws GWPortalException, WSDataUnavailableException {
        final String methodName = " setServicesFilterAndPopupTitle() : ";
        // Get nagiosStatistics managed beans
        NagiosStatisticsBean nagiosStatisticsBean = (NagiosStatisticsBean) FacesUtils
                .getManagedBean(NagiosStatisticsConstants.NAGIOS_STATISTICS_MANAGED_BEAN);
        Filter filterForDisabledServices = null;
        Filter finalFilter = null;
        if (nagiosStatisticsBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError",
                    Constant.METHOD + methodName + NULL_NAGIOS_STATS_BEAN);
            return finalFilter;
        }
        /*
         * Set the column header for the grid displayed in pop-up panel for
         * Disabled Services.
         */
        nagiosStatisticsBean.setHeaderTitleName(SERVICE_NAME);

        // Create filter criteria.
        // Create the left filter
        Filter leftFilter = new Filter();
        // Check if the link is clicked for Active Checks-'Disabled
        // Services'
        if ((componentId
                .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_ACTIVE_CHECKS_SERVICES))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_ACTIVE_CHECKS_SERVICES_STACKED))) {
            /*
             * Set the title for modal panel pop-up to 'Active Checks:Disabled
             * Services'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_activeChecksDisabledServices"));
            /*
             * This filter fetches all the services for whom the service-status
             * table has a property 'isChecksEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.SERVICE_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_ACTIVECHECKS_ENABLED_PROPERTY));
        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_PASSIVE_CHECKS_SERVICES))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_PASSIVE_CHECKS_SERVICES_STACKED))) {
            /*
             * Check if the link is clicked for Passive Checks-'Disabled
             * Services'
             */

            /*
             * Set the title for modal panel pop-up to 'Passive Checks:Disabled
             * Services'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_passiveChecksDisabledServices"));
            /*
             * This filter fetches all the services for whom the service-status
             * table has a property 'isAcceptPassiveChecks'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.SERVICE_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_ACCEPT_PASSIVECHECKS_PROPERTY));
        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_NOTIFICATIONS_SERIVCES))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_NOTIFICATIONS_SERIVCES_STACKED))) {
            /*
             * Check if the link is clicked for Notifications-'Disabled
             * Services'
             */

            /*
             * Set the title for modal panel pop-up to 'Notification:Disabled
             * Services'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_notificationDisabledServices"));
            /*
             * This filter fetches all the services for whom the service-status
             * table has a property 'isNotificationsEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.SERVICE_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_NOTIFICATIONS_ENABLED_PROPERTY));
        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_FLAP_DETECTION_SERIVCES))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_FLAP_DETECTION_SERIVCES_STACKED))) {
            /*
             * Check if the link is clicked for Flap Detection-'Disabled
             * Services'
             */

            /*
             * Set the title for modal panel pop-up to 'Flap Detection:Disabled
             * Services'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_flapDetectionDisabledServices"));
            /*
             * This filter fetches all the services for whom the service-status
             * table has a property 'isNotificationsEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.SERVICE_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_FLAP_DETECTION_ENABLED_PROPERTY));
        } else if ((componentId
                .equals(NagiosStatisticsConstants.COMPONENT_CMD_EVENT_HANDLERS_SERIVCES))
                || (componentId
                        .equalsIgnoreCase(NagiosStatisticsConstants.COMPONENT_CMD_EVENT_HANDLERS_SERIVCES_STACKED))) {
            /*
             * Check if the link is clicked for Event Handlers -'Disabled
             * Services'
             */

            /*
             * Set the title for modal panel pop-up to 'Event Handlers:Disabled
             * Services'
             */
            nagiosStatisticsBean
                    .setPanelPopupTitle(ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_popupTitle_eventHandlersDisabledServices"));

            /*
             * This filter fetches all the services for whom the service-status
             * table has a property 'isEventHandlersEnabled'
             */
            leftFilter
                    .setStringProperty(new StringProperty(
                            FilterConstants.SERVICE_STATUS_PROPERTYVALUES_NAME,
                            NagiosStatisticsConstants.IS_EVENT_HANDLERS_ENABLED_PROPERTY));
        }
        leftFilter.setOperator(FilterOperator.EQ);
        // Create the right filter
        Filter rightFilter = new Filter(
                FilterConstants.SERVICE_STATUS_PROPERTYVALUES_VALUEBOOLEAN,
                FilterOperator.EQ, false);
        // left filter AND right filter
        filterForDisabledServices = Filter.AND(leftFilter, rightFilter);

        // Create the filter for current context
        Filter contextFilter = null;
        switch (selectedNodeType) {
            case HOST_GROUP:
                contextFilter = getServiceFilterForHostGroup();
                break;
            case SERVICE_GROUP:
                contextFilter = getServiceFilterForServiceGroup();
                break;
            default:
                break;
        }
        // contextFilter will be null in case of nodeType = NETWORK
        if (contextFilter != null) {
            // filterForDisabledHosts AND contextFilter
            finalFilter = Filter.AND(filterForDisabledServices, contextFilter);
        } else {
            // Return filterForDisabledServices
            finalFilter = filterForDisabledServices;
        }
        return finalFilter;
    }

    /**
     * This method constructs the filter to get the list of services in a
     * particular service group.
     * 
     * @return
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private Filter getServiceFilterForServiceGroup()
            throws WSDataUnavailableException, GWPortalException {
        final String methodName = " getServiceFilterForServiceGroup() : ";
        Filter finalFilter = null;
        if (selectedNodeName == null
                || selectedNodeName.equals(Constant.EMPTY_STRING)) {
            handleError(
                    "com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError",
                    Constant.METHOD + methodName + "service group with Name ["
                            + selectedNodeName + "] is null.");
            return finalFilter;
        }
        CategoryEntity[] entities = foundationWsFacade
                .getCategoryEntities(selectedNodeName);
        // Logic to generate comma separated list of the result of
        // object-id's
        StringBuilder categoryStringBuilder = new StringBuilder();
        if ((entities == null) || (entities.length == 0)) {
            handleError(
                    "com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError",
                    Constant.METHOD + methodName + "No services found.");
            return finalFilter;
        }
        for (CategoryEntity categoryEntity : entities) {
            // build comma separated string of Service IDs in service
            // group
            if (categoryEntity == null) {
                handleError(
                        "com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError",
                        Constant.METHOD + methodName + "Null service.");
                return finalFilter;
            }
            categoryStringBuilder.append(categoryEntity.getObjectID()
                    + Constant.COMMA);
        }
        String objectIdList = categoryStringBuilder.toString();
        if (objectIdList == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError",
                    Constant.METHOD + methodName + "Null objectIdList.");
            // No list found
            return finalFilter;
        }
        finalFilter = new Filter(FilterConstants.SERVICE_STATUS_ID,
                FilterOperator.IN, objectIdList);

        return finalFilter;
    }

    /**
     * This method constructs the filter to get the list of services in a
     * particular host group.
     * 
     * @return
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    private Filter getServiceFilterForHostGroup() throws GWPortalException,
            WSDataUnavailableException {
        Filter finalFilter = null;
        if (selectedNodeName == null
                || selectedNodeName.equals(Constant.EMPTY_STRING)) {
            setError(true);
            setErrorMessage(ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_nagiosStatisticsPortlet_getDisabledHostsError"));
            return finalFilter;
        }
        finalFilter = new Filter(
                FilterConstants.SERVICES_BY_HOST_GROUP_NAME_STRING_PROPERTY,
                FilterOperator.EQ, selectedNodeName);
        return finalFilter;
    }

    /**
     * Retrieves list of disabled hosts using getHostsbyCriteria() ws call.
     * 
     * @param filter
     * @return modelPopupDataList
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public List<ModelPopUpDataBean> getDisabledHosts(Filter filter)
            throws GWPortalException, WSDataUnavailableException {
        // Create instance of HostWSFacade
        HostWSFacade hostWSFacade = new HostWSFacade();
        // Call getSimpleHostsbyCriteria() API
        WSFoundationCollection wsFoundationCollection = hostWSFacade
                .getSimpleHostsbyCriteria(filter, null, 0, 0, false);
        if ((wsFoundationCollection == null)
                || (wsFoundationCollection.getSimpleHost() == null)
                || (wsFoundationCollection.getSimpleHost().length == 0)) {
            throw new GWPortalException(
                    "getSimpleHostsbyCriteria() returned empty results");
        }
        SimpleHost[] simpleHostArray = wsFoundationCollection.getSimpleHost();
        List<ModelPopUpDataBean> modelPopupDataList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        if (simpleHostArray != null) {
            /*
             * Iterate over the Host array and populate the ModelPopUpDataBean
             * list
             */
            for (SimpleHost simpleHost : simpleHostArray) {
                ModelPopUpDataBean modelPopUpDataBean = new ModelPopUpDataBean();
                if (simpleHost != null) {
                    modelPopUpDataBean.setName(simpleHost.getName());
                    // Set the host URL, so that user can navigate to the host
                    // page from modal-popup of disabled Hosts.
                    modelPopUpDataBean.setSubPageURL(NodeURLBuilder
                            .buildNodeURL(NodeType.HOST,
                                    simpleHost.getHostID(), simpleHost
                                            .getName()));
                    Date lastCheckTime = simpleHost.getLastCheckTime();
                    if (lastCheckTime == null) {
                        modelPopUpDataBean.setDatetime(NOT_AVAILABLE);
                    } else {
                        try {
                            modelPopUpDataBean.setDatetime(DateUtils.format(
                                    lastCheckTime, DATETIME_PATTERN));
                        } catch (Exception e) {
                            modelPopUpDataBean.setDatetime(DateUtils.format(
                                    lastCheckTime,
                                    Constant.DEFAULT_DATETIME_PATTERN));
                        }
                    }
                    /* To get the icon to be displayed for the host-status. */
                    NetworkObjectStatusEnum hostStatus = MonitorStatusUtilities
                            .getEntityStatus(simpleHost, NodeType.HOST);
                    if (hostStatus != null) {
                        modelPopUpDataBean
                                .setIconPath(hostStatus.getIconPath());
                    }
                    modelPopupDataList.add(modelPopUpDataBean);
                }
            }
        }
        return modelPopupDataList;
    }

    /**
     * Retrieves list of disabled services using getServicesbyCriteria() ws
     * call.
     * 
     * @param filter
     * @return modelPopupDataList
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public List<ModelPopUpDataBean> getDisabledServices(Filter filter)
            throws GWPortalException, WSDataUnavailableException {
        // Create instance of serviceWSFacade
        ServiceWSFacade serviceWSFacade = new ServiceWSFacade();

        // Call getServicesbyCriteria() API
        SimpleServiceStatus[] simpleServicesArray = serviceWSFacade
                .getSimpleServicesbyCriteria(filter, null, -1, -1);
        List<ModelPopUpDataBean> modelPopupDataList = Collections
                .synchronizedList(new ArrayList<ModelPopUpDataBean>());
        if (simpleServicesArray != null) {
            /*
             * Iterate over the Services array and populate the
             * ModelPopUpDataBean list
             */
            for (SimpleServiceStatus simpleService : simpleServicesArray) {
                ModelPopUpDataBean modelPopUpDataBean = new ModelPopUpDataBean();
                if (simpleService != null) {
                    modelPopUpDataBean.setName(simpleService.getDescription());
                    // Set the host URL, so that user can navigate to the
                    // service page from modal-popup of disabled Service.
                    modelPopUpDataBean.setSubPageURL(NodeURLBuilder
                            .buildNodeURL(NodeType.SERVICE, simpleService
                                    .getServiceStatusID(), simpleService
                                    .getDescription()));
                    Date lastCheckTime = simpleService.getLastCheckTime();
                    if (lastCheckTime == null) {
                        modelPopUpDataBean.setDatetime(NOT_AVAILABLE);
                    } else {
                        try {
                            modelPopUpDataBean.setDatetime(DateUtils.format(
                                    lastCheckTime, DATETIME_PATTERN));
                        } catch (Exception e) {
                            modelPopUpDataBean.setDatetime(DateUtils.format(
                                    lastCheckTime,
                                    Constant.DEFAULT_DATETIME_PATTERN));
                        }
                    }
                    /* To get the icon to be displayed for the service-status. */
                    NetworkObjectStatusEnum serviceStatus = MonitorStatusUtilities
                            .getEntityStatus(simpleService, NodeType.SERVICE);
                    if (serviceStatus != null) {
                        modelPopUpDataBean.setIconPath(serviceStatus
                                .getIconPath());
                    }
                    String parentName = simpleService.getHostName();
                    int parentId = simpleService.getHostId();
                    if (parentName != null) {
                        modelPopUpDataBean.setParentName(parentName);
                        modelPopUpDataBean.setParentPageURL(NodeURLBuilder
                                .buildNodeURL(NodeType.HOST, parentId,
                                        parentName));
                    }
                    modelPopupDataList.add(modelPopUpDataBean);
                }
            }
        }
        return modelPopupDataList;
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
     * Sets the currentPopupForServices.
     * 
     * @param currentPopupForServices
     *            the currentPopupForServices to set
     */
    public void setCurrentPopupForServices(boolean currentPopupForServices) {
        this.currentPopupForServices = currentPopupForServices;
    }

    /**
     * Returns the currentPopupForServices.
     * 
     * @return the currentPopupForServices
     */
    public boolean isCurrentPopupForServices() {
        return currentPopupForServices;
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
     * Sets the selectedNodeType.
     * 
     * @param selectedNodeType
     *            the selectedNodeType to set
     */
    public void setSelectedNodeType(NodeType selectedNodeType) {
        this.selectedNodeType = selectedNodeType;
    }

    /**
     * Returns the selectedNodeName.
     * 
     * @return the selectedNodeName
     */
    public String getSelectedNodeName() {
        return selectedNodeName;
    }

    /**
     * Sets the selectedNodeName.
     * 
     * @param selectedNodeName
     *            the selectedNodeName to set
     */
    public void setSelectedNodeName(String selectedNodeName) {
        this.selectedNodeName = selectedNodeName;
    }
}
