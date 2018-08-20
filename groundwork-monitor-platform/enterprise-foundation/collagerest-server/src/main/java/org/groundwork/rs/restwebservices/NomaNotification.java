package org.groundwork.rs.restwebservices;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.actions.ActionService;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.rs.restwebservices.utils.LoginHelper;
import org.groundwork.rs.restwebservices.utils.ResponseHelper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/noma")
public class NomaNotification {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * Noma Notification method for the host
	 * 
	 * @param username
	 * @param password
	 * @param hostState
	 * @param hostName
	 * @param hostGroupNames
	 * @param notificationType
	 * @param hostAddress
	 * @param hostOutput
	 * @param shortDateTime
	 * @param hostNotificationId
	 * @param notificationAuthOrAlias
	 * @param notificationComment
	 * @param notificationRecipients
	 * @return
	 */
	@POST
	@Path("/notifyHost")
	@Produces("application/xml")
	public String notifyHost(
			@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("hoststate") String hostState,
			@FormParam("hostname") String hostName,
			@FormParam("hostgroupnames") String hostGroupNames,
			@FormParam("notificationtype") String notificationType,
			@FormParam("hostaddress") String hostAddress,
			@FormParam("hostoutput") String hostOutput,
			@FormParam("shortdatetime") String shortDateTime,
			@FormParam("hostnotificationid") String hostNotificationId,
			@FormParam("notificationauthoralias") String notificationAuthOrAlias,
			@FormParam("notificationcomment") String notificationComment,
			@FormParam("notificationrecipients") String notificationRecipients) {

		String response = null;
		if (username == null || password == null) {
			response = ResponseHelper.buildStatus("1",
					"INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		if (!LoginHelper.login(username, password)) {
			response = ResponseHelper.buildStatus("1",
					"INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("-c", "-c");
		parameters.put("notifyType", "h");
		if (hostState != null) {
			parameters.put("-s", "-s");
			parameters.put("hoststate", hostState);
		}
		if (hostName != null) {
			parameters.put("-H", "-H");
			parameters.put("hostname", hostName);
		}
		if (hostGroupNames != null) {
			parameters.put("-G", "-G");
			parameters.put("hostgroupnames", hostGroupNames);
		}
		if (notificationType != null) {
			parameters.put("-n", "-n");
			parameters.put("notificationtype", notificationType);
		}
		if (hostAddress != null) {
			parameters.put("-i", "-i");
			parameters.put("hostaddress", hostAddress);
		}
		if (hostOutput != null) {
			parameters.put("-o", "-o");
			parameters.put("hostoutput", hostOutput);
		}
		if (shortDateTime != null) {
			parameters.put("-t", "-t");
			parameters.put("shortdatetime", shortDateTime);
		}
		if (hostNotificationId != null) {
			parameters.put("-u", "-u");
			parameters.put("hostnotificationid", hostNotificationId);
		}
		if (notificationAuthOrAlias != null) {
			parameters.put("-A", "-A");
			parameters.put("notificationauthoralias", notificationAuthOrAlias);
		}
		if (notificationComment != null) {
			parameters.put("-C", "-C");
			parameters.put("notificationcomment", notificationComment);
		}
		if (notificationRecipients != null) {
			parameters.put("-R", "-R");
			parameters.put("notificationrecipients", notificationRecipients);
		}
		return performAction(parameters, "Noma Notify For Host");
	}

	/**
	 * Noma Notification service for the service
	 * 
	 * @param username
	 * @param password
	 * @param serviceState
	 * @param hostName
	 * @param hostGroupNames
	 * @param serviceGroupNames
	 * @param serviceDescription
	 * @param serviceOutput
	 * @param notificationType
	 * @param hostAlias
	 * @param hostAddress
	 * @param shortDateTime
	 * @param serviceNotificationId
	 * @param notificationAuthOrAlias
	 * @param notificationComment
	 * @param notificationRecipients
	 * @return
	 */
	@POST
	@Path("/notifyService")
	@Produces("application/xml")
	public String notifyService(
			@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("servicestate") String serviceState,
			@FormParam("hostname") String hostName,
			@FormParam("hostgroupnames") String hostGroupNames,
			@FormParam("servicegroupnames") String serviceGroupNames,
			@FormParam("servicedescription") String serviceDescription,
			@FormParam("serviceoutput") String serviceOutput,
			@FormParam("notificationtype") String notificationType,
			@FormParam("hostalias") String hostAlias,
			@FormParam("hostaddress") String hostAddress,
			@FormParam("shortdatetime") String shortDateTime,
			@FormParam("servicenotificationid") String serviceNotificationId,
			@FormParam("notificationauthoralias") String notificationAuthOrAlias,
			@FormParam("notificationcomment") String notificationComment,
			@FormParam("notificationrecipients") String notificationRecipients) {

		String response = null;
		if (username == null || password == null) {
			response = ResponseHelper.buildStatus("1",
					"INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		if (!LoginHelper.login(username, password)) {
			response = ResponseHelper.buildStatus("1",
					"INVALID USERNAME OR PASSWORD");
			return response;
		} // end if

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("-c", "-c");
		parameters.put("notifyType", "s");
		if (serviceState != null) {
			parameters.put("-s", "-s");
			parameters.put("servicestate", serviceState);
		}
		if (hostName != null) {
			parameters.put("-H", "-H");
			parameters.put("hostname", hostName);
		}
		if (hostGroupNames != null) {
			parameters.put("-G", "-G");
			parameters.put("hostgroupnames", hostGroupNames);
		}
		if (serviceGroupNames != null) {
			parameters.put("-E", "-E");
			parameters.put("servicegroupnames", serviceGroupNames);
		}
		if (serviceDescription != null) {
			parameters.put("-S", "-S");
			parameters.put("servicedescription", serviceDescription);
		}
		if (serviceOutput != null) {
			parameters.put("-o", "-o");
			parameters.put("serviceoutput", serviceOutput);
		}
		if (notificationType != null) {
			parameters.put("-n", "-n");
			parameters.put("notificationtype", notificationType);
		}
		if (hostAlias != null) {
			parameters.put("-a", "-a");
			parameters.put("hostalias", hostAlias);
		}
		if (hostAddress != null) {
			parameters.put("-i", "-i");
			parameters.put("hostaddress", hostAddress);
		}
		if (shortDateTime != null) {
			parameters.put("-t", "-t");
			parameters.put("shortdatetime",  shortDateTime);
		}
		if (serviceNotificationId != null) {
			parameters.put("-u", "-u");
			parameters.put("servicenotificationid", serviceNotificationId);
		}
		if (notificationAuthOrAlias != null) {
			parameters.put("-A", "-A");
			parameters.put("notificationauthoralias", notificationAuthOrAlias);
		}
		if (notificationComment != null) {
			parameters.put("-C", "-C");
			parameters.put("notificationcomment", notificationComment);
		}
		if (notificationRecipients != null) {
			parameters.put("-R", "-R");
			parameters.put("notificationrecipients", notificationRecipients);
		}
		return performAction(parameters, "Noma Notify For Service");

	}

	/**
	 * Helper method to perform action
	 * 
	 * @return
	 */
	private String performAction(Map<String, String> parameters,
			String actionName) {
		String response = null;
		CollageFactory _collageFactory = CollageFactory.getInstance();
		ActionService service = _collageFactory.getActionService();
		FoundationQueryList list = service.getActionByApplicationType("NOMA",
				false);
		List<Action> results = list.getResults();
		int actionID = -1;
		for (Action action : results) {
			if (action.getName().equalsIgnoreCase(actionName)) {
				actionID = action.getActionId();
				break;
			} // end if
		} // end for
		List<ActionPerform> listActionPerforms = new ArrayList<ActionPerform>();

		listActionPerforms.add(new ActionPerform(actionID, parameters));
		try {

			List<ActionReturn> returns = service
					.performActions(listActionPerforms);
			ActionReturn actReturn = returns.get(0);
			response = ResponseHelper.buildStatus(actReturn.getReturnCode(),
					actReturn.getReturnValue());
		} catch (CollageException e) {
			response = ResponseHelper.buildStatus("99", e.getMessage());
			log.error(e.getMessage());
		}
		return response;
	}

}
