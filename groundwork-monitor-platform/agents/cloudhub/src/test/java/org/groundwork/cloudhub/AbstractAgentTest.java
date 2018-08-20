package org.groundwork.cloudhub;

import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.gwos.GwosServiceFactory;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.monitor.MonitorAgentSynchronizer;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.rs.dto.profiles.Metric;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class AbstractAgentTest {

    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;

    @Resource(name = ProfileService.NAME)
    protected ProfileService profileService;

    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory factory;

    @Resource(name = ConnectorFactory.NAME)
    protected ConnectorFactory connectorFactory;

    @Resource(name = MonitorAgentSynchronizer.NAME)
    protected MonitorAgentSynchronizer synchronizer;

    protected List<BaseQuery> getPrimaryMetrics(ProfileMetrics profileMetrics) {
        return getPrimaryMetrics(profileMetrics, false);
    }

    protected List<BaseQuery> getPrimaryMetrics(ProfileMetrics profileMetrics, boolean filter) {
        List<BaseQuery> primaryMetrics = new ArrayList<>();
        if (profileMetrics != null) {
            for (Metric hypervisorMetricsFromXML : profileMetrics.getPrimary()) {
                if (!filter || hypervisorMetricsFromXML.isMonitored()
                        || hypervisorMetricsFromXML.isGraphed())
                    primaryMetrics.add(new BaseQuery(hypervisorMetricsFromXML));
            }
        }
        return primaryMetrics;
    }

    protected List<BaseQuery> getSecondaryMetrics(ProfileMetrics profileMetrics) {
        return getSecondaryMetrics(profileMetrics, false);
    }

    protected List<BaseQuery> getSecondaryMetrics(ProfileMetrics profileMetrics, boolean filter) {
        List<BaseQuery> secondaryMetrics = new ArrayList<>();
        if (profileMetrics != null) {
            for (Metric vmMetricXML : profileMetrics.getSecondary()) {
                if (!filter || vmMetricXML.isMonitored()
                        || vmMetricXML.isGraphed())
                    secondaryMetrics.add(new BaseQuery(vmMetricXML));
            }
        }
        return secondaryMetrics;
    }

    protected List<BaseQuery> getCustomMetrics(ProfileMetrics profileMetrics) {
        return getCustomMetrics(profileMetrics, true);
    }

    protected List<BaseQuery> getCustomMetrics(ProfileMetrics profileMetrics, boolean filter) {
        List<BaseQuery> customMetrics = new ArrayList<>();
        if (profileMetrics != null) {
            for (Metric customMetricFromXML : profileMetrics.getCustom()) {
                if (!filter || customMetricFromXML.isMonitored() || customMetricFromXML.isGraphed())
                    customMetrics.add(new BaseQuery(customMetricFromXML));
            }
        }
        return customMetrics;
    }

}
