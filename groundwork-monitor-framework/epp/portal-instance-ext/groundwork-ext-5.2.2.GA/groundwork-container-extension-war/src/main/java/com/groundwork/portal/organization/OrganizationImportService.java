package com.groundwork.portal.organization;

/**
 * Groundwork. The purpose of this class is to import users and roles into the portal data store using the organizational service. This interface is
 * being developed to assist in the migration of from JBoss Portal to EPP. This could is expected to run at startup. Initially it will be run
 * everytime the application starts. It may necessary to implement some code to only run the very first time. This may also be adjusted to run at
 * will, perhaps with a portlet to import users whenever an admin feels it is warranted.
 *
 * @author  kmcanoy@redhat.com
 */
public interface OrganizationImportService {
    void importUsers();
}
