package com.groundworkopensource.portal.statusviewer.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * The Class HostViewPortlet.
 */
public class HostViewPortlet extends BasePortlet {

    /** HOST_HEALTH_IFACE - view file for Service Health Portlet. */
    private static final String HOSTVIEW_IFACE = "/jsp/hostview.iface";

    /** HOSTHEALTH_TITLE. */
    public static final String HOSTVIEW_TITLE = "Host View";

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
        response
                .setTitle(PortletUtils.getPortletTitle(request, HOSTVIEW_TITLE));
        request.setAttribute(Constant.IS_IN_SV_CONSTANT, Boolean.valueOf("true"));
        super.setViewPath(HOSTVIEW_IFACE);
        super.doView(request, response);
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
     * portlet.
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
     */
    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit HostView Preferences");

        // call doEditPref() of BasePortlet.
        /*
         * super .doEditPref(request, response, DashboardEditPrefConstants
         * .getEditPreferences(NodeType.HOST), Constant.HOSTSTAT_EDIT_PATH);
         */
    }

}
