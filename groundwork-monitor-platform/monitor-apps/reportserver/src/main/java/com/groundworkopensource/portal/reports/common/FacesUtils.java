/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.common;

import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpSession;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

import com.groundworkopensource.portal.model.ExtendedUIRole;
import com.groundworkopensource.portal.model.CommonUtils;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * JSF utilities.
 */
@SuppressWarnings("deprecation")
public class FacesUtils {
	/**
	 * Protected Constructor - Rationale: Instantiating utility classes does not
	 * make sense. Hence the constructors should either be private or (if you
	 * want to allow sub-classing) protected. <br>
	 * 
	 * Refer to "HideUtilityClassConstructor" section in
	 * http://checkstyle.sourceforge.net/config_design.html.
	 */

	/** . */

	public static final String EXTENDED_ROLE_ATT_BV = "com.gwos.portal.ext_role_atts.BV";

	protected FacesUtils() {
		// prevents calls from subclass
		throw new UnsupportedOperationException();
	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(FacesUtils.class
			.getName());

	/**
	 * 
	 */
	private static final String CONTEXT_PARAMETER_NOT_FOUND = "Context Parameter not found.";
	/**
	 * 
	 */
	private static final String CONTEXT_PARAMETER_IS_NULL_OR_EMPTY = "Context Parameter is null or empty.";
	/**
	 * 
	 */
	private static final String EMPTY_STRING = "";

	/**
	 * Get servlet context.
	 * 
	 * @return the servlet context
	 */
	public static ServletContext getServletContext() {
		return (ServletContext) FacesContext.getCurrentInstance()
				.getExternalContext().getContext();
	}

	/**
	 * @return ExternalContext
	 */
	public static ExternalContext getExternalContext() {
		FacesContext fc = FacesContext.getCurrentInstance();
		return fc.getExternalContext();
	}

	/**
	 * @param create
	 * @return HttpSession
	 */
	public static HttpSession getHttpSession(final boolean create) {
		return (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(create);
	}

	/**
	 * Get managed bean based on the bean name.
	 * 
	 * @param beanName
	 *            the bean name
	 * @return the managed bean associated with the bean name
	 */
	public static Object getManagedBean(final String beanName) {

		return getValueBinding(getJsfEl(beanName)).getValue(
				FacesContext.getCurrentInstance());
	}

	/**
	 * Remove the managed bean based on the bean name.
	 * 
	 * @param beanName
	 *            the bean name of the managed bean to be removed
	 */
	public static void resetManagedBean(final String beanName) {
		getValueBinding(getJsfEl(beanName)).setValue(
				FacesContext.getCurrentInstance(), null);
	}

	/**
	 * Store the managed bean inside the session scope.
	 * 
	 * @param beanName
	 *            the name of the managed bean to be stored
	 * @param managedBean
	 *            the managed bean to be stored
	 */
	public static void setManagedBeanInSession(final String beanName,
			final Object managedBean) {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put(beanName, managedBean);
	}

	/**
	 * Store the managed bean inside the request scope.
	 * 
	 * @param beanName
	 *            the name of the managed bean to be stored
	 * @param managedBean
	 *            the managed bean to be stored
	 */
	public static void setManagedBeanInRequest(final String beanName,
			final Object managedBean) {
		FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
				.put(beanName, managedBean);
	}

	/**
	 * Get parameter value from request scope.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @return the parameter value
	 */
	public static String getRequestParameter(final String name) {
		return (String) FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get(name);
	}

	/**
	 * Gest parameter value from the the session scope.
	 * 
	 * @param name
	 *            name of the parameter
	 * @return the parameter value if any.
	 */
	public static String getSessionParameter(final String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest myRequest = (HttpServletRequest) context
				.getExternalContext().getRequest();
		return myRequest.getParameter(name);
	}

	/**
	 * Get parameter value from the web.xml file.
	 * 
	 * @param parameter
	 *            name to look up
	 * @return the value of the parameter
	 */
	public static String getFacesParameter(final String parameter) {
		// Get the servlet context based on the faces context
		ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
				.getExternalContext().getContext();

		// Return the value read from the parameter
		return sc.getInitParameter(parameter);
	}

	/**
	 * Add information message.
	 * 
	 * @param msg
	 *            the information message
	 */
	public static void addInfoMessage(final String msg) {
		addInfoMessage(null, msg);
	}

	/**
	 * Add information message to a specific client.
	 * 
	 * @param clientId
	 *            the client id
	 * @param msg
	 *            the information message
	 */
	public static void addInfoMessage(final String clientId, final String msg) {
		FacesContext.getCurrentInstance().addMessage(clientId,
				new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
	}

	/**
	 * Add error message.
	 * 
	 * @param msg
	 *            the error message
	 */
	public static void addErrorMessage(final String msg) {
		addErrorMessage(null, msg);
	}

	/**
	 * Add error message to a specific client.
	 * 
	 * @param clientId
	 *            the client id
	 * @param msg
	 *            the error message
	 */
	public static void addErrorMessage(final String clientId, final String msg) {
		FacesContext.getCurrentInstance().addMessage(clientId,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
	}

	/**
	 * @return Application
	 */
	private static Application getApplication() {
		ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder
				.getFactory(FactoryFinder.APPLICATION_FACTORY);
		return appFactory.getApplication();
	}

	/**
	 * @return ValueBinding
	 * 
	 * @param el
	 * @return
	 */
	private static ValueBinding getValueBinding(final String el) {
		return getApplication().createValueBinding(el);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unused")
	private static HttpServletRequest getServletRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
	}

	/**
	 * @param value
	 * @return String
	 */
	private static String getJsfEl(final String value) {
		return "#{" + value + "}";
	}

	/**
	 * method used to get the context param value from the web.xml.
	 * 
	 * @param paramname
	 * @return
	 */
	public static String getContextParam(final String paramname) {

		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext context = fc.getExternalContext();
		Map<String, String> initParameterMap = context.getInitParameterMap();

		String name = "";
		if (initParameterMap != null && initParameterMap.containsKey(paramname)) {
			name = initParameterMap.get(paramname);

			if (name != null && !name.equals(EMPTY_STRING)) {
				return name;
			} else {
				LOGGER.warn(CONTEXT_PARAMETER_IS_NULL_OR_EMPTY);
			}
		} else {
			LOGGER.warn(CONTEXT_PARAMETER_NOT_FOUND);
		}

		return name;
	}

	/**
	 * Gets the ExtendedRole Attributes
	 * 
	 * @return List of Extended UI Roles
	 */
	@SuppressWarnings("unchecked")
	public static List<ExtendedUIRole> getExtendedRoleAttributes() {
		List<ExtendedUIRole> retObj = null;
		try {
			HttpServletRequest request = (HttpServletRequest) PolicyContext
					.getContext("javax.servlet.http.HttpServletRequest");
			if (request != null && request.getSession() != null) {
				Object obj = request.getSession().getAttribute(
						EXTENDED_ROLE_ATT_BV);
				if (obj == null) {
					retObj = com.groundworkopensource.portal.common.FacesUtils
							.getExtendedRoles().getList();
					request.getSession().setAttribute(EXTENDED_ROLE_ATT_BV,
							retObj);
				}
			} // end if
		} catch (Exception exc) {
			LOGGER.error("Unable to read extendedrole attributes");
		}
		return retObj;
	}

}