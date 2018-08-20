package org.groundwork.cloudhub.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PostgresUtils {

    public static String getEnvironmentVariable(String variable, String defaultValue) {
        String value = System.getenv(variable);
        return (value == null) ? defaultValue : value ;
    }

    public static boolean restoreDatabase() {
        try {
            List<String> cmds = new ArrayList<String>();
            cmds.add("pg_restore");
            cmds.add("-d");
            cmds.add("gwcollagedb");
            cmds.add("-U");
            cmds.add("postgres");
            cmds.add("-F");
            cmds.add("t");
            cmds.add("-c");
            cmds.add("./src/test/testdata/gwcollagedb-2013-09-05.sql.tar");
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.environment().put("PGLIB", getEnvironmentVariable("PGLIB", "/usr/lib/pgsql"));
            pb.environment().put("PGDATA", getEnvironmentVariable("PGDATA", "/var/lib/pgsql/data"));
            pb.environment().put("PGHOST", getEnvironmentVariable("PGHOST", "localhost"));
            pb.environment().put("PGPASSWORD", getEnvironmentVariable("PGPASSWORD", "postgres"));
            Process process = pb.command(cmds).start();
            process.waitFor();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
