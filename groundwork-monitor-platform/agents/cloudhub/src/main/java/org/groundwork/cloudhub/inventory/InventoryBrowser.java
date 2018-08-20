package org.groundwork.cloudhub.inventory;

public interface InventoryBrowser {

    /**
     * Gather inventory of virtual resources
     *
     * @param options optional retrieval of inventory
     * @return a snapshot of the data center inventory
     */
    DataCenterInventory gatherInventory(InventoryOptions options);
}
