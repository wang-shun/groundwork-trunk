/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2016  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.agents.appservers.collector.servlet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.URLConnection;

/**
 * TLSV12ClientConfiguration
 *
 * TLSv1.2 client configuration utilities for Groundwork Services and
 * APIs required for JDK 1.7.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @author <a href="mailto:rgraffy@gwos.com">Ryan Gaffy</a>
 * @version $Id:$
 */
public class TLSV12ClientConfiguration {

    private static final boolean IS_JDK_17 = System.getProperty("java.version").startsWith("1.7");

    /**
     * Configure URLConnection for SSL/HTTPS connection to Groundwork Services and
     * APIs. URLConnections are normally obtained from URL.openConnection().
     * Does nothing if connection is not HTTPS.
     *
     * TLSv1.2 must be explicitly enabled for JDK 1.7. Is not required for JDK 1.8+.
     *
     * @param connection URLConnection to configure
     */
    public static void configure(URLConnection connection) {
        if (IS_JDK_17 && (connection instanceof HttpsURLConnection)) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLSv1.2");
                ctx.init(null, null, null);
                ((HttpsURLConnection) connection).setSSLSocketFactory(ctx.getSocketFactory());
            } catch (Exception e) {
                throw new RuntimeException("Unable to setup SSL TLSv1.2 connection", e);
            }
        }
    }
}
