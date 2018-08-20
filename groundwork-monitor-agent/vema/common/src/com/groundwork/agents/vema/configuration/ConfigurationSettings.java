/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/

package com.groundwork.agents.vema.configuration;

public class ConfigurationSettings {
	private boolean isGraphed			= false;
	private boolean isMonitored			= false;
	private double	dwWarningThreshold	= 0.0;
	private double	dwCriticalThreshold	= 0.0;
	private double	dwMinValue			= 0.0;
	private double	dwMaxValue			= 0.0;
	
	public ConfigurationSettings(boolean isMonitored, boolean isGraphed,double warningThreshold, double criticalThreshold)
	{
		this.isMonitored			=	isMonitored;
		this.isGraphed				=	isGraphed;
		this.dwWarningThreshold		=	warningThreshold;
		this.dwCriticalThreshold	=	criticalThreshold;
	}
	
	public ConfigurationSettings(boolean isMonitored, boolean isGraphed,double warningThreshold, double criticalThreshold, double minValue, double maxValue)
	{
		this.isMonitored			=	isMonitored;
		this.isGraphed				=	isGraphed;
		this.dwWarningThreshold		=	warningThreshold;
		this.dwCriticalThreshold	=	criticalThreshold;
		this.dwMinValue				=	minValue;
		this.dwMaxValue				=	maxValue;
	}

	public boolean isGraphed()                       { return isGraphed; } 
	public boolean isMonitored()                     { return isMonitored; } 
	public double getDwWarningThreshold()            { return dwWarningThreshold; }
	public double getDwCriticalThreshold()           { return dwCriticalThreshold; }
	public double getDwMinValue()                    { return dwMinValue; } 
	public double getDwMaxValue()                    { return dwMaxValue; } 

	public void setisGraphed(boolean value)          { this.isGraphed           = value; }
	public void setIsMonitored(boolean value)        { this.isMonitored         = value; }
	public void setDwWarningThreshold(double value)  { this.dwWarningThreshold  = value; } 
	public void setDwCriticalThreshold(double value) { this.dwCriticalThreshold = value; } 
	public void setDwMinValue(double value)          { this.dwMinValue          = value; } 
	public void setDwMaxValue(double value)          { this.dwMaxValue          = value; }
}
