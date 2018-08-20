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
package org.groundwork.cloudhub.connectors;

import java.text.SimpleDateFormat;

public interface ConnectorConstants
{
    public static final String SYNTHETIC_PREFIX = "syn.";

    /**
     * A set of date-time formatting strings corresponding to
     * the format sent in vmWare, and that expected by GWOS
     *
     * This list should be updated with other system strings, and potentially
     * for things like 'VMware versions'.
     */
    public static String rhevDateFormat               = "yyyy-MM-dd'T'HH:mm:ss.SSS";  // really special see VemaBaseVM
    public static String vmWareDateFormat             = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static String vmWareDateFormat2            = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static String gwosDateFormat               = "yyyy-MM-dd HH:mm:ss";
    public static String gwosIsoDateFormat            = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    SimpleDateFormat VMWARE_DATE_FORMAT = new SimpleDateFormat(ConnectorConstants.vmWareDateFormat);
    SimpleDateFormat VMWARE_DATE_FORMAT2 = new SimpleDateFormat(ConnectorConstants.vmWareDateFormat2);
    SimpleDateFormat RHEV_DATE_FORMAT = new SimpleDateFormat(ConnectorConstants.rhevDateFormat);

    // Reserved for future use. When implementing, moving constants to Configuration Providers
    public static String CONNECTOR_CITRIX             = "xen";
    public static String CONNECTOR_ORACLE             = "vbox";
    public static String CONNECTOR_MICROSOFT          = "hyperv";
    public static String CONNECTOR_AMAZON             = "ec2";
    public static String CONNECTOR_GOOGLE             = "goog";

    // Reserved for future use. When implementing, moving constants to Configuration Providers
    public static String APPLICATIONTYPE_CITRIX       = "CHXEN";
    public static String APPLICATIONTYPE_ORACLE       = "CHVBOX";  // oracle/sun 'virtual box'
    public static String APPLICATIONTYPE_MICROSOFT    = "CHMSHV";  // microsoft hyper-V
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

    /**
     * Entity scope for Hostgroup operations
     */
    public static String ENTITY_MGMT_SERVER           = "mgmt-server";
    public static String ENTITY_HYPERVISOR            = "hypervisor";
    public static String ENTITY_VM                    = "vm";

    /**
     * Default Prefixes for VMs variations
     */
    public static String PREFIX_VM_NETWORK     = "NET-";
    public static String PREFIX_VM_STORAGE     = "STOR-";

    public static String PREFIX_NETWORK        = "NET:";
    public static String PREFIX_STORAGE        = "STOR:";
    public static String PREFIX_POOL           = "POOL:";
    public static String PREFIX_CLUSTER        = "CLSTR:";
    public static String PREFIX_DATACENTER     = "DC:";

    public static String [] SPECIAL_PREFIXES = {
            PREFIX_NETWORK,
            PREFIX_STORAGE,
            PREFIX_POOL
    };

    public static String [] VM_PREFIXES = {
            PREFIX_VM_NETWORK,
            PREFIX_VM_STORAGE
    };

    public static final String CONFIG_CANONICAL_BASE  = "_gwos_config";          // more underbars!
    public static final String CONFIG_FILE_BASE       = "-gwos-config";

    public static final String VEMA_CONFIG_FILE       = "vema"           + CONFIG_FILE_BASE;
    public static final String VEMA_CONFIG_CANONICAL  = "vema"           + CONFIG_CANONICAL_BASE;

    public static final String LEGACY_CONNECTOR_VMWARE = "vmware";
    public static final String LEGACY_CONNECTOR_RHEV = "rhev";
	public static final String LEGACY_VMWARE_CONFIG_FILE = LEGACY_CONNECTOR_VMWARE + CONFIG_FILE_BASE;
	public static final String LEGACY_RHEV_CONFIG_FILE = LEGACY_CONNECTOR_RHEV + CONFIG_FILE_BASE;

	public static final String CONFIG_FILE_EXTN       = ".xml";
	public static final String CONFIG_FILE_PATH       = "/usr/local/groundwork/config/";

    public final  String HOSTLESS_VMS                 = "Inactive VMs";


}
