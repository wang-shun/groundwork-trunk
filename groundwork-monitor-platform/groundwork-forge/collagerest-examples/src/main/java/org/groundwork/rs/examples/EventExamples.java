package org.groundwork.rs.examples;

import org.groundwork.rs.client.EventClient;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;

public class EventExamples {

    private final FoundationConnection connection;

    public EventExamples(FoundationConnection connection) {
        this.connection = connection;
    }

    public void createEvents() {
        EventClient client = new EventClient(connection.getDeploymentUrl());
        DtoOperationResults results = client.post(buildEventUpdate());
        if (connection.isEnableAsserts()) assert 1 == results.getCount().intValue();
        String entity = null;
        for (DtoOperationResult result : results.getResults()) {
            if (connection.isEnableAsserts()) assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
            entity = result.getEntity();
        }
        // assert data written
        DtoEvent event = client.lookup(entity);
        if (connection.isEnableAsserts()) assert(1 == event.getMsgCount().intValue());
        if (connection.isEnableAsserts()) assertEventWritten(event);

        // repeat should update same entity
        results = client.post(buildEventUpdate());
        if (connection.isEnableAsserts()) assert(1 == results.getCount().intValue());
        DtoOperationResult result = results.getResults().get(0);
        if (connection.isEnableAsserts()) assert (entity == result.getEntity());
        if (connection.isEnableAsserts()) assert (DtoOperationResult.SUCCESS == result.getStatus());

        // assert data written second time
        event = client.lookup(entity);
        if (connection.isEnableAsserts()) assert (2 == event.getMsgCount().intValue());
        if (connection.isEnableAsserts()) assertEventWritten(event);

        // reset data for next test
        client.delete(entity);

        // test its deleted
        event = client.lookup(entity);
        if (connection.isEnableAsserts())  assert (null == event);
    }

    public DtoEventList buildEventUpdate() {
        DtoEventList events = new DtoEventList();
        DtoEvent event = new DtoEvent();
        event.setConsolidationName("NAGIOSEVENT");
        event.setMonitorServer("localhost");
        event.setMonitorStatus("UP");
        event.setService("local_load");
        event.setAppType("NAGIOS");
        event.setDevice("127.0.0.1");
        event.setSeverity("SERIOUS");
        event.setTextMessage("This is a serious Nagios Message on Device 127.0.0.1");
        event.setOperationStatus("OPEN");
        event.setHost("localhost");
        event.setReportDate(CollageClientUtils.parseDate("2013-06-02 10:55:32.943"));
        event.putProperty("Latency", new Double(125.31));
        event.putProperty("UpdatedBy", "UnitTester");
        event.putProperty("Comments", "This is a test.");
        event.setErrorType("MyError");
        event.setComponent("MySubComponent");
        events.add(event);
        return events;
    }

    // Optional Assertions

    public void assertEventWritten(DtoEvent event) {
        assert("UP" == event.getMonitorStatus());
        assert("local_load" == event.getService());
        assert("NAGIOS" == event.getAppType());
        assert("127.0.0.1" == event.getDevice());
        assert("SERIOUS" == event.getSeverity());
        assert("This is a serious Nagios Message on Device 127.0.0.1" == event.getTextMessage());
        assert("OPEN" == event.getOperationStatus());
        assert("localhost" == event.getHost());
        assert("125.31" == event.getProperty("Latency"));
        assert("UnitTester" == event.getProperty("UpdatedBy"));
        assert("This is a test." == event.getProperty("Comments"));
        assert("MyError" == event.getProperty("ErrorType"));
        assert("MySubComponent" == event.getProperty("SubComponent"));
    }

}
