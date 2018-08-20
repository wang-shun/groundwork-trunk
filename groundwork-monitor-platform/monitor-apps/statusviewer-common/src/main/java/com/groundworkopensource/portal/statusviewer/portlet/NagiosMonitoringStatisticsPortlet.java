package com.groundworkopensource.portal.statusviewer.portlet;

import static com.groundworkopensource.portal.statusviewer.common.Constant.NAGIOS_MONITORING_HORIZONTAL_VIEW_PATH;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;
import com.groundworkopensource.portal.statusviewer.common.NagiosStatisticsConstants;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

/**
 * This portlet displays the Nagios Monitoring Statistics for the
 * host-group,service group or the entire network in horizontal layout.
 * 
 * @author shivangi_walvekar
 * 
 */
public class NagiosMonitoringStatisticsPortlet extends BasePortlet {

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    @Override
    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {

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
        super.setViewPath(NAGIOS_MONITORING_HORIZONTAL_VIEW_PATH);

        // Set the portlet title.
        renderResponse.setTitle(PortletUtils.getPortletTitle(renderRequest,
                NagiosStatisticsConstants.NAGIOS_STATISTICS_PORTLET_TITLE));

        super.doView(renderRequest, renderResponse);
    }
}
