<?php
class bookshelfView extends GuavaApplication {
	private $iframe;
	private $baseURL;
	private $initialURL;
	
	function __construct() {
		global $guava;
		parent::__construct("bookshelf");
		
		// WHAT'S THE BASE URL
		$this->baseURL = "packages/bookshelf/bookshelf-data/Bookshelf.htm";
		
		// WHAT'S THE INITIAL URL TO VIEW
		$this->initialURL = "";
		
		// $this->addMenuItem("Open in Separate Window", "clone");
	}
	
	public function menuCommand($command) {
		global $guava;
		if($command == "clone") {
			$newBookshelf = new bookshelfView();
			$newBookshelf->init();
			$guava->objectView($newBookshelf);
		}
	}
	
	public function init() {
		$this->iframe = new IFrame($this->baseURL . $this->initialURL);
		
	}
	
	public function close() {
		if(isset($this->iframe)) {
			$this->iframe->unregister();
			$this->iframe = null;
		}
	}
	
	function Draw() {
		$this->iframe->Draw();
	}
}
?>
