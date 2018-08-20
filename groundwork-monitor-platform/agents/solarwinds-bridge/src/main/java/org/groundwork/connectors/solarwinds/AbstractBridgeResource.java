package org.groundwork.connectors.solarwinds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.connectors.solarwinds.gwos.GroundworkService;
import org.groundwork.connectors.solarwinds.monitor.BridgeStatusService;
import org.groundwork.connectors.solarwinds.status.MonitorStatus;
import org.groundwork.connectors.solarwinds.status.OperationalStatus;
import org.groundwork.connectors.solarwinds.status.SeverityStatus;
import org.groundwork.rs.client.PerfDataClient;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPerfData;
import org.groundwork.rs.dto.DtoPerfDataList;
import org.joda.time.DateTime;

import javax.servlet.ServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AbstractBridgeResource {

    protected static Log log = LogFactory.getLog(AbstractBridgeResource.class);

    protected static final String SW_STATUS_POSTFIX = ", SW_Status=";
    protected static final String DROPPING_REQUEST_NO_HOST_PROVIDED = "Dropping request. No host provided";
    protected static final String DROPPING_REQUEST_NO_SERVICE_PROVIDED = "Dropping request. No service provided";

    public final static String PARAM_AGENT_ID = "AgentID";
    public final static String PARAM_TIMESTAMP = "TimeStamp";
    public final static String PARAM_HOST = "Host";
    public final static String PARAM_STATUS = "State";
    public final static String PARAM_MESSAGE = "Message";
    public final static String PARAM_PERFORMANCE = "Perf";
    public final static String PARAM_IP = "IP";
    public final static String PARAM_SERVICE = "Service";
    public final static String PARAM_HOST_GROUP = "Hostgroups";

    public static final String WEB_APPLICATION_EXCEPTION = "Web application exception: ";
    public static final String UNEXPECTED_EXCEPTION = "Unexpected exception: ";
    public static final String SOLAR_WIND_DATE_FORMAT = "MM/dd/yyyy hh:mm aa";

    public static final String NOTIFICATIONTYPE_PROBLEM = "PROBLEM";
    public static final String NOTIFICATIONTYPE_RECOVERY = "RECOVERY";
    public static final String GWOS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected Date parseDate(String timeStamp) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(SOLAR_WIND_DATE_FORMAT);
            return dateFormat.parse(timeStamp);
        }
        catch (Exception e1) {
            try {
                return DateTime.parse(timeStamp).toDate();
            }
            catch (Exception e2) {
                return new Date();
            }
        }
    }

    protected String makeHostGroupsList(List<DtoHostGroup> groups) {
        StringBuffer result = new StringBuffer("");
        if (groups != null) {
            int count = 0;
            for (DtoHostGroup group : groups) {
                if (count > 0)
                    result.append(",");
                result.append(group.getName());
                count++;
            }
        }
        return result.toString();
    }

    protected DtoHostGroupList parseHostGroups(String commaSeparated, DtoHost dtoHost) {
        String [] items = commaSeparated.split(",");
        DtoHostGroupList groups = new DtoHostGroupList();
        for (String name : items) {
            name = name.trim();
            DtoHostGroup hostGroup = new DtoHostGroup();
            hostGroup.setName(name);
            hostGroup.setDescription(name);
            hostGroup.setAppType(dtoHost.getAppType());
            hostGroup.setAgentId(dtoHost.getAgentId());
            DtoHost host = new DtoHost();
            host.setHostName(dtoHost.getHostName());
            hostGroup.addHost(host);
            groups.add(hostGroup);
        }
        return groups;
    }

    protected String getRemoteAddress(ServletRequest request, String defaultName) {
        String result = null;
        try {
            result = request.getRemoteAddr();
        }
        catch (Exception e) {
            result = null;
        }
        if (result == null) {
            try {
                result = request.getRemoteHost();
            }
            catch (Exception e) {
                result = null;
            }
            if (result == null) {
                return defaultName;
            }
        }
        return result;
    }

    protected void sendPerformanceData(String hostName, String serviceName, String perfData, String agentID) {
        if (serviceName != null)
            serviceName = "";
        DtoPerfDataList perfs = new DtoPerfDataList();
        DtoPerfData perf = new DtoPerfData();
        perf.setAppType(SolarWindsConfiguration.instance().getAppType());
        perf.setLabel(serviceName);
        perf.setServerName(hostName);
        perf.setServerTime(new Date().getTime()/1000L);
        perf.setServiceName(serviceName);
        perf.setValue(perfData);
        // TODO: should we be setting these?
        //perf.setWarning("300");
        //perf.setCritical("500");
        perfs.add(perf);
        PerfDataClient client = GroundworkService.getPerfDataClient();
        DtoOperationResults results = client.post(perfs);
        for (DtoOperationResult result : results.getResults()) {
            if (!result.getStatus().equals(DtoOperationResult.SUCCESS)) {
                String errorMessage = String.format("failed to send performance data for host %s, service %s, message: %s", hostName, serviceName, perfData);
                log.error(errorMessage);
                BridgeStatusService.logBridgeStatus(errorMessage, agentID, MonitorStatus.CRITICAL, SeverityStatus.CRITICAL, OperationalStatus.OPEN);

            }
        }
        if (results.getFailed() == 0 && log.isDebugEnabled()) {
            log.debug(String.format("sent performance data for host %s, service %s, message: %s", hostName, serviceName, perfData));
        }
    }

    public static String nowTime() {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(GWOS_DATE_FORMAT);
        return sdf.format(now).toString();
    }

    protected String formatPluginOutput(DtoHost host, String message) {
        return String.format("Status %s %s / %s",
                (host.getMonitorStatus() == null) ? "" : host.getMonitorStatus(),
                (host.getLastCheckTime() == null) ? "" : host.getLastCheckTime(),
                (message == null) ? "" : message);
    }


}
