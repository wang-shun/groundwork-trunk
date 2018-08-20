<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class DashboardDaemon extends SystemModule {
    private $_dbConn = null;

    function __construct() {
    	global $guava;
    	parent::__construct("dashboarddaemon");

		$this->_dbConn = ADONewConnection($guava->getpreference("dashboards", "dbtype"));
		$this->dBConnect();
    }

    function init() {
    	global $dashboarddaemon;
    	$dashboarddaemon = $this;
		$this->dBConnect();
    }

    function restart() {
    	global $dashboarddaemon;
    	$dashboarddaemon = $this;
		$this->dBConnect();
    }

    function dBConnect() {
    	global $guava;
		@$this->_dbConn->PConnect($guava->getpreference("dashboards", "address"), $guava->getpreference("dashboards", "username"), $guava->getpreference("dashboards", "password"), $guava->getpreference("dashboards", "dbname"));

		if(!$this->_dbConn->IsConnected()) {
			$i = new InfoDialog("Unable to connect to database: '".$guava->getpreference("dashboards", "dbname")."' with supplied credentials; please check settings in 'guava_preferences' table.");
			$i->show();
		}

		$this->_dbConn->debug=0;
		$this->_dbConn->SetFetchMode(ADODB_FETCH_ASSOC);
    }

	//same result as  getDashboardListForUser($user_id ,$access='write') 
    function getWritableDashboardsForUser($user_id) {
    	global $guava;
    	
    	$dashboards = array();

		// get all dashboards that this user authored
    	//$query = "SELECT * FROM dashboard WHERE uid = '$user_id'";
		// get all dashboards that this user authored, or has write priv on
    	// $query = "SELECT d.* FROM dashboard.dashboard d RIGHT JOIN dashboard.privileges p on d.id = p.dashboard_id where d.uid = '$user_id' or (p.type = 'user' and p.target_id = '$user_id'and p.write = '1')";

		// get all dashboards that the group that this user belongs to has write priv on
		// $query = "SELECT d.* FROM dashboard.dashboard d RIGHT JOIN dashboard.privileges p ON p.dashboard_id = d.id RIGHT JOIN guava.guava_group_assignments AS ga ON (ga.group_id = p.target_id AND p.type = 'group') WHERE ga.user_id = '$user_id' AND p.write = '1'";


//FUTURE: may want to make separate functions to get these values such as
//getWriteableDashboardListForUsers,getWriteableDashboardListForGroups, getWriteableDashboardListForRoles

		$query = <<<SQL
		SELECT d.* FROM dashboard.dashboard AS d WHERE d.global = '1'
		    UNION
		SELECT d.* FROM dashboard.dashboard AS d WHERE d.uid = '$user_id'
			UNION
		SELECT d.* FROM dashboard.dashboard d RIGHT JOIN dashboard.privileges p ON d.id = p.dashboard_id WHERE p.type = 'user' AND p.target_id = '$user_id' AND p.write = '1'
			UNION
		SELECT d.* FROM dashboard.dashboard d RIGHT JOIN dashboard.privileges p ON p.dashboard_id = d.id RIGHT JOIN guava.guava_group_assignments AS ga ON (ga.group_id = p.target_id AND p.type = 'group') WHERE ga.user_id = '$user_id' AND p.write = '1'
			UNION
		SELECT d.* FROM dashboard.dashboard d RIGHT JOIN dashboard.privileges p ON p.dashboard_id = d.id RIGHT JOIN guava.guava_role_assignments AS gr ON (gr.role_id = p.target_id AND p.type = 'role') WHERE gr.user_id = '$user_id' AND p.write = '1'
SQL;
				
    	$result = $this->_dbConn->Execute($query);

    	if ($this->_dbConn->ErrorNo() != 0) {
    		throw new GuavaException("Could not execute query '" . $query . "'.");
    	}

    	while (!$result->EOF) {
    		$dashboard = null;
    		$dashboards[$result->fields['id']] = $result->fields;
    		$result->MoveNext();
    	}
    	$result->Close();
    	return $dashboards;
    }
    
    function dashboardExists($name) {
    	$name = mysql_real_escape_string($name);
    	$query = "SELECT * FROM dashboard WHERE name = '$name'";
    	$result = $this->_dbConn->Execute($query);
    	if($result->EOF) {
    		return false;
    	}
    	return true;
    }

/**
 * getDefaultDashboardID function
 *
 * @return int DefaultDashboardID of that role
 * @author Amy Tang
 **/
function getDefaultDashboardID($userid){
    global $guava;

    $default_role_id = $guava->getDefaultRole($userid);
    if($default_role_id){
        $query  = "SELECT dashboard_id FROM roles_defaultdashboards WHERE role_id = '$default_role_id'";
        if ($result = $this->_dbConn->Execute($query)) {
            return $result->fields['dashboard_id'];            
        }
        else
		{
			$temp = new InfoDialog("Role $default_role_id does not have a default dashboard.");
			$temp->show();
		}
    }
    else
	{
		// $temp = new InfoDialog("User doesn't have a default role id.");
		// 	 	$temp->show();    
		$query = "SELECT id FROM dashboard where name='systemdefault'";
        if ($result = $this->_dbConn->Execute($query)) {
            return $result->fields['id'];            
        }else{
			return false;
		}
	}
    return false;
}

/**
 * getSystemDefaultDashboardID function
 *
 * @return int SystemDefaultDashboardID
 * 
 **/
function getSystemDefaultDashboardID(){
	global $guava;
	if ($systemdefaultdashboard_id = $guava->getSystemDefaultDashboardID()){
		return $systemdefaultdashboard_id;
	}
	else return false;
	
	
}

/**
 * getDefaultDashboardID function
 * @deprecated
 * @return int default dashboard ID for a given user ID
 * @author Amy Tang
 **/

	//  	function getDefaultDashboardID($uid){
	// 	global $guava;
	// 	// $query = "SELECT d.id FROM dashboard d RIGHT JOIN default_dashboards dd ON (d.id = dd.dashboard_id ) WHERE dd.user_id='$uid'";
	// 	// 
	// 	// //get default dashboard if you've created one for yourself
	// 	// if ($result = $this->_dbConn->Execute($query) && $result->num_rows > 0) {
	// 	// 	$defaultdashboardid = $result->fields['id'];			
	// 	// }//otherwise, it finds the default dashboard for your role
	// 	// else{
	// 		//get all role ids for that user
	// 		$user_roles = $guava->getAllRolesForUser($uid);
	// 		// $temp = new InfoDialog ("Roles: ".$user_roles[0]);
	// 		// $temp->show();
	// 		//find role_ids for roles: admin,executive,operator
	// 		$query = "SELECT gr.role_id,gr.name from guava.guava_roles gr WHERE gr.name IN ('Administrators','Executives','Operators')";
	// 		$result = $this->_dbConn->Execute($query);
	// 		while (!$result->EOF) {
	//     		$roles[$result->fields['name']] = $result->fields['role_id'];
	//     		$result->MoveNext();
	//     	}
	// 		
	// 		// $temp = new InfoDialog ("Role dashboards: ".var_export($roles));
	// 		//if admin
	// 		if (in_array($roles['Administrators'],$user_roles)) {
	// 			$query = "SELECT d.id FROM dashboard d WHERE d.name = 'defaultadmin'";
	// 		}elseif (in_array($roles['Executives'],$user_roles)){
	// 			$query = "SELECT d.id FROM dashboard d WHERE d.name = 'defaultexecutive'";
	// 		}elseif (in_array($roles['Operators'],$user_roles)){
	// 			$query = "SELECT d.id FROM dashboard d WHERE d.name = 'defaultoperator'";
	// 		}else {
	// 			//is a 'user'
	// 			$query = "SELECT d.id FROM dashboard d WHERE d.name = 'defaultuser'";
	// 		}
	// 		// $temp = new InfoDialog ("Query: ".$query);
	// 		// $temp->show();
	// 		if ($result = $this->_dbConn->Execute($query)) {
	// 			$defaultdashboardid = $result->fields['id'];
	// 		}else{
	// 			throw new GuavaException("No default role dashboard found - should not happen :'". $query . "'.");
	// 		}
	// 		
	// 		// //assuming role_id =   1 - admin, 2 - executive, 3 - operator, everything else is a user
	// 		// 			$adminRoleUsers = $guava->getRoleAssignments($this->role_id);
	// 
	// 	// }
	// 	
	// 	// $dashboard = getDashboard($result);
	// 	// if ($this->_dbConn->ErrorNo() != 0) {
	// 	//     throw new GuavaException("Could not execute query '" . $query . "'.");
	// 	// }
	// 	// if(!$result->EOF) {
	// 	// 	$dashboard = $result->fields;
	// 	// }
	// 	
	// 	return $defaultdashboardid;
	// }
    
    function addDashboardPrivilege($dashboardID, $type, $id, $access) {
		if ($access == 'write'){
    		$query = "INSERT INTO privileges(dashboard_id, type, target_id,`write`) VALUES('$dashboardID', '$type', '$id','1')";
		}else{
			$query = "INSERT INTO privileges(dashboard_id, type, target_id,`write`) VALUES('$dashboardID', '$type', '$id','0')";
		}
		// $temp = new InfoDialog ("Query: ".$query);
		// $temp->show();
		
    	$result = $this->_dbConn->Execute($query);
    }
    
    function getDashboardPrivileges($dashboard) {
    	$privs = array();
    	$query = "SELECT * FROM privileges WHERE dashboard_id = '$dashboard'";
    	$result = $this->_dbConn->Execute($query);
    	while(!$result->EOF) {
    		$privs[] = $result->fields;
    		$result->MoveNext();
    	}
    	return $privs;
    }
    
    function getDashboardListForUser($user_id ,$access='read') {
    	global $guava;
    	
    	$dashboards = array();

		//permissions for global dashboards and the ones created by yourself
    	$query = "SELECT * FROM dashboard WHERE global = '1' OR uid = '$user_id'";
    	
    	$result = $this->_dbConn->Execute($query);

    	if ($this->_dbConn->ErrorNo() != 0) {
    		throw new GuavaException("Could not execute query '" . $query . "'.");
    	}

		//adds dashboards found that is global or created by user to dashboard array
    	while (!$result->EOF) {
    		$dashboard = null;
    		$dashboards[$result->fields['id']] = $result->fields;
    		$result->MoveNext();
    	}
    	$result->Close();
    	
    	/* QUERY DASHBOARD PRIVILEGES FOR THIS USER SPECIFICALLY */
    	$dashboardList = array(); 
  
 	//slow
	// $query = "SELECT * FROM dashboard WHERE id IN (SELECT dashboard_id FROM privileges WHERE type ='user' AND target_id = '$user_id')";
	
		//get all dashboards for which the user has a privilege entry in privilege table, meaning either read or write access
		if ($access == 'read'){
			 $query = "SELECT d.* FROM dashboard d LEFT JOIN privileges p ON (d.id = p.dashboard_id AND p.type='user') WHERE p.target_id = '$user_id'";
		}elseif($access == 'write'){
			 $query = "SELECT d.* FROM dashboard d LEFT JOIN privileges p ON (d.id = p.dashboard_id AND p.type='user') WHERE (p.target_id = '$user_id' AND p.write = '1')";
		}
		// $temp = new InfoDialog ("Inside of getDashboardListForUser" . $query);
		// $temp->show();
			
	    $result = $this->_dbConn->Execute($query);
	    
	    if ($this->_dbConn->ErrorNo() != 0) {
			throw new GuavaException("Could not execute query '" . $query . "'.");
	    }

		//add dashboards that user has privileges 
	    while (!$result->EOF) {
			$dashboard = null;
	
			foreach (array_keys($result->fields) as $key) {
			    $dashboard[$key] = $result->fields[$key];
			}
	
			$dashboardList[] = $dashboard;
			$result->MoveNext();
	    }

	    $result->Close();
    	foreach($dashboardList as $dashboard) {
    		if(!isset($dashboards[$dashboard['id']])) {
    			$dashboards[$dashboard['id']] = $dashboard;
    		}
    	}
    	/* END DASHBOARD PRIVILEGE QUERY */

    	$groups = $guava->getAllGroupsForUser($user_id);    		
    	$dashboardList = $this->getDashboardListForGroups($groups, $access);
    	
		//add dashboards where user's group has privileges
    	foreach($dashboardList as $dashboard) {
    		if(!isset($dashboards[$dashboard['id']])) {
    			$dashboards[$dashboard['id']] = $dashboard;
    		}
    	}

    	$roles  = $guava->getAllRolesForUser($user_id);
    	$dashboardList = $this->getDashboardListForRoles($roles, $access);

		//add dashboards where users's role has privileges
    	foreach($dashboardList as $dashboard) {
    		if(!isset($dashboards[$dashboard['id']])) {
    			$dashboards[$dashboard['id']] = $dashboard;
    		}
    	}
    	
    	return $dashboards;
    }

    private function getDashboardListForRoles($roles = array(), $access = 'read') {
	$dashboardList = array();

	if (is_array($roles) && (!empty($roles))) {

	    // construct the roles ids we wish to search for
	    // foreach ($roles as $roleId) { $roleIdStr = "'" . $roleId . "', "; }
	    // $roleIdStr = substr($roleIdStr, 0, -2);
		$roleIdStr = '\''.implode('\',\'',$roles).'\'';

	    // $query = "SELECT * FROM dashboard WHERE id IN " .
	    // 		     "(SELECT dashboard_id FROM privileges WHERE type = 'role' AND target_id IN (" .
	    // 		     $roleIdStr . "))";

		if ($access == 'read'){
			 $query = "SELECT d.* FROM dashboard d LEFT JOIN privileges p ON (d.id = p.dashboard_id AND p.type='role') WHERE p.target_id IN (" . $roleIdStr . ")";
		}elseif($access == 'write'){
	 		$query = "SELECT d.* FROM dashboard d LEFT JOIN privileges p ON (d.id = p.dashboard_id AND p.type='role' ) WHERE p.write = '1' AND p.target_id IN (" . $roleIdStr . ")";
		}
		// 
		// $temp = new InfoDialog ("Inside of getDashboardListForRoles: " . $query);
		// $temp->show();
	    $result = $this->_dbConn->Execute($query);

	    if ($this->_dbConn->ErrorNo() != 0) {
		throw new GuavaException("Could not execute query '" . $query . "'.");
	    }

	    while (!$result->EOF) {
		$dashboard = null;

		foreach (array_keys($result->fields) as $key) {
		    $dashboard[$key] = $result->fields[$key];
		}

		$dashboardList[] = $dashboard;
		$result->MoveNext();
	    }

	    $result->Close();
	}

	return $dashboardList;
    }

    private function getDashboardListForGroups($groups = array(), $access='read') {
	$dashboardList = array();

	if(Count($groups)) {
		// construct the roles ids we wish to search for
		// foreach ($groups as $groupId) { $groupIdStr = "'" . $groupId . "', "; }
		// $groupIdStr = substr($groupIdStr, 0, -2);
		$groupIdStr = '\''.implode('\',\'',$groups).'\'';
		
		// $query = "SELECT * FROM dashboard WHERE id IN " .
		// 		         "(SELECT dashboard_id FROM privileges WHERE type = 'group' AND target_id IN (" .
		// 		         $groupIdStr . "))";

		if ($access == 'read'){
			 $query = "SELECT d.* FROM dashboard d LEFT JOIN privileges p ON (d.id = p.dashboard_id AND p.type='group') WHERE p.target_id IN (" . $groupIdStr . ")";
		}elseif($access == 'write'){
			$query = "SELECT d.* FROM dashboard d LEFT JOIN privileges p ON (d.id = p.dashboard_id AND p.type='group') WHERE p.write = '1' AND p.target_id IN (" . $groupIdStr . ")";
		}
		$result = $this->_dbConn->Execute($query);
		
		// $temp = new InfoDialog ("Inside of getDashboardListForGroups" . $query);
		// $temp->show();

		if ($this->_dbConn->ErrorNo() != 0) {
		    throw new GuavaException("Could not execute query '" . $query . "'.");
		}
	
		while (!$result->EOF) {
		    $dashboard = null;
	
		    foreach (array_keys($result->fields) as $key) {
			$dashboard[$key] = $result->fields[$key];
		    }
	
		    $dashboardList[] = $dashboard;
		    $result->MoveNext();
		}
	
		$result->Close();
	}

	return $dashboardList;
    }

		//     function getDashboard($dashboardID) {
		//     	$dashboard = null;
		// $query = "SELECT * FROM dashboard WHERE id = '$dashboardID'";
		//     	$result = $this->_dbConn->Execute($query);
		// if ($this->_dbConn->ErrorNo() != 0) {
		// 	return false;
		//     // throw new GuavaException("Could not execute query '" . $query . "'.");
		// }
		// if(!$result->EOF) {
		// 	$dashboard = $result->fields;
		// }
		// return $dashboard;
		//     }

	function getDashboard($dashboardID) {
        $dashboard = null;
        $query = "SELECT * FROM dashboard WHERE id = '$dashboardID'";
	    try {
            $result = $this->_dbConn->Execute($query);
        } catch (ADODB_Exception $e) {
        //    echo 'failed to get dashboard '.$dashboardID."\n";
        //    echo "adodb exception message: ".$e->getMessage();
            throw new GuavaException("Could not execute query '" . $query . "'.\nADODB exception message: ".$e->getMessage());
        }
        if(!$result->EOF) {
            $dashboard = $result->fields;
        }
        return $dashboard;
    }

	/**
	* getAllDashboards function
	*
	* @return array all dashboards in system
	* @author Amy Tang
	**/
	function getAllDashboards()
	{
		$dashboard = null;
        $query = "SELECT * FROM dashboard";
	    try {
            $result = $this->_dbConn->Execute($query);
        } catch (ADODB_Exception $e) {
            throw new GuavaException("Could not execute query '" . $query . "'.\nADODB exception message: ".$e->getMessage());
        }
        while(!$result->EOF) {
        	$dashboard[] = $result->fields;
			$result->MoveNext();
        }
        return $dashboard;
	}

    function __destruct() {
	if (isset($dbConn)) { $dbConn->Close(); } 
    }
    
    function getWidgetsForDashboard($dashboardID) {
    	$widgets = array();
    	// First, we must get membership list
    	$query = "SELECT widget_id FROM widgetmap WHERE dashboard_id = '$dashboardID'";
    	$result = $this->_dbConn->Execute($query);
		if ($this->_dbConn->ErrorNo() != 0) {
		    throw new GuavaException("Could not execute query '" . $query . "'.");
		}
		while(!$result->EOF) {
			$widgetResult = $this->_dbConn->Execute("SELECT * FROM widget WHERE id = '" . $result->fields['widget_id'] . "'");
			if(!$widgetResult->EOF) {
				$tempWidget = $widgetResult->fields;
				$tempWidget['configuration'] = unserialize($tempWidget['configuration']);
				$widgets[] = $tempWidget;
			}
		$result->MoveNext();
		}
		return $widgets;
    }
    
    
    // Let's save a dashboard!
    public function createDashboard($name, $backgroundColor, $backgroundImage, $repeatX, $repeatY, $refresh, $global, $uid,$default=0) {
   	// $query = "INSERT INTO dashboard(name, background_color, background_image, global, uid) VALUES('" . mysql_real_escape_string($name) . "','" . mysql_real_escape_string($backgroundColor) . "','" . mysql_real_escape_string($backgroundImage) . "','" . $global . "','" . $uid . "')";

    	 $query = "INSERT INTO dashboard(name, background_color, background_image, global, uid, background_repeat_x, background_repeat_y, refresh) VALUES('" . mysql_real_escape_string($name) . "','" . mysql_real_escape_string($backgroundColor) . "','" . mysql_real_escape_string($backgroundImage) . "','" . $global . "','" . $uid . "','" . (int)$repeatX . "','" . (int)$repeatY . "','" . (int)$refresh . "')";
		// 
		// $infodialog = new InfoDialog("Query:" . $query);
		// $infodialog->show();
			
   	 	$this->_dbConn->Execute($query);

		if ($this->_dbConn->ErrorNo() != 0) {
		    throw new GuavaException("Could not execute query '" . $query . "'.");
		}
		$id = $this->_dbConn->Insert_ID();
		
		//add to default_dashboards table if default set to 1
		//need to check for unique?
		if($default){
			$query = "INSERT INTO default_dashboards(user_id,dashboard_id) VALUES ('".$uid."','".$id."')";
			$this->_dbConn->Execute($query);

			if ($this->_dbConn->ErrorNo() != 0) {
			    throw new GuavaException("Could not execute query '" . $query . "'.");
			}
		}
		
		return $id;
    }
    
    public function saveDashboard($id, $name, $backgroundColor, $backgroundImage, $repeatX, $repeatY, $refresh, $global, $uid, $default=0) {
		if(strlen($name) > 50)
		{
			$name = substr($name,0,50);
		}
    	// $query = "UPDATE dashboard SET name = '" . mysql_real_escape_string($name) . "', background_color = '$backgroundColor', background_image = '" . mysql_real_escape_string($backgroundImage) . "', global = '$global', uid = '$uid' WHERE id = '$id'";

		$query = "UPDATE dashboard SET name = '" . mysql_real_escape_string($name) . "', background_color = '$backgroundColor', background_image = '" . mysql_real_escape_string($backgroundImage) . "', global = '$global', uid = '$uid', background_repeat_x = '$repeatX', background_repeat_y = '$repeatY', refresh = '$refresh' WHERE id = '$id'";
    	
		$this->_dbConn->Execute($query);
		
		if ($this->_dbConn->ErrorNo() != 0) {
		    throw new GuavaException("Could not execute query '" . $query . "'.");
		}
		$id = $this->_dbConn->Insert_ID();
		
		//add to default_dashboards table if default set to 1
		//need to check for unique?
		if($default){
			$query = "INSERT INTO default_dashboards(user_id,dashboard_id) VALUES ('".$uid."','".$id."')";
			$this->_dbConn->Execute($query);

			if ($this->_dbConn->ErrorNo() != 0) {
			    throw new GuavaException("Could not execute query '" . $query . "'.");
			}
		}
		return $id;    	
    }
    
    public function clearDashboard($id) {
    	$widgetList = $this->getWidgetsForDashboard($id);
    	foreach($widgetList as $widget) {
	    	$query = "DELETE FROM widgetmap WHERE widget_id = '" . $widget['id'] . "'";
	    	$this->_dbConn->Execute($query);
	    	$query = "DELETE FROM widget WHERE id = '" . $widget['id'] . "'";
	    	$this->_dbConn->Execute($query);
    	}
    	$query = "DELETE FROM privileges WHERE dashboard_id = '$id'";
    	$this->_dbConn->Execute($query);
    }
    
    public function deleteDashboard($id) {
	    global $guava;
    	$widgetList = $this->getWidgetsForDashboard($id);
    	foreach($widgetList as $widget) {
	    	$query = "DELETE FROM widgetmap WHERE widget_id = '" . $widget['id'] . "'";
	    	$this->_dbConn->Execute($query);
	    	$query = "DELETE FROM widget WHERE id = '" . $widget['id'] . "'";
	    	$this->_dbConn->Execute($query);
    	}
    	$query = "DELETE FROM privileges WHERE dashboard_id = '$id'";
    	$this->_dbConn->Execute($query);
		// $query = "DELETE FROM roles_defaultdashboards WHERE dashboard_id = '$id'";
		// $this->_dbConn->Execute($query);
    	$query = "DELETE FROM dashboard WHERE id = '$id'";
    	$this->_dbConn->Execute($query);

		$systemdefaultdashboard_id = $guava->getSystemDefaultDashboardID();
		$query = 'UPDATE roles_defaultdashboards set dashboard_id='.$systemdefaultdashboard_id.' WHERE dashboard_id='. $id;
    	$this->_dbConn->Execute($query);	

		return true;
    }

	/**
	 * findUsersForDashboard function
	 *
	 * @return int number of users that are using this dashboard
	 * @author Amy Tang
	 **/
	public function findUsersForDashboard($dashboard_id){
		$query = 'SELECT COUNT(*) as usercount FROM guava_users gu JOIN roles_defaultdashboards rdd ON (gu.default_role_id=rdd.role_id) WHERE rdd.dashboard_id='.(int)$dashboard_id;
    	$result = $this->_dbConn->Execute($query);
		return $result->fields['usercount'];
	}
    
    public function addDashboardWidget($dashboardID, $name, $class, $top, $left, $width, $height, $zindex, $configObject) { 	
    	$query = "insert into widget(name, class, y, x, width, height, zindex, configuration) VALUES ('" . 
    			mysql_real_escape_string($name) . "','" . $class . "','" . $top . "','" . $left . "','" . $width . "','" . $height . "','" . $zindex . "','" . mysql_real_escape_string(serialize($configObject)) . "')";
    	@$this->_dbConn->Execute($query);
		if ($this->_dbConn->ErrorNo() != 0) {
		    throw new GuavaException("Could not execute query '" . $query . "'.");
		}
		$id = $this->_dbConn->Insert_ID();
		// Create mapping
		$query = "insert into widgetmap(dashboard_id, widget_id) VALUES ('" . $dashboardID . "','" . $id . "')";
    	@$this->_dbConn->Execute($query);
		if ($this->_dbConn->ErrorNo() != 0) {
		    throw new GuavaException("Could not execute query '" . $query . "'.");
		}
		return $id;	
    }
    
	/**
	 * getBackgroundImages
	 * gets list of bg images from preset directory
	 * TODO: image validation?
	 * @return images array
	 * @author Amy Tang
	 */
	
	function getBackgroundImages(){
		$images = array();
		$path = GUAVA_FS_ROOT."packages/dashboard/images/background";
		if (is_dir($path)) {
		    if ($dh = opendir($path)) {
		        while (($file = readdir($dh)) !== false) {
					if($file != '.' && $file != '..' && $file != '.svn')
					{
						$images[] = array('name' => $file,'url'=>GUAVA_WS_ROOT . 'packages/dashboard/images/background/'.$file);
					}
		        }
		        closedir($dh);
		    }
		}
		return $images;
	}

}

?>