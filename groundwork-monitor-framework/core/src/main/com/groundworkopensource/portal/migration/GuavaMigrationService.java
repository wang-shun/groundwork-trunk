/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.migration;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.security.auth.Subject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.core.identity.service.IdentityServiceControllerImpl;
import org.jboss.portal.core.impl.model.CustomizationManagerService;
import org.jboss.portal.core.impl.model.portal.PageImpl;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.security.PortalPermission;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;
import org.jboss.portal.theme.ThemeConstants;

/**
 * Migrates legacy guava data into the JBoss Portal framework. This component
 * should only be run once, when the JBoss Portal first starts. Note: this
 * service depends on correct credentials (i.e. username and password) for the
 * guava database being set in /usr/local/groundwork/config/db.properties.
 * 
 * @author Paul Burry
 * @version $Revision: 2053 $
 * @since GWMON 6.0
 */
public class GuavaMigrationService extends AbstractJBossService
{
    /**
     * User profile attribute indicating if the user should change their 
     * password upon initial login.
     */
    public static final String CHANGE_PASSWORD_ATTR = 
        "portal.user.changePassword";
    
    /**
     * The name of the lock file that will be created when this service is run.
     */
    private static final String LOCK_FILE_NAME = "migration.lck";

    /**
     * The file system path of the migration properties file.
     */
    private String configFilePath = "/usr/local/groundwork/config/migration.properties";

    /**
     * The file system path of the Guava DB configuration file.
     */
    private String guavaConfigFilePath = "/usr/local/groundwork/config/db.properties";

    /**
     * The initial password of all new users in the system.
     */
    private String initialUserPassword;

    /**
     * Force this component to run on startup, even if it already has before.
     */
    private boolean forceRun = false;

    /**
     * The Identity Service Controller.
     */
    private IdentityServiceControllerImpl identityServiceController;

    /**
     * The JBoss Portal customization manager.
     */
    private CustomizationManager customizationManager;

    /**
     * @return the customizationManager
     */
    public CustomizationManager getCustomizationManager() {
        return customizationManager;
    }

    /**
     * @param customizationManager
     *            the customizationManager to set
     */
    public void setCustomizationManager(
            CustomizationManager customizationManager) {
        this.customizationManager = customizationManager;
    }

    /**
     * @return the configFilePath
     */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * @param configFilePath
     *            the configFilePath to set
     */
    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    /**
     * @return the guavaConfigFilePath
     */
    public String getGuavaConfigFilePath() {
        return guavaConfigFilePath;
    }

    /**
     * @param guavaConfigFilePath
     *            the guavaConfigFilePath to set
     */
    public void setGuavaConfigFilePath(String guavaConfigFilePath) {
        this.guavaConfigFilePath = guavaConfigFilePath;
    }

    /**
     * @return the initialUserPassword
     */
    public String getInitialUserPassword() {
        return initialUserPassword;
    }

    /**
     * @param initialUserPassword
     *            the initialUserPassword to set
     */
    public void setInitialUserPassword(String initialUserPassword) {
        this.initialUserPassword = initialUserPassword;
    }

    /**
     * @return the forceRun
     */
    public boolean isForceRun() {
        return forceRun;
    }

    /**
     * @param forceRun
     *            the forceRun to set
     */
    public void setForceRun(boolean forceRun) {
        this.forceRun = forceRun;
    }

    /**
     * @return the identityServiceController
     */
    public IdentityServiceControllerImpl getIdentityServiceController() {
        return identityServiceController;
    }

    /**
     * @param identityServiceController
     *            the identityServiceController to set
     */
    public void setIdentityServiceController(
            IdentityServiceControllerImpl identityServiceController) {
        this.identityServiceController = identityServiceController;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.system.ServiceMBeanSupport#startService()
     */
    @Override
    protected void startService() throws Exception {
        log.info("Starting Guava migration service");

        super.startService();

        if (initialUserPassword == null) {
            log.warn("Initial user password is not set: using 'changeme'");
            initialUserPassword = "changeme";
        }

        // Use a lock file to indicate that this service has already been run.
        File lockFile = new File(LOCK_FILE_NAME);

        Connection guavaDb = null;
        Session session = null;
        Transaction transaction = null;

        // HACK: Because we aren't logged in when this runs, use a dummy
        // authorization manager to bypass normal JBoss Portal security
        // checks when creating dashboard objects
        PortalAuthorizationManagerFactory authManagerFactory = ((CustomizationManagerService) customizationManager)
                .getPortalAuthorizationManagerFactory();
        ((CustomizationManagerService) customizationManager)
                .setPortalAuthorizationManagerFactory(new MigrationAuthorizationManagerFactory());

        try {
            if (!lockFile.exists() || forceRun) {
                lockFile.createNewFile();

                // Load the guava DB properties file.
                log.info("Using Guava DB properties from "
                        + guavaConfigFilePath);
                File guavaConfigFile = new File(guavaConfigFilePath);
                if (!guavaConfigFile.canRead()) {
                    throw new GuavaMigrationException(
                            "Guava DB properties file not found, or is not "
                                    + "readable: aborting migration");
                }
                Properties guavaProperties = new Properties();
                guavaProperties.load(new FileInputStream(guavaConfigFile));

                // Connect to the guava DB
                Class.forName("com.mysql.jdbc.Driver");
                String username = guavaProperties.getProperty("guava.username",
                        "root").trim();
                String password = guavaProperties.getProperty("guava.password",
                        "").trim();
                String dbhost = guavaProperties.getProperty("guava.dbhost",
                        "localhost").trim();
                String database = guavaProperties.getProperty("guava.database",
                        "guava").trim();
                String guavaDbUrl = "jdbc:mysql://" + dbhost + "/" + database
                        + "?user=" + username + "&password=" + password;

                log.debug("Connecting to guava DB with connection string: "
                        + guavaDbUrl);

                guavaDb = DriverManager.getConnection(guavaDbUrl);

                // Load the migration properties file
                File migrationConfigFile = new File(configFilePath);
                boolean migrateDashboards = false;
                Properties migrationProperties = null;
                if (!migrationConfigFile.exists()) {
                    log.warn("Dashboard migration properties not found, or " +
                            "is not readable: dashboards will not be migrated");
                } else {
                    migrationProperties = new Properties();
                    migrationProperties
                            .load(new FileInputStream(configFilePath));
                    migrateDashboards = true;
                }

                // Get all of the relevant JBoss Portal identity service modules
                SessionFactory identitySessionFactory = (SessionFactory) new InitialContext()
                        .lookup("java:portal/IdentitySessionFactory");
                if (identitySessionFactory != null) {
                    session = identitySessionFactory.openSession();
                    transaction = session.beginTransaction();
                }
                IdentityServiceControllerImpl identityService = (IdentityServiceControllerImpl) new InitialContext()
                        .lookup("java:/portal/IdentityServiceController");
                if (identityService == null) {
                    throw new GuavaMigrationException(
                            "Cannot access identity service: migration cannot continue");
                }

                IdentityContext identityContext = identityService
                        .getIdentityContext();
                UserModule userModule = (UserModule) identityContext
                        .getObject(IdentityContext.TYPE_USER_MODULE);
                UserProfileModule userProfileModule = (UserProfileModule) identityContext
                        .getObject(IdentityContext.TYPE_USER_PROFILE_MODULE);
                RoleModule roleModule = (RoleModule) identityContext
                        .getObject(IdentityContext.TYPE_ROLE_MODULE);
                MembershipModule membershipModule = (MembershipModule) identityContext
                        .getObject(IdentityContext.TYPE_MEMBERSHIP_MODULE);

                // Map Roles
                Statement statement = guavaDb.createStatement();
                ResultSet resultSet = statement
                        .executeQuery("select role_id, name, description from guava_roles");
                HashMap<Integer, Role> roleMap = new HashMap<Integer, Role>();
                Integer guavaId = null;

                while (resultSet.next()) {
                    guavaId = resultSet.getInt("role_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");

                    // The Administrators and Operators roles already exist in
                    // JBoss Portal: do not create new ones
                    Role role = null;
                    if (!name.equals("Administrators")
                            && !name.equals("Operators")) {
                        log.info("Creating role: '" + name + "'");
                        role = roleModule.createRole(name, description);
                    } else {
                        role = roleModule.findRoleByName(name
                                .equals("Administrators") ? "Admin"
                                : "Operator");
                        log.info("Mapping Guava role '" + name + "' to '"
                                + role.getName() + "'");
                    }

                    roleMap.put(guavaId, role);
                }
                resultSet.close();

                // Map Users
                resultSet = statement
                        .executeQuery("select gu.user_id, gu.username from guava_users gu");
                HashMap<Integer, User> userMap = new HashMap<Integer, User>();

                while (resultSet.next()) {
                    guavaId = resultSet.getInt("user_id");
                    String name = resultSet.getString("username");

                    User user = null;
                    try {
                        user = userModule.findUserByUserName(name);
                        log.warn("User '" + name + "' already exists");
                    } catch (NoSuchUserException exception) {
                        log.info("Creating user '" + name + "'");
                        user = userModule.createUser(name, initialUserPassword);
                        userProfileModule.setProperty(user,
                                User.INFO_USER_ENABLED, Boolean.TRUE);
                        
                        // Force the user to change his/her password upon 
                        // first logging in
                        userProfileModule.setProperty(user, 
                                CHANGE_PASSWORD_ATTR, Boolean.toString(true));
                    }
                    userMap.put(guavaId, user);
                }
                resultSet.close();

                // Map user/role relationships
                resultSet = statement.executeQuery("select user_id, role_id "
                        + "from guava_role_assignments order by user_id");

                HashMap<User, Set<Role>> userRoleMap = new HashMap<User, Set<Role>>();
                while (resultSet.next()) {
                    User user = userMap.get(resultSet.getInt("user_id"));
                    if (user != null) {
                        Set<Role> roles = userRoleMap.get(user);
                        if (roles == null) {
                            roles = new HashSet<Role>();
                            userRoleMap.put(user, roles);
                        }

                        Role role = roleMap.get(resultSet.getInt("role_id"));
                        if (role != null) {
                            log.info("Assigning user '" + user.getUserName()
                                    + "' to role '" + role.getName() + "'");
                            roles.add(role);
                        }
                    }
                }
                resultSet.close();

                for (User user : userRoleMap.keySet()) {
                    Set<Role> roles = userRoleMap.get(user);
                    if (roles.size() > 0) {
                        membershipModule.assignRoles(user, roles);
                    }
                }

                if (migrateDashboards) {
                    // Create a set of "trouble" widgets (i.e. Troubled Hosts,
                    // Troubled Services, etc.). These widgets will be mapped
                    // to a single Seurat View instance.
                    HashSet<String> troubleWidgets = new HashSet<String>();
                    String troubleWidgetString = migrationProperties
                            .getProperty("widget.troubleList");
                    if (troubleWidgetString != null) {
                        for (String troubleWidget : Pattern.compile(",").split(
                                troubleWidgetString)) {
                            troubleWidgets.add(troubleWidget);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Trouble widgets:");
                        for (String troubleWidget : troubleWidgets) {
                            log.debug(troubleWidget);
                        }
                    }

                    // Get user dashboards
                    resultSet = statement
                            .executeQuery("select id, uid from dashboard where isdefault = '1'");
                    HashMap<User, Integer> dashboardMap = new HashMap<User, Integer>();

                    while (resultSet.next()) {
                        guavaId = resultSet.getInt("id");
                        Integer userId = resultSet.getInt("uid");
                        log.debug("Dashboard ID: " + guavaId + ", User ID: "
                                + userId);

                        User user = userMap.get(userId);
                        if (user != null) {
                            log.debug("Dashboard for user "
                                    + user.getUserName() + " retrieved");
                            dashboardMap.put(user, guavaId);
                        }
                    }
                    resultSet.close();

                    // Map each user's 5.3 dashboard widgets to 6.0 portlets and
                    // add them to their new dashboards.
                    PreparedStatement preparedStatement = guavaDb
                            .prepareStatement("select widget.id, widget.name, widget.class "
                                    + "from widget, widgetmap "
                                    + "where widgetmap.widget_id = widget.id and "
                                    + "widgetmap.dashboard_id = ?");

                    for (User user : dashboardMap.keySet()) {
                        log.debug("Importing widgets for user "
                                + user.getUserName());
                        PageImpl dashboardPage = (PageImpl) customizationManager
                                .getDashboard(user).getDefaultPage();
                        preparedStatement.setInt(1, dashboardMap.get(user));
                        resultSet = preparedStatement.executeQuery();
                        boolean troubleWidgetMapped = false;
                        while (resultSet.next()) {
                            String widgetName = resultSet.getString(2);
                            String widgetClass = resultSet.getString(3);
                            log.debug("Widget name: " + widgetName
                                    + ", class: " + widgetClass);
                            if (troubleWidgets.contains(widgetClass)) {
                                if (troubleWidgetMapped) {
                                    continue;
                                } else {
                                    widgetName = "Trouble View";
                                    troubleWidgetMapped = true;
                                }
                            }

                            String portletId = migrationProperties
                                    .getProperty("widget." + widgetClass);
                            log.debug("Mapped to portlet: " + portletId);
                            if (portletId != null) {
                                String region = migrationProperties
                                        .getProperty("portlet." + portletId
                                                + ".region");
                                log.debug("Adding window: name = " + widgetName
                                        + ", URI = " + portletId
                                        + ", region = " + region);
                                Window window = dashboardPage.createWindow(
                                        widgetName, ContentType.PORTLET,
                                        portletId);

                                if (widgetName != null) {
                                    window.setDisplayName(new LocalizedString(
                                            widgetName));
                                }

                                if (region != null) {
                                    window.setDeclaredProperty(
                                            ThemeConstants.PORTAL_PROP_REGION,
                                            region);
                                }
                            }
                        } // end while (resultSet...)

                        resultSet.close();
                    } // end for (User user : dashboardMap...)
                } // end if (migrateDashboards)

                log.info("Guava migration service complete");
            } else {
                log
                        .warn("Guava migration service has already been run.  Aborting.");
            }
        } catch (GuavaMigrationException exception) {
            log.error(exception.getMessage());
            if (transaction != null) {
                transaction.rollback();
            }
        } catch (Exception exception) {
            log.error("Error during migration: cannot continue", exception);
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            if (authManagerFactory != null) {
                ((CustomizationManagerService) customizationManager)
                        .setPortalAuthorizationManagerFactory(authManagerFactory);
            }

            if (transaction != null && !transaction.wasRolledBack()) {
                transaction.commit();
            }

            if (session != null) {
                session.close();
            }

            if (guavaDb != null && !guavaDb.isClosed()) {
                guavaDb.close();
            }
        }

        log.info("Stopping migration service");
        stopService();
    }

    /**
     * Dummy authorization manager factory.
     */
    private static final class MigrationAuthorizationManagerFactory implements
            PortalAuthorizationManagerFactory
    {

        private static final PortalAuthorizationManager manager = new PortalAuthorizationManager() {
            public boolean checkPermission(PortalPermission permission)
                    throws IllegalArgumentException, PortalSecurityException {
                return true;
            }

            public boolean checkPermission(Subject checkedSubject,
                    PortalPermission permission)
                    throws IllegalArgumentException, PortalSecurityException {
                return true;
            }
        };

        public PortalAuthorizationManager getManager()
                throws PortalSecurityException {
            return manager;
        }
    }
}
