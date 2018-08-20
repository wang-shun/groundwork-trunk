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

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkObjectStatusEnum;

/**
 * 
 * @author manish_kjain
 * 
 */
public class ChartHandler {

    /**
     * setting custom colors for a pie chart.
     * 
     * @param plot
     * @param colors
     * @param dataset
     * @return PiePlot
     */
    @SuppressWarnings({"deprecation", "unchecked" })
    public PiePlot setPieChartColor(PiePlot plot, Color[] colors,
            DefaultPieDataset dataset) {
        // pie chart back ground color
       plot.setBackgroundPaint(new Color(Integer.parseInt(
              Constant.PIE_BACKGRUOND_COLOR, Constant.SIXTEEN)));
        List<Comparable> keys = dataset.getKeys();       
        for (int i = 0; i < keys.size(); i++) {
            plot.setSectionPaint(i, colors[i]);
        }
        return plot;
    }

    /**
     * @param statismap
     * @return DefaultPieDataset
     */
    public DefaultPieDataset getHostorHostGroupPieDataSet(
            Map<String, Long> statismap) {
        DefaultPieDataset data = new DefaultPieDataset();
        data.setValue(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                .getMonitorStatusName(), statismap
                .get(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                        .getMonitorStatusName()));
        data.setValue(NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
                .getMonitorStatusName(), statismap
                .get(NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
                        .getMonitorStatusName()));
        data.setValue(Constant.HOST_OR_GROUP_UNREACHABLE, statismap
                .get(Constant.HOST_OR_GROUP_UNREACHABLE));
        data.setValue(Constant.HOST_OR_SERVICE_PENDING, statismap
                .get(Constant.HOST_OR_SERVICE_PENDING));
        data.setValue(Constant.HOST_OR_GROUP_UP, statismap
                .get(Constant.HOST_OR_GROUP_UP));

        return data;
    }
    
    /**
     * @param statismap
     * @return DefaultPieDataset
     */
    public DefaultPieDataset getHostGroupEventPieDataSet(
            Map<String, Long> statismap) {
        DefaultPieDataset data = new DefaultPieDataset();
        data.setValue(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                .getMonitorStatusName(), statismap
                .get(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                        .getMonitorStatusName()));
       data.setValue(NetworkObjectStatusEnum.HOST_WARNING
                .getMonitorStatusName(), statismap
                .get(NetworkObjectStatusEnum.HOST_WARNING
                        .getMonitorStatusName()));
        data.setValue(Constant.HOST_OR_GROUP_UNREACHABLE, statismap
                .get(Constant.HOST_OR_GROUP_UNREACHABLE));
        data.setValue(Constant.HOST_OR_SERVICE_PENDING, statismap
                .get(Constant.HOST_OR_SERVICE_PENDING));
        data.setValue(Constant.HOST_OR_GROUP_UP, statismap
                .get(Constant.HOST_OR_GROUP_UP));

        return data;
    }

    /**
     * @param statismap
     * @return DefaultPieDataset
     */
    public DefaultPieDataset getServiceorServiceGroupPieDataSet(
            Map<String, Long> statismap) {
        DefaultPieDataset data = new DefaultPieDataset();
        data.setValue(Constant.UNSCHEDULED_CRITICAL, statismap
                .get(Constant.UNSCHEDULED_CRITICAL.toUpperCase()));
        data.setValue(Constant.SCHEDULED_CRITICAL, statismap
                .get(Constant.SCHEDULED_CRITICAL.toUpperCase()));
        data.setValue(Constant.SERVICE_WARNING, statismap
                .get(Constant.SERVICE_WARNING));
        data.setValue(Constant.SERVICE_UNKNOWN, statismap
                .get(Constant.SERVICE_UNKNOWN));
        data.setValue(Constant.HOST_OR_SERVICE_PENDING, statismap
                .get(Constant.HOST_OR_SERVICE_PENDING));
        data.setValue(Constant.SERVICE_OK, statismap.get(Constant.SERVICE_OK));
        return data;
    }

    /**
     * return color array for host and host group pie chart.
     * 
     * @return Color[]
     */
    public Color[] getHostorHostGroupColorArray() {
        Color[] colors = {
                new Color(Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.ORAGNE_HEX,
                        Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.GRAY_HEX, Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.BLUE_HEX, Constant.SIXTEEN)),
                new Color(Integer
                        .parseInt(Constant.GREEN_HEX, Constant.SIXTEEN)) };
        return colors;
    }
    
    /**
     * return color array for host and host group pie chart.
     * 
     * @return Color[]
     */
    public Color[] getHostGroupEventColorArray() {
        Color[] colors = {
                new Color(Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.YELLOW_HEX,
                        Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.GRAY_HEX, Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.BLUE_HEX, Constant.SIXTEEN)),
                new Color(Integer
                        .parseInt(Constant.GREEN_HEX, Constant.SIXTEEN)) };
        return colors;
    }

    /**
     * return color array for Service and Service group pie chart.
     * 
     * @return Color[]
     */
    public Color[] getSetviceorServiceGroupColorArray() {
        Color[] colors = {
                new Color(Integer.parseInt(Constant.RED_HEX, Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.ORAGNE_HEX,
                        Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.YELLOW_HEX,
                        Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.GRAY_HEX, Constant.SIXTEEN)),
                new Color(Integer.parseInt(Constant.BLUE_HEX, Constant.SIXTEEN)),
                new Color(Integer
                        .parseInt(Constant.GREEN_HEX, Constant.SIXTEEN)) };
        return colors;
    }

    /**
     * Creates a map for the monitor status and corresponding color for stacked
     * bar chart .
     * 
     * @return - colorMap (key - Monitor status,value- color)
     * 
     */
    public static HashMap<String, Color> getColorMapForStackedBarChart() {
        HashMap<String, Color> colorMap = new HashMap<String, Color>();

        // For SERVICE

        // Set color for monitor status - 'Unscheduled Critical'
        colorMap.put(NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
                .getMonitorStatusName(),
                NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
                        .getMonitorStatusColor());
        // Set color for monitor status - 'Scheduled Critical'
        colorMap.put(NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
                .getMonitorStatusName(),
                NetworkObjectStatusEnum.SERVICE_CRITICAL_SCHEDULED
                        .getMonitorStatusColor());
        // Set color for monitor status - 'WARNING'
        colorMap
                .put(NetworkObjectStatusEnum.SERVICE_WARNING
                        .getMonitorStatusName(),
                        NetworkObjectStatusEnum.SERVICE_WARNING
                                .getMonitorStatusColor());
        // Set color for monitor status - 'Unknown'
        colorMap
                .put(NetworkObjectStatusEnum.SERVICE_UNKNOWN
                        .getMonitorStatusName(),
                        NetworkObjectStatusEnum.SERVICE_UNKNOWN
                                .getMonitorStatusColor());
        // Set color for monitor status - 'Pending'
        colorMap
                .put(NetworkObjectStatusEnum.SERVICE_PENDING.getStatus(),
                        NetworkObjectStatusEnum.SERVICE_PENDING
                                .getMonitorStatusColor());
        // Set color for monitor status - 'OK'
        colorMap.put(NetworkObjectStatusEnum.SERVICE_OK.getMonitorStatusName(),
                NetworkObjectStatusEnum.SERVICE_OK.getMonitorStatusColor());
        /*
         * Set color for monitor status - 'CRITICAL' This state no more exists.
         * For compliance with older datasets adding this monitor state.
         */
        colorMap.put(Constant.SERVICE_CRITICAL,
                NetworkObjectStatusEnum.SERVICE_CRITICAL_UNSCHEDULED
                        .getMonitorStatusColor());

        // colors for ACK status - SERVICE
        colorMap.put(NetworkObjectStatusEnum.SERVICE_ACK_OK.getStatus(),
                NetworkObjectStatusEnum.SERVICE_ACK_OK.getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.SERVICE_ACK_CRITICAL.getStatus(),
                NetworkObjectStatusEnum.SERVICE_ACK_CRITICAL
                        .getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.SERVICE_ACK_WARNING.getStatus(),
                NetworkObjectStatusEnum.SERVICE_ACK_WARNING
                        .getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.SERVICE_ACK_UNKNOWN.getStatus(),
                NetworkObjectStatusEnum.SERVICE_ACK_UNKNOWN
                        .getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.SERVICE_ACK_PENDING.getStatus(),
                NetworkObjectStatusEnum.SERVICE_ACK_PENDING
                        .getMonitorStatusColor());

        // For HOST

        // colors for ACK status - HOST
        colorMap.put(NetworkObjectStatusEnum.HOST_ACK_UP.getStatus(),
                NetworkObjectStatusEnum.HOST_ACK_UP.getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.HOST_ACK_DOWN.getStatus(),
                NetworkObjectStatusEnum.HOST_ACK_DOWN.getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.HOST_ACK_WARNING.getStatus(),
                NetworkObjectStatusEnum.HOST_ACK_WARNING
                        .getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.HOST_ACK_UNREACHABLE.getStatus(),
                NetworkObjectStatusEnum.HOST_ACK_UNREACHABLE
                        .getMonitorStatusColor());
        colorMap.put(NetworkObjectStatusEnum.HOST_ACK_PENDING.getStatus(),
                NetworkObjectStatusEnum.HOST_ACK_PENDING
                        .getMonitorStatusColor());

        // Set color for monitor status - 'Unscheduled Down'
        colorMap.put(NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                .getMonitorStatusName(),
                NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                        .getMonitorStatusColor());
        // Set color for monitor status - 'Scheduled Down'
        colorMap.put(NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
                .getMonitorStatusName(),
                NetworkObjectStatusEnum.HOST_DOWN_SCHEDULED
                        .getMonitorStatusColor());
        // Set color for monitor status - 'UNREACHABLE'
        colorMap.put(NetworkObjectStatusEnum.HOST_UNREACHABLE
                .getMonitorStatusName(),
                NetworkObjectStatusEnum.HOST_UNREACHABLE
                        .getMonitorStatusColor());
        // Set color for monitor status - 'PENDING'
        colorMap.put(NetworkObjectStatusEnum.HOST_PENDING.getStatus(),
                NetworkObjectStatusEnum.HOST_PENDING.getMonitorStatusColor());
        // Set color for monitor status - 'UP'
        colorMap.put(NetworkObjectStatusEnum.HOST_UP.getMonitorStatusName(),
                NetworkObjectStatusEnum.HOST_UP.getMonitorStatusColor());
        /*
         * Set color for monitor status - 'DOWN' This state no more exists. For
         * compliance with older datasets adding this monitor state.
         */
        colorMap.put(Constant.HOST_OR_GROUP_DOWN,
                NetworkObjectStatusEnum.HOST_DOWN_UNSCHEDULED
                        .getMonitorStatusColor());

        /*
         * Since the monitor status returned from web services does not
         * differentiate between Host PENDING and SERVICE PENDING , adding
         * 'Pending' as a common status.
         */
        colorMap.put(Constant.PENDING, NetworkObjectStatusEnum.SERVICE_PENDING
                .getMonitorStatusColor());
        colorMap
                .put(Constant.PENDING_UPPER_CASE,
                        NetworkObjectStatusEnum.SERVICE_PENDING
                                .getMonitorStatusColor());
        /**
         * When there are no state transitions for a host/service,the status is
         * set to empty string.
         */
        colorMap.put(NetworkObjectStatusEnum.NO_STATUS.getMonitorStatusName(),
                NetworkObjectStatusEnum.NO_STATUS.getMonitorStatusColor());
        return colorMap;
    }

    /**
     * Setting custom colors for a stacked bar chart for Service context.
     * 
     * @param stackedBarRenderer
     * @param colorMap
     * @param dataset
     * @return StackedBarRenderer
     */

    @SuppressWarnings("unchecked")
    public StackedBarRenderer setStackedBarChartColors(
            StackedBarRenderer stackedBarRenderer,
            HashMap<String, Color> colorMap, CategoryDataset dataset) {

        List<Comparable> keys = dataset.getRowKeys();

        // Iterate over the color map.
        for (Map.Entry<String, Color> mapEntry : colorMap.entrySet()) {
            // Iterate over the row keys to check the monitor status and set the
            // color accordingly.
            for (int i = 0; i < keys.size(); i++) {
                Comparable<String> rowKey = keys.get(i);
                String rowKeyString = rowKey.toString();
                if (rowKeyString.startsWith(mapEntry.getKey())) {
                    stackedBarRenderer.setSeriesPaint(i, mapEntry.getValue());
                }
            }
        }
        return stackedBarRenderer;
    }
}
