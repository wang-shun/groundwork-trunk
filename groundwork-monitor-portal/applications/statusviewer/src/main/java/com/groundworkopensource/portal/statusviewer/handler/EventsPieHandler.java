/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.handler;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.awt.Color;
import java.io.IOException;
import java.net.URLEncoder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.HostGroupQueryType;
import org.groundwork.foundation.ws.model.impl.ServiceQueryType;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSHostGroup;
import org.groundwork.foundation.ws.api.WSEvent;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import com.groundworkopensource.portal.common.ws.impl.WebServiceLocator;
import com.groundworkopensource.portal.statusviewer.bean.EventPieBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import javax.faces.event.ActionEvent;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import com.groundworkopensource.portal.statusviewer.bean.EventServerPush;
import com.icesoft.faces.async.render.SessionRenderer;

import com.icesoft.faces.component.DisplayEvent;

/**
 * This class is a handler of Event Pies Portlet.
 * 
 * @author Arul Shanmmugam
 * 
 */
public class EventsPieHandler extends EventServerPush {

	private static Logger logger = Logger.getLogger(EventsPieHandler.class
			.getName());

	private List<EventPieBean> hostGroupMap = new ArrayList<EventPieBean>();

	private String CONS_STATUS_UP = "Up/OK";
	private String CONS_STATUS_DOWN = "Down/Critical";
	private String CONS_STATUS_UNKNOWN = "Unreachable/Unknown";
	private String CONS_STATUS_WARNING = "Warning";
	private String CONS_STATUS_PENDING = "Pending";

	public static final String EVENT_CONSOLE_BASE_URL = "/portal/classic/console";
	
	private UserExtendedRoleBean userExtendedRoleBean = null;

	/**
	 * @param EventsPieHandler
	 * 
	 */
	public EventsPieHandler() {
		this.load();
	}

	/**
	 * Loads Hostgroup event pies
	 */
	private void load() {
		hostGroupMap.clear();
		try {
			WSHostGroup wsHostGroup = WebServiceLocator.getInstance()
					.hostGroupLocator().getwshostgroup();
			HostGroupQueryType queryType = HostGroupQueryType.ALL;
			WSFoundationCollection col = wsHostGroup.getHostGroups(queryType,
					null, null, true, -1, -1, null);

			WSEvent wsEvent = WebServiceLocator.getInstance().eventLocator()
					.getwsevent();

			HostGroup[] hostGroups = col.getHostGroup();
			if (hostGroups != null) {
				for (int i = 0; i < hostGroups.length; i++) {
					HostGroup hostGroup = (HostGroup) hostGroups[i];
					String hostGroupName = hostGroup.getName();

					// Check for MSP access
					if (userExtendedRoleBean == null) {
						userExtendedRoleBean = PortletUtils
								.getUserExtendedRoleBean();
					}
					boolean isRestricted = false;
					List<String> authorizedHostGroups = null;
					if (userExtendedRoleBean != null) {
						authorizedHostGroups = userExtendedRoleBean
								.getExtRoleHostGroupList();
						String defaultHostGroup = userExtendedRoleBean
								.getDefaultHostGroup();
						if (authorizedHostGroups.size() > 0
								&& defaultHostGroup != null) {
							isRestricted = true;
						}
					}
					if (!isRestricted
							|| (isRestricted && authorizedHostGroups != null && authorizedHostGroups
									.contains(hostGroupName))) {
						WSFoundationCollection eventCol = wsEvent
								.getEventStatisticsByHostGroup(
										"ALL",
										hostGroupName, null, null,
										"MonitorStatusWithOpen");
						StatisticProperty[] stats = eventCol
								.getStatisticCollection();

						EventPieBean map = new EventPieBean();
						map.setGroupName(hostGroupName);
						map.setConsoleURL(EventsPieHandler.EVENT_CONSOLE_BASE_URL
								+ "?filterType=HostGroup&filterValue="
								+ URLEncoder.encode(hostGroupName));
						logger.debug("==>" + hostGroupName);
						if (hostGroupName != null && hostGroupName.length() > 8)
							map.setTruncatedGroupName(hostGroupName.substring(
									0, 7) + "...");
						else
							map.setTruncatedGroupName(hostGroupName);

						HashMap<String, Long> statMap = new HashMap<String, Long>();

						long downCount = 0;
						long warningCount = 0;
						long unknownCount = 0;
						long pendingCount = 0;
						long upCount = 0;

						for (StatisticProperty stat : stats) {
							String statusName = stat.getName();
							if (statusName.equalsIgnoreCase("DOWN")
									|| statusName
											.equalsIgnoreCase("SCHEDULED DOWN")
									|| statusName
											.equalsIgnoreCase("UNSCHEDULED DOWN")
									|| statusName
											.equalsIgnoreCase("SCHEDULED CRITICAL")
									|| statusName
											.equalsIgnoreCase("UNSCHEDULED CRITICAL")
									|| statusName.equalsIgnoreCase("CRITICAL")
									|| statusName.equalsIgnoreCase("SUSPENDED")) {
								downCount = downCount + stat.getCount();
							}
							if (statusName.equalsIgnoreCase("UP")
									|| statusName.equalsIgnoreCase("OK")) {
								upCount = upCount + stat.getCount();
							}
							if (statusName.equalsIgnoreCase("UNKNOWN")
									|| statusName
											.equalsIgnoreCase("UNREACHABLE")) {
								unknownCount = unknownCount + stat.getCount();
							}
							if (statusName.equalsIgnoreCase("PENDING_HOST")
									|| statusName.equalsIgnoreCase("PENDING")
									|| statusName
											.equalsIgnoreCase("PENDING_SERVICE")) {
								pendingCount = pendingCount + stat.getCount();
							}
							if (statusName.equalsIgnoreCase("WARNING_HOST")
									|| statusName.equalsIgnoreCase("WARNING")
									|| statusName
											.equalsIgnoreCase("WARNING_SERVICE")) {
								warningCount = warningCount + stat.getCount();
							}

						}
						StatisticProperty[] consolidatedStats = new StatisticProperty[5];
						StatisticProperty propDown = new StatisticProperty();
						propDown.setName(CONS_STATUS_DOWN);
						propDown.setCount(downCount);
						StatisticProperty propUP = new StatisticProperty();
						propUP.setName(CONS_STATUS_UP);
						propUP.setCount(upCount);
						StatisticProperty propUnknown = new StatisticProperty();
						propUnknown.setName(CONS_STATUS_UNKNOWN);
						propUnknown.setCount(unknownCount);
						StatisticProperty propPending = new StatisticProperty();
						propPending.setName(CONS_STATUS_PENDING);
						propPending.setCount(pendingCount);
						StatisticProperty propWarn = new StatisticProperty();
						propWarn.setName(CONS_STATUS_WARNING);
						propWarn.setCount(warningCount);
						consolidatedStats[0] = propUP;
						consolidatedStats[1] = propUnknown;
						consolidatedStats[2] = propPending;
						consolidatedStats[3] = propDown;
						consolidatedStats[4] = propWarn;
						map.setStats(consolidatedStats);
						StringBuffer toolTip = new StringBuffer();
						toolTip.append("<table>");
						toolTip.append("<tr>");
						toolTip.append("<td>");
						toolTip.append(CONS_STATUS_DOWN);
						toolTip.append("</td>");
						toolTip.append("<td>");
						toolTip.append(downCount);
						toolTip.append("</td>");
						toolTip.append("</tr>");
						toolTip.append("<tr>");
						toolTip.append("<td>");
						toolTip.append(CONS_STATUS_UNKNOWN);
						toolTip.append("</td>");
						toolTip.append("<td>");
						toolTip.append(unknownCount);
						toolTip.append("</td>");
						toolTip.append("</tr>");
						toolTip.append("<tr>");
						toolTip.append("<td>");
						toolTip.append(CONS_STATUS_PENDING);
						toolTip.append("</td>");
						toolTip.append("<td>");
						toolTip.append(pendingCount);
						toolTip.append("</td>");
						toolTip.append("</tr>");
						toolTip.append("<tr>");
						toolTip.append("<td>");
						toolTip.append(CONS_STATUS_WARNING);
						toolTip.append("</td>");
						toolTip.append("<td>");
						toolTip.append(warningCount);
						toolTip.append("</td>");
						toolTip.append("</tr>");
						toolTip.append("<tr>");
						toolTip.append("<td>");
						toolTip.append(CONS_STATUS_UP);
						toolTip.append("</td>");
						toolTip.append("<td>");
						toolTip.append(upCount);
						toolTip.append("</td>");
						toolTip.append("</tr>");
						toolTip.append("<tr>");
						toolTip.append("<td>");
						toolTip.append("</table>");
						map.setStatToolTip(toolTip.toString());
						logger.debug("Down==>" + downCount);
						logger.debug("Up==>" + upCount);
						logger.debug("Unknown==>" + unknownCount);
						logger.debug("Warning==>" + warningCount);
						logger.debug("Pending==>" + pendingCount);
						statMap.put("UNSCHEDULED DOWN", downCount);
						statMap.put("UP", upCount);
						statMap.put("UNREACHABLE", unknownCount);
						statMap.put("WARNING_HOST", warningCount);
						statMap.put("PENDING", pendingCount);
						map.setChart(this.getHGOrHPieChartBytes(statMap));

						hostGroupMap.add(map);
					} // end if
				} // end for
			} // end if
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * Populates hostgroup heat map
	 */
	public List<EventPieBean> getHostGroupMap() {
		return hostGroupMap;
	}

	/*
	 * public void tabSelection(TabChangeEvent event) {
	 * 
	 * }
	 */

	/**
	 * draw host group pie chart return host group pie chart byte array.
	 * 
	 * @param statisticsMap
	 * @return bytes
	 * @throws IOException
	 */
	private byte[] getHGOrHPieChartBytes(Map<String, Long> statisticsMap)
			throws IOException {
		byte[] encodeAsPNG = null;

		ChartHandler chartHandler = new ChartHandler();
		Color[] colors = chartHandler.getHostGroupEventColorArray();
		DefaultPieDataset data = chartHandler
				.getHostGroupEventPieDataSet(statisticsMap);
		// get jfree chart object from factory.
		JFreeChart chart = ChartFactory.createPieChart(Constant.EMPTY_STRING,
				data, false, false, false);
		// chart.setBackgroundPaint(Color.BLACK);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(null);
		plot.setOutlinePaint(null);
		// plot.setForegroundAlpha(0.5f);
		plot.setLabelLinksVisible(true);
		chartHandler.setPieChartColor(plot, colors, data);
		encodeAsPNG = ChartUtilities.encodeAsPNG(chart.createBufferedImage(60,
				59));

		return encodeAsPNG;

	}

	/**
	 * Call back method for JMS
	 * 
	 * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
	 */
	@Override
	public void refresh(String xmlTopic) {
		try {

			if (xmlTopic != null) {
				// hashmap
				this.load();
				SessionRenderer.render(groupRenderName);
			} // end if

		} catch (Exception exc) {
			logger.error("Exception in EventPieHandler : " + exc.getMessage());
		}
	}

}
