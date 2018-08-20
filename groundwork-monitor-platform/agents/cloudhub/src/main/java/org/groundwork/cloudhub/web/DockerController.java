package org.groundwork.cloudhub.web;

import com.groundwork.collage.model.AuditLog;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.monitor.CloudhubMonitorAgent;
import org.groundwork.cloudhub.profile.ContainerProfileWrapper;
import org.groundwork.cloudhub.profile.ProfileServiceState;
import org.groundwork.rs.dto.profiles.ContainerProfile;
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
import java.util.List;

import static org.groundwork.cloudhub.web.CloudHubUI.DOCKER_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.ERROR_MESSAGE;
import static org.groundwork.cloudhub.web.CloudHubUI.GWOS_ERROR;
import static org.groundwork.cloudhub.web.CloudHubUI.PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.READFAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.REMOTE_PROFILE_DOES_NOT_EXIST;
import static org.groundwork.cloudhub.web.CloudHubUI.RESULT;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_FAILURE;
import static org.groundwork.cloudhub.web.CloudHubUI.SAVE_SUCCESS;
import static org.groundwork.cloudhub.web.CloudHubUI.SUCCESS;

@Controller
@RequestMapping("/docker2")
public class DockerController extends HostController {

    protected static final String PROFILE_STATE = "profile-state-docker";
    protected static final String DOCKER_PREFIX_STATE = "prefix-state-docker";
    protected static final String DOCKER_PREFIX_NEW = "<<<-new->>>";

    private static Logger log = Logger.getLogger(DockerController.class);

    @RequestMapping(value = "/navigateCreateConnection", method = RequestMethod.GET)
    public ModelAndView navigateCreateConnection(HttpServletRequest request, HttpSession session) {

        log.info("configuration service = " + configurationService);

        DockerConfiguration configuration = null;

        try {
            configuration = (DockerConfiguration) configurationService.createConfiguration(VirtualSystem.DOCKER);
            configuration.getCommon().setUiCheckIntervalMinutes(String.valueOf(configuration.getCommon().getCheckIntervalMinutes()));
            request.getSession(true).setAttribute(DockerController.DOCKER_PREFIX_STATE, DOCKER_PREFIX_NEW);
            request.getSession(true).setAttribute(HOSTNAME_STATE, HOSTNAME_NEW);
            setGwVersion(configuration, session);
            setConfigDefaultsByVersion(configuration, request);
        } catch (CloudHubException che) {
            log.error("Exception occurred navigating create docker connection", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating create docker connection", ex);
        }

        return new ModelAndView("docker2/create-connection", "configBean", configuration);
    }

    @RequestMapping(value = "saveConnectionConfiguration", method = RequestMethod.POST)
    public ModelAndView saveConnectionConfiguration(@Valid @ModelAttribute(value = "configBean") DockerConfiguration configBean, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("docker2/create-connection", "configBean", configBean);
        }
        String result = SAVE_SUCCESS;
        if (!isPrefixUnique(configBean)) {
            request.setAttribute(ERROR_MESSAGE, "Prefix " + configBean.getConnection().getPrefix() + " is not unique. Please chose another prefix.");
            result = SAVE_FAILURE;
        }
        else {
            result = saveConfiguration(configBean, request, session);
            if (result.equals(SAVE_SUCCESS)) {
                String deleteMessage = submitRenameWhenPrefixChanges(request, configBean);
                if (deleteMessage != null) {
                    request.setAttribute(ERROR_MESSAGE, deleteMessage);
                    result = SAVE_FAILURE;
                }
            }
        }
        request.setAttribute("result", result);
        return new ModelAndView("docker2/create-connection", "configBean", configBean);
    }

    boolean isPrefixUnique(DockerConfiguration configuration) {
        String newPrefix = configuration.getConnection().getPrefix();
        if (newPrefix == null) {
            return false; // invalid
        }
        List<DockerConfiguration> configurations = (List<DockerConfiguration>) configurationService.listConfigurations(VirtualSystem.DOCKER);
        for (DockerConfiguration dockerConfiguration : configurations) {
            if (!configuration.getCommon().getAgentId().equals(dockerConfiguration.getCommon().getAgentId())) {
                String prefix = dockerConfiguration.getConnection().getPrefix();
                if (prefix != null && prefix.equals(newPrefix)) {
                    return false;
                }
            }
        }
        return true;
    }

    @RequestMapping(value = "/testConnection", method = RequestMethod.POST)
    @ResponseBody
    protected String testConnection(@ModelAttribute(value = "configBean") DockerConfiguration configuration, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
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
            log.error("Exception occurred while testing docker connection", vex);
            message = vex.getMessage();
            result = DOCKER_ERROR;
        } catch (CloudHubException che) {
            log.error("Exception occurred while testing docker connection", che);
            message = che.getMessage();
            result = GWOS_ERROR;
        } catch (Exception ex) {
            log.error("Exception occurred while testing docker connection", ex);
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
    public ModelAndView navigateToProfile(@ModelAttribute(value = "configBean") DockerConfiguration configBean, HttpServletRequest request) {

        String result = "";
        ContainerProfile containerProfile = null;
        ContainerProfile localContainerProfile = null;
        ContainerProfile remoteContainerProfile = null;
        ContainerProfileWrapper profileBean = null;

        try {
            request.getSession(true).removeAttribute(PROFILE_STATE);
            localContainerProfile = profileService.readContainerProfile(VirtualSystem.DOCKER, configBean.getCommon().getAgentId());
            remoteContainerProfile = profileService.readRemoteContainerProfile(VirtualSystem.DOCKER, configBean.getGwos());
            if (localContainerProfile == null && remoteContainerProfile == null) {
                containerProfile = profileService.createContainerProfile(VirtualSystem.DOCKER, configBean.getCommon().getAgentId());
                result = PROFILE_DOES_NOT_EXIST;
                request.setAttribute(RESULT, result);
                return new ModelAndView("docker2/create-connection", "configBean", configBean);
            } else {
                if (remoteContainerProfile == null) {
                    result = REMOTE_PROFILE_DOES_NOT_EXIST;
                }
                request.setAttribute(RESULT, result);
                containerProfile = profileService.mergeContainerProfiles(VirtualSystem.DOCKER, remoteContainerProfile, localContainerProfile);
            }
            profileBean = new ContainerProfileWrapper(containerProfile, configBean.getCommon());
            ProfileServiceState state = new ProfileServiceState();
            state.addMetrics(profileBean.getEngineMetrics(), profileBean.getContainerMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);
        } catch (Exception e) {
            log.error("Exception occurred while navigating docker profile", e);
            if (containerProfile == null) {
                containerProfile = profileService.createContainerProfile(VirtualSystem.DOCKER, configBean.getCommon().getAgentId());
            }
            profileBean = new ContainerProfileWrapper(containerProfile, configBean.getCommon());
            request.setAttribute(RESULT, READFAILURE);
            request.setAttribute(ERROR_MESSAGE, e.getMessage());
        }
        return new ModelAndView("docker2/assign-thresholds", "profileBean", profileBean);
    }

    @RequestMapping(value = "/saveConnectionProfile", method = RequestMethod.POST)
    public ModelAndView saveConnectionProfile(@Valid @ModelAttribute(value = "profileBean") ContainerProfileWrapper profile, BindingResult bindingResult, HttpServletRequest request) {

        String result = SUCCESS;
        try {

            if (bindingResult.hasErrors()) {
                return new ModelAndView("docker2/assign-thresholds", "profileBean", profile);
            }

            ContainerProfile containerProfile = profileService.readContainerProfile(VirtualSystem.DOCKER, profile.getAgent());
            if (containerProfile == null) {
                containerProfile = profileService.createContainerProfile(VirtualSystem.DOCKER, profile.getAgent());
            }
            containerProfile = profile.mergeToProfile(containerProfile);
            profileService.saveProfile(containerProfile);
            collectorService.setConfigurationUpdated(VirtualSystem.DOCKER);

            ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());
            if (!StringUtils.isEmpty(configuration.getCommon().getConfigurationFile())) {
                String agentIdentifier = configuration.getCommon().getConfigurationFile();
                collectorService.setConfigurationUpdated(agentIdentifier);
            }

            // delete any metric services delete from UI from backend
            ProfileServiceState state = (ProfileServiceState)request.getSession(true).getAttribute(PROFILE_STATE);
            deleteRemovedMetrics(state, profile.getEngineMetrics(), profile.getContainerMetrics(), configuration, getCurrentUser(request));
            state.addMetrics(profile.getEngineMetrics(), profile.getContainerMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);
        } catch (CloudHubException che) {
            log.error("Exception occurred while saving docker profile", che);
            result = SAVE_FAILURE;
        } catch (Exception ex) {
            log.error("Exception occurred while saving docker profile", ex);
            result = SAVE_FAILURE;
        }

        request.setAttribute(RESULT, result);

        return new ModelAndView("docker2/assign-thresholds", "profileBean", profile);
    }

    protected String submitRenameWhenPrefixChanges(HttpServletRequest request, DockerConfiguration configuration) {
        String message = null;
        try {
            String previousPrefix = (String) request.getSession(true).getAttribute(DockerController.DOCKER_PREFIX_STATE);
            if (previousPrefix == null)
                previousPrefix = "";
            String newPrefix = configuration.getConnection().getPrefix();
            if (newPrefix == null)
                newPrefix = "";
            if (!previousPrefix.equals(DOCKER_PREFIX_NEW) && !previousPrefix.equals(newPrefix)) {
                String agentIdentifier = configuration.getCommon().getConfigurationFile();
                CloudhubMonitorAgent agent = collectorService.lookup(agentIdentifier);
                if (agent != null) {
                    agent.submitRequestToRenameHosts(configuration.getCommon().getAgentId(), previousPrefix, newPrefix);
                }
                else {
                    // agent was never started, start it so we can submit a deletion
                    configuration.getCommon().setServerSuspended(true);
                    collectorService.startMonitoringConnection(configuration);
                    agent = collectorService.lookup(agentIdentifier);
                    if (agent != null) {
                        agent.submitRequestToRenameHosts(configuration.getCommon().getAgentId(), previousPrefix, newPrefix);
                        getGwosService(configuration).auditLogHost(configuration.getCommon().getVirtualSystem(),
                                configuration.getConnection().getHostName(),
                                AuditLog.Action.MODIFY.name(),
                                "Renaming all Docker prefixes from " + previousPrefix + " to " + newPrefix,
                                getCurrentUser(request));
                        Thread.sleep(1000);
                    }
                }
                request.getSession(true).setAttribute(DockerController.DOCKER_PREFIX_STATE, newPrefix);
            }
        }
        catch (Exception e) {
            log.error("Failed to delete old prefix records", e);
            message = e.getMessage();
        }
        return message;
    }
}