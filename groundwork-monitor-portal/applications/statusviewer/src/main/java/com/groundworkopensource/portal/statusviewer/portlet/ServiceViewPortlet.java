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
 * @author swapnil_gujrathi
 * 
 */
public class ServiceViewPortlet extends BasePortlet {
    /**
     * SERVICE_HEALTH_IFACE - view file for Service Health Portlet.
     */
    private static final String SERVICEVIEW_IFACE = "/jsp/serviceview.iface";

    /**
     * SERVICEHEALTH_TITLE
     */
    public static final String SERVICEVIEW_TITLE = "Service View";

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                SERVICEVIEW_TITLE));
        request.setAttribute(Constant.IS_IN_SV_CONSTANT, Boolean.valueOf("true"));
        super.setViewPath(SERVICEVIEW_IFACE);
        super.doView(request, response);
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
    }

    /**
     * This method is Responsible for editing preferences of SERVICE statistics
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
        response.setTitle("Edit ServiceView Preferences");

    }

}
