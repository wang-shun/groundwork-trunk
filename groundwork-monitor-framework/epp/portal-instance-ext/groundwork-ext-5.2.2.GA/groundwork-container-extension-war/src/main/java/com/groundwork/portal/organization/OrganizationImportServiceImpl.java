package com.groundwork.portal.organization;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.picocontainer.Startable;

public class OrganizationImportServiceImpl implements Startable {
    private static final Log    log                     = ExoLogger.getLogger(OrganizationImportServiceImpl.class);
    private OrganizationService organizationService;
    private String              portalContainerName;  // injected
    private static final String DEFAULT_MEMBERSHIP_TYPE = "member";

    // this test user lets us know if the import hasoccurred
    private String testUserName;
    private String importFileLocation;

    // This can be skipped for any reason. Originally for properties used in the
    // User object
    private Set<String> skipPropertySet = new HashSet<String>();

    public OrganizationImportServiceImpl(InitParams initParams) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OrganizationImportServiceImpl");
        }

        ValueParam portalContainerNameParam = initParams.getValueParam("portalContainerName");

        portalContainerName = portalContainerNameParam.getValue();

        ValueParam testUserNameParam = initParams.getValueParam("testUserName");

        testUserName = testUserNameParam.getValue();

        ValueParam importFileLocationParam = initParams.getValueParam("importFileLocation");

        importFileLocation = importFileLocationParam.getValue();
        log.info("File Location is " + importFileLocation);

        ValueParam skipListParam          = initParams.getValueParam("skipList");
        String     commaSeparatedSkipList = skipListParam.getValue();
        String[]   skipListArray          = StringUtils.split(commaSeparatedSkipList);

        for (String skip : skipListArray) {
            skipPropertySet.add(StringUtils.trim(skip));
        }

        log.trace("Portal Container Name set to : " + portalContainerName);
        organizationService = (OrganizationService) getPortalContainer(portalContainerName).getComponentInstanceOfType(OrganizationService.class);

        if (log.isDebugEnabled()) {
            log.debug("This is where I want to import the users at the moment");
            log.debug("Org Service Found: " + organizationService);
        }
    }

    private FileInputStream openFile(String type) throws Exception {
        if (importFileLocation == null) {
            throw new IllegalArgumentException("Filename is null");
        }

        File inputFile = new File(importFileLocation + "/" + type + ".xml");

        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File doesn't exist: " + importFileLocation + "/users.xml");
        }

        // Create input stream
        FileInputStream fis = new FileInputStream(inputFile);

        return fis;
    }

    private void importRoles() throws Exception {
        FileInputStream  fis    = openFile("roles");
        RoleImportHelper helper = new RoleImportHelper();

        helper.startImport(fis);

        List<ImportRole> roles      = helper.getImportRoles();
        MembershipType   memberType = organizationService.getMembershipTypeHandler().findMembershipType(DEFAULT_MEMBERSHIP_TYPE);

        for (ImportRole role : roles) {
            String groupId = "/" + role.getName();
            Group  group   = null;

            if (organizationService.getGroupHandler().findGroupById(groupId) == null) {
                group = organizationService.getGroupHandler().createGroupInstance();
                group.setGroupName(role.getName());
                group.setDescription(role.getDisplayName());
                group.setLabel(role.getDisplayName());
                organizationService.getGroupHandler().addChild(null, group, true);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Group " + role.getName() + " already exists. Ignoring");
                }
            }

            for (String member : role.getMembers()) {
                User user = organizationService.getUserHandler().findUserByName(member);

                if ((user != null) && (group != null)) {
                    organizationService.getMembershipHandler().linkMembership(user, group, memberType, true);
                } else {
                    log.info("User " + member + " not found. Could not add to group " + role.getName());
                }
            }
        }

        helper.endImport();
    }

    private void importUsers() throws Exception {
        FileInputStream  fis    = openFile("users");
        UserImportHelper helper = new UserImportHelper();

        helper.startImport(fis);

        for (ImportUser importUser : helper.getUsers()) {
            User                user  = organizationService.getUserHandler().createUserInstance(importUser.getUserName());
            Map<String, String> props = importUser.getProperties();

            user.setPassword("$!$ALa1");
            user.setFirstName(props.get("user.name.given"));
            user.setLastName(props.get("user.name.family"));
            user.setEmail(props.get("user.business-info.online.email"));

            if (StringUtils.isEmpty(user.getEmail())) {
                user.setEmail(importUser.getUserName() + "@localhost");
            }

            if (organizationService.getUserHandler().findUserByName(user.getUserName()) == null) {
                organizationService.getUserHandler().createUser(user, true);

                if (log.isDebugEnabled()) {
                    log.debug("    Created user " + user.getUserName());
                }
            }

            UserProfile profile = organizationService.getUserProfileHandler().createUserProfileInstance(user.getUserName());

            for (Map.Entry<String, String> entry : props.entrySet()) {
                if (!skipPropertySet.contains(entry.getKey()) && StringUtils.isNotEmpty(entry.getKey()) && StringUtils.isNotEmpty(entry.getValue())) {
                    profile.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            log.info("Profile: " + ReflectionToStringBuilder.toString(profile));
            organizationService.getUserHandler().saveUser(user, true);
            organizationService.getUserProfileHandler().saveUserProfile(profile, true);
        }

        helper.endImport();
    }

    private void demoImportUsers() throws Exception {
        // User user = new UserImpl("kmcanoy");
        User user = organizationService.getUserHandler().createUserInstance("kmcanoy");

        user.setPassword("gtn");
        user.setFirstName("Kevin");
        user.setLastName("McAnoy");
        user.setEmail("email@localhost.com");

        if (organizationService.getUserHandler().findUserByName(user.getUserName()) == null) {
            organizationService.getUserHandler().createUser(user, true);

            if (log.isDebugEnabled()) {
                log.debug("    Created user " + user.getUserName());
            }
        }

        UserProfile profile = organizationService.getUserProfileHandler().createUserProfileInstance(user.getUserName());

        profile.setAttribute("booze", "perfume");
        log.info("User: " + ReflectionToStringBuilder.toString(user));
        organizationService.getUserHandler().saveUser(user, true);
        organizationService.getUserProfileHandler().saveUserProfile(profile, true);
    }

    /**
     * Many portal containers can reside within the same application server. This method ensures that the correct portal is return for a user. This
     * method is similar to the provided method in the AbstractLoginModule
     *
     * @param   portalContainerName  the name of the portal container
     *
     * @return  The portal container of defined for the application
     */
    public static ExoContainer getPortalContainer(String portalContainerName) {
        ExoContainer container = ExoContainerContext.getCurrentContainer();

        if (container instanceof RootContainer) {
            container = RootContainer.getInstance().getPortalContainer(portalContainerName);

            if (container == null) {
                // Probably should write a custom exception class here
                throw new NullPointerException("The eXo container is null, because the current container is a RootContainer "
                                                 + "and there is no PortalContainer with the name '" + portalContainerName + "'.");
            }
        } else if (container == null) {
            throw new NullPointerException("The eXo container is null, because the current container is null.");
        }

        return container;
    }

    public void start() {
        // TODO Auto-generated method stub
        log.info("Starting organization importer");

        try {
            User user = organizationService.getUserHandler().findUserByName(testUserName);

            if (user != null) {  // user found means we need to import
                log.info(testUserName + " found. Need to run import");

                if (organizationService instanceof ComponentRequestLifecycle) {
                    RequestLifeCycle.begin((ComponentRequestLifecycle) organizationService);
                }

                importUsers();
                importRoles();
                organizationService.getUserHandler().removeUser(testUserName, true);

                if (organizationService instanceof ComponentRequestLifecycle) {
                    RequestLifeCycle.end();
                }
            }
        } catch (Exception ex) {
            log.error("Failed to import users", ex);
        }
    }

    public void stop() {
        // TODO Auto-generated method stub
        log.error("stop");
    }
}
