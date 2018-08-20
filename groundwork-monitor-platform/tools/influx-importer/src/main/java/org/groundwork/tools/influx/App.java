package org.groundwork.tools.influx;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        CommandLine commandLine = buildCommandOptions(args);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<OpenTSDBRecord> tsdbRecords = new ArrayList<>();
            String tsdbDirectory = commandLine.getOptionValue("t");
            File dir = new File(tsdbDirectory);
            File[] directoryListing = dir.listFiles();
            if (directoryListing == null || directoryListing.length == 0) {
                System.out.println("[ERROR] No files in dir:" + tsdbDirectory + ".. exiting");
                System.exit(0);
            }
            // process directory listing of files
            for (File file : directoryListing) {
                if (file.isFile()) {
                    try {
                        List<OpenTSDBRecord> records = objectMapper.readValue(file, new TypeReference<List<OpenTSDBRecord>>() {
                        });
                        if (records.size() == 0) {
                            System.out.println("...skipping " + file.getName() + ", empty file");
                        }
                        System.out.println("...: " + records.get(0).getMetric());
                        tsdbRecords.add(records.get(0));
                    } catch (Exception e) {
                        System.out.println("...skipping " + file.getName() + ", error reading: " + e.getMessage());
                    }
                }
            }
            if (tsdbRecords.size() == 0) {
                System.out.println("[ERROR] No TSDB records to process...exiting");
                System.exit(0);
            }
            // write to influx db
            InfluxWriter writer = new InfluxWriter(commandLine.getOptionValue("i"), commandLine.getOptionValue("d"));
            writer.connect();
            int total = 0;
            for (OpenTSDBRecord record : tsdbRecords) {
                int count = writer.write(record);
                System.out.println("...metrics written " + record.getMetric() + ((count >= 0) ? " successfully" : " with errors"));
                if (count > 0) {
                    total = total + count;
                }
            }
            writer.close();
            System.out.println("--- import run completed, " + tsdbRecords.size() + " TSDB files processed, " + total + " metrics written to Influx");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }


    public static CommandLine buildCommandOptions(String[] args) {
        System.out.println("---- Groundwork Open TSDB to Influx Mapping Utility -----");
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        Option tsdbDirectory = Option.builder("t").longOpt("tsdb-dir")
                .desc("path to OpenTSDB directory")
                .required(true)
                .hasArg(true)
                .argName("TSDB-DIRECTORY-PATH").build();
        Option influxURL = Option.builder("i").longOpt("influx")
                .desc("Influx db host and port, for ex: http://localhost:8086")
                .required(true)
                .hasArg(true)
                .argName("INFLUXDB-HOST-PORT").build();
        Option database = Option.builder("d").longOpt("database")
                .desc("Influx database name to import into")
                .required(true)
                .hasArg(true)
                .argName("INFLUXDB-DATABASE").build();

        options.addOption(tsdbDirectory).addOption(influxURL).addOption(database);
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(120);
            formatter.printHelp("java -jar influx-importer.jar -t /usr/local/groundwork/tsdb -i http://localhost:8086 -d groundwork2", options);
            System.exit(1);
        }
        return line;
    }
}
