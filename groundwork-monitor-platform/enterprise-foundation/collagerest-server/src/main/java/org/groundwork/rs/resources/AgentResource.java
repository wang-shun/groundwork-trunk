package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.rs.dto.DtoOperationResults;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/agents")
public class AgentResource extends AbstractResource {
    public static final String RESOURCE_PREFIX = "/agents/";
    protected static Log log = LogFactory.getLog(AgentResource.class);

    @DELETE
    @Path("/{agentId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteAllAgentEntities(@PathParam("agentId") String agentId) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE by /agents for %s", agentId));
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        HostService hostService =  CollageFactory.getInstance().getHostService();
        HostGroupService hostGroupService =  CollageFactory.getInstance().getHostGroupService();
        StatusService statusService =  CollageFactory.getInstance().getStatusService();
        QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();

        DtoOperationResults results = new DtoOperationResults("Agent", DtoOperationResults.DELETE);

        // delete by agent id on hosts
        String query = String.format("agentId = '%s'", agentId);
        List<Host> hosts = null;
        QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
        if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
        hosts = hostService.queryHosts(translation.getHql(), translation.getCountHql(), -1, -1).getResults();
        for (Host host : hosts) {
            String name = host.getHostName();
            try {
                Integer id = admin.removeHost(name);
                if (id == null) {
                    results.fail(name, "Host " + name + " not found, cannot delete.");
                }
                else {
                    results.success(name, "Host deleted.");
                }
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove host: %s. %s", name, e.getMessage()), e);
                results.fail(name, e.toString());
            }
        }

        // delete by agent id on host group
        List<HostGroup> hostGroups = null;
        translation = queryTranslator.translate(query, QueryTranslator.HOSTGROUP_KEY);
        if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
        hostGroups = hostGroupService.queryHostGroups(translation.getHql(), translation.getCountHql(), -1, -1).getResults();
        for (HostGroup hostGroup : hostGroups) {
            String name = hostGroup.getName();
            try {
                Integer id = admin.removeHostGroup(name);
                if (id != null) {
                    admin.removeCategoryEntity(CategoryService.ENTITY_TYPE_CODE_HOSTGROUP, id);
                }
                if (id == null) {
                    results.fail(name, "Host Group " + name + " not found, cannot delete.");
                }
                else {
                    results.success(name, "Host Group deleted.");
                }
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove host group: %s. %s", name, e.getMessage()), e);
                results.fail(name, e.toString());
            }
        }

        // delete by agent id on service
        List<ServiceStatus> services = null;
        translation = queryTranslator.translate(query, QueryTranslator.SERVICE_KEY);
        if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
        services = statusService.queryServiceStatus(translation.getHql(), translation.getCountHql(), -1, -1).getResults();
        for (ServiceStatus service : services) {
            int id = service.getServiceStatusId();
            String name = String.format("%s-%s", service.getHost().getHostName(), service.getServiceDescription());
            try {
                admin.removeService(id);
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove service: %s. %s", name, e.getMessage()), e);
                results.fail(name, e.toString());
            }
        }
        stopMetricsTimer(timer);
        return results;
    }



}
