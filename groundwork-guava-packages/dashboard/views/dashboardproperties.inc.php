<?php
/*
 * Created on Dec 5, 2007
 *
 * @author dpuertas
 */
 
 class DashboardPropertiesDialog extends Dialog implements ActionListener {
	
	private $applyButton;
	private $okButton;
	private $cancelButton;
	private $tileCheckBox;
	private $nameInput;
	private $backgroundImageURL;
	
	private $__source;
	private $source;

	
	public function __construct($source) {
		parent::__construct();
		
 		$this->addUIElements();
		$this->__source = $source;
		$this->setTemplate('/usr/local/groundwork/guava/packages/dashboard/templates/dashboardpropertiesdialog.xml');
	}

	private function addUIElements(){
 		 global $dashboarddaemon;
 		
 		 $this->nameInput = new InputText();
 		 $this->bind("nameInput",$this->nameInput);
 		 
 		 $this->tileCheckBox = new InputCheckBox();
 		 $this->bind("tileCheckBox",$this->tileCheckBox);
 
 
 		$images = $dashboarddaemon->getBackgroundImages();
		if(!empty($images))
		{
			$this->backgroundImageURL = new InputSelect();
			$this->backgroundImageURL->addOption('',"No Background");
			foreach($images as $image)
			{
				$this->backgroundImageURL->addOption($image['url'],$image['name']);
			}
			$this->backgroundImageURL->addActionListener("select",$this);
		}
		$this->bind("backgroundImageList",$this->backgroundImageURL);
		//TODO: Show the one is currently selected
 
	     $this->applyButton = new Button("Apply");
		 $this->applyButton->addActionListener("click",$this);
		 $this->bind("applyButton",$this->applyButton);
		 
	     $this->okButton = new Button("OK");
	     $this->okButton->addActionListener("click",$this);
		 $this->bind("okButton",$this->okButton);
		 
	     $this->cancelButton = new Button("Cancel");
	     $this->cancelButton->addActionListener("click",$this);
		 $this->bind("cancelButton",$this->cancelButton);
	
}

	public function getSource() {
		return $this->__source;
	}
	
	public function unregister() {
		parent::unregister();
 		$this->cancelButton->unregister();
 		$this->applyButton->unregister();
 		$this->okButton->unregister();
 		$this->backgroundImageURL->unregister();
 		$this->tileCheckBox->unregister();
 		$this->nameInput->unregister();
	}
	
	public function actionPerformed($event) {
		if($event->getSource() === $this->cancelButton) {
			$this->hide();
			$this->unregister();
		}
 
	}


 }
?>
