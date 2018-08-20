package org.groundwork.cloudhub.profile;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.HubProfile;
import org.groundwork.rs.dto.profiles.NetHubProfile;

import java.util.List;

public interface ProfileService {

    final String NAME = "ProfileService";

    /**
     * Create a new monitor profile file, assign default values and then store it on file system..
     *
     * @param virtualSystem the required virtual system type
     * @param agent         agent identification
     * @return newly created hub profile
     * @throws CloudHubException
     */
    HubProfile createProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Create a new Cloud monitor profile file, assign default values and then store it on file system..
     *
     * @param virtualSystem the required virtual system type
     * @param agent         agent identification
     * @return newly created cloud hub profile
     * @throws CloudHubException
     */
    CloudHubProfile createCloudProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Create a new NetHub monitor profile file, assign default values and then store it on file system..
     *
     * @param virtualSystem the required virtual system type
     * @param agent         agent identification
     * @return newly created net hub profile
     * @throws CloudHubException
     */
    NetHubProfile createNetworkProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Create a new Container monitor profile file, assign default values and then store it on file system..
     *
     * @param virtualSystem the required virtual system type
     * @param agent         agent identification
     * @return newly created container hub profile
     * @throws CloudHubException
     */
    ContainerProfile createContainerProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Saves a cloud or network monitoring profile to the file system
     *
     * @param profile
     * @throws CloudHubException
     */
    void saveProfile(HubProfile profile) throws CloudHubException;

    /**
     * Retrieves a local  monitoring profile from the file system
     *
     * @param virtualSystem the type of profile to retrieve
     * @param agent         agent identification
     * @return profile
     * @throws CloudHubException
     */
    HubProfile readProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Retrieves a local cloud monitoring profile from the file system
     *
     * @param virtualSystem the type of profile to retrieve
     * @param agent         agent identification
     * @return profile
     * @throws CloudHubException
     */
    CloudHubProfile readCloudProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Retrieves a local network monitoring profile from the file system
     *
     * @param virtualSystem the type of profile to retrieve
     * @param agent         agent identification
     * @return profile
     * @throws CloudHubException
     */
    NetHubProfile readNetworkProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Retrieves a local container monitoring profile from the file system
     *
     * @param virtualSystem the type of profile to retrieve
     * @param agent         agent identification
     * @return profile
     * @throws CloudHubException
     */
    ContainerProfile readContainerProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException;

    /**
     * Takes the remote cloud profile on the GWOS server and the cloud profile on the local machine.
     * Updates the local with any new metric on the gwos server.
     *
     * @param remoteProfile
     * @param localProfile
     * @return
     */
    CloudHubProfile mergeCloudProfiles(VirtualSystem virtualSystem, CloudHubProfile remoteProfile, CloudHubProfile localProfile);

    /**
     * Takes the remote network profile on the GWOS server and the network profile on the local machine.
     * Updates the local with any new metric on the gwos server.
     *
     * @param remoteProfile
     * @param localProfile
     * @return
     */
    NetHubProfile mergeNetworkProfiles(VirtualSystem virtualSystem, NetHubProfile remoteProfile, NetHubProfile localProfile);

    /**
     * Takes the remote container profile on the GWOS server and the network profile on the local machine.
     * Updates the local with any new metric on the gwos server.
     *
     * @param remoteProfile
     * @param localProfile
     * @return
     */
    ContainerProfile mergeContainerProfiles(VirtualSystem virtualSystem, ContainerProfile remoteProfile, ContainerProfile localProfile);

    /**
     * Make restful web service request to retrieve the remote profile from the GWOS server
     *
     * @param virtualSystem the type of profile to retrieve remotely
     * @return an updated HubProfile from the GWOS server
     */
    public HubProfile readRemoteProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration);

    /**
     * Make restful web service request to retrieve the remote cloud profile from the GWOS server
     *
     * @param virtualSystem the type of profile to retrieve remotely
     * @return an updated CloudHubProfile from the GWOS server
     */
    CloudHubProfile readRemoteCloudProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration);

    /**
     * Make restful web service request to retrieve the remote network profile from the GWOS server
     *
     * @param virtualSystem the type of profile to retrieve remotely
     * @return an updated CloudHubProfile from the GWOS server
     */
    NetHubProfile readRemoteNetworkProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration);

    /**
     * Make restful web service request to retrieve the remote container profile from the GWOS server
     *
     * @param virtualSystem the type of profile to retrieve remotely
     * @return an updated ContainerProfile from the GWOS server
     */
    ContainerProfile readRemoteContainerProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration);

    /**
     * Does a monitoring profile exist for the given file system
     *
     * @param virtualSystem
     * @param agent
     * @return true if the profile does exist, otherwise false
     */
    boolean doesProfileExist(VirtualSystem virtualSystem, String agent);

    /**
     * Read metrics from any profile source (cloud or network or ...)
     *
     * @param virtualSystem
     * @param agent
     * @return generic profile metrics
     */
    ProfileMetrics readMetrics(VirtualSystem virtualSystem, String agent);

    /**
     * look for any profile in 7.0.x format and move them to new location
     *
     * @param virtualSystem  the type of profiles to migrate
     * @param configurations list of corresponding configurations for this virtual system
     * @return count of profiles created per configuration
     * @throws CloudHubException
     */
    int migrateProfiles(VirtualSystem virtualSystem, List<? extends ConnectionConfiguration> configurations) throws CloudHubException;

    /**
     * Remove a profile from the profile directory
     *
     * @param virtualSystem
     * @param alias
     * @return true if deleted, false if not found
     * @throws CloudHubException
     */
    boolean removeProfile(VirtualSystem virtualSystem, String alias) throws CloudHubException;

    /**
     * Read a local profile template
     *
     * @param virtualSystem
     * @return
     */
    HubProfile readProfileTemplate(VirtualSystem virtualSystem);

    /**
     * Check for any new metrics (not updates)
     *
     * @param remote
     * @param local
     * @return count of new metrics
     */
    int checkForNewMetrics(CloudHubProfile remote, CloudHubProfile local);
}