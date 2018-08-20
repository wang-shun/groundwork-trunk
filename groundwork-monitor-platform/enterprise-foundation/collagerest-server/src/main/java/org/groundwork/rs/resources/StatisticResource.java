package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.impl.StateStatistics;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.rs.conversion.StateStatisticConverter;
import org.groundwork.rs.dto.DtoAvailability;
import org.groundwork.rs.dto.DtoStateStatistic;
import org.groundwork.rs.dto.DtoStateStatisticList;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Path("/statistics")
public class StatisticResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/statistics/";
    protected static Log log = LogFactory.getLog(StatisticResource.class);

    @GET
    @Path("/hosts/{hostNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatistic getHostStatisticsByNames(@PathParam("hostNames") String hostNames) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /statistics/hosts/%s", hostNames));
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            String names[] = parseNamesArray(hostNames);
            StateStatistics stat = statisticsService.getAllHostStatisticsByNames(names);
            return StateStatisticConverter.convert(stat);
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for host statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/totals/hosts")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatistic getHostTotalStatistics() {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /statistics/hosts");
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            StateStatistics stat = statisticsService.getHostStatisticTotals();
            if (stat == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Stats not found for total hosts.")).build());
            }
            return StateStatisticConverter.convert(stat);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for total host statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/hostgroups/{hostGroupNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatisticList getHostGroupStatisticsByName(@PathParam("hostGroupNames") String hostGroupNames) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /statistics/hostgroups/%s", hostGroupNames));
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            List<String> names = parseNames(hostGroupNames);
            Collection<StateStatistics> stats = statisticsService.getHostStatisticsByHostGroupNames(names);
            List<DtoStateStatistic> dtoStats = new ArrayList<>(stats.size());
            for (StateStatistics stat : stats) {
                if (stat != null) {
                    dtoStats.add(StateStatisticConverter.convert(stat));
                }
            }
            return new DtoStateStatisticList(dtoStats);
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("An error occurred processing request for host group statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/hostgroups")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatisticList getHostGroupStatistics() {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /statistics/hostgroups");
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            Collection<StateStatistics> stats = statisticsService.getAllHostStatistics(); // this groups by host groups
            if (stats == null || stats.size() == 0) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Stats not found for all host groups.")).build());
            }
            List<DtoStateStatistic> dtoStats = new ArrayList<>(stats.size());
            for (StateStatistics stat : stats) {
                if (stat != null) {
                    dtoStats.add(StateStatisticConverter.convert(stat));
                }
            }
            return new DtoStateStatisticList(dtoStats);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for all host statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/services")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatisticList getServiceStatistics() {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /statistics/services");
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            Collection<StateStatistics> stats = statisticsService.getAllServiceStatistics();
            if (stats == null || stats.size() == 0) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Stats not found for services.")).build());
            }
            List<DtoStateStatistic> dtoStats = new ArrayList<>(stats.size());
            for (StateStatistics stat : stats) {
                if (stat != null) {
                    dtoStats.add(StateStatisticConverter.convert(stat));
                }
            }
            return new DtoStateStatisticList(dtoStats);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for services statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/services/hosts/{hostNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatisticList getServiceStatisticsByHostNames(@PathParam("hostNames") String hostNames) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /statistics/services/hosts/%s", hostNames));
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            String names[] = parseNamesArray(hostNames);
            List<DtoStateStatistic> dtoStats = new ArrayList<>(names.length);
            for (String name : names) {
                StateStatistics stat = statisticsService.getServiceStatisticByHostName(name);
                if (stat != null) {
                    dtoStats.add(StateStatisticConverter.convert(stat));
                }
            }
            return new DtoStateStatisticList(dtoStats);
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for service/hosts statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/services/hostgroups/{hostGroupNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatisticList getServiceStatisticsByHostGroupNames(@PathParam("hostGroupNames") String hostGroupNames) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /statistics/services/hostgroups/%s", hostGroupNames));
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            List<String> names = parseNames(hostGroupNames);
            Collection<StateStatistics> stats = statisticsService.getServiceStatisticsByHostGroupNames(names);
            List<DtoStateStatistic> dtoStats = new ArrayList<>(stats.size());
            for (StateStatistics stat : stats) {
                if (stat != null) {
                    dtoStats.add(StateStatisticConverter.convert(stat));
                }
            }
            return new DtoStateStatisticList(dtoStats);
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for service/hostgroups statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/services/servicegroups/{serviceGroupNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatisticList getServiceStatisticsByServiceGroupNames(@PathParam("serviceGroupNames") String serviceGroupNames) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /statistics/services/servicegroups/%s", serviceGroupNames));
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            String names[] = parseNamesArray(serviceGroupNames);
            List<DtoStateStatistic> dtoStats = new ArrayList<>(names.length);
            for (String name : names) {
                StateStatistics stat = statisticsService.getServiceStatisticsByServiceGroupName(name);
                if (stat != null) {
                    dtoStats.add(StateStatisticConverter.convert(stat));
                }
            }
            return new DtoStateStatisticList(dtoStats);
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for service/servicegroups statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/services/servicegroups")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatisticList getServiceStatisticsByServiceGroups() {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /statistics/services/servicegroups");
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            Collection<StateStatistics> stats = statisticsService.getServiceStatisticsForAllServiceGroups();
            List<DtoStateStatistic> dtoStats = new ArrayList<>(stats.size());
            for (StateStatistics stat : stats) {
                if (stat != null) {
                    dtoStats.add(StateStatisticConverter.convert(stat));
                }
            }
            return new DtoStateStatisticList(dtoStats);
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for service/servicegroups statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/totals/services")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoStateStatistic getServiceStatisticsTotals() {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /statistics/services/totals");
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            StateStatistics stat = statisticsService.getServiceStatisticTotals();
            return StateStatisticConverter.convert(stat);
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for service total statistics.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/availability/hosts")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoAvailability getHostAvailabilityForHostGroup(@QueryParam("hostGroup") @DefaultValue("") String hostGroup) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /statistics/availability/hosts?hostGroup=%s", hostGroup));
            }
            if (hostGroup.equals("")) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(String.format("Must specify 'hostGroup' query parameter")).build());
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            double availability = statisticsService.getHostAvailabilityForHostgroup(hostGroup);
            return new DtoAvailability("hosts", "hostGroup", hostGroup, availability);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for host availability.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/availability/services")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoAvailability getServiceAvailability(@QueryParam("hostGroup") @DefaultValue("") String hostGroup,
                                           @QueryParam("serviceGroup") @DefaultValue("") String serviceGroup) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                StringBuffer queries = new StringBuffer();
                queries.append("hostGroup=");
                queries.append(hostGroup);
                queries.append(", serviceGroup=");
                queries.append(serviceGroup);
                log.debug(String.format("processing /GET on /statistics/availability/services (%s) ", queries.toString()));
            }
            if (hostGroup.equals("") && serviceGroup.equals((""))) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(String.format(
                        "Must specify one query parameter: either 'hostGroup' or 'serviceGroup'")).build());
            }
            StatisticsService statisticsService = CollageFactory.getInstance().getStatisticsService();
            if (!hostGroup.equals("")) {
                double availability = statisticsService.getServiceAvailabilityForHostGroup(hostGroup);
                return new DtoAvailability("services", "hostGroup", hostGroup, availability);
            }
            else {
                double availability = statisticsService.getServiceAvailabilityForServiceGroup(serviceGroup);
                return new DtoAvailability("services", "serviceGroup", serviceGroup, availability);
            }
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for service availability.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }


}
