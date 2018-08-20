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

package com.groundworkopensource.portal.common.ws;

/**
 * This class defines constants to be used across projects.
 * 
 * @author rashmi_tambe
 */
public class Constants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected Constants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * "wshost" Web service.
     */
    public static final String FOUNDATION_END_POINT_HOST = "wshost";

    /**
     * "wshostgroup" Web service.
     */
    public static final String FOUNDATION_END_POINT_HOST_GROUP = "wshostgroup";

    /**
     * "wsservice" Web service.
     */
    public static final String FOUNDATION_END_POINT_SERVICE = "wsservice";

    /**
     * "wsstatistics" Web service.
     */
    public static final String FOUNDATION_END_POINT_STATISTICS = "wsstatistics";

    /**
     * "wsevent" web service.
     */
    public static final String FOUNDATION_END_POINT_EVENT = "wsevent";

    /**
     * "wscategory" Web service.
     */
    public static final String FOUNDATION_END_POINT_CATEGORY = "wscategory";

    /**
     * "wscommon" Web service.
     */
    public static final String FOUNDATION_END_POINT_COMMON = "wscommon";

    /**
     * "wsrrd" Web service.
     */
    public static final String FOUNDATION_END_POINT_RRD = "wsrrd";
    /**
     * key to look for in web.xml for application-level rendering interval. used
     * by ICEFaces interval renderer to update data on periodic basic.
     */
    public static final String RENDER_INTERVAL_KEY = "renderer.interval";
}
