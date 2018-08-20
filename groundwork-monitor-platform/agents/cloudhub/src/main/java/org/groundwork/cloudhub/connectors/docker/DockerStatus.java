package org.groundwork.cloudhub.connectors.docker;

import org.groundwork.cloudhub.connectors.opendaylight.client.MetricInfo;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.metrics.BaseQuery;

/**
 * This class is a place holder to be filled in when the Open Daylight API starts returning device statuses.
 * One feature implemented here is the calculation of switch (vm) status based on summing and averaging of all ports
 * If 50% or more of a switches ports have surpassed threshold, the switch is set into warning status
 *
 **/
public enum DockerStatus {

    UP,
    WARNING,
    UNKNOWN;

    public static int getWarningCounts(MetricInfo metric, BaseQuery query) {
        return (metric.metric >= query.getWarning()) ? 1 : 0;
    }

    public static final double WARNING_THRESHOLD = 0.50;

    public static String convertToGroundworkStatus(int warningCounts, int totalQueries) {
        if (warningCounts > 0 || totalQueries > 0) {
            double percent = warningCounts / totalQueries;
            if (percent >= WARNING_THRESHOLD)
                return GwosStatus.WARNING.status;
        }
        return GwosStatus.UP.status;
    }

}
