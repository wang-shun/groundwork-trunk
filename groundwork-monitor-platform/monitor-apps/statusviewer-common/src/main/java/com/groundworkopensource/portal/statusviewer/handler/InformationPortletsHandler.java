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

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.FacesUtils;
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
import com.groundworkopensource.portal.statusviewer.bean.InformationBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.bean.UserRoleBean;
import com.groundworkopensource.portal.statusviewer.common.CollagePropertyTypeConstants;
import com.groundworkopensource.portal.statusviewer.common.CommonUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.actions.HostActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ParentMenuActionEnum;
import com.groundworkopensource.portal.statusviewer.common.actions.ServiceActionEnum;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.CheckType;
import org.groundwork.foundation.ws.model.impl.DoubleProperty;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.IntegerProperty;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.StateType;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.portlet.PortletSession;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for Information Portlets Viz. 1) Host Information Portlet 2) Service
 * Information Portlet.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class InformationPortletsHandler extends ServerPush implements
        Serializable {

    /**
     * constant for $SERVICE$
     */
    private static final String SERVICE_TOKEN = "$SERVICE$";

    /**
     * constant for $HOST$
     */
    private static final String HOST_TOKEN = "$HOST$";

    /**
     * TRUNCATE_CUSTOM_HOST_URL after 18 characters
     */
    private static final int TRUNCATE_CUSTOM_HOST_URL = 40;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 661063811458041919L;

    /**
     * Warning Constant.
     */
    private static final String WARNING = "Warning";

    /**
     * notification string constant for CurrentNotificationNumber
     */
    private static final String NOTIFICATION = "notification ";

    /**
     * Constant for N/A
     */
    private static final String NOT_AVAILABLE = "N/A";

    /**
     * logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(InformationPortletsHandler.class.getName());

    /**
     * foundationWSFacade instance
     */
    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

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
     * Host Information Bean.
     */
    private InformationBean hostInformationBean;

    /**
     * Service Information Bean.
     */
    private InformationBean serviceInformationBean;

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
     * Latency Warning should appear if Latency crosses specific threshold.
     * FIXME Use a preference to store threshold value. Bigger than 10 sec
     * should be a warning. Take this from status-viewer.properties.
     */
    private static final int LATENCY_WARNING_THRESHOLD_IN_SECONDS_DEFAULT = 10;

    /**
     * Key for Latency warning threshold.
     * 
     * Host/Service Information Portlets: Latency Warning should appear if
     * Latency crosses specific threshold (in SECONDS). Bigger than 10 seconds
     * should be a warning.
     */
    private static final String LATENCY_WARNING_THRESHOLD_IN_SECONDS_KEY = "portal.statusviewer.latency.warning.threshold";

    /**
     * Key for Host Custom URL 1.
     */
    private static final String CUSTOM_HOST_URL_1_KEY = "portal.statusviewer.host.custom.url.1";
    /**
     * Key for Host Custom URL 2.
     */
    private static final String CUSTOM_HOST_URL_2_KEY = "portal.statusviewer.host.custom.url.2";
    /**
     * Key for Host Custom URL 3.
     */
    private static final String CUSTOM_HOST_URL_3_KEY = "portal.statusviewer.host.custom.url.3";
    /**
     * Key for Host Custom URL 4.
     */
    private static final String CUSTOM_HOST_URL_4_KEY = "portal.statusviewer.host.custom.url.4";
    /**
     * Key for Host Custom URL 5.
     */
    private static final String CUSTOM_HOST_URL_5_KEY = "portal.statusviewer.host.custom.url.5";

    /**
     * Key for Host Custom URL 1 display.name.
     */
    private static final String CUSTOM_HOST_URL_1_DISPLAY_NAME_KEY = "portal.statusviewer.host.custom.url.1.display.name";
    /**
     * Key for Host Custom URL 2 display.name.
     */
    private static final String CUSTOM_HOST_URL_2_DISPLAY_NAME_KEY = "portal.statusviewer.host.custom.url.2.display.name";
    /**
     * Key for Host Custom URL 3 display.name.
     */
    private static final String CUSTOM_HOST_URL_3_DISPLAY_NAME_KEY = "portal.statusviewer.host.custom.url.3.display.name";
    /**
     * Key for Host Custom URL 4 display.name.
     */
    private static final String CUSTOM_HOST_URL_4_DISPLAY_NAME_KEY = "portal.statusviewer.host.custom.url.4.display.name";
    /**
     * Key for Host Custom URL 5 display.name.
     */
    private static final String CUSTOM_HOST_URL_5_DISPLAY_NAME_KEY = "portal.statusviewer.host.custom.url.5.display.name";

    /**
     * Key for Service Custom URL 1.
     */
    private static final String CUSTOM_SERVICE_URL_1_KEY = "portal.statusviewer.service.custom.url.1";
    /**
     * Key for Service Custom URL 2.
     */
    private static final String CUSTOM_SERVICE_URL_2_KEY = "portal.statusviewer.service.custom.url.2";
    /**
     * Key for Service Custom URL 3.
     */
    private static final String CUSTOM_SERVICE_URL_3_KEY = "portal.statusviewer.service.custom.url.3";
    /**
     * Key for Service Custom URL 4.
     */
    private static final String CUSTOM_SERVICE_URL_4_KEY = "portal.statusviewer.service.custom.url.4";
    /**
     * Key for Service Custom URL 5.
     */
    private static final String CUSTOM_SERVICE_URL_5_KEY = "portal.statusviewer.service.custom.url.5";

    /**
     * Key for Service Custom URL 1 display.name.
     */
    private static final String CUSTOM_SERVICE_URL_1_DISPLAY_NAME_KEY = "portal.statusviewer.service.custom.url.1.display.name";
    /**
     * Key for Service Custom URL 2 display.name.
     */
    private static final String CUSTOM_SERVICE_URL_2_DISPLAY_NAME_KEY = "portal.statusviewer.service.custom.url.2.display.name";
    /**
     * Key for Service Custom URL 3 display.name.
     */
    private static final String CUSTOM_SERVICE_URL_3_DISPLAY_NAME_KEY = "portal.statusviewer.service.custom.url.3.display.name";
    /**
     * Key for Service Custom URL 4 display.name.
     */
    private static final String CUSTOM_SERVICE_URL_4_DISPLAY_NAME_KEY = "portal.statusviewer.service.custom.url.4.display.name";
    /**
     * Key for Service Custom URL 5 display.name.
     */
    private static final String CUSTOM_SERVICE_URL_5_DISPLAY_NAME_KEY = "portal.statusviewer.service.custom.url.5.display.name";
    /**
     * subpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

    /**
     * Latency Warning Threshold.
     */
    private int latencyWaringThreshold;

    /**
     * preferences Keys Map to be used for reading preferences.
     */
    private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();

    /**
     * hostCustomURLPrefs
     */
    private static final List<String> HOST_CUSTOM_URL_PREFS = new ArrayList<String>();
    /**
     * serviceCustomURLPrefs
     */
    private static final List<String> SERVICE_CUSTOM_URL_PREFS = new ArrayList<String>();

    /**
     * SV_HOST_CUSTOM_URLS
     */
    private static final List<String> SV_HOST_CUSTOM_URLS = new ArrayList<String>();
    /**
     * SV_SERVICE_CUSTOM_URLS
     */
    private static final List<String> SV_SERVICE_CUSTOM_URLS = new ArrayList<String>();
    /**
     * SV_HOST_CUSTOM_URLS_DISPLAY_NAME
     */
    private static final List<String> SV_HOST_CUSTOM_URLS_DISPLAY_NAME = new ArrayList<String>();
    /**
     * SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME
     */
    private static final List<String> SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME = new ArrayList<String>();

    /**
     * Checktype active
     */
    private static final String CHECKTYPE_ACTIVE = "Active";
    /**
     * ReferenceTreeMetaModel instance
     */
    private ReferenceTreeMetaModel referenceTreeModel;

    static {
        // preferences Keys Map
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.SERVICE_NAME,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.HOST_NAME, NodeType.HOST);

        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_PREF,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_PREF,
                NodeType.HOST);

        // host Custom URL Preferences
        HOST_CUSTOM_URL_PREFS.add(Constant.DEFAULT_HOST_CUST_LINK1_PREF);
        HOST_CUSTOM_URL_PREFS.add(Constant.DEFAULT_HOST_CUST_LINK2_PREF);
        HOST_CUSTOM_URL_PREFS.add(Constant.DEFAULT_HOST_CUST_LINK3_PREF);
        HOST_CUSTOM_URL_PREFS.add(Constant.DEFAULT_HOST_CUST_LINK4_PREF);
        HOST_CUSTOM_URL_PREFS.add(Constant.DEFAULT_HOST_CUST_LINK5_PREF);

        // service Custom URL Preferences
        SERVICE_CUSTOM_URL_PREFS.add(Constant.DEFAULT_SERVICE_CUST_LINK1_PREF);
        SERVICE_CUSTOM_URL_PREFS.add(Constant.DEFAULT_SERVICE_CUST_LINK2_PREF);
        SERVICE_CUSTOM_URL_PREFS.add(Constant.DEFAULT_SERVICE_CUST_LINK3_PREF);
        SERVICE_CUSTOM_URL_PREFS.add(Constant.DEFAULT_SERVICE_CUST_LINK4_PREF);
        SERVICE_CUSTOM_URL_PREFS.add(Constant.DEFAULT_SERVICE_CUST_LINK5_PREF);

        // read custom URLs for Status Viewer
        readCustomURLSFromProperties();
    }

    /**
     * HOST_NAGIOS_LINK
     */
    private static final String HOST_NAGIOS_LINK = "/nagios-app/extinfo.cgi?type=1&host=";
    /**
     * SERVICE_NAGIOS_LINK
     */
    private static final String SERVICE_NAGIOS_LINK = "/nagios-app/extinfo.cgi?type=2&host=";

    /**
     * SERVICE Nagios Link - constant for "&service="
     */
    private static final String SERVICE_NAGIOS_LINK_SERVICE_PART = "&service=";

    /**
     * facesContext
     */
    private FacesContext facesContext;

    /**
     * Hidden Field for information portlets
     */
    private String informationHiddenField = Constant.HIDDEN;

    /**
     * Variable which decides if user in Admin or Operator role. We are fetching
     * user roles just once in constructor and reusing it while PUSH. Reason
     * behind this strategy is as below:
     * <p>
     * User roles are lost during portlet interaction (we believed at the time
     * it was due to Ajax Push) because those interactions take place via
     * ServletRequest objects, not PortletRequest objects.
     * 
     * To support user roles with Portlets we will likely need either: acegi for
     * Portlets, or to cache user role determinations (note that the caching
     * cannot work in all cases because the user roles could change during
     * portlet execution, which is potentially a security hole, and user roles
     * requested during the initial page view may be different from those
     * requested during subsequent views; there are reasonable ways to structure
     * pages to work around this, however).
     * <p>
     * Refer - http://jira.icefaces.org/browse/ICE-1674
     * 
     */
    private boolean userInAdminOrOperatorRole;

    /**
     * UserExtendedRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * date time pattern
     */
    private String datetimePattern;

    /**
     * Default Constructor
     */
    public InformationPortletsHandler() {

        try {
            // super(Constant.INFORMATION_RENDER_GROUP);
            subpageIntegrator = new SubpageIntegrator();
            // initialize the faces context to be used in JMS thread
            facesContext = FacesContext.getCurrentInstance();

            referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                    .getManagedBean(Constant.REFERENCE_TREE);

            // read latency warning threshold from properties
            readLatencyThreashold();

            userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

            // subpage integration
            handleSubpageIntegration();
            UserRoleBean userRoleBean = (UserRoleBean) FacesUtils
                    .getManagedBean("userRoleBean");
            // read the user role from portlet request
            userInAdminOrOperatorRole = userRoleBean
                    .isUserInAdminOrOperatorRole();
            try {
                datetimePattern = PropertyUtils.getProperty(
                        ApplicationType.STATUS_VIEWER,
                        Constant.STATUS_VIEWER_DATETIME_PATTERN);
            } catch (Exception e) {
                // Ignore exception
                datetimePattern = Constant.DEFAULT_DATETIME_PATTERN;
            }

        } catch (Exception e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Reads latency warning threshold from property file. By default assign it
     * as 10 seconds.
     */
    private void readLatencyThreashold() {
        // read latency warning threshold from property file. By default assign
        // it as 10 seconds.
        this.latencyWaringThreshold = LATENCY_WARNING_THRESHOLD_IN_SECONDS_DEFAULT;
        String latencyThreashold = PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                LATENCY_WARNING_THRESHOLD_IN_SECONDS_KEY);
        if (null != latencyThreashold
                && !latencyThreashold.equals(Constant.EMPTY_STRING)) {
            try {
                latencyWaringThreshold = Integer.parseInt(latencyThreashold);
            } catch (NumberFormatException nfe) {
                // just log the exception as we have already assigned default
                // value
                LOGGER
                        .info("NumberFormatException while reading latency warning threshold from property file. Please check property [portal.statusviewer.latency.warning.threshold]. Continuing by assigning default threshold as 10 seconds.");
            }
        }
    }

    /**
     * Reads custom URLS from status-viewer properties.
     */
    private static void readCustomURLSFromProperties() {
        // status viewer host custom URL
        SV_HOST_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_HOST_URL_1_KEY));
        SV_HOST_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_HOST_URL_2_KEY));
        SV_HOST_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_HOST_URL_3_KEY));
        SV_HOST_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_HOST_URL_4_KEY));
        SV_HOST_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_HOST_URL_5_KEY));

        // status viewer host custom URL display name
        SV_HOST_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_HOST_URL_1_DISPLAY_NAME_KEY));
        SV_HOST_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_HOST_URL_2_DISPLAY_NAME_KEY));
        SV_HOST_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_HOST_URL_3_DISPLAY_NAME_KEY));
        SV_HOST_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_HOST_URL_4_DISPLAY_NAME_KEY));
        SV_HOST_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_HOST_URL_5_DISPLAY_NAME_KEY));

        // status viewer service custom URL
        SV_SERVICE_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_SERVICE_URL_1_KEY));
        SV_SERVICE_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_SERVICE_URL_2_KEY));
        SV_SERVICE_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_SERVICE_URL_3_KEY));
        SV_SERVICE_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_SERVICE_URL_4_KEY));
        SV_SERVICE_CUSTOM_URLS.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, CUSTOM_SERVICE_URL_5_KEY));

        // status viewer service custom URL display name
        SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_SERVICE_URL_1_DISPLAY_NAME_KEY));
        SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_SERVICE_URL_2_DISPLAY_NAME_KEY));
        SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_SERVICE_URL_3_DISPLAY_NAME_KEY));
        SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_SERVICE_URL_4_DISPLAY_NAME_KEY));
        SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME.add(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER,
                CUSTOM_SERVICE_URL_5_DISPLAY_NAME_KEY));
    }

    /**
     * Handles the subpage integration: Reads parameters from request in case of
     * Status Viewer. If portlet is in dashboard, reads preferences.
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
            String message2 = new PreferencesException().getMessage();
            handleInfo(message2);
            LOGGER.debug(message2);
            return;
        }
        // get the required data from SubpageIntegrator
        selectedNodeType = subpageIntegrator.getNodeType();
        selectedNodeId = subpageIntegrator.getNodeID();
        selectedNodeName = subpageIntegrator.getNodeName();
        inStatusViewer = subpageIntegrator.isInStatusViewer();

        LOGGER.debug("@@@@@@@@@@@@@@ [Information Portlets] # Node Type ["
                + selectedNodeType + "] # Node Name [" + selectedNodeName
                + "] # Node ID [" + selectedNodeId + "] # In Status Viewer ["
                + inStatusViewer + "]");
    }

    /**
     * Initializes Host and Service information portlets.
     */
    private void initializeInformationPortlets() {
        // re-initialize the bean so as to reload UI
        setError(false);
        setInfo(false);
        setMessage(false);
        // if selected node type is still null, then return from here.
        if (null == selectedNodeType) {
            return;
        }
        // as per node type, initialize information beans
        switch (selectedNodeType) {
            case HOST:
                // initialize and set host information bean details
                if (null == hostInformationBean) {
                    hostInformationBean = new InformationBean();
                }
                setHostInformationPortletDetails();
                break;

            case SERVICE:
                // initialize and set service information bean details
                if (null == serviceInformationBean) {
                    serviceInformationBean = new InformationBean();
                }
                setServiceInformationPortletDetails();
                break;

            default:
                LOGGER
                        .info("Information Portlets are not applicable for Node Type ["
                                + selectedNodeType + "]");
        }
    }

    /**
     * sets HostInformationPortlet Details<br>
     *
     */
    private void setHostInformationPortletDetails() {
        // retrieve actual Host from Id
        Host host;
        ServiceStatus[] serviceStatuses = null;
        try {
            host = foundationWSFacade.getHostsByName(selectedNodeName);
            serviceStatuses = foundationWSFacade.getServicesByHostName(selectedNodeName);
        } catch (WSDataUnavailableException e) {
            String hostNotAvailableErrorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_hostUnavailable")
                    + " [" + selectedNodeName + "]";
            LOGGER.error(hostNotAvailableErrorMessage);
            handleInfo(hostNotAvailableErrorMessage);
            return;

        } catch (GWPortalGenericException e) {
            LOGGER.error(
                    "Host Information Portlet : No Hosts available with Name ["
                            + selectedNodeName + "]", e);
            handleError(e.getMessage());
            return;
        }

        // set the selected node Id here (seems weird but required for JMS Push
        // in Dashboard)
     // One way of differentiating NAGIOS apptype and VEMA
        selectedNodeId = host.getHostID();
        boolean isAtleastOneNagiosService = false;
        if (serviceStatuses != null && serviceStatuses.length > 0)  {
            isAtleastOneNagiosService = CommonUtils.isAtleastOnceNagiosService(serviceStatuses);
        }
        hostInformationBean.setApplicationType(isAtleastOneNagiosService == true ? Constant.APP_TYPE_NAGIOS : CommonUtils.getApplicationNameByID(host.getApplicationTypeID()));

        
        String hostName = host.getName();
        

        // check for extended role permissions
        if (!referenceTreeModel.checkNodeForExtendedRolePermissions(
                selectedNodeId, NodeType.HOST, hostName, userExtendedRoleBean
                        .getExtRoleHostGroupList(), userExtendedRoleBean
                        .getExtRoleServiceGroupList())) {
            String inadequatePermissionsMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                    + " [" + hostName + "]";
            handleInfo(inadequatePermissionsMessage);
            return;
        }

        // get Property Type Binding for this Host
        PropertyTypeBinding propertyTypeBinding = host.getPropertyTypeBinding();

        // Check Type - Active / Passive
        String checkTypeValue = Constant.EMPTY_STRING;
        CheckType checkType = host.getCheckType();
        if (null != checkType) {
            checkTypeValue = checkType.getName();
            checkTypeValue = Constant.OPENING_ROUND_BRACE
                    + MonitorStatusUtilities.getCamelCaseStatus(checkTypeValue)
                    + Constant.CLOSING_ROUND_BRACE;
        }
        hostInformationBean.setCheckType(checkTypeValue);

        // Last Check Time
        String hostLastCheckTime = Constant.EMPTY_STRING;
        Date lastCheckTime = host.getLastCheckTime();
        if (null != lastCheckTime) {
            try {
                hostLastCheckTime = DateUtils.format(lastCheckTime,
                        datetimePattern);
            } catch (Exception e) {
                hostLastCheckTime = DateUtils.format(lastCheckTime,
                        Constant.DEFAULT_DATETIME_PATTERN);
            }
        }
        hostInformationBean.setLastCheckTime(hostLastCheckTime);

        // Next Check Time
        String hostNextCheckTime = Constant.EMPTY_STRING;
        Date nextCheckTime = host.getNextCheckTime();
        if (null != nextCheckTime) {
            try {
                hostNextCheckTime = DateUtils.format(nextCheckTime,
                        datetimePattern);
            } catch (Exception e) {
                hostNextCheckTime = DateUtils.format(nextCheckTime,
                        Constant.DEFAULT_DATETIME_PATTERN);
            }
        }
        if (hostLastCheckTime.equalsIgnoreCase(hostNextCheckTime)) {
            hostInformationBean.setNextCheckTime(NOT_AVAILABLE);
        } else {
            // Only set the next checktime for Active checks
            if (checkTypeValue != null && checkType.getName().equalsIgnoreCase(CHECKTYPE_ACTIVE))
                hostInformationBean.setNextCheckTime(hostNextCheckTime);
            else
                hostInformationBean.setNextCheckTime(NOT_AVAILABLE);
        }

        // State Type
        String stateTypeValue = Constant.EMPTY_STRING;
        StateType stateType = host.getStateType();
        if (null != stateType) {
            stateTypeValue = stateType.getName();
            stateTypeValue = Constant.OPENING_ROUND_BRACE
                    + MonitorStatusUtilities.getCamelCaseStatus(stateTypeValue)
                    + Constant.CLOSING_ROUND_BRACE;
        }
        hostInformationBean.setStateType(stateTypeValue);

        // set the Nagios link for Host
        hostInformationBean.setNagiosLink(getHostNagiosLink(hostName));

        /*
         * Depending on the acknowledgment status:
         * 
         * Acknowledged: No Acknowledge <= link to the acknowledge action
         * Acknowledged: Yes. By <username> at YYYY-MM-DD HH:MM:SS.
         */
        String hostCurrentStatus = host.getMonitorStatus().getName();
        if (NetworkObjectStatusEnum.HOST_UP.getStatus().equalsIgnoreCase(
                hostCurrentStatus)
                || NetworkObjectStatusEnum.HOST_PENDING.getStatus()
                        .equalsIgnoreCase(hostCurrentStatus)) {
            hostInformationBean.setAcknowledged(NOT_AVAILABLE);
        } else {
            BooleanProperty isHostAcknowledgedProperty = propertyTypeBinding
                    .getBooleanProperty(Constant.IS_ACKNOWLEDGED);
            if (isHostAcknowledgedProperty != null) {
                if (isHostAcknowledgedProperty.isValue()) {
                    hostInformationBean.setAcknowledged(Constant.YES);
                } else {
                    hostInformationBean.setAcknowledged(Constant.NO);
                }
            }
        }
        hostInformationBean.setHostName(hostName);

        // read custom URLs

        if (inStatusViewer) {
            processCustomURLS(hostInformationBean, SV_HOST_CUSTOM_URLS,
                    SV_HOST_CUSTOM_URLS_DISPLAY_NAME);
        } else {
            processCustomURLS(hostInformationBean, HOST_CUSTOM_URL_PREFS,
                    SV_HOST_CUSTOM_URLS_DISPLAY_NAME);
        }

        // set all the information properties common to both Host and Service
        // Information portlets
        setCommonInfoPortletDetails(hostInformationBean, propertyTypeBinding);
    }

    /**
     * Returns property value when supplied property name.
     * 
     * @param propertyTypeBinding
     * @param propertyName
     * @return property Value.
     */
    private String getProperty(PropertyTypeBinding propertyTypeBinding,
            String propertyName) {
        String propValue = CollagePropertyTypeConstants.PROPERTY_VALUE_UNAVAILABLE;
        Object propertyValue = propertyTypeBinding
                .getPropertyValue(propertyName);
        if (null != propertyValue) {
            propValue = propertyValue.toString();
        }
        // LOGGER.debug(propertyName + " : " + propValue);
        return propValue;
    }

    /**
     * sets Service Information Portlet Details. <br>
     *
     */
    private void setServiceInformationPortletDetails() {
        ServiceStatus service = null;
        try {
            if (inStatusViewer) {
                // retrieve service by using id
                service = foundationWSFacade.getServicesById(selectedNodeId);
            } else {
                if (null != facesContext) {
                    FacesUtils.setFacesContext(facesContext);
                }
                // use preferences - host name and service name to get the
                // service
                Map<String, String> servicePortletPreferences = PortletUtils
                        .getServicePortletPreferences();
                String hostName = servicePortletPreferences
                        .get(PreferenceConstants.HOST_NAME);
                String serviceName = servicePortletPreferences
                        .get(PreferenceConstants.SERVICE_NAME);
                try {
                    service = foundationWSFacade
                            .getServiceByHostAndServiceName(hostName,
                                    serviceName);
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
                    return;
                }
            }
        } catch (GWPortalGenericException e) {
            LOGGER
                    .error(
                            "Service Information Portlet: Exception while retrieving Service - ",
                            e);
            handleError(e.getMessage());
            return;
        }

        // Retrieve service name and associated host name
        String serviceName = service.getDescription();
        String hostName = service.getHost().getName();

        // Service application type is retrieved from service
        serviceInformationBean.setApplicationType(CommonUtils.getApplicationNameByID(service.getApplicationTypeID()));

        // set the selected node Id here (seems weird but required for JMS Push
        // in Dashboard)
        selectedNodeId = service.getServiceStatusID();

        // check for extended role permissions
        if (!referenceTreeModel.checkNodeForExtendedRolePermissions(
                selectedNodeId, NodeType.SERVICE, serviceName,
                userExtendedRoleBean.getExtRoleHostGroupList(),
                userExtendedRoleBean.getExtRoleServiceGroupList())) {
            String inadequatePermissionsMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                    + " [" + serviceName + "]";
            handleInfo(inadequatePermissionsMessage);
            return;
        }

        // get property type binding
        PropertyTypeBinding propertyTypeBinding = service
                .getPropertyTypeBinding();

        // Check Type - Active / Passive
        String checkTypeValue = Constant.EMPTY_STRING;
        CheckType checkType = service.getCheckType();
        if (null != checkType) {
            checkTypeValue = checkType.getName();
            checkTypeValue = Constant.OPENING_ROUND_BRACE
                    + MonitorStatusUtilities.getCamelCaseStatus(checkTypeValue)
                    + Constant.CLOSING_ROUND_BRACE;
        }
        serviceInformationBean.setCheckType(checkTypeValue);

        // Last Check Time
        String serviceLastCheckTime = Constant.EMPTY_STRING;
        Date lastCheckTime = service.getLastCheckTime();
        if (null != lastCheckTime) {
            try {
                serviceLastCheckTime = DateUtils.format(lastCheckTime,
                        datetimePattern);
            } catch (Exception e) {
                serviceLastCheckTime = DateUtils.format(lastCheckTime,
                        Constant.DEFAULT_DATETIME_PATTERN);
            }
        }
        serviceInformationBean.setLastCheckTime(serviceLastCheckTime);

        // Next Check Time
        String serviceNextCheckTime = Constant.EMPTY_STRING;
        Date nextCheckTime = service.getNextCheckTime();
        if (null != nextCheckTime) {
            try {
                serviceNextCheckTime = DateUtils.format(nextCheckTime,
                        datetimePattern);
            } catch (Exception e) {
                serviceNextCheckTime = DateUtils.format(nextCheckTime,
                        Constant.DEFAULT_DATETIME_PATTERN);
            }
        }
        if (serviceLastCheckTime.equalsIgnoreCase(serviceNextCheckTime)) {
            serviceInformationBean.setNextCheckTime(NOT_AVAILABLE);
        } else {
            // Only set the next checktime for Active checks
            if (checkTypeValue != null && checkType.getName().equalsIgnoreCase(CHECKTYPE_ACTIVE))
                serviceInformationBean.setNextCheckTime(serviceNextCheckTime);
            else
                serviceInformationBean.setNextCheckTime(NOT_AVAILABLE);

        }

        // State Type
        String stateTypeValue = Constant.EMPTY_STRING;
        StateType stateType = service.getStateType();
        if (null != stateType) {
            stateTypeValue = stateType.getName();
            stateTypeValue = Constant.OPENING_ROUND_BRACE
                    + MonitorStatusUtilities.getCamelCaseStatus(stateTypeValue)
                    + Constant.CLOSING_ROUND_BRACE;
        }
        serviceInformationBean.setStateType(stateTypeValue);

        // set the Nagios link for Service
        serviceInformationBean.setNagiosLink(getServiceNagiosLink(hostName,
                serviceName));

        /*
         * Depending on the acknowledgment status:
         * 
         * Acknowledged: No Acknowledge <= link to the acknowledge action
         * Acknowledged: Yes. By <username> at YYYY-MM-DD HH:MM:SS.
         */
        NetworkMetaEntity serviceNetworkMetaEntity = referenceTreeModel
                .getServiceById(selectedNodeId);
        // String serviceCurrentStatus = service.getMonitorStatus().getName();
        String serviceCurrentStatus = NetworkObjectStatusEnum.NO_STATUS
                .getStatus();
        if (null != serviceNetworkMetaEntity) {
            // make use of service status from RefrenceTreeMEtaModel over here
            serviceCurrentStatus = serviceNetworkMetaEntity.getStatus()
                    .getStatus();
        }

        if (NetworkObjectStatusEnum.SERVICE_OK.getStatus().equalsIgnoreCase(
                serviceCurrentStatus)
                || NetworkObjectStatusEnum.SERVICE_PENDING.getStatus()
                        .equalsIgnoreCase(serviceCurrentStatus)) {
            serviceInformationBean.setAcknowledged(NOT_AVAILABLE);
        } else {
            BooleanProperty isServiceAcknowledgedProperty = propertyTypeBinding
                    .getBooleanProperty(Constant.IS_PROBLEM_ACKNOWLEDGED);
            if (isServiceAcknowledgedProperty != null) {
                if (isServiceAcknowledgedProperty.isValue()) {
                    serviceInformationBean.setAcknowledged(Constant.YES);
                } else {
                    serviceInformationBean.setAcknowledged(Constant.NO);
                }
            }
        }
        serviceInformationBean.setHostName(hostName);
        serviceInformationBean.setServiceName(serviceName);

        // read custom URLs
        if (inStatusViewer) {
            processCustomURLS(serviceInformationBean, SV_SERVICE_CUSTOM_URLS,
                    SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME);
        } else {
            processCustomURLS(serviceInformationBean, SERVICE_CUSTOM_URL_PREFS,
                    SV_SERVICE_CUSTOM_URLS_DISPLAY_NAME);
        }

        // set all the information properties common to both Host and Service
        // Information portlets
        setCommonInfoPortletDetails(serviceInformationBean, propertyTypeBinding);
    }

    /**
     * Sets Information portlet details which are common in both Host and
     * Service Information portlets.
     * 
     * @param informationBean
     * @param propertyTypeBinding
     */
    private void setCommonInfoPortletDetails(InformationBean informationBean,
            PropertyTypeBinding propertyTypeBinding) {
        // Status Value
        String statusValue = getProperty(propertyTypeBinding,
                CollagePropertyTypeConstants.LAST_PLUGIN_OUTPUT_PROPERTY);
        if (statusValue.contains("\\n")) {
            statusValue = statusValue.replaceAll("\\\\n", Constant.BR);
        }
        statusValue = CommonUtils.getWrapString(statusValue, Constant.FIFTEEN);
        informationBean.setStatusValue(statusValue);

        // In Downtime
        /*
         * logic: If value of this property is '0' then display 'No'. If
         * property value not available, display 'Unavailable'. Else display
         * 'Yes'.
         */
        String inDowntime = getProperty(propertyTypeBinding,
                CollagePropertyTypeConstants.SCHEDULE_DOWNTIME_DEPTH_PROPERTY);
        try {
            int inScheduleDowntime = Integer.parseInt(inDowntime);
            if (inScheduleDowntime == 0) {
                inDowntime = Constant.NO;
            } else {
                inDowntime = Constant.YES;
            }
        } catch (NumberFormatException e) {
            inDowntime = CollagePropertyTypeConstants.PROPERTY_VALUE_UNAVAILABLE;
        }

        informationBean.setScheduleDowntime(inDowntime);

        // Notification Count - current Notification Number
        int currentNotificationNumber = 0;

        IntegerProperty curNotificationNumProperty = propertyTypeBinding
                .getIntegerProperty(CollagePropertyTypeConstants.CURRENT_NOTIFICATION_NUMBER);
        if (null != curNotificationNumProperty) {
            currentNotificationNumber = curNotificationNumProperty.getValue();
        }

        informationBean
                .setCurrentNotificationNumber(Constant.OPENING_ROUND_BRACE
                        + NOTIFICATION + currentNotificationNumber
                        + Constant.CLOSING_ROUND_BRACE);

        // Last Notification Time
        if (currentNotificationNumber == 0) {
            informationBean.setLastNotificationTime(NOT_AVAILABLE);
        } else {
            informationBean
                    .setLastNotificationTime(getProperty(
                            propertyTypeBinding,
                            CollagePropertyTypeConstants.LAST_NOTIFICATION_TIME_PROPERTY));
        }

        // Percentage State Change
        informationBean.setPercentageStateChange(getProperty(
                propertyTypeBinding,
                CollagePropertyTypeConstants.PERCENTAGE_STATE_CHANGE_PROPERTY));

        // Current Check Attempts
        informationBean.setCurrentCheckAttempts(getProperty(
                propertyTypeBinding,
                CollagePropertyTypeConstants.CURRENT_CHECK_ATTEMPTS));

        // Max Check Attempts
        informationBean.setMaxCheckAttempts(getProperty(propertyTypeBinding,
                CollagePropertyTypeConstants.MAX_CHECK_ATTEMPTS));

        // Latency and Latency Threshold Warning.
        informationBean.setLatencyThresholdWarning(Constant.EMPTY_STRING);
        informationBean.setShowLatencyWarning(false);

        informationBean
                .setLatency(CollagePropertyTypeConstants.PROPERTY_VALUE_UNAVAILABLE);
        DoubleProperty latencyDoubleProperty = propertyTypeBinding
                .getDoubleProperty(CollagePropertyTypeConstants.LATENCY_PROPERTY);
        if (null != latencyDoubleProperty) {
            double latency = latencyDoubleProperty.getValue()
                    / Constant.THOUSAND;
            informationBean.setLatency(String.valueOf(latency));
            if (latency > latencyWaringThreshold) {
                informationBean.setLatencyThresholdWarning(WARNING);
                informationBean.setShowLatencyWarning(true);
            }
        }

        // Duration Viz. Execution Time
        informationBean
                .setDuration(CollagePropertyTypeConstants.PROPERTY_VALUE_UNAVAILABLE);
        DoubleProperty durationDoubleProperty = propertyTypeBinding
                .getDoubleProperty(CollagePropertyTypeConstants.EXECUTION_TIME);
        if (null != durationDoubleProperty) {
            double duration = durationDoubleProperty.getValue()
                    / Constant.THOUSAND;
            informationBean.setDuration(String.valueOf(duration));
        }

        // Notifications Enabled? - default should be false
        informationBean.setNotificationsEnabled(false);
        BooleanProperty notificationsBooleanProperty = propertyTypeBinding
                .getBooleanProperty(NagiosStatisticsConstants.IS_NOTIFICATIONS_ENABLED_PROPERTY);
        if (null != notificationsBooleanProperty) {
            informationBean
                    .setNotificationsEnabled(notificationsBooleanProperty
                            .isValue());
        }

        // Active Checks enabled? - default should be false
        informationBean.setActiveChecksEnabled(false);
        BooleanProperty activeChecksEnabledBooleanProperty = propertyTypeBinding
                .getBooleanProperty(NagiosStatisticsConstants.IS_ACTIVECHECKS_ENABLED_PROPERTY);
        if (null != activeChecksEnabledBooleanProperty) {
            informationBean
                    .setActiveChecksEnabled(activeChecksEnabledBooleanProperty
                            .isValue());
        }

        // Is User In Admin Or Operator Role?
        informationBean.setUserInAdminOrOperatorRole(userInAdminOrOperatorRole);
    }

    /**
     * @param customUrl
     * @param informationBean
     * @return customUrl
     */
    private String replaceDollarParams(String customUrl,
            InformationBean informationBean) {
        if (customUrl != null
                && !customUrl.trim().equals(Constant.EMPTY_STRING)) {
            if (customUrl.contains(HOST_TOKEN)) {
                customUrl = customUrl.replace(HOST_TOKEN, informationBean
                        .getHostName());
            }
            if (customUrl.contains(SERVICE_TOKEN)) {
                String serviceName = informationBean.getServiceName();
                if (serviceName != null
                        && !serviceName.trim().equals(Constant.EMPTY_STRING)) {
                    customUrl = customUrl.replace(SERVICE_TOKEN, serviceName);
                }
            }
        }

        return customUrl;
    }

    /**
     * Processes custom URLs.
     * 
     * @param informationBean
     * @param customUrlPrefs
     */
    private void processCustomURLS(InformationBean informationBean,
            List<String> customUrlPrefs, List<String> customUrlDisplayName) {
        // Custom Link 1
        try {
            // informationBean.setCustomLink1URLValue(null);
            String custLink1Preference = null;

            if (inStatusViewer) {
                custLink1Preference = customUrlPrefs.get(Constant.ZERO);

                String custLink1DisplayName = customUrlDisplayName
                        .get(Constant.ZERO);
                // replace $ parameters
                custLink1Preference = replaceDollarParams(custLink1Preference,
                        informationBean);
                String customURL1Host = isMalformedURL(custLink1Preference);
                if (null != customURL1Host) {
                    if (custLink1DisplayName != null
                            && !custLink1DisplayName.trim().equals(
                                    Constant.EMPTY_STRING)) {
                        informationBean
                                .setCustomLink1URLValue(truncateCustomURLHost(custLink1DisplayName
                                        .trim()));
                    } else {
                        informationBean
                                .setCustomLink1URLValue(truncateCustomURLHost(customURL1Host));
                    }
                    informationBean.setCustomLink1URL(custLink1Preference);
                }
            } else {
                custLink1Preference = FacesUtils.getPreference(customUrlPrefs
                        .get(Constant.ZERO));
                // replace $ parameters
                custLink1Preference = replaceDollarParams(custLink1Preference,
                        informationBean);

                String customURL1Host = isMalformedURL(custLink1Preference);
                if (null != customURL1Host) {

                    informationBean
                            .setCustomLink1URLValue(truncateCustomURLHost(customURL1Host));

                    informationBean.setCustomLink1URL(custLink1Preference);
                }
            }

        } catch (PreferencesException e) {
            // ignore the exception
            // informationBean.setCustomLink1URLValue(null);
        }

        // Custom Link 2
        try {
            // informationBean.setCustomLink2URLValue(null);
            String custLink2Preference = null;

            if (inStatusViewer) {
                custLink2Preference = customUrlPrefs.get(Constant.ONE);

                String custLink2DisplayName = customUrlDisplayName
                        .get(Constant.ONE);
                // replace $ parameters
                custLink2Preference = replaceDollarParams(custLink2Preference,
                        informationBean);
                String customURL2Host = isMalformedURL(custLink2Preference);
                if (null != customURL2Host) {
                    if (custLink2DisplayName != null
                            && !custLink2DisplayName.trim().equals(
                                    Constant.EMPTY_STRING)) {
                        informationBean
                                .setCustomLink2URLValue(truncateCustomURLHost(custLink2DisplayName
                                        .trim()));
                    } else {
                        informationBean
                                .setCustomLink2URLValue(truncateCustomURLHost(customURL2Host));
                    }
                    informationBean.setCustomLink2URL(custLink2Preference);
                }
            } else {
                custLink2Preference = FacesUtils.getPreference(customUrlPrefs
                        .get(Constant.ONE));
                // replace $ parameters
                custLink2Preference = replaceDollarParams(custLink2Preference,
                        informationBean);

                String customURL2Host = isMalformedURL(custLink2Preference);
                if (null != customURL2Host) {

                    informationBean
                            .setCustomLink2URLValue(truncateCustomURLHost(customURL2Host));

                    informationBean.setCustomLink2URL(custLink2Preference);
                }
            }

        } catch (PreferencesException e) {
            // ignore the exception
            // informationBean.setCustomLink2URLValue(null);
        }

        // Custom Link 3
        try {
            // informationBean.setCustomLink3URLValue(null);
            String custLink3Preference = null;

            if (inStatusViewer) {
                custLink3Preference = customUrlPrefs.get(Constant.TWO);

                String custLink3DisplayName = customUrlDisplayName
                        .get(Constant.TWO);
                // replace $ parameters
                custLink3Preference = replaceDollarParams(custLink3Preference,
                        informationBean);
                String customURL3Host = isMalformedURL(custLink3Preference);
                if (null != customURL3Host) {
                    if (custLink3DisplayName != null
                            && !custLink3DisplayName.trim().equals(
                                    Constant.EMPTY_STRING)) {
                        informationBean
                                .setCustomLink3URLValue(truncateCustomURLHost(custLink3DisplayName
                                        .trim()));
                    } else {
                        informationBean
                                .setCustomLink3URLValue(truncateCustomURLHost(customURL3Host));
                    }
                    informationBean.setCustomLink3URL(custLink3Preference);
                }
            } else {
                custLink3Preference = FacesUtils.getPreference(customUrlPrefs
                        .get(Constant.TWO));
                // replace $ parameters
                custLink3Preference = replaceDollarParams(custLink3Preference,
                        informationBean);

                String customURL3Host = isMalformedURL(custLink3Preference);
                if (null != customURL3Host) {

                    informationBean
                            .setCustomLink3URLValue(truncateCustomURLHost(customURL3Host));

                    informationBean.setCustomLink3URL(custLink3Preference);
                }
            }

        } catch (PreferencesException e) {
            // ignore the exception
            // informationBean.setCustomLink3URLValue(null);
        }

        // Custom Link 4
        try {
            // informationBean.setCustomLink4URLValue(null);
            String custLink4Preference = null;

            if (inStatusViewer) {
                custLink4Preference = customUrlPrefs.get(Constant.THREE);

                String custLink4DisplayName = customUrlDisplayName
                        .get(Constant.THREE);
                // replace $ parameters
                custLink4Preference = replaceDollarParams(custLink4Preference,
                        informationBean);
                String customURL4Host = isMalformedURL(custLink4Preference);
                if (null != customURL4Host) {
                    if (custLink4DisplayName != null
                            && !custLink4DisplayName.trim().equals(
                                    Constant.EMPTY_STRING)) {
                        informationBean
                                .setCustomLink4URLValue(truncateCustomURLHost(custLink4DisplayName
                                        .trim()));
                    } else {
                        informationBean
                                .setCustomLink4URLValue(truncateCustomURLHost(customURL4Host));
                    }
                    informationBean.setCustomLink4URL(custLink4Preference);
                }
            } else {
                custLink4Preference = FacesUtils.getPreference(customUrlPrefs
                        .get(Constant.THREE));
                // replace $ parameters
                custLink4Preference = replaceDollarParams(custLink4Preference,
                        informationBean);

                String customURL4Host = isMalformedURL(custLink4Preference);
                if (null != customURL4Host) {

                    informationBean
                            .setCustomLink4URLValue(truncateCustomURLHost(customURL4Host));

                    informationBean.setCustomLink4URL(custLink4Preference);
                }
            }

        } catch (PreferencesException e) {
            // ignore the exception
            // informationBean.setCustomLink4URLValue(null);
        }

        // Custom Link 5
        try {
            // informationBean.setCustomLink5URLValue(null);
            String custLink5Preference = null;

            if (inStatusViewer) {
                custLink5Preference = customUrlPrefs.get(Constant.FOUR);

                String custLink5DisplayName = customUrlDisplayName
                        .get(Constant.FOUR);
                // replace $ parameters
                custLink5Preference = replaceDollarParams(custLink5Preference,
                        informationBean);
                String customURL5Host = isMalformedURL(custLink5Preference);
                if (null != customURL5Host) {
                    if (custLink5DisplayName != null
                            && !custLink5DisplayName.trim().equals(
                                    Constant.EMPTY_STRING)) {
                        informationBean
                                .setCustomLink5URLValue(truncateCustomURLHost(custLink5DisplayName
                                        .trim()));
                    } else {
                        informationBean
                                .setCustomLink5URLValue(truncateCustomURLHost(customURL5Host));
                    }
                    informationBean.setCustomLink5URL(custLink5Preference);
                }
            } else {
                custLink5Preference = FacesUtils.getPreference(customUrlPrefs
                        .get(Constant.FOUR));
                // replace $ parameters
                custLink5Preference = replaceDollarParams(custLink5Preference,
                        informationBean);

                String customURL5Host = isMalformedURL(custLink5Preference);
                if (null != customURL5Host) {

                    informationBean
                            .setCustomLink5URLValue(truncateCustomURLHost(customURL5Host));

                    informationBean.setCustomLink5URL(custLink5Preference);
                }
            }

        } catch (PreferencesException e) {
            // ignore the exception
            // informationBean.setCustomLink5URLValue(null);
        }
    }

    /**
     * Truncates Custom URL Host
     * 
     * @param customURLHost
     * @return truncated custom URL Host if length is > TRUNCATE_CUSTOM_HOST_URL
     */
    private String truncateCustomURLHost(String customURLHost) {
        if (customURLHost.length() > TRUNCATE_CUSTOM_HOST_URL) {
            customURLHost = customURLHost.substring(0, TRUNCATE_CUSTOM_HOST_URL
                    - Constant.THREE)
                    + Constant.ELLIPSES;
        }
        return customURLHost;
    }

    /**
     * Method used to navigate to Host-Acknowledgment pop up page
     * 
     * @param event
     */
    public void showHostAcknowledgementPopup(ActionEvent event) {
        // LOGGER
        // .debug(
        // "showHostAcknowledgementPopup(): Displaying acknowledgement popup for host"
        // );
        showAcknowledgementPopup(event, false);
    }

    /**
     * Method used to navigate to Service-Acknowledgment pop up page
     * 
     * @param event
     */
    public void showServiceAcknowledgementPopup(ActionEvent event) {
        // LOGGER
        // .debug(
        // "showHostAcknowledgementPopup(): Displaying acknowledgement popup for host"
        // );
        showAcknowledgementPopup(event, true);
    }

    /**
     * Displays the Acknowledgment Pop-up for Host / Service
     * 
     * @param event
     * @param isService
     */
    private void showAcknowledgementPopup(ActionEvent event, boolean isService) {
        // Set parameters in pop-up display bean
        AcknowledgePopupBean acknowledgePopupBean = (AcknowledgePopupBean) FacesUtils
                .getManagedBean(Constant.ACKNOWLEDGE_POPUP_MANAGED_BEAN);

        if (acknowledgePopupBean == null) {
            // LOGGER
            // .debug(
            // "setAcknowledgeParameters(): Cannot retrieve acknowledgement pop up bean"
            // );
            return;
        }

        // Get runtime attributes for acknowledgment
        String hostName = (String) event.getComponent().getAttributes().get(
                Constant.ACKNOWLEDGE_PARAM_HOST_NAME);
        String userName = FacesUtils.getLoggedInUser();

        // Indicates this is a service acknowledgment pop-up
        acknowledgePopupBean.setHostAck(!isService);
        acknowledgePopupBean.setHostName(hostName);
        acknowledgePopupBean.setAuthor(userName);
        acknowledgePopupBean.setUserName(userName);
       // making the Persistent comemnt checked
        acknowledgePopupBean.setPersistentComment(true);
        if (isService) {
            String serviceName = (String) event.getComponent().getAttributes()
                    .get(Constant.ACKNOWLEDGE_PARAM_SERVICE_NAME);
            acknowledgePopupBean.setServiceDescription(serviceName);
        }
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

    /**
     * Checks if URL is malformed.
     * 
     * @param customURL
     * @return Host in the passed URL if its not malformed. Returns null if URL
     *         is malformed.
     */
    private String isMalformedURL(String customURL) {
        try {
            URL url = new URL(customURL);
            return url.getHost();
        } catch (MalformedURLException e1) {
            // returning null to indicate that URL is malformed.
            return null;
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
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {
        initializeInformationPortlets();
    }

    /**
     * Returns the hostInformationBean.
     * 
     * @return the hostInformationBean
     */
    public InformationBean getHostInformationBean() {
        return hostInformationBean;
    }

    /**
     * Returns the serviceInformationBean.
     * 
     * @return the serviceInformationBean
     */
    public InformationBean getServiceInformationBean() {
        return serviceInformationBean;
    }

    /**
     * Method for handling "schedule downtime" link clicked event for Host.
     * 
     * @param event
     */
    public void scheduleDowntimeForHost(ActionEvent event) {
        sendEventToActions(event,
                HostActionEnum.Downtime.SCHEDULE_HOST_DOWNTIME.name(),
                HostActionEnum.Downtime.SCHEDULE_HOST_DOWNTIME
                        .getActionCommand(), ParentMenuActionEnum.DOWNTIME
                        .getMenuString());
    }

    /**
     * Method for handling "schedule downtime" link clicked event for Service.
     * 
     * @param event
     */
    public void scheduleDowntimeForService(ActionEvent event) {
        sendEventToActions(event,
                ServiceActionEnum.Downtime.SCHEDULE_SVC_DOWNTIME.name(),
                ServiceActionEnum.Downtime.SCHEDULE_SVC_DOWNTIME
                        .getActionCommand(), ParentMenuActionEnum.DOWNTIME
                        .getMenuString());
    }

    /**
     * Method for handling "re-schedule next check" link clicked event for Host.
     * 
     * @param event
     */
    public void rescheduleNextCheckForHost(ActionEvent event) {
        sendEventToActions(event,
                HostActionEnum.CheckResults.SCHEDULE_HOST_CHECK.name(),
                HostActionEnum.CheckResults.SCHEDULE_HOST_CHECK
                        .getActionCommand(), ParentMenuActionEnum.CHECK_RESULTS
                        .getMenuString());
    }

    /**
     * Method for handling "re-schedule next check" link clicked event for
     * Service.
     * 
     * @param event
     */
    public void rescheduleNextCheckForService(ActionEvent event) {
        sendEventToActions(event,
                ServiceActionEnum.CheckResults.SCHEDULE_SVC_CHECK.name(),
                ServiceActionEnum.CheckResults.SCHEDULE_SVC_CHECK
                        .getActionCommand(), ParentMenuActionEnum.CHECK_RESULTS
                        .getMenuString());
    }

    /**
     * Method for handling "disable active checks" link clicked event for Host.
     * 
     * @param event
     */
    public void disableActiveChecksForHost(ActionEvent event) {
        sendEventToActions(event, HostActionEnum.Settings.DISABLE_HOST_CHECK
                .name(), HostActionEnum.Settings.DISABLE_HOST_CHECK
                .getActionCommand(), ParentMenuActionEnum.SETTINGS
                .getMenuString());
    }

    /**
     * Method for handling "disable active checks" link clicked event for
     * Service.
     * 
     * @param event
     */
    public void disableActiveChecksForService(ActionEvent event) {
        sendEventToActions(event, ServiceActionEnum.Settings.DISABLE_SVC_CHECK
                .name(), ServiceActionEnum.Settings.DISABLE_SVC_CHECK
                .getActionCommand(), ParentMenuActionEnum.SETTINGS
                .getMenuString());
    }

    /**
     * Method for handling "enable active checks" link clicked event for Host.
     * 
     * @param event
     */
    public void enableActiveChecksForHost(ActionEvent event) {
        sendEventToActions(event, HostActionEnum.Settings.ENABLE_HOST_CHECK
                .name(), HostActionEnum.Settings.ENABLE_HOST_CHECK
                .getActionCommand(), ParentMenuActionEnum.SETTINGS
                .getMenuString());
    }

    /**
     * Method for handling "enable active checks" link clicked event for
     * Service.
     * 
     * @param event
     */
    public void enableActiveChecksForService(ActionEvent event) {
        sendEventToActions(event, ServiceActionEnum.Settings.ENABLE_SVC_CHECK
                .name(), ServiceActionEnum.Settings.ENABLE_SVC_CHECK
                .getActionCommand(), ParentMenuActionEnum.SETTINGS
                .getMenuString());
    }

    /**
     * Method for handling "enable notification" link clicked event for Host.
     * 
     * @param event
     */
    public void enableNotificationForHost(ActionEvent event) {
        sendEventToActions(event,
                HostActionEnum.Notifications.ENABLE_HOST_NOTIFICATIONS.name(),
                HostActionEnum.Notifications.ENABLE_HOST_NOTIFICATIONS
                        .getActionCommand(), ParentMenuActionEnum.NOTIFICATIONS
                        .getMenuString());
    }

    /**
     * Method for handling "enable notification" link clicked event for Service.
     * 
     * @param event
     */
    public void enableNotificationForService(ActionEvent event) {
        sendEventToActions(
                event,
                ServiceActionEnum.Notifications.ENABLE_SVC_NOTIFICATIONS.name(),
                ServiceActionEnum.Notifications.ENABLE_SVC_NOTIFICATIONS
                        .getActionCommand(), ParentMenuActionEnum.NOTIFICATIONS
                        .getMenuString());
    }

    /**
     * Method for handling "disable notification" link clicked event for Host.
     * 
     * @param event
     */
    public void disableNotificationForHost(ActionEvent event) {
        sendEventToActions(event,
                HostActionEnum.Notifications.DISABLE_HOST_NOTIFICATIONS.name(),
                HostActionEnum.Notifications.DISABLE_HOST_NOTIFICATIONS
                        .getActionCommand(), ParentMenuActionEnum.NOTIFICATIONS
                        .getMenuString());
    }

    /**
     * Method for handling "disable notification" link clicked event for
     * Service.
     * 
     * @param event
     */
    public void disableNotificationForService(ActionEvent event) {
        sendEventToActions(event,
                ServiceActionEnum.Notifications.DISABLE_SVC_NOTIFICATIONS
                        .name(),
                ServiceActionEnum.Notifications.DISABLE_SVC_NOTIFICATIONS
                        .getActionCommand(), ParentMenuActionEnum.NOTIFICATIONS
                        .getMenuString());
    }

    /**
     * Set the childId,childValue,parentMenu as per the current
     * context(Host,service,host group,service group).
     * 
     * @param event
     * @param commandName
     * @param actionCommand
     * @param parentMenuString
     */
    private void sendEventToActions(ActionEvent event, String commandName,
            String actionCommand, String parentMenuString) {
        // Action Command integration
        PortletSession session = FacesUtils.getPortletSession(false);
        if (session == null) {
            LOGGER
                    .error("Null portlet session obtained before calling actionHandler.showPopup()");
            return;
        }
        /**
         * Set the childId,childValue,parentMenu as per the current
         * context(Host,service,host group,service group). Enums are defined for
         * each of the context. Use the correct ones as per context.
         * (sstateController.getNodeType() gives the current context.) In this
         * example code,I am assuming that the context is HOST and hence using
         * 'HostActionEnum'.
         */
        // Set the command name as childId.
        session.setAttribute(Constant.SESSION_ATTR_CHILD_ID, commandName);
        // Set the action command name as childValue.
        session.setAttribute(Constant.SESSION_ATTR_CHILD_VALUE, actionCommand);
        // Set the parent menu name as parent menu.
        session.setAttribute(Constant.SESSION_ATTR_PARENT_MENU,
                parentMenuString);

        // call action handler's show popup method
        ActionHandlerEE actionHandler = (ActionHandlerEE) FacesUtils
                .getManagedBean(Constant.ACTION_HANDLER_MANAGED_BEAN);
        LOGGER.debug("calling show popup of action handler");
        actionHandler.showPopup(event, selectedNodeType, selectedNodeId,
                selectedNodeName);
    }

    /**
     * Returns 'Host' nagios link. <br>
     * 
     * Example - Nagios "localhost" link - http://v-groundwork2
     * .persistent.co.in/nagios/cgi-bin/extinfo.cgi?type=1&host=localhost
     * 
     * @return Nagios link for Host
     */
    private String getHostNagiosLink(String hostName) {
        return HOST_NAGIOS_LINK + hostName;
    }

    /**
     * Returns 'Service' nagios link. <br>
     * Example - Nagios "localhost" link - http://v-groundwork2
     * .persistent.co.in/nagios/cgi-bin/extinfo.cgi?type=1&host=localhost
     * 
     * @return Nagios link for Service
     */
    private String getServiceNagiosLink(String hostName, String serviceName) {
        return new StringBuilder(SERVICE_NAGIOS_LINK).append(hostName).append(
                SERVICE_NAGIOS_LINK_SERVICE_PART).append(serviceName)
                .toString();
    }

    // JMS PUSH

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlTopic) {
        // if (xmlTopic == null) {
        // LOGGER
        //.debug("refresh() of Information Portlets : Received null XML Message."
        // );
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
        // .debug(
        // "refresh() of Information Portlets : Received null JMS Updates using JMSUtils.getJMSUpdatesListFromXML() utility method"
        // );
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
        // /*
        // * Initializes Host and Service information portlets as per
        // * the Node Type.
        // */
        // initializeInformationPortlets(true);
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER
        // .debug("Pushing information portlets for selectedNodeId : "
        // + selectedNodeId);
        // }
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
     * Sets the informationHiddenField.
     * 
     * @param informationHiddenField
     *            the informationHiddenField to set
     */
    public void setInformationHiddenField(String informationHiddenField) {
        this.informationHiddenField = informationHiddenField;
    }

    /**
     * Returns the informationHiddenField.
     * 
     * @return the informationHiddenField
     */
    public String getInformationHiddenField() {

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
            LOGGER
                    .debug(" In getInformationHiddenField() of Information Portlets. Calling initializeInformationPortlets ....");
            /*
             * Initializes Host and Service information portlets as per the Node
             * Type.
             */
            initializeInformationPortlets();
        }
        setIntervalRender(false);

        return informationHiddenField;
    }

}
