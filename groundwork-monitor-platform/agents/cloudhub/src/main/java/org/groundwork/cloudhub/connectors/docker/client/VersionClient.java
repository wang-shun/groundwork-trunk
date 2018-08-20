package org.groundwork.cloudhub.connectors.docker.client;

import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Retrieves the CAdvisor release version, not the CAdvisor API version
 *
 */
public class VersionClient extends BaseDockerClient {

    public VersionClient(DockerConnection connection) {
        super(connection, 2);
    }

    public String getVersionInfo() throws ConnectorException {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        try {
            URL url = new URL(makeDockerVersionConnection());
            urlConnection = url.openConnection();
            urlConnection.connect();
            stream = urlConnection.getInputStream();
            StringWriter writer = new StringWriter();
            drain(new InputStreamReader(stream), writer);
            String version = writer.toString();
            if (version.toLowerCase().startsWith("unsupported")) {
                version = "";
                apiLevel = 1;
            }
            else {
                apiLevel = (StringUtils.isEmpty(version) ? 1 : 2);
            }
            return version;
        }
        catch (Exception e) {
            this.apiLevel = 1;
            throw new ConnectorException(e);
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
    }

}
