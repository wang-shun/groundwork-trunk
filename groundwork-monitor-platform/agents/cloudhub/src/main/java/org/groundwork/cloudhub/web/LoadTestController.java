/*
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.cloudhub.web;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.LoadTestConfiguration;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.profile.CloudHubProfileWrapper;
import org.groundwork.cloudhub.profile.ProfileServiceState;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.HubProfile;
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

import static org.groundwork.cloudhub.web.CloudHubUI.*;

/**
 * LoadTestController
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Controller
@RequestMapping("/loadtest")
public class LoadTestController extends HostController {

    protected static final String PROFILE_STATE = "profile-state-loadtest";
    private static Logger log = Logger.getLogger(LoadTestController.class);

    @RequestMapping(value = "/navigateCreateConnection", method = RequestMethod.GET)
    public ModelAndView navigateCreateConnection(HttpServletRequest request, HttpSession session) {

        log.info("configuration service = " + configurationService);

        LoadTestConfiguration configuration = null;

        try {
            configuration = (LoadTestConfiguration) configurationService.createConfiguration(VirtualSystem.LOADTEST);
            configuration.getCommon().setUiCheckIntervalMinutes(String.valueOf(configuration.getCommon().getCheckIntervalMinutes()));
            request.getSession(true).setAttribute(HOSTNAME_STATE, HOSTNAME_NEW);
            setGwVersion(configuration, session);
            setConfigDefaultsByVersion(configuration, request);
        } catch (CloudHubException che) {
            log.error("Exception occurred navigating create loadtest connection", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating create loadtest connection", ex);
        }

        return new ModelAndView("loadtest/create-connection", "configBean", configuration);
    }

    @RequestMapping(value = "saveConnectionConfiguration", method = RequestMethod.POST)
    public ModelAndView saveConnectionConfiguration(@Valid @ModelAttribute(value = "configBean") LoadTestConfiguration configBean, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("loadtest/create-connection", "configBean", configBean);
        }
        String result = saveConfiguration(configBean, request, session);
        request.setAttribute("result", result);
        return new ModelAndView("loadtest/create-connection", "configBean", configBean);
    }

    @Override
    protected void createNewProfile(ConnectionConfiguration configuration) {
        try {
            HubProfile profile = profileService.readProfile(VirtualSystem.LOADTEST, configuration.getCommon().getAgentId());
            if (profile == null) {
                ManagementConnector connector = connectorFactory.getManagementConnector(configuration);
                connector.openConnection(configuration.getConnection());
                HubProfile connectorProfile = connector.readProfile();
                if (connectorProfile == null) {
                    HubProfile localProfile = profileService.createProfile(VirtualSystem.LOADTEST, configuration.getCommon().getAgentId());
                    profileService.saveProfile(localProfile);
                } else {
                    connectorProfile.setAgent(configuration.getCommon().getAgentId());
                    profileService.saveProfile(connectorProfile);
                }
            }
        } catch (CloudHubException che) {
            log.error("Exception occurred while creating loadtest profile", che);
        } catch (Exception ex) {
            log.error("Exception occurred while creating loadtest profile", ex);
        }
    }

    @RequestMapping(value = "/testConnection", method = RequestMethod.POST)
    @ResponseBody
    protected String testConnection(@Valid @ModelAttribute(value = "configBean") LoadTestConfiguration configuration, BindingResult bindingResult, HttpServletRequest request, HttpSession session) {
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
                connector.connect(configuration.getConnection());
            }
        } catch (ConnectorException vex) {
            log.error("Exception occurred while testing loadtest connection", vex);
            message = vex.getMessage();
            result = LOADTEST_ERROR;
        } catch (CloudHubException che) {
            log.error("Exception occurred while testing loadtest connection", che);
            message = che.getMessage();
            result = GWOS_ERROR;
        } catch (Exception ex) {
            log.error("Exception occurred while testing loadtest connection", ex);
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
    public ModelAndView navigateToProfile(@ModelAttribute(value = "configBean") LoadTestConfiguration configBean, HttpServletRequest request) {

        String result = "";
        CloudHubProfile cloudHubProfile = null;
        CloudHubProfile localCloudHubProfile = null;
        CloudHubProfile connectorCloudHubProfile = null;
        CloudHubProfileWrapper profileBean = null;

        try {
            request.getSession(true).removeAttribute(PROFILE_STATE);
            localCloudHubProfile = profileService.readCloudProfile(VirtualSystem.LOADTEST, configBean.getCommon().getAgentId());
            ManagementConnector connector = connectorFactory.getManagementConnector(configBean);
            connector.openConnection(configBean.getConnection());
            connectorCloudHubProfile = connector.readCloudProfile();
            if (localCloudHubProfile == null && connectorCloudHubProfile == null) {
                result = PROFILE_DOES_NOT_EXIST;
                request.setAttribute(RESULT, result);
                return new ModelAndView("loadtest/create-connection", "configBean", configBean);
            } else {
                if (connectorCloudHubProfile == null) {
                    result = CONNECTOR_PROFILE_DOES_NOT_EXIST;
                }
                request.setAttribute(RESULT, result);
                cloudHubProfile = profileService.mergeCloudProfiles(VirtualSystem.LOADTEST, connectorCloudHubProfile, localCloudHubProfile);
            }
            profileBean = new CloudHubProfileWrapper(cloudHubProfile, configBean.getCommon());
            ProfileServiceState state = new ProfileServiceState();
            state.addMetrics(profileBean.getHypervisorMetrics(), profileBean.getVmMetrics());
            request.getSession(true).setAttribute(PROFILE_STATE, state);

        } catch (CloudHubException che) {
            log.error("Exception occurred while navigating loadtest profile", che);
        } catch (Exception ex) {
            log.error("Exception occurred while navigating loadtest profile", ex);
        }

        return new ModelAndView("loadtest/assign-thresholds", "profileBean", profileBean);
    }

    @RequestMapping(value = "/saveConnectionProfile", method = RequestMethod.POST)
    public ModelAndView saveConnectionProfile(@Valid @ModelAttribute(value = "profileBean") CloudHubProfileWrapper profile, BindingResult bindingResult, HttpServletRequest request) {

        String result = SUCCESS;
        try {

            if (bindingResult.hasErrors()) {
                request.setAttribute(RESULT, SAVE_FAILURE);
                return new ModelAndView("loadtest/assign-thresholds", "profileBean", profile);
            }

            ConnectionConfiguration configuration = configurationService.readConfiguration(profile.getConfigFilePath() + "/" + profile.getConfigFileName());

            CloudHubProfile cloudHubProfile = profileService.readCloudProfile(VirtualSystem.LOADTEST, profile.getAgent());
            if (cloudHubProfile == null) {
                cloudHubProfile = profileService.createCloudProfile(VirtualSystem.LOADTEST, profile.getAgent());
                ManagementConnector connector = connectorFactory.getManagementConnector(configuration);
                connector.openConnection(configuration.getConnection());
                CloudHubProfile connectorCloudHubProfile = connector.readCloudProfile();
                cloudHubProfile = profileService.mergeCloudProfiles(VirtualSystem.LOADTEST, connectorCloudHubProfile, cloudHubProfile);
            }
            String profileExtraState = profile.getExtraState();
            if (profileExtraState != null && profileExtraState.trim().length() > 0) {
                mergeParsedProfileExtraState(profileExtraState, cloudHubProfile.getHypervisor().getMetrics(), cloudHubProfile.getVm().getMetrics());
                cloudHubProfile = profile.mergeToProfile(cloudHubProfile);
                profile = new CloudHubProfileWrapper(cloudHubProfile, profile.getAgent(), profile.getConfigFilePath(), profile.getConfigFileName(),
                        configuration.getCommon().isCustomView(), configuration.getCommon().isStorageView());
            }
            else {
                cloudHubProfile = profile.mergeToProfile(cloudHubProfile);
            }
            profileService.saveProfile(cloudHubProfile);
            collectorService.setConfigurationUpdated(VirtualSystem.LOADTEST);
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
            log.error("Exception occurred while saving loadtest profile", che);
            result = SAVE_FAILURE;
        } catch (Exception ex) {
            log.error("Exception occurred while saving loadtest profile", ex);
            result = SAVE_FAILURE;
        }

        request.setAttribute(RESULT, result);

        return new ModelAndView("loadtest/assign-thresholds", "profileBean", profile);
    }
}
