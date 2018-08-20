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

package com.groundwork.core.security.ldap;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

/**
 * LDAPSocketFactory
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPSocketFactory extends SocketFactory {

    public static final String FACTORY_SOCKET_CONTEXT_PARAM = "java.naming.ldap.factory.socket";

    private static final boolean IS_JDK_17 = System.getProperty("java.version").startsWith("1.7");

    private LDAPConfig config;
    private SSLSocketFactory sslSocketFactory;

    /**
     * SocketFactory provider class used for InitialLdapContext connections. LDAP configuration
     * is accessed from the calling LDAPAggregator TLS in constructor since class provider pattern
     * used by InitialLdapContext does not accept parameters.
     */
    public LDAPSocketFactory() {
        this.config = LDAPAggregator.getLDAPConfigTLS();
        this.sslSocketFactory = null;
        if ("ssl".equalsIgnoreCase(config.getSecurityProtocol()) || config.getProviderURL().toLowerCase().startsWith("ldaps://")) {
            this.sslSocketFactory = createSSLSocketFactory(this.config);
        }
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return (sslSocketFactory != null) ?
                sslSocketFactory.createSocket(host, port) :
                new Socket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return (sslSocketFactory != null) ?
                sslSocketFactory.createSocket(host, port, localHost, localPort) :
                new Socket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return (sslSocketFactory != null) ?
                sslSocketFactory.createSocket(host, port) :
                new Socket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return (sslSocketFactory != null) ?
                sslSocketFactory.createSocket(address, port, localAddress, localPort) :
                new Socket(address, port, localAddress, localPort);
    }

    /**
     * Construct configured SSL SocketFactory based on LDAP configuration.
     *
     * @param config LDAP configuration
     * @return SSL socket factory
     */
    public static SSLSocketFactory createSSLSocketFactory(LDAPConfig config) {
        try {
            // configure SSL context based on LDAP configuration; note: TLSv1.2 must be
            // explicitly enabled for JDK 1.7. Is not required for JDK 1.8+.
            SSLContext ctx = IS_JDK_17 ? SSLContext.getInstance("TLSv1.2") : SSLContext.getInstance("TLS");
            KeyManager[] trustStoreKeyManagers = null;
            TrustManager[] trustStoreTrustManagers = null;
            if (config.getTrustStore() != null && config.getTrustStore().length() > 0) {
                char[] keyPassword = null;
                if (config.getTrustStorePassword() != null && config.getTrustStorePassword().length() > 0) {
                    keyPassword = config.getTrustStorePassword().toCharArray();
                }
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                FileInputStream keyFile = new FileInputStream(config.getTrustStore());
                keyStore.load(keyFile, keyPassword);
                keyFile.close();
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, keyPassword);
                trustStoreKeyManagers = keyManagerFactory.getKeyManagers();
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                trustStoreTrustManagers = trustManagerFactory.getTrustManagers();
            }
            ctx.init(trustStoreKeyManagers, trustStoreTrustManagers, null);
            // return SSL socket factory for SSL context
            return ctx.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Unable to setup LDAP SSL socket factory: "+e, e);
        }
    }

    /**
     * Default provider contract override.
     *
     * @return socket factory
     */
    public static SocketFactory getDefault() {
        return new LDAPSocketFactory();
    }
}
