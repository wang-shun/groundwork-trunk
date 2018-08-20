package com.groundworkopensource.portal.common.ws;

import com.groundworkopensource.portal.common.CommonConstants;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.ApplicationTypeClient;
import org.groundwork.rs.dto.DtoApplicationType;

import java.util.List;

/**
 * Created by ArulShanmugam on 4/17/14.
 */
public class ApplicationTypeHelper {
    private static List<DtoApplicationType> appTypes = null;

    public static List<DtoApplicationType> getApplicationTypes() {
        if(appTypes == null) {
            ApplicationTypeClient client = new ApplicationTypeClient(WSClientConfiguration.getProperty(CommonConstants.FOUNDATION_REST_MOUNT_POINT));
            appTypes = client.list();
        }
        return appTypes;
    }
}