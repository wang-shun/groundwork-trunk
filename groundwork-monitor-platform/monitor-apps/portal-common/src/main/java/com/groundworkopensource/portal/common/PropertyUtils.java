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
package com.groundworkopensource.portal.common;

import org.apache.log4j.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletContext;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Utility methods for dealing with operations related to Property files.
 * 
 * @author manish jain
 */
public class PropertyUtils {

    /**
     * WEB-INF/classes path
     */
    private static final String WEB_INF_CLASSES_PATH = "/WEB-INF/classes/";

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected PropertyUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * Logger.
     */
    private static Logger logger = Logger.getLogger(PropertyUtils.class
            .getName());

    /**
     * status viewer properties.
     */
    private static Properties statusViewerProperties = null;
    /**
     * report viewer properties.
     */
    private static Properties reportViewerProperties = null;
    /**
     * console properties.
     */
    private static Properties consoleProperties = null;
    /**
     * dashboard properties.
     */
    private static Properties dashboardProperties = null;

    /**
     * loadPropertices method load the Property file from specified file path
     * and return the Properties
     * 
     * @param propfilepath
     * @return Properties
     * @throws IOException
     */
    public static Properties loadPropertices(String propfilepath)
            throws IOException {
        Properties appProps = new Properties();
        InputStream propstream = null;
        if (propfilepath != null) {
            // get the inputStream of Property file
            propstream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(propfilepath);
            // load the property in to object
            appProps.load(propstream);
            propstream.close();

        }
        return appProps;
    }

    /**
     * getProperticesFromContext returns the Properties from the portlet context
     * 
     * @param propertyAttribute
     * @return Properties
     */
    public static Properties getProperticesFromContext(String propertyAttribute) {
        Properties appProps = new Properties();
        ExternalContext exContext = FacesContext.getCurrentInstance()
                .getExternalContext();

        if (exContext != null) {
            Object context = exContext.getContext();

            if (context != null) {
                PortletContext portletContext = (PortletContext) exContext
                        .getContext();
                appProps = (Properties) portletContext
                        .getAttribute(propertyAttribute);

            } // end if
        } // end if
        return appProps;
    }

    /**
     * set the property object in to portlet context
     * 
     * @param propertyAttribute
     * @param porps
     */
    public static void setProperticesFromContext(String propertyAttribute,
            Properties porps) {
        ExternalContext exContext = FacesContext.getCurrentInstance()
                .getExternalContext();

        if (exContext != null) {
            Object context = exContext.getContext();
            if (context != null) {
                PortletContext portletContext = (PortletContext) exContext
                        .getContext();
                portletContext.setAttribute(propertyAttribute, porps);
            } // end if
        } // end if
    }

    /**
     * get the value of key from the property file
     * 
     * @param propFilePath
     * @param key
     * @return String
     * @throws IOException
     */
    public static String getPropertyFromFile(String propFilePath, String key)
            throws IOException {
        String value = null;
        if (propFilePath != null) {
            Properties porps = loadPropertices(propFilePath);
            if (porps != null) {
                value = porps.getProperty(key);
            }
        }
        return value;
    }

    /**
     * @param applicationType
     * @param propertyName
     * @return property
     */
    public static String getProperty(ApplicationType applicationType,
            String propertyName) {
        String value = null;
        switch (applicationType) {
            case STATUS_VIEWER:
                if (statusViewerProperties == null) {
                    statusViewerProperties = loadProperties(applicationType);
                }
                if (null != statusViewerProperties) {
                    value = statusViewerProperties.getProperty(propertyName);
                }
                break;
            case REPORT_VIEWER:
                if (reportViewerProperties == null) {
                    reportViewerProperties = loadProperties(applicationType);
                }
                if (null != reportViewerProperties) {
                    value = reportViewerProperties.getProperty(propertyName);
                }
                break;
            case EVENT_CONSOLE:
                if (consoleProperties == null) {
                    consoleProperties = loadProperties(applicationType);
                }
                if (null != consoleProperties) {
                    value = consoleProperties.getProperty(propertyName);
                }
                break;
            case DASHBOARD:
                if (dashboardProperties == null) {
                    dashboardProperties = loadProperties(applicationType);
                }
                if (null != dashboardProperties) {
                    value = dashboardProperties.getProperty(propertyName);
                }
                break;
            default:
                break;
        }

        return value;
    }

    /**
     * load properties as per the application type.
     * 
     * @param applicationType
     */
    private static Properties loadProperties(ApplicationType applicationType) {
    	PortletContext portletContext = getPortletContext();
        if (null != portletContext) {
        	Object propObj = portletContext.getAttribute(applicationType
                    .getContextAttributeName());
                    if (propObj != null)
                    	return (Properties) propObj;
                    else
                    	return loadPropertiesFromFilePath(applicationType.getDefaultPropertiesPath());          
        } // end of if
        return loadPropertiesFromFilePath(applicationType.getDefaultPropertiesPath());
    }

    /**
     * loads Fallback Properties.
     * 
     * @param applicationType
     * @param servletContext
     * @return Fallback Properties
     */
    public static Properties loadFallbackProperties(
            ApplicationType applicationType, ServletContext servletContext) {
        // load fallback properties file
        Properties fallbackProps = new Properties();
        if (null != servletContext) {
            InputStream fallbackIS = null;
            try {
                String propertiesFilePath = servletContext.getRealPath(applicationType.getFallbackPropertiesPath());
                File propertiesFile = new File(propertiesFilePath);
                if (propertiesFile.isFile() && propertiesFile.canRead()) {
                    fallbackIS = new FileInputStream(propertiesFile);
                } else if (propertiesFilePath.contains(WEB_INF_CLASSES_PATH)) {
                    String propertiesResourcePath =
                            propertiesFilePath.substring(propertiesFilePath.indexOf(WEB_INF_CLASSES_PATH)+
                                    WEB_INF_CLASSES_PATH.length()-1);
                    fallbackIS = PropertyUtils.class.getResourceAsStream(propertiesResourcePath);
                }
                if (fallbackIS == null) {
                    throw new FileNotFoundException(propertiesFilePath);
                }
                fallbackProps.load(fallbackIS);
            } catch (Exception e) {
                logger.debug("Unable to find fallback properties ["
                        + applicationType.getFallbackPropertiesPath()
                        + "]. Something really gone wrong with build process. Setting empty properties in context ...");
            } finally {
                try {
                    if (null != fallbackIS) {
                        fallbackIS.close();
                    }
                } catch (IOException ioe) {
                    logger.error("Unable to close the input stream for fallback properties file - " + ioe.getMessage());
                }
            }
        }
        return fallbackProps;
    }

    /**
     * loads Default Properties.
     * 
     * @param applicationType
     * @param servletContext
     * @return Default Properties
     */
    public static Properties loadDefaultProperties(
            ApplicationType applicationType, ServletContext servletContext) {
        // load default properties file
        Properties defaultProps = new Properties();
        if (null != servletContext) {
            defaultProps = loadPropertiesFromFilePath(applicationType
                    .getDefaultPropertiesPath());
        }
        return defaultProps;
    }

    /**
     * Loads properties from file path.
     * 
     * @param filePath
     * @return Properties
     */
    public static Properties loadPropertiesFromFilePath(String filePath) {
        InputStream defaultIS = null;
        Properties defaultProps = new Properties();
        try {
            File propertiesFile = new File(filePath);
            if (propertiesFile.isFile() && propertiesFile.canRead()) {
                defaultIS = new FileInputStream(propertiesFile);
            } else if (filePath.contains(WEB_INF_CLASSES_PATH)) {
                String propertiesResourcePath =
                        filePath.substring(filePath.indexOf(WEB_INF_CLASSES_PATH)+WEB_INF_CLASSES_PATH.length()-1);
                defaultIS = PropertyUtils.class.getResourceAsStream(propertiesResourcePath);
            }
            if (defaultIS == null) {
                throw new FileNotFoundException(filePath);
            }
            defaultProps.load(defaultIS);
        } catch (Exception e) {
            logger.debug("Unable to find properties file [" + filePath + "]");
        } finally {
            try {
                if (defaultIS != null) {
                    defaultIS.close();
                }
            } catch (IOException ioe) {
                logger.error("Unable to close the input stream for properties file ["
                        + filePath
                        + "]. Exception is - "
                        + ioe.getMessage());
            }
        }
        return defaultProps;
    }

    /**
     * Returns PortletContext.
     * 
     * @return PortletContext
     */
    private static PortletContext getPortletContext() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (null != facesContext) {
            ExternalContext exContext = facesContext.getExternalContext();
            if (exContext != null) {
                Object context = exContext.getContext();
                if (context != null) {
                    PortletContext portletContext = (PortletContext) exContext
                            .getContext();
                    return portletContext;
                }
            }
        }
        return null;
    }

    /**
     * Validates the if all properties in the fallback props are available in
     * the default config properties. It copies missing properties in default
     * one from fallback properties.
     * 
     * @param defaultProp
     * @param fallbackProp
     * @return true if all properties in the fallback props are available in the
     *         default config properties.
     */
    private static boolean validateKeys(Properties defaultProp,
            Properties fallbackProp) {
        boolean returnValue = true;
        Enumeration<Object> fallbackEnumerator = fallbackProp.keys();
        while (fallbackEnumerator.hasMoreElements()) {
            String str = (String) fallbackEnumerator.nextElement();
            /*
             * copy missing properties in default one from fallback
             * properties.
             */
            if (!defaultProp.containsKey(str)) {
                defaultProp.put(str, fallbackProp.getProperty(str));
            } // end if
        } // end while
        return returnValue;
    }

    /**
     * Loads properties into Servlet Context. This method carries out following
     * steps: <br>
     * 1) load fallback properties file<br>
     * 2) load default properties file<br>
     * 3) if default properties are null or empty, then load fallback properties
     * else validate default properties with fallback one. <br>
     * 
     * @param applicationType
     * @param servletContext
     */
    public static void loadPropertiesIntoServletContext(
            ApplicationType applicationType, ServletContext servletContext) {
        // load fallback properties file
        Properties fallbackProps = PropertyUtils.loadFallbackProperties(
                applicationType, servletContext);
        if (null == fallbackProps || fallbackProps.isEmpty()) {
            servletContext.setAttribute(applicationType
                    .getContextAttributeName(), fallbackProps);
            return;
        }

        // load default properties file
        Properties defaultProps = PropertyUtils.loadDefaultProperties(
                applicationType, servletContext);

        // if default are null or empty, then set fallback properties
        // if all things goes well, validate default properties with fallback
        // one
        if (defaultProps == null || defaultProps.isEmpty()
                || !PropertyUtils.validateKeys(defaultProps, fallbackProps)) {
            // load fallback properties
            servletContext.setAttribute(applicationType
                    .getContextAttributeName(), fallbackProps);
        } else {
            servletContext.setAttribute(applicationType
                    .getContextAttributeName(), defaultProps);
        }
    }
}
