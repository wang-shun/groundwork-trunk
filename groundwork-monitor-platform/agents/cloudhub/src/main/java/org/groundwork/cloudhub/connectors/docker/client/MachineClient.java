package org.groundwork.cloudhub.connectors.docker.client;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.exceptions.ConnectorException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MachineClient extends BaseDockerClient {

    public MachineClient(DockerConnection connection, int apiLevel) {
        super(connection, apiLevel);
    }

    public DockerMachineInfo getMachineInfo() throws ConnectorException {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        List<ContainerInfo> containers = new ArrayList<ContainerInfo>();
        try {
            URL url = new URL(makeDockerEngineMachineConnection());
            urlConnection = url.openConnection();
            urlConnection.connect();
            stream = urlConnection.getInputStream();
            StringWriter writer = new StringWriter();
            drain(new InputStreamReader(stream), writer);
            String payload = writer.toString();
            ReadContext context = JsonPath.parse(payload);
            Integer numCores = context.read("$.num_cores", Integer.class);
            Long memoryCapacity = context.read("$.memory_capacity", Long.class);
            return new DockerMachineInfo(numCores, memoryCapacity);
        }
        catch (Exception e) {
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
