package com.groundworkopensource.portal.common;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by dtaylor on 3/16/15.
 */
public class GroundworkInfoReader {

    private static Logger logger = Logger.getLogger(GroundworkInfoReader.class.getName());


    public static final String INFO_FILE = "/usr/local/groundwork/Info.txt";
    public static final String DOT_PREFIX = ".....";

    public static String[] SKIP_KEYS = {
            "Groundwork",
            "Components",
            "Product"
    };

    public static Properties readInfoProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(INFO_FILE)));
            Set<String> removals = new HashSet<>();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("=")) {
                    removals.add(key);
                }
                for (String skip : SKIP_KEYS) {
                    if (key.equalsIgnoreCase(skip)) {
                        removals.add(key);
                        break;
                    }
                }
            }
            for (String key : removals) {
                properties.remove(key);
            }
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                if (value.startsWith(DOT_PREFIX)) {
                    String newValue = value.substring(DOT_PREFIX.length()).trim();
                    properties.put(key, newValue);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read " + INFO_FILE + ", message: " + e.getMessage(), e);
        }
        return properties;
    }

    public static String readInfoHTML() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(INFO_FILE));
            StringBuffer html = new StringBuffer();
            html.append("<p>\n");
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line).append("<br>\n");
            }
            html.append("</p>\n");
            return html.toString();
        }
        catch (Exception e) {
            logger.error("Failed to read " + INFO_FILE + ", message: " + e.getMessage(), e);
            return "<p>System Information is not available</p>";
        }
    }
}
