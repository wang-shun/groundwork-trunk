package com.gwos.statusservice.rest;

/**
 * Created by rruttimann on 02/11/15.
 */

import org.junit.Test;
import com.gwos.statusservice.rest.ServiceSummaryHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestServiceSummary {

    private Log log = LogFactory.getLog(this.getClass());

    private ServiceSummaryHelper serviceSummaryHelper = null;     // service class

    @Test
    public void serviceStatisticsTest() throws Exception {
        this.serviceSummaryHelper = new ServiceSummaryHelper();

        // Add three type of critical messages
        serviceSummaryHelper.updateStatistics(ServiceSummaryHelper.UNSCHEDULED_CRITICAL,false,0);
        serviceSummaryHelper.updateStatistics(ServiceSummaryHelper.SCHEDULED_CRITICAL,false,1);
        serviceSummaryHelper.updateStatistics(ServiceSummaryHelper.CRITICAL,true,1);

        serviceSummaryHelper.getCRITICAL_ack();         // should be 0
        serviceSummaryHelper.getCRITICAL_ackdown();     // should be 1
        serviceSummaryHelper.getCRITICAL_downtime();    //should  be 1
        serviceSummaryHelper.getCRITICAL_normal();      //should be 1

    }
}