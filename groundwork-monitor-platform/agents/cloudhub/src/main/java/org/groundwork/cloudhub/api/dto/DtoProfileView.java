package org.groundwork.cloudhub.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.cloudhub.profile.BaseProfileWrapper;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.cloudhub.profile.ProfileMetricGroup;
import org.groundwork.cloudhub.profile.UIMetric;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Excludes;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.ProfileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dtaylor on 5/18/15.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoProfileView extends BaseProfileWrapper {

    private static Logger log = Logger.getLogger(DtoProfileView.class);

    private Map<String,ProfileMetricGroup> views = new HashMap<>();
    private List<String> excludes = new ArrayList<>();

    private String configFileName;

    private String configFilePath;
    private DtoProfileState state;
    private Boolean isService = true;

    public DtoProfileView() {
        super();
    }

    public DtoProfileView(CloudHubProfile cloudHubProfile, CommonConfiguration common, Boolean isConnected) {
        this(cloudHubProfile, common, isConnected, true, null);
    }

    public DtoProfileView(CloudHubProfile cloudHubProfile, CommonConfiguration common, Boolean isConnected, Boolean isService) {
        this(cloudHubProfile, common, isConnected, isService, null);
    }

    public DtoProfileView(CloudHubProfile cloudHubProfile, CommonConfiguration common, Boolean isConnected, Boolean isService, Map<String,String> displayNames) {
        super(cloudHubProfile.getProfileType().name(), common.getAgentId());
        this.isService = isService;
        buildGroupMetricList(cloudHubProfile.getHypervisor().getMetrics(), MetricType.hypervisor, cloudHubProfile.getProfileType());
        if (cloudHubProfile.getProfileType().equals(ProfileType.amazon)) {
            if (common.isCustomView()) {
                buildGroupMetricList(cloudHubProfile.getCustom().getMetrics(), MetricType.custom, cloudHubProfile.getProfileType());
            }
        }
        else {
            buildGroupMetricList(cloudHubProfile.getVm().getMetrics(), MetricType.vm, cloudHubProfile.getProfileType());
        }
        if (displayNames != null) {
            for (Map.Entry<String,ProfileMetricGroup> entry : views.entrySet()) {
                String displayName = displayNames.get(entry.getKey());
                if (displayName != null) {
                    entry.getValue().setDisplayName(displayName);
                }
            }
        }
        setConfigFileName(common.getConfigurationFile());
        setConfigFilePath(common.getPathToConfigurationFile());
        state = new DtoProfileState(isConnected);
        if (cloudHubProfile.getExcludes() != null) {
            for (String exclude : cloudHubProfile.getExcludes().getExcludes()) {
                this.excludes.add(exclude);
            }
        }
    }

    private void buildGroupMetricList(List<Metric> metrics, MetricType metricType, ProfileType profileType) {
        for (Metric metric : metrics) {
            UIMetric uiMetric = new UIMetric(metric);
            if (isService) {
                if (uiMetric.getServiceType() == null) {
                    log.error("Service type not set for metric: " + metric.getName());
                    continue;
                }
            }
            else {
                if (profileType.equals(ProfileType.amazon) || profileType.equals(ProfileType.vmware)) {
                    if (uiMetric.getServiceType() == null) {
                        uiMetric.setServiceType(uiMetric.getSourceType() == null ? metricType.name() : uiMetric.getSourceType());
                    }
                }
                else {
                    uiMetric.setServiceType(uiMetric.getSourceType() == null ? metricType.name() : uiMetric.getSourceType());
                    //uiMetric.setSourceType(metricType.name());
                }
            }
            ProfileMetricGroup group = views.get(uiMetric.getServiceType());
            if (group == null) {
                group = new ProfileMetricGroup(uiMetric.getServiceType(), metricType);
                views.put(uiMetric.getServiceType(), group);
            }
            group.addMetric(uiMetric);
        }
        for (ProfileMetricGroup group : views.values()) {
            Collections.sort(group.getMetrics(), new Comparator<UIMetric>() {
                @Override
                public int compare(UIMetric m1, UIMetric m2) {
                    return m1.getName().compareTo(m2.getName());
                }
            });
        }
    }

    public CloudHubProfile mergeToProfile(CloudHubProfile  profile) {
        profile.setAgent(getAgent());
        profile.setProfileType(ProfileType.valueOf(getProfileType()));
        for (ProfileMetricGroup view : this.getViews().values()) {
            for (UIMetric uiMetric : view.getMetrics()) {
                if (uiMetric.getServiceType() == null && !view.getMetricType().equals(MetricType.custom)) {
                    log.error("Service type not set for metric: " + uiMetric.getName());
                    continue;
                }
                if (view.getMetricType().equals(MetricType.hypervisor)) {
                   profile.getHypervisor().addMetric(createMetric(uiMetric));
                }
                else if (view.getMetricType().equals(MetricType.vm)) {
                    profile.getVm().addMetric(createMetric(uiMetric));
                }
                else if (view.getMetricType().equals(MetricType.custom)) {
                    profile.getCustom().addMetric(createMetric(uiMetric));
                }
                else {
                    log.error("Invalid metric type set for metric: " + uiMetric.getName() + ", " + view.getMetricType());
                    continue;
                }
            }
        }
        if (this.getExcludes() != null && this.getExcludes().size() > 0) {
            Excludes ex = new Excludes();
            for (String exclude : this.getExcludes()) {
                ex.addExclude(exclude);
            }
            profile.setExcludes(ex);
        }
        return profile;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public Map<String, ProfileMetricGroup> getViews() {
        return views;
    }

    public void setViews(Map<String, ProfileMetricGroup> views) {
        this.views = views;
    }

    public DtoProfileState getState() {
        return state;
    }

    public void setState(DtoProfileState state) {
        this.state = state;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    protected Metric createMetric(UIMetric uiMetric) {
        return new Metric(
                uiMetric.getName(),
                uiMetric.getDescription(),
                uiMetric.isMonitored(),
                uiMetric.isGraphed(),
                uiMetric.getWarningThreshold(),
                uiMetric.getCriticalThreshold(),
                uiMetric.getSourceType(),
                uiMetric.getComputeType(),
                uiMetric.getCustomName(),
                uiMetric.getExpression(),
                uiMetric.getFormat(),
                uiMetric.getServiceType());
    }

}
