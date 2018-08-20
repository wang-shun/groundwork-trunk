package com.groundworkopensource.portal.webui;

import java.io.IOException;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;
import javax.portlet.PortletPreferences;
import javax.portlet.ActionResponse;
import javax.portlet.ActionRequest;

/**
 * GroundWorkLicenseMessagePortlet displays soft limit exceeded message.
 * 
 * @author ArulShanmugam
 * 
 */

public class GroundWorkLicenseMessagePortlet extends GenericPortlet {

	private static final String CONTENT_TYPE_HTML = "text/html;charset=UTF-8";

	protected final void doView(RenderRequest request, RenderResponse response)
			throws UnavailableException, PortletSecurityException,
			PortletException, IOException {
		try {
			response.setContentType(CONTENT_TYPE_HTML);
			String path = "/pages/license-message.jsp";
			PortletRequestDispatcher rd = this.getPortletContext()
					.getRequestDispatcher(path);
			rd.include(request, response);

		} catch (Exception e) {
			e.printStackTrace();
			throw new PortletException(e);
		}

	}

}