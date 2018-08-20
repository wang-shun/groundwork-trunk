package com.groundworkopensource.portal.common;

import java.io.Serializable;
import java.util.Locale;

/**
 * LocaleBean bean class for each portal application.
 * 
 * @author manish_kjain
 * 
 */
public class LocaleBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7366456710105833903L;

    /**
     * property file key
     */
    private static final String LOCALE_KEY = "locale";

    /**
     * Base Name
     */
    private String baseName;

    /**
     * locale
     */
    private String locale;
    
    private ApplicationType applicationType = null;

    /**
     * Constructor
     */
    public LocaleBean() {
        String appType = FacesUtils
                .getContextParam(CommonConstants.APPLICATION_TYPE_CONTEXT_PARAM_NAME);
        applicationType = ApplicationType
                .getApplicationType(appType);
        init();
    }
    
    /**
     * Constructor
     */
    public LocaleBean(ApplicationType applicationType) {
    	this.applicationType = applicationType;
    	init();
    }
    
    private void init() {
    	// read locale from application specific properties file
        locale = PropertyUtils.getProperty(applicationType, LOCALE_KEY);

        // if null/empty, assign Default locale
        if (locale == null || locale.equals(CommonConstants.EMPTY_STRING)) {
            // assign default system locale - default locale for this instance
            // of the Java Virtual Machine
            locale = Locale.getDefault().getLanguage();
        }
        baseName = applicationType.getResourceBundleName() + "_" + locale;
    }

    /**
     * @return String
     */
    public String getBaseName() {
        return baseName;
    }
    
    /**
     * @return ApplicationType
     */
    public ApplicationType getApplicationType() {
        return applicationType;
    }

    /**
     * @param strBaseName
     */
    public void setBaseName(String strBaseName) {
        this.baseName = strBaseName;
    }

    /**
     * @return String
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param strLocale
     */
    public void setLocale(String strLocale) {
        this.locale = strLocale;
    }

}
