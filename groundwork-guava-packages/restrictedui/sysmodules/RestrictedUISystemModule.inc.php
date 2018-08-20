<?php


/*
 * Created on Jun 21, 2006
 *
 * @author Daniel Puertas <dpuertas@itgroundwork.com>
 * 
 */

class RestrictedUISystemModule extends SystemModule {
 
public function __construct() {
		global $rui;
		$rui = $this;
		$this->getIncludedHostGroups();

	}

	private function getHostGroups($guavaGroupID) {
		global $guava;
		$sql = "SELECT hostGroupID " .
		"FROM restrictedui.GuavaGroup_HostGroup " .
		"WHERE guavaGroupID = '$guavaGroupID'";

		$conn = SVConnectionManager :: getNewConnection();
		$results = $conn->Execute($sql);
		$retArray = array ();

		foreach ($results as $hostGroup) {
			array_push($retArray, $hostGroup['hostGroupID']);
		}
		return $retArray;
	}

	public function getIncludedHostGroups() {
		global $rui;
		global $guava;
		$includedHostGroups = array();
		$groupList = $guava->getAllGroupsForUser($_SESSION['user_id']);
		foreach ($groupList as $group) {
			$hostGroups = $this->getHostGroups($group);
			$includedHostGroups = array_merge($includedHostGroups, $hostGroups); //verify syntax
		}
		return $includedHostGroups;
	}

	public function init() {

	}

	public function restart() {
		global $rui;
		$rui = $this;
		if (!isset ($this->includedHostGroups)) {
			$this->getIncludedHostGroups();
		}
	}

}
?>