package com.groundworkopensource.portal.statusviewer.portlet;

import static com.groundworkopensource.portal.statusviewer.common.Constant.NAGIOS_MONITORING_STACKED_VIEW_PATH;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.DashboardEditPrefConstants;

/**
 * This portlet displays the Nagios Monitoring Statistics for the
 * host-group,service group or the entire network in a stacked layout.
 * 
 * @author shivangi_walvekar
 * 
 */
public class StackedNagiosMonitoringStatisticsPortlet extends BasePortlet {

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
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setTitle("Edit Host Preferences");

        // call doEditPref() of BasePortlet.
        super
                .doEditPref(request, response, DashboardEditPrefConstants
                        .getEditPreferences(NodeType.HOST),
                        Constant.HOSTSTAT_EDIT_PATH);
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        super.setViewPath(NAGIOS_MONITORING_STACKED_VIEW_PATH);

        // Set the portlet title.
        renderResponse.setTitle(PortletUtils.getPortletTitle(renderRequest,
                NagiosStatisticsConstants.NAGIOS_STATISTICS_PORTLET_TITLE));

        super.doView(renderRequest, renderResponse);
    }
}
