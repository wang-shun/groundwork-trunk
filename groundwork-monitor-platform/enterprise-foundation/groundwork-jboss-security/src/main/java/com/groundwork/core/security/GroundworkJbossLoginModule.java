package com.groundwork.core.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.jboss.security.SimpleGroup;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.acl.Group;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/*
 * GroundworkJbossLoginModule specially build for securing the foundation soap and
 * rest apis. This module also synchronizes LDAP users and groups to portal
 * Author - Arul Shanmugam
 * Since GWM 7.0
 */
public class GroundworkJbossLoginModule extends UsernamePasswordLoginModule {

    private static Log log = LogFactory
            .getLog(GroundworkJbossLoginModule.class);

    private static final String AUTH_ENDPOINT = "/sso/authcallback/postauth/";

    private static final String ROLES_ENDPOINT = "/sso/authcallback/roles/";

    private String username = null;

    private String password = null;

    private static String BASE_URL = null;

    private static final String DEFAULT_PASSWORD = "dummy";

    private OrganizationService organizationService;

    private boolean isLDAP = false;

    private static final String DESCRIPTION = " Description";

    private UserHandler userHandler = null;

    private MembershipTypeHandler memTypeHandler = null;

    private MembershipHandler memshipHandler = null;

    private GroupHandler groupHandler = null;

    private static final String AUTHENTICATED = "Authenticated";

    private static final String GW_AUTHENTICATED = "GWAuthenticated";

    private static final String FOUNDATION_JVM_HOME = "/usr/local/groundwork/foundation/container/jpp2";

    private boolean isStandAloneFoundation = false;

    private static String SERVER_NAME = null;

    private static final String DEFAULT_MEMBERSHIP = "gw-portal-user";

    private String EVERYONE = "Everyone";

    @SuppressWarnings("rawtypes")
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        SERVER_NAME = GateinConfiguration
                .getProperty(GateinConfiguration.JPP_PROTOCOL)
                + "://"
                + GateinConfiguration.getProperty(GateinConfiguration.JPP_HOST)
                + ":"
                + GateinConfiguration.getProperty(GateinConfiguration.JPP_PORT);
        BASE_URL = SERVER_NAME
                + "/rest";
        useFirstPass = true;
        log.debug(BASE_URL);

        isLDAP = LDAPHelper.getInstance().isLDAP();
        File standaloneFoundationFolder = new File(FOUNDATION_JVM_HOME);
        isStandAloneFoundation = standaloneFoundationFolder.exists();
        if (ExoContainerContext.getCurrentContainerIfPresent() != null) {
            ExoContainer container = ExoContainerContext
                    .getCurrentContainer();
            if (container != null) {
                organizationService = (OrganizationService) container
                        .getComponentInstanceOfType(OrganizationService.class);
                if (organizationService != null) {
                    memTypeHandler = organizationService
                            .getMembershipTypeHandler();
                    groupHandler = organizationService
                            .getGroupHandler();
                    userHandler = organizationService.getUserHandler();
                    memshipHandler = organizationService
                            .getMembershipHandler();
                }
            } // end if
        }
    }

    @Override
    public boolean login() throws LoginException {
        log.debug("Enter login");
        try {
            // Get identity set by SharedStateLoginModule in case of successful
            // authentication
            Identity identity = null;
            if (sharedState.containsKey("exo.security.identity")) {
                identity = (Identity) sharedState.get("exo.security.identity");
            }
            String[] userpass = super.getUsernameAndPassword();
            log.debug("User ==>" + userpass[0]);
            this.username = userpass[0];
            this.password = userpass[1];

            if (username != null && password != null) {
                // get URL content
                String endPoint = BASE_URL + AUTH_ENDPOINT;

                boolean output = false;


                if (ExoContainerContext.getCurrentContainerIfPresent() == null) {
                    // If we get here,  most probably it is a standalone foundation scenario. No gatein stuff running here
                    log.debug("Stand alone foundation...");
                    if (isLDAP) {
                        log.debug("LDAP is ENABLED!");
                        // Authenticate with LDAP,dont sync
                        // First get the userDN and then try to authenticate
                        String userDN = LDAPHelper.getInstance()
                                .selectUserDN(this.username);
                        output = LDAPHelper.getInstance().authenticate(
                                userDN, this.password);

                    } else {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("username", this.username);
                        map.put("password", this.password);
                        output = Boolean.parseBoolean(this.httpPost(
                                endPoint, map));
                    }
                    super.loginOk = output;
                    log.debug("Login status==>" + output);
                    return output;
                }

                ExoContainer container = ExoContainerContext
                        .getCurrentContainer();
                // If container is null then there is no portal
                // container involved, just servlet container
                // Remember this login module is used by gatein-domain as well
                // as secure api domain
                if (container != null) {

                    if (organizationService != null) {
                        log.debug("Context is Portal(eXo)!");
                        if (isLDAP) {
                            log.debug("LDAP is ENABLED!");
                            // if isLDAP == true
                            // populate roles. No need to authenticate and it is
                            // done on the UI. Only
                            // Perform authorization
                            // Create user/group in portal
                            // Link user to membership

                            org.exoplatform.services.organization.User user = userHandler
                                    .findUserByName(username);
                            String[] ldapGroups = LDAPHelper.getInstance()
                                    .selectRolesByUsername(username);
                            if (ldapGroups != null && ldapGroups.length > 0) {
                                Set<String> portalGroups = new HashSet<String>();
                                Set<String> portalRoles = new HashSet<String>();
                                for (String ldapGroup : ldapGroups) {
                                    // ignore authenticated roles
                                    if (ldapGroup.equalsIgnoreCase(GroundworkJbossLoginModule.AUTHENTICATED) ||
                                            ldapGroup.equalsIgnoreCase(GroundworkJbossLoginModule.GW_AUTHENTICATED)) {
                                        continue;
                                    }
                                    // map ldap group to portal group and role
                                    String [] portalRole = new String[]{null};
                                    String portalGroup = lookupPortalGroupAndMappedMembershipType(ldapGroup, portalRole);
                                    if (portalGroup != null) {
                                        portalGroups.add(portalGroup);
                                        portalRoles.add(portalRole[0]);
                                    }
                                }

                                if (user == null) {
                                    log.debug("User doesnot exist in portal.Lookup role for user ==>"
                                            + this.username);
                                    this.linkMembership(portalGroups);
                                } else {

                                    // If user already exist in portal, check for the roles.
                                    // If role mismatch found, then resynch it. If not just let it go
                                    Collection<Membership> dbRoles = memshipHandler
                                            .findMembershipsByUser(this.username);
                                    int portalRolesSize = portalRoles.size();
                                    int dbRoleSize = dbRoles.size();
                                    int equalSizeCounter = 0;
                                    if (portalRolesSize == dbRoleSize) {
                                        for (Membership dbRole : dbRoles) {
                                            if (portalRoles.contains(dbRole.getMembershipType())) {
                                                equalSizeCounter++;
                                            }
                                        }
                                        // since we are in equal criteria, just check on one side
                                        if (equalSizeCounter != portalRolesSize) {
                                            log.debug("Role mismatch! Removing all membership for the user in portal and synching with ldap roles");
                                            // Remove all memberships for the user and relink it
                                            memshipHandler.removeMembershipByUser(this.username,true);
                                            this.linkMembership(portalGroups);
                                        } else {
                                            log.debug("All LDAP Roles Match Portal!");
                                        }
                                    } else {
                                        log.debug("Role mismatch! Removing all membership for the user in portal and synching with ldap roles");
                                        // Remove all memberships for the user and relink it
                                        memshipHandler.removeMembershipByUser(this.username, true);
                                        this.linkMembership(portalGroups);
                                    }
                                }
                                output = true;
                                // Recreate identity
                                Authenticator authenticator = (Authenticator) ExoContainerContext
                                        .getCurrentContainer()
                                        .getComponentInstanceOfType(
                                                java.net.Authenticator.class);
                                if (authenticator != null) {
                                    identity = authenticator.createIdentity(identity
                                            .getUserId());
                                    sharedState.put("exo.security.identity",
                                            identity);
                                }

                            } else {
                                log.error("CANNOT AUTHORIZE USER! User ==>"
                                        + this.username
                                        + " No groups associated to the user!");
                                output = false;
                            }

                        } else {
                            // For Portal(eXo) & DB this login module does
                            // nothing just passthru
                            output = true;
                        }
                    } else {
                        log.debug("Context is Servlet!");
                        if (isLDAP) {
                            log.debug("LDAP is ENABLED!");
                            // Authenticate with LDAP,dont sync
                            // First get the userDN and then try to authenticate
                            String userDN = LDAPHelper.getInstance()
                                    .selectUserDN(this.username);
                            String decryptedCredentials = this.password;
                            // If the incoming username matches the ws_client webservices user then try to decrypt
                            if (this.username.equalsIgnoreCase(WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME))) {
                                // Remember here, when you get the password from the WSClientConfiguration, it is encrypted password.
                                // So check for the match and if that is true then decrypt.
                                if (this.password.equalsIgnoreCase(WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD)))
                                    decryptedCredentials = JasyptUtils.jasyptDecrypt(this.password);
                            }
                            output = LDAPHelper.getInstance().authenticate(
                                    userDN, decryptedCredentials);

                        } else {
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("username", this.username);
                            map.put("password", this.password);
                            output = Boolean.parseBoolean(this.httpPost(
                                    endPoint, map));
                        }
                    }// end if
                } // end if

                super.loginOk = output;
                log.debug("Login status==>" + output);
                return output;
            } // endif

        } catch (Exception le) {
            log.debug("Error occured during login ==>" + le.toString(), le);
            super.loginOk = false;
            return false;
        }
        super.loginOk = false;
        return false;
    }

    public boolean commit() throws LoginException {
        if (useFirstPass) {
            // Add the username and password to the shared state map
            sharedState.put("javax.security.auth.login.name", username);
            sharedState.put("javax.security.auth.login.password",
                    password.toCharArray());
        } // end if

        return true;
    }

    @Override
    protected Group[] getRoleSets() throws LoginException {
        log.debug("Enter getRoleSets");
        String roles = null;
        Group[] groups = null;
        if (username != null && password != null) {
            this.username = (String) username;
            this.password = (String) password;
            // get URL content
            String endPoint = BASE_URL + ROLES_ENDPOINT + this.username;
            roles = this.httpGet(endPoint);
            log.debug("Output roles ==>" + roles);
        } // end if

        if (roles != null) {
            StringTokenizer stkn = new StringTokenizer(roles, ",");
            groups = new Group[stkn.countTokens()];
            int index = 0;
            while (stkn.hasMoreTokens()) {
                SimpleGroup group = new SimpleGroup(stkn.nextToken());
                groups[index] = group;
                index++;
            }
        }
        return groups;
    }

    /*
     * Helper for the http post method
     */
    private String httpPost(String endPoint, HashMap<String, String> urlParams) {
        URL url = null;
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        OutputStreamWriter writer = null;
        String inputLine = null;
        StringBuffer urlParameters = new StringBuffer();
        for (Map.Entry<String, String> entry : urlParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            urlParameters.append(key);
            urlParameters.append("=");
            urlParameters.append(URLEncoder.encode(value));
            urlParameters.append("&");
        }

        String data = urlParameters.toString().substring(0,
                urlParameters.toString().length() - 1);

        try {
            url = new URL(endPoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.toString().getBytes().length));
            conn.setRequestProperty("Content-Language", "en-US");

            writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data);
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            inputLine = reader.readLine();
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (writer != null)
                    writer.close();
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return inputLine;
    }


    /*
     * Helper for the http Get method
     */
    private String httpGet(String endPoint) {
        URL url;
        URLConnection conn = null;
        BufferedReader br = null;
        InputStreamReader reader = null;
        String inputLine = null;
        try {

            url = new URL(endPoint);
            conn = url.openConnection();
            reader = new InputStreamReader(conn.getInputStream());

            // open the stream and put it into BufferedReader
            br = new BufferedReader(reader);

            // Rest returns only one line output
            inputLine = br.readLine();
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            try {
                if (br != null)
                    br.close();
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return inputLine;

    }

    @Override
    protected String getUsersPassword() {
        return password;
    }

    /**
     * Creates group
     */
    private org.exoplatform.services.organization.Group createGroup(
            String parent, String name) {
        org.exoplatform.services.organization.Group newGroup = null;
        try {
            org.exoplatform.services.organization.Group parentGroup = null;
            if (parent != null) {
                parentGroup = groupHandler.findGroupById(parent);
            }
            newGroup = groupHandler.createGroupInstance();
            newGroup.setGroupName(name);
            newGroup.setDescription(name + DESCRIPTION);
            newGroup.setLabel(name);
            if (parentGroup != null) {
                groupHandler.addChild(parentGroup, newGroup, true);
            } else {
                groupHandler.addChild(null, newGroup, true);
            }
            groupHandler.saveGroup(newGroup, true);
            newGroup = groupHandler.findGroupById(name);
        } catch (Exception e) {
            log.error("Error on create group [" + name + "] " + e.getMessage(),
                    e);
        }
        return newGroup;
    }


    /**
     * Links memberships
     */
    private void linkMembership(Set<String> groups) {
        if (groups != null) {
            try {
                for (String group : groups) {
                    // Create group/membership if not exists. Filter out
                    // authenticated
                    if (!group.equals(GroundworkJbossLoginModule.AUTHENTICATED) && !group.equals(GroundworkJbossLoginModule.GW_AUTHENTICATED)) {
                        // map group to portal group and mapped membership type
                        String [] portalMappedMembershipType = new String[]{null};
                        String portalGroup = lookupPortalGroupAndMappedMembershipType(group, portalMappedMembershipType);
                        // if membership mapping exists, create and link user to group
                        if (portalGroup != null) {
                            org.exoplatform.services.organization.User user = userHandler.findUserByName(this.username);
                            if (user == null) {
                                user = userHandler.createUserInstance(this.username);
                                // This is just dummy password. Password never synched!
                                user.setPassword(DEFAULT_PASSWORD);
                                HashMap<String, String> userProps = LDAPHelper
                                        .getInstance().selectUserProperties(this.username);
                                user.setFirstName(userProps.get("firstname") == null ? this.username
                                        : userProps.get("firstname"));
                                user.setLastName(userProps.get("lastname") == null ? this.username
                                        : userProps.get("lastname"));
                                user.setEmail(userProps.get("mail") == null ? this.username
                                        + "@mycompany.com" : userProps.get("mail"));

                                userHandler.createUser(user, true);
                                // organization-configuration.xml has a NewUserEventListener. This listener creates a user
                                // with a default membership of gw-portal-user whenever new user is created. This is required
                                // for DB based portal config as well as LDAP based config. Since we cannot remove that listener,
                                // we just remove all memberships for the newly created user here. Then link it based on the definition
                                // in ldap-mapping-directives.properties.
                                memshipHandler.removeMembershipByUser(this.username, true);

                            } // end if
                            // lookup portal group
                            org.exoplatform.services.organization.Group newPortalGroup = findGroup(portalGroup);
                            if (newPortalGroup == null) {
                                newPortalGroup = this.createGroup(null, portalGroup);
                            }
                            // lookup membership type
                            MembershipType newPortalMembershipType = memTypeHandler.findMembershipType(portalMappedMembershipType[0]);
                            if (newPortalMembershipType == null) {
                                log.info("Cannot find " + portalMappedMembershipType[0] + " membership in the portal DB, assigning to lowest privilege membership " + GroundworkJbossLoginModule.DEFAULT_MEMBERSHIP);
                                newPortalMembershipType = memTypeHandler.findMembershipType(GroundworkJbossLoginModule.DEFAULT_MEMBERSHIP);
                            }
                            // link user to group using membership type
                            log.debug("Linking " + user.getUserName()
                                    + " with portal group " + newPortalGroup.getGroupName()
                                    + " and membership type, (a.k.a role), " + newPortalMembershipType.getName());
                            memshipHandler.linkMembership(user, newPortalGroup, newPortalMembershipType, true);
                        } else {
                            log.info("User " + this.username + " is associated to " + group + " in LDAP/AD. But there is no mention of that group in " + LDAPMappingDirectiveConfig.getLDAPMappingPropertyFileLocation());
                        }


                    } // end if
                } // end for
            } catch (Exception e) {
                log.error("Error while linking membership: " + e.getMessage());
            }
        }

    }

    private static String lookupPortalGroupAndMappedMembershipType(String ldapGroup, String [] portalMappedMembershipType) {
        // recursively lookup portal group and membership type/role from LDAP mapping directive config
        String portalGroup = ldapGroup;
        String membershipType = LDAPMappingDirectiveConfig.getProperty(portalGroup);
        if (membershipType == null || membershipType.equals("")) {
            return null;
        }
        for (String nextMembershipType = LDAPMappingDirectiveConfig.getProperty(membershipType); (nextMembershipType != null && !nextMembershipType.equals("")); nextMembershipType = LDAPMappingDirectiveConfig.getProperty(membershipType)) {
            portalGroup = membershipType;
            membershipType = nextMembershipType;
        }
        if (portalMappedMembershipType != null) {
            portalMappedMembershipType[0] = membershipType;
        }
        return portalGroup;
    }

    /**
     * Helper to check if group exists
     */
    private org.exoplatform.services.organization.Group findGroup(String name) {
        Collection<org.exoplatform.services.organization.Group> groups = null;
        try {
            groups = groupHandler.getAllGroups();
        } catch (Exception e) {
            log.error(
                    "Error on finding group [" + name + "] " + e.getMessage(),
                    e);
        }
        if (groups != null) {
            for (org.exoplatform.services.organization.Group exogroup : groups) {
                String groupName = exogroup.getGroupName();
                if (name.equalsIgnoreCase(groupName))
                    return exogroup;
            }
        }
        return null;
    }

}