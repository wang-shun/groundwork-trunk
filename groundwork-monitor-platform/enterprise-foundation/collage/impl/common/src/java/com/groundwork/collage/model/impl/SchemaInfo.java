/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class SchemaInfo implements Serializable, com.groundwork.collage.model.SchemaInfo
{
    private static final long serialVersionUID = 1;

    /** identifier field */
    private String name;

    /** identifier field */
    private String value;

    /** full constructor */
    public SchemaInfo(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    /** default constructor */
    public SchemaInfo()
    {
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return this.value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
    
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof SchemaInfo) ) return false;
        SchemaInfo castOther = (SchemaInfo) other;
        return new EqualsBuilder()
            .append(this.getValue(), castOther.getName())
            .append(this.getValue(), castOther.getValue())
            .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(getValue())
            .toHashCode();
    }

    public String toString()
    {
        return new ToStringBuilder(this).append("name", getName()).append(
                "value", getValue()).toString();
    }

}
