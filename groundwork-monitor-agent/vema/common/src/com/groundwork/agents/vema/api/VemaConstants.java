/*
 * Copyright 2012 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundwork.agents.vema.api;

import java.util.ArrayList;
import java.util.Arrays;

public interface VemaConstants 
{
    public static enum VSystem
    {
        VMWARE,
        CITRIX,
        ORACLE,          // AKA "SUN"
        MICROSOFT,
        REDHAT,
        AMAZON,
        GOOGLE
    }

    public static enum MonitorState
    {
        UP,              // these are RUN-state enums
        DOWN,            // which would in theory be stored
        WARNING,         // in the associated persistent
        CRITICAL,        // objects regarding their
        UNREACHABLE,     // accessibility.
        SUSPENDED,
        UNKNOWN,
    };

    public static enum ConnectionState
    {
        NASCENT,       // -> connecting
        CONNECTING,    // -> connected | timedout
        CONNECTED,     // -> disconnected
        TIMEDOUT,      // -> timedout | failed
        FAILED,        // -> failed
        DISCONNECTED,  // -> connecting
        SEMICONNECTED  // -> connected   (in multihost case)
    };

    /**
     * A set of date-time formatting strings corresponding to
     * the format sent in vmWare, and that expected by GWOS
     *
     * This list should be updated with other system strings, and potentially
     * for things like 'VMware versions'.
     */
    public static String rhevDateFormat               = "yyyy-MM-dd'T'HH:mm:ss.SSS";  // really special see VemaBaseVM
    public static String vmWareDateFormat             = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static String gwosDateFormat               = "yyyy-MM-dd HH:mm:ss";

    public static String CONNECTOR_VMWARE             = "vmware";
    public static String CONNECTOR_CITRIX             = "xen";
    public static String CONNECTOR_ORACLE             = "vbox";
    public static String CONNECTOR_MICROSOFT          = "hyperv";
    public static String CONNECTOR_RHEV               = "rhev";
    public static String CONNECTOR_AMAZON             = "ec2";
    public static String CONNECTOR_GOOGLE             = "goog";

    public static String APPLICATIONTYPE_VMWARE       = "VEMA";  // eventually must become CHVMWARE
    public static String APPLICATIONTYPE_CITRIX       = "CHXEN";
    public static String APPLICATIONTYPE_ORACLE       = "CHVBOX";  // oracle/sun 'virtual box'
    public static String APPLICATIONTYPE_MICROSOFT    = "CHMSHV";  // microsoft hyper-V
    public static String APPLICATIONTYPE_RHEV         = "CHRHEV";
    public static String APPLICATIONTYPE_AMAZON       = "CHEC2";
    public static String APPLICATIONTYPE_GOOGLE       = "CHGOOG";  // maybe GOCE (compute engine)

    //------------------------------------------------------------------------------------------------------
    // IMPORTANT "Words of Art" used by different vendors
    //------------------------------------------------------------------------------------------------------
    // Concept                          VEMA      VMware                       RHEV     
    //------------------------------------------------------------------------------------------------------
    // Virtual Machine                  VM        VirtualMachine               VM
    // Host Machine (hardware computer) HOST      HOST                         HOST
    // Host kernel (hypervisor)         HYPER     hypervisor                   ---
    // Data Center                      DC        datacenter                   datacenter
    // Virtual Network  (for VMs)       VLAN      VLAN                         VLAN
    // Virtual Disk     (for VMs)       VDISK     Allocated Disk               ---
    // Virtual Disk                     VDISK     Virtual Disk                 ---
    // Compute Cluster  (hardware)      CLUSTER   Cluster Compute Resource     cluster
    // Resource Pool    (hardware)      RESOURCE  Resource Pool                ---
    // Datastore (hardware side)        DSTORE    Datastore                    storage pool
    // Virtual Center                   VCENTER   Virtual Center               ---
    //------------------------------------------------------------------------------------------------------

    public static String OBJECTTYPE_VM                = "VM";        // AKA "virtual machine"
    public static String OBJECTTYPE_HOST              = "HOST";      // AKA "hardware machine"
    public static String OBJECTTYPE_HYPERVISOR        = "HYPER";     // AKA "hardware operating system"
    public static String OBJECTTYPE_DATACENTER        = "DC";
    public static String OBJECTTYPE_NETWORK           = "VLAN";      // AKA "virtual network"
    public static String OBJECTTYPE_VDISK             = "VDISK";     // AKA "virtual disk"
    public static String OBJECTTYPE_CLUSTER           = "CLUSTER";   // AKA "cluster compute resource"
    public static String OBJECTTYPE_RESOURCE          = "RESOURCE";  // AKA "Resource pool"
    public static String OBJECTTYPE_DSTORE            = "DSTORE";    // AKA "Data storage"
    public static String OBJECTTYPE_VCENTER           = "VCENTER";   // AKA "virtual center"

    /**
     * Entity scope for Hostgroup operations
     */
    public static String ENTITY_MGMT_SERVER           = "mgmt-server";
    public static String ENTITY_HYPERVISOR            = "hypervisor";

    /**
     *  Prefixes used for Hostgroups in GroundWork Monitor
     */
    public static String MGMT_SERVER_VMWARE           = "vSphere management server";
    public static String HYPERVISOR_VMWARE            = "ESXi hypervisor";

    public static String MGMT_SERVER_RHEV             = "RHEV management server";
    public static String HYPERVISOR_RHEV              = "RHEV hypervisor";    

    public static String PREFIX_VMWARE_MGMT_SERVER    = "VSS:";
    public static String PREFIX_VMWARE_HYPERVISOR     = "ESX:";
    public static String PREFIX_VMWARE_NETWORK        = "NET:";
    public static String PREFIX_VMWARE_CLUSTER        = "CLSTR:";
    public static String PREFIX_VMWARE_STORAGE        = "STOR:";
    public static String PREFIX_VMWARE_DATACENTER     = "DC:";

    // ----------------------------------------------------------------------
    // RED HAT ENTITIES
    //  
    //  Management server - collects information about a RHEV vm/host collection
    //  Hypervisor        - hardware host
    //  Network           - logical connected network
    //  Cluster           - cluster of VMs
    //  Storage Domain    - that VMs might be attached to
    //  Data Center       - largest entity, the "data center"
    // ----------------------------------------------------------------------
    public static String PREFIX_RHEV_MGMT_SERVER      = "RHEV-M:";
    public static String PREFIX_RHEV_HYPERVISOR       = "RHEV-H:";
    public static String PREFIX_RHEV_NETWORK          = "NET:";
    public static String PREFIX_RHEV_CLUSTER          = "CLSTR:";
    public static String PREFIX_RHEV_STORAGE          = "STOR:";
    public static String PREFIX_RHEV_DATACENTER       = "DC:";

    public static ArrayList<String> PREFIXLIST_VMWARE = new ArrayList<String>( Arrays.asList( 
            PREFIX_VMWARE_MGMT_SERVER,
            PREFIX_VMWARE_HYPERVISOR   
            ));

    public static ArrayList<String> PREFIXLIST_RHEV   = new ArrayList<String>( Arrays.asList( 
            PREFIX_RHEV_MGMT_SERVER,
            PREFIX_RHEV_HYPERVISOR   
            ));

    public static final String PROFILE_CANONICAL_BASE = "_monitoring_profile";   // note underbars!
    public static final String PROFILE_FILE_BASE      = "-monitoring-profile";
    public static final String CONFIG_CANONICAL_BASE  = "_gwos_config";          // more underbars!
    public static final String CONFIG_FILE_BASE       = "-gwos-config";

    public static final String VEMA_CONFIG_FILE       = "vema"           + CONFIG_FILE_BASE;
    public static final String VEMA_CONFIG_CANONICAL  = "vema"           + CONFIG_CANONICAL_BASE;

	public static final String VMWARE_CONFIG_FILE     = CONNECTOR_VMWARE + CONFIG_FILE_BASE;
	public static final String RHEV_CONFIG_FILE       = CONNECTOR_RHEV   + CONFIG_FILE_BASE;

 	public static final String VMWARE_PROFILE_FILE    = CONNECTOR_VMWARE + PROFILE_FILE_BASE;
	public static final String RHEV_PROFILE_FILE      = CONNECTOR_RHEV   + PROFILE_FILE_BASE;

	public static final String CONFIG_FILE_EXTN       = ".xml";
	public static final String CONFIG_FILE_PATH       = "/usr/local/groundwork/config/";

    public final  String HOSTLESS_VMS                 = "Inactive VMs";
}
