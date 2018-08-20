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
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class PerformanceDataLabel implements Serializable, com.groundwork.collage.model.PerformanceDataLabel
{

    private static final long serialVersionUID = 1;

    /** identifier field */
    private Integer performanceDataLabelId;

    /** persistent field */
    private String performanceName;

    /** persistent field */
    private String serviceDisplayName;
    
    /** persistent field */
    private String metricLabel;
    
    /** persistent field */
    private String unit;

    /** full constructor */
    public PerformanceDataLabel( 
            String performanceName,
            String serviceDisplayName,
            String metricLabel,
            String unit)
    {
        this.performanceName = performanceName;
        this.serviceDisplayName = serviceDisplayName;
        this.metricLabel = metricLabel;
        this.unit = unit;
    }

    /** default constructor */
    public PerformanceDataLabel()
    {
    }

    public PerformanceDataLabel(String performanceName)
    {
    	this.performanceName = performanceName;
    }
    
    public Integer getPerformanceDataLabelId()
    {
        return this.performanceDataLabelId;
    }


    public void setPerformanceDataLabelId(Integer performanceDataLabelId)
    {
        this.performanceDataLabelId = performanceDataLabelId;
    }

    public String getPerformanceName()
    {
        return this.performanceName;
    }

    public void setPerformanceName(String performanceName)
    {
        this.performanceName = performanceName;
    }

    public String getServiceDisplayName()
    {
        return this.serviceDisplayName;
    }

    public void setServiceDisplayName(String serviceDisplayName)
    {
        this.serviceDisplayName = serviceDisplayName;
    }
    
    public String getMetricLabel()
    {
        return this.metricLabel;
    }

    public void setMetricLabel(String metricLabel)
    {
        this.metricLabel = metricLabel;
    }

    
    public String getUnit()
    {
        return this.unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    
    public String toString()
    {
        return new ToStringBuilder(this).append("performanceDataLabelId",
                getPerformanceDataLabelId()).toString();
    }



}

