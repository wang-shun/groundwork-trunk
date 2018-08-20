/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.statusviewer.common;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.model.ExtendedUIRole;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import org.apache.log4j.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author swapnil_gujrathi
 * 
 */
public class PortletUtils {

	/**
	 * ENTIRE_NETWORK
	 */
	private static final String ENTIRE_NETWORK = "Entire Network";

	/** . */
	public static final String EXTENDED_ROLE_ATT_SV = "com.gwos.portal.ext_role_atts.SV";

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(PortletUtils.class
			.getName());

	// /**
	// * Logger.
	// */
	// private static final Logger LOGGER = Logger.getLogger(PortletUtils.class
	// .getName());

	/**
	 * preferences Keys Map to be used for reading preferences.
	 */
	private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
	static {
		PREFERENCE_KEYS_MAP.put(Constant.NODE_NAME_PREF, null);
		PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_PREF,
				NodeType.SERVICE);
		PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_PREF,
				NodeType.HOST);
		PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_GROUP_PREF,
				NodeType.HOST_GROUP);
		PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_GROUP_PREF,
				NodeType.SERVICE_GROUP);
	}

	/**
	 * JAVAX_PORTLET_REQUEST constant
	 */
	private static final String JAVAX_PORTLET_REQUEST = "javax.portlet.request";

	/**
	 * Gets the "node name" from request attribute and constructs the
	 * "portlet title".
	 * 
	 * @param request
	 * @param staticPortletTitle
	 * @return Portlet Title to be displayed
	 */
	public static String getPortletTitle(RenderRequest request,
			String staticPortletTitle) {
		return computePortletTitle(request, staticPortletTitle, false, true);
	}

	/**
	 * Gets the "node name" from request attribute and constructs the
	 * "portlet title".
	 * 
	 * @param request
	 * @param staticPortletTitle
	 * @param isServicePortlet
	 * @return Portlet Title to be displayed
	 */
	public static String getPortletTitle(RenderRequest request,
			String staticPortletTitle, boolean isServicePortlet) {
		return computePortletTitle(request, staticPortletTitle,
				isServicePortlet, false);
	}

	/**
	 * @param request
	 * @param staticPortletTitle
	 * @param isServicePortlet
	 * @param isPortletApplicableForEntireNetwork
	 * @return Portlet Title to be displayed
	 */
	public static String getPortletTitle(RenderRequest request,
			String staticPortletTitle, boolean isServicePortlet,
			boolean isPortletApplicableForEntireNetwork) {
		return computePortletTitle(request, staticPortletTitle,
				isServicePortlet, isPortletApplicableForEntireNetwork);
	}

	/**
	 * Gets the "node name" from request attribute and constructs the
	 * "portlet title".
	 * 
	 * @param request
	 * @param staticPortletTitle
	 * @param isServicePortlet
	 * @param isPortletApplicableForEntireNetwork
	 * @return Portlet Title to be displayed
	 */
	private static String computePortletTitle(RenderRequest request,
			String staticPortletTitle, boolean isServicePortlet,
			boolean isPortletApplicableForEntireNetwork) {

		StringBuilder portletTitle = new StringBuilder();
		try {
			String nodeName = (String) request
					.getAttribute(IPCHandlerConstants.NODE_NAME_PARAM);
			if (nodeName != null && !nodeName.equals(Constant.EMPTY_STRING)) {
				// node name received - so we are in status viewer
				portletTitle.append(staticPortletTitle).append(Constant.COLON)
						.append(Constant.SPACE);
				portletTitle.append(nodeName);
				return portletTitle.toString();
			}

			// check for preferences
			RenderRequest preferenceRequest = (RenderRequest) request
					.getAttribute(JAVAX_PORTLET_REQUEST);
			boolean prefFound = false;
			if (preferenceRequest != null) {
				PortletPreferences allPreferences = FacesUtils
						.getAllPreferences(request, true);
				if (allPreferences != null && allPreferences.getMap() != null
						&& !allPreferences.getMap().isEmpty()) {
					/*
					 * read the custom portlet title from preferences and return
					 * it if specified and not null or empty
					 */
					String customPortletTitlePrefValue = allPreferences
							.getValue(PreferenceConstants.CUSTOM_PORTLET_TITLE,
									Constant.EMPTY_STRING);
					if (customPortletTitlePrefValue != null
							&& !customPortletTitlePrefValue.trim().equals(
									Constant.EMPTY_STRING)) {
						// return custom portlet title
						return customPortletTitlePrefValue;
					}

					/*
					 * for service portlet, we need to append host name in round
					 * brackets along with service name to the portlet title.
					 */
					if (isServicePortlet) {
						String serviceNamePrefValue = allPreferences.getValue(
								PreferenceConstants.DEFAULT_SERVICE_PREF,
								Constant.EMPTY_STRING);
						if (serviceNamePrefValue != null
								&& !serviceNamePrefValue.trim().equals(
										Constant.EMPTY_STRING)) {
							// append 'static portlet title'
							portletTitle.append(staticPortletTitle)
									.append(Constant.COLON)
									.append(Constant.SPACE);
							// append service name
							portletTitle.append(serviceNamePrefValue);

							// append host name
							String hostNamePrefValue = allPreferences.getValue(
									PreferenceConstants.DEFAULT_HOST_PREF,
									Constant.EMPTY_STRING);
							if (hostNamePrefValue != null
									&& !hostNamePrefValue.trim().equals(
											Constant.EMPTY_STRING)) {
								portletTitle.append(Constant.SPACE);
								portletTitle
										.append(Constant.OPENING_ROUND_BRACE);
								portletTitle.append(hostNamePrefValue);
								portletTitle
										.append(Constant.CLOSING_ROUND_BRACE);
							}

							// return the portlet title
							return portletTitle.toString();
						}
					} // end of if (isServicePortlet)
					Set<Entry<String, NodeType>> prefsEntrySet = PREFERENCE_KEYS_MAP
							.entrySet();

					for (Entry<String, NodeType> prefEntry : prefsEntrySet) {
						String preferenceValue = allPreferences.getValue(
								prefEntry.getKey(), Constant.EMPTY_STRING);
						if (preferenceValue != null
								&& !preferenceValue.trim().equals(
										Constant.EMPTY_STRING)) {
							// set node name and node type
							nodeName = preferenceValue;
							prefFound = true;
							break;
						} // end of if
					} // end of for
				}
			}

			// append 'static portlet title'
			portletTitle.append(staticPortletTitle).append(Constant.COLON)
					.append(Constant.SPACE);

			if (prefFound) {
				// Preferences found. So assign title as per the preference
				// value
				portletTitle.append(nodeName);
			} else if (!isPortletApplicableForEntireNetwork) {
				return staticPortletTitle;
			} else {
				// No preferences found. So assign title as 'Entire Network"
				portletTitle.append(ENTIRE_NETWORK);
			}
		} catch (PreferencesException pe) {
			// OK to eat.
			portletTitle = new StringBuilder();
			portletTitle.append(staticPortletTitle).append(Constant.COLON)
					.append(Constant.SPACE).append(ENTIRE_NETWORK);
		}
		return portletTitle.toString();

		// CODE KEPT FOR FUTURE DEBUGGING USE
		// Enumeration attributeNames = request.getAttributeNames();
		// while (attributeNames.hasMoreElements()) {
		// String key = (String) attributeNames.nextElement();
		// logger.error(key + " : " + request.getAttribute(key));
		// }
	}

	/**
	 * Returns Map of preferences - contains ServiceName and HostName if
	 * obtained. Throws exception if preferences not received.
	 * 
	 * @return Map of preferences - contains ServiceName and HostName if
	 *         obtained.
	 * @throws PreferencesException
	 */
	public static Map<String, String> getServicePortletPreferences()
			throws PreferencesException {
		PortletPreferences allPreferences = FacesUtils.getAllPreferences();
		if (null != allPreferences) {
			String serviceName = allPreferences.getValue(
					PreferenceConstants.SERVICE_NAME, Constant.EMPTY_STRING);
			if (serviceName == null
					|| serviceName.equals(Constant.EMPTY_STRING)) {
				serviceName = allPreferences.getValue(
						PreferenceConstants.DEFAULT_SERVICE_PREF,
						Constant.EMPTY_STRING);
			}
			String hostName = allPreferences.getValue(
					PreferenceConstants.HOST_NAME, Constant.EMPTY_STRING);
			if (hostName == null || hostName.equals(Constant.EMPTY_STRING)) {
				hostName = allPreferences.getValue(
						PreferenceConstants.DEFAULT_HOST_PREF,
						Constant.EMPTY_STRING);
			}

			if (null != hostName && null != serviceName
					&& !hostName.equals(Constant.EMPTY_STRING)
					&& !serviceName.equals(Constant.EMPTY_STRING)) {
				Map<String, String> servicePrefsMap = new HashMap<String, String>();
				servicePrefsMap.put(PreferenceConstants.HOST_NAME, hostName);
				servicePrefsMap.put(PreferenceConstants.SERVICE_NAME,
						serviceName);
				return servicePrefsMap;
			}
		}
		throw new PreferencesException();
	}

	/**
	 * Protected Constructor - Rationale: Instantiating utility classes does not
	 * make sense. Hence the constructors should either be private or (if you
	 * want to allow sub-classing) protected. <br>
	 * 
	 * Refer to "HideUtilityClassConstructor" section in
	 * http://checkstyle.sourceforge.net/config_design.html.
	 */
	protected PortletUtils() {
		// prevents calls from subclass
		throw new UnsupportedOperationException();
	}

	/**
	 * return true if portlet is in dashboard otherwise false
	 * 
	 * @return boolean
	 */
	public static boolean isInDashbord() {
		// IS_IN_SV Contant is set only in networkview, hostgroupview,
		// hostview, servicegroupview and serviceview portlets
		Object obj = getRenderReqParam(Constant.IS_IN_SV_CONSTANT);
		if (obj != null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * return true if portlet is in status viewer otherwise false
	 * 
	 * @return boolean
	 */
	public static boolean isInStatusViewer() {
		// IS_IN_SV Contant is set only in networkview , hostgroupview,
		// hostview, servicegroupview and serviceview portlets
		Object obj = getRenderReqParam(Constant.IS_IN_SV_CONSTANT);
		if (obj != null) {
			Boolean isInSVFlag = (Boolean) obj;
			return isInSVFlag.booleanValue();
		} else {
			return false;
		}
	}

	/**
	 * Returns PortletRequest
	 * 
	 * @return PortletRequest
	 */
	private static PortletRequest getPortletRequest() {
		PortletRequest request = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (null != facesContext && null != facesContext.getExternalContext()) {
			ExternalContext externalContext = facesContext.getExternalContext();
			request = (PortletRequest) externalContext.getRequest();
		}
		return request;
	}

	/**
	 * This method retrieved the parameter value from render request object.
	 * While accessing portlets, the parameters are passed via URL i.e. query
	 * parameters. The interceptor sets these parameters in renderRequest
	 * object. The handler retrieves if from render request and initializes
	 * corresponding bean.
	 * 
	 * @param key
	 * @return Object for ReqParam
	 */
	private static Object getRenderReqParam(String key) {

		PortletRequest portletRequest = getPortletRequest();

		if (portletRequest != null) {
			return portletRequest.getAttribute(key);
		}
		return null;
	}

	/**
	 * Checks if actions is enabled for the user or not true. Or else false.
	 * 
	 * @return true if actions are enabled
	 */
	public static boolean isActionsEnabled() {
		PortletRequest portletRequest = getPortletRequest();
		if (portletRequest != null) {
			try {
				List<ExtendedUIRole> extUIRole = (List<ExtendedUIRole>) getHttpServletRequest()
						.getSession().getAttribute(EXTENDED_ROLE_ATT_SV);
				if (extUIRole != null) {
					for (ExtendedUIRole role : extUIRole) {
						if (role.isActionsEnabled())
							return true;
					} // end for
				} // end if
			} catch (PolicyContextException pce) {
				// OK to swallow
			} // try/catch
		} // end if
		return false;
	}

	/* Get the http session to the user */
	protected static HttpServletRequest getHttpServletRequest()
			throws PolicyContextException {
		HttpServletRequest request = (HttpServletRequest) PolicyContext
				.getContext("javax.servlet.http.HttpServletRequest");
		return request;
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
						EXTENDED_ROLE_ATT_SV);
				if (obj == null) {
					// Get the extended role from REST API...
					com.groundworkopensource.portal.model.ExtendedRoleList wrapperList = FacesUtils
							.getExtendedRoles();
					retObj = wrapperList.getList();
					request.getSession().setAttribute(EXTENDED_ROLE_ATT_SV,
							retObj);
				} else {
					retObj = (List<ExtendedUIRole>) obj;
				}
			}
		} catch (Exception pce) {
			// LOGGER.error(pce.getMessage());
		} // end try/catch
		return retObj;
	}

	/**
	 * Returns UserRoleBean instance. This method first looks the instance in
	 * "logged in users session scope". If its not in the session, it
	 * instantiates UserRoleBean, stores it in the session and returns it.
	 * 
	 * @return UserRoleBean
	 */
	public static UserExtendedRoleBean getUserExtendedRoleBean() {
		if (null != FacesContext.getCurrentInstance()) {
			try {
				Object object = FacesUtils
						.getManagedBean(Constant.USER_EXTENDED_ROLE_BEAN);
				if (null != object) {
					UserExtendedRoleBean userExtendedRoleBean = (UserExtendedRoleBean) object;
					return userExtendedRoleBean;
				}
			} catch (Exception e) {
				// try it from the standard session
				Object retObj = FacesUtils.getPortletSession(false)
						.getAttribute(Constant.USER_EXTENDED_ROLE_BEAN);
				if (retObj != null) {
					UserExtendedRoleBean userExtendedRoleBean = (UserExtendedRoleBean) retObj;
					return userExtendedRoleBean;
				} // end if

			}
		} // end try/catch
			// When cross contexting from event console especially for Event
			// Tile popup the bean is null,
			// so let is create it and put it in the session
		UserExtendedRoleBean userExtendedRoleBean = new UserExtendedRoleBean();
		if (null != FacesContext.getCurrentInstance()) {
			FacesUtils.getPortletSession(false).setAttribute(
					Constant.USER_EXTENDED_ROLE_BEAN, userExtendedRoleBean);
		}

		// If you reach here then some weird scenario session timed out.
		LOGGER.debug("Cross context call from event console");
		return userExtendedRoleBean;
	}

	/**
	 * Sets portlet title to "Host Group Dashboard portlets" for users with
	 * extended roles set.
	 * 
	 * @param request
	 * @param response
	 * @param staticPortletTitle
	 */
	public static void setHostGroupDashboardPortletTitle(RenderRequest request,
			RenderResponse response, String staticPortletTitle) {
		if (!isInStatusViewer()) {
			try {
				PortletPreferences allPreferences = FacesUtils
						.getAllPreferences(request, false);
				if (null != allPreferences) {
					// if default HG preference is null or empty
					String defaultHGPref = allPreferences.getValue(
							PreferenceConstants.DEFAULT_HOST_GROUP_PREF,
							Constant.EMPTY_STRING);
					if (null == defaultHGPref
							|| Constant.EMPTY_STRING.equals(defaultHGPref)) {
						// check if custom portlet title is set
						String customPortletTitle = allPreferences.getValue(
								PreferenceConstants.CUSTOM_PORTLET_TITLE,
								Constant.EMPTY_STRING);
						if (null == customPortletTitle
								|| Constant.EMPTY_STRING
										.equals(customPortletTitle)) {
							// now check for defaultHostGroupPreference set in
							// portlet.xml
							String hgPreferenceFromPortletXML = allPreferences
									.getValue(
											Constant.PORTLET_XML_DEFAULT_HOSTGROUP_PREFERENCE,
											Constant.EMPTY_STRING);
							if (null != hgPreferenceFromPortletXML
									&& !Constant.EMPTY_STRING
											.equals(hgPreferenceFromPortletXML)) {
								// initialize UserExtendedRoleBean
								UserExtendedRoleBean userExtendedRoleBean = new UserExtendedRoleBean(
										getExtendedRoleAttributes());

								// get the extended role host group list
								List<String> extRoleHostGroupList = userExtendedRoleBean
										.getExtRoleHostGroupList();
								if (!extRoleHostGroupList.isEmpty()
										&& !extRoleHostGroupList
												.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
										&& !extRoleHostGroupList
												.contains(hgPreferenceFromPortletXML)) {
									response.setTitle(staticPortletTitle
											+ Constant.SPACE_COLON_SPACE
											+ userExtendedRoleBean
													.getDefaultHostGroup());
								}
							}
						}
					}
				}

			} catch (PreferencesException e) {
				// ignore
			}
		}
	}

	/**
	 * Sets portlet title to "Service Group Dashboard portlets" for users with
	 * extended roles set.
	 * 
	 * @param request
	 * @param response
	 * @param staticPortletTitle
	 */
	public static void setServiceGroupDashboardPortletTitle(
			RenderRequest request, RenderResponse response,
			String staticPortletTitle) {
		if (!isInStatusViewer()) {
			try {
				PortletPreferences allPreferences = FacesUtils
						.getAllPreferences(request, false);
				if (null != allPreferences) {
					// if default SG preference is null or empty
					String defaultSGPref = allPreferences.getValue(
							PreferenceConstants.DEFAULT_SERVICE_GROUP_PREF,
							Constant.EMPTY_STRING);
					if (null == defaultSGPref
							|| Constant.EMPTY_STRING.equals(defaultSGPref)) {
						// check if custom portlet title is set
						String customPortletTitle = allPreferences.getValue(
								PreferenceConstants.CUSTOM_PORTLET_TITLE,
								Constant.EMPTY_STRING);
						if (null == customPortletTitle
								|| Constant.EMPTY_STRING
										.equals(customPortletTitle)) {
							/*
							 * now check for defaultServiceGroupPreference set
							 * in portlet.xml
							 */
							String sgPreferenceFromPortletXML = allPreferences
									.getValue(
											Constant.PORTLET_XML_DEFAULT_SERVICEGROUP_PREFERENCE,
											Constant.EMPTY_STRING);
							if (null != sgPreferenceFromPortletXML
									&& !Constant.EMPTY_STRING
											.equals(sgPreferenceFromPortletXML)) {
								// initialize UserExtendedRoleBean
								UserExtendedRoleBean userExtendedRoleBean = new UserExtendedRoleBean(
										getExtendedRoleAttributes());

								// get the extended role Service group list
								List<String> extRoleServiceGroupList = userExtendedRoleBean
										.getExtRoleServiceGroupList();
								if (!extRoleServiceGroupList.isEmpty()
										&& !extRoleServiceGroupList
												.contains(UserExtendedRoleBean.RESTRICTED_KEYWORD)
										&& !extRoleServiceGroupList
												.contains(sgPreferenceFromPortletXML)) {
									response.setTitle(staticPortletTitle
											+ Constant.SPACE_COLON_SPACE
											+ userExtendedRoleBean
													.getDefaultServiceGroup());
								}
							}
						}
					}
				}

			} catch (PreferencesException e) {
				// ignore
			}
		}
	}
}
