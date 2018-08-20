package org.groundwork.cloudhub.connectors.openstack.client;

/**
 * Created by dtaylor on 12/20/13.
 */
public class TenantInfo {

    public String accessToken;
    public String tenantId;
    public String tenantName;

    public TenantInfo() {

    }

    public TenantInfo(String accessToken, String tenantId, String tenantName) {
        this.accessToken = accessToken;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
    }
}
