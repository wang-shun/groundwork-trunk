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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.portlet.PortletSession;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.StateTransition;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.statusviewer.bean.AvailabilityBean;
import com.groundworkopensource.portal.statusviewer.bean.PerfMeasurementIPCBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.bean.StateTransitionBean;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.TimeIntervalEnumEE;
import com.groundworkopensource.portal.statusviewer.common.ValidationUtils;

/**
 * This class retrieves service state transitions data from wsEvent web service
 * and generates a stacked bar chart out of it.
 * 
 * FIXME This is complete duplicate code. Should use HostAvailabilityHandler.
 * SHIVANGI please remove duplication afterwards. There should be just one
 * handler for Availability.
 * 
 * @author shivangi_walvekar
 * 
 */
public class ServiceAvailabilityHandler extends ServerPush implements
        Serializable {

    /**
     * ONE HOUR IN MILLISECONDS.
     */
    private static final int ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

    /**
     * HOURS_IN_A_DAY
     */
    private static final int HOURS_IN_A_DAY = 24;

    /**
     * DATE_DIFF_168_HOURS
     */
    private static final int DATE_DIFF_168_HOURS = 168;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7706940467139272946L;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(ServiceAvailabilityHandler.class.getName());

    /**
     * Error boolean to set if error occurred
     */
    private boolean error = false;

    /**
     * Id for form UI component
     */
    private String serviceAvailabilityFrmID;

    /**
     * ReferenceTreeMetaModel instance
     * <p>
     * !!!!!!!!!!! IMP !!!!!!!!!! : Please do not remove below declaration of
     * referenceTreeModel.
     */
    private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            .getManagedBean(Constant.REFERENCE_TREE);

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
     * info boolean to set if any information is to be displayed on UI.
     */
    private boolean info = false;

    /**
     * @return info
     */
    public boolean isInfo() {
        return info;
    }

    /**
     * @param info
     */
    public void setInfo(boolean info) {
        this.info = info;
    }

    /**
     * Boolean to indicate if any message is to be set to display on UI.
     */
    private boolean message = false;

    /**
     * @return message
     */
    public boolean isMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(boolean message) {
        this.message = message;
    }

    /**
     * Error message to be shown on UI,in case of errors/exceptions
     */
    private String errorMessage;

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
     * Old Start date string - before computations for the date axis.
     */
    private String oldStartDateString;

    /**
     * @return oldStartDateString
     */
    public String getOldStartDateString() {
        return oldStartDateString;
    }

    /**
     * @param oldStartDateString
     */
    public void setOldStartDateString(String oldStartDateString) {
        this.oldStartDateString = oldStartDateString;
    }

    /**
     * information message to show on UI
     */
    private String infoMessage;

    /**
     * @return infoMessage
     */
    public String getInfoMessage() {
        return infoMessage;
    }

    /**
     * @param infoMessage
     */
    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    /**
     * Time selected from 'time selector' drop-down.
     */
    private String selectedTime;

    /**
     * List of selectItems for time selector component.
     */
    private List<SelectItem> timeSelectorList;

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
     * boolean variable for custom dates
     */
    private boolean customDate = false;

    /**
     * subpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

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
     * previous custom End date
     */
    private String custOldEndDate;

    /**
     * Returns the custOldEndDate.
     * 
     * @return the custOldEndDate
     */
    public String getCustOldEndDate() {
        return custOldEndDate;
    }

    /**
     * Sets the custOldEndDate.
     * 
     * @param custOldEndDate
     *            the custOldEndDate to set
     */
    public void setCustOldEndDate(String custOldEndDate) {
        this.custOldEndDate = custOldEndDate;
    }

    /**
     * Returns the custOldStartDate.
     * 
     * @return the custOldStartDate
     */
    public String getCustOldStartDate() {
        return custOldStartDate;
    }

    /**
     * Sets the custOldStartDate.
     * 
     * @param custOldStartDate
     *            the custOldStartDate to set
     */
    public void setCustOldStartDate(String custOldStartDate) {
        this.custOldStartDate = custOldStartDate;
    }

    /**
     * previous custom start date
     */
    private String custOldStartDate;
    /**
     * Previous Time selected from 'time selector' drop-down.
     */
    private String oldSelectedTime;

    /**
     * Flag to identify if portlet is placed in StatusViewer sub-pages apart
     * from Network View.
     */
    private boolean inStatusViewer;

    /**
     * ChartHandler reference
     */
    private ChartHandler chartHandler;

    /**
     * Value axis for the host availability bar chart.
     */
    private DateAxis dateAxis;

    /**
     * @return dateAxis
     */
    public DateAxis getDateAxis() {
        return dateAxis;
    }

    /**
     * @param dateAxis
     */
    public void setDateAxis(DateAxis dateAxis) {
        this.dateAxis = dateAxis;
    }

    /**
     * @return chartHandler
     */
    public ChartHandler getChartHandler() {
        return chartHandler;
    }

    /**
     * @param chartHandler
     */
    public void setChartHandler(ChartHandler chartHandler) {
        this.chartHandler = chartHandler;
    }

    /**
     * SimpleDateFormat
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            Constant.DATE_FORMAT_24_HR_CLK);

    /**
     * @return timeSelectorList
     */
    public List<SelectItem> getTimeSelectorList() {
        return timeSelectorList;
    }

    /**
     * @param timeSelectorList
     */
    public void setTimeSelectorList(List<SelectItem> timeSelectorList) {
        this.timeSelectorList = timeSelectorList;
    }

    /**
     * @return selectedTime
     */
    public String getSelectedTime() {
        return selectedTime;
    }

    /**
     * @param selectedTime
     */
    public void setSelectedTime(String selectedTime) {
        this.selectedTime = selectedTime;
    }

    /**
     * Instance of FoundationWSFacade
     */
    private final FoundationWSFacade foundationWSFacade;

    /**
     * integer constant for 11
     */
    public static final int ELEVEN = 11;

    /**
     * integer constant for width of the bar chart
     */
    public static final int BAR_CHART_WIDTH = 820;

    /**
     * integer constant for height of the bar chart
     */
    // public static final int BAR_CHART_HEIGHT = 130;
    public static final int BAR_CHART_HEIGHT = 100;

    /**
     * constant for maximum width of the bars in bar chart
     */
    public static final double MAX_BAR_WIDTH = 0.2;

    /**
     * constant for minimum width of the bars in bar chart
     */
    public static final double MIN_BAR_WIDHT = 0.1;

    /**
     * constant for item margin for the bar chart
     */
    // public static final double ITEM_MARGIN = 0.5;
    public static final double ITEM_MARGIN = 0.3;

    /**
     * Start date string for state transitions
     */
    private String startDateString;

    /**
     * End date string for state transitions
     */
    private String endDateString;

    /**
     * Start date for state transitions
     */
    private Calendar startDate;

    /**
     * End date for state transitions
     */
    private Calendar endDate;

    /**
     * paddingStyle to padding right in status viewer
     */
    private String paddingStyle = Constant.EMPTY_STRING;

    /**
     * hidden field
     */
    private String hiddenField = Constant.HIDDEN;

    /**
     * dynamic table ID
     */
    private String tableID;

    /**
     * CUSTOM DATES SESSION ATTRIBUTE
     */
    private static final String SERVICE_CUSTOM_DATES_SESSION_ATTRIBUTE = "serviceCustomDates";

    /**
     * TIME FILTER SESSION ATTRIBUTE
     */
    private static final String TIME_FILTER_SESSION_ATTRIBUTE = "serviceSelectedTimeFilter";

    /**
     * 
     * @return hiddenField
     */
    public String getHiddenField() {
        boolean updatePerfTimeFilter = false;
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
                // Set the default selected time as TODAY
                setSelectedTime(TimeIntervalEnumEE.TODAY.getValue());
                setOldSelectedTime(TimeIntervalEnumEE.TODAY.getValue());
                setCustomDate(false);
                updatePerfTimeFilter = true;

            }
        }
        if (!isInfo() && isIntervalRender()) {
            initialize(updatePerfTimeFilter);
        }
        setIntervalRender(false);
        return hiddenField;
    }

    /**
     * 
     * @param hiddenField
     */
    public void setHiddenField(String hiddenField) {
        this.hiddenField = hiddenField;
    }

    /**
     * @return startDate
     */
    public Calendar getStartDate() {
        return startDate;
    }

    /**
     * @param startDate
     */
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * @return endDate
     */
    public Calendar getEndDate() {
        return endDate;
    }

    /**
     * @param endDate
     */
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     * @return startDateString
     */
    public String getStartDateString() {
        return startDateString;
    }

    /**
     * @param startDateString
     */
    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    /**
     * @return endDateString
     */
    public String getEndDateString() {
        return endDateString;
    }

    /**
     * @param endDateString
     */
    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    /**
     * Path of the icon to be displayed for current
     */
    private String iconPath;

    /**
     * @return iconPath
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * @param iconPath
     */
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * Node Type - indicates current node type(host/Service/Service Group/Host
     * Group)
     */
    private NodeType currentNodeType;

    /**
     * @return currentNodeType
     */
    public NodeType getCurrentNodeType() {
        return currentNodeType;
    }

    /**
     * @param currentNodeType
     */
    public void setCurrentNodeType(NodeType currentNodeType) {
        this.currentNodeType = currentNodeType;
    }

    /**
     * List of ServiceAvailabilityBeans
     */
    private List<AvailabilityBean> availabilityBeanList = Collections
            .synchronizedList(new ArrayList<AvailabilityBean>());

    /**
     * @return availabilityBeanList
     */
    public List<AvailabilityBean> getAvailabilityBeanList() {
        return availabilityBeanList;
    }

    /**
     * 
     * @param availabilityBeanList
     */
    public void setAvailabilityBeanList(
            List<AvailabilityBean> availabilityBeanList) {
        this.availabilityBeanList = availabilityBeanList;
    }

    /**
     * Current host
     */
    private Host currentHost;

    /**
     * @return currentHost
     */
    public Host getCurrentHost() {
        return currentHost;
    }

    /**
     * @param currentHost
     */
    public void setCurrentHost(Host currentHost) {
        this.currentHost = currentHost;
    }

    // CHART BYTE ARRAYS
    /**
     * Byte array to store service state transitions bar chart Image
     */
    private byte[] serviceTransitionsChartImage;

    /**
     * @return serviceTransitionsChartImage
     */
    public byte[] getServiceTransitionsChartImage() {
        return serviceTransitionsChartImage;
    }

    /**
     * @param serviceTransitionsChartImage
     */
    public void setServiceTransitionsChartImage(
            byte[] serviceTransitionsChartImage) {
        this.serviceTransitionsChartImage = serviceTransitionsChartImage;
    }

    /**
     * List of host state transitions
     */
    private List<StateTransitionBean> hostStateTranistionList = Collections
            .synchronizedList(new ArrayList<StateTransitionBean>());

    /**
     * This list hold the state transitions data for host plus the services on
     * this host. This data will be used to generate the bar chart.
     */
    private List<StateTransitionBean> serviceAvailabilityDataList = Collections
            .synchronizedList(new ArrayList<StateTransitionBean>());

    /**
     * @return serviceAvailabilityDataList
     */
    public List<StateTransitionBean> getServiceAvailabilityDataList() {
        return serviceAvailabilityDataList;
    }

    /**
     * @param serviceAvailabilityDataList
     */
    public void setServiceAvailabilityDataList(
            List<StateTransitionBean> serviceAvailabilityDataList) {
        this.serviceAvailabilityDataList = serviceAvailabilityDataList;
    }

    /**
     * @return hostStateTranistionList
     */
    public List<StateTransitionBean> getHostStateTranistionList() {
        return hostStateTranistionList;
    }

    /**
     * @param hostStateTranistionList
     */
    public void setHostStateTranistionList(
            List<StateTransitionBean> hostStateTranistionList) {
        this.hostStateTranistionList = hostStateTranistionList;
    }

    /**
     * Divisor to be used when diving the selected time range into equal
     * intervals.
     */
    private static final int DIVISOR_FOR_INTERVALS = 8;

    /**
     * This list maintains the list of time intervals between start date and end
     * date.
     */
    private List<String> timeIntervalList = Collections
            .synchronizedList(new ArrayList<String>());

    /**
     * @return timeIntervalList
     */
    public List<String> getTimeIntervalList() {
        return timeIntervalList;
    }

    /**
     * @param timeIntervalList
     */
    public void setTimeIntervalList(List<String> timeIntervalList) {
        this.timeIntervalList = timeIntervalList;
    }

    /**
     * HashMap for the monitor status and corresponding color for stacked bar
     * chart.
     */
    private static HashMap<String, Color> colorMap = new HashMap<String, Color>();

    /**
     * facesContext
     */
    private final FacesContext facesContext;

    /**
     * UserRoleBean instance.
     */
    private UserExtendedRoleBean userExtendedRoleBean;

    /**
     * Static initializer that creates a map for the monitor status and
     * corresponding color for stacked bar chart .
     */
    static {
        try {
            colorMap = ChartHandler.getColorMapForStackedBarChart();
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    /**
     * constructor
     */
    public ServiceAvailabilityHandler() {
        subpageIntegrator = new SubpageIntegrator();
        // initialize the faces context to be used in JMS thread
        facesContext = FacesContext.getCurrentInstance();
        this.foundationWSFacade = new FoundationWSFacade();
        // Instantiate ChartHandler
        chartHandler = new ChartHandler();

        int randomID = new Random().nextInt(Constant.TEN_HOUSANED);
        serviceAvailabilityFrmID = "serviceAvailabilityPortlet_frm" + randomID;

        // get the UserRoleBean managed instance
        userExtendedRoleBean = PortletUtils.getUserExtendedRoleBean();

        // handle subpage integration over here
        if (!handleSubpageIntegration()) {
            return;
        }
        // Populate the time selection drop down
        initializeTimeSelectors();
        // set the start and end time
        try {
            setTimeBounds();
        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
            return;
        }
        // setting time filter for perf measurement portlet.
        // inDashboard = PortletUtils.isInDashbord();
        if (subpageIntegrator.isInStatusViewer()) {
            setPerfMeasurementTimeFilter();

        } else {

            String strSelectedTime = (String) FacesUtils.getPortletSession(
                    false).getAttribute(TIME_FILTER_SESSION_ATTRIBUTE,
                    PortletSession.PORTLET_SCOPE);

            if (strSelectedTime != null) {
                // check for custom dates
                if ("-1".equalsIgnoreCase(strSelectedTime)) {

                    // getting custom dates from session
                    String customTime = (String) FacesUtils.getPortletSession(
                            false).getAttribute(
                            SERVICE_CUSTOM_DATES_SESSION_ATTRIBUTE,
                            PortletSession.PORTLET_SCOPE);

                    if (null != customTime) {
                        int dateIndexOf = customTime
                                .indexOf(Constant.CUSTOM_DATE_DELIMITERS);
                        // parsing custom start date and end date
                        if (dateIndexOf != -1) {
                            String customStartDate = customTime.substring(0,
                                    dateIndexOf);
                            String customEndDate = customTime.substring(
                                    dateIndexOf + Constant.SIX, customTime
                                            .length());

                            setCustStartDate(customStartDate);
                            setCustEndDate(customEndDate);
                            setCustOldStartDate(customStartDate);
                            setCustOldEndDate(customEndDate);

                        } else {
                            // initialize default custom start date and end date
                            // start date
                            initializeDates();
                        }
                    } else {
                        // initialize default custom start date and end date
                        // start date
                        initializeDates();
                    }

                    setCustomDate(true);
                    setCustOldStartDate(getCustStartDate());
                    setCustOldEndDate(getCustEndDate());
                }

                // Set the default selected time as TODAY
                setSelectedTime(strSelectedTime);
                setOldSelectedTime(strSelectedTime);
            }
        }
    }

    /**
     * preferences Keys Map to be used for reading preferences.
     */
    private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
    static {
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_PREF,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_PREF,
                NodeType.HOST);
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
        if (!isPrefSet) {
            /*
             * as this portlet is not applicable for "Network View", show the
             * error message to user. If it was in the "Network View", then we
             * would have to assign Node Type as NETWORK with NodeId as 0.
             */
            String prefMessage = new PreferencesException().getMessage();
            setMessage(true);
            setInfo(true);
            setInfoMessage(prefMessage);
            LOGGER.error(prefMessage);
            return false;
        }
        // get the required data from SubpageIntegrator
        selectedNodeType = subpageIntegrator.getNodeType();
        selectedNodeId = subpageIntegrator.getNodeID();
        selectedNodeName = subpageIntegrator.getNodeName();
        inStatusViewer = subpageIntegrator.isInStatusViewer();

        // for debugging purpose
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder(
                    "[Service Availability Portlet] # Node Type [").append(
                    selectedNodeType).append("] # Node Name [").append(
                    selectedNodeName).append("] # Node ID [").append(
                    selectedNodeId).append("] # In Status Viewer [").append(
                    inStatusViewer).append("]"));
        }

        return true;
    }

    /**
     * Initializes the portlet with the required data and generate the service
     * availability bar chart.
     */
    private void initialize(boolean updatePerfTime) {
        if (selectedNodeType == null) {
            return;
        }
        // cleanup
        cleanup();
        try {
            // check for custom dates
            if (isCustomDate()) {
                // Set the custom startDate,endDate
                setTimeBoundsForCustomDates();
            } else {

                // Set the startDate,endDate
                setTimeBounds();

            }
            if (updatePerfTime && subpageIntegrator.isInStatusViewer()) {
                setPerfMeasurementTimeFilter();
            }
            // Generate service availability bar chart
            showServiceAvailability();
        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
            return;
        }
    }

    /**
     * This method creates the customized DateAxis as per the time selected by
     * user.It sets the TickUnit and date format for the dates/time to be
     * displayed on the range axis of the stacked bar chart.
     * 
     * @param timeEnum
     * @throws GWPortalGenericException
     */
    private void createRangeAxis(TimeIntervalEnumEE timeEnum)
            throws GWPortalGenericException {
        DateAxis axis = new DateAxis();
        try {
            /*
             * Set the start time to 12:59:59 so that jfreeChart displays 12 AM
             * on the range axis.If the start time is set to 00:00:00 i.e.
             * midnight,jfreeChart does not display 12 AM tick-label on the
             * graph.
             */
            Calendar cal = Calendar.getInstance();
            cal.setTime(getStartDate().getTime());
            cal.add(Calendar.SECOND, -1);
            setStartDate(cal);

            // Set the maximum date for the rangeAxis.
            axis.setMaximumDate(dateFormat.parse(getEndDateString()));
            // Set the minimum date for the rangeAxis
            // axis.setMinimumDate(dateFormat.parse(getStartDateString()));
            axis.setMinimumDate(getStartDate().getTime());
        } catch (ParseException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
            throw new GWPortalGenericException();
        }
        DateTickUnit dateTickUnit = null;
        SimpleDateFormat sdf = null;
        if (timeEnum == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "divideTimeIntervals : Found null timeIntervalEnum");
            throw new GWPortalException();
        }
        // The selected time is TODAY.
        if (timeEnum == TimeIntervalEnumEE.TODAY) {
            int incrementBy = 0;
            Calendar cal = Calendar.getInstance();
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            if (hours > DIVISOR_FOR_INTERVALS) {
                incrementBy = hours / DIVISOR_FOR_INTERVALS;
            } else {
                incrementBy = Integer.parseInt(timeEnum.getValue());
            }
            dateTickUnit = new DateTickUnit(DateTickUnit.HOUR, incrementBy);
            sdf = new SimpleDateFormat(Constant.TIME_FORMAT);
        } else if
        // For all other values of selected time
        (!(timeEnum == TimeIntervalEnumEE.TODAY)) {
            // Selected time can be divided into 8 equal intervals of hours.
            if (timeEnum.isHours()) {
                int totalHours = Integer.parseInt(timeEnum.getValue());
                dateTickUnit = new DateTickUnit(DateTickUnit.HOUR,
                        (totalHours / DIVISOR_FOR_INTERVALS));
                sdf = new SimpleDateFormat(Constant.DATE_FORMAT_HOURS_ONLY);
            } else {
                // Selected time can be divided into 8 equal intervals of days.
                int totalDays = Integer.parseInt(timeEnum.getValue());
                dateTickUnit = new DateTickUnit(DateTickUnit.DAY,
                        (totalDays / DIVISOR_FOR_INTERVALS));
                sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
            }
        }
        // Set Tick unit
        axis.setTickUnit(dateTickUnit);
        axis.setAutoRange(true);
        // Set the date format
        axis.setDateFormatOverride(sdf);
        // Set lower margin.
        axis.setLowerMargin(0.0D);
        axis.setUpperMargin(0.0D);
        // Set the variable to the class level DateAxis variable.
        setDateAxis(axis);
    }

    /**
     * This method retrieves the state transitions data by web service calls,
     * creates a stacked bar chart,creates a byte array of the bar chart image
     * and renders it.
     * 
     * @throws GWPortalException
     */
    private void showServiceAvailability() throws GWPortalGenericException {
        try {
            createServiceAvailabilityData();
            // Generate bar chart
            JFreeChart barChart = generateBarChart(getServiceAvailabilityDataList());
            // chart to bytes
            byte[] encodedPNG = encodeAsPNG(barChart);

            if (encodedPNG == null || (encodedPNG.length == 0)) {
                handleError(
                        "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                        Constant.METHOD
                                + "showServiceAvailability() : byte array encodedPNG is null");
                return;
            }
            setServiceTransitionsChartImage(encodedPNG);

        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
            throw e;
        }

    }

    /**
     * Generates bar chart for the service state transitions
     * 
     * @throws GWPortalGenericException
     */
    private JFreeChart generateBarChart(
            List<StateTransitionBean> stateTransitions) {
        JFreeChart jfreeChart = null;
        if (stateTransitions == null || (stateTransitions.size() == 0)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER
                        .debug(Constant.METHOD
                                + "generateBarChart(): No state transitions available for : "
                                + selectedNodeName);
            }
        }
        DefaultCategoryDataset barChartDataSet = new DefaultCategoryDataset();
        try {
            if (stateTransitions != null) {
                for (StateTransitionBean stateTransitionBean : stateTransitions) {
                    if (stateTransitionBean == null) {
                        handleError(
                                "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                                Constant.METHOD
                                        + " generateBarChart() : Null stateTransitionBean");
                        return jfreeChart;
                    }
                    barChartDataSet.addValue(new Double(stateTransitionBean
                            .getTimeInState()), stateTransitionBean
                            .getToState(), stateTransitionBean.getEntityName());
                }
            }
        } catch (Exception ex) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    ex);
            return jfreeChart;
        }
        jfreeChart = createChart(barChartDataSet);
        return jfreeChart;
    }

    /**
     * 
     * @param barChart
     * @return encodeAsPNG - byte array of the generated PNG image
     * @throws GWPortalGenericException
     * @throws GWPortalGenericException
     */
    private byte[] encodeAsPNG(JFreeChart barChart)
            throws GWPortalGenericException {
        byte[] encodeAsPNG = null;
        if (barChart == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    "Found null barChart inside encodeAsPNG() method . SelectedNodeName =  "
                            + selectedNodeName);
            throw new GWPortalGenericException();
        }
        try {
            encodeAsPNG = ChartUtilities.encodeAsPNG(barChart
                    .createBufferedImage(BAR_CHART_WIDTH, BAR_CHART_HEIGHT));
        } catch (IOException ioEx) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    ioEx.getMessage());
        }
        return encodeAsPNG;
    }

    /**
     * This method creates the Jfeechart
     * 
     * @param categoryDataset
     * @return jfreeChart
     */
    private JFreeChart createChart(DefaultCategoryDataset categoryDataset) {
        // Create Jfree chart instance.
        JFreeChart jfreechart = ChartFactory.createStackedBarChart(
                Constant.EMPTY_STRING, Constant.EMPTY_STRING,
                Constant.EMPTY_STRING, categoryDataset,
                PlotOrientation.HORIZONTAL, false, true, false);

        // Get plot for the jfreechart
        CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
        categoryplot.setOutlineVisible(false);
        // Set the background color.
        categoryplot.setBackgroundPaint(Color.WHITE);

        // // Create the font for the domain axis labels.
        // Font fontForCategoryAxis = new Font(Constant.FONT_FOR_AXIS_LABELS,
        // Font.BOLD, Constant.TEN);

        // Create the font for the domain axis labels.
        Font fontForCategoryAxis = new Font(Constant.FONT_FOR_AXIS_LABELS,
                Font.PLAIN, Constant.TEN);

        CategoryAxis categoryAxis = categoryplot.getDomainAxis();
        categoryAxis.setCategoryMargin(ITEM_MARGIN);
        categoryAxis.setAxisLineVisible(false);

        // Set the tick label font for category axis.
        categoryAxis.setTickLabelFont(fontForCategoryAxis);

        // categoryplot.getDomainAxis().setCategoryMargin(ITEM_MARGIN);

        // Get the renderer for plot.
        StackedBarRenderer stackedBarRenderer = (StackedBarRenderer) categoryplot
                .getRenderer();
        stackedBarRenderer.setRenderAsPercentages(false);
        // Set maximum width for the bars.
        stackedBarRenderer.setMaximumBarWidth(MAX_BAR_WIDTH);

        // Get the number of milliseconds corresponding to the start date.
        Long l = getStartDate().getTimeInMillis();
        stackedBarRenderer.setBase(l);

        categoryplot.setRangeAxis(getDateAxis());
        // Create the font for the domain axis labels.
        Font fontForRangeAxis = new Font(Constant.FONT_FOR_AXIS_LABELS,
                Font.BOLD, Constant.EIGHT);
        // Set the font for range axis tick labels.
        categoryplot.getRangeAxis().setTickLabelFont(fontForRangeAxis);
        // Set the color of the vertical lines of date axis.
        categoryplot.setRangeGridlineStroke(new BasicStroke(
                Constant.RANGEAXIS_LINES_WIDTH));
        categoryplot.setRangeGridlinePaint(Color.black);
        categoryplot.getRangeAxis().setAxisLineVisible(false);
        // Set minimum width for the bars.
        stackedBarRenderer.setMinimumBarLength(MIN_BAR_WIDHT);

        stackedBarRenderer.setDrawBarOutline(false);
        // Do no show shadow.
        stackedBarRenderer.setShadowVisible(false);

        // Set the series colors
        getChartHandler().setStackedBarChartColors(stackedBarRenderer,
                colorMap, categoryDataset);
        return jfreechart;
    }

    /**
     * This method fetches the service state transitions data , generates a
     * jfree chart and creates byte array out of it.
     * 
     * @return in case of any errors - when we need to show error / info
     *         message, this will return false.
     * 
     * @throws GWPortalGenericException
     */
    private boolean createServiceAvailabilityData()
            throws GWPortalGenericException {
        ServiceStatus service = null;
        if (inStatusViewer) {
            // retrieve service by using id
            service = foundationWSFacade.getServicesById(selectedNodeId);

        } else {
            if (null != facesContext) {
                FacesUtils.setFacesContext(facesContext);
            }

            // use preferences - host name and service name to get the service
            Map<String, String> servicePortletPreferences = null;
            try {
                servicePortletPreferences = PortletUtils
                        .getServicePortletPreferences();
            } catch (PreferencesException preferencesException) {
                handleInfo(preferencesException.getMessage());
                return false;
            }

            String hostName = servicePortletPreferences
                    .get(PreferenceConstants.HOST_NAME);
            String serviceName = servicePortletPreferences
                    .get(PreferenceConstants.SERVICE_NAME);

            try {
                service = foundationWSFacade.getServiceByHostAndServiceName(
                        hostName, serviceName);
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
                return false;
            }
        }

        if (service == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "createServiceAvailabilityData() : services[0] is null.");
            throw new GWPortalGenericException();
        }

        // set the selected node Id here (seems weird but required for JMS Push
        // in Dashboard)
        selectedNodeId = service.getServiceStatusID();

        // check for extended role permissions - in dashboard
        if (!inStatusViewer
                && !referenceTreeModel.checkNodeForExtendedRolePermissions(
                        selectedNodeId, NodeType.SERVICE, service
                                .getDescription(), userExtendedRoleBean
                                .getExtRoleHostGroupList(),
                        userExtendedRoleBean.getExtRoleServiceGroupList())) {
            String inadequatePermissionsMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_inadequate_permissions")
                    + " [" + service.getDescription() + "]";
            handleInfo(inadequatePermissionsMessage);
            return false;
        }

        // Get the current monitor status for this service.
        NetworkObjectStatusEnum serviceStatus = MonitorStatusUtilities
                .getEntityStatus(service, NodeType.SERVICE);
        if (serviceStatus == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "createServiceAvailabilityData() : Service Monitor Status not found.");
            throw new GWPortalGenericException();
        }
        // Set the icon path for the current monitor status.
        setIconPath(serviceStatus.getIconPath());

        // Set the current host.
        setCurrentHost(service.getHost());

        // Set the availabilityBean values.
        AvailabilityBean serviceAvailabilityBean = new AvailabilityBean();
        // Set the host for this service.
        serviceAvailabilityBean.setHost(getCurrentHost());
        serviceAvailabilityBean.setServiceStatus(service);
        serviceAvailabilityBean.setService(true);

        List<StateTransitionBean> serviceTransitions = null;
        String serviceName = service.getDescription();
        // Get service state transitions data.
        serviceTransitions = getServiceStateTransitions(serviceAvailabilityBean);

        if (serviceTransitions == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "createServiceAvailabilityData() :  Found null state transitions data for service : "
                            + serviceName);
        } else {
            // Append the serviceTransitions to the serviceAvailabilityDataList.
            serviceAvailabilityDataList.addAll(serviceTransitions);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Retrieved  " + serviceTransitions.size()
                        + " state transitions for service : "
                        + service.getDescription());
            }
        }
        return true;
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
        // Set the default selected time as TODAY.
        setSelectedTime(TimeIntervalEnumEE.TODAY.getValue());
        setOldSelectedTime(TimeIntervalEnumEE.TODAY.getValue());
        return timeSelectorList;
    }

    // /**
    // * In case there are no state transitions for a service and creates a
    // dummy
    // * transition data to display bar chart.
    // *
    // * @return stateTransitionBean
    // */
    // private StateTransitionBean getCurrentStateForService(ServiceStatus
    // service) {
    // StateTransitionBean stateTransitionBean = new StateTransitionBean();
    // /*
    // * Since there are no state transitions for this service,we assume that
    // * service is in the some unknown state.
    // */
    //
    // // Set the service name to be displayed as label on category axis of the
    // // bar chart.
    // stateTransitionBean.setEntityName(AvailabilityDataSetHandler
    // .createServiceName(service.getDescription()));
    //
    // // stateTransitionBean.setEntityName(service.getDescription());
    //
    // stateTransitionBean.setServiceName(service.getDescription());
    //
    // // stateTransitionBean.setIconPath(serviceAvailBean.getIconPath());
    // // Calculate the time in state.
    // stateTransitionBean.setTimeInState(calculateDurationInState());
    //
    // if (service.getMonitorStatus() == null) {
    // handleError(
    // "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
    // Constant.METHOD
    // +
    // "getCurrentStateForService() : Null Monitor status found for service : "
    // + service.getDescription());
    // return stateTransitionBean;
    // }
    // // Set the monitor status for service.
    // stateTransitionBean.setToState(Constant.NO_STATUS);
    // return stateTransitionBean;
    // }

    /**
     * In case there are no state transitions for a service and creates a dummy
     * transition data to display bar chart.
     * 
     * @return stateTransitionBean
     */
    private StateTransitionBean getCurrentStateForService(ServiceStatus service) {
        StateTransitionBean stateTransitionBean = new StateTransitionBean();
        /*
         * Since there are no state transitions for this service,we assume that
         * service is in the some unknown state.
         */

        // Set the service name to be displayed as label on category axis of the
        // bar chart.
        stateTransitionBean.setEntityName(AvailabilityDataSetHandler
                .createServiceName(service.getDescription()));

        // stateTransitionBean.setEntityName(service.getDescription());

        stateTransitionBean.setServiceName(service.getDescription());

        // stateTransitionBean.setIconPath(serviceAvailBean.getIconPath());
        // Calculate the time in state.
        stateTransitionBean.setTimeInState(calculateDurationInState());

        if (service.getMonitorStatus() == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "getCurrentStateForService() : Null Monitor status found for service : "
                            + service.getDescription());
            return stateTransitionBean;
        }
        // Set the monitor status for service.
        if (service.getMonitorStatus().getName().equalsIgnoreCase(
                Constant.PENDING)) {
            // If current status is PENDING,no bar should be displayed
            stateTransitionBean.setToState(Constant.NO_STATUS);
        } else {
            stateTransitionBean
                    .setToState(service.getMonitorStatus().getName());
        }
        return stateTransitionBean;
    }

    /**
     * This method computes/retrieves data needed to display the bar chart for
     * host availability for Service. *
     * 
     * @param serviceAvailBean
     * @return stateTransitionBeanList
     */
    public List<StateTransitionBean> getServiceStateTransitions(
            AvailabilityBean serviceAvailBean) {
        List<StateTransitionBean> stateTransitionBeanList = new ArrayList<StateTransitionBean>();
        WSFoundationCollection wsFoundationCollection = null;
        if (serviceAvailBean == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "getServiceStateTransitions() : Found null serviceAvailBean");
            return stateTransitionBeanList;
        }
        if (serviceAvailBean.getServiceStatus() == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "getServiceStateTransitions() : serviceAvailBean.getServiceStatus() is null");
            return stateTransitionBeanList;
        }
        ServiceStatus service = serviceAvailBean.getServiceStatus();

        // Get the current monitor status for host
        String currentStatus = AvailabilityDataSetHandler
                .getCurrentServiceStatusName(service);

        // String startDt = getStartDateString();
        // Calendar calStartDate = getStartDate();
        // TimeIntervalEnum timeEnum = TimeIntervalEnum
        // .getTimeIntervalEnum(getSelectedTime());
        // if (timeEnum == TimeIntervalEnum.TODAY) {
        // startDt = getOldStartDateString();
        // try {
        // calStartDate.setTime(dateFormat.parse(startDt));
        // } catch (ParseException ex) {
        // handleError(
        // "com_groundwork_portal_statusviewer_hostAvailabilityPortlet_error",
        // ex);
        // return stateTransitionBeanList;
        // }
        // }
        try {
            wsFoundationCollection = foundationWSFacade
                    .getServiceStateTransitions(getCurrentHost().getName(),
                            service.getDescription(), getStartDateString(),
                            getEndDateString());
        } catch (GWPortalGenericException ex) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    ex);
            return stateTransitionBeanList;
        }
        if (wsFoundationCollection == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "getServiceStateTransitions : wsFoundationCollection is null.");
            return stateTransitionBeanList;
        }
        StateTransition[] stateTransitions = wsFoundationCollection
                .getStateTransition();
        // No state transitions for this service
        if ((stateTransitions == null) || (stateTransitions.length == 0)) {
            /**
             * NEW IMPLEMETNATION - If there are no state transitions available,
             * display an empty image.
             */
            StateTransitionBean transitionBean = getCurrentStateForService(service);
            if (transitionBean == null) {
                handleError(
                        "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                        Constant.METHOD
                                + "getServiceStateTransitions() : Null state transition data found for service :"
                                + selectedNodeName);
                return stateTransitionBeanList;
            }
            stateTransitionBeanList.add(transitionBean);
        } else {
            AvailabilityDataSetHandler availabilityDataSetHandler = new AvailabilityDataSetHandler(
                    getStartDate());
            stateTransitionBeanList = availabilityDataSetHandler
                    .createAvailabilityData(stateTransitions, Constant.ZERO,
                            selectedNodeName, currentStatus, false);
        }
        return stateTransitionBeanList;
    }

    /**
     * This method calculates the duration for which the host/service was in a
     * particular state for the user selected end date.
     * 
     * @return durationInState
     * @throws GWPortalException
     */
    private long calculateDurationInState() {
        long durationInState = 0;
        try {
            Date startDt = dateFormat.parse(getStartDateString());
            Date endDt = dateFormat.parse(getEndDateString());
            if (startDt == null || endDt == null) {
                handleError(
                        "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                        Constant.METHOD
                                + "calculateDurationInState() : Start date or end date is null.");
                return durationInState;
            }
            durationInState = endDt.getTime() - startDt.getTime();
        } catch (Exception e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
        }
        return durationInState;
    }

    /**
     * This method is called when user clicks on 'Apply' button on Host
     * Availability Portlet. It retrieves the host state transitions for the
     * selected time period and displays a jfree bar chart.
     * 
     * @param event
     */
    public void apply(javax.faces.event.ActionEvent event) {
        // Clean up the used resources.
        cleanup();
        try {
            setOldSelectedTime(getSelectedTime());
            if ("-1".equalsIgnoreCase(getSelectedTime())) {
                setCustomDate(true);
                setCustOldStartDate(getCustStartDate());
                setCustOldEndDate(getCustEndDate());
                setTimeBoundsForCustomDates();
            } else {
                setCustomDate(false);
                // Set the startDate,endDate
                setTimeBounds();

            }
            // setting time filter for perf measurement portlet.
            if (subpageIntegrator.isInStatusViewer()) {
                setPerfMeasurementTimeFilter();
            } else {
                // setting selected time filer in to session
                FacesUtils.getPortletSession(false).setAttribute(
                        TIME_FILTER_SESSION_ATTRIBUTE, getSelectedTime(),
                        PortletSession.PORTLET_SCOPE);
                if ("-1".equalsIgnoreCase(getSelectedTime())) {
                    FacesUtils.getPortletSession(false).setAttribute(
                            SERVICE_CUSTOM_DATES_SESSION_ATTRIBUTE,
                            getCustStartDate()
                                    + Constant.CUSTOM_DATE_DELIMITERS
                                    + getCustEndDate(),
                            PortletSession.PORTLET_SCOPE);
                }
            }
            // get the service availability data and generate the bar chart.
            showServiceAvailability();
        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
            return;
        }
    }

    /**
     * Cleans up/resets the used lists/resources
     */
    private void cleanup() {
        availabilityBeanList.clear();
        serviceAvailabilityDataList.clear();
        // reset the error,info,message flags
        setError(false);
        setInfo(false);
        setMessage(false);
    }

    /**
     * setting start time and end time attribute in PerfMeasurementIPCBean
     * instance to get in PerfMeasurementportlet while IPC.
     */
    private void setPerfMeasurementTimeFilter() {
        PerfMeasurementIPCBean perfMeasurementIPCBean = new PerfMeasurementIPCBean();
        StateController stateController = new StateController();
        perfMeasurementIPCBean.setStartDate(getStartDateString());
        perfMeasurementIPCBean.setEndDate(getEndDateString());
        perfMeasurementIPCBean.setWidth(BAR_CHART_WIDTH);
        stateController.applyPerfTimeFilter(perfMeasurementIPCBean);
        stateController = null;
    }

    /**
     * This method interprets the time selected by the user and sets the
     * startTime and endTime for retrieving host state transitions. *
     * 
     * @throws GWPortalGenericException
     */
    public void setTimeBounds() throws GWPortalGenericException {
        if (getSelectedTime() == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "setTimeBounds() : Selected Time found to be null.");
            return;
        }
        TimeIntervalEnumEE timeIntervalEnum = TimeIntervalEnumEE
                .getTimeIntervalEnum(getOldSelectedTime());
        // Get Calendar instance
        Calendar calendar = Calendar.getInstance();

        if (calendar == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "setTimeBounds() : Found null Calendar object");
            return;
        }
        // Set the end date string
        setEndDateString(dateFormat.format(calendar.getTime()));
        // set end date
        setEndDate(calendar);

        if (timeIntervalEnum == TimeIntervalEnumEE.TODAY) {
            // Set the calendar time to start of current day i.e. 00:00:00
            calendar.set(calendar.get(Calendar.YEAR), calendar
                    .get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
            // set oldStartDateString
            setOldStartDateString(dateFormat.format(calendar.getTime()));
            // set start date string
            setStartDateString(dateFormat.format(calendar.getTime()));
            // Set start date
            setStartDate(calendar);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, timeIntervalEnum
                    .getValueToAdd());
            // set start date string
            setStartDateString(dateFormat.format(calendar.getTime()));
            // Set start date
            setStartDate(calendar);
        }

        // Clear the time interval list created earlier.
        setTimeIntervalList(new ArrayList<String>());

        /*
         * Recompute the start date so as to skip older data but include data
         * till the current date. In case of TODAY, the time slot should start
         * from midnight,hence skip recomputing the start date.
         */
        if (timeIntervalEnum != TimeIntervalEnumEE.TODAY) {
            computeStartDate(timeIntervalEnum);
        }

        try {
            // create the customize range axis,as per the time bounds selected
            // by user.
            createRangeAxis(timeIntervalEnum);
        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
            throw e;
        }
    }

    /**
     * This method computes the start date so that state transitions till the
     * current date should be displayed on the bar chart and the older
     * transitions can be skipped ,if required. This is required because there
     * is limit on the number of intervals to be shown on bar chart.
     * 
     * @param timeEnum
     * @throws GWPortalGenericException
     */
    private void computeStartDate(TimeIntervalEnumEE timeEnum)
            throws GWPortalGenericException {
        Calendar calendar = getStartDate();
        int remainder = 0;
        int totalHours = 0;
        try {
            // For hours
            if (timeEnum.isHours()) {
                // int hoursTillNow = Calendar.getInstance().get(
                // Calendar.HOUR_OF_DAY);
                // for TODAY
                // if (timeEnum == TimeIntervalEnum.TODAY) {
                // totalHours = hoursTillNow;
                // } else {
                totalHours = Integer.parseInt(timeEnum.getValue());
                // }

                remainder = totalHours % DIVISOR_FOR_INTERVALS;
                // Add remainder number of hours to the start date.
                if ((remainder > 0) && (totalHours > DIVISOR_FOR_INTERVALS)) {
                    calendar.add(Calendar.HOUR_OF_DAY, remainder);
                }
            } else {
                // For days
                int totalDays = Integer.parseInt(timeEnum.getValue());
                remainder = totalDays % DIVISOR_FOR_INTERVALS;
                // Subtract remainder number of days from the start date.
                if (remainder > 0) {
                    calendar.add(Calendar.DATE, remainder);
                }
            }
            setStartDate(calendar);
            setStartDateString(dateFormat.format(calendar.getTime()));
        } catch (Exception ex) {
            handleError(
                    "com_groundwork_portal_statusviewer_hostAvailabilityPortlet_error",
                    ex);
            throw new GWPortalGenericException();
        }
    }

    /**
     * Method that will be called on click of "Retry now" button on message
     * page. This method re-renders the portlet contents
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {
        initialize(true);
    }

    /**
     * This method sets the message flag to true,set the message message to be
     * displayed to the user and logs the message.Ideally each catch block
     * should call this method.
     * 
     * @param resourceKey
     *            - key for the localized message to be displayed on the UI.
     * @param logMessage
     *            - message to be logged.
     * 
     */
    public void handleError(String resourceKey, String logMessage) {
        setMessage(true);
        setError(true);
        setErrorMessage(ResourceUtils.getLocalizedMessage(resourceKey));
        LOGGER.error(logMessage);
    }

    /**
     * This method sets the message flag to true,set the message message to be
     * displayed to the user and logs the message.Ideally each catch block
     * should call this method.
     * 
     * @param resourceKey
     *            - key for the localized message to be displayed on th UI.
     * @param exception
     *            - Exception to be logged.
     * 
     */
    public void handleError(String resourceKey, Exception exception) {
        setMessage(true);
        setError(true);
        setErrorMessage(ResourceUtils.getLocalizedMessage(resourceKey));
        LOGGER.error(exception);
    }

    /**
     * Handles Info : sets Info flag and message.
     */
    private void handleInfo(String infoMsg) {
        setMessage(true);
        setInfo(true);
        setInfoMessage(infoMsg);
    }

    /**
     * Call back method for JMS
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(String)
     */
    @Override
    public void refresh(String topicXML) {
        // List<JMSUpdate> jmsUpdates = new ArrayList<JMSUpdate>();
        // try {
        // if (topicXML != null) {
        // // Get the jms updates.
        // jmsUpdates = JMSUtils.getJMSUpdatesListFromXML(topicXML,
        // selectedNodeType);
        // }
        // if (jmsUpdates != null) {
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
        // "Processing JMS push in service availability portlet for selectedNodeId : "
        // + selectedNodeId);
        // }
        // /*
        // * Fetch the service transitions and generate the
        // * bar chart.
        // */
        // initialize();
        // SessionRenderer.render(groupRenderName);
        // }
        // }
        // }
        // }
        // } catch (Exception exc) {
        // LOGGER.error(exc.getMessage());
        // }
    }

    /**
     * Returns the paddingStyle.
     * 
     * @return the paddingStyle
     */
    public String getPaddingStyle() {
        paddingStyle = Constant.PAD_RIGHT135;
        boolean inDashbord = PortletUtils.isInDashbord();
        if (inDashbord) {
            paddingStyle = "padRight33";
        }

        return paddingStyle;
    }

    /**
     * This method creates the customized DateAxis as per the Custom time
     * selected by user.It sets the TickUnit and date format for the dates/time
     * to be displayed on the range axis of the stacked bar chart.
     * 
     * @param timeEnum
     * @throws GWPortalGenericException
     */
    private void createRangeAxisForCustomDates(TimeIntervalEnumEE timeEnum)
            throws GWPortalGenericException {
        DateAxis axis = new DateAxis();
        try {
            /*
             * Set the start time to 12:59:59 so that jfreeChart displays 12 AM
             * on the range axis.If the start time is set to 00:00:00 i.e.
             * midnight,jfreeChart does not display 12 AM tick-label on the
             * graph.
             */
            Calendar cal = Calendar.getInstance();
            cal.setTime(getStartDate().getTime());
            cal.add(Calendar.SECOND, -1);
            setStartDate(cal);

            // Set the maximum date for the rangeAxis.
            axis.setMaximumDate(dateFormat.parse(getEndDateString()));

            // Set the minimum date for the rangeAxis
            // axis.setMinimumDate(dateFormat.parse(getStartDateString()));
            axis.setMinimumDate(getStartDate().getTime());
        } catch (ParseException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    e);
            throw new GWPortalGenericException();
        }
        DateTickUnit dateTickUnit = null;
        SimpleDateFormat sdf = null;
        if (timeEnum == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "divideTimeIntervals : Found null timeIntervalEnum");
            throw new GWPortalException();
        }
        String startDateStr = getStartDateString().trim();
        String startDateOnly = startDateStr.substring(0, startDateStr
                .indexOf(" "));

        String endDateStr = getEndDateString().trim();
        String endDateOnly = endDateStr.substring(0, endDateStr.indexOf(" "));

        Date parseStartDate = null;
        Date parseEndDate = null;
        try {
            parseStartDate = dateOnlyFormat.parse(startDateOnly);
            parseEndDate = dateOnlyFormat.parse(endDateOnly);
        } catch (ParseException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "createRangeAxisForCustomDates : Found Date Parse exception");
            throw new GWPortalException();
        }

        long dateDiffInHour = dateDiffInHour(getStartDate(), getEndDate());
        if (parseStartDate.equals(parseEndDate)) {
            LOGGER.debug("Custom Dates are equals..........");
            int incrementBy = 1;
            if (dateDiffInHour > DIVISOR_FOR_INTERVALS) {
                incrementBy = (int) dateDiffInHour / DIVISOR_FOR_INTERVALS;
            }
            dateTickUnit = new DateTickUnit(DateTickUnit.HOUR, incrementBy);
            sdf = new SimpleDateFormat(Constant.TIME_FORMAT);
        } else {
            if (dateDiffInHour < DIVISOR_FOR_INTERVALS) {
                dateTickUnit = new DateTickUnit(DateTickUnit.HOUR, 1);
                sdf = new SimpleDateFormat(Constant.DATE_FORMAT_HOURS_ONLY);
            } else if (dateDiffInHour < DATE_DIFF_168_HOURS) {
                int totalHours = (int) dateDiffInHour;
                dateTickUnit = new DateTickUnit(DateTickUnit.HOUR,
                        (totalHours / DIVISOR_FOR_INTERVALS));
                sdf = new SimpleDateFormat(Constant.DATE_FORMAT_HOURS_ONLY);
            } else {
                // Selected time can be divided into 8 equal intervals of days.
                int totalDays = (int) dateDiffInHour / HOURS_IN_A_DAY;
                dateTickUnit = new DateTickUnit(DateTickUnit.DAY,
                        (totalDays / DIVISOR_FOR_INTERVALS));
                sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
            }
        }

        // Set Tick unit
        axis.setTickUnit(dateTickUnit);
        axis.setAutoRange(true);
        // Set the date format
        axis.setDateFormatOverride(sdf);
        // Set lower margin.
        axis.setLowerMargin(0.0D);
        axis.setUpperMargin(0.0D);
        // Set the variable to the class level DateAxis variable.
        setDateAxis(axis);
    }

    /**
     * get difference between two dates
     * 
     * @param startDate
     * @param endDate
     * @return long
     */
    private long dateDiffInHour(Calendar startDate, Calendar endDate) {
        long starttimeInMillis = startDate.getTimeInMillis();
        long endtimeInMillis = endDate.getTimeInMillis();
        long diff = endtimeInMillis - starttimeInMillis;
        long diffHours = diff / ONE_HOUR_IN_MILLIS;
        return diffHours;
    }

    /**
     * This method interprets the custom time selected by the user and sets the
     * startTime and endTime for retrieving host state transitions.
     * 
     * @throws GWPortalGenericException
     * 
     */
    public void setTimeBoundsForCustomDates() throws GWPortalGenericException {
        if (getSelectedTime() == null) {
            handleError(
                    "com_groundwork_portal_statusviewer_hostAvailabilityPortlet_error",
                    Constant.METHOD
                            + "setTimeBounds() : Selected Time found to be null.");
            return;
        }
        TimeIntervalEnumEE timeIntervalEnum = TimeIntervalEnumEE
                .getTimeIntervalEnum(getOldSelectedTime());
        String customStartDate = getCustOldStartDate();
        String customEndDate = getCustOldEndDate();
        if (getCustStartDate() != null && getCustEndDate() != null) {
            customStartDate = getCustStartDate().concat(":00");
            customEndDate = getCustEndDate().concat(":00");
        }
        // clear faces message
        FacesContext context = FacesUtils.getFacesContext();
        if (context != null) {
            String frmID = "SVform";
            if (!subpageIntegrator.isInStatusViewer()) {
                frmID = serviceAvailabilityFrmID;
            }

            UIComponent startDateTimeComponent = context
                    .getViewRoot()
                    .findComponent(
                            frmID
                                    + Constant.COLON
                                    + "serviceAvailabilityPortlet_startDateTime");

            // validate Start date
            if (!validateStartDateTime(context, startDateTimeComponent,
                    customStartDate, customEndDate)) {
                return;
            }
            UIComponent endDateTimeComponent = context.getViewRoot()
                    .findComponent(
                            frmID + Constant.COLON
                                    + "serviceAvailabilityPortlet_endDateTime");
            // validate End date
            if (!validateEndDateTime(context, endDateTimeComponent,
                    customEndDate, customStartDate)) {
                return;
            }

        }

        // Set the end date string
        setEndDateString(customEndDate);
        try {
            Date parseEndDate = dateFormat.parse(getEndDateString());
            // Get Calendar instance
            Calendar calendar = Calendar.getInstance();

            if (calendar == null) {
                handleError(
                        "com_groundwork_portal_statusviewer_hostAvailabilityPortlet_error",
                        Constant.METHOD
                                + "setTimeBounds() : Found null Calendar object");
                return;
            }
            calendar.setTime(parseEndDate);
            // set end date
            setEndDate(calendar);

        } catch (ParseException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "createRangeAxisForCustomDates : Found Date Parse exception");
            throw new GWPortalException();
        }
        setStartDateString(customStartDate);
        try {
            Date parseStartDate = dateFormat.parse(getStartDateString());
            // Get Calendar instance
            Calendar calendar = Calendar.getInstance();

            if (calendar == null) {
                handleError(
                        "com_groundwork_portal_statusviewer_hostAvailabilityPortlet_error",
                        Constant.METHOD
                                + "setTimeBounds() : Found null Calendar object");
                return;
            }
            calendar.setTime(parseStartDate);
            // set end date
            setStartDate(calendar);
        } catch (ParseException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_serviceAvailabilityPortlet_error",
                    Constant.METHOD
                            + "createRangeAxisForCustomDates : Found Date Parse exception");
        }

        try {
            // create the customize range axis,as per the time bounds selected
            // by user.
            createRangeAxisForCustomDates(timeIntervalEnum);
        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_hostAvailabilityPortlet_error",
                    e);
            throw e;
        }
    }

    /**
     * This method validates the startTime field. 1) Should be non-empty. 2)
     * Format:mm/dd/yyyy hh:mm:ss 3) Must be > current time 4) must be < endTime
     * 
     * @param context
     * @param component
     * @param value
     * @param custEndDate
     * @return boolean
     */
    public boolean validateStartDateTime(FacesContext context,
            UIComponent component, Object value, String custEndDate) {
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
                return false;
            }
            // check for date format.
            if (!ValidationUtils.isValidDateFormat(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate_format"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate_format"),
                                context, component);
                return false;
            }
            /*
             * Check for the valid values of the
             * date,month,year,hours,minutes,seconds.
             */
            if (!ValidationUtils.validateDateTimeFields(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_startDate"),
                                context, component);
                return false;
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
                return false;
            }

        } // (value != null)
        return true;
    }

    /**
     * This method validates the startTime field. 1) Should be non-empty. 2)
     * Format:mm/dd/yyyy hh:mm:ss 3) Must be > current time 4) must be < endTime
     * 
     * @param context
     * @param component
     * @param value
     * @param custStartdate
     * @return boolean
     */
    public boolean validateEndDateTime(FacesContext context,
            UIComponent component, Object value, String custStartdate) {
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
                return false;
            }
            // check for date format.
            if (!ValidationUtils.isValidDateFormat(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate_format"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate_format"),
                                context, component);
                return false;
            }
            /*
             * Check for the valid values of the
             * date,month,year,hours,minutes,seconds.
             */
            if (!ValidationUtils.validateDateTimeFields(inputString,
                    Constant.DATE_FORMAT_24_HR_CLK)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_invalid_endDate"),
                                context, component);
                return false;
            }
            // Check if the input date < startTime
            if (ValidationUtils.isPastDate(inputString, custStartdate,
                    dateFormat)) {
                ((UIInput) component).setValid(false);
                ValidationUtils
                        .showMessage(
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_greater_endDate"),
                                ResourceUtils
                                        .getLocalizedMessage("com_groundwork_portal_statusviewer_greater_endDate"),
                                context, component);
                return false;
            }

        } // (value != null)
        return true;
    }

    /**
     * selected TimeChange Listener
     * 
     * @param event
     */
    public void selectedServiceTimeChangeListener(ValueChangeEvent event) {

        String newSelectedTime = (String) event.getNewValue();

        if ("-1".equalsIgnoreCase(newSelectedTime)) {

            initializeDates();

        }

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
     * Sets the tableID.
     * 
     * @param tableID
     *            the tableID to set
     */
    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    /**
     * Returns the tableID.
     * 
     * @return the tableID
     */
    public String getTableID() {
        return tableID;
    }

    /**
     * Sets the serviceAvailabilityFrmID.
     * 
     * @param serviceAvailabilityFrmID
     *            the serviceAvailabilityFrmID to set
     */
    public void setServiceAvailabilityFrmID(String serviceAvailabilityFrmID) {
        this.serviceAvailabilityFrmID = serviceAvailabilityFrmID;
    }

    /**
     * Returns the serviceAvailabilityFrmID.
     * 
     * @return the serviceAvailabilityFrmID
     */
    public String getServiceAvailabilityFrmID() {
        return serviceAvailabilityFrmID;
    }
}
