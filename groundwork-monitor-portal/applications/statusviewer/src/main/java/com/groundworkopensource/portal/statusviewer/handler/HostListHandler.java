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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIViewRoot;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.HostGroup;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.AcknowledgePopupBean;
import com.groundworkopensource.portal.statusviewer.bean.HostListDataBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.FilterComputer;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.icesoft.faces.component.datapaginator.DataPaginator;

/**
 * This class provides necessary information and methods to display Host List
 * under Host groups and other places. This class fetches data on-the-fly, by
 * using on demand pagination.
 * 
 * @author nitin_jadhav
 * 
 */
public class HostListHandler extends ServerPush implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6203263632235928775L;

    /**
     * HGVFORM_HOST_LIST_PAGER_BOTTOM : id of paginator in host group view host
     * list
     */
    private static final String HGVFORM_HOST_LIST_PAGER_BOTTOM = "HGVform:HostListPagerBottom";

    /**
     * service data table instance variable
     */
    private HostListDataBean dataTableBean;

    /**
     * Constant PORTAL_STATUSVIEWER_MAX_STATUS_INFORMATION_DETAILS_CHARS
     */
    private static final String PORTAL_STATUSVIEWER_MAX_STATUS_INFORMATION_DETAILS_CHARS = "portal.statusviewer.maxStatusInformationDetailsChars";

    /**
     * PORTAL_STATUSVIEWER_HOST_LIST_PAGE_SIZE constant
     */
    private static final String PORTAL_STATUSVIEWER_HOST_LIST_PAGE_SIZE = "portal.statusviewer.hostListPageSize";

    /**
     * Host name parameter
     */
    private static final String PARAM_HOST_NAME = "hostName";

    /**
     * logger
     */
    private static final Logger LOGGER = Logger.getLogger(HostListHandler.class
            .getName());

    /**
     * Constant DEFAULT_HOST_LUST_PAGE_SIZE
     */
    private static final int DEFAULT_HOST_LIST_PAGE_SIZE = 6;

    /**
     * Constant DEFAULT_MAX_CHAR_SIZE
     */
    private static final int DEFAULT_MAX_CHAR_SIZE = 100;

    /**
     * HostList Page Size. This property will be read from properties file. if
     * not present, it will take default page size 6.
     */
    private int hostListPageSize;

    /**
     * Max Status Information Details characters. This property will be read
     * from properties file. if not present, it will take default size 100.
     */
    private int maxStatusInformationDetailsChars;

    /**
     * foundationWSFacade Object to call web services.
     */
    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * stateController instance variable
     */
    private StateController stateController = null;

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
     * Flag to identify if portlet is placed in StatusViewer sub-pages apart
     * from Network View.
     */
    private boolean inStatusViewer;

    /**
     * ReferenceTreeMetaModel instance.
     */
    private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            .getManagedBean(Constant.REFERENCE_TREE);

    /**
     * preferences Keys Map to be used for reading preferences.
     */
    private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
    static {
        // PREFERENCE_KEYS_MAP.put(PreferenceConstants.HOST_GROUP_NAME,
        // NodeType.HOST_GROUP);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_GROUP_PREF,
                NodeType.HOST_GROUP);
        PREFERENCE_KEYS_MAP.put(
                Constant.PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE,
                NodeType.HOST_GROUP);
    }

    /**
     * hostListHiddenField
     */
    private String hostListHiddenField;
    /**
     * hostListHiddenField
     */
    private String hostListNavHiddenField;
    /**
     * old filter string, used to compare and detect filter change.
     */
    private String oldHostFilter = Constant.EMPTY_STRING;

    /**
     * SubpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

    /**
     * userExtendedRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Flag for "Show up hosts" filter.
     */
    private boolean showNonUpHosts = false;

    /**
     * Flag for "Show unreachable hosts" filter.
     */
    private boolean showNonUnreachableHosts = false;

    /**
     * Flag for "Show down (unscheduled) hosts" filter.
     */
    private boolean showNonDownUnScheduledHosts = false;

    /**
     * Flag for "Show down (scheduled) hosts" filter.
     */
    private boolean showNonDownScheduledHosts = false;
    
    /**
     * Flag for "Show pending hosts" filter.
     */
    private boolean showNonPendingHosts = false;

    /**
     * Flag for "Show Acknowledged hosts" filter.
     */
    private boolean showNonAcknowledgedHosts = false;
    
    /**
     * Id for form UI component
     */
    private String hostListFrmID;

    /**
     * monitorStatusFilter to be used for retrieving hosts which are Non up.
     */
    private static final Filter hostNonUpFilter = new Filter(
            FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
            FilterOperator.NE, "UP");
    
    /**
     * monitorStatusFilter to be used for retrieving hosts which are Non unreachable.
     */
    private static final Filter hostNonUnreachableFilter = new Filter(
            FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
            FilterOperator.NE, "UNREACHABLE");
    
    /**
     * monitorStatusFilter to be used for retrieving hosts which are Non unreachable acknowledged.
     */
    private static final Filter hostNonUnreachableAcknowledgedFilter = new Filter(
            FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
            FilterOperator.NE, "ACKNOWLEDGEMENT (UNREACHABLE)");
    
    /**
     * monitorStatusFilter to be used for retrieving hosts which are Non down unscheduled.
     */
    private static final Filter hostNonDownUnscheduledFilter = new Filter(
            FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
            FilterOperator.NE, "UNSCHEDULED DOWN");
    
    /**
     * monitorStatusFilter to be used for retrieving hosts which are Non down scheduled.
     */
    private static final Filter hostNonDownScheduledFilter = new Filter(
            FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
            FilterOperator.NE, "SCHEDULED DOWN");
    
    /**
     * monitorStatusFilter to be used for retrieving hosts which are Non down Acknowledged.
     */
    private static final Filter hostNonDownAcknowledgedFilter = new Filter(
            FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
            FilterOperator.NE, "ACKNOWLEDGEMENT (DOWN)");
    
    /**
     * monitorStatusFilter to be used for retrieving hosts which Non are pending.
     */
    private static final Filter hostNonPendingFilter = new Filter(
            FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
            FilterOperator.NE, "PENDING");
    
    /**
     * constructor
     */
    public HostListHandler() {
        // super(Constant.HOSTS_RENDER_GROUP);

        subpageIntegrator = new SubpageIntegrator();
        int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
        hostListFrmID = "HLPortlet_frmHostList" + randomID;
        // set values from property file
        setValuesFromPropertyFile();

        dataTableBean = new HostListDataBean(hostListPageSize);

        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        // handle subpage integration
        if (!handleSubpageIntegration()) {
            return;
        }

        // if in the dashboard, retrieve the HostGroupID from HostGroupName
        // passed as preference. Assign this Id to the selectedNodeId
        if (!doDashboardProcessing()) {
            return;
        }

        if (null == selectedNodeName) {
            dataTableBean
                    .handleError("Request Parameters not obtained from Interceptor.");
            return;
        }

        // get primary filter
        Filter primaryFilter = getPrimaryFilter();
        dataTableBean.setPrimaryFilter(primaryFilter);

        dataTableBean
                .setMaxStatusInformationDetailsChars(maxStatusInformationDetailsChars);
        /*
         * for default sorting by monitor status: Default Host and Service List
         * Portlets behavior should be configured as "Order by MonitorStatus"
         * and "the services that are NOT OK should come up first".
         */
        this.dataTableBean.setAscending(false);
        this.dataTableBean
                .setSortColumnName(FilterConstants.HOST_MONITOR_STATUS);

    }

    /**
     * Does Dashboard related processing.
     */
    private boolean doDashboardProcessing() {
        if (!inStatusViewer) {
            try {
                // "Show Non Up hosts" filter.
                String showNonUpHostsPreference = FacesUtils
                        .getPreference(Constant.HOST_FILTER_UP_PREF);
                if (null == showNonUpHostsPreference
                        || showNonUpHostsPreference
                                .equals(Constant.EMPTY_STRING)) {
                    showNonUpHosts = true;
                } else {
                    showNonUpHosts = Boolean
                            .parseBoolean(showNonUpHostsPreference);
                }
                // "Show Non Unreachable hosts" filter.
                String showNonUnreachableHostsPreference = FacesUtils
                        .getPreference(Constant.HOST_FILTER_UNREACHABLE_PREF);
                if (null == showNonUnreachableHostsPreference
                        || showNonUnreachableHostsPreference
                                .equals(Constant.EMPTY_STRING)) {
                    showNonUnreachableHosts = false;
                } else {
                    showNonUnreachableHosts = Boolean
                            .parseBoolean(showNonUnreachableHostsPreference);
                }
                // "Show Non Down unscheduled hosts" filter.
                String showNonDownUnScheduledHostsPreference = FacesUtils
                        .getPreference(Constant.HOST_FILTER_DOWN_UNSCHEDULED_PREF);
                if (null == showNonDownUnScheduledHostsPreference
                        || showNonDownUnScheduledHostsPreference
                                .equals(Constant.EMPTY_STRING)) {
                    showNonDownUnScheduledHosts = false;
                } else {
                    showNonDownUnScheduledHosts = Boolean
                            .parseBoolean(showNonDownUnScheduledHostsPreference);
                }
                // "Show Non Down scheduled hosts" filter.
                String showNonDownScheduledHostsPreference = FacesUtils
                        .getPreference(Constant.HOST_FILTER_DOWN_SCHEDULED_PREF);
                if (null == showNonDownScheduledHostsPreference
                        || showNonDownScheduledHostsPreference
                                .equals(Constant.EMPTY_STRING)) {
                    showNonDownScheduledHosts = false;
                } else {
                    showNonDownScheduledHosts = Boolean
                            .parseBoolean(showNonDownScheduledHostsPreference);
                }
                // "Show Non Pending hosts" filter.
                String showNonPendingHostsPreference = FacesUtils
                        .getPreference(Constant.HOST_FILTER_PENDING_PREF);
                if (null == showNonPendingHostsPreference
                        || showNonPendingHostsPreference
                                .equals(Constant.EMPTY_STRING)) {
                    showNonPendingHosts = false;
                } else {
                    showNonPendingHosts = Boolean
                            .parseBoolean(showNonPendingHostsPreference);
                }
                // "Show NonAcknowledged hosts" filter.
                String showNonAcknowledgedHostsPreference = FacesUtils
                        .getPreference(Constant.HOST_FILTER_ACKNOWLEDGED_PREF);
                if (null == showNonAcknowledgedHostsPreference
                        || showNonAcknowledgedHostsPreference
                                .equals(Constant.EMPTY_STRING)) {
                    showNonAcknowledgedHosts = false;
                } else {
                    showNonAcknowledgedHosts = Boolean
                            .parseBoolean(showNonAcknowledgedHostsPreference);
                }
            } catch (PreferencesException e) {
                /*
                 * ignore the exception.For HOST_FILTER_**_PREF, set it to
                 * 'true' for showNonUpHosts and false for the rest if not available.
                 */
            	// JIRA GWMON-9074
                showNonUpHosts = true;
                showNonUnreachableHosts = false;
                showNonDownUnScheduledHosts = false;
                showNonDownScheduledHosts = false;
                showNonPendingHosts = false;
                showNonAcknowledgedHosts = false;
            }
            dataTableBean.setShowNonUpHosts(showNonUpHosts);
            dataTableBean.setShowNonUnreachableHosts(showNonUnreachableHosts);
            dataTableBean.setShowNonDownUnScheduledHosts(showNonDownUnScheduledHosts);
            dataTableBean.setShowNonDownScheduledHosts(showNonDownScheduledHosts);
            dataTableBean.setShowNonPendingHosts(showNonPendingHosts);
            dataTableBean.setShowNonAcknowledgedHosts(showNonAcknowledgedHosts);
            try {
                if (userExtendedRoleBean.getExtRoleHostGroupList().contains(
                        UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                    String inadequatePermissionsMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                            + " [ Host Groups ] data";
                    dataTableBean.handleInfo(inadequatePermissionsMessage);
                    return false;
                }

                if (!NodeType.NETWORK.equals(selectedNodeType)) {
                    HostGroup hostGroupsByName = foundationWSFacade
                            .getHostGroupsByName(selectedNodeName);
                    selectedNodeId = hostGroupsByName.getHostGroupID();

                    // check for extended role permissions
                    if (!referenceTreeModel
                            .checkNodeForExtendedRolePermissions(
                                    selectedNodeId, NodeType.HOST_GROUP,
                                    selectedNodeName, userExtendedRoleBean
                                            .getExtRoleHostGroupList(),
                                    userExtendedRoleBean
                                            .getExtRoleServiceGroupList())) {
                        String inadequatePermissionsMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                + " [" + selectedNodeName + "]";
                        dataTableBean.handleInfo(inadequatePermissionsMessage);
                        return false;
                    }

                } else {
                    // For entire Network, check if extended role lists are
                    // empty
                    if (!userExtendedRoleBean.getExtRoleHostGroupList()
                            .isEmpty()) {
                        // user does not have access to entire network
                       /* String inadequatePermissionsMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                + " [ Entire Network ] data.";
                        dataTableBean.handleInfo(inadequatePermissionsMessage);
                        return false;*/
                    }
                }
            } catch (WSDataUnavailableException e) {
                String hostGroupNotAvailableErrorMessage = ResourceUtils
                        .getLocalizedMessage("com_groundwork_portal_statusviewer_hostGroupUnavailable")
                        + " [" + selectedNodeName + "]";
                LOGGER.error(hostGroupNotAvailableErrorMessage);
                dataTableBean.handleInfo(hostGroupNotAvailableErrorMessage);
                return false;
            } catch (GWPortalGenericException e) {
                LOGGER
                        .warn(
                                "Exception while retrieving HostGroup By Name in Dashboard. JMS PUSH may not work. Exception ["
                                        + e.getMessage() + "]", e);
            }

        }
        return true;
    }

    /**
     * Handles the subpage integration: Reads parameters from request in case of
     * Status Viewer. If portlet is in dashboard, reads preferences.
     * 
     * @return
     */
    private boolean handleSubpageIntegration() {
        boolean isPrefSet = subpageIntegrator
                .doSubpageIntegration(PREFERENCE_KEYS_MAP);
        stateController = subpageIntegrator.getStateController();
        try {
	        if (!isPrefSet || FacesUtils.getPreference(Constant.NODE_TYPE_PREF).equals("Network")) {
	            /*
	             * host list Portlets are applicable for "Network View" in dash
	             * board. So we should not show error here - instead assign Node
	             * Type as NETWORK with NodeId as 0.
	             */
	            selectedNodeType = NodeType.NETWORK;
	            selectedNodeId = 0;
	            selectedNodeName = Constant.EMPTY_STRING;
	            dataTableBean.setSelectedNodeType(selectedNodeType);
	            inStatusViewer = subpageIntegrator.isInStatusViewer();
	            // dataTableBean.handleInfo(new
	            // PreferencesException().getMessage());
	            return true;
	        }
        } catch (PreferencesException e) {
        	LOGGER.error("Error reading preferences..");
        }
        // get the required data from SubpageIntegrator
        selectedNodeType = subpageIntegrator.getNodeType();
        selectedNodeId = subpageIntegrator.getNodeID();
        selectedNodeName = subpageIntegrator.getNodeName();
        inStatusViewer = subpageIntegrator.isInStatusViewer();
        dataTableBean.setSelectedNodeType(selectedNodeType);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Host List Portlet] # Node Type [" + selectedNodeType
                    + "] # Node Name [" + selectedNodeName + "] # Node ID ["
                    + selectedNodeId + "]");
        }
        return true;
    }

    /**
     * 
     * @return primaryFilter
     */
    private Filter getPrimaryFilter() {
        Filter primaryFilter = null;

        if (NodeType.NETWORK == selectedNodeType) {

            // construct filter for "all the hosts under Entire Network.." and
            // set in dataTableBean
        	primaryFilter = new Filter("applicationType.name",
                    FilterOperator.EQ, "NAGIOS");
            List<String> extRoleHostGroupList = userExtendedRoleBean.getExtRoleHostGroupList();
        	if (!extRoleHostGroupList.isEmpty()) {            		
            	Iterator<NetworkMetaEntity> authorizedHostGroups= referenceTreeModel.getExtRoleHostGroups(extRoleHostGroupList);
            	Filter hgFilter = null;
        		while (authorizedHostGroups.hasNext()) {
        			NetworkMetaEntity authorizedHostGroup = authorizedHostGroups.next();	 
        			if (hgFilter == null)
	        			hgFilter = new Filter(FilterConstants.HOST_GROUP_NAME,
		                        FilterOperator.EQ, authorizedHostGroup.getName());
        			else {
        				Filter tempFilter = new Filter(FilterConstants.HOST_GROUP_NAME,
		                        FilterOperator.EQ, authorizedHostGroup.getName());
        				hgFilter = Filter.OR(tempFilter, hgFilter);
        			}
        		} // end while    
        		primaryFilter = Filter.AND(primaryFilter, hgFilter);    
        	} // end if
            if (showNonUpHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonUpFilter);
            if (showNonUnreachableHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonUnreachableFilter);
            if (showNonDownUnScheduledHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonDownUnscheduledFilter);
            if (showNonDownScheduledHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonDownScheduledFilter);
            if (showNonPendingHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonPendingFilter);
            
        } else {
            // construct filter for "all the hosts under host group.." and
            // set in dataTableBean
            primaryFilter = new Filter(FilterConstants.HOST_GROUP_NAME,
                    FilterOperator.EQ, selectedNodeName);
            if (showNonUpHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonUpFilter);
            if (showNonUnreachableHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonUnreachableFilter);
            if (showNonDownUnScheduledHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonDownUnscheduledFilter);
            if (showNonDownScheduledHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonDownScheduledFilter);
            if (showNonPendingHosts)
            	primaryFilter = Filter.AND(primaryFilter, hostNonPendingFilter);
        }

        return primaryFilter;
    }

    /**
     * Sets values from property file. if not present, default values will be
     * used.
     */
    private void setValuesFromPropertyFile() {

        // get Page List Size
        boolean hostListPageSizeObtained = false;
        if (!inStatusViewer) {
            try {
                String hostsPerPagePreference = FacesUtils
                        .getPreference(Constant.HOSTS_PER_PAGE_PREF);
                if (null != hostsPerPagePreference
                        && !hostsPerPagePreference.trim().equals(
                                Constant.EMPTY_STRING)) {
                    hostListPageSize = Integer.parseInt(hostsPerPagePreference);
                    hostListPageSizeObtained = true;
                }
            } catch (PreferencesException e) {
                hostListPageSizeObtained = false;
            } catch (NumberFormatException e) {
                hostListPageSizeObtained = false;
            }
        }

        // get hostListPageSize and dateFormatProperty from application
        // property files.
        if (!hostListPageSizeObtained) {
            String pageSizeProperty = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    PORTAL_STATUSVIEWER_HOST_LIST_PAGE_SIZE);
            try {
                if (pageSizeProperty != null) {
                    hostListPageSize = Integer.parseInt(pageSizeProperty);
                    LOGGER.info("Got hostListPageSize from properties file: "
                            + hostListPageSize);
                    if (hostListPageSize == 0) {
                        hostListPageSize = DEFAULT_HOST_LIST_PAGE_SIZE;
                    }
                } else {
                    // null - use default value
                    LOGGER
                            .info("value for hostListPageSize not found in properties file. using default value.");
                    hostListPageSize = DEFAULT_HOST_LIST_PAGE_SIZE;
                }
            } catch (NumberFormatException e) {
                // error! use default value
                LOGGER
                        .info("Got incorrect value for hostListPageSize from properties file. using default value.");
                hostListPageSize = DEFAULT_HOST_LIST_PAGE_SIZE;
            }
        }

        // get portal.statusviewer.maxStatusInformationDetailsChars and
        // dateFormatProperty from application
        // property files.
        String maxCharsProperty = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                PORTAL_STATUSVIEWER_MAX_STATUS_INFORMATION_DETAILS_CHARS);
        try {
            if (maxCharsProperty != null) {
                maxStatusInformationDetailsChars = Integer
                        .parseInt(maxCharsProperty);
            } else {
                // null - use default value
                LOGGER
                        .info("value for maxStatusInformationDetailsChars not found in properties file. using default value.");
                maxStatusInformationDetailsChars = DEFAULT_MAX_CHAR_SIZE;
            }
        } catch (NumberFormatException e) {
            // error! use default value
            LOGGER
                    .info("Got incorrect value for maxStatusInformationDetailsChars from properties file. using default value.");
            maxStatusInformationDetailsChars = DEFAULT_MAX_CHAR_SIZE;
        }
    }

    /**
     * This method gets applied filter from stateController and combines it with
     * existing filter, to get final filter.
     * 
     * @param leftFilter
     * @return
     */
    private Filter getFinalFilter(Filter leftFilter) {
        Filter finalFilter;
        String currentHostFilter = stateController.getCurrentHostFilter();
        // check if stateController return empty string means no filter. if yes,
        // final filter is left filter.
        if (Constant.EMPTY_STRING.equals(currentHostFilter)) {
            finalFilter = leftFilter;
        } else {
            // ..otherwise combine two of 'em to get final filter
            FilterComputer filterComputer = new FilterComputer();
            Filter rightFilter = filterComputer
                    .getHostFilter(currentHostFilter);
            finalFilter = Filter.AND(leftFilter, rightFilter);
        }
        return finalFilter;
    }

    /**
     * Method to get Current Data table bean object.
     * 
     * @return ServiceListDataBean
     */
    public HostListDataBean getCurrentDataTableBean() {
        return dataTableBean;
    }

    /**
     * This method initializes the pagination related values in dataTableBean,
     * in case of filter application.
     */
    public void initializePage() {
        // This logger statement is gives false impression ,logger statement
        // should be in jms push (message method).
        // reset values here!
        // LOGGER.info("Re-initializing service List in initializePage()");
        dataTableBean.setLastPage(null);
        dataTableBean.setLastStartRow(-1);
        dataTableBean.setPage(null);
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {
        // re-initialize the bean so as to reload UI
        if (dataTableBean == null) {
            dataTableBean = new HostListDataBean(hostListPageSize);
        }
        dataTableBean.setError(false);
        dataTableBean.setInfo(false);
        dataTableBean.setMessage(false);

        doDashboardProcessing();

        initializePage();
    }

    /**
     * Sets the dataTableBean.
     * 
     * @param dataTableBean
     *            the dataTableBean to set
     */
    public void setDataTableBean(HostListDataBean dataTableBean) {
        this.dataTableBean = dataTableBean;
    }

    /**
     * Returns the dataTableBean.
     * 
     * @return the dataTableBean
     */
    public HostListDataBean getDataTableBean() {
        return dataTableBean;
    }

    /**
     * Hidden field for IPC handling, that causes rendering of portlet on change
     * of filter.his method call twice on render .so removed web service call
     * from this method for bug GWMON 7045.
     * 
     * @return String
     */
    public String getHostListHiddenField() {
        String newHostFilter = stateController.getCurrentHostFilter();
        if (!(NodeType.NETWORK == selectedNodeType)) {
            // compute the final filter
            Filter finalFilter = getFinalFilter(dataTableBean
                    .getPrimaryFilter());
            dataTableBean.setFinalFilter(finalFilter);
        }
        if (isIntervalRender()
                || !newHostFilter.equalsIgnoreCase(oldHostFilter)) {
            // copy new filter to old one
            this.oldHostFilter = newHostFilter;
            // initialize data page
            initializePage();

            setIntervalRender(false);
        }
        return this.hostListHiddenField;
    }

    /**
     * Hidden field for IPC handling, that causes rendering of portlet on change
     * of filter
     * 
     * @param hiddenFieldValue
     */
    public void setHostListHiddenField(String hiddenFieldValue) {
        this.hostListHiddenField = hiddenFieldValue;
    }

    /**
     * Method called when "acknowledge" link is clicked in Host List table
     * 
     * @param event
     */
    public void showAcknowledgementPopup(ActionEvent event) {
        // Get parameters for acknowledging
        String hostName = (String) event.getComponent().getAttributes().get(
                PARAM_HOST_NAME);
        String userName = FacesUtils.getLoggedInUser();

        // Set parameters in acknowledge popup bean
        AcknowledgePopupBean acknowledgePopupBean = (AcknowledgePopupBean) FacesUtils
                .getManagedBean(Constant.ACKNOWLEDGE_POPUP_MANAGED_BEAN);
        if (acknowledgePopupBean == null) {
            LOGGER
                    .debug("setAcknowledgeParameters(): Cannot retrieve acknowledgement pop up bean");
            return;
        }
        // Indicates this is acknowledge command for service
        acknowledgePopupBean.setHostAck(true);
        acknowledgePopupBean.setHostName(hostName);
        acknowledgePopupBean.setAuthor(userName);
        acknowledgePopupBean.setUserName(userName);

        // set if in dashboard or in status viewer
        boolean inDashbord = PortletUtils.isInDashbord();
        acknowledgePopupBean.setInStatusViewer(!inDashbord);
        if (inDashbord) {
            acknowledgePopupBean
                    .setPopupStyle(Constant.ACK_POPUP_DASHBOARD_STYLE);
        }

        // Set pop-up visible
        acknowledgePopupBean.setVisible(true);
    }

    // JMS PUSH
    /**
     * This is callback method for JMS push. It will be called whenever there is
     * update from JMS topic.
     * 
     * * @see
     * com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh
     * (java.lang.String)
     */
    @Override
    public void refresh(String xmlTopic) {

        // if (xmlTopic == null) {
        // LOGGER
        // .debug("refresh() of HostList Portlet : Received null XML Message.");
        // return;
        // }
        //
        // /*
        // * Get the JMS updates for xmlMessage & particular nodeType [For
        // * comments-portlet will be only HOST or SERVICE].
        // *
        // * Update messages each indicating - action, id , node-type.
        // */
        // List<JMSUpdate> jmsUpdates = JMSUtils.getJMSUpdatesListFromXML(
        // xmlTopic, selectedNodeType);
        // if (jmsUpdates == null || jmsUpdates.isEmpty()) {
        // // no JMS Updates for selectedNodeType - return from here
        // return;
        // }
        //
        // for (JMSUpdate update : jmsUpdates) {
        // if (update != null) {
        // /*
        // * If the nodeId matches with the enitiyID from jmsUpdates list,
        // * then only reload the data.
        // */
        // if (update.getId() == selectedNodeId) {
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug("Refresh HostList data for HostGroup ["
        // + selectedNodeName + "]");
        // }
        // /*
        // * set the hidden field - so that IceFaces will re-render
        // * all things.
        // */
        // setHostListHiddenField(Constant.EMPTY_STRING);
        //
        // // initialize data page
        // initializePage();
        //
        // // Initiate server side rendering to update portlet.
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
     * Sets the hostListNavHiddenField.
     * 
     * @param hostListNavHiddenField
     *            the hostListNavHiddenField to set
     */
    public void setHostListNavHiddenField(String hostListNavHiddenField) {
        this.hostListNavHiddenField = hostListNavHiddenField;
    }

    /**
     * Returns the hostListNavHiddenField.
     * 
     * @return the hostListNavHiddenField
     */
    public String getHostListNavHiddenField() {
        if (isIntervalRender()) {
            return hostListNavHiddenField;
        }
        if (subpageIntegrator.isInStatusViewer()) {
            // fetch the latest nav params
            subpageIntegrator.setNavigationParameters();
            // check for node type and node Id
            int nodeID = subpageIntegrator.getNodeID();
            NodeType nodeType = subpageIntegrator.getNodeType();
            if (nodeID != selectedNodeId || !nodeType.equals(selectedNodeType)) {
                // update node type vals
                selectedNodeType = nodeType;
                selectedNodeName = subpageIntegrator.getNodeName();
                selectedNodeId = nodeID;
                setIntervalRender(true);
                dataTableBean.setSelectedNodeType(selectedNodeType);
                // update state-controller
                stateController.update(selectedNodeType, selectedNodeName,
                        selectedNodeId);

                // force paginator to go to first page

                UIViewRoot viewRoot = FacesUtils.getFacesContext()
                        .getViewRoot();

                DataPaginator paginator = (DataPaginator) viewRoot
                        .findComponent(HGVFORM_HOST_LIST_PAGER_BOTTOM);

                if (paginator != null) {
                    paginator.gotoFirstPage();
                }

                // host list specific logic
                // get primary filter
                Filter primaryFilter = getPrimaryFilter();
                dataTableBean.setPrimaryFilter(primaryFilter);

                this.getHostListHiddenField();
            }
        }
        return hostListNavHiddenField;
    }

    /**
     * Sets the hostListFrmID.
     * 
     * @param hostListFrmID
     *            the hostListFrmID to set
     */
    public void setHostListFrmID(String hostListFrmID) {
        this.hostListFrmID = hostListFrmID;
    }

    /**
     * Returns the hostListFrmID.
     * 
     * @return the hostListFrmID
     */
    public String getHostListFrmID() {
        return hostListFrmID;
    }

}
