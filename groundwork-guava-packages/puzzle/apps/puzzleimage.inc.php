<?php 
/**
 * @author Daniel Puertas <dpuertas@groundworkopensource.com>
 *
 */
class PuzzleImage extends Image {
	private $src;
	public $currentPosition;
	public $correctPosition;
	public $name;
	private $root;
	private $names;
	private $imageGrid;
	
	
	public function __construct($src,$correctPosition,$myName){
		$this->root = "/monitor/packages/puzzle/images/";
		$this->src= $src;
		$this->name = $myName;
		parent::__construct($this->src);
		$this->correctPosition = $correctPosition;
		$this->currentPosition = $correctPosition;
		//$this->names = array("one","two","three","four","five","six","seven","eight","nine");
		if(!isset($this->correctPosition)){
			throw new Exception("Error initializing PuzzleImage COR: $src, $correctPosition");
		}
		if(!isset($this->currentPosition)){
			throw new Exception("Error initializing PuzzleImage CUR: $src, $currentPosition");
		}
		if(!isset($this->src)){
			throw new Exception("Error initializing PuzzleImage SRC: $src, $currentPosition");
		}		
	}
	
    /**
     * Returns the IMG SRC string for the PuzzleImage
     *
     * @return String $src
     */
    public function getSrc(){
    	return $this->src;
    }
	/**
	 * Sets the current position of the PuzzleImage to a new square on the grid.
	 *
	 * @param int $pos
	 */
	public function setCurrentPosition($pos){
		global $guava;
		$this->currentPosition = $pos;
		$guava->console("COR $this->correctPosition set to $this->currentPosition");
	}
	
 
	/**
	 * Returns the current position of the blank square on the puzzle
	 *
	 * @param PuzzleImage $imageGrid
	 * @return int $blankPos
	 */
	public function getBlankPosition($imageGrid){
		$this->imageGrid = $imageGrid;
		$blankPos = NULL;
		$blankPos = $this->_checkNorth();
		if(!isset($blankPos)){
			$blankPos = $this->_checkSouth();
		}
		if(!isset($blankPos)){
			$blankPos = $this->_checkEast();
		}
		if(!isset($blankPos)){
			$blankPos = $this->_checkWest();
		}
		return $blankPos;
	}
	
	private function _checkNorth(){
		global $guava;
		$northPos = $this->currentPosition - 3;
		$guava->console("checking North at $northPos = " . $this->imageGrid[$northPos]->correctPosition);

		if($northPos < 1 || $northPos > 9){
			return null;
		}
		if($this->imageGrid[$northPos]->correctPosition == 9){
			return $northPos;
		}
	}
	private function _checkSouth(){
		global $guava;
		$southPos = $this->currentPosition + 3;
		$guava->console("checking South at $southPos = " . $this->imageGrid[$southPos]->correctPosition);

		if($southPos < 1 || $southPos > 9){
			return null;
		}
		if($this->imageGrid[$southPos]->correctPosition == 9){
			return $southPos;
		}		
	}
	
	private function _checkWest(){
		global $guava;
		$westPos = $this->currentPosition - 1;
				$guava->console("checking West at $westPos = " . $this->imageGrid[$westPos]->correctPosition);

		if($westPos < 1 || $westPos > 9){
			return null;
		}
		if($this->imageGrid[$westPos]->correctPosition == 9){
			return $westPos;
		}	
	}
		
	private function _checkEast(){
		global $guava;
		$eastPos = $this->currentPosition + 1;
		$guava->console("checking East at $eastPos = " . $this->imageGrid[$eastPos]->correctPosition);

		if($eastPos < 1 || $eastPos > 9){
			return null;
		}
		if($this->imageGrid[$eastPos]->correctPosition == 9){
			return $eastPos;
		}		
	}
}?>
