package org.groundwork.cloudhub.connectors.vmware2;

public abstract class VmWareBaseConverter {

    public static final String PROP_NAME = "name";
    // Host
    public static final String PROP_VM = "vm";
    public static final String PROP_HOST_MODEL = "summary.hardware.model";
    public static final String PROP_HOST_UPTIME = "summary.quickStats.uptime";

    // Virtual Machine
    public static final String PROP_GUEST_NETWORK = "guest.net";
    public static final String PROP_RUNTIME_HOST = "summary.runtime.host";
    public static final String PROP_GUEST_STATE = "guest.guestState";
    public static final String PROP_IP_ADDRESS  = "guest.ipAddress";
    // Host and Virtual Machine
    public static final String PROP_BOOTTIME = "summary.runtime.bootTime";
    public static final String PROP_UPTIME = "summary.quickStats.uptimeSeconds";

}
