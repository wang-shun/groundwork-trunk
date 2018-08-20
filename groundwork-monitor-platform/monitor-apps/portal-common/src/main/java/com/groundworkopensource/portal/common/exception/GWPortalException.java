
package com.groundworkopensource.portal.common.exception;

import com.groundworkopensource.portal.common.ResourceUtils;

/**
 * This exception is thrown whenever some undesirable event occurs, such as
 * resource is not found or portal session is found null.
 * 
 * @author nitin_jadhav
 * 
 */
public class GWPortalException extends GWPortalGenericException {

    /**
     * Serial Id
     */
    private static final long serialVersionUID = -6857387008135424304L;

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
                    .getLocalizedMessage("com_groundwork_portal_error_gwportalError");
        } catch (Exception e) {
            message = ERROR_MESSAGE;
        }
    }

    /**
     * constructor with empty arguments
     */
    public GWPortalException() {
        super(message);
    }

    /**
     * constructor with error message.
     * 
     * @param message
     */

    public GWPortalException(String message) {
        super(message);
    }

    /**
     * constructor with throwable error.
     * 
     * @param t
     */
    public GWPortalException(Throwable t) {
        super(t);
    }

    /**
     * constructor with error message and throwable error.
     * 
     * @param name
     * @param t
     */
    public GWPortalException(String name, Throwable t) {
        super(name, t);
    }

}
