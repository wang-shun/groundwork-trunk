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

package com.groundworkopensource.portal.statusviewer.bean;

/**
 * Back end bean for holding Rrg graph byes.
 * 
 * @author manish_kjain
 * 
 */
public class RrdGraphBean {

    /**
     * byte array contain rrd byte array
     */
    private byte[] rrdGraphBytes;

    /**
     * title of panel collapsible
     */
    private String collapsibleTitle;

    /**
     * expanded
     */
    private boolean expanded;

    /**
     * render graph using client rendering
     */
    private boolean clientRendering;
    private boolean hideRRDs;

    /**
     * host name metadata
     */
    private String hostName;

    /**
     * service name metadata
     */
    private String serviceName;

    /**
     * unix start time metadata, (seconds since epoch)
     */
    private long startTime;

    /**
     * unix end time metadata, (seconds since epoch)
     */
    private long endTime;

    /**
     * application type metadata, (e.g. NAGIOS)
     */
    private String applicationType;

    /**
     * render graph width in pixels
     */
    private int width;

    /**
     * render target div tag id
     */
    private String targetDivId;

    /**
     * Sets the rrdGraphBytes.
     * 
     * @param rrdGraphBytes
     *            the rrdGraphBytes to set
     */
    public void setRrdGraphBytes(byte[] rrdGraphBytes) {
        this.rrdGraphBytes = rrdGraphBytes;
    }

    /**
     * Returns the rrdGraphBytes.
     * 
     * @return the rrdGraphBytes
     */
    public byte[] getRrdGraphBytes() {
        return rrdGraphBytes;
    }

    /**
     * Sets the collapsibleTitle.
     * 
     * @param collapsibleTitle
     *            the collapsibleTitle to set
     */
    public void setCollapsibleTitle(String collapsibleTitle) {
        this.collapsibleTitle = collapsibleTitle;
    }

    /**
     * Returns the collapsibleTitle.
     * 
     * @return the collapsibleTitle
     */
    public String getCollapsibleTitle() {
        return collapsibleTitle;
    }

    /**
     * Sets the expanded.
     * 
     * @param expanded
     *            the expanded to set
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * Returns the expanded.
     * 
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Return client rendering flag.
     *
     * @return client rendering
     */
    public boolean isClientRendering() {
        return clientRendering;
    }

    /**
     * Return RRD rendering flag.
     *
     * @return RRD rendering
     */
    public boolean isHideRRDs() {
        return hideRRDs;
    }

    /**
     * Set client rendering flag.
     *
     * @param clientRendering client rendering
     */
    public void setClientRendering(boolean clientRendering) {
        this.clientRendering = clientRendering;
    }

    /**
     * Set RRD rendering flag.
     *
     * @param hideRRDs RRD rendering
     */
    public void setHideRRDs(boolean hideRRDs) {
        this.hideRRDs = hideRRDs;
    }

    /**
     * Return graph host name metadata.
     *
     * @return host name metadata
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Set graph host name metadata.
     *
     * @param hostName host name metadata
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Return graph service name metadata.
     *
     * @return service name metadata
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Set graph service name metadata.
     *
     * @param serviceName service name metadata
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Return graph start time metadata, (unix seconds since epoch).
     *
     * @return start time metadata
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Set graph start time metadata, (unix seconds since epoch).
     *
     * @param startTime start time metadata
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Return graph end time metadata, (unix seconds since epoch).
     *
     * @return end time metadata
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Set graph end time metadata, (unix seconds since epoch).
     *
     * @param endTime end time metadata
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Return graph application type metadata, (e.g. NAGIOS).
     *
     * @return application type metadata
     */
    public String getApplicationType() {
        return applicationType;
    }

    /**
     * Set graph application type metadata, (e.g. NAGIOS).
     *
     * @param applicationType application type metadata
     */
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    /**
     * Return graph width, (pixels).
     *
     * @return graph width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set graph width, (pixels).
     *
     * @param width graph width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Return rendering target div tag id.
     *
     * @return target div tag id
     */
    public String getTargetDivId() {
        return targetDivId;
    }

    /**
     * Set rendering target div tag id.
     *
     * @param targetDivId target div tag id
     */
    public void setTargetDivId(String targetDivId) {
        this.targetDivId = targetDivId;
    }
}
