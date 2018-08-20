package com.groundwork.dashboard.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashboardConfigurationServiceImpl implements DashboardConfigurationService {

    private static Logger log = Logger.getLogger(DashboardConfigurationServiceImpl.class);

    public static final String CONFIG_FILE_EXTN       = ".xml";
    public static final String CONFIG_FILE_PATH       = "/usr/local/groundwork/config/dashboards/";
    public static final String CONFIG_DEPLOY_PATH     = "/usr/local/groundwork/config/dashboards/deploy/";
    protected static final String CONFIG_FILE_PATTERN = "^[a-zA-Z0-9-_]+\\.xml";

    protected Pattern configFilePattern = Pattern.compile(CONFIG_FILE_PATTERN);

    public DashboardConfigurationServiceImpl() {
        createConfigurationDirectories();
    }
    
    @Override
    public List<DashboardConfiguration> list() throws DashboardConfigurationException {
        List<DashboardConfiguration> configurations = new LinkedList<DashboardConfiguration>();
        File dir = new File(CONFIG_FILE_PATH);
        if (!dir.exists()) {
            throw new DashboardConfigurationException("Failed to create configuration directory: " + CONFIG_FILE_PATH);
        }
        File[] children = dir.listFiles();
        for (File file : children) {
            if (file.isFile()) {
                Matcher matcher = configFilePattern.matcher(file.getName());
                if (matcher.matches()) {
                    try {
                        DashboardConfiguration configuration = readInternal(file.getPath());
                        if (configuration != null) {
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

    protected String buildFilePath(String name) {
        return (name.endsWith(CONFIG_FILE_EXTN)) ? CONFIG_FILE_PATH + name : CONFIG_FILE_PATH + name + CONFIG_FILE_EXTN;
        }

    @Override
    public DashboardConfiguration read(String name) throws DashboardConfigurationException {
        String path = buildFilePath(name);
        return readInternal(path);
    }

    public synchronized DashboardConfiguration readInternal(String path) throws DashboardConfigurationException {
        DashboardConfiguration configuration = null;
        try {
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(DashboardConfiguration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            configuration = (DashboardConfiguration) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new DashboardConfigurationException("Failed to read config at " + path + ": " + e, e);
        }
        return configuration;
    }

    @Override
    public boolean exists(String name) throws DashboardConfigurationException {
        try {
            String path = buildFilePath(name);
            File file = new File(path);
            return file.exists();
        } catch (Exception e) {
            throw new DashboardConfigurationException("Failed to access check configuration: " + name, e);
        }
    }

    @Override
    public boolean remove(String name) throws DashboardConfigurationException {
        try {
            String path = buildFilePath(name);
            File file = new File(path);
            return file.delete();
        } catch (Exception e) {
            throw new DashboardConfigurationException("Failed to remove configuration: " + name, e);
        }
    }

    @Override
    public DashboardConfiguration save(DashboardConfiguration configuration) throws DashboardConfigurationException {
        try {
            String path = buildFilePath(configuration.getName());
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(DashboardConfiguration.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(configuration, file);
            return configuration;

        } catch (Exception e) {
            throw new DashboardConfigurationException("Failed to save configuration: " +  configuration.getName(), e);
        }
    }

    protected synchronized void createConfigurationDirectories() throws DashboardConfigurationException {
        try {
            File dir = new File(CONFIG_FILE_PATH);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new DashboardConfigurationException("Failed to create configuration directory: " + CONFIG_FILE_PATH);
                }
            }
        }
        catch (SecurityException e) {
            throw new DashboardConfigurationException("Failed to access configuration directory: " + CONFIG_FILE_PATH, e);
        }
        try {
            File dir = new File(CONFIG_DEPLOY_PATH);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new DashboardConfigurationException("Failed to create configuration deploy directory: " + CONFIG_DEPLOY_PATH);
                }
            }
        }
        catch (SecurityException e) {
            throw new DashboardConfigurationException("Failed to access configuration deploy directory: " + CONFIG_DEPLOY_PATH, e);
        }
    }

    public DashboardConfiguration copy(DashboardConfiguration original) throws DashboardConfigurationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DashboardConfiguration deepCopy = objectMapper.readValue(objectMapper.writeValueAsString(original), DashboardConfiguration.class);
            return deepCopy;
        }
        catch (Exception e) {
            throw new DashboardConfigurationException(e);
        }
    }


}
