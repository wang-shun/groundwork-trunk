package com.groundwork.feeder.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class RRDPerfDataWriter implements PerfDataWriter {

    private static final String MESSAGE_DELIMITER = "\t";

    private static final String PERF_DATA_PROPERTIES_FILE = "/usr/local/groundwork/config/perfdata.properties";
    private static final int MAX_EXCEPTIONS_PER_APPTYPE = 1;

    private Log log = LogFactory.getLog(RRDPerfDataWriter.class);

    private Map<String, Properties> propertiesByAppType = null;
    private Map<String, Integer> exceptionCountForAppType = new HashMap<>();

    public RRDPerfDataWriter() {
        propertiesByAppType = this.parseServicePerfDataConfig(PERF_DATA_PROPERTIES_FILE);
    }

    public Properties getProperties(String appType) {
        return propertiesByAppType.get(appType);
    }

    public void writeMessages(List<String> messageList, String appType) {
        if (appType == null) {
            log.error("App Type not provided, cannot process message");
            return;
        }
        String processedFileName = calculateProcessedFileName(appType);
        if (processedFileName == null) {
             // skip it, had problem with appType
            return;
        }
        String tempFileName = calculateTempFile(processedFileName);
        File fileTemp = new File(tempFileName);
        if (!fileTemp.isFile()) {
            // creating the temp .dat file
            if (log.isDebugEnabled()) {
                log.info("no .dat file exists. so creating new.. " + tempFileName);
            }
            try {
                fileTemp.createNewFile();
            }
            catch (IOException e) {
                log.error("Could not create a new file " + tempFileName + ", error: " + e.getMessage(), e);
                return;
            }
        }

        BufferedWriter bw = null;
        try {
            FileWriter fw = new FileWriter(fileTemp.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            for (String message : messageList) {
                // message format:
                // serverTime TAB serverName TAB serviceName TAB TAB label = value ; warning ; critical [ TAB [ tagName = tagValue ; ]* ]?
                //
                // strip non-standard extended tags from message not supported by RRD
                int extendedTagsIndex = extendedTagsIndex(message);
                if (extendedTagsIndex != -1) {
                    message = message.substring(0, extendedTagsIndex);
                }
                // write RRD message
                bw.write(message);
                bw.write("\n");
            }
        }
        catch (Exception e) {
            log.error("Error writing perf data for fileName: " + tempFileName + " error: " + e.getMessage(), e);
        }
        finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException fje) {
                    log.error("Error closing writer for " + tempFileName + " failed to close: " + fje.getMessage(), fje);
                }
            }
        }

        // renaming the file
        if (log.isDebugEnabled()) {
            log.debug("checking if .being_processed exists for "  + processedFileName);
        }
        File fileNew = new File(processedFileName);
        if (!fileNew.isFile()) {
            if (log.isInfoEnabled()) {
                log.info("Renaming '" + tempFileName + "' file to .being_processed file '" + processedFileName);
            }
            fileTemp.renameTo(fileNew);
        }

    }

    /**
     * Parse the perfdata.properties and creates the Hashmap of perfdata source
     * and its properties
     *
     * @return Hashmap of perfdata source and its properties
     */
    private Map<String, Properties> parseServicePerfDataConfig(String perfDataPropFilePath) {
        Map<String, Properties> perfData_source_map = new HashMap<String, Properties>();
        String service_perfdata_start_tag = "<service_perfdata_files>";
        String service_perfdata_end_tag = "</service_perfdata_files>";
        String perfdata_source_start_tag = "<perfdata_source";
        String perfdata_source_end_tag = "</perfdata_source>";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(perfDataPropFilePath));
            String sCurrentLine;
            boolean servicePerfStart = false;
            boolean perfSourceStart = false;
            String source = null;
            Properties prop = null;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.equalsIgnoreCase(service_perfdata_start_tag))
                    servicePerfStart = true;
                if (servicePerfStart) {
                    if (sCurrentLine != null
                            && !sCurrentLine.trim().startsWith("#")
                            && sCurrentLine.trim().startsWith(
                            perfdata_source_start_tag)) {
                        source = sCurrentLine
                                .substring(
                                        sCurrentLine
                                                .indexOf(perfdata_source_start_tag) + 17,
                                        sCurrentLine.length() - 1);
                        prop = new Properties();
                        perfSourceStart = true;
                    } // end if
                    if (perfSourceStart) {
                        if (sCurrentLine != null
                                && sCurrentLine.indexOf("=") != -1) {
                            StringTokenizer stkn = new StringTokenizer(
                                    sCurrentLine, "=");
                            if (prop != null)
                                prop.put(stkn.nextToken().trim(), stkn
                                        .nextToken().trim()
                                        .replaceAll("\"", ""));
                        } // end if
                    } // end if
                    if (sCurrentLine.trim().equalsIgnoreCase(
                            perfdata_source_end_tag)) {
                        perfSourceStart = false;
                        perfData_source_map.put(source.trim(), prop);
                    } // end if
                } // end if
                if (sCurrentLine != null
                        && sCurrentLine.trim().equalsIgnoreCase(
                        service_perfdata_end_tag))
                    servicePerfStart = false;
            } // end while

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } // end try/catch
            } // end if
        } // end finally
        return perfData_source_map;
    }

    private String calculateProcessedFileName(String appType) {
        Properties properties = propertiesByAppType.get(appType);
        if (properties == null) {
            logAppTypeError(appType);
            return null;
        }
        String perfDataFile = properties.getProperty("perfdata_file");
        if (perfDataFile == null) {
            logAppTypeError(appType);
        }
        return perfDataFile;
    }

    private String calculateTempFile(String dataFilePath) {
        return dataFilePath.substring(0, dataFilePath.indexOf(".being_processed"));
    }

    private void logAppTypeError(String appType) {
        Integer count = exceptionCountForAppType.get(appType);
        if (count == null) {
            count = new Integer(0);
        }
        count = count + 1;
        if (count <= MAX_EXCEPTIONS_PER_APPTYPE) {
            log.error("Property Type not found in RR Perf Data configuration: " + appType);
        }
        exceptionCountForAppType.put(appType, count);
    }

    private static int extendedTagsIndex(String message) {
        int extendedTagsIndex = -1;
        for (int i = 0; (i < 5); i++) {
            extendedTagsIndex = message.indexOf(MESSAGE_DELIMITER, extendedTagsIndex+1);
            if (extendedTagsIndex == -1) {
                break;
            }
        }
        return extendedTagsIndex;
    }
}
