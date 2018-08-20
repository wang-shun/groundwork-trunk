package com.groundwork.collage.test;

import com.groundwork.collage.model.LogPerformanceData;
import com.groundwork.collage.model.PerformanceDataLabel;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.performancedata.PerformanceDataService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dtaylor on 3/12/17.
 */
public class TestPerformanceDataService extends AbstractTestCaseWithTransactionSupport {

    public TestPerformanceDataService(String x) {
        super(x);
    }

    private PerformanceDataService performanceDataService;

    /**
     * define the tests to be run in this class
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();

        executeScript(false, "testdata/monitor-data.sql");

        suite.addTest(new TestPerformanceDataService("testWriteAndRead"));
        suite.addTest(new TestPerformanceDataService("testGetPerformanceData"));
        suite.addTest(new TestPerformanceDataService("testWriteWithoutLabel"));
        suite.addTest(new TestPerformanceDataService("testCleanup"));

        return suite;
    }

    public void setUp() throws Exception {
        super.setUp();
        performanceDataService = collage.getPerformanceDataService();
        assertNotNull(performanceDataService);
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }

    public static final String CPU_JAVA = "cpu-java-s";

    public void testWriteAndRead() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        // setup one label
        PerformanceDataLabel cpuJava = performanceDataService.createPerformanceDataLabelEntry(CPU_JAVA);
        performanceDataService.updatePerformanceDataLabelEntry(cpuJava.getPerformanceDataLabelId(), "disk_utilization", "disk", "percent");
        // ensure write read of label
        PerformanceDataLabel label = performanceDataService.lookupPerformanceDataLabel(CPU_JAVA);
        assertNotNull(label);

        // write one metric
        String date = dateFormat.format(new Date());
        String hostName = "nagios";
        String serviceDescription = "local_disk";
        performanceDataService.createOrUpdatePerformanceData(hostName, serviceDescription, CPU_JAVA, 40.0, date);
        LogPerformanceData perfData = performanceDataService.lookupPerformanceData(hostName, serviceDescription, CPU_JAVA);
        System.out.println("pd: " + perfData.toString());
        assertNotNull(perfData);
        assertEquals(perfData.getAverage(), 40.0);
        assertEquals(perfData.getMaximum(), 40.0);
        assertEquals(perfData.getMinimum(), 40.0);
        assertEquals(perfData.getMeasurementPoints().intValue(), 1);

        // testCache
        date = dateFormat.format(new Date());
        performanceDataService.createOrUpdatePerformanceData(hostName, serviceDescription, CPU_JAVA, 60.0, date);
        perfData = performanceDataService.lookupPerformanceData(hostName, serviceDescription, CPU_JAVA);
        assertNotNull(perfData);
        assertEquals(perfData.getAverage(), 50.0);
        assertEquals(perfData.getMaximum(), 60.0);
        assertEquals(perfData.getMinimum(), 40.0);
        assertEquals(perfData.getMeasurementPoints().intValue(), 2);

        date = dateFormat.format(new Date());
        performanceDataService.createOrUpdatePerformanceData(hostName, serviceDescription, CPU_JAVA, 80.0, date);
        perfData = performanceDataService.lookupPerformanceData(hostName, serviceDescription, CPU_JAVA);
        assertNotNull(perfData);
        assertEquals(perfData.getAverage(), 60.0);
        assertEquals(perfData.getMaximum(), 80.0);
        assertEquals(perfData.getMinimum(), 40.0);
        assertEquals(perfData.getMeasurementPoints().intValue(), 3);

        // tear down label and dependent perf data
        performanceDataService.deleteLabel(CPU_JAVA);
        label = performanceDataService.lookupPerformanceDataLabel(CPU_JAVA);
        assertNull(label);
        perfData = performanceDataService.lookupPerformanceData(hostName, serviceDescription, CPU_JAVA);
        assertNull(perfData);
    }

    public void testGetPerformanceData() {
        String html = performanceDataService.getPerformanceDataLabel();
        assertNotNull(html);
    }

    public void testWriteWithoutLabel() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        // write one metric with label
        String date = dateFormat.format(new Date());
        String hostName = "nagios";
        String serviceDescription = "local_disk";
        performanceDataService.createOrUpdatePerformanceData(hostName, serviceDescription, CPU_JAVA, 40.0, date);
        LogPerformanceData perfData = performanceDataService.lookupPerformanceData(hostName, serviceDescription, CPU_JAVA);
        assertNotNull(perfData);
        assertEquals(perfData.getAverage(), 40.0);
        assertEquals(perfData.getMaximum(), 40.0);
        assertEquals(perfData.getMinimum(), 40.0);
        assertEquals(perfData.getMeasurementPoints().intValue(), 1);


        // tear down label and dependent perf data
        performanceDataService.deleteLabel(CPU_JAVA);
        PerformanceDataLabel label = performanceDataService.lookupPerformanceDataLabel(CPU_JAVA);
        assertNull(label);
        perfData = performanceDataService.lookupPerformanceData(hostName, serviceDescription, CPU_JAVA);
        assertNull(perfData);

    }

    public void testCleanup() {
        performanceDataService.deleteLabel(CPU_JAVA);
    }
}
