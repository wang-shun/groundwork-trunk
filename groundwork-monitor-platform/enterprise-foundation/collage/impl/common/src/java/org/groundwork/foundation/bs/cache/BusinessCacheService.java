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
package org.groundwork.foundation.bs.cache;

import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.ServiceStatus;

import java.util.Collection;

public interface BusinessCacheService {

	/**
	 * Invalidate all host state and caches.
	 *
	 * @param hostNames
	 * @paran servicesUpdateOnly
	 */
	public void invalidate(Collection<String> hostNames, boolean servicesUpdateOnly);

	/**
	 * Cache host device mapping.
	 *
	 * @param hosts
	 * @param force
	 */
	public void saveHostDevice(Collection<Host> hosts, boolean force);

	/**
	 * Cache host device mapping.
	 *
	 * @param host
	 * @param force
     */
	public void saveHostDevice(Host host, boolean force);

	/**
	 * Cache host device mapping.
	 *
	 * @param services
	 * @param force
	 */
	public void saveServiceHostDevice(Collection<ServiceStatus> services, boolean force);

	/**
	 * Cache host device mapping.
	 *
	 * @param service
	 * @param force
	 */
	public void saveServiceHostDevice(ServiceStatus service, boolean force);

	/**
	 * Invalidate all host state and caches.
	 *
	 * @paran deviceIds
	 */
	public void invalidate(Collection<Integer> deviceIds);
}
