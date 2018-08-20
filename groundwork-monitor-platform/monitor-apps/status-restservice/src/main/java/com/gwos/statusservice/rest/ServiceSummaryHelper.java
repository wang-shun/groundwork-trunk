package com.gwos.statusservice.rest;

import com.gwos.statusservice.beans.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copyright 2015 GroundWork Open Source, Inc. ("GroundWork") All rights reserved.
 * <p/>
 * <p/>
 * The class encapsulates summary calculations for the Nagvis maps. to better determine if services
 * have been acknowledged or are currently in downtime. This information was previously (GWME 6.7)
 * retrieved from the database and was never ported into  GWME 7.0
 * <p/>
 * Class was created as part of the GWME 7.0.2-02 patch
 * <p/>
 * Created by rruttimann on 1/22/15.
 */
public class ServiceSummaryHelper {

    //Members
    private int PENDING_normal = 0;
    private int PENDING_downtime = 0;

    private int OK_normal = 0;
    private int OK_downtime = 0;

    private int WARNING_normal = 0;
    private int WARNING_downtime = 0;
    private int WARNING_ack = 0;
    private int WARNING_ackdown = 0;

    private int CRITICAL_normal = 0;
    private int CRITICAL_downtime = 0;
    private int CRITICAL_ack = 0;
    private int CRITICAL_ackdown = 0;

    private int UNKNOWN_normal = 0;
    private int UNKNOWN_downtime = 0;
    private int UNKNOWN_ack = 0;
    private int UNKNOWN_ackdown = 0;

    // Constants
    public static String UNSCHEDULED_CRITICAL = "UNSCHEDULED CRITICAL";
    public static String SCHEDULED_CRITICAL = "SCHEDULED CRITICAL";
    public static String CRITICAL = "CRITICAL";
    private final String WARNING = "WARNING";
    private final String PENDING = "PENDING";
    private final String OK = "OK";
    private final String UNKNOWN = "UNKNOWN";

    /* logging */
    private Log log = LogFactory.getLog(this.getClass());

    //methods

    /**
     * Calculating the statistics for the lifetime of the Object. Depending on the input for the services different
     * properties will be set that later can be reported back to the caller.
     * Input:
     *
     * @param monitorStatus     as a String in upper case
     * @param acknowledged      boolean that can be true or false
     * @param serviceInDowntime int that can be any value since it defines the downtime level. -1 for un-initialized
     */
    public void updateStatistics(String monitorStatus, Boolean acknowledged, int serviceInDowntime) {
        if (log.isDebugEnabled())
            log.debug("updateStatistics: Monitor status (" + monitorStatus + ") acknowledged (" + acknowledged + ") Downtime (" + serviceInDowntime + ")");

        if ((monitorStatus.compareTo(UNSCHEDULED_CRITICAL) == 0)
                || (monitorStatus.compareTo(SCHEDULED_CRITICAL) == 0)
                || (monitorStatus.compareTo(CRITICAL) == 0)) {
            /* Status Critical*/
            if (log.isDebugEnabled())
                log.debug("Critical clause");
            if (acknowledged && (serviceInDowntime > 0)) {
                CRITICAL_ackdown++;                                 // Service acknowledged and in downtime
            } else if (acknowledged && (serviceInDowntime <= 0)) {
                CRITICAL_ack++;                                     // Service acknowledged not in downtime
            } else if (!acknowledged && (serviceInDowntime > 0)) {
                CRITICAL_downtime++;                                // Service not acknowledged and in downtime
            } else
                CRITICAL_normal++;                                  // Service not acknowledged not in downtime
        } else if (monitorStatus.compareTo(WARNING) == 0) {
            /* Status Warning */
            if (log.isDebugEnabled())
                log.debug("Warning clause");
            if (acknowledged && (serviceInDowntime > 0)) {
                WARNING_ackdown++;                                 // Service acknowledged and in downtime
            } else if (acknowledged && (serviceInDowntime <= 0)) {
                WARNING_ack++;                                     // Service acknowledged not in downtime
            } else if (!acknowledged && (serviceInDowntime > 0)) {
                WARNING_downtime++;                                // Service not acknowledged and in downtime
            } else
                WARNING_normal++;                                  // Service not acknowledged not in downtime
        } else if (monitorStatus.compareTo(UNKNOWN) == 0) {
            /* Status UNKNOWN */
            if (log.isDebugEnabled())
                log.debug("Unknown clause");
            if (acknowledged && (serviceInDowntime > 0)) {
                UNKNOWN_ackdown++;                                 // Service acknowledged and in downtime
            } else if (acknowledged && (serviceInDowntime <= 0)) {
                UNKNOWN_ack++;                                     // Service acknowledged not in downtime
            } else if (!acknowledged && (serviceInDowntime > 0)) {
                UNKNOWN_downtime++;                                // Service not acknowledged and in downtime
            } else
                UNKNOWN_normal++;                                  // Service not acknowledged not in downtime
        } else if (monitorStatus.compareTo(PENDING) == 0) {
            /* Status PENDING */
            if (log.isDebugEnabled())
                log.debug("Pending clause");
            if (serviceInDowntime > 0) {
                PENDING_downtime++;                             // Service in downtime (acknowledge doesn't exist
            } else
                PENDING_normal++;                               // Service not in downtime (acknowledge doesn't exist)
        } else if (monitorStatus.compareTo(OK) == 0) {
            /* Status OK */
            if (log.isDebugEnabled())
                log.debug("OK clause");
            if (serviceInDowntime > 0) {
                OK_downtime++;                                 // Service in downtime (acknowledge doesn't exist
            } else
                OK_normal++;                                   // Service not in downtime (acknowledge doesn't exist)
        }
    }

    /**
     * Sets the service summary statistic properties in the host object that is passed in
     * @param host Host reference on which the statistics will be updated
     */
    public Host syncronizeStatistics(Host host) {

        host.setPendingNormal(this.getPENDING_normal());
        host.setPendingDowntime(this.getPENDING_downtime());

        host.setOkNormal(this.getOK_normal());
        host.setOkDowntime(this.getOK_downtime());

        host.setWarningNormal(this.getWARNING_normal());
        host.setWarningDowntime(this.getWARNING_downtime());
        host.setWarningAck(this.getWARNING_ack());
        host.setWarningAckdown(this.getWARNING_ackdown());

        if (log.isDebugEnabled())
            log.debug("Synchronize Statistics: Critical normal (" + this.getCRITICAL_normal() +") Downtime (" + this.getCRITICAL_downtime() +") Ack (" + this.getCRITICAL_ack() +") + AckDown (" + this.getCRITICAL_ackdown() +")");
        host.setCriticalNormal(this.getCRITICAL_normal());
        host.setCriticalDowntime(this.getCRITICAL_downtime());
        host.setCriticalAck(this.getCRITICAL_ack());
        host.setCriticalAckdown(this.getCRITICAL_ackdown());

        host.setUnknownNormal(this.getUNKNOWN_normal());
        host.setUnknownDowntime(this.getUNKNOWN_downtime());
        host.setUnknownAck(this.getUNKNOWN_ack());
        host.setUnknownAckdown(this.getUNKNOWN_ackdown());

        return host;
    }

    // getters
    public int getPENDING_normal() {
        return PENDING_normal;
    }

    public int getPENDING_downtime() {
        return PENDING_downtime;
    }

    public int getOK_normal() {
        return OK_normal;
    }

    public int getOK_downtime() {
        return OK_downtime;
    }

    public int getWARNING_normal() {
        return WARNING_normal;
    }

    public int getWARNING_downtime() {
        return WARNING_downtime;
    }

    public int getWARNING_ack() {
        return WARNING_ack;
    }

    public int getWARNING_ackdown() {
        return WARNING_ackdown;
    }

    public int getCRITICAL_normal() {
        return CRITICAL_normal;
    }

    public int getCRITICAL_downtime() {
        return CRITICAL_downtime;
    }

    public int getCRITICAL_ack() {
        return CRITICAL_ack;
    }

    public int getCRITICAL_ackdown() {
        return CRITICAL_ackdown;
    }

    public int getUNKNOWN_normal() {
        return UNKNOWN_normal;
    }

    public int getUNKNOWN_downtime() {
        return UNKNOWN_downtime;
    }

    public int getUNKNOWN_ack() {
        return UNKNOWN_ack;
    }

    public int getUNKNOWN_ackdown() {
        return UNKNOWN_ackdown;
    }
}