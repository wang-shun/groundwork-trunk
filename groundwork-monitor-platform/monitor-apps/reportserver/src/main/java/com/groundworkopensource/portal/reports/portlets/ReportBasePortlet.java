package com.groundworkopensource.portal.reports.portlets;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;


/**
 * The Class ReportBasePortlet.
 */
public class ReportBasePortlet extends BasePortlet {
    

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
        super.setViewPath(portletConfig.getInitParameter("com.icesoft.faces.portlet.viewPageURL"));
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
        // Do nothing
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
    	// Do nothing
    }
    
    

}
