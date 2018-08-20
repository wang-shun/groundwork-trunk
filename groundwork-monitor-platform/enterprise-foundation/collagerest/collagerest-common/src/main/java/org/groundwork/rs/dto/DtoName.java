/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DtoName
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="name")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoName {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String canonicalName;

    /**
     * Default constructor.
     */
    public DtoName() {
    }

    /**
     * Name constructor.
     */
    public DtoName(String name) {
        this.name = name;
    }

    /**
     * Alias and canonical names constructor.
     */
    public DtoName(String name, String canonicalName) {
        this(name);
        this.canonicalName = canonicalName;
    }

    /**
     * equals Object protocol implementation.
     *
     * @param other other to test
     * @return equals
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof DtoName) {
            DtoName otherName = (DtoName)other;
            return (name.equals(otherName.name) &&
                    (((canonicalName != null) && canonicalName.equals(otherName.canonicalName)) ||
                            ((canonicalName == null) && (otherName.canonicalName == null))));
        }
        return false;
    }

    /**
     * hashCode Object protocol implementation.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return name.hashCode() ^ ((canonicalName != null) ? canonicalName.hashCode() : 0);
    }

    /**
     * toString Object protocol implementation.
     *
     * @return this as string
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[name=%s,canonicalName=%s]",
                System.identityHashCode(this), name, canonicalName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }
}
