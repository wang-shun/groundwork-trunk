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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryClient extends BaseDockerClient {

    protected static final String DOCKER_HYPERVISOR = "docker-";
    protected static final String DOCKER_INTERNAL_PREFIX = "/docker/";

    public InventoryClient(DockerConnection connection, int apiLevel) {
        super(connection, apiLevel);
    }

    public List<DockerEngineInfo> listDockerEngines() throws ConnectorException {
        List<DockerEngineInfo> engines = new ArrayList<DockerEngineInfo>();
        String name = "unknown-host";
        try {
            URL url = new URL(makeDockerEngineMetricsConnection());
            name = /*DOCKER_HYPERVISOR + */ url.getHost();
//            if (!isEmpty(connection.getPrefix()))
//                name = connection.getPrefix() + name;
        }
        catch (Exception e) {
            throw new ConnectorException(e);

        }
        engines.add(new DockerEngineInfo(name));
        return engines;
    }

    public List<ContainerInfo> listContainers(String engine) throws ConnectorException {
        java.net.URLConnection urlConnection = null;
        InputStream stream = null;
        Map<String,ContainerInfo> containers = new HashMap<>();
        try {
            URL url = new URL(makeInventoryConnection());
            urlConnection = url.openConnection();
            urlConnection.connect();
            stream = urlConnection.getInputStream();
            StringWriter writer = new StringWriter();
            drain(new InputStreamReader(stream), writer);
            String payload = writer.toString();
            ReadContext context = JsonPath.parse(payload);
            List<String> aliases = context.read("$[*].aliases[0]");
//            List<String> names = context.read("$.subcontainers..name");
//            List<String> aliases = context.read("$.subcontainers..aliases[0]");
            for (String alias : aliases) {
                // @since 7.1.1: alias returned json format change from Docker 1.3 -> Docker 2.x API
                String id = (apiLevel == 2) ? alias : alias.substring(DOCKER_INTERNAL_PREFIX.length());
                String name = id;
                if (!isEmpty(connection.getPrefix()))
                    name = connection.getPrefix() + name;
                containers.put(name, new ContainerInfo(name, id, engine));
            }
            List<ContainerInfo> result = new ArrayList<ContainerInfo>();
            for (ContainerInfo info : containers.values()) {
                result.add(info);
            }
            return result;
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
