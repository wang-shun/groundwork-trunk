<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 


class DashboardConfigureView extends View {

	private $myForm;
	
	function __construct() {
		global $guava;
		parent::__construct("DashboardConfigure");
		
		// Create our form object
		$this->myForm = new Form();
		$this->myForm->addListener("config", $this, "formHandler");
			
		$this->myForm->addInputSelect("dbtype");
		$this->myForm->addSelectOption("dbtype", "mysql", "MySQL");
		
		$this->myForm->addInputText("address", 50, 255);
		$this->myForm->addInputText("username", 50);
		$this->myForm->addInputText("dbname", 50);
		$this->myForm->addInputPassword("password", 50);
				
		$this->myForm->addSubmitButton("submit", "Change Settings");
		
		$this->myForm->setFieldValue('dbtype', $guava->getpreference('dashboards', 'dbtype'));
		$this->myForm->setFieldValue('dbname', $guava->getpreference('dashboards', 'dbname'));
		$this->myForm->setFieldValue('address', $guava->getpreference('dashboards', 'address'));
		$this->myForm->setFieldValue('username', $guava->getpreference('dashboards', 'username'));
		$this->myForm->setFieldValue('password', $guava->getpreference('dashboards', 'password'));
		$this->targetData("error_msg", "");
		
	}
	
	public function formHandler() {
		global $guava;
		// We've got updated properties.  Let's first check for required fields.
		if($this->myForm->getFieldValue('address') == '' || $this->myForm->getFieldValue('dbname') == '' || 
			$this->myForm->getFieldValue('username') == '') {
			$err = new ErrorDialog("Please  provide the required fields.");
			$err->show();
			//$this->targetData("error_msg", "You must provide the required fields.");
		}
		else {
			$dbConn = ADONewConnection($this->myForm->getFieldValue('dbtype'));
			@$dbConn->connect($this->myForm->getFieldValue('address'),$this->myForm->getFieldValue('username'),$this->myForm->getFieldValue('password'),$this->myForm->getFieldValue('dbname'));
			if($dbConn->isConnected()){
				$this->_setPreferences();
				}
			else{
				$err = new ErrorDialog("Unable to connect to dashboards database.  Verify connection parameters");
				$err->show();
				}
		}
	}
	
	private function _setPreferences(){
			global $guava;
				$guava->setpreference("dashboards", "dbtype", $this->myForm->getFieldValue('dbtype'));
				$guava->setpreference("dashboards", "address", $this->myForm->getFieldValue('address'));
				$guava->setpreference("dashboards", "username", $this->myForm->getFieldValue('username'));
				$guava->setpreference("dashboards", "password", $this->myForm->getFieldValue('password'));
				$guava->setpreference("dashboards", "dbname", $this->myForm->getFieldValue('dbname'));
				$this->targetData("error_msg", "Successfully updated the dashboards Configuration Settings.");
	}
	
	public function init() {
	}
	
	public function close() {
	}
	
	public function menuCommand($command) {
		// Not valid for a configureview
	}
	
	public function sideNavValue($value) {
		// Not valid for a configureview
	}
	
	public function refresh() {
		// does nothing so far
	}
	
	private function updateSysFile($file, $data) {
		// Not yet implemented
		return false;	
	}
	
	public function render() {
		global $guava;
		
		print_window_header("Dashboards Configuration", "80%", "center");
		$this->myForm->Open();
		?>
		<div align="center" class="error_msg"><?=$this->printTarget("error_msg");?></div>
		<br />
		<h1>Dashboards Database</h1>		
		The dashboards database stores the collection of dashboards, including who can access which dashboards.
		<table>
		<tr>
			<td colspan="2"><span class="formHeader" <?=$this->tooltip("Database Type", "Specify the type of database server your dashboards database resides on.");?>>Database Type: </span>
			<?php $this->myForm->printField("dbtype");?><br />
			<br /></td>
		</tr>
		<tr>
			<td><span class="formHeader" <?=$this->tooltip("Database Address", "Specify the IP address or hostname of the database server which your dashboards database resides on.");?>>Database Address</span></td>
			<td><?php $this->myForm->printField("address");?><br />
			<br /></td>
		</tr>
		<tr>
			<td><span class="formHeader" <?=$this->tooltip("Database Name", "Specify the name the database server which your dashboards database resides on.");?>>Database Name</span></td>
			<td><?php $this->myForm->printField("dbname");?><br />
			<br /></td>
		</tr>
		<tr>
			<td><span class="formHeader" <?=$this->tooltip("Database Username", "Specify the username of the user to authenticate against the database server to reach your dashboards database.");?>>Database Username</span></td>
			<td><?php $this->myForm->printField("username");?><br />
			<br /></td>
		</tr>
		<tr>
			<td><span class="formHeader" <?=$this->tooltip("Database Password", "Specify the password of the user to authenticate against the database server to reach your dashboards database.");?>>Database Password</span></td>
			<td><?php $this->myForm->printField("password");?><br />
			<br /></td>
		</tr>
		</table>		
		<table>
		<tr>
			<td><?php $this->myForm->printField("submit");?></td>
		</tr>
		</table>
		<?php
		$this->myForm->Close();
		print_window_footer();
	}
}
?>