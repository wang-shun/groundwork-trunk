package org.groundwork.cloudhub.profile;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.gwos.BaseRestGwosService;
import org.groundwork.rs.client.CollageRestException;
import org.groundwork.rs.client.hubs.ProfileClient;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.Excludes;
import org.groundwork.rs.dto.profiles.HubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.NetHubProfile;
import org.groundwork.rs.dto.profiles.ProfileType;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(ProfileService.NAME)
public class ProfileServiceImpl extends BaseRestGwosService implements ProfileService {

    private static Logger log = Logger.getLogger(ProfileServiceImpl.class);

    public static final String CONFIG_FILE_EXTN = ".xml";
    public static final String TEMPLATE_FILE_PATH = "/usr/local/groundwork/config/cloudhub/profile-templates/";
    public static final String OLD_CONFIG_FILE_PATH = "/usr/local/groundwork/config/";
    public static final String NEW_CONFIG_FILE_PATH = "/usr/local/groundwork/config/cloudhub/profiles/";
    public static final String OLD_CONFIG_FILE_BASE_NAME = "%s-monitoring-profile";
    public static final String TEMPLATE_FILE_BASE_NAME = "%s_monitoring_profile";
    public static final String NEW_CONFIG_FILE_BASE_NAME = "%s-%s";


    public ProfileServiceImpl() {
        createProfileDirectory();
    }

    @Override
    public HubProfile createProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        switch (ProfileConversion.convertVirtualSystemToHubType(virtualSystem)) {
            case cloud:
                return createCloudProfile(virtualSystem, agent);
            case network:
                return createNetworkProfile(virtualSystem, agent);
            case container:
                return createContainerProfile(virtualSystem, agent);
        }
        throw new CloudHubException("Unknown Virtual System");
    }

    @Override
    public CloudHubProfile createCloudProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        return new CloudHubProfile(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem), agent);
    }

    @Override
    public NetHubProfile createNetworkProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        return new NetHubProfile(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem), agent);
    }

    @Override
    public ContainerProfile createContainerProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        return new ContainerProfile(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem), agent);
    }

    @Override
    public synchronized void saveProfile(HubProfile profile) throws CloudHubException {
        String path = "";
        try {
            JAXBContext context = JAXBContext.newInstance(profile.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            path = filePathFromProfile(profile);
            File file = new File(path);
            marshaller.marshal(profile, file);

        } catch (JAXBException e) {
            throw new CloudHubException("Failed to save cloud profile: " + path, e);
        }
    }

    @Override
    public synchronized CloudHubProfile readCloudProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        return (CloudHubProfile) readProfileInternal(CloudHubProfile.class, virtualSystem, agent);
    }

    @Override
    public synchronized NetHubProfile readNetworkProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        return (NetHubProfile) readProfileInternal(NetHubProfile.class, virtualSystem, agent);
    }

    @Override
    public synchronized ContainerProfile readContainerProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        return (ContainerProfile) readProfileInternal(ContainerProfile.class, virtualSystem, agent);
    }

    @Override
    public synchronized HubProfile readProfile(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        Class profileClass = ProfileConversion.convertVirtualSystemToProfileClass(virtualSystem);
        return readProfileInternal(profileClass, virtualSystem, agent);
    }

    protected HubProfile readProfileInternal(Class clazz, VirtualSystem virtualSystem, String agent) throws CloudHubException {
        HubProfile profile = null;
        ProfileType profileType = ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem);

        String path = newFilePathFromVirtualSystem(profileType, agent);
        File file = null;
        try {
            file = new File(path);
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                profile = (HubProfile) unmarshaller.unmarshal(file);
                profile.setProfileType(profileType);
            }
        } catch (JAXBException e) {
            String message = e.getMessage();
            if (message.contains("unexpected element") && message.contains("container-monitoring")) {
                // Since 7.2.1 migrate docker format
                if (file == null) {
                    throw new CloudHubException("Failed to read profile: " + path, e);
                }
                return migrateDockerToCloudHub(file, agent);
            }
            String msg = "Failed to read profile: " + path;
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }
        return profile;
    }


    protected HubProfile migrateDockerToCloudHub(File file, String agent) throws CloudHubException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ContainerProfile.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            ContainerProfile containerProfile = (ContainerProfile) unmarshaller.unmarshal(file);
            CloudHubProfile profile = createCloudProfile(VirtualSystem.DOCKER, agent);
            profile.setHypervisor(containerProfile.getEngine());
            profile.setVm(containerProfile.getContainer());
            profile.setExcludes(containerProfile.getExcludes());
            saveProfile(profile);
            return profile;
        } catch (JAXBException e) {
            String msg = "Failed to migrate profile: " + file.getPath();
            log.error(msg, e);
            throw new CloudHubException(msg, e);
        }

    }

    /**
     * For support from 7.0.2 -> CloudHub 2.0. When 2.0 is installed on 7.0.2 systems, the following profiles are copied
     * into the groundwork config/cloudhub/profile-templates directory and are expected to be there:
     *
     *     1. vmware_monitoring_profile.xml
     *     2. rhev_monitoring_profile.xml
     *     3. openstack_monitoring_profile.xml
     *     4. opendaylight_monitoring_profile.xml
     *     5. docker_monitoring_profile.xml
     *
     * This method reads the profile locally instead of remotely for backward compatibility
     *
     * @param clazz
     * @param virtualSystem
     * @return
     * @throws CloudHubException
     */
    protected HubProfile readProfileTemplateInternal(Class clazz, VirtualSystem virtualSystem) throws CloudHubException {
        HubProfile profile = null;
        ProfileType profileType = ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem);
        String path = templatePathFromVirtualSystem(profileType);
        try {
            File file = new File(path);
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                profile = (HubProfile) unmarshaller.unmarshal(file);
                profile.setProfileType(profileType);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new CloudHubException("Failed to read profile template: " + path, e);
        }
        return profile;
    }

    @Override
    public HubProfile readRemoteProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration) {
        switch (ProfileConversion.convertVirtualSystemToHubType(virtualSystem)) {
            case cloud:
                return readRemoteCloudProfile(virtualSystem, configuration);
            case network:
                return readRemoteNetworkProfile(virtualSystem, configuration);
            case container:
                return readRemoteContainerProfile(virtualSystem, configuration);
        }
        return null;
    }

    @Override
    public CloudHubProfile readRemoteCloudProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration) {
        if (configuration.getGwosVersion().startsWith(GWOSConfiguration.DEFAULT_VERSION)) {
            try {
                ProfileClient client = new ProfileClient(this.buildRsConnectionString(configuration));
                return client.lookupCloud(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem));
            }
            catch (CollageRestException e) {
                log.error("Failed to connect to remote profile: " + e.getMessage(), e);
            }
        }
        return (CloudHubProfile)readProfileTemplate(virtualSystem);
    }

    @Override
    public NetHubProfile readRemoteNetworkProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration) {
        if (configuration.getGwosVersion().startsWith(GWOSConfiguration.DEFAULT_VERSION)) {
            try {
                ProfileClient client = new ProfileClient(this.buildRsConnectionString(configuration));
                return client.lookupNetwork(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem));
            }
            catch (CollageRestException e) {
                log.error("Failed to connect to remote profile: " + e.getMessage(), e);
            }
        }
        return (NetHubProfile)readProfileTemplate(virtualSystem);
    }

    @Override
    public ContainerProfile readRemoteContainerProfile(VirtualSystem virtualSystem, GWOSConfiguration configuration) {

        if (configuration.getGwosVersion().startsWith(GWOSConfiguration.DEFAULT_VERSION)) {
            try {
                ProfileClient client = new ProfileClient(this.buildRsConnectionString(configuration));
                return client.lookupContainer(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem));
            }
            catch (CollageRestException e) {
                log.error("Failed to connect to remote profile: " + e.getMessage(), e);
            }
        }
        return (ContainerProfile)readProfileTemplate(virtualSystem);
    }

    @Override
    public HubProfile readProfileTemplate(VirtualSystem virtualSystem) {
        ProfileType profileType = ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem);
        String path = templatePathFromVirtualSystem(profileType);
        boolean exists = false;
        try {
            File file = new File(path);
            exists = file.exists();
        } catch (Exception e) {
            log.error("Could not read profile template, will try to create " + path);
        }
        if (!exists) {
            File dir = new File(path).getParentFile();
            boolean made = dir.mkdirs();
            log.debug("Had to make profile template dirs: " + made + " for path " + path);
        }
        HubProfile profile = (HubProfile) readProfileTemplateInternal(ProfileConversion.convertVirtualSystemToProfileClass(virtualSystem), virtualSystem);
        if (profile == null) {
            log.error("Could not read profile template, creating temp profile for " + virtualSystem);
            return null;
        }
        return profile;
    }

    @Override
    public CloudHubProfile mergeCloudProfiles(VirtualSystem virtualSystem, CloudHubProfile remoteProfile, CloudHubProfile localProfile) throws CloudHubException {
        String agent = (localProfile == null) ? (remoteProfile == null) ? "unknown" : remoteProfile.getAgent() : localProfile.getAgent();
        CloudHubProfile mergedProfile = new CloudHubProfile(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem), agent);
        if (localProfile == null && remoteProfile == null) {
            return mergedProfile;
        }
        if (localProfile == null) {
            return remoteProfile;
        }
        if (remoteProfile == null) {
            return localProfile;
        }
        int count = mergeInternal(
                localProfile.getHypervisor().getMetrics(), localProfile.getVm().getMetrics(), localProfile.getCustom().getMetrics(),
                remoteProfile.getHypervisor().getMetrics(), remoteProfile.getVm().getMetrics(), remoteProfile.getCustom().getMetrics(),
                mergedProfile.getHypervisor().getMetrics(), mergedProfile.getVm().getMetrics(), mergedProfile.getCustom().getMetrics(),
                remoteProfile.getExcludes());
        if (count > 0) {
            saveProfile(mergedProfile);
        }
        return mergedProfile;
    }

    @Override
    public NetHubProfile mergeNetworkProfiles(VirtualSystem virtualSystem, NetHubProfile remoteProfile, NetHubProfile localProfile) throws CloudHubException {
        String agent = (localProfile == null) ? (remoteProfile == null) ? "unknown" : remoteProfile.getAgent() : localProfile.getAgent();
        NetHubProfile mergedProfile = new NetHubProfile(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem), agent);
        if (localProfile == null && remoteProfile == null) {
            return mergedProfile;
        }
        if (localProfile == null) {
            return remoteProfile;
        }
        if (remoteProfile == null) {
            return localProfile;
        }
        int count = mergeInternal(localProfile.getController().getMetrics(), localProfile.getSwitch().getMetrics(), null,
                      remoteProfile.getController().getMetrics(), remoteProfile.getSwitch().getMetrics(), null,
                      mergedProfile.getController().getMetrics(), mergedProfile.getSwitch().getMetrics(), null,
                      remoteProfile.getExcludes());
        if (count > 0) {
            saveProfile(mergedProfile);
        }
        return mergedProfile;
    }

    @Override
    public ContainerProfile mergeContainerProfiles(VirtualSystem virtualSystem, ContainerProfile remoteProfile, ContainerProfile localProfile) throws CloudHubException {
        String agent = (localProfile == null) ? (remoteProfile == null) ? "unknown" : remoteProfile.getAgent() : localProfile.getAgent();
        ContainerProfile mergedProfile = new ContainerProfile(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem), agent);
        if (localProfile == null && remoteProfile == null) {
            return mergedProfile;
        }
        if (localProfile == null) {
            return remoteProfile;
        }
        if (remoteProfile == null) {
            return localProfile;
        }
        int count = mergeInternal(localProfile.getEngine().getMetrics(), localProfile.getContainer().getMetrics(), null,
                remoteProfile.getEngine().getMetrics(), remoteProfile.getContainer().getMetrics(), null,
                mergedProfile.getEngine().getMetrics(), mergedProfile.getContainer().getMetrics(), null,
                remoteProfile.getExcludes());
        if (count > 0) {
            saveProfile(mergedProfile);
        }
        return mergedProfile;
    }

    public int mergeInternal(List<Metric> localPrimary, List<Metric> localSecondary, List<Metric> localCustom,
                              List<Metric> remotePrimary, List<Metric> remoteSecondary, List<Metric> remoteCustom,
                              List<Metric> mergedPrimary, List<Metric> mergedSecondary, List<Metric> mergedCustom,
                              Excludes excludes)
            throws CloudHubException {
        try {
            int count = 0;

            // Merge and update primary metrics
            Map<String, Metric> uniqueHypMetrics = new HashMap<String, Metric>();
            for (Metric hypMetric : remotePrimary) {
                uniqueHypMetrics.put(hypMetric.getName(), hypMetric);
            }
            count += specialMergeCases(localPrimary, uniqueHypMetrics, excludes);
            for (Metric metric : uniqueHypMetrics.values()) {
                mergedPrimary.add(metric);
            }

            // Merge and update secondary metrics
            HashMap<String, Metric> uniqueVmMetrics = new HashMap<String, Metric>();
            for (Metric vmMetric : remoteSecondary) {
                uniqueVmMetrics.put(vmMetric.getName(), vmMetric);
            }
            count += specialMergeCases(localSecondary, uniqueVmMetrics, excludes);
            for (Metric metric : uniqueVmMetrics.values()) {
                mergedSecondary.add(metric);
            }

            // Merge and update custom metrics
            if (localCustom != null) {
                HashMap<String, Metric> uniqueCustomMetrics = new HashMap<String, Metric>();
                for (Metric customMetric : remoteCustom) {
                    uniqueCustomMetrics.put(customMetric.getName(), customMetric);
                }
                count += specialMergeCases(localCustom, uniqueCustomMetrics, null);
                for (Metric metric : uniqueCustomMetrics.values()) {
                    mergedCustom.add(metric);
                }
            }
            return count;

        } catch (Exception e) {
            throw new CloudHubException("Failed to merge the profiles ", e);
        }
    }

    /**
     * Special merge cases:
     * (1) always use the remote copy description, compute type and source type, never allow override of these fields
     * (2) as of version 2.3, expression and format attributes added. These can be overriden but if not, ensure they are set locally
     * (3) exclude processing to remove no longer supported metrics
     *
     * @param local The local copy of the metric to be updated on special cases
     * @param uniqueMetrics The remote copy as a map to be checked for special cases
     * @param excludes
     * @return count if any special cases have been met, which is a hint to save the profile
     */
    protected int specialMergeCases(List<Metric> local, Map<String, Metric> uniqueMetrics, Excludes excludes) {
        int count = 0;
        for (Metric localMetric : local) {
            Metric backup = uniqueMetrics.get(localMetric.getName());
            uniqueMetrics.put(localMetric.getName(), localMetric);
            if (backup != null) {
                // always override description field from remote
                if (localMetric.getDescription() == null && backup.getDescription() != null) {
                    localMetric.setDescription(backup.getDescription());
                }
                if (localMetric.getSourceType() == null && backup.getSourceType() != null) {
                    localMetric.setSourceType(backup.getSourceType());
                    count++;
                }
                if (localMetric.getComputeType() == null && backup.getComputeType() != null) {
                    localMetric.setComputeType(backup.getComputeType());
                    count++;
                }
                // Upgrade support for new expressions and format attributes in 2.3
                if (localMetric.getFormat() == null && backup.getFormat() != null) {
                    localMetric.setFormat(backup.getFormat());
                    count++;
                }
                if (localMetric.getExpression() == null && backup.getExpression() != null) {
                    localMetric.setExpression(backup.getExpression());
                    count++;
                }
                if (localMetric.getServiceType() == null && backup.getServiceType() != null) {
                    localMetric.setServiceType(backup.getServiceType());
                    count++;
                }

            }
        }
        // @since 7.1.1 / CloudHub 2.2 - remove deprecated (excluded) metrics
        if (excludes != null && excludes.getExcludes() != null) {
            for (String exclude: excludes.getExcludes()) {
                if (uniqueMetrics.remove(exclude) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public boolean doesProfileExist(VirtualSystem virtualSystem, String agent) throws CloudHubException {
        String path = newFilePathFromVirtualSystem(ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem), agent);
        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception e) {
            throw new CloudHubException("Failed to access check profile: " + path, e);
        }
    }

    @Override
    public ProfileMetrics readMetrics(VirtualSystem virtualSystem, String agent) {
        switch (ProfileConversion.convertVirtualSystemToHubType(virtualSystem)) {
            case cloud: {
                CloudHubProfile profile = readCloudProfile(virtualSystem, agent);
                if (profile != null)
                    return new ProfileMetrics(profile.getHypervisor().getMetrics(), profile.getVm().getMetrics(), profile.getCustom().getMetrics());
                return null;
            }
            case network: {
                NetHubProfile profile = readNetworkProfile(virtualSystem, agent);
                if (profile != null)
                    return new ProfileMetrics(profile.getController().getMetrics(), profile.getSwitch().getMetrics());
                return null;
            }
            case container: {
                ContainerProfile profile = readContainerProfile(virtualSystem, agent);
                if (profile != null)
                    return new ProfileMetrics(profile.getEngine().getMetrics(), profile.getContainer().getMetrics());
                return null;
            }
            default:
                throw new CloudHubException("Unknown Virtual System");
        }
    }

    protected synchronized void createProfileDirectory() throws CloudHubException {
        try {
            File dir = new File(NEW_CONFIG_FILE_PATH);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new CloudHubException("Failed to create monitor profile directory: " + NEW_CONFIG_FILE_PATH);
                }
            }
        } catch (SecurityException e) {
            throw new CloudHubException("Failed to access monitoring profile directory: " + NEW_CONFIG_FILE_PATH, e);
        }
        try {
            File dir = new File(TEMPLATE_FILE_PATH);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new CloudHubException("Failed to create monitor profile template directory: " + TEMPLATE_FILE_PATH);
                }
            }
        } catch (SecurityException e) {
            throw new CloudHubException("Failed to access monitoring profile template directory: " + TEMPLATE_FILE_PATH, e);
        }

    }

    protected String filePathFromProfile(HubProfile profile) throws CloudHubException {
        ProfileType profileType = profile.getProfileType();
        if (profileType == null)
            throw new CloudHubException("Cannot not determine virtual system type from profile");
        return newFilePathFromVirtualSystem(profileType, profile.getAgent());
    }

    protected String oldFilePathFromVirtualSystem(ProfileType profileType) {
        StringBuilder fullPath = new StringBuilder();
        String prefix = profileType.name();
        String fileName = String.format(OLD_CONFIG_FILE_BASE_NAME, prefix);
        fullPath.append(OLD_CONFIG_FILE_PATH).append(fileName).append(CONFIG_FILE_EXTN);
        return fullPath.toString();
    }

    protected String newFilePathFromVirtualSystem(ProfileType profileType, String agent) {
        StringBuilder fullPath = new StringBuilder();
        String prefix = profileType.name();
        String fileName = String.format(NEW_CONFIG_FILE_BASE_NAME, prefix, agent);
        fullPath.append(NEW_CONFIG_FILE_PATH).append(fileName).append(CONFIG_FILE_EXTN);
        return fullPath.toString();
    }

    protected String templatePathFromVirtualSystem(ProfileType profileType) {
        StringBuilder fullPath = new StringBuilder();
        String prefix = profileType.name();
        String fileName = String.format(TEMPLATE_FILE_BASE_NAME, prefix);
        fullPath.append(TEMPLATE_FILE_PATH).append(fileName).append(CONFIG_FILE_EXTN);
        return fullPath.toString();
    }

    public int migrateProfiles(VirtualSystem virtualSystem, List<? extends ConnectionConfiguration> configurations)
            throws CloudHubException {
        int count = 0;
        createProfileDirectory();
        Class clazz = ProfileConversion.convertVirtualSystemToProfileClass(virtualSystem);
        if (clazz == null) {
            return 0;
        }
        HubProfile profile = readLegacyProfile(clazz, virtualSystem);
        if (profile == null) {
            return 0;
        }
        for (ConnectionConfiguration connection : configurations) {
            profile.setAgent(connection.getCommon().getAgentId());
            saveProfile(profile);
            if (log.isInfoEnabled()) {
                log.info("*** Migrated profile " + profile.getProfileType() + ", " + profile.getAgent());
            }
            count++;
        }
        removeLegacyProfile(virtualSystem);
        return count;
    }

    protected HubProfile readLegacyProfile(Class clazz, VirtualSystem virtualSystem) throws CloudHubException {
        HubProfile profile = null;
        ProfileType profileType = ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem);

        String path = oldFilePathFromVirtualSystem(profileType);
        try {
            File file = new File(path);
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                profile = (HubProfile) unmarshaller.unmarshal(file);
                profile.setProfileType(profileType);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new CloudHubException("Failed to read profile: " + path, e);
        }
        return profile;
    }

    protected boolean removeLegacyProfile(VirtualSystem virtualSystem) throws CloudHubException {
        HubProfile profile = null;
        ProfileType profileType = ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem);
        String path = oldFilePathFromVirtualSystem(profileType);
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CloudHubException("Failed to delete profile: " + path, e);
        }
        return false;
    }

    public boolean removeProfile(VirtualSystem virtualSystem, String alias) throws CloudHubException {
        HubProfile profile = null;
        ProfileType profileType = ProfileConversion.convertVirtualSystemToPropertyType(virtualSystem);
        String path = newFilePathFromVirtualSystem(profileType, alias);
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CloudHubException("Failed to delete profile: " + path, e);
        }
        return false;
    }

    @Override
    public int checkForNewMetrics(CloudHubProfile remote, CloudHubProfile local) {
        int count = 0;
        Map<String, Metric> uniqueHypMetrics = buildMetricMap(local.getHypervisor().getMetrics());
        Map<String, Metric> uniqueVmMetrics = buildMetricMap(local.getVm().getMetrics());
        Map<String, Metric> uniqueCustomMetrics = buildMetricMap(local.getCustom().getMetrics());
        Map<String, String> uniqueExcludes = buildMetricExcludeMap(local.getExcludes());
        for (Metric metric : remote.getHypervisor().getMetrics()) {
            if (!uniqueHypMetrics.containsKey(metric.getName())) {
                count++;
            }
        }
        for (Metric metric : remote.getVm().getMetrics()) {
            if (!uniqueVmMetrics.containsKey(metric.getName())) {
                count++;
            }
        }
        for (Metric metric : remote.getCustom().getMetrics()) {
            if (!uniqueCustomMetrics.containsKey(metric.getName())) {
                count++;
            }
        }
        if (remote.getExcludes() != null && remote.getExcludes().getExcludes() != null) {
            for (String exclude : remote.getExcludes().getExcludes()) {
                if (!uniqueExcludes.containsKey(exclude)) {
                    count++;
                }
            }
        }
        return count;
    }

    private Map<String,Metric> buildMetricMap(List<Metric> metrics) {
        Map<String, Metric> map = new HashMap<String, Metric>();
        for (Metric metric : metrics) {
            map.put(metric.getName(), metric);
        }
        return map;
    }

    private Map<String,String> buildMetricExcludeMap(Excludes excludes) {
        Map<String,String> map = new HashMap<>();
        if (excludes != null && excludes.getExcludes() != null) {
            for (String exclude : excludes.getExcludes()) {
                map.put(exclude, exclude);
            }
        }
        return map;
    }

}
