The users.xml and roles.xml are sample files for testing the import users and roles features.
To use these files you must alter the file groundwork-container-extension-war\src\main\webapp\WEB-INF\conf\groundwork-portal\portal\organization-import-configuration.xml
change the importFileLocation parameter to match the location of the directory where these files exists.
The file is called with new java.io.File(path + file);