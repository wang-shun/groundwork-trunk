package org.groundwork.cloudhub.web;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by dtaylor on 4/21/17.
 */
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            /**
             * Force Resteasy provider to load prior to CXF provider
             * Otherwise, this class cast fails:
             *      instance = (ResteasyProviderFactory)RuntimeDelegate.getInstance();
             * see resteasy's ResteasyProviderFactory.getInstance, line 216 (cast)
             * version: resteasy-jaxrs-2.3.4.Final.jar
             */
            ResteasyProviderFactory factory = new ResteasyProviderFactory();
            ResteasyProviderFactory.setRegisterBuiltinByDefault(true);
            ResteasyProviderFactory.setInstance(factory);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
