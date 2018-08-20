package org.groundwork.cloudhub.web;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.AmazonConfiguration;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.amazon.AmazonConfigurationProvider;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.profile.CloudHubProfileWrapper;
import org.groundwork.cloudhub.profile.ConfigServiceState;
import org.groundwork.cloudhub.profile.ProfileServiceState;
import org.groundwork.cloudhub.profile.UIMetric;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.groundwork.cloudhub.web.CloudHubUI.AMAZON_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.ERROR_MESSAGE;
import static org.groundwork.cloudhub.web.CloudHubUI.GWOS_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.REMOTE_PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.RESULT;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_FAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_SUCCESS;
import static org.groundwork.cloudhub.web.CloudHubUI.SUCCESS;
import static org.groundwork.cloudhub.web.CloudHubUI.SUCCESS_REFRESH;


/**
 * Controller to handle the AmazonConnector 
 */
@Controller
@RequestMapping("/amazon2")
public class AmazonController  extends HostController {

    protected static final String PROFILE_STATE = "profile-state-amazon";
    protected static final String CONFIG_STATE = "config-state-amazon";

    private static Logger log = Logger.getLogger(AmazonController.class);

    @RequestMapping(value = "/navigateCreateConnection", method = RequestMethod.GET)
    public ModelAndView navigateCreateConnection(HttpServletRequest request, HttpSession session) {

        log.info("configuration service = " + configurationService);

        AmazonConfiguration configuration = null;

        try {
            configuration = (AmazonConfiguration) configurationService.createConfiguration(VirtualSystem.AMAZON);
            configuration.getCommon().setUiCheckIntervalMinutes(String.valueOf(configuration.getCommon().getCheckIntervalMinutes()));
            request.getSession(true).setAttribute(HOSTNAME_STATE, HOSTNAME_NEW);
            setGwVersion(configuration, session);
            setConfigDefaultsByVersion(configuration, request);
        } catch (CloudHubException che) {
            log.error("Exception occurred navigating create amazon connection", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating create amazon connection", ex);
        }
        ConfigServiceState state = new ConfigServiceState();
        state.setView(ConfigServiceState.ConfigView.ViewStorage, configuration.getCommon().isStorageView());
        state.setView(ConfigServiceState.ConfigView.ViewNetwork, configuration.getCommon().isNetworkView());
        state.setView(ConfigServiceState.ConfigView.ViewPool, configuration.getCommon().isResourcePoolView());
        state.setView(ConfigServiceState.ConfigView.ViewCustom, configuration.getCommon().isCustomView());
        request.getSession(true).setAttribute(CONFIG_STATE, state);
        return new ModelAndView("amazon2/create-connection", "configBean", configuration);
    }

    @RequestMapping(value = "saveConnectionConfiguration", method = RequestMethod.POST)
    public ModelAndView saveConnectionConfiguration(@Valid @ModelAttribute(value = "configBean") AmazonConfiguration configBean, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("amazon2/create-connection", "configBean", configBean);
        }
        String result = saveConfiguration(configBean, request, session);
        request.setAttribute("result", result);
        // has storage view been turned off?
        ConfigServiceState state = (ConfigServiceState) request.getSession(true).getAttribute(CONFIG_STATE);
        List<String> views = new LinkedList();
        List<String> groupViews = new LinkedList();
        List<String> sourceTypes = new LinkedList<>();
        if (state != null && configBean.getCommon().isStorageView() == false && state.getView(ConfigServiceState.ConfigView.ViewStorage) == true) {
            views.add(AmazonConfigurationProvider.PREFIX_HOST_STORAGE);
            groupViews.add(AmazonConfigurationProvider.PREFIX_AMAZON_STORAGE);
            sourceTypes.add(SourceType.storage.name());
        }
        if (state != null && configBean.getCommon().isNetworkView() == false && state.getView(ConfigServiceState.ConfigView.ViewNetwork) == true) {
            views.add(AmazonConfigurationProvider.PREFIX_HOST_NETWORK);
            groupViews.add(AmazonConfigurationProvider.PREFIX_AMAZON_NETWORK);
            sourceTypes.add(SourceType.network.name());
        }
        if (state != null && configBean.getCommon().isCustomView() == false && state.getView(ConfigServiceState.ConfigView.ViewCustom) == true) {
            sourceTypes.add(SourceType.custom.name());
        }
        if (views.size() > 0 || sourceTypes.size() > 0) {
            deleteViewMetrics(VirtualSystem.AMAZON, views, groupViews, sourceTypes, configBean, getCurrentUser(request), true);
        }
        state.setView(ConfigServiceState.ConfigView.ViewStorage, configBean.getCommon().isStorageView());
        state.setView(ConfigServiceState.ConfigView.ViewNetwork, configBean.getCommon().isNetworkView());
        state.setView(ConfigServiceState.ConfigView.ViewPool, configBean.getCommon().isResourcePoolView());
        state.setView(ConfigServiceState.ConfigView.ViewCustom, configBean.getCommon().isCustomView());
        return new ModelAndView("amazon2/create-connection", "configBean", configBean);
    }

    @RequestMapping(value = "/testConnection", method = RequestMethod.POST)
    @ResponseBody
    protected String testConnection(@Valid @ModelAttribute(value = "configBean") AmazonConfiguration configuration, HttpServletRequest request, BindingResult bindingResult, HttpSession session) {
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
            log.error("Exception occurred while testing amazon connection", vex);
            message = vex.getMessage();
            result = AMAZON_ERROR;
        } catch (CloudHubException che) {
            log.error("Exception occurred while testing amazon connection", che);
            message = che.getMessage();
            result = GWOS_ERROR;
        } catch (Exception ex) {
            log.error("Exception occurred while testing amazon connection", ex);
            message = ex.getMessage();
            result = GWOS_ERROR;
        } finally {
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
    public ModelAndView navigateToProfile(@ModelAttribute(value = "configBean") AmazonConfiguration configBean, HttpServletRequest request) {

        String result = "";
        CloudHubProfile cloudHubProfile = null;
        CloudHubProfile localCloudHubProfile = null;
        CloudHubProfile remoteCloudHubProfile = null;
        CloudHubProfileWrapper profileBean = null;

        try {
            request.getSession(true).removeAttribute(PROFILE_STATE);
            localCloudHubProfile = profileService.readCloudProfile(VirtualSystem.AMAZON, configBean.getCommon().getAgentId());
            remoteCloudHubProfile = profileService.readRemoteCloudProfile(VirtualSystem.AMAZON, configBean.getGwos());
            if (localCloudHubProfile == null && remoteCloudHubProfile == null) {
                result = PROFILE_DOES_NOT_EXIST;
                request.setAttribute(RESULT, result);
                profileBean.setEnableCustom(configBean.getCommon().isCustomView());
                profileBean.setEnableStorage(configBean.getCommon().isStorageView());
                return new ModelAndView("amazon2/create-connection", "configBean", configBean);
            } else {
                if (remoteCloudHubProfile == null) {
                    result = REMOTE_PROFILE_DOES_NOT_EXIST;
                }
                request.setAttribute(RESULT, result);
                cloudHubProfile = profileService.mergeCloudProfiles(VirtualSystem.AMAZON, remoteCloudHubProfile, localCloudHubProfile);
            }
            profileBean = new CloudHubProfileWrapper(cloudHubProfile, configBean.getCommon());
            ProfileServiceState state = new ProfileServiceState();
            state.addMetrics(profileBean.getHypervisorMetrics(), profileBean.getVmMetrics(), profileBean.getCustomMetrics(), true);
            request.getSession(true).setAttribute(PROFILE_STATE, state);

        } catch (CloudHubException che) {
            log.error("Exception occurred while navigating amazon profile", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating amazon profile", ex);
        }
        profileBean.setEnableCustom(configBean.getCommon().isCustomView());
        profileBean.setEnableStorage(configBean.getCommon().isStorageView());
        return new ModelAndView("amazon2/assign-thresholds", "profileBean", profileBean);
    }

    @RequestMapping(value = "/saveConnectionProfile", method = RequestMethod.POST)
    public ModelAndView saveConnectionProfile(@Valid @ModelAttribute(value = "profileBean") CloudHubProfileWrapper profile, BindingResult bindingResult, HttpServletRequest request) {

        String result = SUCCESS;
        boolean enableCustom = false;
        boolean enableStorage = false;

        try {

            if (bindingResult.hasErrors()) {
                ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());
                if (configuration != null) {
                    enableCustom = configuration.getCommon().isCustomView();
                    enableStorage = configuration.getCommon().isStorageView();
                }
                profile.setEnableCustom(enableCustom);
                profile.setEnableStorage(enableStorage);
                request.setAttribute(RESULT, SAVE_FAILURE);
                return new ModelAndView("amazon2/assign-thresholds", "profileBean", profile);
            }

            ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());

            CloudHubProfile cloudHubProfile = profileService.readCloudProfile(VirtualSystem.AMAZON, profile.getAgent());
            if (cloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.AMAZON, profile.getAgent());
                CloudHubProfile remoteCloudHubProfile = profileService.readRemoteCloudProfile(VirtualSystem.AMAZON, configuration.getGwos());
                cloudHubProfile = profileService.mergeCloudProfiles(VirtualSystem.AMAZON, remoteCloudHubProfile, cloudHubProfile);
            }
            String profileExtraState = profile.getExtraState();
            if (profileExtraState != null && profileExtraState.trim().length() > 0) {
                mergeParsedProfileExtraState(profileExtraState, cloudHubProfile.getHypervisor().getMetrics(), cloudHubProfile.getVm().getMetrics());
                cloudHubProfile = profile.mergeToProfile(cloudHubProfile);
                profile = new CloudHubProfileWrapper(cloudHubProfile, profile.getAgent(), profile.getConfigFilePath(), profile.getConfigFileName(),
                        configuration.getCommon().isCustomView(), configuration.getCommon().isStorageView());
            } else {
                cloudHubProfile = profile.mergeToProfile(cloudHubProfile);
            }
            profileService.saveProfile(cloudHubProfile);
            collectorService.setConfigurationUpdated(VirtualSystem.AMAZON);
            if (!StringUtils.isEmpty(configuration.getCommon().getConfigurationFile())) {
                String agentIdentifier = configuration.getCommon().getConfigurationFile();
                collectorService.setConfigurationUpdated(agentIdentifier);
            }
            enableCustom = configuration.getCommon().isCustomView();
            enableStorage = configuration.getCommon().isStorageView();

            // delete any metric services delete from UI from backend
            ProfileServiceState state = (ProfileServiceState) request.getSession(true).getAttribute(PROFILE_STATE);
            if (state == null) {
                state = new ProfileServiceState();
                state.addMetrics(profile.getHypervisorMetrics(), profile.getVmMetrics(), profile.getCustomMetrics(), true);
            }
            deleteRemovedMetrics(state, profile.getHypervisorMetrics(), profile.getVmMetrics(), profile.getCustomMetrics(), configuration, getCurrentUser(request), true);
            state.addMetrics(profile.getHypervisorMetrics(), profile.getVmMetrics(), profile.getCustomMetrics(), true);
            request.getSession(true).setAttribute(PROFILE_STATE, state);
        } catch (CloudHubException che) {
            log.error("Exception occurred while saving amazon profile", che);
            result = SAVE_FAILURE;
        } catch (Exception ex) {
            log.error("Exception occurred while saving amazon profile", ex);
            result = SAVE_FAILURE;
        }

        request.setAttribute(RESULT, result);
        profile.setEnableCustom(enableCustom);
        profile.setEnableStorage(enableStorage);
        return new ModelAndView("amazon2/assign-thresholds", "profileBean", profile);
    }

    @RequestMapping(value = "/refreshCustomMetrics", method = RequestMethod.POST)
    public ModelAndView refreshCustomMetrics(HttpServletRequest request,
                                             @Valid @ModelAttribute(value = "profileBean") CloudHubProfileWrapper profile) {
        ConnectionConfiguration configBean = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());
        boolean enableCustom = configBean.getCommon().isCustomView();
        boolean enableStorage = configBean.getCommon().isStorageView();

        MonitoringConnector connector = connectorFactory.getMonitoringConnector(configBean);
        try {
            connector.connect(configBean.getConnection());
            List<Metric> customMetrics = connector.retrieveCustomMetrics();
            if (profile.getCustomMetrics() == null) {
                profile.setCustomMetrics(new ArrayList<UIMetric>());
            }
            List<UIMetric> uiCustomMetrics = mergeCustomMetrics(profile.getCustomMetrics(), customMetrics);
            profile.setCustomMetrics(uiCustomMetrics);
            request.setAttribute(RESULT, SUCCESS_REFRESH);
        } catch (ConnectorException vex) {
            log.error("Exception occurred while refreshing amazon connection", vex);
            request.setAttribute(ERROR_MESSAGE, "Failed to connect to retrieve custom metrics: " + vex.getMessage());
            request.setAttribute(RESULT, AMAZON_ERROR);
        }
        profile.setEnableCustom(enableCustom);
        profile.setEnableStorage(enableStorage);
        return new ModelAndView("amazon2/assign-thresholds", "profileBean", profile);
    }

}