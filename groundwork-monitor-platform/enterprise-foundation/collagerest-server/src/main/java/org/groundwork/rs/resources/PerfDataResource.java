package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.biz.performance.PerformanceNotification;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.util.DateTime;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.rs.common.GWRestConstants;
import org.groundwork.rs.dto.*;
import org.groundwork.rs.influxdb.InfluxDBClient;
import org.groundwork.rs.opentsdb.OpenTSDBClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/perfdata")
public class PerfDataResource extends AbstractResource {

    protected static Log log = LogFactory.getLog(PerfDataResource.class);

    private PerformanceNotification performance = new PerformanceNotification();
    private DateFormat dateFormat = new SimpleDateFormat(DateTime.DATE_FORMAT);

    private final boolean INFLUX_ENABLED;
    private final boolean NO_WRITERS;
    private final Set<String> LOGPERF_APPS;

    public PerfDataResource() {
        CollageFactory service = CollageFactory.getInstance();
        Properties configuration = service.getFoundationProperties();
        String backend = configuration.getProperty("perfdata.backend.default", "rrd");
        INFLUX_ENABLED = backend.equals("influxdb");
        String writers = configuration.getProperty("perfdata.vema.writers", "");
        NO_WRITERS = StringUtils.isBlank(writers);
        String logPerfAppNames = configuration.getProperty("perfdata.logperf.appnames", "");
        LOGPERF_APPS = new HashSet<>(Arrays.asList(StringUtils.stripAll(StringUtils.split(logPerfAppNames, ","))));
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoPerfDataTimeSeries get(@QueryParam("appType") String appType,
                                     @QueryParam("serverName") String serverName,
                                     @QueryParam("serviceName") String serviceName,
                                     @QueryParam("startTime") @DefaultValue("-1") long startTime,
                                     @QueryParam("endTime") @DefaultValue("-1") long endTime,
                                     @QueryParam("interval") @DefaultValue("-1") long interval) {
        CollageTimer timer = startMetricsTimer();
        // default time related parameters
        if (endTime <= 0L) {
            endTime = System.currentTimeMillis();
        }
        if (startTime <= 0L) {
            startTime = endTime-86400000L;
        }
        if (interval <= 0L) {
            interval = Math.max((long)(((double)(endTime-startTime))/100.0+0.999), 1L);
        }
        // log request
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /perfdata with appType: %s, serverName: %s, serviceName: %s, startTime: %d, endTime: %d, interval: %d",
                    appType, serverName, serviceName, startTime, endTime, interval));
        }
        // validate request
        if (serverName == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("serverName parameter was not provided").build());
        }
        if (serviceName == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("serviceName parameter was not provided").build());
        }

        if (INFLUX_ENABLED) {
            DtoPerfDataTimeSeries dtoPerfDataTimeSeries =
                    InfluxDBClient.query(appType, serverName, serviceName, startTime, endTime, interval);
            if (dtoPerfDataTimeSeries == null || dtoPerfDataTimeSeries.size() == 0) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Performance data not found").build());
            }
            stopMetricsTimer(timer);
            return dtoPerfDataTimeSeries;
        }

        // query OpenTSDB performance data
        try {
            // clean server and service name as written by OpenTSDBPerfDataWriter
            serverName = OpenTSDBClient.cleanNameKey(serverName);
            serviceName = OpenTSDBClient.cleanNameKey(serviceName);
            // query OpenTSDB for app type/server/service data points in time range
            JsonNode openTSDBPerfData = OpenTSDBClient.queryOpenTSDBPerfData(appType, serverName, serviceName,
                    startTime, endTime, interval);
            // translate JSON performance data to performance data time series
            DtoPerfDataTimeSeries dtoPerfDataTimeSeries = new DtoPerfDataTimeSeries(appType, serverName, serviceName,
                    startTime, endTime, interval);
            if (openTSDBPerfData != null) {
                if (!openTSDBPerfData.isArray()) {
                    throw new RuntimeException("metrics performance data expected to be an array");
                }
                for (Iterator<JsonNode> metricsIter = openTSDBPerfData.iterator(); metricsIter.hasNext(); ) {
                    JsonNode metrics = metricsIter.next();
                    if (!metrics.isObject()) {
                        throw new RuntimeException("metric performance data expected to be an object");
                    }
                    JsonNode tags = metrics.get("tags");
                    if ((tags == null) || !tags.isObject()) {
                        throw new RuntimeException("metric.tags performance data required and expected to be an object");
                    }
                    JsonNode dps = metrics.get("dps");
                    if ((dps == null) || !dps.isObject()) {
                        throw new RuntimeException("metric.dps performance data required and expected to be an object");
                    }
                    JsonNode type = tags.get("valuetype");
                    if ((type == null) || !type.isTextual()) {
                        throw new RuntimeException("metric.tags.valuetype performance data required and expected to be text");
                    }
                    String valueType = type.getTextValue();
                    for (Iterator<Map.Entry<String,JsonNode>> dpsIter = dps.getFields(); dpsIter.hasNext(); ) {
                        Map.Entry<String,JsonNode> dpsEntry = dpsIter.next();
                        if (!dpsEntry.getValue().isNumber()) {
                            throw new RuntimeException("metric.dps field value performance data expected to be number");
                        }
                        try {
                            long timestamp = Long.parseLong(dpsEntry.getKey());
                            double value = dpsEntry.getValue().getDoubleValue();
                            dtoPerfDataTimeSeries.add(new DtoPerfDataTimeSeriesValue(valueType, timestamp, value));
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("metric.dps field key performance data expected to be number");
                        }
                    }
                }
            }
            // return not found silently on error or no performance data returned
            if (dtoPerfDataTimeSeries.size() == 0) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("Performance data not found").build());
            }
            return dtoPerfDataTimeSeries;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for performance data.").build());
        } finally {
            OpenTSDBClient.shutdown();
            stopMetricsTimer(timer);
        }
    }


    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults post(DtoPerfDataList perfDataList) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /perfdata with %d entries",
                    (perfDataList == null) ? 0 : perfDataList.size()));
        }
        if (perfDataList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Notification list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("PerfData", DtoOperationResults.UPDATE);

        if (perfDataList.size() == 0) {
            return results;
        }

        // This is a very basic attempt to process data differently based on where it comes from.  Long-term this should
        // most likely come from a proper configuration framework.
        String appName = this.request.getHeader(GWRestConstants.PARAM_GWOS_APP_NAME);
        boolean processLogPerf = LOGPERF_APPS.contains(appName);

        // Write the dtos to influx in a single operation
        if (INFLUX_ENABLED) {
            DtoOperationResults influxResults = InfluxDBClient.write(perfDataList.getPerfDataList());
            // Only capture influx results if influx is the sole writer to avoid double-counting results
            if (NO_WRITERS && !processLogPerf) {
                results = influxResults;
            }
        }

        // Avoid processing perfdata on a per-dto basis unless required
        if (NO_WRITERS && !processLogPerf) {
            if (log.isDebugEnabled()) log.debug("No writers configured");
            stopMetricsTimer(timer);
            return results;
        }

        try {
            if (!performance.canWriteToPerformance())
                throw new BusinessServiceException("Cannot acquire connection to Performance Queue");
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("No Performance Queue available").build());
        }

        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        int count = 0;
        for (DtoPerfData dto : perfDataList.getPerfDataList()) {
            if (dto.getAppType() == null) {
                String message = "No AppType name provided";
                results.fail("appType Unknown", message);
                log.error(message);
                continue;
            }
            if (dto.getServerName() == null && dto.getServiceName() == null) {
                String message = "No Server Name or Service Name name provided";
                results.fail("Server/Service Unknown", message);
                log.error(message);
                continue;
            }
            String entity = safe(dto.getServerName()) + "," + safe(dto.getServiceName()) + "," + safe(dto.getLabel());
            try {
                if (processLogPerf) {
                    double value = Double.parseDouble(dto.getValue());
                    Long msTime = dto.getServerTime() * 1000L;
                    Date checkDate = new Date(msTime);
                    admin.insertPerformanceData(dto.getServerName(), dto.getServiceName(), dto.getLabel(), value, dateFormat.format(checkDate));
                }
                if (!NO_WRITERS) {
                    writeMessage(dto);
                }
                results.success(entity, "OK");
                count++;
            } catch (Exception e) {
                log.error("Unexpected exception in PerfData processing", e);
                results.fail(entity, e.getMessage());
            }
        }
        if (count > 0) {
            try {
                performance.commit();
            } catch (Exception e) {
                throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Could not Commit to Performance Queue").build());
            }
        }

        stopMetricsTimer(timer);
        return results;
    }


    private void writeMessage(DtoPerfData dto) {
        CollageTimer timer = startMetricsTimer();
        // extract message extended tags
        Map<String,String> tags = null;
        if ((dto.getTagNames() != null) && !dto.getTagNames().isEmpty() && (dto.getTagValues() != null) && !dto.getTagValues().isEmpty()) {
            Iterator<String> dtoTagNamesIter = dto.getTagNames().iterator();
            Iterator<String> dtoTagValuesIter = dto.getTagValues().iterator();
            while (dtoTagNamesIter.hasNext() && dtoTagValuesIter.hasNext()) {
                tags = addTag(dtoTagNamesIter.next(), dtoTagValuesIter.next(), tags);
            }
        }
        tags = addTag("component", dto.getComponentTag(), tags);
        tags = addTag("segment", dto.getSegmentTag(), tags);
        tags = addTag("element", dto.getElementTag(), tags);
        tags = addTag("port", dto.getPortTag(), tags);
        tags = addTag("vlan", dto.getVlanTag(), tags);
        tags = addTag("cpu", dto.getCpuTag(), tags);
        tags = addTag("interface", dto.getInterfaceTag(), tags);
        tags = addTag("subinterface", dto.getSubinterfaceTag(), tags);
        tags = addTag("http_method", dto.getHttpMethodTag(), tags);
        tags = addTag("http_code", dto.getHttpCodeTag(), tags);
        tags = addTag("device", dto.getDeviceTag(), tags);
        tags = addTag("what", dto.getWhatTag(), tags);
        tags = addTag("type", dto.getTypeTag(), tags);
        tags = addTag("result", dto.getResultTag(), tags);
        tags = addTag("bin_max", dto.getBinMaxTag(), tags);
        tags = addTag("direction", dto.getDirectionTag(), tags);
        tags = addTag("mtype", dto.getMTypeTag(), tags);
        tags = addTag("unit", dto.getUnitTag(), tags);
        tags = addTag("file", dto.getFileTag(), tags);
        tags = addTag("line", dto.getLineTag(), tags);
        tags = addTag("env", dto.getEnvTag(), tags);
        // write message using performance notification
        performance.writeMessage(dto.getAppType(), dto.getServerName(), dto.getServiceName(), dto.getServerTime(),
                dto.getLabel(), dto.getValue(), dto.getWarning(), dto.getCritical(), tags);
        stopMetricsTimer(timer);
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static Map<String,String> addTag(String tagName, String tagValue, Map<String,String> tags) {
        if ((tagName != null) && (tagName.length() > 0) && (tagValue != null) && (tagName.length() > 0)) {
            if (tags == null) {
                tags = new HashMap<>();
            }
            tags.put(tagName, tagValue);
        }
        return tags;
    }
}
