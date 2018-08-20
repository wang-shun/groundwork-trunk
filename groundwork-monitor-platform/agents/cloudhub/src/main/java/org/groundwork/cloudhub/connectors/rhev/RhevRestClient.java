package org.groundwork.cloudhub.connectors.rhev;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;

//import org.apache.http.client.utils.URIBuilder;

//import org.apache.http.impl.conn.BasicClientConnectionManager;


public class RhevRestClient {
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";

    private static Logger log = Logger.getLogger(RhevRestClient.class);

    private String restHost;           // (e.g. "eng-rhev-m-1.groundwork.groundworkopensource.com")
    private String restLogin;          // (e.g. "admin")
    private String restRealm;          // (e.g. "internal")
    private String restPassword;       // (e.g. "#m3t30r1t3")
    private String restPort;           // (e.g. "443" or "8443" for HTTPS)
    private String restProtocol;       // (e.g. "https")
    private String restBaseNode;       // (e.g. "/api" most often)
    private String keystorePassword;   // (commonly "changeit") for Auth Keystore JDK install
    private String keystoreCertsPath;  // (commonly /usr/java/latest/jre/lib/security/cacerts)
    private KeyStore clientKeyStore = null;

    private boolean isConnected = false;

    public RhevRestClient(String host, String login, String pass,
                          String realm, String port, String protocol, String basenode,
                          String certspath, String keystorepass) throws ConnectorException {
        if (log.isDebugEnabled())
            log.debug("In RESTbox instantiator code");

        if (!basenode.startsWith("/"))
            basenode = "/" + basenode;

        this.restHost = host;
        this.restLogin = login;
        this.restRealm = realm;
        this.restPassword = pass == null ? "" : pass;
        this.restPort = port;
        this.restProtocol = protocol;
        this.restBaseNode = basenode;
        this.keystorePassword = keystorepass;
        this.keystoreCertsPath = certspath;
    }

    public String getHost() {
        return this.restHost;
    }

    public String getLogin() {
        return this.restLogin;
    }

    public String getRealm() {
        return this.restRealm;
    }

    public String getPassword() {
        return this.restPassword;
    }

    public String getPort() {
        return this.restPort;
    }

    public String getProtocol() {
        return this.restProtocol;
    }

    public String getBaseNode() {
        return this.restBaseNode;
    }

    public String gettorePassword() {
        return this.keystorePassword;
    }

    public String gettoreCertsPath() {
        return this.keystoreCertsPath;
    }
    /*
     * isConnectionOK() returns boolean after testing connectivity
     * and throws exceptions when the result is false as to why
     * connectivity is bunged.
     *
     * This could become quite complicated - testing a connection
     * - doing authentication, fail-thru to a non-connected state
     */

    public boolean isConnectionOK() {
        if (this.isConnected == false) {
            if (this.restHost != null
                    && this.restLogin != null
                    && this.restRealm != null
                    && this.restPassword != null
                    && this.restPort != null
                    && this.restProtocol != null
                    && this.restBaseNode != null
                    && this.keystorePassword != null
                    && this.keystoreCertsPath != null) {
                this.isConnected = true; // which the following might negate
                executeAPI("");
            } else {
                this.isConnected = false; // redundant, but "safe" - and clearer...

                if (log.isInfoEnabled())
                    log.info("Couldn't connect: " +
                            (this.restHost == null ? "(null host) " : "") +
                            (this.restLogin == null ? "(null login/user name) " : "") +
                            (this.restRealm == null ? "(null realm) " : "") +
                            (this.restPassword == null ? "(null password) " : "") +
                            (this.restPort == null ? "(null port) " : "") +
                            (this.restProtocol == null ? "(null protocol) " : "") +
                            (this.restBaseNode == null ? "(null base REST node) " : "") +
                            (this.keystorePassword == null ? "(null keystore password) " : "") +
                            (this.keystoreCertsPath == null ? "(null keystore path) " : "")
                    );
            }
        }
        return this.isConnected;
    }


    public String executeAPI(String api) throws ConnectorException {
        String xml = null;  // we can return NULL if didn't work.

        String username = restLogin + "@" + restRealm;
        String password = restPassword;
        String protocol = restProtocol;
        String host = restHost;
        String basenode = restBaseNode;
        int port = Integer.parseInt(restPort);
        String keystoreLocation = keystoreCertsPath;
        String authorizationEncodingString = "Basic "
                + new BASE64Encoder()
                .encode((username + ":" + password).getBytes());
        //URIBuilder uriBuilder = new URIBuilder();

        if (api == null)  // ensure node is a String.
            api = "";

        if (!api.startsWith("/") && api.length() > 0)
            api = "/" + api;

        if (api.startsWith(restBaseNode))  // trim off base node,
            api = api.substring(restBaseNode.length());

        String fullUri = "";
        InputStream keyStoreStream = null;
        try {
//            if (clientKeyStore == null) {
                clientKeyStore = KeyStore.getInstance("JKS");
                keyStoreStream = new FileInputStream(keystoreLocation);
                clientKeyStore.load(keyStoreStream, keystorePassword.toCharArray());
//            }
            // set up the socketfactory, to use our keystore for client
            // authentication.

            SSLSocketFactory socketFactory = new SSLSocketFactory(
                    SSLSocketFactory.TLS,
                    clientKeyStore,
                    keystorePassword,
                    null,
                    null,
                    null,
                    (X509HostnameVerifier) SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            // create and configure scheme registry

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme(restProtocol, port, socketFactory));

            // create a client connection manager to use in creating httpclients

            SingleClientConnManager mgr = new SingleClientConnManager(registry);
            //PoolingClientConnectionManager mgr = new PoolingClientConnectionManager(registry);

            HttpClient httpClient = new DefaultHttpClient(mgr);

            // Build the URI
            String path = restBaseNode + api;
            URI uri = URIUtils.createURI(protocol, host, port, path, null, null );
            fullUri = uri.toString();
//            uriBuilder.setScheme(protocol);
//            uriBuilder.setHost(host);
//            uriBuilder.setPort(port);
//            uriBuilder.setPath(restBaseNode + node); // and put it back!

            //HttpGet httpget = new HttpGet(uriBuilder.build());
            HttpGet httpget = new HttpGet(uri);
            httpget.setHeader("Authorization", authorizationEncodingString);
            HttpResponse response = httpClient.execute(httpget);

            this.isConnected =
                    (response.getStatusLine().getStatusCode() >= 200)
                            && (response.getStatusLine().getStatusCode() < 300);

            xml = httpEntityToString(response.getEntity());  // must do, 'cuz once the stream is used up, its gone.
            log.debug(xml);
        } catch (Exception exc) {
            log.error("\nstack trace: (exception)\n"
                    + "exc.getMessage          = '" + exc.getMessage() + "'\n"
                    + "exc.getLocalizedMessage = '" + exc.getLocalizedMessage() + "'\n"
                    + "exc.toString            = '" + exc.toString() + "'\n"
                    + "---- key variables ----\n"
                    + "node                    = '" + api + "'\n"
                    + "basenode                = '" + basenode + "'\n"
                    + "username                = '" + username + "'\n"
                    + "protocol                = '" + protocol + "'\n"
                    + "hostname                = '" + host + "'\n"
                    + "port                    = '" + port + "'\n"
                    + "keystoreLocation        = '" + keystoreLocation + "'\n"
                    + "authorizationencoding   = '" + authorizationEncodingString + "'\n"
//                    + "URI string              = '" + uriBuilder.toString() + "'\n"
                    + "URI string              = '" + fullUri + "'\n"
                    + "---- stack ----\n"
                    + exc.getStackTrace()[0] + "\n"
                    + exc.getStackTrace()[1] + "\n"
                    + exc.getStackTrace()[2] + "\n"
                    + exc.getStackTrace()[3] + "\n"
                    + exc.getStackTrace()[4] + "\n"
                    + exc.getStackTrace()[5] + "\n"
                    + exc.getStackTrace()[6] + "\n"
                    + exc.getStackTrace()[7]
                    + "====\n");

            // exc.printStackTrace();
            throw new ConnectorException(exc.getMessage(), exc);
        }
        finally {
            if (keyStoreStream != null) {
                try {
                    keyStoreStream.close();
                } catch (IOException e) {
                    log.error("Error closing keystore stream ", e);
                }
            }
        }
        return xml;  // which can be (null)
    }

    private static String httpEntityToString(HttpEntity entity) {
        StringBuilder str = new StringBuilder();
        InputStream is = null;
        try {
            is = entity.getContent();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(is));
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
                str.append(line + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // tough luck...
                e.printStackTrace();
            }
        }
        return str.toString();
    }

}
