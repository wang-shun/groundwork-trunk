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
public class DataCenterProfileWrapper extends BaseProfileWrapper {

    @Valid
    private List<UIMetric> hostMetrics;

    @Valid
    private List<UIMetric> instanceMetrics;

    @Valid
    private List<UIMetric> storageMetrics;

    @Valid
    private List<UIMetric> networkMetrics;

    @Valid
    private List<UIMetric> resourcePoolMetrics;

    private String configFileName;

    private String configFilePath;

    private boolean enableStorage = true;

    public DataCenterProfileWrapper() {
        super();
        instanceMetrics = new ArrayList<UIMetric>();
        hostMetrics = new ArrayList<UIMetric>();
        storageMetrics = new ArrayList<UIMetric>();
        networkMetrics = new ArrayList<UIMetric>();
        resourcePoolMetrics = new ArrayList<UIMetric>();
    }

    public DataCenterProfileWrapper(CloudHubProfile cloudHubProfile, CommonConfiguration common) {
        super(cloudHubProfile.getProfileType().name(), common.getAgentId());
        hostMetrics = new ArrayList<UIMetric>();
        instanceMetrics = new ArrayList<UIMetric>();
        storageMetrics = new ArrayList<UIMetric>();
        networkMetrics = new ArrayList<UIMetric>();
        resourcePoolMetrics = new ArrayList<UIMetric>();
        for (Metric metric : cloudHubProfile.getHypervisor().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            if (uiMetric.getSourceType() == null) {
                hostMetrics.add(uiMetric);
            }
            else {
                if (uiMetric.getSourceType().equals(Metric.SOURCE_TYPE_STORAGE)) {
                    storageMetrics.add(uiMetric);
                }
                else if (uiMetric.getSourceType().equals(Metric.SOURCE_TYPE_NETWORK)) {
                    networkMetrics.add(uiMetric);
                }
                else if (uiMetric.getSourceType().equals(Metric.SOURCE_TYPE_RESOURCEPOOL)) {
                    resourcePoolMetrics.add(uiMetric);
                }
                else {
                    hostMetrics.add(uiMetric);
                }
            }
        }
        for (Metric metric : cloudHubProfile.getVm().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            instanceMetrics.add(uiMetric);
        }
        Collections.sort(hostMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(instanceMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(storageMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(networkMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(resourcePoolMetrics, new Comparator<UIMetric>() {
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
        mergeMetrics(hostMetrics, profile.getHypervisor().getMetrics());
        mergeMetrics(instanceMetrics, profile.getVm().getMetrics());
        mergeMetrics(storageMetrics, profile.getHypervisor().getMetrics());
        mergeMetrics(networkMetrics, profile.getHypervisor().getMetrics());
        mergeMetrics(resourcePoolMetrics, profile.getHypervisor().getMetrics());
        return profile;
    }

    public List<UIMetric> getHypervisorMetrics() {
        List<UIMetric> merged = new ArrayList<>();
        merged.addAll(this.hostMetrics);
        merged.addAll(this.storageMetrics);
        merged.addAll(this.networkMetrics);
        merged.addAll(this.resourcePoolMetrics);
        return merged;
    }

    public void setHostMetrics(List<UIMetric> metrics) {
        this.hostMetrics = metrics;
    }

    public List<UIMetric> getHostMetrics() {
        return this.hostMetrics;
    }

    public List<UIMetric> getInstanceMetrics() {
        return instanceMetrics;
    }

    public void setInstanceMetrics(List<UIMetric> instanceMetrics) {
        this.instanceMetrics = instanceMetrics;
    }

    public List<UIMetric> getStorageMetrics() {
        return storageMetrics;
    }

    public void setStorageMetrics(List<UIMetric> storageMetrics) {
        this.storageMetrics = storageMetrics;
    }

    public List<UIMetric> getNetworkMetrics() {
        return networkMetrics;
    }

    public void setNetworkMetrics(List<UIMetric> networkMetrics) {
        this.networkMetrics = networkMetrics;
    }

    public List<UIMetric> getResourcePoolMetrics() {
        return resourcePoolMetrics;
    }

    public void setResourcePoolMetrics(List<UIMetric> resourcePoolMetrics) {
        this.resourcePoolMetrics = resourcePoolMetrics;
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
        return instanceMetrics;
    }

    public boolean isEnableStorage() {
        return enableStorage;
    }

    public void setEnableStorage(boolean enableStorage) {
        this.enableStorage = enableStorage;
    }

}
