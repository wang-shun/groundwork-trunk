package org.groundwork.agents.monitor;

public enum VirtualSystem {
    VMWARE,
    CITRIX,
    ORACLE,
    AZURE,
    REDHAT,
    AMAZON,
    GOOGLE,
    SELENIUM,
    OPENSTACK,
    OPENSHIFT,
    DOCKER,
    CISCO,
    OPENDAYLIGHT,
    NSX,
    LOADTEST,
    NETAPP,
    CLOUDERA,
    ICINGA2,
    NEDI;

    public static VirtualSystem[] activeVirtualSystems = {
            VMWARE,
            AZURE,
            REDHAT,
            OPENSTACK,
            DOCKER,
            OPENDAYLIGHT,
            AMAZON,
            LOADTEST,
            NETAPP,
            CLOUDERA,
            ICINGA2,
            NEDI
    };

    public static String[] activeCloudHubApplicationTypes = {
            "VEMA",
            "AZURE",
            "CHRHEV",
            "OS",
            "DOCK",
            "ODL",
            "AWS",
            //"LOADTEST",
            "NETAPP",
            "CLOUDERA",
            "ICINGA2",
            "NEDI"
    };
}
