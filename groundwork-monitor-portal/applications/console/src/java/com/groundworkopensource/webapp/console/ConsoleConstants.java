/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

/**
 * interface ConsoleConstants.
 */
public interface ConsoleConstants {

    /** The SYSTE m_ filte r_ hos t_ groups. */
    String SYSTEM_FILTER_HOST_GROUPS = ResourceUtils
            .getLocalizedMessage("com_groundwork_console_navigation_host_groups_label");

    /** The SYSTE m_ filte r_ servic e_ groups. */
    String SYSTEM_FILTER_SERVICE_GROUPS = ResourceUtils
            .getLocalizedMessage("com_groundwork_console_navigation_service_groups_label");

    /** The SYSTE m_ filte r_ applications. */
    String SYSTEM_FILTER_APPLICATIONS = ResourceUtils
            .getLocalizedMessage("com_groundwork_console_navigation_applications_label");

    /** The SYSTE m_ filte r_ operatio n_ status. */
    String SYSTEM_FILTER_OPERATION_STATUS = ResourceUtils
            .getLocalizedMessage("com_groundwork_console_navigation_operation_status_label");

    /** The BRANC h_ contracte d_ icon. */
    String BRANCH_CONTRACTED_ICON = "/assets/tree/folder.gif";

    /** The BRANC h_ expande d_ icon. */
    String BRANCH_EXPANDED_ICON = "/assets/tree/folder_bk.gif";

    /** The LEA f_ icon. */
    String LEAF_ICON = "/assets/icons/filter.gif";

    /** The BRANC h_ lea f_ icon. */
    String BRANCH_LEAF_ICON = "/images/filter.gif";

    /** The FILTE r_ events. */
    String FILTER_EVENTS = ResourceUtils
            .getLocalizedMessage("com_groundwork_console_navigation_filter_events_label");

    /** The PUBLI c_ filte r_ critica l_ events. */
    String PUBLIC_FILTER_CRITICAL_EVENTS = "Critical Events";

    /** The STYL e_ none. */
    String STYLE_NONE = "none";

    // Tabs

    /** The CONSOL e_ props. */
    String CONSOLE_PROPS = "consoleProps";

    /** The SESSIO n_ logi n_ user. */
    String SESSION_LOGIN_USER = "login_user";

    /** The SOR t_ arro w_ up. */
    String SORT_ARROW_UP = "/images/sort_arrow_up.gif";

    /** The SOR t_ arro w_ down. */
    String SORT_ARROW_DOWN = "/images/sort_arrow_down.gif";

    /** The MEN u_ label. */
    String MENU_LABEL = "Actions";

    /** The MEN u_ id. */
    String MENU_ID = "actions";
    // String MENU_ACTION_LISTENER_BIND=
    // "#{tabset.tabs[tabset.tabIndex].actionBean.menuListener}";
    /** The MEN u_ ico n_ off. */
    String MENU_ICON_OFF = "/images/actions_off.png";

    /** The MEN u_ ico n_ on. */
    String MENU_ICON_ON = "/images/actions.png";

    /** The MEN u_ ite m_ actio n_ listene r_ bind. */
    String MENU_ITEM_ACTION_LISTENER_BIND = "#{tabset.tabs[tabset.tabIndex].actionBean.menuItemListener}";

    /** The PRO p_ nam e_ operatio n_ status. */
    String PROP_NAME_OPERATION_STATUS = "operationStatus.name";

    /** The PRO p_ nam e_ ap p_ type. */
    String PROP_NAME_APP_TYPE = "applicationType.name";

    /** The PRO p_ nam e_ device. */
    String PROP_NAME_DEVICE = "device.hosts.hostGroups.name";

    /** The PRO p_ nam e_ categor y_ name. */
    String PROP_NAME_CATEGORY_NAME = "category.name";

    /** The PRO p_ nam e_ monito r_ status. */
    String PROP_NAME_MONITOR_STATUS = "monitorStatus.name";

    /** The PRO p_ nam e_ monito r_ severity. */
    String PROP_NAME_MONITOR_SEVERITY = "severity.name";

    /** The PRO p_ nam e_ ap p_ typ e_ system. */
    String PROP_NAME_APP_TYPE_SYSTEM = "SYSTEM";

    /** The ACTIO n_ retur n_ success. */
    String ACTION_RETURN_SUCCESS = "SUCCESS";

    /** The ACTIO n_ retur n__ htt p_ ok. */
    String ACTION_RETURN__HTTP_OK = "200";

    /** The TOOLTI p_ hid e_ navigation. */
    String TOOLTIP_HIDE_NAVIGATION = "Hide Navigation";

    /** The TOOLTI p_ sho w_ navigation. */
    String TOOLTIP_SHOW_NAVIGATION = "Show Navigation";

    /** The ICO n_ sideba r_ collapse. */
    String ICON_SIDEBAR_COLLAPSE = "sideBarCollapseIcon";

    /** The STYL e_ layoutwrapper. */
    String STYLE_LAYOUTWRAPPER = "layoutWrapper";

    /** The ICO n_ sideba r_ collaps e_ hide. */
    String ICON_SIDEBAR_COLLAPSE_HIDE = "sideBarCollapseIconHide";

    /** The STYL e_ layoutwrappe r_ hid e_ sidebar. */
    String STYLE_LAYOUTWRAPPER_HIDE_SIDEBAR = "layoutWrapper hideSideBar";

    /** The IM g_ sideba r_ arro w_ right. */
    String IMG_SIDEBAR_ARROW_RIGHT = "/assets/icons/sideBarArrowRight.gif";

    /** The IM g_ sideba r_ arro w_ left. */
    String IMG_SIDEBAR_ARROW_LEFT = "/assets/icons/sideBarArrowLeft.gif";

    /** The RE q_ para m_ user. */
    String REQ_PARAM_USER = "user";

    /** The RE q_ para m_ logout. */
    String REQ_PARAM_LOGOUT = "logout";

    /** The RE q_ para m_ sessionid. */
    String REQ_PARAM_SESSIONID = "sessionid";

    /** The LOGOU t_ true. */
    String LOGOUT_TRUE = "true";

    /** The MANAGE d_ bea n_ consol e_ mgr. */
    String MANAGED_BEAN_CONSOLE_MGR = "consoleMgr";

    /** The MANAGE d_ bea n_ filte r_ tree. */
    String MANAGED_BEAN_FILTER_TREE = "filterTreeBean";

    /** The MANAGE d_ bea n_ publi c_ filte r_ tree. */
    String MANAGED_BEAN_PUBLIC_FILTER_TREE = "publicFilterTreeBean";

    /** The MANAGE d_ bea n_ filter. */
    String MANAGED_BEAN_FILTER = "filterBean";

    /** The MANAGE d_ bea n_ tabset. */
    String MANAGED_BEAN_TABSET = "tabset";

    /** The MANAGE d_ bea n_ console. */
    String MANAGED_BEAN_CONSOLE = "console";
    
    /** The MANAGE d_ bea n_ RTMM. */
    String MANAGED_BEAN_RTMM = "referenceTree";


    /** The MANAGE d_ bea n_ locale. */
    String MANAGED_BEAN_LOCALE = "locale";

    /** The MANAGE d_ bea n__ popup. */
    String MANAGED_BEAN__POPUP = "popup";
    
    /** The MANAGE d_ bea n__ popup. */
    String MANAGED_BEAN_ACTION = "actionBean";

    /** The MANAGE d_ bea n__ panelstack. */
    String MANAGED_BEAN__PANELSTACK = "panelStack";

    /** The FILTE r_ device. */
    String FILTER_DEVICE = "device.displayName";
    
    /** The FILTE r_ device. */
    String FILTER_SEVERITY = "severity.name";
    
    /** The FILTE r_ device. */
    String FILTER_OPSTATUS = "operationStatus.name";
    
    /** The FILTE r_ device. */
    String FILTER_MONSTAUTS = "monitorStatus.name";

    /** The FILTE r_ repor t_ date. */
    String FILTER_REPORT_DATE = "reportDate";

    /** The FILTE r_ tex t_ message. */
    String FILTER_TEXT_MESSAGE = "textMessage";

    /** The FILTE r_ servic e_ statu s_ id. */
    String FILTER_SERVICE_STATUS_ID = "serviceStatus.serviceStatusId";

    /** The CONSOL e_ pro p_ path. */
    String CONSOLE_PROP_PATH = "/usr/local/groundwork/config/console.properties";

    /** The CONSOL e_ pro p_ fallbac k_ path. */
    String CONSOLE_PROP_FALLBACK_PATH = "WEB-INF/classes/console.properties";

    /** The CONSOL e_ admi n_ confi g_ path. */
    String CONSOLE_ADMIN_CONFIG_PATH = "/usr/local/groundwork/config/console-admin-config.xml";

    /** The CONSOL e_ admi n_ fallbac k_ confi g_ path. */
    String CONSOLE_ADMIN_FALLBACK_CONFIG_PATH = "WEB-INF/classes/console-admin-config.xml";

    /** The CONSOL e_ admi n_ confi g_ prop. */
    String CONSOLE_ADMIN_CONFIG_PROP = "console-admin-config";

    /** The FOUNDATIO n_ en d_ poin t_ por t_ name. */
    String FOUNDATION_END_POINT_PORT_NAME = "wscommon";

    /** The FOUNDATIO n_ en d_ poin t_ hos t_ group. */
    String FOUNDATION_END_POINT_HOST_GROUP = "wshostgroup";

    /** The FOUNDATIO n_ en d_ poin t_ event. */
    String FOUNDATION_END_POINT_EVENT = "wsevent";

    /** The FOUNDATIO n_ en d_ poin t_ category. */
    String FOUNDATION_END_POINT_CATEGORY = "wscategory";

    /** The FOUNDATIO n_ en d_ poin t_ host. */
    String FOUNDATION_END_POINT_HOST = "wshost";

    /** The FOUNDATIO n_ en d_ poin t_ service. */
    String FOUNDATION_END_POINT_SERVICE = "wsservice";

    /** The PRO p_ w s_ url. */
    String PROP_WS_URL = "foundation_webserviceURL";

    /** The PRO p_ pag e_ size. */
    String PROP_PAGE_SIZE = "page_size";

    /** The PRO p_ nagio s_ sen d_ notify. */
    String PROP_NAGIOS_SEND_NOTIFY = "nagios_send_notification";

    /** The PRO p_ nagio s_ persis t_ comment. */
    String PROP_NAGIOS_PERSIST_COMMENT = "nagios_persistent_comment";

    /** The PRO p_ builti n_ cols. */
    String PROP_BUILTIN_COLS = "built_in_columns";
    
    /** The PRO p_ builti n_ cols. */
    String PROP_EXCLUDED_DYNA_COLS = "invisible.dynamic.columns";

    /** The PRO p_ factor y_ init. */
    String PROP_FACTORY_INIT = "java.naming.factory.initial";

    /** The PRO p_ factor y_ host. */
    String PROP_FACTORY_HOST = "java.naming.factory.host";

    /** The PRO p_ factor y_ port. */
    String PROP_FACTORY_PORT = "java.naming.factory.port";

    /** The PRO p_ contex t_ factory. */
    String PROP_CONTEXT_FACTORY = "context.factory";

    /** The PRO p_ topi c_ name. */
    String PROP_TOPIC_NAME = "topic.name";

    /** The ACTIO n_ para m_ lo g_ mes s_ ids. */
    String ACTION_PARAM_LOG_MESS_IDS = "LogMessageIds";

    /** The ACTIO n_ para m_ use r_ name. */
    String ACTION_PARAM_USER_NAME = "UserName";

    /** The ACTIO n_ para m_ sen d_ notify. */
    String ACTION_PARAM_SEND_NOTIFY = "SendNotification";

    /** The ACTIO n_ para m_ persis t_ comment. */
    String ACTION_PARAM_PERSIST_COMMENT = "PersistentComment";

    /** The ACTIO n_ para m_ comment. */
    String ACTION_PARAM_COMMENT = "Comment";

    /** The ACTIO n_ para m_ valu e_ commen t_ prefix. */
    String ACTION_PARAM_VALUE_COMMENT_PREFIX = "Acknowledged from console at ";

    // For SNMPTRAP, SYSLOG params
    /** The ACTIO n_ para m_ nsc a_ host. */
    String ACTION_PARAM_NSCA_HOST = "nsca_host";

    /** The ACTIO n_ para m_ user. */
    String ACTION_PARAM_USER = "user";
    
    /** The ACTIO n_ para m_ user. */
    String ACTION_PARAM_USER_COMMENT = "user_comment";

    /** The ACTIO n_ para m_ nsc a_ comment. */
    String ACTION_PARAM_NSCA_COMMENT = "comment";

    /** The ACTIO n_ para m_ host. */
    String ACTION_PARAM_HOST = "host";

    /** The ACTIO n_ para m_ service. */
    String ACTION_PARAM_SERVICE = "service";

    /** The ACTIO n_ para m_ state. */
    String ACTION_PARAM_STATE = "state";

    /** The E l_ bin d_ even t_ sel. */
    String EL_BIND_EVENT_SEL = "#{event.selected}";

    /** The E l_ bin d_ tabl e_ multipl e_ sel. */
    String EL_BIND_TABLE_MULTIPLE_SEL = "#{tabset.tabs[tabset.tabIndex].dataTableBean.multipleSelection}";

    /** The E l_ bin d_ ro w_ sel. */
    String EL_BIND_ROW_SEL = "#{tabset.tabs[tabset.tabIndex].dataTableBean.rowSelection}";

    /** The E l_ bin d_ tabl e_ sort. */
    String EL_BIND_TABLE_SORT = "#{tabset.tabs[tabset.tabIndex].dataTableBean.sort}";

    /** The E l_ bin d_ co l_ mo n_ status. */
    String EL_BIND_COL_MON_STATUS = "monitorStatus";
    
    /** The E l_ bin d_severity. */
    String EL_BIND_COL_SEVERITY = "severity";

    /** The E l_ bin d_ htm l_ at t_ styleclass. */
    String EL_BIND_HTML_ATT_STYLECLASS = "styleClass";

    /** The E l_ bin d_ at t_ value. */
    String EL_BIND_ATT_VALUE = "value";
    
    /** The E l_ bin d_ at t_ value. */
    String EL_BIND_ATT_RENDERED= "rendered";
    
    /** The E l_ bin d_ at t_ title. */
    String EL_BIND_ATT_TITLE = "title";

    /** The E l_ bin d_ at t_ multiple. */
    String EL_BIND_ATT_MULTIPLE = "multiple";

    /** The STYL e_ tabl e_ col. */
    String STYLE_TABLE_COL = "tableColumn";

    /** The E l_ bin d_ even t_ left. */
    String EL_BIND_EVENT_LEFT = "#{event.";

    /** The E l_ bin d_ valu e_ right. */
    String EL_BIND_VALUE_RIGHT = ".value}";

    /** The E l_ bin d_ styl e_ right. */
    String EL_BIND_STYLE_RIGHT = ".styleClass}";

    /** The MO n_ statu s_ warn. */
    String MON_STATUS_WARN = "warning";

    /** The MO n_ statu s_ critical. */
    String MON_STATUS_CRITICAL = "critical";

    /** The MO n_ statu s_ unknown. */
    String MON_STATUS_UNKNOWN = "unknown";

    /** The MO n_ statu s_ ok. */
    String MON_STATUS_OK = "ok";

    /** The MO n_ statu s_ up. */
    String MON_STATUS_UP = "up";

    /** The MO n_ statu s_ unreachable. */
    String MON_STATUS_UNREACHABLE = "unreachable";

    /** The MO n_ statu s_ down. */
    String MON_STATUS_DOWN = "down";

    /** The MO n_ statu s_ pending. */
    String MON_STATUS_PENDING = "pending";

    /** The OPERATIO n_ critical. */
    String OPERATION_CRITICAL = "CRITICAL";

    /** The OPERATIO n_ statu s_ open. */
    String OPERATION_STATUS_OPEN = "OPEN";

    /** The RENDERER. */
    String RENDERER = "renderer";

    /** The DEFAUL t_ sor t_ column. */
    String DEFAULT_SORT_COLUMN = "reportDate";

    /** The ENTIT y_ nam e_ servicegroup. */
    String ENTITY_NAME_SERVICEGROUP = "SERVICE_GROUP";

    /** The STYL e_ highligh t_ navigator. */
    String STYLE_HIGHLIGHT_NAVIGATOR = "highlightNavigator";

    /** The IM g_ searchba r_ up. */
    String IMG_SEARCHBAR_UP = "/assets/icons/collapseArrowUp.gif";

    /** The IM g_ searchba r_ down. */
    String IMG_SEARCHBAR_DOWN = "/assets/icons/collapseArrowDown.gif";

    /** The STYL e_ sho w_ searchpanel. */
    String STYLE_SHOW_SEARCHPANEL = "overflow: visible;display: block";

    /** The STYL e_ hid e_ searchpanel. */
    String STYLE_HIDE_SEARCHPANEL = "overflow: visible;display: none";
    // String IMG_PAUSE_EVENTS = "/assets/buttons/btn_pause_on.gif";
    /** The IM g_ paus e_ events. */
    String IMG_PAUSE_EVENTS = "/assets/icons/media_pause.png";

    /** The IM g_ pla y_ events. */
    String IMG_PLAY_EVENTS = "/images/play_messages.gif";
    // String IMG_PLAY_EVENTS = "/assets/buttons/btn_play_on.gif";

    /** The AP p_ typ e_ snmptrap. */
    String APP_TYPE_SNMPTRAP = "SNMPTRAP";

    /** The AP p_ typ e_ syslog. */
    String APP_TYPE_SYSLOG = "SYSLOG";

    /** The AP p_ typ e_ nagios. */
    String APP_TYPE_NAGIOS = "NAGIOS";

    /** The SERVIC e_ snmptra p_ last. */
    String SERVICE_SNMPTRAP_LAST = "snmptraps_last";

    /** The SERVIC e_ syslo g_ last. */
    String SERVICE_SYSLOG_LAST = "syslog_last";

    /** The DEFAUL t_ nsc a_ host. */
    String DEFAULT_NSCA_HOST = "localhost";

    /** The DEFAUL t_ nsc a_ state. */
    String DEFAULT_NSCA_STATE = "0";

    /** The SUBMI t_ passiv e_ rese t_ comment. */
    String SUBMIT_PASSIVE_RESET_COMMENT = "Manual_reset_by_";

    /** The NAGIO s_ servic e_ column. */
    String NAGIOS_SERVICE_COLUMN = "service";

    /** The NAGIO s_ subcomponen t_ column. */
    String NAGIOS_SUBCOMPONENT_COLUMN = "SubComponent";

    /** The DELI m_ colon. */
    String DELIM_COLON = ":";

    /** The ENTIT y_ typ e_ logmessage. */
    String ENTITY_TYPE_LOGMESSAGE = "LOG_MESSAGE";

    /** The NO t_ available. */
    String NOT_AVAILABLE = "NOT AVAILABLE";

    // Filter constants
    /** The FILTE r_ dat a_ typ e_ string. */
    String FILTER_DATA_TYPE_STRING = "STRING";

    /** The FILTE r_ dat a_ typ e_ boolean. */
    String FILTER_DATA_TYPE_BOOLEAN = "BOOLEAN";

    /** The FILTE r_ dat a_ typ e_ int. */
    String FILTER_DATA_TYPE_INT = "INT";

    /** The FILTE r_ dat a_ typ e_ date. */
    String FILTER_DATA_TYPE_DATE = "DATE";

    /** The FILTE r_ dat a_ typ e_ double. */
    String FILTER_DATA_TYPE_DOUBLE = "DOUBLE";

    /** The FILTE r_ dat a_ typ e_ long. */
    String FILTER_DATA_TYPE_LONG = "LONG";

    /** The FILTE r_ pro p_ name. */
    String FILTER_PROP_NAME = "propertyValues.name";

    /** The FILTE r_ pro p_ valu e_ string. */
    String FILTER_PROP_VALUE_STRING = "propertyValues.valueString";

    /** The FILTE r_ pro p_ valu e_ boolean. */
    String FILTER_PROP_VALUE_BOOLEAN = "propertyValues.valueBoolean";

    /** The FILTE r_ pro p_ valu e_ int. */
    String FILTER_PROP_VALUE_INT = "propertyValues.valueInteger";

    /** The FILTE r_ pro p_ valu e_ date. */
    String FILTER_PROP_VALUE_DATE = "propertyValues.valueDate";

    /** The FILTE r_ pro p_ valu e_ double. */
    String FILTER_PROP_VALUE_DOUBLE = "propertyValues.valueDouble";

    /** The FILTE r_ pro p_ valu e_ long. */
    String FILTER_PROP_VALUE_LONG = "propertyValues.valueLong";

    // I18N properties
    /** The I18 n_ consol e_ nav i_ syste m_ filter. */
    String I18N_CONSOLE_NAVI_SYSTEM_FILTER = "com_groundwork_console_navigation_system_filters";

    /** The I18 n_ consol e_ nav i_ publi c_ filter. */
    String I18N_CONSOLE_NAVI_PUBLIC_FILTER = "com_groundwork_console_navigation_public_filters";

    /** The I18 n_ consol e_ conten t_ title. */
    String I18N_CONSOLE_CONTENT_TITLE = " com_groundwork_console_content_title";

    /** The I18 n_ consol e_ conten t_ ta b_ default. */
    String I18N_CONSOLE_CONTENT_TAB_DEFAULT = "com_groundwork_console_content_tab_default";

    /** The I18 n_ consol e_ conten t_ ta b_ new. */
    String I18N_CONSOLE_CONTENT_TAB_NEW = "com_groundwork_console_content_tab_new";

    /** The I18 n_ consol e_ conten t_ searc h_ title. */
    String I18N_CONSOLE_CONTENT_SEARCH_TITLE = "com_groundwork_console_content_search_title";

    /** The I18 n_ consol e_ conten t_ searc h_ device. */
    String I18N_CONSOLE_CONTENT_SEARCH_DEVICE = "com_groundwork_console_content_search_device";

    /** The I18 n_ consol e_ conten t_ searc h_ messages. */
    String I18N_CONSOLE_CONTENT_SEARCH_MESSAGES = "com_groundwork_console_content_search_messages";

    /** The I18 n_ consol e_ conten t_ searc h_ datetime. */
    String I18N_CONSOLE_CONTENT_SEARCH_DATETIME = "com_groundwork_console_content_search_datetime";

    /** The I18 n_ consol e_ conten t_ searc h_ preset. */
    String I18N_CONSOLE_CONTENT_SEARCH_PRESET = "com_groundwork_console_content_search_preset";

    /** The I18 n_ consol e_ conten t_ searc h_ reset. */
    String I18N_CONSOLE_CONTENT_SEARCH_RESET = "com_groundwork_console_content_search_custom";

    /** The I18 n_ consol e_ conten t_ searc h_ heade r_ updat e_ label. */
    String I18N_CONSOLE_CONTENT_SEARCH_HEADER_UPDATE_LABEL = "com_groundwork_console_content_search_header_updatelabel";

    /** The I18 n_ consol e_ conten t_ searc h_ butto n_ updat e_ label. */
    String I18N_CONSOLE_CONTENT_SEARCH_BUTTON_UPDATE_LABEL = "com_groundwork_console_content_search_button_updatelabel";

    /** The I18 n_ consol e_ conten t_ searc h_ button. */
    String I18N_CONSOLE_CONTENT_SEARCH_BUTTON = "com_groundwork_console_content_search_button";

    /** The I18 n_ consol e_ conten t_ rese t_ button. */
    String I18N_CONSOLE_CONTENT_RESET_BUTTON = "com_groundwork_console_content_search_button_reset";

    /** The I18 n_ consol e_ conten t_ selec t_ all. */
    String I18N_CONSOLE_CONTENT_SELECT_ALL = "com_groundwork_console_content_actionbar_button_selectall";

    /** The I18 n_ consol e_ conten t_ deselec t_ all. */
    String I18N_CONSOLE_CONTENT_DESELECT_ALL = "com_groundwork_console_content_actionbar_button_deselectall";

    /** The I18 n_ consol e_ conten t_ actions. */
    String I18N_CONSOLE_CONTENT_ACTIONS = "com_groundwork_console_content_actionbar_button_actions";

    /** The I18 n_ consol e_ conten t_ paus e_ events. */
    String I18N_CONSOLE_CONTENT_PAUSE_EVENTS = "com_groundwork_console_content_actionbar_button_pause";

    /** The I18 n_ consol e_ conten t_ resum e_ events. */
    String I18N_CONSOLE_CONTENT_RESUME_EVENTS = "com_groundwork_console_content_actionbar_button_resume";

    /** The I18 n_ consol e_ conten t_ ta b_ labe l_ apptype. */
    String I18N_CONSOLE_CONTENT_TAB_LABEL_APPTYPE = "com_groundwork_console_content_tab_header_apptypes";

    /** The I18 n_ consol e_ conten t_ ta b_ labe l_ opstatus. */
    String I18N_CONSOLE_CONTENT_TAB_LABEL_OPSTATUS = "com_groundwork_console_content_tab_header_opstatus";

    /** The I18 n_ consol e_ conten t_ ta b_ labe l_ even t_ by. */
    String I18N_CONSOLE_CONTENT_TAB_LABEL_EVENT_BY = "com_groundwork_console_content_tab_header_eventsby";

    /** The I18 n_ consol e_ conten t_ ta b_ labe l_ hos t_ grps. */
    String I18N_CONSOLE_CONTENT_TAB_LABEL_HOST_GRPS = "com_groundwork_console_content_tab_header_hostgroups";

    /** The I18 n_ consol e_ conten t_ ta b_ labe l_ servic e_ grps. */
    String I18N_CONSOLE_CONTENT_TAB_LABEL_SERVICE_GRPS = "com_groundwork_console_content_tab_header_servicegroups";

    /** The I18 n_ consol e_ erro r_ pagination. */
    String I18N_CONSOLE_ERROR_PAGINATION = "com_groundwork_console_error_message_select_and_pagination";

    /** The I18 n_ consol e_ erro r_ invali d_ search. */
    String I18N_CONSOLE_ERROR_INVALID_SEARCH = "com_groundwork_console_error_invalid_search";

    /** The I18 n_ consol e_ erro r_ invali d_ daterange. */
    String I18N_CONSOLE_ERROR_INVALID_DATERANGE = "com_groundwork_console_error_invalid_daterange";

    /** The I18 n_ globa l_ erro r_ messag e1. */
    String I18N_GLOBAL_ERROR_MESSAGE1 = "com_groundwork_global_error_message1";

    /** The I18 n_ globa l_ erro r_ messag e2. */
    String I18N_GLOBAL_ERROR_MESSAGE2 = "com_groundwork_global_error_message2";

    /** The I18 n_ globa l_ erro r_ messag e3. */
    String I18N_GLOBAL_ERROR_MESSAGE3 = "com_groundwork_global_error_message3";
    
    /** The I18 n_ consol e_ nav i_ publi c_ filter. */
    String I18N_CONSOLE_USER_COMMENT = "com_groundwork_console_user_comment";

    // for URL mapping
    /** SERVICEGROUP. */
    String SERVICEGROUP = "servicegroup";

    /** SERVICE. */
    String SERVICE = "service";

    /** HOSTGROUP. */
    String HOSTGROUP = "hostgroup";

    /** HOST. */
    String HOST = "host";

    /** & sign. */
    String AMP = "&";

    /** = sign. */
    String EQ = "=";

    /** SV_LINKS_ENABLED. */
    String SV_LINKS_ENABLED = "sv.links.enabled";

    /** css STYLE. */
    String STYLE = "style";

    /** EMPTY_STRING. */
    public static final String EMPTY_STRING = "";

    /** APP_TYPE_CONSOLE. */
    public static final String APP_TYPE_CONSOLE = "console";
    /**
     * COMMA
     */
    public static final String COMMA = ",";

    /**
     * String property for get service under service group and service
     */
    public static final String SERVICE_STATUS_SERVICE_STATUS_ID = "serviceStatus.serviceStatusId";
    /**
     * 
     */
    public static final String DEFAULT_DATE_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

    /**
     * CUSTOM
     */
    public static final String CUSTOM = "custom";

    /**
     * PRESET
     */
    public static final String PRESET = "preset";

    /**
     * TYPE
     */
    public static final String TYPE = "type";

    /**
     * DATETIME
     */
    public static final String DATETIME = "datetime";

    /**
     * MESSAGE
     */
    public static final String MESSAGE = "message";

    /**
     * DEVICE
     */
    public static final String DEVICE = "device";

    /**
     * SEARCHEVENTS
     */
    public static final String SEARCHEVENTS = "searchevents";

    /**
     * EVENTS
     */
    public static final String EVENTS = "events";

    /** The I18 n_ consol e_ SILENCE_ALARM e_ events. */
    public String I18N_CONSOLE_SILENCE_ALARM = "com_groundwork_console_silence_alarm";
    /** The I18 n_ consol e_ alarm_ events. */
    public String I18N_CONSOLE_ALARM = "com_groundwork_console_alarm";
    /**
     * Turn on alarm image path
     */
    public static final String IMAGES_SPEAKER_JPEG = "/images/speaker.jpeg";

    /**
     * Turn off alarm image path
     */
    public static final String IMAGES_MUTE_JPEG = "/images/mute.jpeg";
    
    /** filter Refresh Image */
    public static final String BTN_FILTER_REFRESH = "/assets/buttons/btn-refresh-icon.png";
    
    /** filter Refresh Image */
    public static final String PROP_TEXT_MESSAGE_SIZE = "text_message_size";
    
    /**
     * HostGroup
     */
    public static final String DB_HOSTGROUP = "HostGroup";
    /**
     * ServiceGroup
     */
    public static final String DB_SERVICEGROUP = "ServiceGroup";
    /**
     * CustomGroup
     */
    public static final String DB_CUSTOMGROUP = "CustomGroup";
    
    
    /**
     * Host group bubble up path.
     */
    public static final String HOST_GROUP_PENDING = "/images/host-group-blue.gif";
    
    /**
     * Host group bubble up path.
     */
    public static final String HOST_GROUP_DOWN_SCHEDULED = "/images/host-group-orange.gif";
    
    /**
     * Host group bubble up path.
     */
    public static final String HOST_GROUP_DOWN_UNSCHEDULED = "/images/host-group-red.gif";
    
    /**
     * Host group bubble up path.
     */
    public static final String HOST_GROUP_UP = "/images/host-group-green.gif";
    
    /**
     * Host group bubble up path.
     */
    public static final String HOST_GROUP_WARNING = "/images/host-group-yellow.gif";
    
    /**
     * Host group bubble up path.
     */
    public static final String HOST_GROUP_UNREACHABLE= "/images/host-group-gray.gif";
    
    /**
     * Service Group bubble up path.
     */
    public static final String SERVICE_GROUP_PENDING = "/images/service-group-blue.gif";
    
    /**
     * Service Group bubble up path.
     */
    public static final String SERVICE_GROUP_CRITICAL_SCHEDULED = "/images/service-group-orange.gif";
    
    /**
     * Service Group bubble up path.
     */
    public static final String SERVICE_GROUP_CRITICAL_UNSCHEDULED = "/images/service-group-red.gif";
    
    /**
     * Service Group bubble up path.
     */
    public static final String SERVICE_GROUP_OK = "/images/service-group-green.gif";
    
    /**
     * Service Group bubble up path.
     */
    public static final String SERVICE_GROUP_WARNING = "/images/service-group-yellow.gif";
    
    /**
     * Service Group bubble up path.
     */
    public static final String SERVICE_GROUP_UNKNOWN= "/images/service-group-gray.gif";
    
    
    /**
     * Custom Group bubble up path.
     */
    public static final String CUSTOM_GROUP_PENDING = "/images/customgroup-blue.gif";
    
    /**
     * Custom Group bubble up path.
     */
    public static final String CUSTOM_GROUP_CRITICAL_SCHEDULED = "/images/customgroup-orange.gif";
    
    /**
     * Custom Group bubble up path.
     */
    public static final String CUSTOM_GROUP_CRITICAL_UNSCHEDULED = "/images/customgroup-red.gif";
    
    /**
     * Custom Group bubble up path.
     */
    public static final String CUSTOM_GROUP_OK = "/images/customgroup-green.gif";
    
    /**
     * Custom Group bubble up path.
     */
    public static final String CUSTOM_GROUP_WARNING = "/images/customgroup-yellow.gif";
    
    /**
     * Custom Group bubble up path.
     */
    public static final String CUSTOM_GROUP_UNREACHABLE= "/images/customgroup-gray.gif";
    
    /**
     * Query params for the cross links to the console
     */
    public static final String GWOS_CONSOLE_VIEWPARAM= "gwos_console_viewparam";
    
    /**
     * Query params for the cross links to the console
     */
    public static final String GWOS_CONSOLE_SESSION_PARAM_DELIM= "^^^^";
    
    /**
     * Managed bean for event pie handler
     */
    //public static final String MANAGED_BEAN_EVENTPIE_HANDLER="eventsPieHandler";

}
