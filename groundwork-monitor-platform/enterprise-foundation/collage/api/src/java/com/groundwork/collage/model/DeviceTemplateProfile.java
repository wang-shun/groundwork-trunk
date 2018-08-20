/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.model;

import java.util.Date;

/**
 * DeviceTemplateProfile
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface DeviceTemplateProfile extends PropertyExtensible {

    /** Entity type */
    static final String ENTITY_TYPE_CODE = "DeviceTemplateProfile";

    /** Spring bean interface id */
    static final String INTERFACE_NAME = "com.groundwork.collage.model.DeviceTemplateProfile";

    /** Hibernate component name that this entity service using */
    static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.DeviceTemplateProfile";

    /* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
     * defined are properties that can be used to query the entity.
     */
    static final String HP_ID = "deviceTemplateProfileId";
    static final String HP_DEVICE_IDENTIFICATION = "deviceIdentification";
    static final String HP_DEVICE_DESCRIPTION = "deviceDescription";
    static final String HP_CACTI_HOST_TEMPLATE = "catctiHostTemplate";
    static final String HP_MONARCH_HOST_PROFILE = "monarchHostTemplate";
    static final String HP_TIMESTAMP = "timestamp";

    /** Entity Property Constants */
    static final String EP_ID = "DeviceTemplateProfileId";
    static final String EP_DEVICE_IDENTIFICATION = "DeviceIdentification";
    static final String EP_DEVICE_DESCRIPTION = "DeviceDescription";
    static final String EP_CACTI_HOST_TEMPLATE = "CactiHostTemplate";
    static final String EP_MONARCH_HOST_PROFILE = "MonarchHostProfile";
    static final String EP_TIMESTAMP = "Timestamp";

    Integer getDeviceTemplateProfileId();

    String getDeviceIdentification();
    void setDeviceIdentification(String deviceIdentification);

    String getDeviceDescription();
    void setDeviceDescription(String deviceDescription);

    String getCactiHostTemplate();
    void setCactiHostTemplate(String cactiHostTemplate);

    String getMonarchHostProfile();
    void setMonarchHostProfile(String monarchHostProfile);

    Date getTimestamp();
    void setTimestamp(Date timestamp);
}
