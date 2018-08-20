package org.groundwork.cloudhub.profile;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CloudHubProfileWrapper extends BaseProfileWrapper {

	@Valid
	private List<UIMetric> hypervisorMetrics;

	@Valid
	private List<UIMetric> vmMetrics;

    @Valid
    private List<UIMetric> customMetrics;

	private String configFileName;
	
	private String configFilePath;
	
	private String extraState;

    private boolean enableCustom = false;
    private boolean enableStorage = false;

    public CloudHubProfileWrapper() {
        super();
    }

    public CloudHubProfileWrapper(CloudHubProfile cloudHubProfile, CommonConfiguration common) {
        this(cloudHubProfile, common.getAgentId(), common.getPathToConfigurationFile(), common.getConfigurationFile(),
                common.isCustomView(), common.isStorageView());
    }
    
    public CloudHubProfileWrapper(CloudHubProfile cloudHubProfile, String agentId, String configPath, String configFile, boolean enableCustom, boolean enableStorage) {
        super(cloudHubProfile.getProfileType().name(), agentId);
        this.enableCustom = enableCustom;
        this.enableStorage = enableStorage;
        hypervisorMetrics = new ArrayList<UIMetric>();
        for (Metric metric : cloudHubProfile.getHypervisor().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            hypervisorMetrics.add(uiMetric);
        }
        vmMetrics = new ArrayList<UIMetric>();
        for (Metric metric : cloudHubProfile.getVm().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            vmMetrics.add(uiMetric);
        }
        customMetrics = new ArrayList<>();
        for (Metric metric : cloudHubProfile.getCustom().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            customMetrics.add(uiMetric);
        }
        Collections.sort(hypervisorMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(vmMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(customMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        setConfigFileName(configFile);
        setConfigFilePath(configPath);
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
        if (vmMetrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : vmMetrics) {
                boolean found = false;
                for (Metric metric : profile.getVm().getMetrics()) {
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
                profile.getVm().getMetrics().add(metric);
            }
        }
        if (customMetrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : customMetrics) {
                boolean found = false;
                for (Metric metric : profile.getCustom().getMetrics()) {
                    if (uiMetric.getName().equals(metric.getName())) {
                        mergeMetric(metric, uiMetric, true);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Metric metric = createMetric(uiMetric);
                    metric.setDescription(uiMetric.getDescription());
                    additional.add(metric);
                }
            }
            for (Metric metric : additional) {
                profile.getCustom().getMetrics().add(metric);
            }
        }

        return profile;
    }

	public List<UIMetric> getHypervisorMetrics() {
		return hypervisorMetrics;
	}

    public void setHypervisorMetrics(List<UIMetric> metrics) {
        this.hypervisorMetrics = metrics;
    }

	public List<UIMetric> getVmMetrics() {
		return vmMetrics;
	}

    public void setVmMetrics(List<UIMetric> metrics) {
        this.vmMetrics = metrics;
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

    public String getExtraState() {
        return extraState;
    }

    public void setExtraState(String extraState) {
        this.extraState = extraState;
    }

    public List<UIMetric> getCustomMetrics() {
        return customMetrics;
    }

    public void setCustomMetrics(List<UIMetric> customMetrics) {
        this.customMetrics = customMetrics;
    }

    public boolean isEnableCustom() {
        return enableCustom;
    }

    public void setEnableCustom(boolean enableCustom) {
        this.enableCustom = enableCustom;
    }

    public boolean isEnableStorage() {
        return enableStorage;
    }

    public void setEnableStorage(boolean enableStorage) {
        this.enableStorage = enableStorage;
    }
}
