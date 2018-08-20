/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2017  GroundWork Open Source Solutions info@groundworkopensource.com

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

import com.groundwork.core.security.LDAPHelper;
import org.groundwork.foundation.ws.impl.JasyptUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * LDAPAuthResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/ldapauth")
public class LDAPAuthResource extends AbstractResource {

    /**
     * Validate LDAP security credentials.
     *
     * @param domain LDAP security domain or null to validate against the default domain
     * @param principalDN LDAP security principal DN
     * @param credential encrypted LDAP security credential
     */
    @GET
    @Path("/validatecredentials")
    @Produces(MediaType.TEXT_PLAIN)
    public void validateCredentials(@QueryParam("domain") String domain,
                                    @QueryParam("principalDN") String principalDN,
                                    @QueryParam("credential") String credential) {
        if (log.isDebugEnabled()) {
            log.debug("processing /GET on /ldapauth/validatecredentials");
        }
        try {
            // validate parameters
            domain = (domain != null && !domain.equals("") ? domain : null);
            if (principalDN == null || principalDN.equals("") || credential == null || credential.equals("")) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
            }
            // decrypt credential
            credential = JasyptUtils.jasyptDecrypt(credential);
            // validate security credentials against domain
            if (!LDAPHelper.getInstance().isLDAP() || !LDAPHelper.getInstance().validateSecurityCredentials(domain, principalDN, credential)) {
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
            }
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for LDAP auth.").build());
        }
    }
}
