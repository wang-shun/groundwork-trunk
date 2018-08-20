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

package org.groundwork.rs.restwebservices;

import org.codehaus.jackson.JsonProcessingException;
import org.groundwork.rs.dto.DtoError;
import org.jboss.resteasy.spi.Failure;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.UnmarshalException;

/**
 * FoundationExceptionMapper
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Provider
public class FoundationExceptionMapper implements ExceptionMapper<Exception> {

    private static final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();

    @Override
    public Response toResponse(Exception e) {
        DtoError dtoError = null;
        if (e instanceof WebApplicationException) {
            // application error
            WebApplicationException wae = (WebApplicationException)e;
            if (wae.getResponse() != null) {
                Object entity = wae.getResponse().getEntity();
                int status = wae.getResponse().getStatus();
                dtoError = new DtoError(((entity != null) ? entity.toString() : wae.getMessage()),
                        ((status >= BAD_REQUEST_STATUS) ? status : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
            }
        } else if (e instanceof Failure) {
            // framework error
            Failure f = (Failure)e;
            if (f.getResponse() != null) {
                Object entity = f.getResponse().getEntity();
                int status = f.getResponse().getStatus();
                dtoError = new DtoError(((entity != null) ? entity.toString() : f.getMessage()),
                        ((status >= BAD_REQUEST_STATUS) ? status : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
            } else if (f.getErrorCode() >= BAD_REQUEST_STATUS) {
                dtoError = new DtoError(e.getMessage(), f.getErrorCode());
            }
        } else if (e instanceof JsonProcessingException) {
            // JSON post data processing error
            dtoError = new DtoError(e.getMessage(), BAD_REQUEST_STATUS);
        } else if (e instanceof UnmarshalException) {
            // XML post data processing error
            if (e.getMessage() != null) {
                dtoError = new DtoError(e.getMessage(), BAD_REQUEST_STATUS);
            } else {
                dtoError = new DtoError("Unable to unmarshal XML post data", BAD_REQUEST_STATUS);
            }
        }
        if (dtoError == null) {
            // internal error
            dtoError = new DtoError(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return Response.status(dtoError.getStatus()).entity(dtoError).build();
    }
}
