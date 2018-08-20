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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.RRDGraph;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;

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
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.TimeIntervalEnumEE;
import com.groundworkopensource.portal.statusviewer.common.ValidationUtils;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.groundworkopensource.portal.statusviewer.handler.SubpageIntegrator;

/**
 * This bean is used by "Performance measurement Portlet".
 * 
 * @author rashmi_tambe
 * 
 */
public class PerfMeasurementBeanEE extends ServerPush implements Serializable {

    /**
     * GRAPH_WIDTH_400
     */
    private static final int GRAPH_WIDTH_400 = 400;

    // /**
    // * graphic img UI component ID constant part
    // */
    // private static final String FRM = "frm";
    //
    // /**
    // * graphic img UI component ID constant part
    // */
    // private static final String RRDIMG = "RRDImg";

    /**
     * graphic img UI component ID constant part
     */
    private static final String PERF_MEASUREMENT_PORTLET = "perfMeasurementPortlet_";

    /**
     * No graph available String constant
     */
    private static final String NO_GRAPH_AVAILABLE = "No graph available";

    /**
     * COLLAPSIBLE_TITLE
     */
    private static final String COLLAPSIBLE_TITLE = "collapsibleTitle";

    /**
     * HUNDREN
     */
    private static final int HUNDREN = 100;

    /**
     * perf graph reduce 7.5 percent constant
     */
    private static final double PERF_GRAPH_REDUCE_PERCENT = 21;

    /**
     * integer constant for width of the bar chart
     */
    public static final int DASHBORD_CHART_WIDTH = 1000;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3407184019878964469L;

    /**
     * Purposely added for debugging purpose. TODO Remove afterwards.
     */
    // private String myName = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(PerfMeasurementBeanEE.class.getName());

    /**
     * Time interval, in milliseconds, between renders.
     */
    private static Long graphRenderInterval;

    /**
     * DEFAULT INTERVAL
     */
    private static final long DEFAULT_INTERVAL = 180000L;

    /**
     * rrd graph list
     */
    private List<RrdGraphBean> rrdGraphList;
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
     * expanded
     */
    private boolean expanded = true;

    /**
     * boolean variable for custom dates
     */
    private boolean customDate = false;

    /**
     * Date format String
     */
    private static final String DATE_FORMAT_24_HR = "MM/dd/yyyy H:mm";
    /**
     * custom date format
     */
    private SimpleDateFormat custDateFormat = new SimpleDateFormat(
            DATE_FORMAT_24_HR);
    /**
     * SimpleDateFormat
     */
    private SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * custom start date
     */
    private String custStartDate;
    /**
     * custom End date
     */
    private String custEndDate;
    /**
     * Previous Time selected from 'time selector' drop-down.
     */
    private String oldSelectedTime;

    /**
     * custom dates render boolean variable
     */
    private boolean renderCustDates = false;

    /**
     * Returns the renderCustDates.
     * 
     * @return the renderCustDates
     */
    public boolean isRenderCustDates() {
        return renderCustDates;
    }

    /**
     * Sets the renderCustDates.
     * 
     * @param renderCustDates
     *            the renderCustDates to set
     */
    public void setRenderCustDates(boolean renderCustDates) {
        this.renderCustDates = renderCustDates;
    }

    /**
     * SimpleDateFormat
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            Constant.DATE_FORMAT_24_HR_CLK);

    /**
     * foundationWSFacade Object to call web services.
     */
    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

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
     * hidden field
     */
    private String perfHiddenField = Constant.HIDDEN;
    /**
     * PerfMeasurementIPCBean
     */
    private PerfMeasurementIPCBean currentPerfTimeFilterbean;

    /**
     * A Hashtable containing "service name" -> corresponding "RRD file path".
     */
    private final Hashtable<String, Boolean> collapsibleStatusMap;

    /**
     * facesContext
     */
    private final FacesContext facesContext;
    /**
     * paddingStyle
     */
    private String paddingStyle = Constant.EMPTY_STRING;

    /**
     * Time selected from 'time selector' drop-down.
     */
    private String selectedTime;

    /**
     * List of selectItems for time selector component.
     */
    private List<SelectItem> timeSelectorList;

    /**
     * Id for form UI component
     */
    private String perfMeasurementFrmID;

    /**
     * ReferenceTreeMetaModel instance
     * <p>
     * !!!!!!!!!!! IMP !!!!!!!!!! : Please do not remove below declaration of
     * referenceTreeModel.
     */
    private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            .getManagedBean(Constant.REFERENCE_TREE);

    /**
     * SubpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

    /**
     * stateController
     */
    // StateController stateController = null;
    static {
        // get the rendering interval value from property.

        try {
            graphRenderInterval = Long.parseLong(PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    "perf.monitoring.rendering.interval"));

        } catch (NumberFormatException numberFormatException) {
            LOGGER
                    .error("Exception while getting perf.monitoring.rendering.interval status-viewer properties files hencs default time 2000 milli second  is set");
            graphRenderInterval = DEFAULT_INTERVAL;
        } catch (Exception e) {
            LOGGER
                    .error("Exception while getting perf.monitoring.rendering.interval status-viewer properties files hencs default time 2000 milli second  is set");
            graphRenderInterval = DEFAULT_INTERVAL;
        }

        LOGGER.info("Retrieved perf.monitoring.rendering.interval = "
                + graphRenderInterval);
    }

    // /**
    // * requestId
    // */
    // protected String requestId = null;
    /**
     * timeperf
     */
    private String timepref;

    /**
     * userExtendedRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Default constructor. Initializes this bean.
     */
    public PerfMeasurementBeanEE() {

        super(graphRenderInterval);
        rrdGraphList = Collections
                .synchronizedList(new ArrayList<RrdGraphBean>());
        collapsibleStatusMap = new Hashtable<String, Boolean>();
        subpageIntegrator = new SubpageIntegrator();

        // initialize the faces context to be used in JMS thread
        facesContext = FacesContext.getCurrentInstance();

        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        // handle subpage integration
        handleSubpageIntegration();

        if (!inStatusViewer) {
            try {

                int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
                // Unique id for form UI component
                setPerfMeasurementFrmID(PERF_MEASUREMENT_PORTLET + "frm"
                        + randomID);

                timepref = FacesUtils.getPreference("timepref");
                LOGGER.debug("Time perf:-" + timepref);
                if ("-1".equalsIgnoreCase(timepref)) {
                    String custStartDatePref = FacesUtils
                            .getPreference("custStartDatePref");
                    String custEndDatePref = FacesUtils
                            .getPreference("custEndDatePref");
                    setCustStartDate(custStartDatePref);
                    setCustEndDate(custEndDatePref);
                    setCustomDate(true);
                }

            } catch (PreferencesException e) {
                LOGGER
                        .error("Exception while getting Time filter Preferences.Hence setting Today time filter :-"
                                + e);

                timepref = "1";

            }
        }

        this.initializeTimeSelectors();

        // Setting selected time same as in Preferences.
        setSelectedTime(timepref);
        setOldSelectedTime(timepref);
        /*
         * String myName = "PerfMeasurementBean_" + String.valueOf(new
         * java.util.Random().nextInt()) + "_"; // Added for debugging
         * LOGGER.warn("************************* : " + myName);
         */
    }

    /**
     * dynamic Table ID
     */
    private String tableID;

    /**
     * Returns the tableID.
     * 
     * @return the tableID
     */
    public String getTableID() {
        return tableID;
    }

    /**
     * Sets the tableID.
     * 
     * @param tableID
     *            the tableID to set
     */
    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    /**
     * preferences Keys Map to be used for reading preferences.
     */
    private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
    static {
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.SERVICE_NAME,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.HOST_NAME, NodeType.HOST);

        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_PREF,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_PREF,
                NodeType.HOST);
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
            String errorMsg = new PreferencesException().getMessage();
            handleInfo(errorMsg);
            // setError(true);
            // setErrorMessage(errorMsg);
            // LOGGER.error(errorMsg);
            return;
        }
        // get the required data from SubpageIntegrator
        selectedNodeType = subpageIntegrator.getNodeType();
        selectedNodeId = subpageIntegrator.getNodeID();
        selectedNodeName = subpageIntegrator.getNodeName();
        inStatusViewer = subpageIntegrator.isInStatusViewer();

        LOGGER.debug("[Performance Measurement Portlet] # Node Type ["
                + selectedNodeType + "] # Node Name [" + selectedNodeName
                + "] # Node ID [" + selectedNodeId + "] # In Status Viewer ["
                + inStatusViewer + "]");
    }

    /**
     * This method is responsible to get rrd related data from web service and
     * create rrd graph list.
     */
    private void createRrdGraph() {
        // re-initialize the bean so as to reload UI
        this.setMessage(false);
        this.setError(false);
        this.setInfo(false);

        /**
         * Name of the host for which performance RRD graphs are to be shown.
         */
        String hostName;

        /**
         * Name of the service for which performance RRD graphs are to be shown.
         */
        String serviceName;

        try {
            if (selectedNodeType == null) {
                throw new Exception(
                        "createRrdGraph(): Cannot instantiate RRD graph generation. Subpage node-type is null.");
            }
            /*
             * get the node type. If node type host then set host name as node
             * name and set service name as null. If node type is service then,
             * set service name as node name and host name as parent host name
             * service .
             */
            // check if portlet is in dash board apply preference time filter.
            if (!inStatusViewer) {
                setSelectedTimeFilterBean();
            }
            if (selectedNodeType.equals(NodeType.HOST)) {
                hostName = selectedNodeName;
                serviceName = null;

                SimpleHost simpleHost = null;
                try {
                    simpleHost = foundationWSFacade.getSimpleHostByName(
                            selectedNodeName, false);
                    if (simpleHost == null) {
                        throw new WSDataUnavailableException();
                    }
                } catch (WSDataUnavailableException e) {
                    String hostNotAvailableErrorMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_hostUnavailable")
                            + " [" + selectedNodeName + "]";
                    LOGGER.error(hostNotAvailableErrorMessage);
                    handleInfo(hostNotAvailableErrorMessage);
                    return;
                }

                // for dashboard, check for extended role permissions
                if (!inStatusViewer
                        && !referenceTreeModel
                                .checkNodeForExtendedRolePermissions(simpleHost
                                        .getHostID(), NodeType.HOST, hostName,
                                        userExtendedRoleBean
                                                .getExtRoleHostGroupList(),
                                        userExtendedRoleBean
                                                .getExtRoleServiceGroupList())) {
                    String inadequatePermissionsMessage = ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                            + " [" + hostName + "]";
                    handleInfo(inadequatePermissionsMessage);
                    return;
                }

            } else if (selectedNodeType.equals(NodeType.SERVICE)) {
                ServiceStatus service = null;
                if (inStatusViewer) {
                    // retrieve service by using id
                    service = foundationWSFacade
                            .getServicesById(selectedNodeId);
                    if (service == null) {
                        // Service with given ID does not exist - Portlet cannot
                        // recover
                        throw new WSDataUnavailableException();
                    }

                    // Setting host associated with service
                    hostName = service.getHost().getName();

                    // set service as selected node name
                    serviceName = selectedNodeName;

                } else {
                    if (null != facesContext) {
                        FacesUtils.setFacesContext(facesContext);
                    }

                    // use preferences - host name and service name
                    Map<String, String> servicePortletPreferences = PortletUtils
                            .getServicePortletPreferences();
                    hostName = servicePortletPreferences
                            .get(PreferenceConstants.HOST_NAME);
                    serviceName = servicePortletPreferences
                            .get(PreferenceConstants.SERVICE_NAME);

                    try {
                        service = foundationWSFacade
                                .getServiceByHostAndServiceName(hostName,
                                        serviceName);

                        // check for extended role permissions
                        if (!inStatusViewer
                                && !referenceTreeModel
                                        .checkNodeForExtendedRolePermissions(
                                                service.getServiceStatusID(),
                                                NodeType.SERVICE,
                                                serviceName,
                                                userExtendedRoleBean
                                                        .getExtRoleHostGroupList(),
                                                userExtendedRoleBean
                                                        .getExtRoleServiceGroupList())) {
                            String inadequatePermissionsMessage = ResourceUtils
                                    .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                                    + " [" + serviceName + "]";
                            handleInfo(inadequatePermissionsMessage);
                            return;
                        }
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
                        // setting information message.
                        handleInfo(serviceNotAvailableErrorMessage);
                        return;

                    } catch (GWPortalGenericException e) {
                        LOGGER
                                .error("Error occured while initializing PerfMeasurement portlet in PerfMeasurementBean()");
                        handleError(e.getMessage());
                    }
                }

            } else {
                // Portlet placed in wrong sub-page. Cannot recover.
                throw new Exception(
                        "Perfromance Measurement Portlet is not applicable for Node Type ["
                                + selectedNodeType + "]");
            }

            long startDateInSec;
            long endDateInSec;
            int graphWidth;
            if (currentPerfTimeFilterbean != null) {

                try {
                    Date startDate = dateFormat.parse(currentPerfTimeFilterbean
                            .getStartDate());
                    startDateInSec = startDate.getTime()
                            / Constant.ONE_THOUSEND;
                    if ("-1".equalsIgnoreCase(timepref)) {
                        Date endDate = dateFormat
                                .parse(currentPerfTimeFilterbean.getEndDate());
                        endDateInSec = endDate.getTime()
                                / Constant.ONE_THOUSEND;
                    } else {
                        // getting current time in second
                        endDateInSec = Calendar.getInstance().getTimeInMillis()
                                / Constant.ONE_THOUSEND;
                    }
                } catch (ParseException e) {
                    LOGGER
                            .error("parse Exception while parsing start date or End date in getRrdGraphList() method.Hence setting end date as current date-time and start date is before 2 hour of end date-time  ");
                    // getting current time in second
                    endDateInSec = Calendar.getInstance().getTimeInMillis()
                            / Constant.ONE_THOUSEND;
                    startDateInSec = endDateInSec - Constant.START_TIME_DIFF;

                }
                graphWidth = currentPerfTimeFilterbean.getWidth();
                graphWidth = getGraphWidth(graphWidth);

            } else {
                LOGGER
                        .info("currentPerfTimeFilterbean is null .Hence setting end date as current date-time and start date is before 2 hour of end date-time and grapth odth is 900.");
                // getting current time in second
                endDateInSec = Calendar.getInstance().getTimeInMillis()
                        / Constant.ONE_THOUSEND;
                startDateInSec = endDateInSec - Constant.START_TIME_DIFF;
                graphWidth = Constant.DEFAULT_GRAPTH_WIDTH;
            }

            if (rrdGraphList.isEmpty()) {
                // long startTime = System.currentTimeMillis();
                RRDGraph[] rrdGraphs = foundationWSFacade.getRrdGraph(hostName,
                        serviceName, startDateInSec, endDateInSec,
                        Constant.NAGIOS.toUpperCase(), graphWidth);
                // LOGGER.info("RRD call  took "
                // + (System.currentTimeMillis() - startTime) + " ms");
                if (rrdGraphs != null && rrdGraphs.length > 0) {
                    if (NO_GRAPH_AVAILABLE.equalsIgnoreCase(rrdGraphs[0]
                            .getRrdLabel())) {
                        throw new GWPortalGenericException(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_no_graph_available_for_service_msg")
                                        + " [" + serviceName + "]");
                    }
                    boolean isExpanded = true;
                    for (int i = 0; i < rrdGraphs.length; i++) {
                        RrdGraphBean rrdGraphBean = new RrdGraphBean();
                        rrdGraphBean.setCollapsibleTitle(rrdGraphs[i]
                                .getRrdLabel());
                        rrdGraphBean.setRrdGraphBytes(rrdGraphs[i].getGraph());
                        rrdGraphBean.setExpanded(isExpanded);
                        rrdGraphList.add(rrdGraphBean);
                        collapsibleStatusMap.put(rrdGraphs[i].getRrdLabel(),
                                isExpanded);
                        isExpanded = false;
                    } // end for
                } else {
                    LOGGER.info("Graph is not avalible for hostname:- "
                            + hostName + " and service name:- " + serviceName);
                    // setting information message.
                    String informationMessage = "";
                    if (serviceName != null) {
                        informationMessage = "No graph available for "
                                + serviceName + " !";
                    } else {
                        informationMessage = "No graph available for "
                                + hostName + " !";
                    }
                    handleInfo(informationMessage);
                } // end if
                // sort rrdGraphList for service name
                sort();
            } else {
                // LOGGER
                // .debug("..........RRD Call for interval render...........");
                Set<Entry<String, Boolean>> entrySet = collapsibleStatusMap
                        .entrySet();
                Iterator<Entry<String, Boolean>> iterator = entrySet.iterator();
                while (iterator.hasNext()) {
                    Entry<String, Boolean> next = iterator.next();
                    String key = next.getKey();
                    Boolean value = next.getValue();
                    if (value) {
                        if (selectedNodeType == NodeType.HOST) {
                            StringTokenizer stringTokenizer = new StringTokenizer(
                                    key, Constant.COLON);
                            serviceName = stringTokenizer.nextToken();
                        }
                        RRDGraph[] rrdGraphsUpdated = foundationWSFacade
                                .getRrdGraph(hostName, serviceName,
                                        startDateInSec, endDateInSec,
                                        Constant.NAGIOS.toUpperCase(),
                                        graphWidth);
                        if (rrdGraphsUpdated != null
                                && rrdGraphsUpdated.length > 0) {
                            for (int i = 0; i < rrdGraphsUpdated.length; i++) {
                                RrdGraphBean rrdGraphBean = new RrdGraphBean();
                                rrdGraphBean.setCollapsibleTitle(key);
                                rrdGraphBean
                                        .setRrdGraphBytes(rrdGraphsUpdated[i]
                                                .getGraph());
                                rrdGraphBean.setExpanded(true);
                                int indexOf = getListIndex(key);
                                if (indexOf != -1) {
                                    rrdGraphList.remove(indexOf);
                                    rrdGraphList.add(indexOf, rrdGraphBean);
                                }

                            }
                        }
                    }
                }
            }

        } catch (WSDataUnavailableException e) {

            if (!isIntervalRender() || rrdGraphList.isEmpty()) {
                handleError(ResourceUtils
                        .getLocalizedMessage(Constant.PERF_MEASUREMENT_ERROR_MESSAGE));
                LOGGER
                        .warn("WSDataUnavailableException in createRrdGraph method:- "
                                + e);
            }
            // else {
            // LOGGER
            // .warn(
            // "ignoring  Interrupted command exception while interval render");
            // }
        } catch (GWPortalGenericException e) {
            LOGGER.warn(e.getMessage());
            handleInfo(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exception in createRrdGraph method:- " + e);
            handleError("No graph available !");
        }
        setRrdGraphList(rrdGraphList);

    }

    /**
     * set start and end current time depending on saved preference.
     */
    private void setSelectedTimeFilterBean() {
        if (currentPerfTimeFilterbean == null) {
            currentPerfTimeFilterbean = new PerfMeasurementIPCBean();
        }
        if (isCustomDate()) {

            currentPerfTimeFilterbean.setStartDate(getCustStartDate().concat(
                    ":00"));
            currentPerfTimeFilterbean
                    .setEndDate(getCustEndDate().concat(":00"));
        } else {
            currentPerfTimeFilterbean
                    .setStartDate(getStartDate(getOldSelectedTime()));
            currentPerfTimeFilterbean.setEndDate(dateFormat.format(Calendar
                    .getInstance().getTime()));
        }
        currentPerfTimeFilterbean.setWidth(Constant.DEFAULT_GRAPTH_WIDTH);
    }

    /**
     * return graph width .
     * 
     * @param graphWidth
     * @return int
     */
    private int getGraphWidth(int graphWidth) {
        // reduce perf measurement graph width by 7.5 percent to align
        // availability and perf measurement portlets
        if (graphWidth != 0 && graphWidth != GRAPH_WIDTH_400) {
            int reducewidth = (int) ((graphWidth * PERF_GRAPH_REDUCE_PERCENT) / HUNDREN);
            graphWidth = graphWidth - reducewidth;
        }
        return graphWidth;
    }

    /**
     * Sets the rrdGraphList.
     * 
     * @param rrdGraphList
     *            the rrdGraphList to set
     */
    public void setRrdGraphList(List<RrdGraphBean> rrdGraphList) {
        this.rrdGraphList = rrdGraphList;
    }

    /**
     * Returns the rrdGraphList.
     * 
     * @return the rrdGraphList
     */
    public List<RrdGraphBean> getRrdGraphList() {
        // LOGGER
        // .debug("........RRDPortlet : getRrdGraphList............");
        return rrdGraphList;
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
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {
        if (selectedNodeType == null) {
            return;
        }

        this.createRrdGraph();

    }

    /**
     * Method that will be called on click of "panelCollapsible" header.
     * 
     * @param event
     */
    public void collapsibleAction(ActionEvent event) {
        // Retrieve parameters
        if (selectedNodeType == NodeType.HOST) {
            String serviceName = null;
            String collapsibleTitle = (String) event.getComponent()
                    .getAttributes().get(COLLAPSIBLE_TITLE);
            if (collapsibleTitle != null) {
                if (!collapsibleTitle.contains(Constant.COLON)) {
                    collapsibleStatusMap.put(collapsibleTitle, true);

                } else {
                    StringTokenizer stringTokenizer = new StringTokenizer(
                            collapsibleTitle, Constant.COLON);
                    serviceName = stringTokenizer.nextToken();
                    if (collapsibleStatusMap.containsKey(collapsibleTitle)) {
                        if (!collapsibleStatusMap.get(collapsibleTitle)) {

                            try {
                                RrdGraphBean rrdGraphForService = getRrdGraphForService(
                                        selectedNodeName, serviceName);
                                if (rrdGraphForService != null) {
                                    rrdGraphForService
                                            .setCollapsibleTitle(collapsibleTitle);
                                    rrdGraphForService.setExpanded(true);
                                }

                                int indexOf = getListIndex(collapsibleTitle);
                                if (indexOf != -1) {
                                    rrdGraphList.remove(indexOf);
                                    rrdGraphList.add(indexOf,
                                            rrdGraphForService);
                                }
                                collapsibleStatusMap
                                        .put(collapsibleTitle, true);

                            } catch (WSDataUnavailableException e) {
                                LOGGER
                                        .error("WSDataUnavailableException in getRrdGraphForService method:- "
                                                + e);
                            }

                        } else {
                            byte[] emptyArray = {};
                            RrdGraphBean emptyRrdGraphBean = new RrdGraphBean();
                            emptyRrdGraphBean
                                    .setCollapsibleTitle(collapsibleTitle);
                            emptyRrdGraphBean.setRrdGraphBytes(emptyArray);
                            emptyRrdGraphBean.setExpanded(false);
                            int indexOf = getListIndex(collapsibleTitle);
                            if (indexOf != -1) {
                                rrdGraphList.remove(indexOf);
                                rrdGraphList.add(indexOf, emptyRrdGraphBean);
                            }
                            collapsibleStatusMap.put(collapsibleTitle, false);
                        }
                    }
                }
            }
        }

        // }

        setIntervalRender(false);

    }

    /**
     * return list index depending on collapsibleTitle.
     * 
     * @param collapsibleTitle
     * @return int
     */
    private int getListIndex(String collapsibleTitle) {
        int indexOf = -1;
        Iterator<RrdGraphBean> iterator = rrdGraphList.iterator();
        while (iterator.hasNext()) {
            RrdGraphBean next = iterator.next();
            if (next != null
                    && next.getCollapsibleTitle().equalsIgnoreCase(
                            collapsibleTitle)) {
                indexOf = rrdGraphList.indexOf(next);
                break;

            }
        }
        return indexOf;
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
     * Sets the perfHiddenField.
     * 
     * @param perfHiddenField
     *            the perfHiddenField to set
     */
    public void setPerfHiddenField(String perfHiddenField) {
        this.perfHiddenField = perfHiddenField;
    }

    /**
     * Returns the perfHiddenField.
     * 
     * @return the perfHiddenField
     */
    public String getPerfHiddenField() {
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
                // subpage - update node type vals
                setIntervalRender(true);

                // clear RRD graph list
                rrdGraphList.clear();
            }
        }

        StateController stateController = subpageIntegrator
                .getStateController();
        currentPerfTimeFilterbean = stateController.getCurrentPerfTimeFilter();
        // IPC is not applicable On dash board hence currentPerfTimeFilterbean
        // will be null
        if (currentPerfTimeFilterbean == null) {
            if (isIntervalRender() && !isInfo()) {
                // LOGGER
                // .debug("In getPerfHiddenField() ... PERF MEASUREMENT PORTLET")
                // ;
                this.createRrdGraph();

            }
        } else {
            PerfMeasurementTimeBean perfMeasurementTimeBean = (PerfMeasurementTimeBean) FacesUtils
                    .getManagedBean("perfMeasurementTimeBean");
            if (perfMeasurementTimeBean != null) {
                // check previous start time and end time with current start
                // time
                // and end time to avoid web service call .
                if (isIntervalRender()
                        || !perfMeasurementTimeBean.getPreviousStartTime()
                                .equalsIgnoreCase(
                                        currentPerfTimeFilterbean
                                                .getStartDate())
                        || !perfMeasurementTimeBean.getPreviousEndTime()
                                .equalsIgnoreCase(
                                        currentPerfTimeFilterbean.getEndDate())) {
                    // LOGGER
                    // .debug(
                    // "In getPerfHiddenField() ... PERF MEASUREMENT PORTLET");
                    this.createRrdGraph();

                    perfMeasurementTimeBean
                            .setPreviousStartTime(currentPerfTimeFilterbean
                                    .getStartDate());
                    perfMeasurementTimeBean
                            .setPreviousEndTime(currentPerfTimeFilterbean
                                    .getEndDate());

                }

            }
        }
        setIntervalRender(false);
        stateController = null;
        return perfHiddenField;
    }

    /**
     * return rrd graph bean for service.
     * 
     * @param hostName
     * @param serviceName
     * @return RrdGraphBean
     * @throws WSDataUnavailableException
     */
    public RrdGraphBean getRrdGraphForService(String hostName,
            String serviceName) throws WSDataUnavailableException {
        RrdGraphBean rrdGraphBean = null;
        long startDateInSec;
        long endDateInSec;
        int graphWidth;
        // check if portlet is in dash board apply preference time filter.
        if (!inStatusViewer) {
            setSelectedTimeFilterBean();
        }
        if (currentPerfTimeFilterbean != null) {

            try {
                Date startDate = dateFormat.parse(currentPerfTimeFilterbean
                        .getStartDate());
                startDateInSec = startDate.getTime() / Constant.ONE_THOUSEND;

                if ("-1".equalsIgnoreCase(timepref)) {
                    Date endDate = dateFormat.parse(currentPerfTimeFilterbean
                            .getEndDate());
                    endDateInSec = endDate.getTime() / Constant.ONE_THOUSEND;
                } else {
                    // getting current time in second
                    endDateInSec = Calendar.getInstance().getTimeInMillis()
                            / Constant.ONE_THOUSEND;
                }

            } catch (ParseException e) {
                LOGGER
                        .error("parse Exception while parsing start date or End date in getRrdGraphList() method.Hence setting end date as current date-time and start date is before 2 hour of end date-time  ");
                // getting current time in second
                endDateInSec = Calendar.getInstance().getTimeInMillis()
                        / Constant.ONE_THOUSEND;
                startDateInSec = endDateInSec - Constant.START_TIME_DIFF;

            }
            graphWidth = currentPerfTimeFilterbean.getWidth();
            graphWidth = getGraphWidth(graphWidth);

        } else {
            LOGGER
                    .info("currentPerfTimeFilterbean is null .Hence setting end date as current date-time and start date is before 2 hour of end date-time and grapth odth is 900.");
            // getting current time in second
            endDateInSec = Calendar.getInstance().getTimeInMillis()
                    / Constant.ONE_THOUSEND;
            startDateInSec = endDateInSec - Constant.START_TIME_DIFF;
            graphWidth = Constant.DEFAULT_GRAPTH_WIDTH;
        }

        RRDGraph[] rrdGraphs = foundationWSFacade.getRrdGraph(hostName,
                serviceName, startDateInSec, endDateInSec, Constant.NAGIOS
                        .toUpperCase(), graphWidth);
        if (rrdGraphs != null && rrdGraphs.length > 0) {

            for (int i = 0; i < rrdGraphs.length; i++) {
                rrdGraphBean = new RrdGraphBean();
                rrdGraphBean.setCollapsibleTitle(rrdGraphs[i].getRrdLabel());
                rrdGraphBean.setRrdGraphBytes(rrdGraphs[i].getGraph());
            }
        }

        return rrdGraphBean;
    }

    /**
     * Sets the expanded.
     * 
     * @param expanded
     *            the expanded to set
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * Returns the expanded.
     * 
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Returns the paddingStyle.
     * 
     * @return the paddingStyle
     */
    public String getPaddingStyle() {
        paddingStyle = "padRight105";
        boolean inDashboard = PortletUtils.isInDashbord();
        if (inDashboard) {
            paddingStyle = "emptyClass";
        }

        return paddingStyle;
    }

    /**
     * Sets the paddingStyle.
     * 
     * @param paddingStyle
     *            the paddingStyle to set
     */
    public void setPaddingStyle(String paddingStyle) {
        this.paddingStyle = paddingStyle;
    }

    /**
     * Return start date as string depending on hours
     * 
     * @param hour
     * @return String
     */
    public String getStartDate(String hour) {
        TimeIntervalEnumEE timeIntervalEnum = TimeIntervalEnumEE
                .getTimeIntervalEnum(hour);
        // Get Calendar instance
        Calendar calendar = Calendar.getInstance();
        if (timeIntervalEnum == TimeIntervalEnumEE.TODAY) {
            // Set the calendar time to start of current day i.e. 00:00:00
            calendar.set(calendar.get(Calendar.YEAR), calendar
                    .get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
            // set start date string
            return dateFormat.format(calendar.getTime());

        } else {
            calendar.add(Calendar.DATE, timeIntervalEnum.getValueToAdd());
            // set start date string
            return dateFormat.format(calendar.getTime());

        }

    }

    /**
     * sort rrdGraphList in ascending order
     * 
     * @param treeNodeList
     */
    private void sort() {
        Comparator<RrdGraphBean> comparator = new Comparator<RrdGraphBean>() {
            public int compare(RrdGraphBean entity1, RrdGraphBean entity2) {
                String name1 = entity1.getCollapsibleTitle();

                String name2 = entity2.getCollapsibleTitle();
                // For sort order ascending -

                return name1.compareTo(name2);

            }
        };
        // sort the group List
        Collections.sort(rrdGraphList, comparator);

    }

    /**
     * Sets the selectedTime.
     * 
     * @param selectedTime
     *            the selectedTime to set
     */
    public void setSelectedTime(String selectedTime) {
        this.selectedTime = selectedTime;
    }

    /**
     * Returns the selectedTime.
     * 
     * @return the selectedTime
     */
    public String getSelectedTime() {
        return selectedTime;
    }

    /**
     * Sets the timeSelectorList.
     * 
     * @param timeSelectorList
     *            the timeSelectorList to set
     */
    public void setTimeSelectorList(List<SelectItem> timeSelectorList) {
        this.timeSelectorList = timeSelectorList;
    }

    /**
     * Returns the timeSelectorList.
     * 
     * @return the timeSelectorList
     */
    public List<SelectItem> getTimeSelectorList() {
        return timeSelectorList;
    }

    /**
     * Populates the time selector list.
     * 
     * @return timeSelectorList
     */
    public List<SelectItem> initializeTimeSelectors() {
        timeSelectorList = new ArrayList<SelectItem>();
        for (TimeIntervalEnumEE timeIntervalEnum : TimeIntervalEnumEE.values()) {
            timeSelectorList.add(new SelectItem(timeIntervalEnum.getValue(),
                    timeIntervalEnum.getLabel()));
        }

        return timeSelectorList;
    }

    /**
     * Method that will be called on click of "Apply" button on time filter.
     * 
     * @param event
     */
    public void applyTimeFilter(ActionEvent event) {
        setOldSelectedTime(getSelectedTime());
        if ("-1".equalsIgnoreCase(getSelectedTime())) {
            setCustomDate(true);
        } else {
            setCustomDate(false);
        }
        this.createRrdGraph();

    }

    /**
     * Returns the inStatusViewer.
     * 
     * @return the inStatusViewer
     */
    public boolean isInStatusViewer() {
        return inStatusViewer;
    }

    /**
     * Sets the inStatusViewer.
     * 
     * @param inStatusViewer
     *            the inStatusViewer to set
     */
    public void setInStatusViewer(boolean inStatusViewer) {
        this.inStatusViewer = inStatusViewer;
    }

    /**
     * Sets the custStartDate.
     * 
     * @param custStartDate
     *            the custStartDate to set
     */
    public void setCustStartDate(String custStartDate) {
        this.custStartDate = custStartDate;
    }

    /**
     * Returns the custStartDate.
     * 
     * @return the custStartDate
     */
    public String getCustStartDate() {
        return custStartDate;
    }

    /**
     * Sets the custEndDate.
     * 
     * @param custEndDate
     *            the custEndDate to set
     */
    public void setCustEndDate(String custEndDate) {
        this.custEndDate = custEndDate;
    }

    /**
     * Returns the custEndDate.
     * 
     * @return the custEndDate
     */
    public String getCustEndDate() {
        return custEndDate;
    }

    /**
     * Sets the oldSelectedTime.
     * 
     * @param oldSelectedTime
     *            the oldSelectedTime to set
     */
    public void setOldSelectedTime(String oldSelectedTime) {
        this.oldSelectedTime = oldSelectedTime;
    }

    /**
     * Returns the oldSelectedTime.
     * 
     * @return the oldSelectedTime
     */
    public String getOldSelectedTime() {
        return oldSelectedTime;
    }

    /**
     * Sets the customDate.
     * 
     * @param customDate
     *            the customDate to set
     */
    public void setCustomDate(boolean customDate) {
        this.customDate = customDate;
    }

    /**
     * Returns the customDate.
     * 
     * @return the customDate
     */
    public boolean isCustomDate() {
        return customDate;
    }

    /**
     * @param event
     */
    public void selectedTimeChangeListener(ValueChangeEvent event) {

        String newSelectedTime = (String) event.getNewValue();

        if ("-1".equalsIgnoreCase(newSelectedTime)) {

            initializeDates();

            setRenderCustDates(true);
        } else {
            setRenderCustDates(false);
        }
        setOldSelectedTime(getSelectedTime());

        // setSelectedTime(newSelectedTime);
    }

    /**
     * initialize custom start date and end date start date- < current date > <
     * 00:00 > End date- < current date > < current time >
     */
    private void initializeDates() {
        Calendar calendar = Calendar.getInstance();
        setCustEndDate(custDateFormat.format(calendar.getTime()));
        setCustStartDate(dateOnlyFormat.format(calendar.getTime()) + " 00:00");
    }

    /**
     * This method validates the startTime field. 1) Should be non-empty. 2)
     * Format:mm/dd/yyyy hh:mm:ss 3) Must be > current time 4) must be < endTime
     * 
     * @param context
     * @param component
     * @param value
     */
    public void validateStartDateTime(FacesContext context,
            UIComponent component, Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_start_datetime_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_start_datetime_non_empty_value"),
                                context, component);
                return;
            }
            // check for date format.
            if (!ValidationUtils.isValidDateFormat(inputString,
                    DATE_FORMAT_24_HR)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate_format"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate_format"),
                                context, component);
                return;
            }
            /*
             * Check for the valid values of the
             * date,month,year,hours,minutes,seconds.
             */
            if (!ValidationUtils.validateDateTimeFields(inputString,
                    DATE_FORMAT_24_HR)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate"),
                                context, component);
                return;
            }
            // Check if the input date < endTime
            if (!ValidationUtils.isPastDate(inputString, getCustEndDate(),
                    dateFormat)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_less_startDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_less_startDate"),
                                context, component);
                return;
            }

        } // (value != null)

    }

    /**
     * This method validates the startTime field. 1) Should be non-empty. 2)
     * Format:mm/dd/yyyy hh:mm:ss 3) Must be > current time 4) must be < endTime
     * 
     * @param context
     * @param component
     * @param value
     */
    public void validateEndDateTime(FacesContext context,
            UIComponent component, Object value) {
        if (value != null) {
            String inputString = (String) value;
            // Check for blank input
            if (ValidationUtils.checkForBlankValue(inputString)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_end_datetime_non_empty_value"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_end_datetime_non_empty_value"),
                                context, component);
                return;
            }
            // check for date format.
            if (!ValidationUtils.isValidDateFormat(inputString,
                    DATE_FORMAT_24_HR)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate_format"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate_format"),
                                context, component);
                return;
            }
            /*
             * Check for the valid values of the
             * date,month,year,hours,minutes,seconds.
             */
            if (!ValidationUtils.validateDateTimeFields(inputString,
                    DATE_FORMAT_24_HR)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate"),
                                context, component);
                return;
            }
            // Check if the input date < startTime
            if (ValidationUtils.isPastDate(inputString, getCustStartDate(),
                    custDateFormat)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_greater_endDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_greater_endDate"),
                                context, component);
                return;
            }

        } // (value != null)

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlMessage) {
        // TODO Method not implemented yet: ServerPush.refresh(...) is not
        // implemented by swapnil_gujrathi
    }

    /**
     * Sets the perfMeasurementFrmID.
     * 
     * @param perfMeasurementFrmID
     *            the perfMeasurementFrmID to set
     */
    public void setPerfMeasurementFrmID(String perfMeasurementFrmID) {
        this.perfMeasurementFrmID = perfMeasurementFrmID;
    }

    /**
     * Returns the perfMeasurementFrmID.
     * 
     * @return the perfMeasurementFrmID
     */
    public String getPerfMeasurementFrmID() {
        return perfMeasurementFrmID;
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

}
