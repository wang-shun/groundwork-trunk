package org.groundwork.rs.auth;

import org.junit.Test;

public class AuthorizationConfigurationTest
{

    @Test
    public void testReadingConfig() throws Exception {
        AuthorizationConfiguration configuration = new AuthorizationConfiguration();
        assert 480 == configuration.getMaxInactiveIntervalMinutes();
        assert 500 == configuration.getMaxSessions();
    }
}
