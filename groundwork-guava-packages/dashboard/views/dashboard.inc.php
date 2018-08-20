<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class DashboardViewerObject extends GuavaObject implements ActionListener {
	private $name;
	private $id;
	
	private $cloneImage;
	
	private $dashboard;
	
	public function __construct($id) {
		// $dialog = new InfoDialog ("In DashboardViewerObject");
		// $dialog->show();
		
		global $dashboarddaemon;
		parent::__construct();
		$this->setTemplate(GUAVA_FS_ROOT . "packages/dashboard/templates/dashboardviewer.xml");
//$this->setTemplate(GUAVA_FS_ROOT . "packages/dashboard/templates/dashboarddefault.xml");
		$this->id = $id;
		// Yay, let's get our characteristics
		$dashboard = $dashboarddaemon->getDashboard($this->id);

		if(!$dashboard) {
			throw new GuavaException("Dashboard with id: " . $this->id . " does not exist.");
		}
		$this->name = $dashboard['name'];

		$this->dashboard = new DashboardObject($this->id);
		//add default attributes for default dashboard?
		//this will set it for all dashboards
		//$this->dashboard->setBackgroundColor("#897343");
		$this->targetData("dashboard", $this->dashboard);

		$this->cloneImage = new Image(GUAVA_WS_ROOT . 'images/clone.gif');
		$this->cloneImage->addActionListener('click', $this);
		$this->targetData("clone", $this->cloneImage);
	}
	
	public function getName() {
		return $this->name;
	}
	
	public function getBackgroundColor() {
		return $this->dashboard->getBackgroundColor();
	}
	
	public function getBackgroundImageURL() {
		return $this->dashboard->getBackgroundImageURL();
	}
	
	public function getRepeatX(){
		return $this->dashboard->getRepeatX();
	}
	
	public function getRepeatY(){
		return $this->dashboard->getRepeatY();
	}
	
	public function getRepeatCSS(){
		return $this->dashboard->getRepeatCSS();
	}
	
	public function getRefresh(){
		return $this->dashboard->getRefresh();
	}
	
	//added
	public function setBackgroundColor($color) {
		return $this->dashboard->setBackgroundColor($color);
	}
	
	public function setBackgroundImageURL($url) {
		return $this->dashboard->setBackgroundImageURL($url);
	}
	
	public function setRepeatX($repeatX){
		return $this->dashboard->setRepeatX($repeatX);
	}
	
	public function setRepeatY($repeatY){
		return $this->dashboard->setRepeatY($repeatY);
	}
	
	public function setRefresh($refresh){
		return $this->dashboard->setRefresh($refresh);
	}
	
	public function unregister() {
		parent::unregister();
		$this->cloneImage->unregister();
		$this->dashboard->unregister();
	}
	
	public function actionPerformed($event) {
		// We want to clone
		global $guava;
		// We want to clone
		$tempObject = new DashboardObject($this->id);
		$guava->objectView($tempObject, 800, 600);
	}
}

class DashboardObject extends GuavaObject {
	
	private $name, $backgroundColor, $backgroundImageURL, $id;
	private $repeatX, $repeatY, $refresh, $refreshTimer;
	
	private $widgets;
	
	public function __construct($dashboardID) {
		// $dialog = new InfoDialog ("In DashboardObject");
		// $dialog->show();
		global $dashboarddaemon;
		parent::__construct();
		
		$this->widgets = array();
		$this->id = $dashboardID;
		// Yay, let's get our characteristics
		$dashboard = $dashboarddaemon->getDashboard($dashboardID);
		if(!$dashboard) {
			throw new GuavaException("Dashboard with id: " . $dashboardID . " does not exist.");
		}

		$this->name = $dashboard['name'];
		// $dashboard is populated from db, so $dashboard array keys may be different from variable names
		$this->backgroundColor = $dashboard['background_color'];
		$this->backgroundImageURL = $dashboard['background_image'];
		$this->repeatX = $dashboard['background_repeat_x'];
		$this->repeatY = $dashboard['background_repeat_y'];
		$this->refresh = $dashboard['refresh'];
		
		if(!empty($this->backgroundImageURL) and $this->refresh > 0)
		{
			$this->refreshTimer = new GuavaTimer(0,$this->refresh*60,$this,"refresh_background");
		}

		// Let's get our widgets!
		$widgetList = $dashboarddaemon->getWidgetsForDashboard($dashboardID);
		
		//print_r($widgetList);
		
		if(count($widgetList)) {
			foreach($widgetList as $widget) {
				$widgetProps = array();
				$widgetProps['name'] = $widget['name'];
				$widgetProps['zIndex'] = $widget['zindex'];
				$widgetProps['top'] = $widget['y'];
				$widgetProps['left'] = $widget['x'];
				$widgetProps['width'] = $widget['width'];
				$widgetProps['height'] = $widget['height'];
				$widgetProps['frames'] = true;
				$widgetProps['movable'] = false;
				$widgetProps['resizable'] = false;
				if(class_exists($widget['class'])) {
					$tempWidget = new $widget['class']($widgetProps, $widget['configuration']);
					$tempWidget->setEditable(false);
					$this->widgets[] = $tempWidget;
				}
			}
		}
		
	}
	
	public function getName(){
		return $this->name;
	}
	
	public function getBackgroundColor() {
		return $this->backgroundColor;
	}
	
	public function getBackgroundImageURL() {
		return $this->backgroundImageURL;
	}
	
	public function getRepeatX(){
		return $this->repeatX;
	}
	
	public function getRepeatY(){
		return $this->repeatY;
	}
	
	public function getRepeatCSS(){
		if ($this->repeatX && $this->repeatY){
			return "repeat";
		}elseif($this->repeatX){
			return "repeat-x";
		}elseif($this->repeatY){
			return "repeat-y";
		}else{
			return "no-repeat";
		}
		
	}
	
	public function getRefresh(){
		return $this->refresh;
	}
	
	public function setID($id) {
		$this->id = $id;
	}
	
	public function setName($name) {
		$this->name = $name;
	}
	
	public function setBackgroundColor($color) {
		$this->backgroundColor = $color;
	}
	
	public function setbackgroundImageURL($image) {
		$this->backgroundImageURL = $image;
	}
	
	public function setRepeatX($repeatX){
		$this->repeatX = $repeatX;
	}
	
	public function setRepeatY($repeatY){
		$this->repeatY = $repeatY;
	}
	
	public function setRefresh($refresh){
		$this->refresh = $refresh;
	}
	
	public function refresh_background(){
		// <div style="width: 100; background-image: url(my_ezh.jpg);">
		// <div style="width: 100; background-image: url(my_ezh.jpg?1213323);">
		// <div style="width: 100; background-image: url(my_ezh.jpg?121323232);">
		global $guava;
		
		//getIdentifier() is guava string used to identify this object
		$jsString = "document.getElementById('".$this->getIdentifier()."').style.backgroundImage = 'url(\'".$this->backgroundImageURL.'?'.mktime()."\')'";

		// $temp = new InfoDialog("Javascript to run: ".$jsString);
		// $temp->show();
		
		// Create new Guava Message with custom javascript, with type 'javascript'
		//$CONTEXT_FRAMEWORK = 'framework'; $TYPE_JAVASCRIPT = 'javascript'; $CDATA = 'cdata'
		//cdata is a type of text, but contains markup which needs to be encapsulated properly.
	
		$tempMessage = new GuavaMessage(GuavaMessage::$CONTEXT_FRAMEWORK, GuavaMessage::$TYPE_JAVASCRIPT);
		$tempMessage->addParameter(new GuavaMessageParameter("javascript", GuavaMessageParameter::$CDATA, $jsString));
		
		//deprecated - build message array by hand as in slideshow example
		//this does not work because there was nothing to handle style
		// $messageArray[] = array('name' => 'identifier', 'type' => 'text', 'value' => $this->getIdentifier());
		// $messageArray[] = array('name' => 'style', 'type' => 'text', 'value' => $this->getStyle());
		// $tempMessage = new GuavaMessage(MSG_TYPE_FRAMEWORK, MSG_FRAMEWORK_OBJECT, $messageArray);
		
		$guava->addMessage($tempMessage);
	}
	
	public function unregister() {
		parent::unregister();
		if(count($this->widgets)) {
		foreach($this->widgets as $widget) {
			$widget->unregister();
		}
		}
		$this->widgets = null;
	}
	
	private function getStyle(){
		$style = 'width: 100%; height: 100%;';
		if($this->backgroundColor != null) {
			$style .= 'background-color: '.$this->backgroundColor.';';
		}

		if($this->backgroundImageURL != null) {
			$style .= 'background-image: url(\''.$this->backgroundImageURL;
			if($this->refresh > 0)
			{
				$style .= '?'.mktime();
			}
			$style .= '\');';
			$style .= 'background-repeat: '.$this->getRepeatCSS().';';
		}	

		return $style;
	}
	
	public function Draw() {
		?>
		<div dojoType="LayoutContainer" layoutChildPriority="top-bottom" style="overflow: hidden; height: 100%; width: 100%">
		<div dojoType="ContentPane" layoutAlign="client" id="<?=$this->getIdentifier();?>" style="<?php 
		echo $this->getStyle();
		?>">
		<?php
		// Should print active widgets here
		if(!empty($this->widgets)) {
			foreach($this->widgets as $widget) {
				$widget->WidgetDraw();
			}			
		}
		?>
		</div>
		</div>
		<?php
	}
	
}

class DashboardOpenDialog extends Dialog implements ActionListener, GuavaMessageHandler   {
	
	private $cancelButton;
	
	private $id;
	public function __construct($handler) {
		// $dialog = new InfoDialog ("In DashboardOpenDialog");
		// $dialog->show();
		parent::__construct();
		$this->addActionListener("open", $handler);
		
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/dashboard/templates/dashboardopen.xml');
		
		// $this->userDefaultCheckBox = new InputCheckBox();
		// $this->userDefaultCheckBox->setIsChecked($userDefault);
		// $this->userDefaultCheckBox->addActionListener("check", $this);
		// $this->targetData("userDefault", $this->userDefaultCheckBox);
		
		$this->cancelButton = new Button("Cancel");
		$this->cancelButton->addActionListener("click", $this);
		$this->targetData("cancelButton", $this->cancelButton);
		
	}
	
	public function actionPerformed($event) {
		$this->hide();
		$this->unregister();
	}
	
	public function unregister() {
		parent::unregister();
		$this->cancelButton->unregister();
	}
	
	public function getID() {
		return $this->id;
	}
	
	public function processMessage($message) {
		// We want to open a dashboard
		$this->id = $message->getParameter("dashboard")->getValue();
		$this->invokeAction("open");
		$this->unregister();
		$this->hide();
	}
	
	public function getDashboards() {
		global $dashboarddaemon;
		global $guava;
		$dashboards = array();
		
		$userRoles = $guava->getAllRolesForUser($_SESSION['user_id']);
		//if user is admin, $role_id=1, show all dashboards
		if (in_array(1, $userRoles)){
			$tempDashboards = $dashboarddaemon->getAllDashboards();
		}else{
			//get all dashboard that user has read or write permissions
			$tempDashboards = $dashboarddaemon->getDashboardListForUser($_SESSION['user_id']);
		}

		
		if(count($tempDashboards)) {
			foreach($tempDashboards as $dashboard) {
				$dashboardInfo['name'] = $dashboard['name'];
				$dashboardInfo['id'] = $dashboard['id'];
				$userInfo = $guava->getUser($dashboard['uid']);
				if(!$userInfo) {
					$dashboardInfo['author'] = 'Author User Deleted';
				}
				else {
					$dashboardInfo['author'] = $userInfo['username'];
				}
				
				$dashboards[] = $dashboardInfo;
			}
		}
		return $dashboards;
	}
	
}


class DashboardView extends View implements ActionListener  {
	private $dashboard;
	
	private $dashboardID;
	
	function __construct() {
		// $dialog = new InfoDialog ("In DashboardView");
		// $dialog->show();
		parent::__construct("Dashboards");
		//this prints out "Open" when you first click on "Dashboard" from navigation
		$this->addMenuItem("Open", "open");
		//$this->setTemplate(GUAVA_FS_ROOT . 'packages/dashboard/templates/dashboarddefault.xml');
	}
	
	public function init() {
		global $dashboarddaemon, $guava;
		//$this->setTemplate(GUAVA_FS_ROOT . 'packages/dashboard/templates/dashboarddefault.xml');
		$this->dashboardID = $dashboarddaemon->getDefaultDashboardID($_SESSION['user_id']);
		if(!$this->dashboardID){
			//get system dashboard
			$this->dashboardID = $dashboarddaemon->getSystemDefaultDashboardID();
		}
		// $temp = new InfoDialog ("dashboardID:".$this->dashboardID);
		// $temp->show();
		$this->dashboard = new DashboardViewerObject($this->dashboardID);
		// $this->dashboard = new DashboardViewerObject(13);
		$this->targetData("content", $this->dashboard);
		// // Tell guava to reload
		// $tempMessage = new GuavaMessage(GuavaMessage::$CONTEXT_FRAMEWORK, GuavaMessage::$TYPE_RELOAD);
		// $guava->addMessage($tempMessage);
		// 
		// $this->targetData("content", 
		// 		"<h1>Welcome to GroundWork Dashboards</h1>
		// 		<table cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">
		// 		<tr>
		// 		<td width=\"80\">
		// 		</td>
		// 		<td>
		// 		<h1>About GroundWork Dashboards</h1>
		// 		Groundwork Dashboards is an easy way to open a dashboard that has been saved. You can view all dashboards you've created or created by your group or role.
		// 
		// 		<br />
		// 		<h1>To Get Started</h1>
		// 		Click \"Open\" and open a saved dashboard<br />
		// 		<br />
		// 		</td>
		// 		</tr>
		// 		</table>");
	}
	
	public function actionPerformed($event) {
		global $guava;
		if(isset($this->dashboard)) {
			$this->dashboard->unregister();
		}
		$id = $event->getSource()->getID();
		
		$this->dashboardID = $id;
		$this->dashboard = new DashboardViewerObject($id);
		
		$this->targetData("content", $this->dashboard);
		// Tell guava to reload
		$tempMessage = new GuavaMessage(GuavaMessage::$CONTEXT_FRAMEWORK, GuavaMessage::$TYPE_RELOAD);
		$guava->addMessage($tempMessage);
	}
	
	public function menuCommand($command) {
		switch($command) {
			case 'open':
				$dialog = new DashboardOpenDialog($this);
				$dialog->show();
				break;
		}
	}
 	
	public function close() {
		if(isset($this->dashboard)) {
			$this->dashboard->unregister();
		}
	}
	
	public function refresh() {
		// Empty
	}
	
	public function Draw() {
		//$this->setTemplate(GUAVA_FS_ROOT . 'packages/dashboard/templates/dashboarddefault.xml');
		$this->printTarget("content");
	}
}

?>
