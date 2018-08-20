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

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PreferencesValidator;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.log4j.Logger;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.PortletInvokerInterceptor;
import org.jboss.portal.portlet.container.ContainerPortletInvoker;
import org.jboss.portal.portlet.impl.info.ContainerPreferencesInfo;
import org.jboss.portal.portlet.impl.jsr168.PortletContainerImpl;
import org.jboss.portal.portlet.impl.spi.AbstractPortletInvocationContext;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.spi.ServerContext;
import org.jboss.portal.portlet.state.AbstractPropertyContext;
import org.jboss.portal.portlet.state.AccessMode;
import org.jboss.portal.portlet.state.PropertyContext;

import com.groundworkopensource.portal.model.PreferencesHelper;

/**
 * This portlet invoker is a hack to get around issues caused by ICEFaces'
 * violation of the JSR-168 and JSR-286 specs (specifically, the ICEFaces
 * portlet container does not call the processAction() method of its portlets).
 * 
 * @author Paul Burry
 * @version $Revision: 1733 $
 * @since GWMON 6.0
 */
public class IceFacesPortletInterceptor extends PortletInvokerInterceptor {
	private static final Logger log = Logger
			.getLogger(IceFacesPortletInterceptor.class);
	public static final String CONTAINER_PREFS_ATTRIBUTE = "com.gwos.container_prefs";
	public static final String VALIDATOR_ATTRIBUTE = "com.gwos.validator_attribute";
	public static final String ADMIN_PREFS_ATTRIBUTE = "adminPref";
	public static final String USER_PREFS_ATTRIBUTE = "userPref";

	public static final String DEFAULT_DASHBOARD_ADMIN_USER = "defaultDashboardAdminUser";

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
		if (invocation instanceof RenderInvocation) {
			Map<String, Object> requestAttributes = invocation
					.getRequestAttributes();
			if (requestAttributes == null) {
				requestAttributes = new HashMap<String, Object>();
			}

			String instanceId = (String) invocation.getAttribute("instanceid");
			log.info("InstanceId=" + instanceId);
			// Populate the admin preferences here for the dashboard
			if (instanceId != null) {
				AbstractPortletInvocationContext invocationContext = (AbstractPortletInvocationContext) invocation
						.getContext();
				HttpServletRequest clientRequest = invocationContext
						.getClientRequest();
				
				ServletContext servletContext  = clientRequest.getSession().getServletContext();
				String defaultDashboardAdminUser = servletContext.getInitParameter(
								DEFAULT_DASHBOARD_ADMIN_USER);
				PropertyContext adminPref = PreferencesHelper
						.findAdminPreferencesByWindowInstanceId(instanceId,
								defaultDashboardAdminUser);
				requestAttributes.put(ADMIN_PREFS_ATTRIBUTE, adminPref);
			} // end if

			// Create a copy of the property map that believes that we are not
			// in the render phase.
			PropertyContext prefs = (PropertyContext) invocation
					.getAttribute(PropertyContext.PREFERENCES_ATTRIBUTE);
			if (prefs != null && prefs instanceof AbstractPropertyContext) {
				AccessMode accessMode = invocation.getInstanceContext()
						.getAccessMode();

				requestAttributes.put(PropertyContext.PREFERENCES_ATTRIBUTE,
						new AbstractPropertyContext(accessMode,
								((AbstractPropertyContext) prefs).getPrefs(),
								false));
			}
			// Add user preferences to the request attribute. Need for the
			// dashboard
			requestAttributes.put(USER_PREFS_ATTRIBUTE, prefs);

			PortletContainerImpl container = (PortletContainerImpl) invocation
					.getAttribute(ContainerPortletInvoker.PORTLET_CONTAINER);
			if (container != null) {
				ContainerPreferencesInfo containerPrefs = (ContainerPreferencesInfo) container
						.getInfo().getPreferences();
				if (containerPrefs != null) {
					requestAttributes.put(CONTAINER_PREFS_ATTRIBUTE,
							containerPrefs);
				}

				PreferencesValidator validator = container
						.getPreferencesValidator();
				if (validator != null) {
					requestAttributes.put(VALIDATOR_ATTRIBUTE, validator);
				}
			}

			invocation.setRequestAttributes(requestAttributes);
		}

		return super.invoke(invocation);
	}

}
