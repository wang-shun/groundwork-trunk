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


import com.groundwork.collage.metrics.CollageTimer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.groundwork.rs.auth.AuthService;
import org.groundwork.rs.common.GWRestConstants;
import org.groundwork.rs.restwebservices.utils.LoginHelper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * AuthResource - groundwork webservice authorization access token login/logout endpoints.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/auth")
public class AuthResource extends AbstractResource {

    protected static Log log = LogFactory.getLog(AuthResource.class);

    private static final String ERROR_INVALID_USER_PASSWORD = "Invalid username or password";

    private static final String ERROR_INVALID_USER_PASSWORD_APP = "Invalid username or password or gwos-app-name";

    private static final String ERROR_INVALID_APP_TOKEN = "Invalid gwos-app-name or gwos-api-token";

    private static final String ERROR_INVALID_TOKEN = "Invalid gwos-api-token";

    private static final String ERROR = "An error occurred processing auth request for user/token [%s]";

    /**
     * Login method to get the token to access groundwork webservice
     *
     * @param user valid encoded Groundwork userid
     * @param password valid encoded Groundwork password
     * @param appName application name making the request, (i.e. nagvis, nagios)
     * @return access token
     */
    @POST
    @Path("/login")
    @Produces({MediaType.TEXT_PLAIN})
    public Response login(@FormParam("user") String user, @FormParam("password") String password, @FormParam(GWRestConstants.PARAM_GWOS_APP_NAME) String appName) {
        CollageTimer timer = startMetricsTimer();
        if (user == null || password == null || appName == null || password.equalsIgnoreCase("")) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(ERROR_INVALID_USER_PASSWORD_APP).build());
        }
        try {
            // decode user and password
            user = new String(Base64.decodeBase64(user));
            if (JasyptUtils.isEncryptionEnabled()) {
                password = JasyptUtils.jasyptDecrypt(password);
            } else {
                byte[] passDecodedBytes = Base64.decodeBase64(password);
                password = new String(passDecodedBytes);
            }
            // authenticate user/password credentials
            if (!LoginHelper.getInstance().authenticate(user, password)) {
                return notFound(ERROR_INVALID_USER_PASSWORD);
            }
            // allocate and return access token
            String token = AuthService.getInstance().makeAccessToken(appName, user);
            return Response.status(Response.Status.OK).entity(token).build();
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format(ERROR, user)).build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Logout method for groundwork webservice
     *
     * @param appName application name making the request, (i.e. nagvis, nagios)
     * @param token access token from login
     * @return success
     */
    @POST
    @Path("/logout")
    @Produces({MediaType.TEXT_PLAIN})
    public boolean logout(@FormParam(GWRestConstants.PARAM_GWOS_APP_NAME) String appName, @FormParam(GWRestConstants.PARAM_GWOS_API_TOKEN) String token) {
        CollageTimer timer = startMetricsTimer();
        if ((token == null) || (appName == null)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(ERROR_INVALID_APP_TOKEN).build());
        }
        try {
            // delete access token
            if (!AuthService.getInstance().deleteAccessToken(token, appName)) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(ERROR_INVALID_TOKEN).build());
            }
            return true;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format(ERROR, token)).build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Checks if the groundwork webservice login access token is valid.
     *
     * @param appName Application name making the request, (i.e. nagvis, nagios)
     * @param token access token from login
     * @return success
     */
    @POST
    @Path("/validatetoken")
    @Produces({MediaType.TEXT_PLAIN})
    public boolean isTokenValid(@FormParam(GWRestConstants.PARAM_GWOS_APP_NAME) String appName, @FormParam(GWRestConstants.PARAM_GWOS_API_TOKEN) String token) {
        CollageTimer timer = startMetricsTimer();
        if ((token == null) || (appName == null)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(ERROR_INVALID_APP_TOKEN).build());
        }
        try {
            // check access token
            return AuthService.getInstance().checkAccessToken(token, appName);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format(ERROR, token)).build());
        } finally {
            stopMetricsTimer(timer);
        }
    }
}
