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

/**
 * Collage Property Type Constants: defines constants for each property type
 * (maps to 'propertytype' type table of GWCollageDB). These constants should be
 * used whenever Host and Service properties needs to be fetched from
 * PropertyType binding object.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class CollagePropertyTypeConstants {
    /**
     * ExecutionTime Property.
     */
    public static final String EXECUTION_TIME = "ExecutionTime";

    /**
     * StateType Property.
     */
    public static final String STATE_TYPE = "StateType";

    /**
     * MaxCheckAttempts Property - Max attempts configured.
     */
    public static final String MAX_CHECK_ATTEMPTS = "MaxAttempts";

    /**
     * CheckAttempts Property - Current attempt running check.
     */
    public static final String CURRENT_CHECK_ATTEMPTS = "CurrentAttempt";

    /**
     * NextCheckTime Property.
     */
    public static final String NEXT_CHECK_TIME = "NextCheckTime";

    /**
     * CurrentNotificationNumber Property.
     */
    public static final String CURRENT_NOTIFICATION_NUMBER = "CurrentNotificationNumber";

    /**
     * LastPluginOutput Property for retrieving Host Status value.
     */
    public static final String LAST_PLUGIN_OUTPUT_PROPERTY = "LastPluginOutput";

    /**
     * ScheduledDowntimeDepth Property.
     */
    public static final String SCHEDULE_DOWNTIME_DEPTH_PROPERTY = "ScheduledDowntimeDepth";

    /**
     * LastNotificationTime Property.
     */
    public static final String LAST_NOTIFICATION_TIME_PROPERTY = "LastNotificationTime";

    /**
     * isChecksEnabled Property.
     */
    public static final String ACTIVE_CHECKS_ENABLED_PROPERTY = "isChecksEnabled";

    /**
     * isPassiveChecksEnabled Property.
     */
    public static final String PASSIVE_CHECKS_ENABLED_PROPERTY = "isPassiveChecksEnabled";

    /**
     * Latency Property.
     */
    public static final String LATENCY_PROPERTY = "Latency";

    /**
     * PercentStateChange Property.
     */
    public static final String PERCENTAGE_STATE_CHANGE_PROPERTY = "PercentStateChange";

    /**
     * Active Checks
     */
    public static final String ACTIVE_CHECKS = "Active";

    /**
     * Passive Checks
     */
    public static final String PASSIVE_CHECKS = "Passive";

    /**
     * PROPERTY_VALUE_UNAVAILABLE.
     */
    public static final String PROPERTY_VALUE_UNAVAILABLE = "N/A";

    /**
     * Comments Dynamic Property
     */
    public static final String COMMENTS = "Comments";

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected CollagePropertyTypeConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }
}
