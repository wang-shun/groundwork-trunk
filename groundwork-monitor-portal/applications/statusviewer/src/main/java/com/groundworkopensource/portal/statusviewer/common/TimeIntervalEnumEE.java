package com.groundworkopensource.portal.statusviewer.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This enum defines the time intervals to be displayed in a dropdown on
 * Host/service Availability portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum TimeIntervalEnumEE {
    /**
     * Enum TODAY
     */
    TODAY("1", "Today", true, 0),

    /**
     * Enum LAST_24_HOURS
     */
    LAST_24_HOURS("24", "Last 24 Hours", true, -1),
    /**
     * Enum LAST_48_HOURS
     */
    LAST_48_HOURS("48", "Last 48 Hours", true, -2),

    /**
     * enum LAST_5_DAYS
     */
    LAST_5_DAYS("120", "Last 5 Days", true, -5),
    /**
     * enum LAST_7_DAYS
     */
    LAST_7_DAYS("168", "Last 7 Days", true, -7),
    /**
     * enum LAST_30_DAYS
     */
    LAST_30_DAYS("30", "Last 30 Days", false, -30),
    /**
     * enum LAST_90_DAYS
     */
    LAST_90_DAYS("90", "Last 90 Days", false, -90),
    /**
     * Custom DAYS
     */
    CUSTOM_DAYS("-1", "Custom Date-Time", false, -00);
    /**
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Value for the enum - If the enum indicates Hours,then value is set to the
     * number of hours,if the enum indicates days > 8,then the value is set to
     * the number of days.This is required to divide the user selected time into
     * 8 equal intervals.
     */
    private String value;
    /**
     * label for the enum to be displayed on UI.
     */
    private String label;
    /**
     * boolean flag indicating id the particular enum indicates the hours or
     * not.
     * 
     */
    private boolean isHours;

    /**
     * Value to add into the calendar object so as to compute actual date for
     * enum.
     */
    private int valueToAdd;

    /**
     * @return valueToAdd
     */
    public int getValueToAdd() {
        return valueToAdd;
    }

    /**
     * @param valueToAdd
     */
    public void setValueToAdd(int valueToAdd) {
        this.valueToAdd = valueToAdd;
    }

    /**
     * @return isHours
     */
    public boolean isHours() {
        return isHours;
    }

    /**
     * @param isHours
     */
    public void setHours(boolean isHours) {
        this.isHours = isHours;
    }

    /**
     * Constructor
     * 
     * @param value
     * @param label
     */
    private TimeIntervalEnumEE(String value, String label, boolean isHours,
            int valueToAdd) {
        this.value = value;
        this.label = label;
        this.isHours = isHours;
        this.valueToAdd = valueToAdd;
    }

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(TimeIntervalEnumEE.class
            .getName());

    /**
     * Map for retrieving parent menu status.
     */
    private static Map<String, TimeIntervalEnumEE> timeIntervalMap;
    // statically initialize entity-status map.
    static {
        timeIntervalMap = new HashMap<String, TimeIntervalEnumEE>() {
            /**
             * Serial Id
             */
            private static final long serialVersionUID = 1L;
            // Put All states in entity status map
            {
                TimeIntervalEnumEE[] timeIntervalArray = TimeIntervalEnumEE
                        .values();
                for (TimeIntervalEnumEE timeInterval : timeIntervalArray) {
                    put(timeInterval.getValue(), timeInterval);
                }
            }
        };
    } // end of static block

    /**
     * Returns TimeIntervalEnum from label string passed.
     * 
     * @param value
     * @return parentMenu
     */
    public static TimeIntervalEnumEE getTimeIntervalEnum(String value) {
        TimeIntervalEnumEE timeIntervalEnum = timeIntervalMap.get(value);
        if (timeIntervalEnum == null) {
            logger.debug("Unknown time interval encountered");
            // Returning TODAY menu as default. This case will never occur.
            return TimeIntervalEnumEE.TODAY;
        }
        return timeIntervalEnum;

    }
}
