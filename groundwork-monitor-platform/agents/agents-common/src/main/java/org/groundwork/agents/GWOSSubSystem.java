package org.groundwork.agents;

import org.groundwork.agents.monitor.VirtualSystem;

public enum GWOSSubSystem {
    CloudHub,
    NetHub,
    Selenium,
    SolarWinds;

    public static GWOSSubSystem convertVirtualSystemToSubsystem(VirtualSystem virtualSystem) {
        switch (virtualSystem) {
            case VMWARE:
            case ORACLE:
            case AZURE:
            case REDHAT:
            case AMAZON:
            case GOOGLE:
            case OPENSTACK:
            case OPENSHIFT:
            case DOCKER:
            case LOADTEST:
            case NETAPP:
            case CLOUDERA:
                return CloudHub;
            case CISCO:
            case OPENDAYLIGHT:
            case NSX:
            case CITRIX:
                return NetHub;
            case SELENIUM:
                return Selenium;
            default:
                return CloudHub;
        }
    }
}




