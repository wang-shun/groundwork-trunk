package org.groundwork.cloudhub.statistics;

import java.util.Date;

public class MonitoringStatistics {

    private String name;
    private Date date;

    private AddCounts addsHypervisors = new AddCounts();
    private AddCounts addsVMs = new AddCounts();
    private ModifyCounts modsHypervisors = new ModifyCounts();
    private ModifyCounts modsVMs = new ModifyCounts();

    private HostQueries hostQueries = new HostQueries();
    private ServiceQueries serviceQueries = new ServiceQueries();
    private EventQueries eventQueries = new EventQueries();

    private ExecutionTimes executionTimes = new ExecutionTimes();

    public MonitoringStatistics(String name) {
        this.name = name;
        this.date = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public class AddCounts {
        private int hosts = 0;
        private int services = 0;
        private int events = 0;

        public void reset() {
            hosts = 0;
            services = 0;
            events = 0;
        }

        public int getHosts() {
            return hosts;
        }

        public void setHosts(int hosts) {
            this.hosts = hosts;
        }

        public int getServices() {
            return services;
        }

        public void setServices(int services) {
            this.services = services;
        }

        public int getEvents() {
            return events;
        }

        public void setEvents(int events) {
            this.events = events;
        }
    }

    public class ModifyCounts {
        private int hosts = 0;
        private int services = 0;
        private int events = 0;
        private int hostNotifications = 0;
        private int serviceNotifications = 0;
        private int performance = 0;

        public void reset() {
            hosts = 0;
            services = 0;
            events = 0;
            hostNotifications = 0;
            serviceNotifications = 0;
            performance = 0;
        }

        public int getHosts() {
            return hosts;
        }

        public void setHosts(int hosts) {
            this.hosts = hosts;
        }

        public int getServices() {
            return services;
        }

        public void setServices(int services) {
            this.services = services;
        }

        public int getEvents() {
            return events;
        }

        public void setEvents(int events) {
            this.events = events;
        }

        public int getHostNotifications() {
            return hostNotifications;
        }

        public void setHostNotifications(int hostNotifications) {
            this.hostNotifications = hostNotifications;
        }

        public int getServiceNotifications() {
            return serviceNotifications;
        }

        public void setServiceNotifications(int serviceNotifications) {
            this.serviceNotifications = serviceNotifications;
        }

        public int getPerformance() {
            return performance;
        }

        public void setPerformance(int performance) {
            this.performance = performance;
        }
    }

    public class HostQueries {
        private int hosts = 0;
        private int hostStatuses = 0;
        private int hostStatusProperty = 0;
        private int hostStatusProperty1 = 0;
        private int hostStatusProperty2 = 0;
        private int hostStatusProperty3 = 0;
        private int hostGroups = 0;

        public void reset() {
            hosts = 0;
            hostStatuses = 0;
            hostStatusProperty = 0;
            hostStatusProperty1 = 0;
            hostStatusProperty2 = 0;
            hostStatusProperty3 = 0;
            hostGroups = 0;
        }

        public int getHosts() {
            return hosts;
        }

        public void setHosts(int hosts) {
            this.hosts = hosts;
        }

        public int getHostStatuses() {
            return hostStatuses;
        }

        public void setHostStatuses(int hostStatuses) {
            this.hostStatuses = hostStatuses;
        }

        public int getHostStatusProperty() {
            return hostStatusProperty;
        }

        public void setHostStatusProperty(int hostStatusProperty) {
            this.hostStatusProperty = hostStatusProperty;
        }

        public int getHostStatusProperty1() {
            return hostStatusProperty1;
        }

        public void setHostStatusProperty1(int hostStatusProperty1) {
            this.hostStatusProperty1 = hostStatusProperty1;
        }

        public int getHostStatusProperty2() {
            return hostStatusProperty2;
        }

        public void setHostStatusProperty2(int hostStatusProperty2) {
            this.hostStatusProperty2 = hostStatusProperty2;
        }

        public int getHostStatusProperty3() {
            return hostStatusProperty3;
        }

        public void setHostStatusProperty3(int hostStatusProperty3) {
            this.hostStatusProperty3 = hostStatusProperty3;
        }

        public int getHostGroups() {
            return hostGroups;
        }

        public void setHostGroups(int hostGroups) {
            this.hostGroups = hostGroups;
        }
    }

    public class ServiceQueries {
        private int services = 0;
        private int servicesCPU = 0; // syn.host.cpu.used
        private int servicesFreeSpace = 0; // summary.freespace
        private int servicesCPUToMax = 0; // syn.vm.cpu.cpuToMax.used
        private int servicesSwappedMemSize = 0; // syn.vm.mem.swappedToConfigMemSize.use
        private int serviceStatusProperty = 0;
        private int serviceStatusProperty1 = 0;
        private int serviceStatusProperty53 = 0;

        public void reset() {
            services = 0;
            servicesCPU = 0; // syn.host.cpu.used
            servicesFreeSpace = 0; // summary.freespace
            servicesCPUToMax = 0; // syn.vm.cpu.cpuToMax.used
            servicesSwappedMemSize = 0; // syn.vm.mem.swappedToConfigMemSize.use
            serviceStatusProperty = 0;
            serviceStatusProperty1 = 0;
            serviceStatusProperty53 = 0;
        }

        public int getServices() {
            return services;
        }

        public void setServices(int services) {
            this.services = services;
        }

        public int getServicesCPU() {
            return servicesCPU;
        }

        public void setServicesCPU(int servicesCPU) {
            this.servicesCPU = servicesCPU;
        }

        public int getServicesFreeSpace() {
            return servicesFreeSpace;
        }

        public void setServicesFreeSpace(int servicesFreeSpace) {
            this.servicesFreeSpace = servicesFreeSpace;
        }

        public int getServicesCPUToMax() {
            return servicesCPUToMax;
        }

        public void setServicesCPUToMax(int servicesCPUToMax) {
            this.servicesCPUToMax = servicesCPUToMax;
        }

        public int getServicesSwappedMemSize() {
            return servicesSwappedMemSize;
        }

        public void setServicesSwappedMemSize(int servicesSwappedMemSize) {
            this.servicesSwappedMemSize = servicesSwappedMemSize;
        }

        public int getServiceStatusProperty() {
            return serviceStatusProperty;
        }

        public void setServiceStatusProperty(int serviceStatusProperty) {
            this.serviceStatusProperty = serviceStatusProperty;
        }

        public int getServiceStatusProperty1() {
            return serviceStatusProperty1;
        }

        public void setServiceStatusProperty1(int serviceStatusProperty1) {
            this.serviceStatusProperty1 = serviceStatusProperty1;
        }

        public int getServiceStatusProperty53() {
            return serviceStatusProperty53;
        }

        public void setServiceStatusProperty53(int serviceStatusProperty53) {
            this.serviceStatusProperty53 = serviceStatusProperty53;
        }

    }

    public class EventQueries {
        private int events = 0;
        private int hostEvents = 0;
        private int serviceEvents = 0;
        private int setupEvents = 0;

        public void reset() {
            events = 0;
            hostEvents = 0;
            serviceEvents = 0;
            setupEvents = 0;
        }

        public int getEvents() {
            return events;
        }

        public void setEvents(int events) {
            this.events = events;
        }

        public int getHostEvents() {
            return hostEvents;
        }

        public void setHostEvents(int hostEvents) {
            this.hostEvents = hostEvents;
        }

        public int getServiceEvents() {
            return serviceEvents;
        }

        public void setServiceEvents(int serviceEvents) {
            this.serviceEvents = serviceEvents;
        }

        public int getSetupEvents() {
            return setupEvents;
        }

        public void setSetupEvents(int setupEvents) {
            this.setupEvents = setupEvents;
        }
    }

    public class ExecutionTimes {
        // total run time counts in milliseconds
        private long monitorSync = 0;
        private long monitorUpdate = 0;
        private long inventorySync = 0;
        private long addHypervisors = 0;
        private long addVMs = 0;
        private long modifyHypervisors = 0;
        private long modifyVMs = 0;

        public void reset() {
            monitorSync = 0;
            monitorUpdate = 0;
            inventorySync = 0;
            long addHypervisors = 0;
            long addVMs = 0;
            long modifyHypervisors = 0;
            long modifyVMs = 0;
        }

        public long getMonitorSync() {
            return monitorSync;
        }

        public void setMonitorSync(long monitorSync) {
            this.monitorSync = monitorSync;
        }

        public long getMonitorUpdate() {
            return monitorUpdate;
        }

        public void setMonitorUpdate(long monitorUpdate) {
            this.monitorUpdate = monitorUpdate;
        }

        public long getInventorySync() {
            return inventorySync;
        }

        public void setInventorySync(long inventorySync) {
            this.inventorySync = inventorySync;
        }

        public long getAddHypervisors() {
            return addHypervisors;
        }

        public void setAddHypervisors(long addHypervisors) {
            this.addHypervisors = addHypervisors;
        }

        public long getAddVMs() {
            return addVMs;
        }

        public void setAddVMs(long addVMs) {
            this.addVMs = addVMs;
        }

        public long getModifyHypervisors() {
            return modifyHypervisors;
        }

        public void setModifyHypervisors(long modifyHypervisors) {
            this.modifyHypervisors = modifyHypervisors;
        }

        public long getModifyVMs() {
            return modifyVMs;
        }

        public void setModifyVMs(long modifyVMs) {
            this.modifyVMs = modifyVMs;
        }
    }

    public AddCounts getAddsHypervisors() {
        return addsHypervisors;
    }

    public AddCounts getAddsVMs() {
        return addsVMs;
    }

    public ModifyCounts getModsHypervisors() {
        return modsHypervisors;
    }

    public ModifyCounts getModsVMs() {
        return modsVMs;
    }

    public HostQueries getHostQueries() {
        return hostQueries;
    }

    public ServiceQueries getServiceQueries() {
        return serviceQueries;
    }

    public EventQueries getEventQueries() {
        return eventQueries;
    }

    public ExecutionTimes getExecutionTimes() {
        return executionTimes;
    }

    public void reset() {
        addsHypervisors.reset();
        addsVMs.reset();
        modsHypervisors.reset();
        modsVMs.reset();
        hostQueries.reset();
        serviceQueries.reset();
        eventQueries.reset();
        executionTimes.reset();
    }
}
