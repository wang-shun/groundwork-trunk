package org.groundwork.rs.client;


import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.groundwork.rs.common.GWRestConstants;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.Base64;

import javax.ws.rs.core.MediaType;

public class AuthClient extends BaseRestClient {

    static final String API_ROOT_SINGLE = "/auth";
    static final String API_ROOT = API_ROOT_SINGLE + "/";

    public enum PasswordEncodingType {
        none,
        base64,
        configured
    }

    protected PasswordEncodingType passwordEncoding = PasswordEncodingType.configured;

    public AuthClient(String deploymentUrl) {
        super(deploymentUrl, true);
        this.mediaType = MediaType.TEXT_PLAIN_TYPE;
    }

    public PasswordEncodingType getPasswordEncoding() {
        return passwordEncoding;
    }

    public void setPasswordEncoding(PasswordEncodingType passwordEncoding) {
        this.passwordEncoding = passwordEncoding;
    }

    public class Response {
        private final String token;
        private final javax.ws.rs.core.Response.Status status;

        public Response(String token, javax.ws.rs.core.Response.Status status) {
            this.token = token;
            this.status = status;
        }

        public String getToken() {
            return token;
        }

        public javax.ws.rs.core.Response.Status getStatus() {
            return status;
        }

        public boolean success() {
            return status == javax.ws.rs.core.Response.Status.OK;
        }

        public boolean authFailure() {
            return status == javax.ws.rs.core.Response.Status.NOT_FOUND;
        }

        public boolean error() {
            return status != javax.ws.rs.core.Response.Status.NOT_FOUND &&
                   status != javax.ws.rs.core.Response.Status.OK;
        }

    }

    public Response login(String user, String password, String appName) throws CollageRestException {
        return login(user, password, appName, mediaType);
    }

    private Response login(String user, String password, String appName, MediaType mediaType) throws CollageRestException {
        ClientResponse<String> response = null;
        int status = 200;
        String token = null;
        try {
            if (user == null || password == null) {
                return new Response("", javax.ws.rs.core.Response.Status.NOT_FOUND);
            }
            tokenSessionManager.addCredentials(deploymentUrl, user, password);
            ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT_SINGLE, "/login"));
            request.accept(mediaType);
            request.formParameter("user", Base64.encodeBytes(user.getBytes()));
            switch (passwordEncoding) {
                case configured: {
                    // If encryption enabled, Send the password as is in encrypted fashion else old way
                    if (JasyptUtils.isEncryptionEnabled()) {
                        request.formParameter("password", password);
                    }
                    else
                        request.formParameter("password", Base64.encodeBytes(password.getBytes()));
                    break;
                }
                case base64:
                    request.formParameter("password", Base64.encodeBytes(password.getBytes()));
                    break;
                case none:
                    request.formParameter("password", password);
                    break;
            }
            request.formParameter(GWRestConstants.PARAM_GWOS_APP_NAME, appName);
            response = request.post();
            token = response.getEntity(String.class);
            tokenSessionManager.setToken(deploymentUrl, token);
            javax.ws.rs.core.Response.Status responseStatus = response.getResponseStatus();
            status = (responseStatus == null) ? response.getStatus() : response.getResponseStatus().getStatusCode();
            if (status == javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode() && mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                // retry using legacy XML media type supported by 7.0.X servers
                return login(user, password, appName, MediaType.APPLICATION_XML_TYPE);
            }
        } catch (CollageRestException cre) {
            throw cre;
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null)
                response.releaseConnection();
        }
        if (token == null || response == null || status != 200) {
            throw new CollageRestException("Failed to negotiate authorization");
        }
        return new Response(token, response.getResponseStatus());
    }

    public javax.ws.rs.core.Response logout(String appName, String token) throws CollageRestException {
        return logout(appName, token, mediaType);
    }

    private javax.ws.rs.core.Response logout(String appName, String token, MediaType mediaType) throws CollageRestException {
        ClientResponse<javax.ws.rs.core.Response> response = null;
        try {
            ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT_SINGLE, "/logout"));
            request.accept(mediaType);
            request.formParameter(GWRestConstants.PARAM_GWOS_APP_NAME, appName);
            if (token == null) {
                token = "";
            }
            request.formParameter(GWRestConstants.PARAM_GWOS_API_TOKEN, token);
            response = request.post();
            int status = response.getResponseStatus().getStatusCode();
            if (status == javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode() && mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                // retry using legacy XML media type supported by 7.0.X servers
                return logout(appName, token, MediaType.APPLICATION_XML_TYPE);
            }
        } catch (CollageRestException cre) {
            throw cre;
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null)
                response.releaseConnection();
        }
        return response;
    }


    public String isTokenValid(String appName, String token) throws CollageRestException {
        return isTokenValid(appName, token, mediaType);
    }

    private String isTokenValid(String appName, String token, MediaType mediaType) throws CollageRestException {
        ClientResponse<String> response = null;
        String result = null;
        try {
            ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT_SINGLE, "/validatetoken"));
            request.accept(mediaType);
            request.formParameter(GWRestConstants.PARAM_GWOS_APP_NAME, appName);
            request.formParameter(GWRestConstants.PARAM_GWOS_API_TOKEN, token);
            response = request.post(String.class);
            result = response.getEntity();
            int status = response.getResponseStatus().getStatusCode();
            if (status == javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode() && mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                // retry using legacy XML media type supported by 7.0.X servers
                return isTokenValid(appName, token, MediaType.APPLICATION_XML_TYPE);
            }
        } catch (CollageRestException cre) {
            throw cre;
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null)
                response.releaseConnection();
        }
        return result;
    }

    /**
     * Removes the credentials for the given deployment  url
     * @param authURL
     * @throws CollageRestException
     */
    public void removeCredentialsFromTokenSession(String authURL) throws CollageRestException {
        tokenSessionManager.removeCredentials(authURL);
    }

}
