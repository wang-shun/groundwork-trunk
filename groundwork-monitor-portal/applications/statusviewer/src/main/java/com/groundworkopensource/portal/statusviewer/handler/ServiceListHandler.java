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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.faces.component.UIViewRoot;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.SimpleHost;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.bean.AcknowledgePopupBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.bean.ServiceListDataBean;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.FilterComputer;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.icesoft.faces.component.datapaginator.DataPaginator;

/**
 * This class provides necessary information and methods to display Service List
 * under Service groups and Host sub-pages. This class fetches data on-the-fly,
 * by using on demand pagination.
 * 
 * @author mridu_narang
 * 
 */
public class ServiceListHandler extends ServerPush implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -9192654079660371285L;

    /**
     * SGVFORM_SLEVENTS_PAGER_BOTTOM : id of paginator in service group view
     * service list portlet
     */
    private static final String SGVFORM_SLEVENTS_PAGER_BOTTOM = "SGVform:SLeventsPagerBottom";

    /**
     * HVFORM_SLEVENTS_PAGER_BOTTOM : id of paginator in host view service list
     * portlet
     */
    private static final String HVFORM_SLEVENTS_PAGER_BOTTOM = "HVform:SLeventsPagerBottom";

    /**
     * service data table instance variable
     */
    private ServiceListDataBean dataTableBean;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(ServiceListHandler.class.getName());

    /**
     * Constant field indicating the maximum number of results i.e. rows
     * displayed in the page
     */
    private int maximumRowsPerPage = DEFAULT_SERVICE_LIST_PAGE_SIZE;

    /**
     * FoundationWSFacade instance
     */
    private final FoundationWSFacade foundFacade = new FoundationWSFacade();

    // Constants

    /**
     * Constant DEFAULT_HOST_LUST_PAGE_SIZE
     */
    private static final int DEFAULT_SERVICE_LIST_PAGE_SIZE = 6;

    /**
     * Constant DEFAULT_MAX_CHAR_SIZE
     */
    private static final int DEFAULT_MAX_CHAR_SIZE = 100;

    /**
     * Constant to retrieve service list page size from status viewer properties
     * file
     */
    private static final String PORTAL_STATUSVIEWER_SERVICE_LIST_PAGE_SIZE = "portal.statusviewer.serviceListPageSize";

    /**
     * Constant to retrieve maximum characters limit for displaying status
     * information details
     */
    private static final String PORTAL_STATUSVIEWER_MAX_STATUS_INFORMATION_DETAILS_CHARS = "portal.statusviewer.maxStatusInformationDetailsChars";

    // UI Specific Fields

    /**
     * User Name
     */
    private String userName;

    /**
     * Parameter for service name
     */
    private static final String PARAM_SERVICE_NAME = "serviceName";

    /**
     * Parameter for host name
     */
    private static final String PARAM_HOST_NAME = "hostName";

    /**
     * Service List Page Size. This property will be read from properties file.
     * if not present, it will take default page size 6.
     */
    private int serviceListPageSize;

    /**
     * Max Status Information Details characters. This property will be read
     * from properties file. if not present, it will take default size 10.
     */
    private int maxStatusInformationDetailsChars;

    /**
     * stateController instance variable
     */
    private StateController stateController = null;

    /**
     * SubpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

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
     * Flag for "Show Non OK Services" filter.
     */
    private boolean showNonOkServices = false;

    /**
     * Flag for "Show Non WARNING Services" filter.
     */
    private boolean showNonWarningServices = false;

    /**
     * Flag for "Show Non CRITICAL Services" filter.
     */
    private boolean showNonCriticalServices = false;

    /**
     * Flag for "Show Non CRITICAL scheduled Services" filter.
     */
    private boolean showNonCriticalScheduledServices = false;

    /**
     * Flag for "Show Non CRITICAL unscheduled Services" filter.
     */
    private boolean showNonCriticalUnscheduledServices = false;

    /**
     * Flag for "Show Non UNKNOWN Services" filter.
     */
    private boolean showNonUnknownServices = false;

    /**
     * Flag for "Show Non PENDING Services" filter.
     */
    private boolean showNonPendingServices = false;

    /**
     * Flag for "Show acknowledged Services" filter.
     */
    private boolean showNonAcknowledgedServices = false;

    /**
     * foundationWSFacade Object to call web services.
     */
    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * monitorStatusFilter to be used for retrieving services which are not OK.
     */
    private static final Filter NON_OK_SERVICES_FILTER = new Filter(
            "monitorStatus.name", FilterOperator.NE, "OK");

    /**
     * monitorStatusFilter to be used for retrieving services which are not WARNING.
     */
    private static final Filter NON_WARNING_SERVICES_FILTER = new Filter(
            "monitorStatus.name", FilterOperator.NE, "WARNING");

    /**
     * monitorStatusFilter to be used for retrieving services which are not CRITICAL.
     */
    private static final Filter NON_CRITICAL_SERVICES_FILTER = new Filter(
            "monitorStatus.name", FilterOperator.NE, "CRITICAL");

    /**
     * monitorStatusFilter to be used for retrieving services which are not CRITICAL (scheduled).
     */
    private static final Filter NON_SCHEDULED_CRITICAL_SERVICES_FILTER = new Filter(
            "monitorStatus.name", FilterOperator.NE, "SCHEDULED CRITICAL");

    /**
     * monitorStatusFilter to be used for retrieving services which are not CRITICAL (unscheduled).
     */
    private static final Filter NON_UNSCHEDULED_CRITICAL_SERVICES_FILTER = new Filter(
            "monitorStatus.name", FilterOperator.NE, "UNSCHEDULED CRITICAL");

    /**
     * monitorStatusFilter to be used for retrieving services which are not UNKNOWN.
     */
    private static final Filter NON_UNKNOWN_SERVICES_FILTER = new Filter(
            "monitorStatus.name", FilterOperator.NE, "UNKNOWN");

    /**
     * monitorStatusFilter to be used for retrieving services which are not PENDING.
     */
    private static final Filter NON_PENDING_SERVICES_FILTER = new Filter(
            "monitorStatus.name", FilterOperator.NE, "PENDING");

    /**
     * ReferenceTreeMetaModel instance
     * <p>
     * !!!!!!!!!!! IMP !!!!!!!!!! : Please do not remove below declaration of
     * referenceTreeModel.
     */
    private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            .getManagedBean(Constant.REFERENCE_TREE);

    /**
     * serviceListHiddenField
     */
    private String serviceListHiddenField;

    /**
     * old filter string, used to compare and detect filter change.
     */
    private String oldServiceFilter = Constant.EMPTY_STRING;

    /**
     * serviceListNavHiddenField
     */
    private String serviceListNavHiddenField;

    /**
     * UserRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Id for form UI component
     */
    private String serviceListFrmID;

    /**
     * Default constructor
     */
    public ServiceListHandler() {
        // Use instance of state controller to retrieve node id and node type
        // ID of current Service-Group or Host depending on sub page
        // do the subpage integration
        subpageIntegrator = new SubpageIntegrator();

        int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
        serviceListFrmID = "servicelistPortlet_frmServiceList" + randomID;

        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        boolean isSubpageIntegration = handleSubpageIntegration();
        if (!isSubpageIntegration) {
            return;
        }

        // read properties required for this portlet
        setValuesFromPropertyFile();
        this.dataTableBean = new ServiceListDataBean(serviceListPageSize);
        this.dataTableBean
                .setMaxStatusInformationDetailsChars(this.maxStatusInformationDetailsChars);

        // call initialization method which does all the work of getting
        // services as per the NodeType
        try {
            if (dataTableBean != null) {
                selectServiceFilter();
            }
        } catch (WSDataUnavailableException e) {
            this.dataTableBean
                    .handleError(
                            "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                            "ServiceListHandler(): Failed to initialize service list handler. Web Service Data Unavailable."
                                    + e.getMessage());
            return;
        } catch (GWPortalException e) {
            this.dataTableBean
                    .handleError(
                            "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                            "ServiceListHandler(): Failed to initialize service list handler."
                                    + e.getMessage());
            return;
        } catch (Exception ex) {
            this.dataTableBean
                    .handleError(
                            "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                            "ServiceListHandler(): Failed to initialize service list handler."
                                    + ex.getMessage());
            return;
        }
    }

    /**
     * Initialization of services list based on type of sub page
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    private void selectServiceFilter() throws WSDataUnavailableException,
            GWPortalException {
        if (null == selectedNodeType) {
            LOGGER
                    .warn("Service List Portlet is not applicable for Node Type ["
                            + this.selectedNodeType + "]");
            return;
        }

        // re-initialize the bean so as to reload UI
        this.dataTableBean.setError(false);
        this.dataTableBean.setInfo(false);
        this.dataTableBean.setMessage(false);

        // get filters as per the node type
        Filter primaryFilter = null;

        /*
         * if in the dashboard, retrieve the ID from HostName / ServiceGroup
         * Name / HostGroup Name passed as preference. Assign this Id to the
         * selectedNodeId
         */

        switch (selectedNodeType) {
            case HOST:
                if (!inStatusViewer) {
                    // validate if entered Host exists
                    try {
                        SimpleHost hostByName = foundationWSFacade
                                .getSimpleHostByName(selectedNodeName, false);
                        if (null == hostByName) {
                            throw new WSDataUnavailableException();
                        }
                        /*
                         * set the selected node Id here (seems weird but
                         * required for JMS Push in Dashboard)
                         */
                        selectedNodeId = hostByName.getHostID();

                        // check for extended role permissions
                        if (!referenceTreeModel
                                .checkNodeForExtendedRolePermissions(
                                        selectedNodeId, NodeType.HOST,
                                        selectedNodeName, userExtendedRoleBean
                                                .getExtRoleHostGroupList(),
                                        userExtendedRoleBean
                                                .getExtRoleServiceGroupList())) {
                            String inadequatePermissionsMessage = ResourceUtils
                                    .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                    + " [" + selectedNodeName + "]";
                            dataTableBean
                                    .handleInfo(inadequatePermissionsMessage);
                            return;
                        }
                    } catch (WSDataUnavailableException e) {
                        String hostNotAvailableErrorMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_hostUnavailable")
                                + " [" + selectedNodeName + "]";
                        dataTableBean.handleInfo(hostNotAvailableErrorMessage);
                        return;
                    } catch (GWPortalGenericException e) {
                        LOGGER
                                .warn(
                                        "Exception while retrieving Host By Name in Dashboard. JMS PUSH may not work. Exception ["
                                                + e.getMessage() + "]", e);
                    }
                } // end of if (!inStatusViewer)

                // LOGGER.debug("Service List to be displayed in Host Subpage");
                // Get filter for particular host id
                primaryFilter = getFilterForHost();
                break;

            case SERVICE_GROUP:
                if (!inStatusViewer) {
                    // validate if entered Service Group exists
                    try {
                        Category category = foundationWSFacade
                                .getCategoryByName(selectedNodeName);
                        if (null == category) {
                            // show appropriate error message to the user
                            String serviceGroupNotAvailableErrorMessage = ResourceUtils
                                    .getLocalizedMessage("com_groundwork_portal_statusviewer_serviceGroupUnavailable")
                                    + " [" + selectedNodeName + "]";
                            LOGGER.error(serviceGroupNotAvailableErrorMessage);
                            this.dataTableBean
                                    .handleInfo(serviceGroupNotAvailableErrorMessage);
                            return;
                        }
                        // set the selected node Id
                        selectedNodeId = category.getCategoryId();

                        // check for extended role permissions
                        if (!userExtendedRoleBean.getExtRoleServiceGroupList()
                                .isEmpty()
                                && !referenceTreeModel
                                        .checkNodeForExtendedRolePermissions(
                                                selectedNodeId,
                                                NodeType.SERVICE_GROUP,
                                                selectedNodeName,
                                                userExtendedRoleBean
                                                        .getExtRoleHostGroupList(),
                                                userExtendedRoleBean
                                                        .getExtRoleServiceGroupList())) {
                            String inadequatePermissionsMessage = ResourceUtils
                                    .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                    + " [" + selectedNodeName + "]";
                            dataTableBean
                                    .handleInfo(inadequatePermissionsMessage);
                            return;
                        }
                    } catch (GWPortalGenericException e) {
                        LOGGER
                                .warn(
                                        "Exception while retrieving Service Group By Name in Dashboard. JMS PUSH may not work. Exception ["
                                                + e.getMessage() + "]", e);
                    }
                } // end of if (!inStatusViewer)
                // LOGGER
                // .debug("Service List to be displayed in Service Group Subpage"
                // );
                // Get filter for particular service group id
                primaryFilter = getFilterForServiceGroup();
                break;

            case HOST_GROUP:
                try {
                    HostGroup hostGroupByName = foundationWSFacade
                            .getHostGroupsByName(selectedNodeName);
                    // Get filter for particular host group
                    primaryFilter = getFilterForHostGroup(hostGroupByName);
                    selectedNodeId = hostGroupByName.getHostGroupID();

                    // check for extended role permissions
                    if (!userExtendedRoleBean.getExtRoleHostGroupList()
                            .isEmpty()
                            && !referenceTreeModel
                                    .checkNodeForExtendedRolePermissions(
                                            selectedNodeId,
                                            NodeType.HOST_GROUP,
                                            selectedNodeName,
                                            userExtendedRoleBean
                                                    .getExtRoleHostGroupList(),
                                            userExtendedRoleBean
                                                    .getExtRoleServiceGroupList())) {
                        String inadequatePermissionsMessage = ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                + " [" + selectedNodeName + "]";
                        dataTableBean.handleInfo(inadequatePermissionsMessage);
                        return;
                    }
                } catch (WSDataUnavailableException e) {
                    String hostGroupNotAvailableErrorMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_hostGroupUnavailable")
                            + " [" + selectedNodeName + "]";
                    LOGGER.error(hostGroupNotAvailableErrorMessage);
                    this.dataTableBean
                            .handleInfo(hostGroupNotAvailableErrorMessage);
                    return;

                } catch (GWPortalGenericException e) {
                    LOGGER
                            .warn(
                                    "Exception while retrieving Host Group By Name. JMS PUSH may not work. Exception ["
                                            + e.getMessage() + "]", e);
                }

                break;

            case NETWORK:
                if (!inStatusViewer) {
                    // check for extended role permissions
                    if (!userExtendedRoleBean.getExtRoleHostGroupList()
                            .isEmpty()
                            || !userExtendedRoleBean
                                    .getExtRoleServiceGroupList().isEmpty()) {
                    	// Get filter for entire network
                        primaryFilter = getFilterForEntireNetwork(true);
                        break;
                    }
                    // Get filter for entire network
                    primaryFilter = getFilterForEntireNetwork(false);
                } // end of if (!inStatusViewer)
                break;

            default:
                break;
        }

        // set the primary filter
        if (null != primaryFilter) {
            this.dataTableBean.setPrimaryFilter(primaryFilter);
            /*
             * for default sorting by monitor status: Default Host and Service
             * List Portlets behavior should be configured as
             * "Order by MonitorStatus" and
             * "the services that are NOT OK should come up first"
             */
            this.dataTableBean.setAscending(false);
            this.dataTableBean
                    .setSortColumnName(FilterConstants.SERVICE_MONITOR_STATUS);

        }

    }

    /**
     * Handles the subpage integration: Reads parameters from request in case of
     * Status Viewer. If portlet is in dashboard, reads preferences.
     */
    private boolean handleSubpageIntegration() {
        // check if portlet is placed in SV
        inStatusViewer = PortletUtils.isInStatusViewer();

        // do subpage integration
        subpageIntegrator.doSubpageIntegration(null);
        // get the StateController
        stateController = subpageIntegrator.getStateController();

        if (inStatusViewer) {
            // get the required data from SubpageIntegrator
            selectedNodeType = subpageIntegrator.getNodeType();
            selectedNodeId = subpageIntegrator.getNodeID();
            selectedNodeName = subpageIntegrator.getNodeName();
        } else {
            boolean checkNodeNamePref = true;
            // For dashboard, initialize NodeType, NodeName parameters
            // Node Type
            selectedNodeType = NodeType.HOST;
            try {
                String nodeTypePreference = FacesUtils
                        .getPreference(Constant.NODE_TYPE_PREF);
                if (null == nodeTypePreference
                        || nodeTypePreference.equals(Constant.EMPTY_STRING)) {
                    throw new PreferencesException();
                }
                if (NodeType.NETWORK.getTypeName().equals(nodeTypePreference)) {
                    // Entire Network
                    selectedNodeType = NodeType.NETWORK;

                } else if (NodeType.SERVICE_GROUP.getTypeName().equals(
                        nodeTypePreference)) {
                    // Service Group
                    selectedNodeType = NodeType.SERVICE_GROUP;

                } else if (NodeType.HOST_GROUP.getTypeName().equals(
                        nodeTypePreference)) {
                    // Host Group
                    selectedNodeType = NodeType.HOST_GROUP;
                }
            } catch (PreferencesException e) {
                List<String> extRoleHostGroupList = userExtendedRoleBean
                        .getExtRoleHostGroupList();
                List<String> extRoleServiceGroupList = userExtendedRoleBean
                        .getExtRoleServiceGroupList();
                if (extRoleHostGroupList.isEmpty()
                        && extRoleServiceGroupList.isEmpty()) {
                    selectedNodeType = NodeType.NETWORK;
                } else if (!extRoleHostGroupList.isEmpty()
                        && !extRoleHostGroupList
                                .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                    selectedNodeType = NodeType.HOST_GROUP;
                    selectedNodeName = userExtendedRoleBean
                            .getDefaultHostGroup();
                    checkNodeNamePref = false;
                } else if (!extRoleServiceGroupList.isEmpty()
                        && !extRoleServiceGroupList
                                .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                    selectedNodeType = NodeType.SERVICE_GROUP;
                    selectedNodeName = userExtendedRoleBean
                            .getDefaultServiceGroup();
                    checkNodeNamePref = false;
                }
            }

            // check for 'node name' filter
            if (checkNodeNamePref && !selectedNodeType.equals(NodeType.NETWORK)) {
                // Node Name
                try {
                    selectedNodeName = FacesUtils
                            .getPreference(Constant.NODE_NAME_PREF);
                    if (null == selectedNodeName
                            || selectedNodeName.equals(Constant.EMPTY_STRING)) {
                        throw new PreferencesException();
                    }
                } catch (PreferencesException e1) {
                    // creating ServiceListDataBean instance to show error
                    // message.
                    this.dataTableBean = new ServiceListDataBean(0);
                    dataTableBean.handleInfo(new PreferencesException()
                            .getMessage());
                    return false;
                }
            }
            // check for 'show Non OK services' filter
            try {
                // "Show Non OK Services" filter.
                String showNonOkServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_OK_PREF);
                if (null == showNonOkServicesPreference
                        || showNonOkServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    // JIRA GWMON-9074
                    showNonOkServices = true;
                } else {
                    showNonOkServices = Boolean
                            .parseBoolean(showNonOkServicesPreference);
                }
                // "Show Non WARNING Services" filter.
                String showNonWarningServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_WARNING_PREF);
                if (null == showNonWarningServicesPreference
                        || showNonWarningServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    // 
                    showNonWarningServices = false;
                } else {
                    showNonWarningServices = Boolean
                            .parseBoolean(showNonWarningServicesPreference);
                }
                // "Show Non CRITICAL Services" filter.
                String showNonCriticalServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_CRITICAL_PREF);
                if (null == showNonCriticalServicesPreference
                        || showNonCriticalServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    //
                    showNonCriticalServices = false;
                } else {
                    showNonCriticalServices = Boolean
                            .parseBoolean(showNonCriticalServicesPreference);
                }
                // "Show Non CRITICAL scheduled Services" filter.
                String showNonCriticalScheduledServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_CRITICAL_SCHEDULED_PREF);
                if (null == showNonCriticalScheduledServicesPreference
                        || showNonCriticalScheduledServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    //
                    showNonCriticalScheduledServices = false;
                } else {
                    showNonCriticalScheduledServices = Boolean
                            .parseBoolean(showNonCriticalScheduledServicesPreference);
                }
                // "Show Non CRITICAL unscheduled Services" filter.
                String showNonCriticalUnscheduledServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_CRITICAL_UNSCHEDULED_PREF);
                if (null == showNonCriticalUnscheduledServicesPreference
                        || showNonCriticalUnscheduledServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    //
                    showNonCriticalUnscheduledServices = false;
                } else {
                    showNonCriticalUnscheduledServices = Boolean
                            .parseBoolean(showNonCriticalUnscheduledServicesPreference);
                }
                // "Show Non UNKNOWN Services" filter.
                String showNonUnknownServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_UNKNOWN_PREF);
                if (null == showNonUnknownServicesPreference
                        || showNonUnknownServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    //
                    showNonUnknownServices = false;
                } else {
                    showNonUnknownServices = Boolean
                            .parseBoolean(showNonUnknownServicesPreference);
                }
                // "Show Non PENDING Services" filter.
                String showNonPendingServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_PENDING_PREF);
                if (null == showNonPendingServicesPreference
                        || showNonPendingServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    //
                    showNonPendingServices = false;
                } else {
                    showNonPendingServices = Boolean
                            .parseBoolean(showNonPendingServicesPreference);
                }
                // "Show Non acknowledged Services" filter.
                String showNonAcknowledgedServicesPreference = FacesUtils
                        .getPreference(Constant.SERVICE_FILTER_ACKNOWLEDGED_PREF);
                if (null == showNonAcknowledgedServicesPreference
                        || showNonAcknowledgedServicesPreference
                                .equals(Constant.EMPTY_STRING)) {
                    //
                    showNonAcknowledgedServices = false;
                } else {
                    showNonAcknowledgedServices = Boolean
                            .parseBoolean(showNonAcknowledgedServicesPreference);
                }
            } catch (PreferencesException e) {
                /*
                 * ignore the exception. For SERVICE_FILTER_OK_PREF, set
                 * it to 'true' if not available.
                 */
                // JIRA GWMON-9074
                showNonOkServices = true;
                showNonWarningServices = false;
                showNonCriticalServices = false;
                showNonCriticalScheduledServices = false;
                showNonCriticalUnscheduledServices = false;
                showNonUnknownServices = false;
                showNonPendingServices = false;
                showNonAcknowledgedServices = false;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Service List Portlet] # Node Type ["
                    + selectedNodeType + "] # Node Name [" + selectedNodeName
                    + "] # Node ID [" + selectedNodeId + "]");
        }
        return true;
    }

    /**
     * Returns Filter for the condition:
     * "All services under the entire network.."
     * 
     * @throws GWPortalException
     */
    private Filter getFilterForEntireNetwork(boolean partialAccess) throws GWPortalException {
        // get all services from RTMM
        Iterator<NetworkMetaEntity> allServices = null;
        if (partialAccess)
        	allServices = referenceTreeModel .getAllowedServicesList();
        else
        	allServices = referenceTreeModel.getAllServices();
        // Logic to generate comma separated list of the result of service-id's
        StringBuilder serviceListStringBuilder = new StringBuilder();
        while (null != allServices && allServices.hasNext()) {
            // build comma separated string of Service IDs in entire network
            NetworkMetaEntity metaEntity = allServices.next();
            if (null != metaEntity) {
                serviceListStringBuilder.append(metaEntity.getObjectId()
                        + Constant.COMMA);
            }
        }
        String serviceIdList = serviceListStringBuilder.toString();
        // create the filter out of this comma separated services string
        Filter leftFilter;
        try {
            leftFilter = new Filter(FilterConstants.SERVICE_STATUS_ID,
                    FilterOperator.IN, serviceIdList);
        } catch (IllegalArgumentException e) {
            LOGGER
                    .error("Illegal arguments passed to filter for services under entire network in getFilterForEntireNetwork()");
            throw new GWPortalException();
        }

        // filter for just showing services which are not OK
        if (showNonOkServices) {
        	leftFilter = Filter.AND(leftFilter, NON_OK_SERVICES_FILTER);
        }
        // filter for just showing services which are not WARNING
        if (showNonWarningServices) {
        	leftFilter = Filter.AND(leftFilter, NON_WARNING_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL
        if (showNonCriticalServices) {
            leftFilter = Filter.AND(leftFilter, NON_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL scheduled
        if (showNonCriticalScheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_SCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL unscheduled
        if (showNonCriticalUnscheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNSCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not UNKNOWN
        if (showNonUnknownServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNKNOWN_SERVICES_FILTER);
        }
        // filter for just showing services which are not PENDING
        if (showNonPendingServices) {
            leftFilter = Filter.AND(leftFilter, NON_PENDING_SERVICES_FILTER);
        }
        return leftFilter;
    }

    
    
    /**
     * Returns Filter for the condition:
     * "All services under this service group.."
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    private Filter getFilterForServiceGroup()
            throws WSDataUnavailableException, GWPortalException {
        CategoryEntity[] entities = this.foundFacade
                .getCategoryEntities(this.selectedNodeName);
        // Logic to generate comma separated list of the result of object-id's
        StringBuilder categoryStringBuilder = new StringBuilder();
        for (CategoryEntity categoryEntity : entities) {
            // build comma separated string of Service IDs in service group
            categoryStringBuilder.append(categoryEntity.getObjectID()
                    + Constant.COMMA);
        }
        String objectIdList = categoryStringBuilder.toString();

        Filter leftFilter;
        try {
            leftFilter = new Filter(FilterConstants.SERVICE_STATUS_ID,
                    FilterOperator.IN, objectIdList);
        } catch (IllegalArgumentException e) {
            LOGGER
                    .error("Illegal arguments passed to filter for services under service group in getFilterForServiceGroup()");
            throw new GWPortalException();
        }

        // filter for just showing services which are not OK
        if (showNonOkServices) {
        	leftFilter = Filter.AND(leftFilter, NON_OK_SERVICES_FILTER);
        }
        // filter for just showing services which are not WARNING
        if (showNonWarningServices) {
        	leftFilter = Filter.AND(leftFilter, NON_WARNING_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL
        if (showNonCriticalServices) {
            leftFilter = Filter.AND(leftFilter, NON_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL scheduled
        if (showNonCriticalScheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_SCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL unscheduled
        if (showNonCriticalUnscheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNSCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not UNKNOWN
        if (showNonUnknownServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNKNOWN_SERVICES_FILTER);
        }
        // filter for just showing services which are not PENDING
        if (showNonPendingServices) {
            leftFilter = Filter.AND(leftFilter, NON_PENDING_SERVICES_FILTER);
        }
        return leftFilter;
    }

    /**
     * Returns Filter for the condition: "All services under this host.."
     */
    private Filter getFilterForHost() {
        // Filter leftFilter = new Filter(FilterConstants.HOST_HOSTID,
        // FilterOperator.EQ, this.selectedNodeId);
        Filter leftFilter = new Filter(FilterConstants.HOST_HOSTNAME,
                FilterOperator.EQ, this.selectedNodeName);
        
        // filter for just showing services which are not OK
        if (showNonOkServices) {
        	leftFilter = Filter.AND(leftFilter, NON_OK_SERVICES_FILTER);
        }
        // filter for just showing services which are not WARNING
        if (showNonWarningServices) {
        	leftFilter = Filter.AND(leftFilter, NON_WARNING_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL
        if (showNonCriticalServices) {
            leftFilter = Filter.AND(leftFilter, NON_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL scheduled
        if (showNonCriticalScheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_SCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL unscheduled
        if (showNonCriticalUnscheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNSCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not UNKNOWN
        if (showNonUnknownServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNKNOWN_SERVICES_FILTER);
        }
        // filter for just showing services which are not PENDING
        if (showNonPendingServices) {
            leftFilter = Filter.AND(leftFilter, NON_PENDING_SERVICES_FILTER);
        }
        return leftFilter;
    }

    /**
     * 
     * Returns Filter for the condition: "All services under this host group.."
     * 
     * @param hostGroup
     * @return filter for services under a host group
     */
    private Filter getFilterForHostGroup(HostGroup hostGroup)
            throws GWPortalException {
        Host[] hosts = hostGroup.getHosts();
        // Logic to generate comma separated list of the host-id's
        StringBuilder hostIdStringBuilder = new StringBuilder();
        for (Host host : hosts) {
            // build comma separated string of Host IDs in host group
            hostIdStringBuilder.append(host.getHostID() + Constant.COMMA);
        }
        String hostIdList = hostIdStringBuilder.toString();

        // create the filter with these hostIds
        Filter leftFilter;
        try {
            leftFilter = new Filter(FilterConstants.HOST_HOSTID,
                    FilterOperator.IN, hostIdList);
        } catch (IllegalArgumentException e) {
            LOGGER
                    .error("Illegal arguments passed to filter for services under host group in getFilterForHostGroup()");
            throw new GWPortalException();
        }

        // filter for just showing services which are not OK
        if (showNonOkServices) {
        	leftFilter = Filter.AND(leftFilter, NON_OK_SERVICES_FILTER);
        }
        // filter for just showing services which are not WARNING
        if (showNonWarningServices) {
        	leftFilter = Filter.AND(leftFilter, NON_WARNING_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL
        if (showNonCriticalServices) {
            leftFilter = Filter.AND(leftFilter, NON_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL scheduled
        if (showNonCriticalScheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_SCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not CRITICAL unscheduled
        if (showNonCriticalUnscheduledServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNSCHEDULED_CRITICAL_SERVICES_FILTER);
        }
        // filter for just showing services which are not UNKNOWN
        if (showNonUnknownServices) {
            leftFilter = Filter.AND(leftFilter, NON_UNKNOWN_SERVICES_FILTER);
        }
        // filter for just showing services which are not PENDING
        if (showNonPendingServices) {
            leftFilter = Filter.AND(leftFilter, NON_PENDING_SERVICES_FILTER);
        }
        return leftFilter;
    }

    /**
     * This method gets applied filter from stateController and combines it with
     * existing filter, to get final filter.
     * 
     * @param leftFilter
     * @param newServiceFilter
     * @return Filter : Final filter
     */
    private Filter getFinalFilter(Filter leftFilter, String newServiceFilter) {
        Filter finalFilter;
        // check if stateController return empty string means no filter. if yes,
        // final filter is left filter.
        if (Constant.EMPTY_STRING.equals(newServiceFilter)) {
            finalFilter = leftFilter;
        } else {
            // ..otherwise combine two of 'em to get final filter
            FilterComputer filterComputer = new FilterComputer();
            Filter rightFilter = filterComputer
                    .getServiceFilter(newServiceFilter);
            finalFilter = Filter.AND(leftFilter, rightFilter);
        }
        return finalFilter;
    }

    /**
     * Sets values from property file. if not present, default values will be
     * used.
     */
    private void setValuesFromPropertyFile() {

        // get Page List Size
        boolean serviceListPageSizeObtained = false;
        if (!inStatusViewer) {
            try {
                String servicesPerPagePreference = FacesUtils
                        .getPreference(Constant.SERVICES_PER_PAGE_PREF);
                if (null != servicesPerPagePreference
                        && !servicesPerPagePreference.trim().equals(
                                Constant.EMPTY_STRING)) {
                    this.serviceListPageSize = Integer
                            .parseInt(servicesPerPagePreference);
                    serviceListPageSizeObtained = true;
                }
            } catch (PreferencesException e) {
                serviceListPageSizeObtained = false;
            } catch (NumberFormatException e) {
                serviceListPageSizeObtained = false;
            }
        }

        if (!serviceListPageSizeObtained) {
            String pageSizeProperty = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    PORTAL_STATUSVIEWER_SERVICE_LIST_PAGE_SIZE);
            try {
                if (pageSizeProperty != null) {
                    this.serviceListPageSize = Integer
                            .parseInt(pageSizeProperty);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER
                                .debug("Got service list page size from properties file: "
                                        + this.serviceListPageSize);
                    }
                } else {
                    // null - use default value
                    LOGGER
                            .info("value for serviceListPageSize not found in properties file. using default value.");
                    this.serviceListPageSize = DEFAULT_SERVICE_LIST_PAGE_SIZE;
                }
            } catch (NumberFormatException e) {
                // Error - use default value
                LOGGER
                        .info("Got incorrect value for serviceListPageSize from properties file. using default value.");
                this.serviceListPageSize = DEFAULT_SERVICE_LIST_PAGE_SIZE;
            }
        }

        // get Maximum Characters Property
        String maxCharsProperty = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                PORTAL_STATUSVIEWER_MAX_STATUS_INFORMATION_DETAILS_CHARS);
        try {
            if (maxCharsProperty != null) {
                this.maxStatusInformationDetailsChars = Integer
                        .parseInt(maxCharsProperty);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER
                            .debug("Got maxStatusInformationDetailsChars from properties file: "
                                    + this.maxStatusInformationDetailsChars);
                }
            } else {
                // null - use default value
                LOGGER
                        .info("value for maxStatusInformationDetailsChars not found in properties file. using default value.");
                this.maxStatusInformationDetailsChars = DEFAULT_MAX_CHAR_SIZE;
            }
        } catch (NumberFormatException e) {
            // Error - use default value
            LOGGER
                    .info("Got incorrect value for maxStatusInformationDetailsChars from properties file. using default value.");
            this.maxStatusInformationDetailsChars = DEFAULT_MAX_CHAR_SIZE;
        }
    }

    /**
     * Method to get Current Data table bean object.
     * 
     * @return ServiceListDataBean
     */
    public ServiceListDataBean getCurrentDataTableBean() {
        return this.dataTableBean;
    }

    /**
     * This method initializes the pagination related values in dataTableBean,
     * in case of filter application.
     */
    public void initializePage() {
        // This logger statement is gives false impression ,logger statement
        // should be in jms push (message method).
        // LOGGER.error("Re-initializing service List in initializePage()");
        // reset values here!

        this.dataTableBean.setLastPage(null);
        this.dataTableBean.setLastStartRow(-1);
        this.dataTableBean.setPage(null);
    }

    // GETTERS & SETTERS

    /**
     * Returns the maximumRowsPerPage.
     * 
     * @return the maximumRowsPerPage
     */
    public int getMaximumRowsPerPage() {
        this.maximumRowsPerPage = this.serviceListPageSize;
        return this.maximumRowsPerPage;
    }

    /**
     * Sets the maximumRowsPerPage.
     * 
     * @param maximumRowsPerPage
     *            the maximumRowsPerPage to set
     */
    public void setMaximumRowsPerPage(int maximumRowsPerPage) {
        this.maximumRowsPerPage = maximumRowsPerPage;
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {

        if (dataTableBean == null) {
            this.dataTableBean = new ServiceListDataBean(serviceListPageSize);
        }
        // re-initialize the bean so as to reload UI
        this.dataTableBean.setError(false);
        this.dataTableBean.setInfo(false);
        this.dataTableBean.setMessage(false);
        this.dataTableBean
                .setMaxStatusInformationDetailsChars(maxStatusInformationDetailsChars);
        initializePage();
        try {
            selectServiceFilter();
        } catch (WSDataUnavailableException e) {
            this.dataTableBean
                    .handleError(
                            "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                            "reloadPage(): selectServiceFilter() failed to retrieve service list. Web Services Data Unavailable. "
                                    + e.getMessage());
            return;
        } catch (GWPortalException e) {
            this.dataTableBean
                    .handleError(
                            "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                            "reloadPage(): selectServiceFilter() failed to retrieve service list. "
                                    + e.getMessage());
            return;
        }

    }

    /**
     * Sets the dataTableBean.
     * 
     * @param dataTableBean
     *            the dataTableBean to set
     */
    public void setDataTableBean(ServiceListDataBean dataTableBean) {
        this.dataTableBean = dataTableBean;
    }

    /**
     * Returns the dataTableBean.
     * 
     * @return the dataTableBean
     */
    public ServiceListDataBean getDataTableBean() {
        return this.dataTableBean;
    }

    /**
     * Hidden field for IPC handling, that causes rendering of portlet on change
     * of filter.This method call twice on render .so removed web service call
     * from this method for bug GWMON 7045.
     * 
     * @return String
     */
    public String getServiceListHiddenField() {

        String newServiceFilter = stateController.getCurrentServiceFilter();
        Filter finalFilter = getFinalFilter(dataTableBean.getPrimaryFilter(),
                newServiceFilter);

        // set the external filter to dataTableBean only if new filter is
        // different than old one.

        // TODO Need to check if filter is Time Filter. If yes, then create
        // filter again.
        // compute the final filter

        this.dataTableBean.setFinalFilter(finalFilter);
        this.dataTableBean.setShowNonAcknowledged(showNonAcknowledgedServices);
        if (isIntervalRender()
                || !newServiceFilter.equalsIgnoreCase(oldServiceFilter)) {
            LOGGER
                    .debug(" in getServiceListiddenField() in SERVICELIST .............");

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Applying new filter to Service List: "
                        + newServiceFilter + " in getCurrentDataTableBean()");
            }
            // copy new filter to old one
            this.oldServiceFilter = newServiceFilter;

            // initialize page
            initializePage();
            setIntervalRender(false);
        }
        return this.serviceListHiddenField;
    }

    /**
     * Sets the serviceListNavHiddenField.
     * 
     * @param serviceListNavHiddenField
     *            the serviceListNavHiddenField to set
     */
    public void setServiceListNavHiddenField(String serviceListNavHiddenField) {
        this.serviceListNavHiddenField = serviceListNavHiddenField;
    }

    /**
     * Returns the serviceListNavHiddenField.
     * 
     * @return the serviceListNavHiddenField
     */
    public String getServiceListNavHiddenField() {
        if (isIntervalRender()) {
            return serviceListNavHiddenField;
        }

        if (inStatusViewer) {
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

                // update state-controller
                stateController.update(selectedNodeType, selectedNodeName,
                        selectedNodeId);

                // force paginator to go to first page

                UIViewRoot viewRoot = FacesUtils.getFacesContext()
                        .getViewRoot();

                DataPaginator paginator = null;
                if (selectedNodeType == NodeType.HOST) {
                    paginator = (DataPaginator) viewRoot
                            .findComponent(HVFORM_SLEVENTS_PAGER_BOTTOM);
                } else if (selectedNodeType == NodeType.SERVICE_GROUP) {
                    paginator = (DataPaginator) viewRoot
                            .findComponent(SGVFORM_SLEVENTS_PAGER_BOTTOM);
                }

                if (paginator != null) {
                    paginator.gotoFirstPage();
                }

                // call initialization method which does all the work of getting
                // services as per the NodeType
                try {
                    if (dataTableBean != null) {
                        selectServiceFilter();
                    }
                } catch (WSDataUnavailableException e) {
                    this.dataTableBean
                            .handleError(
                                    "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                                    "ServiceListHandler(): Failed to initialize service list handler. Web Service Data Unavailable."
                                            + e.getMessage());
                    return serviceListHiddenField;
                } catch (GWPortalException e) {
                    this.dataTableBean
                            .handleError(
                                    "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                                    "ServiceListHandler(): Failed to initialize service list handler."
                                            + e.getMessage());
                    return serviceListHiddenField;
                } catch (Exception ex) {
                    this.dataTableBean
                            .handleError(
                                    "com_groundwork_portal_statusviewer_serviceListPortlet_error",
                                    "ServiceListHandler(): Failed to initialize service list handler."
                                            + ex.getMessage());
                    return serviceListHiddenField;
                }
                setIntervalRender(true);

                this.getServiceListHiddenField();
            }
        }

        return serviceListNavHiddenField;
    }

    /**
     * Method used to navigate to acknowledgment pop up page
     * 
     * @param event
     */
    public void showAcknowledgementPopup(ActionEvent event) {

        String hostName = (String) event.getComponent().getAttributes().get(
                PARAM_HOST_NAME);
        String serviceName = (String) event.getComponent().getAttributes().get(
                PARAM_SERVICE_NAME);
        String author = FacesUtils.getLoggedInUser();
        setAcknowledgeParameters(hostName, serviceName, author);

    }

    /**
     * Method to set acknowledgment bean
     */
    private void setAcknowledgeParameters(String hostName, String serviceName,
            String author) {

        AcknowledgePopupBean popupBean = (AcknowledgePopupBean) FacesUtils
                .getManagedBean(Constant.ACKNOWLEDGE_POPUP_MANAGED_BEAN);

        if (popupBean == null) {
            LOGGER
                    .debug("setAcknowledgeParameters(): Cannot retrieve acknowledgement pop up bean");
            return;
        }
        // Indicates this is ack fro service
        popupBean.setHostAck(false);
        popupBean.setHostName(hostName);
        popupBean.setServiceDescription(serviceName);
        popupBean.setAuthor(author);
        popupBean.setUserName(author);

        // set if in dashboard or in status viewer
        boolean inDashbord = PortletUtils.isInDashbord();
        popupBean.setInStatusViewer(!inDashbord);
        if (inDashbord) {
            popupBean.setPopupStyle(Constant.ACK_POPUP_DASHBOARD_STYLE);
        }

        // Set pop-up visible
        popupBean.setVisible(true);
    }

    /**
     * Hidden field for IPC handling, that causes rendering of portlet on change
     * of filter
     * 
     * @param hiddenFieldValue
     */
    public void setServiceListHiddenField(String hiddenFieldValue) {
        this.serviceListHiddenField = hiddenFieldValue;
    }

    /**
     * Sets the userName.
     * 
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the userName.
     * 
     * @return the userName
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Returns the selectedNodeId.
     * 
     * @return the selectedNodeId
     */
    public int getSelectedNodeId() {
        return this.selectedNodeId;
    }

    /**
     * Sets the selectedNodeId.
     * 
     * @param selectedNodeId
     *            the selectedNodeId to set
     */
    public void setSelectedNodeId(int selectedNodeId) {
        this.selectedNodeId = selectedNodeId;
    }

    // JMS PUSH
    /**
     * This is callback method for JMS push. It will be called whenever there is
     * update from JMS topic.
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String topicXML) {
        // if (topicXML == null) {
        // LOGGER.warn("refresh(): Received null XML Message.");
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
        // topicXML, selectedNodeType);
        // if (jmsUpdates == null) {
        // LOGGER
        // .warn(
        // "refresh(): Received null JMS Updates using JMSUtils.getJMSUpdatesListFromXML() utility method"
        // );
        // return;
        // }
        //
        // // iterate through the received updates
        // for (JMSUpdate update : jmsUpdates) {
        // if (update != null) {
        // // LOGGER.debug("Service List Portlet JMS PUSH : "
        // // + update.toString());
        // /*
        // * If the nodeId matches with the enitiyID from jmsUpdates
        // * list,then only reload the data.
        // */
        // if (update.getId() == selectedNodeId) {
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug("Refresh ServiceList data for "
        // + selectedNodeType + " [" + selectedNodeName
        // + "]");
        // }
        // /*
        // * set the hidden field - so that IceFaces will re-render
        // * all things.
        // */
        // setServiceListHiddenField(Constant.EMPTY_STRING);
        //
        // // initialize page
        // initializePage();
        //
        // /* Initiate server side rendering to update portlet. */
        // SessionRenderer.render(groupRenderName);
        //
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
     * Sets the serviceListFrmID.
     * 
     * @param serviceListFrmID
     *            the serviceListFrmID to set
     */
    public void setServiceListFrmID(String serviceListFrmID) {
        this.serviceListFrmID = serviceListFrmID;
    }

    /**
     * Returns the serviceListFrmID.
     * 
     * @return the serviceListFrmID
     */
    public String getServiceListFrmID() {
        return serviceListFrmID;
    }

}
