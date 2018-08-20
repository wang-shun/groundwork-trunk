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

package com.groundwork.collage.model;

import java.util.Date;

/**
 * LogPerformanceData
 * 
 * @author <a href="mailto:dtaylor@itgroundwork.com">David Sean Taylor</a>
 * @version $Id: LogPerformanceData.java 12936 2008-08-26 18:26:59Z cpora $
 */

public interface LogPerformanceData
{
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.LogPerformanceData";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.LogPerformanceData";
	
    Integer getLogPerformanceDataId();

    Date getLastCheckTime();

    void setLastCheckTime(Date checkTime);
    /*
    String getPerformanceName();

    void setPerformanceName(String performancename);
	*/
    
    Integer getPerformanceDataLabelId();

    public void setPerformanceDataLabelId(Integer performanceDataLabelId);
    
    ServiceStatus getServiceStatus();

    void setServiceStatus(ServiceStatus serviceStatus);
    
    
    PerformanceDataLabel getPerformanceDataLabel();

    void setPerformanceDataLabel(com.groundwork.collage.model.PerformanceDataLabel performanceDataLabel);
    
    
    public Double getAverage();
    public void setAverage(Double average);
    
    public Double getMaximum();
    public void setMaximum(Double maximum);
    
    public Integer getMeasurementPoints();
    public void setMeasurementPoints(Integer measurementPoints);
    
    public Double getMinimum();
    public void setMinimum(Double minimum);
}