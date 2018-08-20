package com.groundwork.dashboard.configuration;

public class DashboardConfigurationFactory {

    public enum ServiceType {
        NOC
    };

    private static DashboardConfigurationService serviceInstance = null;
    private static Object instanceLock = new Object();

    public static DashboardConfigurationService getConfigurationService(ServiceType serviceType) {
        if (serviceInstance == null) {
            synchronized (instanceLock) {
                if (serviceInstance == null) {
                    serviceInstance = new DashboardConfigurationServiceImpl();
                }
            }
        }
        return serviceInstance;
    }
}
