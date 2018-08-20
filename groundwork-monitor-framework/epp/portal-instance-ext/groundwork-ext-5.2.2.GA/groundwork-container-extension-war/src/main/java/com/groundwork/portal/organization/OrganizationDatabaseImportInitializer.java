package com.groundwork.portal.organization;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationDatabaseInitializer;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

public class OrganizationDatabaseImportInitializer implements Startable {
    protected static Log log = ExoLogger.getLogger(OrganizationDatabaseImportInitializer.class);

    public OrganizationDatabaseImportInitializer(InitParams params) throws Exception {
        // super(params);
        log.error("Constructor");
        // TODO Auto-generated constructor stub
    }

    // @Override
    // public void init(OrganizationService service) throws Exception {
    // // TODO Auto-generated method stub
    // log.error("Database exists?: " + (checkExistDatabase(service)));
    // super.init(service);
    //
    // // throw new Exception("Blahhh : " + checkExistDatabase(service));
    // }
    public void start() {
        // TODO Auto-generated method stub
        log.error("start");
    }

    public void stop() {
        // TODO Auto-generated method stub
        log.error("stop");
    }
}
