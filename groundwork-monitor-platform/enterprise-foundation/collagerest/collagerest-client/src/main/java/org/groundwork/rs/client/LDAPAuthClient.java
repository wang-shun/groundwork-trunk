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

package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

/**
 * LDAPAuthClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPAuthClient extends BaseRestClient {
    private static Log log = LogFactory.getLog(LicenseClient.class);

    private static final String API_ROOT_SINGLE = "/ldapauth";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a License REST Client for performing LDAP authentication operations
     * in the Groundwork enterprise foundation server.
     * <p></p>
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public LDAPAuthClient(String deploymentUrl) {
        super(deploymentUrl);
        this.mediaType = MediaType.TEXT_PLAIN_TYPE;
    }

    /**
     * Validate LDAP security credentials.
     *
     * @param domain LDAP security domain or null to validate against the default domain
     * @param principalDN LDAP security principal DN
     * @param credential LDAP security credential
     * @return valid
     * @throws CollageRestException
     */
    public boolean validateCredentials(String domain, String principalDN, String credential) throws CollageRestException {
        // validate parameters
        domain = (domain != null ? domain : "");
        if (principalDN == null || principalDN.equals("") || credential == null || credential.equals("")) {
            throw new IllegalArgumentException("principalDN or credential not specified");
        }
        // encrypt credential
        credential = JasyptUtils.jasyptEncrypt(credential);
        // validate credentials
        String encodedQueryParams;
        try {
            encodedQueryParams = buildEncodedQueryParams(new String[]{"domain", "principalDN", "credential"}, new String[]{domain, principalDN, credential});
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
        String requestUrl = buildUrlWithQueryParams(API_ROOT + "validatecredentials", encodedQueryParams);
        String requestDescription = String.format("validate LDAP credential for [%s]", principalDN);
        try {
            clientRequest(requestUrl, requestDescription, new GenericType<Void>(){});
            return true;
        } catch (CollageRestException cre) {
            if (cre.getStatus() != Response.Status.FORBIDDEN.getStatusCode()) {
                throw cre;
            }
            return false;
        }
    }
}
