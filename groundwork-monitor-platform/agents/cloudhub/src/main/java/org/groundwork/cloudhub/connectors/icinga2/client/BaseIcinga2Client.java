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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.groundwork.cloudhub.configuration.Icinga2Connection;
import org.groundwork.cloudhub.connectors.base.BaseConnectorClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GenericType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseIcinga2Client
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public abstract class BaseIcinga2Client extends BaseConnectorClient {

    protected static final int HTTP_CONNECTION_TIMEOUT_MS = 20000; // 20 seconds
    protected static final int HTTP_READ_TIMEOUT_MS = 60000; // 60 seconds

    protected static ThreadLocal<Map<String,RestClientRequestContext>> contextsCache =
            new ThreadLocal<Map<String,RestClientRequestContext>>();

    private String server;
    private int port;
    private String user;
    private String password;
    private File trustSSLCACertificate;
    private File trustSSLCACertificateKeystore;
    private String trustSSLCACertificateKeystorePassword;
    private boolean trustAllSSL;
    private String contextKey;

    /**
     * Construct new Icinga2 client.
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
    public BaseIcinga2Client(String server, int port, String user, String password, File trustSSLCACertificate,
                             File trustSSLCACertificateKeystore, String trustSSLCACertificateKeystorePassword,
                             boolean trustAllSSL) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        this.contextKey = server + "|" + port + "|" + user + "|" + password ;
        if ((trustSSLCACertificate != null) && trustSSLCACertificate.isFile() && trustSSLCACertificate.canRead()) {
            this.trustSSLCACertificate = trustSSLCACertificate;
            this.contextKey += "|" + trustSSLCACertificate.getAbsolutePath();
        } else if ((trustSSLCACertificateKeystore != null) && trustSSLCACertificateKeystore.isFile() &&
                trustSSLCACertificateKeystore.canRead() && (trustSSLCACertificateKeystorePassword != null)) {
            this.trustSSLCACertificateKeystore = trustSSLCACertificateKeystore;
            this.trustSSLCACertificateKeystorePassword = trustSSLCACertificateKeystorePassword;
            this.contextKey += "|" + trustSSLCACertificateKeystore.getAbsolutePath();
        } else {
            this.trustAllSSL = trustAllSSL;
            this.contextKey += "|" + trustAllSSL;
        }
    }

    /**
     * Construct new Icinga2 client from connnection configuration.
     *
     * @param connection connection configuration
     */
    public BaseIcinga2Client(Icinga2Connection connection) {
        this(connection.getServer(), Integer.parseInt(connection.getPort()), connection.getUsername(), connection.getPassword(),
                ((connection.getTrustSSLCACertificate() != null) ? new File(connection.getTrustSSLCACertificate()) : null),
                ((connection.getTrustSSLCACertificateKeystore() != null) ? new File(connection.getTrustSSLCACertificateKeystore()) : null),
                connection.getTrustSSLCACertificateKeystorePassword(), connection.isTrustAllSSL());
    }

    /**
     * UTF-8 encode a string
     *
     * @param param the string to be encoded
     * @return the encoded string
     */
    protected String encode(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Build a full URL from a relative path and the base deployment URL configured on
     * this client
     *
     * @param apiRoot the relative api path
     * @return a full URL
     */
    protected String build(String apiRoot) {
        return joinApiPath("https://" + server + ":" + port, apiRoot);
    }

    /**
     * Build a full URL from a relative API path, additional path information, and the base
     * deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path additional path
     * @return a full URL
     */
    protected String buildUrlWithPath(String apiRoot, String path) {
        return (!isEmpty(path) ? joinApiPath(build(apiRoot), path) : build(apiRoot));
    }

    /**
     * Build a full URL from a relative API path, additional pre-encoded query parameters,
     * and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param encodedQueryParams the pre-encoded query parameters to append to the final URL
     * @return a full URL
     */
    protected String buildUrlWithQueryParams(String apiRoot, String encodedQueryParams) {
        String url = build(apiRoot);
        if (encodedQueryParams != null) {
            url += encodedQueryParams;
        }
        return url.toString();
    }

    /**
     * Build a full URL from a relative API path, additional path information, additional
     * pre-encoded query parameters, and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path additional path
     * @param encodedQueryParams the pre-encoded query parameters to append to the final URL
     * @return a full URL
     */
    protected String buildUrlWithPathAndQueryParams(String apiRoot, String path, String encodedQueryParams) {
        String url = buildUrlWithPath(apiRoot, path);
        if (encodedQueryParams != null) {
            url += encodedQueryParams;
        }
        return url.toString();
    }

    /**
     * Build an encoded set of query parameters given an array of names and values
     *
     * @param names the query parameter names
     * @param values the query parameter values
     * @return an encoded string of all query parameter names and values
     */
    protected String buildEncodedQueryParams(String[] names, String[] values) {
        if (names == null || values == null || names.length != values.length)
            return null;
        StringBuilder queryParams = new StringBuilder();
        String paramDelim = "?";
        int count = 0;
        for (int index = 0; index < names.length; index++) {
            if (values[index] != null) {
                queryParams.append(paramDelim);
                paramDelim = "&";
                queryParams.append(encode(names[index]));
                queryParams.append("=");
                queryParams.append(encode(values[index]));
                count++;
            }
        }
        return (count == 0) ? null : queryParams.toString();
    }

    /**
     * Create client request.
     *
     * @param url client request url
     * @return client request
     */
    protected ClientRequest createClientRequest(String url) {
        // get or create REST client request contexts for thread
        Map<String,RestClientRequestContext> contexts = contextsCache.get();
        if (contexts == null) {
            contexts = new HashMap<String,RestClientRequestContext>();
            contextsCache.set(contexts);
        }
        RestClientRequestContext context = contexts.get(contextKey);
        if (context == null) {
            context = new RestClientRequestContext(server, port, user, password, trustSSLCACertificate,
                    trustSSLCACertificateKeystore, trustSSLCACertificateKeystorePassword, trustAllSSL);
            contexts.put(contextKey, context);
        }
        return context.newClientRequest(url);
    }

    /**
     * Make client request.
     *
     * @param url request url
     * @param description request description used in exception
     * @param type request type
     * @return return type instance or null if not found
     */
    protected <T> T clientRequest(String url, String description, GenericType<T> type) {
        return clientRequest("GET", url, description, type);
    }

    /**
     * Make client request.
     *
     * @param method HTTP request method
     * @param url request url
     * @param description request description used in exception
     * @param type request type, (use new GenericType<Void>(){} for no return)
     * @return return type instance or null if not found
     */
    protected <T> T clientRequest(String method, String url, String description, GenericType<T> type) {
        return clientRequest(method, url, null, description, type);
    }

    /**
     * Make client request.
     *
     * @param method HTTP request method
     * @param url request url
     * @param body request body object or null
     * @param description request description used in exception
     * @param type request type, (use new GenericType<Void>(){} for no return)
     * @return return type instance or null if not found
     */
    protected <T> T clientRequest(String method, String url, Object body, String description, GenericType<T> type) {
        Response.Status status = null;
        ClientResponse<T> response = null;
        try {
            ClientRequest request = createClientRequest(url);
            if (type.getGenericType() != Void.class) {
                request.accept(MediaType.APPLICATION_JSON_TYPE);
            }
            if (body != null) {
                request.body(MediaType.APPLICATION_JSON_TYPE, body);
            }
            response = request.httpMethod(method, type);
            if (response.getResponseStatus() == Response.Status.OK) {
                if (type.getGenericType() != Void.class) {
                    return response.getEntity(type);
                } else {
                    return null;
                }
            } else if ((response.getResponseStatus() == Response.Status.NOT_FOUND) ||
                    (response.getResponseStatus() == Response.Status.NO_CONTENT)) {
                return null;
            }
            status = response.getResponseStatus();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        status = ((status != null) ? status : Response.Status.SERVICE_UNAVAILABLE);
        throw new RuntimeException(String.format("Exception executing %s with status code of %d, reason: %s",
                description, status.getStatusCode(), status.getReasonPhrase()));
    }

    /**
     * Create configured HTTP client for raw HTTP streaming APIs.

     * @param setReadTimeout set read timeout
     * @return HTTP client instance.
     */
    public HttpClient createHttpClient(boolean setReadTimeout) {
        // return configured http client
        return createHttpClient(server, port, user, password, trustSSLCACertificate, trustSSLCACertificateKeystore,
                trustSSLCACertificateKeystorePassword, trustAllSSL, setReadTimeout);
    }

    /**
     * Shutdown and release thread local cached resources.
     */
    public static void shutdown() {
        // close and remove client executors for thread
        Map<String,RestClientRequestContext> contexts = contextsCache.get();
        if (contexts != null) {
            for (RestClientRequestContext context : contexts.values()) {
                context.close();
            }
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
        public RestClientRequestContext(String server, int port, String user, String password,
                                        File trustSSLCACertificate, File trustSSLCACertificateKeystore,
                                        String trustSSLCACertificateKeystorePassword, boolean trustAllSSL) {
            HttpClient httpClient = createHttpClient(server, port, user, password, trustSSLCACertificate,
                    trustSSLCACertificateKeystore, trustSSLCACertificateKeystorePassword, trustAllSSL, true);
            this.executor = new ApacheHttpClient4Executor(httpClient);
            // client request factory provider factory
            ResteasyProviderFactory providerFactory = new ResteasyProviderFactory();
            // jackson ObjectMapper provider using custom configured ObjectMapper
            ContextResolver<ObjectMapper> objectMapperContextResolver = new Icinga2ObjectMapperContextResolver();
            providerFactory.addContextResolver(objectMapperContextResolver);
            // jackson message body reader/writer JSON provider using custom configured ObjectMapper
            JacksonJsonProvider jsonProvider = new JacksonJsonProvider(objectMapperContextResolver.getContext(null));
            providerFactory.addMessageBodyReader(jsonProvider);
            providerFactory.addMessageBodyWriter(jsonProvider);
            // set client request factory
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
                executor.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Create configured HTTP client for use with REST client request context and raw HTTP streaming APIs.
     *
     * @param server server host name
     * @param port server port
     * @param user authentication user
     * @param password authentication password
     * @param trustSSLCACertificate trusted SSL CA certificate
     * @param trustSSLCACertificateKeystore trusted SSL CA certificate keystore
     * @param trustSSLCACertificateKeystorePassword trusted SSL CA certificate keystore password
     * @param trustAllSSL trust all SSL certificates
     * @param setReadTimeout set read timeout
     * @return HTTP client instance.
     */
    public static HttpClient createHttpClient(String server, int port, String user, String password,
                                              File trustSSLCACertificate, File trustSSLCACertificateKeystore,
                                              String trustSSLCACertificateKeystorePassword, boolean trustAllSSL,
                                              boolean setReadTimeout) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECTION_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(httpParams, setReadTimeout ? HTTP_READ_TIMEOUT_MS : 0);
        DefaultHttpClient httpClient;
        if ((trustSSLCACertificate != null) || (trustSSLCACertificateKeystore != null) || trustAllSSL) {
            try {
                // http client with custom SSL socket
                SSLSocketFactory factory;
                if ((trustSSLCACertificate != null) || (trustSSLCACertificateKeystore != null)) {
                    // load CA certificate keystore
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    if (trustSSLCACertificate != null) {
                        // load CA certificate
                        String caCertificatePEM =
                                new String(Files.readAllBytes(Paths.get(trustSSLCACertificate.getAbsolutePath())));
                        int pemBeginIndex = caCertificatePEM.indexOf("-----BEGIN CERTIFICATE-----");
                        int pemEndIndex = caCertificatePEM.indexOf("-----END CERTIFICATE-----");
                        if ((pemBeginIndex == -1) || (pemEndIndex < pemBeginIndex)) {
                            throw new RuntimeException("PEM certificate required "+trustSSLCACertificate);
                        }
                        byte [] caCertificateDER = DatatypeConverter.parseBase64Binary(
                                caCertificatePEM.substring(pemBeginIndex + 27, pemEndIndex));
                        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                        X509Certificate caCertificate = (X509Certificate)
                                certificateFactory.generateCertificate(new ByteArrayInputStream(caCertificateDER));
                        // create CA certificate keystore
                        keyStore.load(null);
                        keyStore.setCertificateEntry("root", caCertificate);
                    } else {
                        // load CA certificate keystore
                        char[] keyStorePassword = trustSSLCACertificateKeystorePassword.toCharArray();
                        InputStream keyStoreInputStream = null;
                        try {
                            keyStoreInputStream = new FileInputStream(trustSSLCACertificateKeystore);
                            keyStore.load(keyStoreInputStream, keyStorePassword);
                        } finally {
                            if (keyStoreInputStream != null) {
                                keyStoreInputStream.close();
                            }
                        }
                    }
                    // trust CA certificate keystore
                    TrustManagerFactory trustManagerFactory =
                            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(keyStore);
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
                    factory = new SSLSocketFactory(sslContext);
                } else {
                    // trust all SSL certificates
                    TrustStrategy trustStrategy = new TrustStrategy() {
                        @Override
                        public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            return true;
                        }
                    };
                    factory = new SSLSocketFactory(trustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                }
                // register custom SSL socket factory for connection
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("https", port, factory));
                BasicClientConnectionManager manager = new BasicClientConnectionManager(registry);
                httpClient = new DefaultHttpClient(manager, httpParams);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception setting up trust all SSL certificates: "+e, e);
            }
        } else {
            // default http client
            httpClient = new DefaultHttpClient(httpParams);
        }
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(server, port),
                new UsernamePasswordCredentials(user, password));
        return httpClient;
    }
}
