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

package org.groundwork.cloudhub.connectors.client;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.icinga2.client.Icinga2AuthClient;
import org.junit.Test;

import java.io.File;

/**
 * Icinga2AuthClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Icinga2AuthClientTest {

    private static Logger log = Logger.getLogger(Icinga2AuthClientTest.class);

    private String host = "demo70.groundwork.groundworkopensource.com";
    private int port = 5665;
    private String user = "root";
    private String password = "fc764746b29dfa82";
    private File trustSSLCACertificate = new File("./src/test/testdata/icinga2/ca.crt");
    private File trustSSLCACertificateKeystore = new File("./src/test/testdata/icinga2/icinga2-keystore.jks");
    private String trustSSLCACertificateKeystorePassword = "icinga2";
    private boolean trustAllSSL = true;

    @Test
    public void testIcinga2AuthClientCACert() throws Exception {
        log.debug("Create CA cert SSL authentication client...");
        assert trustSSLCACertificate.isFile() && trustSSLCACertificate.canRead();
        Icinga2AuthClient client = new Icinga2AuthClient(host, port, user, password, trustSSLCACertificate, null, null,
                false);
        log.debug("Test authentication...");
        assert client.testAPIAuthentication();
    }

    @Test
    public void testIcinga2AuthClientCACertKeystore() throws Exception {
        log.debug("Create CA cert keystore SSL authentication client...");
        assert trustSSLCACertificateKeystore.isFile() && trustSSLCACertificateKeystore.canRead();
        Icinga2AuthClient client = new Icinga2AuthClient(host, port, user, password, null,
                trustSSLCACertificateKeystore, trustSSLCACertificateKeystorePassword, false);
        log.debug("Test authentication...");
        assert client.testAPIAuthentication();
    }

    @Test
    public void testIcinga2AuthClientAll() throws Exception {
        log.debug("Create all SSL authentication client...");
        Icinga2AuthClient client = new Icinga2AuthClient(host, port, user, password, null, null, null, trustAllSSL);
        log.debug("Test authentication...");
        assert client.testAPIAuthentication();
    }

    @Test
    public void testIcinga2AuthClientFailReachable() throws Exception {
        log.debug("Create all SSL authentication client...");
        Icinga2AuthClient client = new Icinga2AuthClient("not-host", 9999, "not-user", "not-password", null, null, null, trustAllSSL);
        log.debug("Test authentication...");
        assert !client.testAPIAuthentication();
    }

    @Test
    public void testIcinga2AuthClientFailService() throws Exception {
        log.debug("Create all SSL authentication client...");
        Icinga2AuthClient client = new Icinga2AuthClient("localhost", 9999, "not-user", "not-password", null, null, null, trustAllSSL);
        log.debug("Test authentication...");
        assert !client.testAPIAuthentication();
    }

    @Test
    public void testIcinga2AuthClientFailUser() throws Exception {
        log.debug("Create all SSL authentication client...");
        Icinga2AuthClient client = new Icinga2AuthClient(host, port, "not-user", "not-password", null, null, null, trustAllSSL);
        log.debug("Test authentication...");
        assert !client.testAPIAuthentication();
    }

    @Test
    public void testIcinga2AuthClientFailPassword() throws Exception {
        log.debug("Create all SSL authentication client...");
        Icinga2AuthClient client = new Icinga2AuthClient(host, port, user, "not-password", null, null, null, trustAllSSL);
        log.debug("Test authentication...");
        assert !client.testAPIAuthentication();
    }
}
