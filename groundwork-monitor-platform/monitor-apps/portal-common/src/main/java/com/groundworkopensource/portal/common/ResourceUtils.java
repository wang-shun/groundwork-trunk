package com.groundworkopensource.portal.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.groundworkopensource.portal.common.FacesUtils;

import org.apache.log4j.Logger;

/**
 * @author manish_kjain
 * 
 */
public class ResourceUtils {

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */

    /**
     * EMPTY_STRING
     */
    private static final String EMPTY_STRING = "";

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ResourceUtils.class
            .getName());

    /**
     * constructor
     */
    protected ResourceUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * DEFAULT LOCALE BEAN
     */
    private static final String DEFAULT_LOCALE_BEAN = "localeBean";

    /**
     * Missing resource encountered exception
     */
    private static final String MISSING_RESOURCE_ENCOUNTERED_STRING = "Missing resource is encountered.";

    /**
     * default is message_LOCALE.properties file
     */
    private static ResourceBundle defaultResourceBundle;

    /**
     * application type
     */
    private static ApplicationType applicationType;
    /**
     * Locale.
     */
    private static LocaleBean locale = null;

    static {
        String actualLocale = CommonConstants.DEFAULT_ENGLISH_LOCALE;
        if (locale == null) {
        	Object obj = FacesUtils.getServletContext().getAttribute(DEFAULT_LOCALE_BEAN);
        	if (obj != null) {
        		locale = (LocaleBean)obj;
        	}
        }
        else {
        	locale = (LocaleBean) FacesUtils
                    .getManagedBean(DEFAULT_LOCALE_BEAN);
        }
        actualLocale = locale.getLocale();
        // TODO determine resource bundle as per the application type
        // read application type from web.xml
        /*String appType = FacesUtils
                .getContextParam(CommonConstants.APPLICATION_TYPE_CONTEXT_PARAM_NAME);
        applicationType = ApplicationType.getApplicationType(appType);*/

        /*
         * Try to load ResourceBundle from
         * "/usr/local/groundwork/config/resources/" path.
         * 
         * Use "doPrivileged block" to create URL class loader (see more here -
         * http://java.sun.com/j2se/1.5.0/docs/guide/security/doprivileged.html)
         */
        URLClassLoader classLoader = (URLClassLoader) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        try {
                            // privileged code goes here
                            URL[] myUrl = new URL[1];
                            URL url = new URL(
                                    CommonConstants.RESOURCE_BINDLE_PATH_URL);
                            myUrl[0] = url;
                            // create a class loader with URL path consisting of
                            // bundle
                            // files
                            URLClassLoader classLoader = new URLClassLoader(
                                    myUrl);
                            return classLoader;
                        } catch (MalformedURLException e) {
                            LOGGER
                                    .warn("Failed to load resource bundle from default path");
                            // Exception - so bundle may not be there at
                            // specified path.
                            // defaultResourceBundle = null;
                        }
                        // nothing to return
                        return null;
                    }
                });

        // load bundle using this class loader
        if (classLoader != null) {
            try {
                defaultResourceBundle = ResourceBundle.getBundle(
                        applicationType.getResourceBundleName(), new Locale(
                                actualLocale), classLoader);
            } catch (Exception e) {
                defaultResourceBundle = null;
            }
        }
        /*
         * FALLBACK - Loading Resource Bundle from WAR file: we are writing
         * following block outside the try-catch for a purpose: if it fails to
         * get bundle from predefined path then it will throw a exception. In
         * this case we will load default resource bundle. Also while loading if
         * ResourceBundle.getBundle() returns null, then also we load bundle
         * from Fallback method.
         */
        if (null == defaultResourceBundle) {
            // Try to load from WAR.
            defaultResourceBundle = ResourceBundle.getBundle(locale.getApplicationType()
                    .getResourceBundleName(), new Locale(actualLocale));
        }

    }

    /**
     * Method return the value of specified key in parameter.
     * 
     * @param key
     * @param locale
     * @param resourceName
     * @return String
     */
    public static String getLocalizedMessage(final String key,
            final String locale, final String resourceName) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceName,
                new Locale(locale));

        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException mre) {
            LOGGER.warn(MISSING_RESOURCE_ENCOUNTERED_STRING);
        } catch (NullPointerException e) {
            LOGGER.warn(MISSING_RESOURCE_ENCOUNTERED_STRING);
        }
        return EMPTY_STRING;

    }

    /**
     * Wrapper / convenience method for retrieving messages
     * 
     * Default method with MessageResources_#LOCALE#.properties resource file.
     * 
     * @param key
     * @return value associated with passed key
     */
    public static String getLocalizedMessage(final String key) {
        try {
            return defaultResourceBundle.getString(key);
        } catch (MissingResourceException mre) {
            LOGGER.warn(MISSING_RESOURCE_ENCOUNTERED_STRING);
        } catch (NullPointerException e) {
            LOGGER.warn(MISSING_RESOURCE_ENCOUNTERED_STRING);
        }
        return EMPTY_STRING;
    }
}
