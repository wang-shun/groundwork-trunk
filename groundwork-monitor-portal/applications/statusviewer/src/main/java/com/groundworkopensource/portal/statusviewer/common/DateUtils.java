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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;

/**
 * @author manish_kjain
 * 
 */
public class DateUtils {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected DateUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * LOGGER
     */
    private static final Logger LOGGER = Logger.getLogger(DateUtils.class
            .getName());

    /**
     * MINUTS_MILLISECONDS
     */
    private static final int MINUTS_MILLISECONDS = 60000;

    /**
     * HOURS_MILLISECONDS
     */
    private static final long HOURS_MILLISECONDS = 3600000L;

    /**
     * DAYS_MILLISECONDS
     */
    private static final long DAYS_MILLISECONDS = 86400000L;

    /**
     * MINS
     */
    private static final String MINS = " mins";

    /**
     * HOURS
     */
    private static final String HOURS = " hours, ";

    /**
     * DAYS
     */
    private static final String DAYS = " days, ";

    /**
     * Format a date/time into a specific pattern.
     * 
     * @param date
     *            the date to format expressed in milliseconds.
     * @param pattern
     *            the pattern to use to format the date.
     * @return the formatted date.
     */
    public static String format(Date date, String pattern) {
        DateFormat df = createDateFormat(pattern);
        return df.format(date);
    }

    /**
     * return a lenient date format set to GMT time zone.
     * 
     * @param pattern
     *            the pattern used for date/time formatting.
     * @return the configured format for this pattern.
     */
    private static DateFormat createDateFormat(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(true);
        return sdf;
    }

    /**
     * Method computes duration in Days-Hours-Minutes format
     * 
     * @param date
     * @return String representing duration
     */
    public static String computeDuration(Date date) {
        String durationString = "";
        if (date != null) {
            long diff = new Date().getTime() - date.getTime();
            int days = (int) (diff / DAYS_MILLISECONDS);
            diff = diff % DAYS_MILLISECONDS;
            int hours = (int) (diff / HOURS_MILLISECONDS);
            diff = diff % HOURS_MILLISECONDS;
            int minutes = (int) (diff / MINUTS_MILLISECONDS);
            durationString = days + DAYS + hours + HOURS + minutes + MINS;
        }
        return durationString;
    }

    /**
     * This method converts the number of hours,minutes into seconds.
     * 
     * @param strHours
     * @param strMinutes
     * 
     * @return seconds - total number of seconds
     * @throws GWPortalGenericException
     */
    public static long getSeconds(String strHours, String strMinutes)
            throws GWPortalGenericException {
        long seconds = 0;
        long hours = Long.parseLong(strHours);
        long minutes = Long.parseLong(strMinutes);
        if (hours < 0) {
            LOGGER.debug(Constant.INVALID_HOURS);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_zero_hours"));

        }
        if (minutes < 0) {
            LOGGER.debug(Constant.INVALID_MINUTES);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_minutes"));

        }
        seconds = (hours * Constant.SECONDS_MAX_VALUE * Constant.SECONDS_MAX_VALUE)
                + (minutes * Constant.SECONDS_MAX_VALUE);
        return seconds;
    }

    /**
     * This method returns number of seconds since January 1, 1970, 00:00:00 GMT
     * represented by strDate.
     * 
     * @param strDate
     * @return unixTime - number of seconds since January 1, 1970, 00:00:00 GMT.
     * @throws GWPortalGenericException
     */
    public static long getUnixTime(String strDate)
            throws GWPortalGenericException {
        long unixTime = 0;
        if (strDate == null || strDate.trim().equals(Constant.EMPTY_STRING)) {
            LOGGER.debug(Constant.INVALID_DATE_TIME);
            // TODO - shall we throw an exception or just return
            return 0;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constant.DATE_FORMAT_24_HR_CLK);
        try {
            Date date = dateFormat.parse(strDate);
            unixTime = (date.getTime()) / Constant.THOUSAND;
            // double d = Math.round(((date.getTime()) / 1000));
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_dateFormat"));

        }
        return unixTime;
    }

    /**
     * This method returns number of seconds since January 1, 1970, 00:00:00 GMT
     * till current time plus the minutes value passed.
     * 
     * @param minutes
     * @return unixTime
     * @throws GWPortalGenericException
     */
    public static long getUnixTime(int minutes) throws GWPortalGenericException {
        long unixTime = 0;
        if (minutes < 0) {
            LOGGER.error(Constant.INVALID_MINUTES);
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_invalid_minutes"));
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        Date date = cal.getTime();
        unixTime = date.getTime() / Constant.THOUSAND;
        return unixTime;
    }

}
