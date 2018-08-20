
package com.groundworkopensource.portal.common.exception;

import com.groundworkopensource.portal.common.ResourceUtils;


/**
 * This exception should be thrown whenever web service data becomes
 * unavailable.
 * 
 * @author nitin_jadhav
 * 
 */
public class WSDataUnavailableException extends GWPortalGenericException {

    /**
     * serial Id
     */
    private static final long serialVersionUID = 4041885676665433920L;

   /**
     * Localized error message to display on UI
     */
    private static String message;

    /**
     * Default error message to display on UI
     */
    private static final String ERROR_MESSAGE = "Web Services are unavailable. Please retry after some time.";

    /**
     * Static block to initialize exception message
     */
    static {
        try {
            message = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_error_dataUnavailableError");
        } catch (Exception e) {
            message = ERROR_MESSAGE;
        }
    }

    /**
     * constructor with empty arguments
     */
    public WSDataUnavailableException() {
        super(message);
    }

    /**
     * constructor with error message .
     * 
     * @param name
     */
    public WSDataUnavailableException(String name) {
        super(name);
    }

    /**
     * constructor with throwable error.
     * 
     * @param t
     */
    public WSDataUnavailableException(Throwable t) {
        super(t);
    }

    /**
     * constructor with error message and throwable error.
     * 
     * @param name
     * @param t
     */
    public WSDataUnavailableException(String name, Throwable t) {
        super(name, t);
    }
}
