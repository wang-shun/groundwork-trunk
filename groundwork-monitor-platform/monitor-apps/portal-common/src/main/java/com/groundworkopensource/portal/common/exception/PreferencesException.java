
package com.groundworkopensource.portal.common.exception;

import com.groundworkopensource.portal.common.ResourceUtils;

/**
 * This exception should be thrown whenever JMS related error occurs.
 * 
 * @author nitin_jadhav
 * 
 */
public class PreferencesException extends GWPortalGenericException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -996522756617287389L;
    /**
     * Localized error message to display on UI
     */
    private static String message = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_error_preferencesError");

    /**
     * constructor with empty arguments
     */
    public PreferencesException() {
        super(message);
    }

    /**
     * constructor with error message .
     * 
     * @param message
     */
    public PreferencesException(String message) {
        super(message);
    }

    /**
     * constructor with throwable error.
     * 
     * @param t
     */
    public PreferencesException(Throwable t) {
        super(t);
    }

    /**
     * constructor with error message and throwable error.
     * 
     * @param name
     * @param t
     */
    public PreferencesException(String name, Throwable t) {
        super(name, t);
    }
}
