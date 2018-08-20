<?php

class ChatSystemModule extends SystemModule {
	// Database connection parameters
	private $dbServ;	// Either mysql or sqlite
	private $dbHost;
	private $dbUsername;
	private $dbPassword;
	private $dbDatabase;
	private $dbConnection;

	private $interval;		// 2 seconds
		
	function __construct() {
		global $guava;
		$this->dbServ = 'mysql';
		$this->dbHost = $guava->getpreference('chat', 'address');
		$this->dbUsername = $guava->getpreference('chat', 'username');
		$this->dbPassword = $guava->getpreference('chat', 'password');
		$this->dbDatabase = $guava->getpreference('chat', 'dbname');
		$this->dbConnection = ADONewConnection($this->dbServ);
		$this->dbConnection->Connect($this->dbHost, $this->dbUsername,
							$this->dbPassword,$this->dbDatabase);
		if(!$this->dbConnection->IsConnected()) {
			updateStatus("Unable to connect to guava chat database.  Please check your configuration.");
			die();
		}
		$this->dbConnection->SetFetchMode(ADODB_FETCH_ASSOC);	
		
		$this->interval = 2;
		
		parent::__construct("ChatSystemModule");
	}
	
	function init() {
		global $chat;
		$chat = $this;
	}
	
	function restart() {
		global $chat;
		// Restart the object
		$this->dbConnection = ADONewConnection($this->dbServ);
		$this->dbConnection->Connect($this->dbHost, $this->dbUsername,
							$this->dbPassword,$this->dbDatabase);
		if(!$this->dbConnection->IsConnected()) {
			print("Unable to connect to guava chat database.  Please check your configuration.");
			die();
		}
		$this->dbConnection->SetFetchMode(ADODB_FETCH_ASSOC);		
		$chat = $this;	// Recreate the global link to ourself.
	}
	
	function reload() {
		$this->dbHost = $guava->getpreference('chat', 'address');
		$this->dbUsername = $guava->getpreference('chat', 'username');
		$this->dbPassword = $guava->getpreference('chat', 'password');
		$this->dbDatabase = $guava->getpreference('chat', 'dbname');
		$this->dbConnection = ADONewConnection($this->dbServ);
		$this->dbConnection->Connect($this->dbHost, $this->dbUsername,
							$this->dbPassword,$this->dbDatabase);
		if(!$this->dbConnection->IsConnected()) {
			print("Unable to connect to guava chat database.  Please check your configuration.");
			die();
		}
		$this->dbConnection->SetFetchMode(ADODB_FETCH_ASSOC);
	}
	
	public function updateUsers() {
		// We need to delete from chat_users where timestamp is older than now * 3 intervals
		$timestamp = time();
		$timestamp = $timestamp - (3 * $this->interval);
		$query = "SELECT nickname FROM chat_users WHERE timestamp < '".$timestamp."'";
		$result = $this->dbConnection->Execute($query);
		while(!$result->EOF) {
			$this->deregisterNickname($result->fields['nickname']);
			$result->MoveNext();
		}
		return true;
	}
	
	public function nicknameExists($nickname) {
		$query = "SELECT * FROM chat_users WHERE lcase(nickname) = lcase('$nickname')";
		$result = $this->dbConnection->Execute($query);
		if(!$result->EOF) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public function registerNickname($nickname) {
		$timestamp = microtime(true);
		$query = "INSERT INTO chat_users(timestamp, username, nickname) VALUES('".$timestamp."', '".$_SESSION['username']."','".$nickname."')";
		$result = $this->dbConnection->Execute($query);
		$this->addMessage('*** ' .$nickname . " Has Entered The Room...");
		return true;
	}
	
	public function pingNickname($nickname) {
		$timestamp = microtime(true);
		$query = "UPDATE chat_users SET timestamp = '".$timestamp."' WHERE nickname = '".$nickname ."'";
		$result = $this->dbConnection->Execute($query);
		return true;
	}
	
	public function deregisterNickname($nickname) {
		$query = "DELETE from chat_users WHERE nickname = '$nickname'";
		$result = $this->dbConnection->Execute($query);
		$this->addMessage('*** ' .$nickname . " Has Left The Room...");
		return true;
	}
	
	public function addMessage($message) {
		global $guava;
		$timestamp = microtime(true);
		$message = addslashes($message);
		$query = "INSERT INTO chat_messages(timestamp, text) VALUES('". $timestamp ."', '".$message."')";
		$result = $this->dbConnection->Execute($query);
	}
	
	public function getMessages($timestamp = null) {
		$messages = array();
		$query = "SELECT * FROM chat_messages";
		if($timestamp != null) {
			$query .= " WHERE timestamp > '" . $timestamp . "'";
		}
		$query .= " ORDER BY timestamp";

		$result = $this->dbConnection->Execute($query);
		while(!$result->EOF) {
			$result->fields['text'] = stripslashes($result->fields['text']);
			$messages[] = $result->fields;
			$result->MoveNext();
		}
		return $messages;
	}
	
	public function getUsers() {
		$users = array();
		$query = "SELECT * FROM chat_users";
		$result = $this->dbConnection->Execute($query);
		while(!$result->EOF) {
			$users[] = $result->fields;
			$result->MoveNext();
		}
		return $users;
	}
	
	public function pruneMessages() {
		// We're going to prune messages that are over 4 * interval old
		$timestamp = time(true);
		$timestamp = $timestamp - (4 * $this->interval);
		$query = "DELETE FROM chat_messages WHERE timestamp < '$timestamp'";
		$result = $this->dbConnection->Execute($query);
		$this->updateUsers();
		return true;
	}
	
}

?>