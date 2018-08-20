//package simple.groundwork;
//
//import java.util.Map;
//
//import javax.faces.FactoryFinder;
//import javax.faces.application.Application;
//import javax.faces.application.ApplicationFactory;
//import javax.faces.application.FacesMessage;
//import javax.faces.component.UIViewRoot;
//import javax.faces.context.ExternalContext;
//import javax.faces.context.FacesContext;
//import javax.faces.context.FacesContextFactory;
//import javax.faces.el.ValueBinding;
//import javax.faces.lifecycle.Lifecycle;
//import javax.faces.lifecycle.LifecycleFactory;
//import javax.portlet.PortletPreferences;
//import javax.portlet.PortletRequest;
//import javax.portlet.PortletSession;
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//import org.apache.log4j.Logger;
//import org.gatein.pc.api.state.PropertyContext;
//import org.gatein.pc.api.state.PropertyMap;
//import org.gatein.pc.portlet.impl.info.ContainerPreferencesInfo;
//import org.gatein.pc.portlet.impl.jsr168.api.PortletPreferencesImpl;
//import org.gatein.pc.portlet.state.AbstractPropertyContext;
//
///**
// * JSF utilities.
// */
//public class FacesUtils {
//
//    /**
//     * Logger
//     */
//    private static final Logger LOGGER = Logger.getLogger(FacesUtils.class
//            .getName());
//
//    /**
//     * EMPTY_STRING
//     */
//    private static final String EMPTY_STRING = "";
//
//    /**
//     * FacesContext
//     */
//    private static FacesContext facesContext;
//
//    /**
//     * Protected Constructor - Rationale: Instantiating utility classes does not
//     * make sense. Hence the constructors should either be private or (if you
//     * want to allow sub-classing) protected. <br>
//     * 
//     * Refer to "HideUtilityClassConstructor" section in
//     * http://checkstyle.sourceforge.net/config_design.html.
//     */
//    protected FacesUtils() {
//        // prevents calls from subclass
//        throw new UnsupportedOperationException();
//    }
//
//    /**
//     * Get servlet context.
//     * 
//     * @return the servlet context
//     */
//    public static ServletContext getServletContext() {
//        return (ServletContext) FacesContext.getCurrentInstance()
//                .getExternalContext().getContext();
//    }
//
//    /**
//     * get External Context
//     * 
//     * @return ExternalContext
//     */
//    public static ExternalContext getExternalContext() {
//        FacesContext fc = getFacesContext();
//        return fc.getExternalContext();
//    }
//
//    /**
//     * get the http session
//     * 
//     * @param create
//     * @return HttpSession
//     */
//
//    public static HttpSession getHttpSession(boolean create) {
//        return (HttpSession) getFacesContext().getExternalContext().getSession(
//                create);
//    }
//
//    /**
//     * Get managed bean based on the bean name.
//     * 
//     * @param beanName
//     *            the bean name
//     * @return the managed bean associated with the bean name
//     */
//    public static Object getManagedBean(String beanName) {
//        return getValueBinding(getJsfEl(beanName)).getValue(getFacesContext());
//    }
//
//    /**
//     * Remove the managed bean based on the bean name.
//     * 
//     * @param beanName
//     *            the bean name of the managed bean to be removed
//     */
//    public static void resetManagedBean(String beanName) {
//        getValueBinding(getJsfEl(beanName)).setValue(getFacesContext(), null);
//    }
//
//    /**
//     * Store the managed bean inside the session scope.
//     * 
//     * @param beanName
//     *            the name of the managed bean to be stored
//     * @param managedBean
//     *            the managed bean to be stored
//     */
//    @SuppressWarnings("unchecked")
//    public static void setManagedBeanInSession(String beanName,
//            Object managedBean) {
//        getFacesContext().getExternalContext().getSessionMap().put(beanName,
//                managedBean);
//    }
//
//    /**
//     * Store the managed bean inside the request scope.
//     * 
//     * @param beanName
//     *            the name of the managed bean to be stored
//     * @param managedBean
//     *            the managed bean to be stored
//     */
//    @SuppressWarnings("unchecked")
//    public static void setManagedBeanInRequest(String beanName,
//            Object managedBean) {
//        getFacesContext().getExternalContext().getRequestMap().put(beanName,
//                managedBean);
//    }
//
//    /**
//     * Get parameter value from request scope.
//     * 
//     * @param name
//     *            the name of the parameter
//     * @return the parameter value
//     */
//    public static String getRequestParameter(String name) {
//        return getFacesContext().getExternalContext().getRequestParameterMap()
//                .get(name);
//    }
//
//    /**
//     * Gest parameter value from the the session scope.
//     * 
//     * @param name
//     *            name of the parameter
//     * @return the parameter value if any.
//     */
//    public static String getSessionParameter(String name) {
//        FacesContext context = getFacesContext();
//        HttpServletRequest myRequest = (HttpServletRequest) context
//                .getExternalContext().getRequest();
//        return myRequest.getParameter(name);
//    }
//
//    /**
//     * Get parameter value from the web.xml file
//     * 
//     * @param parameter
//     *            name to look up
//     * @return the value of the parameter
//     */
//    public static String getFacesParameter(String parameter) {
//        // Get the servlet context based on the faces context
//        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
//                .getExternalContext().getContext();
//
//        // Return the value read from the parameter
//        return sc.getInitParameter(parameter);
//    }
//
//    /**
//     * Add information message.
//     * 
//     * @param msg
//     *            the information message
//     */
//    public static void addInfoMessage(String msg) {
//        addInfoMessage(null, msg);
//    }
//
//    /**
//     * Add information message to a specific client.
//     * 
//     * @param clientId
//     *            the client id
//     * @param msg
//     *            the information message
//     */
//    public static void addInfoMessage(String clientId, String msg) {
//        FacesContext.getCurrentInstance().addMessage(clientId,
//                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
//    }
//
//    /**
//     * Add error message.
//     * 
//     * @param msg
//     *            the error message
//     */
//    public static void addErrorMessage(String msg) {
//        addErrorMessage(null, msg);
//    }
//
//    /**
//     * Add error message to a specific client.
//     * 
//     * @param clientId
//     *            the client id
//     * @param msg
//     *            the error message
//     */
//    public static void addErrorMessage(String clientId, String msg) {
//        FacesContext.getCurrentInstance().addMessage(clientId,
//                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
//    }
//
//    /**
//     * get the faces application object
//     * 
//     * @return Application
//     */
//    private static Application getApplication() {
//        ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder
//                .getFactory(FactoryFinder.APPLICATION_FACTORY);
//        return appFactory.getApplication();
//    }
//
//    /**
//     * 
//     * @param el
//     * @return ValueBinding
//     */
//
//    private static ValueBinding getValueBinding(String el) {
//        return getApplication().createValueBinding(el);
//    }
//
//    /**
//     * get the El expression of specified parameter value
//     * 
//     * @param value
//     * @returnString
//     */
//    private static String getJsfEl(String value) {
//        return "#{" + value + "}";
//    }
//
//    /**
//     * method used to get the context parameter value from the web.xml
//     * 
//     * @param paramname
//     * @return String
//     */
//    @SuppressWarnings("unchecked")
//    public static String getContextParam(String paramname) {
//        FacesContext fc = FacesContext.getCurrentInstance();
//        Map initParameterMap = fc.getExternalContext().getInitParameterMap();
//        String name = initParameterMap.get(paramname).toString();
//        return name;
//    }
//
//    /**
//     * Returns the portlet session from external context.
//     * 
//     * @param create
//     * @return PortletSession
//     */
//    public static PortletSession getPortletSession(boolean create) {
//        FacesContext context = getFacesContext();
//        ExternalContext externalContext = context.getExternalContext();
//        PortletSession portletSession = (PortletSession) externalContext
//                .getSession(create);
//        return portletSession;
//    }
//
//    /**
//     * This method returns preferences that is set by user.
//     * 
//     * @param preferenceKey
//     * @return reference string value
//     * @throws PreferencesException
//     */
//    public static String getPreference(String preferenceKey) {
//        PortletPreferences preferences = FacesUtils.getAllPreferences();
//        String prefValue = EMPTY_STRING;
//        if (preferences != null) {
//            prefValue = preferences.getValue(preferenceKey, EMPTY_STRING);
//        }
//        return prefValue;
//    }
//
//    /**
//     * This method returns all preferences (default + set by user).
//     * 
//     * @return reference string value
//     * @throws PreferencesException
//     */
//    public static PortletPreferences getAllPreferences() {
//        // get preferences from portlet request
//        FacesContext context = getFacesContext();
//        if (context != null && context.getExternalContext() != null) {
//            ExternalContext externalContext = context.getExternalContext();
//            PortletRequest request = (PortletRequest) externalContext
//                    .getRequest();
//            return FacesUtils.getAllPreferences(request, false);
//        } // end of if (context != null ...
//
//        // something went wrong, throw exception!!
//        return null;
//    }
//
//    /**
//     * This method returns all preferences (default + set by user).
//     * 
//     * @return reference string value
//     * @throws PreferencesException
//     */
//    public static PortletPreferences getAllPreferences(PortletRequest request, boolean isProcessingTitle) {
//        if (request != null) {
//            Object userPref = request
//                    .getAttribute(SampleInterceptor.USER_PREFS_ATTRIBUTE);
//            PropertyContext adminPref = (PropertyContext) request
//                    .getAttribute(SampleInterceptor.ADMIN_PREFS_ATTRIBUTE);
//            Object containerPref = request
//                    .getAttribute(SampleInterceptor.CONTAINER_PREFS_ATTRIBUTE);
//
//            String nameSpace = null;
//            String svNameSpace = null;
//            String mygroundworkNameSpace = null;
//            // If the portlet is placed in mygroundwork or not is determined in 2
//            // ways.For portlet title it is determined from windowID but in the
//            // content
//            // it is determined from the icefaces namespace. This is because
//            // doView() in corresponding portlet sets the title even before the
//            // namespace is set by the icefaces.
//            if (isProcessingTitle) {
//                nameSpace = request.getWindowID();
//                svNameSpace = "/groundwork-monitor/status/";
//                mygroundworkNameSpace = "dashboard:";
//            } else { // Use icefaces namespaces to identify dashboard
//                nameSpace = (String) request
//                        .getAttribute("com.icesoft.faces.NAMESPACE");
//                svNameSpace = "jbpns_2fgroundwork_2dmonitor_2fstatus";
//                mygroundworkNameSpace = "jbpnsdashboard";
//            }
//
//            // Fix for JIRA 8048.If not SV or mygroundwork, then take this order of
//            // preference. User, admin and default.For mygroundwork, User and
//            // default
//
//            if (nameSpace != null && !nameSpace.startsWith(svNameSpace) && !nameSpace.startsWith(mygroundworkNameSpace)) {
//                PortletPreferences prefs = null;
//                if (userPref != null) {
//					PropertyMap propMap = ((AbstractPropertyContext) userPref)
//                            .getPrefs();
//                    if (propMap == null) {
//                        prefs = new PortletPreferencesImpl(adminPref,
//                                (ContainerPreferencesInfo) containerPref, null,
//                                1);
//                    } // end if
//                    else {
//                        prefs = new PortletPreferencesImpl(
//                                (PropertyContext) userPref,
//                                (ContainerPreferencesInfo) containerPref, null,
//                                1);
//                    } // end if
//                } // end if
//                return prefs;
//            } else {
//                // PortletPreferences preferences = preferenceRequest.getPreferences();
//                // if (preferences != null && preferences.getMap() != null
//                // && !preferences.getMap().isEmpty()) {
//                // return all preferences
//                // return preferences;
//                // } // end if
//            }
//
//        } // end of if request !- null}
//
//        // something went wrong, throw exception!!
//        // throw new PreferencesException();
//        return null;
//    }
//
//    /**
//     * This method retrieves the currently logged in user.
//     * 
//     * @return user
//     */
//    public static String getLoggedInUser() {
//        String user = CommonConstants.EMPTY_STRING;
//        FacesContext context = FacesContext.getCurrentInstance();
//        if ((null != context) && (null != context.getExternalContext())) {
//            ExternalContext externalContext = context.getExternalContext();
//            PortletRequest request = (PortletRequest) externalContext
//                    .getRequest();
//            if (null != request) {
//                user = request.getRemoteUser();
//            }
//        }
//        return user;
//    }
//
//    /**
//     * Sets the facesContext.
//     * 
//     * @param faceContext
//     *            the facesContext to set
//     */
//    public static void setFacesContext(FacesContext faceContext) {
//        facesContext = faceContext;
//    }
//
//    /**
//     * Returns the facesContext.
//     * 
//     * @return the facesContext
//     */
//    public static FacesContext getFacesContext() {
//        if (null == FacesContext.getCurrentInstance()) {
//            return facesContext;
//        }
//        return FacesContext.getCurrentInstance();
//    }
//
//    /**
//     * @param request
//     * @param response
//     * @return Faces Context
//     */
//    public static FacesContext getFacesContext(HttpServletRequest request,
//            HttpServletResponse response) {
//        // Get current FacesContext.
//        FacesContext facesContextNew = FacesContext.getCurrentInstance(); // getFacesContext();
//
//        // Check current FacesContext.
//        if (facesContextNew == null) {
//            // LOGGER
//            // .error("!!!!!!!!!! ##### got null faces context. Creating new instance .... ");
//            // Create new Lifecycle.
//            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
//                    .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
//            Lifecycle lifecycle = lifecycleFactory
//                    .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
//
//            // Create new FacesContext.
//            FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder
//                    .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
//            facesContextNew = contextFactory.getFacesContext(request
//                    .getSession().getServletContext(), request, response,
//                    lifecycle);
//
//            // Create new View.
//            UIViewRoot view = facesContextNew.getApplication().getViewHandler()
//                    .createView(facesContextNew, "");
//            facesContextNew.setViewRoot(view);
//
//            // Set current FacesContext.
//            FacesContextWrapper.setCurrentInstance(facesContextNew);
//        }
//
//        return facesContextNew;
//    }
//
//    // Wrap the protected FacesContext.setCurrentInstance() in a inner class.
//    /**
//     * @author swapnil_gujrathi
//     * 
//     */
//    private abstract static class FacesContextWrapper extends FacesContext {
//        /**
//         * @param facesContext
//         */
//        protected static void setCurrentInstance(FacesContext facesContext) {
//            FacesContext.setCurrentInstance(facesContext);
//        }
//    }
//
//}