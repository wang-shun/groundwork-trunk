package org.groundwork.rs.resources;
/*
 * Collage - The ultimate monitoring data integration framework.
 *
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.
 *
 */

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.MonitorServer;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.DeviceConverter;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoDeviceList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoMonitorServer;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoSortType;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Path("/devices")
public class DeviceResource extends AbstractResource {
    public static final String RESOURCE_PREFIX = "/devices/";
    protected static Log log = LogFactory.getLog(DeviceResource.class);

    @GET
    @Path("/{device_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoDevice getDevice(@PathParam("device_name") String deviceName,
                            @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /devices/%s with depth: %s", deviceName, depth));
            }
            if (deviceName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Device name is mandatory").build());
            }
            DeviceService deviceService =  CollageFactory.getInstance().getDeviceService();
            Device device = deviceService.getDeviceByIdentification(deviceName);
            if (device == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Device name [%s] was not found", deviceName)).build());
            }
            return DeviceConverter.convert(device, depth);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for device [%s].", deviceName)).build());
        }
        finally {
        }

    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoDeviceList getDevices(@QueryParam("query") String query,
                     @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper,
                     @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /devices with depth: %s, query: %s,  first: %d, count: %d",
                        depth, (query == null) ? "(none)" : query,  first, count));
            }
            DeviceService deviceService =  CollageFactory.getInstance().getDeviceService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();

            List<Device> devices  = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.DEVICE_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                devices = deviceService.queryDevices(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("identification", DtoSortType.Ascending);
                devices = deviceService.getDevices(null, sortCriteria, first, count).getResults();
            }

            if (devices.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Devices not found for query criteria [%s]",
                                (query != null) ? query : "(all)")).build());
            }
            List<DtoDevice> dtoDevices = new ArrayList<DtoDevice>();
            for (Device device : devices) {
                DtoDevice dtoDevice = DeviceConverter.convert(device, depth);
                dtoDevices.add(dtoDevice);
            }
            return new DtoDeviceList(dtoDevices);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for devices.").build());
        }
        finally {
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createDevices(DtoDeviceList dtoDeviceList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /devices with %d devices", (dtoDeviceList == null) ? 0 : dtoDeviceList.size()));
        }
        if (dtoDeviceList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Device list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Device", DtoOperationResults.UPDATE);
        if (dtoDeviceList.size() == 0) {
            return results;
        }
        List<String> hostsNotFound = new LinkedList<String>();
        for (DtoDevice dtoDevice : dtoDeviceList.getDevices()) {
            if (dtoDevice.getIdentification() == null) {
                results.fail("device Identification Unknown", "No Device Identification provided");
                continue;
            }
            try {
                saveDevice(dtoDevice);
                results.success(dtoDevice.getIdentification(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, dtoDevice.getIdentification()));
            }
            catch (Exception e) {
                results.fail(dtoDevice.getIdentification(), e.getMessage());
            }
        }
        addToNotFoundList(hostsNotFound, results, "Device");
        return results;
    }

    @DELETE
    @Path("/{deviceIdentificationList}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteDevices(@PathParam("deviceIdentificationList") String deviceIdentificationList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /devices/%s", deviceIdentificationList));
        }
        if (deviceIdentificationList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Device identification is mandatory").build());
        }
        List<String> ids = parseNames(deviceIdentificationList);
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        DeviceService deviceService =  CollageFactory.getInstance().getDeviceService();
        DtoOperationResults results = new DtoOperationResults("Device", DtoOperationResults.DELETE);
        for (String id : ids) {
            try {
                Device check = deviceService.getDeviceByIdentification(id);
                if (check == null) {
                    results.warn(id, "Device not found, cannot delete.");
                }
                else {
                    admin.removeDevice(id);
                    results.success(id, "Device deleted.");
                }
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove device: %s. %s", id, e.getMessage()), e);
                results.fail(id, e.toString());
            }
        }
        return results;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteDevicesWithUpdate(DtoDeviceList dtoDeviceList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /devices with %d devices", (dtoDeviceList == null) ? 0 : dtoDeviceList.size()));
        }
        if (dtoDeviceList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Device list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Device", DtoOperationResults.DELETE);
        if (dtoDeviceList.size() == 0) {
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        DeviceService deviceService =  CollageFactory.getInstance().getDeviceService();
        for (DtoDevice device : dtoDeviceList.getDevices()) {
            if (device.getIdentification() == null) {
                results.fail("(unknown)", "Device Identification not provided");
                continue;
            }
            try {
                Device check = deviceService.getDeviceByIdentification(device.getIdentification());
                if (check == null) {
                    results.fail(device.getIdentification(), "Device not found, cannot delete.");
                }
                else {
                    admin.removeDevice(device.getIdentification());
                    results.success(device.getIdentification(), "Device deleted.");
                }
            }
            catch (Exception e) {
                log.error("Failed to remove device : " + e.getMessage(), e);
                results.fail(device.getIdentification(), e.toString());
            }
        }
        // 4. Return the results
        return results;
    }

    private void saveDevice(DtoDevice dtoDevice) {
        DeviceService deviceService =  CollageFactory.getInstance().getDeviceService();
        MonitorServerService monitorServerService =  CollageFactory.getInstance().getMonitorServerService();
        Device device = deviceService.getDeviceByIdentification(dtoDevice.getIdentification());
        if (device != null) {
            if (dtoDevice.getDescription() != null)
                device.setDescription(dtoDevice.getDescription().isEmpty() ? null : dtoDevice.getDescription());
            if (dtoDevice.getDisplayName() != null)
                device.setDisplayName(dtoDevice.getDisplayName().isEmpty() ? null : dtoDevice.getDisplayName());
        }
        else {
            device = deviceService.createDevice(dtoDevice.getIdentification(), dtoDevice.getDisplayName().isEmpty() ? null : dtoDevice.getDisplayName());
            device.setDescription(dtoDevice.getDescription().isEmpty() ? null : dtoDevice.getDescription());
        }
        deviceService.saveDevice(device);
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        HostService hostService =  CollageFactory.getInstance().getHostService();
        int count = 0;
        if (dtoDevice.getHosts() != null) {
            for (DtoHost dtoHost : dtoDevice.getHosts()) {
                Host host = hostIdentityService.getHostByIdOrHostName(dtoHost.getHostName());
                if (host == null) {
                    host = hostService.createHost(dtoHost.getHostName(), device);
                }
                if (isNewHostForDevice(device, host)) {
                    device.getHosts().add(host);
                    count++;
                }
            }
        }
        if (dtoDevice.getMonitorServers() != null) {
            for (DtoMonitorServer dtoMonitorServer : dtoDevice.getMonitorServers()) {
                MonitorServer ms = monitorServerService.getMonitorServerByName(dtoMonitorServer.getMonitorServerName());
                if (ms == null) {
                    ms = monitorServerService.createMonitorServer(dtoMonitorServer.getMonitorServerName());
                }
                if (isNewMonitorServerForDevice(device, ms)) {
                    device.getMonitorServers().add(ms);
                    count++;
                }
            }
        }
        if (count > 0)
            deviceService.saveDevice(device);
    }

    private boolean isNewHostForDevice(Device device, Host host) {
        boolean isNew = true;
        if (device.getHosts() != null) {
            for (Object o : device.getHosts()) {
                Host host1 = (Host)o;
                if (host1.getHostName().equals(host.getHostName())) {
                    isNew = false;
                    break;
                }
            }
        }
        return isNew;
    }

    private boolean isNewMonitorServerForDevice(Device device, MonitorServer monitorServer) {
        boolean isNew = true;
        if (device.getHosts() != null) {
            for (Object o : device.getMonitorServers()) {
                MonitorServer ms = (MonitorServer)o;
                if (ms.getMonitorServerName().equals(monitorServer.getMonitorServerName())) {
                    isNew = false;
                    break;
                }
            }
        }
        return isNew;
    }

}
