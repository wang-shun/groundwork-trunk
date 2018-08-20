<?php

class ChatUserList extends GuavaObject {
	
	public function __construct() {
		parent::__construct();
		$this->updateUsers();
	}
	
	
	public function updateUsers() {
		global $chat;
		// We now need to get a list of the current users
		$userList = $chat->getUsers();
		
		$tempBuffer = '';
		if(count($userList)) {
			foreach($userList as $user) {
				$tempBuffer .= "<b>" .$user['nickname'] . "</b><br />";
			}
		}
		$this->targetData("users", $tempBuffer);
	}
	
	public function Draw() {
		?>
		<div id="<?=$this->getIdentifier();?>">
				<?php $this->printTarget("users"); ?>
		</div>
		<?php
	}
}
?>