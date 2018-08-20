package org.groundwork.cloudhub.web;

import com.groundwork.collage.model.AuditLog;
import org.apache.log4j.Logger;
import org.groundwork.agents.configuration.GWOSVersion;
import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.MonitorChangeState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.*;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.gwos.GwosServiceFactory;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgent;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.profile.ProfileConversion;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.cloudhub.profile.ProfileServiceState;
import org.groundwork.cloudhub.profile.UIMetric;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.HubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_FAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_SUCCESS;

/**
 * The virtual machines are termed as host in ground work. The implementation of Red Hat and the VMWare are the
 * concrete forms of hosts. This class will act as a parent object for all the hosts and will be used
 * as a base class to put all the common functionalities related to the hosts.
 *
 * @author Muhammad Yousaf
 * @version 1.0
 *
 */

public class HostController {

    protected static final String GW_INFO_FILE_PATH       = "/usr/local/groundwork/Info.txt";
    protected static final String NOCHANGE = "<<nochange>>";
    protected static final String MOD_SYMBOL = "=>";
    protected static final int MAX_AUDIT_MESSAGE = 4095;
    protected static final String DEFAULT_USERNAME = "defaultUsername";
    protected static final String DEFAULT_APPLICATION_TYPE = "defaultApplicationType";
    protected static Logger log = Logger.getLogger(HostController.class);

    protected static final String HOSTNAME_STATE = "hostname-state";
    protected static final String HOSTNAME_NEW = "<<<-new->>>";

    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;

    @Resource(name = ProfileService.NAME)
    protected ProfileService profileService;

    @Resource(name = ConnectorFactory.NAME)
    protected ConnectorFactory connectorFactory;

    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory gwosServiceFactory;

    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

	/**
     * On load of the cloudhub app, version of the ground work installed will be checked
     * from file /usr/local/groundwork/Info.txt. 
     * 
     * Following are the cases
     * 
     * 1. If the file does not exists it means that user can access both 6.7 and 7.0 version
     * 2. If the files exists then version will be extracted from the above mentioned info file
     *    and user will have access only to the specified version.

	 * @param session
	 */
    protected void checkGwVersionInstalled(HttpSession session) {
        try {
            File file = new File(GW_INFO_FILE_PATH);
            file.setReadOnly();
            if (!file.exists()) {
                session.setAttribute("canAccessMultipleVersions", true);
            } else {
                Properties props = new Properties();
                try (FileInputStream input = new FileInputStream(file)) {
                    props.load(input);
                    String gwversion = props.getProperty("version");
                    if (gwversion.startsWith("7.0")) {
                        // 7.0.2-3 and beyond uses 7.1.0 APIs
                        String patchLevel = props.getProperty("PatchLevel");
                        if ((patchLevel != null) && !patchLevel.contains("7.0.2-1") && !patchLevel.contains("7.0.2-2")) {
                            gwversion = "7.1.0";
                        }
                    }
                    session.setAttribute("gwversion", gwversion);
                } catch (Exception fnfe) {
                    log.error("Failed to read Info file for version: " + fnfe.getMessage(), fnfe);
                }
                session.setAttribute("canAccessMultipleVersions", false);
            }
        } catch (Exception ex) {
            log.error("Exception occurred while checking the GWOS version installed", ex);
        }
    }

    /**
     * The configuration object that is binded to the front end pages needs to be
     * updated with the version set up at the load of the app. This method will set
     * up the ConnectionConfiguration object with the info of extracted from the file
     * /usr/local/groundwork/Info.txt
     * 
     * @param config
     * @param session
     */
	protected void setGwVersion (ConnectionConfiguration config, HttpSession session) {
		try {
            Boolean canAccessMultipleVersions = (Boolean)session.getAttribute("canAccessMultipleVersions");
            if (canAccessMultipleVersions == null)
                canAccessMultipleVersions = false;
			
			if (canAccessMultipleVersions) {
				config.getCommon().setCanAccessMultipleVersions(true);
			} else {
                String currentGwVersion = (String) session.getAttribute("gwversion");
                if (currentGwVersion != null) {
                    if (currentGwVersion.startsWith("7.0")) {
                        config.getGwos().setGwosVersion("7.0");
                        config.getGwos().setMergeHosts(GWOSConfiguration.DEFAULT_GWOS_70_MERGE_HOSTS);
                    } else {
                        config.getGwos().setGwosVersion("7.1");
                    }
                }
                else {
                    config.getGwos().setGwosVersion("7.1");
                }
				config.getCommon().setCanAccessMultipleVersions(false);
			}
    	} catch (Exception ex) {
    		log.error("Exception occurred while checking the GWOS version installed", ex);
    	}
	}

    protected void setConfigDefaultsByVersion(ConnectionConfiguration configuration, HttpServletRequest request)  {
        if (GWOSVersion.determineVersion(configuration.getGwos().getGwosVersion()).equals(GWOSVersion.version_71)) {
            configuration.getGwos().setWsUsername(GWOSConfiguration.DEFAULT_WS_71_USER);
            configuration.getGwos().setMergeHosts(GWOSConfiguration.DEFAULT_GWOS_71_MERGE_HOSTS);
        } else {
            configuration.getGwos().setMergeHosts(GWOSConfiguration.DEFAULT_GWOS_70_MERGE_HOSTS);
        }
        request.setAttribute(DEFAULT_USERNAME, configuration.getGwos().getWsUsername());
        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
        request.setAttribute(DEFAULT_APPLICATION_TYPE, provider.getApplicationType());
    }

    protected String saveConfiguration(ConnectionConfiguration configBean, HttpServletRequest request, HttpSession session) {
        String result = SAVE_SUCCESS;
        try {
            setGwVersion(configBean, session);
            if (configBean.getCommon().getUiCheckIntervalMinutes() != null) {
                configBean.getCommon().setCheckIntervalMinutes(Integer.parseInt(configBean.getCommon().getUiCheckIntervalMinutes()));
            }
            if (configBean.getCommon().getUiSyncIntervalMinutes() != null) {
                configBean.getCommon().setSyncIntervalMinutes(Integer.parseInt(configBean.getCommon().getUiSyncIntervalMinutes()));
            }
            if (configBean.getCommon().getUiComaIntervalMinutes() != null) {
                configBean.getCommon().setComaIntervalMinutes(Integer.parseInt(configBean.getCommon().getUiComaIntervalMinutes()));
            }
            if (configBean.getCommon().getUiConnectionRetries() != null) {
                configBean.getCommon().setConnectionRetries(Integer.parseInt(configBean.getCommon().getUiConnectionRetries()));
            }
            boolean isNew = false;
            ConnectionConfiguration oldConfig = null;
            String filePath = configBean.getCommon().getPathToConfigurationFile();
            if (filePath == null || filePath.trim().length() == 0) {
                isNew = true;
            }
            if (!isNew) {
                oldConfig = configurationService.readConfiguration(configBean.getCommon().getPathToConfigurationFile() +
                                                                        configBean.getCommon().getConfigurationFile());
            }
            configurationService.saveConfiguration(configBean);
            createNewProfile(configBean);

            String configName = configBean.getCommon().getConfigurationFile();
            if (!StringUtils.isEmpty(configName)) {
                CloudhubMonitorAgent agent = collectorService.lookup(configName);
                if (agent != null) {
                    agent.setConfigurationUpdated();
                }
            }
            session.setAttribute(HOSTNAME_STATE,
                    configBean.getGwos().getGwosServer() == null ? "" : configBean.getGwos().getGwosServer());

            String diffMessage = "(none)";
            diffMessage = (isNew) ? diffAddConfiguration(configBean) : diffModifyConfiguration(oldConfig, configBean);
            String message = trimAuditMessage(formatCommonConnection("Hub config", configBean.getCommon()) + " - " + diffMessage);
            getGwosService(configBean).auditLogHost(configBean.getCommon().getVirtualSystem(),
                    configBean.getConnection().getHostName(),
                    (isNew) ? AuditLog.Action.ADD.name() : AuditLog.Action.MODIFY.name(),
                    message,
                    getCurrentUser(request));

        } catch (CloudHubException che) {
            log.error("Exception occurred while saving connection", che);
            result = SAVE_FAILURE;
        } catch (Exception ex) {
            log.error("Exception occurred while saving connection", ex);
            result = SAVE_FAILURE;
        }
        if (result == SAVE_SUCCESS) {
            configBean.getCommon().setTestConnectionDisabled(false);
        }
        return result;
    }


    /**
     * sync up profile, make sure we always have 1:1 relationship
     * only create profile if it doesn't already exist
     *
     * @param configuration
     */
    protected void createNewProfile(ConnectionConfiguration configuration) {
        HubProfile profile = profileService.readProfile(configuration.getCommon().getVirtualSystem(), configuration.getCommon().getAgentId());
        if (profile == null) {
            HubProfile remoteProfile = profileService.readRemoteProfile(configuration.getCommon().getVirtualSystem(), configuration.getGwos());
            if (remoteProfile == null) {
                HubProfile localProfile = profileService.createProfile(configuration.getCommon().getVirtualSystem(), configuration.getCommon().getAgentId());
                if (localProfile.getProfileType() == null) {
                    localProfile.setProfileType(ProfileConversion.convertVirtualSystemToPropertyType(configuration.getCommon().getVirtualSystem()));
                }
                profileService.saveProfile(localProfile);
            }
            else {
                remoteProfile.setAgent(configuration.getCommon().getAgentId());
                if (remoteProfile.getProfileType() == null) {
                    remoteProfile.setProfileType(ProfileConversion.convertVirtualSystemToPropertyType(configuration.getCommon().getVirtualSystem()));
                }
                profileService.saveProfile(remoteProfile);
            }
        }
    }

    protected int deleteRemovedMetrics(ProfileServiceState state, List<UIMetric> primaryMetrics, List<UIMetric> secondaryMetrics,
                                       ConnectionConfiguration configuration, String userName) {
        return deleteRemovedMetrics(state, primaryMetrics, secondaryMetrics, null, configuration, userName, false);
    }

    protected int deleteRemovedMetrics(ProfileServiceState state, List<UIMetric> primaryMetrics, List<UIMetric> secondaryMetrics,
                                       List<UIMetric> customMetrics,
                                       ConnectionConfiguration configuration, String userName,
                                       boolean usePrefix) {
        int count = 0;
        CloudhubAgentInfo agentInfo = collectorService.createMonitorAgentInfo(configuration);
        GwosService gwosService = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
        auditLogProfileDiffs(gwosService, state, primaryMetrics, secondaryMetrics, customMetrics, configuration, userName);
        if (state != null) {
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(configuration.getCommon().getVirtualSystem());
            List<DeleteServiceInfo> deletedPrimaryServices = provider.createDeleteServiceList(state.determineDeletedPrimaryServices(primaryMetrics, usePrefix));
            List<DeleteServiceInfo> deletedSecondaryServices = provider.createDeleteServiceList(state.determineDeletedSecondaryServices(secondaryMetrics, usePrefix));
            List<DeleteServiceInfo> deletedCustomServices = provider.createDeleteServiceList(state.determineDeletedCustomServices(customMetrics, usePrefix));
            if (deletedPrimaryServices.size() > 0 || deletedSecondaryServices.size() > 0 || deletedCustomServices.size() > 0) {
                CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
                if (agent != null) {
                    agent.submitRequestToDeleteServices(new MonitorChangeState(deletedPrimaryServices, deletedSecondaryServices, deletedCustomServices, userName, false));
                    return deletedPrimaryServices.size() + deletedSecondaryServices.size() + deletedCustomServices.size();
                }
            }
            state.reset();
        }
        return count;
    }

    protected boolean suspendAgent(ConnectionConfiguration configuration, HttpServletRequest request) {
        String configName = configuration.getCommon().getConfigurationFile();
        if (!StringUtils.isEmpty(configName)) {
            CloudhubMonitorAgent agent = collectorService.lookup(configName);
            if (agent != null) {
                configuration.getCommon().setServerSuspended(true);
                agent.suspend();
                saveConfiguration(configuration, request, request.getSession(true));
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/hostNameChanged", method = RequestMethod.GET)
    public @ResponseBody String hostNameChanged(@RequestParam("hostName") String newHost, HttpServletRequest request) {

        String previousHost = (String) request.getSession(true).getAttribute(HOSTNAME_STATE);
        if (previousHost == null)
            previousHost = "";
        if (previousHost.equals(HOSTNAME_NEW)) {
            return NOCHANGE;
        }
        if (previousHost.equals(newHost)) {
            return NOCHANGE;
        }
        return previousHost;
    }

    protected String getCurrentUser(HttpServletRequest request) {
        String username = request.getRemoteUser();
        return (username == null) ? "guest" : username;
    }

    protected String formatCommonConnection(String message, CommonConfiguration common) {
        return String.format("%s: appType: %s, name: %s",
                message, common.getVirtualSystem().name(), common.getDisplayName());
    }

    protected String diffAddConfiguration(ConnectionConfiguration config) {
        StringBuffer diffs = new StringBuffer();
        // Common
        formatAdd(diffs, config.getCommon().getDisplayName(), "display");
        formatAdd(diffs, Integer.toString(config.getCommon().getCheckIntervalMinutes()), "interval");
        formatAdd(diffs, Integer.toString(config.getCommon().getConnectionRetries()), "retries");
        // GWOS
        formatAdd(diffs, config.getGwos().getGwosServer(), "gwServer");
        formatAdd(diffs, config.getGwos().getWsUsername(), "gwUser");
        formatAdd(diffs, Boolean.toString(config.getGwos().isGwosSSLEnabled()), "gwSSL");
        formatAdd(diffs, config.getGwos().getGwosVersion(), "gwVersion");
        // Base Vema Connection
        formatAdd(diffs, config.getConnection().getServer(), "server");
        // Base Secure Vema Connection
        if (config.getConnection() instanceof SecureMonitorConnection) {
            SecureMonitorConnection secure = ((SecureMonitorConnection) config.getConnection());
            formatAdd(diffs, secure.getUsername(), "user");
            formatAdd(diffs, Boolean.toString(secure.isSslEnabled()), "SSL");
        }
        if (config.getConnection() instanceof VmwareConnection) {
            VmwareConnection vmware = ((VmwareConnection) config.getConnection());
            formatAdd(diffs, vmware.getUri(), "URI");
        }
        else if (config.getConnection() instanceof RedhatConnection) {
            RedhatConnection redhat = ((RedhatConnection) config.getConnection());
            formatAdd(diffs, redhat.getUrl(), "URL");
            formatAdd(diffs, redhat.getRealm(), "realm");
            formatAdd(diffs, redhat.getCertificateStore(), "certStore");
        }
        else if (config.getConnection() instanceof OpenStackConnection) {
            OpenStackConnection os = ((OpenStackConnection) config.getConnection());
            formatAdd(diffs, os.getTenantId(), "tenantId");
            formatAdd(diffs, os.getTenantName(), "tenantName");
            formatAdd(diffs, os.getNovaPort(), "novaPort");
            formatAdd(diffs, os.getKeystonePort(), "keystonePort");
            formatAdd(diffs, os.getCeilometerPort(), "ceilPort");
        }
        else if (config.getConnection() instanceof OpenShiftConnection) {

        }
        else if (config.getConnection() instanceof OpenDaylightConnection) {

        }
        else if (config.getConnection() instanceof NSXConnection) {

        }
        else if (config.getConnection() instanceof DockerConnection) {
            DockerConnection oldDocker = ((DockerConnection) config.getConnection());
            formatAdd(diffs, oldDocker.getPrefix(), "prefix");
        }
        else if (config.getConnection() instanceof CiscoConnection) {

        }
        if (config.getConnection() instanceof RedhatConnection ||
            config.getConnection() instanceof VmwareConnection ||
            config.getConnection() instanceof AmazonConnection) {
            formatAdd(diffs, Boolean.toString(config.getCommon().isHypervisorView()), "viewH");
            formatAdd(diffs, Boolean.toString(config.getCommon().isNetworkView()), "viewN");
            formatAdd(diffs, Boolean.toString(config.getCommon().isStorageView()), "viewS");
            formatAdd(diffs, Boolean.toString(config.getCommon().isResourcePoolView()), "viewRP");
        }
        return diffs.toString();
    }

    protected String diffModifyConfiguration(ConnectionConfiguration oldConfig, ConnectionConfiguration newConfig) {
        StringBuffer diffs = new StringBuffer();
        // Common
        if (changed(oldConfig.getCommon().getDisplayName(), newConfig.getCommon().getDisplayName())) {
            formatDiffs(diffs, oldConfig.getCommon().getDisplayName(), newConfig.getCommon().getDisplayName(), "display");
        }
        if (oldConfig.getCommon().getCheckIntervalMinutes() != newConfig.getCommon().getCheckIntervalMinutes()) {
            formatDiffs(diffs, Integer.toString(oldConfig.getCommon().getCheckIntervalMinutes()),
                    Integer.toString(newConfig.getCommon().getCheckIntervalMinutes()), "interval");
        }
        if (oldConfig.getCommon().getConnectionRetries() != newConfig.getCommon().getConnectionRetries()) {
            formatDiffs(diffs, Integer.toString(oldConfig.getCommon().getConnectionRetries()),
                            Integer.toString(newConfig.getCommon().getConnectionRetries()), "retries");
        }
        // GWOS
        if (changed(oldConfig.getGwos().getGwosServer(), newConfig.getGwos().getGwosServer())) {
            formatDiffs(diffs, oldConfig.getGwos().getGwosServer(), newConfig.getGwos().getGwosServer(), "gwServer");
        }
        if (changed(oldConfig.getGwos().getWsUsername(), newConfig.getGwos().getWsUsername())) {
            formatDiffs(diffs, oldConfig.getGwos().getWsUsername(), newConfig.getGwos().getWsUsername(), "gwUser");
        }
        if (changed(oldConfig.getGwos().getWsPassword(), newConfig.getGwos().getWsPassword())) {
            diffs.append("[GWOS Password changed]");
        }
        if (oldConfig.getGwos().isGwosSSLEnabled() != newConfig.getGwos().isGwosSSLEnabled()) {
            formatDiffs(diffs, Boolean.toString(oldConfig.getGwos().isGwosSSLEnabled()),
                    Boolean.toString(newConfig.getGwos().isGwosSSLEnabled()), "gwSSL");
        }
        if (changed(oldConfig.getGwos().getGwosVersion(), newConfig.getGwos().getGwosVersion())) {
            formatDiffs(diffs, oldConfig.getGwos().getGwosVersion(), newConfig.getGwos().getGwosVersion(), "gwVersion");
        }
        // Base Vema Connection
        if (changed(oldConfig.getConnection().getServer(), newConfig.getConnection().getServer())) {
            formatDiffs(diffs, oldConfig.getConnection().getServer(), newConfig.getConnection().getServer(), "server");
        }
        // Base Secure Vema Connection
        if (oldConfig.getConnection() instanceof SecureMonitorConnection) {
            SecureMonitorConnection oldSecure = ((SecureMonitorConnection)oldConfig.getConnection());
            SecureMonitorConnection newSecure = ((SecureMonitorConnection)newConfig.getConnection());
            if (changed(oldSecure.getUsername(), newSecure.getUsername())) {
                formatDiffs(diffs, oldSecure.getUsername(), newSecure.getUsername(), "user");
            }
            if (changed(oldSecure.getPassword(), newSecure.getPassword())) {
                diffs.append("[Connection Password changed]");
            }
            if (oldSecure.isSslEnabled() != newSecure.isSslEnabled()) {
                formatDiffs(diffs, Boolean.toString(oldSecure.isSslEnabled()),
                        Boolean.toString(newSecure.isSslEnabled()), "SSL");
            }
        }
        if (oldConfig.getConnection() instanceof VmwareConnection) {
            VmwareConnection oldVmware = ((VmwareConnection)oldConfig.getConnection());
            VmwareConnection newVmware = ((VmwareConnection)newConfig.getConnection());
            if (changed(oldVmware.getUri(), newVmware.getUri())) {
                formatDiffs(diffs, oldVmware.getUri(), newVmware.getUri(), "URI");
            }
        }
        else if (oldConfig.getConnection() instanceof RedhatConnection) {
            RedhatConnection oldRedhat = ((RedhatConnection)oldConfig.getConnection());
            RedhatConnection newRedhat = ((RedhatConnection)newConfig.getConnection());
            if (changed(oldRedhat.getUrl(), newRedhat.getUrl())) {
                formatDiffs(diffs, oldRedhat.getUrl(), newRedhat.getUrl(), "URL");
            }
            if (changed(oldRedhat.getRealm(), newRedhat.getRealm())) {
                formatDiffs(diffs, oldRedhat.getRealm(), newRedhat.getRealm(), "realm");
            }
            if (changed(oldRedhat.getCertificateStore(), newRedhat.getCertificateStore())) {
                formatDiffs(diffs, oldRedhat.getCertificateStore(), newRedhat.getCertificateStore(), "certStore");
            }
            if (changed(oldRedhat.getCertificatePassword(), newRedhat.getCertificatePassword())) {
                diffs.append("[Redhat Certificate Password changed]");
            }
        }
        else if (oldConfig.getConnection() instanceof OpenStackConnection) {
            OpenStackConnection oldOS = ((OpenStackConnection)oldConfig.getConnection());
            OpenStackConnection newOS = ((OpenStackConnection)newConfig.getConnection());
            if (changed(oldOS.getTenantId(), newOS.getTenantId())) {
                formatDiffs(diffs, oldOS.getTenantId(), newOS.getTenantId(), "tenantId");
            }
            if (changed(oldOS.getTenantName(), newOS.getTenantName())) {
                formatDiffs(diffs, oldOS.getTenantName(), newOS.getTenantName(), "tenantName");
            }
            if (changed(oldOS.getNovaPort(), newOS.getNovaPort())) {
                formatDiffs(diffs, oldOS.getNovaPort(), newOS.getNovaPort(), "novaPort");
            }
            if (changed(oldOS.getKeystonePort(), newOS.getKeystonePort())) {
                formatDiffs(diffs, oldOS.getKeystonePort(), newOS.getKeystonePort(), "keystonePort");
            }
            if (changed(oldOS.getCeilometerPort(), newOS.getCeilometerPort())) {
                formatDiffs(diffs, oldOS.getCeilometerPort(), newOS.getCeilometerPort(), "ceilPort");
            }
        }
        else if (oldConfig.getConnection() instanceof OpenShiftConnection) {

        }
        else if (oldConfig.getConnection() instanceof OpenDaylightConnection) {

        }
        else if (oldConfig.getConnection() instanceof NSXConnection) {

        }
        else if (oldConfig.getConnection() instanceof DockerConnection) {
            DockerConnection oldDocker = ((DockerConnection)oldConfig.getConnection());
            DockerConnection newDocker = ((DockerConnection)newConfig.getConnection());
            if (changed(oldDocker.getPrefix(), newDocker.getPrefix())) {
                formatDiffs(diffs, oldDocker.getPrefix(), newDocker.getPrefix(), "prefix");
            }
        }
        else if (oldConfig.getConnection() instanceof CiscoConnection) {

        }
        else if (oldConfig.getConnection() instanceof AmazonConnection) {

        }
        if (oldConfig.getConnection() instanceof RedhatConnection ||
                oldConfig.getConnection() instanceof VmwareConnection ||
                oldConfig.getConnection() instanceof AmazonConnection) {
            if (oldConfig.getCommon().isHypervisorView() != newConfig.getCommon().isHypervisorView()) {
                formatDiffs(diffs, Boolean.toString(oldConfig.getCommon().isHypervisorView()),
                        Boolean.toString(newConfig.getCommon().isHypervisorView()), "viewH");
            }
            if (oldConfig.getCommon().isNetworkView() != newConfig.getCommon().isNetworkView()) {
                formatDiffs(diffs, Boolean.toString(oldConfig.getCommon().isNetworkView()),
                        Boolean.toString(newConfig.getCommon().isNetworkView()), "viewN");
            }
            if (oldConfig.getCommon().isStorageView() != newConfig.getCommon().isStorageView()) {
                formatDiffs(diffs, Boolean.toString(oldConfig.getCommon().isStorageView()),
                        Boolean.toString(newConfig.getCommon().isStorageView()), "viewS");
            }
            if (oldConfig.getCommon().isResourcePoolView() != newConfig.getCommon().isResourcePoolView()) {
                formatDiffs(diffs, Boolean.toString(oldConfig.getCommon().isResourcePoolView()),
                        Boolean.toString(newConfig.getCommon().isResourcePoolView()), "viewRP");
            }
        }
        return diffs.toString();
    }

    protected boolean changed(String oldValue, String newValue) {
        if (oldValue == null && newValue != null)
            return true;
        if (oldValue != null && newValue == null)
            return true;
        if (oldValue == null && newValue == null)
            return false;
        return !oldValue.equals(newValue);
    }

    protected StringBuffer formatDiffs(StringBuffer diffs, String oldValue, String newValue, String field) {
        diffs.append("[");
        diffs.append(field);
        diffs.append(":");
        if (oldValue == null)
            oldValue = "";
        diffs.append(oldValue);
        diffs.append(MOD_SYMBOL);
        if (newValue == null)
            newValue = "";
        diffs.append(newValue);
        diffs.append("]");
        return diffs;
    }

    protected StringBuffer formatAdd(StringBuffer diffs, String value, String field) {
        diffs.append("[");
        diffs.append(field);
        diffs.append(":");
        if (value == null)
            value = "";
        diffs.append(value);
        diffs.append("]");
        return diffs;
    }

    protected GwosService getGwosService(ConnectionConfiguration connectionConfig) {
        CloudhubAgentInfo agentInfo = collectorService.createMonitorAgentInfo(connectionConfig);
        return gwosServiceFactory.getGwosServicePrototype(connectionConfig, agentInfo);
    }

    protected String calculateDiffs(ProfileServiceState state,
                                    List<UIMetric> newPrimary, List<UIMetric> newSecondary, List<UIMetric> newCustom) {
        if (state == null) {
            return null;
        }
        StringBuffer diffs = new StringBuffer();
        if (newPrimary != null) {
            for (UIMetric newMetric : newPrimary) {
                UIMetric metric = state.getPrimaryMetrics().get(newMetric.getName());
                if (metric != null) {
                    processMetricsDiffs(diffs, metric, newMetric);
                }
            }
        }
        if (newSecondary != null) {
            for (UIMetric newMetric : newSecondary) {
                UIMetric metric = state.getSecondaryMetrics().get(newMetric.getName());
                if (metric != null) {
                    processMetricsDiffs(diffs, metric, newMetric);
                }
            }
        }
        if (newCustom != null) {
            for (UIMetric newMetric : newCustom) {
                UIMetric metric = state.getCustomMetrics().get(newMetric.getName());
                if (metric != null) {
                    processMetricsDiffs(diffs, metric, newMetric);
                }
            }
        }
        return diffs.toString();
    }

    private void processMetricsDiffs(StringBuffer diffs, UIMetric metric, UIMetric newMetric) {
        if (metric != null) {
            if (metric.isGraphed() != newMetric.isGraphed()) {
                formatDiffs(diffs, Boolean.toString(metric.isGraphed()),
                        Boolean.toString(newMetric.isGraphed()), metric.getName() + ":isGraphed");
            }
            if (metric.isMonitored() != newMetric.isMonitored()) {
                formatDiffs(diffs, Boolean.toString(metric.isMonitored()),
                        Boolean.toString(newMetric.isMonitored()), metric.getName() + ":isMonitored");
            }
            if (!metric.getUiWarningThreshold().equals(newMetric.getUiWarningThreshold())) {
                formatDiffs(diffs, metric.getUiWarningThreshold(), newMetric.getUiWarningThreshold(), metric.getName() + ":warning");
            }
            if (!metric.getUiCriticalThreshold().equals(newMetric.getUiCriticalThreshold())) {
                formatDiffs(diffs, metric.getUiCriticalThreshold(), newMetric.getUiCriticalThreshold(), metric.getName() + ":critical");
            }
            if (!metric.getServiceName().equals((newMetric.getServiceName()))) {
                formatDiffs(diffs, metric.getServiceName(), newMetric.getServiceName(), metric.getName() + ":serviceName");
            }
        }
    }

    protected void auditLogProfileDiffs(GwosService gwosService, ProfileServiceState state,
                                        List<UIMetric> newPrimary, List<UIMetric> newSecondary, List<UIMetric> newCustom,
                                     ConnectionConfiguration config, String currentUser) {
        String diffMessage = "(none)";
        diffMessage = calculateDiffs(state, newPrimary, newSecondary, newCustom);
        if (diffMessage != null) {
            String message = trimAuditMessage(formatCommonConnection("Hub profile", config.getCommon()) + " - " + diffMessage);
            gwosService.auditLogHost(config.getCommon().getVirtualSystem(),
                    config.getConnection().getHostName(),
                    AuditLog.Action.MODIFY.name(),
                    message,
                    currentUser);
        }
    }

    protected String trimAuditMessage(String message) {
        String trim = null;
        if (message.length() > MAX_AUDIT_MESSAGE) {
            trim = message.substring(0, MAX_AUDIT_MESSAGE);
        }
        else {
            trim = new String(message);
        }
        return trim;
    }

    protected void mergeParsedProfileExtraState(String extraState, List<Metric> hypervisorMetrics, List<Metric> vmMetrics) {
        HashMap<String, Metric> metricMap = new HashMap<String, Metric>();
        boolean havePrimary = false;
        List<Metric> metrics = hypervisorMetrics;
        for (String categoryState : extraState.split("\\$")) {
            if (havePrimary) {
                if (metrics == vmMetrics) {
                    break;
                } else {
                    metrics = vmMetrics;
                }
            }
            if (!havePrimary || metrics != null) {
                metricMap.clear();
                for (Metric metric : metrics) {
                    metricMap.put(metric.getName(), metric);
                }
                metrics = null;
            }
            for (String rowState : categoryState.split("\\&")) {
                String[] colState = rowState.split("\\|");
                if (colState.length == 0) {
                    continue;
                }
                havePrimary = true;
                Metric metric = metricMap.get(colState[0]);
                if (metric == null) {
                    continue;
                }
                
                metric.setName(colState[0]);
                if (colState.length > 1) {
                    metric.setMonitored(Boolean.parseBoolean(colState[1]));
                }
                if (colState.length > 2) {
                    metric.setGraphed(Boolean.parseBoolean(colState[2]));
                }
                if (colState.length > 3) {
                    metric.setWarningThreshold(Double.parseDouble(colState[3]));
                }
                if (colState.length > 4) {
                    metric.setCriticalThreshold(Double.parseDouble(colState[4]));
                }
                // We will not merge description data since delimiters in the
                // descriptions will be difficult to add back.
            }
        }
    }

    protected int deleteViewMetrics(VirtualSystem virtualSystem, List<String> views, List<String> groupViews, List<String> sourceTypes,
                                    ConnectionConfiguration configuration, String userName) {
        return deleteViewMetrics(virtualSystem, views, groupViews, sourceTypes, configuration, userName, false);
    }

    protected int deleteViewMetrics(VirtualSystem virtualSystem, List<String> views, List<String> groupViews, List<String> sourceTypes,
                                       ConnectionConfiguration configuration, String userName, boolean deleteMetricsBySourceType) {

        try {
            CloudHubProfile localCloudHubProfile = profileService.readCloudProfile(virtualSystem, configuration.getCommon().getAgentId());
            if (localCloudHubProfile == null) {
                return 0;
            }
            List<Metric> metrics = localCloudHubProfile.getHypervisor().getMetrics();
            List<String> deleteServices = new ArrayList<>();
            for (Metric metric : metrics) {
                if (metric.getSourceType() != null && sourceTypes.contains(metric.getSourceType())) {
                    deleteServices.add(metric.getServiceName());
                }
            }
            CloudhubMonitorAgent agent = collectorService.lookup(configuration.getCommon().getConfigurationFile());
            if (agent != null) {
                if (deleteMetricsBySourceType) {
                    agent.submitRequestToDeleteView(new MonitorChangeState(userName, views, groupViews, deleteServices, sourceTypes));
                }
                else {
                    agent.submitRequestToDeleteView(new MonitorChangeState(userName, views, groupViews, deleteServices));
                }
                return deleteServices.size();
            }
        }
        catch (Exception e) {
            log.error("Failed to delete metrics view for " + views.toString(), e);
        }
        return -1;
    }

    protected List<UIMetric> mergeCustomMetrics(List<UIMetric> profileMetrics, List<Metric> customMetrics) {
        List<UIMetric> mergedCustom = new ArrayList<>();
        Map<String, UIMetric> uniqueCustomMetrics = new HashMap<String, UIMetric>();
        for (UIMetric customMetric : profileMetrics) {
            uniqueCustomMetrics.put(customMetric.getName(), customMetric);
        }
        for (Metric customMetric : customMetrics) {
            UIMetric metric = uniqueCustomMetrics.get(customMetric.getName());
            if (metric == null) {
                uniqueCustomMetrics.put(customMetric.getName(), new UIMetric(customMetric));
            }
        }
        for (UIMetric metric : uniqueCustomMetrics.values()) {
            mergedCustom.add(metric);
        }
        return mergedCustom;
    }

}

