package com.groundwork.portal.organization;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;

public class OrganizationImportListener extends BaseComponentPlugin {
    protected static Log log = ExoLogger.getLogger(OrganizationImportListener.class);

    public OrganizationImportListener(InitParams params) throws Exception {
        log.error("Init of listener");
    }

    public void init(OrganizationService service) throws Exception {
        log.error("Init of init");
        this.checkExistDatabase(service);
    }

    /**
     * Borrowed from super class.
     *
     * @param   service
     *
     * @return
     *
     * @throws  Exception
     */
    private boolean checkExistDatabase(OrganizationService service) throws Exception {
        PageList users = service.getUserHandler().getUserPageList(10);

        if ((users != null) && (users.getAvailable() > 0)) {
            return true;
        }

        return false;
    }
}
