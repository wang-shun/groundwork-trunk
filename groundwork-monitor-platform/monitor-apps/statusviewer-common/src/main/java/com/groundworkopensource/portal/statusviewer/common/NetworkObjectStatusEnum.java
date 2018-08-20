package com.groundworkopensource.portal.statusviewer.common;

import org.apache.log4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * It represents status of Hosts/Services/Groups.
 * 
 * first field: actual Monitor status returned by web services (except pending
 * state) Second field: path of icon of the node in tree Third field: status of
 * node to display on screen
 * 
 * @author nitin_jadhav
 */
public enum NetworkObjectStatusEnum {
	/**
	 * HOST_DOWN_UNSCHEDULED
	 */
	HOST_DOWN_UNSCHEDULED("UNSCHEDULED DOWN", "/images/host-red.gif",
			"Unscheduled Down", new Color(Integer.parseInt(Constant.RED_HEX,
					Constant.SIXTEEN))),

	/**
	 * HOST_DOWN_SCHEDULED
	 */
	HOST_DOWN_SCHEDULED("SCHEDULED DOWN", "/images/host-orange.gif",
			"Scheduled Down", new Color(Integer.parseInt(Constant.ORAGNE_HEX,
					Constant.SIXTEEN))),
	/**
	 * HOST_WARNING
	 */
	HOST_WARNING("WARNING_HOST", "/images/host-yellow.gif", "Warning",
			new Color(Integer.parseInt(Constant.YELLOW_HEX, Constant.SIXTEEN))),
	/**
	 * HOST_UNREACHABLE
	 */
	HOST_UNREACHABLE("UNREACHABLE", "/images/host-gray.gif", "Unreachable",
			new Color(Integer.parseInt(Constant.GRAY_HEX, Constant.SIXTEEN))),
	/**
	 * HOST_PENDING
	 */
	HOST_PENDING("PENDING_HOST", "/images/host-blue.gif", "Pending", new Color(
			Integer.parseInt(Constant.BLUE_HEX, Constant.SIXTEEN))),
	/**
	 * HOST_UP
	 */
	HOST_UP("UP", "/images/host-green.gif", "Up", new Color(Integer.parseInt(
			Constant.GREEN_HEX, Constant.SIXTEEN))),

	// Ack status for Host
	/**
	 * ACKNOWLEDGEMENT (DOWN)
	 */
	HOST_ACK_DOWN("ACKNOWLEDGEMENT (DOWN)", "/images/host-red.gif",
			"ACKNOWLEDGEMENT (DOWN)", new Color(Integer.parseInt(
					Constant.RED_HEX, Constant.SIXTEEN))),
	/**
	 * ACKNOWLEDGEMENT (DOWN)
	 */
	HOST_ACK_UP("ACKNOWLEDGEMENT (UP)", "/images/host-green.gif",
			"ACKNOWLEDGEMENT (UP)", new Color(Integer.parseInt(
					Constant.GREEN_HEX, Constant.SIXTEEN))),
	/**
	 * ACKNOWLEDGEMENT (UNREACHABLE)
	 */
	HOST_ACK_UNREACHABLE("ACKNOWLEDGEMENT (UNREACHABLE)",
			"/images/host-gray.gif", "ACKNOWLEDGEMENT (UNREACHABLE)",
			new Color(Integer.parseInt(Constant.GRAY_HEX, Constant.SIXTEEN))),
	/**
	 * ACKNOWLEDGEMENT (PENDING)
	 */
	HOST_ACK_PENDING("ACKNOWLEDGEMENT (PENDING)", "/images/host-blue.gif",
			"ACKNOWLEDGEMENT (PENDING)", new Color(Integer.parseInt(
					Constant.BLUE_HEX, Constant.SIXTEEN))),
	/**
	 * HOST_WARNING
	 */
	HOST_ACK_WARNING("ACKNOWLEDGEMENT (WARNING)", "/images/host-yellow.gif",
			"ACKNOWLEDGEMENT (WARNING)", new Color(Integer.parseInt(
					Constant.YELLOW_HEX, Constant.SIXTEEN))),

	// Ack status for Service
	/**
	 * SERVICE_ACK_CRITICAL (DOWN)
	 */
	SERVICE_ACK_CRITICAL("ACKNOWLEDGEMENT (CRITICAL)",
			"/images/service-red.gif", "ACKNOWLEDGEMENT (CRITICAL)", new Color(
					Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_ACK_OK (DOWN)
	 */
	SERVICE_ACK_OK("ACKNOWLEDGEMENT (OK)", "/images/service-green.gif",
			"ACKNOWLEDGEMENT (OK)", new Color(Integer.parseInt(
					Constant.GREEN_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_ACK_UNKNOWN (UNKNOWN)
	 */
	SERVICE_ACK_UNKNOWN("ACKNOWLEDGEMENT (UNKNOWN)",
			"/images/service-gray.gif", "ACKNOWLEDGEMENT (UNKNOWN)", new Color(
					Integer.parseInt(Constant.GRAY_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_ACK_PENDING (PENDING)
	 */
	SERVICE_ACK_PENDING("ACKNOWLEDGEMENT (PENDING)",
			"/images/service-blue.gif", "ACKNOWLEDGEMENT (PENDING)", new Color(
					Integer.parseInt(Constant.BLUE_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_ACK_WARNING
	 */
	SERVICE_ACK_WARNING("ACKNOWLEDGEMENT (WARNING)",
			"/images/service-yellow.gif", "ACKNOWLEDGEMENT (WARNING)",
			new Color(Integer.parseInt(Constant.YELLOW_HEX, Constant.SIXTEEN))),

	/**
	 * SERVICE_CRITICAL_UNSCHEDULED
	 */
	SERVICE_CRITICAL_UNSCHEDULED("UNSCHEDULED CRITICAL",
			"/images/service-red.gif", "Unscheduled Critical", new Color(
					Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_CRITICAL_SCHEDULED
	 */
	SERVICE_CRITICAL_SCHEDULED("SCHEDULED CRITICAL",
			"/images/service-orange.gif", "Scheduled Critical", new Color(
					Integer.parseInt(Constant.ORAGNE_HEX, Constant.SIXTEEN))),

	/**
	 * SERVICE_WARNING
	 */
	SERVICE_WARNING("WARNING", "/images/service-yellow.gif", "Warning",
			new Color(Integer.parseInt(Constant.YELLOW_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_UNKNOWN
	 */
	SERVICE_UNKNOWN("UNKNOWN", "/images/service-gray.gif", "Unknown",
			new Color(Integer.parseInt(Constant.GRAY_HEX, Constant.SIXTEEN))),

	// Note: These are Non-standard entries. based on whether it is
	// host(group)
	// or service(group), re-assign one of these pending entries to your
	// node.

	/**
	 * SERVICE_PENDING
	 */
	SERVICE_PENDING("PENDING_SERVICE", "/images/service-blue.gif", "Pending",
			new Color(Integer.parseInt(Constant.BLUE_HEX, Constant.SIXTEEN))),

	/**
	 * SERVICE_OK
	 */
	SERVICE_OK("OK", "/images/service-green.gif", "Ok", new Color(
			Integer.parseInt(Constant.GREEN_HEX, Constant.SIXTEEN))),

	/**
	 * HOST_GROUP_UP
	 */
	HOST_GROUP_UP("UP", "/images/host-group-green.gif", "Up", new Color(
			Integer.parseInt(Constant.GREEN_HEX, Constant.SIXTEEN))),
	/**
	 * HOST_GROUP_DOWN_UNSCHEDULED
	 */
	HOST_GROUP_DOWN_UNSCHEDULED("UNSCHEDULED DOWN",
			"/images/host-group-red.gif", "Unscheduled Down", new Color(
					Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN))),

	/**
	 * HOST_GROUP_DOWN_SCHEDULED
	 */
	HOST_GROUP_DOWN_SCHEDULED("SCHEDULED DOWN",
			"/images/host-group-orange.gif", "Scheduled Down", new Color(
					Integer.parseInt(Constant.ORAGNE_HEX, Constant.SIXTEEN))),
	/**
	 * HOST_GROUP_WARNING
	 */
	HOST_GROUP_WARNING("WARNING_HOST", "/images/host-group-yellow.gif",
			"Warning", new Color(Integer.parseInt(Constant.YELLOW_HEX,
					Constant.SIXTEEN))),
	/**
	 * HOST_GROUP_UNREACHABLE
	 */
	HOST_GROUP_UNREACHABLE("UNREACHABLE", "/images/host-group-gray.gif",
			"Unreachable", new Color(Integer.parseInt(Constant.GRAY_HEX,
					Constant.SIXTEEN))),

	/**
	 * HOST_GROUP_PENDING
	 */
	HOST_GROUP_PENDING("PENDING_HOST", "/images/host-group-blue.gif",
			"Pending", new Color(Integer.parseInt(Constant.BLUE_HEX,
					Constant.SIXTEEN))),

	//
	/**
	 * CUSTOM_GROUP_UP
	 */
	CUSTOM_GROUP_UP("UP", "/images/customgroup-green.gif", "Up", new Color(
			Integer.parseInt(Constant.GREEN_HEX, Constant.SIXTEEN))),
	/**
	 * CUSTOM_GROUP_DOWN_UNSCHEDULED
	 */
	CUSTOM_GROUP_DOWN_UNSCHEDULED("UNSCHEDULED DOWN",
			"/images/customgroup-red.gif", "Unscheduled Down", new Color(
					Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN))),

	/**
	 * CUSTOM_GROUP_DOWN_SCHEDULED
	 */
	CUSTOM_GROUP_DOWN_SCHEDULED("SCHEDULED DOWN",
			"/images/customgroup-orange.gif", "Scheduled Down", new Color(
					Integer.parseInt(Constant.ORAGNE_HEX, Constant.SIXTEEN))),
	/**
	 * CUSTOM_GROUP_WARNING
	 */
	CUSTOM_GROUP_WARNING("WARNING_HOST", "/images/customgroup-yellow.gif",
			"Warning", new Color(Integer.parseInt(Constant.YELLOW_HEX,
					Constant.SIXTEEN))),
	/**
	 * CUSTOM_GROUP_UNREACHABLE
	 */
	CUSTOM_GROUP_UNREACHABLE("UNREACHABLE", "/images/customgroup-gray.gif",
			"Unreachable", new Color(Integer.parseInt(Constant.GRAY_HEX,
					Constant.SIXTEEN))),

	/**
	 * CUSTOM_GROUP_PENDING
	 */
	CUSTOM_GROUP_PENDING("PENDING_HOST", "/images/customgroup-blue.gif",
			"Pending", new Color(Integer.parseInt(Constant.BLUE_HEX,
					Constant.SIXTEEN))),
	/**
	 * CUSTOM_GROUP_PENDING
	 */
	CUSTOM_GROUP_UNKNOWN("UNKNOWN_HOST", "/images/customgroup-blue.gif",
			"Unknown", new Color(Integer.parseInt(Constant.BLUE_HEX,
					Constant.SIXTEEN))),
	//

	/**
	 * SERVICE_GROUP_CRITICAL_UNSCHEDULED
	 */
	SERVICE_GROUP_CRITICAL_UNSCHEDULED("UNSCHEDULED CRITICAL",
			"/images/service-group-red.gif", "Unscheduled Critical", new Color(
					Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_GROUP_CRITICAL_SCHEDULED
	 */
	SERVICE_GROUP_CRITICAL_SCHEDULED("SCHEDULED CRITICAL",
			"/images/service-group-orange.gif", "Scheduled Critical",
			new Color(Integer.parseInt(Constant.ORAGNE_HEX, Constant.SIXTEEN))),
	/**
	 * SERVICE_WARNING
	 */
	SERVICE_GROUP_WARNING("WARNING", "/images/service-group-yellow.gif",
			"Warning", new Color(Integer.parseInt(Constant.YELLOW_HEX,
					Constant.SIXTEEN))),
	/**
	 * SERVICE_UNKNOWN
	 */
	SERVICE_GROUP_UNKNOWN("UNKNOWN", "/images/service-group-gray.gif",
			"Unknown", new Color(Integer.parseInt(Constant.GRAY_HEX,
					Constant.SIXTEEN))),
	/**
	 * SERVICE_GROUP_PENDING
	 */
	SERVICE_GROUP_PENDING("PENDING_SERVICE", "/images/service-group-blue.gif",
			"Pending", new Color(Integer.parseInt(Constant.BLUE_HEX,
					Constant.SIXTEEN))),
	/**
	 * SERVICE_GROUP_OK
	 */
	SERVICE_GROUP_OK("OK", "/images/service-group-green.gif", "Ok", new Color(
			Integer.parseInt(Constant.GREEN_HEX, Constant.SIXTEEN))),
	/**
	 * NO_STATUS
	 * 
	 * Note: this is non-standard status, used for internal logic. Its not
	 * mentioned in standard services. MonitorStatusColor is set to Transparent.
	 */

	NO_STATUS("NO_STATUS", "/images/selection.png",
			"Status not available or not applicable", new Color(Constant.ZERO,
					Constant.ZERO, Constant.ZERO, Constant.ZERO)),
					
	HOST_SUSPENDED("SUSPENDED", "/images/selection.png",
			"Suspended", new Color(Constant.ZERO,
					Constant.ZERO, Constant.ZERO, Constant.ZERO)),

	/**
	 * ENTIRE_NETWORK_STATUS
	 * 
	 * this status is used only by "entire network" node.
	 */
	// TODO: "host-gree" icon for "entire-network" node is a workarround.
	// ICEFaces creates 2 beans if any of the node in the tree does not have
	// icon. So root node has to have an icon. Need to change host-gree with
	// proper network icon
	ENTIRE_NETWORK_STATUS("NO_STATUS", "/images/tree_entnet.gif",
			"Status not available or not applicable", new Color(Constant.ZERO,
					Constant.ZERO, Constant.ZERO, Constant.ZERO));

	/**
	 * Constant CONST_0X1000000
	 */
	private static final int CONST_0X1000000 = 0x1000000;
	/**
	 * Constant CONST_0XFFFFFF
	 */
	private static final int CONST_0XFFFFFF = 0xffffff;

	/**
	 * Default Constructor.
	 * 
	 * @param statusName
	 * @param statusIconPath
	 * @param actualStatus
	 */
	NetworkObjectStatusEnum(String statusName, String statusIconPath,
			String actualStatus, Color monitorStatuscolor) {
		this.monitorStatusName = statusName;
		this.iconPath = statusIconPath;
		this.status = actualStatus;
		this.monitorStatusColor = monitorStatuscolor;

	}

	/**
	 * Returns monitor Status Name.
	 * 
	 * @return monitorStatusName
	 */
	public String getMonitorStatusName() {
		return monitorStatusName;
	}

	/**
	 * Returns status iconPath.
	 * 
	 * @return status iconPath
	 */
	public String getIconPath() {
		return iconPath;
	}

	/**
	 * Returns actual status.
	 * 
	 * @return status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * monitor Status Name.
	 */
	private final String monitorStatusName;
	/**
	 * status iconPath.
	 */
	private final String iconPath;
	/**
	 * actual status.
	 */
	private final String status;

	/**
	 * Color for the monitor status of host/service.
	 */
	private Color monitorStatusColor;

	/**
	 * @return monitorStatusColor
	 */
	public Color getMonitorStatusColor() {
		return monitorStatusColor;
	}

	/**
	 * @param monitorStatusColor
	 */
	public void setMonitorStatusColor(Color monitorStatusColor) {
		this.monitorStatusColor = monitorStatusColor;
	}

	/**
	 * Pending Status.
	 */
	public static final String PENDING_STATUS_CONSTANT = "PENDING";

	/**
	 * Map for retrieving entity status.
	 */
	private static Map<String, NetworkObjectStatusEnum> extendedEntityStatusMap;

	/**
	 * Logger
	 */
	private static Logger logger = Logger
			.getLogger(NetworkObjectStatusEnum.class.getName());

	// statically initialize entity-status map.
	static {
		extendedEntityStatusMap = new HashMap<String, NetworkObjectStatusEnum>() {
			/**
			 * Serial Id
			 */
			private static final long serialVersionUID = 1L;
			// Put All states in entity status map
			{

				put("host_up", NetworkObjectStatusEnum.HOST_UP);
				put("host_scheduled down",
						NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED);
				put("host_unscheduled down",
						NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED);
				put("host_pending", NetworkObjectStatusEnum.HOST_PENDING);
				put("host_unreachable",
						NetworkObjectStatusEnum.HOST_UNREACHABLE);
				put("host_warning_host", NetworkObjectStatusEnum.HOST_WARNING);
				put("host_suspended", NetworkObjectStatusEnum.HOST_SUSPENDED);
				// host group
				put("host_group_up", NetworkObjectStatusEnum.HOST_GROUP_UP);
				put("host_group_scheduled down",
						NetworkObjectStatusEnum.HOST_GROUP_DOWN_SCHEDULED);
				put("host_group_unscheduled down",
						NetworkObjectStatusEnum.HOST_GROUP_DOWN_UNSCHEDULED);
				put("host_group_pending",
						NetworkObjectStatusEnum.HOST_GROUP_PENDING);
				put("host_group_unreachable",
						NetworkObjectStatusEnum.HOST_GROUP_UNREACHABLE);
				put("host_group_warning",
						NetworkObjectStatusEnum.HOST_GROUP_WARNING);
				// Service
				put("service_ok", NetworkObjectStatusEnum.SERVICE_OK);
				put("service_scheduled critical",
						NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED);
				put("service_unscheduled critical",
						NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED);
				put("service_warning", NetworkObjectStatusEnum.SERVICE_WARNING);
				put("service_unknown", NetworkObjectStatusEnum.SERVICE_UNKNOWN);
				put("service_pending", NetworkObjectStatusEnum.SERVICE_PENDING);

				// service group
				put("service_group_ok",
						NetworkObjectStatusEnum.SERVICE_GROUP_OK);
				put("service_group_scheduled critical",
						NetworkObjectStatusEnum.SERVICE_GROUP_CRITICAL_SCHEDULED);
				put("service_group_unscheduled critical",
						NetworkObjectStatusEnum.SERVICE_GROUP_CRITICAL_UNSCHEDULED);
				put("service_group_warning",
						NetworkObjectStatusEnum.SERVICE_GROUP_WARNING);
				put("service_group_unknown",
						NetworkObjectStatusEnum.SERVICE_GROUP_UNKNOWN);
				put("service_group_pending",
						NetworkObjectStatusEnum.SERVICE_GROUP_PENDING);
				
				// Since custom groups have both service groups and hostgroups, just put one set of constants in the map
				put("customgroup_up", NetworkObjectStatusEnum.CUSTOM_GROUP_UP);
				put("customgroup_scheduled down",
						NetworkObjectStatusEnum.CUSTOM_GROUP_DOWN_SCHEDULED);
				put("customgroup_unscheduled down",
						NetworkObjectStatusEnum.CUSTOM_GROUP_DOWN_UNSCHEDULED);
				put("customgroup_pending",
						NetworkObjectStatusEnum.CUSTOM_GROUP_PENDING);
				put("customgroup_pending_host",
						NetworkObjectStatusEnum.CUSTOM_GROUP_PENDING);
				put("customgroup_pending_service",
						NetworkObjectStatusEnum.CUSTOM_GROUP_PENDING);
				put("customgroup_unreachable",
						NetworkObjectStatusEnum.CUSTOM_GROUP_UNREACHABLE);
				put("customgroup_warning",
						NetworkObjectStatusEnum.CUSTOM_GROUP_WARNING);
				put("customgroup_warning_host",
						NetworkObjectStatusEnum.CUSTOM_GROUP_WARNING);
				put("customgroup_warning_service",
						NetworkObjectStatusEnum.CUSTOM_GROUP_WARNING);
				put("customgroup_ok", NetworkObjectStatusEnum.CUSTOM_GROUP_UP);
				put("customgroup_scheduled critical",
						NetworkObjectStatusEnum.CUSTOM_GROUP_DOWN_SCHEDULED);
				put("customgroup_unscheduled critical",
						NetworkObjectStatusEnum.CUSTOM_GROUP_DOWN_UNSCHEDULED);
				put("customgroup_unknown",
						NetworkObjectStatusEnum.CUSTOM_GROUP_UNKNOWN);			

			}

		};
	} // end of static block

	/**
	 * This is overloaded method , returns NetworkObjectStatusEnum depending on
	 * node type
	 * 
	 * @param monitorStatus
	 * @param nodeType
	 * @return NetworkObjectStatusEnum
	 */
	public static NetworkObjectStatusEnum getStatusEnumFromMonitorStatus(
			String monitorStatus, NodeType nodeType) {

        if (monitorStatus == null) {
            return NO_STATUS;
        }

		NetworkObjectStatusEnum status = null;
		switch (nodeType) {
		case HOST:
			// Host or HostGroup
			status = extendedEntityStatusMap.get("host_"
					+ monitorStatus.toLowerCase());
			break;
		case SERVICE:
			status = extendedEntityStatusMap.get("service_"
					+ monitorStatus.toLowerCase());
			break;
		case HOST_GROUP:
			status = extendedEntityStatusMap.get("host_group_"
					+ monitorStatus.toLowerCase());
			break;
		case SERVICE_GROUP:
			status = extendedEntityStatusMap.get("service_group_"
					+ monitorStatus.toLowerCase());
			break;
		case CUSTOM_GROUP:
			status = extendedEntityStatusMap.get("customgroup_"
					+ monitorStatus.toLowerCase());
			break;
		default:
			break;
		}
		// Status is not PENDING and something else

		if (status != null) {
			return status;
		}
		logger.debug("Unknown node status encountered:" + monitorStatus);
		return NO_STATUS;
	}

	/**
	 * Returns hex color for given status type. This function looks weird, but
	 * returns equivalent hex code, from the RGB value of the java Color object.
	 * 
	 * @return HexColor
	 */
	public String getHexColor() {
		String substring = Integer.toHexString(
				(monitorStatusColor.getRGB() & CONST_0XFFFFFF)
						| CONST_0X1000000).substring(1);
		return Constant.HASH + substring;
	}
}
