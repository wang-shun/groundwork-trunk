package com.groundworkopensource.portal.statusviewer.handler;

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

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.HostGroupHealthBean;
import com.groundworkopensource.portal.statusviewer.bean.HostHealthBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.bean.ServiceGroupHealthBean;
import com.groundworkopensource.portal.statusviewer.bean.ServiceHealthBean;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.bean.UserRoleBean;
import com.groundworkopensource.portal.statusviewer.common.CommonUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.DateProperty;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.StringProperty;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.portlet.PortletPreferences;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Handler for all health portlet. I.e. host, host group, service, service group
 * 
 * @author nitin_jadhav
 */
public class HealthPortletsHandler extends ServerPush implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4117444986737947585L;

    /**
     * PARENT
     */
    private static final String PARENT = "Parent";

    /**
     * SERVICE_DEPENDENCIES
     */
    // private static final String SERVICE_DEPENDENCIES = "ServiceDependencies";
    /**
     * UNAVAILABLE
     */
    public static final String UNAVAILABLE = "Unavailable";

    /**
     * PORTAL_STATUSVIEWER_HOST_LIST_PAGE_SIZE constant
     */
    private static final String POPUP_PAGE_SIZE = "portal.statusviewer.healthPortletPageSize";

    /**
     * Constant DEFAULT_HOST_LUST_PAGE_SIZE
     */
    private static final int DEFAULT_PAGE_SIZE = 6;

    /**
     * HostList Page Size. This property will be read from properties file. if
     * not present, it will take default page size 6.
     */
    private int pageSize;

    /**
     * LOGGER
     */
    private static final Logger LOGGER = Logger
            .getLogger(HealthPortletsHandler.class.getName());

    /**
     * Instance of WebServiceFactory
     */
    private static final WebServiceFactory WEB_SERVICE_FACTORY = new WebServiceFactory();

    /**
     * foundationWSFacade instance
     */
    private IWSFacade foundationWSFacade = WEB_SERVICE_FACTORY
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * Host Health Bean.
     */
    private HostHealthBean hostHealthBean;

    /**
     * Service Group Health Bean.
     */
    private ServiceGroupHealthBean serviceGroupHealthBean;

    /**
     * Service Health Bean.
     */
    private ServiceHealthBean serviceHealthBean;

    /**
     * Host Group Health Bean.
     */
    private HostGroupHealthBean hostGroupHealthBean;

    /**
     * current node type
     */
    private NodeType selectedNodeType;

    /**
     * current node Id
     */
    private int selectedNodeId;

    /**
     * selectedNodeName
     */
    private String selectedNodeName = Constant.EMPTY_STRING;

    /**
     * selected node application type name
     */
    private String selectedNodeApplicationType;

    /**
     * Flag to identify if portlet is placed in StatusViewer sub-pages apart
     * from Network View.
     */
    private boolean inStatusViewer;

    /**
     * Error boolean to set if error occurred
     */
    private boolean error = false;

    /**
     * info boolean to set if information occurred
     */
    private boolean info = false;

    /**
     * boolean variable message set true when display any type of messages
     * (Error,info or warning) in UI
     */
    private boolean message = false;

    /**
     * information message to show on UI
     */
    private String infoMessage;

    /**
     * Error message to show on UI
     */
    private String errorMessage;

    /**
     * boolean variable to used close and open group popup window. This is for
     * both Host/Service Portlets.
     */
    private boolean groupPopupVisible = false;

    /**
     * boolean variable to used close and open parent & Dependent popup window.
     * This is for both Host/Service Portlets.
     */
    private boolean parentDependentPopupVisible = false;

    /**
     * boolean variable to used close and open parent & Dependent pop up window.
     * This is for both Host/Service Portlets.
     */
    private boolean popupVisible = false;

    /**
     * Host (Foundation) object for host
     */
    private Host host;

    /**
     * healthHiddenField
     */
    private String healthHiddenField;

    /**
     * ReferenceTreeMetaModel instance
     */
    private ReferenceTreeMetaModel referenceTreeModel;

    /**
     * SubpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

    /**
     * Variable which decides if user in Admin or Operator role.
     */
    private boolean userInAdminOrOperatorRole;

    /**
     * MAX_LABEL_LENGTH after which truncated label will be shown on screen
     */
    private static final int MAX_LABEL_LENGTH = 25;

    /**
     * date time pattern
     */
    private String dateTimePattern;

    /**
     * Notes for the Host
     */
    private static final String NOTES = "Notes";

    /**
     * preferences Keys Map to be used for reading preferences.
     */
    private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
    static {
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.SERVICE_NAME,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.SERVICE_GROUP_NAME,
                NodeType.SERVICE_GROUP);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.HOST_NAME, NodeType.HOST);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.HOST_GROUP_NAME,
                NodeType.HOST_GROUP);

        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_PREF,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_GROUP_PREF,
                NodeType.SERVICE_GROUP);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_PREF,
                NodeType.HOST);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_GROUP_PREF,
                NodeType.HOST_GROUP);
        PREFERENCE_KEYS_MAP.put(
                Constant.PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE,
                NodeType.HOST_GROUP);
        PREFERENCE_KEYS_MAP.put(
                Constant.PORTLET_XML_DEFAULT_SERVICEGROUP_PREFERENCE,
                NodeType.SERVICE_GROUP);
    }

    /**
     * facesContext
     */
    private FacesContext facesContext;

    /**
     * UserExtendedRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Default Constructor.
     */
    public HealthPortletsHandler() {
        hostHealthBean = new HostHealthBean();
        serviceHealthBean = new ServiceHealthBean();
        serviceGroupHealthBean = new ServiceGroupHealthBean();
        hostGroupHealthBean = new HostGroupHealthBean();

        subpageIntegrator = new SubpageIntegrator();

        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                .getManagedBean(Constant.REFERENCE_TREE);
        // set values from property file
        setValuesFromPropertyFile();

        try {
            // initialize the faces context to be used in JMS thread
            facesContext = FacesContext.getCurrentInstance();

            handleSubpageIntegration();
            UserRoleBean userRoleBean = (UserRoleBean) FacesUtils
                    .getManagedBean("userRoleBean");
            // read the user role from portlet request
            setUserInAdminOrOperatorRole(userRoleBean
                    .isUserInAdminOrOperatorRole());
        } catch (Exception e) {
            handleError(e.getMessage());
        }

        try {
            dateTimePattern = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.STATUS_VIEWER_DATETIME_PATTERN);
        } catch (Exception e) {
            // Ignore exception
            dateTimePattern = Constant.DEFAULT_DATETIME_PATTERN;
        }
    }

    /**
     * Sets values from property file. if not present, default values will be
     * used.
     */
    private void setValuesFromPropertyFile() {
        // get pageSize and dateFormatProperty from application
        // property files.
        String pageSizeProperty = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, POPUP_PAGE_SIZE);
        try {
            if (pageSizeProperty != null) {
                setPageSize(Integer.parseInt(pageSizeProperty));
                // LOGGER.debug("Got pageSize from properties file: "
                // + getPageSize() + "in setValuesFromPropertyFile()");
                if (getPageSize() == 0) {
                    setPageSize(DEFAULT_PAGE_SIZE);
                }
            } else {
                // null - use default value
                setPageSize(DEFAULT_PAGE_SIZE);
            }
        } catch (NumberFormatException e) {
            // error! use default value
            // logged a warning so that user can set proper pagesize in property
            // file.
            LOGGER
                    .info("Got incorrect value for popup pageSize from properties file. using default value for pageSize : "
                            + DEFAULT_PAGE_SIZE);
            setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * Handles subpage integration.
     */
    private void handleSubpageIntegration() {
        boolean isPrefSet = subpageIntegrator
                .doSubpageIntegration(PREFERENCE_KEYS_MAP);
        if (!isPrefSet) {
            /*
             * as this portlet is not applicable for "Network View", show the
             * error message to user. If it was in the "Network View", then we
             * would have to assign Node Type as NETWORK with NodeId as 0.
             */
            handleInfo(new PreferencesException().getMessage());
            return;
        }
        // get the required data from SubpageIntegrator
        selectedNodeType = subpageIntegrator.getNodeType();
        selectedNodeId = subpageIntegrator.getNodeID();
        selectedNodeName = subpageIntegrator.getNodeName();
        inStatusViewer = subpageIntegrator.isInStatusViewer();

        // check node type for dashboard
        if (!inStatusViewer && selectedNodeType.equals(NodeType.NETWORK)) {
            // As Health Portlets are not applicable for 'Network', need to
            // determine exact Node Type - HG OR SG
            try {
                PortletPreferences allPreferences = FacesUtils
                        .getAllPreferences();
                String pref = allPreferences.getValue(
                        Constant.PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE,
                        Constant.EMPTY_STRING);
                if (pref != null && !pref.equals(Constant.EMPTY_STRING)) {
                    selectedNodeType = NodeType.HOST_GROUP;
                } else {
                    selectedNodeType = NodeType.SERVICE_GROUP;
                }
            } catch (PreferencesException e) {
                // ignore
            }
        }
        // LOGGER.debug("[Health Portlets] # Node Type [" + selectedNodeType
        // + "] # Node Name [" + selectedNodeName + "] # Node ID ["
        // + selectedNodeId + "] # In Status Viewer [" + inStatusViewer
        // + "]");
    }

    /**
     * Initialize Handler. CAll this method to reload current health portlet.
     * 
     * @throws GWPortalException
     * @throws PreferencesException
     */
    private void initialize() throws GWPortalException, PreferencesException {
        // if selected node type is still null, then return from here.
        if (null == selectedNodeType) {
            return;
        }

        // re-initialize the handler so as to reload UI
        setError(false);
        setInfo(false);
        setMessage(false);

        // as per node type, initialize the portlets
        switch (selectedNodeType) {
            case HOST:
                // set host health portlet details
                setHostHealthPortletDetails(selectedNodeName);
                break;

            case SERVICE:
                // set service health portlet details
                if (inStatusViewer) {
                    // get service health details using Id
                    setServiceHealthPortletDetails(selectedNodeId);

                } else {
                    if (null != facesContext) {
                        FacesUtils.setFacesContext(facesContext);
                    }

                    // use preferences - host name and service name to get
                    // health details.
                    Map<String, String> servicePortletPreferences = PortletUtils
                            .getServicePortletPreferences();
                    setServiceHealthPortletDetails(servicePortletPreferences
                            .get(PreferenceConstants.HOST_NAME),
                            servicePortletPreferences
                                    .get(PreferenceConstants.SERVICE_NAME));
                }
                break;

            case SERVICE_GROUP:
                // set service group health portlet details
                setServiceGroupHealthPortletDetails(selectedNodeName);
                break;

            case HOST_GROUP:
            default:
                // set host group health portlet details
                setHostGroupHealthPortletDetails(selectedNodeName);
                break;
        }

    }

    /**
     * sets HostGroupHealthPortlet Details
     */
    private void setHostGroupHealthPortletDetails(String hostGroupName) {
        try {
            List<String> extRoleHostGroupList = userExtendedRoleBean
                    .getExtRoleHostGroupList();
            if (null == hostGroupName
                    || hostGroupName.equals(Constant.EMPTY_STRING)) {
                if (extRoleHostGroupList.isEmpty()) {
                    hostGroupName = Constant.DEFAULT_HOST_GROUP_NAME;
                } else if (!extRoleHostGroupList
                        .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                    hostGroupName = userExtendedRoleBean.getDefaultHostGroup();
                }
            } else if (hostGroupName.equals(Constant.HOSTGROUP_NAME_LINUX)) {
                handleInfo(new PreferencesException().getMessage());
                return;
            }
            // Get the Host Group from Name
            HostGroup selectedHostGroup = foundationWSFacade
                    .getHostGroupsByName(hostGroupName);

            // set the selected node Id here (seems weird but required for JMS
            // Push in Dashboard)
            selectedNodeId = selectedHostGroup.getHostGroupID();
            // get application type
            selectedNodeApplicationType = selectedHostGroup.getApplicationName();
            // Get Host Group Name
            String selectedHostGroupName = selectedHostGroup.getName();

            // check for extended role permissions
            if (!extRoleHostGroupList.isEmpty()
                    && !referenceTreeModel.checkNodeForExtendedRolePermissions(
                            selectedNodeId, NodeType.HOST_GROUP,
                            selectedHostGroupName, userExtendedRoleBean
                                    .getExtRoleHostGroupList(),
                            userExtendedRoleBean.getExtRoleServiceGroupList())) {
                String inadequatePermissionsMessage = ResourceUtils
                        .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                        + " [" + selectedHostGroupName + "]";
                handleInfo(inadequatePermissionsMessage);
                return;
            }

            hostGroupHealthBean.setHostGroupName(selectedHostGroupName);
            // set the host group name label
            if (selectedHostGroupName != null) {
                if (selectedHostGroupName.length() > MAX_LABEL_LENGTH) {
                    hostGroupHealthBean
                            .setHostGroupNameLabel(selectedHostGroupName
                                    .substring(0, MAX_LABEL_LENGTH)
                                    + Constant.ELLIPSES);
                } else {
                    hostGroupHealthBean
                            .setHostGroupNameLabel(selectedHostGroupName);
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Retrieve Health details for Host Group ["
                        + selectedHostGroupName + "]");
            }
            // Host Group Alias
            hostGroupHealthBean.setHostGroupAlias(selectedHostGroup.getAlias());

            // set the host group name label
            if (hostGroupHealthBean.getHostGroupAlias() != null) {
                if (hostGroupHealthBean.getHostGroupAlias().length() > MAX_LABEL_LENGTH) {
                    hostGroupHealthBean
                            .setHostGroupAliasLabel(hostGroupHealthBean
                                    .getHostGroupAlias().substring(0,
                                            MAX_LABEL_LENGTH)
                                    + Constant.ELLIPSES);
                } else {
                    hostGroupHealthBean
                            .setHostGroupAliasLabel(hostGroupHealthBean
                                    .getHostGroupAlias());
                }
            }
            // Monitor Status for Host Group
            hostGroupHealthBean.setStatus(MonitorStatusUtilities
                    .getEntityStatus(selectedHostGroup));

            // Set Host and Service Availability values
            hostGroupHealthBean
                    .setHostAvailability(String
                            .valueOf(foundationWSFacade
                                    .getHostAvailabilityForHostgroup(selectedHostGroupName)));
            hostGroupHealthBean
                    .setServiceAvailability(String
                            .valueOf(foundationWSFacade
                                    .getServiceAvailabilityForHostgroup(selectedHostGroupName)));

            // set Host Group description
            String description = selectedHostGroup.getDescription();
            if (null != description) {
                hostGroupHealthBean.setHostGroupDescription(CommonUtils
                        .getWrapString(description, MAX_LABEL_LENGTH));
            } else {
                hostGroupHealthBean
                        .setHostGroupDescription(Constant.EMPTY_STRING);
            }

        } catch (WSDataUnavailableException e) {
            String hostGroupNotAvailableErrorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_hostGroupUnavailable")
                    + " [" + hostGroupName + "]";
            LOGGER.error(hostGroupNotAvailableErrorMessage);
            handleInfo(hostGroupNotAvailableErrorMessage);

        } catch (GWPortalGenericException e) {
            LOGGER.error(e.getMessage());
            handleError(e.getMessage());
        }
    }

    /**
     * SetHost Health values
     * 
     * @param hostName
     */
    private void setHostHealthPortletDetails(String hostName) {
        try {
            hostHealthBean
                    .setUserInAdminOrOperatorRole(userInAdminOrOperatorRole);
            host = foundationWSFacade.getHostsByName(hostName);

            // set the selected node Id here (seems weird but required for JMS
            // Push in Dashboard)
            selectedNodeId = host.getHostID();
            // get application type
            selectedNodeApplicationType = CommonUtils.getApplicationNameByID(host.getApplicationTypeID());
            hostName = host.getName();

            // check for extended role permissions
            if (!referenceTreeModel.checkNodeForExtendedRolePermissions(
                    selectedNodeId, NodeType.HOST, hostName,
                    userExtendedRoleBean.getExtRoleHostGroupList(),
                    userExtendedRoleBean.getExtRoleServiceGroupList())) {
                String inadequatePermissionsMessage = ResourceUtils
                        .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                        + " [" + hostName + "]";
                handleInfo(inadequatePermissionsMessage);
                return;
            }
            // set the host name
            hostHealthBean.setHostName(hostName);

            // set the label
            if (hostName != null) {
                if (hostName.length() > MAX_LABEL_LENGTH) {
                    hostHealthBean.setHostNameLabel(hostName.substring(0,
                            MAX_LABEL_LENGTH)
                            + Constant.ELLIPSES);
                } else {
                    hostHealthBean.setHostNameLabel(hostName);
                }
            }
            // set alias
            String aliasProperty = getProperty(host.getPropertyTypeBinding(),
                    Constant.ALIAS);
            if (aliasProperty != UNAVAILABLE) {
                hostHealthBean.setAlias(aliasProperty);
            }

            hostHealthBean.setStatus(MonitorStatusUtilities.getEntityStatus(
                    host, NodeType.HOST));
            PropertyTypeBinding propertyTypeBinding = host
                    .getPropertyTypeBinding();
            if (propertyTypeBinding != null) {
                String durationString = "";
                String lastStateChangeDate = "";
                // Duration for which this host is in current state
                DateProperty lastStateChangeProperty = propertyTypeBinding
                        .getDateProperty(CommonConstants.LAST_STATE_CHANGE);
                if (lastStateChangeProperty != null
                        && lastStateChangeProperty.getValue() != null) {
                    Date lastStateChange = lastStateChangeProperty.getValue();
                    // convert date to the format in which it needs to be
                    // displayed
                    lastStateChangeDate = DateUtils.format(lastStateChange,
                            dateTimePattern);
                    // compute duration
                    durationString = DateUtils.computeDuration(lastStateChange);
                }
                hostHealthBean.setLastStateChangeDate(lastStateChangeDate);
                hostHealthBean.setHostStateDuration(durationString);

                // set parents count
                String property = getProperty(propertyTypeBinding, PARENT);
                if (property.equals(Constant.EMPTY_STRING)
                        || property.equals(UNAVAILABLE)) {
                    hostHealthBean.setParentsCount(0);
                } else {
                    StringTokenizer tokenizer = new StringTokenizer(property,
                            Constant.COMMA);
                    hostHealthBean.setParentsCount(tokenizer.countTokens());
                }

                // set Notes for the Host
                String hostNotes = Constant.EMPTY_STRING;
                StringProperty notesProperty = propertyTypeBinding
                        .getStringProperty(NOTES);
                if (null != notesProperty && notesProperty.getValue() != null) {
                    hostNotes = CommonUtils.getWrapString(notesProperty
                            .getValue(), MAX_LABEL_LENGTH);
                }
                hostHealthBean.setHostNotes(hostNotes);
            }

            // set groups count for this host
            List<NetworkMetaEntity> hostGroups = referenceTreeModel
                    .getHostGroupListForHost(selectedNodeId);
            if (hostGroups != null) {
                hostHealthBean.setHostGroupsCount(hostGroups.size());
            }
        } catch (WSDataUnavailableException e) {
            String hostNotAvailableErrorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_hostUnavailable")
                    + " [" + hostName + "]";
            LOGGER.error(hostNotAvailableErrorMessage);
            handleInfo(hostNotAvailableErrorMessage);

        } catch (GWPortalGenericException e) {
            LOGGER
                    .error("Error occured while accessing web services for initializing Host Health Portlet in setHostHealthPortletDetails().");
            handleError(e.getMessage());
        }
    }

    /**
     * Returns Host List by accepting comma separated list of host IDs
     * 
     * @param property
     * @return List
     */
    private List<NetworkMetaEntity> getHostListFromCommaString(String property) {

        List<NetworkMetaEntity> list = new ArrayList<NetworkMetaEntity>();
        if (property == null || UNAVAILABLE.equalsIgnoreCase(property)) {
            return list;
        }
        StringTokenizer tokenizer = new StringTokenizer(property,
                Constant.COMMA);
        while (tokenizer.hasMoreElements()) {
            String hostName = tokenizer.nextToken();
            try {
                SimpleHost hostItem = foundationWSFacade.getSimpleHostByName(
                        hostName, false);
                // Host hostItem = getHostsByName(hostName);

                if (hostItem != null) {
                    list.add(new NetworkMetaEntity(hostItem.getHostID(), null,
                            hostItem.getName(), null, MonitorStatusUtilities
                                    .getEntityStatus(hostItem, NodeType.HOST),
                            NodeType.HOST, null, null, null));
                }
            } catch (NumberFormatException e) {
                LOGGER
                        .error(
                                "Error getting parent having name "
                                        + hostName
                                        + " for host are unavailable in getHostListFromCommaString()",
                                e);
            } catch (GWPortalGenericException ge) {
                LOGGER
                        .error(
                                "Error getting parent having name "
                                        + hostName
                                        + " for host are unavailable in getHostListFromCommaString()",
                                ge);
            }
        }
        return list;
    }

    /**
     * sets ServiceGroupHealthPortlet Details
     * 
     * @param categoryName
     */
    private void setServiceGroupHealthPortletDetails(String categoryName) {
        try {
            List<String> extRoleServiceGroupList = userExtendedRoleBean
                    .getExtRoleServiceGroupList();
            if (null == categoryName
                    || categoryName.equals(Constant.EMPTY_STRING)) {
                if (userExtendedRoleBean.getDefaultServiceGroup() == null) {
                    handleInfo(new PreferencesException().getMessage());
                    return;
                }
                if (extRoleServiceGroupList.isEmpty()
                        || !extRoleServiceGroupList
                                .contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)) {
                    categoryName = userExtendedRoleBean
                            .getDefaultServiceGroup();
                }
            }

            Category category = foundationWSFacade
                    .getCategoryByName(categoryName);

            if (category != null) {
                // set the selected node Id here (seems weird but required for
                // JMS Push in Dashboard)
                selectedNodeId = category.getCategoryId();
                // get application type, (access to service group application
                // not supported via WS so access via RTMM)
                NetworkMetaEntity serviceGroupNetworkEntity = referenceTreeModel.getServiceGroupById(selectedNodeId);
                selectedNodeApplicationType = ((serviceGroupNetworkEntity != null) ? serviceGroupNetworkEntity.getAppType() : null);
                String sgName = category.getName();

                // check for extended role permissions
                if (!userExtendedRoleBean.getExtRoleServiceGroupList()
                        .isEmpty()
                        && !referenceTreeModel
                                .checkNodeForExtendedRolePermissions(
                                        selectedNodeId, NodeType.SERVICE_GROUP,
                                        sgName, userExtendedRoleBean
                                                .getExtRoleHostGroupList(),
                                        userExtendedRoleBean
                                                .getExtRoleServiceGroupList())) {
                    String inadequatePermissionsMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                            + " [" + sgName + "]";
                    handleInfo(inadequatePermissionsMessage);
                    return;
                }

                serviceGroupHealthBean.setServiceGroupName(sgName);
                // set the host group name label
                if (sgName != null) {
                    if (sgName.length() > MAX_LABEL_LENGTH) {
                        serviceGroupHealthBean.setServiceGroupNameLabel(sgName
                                .substring(0, MAX_LABEL_LENGTH)
                                + Constant.ELLIPSES);
                    } else {
                        serviceGroupHealthBean.setServiceGroupNameLabel(sgName);
                    }
                }

                serviceGroupHealthBean.setStatus(MonitorStatusUtilities
                        .getEntityStatus(category));
                double serviceAvailability = foundationWSFacade
                        .getServiceAvailabilityForServiceGroup(category
                                .getName());
                serviceGroupHealthBean.setServiceUptime(String
                        .valueOf(serviceAvailability));

                // set Service Group description
                String description = category.getDescription();
                if (null != description) {
                    serviceGroupHealthBean
                            .setServiceGroupDescription(CommonUtils
                                    .getWrapString(description,
                                            MAX_LABEL_LENGTH));
                } else {
                    serviceGroupHealthBean
                            .setServiceGroupDescription(Constant.EMPTY_STRING);
                }

            } else {
                // show appropriate error message to the user
                String serviceGroupNotAvailableErrorMessage = ResourceUtils
                        .getLocalizedMessage("com_groundwork_portal_statusviewer_serviceGroupUnavailable")
                        + " [" + categoryName + "]";
                LOGGER.error(serviceGroupNotAvailableErrorMessage);
                handleInfo(serviceGroupNotAvailableErrorMessage);
            }
        } catch (GWPortalGenericException e) {
            LOGGER
                    .error("Error occured while initializing Service group health portlet in setServiceGroupHealthPortletDetails().");
            handleError(e.getMessage());
        }
    }

    /**
     * sets ServiceHealthPortlet Details => This will be used when portlet is
     * placed in Status Viewer.
     * 
     * @param id
     * @throws GWPortalException
     */
    private void setServiceHealthPortletDetails(int id)
            throws GWPortalException {
        try {
            ServiceStatus serviceStatus = foundationWSFacade
                    .getServicesById(id);
            setServiceHealthDetails(serviceStatus);
        } catch (WSDataUnavailableException e) {
            LOGGER
                    .error("Error occured while initializing Service health portlet in setServiceHealthPortletDetails()");
            handleError(e.getMessage());
        }
    }

    /**
     * sets ServiceHealthPortlet Details => This will be used when portlet is
     * placed in Dashboard.
     * 
     * @param hostName
     * @param serviceName
     * @throws GWPortalException
     */
    private void setServiceHealthPortletDetails(String hostName,
            String serviceName) throws GWPortalException {
        ServiceStatus service = null;
        try {
            service = foundationWSFacade.getServiceByHostAndServiceName(
                    hostName, serviceName);
            // check for extended role permissions
            if (!referenceTreeModel.checkNodeForExtendedRolePermissions(service
                    .getServiceStatusID(), NodeType.SERVICE, serviceName,
                    userExtendedRoleBean.getExtRoleHostGroupList(),
                    userExtendedRoleBean.getExtRoleServiceGroupList())) {
                String inadequatePermissionsMessage = ResourceUtils
                        .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                        + " [" + serviceName + "]";
                handleInfo(inadequatePermissionsMessage);
                return;
            }
            // set service health portlet details
            setServiceHealthDetails(service);
        } catch (WSDataUnavailableException e) {
            String serviceNotAvailableErrorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_serviceUnavailable")
                    + " ["
                    + serviceName
                    + "] "
                    + ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_serviceForHostUnavailable")
                    + " [" + hostName + "] ";
            LOGGER.error(serviceNotAvailableErrorMessage);
            handleInfo(serviceNotAvailableErrorMessage);

        } catch (GWPortalGenericException e) {
            LOGGER
                    .error("Error occured while initializing Service health portlet in setServiceHealthPortletDetails()");
            handleError(e.getMessage());
        }
    }

    /**
     * Sets Service Health Details.
     * 
     * @param serviceStatus
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private void setServiceHealthDetails(ServiceStatus serviceStatus)
            throws WSDataUnavailableException, GWPortalException {

        serviceHealthBean
                .setUserInAdminOrOperatorRole(userInAdminOrOperatorRole);
        if (serviceStatus != null) {
            serviceHealthBean.setServiceName(serviceStatus.getDescription());
            // set the label
            if (serviceHealthBean.getServiceName() != null) {
                if (serviceHealthBean.getServiceName().length() > MAX_LABEL_LENGTH) {
                    serviceHealthBean.setServiceNameLabel(serviceHealthBean
                            .getServiceName().substring(0, MAX_LABEL_LENGTH)
                            + Constant.ELLIPSES);
                } else {
                    serviceHealthBean.setServiceNameLabel(serviceHealthBean
                            .getServiceName());
                }
            }

            serviceHealthBean.setWarningStatus(false);
            int serviceStatusID = serviceStatus.getServiceStatusID();
            NetworkMetaEntity serviceNetworkMetaEntity = referenceTreeModel
                    .getServiceById(serviceStatusID);
            NetworkObjectStatusEnum serviceEntityStatus = NetworkObjectStatusEnum.NO_STATUS;
            if (null != serviceNetworkMetaEntity) {
                // make use of service status from RefrenceTreeMEtaModel over
                // here
                serviceEntityStatus = serviceNetworkMetaEntity.getStatus();
                // NetworkObjectStatusEnum serviceEntityStatus =
                // MonitorStatusUtilities.getEntityStatus(serviceStatus,
                // NodeType.SERVICE);
                serviceHealthBean.setStatus(serviceEntityStatus);
                // check if warning status
                if (NetworkObjectStatusEnum.SERVICE_WARNING
                        .equals(serviceEntityStatus)) {
                    serviceHealthBean.setWarningStatus(true);
                }
            }
            // set the selected node Id here (seems weird but required for JMS
            // Push in Dashboard)
            selectedNodeId = serviceStatusID;
            // get application type
            selectedNodeApplicationType = CommonUtils.getApplicationNameByID(serviceStatus.getApplicationTypeID());

            // set duration
            // Duration for which this host is in current state
            String durationString = Constant.EMPTY_STRING;
            String lastStateChangeString = Constant.EMPTY_STRING;
            if (null != serviceNetworkMetaEntity
                    && serviceNetworkMetaEntity.getLastStateChange() != null) {
                Date lastStateChangeDate = serviceNetworkMetaEntity
                        .getLastStateChange();
                // convert date to the format in which it needs to be
                // displayed
                lastStateChangeString = DateUtils.format(lastStateChangeDate,
                        dateTimePattern);
                durationString = DateUtils.computeDuration(lastStateChangeDate);
            }
            serviceHealthBean.setServiceStateDuration(durationString);
            serviceHealthBean.setLastStateChangeDate(lastStateChangeString);

            Host parentHost = serviceStatus.getHost();
            if (parentHost != null) {
                String hostName = parentHost.getName();
                serviceHealthBean.setHostName(hostName);
                // set URL for the Host
                serviceHealthBean.setHostUrl(NodeURLBuilder.buildNodeURL(
                        NodeType.HOST, parentHost.getHostID(), hostName));
            }

            // set group list
            List<NetworkMetaEntity> serviceGroupListForService = referenceTreeModel
                    .getServiceGroupListForService(serviceStatusID);
            serviceHealthBean.setServiceGroupsCount(serviceGroupListForService
                    .size());

            // set Notes for the Service
            String serviceNotes = Constant.EMPTY_STRING;
            PropertyTypeBinding propertyTypeBinding = serviceStatus
                    .getPropertyTypeBinding();
            if (null != propertyTypeBinding) {
                StringProperty notesProperty = propertyTypeBinding
                        .getStringProperty(NOTES);
                if (null != notesProperty && notesProperty.getValue() != null) {
                    serviceNotes = CommonUtils.getWrapString(notesProperty
                            .getValue(), MAX_LABEL_LENGTH);
                }
            }
            serviceHealthBean.setServiceNotes(serviceNotes);

            // code for setting of dependent count
            /*
             * PropertyTypeBinding propertyTypeBinding = serviceStatus
             * .getPropertyTypeBinding(); if (propertyTypeBinding != null) { //
             * set parents count String property =
             * getProperty(propertyTypeBinding, SERVICE_DEPENDENCIES); if
             * (property.equals(Constant.EMPTY_STRING) ||
             * property.equals(UNAVAILABLE)) {
             * hostHealthBean.setParentsCount(0); } else { StringTokenizer
             * tokenizer = new StringTokenizer(property, Constant.COMMA);
             * hostHealthBean.setParentsCount(tokenizer.countTokens()); } }
             */
        }
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {

        try {
            initialize();
        } catch (GWPortalGenericException e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Handles error : sets error flag and message.
     */
    private void handleError(String errorMessage) {
        setMessage(true);
        setError(true);
        setErrorMessage(errorMessage);
    }

    /**
     * Handles Info : sets Info flag and message.
     */
    private void handleInfo(String infoMessage) {
        setMessage(true);
        setInfo(true);
        setInfoMessage(infoMessage);
    }

    /**
     * Returns the serviceGroupHealthBean.
     * 
     * @return the serviceGroupHealthBean
     */
    public ServiceGroupHealthBean getServiceGroupHealthBean() {
        return serviceGroupHealthBean;
    }

    /**
     * Returns the serviceHealthBean.
     * 
     * @return the serviceHealthBean
     */
    public ServiceHealthBean getServiceHealthBean() {
        return serviceHealthBean;
    }

    /**
     * Called when user clicks on "Groups for this Host"
     * 
     * @param event
     */
    public void showGroupsForHost(ActionEvent event) {
        hostHealthBean.setAscendingForGroup(true);
        hostHealthBean.setSortGroupColumn(null);
        // Groups: fetch Groups for this Host
        List<NetworkMetaEntity> hostGroups = referenceTreeModel
                .getHostGroupListForHost(selectedNodeId);
        hostHealthBean.setGroupList(hostGroups);
    }

    /**
     * Called when user clicks on "Groups for this Host"
     * 
     * @param event
     */
    public void showParentsForHost(ActionEvent event) {
        // LOGGER.debug("Manipulating Parent popups for host " + host.getName()
        // + " with data in showParentsForHost()");
        PropertyTypeBinding propertyTypeBinding = host.getPropertyTypeBinding();
        // Parents: String of comma separated host IDs
        String property = getProperty(propertyTypeBinding, PARENT);
        if (property.equals(Constant.EMPTY_STRING)
                || property.equals(UNAVAILABLE)) {
            hostHealthBean.setParentList(new ArrayList<NetworkMetaEntity>());
        } else {
            List<NetworkMetaEntity> parentList = getHostListFromCommaString(property);
            hostHealthBean.setParentList(parentList);
        }
    }

    /**
     * Method to open service group Popup
     */
    public void openServiceGroupPopup() {
        // set group list
        List<NetworkMetaEntity> serviceGroupListForService = referenceTreeModel
                .getServiceGroupListForService(selectedNodeId);
        serviceHealthBean.setGroupList(serviceGroupListForService);

        setPopupVisible(true);
        setGroupPopupVisible(true);
    }

    /**
     * Method to open group Popup
     */
    public void openGroupPopup() {
        setPopupVisible(true);
        setGroupPopupVisible(true);
    }

    /**
     * Method to open Parent Dependent Popup
     */
    public void openParentDependentPopup() {
        setPopupVisible(true);
        setParentDependentPopupVisible(true);
    }

    /**
     * Method called when close button on UI is called.
     */
    public void closePopup() {
        setPopupVisible(false);
        setParentDependentPopupVisible(false);
        setGroupPopupVisible(false);
    }

    /**
     * Sets the error.
     * 
     * @param error
     *            the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Returns the error.
     * 
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets the errorMessage.
     * 
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the errorMessage.
     * 
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the message.
     * 
     * @param message
     *            the message to set
     */
    public void setMessage(boolean message) {
        this.message = message;
    }

    /**
     * Returns the message.
     * 
     * @return the message
     */
    public boolean isMessage() {
        return message;
    }

    /**
     * Sets the info.
     * 
     * @param info
     *            the info to set
     */
    public void setInfo(boolean info) {
        this.info = info;
    }

    /**
     * Returns the info.
     * 
     * @return the info
     */
    public boolean isInfo() {
        return info;
    }

    /**
     * Sets the infoMessage.
     * 
     * @param infoMessage
     *            the infoMessage to set
     */
    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    /**
     * Returns the infoMessage.
     * 
     * @return the infoMessage
     */
    public String getInfoMessage() {
        return infoMessage;
    }

    /**
     * Sets the groupPopupVisible.
     * 
     * @param groupPopupVisible
     *            the groupPopupVisible to set
     */
    public void setGroupPopupVisible(boolean groupPopupVisible) {
        this.groupPopupVisible = groupPopupVisible;
    }

    /**
     * Returns the groupPopupVisible.
     * 
     * @return the groupPopupVisible
     */
    public boolean isGroupPopupVisible() {
        return groupPopupVisible;
    }

    /**
     * Sets the parentDependentPopupVisible.
     * 
     * @param parentDependentPopupVisible
     *            the parentDependentPopupVisible to set
     */
    public void setParentDependentPopupVisible(
            boolean parentDependentPopupVisible) {
        this.parentDependentPopupVisible = parentDependentPopupVisible;
    }

    /**
     * Returns the parentDependentPopupVisible.
     * 
     * @return the parentDependentPopupVisible
     */
    public boolean isParentDependentPopupVisible() {
        return parentDependentPopupVisible;
    }

    /**
     * Sets the popupVisible.
     * 
     * @param popupVisible
     *            the popupVisible to set
     */
    public void setPopupVisible(boolean popupVisible) {
        this.popupVisible = popupVisible;
    }

    /**
     * Returns the popupVisible.
     * 
     * @return the popupVisible
     */
    public boolean isPopupVisible() {
        return popupVisible;
    }

    /**
     * Returns the hostHealthBean.
     * 
     * @return the hostHealthBean
     */
    public HostHealthBean getHostHealthBean() {
        return hostHealthBean;
    }

    /**
     * Returns property value when supplied binding and property name.
     * 
     * @param propertyTypeBinding
     * @param propertyName
     * @return property Value.
     */
    private String getProperty(PropertyTypeBinding propertyTypeBinding,
            String propertyName) {
        String propValue = UNAVAILABLE;
        if (propertyTypeBinding != null) {
            Object propertyValue = propertyTypeBinding
                    .getPropertyValue(propertyName);
            if (null != propertyValue) {
                propValue = propertyValue.toString();
            }
        }
        return propValue;
    }

    /**
     * Sets the pageSize.
     * 
     * @param pageSize
     *            the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Returns the pageSize.
     * 
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Call back method for JMS
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlTopic) {
        // if (xmlTopic == null) {
        // LOGGER
        // .debug("refresh() of Health Portlets : Received null XML Message.");
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
        // if (jmsUpdates == null) {
        // LOGGER
        // .debug("refresh() of Health Portlets : Received null JMS Updates using JMSUtils.getJMSUpdatesListFromXML() utility method");
        // return;
        // }
        //
        // for (JMSUpdate update : jmsUpdates) {
        // if (update != null) {
        // // LOGGER.debug("#### Health Portlets JMS PUSH #### : "
        // // + update.toString());
        // /*
        // * If the selectedNodeID matches with the enitiyID from
        // * jmsUpdates list,then only reload the data.
        // */
        // if (update.getId() == selectedNodeId) {
        // // Fetch the host transitions and generate the
        // // bar chart.
        //
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER
        // .debug("Pushing health portlets for selectedNodeId : "
        // + selectedNodeId);
        // }
        //
        // try {
        // // initialize the health portlets
        // initialize();
        // // re-render
        // SessionRenderer.render(groupRenderName);
        //
        // /*
        // * Important: break from here - do not iterate on
        // * further updates from JMS as requirement has already
        // * been satisfied with one.
        // */
        // break;
        //
        // } catch (GWPortalGenericException e) {
        // LOGGER
        // .error("GWPortalGenericException while initializing health portlets for selectedNodeId ["
        // + selectedNodeId
        // + "]. Actual Exception : " + e);
        // }
        // } // end of if (update.getId() == selectedNodeId)
        // } // end of if (update != null)
        // } // end of for (JMSUpdate update : jmsUpdates)
    }

    /**
     * Returns the hostGroupHealthBean.
     * 
     * @return the hostGroupHealthBean
     */
    public HostGroupHealthBean getHostGroupHealthBean() {
        return hostGroupHealthBean;
    }

    /**
     * Sets the healthHiddenField.
     * 
     * @param healthHiddenField
     *            the healthHiddenField to set
     */
    public void setHealthHiddenField(String healthHiddenField) {
        this.healthHiddenField = healthHiddenField;
    }

    /**
     * Returns the healthHiddenField.
     * 
     * @return the healthHiddenField
     */
    public String getHealthHiddenField() {
        if (subpageIntegrator.isInStatusViewer() && !isIntervalRender()) {
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
                // subpage - update node type vals
                setIntervalRender(true);

            }
        }
        if (isIntervalRender()) {
            LOGGER.debug("calling hiddenFiled in getHealthHiddenField()");
            try {
                initialize();
            } catch (GWPortalException e) {
                handleError(e.getMessage());
            } catch (PreferencesException e) {
                handleInfo(e.getMessage());
            }
            setIntervalRender(false);
        }
        return this.healthHiddenField;
    }

    /**
     * Sets the userInAdminOrOperatorRole.
     * 
     * @param userInAdminOrOperatorRole
     *            the userInAdminOrOperatorRole to set
     */
    public void setUserInAdminOrOperatorRole(boolean userInAdminOrOperatorRole) {
        this.userInAdminOrOperatorRole = userInAdminOrOperatorRole;
    }

    /**
     * Returns the userInAdminOrOperatorRole.
     * 
     * @return the userInAdminOrOperatorRole
     */
    public boolean isUserInAdminOrOperatorRole() {
        return userInAdminOrOperatorRole;
    }

    // /**
    // * Returns Host List by accepting comma separated list of host IDs
    // *
    // * Temporarily unused
    // *
    // * @param property
    // * @return List
    // */
    /*
     * private List<NetworkMetaEntity> getServiceListFromCommaString( String
     * property) { List<NetworkMetaEntity> list = new
     * ArrayList<NetworkMetaEntity>(); if (property == null ||
     * UNAVAILABLE.equalsIgnoreCase(property)) { return list; } StringTokenizer
     * tokenizer = new StringTokenizer(property, Constant.COMMA); while
     * (tokenizer.hasMoreElements()) { String serviceId = tokenizer.nextToken();
     * try { ServiceStatus serviceItem = foundationWSFacade
     * .getServicesById(Integer.parseInt(serviceId)); if (serviceItem != null) {
     * NetworkObjectStatusEnum status = MonitorStatusUtilities
     * .getEntityStatus(serviceItem); NetworkMetaEntity serviceMetaEntity = new
     * NetworkMetaEntity( serviceItem.getServiceStatusID(), serviceItem
     * .getDescription(), status, NodeType.SERVICE, null, null, null);
     * list.add(serviceMetaEntity); } } catch (NumberFormatException e) { LOGGER
     * .warn("Error getting dependents having Id " + serviceId +
     * " for service are unavailable in getServiceListFromCommaString()"); }
     * catch (GWPortalGenericException ge) { LOGGER
     * .warn("Error getting dependents having Id " + serviceId +
     * " for service are unavailable in getServiceListFromCommaString()"); } }
     * return list; }
     */

    // /**
    // * Called when user clicks on "Dependents for this Service"
    // *
    // * ***Currently unused method
    // *
    // * @param event
    // */
    /*
     * public void showDependentsForService(ActionEvent event) {
     * LOGGER.debug("Manipulating Dependent popups for service " +
     * serviceStatus.getDescription() +
     * " with data in showDependentsForService()"); // set dependents list
     * PropertyTypeBinding propertyTypeBinding = serviceStatus
     * .getPropertyTypeBinding(); // set parents count String property =
     * getProperty(propertyTypeBinding, SERVICE_DEPENDENCIES); if
     * (property.equals(Constant.EMPTY_STRING) || property.equals(UNAVAILABLE))
     * { hostHealthBean.setParentList(new ArrayList<NetworkMetaEntity>()); }
     * else { List<NetworkMetaEntity> dependentsList =
     * getServiceListFromCommaString(property);
     * hostHealthBean.setParentList(dependentsList); } }
     */

    /**
     * Return selected node application type name, (e.g. NAGIOS).
     *
     * @return application type name
     */
    public String getSelectedNodeApplicationType() {
        return selectedNodeApplicationType;
    }
}
