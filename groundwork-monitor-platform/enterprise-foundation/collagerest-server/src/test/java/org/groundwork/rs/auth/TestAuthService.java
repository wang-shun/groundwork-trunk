package org.groundwork.rs.auth;

import org.junit.Test;

public class TestAuthService {

    private static final String TEST_APPLICATION = "test-application";
    private static final String ADMIN_USERNAME = "RESTAPIACCESS";
    private static final String READER_USERNAME = "RESTAPIREADERACCESS";

    @Test
    public void testBasicAuthServiceOperation() throws Exception {

        // get singleton AuthService
        AuthService authService = AuthService.getInstance();
        assert authService != null;

        // allocate access token
        String accessToken = authService.makeAccessToken(TEST_APPLICATION, ADMIN_USERNAME);
        assert accessToken != null;
        // check access token
        assert authService.checkAccessToken(accessToken, TEST_APPLICATION);
        // delete access token
        assert authService.deleteAccessToken(accessToken, TEST_APPLICATION);
        // check access token
        assert !authService.checkAccessToken(accessToken, TEST_APPLICATION);
    }

    @Test
    public void testAuthServiceCacheOverflow() throws Exception {

        // get singleton AuthService
        AuthService authService = AuthService.getInstance();
        assert authService != null;

        // allocate access token
        String accessToken = authService.makeAccessToken(TEST_APPLICATION, ADMIN_USERNAME);
        // check access token
        assert authService.checkAccessToken(accessToken, TEST_APPLICATION);
        // allocate max access tokens
        for (int i = 0, limit = authService.getMaxAccessTokens(); (i < limit); i++) {
            authService.makeAccessToken(TEST_APPLICATION, ADMIN_USERNAME);
        }
        // check access token
        assert !authService.checkAccessToken(accessToken, TEST_APPLICATION);
    }

    @Test
    public void testReaderAuthorization() throws Exception {

        // get singleton AuthService
        AuthService authService = AuthService.getInstance();
        assert authService != null;

        String accessToken = authService.makeAccessToken(TEST_APPLICATION, ADMIN_USERNAME);
        assert accessToken != null;
        assert authService.checkAccessToken(accessToken, TEST_APPLICATION);
        assert authService.isAdmin(accessToken, TEST_APPLICATION);
        assert authService.deleteAccessToken(accessToken, TEST_APPLICATION);

        accessToken = authService.makeAccessToken(TEST_APPLICATION, READER_USERNAME);
        assert accessToken != null;
        assert authService.checkAccessToken(accessToken, TEST_APPLICATION);
        assert !authService.isAdmin(accessToken, TEST_APPLICATION);
        assert authService.deleteAccessToken(accessToken, TEST_APPLICATION);

    }

}
