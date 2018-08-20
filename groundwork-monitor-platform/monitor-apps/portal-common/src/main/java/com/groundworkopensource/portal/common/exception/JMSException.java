
package com.groundworkopensource.portal.common.exception;

import com.groundworkopensource.portal.common.ResourceUtils;

/**
 * This exception should be thrown whenever JMS related error occurs.
 * 
 * @author nitin_jadhav
 * 
 */
public class JMSException extends GWPortalGenericException {

    /**
     * Serial Id
     */
    private static final long serialVersionUID = -6632278501367061517L;

    /**
     * Localized error message to display on UI
     */
    private static String message = ResourceUtils
            .getLocalizedMessage("com_groundwork_portal_error_jmsError");

    /**
     * constructor with empty arguments
     */
    public JMSException() {
        super(message);
    }

    /**
     * constructor with error message .
     * 
     * @param message
     */
    public JMSException(String message) {
        super(message);
    }

    /**
     * constructor with throwable error.
     * 
     * @param t
     */
    public JMSException(Throwable t) {
        super(t);
    }

    /**
     * constructor with error message and throwable error.
     * 
     * @param name
     * @param t
     */
    public JMSException(String name, Throwable t) {
        super(name, t);
    }
}
