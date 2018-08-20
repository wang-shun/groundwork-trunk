package org.groundwork.rs.client.hubs;
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
import org.groundwork.rs.client.AgentClient;
import org.groundwork.rs.client.BaseRestClient;
import org.groundwork.rs.client.CollageRestException;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.NetHubProfile;
import org.groundwork.rs.dto.profiles.ProfileType;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ProfileClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(AgentClient.class);
    private static final String API_ROOT_SINGLE = "/profiles";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";
    private static final String API_CLOUDHUB = API_ROOT + "cloudhub/";
    private static final String API_NETHUB = API_ROOT + "nethub/";
    private static final String API_CONTAINER = API_ROOT + "container/";

    public ProfileClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public ProfileClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single CloudHub profile by its primary, unique key 'appType' returning a CloudHubProfile transfer object
     *
     *  {@link org.groundwork.rs.dto.DtoMonitorServer} collections.
     * @param profileType the unique name of a CloudHub profile type
     * @return a CloudHub profile object of the specified depth or null if not found
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public CloudHubProfile lookupCloud(ProfileType profileType) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<CloudHubProfile> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_CLOUDHUB, profileType.name());
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<CloudHubProfile>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    CloudHubProfile profile = response.getEntity(new GenericType<CloudHubProfile>() {});
                    return profile;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing lookup CloudHub Profile (%s) with status code of %d, reason: %s",
                profileType.name(), status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Lookup a single NetHub profile by its primary, unique key 'appType' returning a NetHubProfile transfer object
     *
     *  {@link org.groundwork.rs.dto.DtoMonitorServer} collections.
     * @param profileType the unique name of a NetHub profile type
     * @return a NetHub profile object of the specified depth or null if not found
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public NetHubProfile lookupNetwork(ProfileType profileType) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<NetHubProfile> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_NETHUB, profileType.name());
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<NetHubProfile>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    NetHubProfile profile = response.getEntity(new GenericType<NetHubProfile>() {});
                    return profile;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing lookup NetHub Profile (%s) with status code of %d, reason: %s",
                profileType.name(), status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Lookup a single Container profile by its primary, unique key 'appType' returning a ContainerProfile transfer object
     *
     *  {@link org.groundwork.rs.dto.DtoMonitorServer} collections.
     * @param profileType the unique name of a Container profile type
     * @return a Container profile object of the specified depth or null if not found
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public ContainerProfile lookupContainer(ProfileType profileType) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<ContainerProfile> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_CONTAINER, profileType.name());
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<ContainerProfile>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    ContainerProfile profile = response.getEntity(new GenericType<ContainerProfile>() {});
                    return profile;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing lookup Container Profile (%s) with status code of %d, reason: %s",
                profileType.name(), status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
