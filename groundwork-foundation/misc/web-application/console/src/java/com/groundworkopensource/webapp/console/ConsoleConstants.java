/*
* 
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/
package com.groundworkopensource.webapp.console;



public interface ConsoleConstants {
	
	public static final String SYSTEM_FILTER_HOST_GROUPS=ResourceUtils.getLocalizedMessage("com_groundwork_console_navigation_host_groups_label");
	public static final String SYSTEM_FILTER_SERVICE_GROUPS=ResourceUtils.getLocalizedMessage("com_groundwork_console_navigation_service_groups_label");
	public static final String SYSTEM_FILTER_APPLICATIONS= ResourceUtils.getLocalizedMessage("com_groundwork_console_navigation_applications_label");
	public static final String SYSTEM_FILTER_OPERATION_STATUS= ResourceUtils.getLocalizedMessage("com_groundwork_console_navigation_operation_status_label");
	public static final String BRANCH_CONTRACTED_ICON = "assets/tree/folder.gif";
	public static final String BRANCH_EXPANDED_ICON = "assets/tree/folder_bk.gif";
	public static final String LEAF_ICON = "assets/icons/filter.gif";
	public static final String BRANCH_LEAF_ICON = "/images/filter.gif";
	public static final String FILTER_EVENTS= ResourceUtils.getLocalizedMessage("com_groundwork_console_navigation_filter_events_label");
	public static final String PUBLIC_FILTER_CRITICAL_EVENTS="Critical Events";
	public static final String STYLE_NONE="none";
	
	//Tabs

	
	
	public static final String CONSOLE_PROPS = "consoleProps";
	
	public static final String SESSION_LOGIN_USER = "login_user";
	
	public static final String SORT_ARROW_UP="images/sort_arrow_up.gif";
	public static final String SORT_ARROW_DOWN="images/sort_arrow_down.gif";
	
	public static final String MENU_LABEL="Actions";
	public static final String MENU_ID="actions";
	//public static final String MENU_ACTION_LISTENER_BIND= "#{tabset.tabs[tabset.tabIndex].actionBean.menuListener}";
	public static final String MENU_ICON_OFF = "/images/actions_off.png";
	public static final String MENU_ICON_ON = "/images/actions.png";
	public static final String MENU_ITEM_ACTION_LISTENER_BIND="#{tabset.tabs[tabset.tabIndex].actionBean.menuItemListener}";
	
	
	public static final String PROP_NAME_OPERATION_STATUS ="operationStatus.name";
	public static final String PROP_NAME_APP_TYPE ="applicationType.name";
	public static final String PROP_NAME_DEVICE ="device.hosts.hostGroups.name";
	public static final String PROP_NAME_CATEGORY_NAME ="category.name";
	public static final String PROP_NAME_MONITOR_STATUS ="monitorStatus.name";
	public static final String PROP_NAME_MONITOR_SEVERITY ="severity.name";
	public static final String PROP_NAME_APP_TYPE_SYSTEM ="SYSTEM";
	public static final String ACTION_RETURN_SUCCESS = "SUCCESS";
	public static final String ACTION_RETURN__HTTP_OK = "200";
	
	public static final String TOOLTIP_HIDE_NAVIGATION = "Hide Navigation";
	public static final String TOOLTIP_SHOW_NAVIGATION = "Show Navigation";
	public static final String ICON_SIDEBAR_COLLAPSE = "sideBarCollapseIcon";
	public static final String STYLE_LAYOUTWRAPPER = "layoutWrapper";
	public static final String ICON_SIDEBAR_COLLAPSE_HIDE = "sideBarCollapseIconHide";
	public static final String STYLE_LAYOUTWRAPPER_HIDE_SIDEBAR = "layoutWrapper hideSideBar";
	public static final String IMG_SIDEBAR_ARROW_RIGHT = "assets/icons/sideBarArrowRight.gif";
	public static final String IMG_SIDEBAR_ARROW_LEFT = "assets/icons/sideBarArrowLeft.gif";
	
	public static final String REQ_PARAM_USER = "user";
	public static final String REQ_PARAM_LOGOUT = "logout";
	public static final String REQ_PARAM_SESSIONID = "sessionid";
	public static final String LOGOUT_TRUE = "true";
	
	public static final String MANAGED_BEAN_CONSOLE_MGR = "consoleMgr";
	public static final String MANAGED_BEAN_FILTER_TREE = "filterTreeBean";
	public static final String MANAGED_BEAN_PUBLIC_FILTER_TREE = "publicFilterTreeBean";
	public static final String MANAGED_BEAN_FILTER = "filterBean";
	public static final String MANAGED_BEAN_TABSET = "tabset";
	public static final String MANAGED_BEAN_CONSOLE = "console";
	public static final String MANAGED_BEAN_LOCALE = "locale";
	public static final String MANAGED_BEAN__POPUP= "popup";
	public static final String MANAGED_BEAN__PANELSTACK= "panelStack";
	
	public static final String FILTER_DEVICE = "device.displayName";
	public static final String FILTER_REPORT_DATE = "reportDate";
	public static final String FILTER_TEXT_MESSAGE = "textMessage";
	public static final String FILTER_SERVICE_STATUS_ID = "serviceStatus.serviceStatusId";
		
	public static final String CONSOLE_PROP_PATH = "/usr/local/groundwork/config/console.properties";
	public static final String CONSOLE_PROP_FALLBACK_PATH = "WEB-INF/classes/console.properties";
	public static final String CONSOLE_ADMIN_CONFIG_PATH = "/usr/local/groundwork/config/console-admin-config.xml";
	public static final String CONSOLE_ADMIN_FALLBACK_CONFIG_PATH = "WEB-INF/classes/console-admin-config.xml";
	public static final String CONSOLE_ADMIN_CONFIG_PROP = "console-admin-config";
	
	public static final String FOUNDATION_END_POINT_PORT_NAME ="wscommon";
	public static final String FOUNDATION_END_POINT_HOST_GROUP = "wshostgroup";
	public static final String FOUNDATION_END_POINT_EVENT = "wsevent";
	public static final String FOUNDATION_END_POINT_CATEGORY = "wscategory";
	public static final String FOUNDATION_END_POINT_HOST = "wshost";
	public static final String FOUNDATION_END_POINT_SERVICE = "wsservice";
	
	public static final String PROP_WS_URL = "foundation_webserviceURL";
	public static final String PROP_PAGE_SIZE = "page_size";
	public static final String PROP_NAGIOS_SEND_NOTIFY= "nagios_send_notification";
	public static final String PROP_NAGIOS_PERSIST_COMMENT = "nagios_persistent_comment";
	public static final String PROP_BUILTIN_COLS = "built_in_columns";
	public static final String PROP_FACTORY_INIT = "java.naming.factory.initial";
	public static final String PROP_FACTORY_HOST = "java.naming.factory.host";
	public static final String PROP_FACTORY_PORT = "java.naming.factory.port";
	public static final String PROP_CONTEXT_FACTORY="context.factory";
	public static final String PROP_TOPIC_NAME = "topic.name";
	
	public static final String ACTION_PARAM_LOG_MESS_IDS=    "LogMessageIds";
	public static final String ACTION_PARAM_USER_NAME = "UserName";
	public static final String ACTION_PARAM_SEND_NOTIFY = "SendNotification";
	public static final String ACTION_PARAM_PERSIST_COMMENT = "PersistentComment";
	public static final String ACTION_PARAM_COMMENT = "Comment";
	public static final String ACTION_PARAM_VALUE_COMMENT_PREFIX = "Acknowledged from console at ";
	
	// For SNMPTRAP, SYSLOG params
	public static final String ACTION_PARAM_NSCA_HOST = "nsca_host";
	public static final String ACTION_PARAM_USER= "user";
	public static final String ACTION_PARAM_NSCA_COMMENT = "comment";
	public static final String ACTION_PARAM_HOST = "host";
	public static final String ACTION_PARAM_SERVICE = "service";
	public static final String ACTION_PARAM_STATE = "state";
	
	public static final String EL_BIND_EVENT_SEL =  "#{event.selected}";
	public static final String EL_BIND_TABLE_MULTIPLE_SEL = "#{tabset.tabs[tabset.tabIndex].dataTableBean.multipleSelection}";
	public static final String EL_BIND_ROW_SEL="#{tabset.tabs[tabset.tabIndex].dataTableBean.rowSelection}";
	public static final String EL_BIND_TABLE_SORT="#{tabset.tabs[tabset.tabIndex].dataTableBean.sort}";
	public static final String EL_BIND_COL_MON_STATUS="monitorStatus";
	public static final String EL_BIND_HTML_ATT_STYLECLASS="styleClass";
	public static final String EL_BIND_ATT_VALUE="value";
	public static final String EL_BIND_ATT_MULTIPLE="multiple";
	public static final String STYLE_TABLE_COL = "tableColumn";
	public static final String EL_BIND_EVENT_LEFT="#{event.";
	public static final String EL_BIND_VALUE_RIGHT=".value}";
	public static final String EL_BIND_STYLE_RIGHT= ".styleClass}";
	
	
	public static final String MON_STATUS_WARN = "warning";
	public static final String MON_STATUS_CRITICAL = "critical";
	public static final String MON_STATUS_UNKNOWN="unknown";
	public static final String MON_STATUS_OK="ok";
	public static final String MON_STATUS_UP="up";
	public static final String MON_STATUS_UNREACHABLE="unreachable";
	public static final String MON_STATUS_DOWN="down";
	public static final String MON_STATUS_PENDING="pending";
	
	public static final String OPERATION_CRITICAL = "CRITICAL";
	public static final String OPERATION_STATUS_OPEN = "OPEN";
	
	
	public static final String RENDERER ="renderer";
	public static final String DEFAULT_SORT_COLUMN = "reportDate";
	
	public static final String ENTITY_NAME_SERVICEGROUP = "SERVICE_GROUP";
	
	public static final String STYLE_HIGHLIGHT_NAVIGATOR = "highlightNavigator";
	
	public static final String IMG_SEARCHBAR_UP = "assets/icons/collapseArrowUp.gif";
	public static final String IMG_SEARCHBAR_DOWN = "assets/icons/collapseArrowDown.gif";
	public static final String STYLE_SHOW_SEARCHPANEL = "overflow: visible;display: block";
	public static final String STYLE_HIDE_SEARCHPANEL = "overflow: visible;display: none";
	//public static final String IMG_PAUSE_EVENTS = "assets/buttons/btn_pause_on.gif";
	public static final String IMG_PAUSE_EVENTS = "assets/icons/media_pause.png";
	public static final String IMG_PLAY_EVENTS = "images/play_messages.gif";
	//public static final String IMG_PLAY_EVENTS = "assets/buttons/btn_play_on.gif";
	
	public static final String APP_TYPE_SNMPTRAP = "SNMPTRAP";
	public static final String APP_TYPE_SYSLOG = "SYSLOG";
	public static final String APP_TYPE_NAGIOS = "NAGIOS";
	public static final String SERVICE_SNMPTRAP_LAST = "snmptraps_last";
	public static final String SERVICE_SYSLOG_LAST = "syslog_last";
	public static final String DEFAULT_NSCA_HOST = "localhost";
	public static final String DEFAULT_NSCA_STATE = "0";
	public static final String SUBMIT_PASSIVE_RESET_COMMENT = "Manual_reset_by_";
	public static final String NAGIOS_SERVICE_COLUMN = "service";
	public static final String NAGIOS_SUBCOMPONENT_COLUMN = "SubComponent";
	public static final String DELIM_COLON = ":";
	public static final String ENTITY_TYPE_LOGMESSAGE="LOG_MESSAGE";
	public static final String NOT_AVAILABLE="NOT AVAILABLE";
	
	// I18N properties
	public static final String  I18N_CONSOLE_NAVI_SYSTEM_FILTER="com_groundwork_console_navigation_system_filters";
	public static final String  I18N_CONSOLE_NAVI_PUBLIC_FILTER="com_groundwork_console_navigation_public_filters";
	public static final String  I18N_CONSOLE_CONTENT_TITLE=" com_groundwork_console_content_title";
	public static final String  I18N_CONSOLE_CONTENT_TAB_DEFAULT="com_groundwork_console_content_tab_default";
	public static final String  I18N_CONSOLE_CONTENT_TAB_NEW="com_groundwork_console_content_tab_new";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_TITLE="com_groundwork_console_content_search_title";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_DEVICE="com_groundwork_console_content_search_device";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_MESSAGES="com_groundwork_console_content_search_messages";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_DATETIME="com_groundwork_console_content_search_datetime";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_PRESET="com_groundwork_console_content_search_preset";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_RESET= "com_groundwork_console_content_search_custom";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_HEADER_UPDATE_LABEL="com_groundwork_console_content_search_header_updatelabel";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_BUTTON_UPDATE_LABEL="com_groundwork_console_content_search_button_updatelabel";
	public static final String  I18N_CONSOLE_CONTENT_SEARCH_BUTTON="com_groundwork_console_content_search_button";
	public static final String  I18N_CONSOLE_CONTENT_RESET_BUTTON= "com_groundwork_console_content_search_button_reset";
	public static final String  I18N_CONSOLE_CONTENT_SELECT_ALL="com_groundwork_console_content_actionbar_button_selectall";
	public static final String  I18N_CONSOLE_CONTENT_DESELECT_ALL="com_groundwork_console_content_actionbar_button_deselectall";
	public static final String  I18N_CONSOLE_CONTENT_ACTIONS="com_groundwork_console_content_actionbar_button_actions";
	public static final String  I18N_CONSOLE_CONTENT_PAUSE_EVENTS="com_groundwork_console_content_actionbar_button_pause";
	public static final String  I18N_CONSOLE_CONTENT_RESUME_EVENTS="com_groundwork_console_content_actionbar_button_resume";
	public static final String  I18N_CONSOLE_CONTENT_TAB_LABEL_APPTYPE = "com_groundwork_console_content_tab_header_apptypes";
	public static final String  I18N_CONSOLE_CONTENT_TAB_LABEL_OPSTATUS = "com_groundwork_console_content_tab_header_opstatus";
	public static final String  I18N_CONSOLE_CONTENT_TAB_LABEL_EVENT_BY=	"com_groundwork_console_content_tab_header_eventsby";
	public static final String  I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS=	"com_groundwork_console_content_tab_header_hostgroups";
	public static final String  I18N_CONSOLE_CONTENT_TAB_LABEL_SERVICE_GRPS=	"com_groundwork_console_content_tab_header_servicegroups";
	public static final String  I18N_CONSOLE_ERROR_PAGINATION = "com_groundwork_console_error_message_select_and_pagination";
	public static final String  I18N_CONSOLE_ERROR_INVALID_SEARCH ="com_groundwork_console_error_invalid_search";
	public static final String  I18N_CONSOLE_ERROR_INVALID_DATERANGE ="com_groundwork_console_error_invalid_daterange";
	
	public static final String  I18N_GLOBAL_ERROR_MESSAGE1 ="com_groundwork_global_error_message1";
	public static final String  I18N_GLOBAL_ERROR_MESSAGE2 ="com_groundwork_global_error_message2";
	public static final String  I18N_GLOBAL_ERROR_MESSAGE3 ="com_groundwork_global_error_message3";
}
