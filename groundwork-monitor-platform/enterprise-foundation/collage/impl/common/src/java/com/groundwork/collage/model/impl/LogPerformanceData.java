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
public class LogPerformanceData implements Serializable, com.groundwork.collage.model.LogPerformanceData
{

    private static final long serialVersionUID = 1;

    /** identifier field */
    private Integer logPerformanceDataId;

    /** persistent field */
    private Date lastCheckTime;

    /** persistent field */
    private com.groundwork.collage.model.ServiceStatus serviceStatus;
    
    /** persistent field */
    private com.groundwork.collage.model.PerformanceDataLabel performanceDataLabel;
    /** persistent field */
    private Double minimum =-1.0;
    
    /** persistent field */
    private Double maximum =0.0 ;
    
    /** persistent field */
    private Double average =0.0;
    
    /** persistent field */
    private Integer measurementPoints = new Integer(0);
    
    /** persistent field */
    private Integer performanceDataLabelId;

    /** full constructor */
    public LogPerformanceData(Integer logPerformanceDataId, Date checkTime,
            //String performanceName,
    		Integer performanceDataLabelId,
            com.groundwork.collage.model.ServiceStatus serviceStatus,
            com.groundwork.collage.model.PerformanceDataLabel performanceDataLabel,
            Double minimum, Double maximum, Double average,
            Integer measurementPoints)
    {
        this.logPerformanceDataId = logPerformanceDataId;
        this.lastCheckTime = checkTime;
        //this.performanceName = performanceName;
        this.performanceDataLabelId = performanceDataLabelId;
        this.serviceStatus = serviceStatus;
        this.performanceDataLabel = performanceDataLabel;
        this.maximum= maximum;
        this.minimum = minimum;
        this.average = average;
        this.measurementPoints = measurementPoints;
    }

    /** default constructor */
    public LogPerformanceData()
    {
    }

    /** minimal constructor */
    public LogPerformanceData(Integer logPerformanceDataId, 
      		Integer performanceDataLabelId,
    		Date checkTime,
            com.groundwork.collage.model.ServiceStatus serviceStatus,
            com.groundwork.collage.model.PerformanceDataLabel performanceDataLabel
            )
    {
        this.logPerformanceDataId = logPerformanceDataId;
  		this.performanceDataLabelId = performanceDataLabelId;
        this.lastCheckTime = checkTime;
        this.serviceStatus = serviceStatus;
        this.performanceDataLabel = performanceDataLabel;
     }

    public Integer getLogPerformanceDataId()
    {
        return this.logPerformanceDataId;
    }

    public void setLogPerformanceDataId(Integer logPerformanceDataId)
    {
        this.logPerformanceDataId = logPerformanceDataId;
    }

    public Date getLastCheckTime()
    {
        return this.lastCheckTime;
    }

    public void setLastCheckTime(Date checkTime)
    {
        this.lastCheckTime = checkTime;
    }
    /*
    public String getPerformanceName()
    {
        return this.performanceName;
    }

    public void setPerformanceName(String performanceName)
    {
        this.performanceName = performanceName;
    }
    */
    public Integer getPerformanceDataLabelId()
    {
        return this.performanceDataLabelId;
    }

    public void setPerformanceDataLabelId(Integer performanceDataLabelId)
    {
        this.performanceDataLabelId = performanceDataLabelId;
    }
    
    public com.groundwork.collage.model.ServiceStatus getServiceStatus()
    {
        return this.serviceStatus;
    }

    public void setServiceStatus(com.groundwork.collage.model.ServiceStatus serviceStatus)
    {
        this.serviceStatus = serviceStatus;
    }

    public com.groundwork.collage.model.PerformanceDataLabel getPerformanceDataLabel()
    {
        return this.performanceDataLabel;
    }

    public void setPerformanceDataLabel(com.groundwork.collage.model.PerformanceDataLabel performanceDataLabel)
    {
        this.performanceDataLabel = performanceDataLabel;
    }
    
    
    public String toString()
    {
        return new ToStringBuilder(this).append("logPerformanceDataId",
                getLogPerformanceDataId()).toString();
    }

	/**
	 * @return the average
	 */
	public Double getAverage() {
		return average;
	}

	/**
	 * @param average the average to set
	 */
	public void setAverage(Double average) {
		this.average = average;
	}

	/**
	 * @return the maximum
	 */
	public Double getMaximum() {
		return maximum;
	}

	/**
	 * @param maximum the maximum to set
	 */
	public void setMaximum(Double maximum) {
		this.maximum = maximum;
	}

	/**
	 * @return the measurementPoints
	 */
	public Integer getMeasurementPoints() {
		return measurementPoints;
	}

	/**
	 * @param measurementPoints the measurementPoints to set
	 */
	public void setMeasurementPoints(Integer measurementPoints) {
		this.measurementPoints = measurementPoints;
	}

	/**
	 * @return the minimum
	 */
	public Double getMinimum() {
		return minimum;
	}

	/**
	 * @param minimum the minimum to set
	 */
	public void setMinimum(Double minimum) {
		this.minimum = minimum;
	}

}
