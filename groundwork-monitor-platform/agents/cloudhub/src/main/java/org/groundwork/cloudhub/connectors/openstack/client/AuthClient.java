package org.groundwork.cloudhub.connectors.openstack.client;

import org.groundwork.cloudhub.configuration.OpenStackConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringReader;

public class AuthClient extends BaseOpenStackClient {

    public static final String KEYSTONE_AUTH_URL = ":%s/v2.0/tokens";
    public static final String KEYSTONE_AUTHENTICATION = "{\"auth\":{\"passwordCredentials\":{\"username\": \"%s\", \"password\":\"%s\"}, \"tenantName\":\"%s\"}}";

    // OLD Remove this when ready
    public static final String HARD_CODED_KEYSTONE_TOKEN_URL = "http://agno.groundwork.groundworkopensource.com:5000/v2.0/tokens";
    public static final String HARD_CODED_KEYSTONE_TOKEN_REQUEST = "{\"auth\":{\"passwordCredentials\":{\"username\": \"demo\", \"password\":\"55d794a346cf413a\"}, \"tenantName\":\"demo\"}}";
    // admin password: 10e08681091c47e3


    public AuthClient(OpenStackConnection connection) {
        super(connection, true);
    }

    public class AuthResponse {
        private final TenantInfo tenantInfo;
        private final javax.ws.rs.core.Response.Status status;

        public AuthResponse(TenantInfo tenantInfo, Response.Status status) {
            this.tenantInfo = tenantInfo;
            this.status = status;
        }

        public TenantInfo getTenantInfo() {
            return tenantInfo;
        }

        public javax.ws.rs.core.Response.Status getStatus() {
            return status;
        }

        public boolean success() {
            return status == javax.ws.rs.core.Response.Status.OK;
        }

        public boolean authFailure() {
            return status == Response.Status.UNAUTHORIZED;
        }

        public boolean error() {
            return status != Response.Status.UNAUTHORIZED &&
                    status != Response.Status.OK;
        }
    }

    public AuthResponse loginWithToken(TenantInfo tenantInfo) throws ConnectorException {
        tokenSessionManager.addCredentials(connection.getServer(), connection.getUsername(), connection.getPassword(), tenantInfo);
        tokenSessionManager.setToken(connection.getServer(), tenantInfo.accessToken);
        return new AuthResponse(tenantInfo, Response.Status.OK);
    }

    public AuthResponse login(TenantInfo tenantInfo) throws ConnectorException {
        ClientResponse<String> response = null;
        TenantInfo tenant = new TenantInfo();
        try {
            tokenSessionManager.addCredentials(connection.getServer(), connection.getUsername(), connection.getPassword(), tenantInfo);
            String apiPath = String.format(KEYSTONE_AUTH_URL, connection.getKeystonePort());
            ClientRequest request = createClientRequest(build(apiPath));
            request.accept(mediaType);
            String tokenRequest = String.format(KEYSTONE_AUTHENTICATION, connection.getUsername(), connection.getPassword(), tenantInfo.tenantName);
            request.body(MediaType.APPLICATION_JSON, tokenRequest);
            response = request.post();
            if (response.getResponseStatus() == Response.Status.OK) {
                String payload = response.getEntity(String.class);
                JsonReader reader = Json.createReader(new StringReader(payload));
                JsonObject object = reader.readObject();
                tenant.accessToken = object.getJsonObject("access").getJsonObject("token").getJsonString("id").getString(); //.toString();
                tenant.tenantId = object.getJsonObject("access").getJsonObject("token").getJsonObject("tenant").getJsonString("id").getString();
                tenant.tenantName = object.getJsonObject("access").getJsonObject("token").getJsonObject("tenant").getJsonString("name").getString();
                reader.close();
                tokenSessionManager.setToken(connection.getServer(), tenant.accessToken);
            }
        } catch (Exception e) {
            return new AuthResponse(new TenantInfo(), Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            if (response != null)
                response.releaseConnection();
        }

        return new AuthResponse(tenant, response.getResponseStatus());
    }

    public AuthResponse logout(String server) throws ConnectorException {
        TokenSessionManager.Credentials credentials = tokenSessionManager.getCredentials(server);
        tokenSessionManager.removeToken(server);
        if (credentials == null) {
            return new AuthResponse(new TenantInfo(), Response.Status.BAD_REQUEST);
        }
        // TODO: implement logout if supported
        // TODO: throw exception on error
        return new AuthResponse(new TenantInfo(), Response.Status.OK);
    }

    public TenantInfo retrieveNewKeystoneToken() throws Exception {

        ClientRequest request = new ClientRequest(HARD_CODED_KEYSTONE_TOKEN_URL);
        request = request.followRedirects(true);
        request.accept(MediaType.APPLICATION_JSON);
        request.body(MediaType.APPLICATION_JSON, HARD_CODED_KEYSTONE_TOKEN_REQUEST);
        ClientResponse<String> response = request.post();
        String payload;
        if (response.getResponseStatus() == Response.Status.OK) {
            payload = response.getEntity(String.class);
        }
        else {
            throw new IOException("Failed to connect to Keystone Token: " + response.getResponseStatus());
        }
        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        TenantInfo tenant = new TenantInfo();
        tenant.accessToken = object.getJsonObject("access").getJsonObject("token").getJsonString("id").getString(); //.toString();
        tenant.tenantId = object.getJsonObject("access").getJsonObject("token").getJsonObject("tenant").getJsonString("id").getString();
        tenant.tenantName = object.getJsonObject("access").getJsonObject("token").getJsonObject("tenant").getJsonString("name").getString();
        reader.close();
        return tenant;
    }

}
