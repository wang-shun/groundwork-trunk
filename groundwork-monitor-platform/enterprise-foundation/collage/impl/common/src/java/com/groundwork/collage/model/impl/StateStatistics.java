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
import java.util.Iterator;
import java.util.List;

public class StateStatistics implements Serializable
{
    private long totalHosts;

    private long totalServices;
    
    private String hostGroupName;

    private double availability=0;
    private List<StatisticProperty> statisticProperties = null;

    public StateStatistics() {
    }

    public StateStatistics(
    	   String hostGroupName,
           long totalHosts,
           long totalServices,
           List<StatisticProperty> statisticProperties) {
    	   this.hostGroupName = hostGroupName;
           this.totalHosts = totalHosts;
           this.totalServices = totalServices;
           this.statisticProperties = statisticProperties;
    }
    
    public StateStatistics(
     	   String hostGroupName,
            long totalHosts,
            long totalServices,
            List<StatisticProperty> statisticProperties,
            double availability) {
     	   this.hostGroupName = hostGroupName;
            this.totalHosts = totalHosts;
            this.totalServices = totalServices;
            this.statisticProperties = statisticProperties;
            this.availability = availability;
     }
     
    
    
    /* Setters and Getters */
    
    /**
	 * @return Returns the statisticProperties.
	 */
	public List<StatisticProperty> getStatisticProperties() {
		return statisticProperties;
	}

	/**
	 * @param statisticProperties The statisticProperties to set.
	 */
	public void setStatisticProperties(List<StatisticProperty> statisticProperties) {
		this.statisticProperties = statisticProperties;
	}

	/**
	 * @return Returns the totalHosts.
	 */
	public long getTotalHosts() {
		return totalHosts;
	}

	/**
	 * @param totalHosts The totalHosts to set.
	 */
	public void setTotalHosts(long totalHosts) {
		this.totalHosts = totalHosts;
	}

	/**
	 * @return Returns the totalServices.
	 */
	public long getTotalServices() {
		return totalServices;
	}

	/**
	 * @param totalServices The totalServices to set.
	 */
	public void setTotalServices(long totalServices) {
		this.totalServices = totalServices;
	}
	
	/**
	 * @return Returns the totalServices.
	 */
	public double getAvailability() {
		return availability;
	}

	/**
	 * @param totalServices The totalServices to set.
	 */
	public void setAvailability(double availability) {
		this.availability = availability;
	}
	
	/**
	 * @return Returns the hostGroupName.
	 */
	public String getHostGroupName() {
		return hostGroupName;
	}

	/**
	 * @param hostGroupName The hostGroupName to set.
	 */
	public void setHostGroupName(String hostGroupName) {
		this.hostGroupName = hostGroupName;
	}
	
	public StatisticProperty getStatisticProperty (String status)
	{
		if (statisticProperties == null || statisticProperties.size() == 0)
			return null;
		
		StatisticProperty statisticProperty = null;
		Iterator<StatisticProperty> it = statisticProperties.iterator();
		while (it.hasNext())
		{
			statisticProperty = it.next();
			if (statisticProperty.getName().equals(status))
				return statisticProperty;
		}
		
		return null;
	}
	
	public String toString ()
	{
		StringBuilder sb = new StringBuilder(64);
		
		sb.append("Total Hosts: ");
		sb.append(totalHosts);
		sb.append(", Total Services: ");
		sb.append(totalServices);
		sb.append(", Host Group Name: ");
		sb.append(hostGroupName);
		
		if (statisticProperties != null) 
		{
			sb.append("\nStatistic Properties:\n");
			
			for (int i = 0; i < statisticProperties.size(); i++)
			{
				sb.append(statisticProperties.get(i));
				sb.append("\n");
			}
		}

		return sb.toString();
	}
}
