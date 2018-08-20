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

package org.groundwork.rs.opentsdb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.groundwork.rs.client.ObjectMapperContextResolver;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * OpenTSDBClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class OpenTSDBClient {

    private static final Log log = LogFactory.getLog(OpenTSDBClient.class);

    private static final int HTTP_CONNECTION_TIMEOUT_MS = 10000;
    private static final int HTTP_READ_TIMEOUT_MS = 0; // 0 is infinite
    private static final GenericType<JsonNode> JSON_TYPE = new GenericType<JsonNode>(){};
    private static final GenericType<String> STRING_TYPE = new GenericType<String>(){};

    private static final ThreadLocal<RestClientRequestContext> contextsCache =
            new ThreadLocal<RestClientRequestContext>();
    private static final ObjectMapper errorMapper = new ObjectMapper();

    private static String openTSDBHost = OpenTSDBConfiguration.getOpenTSDBHost();
    private static int openTSDBPort = OpenTSDBConfiguration.getOpenTSDBPort();

    /**
     * Query performance data from OpenTSDB.
     *
     * @param appType application type or null for all, (source tag value)
     * @param serverName service name, (hostname tag value)
     * @param serviceName service name, (metric name)
     * @param startTime query start time, (millis since epoch)
     * @param endTime  query end time, (millis since epoch)
     * @param interval query downsample interval, (millis)
     * @return JSON query results or null on error
     */
    public static JsonNode queryOpenTSDBPerfData(String appType, String serverName, String serviceName,
                                                 long startTime, long endTime, long interval) {
        ClientResponse<JsonNode> response = null;
        try {
            // OpenTSDB query request: start, end, millisecond resolution, interval/downsample,
            // and serviceName/metric name form the basic query; appType/source tag is an
            // optional filter; serverName/hostname tag is a required filter; returns multiple
            // time series for each valuetype tag
            String url = "http://"+openTSDBHost+":"+openTSDBPort+"/api/query?"+
                    "start="+startTime+"&end="+endTime+"&ms=true&"+
                    "m=avg:"+interval+"ms-avg:"+encode(serviceName)+
                    "%7B"+
                    ((appType != null) ? "source="+encode(appType)+"," : "")+
                    "hostname="+encode(serverName)+","+
                    "valuetype=*"+
                    "%7D";
            ClientRequest request = createClientRequest(url);
            request.accept(MediaType.APPLICATION_JSON_TYPE);
            response = request.get(JSON_TYPE);
            // return OpenTSDB query or error response
            Response.Status status = response.getResponseStatus();
            if (status == Response.Status.OK) {
                return response.getEntity(JSON_TYPE);
            } else {
                // log error response
                log.error(String.format("Perf data OpenTSDB query returned with status code of %d, reason: %s",
                        status.getStatusCode(), status.getReasonPhrase()));
                // log error OpenTSDB query response
                String responseContentType = response.getHeaders().getFirst("Content-Type");
                if (responseContentType != null) {
                    if (responseContentType.startsWith(MediaType.APPLICATION_JSON)) {
                        JsonNode jsonResponse = response.getEntity(JSON_TYPE);
                        if (jsonResponse != null) {
                            JsonNode errorResponse = jsonResponse.get("error");
                            if ((errorResponse != null) && errorResponse.isObject()) {
                                JsonNode messageResponse = jsonResponse.get("message");
                                if ((messageResponse != null) && messageResponse.isTextual()) {
                                    log.error(String.format("Perf data OpenTSDB query error: %s",
                                            messageResponse.getTextValue()));
                                }
                            }
                        }
                    } else if (responseContentType.startsWith(MediaType.TEXT_HTML)) {
                        String htmlResponse = response.getEntity(STRING_TYPE);
                        if (htmlResponse != null) {
                            String message = htmlResponse.replaceAll("[\\r\\n]+", " ").replaceAll("<[^>]*>", " ").
                                    replaceAll("&[a-z]+;", " ").replaceAll("\\s+", " ").trim();
                            log.error(String.format("Perf data OpenTSDB query request failed: %s", message));
                        }
                    }
                }
                return null;
            }
        } catch (Exception e) {
            log.error("Unable to query OpenTSDB perf data: "+e, e);
            return null;
        } finally {
            // cleanup OpenTSDB query response
            if (response != null) {
                response.releaseConnection();
            }
        }
    }

    /**
     * Put performance data to OpenTSDB.
     *
     * @param perfDataPoints performance data points
     */
    public static void putOpenTSDBPerfData(JsonNode perfDataPoints) {
        ClientResponse<JsonNode> response = null;
        try {
            // OpenTSDB data points put request with details
            String url = "http://"+openTSDBHost+":"+openTSDBPort+"/api/put?details";
            ClientRequest request = createClientRequest(url);
            request.accept(MediaType.APPLICATION_JSON_TYPE);
            request.body(MediaType.APPLICATION_JSON_TYPE, perfDataPoints);
            response = request.post(JSON_TYPE);
            // verify OpenTSDB put response
            Response.Status status = response.getResponseStatus();
            if ((status != Response.Status.NO_CONTENT) && (status != Response.Status.OK)) {
                // log error response
                log.error(String.format("Perf data OpenTSDB put returned with status code of %d, reason: %s",
                        status.getStatusCode(), status.getReasonPhrase()));
                // log failed/errors OpenTSDB put response
                String responseContentType = response.getHeaders().getFirst("Content-Type");
                if (responseContentType != null) {
                    if (responseContentType.startsWith(MediaType.APPLICATION_JSON)) {
                        JsonNode jsonResponse = response.getEntity(JSON_TYPE);
                        if (jsonResponse != null) {
                            JsonNode successResponse = jsonResponse.get("success");
                            int success = ((successResponse != null) ? successResponse.asInt() : 0);
                            JsonNode failedResponse = jsonResponse.get("failed");
                            int failed = ((failedResponse != null) ? failedResponse.asInt() : 0);
                            if (failed > 0) {
                                log.error(String.format("Perf data OpenTSDB puts failed %d of %d", failed, success + failed));
                                JsonNode errorsResponse = jsonResponse.get("errors");
                                if (errorsResponse instanceof ArrayNode) {
                                    for (Iterator<JsonNode> errorsIter = errorsResponse.iterator(); errorsIter.hasNext(); ) {
                                        log.error(String.format("Perf data OpenTSDB put error: %s",
                                                errorMapper.writeValueAsString(errorsIter.next())));
                                    }
                                }
                            }
                        }
                    } else if (responseContentType.startsWith(MediaType.TEXT_HTML)) {
                        String htmlResponse = response.getEntity(STRING_TYPE);
                        if (htmlResponse != null) {
                            String message = htmlResponse.replaceAll("[\\r\\n]+", " ").replaceAll("<[^>]*>", " ").
                                    replaceAll("&[a-z]+;", " ").replaceAll("\\s+", " ").trim();
                            log.error(String.format("Perf data OpenTSDB puts request failed: %s", message));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to write OpenTSDB perf data: "+e, e);
        } finally {
            // cleanup OpenTSDB put response
            if (response != null) {
                response.releaseConnection();
            }
        }
    }

    /**
     * Create client request for url. Utilizes thread local cached
     * request context.
     *
     * @param url request url
     * @return client request for url
     */
    private static ClientRequest createClientRequest(String url) {
        // get or create REST client request context for thread
        RestClientRequestContext context = contextsCache.get();
        if (context == null) {
            context = new RestClientRequestContext();
            contextsCache.set(context);
        }
        // create client request
        return context.newClientRequest(url);
    }

    /**
     * Shutdown and release thread local cached resources.
     */
    public static void shutdown() {
        // close and remove client executor for thread
        RestClientRequestContext context = contextsCache.get();
        if (context != null) {
            context.close();
            contextsCache.remove();
        }
    }

    /**
     * Class to encapsulate REST client request creation context. Intended to
     * cached and reused per thread.
     */
    private static class RestClientRequestContext {

        public ApacheHttpClient4Executor executor;
        public ClientRequestFactory factory;

        /**
         * Construct and configure context executor and request factory.
         */
        public RestClientRequestContext() {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECTION_TIMEOUT_MS);
            HttpConnectionParams.setSoTimeout(httpParams, HTTP_READ_TIMEOUT_MS);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            this.executor = new ApacheHttpClient4Executor(httpClient);
            ResteasyProviderFactory providerFactory = ResteasyProviderFactory.getInstance();
            providerFactory.addContextResolver(ObjectMapperContextResolver.class);
            this.factory = new ClientRequestFactory(this.executor, providerFactory);
            this.factory.setFollowRedirects(true);
        }

        /**
         * Create new client request for specified url in this context.
         *
         * @param url request url
         * @return client request
         */
        public ClientRequest newClientRequest(String url) {
            return factory.createRequest(url);
        }

        /**
         * Close context.
         */
        public void close() {
            try {
                if (!executor.isClosed()) {
                    executor.close();
                }
            } catch (Exception e) {
            }
        }

        /**
         * Finalize: close context.
         */
        public void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }

    /**
     * UTF-8 URL encode a string
     *
     * @param param the string to be encoded
     * @return the encoded string
     * @throws UnsupportedEncodingException
     */
    private static String encode(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8").replace("+", "%20");
    }

    /**
     * Clean metric name and tag key/values replacing illegal characters for
     * OpenTSDB with underscores. Must be kept in sync with client-side code
     * performing same operation, (see GroundWorkOpenTSDBDatasource).
     *
     * @param nameKey input name or tag key/value
     * @return clean name or tag key/value
     */
    public static String cleanNameKey(String nameKey) {
        return nameKey.replaceAll("[^-a-zA-Z0-9_./]", "_");
    }

    /**
     * Clean tag value list elements replacing illegal characters for
     * OpenTSDB with underscores. Uses cleanNameKey() but also reserves the
     * legal slash character, ('/'), to separate list elements.
     *
     * @param nameKey input tag value list element
     * @return clean tag value list element
     */
    public static String cleanNameKeyListElement(String nameKey) {
        return cleanNameKey(nameKey).replaceAll("/", "_");
    }
}
