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

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoCustomGroupUpdateList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="customGroups")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCustomGroupUpdateList {

    @XmlElement(name="customGroup")
    @JsonProperty("customGroups")
    private List<DtoCustomGroupUpdate> customGroups = new ArrayList<DtoCustomGroupUpdate>();

    public DtoCustomGroupUpdateList() {}
    public DtoCustomGroupUpdateList(List<DtoCustomGroupUpdate> customGroups) {this.customGroups = customGroups;}

    public List<DtoCustomGroupUpdate> getCustomGroups() {
        return customGroups;
    }

    public void add(DtoCustomGroupUpdate customGroup) {
        customGroups.add(customGroup);
    }

    public int size() {
        return customGroups.size();
    }
}
