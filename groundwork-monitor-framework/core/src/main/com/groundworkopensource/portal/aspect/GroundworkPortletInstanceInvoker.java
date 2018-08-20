/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.aspect;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jboss.portal.core.model.instance.InstanceInvoker;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.impl.spi.AbstractPortletInvocationContext;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.spi.UserContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom portlet instance invoker for the Groundwork Monitor portal.
 * 
 * @author Paul Burry
 * @version $Revision: 1811 $
 * @since GWMON 6.0
 */
public class GroundworkPortletInstanceInvoker extends InstanceInvoker {
	private static final Logger log = Logger
			.getLogger(GroundworkPortletInstanceInvoker.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.portal.core.model.instance.InstanceInvoker#invoke(org.jboss
	 * .portal.portlet.invocation.PortletInvocation)
	 */
	@Override
	public PortletInvocationResponse invoke(PortletInvocation invocation)
			throws IllegalArgumentException, PortletInvokerException {
		String targetId = invocation.getTarget().getId();
		// Status Viewer page render requests
		if (invocation instanceof RenderInvocation
				&& invocation.getTarget().getId().contains("statusviewer")) {
			AbstractPortletInvocationContext invocationContext = (AbstractPortletInvocationContext) invocation
					.getContext();
			HttpServletRequest clientRequest = invocationContext
					.getClientRequest();
			String pathInfo = clientRequest.getPathInfo();
			UserContext userContext = invocation.getUserContext();
			String userId = (userContext == null ? null : userContext.getId());

			if (pathInfo != null && pathInfo.equals("/portal/groundwork-monitor/status")) {
				String nodeType = (String) clientRequest
						.getParameter("nodeType");
				String nodeID = (String) clientRequest.getParameter("nodeID");
				String nodeName = (String) clientRequest.getParameter("name");
				String path = (String) clientRequest.getParameter("path");
				clientRequest.setAttribute("com.gwos.sv.nodeType", nodeType);
				clientRequest.setAttribute("com.gwos.sv.nodeID", nodeID);
				clientRequest.setAttribute("com.gwos.sv.nodeName", nodeName);
				clientRequest.setAttribute("com.gwos.sv.path", path);
			} // end if
		} // end if

		return super.invoke(invocation);
	}
}
