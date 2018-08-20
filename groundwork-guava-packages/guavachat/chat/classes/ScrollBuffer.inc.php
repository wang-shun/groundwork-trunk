<?php

class ScrollBuffer extends GuavaObject {
	private $buffer;
	
	private $bufferSize;
	
	
	function __construct($bufferSize = 50) {
		$this->bufferSize = $bufferSize;
		parent::__construct();
	}
	
	function append($divObject) {
		global $guava;
		if(count($this->buffer) == $this->bufferSize) {
			// Then we need to shift
			$tempObject = array_shift($this->buffer);
			// We now need to tell the framework to remove this object from the presentation layer
			$messageArray[] = array('name' => 'identifier', 'type' => 'string', 'value' => $tempObject->getIdentifier());
			$messageArray[] = array('name' => 'parent', 'type' => 'string', 'value' => $this->getIdentifier());
			$messageArray[] = array('name' => 'remove', 'type' => 'boolean', 'value' => '1');
			$tempMessage = new GuavaMessage(MSG_TYPE_FRAMEWORK, MSG_FRAMEWORK_OBJECT, $messageArray);
			$guava->addMessage($tempMessage);
			// we better call the delete
			$tempObject->Destroy();
		}
		$this->buffer[] = $divObject;
		ob_start();
		$divObject->Draw();
		$outputBuffer = ob_get_contents();
		ob_end_clean();
		// We have our output in the buffer
		$messageArray[] = array('name' => 'identifier', 'type' => 'string', 'value' => $this->getIdentifier());
		$messageArray[] = array('name' => 'append', 'type' => 'cdata', 'value' => $outputBuffer);
		$tempMessage = new GuavaMessage(MSG_TYPE_FRAMEWORK, MSG_FRAMEWORK_OBJECT, $messageArray);
		$guava->addMessage($tempMessage);
	}
	
	function Clear() {
		if(count($this->buffer)) {
			foreach($this->buffer as $buffer) {
				$buffer->Destroy();
			}
		}
		$this->buffer = null;
	}

	function Destroy() {
		if(count($this->buffer)) {
			foreach($this->buffer as $bufferEntry) {
				$bufferEntry->Destroy();
			}
		}
		parent::Destroy();
	}
	
	function Draw() {
		?>
		<div style="height: 100%; overflow: auto;" id="<?=$this->getIdentifier();?>">
		<?php
		if(count($this->buffer)) {
			foreach($this->buffer as $bufferEntry) {
				$bufferEntry->Draw();
			}
		}
		?>
		</div>
		<?php
	}
}

?>