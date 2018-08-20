/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundwork.agents.appservers.collector.api;

import java.util.HashMap;
/**
 * This class collects the performance data for websphere
 * 
 * @author Arul Shanmugam
 * 
 */
public interface GWOSCollector {	
	
		
	/**
	 * Gets the Performance data.
	 * @throws Exception
	 */
	public HashMap<String, Object> getPerformanceData() throws Exception;

}
