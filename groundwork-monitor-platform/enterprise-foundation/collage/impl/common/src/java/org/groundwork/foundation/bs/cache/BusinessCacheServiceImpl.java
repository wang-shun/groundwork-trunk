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

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.ServiceStatus;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.status.StatusService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BusinessCacheServiceImpl implements  BusinessCacheService
{
	private HostService hostService;
	private StatusService statusService;
	private HostIdentityService hostIdentityService;
	private Set<String> hostNamesWithDeviceIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private ConcurrentHashMap<Integer, Set<String>> deviceIdToHostNames = new ConcurrentHashMap<>();

	public BusinessCacheServiceImpl() {
	}

	public HostService getHostService() {
		return hostService;
	}

	public void setHostService(HostService hostService) {
		this.hostService = hostService;
	}

	public StatusService getStatusService() {
		return statusService;
	}

	public void setStatusService(StatusService statusService) {
		this.statusService = statusService;
	}

	public HostIdentityService getHostIdentityService() {
		return hostIdentityService;
	}

	public void setHostIdentityService(HostIdentityService hostIdentityService) {
		this.hostIdentityService = hostIdentityService;
	}

	@Override
	public void invalidate(Collection<String> hostNames, boolean serviceUpdateOnly) {
		// propagate invalidation to services with caches
		if (!serviceUpdateOnly) {
			if (hostIdentityService != null) {
				hostIdentityService.invalidateHosts(hostNames);
			}
			if (hostService != null) {
				hostService.invalidateHosts(hostNames);
			}
		}
		if (statusService != null) {
			statusService.invalidateHosts(hostNames);
		}
	}

	@Override
	public void saveHostDevice(Collection<Host> hosts, boolean force) {
		for (Host host : hosts) {
			saveHostDevice(host, force);
		}
	}

	@Override
	public void saveHostDevice(Host host, boolean force) {
		if (host != null) {
			String hostName = host.getHostName().toLowerCase();
			if (force || !hostNamesWithDeviceIds.contains(hostName)) {
				Device device = host.getDevice();
				if (device != null) {
					Collection<String> deviceHostNames = deviceIdToHostNames.get(device.getDeviceId());
					while (deviceHostNames == null) {
						deviceIdToHostNames.putIfAbsent(device.getDeviceId(),
								Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));
						deviceHostNames = deviceIdToHostNames.get(device.getDeviceId());
					}
					deviceHostNames.add(hostName);
					hostNamesWithDeviceIds.add(hostName);
				}
			}
		}
	}

	@Override
	public void saveServiceHostDevice(Collection<ServiceStatus> services, boolean force) {
		for (ServiceStatus service : services) {
			saveHostDevice(service.getHost(), force);
		}
	}

	@Override
	public void saveServiceHostDevice(ServiceStatus service, boolean force) {
		if (service != null) {
			saveHostDevice(service.getHost(), force);
		}
	}

	@Override
	public void invalidate(Collection<Integer> deviceIds) {
		// translate device ids to host names and propagate invalidation
		Set<String> hostNames = new HashSet<>();
		for (Integer deviceId : deviceIds) {
			Collection<String> deviceHostNames = deviceIdToHostNames.remove(deviceId);
			if (deviceHostNames != null) {
				hostNames.addAll(deviceHostNames);
				for (String hostName : deviceHostNames) {
					hostNamesWithDeviceIds.remove(hostName);
				}
			}
		}
		if (!hostNames.isEmpty()) {
			invalidate(hostNames, false);
		}
	}
}
