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

package org.groundwork.rs.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DtoBizHostServiceInDowntimeList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="bizHostServiceInDowntimes")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoBizHostServiceInDowntimeList {

    @XmlElement(name="bizHostServiceInDowntime")
    @JsonProperty("bizHostServiceInDowntimes")
    private List<DtoBizHostServiceInDowntime> bizHostServiceInDowntimes = new ArrayList<DtoBizHostServiceInDowntime>();

    /**
     * Shallow copy constructor.
     *
     * @param bizHostServiceInDowntimes BizHostServiceInDowntimes instances to copy.
     */
    public DtoBizHostServiceInDowntimeList(Collection<DtoBizHostServiceInDowntime> bizHostServiceInDowntimes) {
        this.bizHostServiceInDowntimes.addAll(bizHostServiceInDowntimes);
    }

    /**
     * Default constructor.
     */
    public DtoBizHostServiceInDowntimeList() {
    }

    /**
     * Add host service instance to biz host service in downtime list.
     *
     * @param dtoBizHostServiceInDowntime host service instance to add
     */
    public void add(DtoBizHostServiceInDowntime dtoBizHostServiceInDowntime) {
        bizHostServiceInDowntimes.add(dtoBizHostServiceInDowntime);
    }

    /**
     * Get biz host service in downtime list size.
     *
     * @return size of biz host service in downtime list
     */
    public int size()
    {
        return bizHostServiceInDowntimes.size();
    }

    public List<DtoBizHostServiceInDowntime> getBizHostServiceInDowntimes() {
        return bizHostServiceInDowntimes;
    }
}
