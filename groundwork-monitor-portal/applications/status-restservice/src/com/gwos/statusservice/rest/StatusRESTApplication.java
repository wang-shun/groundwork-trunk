package com.gwos.statusservice.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rest")
public class StatusRESTApplication extends Application {

	private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> prototypes = new HashSet<Class<?>>();

    public StatusRESTApplication() {
        singletons.add(new EntityStatus());  
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