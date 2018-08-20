<?php
 

/* HELPER METHODS */
// Database Abstraction Library (ADOdb)
#1require_once('/usr/local/groundwork/core/guava/htdocs/guava/adodb/adodb.inc.php');
#1require_once('/usr/local/groundwork/core/guava/htdocs/guava/adodb/adodb-exceptions.inc.php');

#1define(GUAVA_FS_ROOT, "/usr/local/groundwork/core/guava/htdocs/guava/");

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
#1		$query = "SELECT package_id FROM guava_packages WHERE name = '" . $this->packageInfo['name'] ."'";
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
#1		$query = "INSERT INTO guava_packages(name, version_major, version_minor, configclassname) VALUES('" . $packageInfo['name'] ."', '".$packageInfo['version_major']."','".$packageInfo['version_minor']."', '".$packageInfo['configclassname']."')";
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
#1				$query = "INSERT INTO guava_sysmodules(modname) VALUES('" . $sysmodule['modname'] . "')";
				$result = $dbConn->Execute($query);
			}
		}
		if(count($views)) {
			foreach($views as $view) {
				fprintf($includePointer, "require_once(GUAVA_FS_ROOT . '%s');\n", $basepath . $view['file']);
				if($view['icon']) {
#1					$query = "INSERT INTO guava_views(viewname, viewclass,viewdescription, viewicon) VALUES('" . $view['name'] . "','".$view['classname'] . "','" . $view['description'] ."', '" . $basepath . $view['icon'] . "')";						
				}
				else {
#1					$query = "INSERT INTO guava_views(viewname, viewclass,viewdescription) VALUES('" . $view['name'] . "','".$view['classname'] . "','" . $view['description'] ."')";	
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
#1		$query = "DELETE FROM guava_sysmodules WHERE modname = '".$sysmodule . "'";
		$result = $dbConn->Execute($query);	
		return true;
	}
	
	public static function removeView($viewName) {
		global $dbConn;
#1		$query = "SELECT view_id FROM guava_views WHERE viewclass = '" . $viewName ."'";
#1		$result = $dbConn->Execute($query);
#1		$viewID = $result->fields['view_id'];
#1		$query = "DELETE FROM guava_roleviews WHERE view_id = '" . $viewID . "'";
#1		$result = $dbConn->Execute($query);
#1		$query = "DELETE FROM guava_views WHERE view_id = '" . $viewID . "'";
#1		$result = $dbConn->Execute($query);
#1		return true;
#1	}
	
	public static function removePackage($packageName,$host) {
		global $dbConn;
		print "Removing GWM Package $packageName from $host database...";
		$query = "";
		
#1		if($packageName == "svcactigraph"){
#1			$query = "DELETE FROM guava_packages WHERE name='Status Viewer Cacti Graphs'";	
#1		}
#1		else{ 
#1			$query = "DELETE FROM guava_packages WHERE name like '$packageName%$host%'";
#1		}
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
#1			deleteFiles("/usr/local/groundwork/core/guava/htdocs/guava/packages/" . $packageName . "/");
#1			print "OK\n";

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
#1		deleteFiles("/usr/local/groundwork/core/guava/htdocs/guava/packages/" . $packageName . "_" . $host . "/");
#1		print "OK\n";

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
#1	$query = "SELECT preference_id FROM guava_preferences WHERE packagename='$packagename' AND prefname = '$prefname'";
#1	$result = $dbConn->Execute($query);
#1	if(!$result->EOF) {
#1		// We have a returned preference!
#1		// So we must update
#1		$query = "UPDATE guava_preferences SET value = '$value' WHERE packagename = '$packagename' and prefname ='$prefname'";
#1		$result = $dbConn->Execute($query);	
#1		if(!$result) {
#1			return true;
#1		}
#1		else {
#1			return false;
#1		}
#1	}
	// If we got here, there is no such preference, so set it now.
#1	$query = "INSERT INTO guava_preferences(packagename, prefname, value) VALUES('$packagename', '$prefname', '$value')";
#1	$result = $dbConn->Execute($query);
#1	if($result)
#1		return true;
#1	else
#1		return false;
}

function getpreference($packagename, $prefname) {
	global $dbConn;
#1	$query = "SELECT * from guava_preferences WHERE packagename='$packagename' AND prefname = '$prefname'";
#1	$result = $dbConn->Execute($query);
#1	if(!$result->EOF) {
#1		return $result->fields['value'];
#1	}
#1	else {
#1		return null;
#1	}
}

function getRoleID($rolename) {
	global $dbConn;
	$roleList = array();
#1	$query = "SELECT * FROM guava_roles";
#1	$result = $dbConn->Execute($query);
#1	while(!$result->EOF) {
#1		if($result->fields['name'] == $rolename) {
#1			return $result->fields['role_id'];
#1		}
#1		$result->MoveNext();
#1	}
#1	return false;
}

#1function attachView($role_id, $viewClass) {
#1	global $dbConn;

		// TODO: Most of this convoluted stuff should be handled in one database roundtrip 
		// with uniqueness enforced by a compound primary key in MySQL.

	//print("attachView($role_id , $viewClass)");
		
		// First obtain view class
#1		$query = "SELECT view_id FROM guava_views WHERE viewclass = '$viewClass'";
#1		$result = $dbConn->Execute($query);
#1		if($result == false){
#1			$errorMsg = $dbConn->ErrorMsg();
#1			throw new Exception($errorMsg);
#1		}
#1		if($result->EOF) {
#1		    return false;
#1		}
#1		else {
#1			$view_id = $result->fields['view_id'];
#1		}
		// First check to see if this view already belongs to group
#1		$query = "SELECT role_id FROM guava_roleviews WHERE role_id = '$role_id' AND view_id = '$view_id'";
#1		$result = $dbConn->Execute($query);
#1		if($result == false){
#1			$errorMsg = $dbConn->ErrorMsg();
#1			throw new Exception($errorMsg);
#1		}
		
#1		if(!$result->EOF) {
#1			return false;
#1		}
#1		else {
#1			$query = "SELECT COUNT(*) AS count FROM guava_roleviews WHERE role_id = '$role_id'";
#1			$result = $dbConn->Execute($query);
#1			if($result == false){
#1			$errorMsg = $dbConn->ErrorMsg();
#1			throw new Exception($errorMsg);
#1			}
#1			$query = "INSERT into guava_roleviews(role_id, view_id, vieworder) VALUES('$role_id', '$view_id', '".($result->fields['count'] + 1) . "')";
#1			$result = $dbConn->Execute($query);
			
#1			if($result == false){
#1			$errorMsg = $dbConn->ErrorMsg();
#1			throw new Exception($errorMsg);
#1			}
			
#1			return true;
#1		}
#1}

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

#1function transformPasswords() {
#1	global $dbConn;
#1	$query = "SELECT * FROM guava_users";
#1	$result = $dbConn->Execute($query);
#1	while(!$result->EOF) {
#1		// This will work for 5.0.4, but it won't work beyond that since we might be
#1		// passing an encrypted password to md5 here!
#1		$subquery = "UPDATE guava_users SET password = md5('" . $result->fields['password'] . "') WHERE user_id = " . $result->fields['user_id'];
#1		$dbConn->Execute($subquery);
#1		$result->MoveNext();
#1	}
#1}

 

#1function updateGuavaUsers(){
#1	global $dbConn;
	
	//Add default_role_id if necessary
#1	$query = "DESC guava_users";
#1	$result = $dbConn->Execute($query);
#1 	$fields = array();
#1    while(!$result->EOF) {
#1        $fields[] = $result->fields[0];
#1		$result->MoveNext();
#1	}
#1	if (!in_array('default_role_id', $fields)){
#1		$query = "ALTER TABLE `guava_users` ADD  `default_role_id` int(11) unsigned NULL";
#1		    if(!$result = $dbConn->Execute($query)){
#1	                $errorMsg = "Unable to add default_role_id field with query: ".$query;
#1	                throw new Exception($errorMsg);
#1	        }
#1	}
	
#1	// Update users - set default role, giving preference to roles with exec perms
#1	$query =  "SELECT 	 user_id, " .
#1						"role_id " .
#1			  "FROM	(SELECT	gra.user_id, " . 
#1								"	gra.role_id " .  
#1							 "FROM 	guava_role_assignments gra," . 
#1								"	guava_roles gr " .
#1							"WHERE 	gra.role_id=gr.role_id " . 
#1							"ORDER BY user_id, " .
#1									"canExecute DESC) a " . 
#1			"GROUP BY a.user_id"; 
#1	$results = $dbConn->Execute($query);
#1	while(!$results->EOF){
#1		
#1		$userQuery = " UPDATE guava_users " .
#1					 " SET default_role_id=" . $results->fields['role_id'] .
#1					 " WHERE user_id=" . $results->fields['user_id'];
#1		$dbConn->Execute($userQuery);
#1		$results->MoveNext(); 
#1	}			
#1	
#1	
#1}

  
 

#1function cleanupUsers(){
#1	global $dbConn;
#1	$query = "SELECT user_id FROM guava_users WHERE username in ('exec','basic')";
#1	$result = mysql_query($query);
#1	if($result && mysql_num_rows($result) > 0)
#1	{
#1		while($row = mysql_fetch_row($result))
#1		{
#1			$query = "DELETE FROM guava_users WHERE user_id = {$row[0]}";
#1			mysql_query($query);
#1			$query = "DELETE FROM guava_role_assignments WHERE user_id = {$row[0]}";
#1			mysql_query($query);
#1			$query = "DELETE FROM guava_group_assignments WHERE user_id = {$row[0]}";
#1			mysql_query($query);
#1		}
#1	}
#1}

#1function deleteGuavaRoleAssignments(){
#1	global $dbConn;
#1	$query = "DELETE FROM guava_role_assignments";
#1	$result = $dbConn->Execute($query);
#1	if($dbConn->ErrorNo()) {
#1            throw new Exception("Failure removing entries from guava_role_assignments with query: " . $query);
#1    }
#1    else {
            // return $true;
#1    }
	
}

#1function addDefaultGuavaRoleAssignments($relationships){
#1	global $dbConn;
#1	foreach ($relationships as $roleid => $userid){
#1		if (!existsGuavaRoleRelationship($roleid,$userid)){
#1			$query = "INSERT INTO guava_role_assignments VALUES ($roleid,$userid)";
#1			// echo $query;
#1			$result = $dbConn->Execute($query);
#1			if($dbConn->ErrorNo()) {
#1	                throw new Exception("Failure adding guava_role_assignments with query: " . $query);
#1	        }
#1	        else {
#1			//do nothing
#1			
#1	        }
#1		}else{
#1			//do nothing, relationship already exists
#1		}
#1	}
#1}

#1function get_user_id($name){
#1	global $dbConn;
#1	$query = "SELECT user_id FROM guava.guava_users WHERE username ='$name'";
#1	if ($result = $dbConn->Execute($query)) {
#1		$row = $result->FetchRow($result);
#1		return $row[0];
#1	}else{
#1		throw new Exception("Failure select user_id from guava.guava_users with query: " . $query);
#1	}
#1}

#1function get_role_id($name){
#1	global $dbConn;
#1	$query = "SELECT role_id FROM guava.guava_roles WHERE name ='$name'";
#1	// echo $query;
#1	if ($result = $dbConn->Execute($query)) {
#1		$row = $result->FetchRow($result);
#1		return $row[0];
#1	}else{
#1		throw new Exception("Failure select role id from guava roles with query: " . $query);
#1	}
#1}

#1function existsGuavaRoleRelationship($roleid,$userid){
#1	global $dbConn;
#1	$query = "SELECT * from guava_role_assignments where role_id = $roleid AND user_id = $userid";
#1	$result = $dbConn->Execute($query);
#1	if ($result->EOF){
#1		return false;
#1	}else{
#1		return true;
#1	}
#1}


 

#1function database_connect($host, $name, $user, $password) {
#1	print("Attempting to connect to Guava database...");

#1	$dsn = 'mysql://' . $user . ":" . $password . "@" . $host . "/" . $name;
#1	$dbConn = &ADONewConnection($dsn);
#1	if(!$dbConn) {
#1		print("FAILED\n");
#1		return false;
#1	}
#1	else {
#1		print("OK\n");
#1		return $dbConn;
#1	}
#1}
#1 
#1function remove($component,$component_host) {

#1		$component = strtolower($component);
#1		$component_host = $component_host;
#1 		$component_viewname = "";
#1		//Add Performance to Admin View
#1		if($component == "weathermap"){
#1			$component_viewname = "weathermapeditorView" . $component_host;
#1			$pkgdir = 	"weathermap-editor" . "_" . $component_host;
#1	
#1		}
#1		else if($component == "svcactigraph"){
#1			$pkgdir = $component;
#1			$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/" . $pkgdir . "/package.pkg");
#1			if($package->isInstalled()) {
#1				$package->removePackage($component,$component_host);
#1			}//endif isInstalled
#1			return 1;
#1
#1		}
#1		else{
#1			$pkgdir = 	$component . "_" . $component_host;
#1			$component_viewname = $component . "View" . $component_host;
#1		}	
#1		
#1		$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/" . $pkgdir . "/package.pkg");
#1		if($package->isInstalled()) {
#1			$package->removePackage($component,$component_host);
#1		}//endif isInstalled
		
#1		print("Removing $component View from Administrators role...");
#1			$adminRoleID = getRoleID("Administrators");
#1			if($adminRoleID === false) {
#1				print("Administrators role not found.  Administrator will need to assign Performance manually to role.\n");
#1			}//endif
#1			else {
#1				if(!GuavaPackage::removeView($component_viewname)) {
#1					print("Unable to remove $component view. May already have been removed.\n");
#1					}//endif attached
#1				else{
#1					print("OK\n");
#1					}
#1			}
#1			return 1;
#1} 

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
