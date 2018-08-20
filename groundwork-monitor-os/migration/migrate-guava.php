<?php
/*
Guava - A PHP Based Application Framework and Environment
Copyright (C) 2008 Groundwork Open Source Solutions

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 

migrate-guavadb.php
Attempts to migrate Guava db from these versions:

4.5-26 to 5.0.5 (SB)

5.0.2 -> 5.0.5 (SB)

5.0.3 -> 5.0.5 (SB)

5.0.4 -> 5.0.5 (SB)

*/

/* HELPER METHODS */
// Database Abstraction Library (ADOdb)
require_once('/usr/local/groundwork/guava/adodb/adodb.inc.php');
require_once('/usr/local/groundwork/guava/adodb/adodb-exceptions.inc.php');

define(GUAVA_FS_ROOT, "/usr/local/groundwork/guava/");

define(SCHEMA_PRODUCT, "gwmsb");
define(SCHEMA_VERSION, "5.1");


class GuavaPackage {
	private $basepath;
	private $packageInfo;
	private $errorno;
	private $errormsg;
	private $sysmodules;
	private $views;
	private $supportfiles;
	
	public function __construct($filename) {
		$this->basepath = dirname($filename) . "/";
		$this->package = null;
		$this->views = array();
		$this->sysmodules = array();
		$this->supportfiles = array();
		
		if ( ($fp = @fopen($filename, 'r')) == FALSE) {
		    $this->errorno = GUAVA_FAILED_TO_OPEN_PACKAGE_FILE;
		    $this->errormsg = "Failed to read package file:" . $filename;
		    throw new Exception($this->errormsg);
		}
		$objectName = '';
		while ($line = fgets($fp)) {
		    if (preg_match('/^\s*(|#.*)$/', $line)) {
				continue;
		    }		    
		    if (preg_match('/^\s*define\s+(\S+)\s*{\s*$/', $line, $regs)) {
				$objectName = $regs[1];
				$tmpobject = array();
				continue;
		    }
	
		    if (preg_match('/^\s*}/', $line)) { //Completed object End curley bracket must be on it's own line
				switch($objectName) {
					case 'package':
						$this->packageInfo = $tmpobject;
					    break;
					    
					case 'sysmodule':
						$this->sysmodules[] = $tmpobject;
					    break;
					    
					case 'view':
						$this->views[] = $tmpobject;
					    break;
					    
					case 'supportfile':
						$this->supportfiles[] = $tmpobject['file'];
						break;
				}		
				$objectName = '';
				continue;
		    }		    
		    if (preg_match('/\s*(\S+)\s+=\s+([^#]+)/', $line, $regs)) {	    
			    $tmpobject[trim($regs[1])] = trim($regs[2]);	// Make sure we get rid of any weird whitespace chars
				continue;
		    }
		}
	}
	
	public function isInstalled() {
		global $dbConn;
		$error_msg = '';
		$query = "SELECT package_id FROM guava_packages WHERE name = '" . $this->packageInfo['name'] ."'";
		$result = $dbConn->Execute($query);
		if($result->EOF) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public function installPackage() {
		global $dbConn;
		$packageObject = $this;
		//Add package includes  to Runtime.inc.php file	   
		// Open runtime file for READING
		$includeArray = @file(GUAVA_FS_ROOT . 'includes/runtime.inc.php');
		if($includeArray === FALSE) {
			// Couldn't read in the file for some reason
			throw new Exception("Unable to read File: includes/runtime.inc.php. Please check this file's permissions.");
			return false;
		}
		// Open runtime file for WRITING
		$includePointer = @fopen(GUAVA_FS_ROOT . 'includes/runtime.inc.php', "w");
		if($includePointer === false) {
			throw new Exception("Unable to write to File: includes/runtime.inc.php. Please make sure this file is writeable.");
			return false;
		}
		// Let's start copying the file over, until we hit the end.file comment
		for($counter = 0; $counter < count($includeArray); $counter++) {
			if(strpos($includeArray[$counter], 'end.file') !== false) {
				$found = 1;
				break;
			}
			fwrite($includePointer, $includeArray[$counter]);
		}
		if(!$found) { 
			throw new Exception("The runtime.inc.php file has been corrupted.");
			return false;
		}
		$packageInfo = $packageObject->getInfo();
		// We're at the end.file
		fprintf($includePointer, "// start.%s\n", $packageInfo['shortname']);
		$sysmodules = $packageObject->getSysModules();
		$views = $packageObject->getViews();
		$files = $packageObject->getSupportFiles();
		$basepath = str_replace(GUAVA_FS_ROOT, '', $packageObject->getBasePath());
		$query = "INSERT INTO guava_packages(name, version_major, version_minor, configclassname) VALUES('" . $packageInfo['name'] ."', '".$packageInfo['version_major']."','".$packageInfo['version_minor']."', '".$packageInfo['configclassname']."')";
		try{
		$result = $dbConn->Execute($query);
		}
		catch(ADODB_Exception $ae){
			return false;
		}
		$packageID = $dbConn->Insert_ID();
		
		if(count($files)) {
			foreach($files as $file) {
				// Let's add to the include file
				fprintf($includePointer, "require_once(GUAVA_FS_ROOT . '%s');\n", $basepath . $file);
			}
		}
		
		if(count($sysmodules)) {
			foreach($sysmodules as $sysmodule) {
				fprintf($includePointer, "require_once(GUAVA_FS_ROOT . '%s');\n", $basepath . $sysmodule['file']);
				$query = "INSERT INTO guava_sysmodules(modname) VALUES('" . $sysmodule['modname'] . "')";
				$result = $dbConn->Execute($query);
			}
		}
		if(count($views)) {
			foreach($views as $view) {
				fprintf($includePointer, "require_once(GUAVA_FS_ROOT . '%s');\n", $basepath . $view['file']);
				if($view['icon']) {
					$query = "INSERT INTO guava_views(viewname, viewclass,viewdescription, viewicon) VALUES('" . $view['name'] . "','".$view['classname'] . "','" . $view['description'] ."', '" . $basepath . $view['icon'] . "')";						
				}
				else {
					$query = "INSERT INTO guava_views(viewname, viewclass,viewdescription) VALUES('" . $view['name'] . "','".$view['classname'] . "','" . $view['description'] ."')";	
				}
				$result = $dbConn->Execute($query);
			}			
		}
		// We've written our stuff to the files.  Let's close it off
		fprintf($includePointer, "// end.%s\n", $packageInfo['shortname']);
		fprintf($includePointer, "// end.file\n");
		fprintf($includePointer, "?>\n");
		fclose($includePointer);
		return true;	
	}
	
	public function getInfo() {
		return $this->packageInfo;
	}
	
	public function getBasePath() {
		return $this->basepath;
	}
	
	public function getSysModules() {
		return $this->sysmodules;
	}
	
	public function getSupportFiles() {
		return $this->supportfiles;
	}
	
	public function getViews() {
		return $this->views;
	}
	
	public static function removeSysModule($sysmodule) {
		global $dbConn;
		$query = "DELETE FROM guava_sysmodules WHERE modname = '".$sysmodule . "'";
		$result = $dbConn->Execute($query);	
		return true;
	}
	
	public static function removeView($viewName) {
		global $dbConn;
		$query = "SELECT view_id FROM guava_views WHERE viewclass = '" . $viewName ."'";
		$result = $dbConn->Execute($query);
		$viewID = $result->fields['view_id'];
		$query = "DELETE FROM guava_roleviews WHERE view_id = '" . $viewID . "'";
		$result = $dbConn->Execute($query);
		$query = "DELETE FROM guava_views WHERE view_id = '" . $viewID . "'";
		$result = $dbConn->Execute($query);
		return true;
	}
	
	public static function removePackage($packageName) {
		global $dbConn;
		$query = "DELETE FROM guava_packages WHERE name = '$packageName'";
		$result = $dbConn->Execute($query);
		return true;
	}
	
	public static function removeFileReference($shortname) {
		$includeArray = @file(GUAVA_FS_ROOT . 'includes/runtime.inc.php');
		if($includeArray === FALSE) {
			// Couldn't read in the file for some reason
			return false;
		}
		// Okay, we read the file, let's start creating a new one
		
		$includePointer = @fopen(GUAVA_FS_ROOT . 'includes/runtime.inc.php', "w");
		if($includePointer === false) {
			return false;
		}
		// Let's start copying the file over, until we hit the end.file comment
		for($counter = 0; $counter < count($includeArray); $counter++) {
			if(preg_match('/start.' . $shortname . '$/', $includeArray[$counter]) !== 0) {
				$found = 1;
				break;
			}
			fwrite($includePointer, $includeArray[$counter]);
		}
		for(; $counter < count($includeArray); $counter++) {
			if(preg_match('/end.' . $shortname . '$/', $includeArray[$counter]) !== 0) {
				$found = 1;
				$counter++;
				break;
			}			
		}
		for(; $counter < count($includeArray); $counter++) {
			fwrite($includePointer, $includeArray[$counter]);	
		}
		fclose($includePointer);
		return true;
	}
}

class GuavaTheme {
	
	private $basepath;
	
	private $properties;
	
	private $modules;
	
	private $errormsg;
	
	public function __construct($filename) {
		$this->basepath = dirname($filename) . "/";
		$this->basepath = str_replace(GUAVA_FS_ROOT, '', $this->basepath);
		$this->modules = null;
		
		$tempDocument = new DOMDocument();
		
		if(!@$tempDocument->load($filename)) {
		    $this->errormsg = "Failed to read theme file:" . $filename;
		}

		// Let's read the stuff.
		
		$propNodes = $tempDocument->getElementsByTagName("property");
		
		$numOfProperties = $propNodes->length;
		
		for($counter = 0; $counter < $numOfProperties; $counter++) {
			$this->properties[$propNodes->item($counter)->getAttribute('name')] = $propNodes->item($counter)->firstChild->nodeValue;
		}
		
		$moduleNodes = $tempDocument->getElementsByTagName("module");
		
		$numOfModules = $moduleNodes->length;
		
		for($counter = 0; $counter < $numOfModules; $counter++) {
			$this->modules[$moduleNodes->item($counter)->getAttribute('name')] = $this->basepath . $moduleNodes->item($counter)->getAttribute('file');
		}
		
	}
	
	public function getThemeID() {
		global $dbConn;
		$query = "SELECT id FROM guava_themes WHERE title = '" . $this->properties['title'] . "'";
		$result = $dbConn->Execute($query);
		if($result->EOF) {
			return false;
		}
		else {
			return $result->fields['id'];
		}
	}
	
	public function isInstalled() {
		global $dbConn;
		$query = "SELECT id FROM guava_themes WHERE title = '" . $this->properties['title'] . "'";
		$result = $dbConn->Execute($query);
		if($result->EOF) {
			return false;
		}
		else {
			return true;
		}
	}
	
	function installTheme() {
		global $dbConn;
		if($this->isInstalled()) {
			return false;
		}
		$themeObject = $this;
		$themeInfo = $themeObject->getInfo();
		$themeModules = $themeObject->getModules();
		
		$query = "INSERT INTO guava_themes(title, version, author, email) VALUES('" . $themeInfo['title'] . "','" . $themeInfo['version'] . "','" . $themeInfo['author'] . "','" . $themeInfo['email'] . "')";
		$result = $dbConn->Execute($query);
		
		$themeID = $dbConn->Insert_ID();
		if(count($themeModules)) {
			foreach($themeModules as $name => $file) {
				$query = "INSERT INTO guava_theme_modules(theme_id, name, file) VALUES('" . $themeID . "','" . $name . "','" . $file . "')";
				$result = $dbConn->Execute($query);
			}
		}
		return true;		
	}
	
	public static function removeTheme($name) {
		global $dbConn;
		$query = "SELECT id FROM guava_themes WHERE title = '" . $name . "'";
		$result = $dbConn->Execute($query);
		if($result->EOF) {
			return true;
		}
		else {
			$id = $result->fields['id'];
			$query = "DELETE FROM guava_theme_modules WHERE theme_id = '$id'";
			$dbConn->Execute($query);
			$query = "DELETE FROM guava_themes WHERE id = '$id'";
			$dbConn->Execute($query);
			return true;
		}
	}
	
	public function getInfo() {
		return $this->properties;
	}
	

	public function getModules() {
		return $this->modules;
	}
	
}

function setpreference($packagename, $prefname, $value) {
	global $dbConn;
	// First check to see if preference exists
	$query = "SELECT preference_id FROM guava_preferences WHERE packagename='$packagename' AND prefname = '$prefname'";
	$result = $dbConn->Execute($query);
	if(!$result->EOF) {
		// We have a returned preference!
		// So we must update
		$query = "UPDATE guava_preferences SET value = '$value' WHERE packagename = '$packagename' and prefname ='$prefname'";
		$result = $dbConn->Execute($query);	
		if(!$result) {
			return true;
		}
		else {
			return false;
		}
	}
	// If we got here, there is no such preference, so set it now.
	$query = "INSERT INTO guava_preferences(packagename, prefname, value) VALUES('$packagename', '$prefname', '$value')";
	$result = $dbConn->Execute($query);
	if($result)
		return true;
	else
		return false;
}

function getpreference($packagename, $prefname) {
	global $dbConn;
	$query = "SELECT * from guava_preferences WHERE packagename='$packagename' AND prefname = '$prefname'";
	$result = $dbConn->Execute($query);
	if(!$result->EOF) {
		return $result->fields['value'];
	}
	else {
		return null;
	}
}

function getRoleID($rolename) {
	global $dbConn;
	$roleList = array();
	$query = "SELECT * FROM guava_roles";
	$result = $dbConn->Execute($query);
	while(!$result->EOF) {
		if($result->fields['name'] == $rolename) {
			return $result->fields['role_id'];
		}
		$result->MoveNext();
	}
	return false;
}

function attachView($role_id, $viewClass) {
	global $dbConn;

		// TODO: Most of this convoluted stuff should be handled in one database roundtrip 
		// with uniqueness enforced by a compound primary key in MySQL.

	//print("attachView($role_id , $viewClass)");
		
		// First obtain view class
		$query = "SELECT view_id FROM guava_views WHERE viewclass = '$viewClass'";
		$result = $dbConn->Execute($query);
		if($result == false){
			$errorMsg = $dbConn->ErrorMsg();
			throw new Exception($errorMsg);
		}
		if($result->EOF) {
		    return false;
		}
		else {
			$view_id = $result->fields['view_id'];
		}
		// First check to see if this view already belongs to group
		$query = "SELECT role_id FROM guava_roleviews WHERE role_id = '$role_id' AND view_id = '$view_id'";
		$result = $dbConn->Execute($query);
		if($result == false){
			$errorMsg = $dbConn->ErrorMsg();
			throw new Exception($errorMsg);
		}
		
		if(!$result->EOF) {
			return false;
		}
		else {
			$query = "SELECT COUNT(*) AS count FROM guava_roleviews WHERE role_id = '$role_id'";
			$result = $dbConn->Execute($query);
			if($result == false){
			$errorMsg = $dbConn->ErrorMsg();
			throw new Exception($errorMsg);
			}
			$query = "INSERT into guava_roleviews(role_id, view_id, vieworder) VALUES('$role_id', '$view_id', '".($result->fields['count'] + 1) . "')";
			$result = $dbConn->Execute($query);
			
			if($result == false){
			$errorMsg = $dbConn->ErrorMsg();
			throw new Exception($errorMsg);
			}
			
			return true;
		}
}

function dbquery($query) {
	global $dbConn;
	$result = $dbConn->Execute($query);
	if($dbConn->ErrorNo()) {
		throw new Exception("Failure executing query: " . $query);
	}
	else {
		return $result;
	}
}

function transformPasswords() {
	global $dbConn;
	$query = "SELECT * FROM guava_users";
	$result = $dbConn->Execute($query);
	while(!$result->EOF) {
		// This will work for 5.0.4, but it won't work beyond that since we might be
		// passing an encrypted password to md5 here!
		$subquery = "UPDATE guava_users SET password = md5('" . $result->fields['password'] . "') WHERE user_id = " . $result->fields['user_id'];
		$dbConn->Execute($subquery);
		$result->MoveNext();
	}
}

function addCanExecute(){
	global $dbConn;
	$query = "DESC guava_roles";
	$result = $dbConn->Execute($query);
 	$fields = array();
    while(!$result->EOF) {
        $fields[] = $result->fields[0];
		$result->MoveNext();
	}
	if (!in_array('canExecute', $fields)){
		$query = "ALTER TABLE `guava_roles` ADD  `canExecute` int(11) default 1";
		    if(!$result = $dbConn->Execute($query)){
	                $errorMsg = "Unable to add canExecute field with query: ".$query;
	                throw new Exception($errorMsg);
	        }
		
	}
}

function updateGuavaUsers(){
	global $dbConn;
	
	//Add default_role_id if necessary
	$query = "DESC guava_users";
	$result = $dbConn->Execute($query);
 	$fields = array();
    while(!$result->EOF) {
        $fields[] = $result->fields[0];
		$result->MoveNext();
	}
	if (!in_array('default_role_id', $fields)){
		$query = "ALTER TABLE `guava_users` ADD  `default_role_id` int(11) unsigned NULL";
		    if(!$result = $dbConn->Execute($query)){
	                $errorMsg = "Unable to add default_role_id field with query: ".$query;
	                throw new Exception($errorMsg);
	        }
	}
	
	// Update users - set default role, giving preference to roles with exec perms
	$query =  "SELECT 	 user_id, " .
						"role_id " .
			  "FROM	(SELECT	gra.user_id, " . 
								"	gra.role_id " .  
							 "FROM 	guava_role_assignments gra," . 
								"	guava_roles gr " .
							"WHERE 	gra.role_id=gr.role_id " . 
							"ORDER BY user_id, " .
									"canExecute DESC) a " . 
			"GROUP BY a.user_id"; 
	$results = $dbConn->Execute($query);
	while(!$results->EOF){
		
		$userQuery = " UPDATE guava_users " .
					 " SET default_role_id=" . $results->fields['role_id'] .
					 " WHERE user_id=" . $results->fields['user_id'];
		$dbConn->Execute($userQuery);
		$results->MoveNext(); 
	}			
	
	
}

function cleanupGuavaRoleAssignments(){
	global $dbConn;
	$query = "DESC guava_role_assignments";
	$result = $dbConn->Execute($query);
 	$fields = array();
    while(!$result->EOF) {
        $fields[] = $result->fields[0];
		$result->MoveNext();
	}
	if (in_array('defaultrole', $fields)){
		$query = "ALTER TABLE guava_role_assignments DROP defaultrole";
		$result = $dbConn->Execute($query);
		if($dbConn->ErrorNo()) {
	            throw new Exception("Failure cleaning up guava_role_assignments with query: " . $query);
	    } 
	}
}

function updateGuavaRoles(){
	global $dbConn;
	$query = "UPDATE guava_roles set name='Operators' where name='Operator'";
	$result = $dbConn->Execute($query);
}

//change primary keys
function updateGuavaRoleAssignments(){
	    global $dbConn;

	    $query = "DESC guava_role_assignments";
	    $result = $dbConn->Execute($query);
	    $fields = array();
	    $primary_key_fields = array();
	    while(!$result->EOF) {
	        $fields[] = $result->fields[0];

	        // save list of current primary key fields into an array:
	        if($result->fields[3] == 'PRI')
	        {
	            $primary_key_fields[] = $result->fields[0];
	        }

	        $result->MoveNext();
	    }
	    // make sure list of current primary key fields is sorted alphabetically for comparision later
	    sort($primary_key_fields);


	    // if no primary key exists, OR, if current primary key is not the one we want
	    if(empty($primary_key_fields) || $primary_key_fields != array('role_id','user_id')){
	        // if current primary key is not the one we want
	        if(!empty($primary_key_fields))
	        {
	            // remove current primary key
	            $query = "ALTER TABLE guava_role_assignments DROP PRIMARY KEY;";
	            $result = $dbConn->Execute($query);
	            if($dbConn->ErrorNo()) {
	                throw new Exception("Failure removing existing primary key for table with query: " . $query);
	            }
	        }

	        // create new primary key
	        $query = "ALTER TABLE guava_role_assignments ADD PRIMARY KEY (user_id, role_id);";
	        $result = $dbConn->Execute($query);
	        if($dbConn->ErrorNo()) {
	            throw new Exception("Failure adding primary key for table with query: " . $query);
	        }
	    }
	
		
}

function cleanupRoles(){
	global $dbConn;
	$query = "SELECT role_id FROM guava_roles WHERE name in ('Executives','BasicUsers')";
	$result = mysql_query($query);
	if($result && mysql_num_rows($result) > 0)
	{
		while($row = mysql_fetch_row($result))
		{
			$query = "DELETE FROM guava_roles WHERE role_id = {$row[0]}";
			mysql_query($query);
			$query = "DELETE FROM guava_roleviews WHERE role_id = {$row[0]}";
			mysql_query($query);
			$query = "DELETE FROM guava_role_assignments WHERE role_id = {$row[0]}";
			mysql_query($query);
		}
	}
}

function cleanupUsers(){
	global $dbConn;
	$query = "SELECT user_id FROM guava_users WHERE username in ('exec','basic')";
	$result = mysql_query($query);
	if($result && mysql_num_rows($result) > 0)
	{
		while($row = mysql_fetch_row($result))
		{
			$query = "DELETE FROM guava_users WHERE user_id = {$row[0]}";
			mysql_query($query);
			$query = "DELETE FROM guava_role_assignments WHERE user_id = {$row[0]}";
			mysql_query($query);
			$query = "DELETE FROM guava_group_assignments WHERE user_id = {$row[0]}";
			mysql_query($query);
		}
	}
}

function deleteGuavaRoleAssignments(){
	global $dbConn;
	$query = "DELETE FROM guava_role_assignments";
	$result = $dbConn->Execute($query);
	if($dbConn->ErrorNo()) {
            throw new Exception("Failure removing entries from guava_role_assignments with query: " . $query);
    }
    else {
            // return $true;
    }
	
}

function addDefaultGuavaRoleAssignments($relationships){
	global $dbConn;
	foreach ($relationships as $roleid => $userid){
		if (!existsGuavaRoleRelationship($roleid,$userid)){
			$query = "INSERT INTO guava_role_assignments VALUES ($roleid,$userid)";
			// echo $query;
			$result = $dbConn->Execute($query);
			if($dbConn->ErrorNo()) {
	                throw new Exception("Failure adding guava_role_assignments with query: " . $query);
	        }
	        else {
			//do nothing
			
	        }
		}else{
			//do nothing, relationship already exists
		}
	}
}

function get_user_id($name){
	global $dbConn;
	$query = "SELECT user_id FROM guava.guava_users WHERE username ='$name'";
	if ($result = $dbConn->Execute($query)) {
		$row = $result->FetchRow($result);
		return $row[0];
	}else{
		throw new Exception("Failure select user_id from guava.guava_users with query: " . $query);
	}
}

function get_role_id($name){
	global $dbConn;
	$query = "SELECT role_id FROM guava.guava_roles WHERE name ='$name'";
	// echo $query;
	if ($result = $dbConn->Execute($query)) {
		$row = $result->FetchRow($result);
		return $row[0];
	}else{
		throw new Exception("Failure select role id from guava roles with query: " . $query);
	}
}

function existsGuavaRoleRelationship($roleid,$userid){
	global $dbConn;
	$query = "SELECT * from guava_role_assignments where role_id = $roleid AND user_id = $userid";
	$result = $dbConn->Execute($query);
	if ($result->EOF){
		return false;
	}else{
		return true;
	}
}


function print_usage() {
	global $argv;
?>
Usage:
	<?=$argv[0];?> <database server> <database name> <database user> <database password>
	
Groundwork Monitor Small Business Guava DB Migration Script
		
<?php
}

function database_connect($host, $name, $user, $password) {
	print("Attempting to connect to Guava database...");

	$dsn = 'mysql://' . $user . ":" . $password . "@" . $host . "/" . $name;
	$dbConn = &ADONewConnection($dsn);
	if(!$dbConn) {
		print("FAILED\n");
		return false;
	}
	else {
		print("OK\n");
		return $dbConn;
	}
}

function upgrade45Schema() {
	print("Upgrading 4.5 Schema...\n");
	print("Attempting to modify packages table...");
	dbquery("ALTER TABLE guava_packages ADD UNIQUE(name)");
	dbquery("UPDATE guava_packages SET name = 'Guava Core', configclassname = 'LDAPConfigureView' WHERE name = 'Groundwork Monitor Base';");
	print("OK\n");
	print("Adding necessary Guava Themes tables...");
	dbquery("CREATE TABLE `guava_theme_modules` (
 `id` int(11) unsigned NOT NULL auto_increment,
   `theme_id` int(11) NOT NULL default '0',
   `name` varchar(255) NOT NULL default '',
   `file` varchar(255) NOT NULL default '',
   PRIMARY KEY  (`id`)
 ) ENGINE=MyISAM DEFAULT CHARSET=latin1;");
	dbquery("CREATE TABLE `guava_themes` (
   `id` int(11) unsigned NOT NULL auto_increment,
   `title` varchar(255) NOT NULL default '',
   `version` varchar(255) NOT NULL default '',
   `author` varchar(255) NOT NULL default '',
   `email` varchar(255) NOT NULL default '',
   PRIMARY KEY  (`id`)
 ) ENGINE=MyISAM DEFAULT CHARSET=latin1;");
	print("Attempting to install Groundwork Monitor Small Business Theme...");
	$theme = new GuavaTheme(GUAVA_FS_ROOT . "themes/gwmsb/theme.xml");
	if(!$theme->installTheme()) {
		throw new Exception("Theme already installed.\n");
	}
	print("OK\n");
	$themeID = $theme->getThemeID();
	print("Setting Small Business Theme as default...");
	setpreference("guava", "theme", $themeID);
	print("OK\n");
	// print("Updating Administrators role description...");
	// 	dbquery("UPDATE guava_roles SET description = 'Users who are Administrators of Guava' WHERE name = 'Administrators' AND description = 'Users who are Administrators of the Monitor Product';");
	// 	print("OK\n");
	print("Updating Role Views Table comment...");
	dbquery("ALTER TABLE `guava_roleviews`  COMMENT = 'Guava applications attached to Groups';");
	print("OK\n");
	print("Changing reference from LDAPAuthModule to GuavaLDAPAuthModule...");
	dbquery("UPDATE guava_sysmodules SET modname = 'GuavaLDAPAuthModule' WHERE modname = 'LDAPAuthModule';");
	print("OK\n");
	dbquery("UPDATE guava_users SET authmodule = 'GuavaLDAPAuthModule' WHERE authmodule = 'LDAPAuthModule';");
	print("OK\n");
	print("Adding additional fields to guava_views table...");
	dbquery("ALTER TABLE `guava_views` ADD `viewicon` VARCHAR( 255 ) NULL ;");
	dbquery("ALTER TABLE `guava_views` ADD `viewdescription` VARCHAR (255) NULL;");
	print("OK\n");
	print("Updating Initial Applications to reflect new Class names, descriptions and icons...");
	dbquery("UPDATE guava_views SET viewclass = 'GuavaHomeView', viewicon = 'packages/guava/images/home.gif' WHERE (viewclass = 'MonitorHomeView');");
	dbquery("UPDATE guava_views SET viewclass = 'GuavaAdministrationView', viewdescription = 'Manage Users, Packages and Themes with the Administration Panel', viewicon = 'packages/guava/images/config.gif' WHERE (viewclass = 'MonitorConfigurationView');");
	dbquery("UPDATE guava_views set viewname = 'About GroundWork Monitor...' where viewclass='GuavaHomeView'");
	print("OK\n");

	/**
	 * @todo Verify we need to do this.  Since we're doing db consolidation.  :/
	 */
	//print("Deleting bookshelf tables...");
	dbquery("DROP TABLE `bookshelf_libraries`, `bookshelf_library_categories`, `bookshelf_library_documents`, `bookshelf_library_links`;");
	print("OK\n");
	print("Updating package definitions...");
	dbquery("UPDATE `guava_packages` SET `configclassname` = '' WHERE `name` = 'Groundwork Monarch EZ';");
	dbquery("UPDATE `guava_packages` SET `configclassname` = '' WHERE `name` = 'Groundwork Monarch';");
	dbquery("UPDATE `guava_packages` SET `configclassname` = '' WHERE `name` = 'Nagios';");
	dbquery("UPDATE `guava_packages` SET `configclassname` = '' WHERE `name` = 'Nagios Map';");
	dbquery("UPDATE `guava_packages` SET `configclassname` = '' WHERE `name` = 'Nagios Reports';");
	dbquery("UPDATE `guava_packages` SET `name` = 'Reports' WHERE `name` = 'Groundwork Reports';");
	dbquery("UPDATE `guava_packages` SET `name` = 'Performance Configuration', `configclassname` = '' WHERE `name` = 'Groundwork Performance Configuration';");
	dbquery("UPDATE `guava_packages` SET `name` = 'Monitoring Server', `configclassname` = '' WHERE `name` = 'Groundwork Monitoring Server';");
	dbquery("UPDATE `guava_packages` SET `name` = 'Performance', `configclassname` = '' WHERE `name` = 'Groundwork Performance';");
	print("OK\n");
	print("Deleting unneeded preferences...");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'profiles';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'nagiosmap';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'monarch';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'nagiosmap';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'reports';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'nagiosreports';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'performance';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'nagios';");
	dbquery("DELETE FROM guava_preferences WHERE packagename = 'bookshelf';");
	print("OK\n");
	print("Updating Foundation preferences...");
	dbquery("UPDATE guava_preferences SET value = 'localhost' WHERE packagename = 'foundation' AND prefname = 'feederurl';");
	dbquery("INSERT INTO guava_preferences(packagename, prefname, value) VALUES('foundation', 'webserviceurl', 'http://localhost:8080/foundation-webapp/services');");
	dbquery("INSERT INTO guava_preferences(packagename, prefname, value) VALUES('foundation', 'webservicename', 'foundation-webapp/services');");
	print("OK\n");
	print("Changing information for available applications...");
	dbquery("UPDATE guava_views SET viewclass = 'configurationezView', viewdescription = 'Configuration Made Simple', viewicon = 'packages/ezmonarch/images/configuration.gif' WHERE viewname = 'Configuration EZ';");
	dbquery("UPDATE guava_views SET viewclass = 'configurationView', viewdescription = 'Utility to manage Nagios configuration files', viewicon = 'packages/monarch/images/configuration.gif' WHERE viewname = 'Configuration';");
	dbquery("UPDATE guava_views SET viewclass = 'nagiosView', viewdescription = 'Nagios Interface', viewicon = 'packages/nagios/images/nagios.gif' WHERE viewname = 'Nagios';");
	dbquery("UPDATE guava_views SET viewname = 'Nagios Map', viewclass = 'nagiosmapView', viewdescription = 'Nagios Map Pages', viewicon = 'packages/nagiosmap/images/nagios.gif' WHERE viewname = 'Map';");
	dbquery("UPDATE guava_views SET viewclass = 'nagiosreportsView', viewdescription = 'Nagios Reports Pages', viewicon = 'packages/nagiosreports/images/nagios.gif' WHERE viewname = 'Nagios Reports';");
	dbquery("UPDATE guava_views SET viewdescription = 'Browse or Search the Knowledge Base.', viewicon = 'packages/bookshelf/images/bookshelf.gif' WHERE viewname = 'Bookshelf';");
	dbquery("UPDATE guava_views SET viewdescription = 'View network data', viewicon = 'packages/sv/images/status_viewer.gif' WHERE viewname = 'Status';");
	dbquery("UPDATE guava_views SET viewdescription = 'GroundWork, Insight, and Nagios Reports', viewclass = 'reportsView', viewicon = 'packages/reports/images/reports.gif' WHERE viewname = 'Reports';");
	dbquery("UPDATE guava_views SET viewdescription = 'Utility to show and configuration Performance Graphs', viewclass = 'performanceconfigurationView', viewicon = 'packages/performanceconfiguration/images/performance.gif' WHERE viewname = 'Performance Configuration';");
	dbquery("UPDATE guava_views SET viewdescription = 'Groundwork Monitoring Server Status Page', viewclass = 'monitoringserverView', viewicon = 'packages/monitoringserver/images/nagios_host.gif' WHERE viewname = 'Monitoring Server';");
	dbquery("UPDATE guava_views SET viewdescription = 'Utility to select and display data in RRD files as graphs', viewclass = 'performanceView', viewicon = 'packages/performance/images/performance.gif' WHERE viewname = 'Performance';");
	dbquery("DELETE FROM guava_views WHERE viewclass = 'FoundationCategoryView';");
	print("OK\n");
	print("Changing user passwords to md5 hashes...");
	transformPasswords();
	print("OK\n");
	return true;

	
	
	
}
 

function migrate() {
	global $dbConn;
	// First see if we have schema info
	$result = dbquery("show tables like '%guava_schema%';");
	
	if(!$result->EOF && $result->fields['version'] >= SCHEMA_VERSION) {
		// Then do nothing.
	}
	else {
		if($result->EOF) {
			//WE DON'T HAVE SCHEMA
			dbquery("CREATE TABLE `guava_schema` (
			   `product` varchar(255) NOT NULL default '',
			   `version` varchar(255) NOT NULL default ''
			 ) ENGINE=MyISAM DEFAULT CHARSET=latin1;");
			dbquery("INSERT INTO guava_schema VALUES('" . SCHEMA_PRODUCT . "', '" . SCHEMA_VERSION . "')");
		}
 	
 		print ("Updating guava role Operator to Operators\n");	 
		updateGuavaRoles();
		
		// CHECK FOR EXISTENCE OF THEMES DB, IF WE DON'T HAVE THEMES, WE'RE A 4.5 SYSTEM
		$result = dbquery("SHOW TABLES LIKE 'guava_themes'");
		if($result->EOF) {
			upgrade45Schema();
		}
		$result = dbquery("SELECT password FROM guava_users");
		$transformed = true;	// Assume true for now
		while(!$result->EOF) {
			if(strlen($result->fields['password']) != 32) {
				$transformed = false;
			}
			$result->MoveNext();
		}
		if(!$transformed) {
			print("Transforming passwords...");
			transformPasswords();
			print("DONE\n");
		}
		GuavaPackage::removePackage("Configuration EZ");
		//**Reports Consolidation**
 	
		print("Consolidating Performance Graphs...\n");
		GuavaPackage::removeView("performanceconfigurationView");
		
		//Add Performance to Admin View
		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/performance/package.pkg");
		if(!$package->isInstalled()) {
			$package->installPackage();
			 
		}//endif isInstalled
		
		print("\nAttaching Performance View to Administrators role...");
			$adminRoleID = getRoleID("Administrators");
			if($adminRoleID === false) {
				print("Administrators role not found.  Administrator will need to assign Performance manually to role.\n");
			}//endif
			else {
				if(!attachView($adminRoleID, "performanceView")) {
					print("Unable to attach view Performance.\n");
				}//endif attached
			else{
					print("OK\n");
				}
			}
			
			print("\nAttaching Performance View to Operators role...");
			$adminRoleID = getRoleID("Operators");
			if($adminRoleID === false) {
				print("Operators role not found.  Administrator will need to assign Performance manually to role.\n");
			}//endif
			else {
				if(!attachView($adminRoleID, "performanceView")) {
					print("Unable to attach view Performance.\n");
				}//endif attached
			else{
					print("OK\n");
				}
			}
		
 		print ("Consolidating Reports\n");
 		//Remove Advanced Reporting View
		GuavaPackage::removeView("gwreportserverView");
		//Remove Nagios Reports View
		GuavaPackage::removeView("nagiosreportsView");
		//Update Description
		dbquery("UPDATE guava_views SET viewdescription = 'Insight and Nagios Reports' WHERE viewname = 'Reports';");
	 	
		//Add Reports to Admin View
		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/reports/package.pkg");
		if(!$package->isInstalled()) {
			$package->installPackage();
		}//endif isInstalled
		
		print("\nAttaching View Reports to Administrators role...");
			$adminRoleID = getRoleID("Administrators");
			if($adminRoleID === false) {
				print("Administrators role not found.  Administrator will need to assign Rerports manually to role.\n");
			}//endif
			else {
				if(!attachView($adminRoleID, "reportsView")) {
					print("Unable to attach view Reports.\n");
				}
			else{
					print("OK\n");
				}
				//endif attached
			}//endelse
		
		
		

		print ("Updating for Role Execute Permissions\n");
		// dbquery("ALTER TABLE guava_roles ADD canExecute int(11) default 1");
		addCanExecute();
		
		
		//Remove EZMonarch
		print ("Removing Groundwork Monarch EZ Package\n");
		GuavaPackage::removePackage("Groundwork Monarch EZ");
		GuavaPackage::removeView("configurationezView");
		
		GuavaPackage::removeFileReference("gwbookshelf");
		GuavaPackage::removePackage("Groundwork Bookshelf");
		
		//Remove Bookshelf - Gone from Open Source 
		GuavaPackage::removePackage("Bookshelf");
		GuavaPackage::removeView("bookshelfView");
		GuavaPackage::removeFileReference("bookshelf");
		
		//Remove Old Bookshelf - mnogosearch version
		GuavaPackage::removeView("BookshelfBookshelfView");
		GuavaPackage::removeSysModule("BookshelfsystemModule");

		// Okay, time to remove PUC and sv2
		GuavaPackage::removeFileReference("gwpuc");
		GuavaPackage::removePackage("PHP Utility Classes");
		
		// Perfgraphs are now installed into sv2 internally.
		GuavaPackage::removeFileReference("svperfgraphs");
		GuavaPackage::removePackage("Status Viewer Performance Graphs");
		GuavaPackage::removeSysModule("SVPerfGraphsSystemModule");
 
		GuavaPackage::removeFileReference("gwstatusviewer"); 
		GuavaPackage::removeSysModule("SVSystemModule");
		GuavaPackage::removeView("SVStatusView");
		GuavaPackage::removePackage("Groundwork Status Viewer");
		
			
		GuavaPackage::removePackage("Groundwork Console");
		GuavaPackage::removeFileReference("gwconsole");
		GuavaPackage::removeSysModule("ConsoleSystemModule");
		GuavaPackage::removeView("ConsoleApplication");
		GuavaPackage::removeView("ConsoleMainView");
		
		
		GuavaTheme::removeTheme("Groundwork Monitor Small Business");
		GuavaTheme::removeTheme("Groundwork Monitor Small Business Classic");
		
		// For professional upgrades only
		GuavaTheme::removeTheme("Groundwork Monitor Professional Classic");

		setpreference("dashboards", "username", "guava");
		setpreference("dashboards", "dbname", "guava");
		
		/*
		setpreference("mnogosearch", "username", "guava");
		setpreference("mnogosearch", "dbname", "guava");
		*/
		
		/*
		setpreference("foundation", "port", "8080");
		setpreference("foundation", "feederurl", "localhost");
		setpreference("foundation", "webserviceurl", "http://localhost:8080/foundation-webapp/services");
		setpreference("foundation", "webservicename", "foundation-webapp/services");
		*/
		
		$webserviceurl = getpreference("foundation", "webserviceurl");
		
		
		preg_match("/(.*):(\d*)\/(.*)/", $webserviceurl, $matches);
		
		if($matches[2] == "80") {
			$newurl = $matches[1] . ":8080/" . $matches[3];
			setpreference("foundation", "webserviceurl", $newurl);
		}
		
		
		
		
		
		// set new sv2 preferences
		setpreference("sv2", "comment_file", "/usr/local/groundwork/nagios/var/nagioscomment.log");
		setpreference("sv2", "downtime_file", "/usr/local/groundwork/nagios/var/nagiosdowntime.log");
		setpreference("sv2", "command_file", "/usr/local/groundwork/nagios/var/spool/nagios.cmd");
		
		setpreference("sv2", "address", "localhost");
		setpreference("sv2", "username", "monarch");
		setpreference("sv2", "password", "gwrk");
		setpreference("sv2", "dbname", "monarch");
		setpreference("sv2", "rrdtoolpath", "/usr/local/groundwork/bin/rrdtool");
		
		
		
		$theme = new GuavaTheme(GUAVA_FS_ROOT . "themes/gwmos/theme.xml");
		if(!$theme->installTheme()) {
			print("OS Theme already installed.\n");
		}
		print("OK\n");
		
		print("\n");
		print("Attempting to install Theme for 5.3");
		$theme = new GuavaTheme(GUAVA_FS_ROOT . "themes/gwmos53/theme.xml");
		if(!$theme->installTheme()) {
			print("OS 5.3 Theme already installed.\n");
		}
		print("OK\n");
		
		$themeID = $theme->getThemeID();
		print("Setting OS 5.3 Theme as default...");
		setpreference("guava", "theme", $themeID);
		print("OK\n");

		// Install AutoDiscovery
		print ("\nInstalling AutoDiscovery...");
		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/autodiscovery/package.pkg");
		if(!$package->isInstalled()) {
			$package->installPackage();
		}//endif AutoDiscovery isInstalled
		
		// Attach AutoDiscovery to Admin Role
			print("\nAttaching View AutoDiscovery to Administrators role...");
			$adminRoleID = getRoleID("Administrators");
			if($adminRoleID === false) {
				print("Administrators role not found.  Administrator will need to assign AutoDiscovery manually to role.\n");
			}//endif
			else {
				if(!attachView($adminRoleID, "AutoDiscoveryView")) {
					print("Unable to attach view AutoDiscovery.\n");
				}//endif attached
				else{
					print("OK\n");
				}
			}//endelse
			
		
		//Install Tools Package
	 	print ("\nInstalling Tools...");
		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/tools/package.pkg");
		if(!$package->isInstalled()) {
			$package->installPackage();
		}//endif Tools isInstalled
			
		//Add Tools View to Admin and Operator
			print("\nAttaching View Tools to Administrators role...");
			$adminRoleID = getRoleID("Administrators");	
			if($adminRoleID === false) {
				print("Administrators role not found.  Administrator will need to assign Tools manually to role.\n");
			}//endif
			else {
				if(!attachView($adminRoleID, "toolsView")) {
					print("Unable to attach view Tools.\n");
				}
				else{
					print("OK\n");//endif attached
				}
			}//endelse
			
			print("\nAttaching View Tools to Operators role...");
			$opRoleID = getRoleID("Operators");
			if($opRoleID === false) {
				print("Operators role not found.  Administrator will need to assign Tools manually to role.\n");
			}//endif
			else {
				if(!attachView($opRoleID, "toolsView")) {
					print("Unable to attach view Tools.\n");
				}//endif attached
				else{
					print("OK\n");
				}
			}//endelse

		print("Consolidating Tools...");
		GuavaPackage::removeView("profiletoolsView");
		GuavaPackage::removeFileReference("profiletools");
		
		//Update Tools description
		dbquery("UPDATE guava_views SET   viewdescription = 'Monitoring Server Tools'   WHERE viewname = 'Tools';");
		
		//Update Configuration description
		dbquery("UPDATE guava_views SET  viewdescription = 'Manage Nagios Configuration'   WHERE viewname = 'Configuration';");
		
		
	//	GuavaPackage::removeView("nagiosView");
		GuavaPackage::removeView("GuavaWrappitView");
		
	 	print("Consolidating SV...");
	 	GuavaPackage::removeView("nagiosmapView");
	 	GuavaPackage::removeView("monitoringserverView");

		// Time to install SV2
		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/sv2/package.pkg");
		if(!$package->isInstalled()) {
			$package->installPackage();
				
		}		
				print("Attaching View Status to Operator role...");
			$opRoleID = getRoleID("Operators");
			if($opRoleID === false) {
				// Try again, but this time with Operators
				$opRoleID = getRoleID("Operators");	
			}
			if($opRoleID === false) {
				print("Operator role not found.  Administrator will need to assign Status manually to role.\n");
			}
			else {
				if(!attachView($opRoleID, "SV2Application")) {
					print("Unable to attach view Status.\n");
				}
				else{
					print("OK\n");
				}
		
				}
				
				
			
			print("\nAttaching View Status to Administrators role...");
			$adminRoleID = getRoleID("Administrators");	
			if($adminRoleID === false) {
				print("Administrators role not found.  Administrator will need to assign Status manually to role.\n");
			}//endif
			else {
				if(!attachView($adminRoleID, "SV2Application")) {
					print("Unable to attach view Status.\n");
				}
				else{
					print("OK\n");//endif attached
				}
			}

 
 
	 
		print("\n");
		print('Nagios comments are now in status.log update Status Viewer preference');
		setpreference("sv2", "comment_file", "/usr/local/groundwork/nagios/var/status.log");
		print("\n"); 
		
		print("\n");
		print("Attempting to clean up Guava Views");
		dbquery("UPDATE guava_views set viewname = 'About GroundWork Monitor...' where viewclass='GuavaHomeView'");
		print("\n");
		
		print("\n");
		print("Attempting to clean up Guava Users");
		cleanupUsers();
		print("\n");
		
		print("\n");
		print("Attempting to update Guava Users Table");
		updateGuavaUsers();
		print("\n");

		print("\n");
		print("Attempting to clean up Guava Roles");
		cleanupRoles();
		print("\n");

		print("\n");
		print("Attempting to cleanup Guava_Role_Assignments Table");
		cleanupGuavaRoleAssignments();
		print("\n");

		print("\n");
		print("Attempting to update Guava_Role_Assignments Table");
		updateGuavaRoleAssignments();
		print("\n");
		
		// print("\n");
		// print("Attempting to remove all entries from Guava_Role_Assignments Table");	
		// deleteGuavaRoleAssignments();	
		// print("\n");
		
		print("\n");
		print("Attempting to Update Guava Users Table");
		updateGuavaUsers();
		print("\n");
		
		print("Attempting to add default role assignments \n");
		try{
		$admin_id = get_user_id('admin');
		$op_id = get_user_id('joe');

		$admin_role_id = get_role_id('Administrators');
		$op_role_id = get_role_id('Operators');
		$roleassignments = null;
		if($admin_id && $admin_role_id && $op_id && $op_role_id){
			$roleassignments = array($admin_role_id=>$admin_id,$op_role_id=>$op_id);
			}
		else if($admin_id && $admin_role_id){
			$roleassignments = array($admin_role_id=>$admin_id);
			}
		else if($op_id && $op_role_id){
			$roleassignments = array($op_role_id=>$op_id);
			}
		if($roleassignments){
			addDefaultGuavaRoleAssignments($roleassignments);			
			}
		}
		catch (Exception $e){
			print("Problem adding default guava role assignments: " . $e->getMessage() . "\n");
			return 0;
		}
		

		return true;
	}
	return true;
	
}

// Check for number of arguments
if($argc < 5) {
	print_usage();
	return -1;
}
// If we got here, we have the right number of parameters
if(!$dbConn = database_connect($argv[1], $argv[2], $argv[3], $argv[4])) {
	return -1;
}
else {
	if(!migrate()) {
		print("There was a failure migrating the Guava system.  Please contact support...\n\n");
	}
	else {
		print("Migration Successful.\n\n");
	}
}

?>
