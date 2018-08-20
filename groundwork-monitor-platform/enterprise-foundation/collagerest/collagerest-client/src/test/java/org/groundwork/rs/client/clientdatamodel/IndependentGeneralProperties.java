package org.groundwork.rs.client.clientdatamodel;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.client.AbstractClientTest;

import java.lang.*;
/**
 * Created by rphillips on 3/5/16.
 */
public class IndependentGeneralProperties {
    public static final String _restUserID = System.getProperty("IND_GWOS_REST_USER");
    public static final String _restPassword = System.getProperty("IND_GWOS_REST_PW");
    public static final String _token = "";
    public static final String _appName = System.getProperty("IND_GWOS_REST_APP");

    public static final String _authHttpAcceptHeader = "text/plain";
    public static final String _authHttpContentType = "application/x-www-form-urlencoded";
    public static final String _authUri = System.getProperty("IND_GWOS_REST_AUTHURI");
    public static final String _baseUrl = System.getProperty("IND_GWOS_REST_API_BASE");
    public static final String _createHostUri = System.getProperty("IND_GWOS_REST_HOSTURI");
    public static final Integer _hostsToGenerate = Integer.parseInt(System.getProperty("IND_GWOS_REST_HOSTS_TO_GENERATE"));
    public static final Integer _eventsToGenerate = Integer.parseInt(System.getProperty("IND_GWOS_REST_EVENTS_TO_GENERATE"));
    public static final Long _hostsToGenerateLong = Long.parseLong(System.getProperty("IND_GWOS_REST_HOSTS_TO_GENERATE"));

}
