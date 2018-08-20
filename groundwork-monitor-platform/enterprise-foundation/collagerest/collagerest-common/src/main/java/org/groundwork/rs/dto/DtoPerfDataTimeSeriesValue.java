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
 * DtoPerfDataTimeSeriesValue
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "perfDataTimeSeriesValue")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoPerfDataTimeSeriesValue {

    @XmlAttribute
    private String valueType;
    @XmlAttribute
    private Long timestamp;
    @XmlAttribute
    private Double value;

    /**
     * Default persistence constructor.
     */
    public DtoPerfDataTimeSeriesValue() {
    }

    /**
     * Constructor.
     *
     * @param valueType performance data value type
     * @param timestamp performance data timestamp
     * @param value performance data value
     */
    public DtoPerfDataTimeSeriesValue(String valueType, Long timestamp, Double value) {
        this.valueType = valueType;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
