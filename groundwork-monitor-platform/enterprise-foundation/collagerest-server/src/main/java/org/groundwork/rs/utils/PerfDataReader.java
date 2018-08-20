package org.groundwork.rs.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class PerfDataReader {

    static final String PERFDATA_CONFIG_FILE = "/usr/local/groundwork/config/perfdata.properties";

    /**
     * Read and parse the perfdata.properties and creates the Hashmap of perfdata source
     * and its properties
     *
     * @return Hashmap of perfdata source and its properties
     */
    public Map<String, Properties> readPerfDataConfig() {
        Map<String, Properties> perfData_source_map = new HashMap<String, Properties>();
        String service_perfdata_start_tag = "<service_perfdata_files>";
        String service_perfdata_end_tag = "</service_perfdata_files>";
        String perfdata_source_start_tag = "<perfdata_source";
        String perfdata_source_end_tag = "</perfdata_source>";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(PERFDATA_CONFIG_FILE));
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

}
