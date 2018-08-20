package org.groundwork.cloudhub.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dtaylor on 8/8/17.
 */
public class ServiceContainerNode extends InventoryNode {

    private Map<String, ServiceNode> services = new HashMap<>();
    private String prefixedName;

    public ServiceContainerNode(String name, String prefixedName) {
        super(name);
        this.prefixedName = prefixedName;
    }

    public Map<String, ServiceNode> getServices() {
        return services;
    }

    public ServiceNode addService(String serviceName, String querySpec, Integer id) {
        ServiceNode node = new ServiceNode(serviceName, querySpec, id);
        services.put(serviceName,  node);
        return node;
    }

    public String getPrefixedName() {
        return prefixedName;
    }

    public void setPrefixedName(String prefixedName) {
        this.prefixedName = prefixedName;
    }
}
