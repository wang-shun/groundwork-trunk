
package com.groundworkopensource.portal.common;

/**
 * EditPrefsBean used while editing preferences (currently in Service List
 * portlet).
 * 
 * @author swapnil_gujrathi
 * 
 */
public class EditPrefsBean {
    /**
     * preferenceKey
     */
    private String preferenceKey;
    /**
     * defaultPreferenceValue
     */
    private String defaultPreferenceValue;
    /**
     * requestAttributeName
     */
    private String requestAttributeName;
    /**
     * reqAttribute
     */
    private boolean reqAttribute;

    /**
     * populateEntityList
     */
    private boolean populateEntityList;

    /**
     * @param preferenceKey
     * @param defaultPreferenceValue
     * @param requestAttributeName
     * @param reqAttribute
     * @param populateEntityList
     */
    public EditPrefsBean(String preferenceKey, String defaultPreferenceValue,
            String requestAttributeName, boolean reqAttribute,
            boolean populateEntityList) {
        super();
        this.preferenceKey = preferenceKey;
        this.defaultPreferenceValue = defaultPreferenceValue;
        this.requestAttributeName = requestAttributeName;
        this.reqAttribute = reqAttribute;
        this.populateEntityList = populateEntityList;
    }

    /**
     * Returns the preferenceKey.
     * 
     * @return the preferenceKey
     */
    public String getPreferenceKey() {
        return preferenceKey;
    }

    /**
     * Sets the preferenceKey.
     * 
     * @param preferenceKey
     *            the preferenceKey to set
     */
    public void setPreferenceKey(String preferenceKey) {
        this.preferenceKey = preferenceKey;
    }

    /**
     * Returns the defaultPreferenceValue.
     * 
     * @return the defaultPreferenceValue
     */
    public String getDefaultPreferenceValue() {
        return defaultPreferenceValue;
    }

    /**
     * Sets the defaultPreferenceValue.
     * 
     * @param defaultPreferenceValue
     *            the defaultPreferenceValue to set
     */
    public void setDefaultPreferenceValue(String defaultPreferenceValue) {
        this.defaultPreferenceValue = defaultPreferenceValue;
    }

    /**
     * Returns the requestAttributeName.
     * 
     * @return the requestAttributeName
     */
    public String getRequestAttributeName() {
        return requestAttributeName;
    }

    /**
     * Sets the requestAttributeName.
     * 
     * @param requestAttributeName
     *            the requestAttributeName to set
     */
    public void setRequestAttributeName(String requestAttributeName) {
        this.requestAttributeName = requestAttributeName;
    }

    /**
     * Returns the reqAttribute.
     * 
     * @return the reqAttribute
     */
    public boolean isReqAttribute() {
        return reqAttribute;
    }

    /**
     * Sets the reqAttribute.
     * 
     * @param reqAttribute
     *            the reqAttribute to set
     */
    public void setReqAttribute(boolean reqAttribute) {
        this.reqAttribute = reqAttribute;
    }

    /**
     * Sets the populateEntityList.
     * 
     * @param populateEntityList
     *            the populateEntityList to set
     */
    public void setPopulateEntityList(boolean populateEntityList) {
        this.populateEntityList = populateEntityList;
    }

    /**
     * Returns the populateEntityList.
     * 
     * @return the populateEntityList
     */
    public boolean isPopulateEntityList() {
        return populateEntityList;
    }
}
