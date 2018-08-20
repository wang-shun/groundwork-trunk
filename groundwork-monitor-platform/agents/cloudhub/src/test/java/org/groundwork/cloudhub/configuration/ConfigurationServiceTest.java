package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.SharedSecretProtector;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class ConfigurationServiceTest extends AbstractAgentTest {

    @Test
    public void testOpenStackCreate() {
        OpenStackConfiguration os = new OpenStackConfiguration();
        ServerConfigurator.setupOpenStackAgnosConnection(os.getConnection());
        ServerConfigurator.setupLocalGroundworkServer(os.getGwos());
        configurationService.saveConfiguration(os);

        ConnectionConfiguration conn = configurationService.readConfiguration(os.getCommon().getPathToConfigurationFile() +
                os.getCommon().getConfigurationFile());
        assert conn.getGwos().getGwosServer().equals("localhost");
        assert ((OpenStackConnection)conn.getConnection()).getTenantName().equals("demo");
        configurationService.deleteConfiguration(conn);
        assert configurationService.doesConfigurationExist(os.getCommon().getPathToConfigurationFile() +
                        os.getCommon().getConfigurationFile()) == false;
    }

    @Test
    public void testConfigurationCrud() {
        ConnectionConfiguration vmware = new VmwareConfiguration();
        vmware.getCommon().setDisplayName("My Configuration-Vmware");
        configurationService.saveConfiguration(vmware);
        RedhatConfiguration redhat = new RedhatConfiguration();
        redhat.getCommon().setDisplayName("My Configuration-Redhat");
        redhat.getConnection().setCertificateStore("/path/to/store");
        redhat.getConnection().setPort("8080");
        configurationService.saveConfiguration(redhat);

        RedhatConfiguration rhel = (RedhatConfiguration)configurationService.readConfiguration(redhat.getCommon().getPathToConfigurationFile() +
                                            redhat.getCommon().getConfigurationFile());
        Assert.assertNotNull(rhel);
        assertEquals("/path/to/store", rhel.getConnection().getCertificateStore());

        configurationService.deleteConfiguration(rhel);
        configurationService.deleteConfiguration(vmware);

        assert configurationService.doesConfigurationExist(vmware.getCommon().getPathToConfigurationFile() +
                vmware.getCommon().getConfigurationFile()) == false;

        assert configurationService.doesConfigurationExist(rhel.getCommon().getPathToConfigurationFile() +
                rhel.getCommon().getConfigurationFile()) == false;
    }

    @Test
    public void testPassword() {
        VmwareConfiguration vmware = new VmwareConfiguration();
        vmware.getCommon().setDisplayName("Password test");
        vmware.getConnection().setUsername("admin");
        vmware.getConnection().setPassword("M3t30r1t3");
        configurationService.saveConfiguration(vmware);

        VmwareConfiguration test = (VmwareConfiguration)configurationService.readConfiguration(vmware.getCommon().getPathToConfigurationFile() +
                vmware.getCommon().getConfigurationFile());
        Assert.assertNotNull(vmware);

        assertEquals("admin", test.getConnection().getUsername());
        assertEquals("M3t30r1t3", test.getConnection().getPassword());
        configurationService.deleteConfiguration(vmware);

        assert configurationService.doesConfigurationExist(test.getCommon().getPathToConfigurationFile() +
                test.getCommon().getConfigurationFile()) == false;
    }


    @Test
    public void testVmwareConversion() throws Exception {
        File source = new File("./src/test/testdata/vema-gwos-config.xml");
        File dest = new File("/usr/local/groundwork/config/vema-gwos-config.xml");
        copyFile(source, dest);
        assert dest.exists();
        VmwareConfiguration converted = configurationService.convertLegacyConfiguration(VirtualSystem.VMWARE);
        assertNotNull(converted);
        assertEquals("localhost", converted.getGwos().getGwosServer());
        assertEquals("4913", converted.getGwos().getGwosPort());
        assertEquals("/foundation-webapp/services", converted.getGwos().getWsEndPoint());
        assertEquals("RESTAPIACCESS", converted.getGwos().getWsUsername());
        VmwareConnection connection = converted.getConnection();
        assertEquals("sdk", connection.getUri());
        assertEquals("vermont.groundwork.groundworkopensource.com", connection.getServer());
        assertEquals("https://vermont.groundwork.groundworkopensource.com/sdk", connection.getUrl());
        assertEquals("vmware-dev", connection.getUsername());
        assertEquals("M3t30r1t3", connection.getPassword());
        configurationService.deleteConfiguration(converted);
        dest.delete();
        assert dest.exists() == false;
    }

    @Test
    public void testRedhatConversion() throws Exception {
        File source = new File("./src/test/testdata/rhev-gwos-config.xml");
        File dest = new File("/usr/local/groundwork/config/rhev-gwos-config.xml");
        copyFile(source, dest);
        assert dest.exists();
        RedhatConfiguration converted = configurationService.convertLegacyConfiguration(VirtualSystem.REDHAT);
        assertNotNull(converted);
        assertEquals("localhost", converted.getGwos().getGwosServer());
        assertEquals("4913", converted.getGwos().getGwosPort());
        assertEquals("/foundation-webapp/services", converted.getGwos().getWsEndPoint());
        assertEquals("RESTAPIACCESS", converted.getGwos().getWsUsername());
        RedhatConnection connection = converted.getConnection();
        assertEquals("api", connection.getUri());
        assertEquals("eng-rhev-m-1", connection.getServer());
        assertEquals("https://eng-rhev-m-1/api", connection.getUrl());
        assertEquals("admin", connection.getUsername());
        assertEquals("#m3t30r1t3", connection.getPassword());
        assertEquals("internal", connection.getRealm());
        assertEquals("https", connection.getProtocol());
        assertEquals("/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/security/cacerts", connection.getCertificateStore());
        assertEquals("changeit", connection.getCertificatePassword());
        configurationService.deleteConfiguration(converted);
        dest.delete();
        assert dest.exists() == false;
    }

    @Test
    public void testNotFoundConversion() throws Exception {
        VmwareConfiguration vmwareConfiguration =  configurationService.convertLegacyConfiguration(VirtualSystem.VMWARE);
        assertNull(vmwareConfiguration);
        RedhatConfiguration redhatConfiguration =  configurationService.convertLegacyConfiguration(VirtualSystem.REDHAT);
        assertNull(redhatConfiguration);
    }

    @Test
    public void testEncrypt() throws Exception {
        String encrypted = SharedSecretProtector.encrypt("#m3t30r1t3");
        assertNotNull(encrypted);
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    @Test
    public void testAutoUpgradeOfOpenStackSampleRate() {
        OpenStackConfiguration os = (OpenStackConfiguration)
                configurationService.readConfiguration("./src/test/testdata/ceilometer/cloudhub-openstack-40.xml");
        assert(os.getConnection().getCeilometerSampleRateMinutes().equals("10"));
        os.getConnection().setCeilometerSampleRateMinutes("11");
        assert(os.getConnection().getCeilometerSampleRateMinutes().equals("11"));
        os.getCommon().setPathToConfigurationFile("./target/");
        configurationService.saveConfiguration(os);
        os = (OpenStackConfiguration)
                configurationService.readConfiguration("./target/cloudhub-openstack-40.xml");
        assert(os.getConnection().getCeilometerSampleRateMinutes().equals("11"));
    }

    @Test
    public void testGwosConfig() {
        VmwareConfiguration config = configurationService.createConfiguration(VirtualSystem.VMWARE);
        assert config.getGwos().getMonitor() == true;
    }
}
