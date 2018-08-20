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

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.List;

/**
 * CacheControlResponseFilter - adds HTTP Cache-Control headers to REST API endpoints.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Provider
@ServerInterceptor
public class CacheControlResponseFilter implements PostProcessInterceptor {

    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String CACHE_CONTROL_NO_CACHE = "no-cache";
    private static final String CACHE_CONTROL_NO_STORE = "no-store";

    @Override
    public void postProcess(ServerResponse serverResponse) {
        // add Cache-Control no-cache no-store headers to all responses
        // if they are not explicitly set already
        MultivaluedMap<String, Object> headers = serverResponse.getMetadata();
        List<Object> cacheControlHeaders = headers.get(CACHE_CONTROL);
        if (cacheControlHeaders == null) {
            cacheControlHeaders = Arrays.asList(new Object[]{CACHE_CONTROL_NO_CACHE+", "+CACHE_CONTROL_NO_STORE});
            headers.put(CACHE_CONTROL, cacheControlHeaders);
        }
    }
}
