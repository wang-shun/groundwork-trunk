package org.groundwork.cloudhub.connectors.rhev;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.rhev.restapi.API;
import org.groundwork.cloudhub.connectors.rhev.restapi.Disk;
import org.groundwork.cloudhub.connectors.rhev.restapi.Disks;
import org.groundwork.cloudhub.connectors.rhev.restapi.Host;
import org.groundwork.cloudhub.connectors.rhev.restapi.Hosts;
import org.groundwork.cloudhub.connectors.rhev.restapi.Link;
import org.groundwork.cloudhub.connectors.rhev.restapi.NIC;
import org.groundwork.cloudhub.connectors.rhev.restapi.Network;
import org.groundwork.cloudhub.connectors.rhev.restapi.Networks;
import org.groundwork.cloudhub.connectors.rhev.restapi.Nics;
import org.groundwork.cloudhub.connectors.rhev.restapi.StorageDomain;
import org.groundwork.cloudhub.connectors.rhev.restapi.StorageDomains;
import org.groundwork.cloudhub.connectors.rhev.restapi.VM;
import org.groundwork.cloudhub.connectors.rhev.restapi.VMs;
import org.groundwork.cloudhub.connectors.rhev.restapi.VmPool;
import org.groundwork.cloudhub.connectors.rhev.restapi.VmPools;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RhevInventoryBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(RhevInventoryBrowser.class);

    private RhevRestClient restClient;

    public RhevInventoryBrowser(RhevRestClient restClient) {
        this.restClient = restClient;
    }

    private Map<String, String> poolMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> storageMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> networkMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> hostMap = new ConcurrentHashMap<String, String>();
    private JAXBContext context = null;

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) throws ConnectorException {
        DataCenterInventory inventory = new DataCenterInventory(options);

        if (options.isViewHypervisors()) {
            retrieveHypervisors(inventory);
        }
        if (options.isViewDatastores()) {
            retrieveStorageDomains(inventory);
        }
        if (options.isViewNetworks()) {
            retrieveNetworks(inventory);
        }
        if (options.isViewResourcePools()) {
            retrieveResourcePools(inventory);
        }
        if (log.isDebugEnabled()) {
            inventory.debug();
        }

        retrieveVirtualMachines(inventory);

        return inventory;
    }

    private void retrieveStorageDomains(DataCenterInventory inventory) throws ConnectorException {
        Map<String, InventoryContainerNode> datastores = inventory.getDatastores();
        try {
            String xml = restClient.executeAPI("/api/storagedomains");
            JAXBContext context = lookupJAXBContext();
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            StorageDomains storageDomains = um.unmarshal(ss, StorageDomains.class).getValue();
            for (StorageDomain storageDomain : storageDomains.getStorageDomains()) {
                String name = cleanse(storageDomain.getName());
                String id = cleanse(storageDomain.getId());
                InventoryContainerNode node =
                        new InventoryContainerNode(name, id);
                storageMap.put(id, name);
                datastores.put(name, node);
            }
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve storage domains", e);
        }
    }

    /**
     * WARNING: this operation can get expensive when called repeatedly
     *
     * @param href
     * @return
     */
    private String retrieveStorageByDiskId(String href) throws ConnectorException {
        try {
            String xml = restClient.executeAPI(href);
            JAXBContext context = lookupJAXBContext();
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            Disks disks = um.unmarshal(ss, Disks.class).getValue();
            for (Disk disk : disks.getDisks()) {
                String id = cleanse(disk.getId());
                StorageDomains domains = disk.getStorageDomains();
                if (domains != null) {
                    for (StorageDomain domain : domains.getStorageDomains()) {
                        String key = storageMap.get(domain.getId());
                        if (key != null)
                            return key;
                    }
                }
            }
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve Disks", e);
        }
        return null;
    }

    /**
     * WARNING: this operation can get expensive when called repeatedly
     *
     * @param href
     * @return
     */
    private String retrieveNetworkByNicRef(String href) throws ConnectorException {
        try {
            String xml = restClient.executeAPI(href);
            JAXBContext context = lookupJAXBContext();
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            Nics nics = um.unmarshal(ss, Nics.class).getValue();
            for (NIC nic : nics.getNics()) {
                String id = cleanse(nic.getId());
                Network network = nic.getNetwork();
                if (network != null) {
                    String key = networkMap.get(network.getId());
                    if (key != null)
                        return key;
                }
            }
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve Disks", e);
        }
        return null;
    }

    private JAXBContext lookupJAXBContext() throws ConnectorException {
        if (context == null) {
            try {
                context = JAXBContext.newInstance(API.class);
            }
            catch (Exception e) {
                throw new ConnectorException("Failed to get API context " + API.class, e);
            }
        }
        return context;
    }

    private void retrieveResourcePools(DataCenterInventory inventory) throws ConnectorException {
        Map<String, InventoryContainerNode> pools = inventory.getResourcePools();
        try {
            String xml = restClient.executeAPI("/api/vmpools");
            JAXBContext context = lookupJAXBContext();
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            VmPools vmPools = um.unmarshal(ss, VmPools.class).getValue();
            for (VmPool vmPool : vmPools.getVmPools()) {
                String name = cleanse(vmPool.getName());
                String id = cleanse(vmPool.getId());
                InventoryContainerNode node =
                        new InventoryContainerNode(name, id);
                poolMap.put(id, name);
                pools.put(name, node);
            }
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve VM Pools", e);
        }
    }

    private void retrieveNetworks(DataCenterInventory inventory) throws ConnectorException {
        Map<String, InventoryContainerNode> pools = inventory.getNetworks();
        try {
            String xml = restClient.executeAPI("/api/networks");
            JAXBContext context = lookupJAXBContext();
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            Networks networks = um.unmarshal(ss, Networks.class).getValue();
            for (Network network : networks.getNetworks()) {
                String name = cleanse(network.getName());
                String id = cleanse(network.getId());
                InventoryContainerNode node =
                        new InventoryContainerNode(name, id);
                networkMap.put(id, name);
                pools.put(name, node);
            }
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve Networks", e);
        }
    }

    private void retrieveHypervisors(DataCenterInventory inventory) throws ConnectorException {
        Map<String, InventoryContainerNode> hypervisors = inventory.getHypervisors();
        try {
            String xml = restClient.executeAPI("/api/hosts");
            JAXBContext context = lookupJAXBContext();
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            Hosts hosts = um.unmarshal(ss, Hosts.class).getValue();
            for (Host host : hosts.getHosts()) {
                String name = cleanse(host.getName());
                String id = cleanse(host.getId());
                InventoryContainerNode node =
                        new InventoryContainerNode(name, id);
                hostMap.put(id, name);
                hypervisors.put(name, node);
            }
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve Networks", e);
        }
    }

    /**
     * Returns an Inventory of all virtual machines in system
     *
     */
    private void retrieveVirtualMachines(DataCenterInventory inventory) throws ConnectorException {
        Map<String, VirtualMachineNode> virtualMachines = inventory.getVirtualMachines();
        try {
            String xml = restClient.executeAPI("/api/vms");
            JAXBContext context = lookupJAXBContext();
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            VMs vms = um.unmarshal(ss, VMs.class).getValue();
            for (VM vm : vms.getVMs()) {
                String name = cleanse(vm.getName());
                String id = cleanse(vm.getId());
                VirtualMachineNode node = new VirtualMachineNode(name, id);
                inventory.getSystemNameMap().put(id, name);


                for (Link link : vm.getLinks()) {
                    String rel = link.getRel();
                    String href = link.getHref();
                    if (inventory.getOptions().isViewDatastores() && rel.equalsIgnoreCase("disks")) {
                        String storageKey = retrieveStorageByDiskId(href);
                        if (storageKey != null)  {
                            InventoryContainerNode pool = inventory.getDatastores().get(storageKey);
                            if (pool != null)
                                pool.putVM(name, node);
                        }
                    }
                    else if (inventory.getOptions().isViewNetworks() && rel.equalsIgnoreCase("nics")) {
                        String networkKey = retrieveNetworkByNicRef(href);
                        if (networkKey != null)  {
                            InventoryContainerNode pool = inventory.getNetworks().get(networkKey);
                            if (pool != null)
                                pool.putVM(name, node);
                        }
                    }
                }
                if (vm.getHost() != null) {
                    String hostId = vm.getHost().getId();
                    if (!cleanse(hostId).equals("")) {
                        String hostName = hostMap.get(hostId);
                        if (hostName != null) {
                            InventoryContainerNode host = inventory.getHypervisors().get(hostName);
                            if (host != null)
                                host.putVM(name, node);
                        }
                    }
                }
                if (inventory.getOptions().isViewResourcePools() && vm.getVmPool() != null) {
                    String poolId = vm.getVmPool().getId();
                    if (!cleanse(poolId).equals("")) {
                        String poolName = poolMap.get(poolId);
                        if (poolName != null) {
                            InventoryContainerNode pool = inventory.getResourcePools().get(poolName);
                            if (pool != null)
                                pool.putVM(name, node);
                        }
                    }
                }
                virtualMachines.put(name, node);
            }
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to retrieve virtual machines", e);
        }
    }


    private String cleanse(String value) {
        return value == null ? "" : value;
    }

}
