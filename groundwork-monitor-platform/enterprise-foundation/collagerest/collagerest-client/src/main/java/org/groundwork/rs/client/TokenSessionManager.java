package org.groundwork.rs.client;

import org.groundwork.foundation.ws.impl.WSClientConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenSessionManager {

    class Credentials {
        private final String username;
        private final String password;

        Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
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

    public void addCredentials(String server, String userName, String password) {
        credentials.put(server, new Credentials(userName, password));
    }

    Credentials getCredentials(String server) {
        return credentials.get(server);
    }

    public void removeCredentials(String server) {
        credentials.remove(server);
    }
}
