<?php
 

/* HELPER METHODS */
// Database Abstraction Library (ADOdb)
require_once('/usr/local/groundwork/core/guava/htdocs/guava/adodb/adodb.inc.php');
require_once('/usr/local/groundwork/core/guava/htdocs/guava/adodb/adodb-exceptions.inc.php');

define(GUAVA_FS_ROOT, "/usr/local/groundwork/core/guava/htdocs/guava/");

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
	
	public static function removePackage($packageName,$host) {
		global $dbConn;
		print "Removing GWM Package $packageName from $host database...";
		$query = "";
		
		if($packageName == "svcactigraph"){
			$query = "DELETE FROM guava_packages WHERE name='Status Viewer Cacti Graphs'";	
		}
		else{ 
			$query = "DELETE FROM guava_packages WHERE name like '$packageName%$host%'";
		}
		$result = $dbConn->Execute($query);
		print "OK\n";
		$removeName = "";
		
		
		print "Removing GWM Package $packageName from $host filesystem...";
		if($packageName == "weathermap"){ 
			$packageName = "weathermap-editor"; 
			$removeName = "weathermap_editor_" . $host;
			}
		
		else if($packageName == "svcactigraph"){
			$removeName = $packageName;
			deleteFiles("/usr/local/groundwork/core/guava/htdocs/guava/packages/" . $packageName . "/");
			print "OK\n";

		    $removeName = $removeName . "s";	
			print "Removing File Reference for $removeName...";

			GuavaPackage::removeFileReference($removeName);
			print "OK\n";
			
			print "Removing SVCactiGraphs System Module...";
			GuavaPackage::removeSysModule("SVCactiGraphsSystemModule");
			print "OK\n";

			return true;
		}
		else{
			$removeName = $packageName . "_" . $host;
		}
		deleteFiles("/usr/local/groundwork/core/guava/htdocs/guava/packages/" . $packageName . "_" . $host . "/");
		print "OK\n";

		print "Removing File Reference for $removeName...";

		GuavaPackage::removeFileReference($removeName);
		
		
		print "OK\n";
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



function deleteFiles($path, $match="*", $delSubdirFiles = false){


static $deleted = 0;

$dirs = glob($path."*",GLOB_NOSORT); // GLOB_NOSORT to make it quicker

$files = glob($path.$match, GLOB_NOSORT);

foreach ($files as $file){

	if(is_file($file)){
 
		unlink($file);
 
		$deleted++;
 
	}
 
	}
 
if ($delSubdirFiles) {
 
	foreach ($dirs as $dir){
 
		if (is_dir($dir)){
 
			$dir = basename($dir) . "/";
 
			deleteFiles($path.$dir,$match);
 
			}
 
		}
}
	 
return $deleted;
 
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
 
function remove($component,$component_host) {

		$component = strtolower($component);
		$component_host = $component_host;
 		$component_viewname = "";
		//Add Performance to Admin View
		if($component == "weathermap"){
			$component_viewname = "weathermapeditorView" . $component_host;
			$pkgdir = 	"weathermap-editor" . "_" . $component_host;
	
		}
		else if($component == "svcactigraph"){
			$pkgdir = $component;
			$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/" . $pkgdir . "/package.pkg");
			if($package->isInstalled()) {
				$package->removePackage($component,$component_host);
			}//endif isInstalled
			return 1;

		}
		else{
			$pkgdir = 	$component . "_" . $component_host;
			$component_viewname = $component . "View" . $component_host;
		}	
		
		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/" . $pkgdir . "/package.pkg");
		if($package->isInstalled()) {
			$package->removePackage($component,$component_host);
		}//endif isInstalled
		
		print("Removing $component View from Administrators role...");
			$adminRoleID = getRoleID("Administrators");
			if($adminRoleID === false) {
				print("Administrators role not found.  Administrator will need to assign Performance manually to role.\n");
			}//endif
			else {
				if(!GuavaPackage::removeView($component_viewname)) {
					print("Unable to remove $component view. May already have been removed.\n");
					}//endif attached
				else{
					print("OK\n");
					}
			}
			return 1;
} 

function install($component,$component_host) {

		$component = strtolower($component);
		$component_host = $component_host;
 		$component_viewname = "";
		//Add Performance to Admin View
		if($component == "weathermap"){
			$component_viewname = "weathermapeditorView" . $component_host;
			$pkgdir = 	"weathermap-editor" . "_" . $component_host;
	
		}
		else if($component == "svcactigraph"){

			$pkgdir = $component;
			$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/" . $pkgdir . "/package.pkg");

			if(!$package->isInstalled()) {
				$package->installPackage();
			}//endif isInstalled
			return 1;

		}
		else{
			$pkgdir = 	$component . "_" . $component_host;
			$component_viewname = $component . "View" . $component_host;
		}	
		
		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/" . $pkgdir . "/package.pkg");
		if(!$package->isInstalled()) {
			$package->installPackage();
		}//endif isInstalled
		
		print("\nAttaching $component View to Administrators role...");
			$adminRoleID = getRoleID("Administrators");
			if($adminRoleID === false) {
				print("Administrators role not found.  Administrator will need to assign Performance manually to role.\n");
			}//endif
			else {
				if(!attachView($adminRoleID, $component_viewname)) {
					print("Unable to attach $component view. May already be attached.\n");
					}//endif attached
				else{
					print("OK\n");
					}
			}
			return 1;
}
 
//***********************************************************************************************
// install_application.php Cacti install
// Check for number of arguments
if($argc < 3) {
	print "ARGS: $argv[1] $argv[2] $argv[3] $argv[4]\n";
	return -1;
}
 

$component = $argv[1]; // Cacti, Weathermap, ntop, or NeDi
$action= $argv[2]; // install  or uninstall
$component_host = $argv[3];
 
print "$action $component on $component_host\n";

//connection parameters for guava database
$host = "localhost";
$database = "guava";
$username = "guava";
$password = "gwrk";
 
if(!$dbConn = database_connect($host,$database,$username,$password)) {
		print("Unable to establish a connection to the GroundWork Monitor guava database.");
		return -1;
}
else {

	if($action == "install"){
		if(!install($component,$component_host)) {
			print("There was a failure installing the $component GroundWork Monitor Application package. You may need to install the package manually.\n\n");
		}
		else {
			print("Installation Successful.\n\n");
		}
	}
	else{
		if(!remove($component,$component_host)) {
			print("There was a failure removing the $component GroundWork Monitor Application package. You may need to remove the package manually.\n\n");
		}
		else {
			print("Removal Successful.\n\n");
		}
	}
} 
?>
