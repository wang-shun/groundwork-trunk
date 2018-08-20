
package com.groundworkopensource.portal.licensing;

/**
 * Application Type - Status Viewer / Console / Dashboard.
 * 
 * @author swapnil_gujrathi
 * 
 */
public enum ApplicationType {

    /**
     * Status Viewer.
     */
    STATUS_VIEWER(CommonConstants.STATUS_VIEWER_PROP_PATH,
            CommonConstants.STATUS_VIEWER_PROP_FALLBACK_PATH,
            CommonConstants.STATUS_VIEWER_PROPS,
            CommonConstants.STATUS_VIEWER_RESOURCE_BUNDLE_NAME),
    /**
     * Console.
     */
    EVENT_CONSOLE(CommonConstants.CONSOLE_PROP_PATH,
            CommonConstants.CONSOLE_PROP_FALLBACK_PATH,
            CommonConstants.CONSOLE_PROPS, ""),
    /**
     * Report Viewer.
     */
    REPORT_VIEWER(CommonConstants.REPORT_VIEWER_PROP_PATH,
            CommonConstants.REPORT_VIEWER_PROP_FALLBACK_PATH,
            CommonConstants.REPORT_VIEWER_PROPS,
            CommonConstants.REPORT_VIEWER_RESOURCE_BUNDLE_NAME),
    /**
     * Dashboard. 
     */
    DASHBOARD(CommonConstants.DASHBOARD_PROP_PATH,
            CommonConstants.DASHBOARD_PROP_FALLBACK_PATH,
            CommonConstants.DASHBOARD_PROPS,
            CommonConstants.DASHBOARD_RESOURCE_BUNDLE_NAME);

    /**
     * Application Type constructor.
     */
    ApplicationType(String defaultPropsPath, String fallbackPropsPath,
            String contextAttrName, String resourceBundle) {
        this.defaultPropertiesPath = defaultPropsPath;
        this.fallbackPropertiesPath = fallbackPropsPath;
        this.contextAttributeName = contextAttrName;
        this.resourceBundleName = resourceBundle;
    }

    /**
     * Returns the defaultPropertiesPath.
     * 
     * @return the defaultPropertiesPath
     */
    public String getDefaultPropertiesPath() {
        return defaultPropertiesPath;
    }

    /**
     * Returns the fallbackPropertiesPath.
     * 
     * @return the fallbackPropertiesPath
     */
    public String getFallbackPropertiesPath() {
        return fallbackPropertiesPath;
    }

    /**
     * Returns the contextAttributeName.
     * 
     * @return the contextAttributeName
     */
    public String getContextAttributeName() {
        return contextAttributeName;
    }

    /**
     * Returns the resourceBundleName.
     * 
     * @return the resourceBundleName
     */
    public String getResourceBundleName() {
        return resourceBundleName;
    }

    /**
     * Returns actual ApplicationType enum instance from applicationType string.
     * 
     * @param applicationType
     * @return ApplicationType
     */
    public static ApplicationType getApplicationType(String applicationType) {
        if (null == applicationType
                || applicationType
                        .equalsIgnoreCase(CommonConstants.STATUS_VIEWER_APPLICATION)) {
            // if null then return Status Viewer
            return STATUS_VIEWER;
        } else if (applicationType
                .equalsIgnoreCase(CommonConstants.REPORT_VIEWER_APPLICATION)) {
            return REPORT_VIEWER;
        } else if (applicationType
                .equalsIgnoreCase(CommonConstants.EVENT_CONSOLE_APPLICATION)) {
            return EVENT_CONSOLE;
        } else {
            return DASHBOARD;
        }
    }

    /**
     * defaultPropertiesPath.
     */
    private String defaultPropertiesPath;
    /**
     * fallbackPropertiesPath.
     */
    private String fallbackPropertiesPath;
    /**
     * contextAttributeName.
     */
    private String contextAttributeName;
    /**
     * name of resource bundle.
     */
    private String resourceBundleName;
}
