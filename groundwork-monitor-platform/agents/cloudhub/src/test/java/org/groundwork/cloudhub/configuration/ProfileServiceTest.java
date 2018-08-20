package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ContainerProfileWrapper;
import org.groundwork.cloudhub.profile.ProfileConversion;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.cloudhub.profile.ProfileServiceImpl;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class ProfileServiceTest extends AbstractAgentTest {

    public final static String TEST_AGENT = "testAgent";

    public static Path copyTestProfile(VirtualSystem vs, String agent) throws IOException {
        String virtualName = ProfileConversion.convertVirtualSystemToPropertyType(vs).name();
        return Files.copy(
                new java.io.File("/usr/local/groundwork/core/vema/profiles/" + virtualName + "_monitoring_profile.xml").toPath(),
                new java.io.File("/usr/local/groundwork/config/cloudhub/profiles/" + virtualName + "-" + agent + ".xml").toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static Path copyUnitTestProfile(VirtualSystem vs, String agent) throws IOException {
        String virtualName = ProfileConversion.convertVirtualSystemToPropertyType(vs).name();
        return Files.copy(
                new java.io.File("./src/test/testdata/cloudera/cloudera_unittest_profile.xml").toPath(),
                new java.io.File("/usr/local/groundwork/config/cloudhub/profiles/" + virtualName + "-" + agent + ".xml").toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testMigrate() throws Exception {
        ProfileService service = new ProfileServiceImpl() ;
        int result = service.migrateProfiles(VirtualSystem.DOCKER, configurationService.listConfigurations(VirtualSystem.DOCKER));
        assert result == 0;
    }

    @Test
    public void testRead() throws Exception {
        ProfileService service = new ProfileServiceImpl() ;
        copyTestProfile(VirtualSystem.VMWARE, TEST_AGENT);
        CloudHubProfile vmware = service.readCloudProfile(VirtualSystem.VMWARE, TEST_AGENT);
        assert(vmware.getHypervisor().getMetrics().size() > 0);
        service.removeProfile(VirtualSystem.VMWARE, TEST_AGENT);
        assert(!service.doesProfileExist(VirtualSystem.VMWARE, TEST_AGENT));
    }

    @Test
    public void testProfileCrud() {
        ProfileService service = new ProfileServiceImpl() ;
        CloudHubProfile vmware = service.createCloudProfile(VirtualSystem.VMWARE, TEST_AGENT);
        Metric m1 = new Metric("a", "adesc", false, false, 987, 123, Metric.SOURCE_TYPE_NETWORK, Metric.COMPUTE_TYPE_REGEX, "aa", "GW:max(a1,a2)", "a=%d", "CLUSTER");
        Metric m2 = new Metric("d", "ddesc", true, true, 654, 456, Metric.SOURCE_TYPE_STORAGE, Metric.COMPUTE_TYPE_REGEX, null, "GW:max(d1,d2)", "d=%d", "HOST");
        vmware.getHypervisor().addMetric(m1);
        vmware.getHypervisor().addMetric(m2);
        Metric m3 = new Metric("g", "gdesc", false, true, 321, 789, null, null, "gg", "GW:max(g1,g2)", "g=%d", "HDFS");
        Metric m4 = new Metric("k", "kdesc", true, false, 123, 012, Metric.SOURCE_TYPE_STORAGE, Metric.COMPUTE_TYPE_REGEX, "gg", "GW:max(k1,k2)", "k=%d", "HDFS");
        vmware.getVm().addMetric(m3);
        vmware.getVm().addMetric(m4);
        service.saveProfile(vmware);
        CloudHubProfile redhat = service.createCloudProfile(VirtualSystem.REDHAT, TEST_AGENT);
        redhat.getHypervisor().addMetric(m3);
        redhat.getHypervisor().addMetric(m4);
        redhat.getVm().addMetric(m1);
        redhat.getVm().addMetric(m2);
        service.saveProfile(redhat);

        assert(service.doesProfileExist(VirtualSystem.VMWARE, TEST_AGENT));
        assert(service.doesProfileExist(VirtualSystem.REDHAT, TEST_AGENT));

        vmware = service.readCloudProfile(VirtualSystem.VMWARE, TEST_AGENT);
        Assert.assertNotNull(vmware);
        assert vmware.getAgent().equals(TEST_AGENT);
        Assert.assertEquals(2, vmware.getHypervisor().getMetrics().size());
        Assert.assertEquals("a", vmware.getHypervisor().getMetrics().get(0).getName());
        Assert.assertEquals("aa", vmware.getHypervisor().getMetrics().get(0).getCustomName());
        Assert.assertEquals("aa", vmware.getHypervisor().getMetrics().get(0).getServiceName());
        Assert.assertEquals("ddesc", vmware.getHypervisor().getMetrics().get(1).getDescription());
        Assert.assertEquals(123.0, vmware.getHypervisor().getMetrics().get(0).getCriticalThreshold(), 0.0001);
        Assert.assertEquals(true, vmware.getHypervisor().getMetrics().get(1).isMonitored());
        // @since CloudHub 2.3 expressions
        Assert.assertEquals("GW:max(a1,a2)", vmware.getHypervisor().getMetrics().get(0).getExpression());
        Assert.assertEquals("a=%d", vmware.getHypervisor().getMetrics().get(0).getFormat());
        Assert.assertEquals("GW:max(d1,d2)", vmware.getHypervisor().getMetrics().get(1).getExpression());
        Assert.assertEquals("d=%d", vmware.getHypervisor().getMetrics().get(1).getFormat());
        // @since CloudHub 2.3 serviceType
        Assert.assertEquals("CLUSTER", vmware.getHypervisor().getMetrics().get(0).getServiceType());
        Assert.assertEquals("HOST", vmware.getHypervisor().getMetrics().get(1).getServiceType());

        Assert.assertEquals(2, vmware.getVm().getMetrics().size());
        Assert.assertEquals("g", vmware.getVm().getMetrics().get(0).getName());
        Assert.assertEquals("gg", vmware.getVm().getMetrics().get(0).getCustomName());
        Assert.assertEquals("gg", vmware.getVm().getMetrics().get(0).getServiceName());
        Assert.assertEquals("kdesc", vmware.getVm().getMetrics().get(1).getDescription());
        Assert.assertEquals(789.0, vmware.getVm().getMetrics().get(0).getCriticalThreshold(), 0.0001);
        Assert.assertEquals(false, vmware.getVm().getMetrics().get(1).isGraphed());
        // @since CloudHub 2.3 expressions
        Assert.assertEquals("GW:max(g1,g2)", vmware.getVm().getMetrics().get(0).getExpression());
        Assert.assertEquals("g=%d", vmware.getVm().getMetrics().get(0).getFormat());
        Assert.assertEquals("GW:max(k1,k2)", vmware.getVm().getMetrics().get(1).getExpression());
        Assert.assertEquals("k=%d", vmware.getVm().getMetrics().get(1).getFormat());
        // @since CloudHub 2.3 serviceType
        Assert.assertEquals("HDFS", vmware.getVm().getMetrics().get(0).getServiceType());
        Assert.assertEquals("HDFS", vmware.getVm().getMetrics().get(1).getServiceType());

        Assert.assertEquals(Metric.SOURCE_TYPE_NETWORK, vmware.getHypervisor().getMetrics().get(0).getSourceType());
        Assert.assertEquals(Metric.COMPUTE_TYPE_REGEX, vmware.getHypervisor().getMetrics().get(0).getComputeType());
        Assert.assertEquals(Metric.SOURCE_TYPE_STORAGE, vmware.getHypervisor().getMetrics().get(1).getSourceType());
        Assert.assertEquals(Metric.COMPUTE_TYPE_REGEX, vmware.getHypervisor().getMetrics().get(1).getComputeType());
        Assert.assertNull(vmware.getVm().getMetrics().get(0).getSourceType());
        Assert.assertNull(vmware.getVm().getMetrics().get(0).getComputeType());


        service.removeProfile(VirtualSystem.VMWARE, TEST_AGENT);
        service.removeProfile(VirtualSystem.REDHAT, TEST_AGENT);
        assert(!service.doesProfileExist(VirtualSystem.VMWARE, TEST_AGENT));
        assert(!service.doesProfileExist(VirtualSystem.REDHAT, TEST_AGENT));

    }

    @Test
    public void testContainerProfile() {
        ProfileService service = new ProfileServiceImpl();
        ContainerProfile docker = service.createContainerProfile(VirtualSystem.DOCKER, TEST_AGENT);
        Metric m1 = new Metric("a", "adesc", false, false, 987, 123, null, null, "aa", "GW:min(aa1,aa2)", "aa=%d", "HOST");
        Metric m2 = new Metric("d", "ddesc", true, true, 654, 456, null, null, null, null, null, null);
        docker.getEngine().addMetric(m1);
        docker.getEngine().addMetric(m2);
        Metric m3 = new Metric("g", "gdesc", false, true, 321, 789, null, null, "gg", "GW:min(gg1,gg2)", "gg=%d", null);
        Metric m4 = new Metric("k", "kdesc", true, false, 123, 012, null, null, null, null, null, null);
        docker.getContainer().addMetric(m3);
        docker.getContainer().addMetric(m4);
        service.saveProfile(docker);

        assert(service.doesProfileExist(VirtualSystem.DOCKER, TEST_AGENT));

        docker = service.readContainerProfile(VirtualSystem.DOCKER, TEST_AGENT);
        Assert.assertNotNull(docker);
        assert docker.getAgent().equals(TEST_AGENT);
        Assert.assertEquals(2, docker.getEngine().getMetrics().size());
        Assert.assertEquals("a", docker.getEngine().getMetrics().get(0).getName());
        Assert.assertEquals("ddesc", docker.getEngine().getMetrics().get(1).getDescription());
        Assert.assertEquals(123.0, docker.getEngine().getMetrics().get(0).getCriticalThreshold(), 0.0001);
        Assert.assertEquals(true, docker.getEngine().getMetrics().get(1).isMonitored());
        // @since CloudHub 2.3 expressions
        Assert.assertEquals("GW:min(aa1,aa2)", docker.getEngine().getMetrics().get(0).getExpression());
        Assert.assertEquals("aa=%d", docker.getEngine().getMetrics().get(0).getFormat());
        Assert.assertEquals(null, docker.getEngine().getMetrics().get(1).getExpression());
        Assert.assertEquals(null, docker.getEngine().getMetrics().get(1).getFormat());

        Assert.assertEquals(2, docker.getContainer().getMetrics().size());
        Assert.assertEquals("g", docker.getContainer().getMetrics().get(0).getName());
        Assert.assertEquals("gg", docker.getContainer().getMetrics().get(0).getCustomName());
        Assert.assertEquals("gg", docker.getContainer().getMetrics().get(0).getServiceName());
        Assert.assertEquals("kdesc", docker.getContainer().getMetrics().get(1).getDescription());
        Assert.assertEquals(789.0, docker.getContainer().getMetrics().get(0).getCriticalThreshold(), 0.0001);
        Assert.assertEquals(false, docker.getContainer().getMetrics().get(1).isGraphed());
        // @since CloudHub 2.3 expressions
        Assert.assertEquals("GW:min(gg1,gg2)", docker.getContainer().getMetrics().get(0).getExpression());
        Assert.assertEquals("gg=%d", docker.getContainer().getMetrics().get(0).getFormat());
        Assert.assertEquals(null, docker.getContainer().getMetrics().get(1).getExpression());
        Assert.assertEquals(null, docker.getContainer().getMetrics().get(1).getFormat());

        service.removeProfile(VirtualSystem.DOCKER, TEST_AGENT);
        assert(!service.doesProfileExist(VirtualSystem.DOCKER, TEST_AGENT));

    }

    @Test
    public void testReadCustomName() throws Exception {
        ProfileService service = new ProfileServiceImpl() ;
        copyUnitTestProfile(VirtualSystem.CLOUDERA, TEST_AGENT);
        try {
            CloudHubProfile clouderaProfile = service.readCloudProfile(VirtualSystem.CLOUDERA, TEST_AGENT);
            assert (clouderaProfile.getHypervisor().getMetrics().size() > 0);
            List<Metric> metrics = clouderaProfile.getVm().getMetrics();
            for (Metric metric : metrics) {
                assert metric.getName() != null;
                if (metric.getServiceType().equals("ZOOKEEPER")) {
                    System.out.println("name: " + metric.getName());
                    System.out.println("custom name: " + metric.getCustomName());
                    System.out.println("service name: " + metric.getServiceName());
                    System.out.println("--");
                }
            }
        }
        finally {
            service.removeProfile(VirtualSystem.CLOUDERA, TEST_AGENT);
            assert (!service.doesProfileExist(VirtualSystem.CLOUDERA, TEST_AGENT));
        }
    }

}
