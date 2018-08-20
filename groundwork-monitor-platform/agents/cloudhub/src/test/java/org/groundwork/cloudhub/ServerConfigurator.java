package org.groundwork.cloudhub;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.*;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Centralized configuration for GWOS Virtualization Test Servers
 *
 */
public class ServerConfigurator {

    // VmWare Dev Vermont Server
    public static final String VERMONT_VMWARE_DISPLAYNAME = "Vermont";
    public static final String VERMONT_VMWARE_USERNAME = "groundwork\\vmware-dev";
    public static final String VERMONT_VMWARE_PASSWORD = "M3t30r1t3";
    public static final String VERMONT_VMWARE_SERVER = "vermont2.groundwork.groundworkopensource.com"; ///sdk";
    public static final String VERMONT_VMWARE_URI = "sdk";

    // Redhat Dev RHEV-M-1 Server
    public static final String RHEV_M1_USERNAME = "admin";
    public static final String RHEV_M1_PASSWORD = "#m3t30r1t3";
    public static final String RHEV_M1_SERVER = "eng-rhev-m-1.groundwork.groundworkopensource.com";
    public static final String RHEV_M1_URI = "api";
    public static final String RHEV_M1_REALM = "internal";
    public static final String RHEV_M1_DISPLAY_NAME = "RHEV-M1";
    public static final String RHEV_M1_CERT_PASSWORD = "changeit";
    public static final String RHEV_M1_CERT_STORE = "/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/security/cacerts";
    //public static final String RHEV_M1_CERT_STORE = "/usr/local/groundwork/java/jre/lib/security/certificates/cacerts";

    public enum OpenStackTestType {
        agno,
        mirantis,
        juno,
        icehouse,
        kilo,
        liberty
    };

    // OpenStack Kilo Server
    public static final String KILO_OPENSTACK_DISPLAYNAME = "Kilo OpenStack";
    public static final String KILO_OPENSTACK_USERNAME = "admin";
    public static final String KILO_OPENSTACK_PASSWORD = "admin";
    public static final String KILO_OPENSTACK_SERVER = "172.16.0.2";
    public static final String KILO_OPENSTACK_TENANT_ID = "1497021bfcff43cca34a66500d7c52e7";
    public static final String KILO_OPENSTACK_TENANT_NAME = "admin";

    // OpenStack Liberty Server
    public static final String LIBERTY_OPENSTACK_DISPLAYNAME = "Liberty OpenStack";
    public static final String LIBERTY_OPENSTACK_USERNAME = "admin";
    public static final String LIBERTY_OPENSTACK_PASSWORD = "admin";
    public static final String LIBERTY_OPENSTACK_SERVER = "172.16.0.3" ;// "172.16.1.2";
    public static final String LIBERTY_OPENSTACK_TENANT_ID = "8d535e793df54af291d06c15343acdd2";
    public static final String LIBERTY_OPENSTACK_TENANT_NAME = "admin";

    // OpenStack Dev Agno Server
    public static final String AGNO_OPENSTACK_DISPLAYNAME = "Agno OpenStack";
    public static final String AGNO_OPENSTACK_USERNAME = "demo";
    public static final String AGNO_OPENSTACK_PASSWORD = "55d794a346cf413a";
    public static final String AGNO_OPENSTACK_SERVER = "agno.groundwork.groundworkopensource.com";
    public static final String AGNO_OPENSTACK_TENANT_ID = "6fd89a41705441a5b718aebe07a763d0";
    public static final String AGNO_OPENSTACK_TENANT_NAME = "demo";

    // OpenStack Dev Mirantis Server
    public static final String MIRANTIS_OPENSTACK_DISPLAYNAME = "Mirantis OpenStack";
    public static final String MIRANTIS_OPENSTACK_USERNAME = "admin";
    public static final String MIRANTIS_OPENSTACK_PASSWORD = "admin";
    public static final String MIRANTIS_OPENSTACK_SERVER = "172.28.116.22";
    public static final String MIRANTIS_OPENSTACK_SERVER_NAME = "node-10.groundwork.groundworkopensource.com";
    public static final String MIRANTIS_OPENSTACK_TENANT_ID = "b281305fec264397b7a49efb48278c24";
    public static final String MIRANTIS_OPENSTACK_TENANT_NAME = "admin";

    // OpenStack Amazon Juno Server
    public static final String JUNO_OPENSTACK_DISPLAYNAME = "Amazon JUNO OpenStack";
//    public static final String JUNO_OPENSTACK_USERNAME = "admin";
//    public static final String JUNO_OPENSTACK_PASSWORD = "47a34d24c5ff4f27";
    public static final String JUNO_OPENSTACK_USERNAME = "demo";
    public static final String JUNO_OPENSTACK_PASSWORD = "d7808e87038c4365";
    public static final String JUNO_OPENSTACK_SERVER = "10.0.10.9";
    public static final String JUNO_OPENSTACK_SERVER_NAME = "ip-10-0-10-9.localdomain";
    public static final String JUNO_OPENSTACK_TENANT_ADMIN_ID = "743641cfa0714cf3adb62ba7bcc7fd7b";
    public static final String JUNO_OPENSTACK_TENANT_ID = "75656e8e331f4f4cb38a048e924812a2";
    public static final String JUNO_OPENSTACK_TENANT_ADMIN_NAME = "admin";
    public static final String JUNO_OPENSTACK_TENANT_NAME = "demo";

    // OpenStack IceHouse Demo
    public static final String ICEHOUSE_OPENSTACK_DISPLAYNAME = "Amazon IceHouse OpenStack";
    public static final String ICEHOUSE_OPENSTACK_ADMIN_USERNAME = "admin";
    public static final String ICEHOUSE_OPENSTACK_ADMIN_PASSWORD = "07376ff2bb3a40b0";
    public static final String ICEHOUSE_OPENSTACK_USERNAME = "demo";
    public static final String ICEHOUSE_OPENSTACK_PASSWORD = "6016f43daa664e78";
    public static final String ICEHOUSE_OPENSTACK_SERVER = "10.0.11.24"; // "54.183.14.78";     // 10.0.11.124 54.183.12.89 54.183.14.78
    public static final String ICEHOUSE_OPENSTACK_SERVER_NAME = "ip-10-0-11-124.localdomain";
    public static final String ICEHOUSE_OPENSTACK_TENANT_ADMIN_ID = "2e09517772fc41cc85bdf6971f41d63c";
    public static final String ICEHOUSE_OPENSTACK_TENANT_ID = "ada290162d094c65a705e05e73c9d087";
    public static final String ICEHOUSE_OPENSTACK_TENANT_ADMIN_NAME = "admin";
    public static final String ICEHOUSE_OPENSTACK_TENANT_NAME = "demo";

    // OpenDaylight Dev Server
    public static final String OPEN_DAYLIGHT_USERNAME = "admin";
    public static final String OPEN_DAYLIGHT_PASSWORD = "admin";
    public static final String OPEN_DAYLIGHT_SERVER = "172.28.113.201:8080";// http://172.28.113.201:8080/controller/nb/v2/statistics";
    public static final String OPEN_DAYLIGHT_SERVER_WITHOUT_PORT = "172.28.113.201";
    public static final String OPEN_DAYLIGHT_DISPLAY_NAME = "GW OpenDaylight Test Server";
    public static final String OPEN_DAYLIGHT_CONTAINER = "default";

    // Docker Dev Server
    //public static final String DOCKER_SERVER = "dock-01-integration.groundwork.groundworkopensource.com:8081";
    public static final String DOCKER_SERVER = "dstmachine:9292"; //"192.168.99.100:32768";
    public static final String DOCKER_SERVER_WITHOUT_PORT = "dstmachine"; // "dock-01-integration.groundwork.groundworkopensource.com";
    public static final String DOCKER_DISPLAY_NAME = "GW Docker Test Server";

    // Amazon AWS Server
    public static final String AMAZON_DISPLAYNAME = "Amazon Web Services (AWS)";
    public static final String AMAZON_ENDPOINT = "us-west-2.amazonaws.com";
    public static final String AMAZON_ACCESS_KEY = "AKIAINUPQPZ3G76P57BA";
    public static final String AMAZON_SECRET_KEY = "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";

    public static final String AMAZON_ACCESS_KEY_2 = "AKIAI3H6IX6YSGL3HCMA";
    public static final String AMAZON_SECRET_KEY_2 = "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";

    // NetApp Server
    public static final String NETAPP_DISPLAY_NAME = "NetApp Storage";
    public static final String NETAPP_SERVER = "gwos-netapp-colo";
    public static final String NETAPP_ADMIN_USER = "admin";
    public static final String NETAPP_ADMIN_PASSWORD = "m3t30r1t3";

    // Cloudera Server
    public static final String CLOUDERA_DEV_DISPLAY_NAME = "Cloudera Dev Instance";
    public static final String CLOUDERA_DEV_SERVER = "dev-cloudera";
    public static final String CLOUDERA_DEV_ADMIN_USER = "admin";
    public static final String CLOUDERA_DEV_ADMIN_PASSWORD = "d3vcloudera!";

    // Cloudera Lab Server
    public static final String CLOUDERA_DISPLAY_NAME = "Cloudera Lab";
    public static final String CLOUDERA_SERVER = "172.28.111.205";
    public static final String CLOUDERA_ADMIN_USER = "admin";
    public static final String CLOUDERA_ADMIN_PASSWORD = "admin";

    // Azure Server
    public static final String AZURE_DISPLAY_NAME = "Azure Dev Instance";
    public static final String AZURE_CREDENTIALS_FILE = "/usr/local/groundwork/config/cloudhub/azure/cloudhub.azureauth";

    // NeDI
    public static final String NEDI_DISPLAY_NAME = "NeDi Connector";
    public static final String NEDI_USERNAME = "nedi";
    public static final String NEDI_PASSWORD = "dbpa55";
    public static final String NEDI_SERVER = "localhost";
    public static final String NEDI_SERVER_PORT = "5432";
    public static final String NEDI_DATABASE = "nedi";
    public static final String NEDI_POLICY_HOST = "localhost";


    // Icinga2 Server
    public static final String ICINGA2_DISPLAY_NAME = "Icinga2 Test Monitor Service";
    public static final String ICINGA2_SERVER = "demo70.groundwork.groundworkopensource.com";
    public static final String ICINGA2_PORT = "5665";
    public static final String ICINGA2_USERNAME = "root";
    public static final String ICINGA2_PASSWORD = "fc764746b29dfa82";

    // Groundwork Server
    public static final String GWOS_REST_API_PROPERTY = "GWOS_REST_API";
    public static final String GWOS_REST_USER_PROPERTY = "GWOS_REST_USER";
    public static final String GWOS_REST_PW_PROPERTY = "GWOS_REST_PW";
    public static final String GWOS_DEFAULT_FULL_ENDPOINT = "http://localhost:8080/foundation-webapp/api";
    public static final String GWOS_70_USER = "wsuser";
    public static final String GWOS_70_PW = "wsuser";
    public static final String GWOS_71_USER = "RESTAPIACCESS";
    public static final String GWOS_71_PW = "7UZZVvnLbuRNk12Yk5H33zeYdWQpnA7j9shir7QfJgwh";
    // Groundwork 7.0.2 Server
    public static final String GWOS_702_SERVER = "eng-rh6-dev1";

    public static GWOSConfiguration setupLocalGroundworkServer(GWOSConfiguration gwos) {
        String restEndPoint = System.getProperty(GWOS_REST_API_PROPERTY, GWOS_DEFAULT_FULL_ENDPOINT);
        String user = System.getProperty(GWOS_REST_USER_PROPERTY, GWOS_71_USER);
        String pw = System.getProperty(GWOS_REST_PW_PROPERTY, GWOS_71_PW);
        try {
            URL url = new URL(restEndPoint);
            gwos.setWsUsername(user);
            gwos.setWsPassword(pw);
            if (url.getPort() != -1) {
                gwos.setGwosServer(url.getHost() + ":" + url.getPort());
                gwos.setGwosPort(Integer.toString(url.getPort()));
            } else {
                gwos.setGwosServer(url.getHost());
            }
            String o = url.getProtocol();
            gwos.setGwosSSLEnabled(url.getProtocol().equals("https"));
            gwos.setRsEndPoint(url.getPath());
            gwos.setGwosVersion("7.1.0");
            gwos.setMergeHosts(GWOSConfiguration.DEFAULT_MERGE_HOSTS);
            return gwos;
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Bad Rest End Point Provided in Test" + restEndPoint, e);
        }
    }

    public static GWOSConfiguration setupGWOS702Configuration(String gwosServer, GWOSConfiguration gwos) {
        gwos.setGwosServer(gwosServer);
        gwos.setWsUsername(GWOS_70_USER);
        gwos.setWsPassword(GWOS_70_PW);
        gwos.setGwosVersion("7.0.2");
        gwos.setMergeHosts(GWOSConfiguration.DEFAULT_GWOS_70_MERGE_HOSTS);
        return gwos;
    }

    public static OpenStackConnection setupOpenStackKiloConnection(OpenStackConnection os) {
        os.setUsername(KILO_OPENSTACK_USERNAME);
        os.setPassword(KILO_OPENSTACK_PASSWORD);
        os.setServer(KILO_OPENSTACK_SERVER);
        os.setSslEnabled(false);
        os.setTenantId(KILO_OPENSTACK_TENANT_ID);
        os.setTenantName(KILO_OPENSTACK_TENANT_NAME);
        return os;
    }

    public static OpenStackConnection setupOpenStackAgnosConnection(OpenStackConnection os) {
        os.setUsername(AGNO_OPENSTACK_USERNAME);
        os.setPassword(AGNO_OPENSTACK_PASSWORD);
        os.setServer(AGNO_OPENSTACK_SERVER);
        os.setSslEnabled(false);
        os.setTenantId(AGNO_OPENSTACK_TENANT_ID);
        os.setTenantName(AGNO_OPENSTACK_TENANT_NAME);
        return os;
    }

    public static OpenStackConnection setupOpenStackMirantisConnection(OpenStackConnection os) {
        os.setUsername(MIRANTIS_OPENSTACK_USERNAME);
        os.setPassword(MIRANTIS_OPENSTACK_PASSWORD);
        os.setServer(MIRANTIS_OPENSTACK_SERVER);
        os.setSslEnabled(false);
        os.setTenantId(MIRANTIS_OPENSTACK_TENANT_ID);
        os.setTenantName(MIRANTIS_OPENSTACK_TENANT_NAME);
        return os;
    }

    public static OpenStackConnection setupOpenStackJunoConnection(OpenStackConnection os) {
        os.setUsername(JUNO_OPENSTACK_USERNAME);
        os.setPassword(JUNO_OPENSTACK_PASSWORD);
        os.setServer(JUNO_OPENSTACK_SERVER);
        os.setSslEnabled(false);
        os.setTenantId(JUNO_OPENSTACK_TENANT_ID);
        os.setTenantName(JUNO_OPENSTACK_TENANT_NAME);
        return os;
    }

    public static OpenStackConnection setupOpenStackIceHouseConnection(OpenStackConnection os) {
        os.setUsername(ICEHOUSE_OPENSTACK_USERNAME);
        os.setPassword(ICEHOUSE_OPENSTACK_PASSWORD);
        os.setServer(ICEHOUSE_OPENSTACK_SERVER);
        os.setSslEnabled(false);
        os.setTenantId(ICEHOUSE_OPENSTACK_TENANT_ID);
        os.setTenantName(ICEHOUSE_OPENSTACK_TENANT_NAME);
        return os;
    }

    public static OpenStackConnection setupOpenStackLibertyConnection(OpenStackConnection os) {
        os.setUsername(LIBERTY_OPENSTACK_USERNAME);
        os.setPassword(LIBERTY_OPENSTACK_PASSWORD);
        os.setServer(LIBERTY_OPENSTACK_SERVER);
        os.setSslEnabled(false);
        os.setTenantId(LIBERTY_OPENSTACK_TENANT_ID);
        os.setTenantName(LIBERTY_OPENSTACK_TENANT_NAME);
        return os;
    }

    public static VmwareConnection setupVmwareVermontConnection(VmwareConnection vmware) {
        vmware.setUsername(VERMONT_VMWARE_USERNAME);
        vmware.setPassword(VERMONT_VMWARE_PASSWORD);
        vmware.setServer(VERMONT_VMWARE_SERVER);
        vmware.setSslEnabled(true);
        vmware.setUri(VERMONT_VMWARE_URI);
        return vmware;
    }

    public static VmwareConfiguration createVmwareVermontServer(ConfigurationService configurationService) {
        VmwareConfiguration vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
        setupLocalGroundworkServer(vmware.getGwos());
        vmware.getCommon().setDisplayName(VERMONT_VMWARE_DISPLAYNAME);
        setupVmwareVermontConnection(vmware.getConnection());
        configurationService.saveConfiguration(vmware);
        return vmware;
    }

    public static OpenStackConfiguration createOpenStackAgnoServer(ConfigurationService configurationService)
            throws MalformedURLException {
        OpenStackConfiguration os = configurationService.createConfiguration(VirtualSystem.OPENSTACK);
        setupLocalGroundworkServer(os.getGwos());
        os.getCommon().setDisplayName(AGNO_OPENSTACK_DISPLAYNAME);
        setupOpenStackAgnosConnection(os.getConnection());
        configurationService.saveConfiguration(os);
        return os;
    }

    public static OpenStackConfiguration createOpenStackMirantisServer(ConfigurationService configurationService)
            throws MalformedURLException {
        OpenStackConfiguration os = configurationService.createConfiguration(VirtualSystem.OPENSTACK);
        setupLocalGroundworkServer(os.getGwos());
        os.getCommon().setDisplayName(MIRANTIS_OPENSTACK_DISPLAYNAME);
        setupOpenStackAgnosConnection(os.getConnection());
        configurationService.saveConfiguration(os);
        return os;
    }

    public static OpenStackConfiguration createOpenStackJunoServer(ConfigurationService configurationService)
            throws MalformedURLException {
        OpenStackConfiguration os = configurationService.createConfiguration(VirtualSystem.OPENSTACK);
        setupLocalGroundworkServer(os.getGwos());
        os.getCommon().setDisplayName(JUNO_OPENSTACK_DISPLAYNAME);
        setupOpenStackAgnosConnection(os.getConnection());
        configurationService.saveConfiguration(os);
        return os;
    }

    public static RedhatConfiguration createRedhatServer(ConfigurationService configurationService) {
        RedhatConfiguration rhev = configurationService.createConfiguration(VirtualSystem.REDHAT);
        setupLocalGroundworkServer(rhev.getGwos());
        rhev.getCommon().setDisplayName(RHEV_M1_DISPLAY_NAME);
        setupRedhatConnection(rhev.getConnection());
        configurationService.saveConfiguration(rhev);
        return rhev;
    }

    public static RedhatConnection setupRedhatConnection(RedhatConnection rhev) {
        rhev.setUsername(RHEV_M1_USERNAME);
        rhev.setPassword(RHEV_M1_PASSWORD);
        rhev.setServer(RHEV_M1_SERVER);
        rhev.setUri(RHEV_M1_URI);
        rhev.setRealm(RHEV_M1_REALM);
        rhev.setCertificatePassword(RHEV_M1_CERT_PASSWORD);
        rhev.setCertificateStore(RHEV_M1_CERT_STORE);
        rhev.setProtocol("https");
        rhev.setPort("443");
        return rhev;
    }

    public static void enableAllViews(CommonConfiguration common) {
        common.setHypervisorView(true);
        common.setNetworkView(true);
        common.setStorageView(true);
        common.setResourcePoolView(true);
    }

    public static void disableAllViews(CommonConfiguration common) {
        common.setHypervisorView(false);
        common.setNetworkView(false);
        common.setStorageView(false);
        common.setResourcePoolView(false);
    }

    public static OpenDaylightConnection setupOpenDaylightConnection(OpenDaylightConnection os) {
        os.setUsername(OPEN_DAYLIGHT_USERNAME);
        os.setPassword(OPEN_DAYLIGHT_PASSWORD);
        os.setServer(OPEN_DAYLIGHT_SERVER);
        os.setContainer(OPEN_DAYLIGHT_CONTAINER);
        os.setSslEnabled(false);
        return os;
    }

    public static OpenDaylightConfiguration createOpenDaylightServer(ConfigurationService configurationService) {
        OpenDaylightConfiguration daylight = configurationService.createConfiguration(VirtualSystem.OPENDAYLIGHT);
        setupLocalGroundworkServer(daylight.getGwos());
        daylight.getCommon().setDisplayName(OPEN_DAYLIGHT_DISPLAY_NAME);
        setupOpenDaylightConnection(daylight.getConnection());
        configurationService.saveConfiguration(daylight);
        return daylight;
    }

    public static DockerConfiguration createDockerServer(ConfigurationService configurationService) {
        DockerConfiguration docker = configurationService.createConfiguration(VirtualSystem.DOCKER);
        setupLocalGroundworkServer(docker.getGwos());
        docker.getCommon().setDisplayName(DOCKER_DISPLAY_NAME);
        setupDockerConnection(docker.getConnection());
        configurationService.saveConfiguration(docker);
        return docker;
    }

    public static DockerConnection setupDockerConnection(DockerConnection docker) {
        docker.setServer(DOCKER_SERVER);
        docker.setPrefix("dev1-");
        return docker;
    }

    public static AmazonConfiguration createAmazonServer(ConfigurationService configurationService) {
        AmazonConfiguration amazon = configurationService.createConfiguration(VirtualSystem.AMAZON);
        setupLocalGroundworkServer(amazon.getGwos());
        amazon.getCommon().setDisplayName(AMAZON_DISPLAYNAME);
        setupAmazonConnection(amazon.getConnection());
        configurationService.saveConfiguration(amazon);
        return amazon;
    }

    public static AmazonConnection setupAmazonConnection(AmazonConnection amazon) {
        amazon.setUsername(AMAZON_ACCESS_KEY);
        amazon.setPassword(AMAZON_SECRET_KEY);
        amazon.setServer(AMAZON_ENDPOINT);
        amazon.setSslEnabled(true);
        return amazon;
    }

    public static AmazonConnection setupAmazonConnection2(AmazonConnection amazon) {
        amazon.setUsername(AMAZON_ACCESS_KEY_2);
        amazon.setPassword(AMAZON_SECRET_KEY_2);
        amazon.setServer(AMAZON_ENDPOINT);
        amazon.setSslEnabled(true);
        return amazon;
    }

    public static LoadTestConnection setupLoadTestConnection(LoadTestConnection loadTest) {
        loadTest.setHosts(10);
        loadTest.setHostGroups(2);
        loadTest.setHostsDownPercent(1.0F);
        loadTest.setServicesCriticalPercent(1.0F);
        return loadTest;
    }

    public static NetAppConfiguration createNetAppServer(ConfigurationService configurationService) {
        NetAppConfiguration netapp = configurationService.createConfiguration(VirtualSystem.NETAPP);
        setupLocalGroundworkServer(netapp.getGwos());
        netapp.getCommon().setDisplayName(NETAPP_DISPLAY_NAME);
        setupNetAppConnection(netapp.getConnection());
        configurationService.saveConfiguration(netapp);
        return netapp;
    }

    public static NetAppConnection setupNetAppConnection(NetAppConnection netapp) {
        netapp.setUsername(NETAPP_ADMIN_USER);
        netapp.setPassword(NETAPP_ADMIN_PASSWORD);
        netapp.setServer(NETAPP_SERVER);
        netapp.setSslEnabled(false);
        return netapp;
    }

    public static Icinga2Configuration createIcinga2Server(ConfigurationService configurationService) {
        Icinga2Configuration icinga2 = configurationService.createConfiguration(VirtualSystem.ICINGA2);
        setupLocalGroundworkServer(icinga2.getGwos());
        icinga2.getCommon().setDisplayName(ICINGA2_DISPLAY_NAME);
        setupIcinga2Connection(icinga2.getConnection());
        configurationService.saveConfiguration(icinga2);
        return icinga2;
    }

    public static Icinga2Connection setupIcinga2Connection(Icinga2Connection icinga2) {
        icinga2.setServer(ICINGA2_SERVER);
        icinga2.setPort(ICINGA2_PORT);
        icinga2.setUsername(ICINGA2_USERNAME);
        icinga2.setPassword(ICINGA2_PASSWORD);
        icinga2.setTrustAllSSL(true);
        return icinga2;
    }

    public static ClouderaConfiguration createClouderaServer(ConfigurationService configurationService) {
       ClouderaConfiguration cloudera = configurationService.createConfiguration(VirtualSystem.CLOUDERA);
       setupLocalGroundworkServer(cloudera.getGwos());
       cloudera.getCommon().setDisplayName(CLOUDERA_DISPLAY_NAME);
       setupClouderaConnection(cloudera.getConnection());
       configurationService.saveConfiguration(cloudera);
       return cloudera;
    }

    public static ClouderaConnection setupClouderaConnection(ClouderaConnection cloudera) {
        cloudera.setUsername(CLOUDERA_ADMIN_USER);
        cloudera.setPassword(CLOUDERA_ADMIN_PASSWORD);
        cloudera.setServer(CLOUDERA_SERVER);
        cloudera.setSslEnabled(false);
        return cloudera;
    }

    public static AzureConfiguration createAzureServer(ConfigurationService configurationService) {
        AzureConfiguration azure = configurationService.createConfiguration(VirtualSystem.AZURE);
        setupLocalGroundworkServer(azure.getGwos());
        azure.getCommon().setDisplayName(AZURE_DISPLAY_NAME);
        setupAzureConnection(azure.getConnection());
        configurationService.saveConfiguration(azure);
        return azure;
    }

    public static AzureConnection setupAzureConnection(AzureConnection azureConnection) {
        azureConnection.setCredentialsFile(AZURE_CREDENTIALS_FILE);
        azureConnection.setTimeoutMs(6000L);
        return azureConnection;
    }

    public static NediConfiguration createNediServer(ConfigurationService configurationService) {
        NediConfiguration nedi = configurationService.createConfiguration(VirtualSystem.NEDI);
        setupLocalGroundworkServer(nedi.getGwos());
        nedi.getCommon().setDisplayName(NEDI_DISPLAY_NAME);
        setupNediConnection(nedi.getConnection());
        configurationService.saveConfiguration(nedi);
        return nedi;
    }

    public static NediConnection setupNediConnection(NediConnection nedi) {
        nedi.setUsername(NEDI_USERNAME);
        nedi.setPassword(NEDI_PASSWORD);
        nedi.setServer(NEDI_SERVER);
        nedi.setPort(NEDI_SERVER_PORT);
        nedi.setDatabase(NEDI_DATABASE);
        nedi.setPolicyHost(NEDI_POLICY_HOST);
        nedi.setSslEnabled(false);
        return nedi;
    }

}

