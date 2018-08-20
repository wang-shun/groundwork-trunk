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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.UUID;

/**
 * UUIDEntityPropertyValue
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class UUIDEntityPropertyValue extends PropertyValue implements Serializable, com.groundwork.collage.model.UUIDEntityPropertyValue{

    private static final long serialVersionUID = 1;

    /** object identifier field */
    private UUID objectId;

    /** entity type id */
    private Integer entityTypeId;

    /**
     * Property value constructor.
     *
     * @param objectId object identifier
     * @param name property name
     * @param value property value
     */
    public UUIDEntityPropertyValue(UUID objectId, String name, Object value) {
        super(name, value);
        this.objectId = objectId;
    }

    /**
     * Default persistence constructor.
     */
    public UUIDEntityPropertyValue() {
    }

    /**
     * Object equals protocol. Two property value objects are equal if they have
     * the same name, compared in a case-insensitive way and related to the same
     * owner id and type.
     *
     * @param other other to compare against this
     * @return equals boolean flag
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UUIDEntityPropertyValue)) {
            return false;
        }
        UUIDEntityPropertyValue castOther = (UUIDEntityPropertyValue)other;
        return new EqualsBuilder()
                .append(this.getObjectId(), castOther.getObjectId())
                .append(this.getEntityTypeId(), castOther.getEntityTypeId())
                .append(this.getName().toLowerCase(), castOther.getName().toLowerCase())
                .isEquals();
    }

    /**
     * Object hashCode protocol. Two property value objects are equal if they have
     * the same name, compared in a case-insensitive way and related to the same
     * owner id and type.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getObjectId())
                .append(getEntityTypeId())
                .append(getName().toLowerCase())
                .toHashCode();
    }

    @Override
    public UUID getObjectId() {
        return objectId;
    }

    @Override
    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    @Override
    public Integer getEntityTypeId() {
        return entityTypeId;
    }

    @Override
    public void setEntityTypeId(Integer entityTypeId) {
        this.entityTypeId = entityTypeId;
    }
}
