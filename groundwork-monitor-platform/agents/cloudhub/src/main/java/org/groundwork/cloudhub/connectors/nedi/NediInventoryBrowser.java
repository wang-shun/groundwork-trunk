package org.groundwork.cloudhub.connectors.nedi;

import com.zaxxer.hikari.HikariDataSource;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;

public class NediInventoryBrowser implements InventoryBrowser {

    private HikariDataSource dataSource;
    private String policyHost;
    private NediDatabaseAccess databaseAccess;

    public NediInventoryBrowser(HikariDataSource dataSource, String policyHost) {
        this.dataSource = dataSource;
        this.policyHost = policyHost;
        this.databaseAccess = new NediDatabaseAccess();
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) {
        DataCenterInventory inventory = new DataCenterInventory(options);
        databaseAccess.queryDeviceInventory(dataSource, inventory.getHypervisors());
        databaseAccess.queryPolicyInventory(dataSource, policyHost, inventory.getHypervisors());
        return inventory;
    }


}
