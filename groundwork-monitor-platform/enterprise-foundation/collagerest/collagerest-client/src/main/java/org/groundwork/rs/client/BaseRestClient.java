package org.groundwork.rs.client;

import com.groundwork.collage.util.TLSV12ClientConfiguration;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.common.GWRestConstants;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
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
import java.util.Collections;
import java.util.List;

/**
 * Base implementation and helpers for the Java REST Client for performing query and administrative operations
 * on monitored entities in the Groundwork enterprise foundation server. Encoding, media type manipulations, and
 * path building functions are provided to all REST Client classes.
 */
public abstract class BaseRestClient {

    public final static String DEFAULT_WS_USER = "wsuser";
    public final static String DEFAULT_WS_PW = "wsuser";
    public final static String RETRY_AUTH = "Retrying Authentication, token may have timed out ....";
    public final static String SEPARATOR = "/";
    public final static String PARAM_QUERY_NAME = "query";
    public final static String PARAM_QUERY = PARAM_QUERY_NAME + "=";
    public final static String PARAM_DEPTH_NAME = "depth";
    public final static String PARAM_DEPTH = PARAM_DEPTH_NAME + "=";
    public final static String PARAM_START_NAME = "first";
    public final static String PARAM_START = PARAM_START_NAME + "=";
    public final static String PARAM_COUNT_NAME = "count";
    public final static String PARAM_COUNT = PARAM_COUNT_NAME + "=";
    public final static String ASYNC_NAMES[] = {"async"};
    public final static String ASYNC_VALUES[] = {"true"};
    public final static String NOT_ASYNC_VALUES[] = {"false"};
    public static final String MERGE_NAMES[] = {"merge"};
    public static final String MERGE_VALUES[] = {"true"};
    public static final String NOT_MERGE_VALUES[] = {"false"};
    public static final String RPC_AUTOCOMPLETE = "autocomplete";

    public static final int TOO_MANY_REQUESTS = 429;

    protected static final int HTTP_CONNECTION_TIMEOUT_MS = 60000;
    protected static final int HTTP_READ_TIMEOUT_MS = 0; // 0 is infinite
    public static final String APP_NAME = "javaClient";
    protected static final int RETRIES = 2;
    protected static final String FOUNDATION_REST_URL = "foundation_rest_url";
    protected static final TokenSessionManager tokenSessionManager = new TokenSessionManager();

    /**  valid, supported internet media type (MIME) supported */
    protected MediaType mediaType = MediaType.APPLICATION_XML_TYPE;
    /** the deployment root URL for all Rest operations */
    protected final String deploymentUrl;

    /** the auth root URL for all Rest operations */
    protected final String authUrl;

    /** REST client request contexts thread local cache */
    protected static ThreadLocal<RestClientRequestContext> contextsCache = new ThreadLocal<RestClientRequestContext>();

    protected boolean isAuthRequest = false;

    /**
     * Create a Device REST Client for performing query and administrative operations
     * on the devices in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public BaseRestClient(String deploymentUrl) {
        this(deploymentUrl,false);
    }

    public BaseRestClient(String deploymentUrl, boolean isAuthRequest) {
        this(deploymentUrl,isAuthRequest,deploymentUrl);
    }

    public BaseRestClient(String deploymentUrl, boolean isAuthRequest, String authUrl) {
        this.deploymentUrl = deploymentUrl;
        this.isAuthRequest = isAuthRequest;
        this.authUrl = authUrl;
    }

    public BaseRestClient(String deploymentUrl, String authUrl) {
        this.deploymentUrl = deploymentUrl;
        this.authUrl = authUrl;
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
     * Join a base URL with a relative path to create a full URL
     *
     * @param base the base URL such as http://server-name[:port]/foundation-webapp/api
     * @param path the path such as /devices?query=etc
     * @return the correctly joined path respecting path separators
     */
    protected String joinApiPath(String base, String path) {
        StringBuilder result = new StringBuilder();
        if (base == null) base = "";
        if (path == null) path = "";
        result.append(base);
        if (base.endsWith(SEPARATOR)) {
            if (path.startsWith(SEPARATOR))
                result.append(path.substring(1));
            else
                result.append(path);
        }
        else {
            if (path.startsWith(SEPARATOR))
                result.append(path);
            else {
                result.append(SEPARATOR);
                result.append(path);
            }
        }
        return result.toString();
    }

    /**
     * Given a list of names, create a comma-separated list of items to be passed on a URL
     *
     * @param list the list of names to be comma-separated
     * @return the comma-separated string representation of the list
     */
    protected String makeCommaSeparatedParamFromList(List<?> list)  {
        StringBuilder result = new StringBuilder();
        boolean initial = true;
        for (Object item : list) {
            if (initial)
                initial = false;
            else
                result.append(",");
            result.append(item.toString());
        }
        return result.toString();
    }

    /**
     * UTF-8 encode a string
     *
     * @param param the string to be encoded
     * @return the encoded string
     * @throws UnsupportedEncodingException
     */
    protected String encode(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8").replace("+", "%20");
        //return URLUtils.encodePathSegment(param);
    }

    /**
     * Build a full URL from a relative path and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @return a full URL
     */
    protected String build(String apiRoot)  {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        return url.toString();
    }

    /**
     * Build a full URL from a relative API path, additional path information,
     * and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path additional path information
     * @return a full URL
     */
    protected String buildUrlWithPath(String apiRoot, String path)  {
        return buildUrlWithPaths(apiRoot, path, null);
    }

    /**
     * Build a full URL from a relative API path, additional path information,
     * and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path0 additional path information
     * @param path1 additional path information
     * @return a full URL
     */
    protected String buildUrlWithPaths(String apiRoot, String path0, String path1)  {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        if (path0 != null) {
            url.append(path0);
            if (path1 != null)
                url.append('/').append(path1);
        }
        return url.toString();
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
        return buildUrlWithPathAndQueryParams(apiRoot, null, encodedQueryParams);
    }

    /**
     * Build a full URL from a relative API path, additional path information, additional
     * pre-encoded query parameters, and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path additional path information
     * @param encodedQueryParams the pre-encoded query parameters to append to the final URL
     * @return a full URL
     */
    protected String buildUrlWithPathAndQueryParams(String apiRoot, String path, String encodedQueryParams) {
        return buildUrlWithPathsAndQueryParams(apiRoot, path, null, encodedQueryParams);
    }

    /**
     * Build a full URL from a relative API path, additional paths information, additional
     * pre-encoded query parameters, and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path0 additional path information
     * @param path1 additional path information
     * @param encodedQueryParams the pre-encoded query parameters to append to the final URL
     * @return a full URL
     */
    protected String buildUrlWithPathsAndQueryParams(String apiRoot, String path0, String path1, String encodedQueryParams) {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        if (path0 != null) {
            url.append(path0);
            if (path1 != null)
                url.append('/').append(path1);
        }
        if (encodedQueryParams != null)
            url.append(encodedQueryParams);
        return url.toString();
    }

    /**
     * Build a full URL from a relative API path, additional path information, a depth-type query parameter,
     * and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path additional path information
     * @param depthType the depth type to be added as a query parameter string
     * @return a full URL
     * @throws UnsupportedEncodingException
     */
    protected String buildLookupWithDepth(String apiRoot, String path, DtoDepthType depthType) throws UnsupportedEncodingException {
        return buildLookupWithDepth(apiRoot, path, null, depthType);
    }

    /**
     * Build a full URL from a relative API path, additional path information, a depth-type query parameter,
     * and the base deployment URL configured on this client
     *
     * @param apiRoot the relative api path
     * @param path0 additional path information
     * @param path1 additional path information or null
     * @param depthType the depth type to be added as a query parameter string
     * @return a full URL
     * @throws UnsupportedEncodingException
     */
    protected String buildLookupWithDepth(String apiRoot, String path0, String path1, DtoDepthType depthType) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        url.append(path0);
        if (path1 != null)
            url.append('/').append(path1);
        if (depthType != DtoDepthType.Shallow) {
            url.append("?");
            url.append(PARAM_DEPTH);
            url.append(depthType.toString().toLowerCase());
        }
        return url.toString();
    }

    protected String buildLookupWithPathQueryDepth(String apiRoot, String path, String query, DtoDepthType depthType) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        url.append(path);
        url.append(query);
        if (depthType != DtoDepthType.Shallow) {
            url.append("&");
            url.append(PARAM_DEPTH);
            url.append(depthType.toString().toLowerCase());
        }
        return url.toString();
    }

    /**
     * Build a full URL from a relative API path, additional path information, query parameters,
     * and the base deployment URL configured on this client. The query parameters will be encoded.
     *
     * @param apiRoot the relative api path
     * @param query additional query parameters to be encoded
     * @param depthType the depth type to be added as a query parameter string
     * @return a full URL
     * @throws UnsupportedEncodingException
     */
    protected String buildEncodedQueryWithDepth(String apiRoot, String query, DtoDepthType depthType) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        url.append("?");
        url.append(PARAM_QUERY);
        url.append(encode(query));
        if (depthType != DtoDepthType.Shallow) {
            url.append("&");
            url.append(PARAM_DEPTH);
            url.append(depthType.toString().toLowerCase());
        }
        return url.toString();
    }

    /**
     * Build and encode a full URL from an api root path, a HQL query to be encoded, and two additional query
     * parameters for start and count values.
     *
     * @param apiRoot the relative api path
     * @param query the HQL query string to be encoded as a query parameter
     * @param start the start query parameter
     * @param count the count query parameter
     * @return a full, encoded URL
     * @throws UnsupportedEncodingException
     */
    protected String buildEncodedQuery(String apiRoot, String query, int start, int count)
            throws UnsupportedEncodingException {
        return buildEncodedQuery(apiRoot, query, null, start, count);
    }

    /**
     * Build and encode a full URL from an api root path, a HQL query to be encoded, and two additional query
     * parameters for start and count values.
     *
     * @param apiRoot the relative api path
     * @param query the HQL query string to be encoded as a query parameter
     * @param depthType the depth type to be added as a query parameter string
     * @param start the start query parameter
     * @param count the count query parameter
     * @return a full, encoded URL
     * @throws UnsupportedEncodingException
     */
    protected String buildEncodedQuery(String apiRoot, String query, DtoDepthType depthType, int start, int count)
            throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        String paramDelim = "?";
        if (query != null) {
            url.append(paramDelim);
            url.append(PARAM_QUERY);
            url.append(encode(query));
            paramDelim = "&";
        }
        if (depthType != null && depthType != DtoDepthType.Shallow) {
            url.append(paramDelim);
            paramDelim = "&";
            url.append(PARAM_DEPTH);
            url.append(depthType.toString().toLowerCase());
        }
        if (start > -1) {
            url.append(paramDelim);
            url.append(PARAM_START);
            url.append(start);
            url.append("&");
            url.append(PARAM_COUNT);
            url.append(count);
        }
        return url.toString();
    }

    /**
     * Build an encoded set of query parameters given an array of names and values
     *
     * @param names the query parameter names
     * @param values the query parameter values
     * @param depthType the depth type to be added as a query parameter string
     * @return an encoded string of all query parameter names and values
     * @throws UnsupportedEncodingException
     */
    protected String buildEncodedQueryParamsWithDepth(String[] names, String[] values, DtoDepthType depthType)
            throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        String params = buildEncodedQueryParams(names, values);
        if (params != null) {
            url.append(params);
        }
        if (depthType != DtoDepthType.Shallow) {
            url.append((url.length() == 0) ? "?" : "&");
            url.append(PARAM_DEPTH);
            url.append(depthType.toString().toLowerCase());
        }
        return ((url.length() != 0) ? url.toString() : null);
    }

    /**
     * Build an encoded set of query parameters given an array of names and values
     *
     * @param names the query parameter names
     * @param values the query parameter values
     * @return an encoded string of all query parameter names and values
     * @throws UnsupportedEncodingException
     */
    protected String buildEncodedQueryParams(String[] names, String[] values) throws UnsupportedEncodingException {
        String queryParams = buildEncodedPostParams(names, values);
        return ((queryParams != null) ? "?" + queryParams : null);
    }

    /**
     * Build an encoded set of post parameters given an array of names and values
     *
     * @param names the post parameter names
     * @param values the post parameter values
     * @return an encoded string of all post parameter names and values
     * @throws UnsupportedEncodingException
     */
    protected String buildEncodedPostParams(String[] names, String[] values) throws UnsupportedEncodingException {
        if (names == null || values == null || names.length != values.length)
            return null;
        StringBuilder postParams = new StringBuilder();
        String paramDelim = "";
        int count = 0;
        for (int index = 0; index < names.length; index++) {
            if (values[index] != null) {
                postParams.append(paramDelim);
                postParams.append(encode(names[index]));
                postParams.append("=");
                postParams.append(encode(values[index]));
                count++;
                paramDelim = "&";
            }
        }
        return (count == 0) ? null : postParams.toString();
    }

    protected ClientRequest createClientRequest(String url) throws CollageRestException {
        // get or create REST client request context for thread
        RestClientRequestContext context = contextsCache.get();
        if (context == null) {
            context = new RestClientRequestContext();
            contextsCache.set(context);
        }

        if (!isAuthRequest) {
            String token = tokenSessionManager.getToken(deploymentUrl);
            if (token == null) {
                // authenticate and get new token
                token = loginInternal();
                tokenSessionManager.setToken(deploymentUrl, token);
            }
            ClientRequest request = context.newClientRequest(url);
            request.header(GWRestConstants.HEADER_GWOS_API_TOKEN, token);
            request.header(GWRestConstants.HEADER_GWOS_APP_NAME, APP_NAME);
            return request;
        } else {
            return context.newClientRequest(url);
        }
    }

    protected String loginInternal() throws CollageRestException {
        AuthClient authClient = new AuthClient(authUrl);
        TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(authUrl);
        String username;
        String password;
        if (credentials == null) {
            username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
            if (username == null) {
                username = DEFAULT_WS_USER;
            }
            if (password == null) {
                password = DEFAULT_WS_PW;
            }
        }
        else {
            username = credentials.getUsername();
            password = credentials.getPassword();
        }
        AuthClient.Response response = authClient.login(username, password, APP_NAME);
        if (response.getStatus() != javax.ws.rs.core.Response.Status.OK) {
            throw new CollageRestException("Failed to authenticate on request, status code: " + response.getStatus(), response.getStatus().getStatusCode());
        }
        return response.getToken();
    }

    protected boolean isEmpty(String s) {
        if (s == null) return true;
        if (s.trim().equals("")) return true;
        return false;
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
            TLSV12ClientConfiguration.configure(httpClient);
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

    /**
     * Make client request with authorization retry.
     *
     * @param url request url
     * @param description request description used in exception
     * @param type request type
     * @return return type instance or null if not found
     * @throws CollageRestException on client or server side error
     */
    protected <T> T clientRequest(String url, String description, GenericType<T> type)
            throws CollageRestException {
        return clientRequest("GET", url, description, type);
    }

    /**
     * Make client request with authorization retry.
     *
     * @param method HTTP request method
     * @param url request url
     * @param description request description used in exception
     * @param type request type, (use new GenericType<Void>(){} for no return)
     * @return return type instance or null if not found
     * @throws CollageRestException on client or server side error
     */
    protected <T> T clientRequest(String method, String url, String description, GenericType<T> type)
            throws CollageRestException {
        return clientRequest(method, url, null, description, type);
    }

    /**
     * Make client request with authorization retry.
     *
     * @param method HTTP request method
     * @param url request url
     * @param body request body object or null
     * @param description request description used in exception
     * @param type request type, (use new GenericType<Void>(){} for no return)
     * @return return type instance or null if not found
     * @throws CollageRestException on client or server side error
     */
    protected <T> T clientRequest(String method, String url, Object body, String description, GenericType<T> type)
            throws CollageRestException {
        return clientRequest(method, url, null, body, null, description, type);
    }

    /**
     * Make client request with authorization retry.
     *
     * @param method HTTP request method
     * @param url request url
     * @param accept accept content type or null
     * @param body request body object or null
     * @param contentType request body content type or null
     * @param description request description used in exception
     * @param type request type, (use new GenericType<Void>(){} for no return)
     * @return return type instance or null if not found
     * @throws CollageRestException on client or server side error
     */
    protected <T> T clientRequest(String method, String url, String accept, Object body, String contentType,
                                  String description, GenericType<T> type)
            throws CollageRestException {
        Response.Status status = null;
        ClientResponse<T> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(url);
                if (type.getGenericType() != Void.class) {
                    if (accept != null) {
                        request.accept(accept);
                    } else {
                        request.accept(mediaType);
                    }
                }
                if (body != null) {
                    if (contentType != null) {
                        request.body(contentType, body);
                    } else {
                        request.body(mediaType, body);
                    }
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
                } else if (response.getStatus() == TOO_MANY_REQUESTS) {
                    throw new CollageRestException(response.getEntity(String.class), response.getStatus());
                } else if ((response.getResponseStatus() == Response.Status.UNAUTHORIZED) && (retry < 1)) {
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        } catch (CollageRestException cre) {
            throw cre;
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        status = ((status != null) ? status : Response.Status.SERVICE_UNAVAILABLE);
        throw new CollageRestException(String.format("Exception executing %s with status code of %d, reason: %s",
                description, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    protected String buildPostAsyncMergeURL(String rootURL, String rootSingle, boolean merge, boolean async) {
        try {
            String requestUrl;
            if (async || !merge) {
                String[] asyncValues = (async ? ASYNC_VALUES : NOT_ASYNC_VALUES);
                String[] mergeValues = (merge ? MERGE_VALUES : NOT_MERGE_VALUES);
                requestUrl = buildUrlWithPathAndQueryParams(rootURL, null,
                        buildEncodedQueryParams(
                            new String[]{ASYNC_NAMES[0], MERGE_NAMES[0]},
                            new String[]{asyncValues[0], mergeValues[0]}));
            } else {
                requestUrl = build(rootSingle);
            }
            return requestUrl;
        }
        catch (UnsupportedEncodingException e) {
            throw new CollageRestException(e);
        }
    }


    protected List<DtoName> autoComplete(String prefix, String apiRoot) {
        return autoComplete(prefix, apiRoot, null);
    }

    protected List<DtoName> autoComplete(String prefix, String apiRoot, Integer limit) {
        prefix = (((prefix != null) && !prefix.isEmpty()) ? prefix : "*");
        String requestUrl ;
        if (limit != null) {
            String requestParams;
            try {
                requestParams = buildEncodedQueryParams(new String[]{"limit"}, new String[]{Integer.toString(limit)});
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException(uee);
            }
            requestUrl = buildUrlWithPathsAndQueryParams(apiRoot, RPC_AUTOCOMPLETE, prefix, requestParams);
        }
        else {
            requestUrl = buildUrlWithPaths(apiRoot, RPC_AUTOCOMPLETE, prefix);

        }
        String requestDescription = String.format("autocomplete prefix [%s]", prefix);
        DtoNamesList dtoNamesList = clientRequest(requestUrl, requestDescription, new GenericType<DtoNamesList>(){});
        return ((dtoNamesList != null) ? dtoNamesList.getNames() : Collections.EMPTY_LIST);
    }

}
