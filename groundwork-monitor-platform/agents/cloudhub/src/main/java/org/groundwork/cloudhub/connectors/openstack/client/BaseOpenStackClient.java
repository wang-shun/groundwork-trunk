package org.groundwork.cloudhub.connectors.openstack.client;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.groundwork.cloudhub.connectors.base.BaseConnectorClient;
import org.groundwork.cloudhub.configuration.OpenStackConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.rs.client.ObjectMapperContextResolver;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class BaseOpenStackClient extends BaseConnectorClient {

    public static final String DEMO_BASE_URL = "http://agno.groundwork.groundworkopensource.com";

    public final static String RETRY_AUTH = "Retrying Authentication, token may have timed out ....";
    protected static final int RETRIES = 2;

    protected static final int HTTP_CONNECTION_TIMEOUT_MS = 20000; // 20 seconds
    protected static final int HTTP_READ_TIMEOUT_MS = 60000; // 60 seconds

    protected static final TokenSessionManager tokenSessionManager = new TokenSessionManager();

    /**  valid, supported internet media type (MIME) supported */
    protected MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    /** the deployment root URL for all Rest operations */
    protected final OpenStackConnection connection;

    /** REST client request contexts thread local cache */
    protected static ThreadLocal<RestClientRequestContext> contextsCache = new ThreadLocal<RestClientRequestContext>();

    private boolean isAuthRequest = false;


    /**
     * Create a Device REST Client for performing query and administrative operations
     * on the devices in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param connection the deployment connection info for all Rest operations
     */
    public BaseOpenStackClient(OpenStackConnection connection) {
        this(connection, false);
    }

    public BaseOpenStackClient(OpenStackConnection connection, boolean isAuthRequest) {
        this.connection = connection;
        this.isAuthRequest = isAuthRequest;
    }


    /**
     * All operations for this client will communicate with this media type
     * @return a valid, supported internet media type (MIME) supported
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * All operations for this client will communicate with this media type
     * @param mediaType  a valid, supported internet media type (MIME) supported
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * UTF-8 encode a string
     *
     * @param param the string to be encoded
     * @return the encoded string
     * @throws java.io.UnsupportedEncodingException
     */
    protected String encode(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8");
    }

    /**
     * Build a full URL from a relative path and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @return a full URL
     */
    protected String build(String apiRoot)  {
        String protocol = (connection.isSslEnabled()) ? "https://" : "http://";
        StringBuilder url = new StringBuilder();
        url.append(protocol);
        url.append(joinApiPath(connection.getServer(), apiRoot));
        return url.toString();
    }

    /**
     * Build a full URL from a relative API path, additional path information, additional pre-encoded query parameters,
     * and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param encodedQueryParams the pre-encoded query parameters to append to the final URL
     * @return a full URL
     */
    protected String buildUrlWithPathAndQueryParams(String apiRoot, String encodedQueryParams) {
        String url = build(apiRoot);
        if (encodedQueryParams != null)
            url += (encodedQueryParams);
        return url.toString();
    }


    /**
     * Build an encoded set of query parameters given an array of names and values
     *
     * @param names the query parameter names
     * @param values the query parameter values
     * @return an encoded string of all query parameter names and values
     * @throws java.io.UnsupportedEncodingException
     */
    protected String buildEncodedQueryParams(String[] names, String[] values) throws UnsupportedEncodingException {
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

    protected ClientRequest createClientRequest(String url) throws ConnectorException {
        // get or create REST client request context for thread
        RestClientRequestContext context = contextsCache.get();
        if (context == null) {
            context = new RestClientRequestContext();
            contextsCache.set(context);
        }

        if (isAuthRequest == false) {
            String token = tokenSessionManager.getToken(connection.getServer());
            if (token == null) {
                // authenticate and get new token
                AuthClient.AuthResponse response = loginInternal();
                token = response.getTenantInfo().accessToken;
                tokenSessionManager.setToken(connection.getServer(), token);
            }
            ClientRequest request = context.newClientRequest(url);
            request = request.followRedirects(true);
            request.header("X-Auth-Token", token);
            return request;
        }
        else {
            ClientRequest request = context.newClientRequest(url);
            return request.followRedirects(true);
        }
    }

    protected AuthClient.AuthResponse loginInternal() throws ConnectorException {
        AuthClient authClient = new AuthClient(connection);
        TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(connection.getServer());
        if (credentials == null) {
            throw new ConnectorException("Failed to find any configured credentials");
        }
        AuthClient.AuthResponse response = authClient.login(credentials.getTenantInfo());
        if (response.getStatus() != javax.ws.rs.core.Response.Status.OK) {
            throw new ConnectorException("Failed to authenticate on request, status code: " + response.getStatus());
        }
        return response;
    }

    public TokenSessionManager getTokenSessionManager() {
        return tokenSessionManager;
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
            HttpConnectionParams.setSoTimeout(httpParams, HTTP_READ_TIMEOUT_MS );
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
                executor.close();
            } catch (Exception e) {
            }
        }
    }

}
