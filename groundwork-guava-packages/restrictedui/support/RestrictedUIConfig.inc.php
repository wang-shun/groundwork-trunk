<?php
/*
 * Created on Jun 21, 2006
 *
 * @author Daniel Puertas <dpuertas@itgroundwork.com>
 * 
 */
 
 
 class RestrictedUIConfig extends GuavaObject   {
 		private $parentView;
 		private $garbage;
 		private $guavaGroupSelect;
      	private $hostGroupSelect;
      	private $addButton;
      	private $removeButton;
      	private $GGhostGroupSelect;
      	 		 
 		public function __construct($parentView) {
 			global $foundationDB;
			global $sv;
			parent::__construct($parentView);			
 
			//Initialize Variables;
			$this->parentView = $parentView;
			$this->sv = $sv;
			$this->garbage = array();
		
			//Set Template
			$this->setTemplate(GUAVA_FS_ROOT . "packages/restrictedui/templates/config.xml");
			 
			$this->guavaGroupSelect = new Select(1,$this);
			$this->bind("guavaGroupSelect",$this->guavaGroupSelect);
			$this->guavaGroupSelect->addClickListener("guavaGroupChange", $this, "guavaGroupHandler", null);
			$this->populateGuavaGroupSelect();

			$this->GGhostGroupSelect = new InputSelect(5,$this);
			$this->bind("GGhostGroupSelect",$this->GGhostGroupSelect);
		 	$this->updateHostGroups('Admins'); //should be index[0] or something
			
			$this->hostGroupSelect = new InputSelect(5,$this);
			$this->bind("hostGroupSelect",$this->hostGroupSelect);
			$this->populateHostGroupSelect();
			
			$this->addButton = new Button("<<",$this);
			$this->bind("addButton",$this->addButton);
			$this->addButton->addClickListener("add",$this,"assignHostGroup",null);
			
			$this->removeButton = new Button(">>",$this);
			$this->bind("removeButton",$this->removeButton);
		    $this->removeButton->addClickListener("remove",$this,"removeHostGroup",null);			
 		}
 		
 		public function assignHostGroup(){
 			
 			$guavaGroup = $this->guavaGroupSelect->getValue();
 			$hostGroup = $this->hostGroupSelect->getValue();
 			$sql = "INSERT INTO restrictedui.GuavaGroup_HostGroup(guavaGroupID,hostGroupID)
						 							 values((select group_id from guava.guava_groups where name = '$guavaGroup'),
																 (select HostGroupID from HostGroup where Name = '$hostGroup') )";
			try{
				$conn = SVConnectionManager::getNewConnection();
			    if($conn != null){
			    	$conn->Execute($sql);
			    }
			}
			catch(Exception $e){
					$err = new ErrorDialog("<b>You have already assigned <i>$hostGroup</i> to group <i>$guavaGroup</i></b>");
					$err->show();		
			}
 		}
 		public function removeHostGroup(){
 			$guavaGroup = $this->guavaGroupSelect->getValue();
 			$GGhostGroup = $this->GGhostGroupSelect->getValue();
 			$sql = "DELETE FROM restrictedui.GuavaGroup_HostGroup" .
 					"		WHERE guavaGroupID = (select group_id from guava.guava_groups where name = '$guavaGroup')  AND" .
 					"						hostGroupID = (select HostGroupID from HostGroup where Name = '$GGhostGroup')";
 			try{
 				$conn = SVConnectionManager::getNewConnection();
 				if($conn != null){
 					$conn->Execute($sql);
 				}
 			}
 			catch(Exception $e){
 				$err = new ErrorDialog("Couldn't delete $GGhostGroup from group $guavaGroup'");
 				$err->show();
 			}
 		}
 		
 		public  function guavaGroupHandler($groupSelect){

 			$this->updateHostGroups($groupSelect->getValue());
 		}
 		
 		private function updateHostGroups($guavaGroup){
 		 $this->GGhostGroupSelect->removeAll();
 			
 			$searchResults = $this->getHostGroups($guavaGroup);
 			
 			foreach($searchResults as $hostGroup) {
		
 					$this->GGhostGroupSelect->addOption($hostGroup['Name'],$hostGroup['Name']);
 			}
 				
 		
	
 			
 		}
 		
 		private function  getHostGroups($guavaGroup){
 		$sql = "SELECT 	hg.Name " .
 					"FROM  		guava.guava_groups gg, " .
 					"    				 sv.HostGroup hg, ".
 					"     				restrictedui.GuavaGroup_HostGroup gghg " .
 					"WHERE 	gghg.hostGroupID = hg.HostGroupID AND " .
 					"					gghg.guavaGroupID = gg.group_id AND " .
 					"					gg.name = '$guavaGroup' ";
  		$searchResults = array();
  		try{
  			$conn = SVConnectionManager::getNewConnection();
  			if($conn != null){
  				
    	//	$results = $conn->Execute("SELECT Name FROM sv.HostGroup LIMIT 1");
  			
 		$results = $conn->Execute($sql);
	while (! $results->EOF){
  					$searchResults[] = $results->fields;
  					$results->MoveNext();
  				}
  			}
  			else{
 				$err =  new ErrorDialog("Connection was null");
 				$err->show();
   			}
  		}
  		catch(Exception $e){
  			$err = new ErrorDialog("Exception getHostGroups(): " . $e->getMessage());
  			$err->show();
  		}

 		return $searchResults;
 		}
 		
 		
 		
 		private function populateGuavaGroupSelect(){
			$conn =  & GuavaConnectionManager::getNewConnection();
			$sql = "SELECT name" .
						" FROM	guava_groups";
			$groupList= $conn->Execute($sql);
		//	$cnt = 0;
			foreach($groupList as $group){
				
				$this->guavaGroupSelect->addOption($group['name'],$group['name']);
				//$cnt++;
			}
 		}
 		
 		private function populateHostGroupSelect(){
 			$conn = SVConnectionManager::getNewConnection();
 			$sql = "SELECT name".
 						" FROM HostGroup";
 			$groupList = $conn->Execute($sql);
 			
 			
 			$cnt = 0;
 			foreach($groupList as $group){	
				$this->hostGroupSelect->addOption($group['name'],$group['name']);
				$cnt++;
			}
 			
 		}
 		

private function GGpopulateHostGroupSelect(){
 			$conn = SVConnectionManager::getNewConnection();
 			$sql = "SELECT name".
 						" FROM HostGroup";
 			$groupList = $conn->Execute($sql);
 			
 
 			
 			$cnt = 0;
 			foreach($groupList as $group){	
				$this->GGhostGroupSelect->addOption($cnt,$group['name']);
				$cnt++;
			}
 			
 }
 		
 		
 		public function refresh(){
 			//not needed
 		}
 		
 		public function restart(){
 			
 		}
 		
 		public function close(){
 			
 		}
 }
 
 
 
 
 
 
 
 
 
?>
