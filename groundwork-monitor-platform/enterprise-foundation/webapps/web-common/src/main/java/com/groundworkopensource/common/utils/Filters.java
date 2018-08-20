/*
 * Common -Utilities framework.
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
package com.groundworkopensource.common.utils;

public class Filters implements java.io.Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HostFilter[] hostFilter = null;
	private ServiceFilter[] serviceFilter = null;
	public HostFilter[] getHostFilter() {
		return hostFilter;
	}
	public void setHostFilter(HostFilter[] hostFilter) {
		this.hostFilter = hostFilter;
	}
	public ServiceFilter[] getServiceFilter() {
		return serviceFilter;
	}
	public void setServiceFilter(ServiceFilter[] serviceFilter) {
		this.serviceFilter = serviceFilter;
	}

}
