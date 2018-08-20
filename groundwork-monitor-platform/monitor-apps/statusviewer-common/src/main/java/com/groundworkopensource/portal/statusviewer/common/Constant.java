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

package com.groundworkopensource.portal.statusviewer.common;

import com.groundworkopensource.portal.common.ResourceUtils;

/**
 * Constant file for Status Viewer.
 * 
 * @author manish_kjain
 */
public class Constant {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected Constant() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * JSP_FOLDER_PATH
     */
    private static final String JSP_FOLDER_PATH = "/jsp/";

    /**
     * 
     */
    public static final String HOST_GROUP_VIEW_PATH = JSP_FOLDER_PATH
            + "HostGroup.iface";

    /**
     * 
     */
    public static final String HOST_VIEW_PATH = JSP_FOLDER_PATH + "host.iface";

    /**
     * 
     */
    public static final String SERVICE_GROUP_VIEW_PATH = JSP_FOLDER_PATH
            + "ServiceGroup.iface";

    /**
     * 
     */
    public static final String SERVICE_VIEW_PATH = JSP_FOLDER_PATH
            + "Service.iface";

    /**
     * Path of the jsp file to be displayed in VIEW mode for Filter portlet
     */
    public static final String FILTER_VIEW_PATH = JSP_FOLDER_PATH
            + "filterView.iface";
    /**
    *
    */
    public static final String MONITOR_PERFORMANCE_VIEW_PATH = JSP_FOLDER_PATH
            + "monitoringPerformanceView.iface";

    /**
    *
    */
    public static final String NETWORK_TREE_VIEW_PATH = JSP_FOLDER_PATH
            + "networkTree.iface";

    /**
     * HOST_GROUP_HEALTH PATH - view file for Host Group Health Portlet.
     */
    public static final String HOST_GROUP_HEALTH_PATH = JSP_FOLDER_PATH
            + "hostGroupHealth.iface";

    /**
     * HOST_INFORMATION_PATH - view file for Host Information Portlet.
     */
    public static final String HOST_INFORMATION_PATH = JSP_FOLDER_PATH
            + "hostInformation.iface";

    /**
     * Path of the jsp file to be displayed in VIEW mode for Stacked
     * NagiosMonitoring portlet
     */
    public static final String NAGIOS_MONITORING_STACKED_VIEW_PATH = JSP_FOLDER_PATH
            + "nagiosMonitoringStatisticsStacked.iface";

    /**
     * Path of the jsp file to be displayed in VIEW mode for horizontal
     * NagiosMonitoring portlet
     */
    public static final String NAGIOS_MONITORING_HORIZONTAL_VIEW_PATH = JSP_FOLDER_PATH
            + "nagiosMonitoringStatisticsHorizontal.iface";

    /**
     * Path of the jsp file to be displayed in VIEW mode for Host List portlet
     */
    public static final String HOSTLIST_VIEW_PATH = JSP_FOLDER_PATH
            + "hostList.iface";

    /**
     * View file for Perf Measurement Portlet.
     */
    public static final String PERF_MEASUREMENT_PATH = JSP_FOLDER_PATH
            + "perfmeasurement.iface";

    /**
     * Path of the jsp file to be displayed in VIEW mode for Service List
     * portlet
     */
    public static final String SERVICELIST_VIEW_PATH = JSP_FOLDER_PATH
            + "serviceListView.iface";

    /**
     * 
     */
    public static final String HOST_GROUP_STATISTICS_BEAN = "hostGroupStatisticsBean";

    /**
     * 
     */
    public static final String SERVICE_GROUP_STATISTICS = "serviceGroupStatistics";

    /**
     * 
     */
    public static final String HOST_STATISTICS_BEAN = "hostStatisticsBean";

    /**
    *
    */
    public static final String FILTER_BEAN = "filterBean";

    /**
     * Empty String.
     */
    public static final String EMPTY_STRING = "";

    /**
     * SPACE
     */
    public static final String SPACE = " ";

    /**
     * Host unreachable status.
     */
    public static final String HOST_OR_GROUP_UNREACHABLE = "UNREACHABLE";
    /**
     * Host down status.
     */
    public static final String HOST_OR_GROUP_DOWN = "DOWN";
    /**
     * Host up status.
     */
    public static final String HOST_OR_GROUP_UP = "UP";
    /**
     * Service ok status.
     */
    public static final String SERVICE_OK = "OK";
    /**
     * Host or Service pending status.
     */
    public static final String HOST_OR_SERVICE_PENDING = "PENDING";
    /**
     * Service unknown status.
     */
    public static final String SERVICE_UNKNOWN = "UNKNOWN";
    /**
     * Service warning status.
     */
    public static final String SERVICE_WARNING = "WARNING";
    /**
     * Service critical status.
     */
    public static final String SERVICE_CRITICAL = "CRITICAL";

    /**
     * Constant defining time for which the monitor status value = UP for host
     */
    public static final String TIME_UP = "timeUp";

    /**
     * Constant defining time for which the monitor status value = DOWN for host
     */
    public static final String TIME_DOWN = "timeDown";

    /**
     * Constant defining time for which the monitor status value = UNREACHABLE
     * for host
     */
    public static final String TIME_UNREACHABLE = "timeUnreachable";

    /**
     * Constant defining time for which the monitor status value = OK for host
     */
    public static final String TIME_OK = "timeOk";
    /**
     * Constant defining time for which the monitor status value = UNKNOWN for
     * host
     */
    public static final String TIME_UNKNOWN = "timeUnkown";
    /**
     * Constant defining time for which the monitor status value = WARNING for
     * host
     */
    public static final String TIME_WARNING = "timeWarning";
    /**
     * Constant defining time for which the monitor status value = CRITICAL for
     * host
     */
    public static final String TIME_CRITICAL = "timeCritical";

    /**
     * String Constant for "Hosts"
     */
    public static final String HOSTS = "Hosts";

    /**
     * String Constant for "Services"
     */
    public static final String SERVICES = "Services";
    /**
     * Model pop up date format
     */
    public static final String MODEL_POPUP_DATE_FROMAT = "MM/dd/yyyy h:mm:ss a";

    /**
     * String constant for "Host Name"
     */
    public static final String HOST_NAME = "Host Name";

    /**
     * String constant for "Service Name"
     */
    public static final String SERVICE_NAME = "Service Name";

    /**
     * String constant for "0"
     */
    public static final String STRING_ZERO = "0";

    /**
     * String constant for "1"
     */
    public static final String STRING_ONE = "1";

    /**
     * String constant for QUERY_TYPE. Used as a portletSession attribute.
     */
    public static final String QUERY_TYPE = "QUERY_TYPE";

    /**
     * String constant for QUERY_VALUE. Used as a portletSession attribute.
     */
    public static final String QUERY_VALUE = "QUERY_VALUE";

    /**
     * get host group monitor filter string property.
     */
    public static final String HOSTGROUP_MONITORSTATUS_NAME = "hosts.hostStatus.hostMonitorStatus.name";

    /**
     * service statistics bean name constant.
     */
    public static final String SERVICE_STATISTICS_BEAN = "serviceStatisticsBean";

    /**
     * constant NO is used to display Acknowledged for service model pop up
     */
    public static final String NO = "No";

    /**
     * constant YES is used to display Acknowledged for service model pop up
     */
    public static final String YES = "Yes";

    /**
     * boolean isProblemAcknowledged property.
     */
    public static final String IS_PROBLEM_ACKNOWLEDGED = "isProblemAcknowledged";

    /**
     * boolean isProblemAcknowledged property for host.
     */
    public static final String IS_ACKNOWLEDGED = "isAcknowledged";

    /**
     * popUp selection bean name
     */
    public static final String POP_UP_SELECT_BEAN = "popUpSelectBean";

    /**
     * comma for building service status id String.
     */
    public static final String COMMA = ",";

    /**
     * servlet content type constant.
     */
    public static final String IMAGE_PNG = "image/png";

    /**
     * pie chart query string
     */
    public static final String TYPE = "type";
    /**
     * chart request parameter
     */
    public static final String HOSTGROUP = "hostgroup";

    /**
     * menu selected item for host portlet.
     */
    public static final String ALLHOST = "allhost";
    /**
     * menu selected item for host Group portlet.
     */
    public static final String ALLHOSTGROUPS = "allhostgroups";
    /**
     * menu selected item for host portlet.
     */
    public static final String FILTEREDHOST = "filteredhost";
    /**
     * request parameter form host portlet.
     */
    public static final String HOSTSTATUS = "hoststatus";

    /**
     * menu selected item for host Group portlet.
     */
    public static final String FILTEREDHOSTGROUPS = "filteredhostgroups";

    /**
     * Total constant
     * 
     */
    public static final String TOTAL = "total";

    /**
     * service portlet select item value
     */
    public static final String ALL_SERVICE = "allservice";
    /**
     * service portlet select item value
     */
    public static final String FILTEREDSERVICE = "filteredservice";

    /**
     * width of dial chart.
     */
    public static final int DIAL_WIDTH = 153;
    /**
     * Height of dial chart.
     */
    public static final int DIAL_HEIGHT = 150;

    /**
     * width of pie chart.
     */
    public static final int PIE_WIDTH = 153;
    /**
     * Height of pie chart.
     */
    public static final int PIE_HEIGHT = 150;

    /**
     * ReferenceTreeMetaModel REFERENCE_TREE String
     */
    public static final String REFERENCE_TREE = "referenceTree";

    /**
     * String constant for "("
     */
    public static final String OPENING_ROUND_BRACE = "(";

    /**
     * String constant for ")"
     */
    public static final String CLOSING_ROUND_BRACE = ")";

    /**
     * Constant for string 'Today'
     */
    public static final String TODAY = "Today";

    /**
     * Constant for string 'Last 24 Hours'
     */
    public static final String LAST_24_HOURS = "Last 24 Hours";

    /**
     * Constant for string 'Last 48 Hours'
     */
    public static final String LAST_48_HOURS = "Last 48 Hours";

    /**
     * Constant for string 'Last 5 Days'
     */
    public static final String LAST_5_DAYS = "Last 5 Days";

    /**
     * Constant for string 'Last 7 Days'
     */
    public static final String LAST_7_DAYS = "Last 7 Days";

    /**
     * Constant for string 'Last 30 Days'
     */
    public static final String LAST_30_DAYS = "Last 30 Days";

    /**
     * Constant for string 'Last 90 Days'
     */
    public static final String LAST_90_DAYS = "Last 90 Days";

    /**
     * Host Availability bean name constant.
     */
    public static final String HOST_AVAILABILITY_BEAN = "hostAvailabilityBean";

    /**
     * String Constant for ":"
     */
    public static final String COLON = ":";
    
    /**
     * String Constant for "#:^"
     */
    public static final String NODE_VIEW_DELIMITER = "^^^";

    /**
     * Path of the jsp file to be displayed in VIEW mode for host availability
     * portlet
     */
    public static final String HOST_AVAILABILITY_VIEW_PATH = JSP_FOLDER_PATH
            + "hostAvailability.iface";

    /**
     * /** ALIAS
     */
    public static final String ALIAS = "Alias";

    /**
     * This is a boolean property which defines if performance measurement is
     * enabled for a service or not. It is part of "ServiceStatus" element in
     * foundtion.
     */
    public static final String PERF_MEASUREMENT_PROPERTY = "isProcessPerformanceData";

    /**
     * all RRD files for groundwork are stored in below-mentioned path.
     */
    public static final String RRD_FILE_BASE_PATH = "/usr/local/groundwork/rrd/";

    /**
     * RRD file extension.
     */
    public static final String RRD_EXTENSION = ".rrd";

    /**
     * Separator character used in rrd file name. i.e. '_'.
     */
    public static final Character RRD_FILE_NAME_SEPARATOR = '_';

    /**
     * SCHEDULED_DOWN_TIME_DEPTH
     */
    public static final String SCHEDULED_DOWN_TIME_DEPTH = "ScheduledDownTimeDepth";

    /**
     * Date time pattern of Event Portlet
     */
    public static final String EVENT_DATETIME_PATTERN = "MM/dd/yyyy h:mm:ss a";
    /**
     * one constant
     */
    public static final int ONE = 1;

    /**
     * One constant.
     */
    public static final int ZERO = 0;

    /**
     * Event portlet column string.
     */
    public static final String EVENT_COLUMN_STRING = "reportDate:Received By GW,msgCount:Msg Count,device:Device,monitorStatus:Status,textMessage:Message,applicationType:Application Type,severity:Severity,lastInsertDate:Last Detected,firstInsertDate:First Detected";

    /**
     * Event portlet page size.
     */
    public static final String EVENT_PAGE_SIZE = "event_page_size";

    /**
     * Date time pattern of status viewer
     */
    public static final String STATUS_VIEWER_DATETIME_PATTERN = "portal.statusviewer.dateFormatString";

    /**
     * Default Date time pattern
     */
    public static final String DEFAULT_DATETIME_PATTERN = "MM/dd/yyyy h:mm:ss a";

    /**
     * Default sorted column name
     */
    public static final String DEFAULT_SORT_COLUMN_NAME = "reportDate";

    /**
     * property file path name.it should be come from the web.xml.
     */
    public static final String PROPERTY_FILE_PATH = "app.property.file.path";

    /**
     * status viewer property Attribute name
     */
    public static final String STATUSVIEWER_PROP = "StatusviewerProperties";

    /**
     * value constant used in event portlet.
     */
    public static final String VALUE = "value";

    /**
     * value El constant used in event portlet
     */
    public static final String VALUE_EL = ".value}";

    /**
     * event El constant used in event portlet
     */
    public static final String EVENT_BEAN_EL = "#{event.";

    /**
     * status bean name
     */
    public static final String STATUS_BEAN = "monitorStatus";
    
    /** The E l_ bin d_severity. */
    public static final String  EL_BIND_COL_SEVERITY = "severity";

    /**
     * sorting arrow path
     */
    public static final String IMAGES_SORT_ARROW_DOWN_GIF = "/images/sort_arrow_down.gif";

    /**
     * sorting arrow path
     */
    public static final String IMAGES_SORT_ARROW_UP_GIF = "/images/sort_arrow_up.gif";

    /**
     * eventMenuActionBean Bean name
     */
    public static final String EVENT_MENU_ACTION_BEAN = "eventMenuActionBean";

    /**
     * eventMessageSelectBean name
     */
    public static final String EVENT_MESSAGE_SELECT_BEAN = "eventMessageSelectBean";

    /**
     * open event status
     */
    public static final String OPEN = "OPEN";

    /**
     * open event status
     */
    public static final String ACKNOWLEDGE = "ACKNOWLEDGED";

    /**
     * eventFilterBean name
     */
    public static final String EVENT_FILTER_BEAN = "eventFilterBean";

    /**
     * El expression for sorting data table
     */
    public static final String EVENT_LIST_BEAN_DATA_TABLE_BEAN_SORT = "#{eventListBean.dataTableBean.sort}";

    /**
     * HTML panel group Style class
     */
    public static final String TABLE_COLUMN = "tableColumn";

    /**
     * 
     */
    public static final String STYEL_CLASS = "styleClass";

    /**
     * style class ext.
     */
    public static final String STYLE_CLASS_EL = ".styleClass}";

    /**
     * data table cell panel group ID
     */
    public static final String PNL = "pnl_";

    /**
     * cell output text component ID
     */
    public static final String TXT = "txt_";

    /**
     * Html graphics image ID
     */
    public static final String SORT_IMG = "sortImg";

    /**
     * Header style class
     */
    public static final String ICE_OUT_TXT = "iceOutTxt";

    /**
     * header panel grid style
     * 
     */
    public static final String CELLPADDING_0_CELLSPACING_0 = "cellpadding:0;cellspacing:0;";

    /**
     * data table header Id first part
     */
    public static final String HEADER = "header_";

    /**
     * Sort header ID first part
     */
    public static final String SORT_HEADER_ID = "sortHeaderId_";

    /**
     * Row selection style class
     */
    public static final String ICE_ROW_SEL_SELECTED = "iceRowSelSelected";

    /**
     * Mouse Row over style class
     */
    public static final String ICE_ROW_SEL_SELECTED_MOUSE_OVER = "iceRowSelSelectedMouseOver ";

    /**
     * Event bean multiple Row selection EL
     */
    public static final String EVENT_LIST_BEAN_DATA_TABLE_BEAN_ROW_SELECTION = "#{eventListBean.dataTableBean.rowSelection}";

    /**
     * Value Expression of multiple Row selection
     */
    public static final String MULTIPLE = "multiple";

    /**
     * Event bean multiple Row selection Boolean EL
     */
    public static final String EVENT_LIST_BEAN_DATA_TABLE_BEAN_MULTIPLE_SELECTION = "#{eventListBean.dataTableBean.multipleSelection}";

    /**
     * Event bean Row selection EL
     */
    public static final String EVENT_SELECT_ED = "#{event.selected}";

    /**
     * Row Id
     */
    public static final String RS = "rs_";

    /**
     * Column Id
     */
    public static final String COL = "col_";

    /**
     * Event LIst Bean Name
     */
    public static final String EVENT_LIST_BEAN = "eventListBean";

    public static final String EVENT_ACTION_HANDLER_BEAN = "eventActionHandler";

    /**
     * constant TRUE
     */
    public static final String TRUE = "true";

    /**
     * constant
     */
    public static final int TEN = 10;
    /**
     * constant
     */
    public static final int NINE = 9;
    /**
     * constant
     */
    public static final int EIGHT = 8;
    /**
     * constant
     */
    public static final int SEVEN = 7;
    /**
     * constant
     */
    public static final int SIX = 6;
    /**
     * constant
     */
    public static final int FIVE = 5;
    /**
     * constant
     */
    public static final int FOUR = 4;
    /**
     * constant
     */
    public static final int THREE = 3;
    /**
     * constant
     */
    public static final int TWO = 2;
    /**
     * ELEVEN constant
     */
    public static final int ELEVEN = 11;

    /**
     * action web service return success
     */
    public static final String ACTION_RETURN_SUCCESS = "SUCCESS";

    /**
     * action web service return code
     */
    public static final String ACTION_RETURN_HTTP_OK = "200";

    /**
     * Event portlet jspx file name
     */
    public static final String JSP_EVENT_IFACE = "/jsp/event.iface";

    /**
     * first part of Event portlet name
     */
    public static final String EVENT = "Events";

    /**
     * 
     */
    public static final String CLOSED_BRACE = "}";

    /**
     * 
     */
    public static final String NAGIOS_ACKNOWLEDGE = "Nagios Acknowledge";

    /**
     * 
     */
    public static final String NAGIOS_ACKNOWLEDGE_ID = "nagiosAcknowledge";

    /**
     * open constant
     */
    public static final String OPEN_MENU = "Open";

    /**
     * open Log message menu Item value
     */
    public static final String OPEN_LOG_MESSAGE = "Open Log Message";

    /**
     * open Log message menu Item Id
     */
    public static final String OPEN_LOG_MESSAGE_ID = "openLogMessage";

    /**
     * close Constant
     */
    public static final String CLOSE = "Close";

    /**
     * Notify Log message menu Item Value
     */
    public static final String CLOSE_LOG_MESSAGE = "Close Log Message";

    /**
     * Notify Log message menu Item Id
     */
    public static final String CLOSE_LOG_MESSAGE_ID = "closeLogMessage";

    /**
     * Notify Constant
     */
    public static final String NOTIFY = "Notify";

    /**
     * Notify menu Item Value
     */
    public static final String NOTIFY_LOG_MESSAGE = "Notify Log Message";

    /**
     * Notify Log message menu Item Id
     */
    public static final String NOTIFY_LOG_MESSAGE_ID = "notifyLogMessage";

    /**
     * Accept menu Item parameter value
     */
    public static final String ACCEPT = "Accept";

    /**
     * Accept menu Item parameter name
     */
    public static final String MENU_POP_UP_PARAM = "menuPopUpParam";

    /**
     * Accept menu Item Value
     */
    public static final String ACCEPT_LOG_MESSAGE = "Accept Log Message";

    /**
     * Accept Log message menu Item Id
     */
    public static final String ACCEPT_LOG_MESSAGE_ID = "acceptLogMessage";

    /**
     * Event Menu Pop item listener EL
     */
    public static final String EVENT_MENU_ACTION_BEAN_MENU_POP_UP_LISTENER = "#{eventMenuActionBean.menuPopUpListener}";

    /**
     * Event menu pop up Id
     */
    public static final String MNU_POP_UP_EVENT = "mnuPopUpEvent";

    /**
     * nagios String constant
     */
    public static final String NAGIOS = "Nagios";

    /**
     * Application type SYSLOG
     */
    public static final String SYSLOG = "SYSLOG";

    /**
     * Application type SNMPTRAP
     */
    public static final String SNMPTRAP = "SNMPTRAP";

    /**
     * SNMP or SYSLOG menu Item parameter value
     */
    public static final String PASSIVE = "Passive";

    /**
     * SNMP or SYSLOG menu Item value
     */
    public static final String SUBMIT_PASSIVE_CHECK = "Submit Passive Check";

    /**
     * SNMP or SYSLOG menu Item ID
     */
    public static final String SYS_LOG_OR_SNMP_ID = "sysLogOrSnmp";

    /**
     * This variable is used get sort data for application type column .
     */
    public static final String APPLICATION_TYPE_NAME = "applicationType.name";
    /**
     * This variable is used get sort data for severity column .
     */
    public static final String SEVERITY_NAME = "severity.name";
    /**
     * This variable is used get sort data for status column .
     */
    public static final String MONITOR_STATUS_NAME = "monitorStatus.name";
    /**
     * This variable is used get sort data for device column .
     */
    public static final String DEVICE_DISPLAY_NAME = "device.displayName";
    /**
     * irst Insert Date of log message
     */
    public static final String FIRST_INSERT_DATE = "firstInsertDate";
    /**
     * last Insert Date of log message
     */
    public static final String LAST_INSERT_DATE = "lastInsertDate";
    /**
     * textMessage Type of event
     */
    public static final String TEXT_MESSAGE = "textMessage";
    /**
     * Application Type of event
     */
    public static final String APPLICATION_TYPE = "applicationType";
    /**
     * severity of event
     */
    public static final String SEVERITY = "severity";
    /**
     * status bean of event name
     */
    public static final String EVENT_STATUS_BEAN = "statusBean";
    /**
     * device of event
     */
    public static final String DEVICE = "device";
    /**
     * message count
     */
    public static final String MSG_COUNT = "msgCount";
    /**
     * Report date of event
     */
    public static final String REPORT_DATE = "reportDate";

    /**
     * Application type of event portlet
     */
    public static final String SYSTEM = "SYSTEM";

    /**
     * menu bar item listener
     */
    public static final String EVENT_MENU_ACTION_BEAN_MENU_ITEM_LISTENER = "#{eventMenuActionBean.menuItemListener}";

    /**
     * menu bar dummy listener .
     */
    public static final String EVENT_MENU_ACTION_BEAN_DUMMY_LISTENER = "#{eventMenuActionBean.dummyListener}";

    /**
     * menu bar ID
     */
    public static final String MENU_BAR_ID = "actions";

    /**
     * Label on action button
     */
    public static final String ACTIONS_BUTTON_LABEL = "com_groundwork_portal_statusviewer_Event_button_action";
    /**
     * Event table style class for up and ok status
     */
    public static final String HIGHLIT_GREEN = "highlit_green";
    /**
     * Event status type up
     */
    public static final String UP = "up";
    /**
     * Event status type ok
     */
    public static final String OK = "ok";
    /**
     * Event table style class for unknown and pending status
     */
    public static final String HIGHLIT_GRAY = "highlit_gray";
    /**
     * Event status type warning
     */
    public static final String PENDING = "pending";
    /**
     * Event status type warning
     */
    public static final String UNKNOWN = "unknown";
    /**
     * Event table style class for down ,unreachable and critical status .
     */
    public static final String HIGHLIT_RED = "highlit_red";
    /**
     * Event status type warning
     */
    public static final String DOWN = "down";
    /**
     * Event status type warning
     */
    public static final String UNREACHABLE = "unreachable";
    /**
     * Event status type warning
     */
    public static final String CRITICAL = "critical";
    /**
     * warning style class for event data table
     */
    public static final String HIGHLIT_YELLOW = "highlit_yellow";
    /**
     * Event status type warning
     */
    public static final String EVENT_WARNING = "warning";

    /**
     * 
     * event portlet select button label
     */
    public static final String SELECT_ALL_BUTTON_LABEL = "com_groundwork_portal_statusviewer_Event_button_selectall";
    /**
     * 
     * event portlet de select button label
     */
    public static final String DE_SELECT_ALL_BUTTON_LABEL = "com_groundwork_portal_statusviewer_Event_button_deselectall";

    /**
     * yellow hex decimal color code
     */
    public static final String YELLOW_HEX = "FEF322";
    /**
     * orange hex decimal color code
     */
    public static final String ORAGNE_HEX = "FF9E33";
    /**
     * Scheduled Critical monitor status type
     */
    public static final String SCHEDULED_CRITICAL = "Scheduled Critical";
    /**
     * Unscheduled Critical monitor status type
     */
    public static final String UNSCHEDULED_CRITICAL = "Unscheduled Critical";
    /**
     * 16 Constant
     */
    public static final int SIXTEEN = 16;
    /**
     * blue hex decimal color code
     */
    public static final String BLUE_HEX = "2C95EB";
    /**
     * gray hex decimal color code
     */
    public static final String GRAY_HEX = "B5B4B3";
    /**
     * green hex decimal color code
     */
    public static final String GREEN_HEX = "47D240";
    /**
     * Red hex decimal color code
     */
    public static final String RED_HEX = "F01F19";

    /**
     * and string.
     */
    public static final String AND_STRING = "&";
    /**
     * request parameter for current selected filter
     */
    public static final String FILTER_NAME_PARAMETER = "filterName=";
    /**
     * request parameter for host group to identified servlet call .
     */
    public static final String TYPE_HOSTGROUP_PARAMETER = "?type=hostgroup";
    /**
     * servlet path for host group and servlet group
     */
    public static final String PIE_CHART_SERVLET_PATH = "/PieChart";

    /**
     * Statistics handler bean name
     */
    public static final String STATISTICS_HANDLER = "statisticsHandler";

    /**
     * request parameter for Service group to identified servlet call .
     */
    public static final String TYPE_SERVICE = "?type=service";

    /**
     * Critical colon Scheduled constant to be display on UI
     */
    public static final String CRITICAL_COLON_SCHEDULED = "Critical:Scheduled";
    /**
     * Critical colon unscheduled constant to be display on UI
     */
    public static final String CRITICAL_COLON_UN_SCHEDULED = "Critical:UnScheduled";
    /**
     * monitor status Down Scheduled".
     */
    public static final String DOWN_SCHEDULED = "Down Scheduled";
    /**
     * monitor status Down UnSchedule.
     */
    public static final String DOWN_UN_SCHEDULED = "Down UnScheduled";
    /**
     * down colon unscheduled constant to be display on UI
     */
    public static final String DOWN_COLON_SCHEDULED = "Down:Scheduled";
    /**
     * down colon scheduled constant to be display on UI
     */
    public static final String DOWN_COLON_UN_SCHEDULED = "Down:UnScheduled";

    /**
     * used to construct pop up window title
     */
    public static final String CLOSED_PARENTHESES = ")";
    /**
     * used to construct pop up window title
     */
    public static final String OPEN_PARENTHESES = " (";
    /**
     * first part of host statistics pop up title
     */
    public static final String HOST_COLON = "Host: ";

    /**
     * pie chart background HEX color code
     */
    public static final String PIE_BACKGRUOND_COLOR = "F8F8F8";

    /**
     * current selected filter name Request parameter for host and service.
     */
    public static final String REQUEST_PARAMETER_FILTER_NAME = "?filterName=";

    /**
     * Host pie chart servlet name
     */
    public static final String HOST_PIE_CHART_SERVLET_NAME = "/HostPieChart";

    /**
     * service portlet pie chart servlet name
     */
    public static final String SERVICE_PIE_CHART_SERVLET_NAME = "/ServicePieChart";

    /**
     * service Group Title sub String
     */
    public static final String SERVICE_GROUP_SUB_TITLE = "com_groundwork_portal_statusviewer_ServiceGroup_Modelpopup_title";
    /**
     * service Title sub String
     */
    public static final String SERVICE_SUB_TITLE = "com_groundwork_portal_statusviewer_Service_Modelpopup_title";
    /**
     * host Group Title sub String
     */
    public static final String HOST_GROUP_GROUP_SUB_TITLE = "com_groundwork_portal_statusviewer_hostGroup_Modelpopup_title";
    /**
     * host Title sub String
     */
    public static final String HOST_SUB_TITLE = "com_groundwork_portal_statusviewer_host_Modelpopup_title";

    /**
     * service group status model pop up request paramater
     */
    public static final String SERVICEGRPSTATUS_PARAMETER = "servicegrpstatus";

    /**
     * Total length of model popUP title.
     */
    public static final int FIFTY = 50;

    /**
     * Three dots using for "to be continued" on mobel pop up title bar if title
     * text length exceeds 50 character .
     */
    public static final String DOTS = " ...";

    /**
     * Host group pop request parameter
     */
    public static final String HOSTGROUP_STATUS = "hostgroupStatus";
    /**
     * Service Group Statistics bean
     */
    public static final String SERVICE_GROUP_STATISTICS_BEAN = "serviceGroupStatistics";

    /**
     * model pop up service Group filter item value
     */
    public static final String ALLSERVICEGROUP = "allservicegroup";

    /**
     * model pop up service Group filter time value
     * 
     */
    public static final String FILTEREDSERVICEGROUP = "filteredservicegroup";
    /**
     * servlet request parameter
     */
    public static final String FILTER_NAME = "filterName";

    /**
     * host monitor status up
     */
    public static final String UP_CAMEL_CASE = "Up";

    /**
     * host monitor status Unreachable
     */
    public static final String UNREACHABLE_CAMEL_CASE = "Unreachable";

    /**
     * host monitor status Scheduled Down
     */
    public static final String SCHEDULED_DOWN = "Scheduled Down";

    /**
     * host monitor status UnScheduled Down
     */
    public static final String UN_SCHEDULED_DOWN = "UnScheduled Down";

    /**
     * Service monitor status Ok
     */
    public static final String OK_CAMEL_CASE = "Ok";

    /**
     * Service monitor status Pending
     */
    public static final String PENDING_CAMEL_CASE = "Pending";

    /**
     * Service monitor status Unknown
     */
    public static final String UNKNOWN_CAMEL_CASE = "Unknown";

    /**
     * Service monitor status Warning
     */
    public static final String WARNING = "Warning";

    /**
     * Service monitor status Scheduled Critical
     */
    public static final String SCHEDULED_CRITICAL_CAMEL_CASE = "Scheduled Critical";

    /**
     * Service monitor status Unscheduled Critical
     */
    public static final String UNSCHEDULED_CRITICAL_CAMEL_CASE = "Unscheduled Critical";

    /**
     * Path of the jsp file to be displayed in VIEW mode for actions portlet
     */
    public static final String ACTIONS_VIEW_PATH = JSP_FOLDER_PATH
            + "actions.iface";

    /**
     * Constant for ';' (semicolon)
     */
    public static final String SEMICOLON = ";";

    /**
     * Name of the managed bean CommandParamsBean
     */
    public static final String COMMAND_PARAMS_MANAGED_BEAN = "commandParamsBean";

    /**
     * Constant for '\n' (new line character)
     */
    public static final char NEWLINE_CHAR = '\n';

    /**
     * Constant for ' ' (single space)
     */
    public static final char EMPTY_CHAR = ' ';

    /**
     * Constant for 'Minutes value is invalid.'
     */
    public static final String INVALID_MINUTES = "Minutes value is invalid.";

    /**
     * Constant for 'Minutes value is invalid.'
     */
    public static final String INVALID_HOURS = "Hours value is invalid.";

    /**
     * Constant for maximum allowed value for minutes
     */
    public static final int MINUTES_MAX_VALUE = 60;

    /**
     * Constant for maximum allowed value for seconds
     */
    public static final int SECONDS_MAX_VALUE = 60;

    /**
     * Constant for 'DateTime value is invalid.'
     */
    public static final String INVALID_DATE_TIME = "DateTime value is invalid.";

    /**
     * Simple date format
     */
    public static final String SIMPLE_DATE_FROMAT = "MM/dd/yyyy h:mm:ss";

    /**
     * Constant to represent int constant 1000
     */
    public static final int THOUSAND = 1000;

    /**
     * Visibility bean name constant.
     */
    public static final String VISIBILITY_BEAN = "visibilityBean";

    /**
     * Constant to represent string 'Hostgroup Name:'
     */
    public static final String HOST_GROUP_NAME = "Hostgroup Name :";

    /**
     * Constant to represent string 'Service:'
     */
    public static final String SERVICE = "Service";

    /**
     * Constant to represent string 'Service Group:'
     */
    public static final String SERVICE_GROUP = "Service Group :";

    /**
     * String constant for 'No services found for host '
     */
    public static final String NO_SERVICES_FOUND_FOR_THIS_HOST = "No services found for host ";

    /**
     * Constant to represent string 'svc_description'
     */
    public static final String SERVICE_DESC = "<svc_description>";

    /**
     * Constant for '\n' (new line) of type String
     */
    public static final String NEWLINE = "\n";

    /**
     * Constant for String "|"
     */
    public static final String PIPE = "|";

    /**
     * int constant for array index 2
     */
    public static final int INDEX_TWO = 2;

    /**
     * int constant for array index 3
     */
    public static final int INDEX_THREE = 3;

    /**
     * int constant for array index 4
     */
    public static final int INDEX_FOUR = 4;

    /**
     * int constant for array index 5
     */
    public static final int INDEX_FIVE = 5;

    /**
     * Name of the managed bean actionBean
     */
    public static final String ACTION_MANAGED_BEAN = "actionBean";

    /**
     * String constant for 'localhost'
     */
    public static final String DEFAULT_HOST_NAME = "localhost";

    /**
     * String constant for 'admin'
     */
    public static final String DEFAULT_USER_NAME = "admin";

    /**
     * String constant for 'current_users'
     */
    public static final String DEFAULT_SERVICE_NAME = "current_users";

    /**
     * String constant for 'Service Group 1'
     */
    public static final String DEFAULT_SERVICE_GROUP_NAME = "Service Group 1";

    /**
     * String constant for 'Linux servers'
     */
    public static final String DEFAULT_HOST_GROUP_NAME = "Linux Servers";

    /**
     * HASH constant
     */
    public static final String HASH = "#";

    /**
     * Managed bean for popUp
     */
    public static final String POP_UP_MANAGED_BEAN = "popup";
    /**
     * Managed bean for host availability
     */
    public static final String HOST_AVAILABILITY_MANAGED_BEAN = "hostAvailabilityBean";
    /**
     * Constant for HTML_COMMANDLINK_CLASS_NAME
     */
    public static final String HTML_COMMANDLINK_CLASS_NAME = "com.icesoft.faces.component.ext.HtmlCommandLink";

    /**
     * Constant for session attribute 'CHILD_ID'
     */
    public static final String SESSION_ATTR_CHILD_ID = "CHILD_ID";

    /**
     * Constant for session attribute 'CHILD VALUE'
     */
    public static final String SESSION_ATTR_CHILD_VALUE = "CHILD_VALUE";

    /**
     * Constant for session attribute 'PARENT_MENU'
     */
    public static final String SESSION_ATTR_PARENT_MENU = "PARENT_MENU";

    /**
     * SERVICE_INFORMATION_PATH - view file for Host Information Portlet.
     */
    public static final String SERVICE_INFORMATION_PATH = JSP_FOLDER_PATH
            + "serviceInformation.iface";

    /**
     * Managed bean for Action Handler
     */
    public static final String ACTION_HANDLER_MANAGED_BEAN = "actionHandler";

    /**
     * Constant for string 'host'
     */
    public static final String HOST = "host";

    /**
     * action listener for parent menu on actions portlet.
     */
    public static final String ACTION_LISTENER_FOR_PARENT_ACTION_MENUS = "#{actionBean.parentMenuListener}";

    /**
     * action for child menu on actions portlet.
     */
    public static final String ACTION_FOR_CHILD_ACTION_MENUS = "#{popup.openPopup}";

    /**
     * action listener for child menu on actions portlet.
     */
    public static final String ACTION_LISTENER_FOR_CHILD_ACTION_MENUS = "#{actionHandler.showPopup}";

    /**
     * default hidden text for hidden field
     */
    public static final String HIDDEN = "hidden";

    /**
     * Path of the jsp file to be displayed in VIEW mode for Filter portlet
     */
    public static final String COMMENTS_VIEW_PATH = JSP_FOLDER_PATH
            + "commentsView.iface";

    /**
     * Question Mark - ?
     */
    public static final String QUESTION_MARK = "?";

    /**
     * Equal Operator "=".
     */
    public static final String EQUALS_OPERATOR = "=";

    /**
     * constant for 'am' i.e. After 12 Midnight and before 12 Noon.
     */
    public static final String AM = "am";

    /**
     * constant for 'pm' i.e. After 12 Noon and Before 12 Midnight
     */
    public static final String PM = "pm";

    /**
     * constant for int value 12
     */
    public static final int TWELVE = 12;

    /**
     * Time format
     */
    public static final String TIME_FORMAT = "h a";

    /**
     * Constant for string '[Method] : '
     */
    public static final String METHOD = "[Method] : ";

    /**
     * int constant for 100
     */
    public static final int HUNDRED = 100;

    /**
     * sort column name for service list
     */
    public static final String SERVICE_LIST_SORT_COLUMN_NAME = "serviceDescription";

    /**
     * sort column name for host list
     */
    public static final String HOST_LIST_SORT_COLUMN_NAME = "hostName";

    /**
     * Single Quotes: '
     */
    public static final String SINGLE_QUOTES = "'";

    /**
     * Path of the jsp file to be displayed in VIEW mode for service
     * availability portlet
     */
    public static final String SERVICE_AVAILABILITY_VIEW_PATH = JSP_FOLDER_PATH
            + "serviceAvailability.iface";

    /**
     * Date format displaying date skipping the time format.
     */
    public static final String DATE_FORMAT = "MM/dd/yy";

    /**
     * Simple date format
     */
    public static final String DATE_FORMAT_HOURS_ONLY = "MM/dd/yy h a";
    /**
     * 1000 constant
     */
    public static final int ONE_THOUSEND = 1000;

    /**
     * Start time difference
     */
    public static final long START_TIME_DIFF = 7200;

    /**
     * Path of the jsp file to be displayed in EDIT mode for HostGroup
     * Statistics portlet
     */
    public static final String HOSTGROUPSTAT_EDIT_PATH = JSP_FOLDER_PATH
            + "hostGroupPref.jsp";
    /**
     * Path of the jsp file to be displayed in EDIT mode for HostGroup Network
     * prefrence
     */
    public static final String HOSTGROUPNETWORK_EDIT_PATH = JSP_FOLDER_PATH
            + "hostGroupNetworkPref.jsp";
    /**
     * Path of the jsp file to be displayed in EDIT mode for Host Statistics
     * portlet
     */
    public static final String HOSTSTAT_EDIT_PATH = JSP_FOLDER_PATH
            + "hostPref.jsp";

    /**
     * Path of the jsp file to be displayed in EDIT mode for Host information
     * portlet
     */
    public static final String HOSTINFO_EDIT_PATH = JSP_FOLDER_PATH
            + "hostInfoPref.jsp";

    /**
     * Path of the jsp file to be displayed in EDIT mode for ServiceGroup
     * Statistics portlet
     */
    public static final String SERVICEGROUPSTAT_EDIT_PATH = JSP_FOLDER_PATH
            + "serviceGroupPref.jsp";

    /**
     * Path of the jsp file to be displayed in EDIT mode for Service Statistics
     * portlet
     */
    public static final String SERVICEGROUPSTATISTICS_EDIT_PATH = JSP_FOLDER_PATH
            + "serviceStatisticsPref.jsp";

    /**
     * Path of the jsp file to be displayed in EDIT mode for Service Statistics
     * portlet
     */
    public static final String SERVICESTAT_EDIT_PATH = JSP_FOLDER_PATH
            + "serviceStatusPref.jsp";

    /**
     * Path of the jsp file to be displayed in EDIT mode for Service Statistics
     * portlet
     */
    public static final String SERVICEINFO_EDIT_PATH = JSP_FOLDER_PATH
            + "serviceStatusInfoPref.jsp";

    /**
     * Path of the jsp file to be displayed in EDIT mode for Service Statistics
     * portlet
     */
    public static final String EVENT_EDIT_PATH = JSP_FOLDER_PATH
            + "eventPref.jsp";

    /**
     * Path of the jsp file to be displayed in EDIT mode for Service List
     * portlet
     */
    public static final String SERVICELIST_EDIT_PATH = JSP_FOLDER_PATH
            + "serviceListPref.jsp";

    /**
     * Default HostGroup Pref
     */
    public static final String DEFAULT_HOSTGROUP_PREF = "defaultHostGroupPref";

    /**
     * Default Host Pref
     */
    public static final String DEFAULT_HOST_PREF = "defaultHostPref";

    /**
     * Default ServiceGroup Pref
     */
    public static final String DEFAULT_SERVICEGROUP_PREF = "defaultServiceGroupPref";

    /**
     * Default Service Pref
     */
    public static final String DEFAULT_SERVICE_PREF = "defaultServicePref";

    /**
     * Default Host Custom Link1 Pref
     */
    public static final String DEFAULT_HOST_CUST_LINK1_PREF = "defaultHostCustLink1Pref";

    /**
     * Default Host Custom Link2 Pref
     */
    public static final String DEFAULT_HOST_CUST_LINK2_PREF = "defaultHostCustLink2Pref";

    /**
     * Default Host Custom Link3 Pref
     */
    public static final String DEFAULT_HOST_CUST_LINK3_PREF = "defaultHostCustLink3Pref";

    /**
     * Default Host Custom Link4 Pref
     */
    public static final String DEFAULT_HOST_CUST_LINK4_PREF = "defaultHostCustLink4Pref";

    /**
     * Default Host Custom Link5 Pref
     */
    public static final String DEFAULT_HOST_CUST_LINK5_PREF = "defaultHostCustLink5Pref";

    /**
     * Default Service Custom Link1 Pref
     */
    public static final String DEFAULT_SERVICE_CUST_LINK1_PREF = "defaultServiceCustLink1Pref";

    /**
     * Default Service Custom Link2 Pref
     */
    public static final String DEFAULT_SERVICE_CUST_LINK2_PREF = "defaultServiceCustLink2Pref";

    /**
     * Default Service Custom Link3 Pref
     */
    public static final String DEFAULT_SERVICE_CUST_LINK3_PREF = "defaultServiceCustLink3Pref";

    /**
     * Default Service Custom Link4 Pref
     */
    public static final String DEFAULT_SERVICE_CUST_LINK4_PREF = "defaultServiceCustLink4Pref";

    /**
     * Default Service Custom Link5 Pref
     */
    public static final String DEFAULT_SERVICE_CUST_LINK5_PREF = "defaultServiceCustLink5Pref";

    /**
     * Custom Link1 Pref request attribute
     */
    public static final String HOST_CUSTLINK1_PREF_REQ_ATT = "hostCustLink1";

    /**
     * Custom Link2 Pref request attribute
     */
    public static final String HOST_CUSTLINK2_PREF_REQ_ATT = "hostCustLink2";

    /**
     * Custom Link3 Pref request attribute
     */
    public static final String HOST_CUSTLINK3_PREF_REQ_ATT = "hostCustLink3";

    /**
     * Custom Link4 Pref request attribute
     */
    public static final String HOST_CUSTLINK4_PREF_REQ_ATT = "hostCustLink4";

    /**
     * Custom Link5 Pref request attribute
     */
    public static final String HOST_CUSTLINK5_PREF_REQ_ATT = "hostCustLink5";

    /**
     * Events per page req attribute
     */
    public static final String EVENT_PER_PAGE_PREF_REQ_ATT = "eventsPerPage";

    /**
     * Default Events per page
     */
    public static final String DEFAULT_EVENT_PER_PAGE_PREF = "defaultEventsPerPage";

    /**
     * Custom Link1 Pref request attribute
     */
    public static final String SERVICE_CUSTLINK1_PREF_REQ_ATT = "serviceCustLink1";

    /**
     * Custom Link2 Pref request attribute
     */
    public static final String SERVICE_CUSTLINK2_PREF_REQ_ATT = "serviceCustLink2";

    /**
     * Custom Link3 Pref request attribute
     */
    public static final String SERVICE_CUSTLINK3_PREF_REQ_ATT = "serviceCustLink3";

    /**
     * Custom Link4 Pref request attribute
     */
    public static final String SERVICE_CUSTLINK4_PREF_REQ_ATT = "serviceCustLink4";

    /**
     * Custom Link5 Pref request attribute
     */
    public static final String SERVICE_CUSTLINK5_PREF_REQ_ATT = "serviceCustLink5";

    /**
     * HostGroup Pref request attribute
     */
    public static final String HOSTGROUP_PREF_REQ_ATT = "hostGroupPref";

    /**
     * Host Pref request attribute
     */
    public static final String HOST_PREF_REQ_ATT = "hostPref";

    /**
     * ServiceGroup Pref request attribute
     */
    public static final String SERVICEGROUP_PREF_REQ_ATT = "serviceGroupPref";

    /**
     * Service Pref request attribute
     */
    public static final String SERVICE_HOST_PREF_REQ_ATT = "serviceHostPref";

    /**
     * Service Pref request attribute
     */
    public static final String SERVICE_PREF_REQ_ATT = "servicePref";

    /**
     * Managed bean for acknowledgment pop-up bean
     */
    public static final String ACKNOWLEDGE_POPUP_MANAGED_BEAN = "acknowledgePopupBean";

    /**
     * JMS Publisher Constant for - HOST
     */
    public static final String JMS_HOST = "HOST";

    /**
     * JMS Publisher Constant for - SERVICE
     */
    public static final String JMS_SERVICESTATUS = "SERVICESTATUS";

    /**
     * JMS Publisher Constant for - ID of the entity changed
     */
    public static final String JMS_ID = "ID=";

    /**
     * Single Quotes: '
     */
    public static final String DOUBLE_QUOTE = "\"";

    /**
     * Style class for the actions portlet menu items.
     */
    public static final String ACTION_MENU_ITEMS_STYLE_CLASS = "menuBorder ";

    /**
     * Group Render Name.
     */
    public static final String GROUP_RENDER_NAME = "entity";

    /**
     * JMS Publisher Constant for - TYPE of the entity
     */
    public static final String JMS_TYPE = "TYPE";

    /**
     * JMS Publisher Constant for - ENTITY
     */
    public static final String ENTITY = "ENTITY";

    /**
     * Date format for 24-hour clock.
     */
    public static final String DATE_FORMAT_24_HR_CLK = "MM/dd/yyyy H:mm:ss";

    /**
     * JMS Publisher Constant for - HOSTGROUP
     */
    public static final String JMS_HOSTGROUP = "HOSTGROUP";
    
    /**
     * JMS Publisher Constant for - CUSTOMGROUP
     */
    public static final String JMS_CUSTOMGROUP = "CUSTOMGROUP";

    /**
     * JMS Publisher Constant for - SERVICE GROUP
     */
    public static final String JMS_SERVICEGROUP = "SERVICEGROUP";

    /**
     * TEXT
     */
    public static final String TEXT = "TEXT";

    /**
     * SEURAT_IFACE - view file for Seurat Portlet.
     */
    public static final String SEURAT_IFACE = "/jsp/seurat.iface";

    /**
     * Session Renderer Group for Comments Portlet.
     */
    public static final String COMMENTS_RENDER_GROUP = "commentsGroup";

    /**
     * Session Renderer Group for Comments Portlet.
     */
    public static final String SERVICES_RENDER_GROUP = "servicesGroup";

    /**
     * Session Renderer Group for Comments Portlet.
     */
    public static final String HOSTS_RENDER_GROUP = "hostsGroup";

    /**
     * Session Renderer Group for Information Portlet.
     */
    public static final String INFORMATION_RENDER_GROUP = "informationGroup";

    /**
     * HEALTH_RENDER_NAME Render group that contains health portlets
     */
    public static final String HEALTH_RENDER_NAME = "healthPortlets";

    /**
     * Entire Network root node
     */
    public static final String ENTIRE_NETWORK = "Entire Network";

    /**
     * Entire Network root node
     */
    public static final String SUBPAGE_TREE_PATH = "path";
    /**
     * service popup page size
     */
    public static final String SERVICE_POPUP_PAGE_SIZE = "service.summary.popup.rows";

    /**
     * NO_STATUS
     */
    public static final String NO_STATUS = "NO_STATUS";

    /**
     * Opening square bracket
     */
    public static final String OPENING_SQUARE_BRACKET = "[";

    /**
     * Closing square bracket
     */
    public static final String CLOSING_SQUARE_BRACKET = "]";

    /**
     * default graph width
     */
    public static final int DEFAULT_GRAPTH_WIDTH = 820;

    /**
     * String constant for font - 'Verdana'
     */
    public static final String FONT_FOR_AXIS_LABELS = "Verdana";

    /**
     * String constant for font name '!'.
     */
    public static final String EXCLAMATION_MARK = "!";

    /**
     * String constant for style of DIV tag in host availabilty.jspx
     */
    public static final String DIV_TAG_VISIBLE_STYLE_FOR_HOST_AVAIL = "height: auto; border: none; overflow-x:hidden;overflow-y:auto;";

    /**
     * String constant for style of DIV tag in host availabilty.jspx for
     * style=display:none
     */
    public static final String DIV_TAG_INVISIBLE_STYLE_FOR_HOST_AVAIL = "display:none";
    /**
     * Session Renderer Group for Host Availability Portlet.
     */
    public static final String HOST_AVAILABILTY_RENDER_GROUP = "hostAvailabilityEntity";

    /**
     * Session Renderer Group for Service Availability Portlet.
     */
    public static final String SERVICE_AVAILABILTY_RENDER_GROUP = "serviceAvailabilityEntity";

    /**
     * Group Render Name for actions portlet.
     */
    public static final String ACTIONS_GROUP_RENDER_NAME = "actionsEntity";

    /**
     * Group Render Name for nagios statistics portlet.
     */
    public static final String NAGIOS_STATISTICS_GROUP_RENDER_NAME = "nagiosStatisticsPortletEntity";

    /**
     * EVENT_CONTENT_PAUSE_EVENTS
     */
    public static final String EVENT_CONTENT_PAUSE_EVENTS = "com_groundwork_portal_statusviewer_Event_button_pause";
    /**
     * EVENT_CONTENT_RESUME_EVENTS
     */
    public static final String EVENT_CONTENT_RESUME_EVENTS = "com_groundwork_portal_statusviewer_Event_button_resume";

    /**
     * eventFreezeBean content
     */
    public static final String EVENT_FREEZE_BEAN = "eventFreezeBean";

    /**
     * host group summary pop up row size
     */
    public static final String HOST_GROUP_SUMMARY_POPUP_ROWS = "host.group.summary.popup.rows";
    /**
     * service group summary pop up data table size
     */
    public static final String SERVICE_GROUP_SUMMARY_POPUP_ROWS = "service.group.summary.popup.rows";

    /**
     * Constant for => " : "
     */
    public static final String SPACE_COLON_SPACE = " : ";

    /**
     * ELLIPSES
     */
    public static final String ELLIPSES = "...";

    /**
     * UNSERSCORE String
     */
    public static final String UNSERSCORE = "_";

    /**
     * CSS class for the nagios monitoring portlet when a particular monitoring
     * options is enabled.
     */
    public static final String CSS_NAGIOS_GREEN_HEADER = "iceDatTblColHdr1_typD";

    /**
     * CSS class for the nagios monitoring portlet when a particular monitoring
     * options is globally disabled.
     */
    public static final String CSS_NAGIOS_GRAY_HEADER = "iceDatTblColHdr1_typE";

    /**
     * DYNAMIC_COLUMN_SERVICE
     */
    public static final String DYNAMIC_COLUMN_SERVICE = "service";

    /**
     * table header
     */
    public static final String TABLE_HEADER = "table-header";

    /**
     * DYNAMIC_PROPERTY
     */
    public static final String DYNAMIC_PROPERTY = "dynamicProperty.";

    /**
     * LOG_MESSAGE
     */
    public static final String LOG_MESSAGE = "LOG_MESSAGE";

    /**
     * APP_TYPE_SYSTEM
     */
    public static final String APP_TYPE_SYSTEM = "SYSTEM";

    /**
     * APP_TYPE_NAGIOS
     */
    public static final String APP_TYPE_NAGIOS = "NAGIOS";

    /**
     * NodeName preference parameter
     */
    public static final String NODE_NAME_PREF = "gw_nodeName";

    /**
     * NodeType preference parameter
     */
    public static final String NODE_TYPE_PREF = "nodeType";

    /**
     * Preference parameter for "Do not show services in state OK" filter.
     */
    public static final String SERVICE_FILTER_OK_PREF = "serviceFilterOK";

    /**
     * Preference parameter for "Do not show services in state WARINING" filter.
     */
    public static final String SERVICE_FILTER_WARNING_PREF = "serviceFilterWARNING";

    /**
     * Preference parameter for "Do not show services in state CRITICAL" filter.
     */
    public static final String SERVICE_FILTER_CRITICAL_PREF = "serviceFilterCRITICAL";

    /**
     * Preference parameter for "Do not show services in state OK scheduled" filter.
     */
    public static final String SERVICE_FILTER_CRITICAL_SCHEDULED_PREF = "serviceFilterCRITICALscheduled";

    /**
     * Preference parameter for "Do not show services in state OK unscheduled" filter.
     */
    public static final String SERVICE_FILTER_CRITICAL_UNSCHEDULED_PREF = "serviceFilterCRITICALunscheduled";

    /**
     * Preference parameter for "Do not show services in state UNKNOWN" filter.
     */
    public static final String SERVICE_FILTER_UNKNOWN_PREF = "serviceFilterUNKNOWN";

    /**
     * Preference parameter for "Do not show services in state PENDING" filter.
     */
    public static final String SERVICE_FILTER_PENDING_PREF = "serviceFilterPENDING";

    /**
     * Preference parameter for "Do not show services in state UNKNOWN" filter.
     */
    public static final String SERVICE_FILTER_ACKNOWLEDGED_PREF = "serviceFilterACKNOWLEDGED";

    /**
     * Preference parameter for "Do not show Hosts in state UP" filter.
     */
    public static final String HOST_FILTER_UP_PREF = "hostFilterUP";

    /**
     * Preference parameter for "Do not show Hosts in state DOWN (unscheduled) " filter.
     */
    public static final String HOST_FILTER_DOWN_UNSCHEDULED_PREF = "hostFilterDOWNUNSCHEDULED";

    /**
     * Preference parameter for "Do not show Hosts in state DOWN (scheduled) " filter.
     */
    public static final String HOST_FILTER_DOWN_SCHEDULED_PREF = "hostFilterDOWNSCHEDULED";    
    /**
     * Preference parameter for "Do not show Hosts in state UNREACHABLE" filter.
     */
    public static final String HOST_FILTER_UNREACHABLE_PREF = "hostFilterUNREACHABLE";
    /**
     * Preference parameter for "Do not show Hosts in state PENDING" filter.
     */
    public static final String HOST_FILTER_PENDING_PREF = "hostFilterPENDING";
    
    /**
     * Preference parameter for "Do not show ACKNOWLEDGED Hosts" filter.
     */
    public static final String HOST_FILTER_ACKNOWLEDGED_PREF = "hostFilterACKNOWLEDGED";

    
    /**
     * Constant for "false".
     */
    public static final String FALSE_CONSTANT = "false";

    /**
     * Preference parameter for "services per page" in Service List portlet
     */
    public static final String SERVICES_PER_PAGE_PREF = "servicesPerPage";

    /**
     * Acknowledge Parameter for host name
     */
    public static final String ACKNOWLEDGE_PARAM_HOST_NAME = "hostName";

    /**
     * Acknowledge Parameter for service name
     */
    public static final String ACKNOWLEDGE_PARAM_SERVICE_NAME = "serviceName";

    /**
     * float constant used for setting the widht of the vertical grid lines of
     * date axis on availability portlets.
     */
    public static final float RANGEAXIS_LINES_WIDTH = 1.0f;

    /**
     * int constant for value 30
     */
    public static final int THIRTY = 30;

    /**
     * padding class for host and service Availability portlet
     */
    public static final String PAD_RIGHT135 = "padRight130";

    /**
     * PERF_MEASUREMENT_ERROR_MESSAGE
     */
    public static final String PERF_MEASUREMENT_ERROR_MESSAGE = "com_groundwork_portal_statusviewer_perf_measurement_error_msg";

    /**
     * String constant for style of DIV tag in commentsView.jspx for Service
     * context
     */
    public static final String DIV_TAG_STYLE_COMMENTS_SERVICE = "height:140px ; width:470px ; overflow-x:auto;overflow-y:auto;";

    /**
     * String constant for style of DIV tag in commentsView.jspx for Host
     * context
     */
    public static final String DIV_TAG_STYLE_COMMENTS_HOST = "height:140px ; width:750px ; overflow-x:auto;overflow-y:auto;";

    /**
     * int cosntant for the value 35.
     */
    public static final int THIRTY_FIVE = 80;

    /**
     * int cosntant for the value 19.
     */
    public static final int NINETEEN = 19;

    /**
     * Pattern for numeric value.
     */
    public static final String NUMERIC_VALUE_PATTERN = "[0-9]+";

    /**
     * Pattern for check output field on actions portlet pop-up.
     */
    public static final String CHK_OUTPUT_PATTERN = "[a-zA-Z0-9[^;]]+";

    /**
     * nagios portlet column css class
     */
    public static final String ICE_DAT_TBL_COL1_TYP_E = "iceDatTblCol1_typE";

    /**
     * nagios portlet row css class
     */
    public static final String ICE_DAT_TBL_ROW1_TYP_D = "iceDatTblRow1_typD";

    /**
     * nagios portlet row css class
     */
    public static final String ICE_DAT_TBL_ROW1_TYP_E = "iceDatTblRow1_typE";

    /**
     * nagios portlet column header css class
     */
    public static final String ICE_DAT_TBL_COL_HDR1_TYP_E = "iceDatTblColHdr1_typE";

    /**
     * nagios portlet column css class
     */
    public static final String ICE_DAT_TBL_COL1_TYP_D = "iceDatTblCol1_typD";

    /**
     * nagios portlet column header css class
     */
    public static final String ICE_DAT_TBL_COL_HDR1_TYP_D = "iceDatTblColHdr1_typD";

    /**
     * nagios portlet css class
     */
    public static final String ICE_DAT_TBL_TYP_D = "iceDatTbl_typD";

    /**
     * nagios portlet css class
     */
    public static final String ICE_DAT_TBL_TYP_E = "iceDatTbl_typE";

    /**
     * PLUS
     */
    public static final String PLUS = "+";

    /**
     * WIDTH_300PX style
     */
    public static final String WIDTH_300PX = "width: 300px;";
    /**
     * WIDTH_490PX style
     */
    public static final String WIDTH_490PX = "width:490px;";

    /**
     * constant for "<br>
     * "
     */
    public static final String BR = "<br>";

    /**
     * Localized String for "Yes"
     */
    public static final String YES_STRING = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_dashboard_seurat_yes");

    /**
     * Localized String for "No"
     */
    public static final String NO_STRING = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_dashboard_seurat_no");

    /**
     * Localized String for "N/A"
     */
    public static final String NOT_AVAILABLE_STRING = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_dashboard_seurat_NA");

    /**
     * ACK_POPUP_STATUSVIEWER style
     */
    public static final String ACK_NAGIOS_ERR_POPUP_STATUSVIEWER_STYLE = "z-index:999; width: 500px; height: 155px; background: #FFFFFF;";

    /**
     * ACK_POPUP_DASHBOARD style
     */
    public static final String ACK_NAGIOS_ERR_POPUP_DASHBOARD_STYLE = "z-index:999; top: 20%; left: 100px; width: 500px; height: 200px; position:relative; background: #FFFFFF;";

    /**
     * ACK_POPUP_STATUSVIEWER style
     */
    public static final String ACK_POPUP_STATUSVIEWER_STYLE = "z-index:999; width: 900px; height: 200px; background: #FFFFFF;";

    /**
     * ACK_POPUP_DASHBOARD style
     */
    public static final String ACK_POPUP_DASHBOARD_STYLE = "z-index:999; top: 20%; left: 100px; width: 900px; height: 200px; position:relative; background: #FFFFFF;";

    /**
     * Event status type pending (Upper case)
     */
    public static final String PENDING_UPPER_CASE = "PENDING";

    /**
     * View file for Perf Measurement Portlet In EE.
     */
    public static final String PERF_MEASUREMENT_PATH_EE = JSP_FOLDER_PATH
            + "perfmeasurementEE.iface";
    /**
     * HostGroupView namespace
     */
    public static final String HOSTGROUPVIEW = "HostGroupView";
    /**
     * ServiceGroupView namespace
     */
    public static final String SERVICEGROUPVIEW = "ServiceGroupView";
    /**
     * HostView namespace
     */
    public static final String HOSTVIEW = "HostView";
    /**
     * ServiceView namespace
     */
    public static final String SERVICEVIEW = "ServiceView";

    /**
     * Default seurat entire n/w Pref
     */
    public static final String SEURAT_ENTIRENETWORK_PREF = "seuratEntNetPref";

    /**
     * Path of the jsp file to be displayed in EDIT mode for seurat portlet
     */
    public static final String SEURAT_EDIT_PATH = "/jsp/seuratPref.jsp";

    /**
     * TEN_HOUSANED constant
     */
    public static final int TEN_HOUSANED = 10000;

    /**
     * Pattern for check output field on actions portlet pop-up.
     */
    public static final String COMMENTS_PATTERN = "[a-zA-Z0-9]+";

    /**
     * HOST_INFORMATION_PATH - view file for Host Information Portlet.
     */
    public static final String DATE_TIME_PORTLET_PATH = JSP_FOLDER_PATH
            + "dateTime.iface";

    /**
     * DATE_FORMAT_FOR_DATETIME_PORTLET
     */
    public static final String DATE_FORMAT_FOR_DATETIME_PORTLET = "EEEE, MMMM d yyyy HH:mm:ss a z";

    /**
     * CUSTOM DATE DELIMITERS
     */
    public static final String CUSTOM_DATE_DELIMITERS = "#@%$*&";

    /**
     * USER_ROLE_BEAN
     */
    public static final String USER_ROLE_BEAN = "userRoleBean";

    /**
     * USER_EXTENDED_ROLE_BEAN
     */
    public static final String USER_EXTENDED_ROLE_BEAN = "userExtendedRoleBean";

    /**
     * PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE
     */
    public static final String PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE = "defaultHostGroupPreference";

    /**
     * HOSTGROUP_NAME_LINUX
     */
    public static final String HOSTGROUP_NAME_LINUX = "#L!nux#";

    /**
     * PORTLET_XML_DEFAULT_SERVICEGROUP_PREFERENCE
     */
    public static final String PORTLET_XML_DEFAULT_SERVICEGROUP_PREFERENCE = "defaultServiceGroupPreference";
    /**
     * Default Service Group Name.
     */
    public static final String SERVICEGROUP_NAME_DEFAULT = "!D#fault!";

    /**
     * 15 Constant
     */
    public static final int FIFTEEN = 15;

    /**
     * Preference parameter for "Hosts per page" in Service List portlet
     */
    public static final String HOSTS_PER_PAGE_PREF = "hostsPerPage";

    /**
     * seurat
     */
    public static final String SEURAT = "seurat";
    
    /**
     * UI HostGroup Constant
     */
    public static final String UI_HOST_GROUP = "Host Group";
    
    /**
     * UI ServiceGroup Constant
     */
    public static final String UI_SERVICE_GROUP = "Service Group";
    
    /**
     * UI CustomGroup Constant
     */
    public static final String UI_CUSTOM_GROUP = "Custom Group";
    
    /**
     * DB HostGroup Constant
     */
    public static final String DB_HOST_GROUP = "HostGroup";
    
    /**
     * DB ServiceGroup Constant
     */
    public static final String DB_SERVICE_GROUP = "ServiceGroup";
    
    /**
     * DB CustomGroup Constant
     */
    public static final String DB_CUSTOM_GROUP = "CustomGroup";
    
    public static final String VEMA = "VEMA";
    
    public static final String IS_IN_SV_CONSTANT = "IsInStatusViewer";

    /**
     * healthPortletsHandler
     */
    public static final String HEALTH_PORTLETS_HANDLER_BEAN = "healthPortletsHandler";

}
