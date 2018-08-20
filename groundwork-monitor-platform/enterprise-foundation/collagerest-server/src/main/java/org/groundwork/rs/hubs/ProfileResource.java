package org.groundwork.rs.hubs;
/*
 * Collage - The ultimate monitoring data integration framework.
 *
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.
 *
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.NetHubProfile;
import org.groundwork.rs.resources.AbstractResource;
import org.groundwork.rs.resources.HostResource;
import org.groundwork.rs.resources.ResourceMessages;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

@Path("/profiles")
public class ProfileResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/profiles/";
    protected static Log log = LogFactory.getLog(HostResource.class);
    protected final static String PROFILES_DIRECTORY = "/usr/local/groundwork/core/vema/profiles/";
    protected final static String PROFILE_SUFFIX = "_monitoring_profile.xml";

    @GET
    @Path("/cloudhub/{appType}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public CloudHubProfile getCloudHubProfile(@PathParam("appType") String appType) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /profiles/cloudhub/%s", appType));
            }
            if (appType == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("App Type is mandatory").build());
            }
            return readCloudHubProfile(appType);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw (WebApplicationException)e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for CloudHub appType [%s].", appType)).build());
        }
    }

    @GET
    @Path("/nethub/{appType}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public NetHubProfile getNetHubProfile(@PathParam("appType") String appType) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /profiles/nethub/%s", appType));
            }
            if (appType == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("App Type is mandatory").build());
            }
            return readNetHubProfile(appType);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw (WebApplicationException)e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for NetHub appType [%s].", appType)).build());
        }
    }

    @GET
    @Path("/container/{appType}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ContainerProfile getContainerProfile(@PathParam("appType") String appType) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /profiles/container/%s", appType));
            }
            if (appType == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("App Type is mandatory").build());
            }
            return readContainerProfile(appType);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw (WebApplicationException)e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for Container appType [%s].", appType)).build());
        }
    }

    public synchronized CloudHubProfile readCloudHubProfile(String appType) throws Exception {
        CloudHubProfile localProfile = null;
        String path = filePathFromAppType(appType);
        try {
            File file = new File(path);
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(CloudHubProfile.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                localProfile = (CloudHubProfile) unmarshaller.unmarshal(file);
            }
            else
                throw new Exception("Failed to find CloudHub profile: " + path);
        } catch (JAXBException e) {
            throw new Exception("Failed to read CloudHub profile: " + path, e);
        }
        return localProfile;
    }

    public synchronized NetHubProfile readNetHubProfile(String appType) throws Exception {
        NetHubProfile profile = null;
        String path = filePathFromAppType(appType);
        try {
            File file = new File(path);
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(NetHubProfile.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                profile = (NetHubProfile) unmarshaller.unmarshal(file);
            }
            else
                throw new Exception("Failed to find NetHub profile: " + path);
        } catch (JAXBException e) {
            throw new Exception("Failed to read nethub profile: " + path, e);
        }
        return profile;
    }

    public synchronized ContainerProfile readContainerProfile(String appType) throws Exception {
        ContainerProfile profile = null;
        String path = filePathFromAppType(appType);
        try {
            File file = new File(path);
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(ContainerProfile.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                profile = (ContainerProfile) unmarshaller.unmarshal(file);
            }
            else
                throw new Exception("Failed to find Container profile: " + path);
        } catch (JAXBException e) {
            throw new Exception("Failed to read container profile: " + path, e);
        }
        return profile;
    }

    protected String filePathFromAppType(String appType) {
        StringBuilder fullPath = new StringBuilder();
        fullPath.append(PROFILES_DIRECTORY).append(appType).append(PROFILE_SUFFIX);
        return fullPath.toString();
    }

}


