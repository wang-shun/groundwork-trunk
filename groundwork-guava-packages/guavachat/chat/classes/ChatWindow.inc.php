<?php

class ChatWindow extends GuavaObject {
	
	private $tabPanel;
	
	private $mainChat;
	private $mainChatPane;
	
	private $subChats;
	private $subChatPanes;
	
	public function __construct() {
		$this->mainChat = new ScrollBuffer(50);
		$this->mainChatPane = new ContentPane($this->mainChat);
		$this->tabPanel = new TabPane();
		$this->tabPanel->setHeight("500px");
		$this->tabPanel->addPane("Main Chat", $this->mainChatPane);
		$this->tabPanel->addPane("Second", $this->subChatPanes[0]);
	}
	
	public function Destroy() {
		$this->mainChatPane->Destroy();
		unset($this->mainChatPane);
		$this->mainChat->Destroy();
		unset($this->mainChat);
		$this->tabPanel->Destroy();
		unset($this->mainChat);
		
		parent::Destroy();
	}
	
	public function updateChats() {
		global $chat;
			$tempMessages = $chat->getMessages($this->lastTimestamp);
			$this->lastTimestamp = microtime(true);
			$tempOutput = "";
			if(count($tempMessages)) {
				foreach($tempMessages as $message) {
					$timestamp = strtok($message['timestamp'], '.');
					$timestamp = date("g:i:s", $timestamp);
					$tempOutput = "<b>[" . $timestamp ."]</b> " . $message['text'] . "<br />";
					$tempDiv = new CodeDiv();
					$tempDiv->setContent($tempOutput);
					$this->mainChat->append($tempDiv);
				}
			}
	}
	
	public function Draw() {
		?>
		<div id="<?=$this->getIdentifier();?>">
			<?php $this->tabPanel->Draw(); ?>
		</div>
		<?php
	}
}