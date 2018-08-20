package com.groundworkopensource.portal.statusviewer.servlet;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.NodeURLBuilder;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

/**
 * @author swapnil_gujrathi
 * 
 */
public class URLMapServlet extends HttpServlet {
    /**
     * SERVICE URL parameter.
     */
    private static final String SERVICE = "service";

    /**
     * HOST URL parameter.
     */
    private static final String HOST = "host";

    /**
     * HOSTGROUP URL parameter.
     */
    private static final String HOSTGROUP = "hostgroup";

    /**
     * SERVICEGROUP URL parameter.
     */
    private static final String SERVICEGROUP = "servicegroup";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(URLMapServlet.class
            .getName());

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
    }

    /**
     * (non-Javadoc).
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * 
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            mapURL(request, response);
        } catch (GWPortalException exception) {
            LOGGER.warn(exception.getMessage());
            // redirect to Status Viewer => Entire Network page.
            redirectToEntireNetworkPage(response);
        } catch (Exception e) {
            LOGGER
                    .warn("Unable to get the Faces Context. Redirecting user to Status Viewer -> Entire Network view. Actual Exception : "
                            + e);
            // redirect to Status Viewer => Entire Network page.
            redirectToEntireNetworkPage(response);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * mapURL() parses URL parameters and then redirects to appropriate status
     * viewer URL.
     * 
     * @param request
     * @param response
     * @throws GWPortalException
     */
    private void mapURL(HttpServletRequest request, HttpServletResponse response)
            throws GWPortalException {
        // Get the FacesContext inside HttpServlet.
        FacesContext facesContext = FacesUtils.getFacesContext(request,
                response);

        // Now you can do your thing with the facesContext.
        if (null == facesContext) {
            LOGGER
                    .warn("Unable to get the Faces Context. Redirecting user to Status Viewer -> Entire Network view.");
            // Redirect to Status Viewer => Entire Network page.
            redirectToEntireNetworkPage(response);
            return;
        }
        FacesUtils.setFacesContext(facesContext);
        // retrieve the RTMM managed bean instance from FacesContext.
        ReferenceTreeMetaModel referenceTreeModel = null;
        try {
            referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
                    .getManagedBean(Constant.REFERENCE_TREE);
        } catch (Exception e) {
            // initialize RTMM
            // LOGGER.error("initializing RTMM ... ");
            // facesContext.getExternalContext().getApplicationMap().put(
            // Constant.REFERENCE_TREE, new ReferenceTreeMetaModel());
            // referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
            // .getManagedBean(Constant.REFERENCE_TREE);
            referenceTreeModel = null;
        }

        if (null == referenceTreeModel) {
            LOGGER
                    .warn("Unable to get the managed bean instance of ReferenceTreeMetaModel (RTMM). Redirecting user to Status Viewer -> Entire Network view.");
            /*
             * Unable to get managed bean instance of ReferenceTreeMetaModel
             * (RTMM). RTMM instance should be there. Application scoped RTMM
             * gets initialized whenever any user accesses any Status Viewer
             * view (entire network / host / service / hodt group / service
             * group) first time after deploying the application.
             */
            // Redirect to Status Viewer => Entire Network page.
            redirectToEntireNetworkPage(response);
            return;
        }
        // first check for type "service".
        String service = request.getParameter(SERVICE);
        if (null != service) {
            // service gets identified by the "host" name. So check for
            // host.
            String host = request.getParameter(HOST);
            if (null == host) {
                String serviceGroup = request.getParameter("servicegroup");
                if (null == serviceGroup) {
                    throw new GWPortalException(
                            "Invalid parameters specified by external application while accessing Service in the status viewer. For Service, application should specify both host name and service name. Redirecting user to Status Viewer -> Entire Network view.");
                } else {
                    // retrieve Service Entity by Host and Service Name
                    NetworkMetaEntity serviceEntityByServiceGroupAndServiceName = referenceTreeModel
                            .getServiceEntityByServiceGroupAndServiceName(
                                    serviceGroup, service);
                    redirectToStatusViewerURL(
                            serviceEntityByServiceGroupAndServiceName,
                            response, serviceGroup);
                }
            } else {
                // retrieve Service Entity by Host and Service Name
                NetworkMetaEntity serviceEntityByHostAndServiceName = referenceTreeModel
                        .getServiceEntityByHostAndServiceName(host, service);
                redirectToStatusViewerURL(serviceEntityByHostAndServiceName,
                        response, null);
            }

            return;
        }

        // type "host"
        String host = request.getParameter(HOST);
        if (null != host) {
            NetworkMetaEntity hostByName = referenceTreeModel.getEntityByName(
                    NodeType.HOST, host);
            redirectToStatusViewerURL(hostByName, response, null);
            return;
        }

        // type "host group"
        String hostgroup = request.getParameter(HOSTGROUP);
        if (null != hostgroup) {
            NetworkMetaEntity hostGroupByName = referenceTreeModel
                    .getEntityByName(NodeType.HOST_GROUP, hostgroup);
            redirectToStatusViewerURL(hostGroupByName, response, null);
            return;
        }

        // type "service group"
        String servicegroup = request.getParameter(SERVICEGROUP);
        if (null != servicegroup) {
            NetworkMetaEntity serviceGroupByName = referenceTreeModel
                    .getEntityByName(NodeType.SERVICE_GROUP, servicegroup);
            redirectToStatusViewerURL(serviceGroupByName, response, null);
            return;
        }

    }

    /**
     * Redirects To Status Viewer => Entire Network Page.
     * 
     * @param response
     */
    private void redirectToEntireNetworkPage(HttpServletResponse response) {
        try {
            response.sendRedirect(response.encodeRedirectURL(NodeURLBuilder
                    .getBaseURL()));
        } catch (IOException e) {
            LOGGER
                    .error("Failed to redirect to Status Viewer -> Entire Network page. URL ["
                            + NodeURLBuilder.getBaseURL()
                            + "] ... Actual Exception : " + e);
        }
    }

    /**
     * Constructs and Redirects to Status Viewer URL.
     * 
     * @param networkMetaEntity
     * @param response
     */
    private void redirectToStatusViewerURL(NetworkMetaEntity networkMetaEntity,
            HttpServletResponse response, String parent) {
        // create URL like this:
        // http://ps5764/portal/auth/status/HostView+1?name=localhost&svcmd=create&path=Linux+Servers%2Clocalhost
        String buildNodeURL = NodeURLBuilder.buildNodeURLForExternalRequest(
                networkMetaEntity.getType(), networkMetaEntity.getObjectId(),
                networkMetaEntity.getName(), parent);
        try {
            response.sendRedirect(response.encodeRedirectURL(buildNodeURL));
        } catch (IOException e) {
            LOGGER.error("Failed to redirect to URL [" + buildNodeURL
                    + "] ... Actual Exception : " + e);
        }
    }
}