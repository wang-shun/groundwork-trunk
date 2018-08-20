package com.groundwork.portal.security;

import javax.security.auth.login.LoginException;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.jaas.AbstractLoginModule;

public class InfoLoginModule extends AbstractLoginModule {
    private static final Log log                 = ExoLogger.getLogger(InfoLoginModule.class);
    private String           portalContainerName = "groundwork-portal";

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean commit() throws LoginException {
        return true;
    }

    public boolean login() throws LoginException {
        log.info("InfoLoginModule start");

        try {
            // for (Object object : getContainer().getComponentInstances()) {
            // log.info("Component: " + object);
            // }
            OrganizationService organizationService = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
            Identity            identity            = (Identity) sharedState.get("exo.security.identity");

            if (identity == null) {
                LoginException loginException = new LoginException("User identity was not found. Aborting login");

                throw loginException;
            }

            UserProfile up = organizationService.getUserProfileHandler().findUserProfileByName(identity.getUserId());

            log.info("User Profile for " + identity.getUserId() + " : " + ReflectionToStringBuilder.toString(up));
            log.info("UP props: " + ReflectionToStringBuilder.toString(up.getUserInfoMap()));
            log.info("Identity: " + ReflectionToStringBuilder.toString(identity));
        } catch (Exception ex) {
            // TODO Throw login exception when needed
            ex.printStackTrace();
        }

        log.info("InfoLoginModule end");

        return true;
    }

    public boolean logout() throws LoginException {
        return true;
    }

    @Override
    protected Log getLogger() {
        return log;
    }
}
