package org.groundwork.cloudhub.web;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.NSXConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.profile.NetHubProfileWrapper;
import org.groundwork.cloudhub.profile.ProfileServiceState;
import org.groundwork.rs.dto.profiles.NetHubProfile;
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

import static org.groundwork.cloudhub.web.CloudHubUI.ERROR_MESSAGE;
import static org.groundwork.cloudhub.web.CloudHubUI.GWOS_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.NSX_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.READFAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.REMOTE_PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.RESULT;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_FAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_SUCCESS;
import static org.groundwork.cloudhub.web.CloudHubUI.SUCCESS;

@Controller
@RequestMapping("/nsx")
public class NSXController extends HostController {

    protected static final String PROFILE_STATE = "profile-state-nsx";
    private static Logger log = Logger.getLogger(NSXController.class);

    @RequestMapping(value = "/navigateCreateConnection", method = RequestMethod.GET)
    public ModelAndView navigateCreateConnection(HttpServletRequest request, HttpSession session) {

        log.info("configuration service = " + configurationService);

        NSXConfiguration configuration = null;

        try {
            configuration = (NSXConfiguration) configurationService.createConfiguration(VirtualSystem.NSX);
            configuration.getCommon().setUiCheckIntervalMinutes(String.valueOf(configuration.getCommon().getCheckIntervalMinutes()));
            request.getSession(true).setAttribute(HOSTNAME_STATE, HOSTNAME_NEW);
            setGwVersion(configuration, session);
            setConfigDefaultsByVersion(configuration, request);
        } catch (CloudHubException che) {
            log.error("Exception occurred navigating create NSX connection", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating create NSX connection", ex);
        }
        return new ModelAndView("nsx/create-connection", "configBean", configuration);
    }

    @RequestMapping(value = "saveConnectionConfiguration", method = RequestMethod.POST)
    public ModelAndView saveConnectionConfiguration(@Valid @ModelAttribute(value = "configBean") NSXConfiguration configBean, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("nsx/create-connection", "configBean", configBean);
        }
        String result = saveConfiguration(configBean, request, session);
        request.setAttribute("result", result);
        return new ModelAndView("nsx/create-connection", "configBean", configBean);
    }

    @RequestMapping(value = "/testConnection", method = RequestMethod.POST)
    @ResponseBody
    protected String testConnection(@Valid @ModelAttribute(value = "configBean") NSXConfiguration configuration, HttpServletRequest request,  BindingResult bindingResult, HttpSession session) {
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
            log.error("Exception occurred while testing NSX connection", vex);
            message = vex.getMessage();
            result = NSX_ERROR;
        } catch (CloudHubException che) {
            log.error("Exception occurred while testing NSX connection", che);
            message = che.getMessage();
            result = GWOS_ERROR;
        } catch (Exception ex) {
            log.error("Exception occurred while testing NSX connection", ex);
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
    public ModelAndView navigateToProfile(@ModelAttribute(value = "configBean") NSXConfiguration configBean, HttpServletRequest request) {

        String result = "";
        NetHubProfile netHubProfile = null;
        NetHubProfile localNetHubProfile = null;
        NetHubProfile remoteNetHubProfile = null;
        NetHubProfileWrapper profileBean = null;

        try {
            request.getSession(true).removeAttribute(PROFILE_STATE);
            localNetHubProfile = profileService.readNetworkProfile(VirtualSystem.NSX, configBean.getCommon().getAgentId());
            remoteNetHubProfile = profileService.readRemoteNetworkProfile(VirtualSystem.NSX, configBean.getGwos());
            if (localNetHubProfile == null && remoteNetHubProfile == null) {
                netHubProfile = profileService.createNetworkProfile(VirtualSystem.NSX, configBean.getCommon().getAgentId());
                result = PROFILE_DOES_NOT_EXIST;
                request.setAttribute(RESULT, result);
                return new ModelAndView("nsx/create-connection", "configBean", configBean);
            } else {
                if (remoteNetHubProfile == null) {
                    result = REMOTE_PROFILE_DOES_NOT_EXIST;
                }
                request.setAttribute(RESULT, result);
                netHubProfile = profileService.mergeNetworkProfiles(VirtualSystem.NSX, remoteNetHubProfile, localNetHubProfile);
            }
            profileBean = new NetHubProfileWrapper(netHubProfile, configBean.getCommon());
            ProfileServiceState state = new ProfileServiceState();
            state.addMetrics(profileBean.getControllerMetrics(), profileBean.getSwitchMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);

        } catch (Exception e) {
            log.error("Exception occurred while navigating NSX profile", e);
            if (netHubProfile == null) {
                netHubProfile = profileService.createNetworkProfile(VirtualSystem.NSX, configBean.getCommon().getAgentId());
            }
            profileBean = new NetHubProfileWrapper(netHubProfile, configBean.getCommon());
            request.setAttribute(RESULT, READFAILURE);
            request.setAttribute(ERROR_MESSAGE, e.getMessage());
        }

        return new ModelAndView("nsx/assign-thresholds", "profileBean", profileBean);
    }

    @RequestMapping(value = "/saveConnectionProfile", method = RequestMethod.POST)
    public ModelAndView saveConnectionProfile(@Valid @ModelAttribute(value = "profileBean") NetHubProfileWrapper profile, BindingResult bindingResult, HttpServletRequest request) {

        String result = SUCCESS;
        try {

            if (bindingResult.hasErrors()) {
                return new ModelAndView("nsx/assign-thresholds", "profileBean", profile);
            }

            NetHubProfile netHubProfile = profileService.readNetworkProfile(VirtualSystem.NSX, profile.getAgent());
            if (netHubProfile == null) {
                netHubProfile = profileService.createNetworkProfile(VirtualSystem.NSX, profile.getAgent());
            }
            netHubProfile = profile.mergeToProfile(netHubProfile);
            profileService.saveProfile(netHubProfile);

            collectorService.setConfigurationUpdated(VirtualSystem.NSX);

            ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());
            if (!StringUtils.isEmpty(configuration.getCommon().getConfigurationFile())) {
                String agentIdentifier = configuration.getCommon().getConfigurationFile();
                collectorService.setConfigurationUpdated(agentIdentifier);
            }

            // delete any metric services delete from UI from backend
            ProfileServiceState state = (ProfileServiceState)request.getSession(true).getAttribute(PROFILE_STATE);
            deleteRemovedMetrics(state, profile.getControllerMetrics(), profile.getSwitchMetrics(), configuration, getCurrentUser(request));
            state.addMetrics(profile.getControllerMetrics(), profile.getSwitchMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);
        } catch (CloudHubException che) {
            log.error("Exception occurred while saving NSX profile", che);
            result = SAVE_FAILURE;
        } catch (Exception ex) {
            log.error("Exception occurred while saving NSX profile", ex);
            result = SAVE_FAILURE;
        }
        request.setAttribute(RESULT, result);

        return new ModelAndView("nsx/assign-thresholds", "profileBean", profile);
    }
}