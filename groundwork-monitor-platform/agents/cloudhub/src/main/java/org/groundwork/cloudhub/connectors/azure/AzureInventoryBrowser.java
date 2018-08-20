package org.groundwork.cloudhub.connectors.azure;

import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;

import java.util.List;

/**
 * Created by justinchen on 2/15/18.
 */
public class AzureInventoryBrowser implements InventoryBrowser {

    private List<ConfigurationView> views;

    public AzureInventoryBrowser(List<ConfigurationView> views) {
        this.views = views;
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) {
        return new DataCenterInventory(options);
    }
}
