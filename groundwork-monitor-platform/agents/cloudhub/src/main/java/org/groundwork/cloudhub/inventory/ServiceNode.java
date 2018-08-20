package org.groundwork.cloudhub.inventory;

/**
 * Created by dtaylor on 8/8/17.
 */
public class ServiceNode extends InventoryNode {

    private Integer id;
    private String querySpec;

    public ServiceNode(String name, String querySpec, Integer id) {
        super(name);
        this.id = id;
        this.querySpec = querySpec;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuerySpec() {
        return querySpec;
    }
}
