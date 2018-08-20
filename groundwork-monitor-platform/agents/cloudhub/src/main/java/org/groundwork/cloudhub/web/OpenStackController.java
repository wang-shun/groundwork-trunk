package org.groundwork.cloudhub.web;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.OpenStackConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.openstack.OpenStackConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.profile.OpenStackProfileWrapper;
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

import static org.groundwork.cloudhub.web.CloudHubUI.ERROR_MESSAGE;
import static org.groundwork.cloudhub.web.CloudHubUI.GWOS_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.OPENSTACK_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.REMOTE_PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.READFAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.RESULT;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_FAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_SUCCESS;
import static org.groundwork.cloudhub.web.CloudHubUI.SUCCESS;

@Controller
@RequestMapping("/openstack")
public class OpenStackController extends HostController {

    protected static final String PROFILE_STATE = "profile-state-openstack";
    private static Logger log = Logger.getLogger(OpenStackController.class);

    @RequestMapping(value = "/navigateCreateConnection", method = RequestMethod.GET)
    public ModelAndView navigateCreateConnection(HttpServletRequest request, HttpSession session) {

        log.info("configuration service = " + configurationService);

        OpenStackConfiguration configuration = null;

        try {
            configuration = (OpenStackConfiguration) configurationService.createConfiguration(VirtualSystem.OPENSTACK);
            configuration.getCommon().setUiCheckIntervalMinutes(String.valueOf(configuration.getCommon().getCheckIntervalMinutes()));
            request.getSession(true).setAttribute(HOSTNAME_STATE, HOSTNAME_NEW);
            setGwVersion(configuration, session);
            setConfigDefaultsByVersion(configuration, request);
        } catch (CloudHubException che) {
            log.error("Exception occurred navigating create openstack connection", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating create openstack connection", ex);
        }

        return new ModelAndView("openstack/create-connection", "configBean", configuration);
    }

    @RequestMapping(value = "saveConnectionConfiguration", method = RequestMethod.POST)
    public ModelAndView saveConnectionConfiguration(@Valid @ModelAttribute(value = "configBean") OpenStackConfiguration configBean, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("openstack/create-connection", "configBean", configBean);
        }
        String result = saveConfiguration(configBean, request, session);
        request.setAttribute("result", result);
        return new ModelAndView("openstack/create-connection", "configBean", configBean);
    }

    @RequestMapping(value = "/testConnection", method = RequestMethod.POST)
    @ResponseBody
    protected String testConnection(@Valid @ModelAttribute(value = "configBean") OpenStackConfiguration configuration, HttpServletRequest request, BindingResult bindingResult, HttpSession session) {
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
                if (connector.queryConnectorInfo(OpenStackConnector.TAG_CEILOMETER_ENABLED).equals("false")) {
                    message = "(Ceilometer is Disabled)";
                }
            }
        } catch (ConnectorException vex) {
            log.error("Exception occurred while testing openstack connection", vex);
            message = vex.getMessage();
            result = OPENSTACK_ERROR;
        } catch (CloudHubException che) {
            log.error("Exception occurred while testing openstack connection", che);
            message = che.getMessage();
            result = GWOS_ERROR;
        } catch (Exception ex) {
            log.error("Exception occurred while testing openstack connection", ex);
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
    public ModelAndView navigateToProfile(@ModelAttribute(value = "configBean") OpenStackConfiguration configBean, HttpServletRequest request) {

        String result = "";
        CloudHubProfile cloudHubProfile = null;
        CloudHubProfile localCloudHubProfile = null;
        CloudHubProfile remoteCloudHubProfile = null;
        OpenStackProfileWrapper profileBean = null;

        try {
            request.getSession(true).removeAttribute(PROFILE_STATE);
            localCloudHubProfile = profileService.readCloudProfile(VirtualSystem.OPENSTACK, configBean.getCommon().getAgentId());
            remoteCloudHubProfile = profileService.readRemoteCloudProfile(VirtualSystem.OPENSTACK, configBean.getGwos());
            if (localCloudHubProfile == null && remoteCloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.OPENSTACK, configBean.getCommon().getAgentId());
                result = PROFILE_DOES_NOT_EXIST;
                request.setAttribute(RESULT, result);
                return new ModelAndView("openstack/create-connection", "configBean", configBean);
            } else {
                if (remoteCloudHubProfile == null) {
                    result = REMOTE_PROFILE_DOES_NOT_EXIST;
                }
                request.setAttribute(RESULT, result);
                cloudHubProfile = profileService.mergeCloudProfiles(VirtualSystem.OPENSTACK, remoteCloudHubProfile, localCloudHubProfile);
            }
            profileBean = new OpenStackProfileWrapper(cloudHubProfile, configBean.getCommon());
            ProfileServiceState state = new ProfileServiceState();
            state.addMetrics(profileBean.getHypervisorMetrics(), profileBean.getVmMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);

        } catch (Exception e) {
            log.error("Exception occurred while navigating openstack profile", e);
            if (cloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.OPENSTACK, configBean.getCommon().getAgentId());
            }
            profileBean = new OpenStackProfileWrapper(cloudHubProfile, configBean.getCommon());
            request.setAttribute(RESULT, READFAILURE);
            request.setAttribute(ERROR_MESSAGE, e.getMessage());
        }
        return new ModelAndView("openstack/assign-thresholds", "profileBean", profileBean);
    }

    @RequestMapping(value = "/saveConnectionProfile", method = RequestMethod.POST)
    public ModelAndView saveConnectionProfile(@Valid @ModelAttribute(value = "profileBean") OpenStackProfileWrapper profile, BindingResult bindingResult, HttpServletRequest request) {

        String result = SUCCESS;
        try {

            if (bindingResult.hasErrors()) {
                return new ModelAndView("openstack/assign-thresholds", "profileBean", profile);
            }

            CloudHubProfile cloudHubProfile = profileService.readCloudProfile(VirtualSystem.OPENSTACK, profile.getAgent());
            if (cloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.OPENSTACK, profile.getAgent());
            }
            cloudHubProfile = profile.mergeToProfile(cloudHubProfile);
            profileService.saveProfile(cloudHubProfile);
            collectorService.setConfigurationUpdated(VirtualSystem.OPENSTACK);

            ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());
            if (!StringUtils.isEmpty(configuration.getCommon().getConfigurationFile())) {
                String agentIdentifier = configuration.getCommon().getConfigurationFile();
                collectorService.setConfigurationUpdated(agentIdentifier);
            }

            // delete any metric services delete from UI from backend
            ProfileServiceState state = (ProfileServiceState)request.getSession(true).getAttribute(PROFILE_STATE);
            deleteRemovedMetrics(state, profile.getHypervisorMetrics(), profile.getVmMetrics(), configuration, getCurrentUser(request));
            state.addMetrics(profile.getHypervisorMetrics(), profile.getVmMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);
        } catch (CloudHubException che) {
            log.error("Exception occurred while saving openstack profile", che);
            result = SAVE_FAILURE;
        } catch (Exception ex) {
            log.error("Exception occurred while saving openstack profile", ex);
            result = SAVE_FAILURE;
        }

        request.setAttribute(RESULT, result);

        return new ModelAndView("openstack/assign-thresholds", "profileBean", profile);
    }
}