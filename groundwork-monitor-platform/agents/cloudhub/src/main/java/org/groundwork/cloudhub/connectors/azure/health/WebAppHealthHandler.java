package org.groundwork.cloudhub.connectors.azure.health;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.SiteAvailabilityState;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebAppBase;
import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.HashMap;
import java.util.Map;

public class WebAppHealthHandler implements AzureHealthHandler {

    private static final Map<SiteAvailabilityState,HealthInfo> AZURE_SITE_AVAILABILITY_TO_GW_STATE = ImmutableMap.<SiteAvailabilityState,HealthInfo> builder()
            .put(SiteAvailabilityState.NORMAL, new HealthInfo(GwosStatus.UP.status))
            .put(SiteAvailabilityState.LIMITED, new HealthInfo(GwosStatus.WARNING.status))
            .put(SiteAvailabilityState.DISASTER_RECOVERY_MODE, new HealthInfo(GwosStatus.UNSCHEDULED_DOWN.status))
            .build();


    @Override
    public HealthInfo healthCheck(Object resource) {
        SiteAvailabilityState availState = null;
        if (resource instanceof WebAppBase) {
            WebAppBase sitesApp = (WebAppBase) resource;
            availState = sitesApp.availabilityState();
        }
        HealthInfo info  = AZURE_SITE_AVAILABILITY_TO_GW_STATE.get(availState);
        if (info == null) {
            return new HealthInfo(GwosStatus.UNREACHABLE.status);
        }
        return info;
    }

    @Override
    public Map<String, Object> buildResources(Azure azure) {
        Map<String, Object> resourceMap = new HashMap<>();
        // TODO: aggregate both function app and web app, any more?
        for (FunctionApp functionApp : azure.appServices().functionApps().list()) {
            resourceMap.put(functionApp.id().toLowerCase(), functionApp);
        }
        for (WebApp webApp : azure.webApps().list()) {
            resourceMap.put(webApp.id().toLowerCase(), webApp);
        }
        return resourceMap;
    }

}
