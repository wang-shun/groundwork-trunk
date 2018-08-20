package org.groundwork.cloudhub.statistics;

import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(MonitoringStatisticsService.NAME)
public class MonitoringStatisticsServiceImpl implements MonitoringStatisticsService {

    public static final String STATISTICS_DIRECTORY = "/usr/local/groundwork/config/cloudhub/statistics/";
    public static final String STATISTICS_FILE_BASE = "loadtest-%s.csv";
    public static final char COMMA_SEPARATOR = ',';
    public static final String CSV_SPLIT_BY = ",";
    public static final char TEXT_SEPARATOR = '"';
    public static final char NEWLINE = '\n';
    protected static final String CONFIG_FILE_PATTERN = "([a-z]+)-([0-9]+)\\.csv";
    protected Pattern configFilePattern = Pattern.compile(CONFIG_FILE_PATTERN);
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Map<String,MonitoringStatistics> statisticsMap = new ConcurrentHashMap<>();
    private boolean enabled = false;

    @Override
    public MonitoringStatistics create(String agentName) {
        MonitoringStatistics ms = new MonitoringStatistics(agentName);
        statisticsMap.put(agentName, ms);
        return ms;
    }

    @Override
    public MonitoringStatistics lookup(String agentName) {
        return statisticsMap.get(agentName);
    }

    @Override
    public void setEnabled(boolean flag) {
        enabled = flag;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public MonitoringStatistics rename(String oldName, String newName) {
        MonitoringStatistics statistics = statisticsMap.get(oldName);
        if (statistics != null) {
            statisticsMap.remove(oldName);
            statistics.setName(newName);
            statisticsMap.put(newName, statistics);
            return statistics;
        }
        return null;
    }

    @Override
    public String save() {
        createConfigurationDirectory();
        // write to comma-separated file for spreadsheet imports
        String filePath = STATISTICS_DIRECTORY + calculateNextFileName();
        try {
            FileWriter writer = new FileWriter(filePath, false);
            // sort by name
            List<String> names = new ArrayList<>();
            for (String name : statisticsMap.keySet()) {
                names.add(name);
            }
            Collections.sort(names);

            //  walk through each row
            writeStrings(names, writer, "Name", "getName");
            writeDates(names, writer, "Date", "getDate");
            // execution times
            writeExecutionTimes(names, writer, "Execute Inventory Sync", "getInventorySync");
            writeExecutionTimes(names, writer, "Execute Monitor Sync", "getMonitorSync");
            writeExecutionTimes(names, writer, "Execute Monitor Update", "getMonitorUpdate");
            writeExecutionTimes(names, writer, "Execute Add Hypervisors", "getAddHypervisors");
            writeExecutionTimes(names, writer, "Execute Add VMs", "getAddVMs");
            writeExecutionTimes(names, writer, "Execute Modify Hypervisors", "getModifyHypervisors");
            writeExecutionTimes(names, writer, "Execute Modify VMs", "getModifyVMs");
            // adds
            writeAddHypervisors(names, writer, "Add Hypervisor Hosts", "getHosts");
            writeAddHypervisors(names, writer, "Add Hypervisor Services", "getServices");
            writeAddHypervisors(names, writer, "Add Hypervisor Events", "getEvents");
            writeAddVMs(names, writer, "Add VM Hosts", "getHosts");
            writeAddVMs(names, writer, "Add VM Services", "getServices");
            writeAddVMs(names, writer, "Add VM Events", "getEvents");
            // modifies
            writeModHypervisors(names, writer, "Mod Hypervisor Hosts", "getHosts");
            writeModHypervisors(names, writer, "Mod Hypervisor Services", "getServices");
            writeModHypervisors(names, writer, "Mod Hypervisor Events", "getEvents");
            writeModHypervisors(names, writer, "Mod Hypervisor HostNotifies", "getHostNotifications");
            writeModHypervisors(names, writer, "Mod Hypervisor ServiceNotifies", "getServiceNotifications");
            writeModHypervisors(names, writer, "Mod Hypervisor Performance", "getPerformance");
            writeModVMs(names, writer, "Mod VM Hosts", "getHosts");
            writeModVMs(names, writer, "Mod VM Services", "getServices");
            writeModVMs(names, writer, "Mod VM Events", "getEvents");
            writeModVMs(names, writer, "Mod VM HostNotifies", "getHostNotifications");
            writeModVMs(names, writer, "Mod VM ServiceNotifies", "getServiceNotifications");
            writeModVMs(names, writer, "Mod VM Performance", "getPerformance");
            // host queries
            writeHostQueries(names, writer, "Query Host", "getHosts");
            writeHostQueries(names, writer, "Query HostStatus", "getHostStatuses");
            writeHostQueries(names, writer, "Query HostStatus PropAll", "getHostStatusProperty");
            writeHostQueries(names, writer, "Query HostStatus Prop1", "getHostStatusProperty1");
            writeHostQueries(names, writer, "Query HostStatus Prop2", "getHostStatusProperty2");
            writeHostQueries(names, writer, "Query HostStatus Prop3", "getHostStatusProperty3");
            writeHostQueries(names, writer, "Query HostGroups", "getHostGroups");
            // service queries
            writeServiceQueries(names, writer, "Query Services", "getServices");
            writeServiceQueries(names, writer, "Query Services CPU", "getServicesCPU");
            writeServiceQueries(names, writer, "Query Services CPUMax", "getServicesCPUToMax");
            writeServiceQueries(names, writer, "Query Services FreeSpace", "getServicesFreeSpace");
            writeServiceQueries(names, writer, "Query Services MemSize", "getServicesSwappedMemSize");
            writeServiceQueries(names, writer, "Query Services PropAll", "getServiceStatusProperty");
            writeServiceQueries(names, writer, "Query Services Prop1", "getServiceStatusProperty1");
            writeServiceQueries(names, writer, "Query Services Prop53", "getServiceStatusProperty53");
            // event queries
            writeEventQueries(names, writer, "Query Events", "getEvents");
            writeEventQueries(names, writer, "Query Events Host", "getHostEvents");
            writeEventQueries(names, writer, "Query Events Service", "getServiceEvents");
            writeEventQueries(names, writer, "Query Events Setup", "getSetupEvents");

            writer.flush();
            writer.close();
            return filePath;
        }
        catch (Exception e) {
            throw new CloudHubException("Failed to save statistics  " + filePath, e);
        }
    }

    protected void writeStrings(List<String> names, Writer writer, String columnName, String methodName)
        throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(TEXT_SEPARATOR);
            writer.append((String)method.invoke(statistics));
            writer.append(TEXT_SEPARATOR);
        }
        writer.append(NEWLINE);
    }

    protected void writeDates(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            Date date = (Date)method.invoke(statistics);
            writer.append(dateFormat.format(date));
        }
        writer.append(NEWLINE);
    }

    protected void writeExecutionTimes(List<String> names, Writer writer, String columnName, String methodName)
        throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);

            Method method = statistics.getExecutionTimes().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Long.toString((Long) method.invoke(statistics.getExecutionTimes())));
        }
        writer.append(NEWLINE);
    }

    protected void writeAddHypervisors(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getAddsHypervisors().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Integer.toString((Integer) method.invoke(statistics.getAddsHypervisors())));
        }
        writer.append(NEWLINE);
    }

    protected void writeAddVMs(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getAddsVMs().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Integer.toString((Integer) method.invoke(statistics.getAddsVMs())));
        }
        writer.append(NEWLINE);
    }

    protected void writeModHypervisors(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getModsHypervisors().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Integer.toString((Integer) method.invoke(statistics.getModsHypervisors())));
        }
        writer.append(NEWLINE);
    }

    protected void writeModVMs(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getModsVMs().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Integer.toString((Integer) method.invoke(statistics.getModsVMs())));
        }
        writer.append(NEWLINE);
    }

    protected void writeHostQueries(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getHostQueries().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Integer.toString((Integer) method.invoke(statistics.getHostQueries())));
        }
        writer.append(NEWLINE);
    }

    protected void writeServiceQueries(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getServiceQueries().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Integer.toString((Integer) method.invoke(statistics.getServiceQueries())));
        }
        writer.append(NEWLINE);
    }

    protected void writeEventQueries(List<String> names, Writer writer, String columnName, String methodName)
            throws Exception {
        writer.append(TEXT_SEPARATOR);
        writer.append(columnName);
        writer.append(TEXT_SEPARATOR);
        for (String name : names) {
            MonitoringStatistics statistics = statisticsMap.get(name);
            Method method = statistics.getEventQueries().getClass().getMethod(methodName);
            writer.append(COMMA_SEPARATOR);
            writer.append(Integer.toString((Integer) method.invoke(statistics.getEventQueries())));
        }
        writer.append(NEWLINE);
    }

    protected synchronized void createConfigurationDirectory() throws CloudHubException {
        try {
            File dir = new File(STATISTICS_DIRECTORY);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new CloudHubException("Failed to create CloudHub Statistics directory: " + STATISTICS_DIRECTORY);
                }
            }
        }
        catch (SecurityException e) {
            throw new CloudHubException("Failed to access CloudHub statistics directory: " + STATISTICS_DIRECTORY, e);
        }
    }

    protected String calculateNextFileName() throws CloudHubException {
        int last = 0;
        File dir = new File(STATISTICS_DIRECTORY);
        if (!dir.exists()) {
            throw new CloudHubException("Failed to find statistics directory: " + STATISTICS_DIRECTORY);
        }
        File[] children = dir.listFiles();
        for (File file : children) {
            if (file.isFile()) {
                int number = extractConfigNumberFromFilename(file.getName());
                last = (number > last) ? number : last;
            }
        }
        String base = String.format(STATISTICS_FILE_BASE, last + 1);
        return base;
    }

    protected int extractConfigNumberFromFilename(String fileName) {
        Matcher matcher = configFilePattern.matcher(fileName);
        if (matcher.matches())
        {
            if (matcher.groupCount() == 2) {
                String number = matcher.group(2);
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

    public MonitoringStatistics readCSV(String csvFileName, int index) {
        MonitoringStatistics statistics = new MonitoringStatistics(csvFileName);
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(csvFileName));
            // skip name
            line = br.readLine();
            if (line == null)
                return statistics;
            // skip date
            line = br.readLine();
            if (line == null)
                return statistics;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] values = line.split(CSV_SPLIT_BY);
                String name = values[0].replace("\"", "");
                Integer count = Integer.parseInt(values[index]);
                switch (name) {
                    case "Query Host":
                        statistics.getHostQueries().setHosts(count);
                        break;
                    case "Query HostStatus":
                        statistics.getHostQueries().setHostStatuses(count);
                        break;
                    case "Query HostStatus PropAll":
                        statistics.getHostQueries().setHostStatusProperty(count);
                        break;
                    case "Query HostStatus Prop1":
                        statistics.getHostQueries().setHostStatusProperty1(count);
                        break;
                    case "Query HostStatus Prop2":
                        statistics.getHostQueries().setHostStatusProperty2(count);
                        break;
                    case "Query HostStatus Prop3":
                        statistics.getHostQueries().setHostStatusProperty3(count);
                        break;
                    case "Query HostGroups":
                        statistics.getHostQueries().setHostGroups(count);
                        break;
                    case "Query Services":
                        statistics.getServiceQueries().setServices(count);
                        break;
                    case "Query Services CPU":
                        statistics.getServiceQueries().setServicesCPU(count);
                        break;
                    case "Query Services CPUMax":
                        statistics.getServiceQueries().setServicesCPUToMax(count);
                        break;
                    case "Query Services FreeSpace":
                        statistics.getServiceQueries().setServicesFreeSpace(count);
                        break;
                    case "Query Services MemSize":
                        statistics.getServiceQueries().setServicesSwappedMemSize(count);
                        break;
                    case "Query Services PropAll":
                        statistics.getServiceQueries().setServiceStatusProperty(count);
                        break;
                    case "Query Services Prop1":
                        statistics.getServiceQueries().setServiceStatusProperty1(count);
                        break;
                    case "Query Services Prop53":
                        statistics.getServiceQueries().setServiceStatusProperty53(count);
                        break;
                    case "Query Events":
                        statistics.getEventQueries().setEvents(count);
                        break;
                    case "Query Events Host":
                        statistics.getEventQueries().setHostEvents(count);
                        break;
                    case "Query Events Service":
                        statistics.getEventQueries().setServiceEvents(count);
                        break;
                    case "Query Events Setup":
                        statistics.getEventQueries().setSetupEvents(count);
                        break;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return statistics;
    }


}
