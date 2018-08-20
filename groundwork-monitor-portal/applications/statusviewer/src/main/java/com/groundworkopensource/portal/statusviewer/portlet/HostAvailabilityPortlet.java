package com.groundworkopensource.portal.statusviewer.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DashboardEditPrefConstants;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * This portlet displays the Host availability for the host.
 * 
 * @author shivangi_walvekar
 * 
 */
public class HostAvailabilityPortlet extends BasePortlet {

    /**
     * String Constant for title "Host Availability"
     */
    public static final String HOST_AVAILABILITY_PORTLET_TITLE = "Recent State Changes";

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.BasePortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.setViewPath(Constant.HOST_AVAILABILITY_VIEW_PATH);
        // Set the portlet title.
        response.setTitle(PortletUtils.getPortletTitle(request,
                HOST_AVAILABILITY_PORTLET_TITLE, false, false));
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
        super.processAction(request, response, DashboardEditPrefConstants
                .getRequestPreferenceParamsMap(NodeType.HOST));
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
        response.setTitle("Edit Host Preferences");

        // call doEditPref() of BasePortlet.
        super
                .doEditPref(request, response, DashboardEditPrefConstants
                        .getEditPreferences(NodeType.HOST),
                        Constant.HOSTSTAT_EDIT_PATH);
    }
}
