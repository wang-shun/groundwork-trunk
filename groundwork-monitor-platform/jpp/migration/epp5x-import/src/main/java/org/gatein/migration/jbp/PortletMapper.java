package org.gatein.migration.jbp;

import java.util.HashMap;
import java.util.Map;

public enum PortletMapper {
    STATUS_VIEWER            ("CustomGroups", "OrganizationPortlet", "exoadmin"),
    ADMIN                    ("AdminPortlet", "OrganizationPortlet", "exoadmin"),
    FOUNDATION_ADMINISTRATION("FoundationAdministration", "OrganizationPortlet", "exoadmin"),
    IDENTITY_ADMIN_PORTLET   ("IdentityAdminPortlet", "OrganizationPortlet", "exoadmin"),
    NAGVIS_PORTLET_ADMIN     ("nagvis-portlet-admin", "OrganizationPortlet", "exoadmin"),
    VEMA_4_VMWARE            ("VEMA4VMWare", "OrganizationPortlet", "exoadmin"),
    GW_AUTOMATION_VIEW       ("GroundWorkAutomationView", "OrganizationPortlet", "exoadmin"),
    GW_AUTOCONFIG_VIEW       ("GroundWorkAutoConfigureView", "OrganizationPortlet", "exoadmin"),
    VALIDATE_LICENSE         ("ValidateLicense", "OrganizationPortlet", "exoadmin");

    private static Map<String, PortletMapper> portletMap = new HashMap<String, PortletMapper>();

    static {
        portletMap.put(STATUS_VIEWER.getJbossName(), STATUS_VIEWER);
        portletMap.put(ADMIN.getJbossName(), ADMIN);
        portletMap.put(FOUNDATION_ADMINISTRATION.getJbossName(), FOUNDATION_ADMINISTRATION);
        portletMap.put(IDENTITY_ADMIN_PORTLET.getJbossName(), IDENTITY_ADMIN_PORTLET);
        portletMap.put(VALIDATE_LICENSE.getJbossName(), VALIDATE_LICENSE);
        portletMap.put(VEMA_4_VMWARE.getJbossName(), VEMA_4_VMWARE);
        portletMap.put(NAGVIS_PORTLET_ADMIN.getJbossName(), NAGVIS_PORTLET_ADMIN);
        portletMap.put(GW_AUTOCONFIG_VIEW.getJbossName(), GW_AUTOCONFIG_VIEW);
    }

    private String jbossName;
    private String eppName;
    private String applicationRegRef;

    private PortletMapper(String jbossName, String eppName, String applicationRegRef) {
        this.jbossName         = jbossName;
        this.eppName           = eppName;
        this.applicationRegRef = applicationRegRef;
    }

    public static PortletMapper find(String exportPortletName) {
        PortletMapper portlet = portletMap.get(exportPortletName);

        if (portlet == null) {
            // TODO throw exception
            return STATUS_VIEWER;

            // throw new IllegalArgumentException(exportPortletName +
            // " is not defined");
        }

        return portlet;
    }

    public String getJbossName() {
        return jbossName;
    }

    public String getEppName() {
        return eppName;
    }

    public String getApplicationRegRef() {
        return applicationRegRef;
    }
}
