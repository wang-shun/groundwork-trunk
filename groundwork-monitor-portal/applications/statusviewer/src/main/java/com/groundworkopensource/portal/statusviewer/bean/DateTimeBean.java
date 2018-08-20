package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * Managed bean for the DateTimeBean portlet.
 * 
 * @author shivangi_walvekar
 */
public class DateTimeBean extends ServerPush implements Serializable {

    /** serialVersionID. */
    private static final long serialVersionUID = 5268831597075364642L;

    /** LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DateTimeBean.class
            .getName());

    /** Time interval, in milliseconds, between renders. */
    private static final long RENDERING_INTERVAL = 60000;

    /** Boolean to indicate if error has occurred. */
    private boolean error = false;

    /** Error message to be shown on UI,in case of errors/exceptions. */
    private String errorMessage;

    /** Current date-time. */
    private String currentTime;

    /** Local object for the logged in user. */
    private Locale userLocale;

    /** SimpleDateFormat. */
    private SimpleDateFormat dateFormat;

    /**
     * Gets the date format.
     * 
     * @return dateFormat
     */
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the date format.
     * 
     * @param dateFormat
     *            the date format
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Gets the user locale.
     * 
     * @return userLocale
     */
    public Locale getUserLocale() {
        return userLocale;
    }

    /**
     * Sets the user locale.
     * 
     * @param userLocale
     *            the user locale
     */
    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
    }

    /**
     * Gets the current time.
     * 
     * @return currentTime
     */
    public String getCurrentTime() {
        return currentTime;
    }

    /**
     * Sets the current time.
     * 
     * @param currentTime
     *            the current time
     */
    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * Checks if is error.
     * 
     * @return error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets the error.
     * 
     * @param error
     *            the error
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Gets the error message.
     * 
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     * 
     * @param errorMessage
     *            the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** hidden filed used to re-render the bean. */
    private String hiddenField = Constant.HIDDEN;

    /**
     * Gets the hidden field.
     * 
     * @return the hidden field
     */
    public String getHiddenField() {
        if (isIntervalRender()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("######## Current user Locale = "
                        + userLocale.getDisplayName());
            }
            currentTime = dateFormat.format(Calendar.getInstance(userLocale)
                    .getTime());
        }
        setIntervalRender(false);
        return hiddenField;
    }

    /**
     * Sets the hidden field.
     * 
     * @param hiddenField
     *            the hidden field
     */
    public void setHiddenField(String hiddenField) {
        this.hiddenField = hiddenField;
    }

    /**
     * Constructor.
     */
    public DateTimeBean() {
        super(RENDERING_INTERVAL);
        FacesContext context = FacesUtils.getFacesContext();
        if (context != null) {
            // Get the locale.
            userLocale = context.getExternalContext().getRequestLocale();
            dateFormat = new SimpleDateFormat(
                    Constant.DATE_FORMAT_FOR_DATETIME_PORTLET, userLocale);
        }
    }

    /**
     * (non-Javadoc).
     * 
     * @param xmlMessage
     *            the xml message
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlMessage) {
        // TODO Auto-generated method stub

    }
}
