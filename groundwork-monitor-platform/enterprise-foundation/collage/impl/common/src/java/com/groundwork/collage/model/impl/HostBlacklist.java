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
import java.util.List;

/**
 * HostBlacklist
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostBlacklist extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.HostBlacklist {

    private static final long serialVersionUID = 1;

    /* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
     * defined are properties that can be used to query the entity.
     */
    private static final PropertyType PROP_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ID,
                    HP_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    HostBlacklist.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_HOST_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_HOST_NAME,
                    HP_HOST_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    HostBlacklist.ENTITY_TYPE_CODE,
                    true);

    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
    private static List<PropertyType> BUILT_IN_PROPERTIES = null;

    /** Unique auto generated id */
    private Integer hostBlacklistId;

    /** Host name, (required) */
    private String hostName;

    /**
     * Default persistence HostBlacklist constructor.
     */
    public HostBlacklist() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return HostBlacklist as String
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("hostBlacklistId", hostBlacklistId)
                .append("hostName", hostName)
                .toString();
    }

    /**
     * equals Object protocol implementation: HostBlacklist
     * instances considered equal if hostBlacklistIds match.
     *
     * @param other other object to compare
     * @return equals flag
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HostBlacklist)) {
            return false;
        }
        HostBlacklist castOther = (HostBlacklist)other;
        return new EqualsBuilder()
            .append(hostBlacklistId, castOther.hostBlacklistId)
            .isEquals();
    }

    /**
     * hashCode Object protocol implementation: HostBlacklist
     * instances considered equal if hostBlacklistIds match.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(hostBlacklistId)
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
            return ((getHostBlacklistId() != null) ? getHostBlacklistId().toString() : null);
        } else if (key.equalsIgnoreCase(EP_HOST_NAME)) {
            return getHostName();
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
            BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(2);
            BUILT_IN_PROPERTIES.add(PROP_ID);
            BUILT_IN_PROPERTIES.add(PROP_HOST_NAME);
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
        return new EntityPropertyValue(hostBlacklistId, getEntityTypeId(), name, value);
    }

    /* (non-Javadoc)
     * @see com.groundwork.collage.model.PropertyExtensible#getComponentProperties()
     */
    @Override
    public List<PropertyType> getComponentProperties() {
        return getBuiltInProperties();
    }

    @Override
    public Integer getHostBlacklistId() {
        return hostBlacklistId;
    }

    public void setHostBlacklistId(Integer hostBlacklistId) {
        this.hostBlacklistId = hostBlacklistId;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
