/*
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

package org.groundwork.cloudhub.configuration;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Icinga2Connection
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "icinga2")
@XmlType(propOrder = {"server", "port", "username", "password", "trustSSLCACertificate", "trustSSLCACertificateKeystore",
        "trustSSLCACertificateKeystorePassword", "trustAllSSL", "metricsGraphed"})
public class Icinga2Connection extends BaseMonitorConnection implements SecureMonitorConnection {

    @Pattern(regexp="^[0-9]{2,4}$", message="Not a valid port number.")
    private String port = "5665";

    @NotNull
    private String username;

    @NotNull
    private String password;

    private String trustSSLCACertificate;

    private String trustSSLCACertificateKeystore;

    private String trustSSLCACertificateKeystorePassword;

    private boolean trustAllSSL;

    private boolean metricsGraphed = true;

    public Icinga2Connection() {
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    @XmlTransient
    public boolean isSslEnabled() {
        return true;
    }

    @Override
    public void setSslEnabled(boolean sslEnabled) {
        throw new RuntimeException("Cannot set SSL enabled connection configuration");
    }

    public String getTrustSSLCACertificate() {
        return trustSSLCACertificate;
    }

    public void setTrustSSLCACertificate(String trustSSLCACertificate) {
        this.trustSSLCACertificate = trustSSLCACertificate;
    }

    public String getTrustSSLCACertificateKeystore() {
        return trustSSLCACertificateKeystore;
    }

    public void setTrustSSLCACertificateKeystore(String trustSSLCACertificateKeystore) {
        this.trustSSLCACertificateKeystore = trustSSLCACertificateKeystore;
    }

    public String getTrustSSLCACertificateKeystorePassword() {
        return trustSSLCACertificateKeystorePassword;
    }

    public void setTrustSSLCACertificateKeystorePassword(String trustSSLCACertificateKeystorePassword) {
        this.trustSSLCACertificateKeystorePassword = trustSSLCACertificateKeystorePassword;
    }

    public boolean isTrustAllSSL() {
        return trustAllSSL;
    }

    public void setTrustAllSSL(boolean trustAllSSL) {
        this.trustAllSSL = trustAllSSL;
    }

    public boolean isMetricsGraphed() {
        return metricsGraphed;
    }

    public void setMetricsGraphed(boolean metricsGraphed) {
        this.metricsGraphed = metricsGraphed;
    }
}
