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
package com.groundworkopensource.portal.statusviewer.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * This class contains common utility methods for input validations.
 * 
 * @author shivangi_walvekar
 * 
 */
public class ValidationUtils {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected ValidationUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    // /**
    // * LOGGER
    // */
    // private static final Logger LOGGER =
    // Logger.getLogger(ValidationUtils.class
    // .getName());

    /**
     * This method checks if the value is a numeric value or not.
     * 
     * @param value
     * @return true if the value is numeric, false otherwise.
     */
    public static boolean isNumeric(Object value) {
        if (value != null) {
            String inputString = (String) value;
            // if the inputString is non-empty,check for the expected format.
            if (!Constant.EMPTY_STRING.equals(inputString.trim())) {
                if (!isPatternMatching(inputString,
                        Constant.NUMERIC_VALUE_PATTERN)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method checks if the value is a valid text using the regular
     * expression for pattern.
     * 
     * @param value
     * @param pattern
     * @return true if the value is a valid text,false otherwise.
     */
    public static boolean isValidText(Object value, String pattern) {
        if (value != null) {
            String inputString = (String) value;
            // if the inputString is non-empty,check for the expected format.
            if (!Constant.EMPTY_STRING.equals(inputString.trim())) {
                if (!isPatternMatching(inputString, pattern)) {
                    return false;
                }
            }
        }
        return true;
    }

    // /**
    // * This method replaces the '&' with '&amp;'. This is required as SAX
    // * parsers does not understand '&'.
    // *
    // * @param value
    // * @return String after replacing '&' with '&amp;'
    // */
    // public static String replaceAmpersand(String value) {
    // String returnStr = Constant.EMPTY_STRING;
    // if (value != null) {
    // LOGGER.error("^&^&^&^&^&^&^&^&^&^& input for replacement = "
    // + value);
    // // if the inputString is non-empty,check for the occurrence of '&'.
    // if (!Constant.EMPTY_STRING.equals(value.trim())) {
    // if ((value.indexOf(Constant.AND_STRING) != -1)) {
    // LOGGER
    // .error("<><><<<><><><><><><><><><><><><><Got & at the index  = "
    // + (value.indexOf(Constant.AND_STRING)));
    // // Replace '&' with '&amp;'
    // returnStr = value.replaceAll(Constant.AND_STRING,
    // Constant.AMPERSAND);
    // } else {
    // returnStr = value;
    // }
    // }
    // }
    // LOGGER.error("$%$%$$%$%$%$%$%$%$%$%$%$%$%$% replaced string = "
    // + returnStr);
    // return returnStr;
    // }

    /**
     * This method checks if the input matches the pattern.
     * 
     * @param input
     * @param pattern
     * @return true if the input matches with the pattern,false otherwise.
     */
    public static boolean isPatternMatching(String input, String pattern) {
        if ((input != null) && (!Constant.EMPTY_STRING.equals(input.trim()))) {
            /* Create a pattern mask */
            Pattern mask = Pattern.compile(pattern);
            /* Check to ensure that the value is in valid data format */
            Matcher matcher = mask.matcher(input);
            if (!matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method checks if the entered value is within the range of 1 to 60
     * (Minutes value).
     * 
     * @param input
     * @return true if the input is within the range of 1 to 60,false otherwise.
     */
    public static boolean isValidMinutes(String input) {
        if ((input != null) && (!Constant.EMPTY_STRING.equals(input.trim()))) {
            int minutes = Integer.parseInt(input);
            if ((minutes <= Constant.ZERO)
                    || (minutes > Constant.MINUTES_MAX_VALUE)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method validates the component for non-empty value.
     * 
     * @param value
     * @return true if the the value is empty,false otherwise.
     */
    public static boolean checkForBlankValue(Object value) {
        if (value != null) {
            String inputString = (String) value;
            if (Constant.EMPTY_STRING.equals(inputString.trim())
                    || (inputString.trim().length() == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method validates date format.
     * 
     * @param value
     * @param format
     * @return true - if the date format matches the expected pattern, false
     *         otherwise.
     */
    public static boolean isValidDateFormat(Object value, String format) {
        if (value != null) {
            String inputString = (String) value;
            // if the inputString is non-empty,check for the expected format.
            if (!Constant.EMPTY_STRING.equals(inputString.trim())) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                try {
                    dateFormat.parse(inputString);
                    /*
                     * Check if the length of the input string > 19. (MM/dd/yyyy
                     * hh:mm:ss = 19 characters)
                     */
                    if (inputString.length() > Constant.NINETEEN) {
                        return false;
                    }
                } catch (ParseException e) {
                    return false;
                } catch (Exception ex) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method checks if the day,month,year,hours,minutes,seconds field of
     * calendar are in valid range.
     * 
     * @param dateString
     * @param format
     * @return true if the date is a valid date (i.e. any of the fields are not
     *         out-of-range.),false otherwise.
     */
    public static boolean validateDateTimeFields(String dateString,
            String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            dateFormat.setLenient(false);
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        try {
            calendar.setTime(date);
            // get the day value.
            calendar.get(Calendar.DATE);
            // get the month value.
            calendar.get(Calendar.MONTH);
            // get the year value.
            calendar.get(Calendar.YEAR);
            // get the hours value.
            calendar.get(Calendar.HOUR_OF_DAY);
            // get the minutes value.
            calendar.get(Calendar.MINUTE);
            // get the day value.
            calendar.get(Calendar.SECOND);
            /*
             * Set the values to the calendar fields. If the values are
             * out-of-range,then an exception will be thrown.
             */
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * This method checks if the value is > compareWithValue.
     * 
     * @param value
     * @param compareWithValue
     * @return true if the value < compareWithValue,false otherwise.
     */
    public static boolean isPastDate(Object value, String compareWithValue) {
        if (value != null) {
            String inputString = (String) value;
            // if the inputString is non-empty then check for max length.
            if (!Constant.EMPTY_STRING.equals(inputString.trim())) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        Constant.DATE_FORMAT_24_HR_CLK);
                try {
                    Date date = dateFormat.parse(inputString);
                    Date compareToDate = dateFormat.parse(compareWithValue);
                    if (date.before(compareToDate)) {
                        return true;
                    }
                } catch (ParseException e) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method checks if the value is > compareWithValue.
     * 
     * @param value
     * @param compareWithValue
     * @param dateFormat
     * @return true if the value < compareWithValue,false otherwise.
     */
    public static boolean isPastDate(Object value, String compareWithValue,
            SimpleDateFormat dateFormat) {
        if (value != null) {
            String inputString = (String) value;
            // if the inputString is non-empty then check for max length.
            if (!Constant.EMPTY_STRING.equals(inputString.trim())) {

                try {
                    Date date = dateFormat.parse(inputString);
                    Date compareToDate = dateFormat.parse(compareWithValue);
                    if (date.before(compareToDate)) {
                        return true;
                    }
                } catch (ParseException e) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 
     * Method to set detail & summary message, severity fields for Faces
     * Message.
     * 
     * @param detailMessage
     * @param summaryMessage
     * @param facesContext
     * @param component
     */
    public static void showMessage(String detailMessage, String summaryMessage,
            FacesContext facesContext, UIComponent component) {
        // Custom faces message
        FacesMessage message = new FacesMessage();
        // Set message details
        message.setDetail(detailMessage);
        message.setSummary(summaryMessage);
        // Set severity
        message.setSeverity(FacesMessage.SEVERITY_ERROR);
        facesContext.addMessage(component.getClientId(facesContext), message);
    }

    /**
     * This method clears the facesMessages for the component.
     * 
     * @param facesContext
     * @param clientId
     *            - clientId for the component whose facesMessages are to be
     *            cleared.
     */
    public static void clearFacesMessages(FacesContext facesContext,
            String clientId) {
        if (facesContext != null) {
            Iterator<FacesMessage> messages = facesContext
                    .getMessages(clientId);
            if (messages != null) {
                while (messages.hasNext()) {
                    messages.next();
                    messages.remove();
                }
            }
        }
    }

    /**
     * This method clears the All facesMessages .
     * 
     * @param facesContext
     */
    public static void clearAllFacesMessages(FacesContext facesContext) {
        if (facesContext != null) {
            Iterator<FacesMessage> messages = facesContext.getMessages();
            if (messages != null) {
                while (messages.hasNext()) {
                    messages.next();
                    messages.remove();
                }
            }
        }
    }
}
