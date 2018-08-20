package com.groundwork.feeder.service;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class RRDPerfDataWriterTest {

    @Test
    public void testParseConfig() {
        RRDPerfDataWriter rrdWriter = new RRDPerfDataWriter();
        Properties vema = rrdWriter.getProperties("VEMA");
        assert vema.getProperty("perfdata_file").equals("/usr/local/groundwork/core/vema/var/vema-perfdata.dat.being_processed");
        assert vema.getProperty("seek_file").equals("/usr/local/groundwork/core/vema/var/vema-perfdata.dat.seek");
        Properties redhat = rrdWriter.getProperties("CHRHEV");
        assert redhat.getProperty("perfdata_file").equals("/usr/local/groundwork/core/vema/var/chrhev-perfdata.dat.being_processed");
        assert redhat.getProperty("seek_file").equals("/usr/local/groundwork/core/vema/var/chrhev-perfdata.dat.seek");
        Properties openStack = rrdWriter.getProperties("OS");
        assert openStack.getProperty("perfdata_file").equals("/usr/local/groundwork/core/vema/var/os-perfdata.dat.being_processed");
        assert openStack.getProperty("seek_file").equals("/usr/local/groundwork/core/vema/var/os-perfdata.dat.seek");
        Properties docker = rrdWriter.getProperties("DOCK");
        assert docker.getProperty("perfdata_file").equals("/usr/local/groundwork/core/vema/var/dock-perfdata.dat.being_processed");
        assert docker.getProperty("seek_file").equals("/usr/local/groundwork/core/vema/var/dock-perfdata.dat.seek");
        Properties openDayLight = rrdWriter.getProperties("ODL");
        assert openDayLight.getProperty("perfdata_file").equals("/usr/local/groundwork/core/vema/var/odl-perfdata.dat.being_processed");
        assert openDayLight.getProperty("seek_file").equals("/usr/local/groundwork/core/vema/var/odl-perfdata.dat.seek");

        Properties nagios = rrdWriter.getProperties("NAGIOS");
        assert nagios == null;
    }

    @Test
    public void testWritesAndAppends() {
        RRDPerfDataWriter rrdWriter = new RRDPerfDataWriter();

        deleteTestData();

        try {
            // create process files to prevent rename and test appending
            createFile("/usr/local/groundwork/core/vema/var/dock-perfdata.dat.being_processed");
            createFile("/usr/local/groundwork/core/vema/var/vema-perfdata.dat.being_processed");

            List<String> messages = new LinkedList<>();
            for (int ix = 1; ix < 20; ix++) {
                messages.add(Integer.toString(ix));
            }
            rrdWriter.writeMessages(messages, "DOCK");
            rrdWriter.writeMessages(messages, "VEMA");

            assertAccessToDataFile("/usr/local/groundwork/core/vema/var/dock-perfdata.dat.being_processed");
            assertAccessToDataFile("/usr/local/groundwork/core/vema/var/dock-perfdata.dat");
            assertAccessToDataFile("/usr/local/groundwork/core/vema/var/vema-perfdata.dat.being_processed");
            assertAccessToDataFile("/usr/local/groundwork/core/vema/var/vema-perfdata.dat");

            assertReadData("/usr/local/groundwork/core/vema/var/dock-perfdata.dat", 20);
            assertReadData("/usr/local/groundwork/core/vema/var/vema-perfdata.dat", 20);

            messages = new LinkedList<>();
            for (int ix = 20; ix < 30; ix++) {
                messages.add(Integer.toString(ix));
            }
            rrdWriter.writeMessages(messages, "DOCK");
            rrdWriter.writeMessages(messages, "VEMA");

            assertReadData("/usr/local/groundwork/core/vema/var/dock-perfdata.dat", 30);
            assertReadData("/usr/local/groundwork/core/vema/var/vema-perfdata.dat", 30);

            // force RRDPerfDataWriter to rename by deleting being processed files
            deleteFile("/usr/local/groundwork/core/vema/var/dock-perfdata.dat.being_processed");
            deleteFile("/usr/local/groundwork/core/vema/var/vema-perfdata.dat.being_processed");

            messages = new LinkedList<>();
            for (int ix = 30; ix < 40; ix++) {
                messages.add(Integer.toString(ix));
            }
            rrdWriter.writeMessages(messages, "DOCK");
            rrdWriter.writeMessages(messages, "VEMA");

            assertReadData("/usr/local/groundwork/core/vema/var/dock-perfdata.dat.being_processed", 40);
            assertReadData("/usr/local/groundwork/core/vema/var/vema-perfdata.dat.being_processed", 40);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            deleteTestData();
        }
    }

    private void assertReadData(String filename, int expectedCount) throws IOException {
        FileReader fileReader = new FileReader(new File(filename));
        BufferedReader reader = new BufferedReader(fileReader);
        int count = 0;
        try {
            String line = reader.readLine();
            int ix = 1;
            while (line != null) {
                int value = Integer.parseInt(line);
                assert value == ix;
                line = reader.readLine();
                ix++;
                count++;
            }
        }
        finally {
            fileReader.close();
        }
        count++;
        assert count == expectedCount;
    }

    private void deleteTestData() {
        // remove all data from dat and being_processed files for DOCK appType
        deleteFile("/usr/local/groundwork/core/vema/var/dock-perfdata.dat");
        deleteFile("/usr/local/groundwork/core/vema/var/dock-perfdata.dat.being_processed");
        // remove all data from dat and being_processed files for VEMA appType
        deleteFile("/usr/local/groundwork/core/vema/var/vema-perfdata.dat");
        deleteFile("/usr/local/groundwork/core/vema/var/vema-perfdata.dat.being_processed");
    }

    private void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists())
            file.delete();
        assert file.exists() == false;
    }

    private void assertAccessToDataFile(String filename) {
        File file = new File(filename);
        assert file.exists() == true;
    }

    private boolean createFile(String filename) throws IOException {
        File file = new File(filename);
        return file.createNewFile();
    }
}