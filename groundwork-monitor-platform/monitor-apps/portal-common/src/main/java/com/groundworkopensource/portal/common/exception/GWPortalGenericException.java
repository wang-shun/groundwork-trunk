
package com.groundworkopensource.portal.common.exception;

import com.groundworkopensource.portal.common.ResourceUtils;

/**
 * A generic exception to throw when error occurs in Portal system. Serves as a
 * base class for other fine-grained exceptions.
 * 
 * @author nitin_jadhav
 * 
 */
public class GWPortalGenericException extends Exception {

    /**
     * Serial Id
     */
    private static final long serialVersionUID = -4807075398374004310L;

    // TODO i18n ResourceUtils.getLocalizedMessage("generic_exception");
    /**
     * Localized error message to display on UI
     */
    private static String message;

    /**
     * Default error message to display on UI
     */
    private static final String ERROR_MESSAGE = "An error occurred. Please retry after some time.";

    /**
     * Static block to initialize exception message
     */
    static {
        try {
            message = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_error_genericError");
        } catch (Exception e) {
            message = ERROR_MESSAGE;
        }
    }

    /**
     * constructor with empty arguments
     */
    public GWPortalGenericException() {
        super(message);
    }

    /**
     * constructor with error message .
     * 
     * @param message
     */
    public GWPortalGenericException(String message) {
        super(message);
    }

    /**
     * constructor with throwable error.
     * 
     * @param t
     */
    public GWPortalGenericException(Throwable t) {
        super(t);
    }

    /**
     * constructor with error message and throwable error.
     * 
     * @param name
     * @param t
     */
    public GWPortalGenericException(String name, Throwable t) {
        super(name, t);
    }

}
