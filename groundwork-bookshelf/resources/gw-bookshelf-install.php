<?php
/*
 * Created on Feb 6, 2008
 *
 * gw-bookshelf-install.ph
 * 
 * This file is executed only when it is detected that GroundWork Monitor core RPM is installed.
 * This script:
 * 
 * 1) Installs the bookshelf package into guava
 * 2) attaches the bookshelf view to the Operator and Administrator roles
 * 
 */
 
if(file_exists('/usr/local/groundwork/guava/adodb/adodb.inc.php')){
	require_once('/usr/local/groundwork/guava/adodb/adodb.inc.php');
	require_once('/usr/local/groundwork/guava/adodb/adodb-exceptions.inc.php');
}
else{
	print("GroundWork Monitor not installed. Skipping package install\n");
    exit(0);
}
define(GUAVA_FS_ROOT, "/usr/local/groundwork/guava/");
 

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

	if($argv[5] == "remove"){
		remove();
		exit(0);
	}


	if(!install()) {
		print("There was a failure installing the GroundWork Bookshelf Documentation.  Please contact support...\n\n");
	}
	else {
		print("Bookshelf Documentation Install Successful.\n\n");
	}
}
function remove(){
		print("Removing Bookshelf Documentation...");
		//Remove Bookshelf - Gone from Open Source 
		GuavaPackage::removePackage("Bookshelf");
		GuavaPackage::removeView("bookshelfView");
		GuavaPackage::removeFileReference("bookshelf");
		print("OK\n");
}

function install() {
    if(!file_exists("/usr/local/groundwork/guava/packages/bookshelf/package.pkg")){
		print("Bookshelf package not available. Skipping package install\n");
    	exit(0);
    }

   
	$package = new GuavaPackage(GUAVA_FS_ROOT . "packages/bookshelf/package.pkg");
		if(!$package->isInstalled()) {
			print ("Installing Bookshelf Package for GroundWork Monitor...");
			$package->installPackage();
			print ("OK\n");
  		}
			print("Attaching Bookshelf View to Operator Role...");
			$opRoleID = getRoleID("Operators");
			if($opRoleID === false) {
				$opRoleID = getRoleID("Operators");
			}
			if($opRoleID === false) {
				print("Operator role not found.  Administrator may need to assign Bookshelf manually to role.\n");
			}
			else {
				if(!attachView($opRoleID, "bookshelfView")) {
					print("Unable to attach view Bookshelf..\n");
				}
			else{
					print("OK\n");
				}
			}
			print("Attaching Bookshelf View to Administrator Role...");

			$adminRoleID = getRoleID("Administrator");
			if($adminRoleID === false) {
				$adminRoleID = getRoleID("Administrators");
			}
			if($adminRoleID === false) {
				print("Administrator role not found.  Administrator may need to assign Bookshelf manually to role.\n");
			}
			else {
				if(!attachView($adminRoleID, "bookshelfView")) {
					print("Unable to attach view Bookshelf...\n");
				}
			else{
					print("OK\n");
				}
			}
			
			return true; 
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


function print_usage() {
	global $argv;
?>
Usage:
	<?=$argv[0];?> <database server> <database name> <database user> <database password>
	
 		
<?php
}

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
 
 
?>
