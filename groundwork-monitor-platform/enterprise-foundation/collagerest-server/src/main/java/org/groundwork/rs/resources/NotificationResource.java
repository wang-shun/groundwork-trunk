package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.actions.ActionService;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.rs.dto.DtoHostNotification;
import org.groundwork.rs.dto.DtoHostNotificationList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoServiceNotification;
import org.groundwork.rs.dto.DtoServiceNotificationList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/notifications")
public class NotificationResource extends AbstractResource {
    public static final String RESOURCE_PREFIX = "/notifications/";
    static final String NOMA_ERROR = "NOMA-Error: ";
    static final String NOMA_HOST_ACTION = "Noma Notify For Host";
    static final String NOMA_SERVICE_ACTION = "Noma Notify For Service";
    protected static Log log = LogFactory.getLog(NotificationResource.class);

    @POST
    @Path("/hosts")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults notifyHosts(DtoHostNotificationList notifications) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /notifications/hosts with %d notifications",
                    (notifications == null) ? 0 : notifications.size()));
        }
        if (notifications == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Notification list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("HostNotification", DtoOperationResults.UPDATE);
        if (notifications.size() == 0) {
            return results;
        }
        List<String> errors = new ArrayList<>();
        for (DtoHostNotification dto : notifications.getNotifications()) {
            String entity = (dto.getHostName() == null) ? "(unknown host)" : dto.getHostName();
            try {
                cleanupHostData(dto);
                Map<String, String> nomaPayload = buildHostNomaNotification(dto);
                String result = performAction(nomaPayload, NOMA_HOST_ACTION);
                if (result.startsWith(NOMA_ERROR)) {
                    results.fail(entity, result);
                    errors.add(result);
                }
                else {
                    results.success(entity, result);
                }
            } catch (Exception e) {
                log.error("Unexpected exception in NOMA processing", e);
                results.fail(entity, e.getMessage());
            }
        }
        if (errors.size() > 0) {
            log.error("Host Notification: errors writing notifications: " + errors.get(0));
        }
        return results;
    }

    @POST
    @Path("/services")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults notifyServices(DtoServiceNotificationList notifications) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /notifications/services with %d notifications",
                    (notifications == null) ? 0 : notifications.size()));
        }
        if (notifications == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Notification list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("ServiceNotification", DtoOperationResults.UPDATE);
        if (notifications.size() == 0) {
            return results;
        }
        List<String> errors = new ArrayList<>();
        for (DtoServiceNotification dto : notifications.getNotifications()) {
            String entity = (dto.getHostName() == null) ? "" : dto.getHostName();
            entity = entity +  ":" + ((dto.getServiceDescription() == null) ? "" : dto.getServiceDescription());
            try {
                cleanupServiceData(dto);
                Map<String, String> nomaPayload = buildServiceNomaNotification(dto);
                String result = performAction(nomaPayload, NOMA_SERVICE_ACTION);
                if (result.startsWith(NOMA_ERROR)) {
                    results.fail(entity, result);
                    errors.add(result);
                }
                else {
                    results.success(entity, result);
                }
            } catch (Exception e) {
                log.error("Unexpected exception in NOMA processing", e);
                results.fail(entity, e.getMessage());
            }
        }
        if (errors.size() > 0) {
            log.error("Service Notification: errors writing notifications: " + errors.get(0));
        }
        return results;
    }

    private String performAction(Map<String, String> parameters, String actionName) {
        String response = null;
        CollageFactory _collageFactory = CollageFactory.getInstance();
        ActionService service = _collageFactory.getActionService();
        FoundationQueryList list = service.getActionByApplicationType("NOMA", false);
        List<Action> results = list.getResults();
        int actionID = -1;
        for (Action action : results) {
            if (action.getName().equalsIgnoreCase(actionName)) {
                actionID = action.getActionId();
                break;
            }
        }
        List<ActionPerform> listActionPerforms = new ArrayList<ActionPerform>();
        listActionPerforms.add(new ActionPerform(actionID, parameters));
        try {
            List<ActionReturn> returns = service.performActions(listActionPerforms);
            ActionReturn actReturn = returns.get(0);
            if (actReturn.getReturnCode() != null && actReturn.getReturnCode().equals(ActionReturn.CODE_SUCCESS)) {
                return (actReturn.getReturnValue() != null) ? actReturn.getReturnValue() : "Notified OK";
            }
            return NOMA_ERROR + actReturn.getReturnValue() + ", code: " + actReturn.getReturnCode();
        } catch (CollageException e) {
            log.error("Error in NOMA Host Action", e);
            return NOMA_ERROR + e.getMessage();
        }
    }


    protected Map<String, String> buildHostNomaNotification(DtoHostNotification noma) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("-c", "-c");
        parameters.put("notifyType", "h");
        if (noma.getHostState() != null) {
            parameters.put("-s", "-s");
            parameters.put("hoststate", noma.getHostState());
        }
        if (noma.getHostName() != null) {
            parameters.put("-H", "-H");
            parameters.put("hostname", noma.getHostName());
        }
        if (noma.getHostGroupNames() != null) {
            parameters.put("-G", "-G");
            parameters.put("hostgroupnames", noma.getHostGroupNames());
        }
        if (noma.getNotificationType() != null) {
            parameters.put("-n", "-n");
            parameters.put("notificationtype", noma.getNotificationType());
        }
        if (noma.getHostAddress() != null) {
            parameters.put("-i", "-i");
            parameters.put("hostaddress", noma.getHostAddress());
        }
        if (noma.getHostOutput() != null) {
            parameters.put("-o", "-o");
            parameters.put("hostoutput", noma.getHostOutput());
        }
        if (noma.getCheckDateTime() != null) {
            parameters.put("-t", "-t");
            parameters.put("shortdatetime", noma.getCheckDateTime());
        }
        if (noma.getHostNotificationId() != null) {
            parameters.put("-u", "-u");
            parameters.put("hostnotificationid", noma.getHostNotificationId());
        }
        if (noma.getNotificationAuthOrAlias() != null) {
            parameters.put("-A", "-A");
            parameters.put("notificationauthoralias", noma.getNotificationAuthOrAlias());
        }
        if (noma.getNotificationComment() != null) {
            parameters.put("-C", "-C");
            parameters.put("notificationcomment", noma.getNotificationComment());
        }
        if (noma.getNotificationRecipients() != null) {
            parameters.put("-R", "-R");
            parameters.put("notificationrecipients", noma.getNotificationRecipients());
        }
        return parameters;
    }

    protected Map<String, String> buildServiceNomaNotification(DtoServiceNotification noma) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("-c", "-c");
        parameters.put("notifyType", "s");
        if (noma.getServiceState() != null) {
            parameters.put("-s", "-s");
            parameters.put("servicestate", noma.getServiceState());
        }
        if (noma.getHostName() != null) {
            parameters.put("-H", "-H");
            parameters.put("hostname", noma.getHostName());
        }
        if (noma.getHostGroupNames() != null) {
            parameters.put("-G", "-G");
            parameters.put("hostgroupnames", noma.getHostGroupNames());
        }
        if (noma.getServiceGroupNames() != null) {
            parameters.put("-E", "-E");
            parameters.put("servicegroupnames", noma.getServiceGroupNames());
        }
        if (noma.getServiceDescription() != null) {
            parameters.put("-S", "-S");
            parameters.put("servicedescription", noma.getServiceDescription());
        }
        if (noma.getServiceOutput() != null) {
            parameters.put("-o", "-o");
            parameters.put("serviceoutput", noma.getServiceOutput());
        }
        if (noma.getNotificationType() != null) {
            parameters.put("-n", "-n");
            parameters.put("notificationtype", noma.getNotificationType());
        }
        if (noma.getHostAlias() != null) {
            parameters.put("-a", "-a");
            parameters.put("hostalias", noma.getHostAlias());
        }
        if (noma.getHostAddress() != null) {
            parameters.put("-i", "-i");
            parameters.put("hostaddress", noma.getHostAddress());
        }
        if (noma.getCheckDateTime() != null) {
            parameters.put("-t", "-t");
            parameters.put("shortdatetime", noma.getCheckDateTime());
        }
        if (noma.getServiceNotificationId() != null) {
            parameters.put("-u", "-u");
            parameters.put("servicenotificationid", noma.getServiceNotificationId());
        }
        if (noma.getNotificationAuthOrAlias() != null) {
            parameters.put("-A", "-A");
            parameters.put("notificationauthoralias", noma.getNotificationAuthOrAlias());
        }
        if (noma.getNotificationComment() != null) {
            parameters.put("-C", "-C");
            parameters.put("notificationcomment", noma.getNotificationComment());
        }
        if (noma.getNotificationRecipients() != null) {
            parameters.put("-R", "-R");
            parameters.put("notificationrecipients", noma.getNotificationRecipients());
        }
        return parameters;
    }

    protected void cleanupHostData(DtoHostNotification noma) {
        // Required fields
        // if (noma.getHostState() == null) {
        // if (noma.getHostName() == null) {
        // if (noma.getHostOutput() == null) {
        if (noma.getHostGroupNames() == null) {
            noma.setHostGroupNames("");
        }
        if (noma.getNotificationType() == null) {
            noma.setNotificationType("");
        }
        if (noma.getHostAddress() == null) {
            noma.setHostAddress("");
        }
        if (noma.getCheckDateTime() == null) {
            noma.setCheckDateTime("");
        }
        if (noma.getHostNotificationId() == null) {
            noma.setHostNotificationId("");
        }
        if (noma.getNotificationAuthOrAlias() == null) {
            noma.setNotificationAuthOrAlias("");
        }
        if (noma.getNotificationComment() == null) {
            noma.setNotificationComment("");
        }
        if (noma.getNotificationRecipients() == null) {
            noma.setNotificationRecipients("");
        }
    }

    protected void cleanupServiceData(DtoServiceNotification noma) {
        // Required
        // if (noma.getServiceState() != null) {
        // if (noma.getHostName() != null) {
        // if (noma.getServiceDescription() != null) {
        // if (noma.getServiceOutput() != null) {
        if (noma.getHostGroupNames() == null) {
            noma.setHostGroupNames("");
        }
        if (noma.getServiceGroupNames() == null) {
            noma.setServiceGroupNames("");
        }
        if (noma.getNotificationType() == null) {
            noma.setNotificationType("");
        }
        if (noma.getHostAlias() == null) {
            noma.setHostAlias("");
        }
        if (noma.getHostAddress() == null) {
            noma.setHostAddress("");
        }
        if (noma.getCheckDateTime() == null) {
            noma.setCheckDateTime("");
        }
        if (noma.getServiceNotificationId() == null) {
            noma.setServiceNotificationId("");
        }
        if (noma.getNotificationAuthOrAlias() == null) {
            noma.setNotificationAuthOrAlias("");
        }
        if (noma.getNotificationComment() == null) {
            noma.setNotificationComment("");
        }
        if (noma.getNotificationRecipients() == null) {
            noma.setNotificationRecipients("");
        }
    }

}
