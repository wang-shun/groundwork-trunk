<?php
/** Our ExternalWidgetConfigureDialog extends GuavaWidgetConfigureDialog, which is the Dialog 
 * type which handles configuring widgets.
 *
 */
class ExternalWidgetConfigureDialog extends GuavaWidgetConfigureDialog implements ActionListener  {
	
	/**
	 * The input field to handle a new location/
	 *
	 * @var InputText
	 */
	private $locationField;

	/**
	 * The title input field of this widget.
	 *
	 * @var InputText
	 */
	private $widgetTitleField;

	/**
	 * The Button to trigger updating the location in our dialog.
	 *
	 * @var Button
	 */
	private $changeButton;
	
	/**
	 * Just a string to hold the current value of our dialog's location.
	 *
	 * @var string
	 */
	private $location;

	/**
	 * A string to hold the title of the Widget.
	 *
	 * @var string
	 */
	private $widgetTitle;

	private $_updateFrequencyField;

	private $actualWidget;
	/**
	 * In Dialog's constructor, we call our parent's constructor to properly initialize 
	 * the dialog.  The $source is a reference to the widget in which this dialog will configure.
	 *
	 * @param GuavaWidget $source
	 */
	public function __construct($source) {
		parent::__construct($source);
		$this->actualWidget = $source;

		$this->setTemplate(GUAVA_FS_ROOT . 'packages/gwwidgets/templates/externalconfig.xml');
		/**
		 * Get the value of the location in which the widget is currently pointing 
		 * to.
		 */
		$this->location = $source->getLocation();	
		$this->targetData("location", $this->location);

		$this->locationField = new InputText(50, 255);
		$this->targetData("locationField", $this->locationField);
		$this->locationField->setValue($source->getLocation());

		$this->widgetTitle = $source->getName();
		$this->targetData("widgetTitle", $this->widgetTitle); 
		$this->widgetTitleField = new InputText(50, 50);
		$this->targetData("widgetTitleField", $this->widgetTitleField);
		$this->widgetTitleField->setValue($source->getName());
		//$this->changeButton = new Button("Update Widget Parameters");
	//	$this->changeButton->addActionListener("click", $this);
		//$this->targetData("changeButton", $this->changeButton);
/*
		$this->_updateFrequencyField = new Select();
		$this->_updateFrequencyField->addOption('--never--', "Never");
		$this->_updateFrequencyField->addOption(  60, "Every  1 minute");
		$this->_updateFrequencyField->addOption( 300, "Every  5 minutes");
		$this->_updateFrequencyField->addOption( 600, "Every 10 minutes");
		$this->_updateFrequencyField->addOption( 900, "Every 15 minutes");
		$this->_updateFrequencyField->addOption(1800, "Every 30 minutes");
		$this->_updateFrequencyField->addOption(2700, "Every 45 minutes");
		$this->_updateFrequencyField->addOption(3600, "Every hour");
		$this->_updateFrequencyField->setValue('--never--');

		$this->_updateFrequencyField->addActionListener("click", $this);
		*/
		//$this->targetData("updateFrequencyField", $this->_updateFrequencyField->toString());
	}
	
	/**
	 * Perform regular cleanup.
	 *
	 */
	public function unregister() {
		parent::unregister();

		//$this->locationField->unregister();
		unset($this->locationField);

		//$this->widgetTitleField->unregister();
		unset($this->widgetTitleField);

		//$this->changeButton->unregister();
		unset($this->changeButton);

		//unset($this->_updateFrequencyField);

	}
	
	/**
	 * A method which will be called by the Widget we are configuring to get our new 
	 * location string.
	 *
	 * @return string
	 */
	public function getLocation() {
		return $this->location;
	}
	
	public function setLocation($loc){
		$this->location = $loc;
	}
	/**
	 * A method which will be called by the Widget we are configuring to get the Widget title.
	 *
	 * @return string
	 */
	public function getWidgetTitle() {
		return $this->widgetTitle;
	}

	public function setWidgetTitle($title){
		$this->widgetTitle = $title;
	}
	
	public function getUpdateFrequency() {
		//return $this->_updateFrequencyField->getValue();
	}

	/**
	 * Handle updating our location.
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) {
		parent::actionPerformed($event);
 
		/**
		 * If something ELSE triggered the event (usually the OK or close 
		 * button, then let's hide this dialog and call our unregister 
		 * method.  You must always do this for a ConfigureDialog in your 
		 * actionPerformed handler.
		 */

	 
	       $source = $event->getSource();
  		if ($source === $this->_updateFrequencyField) {
                       
		return;
		}
		
		$buttonLabel = $source->getLabel();
    
            
	     	if($buttonLabel == "OK"){  
			//no blanks allowed
			if (($this->locationField->getValue() == "") ||
			    ($this->widgetTitleField->getValue() == "")) {
				$dialog = new ErrorDialog("The Widget Title or Location items cannot be blank.");			
			        $dialog->show();
				return;
				}

			//no incomplete URLs allowed 				
			if(  	(preg_match('/\w+\:$/',$this->locationField->getValue())) ||
					(preg_match('/\w+\:\/$/',$this->locationField->getValue())) ||
					(preg_match('/\w+\:\/\/$/',$this->locationField->getValue()))
				){
				  	$dialog = new ErrorDialog("Please enter a complete URL");
				  	$dialog->show();
				  	return;
				  }

             		 
			$this->widgetTitle = $this->widgetTitleField->getValue();
 			$this->actualWidget->setTitle($this->widgetTitle);

			$this->location = $this->locationField->getValue();
			$this->actualWidget->setLocation($this->location);
  			
 			} //end OK

		else if($buttonLabel == "Cancel"){
				;
			}//end Cancel
		  	 
	      	$this->hide();
		$this->unregister();
		 

	}//end actionPerformed
	
	
}
?>