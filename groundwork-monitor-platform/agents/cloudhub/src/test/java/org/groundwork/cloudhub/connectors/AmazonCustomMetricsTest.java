package org.groundwork.cloudhub.connectors;

import com.amazonaws.services.cloudwatch.model.*;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.AmazonConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.connectors.amazon.AWSConnection;
import org.groundwork.cloudhub.connectors.amazon.AmazonConnector;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class AmazonCustomMetricsTest extends AbstractAgentTest {

    @Test
    public void AmazonCustomMetricsReadTest() throws Exception {
        AmazonConfiguration config = null;
        try {
            config = new AmazonConfiguration();
            ServerConfigurator.setupAmazonConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            AmazonConnector connector = (AmazonConnector) connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());


            // List Metrics
            HashMap<String, Metric> availableMetrics = new HashMap<String, Metric>();
            HashMap<String, Metric> awsMetrics = new HashMap<String, Metric>();
            ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
            //listMetricsRequest.setDimensions(dimFilterList);
            for (; ; ) {
                ListMetricsResult metrics = connector.getAwsConnection().getMetricsClient().listMetrics(listMetricsRequest);
                for (Metric metric : metrics.getMetrics()) {
                    if (!metric.getNamespace().startsWith("AWS")) {
                        availableMetrics.put(metric.getNamespace() + "." + metric.getMetricName(), metric);
                    }
                    else {
                        awsMetrics.put(metric.getNamespace() + "." + metric.getMetricName(), metric);
                    }
                }

                String nextBatchId = metrics.getNextToken();
                if (nextBatchId == null) {
                    break;
                }
                listMetricsRequest.setNextToken(nextBatchId);
            }
//            Map<String, Metric> sorted = new TreeMap<String, Metric>(awsMetrics);
//            for (Map.Entry entry : sorted.entrySet()) {
//                Metric m = (Metric)entry.getValue();
//                System.out.println(entry.getKey()) ;
//            }
            for (Map.Entry entry : availableMetrics.entrySet()) {
                Metric m = (Metric)entry.getValue();
                System.out.println("key: " + entry.getKey() + ", value: " + m.getMetricName()) ;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
        }
    }


    @Test
    public void AmazonCustomMetricsPopulateTest() throws Exception {
        System.out.println("-- Running Custom Metrics populator... ");
        final AmazonConfiguration config = new AmazonConfiguration();;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("Shutting down ...");
                    if (config != null)
                        configurationService.deleteConfiguration(config);
                    profileService.removeProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
                    System.out.println("-- Completed Custom Metrics populator... ");

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        try {
            ServerConfigurator.setupAmazonConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            AmazonConnector connector = (AmazonConnector) connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());

            String[] metrics = {"Random1", "Random2"};
            String [] instances = {"i-c273b035", "i-385973f1"};

            for (;;) {
                System.out.println("-- populating custom metrics DST/Test... ");
                for (String instance : instances) {
                    List<Dimension> dimensions = new ArrayList<>();
                    Dimension dim = new Dimension();
                    dim.setName("InstanceId");
                    dim.setValue(instance);
                    dimensions.add(dim);
                    populate(connector.getAwsConnection(), "DST/Test", metrics, dimensions);
                }
                System.out.println("-- ....metrics populated");
                Thread.sleep(30000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
            System.out.println("-- Completed Custom Metrics populator... ");
        }
    }

    public void populate(AWSConnection connection, String namespace, String[] metrics, List<Dimension> dimensions) throws ParseException {

        List<String> awsMetricStatList = Arrays.asList(new String[]{"Average", "SampleCount"});
        GetMetricStatisticsRequest getMetricStatisticsRequest = new GetMetricStatisticsRequest();
        getMetricStatisticsRequest.setStatistics(awsMetricStatList);

        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest();
        putMetricDataRequest.setNamespace(namespace);
        Random rand = new Random();
        for (String metric : metrics) {
            MetricDatum metricDatum = new MetricDatum();
            metricDatum.setMetricName(metric);
            metricDatum.setTimestamp(generateDate(0));
            metricDatum.setDimensions(dimensions);
            metricDatum.setUnit(StandardUnit.Count);
            int  n = rand.nextInt(20) + 1;
            metricDatum.setValue(new Double(n));
            putMetricDataRequest.getMetricData().add(metricDatum);
        }
        connection.getMetricsClient().putMetricData(putMetricDataRequest);
    }

    public Date generateDate(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, offset);
        return cal.getTime();
    }

}
