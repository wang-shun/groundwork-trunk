/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.rs.biz;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.biz.RTMMServices;
import com.groundwork.collage.biz.model.RTMMCustomGroup;
import com.groundwork.collage.biz.model.RTMMHost;
import com.groundwork.collage.biz.model.RTMMHostGroup;
import com.groundwork.collage.biz.model.RTMMServiceGroup;
import com.groundwork.collage.metrics.CollageTimer;
import org.groundwork.rs.conversion.CustomGroupConverter;
import org.groundwork.rs.conversion.HostConverter;
import org.groundwork.rs.conversion.HostGroupConverter;
import org.groundwork.rs.conversion.ServiceGroupConverter;
import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoCustomGroupList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.dto.DtoServiceGroupList;
import org.groundwork.rs.resources.AbstractResource;
import org.groundwork.rs.resources.ResourceMessages;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * RTMMResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/rtmm")
public class RTMMResource extends AbstractResource {

    /**
     * Get all hosts optimized for RTMM.
     *
     * @return collection of DTO hosts
     */
    @GET
    @Path("/hosts")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostList getHosts() {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug("processing /GET on /rtmm/hosts");
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection<RTMMHost> hosts = rtmm.getHosts();
            if ((hosts == null) || hosts.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Hosts not found").build());
            }
            return convertHostsToDtoList(hosts);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for hosts.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get hosts by id optimized for RTMM.
     *
     * @param hostIds host ids
     * @return collection of DTO hosts
     */
    @GET
    @Path("/hosts/{hostIds}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostList getHost(@PathParam("hostIds") String hostIds) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /rtmm/host/%s", hostIds));
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection <RTMMHost> hosts = null;
            if (!hostIds.contains(",")) {
                int hostId = Integer.parseInt(hostIds);
                RTMMHost host = rtmm.getHost(hostId);
                if (host == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Host [%d] not found", hostId)).build());
                }
                hosts = Arrays.asList(host);
            } else {
                hosts = rtmm.getHosts(parseCSVIds(hostIds));
                if ((hosts == null) || hosts.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Hosts [%s] not found", hostIds)).build());
                }
            }
            return convertHostsToDtoList(hosts);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for host.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get all host groups optimized for RTMM.
     *
     * @return collection of DTO host groups
     */
    @GET
    @Path("/hostgroups")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostGroupList getHostGroups() {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug("processing /GET on /rtmm/hostgroups");
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection<RTMMHostGroup> hostGroups = rtmm.getHostGroups();
            if ((hostGroups == null) || hostGroups.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Host groups not found").build());
            }
            return convertHostGroupsToDtoList(hostGroups);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for host groups.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get host groups by id optimized for RTMM.
     *
     * @param hostGroupIds host group ids
     * @return collection of DTO host groups
     */
    @GET
    @Path("/hostgroups/{hostGroupIds}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostGroupList getHostGroup(@PathParam("hostGroupIds") String hostGroupIds) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /rtmm/hostgroup/%s", hostGroupIds));
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection <RTMMHostGroup> hostGroups = null;
            if (!hostGroupIds.contains(",")) {
                int hostGroupId = Integer.parseInt(hostGroupIds);
                RTMMHostGroup hostGroup = rtmm.getHostGroup(hostGroupId);
                if (hostGroup == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Host group [%d] not found", hostGroupId)).build());
                }
                hostGroups = Arrays.asList(hostGroup);
            } else {
                hostGroups = rtmm.getHostGroups(parseCSVIds(hostGroupIds));
                if ((hostGroups == null) || hostGroups.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Host groups [%s] not found", hostGroupIds)).build());
                }
            }
            return convertHostGroupsToDtoList(hostGroups);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for host group.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get all service groups optimized for RTMM.
     *
     * @return collection of DTO service groups
     */
    @GET
    @Path("/servicegroups")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoServiceGroupList getServiceGroups() {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug("processing /GET on /rtmm/servicegroups");
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection<RTMMServiceGroup> serviceGroups = rtmm.getServiceGroups();
            if ((serviceGroups == null) || serviceGroups.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Service groups not found").build());
            }
            return convertServiceGroupsToDtoList(serviceGroups);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for service groups.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get service groups by id optimized for RTMM.
     *
     * @param serviceGroupIds service group ids
     * @return collection of DTO service groups
     */
    @GET
    @Path("/servicegroups/{serviceGroupIds}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoServiceGroupList getServiceGroup(@PathParam("serviceGroupIds") String serviceGroupIds) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /rtmm/servicegroup/%s", serviceGroupIds));
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection <RTMMServiceGroup> serviceGroups = null;
            if (!serviceGroupIds.contains(",")) {
                int serviceGroupId = Integer.parseInt(serviceGroupIds);
                RTMMServiceGroup serviceGroup = rtmm.getServiceGroup(serviceGroupId);
                if (serviceGroup == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Service group [%d] not found", serviceGroupId)).build());
                }
                serviceGroups = Arrays.asList(serviceGroup);
            } else {
                serviceGroups = rtmm.getServiceGroups(parseCSVIds(serviceGroupIds));
                if ((serviceGroups == null) || serviceGroups.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Service groups [%s] not found", serviceGroupIds)).build());
                }
            }
            return convertServiceGroupsToDtoList(serviceGroups);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for service group.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get all custom groups optimized for RTMM.
     *
     * @return collection of DTO custom groups
     */
    @GET
    @Path("/customgroups")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoCustomGroupList getCustomGroups() {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug("processing /GET on /rtmm/customgroups");
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection<RTMMCustomGroup> customGroups = rtmm.getCustomGroups();
            if ((customGroups == null) || customGroups.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Custom groups not found").build());
            }
            return convertCustomGroupsToDtoList(customGroups);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for custom groups.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get custom groups by id optimized for RTMM.
     *
     * @param customGroupIds custom group ids
     * @return collection of DTO custom groups
     */
    @GET
    @Path("/customgroups/{customGroupIds}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoCustomGroupList getCustomGroup(@PathParam("customGroupIds") String customGroupIds) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /rtmm/customgroup/%s", customGroupIds));
        }
        RTMMServices rtmm = (RTMMServices)CollageFactory.getInstance().getAPIObject(RTMMServices.SERVICE);
        try {
            Collection <RTMMCustomGroup> customGroups = null;
            if (!customGroupIds.contains(",")) {
                int customGroupId = Integer.parseInt(customGroupIds);
                RTMMCustomGroup customGroup = rtmm.getCustomGroup(customGroupId);
                if (customGroup == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Custom group [%d] not found", customGroupId)).build());
                }
                customGroups = Arrays.asList(customGroup);
            } else {
                customGroups = rtmm.getCustomGroups(parseCSVIds(customGroupIds));
                if ((customGroups == null) || customGroups.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Custom groups [%s] not found", customGroupIds)).build());
                }
            }
            return convertCustomGroupsToDtoList(customGroups);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for custom group.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Parse CSV id list into array.
     *
     * @param ids CSV id list string
     * @return ids array
     */
    private static Integer [] parseCSVIds(String ids) {
        String [] splitIds = ids.split("\\s*,\\s*");
        Integer [] parsedIds = new Integer[splitIds.length];
        for (int i = 0, limit = splitIds.length; (i < limit); i++) {
            parsedIds[i] = Integer.parseInt(splitIds[i]);
        }
        return parsedIds;
    }

    /**
     * Convert RTMM to DTO hosts.
     *
     * @param hosts collection of RTMM hosts
     * @return collection of DTO hosts
     */
    private static DtoHostList convertHostsToDtoList(Collection<RTMMHost> hosts) {
        List<DtoHost> dtoHosts = new ArrayList<DtoHost>();
        for (RTMMHost host : hosts) {
            DtoHost dtoHost = HostConverter.convert(host);
            dtoHosts.add(dtoHost);
        }
        return new DtoHostList(dtoHosts);
    }

    /**
     * Convert RTMM to DTO host groups.
     *
     * @param hostGroups collection of RTMM host groups
     * @return collection of DTO host groups
     */
    private static DtoHostGroupList convertHostGroupsToDtoList(Collection<RTMMHostGroup> hostGroups) {
        List<DtoHostGroup> dtoHostGroups = new ArrayList<DtoHostGroup>();
        for (RTMMHostGroup hostGroup : hostGroups) {
            DtoHostGroup dtoHostGroup = HostGroupConverter.convert(hostGroup);
            dtoHostGroups.add(dtoHostGroup);
        }
        return new DtoHostGroupList(dtoHostGroups);
    }

    /**
     * Convert RTMM to DTO service groups.
     *
     * @param serviceGroups collection of RTMM service groups
     * @return collection of DTO service groups
     */
    private static DtoServiceGroupList convertServiceGroupsToDtoList(Collection<RTMMServiceGroup> serviceGroups) {
        List<DtoServiceGroup> dtoServiceGroups = new ArrayList<DtoServiceGroup>();
        for (RTMMServiceGroup serviceGroup : serviceGroups) {
            DtoServiceGroup dtoServiceGroup = ServiceGroupConverter.convert(serviceGroup);
            dtoServiceGroups.add(dtoServiceGroup);
        }
        return new DtoServiceGroupList(dtoServiceGroups);
    }

    /**
     * Convert RTMM to DTO custom groups.
     *
     * @param customGroups collection of RTMM custom groups
     * @return collection of DTO custom groups
     */
    private static DtoCustomGroupList convertCustomGroupsToDtoList(Collection<RTMMCustomGroup> customGroups) {
        List<DtoCustomGroup> dtoCustomGroups = new ArrayList<DtoCustomGroup>();
        for (RTMMCustomGroup customGroup : customGroups) {
            DtoCustomGroup dtoCustomGroup = CustomGroupConverter.convert(customGroup);
            dtoCustomGroups.add(dtoCustomGroup);
        }
        return new DtoCustomGroupList(dtoCustomGroups);
    }
}
