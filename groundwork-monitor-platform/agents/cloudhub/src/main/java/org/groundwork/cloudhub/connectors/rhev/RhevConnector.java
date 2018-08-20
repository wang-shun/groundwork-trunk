package org.groundwork.cloudhub.connectors.rhev;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.RedhatConnection;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.rhev.restapi.*;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.utils.Conversion;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * RHEV Connector
 *
 * A utility-application to scan virtual-machine  processing cloud
 * APIs ( application-programming-interfaces ), translating the results
 * obtained, on a periodic basis, to the format( s ) that may directly
 * be used by the GroundWorkOpenSystems ( GWOS ) systems management
 * software.
 *
 *
 * The authorization parameters are:
 *
 * accessauth            [req]: establish the authorization node
 * url | system          [req]: url of the web service
 * user[name]            [req]: username for the authentication
 * pass[word]            [req]: password for the authentication
 * vm[name]              [req]: name of the vm to address
 *
 * OPERATING DESIGN:
 *
 * </pre>
 */

@Service(RhevConnector.NAME)
@Scope("prototype")
public class RhevConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    public static final String NAME = "RhevConnector";
    private static final int MAXRETRIES = 3;  //internal, arbitrary
    private static final long RETRYGAP = 5L * 1000L;  // 5 secs...
    private static String VMS = "vms";
    private static String HOSTS = "hosts";
    private static String DATACENTERS = "datacenters";
    private static String VMPOOLS = "vmpools";
    private static String STORAGEDOMAINS = "storagedomains";
    private static String NETWORKS = "networks";
    private static String GROUPS = "groups";
    private static String DISKS = "disks";
    private static String CLUSTERS = "clusters";

    private static Logger log = Logger.getLogger(RhevConnector.class);

    private API rhevApi;            // for the "top" level of the REST API containing
    private Hosts rhevHosts;          // list of hosts...
    private VMs rhevVms;            // list of vm's
    private StorageDomains rhevStorageDomains; // list of storage domains
    private Networks rhevNetworks;       // list of logical networks
    private Groups rhevGroups;         // list of groups
    private Disks rhevDisks;          // list of "disks"
    private DataCenters rhevDataCenters;    // list of data centrs
    private VmPools rhevVmPools;       // list of VmPools 
    private Clusters rhevClusters;       // list of clusters

    private RhevRestClient restClient = null;     // for the REST API access class
    private ConcurrentHashMap<String,              // to implement category->element->parameter->value
            ConcurrentHashMap<String,              //
                    ConcurrentHashMap<String, String>>> rhevMap = null;

    private ConcurrentHashMap<String, String> i2n = new ConcurrentHashMap<String, String>();

    private ArrayList<String> hostFilters;
    private ArrayList<String> VMFilters;

    private ConnectionState connectionState = ConnectionState.NASCENT;

    private int minSkippedBeforeDefunct = 0;  // settable
    private int minSkippedBeforeDelete = 1;  // settable
    private int minTimeBeforeDefunct = 0 * 60; // seconds
    private int minTimeBeforeDelete = 1 * 60; // seconds

    private Map<String, JAXBContext> contextMap = new ConcurrentHashMap<String, JAXBContext>();

    @Resource(name = ConnectorFactory.NAME)
    private ConnectorFactory connectorFactory;

    public RhevConnector()    // constructor
    {
        log.debug("inside VemaRhev() constructor");
    }

    public void setDefunctCriteria(int minSkipped, int minTime, int dieSkipped, int dieTime) {
        minSkippedBeforeDefunct = minSkipped;
        minTimeBeforeDefunct = minTime;
        minSkippedBeforeDelete = dieSkipped;
        minTimeBeforeDelete = dieTime;
    }

    private void connect() {
        switch (connectionState) {
            case CONNECTED:
                return;
            case FAILED:
                return;
            case TIMEDOUT:
                return;
            case CONNECTING:   // fallthru to DEFAULT
            case DISCONNECTED:   // fallthru to DEFAULT
            case NASCENT:   // fallthru to DEFAULT
            default:
                connectionState =
                        restClient.isConnectionOK()
                                ? ConnectionState.CONNECTED
                                : ConnectionState.FAILED
                ;
                // fall thru switch block
        }
        return;
    }

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        RedhatConnection connection = (RedhatConnection) monitorConnection;
        connect(connection.getServer(), connection.getUsername(), connection.getPassword(),
                connection.getRealm(), connection.getPort(), connection.getProtocol(), connection.getUri(),
                connection.getCertificateStore(), connection.getCertificatePassword());
    }

    @Override
    public void disconnect() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED)
            connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public ConnectionState getConnectionState() {
        if (connectionState == ConnectionState.CONNECTED)
            if (restClient.isConnectionOK() == false)
                connectionState = ConnectionState.DISCONNECTED;

        return connectionState;
    }

    @Override
    public void openConnection(MonitorConnection monitorConnection) throws ConnectorException {
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }
    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

    private void connect(String host, String login, String password,
                         String realm, String port, String protocol, String restbase,
                         String certspath, String keystorepass) throws ConnectorException {
        try {
            log.debug("\ninfo: connect( "
                            + "\n   host     = '" + (host == null ? "undef" : host) + "'"
                            + "\n   login    = '" + (login == null ? "undef" : login) + "'"
                            + "\n   pass     = '" + (password == null ? "undef" : "*******") + "'"
                            + "\n   realm    = '" + (realm == null ? "undef" : realm) + "'"
                            + "\n   port     = '" + (port == null ? "undef" : port) + "'"
                            + "\n   protocol = '" + (protocol == null ? "undef" : protocol) + "'"
                            + "\n   restbase = '" + (restbase == null ? "undef" : restbase) + "'"
                            + "\n   certpath = '" + (certspath == null ? "undef" : certspath) + "'"
                            + "\n   keypass  = '" + (keystorepass == null ? "undef" : keystorepass) + "'"
                            + "\n)\n"
            );

            restClient = new RhevRestClient(
                    host,           //
                    login,          //
                    password,       //
                    realm,          //
                    port,           //
                    protocol,       //
                    restbase,       //
                    certspath,      //
                    keystorepass    //
            );

        } catch (Exception e) {
            log.debug("\nconnect( "
                            + "\n   host     = '" + (host == null ? "undef" : host) + "'"
                            + "\n   login    = '" + (login == null ? "undef" : login) + "'"
                            + "\n   pass     = '" + (password == null ? "undef" : "*******") + "'"
                            + "\n   realm    = '" + (realm == null ? "undef" : realm) + "'"
                            + "\n   port     = '" + (port == null ? "undef" : port) + "'"
                            + "\n   protocol = '" + (protocol == null ? "undef" : protocol) + "'"
                            + "\n   restbase = '" + (restbase == null ? "undef" : restbase) + "'"
                            + "\n   certpath = '" + (certspath == null ? "undef" : certspath) + "'"
                            + "\n   keypass  = '" + (keystorepass == null ? "undef" : keystorepass) + "'"
                            + "\n)\n"
            );
            log.error("connect() - couldn't instantiate REST object", e);
            throw new ConnectorException("connect() - couldn't instantiate REST object", e);
        }
        API api = executeSingleAPI(API.class, "/api");
        if (api == null) { // TODO: executeSingleAPI should not gobble exceptions
            throw new ConnectorException("Failed to connect to RHEV ");
        }
        log.debug("past connect({parameters})");
        connect();
        if (!restClient.isConnectionOK())
            throw new ConnectorException("Failed to connect to RHEV ");
        log.debug("past connect()");
    }

    public String formatRhevMap() {
        StringBuilder s = new StringBuilder(10000);  // hint at initial sizing
        String indent = "";

        for (String category : rhevMap.keySet()) {
            indent = "";
            s.append(indent + "category: " + category + "\n");
            for (String element : rhevMap.get(category).keySet()) {
                indent = "  ";
                s.append(indent + "element: " + element + "\n");
                ArrayList<String> keys = new ArrayList<String>();
                keys.addAll(rhevMap.get(category).get(element).keySet());
                Collections.sort(keys);
                for (String parameter : keys) {
                    indent = "    ";
                    s.append(indent +
                            String.format(
                                    "parameter: %-40s: %s\n",
                                    parameter,
                                    rhevMap.get(category).get(element).get(parameter).toString()
                            ));
                }
            }
        }
        s.append("\n");
        return s.toString();
    }

    private void getAndCompileREST() throws ConnectorException {
        // first... make no assumptions about "currentness", and force a re-get of all
        // REST data.

        if (rhevMap != null) rhevMap.clear();
        if (i2n != null) i2n.clear();

        rhevApi = null; // whack 'em all first
        rhevHosts = null; // tho really not necessary
        rhevVms = null; // because object garbage
        rhevStorageDomains = null; // collection is quite well behaved
        rhevNetworks = null; // in Java
        rhevGroups = null;
        rhevDisks = null;
        rhevDataCenters = null;
        rhevVmPools = null;
        rhevClusters = null;

        rhevApi = executeSingleAPI(API.class, "/api");
        rhevHosts = retrieveHosts();
        rhevVms = retrieveVMs();
        if (collectionMode.isDoStorageDomains())
            rhevStorageDomains = retrieveStorageDomains();
        if (collectionMode.isDoNetworks())
            rhevNetworks = retrieveNetworks();
        if (collectionMode.isDoResourcePools())
            rhevVmPools = retrieveVmPools();

//        rhevGroups = retrieveGroups("/api/groups");
//        rhevDisks = retrieveDisks("/api/disks");
//        rhevDataCenters = retrieveDataCenters();
//        rhevClusters = retrieveClusters();

        compileAPI();              // then compile stats into rhevMap
        compileHosts();
        compileVMs();
        if (collectionMode.isDoStorageDomains())
            compileStorageDomains();
        if (collectionMode.isDoNetworks())
            compileNetworks();
        if (collectionMode.isDoResourcePools())
            compileVmPools();

//        compileGroups();
//        compileDisks();
//        compileDataCenters();
//        compileClusters();

        if (log.isDebugEnabled())
            log.debug(formatRhevMap());  // TRACE of objects...
    }

    /**
     * rhevMapAdd( category, element, parameter value )
     * <p/>
     * Adds values to rhevMap.category.element.parameter = value
     * ...conceptually
     */
    private void rhevMapAdd(String category, String element, String parameter, String value) throws ConnectorException {
        if (rhevMap == null)
            rhevMap = new ConcurrentHashMap<String,
                    ConcurrentHashMap<String,
                            ConcurrentHashMap<String, String>>>();

        if (!rhevMap.containsKey(category))
            rhevMap.put(category,
                    new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>());

        if (!rhevMap.get(category).containsKey(element))
            rhevMap.get(category).put(element,
                    new ConcurrentHashMap<String, String>());

        // now put it down!  zapnull() ensures no nulls, but "blanks" instead;
        try {
            rhevMap.get(category).get(element).put(parameter, zapnull(value));
        } catch (Exception e) {
            log.error(String.format(
                            "\nBad rhevMap('%s').get('%s').put('%s', '%s')\n",
                            category,
                            element,
                            parameter,
                            zapnull(value))
            );
        }
    }

    private void rhevMapDelete(String category, String element, String parameter) throws Exception {

        throw new Exception("PROGRAMMER: need to fix this to avoid nulls");
           /*
        if(   rhevMap == null
    	||  ! rhevMap.containsKey( category  )
    	||  ! rhevMap.get( category  ).containsKey( element )
    	||  ! rhevMap.get( category ).get( element ).containsKey( parameter ) )
    		return;

    	// this will trim off parameter->value pairs
    	rhevMap.get( category ).get( element ).remove( parameter );

    	// this will trim out element branches that don't have parameters
    	if( rhevMap.get( category ).get( element ).size() == 0 )
    		rhevMap.get( category ).remove( element );

    	// this will trim out category branches that don't have elements
    	if( rhevMap.get( category ).size() == 0)
    		rhevMap.remove( category );
    	*/
    }

    private String rhevMapGet(String category, String element, String parameter) {
        if (rhevMap == null
                || category == null
                || element == null
                || parameter == null
                || !rhevMap.containsKey(category)
                || !rhevMap.get(category).containsKey(element)
                || !rhevMap.get(category).get(element).containsKey(parameter)
                )
            return "";

        return rhevMap.get(category).get(element).get(parameter);
    }

    // ----------------------------------------------------------------------
    // COMPILE section... to make all the connections and names right!
    // ----------------------------------------------------------------------
    //
    public void compileAPI() throws ConnectorException {
        // nothing to do.  NO stats come from this level.
    }

    private void compileHosts() throws ConnectorException {
        if (rhevHosts == null)
            throw new ConnectorException("no data in hosts structure");

        String category = HOSTS;

        for (Host host : rhevHosts.getHosts()) {
            String id = host.getId();
            String clusterid = host.getCluster() == null ? "" : host.getCluster().getId();

            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", host.getName());
            rhevMapAdd(category, id, "address", host.getAddress() == null
                    ? "" : host.getAddress());
            rhevMapAdd(category, id, "certificate.organization", host.getCertificate() == null
                    ? "" : host.getCertificate().getOrganization());
            rhevMapAdd(category, id, "cluster.id", clusterid);
            rhevMapAdd(category, id, "cluster.name", i2nGet(clusterid));
            rhevMapAdd(category, id, "cpu.id", host.getCpu() == null
                    ? "" : host.getCpu().getId());
            rhevMapAdd(category, id, "cpu.name", host.getCpu() == null
                    ? "" : host.getCpu().getName());
            rhevMapAdd(category, id, "cpu.speed", host.getCpu() == null
                    || host.getCpu().getSpeed() == null
                    ? "" : host.getCpu().getSpeed().toString());
            rhevMapAdd(category, id, "cpu.cores", host.getCpu() == null
                    ? "" : host.getCpu().getTopology().getCores().toString());
            rhevMapAdd(category, id, "description", host.getDescription() == null
                    ? "" : host.getDescription());
            rhevMapAdd(category, id, "memory", host.getMemory() == null
                    ? "" : Conversion.byte2MB(
                    host.getMemory().toString()));
            rhevMapAdd(category, id, "max_sched_memory", host.getMaxSchedulingMemory() == null
                    ? "" : Conversion.byte2MB(
                    host.getMaxSchedulingMemory().toString()));
            rhevMapAdd(category, id, "active", host.getSummary() == null
                    ? "" : host.getSummary().getActive().toString());
            rhevMapAdd(category, id, "migrating", host.getSummary() == null
                    ? "" : host.getSummary().getMigrating().toString());
            rhevMapAdd(category, id, "total", host.getSummary() == null
                    ? "" : host.getSummary().getTotal().toString());
            rhevMapAdd(category, id, "type", host.getType() == null
                    ? "" : host.getType());
            rhevMapAdd(category, id, "port", host.getPort() == null
                    ? "" : host.getPort().toString());
            rhevMapAdd(category, id, "status.state", host.getStatus() == null
                    ? "" : host.getStatus().getState());
            rhevMapAdd(category, id, "status.detail", host.getStatus() == null
                    ? "" : host.getStatus().getDetail());

            for (Link link : host.getLinks()) {
                String rel = link.getRel();
                String href = link.getHref();
                if (rel.equalsIgnoreCase("storage"))  // unlike above is really "host storage" type
                {
                    if (host.getStatus() == null
                            || !host.getStatus().getState().equalsIgnoreCase("up"))
                        continue;   // short circuit - skip storage, has a BIG timeout!

                    HostStorage hoststorage = retrieveHostStorage(href);

                    int i = 0;
                    for (Storage storage : hoststorage.getStorage()) {
                        rhevMapAdd(category, id, "storage[" + i + "].id", storage.getId());
                        rhevMapAdd(category, id, "storage[" + i + "].name", storage.getName());
                        rhevMapAdd(category, id, "storage[" + i + "].size", storage.getLogicalUnits() == null
                                || storage.getLogicalUnits().isEmpty()
                                ? "" : Conversion.byte2MB(
                                storage.getLogicalUnits().get(0).getSize().toString()));
                        rhevMapAdd(category, id, "storage[" + i + "].host.id", storage.getHost() == null
                                ? "" : storage.getHost().getId());
                        i++;
                    }
                } else if (rel.equalsIgnoreCase("nics")) {
                    HostNics hostnics = retrieveHostNics(href);
                    int i = 0;
                    for (HostNIC hostnic : hostnics.getHostNics()) {
                        String nicid = hostnic.getNetwork() == null ? "" : hostnic.getNetwork().getId();

                        rhevMapAdd(category, id, "nic[" + i + "].id", hostnic.getId());
                        rhevMapAdd(category, id, "nic[" + i + "].name", hostnic.getName());
                        rhevMapAdd(category, id, "nic[" + i + "].network.id", nicid);
                        rhevMapAdd(category, id, "nic[" + i + "].network.name", i2nGet(nicid));
                        rhevMapAdd(category, id, "nic[" + i + "].mac", hostnic.getMac() == null
                                ? "" : hostnic.getMac().getAddress());
                        rhevMapAdd(category, id, "nic[" + i + "].ip", hostnic.getIp() == null
                                ? "" : hostnic.getIp().getAddress());
                        rhevMapAdd(category, id, "nic[" + i + "].mask", hostnic.getIp() == null
                                ? "" : hostnic.getIp().getNetmask());
                        rhevMapAdd(category, id, "nic[" + i + "].gateway", hostnic.getIp() == null
                                ? "" : hostnic.getIp().getGateway());
                        rhevMapAdd(category, id, "nic[" + i + "].speed", hostnic.getSpeed() == null
                                ? "" : Conversion.byte2KB(hostnic.getSpeed().toString()));
                        rhevMapAdd(category, id, "nic[" + i + "].boot", hostnic.getBootProtocol() == null
                                ? "" : hostnic.getBootProtocol());
                        rhevMapAdd(category, id, "nic[" + i + "].status.state", hostnic.getStatus() == null
                                ? "" : hostnic.getStatus().getState());

                        for (Link link2 : hostnic.getLinks()) {
                            String rel2 = link2.getRel();
                            String href2 = link2.getHref();
                            if (rel2.equalsIgnoreCase("statistics")) {
                                Statistics statistics = retrieveStatistics(href2);
                                for (Statistic stat : statistics.getStatistics()) {
                                    String name = stat.getName();
                                    String value =
                                            stat.getValues().getValues() == null
                                                    || stat.getValues().getValues().get(0) == null
                                                    || stat.getValues().getValues().get(0).getDatum() == null
                                                    ? "" : stat.getValues().getValues().get(0).getDatum().toString();

                                    rhevMapAdd(category, id, "nic[" + i + "].stat." + name + ".id", stat.getId());
                                    rhevMapAdd(category, id, "nic[" + i + "].stat." + name + ".name", stat.getName());
                                    rhevMapAdd(category, id, "nic[" + i + "].stat." + name + ".description", stat.getDescription());
                                    rhevMapAdd(category, id, "nic[" + i + "].stat." + name + ".type", stat.getType().toString());
                                    rhevMapAdd(category, id, "nic[" + i + "].stat." + name + ".unit", "KB/s");
                                    rhevMapAdd(category, id, "nic[" + i + "].stat." + name + ".value", Conversion.byte2KB(value));

                                }
                            }
                        }
                        i++;
                    }
                } else if (rel.equalsIgnoreCase("statistics")) {
                    Statistics statistics = retrieveStatistics(href);
                    int i = 0;
                    for (Statistic stat : statistics.getStatistics()) {
                        String name = stat.getName();
                        String value =
                                stat.getValues().getValues() == null
                                        || stat.getValues().getValues().get(0) == null
                                        || stat.getValues().getValues().get(0).getDatum() == null
                                        ? "" : stat.getValues().getValues().get(0).getDatum().toString();

                        if (name.startsWith("memory")
                                || name.startsWith("swap"))
                            value = Conversion.byte2MB(value);

                        rhevMapAdd(category, id, "stat." + name + ".id", stat.getId());
                        rhevMapAdd(category, id, "stat." + name + ".name", stat.getName());
                        rhevMapAdd(category, id, "stat." + name + ".description", stat.getDescription());
                        rhevMapAdd(category, id, "stat." + name + ".type", stat.getType().toString());
                        rhevMapAdd(category, id, "stat." + name + ".unit", "KB/s");
                        rhevMapAdd(category, id, "stat." + name + ".value", value);

                        i++;
                    }
                } else if (rel.equalsIgnoreCase("tags"))
                    continue; // do nothing
                else if (rel.equalsIgnoreCase("permissions"))
                    continue; // do nothing
                else
                    continue; // do nothing ... though more worrisome - all cases should be above
            }
        }
    }

    private void compileVMs() throws ConnectorException {
        if (rhevVms == null)
            throw new ConnectorException("no data in VMs structure");

        String category = VMS;

        for (VM vm : rhevVms.getVMs()) {
            String id = vm.getId();
            String hostid = vm.getHost() == null ? "" : vm.getHost().getId();
            String clusterid = vm.getCluster() == null ? "" : vm.getCluster().getId();
            String templateid = vm.getTemplate() == null ? "" : vm.getTemplate().getId();
            String vmpoolid = vm.getVmPool() == null ? "" : vm.getVmPool().getId();

            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", vm.getName());
            rhevMapAdd(category, id, "type", vm.getType());
            rhevMapAdd(category, id, "status.state", vm.getStatus().getState());
            rhevMapAdd(category, id, "status.detail", vm.getStatus().getDetail());
            rhevMapAdd(category, id, "memory", vm.getMemory() == null
                    ? "" : Conversion.byte2MB(vm.getMemory().toString()));
            rhevMapAdd(category, id, "cpu.cores", vm.getCpu() == null
                    ? "" : vm.getCpu().getTopology().getCores().toString());
            rhevMapAdd(category, id, "os.type", vm.getOs() == null
                    ? "" : vm.getOs().getType());
            rhevMapAdd(category, id, "display.type", vm.getDisplay() == null
                    ? "" : vm.getDisplay().getType());
            rhevMapAdd(category, id, "display.address", vm.getDisplay() == null
                    ? "" : vm.getDisplay().getAddress());
            rhevMapAdd(category, id, "display.port", vm.getDisplay().getPort() == null
                    ? "" : vm.getDisplay().getPort().toString());
            rhevMapAdd(category, id, "display.secure_port", vm.getDisplay().getSecurePort() == null
                    ? "" : vm.getDisplay().getSecurePort().toString());
            rhevMapAdd(category, id, "display.monitors", vm.getDisplay().getMonitors() == null
                    ? "" : vm.getDisplay().getMonitors().toString());
            rhevMapAdd(category, id, "host.id", hostid);
            rhevMapAdd(category, id, "host.name", i2nGet(hostid));
            rhevMapAdd(category, id, "cluster.id", clusterid);
            rhevMapAdd(category, id, "cluster.name", i2nGet(clusterid));
            rhevMapAdd(category, id, "template.id", templateid);
            rhevMapAdd(category, id, "template.name", i2nGet(templateid));
            rhevMapAdd(category, id, "start_time", vm.getStartTime() == null
                    ? "" : vm.getStartTime().toString());
            rhevMapAdd(category, id, "origin", vm.getOrigin() == null
                    ? "" : vm.getOrigin());
            rhevMapAdd(category, id, "memory_policy.guaranteed", vm.getMemoryPolicy() == null
                    ? "" : Conversion.byte2MB(vm.getMemoryPolicy().getGuaranteed().toString()));
            rhevMapAdd(category, id, "vmpool.id", vmpoolid);
            rhevMapAdd(category, id, "vmpool.name", i2nGet(vmpoolid));

            if (vm.getGuestInfo() != null) {
                IPs ips = vm.getGuestInfo().getIps(); //                arrgghh....
                int ii = 0;
                for (IP ip : ips.getIPs()) {
                    rhevMapAdd(category, id, "ip[" + ii + "]", ip.getAddress());
                    ii++;
                }
            }

            for (Link link : vm.getLinks()) {
                String rel = link.getRel();
                String href = link.getHref();
                if (rel.equalsIgnoreCase("disks")) {
                    Disks disks = retrieveDisks(href);
                    int i = 0;
                    for (Disk disk : disks.getDisks()) {
                        rhevMapAdd(category, id, "disk[" + i + "].id", disk.getId());
                        rhevMapAdd(category, id, "disk[" + i + "].name", disk.getName());
                        rhevMapAdd(category, id, "disk[" + i + "].size", Conversion.byte2MB(disk.getSize().toString()));
                        rhevMapAdd(category, id, "disk[" + i + "].provisioned_size", Conversion.byte2MB(disk.getProvisionedSize().toString()));
                        rhevMapAdd(category, id, "disk[" + i + "].actual_size", Conversion.byte2MB(disk.getActualSize().toString()));
                        rhevMapAdd(category, id, "disk[" + i + "].status.state", disk.getStatus().getState());
                        i++;
                    }
                } else if (rel.equalsIgnoreCase("nics")) {
                    Nics nics = retrieveNics(href);
                    int i = 0;
                    for (NIC nic : nics.getNics()) {
                        String vmid = nic.getVm() == null ? "" : nic.getVm().getId();
                        String netid = nic.getNetwork() == null ? "" : nic.getNetwork().getId();

                        rhevMapAdd(category, id, "nic[" + i + "].id", nic.getId());
                        rhevMapAdd(category, id, "nic[" + i + "].name", nic.getName());
                        rhevMapAdd(category, id, "nic[" + i + "].vm.id", vmid);
                        rhevMapAdd(category, id, "nic[" + i + "].vm.name", i2nGet(vmid));
                        rhevMapAdd(category, id, "nic[" + i + "].network.id", netid);
                        rhevMapAdd(category, id, "nic[" + i + "].network.name", i2nGet(netid));
                        rhevMapAdd(category, id, "nic[" + i + "].mac", nic.getMac() == null
                                ? "" : nic.getMac().getAddress());
                        rhevMapAdd(category, id, "nic[" + i + "].active", nic.isActive() == null
                                ? "" : nic.isActive().toString());
                        i++;
                    }
                } else if (rel.equalsIgnoreCase("statistics")) {
                    Statistics statistics = retrieveStatistics(href);
                    int i = 0;
                    for (Statistic stat : statistics.getStatistics()) {
                        String name = stat.getName();

                        rhevMapAdd(category, id, "stat." + name + ".id", stat.getId());
                        rhevMapAdd(category, id, "stat." + name + ".name", stat.getName());
                        rhevMapAdd(category, id, "stat." + name + ".description", stat.getDescription());
                        rhevMapAdd(category, id, "stat." + name + ".type", stat.getType().toString());
                        rhevMapAdd(category, id, "stat." + name + ".unit", stat.getUnit().toString());
                        String value =
                                stat.getValues().getValues() == null
                                        || stat.getValues().getValues().get(0) == null
                                        || stat.getValues().getValues().get(0).getDatum() == null
                                        ? "" : stat.getValues().getValues().get(0).getDatum().toString();

                        if (name.startsWith("memory")
                                || name.startsWith("swap"))
                            value = Conversion.byte2MB(value);

                        rhevMapAdd(category, id, "stat." + name + ".value", value);
                        i++;
                    }
                } else if (rel.equalsIgnoreCase("cdroms"))
                    continue; // do nothing
                else if (rel.equalsIgnoreCase("snapshots"))
                    continue; // do nothing
                else if (rel.equalsIgnoreCase("tags"))
                    continue; // do nothing
                else if (rel.equalsIgnoreCase("permissions"))
                    continue; // do nothing
                else
                    continue; // do nothing ... though more worrisome - all cases should be above
            }
        }
    }

    private void compileStorageDomains() throws ConnectorException {
        if (rhevStorageDomains == null)
            throw new ConnectorException("no data in StorageDomains structure");
        String category = STORAGEDOMAINS;
        for (StorageDomain sd : rhevStorageDomains.getStorageDomains()) {
            String id = sd.getId();
            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", sd.getName());
            rhevMapAdd(category, id, "available", sd.getAvailable() == null
                    ? "" : Conversion.byte2MB(sd.getAvailable().toString()));
            rhevMapAdd(category, id, "committed", sd.getCommitted() == null
                    ? "" : Conversion.byte2MB(sd.getCommitted().toString()));
            rhevMapAdd(category, id, "used", sd.getUsed() == null
                    ? "" : Conversion.byte2MB(sd.getUsed().toString()));
            rhevMapAdd(category, id, "storageFormat", sd.getStorageFormat());
            rhevMapAdd(category, id, "type", sd.getType());
            rhevMapAdd(category, id, "storage.type", sd.getStorage() == null
                    ? "" : sd.getStorage().getType());
            rhevMapAdd(category, id, "storage.path", sd.getStorage() == null
                    ? "" : sd.getStorage().getPath());
        }
    }

    private void compileNetworks() throws ConnectorException {
        if (rhevNetworks == null)
            throw new ConnectorException("no data in Networks structure");

        String category = NETWORKS;    // kind of a constant...

        for (Network net : rhevNetworks.getNetworks()) {
            String id = net.getId();
            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", net.getName());
        }
    }

    private void compileGroups() throws ConnectorException {
        if (rhevGroups == null)
            throw new ConnectorException("no data in Groups structure");

        String category = GROUPS;    // kind of a constant...

        for (Group grp : rhevGroups.getGroups()) {
            String id = grp.getId();
            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", grp.getName());
        }
    }

    private void compileDisks() throws ConnectorException {
        if (rhevDisks == null)
            throw new ConnectorException("no data in Disks structure");

        String category = DISKS;    // kind of a constant...

        for (Disk disk : rhevDisks.getDisks()) {
            String id = disk.getId();

            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", disk.getName());
            rhevMapAdd(category, id, "alias", disk.getAlias());
            rhevMapAdd(category, id, "image_id", disk.getImageId());
            rhevMapAdd(category, id, "size", disk.getSize() == null
                    ? "" : Conversion.byte2MB(disk.getSize().toString()));
            rhevMapAdd(category, id, "provisioned_size", disk.getProvisionedSize() == null
                    ? "" : Conversion.byte2MB(disk.getProvisionedSize().toString()));
            rhevMapAdd(category, id, "actual_size", disk.getActualSize() == null
                    ? "" : Conversion.byte2MB(disk.getActualSize().toString()));
            rhevMapAdd(category, id, "status.state", disk.getStatus() == null
                    ? "" : disk.getStatus().getState());
            rhevMapAdd(category, id, "status.detail", disk.getStatus() == null
                    ? "" : disk.getStatus().getDetail());
            rhevMapAdd(category, id, "interface", disk.getInterface());
            rhevMapAdd(category, id, "format", disk.getFormat());

            for (Link link : disk.getLinks()) {
                String rel = link.getRel();
                String href = link.getHref();
                if (rel.equalsIgnoreCase("statistics")) {
                    Statistics statistics = retrieveStatistics(href);
                    int i = 0;
                    for (Statistic stat : statistics.getStatistics()) {
                        String name = stat.getName();

                        rhevMapAdd(category, id, "stat." + name + ".id", stat.getId());
                        rhevMapAdd(category, id, "stat." + name + ".name", stat.getName());
                        rhevMapAdd(category, id, "stat." + name + ".description", stat.getDescription());
                        rhevMapAdd(category, id, "stat." + name + ".type", stat.getType().toString());
                        rhevMapAdd(category, id, "stat." + name + ".unit", stat.getUnit().toString());
                        String value =
                                stat.getValues().getValues() == null
                                        || stat.getValues().getValues().get(0) == null
                                        || stat.getValues().getValues().get(0).getDatum() == null
                                        ? "" : stat.getValues().getValues().get(0).getDatum().toString();

                        if (name.startsWith("memory")
                                || name.startsWith("swap"))
                            value = Conversion.byte2MB(value);

                        rhevMapAdd(category, id, "stat." + name + ".value", value);
                        i++;
                    }
                } else
                    continue; // do nothing ... though more worrisome - all cases should be above
            }
        }
    }

    private void compileDataCenters() throws ConnectorException {
        if (rhevDataCenters == null)
            throw new ConnectorException("no data in DataCenters structure");

        String category = DATACENTERS;    // kind of a constant...

        for (DataCenter dc : rhevDataCenters.getDataCenters()) {
            String id = dc.getId();
            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", dc.getName());
            rhevMapAdd(category, id, "storage_type", dc.getStorageType());
            rhevMapAdd(category, id, "description", dc.getDescription());
            rhevMapAdd(category, id, "storage_format", dc.getStorageFormat());
            rhevMapAdd(category, id, "status.state", dc.getStatus() == null
                    ? "" : dc.getStatus().getState());
            rhevMapAdd(category, id, "status.detail", dc.getStatus() == null
                    ? "" : dc.getStatus().getDetail());
        }
    }

    private void compileVmPools() throws ConnectorException {
        if (rhevVmPools == null)
            throw new ConnectorException("no data in VmPools structure");

        String category = VMPOOLS;    // kind of a constant...

        for (VmPool vmpool : rhevVmPools.getVmPools()) {
            String id = vmpool.getId();
            String clusterid = vmpool.getCluster() == null ? "" : vmpool.getCluster().getId();

            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", vmpool.getName());
            rhevMapAdd(category, id, "description", vmpool.getDescription());
            rhevMapAdd(category, id, "cluster.id", clusterid);
            rhevMapAdd(category, id, "cluster.name", i2nGet(clusterid));
            rhevMapAdd(category, id, "size", vmpool.getSize() == null
                    ? "" : vmpool.getSize().toString());

        }
    }

    private void compileClusters() throws ConnectorException {
        if (rhevClusters == null)
            throw new ConnectorException("no data in Clusters structure");

        String category = CLUSTERS;    // kind of a constant...

        for (Cluster clu : rhevClusters.getClusters()) {
            String id = clu.getId();
            rhevMapAdd(category, id, "id", id);
            rhevMapAdd(category, id, "name", clu.getName());
            rhevMapAdd(category, id, "description", clu.getDescription());
            rhevMapAdd(category, id, "cpu.id", clu.getCpu() == null
                    ? "" : clu.getCpu().getId());
            rhevMapAdd(category, id, "datacenter.id", clu.getDataCenter() == null
                    ? "" : clu.getDataCenter().getId());
        }
    }


    private Hosts retrieveHosts() throws ConnectorException {
        Hosts hosts = executeAPI(Hosts.class, "/api/hosts");
        if (hosts == null)
            throw new ConnectorException("Couldn't retrieve Hosts");
        for (Host host : hosts.getHosts())
            i2n.put(zapnull(host.getId()), zapnull(host.getName()));
        return hosts;
    }

    private Statistics retrieveStatistics(String entrypoint) throws ConnectorException {
        Statistics statistics = executeAPI(Statistics.class, (entrypoint == null) ? "/api/statistics" : entrypoint);
        if (statistics == null)
            throw new ConnectorException("Couldn't retrieve Statistics");
        return statistics;
    }

    private VMs retrieveVMs() throws ConnectorException {
        VMs vms = executeAPI(VMs.class, "/api/vms");
        if (vms == null)
            throw new ConnectorException("Couldn't retrieve VMs");
        for (VM vm : vms.getVMs())
            i2n.put(zapnull(vm.getId()), zapnull(vm.getName()));
        return vms;
    }

    private StorageDomains retrieveStorageDomains() throws ConnectorException {
        StorageDomains sd = executeAPI(StorageDomains.class, "/api/storagedomains");
        if (sd == null)
            throw new ConnectorException("Couldn't retrieve StorageDomains");
        for (StorageDomain storagedomain : sd.getStorageDomains())
            i2n.put(zapnull(storagedomain.getId()), zapnull(storagedomain.getName()));
        return sd;
    }

    private Networks retrieveNetworks() throws ConnectorException {
        Networks net = executeAPI(Networks.class, "/api/networks");
        if (net == null)
            throw new ConnectorException("Couldn't retrieve Networks");
        for (Network network : net.getNetworks())
            i2n.put(zapnull(network.getId()), zapnull(network.getName()));
        return net;
    }

    private Disks retrieveDisks(String entrypoint) throws ConnectorException {
        Disks disks = executeAPI(Disks.class, (entrypoint == null) ? "/api/disks" : entrypoint);
        if (disks == null)
            throw new ConnectorException("Couldn't retrieve Disks");
        for (Disk disk : disks.getDisks())
            i2n.put(zapnull(disk.getId()), zapnull(disk.getName()));
        return disks;
    }


    private HostStorage retrieveHostStorage(String entrypoint) throws ConnectorException {
        HostStorage hostStorage = executeAPI(HostStorage.class, (entrypoint == null) ? "/api/hoststorage" : entrypoint);
        if (hostStorage == null)
            throw new ConnectorException("Couldn't retrieve HostStorage");
        return hostStorage;
    }

    private Nics retrieveNics(String entrypoint) throws ConnectorException {
        Nics nics = executeAPI(Nics.class, (entrypoint == null) ? "/api/nics" : entrypoint);
        if (nics == null)
            throw new ConnectorException("Couldn't retrieve Nics");
        for (NIC nic : nics.getNics())
            i2n.put(zapnull(nic.getId()), zapnull(nic.getName()));
        return nics;
    }

    private HostNics retrieveHostNics(String entrypoint) throws ConnectorException {
        HostNics hostnics = executeAPI(HostNics.class, (entrypoint == null) ? "/api/hostnics" : entrypoint);
        if (hostnics == null)
            throw new ConnectorException("Couldn't retrieve HostNics");
        for (HostNIC hostnic : hostnics.getHostNics())
            i2n.put(zapnull(hostnic.getId()), zapnull(hostnic.getName()));
        return hostnics;
    }

    private DataCenters retrieveDataCenters() throws ConnectorException {
        DataCenters dcs = executeAPI(DataCenters.class, "/api/datacenters");
        if (dcs == null)
            throw new ConnectorException("Couldn't retrieve DataCenters");
        for (DataCenter dc : dcs.getDataCenters())
            i2n.put(zapnull(dc.getId()), zapnull(dc.getName()));
        return dcs;
    }

    private VmPools retrieveVmPools() throws ConnectorException {
        VmPools vmpools = executeAPI(VmPools.class, "/api/vmpools");
        if (vmpools == null)
            throw new ConnectorException("Couldn't retrieve VmPools");
        for (VmPool vmp : vmpools.getVmPools())
            i2n.put(zapnull(vmp.getId()), zapnull(vmp.getName()));
        return vmpools;
    }

    private Clusters retrieveClusters() throws ConnectorException {
        Clusters clusters = executeAPI(Clusters.class, "/api/clusters");
        if (clusters == null)
            throw new ConnectorException("Couldn't retrieve Clusters");
        for (Cluster cluster : clusters.getClusters())
            i2n.put(zapnull(cluster.getId()), zapnull(cluster.getName()));
        return clusters;
    }

    private String zapnull(String value) {
        return value == null ? "" : value;
    }

    private String i2nGet(String key) {
        if (key == null) return "(null)";
        if (key.isEmpty()) return "";
        if (i2n.get(key) == null) return "";
        return i2n.get(key);
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState,
                                          List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException {
        boolean onlyRequestedMetrics = true;
        boolean crushMetricsIfDown = true;
        boolean deleteDefunctMembers = true;

        MonitoringState monitoringState = new MonitoringState();
        Map<String, RhevVM> vmPool = new ConcurrentHashMap<String, RhevVM>();
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        long startTime = System.currentTimeMillis();  // capture "now"
        StringBuffer timeStamp = new StringBuffer(100);

        if (priorState == null)    // safety check BEFORE any return()
            priorState = new MonitoringState();

        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.REDHAT);

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("collectMetrics(): not connected");
            return priorState;
        }

        timeStamp.append("vmcall("
                + Double.toString((System.currentTimeMillis() - startTime) / 1000.0) + ") ");

        // ------------------------------------------------------------
        // Basic method: go get REST API tree of data
        // ... decode it into our HOSTLIST structure
        // ... patch together all the symoblic references
        // ... drop it out to the bottom clean-up code.
        // ------------------------------------------------------------

        getAndCompileREST();

        if (rhevMap == null
                || rhevMap.get(VMS) == null
                || rhevMap.get(HOSTS) == null) {
            throw new ConnectorException("No/incomplete data in RHEV map object.");
        }

        gatherVirtualMachineMetrics(vmPool, queryPool, vmQueries);
        gatherHostsMetrics(monitoringState, queryPool, hostQueries);

        timeStamp.append("premerge("
                + Double.toString((System.currentTimeMillis() - startTime) / 1000.0) + ") ");


        // -------------------------------------------------
        // LINKS the VMs to HOSTs, one at a time
        // -------------------------------------------------
        StringBuilder sb = new StringBuilder();
        sb.append("Linking VMs to Hosts\n");
        if (log.isDebugEnabled())
            log.debug("debug 6a");
        for (String vmoname : vmPool.keySet()) {
            RhevVM vmo = vmPool.get(vmoname);
            String vmHypervisor = vmo.getHypervisor();
            BaseHost ho;
            if (log.isDebugEnabled())
                log.debug("debug 6b: vm='" + vmoname + "' host='" + vmHypervisor + "'");
            if ((ho = monitoringState.hosts().get(vmHypervisor)) == null
                    && (ho = monitoringState.hosts().get(ConnectorConstants.HOSTLESS_VMS)) == null) {
                ho = new RhevHost(ConnectorConstants.HOSTLESS_VMS);
                if (log.isDebugEnabled())
                    log.debug("debug 6c: set up a HOSTLESS list, because host '" + vmHypervisor + "' not found");
                ho.setRunState(((RhevHost) ho).getMonitorState());
                monitoringState.hosts().put(ConnectorConstants.HOSTLESS_VMS, ho);  // kind of the cart before the horse, but objects are smart
            }
            sb.append(String.format("vm: '%-30s'  hy: '%-30s' --> '%s'\n", vmoname, vmHypervisor, ho.getHostName()));
            ho.putVM(vmoname, vmo);
        }
        sb.append("--- END ---\n\n");
        if (log.isDebugEnabled())
            log.debug(sb.toString());

        mergeMonitoringResults(priorState.hosts(), monitoringState.hosts(), deleteDefunctMembers);
        monitoringState = null;   // explicitly kill off objects!

        if (collectionMode.isDoStorageDomains()) {
            Map<String, RhevStorage> storageMap = gatherStorageMetrics(vmQueries);
            Map<String, RhevStorage> storageVms = new ConcurrentHashMap<String, RhevStorage>();
            for (String storageName : storageMap.keySet()) {
                RhevStorage storage = storageMap.get(storageName);
                String prefixedName = provider.getPrefix(ConfigurationProvider.PrefixType.VmStorage) + storageName;
                storage.setHostName(prefixedName);
                storageVms.put(prefixedName, storage);
            }
            mergeMonitoringResults(priorState.hosts(), storageVms, deleteDefunctMembers);
        }

        if (collectionMode.isDoNetworks()) {
        }

        // ----------------------------------------------------------------------
        // now clear out the metrics that the upper code doesn't want to monitor
        // ----------------------------------------------------------------------
        // The special "object.getMergeCount() > 1" case below INITIALLY suppresses
        // the metric-crushing logic.  Thus, on new HOSTs and VMs, when they first
        // appear in the tree, their metrics will be passed along, whatever their
        // values.  (VIM25 values).  After that, on the 2nd and subsequent merge,
        // the metrics will be suppressed IF the getRunState is either DOWN or SUSPEND
        // ... which lightens traffic substantially, and reduces server-side load as well.
        //
        // NOTE: this is "> 1" (instead of >= 1) because these tests are done POST-merging
        //       where merging increments the merge counter.  Thus the brand new objects
        //       will have a merge-count of exactly 1, here.
        // ----------------------------------------------------------------------

        if (onlyRequestedMetrics || crushMetricsIfDown)  // a STRIP OUT operation!
        {
            for (BaseHost hosto : priorState.hosts().values()) {
                boolean crushHostMetrics = false;

                if ((crushMetricsIfDown)
                        && (hosto.getMergeCount() > 1)  // VERY useful special case (see above)
                        && (hosto.getRunState().contains("DOWN")
                        || hosto.getRunState().contains("SUSPEND")))
                    crushHostMetrics = true;

                for (String metricName : hosto.getMetricPool().keySet())
                    if (crushHostMetrics || !hosto.getMetric(metricName).isMonitored())
                        hosto.getMetricPool().remove(metricName);

                for (String configName : hosto.getConfigPool().keySet())
                    if (crushHostMetrics || !hosto.getConfig(configName).isMonitored())
                        hosto.getConfigPool().remove(configName);

                for (BaseVM vmo : hosto.getVMPool().values()) {
                    boolean crushVMMetrics = false;

                    if ((crushMetricsIfDown) &&
                            (vmo.getMergeCount() > 1) &&  // VERY useful special case (see above)
                            (vmo.getRunState().contains("DOWN")
                                    || vmo.getRunState().contains("SUSPEND")
                            )) {
                        crushVMMetrics = true;
                    }

                    for (String metricName : vmo.getMetricPool().keySet())
                        if (crushVMMetrics || !vmo.getMetric(metricName).isMonitored())
                            vmo.getMetricPool().remove(metricName);

                    for (String configName : vmo.getConfigPool().keySet())
                        if (crushVMMetrics || !vmo.getConfig(configName).isMonitored())
                            vmo.getConfigPool().remove(configName);
                }
            }
        }

        if (log.isDebugEnabled()) {
            timeStamp.append("end("
                    + Double.toString((System.currentTimeMillis() - startTime) / 1000.0) + ") ");
            log.debug("getListHost(" + (restClient == null ? "(undef)" : restClient.getHost()) + ") timestamps: [" + timeStamp.toString() + "]");
        }
        // return priorVBH.size() == 0 ? null : priorVBH;   // using NULL as flag
        return priorState;   // which must never be null by this point in code.
    }

    private void mergeMonitoringResults(
            Map<String, BaseHost> baseHosts,
            Map<String, ? extends BaseHost> newHosts,
            boolean autoDelete) {

        // set up SKIP counters... to DETECT dropped/moved hosts & vms.
        // log.info( "HostListMerge 1" );
        for (BaseHost hosto : newHosts.values()) {
            hosto.incSkipped();
            for (BaseVM vmo : hosto.getVMPool().values())
                vmo.incSkipped();
        }

        // log.info( "HostListMerge 2" );
        int added = 0;
        // now try to merge in the newstuff...
        for (String host : newHosts.keySet()) {
            // begin by ensuring target object exists, for hosts.
            // log.info( "HostListMerge 2a" );
            if (!baseHosts.containsKey(host))  // if BASE LIST doesn't have the host
            {
                added++;
                baseHosts.put(host, new RhevHost(host));
            }

            // log.info( "HostListMerge 2x" );
            // and target object (virtual machines) exists, too.
            for (String vm : newHosts.get(host).getVMPool().keySet()) {
                // log.info( "HostListMerge 2b" );
                if (!baseHosts.get(host).getVMPool().containsKey(vm)) {
                    added++;
                    baseHosts.get(host).getVMPool().put(vm, new RhevVM(vm));
                }
                // log.info( "HostListMerge 2c" );
            }
            // now merge them
            baseHosts.get(host).mergeInNew(newHosts.get(host));
            // log.info( "HostListMerge 2z" );
        }

        // log.info( "HostListMerge 3" );
        // DELETION OF ORPHANED OBJECTS HERE  // This code deletes 'em.
        int deleted = 0;
        int vmcounter = 0;
        int hostcounter = 0;

        for (String host : baseHosts.keySet()) {
            hostcounter++;
            // log.info( "HostListMerge 3a host='" + host + "'" );
            for (String vm : baseHosts.get(host).getVMPool().keySet()) {
                vmcounter++;
                // log.info( "HostListMerge 3b vm  ='" + vm + "'" );
                if (autoDelete
                        && baseHosts.get(host).getVM(vm).isStale(minSkippedBeforeDelete, minTimeBeforeDelete)) {
                    // 130502.rlynch: CONFIRMED is working right.  No duplication of VMs between merges.
                    baseHosts.get(host).getVMPool().remove(vm);
                    deleted++;
                    log.info("'" + vm + "'... orphaned VM deleted (min#:" + minSkippedBeforeDelete + " minT:" + minTimeBeforeDelete + ")");
                }
            }
            // log.info( "HostListMerge 3c" );
            if (autoDelete
                    && baseHosts.get(host).isStale(minSkippedBeforeDelete, minTimeBeforeDelete)) {
                baseHosts.remove(host);
                deleted++;
                log.debug("'" + host + "'... orphaned HOST deleted (min#:" + minSkippedBeforeDelete + " minT:" + minTimeBeforeDelete + ")");
            }
            // log.info( "HostListMerge 3d" );
        }
        log.info("Hypervisors: (" + hostcounter
                + "),  VMs: (" + vmcounter
                + "),  Added: (" + added
                + "),  Deleted: (" + deleted
                + ")");
    }

    private static class TrustAllTrustManager implements
            javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    private static void trustAllHttpsCertificates() throws Exception {
        // Create a trust manager that does not validate certificate chains:
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(
                sc.getSocketFactory());
    }

    /**
     * Establishes session with the virtual center server.
     *
     * @throws Exception the exception
     */
    public void attach() throws Exception       // this was connect() from the VIM25 example
    {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };

        //trustAllHttpsCertificates();
        //HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }


    private static void printSoapFaultException(SOAPFaultException sfe) {
        //log.debug( "SOAP Fault -" );
        if (sfe.getFault().hasDetail())
            log.debug(sfe.getFault().getDetail().getFirstChild().getLocalName());

        if (sfe.getFault().getFaultString() != null)
            log.debug("\n Message: " + sfe.getFault().getFaultString());
    }

    private ConcurrentHashMap<String, Boolean> everSeen = new ConcurrentHashMap<String, Boolean>();

    private void logonce(String message) {
        if (everSeen.get(message) == null) {
            everSeen.put(message, true);
            log.info(message);
        }
    }

    @Override
    public DataCenterInventory gatherInventory() {
        InventoryBrowser inventoryBrowser = new RhevInventoryBrowser(restClient);
        InventoryOptions options = new InventoryOptions(collectionMode.isDoHosts(), collectionMode.isDoStorageDomains(),
                                            collectionMode.isDoNetworks(), collectionMode.isDoResourcePools(),
                                            collectionMode.isDoTaggedGroups(), collectionMode.getGroupTag());
        DataCenterInventory inventory = inventoryBrowser.gatherInventory(options);
        return inventory;
    }

    private void gatherVirtualMachineMetrics(Map<String, RhevVM> vmPool,
                                             Map<String, BaseQuery> queryPool,
                                             List<BaseQuery> vmQueries) throws ConnectorException {
        String category = VMS;
        StringBuilder message = new StringBuilder();
        message.append("gatherVirtualMachineMetrics(), building " + category + ":\n");

        RhevVM metricsDefaults = new RhevVM("_temp");

        for (BaseQuery accessor : metricsDefaults.getDefaultMetricList())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : metricsDefaults.getDefaultConfigList())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : metricsDefaults.getDefaultSyntheticList())
            queryPool.put(accessor.getQuery(), accessor);

        if (vmQueries != null) {
            for (BaseQuery accessor : vmQueries) // LAST to OVERRIDE the above defaults
                if (accessor.getSourceType() == SourceType.diagnostics || accessor.getSourceType() == SourceType.compute) {
                    queryPool.put(accessor.getQuery(), accessor);
                }
        }

        // process all the VMs
        for (String element : rhevMap.get(category).keySet()) {
            message.append("bing: " + element + "\n");
            ConcurrentHashMap<String, String> server = rhevMap.get(category).get(element);

            String name = zapnull(server.get("name"));
            String description = zapnull(server.get("description"));
            String textDate = zapnull(server.get("start_time"));
            String macaddress = zapnull(server.get("nic[0].mac"));
            String ipaddress = zapnull(server.get("ip[0]"));
            String clustername = zapnull(server.get("cluster.name"));
            String hostname = zapnull(server.get("host.name"));
            String vmpoolname = zapnull(server.get("vmpool.name"));
            String statusstate = zapnull(server.get("status.state"));

            if (log.isDebugEnabled())
                log.debug(""
                                + "element          = '" + element + "'\n"
                                + "vmo.name         = '" + name + "'\n"
                                + "vmo.description  = '" + description + "'\n"
                                + "vmo.textDate     = '" + textDate + "'\n"
                                + "vmo.macaddress   = '" + macaddress + "'\n"
                                + "vmo.ipaddress    = '" + ipaddress + "'\n"
                                + "vmo.clustername  = '" + clustername + "'\n"
                                + "vmo.hostname     = '" + hostname + "'\n"
                                + "vmo.vmpoolname   = '" + vmpoolname + "'\n"
                                + "vmo.statusstate  = '" + statusstate + "'\n"
                );

            RhevVM vmo = new RhevVM(name);

            vmPool.put(name, vmo);  // kind of the cart before the horse, but objects are smart

            vmo.setBootDate(textDate, ConnectorConstants.RHEV_DATE_FORMAT);
            vmo.setGuestState(statusstate);
            vmo.setLastUpdate();
            vmo.setHostGroup(clustername);
            vmo.setIpAddress(ipaddress);
            vmo.setMacAddress(macaddress);
            vmo.setHypervisor(hostname);
            vmo.setVMName(name);
            vmo.setVmGroup(vmpoolname);


            for (String parameter : server.keySet()) {
                BaseQuery vbq = queryPool.get(parameter);
                if (vbq == null)  // if parameter not in (pool of queries to do)
                    continue;

                if (vbq.isGraphed() == false && vbq.isMonitored() == false)
                    continue;      // short-circuit for non-used parameters

                BaseMetric vbm = new BaseMetric(
                        parameter,
                        vbq.getWarning(),
                        vbq.getCritical(),
                        vbq.isGraphed(),
                        vbq.isMonitored(),
                        vbq.getCustomName()
                );

                if (vbq.isTraced())
                    vbm.setTrace();

                String value = server.get(parameter);

                vbm.setValue(value);

                if (parameter.endsWith("value")     // RHEV specific
                        && (parameter.startsWith("stat.")   // RHEV specific
                        || parameter.startsWith("nic")     // RHEV specific
                )) {
                    vmo.putMetric(parameter, vbm);
                } else {
                    vmo.putConfig(parameter, vbm);
                }

                message.append(String.format("%-80s: %s\n",
                        category + "/" + element + "/" + parameter, value));
            }

            for (String query : queryPool.keySet()) {
                BaseSynthetic vbs;

                // we're ONLY working with the synthetics for vms...
                if (!query.startsWith("syn.vm."))
                    continue;

                if ((vbs = metricsDefaults.getSynthetic(query)) != null) {
                    BaseQuery vbq = queryPool.get(query);

                    if (vbq == null)  // indicative of bigger problems...
                        continue;

                    BaseMetric vbm = new BaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored(),
                            vbq.getCustomName()
                    );

                    String value1 = vmo.getValueByKey(vbs.getLookup1());
                    String value2 = vmo.getValueByKey(vbs.getLookup2());

                    if (value1 == null)
                        logonce("Couldn't find '" + vbs.getLookup1() + "' in VM metrics");

                    if (value2 == null)
                        logonce("Couldn't find '" + vbs.getLookup2() + "' in VM metrics");

                    if (vbq.isTraced())
                        vbm.setTrace();

                    String result = String.valueOf(vbs.compute(value1, value2)) + "%";
                    vbm.setValue(result);
                    vmo.putMetric(query, vbm);
                    // log.info( "VM metric[ " + query + " ] = '" + result + "'" );
                } else {
                    logonce("Couldn't find synthetic rule '" + query + "'");
                }
            }

            // Do NOT move this above the foregoing vmObj filler-code.  The
            // MonitorState can only be computed once all the above is done.
            vmo.setRunState(vmo.getMonitorState());
        }
        if (log.isDebugEnabled())
            log.debug(message);
    }

    private void gatherHostsMetrics(MonitoringState hostPool,
                                    Map<String, BaseQuery> queryPool,
                                    List<BaseQuery> hostQueries) throws ConnectorException {
        String category = HOSTS;
        StringBuilder message = new StringBuilder();
        message.append("gatherHostMetrics(), building " + category + ":\n");

        RhevHost metricsDefaults = new RhevHost("_temp");

        for (BaseQuery accessor : metricsDefaults.getDefaultMetricList())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : metricsDefaults.getDefaultConfigList())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : metricsDefaults.getDefaultSyntheticList())
            queryPool.put(accessor.getQuery(), accessor);

        if (hostQueries != null) {
            for (BaseQuery accessor : hostQueries) // LAST to ensure OVERRIDE capability.
                queryPool.put(accessor.getQuery(), accessor);
        }

        for (String element : rhevMap.get(category).keySet()) {
            ConcurrentHashMap<String, String> server = rhevMap.get(category).get(element);

            String active = zapnull(server.get("active"));
            String ipaddress = zapnull(server.get("address"));
            String clustername = zapnull(server.get("cluster.name"));
            String cpucores = zapnull(server.get("cpu.cores"));
            String cpuname = zapnull(server.get("cpu.name"));
            String cpuspeed = zapnull(server.get("cpu.speed"));
            String maxmemory = zapnull(server.get("max_sched_memory"));
            String memory = zapnull(server.get("memory"));
            String name = zapnull(server.get("name"));
            String port = zapnull(server.get("port"));

            String cpuidle = zapnull(server.get("stat.cpu.current.idle.value"));
            String cpusystem = zapnull(server.get("stat.cpu.current.system.value"));
            String cpuuser = zapnull(server.get("stat.cpu.current.user.value"));
            String cpuload5m = zapnull(server.get("stat.cpu.load.avg.5m.value"));
            String ksmload = zapnull(server.get("stat.ksm.cpu.current.value"));

            String membuffers = zapnull(server.get("stat.memory.buffers.value"));
            String memcached = zapnull(server.get("stat.memory.cached.value"));
            String memfree = zapnull(server.get("stat.memory.free.value"));
            String memshared = zapnull(server.get("stat.memory.shared.value"));
            String memtotal = zapnull(server.get("stat.memory.total.value"));
            String memused = zapnull(server.get("stat.memory.used.value"));

            String swapfree = zapnull(server.get("stat.swap.free.value"));
            String swaptotal = zapnull(server.get("stat.swap.total.value"));
            String swapused = zapnull(server.get("stat.swap.used.value"));
            String swapcached = zapnull(server.get("stat.swap.cached.value"));

            String macaddress = zapnull(server.get("nic[0].mac"));
            String description = zapnull(server.get("description"));

            String bootdate = "";  // there is no "date" data in RHEV for the hosts...
            String lastupdate = "";  // which is rather odd, but there you are.

            RhevHost ho = new RhevHost(name);

            hostPool.hosts().put(name, ho);  // kind of the cart before the horse, but objects are smart

            ho.setBootDate(bootdate, ConnectorConstants.RHEV_DATE_FORMAT);
            ho.setHostGroup(clustername);
            ho.setIpAddress(ipaddress);
            ho.setMacAddress(macaddress);
            ho.setLastUpdate();    // computed, not in data...
            ho.setDescription(description);

            for (String parameter : server.keySet()) {
                BaseQuery vbq = queryPool.get(parameter);
                if (vbq == null)  // if parameter not in (pool of queries to do)
                    continue;

                if (vbq.isGraphed() == false && vbq.isMonitored() == false)
                    continue;      // short-circuit for non-used parameters

                BaseMetric vbm = new BaseMetric(
                        parameter,
                        vbq.getWarning(),
                        vbq.getCritical(),
                        vbq.isGraphed(),
                        vbq.isMonitored(),
                        vbq.getCustomName()
                );

                if (vbq.isTraced())
                    vbm.setTrace();

                String value = server.get(parameter);

                vbm.setValue(value);

                if (parameter.endsWith("value")
                        && (parameter.startsWith("stat.")
                        || parameter.startsWith("nic")
                )) {
                    ho.putMetric(parameter, vbm);
                } else {
                    ho.putConfig(parameter, vbm);
                }

                message.append(String.format("%-80s: %s\n",
                        category + "/" + element + "/" + parameter, value));
            }

            for (String query : queryPool.keySet()) {
                BaseSynthetic vbs;

                // we're ONLY working with the synthetics for HOSTS...
                if (!query.startsWith("syn.host."))
                    continue;

                if ((vbs = metricsDefaults.getSynthetic(query)) != null) {
                    BaseQuery vbq = queryPool.get(query);

                    if (vbq == null)  // indicative of bigger problems...
                        continue;

                    BaseMetric vbm = new BaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored(),
                            vbq.getCustomName()
                    );

                    String value1 = ho.getValueByKey(vbs.getLookup1());
                    String value2 = ho.getValueByKey(vbs.getLookup2());

                    if (value1 == null)
                        logonce("Couldn't find '" + vbs.getLookup1() + "' in HOST metrics");

                    if (value2 == null)
                        logonce("Couldn't find '" + vbs.getLookup2() + "' in HOST metrics");

                    if (vbq.isTraced())
                        vbm.setTrace();

                    String result = String.valueOf(vbs.compute(value1, value2)) + "%";
                    vbm.setValue(result);
                    ho.putMetric(query, vbm);
                    // log.info( "host metric[ " + query + " ] = '" + result + "'" );
                } else {
                    logonce("Couldn't find synthetic rule '" + query + "'");
                }
            }
            ho.setRunState(ho.getMonitorState());
        }
        if (log.isDebugEnabled())
            log.debug(message);
    }

    private Map<String, RhevStorage> gatherStorageMetrics(List<BaseQuery> vmQueries) throws ConnectorException {
        Map<String, RhevStorage> storageMap = new ConcurrentHashMap<String, RhevStorage>();
        String category = STORAGEDOMAINS;
        StringBuilder message = new StringBuilder();
        message.append("gatherStorageMetrics(), building " + category + ":\n");

        RhevStorage metricsDefaults = new RhevStorage("_temp");
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        for (BaseQuery accessor : metricsDefaults.getDefaultMetricList())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : metricsDefaults.getDefaultConfigList())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : metricsDefaults.getDefaultSyntheticList())
            queryPool.put(accessor.getQuery(), accessor);

        if (vmQueries != null) {
            for (BaseQuery accessor : vmQueries) // LAST to OVERRIDE the above defaults
                if (accessor.getSourceType() == SourceType.storage) {
                    queryPool.put(accessor.getQuery(), accessor);
                }
        }


        for (String element : rhevMap.get(category).keySet()) {
            ConcurrentHashMap<String, String> server = rhevMap.get(category).get(element);
            String id = zapnull(server.get("id"));
            String name = zapnull(server.get("name"));
            String available = zapnull(server.get("available"));
            String used = zapnull(server.get("used"));
            String storageFormat = zapnull(server.get("storageFormat"));
            String type = zapnull(server.get("type"));
            String storageType = zapnull(server.get("storage.type"));
            String storagePath = zapnull(server.get("storage.path"));
            boolean offline = (available.equals("") || used.equals(""));

            RhevStorage storage = new RhevStorage(name);
            for (String parameter : server.keySet()) {
                BaseQuery vbq = queryPool.get(parameter);
                if (vbq == null)  // if parameter not in (pool of queries to do)
                    continue;

                if (vbq.isGraphed() == false && vbq.isMonitored() == false)
                    continue;      // short-circuit for non-used parameters

                BaseMetric vbm = new BaseMetric(
                        parameter,
                        vbq.getWarning(),
                        vbq.getCritical(),
                        vbq.isGraphed(),
                        vbq.isMonitored(),
                        vbq.getCustomName()
                );

                if (vbq.isTraced())
                    vbm.setTrace();

                String value = server.get(parameter);
                if (value == null || value.equals(""))
                    value = "0";
                vbm.setValue(value);

                if (storage.isMetric(parameter))
                    storage.putMetric(parameter, vbm);
                else
                    storage.putConfig(parameter, vbm);

                if (log.isDebugEnabled()) {
                    message.append(String.format("%-80s: %s\n",
                            category + "/" + element + "/" + parameter, value));
                }
            }
            computerSynthetics(queryPool, storage);
            String extra = storagePath + " (" + storageFormat + "-" + type + "-" + storageType + ")";
            storage.setDescription(storagePath);
            storage.setRunExtra(extra);
            storage.setRunState(offline ? RhevHost.sUnschedDown : RhevHost.sUp);
            storageMap.put(name, storage);
        }
        if (log.isDebugEnabled())
            log.debug(message);
        return storageMap;
    }

    private <T extends BaseResource> T executeSingleAPI(Class<T> tClass, String entryPoint) throws ConnectorException {
        String xml = null;
        try {
            xml = restClient.executeAPI(entryPoint);
            if (xml != null) {
                JAXBContext context = lookupJAXBContext(tClass);
                Unmarshaller um = context.createUnmarshaller();
                StreamSource ss = new StreamSource(new StringReader(xml));
                return um.unmarshal(ss, tClass).getValue();
            }
        } catch (ConnectorException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectorException("Failed to execute API " + entryPoint, e);
        }
        return null;
    }

    private <T extends BaseResources> T executeAPI(Class<T> tClass, String entryPoint) throws ConnectorException {
        String xml = null;
        try {
            xml = restClient.executeAPI(entryPoint);
            if (xml != null) {
                JAXBContext context = lookupJAXBContext(tClass);
                Unmarshaller um = context.createUnmarshaller();
                StreamSource ss = new StreamSource(new StringReader(xml));
                return um.unmarshal(ss, tClass).getValue();
            }
        } catch (ConnectorException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectorException("Failed to execute API " + entryPoint, e);
        }
        return null;
    }

    private JAXBContext lookupJAXBContext(Class tClass) throws ConnectorException {
        JAXBContext context = contextMap.get(tClass);
        if (context == null) {
            try {
                context = JAXBContext.newInstance(tClass);
                contextMap.put(tClass.getName(), context);
            } catch (Exception e) {
                throw new ConnectorException("Failed to execute API " + tClass, e);
            }
        }
        return context;
    }

    private void computerSynthetics(Map<String, BaseQuery> queryPool, RhevHost vmObj) {
        for (String query : queryPool.keySet()) {
            if (!query.startsWith(ConnectorConstants.SYNTHETIC_PREFIX))
                continue;
            BaseSynthetic vbs;
            BaseQuery vbq = queryPool.get(query);
            BaseMetric vbm = new BaseMetric(
                    query,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    vbq.getCustomName()
            );
            String result = "uncomputed";
            if ((vbs = vmObj.getSynthetic(query)) != null) {
                String value1 = vmObj.getValueByKey(vbs.getLookup1());
                String value2 = vmObj.getValueByKey(vbs.getLookup2());
                result = String.valueOf(vbs.compute(value1, value2)) + "%";
            }
            vbm.setValue(result);

            if (vbq.isTraced())
                vbm.setTrace();

            vmObj.putMetric(query, vbm);
        }
    }

}

