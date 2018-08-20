package org.groundwork.tools.influx;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InfluxWriter {

    private String connectionString;
    private String database;
    private InfluxDB influxDB;

    public InfluxWriter(String connectionString, String database) {
        this.connectionString = connectionString;
        this.database = database;
    }

    public void connect() {
        influxDB = InfluxDBFactory.connect(connectionString);
        List<String> databases = influxDB.describeDatabases();
        if (!databases.contains(database)) {
            System.out.println("Database " + database + " does not exist.  Creating...");
            influxDB.createDatabase(database);
        }
        //influxDB.setDatabase(database);
        System.out.println("--- influx connected, version=" + influxDB.version());
    }

    public void close() {
        influxDB.close();
    }

    public int write(OpenTSDBRecord record) {
        int count = 0;
        try {
            BatchPoints batchPoints = BatchPoints.database(database).build();
            for (Map.Entry<String, Double> dps : record.getDps().entrySet()) {
                Long timeStamp = Long.parseLong(dps.getKey());
                Point point = Point.measurement(record.getMetric())
                        .time(timeStamp, TimeUnit.SECONDS)
                        .addField(record.getMetric(), dps.getValue())
                        .tag(record.getTags())
                        .build();
                batchPoints.point(point);
                count = count + 1;
            }
            influxDB.write(batchPoints);
            return count;
        }
        catch (Exception e) {
            System.out.println("[ERROR] error writing influx records " + e.getMessage());
            return -1;
        }

    }

    /**
    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086");
        String database = "groundwork";
        // Reduce transfer size for potentially modest performance boost on large write operations
        influxDB.enableGzip();

        // Create the DB if it does not exist
        List<String> databases = influxDB.describeDatabases();
        if (!databases.contains(database)) {
            System.out.println("Database " + database + " does not exist.  Creating...");
            influxDB.createDatabase(database);
        }
//        else {
//            influxDB.setDatabase(database);
//        }
        Pong pong = influxDB.ping(); influxDB.version();
        System.out.println("version=" + pong.getVersion());
        System.out.println("version=" + influxDB.version());

        influxDB.enableBatch(BatchOptions.DEFAULTS);

        influxDB.write(Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L)
                .tag("host", "myhost")
                .build());

        influxDB.flush();
        Query query = new Query("SELECT idle FROM cpu", database);
        QueryResult results = influxDB.query(query);
        for (QueryResult.Result result : results.getResults()) {
            System.out.println(result.toString());
        }
        influxDB.close();
    }
    ***/
}
