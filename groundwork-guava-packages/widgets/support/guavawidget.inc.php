<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

final class GuavaWidgetRenameDialog extends Dialog implements ActionListener {
	private $oldName;
	private $newName;
	
	private $__source;
	
	private $submitButton;
	
	public function __construct($source, $oldname) {
		parent::__construct();
		$this->__source = $source;
		
		$this->oldName = $oldname;
		
		$this->newName = new InputText(20, 255, $oldname);
		
		$this->cancelButton = new Button("Cancel");
		$this->cancelButton->addActionListener("click",$this);
		$this->submitButton = new Button("OK");
		$this->submitButton->addActionListener("click", $this);
	}
	
	public function actionPerformed($event) {
	if($event->getSource() === $this->submitButton) {
		$this->__source->setName($this->newName->getValue());
	}
		$this->hide();
		$this->unregister();
	}
	
	public function unregister() {
		parent::unregister();
		$this->newName->unregister();
		if (isset($this->_timer)){
			$this->_timer->disable();
		}
	}
	
	public function Draw() {
		?>
		<h1>Rename <?=$this->oldName;?></h1>
		New Name: <?=$this->newName->Draw();?><br />
		<br />
		<table><tr><td>
		<?=$this->cancelButton->Draw();?></td>
		<td><? 
		   $this->submitButton->Draw();?>
		   </td></tr></table>
		<?php
	}
	
	
}

abstract class GuavaWidgetConfigureDialog extends Dialog implements ActionListener {
	
	private $__okButton;
	private $__cancelButton;
	private $__source;
	
	public function __construct($source) {
		parent::__construct();
		$this->__okButton = new Button("OK");
		$this->__okButton->addActionListener("click", $this);
		$this->__cancelButton = new Button("Cancel");
		$this->__cancelButton->addActionListener("click", $this);
		
		$this->__source = $source;
		
		$this->addActionListener('configured', $source);
	}
	
	public function getSource() {
		return $this->__source;
	}
	
	public function unregister() {
		parent::unregister();
		$this->__okButton->unregister();
		$this->__cancelButton->unregister();
	}
	
	public function actionPerformed($event) {
		if($event->getSource() === $this->__cancelButton) {
			$this->hide();
			$this->unregister();
		}
		else if($event->getSource() === $this->__okButton) {
			$this->invokeAction('configured');
		}
	}

	protected function DialogDraw() {
			?>
		<div id="<?=$this->getIdentifier();?>.__dialog" dojoType="Dialog" bgColor="<?=$this->bgColor;?>" bgOpacity="<?=$this->bgOpacity;?>" toggle="<?=$this->toggle;?>" toggleDuration="<?=$this->toggleDuration;?>">
		
		<div style="padding: 10px; width: 500px;">
		<?php
		$this->Draw();
		?>
		<hr />
			<div align="right">
				<?=$this->__okButton->Draw();?> <?=$this->__cancelButton->Draw();?>
			</div>
		</div>
		</div>
		<?php
	}
	
}


class GuavaWidget extends GuavaObject implements GuavaMessageHandler  {
    private $_widgetProps;      // array of default widget properties (see constructor)
    private $_moveHandlers;	// Not yet implemented
    
    private $_configClass;

    //variables for refresh rate
    private $_defaultRefreshRate = 30; //default refresh rate in seconds
    private $refreshRate;
    private $_timer;


  
    private $_menuItems;	// Custom menu items
    
    public function __construct($defaultWidgetProperties, $configObject = null) {
		parent::__construct();
		
		
		$this->_menuItems = array();
	
		// list of properties that are required by all widgets
		$requiredProps = array('name', 'zIndex', 'top', 'left', 
	                               'width', 'height', 'frames', 'movable', 'resizable');
	
		// cycle through properties and make sure they are all specified
		// throw exception if property is not to be found
		foreach ($requiredProps as $property) {
		    if (!isset($defaultWidgetProperties[$property])) {
			throw new GuavaException("Guava Widget Property: '" . $property . 
						 "' not specified during object construction.");
		    }
		}
	
		// all necessary properties exist so let's accept them
		$this->_widgetProps = $defaultWidgetProperties;
	
		$this->setEditable(true);

		$this->targetData("__name", $this->_widgetProps['name']);
		
		
		// initialize
		$this->init();
		
		// set refresh rate for widget
		if(!isset($this->refreshRate)){
			$this->refreshRate= $this->_defaultRefreshRate;
			}
		$this->_timer = new GuavaTimer(0,$this->refreshRate, $this, "update");
		

		if($configObject) {
		    $this->loadConfig($configObject);
		}
    }

    public function addMenuItem($label, $action, $handler) {
    	if($handler instanceof ActionListener) {
	    	$this->_menuItems[] = array('label' => $label, 'action' => $action);
	    	$this->addActionListener($action, $handler);
	    	return true;
    	}
    	else {
    		return false;
    	}
    }
    
    protected function setConfigClass($className) {
    	$this->_configClass = $className;
    }
    
    protected function init() {}
    
    // should be overriden for any widgets that require further custom configuration
    public function loadConfig($configObject) {}
    
    // Get/Set Methods
    public function setName($name)       {$this->_widgetProps['name']      = $name; $this->targetData("__name", $name);    return true;}
    public function setZIndex($zindex)   {$this->_widgetProps['zIndex']    = $zindex;  return true;}
    public function setHeight($height)   {$this->_widgetProps['height']    = $height;  return true;}
    public function setWidth($width)     {$this->_widgetProps['width']     = $width;   return true;}
    public function setTop($top)         {$this->_widgetProps['top']       = $top;     return true;}
    public function setLeft($left)       {$this->_widgetProps['left']      = $left;    return true;}
    public function setMovable($movable) {$this->_widgetProps['movable']   = $movable; return true;}
    public function setFrames($frames)   {$this->_widgetProps['frames']    = $frames;  return true;}
    public function setResizable($flag)  {$this->_widgetProps['resizable'] = $flag;    return true;}
    public function setEditable($flag) {$this->_widgetProps['editable'] = $flag; return true;}
    public function setRefreshRate($rate) {
    		$this->refreshRate = $rate; 
    		$this->_timer->disable();
    		$this->_timer = null;
    		unset($this->_timer);
			$this->_timer = new GuavaTimer(0,$this->refreshRate, $this, "update");
    		return true;}
    
    public function getRefreshRate()	   { return $this->refreshRate;}
    public function getName()            { return $this->_widgetProps['name'];      }
    public function getZIndex()          { return $this->_widgetProps['zIndex'];    }
    public function getHeight()          { return $this->_widgetProps['height'];    }
    public function getWidth()           { return $this->_widgetProps['width'];     }
    public function getTop()             { return $this->_widgetProps['top'];       }
    public function getLeft()            { return $this->_widgetProps['left'];      }
    public function isMovable()          { return $this->_widgetProps['movable'];   }
    public function hasFrames()          { return $this->_widgetProps['frames'];    }
    public function isResizable()        { return $this->_widgetProps['resizable']; }
    public function isEditable()		{ return $this->_widgetProps['editable']; }
    public function isConfigurable() { return ($this->_configClass != null); }

	public function disableRefresh(){
		$this->refreshRate = 0;
		$this->_timer->disable();
		$this->_timer = null;
		unset($this->_timer);
		return true;
	}
    // Not yet implemented
    public function addMoveHandler($name, $guavaObject, $method, $parameter = null) {}

    public function getConfigObject() { return null; }
    
    
    // Not yet implemented
    public function removeMoveHandler($name) {}

    public function WidgetDraw() {
    	$this->DrawOpen();
    	$this->Draw();
    	$this->DrawClose();
    }
    
	public function toString() {
		ob_start();
		$this->WidgetDraw();
		$buffer = ob_get_contents();
		ob_end_clean();
		return $buffer;
	}
	
	private function DrawOpen() {
		?>
		<table id="<?=$this->getIdentifier();?>" border="0" cellspacing="0" cellpadding="0" style="z-index: <?=$this->getZIndex();?>; position: absolute; left: <?=$this->getLeft();?>px; top: <?=$this->getTop();?>px; width: <?=$this->getWidth();?>px; height: <?=$this->getHeight();?>px;">
			<tr>
				<td  style="width: 1px; background: #0081c4; height: 3px;"><!--top border--></td>
				<td <?php if($this->isResizable()) { ?>onmousedown="gwwidgets.core.WidgetManager.ResizeTop('<?=$this->getIdentifier();?>', event);"<?php } ?> style="cursor: n-resize; background: #0081c4;"></td>
				<td  style="width: 4px; background: #0081c4; height: 1px;"><!--right border--></td>

			</tr>
			<tr>
				<td <?php if($this->isResizable()) { ?>onmousedown="gwwidgets.core.WidgetManager.ResizeLeft('<?=$this->getIdentifier();?>', event);"<?php } ?> rowspan="3" style="cursor: w-resize; background: #0081c4; width: 4px;"><!--left border--></td>
				<td id="<?=$this->getIdentifier();?>_header" <?php if($this->isMovable()) { ?>onmousedown="gwwidgets.core.WidgetManager.Move('<?=$this->getIdentifier();?>', event);"<?php } ?> style="overflow: hidden; background-image: url('packages/widgets/images/windowHeader.png'); background-position: 0% 0%; background-repeat: repeat-x; height: 18px; font-size: 10px; font-weight: bold; color: #333333;"><div style="overflow: hidden; white-space: nowrap;"><?=$this->printTarget("__name");?></div></td>
				<td <?php if($this->isResizable()) { ?>onmousedown="gwwidgets.core.WidgetManager.ResizeRight('<?=$this->getIdentifier();?>', event);"<?php } ?> rowspan="3" style="cursor: e-resize; background: #0081c4; width: 1px;"></td>
			</tr>
			<tr>
				<td style="background: #cccccc; height: 1px;"></td>
			</tr>
			<tr>
				<td valign="top" align="left" style="background-image: url('packages/widgets/images/windowBg.png'); background-position: 0% 0%; background-repeat: repeat-x; background-color: #ebebea;">
				<div id="<?=$this->getIdentifier();?>_content" dojoType="ContentPane" style="height: <?=($this->getHeight() - 21);?>px; width: <?=($this->getWidth() - 4);?>px; overflow: auto;">
		<?php
	}
	
	private function DrawClose() {
		// Draws the close of the widget, if frame is provided, frame as well
		?>
				</div>
				</td>
			</tr>
			<tr>
				<td  style="width: 1px; background: #0081c4; height: 1px;"></td>
				<td <?php if($this->isResizable()) {?>onmousedown="gwwidgets.core.WidgetManager.ResizeBottom('<?=$this->getIdentifier();?>', event);"<?php } ?> style=" cursor: s-resize; background: #0081c4;"></td>
				<td  style="width: 1px; background: #0081c4; height: 3px;"><!--bottom border--></td>
			</tr>
		</table>
		
		<?php
		if($this->isEditable()) {
			?>
			<div dojoType="PopupMenu2" targetNodeIds="<?=$this->getIdentifier();?>_header"><?php
				foreach($this->_menuItems as $menuItem) {
					?><div dojoType="MenuItem2" caption="<?=$menuItem['label'];?>" onclick="addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: '<?=$this->getIdentifier();?>'}, {name: 'action', type: 'string', value: '<?=$menuItem['action'];?>'}]); sendMessageQueue();"></div><?php
				}
				?><div dojoType="MenuItem2" caption="Rename" onclick="addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: '<?=$this->getIdentifier();?>'}, {name: 'action', type: 'string', value: 'rename'}]); sendMessageQueue();"></div><?php
				if($this->isConfigurable()) {
					?><div dojoType="MenuItem2" caption="Configure..." onclick="addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: '<?=$this->getIdentifier();?>'}, {name: 'action', type: 'string', value: 'configure'}]); sendMessageQueue();"> </div><?php
				}
				?></div>
			<?php
		}
	}
	
	
	public function processMessage($message) {
		if($message->parameterExists('action')) {
			if($message->getParameter('action')->getValue() == 'move') {
				$this->setTop($message->getParameter('top')->getValue());
				$this->setLeft($message->getParameter('left')->getValue());
				$this->setHeight($message->getParameter('height')->getValue());
				$this->setWidth($message->getParameter('width')->getValue());
			}
			if($message->getParameter('action')->getValue() == 'configure') {
				if(isset($this->_configClass)) {
					$className = $this->_configClass;
					$configDialog = new $className($this);
					$configDialog->show();
				}
			}
			if($message->getParameter('action')->getValue() == 'rename') {
				$renameDialog = new GuavaWidgetRenameDialog($this, $this->getName());
				$renameDialog->show();
			}
			else {
				$this->invokeAction($message->getParameter('action')->getValue());
			}
		}
	}
	

	
	
	public function reload() {}
}

?>
