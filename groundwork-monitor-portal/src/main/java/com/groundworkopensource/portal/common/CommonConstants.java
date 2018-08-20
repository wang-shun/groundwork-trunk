
package com.groundworkopensource.portal.common;

/**
 * Constant files for common package.
 * 
 * @author swapnil_gujrathi
 */
public class CommonConstants {

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected CommonConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * GROUNDWORK_DEFAULT_CONFIG_PATH - "/usr/local/groundwork/config/".
     */
    public static final String GROUNDWORK_DEFAULT_CONFIG_PATH = "/usr/local/groundwork/config/";

    /**
     * Resource bundle path. This path also works for Windows. - you need to
     * have "/usr/local/groundwork/config/resources/" directory structure
     * created on drive on which you have Jboss server running.
     */
    public static final String RESOURCE_BINDLE_PATH = GROUNDWORK_DEFAULT_CONFIG_PATH
            + "resources/";
    /**
     * Resource bundle path URL - required for putting this into class-path via
     * class loader.
     */
    public static final String RESOURCE_BINDLE_PATH_URL = "file:"
            + RESOURCE_BINDLE_PATH;

    /**
     * status.properties with default path -
     * "/usr/local/groundwork/config/status-viewer.properties".
     */
    public static final String STATUS_VIEWER_PROP_PATH = GROUNDWORK_DEFAULT_CONFIG_PATH
            + "status-viewer.properties";
    /**
     * status-viewer.properties fallback path.
     */
    public static final String STATUS_VIEWER_PROP_FALLBACK_PATH = "WEB-INF/classes/status-viewer.properties";
    /**
     * Constant name for setting attribute into Context with value as
     * "properties" read from status-viewer.properties.
     */
    public static final String STATUS_VIEWER_PROPS = "statusViewerProps";
    /**
     * STATUS VIEWER RESOURCE BUNDLE NAME
     */
    public static final String STATUS_VIEWER_RESOURCE_BUNDLE_NAME = "StatusViewerResources";

    /**
     * console.properties with default path -
     * "/usr/local/groundwork/config/console.properties".
     */
    public static final String CONSOLE_PROP_PATH = GROUNDWORK_DEFAULT_CONFIG_PATH
            + "console.properties";
    /**
     * console.properties fallback path.
     */
    public static final String CONSOLE_PROP_FALLBACK_PATH = "WEB-INF/classes/console.properties";
    /**
     * Constant name for setting attribute into Context with value as
     * "properties" read from console.properties.
     */
    public static final String CONSOLE_PROPS = "consoleProps";
    /**
     * report-viewer.properties with default path -
     * "/usr/local/groundwork/config/report-viewer.properties".
     */
    public static final String REPORT_VIEWER_PROP_PATH = GROUNDWORK_DEFAULT_CONFIG_PATH
            + "report-viewer.properties";
    /**
     * report-viewer.properties fallback path.
     */
    public static final String REPORT_VIEWER_PROP_FALLBACK_PATH = "WEB-INF/classes/report-viewer.properties";
    /**
     * Constant name for setting attribute into Context with value as
     * "properties" read from report-viewer.properties.
     */
    public static final String REPORT_VIEWER_PROPS = "reportViewerProps";

    /**
     * dashboard.properties with default path -
     * "/usr/local/groundwork/config/dashboard.properties".
     */
    public static final String DASHBOARD_PROP_PATH = GROUNDWORK_DEFAULT_CONFIG_PATH
            + "dashboard.properties";
    /**
     * dashboard.properties fallback path.
     */
    public static final String DASHBOARD_PROP_FALLBACK_PATH = "WEB-INF/classes/dashboard.properties";

    /**
     * DASHBOARD RESOURCE BUNDLE NAME
     */
    public static final String DASHBOARD_RESOURCE_BUNDLE_NAME = "DashboardResources";

    /**
     * Constant name for setting attribute into Context with value as
     * "properties" read from dashboard.properties.
     */
    public static final String DASHBOARD_PROPS = "dashboard";
    /**
     * REPORT VIEWER RESOURCE BUNDLE NAME
     */
    public static final String REPORT_VIEWER_RESOURCE_BUNDLE_NAME = "ReportViewerResources";

    /**
     * "Application Type" context parameter name - value of which should come
     * from the web.xml.
     */
    public static final String APPLICATION_TYPE_CONTEXT_PARAM_NAME = "foundation.application.type";

    /**
     * This parameter holds the URL to look for web service end point.
     * Application should specify Web Service end point either in application
     * specific properties file or in web.xml file. Key for Web Service URL end
     * point parameter must be - "foundation.webservice.url".
     */
    public static final String FOUNDATION_WS_URL_KEY = "foundation.webservice.url";
    
    /**
     * This parameter holds the URL to look for resteasy service end point.
     * Application should specify Web Service end point either in application
     * specific properties file or in web.xml file. Key for Web Service URL end
     * point parameter must be - "portal.extension.resteasy.webservice.url".
     */
    public static final String PORTAL_EXTN_RESTEASY_URL_KEY = "portal.extension.resteasy.service.url";

    /**
     * STATUS_VIEWER_APPLICATION.
     */
    public static final String STATUS_VIEWER_APPLICATION = "statusViewer";
    /**
     * REPORT_VIEWER_APPLICATION.
     */
    public static final String REPORT_VIEWER_APPLICATION = "reportViewer";
    /**
     * EVENT_CONSOLE_APPLICATION.
     */
    public static final String EVENT_CONSOLE_APPLICATION = "eventConsole";
    /**
     * DASHBOARD_APPLICATION.
     */
    public static final String DASHBOARD_APPLICATION = "dashboard";
    /**
     * EMPTY_STRING - ""
     */
    public static final String EMPTY_STRING = "";

    /**
     * Value of Default Filter i.e. when none is applied
     */
    public static final String DEFAULT_FILTER = "";
    /**
     * DEFAULT_ENGLISH_LOCALE - "en".
     */
    public static final String DEFAULT_ENGLISH_LOCALE = "en";

    /**
     * Namespace used by ICEFaces portlet bridge.
     */
    public static final String ICEFACES_NAMESPACE = "com.icesoft.faces.NAMESPACE";

    /**
     * One of the request attribute used by ICEFaces portlet bridge.
     */
    public static final String ICEFACES_PORTLET = "com.icesoft.faces.PORTLET";

    /**
     * One of the request attribute used by ICEFaces portlet bridge.
     */
    public static final String ICEFACES_PORTLET_ARTIFACT = "com.icesoft.faces.portlet.artifact";

    /**
     * Content type to be set in response.
     */
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    /**
     * One of the request attribute value used by ICEFaces portlet bridge.
     */
    public static final String PORTLET = "portlet";

    /**
     * Constant defining filter XML file name
     */
    public static final String FILTER_XML_NAME = "portal-filter.xml";

    /**
     * Constant defining XML schema file name
     */
    public static final String SCHEMA_NAME = "FilterSchema.xsd";

    // JMS Constants
    /**
     * Initial Naming Factory
     */
    public static final String PROP_FACTORY_INIT = "java.naming.factory.initial";
    /**
     * Host
     */
    public static final String PROP_FACTORY_HOST = "java.naming.factory.host";
    /**
     * Port
     */
    public static final String PROP_FACTORY_PORT = "java.naming.factory.port";
    /**
     * Context Factory
     */
    public static final String PROP_CONTEXT_FACTORY = "topic.connection.factory";
    /**
     * Topic Name
     */
    public static final String PROP_TOPIC_NAME = "performance.topic.name";

    /**
     * Service Monitor status Critical
     */
    public static final String CRITICAL = "Critical";

    /**
     * Host Monitor status Unreachable
     */
    public static final String UNREACHABLE = "Unreachable";

    /**
     * Host Monitor status Up
     */
    public static final String UP = "Up";

    /**
     * Host Monitor status Scheduled Down
     */
    public static final String SCHEDULED_DOWN = "Scheduled Down";

    /**
     * Host Monitor status UnScheduled Down
     */
    public static final String UN_SCHEDULED_DOWN = "UnScheduled Down";

    /**
     * Service Monitor status OK
     */
    public static final String OK = "OK";

    /**
     * Service or host Monitor status Pending
     */
    public static final String PENDING = "Pending";

    /**
     * Service Monitor status Unknown
     */
    public static final String UNKNOWN = "Unknown";

    /**
     * Service Monitor status Warning
     */
    public static final String WARNING = "Warning";

    /**
     * Service Monitor status Scheduled Critical
     */
    public static final String SCHEDULED_CRITICAL = "Scheduled Critical";

    /**
     * Service Monitor status Unscheduled Critical
     */
    public static final String UNSCHEDULED_CRITICAL = "Unscheduled Critical";

    /**
     * nagois constant string
     */
    public static final String NAGIOS = "NAGIOS";

    /**
     * TOTAL_COUNT_KEY to be used to put total into statistics HashMap.
     */
    public static final String TOTAL_COUNT_KEY = "total";

    /**
     * SERVICE EXCEPTION MESSAGE
     */
    public static final String SERVICE_EXCEPTION_MESSAGE = "ServiceException while getting binding object for \"statistics\" web service";

    /**
     * REMOTE EXCEPTION MESSAGE
     */
    public static final String REMOTE_EXCEPTION_MESSAGE = "RemoteException while contacting \"statistics\" foundation web service";

    /**
     * WSFOUNDATION EXCEPTION MESSAGE
     */
    public static final String WSFOUNDATION_EXCEPTION_MESSAGE = "WSFoundationException while getting getStatistics data";

    /**
     * Actual EXCEPTION MESSAGE
     */
    public static final String ACTUAL_EXCEPTION_MESSAGE = " Actual exception message : ";

    /**
     * Host Monitor status DOWN
     */
    public static final String DOWN = "DOWN";

    /**
     * This parameter holds the server name/ip address where event broker is
     * setup. Key for event broker server parameter must be -
     * "eventBroker.server".
     */
    public static final String EVENT_BROKER_SERVER = "eventBroker.server";

    /**
     * This parameter holds port number on which action portlet should connect
     * to for sending nagios commands. Key for event broker port parameter must
     * be - "eventBroker.port".
     */
    public static final String EVENT_BROKER_PORT = "eventBroker.port";

    /**
     * This parameter holds Encryption algorithm to be used when sending nagios
     * commands from actions portlet to the event broker. Key for encryprion
     * algortihm parameter must be - "eventBroker.encryptionAlgorithm".
     */
    public static final String EVENT_BROKER_ENCRYPTION_ALGORITHM = "eventBroker.encryptionAlgorithm";

    /**
     * This parameter holds Encryption key used for encrypting nagios commands
     * to be sent to the event broker from actions portlet. Key for encryption
     * key parameter must be - "eventBroker.encryptionKey".
     */
    public static final String EVENT_BROKER_ENCRYPTION_KEY = "eventBroker.encryptionKey";

    /**
     * LAST_STATE_CHANGE constant
     */
    public static final String LAST_STATE_CHANGE = "LastStateChange";

    /**
     * boolean isProblemAcknowledged property for host.
     */
    public static final String IS_ACKNOWLEDGED = "isAcknowledged";

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
     * Comma
     */
    public static final String COMMA = ",";

    /**
     * JAVAX_PORTLET_REQUEST constant
     */
    public static final String JAVAX_PORTLET_REQUEST = "javax.portlet.request";

}
