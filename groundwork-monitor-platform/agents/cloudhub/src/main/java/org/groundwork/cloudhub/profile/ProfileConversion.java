package org.groundwork.cloudhub.profile;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.NetHubProfile;
import org.groundwork.rs.dto.profiles.ProfileType;

public class ProfileConversion {

    public static ProfileType convertVirtualSystemToPropertyType(VirtualSystem virtualSystem) throws CloudHubException {
        switch (virtualSystem) {
            case VMWARE:
                return ProfileType.vmware;
            case CITRIX:
                return ProfileType.citrix;
            case ORACLE:
                return ProfileType.oracle;
            case AZURE:
                return ProfileType.azure;
            case REDHAT:
                return ProfileType.rhev;
            case AMAZON:
                return ProfileType.amazon;
            case GOOGLE:
                return ProfileType.google;
            case SELENIUM:
                return ProfileType.selenium;
            case OPENSTACK:
                return ProfileType.openstack;
            case OPENSHIFT:
                return ProfileType.openshift;
            case DOCKER:
                return ProfileType.docker;
            case CISCO:
                return ProfileType.cisco;
            case OPENDAYLIGHT:
                return ProfileType.opendaylight;
            case NSX:
                return ProfileType.nsx;
            case LOADTEST:
                return ProfileType.loadtest;
            case NETAPP:
                return ProfileType.netapp;
            case CLOUDERA:
                return ProfileType.cloudera;
            case NEDI:
                return ProfileType.nedi;
            case ICINGA2:
                return null;
            default:
                throw new CloudHubException("Unknown Virtual System");
        }
    }

    public static Class convertVirtualSystemToProfileClass(VirtualSystem virtualSystem) throws CloudHubException {
        switch (virtualSystem) {
            case VMWARE:
            case CITRIX:
            case ORACLE:
            case AZURE:
            case REDHAT:
            case AMAZON:
            case GOOGLE:
            case SELENIUM:
            case OPENSTACK:
            case OPENSHIFT:
            case LOADTEST:
            case NETAPP:
            case CLOUDERA:
            case NEDI:
            case DOCKER:
                return CloudHubProfile.class;
            case CISCO:
            case OPENDAYLIGHT:
            case NSX:
                return NetHubProfile.class;
            case ICINGA2:
                return null;
            default:
                throw new CloudHubException("Unknown Virtual System");
        }
    }


    public static VirtualSystem convertProfileTypeToVirtualSystem(ProfileType profileType) throws CloudHubException {
        switch (profileType) {
            case vmware:
                return VirtualSystem.VMWARE;
            case citrix:
                return VirtualSystem.CITRIX;
            case oracle:
                return VirtualSystem.ORACLE;
            case azure:
                return VirtualSystem.AZURE;
            case rhev:
                return VirtualSystem.REDHAT;
            case amazon:
                return VirtualSystem.AMAZON;
            case google:
                return VirtualSystem.GOOGLE;
            case selenium:
                return VirtualSystem.SELENIUM;
            case openstack:
                return VirtualSystem.OPENSTACK;
            case openshift:
                return VirtualSystem.OPENSHIFT;
            case docker:
                return VirtualSystem.DOCKER;
            case cisco:
                return VirtualSystem.CISCO;
            case opendaylight:
                return VirtualSystem.OPENDAYLIGHT;
            case nsx:
                return VirtualSystem.NSX;
            case loadtest:
                return VirtualSystem.LOADTEST;
            case netapp:
                return VirtualSystem.NETAPP;
            case cloudera:
                return VirtualSystem.CLOUDERA;
            case nedi:
                return VirtualSystem.NEDI;
            default:
                throw new CloudHubException("Unknown Virtual System");
        }

    }

    public static HubType convertVirtualSystemToHubType(VirtualSystem virtualSystem) throws CloudHubException {
        switch (virtualSystem) {
            case VMWARE:
            case ORACLE:
            case AZURE:
            case REDHAT:
            case AMAZON:
            case GOOGLE:
            case OPENSTACK:
            case OPENSHIFT:
            case CITRIX:
            case LOADTEST:
            case NETAPP:
            case CLOUDERA:
            case NEDI:
            case DOCKER:
                return HubType.cloud;
            case OPENDAYLIGHT:
            case NSX:
            case CISCO:
                return HubType.network;
            case SELENIUM:
                return HubType.test;
            case ICINGA2:
                return HubType.monitor;
            default:
                throw new CloudHubException("Unknown Virtual System");
        }
    }

    public static MetricType convertVirtualSystemToMetricType(VirtualSystem virtualSystem, boolean isPrimary) {
        switch (virtualSystem) {
            case VMWARE:
            case ORACLE:
            case AZURE:
            case REDHAT:
            case GOOGLE:
            case OPENSTACK:
            case OPENSHIFT:
            case CITRIX:
            case NETAPP:
            case CLOUDERA:
            case NEDI:
            case LOADTEST:
            case DOCKER:
                return (isPrimary) ? MetricType.hypervisor : MetricType.vm;
            case AMAZON:
                return MetricType.vm; // CLOUDHUB-354: Amazon has no Hypervisor Metrics
            case OPENDAYLIGHT:
            case NSX:
            case CISCO:
                return (isPrimary) ? MetricType.netController : MetricType.netSwitch;
            case SELENIUM:
                return (isPrimary) ? MetricType.testEngine : MetricType.test;
            case ICINGA2:
                return MetricType.monitor;
            default:
                return (isPrimary) ? MetricType.hypervisor : MetricType.vm;
        }
    }

}
