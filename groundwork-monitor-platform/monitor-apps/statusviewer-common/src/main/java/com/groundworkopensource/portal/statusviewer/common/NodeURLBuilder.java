package com.groundworkopensource.portal.statusviewer.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;

/**
 * Used to build URLs for links (sub-pages) that refers to network objects
 * (Network Hosts, Groups etc).
 * 
 * @author nitin_jadhav
 * 
 */
public class NodeURLBuilder {

    /**
     * URL_PATH
     */
    private static final String URL_PATH = "&path=";

    /**
     * URL_CREATE
     */
    private static final String URL_CREATE = "&svcmd=create";

    /**
     * URL_UTF
     */
    private static final String URL_UTF = "UTF-8";

    /**
     * URL_NAME
     */
    private static final String URL_NAME = "&name=";

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(NodeURLBuilder.class);

    /**
     * Base URL for Status view main page.
     */
    private static final String BASE_URL = "/portal/classic/status";

    /**
     * Base URL for Status View pages.
     */
    // private static final String STATUS_VIEWER_BASE_URL =
    // "/portal/auth/status/";
    private static final String STATUS_VIEWER_BASE_URL = "/portal/classic/status";

    /**
     * Builds subpage URL and returns.
     * 
     * @param nodeType
     * @param nodeId
     * @param nodeName
     * @return subpage URL after building
     */
    public static String buildNodeURL(NodeType nodeType, Integer nodeId,
            String nodeName) {

        return buildNodeURL(nodeType, nodeId, nodeName, null);
    }

    /**
     * Builds subpage URL for the Treeview only and returns.
     * 
     * @param nodeType
     * @param nodeId
     * @param nodeName
     * @param parent
     *            pass this parameter as null, if node have no parents.
     *            otherwise pass comma separated list of node names.
     * @return subpage URL after building
     */
    public static String buildNodeURLForExternalRequest(NodeType nodeType,
            Integer nodeId, String nodeName, String parent) {
        return buildStatusViewerNodeURL(nodeType, nodeId, nodeName, parent);
    }

    /**
     * Builds subpage URL for the Treeview only and returns.
     * 
     * @param nodeType
     * @param nodeId
     * @param nodeName
     * @param parent
     *            pass this parameter as null, if node have no parents.
     *            otherwise pass comma seperated list of node names.
     * @return subpage URL after building
     */
    public static String buildNodeURL(NodeType nodeType, Integer nodeId,
            String nodeName, String parent) {

        // return empty string ("") if Dashboard Links are Disabled as per the
        // User Role.
        if (!PortletUtils.isInStatusViewer()) {
            // get the UserRoleBean managed instance
            UserExtendedRoleBean userExtendedRoleBean = PortletUtils
                    .getUserExtendedRoleBean();
            // (UserRoleBean) FacesUtils
            // .getManagedBean(Constant.USER_ROLE_BEAN);

            if (null != userExtendedRoleBean
                    && userExtendedRoleBean.isDashboardLinksDisabled()) {
                return Constant.EMPTY_STRING;
            }
        }

        return buildStatusViewerNodeURL(nodeType, nodeId, nodeName, parent);
    }

    /**
     * Builds Node URL for Status Viewer
     * 
     * @param nodeType
     * @param nodeId
     * @param nodeName
     * @param parent
     * @return subpage URL after building
     */
    private static String buildStatusViewerNodeURL(NodeType nodeType,
            Integer nodeId, String nodeName, String parent) {
        // build and set URL of node here, Tree and other portlets will call
        // this method
        // TODO: Change to get request path from StatusViewManager component
        StringBuilder nodeUrlBuffer = null;

        if (NodeType.NETWORK.equals(nodeType)) {
            nodeUrlBuffer = new StringBuilder(BASE_URL);
        } else {
            nodeUrlBuffer = new StringBuilder(STATUS_VIEWER_BASE_URL).append(
                    "?nodeType=").append(nodeType.getSubPageName()).append(
                    "&nodeID=").append(nodeId);

            if (nodeName != null && !nodeName.equals(Constant.EMPTY_STRING)) {
                try {
                    nodeUrlBuffer.append(URL_NAME).append(
                            URLEncoder.encode(nodeName, URL_UTF));
                    nodeUrlBuffer.append(URL_CREATE);
                    String path = null;
                    if (parent != null) {
                        path = new StringBuilder().append(parent).append(
                                Constant.COMMA).append(nodeName).toString();
                    } else {
                        path = nodeName;
                    }
                    nodeUrlBuffer.append(URL_PATH);
                    nodeUrlBuffer.append(URLEncoder.encode(path, URL_UTF));
                } catch (UnsupportedEncodingException e) {
                    // This should never be thrown
                    LOGGER
                            .error("UnsupportedEncodingException while building NodeURL. Exception : "
                                    + e);
                }
            }
        }

        return nodeUrlBuffer.toString();
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected NodeURLBuilder() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the baseURL.
     * 
     * @return the baseURL
     */
    public static String getBaseURL() {
        return BASE_URL;
    }

}
