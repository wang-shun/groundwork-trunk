package com.groundworkopensource.portal.statusviewer.portlet;

import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * The Class NetworkViewPortlet.
 */
public class NetworkViewPortlet extends BaseAuthPortlet {
    /**
     * 
     */
    private static final String NETWORKVIEW_IFACE = "/jsp/networkView.iface";

    /**
     * NETWORKVIEW_TITLE
     */
    public static final String NETWORKVIEWVIEW_TITLE = "Network View";

    /**
     * Status viewer application name for authentication.
     */
    public static final String STATUS_VIEWER_APP_NAME = "status-viewer";

    /**
     * Add head scripts and links.
     *
     * @param request render request
     * @param response render response
     */
    protected void provideHeaders(RenderRequest request, RenderResponse response) {
        addStyleLink(response, request.getContextPath() + "/css/networkView.css", "gw_networkview_css");
        addJavaScript(response, request.getContextPath() + "/js/d3.min.js", "d3_lib");
        addJavaScript(response, request.getContextPath() + "/js/dimple.latest.min.js", "dimple_lib");
        addJavaScript(response, request.getContextPath() + "/js/networkView.js", "gw_networkview_lib");
    }

    /**
     * (non-Javadoc).
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * 
     * @throws PortletException
     *             the portlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                NETWORKVIEWVIEW_TITLE));
        request.setAttribute(Constant.IS_IN_SV_CONSTANT, Boolean.valueOf("true"));
        if (authenticate(request, response, GW_APP_NAME)) {
            super.setViewPath(NETWORKVIEW_IFACE);
            super.doView(request, response);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        // call processAction() of BasePortlet.
        /*
         * super.processAction(request, response, DashboardEditPrefConstants
         * .getRequestPreferenceParamsMap(NodeType.HOST));
         */
    }

    /**
     * This method is Responsible for editing preferences of host statistics
     * portlet
     * 
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit NetworkView Preferences");

        // call doEditPref() of BasePortlet.
        /*
         * super .doEditPref(request, response, DashboardEditPrefConstants
         * .getEditPreferences(NodeType.HOST), Constant.HOSTSTAT_EDIT_PATH);
         */
    }

}