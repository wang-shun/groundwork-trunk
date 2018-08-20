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


public class StateTransition implements Serializable, com.groundwork.collage.model.StateTransition
{
	protected String hostName = null;
	protected com.groundwork.collage.model.MonitorStatus fromStatus = null;
	protected Date fromTransitionDate = null;
	protected com.groundwork.collage.model.MonitorStatus toStatus = null;
	protected Date toTransitionDate = null;	
	protected Date endTransitionDate = null;
	protected Long durationInState;
	
	public String getHostName ()
	{
		return hostName;
	}

	public String getServiceDescription () {
		return null;
	}
	
	public com.groundwork.collage.model.MonitorStatus getFromStatus ()
	{
		return fromStatus;
	}
	
	public Date getFromTransitionDate ()
	{
		return fromTransitionDate;
	}
	
	public com.groundwork.collage.model.MonitorStatus getToStatus ()
	{
		return toStatus;
	}

	public void setToStatus (com.groundwork.collage.model.MonitorStatus status)
	{
		toStatus = status;
	}

	public Date getToTransitionDate ()
	{
		return toTransitionDate;
	}

	public void setToTransitionDate (Date date)
	{
		toTransitionDate = date;
	}

	public Long getDurationInState ()
	{
		/*if (toTransitionDate == null)
			return -1L;
		
		// If there is no end transition date then we use the current
		// server time to determine duration
		if (endTransitionDate == null)
			return (new Date().getTime() - toTransitionDate.getTime());
		else
			return (endTransitionDate.getTime() - toTransitionDate.getTime());*/
		return durationInState;
		
	}
	
	public Date getEndTransitionDate ()
	{
		return endTransitionDate;
	}
	
	public void setEndTransitionDate (Date endDate)
	{
		endTransitionDate = endDate;
	}
	
	/**
	 * Construct a host or service transition - If service description is provided a ServiceStateTransition is created.
	 * @param hostName
	 * @param serviceDescription
	 * @param fromStatus
	 * @param fromDate
	 * @param toStatus
	 * @param toDate
	 * @return
	 */
	public static com.groundwork.collage.model.impl.StateTransition createStateTransition(String hostName, 
							String serviceDescription, 
							com.groundwork.collage.model.MonitorStatus fromStatus, 
							Date fromDate, 
							com.groundwork.collage.model.MonitorStatus toStatus, 
							Date toDate)
	{
		if (serviceDescription == null || serviceDescription.length() == 0)
		{
			return new HostStateTransition(hostName, fromStatus, fromDate, toStatus, toDate);
		}
				
		return new ServiceStateTransition(hostName, serviceDescription, fromStatus, fromDate, toStatus, toDate);
	}

	public void setDurationInState(Long durationInState) {
		this.durationInState = durationInState;
	}
}
