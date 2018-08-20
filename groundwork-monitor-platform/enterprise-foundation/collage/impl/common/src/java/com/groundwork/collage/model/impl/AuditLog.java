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
 * AuditLog
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuditLog extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.AuditLog {

    private static final long serialVersionUID = 1;

    /* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
     * defined are properties that can be used to query the entity.
     */
    private static final PropertyType PROP_ID =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ID,
                    HP_ID, // Description is hibernate property name
                    PropertyType.DataType.INTEGER,
                    AuditLog.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_SUBSYSTEM =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_SUBSYSTEM,
                    HP_SUBSYSTEM, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    AuditLog.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_ACTION =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_ACTION,
                    HP_ACTION, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    AuditLog.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_DESCRIPTION =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_DESCRIPTION,
                    HP_DESCRIPTION, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    AuditLog.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_USERNAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_USERNAME,
                    HP_USERNAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    AuditLog.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_TIMESTAMP =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_TIMESTAMP,
                    HP_TIMESTAMP, // Description is hibernate property name
                    PropertyType.DataType.DATE,
                    AuditLog.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_HOST_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_HOST_NAME,
                    HP_HOST_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Host.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_SERVICE_DESCRIPTION =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_SERVICE_DESCRIPTION,
                    HP_SERVICE_DESCRIPTION, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    ServiceStatus.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_HOST_GROUP_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_HOST_GROUP_NAME,
                    HP_HOST_GROUP_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    HostGroup.ENTITY_TYPE_CODE,
                    true);

    private static final PropertyType PROP_SERVICE_GROUP_NAME =
            new com.groundwork.collage.model.impl.PropertyType(
                    EP_SERVICE_GROUP_NAME,
                    HP_SERVICE_GROUP_NAME, // Description is hibernate property name
                    PropertyType.DataType.STRING,
                    Category.ENTITY_TYPE_CODE,
                    true);

    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
    private static List<PropertyType> BUILT_IN_PROPERTIES = null;

    /** Unique auto generated id number */
    private Integer auditLogId;

    /** Console, SV, Monarch, CloudHub, etc., (required) */
    private String subsystem;

    /** Action, (required) */
    private Action action;

    /** Description, (required) */
    private String description;

    /** Username, (required) */
    private String username;

    /** Timestamp, (required) */
    private Date timestamp = new Date();

    /** Host name, (optional) */
    private String hostName;

    /** Service description, (optional) */
    private String serviceDescription;

    /** Host group name, (optional) */
    private String hostGroupName;

    /** Service group name, (optional) */
    private String serviceGroupName;

    /**
     * Default persistence AuditLog constructor.
     */
    public AuditLog() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return AuditLog as String
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("auditLogId", auditLogId)
                .append("subsystem", subsystem)
                .append("action", action)
                .append("description", description)
                .append("username", username)
                .append("timestamp", timestamp)
                .append("hostName", hostName)
                .append("serviceDescription", serviceDescription)
                .append("hostGroupName", hostGroupName)
                .append("serviceGroupName", serviceGroupName)
                .toString();
    }

    /**
     * equals Object protocol implementation: AuditLog
     * instances considered equal if auditLogIds match.
     *
     * @param other other object to compare
     * @return equals flag
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuditLog)) {
            return false;
        }
        AuditLog castOther = (AuditLog)other;
        return new EqualsBuilder()
            .append(auditLogId, castOther.auditLogId)
            .isEquals();
    }

    /**
     * hashCode Object protocol implementation: AuditLog
     * instances considered equal if auditLogIds match.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(auditLogId)
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
            return getAuditLogId();
        } else if (key.equalsIgnoreCase(EP_SUBSYSTEM)) {
            return getSubsystem();
        } else if (key.equalsIgnoreCase(EP_ACTION)) {
            return ((getAction() != null) ? getAction().name() : null);
        } else if (key.equalsIgnoreCase(EP_DESCRIPTION)) {
            return getDescription();
        } else if (key.equalsIgnoreCase(EP_USERNAME)) {
            return getUsername();
        } else if (key.equalsIgnoreCase(EP_TIMESTAMP)) {
            return getTimestamp();
        } else if (key.equalsIgnoreCase(EP_HOST_NAME)) {
            return getHostName();
        } else if (key.equalsIgnoreCase(EP_SERVICE_DESCRIPTION)) {
            return getServiceDescription();
        } else if (key.equalsIgnoreCase(EP_HOST_GROUP_NAME)) {
            return getHostGroupName();
        } else if (key.equalsIgnoreCase(EP_SERVICE_GROUP_NAME)) {
            return getServiceGroupName();
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
            BUILT_IN_PROPERTIES.add(PROP_SUBSYSTEM);
            BUILT_IN_PROPERTIES.add(PROP_ACTION);
            BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);
            BUILT_IN_PROPERTIES.add(PROP_USERNAME);
            BUILT_IN_PROPERTIES.add(PROP_TIMESTAMP);
            BUILT_IN_PROPERTIES.add(PROP_HOST_NAME);
            BUILT_IN_PROPERTIES.add(PROP_SERVICE_DESCRIPTION);
            BUILT_IN_PROPERTIES.add(PROP_HOST_GROUP_NAME);
            BUILT_IN_PROPERTIES.add(PROP_SERVICE_GROUP_NAME);
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
        return new EntityPropertyValue(auditLogId, getEntityTypeId(), name, value);
    }

    /* (non-Javadoc)
     * @see com.groundwork.collage.model.PropertyExtensible#getComponentProperties()
     */
    @Override
    public List<PropertyType> getComponentProperties() {
        // Filterable properties are the same as the built-in properties
        return getBuiltInProperties();
    }

    @Override
    public Integer getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Integer auditLogId) {
        this.auditLogId = auditLogId;
    }

    @Override
    public String getSubsystem() {
        return subsystem;
    }

    @Override
    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String getServiceDescription() {
        return serviceDescription;
    }

    @Override
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    @Override
    public String getHostGroupName() {
        return hostGroupName;
    }

    @Override
    public void setHostGroupName(String hostGroupName) {
        this.hostGroupName = hostGroupName;
    }

    @Override
    public String getServiceGroupName() {
        return serviceGroupName;
    }

    @Override
    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }
}
