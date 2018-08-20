<?php
/*
Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms.
*/

//include_once("/usr/local/groundwork/guava/packages/dashboard/views/dashboardproperties.inc.php");

class DashboardBuilderLoadDialog extends Dialog implements ActionListener, GuavaMessageHandler   {

	private $cancelButton;
	private $openButton;

	private $id;
	public function __construct($handler) {
		// $dialog = new InfoDialog ("In DashboardBuilderLoadDialog");
		// $dialog->show();

		parent::__construct();
		$this->addActionListener("load", $handler);

		$this->setTemplate(GUAVA_FS_ROOT . 'packages/dashboard/templates/dashboardload.xml');

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
		// $temp = new InfoDialog("Loading dashboard id: ".$this->id);
		// $temp->show();
		$this->invokeAction("load");
		$this->unregister();
		$this->hide();
	}

	public function getDashboards() {
		global $dashboarddaemon;
		global $guava;
		$dashboards = array();

		//	$tempDashboards = $dashboarddaemon->getWritableDashboardsForUser($_SESSION['user_id']);

		$userRoles = $guava->getAllRolesForUser($_SESSION['user_id']);
		//if user is admin, $role_id=1, show all dashboards
		if(in_array(1, $userRoles)) {
			$tempDashboards = $dashboarddaemon->getAllDashboards();
		} else {
			$tempDashboards = $dashboarddaemon->getDashboardListForUser($_SESSION['user_id'],'write');
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

class DashboardCreateDialog extends Dialog implements ActionListener, GuavaMessageHandler  {

	private $name;
	private $backgroundColor;
	private $backgroundImageURL;

	private $fileBrowseButton;
	private $repeatXCheckBox;
	private $repeatYCheckBox;
	private $refresh;

	private $myForm;
	private $submitButton;
	private $cancelButton;

	private $errorMsg;

	function __construct($handler) {
		global $dashboarddaemon;
		// $dialog = new InfoDialog ("In DashboardCreateDialog");
		// $dialog->show();

		parent::__construct();
		$this->setTemplate(GUAVA_FS_ROOT . 'packages/dashboard/templates/dashboardcreate.xml');

		$this->addActionListener("finish", $handler);

		$this->name = new InputText(20, 50);
		$this->targetData("name", $this->name);

		$this->backgroundColor = "#FFF77B";
		$this->targetData("backgroundcolor", "<div style=\"background: " . $this->backgroundColor . "; border: 1px solid black; width:20px; height:20px;\"></div>");

		// $this->backgroundImageURL = new InputText(20, 255);
		// $this->targetData("backgroundImageURL", $this->backgroundImageURL);

		$images = $dashboarddaemon->getBackgroundImages();
		if(!empty($images)) {
			$this->backgroundImageURL = new InputSelect();
			$this->backgroundImageURL->addOption('',"No Background");
			foreach($images as $image) {
				$this->backgroundImageURL->addOption($image['url'],$image['name']);
			}
			$this->backgroundImageURL->addActionListener("select",$this);
			$this->targetData("backgroundImageURL", $this->backgroundImageURL);
		}

		// $this->fileBrowseButton = new Button("Browse");
		// $this->fileBrowseButton->addActionListener("click", $this);
		// //?
		// $this->targetData("fileBrowse", $this->fileBrowseButton);

		//$this->repeatXCheckBox = new CheckBox("Repeat X");
		$this->repeatXCheckBox = new InputCheckBox();
		$this->repeatXCheckBox->addActionListener("check", $this);
		$this->repeatXCheckBox->setValue(true);
		$this->targetData("repeatX", $this->repeatXCheckBox);

		//$this->repeatYCheckBox = new CheckBox("Repeat Y");
		$this->repeatYCheckBox = new InputCheckBox();
		$this->repeatYCheckBox->addActionListener("check", $this);
		$this->repeatYCheckBox->setValue(true);
		$this->targetData("repeatY", $this->repeatYCheckBox);

		$this->refresh = 0;
		//$this->refresh = new InputText(20, 255, $refresh);
		//$this->targetData("refresh", $this->refresh);

		$this->submitButton = new Button("OK");
		$this->targetData("submitButton", $this->submitButton);
		$this->submitButton->addActionListener("click", $this);

		$this->cancelButton = new Button("Cancel");
		$this->cancelButton->addActionListener("click", $this);
		$this->targetData("cancel", $this->cancelButton);

		$this->errorMsg = new CodeDiv();
		$this->targetData("errormsg", "");
	}

	public function unregister() {
		parent::unregister();

		$this->name->unregister();
		$this->backgroundImageURL->unregister();

		//$this->fileBrowseButton->unregister();
		$this->repeatXCheckBox->unregister();
		$this->repeatYCheckBox->unregister();

		$this->cancelButton->unregister();
		$this->submitButton->unregister();

		if(isset($this->errorMsg)) {
			$this->errorMsg->unregister();
		}
	}

	public function getName() {
		return $this->name->getValue();
	}

	public function getBackgroundColor() {
		return $this->backgroundColor;
	}

	public function getBackgroundImageURL() {
		return $this->backgroundImageURL->getValue();
	}

	public function getRepeatX() {
		return $this->repeatXCheckBox->isChecked();
	}

	public function getRepeatY() {
		return $this->repeatYCheckBox->isChecked();
	}

	public function getRefresh() {
		return $this->refresh;
	}

	public function processMessage($message) {
		if($message->parameterExists('setcolor')) {
			$this->backgroundColor = $message->getParameter('setcolor')->getValue();
			$this->targetData("backgroundcolor", "<div style=\"background: " . $message->getParameter('setcolor')->getValue() . "; border: 1px solid black; width: 20px; height:20px;\"></div>");
		}
	}

	//why do we need another cleanup similar to unregister?
	private function cleanup() {
		$this->cancelButton->unregister();
		$this->name->unregister();
		$this->backgroundImageURL->unregister();
		$this->submitButton->unregister();
	}

	/**
	 * Enter description here...
	 *
	 * @param ActionEvent $event
	 */
	public function actionPerformed($event) {
		global $dashboarddaemon;
		if($event->getSource() === $this->cancelButton) {
			$this->cleanup();
			$this->hide();
		}
		else {
			if(trim($this->name->getValue()) == "") {
				$this->errorMsg->setContent("<div align=\"center\" style=\"color: red;\">Name is required.</div>");
				$this->targetData("errormsg", $this->errorMsg);
			}
			else {
				// Check for dashboard with existing name
				if($dashboarddaemon->dashboardExists($this->name->getValue())) {
					// var_dump($this->name->getValue());
					$this->errorMsg->setContent("<div align=\"center\" style=\"color: red;\">A dashboard with the name " . $this->name->getValue() . " already exists!</div>");
					$this->targetData("errormsg", $this->errorMsg);
					return;
				}
				else {
					$this->invokeAction("finish");
					$this->cleanup();
					$this->hide();
				}
			}
		}
	}
}

//this draws Widget List on top left when you are in Dashboard Builder
class DashboardBuilderWidgetListItem extends GuavaObject implements Droppable {

	private $name;
	private $description;

	function __construct($item) {
		// $dialog = new InfoDialog ("In DashboardBuilderWidgetListItem");
		// $dialog->show();

		parent::__construct();
		$this->name = $item['name'];
		$this->description = $item['description'];
	}

	public function getName() {
		return $this->name;
	}

	public function getDescription() {
		return $this->description;
	}

	public function Draw() {
			?>
			<div style="background-image: url('packages/dashboard/images/widgetName.png'); background-position: 0% 0%; background-repeat: repeat-x; height: 18px; font-size: 10px; font-weight: normal; color: #000000;">
			  &nbsp;
			<span id="<?=$this->getIdentifier();?>"><?=$this->description;?></span></div>
			<script type="text/javascript">
			new dojo.dnd.HtmlDragSource(byId('<?=$this->getIdentifier();?>'), 'widgets');
			</script>
			<?php
	}
}

class DashboardBuilderWidgetList extends GuavaWidget    {

	private $widgetList;

	public function __construct() {
		$widgetProp['name'] = "Widget List";
		$widgetProp['zIndex'] = 5000;
		$widgetProp['top'] = 5;
		$widgetProp['left'] = 5;
		$widgetProp['width'] = 200;
		$widgetProp['height'] = 300;
		$widgetProp['frames'] = true;
		$widgetProp['movable'] = true;
		$widgetProp['resizable'] = true;

		parent::__construct($widgetProp);
		global $widgetdaemon;
		$this->widgetList = array();
		$tempList = $widgetdaemon->getWidgets();
		foreach($tempList as $item) {
			$widget = new DashboardBuilderWidgetListItem($item);
			$this->widgetList[] = $widget;
		}
	}

	public function unregister() {
		parent::unregister();
		foreach($this->widgetList as $widget) {
			$widget->unregister();
		}
	}

	public function Draw() {
		global $widgetdaemon;
		foreach($this->widgetList as $widget) {
			$widget->Draw();
		}
	}
}

class DashboardBuilderCanvasWidgetCollection extends GuavaObject implements ActionListener {
	private $widgets;

	public function __construct() {
		// $dialog = new InfoDialog ("In DashboardBuilderCanvasWidgetCollection");
		// $dialog->show();

		parent::__construct();
		$this->widgets = array();
	}

	public function addWidget($widget) {
		$this->widgets[] = $widget;

		$widget->addMenuItem("Delete", "delete", $this);
		$this->invokeAction("update");
	}

	public function unregister() {
		foreach($this->widgets as $widget) {
			$widget->unregister();
		}
	}

	public function actionPerformed($event) {
		$counter = 0;
		for($counter = 0; $counter < count($this->widgets); $counter++) {
			if($this->widgets[$counter] === $event->getSource()) {
				$this->widgets[$counter]->unregister();
				array_splice($this->widgets, $counter, 1);
				$this->invokeAction("update");
			}
		}
	}

	public function getWidgets() {
		return $this->widgets;
	}

	public function Draw() {
		foreach($this->widgets as $widget) {
			if($widget instanceof GuavaWidget) {
				$widget->WidgetDraw();
			}
			//$widget->Draw();
		}
	}
}

// draws the dashboard edit view ?
class DashboardBuilderCanvas extends GuavaObject implements GuavaMessageHandler, ActionListener, DropTarget {

	private $name;
	private $backgroundColor;
	private $backgroundImageURL;

	private $repeatX;
	private $repeatY;
	private $refresh;

	private $widgetList;

	private $dashboardProperties;

	private $widgetCollection;

	public $clone; //true or false

	private $id; //dashboard id

	function __construct($id,$name, $backgroundColor = null, $backgroundImageURL = null, $repeatX = 0, $repeatY = 0, $refresh = 0) {
		// $dialog = new InfoDialog ("In DashboardBuilderCanvas");
		// $dialog->show();

		parent::__construct();

		$this->id = $id;
		$this->name = $name;
		$this->backgroundColor = $backgroundColor;
		$this->backgroundImageURL = $backgroundImageURL;

		$this->repeatX = $repeatX;
		$this->repeatY = $repeatY;
		$this->refresh = $refresh;

		$this->widgetCollection = new DashboardBuilderCanvasWidgetCollection();
		$this->widgetCollection->addActionListener("update", $this);
		$this->targetData("widgets", $this->widgetCollection);

		$this->widgetList = new DashboardBuilderWidgetList();

		$this->dashboardProperties = new DashboardPropertiesWidget($id, $name, $backgroundColor, $backgroundImageURL, $repeatX, $repeatY, $refresh);

		$this->targetData("widgetlist", $this->widgetList);
		$this->targetData("properties", $this->dashboardProperties);

		// $this->id = null;
	}

	public function unregister() {
		parent::unregister();

		$this->dashboardProperties->unregister();
		// 	$this->id->unregister();

		$this->widgetList->unregister();
		$this->widgetCollection->unregister();
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

	public function setBackgroundImageURL($image) {
		$this->backgroundImageURL = $image;
	}

	public function setRepeatX($repeatX) {
		$this->repeatX = $repeatX;
	}

	public function setRepeatY($repeatY) {
		$this->repeatY = $repeatY;
	}

	public function setRefresh($refresh) {
		$this->refresh = $refresh;
	}

	public function getID() {
		return $this->id;
	}

	public function getWidgets() {
		return $this->widgetCollection->getWidgets();
	}

	public function getName() {
		return $this->name;
	}

	public function getBackgroundColor() {
		return $this->backgroundColor;
	}

	public function getBackgroundImageURL() {
		return $this->backgroundImageURL;
	}

	public function getRepeatX() {
		// return 1;
		return $this->repeatX;
	}

	public function getRepeatY() {
		return $this->repeatY;
	}

	public function getRefresh() {
		return $this->refresh;
	}

	public function dropped($source, $x, $y) {
		global $widgetdaemon;
		global $guava;

		$widgetprops['name'] = $source->getDescription();
		$widgetprops['zIndex'] = (count($this->widgets) + 1) ;
		$widgetprops['top'] = $x;
		$widgetprops['left'] = $y;
		$widgetprops['width'] = "300";
		$widgetprops['height'] = "400";
		$widgetprops['frames'] = true;
		$widgetprops['movable'] = true;
		$widgetprops['resizable'] = true;
		$tempWidget = $widgetdaemon->createWidget($source->getName(), $widgetprops);

		$this->widgetCollection->addWidget($tempWidget);
	}

	public function addWidget($widget) {
		//$widget->addMenuItem("Delete", "delete", $this);
		$this->widgetCollection->addWidget($widget);
		$this->invokeAction("update");
	}

	public function Draw() {
		?>
		<div dojoType="LayoutContainer" layoutChildPriority="top-bottom" style="overflow: hidden; height: 100%; width: 100%">
		<div dojoType="ContentPane" layoutAlign="client" id="<?=$this->getIdentifier();?>" style="width: 100%; height: 100%; <?php
		$this->printTarget("content");
		if($this->backgroundColor != null) {
			?>
			background-color: <?=$this->backgroundColor;?>;
			<?php
		}
		if($this->backgroundImageURL != null) {
			?>
			background-image: url('<?=$this->backgroundImageURL;?>');
			<?php
			//need to check for == 1?
			if($this->repeatX and $this->repeatY) {
				?>
				background-repeat: repeat;
				<?php
			}elseif($this->repeatX) {
				?>
				background-repeat: repeat-x;
				<?php
			}elseif($this->repeatY) {
			?>
				background-repeat: repeat-y;
			<?php
			} else {
			?>
				background-repeat: no-repeat;
			<?php
			}
		}
		?>">
		<?php
		// Should print active widgets here

		$this->printTarget("widgets");
		$this->printTarget("widgetlist");
		$this->printTarget("properties");
		?>
		</div>
		</div>

		<script type="text/javascript">
		new GuavaDropTarget(byId('<?=$this->getIdentifier();?>'), 'widgets');
		</script>
		<?php
		/*
		$buffer = "<div id=\"". $this->getIdentifier() . "\" style=\"overflow: hidden; border: 1px solid grey; height: ".$this->height ."; width: " .$this->width ."; background-color: " .$this->backgroundColor .";";
		if($this->backgroundImageURL <> "") {
			$buffer .= " background-image: url(" . $this->backgroundImageURL . ");";
		}
		$buffer .= "\">";

		foreach($this->widgets as $widget) {
			$buffer .= $widget->toString();
		}

		$buffer .= "</div>

		print($buffer);
		*/
	}

	public function processMessage($message) {
		$dialog = new InfoDialog("Got message!");
		$dialog->show();
	}

	public function actionPerformed($event) {
		// could be from a widget!
		$this->targetData("widgets", $this->widgetCollection);
	}
}

class DashboardBuilderSaveDialogPrivs extends GuavaObject implements ActionListener {
	private $typeSelect;
	private $idSelect;
	private $addButton;

	private $deleteLinks;

	private $privs;
	private $writePrivs;

	function __construct() {
		// $dialog = new InfoDialog ("In DashboardBuilderSaveDialogPrivs");
		// $dialog->show();

		global $guava;
		parent::__construct();

		$this->setTemplate(GUAVA_FS_ROOT . "packages/dashboard/templates/dashboardsave.xml");
		// Populate the select fields
		$this->idSelect = new InputSelect();
		$this->targetData("idSelect", $this->idSelect);
		$this->typeSelect = new Select();
		$userList = $guava->getNonAdminUserList();

		if(sizeof($userList)>0) {
			$this->typeSelect->addOption("user", "User");
			//default typeSelect on user, this generates value drop down list for this
			foreach($userList as $user) {
				$this->idSelect->addOption($user['user_id'], $user['username']);
			}
		}

		$groupList = $guava->getGroupList();
		if(sizeof($groupList)>0) {
			$this->typeSelect->addOption("group", "Group");
			if($this->idSelect->Size() == 0) {
				foreach($groupList as $group) {
					$this->idSelect->addOption($group['group_id'], $group['name']);
				}
			}

		}
		$roleList = $guava->getNonAdminRoleList();
		if(sizeof($roleList)>0) {
			$this->typeSelect->addOption("role", "Role");
			if($this->idSelect->Size() == 0) {
				foreach($roleList as $role) {
					$this->idSelect->addOption($role['role_id'], $role['name']);
				}
			}
		}
		$this->typeSelect->addActionListener("click", $this);
		$this->targetData("typeSelect", $this->typeSelect);

		//added for read/write access of dashboards
		$this->accessSelect = new Select();
		$this->accessSelect->addOption("read", "read");
		$this->accessSelect->addOption("write", "write");
		$this->accessSelect->addActionListener("click", $this);
		$this->targetData("accessSelect", $this->accessSelect);

		//to add privileges
		$this->addButton = new Button("Add");
		$this->targetData("addButton", $this->addButton);
		$this->addButton->addActionListener("click", $this);

		//need to addButton to add write privileges or use same button?

		//need a separate deleteLinks for write?
		$this->deleteLinks = array();
		$this->deleteWriteLinks = array();

		$this->privs = array();
		$this->writePrivs = array();

		$this->targetData("privList", "" );
		$this->targetData("privWriteList", "");
	}

	//addPrivilege for read access
	public function addPrivilege($type, $id) {
		global $guava;
		switch($type) {
			case 'user':
				$userInfo = $guava->getUser($id);
				if($userInfo) {
					$priv_array = array('type' => 'user', 'id' => $id, 'access' => 'read', 'name' => $userInfo['username']);
					if(!$this->privExists($priv_array,'read')) {
						$this->privs[] = $priv_array;
					}
				}
				break;
			case 'group':
				$groupInfo = $guava->getGroup($id);
				if($groupInfo) {
					$priv_array = array('type' => 'group', 'id' => $id, 'access' => 'read', 'name' => $groupInfo['name']);
					if(!$this->privExists($priv_array,'read')) {
						$this->privs[] = $priv_array;
					}
				}
				break;
			case 'role':
				$roleInfo = $guava->getRole($id);
				if($roleInfo) {
					$priv_array = array('type' => 'role', 'id' => $id, 'access' => 'read', 'name' => $roleInfo['name']);
					if(!$this->privExists($priv_array,'read')) {
						$this->privs[] = $priv_array;
					}
				}
				break;
		}
		$this->rebuildListTarget();
	}

	// Added to allow groups/roles/users to edit a dashboard if they have 'write' set to 1 in privileges table
	public function addWritePrivilege($type,$id) {
		global $guava;
		switch($type) {
			case 'user':
				$userInfo = $guava->getUser($id);
				if($userInfo) {
					$priv_array = array('type' => 'user', 'id' => $id, 'access' => 'write', 'name' => $userInfo['username']);
					if(!$this->privExists($priv_array,'write')) {
						$this->writePrivs[] = $priv_array;
					}
				}
				break;
			case 'group':
				$groupInfo = $guava->getGroup($id);
				if($groupInfo) {
					$priv_array = array('type' => 'group', 'id' => $id, 'access' => 'write', 'name' => $groupInfo['name']);
					if(!$this->privExists($priv_array,'write')) {
						$this->writePrivs[] = $priv_array;
					}
				}
				break;
			case 'role':
				$roleInfo = $guava->getRole($id);
				if($roleInfo) {
					$priv_array = array('type' => 'role', 'id' => $id, 'access' => 'write', 'name' => $roleInfo['name']);
					if(!$this->privExists($priv_array,'write')) {
						$this->writePrivs[] = $priv_array;
					}
				}
				break;
		}
		$this->rebuildListTarget();
	}

	//checks if array already exists in either writePrivs or privs array
	private function privExists($priv_array,$access) {
		$priv_array_name = ($access == 'write') ? 'writePrivs' : 'privs';
		if($access == 'read') {
			foreach ($this->privs as $priv) {
				if($priv == $priv_array) {
					return true;
				}
			}
		}
		elseif($access == 'write') {
			foreach ($this->writePrivs as $priv) {
				if($priv == $priv_array) {
					return true;
				}
			}
		}
		return false;
	}

	private function rebuildListTarget() {

		foreach($this->deleteLinks as $link) {
			$link->unregister();
		}
		$this->deleteLinks = array();

		// $temp = new InfoDialog("Read Privileges array".var_export($this->privs));
		// $temp->show();

		foreach($this->privs as $priv) {
			$tempLink = new TextLink("Delete");
			$tempLink->addActionListener("click", $this);
			$this->deleteLinks[] = $tempLink;
			$buffer .= '<div style="background: #dddddd; height: 25px; border-width: 1px 0px 1px 0px; border-style: solid; border-bottom-color: black; border-left-color: grey; border-right-color: black; border-top-color: grey;"><table width="450"><tr><td>'.$priv['type'] . '  :  ' . $priv['name'] .' </td><td align="right">' . $tempLink->toString() . '</td></tr></table></div>';
		}

		$this->targetData("privList", $buffer);
		unset($buffer);

		// $temp = new InfoDialog("Write Privileges array".var_export($this->writePrivs));
		// $temp->show();

		foreach($this->deleteWriteLinks as $link) {
			$link->unregister();
		}
		$this->deleteWriteLinks = array();

		foreach($this->writePrivs as $writePriv) {
			$tempLink = new TextLink("Delete");
			$tempLink->addActionListener("click", $this);
			$this->deleteWriteLinks[] = $tempLink;
			$buffer .= '<div style="background: #dddddd; height: 25px; border-width: 1px 0px 1px 0px; border-style: solid; border-bottom-color: black; border-left-color: grey; border-right-color: black; border-top-color: grey;"><table width="450"><tr><td>'.$writePriv['type'] . ': ' . $writePriv['name'] .'</td><td align="right">' . $tempLink->toString() . '</td></tr></table></div>';
		}

		$this->targetData("privWriteList", $buffer);
		unset($buffer);
	}

	function getPrivs() {
		return $this->privs;
	}

	function getWritePrivs() {
		return $this->writePrivs;
	}

	function unregister() {
		parent::unregister();
		$this->typeSelect->unregister();
		$this->idSelect->unregister();
		$this->accessSelect->unregister();
		$this->addButton->unregister();
		foreach($this->deleteLinks as $link) {
			$link->unregister();
		}
		foreach($this->deleteWriteLinks as $link) {
			$link->unregister();
		}
	}

	function actionPerformed($event) {
		global $guava;
		if($event->getSource() === $this->typeSelect) {
			//clear values for values dropdown when typeSelect is repicked
			$this->idSelect->removeAll();
			switch($this->typeSelect->getValue()) {
				case 'user':
					$userList = $guava->getNonAdminUserList();
					foreach($userList as $user) {
						$this->idSelect->addOption($user['user_id'], $user['username']);
					}
					break;
				case 'group':
					$this->groupSelect = new InputSelect();
					$groupList = $guava->getGroupList();
					foreach($groupList as $group) {
						$this->idSelect->addOption($group['group_id'], $group['name']);
					}
					break;
				case 'role':
					$this->roleSelect = new InputSelect();
					$roleList = $guava->getNonAdminRoleList();
					foreach($roleList as $role) {
						$this->idSelect->addOption($role['role_id'], $role['name']);
					}
					break;
			}
			$this->targetData("idSelect", $this->idSelect);
		} elseif($event->getSource() === $this->addButton) {
			if($this->accessSelect->getValue() == 'read') {
				$this->addPrivilege($this->typeSelect->getValue(),$this->idSelect->getValue());
			}elseif($this->accessSelect->getValue() == 'write') {
				$this->addWritePrivilege($this->typeSelect->getValue(),$this->idSelect->getValue());
			}
			// switch($this->typeSelect->getValue()) {
			// 	case 'user':
			// 		$userInfo = $guava->getUser($this->idSelect->getValue());
			// 		// $this->privs[] = array('type' => 'user', 'id' => $this->idSelect->getValue(), 'name' => $userInfo['username']);
			// 		if($this->accessSelect->getValue() == 'read') {
			// 			$this->privs[] = array('type' => 'user', 'id' => $this->idSelect->getValue(),'access' => $this->accessSelect->getValue(), 'name' => $userInfo['username']);
			// 		} else { //write access
			// 			$this->writePrivs[] = array('type' => 'user', 'id' => $this->idSelect->getValue(),'access' => $this->accessSelect->getValue(), 'name' => $userInfo['username']);
			// 		}
			// 		break;
			// 	case 'group':
			// 		$groupInfo = $guava->getGroup($this->idSelect->getValue());
			// 		// $this->privs[] = array('type' => 'group', 'id' => $this->idSelect->getValue(), 'name' => $groupInfo['name']);
			// 		if($this->accessSelect->getValue() == 'read') {
			// 			$this->privs[] = array('type' => 'group', 'id' => $this->idSelect->getValue(),'access' => $this->accessSelect->getValue(), 'name' => $userInfo['username']);
			// 		} else { //write access
			// 			$this->writePrivs[] = array('type' => 'group', 'id' => $this->idSelect->getValue(),'access' => $this->accessSelect->getValue(), 'name' => $userInfo['username']);
			// 		}
			// 		break;
			//
			// 	case 'role':
			// 		$roleInfo = $guava->getRole($this->idSelect->getValue());
			// 		// $this->privs[] = array('type' => 'role', 'id' => $this->idSelect->getValue(), 'name' => $roleInfo['name']);
			// 		if($this->accessSelect->getValue() == 'read') {
			// 			$this->privs[] = array('type' => 'role', 'id' => $this->idSelect->getValue(),'access' => $this->accessSelect->getValue(), 'name' => $userInfo['username']);
			// 		} else { //write access
			// 			$this->writePrivs[] = array('type' => 'role', 'id' => $this->idSelect->getValue(),'access' => $this->accessSelect->getValue(), 'name' => $userInfo['username']);
			// 		}
			// 		break;
			// }
			// $this->rebuildListTarget();
		} else {
			// We're trying to delete
			$counter = 0;
			for($counter = 0; $counter < count($this->deleteLinks); $counter++) {
				if($this->deleteLinks[$counter] === $event->getSource()) {
					array_splice($this->privs, $counter, 1);
					$this->rebuildListTarget();
					return;
				}
			}

			// delete items from write privilege section
			$counter = 0;
			for($counter = 0; $counter < count($this->deleteWriteLinks); $counter++) {
				if($this->deleteWriteLinks[$counter] === $event->getSource()) {
					array_splice($this->writePrivs, $counter, 1);
					$this->rebuildListTarget();
					return;
				}
			}
		}
	}
}

class DashboardBuilderSaveDialog extends Dialog implements ActionListener {

	private $globalCheckbox;

	private $canvasName;

	private $canvas;

	private $privObject;

	private $saveButton;
	private $cancelButton;

	function __construct($canvas,$name) {
		// $dialog = new InfoDialog ("In DashboardBuilderSaveDialog");
		// $dialog->show();

		global $guava;
		global $dashboarddaemon;
		parent::__construct();

		$this->canvas = $canvas;

		$this->canvasName = $name;

		$this->globalCheckbox = new CheckBox();
		$this->globalCheckbox->addActionListener("click", $this);

		$this->globalCheckbox->setIsChecked(true);

		$this->targetData("privs", "");
		$this->targetData("writePrivs", "");

		$this->saveButton = new Button("Save");
		$this->saveButton->addActionListener("click", $this);

		$this->cancelButton = new Button("Cancel");
		$this->cancelButton->addActionListener("click", $this);

		$this->privObject = new DashboardBuilderSaveDialogPrivs();

		$oldID = $this->canvas->getID();
		if($oldID) {
			// The dashboard exists.  Let's load up any existing privileges
			//will get values for dashboardid, type (user,group,role), target_id, and write
			$privileges = $dashboarddaemon->getDashboardPrivileges($oldID);
			foreach($privileges as $priv) {
				if($priv['write'] == 0) {
					$this->privObject->addPrivilege($priv['type'], $priv['target_id']);
				} else {
					//add only when there is write privileges
					$this->privObject->addWritePrivilege($priv['type'], $priv['target_id']);
				}
			}
		}
	}

	public function unregister() {
		parent::unregister();
		$this->globalCheckbox->unregister();
		$this->privObject->unregister();
	}

	public function actionPerformed($event) {
		global $dashboarddaemon;
		if($event->getSource() === $this->cancelButton) {
			$this->unregister();
			$this->hide();
		} elseif($event->getSource() === $this->globalCheckbox) {
			$this->hide();
			if($this->globalCheckbox->isChecked()) {
				$this->targetData("privs", "");
				//$this->targetData("writePrivs","");
			}
			else {
				$this->targetData("privs", $this->privObject);
				//$this->targetData("writePrivs", $this->privObject);
			}
			$this->show();
		} elseif($event->getSource() === $this->saveButton) {
			// Time to save!

			if($this->canvas->getID() == null) {
				// It's a new dashboard
				// public function createDashboard($name, $backgroundColor, $backgroundImageURL, $repeatX, $repeatY, $refresh, $global, $uid) {
				// $temp = new InfoDialog("Saving new dashboard ".$this->canvas->getName());
				// $temp->show();

				$newID = $dashboarddaemon->createDashboard($this->canvas->getName(), $this->canvas->getBackgroundColor(), $this->canvas->getBackgroundImageURL(), $this->canvas->getRepeatX(), $this->canvas->getRepeatY(), $this->canvas->getRefresh(), $this->globalCheckbox->isChecked() ? 1 : 0, $_SESSION['user_id']);
				// $temp = new InfoDialog("ID returning from createDashbaord: ".$newID);
				// $temp->show();
				if($newID) {
					$widgets = $this->canvas->getWidgets();

					foreach($widgets as $widget) {
						$configObject = $widget->getConfigObject();
						$dashboarddaemon->addDashboardWidget($newID, $widget->getName(), get_class($widget), $widget->getTop(), $widget->getLeft(), $widget->getWidth(), $widget->getHeight(), $widget->getZIndex(), $configObject);
					}

					$this->canvas->setID($newID);
					$dialog = new InfoDialog("Successfully saved dashboard: ".$this->canvas->getName() );
					$dialog->show();
					// $dialog = new InfoDialog("NEWID: ".$this->canvas->getID() );
					// $dialog->show();
					$this->unregister();
					$this->hide();

				}
				//not global privileges
				if(!$this->globalCheckbox->isChecked()) {
					// We have to provide additional privileges
					$privs = $this->privObject->getPrivs();
					foreach($privs as $priv) {
						$dashboarddaemon->addDashboardPrivilege($newID, $priv['type'], $priv['id'],'read');
					}
					$writePrivs = $this->privObject->getWritePrivs();
					foreach($writePrivs as $priv) {
						$dashboarddaemon->addDashboardPrivilege($newID, $writePriv['type'], $writePriv['id'],'write');
					}
				}
			}
			else { //saving edited dashboard
				// $temp = new InfoDialog("Saving editted dashboard ID".$this->canvas->getID());
				// 				$temp->show();
				//first remove dashboard
				$dashboarddaemon->clearDashboard($this->canvas->getID());
				// public function createDashboard($name, $backgroundColor, $backgroundImageURL, $repeatX, $repeatY, $refresh, $global, $uid) {
				// then add back info with updated data

				$dashboarddaemon->saveDashboard($this->canvas->getID(), $this->canvas->getName(), $this->canvas->getBackgroundColor(), $this->canvas->getBackgroundImageURL(), $this->canvas->getRepeatX(), $this->canvas->getRepeatY(), $this->canvas->getRefresh(), $this->globalCheckbox->isChecked() ? 1 : 0, $_SESSION['user_id']);
					$widgets = $this->canvas->getWidgets();

					foreach($widgets as $widget) {
						$configObject = $widget->getConfigObject();
						$dashboarddaemon->addDashboardWidget($this->canvas->getID(), $widget->getName(), get_class($widget), $widget->getTop(), $widget->getLeft(), $widget->getWidth(), $widget->getHeight(), $widget->getZIndex(), $configObject);
					}

					if(!$this->globalCheckbox->isChecked()) {
						// We have to provide additional privileges
						$privs = $this->privObject->getPrivs();
						foreach($privs as $priv) {
							$dashboarddaemon->addDashboardPrivilege($this->canvas->getID(), $priv['type'], $priv['id'],$priv['access']);
							}

						$writePrivs = $this->privObject->getWritePrivs();
						foreach($writePrivs as $writePriv) {
							$dashboarddaemon->addDashboardPrivilege($this->canvas->getID(), $writePriv['type'], $writePriv['id'],$writePriv['access']);
						}
					}
					$dialog = new InfoDialog("Successfully saved dashboard: ".$this->canvas->getName() );
					$dialog->show();
					$this->unregister();
					$this->hide();

				// Rewriting an existing dashboard
			}
		}
	}

	public function Draw() {
		?>
		<div style="width: 600px">
		<h1>Save Dashboard <?=$this->canvasName?></h1>
		<br />
		<?php $this->globalCheckbox->Draw();?> Globally Accessible?<br />
		<br />
	    <?php $this->printTarget("privs"); ?><br />
		<br />

		<?php $this->saveButton->Draw();?> <?php $this->cancelButton->Draw();?>
		</div>
		<?php
	}
}

class DashboardPropertiesWidget extends GuavaWidget implements ActionListener, GuavaMessageHandler   {
	private $ID;
	private $name;
	private $backgroundColor;

	private $backgroundImageURL;
	private $repeatXCheckBox;
	private $repeatYCheckBox;
	private $refresh;

//	private $fileBrowseButton;
	private $modifyButton;
	private $saveButton;
	private $deleteButton;
	private $cloneButton;

	public function __construct($id, $name, $backgroundColor = null, $backgroundImageURL = null, $repeatX = 0, $repeatY = 0, $refresh = 0) {
		global $dashboarddaemon;
		// $dialog = new InfoDialog ("In DashboardPropertiesWidget: $id $name $backgroundColor $backgroundImageURL $repeatX $repeatY $refresh");
		// $dialog->show();

		$widgetProp['name'] = "Dashboard Properties";
		$widgetProp['zIndex'] = 5001;
		$widgetProp['top'] = 315;
		$widgetProp['left'] = 5;
		$widgetProp['width'] = 300;
		$widgetProp['height'] = 350;
		$widgetProp['frames'] = true;
		$widgetProp['movable'] = true;
		$widgetProp['resizable'] = true;
		$this->isDefault = false;
		$this->ID = $id;
		// $this->name = $name;
		// $this->backgroundColor = $backgroundColor;
		// $this->backgroundImageURL = $backgroundImageURL;
		// $this->repeatX = $repeatX;
		// $this->repeatY = $repeatY;
		// $this->refresh = $refresh;

		parent::__construct($widgetProp);
		$this->setTemplate(GUAVA_FS_ROOT . "packages/dashboard/templates/dashboardproperties.xml");
		// $tmp = new InfoDialog ("dashboard name: $name");
		// $tmp->show();

		// Can't change name to default dashboards
		$this->name = new InputText(20, 50, $name);

		if(in_array($name, array('systemdefault'))) {
			$this->isDefault = true;
		}

		$this->targetData("name", $this->name);

		$this->backgroundColor = $backgroundColor;
		$this->targetData("backgroundcolor", "<div style=\"background: " . $this->backgroundColor . "; border: 1px solid black; width: 20px; height:20px;\"></div>");

		// $this->backgroundImageURL = new InputText(20, 255, $backgroundImageURL);
		// $this->targetData("backgroundImageURL", $this->backgroundImageURL);

		// $this->backgroundAddForm = new Form();
		// $this->backgroundAddForm->addListener("addbackground", $this, "backgroundAddHandler", null);
		$images = $dashboarddaemon->getBackgroundImages();
		if(!empty($images)) {
			$this->backgroundImageURL = new InputSelect();
			$this->backgroundImageURL->addOption('',"No Background");
			foreach($images as $image) {
				$this->backgroundImageURL->addOption($image['url'],$image['name']);
			}
			$this->backgroundImageURL->setValue($backgroundImageURL);
			$this->backgroundImageURL->addActionListener("select",$this);
			$this->targetData("backgroundImageURL", $this->backgroundImageURL);
		}
		// $this->backgroundAddButton = new SubmitButton("Add Background");

		// $this->fileBrowseButton = new Button("Browse");
		// $this->fileBrowseButton->addActionListener("click", $this);
		// $this->targetData("fileBrowse", $this->fileBrowseButton);

		//$this->repeatXCheckBox = new CheckBox("Repeat X");
		$this->repeatXCheckBox = new InputCheckBox();
		$this->repeatXCheckBox->setIsChecked($repeatX);
		$this->repeatXCheckBox->addActionListener("check", $this);
		$this->targetData("repeatX", $this->repeatXCheckBox);

		//$this->repeatYCheckBox = new CheckBox("Repeat Y");
		$this->repeatYCheckBox = new InputCheckBox();
		$this->repeatYCheckBox->setIsChecked($repeatY);
		$this->repeatYCheckBox->addActionListener("check", $this);
		$this->targetData("repeatY", $this->repeatYCheckBox);

		$this->refresh = 0;
		//$this->refresh = new InputText(20, 255, $refresh);
		//$this->targetData("refresh", $this->refresh);

		$this->modifyButton = new Button("Update Properties");
		$this->modifyButton->addActionListener("click", $this);
		$this->targetData("modify", $this->modifyButton);

		$this->saveButton = new Button("Save Dashboard");
		$this->saveButton->addActionListener("click", $this);
		$this->targetData("save", $this->saveButton);

		//systemdefault dashboard can not be deleted
		if($id != $dashboarddaemon->getSystemDefaultDashboardID()) {
			$this->deleteButton = new Button("Delete Dashboard");
			$this->deleteButton->addActionListener("click", $this);
			$this->targetData("delete", $this->deleteButton);
		}

		$this->cloneButton = new Button("Clone Dashboard");
		$this->cloneButton->addActionListener("click", $this);
		$this->targetData("clone", $this->cloneButton);
	}

	public function processMessage($message) {
		// $temp = new InfoDialog("inside of DashboardPropertiesWidget's Processor");
		// $temp->show();
		if($message->parameterExists('setcolor')) {
			$this->backgroundColor = $message->getParameter('setcolor')->getValue();
			$this->targetData("backgroundcolor", "<div style=\"background: " . $message->getParameter('setcolor')->getValue() . "; border: 1px solid black; width: 20px; height:20px;\"></div>");
		}
		//do we need to add anything for imageURL,repeatX,repeatY, and refresh?
	}

	public function actionPerformed($event) {
		global $guava;

		if($event->getSource() === $this->saveButton) {
			$myName = $this->name->getValue();
			if($this->isDefault && !(in_array($myName, array('systemdefault')))) {
				$err = new ErrorDialog("You may not change the name of the system default dashboard");
				$err->show();
				return;
			}
			// Let's broadcast a message to our dashboard application to save the dashboard
			$tempMessage = new GuavaMessage("dashboardbuilder", "save");
			// $guava->getProcessor()->processMessage($tempMessage);

		}
		if($event->getSource() === $this->modifyButton) {
			$tempMessage = new GuavaMessage("dashboardbuilder", "modify");

			// this will call processMessage in DashboardBuilderView, looks like
			// $guava->getProcessor()->processMessage($tempMessage);
		}
		if($event->getSource() === $this->deleteButton) {
			$tempMessage = new GuavaMessage("dashboardbuilder", "delete");
		}
		if($event->getSource() === $this->cloneButton) {
			$this->isDefault = false;
			$tempMessage = new GuavaMessage("dashboardbuilder", "clone");
			//$tempMessage->addParameter(new GuavaMessageParameter("name", "string", $this->name.'_copy'));
			// $guava->getProcessor()->processMessage($tempMessage);
		}
		//add all canvas info to message

		$tempMessage->addParameter(new GuavaMessageParameter("ID", "string", $this->ID));

		if(isset($this->name) && $this->name instanceof GuavaObject) {
			//field exists
			$tempMessage->addParameter(new GuavaMessageParameter("name", "string", $this->name->getValue()));
		} else {
			//no field, it is already assigned
			$tempMessage->addParameter(new GuavaMessageParameter("name", "string", $this->name));
		}

		$tempMessage->addParameter(new GuavaMessageParameter("backgroundColor", "string", $this->backgroundColor));
		$tempMessage->addParameter(new GuavaMessageParameter("backgroundImageURL", "string", $this->backgroundImageURL->getValue()));
		$tempMessage->addParameter(new GuavaMessageParameter("repeatX", "string", $this->repeatXCheckBox->isChecked()));
		$tempMessage->addParameter(new GuavaMessageParameter("repeatY", "string", $this->repeatYCheckBox->isChecked()));
		$tempMessage->addParameter(new GuavaMessageParameter("refresh", "string", $this->refresh));
		// var_dump($tempMessage);
		//process message
		$guava->getProcessor()->processMessage($tempMessage);
	}

	public function unregister() {
		parent::unregister();
		if(!in_array($this->name, array('systemdefault')) && !is_string($this->name)) {
			$this->name->unregister();
		}
		// $this->backgroundColor->unregister();

		$this->backgroundImageURL->unregister();
		$this->repeatXCheckBox->unregister();
		$this->repeatYCheckBox->unregister();
		//$this->refresh->unregister();

		// $this->fileBrowseButton->unregister();
		$this->modifyButton->unregister();
		$this->saveButton->unregister();
		if(isset($this->deleteButton)) {
			$this->deleteButton->unregister();
		}
		$this->cloneButton->unregister();
	}
}

class DashboardBuilderView extends GuavaApplication implements ActionListener, GuavaMessageHandler  {

	private $canvas;

	// dashboard stats

	function __construct() {
		global $guava;
		// $dialog = new InfoDialog ("In DashboardBuilderView");
		// $dialog->show();

		parent::__construct("DashboardBuilder");
	}

	public function init() {
		global $guava;
		$this->addMenuItem("New", "new");
		$this->addMenuItem("Open", "load");
//		$this->addMenuItem("Save","save");
//		$this->addMenuItem("Save As...","saveas");
//		$this->addMenuItem("Close","close");
//		$this->addMenuItem("Properties","properties");
//		$this->addMenuItem("Share","share");

		// Let's add a dashboardbuilder context and listen for it
		$processor = $guava->getProcessor();
		$processor->addContext('dashboardbuilder');
		$processor->addContextListener('dashboardbuilder', $this);

		//$this->targetData("content", '');
		$this->targetData("content",
			"<div style=\"width:100%; height: 100%;\">
			<div style=\"padding:10px;\"><h1>Welcome to GroundWork Monitor Dashboard Builder</h1></div>
			<table cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">
			<tr>
			<td width=\"80\">
			</td>
			<td>
			<h1>About Dashboard Builder</h1>
			The Dashboard Builder feature enables you to quickly and easily build custom role-based management Dashboards which are viewable in the Dashboards application.

			<br />
			<h1>To Get Started</h1>
			Click \"New\" to create a new dashboard. Click \"Load\" to edit an existing Dashboard. <br />
			<br />
			</td>
			</tr>
			</table></div>");
	}

	public function processMessage($message) {
		global $dashboarddaemon,$canvas;
		// $temp = new InfoDialog("inside of DashboardBuilderView's Processor");
		// $temp->show();
		// var_dump($message);
		$orig_name = $this->canvas->getName();

		if(!$message->getParameter("ID")) {
			// var_dump($this->canvas);
			$id = $this->canvas->getID();
			if($message->getType() == "deleteDashboardConfirm") {
				// $temp = new InfoDialog("confirmed dashboard deletion");
				// $temp->show();

				$dashboarddaemon->deleteDashboard($id);
				$this->targetData("content", "");
			} elseif($message->getType() == "cancelDashboardConfirm") {
				// $temp = new InfoDialog("cancel dashboard deletion");
				// $temp->show();
			}
		} else {
			$id = $message->getParameter("ID")->getValue();

			// $temp = new InfoDialog("Getting ID from message:$id");
			// $temp->show();
			// var_dump("ID: ".$id);

			$new_name = $message->getParameter("name")->getValue();
			$new_backgroundColor = $message->getParameter("backgroundColor")->getValue();
			$new_backgroundImageURL = $message->getParameter("backgroundImageURL")->getValue();
			$new_repeatX = $message->getParameter("repeatX")->getValue();
			$new_repeatY = $message->getParameter("repeatY")->getValue();
			$new_refresh = 0;//$message->getParameter("refresh")->getValue();

			// $this->canvas->setID($id);
			// var_dump($this->canvas->getID());
			// $temp = new InfoDialog("After setting canvas id, id is ".$this->canvas->getID());
			// $temp->show();
			$this->canvas->setName($new_name);
			$this->canvas->setBackgroundColor($new_backgroundColor);
			$this->canvas->setBackgroundImageURL($new_backgroundImageURL);
			$this->canvas->setRepeatX($new_repeatX);
			$this->canvas->setRepeatY($new_repeatY);
			//$this->canvas->setRefresh($new_refresh);

			if($message->getType() == 'save') {
				if($this->canvas->clone) {
					$this->canvas->setID(null);

					// $dialog = new InfoDialog("This is clone -> Canvas ID: ".$this->canvas->getID() );
					// $dialog->show();
					// var_dump($new_name);
					// var_dump($message);

					//if canvas is a clone, make sure name does not exists
					if($dashboarddaemon->dashboardExists($new_name)) {
						$dialog = new ErrorDialog("A dashboard with name: ".$new_name." already exists!");
						$dialog->show();
					} else {
						// $temp = new InfoDialog("A clone named ".$new_name." does not exist.");
						// $temp->show();
				          $this->canvas->clone = false;
						$dialog = new DashboardBuilderSaveDialog($this->canvas,$new_name);
						$dialog->show();
					}
				} else {
					// var_dump ($this->canvas);
					// $temp = new InfoDialog("Not a clone. Canvas Id: ".$this->canvas->getID());
					// $temp->show();
					// not a clone
					// straightforward save

					$dialog = new DashboardBuilderSaveDialog($this->canvas,$orig_name);
					$dialog->show();
				}
			} elseif($message->getType() == "delete") {
				$id = $this->canvas->getID();
				// if($dashboardID = $this->canvas->getID()) {
				if($id) {
					// create new confirm dialog, and tell it to send the result to 'dashboardbuilder' context

					//dashboardbuilder will be the context so use the processMessage of class DashboardBuilderView
					$usercount = $dashboarddaemon->findUsersForDashboard($id);
					$temp = new ConfirmDialog("Do you really want to delete dashboard $new_name? There are $usercount users of this dashboard.",'dashboardbuilder',"deleteDashboardConfirm","deleteDashboardCancel");
					$temp->show();
				} else {
					// $dialog = new InfoDialog("Canvas Name: ".$this->canvas->getName().",ID:".$this->canvas->getID());
					// $dialog->show();
					$dialog = new InfoDialog("Dashboard does not yet exist. Please save first.");
					$dialog->show();
				}
			} elseif($message->getType() == "clone") {
				$this->canvas->clone = true;

				if($dashboardID = $this->canvas->getID()) {
					$this->canvas->setID(null);

					$dialog = new InfoDialog("Dashboard cloned.  You are now working with a copy of the original dashboard.");
					$dialog->show();
				} else {
					$dialog = new InfoDialog("This dashboard has not yet been saved, therefore no clone can be made.");
					$dialog->show();
				}
			}
			else { // update is default
				// check for duplicate name first!
				if($dashboarddaemon->dashboardExists($message->getParameter("name")->getValue()) && $message->getParameter("name")->getValue() != $this->canvas->getName()) {
					$dialog = new ErrorDialog("A dashboard with the name " . $message->getParameter("name")->getValue() . " already exists!");
					$dialog->show();
				} else {
					// $t = new InfoDialog("Repeat X".$message->getParameter("repeatX")->getValue());
					// $t->show();
					// $this->canvas->setID($message->getParameter("ID")->getValue());
					$this->canvas->setName($message->getParameter("name")->getValue());
					$this->canvas->setBackgroundColor($message->getParameter("backgroundColor")->getValue());
					$this->canvas->setBackgroundImageURL($message->getParameter("backgroundImageURL")->getValue());
					$this->canvas->setRepeatX($message->getParameter("repeatX")->getValue());
					$this->canvas->setRepeatY($message->getParameter("repeatY")->getValue());
					//$this->canvas->setRefresh($message->getParameter("refresh")->getValue());
					// $this->canvas->Draw();
					// $t = new InfoDialog("Inside of Dashboard Builder View: RepeatX: ".$this->canvas->getRepeatX()."RepeatY: ".$this->canvas->getRepeatY()."Refresh: ".$this->canvas->getRefresh()."backgroundImageURL: ".$this->canvas->getBackgroundImageURL());
					// $t->show();

					$this->targetData("content", $this->canvas);
					$dialog = new InfoDialog("Dashboard properties modified.  You must save your dashboard for changes to take effect.");
					$dialog->show();
				}
			}
		}
	}

	public function menuCommand($command) {
		if($command == "new") {
			$dialog = new DashboardCreateDialog($this);
			$dialog->show();
		} elseif($command == "load") {
			$dialog = new DashboardBuilderLoadDialog($this);
			$dialog->show();
		} elseif($command == "properties") {
			$dialog = new DashboardPropertiesDialog($this);
			$dialog->show();
		}
	}

	public function actionPerformed($event) {
		global $dashboarddaemon;
		global $guava;
		if($event->getAction() == "load") {
			if(isset($this->canvas)) {
				$this->canvas->unregister();
			}
			$dashboard = $dashboarddaemon->getDashboard($event->getSource()->getID());
			// var_dump($dashboard);

			$this->canvas = new DashboardBuilderCanvas($dashboard['id'],$dashboard['name'], $dashboard['background_color'], $dashboard['background_image'], $dashboard['background_repeat_x'], $dashboard['background_repeat_y'], $dashboard['refresh']);
			// deprecated - dashboard canvas id set in __construct now
			// $this->canvas->setID($dashboard['id']);

			// Let's get our widgets!
			$widgetList = $dashboarddaemon->getWidgetsForDashboard($dashboard['id']);

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
					$widgetProps['movable'] = true;
					$widgetProps['resizable'] = true;
					if(class_exists($widget['class'])) {
						$tempWidget = new $widget['class']($widgetProps, $widget['configuration']);
						$this->canvas->addWidget($tempWidget);
					}
				}
			}
			$this->targetData("content", $this->canvas);
			$tempMessage = new GuavaMessage(GuavaMessage::$CONTEXT_FRAMEWORK, GuavaMessage::$TYPE_RELOAD);
			$guava->addMessage($tempMessage);
		}
		else {
			$dialog = $event->getSource();
			if(isset($this->canvas)) {
				$this->canvas->unregister();
			}

			//first element of constructor is null because dashboard has just been created and not saved.
			$this->canvas = new DashboardBuilderCanvas(null,$dialog->getName(), $dialog->getBackgroundColor(), $dialog->getBackgroundImageURL(), $dialog->getRepeatX(), $dialog->getRepeatY(), $dialog->getRefresh());

			//$this->builderUI = new DashboardBuilderUI($dialog->getName(), $dialog->getWidth(), $dialog->getHeight(), $dialog->getBackgroundColor(), $dialog->getBackgroundImageURL(), $dialog->getWidgetFrames());
			$this->targetData("content", $this->canvas);
			$tempMessage = new GuavaMessage(GuavaMessage::$CONTEXT_FRAMEWORK, GuavaMessage::$TYPE_RELOAD);
			$guava->addMessage($tempMessage);
		}
	}

	public function close() {
		global $guava;
		if(isset($this->canvas))
			$this->canvas->unregister();
		if(isset($this->dashboardWidgetList))
			$this->dashboardWidgetList->unregister();
		if(isset($this->dashboardWidgetList))
			$this->dashboardWidgetList->unregister();
		$processor = $guava->getProcessor();
		$processor->removeContextListener("dashboardbuilder", $this);
		parent::unregister();
	}

	public function refresh() {
		// Empty
	}

	public function Draw() {
		$this->printTarget("content");
	}
}

?>
