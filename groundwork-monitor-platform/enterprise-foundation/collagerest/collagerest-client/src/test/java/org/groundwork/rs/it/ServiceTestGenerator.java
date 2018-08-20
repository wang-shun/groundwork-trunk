package org.groundwork.rs.it;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ServiceTestGenerator extends IntegrationTestGenerator{
    
    public final static String APP_TYPE = "VEMA";
    public final static String MONITOR_STATUS = MonitorStatusBubbleUp.OK;
    public final static String STATE_TYPE = "HARD";
    public final static String LAST_HARD_STATE = MonitorStatusBubbleUp.PENDING;
    public final static String CHECK_TYPE = "ACTIVE";

    public static DtoServiceList buildServiceInserts(IntegrationTestContext<DtoService> context) {
        DtoServiceList services = new DtoServiceList();
        int max =  context.getStart() + context.getCount();
        int baseIndex = 0;
        for (int ix = context.getStart(); ix < max; ix++) {
            DtoService service = new DtoService();
            String index = String.format(FORMAT_NUMBER_SUFFIX, ix);
            String serviceName = context.getPrefix() + context.getDelimiter() + index;
            service.setDescription(serviceName);
            service.setHostName(context.getOwner());
            service.setAppType(APP_TYPE);
            if (context.getMonitorStatuses() != null) {
                service.setMonitorStatus(context.getMonitorStatuses()[baseIndex]);
            }
            else {
                service.setMonitorStatus(MONITOR_STATUS);
            }
            Date[] lastCheckTimes = (Date[])context.getValuesForProperty("LastCheckTime");
            if (lastCheckTimes != null) {
                service.setLastCheckTime(lastCheckTimes[baseIndex]);
                service.setNextCheckTime(new Date(lastCheckTimes[baseIndex].getTime() + 60000L));
            }
            else {
                service.setLastCheckTime(dateWithoutMilliseconds(new Date()));
                service.setNextCheckTime(dateWithoutMilliseconds(new Date(service.getLastCheckTime().getTime() + 60000L)));
            }
            service.setLastHardState(LAST_HARD_STATE);
            service.setStateType(STATE_TYPE);
            service.setCheckType(CHECK_TYPE);
            service.setMetricType("vm");
            service.setDomain("domain");
            service.setAgentId(context.getAgentId());
            // properties
            service.putProperty("Latency", new Double(175.4));
            service.putProperty("LastPluginOutput", "1.output");
            service.putProperty("isProblemAcknowledged", false);
            Double[] executionTimes = (Double[])context.getValuesForProperty("ExecutionTime");
            if (executionTimes != null) {
                service.putProperty("ExecutionTime", executionTimes[baseIndex]);
            }
            baseIndex = baseIndex + 1;
            services.add(service);
            context.addResult(makeServiceKey(service.getHostName(), service.getDescription()), service);
        }
        return services;
    }

    protected static final String SHALLOW_SKIP_INSERTED_SERVICE_FIELDS[] =  {"id", "appTypeDisplayName", "deviceIdentification", "lastPlugInOutput" };

    protected static final String SIMPLE_INCLUDE_INSERTED_SERVICE_FIELDS[] = {"appType", "description", "agentId" };
    // TODO: sync also returns services and 1 properties: Notes
    protected static final String SYNC_INCLUDE_INSERTED_SERVICE_FIELDS[] =  {"appType", "description", "agentId" };

    public static void assertServices(List<DtoService> dtoServices, IntegrationTestContext<DtoService> context) {
        assertServices(dtoServices, context, DtoDepthType.Shallow);
    }

    public static void assertServices(List<DtoService> dtoServices, IntegrationTestContext<DtoService> context, DtoDepthType depthType) {
        for (DtoService service : dtoServices) {
            assertService(service, context, depthType);
        }
    }

    public static void assertService(DtoService service, IntegrationTestContext<DtoService> context) {
        assertService(service, context, null);
    }

    public static void assertService(DtoService service, IntegrationTestContext<DtoService> context, DtoDepthType depthType) {
        DtoService serviceBefore = context.lookupResult(makeServiceKey(service.getHostName(), service.getDescription()));
        if (serviceBefore == null && context.getSkipUnmatchedResults()) {
            return;
        }
        assertThat(serviceBefore).isNotNull();
        String comment = String.format("Comparing Service %s", service.getDescription());
        if (depthType == null) {
            depthType = DtoDepthType.Shallow;
        }
        switch (depthType) {  // TODO: clients don't support depth
            case Simple:
                assertThat(service).as(comment).isEqualToComparingOnlyGivenFields(SIMPLE_INCLUDE_INSERTED_SERVICE_FIELDS);
                break;
            case Shallow:
                assertThat(service).as(comment).isEqualToIgnoringGivenFields(serviceBefore, SHALLOW_SKIP_INSERTED_SERVICE_FIELDS);
                break;
            case Sync:
                assertThat(service).as(comment).isEqualToComparingOnlyGivenFields(SYNC_INCLUDE_INSERTED_SERVICE_FIELDS);
                break;
            case Deep:
            case Full:
                fail("not valid depths for service");
                break;
        }
    }

    public static String makeServiceKey(String host, String service) {
        return host + ":" + service;
    }

    public static List<String> reduceToNames(List<DtoService> services) {
        List<String> names = new ArrayList<>();
        for (DtoService service : services) {
            names.add(service.getDescription());
        }
        return names;
    }

    /*
    TODO: see AbstractRestTask.java, stripping off milliseconds
       DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     */
    public static Date dateWithoutMilliseconds(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.clear(Calendar.MILLISECOND);
        return instance.getTime();
    }


}
