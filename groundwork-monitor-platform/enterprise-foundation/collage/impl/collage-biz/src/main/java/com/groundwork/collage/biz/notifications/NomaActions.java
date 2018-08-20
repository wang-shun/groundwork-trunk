package com.groundwork.collage.biz.notifications;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.actions.ActionService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FoundationQueryList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NomaActions {

    public static final String NOMA_ERROR = "NOMA-Error: ";
    public static final String NOMA_HOST_ACTION = "Noma Notify For Host";
    public static final String NOMA_SERVICE_ACTION = "Noma Notify For Service";
    public static Log log = LogFactory.getLog(NomaActions.class);

    public static String performAction(Map<String, String> parameters, String actionName) {
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
            //return NOMA_ERROR + e.getMessage();
            throw new BusinessServiceException(e);
        }
    }


    public static Map<String, String> buildHostNomaNotification(NomaHostNotification noma) {
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

    public static Map<String, String> buildServiceNomaNotification(NomaServiceNotification noma) {
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

    public static void cleanupHostData(NomaHostNotification noma) {
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

    public static void cleanupServiceData(NomaServiceNotification noma) {
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
