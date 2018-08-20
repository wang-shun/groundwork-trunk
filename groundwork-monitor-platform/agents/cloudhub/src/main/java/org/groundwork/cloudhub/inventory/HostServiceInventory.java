package org.groundwork.cloudhub.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dtaylor on 8/8/17.
 */
public class HostServiceInventory {

    protected Map<String, ServiceContainerNode> hosts = new HashMap<>();

    public HostServiceInventory() {
    }

    public Map<String, ServiceContainerNode> getHosts() {
        return hosts;
    }

    public ServiceContainerNode lookupHost(String hostName) {
        return hosts.get(hostName);
    }

    public ServiceContainerNode addHost(ServiceContainerNode host) {
        hosts.put(host.getName(), host);
        return host;
    }
}
