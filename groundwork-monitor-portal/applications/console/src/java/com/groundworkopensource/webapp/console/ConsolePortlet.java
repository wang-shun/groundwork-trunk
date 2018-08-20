package com.groundworkopensource.webapp.console;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import com.groundworkopensource.portal.common.BasePortlet;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.apache.log4j.Logger;

/**
 * The Class ConsolePortlet.
 */
public class ConsolePortlet extends BasePortlet {
	/**
     * 
     */
	private static final String CONSOLE_IFACE = "/Console.iface";

	/**
	 * CONSOLE_TITLE
	 */
	public static final String CONSOLE_TITLE = "Event Console";

	/**
	 * Logging
	 */
	private static Logger logger = Logger.getLogger(ConsolePortlet.class
			.getName());

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
		HttpServletRequest httpServletRequest = getHttpServletRequest();
		if (httpServletRequest.getParameterNames() != null
				&& httpServletRequest.getParameterNames().hasMoreElements()) {
			String filterType = httpServletRequest.getParameter("filterType");
			String filterValue = httpServletRequest.getParameter("filterValue");
			if (filterType != null && filterValue != null)
				request.getPortletSession().setAttribute(
						ConsoleConstants.GWOS_CONSOLE_VIEWPARAM,
							filterType + ConsoleConstants.GWOS_CONSOLE_SESSION_PARAM_DELIM + filterValue);
		}
		// Set the portlet title.
		response.setTitle(CONSOLE_TITLE);
		super.setViewPath(CONSOLE_IFACE);
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

	/**
	 * Helper to get the httpservlet
	 */
	/* Get the http session to the user */
	protected HttpServletRequest getHttpServletRequest() {
		HttpServletRequest request = null;
		try {
			request = (HttpServletRequest) PolicyContext
					.getContext("javax.servlet.http.HttpServletRequest");
		} catch (PolicyContextException pce) {
			logger.error(pce.getMessage());
		} // end try/catch
		return request;
	}

}
