package com.groundwork.downtime;

import com.groundwork.downtime.db.DowntimeDBService;
import com.groundwork.downtime.http.DowntimeHttpService;

public class DowntimeServiceFactory {

    private static DowntimeService serviceInstance = null;
    private static Object instanceLock = new Object();
    private static boolean USE_DATABASE_SERVICE = false;

    public static DowntimeService getServiceInstance() {
        if (serviceInstance == null) {
            synchronized (instanceLock) {
                if (serviceInstance == null) {
                    serviceInstance = (USE_DATABASE_SERVICE) ? new DowntimeDBService() : new DowntimeHttpService();
                }
            }
        }
        return serviceInstance;
    }
}
