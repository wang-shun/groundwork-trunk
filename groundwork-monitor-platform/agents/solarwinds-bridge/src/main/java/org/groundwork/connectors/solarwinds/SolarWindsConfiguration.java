package org.groundwork.connectors.solarwinds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.connectors.solarwinds.status.MonitorStatus;
import org.groundwork.foundation.ws.impl.ConfigurationWatcher;
import org.groundwork.foundation.ws.impl.ConfigurationWatcherNotificationListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SolarWindsConfiguration implements ConfigurationWatcherNotificationListener {

    private static Log log = LogFactory.getLog(SolarWindsConfiguration.class);

    public static final String SOLAR_WIND_CONFIG_FILE = "/usr/local/groundwork/config/solarwinds_bridge.properties";
    public static final String DEFAULT_REST_API_ENDPOINT = "http://localhost:8080/foundation-webapp/api";

    // Configuration Properties
    private boolean isAuditMode = false;
    private Set<String> validAgents = new HashSet<>();
    private String unknownHost = "SW_Unknown_Host";
    private String unknownService = "SW_Unknown_Service";
    private boolean isProcessUnknownHosts = true;
    private boolean isProcessUnknownServices = true;
    private boolean isNotificationsEnabled = false;
    private String bridgeService = "Bridge_Status";
    private String solarWindsService = "Solarwinds_Status";
    private String defaultHostGroup = "Solarwinds";
    private boolean isAddToDefaultHostGroup = true;
    private boolean isStatusSuffix = true;
    private boolean useSolarWindsTimestamps = true;
    private String restApiEndpoint = DEFAULT_REST_API_ENDPOINT;
    private String bridgeDevice = "localhost";
    private String appType = "Solarwinds";
    private String appTypeDescription = "Solarwinds Application";
    private String appTypeCriteria = "Device;Host;ServiceDescription";
    private List<String> appTypeProperties = new LinkedList<>();
    private List<String> appTypeEntities = new LinkedList<>();

    private int bridgeHeartbeat = 300; // seconds
    private int pingAPIRetries = 30; // -1 is infinite
    private int pingAPISleep = 30; // seconds

    Map<String,String> statusMap = new HashMap<String,String>();

    private static SolarWindsConfiguration singleton = null;
    private Properties configuration = null;
    private String watchedFileName = null;

    // Public Static Interface
    public synchronized static final SolarWindsConfiguration instance() {
        if (singleton == null) {
            singleton = new SolarWindsConfiguration();
            singleton.initConfiguration();
        }
        return singleton;
    }

    private SolarWindsConfiguration() {
    }

    private synchronized void initConfiguration() {
        String configFile = reloadConfiguration();
        String filePath = Paths.get(configFile).toString();
        watchedFileName = Paths.get(configFile).getFileName().toString();
        ConfigurationWatcher.registerListener(this, filePath);
    }

    private synchronized String reloadConfiguration() {
        configuration = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(SOLAR_WIND_CONFIG_FILE);
            configuration.load(fis);
            refreshValues();
            if (log.isInfoEnabled()) {
                log.info("Solar Winds file loaded");
            }

        } catch (IOException e) {
            log.error(e);
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return SOLAR_WIND_CONFIG_FILE;
    }

    public void notifyChange(Path path) {
        if (watchedFileName != null) {
            if (watchedFileName.equals(path.toString())) {
                if (log.isInfoEnabled()) {
                    log.info("Received Notification of change on SolarWindsConfiguration file " + path.toString());
                }
                reloadConfiguration();
            }
        }
    }

    private void refreshValues() {
        this.isAuditMode = Boolean.parseBoolean(configuration.getProperty("Audit_Mode", "false"));
        String[] agentsList = configuration.getProperty("SW_Agents", "").split(", ");
        if (!agentsList[0].equals("")) {
            for (String agent : agentsList) {
                this.validAgents.add(agent);
            }
        }
        unknownHost = configuration.getProperty("Unknown_Host", unknownHost);
        unknownService = configuration.getProperty("Unknown_Service", unknownService);
        isProcessUnknownHosts = Boolean.parseBoolean(configuration.getProperty("Process_Unknown_Hosts"));
        isProcessUnknownServices = Boolean.parseBoolean(configuration.getProperty("Process_Unknown_Services"));
        isNotificationsEnabled = Boolean.parseBoolean(configuration.getProperty("Notifications_Enabled"));
        bridgeService = configuration.getProperty("Bridge_Service", bridgeService);
        solarWindsService = configuration.getProperty("Solarwinds_Service", solarWindsService);
        defaultHostGroup = configuration.getProperty("Default_Hostgroup", defaultHostGroup);
        isAddToDefaultHostGroup = Boolean.parseBoolean(configuration.getProperty("Add_All_to_Default_Hostgroup"));
        isStatusSuffix = Boolean.parseBoolean(configuration.getProperty("Add_Status_Suffix"));
        useSolarWindsTimestamps = Boolean.parseBoolean(configuration.getProperty("Use_SW_Timestamps"));
        restApiEndpoint = configuration.getProperty("Rest_API_Endpoint", DEFAULT_REST_API_ENDPOINT);
        bridgeDevice = configuration.getProperty("bridgeDevice", bridgeDevice);
        bridgeHeartbeat = Integer.parseInt(configuration.getProperty("Bridge_Heartbeat", Integer.toString(bridgeHeartbeat)));
        pingAPIRetries = Integer.parseInt(configuration.getProperty("Ping_API_Retries", Integer.toString(pingAPIRetries)));
        pingAPISleep = Integer.parseInt(configuration.getProperty("Ping_API_Sleep", Integer.toString(pingAPISleep)));
        // Application Type setup
        appType = configuration.getProperty("App_Type", appType);
        appTypeDescription = configuration.getProperty("App_Type_Description", appTypeDescription);
        appTypeCriteria = configuration.getProperty("App_Type_Criteria", appTypeCriteria);
        String[] list = configuration.getProperty("App_Type_Properties", "").split(", ");
        if (!list[0].equals("")) {
            for (String item : list) {
                this.appTypeProperties.add(item);
            }
        }
        list = configuration.getProperty("App_Type_Entities", "").split(", ");
        if (!list[0].equals("")) {
            for (String item : list) {
                this.appTypeEntities.add(item);
            }
        }

//        App_Type = Solarwinds
//        App_Type_Description = Solarwinds Application
//                App_Criteria = Device;Host;ServiceDescription
//                App_Type_Properties = LastPluginOutput, isAcknowledged
//        App_Type_Entities = LOG_MESSAGE, HOST_STATUS

        Set<String> propertyNames = configuration.stringPropertyNames();
        for (String name : propertyNames) {
            if (name.startsWith("SWS_")) {
                String value = configuration.getProperty(name);
                if (value != null) {
                    statusMap.put(name.substring("SWS_".length()).toUpperCase().replace('_', ' '), value.toUpperCase());
                }
            }
        }
    }

    // properties

    public boolean isAuditMode() {
        return isAuditMode;
    }

    public boolean isValidAgent(String agent) {
        if (validAgents.size() == 0)
            return true; // empty list allows all agents
        return validAgents.contains(agent);
    }

    public String getUnknownHost() {
        return unknownHost;
    }

    public String getUnknownService() {
        return unknownService;
    }

    public boolean isProcessUnknownHosts() {
        return isProcessUnknownHosts;
    }

    public String getDefaultHostGroup() {
        return defaultHostGroup;
    }

    public String getBridgeService() {
        return bridgeService;
    }

    public boolean isProcessUnknownServices() {
        return isProcessUnknownServices;
    }

    public boolean isNotificationsEnabled() {
        return isNotificationsEnabled;
    }

    public boolean isStatusSuffix() {
        return isStatusSuffix;
    }

    public boolean isAddToDefaultHostGroup() {
        return isAddToDefaultHostGroup;
    }

    public String getRestApiEndpoint() {
        return restApiEndpoint;
    }

    public String translateStatus(String propertyName) {
        String gwStatus = statusMap.get(propertyName.toUpperCase());
        return (gwStatus == null) ? MonitorStatus.UNKNOWN.name() : gwStatus;
    }

    public boolean isUseSolarWindsTimestamps() {
        return useSolarWindsTimestamps;
    }

    public String getBridgeDevice() {
        return bridgeDevice;
    }

    public String getSolarWindsService() {
        return solarWindsService;
    }

    public int getBridgeHeartbeat() {
        return bridgeHeartbeat;
    }

    public String getAppType() {
        return appType;
    }

    public String getAppTypeDescription() {
        return appTypeDescription;
    }

    public String getAppTypeCriteria() {
        return appTypeCriteria;
    }

    public List<String> getAppTypeProperties() {
        return appTypeProperties;
    }

    public List<String> getAppTypeEntities() {
        return appTypeEntities;
    }

    public int getPingAPIRetries() {
        return pingAPIRetries;
    }

    public int getPingAPISleep() {
        return pingAPISleep;
    }
}
