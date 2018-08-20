/*
 * 
 * Copyright 2012 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.bean;

import org.groundwork.foundation.ws.model.impl.StatisticProperty;

public class EventPieBean implements java.io.Serializable {
	private String groupName = null;
	
	private String truncatedGroupName = null;

	private byte[] chart = null;

	private StatisticProperty[] stats = null;
	
	private String statToolTip = null;
	
	
	private String consoleURL = null;
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getTruncatedGroupName() {
		return truncatedGroupName;
	}

	public void setTruncatedGroupName(String truncatedGroupName) {
		this.truncatedGroupName = truncatedGroupName;
	}

	public StatisticProperty[] getStats() {
		return stats;
	}

	public void setStats(StatisticProperty[] stats) {
		this.stats = stats;
	}

	public byte[] getChart() {
		return chart;
	}

	public void setChart(byte[] chart) {
		this.chart = chart;
	}
	
	public String getStatToolTip() {
		return statToolTip;
	}

	public void setStatToolTip(String statToolTip) {
		this.statToolTip = statToolTip;
	}
	
	public String getConsoleURL() {
		return consoleURL;
	}

	public void setConsoleURL(String consoleURL) {
		this.consoleURL = consoleURL;
	}

}
