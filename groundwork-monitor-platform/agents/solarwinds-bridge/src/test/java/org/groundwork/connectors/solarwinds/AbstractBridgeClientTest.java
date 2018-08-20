package org.groundwork.connectors.solarwinds;

/**
 * Created by dtaylor on 6/24/14.
 */
public class AbstractBridgeClientTest {

    protected static final String DEFAULT_URL = "http://localhost:8080/solarwinds-bridge/api";
    //protected static final String DEFAULT_URL = "http://eng-rh6-64.groundwork.groundworkopensource.com/solarwinds-bridge/api";

    protected static final String REST_API_URL = "http://localhost/api";

    public String getApiUrl() {
        // TODO: make this configurable
        return DEFAULT_URL;
    }
}
