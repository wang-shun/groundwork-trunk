package com.groundworkopensource.portal.statusviewer.common;

/**
 * This enumerator represents all possible network node types in the system
 * (Network). It has type name and corresponding page name in multi page view.
 * 
 * Parameters: Node Name, Sub page name as it appears in URL
 * 
 * @author nitin_jadhav
 * 
 */
public enum NodeType {

    /**
     * NETWORK NodeType
     */
    NETWORK("Network", IPCHandlerConstants.NETWORK_SUBPAGE_NAME, null,
            Constant.EMPTY_STRING),
    /**
     * HOST NodeType
     */
    HOST("Host", IPCHandlerConstants.HOST_SUBPAGE_NAME,
            IPCHandlerConstants.HOST_ID_PARAM, Constant.JMS_HOST),
    /**
     * HOST_GROUP NodeType
     */
    HOST_GROUP("Host Group", IPCHandlerConstants.HOST_GROUP_SUBPAGE_NAME,
            IPCHandlerConstants.HOSTGROUP_ID_PARAM, Constant.JMS_HOSTGROUP),
    /**
     * SERVICE NodeType
     */
    SERVICE("Service", IPCHandlerConstants.SERVICE_SUBPAGE_NAME,
            IPCHandlerConstants.SERVICE_ID_PARAM, Constant.JMS_SERVICESTATUS),
    /**
     * SERVICE_GROUP NodeType
     */
    SERVICE_GROUP("Service Group",
            IPCHandlerConstants.SERVICE_GROUP_SUBPAGE_NAME,
            IPCHandlerConstants.SERVICEGROUP_ID_PARAM,
            Constant.JMS_SERVICEGROUP),
    
    /**
     * CUSTOM_GROUP NodeType
     */
    CUSTOM_GROUP("CustomGroup",
            IPCHandlerConstants.CUSTOM_GROUP_SUBPAGE_NAME,
            IPCHandlerConstants.CUSTOMGROUP_ID_PARAM,
            Constant.JMS_CUSTOMGROUP);

    /**
     * @param subPageName
     */
    private NodeType(String typeName, String subPageName, String nodeIdParam,
            String jmsEntityType) {
        this.typeName = typeName;
        this.subPageName = subPageName;
        this.nodeIdParameter = nodeIdParam;
        this.jmsEntityType = jmsEntityType;

    }

    /**
     * Type Name
     */
    private String typeName;

    /**
     * subPageName
     */
    private String subPageName;

    /**
     * Actual Node Id parameter.
     */
    private String nodeIdParameter;

    /**
     * Entity type used for JMS update objects.
     */
    private String jmsEntityType;

    /**
     * @return the subPageName
     */
    public String getSubPageName() {
        return subPageName;
    }

    /**
     * Retrieves NodeType from NodeId parameter.
     * 
     * @param nodeIdParameter
     * @return NodeType
     */
    public static NodeType getNodeType(String nodeIdParameter) {
        if (IPCHandlerConstants.HOST_ID_PARAM.equalsIgnoreCase(nodeIdParameter)) {
            return HOST;
        }
        if (IPCHandlerConstants.HOSTGROUP_ID_PARAM
                .equalsIgnoreCase(nodeIdParameter)) {
            return HOST_GROUP;
        }
        if (IPCHandlerConstants.SERVICE_ID_PARAM
                .equalsIgnoreCase(nodeIdParameter)) {
            return SERVICE;
        }
        if (IPCHandlerConstants.SERVICEGROUP_ID_PARAM
                .equalsIgnoreCase(nodeIdParameter)) {
            return SERVICE_GROUP;
        }
        
        if (IPCHandlerConstants.CUSTOMGROUP_ID_PARAM
                .equalsIgnoreCase(nodeIdParameter)) {
            return CUSTOM_GROUP;
        }
        // by default return Network
        return NETWORK;
    }

    /**
     * Retrieves NodeType from Node Type Name parameter.
     * 
     * @param nodeTypeName
     * @return NodeType
     */
    public static NodeType getNodeTypeByTypeName(String nodeTypeName) {
        if (HOST.typeName.equalsIgnoreCase(nodeTypeName)) {
            return HOST;
        }
        if (HOST_GROUP.typeName.equalsIgnoreCase(nodeTypeName)) {
            return HOST_GROUP;
        }
        if (SERVICE.typeName.equalsIgnoreCase(nodeTypeName)) {
            return SERVICE;
        }
        if (SERVICE_GROUP.typeName.equalsIgnoreCase(nodeTypeName)) {
            return SERVICE_GROUP;
        }
        if (CUSTOM_GROUP.typeName.equalsIgnoreCase(nodeTypeName)) {
            return CUSTOM_GROUP;
        }
        // by default return Network
        return NETWORK;
    }

    /**
     * Retrieves NodeType from Node view parameter.
     * 
     * @param nodeView
     * 
     * @return NodeType
     */
    public static NodeType getNodeTypeByView(String nodeView) {
        if (IPCHandlerConstants.HOST_SUBPAGE_NAME.equalsIgnoreCase(nodeView)) {
            return HOST;
        }
        if (IPCHandlerConstants.HOST_GROUP_SUBPAGE_NAME
                .equalsIgnoreCase(nodeView)) {
            return HOST_GROUP;
        }
        if (IPCHandlerConstants.SERVICE_SUBPAGE_NAME.equalsIgnoreCase(nodeView)) {
            return SERVICE;
        }
        if (IPCHandlerConstants.SERVICE_GROUP_SUBPAGE_NAME
                .equalsIgnoreCase(nodeView)) {
            return SERVICE_GROUP;
        }
        if (IPCHandlerConstants.CUSTOM_GROUP_SUBPAGE_NAME
                .equalsIgnoreCase(nodeView)) {
            return CUSTOM_GROUP;
        }
        // by default return Network
        return NETWORK;
    }

    /**
     * Retrieves Node view from Node Type parameter.
     * 
     * @param nodeType
     * @return Node view
     */
    public static String getNodeViewByNodeType(NodeType nodeType) {
        switch (nodeType) {
            case HOST:
                return IPCHandlerConstants.HOST_SUBPAGE_NAME;
            case HOST_GROUP:
                return IPCHandlerConstants.HOST_GROUP_SUBPAGE_NAME;
            case SERVICE:
                return IPCHandlerConstants.SERVICE_SUBPAGE_NAME;
            case SERVICE_GROUP:
                return IPCHandlerConstants.SERVICE_GROUP_SUBPAGE_NAME;
            case CUSTOM_GROUP:
                return IPCHandlerConstants.CUSTOM_GROUP_SUBPAGE_NAME;
            default:
                return IPCHandlerConstants.NETWORK_SUBPAGE_NAME;
        }
    }

    /**
     * Returns the typeName.
     * 
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns the nodeIdParameter.
     * 
     * @return the nodeIdParameter
     */
    public String getNodeIdParameter() {
        return nodeIdParameter;
    }

    /**
     * @param jmsEntityType
     */
    public void setJmsEntityType(String jmsEntityType) {
        this.jmsEntityType = jmsEntityType;
    }

    /**
     * @return jmsEntityType
     */
    public String getJmsEntityType() {
        return jmsEntityType;
    }
}
