package org.groundwork.rs.integration;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.groundwork.rs.client.CategoryClient;
import org.groundwork.rs.client.DeviceClient;
import org.groundwork.rs.client.EventClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.common.ConfiguredObjectMapper;
import org.groundwork.rs.dto.DtoCategory;
import org.groundwork.rs.dto.DtoCategoryList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoDeviceList;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegrationDataTool {

    public static final String DEFAULT_DEPLOYMENT_URL = "http://localhost:8080/foundation-webapp/api";

    public static final String HOSTS_FILE = "./src/test/data/hosts.json";
    public static final String HOSTGROUPS_FILE = "./src/test/data/hostgroups.json";
    public static final String SERVICES_FILE = "./src/test/data/services.json";
    public static final String DEVICES_FILE = "./src/test/data/devices.json";
    public static final String CATEGORIES_FILE = "./src/test/data/categories.json";
    public static final String EVENTS_FILE = "./src/test/data/events.json";

    public static final String SYSTEM_PARAM_GWOS_REST_API = "GWOS_REST_API";
    public static final String SYSTEM_PARAM_GWOS_REST_USER = "GWOS_REST_USER";
    public static final String SYSTEM_PARAM_GWOS_REST_PW = "GWOS_REST_PW";
    public static final String SYSTEM_PARAM_GWOS_REST_APP = "GWOS_REST_APP";
    public static final String PARAM_ACTION = "-action";
    public static final String PARAM_CREATE_DATA_FILES = "createDataFiles";
    public static final String PARAM_IMPORT_DATABASE = "importDatabase";
    public static final String PARAM_TEAR_DOWN = "tearDown";

    private ObjectMapper mapper;
    private HostClient hostClient;
    private HostGroupClient hostGroupClient;
    private ServiceClient serviceClient;
    private DeviceClient deviceClient;
    private CategoryClient categoryClient;
    private EventClient eventClient;

    public static void main(String[] args) throws IOException {
        String url = System.getProperty(SYSTEM_PARAM_GWOS_REST_API, DEFAULT_DEPLOYMENT_URL);
        System.out.println("**** Integration Data Tool starting with URL " + url + "... ");
        if (args.length == 2 && args[0].equalsIgnoreCase(PARAM_ACTION)) {
            if (args[1].equalsIgnoreCase(PARAM_CREATE_DATA_FILES)) {
                System.out.println("**** Creating data files ... ");
                IntegrationDataTool dataTool = new IntegrationDataTool(url);
                dataTool.createHostsDataFile();
                dataTool.createHostGroupsDataFile();
                dataTool.createServicesDataFile();
                dataTool.createDevicesDataFile();
                dataTool.createCategoriesDataFile();
                dataTool.createEventsDataFile();
                System.out.println("**** ... Creating data files completed.");
                return;
            } else if (args[1].equalsIgnoreCase(PARAM_IMPORT_DATABASE)) {
                System.out.println("**** Importing database ... ");
                IntegrationDataTool dataTool = new IntegrationDataTool(url);
                dataTool.populateHosts();
                dataTool.populateHostGroups();
                dataTool.populateServices();
                dataTool.populateDevices();
                dataTool.populateCategories();
                dataTool.populateEvents();
                System.out.println("**** ... Importing database completed.");

                return;
            } else if (args[1].equalsIgnoreCase(PARAM_TEAR_DOWN)) {
                System.out.println("**** Tearing down database ... ");
                IntegrationDataTool dataTool = new IntegrationDataTool(url);
                dataTool.tearDownHosts();
                dataTool.tearDownHostGroups();
                dataTool.tearDownServices();
                dataTool.tearDownDevices();
                dataTool.tearDownCategories();
                dataTool.tearDownEvents();
                System.out.println("**** ... Tearing down database completed.");
                return;
            }
        }
        usage();
        System.exit(1);
    }

    public IntegrationDataTool(String deploymentUrl) {
        this.mapper = createMapper();
        this.hostClient = new HostClient(deploymentUrl);
        this.hostGroupClient = new HostGroupClient(deploymentUrl);
        this.serviceClient = new ServiceClient(deploymentUrl);
        this.deviceClient = new DeviceClient(deploymentUrl);
        this.categoryClient = new CategoryClient(deploymentUrl);
        this.eventClient = new EventClient(deploymentUrl);
    }

    /*
        Data File Creation APIs - use these to read from 7.0.2 test data and write to text files
     */

    public void createHostsDataFile() throws IOException {
        File file = new File(HOSTS_FILE);
        file.delete();
        mapper.writeValue(file, hostClient.list());
    }

    public void createHostGroupsDataFile() throws IOException {
        File file = new File(HOSTGROUPS_FILE);
        file.delete();
        mapper.writeValue(file, hostGroupClient.list());
    }

    public void createServicesDataFile() throws IOException {
        File file = new File(SERVICES_FILE);
        file.delete();
        mapper.writeValue(file, serviceClient.list());
    }

    public void createDevicesDataFile() throws IOException {
        File file = new File(DEVICES_FILE);
        file.delete();
        mapper.writeValue(file, deviceClient.list());
    }

    public void createCategoriesDataFile() throws IOException {
        File file = new File(CATEGORIES_FILE);
        file.delete();
        mapper.writeValue(file, categoryClient.list(DtoDepthType.Deep));
    }

    public void createEventsDataFile() throws IOException {
        File file = new File(EVENTS_FILE);
        file.delete();
        mapper.writeValue(file, eventClient.list());

    }

    /*
        Population APIs - use these to write to 7.1.0 Integration Test DB
     */

    public void populateHosts() throws IOException {
        List<DtoHost> hosts = mapper.readValue(new File(HOSTS_FILE), new TypeReference<List<DtoHost>>() {});
        DtoHostList hostUpdates = new DtoHostList();
        for (DtoHost host : hosts) {
            hostUpdates.add(host);
        }
        hostClient.post(hostUpdates);
    }

    public void populateHostGroups() throws IOException {
        List<DtoHostGroup> hostGroups = mapper.readValue(new File(HOSTGROUPS_FILE), new TypeReference<List<DtoHostGroup>>() {});
        DtoHostGroupList hostGroupUpdates = new DtoHostGroupList();
        for (DtoHostGroup hostGroup : hostGroups) {
            hostGroupUpdates.add(hostGroup);
        }
        hostGroupClient.post(hostGroupUpdates);
    }

    public void populateServices() throws IOException {
        List<DtoService> services = mapper.readValue(new File(SERVICES_FILE), new TypeReference<List<DtoService>>() {});
        DtoServiceList serviceUpdates = new DtoServiceList();
        for (DtoService service : services) {
            serviceUpdates.add(service);
        }
        serviceClient.post(serviceUpdates);
    }

    public void populateDevices() throws IOException {
        List<DtoDevice> devices = mapper.readValue(new File(DEVICES_FILE), new TypeReference<List<DtoDevice>>() {});
        DtoDeviceList deviceUpdates = new DtoDeviceList();
        for (DtoDevice device : devices) {
            deviceUpdates.add(device);
        }
        deviceClient.post(deviceUpdates);
    }

    public void populateCategories() throws IOException {
        List<DtoCategory> categories = mapper.readValue(new File(CATEGORIES_FILE), new TypeReference<List<DtoCategory>>() {});
        DtoCategoryList categoryUpdates = new DtoCategoryList();
        for (DtoCategory category : categories) {
            categoryUpdates.add(category);
        }
        categoryClient.post(categoryUpdates);
    }

    public void populateEvents() throws IOException {
        List<DtoEvent> events = mapper.readValue(new File(EVENTS_FILE), new TypeReference<List<DtoEvent>>() {});
        DtoEventList eventUpdates = new DtoEventList();
        for (DtoEvent event : events) {
            if (!filterHost(event.getHost()))
                eventUpdates.add(event);
        }
        eventClient.post(eventUpdates);
    }

    private boolean filterHost(String hostName) {
        if (hostName == null)
            return true;
        if (hostName.trim().isEmpty())
            return true;
        if (hostName.equals("localhost"))
            return true;
        if (hostName.equals("bsm-host"))
            return true;
        return false;
    }

    private boolean filterHostGroup(String hostName) {
        if (hostName == null)
            return true;
        if (hostName.trim().isEmpty())
            return true;
        if (hostName.equals("Linux Servers"))
            return true;
        if (hostName.equals("BSM:Business Objects"))
            return true;
        return false;
    }

    private boolean filterDevice(String hostName) {
        if (hostName == null)
            return true;
        if (hostName.trim().isEmpty())
            return true;
        if (hostName.equals("127.0.0.1"))
            return true;
        if (hostName.equals("bsm-host"))
            return true;
        return false;
    }

    private boolean filterHostAndService(String hostName, String serviceName, String monitorStatus) {
        if (hostName == null && serviceName == null) {
            if (monitorStatus == null || monitorStatus.equals("CRITICAL"))
                return false;
            return true;
        }
        if (hostName.trim().isEmpty())
            return true;
        if (hostName.equals("localhost"))
            return true;
        if (hostName.equals("bsm-host"))
            return true;
        return false;
    }

    /*
        Tear down APIs
     */
    public void tearDownHosts() throws IOException {
        List<DtoHost> hosts = mapper.readValue(new File(HOSTS_FILE), new TypeReference<List<DtoHost>>() {});
        List<String> hostNames = new ArrayList<String>();
        for (DtoHost host : hosts) {
            if (!filterHost(host.getHostName())) {
                hostNames.add(host.getHostName());
            }
        }
        hostClient.delete(hostNames);
    }

    public void tearDownHostGroups() throws IOException {
        List<DtoHostGroup> hostGroups = mapper.readValue(new File(HOSTGROUPS_FILE), new TypeReference<List<DtoHostGroup>>() {});
        List<String> hostGroupNames = new ArrayList<String>();
        for (DtoHostGroup hostGroup : hostGroups) {
            if (!filterHostGroup(hostGroup.getName())) {
                hostGroupNames.add(hostGroup.getName());
            }
        }
        hostGroupClient.delete(hostGroupNames);
    }

    public void tearDownServices() throws IOException {
        List<DtoService> services = mapper.readValue(new File(SERVICES_FILE), new TypeReference<List<DtoService>>() {});
        Map<String, HostServices> hostServicesMap = new HashMap<String, HostServices>();
        for (DtoService service : services) {
            if (!filterHost(service.getHostName())) {
                HostServices hostServices = hostServicesMap.get(service.getHostName());
                if (hostServices == null) {
                    hostServices = new HostServices(service.getHostName());
                    hostServicesMap.put(service.getHostName(), hostServices);
                }
                hostServices.serviceNames.add(service.getDescription());
            }
        }
        for (HostServices hostServices : hostServicesMap.values()) {
            serviceClient.delete(hostServices.serviceNames, hostServices.hostName);
        }
    }

    public void tearDownDevices() throws IOException {
        List<DtoDevice> devices = mapper.readValue(new File(DEVICES_FILE), new TypeReference<List<DtoDevice>>() {});
        List<String> deviceNames = new ArrayList<String>();
        for (DtoDevice device : devices) {
            if (!filterDevice(device.getIdentification())) {
                deviceNames.add(device.getIdentification());
            }
        }
        deviceClient.delete(deviceNames);
    }

    public void tearDownCategories() throws IOException {
        List<DtoCategory> categories = mapper.readValue(new File(CATEGORIES_FILE), new TypeReference<List<DtoCategory>>() {});
        DtoCategoryList deletes = new DtoCategoryList();
        for (DtoCategory category : categories) {
            DtoCategory delete = new DtoCategory();
            delete.setName(category.getName());
            delete.setEntityTypeName(category.getEntityTypeName());
            deletes.getCategories().add(category);
        }
        categoryClient.delete(deletes);
    }

    public void tearDownEvents() throws IOException {
        List<DtoEvent> events = eventClient.list();
        List<String> ids = new ArrayList<String>();
        for (DtoEvent event : events) {
            String monitorStatus = event.getMonitorStatus();
            if (monitorStatus != null && !monitorStatus.equals("PENDING")) {
                if (!filterHostAndService(event.getHost(), event.getService(), event.getMonitorStatus())) {
                    ids.add(event.getId().toString());
                }
            }
        }
        if (ids.size() > 0)
            eventClient.delete(ids);
    }

    /*
        Utilites
     */

    public class HostServices {
        public List<String> serviceNames;
        public String hostName;

        public HostServices(String hostName) {
            this.serviceNames = new ArrayList<String>();
            this.hostName = hostName;
        }
    }

    public ObjectMapper createMapper() {
        return new ConfiguredObjectMapper();
    }

    public static void usage() {
        System.out.println("\nRequired parameters: -action createDataFiles | importDatabase | tearDown");
    }

}
