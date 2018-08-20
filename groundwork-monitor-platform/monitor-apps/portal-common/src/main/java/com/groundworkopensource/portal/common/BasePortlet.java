package com.groundworkopensource.portal.common;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.icesoft.faces.webapp.http.portlet.MainPortlet;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A base class portlet to be extended by all portlets in application.
 * 
 * @author rashmi_tambe
 * 
 */
public abstract class BasePortlet extends MainPortlet {

	/**
	 * constant for - serviceList
	 */
	public static final String SERVICE_LIST = "serviceList";

	/**
	 * constant for - serviceGroupList
	 */
	public static final String SERVICE_GROUP_LIST = "serviceGroupList";

	/**
	 * constant for - hostList
	 */
	public static final String HOST_LIST = "hostList";

	/**
	 * constant for - hostGroupList
	 */
	public static final String HOST_GROUP_LIST = "hostGroupList";

	/**
	 * logger
	 */
	private static final Logger logger = Logger.getLogger(BasePortlet.class);

	/**
	 * path of JSP - used by request-dispatcher
	 */
	private String viewPath;

	/**
	 * path of JSP - used by request-dispatcher
	 */
	private String editPath;

	private static final String ERROR_IFACE = "/jsp/error.iface";

	/**
	 * Foundation WebService Instance from factory.
	 */
	private IWSFacade foundationWSFacade = new WebServiceFactory()
			.getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

	/**
	 * @param strViewPath
	 *            the viewPath to set
	 */
	public void setViewPath(String strViewPath) {
		this.viewPath = strViewPath;
	}
	

    /**
	 * Renders view.
	 * 
	 * @param request
	 * @param response
	 */
	@Override
	protected void doEdit(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		doInclude(request, response, editPath);
	}

	/**
	 * Renders view.
	 * 
	 * @param request
	 * @param response
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		Object extRoleList = null;
        if (request.getRemoteUser() != null) {
            try {
                extRoleList = FacesUtils.getExtendedRoles(request.getRemoteUser());
            } catch (Exception exc) {
                logger.error("Unable to lookup extended roles for "+request.getRemoteUser()+": "+exc.getMessage(), exc);
            }
        } else {
            logger.error("Remote user not found for request");
        }
		// Dont worry about the object type here
		if (extRoleList != null)
			doInclude(request, response, viewPath);
		else {
			request.setAttribute("errorMessage",
					FacesUtils.NO_EXTENDED_ROLES_ERROR);
			doInclude(request, response, ERROR_IFACE);
		}
	}

	/**
	 * @param editPath
	 */
	public void setEditPath(String editPath) {
		this.editPath = editPath;
	}

	/**
	 * (non-Javadoc).
	 * 
	 * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
	 *      javax.portlet.ActionResponse)
	 */

	protected void processAction(ActionRequest request,
			ActionResponse response, String reqParam, String prefKey)
			throws PortletException, IOException {
		logger.debug("In ProcessAction.......");

		Object prefObj = request.getParameter(reqParam);
		if (prefObj != null) {
			logger.debug("About to store Pref: ");
			String prefValue = (String) prefObj;
			PortletPreferences pref = request.getPreferences();
			pref.setValue(prefKey, prefValue);
			logger.debug("Before storing Pref: " + prefValue);
			pref.store();
			logger.debug("Preference stored succesfully..");
			response.setPortletMode(PortletMode.VIEW);
		}
	}

	/**
	 * Stores preference values by making use of request parameter passed.
	 * 
	 * @param request
	 * @param response
	 * @param reqPrefParamMap
	 *            - key should be "reqParam" and value should be associated
	 *            "prefKey"
	 * @throws PortletException
	 * @throws IOException
	 */
	protected void processAction(ActionRequest request,
			ActionResponse response, Map<String, String> reqPrefParamMap)
			throws PortletException, IOException {
		// get the portlet preferences
		PortletPreferences pref = request.getPreferences();
		// iterate through reqPrefParamMap to get reqParam and prefKey
		Set<Entry<String, String>> reqPrefParamMapEntrySet = reqPrefParamMap
				.entrySet();
		for (Entry<String, String> entry : reqPrefParamMapEntrySet) {
			// retrieve from request and set into preferences
			Object prefObj = request.getParameter(entry.getKey());
			if (prefObj != null) {
				pref.setValue(entry.getValue(), (String) prefObj);
			}
		}
		// store preferences
		pref.store();
		// set the portlet mode to VIEW
		response.setPortletMode(PortletMode.VIEW);
	}

	/**
	 * Common helper method to edit the preference page.
	 * 
	 * @param request
	 * @param response
	 * @param editPrefs
	 * @param viewId
	 * @throws PortletException
	 * @throws IOException
	 */
	protected void doEditPref(RenderRequest request, RenderResponse response,
			List<EditPrefsBean> editPrefs, String viewId)
			throws PortletException, IOException {
		if (editPrefs == null || editPrefs.isEmpty()) {
			throw new PortletException("Invalid Argument exception");
		} // end if

		// get preferences from request
		PortletPreferences pref = request.getPreferences();

		for (EditPrefsBean editPrefsBean : editPrefs) {
			String prefKey = editPrefsBean.getPreferenceKey();
			// populate the entity list
			// IMP DO NOT UN-COMMENT
			// if (editPrefsBean.isPopulateEntityList()) {
			// populateEntityList(prefKey, request);
			// }

			// set the request attribute
			if (editPrefsBean.isReqAttribute()) {
				String prefValue = pref.getValue(prefKey,
						editPrefsBean.getDefaultPreferenceValue());
				request.setAttribute(editPrefsBean.getRequestAttributeName(),
						prefValue);
			}
		}

		// get the request dispatcher using viewId
		PortletContext ctxt = getPortletContext();
		PortletRequestDispatcher disp = ctxt.getRequestDispatcher(viewId);
		response.setContentType("text/html");
		disp.include(request, response);
	}

	/**
	 * Common helper method to edit the preference page.
	 * 
	 * @param request
	 * @param response
	 * @param prefKey
	 * @param defaultPrefValue
	 * @param reqAttribute
	 * @param viewId
	 * @throws PortletException
	 * @throws IOException
	 */
	protected void doEditPref(RenderRequest request, RenderResponse response,
			String prefKey, String defaultPrefValue, String reqAttribute,
			String viewId) throws PortletException, IOException {
		if (prefKey == null || defaultPrefValue == null || reqAttribute == null
				|| viewId == null) {
			throw new PortletException("Invalid Argument exception");
		} // end if

		// IMP DO NOT UN-COMMENT
		// populateEntityList(prefKey, request);
		PortletPreferences pref = request.getPreferences();
		String prefValue = pref.getValue(prefKey, defaultPrefValue);
		request.setAttribute(reqAttribute, prefValue);
		PortletContext ctxt = getPortletContext();
		PortletRequestDispatcher disp = ctxt.getRequestDispatcher(viewId);
		response.setContentType("text/html");
		disp.include(request, response);
	}

	/**
	 * Populates entity list (Host / HostGroup / Service / ServiceGroup) as per
	 * the prefKey passed as a parameter. This method also sets the request
	 * attribute by generating entity list required for auto complete feature.
	 * 
	 * @param prefKey
	 * @param request
	 */
	private void populateEntityList(String prefKey, RenderRequest request) {

		if (prefKey.equalsIgnoreCase(CommonConstants.DEFAULT_HOSTGROUP_PREF)) {
			request.setAttribute(HOST_GROUP_LIST, getAllHostGroupNameList());

		} else if (prefKey.equalsIgnoreCase(CommonConstants.DEFAULT_HOST_PREF)) {
			request.setAttribute(HOST_LIST, getAllHostNameList());

		} else if (prefKey
				.equalsIgnoreCase(CommonConstants.DEFAULT_SERVICEGROUP_PREF)) {
			request.setAttribute(SERVICE_GROUP_LIST,
					getAllServiceGroupNameList());

		} else if (prefKey
				.equalsIgnoreCase(CommonConstants.DEFAULT_SERVICE_PREF)) {
			request.setAttribute(SERVICE_LIST, getAllServiceNameList());
		}
	}

	/**
	 * Return comma separated list of all service group names
	 * 
	 * @return comma separated list of all service group names
	 */
	public String getAllServiceGroupNameList() {
		Category[] serviceGroups = null;
		try {
			serviceGroups = foundationWSFacade.getAllServiceGroups();

		} catch (WSDataUnavailableException exc) {
			logger.error(exc.getMessage());
		} catch (GWPortalException exc) {
			logger.error(exc.getMessage());
		}
		StringBuilder serviceGroupNames = new StringBuilder();
		if (serviceGroups != null) {
			for (int i = 0; i < serviceGroups.length; i++) {
				serviceGroupNames.append(serviceGroups[i].getName());
				serviceGroupNames.append(CommonConstants.COMMA);
			}
		} // end if
		return serviceGroupNames.toString();
	}

	/**
	 * Return comma separated list of all host group names
	 * 
	 * @return comma separated list of all host group names
	 */
	public String getAllHostGroupNameList() {
		HostGroup[] hostGroups = null;
		try {
			hostGroups = foundationWSFacade.getAllHostGroups();

		} catch (WSDataUnavailableException exc) {
			logger.error(exc.getMessage());
		} catch (GWPortalException exc) {
			logger.error(exc.getMessage());
		}
		StringBuilder hostGroupNames = new StringBuilder();
		if (null != hostGroups) {
			for (int i = 0; i < hostGroups.length; i++) {
				hostGroupNames.append(hostGroups[i].getName());
				hostGroupNames.append(CommonConstants.COMMA);
			}
		}
		return hostGroupNames.toString();
	}

	/**
	 * Return comma separated all service name list
	 * 
	 * @return comma separated all service name list
	 */
	public String getAllServiceNameList() {
		ServiceStatus[] services = null;
		try {
			services = foundationWSFacade.getServices();
		} catch (WSDataUnavailableException exc) {
			logger.error(exc.getMessage());
		} catch (GWPortalException exc) {
			logger.error(exc.getMessage());
		}
		// Build Service String
		StringBuilder serviceNames = new StringBuilder();
		if (services != null) {
			for (int i = 0; i < services.length; i++) {
				serviceNames.append(services[i].getDescription());
				serviceNames.append(CommonConstants.COMMA);
			}
		}
		return serviceNames.toString();
	}

	/**
	 * Return comma separated all host name list
	 * 
	 * @return comma separated all host name list
	 */
	public String getAllHostNameList() {
		SimpleHost[] hosts = null;
		try {
			hosts = foundationWSFacade.getSimpleHosts();
		} catch (WSDataUnavailableException exc) {
			logger.error(exc.getMessage());
		} catch (GWPortalException exc) {
			logger.error(exc.getMessage());
		}
		// Build Host String
		StringBuilder hostNames = new StringBuilder();
		if (hosts != null) {
			for (int i = 0; i < hosts.length; i++) {
				hostNames.append(hosts[i].getName());
				hostNames.append(CommonConstants.COMMA);
			}
		}
		return hostNames.toString();
	}

    /**
     * Get HttpServletRequest from RenderRequest utility.
     *
     * @param request render request
     * @return HTTP servlet request
     */
    protected static HttpServletRequest getServletRequest(RenderRequest request) {
        try {
            return (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
        } catch (Exception e) {
            logger.error("Failed to retrieve portal servlet request: " + e.getMessage(), e);
        }
        return null;
    }
}
