package org.groundwork.cloudhub.profile;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dtaylor on 5/18/15.
 */
public class OpenStackProfileWrapper extends BaseProfileWrapper {

    @Valid
    private List<UIMetric> hypervisorMetrics;

    @Valid
    private List<UIMetric> ceilometerMetrics;

    @Valid
    private List<UIMetric> computeMetrics;

    private String configFileName;

    private String configFilePath;

    public OpenStackProfileWrapper() {
        super();
        ceilometerMetrics = new ArrayList<UIMetric>();
        computeMetrics = new ArrayList<UIMetric>();
    }

    public OpenStackProfileWrapper(CloudHubProfile cloudHubProfile, CommonConfiguration common) {
        super(cloudHubProfile.getProfileType().name(), common.getAgentId());
        hypervisorMetrics = new ArrayList<UIMetric>();
        for (Metric metric : cloudHubProfile.getHypervisor().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            hypervisorMetrics.add(uiMetric);
        }
        ceilometerMetrics = new ArrayList<UIMetric>();
        computeMetrics = new ArrayList<UIMetric>();
        for (Metric metric : cloudHubProfile.getVm().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            if (uiMetric.getSourceType() == null &&
               (uiMetric.getName().equals("cpu_util") || uiMetric.getName().equals("disk.read.bytes"))) {
                ceilometerMetrics.add(uiMetric);
            }
            else if (uiMetric.getSourceType() != null && uiMetric.getSourceType().equals(Metric.SOURCE_TYPE_CEILOMETER)) {
                ceilometerMetrics.add(uiMetric);
            }
            else {
                computeMetrics.add(uiMetric);
            }
        }
        Collections.sort(hypervisorMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(ceilometerMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(computeMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        setConfigFileName(common.getConfigurationFile());
        setConfigFilePath(common.getPathToConfigurationFile());
    }

    public CloudHubProfile mergeToProfile(CloudHubProfile profile) {
        profile.setAgent(getAgent());
        if (hypervisorMetrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : hypervisorMetrics) {
                boolean found = false;
                for (Metric metric : profile.getHypervisor().getMetrics()) {
                    if (uiMetric.getName().equals(metric.getName())) {
                        mergeMetric(metric, uiMetric);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    additional.add(createMetric(uiMetric));
                }
            }
            for (Metric metric : additional) {
                profile.getHypervisor().getMetrics().add(metric);
            }
        }
        mergeMetrics(ceilometerMetrics, profile.getVm().getMetrics());
        mergeMetrics(computeMetrics, profile.getVm().getMetrics());
        return profile;
    }

    public List<UIMetric> getHypervisorMetrics() {
        return hypervisorMetrics;
    }

    public void setHypervisorMetrics(List<UIMetric> metrics) {
        this.hypervisorMetrics = metrics;
    }

    public List<UIMetric> getComputeMetrics() {
        return computeMetrics;
    }

    public void setComputeMetrics(List<UIMetric> metrics) {
        this.computeMetrics = metrics;
    }

    public List<UIMetric> getCeilometerMetrics() {
        return ceilometerMetrics;
    }

    public void setCeilometerMetrics(List<UIMetric> metrics) {
        this.ceilometerMetrics = metrics;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public List<UIMetric> getVmMetrics() {
        List<UIMetric> merged = new ArrayList<>();
        merged.addAll(this.ceilometerMetrics);
        merged.addAll(this.computeMetrics);
        return merged;
    }

}
