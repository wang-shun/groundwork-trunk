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
 * DtoSuggestion
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="suggestion")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoSuggestion {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String entityType;

    /**
     * Default constructor.
     */
    public DtoSuggestion() {
    }

    /**
     * Full constructor.
     *
     * @param name suggestion name
     * @param entityType suggestion entity type
     */
    public DtoSuggestion(String name, String entityType) {
        this.name = name;
        this.entityType = entityType;
    }

    /**
     * toString Object protocol implementation.
     *
     * @return Suggestions as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[name=%s,entityType=%s]",
                System.identityHashCode(this), name, entityType);
    }

    public String getName() {
        return name;
    }

    public String getEntityType() {
        return entityType;
    }
}
