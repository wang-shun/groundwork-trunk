<?php
require_once(GUAVA_FS_ROOT . 'packages/puzzle/apps/puzzleimage.inc.php');

/**
 * @author Daniel Puertas <dpuertas@groundworkopensource.com>
 *
 */
class PuzzleApp extends GuavaApplication{

	private $names;
	//image objects
	private $oneImage, $twoImage,$threeImage,$fourImage,$fiveImage,$sixImage,$sevenImage,$eightImage,$nineImage;
	private $imageGrid;
	private $randButton;
	
	public function __construct(){
		parent::__construct("puzzle");

		$this->setTemplate(GUAVA_FS_ROOT . "packages/puzzle/templates/puzzle.xml");
		$this->names = array("INVALID","one","two","three","four","five","six","seven","eight","nine");
	}

	 
	public function init(){
		$this->randButton = new Button("Start!");
		$this->randButton->addClickListener("click", $this, "randomize");
	 	$this->bind("randButton", $this->randButton);

		for($pos=1;$pos<10;$pos++){
			$imageObjName = $this->names[$pos] . "Image";
			$imageSrc = "/monitor/packages/puzzle/images/" . $pos . ".jpg";
			$this->$imageObjName = new PuzzleImage("$imageSrc",$pos,"$imageObjName");
			$this->imageGrid[$pos] = $this->$imageObjName;	
			$this->$imageObjName->addClickListener("click",$this,"positionSwapper");	
			$this->targetData("$imageObjName",$this->$imageObjName->toString());			
		} 	
		
		 
	
	}
	
	/**
	 * swaps the position of the Images and Redraws
	 *
	 * @param PuzzleImage $myImage
	 * @param unknown_type $parameter
	 */
	public function positionSwapper($myImage,$parameter = null){
		global $guava;
		$blackpos = $myImage->currentPosition;
		$guava->console("CLICKED IMAGE $myImage->correctPosition CURRENT POS: " . $blackpos);
		$whitepos = $myImage->getBlankPosition($this->imageGrid);
		//if the click is valid (adjacent to white square), swap the images
		if(isset($whitepos)){
			$this->_swap($whitepos,$blackpos);
		}
		
		if($this->puzzleIsCorrect()){
			
			$info = new InfoDialog("You won!");
			$info->show();
		}
	}
	
	/**
	 * checks if the pieces are all in their correct positions
	 * @return true/false
	 */
	private function puzzleIsCorrect(){
		$isCorrect = true;

		for($i=1;$i<=9;$i++){
			$testObj = $this->imageGrid[$i];
			if($testObj->correctPosition != $testObj->currentPosition){
				$isCorrect = false;
				 
			}
		}

		return $isCorrect;
	}
	
	private function _swap($whitepos,$blackpos){
		    global $guava;
		    $guava->console("WHITE POSITION: " . $whitepos);

			// update piece's current position
			//$this->imageGrid[$pos]->setCurrentPosition($whitepos);
			$this->imageGrid[$blackpos]->setCurrentPosition($whitepos);
			$this->imageGrid[$whitepos]->setCurrentPosition($blackpos);
			
			// move object to new place on imageGrid Array
			$white = $this->imageGrid[$whitepos];
			$black = $this->imageGrid[$blackpos];
			
			$this->imageGrid[$whitepos] = $black;
			$this->imageGrid[$blackpos] = $white;

			$guava->console($this->imageGrid[$whitepos]->correctPosition . " should be $white->currentPosition");
			//get Targets for objects 
			$blackLoc = $this->names[$blackpos] . "Image";
			$whiteLoc = $this->names[$whitepos] . "Image";
			 
			//swap positions on display
			if(isset($white) && isset($black)){
			$this->targetData("$blackLoc",$white->toString());
			$this->targetData("$whiteLoc",$black->toString());
			}
	
	}
	
	public function randomize(){
		
		for($pos=1;$pos<10;$pos++){
			$rnd = rand(1,9);
			$this->_swap($pos,$rnd);
		}
	}
	
	public function close(){
	}
}

?>
