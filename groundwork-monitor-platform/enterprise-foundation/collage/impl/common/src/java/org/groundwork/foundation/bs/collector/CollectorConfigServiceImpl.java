/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.foundation.bs.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CollectorConfigServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class CollectorConfigServiceImpl implements CollectorConfigService {

    protected static Log log = LogFactory.getLog(CollectorConfigServiceImpl.class);

    private static final String CONNECTOR_CONFIG_DIRECTORY_PROPERTY_NAME = "collectors.configurations.directory";
    private static final String CONNECTOR_CONFIG_DIRECTORY_DEFAULT =
            "/usr/local/groundwork/config/collectors/configurations";
    private static final String CONNECTOR_CONFIG_TEMPLATE_DIRECTORY_PROPERTY_NAME = "collectors.templates.directory";
    private static final String CONNECTOR_CONFIG_TEMPLATE_DIRECTORY_DEFAULT =
            "/usr/local/groundwork/config/collectors/templates";

    private static final String YAML_EXTENSION = ".yaml";
    private static final String YAML_FILE_ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 8096;

    @Override
    public List<String> getCollectorConfigFileNames() {
        return listFileNames(getCollectorConfigDirectory());
    }

    @Override
    public String getCollectorConfig(String fileName) {
        if ((fileName == null) || !fileName.endsWith(YAML_EXTENSION)) {
            throw new IllegalArgumentException("YAML filename required");
        }
        return readFile(getCollectorConfigDirectory(), fileName);
    }

    @Override
    public boolean putCollectorConfig(String fileName, String content) {
        if ((fileName == null) || !fileName.endsWith(YAML_EXTENSION)) {
            throw new IllegalArgumentException("YAML filename required");
        }
        if ((content == null) || content.isEmpty() || !validateContent(content, false)) {
            throw new IllegalArgumentException("Valid YAML file content required");
        }
        return writeFile(getCollectorConfigDirectory(), fileName, content);
    }

    @Override
    public boolean deleteCollectorConfig(String fileName) {
        if ((fileName == null) || !fileName.endsWith(YAML_EXTENSION)) {
            throw new IllegalArgumentException("YAML filename required");
        }
        return deleteFile(getCollectorConfigDirectory(), fileName);
    }

    @Override
    public List<String> getCollectorConfigTemplateFileNames() {
        return listFileNames(getCollectorConfigTemplateDirectory());
    }

    @Override
    public String getCollectorConfigTemplate(String templateFileName) {
        if ((templateFileName == null) || !templateFileName.endsWith(YAML_EXTENSION)) {
            throw new IllegalArgumentException("YAML template filename required");
        }
        return readFile(getCollectorConfigTemplateDirectory(), templateFileName);
    }

    @Override
    public String findCollectorConfigTemplate(Map<String,String> identityProperties) {
        // validate identity properties
        if ((identityProperties == null) || identityProperties.isEmpty()) {
            throw new IllegalArgumentException("Identity properties required");
        }
        String identityAgentType = identityProperties.get(COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY);
        String identityPrefix = identityProperties.get(COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY);
        if ((identityAgentType == null) || identityAgentType.isEmpty()) {
            throw new IllegalArgumentException("Identity agent type property required");
        }
        // scan templates for best match
        List<String> templateFileNames = getCollectorConfigTemplateFileNames();
        if ((templateFileNames == null) || templateFileNames.isEmpty()) {
            return null;
        }
        String fileName = null;
        for (String templateFileName : templateFileNames) {
            String templateContent = getCollectorConfigTemplate(templateFileName);
            if ((templateContent != null) && !templateContent.isEmpty()) {
                // parse template
                String templateIdentityAgentType = null;
                String templateIdentityPrefix = null;
                try {
                    Map<String, Object> templateConfig = parseCollectorConfigContent(templateContent);
                    Map<String, Object> templateConfigIdentity =
                            (Map<String, Object>) templateConfig.get(COLLECTOR_CONFIG_IDENTITY_SECTION_KEY);
                    templateIdentityAgentType = (String) templateConfigIdentity.
                            get(COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY);
                    templateIdentityPrefix = (String) templateConfigIdentity.
                            get(COLLECTOR_CONFIG_IDENTITY_SECTION_PREFIX_KEY);
                } catch (Exception e) {
                }
                // match template
                if (identityAgentType.equalsIgnoreCase(templateIdentityAgentType)) {
                    if ((identityPrefix == null) || identityPrefix.isEmpty() ||
                            identityPrefix.equalsIgnoreCase(templateIdentityPrefix)) {
                        fileName = templateFileName;
                        break;
                    }
                    if (fileName == null) {
                        fileName = templateFileName;
                    }
                }
            }
        }
        return fileName;
    }

    @Override
    public boolean putCollectorConfigTemplate(String templateFileName, String content) {
        if ((templateFileName == null) || !templateFileName.endsWith(YAML_EXTENSION)) {
            throw new IllegalArgumentException("YAML template filename required");
        }
        if ((content == null) || content.isEmpty() || !validateContent(content, true)) {
            throw new IllegalArgumentException("Valid YAML template file content required");
        }
        return writeFile(getCollectorConfigTemplateDirectory(), templateFileName, content);
    }

    @Override
    public boolean deleteCollectorConfigTemplate(String templateFileName) {
        if ((templateFileName == null) || !templateFileName.endsWith(YAML_EXTENSION)) {
            throw new IllegalArgumentException("YAML template filename required");
        }
        return deleteFile(getCollectorConfigTemplateDirectory(), templateFileName);
    }

    @Override
    public String createCollectorConfig(String templateFileName, Map<String,String> identityProperties, String fileName) {
        // get collector config template
        String content = getCollectorConfigTemplate(templateFileName);
        if (content == null) {
            return null;
        }
        // determine template dumper style
        boolean flow = ((content.contains("{") && content.contains("}")) ||
                (content.contains("[") && content.contains("]")));
        // merge specified properties into identity
        if ((identityProperties != null) && !identityProperties.isEmpty()) {
            try {
                Yaml yaml = new Yaml(getDumperOptions(flow));
                Map<String,Object> config = (Map<String,Object>) yaml.load(content);
                Map<String,Object> configIdentity =
                        (Map<String,Object>) config.get(COLLECTOR_CONFIG_IDENTITY_SECTION_KEY);
                if (configIdentity == null) {
                    configIdentity = new HashMap<String,Object>();
                    config.put(COLLECTOR_CONFIG_IDENTITY_SECTION_KEY, configIdentity);
                }
                for (Map.Entry<String, String> identityProperty : identityProperties.entrySet()) {
                    configIdentity.put(identityProperty.getKey(), identityProperty.getValue());
                }
                content = yaml.dump(config);
            } catch (Exception e) {
                return null;
            }
        }
        // put collector config
        if (!putCollectorConfig(fileName, content)) {
            return null;
        }
        return content;
    }

    @Override
    public Map<String,Object> parseCollectorConfigContent(String content) {
        try {
            return (Map<String,Object>)((new Yaml()).load(content));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String formatCollectorConfigContent(Map<String,Object> config, boolean flow) {
        try {
            return (new Yaml(getDumperOptions(flow))).dump(config);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get collector config directory file from Foundation configuration.
     * Not cached to support live configuration changes. Attempt to
     * create if directory does not exist.
     *
     * @return collector config directory file or null
     */
    private static File getCollectorConfigDirectory() {
        String collectorConfigDirectory =
                FoundationConfiguration.getProperty(CONNECTOR_CONFIG_DIRECTORY_PROPERTY_NAME);
        if (collectorConfigDirectory == null) {
            collectorConfigDirectory = CONNECTOR_CONFIG_DIRECTORY_DEFAULT;
        }
        File collectorConfigDirectoryFile = new File(collectorConfigDirectory);
        if (!collectorConfigDirectoryFile.exists()) {
            if (collectorConfigDirectoryFile.mkdirs()) {
                log.info("CollectorConfigService created configurations directory: "+collectorConfigDirectoryFile);
            }
        }
        if (collectorConfigDirectoryFile.isDirectory() && collectorConfigDirectoryFile.canRead() &&
                collectorConfigDirectoryFile.canWrite() && collectorConfigDirectoryFile.canExecute()) {
            return collectorConfigDirectoryFile;
        }
        log.error("CollectorConfigService cannot access configurations directory: " + collectorConfigDirectoryFile);
        return null;
    }

    /**
     * Get collector config template directory file from Foundation
     * configuration. Not cached to support live configuration changes.
     * Attempt to create if directory does not exist.
     *
     * @return collector config template directory file or null
     */
    private static File getCollectorConfigTemplateDirectory() {
        String collectorConfigTemplateDirectory =
                FoundationConfiguration.getProperty(CONNECTOR_CONFIG_TEMPLATE_DIRECTORY_PROPERTY_NAME);
        if (collectorConfigTemplateDirectory == null) {
            collectorConfigTemplateDirectory = CONNECTOR_CONFIG_TEMPLATE_DIRECTORY_DEFAULT;
        }
        File collectorConfigTemplateDirectoryFile = new File(collectorConfigTemplateDirectory);
        if (!collectorConfigTemplateDirectoryFile.exists()) {
            if (collectorConfigTemplateDirectoryFile.mkdirs()) {
                log.info("CollectorConfigService created templates directory: "+collectorConfigTemplateDirectoryFile);
            }
        }
        if (collectorConfigTemplateDirectoryFile.isDirectory() && collectorConfigTemplateDirectoryFile.canRead() &&
                collectorConfigTemplateDirectoryFile.canWrite() && collectorConfigTemplateDirectoryFile.canExecute()) {
            return collectorConfigTemplateDirectoryFile;
        }
        log.error("CollectorConfigService cannot access templates directory: " + collectorConfigTemplateDirectoryFile);
        return null;
    }

    /**
     * List YAML file names in directory.
     *
     * @param directory YAML files directory
     * @return collection of file names or null
     */
    private static List<String> listFileNames(File directory) {
        if ((directory == null) || !directory.isDirectory() || !directory.canRead()) {
            return null;
        }
        File [] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(YAML_EXTENSION));
            }
        });
        List<String> fileNames = new ArrayList<String>();
        for (File file : files) {
            fileNames.add(file.getName());
        }
        Collections.sort(fileNames, new Comparator<String>() {
            @Override
            public int compare(String fileName1, String fileName2) {
                return fileName1.toLowerCase().compareTo(fileName2.toLowerCase());
            }
        });
        return fileNames;
    }

    /**
     * Read YAML file content.
     *
     * @param directory YAML files directory
     * @param fileName YAML file name
     * @return file content
     */
    private static String readFile(File directory, String fileName) {
        if ((directory == null) || !directory.isDirectory() || !directory.canRead()) {
            return null;
        }
        File file = new File(directory, fileName);
        if (!file.isFile() || !file.canRead()) {
            return null;
        }
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), YAML_FILE_ENCODING);
            StringBuilder readContent = new StringBuilder();
            char [] readBuffer = new char[BUFFER_SIZE];
            for (int read = reader.read(readBuffer); (read != -1); read = reader.read(readBuffer)) {
                readContent.append(readBuffer, 0, read);
            }
            return readContent.toString();
        } catch (IOException ioe) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Validate YAML file content. YAML file content is required
     * to be parsed into a string map. YAML file template content
     * is required to have an identity section and an agent type
     * identity property.
     *
     * @param content YAML file content
     * @param template validate as template
     * @return valid flag
     */
    private static boolean validateContent(String content, boolean template) {
        try {
            // validate YAML content
            Map<String,Object> config = (Map<String,Object>)((new Yaml()).load(content));
            if (config == null) {
                return false;
            }
            // validate YAML template content
            if (template) {
                Map<String, Object> configIdentity =
                        (Map<String, Object>) config.get(COLLECTOR_CONFIG_IDENTITY_SECTION_KEY);
                if (configIdentity == null) {
                    return false;
                }
                String configIdentityAgentType = (String) configIdentity.
                        get(COLLECTOR_CONFIG_IDENTITY_SECTION_AGENT_TYPE_KEY);
                if (configIdentityAgentType == null) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Write YAML file content.
     *
     * @param directory YAML files directory
     * @param fileName YAML file name
     * @param content YAML file content
     * @return write success
     */
    private static boolean writeFile(File directory, String fileName, String content) {
        if ((directory == null) || !directory.isDirectory() || !directory.canWrite()) {
            return false;
        }
        File file = new File(directory, fileName);
        if (file.isFile() && !file.canWrite()) {
            return false;
        }
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), YAML_FILE_ENCODING);
            Reader reader = new StringReader(content);
            char [] writeBuffer = new char[BUFFER_SIZE];
            for (int read = reader.read(writeBuffer); (read != -1); read = reader.read(writeBuffer)) {
                writer.write(writeBuffer, 0, read);
            }
            writer.flush();
            return true;
        } catch (IOException ioe) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Delete YAML file content.
     *
     * @param directory YAML files directory
     * @param fileName YAML file name
     * @return delete success
     */
    private static boolean deleteFile(File directory, String fileName) {
        if ((directory == null) || !directory.isDirectory() || !directory.canWrite()) {
            return false;
        }
        File file = new File(directory, fileName);
        if (!file.exists()) {
            return true;
        }
        if (!file.isFile() || !file.canWrite()) {
            return false;
        }
        return file.delete();
    }

    /**
     * Construct flow or block dumper options.
     *
     * @param flow flow or block style
     * @return dumper options
     */
    private static DumperOptions getDumperOptions(boolean flow) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(4);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        dumperOptions.setDefaultFlowStyle(flow ? DumperOptions.FlowStyle.FLOW : DumperOptions.FlowStyle.BLOCK);
        return dumperOptions;
    }
}
