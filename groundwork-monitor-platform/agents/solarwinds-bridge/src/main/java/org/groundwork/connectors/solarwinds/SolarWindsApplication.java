package org.groundwork.connectors.solarwinds;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class SolarWindsApplication extends Application {

	private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> prototypes = new HashSet<Class<?>>();

    public SolarWindsApplication() {
        singletons.add(new ObjectMapperContextResolver());
        singletons.add(new SolarWindsTestResource());
        singletons.add(new HostBridgeResource());
        singletons.add(new ServiceBridgeResource());
    }

    @Override
    public Set<Object> getSingletons() {
            return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return prototypes;
    }

}