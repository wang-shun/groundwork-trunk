package com.groundworkopensource.portal.common;

import java.util.Map;
import java.io.InputStreamReader;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.groundworkopensource.portal.model.ExtendedRoleList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import com.groundworkopensource.portal.common.ws.impl.WebServiceLocator;
import org.apache.log4j.Logger;


import com.groundworkopensource.portal.common.exception.PreferencesException;

/**
 * JSF utilities.
 */
public class FacesUtils {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(FacesUtils.class
            .getName());

    /**
     * EMPTY_STRING
     */
    private static final String EMPTY_STRING = "";

    /**
     * FacesContext
     */
    private static FacesContext facesContext;
    
    public static final String CONTAINER_PREFS_ATTRIBUTE = "com.gwos.container_prefs";
	public static final String ADMIN_PREFS_ATTRIBUTE = "adminPref";
	public static final String USER_PREFS_ATTRIBUTE = "userPref";
	
	private static ServletContext servletContext = null; 
	
	public static final String NO_EXTENDED_ROLES_ERROR = "Unable to extract extended roles for user!";

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected FacesUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * Get servlet context.
     * 
     * @return the servlet context
     */
    public static ServletContext getServletContext() {
    	if (FacesUtils.servletContext != null)
    		return FacesUtils.servletContext;
    	else
    		return (ServletContext) FacesContext.getCurrentInstance()
    				.getExternalContext().getContext();
    }
    
    /**
     * Get servlet context.
     * 
     * @return the servlet context
     */
    public static ServletContext setServletContext(ServletContext servletContext) {
        return FacesUtils.servletContext = servletContext;
    }

    /**
     * get External Context
     * 
     * @return ExternalContext
     */
    public static ExternalContext getExternalContext() {
        FacesContext fc = getFacesContext();
        return fc.getExternalContext();
    }

    /**
     * get the http session
     * 
     * @param create
     * @return HttpSession
     */

    public static HttpSession getHttpSession(boolean create) {
        return (HttpSession) getFacesContext().getExternalContext().getSession(
                create);
    }

    /**
     * Get managed bean based on the bean name.
     * 
     * @param beanName
     *            the bean name
     * @return the managed bean associated with the bean name
     */
    public static Object getManagedBean(String beanName) {
        return getValueBinding(getJsfEl(beanName)).getValue(getFacesContext());
    }

    /**
     * Remove the managed bean based on the bean name.
     * 
     * @param beanName
     *            the bean name of the managed bean to be removed
     */
    public static void resetManagedBean(String beanName) {
        getValueBinding(getJsfEl(beanName)).setValue(getFacesContext(), null);
    }

    /**
     * Store the managed bean inside the session scope.
     * 
     * @param beanName
     *            the name of the managed bean to be stored
     * @param managedBean
     *            the managed bean to be stored
     */
    @SuppressWarnings("unchecked")
    public static void setManagedBeanInSession(String beanName,
            Object managedBean) {
        getFacesContext().getExternalContext().getSessionMap().put(beanName,
                managedBean);
    }

    /**
     * Store the managed bean inside the request scope.
     * 
     * @param beanName
     *            the name of the managed bean to be stored
     * @param managedBean
     *            the managed bean to be stored
     */
    @SuppressWarnings("unchecked")
    public static void setManagedBeanInRequest(String beanName,
            Object managedBean) {
        getFacesContext().getExternalContext().getRequestMap().put(beanName,
                managedBean);
    }

    /**
     * Get parameter value from request scope.
     * 
     * @param name
     *            the name of the parameter
     * @return the parameter value
     */
    public static String getRequestParameter(String name) {
        return getFacesContext().getExternalContext().getRequestParameterMap()
                .get(name);
    }

    /**
     * Gest parameter value from the the session scope.
     * 
     * @param name
     *            name of the parameter
     * @return the parameter value if any.
     */
    public static String getSessionParameter(String name) {
        FacesContext context = getFacesContext();
        HttpServletRequest myRequest = (HttpServletRequest) context
                .getExternalContext().getRequest();
        return myRequest.getParameter(name);
    }

    /**
     * Get parameter value from the web.xml file
     * 
     * @param parameter
     *            name to look up
     * @return the value of the parameter
     */
    public static String getFacesParameter(String parameter) {
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
    public static void addInfoMessage(String msg) {
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
    public static void addInfoMessage(String clientId, String msg) {
        FacesContext.getCurrentInstance().addMessage(clientId,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
    }

    /**
     * Add error message.
     * 
     * @param msg
     *            the error message
     */
    public static void addErrorMessage(String msg) {
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
    public static void addErrorMessage(String clientId, String msg) {
        FacesContext.getCurrentInstance().addMessage(clientId,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
    }

    /**
     * get the faces application object
     * 
     * @return Application
     */
    private static Application getApplication() {
        ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder
                .getFactory(FactoryFinder.APPLICATION_FACTORY);
        return appFactory.getApplication();
    }

    /**
     * 
     * @param el
     * @return ValueBinding
     */

    private static ValueBinding getValueBinding(String el) {
        return getApplication().createValueBinding(el);
    }

    /**
     * get the El expression of specified parameter value
     * 
     * @param value
     * @returnString
     */
    private static String getJsfEl(String value) {
        return "#{" + value + "}";
    }

    /**
     * method used to get the context parameter value from the web.xml
     * 
     * @param paramname
     * @return String
     */
    @SuppressWarnings("unchecked")
    public static String getContextParam(String paramname) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map initParameterMap = fc.getExternalContext().getInitParameterMap();
        String name = initParameterMap.get(paramname).toString();
        return name;
    }

    /**
     * Returns the portlet session from external context.
     * 
     * @param create
     * @return PortletSession
     */
    public static PortletSession getPortletSession(boolean create) {
        FacesContext context = getFacesContext();
        ExternalContext externalContext = context.getExternalContext();
        PortletSession portletSession = (PortletSession) externalContext
                .getSession(create);
        return portletSession;
    }

    /**
     * This method returns preferences that is set by user.
     * 
     * @param preferenceKey
     * @return reference string value
     * @throws PreferencesException
     */
    public static String getPreference(String preferenceKey)
            throws PreferencesException {
        PortletPreferences preferences = FacesUtils.getAllPreferences();
        String prefValue = EMPTY_STRING;
        if (preferences != null) {
            prefValue = preferences.getValue(preferenceKey, EMPTY_STRING);
        }
        return prefValue;
    }

    /**
     * This method returns all preferences (default + set by user).
     * 
     * @return reference string value
     * @throws PreferencesException
     */
    public static PortletPreferences getAllPreferences()
            throws PreferencesException {
        // get preferences from portlet request
        FacesContext context = getFacesContext();
        if (context != null && context.getExternalContext() != null) {
            ExternalContext externalContext = context.getExternalContext();
            PortletRequest request = (PortletRequest) externalContext
                    .getRequest();
            return request.getPreferences();
        } // end of if (context != null ...

        // something went wrong, throw exception!!
        return null;
    }
    
    /**
     * This method returns all preferences (default + set by user).
     * 
     * @return reference string value
     * @throws PreferencesException
     */
    public static PortletPreferences getAllPreferences(PortletRequest request, boolean isProcessingTitle)
            throws PreferencesException {
    	 return request.getPreferences();
    }


    /**
     * This method retrieves the currently logged in user.
     * 
     * @return user
     */
    public static String getLoggedInUser() {
        String user = CommonConstants.EMPTY_STRING;
        FacesContext context = FacesContext.getCurrentInstance();
        if ((null != context) && (null != context.getExternalContext())) {
            ExternalContext externalContext = context.getExternalContext();
            Object reqObj = externalContext.getRequest();
            if (reqObj instanceof HttpServletRequest) {
            	if (null != externalContext) {
            		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
                    user = request.getRemoteUser();
                }
            }
            if (reqObj instanceof PortletRequest) {
            	PortletRequest request = (PortletRequest) externalContext
            			.getRequest();
            	if (null != request) {
            		user = request.getRemoteUser();
            	}
            } // end if
        }
        return user;
    }

    /**
     * Sets the facesContext.
     * 
     * @param faceContext
     *            the facesContext to set
     */
    public static void setFacesContext(FacesContext faceContext) {
        facesContext = faceContext;
    }

    /**
     * Returns the facesContext.
     * 
     * @return the facesContext
     */
    public static FacesContext getFacesContext() {
        if (null == FacesContext.getCurrentInstance()) {
            return facesContext;
        }
        return FacesContext.getCurrentInstance();
    }

    /**
     * @param request
     * @param response
     * @return Faces Context
     */
    public static FacesContext getFacesContext(HttpServletRequest request,
            HttpServletResponse response) {
        // Get current FacesContext.
        FacesContext facesContextNew = FacesContext.getCurrentInstance(); // getFacesContext();

        // Check current FacesContext.
        if (facesContextNew == null) {
            // LOGGER
            // .error("!!!!!!!!!! ##### got null faces context. Creating new instance .... ");
            // Create new Lifecycle.
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
                    .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory
                    .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            // Create new FacesContext.
            FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder
                    .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            facesContextNew = contextFactory.getFacesContext(request
                    .getSession().getServletContext(), request, response,
                    lifecycle);

            // Create new View.
            UIViewRoot view = facesContextNew.getApplication().getViewHandler()
                    .createView(facesContextNew, "");
            facesContextNew.setViewRoot(view);

            // Set current FacesContext.
            FacesContextWrapper.setCurrentInstance(facesContextNew);
        }

        return facesContextNew;
    }
    
    /**
	 * returns all available extended role
	 * 
	 * @return Collection<CustomGroup>
	 * @throws WSDataUnavailableException
	 */

	public static ExtendedRoleList getExtendedRoles()
			throws Exception {
		return FacesUtils.getExtendedRoles(FacesUtils.getLoggedInUser());
	}
	
	public static ExtendedRoleList getExtendedRoles(String user)
			throws Exception {
		DefaultHttpClient httpClient = null;
		ExtendedRoleList roleList = null;
		HttpResponse response = null;
		
		StringBuilder builder = new StringBuilder();
		builder.append("userName");
		builder.append("=");
		builder.append(URLEncoder.encode(user));
		// Take base endpoint and append with path & path param
		String EXTENDED_ROLES_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + "extendedrole/findrolesbyuser?" + builder.toString();
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			HttpGet getRequest = new HttpGet(EXTENDED_ROLES_ENDPOINT);
			getRequest.addHeader("accept", "application/xml");
			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			JAXBContext context = JAXBContext
					.newInstance(ExtendedRoleList.class);
			Unmarshaller um = context.createUnmarshaller();
			roleList = (ExtendedRoleList) um
					.unmarshal(new StreamSource(new InputStreamReader((response
							.getEntity().getContent()))));
		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new Exception(exc.getMessage());
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return roleList;
	}


    // Wrap the protected FacesContext.setCurrentInstance() in a inner class.
    /**
     * @author swapnil_gujrathi
     * 
     */
    private abstract static class FacesContextWrapper extends FacesContext {
        /**
         * @param facesContext
         */
        protected static void setCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }

}