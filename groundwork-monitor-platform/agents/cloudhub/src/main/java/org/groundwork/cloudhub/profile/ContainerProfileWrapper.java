package org.groundwork.cloudhub.profile;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.Metric;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContainerProfileWrapper extends BaseProfileWrapper {

	@Valid
	private List<UIMetric> engineMetrics;

	@Valid
	private List<UIMetric> containerMetrics;

	private String configFileName;

	private String configFilePath;

    public ContainerProfileWrapper() {
        super();
    }

    public ContainerProfileWrapper(ContainerProfile dockerProfile, CommonConfiguration common) {
        super(dockerProfile.getProfileType().name(), common.getAgentId());
        engineMetrics = new ArrayList<UIMetric>();
        for (Metric metric : dockerProfile.getEngine().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            engineMetrics.add(uiMetric);
        }
        containerMetrics = new ArrayList<UIMetric>();
        for (Metric metric : dockerProfile.getContainer().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            containerMetrics.add(uiMetric);
        }
        Collections.sort(engineMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(containerMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        setConfigFileName(common.getConfigurationFile());
        setConfigFilePath(common.getPathToConfigurationFile());
    }

    public ContainerProfile mergeToProfile(ContainerProfile profile) {
        profile.setAgent(getAgent());
        if (engineMetrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : engineMetrics) {
                boolean found = false;
                for (Metric metric : profile.getEngine().getMetrics()) {
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
                profile.getEngine().getMetrics().add(metric);
            }
        }
        if (containerMetrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : containerMetrics) {
                boolean found = false;
                for (Metric metric : profile.getContainer().getMetrics()) {
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
                profile.getContainer().getMetrics().add(metric);
            }
        }
        return profile;
    }


	public List<UIMetric> getEngineMetrics() {
		return engineMetrics;
	}

	public List<UIMetric> getContainerMetrics() {
		return containerMetrics;
	}

    public void setEngineMetrics(List<UIMetric> metrics) {
        this.engineMetrics = metrics;
    }

    public void setContainerMetrics(List<UIMetric> metrics) {
        this.containerMetrics = metrics;
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

}
