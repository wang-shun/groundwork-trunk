<?php

class ChatConfigureView extends View {
	private $dbConnection;
	
	function __construct() {
		global $guava;
		// Let's set our preferences
		$_POST['chat']['address'] = $guava->getpreference('chat', 'address');
		$_POST['chat']['username'] = $guava->getpreference('chat', 'username');
		$_POST['chat']['password'] = $guava->getpreference('chat', 'password');
		$_POST['chat']['dbname'] = $guava->getpreference('chat', 'dbname');
		$this->dbConnection = ADONewConnection("mysql");
		$this->dbConnection->Connect($_POST['chat']['address'], $_POST['chat']['username'],
							$_POST['chat']['password'],$_POST['chat']['dbname']);
		if(!$this->dbConnection->IsConnected()) {
			$invalidConfiguration = true;
		}
		parent::__construct("ChatConfigure");
	}
	
	public function init() {
	}
	
	public function close() {
	}
	
	public function menuCommand($command) {
		// Not valid for a configureview
	}
	
	
	public function refresh() {
		// Empty
	}
	
	
	public function render() {
		global $bookshelf;
		global $guava;
		// Set context, if provided by url
		if(!isset($_POST['request'])) {
				$_POST['chat']['address'] = $guava->getpreference('chat', 'address');
				$_POST['chat']['username'] = $guava->getpreference('chat', 'username');
				$_POST['chat']['password'] = $guava->getpreference('chat', 'password');
				$_POST['chat']['dbname'] = $guava->getpreference('chat', 'dbname');		
		}		
		else {
			if($_POST['request'] == 'propertiesupdate') {
				// We've got updated properties.  Let's first check for required fields.
				if($_POST['chat']['address'] == '' || $_POST['chat']['dbname'] == '' || $_POST['chat']['username'] == '') {
					$error_msg = "You must provide the required fields.";
				}
				else {
					// yay, we have fields.  Let's try a test connection.
					$testConn = ADONewConnection("mysql");
					@$testConn->Connect($_POST['chat']['address'], $_POST['chat']['username'],
										$_POST['chat']['password'],$_POST['chat']['dbname']);
					if(!$testConn->IsConnected()) {
						$error_msg = "Failed to connect to Guava Chat database.  Verify your connection parameters are correct.";
					}
					else {
						$this->dbConnection = $testConn;
					}
				}
				if(!isset($error_msg)) {
					$guava->setpreference("chat", "address", $_POST['chat']['address']);
					$guava->setpreference("chat", "username", $_POST['chat']['username']);
					$guava->setpreference("chat", "password", $_POST['chat']['password']);
					$guava->setpreference("chat", "dbname", $_POST['chat']['dbname']);
					$error_msg = "Successfully updated the Configuration Settings.";
				}
			}			
		}
		
		if(isset($error_msg)) {
			?>
			<br />
			<div align="center"><?=$error_msg;?></div>
			<br />
			<?php
		}

		print_window_header("Guava Chat Configuration", "80%", "center");
		?>
			<form name="configform" action="<?=$_SERVER['PHP_SELF'];?>" method="post">
			<input type="hidden" name="request" value="propertiesupdate" />
			<table>
			<tr class="altTop">
				<td colspan="2">
					<span class="formHeader">chat database configuration</span>
				</td>
			</tr>
			<tr>
				<td><span class="formHeader" <?=$this->tooltip("Database Address", "Specify the IP address or hostname of the database server which your Guava Chat database resides on.");?>>Database Address</span></td>
				<td><input type="text" size="50" maxlength="50" name="chat[address]" value="<?=$_POST['chat']['address'];?>" /><br />
				<br /></td>
			</tr>
			<tr>
				<td><span class="formHeader" <?=$this->tooltip("Database Name", "Specify the name the database server which your Guava Chat database resides on.");?>>Database Name</span></td>
				<td><input type="text" size="50" maxlength="50" name="chat[dbname]" value="<?=$_POST['chat']['dbname'];?>" /><br />
				<br /></td>
			</tr>
			<tr>
				<td><span class="formHeader" <?=$this->tooltip("Database Username", "Specify the username of the user to authenticate against the database server to reach your Guava Chat database.");?>>Database Username</span></td>
				<td><input type="text" size="50" maxlength="50" name="chat[username]" value="<?=$_POST['chat']['username'];?>" /><br />
				<br /></td>
			</tr>
			<tr>
				<td><span class="formHeader" <?=$this->tooltip("Database Password", "Specify the password of the user to authenticate against the database server to reach your Guava Chat database.");?>>Database Password</span></td>
				<td><input type="password" size="50" maxlength="50" name="chat[password]" /><br />
				<br /></td>
			</tr>
			</table>
			<table>
			<tr>
				<td><input type="submit" value="Change Settings" /></td>
			</tr>
			</table>
			</form>
			<?php
		print_window_footer();
	}
}
?>