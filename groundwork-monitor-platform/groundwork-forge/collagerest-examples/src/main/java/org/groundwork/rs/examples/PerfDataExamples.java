package org.groundwork.rs.examples;

import org.groundwork.rs.client.PerfDataClient;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPerfData;
import org.groundwork.rs.dto.DtoPerfDataList;

public class PerfDataExamples {

    private final FoundationConnection connection;

    public PerfDataExamples(FoundationConnection connection) {
        this.connection = connection;
    }

    public void createPerformanceData() {
        PerfDataClient performanceClient = new PerfDataClient(connection.getDeploymentUrl());
        DtoPerfDataList performanceData = new DtoPerfDataList();
        DtoPerfData data = new DtoPerfData();
        data.setServiceName("local_load");
        data.setServerName("localhost");
        data.setValue("1");
        data.setWarning("20");
        data.setCritical("30");
        data.setAppType("NAGIOS");
        performanceData.add(data);

        DtoOperationResults results = performanceClient.post(performanceData);
        if (connection.isEnableAsserts()) {
            assert 1 == results.getCount();
            for (DtoOperationResult result : results.getResults()) {
                assert (result.getStatus().equals(DtoOperationResult.SUCCESS));
            }
        }
    }


}
