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

package com.groundwork.collage.model.impl;

import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DeviceTemplateProfile
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class DeviceTemplateProfile extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.DeviceTemplateProfile {

    private static final long serialVersionUID = 1;

    /* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
     * defined are properties that can be used to query the entity.
     */
    private static final PropertyType PROP_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ID,
                    HP_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    DeviceTemplateProfile.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_DEVICE_IDENTIFICATION =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_DEVICE_IDENTIFICATION,
                    HP_DEVICE_IDENTIFICATION, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    DeviceTemplateProfile.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_DEVICE_DESCRIPTION =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_DEVICE_DESCRIPTION,
                    HP_DEVICE_DESCRIPTION, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    DeviceTemplateProfile.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_CACTI_HOST_TEMPLATE =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_CACTI_HOST_TEMPLATE,
                    HP_CACTI_HOST_TEMPLATE, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    DeviceTemplateProfile.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_MONARCH_HOST_PROFILE =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_MONARCH_HOST_PROFILE,
                    HP_MONARCH_HOST_PROFILE, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    DeviceTemplateProfile.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_TIMESTAMP =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_TIMESTAMP,
                    HP_TIMESTAMP, // Description is hibernate property name
                    PropertyType.DataType.DATE,
                    DeviceTemplateProfile.ENTITY_TYPE_CODE,
                    true);

    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
    private static List<PropertyType> BUILT_IN_PROPERTIES = null;

    /** Unique auto generated id number */
    private Integer deviceTemplateProfileId;

    /** Device identification or '*' wildcard for default, (required) */
    private String deviceIdentification;

    /** Device description */
    private String deviceDescription;

    /** Cacti host template */
    private String cactiHostTemplate;

    /** Monarch host profile */
    private String monarchHostProfile;

    /** Timestamp edited, (required) */
    private Date timestamp;

    /**
     * Default persistence DeviceTemplateProfile constructor.
     */
    public DeviceTemplateProfile() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return DeviceTemplateProfile as String
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("deviceTemplateProfileId", deviceTemplateProfileId)
                .append("deviceIdentification", deviceIdentification)
                .append("deviceDescription", deviceDescription)
                .append("cactiHostTemplate", cactiHostTemplate)
                .append("monarchHostProfile", monarchHostProfile)
                .append("timestamp", timestamp)
                .toString();
    }

    /**
     * equals Object protocol implementation: DeviceTemplateProfile
     * instances considered equal if deviceTemplateProfileIds match.
     *
     * @param other other object to compare
     * @return equals flag
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeviceTemplateProfile)) {
            return false;
        }
        DeviceTemplateProfile castOther = (DeviceTemplateProfile)other;
        return new EqualsBuilder()
                .append(deviceTemplateProfileId, castOther.deviceTemplateProfileId)
                .isEquals();
    }

    /**
     * hashCode Object protocol implementation: DeviceTemplateProfile
     * instances considered equal if deviceTemplateProfileIds match.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(deviceTemplateProfileId)
                .toHashCode();
    }

    /**
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to get the value of one
     * of the named property getters
     *
     * @param key property key
     * @return property value
     * @throws IllegalArgumentException
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    @Override
    public Object getProperty(String key) throws IllegalArgumentException {
        if ((key == null) || (key.length() == 0)) {
            throw new IllegalArgumentException("Invalid null / empty property key.");
        }
        if (key.equalsIgnoreCase(EP_ID)) {
            return getDeviceTemplateProfileId();
        } else if (key.equalsIgnoreCase(EP_DEVICE_IDENTIFICATION)) {
            return getDeviceIdentification();
        } else if (key.equalsIgnoreCase(EP_DEVICE_DESCRIPTION)) {
            return getDeviceDescription();
        } else if (key.equalsIgnoreCase(EP_CACTI_HOST_TEMPLATE)) {
            return getCactiHostTemplate();
        } else if (key.equalsIgnoreCase(EP_MONARCH_HOST_PROFILE)) {
            return getMonarchHostProfile();
        } else if (key.equalsIgnoreCase(EP_TIMESTAMP)) {
            return getTimestamp();
        } else {
            return super.getProperty(key);
        }
    }

    /* (non-Javadoc)
     * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getBuiltInProperties()
     */
    @Override
    public List<PropertyType> getBuiltInProperties() {
        if (BUILT_IN_PROPERTIES == null) {
            BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
            BUILT_IN_PROPERTIES.add(PROP_ID);
            BUILT_IN_PROPERTIES.add(PROP_DEVICE_IDENTIFICATION);
            BUILT_IN_PROPERTIES.add(PROP_DEVICE_DESCRIPTION);
            BUILT_IN_PROPERTIES.add(PROP_CACTI_HOST_TEMPLATE);
            BUILT_IN_PROPERTIES.add(PROP_MONARCH_HOST_PROFILE);
            BUILT_IN_PROPERTIES.add(PROP_TIMESTAMP);
        }
        return BUILT_IN_PROPERTIES;
    }

    /* (non-Javadoc)
     * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getEntityTypeCode()
     */
    @Override
    public String getEntityTypeCode() {
        return ENTITY_TYPE_CODE;
    }

    /* (non-Javadoc)
     * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getPropertyValueInstance(java.lang.String, java.lang.Object)
     */
    @Override
    public PropertyValue getPropertyValueInstance(String name, Object value) {
        return new EntityPropertyValue(deviceTemplateProfileId, getEntityTypeId(), name, value);
    }

    /* (non-Javadoc)
     * @see com.groundwork.collage.model.PropertyExtensible#getComponentProperties()
     */
    @Override
    public List<com.groundwork.collage.model.PropertyType> getComponentProperties() {
        // Filterable properties are the same as the built-in properties
        return getBuiltInProperties();
    }

    @Override
    public Integer getDeviceTemplateProfileId() {
        return deviceTemplateProfileId;
    }

    public void setDeviceTemplateProfileId(Integer deviceTemplateProfileId) {
        this.deviceTemplateProfileId = deviceTemplateProfileId;
    }

    @Override
    public String getDeviceIdentification() {
        return deviceIdentification;
    }

    @Override
    public void setDeviceIdentification(String deviceIdentification) {
        this.deviceIdentification = deviceIdentification;
    }

    @Override
    public String getDeviceDescription() {
        return deviceDescription;
    }

    @Override
    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    @Override
    public String getCactiHostTemplate() {
        return cactiHostTemplate;
    }

    @Override
    public void setCactiHostTemplate(String cactiHostTemplate) {
        this.cactiHostTemplate = cactiHostTemplate;
    }

    @Override
    public String getMonarchHostProfile() {
        return monarchHostProfile;
    }

    @Override
    public void setMonarchHostProfile(String monarchHostProfile) {
        this.monarchHostProfile = monarchHostProfile;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
