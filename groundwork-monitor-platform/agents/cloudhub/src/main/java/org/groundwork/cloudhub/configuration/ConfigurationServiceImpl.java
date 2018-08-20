package org.groundwork.cloudhub.configuration;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.legacy.LegacyConfigurationReader;
import org.groundwork.cloudhub.configuration.legacy.LegacyGwosConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(ConfigurationService.NAME)
public class ConfigurationServiceImpl implements ConfigurationService {

    private static Logger log = Logger.getLogger(ConfigurationServiceImpl.class);

    public static final String CONFIG_FILE_EXTN       = ".xml";
    public static final String CONFIG_FILE_PATH       = "/usr/local/groundwork/config/cloudhub/";
    public static final String CONFIG_DEPLOY_PATH     = "/usr/local/groundwork/config/cloudhub/deploy/";
    public static final String CONFIG_FILE_BASE_NAME  = "cloudhub-%s-%d";
    protected static final String CONFIG_FILE_PATTERN = "([a-z]+)-([a-z][a-z0-9]*)-([0-9]+)\\.([a-z]{3})";

    protected Pattern configFilePattern = Pattern.compile(CONFIG_FILE_PATTERN);

    @Resource(name = ConnectorFactory.NAME)
    private ConnectorFactory connectorFactory;

    public ConfigurationServiceImpl() {
        createConfigurationDirectories();
    }

    @Override
    public ConnectionConfiguration createConfiguration(VirtualSystem virtualSystem) throws CloudHubException {
        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(virtualSystem);
        if (provider != null) {
            return provider.createConfiguration();
        }
        throw new CloudHubException("Unsupported Virtual System: " + virtualSystem);
    }

    @Override
    public synchronized ConnectionConfiguration saveConfiguration(ConnectionConfiguration configuration) throws CloudHubException {
        String fullPath = "";
        try {
            if (StringUtils.isEmpty(configuration.getCommon().getPathToConfigurationFile()))
                configuration.getCommon().setPathToConfigurationFile(CONFIG_FILE_PATH);
            if (StringUtils.isEmpty(configuration.getCommon().getConfigurationFile()))
                configuration.getCommon().setConfigurationFile(calculateNextFileName(configuration));
            fullPath = configuration.getCommon().getPathToConfigurationFile() + configuration.getCommon().getConfigurationFile();
            File file = new File(fullPath);
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
            if (provider == null) {
                throw new CloudHubException("Unsupported Virtual System: " + configuration.getCommon().getVirtualSystem());
            }
            Class clazz = provider.getImplementingClass();
            String password = provider.encryptPassword(configuration);
            JAXBContext context = JAXBContext.newInstance(clazz);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(configuration, file);
            provider.decryptPassword(configuration); // reset the password in memory
            return configuration;
        } catch (JAXBException e) {
            throw new CloudHubException("Failed to save configuration at " + fullPath + ": " + e, e);
        }
    }

    @Override
    public synchronized void deleteConfiguration(ConnectionConfiguration configuration) throws CloudHubException {
        String path = configuration.getCommon().getPathToConfigurationFile();
        if (path == null) {
            throw new CloudHubException("File to delete has does not have path set");
        }
        String fileName = configuration.getCommon().getConfigurationFile();
        if (fileName == null) {
            throw new CloudHubException("File to delete has does not have file name set");
        }
        StringBuilder filePath = new StringBuilder();
        filePath.append(path);
        if (!path.endsWith("/"))
            filePath.append("/");
        filePath.append(fileName);
        try {
            File configFile = new File(filePath.toString());
            configFile.delete();
        }
        catch (Exception e) {
            throw new CloudHubException("Failed to delete config: " + filePath.toString(), e);
        }
    }

    @Override
    public List<ConnectionConfiguration> listAllConfigurations() throws CloudHubException {
        return listConfigurations(null);
    }

    @Override
     public List<ConnectionConfiguration> listConfigurations(VirtualSystem virtualSystem) throws CloudHubException {
        List<ConnectionConfiguration> configurations = new LinkedList<ConnectionConfiguration>();
        File dir = new File(CONFIG_FILE_PATH);
        if (!dir.exists()) {
            throw new CloudHubException("Failed to create configuration directory: " + CONFIG_FILE_PATH);
        }
        File[] children = dir.listFiles();
        for (File file : children) {
            if (file.isFile()) {
                Matcher matcher = configFilePattern.matcher(file.getName());
                if (matcher.matches()) {
                    try {
                        ConnectionConfiguration configuration = readConfiguration(file.getPath());
                        if (configuration != null) {
                            if (virtualSystem == null ||
                               (virtualSystem != null && virtualSystem.equals(configuration.getCommon().getVirtualSystem())))
                                configurations.add(configuration);
                        }
                    }
                    catch (Exception e) {
                        log.error("Failed to load configuration " + file.getName(), e);
                    }
                }
            }
        }
        return configurations;
    }

    @Override
    public synchronized ConnectionConfiguration refreshConfiguration(ConnectionConfiguration configuration) throws CloudHubException {
        String path = configuration.getCommon().getPathToConfigurationFile() + configuration.getCommon().getConfigurationFile();
        return readConfiguration(path);
    }

    @Override
    public synchronized ConnectionConfiguration readConfiguration(String path) throws CloudHubException {
        ConnectionConfiguration configuration = null;
        try {
            VirtualSystem virtualSystem = extractVirtualSystemFromFilename(path);
            if (virtualSystem == null) {
                throw new CloudHubException("Unsupported Virtual System from path: " + path);
            }
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(virtualSystem);
            if (provider == null) {
                throw new CloudHubException("Unsupported Virtual System: " + virtualSystem);
            }
            Class clazz = provider.getImplementingClass();
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            configuration = (ConnectionConfiguration) unmarshaller.unmarshal(file);
            if (configuration instanceof ClouderaConfiguration) {
                ClouderaConfiguration cc = (ClouderaConfiguration)configuration;
                cc.getConnection().setPrefixServiceNames(cc.getCommon().getPrefixServiceNames());
            }
            provider.decryptPassword(configuration);
            provider.migrateConfiguration(configuration);
        } catch (JAXBException e) {
            throw new CloudHubException("Failed to read config at " + path + ": " + e, e);
        }
        return configuration;
    }

    @Override
    public boolean doesConfigurationExist(String path) throws CloudHubException {
        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception e) {
            throw new CloudHubException("Failed to access check configuration: " + path, e);
        }
    }

    protected synchronized void createConfigurationDirectories() throws CloudHubException {
        try {
            File dir = new File(CONFIG_FILE_PATH);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new CloudHubException("Failed to create configuration directory: " + CONFIG_FILE_PATH);
                }
            }
        }
        catch (SecurityException e) {
            throw new CloudHubException("Failed to access configuration directory: " + CONFIG_FILE_PATH, e);
        }
        try {
            File dir = new File(CONFIG_DEPLOY_PATH);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new CloudHubException("Failed to create configuration deploy directory: " + CONFIG_DEPLOY_PATH);
                }
            }
        }
        catch (SecurityException e) {
            throw new CloudHubException("Failed to access configuration deploy directory: " + CONFIG_DEPLOY_PATH, e);
        }
    }

    protected String calculateNextFileName(ConnectionConfiguration configuration) throws CloudHubException {
        int last = 0;
        File dir = new File(CONFIG_FILE_PATH);
        if (!dir.exists()) {
            throw new CloudHubException("Failed to create configuration directory: " + CONFIG_FILE_PATH);
        }
        File[] children = dir.listFiles();
        for (File file : children) {
            if (file.isFile()) {
                int number = extractConfigNumberFromFilename(file.getName());
                last = (number > last) ? number : last;
            }
        }
        String base = String.format(CONFIG_FILE_BASE_NAME, configuration.getCommon().getVirtualSystem().toString().toLowerCase(), last + 1);
        return base + CONFIG_FILE_EXTN;
    }

    protected int extractConfigNumberFromFilename(String fileName) {
        Matcher matcher = configFilePattern.matcher(fileName);
        if (matcher.matches())
        {
            if (matcher.groupCount() == 4) {
                String number = matcher.group(3);
                try {
                    return Integer.parseInt(number);
                }
                catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public VirtualSystem extractVirtualSystemFromFilename(String fullPath) {
        // Java7: Path path = FileSystems.getDefault().getPath(fullPath);
        File path = new File(fullPath);
        Matcher matcher = configFilePattern.matcher(path.getName());
        if (matcher.matches())
        {
            if (matcher.groupCount() == 4) {
                String vs = matcher.group(2);
                try {
                    return VirtualSystem.valueOf(vs.toUpperCase());
                }
                catch (Exception e) {}
            }
        }
        return null;
    }

    @Override
    public ConnectionConfiguration convertLegacyConfiguration(VirtualSystem virtualSystem) throws CloudHubException {
        LegacyGwosConfiguration oldConfig = LegacyConfigurationReader.readLegacyConfiguration(virtualSystem);
        ConnectionConfiguration newConfig = null;
        if (oldConfig != null) {
            // convert legacy to new, save it, and then delete it
            switch (virtualSystem) {
                case VMWARE:
                    newConfig = convertVmWare(oldConfig);
                    saveConfiguration(newConfig);
                    break;
                case REDHAT:
                    newConfig = convertRedhat(oldConfig);
                    saveConfiguration(newConfig);
                    break;
                case OPENSTACK:
                case OPENSHIFT:
                case DOCKER:
                    // NOT SUPPORTED
                    break;
                case CISCO:
                case NSX:
                case OPENDAYLIGHT:
                    // NOT SUPPORTED
                    break;
            }
            LegacyConfigurationReader.deleteLegacyConfiguration(virtualSystem);
        }
        return newConfig;
    }

    @Override
    public String generateAgentId() {
        return UUID.randomUUID().toString();
    }

    @PostConstruct
    private void start() {
        log.debug("Starting up configuration service");
        convertLegacyConfiguration(VirtualSystem.VMWARE);
        convertLegacyConfiguration(VirtualSystem.REDHAT);
    }

    private VmwareConfiguration convertVmWare(LegacyGwosConfiguration legacy) {
        VmwareConfiguration vmware = new VmwareConfiguration();
        convertGwos(legacy, vmware.getGwos());
        vmware.getConnection().setUsername(legacy.getWsUser());
        convertCommon(legacy, vmware.getCommon());
        vmware.getConnection().setUri(legacy.getVirtualEnvURI());
        vmware.getConnection().setUrl(legacy.getVirtualEnvURL());
        vmware.getConnection().setUsername(legacy.getVirtualEnvUser());
        vmware.getConnection().setPassword(legacy.getVirtualEnvPassword());
        vmware.getConnection().setSslEnabled(legacy.isVirtualEnvSSLEnabled());
        vmware.getConnection().setServer(legacy.getVirtualEnvServer());
        return vmware;
    }

    private RedhatConfiguration convertRedhat(LegacyGwosConfiguration legacy) {
        RedhatConfiguration redhat = new RedhatConfiguration();
        convertGwos(legacy, redhat.getGwos());
        convertCommon(legacy, redhat.getCommon());
        redhat.getConnection().setUri(legacy.getVirtualEnvURI());
        redhat.getConnection().setUrl(legacy.getVirtualEnvURL());
        redhat.getConnection().setRealm(legacy.getVirtualEnvRealm());
        redhat.getConnection().setUsername(legacy.getVirtualEnvUser());
        redhat.getConnection().setPassword(legacy.getVirtualEnvPassword());
        redhat.getConnection().setPort(legacy.getVirtualEnvPort());
        redhat.getConnection().setProtocol(legacy.getVirtualEnvProtocol());
        redhat.getConnection().setSslEnabled(legacy.isVirtualEnvSSLEnabled());
        redhat.getConnection().setCertificateStore(legacy.getCertificateStore());
        redhat.getConnection().setCertificatePassword(legacy.getCertificatePassword());
        redhat.getConnection().setServer(legacy.getVirtualEnvServer());
        return redhat;
    }

    private GWOSConfiguration convertGwos(LegacyGwosConfiguration legacy, GWOSConfiguration gwos) {
        gwos.setGwosPort(legacy.getGwosPort());
        gwos.setGwosServer(legacy.getGwosServer());
        gwos.setGwosSSLEnabled(legacy.isGwosSSLEnabled());
        gwos.setGwosVersion(GWOSConfiguration.DEFAULT_VERSION);
        gwos.setWsEndPoint(legacy.getWsEndpoint());
        gwos.setWsHostName(legacy.getWsHostName());
        gwos.setWsHostGroupName(legacy.getWsHostGroupName());
        gwos.setWsUsername(legacy.getWsUser());
        gwos.setWsPassword(legacy.getWsPassword());
        gwos.setMergeHosts(GWOSConfiguration.DEFAULT_MERGE_HOSTS);
        return gwos;
    }

    private CommonConfiguration convertCommon(LegacyGwosConfiguration legacy, CommonConfiguration common) {
        common.setDisplayName(common.getVirtualSystem().name() + " - 6.7 conversion");
        common.setCheckIntervalMinutes(legacy.getCheckInterval());
        common.setSyncIntervalMinutes(legacy.getSyncInterval());
        common.setComaIntervalMinutes(legacy.getComaInterval());
        return common;
    }

    public boolean matchConfigurationFileName(String fileName) {
        Matcher matcher = configFilePattern.matcher(fileName);
        return matcher.matches();
    }

    @Override
    public int countByHostName(String hostName) {
        int count = 0;
        for (ConnectionConfiguration config : listAllConfigurations()) {
            if (config.getGwos().getMonitor() &&        config.getGwos().getGwosServer() != null && config.getGwos().getGwosServer().equals(hostName)) {
                count = count + 1;
            }
        }
        return count;
    }

}

