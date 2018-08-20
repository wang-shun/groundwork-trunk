package org.groundwork.rs.client.hubs;
/*
 * Collage - The ultimate monitoring data integration framework.
 *
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.
 *
 */

import org.groundwork.rs.client.AbstractHostTest;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.NetHubProfile;
import org.groundwork.rs.dto.profiles.ProfileType;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

public class ProfileClientTest extends AbstractHostTest {


    @Test
    public void testRetrieveProfiles() throws Exception {
        if (serverDown) return;

        // Test Azure
        ProfileClient client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        CloudHubProfile azureProfile = client.lookupCloud(ProfileType.azure);
        assert azureProfile != null;
        assert azureProfile.getProfileType().equals(ProfileType.azure);

        // Test Nedi
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        CloudHubProfile nediProfile = client.lookupCloud(ProfileType.nedi);
        assert nediProfile != null;
        assert nediProfile.getProfileType().equals(ProfileType.nedi);

        // Test VMWare
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        CloudHubProfile profile = client.lookupCloud(ProfileType.vmware);
        assert profile != null;
        validateVMWare(profile);

        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        profile = client.lookupCloud(ProfileType.vmware);
        assert profile != null;
        validateVMWare(profile);

        // Test RedHat
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        profile = client.lookupCloud(ProfileType.rhev);
        assert profile != null;
        validateRedHat(profile);

        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        profile = client.lookupCloud(ProfileType.rhev);
        assert profile != null;
        validateRedHat(profile);

        // Test OpenStack
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        profile = client.lookupCloud(ProfileType.openstack);
        assert profile != null;
        validateOpenStack(profile);

        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        profile = client.lookupCloud(ProfileType.openstack);
        assert profile != null;
        validateOpenStack(profile);

        // Test Open DayLight
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        NetHubProfile netProfile = client.lookupNetwork(ProfileType.opendaylight);
        assert netProfile != null;
        validateOpenDaylight(netProfile);

        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        netProfile = client.lookupNetwork(ProfileType.opendaylight);
        assert netProfile != null;
        validateOpenDaylight(netProfile);

        // Test Docker
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        CloudHubProfile dockerProfile = client.lookupCloud(ProfileType.docker);
        assert dockerProfile != null;
        validateDocker(dockerProfile);

        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        dockerProfile = client.lookupCloud(ProfileType.docker);
        assert dockerProfile != null;
        validateDocker(dockerProfile);

        // Test Cloudera
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_XML_TYPE);
        CloudHubProfile clouderaProfile = client.lookupCloud(ProfileType.cloudera);
        assert clouderaProfile != null;
        validateCloudera(clouderaProfile);

        // Test Cloudera
        client = new ProfileClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        clouderaProfile = client.lookupCloud(ProfileType.cloudera);
        assert clouderaProfile != null;
        validateCloudera(clouderaProfile);

    }

    protected void validateVMWare(CloudHubProfile profile) {
        assert profile.getProfileType().equals(ProfileType.vmware);
        // Hypervisors
        assert profile.getHypervisor().getMetrics().size() == 10;
        Metric metric = profile.getHypervisor().getMetrics().get(0);
        assert "summary.quickStats.overallCpuUsage".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == false;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;

        metric = profile.getHypervisor().getMetrics().get(3);
        assert "syn.host.cpu.used".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 75;
        assert metric.getCriticalThreshold() == 95;
        assert metric.getFormat().equals("%d%%");
        assert metric.getExpression().startsWith("GW:percentageUsed");
        assert metric.getComputeType().equals("synthetic");

        // VMs
        assert profile.getVm().getMetrics().size() == 30;
        metric = profile.getVm().getMetrics().get(0);
        assert "summary.quickStats.balloonedMemory".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == false;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;

        metric = profile.getVm().getMetrics().get(16);
        assert "summary.storage.committed".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == false;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;

        metric = profile.getVm().getMetrics().get(23);
        assert "syn.vm.mem.swappedToConfigMemSize.used".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 75;
        assert metric.getCriticalThreshold() == 90;
    }

    protected void validateRedHat(CloudHubProfile profile) {
        assert profile.getProfileType().equals(ProfileType.rhev);
        // Hypervisors
        assert profile.getHypervisor().getMetrics().size() == 32;
        Metric metric = profile.getHypervisor().getMetrics().get(4);
        assert "stat.cpu.load.avg.5m.value".equals(metric.getName());
        assert "5 minute Load Average".equals(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 50;
        assert metric.getCriticalThreshold() == 90;

        metric = profile.getHypervisor().getMetrics().get(19);
        assert "syn.host.cpu.used".equals(metric.getName());
        assert "% CPU used".equals(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 80;
        assert metric.getCriticalThreshold() == 90;

        // VMs
        assert profile.getVm().getMetrics().size() == 21;
        metric = profile.getVm().getMetrics().get(11);
        assert "stat.cpu.current.guest.value".equals(metric.getName());
        assert "CPU Current Guest".equals(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 50.0;
        assert metric.getCriticalThreshold() == 90.0;

        metric = profile.getVm().getMetrics().get(20);
        assert "syn.vm.disk[2].actual".equals(metric.getName());
        assert "Percent Disk 2 Used".equals(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == false;
        assert metric.getWarningThreshold() == 50.0;
        assert metric.getCriticalThreshold() == 90.0;
    }

    protected void validateOpenStack(CloudHubProfile profile) {
        assert profile.getProfileType().equals(ProfileType.openstack);
        // Hypervisors

        assert profile.getHypervisor().getMetrics().size() == 3;
        Metric metric = profile.getHypervisor().getMetrics().get(0);
        assert "running_vms".equals(metric.getName());
        assert "Number of running Virtual Machines".equals(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 8;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getComputeType() == null;
        assert metric.getSourceType() == null;


        metric = profile.getHypervisor().getMetrics().get(1);
        assert "free_ram_mb".equals(metric.getName());
        assert "Free Memory in MB".equals(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 2;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getComputeType() == null;
        assert metric.getSourceType() == null;

        // VMs
        assert profile.getVm().getMetrics().size() == 22;
        metric = profile.getVm().getMetrics().get(0);
        assert "disk.read.bytes".equals(metric.getName());
        assert "Cumulative Bytes Read from Disk".equals(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getComputeType() == null;
        assert metric.getSourceType().equals(Metric.SOURCE_TYPE_CEILOMETER);

        metric = profile.getVm().getMetrics().get(4);
       assert "cpu_util".equals(metric.getName());
       assert "Percent CPU Utilization (Gauge)".equals(metric.getDescription());
       assert metric.isGraphed() == true;
       assert metric.isMonitored() == true;
       assert metric.getWarningThreshold() == 75;
       assert metric.getCriticalThreshold() == 95;
       assert metric.getComputeType() == null;
       assert metric.getSourceType().equals(Metric.SOURCE_TYPE_CEILOMETER);

        metric = profile.getVm().getMetrics().get(5);
        assert "memory".equals(metric.getName());
        assert "Total Memory on VM/Server (bytes)".equals(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getComputeType() == null;
        assert metric.getSourceType() == null;

        metric = profile.getVm().getMetrics().get(8);
        assert "cpu(.)_time".equals(metric.getName());
        assert "CPU Execution Time (Hertz)".equals(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == false;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getComputeType().equals(Metric.COMPUTE_TYPE_REGEX);
        assert metric.getSourceType() == null;

        metric = profile.getVm().getMetrics().get(10);
        assert "tap(.+)_rx".equals(metric.getName());
        assert "Network TAP device Byte Receive Count".equals(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getComputeType().equals(Metric.COMPUTE_TYPE_REGEX);
        assert metric.getSourceType() == null;

    }

    protected void validateOpenDaylight(NetHubProfile profile) {
        assert profile.getProfileType().equals(ProfileType.opendaylight);
        // Controllers
        assert profile.getController().getMetrics().size() == 0;

        // Switches
        assert profile.getSwitch().getMetrics().size() == 12;
        Metric metric = profile.getSwitch().getMetrics().get(0);
        assert "receivePackets".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == false;
        assert metric.getWarningThreshold() == 10000;
        assert metric.getCriticalThreshold() == 300000;

        metric = profile.getSwitch().getMetrics().get(7);
        assert "transmitErrors".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 100;
        assert metric.getCriticalThreshold() == 500;

    }

    protected void validateDocker(CloudHubProfile profile) {
        assert profile.getProfileType().equals(ProfileType.docker);
        // Engine
        assert profile.getHypervisor().getMetrics().size() == 3;
        Metric metric = profile.getHypervisor().getMetrics().get(1);
        assert "cpu.usage.total".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 75;
        assert metric.getCriticalThreshold() == 90;

        // Containers
        assert profile.getVm().getMetrics().size() == 11;
        metric = profile.getVm().getMetrics().get(9);
        assert "syn.memory.usage".equals(metric.getName());
        assert "Memory usage in mega bytes".equals(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == 2048;
        assert metric.getCriticalThreshold() == 4096;

    }

    protected void validateCloudera(CloudHubProfile profile) {
        assert profile.getProfileType().equals(ProfileType.cloudera);
        // Cluster, Host
        assert profile.getHypervisor().getMetrics().size() > 1;
        Metric metric = profile.getHypervisor().getMetrics().get(1);
        assert "total_read_bytes_rate_across_disks".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == false;
        assert metric.isMonitored() == false;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getServiceType().equals("CLUSTER");

        // Services
        assert profile.getVm().getMetrics().size() > 4;
        metric = profile.getVm().getMetrics().get(4);
        assert "total_read_requests_rate_across_regionservers".equals(metric.getName());
        assert !isEmpty(metric.getDescription());
        assert metric.isGraphed() == true;
        assert metric.isMonitored() == true;
        assert metric.getWarningThreshold() == -1;
        assert metric.getCriticalThreshold() == -1;
        assert metric.getServiceType().equals("HBASE");

    }

    protected boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
}
