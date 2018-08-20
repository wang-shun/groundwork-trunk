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

package org.groundwork.cloudhub.connectors.icinga2.client;

import org.groundwork.cloudhub.configuration.Icinga2Connection;
import org.jboss.resteasy.util.GenericType;

import java.io.File;

/**
 * Icinga2InventoryClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2AuthClient extends BaseIcinga2Client {

    private static final String API = "/v1";

    /**
     * Construct Icinga2 authentication client.
     *
     * @param server server host name
     * @param port server port
     * @param user authentication user
     * @param password authentication password
     * @param trustSSLCACertificate trusted SSL CA certificate
     * @param trustSSLCACertificateKeystore trusted SSL CA certificate keystore
     * @param trustSSLCACertificateKeystorePassword trusted SSL CA certificate keystore password
     * @param trustAllSSL trust all SSL certificates
     */
    public Icinga2AuthClient(String server, int port, String user, String password, File trustSSLCACertificate,
                             File trustSSLCACertificateKeystore, String trustSSLCACertificateKeystorePassword,
                             boolean trustAllSSL) {
        super(server, port, user, password, trustSSLCACertificate, trustSSLCACertificateKeystore,
                trustSSLCACertificateKeystorePassword, trustAllSSL);
    }

    /**
     * Construct Icinga2 authentication client from connection configuration.
     *
     * @param connection connection configuration
     */
    public Icinga2AuthClient(Icinga2Connection connection) {
        super(connection);
    }

    /**
     * Test API authentication.
     *
     * @return authenticated
     */
    public boolean testAPIAuthentication() {
        String url = build(API);
        try {
            clientRequest(url, "Icinga2 api query", new GenericType<Void>(){});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
