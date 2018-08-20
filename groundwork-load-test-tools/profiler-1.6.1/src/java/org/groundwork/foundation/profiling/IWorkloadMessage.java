/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@itgroundwork.com

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
package org.groundwork.foundation.profiling;

import java.sql.Connection;

import org.groundwork.foundation.profiling.exceptions.ProfilerException;

public interface IWorkloadMessage extends Runnable 
{
	public String buildMessage () throws ProfilerException;
	
	public boolean isUpdateComplete () throws ProfilerException;
	
	public java.sql.Timestamp captureMetrics () throws ProfilerException;
	
	public IWorkloadMessage getRunnableInstance (int workloadId, 
												 int batchCount, 
												 MessageSocketInfo messageSocketInfo, 
												 Connection dbProfilerConnection, 
												 Connection dbSourceConnection,
												 long deltaTime);
}
