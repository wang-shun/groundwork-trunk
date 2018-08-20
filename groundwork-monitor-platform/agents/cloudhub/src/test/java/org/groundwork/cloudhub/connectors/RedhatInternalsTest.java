package org.groundwork.cloudhub.connectors;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.connectors.rhev.RhevRestClient;
import org.groundwork.cloudhub.connectors.rhev.restapi.API;
import org.groundwork.cloudhub.connectors.rhev.restapi.BaseResource;
import org.groundwork.cloudhub.connectors.rhev.restapi.BaseResources;
import org.groundwork.cloudhub.connectors.rhev.restapi.Disk;
import org.groundwork.cloudhub.connectors.rhev.restapi.Disks;
import org.groundwork.cloudhub.connectors.rhev.restapi.Host;
import org.groundwork.cloudhub.connectors.rhev.restapi.Hosts;
import org.groundwork.cloudhub.connectors.rhev.restapi.Link;
import org.groundwork.cloudhub.connectors.rhev.restapi.Network;
import org.groundwork.cloudhub.connectors.rhev.restapi.Networks;
import org.groundwork.cloudhub.connectors.rhev.restapi.StorageDomain;
import org.groundwork.cloudhub.connectors.rhev.restapi.StorageDomains;
import org.groundwork.cloudhub.connectors.rhev.restapi.VM;
import org.groundwork.cloudhub.connectors.rhev.restapi.VMs;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;

/**
 * Use this class to discover properties prior to developing code, tests
 *
 */
public class RedhatInternalsTest {

    private static Logger log = Logger.getLogger(RedhatInternalsTest.class);

    private RhevRestClient restClient = null;     // for the REST API access class

    @Test
    public void storageTest() throws Exception {
        System.out.println("Storage Test...");
        connect(ServerConfigurator.RHEV_M1_SERVER, // "172.28.113.197"
                ServerConfigurator.RHEV_M1_USERNAME,
                ServerConfigurator.RHEV_M1_PASSWORD,
                ServerConfigurator.RHEV_M1_REALM,
                "443",
                "https",
                ServerConfigurator.RHEV_M1_URI,
                ServerConfigurator.RHEV_M1_CERT_STORE,
                ServerConfigurator.RHEV_M1_CERT_PASSWORD
                );
        retrieveStorageMetrics();
        disconnect();
    }

    @Test
    public void networkTest() throws Exception {
        System.out.println("Network Test...");
        connect(ServerConfigurator.RHEV_M1_SERVER,
                ServerConfigurator.RHEV_M1_USERNAME,
                ServerConfigurator.RHEV_M1_PASSWORD,
                ServerConfigurator.RHEV_M1_REALM,
                "443",
                "https",
                ServerConfigurator.RHEV_M1_URI,
                ServerConfigurator.RHEV_M1_CERT_STORE,
                ServerConfigurator.RHEV_M1_CERT_PASSWORD
        );
        retrieveNetworkMetrics();
        disconnect();
    }

    @Test
    public void hostTest() throws Exception {
        System.out.println("Host Test...");
        connect(ServerConfigurator.RHEV_M1_SERVER,
                ServerConfigurator.RHEV_M1_USERNAME,
                ServerConfigurator.RHEV_M1_PASSWORD,
                ServerConfigurator.RHEV_M1_REALM,
                "443",
                "https",
                ServerConfigurator.RHEV_M1_URI,
                ServerConfigurator.RHEV_M1_CERT_STORE,
                ServerConfigurator.RHEV_M1_CERT_PASSWORD
        );
        retrieveHostsMetrics();
        retrieveVMMetrics();
        disconnect();
    }

    private void retrieveStorageMetrics() {
        InventoryType inventoryType = InventoryType.Datastore;
        try {
            StorageDomains wrapper = executeMultiAPI(StorageDomains.class, "/api/storagedomains");
            List<StorageDomain> storageDomains = wrapper.getStorageDomains();
            if (storageDomains != null) {
                for (StorageDomain storageDomain : storageDomains) {
                    System.out.println("domain = " + storageDomain.getName());
                    System.out.format("\tavail: %d, commit: %d, used: %d, sformat: %s type: %s\n",
                            storageDomain.getAvailable(),
                            storageDomain.getCommitted(),
                            storageDomain.getUsed(),
//                            storageDomain.getStatus().getDetail(),
//                            storageDomain.getStatus().getState(),
                            storageDomain.getStorageFormat(),
                            storageDomain.getType());
                    System.out.format("\t href: %s, id: %s\n",
//                            storageDomain.getDescription(),
//                            storageDomain.getCreationStatus().getDetail(),
//                            storageDomain.getCreationStatus().getState(),
                            storageDomain.getHref(),
                            storageDomain.getId());
                    System.out.format("\tstorage: %s, addr: %s, type: %s,%s, path: %s\n",
                            storageDomain.getStorage().getName(),
                            storageDomain.getStorage().getAddress(),
                            storageDomain.getStorage().getType(),
                            storageDomain.getStorage().getVfsType(),
                            storageDomain.getStorage().getPath());

                }
            }

        } catch (Exception e) {
            log.error("Failed to retrieve metrics for " + inventoryType.name(), e);
        }

    }

    private void retrieveNetworkMetrics() {
        InventoryType inventoryType = InventoryType.Network;
        try {
            Networks wrapper = executeMultiAPI(Networks.class, "/api/networks");
            List<Network> networks = wrapper.getNetworks();
            if (networks != null) {
                for (Network network : networks) {
                    System.out.println("network = " + network.getName());
                }
            }

        } catch (Exception e) {
            log.error("Failed to retrieve metrics for " + inventoryType.name(), e);
        }
    }

    private void retrieveHostsMetrics() {
        InventoryType inventoryType = InventoryType.Hypervisor;
        try {
            Hosts wrapper = executeMultiAPI(Hosts.class, "/api/hosts");
            List<Host> hosts = wrapper.getHosts();
            if (hosts != null) {
                for (Host host : hosts) {
                    System.out.println("host = " + host.getName());
                }
            }

        } catch (Exception e) {
            log.error("Failed to retrieve metrics for " + inventoryType.name(), e);
        }
    }

    private void retrieveVMMetrics() {
        InventoryType inventoryType = InventoryType.VirtualMachine;
        try {
            VMs wrapper = executeMultiAPI(VMs.class, "/api/vms");
            List<VM> vms = wrapper.getVMs();
            if (vms != null) {
                for (VM vm : vms) {
                    System.out.println("vm = " +vm.getName());
                    for (Link link : vm.getLinks()) {
                        String rel = link.getRel();
                        String href = link.getHref();
                        if (rel.equalsIgnoreCase("disks")) {
                            Disks disks = retrieveDisks(href);
                            int i = 0;
                            for (Disk disk : disks.getDisks()) {
                                System.out.println("Disk is " + disk.getName());
                                String diskHref = disk.getHref();
                                StorageDomains domains = disk.getStorageDomains();
                                if (domains != null) {
                                    for (StorageDomain domain : domains.getStorageDomains()) {
                                        System.out.println("domain is " + domain.getId());
                                    }
                                }
                            }
                        }
                        else if (rel.equalsIgnoreCase("nics")) {

                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to retrieve metrics for " + inventoryType.name(), e);
        }
    }

    private Disks retrieveDisks(String entrypoint) throws ConnectorException {
        Disks disks = executeMultiAPI(Disks.class, (entrypoint == null) ?  "/api/disks" : entrypoint);
        if (disks == null)
            throw new ConnectorException("Couldn't retrieve Disks");
        return disks;
    }


    private void connect(String host, String login, String password,
                         String realm, String port, String protocol, String restbase,
                         String certspath, String keystorepass) throws ConnectorException {
        try {
            restClient = new RhevRestClient(
                    host,
                    login,
                    password,
                    realm,
                    port,
                    protocol,
                    restbase,
                    certspath,
                    keystorepass
            );

        } catch (Exception e) {
            log.error("connect() - couldn't instantiate REST object", e);
        }
        API api = executeAPI(API.class, "/api");
        if (!restClient.isConnectionOK())
            throw new ConnectorException("Failed to connect to RHEV ");
    }


    public void disconnect() throws ConnectorException {
        try {
        } catch (Exception e) {
            log.error("Failed to disconnect", e);
        }
    }

    private <T extends BaseResource> T executeAPI(Class<T> tClass, String entryPoint) throws ConnectorException {
        String xml = null;
        try {
            xml = restClient.executeAPI(entryPoint);
            JAXBContext context = JAXBContext.newInstance(tClass);
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            return um.unmarshal(ss, tClass).getValue();
        }
        catch (Exception e) {
            log.error("Failed to execute API " + entryPoint, e);
        }
        return null;
    }

    private <T extends BaseResources> T executeMultiAPI(Class<T> tClass, String entryPoint) throws ConnectorException {
        String xml = null;
        try {
            xml = restClient.executeAPI(entryPoint);
            JAXBContext context = JAXBContext.newInstance(tClass);
            Unmarshaller um = context.createUnmarshaller();
            StreamSource ss = new StreamSource(new StringReader(xml));
            return um.unmarshal(ss, tClass).getValue();
        }
        catch (Exception e) {
            log.error("Failed to execute API " + entryPoint, e);
        }
        return null;
    }


}
