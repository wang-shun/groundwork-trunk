<?php

class ChatInput extends GuavaObject {
	
	private $inputForm;
	
	private $inputText;
	private $inputButton;
	
	private $nickname;
	
	public function __construct() {
		parent::__construct();
		$this->inputForm = new Form();
		$this->inputForm->addListener("input", $this, "inputHandle", null);
		$this->inputButton = new SubmitButton("Submit");
		$this->inputText = new InputText();		
		$this->inputText->setSize(100, 255);
		$this->inputText->setValue('');
	}
	
	public function setNickname($nickname) {
		$this->nickname = $nickname;
	}
	
	public function addMessage($message, $action = false) {
		global $chat;
		if($action == false) {
			$newMessage = "<b>&lt;".$this->nickname."&gt;</b> " . $message;
		}
		else {
			$newMessage = "<b>* " . $this->nickname . " " . $message . "</b>";
		}
		$chat->addMessage($newMessage);
	}
	
	public function inputHandle($guavaObject, $parameter = null) {
		global $chat;
		global $guava;
		$chat->pruneMessages();
		if(strpos($this->inputText->getValue(), "/") === 0) {
			// This is an action, process it accordingly
		    preg_match('/^\/(\S+)\s+(.+)$/', $this->inputText->getValue(), $regs);
		    if($regs[1] == 'me') {
		    	$this->addMessage("<pre style=\"display: inline; font-family:verdana, helvetica, arial;\">" . $regs[2] . "</pre>", 1);	
		    }
		}
		else {
			if($this->inputText->getValue() != '') {
				$this->addMessage("<pre style=\"display: inline; font-family:verdana, helvetica, arial;\">" . $this->inputText->getValue() . "</pre>");
			}
		}
		$this->inputText->setValue('');
		// We need to scroll
		/*
		$messageArray[] = array('name' => 'javascript', 'type' => 'cdata', 'value' => 'scrollChat();');
		$tempMessage = new GuavaMessage(MSG_TYPE_FRAMEWORK, MSG_FRAMEWORK_JAVASCRIPT, $messageArray);
		$guava->addMessage($tempMessage);
		*/
	}
	
	public function Destroy() {
		$this->inputForm->Destroy();
		unset($this->inputForm);
		$this->inputButton->Destroy();
		unset($this->inputButton);
		$this->inputText->Destroy();
		unset($this->inputText);
	}
	
	public function Draw() {
		?>
		<div id="<?=$this->getIdentifier();?>">
			<?php
			$this->inputForm->Open();
			$this->inputText->Draw();
			$this->inputButton->Draw();
			$this->inputForm->Close();
			?>
		</div>
		<?php
	}
}
?>