package org.groundwork.cloudhub.connectors.openstack.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenSessionManager {

    class Credentials {
        private final String username;
        private final String password;
        private final TenantInfo tenantInfo;

        Credentials(String username, String password, TenantInfo tenantInfo) {
            this.username = username;
            this.password = password;
            this.tenantInfo = tenantInfo;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public TenantInfo getTenantInfo() { return tenantInfo; }
    }
    /**
     * Map of unique server endpoints mapped to CollageRest Server tokens
     */
    private Map<String, String> sessions = new ConcurrentHashMap<String, String>();
    private Map<String, Credentials> credentials = new ConcurrentHashMap<String, Credentials    >();

    public TokenSessionManager() {
    }

    public String getToken(String server) {
        return sessions.get(server);
    }

    public void setToken(String server, String token) {
        sessions.put(server, token);
    }

    public void removeToken(String server) {
        sessions.remove(server);
    }

    public void addCredentials(String server, String userName, String password, TenantInfo tenantInfo) {
        credentials.put(server, new Credentials(userName, password, tenantInfo));
    }

    Credentials getCredentials(String server) {
        return credentials.get(server);
    }
}
