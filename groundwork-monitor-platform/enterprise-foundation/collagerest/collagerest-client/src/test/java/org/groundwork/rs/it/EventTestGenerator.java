package org.groundwork.rs.it;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;

import java.util.Date;

import static org.groundwork.rs.it.ServiceTestGenerator.makeServiceKey;

public class EventTestGenerator extends IntegrationTestGenerator {

    public static DtoEventList buildEventInserts(IntegrationTestContext<DtoEvent> context) {
        DtoEventList events = new DtoEventList();
        int max =  context.getStart() + context.getCount();
        int baseIndex = 0;
        long firstInsertDateMillis = System.currentTimeMillis()+1000L;
        for (int ix = context.getStart(); ix < max; ix++) {
            DtoEvent event = new DtoEvent();
            if (context.getMonitorStatuses() != null) {
                event.setMonitorStatus(context.getMonitorStatuses()[baseIndex]);
            } else {
                event.setMonitorStatus(MonitorStatusBubbleUp.PENDING);
            }
            if (context.getOwner().contains(":")) {
                event.setHost(context.getOwner().split(":")[0]);
                event.setService(context.getOwner().split(":")[1]);
                event.setTextMessage(String.format("Test %s:%s %s %d", event.getHost(), event.getService(),
                        event.getMonitorStatus(), ix));
            } else {
                event.setHost(context.getOwner());
                event.setTextMessage(String.format("Test %s %s %d", event.getHost(), event.getMonitorStatus(), ix));
            }
            event.setDevice(event.getHost());
            event.setAppType("NAGIOS");
            event.setSeverity(String.format("ACKNOWLEDGEMENT (%s)", event.getMonitorStatus()));
            event.setApplicationSeverity(event.getSeverity());
            event.setPriority("5");
            event.setTypeRule("UNDEFINED");
            event.setComponent("UNDEFINED");
            event.setOperationStatus("OPEN");
            event.setFirstInsertDate(new Date(firstInsertDateMillis+1000L*baseIndex));
            event.setLastInsertDate(new Date(firstInsertDateMillis+1000L*(baseIndex+1)));
            event.setReportDate(event.getFirstInsertDate());
            baseIndex = baseIndex + 1;
            events.add(event);
            context.addResult(makeServiceKey(event.getHost(), event.getService()), event);
        }
        return events;
    }
}
