package org.groundwork.cloudhub.web;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.profile.ConfigServiceState;
import org.groundwork.cloudhub.profile.DataCenterProfileWrapper;
import org.groundwork.cloudhub.profile.ProfileServiceState;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;

import static org.groundwork.cloudhub.web.CloudHubUI.ERROR_MESSAGE;
import static org.groundwork.cloudhub.web.CloudHubUI.GWOS_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.READFAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.REMOTE_PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.RESULT;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_FAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_SUCCESS;
import static org.groundwork.cloudhub.web.CloudHubUI.SUCCESS;
import static org.groundwork.cloudhub.web.CloudHubUI.VMWARE_ERROR;

@Controller
@RequestMapping("/vmware2")
public class VmWareController extends HostController {

    protected static final String PROFILE_STATE = "profile-state-vmware";
    protected static final String CONFIG_STATE = "config-state-vmware";
    private static Logger log = Logger.getLogger(VmWareController.class);

    @RequestMapping(value = "/navigateCreateConnection", method = RequestMethod.GET)
    public ModelAndView navigateCreateConnection(HttpServletRequest request, HttpSession session) {

        log.info("configuration service = " + configurationService);

        VmwareConfiguration configuration = null;

        try {
            configuration = (VmwareConfiguration) configurationService.createConfiguration(VirtualSystem.VMWARE);
            configuration.getCommon().setUiCheckIntervalMinutes(String.valueOf(configuration.getCommon().getCheckIntervalMinutes()));
            request.getSession(true).setAttribute(HOSTNAME_STATE, HOSTNAME_NEW);
            setGwVersion(configuration, session);
            setConfigDefaultsByVersion(configuration, request);
        } catch (CloudHubException che) {
            log.error("Exception occurred navigating create vmware connection", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating create vmware connection", ex);
        }
        ConfigServiceState state = new ConfigServiceState();
        state.setView(ConfigServiceState.ConfigView.ViewStorage, configuration.getCommon().isStorageView());
        state.setView(ConfigServiceState.ConfigView.ViewNetwork, configuration.getCommon().isNetworkView());
        state.setView(ConfigServiceState.ConfigView.ViewPool, configuration.getCommon().isResourcePoolView());
        request.getSession(true).setAttribute(CONFIG_STATE, state);

        return new ModelAndView("vmware/create-connection", "configBean", configuration);
    }

    @RequestMapping(value = "saveConnectionConfiguration", method = RequestMethod.POST)
    public ModelAndView saveConnectionConfiguration(@Valid @ModelAttribute(value = "configBean") VmwareConfiguration configBean, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("vmware/create-connection", "configBean", configBean);
        }
        String result = saveConfiguration(configBean, request, session);
        request.setAttribute("result", result);
        // has storage view been turned off?
        ConfigServiceState state = (ConfigServiceState)request.getSession(true).getAttribute(CONFIG_STATE);
        List<String> views = new LinkedList();
        List<String> sourceTypes = new LinkedList<>();
        List<String> groupViews = new LinkedList();
        if (state != null && configBean.getCommon().isStorageView() == false && state.getView(ConfigServiceState.ConfigView.ViewStorage) == true) {
            views.add(ConnectorConstants.PREFIX_VM_STORAGE);
            groupViews.add(ConnectorConstants.PREFIX_STORAGE);
            sourceTypes.add(SourceType.storage.name());
        }
        if (state != null && configBean.getCommon().isNetworkView() == false && state.getView(ConfigServiceState.ConfigView.ViewNetwork) == true) {
            views.add(ConnectorConstants.PREFIX_VM_NETWORK);
            groupViews.add(ConnectorConstants.PREFIX_NETWORK);
            sourceTypes.add(SourceType.network.name());
        }
        if (views.size() > 0) {
            deleteViewMetrics(VirtualSystem.VMWARE, views, groupViews, sourceTypes, configBean, getCurrentUser(request));
        }
        state.setView(ConfigServiceState.ConfigView.ViewStorage, configBean.getCommon().isStorageView());
        state.setView(ConfigServiceState.ConfigView.ViewNetwork, configBean.getCommon().isNetworkView());
        state.setView(ConfigServiceState.ConfigView.ViewPool, configBean.getCommon().isResourcePoolView());
        return new ModelAndView("vmware/create-connection", "configBean", configBean);
    }

    @RequestMapping(value = "/testConnection", method = RequestMethod.POST)
    @ResponseBody
    protected String testConnection(@Valid @ModelAttribute(value = "configBean") VmwareConfiguration configuration, HttpServletRequest request,  BindingResult bindingResult, HttpSession session) {
        String result = SUCCESS;
        String message = "";
        try {
            request.setAttribute("createProfileEnabled", "true");
            GwosService gwosService = getGwosService(configuration);
            boolean success = gwosService.testConnection(configuration);
            if (!success) {
                result = GWOS_ERROR;
            } else {
                MonitoringConnector connector = connectorFactory.getMonitoringConnector(configuration);
                connector.testConnection(configuration.getConnection());
            }
        } catch (ConnectorException vex) {
            log.error("Exception occurred while testing vmware connection", vex);
            message = vex.getMessage();
            result = VMWARE_ERROR;
        } catch (CloudHubException che) {
            log.error("Exception occurred while testing vmware connection", che);
            message = che.getMessage();
            result = GWOS_ERROR;
        } catch (Exception ex) {
            log.error("Exception occurred while testing vmware connection", ex);
            message = ex.getMessage();
            result = GWOS_ERROR;
        }
        finally {
            if (result.equals(SUCCESS) || result.equals(SAVE_SUCCESS)) {
                configuration.getCommon().setCreateProfileDisabled(false);
                configuration.getCommon().setTestConnectionDisabled(false);
            }
        }
        request.setAttribute(RESULT, result);
        request.setAttribute(ERROR_MESSAGE, message);
        return "{ \"result\": \"" + result + "\", \"errorMessage\": \"" + message + "\"}";
    }

    @RequestMapping(value = "/navigateToProfile", method = RequestMethod.POST)
    public ModelAndView navigateToProfile(@ModelAttribute(value = "configBean") VmwareConfiguration configBean, HttpServletRequest request) {

        String result = "";
        CloudHubProfile cloudHubProfile = null;
        CloudHubProfile localCloudHubProfile = null;
        CloudHubProfile remoteCloudHubProfile = null;
        DataCenterProfileWrapper profileBean = null;

        try {
            request.getSession(true).removeAttribute(PROFILE_STATE);
            localCloudHubProfile = profileService.readCloudProfile(VirtualSystem.VMWARE, configBean.getCommon().getAgentId());
            remoteCloudHubProfile = profileService.readRemoteCloudProfile(VirtualSystem.VMWARE, configBean.getGwos());
            if (localCloudHubProfile == null && remoteCloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.VMWARE, configBean.getCommon().getAgentId());
                result = PROFILE_DOES_NOT_EXIST;
                request.setAttribute(RESULT, result);
                profileBean.setEnableStorage(configBean.getCommon().isStorageView());
                return new ModelAndView("vmware/create-connection", "configBean", configBean);
            } else {
                if (remoteCloudHubProfile == null) {
                    result = REMOTE_PROFILE_DOES_NOT_EXIST;
                }
                request.setAttribute(RESULT, result);
                cloudHubProfile = profileService.mergeCloudProfiles(VirtualSystem.VMWARE, remoteCloudHubProfile, localCloudHubProfile);
            }
            profileBean = new DataCenterProfileWrapper(cloudHubProfile, configBean.getCommon());
            ProfileServiceState state = new ProfileServiceState();
            state.addMetrics(profileBean.getHypervisorMetrics(), profileBean.getVmMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);

        } catch (Exception e) {
            log.error("Exception occurred while navigating vmware profile", e);
            if (cloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.VMWARE, configBean.getCommon().getAgentId());
            }
            profileBean = new DataCenterProfileWrapper(cloudHubProfile, configBean.getCommon());
            request.setAttribute(RESULT, READFAILURE);
            request.setAttribute(ERROR_MESSAGE, e.getMessage());
        }
        profileBean.setEnableStorage(configBean.getCommon().isStorageView());
        return new ModelAndView("vmware/assign-thresholds", "profileBean", profileBean);
    }

    @RequestMapping(value = "/saveConnectionProfile", method = RequestMethod.POST)
    public ModelAndView saveConnectionProfile(@Valid @ModelAttribute(value = "profileBean") DataCenterProfileWrapper profile, BindingResult bindingResult, HttpServletRequest request) {

        String result = SUCCESS;
        boolean enableStorage = false;
        try {

            if (bindingResult.hasErrors()) {
                ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());
                if (configuration != null) {
                    enableStorage = configuration.getCommon().isStorageView();
                }
                profile.setEnableStorage(enableStorage);
                return new ModelAndView("vmware/assign-thresholds", "profileBean", profile);
            }

            CloudHubProfile cloudHubProfile = profileService.readCloudProfile(VirtualSystem.VMWARE, profile.getAgent());
            if (cloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.VMWARE, profile.getAgent());
            }
            cloudHubProfile = profile.mergeToProfile(cloudHubProfile);
            profileService.saveProfile(cloudHubProfile);
            collectorService.setConfigurationUpdated(VirtualSystem.VMWARE);
            ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());
            if (!StringUtils.isEmpty(configuration.getCommon().getConfigurationFile())) {
                String agentIdentifier = configuration.getCommon().getConfigurationFile();
                collectorService.setConfigurationUpdated(agentIdentifier);
            }
            enableStorage = configuration.getCommon().isStorageView();
            // delete any metric services delete from UI from backend
            ProfileServiceState state = (ProfileServiceState)request.getSession(true).getAttribute(PROFILE_STATE);
            deleteRemovedMetrics(state, profile.getHypervisorMetrics(), profile.getVmMetrics(), configuration, getCurrentUser(request));
            state.addMetrics(profile.getHypervisorMetrics(), profile.getVmMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);
        } catch (CloudHubException che) {
            log.error("Exception occurred while saving vmware profile", che);
            result = SAVE_FAILURE;
        } catch (Exception ex) {
            log.error("Exception occurred while saving vmware profile", ex);
            result = SAVE_FAILURE;
        }

        request.setAttribute(RESULT, result);
        profile.setEnableStorage(enableStorage);
        return new ModelAndView("vmware/assign-thresholds", "profileBean", profile);
    }
}