package org.groundwork.cloudhub.profile;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.NetHubProfile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NetHubProfileWrapper extends BaseProfileWrapper {

	@Valid
	private List<UIMetric> controllerMetrics;

	@Valid
	private List<UIMetric> switchMetrics;
	
	private String configFileName;
	
	private String configFilePath;

    public NetHubProfileWrapper() {
        super();
    }

    public NetHubProfileWrapper(NetHubProfile netHubProfile, CommonConfiguration common) {
        super(netHubProfile.getProfileType().name(), common.getAgentId());
        controllerMetrics = new ArrayList<UIMetric>();
        for (Metric metric : netHubProfile.getController().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            controllerMetrics.add(uiMetric);
        }
        switchMetrics = new ArrayList<UIMetric>();
        for (Metric metric : netHubProfile.getSwitch().getMetrics()) {
            UIMetric uiMetric = new UIMetric(metric);
            switchMetrics.add(uiMetric);
        }
        Collections.sort(controllerMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        Collections.sort(switchMetrics, new Comparator<UIMetric>() {
            @Override
            public int compare(UIMetric m1, UIMetric m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        setConfigFileName(common.getConfigurationFile());
        setConfigFilePath(common.getPathToConfigurationFile());
    }

    public NetHubProfile mergeToProfile(NetHubProfile profile) {
        profile.setAgent(getAgent());
        if (controllerMetrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : controllerMetrics) {
                boolean found = false;
                for (Metric metric : profile.getController().getMetrics()) {
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
                profile.getController().getMetrics().add(metric);
            }
        }
        if (switchMetrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : switchMetrics) {
                boolean found = false;
                for (Metric metric : profile.getSwitch().getMetrics()) {
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
                profile.getSwitch().getMetrics().add(metric);
            }
        }
        return profile;
    }


	public List<UIMetric> getControllerMetrics() {
		return controllerMetrics;
	}

	public List<UIMetric> getSwitchMetrics() {
		return switchMetrics;
	}

    public void setControllerMetrics(List<UIMetric> metrics) {
        this.controllerMetrics = metrics;
    }

    public void setSwitchMetrics(List<UIMetric> metrics) {
        this.switchMetrics = metrics;
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
