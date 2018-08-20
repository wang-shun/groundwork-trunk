package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPerfData;
import org.groundwork.rs.dto.DtoPerfDataList;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Date;

public class PerfDataClientTest extends AbstractClientTest {


    @Test
    public void testPerfData() throws Exception {
        if (serverDown) return;
        DtoPerfDataList perfs = new DtoPerfDataList();
        DtoPerfData perf = new DtoPerfData();
        perf.setAppType("VEMA");
        perf.setCritical("500");
        perf.setLabel("CPU");
        perf.setServerName("localhost");
        perf.setServerTime(1397512737L);
        perf.setServiceName("cpu");
        perf.setValue("3343");
        perf.setWarning("300");
        perfs.add(perf);
        DtoPerfData perf2 = new DtoPerfData();
        perf2.setAppType("VEMA");
        perf2.setCritical("800");
        perf2.setLabel("FreeSpace");
        perf2.setServerName("localhost");
        perf2.setServerTime(1397512737L);
        perf2.setServiceName("freespace");
        perf2.setValue("334333");
        perf2.setWarning("600");
        perfs.add(perf2);
        DtoPerfData perf3 = new DtoPerfData();
        perf3.setAppType("VEMA");
        perf3.setCritical("5.0");
        perf3.setLabel("LoadFactor");
        perf3.setServerName("localhost");
        perf3.setServerTime(1397512737L);
        perf3.setServiceName("load");
        perf3.setValue("0.9");
        perf3.setWarning("4.0");
        perf3.setCpuTag("0");
        perf3.setTypeTag("core");
        perf3.setTagNames(Arrays.asList(new String[]{"test"}));
        perf3.setTagValues(Arrays.asList(new String[]{"value"}));
        perfs.add(perf3);
        PerfDataClient client = new PerfDataClient(getDeploymentURL());
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = client.post(perfs);
        assert 3 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert (result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
    }

    //@Test
    public void testPerfDataReports() throws Exception {
        if (serverDown) return;
        long now = new Date().getTime();

        PerfDataClient client = new PerfDataClient(getDeploymentURL());
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);

        DtoPerfDataList perfs = new DtoPerfDataList();
        perfs.add(createPerfData("CPU-JAVA", "localhost", now, "local_cpu_java", "40"));
        perfs.add(createPerfData("CPU-PERL", "localhost", now, "local_cpu_perl", "40"));
        perfs.add(createPerfData("LOCAL-MEMORY", "localhost", now, "local_memory", "40"));
        DtoOperationResults results = client.post(perfs);
        assert 1 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert (result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        perfs = new DtoPerfDataList();
        perfs.add(createPerfData("CPU-JAVA", "localhost", now, "local_cpu_java", "60"));
        perfs.add(createPerfData("CPU-PERL", "localhost", now, "local_cpu_perl", "60"));
        perfs.add(createPerfData("LOCAL-MEMORY", "localhost", now, "local_memory", "60"));
        results = client.post(perfs);
        assert 1 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert (result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

    }

    public DtoPerfData createPerfData(String label, String serverName, long now, String serviceName, String value) {
        DtoPerfData perf = new DtoPerfData();
        perf.setAppType("NAGIOS");
        perf.setLabel(label);
        perf.setServerName(serverName);
        perf.setServerTime(now);
        perf.setServiceName(serviceName);
        perf.setValue(value);
        return perf;
    }

    @Test
    public void testPerfDataTags() throws Exception {
        if (serverDown) return;
        DtoPerfDataList perfs = new DtoPerfDataList();
        DtoPerfData perf = new DtoPerfData();
        perf.setAppType("VEMA");
        perf.setCritical("500");
        perf.setLabel("CPU-Used");
        perf.setServerName("localhost");
        perf.setServerTime(new Date().getTime() / 1000);
        perf.setServiceName("syn.host.cpu.used");
        perf.setValue("400");
        perf.setWarning("300");
        perf.setTagNames(Arrays.asList(new String[]{"tag0", "tag1", "tag2"}));
        perf.setTagValues(Arrays.asList(new String[]{"australia", "sydney", "cronulla"}));
        perfs.add(perf);
        PerfDataClient client = new PerfDataClient(getDeploymentURL());
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = client.post(perfs);
        assert 1 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert (result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
    }
}
