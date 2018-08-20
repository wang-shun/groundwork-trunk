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
 * DtoCategoryUpdateList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "categoryUpdates")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCategoryUpdateList {

    @XmlElement(name="categoryUpdate")
    @JsonProperty("categoryUpdates")
    private List<DtoCategoryUpdate> categoryUpdates = new ArrayList<DtoCategoryUpdate>();

    public DtoCategoryUpdateList() {}
    public DtoCategoryUpdateList(List<DtoCategoryUpdate> categoryUpdates) {this.categoryUpdates = categoryUpdates;}

    public List<DtoCategoryUpdate> getCategoryUpdates() {
        return categoryUpdates;
    }

    public void add(DtoCategoryUpdate categoryUpdate) {
        categoryUpdates.add(categoryUpdate);
    }

    public int size() {
        return categoryUpdates.size();
    }

}
