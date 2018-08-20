package org.groundwork.cloudhub.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.groundwork.agents.monitor.VirtualSystem;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

@XmlRootElement(name = "common")
@XmlType(propOrder={"virtualSystem", "displayName", "agentId", "applicationType", "checkIntervalMinutes", "syncIntervalMinutes",
        "comaIntervalMinutes", "connectionRetries", "hypervisorView", "canAccessMultipleVersions", "storageView", "networkView",
        "resourcePoolView", "customView", "createProfileDisabled", "testConnectionDisabled", "serverSuspended", "configurationFile",
        "pathToConfigurationFile", "uiCheckIntervalMinutes", "uiSyncIntervalMinutes", "uiComaIntervalMinutes", "uiConnectionRetries",
        "enableGroupTag", "groupTag", "prefixServiceNames" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonConfiguration {

    private VirtualSystem virtualSystem;
    
    @NotBlank(message="Display name cannot be empty.")
    private String displayName;
    private String agentId;
    private String applicationType;

    private int checkIntervalMinutes = 5;
    private int syncIntervalMinutes = 2;
    private int comaIntervalMinutes = 15;
    private int connectionRetries = 10;

    @Pattern(regexp="^[1-9]\\d{0,3}$", message="Not a valid check interval (minutes). Valid 1-9999")
    private String uiCheckIntervalMinutes = "5";

    @Pattern(regexp="\\d+", message="Not a valid number.")
    private String uiSyncIntervalMinutes = "2";

    @Pattern(regexp="\\d+", message="Not a valid number.")
    private String uiComaIntervalMinutes = "15";

    @Pattern(regexp="0|-1|(^[1-9]\\d{0,3}$)", message="Not a valid retry interval. Valid 0, -1 or 1-999")
    private String uiConnectionRetries = "10";

    private boolean hypervisorView = true;
    private boolean storageView = false;
    private boolean networkView = false;
    private boolean resourcePoolView = false;
    private boolean customView = false;
    private boolean serverSuspended = true;
    private boolean testConnectionDisabled = true;
	private boolean createProfileDisabled = true;
	private boolean canAccessMultipleVersions = true;

    // 7.1.1 - Added Group Tagging feature for AWS, but put in core in case needed in future
    private boolean enableGroupTag = false;
    private String groupTag = "GWHostGroup";

	private String configurationFile;
    private String pathToConfigurationFile;

    // 7.2.0 / Cloudhub 2.3.0 for Cloudera initially
    private Boolean prefixServiceNames = false;

    public CommonConfiguration() {}

    public CommonConfiguration(VirtualSystem virtualSystem) {
        this.virtualSystem = virtualSystem;
        agentId = UUID.randomUUID().toString();
    }

    public VirtualSystem getVirtualSystem() {
        return virtualSystem;
    }

    public void setVirtualSystem(VirtualSystem virtualSystem) {
        this.virtualSystem = virtualSystem;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPathToConfigurationFile() {
        return pathToConfigurationFile;
    }

    public void setPathToConfigurationFile(String pathToConfigurationFile) {
        this.pathToConfigurationFile = pathToConfigurationFile;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    @XmlElement(name="checkInterval")
    public int getCheckIntervalMinutes() {
        return checkIntervalMinutes;
    }

    public void setCheckIntervalMinutes(int checkIntervalMinutes) {
        this.checkIntervalMinutes = checkIntervalMinutes;
    }

    @XmlElement(name="syncInterval")
    public int getSyncIntervalMinutes() {
        return syncIntervalMinutes;
    }

    public void setSyncIntervalMinutes(int syncIntervalMinutes) {
        this.syncIntervalMinutes = syncIntervalMinutes;
    }

    @XmlElement(name="comaInterval")
    public int getComaIntervalMinutes() {
        return comaIntervalMinutes;
    }

    public void setComaIntervalMinutes(int comaIntervalMinutes) {
        this.comaIntervalMinutes = comaIntervalMinutes;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public boolean isHypervisorView() {
        return hypervisorView;
    }

    public void setHypervisorView(boolean hypervisorView) {
        this.hypervisorView = hypervisorView;
    }

    public boolean isStorageView() {
        return storageView;
    }

    public void setStorageView(boolean storageView) {
        this.storageView = storageView;
    }

    public boolean isNetworkView() {
        return networkView;
    }

    public void setNetworkView(boolean networkView) {
        this.networkView = networkView;
    }

    public boolean isResourcePoolView() {
        return resourcePoolView;
    }

    public void setResourcePoolView(boolean resourcePoolView) {
        this.resourcePoolView = resourcePoolView;
    }

    public boolean isCustomView() {
        return customView;
    }

    public void setCustomView(boolean customView) {
        this.customView = customView;
    }

    public String getUiCheckIntervalMinutes() {
		return uiCheckIntervalMinutes;
	}

	public void setUiCheckIntervalMinutes(String uiCheckIntervalMinutes) {
		this.uiCheckIntervalMinutes = uiCheckIntervalMinutes;
	}

	public String getUiSyncIntervalMinutes() {
		return uiSyncIntervalMinutes;
	}

	public void setUiSyncIntervalMinutes(String uiSyncIntervalMinutes) {
		this.uiSyncIntervalMinutes = uiSyncIntervalMinutes;
	}

	public String getUiComaIntervalMinutes() {
		return uiComaIntervalMinutes;
	}

	public void setUiComaIntervalMinutes(String uiComaIntervalMinutes) {
		this.uiComaIntervalMinutes = uiComaIntervalMinutes;
	}
	
    public boolean isServerSuspended() {
		return serverSuspended;
	}

	public void setServerSuspended(boolean isServerSuspended) {
		this.serverSuspended = isServerSuspended;
	}

	public boolean isTestConnectionDisabled() {
		return testConnectionDisabled;
	}

	public void setTestConnectionDisabled(boolean testConnectionDisabled) {
		this.testConnectionDisabled = testConnectionDisabled;
	}

	public boolean isCreateProfileDisabled() {
		return createProfileDisabled;
	}

	public void setCreateProfileDisabled(boolean createProfileDisabled) {
		this.createProfileDisabled = createProfileDisabled;
	}

	public boolean isCanAccessMultipleVersions() {
		return canAccessMultipleVersions;
	}

	public void setCanAccessMultipleVersions(boolean canAccessMultipleVersions) {
		this.canAccessMultipleVersions = canAccessMultipleVersions;
	}

    public int getConnectionRetries() {
        return connectionRetries;
    }

    public void setConnectionRetries(int connectionRetries) {
        this.connectionRetries = connectionRetries;
    }

    public String getUiConnectionRetries() {
        return uiConnectionRetries;
    }

    public void setUiConnectionRetries(String uiConnectionRetries) {
        this.uiConnectionRetries = uiConnectionRetries;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getGroupTag() {
        return groupTag;
    }

    public void setGroupTag(String groupTag) {
        this.groupTag = groupTag;
    }

    public boolean isEnableGroupTag() {
        return enableGroupTag;
    }

    public void setEnableGroupTag(boolean enableGroupTag) {
        this.enableGroupTag = enableGroupTag;
    }

    public Boolean isPrefixServiceNames() {
        return prefixServiceNames;
    }
    
    @JsonProperty
    public Boolean getPrefixServiceNames() {
        return prefixServiceNames;
    }

    public void setPrefixServiceNames(Boolean prefixServiceNames) {
        this.prefixServiceNames = prefixServiceNames;
    }
}
