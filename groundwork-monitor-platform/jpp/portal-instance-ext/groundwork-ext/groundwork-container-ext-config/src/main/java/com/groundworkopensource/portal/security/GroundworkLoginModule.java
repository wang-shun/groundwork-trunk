package com.groundworkopensource.portal.security;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.security.Identity;
import javax.security.auth.login.LoginException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.jaas.AbstractLoginModule;

public class GroundworkLoginModule extends AbstractLoginModule {
	private static final Log log = ExoLogger
			.getLogger(GroundworkLoginModule.class);
	private String portalContainerName = "portal";

	public boolean abort() throws LoginException {
		return true;
	}

	public boolean commit() throws LoginException {
		return true;
	}

	public boolean login() throws LoginException {
		log.debug("GroundworkLoginModule start");
		return true;
	}

	public boolean logout() throws LoginException {
		return true;
	}

	@Override
	protected Log getLogger() {
		return log;
	}

	/**
	 * Helper to get the HttpServletRequest
	 */
	private HttpServletRequest getHttpRequestInfo() {
		/** The JACC PolicyContext key for the current Subject */
		HttpServletRequest request = null;
		try {
			request = (HttpServletRequest) PolicyContext
					.getContext("javax.servlet.http.HttpServletRequest");
		} catch (PolicyContextException e) {
			log.error("Exception in getHttpRequestInfo(): " + e);
			e.printStackTrace();
		}
		return request;
	}

	
}
