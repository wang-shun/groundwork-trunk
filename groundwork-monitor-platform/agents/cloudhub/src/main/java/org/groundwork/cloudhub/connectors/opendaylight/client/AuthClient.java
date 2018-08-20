package org.groundwork.cloudhub.connectors.opendaylight.client;

import org.apache.commons.codec.binary.Base64;
import org.groundwork.cloudhub.configuration.OpenDaylightConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.opendaylight.controller.statistics.northbound.AllFlowStatistics;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class AuthClient extends BaseOpenDaylightClient {

    public AuthClient(OpenDaylightConnection connection) {
        super(connection);
    }

    public class AuthResponse {
        private final Response.Status status;

        public AuthResponse(Response.Status status) {
            this.status = status;
        }

        public Response.Status getStatus() {
            return status;
        }

        public boolean success() {
            return status == Response.Status.OK;
        }

        public boolean authFailure() {
            return status == Response.Status.UNAUTHORIZED;
        }

        public boolean error() {
            return status != Response.Status.UNAUTHORIZED &&
                    status != Response.Status.OK;
        }
    }

    public AuthResponse login() throws ConnectorException {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        try {
            String authString = connection.getUsername() + ":" + connection.getPassword();
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            String connectionString = makeFlowConnection();
            java.net.URL url = new java.net.URL(connectionString);
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            urlConnection.setRequestProperty("Accept", "application/xml");
            urlConnection.connect();
            JAXBContext context = JAXBContext.newInstance(AllFlowStatistics.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            stream = urlConnection.getInputStream();
            AllFlowStatistics result = (AllFlowStatistics) unmarshaller.unmarshal(stream);
        }
        catch (Exception e) {
            return new AuthResponse(Response.Status.INTERNAL_SERVER_ERROR);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                ((HttpURLConnection)urlConnection).disconnect();
            }
        }
        return new AuthResponse(Response.Status.OK);
    }

    public AuthResponse logout(String server) throws ConnectorException {
        return new AuthResponse(Response.Status.OK);
    }


}
