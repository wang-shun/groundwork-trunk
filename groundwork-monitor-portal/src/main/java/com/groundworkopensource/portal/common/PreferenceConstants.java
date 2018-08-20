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

package com.groundworkopensource.portal.common;


/**
 * @author nitin_jadhav
 * 
 *         This class contains constants, that are used for accessing
 *         preferences through FacesUtils.getPreference() API
 */
public class PreferenceConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected PreferenceConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * HOST_GROUP_NAME Constant
     */
    public static final String HOST_GROUP_NAME = "hostgroupname";

    /**
     * HOST NAME Constant
     */
    public static final String HOST_NAME = "hostname";

    /**
     * SERVICE_GROUP_NAME Constant
     */
    public static final String SERVICE_GROUP_NAME = "servicegroupname";

    /**
     * SERVICE_NAME Constant
     */
    public static final String SERVICE_NAME = "servicename";

    /**
     * HOST ID Constant
     */
    public static final String HOST_ID = "hostid";

    /**
     * SERVICE ID Constant
     */
    public static final String SERVICE_ID = "serviceid";

    /**
     * SERVICE GROUP ID Constant
     */
    public static final String SERVICE_GROUP_ID = "servicegroupid";

    /**
     * DEFAULT_HOST_GROUP_PREF Constant
     */
    public static final String DEFAULT_HOST_GROUP_PREF = "defaultHostGroupPref";

    /**
     * DEFAULT_HOST_PREF Constant
     */
    public static final String DEFAULT_HOST_PREF = "defaultHostPref";

    /**
     * DEFAULT_SERVICE_GROUP_PREF Constant
     */
    public static final String DEFAULT_SERVICE_GROUP_PREF = "defaultServiceGroupPref";

    /**
     * DEFAULT_SERVICE_PREF Constant
     */
    public static final String DEFAULT_SERVICE_PREF = "defaultServicePref";

    /**
     * CUSTOM_PORTLET_TITLE Constant
     */
    public static final String CUSTOM_PORTLET_TITLE = "customPortletTitle";

}
