package com.groundworkopensource.portal.statusviewer.common;

/**
 * IPC Handler Constants
 * 
 * @author swapnil_gujrathi
 */
public final class IPCHandlerConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected IPCHandlerConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    // Sub page name constants
    /**
     * HOST_GROUP_SUBPAGE_NAME
     */
    public static final String HOST_GROUP_SUBPAGE_NAME = "HostGroupView";
    /**
     * HOST_SUBPAGE_NAME
     */
    public static final String HOST_SUBPAGE_NAME = "HostView";
    /**
     * NETWORK_SUBPAGE_NAME
     */
    public static final String NETWORK_SUBPAGE_NAME = "NetworkView";
    /**
     * SERVICE_GROUP_SUBPAGE_NAME
     */
    public static final String SERVICE_GROUP_SUBPAGE_NAME = "ServiceGroupView";
    /**
     * SERVICE_SUBPAGE_NAME
     */
    public static final String SERVICE_SUBPAGE_NAME = "ServiceView";
    
    /**
     * CUSTOM_GROUP_SUBPAGE_NAME
     */
    public static final String CUSTOM_GROUP_SUBPAGE_NAME = "CustomGroupView";

    /**
     * Unique identifier for the Host passed via render request
     */
    public static final String HOST_ID_PARAM = "hid";

    /**
     * Unique identifier for the HostGroup passed via render request
     */
    public static final String HOSTGROUP_ID_PARAM = "hgid";
    
    /**
     * Unique identifier for the CustomGroup passed via render request
     */
    public static final String CUSTOMGROUP_ID_PARAM = "custid";

    /**
     * Unique identifier for the Service passed via render request
     */
    public static final String SERVICE_ID_PARAM = "sid";

    /**
     * Unique identifier for the Service Group passed via render request
     */
    public static final String SERVICEGROUP_ID_PARAM = "sgid";

    /**
     * Unique identifier for the "Node Name" passed via render request
     */
    public static final String NODE_NAME_PARAM = "name";

    /**
     * UNDERSCORE
     */
    public static final String UNDERSCORE = "_";

    /**
     * COLON
     */
    public static final String COLAN = ":";

    /**
     * HTTP
     */
    public static final String HTTP = "http://";
    /**
     * FRONT_SLASH
     */
    public static final String FRONT_SLASH = "/";

    /**
     * FOUNDATION_APPLICATION_TYPE - StatusViewer / Dashboard
     */
    public static final String FOUNDATION_APPLICATION_TYPE = "foundation.application.type";

    /**
     * SERVICE_FILTER Session Attribute name.
     */
    public static final String SERVICE_FILTER = "SERVICE_FILTER";

    /**
     * HOST_FILTER Session Attribute name.
     */
    public static final String HOST_FILTER = "HOST_FILTER";
    /**
     * perf Measurement time filter session attribute name
     */
    public static final String PERF_TIME_FILTER = "PERF_TIME_FILTER";

    /**
     * FILTER_STATE_BEAN_NAME
     */
    public static final String FILTER_STATE_BEAN_NAME = "filterStateBean";

    /**
     * TREE_STATE_BEAN_NAME
     */
    public static final String TREE_STATE_BEAN_NAME = "treeStateBean";
    /**
     * "Status" which is part of request URL. <br>
     * IMPORTANT: DO NOT CHANGE THIS CONSTANT.
     */
    public static final String STATUS = "portal/classic/status";

    /**
     * ICESOFT NAMESPACE constant - icesoft attribute to determine if the
     * portlet is in dashboard or status viewer.
     */
    public static final String ICESOFT_NAMESPACE = "com.icesoft.faces.NAMESPACE";

    /**
     * Status viewer node name attribute
     */
    public static final String SV_NODE_NAME_ATTRIBUTE = "com.gwos.sv.nodeName";

    /**
     * Status viewer node id attribute
     */
    public static final String SV_NODE_ID_ATTRIBUTE = "com.gwos.sv.nodeID";

    /**
     * Status viewer node type attribute
     */
    public static final String SV_NODE_TYPE_ATTRIBUTE = "com.gwos.sv.nodeType";

    /**
     * Status viewer path attribute
     */
    public static final String SV_PATH_ATTRIBUTE = "com.gwos.sv.path";
    
    /**
     * Status viewer tab press attribute
     */
    public static final String SV_TAB_PRESSED_ATTRIBUTE = "com.gwos.sv.tab.pressed";
}
