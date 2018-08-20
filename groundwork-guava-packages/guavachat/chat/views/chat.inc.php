<?php

require_once(GUAVA_FS_ROOT . 'packages/chat/classes/ChatInput.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/chat/classes/ChatWindow.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/chat/classes/ScrollBuffer.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/chat/classes/UserList.inc.php');

class ChatChatView extends View {
	private $nickname;		// Our nickname for this chat session
	
	private $lastTimestamp;	// Our last timestamp for refresh
	
	private $exitLink;
	
	// New interface modules
	private $chatInput;
	private $chatWindow;
	private $userList;
	
	private $loginDialog;
	
	function __construct() {
		$this->lastTimestamp = null;
		parent::__construct("Chat");
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/chat/templates/chat.xml');
	}
	
	public function unsetNickname() {
		global $chat;
		if(isset($this->exitLink)) {
			$this->exitLink->Destroy();
		}
		$this->exitLink = null;
		$this->disableRefresh();
		if(isset($this->buffer)) {
			$this->buffer->Clear();
			$this->buffer->Destroy();
		}
		$this->buffer = null;
		if(isset($this->submitButton)) {
			$this->submitButton->Destroy();
		}
		$this->submitButton = null;
		if(isset($this->inputText)) {
			$this->inputText->Destroy();
		}
		$this->inputText = null;
		if(isset($this->inputForm)) {
			$this->inputForm->Destroy();
		}
		$this->inputForm = null;
		
		if(isset($this->nickname)) {
			$chat->deregisterNickname($this->nickname);
			$this->nickname = null;
		}
		
		$this->targetData("users", '');
		
		// Okay, let's recreate our form stuff
		$this->inputForm = new Form();
		$this->inputForm->addListener("nickname", $this, "setNickname", null);
		$this->inputText = new InputText();
		$this->submitButton = new SubmitButton("Register Nickname");
		
		ob_start();
		print_window_header("Enter A Nickname", "500");
		if(isset($error_msg)) {
			?>
			<div class="error_msg"><?=$error_msg;?></div>
			<br />
			<?php
		}
		?>
		Guava Chat requires that you have a nickname for this session.  Enter a short descriptive nickname 
		for your session below:<br />
		<?php
		$this->inputForm->Open();
		?><b>Nickname:</b> <?php
		$this->inputText->Draw();
		$this->submitButton->Draw();
		$this->inputForm->Close();
		print("<br />");
		print_window_footer();
		$buffer = ob_get_contents();
		ob_end_clean();
		$this->targetData("contents", $buffer);
	}
	
	public function exitChat($guavaObject, $parameter = null) {
		$this->unsetNickname();
	}
	
	public function setNickname($guavaObject, $parameter = null) {
		global $chat;
		// First update nickname table
		$chat->updateUsers();
		// We need to check for nickname availability
		if($chat->nicknameExists($this->inputText->getValue())) {
			$error_msg = "That nickname is currently taken.  Please choose another...";
		}
		else {
			if(!$chat->registerNickname($this->inputText->getValue())) {
				$error_msg = "That nickname is currently taken.  Please choose another...";
			}
			else {
				$this->nickname = $this->inputText->getValue();
				$this->lastTimestamp = null;
				//Setup our objects
				$this->submitButton->setLabel("Submit");
				$this->inputForm->removeListener("nickname");
				$this->inputForm->addListener("input", $this, "inputHandle", null);
				$this->inputText->setSize(100, 255);
				$this->inputText->setValue('');
				$this->buffer = new ScrollBuffer(50);
				$this->buffer->Clear();
				$this->exitLink = new TextLink("Exit Chat");
				$this->exitLink->addClickListener("exit", $this, "exitChat", null);
				$this->enableRefresh(2);
				$this->refresh();
				ob_start();
				// We need to show the chat window, but we also need to show our custom javascript method
				?>
				<table width="100%">
				<tr>
				<td width="*" height="90%">
				<table width="100%">
				<tr>
				<td bgcolor="gray" style="color: white;">Chat Window</td>
				</tr>
				<tr>
				<td>
				<?php
				$this->buffer->Draw();
				?>
				</td>
				</tr>
				</table>
				</td>
				<td width="200">
				<table width="100%">
				<tr>
				<td bgcolor="gray" style="color: white;">User Listing</td>
				</tr>
				<tr>
				<td>
				<div style="height: 400px; overflow: auto; border-style:solid; padding: 2px; border-width: 1px;">
				<?=$this->printTarget("users");
				?>
				</div>
				</td>
				</tr>
				</table>
				</td>
				</tr>
				<tr>
				<td>
				<?php $this->inputForm->Open();?>
				<?=$this->inputText->Draw();?> <?=$this->submitButton->Draw();?>
				<?php $this->inputForm->Close();?>
				</td>
				<td align="right"><?php $this->exitLink->Draw(); ?></td>
				</tr>
				</table>
				<br />
				<?php
				$buffer = ob_get_contents();
				ob_end_clean();
				$this->targetData("contents", $buffer);
			}
		}
	}
	
	public function init() {
		// $this->unsetNickname();
		$this->chatInput = new ChatInput();
		$this->chatWindow = new ChatWindow();
		$this->userList = new ChatUserList();
		
		$this->nickname = 'testing';
		$this->chatInput->setNickname($this->nickname);
		
		$this->bind("chatInput", $this->chatInput);
		$this->bind("chatWindow", $this->chatWindow);
		$this->bind("userList", $this->userList);
		
		$this->loginDialog = new Dialog();
		$this->loginDialog->setContent("Hello World");
		$this->loginDialog->show();
	}
	
	public function close() {
		global $chat;
		
		$this->chatInput->Destroy();
		$this->chatWindow->Destroy();
		$this->userList->Destroy();
		
		$this->unbind("chatInput");
		$this->unbind("chatWindow");
		$this->unbind("userList");
		
		unset($this->chatInput);
		unset($this->chatWindow);
		unset($this->userList);
	}
	
	public function refresh() {
		global $chat;
		global $guava;
		if(isset($this->nickname)) {
			// First, we need to call prune messages
			$chat->pruneMessages();
			$chat->pingNickname($this->nickname);
			// Then we need to get all the messages which occurred after our last timestamp

			
			$this->chatWindow->updateChats();
			$this->userList->updateUsers();
		}
	}
}

?>