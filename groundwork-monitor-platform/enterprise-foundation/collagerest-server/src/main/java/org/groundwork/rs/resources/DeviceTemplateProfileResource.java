/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.DeviceTemplateProfile;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.devicetemplateprofile.DeviceTemplateProfileService;
import org.groundwork.rs.conversion.DeviceTemplateProfileConverter;
import org.groundwork.rs.dto.DtoDeviceTemplateProfile;
import org.groundwork.rs.dto.DtoDeviceTemplateProfileList;
import org.groundwork.rs.dto.DtoOperationResults;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeviceTemplateProfileResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/devicetemplateprofiles")
public class DeviceTemplateProfileResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/devicetemplateprofiles/";

    private static Log log = LogFactory.getLog(DeviceTemplateProfileResource.class);

    @GET
    @Path("/{device_identification}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoDeviceTemplateProfile getDeviceTemplateProfile(@PathParam("device_identification") String deviceIdentification) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing GET on /devicetemplateprofiles for %s", deviceIdentification));
        }
        if (deviceIdentification == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("DeviceTemplateProfile device identification was not provided").build());
        }
        DeviceTemplateProfileService deviceTemplateProfileService = CollageFactory.getInstance().getDeviceTemplateProfileService();
        try {
            DeviceTemplateProfile deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification(deviceIdentification);
            if (deviceTemplateProfile == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("DeviceTemplateProfile device identification [%s] was not found", deviceIdentification)).build());
            }
            return DeviceTemplateProfileConverter.convert(deviceTemplateProfile);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format("An error occurred processing request for DeviceTemplateProfile device identification [%s]", deviceIdentification)).build());
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoDeviceTemplateProfileList getDeviceTemplateProfiles(@QueryParam("query") String query,
                                                  @QueryParam("first") @DefaultValue("-1") int first,
                                                  @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing GET on /devicetemplateprofiles with query: %s, first: %d, count: %d", (query == null) ? "(none)" : query,  first, count));
            }
            DeviceTemplateProfileService deviceTemplateProfileService = CollageFactory.getInstance().getDeviceTemplateProfileService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<DeviceTemplateProfile> deviceTemplateProfiles = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.DEVICE_TEMPLATE_PROFILE_KEY);
                if (log.isDebugEnabled()) {
                    log.debug("hql = [" + translation.getHql() + "]");
                }
                deviceTemplateProfiles = deviceTemplateProfileService.queryDeviceTemplateProfiles(translation.getHql(), translation.getCountHql(), first, count).getResults();
            } else {
                deviceTemplateProfiles = deviceTemplateProfileService.getDeviceTemplateProfiles(null, null, first, count).getResults();
            }
            if (deviceTemplateProfiles.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("DeviceTemplateProfiles not found for query criteria [%s]", (query != null) ? query : "(all)")).build());
            }
            DtoDeviceTemplateProfileList dtoDeviceTemplateProfiles = new DtoDeviceTemplateProfileList();
            for (DeviceTemplateProfile deviceTemplateProfile : deviceTemplateProfiles) {
                dtoDeviceTemplateProfiles.add(DeviceTemplateProfileConverter.convert(deviceTemplateProfile));
            }
            return dtoDeviceTemplateProfiles;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for devicetemplateprofiles.").build());
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createOrUpdateDeviceTemplateProfiles(DtoDeviceTemplateProfileList dtoDeviceTemplateProfiles) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing POST on /devicetemplateprofiles with %d DeviceTemplateProfiles", (dtoDeviceTemplateProfiles == null) ? 0 : dtoDeviceTemplateProfiles.size()));
        }
        if (dtoDeviceTemplateProfiles == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("DeviceTemplateProfiles list was not provided").build());
        }
        if (dtoDeviceTemplateProfiles.size() == 0) {
            return new DtoOperationResults(DeviceTemplateProfile.ENTITY_TYPE_CODE, DtoOperationResults.INSERT);
        }
        // optimize create all in one transaction
        DeviceTemplateProfileService deviceTemplateProfileService = CollageFactory.getInstance().getDeviceTemplateProfileService();
        boolean createAll = true;
        Map<DtoDeviceTemplateProfile,DeviceTemplateProfile> deviceTemplateProfiles = new HashMap<DtoDeviceTemplateProfile,DeviceTemplateProfile>();
        for (DtoDeviceTemplateProfile dtoDeviceTemplateProfile : dtoDeviceTemplateProfiles.getDeviceTemplateProfiles()) {
            DeviceTemplateProfile deviceTemplateProfile = null;
            if (dtoDeviceTemplateProfile.getDeviceTemplateProfileId() != null) {
                deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileById(dtoDeviceTemplateProfile.getDeviceTemplateProfileId());
            } else {
                deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification(dtoDeviceTemplateProfile.getDeviceIdentification());
            }
            deviceTemplateProfiles.put(dtoDeviceTemplateProfile, deviceTemplateProfile);
            createAll = (createAll && (deviceTemplateProfile == null));
        }
        // attempt to convert and create all device template profiles in one transaction
        if (createAll) {
            DtoOperationResults results = new DtoOperationResults(DeviceTemplateProfile.ENTITY_TYPE_CODE, DtoOperationResults.INSERT);
            try {
                // convert device template profiles
                List<DeviceTemplateProfile> createDeviceTemplateProfiles = new ArrayList<DeviceTemplateProfile>(dtoDeviceTemplateProfiles.size());
                for (DtoDeviceTemplateProfile dtoDeviceTemplateProfile : dtoDeviceTemplateProfiles.getDeviceTemplateProfiles()) {
                    createDeviceTemplateProfiles.add(convertToDeviceTemplateProfile(deviceTemplateProfileService, dtoDeviceTemplateProfile));
                }
                // save device template profiles
                deviceTemplateProfileService.saveDeviceTemplateProfiles(createDeviceTemplateProfiles);
                // add successes to results
                for (DeviceTemplateProfile deviceTemplateProfile : createDeviceTemplateProfiles) {
                    results.success(deviceTemplateProfile.getDeviceIdentification(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, deviceTemplateProfile.getDeviceIdentification()));
                }
                return results;
            } catch (Exception e) {
                // if there is only one device template profile that has failed to convert or
                // save, emit that result
                if (dtoDeviceTemplateProfiles.size() == 1) {
                    // add failure to results
                    String message = "Failed to create DeviceTemplateProfile: " + e.getMessage();
                    results.fail(dtoDeviceTemplateProfiles.getDeviceTemplateProfiles().get(0).getDeviceIdentification(), message);
                    log.error(message, e);
                    return results;
                }
            }
        }
        // process device template profiles one at a time in order to ensure that
        // the results are returned in order and that individual device template
        // profiles may be saved even if others fail
        DtoOperationResults results = new DtoOperationResults(DeviceTemplateProfile.ENTITY_TYPE_CODE, (createAll ? DtoOperationResults.INSERT : DtoOperationResults.UPDATE));
        for (DtoDeviceTemplateProfile dtoDeviceTemplateProfile : dtoDeviceTemplateProfiles.getDeviceTemplateProfiles()) {
            DeviceTemplateProfile deviceTemplateProfile = deviceTemplateProfiles.get(dtoDeviceTemplateProfile);
            if (deviceTemplateProfile == null) {
                // create device template profile
                try {
                    // convert device template profile
                    deviceTemplateProfile = convertToDeviceTemplateProfile(deviceTemplateProfileService, dtoDeviceTemplateProfile);
                    // save device template profile
                    deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile);
                    // add success to results
                    results.success(deviceTemplateProfile.getDeviceIdentification(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, deviceTemplateProfile.getDeviceIdentification()));
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to create DeviceTemplateProfile: " + e.getMessage();
                    results.fail(dtoDeviceTemplateProfile.getDeviceIdentification(), message);
                    log.error(message, e);
                }
            } else {
                // update device template profile
                try {
                    // update and save device template profile
                    if (dtoDeviceTemplateProfile.getDeviceIdentification() != null) {
                        deviceTemplateProfile.setDeviceIdentification(dtoDeviceTemplateProfile.getDeviceIdentification());
                    }
                    if (dtoDeviceTemplateProfile.getDeviceDescription() != null) {
                        deviceTemplateProfile.setDeviceDescription(dtoDeviceTemplateProfile.getDeviceDescription());
                    }
                    if (dtoDeviceTemplateProfile.getCactiHostTemplate() != null) {
                        deviceTemplateProfile.setCactiHostTemplate(dtoDeviceTemplateProfile.getCactiHostTemplate());
                        deviceTemplateProfile.setMonarchHostProfile(null);
                    }
                    else if (dtoDeviceTemplateProfile.getMonarchHostProfile() != null) {
                        deviceTemplateProfile.setCactiHostTemplate(null);
                        deviceTemplateProfile.setMonarchHostProfile(dtoDeviceTemplateProfile.getMonarchHostProfile());
                    }
                    deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile);
                    // add success to results
                    results.success(deviceTemplateProfile.getDeviceIdentification(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, deviceTemplateProfile.getDeviceIdentification()));
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to update DeviceTemplateProfile: " + e.getMessage();
                    results.fail(dtoDeviceTemplateProfile.getDeviceIdentification(), message);
                    log.error(message, e);
                }
            }
        }
        return results;
    }

    private DeviceTemplateProfile convertToDeviceTemplateProfile(DeviceTemplateProfileService deviceTemplateProfileService, DtoDeviceTemplateProfile dtoDeviceTemplateProfile) {
        DeviceTemplateProfile deviceTemplateProfile = deviceTemplateProfileService.createDeviceTemplateProfile(dtoDeviceTemplateProfile.getDeviceIdentification());
        deviceTemplateProfile.setDeviceDescription(dtoDeviceTemplateProfile.getDeviceDescription());
        deviceTemplateProfile.setCactiHostTemplate(dtoDeviceTemplateProfile.getCactiHostTemplate());
        deviceTemplateProfile.setMonarchHostProfile(dtoDeviceTemplateProfile.getMonarchHostProfile());
        return deviceTemplateProfile;
    }

    @DELETE
    @Path("/{device_identifications}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteDeviceTemplateProfiles(@PathParam("device_identifications") String deviceIdentificationsString,
                                                            @QueryParam("clear") @DefaultValue("false") boolean clear) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /devicetemplateprofiles for %s, clear is %b", deviceIdentificationsString, clear));
        }
        List<String> deviceIdentifications = null;
        try {
            deviceIdentifications = parseNames(deviceIdentificationsString);
        } catch (Exception e) {
            String message = String.format("Error converting device identifications [%s]", deviceIdentificationsString);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        DeviceTemplateProfileService deviceTemplateProfileService = CollageFactory.getInstance().getDeviceTemplateProfileService();
        DtoOperationResults results = new DtoOperationResults(DeviceTemplateProfile.ENTITY_TYPE_CODE, (clear ? DtoOperationResults.CLEAR : DtoOperationResults.DELETE));
        for (String deviceIdentification : deviceIdentifications) {
            try {
                if (clear) {
                    // clear device template and profile
                    DeviceTemplateProfile deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification(deviceIdentification);
                    if (deviceTemplateProfile != null) {
                        deviceTemplateProfile.setCactiHostTemplate(null);
                        deviceTemplateProfile.setMonarchHostProfile(null);
                        deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile);
                        // add success to results
                        results.success(deviceIdentification, "DeviceTemplateProfile cleared");
                    } else {
                        // add fail to results
                        results.fail(deviceIdentification, "DeviceTemplateProfile not found");
                    }
                } else {
                    // delete device template profile
                    if (deviceTemplateProfileService.deleteDeviceTemplateProfileByDeviceIdentification(deviceIdentification)) {
                        // add success to results
                        results.success(deviceIdentification, "DeviceTemplateProfile deleted");
                    } else {
                        // add warning to results
                        results.warn(deviceIdentification, "DeviceTemplateProfile not found, cannot delete");
                    }
                }
            } catch (Exception e) {
                // add failure to results
                String message = "Failed to delete or clear DeviceTemplateProfile: " + e.getMessage();
                results.fail(deviceIdentification, message);
                log.error(message, e);
            }
        }
        return results;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteDeviceTemplateProfiles(DtoDeviceTemplateProfileList dtoDeviceTemplateProfiles,
                                                            @QueryParam("clear") @DefaultValue("false") boolean clear) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /devicetemplateprofiles with %d DeviceTemplateProfiles, clear is %b", ((dtoDeviceTemplateProfiles == null) ? 0 : dtoDeviceTemplateProfiles.size()), clear));
        }
        if (dtoDeviceTemplateProfiles == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("DeviceTemplateProfiles list was not provided").build());
        }
        DeviceTemplateProfileService deviceTemplateProfileService = CollageFactory.getInstance().getDeviceTemplateProfileService();
        DtoOperationResults results = new DtoOperationResults(DeviceTemplateProfile.ENTITY_TYPE_CODE, (clear ? DtoOperationResults.CLEAR : DtoOperationResults.DELETE));
        for (DtoDeviceTemplateProfile dtoDeviceTemplateProfile : dtoDeviceTemplateProfiles.getDeviceTemplateProfiles()) {
            if (dtoDeviceTemplateProfile.getDeviceTemplateProfileId() != null) {
                try {
                    if (clear) {
                        // clear device template and profile
                        DeviceTemplateProfile deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileById(dtoDeviceTemplateProfile.getDeviceTemplateProfileId());
                        if (deviceTemplateProfile != null) {
                            deviceTemplateProfile.setCactiHostTemplate(null);
                            deviceTemplateProfile.setMonarchHostProfile(null);
                            deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile);
                            // add success to results
                            results.success(dtoDeviceTemplateProfile.getDeviceTemplateProfileId().toString(), "DeviceTemplateProfile cleared");
                        } else {
                            // add failure to results
                            results.fail(dtoDeviceTemplateProfile.getDeviceTemplateProfileId().toString(), "DeviceTemplateProfile not found");
                        }
                    } else {
                        // delete device template profile
                        deviceTemplateProfileService.deleteDeviceTemplateProfileById(dtoDeviceTemplateProfile.getDeviceTemplateProfileId());
                        // add success to results
                        results.success(dtoDeviceTemplateProfile.getDeviceTemplateProfileId().toString(), "DeviceTemplateProfile deleted");
                    }
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to delete or clear DeviceTemplateProfile: " + e.getMessage();
                    results.fail(dtoDeviceTemplateProfile.getDeviceTemplateProfileId().toString(), message);
                    log.error(message, e);
                }
            } else {
                try {
                    if (clear) {
                        // clear device template and profile
                        DeviceTemplateProfile deviceTemplateProfile = deviceTemplateProfileService.getDeviceTemplateProfileByDeviceIdentification(dtoDeviceTemplateProfile.getDeviceIdentification());
                        if (deviceTemplateProfile != null) {
                            deviceTemplateProfile.setCactiHostTemplate(null);
                            deviceTemplateProfile.setMonarchHostProfile(null);
                            deviceTemplateProfileService.saveDeviceTemplateProfile(deviceTemplateProfile);
                            // add success to results
                            results.success(dtoDeviceTemplateProfile.getDeviceIdentification(), "DeviceTemplateProfile cleared");
                        } else {
                            // add failure to results
                            results.fail(dtoDeviceTemplateProfile.getDeviceIdentification(), "DeviceTemplateProfile not found");
                        }
                    } else {
                        // delete device template profile
                        if (deviceTemplateProfileService.deleteDeviceTemplateProfileByDeviceIdentification(dtoDeviceTemplateProfile.getDeviceIdentification())) {
                            // add success to results
                            results.success(dtoDeviceTemplateProfile.getDeviceIdentification(), "DeviceTemplateProfile deleted");
                        } else {
                            // add warning to results
                            results.warn(dtoDeviceTemplateProfile.getDeviceIdentification(), "DeviceTemplateProfile not found, cannot delete");
                        }
                    }
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to delete or clear DeviceTemplateProfile: " + e.getMessage();
                    results.fail(dtoDeviceTemplateProfile.getDeviceIdentification(), message);
                    log.error(message, e);
                }
            }
        }
        return results;
    }
}
